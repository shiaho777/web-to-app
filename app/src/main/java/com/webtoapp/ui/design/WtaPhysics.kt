package com.webtoapp.ui.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.webtoapp.ui.theme.LocalAnimationSettings
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

fun rubberBand(offset: Float, maxStretch: Float = 400f): Float {
    if (offset == 0f) return 0f
    val absOffset = abs(offset)
    val resistance = 1f - 1f / (absOffset / maxStretch + 1f)
    return resistance * maxStretch * sign(offset)
}

@Composable
fun rememberInterruptibleAnimatable(initial: Float = 0f): Animatable<Float, AnimationVector1D> {
    return remember { Animatable(initial, Float.VectorConverter) }
}

@Composable
fun Modifier.wtaDragToDismissVertical(
    threshold: Float = 180f,
    maxStretch: Float = 400f,
    onDismiss: () -> Unit
): Modifier = composed {
    val offsetY = rememberInterruptibleAnimatable(0f)
    val scope = rememberCoroutineScope()
    val animSettings = LocalAnimationSettings.current

    this
        .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    val current = offsetY.value
                    scope.launch {
                        if (abs(current) > threshold) {
                            onDismiss()
                        } else {
                            offsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = WtaMotion.bouncySpring(),
                                initialVelocity = 0f
                            )
                        }
                    }
                },
                onDragCancel = {
                    scope.launch {
                        offsetY.animateTo(0f, WtaMotion.bouncySpring())
                    }
                }
            ) { change, dragAmount ->
                change.consume()
                val next = offsetY.value + dragAmount.y
                scope.launch {
                    offsetY.snapTo(rubberBand(next, maxStretch))
                }
            }
        }
        .graphicsLayer {
            if (animSettings.enabled) {
                translationY = offsetY.value
            }
        }
}

@Composable
private fun rememberCoroutineScope(): kotlinx.coroutines.CoroutineScope {
    return androidx.compose.runtime.rememberCoroutineScope()
}
