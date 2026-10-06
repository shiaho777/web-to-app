package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StatusBarPageColorTrackerTest {

    @Test
    fun `a settled sample retries after the first paint`() {
        assertThat(statusBarSampleDelays(48L, settle = false).toList()).containsExactly(48L)
        assertThat(statusBarSampleDelays(48L, settle = true).toList())
            .containsExactly(48L, 398L, 1248L)
            .inOrder()
        assertThat(statusBarSampleDelays(-10L, settle = true).toList())
            .containsExactly(0L, 350L, 1200L)
            .inOrder()
    }

    @Test
    fun `a slower early sample cannot overwrite a newer one`() {
        val gate = PageColorSampleGate()
        val burst = gate.begin()
        val first = checkNotNull(gate.open(burst))
        val second = checkNotNull(gate.open(burst))
        assertThat(gate.accept(burst, second)).isTrue()
        assertThat(gate.accept(burst, first)).isFalse()
        assertThat(gate.accept(burst, second)).isFalse()
    }

    @Test
    fun `a new burst drops samples still in flight`() {
        val gate = PageColorSampleGate()
        val firstBurst = gate.begin()
        val stale = checkNotNull(gate.open(firstBurst))
        val secondBurst = gate.begin()
        assertThat(gate.open(firstBurst)).isNull()
        val fresh = checkNotNull(gate.open(secondBurst))
        assertThat(gate.accept(firstBurst, stale)).isFalse()
        assertThat(gate.accept(secondBurst, fresh)).isTrue()
    }
}
