package com.webtoapp.core.bgm

/** A missing switch on an older config stays on. */
internal fun bgmControlEnabled(flag: Boolean?): Boolean = flag != false

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
