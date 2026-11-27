package com.webtoapp.core.engine

import android.graphics.Bitmap
import android.view.View

interface BrowserEngineCallback {

    fun onPageStarted(url: String?)

    fun onPageFinished(url: String?)

    fun onProgressChanged(progress: Int)

    fun onTitleChanged(title: String?)

    fun onIconReceived(icon: Bitmap?)

    fun onError(errorCode: Int, description: String)

    fun onSslError(error: String)

    fun onExternalLink(url: String)

    fun onShowCustomView(view: View?, callback: Any?)

    fun onHideCustomView()

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

    fun onShowFileChooser(
        filePathCallback: android.webkit.ValueCallback<Array<android.net.Uri>>?,
        fileChooserParams: android.webkit.WebChromeClient.FileChooserParams?
    ): Boolean = false

    fun onConsoleMessage(level: Int, message: String, sourceId: String, lineNumber: Int) {}

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {}

    fun onNewWindow(resultMsg: android.os.Message?): Boolean = false

    fun onAndroidPermissionsRequest(permissions: Array<String>, onResult: (Boolean) -> Unit) {
        onResult(true)
    }
}
