package com.webtoapp.core.engine

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.webtoapp.core.i18n.Strings

/**
 * Full-screen dialog that hosts a script-opened window (OAuth popup) above
 * the page that called `window.open`. Closing the dialog does not by itself
 * deliver the sign-in result; the child window has to stay alive until the
 * page calls `window.close()` after `postMessage` (#1316).
 */
internal object ScriptPopupHost {

    private class Entry(val dialog: Dialog, val onDismiss: () -> Unit)

    private val entries = java.util.IdentityHashMap<View, Entry>()

    fun canShow(context: Context): Boolean = findActivity(context)?.let { !it.isFinishing } == true

    fun show(
        context: Context,
        content: View,
        onBack: () -> Boolean,
        onDismiss: () -> Unit
    ): Boolean {
        val activity = findActivity(context) ?: return false
        if (activity.isFinishing) return false
        if (entries.containsKey(content)) return true
        (content.parent as? ViewGroup)?.removeView(content)

        val dialog = Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
        val column = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.WHITE)
        }
        val bar = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(dp(activity, 8), dp(activity, 8), dp(activity, 8), dp(activity, 4))
        }
        val close = TextView(activity).apply {
            text = Strings.close
            setPadding(dp(activity, 16), dp(activity, 10), dp(activity, 16), dp(activity, 10))
            setOnClickListener { dismiss(content) }
        }
        bar.addView(close)
        column.addView(bar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        column.addView(content, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))
        dialog.setContentView(column)
        dialog.setCancelable(true)
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (!onBack()) dismiss(content)
                true
            } else {
                false
            }
        }
        dialog.setOnDismissListener {
            if (entries.remove(content) != null) onDismiss()
        }
        entries[content] = Entry(dialog, onDismiss)
        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return true
    }

    fun dismiss(content: View) {
        entries[content]?.dialog?.dismiss()
    }

    private fun findActivity(context: Context): Activity? {
        var current: Context? = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    private fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}
