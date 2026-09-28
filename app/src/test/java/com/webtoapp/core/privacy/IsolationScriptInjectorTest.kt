package com.webtoapp.core.privacy

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IsolationScriptInjectorTest {

    private val fingerprint = FingerprintGenerator.generateFingerprint(seed = "injector-seed")

    @Test
    fun `returns empty script when isolation disabled`() {
        val script = IsolationScriptInjector.generateIsolationScript(
            config = IsolationConfig(enabled = false),
            fingerprint = fingerprint
        )

        assertThat(script).isEmpty()
    }

    @Test
    fun `default enabled config contains major protection blocks`() {
        val script = IsolationScriptInjector.generateIsolationScript(
            config = IsolationConfig(enabled = true),
            fingerprint = fingerprint
        )

        assertThat(script).contains("Navigator property spoofing")
        assertThat(script).contains("Canvas fingerprint")
        assertThat(script).contains("WebGL")
        assertThat(script).contains("AudioContext")
        assertThat(script).contains("WebRTC")
    }

    @Test
    fun `script respects optional spoof screen and timezone values`() {
        val config = IsolationConfig(
            enabled = true,
            protectCanvas = false,
            protectWebGL = false,
            protectAudio = false,
            blockWebRTC = false,
            protectFonts = true,
            spoofScreen = true,
            customScreenWidth = 1111,
            customScreenHeight = 777,
            customDevicePixelRatio = 2.5f,
            spoofTimezone = true,
            customTimezone = "Europe/Paris"
        )

        val script = IsolationScriptInjector.generateIsolationScript(config, fingerprint)

        assertThat(script).contains("Font fingerprint protection")
        assertThat(script).contains("Screen/window dimension spoofing")
        assertThat(script).contains("__wta_scrW__ = 1111")
        assertThat(script).contains("__wta_scrH__ = 777")
        assertThat(script).contains("__wta_dpr__ = 2.5")
        assertThat(script).contains("Europe/Paris")
    }

    @Test
    fun `script omits navigator block when fingerprint randomization is disabled`() {
        val config = IsolationConfig(
            enabled = true,
            fingerprintConfig = FingerprintConfig(randomize = false),
            protectCanvas = false,
            protectWebGL = false,
            protectAudio = false,
            blockWebRTC = false,
            protectFonts = false,
            spoofScreen = false,
            spoofTimezone = false
        )

        val script = IsolationScriptInjector.generateIsolationScript(config, fingerprint)

        assertThat(script).doesNotContain("Navigator property spoofing")
        assertThat(script).doesNotContain("Canvas fingerprint")
        assertThat(script).doesNotContain("WebGL")
        assertThat(script).doesNotContain("AudioContext")
        assertThat(script).doesNotContain("WebRTC")
    }

    @Test
    fun `custom language drives navigator and Intl locale spoofing`() {
        val config = IsolationConfig(
            enabled = true,
            spoofLanguage = true,
            customLanguage = "de-DE"
        )

        val script = IsolationScriptInjector.generateIsolationScript(config, fingerprint)

        assertThat(script).contains("language: 'de-DE'")
        assertThat(script).contains("languages: Object.freeze([\"de-DE\",\"de\"])".replace("\"", "'"))
        assertThat(script).contains("__wta_locale__ = 'de-DE'")
        assertThat(script).contains("Intl.NumberFormat")
        assertThat(script).contains("Intl.DateTimeFormat")
    }

    @Test
    fun `language spoof without fingerprint randomize still patches navigator language`() {
        val config = IsolationConfig(
            enabled = true,
            fingerprintConfig = FingerprintConfig(randomize = false),
            spoofLanguage = true,
            customLanguage = "fr-FR"
        )

        val script = IsolationScriptInjector.generateIsolationScript(config, fingerprint)

        assertThat(script).contains("Language spoofing (standalone navigator override)")
        assertThat(script).contains("'fr-FR'")
    }

    @Test
    fun `timezone spoof patches Intl and Date wall-clock getters`() {
        val config = IsolationConfig(
            enabled = true,
            spoofTimezone = true,
            customTimezone = "America/New_York"
        )

        val script = IsolationScriptInjector.generateIsolationScript(config, fingerprint)

        assertThat(script).contains("Environment spoofing")
        assertThat(script).contains("__wta_tz__ = 'America/New_York'")
        assertThat(script).contains("formatToParts")           // 动态偏移（含 DST）
        assertThat(script).contains("getTimezoneOffset")
        assertThat(script).contains("Date.prototype.getHours") // 本地时间 getter
        assertThat(script).contains("Date.prototype.toString")
        assertThat(script).contains("window.Date = WtaDate")   // 构造参数按目标时区解释
    }

    @Test
    fun `screen spoof covers inner size dpr orientation and matchMedia`() {
        val config = IsolationConfig(
            enabled = true,
            spoofScreen = true
        )

        val script = IsolationScriptInjector.generateIsolationScript(config, fingerprint)

        assertThat(script).contains("innerWidth")
        assertThat(script).contains("innerHeight")
        assertThat(script).contains("devicePixelRatio")
        assertThat(script).contains("visualViewport")
        assertThat(script).contains("window.matchMedia")
        assertThat(script).contains("orientation")
        assertThat(script).contains("availLeft")
    }

    @Test
    fun `spoof values fall back to fingerprint when unset`() {
        val config = IsolationConfig(
            enabled = true,
            spoofLanguage = true,
            spoofTimezone = true,
            spoofScreen = true
        )

        val script = IsolationScriptInjector.generateIsolationScript(config, fingerprint)

        val fpLang = fingerprint.language.split(",").first().split(";").first().trim()
        assertThat(script).contains("__wta_locale__ = '$fpLang'")
        assertThat(script).contains("__wta_tz__ = '${fingerprint.timezone}'")
        assertThat(script).contains("__wta_scrW__ = ${fingerprint.screenWidth}")
    }
}
