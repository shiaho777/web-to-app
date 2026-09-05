package com.webtoapp.ui.shared

/**
 * Pure playback-advance decision shared by the host gallery player
 * ([com.webtoapp.ui.gallery.GalleryPlayerScreen]) and the shell player
 * ([com.webtoapp.ui.shell.ShellGalleryPlayer]) so both wrap — and reshuffle —
 * identically. Unit-tested on plain JVM.
 */
internal data class GalleryAdvance(
    val targetIndex: Int,
    /** True when the wrap must reshuffle the order first (shuffle-on-loop). */
    val reshuffle: Boolean
)

/**
 * @return next action, or null when playback should stop (end reached, no loop).
 */
internal fun galleryAutoAdvanceTarget(
    currentIndex: Int,
    itemCount: Int,
    loop: Boolean,
    shuffleOnLoop: Boolean
): GalleryAdvance? {
    if (itemCount <= 0) return null
    if (currentIndex < itemCount - 1) return GalleryAdvance(currentIndex + 1, reshuffle = false)
    if (!loop) return null
    return GalleryAdvance(targetIndex = 0, reshuffle = shuffleOnLoop)
}
