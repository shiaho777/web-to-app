package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MainFrameHttpRetryTest {

    @Test
    fun `first 403 of a url retries once and the second does not`() {
        val retry = MainFrameHttpRetry()
        val url = "http://jhgj.bbroot.com/play/1"
        assertThat(retry.shouldSilentRetry(url, 403)).isTrue()
        assertThat(retry.shouldSilentRetry(url, 403)).isFalse()
    }

    @Test
    fun `non 403 blank and non http urls are not retried`() {
        val retry = MainFrameHttpRetry()
        assertThat(retry.shouldSilentRetry("http://example.com/a", 404)).isFalse()
        assertThat(retry.shouldSilentRetry("http://example.com/a", 500)).isFalse()
        assertThat(retry.shouldSilentRetry("http://example.com/a", 401)).isFalse()
        assertThat(retry.shouldSilentRetry("", 403)).isFalse()
        assertThat(retry.shouldSilentRetry(null, 403)).isFalse()
        assertThat(retry.shouldSilentRetry("about:blank", 403)).isFalse()
        assertThat(retry.shouldSilentRetry("file:///android_asset/error.html", 403)).isFalse()
        assertThat(retry.shouldSilentRetry("data:text/html,hi", 403)).isFalse()
    }

    @Test
    fun `each distinct url gets one silent retry until the cap`() {
        val retry = MainFrameHttpRetry(maxDistinctUrls = 2)
        assertThat(retry.shouldSilentRetry("http://example.com/a", 403)).isTrue()
        assertThat(retry.shouldSilentRetry("http://example.com/b", 403)).isTrue()
        assertThat(retry.shouldSilentRetry("http://example.com/c", 403)).isFalse()
        assertThat(retry.shouldSilentRetry("http://example.com/a", 403)).isFalse()
    }

    @Test
    fun `retry headers keep an http referer from the previous page`() {
        assertThat(
            mainFrameHttpRetryHeaders(
                "http://cdn.example/v.mp4",
                "http://jhgj.bbroot.com/"
            )
        ).containsEntry("Referer", "http://jhgj.bbroot.com/")
        assertThat(mainFrameHttpRetryHeaders("http://cdn.example/v.mp4", "http://cdn.example/v.mp4")).isEmpty()
        assertThat(mainFrameHttpRetryHeaders("http://cdn.example/v.mp4", null)).isEmpty()
        assertThat(mainFrameHttpRetryHeaders("http://cdn.example/v.mp4", "about:blank")).isEmpty()
        assertThat(mainFrameHttpRetryHeaders("http://cdn.example/v.mp4", "file:///")).isEmpty()
    }
}
