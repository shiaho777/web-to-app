package com.webtoapp.core.aicoding.agent

import com.webtoapp.core.aicoding.tool.FileChange
import com.webtoapp.core.aicoding.tool.ToolResult

sealed class AgentEvent {
    object Started : AgentEvent()

    data class TextDelta(val delta: String, val accumulated: String) : AgentEvent()

    data class ThinkingDelta(val delta: String, val accumulated: String) : AgentEvent()

    data class ToolCallStarted(val toolCallId: String, val name: String) : AgentEvent()

    data class ToolCallArgsDelta(val toolCallId: String, val delta: String) : AgentEvent()

    data class ToolProgress(
        val toolCallId: String,
        val name: String,
        val delta: String,
        val accumulated: String
    ) : AgentEvent()

    data class ToolExecuting(val toolCallId: String, val name: String, val activity: String?) : AgentEvent()

    data class ToolFinished(
        val toolCallId: String,
        val name: String,
        val argumentsJson: String,
        val result: ToolResult
    ) : AgentEvent()

    data class FileChanged(val change: FileChange) : AgentEvent()

    data class PermissionDenied(val toolCallId: String, val name: String) : AgentEvent()

    data class Usage(
        val inputTokens: Int,
        val outputTokens: Int,
        val cacheReadTokens: Int = 0,
        val cacheCreationTokens: Int = 0
    ) : AgentEvent()

    data class Notice(val message: String) : AgentEvent()

    data class Completed(val summary: String, val toolCallCount: Int) : AgentEvent()

    object Aborted : AgentEvent()

    data class Failed(val message: String) : AgentEvent()
}
