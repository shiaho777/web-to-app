package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult

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

        val content = ctx.fileManager.readText(ctx.sessionId, path)
            ?: return ToolResult.error("Read: $path not found.")

        ctx.readFiles += path

        val lines = content.lines()
        if (offset >= lines.size) {
            return ToolResult.ok("(empty range — file has ${lines.size} lines, offset=$offset)")
        }
        val window = lines.drop(offset).take(limit)

        val sb = StringBuilder()
        val chunkSize = STREAM_CHUNK_LINES
        var lineIdx = 0
        while (lineIdx < window.size) {
            val end = minOf(lineIdx + chunkSize, window.size)
            val piece = StringBuilder()
            for (i in lineIdx until end) {

                if (sb.isNotEmpty() || piece.isNotEmpty()) piece.append('\n')
                piece.append(offset + i + 1).append('\t').append(window[i])
            }
            sb.append(piece)
            ctx.progress(piece.toString())
            lineIdx = end
        }

        val tail = if (offset + limit < lines.size) {
            "\n… (${lines.size - offset - limit} more lines, raise `limit` or set `offset`)"
        } else ""
        if (tail.isNotEmpty()) ctx.progress(tail)

        return ToolResult.ok(sb.toString() + tail)
    }

    companion object {

        private const val STREAM_CHUNK_LINES = 64
    }
}
