package com.webtoapp.ui.shared

/**
 * Decides how the page follows a keyboard animation.
 *
 * The WebView is never translated or resized while the keyboard moves. A
 * translation (#1343) slides `position: fixed` elements behind the status bar
 * (#1398), and a height change on every frame relayouts the page for the whole
 * animation (#1343).
 *
 * Opening and closing follow the keyboard by clipping to its top edge and, while
 * opening, scrolling only the covered part of the document. The layout height
 * settles once, at the end of the animation, and only when a page cannot reveal
 * the focused field without it (#1473) — see [ImeKeyboardProbe].
 */
internal class ImeAnimationFollow {
    /** Keyboard overlap the clip settled on. Not a view padding. */
    var settledBottom: Int = 0
        private set

    /** Bottom padding the content view settled on. Zero while the keyboard is closed. */
    var settledPadding: Int = 0
        private set

    private var animating = false
    private var targetBottom = 0

    fun onPrepare() {
        animating = true
        targetBottom = settledBottom
    }

    /** Final inset reported while an animation is running. */
    fun onTarget(bottom: Int) {
        if (!animating) return
        val target = bottom.coerceAtLeast(0)
        if (target == 0) settledPadding = 0
        targetBottom = target
    }

    fun onFrame(shown: Int): ImeFrame {
        val visible = shown.coerceAtLeast(0)
        val closing = targetBottom < settledBottom ||
            (targetBottom == settledBottom && visible < settledBottom)
        return ImeFrame(
            layoutBottom = settledPadding,
            scrollDocument = !closing,
            clipBottom = visible,
        )
    }

    fun onFrameResize(shown: Int): ImeFrame {
        val visible = shown.coerceAtLeast(0)
        return ImeFrame(
            layoutBottom = visible,
            scrollDocument = false,
            clipBottom = 0,
        )
    }

    fun onEndResize(finalBottom: Int): ImeFrame {
        animating = false
        val settled = finalBottom.coerceAtLeast(0)
        settledBottom = settled
        settledPadding = settled
        targetBottom = settled
        return ImeFrame(layoutBottom = settled, scrollDocument = false, clipBottom = 0)
    }

    fun onEnd(finalBottom: Int): ImeFrame = settle(finalBottom)

    fun jumpTo(bottom: Int): ImeFrame = settle(bottom)

    /** Applies the page height a keyboard needs. The clip stays where it settled. */
    fun applyPadding(bottom: Int): ImeFrame {
        settledPadding = bottom.coerceAtLeast(0)
        return ImeFrame(layoutBottom = settledPadding, clipBottom = settledBottom)
    }

    private fun settle(bottom: Int): ImeFrame {
        animating = false
        val settled = bottom.coerceAtLeast(0)
        settledBottom = settled
        targetBottom = settled
        if (settled == 0) settledPadding = 0
        return ImeFrame(layoutBottom = settledPadding, clipBottom = settled)
    }
}

internal data class ImeFrame(
    /** Bottom padding of the content view. The page is full height at 0. */
    val layoutBottom: Int,
    val scrollDocument: Boolean = false,
    /** Pixels of the content bottom the keyboard covers. 0 clears the clip. */
    val clipBottom: Int = 0,
)
