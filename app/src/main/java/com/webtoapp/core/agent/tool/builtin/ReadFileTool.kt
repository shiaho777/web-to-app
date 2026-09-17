package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult

class ReadFileTool : Tool {
    override val name = "Read"
    override val description = """
        Read a file from the current project. Returns its contents with line numbers.
        - The path must be project-relative (e.g. `index.html`, `src/main.js`).
        - Use offset/limit for large files; default reads up to 2000 lines.
        - Reading a file marks it as seen so subsequent Write/Edit operations on it are allowed.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("path", "Project-relative file path", required = true)
        integer("offset", "Line index to start from (0-based)", default = 0)
        integer("limit", "Maximum lines to return", default = 2000)
    }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject): String? =
        args.get("path")?.asString?.let { "Reading $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val path = ctx.resolveSafePath(args.get("path")?.asString)
            ?: return ToolResult.error("Read: invalid or missing `path`.")
        val offset = args.get("offset")?.asInt?.coerceAtLeast(0) ?: 0
        val limit = args.get("limit")?.asInt?.coerceIn(1, 10_000) ?: 2000

        val window = ctx.fileManager.readTextWindow(ctx.sessionId, path, offset, limit)
            ?: return ToolResult.error("Read: $path not found.")

        if (window.binary) {
            return ToolResult.error(
                "Read: $path is a binary file (${formatBytes(window.totalBytes)}) — " +
                    "not readable as text."
            )
        }
        if (window.lines.isEmpty()) {
            return ToolResult.ok(
                if (offset == 0) "(empty file)"
                else "(empty range — offset=$offset is past the end of $path)"
            )
        }

        ctx.readFiles += path

        val sb = StringBuilder()
        val chunkSize = STREAM_CHUNK_LINES
        var lineIdx = 0
        while (lineIdx < window.lines.size) {
            val end = minOf(lineIdx + chunkSize, window.lines.size)
            val piece = StringBuilder()
            for (i in lineIdx until end) {

                if (sb.isNotEmpty() || piece.isNotEmpty()) piece.append('\n')
                piece.append(offset + i + 1).append('\t').append(window.lines[i])
            }
            sb.append(piece)
            ctx.progress(piece.toString())
            lineIdx = end
        }

        val tail = when {
            window.remainingCapped ->
                "\n… (${window.remainingLines}+ more lines, raise `limit` or set `offset`)"
            window.remainingLines > 0 ->
                "\n… (${window.remainingLines} more lines, raise `limit` or set `offset`)"
            else -> ""
        }
        if (tail.isNotEmpty()) ctx.progress(tail)

        return ToolResult.ok(sb.toString() + tail)
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        bytes < 1024L * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
        else -> "%.1fGB".format(bytes / (1024.0 * 1024 * 1024))
    }

    companion object {

        private const val STREAM_CHUNK_LINES = 64
    }
}
