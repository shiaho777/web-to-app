package com.webtoapp.ui.design

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object WtaMotion {

    val StandardEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    val EnterEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    val ExitEasing: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    const val DurationQuick: Int = 100

    const val DurationMedium: Int = 200

    const val DurationSlow: Int = 300

    const val DurationDeliberate: Int = 400

    /**
     * Global motion tuning, written from WebToAppTheme's composition.
     * Spec factories below read these at call time, so a settings change
     * applies to every animation started afterwards.
     */
    @Volatile
    var durationScale: Float = 1f

    @Volatile
    var motionEnabled: Boolean = true

    private fun scaledDuration(durationMillis: Int): Int =
        (durationMillis * durationScale).toInt().coerceAtLeast(1)

    private fun scaledStiffness(stiffness: Float): Float =
        stiffness / durationScale.coerceAtLeast(0.1f)

    private fun <T> instantSpring(): SpringSpec<T> =
        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 20000f)

    fun <T> pressSpring(): SpringSpec<T> =
        if (!motionEnabled) instantSpring() else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = scaledStiffness(1200f)
        )

    fun <T> settleSpring(): SpringSpec<T> =
        if (!motionEnabled) instantSpring() else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = scaledStiffness(700f)
        )

    fun <T> snapSpring(): SpringSpec<T> =
        if (!motionEnabled) instantSpring() else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = scaledStiffness(900f)
        )

    fun <T> bouncySpring(): SpringSpec<T> =
        if (!motionEnabled) instantSpring() else spring(
            dampingRatio = 0.85f,
            stiffness = scaledStiffness(500f)
        )

    fun <T> gentleSpring(): SpringSpec<T> =
        if (!motionEnabled) instantSpring() else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = scaledStiffness(400f)
        )

    fun <T> standardTween(
        durationMillis: Int = DurationMedium,
        delayMillis: Int = 0
    ): TweenSpec<T> = tween(
        durationMillis = if (motionEnabled) scaledDuration(durationMillis) else 1,
        delayMillis = delayMillis,
        easing = StandardEasing
    )

    fun <T> enterTween(
        durationMillis: Int = DurationMedium,
        delayMillis: Int = 0
    ): TweenSpec<T> = tween(
        durationMillis = if (motionEnabled) scaledDuration(durationMillis) else 1,
        delayMillis = delayMillis,
        easing = EnterEasing
    )

    fun <T> exitTween(
        durationMillis: Int = DurationQuick,
        delayMillis: Int = 0
    ): TweenSpec<T> = tween(
        durationMillis = if (motionEnabled) scaledDuration(durationMillis) else 1,
        delayMillis = delayMillis,
        easing = ExitEasing
    )
}

fun <T> wtaDefaultAnimation(): FiniteAnimationSpec<T> = WtaMotion.standardTween()
