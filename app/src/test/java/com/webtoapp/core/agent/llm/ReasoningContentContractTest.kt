package com.webtoapp.core.agent.llm

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test

/**
 * Verifies the reasoning_content round-trip contract:
 *
 * 1. When an assistant message has reasoningContent, it must be serialized as
 *    `reasoning_content` in the API request (so GLM thinking mode accepts it).
 * 2. When reasoningContent is null/empty, it must still send an empty string
 *    (GLM requires the field on every assistant message once thinking is used).
 *
 * This mirrors the logic in OpenAiCompatProvider.buildMsg() without needing a
 * full Android Context (the provider's buildMsg is too entangled with Context
 * to unit-test directly; this test validates the contract it must follow).
 */
class ReasoningContentContractTest {

    private val IS = "\u2063"

    @Test
    fun `assistant message with reasoningContent should produce reasoning_content in JSON`() {
        val msg = LlmMessage(
            role = LlmMessage.Role.ASSISTANT,
            content = "Hello",
            reasoningContent = "I thought about this"
        )
        val json = simulateBuildMsg(msg)
        assertThat(json.has("reasoning_content")).isTrue()
        assertThat(json.get("reasoning_content").asString).isEqualTo("I thought about this")
    }

    @Test
    fun `assistant message with null reasoningContent should send empty string reasoning_content`() {
        // GLM thinking mode requires the field — empty string is the safe default.
        val msg = LlmMessage(
            role = LlmMessage.Role.ASSISTANT,
            content = "Hello",
            reasoningContent = null
        )
        val json = simulateBuildMsg(msg)
        assertThat(json.has("reasoning_content")).isTrue()
        assertThat(json.get("reasoning_content").asString).isEmpty()
    }

    @Test
    fun `assistant message with empty reasoningContent should send empty string`() {
        val msg = LlmMessage(
            role = LlmMessage.Role.ASSISTANT,
            content = "Hello",
            reasoningContent = ""
        )
        val json = simulateBuildMsg(msg)
        assertThat(json.has("reasoning_content")).isTrue()
        assertThat(json.get("reasoning_content").asString).isEmpty()
    }

    @Test
    fun `non-assistant messages should not have reasoning_content`() {
        val userMsg = LlmMessage(role = LlmMessage.Role.USER, content = "Hi")
        val json = simulateBuildMsg(userMsg)
        assertThat(json.has("reasoning_content")).isFalse()
    }

    @Test
    fun `AgentEvent ThinkingDelta carries segmentId`() {
        // This is in AgentEvent, not LlmEvent — the engine adds segmentId when forwarding.
        val ev = com.webtoapp.core.agent.engine.AgentEvent.ThinkingDelta(
            segmentId = "th-turn-1",
            delta = "thinking...",
            accumulated = "thinking..."
        )
        assertThat(ev.segmentId).isEqualTo("th-turn-1")
        assertThat(ev.delta).isEqualTo("thinking...")
    }

    @Test
    fun `ChatRequest maxTokens defaults to null (omitted from API request)`() {
        val req = ChatRequest(
            apiKey = com.webtoapp.data.model.ApiKeyConfig(
                provider = com.webtoapp.data.model.AiProvider.OPENAI,
                apiKey = "test"
            ),
            model = com.webtoapp.data.model.AiModel(
                id = "test-model",
                name = "Test",
                provider = com.webtoapp.data.model.AiProvider.OPENAI
            ),
            messages = emptyList()
        )
        assertThat(req.maxTokens).isNull()
    }

    /**
     * Simulates the exact logic from OpenAiCompatProvider.buildMsg() for
     * reasoning_content handling, without needing Android Context.
     */
    private fun simulateBuildMsg(msg: LlmMessage): JsonObject {
        val obj = JsonObject()
        obj.addProperty("role", when (msg.role) {
            LlmMessage.Role.SYSTEM -> "system"
            LlmMessage.Role.USER -> "user"
            LlmMessage.Role.ASSISTANT -> "assistant"
            LlmMessage.Role.TOOL -> "tool"
        })
        obj.addProperty("content", msg.content)

        // This is the critical logic from OpenAiCompatProvider.buildMsg():
        if (msg.role == LlmMessage.Role.ASSISTANT) {
            obj.addProperty("reasoning_content", msg.reasoningContent ?: "")
        }

        return obj
    }
}
