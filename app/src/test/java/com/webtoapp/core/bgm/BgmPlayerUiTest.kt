package com.webtoapp.core.bgm

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BgmPlayerUiTest {

    @Test
    fun `missing switch stays on and an explicit off stays off`() {
        assertThat(bgmControlEnabled(null)).isTrue()
        assertThat(bgmControlEnabled(true)).isTrue()
        assertThat(bgmControlEnabled(false)).isFalse()
    }

    @Test
    fun `chip docks to the nearer edge`() {
        assertThat(bgmDockX(currentX = 10f, chipWidth = 100f, parentWidth = 400f, margin = 12f)).isEqualTo(12f)
        assertThat(bgmDockX(currentX = 250f, chipWidth = 100f, parentWidth = 400f, margin = 12f)).isEqualTo(288f)
        assertThat(bgmDockX(currentX = 0f, chipWidth = 300f, parentWidth = 320f, margin = 12f)).isEqualTo(12f)
    }

    @Test
    fun `chip stays inside the vertical margins`() {
        assertThat(bgmClampY(currentY = -20f, chipHeight = 40f, parentHeight = 800f, margin = 12f)).isEqualTo(12f)
        assertThat(bgmClampY(currentY = 900f, chipHeight = 40f, parentHeight = 800f, margin = 12f)).isEqualTo(748f)
        assertThat(bgmClampY(currentY = 100f, chipHeight = 40f, parentHeight = 800f, margin = 12f)).isEqualTo(100f)
    }

    @Test
    fun `previous restarts the current track after three seconds`() {
        assertThat(bgmPreviousAction(3_001L)).isEqualTo(BgmPreviousAction.RESTART)
        assertThat(bgmPreviousAction(3_000L)).isEqualTo(BgmPreviousAction.TRACK)
        assertThat(bgmPreviousAction(0L)).isEqualTo(BgmPreviousAction.TRACK)
    }

    @Test
    fun `manual next wraps and previous steps back inside the list`() {
        assertThat(bgmManualNextIndex(2, 3)).isEqualTo(0)
        assertThat(bgmManualNextIndex(0, 3)).isEqualTo(1)
        assertThat(bgmPreviousLinearIndex(0, 3)).isEqualTo(2)
        assertThat(bgmPreviousLinearIndex(2, 3)).isEqualTo(1)
        assertThat(bgmManualNextIndex(0, 0)).isEqualTo(0)
        assertThat(bgmPreviousLinearIndex(0, 0)).isEqualTo(0)
    }
}
