package com.webtoapp.core.webview

/**
 * True while a WebView file chooser is waiting on the system picker.
 *
 * The shell activity is singleTask, and opening the picker sends the app to
 * the background. TRIM_MEMORY_COMPLETE then tears the WebView down and the
 * resume path builds a new one at the start URL (#1033). The pick comes back
 * to a page that no longer owns the [android.webkit.ValueCallback], so
 * `<input type="file">` never fires `change` and the user lands on the home
 * page (#1319). Callers must not destroy that WebView while this is open.
 */
object FileChooserLifetime {

    @Volatile
    private var open = false

    fun markOpen() {
        open = true
    }

    fun markClosed() {
        open = false
    }

    fun isOpen(): Boolean = open
}
