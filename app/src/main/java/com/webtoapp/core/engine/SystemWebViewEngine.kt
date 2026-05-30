package com.webtoapp.core.engine

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.webkit.*
import com.webtoapp.core.adblock.AdBlocker
import com.webtoapp.core.webview.WebViewCallbacks
import com.webtoapp.core.webview.WebViewManager
import com.webtoapp.data.model.WebViewConfig

class SystemWebViewEngine(
    private val context: Context,
    private val adBlocker: AdBlocker
) : BrowserEngine {

    override val engineType = EngineType.SYSTEM_WEBVIEW

    private var webView: WebView? = null
    private val webViewManager = WebViewManager(context, adBlocker)

    override fun createView(
        context: Context,
        config: WebViewConfig,
        callback: BrowserEngineCallback
    ): View {
        val wv = WebView(context)

        val bridgeCallbacks = object : WebViewCallbacks {
            override fun onPageStarted(url: String?) = callback.onPageStarted(url)
            override fun onPageFinished(url: String?) = callback.onPageFinished(url)
            override fun onProgressChanged(progress: Int) = callback.onProgressChanged(progress)
            override fun onTitleChanged(title: String?) = callback.onTitleChanged(title)
            override fun onIconReceived(icon: Bitmap?) = callback.onIconReceived(icon)
            override fun onError(errorCode: Int, description: String) = callback.onError(errorCode, description)
            override fun onSslError(error: String) = callback.onSslError(error)
            override fun onExternalLink(url: String) = callback.onExternalLink(url)

            override fun onShowCustomView(view: View?, cb: WebChromeClient.CustomViewCallback?) {
                callback.onShowCustomView(view, cb)
            }

            override fun onHideCustomView() = callback.onHideCustomView()

            override fun onGeolocationPermission(origin: String?, cb: GeolocationPermissions.Callback?) {

                cb?.invoke(origin, true, false)
            }

            override fun onPermissionRequest(request: PermissionRequest?) {

                request?.grant(request.resources)
            }

            override fun onShowFileChooser(
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: WebChromeClient.FileChooserParams?
            ): Boolean {

                return false
            }

            override fun onDownloadStart(url: String, userAgent: String, contentDisposition: String, mimeType: String, contentLength: Long) {
                callback.onDownloadStart(url, userAgent, contentDisposition, mimeType, contentLength)
            }

            override fun onLongPress(webView: WebView, x: Float, y: Float): Boolean = false

            override fun onConsoleMessage(level: Int, message: String, sourceId: String, lineNumber: Int) {
                callback.onConsoleMessage(level, message, sourceId, lineNumber)
            }

            override fun onNewWindow(resultMsg: android.os.Message?) {
                callback.onNewWindow(resultMsg)
            }
        }

        webViewManager.configureWebView(wv, config, bridgeCallbacks)
        webView = wv
        return wv
    }

    override fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    override fun evaluateJavascript(script: String, resultCallback: ((String?) -> Unit)?) {
        webView?.evaluateJavascript(script) { result ->
            resultCallback?.invoke(result)
        }
    }

    override fun canGoBack(): Boolean = webView?.canGoBack() ?: false
    override fun goBack() { webView?.goBack() }
    override fun canGoForward(): Boolean = webView?.canGoForward() ?: false
    override fun goForward() { webView?.goForward() }
    override fun reload() { webView?.reload() }
    override fun stopLoading() { webView?.stopLoading() }
    override fun getCurrentUrl(): String? = webView?.url
    override fun getTitle(): String? = webView?.title
    override fun getView(): View? = webView

    override fun destroy() {
        webView?.apply {
            stopLoading()
            clearHistory()
            removeAllViews()
            destroy()
        }
        webView = null
    }

    override fun clearCache(includeDiskFiles: Boolean) {
        webView?.clearCache(includeDiskFiles)
    }

    override fun clearHistory() {
        webView?.clearHistory()
    }

    fun getWebViewManager(): WebViewManager = webViewManager

    fun getWebView(): WebView? = webView
}
