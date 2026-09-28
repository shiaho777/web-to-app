package com.webtoapp.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.webtoapp.ui.design.WtaMotion
import kotlinx.coroutines.delay

@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    staggerDelayMs: Long = 16L,
    slideOffsetDp: Int = 8,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * staggerDelayMs)
        visible = true
    }

    val density = LocalDensity.current
    val slideOffsetPx = with(density) { slideOffsetDp.dp.roundToPx() }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { slideOffsetPx },
            animationSpec = WtaMotion.enterTween(durationMillis = 180)
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun Modifier.breathingFloat(
    floatAmountDp: Float = 3f,
    @Suppress("UNUSED_PARAMETER") rotationDegrees: Float = 0f,
    durationMs: Int = 4200
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val translationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = floatAmountDp,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    return this.graphicsLayer {
        this.translationY = -translationY * density
    }
}

@Composable
fun AnimatedDialogContent(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = if (visible) WtaMotion.settleSpring() else WtaMotion.snapSpring(),
        label = "dialogScale"
    )
    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}

val SnackbarEnterTransition: EnterTransition = slideInVertically(
    initialOffsetY = { it },
    animationSpec = WtaMotion.bouncySpring()
)

val SnackbarExitTransition: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = WtaMotion.settleSpring()
    )

fun tabSlideDirection(previousTab: Int, currentTab: Int): Int {
    return if (currentTab > previousTab) 1 else -1
}

fun tabEnterTransition(direction: Int): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> direction * (fullWidth / 8) },
        animationSpec = WtaMotion.enterTween(durationMillis = 200)
    )
}

fun tabExitTransition(direction: Int): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> -direction * (fullWidth / 12) },
        animationSpec = WtaMotion.exitTween(durationMillis = 160)
    )
}

val CardExpandTransition: EnterTransition = expandVertically(
    animationSpec = WtaMotion.settleSpring(),
    expandFrom = androidx.compose.ui.Alignment.Top,
    clip = true
) + fadeIn(animationSpec = WtaMotion.standardTween(WtaMotion.DurationMedium))

val CardCollapseTransition: ExitTransition = shrinkVertically(
    animationSpec = WtaMotion.snapSpring(),
    shrinkTowards = androidx.compose.ui.Alignment.Top,
    clip = true
) + fadeOut(animationSpec = WtaMotion.exitTween(WtaMotion.DurationQuick))

/**
 * Expand/collapse container that keeps [content] composed while collapsed.
 *
 * Unlike [AnimatedVisibility], collapsing never disposes the subtree, so
 * re-opening is a pure layout animation with zero recomposition cost — and the
 * first composition happens while the (zero-height) content is still invisible
 * instead of inside the click frame. Prefer this for large expandable sections
 * whose cold-open composition is expensive enough to drop frames (#1095).
 */
@Composable
fun WtaPersistentExpandedContent(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = if (expanded) WtaMotion.settleSpring() else WtaMotion.snapSpring(),
        label = "wtaExpandedProgress"
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .graphicsLayer { alpha = progress }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(
                    placeable.width,
                    (placeable.height * progress).roundToInt()
                ) {
                    placeable.place(0, 0)
                }
            },
        content = content
    )
}

data class RippleAnimState(
    val isActive: Boolean = false,
    val progress: Float = 0f
)

@Composable
fun rememberRippleAnimatable(): Animatable<Float, AnimationVector1D> {
    return remember { Animatable(0f) }
}
