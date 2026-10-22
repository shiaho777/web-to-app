package com.webtoapp.core.agent.llm

import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Gemini 3 function calls carry an opaque `thoughtSignature`. The next request
 * must send that exact value back on the same function-call part, or the API
 * rejects the turn with 400. OpenRouter stores it server-side; a direct Google
 * call or a thin relay does not.
 */
internal object ThoughtSignatures {

    fun fromGeminiPart(part: JsonObject, functionCall: JsonObject): String? =
        text(part, "thoughtSignature", "thought_signature")
            ?: text(functionCall, "thoughtSignature", "thought_signature")

    fun fromOpenAiToolCall(toolCall: JsonObject): String? {
        text(toolCall, "thought_signature", "thoughtSignature")?.let { return it }
        val google = child(child(toolCall, "extra_content"), "google") ?: return null
        return text(google, "thought_signature", "thoughtSignature")
    }

    fun geminiPart(name: String, argumentsJson: String, signature: String?): JsonObject =
        JsonObject().apply {
            add("functionCall", JsonObject().apply {
                addProperty("name", name)
                val parsed = runCatching { JsonParser.parseString(argumentsJson) }.getOrNull()
                add("args", if (parsed != null && !parsed.isJsonNull) parsed else JsonObject())
            })
            signature?.takeIf { it.isNotBlank() }?.let { addProperty("thoughtSignature", it) }
        }

    fun attachOpenAi(toolCall: JsonObject, signature: String?) {
        val sig = signature?.takeIf { it.isNotBlank() } ?: return
        val extra = child(toolCall, "extra_content") ?: JsonObject().also {
            toolCall.add("extra_content", it)
        }
        val google = child(extra, "google") ?: JsonObject().also { extra.add("google", it) }
        google.addProperty("thought_signature", sig)
    }

    private fun child(parent: JsonObject?, name: String): JsonObject? {
        val el = parent?.get(name) ?: return null
        return if (el.isJsonObject) el.asJsonObject else null
    }

    private fun text(obj: JsonObject, vararg names: String): String? {
        for (name in names) {
            val el = obj.get(name) ?: continue
            if (!el.isJsonPrimitive || el.isJsonNull) continue
            val value = el.asString
            if (value.isNotBlank()) return value
        }
        return null
    }
}
