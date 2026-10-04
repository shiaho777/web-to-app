package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

class FreshSessionGateTest {

    @After
    fun tearDown() {
        FreshSessionGate.resetForTest()
    }

    @Test
    fun `navigation runs immediately when nothing is being cleared`() {
        var loads = 0
        FreshSessionGate.runWhenReady { loads++ }
        assertThat(loads).isEqualTo(1)
    }

    @Test
    fun `navigation waits until the cookie clear finishes`() {
        var loads = 0
        val ticket = FreshSessionGate.open(armTimeout = false)
        FreshSessionGate.runWhenReady { loads++ }
        assertThat(loads).isEqualTo(0)
        FreshSessionGate.close(ticket)
        assertThat(loads).isEqualTo(1)
    }

    @Test
    fun `navigation waits for every overlapping clear`() {
        var loads = 0
        val first = FreshSessionGate.open(armTimeout = false)
        val second = FreshSessionGate.open(armTimeout = false)
        FreshSessionGate.runWhenReady { loads++ }
        FreshSessionGate.close(first)
        assertThat(loads).isEqualTo(0)
        FreshSessionGate.close(second)
        assertThat(loads).isEqualTo(1)
    }

    @Test
    fun `closing the same ticket twice does not release a later clear`() {
        var loads = 0
        val first = FreshSessionGate.open(armTimeout = false)
        FreshSessionGate.close(first)
        FreshSessionGate.close(first)
        val second = FreshSessionGate.open(armTimeout = false)
        FreshSessionGate.runWhenReady { loads++ }
        assertThat(loads).isEqualTo(0)
        FreshSessionGate.close(second)
        assertThat(loads).isEqualTo(1)
    }
}
