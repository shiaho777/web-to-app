package com.webtoapp.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

private fun <T> navSpringIn(): androidx.compose.animation.core.SpringSpec<T> = spring(
    dampingRatio = 0.82f,
    stiffness = 380f,
    visibilityThreshold = null
)

private fun <T> navSpringOut(): androidx.compose.animation.core.SpringSpec<T> = spring(
    dampingRatio = 0.88f,
    stiffness = 320f,
    visibilityThreshold = null
)

val pageEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = navSpringIn()
    )
}

val pageExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = navSpringOut()
    )
}

val pagePopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = navSpringOut()
    )
}

val pagePopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = navSpringIn()
    )
}
