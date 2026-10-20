package com.webtoapp.core.webview

import android.webkit.WebView
import com.webtoapp.core.engine.BrowserSurface
import com.webtoapp.core.engine.GeckoMediaSessionAdapter

/**
 * Ties [BackgroundMediaKeepAlive] to the live WebView or Gecko session.
 *
 * Call [onFocusLost] before the page can observe the leave, [onHostPause]
 * from the activity, and [onHostResume] when it returns. [bind] watches the
 * media session so a pause from the notification drops the keep-alive.
 */
class BackgroundMediaRuntime(
    private val webView: () -> WebView?,
    private val surface: () -> BrowserSurface?,
    private val dispatchHidden: () -> Unit = {},
) {
    private val keepAlive = BackgroundMediaKeepAlive()
    private var bridge: MediaSessionBridge? = null
    private var gecko: GeckoMediaSessionAdapter? = null
    private var applying = false
    private var pageHeld = false

    var enabled: Boolean
        get() = keepAlive.enabled
        set(value) { keepAlive.enabled = value }

    val isPlaying: Boolean get() = keepAlive.isPlaying

    fun bind(next: MediaSessionBridge?) {
        bridge = next
        next?.setPlaybackActiveListener(::onPlaying)
    }

    fun bind(next: GeckoMediaSessionAdapter?) {
        gecko = next
        next?.setPlaybackActiveListener(::onPlaying)
    }

    fun onFocusLost(allow: Boolean) {
        if (allow && keepAlive.enabled && keepAlive.isPlaying) {
            holdPage()
        }
    }

    fun onFocusGained() {
        if (!keepAlive.isHostPaused) releasePageHold()
    }

    /** @return true when the activity must not pause the engine. */
    fun onHostPause(allow: Boolean): Boolean {
        if (!allow || !keepAlive.enabled) {
            releasePageHold()
            keepAlive.onHostResume()
            return false
        }
        val keep = keepAlive.onHostPause()
        if (keep) holdPage() else releasePageHold()
        return keep
    }

    fun onHostResume() {
        keepAlive.onHostResume()
        releasePageHold()
    }

    /** Drop the listener before the media session is released. */
    fun close() {
        applying = true
        bridge?.setPlaybackActiveListener(null)
        gecko?.setPlaybackActiveListener(null)
        bridge = null
        gecko = null
        keepAlive.onHostResume()
        releasePageHold()
        applying = false
    }

    private fun onPlaying(playing: Boolean) {
        if (applying) {
            keepAlive.notePlaying(playing)
            return
        }
        when (keepAlive.onPlayingChanged(playing)) {
            BackgroundMediaKeepAlive.Action.ARM -> {
                applying = true
                try {
                    holdPage()
                    resumeEngine()
                } finally {
                    applying = false
                }
            }
            BackgroundMediaKeepAlive.Action.RELEASE -> {
                applying = true
                try {
                    releasePageHold()
                    dispatchHidden()
                    pauseEngine()
                } finally {
                    applying = false
                }
            }
            BackgroundMediaKeepAlive.Action.NONE -> Unit
        }
    }

    private fun holdPage() {
        val already = pageHeld
        pageHeld = true
        eachWebView { view ->
            view.ensureBackgroundInterface()
            view.keepPlayingInBackground = true
            view.backgroundHold.held = true
        }
        if (!already) {
            evaluate(BackgroundMediaScript.HOLD_ON + BackgroundMediaScript.RESUME)
        }
    }

    private fun releasePageHold() {
        if (!pageHeld) return
        pageHeld = false
        eachWebView { view ->
            view.keepPlayingInBackground = false
            view.backgroundHold.held = false
        }
        evaluate(BackgroundMediaScript.HOLD_OFF)
    }

    private fun resumeEngine() {
        surface()?.onResume() ?: webView()?.let { view ->
            view.onResume()
            view.resumeTimers()
        }
    }

    private fun pauseEngine() {
        surface()?.onPause() ?: webView()?.onPause()
    }

    private fun evaluate(script: String) {
        val current = surface()
        if (current != null) {
            current.evaluateJavascript(script, null)
        } else {
            webView()?.evaluateJavascript(script, null)
        }
    }

    private fun eachWebView(block: (WtaWebView) -> Unit) {
        (webView() as? WtaWebView)?.let(block)
        (surface()?.webView as? WtaWebView)?.let { view ->
            if (view !== webView()) block(view)
        }
    }
}
