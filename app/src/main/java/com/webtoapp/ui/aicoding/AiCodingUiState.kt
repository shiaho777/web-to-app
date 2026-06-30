package com.webtoapp.ui.aicoding

import com.webtoapp.core.aicoding.files.ProjectFileManager
import com.webtoapp.core.aicoding.permission.ChoiceRequest
import com.webtoapp.core.aicoding.permission.PermissionRequest
import com.webtoapp.core.aicoding.session.AgentSession
import com.webtoapp.core.aicoding.session.RecordedToolCall
import com.webtoapp.core.aicoding.skill.Skill
import com.webtoapp.core.aicoding.todo.TodoManager

data class AiCodingUiState(
    val phase: Phase = Phase.Idle,

    val sessions: List<AgentSession> = emptyList(),
    val currentSession: AgentSession? = null,

    val skills: List<Skill> = emptyList(),

    val streamingText: String = "",
    val streamingThinking: String = "",

    val streamingThinkingStartedAt: Long? = null,

    val streamingThinkingDurationMs: Long? = null,
    val pendingToolCalls: List<RecordedToolCall> = emptyList(),
    val currentActivity: String? = null,

    val projectFiles: List<ProjectFileManager.FileInfo> = emptyList(),
    val selectedFilePath: String? = null,
    val selectedFileContent: String? = null,

    val previewFilePath: String? = null,

    val planActive: Boolean = false,
    val planFilePath: String? = null,

    val todos: List<TodoManager.Item> = emptyList(),

    val composerText: String = "",
    val slashOpen: Boolean = false,
    val slashSuggestions: List<Skill> = emptyList(),
    val slashCommands: List<SlashCommand> = emptyList(),

    val modelPickerOpen: Boolean = false,
    val modelProviderGroups: List<ProviderGroup> = emptyList(),
    val selectedProviderKeyId: String? = null,
    val currentModelLabel: String = "",

    val mentionPickerOpen: Boolean = false,

    val mentionQuery: String = "",

    val mentionMatches: List<com.webtoapp.core.aicoding.files.ProjectFileManager.FileInfo> = emptyList(),

    val drawerOpen: Boolean = false,
    val drawerTab: DrawerTab = DrawerTab.Sessions,
    val drawerSearch: String = "",

    val previewOpen: Boolean = false,

    val editingMessageId: String? = null,

    val pendingPermission: PermissionRequest? = null,
    val pendingChoice: ChoiceRequest? = null,

    val saveAsAppDialogOpen: Boolean = false,

    val detectedArtifacts: List<com.webtoapp.core.aicoding.export.DetectedArtifact> = emptyList(),

    val selectedArtifactId: String? = null,

    val saveAsAppInFlight: Boolean = false,

    val autoApprove: Boolean = false,

    val pendingChanges: List<PendingChange> = emptyList(),

    val changesReviewExpanded: Boolean = false,

    val error: String? = null,
    val info: String? = null,

    val inputTokens: Int = 0,
    val outputTokens: Int = 0
) {
    enum class Phase { Idle, Connecting, Streaming, AwaitingTool, AwaitingUser, Error }
    enum class DrawerTab { Sessions, Files, Skills }

    val canSend: Boolean get() = phase == Phase.Idle && currentSession != null
    val isWorking: Boolean get() = phase != Phase.Idle && phase != Phase.Error
}

data class PendingChange(
    val path: String,
    val kind: Kind,

    val touchedAt: Long
) {
    enum class Kind { Write, Edit, Delete }
}

data class SlashCommand(
    val id: String,
    val command: String,
    val description: String,
    val icon: String,
    val iconColor: String = "9CA3AF"
)

internal val DEFAULT_SLASH_COMMANDS = listOf(
    SlashCommand("model", "/model", "Switch the model for this session", "smart_toy", "3B82F6"),
    SlashCommand("plan", "/plan", "Enter plan mode (read-only research)", "fact_check", "F97316"),
    SlashCommand("exit-plan", "/exit-plan", "Exit plan mode and submit the plan", "logout", "F97316"),
    SlashCommand("compact", "/compact", "Compress conversation context", "compress", "F59E0B"),
    SlashCommand("clear", "/clear", "Start a new empty session", "auto_delete", "EF4444"),
)

data class ModelChoice(

    val id: String,

    val label: String,

    val subtitle: String,

    val selected: Boolean
)

data class ProviderGroup(

    val apiKeyId: String,

    val displayName: String,

    val models: List<ModelChoice>
)
