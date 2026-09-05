package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReloadCircuitBreakerTest {

    @Test
    fun `allows budget then trips`() {
        val breaker = ReloadCircuitBreaker(maxReloads = 3, windowMs = 10_000L, clockMs = { 0L })
        assertThat(breaker.noteAutoReload("http://192.168.1.10/login")).isTrue()
        assertThat(breaker.noteAutoReload("http://192.168.1.10/login")).isTrue()
        assertThat(breaker.noteAutoReload("http://192.168.1.10/login")).isTrue()
        assertThat(breaker.noteAutoReload("http://192.168.1.10/login")).isFalse()
    }

    @Test
    fun `window expiry resets budget`() {
        var now = 0L
        val breaker = ReloadCircuitBreaker(maxReloads = 1, windowMs = 1_000L, clockMs = { now })
        assertThat(breaker.noteAutoReload("http://example.com/a")).isTrue()
        assertThat(breaker.noteAutoReload("http://example.com/a")).isFalse()
        now = 1_001L
        assertThat(breaker.noteAutoReload("http://example.com/a")).isTrue()
    }

    @Test
    fun `budgets are per host across url cycles`() {
        val breaker = ReloadCircuitBreaker(maxReloads = 2, windowMs = 10_000L, clockMs = { 0L })
        // Login <-> dashboard cycle on one host shares a single budget.
        assertThat(breaker.noteAutoReload("http://192.168.1.10/login")).isTrue()
        assertThat(breaker.noteAutoReload("http://192.168.1.10/dashboard")).isTrue()
        assertThat(breaker.noteAutoReload("http://192.168.1.10/login")).isFalse()
    }

    @Test
    fun `different hosts do not share budget`() {
        val breaker = ReloadCircuitBreaker(maxReloads = 1, windowMs = 10_000L, clockMs = { 0L })
        assertThat(breaker.noteAutoReload("http://a.example.com/")).isTrue()
        assertThat(breaker.noteAutoReload("http://b.example.com/")).isTrue()
    }

    @Test
    fun `unparseable urls are never blocked`() {
        val breaker = ReloadCircuitBreaker(maxReloads = 1, windowMs = 10_000L, clockMs = { 0L })
        assertThat(breaker.noteAutoReload(null)).isTrue()
        assertThat(breaker.noteAutoReload("")).isTrue()
        assertThat(breaker.noteAutoReload("not a url [[[")).isTrue()
        assertThat(breaker.noteAutoReload("not a url [[[")).isTrue()
    }

    @Test
    fun `host matching is case-insensitive`() {
        val breaker = ReloadCircuitBreaker(maxReloads = 1, windowMs = 10_000L, clockMs = { 0L })
        assertThat(breaker.noteAutoReload("http://Example.COM/a")).isTrue()
        assertThat(breaker.noteAutoReload("http://example.com/b")).isFalse()
    }
}
