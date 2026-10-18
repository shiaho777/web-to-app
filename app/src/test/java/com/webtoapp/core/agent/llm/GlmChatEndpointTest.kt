package com.webtoapp.core.agent.llm

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiKeyConfig
import org.junit.Test

class GlmChatEndpointTest {

    @Test
    fun `preset glm key posts to the official chat completions url`() {
        val config = ApiKeyConfig(provider = AiProvider.GLM, apiKey = "sk-test")
        val url = HttpHelpers.joinUrl(HttpHelpers.baseUrl(config), config.getEffectiveChatEndpoint())
        assertThat(url).isEqualTo("https://open.bigmodel.cn/api/paas/v4/chat/completions")
    }

    @Test
    fun `base that already ends in v4 is not doubled`() {
        val config = ApiKeyConfig(
            provider = AiProvider.GLM,
            apiKey = "sk-test",
            baseUrl = "https://open.bigmodel.cn/api/paas/v4/"
        )
        val url = HttpHelpers.joinUrl(HttpHelpers.baseUrl(config), config.getEffectiveChatEndpoint())
        assertThat(url).isEqualTo("https://open.bigmodel.cn/api/paas/v4/chat/completions")
    }
}
