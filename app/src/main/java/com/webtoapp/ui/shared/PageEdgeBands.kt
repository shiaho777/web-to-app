package com.webtoapp.ui.shared

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/**
 * Paints the fullscreen margin bands. Placed outside the content padding so
 * the gap at the screen edge takes the page color instead of the theme surface.
 */
fun parseBandColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
}

fun Modifier.pageEdgeBands(top: Color?, bottom: Color?, bottomHeightPx: Float): Modifier {
    if (top == null && bottom == null) return this
    return this.drawBehind {
        if (top != null) drawRect(top)
        if (bottom != null && bottom != top && bottomHeightPx > 0f) {
            val height = bottomHeightPx.coerceAtMost(size.height)
            drawRect(
                color = bottom,
                topLeft = Offset(0f, size.height - height),
                size = Size(size.width, height)
            )
        }
    }
}
