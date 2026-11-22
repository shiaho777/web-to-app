package com.webtoapp.ui.shared

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.roundToInt

data class StatusBarAppearance(
    val bandColor: Int?,
    val imagePath: String?,
    val imageAlpha: Float,
    val darkIcons: Boolean,
    val windowColor: Int
) {
    internal fun applyTo(activity: Activity) {
        val window = activity.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
        }
        window.statusBarColor = windowColor
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = darkIcons
    }
}

internal fun resolveStatusBarAppearance(
    mode: String,
    customColor: String?,
    backgroundType: String,
    backgroundImage: String?,
    backgroundAlpha: Float,
    darkIcons: Boolean?,
    pageTop: String?,
    topTabColor: String?,
    themeColor: Int,
    pageBehindBar: Boolean,
    browserToolbarVisible: Boolean,
    classicWindow: Boolean
): StatusBarAppearance {
    val imagePath = backgroundImage?.takeIf { backgroundType == "IMAGE" && !classicWindow }
    val alpha = backgroundAlpha.coerceIn(0f, 1f)
    val sampledColor = parseStatusBarColor(pageTop)
    val transparent = mode == "TRANSPARENT"
    val tabColor = parseStatusBarColor(topTabColor).takeUnless { browserToolbarVisible }
    val contentColor = when {
        browserToolbarVisible -> themeColor
        tabColor != null -> tabColor
        else -> sampledColor ?: themeColor
    }
    val configuredColor = parseStatusBarColor(customColor)
    val color = when {
        imagePath != null -> if (pageBehindBar) contentColor else themeColor
        transparent -> if (classicWindow) themeColor else contentColor
        mode == "PAGE_TOP" -> sampledColor ?: configuredColor ?: themeColor
        mode == "CUSTOM" -> configuredColor ?: themeColor
        else -> themeColor
    }
    val bandAlpha = if (transparent || imagePath != null) 1f else alpha
    val visibleColor = ColorUtils.compositeColors(
        ColorUtils.setAlphaComponent(color, (bandAlpha * Color.alpha(color)).roundToInt()),
        themeColor
    )
    val drawBand = !classicWindow && (!transparent || !pageBehindBar)
    val useDarkIcons = darkIcons ?: (ColorUtils.calculateContrast(Color.BLACK, visibleColor) >=
        ColorUtils.calculateContrast(Color.WHITE, visibleColor))
    return StatusBarAppearance(
        bandColor = visibleColor.takeIf { drawBand && imagePath == null },
        imagePath = imagePath,
        imageAlpha = alpha,
        darkIcons = useDarkIcons,
        windowColor = if (classicWindow) visibleColor else Color.TRANSPARENT
    )
}

private fun parseStatusBarColor(hex: String?): Int? =
    hex?.let { runCatching { Color.parseColor(it) }.getOrNull() }
