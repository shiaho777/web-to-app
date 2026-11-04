package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OfflineNavigationRetryTest {

    private val url = "https://yasinullah.github.io/kataarGame/"

    @Test
    fun `offline network errors retry once per url`() {
        val retry = OfflineNavigationRetry()
        assertThat(retry.shouldSilentRetry(url, -2, "net::ERR_NAME_NOT_RESOLVED")).isTrue()
        assertThat(retry.shouldSilentRetry(url, -2, "net::ERR_NAME_NOT_RESOLVED")).isFalse()
    }

    @Test
    fun `every network error code that can hide a cached navigation is covered`() {
        for (code in listOf(-1, -2, -6, -7, -8)) {
            assertThat(OfflineNavigationRetry().shouldSilentRetry(url, code, "unknown")).isTrue()
        }
    }

    @Test
    fun `disconnected descriptions are covered without a matching error code`() {
        assertThat(
            OfflineNavigationRetry().shouldSilentRetry(url, -100, "net::ERR_INTERNET_DISCONNECTED")
        ).isTrue()
        assertThat(
            OfflineNavigationRetry().shouldSilentRetry(url, -100, "net::ERR_ADDRESS_UNREACHABLE")
        ).isTrue()
        assertThat(
            OfflineNavigationRetry().shouldSilentRetry(url, -100, "net::ERR_NETWORK_CHANGED")
        ).isTrue()
    }

    @Test
    fun `ssl redirect and bad url errors are not retried`() {
        for (code in listOf(-9, -10, -11, -12, -15, -16)) {
            assertThat(OfflineNavigationRetry().shouldSilentRetry(url, code, "unknown")).isFalse()
        }
    }

    @Test
    fun `blank local and non http urls are not retried`() {
        val retry = OfflineNavigationRetry()
        assertThat(retry.shouldSilentRetry("", -2, "unknown")).isFalse()
        assertThat(retry.shouldSilentRetry(null, -2, "unknown")).isFalse()
        assertThat(retry.shouldSilentRetry("about:blank", -2, "unknown")).isFalse()
        assertThat(retry.shouldSilentRetry("file:///android_asset/index.html", -2, "unknown")).isFalse()
        assertThat(retry.shouldSilentRetry("data:text/html,hi", -2, "unknown")).isFalse()
    }

    @Test
    fun `distinct urls are capped`() {
        val retry = OfflineNavigationRetry(maxDistinctUrls = 2)
        assertThat(retry.shouldSilentRetry("https://a.example/", -2, "")).isTrue()
        assertThat(retry.shouldSilentRetry("https://b.example/", -2, "")).isTrue()
        assertThat(retry.shouldSilentRetry("https://c.example/", -2, "")).isFalse()
    }

    @Test
    fun `error description matching is case insensitive`() {
        assertThat(
            OfflineNavigationRetry().shouldSilentRetry(url, -100, "net::err_internet_disconnected")
        ).isTrue()
    }
}
