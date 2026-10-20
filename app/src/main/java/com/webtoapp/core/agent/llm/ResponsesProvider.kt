package com.webtoapp.core.agent.llm

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.network.NetworkModule
import com.webtoapp.data.model.ApiFormat
import com.webtoapp.data.model.AiProvider
import com.webtoapp.util.GsonProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * OpenAI Responses API (/responses) streaming provider. Selected when a CUSTOM
 * endpoint declares [ApiFormat.OPENAI_RESPONSES]; the named providers all speak
 * Chat Completions and never route here.
 */
internal class ResponsesProvider(@Suppress("UNUSED_PARAMETER") context: Context) : LlmProvider {
    private val gson = GsonProvider.gson
    private val sse = SseParser()
    private val client get() = NetworkModule.streamingClient

    override fun supports(provider: AiProvider) = false

    override fun supports(req: ChatRequest): Boolean =
        req.apiKey.provider == AiProvider.CUSTOM && req.apiKey.apiFormat == ApiFormat.OPENAI_RESPONSES

    override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
        trySend(LlmEvent.Started)
        val url = HttpHelpers.joinUrl(HttpHelpers.baseUrl(req.apiKey), req.apiKey.getEffectiveChatEndpoint())
        val body = buildBody(req)
        val builder = Request.Builder().url(url).header("Content-Type", "application/json")
            .post(gson.toJson(body).toRequestBody("application/json".toMediaType()))
        HttpHelpers.applyAuth(builder, req.apiKey)
        val call = client.newCall(builder.build())
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                trySend(LlmEvent.Error(e.message ?: "Network error")); close()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val eb = runCatching { response.body?.string() }.getOrNull().orEmpty()
                    response.body?.close()
                    trySend(HttpHelpers.llmHttpError(response.code, eb, response.header("Retry-After"), response.header("retry-after-ms"))); close(); return
                }
                try {
                    val source = response.body?.source() ?: run { trySend(LlmEvent.Error("Empty response body")); close(); return }
                    // callId -> accumulated function arguments
                    val toolArgs = LinkedHashMap<String, StringBuilder>()
                    val toolNames = LinkedHashMap<String, String>()
                    var finishReason = FinishReason.STOP
                    sse.consume(source) { _, payload ->
                        try {
                            val json = JsonParser.parseString(payload).asJsonObject
                            when (json.get("type")?.asString) {
                                "response.output_text.delta" ->
                                    json.get("delta")?.takeUnless { it.isJsonNull }?.asString?.takeIf { it.isNotEmpty() }
                                        ?.let { trySend(LlmEvent.TextDelta(it)) }
                                "response.reasoning_summary_text.delta",
                                "response.reasoning_text.delta" ->
                                    json.get("delta")?.takeUnless { it.isJsonNull }?.asString?.takeIf { it.isNotEmpty() }
                                        ?.let { trySend(LlmEvent.ThinkingDelta(it)) }
                                "response.output_item.added" -> {
                                    val item = json.getAsJsonObject("item") ?: return@consume true
                                    if (item.get("type")?.asString == "function_call") {
                                        val id = item.get("call_id")?.takeUnless { it.isJsonNull }?.asString
                                            ?: item.get("id")?.asString ?: "call_${toolArgs.size}"
                                        val name = item.get("name")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
                                        toolNames[id] = name
                                        toolArgs.getOrPut(id) { StringBuilder() }
                                        trySend(LlmEvent.ToolCallBegin(id, name))
                                    }
                                }
                                "response.function_call_arguments.delta" -> {
                                    val id = json.get("item_id")?.asString ?: return@consume true
                                    val delta = json.get("delta")?.asString.orEmpty()
                                    if (delta.isNotEmpty()) {
                                        toolArgs.getOrPut(id) { StringBuilder() }.append(delta)
                                        trySend(LlmEvent.ToolCallArgsDelta(id, delta))
                                    }
                                }
                                "response.output_item.done" -> {
                                    val item = json.getAsJsonObject("item") ?: return@consume true
                                    if (item.get("type")?.asString == "function_call") {
                                        val id = item.get("call_id")?.takeUnless { it.isJsonNull }?.asString
                                            ?: item.get("id")?.asString ?: return@consume true
                                        val name = item.get("name")?.takeUnless { it.isJsonNull }?.asString
                                            ?: toolNames[id].orEmpty()
                                        // Prefer the final assembled arguments from the done item.
                                        val finalArgs = item.get("arguments")?.takeUnless { it.isJsonNull }?.asString
                                            ?: toolArgs[id]?.toString().orEmpty()
                                        toolArgs[id] = StringBuilder(finalArgs)
                                        trySend(LlmEvent.ToolCallEnd(id, name, finalArgs))
                                    }
                                }
                                "response.completed" -> {
                                    finishReason = if (toolArgs.isNotEmpty()) FinishReason.TOOL_CALLS else FinishReason.STOP
                                    emitFinish(toolNames, toolArgs, finishReason)
                                    return@consume false
                                }
                                "response.incomplete" -> {
                                    emitFinish(toolNames, toolArgs, FinishReason.LENGTH)
                                    return@consume false
                                }
                                "response.failed" -> {
                                    val err = json.getAsJsonObject("response")?.getAsJsonObject("error")
                                    trySend(LlmEvent.Error(err?.get("message")?.takeUnless { it.isJsonNull }?.asString ?: "Responses request failed"))
                                    return@consume false
                                }
                                "error" -> {
                                    trySend(LlmEvent.Error(json.get("message")?.takeUnless { it.isJsonNull }?.asString ?: "Responses stream error"))
                                    return@consume false
                                }
                            }
                        } catch (e: Exception) {
                            AppLogger.w("ResponsesProvider", "SSE chunk parse failed (non-fatal): ${e.message}")
                        }
                        true
                    }
                } catch (e: Exception) {
                    AppLogger.w("ResponsesProvider", "stream interrupted: ${e.message}")
                    trySend(LlmEvent.Error("Stream interrupted: ${e.message}", recoverable = true))
                } finally { runCatching { response.body?.close() }; close() }
            }
        })
        awaitClose { call.cancel() }
    }

    private fun kotlinx.coroutines.channels.ProducerScope<LlmEvent>.emitFinish(
        names: Map<String, String>,
        args: Map<String, StringBuilder>,
        fr: FinishReason
    ) {
        names.forEach { (id, name) ->
            trySend(LlmEvent.ToolCallEnd(id, name, args[id]?.toString().orEmpty()))
        }
        // Safety net: any function call that accumulated args without a recorded name.
        args.forEach { (id, argsBuilder) ->
            if (id !in names) trySend(LlmEvent.ToolCallEnd(id, "", argsBuilder.toString()))
        }
        trySend(LlmEvent.Done(fr))
    }

    private fun buildBody(req: ChatRequest) = JsonObject().apply {
        addProperty("model", req.model.id)
        addProperty("stream", true)
        addProperty("temperature", req.temperature)
        req.maxTokens?.let { addProperty("max_output_tokens", it) }

        val systemText = req.messages.filter { it.role == LlmMessage.Role.SYSTEM }.joinToString("\n\n") { it.content }
        if (systemText.isNotBlank()) addProperty("instructions", systemText)

        add("input", JsonArray().apply {
            req.messages.filter { it.role != LlmMessage.Role.SYSTEM }.forEach { msg ->
                when (msg.role) {
                    LlmMessage.Role.TOOL -> add(JsonObject().apply {
                        addProperty("type", "function_call_output")
                        addProperty("call_id", msg.toolCallId.orEmpty())
                        addProperty("output", msg.content)
                    })
                    LlmMessage.Role.ASSISTANT -> {
                        msg.toolCalls.forEach { tc ->
                            add(JsonObject().apply {
                                addProperty("type", "function_call")
                                addProperty("call_id", tc.id)
                                addProperty("name", tc.name)
                                addProperty("arguments", tc.argumentsJson)
                            })
                        }
                        if (msg.content.isNotEmpty() || msg.images.isNotEmpty()) {
                            add(buildMessageItem(msg, "output_text"))
                        }
                    }
                    else -> add(buildMessageItem(msg, "input_text"))
                }
            }
        })

        if (req.useTools && req.tools.isNotEmpty()) {
            add("tools", JsonArray().apply {
                req.tools.forEach { t ->
                    add(JsonObject().apply {
                        addProperty("type", "function")
                        addProperty("name", t.name)
                        addProperty("description", t.description)
                        add("parameters", t.parametersSchema)
                    })
                }
            })
            addProperty("tool_choice", "auto")
        }
    }

    private fun buildMessageItem(msg: LlmMessage, textPartType: String) = JsonObject().apply {
        addProperty("role", if (msg.role == LlmMessage.Role.ASSISTANT) "assistant" else "user")
        if (msg.images.isNotEmpty()) {
            add("content", JsonArray().apply {
                if (msg.content.isNotEmpty()) add(JsonObject().apply {
                    addProperty("type", textPartType)
                    addProperty("text", msg.content)
                })
                msg.images.forEach { img ->
                    add(JsonObject().apply {
                        addProperty("type", "input_image")
                        addProperty("image_url", "data:${img.mimeType};base64,${base64(img.bytes)}")
                    })
                }
            })
        } else {
            add("content", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("type", textPartType)
                    addProperty("text", msg.content)
                })
            })
        }
    }

    private fun base64(bytes: ByteArray) = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
}
