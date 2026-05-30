package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.tool.FileChange
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult

class EditFileTool : Tool {
    override val name = "Edit"
    override val description = """
        Replace exact substrings in an existing project file.
        - Read the file first this turn.
        - `old_string` must occur exactly once unless `replace_all=true`.
        - Preserve indentation precisely; whitespace is significant.
        - For wholesale rewrites, use Write instead.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("path", "Project-relative file path", required = true)
        string("old_string", "Exact substring to replace", required = true)
        string("new_string", "Replacement text", required = true)
        boolean("replace_all", "Replace every occurrence instead of failing on multi-match", default = false)
    }

    override fun activityDescription(args: JsonObject): String? =
        args.get("path")?.asString?.let { "Editing $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val path = ctx.resolveSafePath(args.get("path")?.asString)
            ?: return ToolResult.error("Edit: invalid or missing `path`.")
        val oldStr = args.get("old_string")?.asString
            ?: return ToolResult.error("Edit: missing `old_string`.")
        val newStr = args.get("new_string")?.asString
            ?: return ToolResult.error("Edit: missing `new_string`.")
        val replaceAll = args.get("replace_all")?.asBoolean ?: false

        if (oldStr.isEmpty()) return ToolResult.error("Edit: `old_string` cannot be empty.")
        if (oldStr == newStr) return ToolResult.error("Edit: `old_string` and `new_string` are identical — nothing to do.")

        val current = ctx.fileManager.readText(ctx.sessionId, path)
            ?: return ToolResult.error("Edit: $path not found.")

        if (path !in ctx.readFiles) {
            return ToolResult.error("Edit: you must Read $path before editing it.")
        }

        val matches = countOccurrences(current, oldStr)
        if (matches == 0) {
            return ToolResult.error(
                "Edit: `old_string` not found in $path. " +
                    "Re-read the file (it may have changed) or include more surrounding context."
            )
        }
        if (matches > 1 && !replaceAll) {
            return ToolResult.error(
                "Edit: `old_string` matches $matches places in $path. " +
                    "Add more surrounding context to make it unique, or pass replace_all=true."
            )
        }

        val updated = if (replaceAll) {
            current.replace(oldStr, newStr)
        } else {

            val idx = current.indexOf(oldStr)
            current.substring(0, idx) + newStr + current.substring(idx + oldStr.length)
        }

        val info = ctx.fileManager.writeText(ctx.sessionId, path, updated)
            ?: return ToolResult.error("Edit: refusing to write $path (path is unsafe).")

        val n = if (replaceAll) matches else 1
        return ToolResult.ok(
            text = "Replaced $n occurrence(s) in ${info.relativePath}.",
            fileChange = FileChange(info.relativePath, FileChange.Kind.EDIT, updated)
        )
    }

    private fun countOccurrences(haystack: String, needle: String): Int {
        if (needle.isEmpty()) return 0
        var count = 0
        var idx = 0
        while (true) {
            val found = haystack.indexOf(needle, idx)
            if (found < 0) break
            count++
            idx = found + needle.length
        }
        return count
    }
}
