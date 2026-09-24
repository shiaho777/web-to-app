package com.webtoapp.core.webview

import android.content.Context
import android.os.Looper
import android.webkit.ValueCallback
import android.webkit.WebView
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * A WebView whose renderer died (or whose view was destroyed) can throw on
 * *any* access — including the [WebView.getUrl] read the probe performs before
 * pinging. Every throw inside the guarded section must be classified as dead,
 * never escape [RendererLivenessProbe.probe].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RendererLivenessProbeTest {

    @Test
    fun `throwing url access is treated as dead`() {
        val wv = FakeWebView().apply { throwOnUrl = true }
        val dead = AtomicInteger()

        RendererLivenessProbe.probe(wv, stillCurrent = { true }) { dead.incrementAndGet() }

        assertThat(dead.get()).isEqualTo(1)
    }

    @Test
    fun `throwing url access does not fire onDead when view is stale`() {
        val wv = FakeWebView().apply { throwOnUrl = true }
        val dead = AtomicInteger()

        RendererLivenessProbe.probe(wv, stillCurrent = { false }) { dead.incrementAndGet() }

        assertThat(dead.get()).isEqualTo(0)
    }

    @Test
    fun `blank and about-blank views return early without probing`() {
        for (url in listOf(null, "", "about:blank")) {
            val wv = FakeWebView().apply { stubUrl = url }
            val dead = AtomicInteger()

            RendererLivenessProbe.probe(wv, stillCurrent = { true }) { dead.incrementAndGet() }

            assertThat(wv.evalCalls.get()).isEqualTo(0)
            assertThat(dead.get()).isEqualTo(0)
        }
    }

    @Test
    fun `unresponsive renderer fires onDead after the timeout`() {
        // evaluateJavascript records the call but never answers — the classic
        // OEM dead-renderer symptom the probe exists for.
        val wv = FakeWebView().apply { stubUrl = "https://example.com"; autoAnswer = false }
        val dead = AtomicInteger()

        RendererLivenessProbe.probe(wv, stillCurrent = { true }) { dead.incrementAndGet() }
        shadowOf(Looper.getMainLooper()).idleFor(
            RendererLivenessProbe.DEFAULT_TIMEOUT_MS + 100,
            TimeUnit.MILLISECONDS
        )

        assertThat(wv.evalCalls.get()).isEqualTo(1)
        assertThat(dead.get()).isEqualTo(1)
    }

    @Test
    fun `responsive renderer never fires onDead`() {
        val wv = FakeWebView().apply { stubUrl = "https://example.com"; autoAnswer = true }
        val dead = AtomicInteger()

        RendererLivenessProbe.probe(wv, stillCurrent = { true }) { dead.incrementAndGet() }
        shadowOf(Looper.getMainLooper()).idleFor(
            RendererLivenessProbe.DEFAULT_TIMEOUT_MS + 100,
            TimeUnit.MILLISECONDS
        )

        assertThat(wv.evalCalls.get()).isEqualTo(1)
        assertThat(dead.get()).isEqualTo(0)
    }

    @Test
    fun `timeout does not fire onDead when view was already replaced`() {
        val wv = FakeWebView().apply { stubUrl = "https://example.com"; autoAnswer = false }
        val dead = AtomicInteger()

        RendererLivenessProbe.probe(wv, stillCurrent = { false }) { dead.incrementAndGet() }
        shadowOf(Looper.getMainLooper()).idleFor(
            RendererLivenessProbe.DEFAULT_TIMEOUT_MS + 100,
            TimeUnit.MILLISECONDS
        )

        assertThat(dead.get()).isEqualTo(0)
    }

    private class FakeWebView(context: Context = RuntimeEnvironment.getApplication()) :
        WebView(context) {

        var stubUrl: String? = "https://example.com"
        var throwOnUrl = false
        var autoAnswer = true
        var throwOnEval = false
        val evalCalls = AtomicInteger()

        override fun getUrl(): String? {
            if (throwOnUrl) throw IllegalStateException("view destroyed")
            return stubUrl
        }

        override fun evaluateJavascript(script: String, resultCallback: ValueCallback<String>?) {
            evalCalls.incrementAndGet()
            if (throwOnEval) throw IllegalStateException("view destroyed")
            if (autoAnswer) resultCallback?.onReceiveValue("undefined")
        }
    }
}
