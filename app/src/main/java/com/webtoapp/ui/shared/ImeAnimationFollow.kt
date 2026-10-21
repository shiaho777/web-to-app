package com.webtoapp.ui.shared

/**
 * Follows the keyboard without resizing the page on every animation frame.
 *
 * Opening pans the already-laid-out page with [ImeFrame.translationY]. Closing
 * expands the page once, then lets the keyboard uncover it. The final height
 * is applied when the animation ends.
 */
internal class ImeAnimationFollow {
    var laidOutBottom: Int = 0
        private set

    private var animating = false
    private var targetBottom = 0
    private var expandedForClose = false

    fun onPrepare() {
        animating = true
        targetBottom = laidOutBottom
        expandedForClose = false
    }

    /** Final inset reported while an animation is running. */
    fun onTarget(bottom: Int) {
        if (!animating) return
        targetBottom = bottom.coerceAtLeast(0)
    }

    fun onFrame(shown: Int): ImeFrame {
        val visible = shown.coerceAtLeast(0)
        if (expandedForClose) return ImeFrame(translationY = 0f, layoutBottom = null)
        val closing = targetBottom < laidOutBottom ||
            (targetBottom == laidOutBottom && visible < laidOutBottom)
        if (closing && visible < laidOutBottom) {
            expandedForClose = true
            laidOutBottom = 0
            return ImeFrame(translationY = 0f, layoutBottom = 0)
        }
        if (closing) return ImeFrame(translationY = 0f, layoutBottom = null)
        return ImeFrame(translationY = (laidOutBottom - visible).toFloat(), layoutBottom = null)
    }

    fun onEnd(finalBottom: Int): ImeFrame = settle(finalBottom)

    fun jumpTo(bottom: Int): ImeFrame = settle(bottom)

    private fun settle(bottom: Int): ImeFrame {
        animating = false
        expandedForClose = false
        val settled = bottom.coerceAtLeast(0)
        laidOutBottom = settled
        targetBottom = settled
        return ImeFrame(translationY = 0f, layoutBottom = settled)
    }
}

internal data class ImeFrame(
    val translationY: Float,
    val layoutBottom: Int?
)
