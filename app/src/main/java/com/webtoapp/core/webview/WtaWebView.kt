package com.webtoapp.core.webview

import android.content.Context
import android.content.res.Configuration
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
