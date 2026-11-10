package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ToolbarRevealTest {

    @Test
    fun `starts visible`() {
        assertThat(ToolbarReveal().visible).isTrue()
    }

    @Test
    fun `hides once the climb passes the threshold`() {
        val reveal = ToolbarReveal()
        reveal.onScroll(12)
        assertThat(reveal.visible).isTrue()
        reveal.onScroll(40)
        assertThat(reveal.visible).isFalse()
    }

    @Test
    fun `shows as soon as the user scrolls back up`() {
        val reveal = ToolbarReveal()
        reveal.onScroll(60)
        assertThat(reveal.visible).isFalse()
        reveal.onScroll(50)
        assertThat(reveal.visible).isTrue()
    }

    @Test
    fun `shows at the top even without a direction change`() {
        val reveal = ToolbarReveal()
        reveal.onScroll(60)
        assertThat(reveal.visible).isFalse()
        reveal.onScroll(0)
        assertThat(reveal.visible).isTrue()
    }

    @Test
    fun `reports only real transitions`() {
        val reveal = ToolbarReveal()
        assertThat(reveal.onScroll(40)).isTrue()
        assertThat(reveal.onScroll(80)).isFalse()
        assertThat(reveal.onScroll(70)).isTrue()
    }
}
