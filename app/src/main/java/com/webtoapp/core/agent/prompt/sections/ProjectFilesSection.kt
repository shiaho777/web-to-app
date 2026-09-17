package com.webtoapp.core.agent.prompt.sections

import com.webtoapp.core.agent.prompt.PromptLang

object ProjectFilesSection {
    data class FileSummary(val path: String, val lines: Int, val bytes: Long)

    private const val MAX = 30

    /**
     * [FileSummary.lines] is -1 when counting was skipped (binary file or over
     * the line-count cap) — the row then reports size only, which is the honest
     * signal for a giant upload.
     */
    fun build(lang: PromptLang, files: List<FileSummary>): String {
        if (files.isEmpty()) {
            return when (lang) {
                PromptLang.EN -> "# Project files\n(empty — start from scratch)"
                PromptLang.ZH -> "# 项目文件\n（空 — 从零开始）"
            }
        }

        val header = if (lang == PromptLang.EN) "# Project files" else "# 项目文件"
        val rows = files.take(MAX).joinToString("\n") { f ->
            if (f.lines >= 0) "- ${f.path}  (${f.lines} lines, ${formatBytes(f.bytes)})"
            else "- ${f.path}  (${formatBytes(f.bytes)})"
        }
        val overflow = if (files.size > MAX) {
            if (lang == PromptLang.EN) "\n- … (${files.size - MAX} more)"
            else "\n- … （还有 ${files.size - MAX} 个）"
        } else ""
        return "$header\n$rows$overflow"
    }

    private fun formatBytes(b: Long): String = when {
        b < 1024 -> "${b}B"
        b < 1024 * 1024 -> "${b / 1024}KB"
        else -> "${b / (1024 * 1024)}MB"
    }
}
