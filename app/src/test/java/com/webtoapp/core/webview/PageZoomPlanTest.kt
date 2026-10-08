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

    @Test
    fun `viewport widens when zooming out so the page lays out larger`() {
        val viewport = planPageZoomViewport(baseCssWidth = 800, pageZoomPercent = 75)
        assertThat(viewport).isNotNull()
        assertThat(viewport!!.layoutWidthPx).isEqualTo(1067)
        assertThat(viewport.scale).isWithin(0.001).of(800.0 / 1067.0)
    }

    @Test
    fun `viewport narrows when zooming in`() {
        val viewport = planPageZoomViewport(baseCssWidth = 800, pageZoomPercent = 125)
        assertThat(viewport).isNotNull()
        assertThat(viewport!!.layoutWidthPx).isEqualTo(640)
        assertThat(viewport.scale).isWithin(0.0001).of(1.25)
    }

    @Test
    fun `default zoom does not rewrite the viewport`() {
        assertThat(planPageZoomViewport(baseCssWidth = 800, pageZoomPercent = 100)).isNull()
        assertThat(planPageZoomViewport(baseCssWidth = 800, pageZoomPercent = 0)).isNull()
        assertThat(planPageZoomViewport(baseCssWidth = 0, pageZoomPercent = 75)).isNull()
    }

    @Test
    fun `document script locks a pixel width and survives a page meta rewrite`() {
        val script = pageZoomDocumentScript(pageZoomPercent = 75, pinchEnabled = true)
        assertThat(script).isNotNull()
        assertThat(script!!).contains("var PCT=75")
        assertThat(script).contains("var PINCH=1")
        assertThat(script).contains("var NATIVE=0")
        assertThat(script).contains("initial-scale=")
        assertThat(script).contains("MutationObserver")
        assertThat(script).contains("window.top")
        assertThat(script).doesNotContain("device-width")
        assertThat(script).doesNotContain("innerWidth")
        val measured = pageZoomDocumentScript(75, pinchEnabled = true, nativeCssWidth = 800)
        assertThat(measured).contains("var NATIVE=800")
        assertThat(pageZoomDocumentScript(pageZoomPercent = 100, pinchEnabled = true)).isNull()
        assertThat(pageZoomDocumentScript(pageZoomPercent = 0, pinchEnabled = false)).isNull()
    }

    @Test
    fun `pinch disabled locks user scaling`() {
        val script = pageZoomDocumentScript(pageZoomPercent = 125, pinchEnabled = false)
        assertThat(script).isNotNull()
        assertThat(script!!).contains("var PCT=125")
        assertThat(script).contains("var PINCH=0")
    }
}
