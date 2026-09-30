package com.webtoapp.core.agent.engine

import com.webtoapp.core.agent.llm.ChatRequest
import com.webtoapp.core.agent.llm.LlmEvent
import com.webtoapp.core.agent.llm.LlmGateway
import com.webtoapp.core.agent.llm.LlmMessage
import com.webtoapp.core.agent.session.AgentMessage
import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.SavedModel
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.fold

class CompactService(
    private val gateway: LlmGateway,

    private val nonCjkCharsPerToken: Float = 3f,

    private val minRecentMessages: Int = 8,

    private val minRecentTokens: Int = 8_000,

    private val autoCompactThresholdFraction: Float = 0.75f
) {

    fun shouldCompact(messages: List<AgentMessage>, contextLength: Int = 128_000): Boolean =
        estimateTokens(messages) >= (contextLength * autoCompactThresholdFraction).toInt()

    fun estimateTokens(messages: List<AgentMessage>): Int =
        messages.sumOf { estimateTokens(it) }

    /**
     * Estimate tokens for the *actual wire payload* — system prompt + wire-format
     * history + the user message + every tool declaration. The persisted-message
     * estimate alone misses the fixed envelope (system prompt, schemas, re-injected
     * mentions) and undercounts badly on CJK/code, which let oversized requests
     * sail straight into the provider's context-length 400.
     */
    fun estimateRequestTokens(
        systemPrompt: String,
        history: List<LlmMessage>,
        userMessage: String,
        tools: List<Tool>
    ): Int {
        var total = estimateTokens(systemPrompt) + estimateTokens(userMessage)
        history.forEach { total += estimateTokens(it) }
        tools.forEach { tool ->
            total += TOOL_DECL_OVERHEAD_TOKENS +
                estimateTokens(tool.name) +
                estimateTokens(tool.description) +
                estimateTokens(tool.parametersSchema.toString())
        }
        return total
    }

    fun shouldCompactRequest(requestTokens: Int, contextLength: Int = 128_000): Boolean =
        requestTokens >= (contextLength * autoCompactThresholdFraction).toInt()

    fun estimateTokens(m: LlmMessage): Int {
        var tokens = MESSAGE_OVERHEAD_TOKENS +
            estimateTokens(m.content) +
            estimateTokens(m.reasoningContent.orEmpty())
        m.toolCalls.forEach { tc ->
            tokens += TOOL_CALL_OVERHEAD_TOKENS + estimateTokens(tc.name) + estimateTokens(tc.argumentsJson)
        }
        tokens += m.images.size * IMAGE_ESTIMATE_TOKENS
        return tokens
    }

    /**
     * Heuristic chars→tokens. CJK ideographs/kana/hangul run ~1 token per char on
     * common tokenizers, so counting them at the legacy chars/4 rate undercounted
     * Chinese-heavy sessions by ~4x; non-CJK text (code, English) is closer to ~3
     * chars/token. Slight overestimation is deliberate — compaction firing a bit
     * early beats a dead-end provider 400.
     */
    fun estimateTokens(text: String): Int {
        if (text.isEmpty()) return 0
        var cjk = 0
        for (ch in text) if (isCjkChar(ch)) cjk++
        return cjk + ((text.length - cjk) / nonCjkCharsPerToken).toInt()
    }

    private fun isCjkChar(c: Char): Boolean {
        val v = c.code
        return v in 0x2E80..0x9FFF ||   // radicals, kana, CJK unified ideographs
            v in 0xAC00..0xD7AF ||      // Hangul syllables
            v in 0xF900..0xFAFF ||      // CJK compatibility ideographs
            v in 0xFF00..0xFFEF         // fullwidth forms
    }

    suspend fun compact(
        messages: List<AgentMessage>,
        textModel: SavedModel,
        textApiKey: ApiKeyConfig
    ): Result {
        if (messages.size <= minRecentMessages) {
            return Result(messages, summary = null, reason = "below threshold")
        }

        val (history, recent) = splitRecent(messages)
        if (history.isEmpty()) return Result(messages, summary = null, reason = "nothing to summarise")

        val historyText = renderForSummary(history)
        val systemPrompt = """
            You are a conversation summariser. Produce a faithful, structured summary of the prior agent session below. Preserve specific decisions, file paths, errors encountered, and the user's stated goals — those will be needed to continue the work. Discard chit-chat. Use these sections:

            ## Goals
            (what the user is trying to achieve)

            ## Files touched
            (path : one-line note about the change)

            ## Decisions
            (the choices made and why)

            ## Errors and resolutions
            (what broke and how it was fixed)

            ## Open items
            (anything pending — still needed, half-done, deferred)

            Stay under 1500 words.
        """.trimIndent()

        val req = ChatRequest(
            apiKey = textApiKey,
            model = textModel.model,
            messages = listOf(
                LlmMessage(LlmMessage.Role.SYSTEM, systemPrompt),
                LlmMessage(LlmMessage.Role.USER, historyText)
            ),
            tools = emptyList(),
            useTools = false
        )

        val summaryText = try {
            collectText(gateway, req)
        } catch (t: Throwable) {
            AppLogger.w(TAG, "compact summarisation failed: ${t.message}")
            return Result(messages, summary = null, reason = "summarisation failed: ${t.message}")
        }

        if (summaryText.isBlank()) {
            return Result(messages, summary = null, reason = "empty summary returned")
        }

        val carrier = listOf(
            AgentMessage(
                role = AgentMessage.Role.USER,
                content = "[Compacted prior history]\n\n$summaryText"
            ),
            AgentMessage(
                role = AgentMessage.Role.ASSISTANT,
                content = "Got it — continuing from the compacted summary above."
            )
        )
        return Result(carrier + recent, summary = summaryText, reason = "compacted")
    }

    private fun splitRecent(messages: List<AgentMessage>): Pair<List<AgentMessage>, List<AgentMessage>> {
        var keptTokens = 0
        var keptMessages = 0
        var splitIdx = messages.size

        for (i in messages.indices.reversed()) {
            val m = messages[i]
            val tokens = estimateTokens(m)
            keptTokens += tokens
            keptMessages++
            splitIdx = i

            if (keptMessages >= minRecentMessages && keptTokens >= minRecentTokens) break
        }

        if (splitIdx > 0 && messages[splitIdx].role == AgentMessage.Role.ASSISTANT &&
            messages[splitIdx].toolCalls.isNotEmpty()) {
            splitIdx -= 1
        }

        return messages.take(splitIdx) to messages.drop(splitIdx)
    }

    private fun renderForSummary(messages: List<AgentMessage>): String = buildString {
        for (m in messages) {
            append("[")
            append(m.role.name.lowercase())
            append("]\n")
            append(m.content.trim())
            if (m.toolCalls.isNotEmpty()) {
                append("\n  tool calls:\n")
                for (tc in m.toolCalls) {
                    append("    - ")
                    append(tc.name)
                    if (!tc.ok) append(" (error)")
                    append(": ")
                    append(tc.resultPreview.take(160))
                    append("\n")
                }
            }
            if (m.producedFiles.isNotEmpty()) {
                append("\n  files: ")
                append(m.producedFiles.joinToString(", "))
            }
            append("\n\n")
        }
    }

    private suspend fun collectText(gateway: LlmGateway, req: ChatRequest): String {
        var retries = 0
        while (true) {
            val sb = StringBuilder()
            var retryable: String? = null
            var retryAfterMs: Long? = null
            gateway.chatStream(req).fold(Unit) { _, ev ->
                when (ev) {
                    is LlmEvent.TextDelta -> sb.append(ev.delta)
                    is LlmEvent.Error -> if (!ev.recoverable) throw IllegalStateException(ev.message)
                    else { retryable = ev.message; retryAfterMs = ev.retryAfterMs }
                    else -> Unit
                }
            }
            if (retryable == null) return sb.toString().trim()
            if (retries >= 5) throw IllegalStateException(retryable)
            retries++
            val backoff = retryAfterMs ?: (1000L shl (retries - 1).coerceAtMost(4))
            kotlinx.coroutines.delay(backoff)
        }
    }

    private fun estimateTokens(m: AgentMessage): Int {
        var tokens = MESSAGE_OVERHEAD_TOKENS +
            estimateTokens(m.content) +
            estimateTokens(m.thinking.orEmpty())
        m.toolCalls.forEach { tc ->
            tokens += TOOL_CALL_OVERHEAD_TOKENS +
                estimateTokens(tc.argumentsJson) +
                estimateTokens(tc.resultPreview) +
                estimateTokens(tc.name)
        }
        return tokens
    }

    data class Result(
        val messages: List<AgentMessage>,
        val summary: String?,
        val reason: String
    ) {
        val didCompact: Boolean get() = summary != null
    }

    companion object {
        private const val TAG = "CompactService"

        /** Envelope cost per wire message (role, ids, JSON framing). */
        private const val MESSAGE_OVERHEAD_TOKENS = 8

        private const val TOOL_CALL_OVERHEAD_TOKENS = 8

        private const val TOOL_DECL_OVERHEAD_TOKENS = 8

        /** Order-of-magnitude placeholder for an inline image part. */
        private const val IMAGE_ESTIMATE_TOKENS = 1500

        /**
         * True when a provider error text means "the request exceeds the model's
         * context window" — the one failure where compacting and retrying is the
         * correct response instead of dead-ending the turn.
         */
        fun isContextOverflowError(message: String): Boolean {
            val m = message.lowercase()
            return m.contains("context length") ||
                m.contains("context window") ||
                m.contains("context_length") ||
                m.contains("maximum context") ||
                m.contains("too many tokens") ||
                m.contains("prompt is too long") ||
                m.contains("reduce the length") ||
                m.contains("request too large") ||
                m.contains("token limit")
        }
    }
}
