package com.webtoapp.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.webtoapp.ui.design.WtaMotion

private const val NAV_IN_MS = 220
private const val NAV_OUT_MS = 180

val pageEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(animationSpec = tween(NAV_IN_MS, easing = WtaMotion.EnterEasing)) +
        slideInHorizontally(
            initialOffsetX = { fullWidth -> (fullWidth * 0.08f).toInt() },
            animationSpec = tween(NAV_IN_MS, easing = WtaMotion.EnterEasing)
        )
}

val pageExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(animationSpec = tween(NAV_OUT_MS, easing = WtaMotion.ExitEasing)) +
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -(fullWidth * 0.04f).toInt() },
            animationSpec = tween(NAV_OUT_MS, easing = WtaMotion.ExitEasing)
        )
}

val pagePopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(animationSpec = tween(NAV_IN_MS, easing = WtaMotion.EnterEasing)) +
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -(fullWidth * 0.04f).toInt() },
            animationSpec = tween(NAV_IN_MS, easing = WtaMotion.EnterEasing)
        )
}

val pagePopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(animationSpec = tween(NAV_OUT_MS, easing = WtaMotion.ExitEasing)) +
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> (fullWidth * 0.08f).toInt() },
            animationSpec = tween(NAV_OUT_MS, easing = WtaMotion.ExitEasing)
        )
}
