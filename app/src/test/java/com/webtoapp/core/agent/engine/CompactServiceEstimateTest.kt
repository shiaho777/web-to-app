package com.webtoapp.core.agent.engine

import com.google.gson.JsonObject
import com.webtoapp.core.agent.llm.ChatRequest
import com.webtoapp.core.agent.llm.LlmEvent
import com.webtoapp.core.agent.llm.LlmGateway
import com.webtoapp.core.agent.llm.LlmMessage
import com.webtoapp.core.agent.llm.LlmToolCall
import com.webtoapp.core.agent.session.AgentMessage
import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.core.agent.tool.ImageAttachment
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * #1140: the context chip and the auto-compact gate used to estimate only
 * persisted messages at a flat chars/4 — the assembled request (system prompt,
 * tool schemas, replayed tool results, CJK text) could exceed the provider's
 * hard context limit while the chip still showed headroom.
 */
class CompactServiceEstimateTest {

    private val gateway = object : LlmGateway {
        override fun chatStream(req: ChatRequest): Flow<LlmEvent> = flow {}
    }

    private val svc = CompactService(gateway)

    // ---- CJK-aware text estimation ----

    @Test
    fun `cjk text estimates near one token per char`() {
        val text = "你好世界这是一个测试" // 10 CJK chars
        val tokens = svc.estimateTokens(text)
        assertEquals(10, tokens)
    }

    @Test
    fun `cjk history is not undercounted 4x like the old flat rate`() {
        // 10k CJK chars: old estimate = 2500 tokens, real ≈ 10000.
        val messages = listOf(
            AgentMessage(role = AgentMessage.Role.USER, content = "汉".repeat(10_000))
        )
        val estimate = svc.estimateTokens(messages)
        assertTrue("CJK estimate $estimate should be ≥ 9000", estimate >= 9_000)
    }

    @Test
    fun `ascii text uses the non-cjk rate`() {
        val tokens = svc.estimateTokens("a".repeat(300))
        assertEquals(100, tokens)
    }

    @Test
    fun `mixed text splits cjk and ascii`() {
        // 10 CJK (→10) + 30 ascii (→10)
        val tokens = svc.estimateTokens("汉".repeat(10) + "a".repeat(30))
        assertEquals(20, tokens)
    }

    // ---- wire-message estimation ----

    @Test
    fun `llm message estimate counts content reasoning toolcalls and images`() {
        val bare = svc.estimateTokens(LlmMessage(role = LlmMessage.Role.USER, content = ""))
        val withTool = svc.estimateTokens(
            LlmMessage(
                role = LlmMessage.Role.ASSISTANT,
                content = "",
                reasoningContent = "r".repeat(300),
                toolCalls = listOf(LlmToolCall("id", "Write", "x".repeat(300)))
            )
        )
        assertTrue(withTool > bare + 150)

        val withImage = svc.estimateTokens(
            LlmMessage(
                role = LlmMessage.Role.USER,
                content = "",
                images = listOf(ImageAttachment(ByteArray(10), "image/png", "a.png"))
            )
        )
        assertTrue("image should add a large estimate", withImage - bare >= 1000)
    }

    // ---- request-level estimation ----

    private fun stubTool(name: String, schemaSize: Int) = object : Tool {
        override val name = name
        override val description = "tool $name"
        override val parametersSchema = JsonObject().apply {
            addProperty("padding", "x".repeat(schemaSize))
        }
        override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult =
            throw UnsupportedOperationException()
    }

    @Test
    fun `request estimate includes system prompt and tool schemas`() {
        val history = listOf(LlmMessage(role = LlmMessage.Role.USER, content = "hello"))
        val tools = listOf(stubTool("Write", 30_000))

        val historyOnly = svc.estimateTokens(LlmMessage(role = LlmMessage.Role.USER, content = "hello")) * 0 +
            history.sumOf { svc.estimateTokens(it) }
        val full = svc.estimateRequestTokens("s".repeat(30_000), history, "hi", tools)

        // envelope adds ≥ system prompt + schema ≈ 20k tokens over the bare history
        assertTrue("request estimate $full should exceed history-only $historyOnly", full > historyOnly + 15_000)
    }

    @Test
    fun `recorded tool replay content feeds the estimate`() {
        val msg = AgentMessage(
            role = AgentMessage.Role.ASSISTANT,
            content = "",
            toolCalls = listOf(
                RecordedToolCall(
                    toolCallId = "t1",
                    name = "Read",
                    argumentsJson = "{}",
                    resultPreview = "z".repeat(30_000),
                    ok = true
                )
            )
        )
        val estimate = svc.estimateTokens(listOf(msg))
        assertTrue("replayed tool result must count, got $estimate", estimate >= 9_000)
    }

    // ---- thresholds ----

    @Test
    fun `request threshold trips at 75 percent of context length`() {
        val ctx = 1_048_576
        assertFalse(svc.shouldCompactRequest(780_000, ctx))
        assertTrue(svc.shouldCompactRequest(790_000, ctx))
    }

    // ---- provider overflow detection ----

    @Test
    fun `isContextOverflowError matches real provider messages`() {
        assertTrue(
            CompactService.isContextOverflowError(
                "Bad request (400): {\"error\":{\"message\":\"This model's maximum " +
                    "context length is 1048576 tokens. However, you requested 1085580 " +
                    "tokens ... Please reduce the length of the messages\"}}"
            )
        )
        assertTrue(CompactService.isContextOverflowError("Error 400: prompt is too long"))
        assertTrue(CompactService.isContextOverflowError("context_length_exceeded"))
        assertTrue(CompactService.isContextOverflowError("request too large for model"))
        assertTrue(CompactService.isContextOverflowError("input exceeds the token limit"))
    }

    @Test
    fun `isContextOverflowError rejects unrelated failures`() {
        assertFalse(CompactService.isContextOverflowError("401 Unauthorized: invalid api key"))
        assertFalse(CompactService.isContextOverflowError("429 rate limit exceeded, retry after 30s"))
        assertFalse(CompactService.isContextOverflowError("network unreachable"))
        assertFalse(CompactService.isContextOverflowError("500 internal server error"))
    }
}
