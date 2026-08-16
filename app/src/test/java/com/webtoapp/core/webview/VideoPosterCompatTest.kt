package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Guards the contract of [VideoPosterCompat.INJECTION_SCRIPT] — the shim must give every
 * poster-less `<video>` a real data-URI poster so WebView never generates its internal
 * `android-webview-video-poster:` pseudo-URL, which site media pipelines cannot load
 * (CORS/CSP failure, issue #563).
 */
class VideoPosterCompatTest {

    private val script = VideoPosterCompat.INJECTION_SCRIPT

    @Test
    fun `script is guarded against re-injection`() {
        val guardIdx = script.indexOf("__wtaVideoPosterShimInstalled")
        assertThat(guardIdx).isGreaterThan(0)
        val observerIdx = script.indexOf("new MutationObserver")
        assertThat(observerIdx).isGreaterThan(guardIdx)
    }

    @Test
    fun `script only patches videos that have no poster attribute`() {
        // Site-provided posters must never be overwritten — the shim is a fallback only.
        val guardIdx = script.indexOf("hasAttribute('poster')")
        val setIdx = script.indexOf("setAttribute('poster'")
        assertThat(guardIdx).isGreaterThan(0)
        assertThat(setIdx).isGreaterThan(guardIdx)
        assertThat(script).doesNotContain("removeAttribute")
    }

    @Test
    fun `poster fallback is a data URI`() {
        // A data-URI poster is what stops WebView from synthesizing its internal
        // android-webview-video-poster: URL; any remote URL would reintroduce a fetch.
        assertThat(script).contains("data:image/gif;base64,")
    }

    @Test
    fun `script rescans dynamically inserted videos`() {
        // SPA players append <video> long after onPageFinished; the observer must
        // scan added subtrees for late videos.
        assertThat(script).contains("new MutationObserver")
        assertThat(script).contains("childList: true, subtree: true")
    }

    @Test
    fun `script is an IIFE`() {
        assertThat(script.trimStart().startsWith("(function()")).isTrue()
    }
}
