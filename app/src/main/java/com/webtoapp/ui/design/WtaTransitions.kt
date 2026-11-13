package com.webtoapp.ui.design

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment

val WtaFadeIn: EnterTransition = fadeIn(
    animationSpec = WtaMotion.enterTween(WtaMotion.DurationMedium)
)

val WtaFadeOut: ExitTransition = fadeOut(
    animationSpec = WtaMotion.exitTween(WtaMotion.DurationQuick)
)

val WtaExpandIn: EnterTransition = expandVertically(
    animationSpec = WtaMotion.settleSpring(),
    expandFrom = Alignment.Top,
    clip = true
) + WtaFadeIn

val WtaShrinkOut: ExitTransition = shrinkVertically(
    animationSpec = WtaMotion.snapSpring(),
    shrinkTowards = Alignment.Top,
    clip = true
) + WtaFadeOut

val WtaPopIn: EnterTransition = scaleIn(
    initialScale = 0.92f,
    animationSpec = WtaMotion.pressSpring()
) + WtaFadeIn

val WtaPopOut: ExitTransition = scaleOut(
    targetScale = 0.95f,
    animationSpec = WtaMotion.snapSpring()
) + WtaFadeOut

val WtaSlideUpIn: EnterTransition = slideInVertically(
    initialOffsetY = { it / 6 },
    animationSpec = WtaMotion.settleSpring()
) + WtaFadeIn

val WtaSlideDownOut: ExitTransition = slideOutVertically(
    targetOffsetY = { it / 8 },
    animationSpec = WtaMotion.snapSpring()
) + WtaFadeOut

fun wtaTabEnterTransition(direction: Int): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> direction * (fullWidth / 8) },
        animationSpec = WtaMotion.settleSpring()
    ) + WtaFadeIn

fun wtaTabExitTransition(direction: Int): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -direction * (fullWidth / 10) },
        animationSpec = WtaMotion.snapSpring()
    ) + WtaFadeOut