package com.webtoapp.core.ai

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.agent.llm.ChatRequest
import com.webtoapp.core.agent.llm.HttpHelpers
import com.webtoapp.data.model.AiModel
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiFormat
import com.webtoapp.data.model.ApiKeyConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Test

class OpenCodeGoTest {
    @Test
    fun `Go requests reuse the conversation header across endpoints and retries`() {
        val chat = request()
        val initial = outbound(chat)
        val retry = outbound(chat.copy(temperature = 1f))
        val legacy = outbound(chat.copy(apiKey = chat.apiKey.copy(baseUrl = "https://opencode.ai/zen/go/v1")))
        val other = outbound(chat.copy(sessionId = "another-conversation"))

        val header = initial.header("x-opencode-session")
        assertThat(header).matches("ses_[0-9a-f]{32}")
        assertThat(retry.header("x-opencode-session")).isEqualTo(header)
        assertThat(legacy.header("x-opencode-session")).isEqualTo(header)
        assertThat(other.header("x-opencode-session")).isNotEqualTo(header)
        assertThat(legacy.url.encodedPath).isEqualTo("/zen/go/v1/chat/completions")
    }

    @Test
    fun `Go maps the configured Agent protocol to its Console endpoint`() {
        listOf(
            ApiFormat.OPENAI_COMPATIBLE to "/openai/v1/chat/completions",
            ApiFormat.OPENAI_RESPONSES to "/openai/v1/responses",
            ApiFormat.ANTHROPIC to "/anthropic/v1/messages"
        ).forEach { (format, route) ->
            val outbound = outbound(request(format = format))
            assertThat(outbound.url.encodedPath).isEqualTo("/inference/go$route")
            assertThat(outbound.header("Authorization")).isEqualTo("Bearer test-key")
            assertThat(outbound.header("x-opencode-session")).isNotEmpty()
        }

        val custom = outbound(request(baseUrl = "https://example.com", format = ApiFormat.ANTHROPIC))
        assertThat(custom.header("x-api-key")).isEqualTo("test-key")
        assertThat(custom.header("Authorization")).isNull()
        assertThat(custom.header("x-opencode-session")).isNull()
    }

    @Test
    fun `other destinations and redirects do not receive Go session headers`() {
        val initial = outbound(request())
        val otherUrls = listOf(
            "https://opencode.ai.example.com/inference/go/openai/v1/chat/completions",
            "https://opencode.ai/inference/openai/v1/chat/completions",
            "https://opencode.ai/inference/go/v1/models",
            "https://opencode.ai:8443/inference/go/openai/v1/chat/completions",
            "http://opencode.ai/inference/go/openai/v1/chat/completions"
        )
        for (url in otherUrls) {
            val redirected = initial.newBuilder().url(url).build()
            assertThat(OpenCodeGo.applySessionHeader(redirected).header("x-opencode-session")).isNull()
        }

        val sameService = initial.newBuilder().url("https://opencode.ai/zen/go/v1/chat/completions").build()
        assertThat(OpenCodeGo.applySessionHeader(sameService).header("x-opencode-session"))
            .isEqualTo(initial.header("x-opencode-session"))
    }

    private fun request(
        baseUrl: String = "https://opencode.ai/inference/go",
        format: ApiFormat = ApiFormat.OPENAI_COMPATIBLE
    ): ChatRequest = ChatRequest(
        apiKey = ApiKeyConfig(
            provider = AiProvider.CUSTOM,
            apiKey = "test-key",
            baseUrl = baseUrl,
            apiFormat = format
        ),
        model = AiModel(id = "test-model", name = "Test", provider = AiProvider.CUSTOM),
        messages = emptyList(),
        sessionId = "test-conversation"
    )

    private fun outbound(req: ChatRequest): Request {
        val body = "{}".toRequestBody("application/json".toMediaType())
        return OpenCodeGo.applySessionHeader(HttpHelpers.newChatCall(req, body).request())
    }
}
