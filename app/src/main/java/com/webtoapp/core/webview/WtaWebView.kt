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
