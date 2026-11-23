package com.webtoapp.core.agent.llm

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.AiProvider
import com.webtoapp.util.GsonProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

internal class OpenAiCompatProvider(@Suppress("UNUSED_PARAMETER") context: Context) : LlmProvider {
    private val gson = GsonProvider.gson
    private val sse = SseParser()

    override fun supports(provider: AiProvider) = provider != AiProvider.ANTHROPIC && provider != AiProvider.GOOGLE && provider != AiProvider.OLLAMA

    override fun supports(req: ChatRequest): Boolean {
        if (!supports(req.apiKey.provider)) return false
        if (req.apiKey.provider == AiProvider.CUSTOM) {
            return req.apiKey.apiFormat == com.webtoapp.data.model.ApiFormat.OPENAI_COMPATIBLE
        }
        return true
    }

    override fun chatStream(req: ChatRequest): Flow<LlmEvent> = callbackFlow {
        trySend(LlmEvent.Started)
        val inFlight = AtomicReference<Call?>()
        executeCall(req, isRetry = false, inFlight = inFlight)
        awaitClose { inFlight.get()?.cancel() }
    }

    private fun kotlinx.coroutines.channels.ProducerScope<LlmEvent>.executeCall(
        req: ChatRequest,
        isRetry: Boolean,
        inFlight: AtomicReference<Call?>
    ) {
        val effectiveReq = if (isRetry) req.copy(temperature = 1f) else req
        val body = buildBody(effectiveReq)
        val call = HttpHelpers.newChatCall(effectiveReq, gson.toJson(body).toRequestBody("application/json".toMediaType()))
        inFlight.set(call)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!isRetry) {
                    AppLogger.w("OpenAiCompatProvider", "Network failure, retrying once: ${e.message}")
                    try { Thread.sleep(1000) } catch (_: InterruptedException) {}
                    executeCall(req, isRetry = true, inFlight = inFlight)
                } else {
                    trySend(LlmEvent.Error("Network error: ${e.message ?: "connection failed"} [${call.request().url}]")); close()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val eb = runCatching { response.body?.string() }.getOrNull().orEmpty()
                    response.body?.close()
                    if (!isRetry && response.code == 400 && looksLikeTemperatureConstraint(eb)) {
                        AppLogger.w("OpenAiCompatProvider", "Model rejected temperature=${req.temperature}; retrying with temperature=1")
                        executeCall(req, isRetry = true, inFlight = inFlight)
                        return
                    }
                    trySend(HttpHelpers.llmHttpError(response.code, eb, response.header("Retry-After"), response.header("retry-after-ms"))); close(); return
                }
                try {
                    val source = response.body?.source() ?: run { trySend(LlmEvent.Error("Empty response body")); close(); return }
                    val toolArgsAccum = LinkedHashMap<String, StringBuilder>()
                    val toolNames = LinkedHashMap<String, String>()
                    val toolSignatures = HashMap<String, String>()
                    val toolIndexToId = HashMap<Int, String>()
                    var finishReason = FinishReason.STOP
                    var done = false
                    sse.consume(source) { _, payload ->
                        if (payload == "[DONE]") { done = true; emitFinish(toolNames, toolArgsAccum, toolSignatures, finishReason); return@consume false }
                        try {
                            val json = JsonParser.parseString(payload).asJsonObject
                            json.getAsJsonObject("error")?.let { err ->
                                val msg = err.get("message")?.takeUnless { it.isJsonNull }?.asString ?: "API error"
                                trySend(LlmEvent.Error(msg)); return@consume false
                            }
                            val choice = json.getAsJsonArray("choices")?.takeIf { it.size() > 0 }?.get(0)?.asJsonObject ?: return@consume true
                            val delta = choice.getAsJsonObject("delta") ?: choice.getAsJsonObject("message") ?: return@consume true

                            runCatching {
                                sequenceOf("reasoning_content","thinking","reasoning").mapNotNull { delta.get(it) }.firstOrNull()?.let { el ->
                                    if (!el.isJsonNull && el.isJsonPrimitive) el.asString.takeIf { it.isNotEmpty() }?.let { trySend(LlmEvent.ThinkingDelta(it)) }
                                }
                            }
                            runCatching {
                                delta.get("content")?.takeUnless { it.isJsonNull }?.let { el ->
                                    if (el.isJsonPrimitive) el.asString.takeIf { it.isNotEmpty() }?.let { trySend(LlmEvent.TextDelta(it)) }
                                }
                            }
                            runCatching {
                                delta.getAsJsonArray("tool_calls")?.forEach { tcEl ->
                                    val tc = tcEl.asJsonObject; val idx = tc.get("index")?.takeUnless { it.isJsonNull }?.asInt ?: 0
                                    val id = tc.get("id")?.takeUnless { it.isJsonNull }?.asString ?: toolIndexToId[idx] ?: "call_$idx"
                                    toolIndexToId[idx] = id
                                    val func = tc.getAsJsonObject("function")
                                    val name = func?.get("name")?.takeUnless { it.isJsonNull }?.asString?.takeIf { it.isNotEmpty() }
                                    val args = func?.get("arguments")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
                                    if (name != null) {
                                        if (toolNames[id] == null) { toolNames[id] = name; trySend(LlmEvent.ToolCallBegin(id, name)) }
                                    }
                                    if (args.isNotEmpty()) { toolArgsAccum.getOrPut(id) { StringBuilder() }.append(args); trySend(LlmEvent.ToolCallArgsDelta(id, args)) }
                                    ThoughtSignatures.fromOpenAiToolCall(tc)?.let { toolSignatures[id] = it }
                                }
                            }
                            runCatching {
                                choice.get("finish_reason")?.takeUnless { it.isJsonNull }?.asString?.let { r ->
                                    finishReason = when (r) { "tool_calls","function_call" -> FinishReason.TOOL_CALLS; "length" -> FinishReason.LENGTH; else -> FinishReason.STOP }
                                }
                            }
                        } catch (e: Exception) {
                            com.webtoapp.core.logging.AppLogger.w("OpenAiCompatProvider", "SSE chunk parse failed (non-fatal): ${e.message} | payload: $payload")
                        }
                        true
                    }
                    if (!done) emitFinish(toolNames, toolArgsAccum, toolSignatures, finishReason)
                } catch (e: Exception) {
                    AppLogger.w("OpenAiCompatProvider", "stream interrupted: ${e.message}")
                    trySend(LlmEvent.Error("Stream interrupted: ${e.message}", recoverable = true))
                } finally { runCatching { response.body?.close() }; close() }
            }
        })
    }

    private fun looksLikeTemperatureConstraint(errorBody: String): Boolean {
        val lower = errorBody.lowercase()
        return "temperature" in lower && ("only 1 is allowed" in lower || "must be" in lower || "invalid" in lower)
    }

    private fun kotlinx.coroutines.channels.ProducerScope<LlmEvent>.emitFinish(
        names: Map<String, String>,
        args: Map<String, StringBuilder>,
        signatures: Map<String, String>,
        fr: FinishReason
    ) {
        names.forEach { (id, name) ->
            trySend(LlmEvent.ToolCallEnd(id, name, args[id]?.toString().orEmpty(), signatures[id]))
        }
        trySend(LlmEvent.Done(if (names.isNotEmpty() && fr == FinishReason.STOP) FinishReason.TOOL_CALLS else fr))
    }

    private fun buildBody(req: ChatRequest) = JsonObject().apply {
        addProperty("model", req.model.id); addProperty("stream", true); addProperty("temperature", req.temperature)
        req.maxTokens?.let { addProperty("max_tokens", it) }
        add("messages", JsonArray().apply { req.messages.forEach { msg -> add(buildMsg(msg)) } })
        if (req.useTools && req.tools.isNotEmpty()) {
            add("tools", JsonArray().apply { req.tools.forEach { t -> add(buildTool(t)) } })
            addProperty("tool_choice", "auto")
        }
    }
    private fun buildMsg(msg: LlmMessage) = JsonObject().apply {
        addProperty("role", when(msg.role){LlmMessage.Role.SYSTEM->"system";LlmMessage.Role.USER->"user";LlmMessage.Role.ASSISTANT->"assistant";LlmMessage.Role.TOOL->"tool"})
        if (msg.toolCalls.isNotEmpty()) {
            add("tool_calls", JsonArray().apply {
                msg.toolCalls.forEach { tc ->
                    add(JsonObject().apply {
                        addProperty("id", tc.id)
                        addProperty("type", "function")
                        add("function", JsonObject().apply {
                            addProperty("name", tc.name)
                            addProperty("arguments", tc.argumentsJson)
                        })
                        ThoughtSignatures.attachOpenAi(this, tc.thoughtSignature)
                    })
                }
            })
            if (msg.content.isNotEmpty()) addProperty("content", msg.content)
        }
        else if (msg.images.isNotEmpty() && msg.role != LlmMessage.Role.TOOL) {
            add("content", JsonArray().apply {
                if (msg.content.isNotEmpty()) add(JsonObject().apply { addProperty("type", "text"); addProperty("text", msg.content) })
                msg.images.forEach { img ->
                    add(JsonObject().apply {
                        addProperty("type", "image_url")
                        add("image_url", JsonObject().apply {
                            addProperty("url", "data:${img.mimeType};base64,${base64(img.bytes)}")
                        })
                    })
                }
            })
        }
        else addProperty("content", msg.content)

        if (msg.role == LlmMessage.Role.TOOL) {
            if (msg.images.isNotEmpty() && msg.content.isEmpty()) {
                addProperty("content", "[${msg.images.size} image(s) returned by tool; not displayable in this message role]")
            }
        }

        if (msg.role == LlmMessage.Role.ASSISTANT) {
            addProperty("reasoning_content", msg.reasoningContent ?: "")
        }
        msg.toolCallId?.let { addProperty("tool_call_id", it) }; msg.name?.let { addProperty("name", it) }
    }
    private fun base64(bytes: ByteArray) = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    private fun buildTool(t: ToolDeclaration) = JsonObject().apply { addProperty("type","function"); add("function", JsonObject().apply { addProperty("name",t.name);addProperty("description",t.description);add("parameters",t.parametersSchema) }) }
}
