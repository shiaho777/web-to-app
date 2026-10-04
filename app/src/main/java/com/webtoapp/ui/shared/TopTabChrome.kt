package com.webtoapp.ui.shared

/**
 * Colors for the multi-web top tab bar (#1173). A live page sample wins,
 * then the site's stored theme color, then the same light/dark surfaces the
 * status bar uses. Text is chosen from the same luminance cutoff as
 * [WindowHelper.isColorLight].
 */
object TopTabChrome {
    const val LIGHT_BG = "#FFFBFE"
    const val DARK_BG = "#1C1B1F"

    fun backgroundHex(sample: String?, theme: String?, isDark: Boolean): String {
        parseRgb(sample)?.let { return normalize(sample!!) }
        parseRgb(theme)?.let { return normalize(theme!!) }
        return if (isDark) DARK_BG else LIGHT_BG
    }

    /** True when light text should sit on [backgroundHex]. */
    fun textIsLightOn(backgroundHex: String): Boolean = !isLight(backgroundHex)

    fun isLight(hex: String): Boolean {
        val rgb = parseRgb(hex) ?: return true
        val luminance = (0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]) / 255.0
        return luminance > 0.5
    }

    private fun normalize(hex: String): String {
        val body = hex.trim().removePrefix("#")
        val expanded = if (body.length == 3 || body.length == 4) {
            body.asSequence().joinToString("") { "$it$it" }
        } else {
            body
        }
        return "#" + expanded.take(6).uppercase()
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
