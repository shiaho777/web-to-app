package com.webtoapp.ui.agent.components

import com.webtoapp.core.agent.session.AgentSession
import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.ui.agent.AgentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders a session to a Markdown transcript for the drawer's Export action.
 * Pure formatting: reads the persisted session model only (with the safe
 * accessors used everywhere else), so legacy sessions export identically.
 */
object SessionTranscript {

    private val TIME = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun render(session: AgentSession, thinkingHeader: String): String = buildString {
        append("# ").append(session.title.ifBlank { "Session" }).append("\n\n")
        append("- ID: `").append(session.id).append("`\n")
        append("- Created: ").append(TIME.format(Date(session.createdAt))).append("\n")
        append("- Updated: ").append(TIME.format(Date(session.updatedAt))).append("\n")
        append("- Messages: ").append(session.messages.size).append("\n")

        session.messages.forEach { msg ->
            append("\n---\n\n")
            when (msg.role) {
                com.webtoapp.core.agent.session.AgentMessage.Role.USER -> {
                    append("## 👤 User\n\n")
                    msg.userAttachmentsSafe.forEach { att ->
                        append("- 📎 ").append(att.displayName).append('\n')
                    }
                    if (msg.userAttachmentsSafe.isNotEmpty()) append('\n')
                    append(AgentViewModel.stripInlineMarkers(msg.content).trim()).append('\n')
                    msg.mentionedFiles.forEach { f ->
                        append("\n> `@").append(f).append('`')
                    }
                }
                com.webtoapp.core.agent.session.AgentMessage.Role.ASSISTANT -> {
                    append("## 🤖 Assistant\n\n")
                    msg.thinkingSegmentsSafe.forEachIndexed { i, seg ->
                        val c = seg.content.trim()
                        if (c.isBlank()) return@forEachIndexed
                        append("> **").append(thinkingHeader)
                        if (msg.thinkingSegmentsSafe.size > 1) append(" (${i + 1})")
                        append("**\n>\n")
                        c.lines().forEach { line -> append("> ").append(line).append('\n') }
                        append('\n')
                    }
                    msg.toolCalls.forEach { tc -> appendToolCall(tc) }
                    val prose = AgentViewModel.stripInlineMarkers(msg.content).trim()
                    if (prose.isNotEmpty()) append(prose).append('\n')
                }
                com.webtoapp.core.agent.session.AgentMessage.Role.SYSTEM -> {
                    append("## ⚙️ System\n\n")
                    append(msg.content.trim()).append('\n')
                }
            }
        }
    }

    private fun StringBuilder.appendToolCall(tc: RecordedToolCall) {
        append("\n### 🔧 ").append(tc.name).append('\n')
        val args = tc.argumentsJson.trim()
        if (args.isNotEmpty() && args != "{}") {
            append("\n```json\n").append(args).append("\n```\n")
        }
        val result = tc.resultPreview.trim()
        if (result.isNotEmpty() && result != RecordedToolCall.RUNNING_SENTINEL) {
            append("\n```\n").append(result).append("\n```\n")
        }
        append('\n')
    }
}
