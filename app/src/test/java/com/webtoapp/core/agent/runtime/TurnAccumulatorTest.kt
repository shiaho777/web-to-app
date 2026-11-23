package com.webtoapp.core.agent.runtime

import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.core.agent.tool.ToolResult

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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

    @Test
    fun `tool-only turn preserves its timeline markers without adding assistant prose`() {
        val acc = TurnAccumulator("session-1")
        val marker = "\u2063TC:write\u2063"
        acc.startTool("write", "Write")
        acc.applyTextDelta(marker)
        acc.finishTool("write", "Write", "{}", ToolResult.ok("Created index.html"))

        val result = acc.buildFinalMessage(summaryFallback = null, isError = false)!!

        assertEquals(marker, result.content)
        assertEquals(RecordedToolCall.Status.Completed, result.toolCalls.single().effectiveStatus)
    }

    @Test
    fun `progress does not complete a tool and abort keeps its partial output`() {
        val acc = TurnAccumulator("session-1")
        acc.startTool("write", "Write")
        acc.updateToolProgress("write", "progress")
        val running = acc.buildFinalMessage(summaryFallback = null, isError = false)!!.toolCalls.single()
        assertEquals(RecordedToolCall.Status.Running, running.effectiveStatus)

        acc.finishRunningToolsAsAborted()
        val aborted = acc.buildFinalMessage(summaryFallback = null, isError = true, aborted = true)!!
        assertEquals(RecordedToolCall.Status.Stopped, aborted.toolCalls.single().effectiveStatus)
        assertEquals("progress", aborted.toolCalls.single().visibleResult)
    }

}
