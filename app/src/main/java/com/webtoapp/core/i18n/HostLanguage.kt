package com.webtoapp.core.i18n

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Applies the saved (or system) language to the process-wide [Strings] state.
 *
 * [Strings.lang] defaults to Chinese, and [InitializeLanguage] only runs inside
 * the builder's main screen. A launcher shortcut cold-starts [com.webtoapp.ui.webview.WebViewActivity]
 * without that screen, so toasts and plugins stayed Chinese until the builder
 * had been opened in the same process (#1391).
 */
object HostLanguage {

    private const val TAG = "HostLanguage"

    fun apply(context: Context, language: AppLanguage) {
        Strings.setLanguage(language)
        Strings.attachContext(context, language)
    }

    /**
     * Blocks the caller until DataStore has been read. Safe from the main
     * thread: the read runs on [Dispatchers.IO], not on a flow collected on Main.
     */
    fun applyBlocking(context: Context) {
        try {
            val language = runBlocking(Dispatchers.IO) {
                LanguageManager.getInstance(context).getCurrentLanguage()
            }
            apply(context, language)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to apply saved language", e)
        }
    }
}
