package com.webtoapp.core.kernel

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.UserAgentMode
import com.webtoapp.data.model.UserAgentVersions
import org.junit.Test

/**
 * Guards [BrowserIdentityResolver]: a page must never see a `User-Agent` that contradicts the
 * client hints describing the same request.
 *
 * A `User-Agent` string and its `Sec-CH-UA*` metadata used to be produced by two independent
 * settings (`userAgentMode` chose the string, `kernelFlavor` chose the metadata). Setting them
 * inconsistently made the engine advertise its real brands while the UA claimed a different
 * browser — a contradiction anti-bot systems read as spoofing. These tests pin the pair together.
 */
class BrowserIdentityTest {

    private fun resolve(
        flavor: KernelFlavor = KernelFlavor.SYSTEM_DEFAULT,
        legacyMode: UserAgentMode = UserAgentMode.DEFAULT,
        customUserAgent: String? = null,
        desktopMode: Boolean = false,
        desktopUserAgent: String? = null,
        legacyUserAgent: String? = null,
        deviceDisguiseUserAgent: String? = null
    ) = BrowserIdentityResolver.resolve(
        flavor = flavor,
        legacyMode = legacyMode,
        customUserAgent = customUserAgent,
        desktopMode = desktopMode,
        desktopUserAgent = desktopUserAgent,
        legacyUserAgent = legacyUserAgent,
        deviceDisguiseUserAgent = deviceDisguiseUserAgent
    )

    @Test
    fun `nothing selected is a no-op so the engine keeps its own consistent pair`() {
        val identity = resolve()

        assertThat(identity.isNoOp).isTrue()
        assertThat(identity.userAgent).isNull()
        assertThat(identity.profile).isNull()
    }

    @Test
    fun `every resolved identity pairs its UA with metadata describing that same UA`() {
        // The invariant that matters, exercised across the whole input matrix: for any identity
        // that disguises at all, the UA actually sent and the metadata that generates the client
        // hints must come from one and the same description.
        for (flavor in KernelFlavor.entries) {
            for (mode in UserAgentMode.entries) {
                for (custom in listOf(null, "leftover-custom-ua")) {
                    val identity = resolve(flavor = flavor, legacyMode = mode, customUserAgent = custom)
                    if (identity.isNoOp) continue

                    val profile = identity.profile
                    assertThat(profile).isNotNull()
                    assertThat(profile!!.userAgent).isEqualTo(identity.userAgent)
                }
            }
        }
    }

    @Test
    fun `every non-default flavor yields a UA and a profile from that same flavor`() {
        for (flavor in KernelFlavor.entries.filter { it != KernelFlavor.SYSTEM_DEFAULT }) {
            val identity = resolve(flavor = flavor)

            assertThat(identity.userAgent).isNotNull()
            assertThat(identity.profile).isNotNull()
            assertThat(identity.profile!!.flavor).isEqualTo(flavor)
            assertThat(identity.profile!!.userAgent).isEqualTo(identity.userAgent)
        }
    }

    @Test
    fun `desktop flavors describe a desktop platform`() {
        assertThat(KernelFlavor.DESKTOP_FLAVORS).isNotEmpty()
        for (flavor in KernelFlavor.DESKTOP_FLAVORS) {
            val profile = flavor.profile

            assertThat(profile.mobile).isFalse()
            assertThat(profile.platform).isAnyOf("Windows", "macOS")
            assertThat(flavor.isDesktop).isTrue()
        }
    }

    @Test
    fun `mobile flavors describe a mobile platform`() {
        val mobileFlavors = listOf(
            KernelFlavor.BLINK_CHROME,
            KernelFlavor.BLINK_EDGE,
            KernelFlavor.BLINK_SAMSUNG,
            KernelFlavor.GECKO_FIREFOX,
            KernelFlavor.WEBKIT_SAFARI
        )
        for (flavor in mobileFlavors) {
            val profile = flavor.profile

            assertThat(profile.mobile).isTrue()
            assertThat(profile.platform).isAnyOf("Android", "iOS")
            assertThat(flavor.isDesktop).isFalse()
        }
    }

