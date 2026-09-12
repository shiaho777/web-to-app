package com.webtoapp.core.kernel

import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.UserAgentMode

/**
 * The pair of signals a page can read to decide "which browser is this":
 * the `User-Agent` request header, and the User-Agent Client Hints (`Sec-CH-UA*`) which the
 * engine generates from a [KernelFlavorProfile]'s metadata.
 *
 * These two must always travel together. Emitting a UA string with no matching metadata makes
 * the engine fall back to reporting its own brands in `Sec-CH-UA`, so the page sees a UA that
 * claims one browser while the client hints claim another. That contradiction is exactly what
 * anti-bot systems read as spoofing, and it is invisible in a normal browser where both signals
 * agree by construction.
 */
data class BrowserIdentity(
    val userAgent: String?,
    val profile: KernelFlavorProfile?
) {
    val isNoOp: Boolean
        get() = userAgent.isNullOrBlank() && profile == null

    companion object {
        /** No disguise: the engine's own UA and its own client hints, which agree by default. */
        val SYSTEM_DEFAULT = BrowserIdentity(userAgent = null, profile = null)
    }
}

/**
 * Resolves the one browser identity an app should present, from every disguise setting that
 * used to feed the UA pipeline independently.
 *
 * Precedence, highest first:
 *  1. an explicit custom UA string (mode `CUSTOM`, or the device-disguise custom UA)
 *  2. the device-disguise generated UA
 *  3. the selected [KernelFlavor], falling back to the legacy [UserAgentMode] for apps saved
 *     before kernel flavor became the single selector
 *  4. desktop mode
 *  5. the legacy plain `userAgent` string, for apps persisted before UA modes existed
 *  6. system default
 *
 * Every branch returns a UA **and** the metadata that describes it, so callers can never apply
 * one without the other.
 */
object BrowserIdentityResolver {

    private const val TAG = "BrowserIdentityResolver"

    fun resolve(
        flavor: KernelFlavor,
        legacyMode: UserAgentMode,
        customUserAgent: String?,
        desktopMode: Boolean,
        desktopUserAgent: String?,
        legacyUserAgent: String?,
        deviceDisguiseUserAgent: String?
    ): BrowserIdentity {
        // 1. An explicit custom string wins, but only while the custom option is actually
        //    selected — a leftover string from an earlier edit must not silently override the
        //    identity the user has since chosen.
        if (legacyMode == UserAgentMode.CUSTOM) {
            describe(customUserAgent)?.let { return it }
        }

        // 2. Device disguise already produced a concrete UA (possibly its own custom one).
        describe(deviceDisguiseUserAgent)?.let { return it }

        // 3. Kernel flavor is the single identity selector. Legacy mode values are migrated so
        //    apps saved before this change keep the disguise they had.
        val effectiveFlavor = if (flavor != KernelFlavor.SYSTEM_DEFAULT) {
            flavor
        } else {
            legacyMode.toKernelFlavor()
        }
        if (effectiveFlavor != KernelFlavor.SYSTEM_DEFAULT) {
            val profile = effectiveFlavor.profile
            profile.userAgent?.takeIf { it.isNotBlank() }?.let {
                return BrowserIdentity(it, profile)
            }
        }

        // 4. Desktop mode is a viewport policy that also changes the UA; keep it consistent too.
        if (desktopMode) {
            describe(desktopUserAgent)?.let { return it }
        }

        // 5. Apps persisted before UA modes existed carried a plain UA string on the config.
        describe(legacyUserAgent)?.let { return it }

        // 6. Nothing selected: leave the engine's own consistent pair untouched.
        return BrowserIdentity.SYSTEM_DEFAULT
    }

    /**
     * Pairs a UA string with client hints derived from that string.
     *
     * Returns null when the platform cannot be identified. In that case the caller skips this
     * source entirely rather than emitting the UA on its own: a UA sent without matching client
     * hints is precisely the contradiction this resolver exists to prevent, so leaving the
     * engine's own consistent pair in place is the lesser evil.
     */
    private fun describe(userAgent: String?): BrowserIdentity? {
        val ua = userAgent?.takeIf { it.isNotBlank() } ?: return null
        val profile = UserAgentProfileDeriver.derive(ua)
        if (profile == null) {
            AppLogger.w(
                TAG,
                "Unrecognised platform in a custom User-Agent; keeping the engine default rather " +
                    "than sending a UA whose client hints would contradict it: ${ua.take(80)}"
            )
            return null
        }
        return BrowserIdentity(ua, profile)
    }
}

