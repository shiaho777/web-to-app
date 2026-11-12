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
    fun `document script scales the viewport instead of the painted root`() {
        val script = pageZoomDocumentScript(pageZoomPercent = 75)
        assertThat(script).isNotNull()
        assertThat(script!!).contains("var PCT=75")
        assertThat(script).contains("initial-scale=")
        assertThat(script).contains("MutationObserver")
        assertThat(script).contains("window.top")
        assertThat(script).contains("document.readyState==='loading'")
        assertThat(script).doesNotContain("style.zoom")
        assertThat(script).doesNotContain("innerWidth")
        assertThat(pageZoomDocumentScript(pageZoomPercent = 100)).isNull()
        assertThat(pageZoomDocumentScript(pageZoomPercent = 0)).isNull()
    }

    @Test
    fun `script patches the viewport the moment it appears during parsing`() {
        val script = pageZoomDocumentScript(pageZoomPercent = 70)!!
        assertThat(script).contains("document.documentElement||document")
        assertThat(script).contains("childList:true,subtree:true")
        assertThat(script).contains("maximum-scale=")
        assertThat(script).contains("minimum-scale=")
    }

    @Test
    fun `script compares the parsed scale instead of matching a substring`() {
        val script = pageZoomDocumentScript(pageZoomPercent = 70)!!
        assertThat(script).contains("scaleOf(el)===Z")
        assertThat(script).doesNotContain("indexOf('initial-scale='")
    }

    @Test
    fun `script bounds meta rewrites with a reload-surviving budget`() {
        val script = pageZoomDocumentScript(pageZoomPercent = 70)!!
        assertThat(script).contains("sessionStorage")
        assertThat(script).contains("__wtaPageZoomBudget")
        assertThat(script).contains("if(n>=8)")
        assertThat(script).contains("disconnect()")
        assertThat(script).contains("if(!budgetOk())return;")
    }
}
