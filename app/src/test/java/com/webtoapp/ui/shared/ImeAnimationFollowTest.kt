package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * The page height changes at most once per keyboard animation (#1342).
 * Opening must not pan the WebView: a translation moves fixed elements
 * behind the status bar and then snaps back (#1398).
 */
class ImeAnimationFollowTest {

    @Test
    fun `opening does not pan or resize until the animation ends`() {
        val follow = ImeAnimationFollow()
        follow.onPrepare()
        follow.onTarget(200)

        assertThat(follow.onFrame(0)).isEqualTo(ImeFrame(0f, null, scrollDocument = true))
        assertThat(follow.onFrame(100)).isEqualTo(ImeFrame(0f, null, scrollDocument = true))
        assertThat(follow.onFrame(200)).isEqualTo(ImeFrame(0f, null, scrollDocument = true))
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
    fun `a taller keyboard does not pan and settles once`() {
        val follow = ImeAnimationFollow()
        follow.jumpTo(200)
        follow.onPrepare()
        follow.onTarget(280)

        val frame = follow.onFrame(240)
        assertThat(frame.translationY).isEqualTo(0f)
        assertThat(frame.layoutBottom).isNull()
        assertThat(frame.scrollDocument).isTrue()
        assertThat(follow.onEnd(280)).isEqualTo(ImeFrame(0f, 280))
    }
}
