package com.webtoapp.ui.shared

import android.app.Activity
import android.os.Build
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Status-bar color modes also drive the navigation bar and the fullscreen
 * margin bands (#1171). Transparent leaves the bars clear and turns off the
 * system contrast scrim. Follow-page and transparent modes paint the bands
 * from the sampled page so a colorful page is not framed in theme white
 * or black.
 */
object PageEdgeChrome {
    const val LIGHT = "#FFFBFE"
    const val DARK = "#1C1B1F"

    fun topBandHex(mode: String, sampleTop: String?, custom: String?, isDark: Boolean): String? = when (mode) {
        "TRANSPARENT", "PAGE_TOP" -> sampleTop?.takeIf { it.isNotBlank() }
        "CUSTOM" -> custom?.takeIf { it.isNotBlank() } ?: themeHex(isDark)
        "THEME" -> themeHex(isDark)
        else -> null
    }

    fun bottomBandHex(
        mode: String,
        sampleBottom: String?,
        sampleTop: String?,
        custom: String?,
        isDark: Boolean
    ): String? = when (mode) {
        "TRANSPARENT", "PAGE_TOP" -> sampleBottom?.takeIf { it.isNotBlank() }
            ?: sampleTop?.takeIf { it.isNotBlank() }
        "CUSTOM" -> custom?.takeIf { it.isNotBlank() } ?: themeHex(isDark)
        "THEME" -> themeHex(isDark)
        else -> null
    }

    /** Null means a transparent navigation bar. */
    fun navigationBarHex(
        mode: String,
        sampleBottom: String?,
        sampleTop: String?,
        custom: String?,
        isDark: Boolean
    ): String? = when (mode) {
        "TRANSPARENT" -> null
        "PAGE_TOP" -> sampleBottom?.takeIf { it.isNotBlank() }
            ?: sampleTop?.takeIf { it.isNotBlank() }
            ?: themeHex(isDark)
        "CUSTOM" -> custom?.takeIf { it.isNotBlank() } ?: themeHex(isDark)
        else -> themeHex(isDark)
    }

    fun themeHex(isDark: Boolean): String = if (isDark) DARK else LIGHT

    fun applyNavigationBar(
        activity: Activity,
        mode: String,
        sampleBottom: String?,
        sampleTop: String?,
        custom: String?,
        darkIcons: Boolean?,
        isDark: Boolean
    ) {
        // Below API 30 the decor does not draw behind the bars, so a transparent
        // navigation bar is just the window background. Match the status-bar
        // degradation and paint the theme color instead.
        val effectiveMode = if (
            mode == "TRANSPARENT" && WindowHelper.isClassicSystemBarsWindow(activity)
        ) {
            "THEME"
        } else {
            mode
        }
        val hex = navigationBarHex(effectiveMode, sampleBottom, sampleTop, custom, isDark)
        val color = if (hex == null) {
            android.graphics.Color.TRANSPARENT
        } else {
            runCatching { android.graphics.Color.parseColor(hex) }
                .getOrDefault(android.graphics.Color.parseColor(themeHex(isDark)))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced =
                effectiveMode != "TRANSPARENT" && effectiveMode != "PAGE_TOP"
        }
        activity.window.navigationBarColor = color
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.isAppearanceLightNavigationBars = darkIcons
            ?: if (color == android.graphics.Color.TRANSPARENT) !isDark else WindowHelper.isColorLight(color)
    }
}
