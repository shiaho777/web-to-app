package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.tool.FileChange
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult

class DeleteFileTool : Tool {
    override val name = "Delete"
    override val description = """
        Delete a project file. Use sparingly — prefer Edit when removing pieces from a file.
        - The path must be project-relative.
        - Read the file first this turn so we know what's being removed.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("path", "Project-relative file path", required = true)
    }

    override fun activityDescription(args: JsonObject): String? =
        args.get("path")?.asString?.let { "Deleting $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val path = ctx.resolveSafePath(args.get("path")?.asString)
            ?: return ToolResult.error("Delete: invalid or missing `path`.")
        if (!ctx.fileManager.exists(ctx.sessionId, path)) {
            return ToolResult.error("Delete: $path does not exist.")
        }
        if (path !in ctx.readFiles) {
            return ToolResult.error("Delete: Read $path first so we know what we're removing.")
        }
        val deleted = ctx.fileManager.delete(ctx.sessionId, path)
        if (!deleted) return ToolResult.error("Delete: failed to remove $path.")
        ctx.readFiles.remove(path)
        return ToolResult.ok(
            text = "Deleted $path.",
            fileChange = FileChange(path, FileChange.Kind.DELETE)
        )
    }
}
