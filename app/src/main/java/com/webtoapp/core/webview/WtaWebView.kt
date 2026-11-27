package com.webtoapp.core.webview

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.webkit.WebView

class WtaWebView(context: Context) : WebView(context) {
    override fun destroy() {
        WebViewStreamRoutes.destroy(this)
        super.destroy()
    }


    var siteId: String? = null

    var keepPlayingInBackground: Boolean = false

    val backgroundHold: BackgroundMediaHold = BackgroundMediaHold()

    private var backgroundInterfaceAdded = false

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
        fun stripEnterAction(imeOptions: Int): Int =
            (imeOptions and EditorInfo.IME_MASK_ACTION.inv()) or
                EditorInfo.IME_ACTION_NONE or EditorInfo.IME_FLAG_NO_ENTER_ACTION
    }
}
