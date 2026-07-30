package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WebViewManagerExternalLinkTest {

    @Test
    fun `isLocalAppUrl recognizes file and loopback origins`() {
        assertThat(WebViewManager.isLocalAppUrl("file:///android_asset/index.html")).isTrue()
        assertThat(WebViewManager.isLocalAppUrl("file:///data/user/0/com.x/files/html/index.html")).isTrue()
        assertThat(WebViewManager.isLocalAppUrl("http://127.0.0.1:8080/index.html")).isTrue()
        assertThat(WebViewManager.isLocalAppUrl("http://localhost:8080/index.html")).isTrue()
        assertThat(WebViewManager.isLocalAppUrl("https://example.com/")).isFalse()
        assertThat(WebViewManager.isLocalAppUrl(null)).isFalse()
    }

    @Test
    fun `isLoopbackHost matches loopback hosts only`() {
        assertThat(WebViewManager.isLoopbackHost("127.0.0.1")).isTrue()
        assertThat(WebViewManager.isLoopbackHost("localhost")).isTrue()
        assertThat(WebViewManager.isLoopbackHost("[::1]")).isTrue()
        assertThat(WebViewManager.isLoopbackHost("::1")).isTrue()
        assertThat(WebViewManager.isLoopbackHost("example.com")).isFalse()
    }

    @Test
    fun `isExternalUrl treats same host and subdomains as internal`() {
        assertThat(WebViewManager.isExternalUrl("https://example.com/a", "https://example.com/b")).isFalse()
        assertThat(WebViewManager.isExternalUrl("https://cdn.example.com/x", "https://example.com/")).isFalse()
        assertThat(WebViewManager.isExternalUrl("https://other.com/", "https://example.com/")).isTrue()
    }

    @Test
    fun `isExternalUrl treats local-to-local navigations as internal`() {
        // Regression: a local HTML app served from 127.0.0.1 that navigates to localhost
        // (or between file:// pages) was misclassified as external and hijacked, leaving the
        // WebView on net::ERR_ACCESS_DENIED.
        assertThat(
            WebViewManager.isExternalUrl("http://localhost:8080/home.html", "http://127.0.0.1:8080/index.html")
        ).isFalse()
        assertThat(
            WebViewManager.isExternalUrl("file:///data/x/home.html", "file:///data/x/index.html")
        ).isFalse()
        assertThat(
            WebViewManager.isExternalUrl("http://127.0.0.1:8080/", "file:///data/x/index.html")
        ).isFalse()
    }

    @Test
    fun `isExternalUrl treats remote target from local page as external`() {
        assertThat(WebViewManager.isExternalUrl("https://example.com/", "file:///data/x/index.html")).isTrue()
        assertThat(WebViewManager.isExternalUrl("https://example.com/", "http://127.0.0.1:8080/")).isTrue()
    }

    @Test
    fun `isExternalUrl returns false without a current url`() {
        assertThat(WebViewManager.isExternalUrl("https://example.com/", null)).isFalse()
    }
}
