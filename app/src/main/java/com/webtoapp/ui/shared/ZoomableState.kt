package com.webtoapp.ui.shared

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach

/**
 * Pinch-to-zoom + pan + double-tap-toggle state for full-screen image
 * viewers, designed to coexist with [androidx.compose.foundation.pager.HorizontalPager]:
 * single-finger drags at 1x are never consumed (the pager keeps them), while
 * pinches and zoomed pans are.
 *
 * Transform math (viewport pixels, scale about the viewport center, which is
 * what [graphicsLayer] does with the default transform origin):
 * a content point p (viewport coords at scale 1) lands at
 * `center + (p - center) * scale + offset`.
 */
@Stable
class ZoomableState(
    val minScale: Float = 1f,
    val maxScale: Float = 5f,
    val doubleTapScale: Float = 3f,
    val zoomToggleThreshold: Float = 1.5f,
) {
    var scale by mutableFloatStateOf(minScale)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set

    /** Viewport size in pixels. Set from layout; needed for clamping. */
    var viewportSize: Size = Size.Zero
        private set

    /** Content size in pixels at scale 1 (already fitted). */
    var contentSize: Size = Size.Zero
        private set

    fun setLayout(viewport: Size, content: Size) {
        viewportSize = viewport
        contentSize = content
        offset = clampOffset(offset, scale, viewport, content)
    }

    fun reset() {
        scale = minScale
        offset = Offset.Zero
    }

    /**
     * Apply a pinch step, keeping [centroid] (viewport coords) stationary.
     * Returns true when anything changed.
     */
    fun applyZoom(zoomChange: Float, centroid: Offset): Boolean {
        if (zoomChange == 1f) return false
        val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
        if (newScale == scale) return false
        val center = Offset(viewportSize.width / 2f, viewportSize.height / 2f)
        val k = newScale / scale
        val anchored = centroid - center
        offset = clampOffset(anchored - (anchored - offset) * k, newScale, viewportSize, contentSize)
        scale = newScale
        if (scale == minScale) offset = Offset.Zero
        return true
    }

    /** Apply a pan step (viewport pixels). Returns true when anything changed. */
    fun applyPan(panChange: Offset): Boolean {
        if (panChange == Offset.Zero || scale <= minScale) return false
        val next = clampOffset(offset + panChange, scale, viewportSize, contentSize)
        if (next == offset) return false
        offset = next
        return true
    }

    /**
     * Double-tap toggle. Returns the tap point (for callers that need it);
     * mutates state directly (snap, no animation).
     */
    fun toggleZoom(tap: Offset) {
        if (scale > zoomToggleThreshold) {
            reset()
        } else {
            applyZoom(doubleTapScale / scale, tap)
        }
    }

    companion object {
        fun clampOffset(
            offset: Offset,
            scale: Float,
            viewport: Size,
            content: Size
        ): Offset {
            val maxX = maxOf(0f, (content.width * scale - viewport.width) / 2f)
            val maxY = maxOf(0f, (content.height * scale - viewport.height) / 2f)
            return Offset(
                offset.x.coerceIn(-maxX, maxX),
                offset.y.coerceIn(-maxY, maxY)
            )
        }

        fun fittedContentSize(
            imageWidth: Int,
            imageHeight: Int,
            viewport: Size
        ): Size {
            if (imageWidth <= 0 || imageHeight <= 0 ||
                viewport.width <= 0f || viewport.height <= 0f
            ) {
                return Size.Zero
            }
            val s = minOf(viewport.width / imageWidth, viewport.height / imageHeight)
            return Size(imageWidth * s, imageHeight * s)
        }
    }
}

/**
 * Gesture wiring for [ZoomableState]: pinch zoom (focal-aware), pan while
 * zoomed, and pass-through of single-finger drags at 1x so an enclosing
 * pager keeps working. Rotation is intentionally locked.
 */
fun Modifier.zoomableGesture(
    state: ZoomableState,
    enabled: Boolean = true
): Modifier = this.then(
    Modifier.pointerInput(enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            var pastTouchSlop = false
            var zoom = 1f
            var pan = Offset.Zero
            val touchSlop = viewConfiguration.touchSlop
            while (true) {
                val event = awaitPointerEvent()
                val pressed = event.changes.fastAny { it.pressed }
                // Another consumer (e.g. the pager mid-drag) claimed the
                // stream: never fight it.
                val claimed = event.changes.fastAny { it.isConsumed }
                if (claimed) {
                    if (!pressed) break
                    continue
                }
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()
                val pointerCount = event.changes.size
                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    pan += panChange
                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = kotlin.math.abs(1f - zoom) * centroidSize
                    if (zoomMotion > touchSlop ||
                        pan.getDistance() > touchSlop ||
                        pointerCount > 1
                    ) {
                        pastTouchSlop = true
                    } else {
                        if (!pressed) break
                        continue
                    }
                }
                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    var consumedZoom = false
                    var consumedPan = false
                    if (pointerCount > 1 || zoomChange != 1f) {
                        consumedZoom = state.applyZoom(zoomChange, centroid)
                    }
                    if (state.scale > state.minScale && panChange != Offset.Zero) {
                        consumedPan = state.applyPan(panChange)
                    }
                    if (consumedZoom || consumedPan) {
                        event.changes.fastForEach { change ->
                            consumePositionChange(change)
                        }
                    }
                }
                if (!pressed) break
            }
        }
    }
)

private fun consumePositionChange(change: PointerInputChange) {
    if (change.position != change.previousPosition) {
        change.consume()
    }
}

/** Modifier shorthand: [zoomableGesture] + [graphicsLayer] in one. */
fun Modifier.zoomable(
    state: ZoomableState,
    enabled: Boolean = true
): Modifier = this.then(
    Modifier
        .zoomableGesture(state, enabled)
        .graphicsLayer(
            scaleX = state.scale,
            scaleY = state.scale,
            translationX = state.offset.x,
            translationY = state.offset.y
        )
)

/** Touch slop in pixels for gesture detection. */
private val PointerInputScope.touchSlop: Float
    get() = viewConfiguration.touchSlop
