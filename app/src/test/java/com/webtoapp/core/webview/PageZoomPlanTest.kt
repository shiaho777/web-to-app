package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PageZoomPlanTest {

    @Test
    fun `zoom out activates whole-page scale`() {
        val plan = planPageZoom(pageZoomPercent = 75, initialScaleField = 0)
        assertThat(plan.zoomActive).isTrue()
        assertThat(plan.initialScalePercent).isEqualTo(75)
    }

    @Test
    fun `zoom in activates whole-page scale`() {
        val plan = planPageZoom(pageZoomPercent = 125, initialScaleField = 0)
        assertThat(plan.zoomActive).isTrue()
        assertThat(plan.initialScalePercent).isEqualTo(125)
    }

    @Test
    fun `default leaves everything untouched`() {
        val plan = planPageZoom(pageZoomPercent = 100, initialScaleField = 0)
        assertThat(plan.zoomActive).isFalse()
        assertThat(plan.initialScalePercent).isEqualTo(0)
    }

    @Test
    fun `legacy zero treated as default`() {
        val plan = planPageZoom(pageZoomPercent = 0, initialScaleField = 0)
        assertThat(plan.zoomActive).isFalse()
        assertThat(plan.initialScalePercent).isEqualTo(0)
    }

    @Test
    fun `explicit zoom wins over dormant initialScale field`() {
        val plan = planPageZoom(pageZoomPercent = 75, initialScaleField = 80)
        assertThat(plan.zoomActive).isTrue()
        assertThat(plan.initialScalePercent).isEqualTo(75)
    }

    @Test
    fun `dormant initialScale field preserved on default path`() {
        val plan = planPageZoom(pageZoomPercent = 100, initialScaleField = 80)
        assertThat(plan.zoomActive).isFalse()
        assertThat(plan.initialScalePercent).isEqualTo(80)
    }
}
