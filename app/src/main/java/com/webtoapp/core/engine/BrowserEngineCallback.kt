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

    /**
     * History navigation state changed. GeckoView reports canGoBack/canGoForward through its
     * NavigationDelegate; the System WebView path derives them from `onUrlChanged` instead, so
     * this stays a no-op there. Hosts use it to enable/disable back & forward affordances —
     * without it the Gecko toolbar buttons never leave their initial disabled state.
     */
    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {}

    /**
     * A popup window was requested. Return true when the host renders the popup transport;
     * false lets the WebViewManager fall back to loading the popup URL in the same window.
     */
    fun onNewWindow(resultMsg: android.os.Message?): Boolean = false

    /**
     * Request Android runtime permissions on behalf of the engine. GeckoView surfaces these via
     * `PermissionDelegate.onAndroidPermissionsRequest` (e.g. ACCESS_*_LOCATION for geolocation,
     * CAMERA/RECORD_AUDIO for media). The engine must NOT auto-grant: the host shows the system
     * permission dialog and reports whether every permission was granted through [onResult].
     * Default grants so non-shell hosts do not deadlock the engine.
     */
    fun onAndroidPermissionsRequest(permissions: Array<String>, onResult: (Boolean) -> Unit) {
        onResult(true)
    }
}
