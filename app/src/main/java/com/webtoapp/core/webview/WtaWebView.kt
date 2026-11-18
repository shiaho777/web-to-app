package com.webtoapp.core.webview

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.WebView

/**
 * WebView that keeps hardware-keyboard modifiers intact (#1032).
 *
 * A `enterkeyhint="send"` (chat inputs, e.g. chat.qwen.ai) lands in
 * `EditorInfo.imeOptions` as IME_ACTION_SEND. The IME stage runs before the
 * activity's dispatchKeyEvent, and some IMEs translate a hardware Enter into
 * `performEditorAction`/`sendDefaultEditorAction` there — which synthesizes a
 * modifier-less Enter keydown, so Shift+Enter arrives at the page as a bare
 * "send". With a physical keyboard attached the action label is meaningless
 * anyway, so the action is stripped and the IME falls back to forwarding the
 * real key event with its meta state intact.
 */
class WtaWebView(context: Context) : WebView(context) {

    /**
     * Multi-web site this view belongs to, stamped by SiteContent so the
     * activity can save/restore WebView state against the correct site's
     * surface instead of blindly applying it to the first-created one (#1036).
     */
    var siteId: String? = null

    /**
     * While true, a backgrounded window must not be reported as GONE.
     * Chromium pauses HTML media when it sees that, which is what
     * [BackgroundMediaRuntime] is keeping alive.
     */
    var keepPlayingInBackground: Boolean = false

    /** Synchronous flag the page script reads via [BackgroundMediaScript]. */
    val backgroundHold: BackgroundMediaHold = BackgroundMediaHold()

    private var pageZoomHost: PageZoomHost? = null
    private var iframeMode = false
    private var iframeUrl: String? = null
    private var iframeTitle: String? = null
    private var iframeCanGoBack = false
    private var iframeCanGoForward = false

    private var backgroundInterfaceAdded = false

    val isPageZoomHostActive: Boolean
        get() = iframeMode && pageZoomHost?.isActive == true

    internal fun setPageZoomHost(host: PageZoomHost?) {
        if (pageZoomHost === host) return
        if (pageZoomHost != null) {
            removeJavascriptInterface(PageZoomHost.BRIDGE_NAME)
        }
        pageZoomHost = host
        if (host != null) {
            addJavascriptInterface(host.bridge, PageZoomHost.BRIDGE_NAME)
            host.attach(this)
        } else {
            iframeMode = false
            iframeUrl = null
            iframeTitle = null
            iframeCanGoBack = false
            iframeCanGoForward = false
        }
    }

    fun updateIframeState(
        url: String?,
        title: String?,
        canGoBack: Boolean,
        canGoForward: Boolean
    ) {
        if (!url.isNullOrBlank() && url != "about:blank") iframeUrl = url
        if (title != null) iframeTitle = title
        iframeCanGoBack = canGoBack
        iframeCanGoForward = canGoForward
    }

    fun pageZoomNavigateTo(url: String) {
        pageZoomHost?.navigateFrame(this, url)
    }

    internal fun loadPageZoomHost(baseUrl: String, html: String) {
        iframeMode = true
        super.loadDataWithBaseURL(baseUrl, html, "text/html", "utf-8", null)
    }

    override fun loadUrl(url: String) {
        val host = pageZoomHost
        if (host != null && host.isActive) {
            if (host.canWrap(url)) {
                iframeMode = true
                iframeUrl = url
                iframeTitle = null
                iframeCanGoBack = false
                iframeCanGoForward = false
                host.load(this, url)
            } else {
                iframeMode = false
                if (url.startsWith("file:") || url.startsWith("content:")) {
                    setInitialScale(host.zoomPercent)
                }
                super.loadUrl(url)
            }
        } else {
            iframeMode = false
            super.loadUrl(url)
        }
    }

    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) {
        val host = pageZoomHost
        if (host != null && host.isActive) {
            if (host.canWrap(url)) {
                iframeMode = true
                iframeUrl = url
                iframeTitle = null
                iframeCanGoBack = false
                iframeCanGoForward = false
                host.load(this, url)
            } else {
                iframeMode = false
                if (url.startsWith("file:") || url.startsWith("content:")) {
                    setInitialScale(host.zoomPercent)
                }
                super.loadUrl(url, additionalHttpHeaders)
            }
        } else {
            iframeMode = false
            super.loadUrl(url, additionalHttpHeaders)
        }
    }

    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        iframeMode = false
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun reload() {
        if (isPageZoomHostActive) {
            pageZoomHost?.reload(this)
        } else {
            super.reload()
        }
    }

    override fun stopLoading() {
        if (isPageZoomHostActive) pageZoomHost?.stop(this)
        super.stopLoading()
    }

    override fun canGoBack(): Boolean {
        return if (isPageZoomHostActive) iframeCanGoBack || super.canGoBack() else super.canGoBack()
    }

    override fun goBack() {
        if (isPageZoomHostActive && iframeCanGoBack) {
            pageZoomHost?.back(this)
        } else {
            super.goBack()
        }
    }

    override fun canGoForward(): Boolean {
        return if (isPageZoomHostActive) iframeCanGoForward || super.canGoForward() else super.canGoForward()
    }

    override fun goForward() {
        if (isPageZoomHostActive && iframeCanGoForward) {
            pageZoomHost?.forward(this)
        } else {
            super.goForward()
        }
    }

    override fun getUrl(): String? {
        return if (isPageZoomHostActive) iframeUrl ?: super.getUrl() else super.getUrl()
    }

    override fun getTitle(): String? {
        return if (isPageZoomHostActive) iframeTitle ?: super.getTitle() else super.getTitle()
    }

    fun ensureBackgroundInterface() {
        if (backgroundInterfaceAdded) return
        backgroundInterfaceAdded = true
        addJavascriptInterface(backgroundHold, BackgroundMediaScript.JS_INTERFACE)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        if (keepPlayingInBackground && visibility != View.VISIBLE) {
            super.onWindowVisibilityChanged(View.VISIBLE)
            return
        }
        super.onWindowVisibilityChanged(visibility)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val connection = super.onCreateInputConnection(outAttrs)
        if (resources.configuration.keyboard == Configuration.KEYBOARD_QWERTY) {
            outAttrs.imeOptions = stripEnterAction(outAttrs.imeOptions)
        }
        return connection
    }

    companion object {
        /**
         * Clear the editor action and mark "no Enter action" so the IME cannot
         * translate Enter into performEditorAction. Flag bits above the action
         * mask are preserved.
         */
        fun stripEnterAction(imeOptions: Int): Int =
            (imeOptions and EditorInfo.IME_MASK_ACTION.inv()) or
                EditorInfo.IME_ACTION_NONE or EditorInfo.IME_FLAG_NO_ENTER_ACTION
    }
}
