package com.webtoapp.core.appcache

import android.content.Context
import android.webkit.WebStorage
import android.webkit.WebView
import com.webtoapp.core.apkbuilder.ApkBuildCache
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.webview.WebViewPool
import com.webtoapp.data.model.WebApp
import java.io.File
import java.util.Locale

/**
 * Clears a single app's host-side caches:
 *
 * - the incremental APK build cache entry (`apk_build_cache/app_<id>`) — usually the
 *   largest per-app disk consumer; regenerated on the next build;
 * - the app's WebView origin storage (localStorage / IndexedDB / WebSQL) via
 *   [WebStorage.deleteOrigin], so the site starts fresh on the next preview;
 * - the shared WebView HTTP disk cache — android.webkit has no per-origin API for
 *   it, and it is pure re-downloadable cache.
 *
 * Cookies, app configuration and exported APKs are intentionally untouched —
 * clearing cookies would log the user out of every other previewed app.
 */
object AppCacheCleaner {

    data class Result(
        val freedBuildCacheBytes: Long,
        val originsCleared: Int
    ) {
        val freedBytesText: String
            get() = formatBytes(freedBuildCacheBytes)
    }

    fun clearForApp(context: Context, app: WebApp): Result {
        val appContext = context.applicationContext

        var freedBytes = 0L
        try {
            val cache = ApkBuildCache(appContext)
            val packageName = app.packageName.orEmpty()
            freedBytes = cache.entrySizeBytes(app, packageName)
            cache.clear(app, packageName)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to clear build cache for app ${app.id}", e)
        }

        var originsCleared = 0
        for (origin in originsFor(app)) {
            try {
                WebStorage.getInstance().deleteOrigin(origin)
                originsCleared++
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to clear storage for origin $origin", e)
            }
        }

        try {
            val webView = WebViewPool.acquire(appContext)
            try {
                webView.clearCache(true)
            } finally {
                WebViewPool.recycle(webView)
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to clear WebView HTTP cache", e)
        }

        AppLogger.i(
            TAG,
            "Cleared caches for app ${app.id}: freedBytes=$freedBytes origins=$originsCleared"
        )

        return Result(freedBytes, originsCleared)
    }

    /** Web origins whose site storage belongs to this app. Pure and unit-testable. */
    internal fun originsFor(app: WebApp): List<String> {
        val urls = buildList {
            add(app.url)
            app.multiWebConfig?.sites?.forEach { add(it.url) }
        }
        return urls
            .mapNotNull { url -> webOriginOf(url) }
            .distinct()
    }

    private fun webOriginOf(raw: String): String? {
        if (raw.isBlank()) return null
        val trimmed = raw.trim()
        // Saved apps may store the URL without a scheme (the editor normalizes
        // it only at build/preview time); treat bare hostnames as http, same as
        // ensureWebUrlScheme's default.
        val candidate = if (trimmed.contains("://")) trimmed else "http://$trimmed"
        val uri = try {
            java.net.URI(candidate)
        } catch (_: Exception) {
            return null
        }
        val scheme = uri.scheme?.lowercase() ?: return null
        if (scheme !in HTTP_SCHEMES) return null
        val host = uri.host ?: return null
        val port = if (uri.port != -1) ":${uri.port}" else ""
        return "$scheme://$host$port"
    }

    internal fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024L * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            bytes < 1024L * 1024 * 1024 ->
                String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    private val HTTP_SCHEMES = setOf("http", "https")

    private const val TAG = "AppCacheCleaner"
}
