package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class VideoElementFitTest {

    private fun input(
        laidW: Int,
        laidH: Int,
        intrinsicW: Int = laidW,
        intrinsicH: Int = laidH,
        cssWidth: Int = 0,
        cssHeight: Int = 0,
        cssMaxWidth: Int = 0,
        cssMaxHeight: Int = 0,
        parentWidth: Int = 0,
        parentHeight: Int = 0,
        viewportWidth: Int = 400,
        fullscreen: Boolean = false,
    ) = VideoFitInput(
        laidOutWidth = laidW,
        laidOutHeight = laidH,
        intrinsicWidth = intrinsicW,
        intrinsicHeight = intrinsicH,
        cssWidth = cssWidth,
        cssHeight = cssHeight,
        cssMaxWidth = cssMaxWidth,
        cssMaxHeight = cssMaxHeight,
        parentWidth = parentWidth,
        parentHeight = parentHeight,
        viewportWidth = viewportWidth,
        fullscreen = fullscreen,
    )

    @Test
    fun `portrait media pinned to the bubble box keeps that box`() {
        val box = fittedVideoBox(
            input(
                laidW = 1080,
                laidH = 1920,
                intrinsicW = 1080,
                intrinsicH = 1920,
                parentWidth = 240,
                parentHeight = 240,
            )
        )
        assertThat(box).isEqualTo(VideoFitBox(240, 240))
    }

    @Test
    fun `width cap keeps the media aspect when height is free`() {
        val box = fittedVideoBox(
            input(
                laidW = 1080,
                laidH = 1920,
                intrinsicW = 1080,
                intrinsicH = 1920,
                cssMaxWidth = 320,
                viewportWidth = 400,
            )
        )
        assertThat(box).isEqualTo(VideoFitBox(320, 569))
    }

    @Test
    fun `height cap keeps the media aspect when width is only the screen`() {
        val box = fittedVideoBox(
            input(
                laidW = 1080,
                laidH = 1920,
                intrinsicW = 1080,
                intrinsicH = 1920,
                cssMaxHeight = 400,
                viewportWidth = 360,
            )
        )
        assertThat(box).isEqualTo(VideoFitBox(225, 400))
    }

    @Test
    fun `a player that already fits the column is left alone`() {
        assertThat(
            fittedVideoBox(
                input(
                    laidW = 360,
                    laidH = 202,
                    intrinsicW = 1920,
                    intrinsicH = 1080,
                    parentWidth = 360,
                    viewportWidth = 400,
                )
            )
        ).isNull()
    }

    @Test
    fun `fullscreen and missing media are left alone`() {
        assertThat(
            fittedVideoBox(
                input(
                    laidW = 1080,
                    laidH = 1920,
                    parentWidth = 240,
                    parentHeight = 240,
                    fullscreen = true,
                )
            )
        ).isNull()
        assertThat(
            fittedVideoBox(input(laidW = 1080, laidH = 1920, intrinsicW = 0, intrinsicH = 0))
        ).isNull()
    }

    @Test
    fun `script shrinks only overflowing videos and restores fullscreen`() {
        val script = VideoElementFit.SCRIPT
        assertThat(script).contains("window.__wtaVideoFit")
        assertThat(script).contains("loadedmetadata")
        assertThat(script).contains("webkitDisplayingFullscreen")
        assertThat(script).contains("fullscreenElement")
        assertThat(script).contains("data-wta-fit")
        assertThat(script).contains("min-width:0")
        assertThat(script).contains("video{min-width:0;min-height:0;}")
        assertThat(script).contains("insertBefore(style,root.firstChild)")
        assertThat(script).doesNotContain("width:100% !important")
        assertThat(script).doesNotContain("max-width:100%")
        assertThat(script).contains("if(!widthOverflow&&!heightOverflow)return")
        assertThat(script).contains("hasW=0")
        assertThat(script).contains("clientWidth")
        assertThat(script).contains("specifiedLength")
        assertThat(script).contains("cssRules")
        assertThat(script).contains("adoptedStyleSheets")
        assertThat(script).contains("(px|rem|em|vw|vh)")
        assertThat(script).contains("laidW-specW>=8")
        assertThat(script).contains("laidW-sw>=8")
    }
}
