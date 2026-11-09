package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PageZoomPlanTest {

    @Test
    fun `zooming out scales the surface instead of the css root`() {
        val plan = planPageZoom(pageZoomPercent = 75, initialScaleField = 0)
        assertThat(plan.zoomActive).isTrue()
        assertThat(plan.initialScalePercent).isEqualTo(75)
        assertThat(plan.viewScale).isWithin(0.0001f).of(0.75f)
        assertThat(plan.cssZoomActive).isFalse()
        assertThat(pageZoomDocumentScript(75, pinchEnabled = true)).isNull()
    }

    @Test
    fun `zooming out never leaves a viewport-sized box short of the window`() {
        val plan = planPageZoom(pageZoomPercent = 90, initialScaleField = 0)
        val layoutScale = plan.viewScale
        val cssViewportHeight = 1000f / layoutScale
        assertThat(cssViewportHeight * layoutScale).isWithin(0.01f).of(1000f)
    }

    @Test
    fun `zooming in keeps css zoom so text is re-laid out`() {
        val plan = planPageZoom(pageZoomPercent = 125, initialScaleField = 0)
        assertThat(plan.zoomActive).isTrue()
        assertThat(plan.initialScalePercent).isEqualTo(125)
        assertThat(plan.viewScale).isWithin(0.0001f).of(1f)
        assertThat(plan.cssZoomActive).isTrue()
        assertThat(pageZoomDocumentScript(125, pinchEnabled = true)).isNotNull()
    }

    @Test
    fun `default leaves everything untouched`() {
        val plan = planPageZoom(pageZoomPercent = 100, initialScaleField = 0)
        assertThat(plan.zoomActive).isFalse()
        assertThat(plan.initialScalePercent).isEqualTo(0)
        assertThat(plan.viewScale).isWithin(0.0001f).of(1f)
        assertThat(plan.cssZoomActive).isFalse()
    }

    @Test
    fun `legacy zero treated as default`() {
        val plan = planPageZoom(pageZoomPercent = 0, initialScaleField = 0)
        assertThat(plan.zoomActive).isFalse()
        assertThat(plan.initialScalePercent).isEqualTo(0)
        assertThat(plan.viewScale).isWithin(0.0001f).of(1f)
    }

    @Test
    fun `explicit zoom wins over dormant initialScale field`() {
        val plan = planPageZoom(pageZoomPercent = 75, initialScaleField = 80)
        assertThat(plan.zoomActive).isTrue()
        assertThat(plan.initialScalePercent).isEqualTo(75)
        assertThat(plan.viewScale).isWithin(0.0001f).of(0.75f)
    }

    @Test
    fun `dormant initialScale field preserved on default path`() {
        val plan = planPageZoom(pageZoomPercent = 100, initialScaleField = 80)
        assertThat(plan.zoomActive).isFalse()
        assertThat(plan.initialScalePercent).isEqualTo(80)
        assertThat(plan.viewScale).isWithin(0.0001f).of(1f)
    }

    @Test
    fun `document script zooms with css and does not rewrite the viewport meta`() {
        val script = pageZoomDocumentScript(pageZoomPercent = 125, pinchEnabled = true)
        assertThat(script).isNotNull()
        assertThat(script!!).contains("var PCT=125")
        assertThat(script).contains("style.zoom")
        assertThat(script).contains("MutationObserver")
        assertThat(script).contains("window.top")
        assertThat(script).doesNotContain("initial-scale=")
        assertThat(script).doesNotContain("viewport")
        assertThat(script).doesNotContain("device-width")
        assertThat(script).doesNotContain("innerWidth")
        val measured = pageZoomDocumentScript(125, pinchEnabled = false, nativeCssWidth = 800)
        assertThat(measured).contains("var PCT=125")
        assertThat(measured).doesNotContain("initial-scale=")
        assertThat(pageZoomDocumentScript(pageZoomPercent = 100, pinchEnabled = true)).isNull()
        assertThat(pageZoomDocumentScript(pageZoomPercent = 0, pinchEnabled = false)).isNull()
    }
}
