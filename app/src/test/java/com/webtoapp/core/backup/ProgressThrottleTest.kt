package com.webtoapp.core.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProgressThrottleTest {

    @Test
    fun `first call always emits`() {
        val throttle = ProgressThrottle(minIntervalMs = 150, clockMs = { 1_000L })
        assertThat(throttle.shouldEmit()).isTrue()
    }

    @Test
    fun `calls inside the window are swallowed`() {
        var now = 0L
        val throttle = ProgressThrottle(minIntervalMs = 150, clockMs = { now })
        assertThat(throttle.shouldEmit()).isTrue()
        now = 100L
        assertThat(throttle.shouldEmit()).isFalse()
        assertThat(throttle.shouldEmit()).isFalse()
        now = 150L
        assertThat(throttle.shouldEmit()).isTrue()
    }

    @Test
    fun `force bypasses the window and resets it`() {
        var now = 0L
        val throttle = ProgressThrottle(minIntervalMs = 10_000L, clockMs = { now })
        assertThat(throttle.shouldEmit()).isTrue()
        assertThat(throttle.shouldEmit(force = true)).isTrue()
        // Timer restarted by the forced emit: normal call right after is swallowed.
        assertThat(throttle.shouldEmit()).isFalse()
        now = 10_000L
        assertThat(throttle.shouldEmit()).isTrue()
    }
}
