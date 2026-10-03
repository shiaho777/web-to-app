package com.webtoapp.core.python

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.concurrent.atomic.AtomicLong

class ReadinessBudgetTest {

    private class FakeClock : () -> Long {
        val now = AtomicLong(1_000_000L)
        override fun invoke(): Long = now.get()
        fun advance(ms: Long) = now.addAndGet(ms)
    }

    @Test
    fun `expires after the base window with no progress`() {
        val clock = FakeClock()
        val budget = ReadinessBudget(baseMs = 90_000L, hardCapMs = 300_000L, clock = clock)

        assertThat(budget.expired()).isFalse()
        clock.advance(89_999L)
        assertThat(budget.expired()).isFalse()
        clock.advance(1L)
        assertThat(budget.expired()).isTrue()
    }

    @Test
    fun `markProgress resets the base window`() {
        val clock = FakeClock()
        val budget = ReadinessBudget(baseMs = 90_000L, hardCapMs = 300_000L, clock = clock)

        // 60s of silent startup, then an output line arrives.
        clock.advance(60_000L)
        assertThat(budget.expired()).isFalse()
        budget.markProgress()
        clock.advance(89_999L)
        assertThat(budget.expired()).isFalse()

        // The window is measured from the last progress, not from start.
        clock.advance(1L)
        assertThat(budget.expired()).isTrue()
        assertThat(budget.msSinceProgress()).isEqualTo(90_000L)
        assertThat(budget.elapsedMs()).isEqualTo(150_000L)
    }

    @Test
    fun `hard cap wins over repeated progress`() {
        val clock = FakeClock()
        val budget = ReadinessBudget(baseMs = 90_000L, hardCapMs = 300_000L, clock = clock)

        // Keep marking progress every 10s forever: never base-expired...
        repeat(29) {
            clock.advance(10_000L)
            budget.markProgress()
            assertThat(budget.expired()).isFalse()
        }

        // ...but the hard cap from start still applies.
        clock.advance(10_000L)
        assertThat(budget.elapsedMs()).isEqualTo(300_000L)
        assertThat(budget.expired()).isTrue()
    }
}
