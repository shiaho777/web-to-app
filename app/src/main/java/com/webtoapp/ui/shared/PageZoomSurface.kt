package com.webtoapp.ui.shared

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

/**
 * Lays the browser surface out at `1 / scale` and scales it back down, so a
 * zoomed-out page keeps its CSS viewport on the window. CSS `zoom` shrinks the
 * painted box instead, and a `100vh` container then stops one screen short
 * (#1508). Only zoom-out uses this; zoom-in stays on CSS `zoom` so text is
 * re-laid out rather than upscaled.
 */
fun Modifier.pageZoomSurface(pageZoomPercent: Int): Modifier {
    val percent = if (pageZoomPercent > 0) pageZoomPercent else 100
    if (percent >= 100) return this
    val scale = percent / 100f
    return this
        .layout { measurable, constraints ->
            val width = constraints.maxWidth
            val height = constraints.maxHeight
            if (width <= 0 || height <= 0 ||
                width == Constraints.Infinity || height == Constraints.Infinity
            ) {
                val placeable = measurable.measure(constraints)
                return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            val scaledWidth = (width / scale).roundToInt().coerceAtLeast(1)
            val scaledHeight = (height / scale).roundToInt().coerceAtLeast(1)
            val placeable = measurable.measure(Constraints.fixed(scaledWidth, scaledHeight))
            layout(width, height) { placeable.place(0, 0) }
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
}
