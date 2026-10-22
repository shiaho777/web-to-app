package com.webtoapp.core.agent.llm

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ThoughtSignaturesTest {

    @Test
    fun `gemini part signature is read from the part and echoed beside the function call`() {
        val part = JsonParser.parseString(
            """{"functionCall":{"name":"Read","args":{"path":"index.html"}},"thoughtSignature":"sig-1"}"""
        ).asJsonObject
        val call = part.getAsJsonObject("functionCall")

        assertThat(ThoughtSignatures.fromGeminiPart(part, call)).isEqualTo("sig-1")

        val replay = ThoughtSignatures.geminiPart("Read", """{"path":"index.html"}""", "sig-1")
        assertThat(replay.getAsJsonObject("functionCall").get("name").asString).isEqualTo("Read")
        assertThat(replay.get("thoughtSignature").asString).isEqualTo("sig-1")
    }

    @Test
    fun `gemini replay omits the signature when the model did not send one`() {
        val replay = ThoughtSignatures.geminiPart("Read", "{}", null)
        assertThat(replay.has("thoughtSignature")).isFalse()
    }

    @Test
    fun `openai tool call signature round-trips through extra_content`() {
        val inbound = JsonParser.parseString(
            """{"id":"call_1","extra_content":{"google":{"thought_signature":"sig-2"}}}"""
        ).asJsonObject
        assertThat(ThoughtSignatures.fromOpenAiToolCall(inbound)).isEqualTo("sig-2")

        val outbound = JsonObject()
        ThoughtSignatures.attachOpenAi(outbound, "sig-2")
        assertThat(
            outbound.getAsJsonObject("extra_content").getAsJsonObject("google")
                .get("thought_signature").asString
        ).isEqualTo("sig-2")
    }

    @Test
    fun `openai attach is a no-op without a signature`() {
        val outbound = JsonObject()
        ThoughtSignatures.attachOpenAi(outbound, null)
        assertThat(outbound.has("extra_content")).isFalse()
    }
}
