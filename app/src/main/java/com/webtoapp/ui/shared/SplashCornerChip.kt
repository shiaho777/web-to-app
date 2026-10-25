package com.webtoapp.ui.shared

/**
 * The splash corner chip is the black pill behind the countdown and the skip
 * label. An empty pill is still visible, so it is drawn only when at least
 * one of those is actually shown.
 */
fun splashCornerChipVisible(
    showCountdown: Boolean,
    displayTimeSeconds: Int,
    skipAvailable: Boolean,
): Boolean = (showCountdown && displayTimeSeconds > 0) || skipAvailable
