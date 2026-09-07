package com.webtoapp.core.agent.engine

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Regression tests for the idle-timeout stream collector. The #742 rewrite
 * re-collected the cold provider flow for every event, which re-ran the
 * producer (a fresh HTTP request per event) and dropped all response events —
 * the UI sat on "thinking" forever while the client spammed the API.
 */
class CollectWithIdleTimeoutTest {

    @Test(timeout = 10_000)
    fun `flow is subscribed exactly once and every event arrives in order`() = runBlocking {
        val producerRuns = AtomicInteger(0)
        val source = callbackFlow {
            producerRuns.incrementAndGet()
            trySend(1)
            trySend(2)
            trySend(3)
            close()
            awaitClose { }
        }
        val received = mutableListOf<Int>()
        var timedOut = false

        source.collectWithIdleTimeout(idleTimeoutMs = 5_000, onTimeout = { timedOut = true }) {
            received += it
        }

        assertEquals(listOf(1, 2, 3), received)
        assertEquals("producer must run once per request, not once per event", 1, producerRuns.get())
        assertFalse(timedOut)
    }

    @Test(timeout = 10_000)
    fun `idle gap longer than the window triggers onTimeout and cancels the producer`() = runBlocking {
        val producerClosed = AtomicBoolean(false)
        val source = callbackFlow {
            trySend(1)
            awaitClose { producerClosed.set(true) } // never emits again
        }
        val received = mutableListOf<Int>()
        var timedOut = false

        source.collectWithIdleTimeout(idleTimeoutMs = 100, onTimeout = { timedOut = true }) {
            received += it
        }

        assertEquals(listOf(1), received)
        assertTrue(timedOut)
        assertTrue("timing out must tear down the producer (and its HTTP call)", producerClosed.get())
    }

    @Test(timeout = 10_000)
    fun `slow stream with gaps inside the window is not treated as idle`() = runBlocking {
        val source = flow {
            emit(1)
            delay(150)
            emit(2)
        }
        val received = mutableListOf<Int>()
        var timedOut = false

        source.collectWithIdleTimeout(idleTimeoutMs = 5_000, onTimeout = { timedOut = true }) {
            received += it
        }

        assertEquals(listOf(1, 2), received)
        assertFalse(timedOut)
    }

    @Test(timeout = 10_000)
    fun `producer failure propagates to the caller`() = runBlocking {
        val source = flow<Int> {
            emit(1)
            throw IllegalStateException("boom")
        }
        var caught: Throwable? = null

        try {
            source.collectWithIdleTimeout(idleTimeoutMs = 5_000, onTimeout = {}) { }
        } catch (t: Throwable) {
            caught = t
        }

        // kotlinx stacktrace recovery may deliver a copy of the original exception.
        assertTrue(caught is IllegalStateException)
        assertEquals("boom", caught?.message)
    }
}
