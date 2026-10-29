package com.webtoapp.core.bgm

/** A missing switch on an older config stays on. */
internal fun bgmControlEnabled(flag: Boolean?): Boolean = flag != false

/**
 * Keep the chip against one edge. The expanded bar is wider than the ball, so
 * the anchored edge must stay put when the width changes.
 */
internal fun bgmAnchoredX(
    anchorRight: Boolean,
    chipWidth: Float,
    parentWidth: Float,
    margin: Float
): Float {
    if (parentWidth <= chipWidth + margin * 2f) return margin
    return if (anchorRight) parentWidth - chipWidth - margin else margin
}

/** Snap the chip to the nearer horizontal edge. */
internal fun bgmDockX(
    currentX: Float,
    chipWidth: Float,
    parentWidth: Float,
    margin: Float
): Float {
    if (parentWidth <= chipWidth + margin * 2f) return margin
    val midpoint = currentX + chipWidth / 2f
    val right = parentWidth - chipWidth - margin
    return if (midpoint < parentWidth / 2f) margin else right
}

internal fun bgmClampY(
    currentY: Float,
    chipHeight: Float,
    parentHeight: Float,
    margin: Float
): Float {
    val max = (parentHeight - chipHeight - margin).coerceAtLeast(margin)
    return currentY.coerceIn(margin, max)
}
