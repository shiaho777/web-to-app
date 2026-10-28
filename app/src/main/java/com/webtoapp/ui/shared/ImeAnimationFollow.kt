package com.webtoapp.ui.shared

/**
 * Decides how the page follows a keyboard animation.
 *
 * The WebView must not be resized or translated. A translation (#1343) slides
 * fixed elements behind the status bar (#1398). A height change when the
 * animation ends shrinks the layout viewport while Chromium still applies the
 * window IME inset to the visual viewport, so the focused field — already
 * sitting on the keyboard — is inset a second time and flashes.
 *
 * Opening and closing only clip to the keyboard and, while opening, scroll
 * the document by the covered amount. The last animation frame is the
 * settled frame.
 */
internal class ImeAnimationFollow {
    /** Keyboard overlap the clip settled on. Not a view padding. */
    var settledBottom: Int = 0
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
        targetBottom = bottom.coerceAtLeast(0)
    }

    fun onFrame(shown: Int): ImeFrame {
        val visible = shown.coerceAtLeast(0)
        val closing = targetBottom < settledBottom ||
            (targetBottom == settledBottom && visible < settledBottom)
        return if (closing) {
            ImeFrame(translationY = 0f, layoutBottom = null, scrollDocument = false, clipBottom = visible)
        } else {
            ImeFrame(translationY = 0f, layoutBottom = null, scrollDocument = true, clipBottom = visible)
        }
    }

    fun onEnd(finalBottom: Int): ImeFrame = settle(finalBottom)

    fun jumpTo(bottom: Int): ImeFrame = settle(bottom)

    private fun settle(bottom: Int): ImeFrame {
        animating = false
        val settled = bottom.coerceAtLeast(0)
        settledBottom = settled
        targetBottom = settled
        return ImeFrame(translationY = 0f, layoutBottom = null, scrollDocument = false, clipBottom = settled)
    }
}

internal data class ImeFrame(
    val translationY: Float,
    val layoutBottom: Int?,
    val scrollDocument: Boolean = false,
    /** Pixels of the content bottom the keyboard covers. 0 clears the clip. */
    val clipBottom: Int? = null,
)
