package com.webtoapp.core.aicoding.prompt.sections

import com.webtoapp.core.aicoding.prompt.PromptLang
import com.webtoapp.core.aicoding.tool.Tool

internal object ToolUsageSection {
    fun build(lang: PromptLang, tools: List<Tool>): String {
        val list = tools.joinToString("\n") { "- ${it.name}: ${it.description.lineSequence().first().trim()}" }
        return when (lang) {
            PromptLang.EN -> buildEn(list)
            PromptLang.ZH -> buildZh(list)
        }
    }

    private fun buildEn(toolList: String) = """
        # Available tools

        $toolList

        Tool selection rules:
        - Read for any file you intend to look at; never use a side channel.
        - Write only for new files or full rewrites; prefer Edit when the change is local.
        - Edit fails on non-unique matches: include enough surrounding context that old_string occurs exactly once, or pass replace_all=true.
        - Glob to find files by name; Grep to find files by content.
        - ListFiles for a quick directory snapshot.
        - AskUserQuestion only for genuine clarifications. Don't use it to ask "Should I proceed?" — that's what plan mode is for.

        Independent tool calls in a single turn run in parallel. Batch reads aggressively. Sequential calls only when one depends on another's output.

        TodoWrite at the start of any task with three or more steps. Mark each in_progress when you begin and completed when done — the user sees a live checklist.
    """.trimIndent()

    private fun buildZh(toolList: String) = """
        # 可用工具

        $toolList

        工具选择规则：
        - 要看的文件先用 Read；不要绕道。
        - Write 只用于新建文件或完整重写；局部修改优先用 Edit。
        - Edit 在 old_string 不唯一时会失败：要么扩展上下文让 old_string 唯一，要么传 replace_all=true。
        - 按文件名找文件用 Glob；按内容找文件用 Grep。
        - ListFiles 用来快速看目录。
        - AskUserQuestion 只用于真正需要澄清的时候。不要用它来问"我可以继续吗？"——那是 plan mode 的活。

        同一轮中互不依赖的工具调用会被并行执行。读类工具尽量批量调。顺序调用只在确实依赖前一个结果时使用。

        三步以上的任务，开头先 TodoWrite。开始一项时设 in_progress，完成时设 completed——用户能看到这份实时清单。
    """.trimIndent()
}
