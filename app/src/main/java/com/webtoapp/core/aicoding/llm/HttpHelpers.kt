package com.webtoapp.core.aicoding.llm

import com.webtoapp.data.model.AiProvider
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
        when (apiKey.provider) {
            AiProvider.ANTHROPIC -> { builder.header("x-api-key", key); builder.header("anthropic-version", "2023-06-01") }
            AiProvider.GLM -> builder.header("Authorization", key)
            AiProvider.OLLAMA, AiProvider.LM_STUDIO, AiProvider.VLLM -> { if (key.isNotBlank()) builder.header("Authorization", "Bearer $key") }
            else -> builder.header("Authorization", "Bearer $key")
        }
    }

    fun classifyHttpError(code: Int, body: String): Pair<String, Boolean> {
        val lower = body.lowercase()
        val toolRelated = "tool" in lower || "function" in lower
        val msg = when (code) {
            400 -> "Bad request (400): ${body.take(300)}"
            401 -> "API key invalid or expired (401)"
            403 -> "Access denied (403)"
            404 -> "Endpoint or model not found (404)"
            429 -> "Rate limited (429)"
            in 500..599 -> "Server error ($code)"
            else -> "Request failed ($code): ${body.take(300)}"
        }
        return msg to (code == 400 && toolRelated)
    }
}
