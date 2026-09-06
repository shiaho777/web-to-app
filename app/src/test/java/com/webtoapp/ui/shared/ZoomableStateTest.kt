package com.webtoapp.ui.shared

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ZoomableStateTest {

    @Test
    fun `clamp keeps unzoomed content centered`() {
        val out = ZoomableState.clampOffset(
            Offset(100f, -50f), 1f, Size(1080f, 2400f), Size(1080f, 1080f)
        )
        assertThat(out.x).isWithin(0.01f).of(0f)
        assertThat(out.y).isWithin(0.01f).of(0f)
    }

    @Test
    fun `clamp bounds panning to content edges`() {
        // 64px image fitted into 1080x2211 viewport => 1080x1080 at 2x zoom:
        // max offset = (2160-1080)/2 = 540 horizontally, 0 vertically.
        val viewport = Size(1080f, 2211f)
        val content = ZoomableState.fittedContentSize(64, 64, viewport)
        assertThat(content.width).isWithin(1f).of(1080f)
        assertThat(content.height).isWithin(1f).of(1080f)
        val out = ZoomableState.clampOffset(Offset(9999f, 9999f), 2f, viewport, content)
        assertThat(out.x).isWithin(1f).of(540f)
        assertThat(out.y).isWithin(0.01f).of(0f)
        val back = ZoomableState.clampOffset(Offset(-9999f, -5f), 2f, viewport, content)
        assertThat(back.x).isWithin(1f).of(-540f)
        assertThat(back.y).isWithin(0.01f).of(0f)
    }

    @Test
    fun `fitted size honors aspect`() {
        val out = ZoomableState.fittedContentSize(800, 600, Size(1080f, 2211f))
        assertThat(out.width).isWithin(1f).of(1080f)
        assertThat(out.height).isWithin(1f).of(810f)
    }

    @Test
    fun `fitted size rejects degenerate input`() {
        assertThat(ZoomableState.fittedContentSize(0, 10, Size(100f, 100f))).isEqualTo(Size.Zero)
        assertThat(ZoomableState.fittedContentSize(10, 10, Size.Zero)).isEqualTo(Size.Zero)
    }

    @Test
    fun `pinch anchors the centroid`() {
        val s = ZoomableState()
        s.setLayout(Size(1000f, 1000f), Size(800f, 800f))
        // Centroid at (750, 500); zoom 1x -> 2x. Content point under the
        // centroid must stay put: it starts at content coords
        // (750-500, 500-500)/1 = (250, 0), so offset must become
        // (750-500) - (250, 0)*2 = (-250, 0) (within ±300 clamp).
        assertThat(s.applyZoom(2f, Offset(750f, 500f))).isTrue()
        assertThat(s.scale).isWithin(0.001f).of(2f)
        assertThat(s.offset.x).isWithin(0.5f).of(-250f)
        assertThat(s.offset.y).isWithin(0.5f).of(0f)
    }

    @Test
    fun `zoom clamps to min-max and resets offset at min`() {
        val s = ZoomableState()
        s.setLayout(Size(1000f, 1000f), Size(500f, 500f))
        assertThat(s.applyZoom(100f, Offset(500f, 500f))).isTrue()
        assertThat(s.scale).isWithin(0.001f).of(5f)
        assertThat(s.applyZoom(0.0001f, Offset(500f, 500f))).isTrue()
        assertThat(s.scale).isWithin(0.001f).of(1f)
        assertThat(s.offset.x).isWithin(0.01f).of(0f); assertThat(s.offset.y).isWithin(0.01f).of(0f)
    }

    @Test
    fun `pan is ignored at min scale`() {
        val s = ZoomableState()
        s.setLayout(Size(1000f, 1000f), Size(500f, 500f))
        assertThat(s.applyPan(Offset(50f, 50f))).isFalse()
        assertThat(s.offset.x).isWithin(0.01f).of(0f); assertThat(s.offset.y).isWithin(0.01f).of(0f)
    }

    @Test
    fun `double-tap toggles out when zoomed`() {
        val s = ZoomableState()
        s.setLayout(Size(1000f, 1000f), Size(500f, 500f))
        s.applyZoom(3f, Offset(500f, 500f))
        s.toggleZoom(Offset(100f, 100f))
        assertThat(s.scale).isWithin(0.001f).of(1f)
        assertThat(s.offset.x).isWithin(0.01f).of(0f); assertThat(s.offset.y).isWithin(0.01f).of(0f)
    }

    @Test
    fun `double-tap zooms toward the tap point`() {
        val s = ZoomableState()
        s.setLayout(Size(1000f, 1000f), Size(1000f, 1000f))
        // Tap at (750, 500) from 1x: target 3x anchored there gives
        // offset = (250, 0) - (250, 0)*3 = (-500, 0).
        s.toggleZoom(Offset(750f, 500f))
        assertThat(s.scale).isWithin(0.001f).of(3f)
        assertThat(s.offset.x).isWithin(0.5f).of(-500f)
        assertThat(s.offset.y).isWithin(0.5f).of(0f)
    }

    @Test
    fun `reset restores identity`() {
        val s = ZoomableState()
        s.setLayout(Size(1000f, 1000f), Size(500f, 500f))
        s.applyZoom(2f, Offset(500f, 500f))
        s.applyPan(Offset(10f, 10f))
        s.reset()
        assertThat(s.scale).isWithin(0.001f).of(1f)
        assertThat(s.offset.x).isWithin(0.01f).of(0f); assertThat(s.offset.y).isWithin(0.01f).of(0f)
    }
}
