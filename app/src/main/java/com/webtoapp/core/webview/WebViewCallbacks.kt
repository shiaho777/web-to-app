package com.webtoapp.core.webview

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.*

interface WebViewCallbacks {
    fun onPageStarted(url: String?)
    fun onPageCommitVisible(url: String?) {}
    fun onPageFinished(url: String?)
    fun onProgressChanged(progress: Int)
    fun onTitleChanged(title: String?)
    fun onIconReceived(icon: Bitmap?)
    fun onError(errorCode: Int, description: String)
    fun onSslError(error: String)
    fun onExternalLink(url: String)
    fun onShowCustomView(view: android.view.View?, callback: WebChromeClient.CustomViewCallback?)
    fun onHideCustomView()
    fun onGeolocationPermission(origin: String?, callback: GeolocationPermissions.Callback?)
    fun onPermissionRequest(request: PermissionRequest?)

    fun onPermissionRequestCanceled(request: PermissionRequest?) {}

    fun onScrollChanged(webView: android.webkit.WebView?, scrollY: Int) {}
    fun onShowFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?
    ): Boolean

    fun onDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long,
        referrer: String? = null
    )

    fun onExternalDownloadResponse(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long,
        body: java.io.InputStream
    ) {
        onDownloadStart(url, userAgent, contentDisposition, mimeType, contentLength)
    }

    fun onLongPress(webView: WebView, x: Float, y: Float): Boolean = false

    fun onConsoleMessage(level: Int, message: String, sourceId: String, lineNumber: Int) {}

    fun onUrlChanged(webView: WebView?, url: String?) {}

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {}

    fun onNewWindow(resultMsg: android.os.Message?): Boolean = false

    fun onRenderProcessGone(didCrash: Boolean) {}

    fun onExternalAppLaunch(url: String, sourceUrl: String?) {}

    fun onAndroidPermissionsRequest(permissions: Array<String>, onResult: (Boolean) -> Unit) {
        onResult(true)
    }

    fun requestGeolocationAccess(onResult: (Boolean) -> Unit) {
        onResult(false)
    }
}
