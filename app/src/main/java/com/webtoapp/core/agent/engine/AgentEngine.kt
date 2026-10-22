package com.webtoapp.core.agent.engine

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.agent.llm.ChatRequest
import com.webtoapp.core.agent.llm.FinishReason
import com.webtoapp.core.agent.llm.LlmEvent
import com.webtoapp.core.agent.llm.LlmGateway
import com.webtoapp.core.agent.llm.LlmMessage
import com.webtoapp.core.agent.llm.LlmToolCall
import com.webtoapp.core.agent.llm.ToolDeclaration
import com.webtoapp.core.ai.ApiKeyRotation
import com.webtoapp.core.ai.LimitScope
import com.webtoapp.core.agent.permission.PermissionChecker
import com.webtoapp.core.agent.permission.PermissionDecision
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolRegistry
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.withTimeoutOrNull

class AgentEngine(
    private val gateway: LlmGateway,
    private val permissionChecker: PermissionChecker,
    private val abortController: AbortController = AbortController(),
    private val keyRotation: ApiKeyRotation = ApiKeyRotation.shared
) {

    data class Input(
        val systemPrompt: String,
        val history: List<LlmMessage>,
        val userMessage: String,
        val toolContext: ToolContext,
        val registry: ToolRegistry,
        val temperature: Float = 0.7f,
        val maxTurns: Int = 24,
        val maxTokens: Int? = null
    )

    fun run(input: Input): Flow<AgentEvent> = channelFlow {
        send(AgentEvent.Started)

        val messages = mutableListOf<LlmMessage>()
        messages += LlmMessage(LlmMessage.Role.SYSTEM, input.systemPrompt)
        messages += input.history

        if (input.userMessage.isNotEmpty()) {
            messages += LlmMessage(LlmMessage.Role.USER, input.userMessage)
        }

        // Only multimodal (vision) models can accept image parts; strip them for
        // text-only models so attachments never break a non-vision chat request.
        val supportsVision = input.toolContext.textModel.model.capabilities
            .contains(com.webtoapp.data.model.ModelCapability.MULTIMODAL)

        var totalToolCalls = 0
        val accText = StringBuilder()
        val maxContinuations = 3
        var rateLimitRetries = 0

        try {
            for (turn in 1..input.maxTurns) {
                if (abortController.aborted) { send(AgentEvent.Aborted); return@channelFlow }

                val allowedNames = permissionChecker.allowedToolNames()
                val declarations = input.registry.all
                    .filter { allowedNames == null || it.name in allowedNames }
                    .map { tool -> ToolDeclaration(tool.name, tool.description, tool.parametersSchema) }

                val turnText = StringBuilder()

                val turnThinking = StringBuilder()
                val pending = LinkedHashMap<String, Pair<String, StringBuilder>>()
                val thoughtSignatures = HashMap<String, String>()
                // Index in accText where this attempt's output begins; a recoverable-error
                // retry rewrites from here instead of appending a duplicate attempt.
                // RETRY-PATH ONLY: inside the event handlers accText is append-only —
                // rebuilding per event truncated the accumulated buffer to the turn start
                // and left `accumulated` holding just the newest fragment (#749 regression).
                var attemptStartedIndex = accText.length
                fun rebuildAccFromPrefix() {
                    if (accText.length > attemptStartedIndex) {
                        accText.setLength(attemptStartedIndex)
                    }
                }
                var finishReason = FinishReason.STOP
                var hardError: String? = null
                var continuationCount = 0
                var attemptError: String? = null
                var attemptRetryAfter: Long? = null
                var attemptHttp: Int? = null
                var attemptScope: LimitScope? = null
                var attemptRecoverable = false
                var attempts = 0
                val boundKey = input.toolContext.textApiKey
                val textPool = input.toolContext.textKeyPool.ifEmpty { listOf(boundKey) }
                val modelId = input.toolContext.textModel.model.id
                var textKey = boundKey
                val attemptCap = textPool.size + ApiKeyRotation.MAX_SAME_KEY_RETRIES + 1

                do {
                    if (abortController.aborted) { send(AgentEvent.Aborted); return@channelFlow }
                    if (++attempts > attemptCap) {
                        hardError = attemptError ?: Strings.agentNoReadyApiKey
                        break
                    }

                    val picked = keyRotation.select(boundKey, textPool, modelId)
                    if (picked == null) {
                        val wait = keyRotation.soonestWaitMs(boundKey, textPool, modelId)
                        if (wait != null && wait <= ApiKeyRotation.MAX_INLINE_WAIT_MS &&
                            rateLimitRetries < ApiKeyRotation.MAX_SAME_KEY_RETRIES
                        ) {
                            rateLimitRetries++
                            send(AgentEvent.Notice(Strings.agentRateLimitRetry(
                                rateLimitRetries, ApiKeyRotation.MAX_SAME_KEY_RETRIES, wait
                            )))
                            delay(wait)
                            continue
                        }
                        val minutes = wait?.let { ((it + 59_999) / 60_000).coerceAtLeast(1) }
                        hardError = attemptError
                            ?: if (minutes != null) Strings.agentKeysCooling(minutes) else Strings.agentNoReadyApiKey
                        break
                    }
                    textKey = picked
                    attemptError = null
                    attemptRetryAfter = null
                    attemptHttp = null
                    attemptScope = null
                    attemptRecoverable = false

                    val isContinuation = continuationCount > 0
                    val baseMessages = if (isContinuation && turnText.isNotEmpty()) {
                        messages + LlmMessage(
                            role = LlmMessage.Role.ASSISTANT,
                            content = turnText.toString(),
                            reasoningContent = turnThinking.toString().takeIf { it.isNotEmpty() }
                        )
                    } else {
                        messages.toList()
                    }
                    val requestMessages = if (supportsVision) baseMessages
                        else baseMessages.map { if (it.images.isEmpty()) it else it.copy(images = emptyList()) }

                    // Collect the stream exactly once per request attempt. The gateway
                    // flows are cold callbackFlows: re-collecting them re-runs the
                    // producer block, i.e. fires a brand-new HTTP request per event,
                    // while the previous request's response events are dropped — the
                    // turn then hangs forever on "thinking" (the #742 regression).
                    gateway.chatStream(
                        ChatRequest(
                            apiKey = textKey,
                            model = input.toolContext.textModel.model,
                            messages = requestMessages,
                            tools = declarations,
                            temperature = input.temperature,
                            maxTokens = input.maxTokens,
                            useTools = !isContinuation
                        )
                    ).collectWithIdleTimeout(
                        idleTimeoutMs = STREAM_IDLE_TIMEOUT_MS,
                        onTimeout = {
                            attemptError = "LLM stream went idle for ${STREAM_IDLE_TIMEOUT_MS / 1000}s"
                            attemptRetryAfter = null
                            attemptHttp = null
                            attemptScope = null
                            attemptRecoverable = true
                        }
                    ) { ev ->
                        when (ev) {
                            is LlmEvent.Started -> Unit
                            is LlmEvent.TextDelta -> {
                                // Append-only within an attempt. Calling rebuildAccFromPrefix()
                                // here (as #749 briefly did) truncated accText back to the
                                // turn start on EVERY delta, so `accumulated` held only the
                                // newest fragment: the live timeline rendered the body in
                                // replacing chunks and the persisted message ended up as the
                                // last delta alone.
                                turnText.append(ev.delta)
                                accText.append(ev.delta)
                                send(AgentEvent.TextDelta(ev.delta, accText.toString()))
                            }
                            is LlmEvent.ThinkingDelta -> {
                                // On the first delta of a turn's reasoning stream, anchor a
                                // thinking marker in the accumulated text (mirrors the tool
                                // marker) so the timeline can interleave this block with
                                // prose/tools in the order they actually occurred.
                                if (turnThinking.isEmpty()) {
                                    val segmentId = "th-turn-$turn"
                                    val marker = "⁣TH:$segmentId⁣"
                                    accText.append(marker)
                                    send(AgentEvent.TextDelta(marker, accText.toString()))
                                }
                                turnThinking.append(ev.delta)
                                send(AgentEvent.ThinkingDelta("th-turn-$turn", ev.delta, turnThinking.toString()))
                            }
                            is LlmEvent.ToolCallBegin -> {
                                pending[ev.id] = ev.name to StringBuilder()

                                val marker = "⁣TC:${ev.id}⁣"
                                accText.append(marker)
                                send(AgentEvent.TextDelta(marker, accText.toString()))
                                send(AgentEvent.ToolCallStarted(ev.id, ev.name))
                            }
                            is LlmEvent.ToolCallArgsDelta -> {
                                pending[ev.id]?.second?.append(ev.argsDelta)
                                send(AgentEvent.ToolCallArgsDelta(ev.id, ev.argsDelta))
                            }
                            is LlmEvent.ToolCallEnd -> {
                                if (ev.name.isNotEmpty()) {
                                    val entry = pending.getOrPut(ev.id) { ev.name to StringBuilder() }
                                    if (ev.argumentsJson.length >= entry.second.length) {
                                        entry.second.clear()
                                        entry.second.append(ev.argumentsJson)
                                    }
                                }
                                ev.thoughtSignature?.takeIf { it.isNotBlank() }?.let {
                                    thoughtSignatures[ev.id] = it
                                }
                            }
                            is LlmEvent.Done -> finishReason = ev.finishReason
                            is LlmEvent.Error -> {
                                val rotatable = ev.httpCode == 401 || ev.httpCode == 403 ||
                                    ev.httpCode == 429 || ev.limitScope != null
                                if (!ev.recoverable && !rotatable) {
                                    hardError = ev.message
                                } else {
                                    attemptError = ev.message
                                    attemptRetryAfter = ev.retryAfterMs
                                    attemptHttp = ev.httpCode
                                    attemptScope = ev.limitScope
                                    attemptRecoverable = ev.recoverable || rotatable
                                }
                            }
                        }
                    }

                    if (hardError != null) break
                    if (attemptError != null) {
                        when (val decision = keyRotation.onFailure(
                            current = textKey,
                            bound = boundKey,
                            pool = textPool,
                            modelId = modelId,
                            httpCode = attemptHttp,
                            retryAfterMs = attemptRetryAfter,
                            limitScope = attemptScope,
                            recoverable = attemptRecoverable,
                            sameKeyRetries = rateLimitRetries
                        )) {
                            is ApiKeyRotation.KeyDecision.Switch -> {
                                AppLogger.i(TAG, "API key cooled, switching to ${decision.masked}")
                                send(AgentEvent.Notice(Strings.agentKeySwitched(decision.masked)))
                                // Drop this attempt's partial output before the next key
                                // speaks, or the timeline and the saved message repeat it.
                                rebuildAccFromPrefix()
                                turnText.setLength(0)
                                turnThinking.setLength(0)
                                pending.clear()
                                thoughtSignatures.clear()
                                finishReason = FinishReason.STOP
                                continue
                            }
                            is ApiKeyRotation.KeyDecision.Backoff -> {
                                rateLimitRetries = decision.sameKeyRetries
                                send(AgentEvent.Notice(Strings.agentRateLimitRetry(
                                    rateLimitRetries, ApiKeyRotation.MAX_SAME_KEY_RETRIES, decision.waitMs
                                )))
                                rebuildAccFromPrefix()
                                turnText.setLength(0)
                                turnThinking.setLength(0)
                                pending.clear()
                                thoughtSignatures.clear()
                                finishReason = FinishReason.STOP
                                delay(decision.waitMs)
                                continue
                            }
                            is ApiKeyRotation.KeyDecision.Fail -> {
                                hardError = attemptError
                                break
                            }
                        }
                    }
                    if (finishReason == FinishReason.LENGTH && continuationCount < maxContinuations && turnText.isNotEmpty()) {
                        continuationCount++
                        send(AgentEvent.Notice(Strings.agentContinuing))
                        continue
                    }
                    break
                } while (true)

                if (hardError != null) { send(AgentEvent.Failed(hardError!!)); return@channelFlow }

                // This turn's reasoning stream is done (text/tools come next, or the
                // turn ends). Freeze it so the UI can start a fresh live block on the
                // next turn instead of piling into one shared buffer.
                if (turnThinking.isNotEmpty()) send(AgentEvent.ThinkingTurnEnded)

                if (finishReason == FinishReason.LENGTH && continuationCount >= maxContinuations) {
                    send(AgentEvent.Notice(Strings.agentOutputTruncated))
                }

                // Keep the raw (pre-sanitised) arguments per call: a truncated
                // arguments stream must be reported to the model as such, not
                // silently coerced to "{}" and executed with missing params.
                val rawArgsById = HashMap<String, String>()
                val assistantToolCalls = pending.entries.map { (id, pair) ->
                    rawArgsById[id] = pair.second.toString()
                    LlmToolCall(
                        id,
                        pair.first,
                        sanitizeArgumentsJson(pair.second.toString()),
                        thoughtSignatures[id]
                    )
                }
                messages += LlmMessage(
                    role = LlmMessage.Role.ASSISTANT,
                    content = turnText.toString(),
                    toolCalls = assistantToolCalls,
                    reasoningContent = turnThinking.toString().takeIf { it.isNotEmpty() }
                )

                if (assistantToolCalls.isEmpty()) {
                    // Zero text and zero tool calls is a real outcome (some gateways
                    // open the stream, emit Done, and close without any delta). An
                    // empty summary made the turn LOOK successful while persisting
                    // nothing — surface a distinct, diagnosable message instead.
                    send(AgentEvent.Completed(
                        summary = turnText.toString().trim()
                            .ifEmpty { accText.toString().trim() }
                            .ifEmpty { Strings.agentEmptyResponse },
                        toolCallCount = totalToolCalls
                    ))
                    return@channelFlow
                }

                val batches = batchToolCalls(assistantToolCalls, input.registry)
                val toolMessages = mutableListOf<LlmMessage>()

                for (batch in batches) {
                    if (abortController.aborted) { send(AgentEvent.Aborted); return@channelFlow }
                    if (batch.parallel) {
                        val results = runParallel(batch.calls, input, channel, rawArgsById)
                        for ((call, result) in results) {
                            totalToolCalls++
                            emitToolFinish(call, result, channel)
                            toolMessages += LlmMessage(
                                role = LlmMessage.Role.TOOL,
                                content = trimToolText(result.text).ifEmpty { NO_TOOL_OUTPUT },
                                toolCallId = call.id,
                                name = call.name,
                                images = result.images
                            )
                            if (result.planReviewPath != null) {
                                send(AgentEvent.PlanReviewRequired(result.planReviewPath))
                                send(AgentEvent.Completed(
                                    summary = accText.toString().trim().ifEmpty { "Plan submitted for review." },
                                    toolCallCount = totalToolCalls
                                ))
                                return@channelFlow
                            }
                        }
                    } else {
                        for (call in batch.calls) {
                            if (abortController.aborted) { send(AgentEvent.Aborted); return@channelFlow }
                            val result = runSequential(call, input, channel, rawArgsById)
                            totalToolCalls++
                            emitToolFinish(call, result, channel)
                            toolMessages += LlmMessage(
                                role = LlmMessage.Role.TOOL,
                                content = trimToolText(result.text).ifEmpty { NO_TOOL_OUTPUT },
                                toolCallId = call.id,
                                name = call.name,
                                images = result.images
                            )
                            if (result.planReviewPath != null) {
                                send(AgentEvent.PlanReviewRequired(result.planReviewPath))
                                send(AgentEvent.Completed(
                                    summary = accText.toString().trim().ifEmpty { "Plan submitted for review." },
                                    toolCallCount = totalToolCalls
                                ))
                                return@channelFlow
                            }
                        }
                    }
                }

                messages += toolMessages
            }

            send(AgentEvent.Completed(
                summary = accText.toString().trim().ifEmpty { "(reached max turns)" },
                toolCallCount = totalToolCalls
            ))
        } catch (e: AgentAbortedException) {
            send(AgentEvent.Aborted)
        } catch (t: Throwable) {
            if (t is kotlinx.coroutines.CancellationException) {
                // Cancellation is not an engine failure; rethrow so the collector's
                // own cancellation handling (and coroutine machinery) works normally.
                throw t
            }
            AppLogger.e(TAG, "engine crash: ${t.message}", t)
            send(AgentEvent.Failed(t.message ?: "engine error"))
        }
    }

    private suspend fun emitToolFinish(
        call: LlmToolCall,
        result: ToolResult,
        out: SendChannel<AgentEvent>
    ) {
        out.send(AgentEvent.ToolFinished(
            call.id, call.name, call.argumentsJson, result, call.thoughtSignature
        ))
        result.fileChange?.let { out.send(AgentEvent.FileChanged(it)) }
        result.appChange?.let { out.send(AgentEvent.AppChanged(it)) }
        result.builtApk?.let { out.send(AgentEvent.ApkBuilt(it)) }
    }

    private suspend fun runSequential(
        call: LlmToolCall,
        input: Input,
        out: SendChannel<AgentEvent>,
        rawArgsById: Map<String, String>
    ): ToolResult {
        val tool = input.registry[call.name]
            ?: return ToolResult.error("Unknown tool: ${call.name}")

        out.send(
            AgentEvent.ToolExecuting(
                toolCallId = call.id,
                name = call.name,
                // Guarded: several tools' activityDescription implementations read
                // args.get("x")?.asString, which throws for a JSON null / wrong type
                // (Gson never returns null from JsonNull.getAsString, it throws).
                // The same malformed args inside execute() below degrade to an error
                // ToolResult — an unguarded throw here instead kills the whole run.
                activity = runCatching { tool.activityDescription(parseArgs(call.argumentsJson)) }
                    .getOrNull() ?: call.name
            )
        )

        // Weak models frequently get cut off by the token limit mid-arguments,
        // leaving invalid JSON. Coercing that to "{}" and executing anyway just
        // produces confusing "missing parameter" errors the model cannot trace
        // back to the truncation — reject with the real reason instead.
        val rawArgs = rawArgsById[call.id]
        if (!rawArgs.isNullOrBlank() && !isValidJsonObject(rawArgs)) {
            return ToolResult.error(
                "${call.name}: arguments JSON was invalid or truncated (likely cut off by the " +
                    "token limit) and were NOT executed. Re-issue this tool call with complete arguments."
            )
        }

        val args = parseArgs(call.argumentsJson)

        val decision = permissionChecker.check(tool, args, input.toolContext)
        if (decision == PermissionDecision.Deny) {

            val mode = permissionChecker.mode
            val planFile = input.toolContext.effectivePlanFile()
            val hint = when {
                mode == com.webtoapp.core.agent.permission.PermissionMode.Plan && planFile != null ->
                    " — in plan mode, only Write/Edit to $planFile is allowed. " +
                        "Write the plan there and call ExitPlanMode."
                mode == com.webtoapp.core.agent.permission.PermissionMode.Plan ->
                    " — plan mode forbids this tool. Use Read / Glob / Grep / ListFiles / AskUserQuestion " +
                        "to investigate, then ExitPlanMode."
                mode == com.webtoapp.core.agent.permission.PermissionMode.Dream ->
                    " — dream mode only permits writes inside .memory/."
                else -> ""
            }
            return ToolResult.error("Permission denied: ${call.name}$hint")
        }

        val accumulated = StringBuilder()
        val callCtx = input.toolContext.copy(
            progress = { delta: String ->
                if (delta.isNotEmpty()) {
                    accumulated.append(delta)
                    val sent = out.trySend(
                        AgentEvent.ToolProgress(
                            toolCallId = call.id,
                            name = call.name,
                            delta = delta,
                            accumulated = accumulated.toString()
                        )
                    )

                    if (sent.isClosed) Unit
                }
            }
        )

        return try {
            tool.execute(args, callCtx)
        } catch (ce: CancellationException) {
            // AgentAbortedException is a CancellationException and must reach the engine's
            // outer catch (→ Aborted); swallowing real coroutine cancellation here would
            // keep the loop running inside an already-cancelled coroutine.
            throw ce
        } catch (t: Throwable) {
            // Gson's type getters throw UnsupportedOperationException with a
            // class-simple-name-only message (e.g. "Read: JsonNull"), which gives the
            // model nothing actionable — add the offending raw arguments so a wrong-typed
            // parameter is self-healing on the next turn.
            val rawHint = rawArgsById[call.id]?.take(200)
            ToolResult.error(
                "${call.name}: ${t.message ?: t::class.simpleName}" +
                    (if (rawHint != null) " (arguments: $rawHint)" else "")
            )
        }
    }

    private suspend fun runParallel(
        calls: List<LlmToolCall>,
        input: Input,
        out: SendChannel<AgentEvent>,
        rawArgsById: Map<String, String>
    ): List<Pair<LlmToolCall, ToolResult>> =
        coroutineScope {
            val deferred = calls.map { call ->
                async {
                    call to runSequential(call, input, out, rawArgsById)
                }
            }
            deferred.map { it.await() }
        }

    private fun batchToolCalls(calls: List<LlmToolCall>, registry: ToolRegistry): List<Batch> {
        val batches = mutableListOf<Batch>()
        for (call in calls) {
            val readOnly = registry[call.name]?.isReadOnly() == true
            val last = batches.lastOrNull()
            if (readOnly && last != null && last.parallel) {
                last.calls += call
            } else {
                batches += Batch(parallel = readOnly, calls = mutableListOf(call))
            }
        }
        return batches
    }

    private fun parseArgs(json: String): JsonObject = runCatching {
        if (json.isBlank()) JsonObject()
        else {
            val el = JsonParser.parseString(json)
            if (el.isJsonObject) el.asJsonObject else JsonObject()
        }
    }.getOrElse { JsonObject() }

    private fun sanitizeArgumentsJson(json: String): String {
        if (json.isBlank()) return "{}"
        val valid = runCatching {
            val el = JsonParser.parseString(json)
            el.isJsonObject
        }.getOrDefault(false)
        return if (valid) json else "{}"
    }

    private fun isValidJsonObject(json: String): Boolean = runCatching {
        JsonParser.parseString(json).isJsonObject
    }.getOrDefault(false)

    private fun trimToolText(text: String): String =
        if (text.length <= MAX_TOOL_RESULT_CHARS) text
        else text.substring(0, MAX_TOOL_RESULT_CHARS) + "\n… (tool result truncated)"

    private data class Batch(val parallel: Boolean, val calls: MutableList<LlmToolCall>)

    companion object {
        private const val TAG = "AgentEngine"
        private const val MAX_TOOL_RESULT_CHARS = 32_000

        // Some OpenAI-compat gateways reject tool messages with empty content;
        // always send a placeholder instead of "".
        private const val NO_TOOL_OUTPUT = "(no output)"
        // If the LLM stream emits nothing for this long, treat it as a stalled
        // connection and retry (some OpenAI-compat endpoints open the SSE channel,
        // send a partial response, then go silent without ever closing it). 90s is
        // generous enough for slow reasoning models that pause between chunks, but
        // short enough that a genuinely dead connection recovers in ~1.5 minutes
        // instead of the 10-minute OkHttp readTimeout.
        private const val STREAM_IDLE_TIMEOUT_MS = 90_000L
    }
}

