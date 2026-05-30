package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult

class ListFilesTool : Tool {
    override val name = "ListFiles"
    override val description = """
        List every file in the current project, with size and modification time.
        Use this for a quick orientation; for pattern-matched listings use Glob instead.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {  }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject): String = "Listing project files"

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val files = ctx.fileManager.listAll(ctx.sessionId)
        if (files.isEmpty()) return ToolResult.ok("(no files yet — start by writing your first file)")
        val rows = files.joinToString("\n") { f ->
            "${f.relativePath}\t${f.formatSize()}\t${f.formatTime()}"
        }
        return ToolResult.ok(rows)
    }
}
