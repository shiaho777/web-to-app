package com.webtoapp.ui.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.webtoapp.ui.theme.LocalAnimationSettings
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun WtaSwipeBackContainer(
    onBack: () -> Unit,
    enabled: Boolean = true,
    edgeWidthDp: Int = 24,
    dismissThresholdFraction: Float = 0.35f,
    velocityThreshold: Float = 1200f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val edgeWidthPx = with(density) { edgeWidthDp.dp.toPx() }
    val dismissThresholdPx = screenWidthPx * dismissThresholdFraction

    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val animSettings = LocalAnimationSettings.current
    val view = LocalView.current

    val swipeModifier = if (enabled && animSettings.enabled) {
        Modifier.pointerInput(Unit) {
            var thresholdHapticked = false
            // Set the moment a dismiss gesture commits: while the slide-out
            // animation still runs, a second rapid edge swipe would otherwise
            // cancel the in-flight animateTo (mutator mutex) and let BOTH
            // coroutines reach onBack() — a double pop that can empty the nav
            // back stack and strand the app on a blank screen.
            var dismissed = false
            val tracker = VelocityTracker()

            // Only a down inside the left edge is ours. detectHorizontalDragGestures
            // consumes the pointer as soon as horizontal touch-slop is crossed,
            // before it can tell the drag missed the edge, and that cancels the
            // click under the finger. A slightly sliding tap on Build then does
            // nothing, and the next steadier tap is the one that lands.
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (!swipeBackClaimsDown(down.position.x, edgeWidthPx, dismissed)) {
                    return@awaitEachGesture
                }
                thresholdHapticked = false
                tracker.resetTracking()
                var overSlop = 0f
                val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, over ->
                    change.consume()
                    overSlop = over
                } ?: return@awaitEachGesture

                if (overSlop > 0f) {
                    tracker.addPosition(drag.uptimeMillis, drag.position)
                    val next = (offsetX.value + overSlop).coerceAtLeast(0f)
                    scope.launch { offsetX.snapTo(next) }
                    if (!thresholdHapticked && next > dismissThresholdPx && animSettings.hapticsEnabled) {
                        thresholdHapticked = true
                        performHaptic(view)
                    }
                }

                val completed = horizontalDrag(drag.id) { change ->
                    val dragAmount = change.positionChange().x
                    if (dragAmount > 0f || offsetX.value > 0f) {
                        change.consume()
                        tracker.addPosition(change.uptimeMillis, change.position)
                        val next = (offsetX.value + dragAmount).coerceAtLeast(0f)
                        scope.launch { offsetX.snapTo(next) }
                        if (!thresholdHapticked && next > dismissThresholdPx && animSettings.hapticsEnabled) {
                            thresholdHapticked = true
                            performHaptic(view)
                        }
                    }
                }
                if (dismissed) return@awaitEachGesture
                if (!completed) {
                    scope.launch {
                        offsetX.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = 0.78f,
                                stiffness = 350f
                            )
                        )
                    }
                    return@awaitEachGesture
                }

                val lastVelocity = tracker.calculateVelocity().x
                val shouldDismiss = offsetX.value > dismissThresholdPx ||
                    lastVelocity > velocityThreshold
                if (shouldDismiss) dismissed = true
                scope.launch {
                    if (shouldDismiss) {
                        if (animSettings.hapticsEnabled) performHeavyHaptic(view)
                        offsetX.animateTo(
                            targetValue = screenWidthPx,
                            animationSpec = spring(
                                dampingRatio = 0.9f,
                                stiffness = 400f
                            ),
                            initialVelocity = lastVelocity.coerceAtLeast(0f)
                        )
                        onBack()
                    } else {
                        offsetX.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = 0.78f,
                                stiffness = 350f
                            ),
                            initialVelocity = lastVelocity
                        )
                    }
                }
            }
        }
    } else {
        Modifier
    }

    androidx.compose.foundation.layout.Box(
        modifier = swipeModifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = offsetX.value

                val progress = (offsetX.value / screenWidthPx).coerceIn(0f, 1f)
                val s = 1f - progress * 0.04f
                scaleX = s
                scaleY = s
            }
    ) {
        content()
    }
}

/**
 * True only for a pointer-down that swipe-back is allowed to take. Anything else
 * must stay unconsumed so the control under the finger still receives the click.
 */
internal fun swipeBackClaimsDown(downX: Float, edgeWidthPx: Float, dismissed: Boolean): Boolean =
    !dismissed && downX <= edgeWidthPx
