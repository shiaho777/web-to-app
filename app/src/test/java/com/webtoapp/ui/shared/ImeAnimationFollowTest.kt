package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ImeAnimationFollowTest {

    @Test
    fun `opening pans and resizes once at the end`() {
        val follow = ImeAnimationFollow()
        follow.onPrepare()
        follow.onTarget(200)

        assertThat(follow.onFrame(0)).isEqualTo(ImeFrame(0f, null))
        assertThat(follow.onFrame(100)).isEqualTo(ImeFrame(-100f, null))
        assertThat(follow.onFrame(200)).isEqualTo(ImeFrame(-200f, null))
        assertThat(follow.onEnd(200)).isEqualTo(ImeFrame(0f, 200))
        assertThat(follow.laidOutBottom).isEqualTo(200)
    }

    @Test
    fun `closing expands once then uncovers without further resizes`() {
        val follow = ImeAnimationFollow()
        assertThat(follow.jumpTo(200).layoutBottom).isEqualTo(200)

        follow.onPrepare()
        follow.onTarget(0)
        assertThat(follow.onFrame(200)).isEqualTo(ImeFrame(0f, null))
        assertThat(follow.onFrame(150)).isEqualTo(ImeFrame(0f, 0))
        assertThat(follow.onFrame(40)).isEqualTo(ImeFrame(0f, null))
        assertThat(follow.onEnd(0)).isEqualTo(ImeFrame(0f, 0))
    }

    @Test
    fun `a taller keyboard pans the extra height and settles once`() {
        val follow = ImeAnimationFollow()
        follow.jumpTo(200)
        follow.onPrepare()
        follow.onTarget(280)

        assertThat(follow.onFrame(240).translationY).isEqualTo(-40f)
        assertThat(follow.onFrame(240).layoutBottom).isNull()
        assertThat(follow.onEnd(280)).isEqualTo(ImeFrame(0f, 280))
    }
}
