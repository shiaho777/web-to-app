package com.webtoapp.core.extension

import android.webkit.WebView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.webtoapp.core.logging.AppLogger

/**
 * Frame-origin channel for privileged extension calls.
 *
 * [android.webkit.JavascriptInterface] methods cannot see the calling frame, so cookie,
 * fetch, and the other content-bridge operations that act on a URL enter here.
 * WebView sets [android.net.Uri] sourceOrigin; page script cannot choose it.
 */
object ChromeExtensionAuthChannel {
    const val NAME = "WtaExtAuth"
    private const val TAG = "ChromeExtAuth"

    fun install(webView: WebView, listener: WebViewCompat.WebMessageListener) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            AppLogger.w(TAG, "WebMessageListener unavailable; privileged content-bridge calls stay closed")
            return
        }
        runCatching { WebViewCompat.removeWebMessageListener(webView, NAME) }
        WebViewCompat.addWebMessageListener(webView, NAME, setOf("*"), listener)
    }

    fun remove(webView: WebView) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) return
        runCatching { WebViewCompat.removeWebMessageListener(webView, NAME) }
    }
}
