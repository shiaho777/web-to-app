package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.tool.FileChange
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult

class WriteFileTool : Tool {
    override val name = "Write"
    override val description = """
        Create or fully overwrite a project file. The path is project-relative.
        - For new files, the parent directory is created automatically.
        - For existing files, you must Read the file first this turn — otherwise the call fails.
        - For local edits, prefer Edit; this tool replaces the file entirely.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("path", "Project-relative file path", required = true)
        string("content", "Full file contents", required = true)
    }

    override fun activityDescription(args: JsonObject): String? =
        args.get("path")?.asString?.let { "Writing $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val path = ctx.resolveSafePath(args.get("path")?.asString)
            ?: return ToolResult.error("Write: invalid or missing `path`.")
        val content = args.get("content")?.asString
            ?: return ToolResult.error("Write: missing `content`.")

        val alreadyExists = ctx.fileManager.exists(ctx.sessionId, path)
        if (alreadyExists && path !in ctx.readFiles) {
            return ToolResult.error(
                "Write: $path already exists. Call Read on it first so we don't silently overwrite work."
            )
        }

        val info = ctx.fileManager.writeText(ctx.sessionId, path, content)
            ?: return ToolResult.error("Write: refusing to write $path (path is unsafe).")

        ctx.readFiles += path

        val verb = if (alreadyExists) "Overwrote" else "Created"
        val lineCount = if (content.isEmpty()) 0 else content.count { it == '\n' } + 1
        return ToolResult.ok(
            text = "$verb ${info.relativePath} ($lineCount lines, ${content.length} chars).",
            fileChange = FileChange(info.relativePath, FileChange.Kind.WRITE, content)
        )
    }
}
