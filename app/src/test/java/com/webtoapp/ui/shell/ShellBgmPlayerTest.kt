package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShellBgmPlayerTest {

    @Test
    fun `non-shuffle initial order is identity`() {
        val state = initialBgmOrder(4, shuffle = false)
        assertThat(state.order).containsExactly(0, 1, 2, 3).inOrder()
        assertThat(state.pos).isEqualTo(0)
        assertThat(state.currentIndex()).isEqualTo(0)
    }

    @Test
    fun `shuffle initial order is a full permutation`() {
        repeat(10) {
            val state = initialBgmOrder(5, shuffle = true)
            assertThat(state.order).hasSize(5)
            assertThat(state.order.sorted()).containsExactly(0, 1, 2, 3, 4).inOrder()
            assertThat(state.pos).isEqualTo(0)
        }
    }

    @Test
    fun `initial order handles degenerate sizes`() {
        assertThat(initialBgmOrder(0, shuffle = true).order).isEmpty()
        assertThat(initialBgmOrder(0, shuffle = true).currentIndex()).isEqualTo(0)
        assertThat(initialBgmOrder(1, shuffle = true).order).containsExactly(0)
    }

    @Test
    fun `shuffle does not always start on the first track`() {
        // Regression test for "shuffle always plays song #1": with 5 tracks,
        // always landing on index 0 across 30 shuffles has probability
        // (1/5)^30 ~= 1e-21, i.e. deterministic for practical purposes.
        val firsts = (0 until 30).map { initialBgmOrder(5, shuffle = true).currentIndex() }.toSet()
        assertThat(firsts.size).isGreaterThan(1)
    }

    @Test
    fun `advance walks the permutation then reshuffles on wrap`() {
        val start = BgmShuffleOrder(listOf(2, 0, 1), 0)
        assertThat(start.currentIndex()).isEqualTo(2)
        val mid = advanceBgmOrder(start, 3)
        assertThat(mid.order).containsExactly(2, 0, 1).inOrder()
        assertThat(mid.pos).isEqualTo(1)
        assertThat(mid.currentIndex()).isEqualTo(0)
        val wrapped = advanceBgmOrder(advanceBgmOrder(mid, 3), 3)
        assertThat(wrapped.pos).isEqualTo(0)
        assertThat(wrapped.order.sorted()).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun `advance on empty or zero size stays empty`() {
        val state = advanceBgmOrder(BgmShuffleOrder(), 0)
        assertThat(state.order).isEmpty()
        assertThat(state.currentIndex()).isEqualTo(0)
    }

    @Test
    fun `currentIndex falls back to zero when out of range`() {
        assertThat(BgmShuffleOrder(listOf(2, 0), 7).currentIndex()).isEqualTo(0)
    }
}
