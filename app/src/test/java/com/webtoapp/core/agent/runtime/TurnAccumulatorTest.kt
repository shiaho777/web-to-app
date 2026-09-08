package com.webtoapp.core.agent.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Regression coverage for the silent-empty-response bug: a turn whose request died
 * BEFORE the first streamed delta accumulated no text/thinking/tools, and
 * [TurnAccumulator.buildFinalMessage] returned null — the error was then dropped
 * entirely (no persisted bubble, no banner, nothing). A failed turn must always
 * leave a durable record; an empty-but-completed turn must persist the engine's
 * empty-response notice; only a substance-less abort stays dropped.
 */
@RunWith(RobolectricTestRunner::class)
class TurnAccumulatorTest {

    @Test
    fun `error turn with zero streamed output still produces a persisted message`() {
        val acc = TurnAccumulator("session-1")
        val msg = acc.buildFinalMessage(
            summaryFallback = null,
            isError = true,
            errorSuffix = "API key invalid or expired (401)"
        )
        assertNotNull("a failed turn must never vanish without a record", msg)
        assertTrue(msg!!.isError)
        assertTrue(
            "the persisted content must carry the failure reason for diagnosis",
            msg.content.contains("API key invalid or expired (401)")
        )
    }

    @Test
    fun `completed turn with empty-response notice persists that notice`() {
        val acc = TurnAccumulator("session-1")
        val notice = "The model returned an empty response (no text, no tool calls)."
        val msg = acc.buildFinalMessage(summaryFallback = notice, isError = false)
        assertNotNull(msg)
        assertFalse(msg!!.isError)
        assertEquals(notice, msg.content)
    }

    @Test
    fun `substance-less abort is still dropped`() {
        val acc = TurnAccumulator("session-1")
        val msg = acc.buildFinalMessage(
            summaryFallback = null,
            isError = true,
            errorSuffix = null,
            aborted = true
        )
        assertNull("a deliberate cancel with no output must stay silent", msg)
    }

    @Test
    fun `normal completed turn keeps its streamed text over the fallback`() {
        val acc = TurnAccumulator("session-1")
        acc.applyTextDelta("hello world")
        val msg = acc.buildFinalMessage(summaryFallback = "fallback-should-be-ignored", isError = false)
        assertNotNull(msg)
        assertEquals("hello world", msg!!.content)
    }
}
