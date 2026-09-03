package com.webtoapp.core.extension

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Guards the share-code codec contract (issue #768):
 *
 * - V2 (`WTA2:`) round-trips every customized field while dropping defaults,
 *   so bigger modules fit into a single QR code.
 * - V1 (`WTA1:`) and legacy codes keep decoding (old shares stay importable).
 * - `toShareCode()` prefers V1 while it fits, so older app versions can still
 *   read small shares; V2 is only emitted when V1 would not fit one QR code.
 *
 * Needs Robolectric: the codec uses `android.util.Base64`.
 */
@RunWith(RobolectricTestRunner::class)
class ModuleShareCodeTest {

    private fun richModule() = ExtensionModule(
        name = "Share Codec Probe",
        description = "round-trip sentinel",
        code = "console.log('share-code-probe');".repeat(40),
        cssCode = ".probe{color:red}".repeat(10),
        urlMatches = listOf(
            UrlMatchRule(pattern = "*example.com*"),
            UrlMatchRule(pattern = "https://admin.example.com/*", exclude = true)
        ),
        configItems = listOf(
            ModuleConfigItem(key = "token", name = "Token", defaultValue = "abc")
        ),
        configValues = mapOf("token" to "xyz"),
        enabled = false,
        category = ModuleCategory.CONTENT_FILTER,
        tags = listOf("probe", "share")
    )

    /** Deterministic high-entropy payload: fixed seed, unique tokens per line. */
    private fun bulkyCode(lines: Int): String {
        val random = kotlin.random.Random(42)
        val hex = "0123456789abcdef"
        return (1..lines).joinToString("\n") { i ->
            val token = (1..32).map { hex[random.nextInt(16)] }.joinToString("")
            "const sessionToken$i = \"$token\"; // binding $i"
        }
    }

    @Test
    fun `V2 round-trips every customized field`() {
        val original = richModule()

        val decoded = ExtensionModule.fromShareCode(original.toShareCodeV2())

        assertThat(decoded).isNotNull()
        decoded!!
        assertThat(decoded.name).isEqualTo(original.name)
        assertThat(decoded.description).isEqualTo(original.description)
        assertThat(decoded.code).isEqualTo(original.code)
        assertThat(decoded.cssCode).isEqualTo(original.cssCode)
        assertThat(decoded.urlMatches).isEqualTo(original.urlMatches)
        assertThat(decoded.configItems).isEqualTo(original.configItems)
        assertThat(decoded.configValues).isEqualTo(original.configValues)
        assertThat(decoded.enabled).isFalse()
        assertThat(decoded.category).isEqualTo(original.category)
        assertThat(decoded.tags).isEqualTo(original.tags)
    }

    @Test
    fun `V2 default module keeps enabled true (no primitive-default trap)`() {
        // `enabled` defaults to true but Gson leaves omitted primitives as false;
        // the V2 merge onto defaults must restore it.
        val decoded = ExtensionModule.fromShareCode(ExtensionModule(name = "Plain").toShareCodeV2())

        assertThat(decoded).isNotNull()
        assertThat(decoded!!.enabled).isTrue()
        assertThat(decoded.name).isEqualTo("Plain")
    }

    @Test
    fun `V1 codes from older versions still decode`() {
        // Generated once with stock gzip+Base64 over {"name","code","enabled"}.
        val legacyV1 = "WTA1:H4sIANWEmWoC/6tWykvMTVWyUvJJTU9MrlTwzczLVNJRSs5PAQkm5qQWlWgYagJFUvMSk3JSU5Ss0hJzilNrAb++qF84AAAA"

        val decoded = ExtensionModule.fromShareCode(legacyV1)

        assertThat(decoded).isNotNull()
        assertThat(decoded!!.name).isEqualTo("Legacy Mini")
        assertThat(decoded.code).isEqualTo("alert(1)")
        assertThat(decoded.enabled).isFalse()
    }

    @Test
    fun `V2 is strictly smaller than V1 for a customized module`() {
        val module = richModule()

        assertThat(module.toShareCodeV2().length).isLessThan(module.toShareCodeV1().length)
    }

    @Test
    fun `toShareCode prefers V1 while it fits, V2 once V1 overflows`() {
        val smallCode = ExtensionModule(name = "Tiny", code = "1").toShareCode()
        assertThat(smallCode.startsWith("WTA1:")).isTrue()
        assertThat(ExtensionModule.fromShareCode(smallCode)!!.name).isEqualTo("Tiny")

        val big = richModule().copy(code = bulkyCode(56))
        val bigCode = big.toShareCode()
        assertThat(bigCode.startsWith("WTA2:")).isTrue()
        assertThat(QrCodeUtils.canGenerateQrCode(bigCode)).isTrue()
        assertThat(ExtensionModule.fromShareCode(bigCode)!!.code).isEqualTo(big.code)
    }

    @Test
    fun `QR capacity cap matches the physical single-code ceiling`() {
        assertThat(QrCodeUtils.canGenerateQrCode("x".repeat(2953))).isTrue()
        assertThat(QrCodeUtils.canGenerateQrCode("x".repeat(2954))).isFalse()
    }

    @Test
    fun `garbage share codes fail closed`() {
        assertThat(ExtensionModule.fromShareCode("WTA2:!!!not-base64!!!")).isNull()
        assertThat(ExtensionModule.fromShareCode("")).isNull()
    }
}
