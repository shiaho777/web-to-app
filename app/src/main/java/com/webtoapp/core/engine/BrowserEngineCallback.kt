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
        contentLength: Long
    )

    /**
     * File upload request (`<input type="file">`). Engines that surface uploads through a
     * different mechanism (System WebView uses its WebChromeClient directly) can ignore this;
     * GeckoView routes its PromptDelegate file prompts here. Returns true if the chooser was
     * launched, false if the prompt should be dismissed.
     */
    fun onShowFileChooser(
        filePathCallback: android.webkit.ValueCallback<Array<android.net.Uri>>?,
        fileChooserParams: android.webkit.WebChromeClient.FileChooserParams?
    ): Boolean = false

    fun onConsoleMessage(level: Int, message: String, sourceId: String, lineNumber: Int) {}

    fun onNewWindow(resultMsg: android.os.Message?) {}
}
