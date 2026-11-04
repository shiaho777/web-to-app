package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ImeAnimationFollowTest {

    @Test
    fun `opening clips and scrolls and the end frame does not resize`() {
        val follow = ImeAnimationFollow()
        follow.onPrepare()
        follow.onTarget(200)

        assertThat(follow.onFrame(0)).isEqualTo(ImeFrame(0, scrollDocument = true, clipBottom = 0))
        assertThat(follow.onFrame(100)).isEqualTo(ImeFrame(0, scrollDocument = true, clipBottom = 100))
        assertThat(follow.onFrame(200)).isEqualTo(ImeFrame(0, scrollDocument = true, clipBottom = 200))
        assertThat(follow.onEnd(200)).isEqualTo(ImeFrame(0, scrollDocument = false, clipBottom = 200))
        assertThat(follow.settledBottom).isEqualTo(200)
        assertThat(follow.settledPadding).isEqualTo(0)
    }

    @Test
    fun `closing follows the keyboard with the clip and never resizes`() {
        val follow = ImeAnimationFollow()
        assertThat(follow.jumpTo(200).clipBottom).isEqualTo(200)

        follow.onPrepare()
        follow.onTarget(0)
        assertThat(follow.onFrame(200)).isEqualTo(ImeFrame(0, scrollDocument = false, clipBottom = 200))
        assertThat(follow.onFrame(150)).isEqualTo(ImeFrame(0, scrollDocument = false, clipBottom = 150))
        assertThat(follow.onFrame(40)).isEqualTo(ImeFrame(0, scrollDocument = false, clipBottom = 40))
        assertThat(follow.onEnd(0)).isEqualTo(ImeFrame(0, scrollDocument = false, clipBottom = 0))
        assertThat(follow.settledBottom).isEqualTo(0)
    }

    @Test
    fun `a taller keyboard keeps scrolling and settles on the new clip`() {
        val follow = ImeAnimationFollow()
        follow.jumpTo(200)
        follow.onPrepare()
        follow.onTarget(280)

        val frame = follow.onFrame(240)
        assertThat(frame.layoutBottom).isEqualTo(0)
        assertThat(frame.scrollDocument).isTrue()
        assertThat(frame.clipBottom).isEqualTo(240)
        assertThat(follow.onEnd(280)).isEqualTo(ImeFrame(0, scrollDocument = false, clipBottom = 280))
    }

    @Test
    fun `a page that needs the keyboard height keeps it until the keyboard closes`() {
        val follow = ImeAnimationFollow()
        follow.onPrepare()
        follow.onTarget(300)
        follow.onEnd(300)

        val resized = follow.applyPadding(300)
        assertThat(resized).isEqualTo(ImeFrame(300, scrollDocument = false, clipBottom = 300))
        assertThat(follow.settledPadding).isEqualTo(300)

        follow.onPrepare()
        follow.onTarget(320)
        assertThat(follow.onFrame(310).layoutBottom).isEqualTo(300)
        assertThat(follow.onEnd(320).layoutBottom).isEqualTo(300)

        assertThat(follow.applyPadding(320).layoutBottom).isEqualTo(320)
    }

    @Test
    fun `switching to a shorter keyboard keeps the page height until it settles`() {
        val follow = ImeAnimationFollow()
        follow.onPrepare()
        follow.onTarget(300)
        follow.onEnd(300)
        follow.applyPadding(300)

        follow.onPrepare()
        follow.onTarget(260)

        assertThat(follow.onFrame(280).layoutBottom).isEqualTo(300)
        assertThat(follow.onFrame(260).layoutBottom).isEqualTo(300)
        assertThat(follow.onEnd(260).layoutBottom).isEqualTo(300)
        assertThat(follow.settledPadding).isEqualTo(300)
        assertThat(follow.applyPadding(260).layoutBottom).isEqualTo(260)
    }

    @Test
    fun `closing gives the keyboard height back before the keyboard uncovers it`() {
        val follow = ImeAnimationFollow()
        follow.onPrepare()
        follow.onTarget(300)
        follow.onEnd(300)
        follow.applyPadding(300)

        follow.onPrepare()
        follow.onTarget(0)
        assertThat(follow.settledPadding).isEqualTo(0)
        assertThat(follow.onFrame(300)).isEqualTo(ImeFrame(0, scrollDocument = false, clipBottom = 300))
        assertThat(follow.onEnd(0)).isEqualTo(ImeFrame(0, scrollDocument = false, clipBottom = 0))
    }
}
