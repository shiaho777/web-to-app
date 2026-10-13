package com.webtoapp.ui.plugin

import com.google.common.truth.Truth.assertThat
import kotlin.math.hypot
import org.junit.Test

class PluginFanLayoutTest {

    @Test
    fun `one plugin activates directly and many fan out`() {
        assertThat(pluginHandleClickAction(0)).isEqualTo(PluginHandleClick.OPEN_SHEET)
        assertThat(pluginHandleClickAction(1)).isEqualTo(PluginHandleClick.ACTIVATE_ONLY)
        assertThat(pluginHandleClickAction(2)).isEqualTo(PluginHandleClick.FAN_OUT)
        assertThat(pluginHandleClickAction(9)).isEqualTo(PluginHandleClick.FAN_OUT)
    }

    @Test
    fun `fan offsets sit on a ring away from the hub`() {
        val left = pluginFanOffsets(4, 100f, fanRight = false)
        val right = pluginFanOffsets(4, 100f, fanRight = true)
        assertThat(left).hasSize(4)
        assertThat(right).hasSize(4)
        left.forEach { offset ->
            val dist = hypot(offset.x.toDouble(), offset.y.toDouble())
            assertThat(dist).isWithin(1.5).of(100.0)
            assertThat(offset.y).isLessThan(0)
        }
        // Hub on the right fans left (negative x); hub on the left fans right.
        assertThat(left.map { it.x }.average()).isLessThan(0.0)
        assertThat(right.map { it.x }.average()).isGreaterThan(0.0)
        assertThat(pluginFanOffsets(0, 100f, false)).isEmpty()
        assertThat(pluginFanOffsets(3, 0f, false)).isEmpty()
    }

    @Test
    fun `overflow plugins land on a second ring`() {
        val offsets = pluginFanOffsets(9, 80f, fanRight = false, perRing = 7)
        assertThat(offsets).hasSize(9)
        val inner = hypot(offsets[0].x.toDouble(), offsets[0].y.toDouble())
        val outer = hypot(offsets[7].x.toDouble(), offsets[7].y.toDouble())
        assertThat(outer).isGreaterThan(inner)
    }
}
