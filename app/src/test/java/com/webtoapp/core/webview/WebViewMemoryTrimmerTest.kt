package com.webtoapp.core.webview

import android.content.ComponentCallbacks2
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Pure level→action mapping for [WebViewMemoryTrimmer] — the tier boundaries
 * are the contract, so pin them: only COMPLETE may tear the WebView down.
 */
class WebViewMemoryTrimmerTest {

    @Test
    fun `complete trims to teardown`() {
        assertThat(
            WebViewMemoryTrimmer.actionFor(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
        ).isEqualTo(WebViewMemoryTrimmer.TrimAction.TEARDOWN)
        assertThat(
            WebViewMemoryTrimmer.shouldTeardownWebView(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
        ).isTrue()
    }

    @Test
    fun `background lru levels trim heavy but keep the webview`() {
        for (level in intArrayOf(
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE
        )) {
            assertThat(WebViewMemoryTrimmer.actionFor(level))
                .isEqualTo(WebViewMemoryTrimmer.TrimAction.HEAVY)
            assertThat(WebViewMemoryTrimmer.shouldTeardownWebView(level)).isFalse()
        }
    }

    @Test
    fun `ui hidden trims heavy but keeps the webview`() {
        assertThat(
            WebViewMemoryTrimmer.actionFor(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
        ).isEqualTo(WebViewMemoryTrimmer.TrimAction.HEAVY)
    }

    @Test
    fun `foreground levels never tear down the visible webview`() {
        for (level in intArrayOf(
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
        )) {
            assertThat(WebViewMemoryTrimmer.shouldTeardownWebView(level)).isFalse()
        }
        assertThat(
            WebViewMemoryTrimmer.actionFor(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        ).isEqualTo(WebViewMemoryTrimmer.TrimAction.LIGHT)
        assertThat(
            WebViewMemoryTrimmer.actionFor(ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE)
        ).isEqualTo(WebViewMemoryTrimmer.TrimAction.NONE)
    }
}