/**
 * Builds client-hint metadata out of an arbitrary User-Agent string.
 *
 * Needed for UA strings we did not author from a [KernelFlavorProfile] — a user-typed custom UA,
 * or one generated by the device-disguise feature — so those still ship client hints that agree
 * with the UA instead of the engine's real ones.
 *
 * Returns null when the platform cannot be identified: faking `Sec-CH-UA` for an unknown UA
 * would be a guess, and a wrong guess is the very inconsistency this exists to prevent.
 */
internal object UserAgentProfileDeriver {

    private val CHROME_VERSION = Regex("""Chrome/(\d+)""")
    private val EDGE_VERSION = Regex("""Edg(?:A|ios)?/(\d+)""")
    private val FIREFOX_VERSION = Regex("""Firefox/(\d+)""")
    private val SAFARI_VERSION = Regex("""Version/(\d+)""")

    private val WINDOWS = Regex("""Windows NT (\d+)\.(\d+)""")
    private val MACOS = Regex("""Mac OS X (\d+)[_.](\d+)""")
    private val IOS = Regex("""(?:iPhone|iPad); CPU (?:iPhone )?OS (\d+)[_.](\d+)""")
    private val ANDROID = Regex("""Android (\d+)""")

    fun derive(userAgent: String): KernelFlavorProfile? {
        val platform = platformOf(userAgent) ?: return null
        val chromeMajor = CHROME_VERSION.find(userAgent)?.groupValues?.get(1)
        val isChromium = chromeMajor != null
        val isEdge = EDGE_VERSION.containsMatchIn(userAgent)

        val brands = when {
            isEdge && chromeMajor != null -> listOf(
                KernelBrand("Chromium", chromeMajor, "$chromeMajor.0.0.0"),
                KernelBrand("Microsoft Edge", chromeMajor, "$chromeMajor.0.0.0"),
                KernelBrand("Not_A Brand", "24", "24.0.0.0")
            )
            isChromium && chromeMajor != null -> listOf(
                KernelBrand("Chromium", chromeMajor, "$chromeMajor.0.0.0"),
                KernelBrand("Google Chrome", chromeMajor, "$chromeMajor.0.0.0"),
                KernelBrand("Not_A Brand", "24", "24.0.0.0")
            )
            // Firefox and Safari do not ship UA client hints; an empty brand list is what makes
            // `Sec-CH-UA` absent for them, which is what those browsers really do.
            else -> emptyList()
        }

        return KernelFlavorProfile(
            flavor = KernelFlavor.SYSTEM_DEFAULT,
            userAgent = userAgent,
            vendor = when {
                isChromium -> "Google Inc."
                userAgent.contains("Safari") -> "Apple Computer, Inc."
                else -> ""
            },
            hasWindowChrome = isChromium,
            supportsClientHints = isChromium,
            brands = brands,
            mobile = userAgent.contains("Mobile"),
            platform = platform.first,
            platformVersion = platform.second,
            fullVersion = if (isChromium && chromeMajor != null) "$chromeMajor.0.0.0" else "",
            architecture = "",
            bitness = "64",
            model = ""
        )
    }

    /** Returns the UA-CH platform name and major version for the UA, or null when unknown. */
    private fun platformOf(userAgent: String): Pair<String, String>? {
        ANDROID.find(userAgent)?.let {
            return "Android" to "${it.groupValues[1]}.0.0"
        }
        IOS.find(userAgent)?.let {
            return "iOS" to "${it.groupValues[1]}.${it.groupValues[2]}.0"
        }
        WINDOWS.find(userAgent)?.let {
            return "Windows" to "${it.groupValues[1]}.${it.groupValues[2]}.0"
        }
        MACOS.find(userAgent)?.let {
            return "macOS" to "${it.groupValues[1]}.${it.groupValues[2]}.0"
        }
        if (userAgent.contains("Linux")) return "Linux" to ""
        return null
    }
}
