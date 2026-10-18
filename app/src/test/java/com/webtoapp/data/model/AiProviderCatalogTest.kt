package com.webtoapp.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AiProviderCatalogTest {

    @Test
    fun `every provider category is capped at three entries`() {
        // Issue: the AI settings catalog had grown to 32 providers. The trim policy caps
        // each catalog category at three well-known entries.
        val counts = AiProvider.entries.groupBy { it.category }.mapValues { (_, v) -> v.size }
        counts.forEach { (category, count) ->
            if (category != ProviderCategory.CUSTOM) {
                assertThat(count).isAtMost(3)
            }
        }
        assertThat(counts[ProviderCategory.RECOMMENDED]).isAtMost(3)
        assertThat(counts[ProviderCategory.INTERNATIONAL]).isAtMost(3)
        assertThat(counts[ProviderCategory.AGGREGATOR]).isAtMost(3)
        assertThat(counts[ProviderCategory.CHINESE]).isAtMost(3)
    }

    @Test
    fun `removed providers expose their legacy base urls for migration`() {
        assertThat(AiProvider.REMOVED_PROVIDER_BASE_URLS).containsKey("GROQ")
        assertThat(AiProvider.REMOVED_PROVIDER_BASE_URLS["GROQ"]).isEqualTo("https://api.groq.com/openai")
        assertThat(AiProvider.REMOVED_PROVIDER_BASE_URLS).doesNotContainKey("OPENAI")
    }

    @Test
    fun `custom chat endpoint follows the selected api format`() {
        fun config(format: ApiFormat, override: String? = null) = ApiKeyConfig(
            provider = AiProvider.CUSTOM,
            apiKey = "sk-test",
            baseUrl = "https://api.example.com",
            customChatEndpoint = override,
            apiFormat = format
        )

        assertThat(config(ApiFormat.OPENAI_COMPATIBLE).getEffectiveChatEndpoint())
            .isEqualTo("/v1/chat/completions")
        assertThat(config(ApiFormat.ANTHROPIC).getEffectiveChatEndpoint())
            .isEqualTo("/v1/messages")
        assertThat(config(ApiFormat.OPENAI_RESPONSES).getEffectiveChatEndpoint())
            .isEqualTo("/v1/responses")
        // An explicit endpoint override always wins over the format default.
        assertThat(config(ApiFormat.ANTHROPIC, override = "/api/messages").getEffectiveChatEndpoint())
            .isEqualTo("/api/messages")
    }

    @Test
    fun `glm chat endpoint is the v4 completions path`() {
        val config = ApiKeyConfig(provider = AiProvider.GLM, apiKey = "sk-test")
        assertThat(config.getEffectiveModelsEndpoint()).isEqualTo("/v4/models")
        assertThat(config.getEffectiveChatEndpoint()).isEqualTo("/v4/chat/completions")
        assertThat(config.copy(customChatEndpoint = "/custom/chat").getEffectiveChatEndpoint())
            .isEqualTo("/custom/chat")
    }

    @Test
    fun `api format display names carry the wire endpoints`() {
        assertThat(ApiFormat.ANTHROPIC.displayName).contains("/v1/messages")
        assertThat(ApiFormat.OPENAI_COMPATIBLE.displayName).contains("/chat/completions")
        assertThat(ApiFormat.OPENAI_RESPONSES.displayName).contains("/responses")
    }
}
