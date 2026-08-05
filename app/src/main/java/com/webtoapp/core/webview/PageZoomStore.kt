package com.webtoapp.core.webview

import android.content.Context

/**
 * Persists a per-app runtime page-zoom override (a `WebSettings.textZoom` percentage)
 * so a user-adjusted zoom survives cold starts and applies immediately to the live
 * page without a reload.
 *
 * The store is a single shared SharedPreferences file keyed by app package name — it does
 * NOT create one prefs file per app (that would grow unbounded). Instead the package name
 * is embedded in the key, mirroring the [com.webtoapp.util.ConfigPresetStorage] helper
 * pattern.
 *
 * A stored value of `0` means "no runtime override" — fall back to the build-time default
 * (textZoom 100). `WebViewManager` applies a stored override AFTER the viewport/dark-mode
 * settings so it wins over e.g. the DESKTOP mode's `textZoom = 100`.
 */
object PageZoomStore {
    private const val PREFS_NAME = "shell_per_app_zoom"
    private const val KEY_PREFIX = "zoom_"
    private const val DEFAULT_ZOOM = 0

    /**
     * Returns the stored zoom percent for [packageName], or `0` if the user has not set a
     * runtime override for this app.
     */
    fun getZoomPercent(context: Context, packageName: String): Int {
        if (packageName.isBlank()) return DEFAULT_ZOOM
        return prefs(context).getInt(key(packageName), DEFAULT_ZOOM)
    }

    /**
     * Stores [percent] for [packageName]. Pass `0` (or [clearZoom]) to remove the override
     * and fall back to the build-time default.
     */
    fun setZoomPercent(context: Context, packageName: String, percent: Int) {
        if (packageName.isBlank()) return
        prefs(context).edit().putInt(key(packageName), percent.coerceAtLeast(0)).apply()
    }

    /** Removes the stored zoom override for [packageName]. */
    fun clearZoom(context: Context, packageName: String) {
        if (packageName.isBlank()) return
        prefs(context).edit().remove(key(packageName)).apply()
    }

    private fun key(packageName: String) = "$KEY_PREFIX$packageName"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
