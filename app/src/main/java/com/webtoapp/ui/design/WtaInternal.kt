package com.webtoapp.ui.design

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import com.webtoapp.ui.theme.LocalAnimationSettings

@Composable
fun rememberHapticClick(onClick: () -> Unit): () -> Unit {
    val view = LocalView.current
    val animSettings = LocalAnimationSettings.current
    return {
        if (animSettings.hapticsEnabled) {
            performHaptic(view)
        }
        onClick()
    }
}

internal fun performHaptic(view: View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    } else {
        @Suppress("DEPRECATION")
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}

internal fun performHeavyHaptic(view: View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    } else {
        @Suppress("DEPRECATION")
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

fun Modifier.wtaPressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f,
    hoveredScale: Float = 1.01f
): Modifier = composed {
    val animSettings = LocalAnimationSettings.current
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val targetScale = when {
        !animSettings.enabled -> 1f
        isPressed -> pressedScale
        isHovered -> hoveredScale
        else -> 1f
    }
    val targetAlpha = when {
        !animSettings.enabled -> 1f
        isPressed -> 0.92f
        else -> 1f
    }

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (isPressed) WtaMotion.pressSpring() else WtaMotion.bouncySpring(),
        label = "wtaPressScale"
    )
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = WtaMotion.standardTween(
            durationMillis = if (isPressed) 80 else WtaMotion.DurationMedium
        ),
        label = "wtaPressAlpha"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}
