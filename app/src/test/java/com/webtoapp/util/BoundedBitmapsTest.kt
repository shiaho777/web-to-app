package com.webtoapp.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BoundedBitmapsTest {

    @Test
    fun `small images are untouched`() {
        assertThat(BoundedBitmaps.calculateInSampleSize(1000, 800, 2048)).isEqualTo(1)
        assertThat(BoundedBitmaps.calculateInSampleSize(2048, 2048, 2048)).isEqualTo(1)
    }

    @Test
    fun `long side over cap triggers sampling`() {
        // 1080x2412 phone screenshot: height exceeds 2048 -> sample 2 -> 540x1206.
        assertThat(BoundedBitmaps.calculateInSampleSize(1080, 2412, 2048)).isEqualTo(2)
    }

    @Test
    fun `issue 779 repro dimensions downsample below the cap`() {
        // 15360x15360 @ARGB_8888 = 943,718,400 bytes: the exact crash report.
        assertThat(BoundedBitmaps.calculateInSampleSize(15360, 15360, 2048)).isEqualTo(8)
        // 15360 / 8 = 1920 per side -> ~14MB, drawable-safe.
    }

    @Test
    fun `panorama longest side governs`() {
        // 20000x1000 must not slip through on the short side.
        assertThat(BoundedBitmaps.calculateInSampleSize(20000, 1000, 2048)).isEqualTo(16)
    }

    @Test
    fun `sample size stays power of two`() {
        var sample = BoundedBitmaps.calculateInSampleSize(100000, 100000, 2048)
        assertThat(sample and (sample - 1)).isEqualTo(0)
    }

    @Test
    fun `degenerate input never divides by zero`() {
        assertThat(BoundedBitmaps.calculateInSampleSize(0, 100, 2048)).isEqualTo(1)
        assertThat(BoundedBitmaps.calculateInSampleSize(100, -5, 2048)).isEqualTo(1)
        assertThat(BoundedBitmaps.calculateInSampleSize(100, 100, 0)).isEqualTo(1)
    }

    @Test
    fun `icon cap fits launcher needs`() {
        // xxxhdpi adaptive foreground (432px) must survive the icon cap intact.
        assertThat(BoundedBitmaps.calculateInSampleSize(432, 432, BoundedBitmaps.ICON_MAX_DIMENSION))
            .isEqualTo(1)
    }
}
