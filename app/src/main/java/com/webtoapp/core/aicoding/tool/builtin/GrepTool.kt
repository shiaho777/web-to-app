package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult

class GrepTool : Tool {
    override val name = "Grep"
    override val description = """
        Search project files for a regex pattern.
        - Use this instead of asking the user to grep manually.
        - `mode` chooses what to return: `files` (paths only, default), `content` (matches with line numbers), or `count`.
        - `glob` filters which files are searched (e.g. `**/*.html`).
        - `case_insensitive` toggles ignore-case.
        - `context` adds N lines of leading and trailing context to `content` results.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("pattern", "Regex pattern (Java regex syntax)", required = true)
        string("glob", "Optional file glob filter")
        enum("mode", listOf("files", "content", "count"), "Output shape (default: files)")
        boolean("case_insensitive", "Match case-insensitively", default = false)
        integer("context", "Lines of leading/trailing context for `content` mode", default = 0)
        integer("limit", "Max output rows", default = 200)
    }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject): String? =
        args.get("pattern")?.asString?.let { "Searching for $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val patternArg = args.get("pattern")?.asString
            ?: return ToolResult.error("Grep: missing `pattern`.")
        if (patternArg.isEmpty()) return ToolResult.error("Grep: pattern is empty.")

        val mode = args.get("mode")?.asString?.lowercase() ?: "files"
        val caseInsensitive = args.get("case_insensitive")?.asBoolean ?: false
        val context = args.get("context")?.asInt?.coerceIn(0, 5) ?: 0
        val limit = args.get("limit")?.asInt?.coerceIn(1, 1000) ?: 200
        val globRegex = args.get("glob")?.asString?.takeIf { it.isNotBlank() }?.let { compileGlobToRegex(it) }

        val regex = try {
            val flags = if (caseInsensitive) setOf(RegexOption.IGNORE_CASE) else emptySet()
            Regex(patternArg, flags)
        } catch (e: Exception) {
            return ToolResult.error("Grep: invalid regex (${e.message}).")
        }

        val files = ctx.fileManager.listAll(ctx.sessionId).filter { f ->
            f.isText && f.sizeBytes <= MAX_FILE_BYTES && (globRegex == null || globRegex.matches(f.relativePath))
        }
        if (files.isEmpty()) return ToolResult.ok("(nothing to search — no matching text files)")

        when (mode) {
            "files" -> {
                val hits = files.filter { f ->
                    val text = ctx.fileManager.readText(ctx.sessionId, f.relativePath) ?: return@filter false
                    regex.containsMatchIn(text)
                }
                if (hits.isEmpty()) return ToolResult.ok("(no matches)")
                val truncated = hits.size > limit
                return ToolResult.ok(
                    hits.take(limit).joinToString("\n") { it.relativePath } +
                        if (truncated) "\n… (${hits.size - limit} more files)" else ""
                )
            }
            "count" -> {
                val rows = mutableListOf<String>()
                for (f in files) {
                    val text = ctx.fileManager.readText(ctx.sessionId, f.relativePath) ?: continue
                    val n = regex.findAll(text).count()
                    if (n > 0) rows += "${f.relativePath}:$n"
                    if (rows.size >= limit) break
                }
                return ToolResult.ok(if (rows.isEmpty()) "(no matches)" else rows.joinToString("\n"))
            }
            "content" -> {
                val rows = mutableListOf<String>()
                outer@ for (f in files) {
                    val text = ctx.fileManager.readText(ctx.sessionId, f.relativePath) ?: continue
                    val lines = text.lines()
                    lines.forEachIndexed { idx, line ->
                        if (regex.containsMatchIn(line)) {
                            if (context > 0) {
                                val start = (idx - context).coerceAtLeast(0)
                                val end = (idx + context).coerceAtMost(lines.lastIndex)
                                for (i in start..end) {
                                    rows += "${f.relativePath}:${i + 1}:${if (i == idx) "> " else "  "}${lines[i]}"
                                    if (rows.size >= limit) break@outer
                                }
                                rows += "--"
                            } else {
                                rows += "${f.relativePath}:${idx + 1}: $line"
                                if (rows.size >= limit) break@outer
                            }
                        }
                    }
                }
                if (rows.isEmpty()) return ToolResult.ok("(no matches)")

                while (rows.isNotEmpty() && rows.last() == "--") rows.removeAt(rows.lastIndex)
                return ToolResult.ok(rows.joinToString("\n"))
            }
            else -> return ToolResult.error("Grep: unknown mode `$mode`. Use files | content | count.")
        }
    }

    private fun compileGlobToRegex(pattern: String): Regex {
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

    companion object {
        private const val MAX_FILE_BYTES = 5L * 1024 * 1024
    }
}
