package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PageZoomHostTest {

    @Test
    fun `html embeds the target url and an iframe`() {
        val html = PageZoomHost.html("https://example.com/a?b=1", 70)
        assertThat(html).contains("https://example.com/a?b=1")
        assertThat(html).contains("wta-page-frame")
        assertThat(html).contains("WtaIframeHost")
        assertThat(html).contains("0.700000")
    }

    @Test
    fun `html keeps zoom in percent`() {
        val zoomIn = PageZoomHost.html("https://example.com/", 125)
        val zoomOut = PageZoomHost.html("https://example.com/", 50)
        assertThat(zoomIn).contains("1.250000")
        assertThat(zoomOut).contains("0.500000")
    }

    @Test
    fun `html accepts a target url containing a quote`() {
        val html = PageZoomHost.html("https://example.com/a'b", 70)
        assertThat(html).contains("https://example.com/a'b")
    }

    @Test
    fun `frame box covers the viewport without rounding down`() {
        val html = PageZoomHost.html("https://example.com/", 150)
        assertThat(html).contains("el.clientWidth")
        assertThat(html).contains("el.clientHeight")
        assertThat(html).contains("Math.ceil(boxed.w/ZOOM)+1")
        assertThat(html).contains("Math.ceil(boxed.h/ZOOM)+1")
        assertThat(html).doesNotContain("Math.round(window.innerWidth/ZOOM)")
        assertThat(html).doesNotContain("Math.round(window.innerHeight/ZOOM)")
    }
}
