package com.webtoapp.core.aicoding.agent

import com.webtoapp.core.aicoding.llm.ChatRequest
import com.webtoapp.core.aicoding.llm.LlmEvent
import com.webtoapp.core.aicoding.llm.LlmGateway
import com.webtoapp.core.aicoding.llm.LlmMessage
import com.webtoapp.core.aicoding.session.AgentMessage
import com.webtoapp.core.aicoding.session.RecordedToolCall
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.SavedModel
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.fold

class CompactService(
    private val gateway: LlmGateway,

    private val charsPerToken: Float = 4f,

    private val minRecentMessages: Int = 8,

    private val minRecentTokens: Int = 8_000,

    private val autoCompactThresholdTokens: Int = 80_000
) {

    fun shouldCompact(messages: List<AgentMessage>): Boolean =
        estimateTokens(messages) >= autoCompactThresholdTokens

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
        val sb = StringBuilder()
        gateway.chatStream(req).fold(Unit) { _, ev ->
            when (ev) {
                is LlmEvent.TextDelta -> sb.append(ev.delta)
                is LlmEvent.Error -> if (!ev.recoverable) throw IllegalStateException(ev.message)
                else -> Unit
            }
        }
        return sb.toString().trim()
    }

    private fun estimateTokens(messages: List<AgentMessage>): Int =
        messages.sumOf { estimateTokens(it) }

    private fun estimateTokens(m: AgentMessage): Int {
        val charBudget = m.content.length +
            (m.thinking?.length ?: 0) +
            m.toolCalls.sumOf { it.argumentsJson.length + it.resultPreview.length + it.name.length }
        return (charBudget / charsPerToken).toInt()
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
    }
}
