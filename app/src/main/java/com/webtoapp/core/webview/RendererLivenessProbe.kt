package com.webtoapp.core.webview

import android.os.Handler
import android.os.Looper
import android.webkit.WebView

/**
 * Renderer liveness probe for the cases where `WebViewClient.onRenderProcessGone`
 * never gets delivered.
 *
 * The callback is the primary recovery signal, but it is only best-effort: when
 * the system kills the renderer while the app sits in the background (a camera
 * round-trip, a WeChat OAuth hop), some OEM WebView builds drop the callback.
 * The dead WebView then stays attached — the page renders white, `reload()` is
 * a no-op on the dead channel, and only killing the app recovers (#1030).
 *
 * A tiny `evaluateJavascript` ping settles this: a live renderer answers almost
 * immediately, a dead one never calls back. Callers should invoke [probe] when
 * the activity returns to the foreground and only act while [stillCurrent]
 * reports that the probed WebView is still the live one, so a recreation that
 * already happened via the real callback does not get triggered twice.
 */
object RendererLivenessProbe {

    const val DEFAULT_TIMEOUT_MS = 2000L

    fun probe(
        webView: WebView?,
        stillCurrent: () -> Boolean,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        handler: Handler = Handler(Looper.getMainLooper()),
        onDead: () -> Unit
    ) {
        val wv = webView ?: return
        val url = wv.url
        // Nothing was ever loaded into this view — nothing to rescue.
        if (url.isNullOrBlank() || url == "about:blank") return

        var answered = false
        try {
            wv.evaluateJavascript("void 0") { answered = true }
        } catch (e: Exception) {
            // A destroyed view throws instead of answering — that is dead enough.
            if (stillCurrent()) onDead()
            return
        }

        handler.postDelayed({
            if (!answered && stillCurrent()) onDead()
        }, timeoutMs)
    }
}
