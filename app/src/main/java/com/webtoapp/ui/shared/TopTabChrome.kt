package com.webtoapp.ui.shared

import androidx.compose.runtime.mutableStateMapOf

object TopTabChrome {
    const val LIGHT_BG = "#FFFBFE"
    const val DARK_BG = "#1C1B1F"

    fun textIsLightOn(backgroundHex: String): Boolean = !isLight(backgroundHex)

    fun isLight(hex: String): Boolean {
        val rgb = parseRgb(hex) ?: return true
        val luminance = (0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]) / 255.0
        return luminance > 0.5
    }

    internal fun normalizeHex(hex: String?): String? =
        parseRgb(hex)?.joinToString(separator = "", prefix = "#") {
            it.toString(16).padStart(2, '0').uppercase()
        }

    private fun parseRgb(hex: String?): IntArray? {
        if (hex.isNullOrBlank() || !hex.trim().startsWith("#")) return null
        val body = hex.trim().removePrefix("#")
        val expanded = when (body.length) {
            3, 4 -> body.take(3).asSequence().joinToString("") { "$it$it" }
            6, 8 -> body.take(6)
            else -> return null
        }
        if (expanded.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) return null
        return intArrayOf(
            expanded.substring(0, 2).toInt(16),
            expanded.substring(2, 4).toInt(16),
            expanded.substring(4, 6).toInt(16)
        )
    }
}

internal class TopTabColors {
    private val samples = mutableStateMapOf<String, String>()
    private var lastDisplayedColor: String? = null

    fun recordSample(siteId: String, color: String?) {
        TopTabChrome.normalizeHex(color)?.let { samples[siteId] = it }
    }

    fun siteColor(siteId: String?, themeColor: String?): String? =
        siteId?.let(samples::get) ?: TopTabChrome.normalizeHex(themeColor)

    fun backgroundHex(siteColor: String?, isDark: Boolean): String =
        siteColor ?: lastDisplayedColor ?: if (isDark) TopTabChrome.DARK_BG else TopTabChrome.LIGHT_BG

    fun rememberDisplayedColor(siteColor: String?) {
        if (siteColor != null) lastDisplayedColor = siteColor
    }
}
