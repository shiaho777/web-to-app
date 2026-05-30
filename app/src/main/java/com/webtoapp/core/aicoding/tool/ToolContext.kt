package com.webtoapp.core.aicoding.tool

import android.content.Context
import com.webtoapp.core.aicoding.files.ProjectFileManager
import com.webtoapp.core.aicoding.permission.PermissionPrompter
import com.webtoapp.core.aicoding.todo.TodoManager
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.SavedModel

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

    val readFiles: MutableSet<String> = mutableSetOf(),

    val activePlanFile: String? = null,

    val skillRegistry: com.webtoapp.core.aicoding.skill.SkillRegistry? = null,

    val progress: suspend (String) -> Unit = NO_OP_PROGRESS
) {

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
