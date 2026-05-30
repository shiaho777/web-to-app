package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult

class GlobTool : Tool {
    override val name = "Glob"
    override val description = """
        Find files in the project whose path matches a glob pattern.
        - Patterns are matched against project-relative paths.
        - `*` matches characters inside a segment, `**` matches across segments, `?` is one char.
        - Examples: `**/*.html`, `assets/*.png`, `src/**/*.{js,ts}` (alternation is NOT supported — call twice instead).
        - Results are sorted by modification time, newest first.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("pattern", "Glob pattern", required = true)
        integer("limit", "Maximum results", default = 100)
    }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject): String? =
        args.get("pattern")?.asString?.let { "Finding $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val pattern = args.get("pattern")?.asString?.trim()
            ?: return ToolResult.error("Glob: missing `pattern`.")
        if (pattern.isEmpty()) return ToolResult.error("Glob: pattern is empty.")
        val limit = args.get("limit")?.asInt?.coerceIn(1, 1_000) ?: 100

        val regex = compileGlob(pattern)
        val matches = ctx.fileManager.listAll(ctx.sessionId)
            .filter { regex.matches(it.relativePath) }
            .sortedByDescending { it.modifiedAt }

        if (matches.isEmpty()) return ToolResult.ok("(no files match $pattern)")
        val truncated = matches.size > limit
        val rows = matches.take(limit).joinToString("\n") { it.relativePath }
        val tail = if (truncated) "\n… (${matches.size - limit} more — narrow the pattern or raise `limit`)" else ""
        return ToolResult.ok(rows + tail)
    }

    private fun compileGlob(pattern: String): Regex {
        val sb = StringBuilder("^")
        var i = 0
        while (i < pattern.length) {
            val c = pattern[i]
            when {
                c == '*' && i + 1 < pattern.length && pattern[i + 1] == '*' -> {
                    sb.append(".*")
                    i += 2

                    if (i < pattern.length && pattern[i] == '/') i++
                }
                c == '*' -> { sb.append("[^/]*"); i++ }
                c == '?' -> { sb.append("[^/]"); i++ }
                c == '.' || c == '(' || c == ')' || c == '+' || c == '|' || c == '^' ||
                    c == '$' || c == '@' || c == '%' || c == '\\' || c == '{' || c == '}' ||
                    c == '[' || c == ']' -> { sb.append('\\').append(c); i++ }
                else -> { sb.append(c); i++ }
            }
        }
        sb.append('$')
        return Regex(sb.toString())
    }
}
