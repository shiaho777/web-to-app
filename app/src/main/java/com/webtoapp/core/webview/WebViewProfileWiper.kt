package com.webtoapp.core.webview

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import java.io.File

/**
 * Deletes the on-disk system WebView profile (`app_webview` and the legacy
 * cache files next to it).
 *
 * [android.webkit.CookieManager.removeAllCookies] does not do this. It is
 * asynchronous, it leaves Service Workers, Cache Storage, the V8 code cache
 * and the GPU cache in place, and calling [android.webkit.CookieManager.flush]
 * before the callback returns can write the old cookie database back. A
 * generated app then works once — while the profile directory does not exist
 * yet — and fails on every later cold start until the user clears app data.
 *
 * The directory may be removed only before this process has initialized the
 * WebView provider. After that, Chromium holds the cookie database open and
 * deleting it corrupts the profile.
 */
internal object WebViewProfileWiper {

    private const val TAG = "WebViewProfileWiper"

    fun wipeIfProviderInactive(context: Context): Boolean {
        val appInfo = context.applicationInfo ?: return false
        val dataDir = appInfo.dataDir?.let(::File) ?: return false
        return wipe(
            dataDir = dataDir,
            cacheDir = context.cacheDir,
            providerInitialized = isSystemWebViewProviderInitialized()
        )
    }

    /**
     * @return true when the provider was not live and every known profile
     *   path is gone (including the case where none existed). Callers must
     *   then skip [android.webkit.CookieManager] — touching it would create
     *   a new provider and race the page that is about to load.
     */
    fun wipe(dataDir: File, cacheDir: File, providerInitialized: Boolean): Boolean {
        if (providerInitialized) {
            AppLogger.i(TAG, "WebView provider already live; leaving the on-disk profile in place")
            return false
        }
        val targets = profilePaths(dataDir, cacheDir)
        targets.forEach { path ->
            if (!path.exists()) return@forEach
            val removed = deleteTree(path)
            AppLogger.i(TAG, "Fresh-session profile ${path.name} removed=$removed")
        }
        val leftover = targets.filter { it.exists() }
        if (leftover.isNotEmpty()) {
            AppLogger.w(TAG, "Fresh-session profile wipe left ${leftover.joinToString { it.name }}")
            return false
        }
        return true
    }

    fun profilePaths(dataDir: File, cacheDir: File): List<File> = listOf(
        File(dataDir, "app_webview"),
        // Huawei's system WebView uses its own directory name.
        File(dataDir, "app_hws_webview"),
        File(cacheDir, "WebView"),
        File(cacheDir, "org.chromium.android_webview"),
        File(dataDir, "databases/webview.db"),
        File(dataDir, "databases/webview.db-journal"),
        File(dataDir, "databases/webviewCache.db"),
        File(dataDir, "databases/webviewCache.db-journal"),
    )

    /**
     * True once this process has loaded the system WebView provider.
     * Reflection failure is treated as "already live" so we never delete a
     * directory Chromium might have open.
     */
    fun isSystemWebViewProviderInitialized(): Boolean {
        return try {
            val factory = Class.forName("android.webkit.WebViewFactory")
            val field = factory.getDeclaredField("sProviderInstance")
            field.isAccessible = true
            field.get(null) != null
        } catch (t: Throwable) {
            AppLogger.w(TAG, "WebView provider probe failed; skipping directory wipe", t)
            true
        }
    }

    private fun deleteTree(file: File): Boolean {
        if (file.isDirectory) {
            file.listFiles()?.forEach { child -> deleteTree(child) }
        }
        if (!file.exists()) return true
        if (file.delete()) return true
        file.setWritable(true)
        return file.delete()
    }
}
