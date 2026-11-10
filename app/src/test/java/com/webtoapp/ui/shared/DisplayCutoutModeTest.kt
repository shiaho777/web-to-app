package com.webtoapp.ui.shared

import android.view.WindowManager
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.DisplayCutoutMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DisplayCutoutModeTest {

    @Test
    fun `short edges maps to the platform short edges constant`() {
        assertThat(cutoutModeFor(DisplayCutoutMode.SHORT_EDGES))
            .isEqualTo(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
    }

    @Test
    fun `default maps to the platform default constant`() {
        assertThat(cutoutModeFor(DisplayCutoutMode.DEFAULT))
            .isEqualTo(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT)
    }

    @Test
    fun `never maps to the platform never constant`() {
        assertThat(cutoutModeFor(DisplayCutoutMode.NEVER))
            .isEqualTo(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER)
    }

    @Test
    fun `all three modes produce distinct platform constants`() {
        val mapped = DisplayCutoutMode.entries.map(::cutoutModeFor)
        assertThat(mapped).hasSize(3)
        assertThat(mapped).containsNoDuplicates()
    }
}
