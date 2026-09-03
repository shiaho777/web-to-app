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

    val readFiles: MutableSet<String> = mutableSetOf(),

    val activePlanFile: String? = null,

    val progress: suspend (String) -> Unit = NO_OP_PROGRESS
) {

    // Plan mode entered mid-turn publishes the generated plan file path here so
    // PermissionChecker.checkPlan (which compares against activePlanFile) can allow
    // writes to it for the rest of the turn. Kept outside the data-class constructor
    // because the constructor value is a per-turn snapshot that is always null in
    // the mid-turn flow.
    @Volatile
    var livePlanFile: String? = null
        private set

    /** Publishes the active plan file path (plan mode entered mid-turn). */
    fun setActivePlanFile(path: String?) {
        livePlanFile = path
    }

    /** The path plan-mode writes must go to: live publication wins over the snapshot. */
    fun effectivePlanFile(): String? = livePlanFile ?: activePlanFile

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
