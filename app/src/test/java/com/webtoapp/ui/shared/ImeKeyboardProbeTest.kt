package com.webtoapp.ui.shared

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ImeKeyboardProbeTest {

    @Test
    fun `a page with nothing above the keyboard has to give up the keyboard height`() {
        val probe = parseImeProbe("\"800,800,0,700,760\"")!!

        assertThat(needsKeyboardResize(probe, webViewHeightPx = 800, imeBottomPx = 300, restCssPerPx = null))
            .isTrue()
        assertThat(fieldBehindKeyboard(probe, webViewHeightPx = 800, imeBottomPx = 300)).isTrue()
    }

    @Test
    fun `a page that already insets its visual viewport is left alone`() {
        val probe = parseImeProbe("\"800,500,0,420,480\"")!!

        assertThat(needsKeyboardResize(probe, webViewHeightPx = 800, imeBottomPx = 300, restCssPerPx = null))
            .isFalse()
    }

    @Test
    fun `a page that resized its own layout viewport is left alone once the rest scale is known`() {
        val probe = parseImeProbe("\"500,500,0,420,460\"")!!
        val restScale = 800f / 800

        assertThat(needsKeyboardResize(probe, webViewHeightPx = 800, imeBottomPx = 300, restCssPerPx = restScale))
            .isFalse()
        assertThat(needsKeyboardResize(probe, webViewHeightPx = 800, imeBottomPx = 300, restCssPerPx = null))
            .isTrue()
    }

    @Test
    fun `a field that already sits above the keyboard is not scrolled`() {
        val probe = parseImeProbe("\"800,800,0,120,190\"")!!

        assertThat(fieldBehindKeyboard(probe, webViewHeightPx = 800, imeBottomPx = 300)).isFalse()
        assertThat(keyboardBandBottomCss(probe, webViewHeightPx = 800, imeBottomPx = 300)).isEqualTo(500f)
    }

    @Test
    fun `a field scrolled past the top is brought back`() {
        val probe = parseImeProbe("\"800,800,0,-40,40\"")!!

        assertThat(fieldBehindKeyboard(probe, webViewHeightPx = 800, imeBottomPx = 300)).isTrue()
    }

    @Test
    fun `the band follows the physical keyboard height`() {
        val probe = parseImeProbe("\"800,800,0,700,760\"")!!

        assertThat(keyboardBandBottomCss(probe, webViewHeightPx = 800, imeBottomPx = 400)).isEqualTo(400f)
        assertThat(fieldBehindKeyboard(probe, webViewHeightPx = 800, imeBottomPx = 400)).isTrue()
        assertThat(fieldBehindKeyboard(probe, webViewHeightPx = 0, imeBottomPx = 400)).isFalse()
    }

    @Test
    fun `a page without a focused field never scrolls on its own`() {
        val probe = parseImeProbe("\"800,800,0,-1,-1\"")!!

        assertThat(probe.fieldTopCss).isNull()
        assertThat(probe.fieldBottomCss).isNull()
        assertThat(fieldBehindKeyboard(probe, webViewHeightPx = 800, imeBottomPx = 300)).isFalse()
        assertThat(needsKeyboardResize(probe, webViewHeightPx = 800, imeBottomPx = 300, restCssPerPx = null))
            .isTrue()
    }

    @Test
    fun `the probe parser accepts the quoted javascript callback`() {
        assertThat(parseImeProbe("\"800,760,0,120.5,190.25\""))
            .isEqualTo(ImeProbe(800f, 760f, 0f, 120.5f, 190.25f))
        assertThat(parseImeProbe("\"\"")).isNull()
        assertThat(parseImeProbe("null")).isNull()
        assertThat(parseImeProbe("800,760")).isNull()
        assertThat(parseImeProbe("\"0,0,0,-1,-1\"")).isNull()
    }
}
