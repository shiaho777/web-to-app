package com.webtoapp.ui.agent.components

import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.ui.agent.ThinkingSegment

internal sealed interface AssistantTimelinePart {
    data class Prose(val text: String) : AssistantTimelinePart
    data class Thinking(val segment: ThinkingSegment) : AssistantTimelinePart
    data class Tools(val calls: List<RecordedToolCall>) : AssistantTimelinePart
}

internal fun buildAssistantTimeline(
    text: String,
    toolCalls: List<RecordedToolCall>,
    thinking: List<ThinkingSegment>,
    hasLegacyThinking: Boolean = false
): List<AssistantTimelinePart> = buildList {
    val toolsById = toolCalls.associateBy { it.toolCallId }
    val thinkingById = thinking.associateBy { it.id }
    val pendingTools = ArrayList<RecordedToolCall>()
    val seenTools = HashSet<String>()
    val seenThinking = HashSet<String>()

    fun flushTools() {
        if (pendingTools.isNotEmpty()) {
            add(AssistantTimelinePart.Tools(pendingTools.toList()))
            pendingTools.clear()
        }
    }

    fun appendProse(value: String) {
        val prose = value.replace("\u2063", "").trimEnd()
        if (prose.isNotBlank()) {
            flushTools()
            add(AssistantTimelinePart.Prose(prose))
        }
    }

    val content = assistantDisplayText(text, toolCalls.isNotEmpty() || thinking.isNotEmpty() || hasLegacyThinking)
    if (content.isNotEmpty()) {
        var cursor = 0
        for (marker in inlineMarker.findAll(content)) {
            appendProse(content.substring(cursor, marker.range.first))
            val id = marker.groupValues[2]
            when (marker.groupValues[1]) {
                "TC" -> toolsById[id]?.let { if (seenTools.add(id)) pendingTools.add(it) }
                "TH" -> thinkingById[id]?.let {
                    if (seenThinking.add(id)) {
                        flushTools()
                        add(AssistantTimelinePart.Thinking(it))
                    }
                }
            }
            cursor = marker.range.last + 1
        }
        appendProse(content.substring(cursor))
    }

    thinking.filter { seenThinking.add(it.id) }.forEach {
        flushTools()
        add(AssistantTimelinePart.Thinking(it))
    }
    toolCalls.filter { seenTools.add(it.toolCallId) }.forEach(pendingTools::add)
    flushTools()
}

internal fun assistantDisplayText(text: String, hasActivity: Boolean): String =
    if (hasActivity && text.trim() in legacyEmptyAssistantReplies) "" else text

private val inlineMarker = Regex("\u2063(TC|TH):([^\u2063]+)\u2063")

private val legacyEmptyAssistantReplies = setOf(
    "(无输出)", "(no output)", "(لا يوجد ناتج)", "(sem saída)", "(sin salida)",
    "(aucune sortie)", "(keine Ausgabe)", "(нет вывода)", "(出力なし)", "(출력 없음)"
)
