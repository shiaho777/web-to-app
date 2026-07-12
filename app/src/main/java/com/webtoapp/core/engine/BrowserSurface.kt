package com.webtoapp.core.engine

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebBackForwardList
import android.webkit.WebView

class BrowserSurface private constructor(
    val view: View,
    val webView: WebView?,
    private val engine: BrowserEngine?
) {
    val isGecko: Boolean
        get() = engine != null

    val engineType: EngineType
        get() = engine?.engineType ?: EngineType.SYSTEM_WEBVIEW

    fun loadUrl(url: String) {
        engine?.loadUrl(url) ?: webView?.loadUrl(url)
    }

    fun reload() {
        engine?.reload() ?: webView?.reload()
    }

    fun goBack() {
        engine?.goBack() ?: webView?.goBack()
    }

    fun goForward() {
        engine?.goForward() ?: webView?.goForward()
    }

    fun canGoBack(): Boolean = engine?.canGoBack() ?: (webView?.canGoBack() == true)

    fun canGoForward(): Boolean = engine?.canGoForward() ?: (webView?.canGoForward() == true)

    fun stopLoading() {
        engine?.stopLoading() ?: webView?.stopLoading()
    }

    fun getCurrentUrl(): String? = engine?.getCurrentUrl() ?: webView?.url

    fun getTitle(): String? = engine?.getTitle() ?: webView?.title

    fun evaluateJavascript(script: String, resultCallback: ((String?) -> Unit)? = null) {
        if (engine != null) {
            engine.evaluateJavascript(script, resultCallback)
        } else {
            webView?.evaluateJavascript(script) { resultCallback?.invoke(it) }
        }
    }

    fun requestFocus() {
        engine?.requestFocus() ?: webView?.requestFocus()
    }

    fun dispatchKeyEvent(event: KeyEvent): Boolean = view.dispatchKeyEvent(event)

    fun onResume() {
        webView?.onResume()
        webView?.resumeTimers()
        engine?.getView()?.let { }
    }

    fun onPause() {
        webView?.onPause()
    }

    fun resumeTimers() {
        webView?.resumeTimers()
    }

    fun saveState(outState: Bundle): WebBackForwardList? = webView?.saveState(outState)

    fun restoreState(inState: Bundle): WebBackForwardList? = webView?.restoreState(inState)

    fun addJavascriptInterface(obj: Any, name: String) {
        webView?.addJavascriptInterface(obj, name)
    }

    fun destroy() {
        engine?.destroy()
        webView?.let { wv ->
            try {
                wv.stopLoading()
                wv.clearHistory()
                wv.removeAllViews()
                wv.destroy()
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        fun fromWebView(webView: WebView): BrowserSurface =
            BrowserSurface(view = webView, webView = webView, engine = null)

        fun fromEngine(engine: BrowserEngine, view: View): BrowserSurface =
            BrowserSurface(view = view, webView = null, engine = engine)
    }
}
