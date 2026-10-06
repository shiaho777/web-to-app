package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TopTabPagerTest {

    @Test
    fun `finger tracking is one to one inside the open range`() {
        assertThat(TopTabPager.follow(0f, 0.4f, 4)).isWithin(0.001f).of(0.4f)
        assertThat(TopTabPager.follow(1.2f, -0.3f, 4)).isWithin(0.001f).of(0.9f)
    }

    @Test
    fun `a drag cannot walk past the neighboring page`() {
        val followed = TopTabPager.follow(0f, 2.5f, 4)
        assertThat(followed).isGreaterThan(1f)
        assertThat(followed).isLessThan(1f + TopTabPager.OVERSCROLL_LIMIT)
    }

    @Test
    fun `pulling past either end rubber-bands and never runs away`() {
        val before = TopTabPager.follow(0f, -3f, 4)
        assertThat(before).isLessThan(0f)
        assertThat(before).isGreaterThan(-TopTabPager.OVERSCROLL_LIMIT)
        val after = TopTabPager.follow(4f, 3f, 4)
        assertThat(after).isGreaterThan(4f)
        assertThat(after).isLessThan(4f + TopTabPager.OVERSCROLL_LIMIT)
    }

    @Test
    fun `release picks the page the gesture would coast onto`() {
        assertThat(target(0, 0f, velocityX = -2000f)).isEqualTo(1)
        assertThat(target(0, 0f, velocityX = 2000f)).isEqualTo(0)
        assertThat(target(2, 2f, velocityX = -2000f)).isEqualTo(3)
        assertThat(target(2, 2f, velocityX = 2000f)).isEqualTo(1)
        assertThat(target(0, 0.2f, velocityX = 0f)).isEqualTo(0)
        assertThat(target(0, 0.6f, velocityX = 0f)).isEqualTo(1)
        assertThat(target(0, 0.8f, velocityX = 2000f)).isEqualTo(0)
        assertThat(target(0, 0.2f, velocityX = -2000f)).isEqualTo(1)
        assertThat(target(4, 4f, velocityX = -2000f, max = 4)).isEqualTo(4)
        // RTL: finger moving right advances.
        assertThat(target(0, 0f, velocityX = 2000f, layoutSign = -1f)).isEqualTo(1)
    }

    @Test
    fun `only the pages under the finger are on screen`() {
        assertThat(TopTabPager.visibleRange(0f, 3)).isEqualTo(0..0)
        assertThat(TopTabPager.visibleRange(0.25f, 3)).isEqualTo(0..1)
        assertThat(TopTabPager.visibleRange(2f, 3)).isEqualTo(2..2)
        assertThat(TopTabPager.visibleRange(-0.1f, 3)).isEqualTo(0..0)
    }

    @Test
    fun `pages sit beside the current one and flip in RTL`() {
        assertThat(TopTabPager.translationX(1, 0f, 1000, 1f)).isEqualTo(1000)
        assertThat(TopTabPager.translationX(0, 0.25f, 1000, 1f)).isEqualTo(-250)
        assertThat(TopTabPager.translationX(1, 0f, 1000, -1f)).isEqualTo(-1000)
        assertThat(TopTabPager.translationX(0, 0f, 0, 1f)).isEqualTo(0)
    }

    private fun target(
        start: Int,
        position: Float,
        velocityX: Float,
        layoutSign: Float = 1f,
        max: Int = 4
    ): Int = TopTabPager.targetPage(start, position, velocityX, 1000f, max, layoutSign)
}
