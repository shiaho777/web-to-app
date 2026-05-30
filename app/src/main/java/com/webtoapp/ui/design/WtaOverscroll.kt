package com.webtoapp.ui.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

@OptIn(ExperimentalFoundationApi::class)
class WtaOverscrollEffect(
    private val scope: CoroutineScope,
    private val maxStretchPx: Float = 400f
) : OverscrollEffect {

    private val overscrollOffset = Animatable(0f, Float.VectorConverter)

    override val isInProgress: Boolean
        get() = overscrollOffset.value != 0f

    override val effectModifier: Modifier
        get() = Modifier.graphicsLayer {
            translationY = overscrollOffset.value
        }

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        val currentOffset = overscrollOffset.value
        val sameDirection = sign(delta.y) == sign(currentOffset)

        val consumedByPreScroll = if (currentOffset != 0f && !sameDirection) {
            val newOffset = (currentOffset + delta.y).let {
                if (sign(it) != sign(currentOffset)) 0f else it
            }
            val consumed = newOffset - currentOffset
            scope.launch { overscrollOffset.snapTo(newOffset) }
            Offset(0f, consumed)
        } else {
            Offset.Zero
        }

        val remainingDelta = delta - consumedByPreScroll
        val consumedByScroll = performScroll(remainingDelta)
        val leftover = remainingDelta - consumedByScroll

        if (source == NestedScrollSource.Drag && leftover.y != 0f) {
            val next = overscrollOffset.value + leftover.y * 0.5f
            val clamped = rubberBandClamp(next, maxStretchPx)
            scope.launch { overscrollOffset.snapTo(clamped) }
        }

        return consumedByPreScroll + consumedByScroll
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        val consumed = performFling(velocity)
        val leftover = velocity - consumed

        overscrollOffset.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = 0.7f,
                stiffness = 200f
            ),
            initialVelocity = leftover.y * 0.3f
        )
    }
}

private fun rubberBandClamp(offset: Float, maxStretch: Float): Float {
    if (offset == 0f) return 0f
    val absOffset = abs(offset)
    val resistance = 1f - 1f / (absOffset / maxStretch + 1f)
    return resistance * maxStretch * sign(offset)
}

@Composable
fun rememberWtaOverscroll(maxStretchPx: Float = 400f): WtaOverscrollEffect {
    val scope = rememberCoroutineScope()
    return remember(scope, maxStretchPx) { WtaOverscrollEffect(scope, maxStretchPx) }
}
