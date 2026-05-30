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

    val StandardEasing: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)

    val EnterEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

    val ExitEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

    val EmphasizedEasing: Easing = CubicBezierEasing(0.175f, 0.885f, 0.32f, 1.275f)

    const val DurationQuick: Int = 120

    const val DurationMedium: Int = 220

    const val DurationSlow: Int = 340

    const val DurationDeliberate: Int = 480

    fun <T> pressSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.6f,
        stiffness = 800f
    )

    fun <T> settleSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.78f,
        stiffness = 400f
    )

    fun <T> snapSpring(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 600f
    )

    fun <T> bouncySpring(): SpringSpec<T> = spring(
        dampingRatio = 0.55f,
        stiffness = 320f
    )

    fun <T> gentleSpring(): SpringSpec<T> = spring(
        dampingRatio = 0.82f,
        stiffness = 200f
    )

    fun <T> standardTween(
        durationMillis: Int = DurationMedium,
        delayMillis: Int = 0
    ): TweenSpec<T> = tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = StandardEasing
    )

    fun <T> enterTween(
        durationMillis: Int = DurationMedium,
        delayMillis: Int = 0
    ): TweenSpec<T> = tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = EnterEasing
    )

    fun <T> exitTween(
        durationMillis: Int = DurationQuick,
        delayMillis: Int = 0
    ): TweenSpec<T> = tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = ExitEasing
    )
}

fun <T> wtaDefaultAnimation(): FiniteAnimationSpec<T> = WtaMotion.settleSpring()