    @Test
    fun `legacy UA modes migrate onto the equivalent flavor`() {
        for (mode in UserAgentMode.entries) {
            val identity = resolve(legacyMode = mode)

            when (mode) {
                // These two carry no identity of their own: DEFAULT means "no disguise", and
                // CUSTOM only acts when a string is actually supplied below.
                UserAgentMode.DEFAULT, UserAgentMode.CUSTOM -> assertThat(identity.isNoOp).isTrue()

                else -> {
                    assertThat(mode.toKernelFlavor()).isNotEqualTo(KernelFlavor.SYSTEM_DEFAULT)
                    assertThat(identity.profile!!.flavor).isEqualTo(mode.toKernelFlavor())
                }
            }
        }
    }

    @Test
    fun `an explicitly chosen flavor takes precedence over a legacy mode`() {
        val identity = resolve(flavor = KernelFlavor.BLINK_EDGE, legacyMode = UserAgentMode.SAFARI_MOBILE)

        assertThat(identity.profile!!.flavor).isEqualTo(KernelFlavor.BLINK_EDGE)
        assertThat(identity.userAgent).isEqualTo(KernelFlavor.BLINK_EDGE.profile.userAgent)
    }

    @Test
    fun `a leftover custom UA does not override an explicitly chosen flavor`() {
        // A string left behind in the custom field must not silently win once the user has
        // picked a flavor — that is how the old two-setting arrangement drifted apart.
        val identity = resolve(
            flavor = KernelFlavor.BLINK_CHROME,
            legacyMode = UserAgentMode.DEFAULT,
            customUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )

        assertThat(identity.profile!!.flavor).isEqualTo(KernelFlavor.BLINK_CHROME)
        assertThat(identity.userAgent).isEqualTo(KernelFlavor.BLINK_CHROME.profile.userAgent)
    }

    @Test
    fun `a custom Chromium UA ships client hints derived from that string`() {
        val ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/${UserAgentVersions.CHROME}.0.0.0 Safari/537.36"

        val identity = resolve(legacyMode = UserAgentMode.CUSTOM, customUserAgent = ua)
        val profile = identity.profile!!

        assertThat(identity.userAgent).isEqualTo(ua)
        assertThat(profile.mobile).isFalse()
        assertThat(profile.platform).isEqualTo("Windows")
        assertThat(profile.supportsClientHints).isTrue()
        assertThat(profile.brands.map { it.brand }).contains("Chromium")
        assertThat(profile.fullVersion).isEqualTo("${UserAgentVersions.CHROME}.0.0.0")
    }

    @Test
    fun `a custom Safari UA reports no Chromium brands, matching the real browser`() {
        val ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 " +
            "(KHTML, like Gecko) Version/${UserAgentVersions.SAFARI}.1 Safari/605.1.15"

        val identity = resolve(legacyMode = UserAgentMode.CUSTOM, customUserAgent = ua)
        val profile = identity.profile!!

        assertThat(profile.platform).isEqualTo("macOS")
        assertThat(profile.supportsClientHints).isFalse()
        assertThat(profile.brands).isEmpty()
    }

    @Test
    fun `an unrecognisable custom UA falls back to the engine default, not a mismatched pair`() {
        // Deriv: unknown platform means we cannot describe the UA. Emitting it alone would
        // reintroduce the very contradiction this resolver exists to remove, so skip it.
        val identity = resolve(
            legacyMode = UserAgentMode.CUSTOM,
            customUserAgent = "TotallyUnknownAgent/9.9 (MysteryBox)"
        )

        assertThat(identity.isNoOp).isTrue()
    }

    @Test
    fun `device disguise drives both halves of the identity`() {
        val ua = "Mozilla/5.0 (Linux; Android 15; SM-S931B) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/${UserAgentVersions.CHROME}.0.0.0 Mobile Safari/537.36"

        val identity = resolve(deviceDisguiseUserAgent = ua)
        val profile = identity.profile!!

        assertThat(identity.userAgent).isEqualTo(ua)
        assertThat(profile.userAgent).isEqualTo(ua)
        assertThat(profile.mobile).isTrue()
        assertThat(profile.platform).isEqualTo("Android")
    }

    @Test
    fun `an empty device disguise UA is ignored rather than clearing the identity`() {
        val identity = resolve(flavor = KernelFlavor.GECKO_FIREFOX, deviceDisguiseUserAgent = "")

        assertThat(identity.profile!!.flavor).isEqualTo(KernelFlavor.GECKO_FIREFOX)
    }
}
