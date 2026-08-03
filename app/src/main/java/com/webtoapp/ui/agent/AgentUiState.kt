package com.webtoapp.ui.agent

import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.permission.ChoiceRequest
import com.webtoapp.core.agent.permission.PermissionRequest
import com.webtoapp.core.agent.session.AgentSession
import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.core.agent.session.UserAttachment
import com.webtoapp.core.agent.todo.TodoManager

data class AgentUiState(
    val phase: Phase = Phase.Idle,

    val sessions: List<AgentSession> = emptyList(),
    val currentSession: AgentSession? = null,

    val streamingText: String = "",
    val streamingThinkingSegments: List<ThinkingSegment> = emptyList(),

    /**
     * Message id of the assistant draft the service is currently writing, if any.
     * The UI hides this message from the history list while streaming (to avoid it
     * showing twice: once as a saved bubble and once in the live StreamingBubble).
     */
    val streamingDraftMessageId: String? = null,

    val pendingToolCalls: List<RecordedToolCall> = emptyList(),
    val currentActivity: String? = null,

    val projectFiles: List<ProjectFileManager.FileInfo> = emptyList(),
    val selectedFilePath: String? = null,
    val selectedFileContent: String? = null,

    val previewFilePath: String? = null,

    val planActive: Boolean = false,
    val planFilePath: String? = null,
    val pendingPlanReview: PlanReview? = null,

    val todos: List<TodoManager.Item> = emptyList(),

    val composerText: String = "",
    val slashOpen: Boolean = false,
    val slashCommands: List<SlashCommand> = emptyList(),

    val pendingAttachments: List<UserAttachment> = emptyList(),

    val modelPickerOpen: Boolean = false,
    val modelProviderGroups: List<ProviderGroup> = emptyList(),
    val selectedProviderKeyId: String? = null,
    val currentModelLabel: String = "",

    val mentionPickerOpen: Boolean = false,

    val mentionQuery: String = "",

    val mentionMatches: List<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo> = emptyList(),

    val drawerOpen: Boolean = false,
    val drawerTab: DrawerTab = DrawerTab.Sessions,
    val drawerSearch: String = "",

    val previewOpen: Boolean = false,

    val editingMessageId: String? = null,

    val pendingPermission: PermissionRequest? = null,
    val pendingChoice: ChoiceRequest? = null,

    val saveAsAppDialogOpen: Boolean = false,

    val detectedArtifacts: List<com.webtoapp.core.agent.export.DetectedArtifact> = emptyList(),

    val selectedArtifactId: String? = null,

    val saveAsAppInFlight: Boolean = false,

    val autoApprove: Boolean = false,

    val pendingChanges: List<PendingChange> = emptyList(),

    val changesReviewExpanded: Boolean = false,

    val error: String? = null,
    val info: String? = null,

    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
    val estimatedContextTokens: Int = 0,
    val contextCapacity: Int = 0,
    val compacting: Boolean = false,

    val contextPickerOpen: Boolean = false,
    val contextAppIds: List<Long> = emptyList(),
    val contextModuleIds: List<String> = emptyList(),
    val availableContextApps: List<ContextAppItem> = emptyList(),
    val availableContextModules: List<ContextModuleItem> = emptyList(),
    val availableCategories: List<ContextCategoryItem> = emptyList()
) {
    enum class Phase { Idle, Connecting, Streaming, AwaitingTool, AwaitingUser, Error }
    enum class DrawerTab { Sessions, Files }

    val canSend: Boolean get() = phase == Phase.Idle
    val isWorking: Boolean get() = phase != Phase.Idle && phase != Phase.Error
}

data class ContextAppItem(
    val id: Long,
    val name: String,
    val appType: String,
    val categoryId: Long? = null
)

/**
 * One turn's worth of streaming reasoning. [frozenDurationMs] is null while the
 * reasoning for this turn is still arriving (live); once the turn's thinking stream
 * ends it is frozen so the UI renders a static, collapsed block and the next turn
 * can open its own live block. [id] matches the inline marker in the streamed text
 * so the timeline can interleave this block with prose/tools.
 */
data class ThinkingSegment(
    val id: String,
    val content: String,
    val startedAt: Long,
    val frozenDurationMs: Long? = null
)

data class ContextModuleItem(
    val id: String,
    val name: String,
    val sourceType: String
)

data class ContextCategoryItem(
    val id: Long,
    val name: String,
    val icon: String
)

data class PlanReview(
    val planPath: String,
    val content: String
)

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
