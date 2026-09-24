package com.webtoapp.core.port

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PortSessionAuthTest {

    @Before
    fun setUp() {
        PortSessionAuth.clear()
    }

    @After
    fun tearDown() {
        PortSessionAuth.clear()
    }

    @Test
    fun `issued token consumes once`() {
        val token = PortSessionAuth.issue()
        assertThat(token).isNotEmpty()

        assertThat(PortSessionAuth.consume(token)).isTrue()
    }

    @Test
    fun `token is single-use`() {
        val token = PortSessionAuth.issue()

        assertThat(PortSessionAuth.consume(token)).isTrue()
        // Second attempt with the same token must fail — a leaked token can
        // only drive one release call.
        assertThat(PortSessionAuth.consume(token)).isFalse()
    }

    @Test
    fun `unknown and blank tokens are rejected`() {
        assertThat(PortSessionAuth.consume(null)).isFalse()
        assertThat(PortSessionAuth.consume("")).isFalse()
        assertThat(PortSessionAuth.consume("   ")).isFalse()
        assertThat(PortSessionAuth.consume("not-a-issued-token")).isFalse()
    }

    @Test
    fun `tokens do not collide across issues`() {
        val a = PortSessionAuth.issue()
        val b = PortSessionAuth.issue()
        assertThat(a).isNotEqualTo(b)

        // Consuming one does not affect the other.
        assertThat(PortSessionAuth.consume(a)).isTrue()
        assertThat(PortSessionAuth.consume(b)).isTrue()
    }

    @Test
    fun `expired token is rejected`() {
        val t0 = 1_000_000L
        val token = PortSessionAuth.issue(now = t0)

        // Within TTL the token is still valid.
        assertThat(PortSessionAuth.consume("other", now = t0 + 59_000L)).isFalse()
        // Past TTL the real token must also be rejected.
        assertThat(PortSessionAuth.consume(token, now = t0 + 61_000L)).isFalse()
    }

    @Test
    fun `expiry purge does not drop fresh tokens`() {
        val t0 = 1_000_000L
        val old = PortSessionAuth.issue(now = t0)
        val fresh = PortSessionAuth.issue(now = t0 + 61_000L)

        // Purge triggered by the second issue removed the expired one only.
        assertThat(PortSessionAuth.consume(old, now = t0 + 62_000L)).isFalse()
        assertThat(PortSessionAuth.consume(fresh, now = t0 + 62_000L)).isTrue()
    }
}