/**
 * Collect [flow] but abort if it goes [idleTimeoutMs] without emitting any event.
 * Some OpenAI-compatible SSE endpoints open the channel, send a partial response
 * (e.g. the first reasoning chunk), then go silent without ever closing the
 * connection — which would otherwise hang the agent loop for the full OkHttp
 * read timeout (10 minutes). On idle timeout, [onTimeout] is invoked and the
 * collection ends normally so the engine's retry loop can take over.
 *
 * The flow is subscribed EXACTLY ONCE: it is turned into a [ReceiveChannel] via
 * [produceIn], and only the wait for the next element is wrapped in a fresh
 * [withTimeoutOrNull] window per event. Never re-collect the flow per event —
 * the provider flows are cold `callbackFlow`s, so each repeated collect re-runs
 * the producer block (a brand-new HTTP request per event) while the previous
 * request's response events are dropped into a cancelled channel. That was the
 * #742 regression: the UI sat on "thinking" forever with zero output while the
 * client silently spammed the API with duplicate requests.
 *
 * Cancelling (idle timeout, abort, or completion of the surrounding scope)
 * cancels the producer, which propagates into the provider's `awaitClose` and
 * tears down the underlying HTTP call.
 */
internal suspend fun <T> Flow<T>.collectWithIdleTimeout(
    idleTimeoutMs: Long,
    onTimeout: () -> Unit,
    action: suspend (T) -> Unit
) {
    val timeoutMs = idleTimeoutMs.coerceAtLeast(1L)
    coroutineScope {
        val events = produceIn(this)
        try {
            while (true) {
                val result = withTimeoutOrNull(timeoutMs) { events.receiveCatching() }
                when {
                    // A whole window elapsed with no event: the stream stalled. The
                    // finally below cancels the producer (and the HTTP call with it),
                    // so the engine's retry loop starts from a clean slate.
                    result == null -> {
                        onTimeout()
                        return@coroutineScope
                    }
                    result.isSuccess -> action(result.getOrThrow())
                    else -> {
                        // Channel closed: rethrow a producer failure, otherwise the
                        // stream ended normally.
                        result.exceptionOrNull()?.let { throw it }
                        return@coroutineScope
                    }
                }
            }
        } finally {
            // coroutineScope waits for children on block exit but does NOT cancel
            // them: a producer parked in awaitClose would hang the exit forever
            // unless cancelled here.
            events.cancel(null)
        }
    }
}
