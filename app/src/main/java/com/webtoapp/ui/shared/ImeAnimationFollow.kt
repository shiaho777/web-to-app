package com.webtoapp.ui.shared

/**
 * Decides when a keyboard animation may change the page height.
 *
 * The content view must not be translated. Translating it (#1343) slides the
 * whole WebView, including `position: fixed` elements, up behind the status
 * bar and then snaps it back when the height is applied (#1398).
 *
 * Opening leaves the laid-out height alone and reports [ImeFrame.scrollDocument]
 * so only the document scrolls, and only by the amount the focused field is
 * covered. Closing expands once, then lets the keyboard uncover the page.
 * The final height is applied when the animation ends.
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
        if (expandedForClose) return hold()
        val closing = targetBottom < laidOutBottom ||
            (targetBottom == laidOutBottom && visible < laidOutBottom)
        if (closing && visible < laidOutBottom) {
            expandedForClose = true
            laidOutBottom = 0
            return ImeFrame(translationY = 0f, layoutBottom = 0)
        }
        if (closing) return hold()
        return ImeFrame(translationY = 0f, layoutBottom = null, scrollDocument = true)
    }

    fun onEnd(finalBottom: Int): ImeFrame = settle(finalBottom)

    fun jumpTo(bottom: Int): ImeFrame = settle(bottom)

    private fun hold(): ImeFrame = ImeFrame(translationY = 0f, layoutBottom = null)

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
    val layoutBottom: Int?,
    val scrollDocument: Boolean = false,
)
