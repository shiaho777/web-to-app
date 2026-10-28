package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * The WebView is never resized for the keyboard. Opening clips to the inset
 * and scrolls the document; the end frame keeps that clip and does not change
 * the layout height, so the focused field is not painted twice (#1398).
 */
class ImeAnimationFollowTest {

    @Test
    fun `opening clips and scrolls and the end frame does not resize`() {
        val follow = ImeAnimationFollow()
        follow.onPrepare()
        follow.onTarget(200)

        assertThat(follow.onFrame(0)).isEqualTo(ImeFrame(0f, null, scrollDocument = true, clipBottom = 0))
        assertThat(follow.onFrame(100)).isEqualTo(ImeFrame(0f, null, scrollDocument = true, clipBottom = 100))
        assertThat(follow.onFrame(200)).isEqualTo(ImeFrame(0f, null, scrollDocument = true, clipBottom = 200))
        assertThat(follow.onEnd(200)).isEqualTo(ImeFrame(0f, null, scrollDocument = false, clipBottom = 200))
        assertThat(follow.settledBottom).isEqualTo(200)
    }

    @Test
    fun `closing follows the keyboard with the clip and never resizes`() {
        val follow = ImeAnimationFollow()
        assertThat(follow.jumpTo(200).clipBottom).isEqualTo(200)

        follow.onPrepare()
        follow.onTarget(0)
        assertThat(follow.onFrame(200)).isEqualTo(ImeFrame(0f, null, scrollDocument = false, clipBottom = 200))
        assertThat(follow.onFrame(150)).isEqualTo(ImeFrame(0f, null, scrollDocument = false, clipBottom = 150))
        assertThat(follow.onFrame(40)).isEqualTo(ImeFrame(0f, null, scrollDocument = false, clipBottom = 40))
        assertThat(follow.onEnd(0)).isEqualTo(ImeFrame(0f, null, scrollDocument = false, clipBottom = 0))
        assertThat(follow.settledBottom).isEqualTo(0)
    }

    @Test
    fun `a taller keyboard keeps scrolling and settles on the new clip`() {
        val follow = ImeAnimationFollow()
        follow.jumpTo(200)
        follow.onPrepare()
        follow.onTarget(280)

        val frame = follow.onFrame(240)
        assertThat(frame.translationY).isEqualTo(0f)
        assertThat(frame.layoutBottom).isNull()
        assertThat(frame.scrollDocument).isTrue()
        assertThat(frame.clipBottom).isEqualTo(240)
        assertThat(follow.onEnd(280)).isEqualTo(ImeFrame(0f, null, scrollDocument = false, clipBottom = 280))
    }
}
