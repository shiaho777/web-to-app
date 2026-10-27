package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.kernel.UserAgentProfileDeriver
import org.junit.Test

class CloudflareTurnstileCompatTest {

    @Test
    fun `stripped android webview ua gets chrome client hints`() {
        val raw = "Mozilla/5.0 (Linux; Android 14; Pixel 8; wv) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36"
        val stripped = CloudflareCompat.stripWebViewMarker(raw)
        val profile = UserAgentProfileDeriver.derive(stripped)

        assertThat(stripped).doesNotContain("; wv")
        assertThat(profile).isNotNull()
        assertThat(profile!!.brands.map { it.brand }).contains("Google Chrome")
        assertThat(profile.brands.map { it.brand }).doesNotContain("Android WebView")
        assertThat(profile.mobile).isTrue()
        assertThat(profile.platform).isEqualTo("Android")
    }

    @Test
    fun `compat script does not rewrite function toString or invent chrome runtime`() {
        val js = CloudflareCompat.generateCompatJs()
        assertThat(js).doesNotContain("Function.prototype.toString")
        assertThat(js).doesNotContain("chrome.runtime")
        assertThat(js).contains("challenges.cloudflare.com")
        assertThat(js).contains("webdriver")
    }

    @Test
    fun `challenge frames skip wrapped document start scripts`() {
        val wrapped = CloudflareCompat.exemptChallengeFrames("window.__ran = 1;")
        assertThat(wrapped).contains("window.__ran = 1;")
        assertThat(wrapped).contains("challenges.cloudflare.com")
        assertThat(CloudflareCompat.isChallengeFrameHost("challenges.cloudflare.com")).isTrue()
        assertThat(CloudflareCompat.isChallengeFrameHost("x.challenges.cloudflare.com")).isTrue()
        assertThat(CloudflareCompat.isChallengeFrameHost("example.com")).isFalse()
    }

    @Test
    fun `proxy bypass covers the turnstile hosts`() {
        val rules = CloudflareCompat.challengeProxyBypassRules()
        assertThat(rules).contains("challenges.cloudflare.com")
        assertThat(rules).contains("*.challenges.cloudflare.com")
        assertThat(rules).contains("*.hcaptcha.com")
    }
}
