package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WebViewRestoreGuardTest {

    @Test
    fun `ordinary web pages restore`() {
        for (url in listOf(
            "https://chat.qwen.ai/",
            "https://site.example/deep/page?q=1",
            "http://site.example/",
            "file:///android_asset/site/index.html"
        )) {
            assertThat(WebViewRestoreGuard.isUsableRestoredUrl(url, null)).isTrue()
        }
    }

    @Test
    fun `dead entries are rejected`() {
        for (url in listOf(
            null,
            "",
            "about:blank",
            "weixin://dl/business/?t=abc",
            "intent://scan#Intent;scheme=weixin;end",
            "javascript:void(0)",
            "data:text/html,<p>hi</p>"
        )) {
            assertThat(WebViewRestoreGuard.isUsableRestoredUrl(url, null)).isFalse()
        }
    }

    @Test
    fun `the page that launched an external app is vetoed`() {
        val trampoline = "https://open.weixin.qq.com/connect/oauth2/authorize?x=1"
        assertThat(
            WebViewRestoreGuard.isUsableRestoredUrl(trampoline, trampoline)
        ).isFalse()
        // An unrelated page is unaffected even when a marker exists.
        assertThat(
            WebViewRestoreGuard.isUsableRestoredUrl("https://chat.qwen.ai/", trampoline)
        ).isTrue()
    }
}
