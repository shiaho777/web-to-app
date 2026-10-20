package com.webtoapp.core.agent.llm

import com.webtoapp.core.ai.ApiKeyRotation
import com.webtoapp.core.ai.RatedFailure
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiFormat
import com.webtoapp.data.model.ApiKeyConfig
import okhttp3.Request

internal object HttpHelpers {
    fun baseUrl(apiKey: ApiKeyConfig): String =
        (apiKey.baseUrl?.takeIf { it.isNotBlank() } ?: apiKey.provider.baseUrl).trimEnd('/')

    fun joinUrl(base: String, endpoint: String): String {
        val b = base.trimEnd('/'); val e = endpoint.trimStart('/')
        val parts = e.split("/").filter { it.isNotEmpty() }
        if (parts.isNotEmpty() && b.endsWith("/${parts.first()}")) {
            val rest = parts.drop(1).joinToString("/")
            return if (rest.isEmpty()) b else "$b/$rest"
        }
        return "$b/$e"
    }

    fun applyAuth(builder: Request.Builder, apiKey: ApiKeyConfig) {
        val key = apiKey.apiKey.trim().replace("\n", "").replace("\r", "")
        // A CUSTOM endpoint speaking the Anthropic Messages protocol authenticates with
        // x-api-key + anthropic-version, not a Bearer header.
        val anthropicProtocol = apiKey.provider == AiProvider.ANTHROPIC ||
            (apiKey.provider == AiProvider.CUSTOM && apiKey.apiFormat == ApiFormat.ANTHROPIC)
        when {
            anthropicProtocol -> {
                builder.header("x-api-key", key)
                builder.header("anthropic-version", "2023-06-01")
            }
            apiKey.provider == AiProvider.OLLAMA || apiKey.provider == AiProvider.LM_STUDIO ||
                apiKey.provider == AiProvider.VLLM -> {
                if (key.isNotBlank()) builder.header("Authorization", "Bearer $key")
            }
            else -> builder.header("Authorization", "Bearer $key")
        }
    }

    fun classifyHttpError(code: Int, body: String): Pair<String, Boolean> {
        val failure = ApiKeyRotation.describe(code, body, null)
        return failure.message to failure.recoverable
    }

    fun describeHttp(
        code: Int,
        body: String,
        retryAfterHeader: String?,
        retryAfterMsHeader: String? = null
    ): RatedFailure = ApiKeyRotation.describe(code, body, retryAfterHeader, retryAfterMsHeader)

    fun llmHttpError(
        code: Int,
        body: String,
        retryAfterHeader: String?,
        retryAfterMsHeader: String? = null
    ): LlmEvent.Error {
        val failure = describeHttp(code, body, retryAfterHeader, retryAfterMsHeader)
        return LlmEvent.Error(
            message = failure.message,
            recoverable = failure.recoverable,
            retryAfterMs = failure.retryAfterMs,
            httpCode = failure.httpCode,
            limitScope = failure.limitScope
        )
    }

    fun parseRetryAfterMs(headerValue: String?): Long? = ApiKeyRotation.parseRetryAfterHeader(headerValue)
}
