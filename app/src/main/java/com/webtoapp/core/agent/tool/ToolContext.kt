package com.webtoapp.core.agent.tool

import android.content.Context
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.permission.PermissionPrompter
import com.webtoapp.core.agent.todo.TodoManager
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.SavedModel
import com.webtoapp.data.repository.WebAppRepository

data class ToolContext(
    val androidContext: Context,
    val sessionId: String,
    val fileManager: ProjectFileManager,

    val textModel: SavedModel,
    val textApiKey: ApiKeyConfig,

    val imageModel: SavedModel? = null,
    val imageApiKey: ApiKeyConfig? = null,

    val prompter: PermissionPrompter,

    val todos: TodoManager,

    val appRepository: WebAppRepository,

    // Read-files bookkeeping is mutated from parallel read-only tool batches
    // (batchToolCalls runs consecutive read-only tools concurrently) and the set is
    // shared across ToolContext.copy() instances — a plain LinkedHashSet is not
    // thread-safe.
    val readFiles: MutableSet<String> = java.util.Collections.synchronizedSet(mutableSetOf()),

    val activePlanFile: String? = null,

    // Plan mode entered mid-turn publishes the generated plan file path into
    // this holder so PermissionChecker.checkPlan (which compares against
    // effectivePlanFile()) can allow writes to it for the rest of the turn.
    // The holder is a constructor property — copy() shares the same instance —
    // because a plain body-property var would be reset to null on every
    // per-call copy in AgentEngine.runSequential, which silently denied all
    // plan-file writes and deadlocked plan mode (#997). Same trick as
    // readFiles above.
    val livePlanFileRef: java.util.concurrent.atomic.AtomicReference<String?> =
        java.util.concurrent.atomic.AtomicReference(null),

    val progress: suspend (String) -> Unit = NO_OP_PROGRESS
) {

    /** Publishes the active plan file path (plan mode entered mid-turn). */
    fun setActivePlanFile(path: String?) {
        livePlanFileRef.set(path)
    }

    /** The path plan-mode writes must go to: live publication wins over the snapshot. */
    fun effectivePlanFile(): String? = livePlanFileRef.get() ?: activePlanFile

    fun resolveSafePath(rawPath: String?): String? {
        if (rawPath.isNullOrBlank()) return null
        val cleaned = rawPath.trim().trimStart('/').trim('\\')
        if (cleaned.isEmpty()) return null
        if (cleaned.length > MAX_PATH_LENGTH) return null
        if (cleaned.contains("..")) return null
        if (cleaned.contains(':')) return null

        if (cleaned.startsWith('~')) return null
        return cleaned.replace('\\', '/')
    }

    companion object {
        const val MAX_PATH_LENGTH = 500

        val NO_OP_PROGRESS: suspend (String) -> Unit = {  }
    }
}
