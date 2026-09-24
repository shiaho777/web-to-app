package com.webtoapp.core.webview

import android.content.ComponentCallbacks2
import android.content.Context
import android.webkit.CookieManager
import com.webtoapp.core.logging.AppLogger

/**
 * Tiered response to `ComponentCallbacks2.onTrimMemory`.
 *
 * The previous handler only logged "skipped manual GC" — a WebView app that
 * answers memory pressure with nothing forces LMK to reclaim memory from
 * everyone else first, which is how a bloated renderer ends up killing the
 * system Launcher instead of being shed by its owner (#1033).
 *
 * What each tier can afford:
 * - Foreground levels (RUNNING_LOW / RUNNING_CRITICAL): only caches the user
 *   cannot see disappear. The visible WebView is never touched.
 * - UI_HIDDEN and the background LRU levels (BACKGROUND / MODERATE): every
 *   reclaimable cache goes — image memory cache, the WebView prewarm pool —
 *   plus a cookie flush that is cheap while the UI is gone anyway.
 * - COMPLETE: the process is at the head of the kill list and the renderer is
 *   by far our largest allocation. Callers get `true` and should tear the
 *   WebView down (state saved — recreation on resume reuses the same path as
 *   process-death recovery). Better a reload on return than a dead Launcher.
 */
object WebViewMemoryTrimmer {

    private const val TAG = "WebViewMemoryTrimmer"

    enum class TrimAction { NONE, LIGHT, HEAVY, TEARDOWN }

    /** Pure level→action mapping; kept separate so unit tests can pin the tiers. */
    fun actionFor(level: Int): TrimAction = when {
        level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> TrimAction.TEARDOWN
        level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> TrimAction.HEAVY
        level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> TrimAction.HEAVY
        level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> TrimAction.LIGHT
        else -> TrimAction.NONE
    }

    /** True when the caller should tear down its WebView entirely (COMPLETE). */
    fun shouldTeardownWebView(level: Int): Boolean =
        actionFor(level) == TrimAction.TEARDOWN

    /**
     * Perform the cache-side work for [level] and report whether the caller
     * should additionally tear down its WebView.
     */
    fun onTrimMemory(level: Int, context: Context): Boolean {
        return when (actionFor(level)) {
            TrimAction.NONE -> false
            TrimAction.LIGHT -> {
                trimImageCache(context, level)
                false
            }
            TrimAction.HEAVY -> {
                flushCookies()
                clearImageCache(context)
                false
            }
            TrimAction.TEARDOWN -> {
                flushCookies()
                clearImageCache(context)
                runCatching { WebViewPool.release() }
                true
            }
        }
    }

    private fun trimImageCache(context: Context, level: Int) {
        runCatching {
            coil.Coil.imageLoader(context.applicationContext).memoryCache?.trimMemory(level)
        }.onFailure {
            AppLogger.w(TAG, "Image cache trim failed: ${it.message}")
        }
    }

    private fun clearImageCache(context: Context) {
        runCatching {
            coil.Coil.imageLoader(context.applicationContext).memoryCache?.clear()
        }.onFailure {
            AppLogger.w(TAG, "Image cache clear failed: ${it.message}")
        }
    }

    private fun flushCookies() {
        runCatching { CookieManager.getInstance().flush() }
    }
}
