package com.webtoapp.core.agent.prompt.sections

import com.webtoapp.core.agent.prompt.PromptLang
internal object ToolUsageSection {
    fun build(lang: PromptLang): String = when (lang) {
        PromptLang.EN -> en
        PromptLang.ZH -> zh
    }

    private val en = """
        # Using tools

        Use the declared tool schemas for exact arguments. Find files with Glob, Grep, or ListFiles. Use Read before modifying an existing file and Edit for targeted changes.

        Batch independent reads. Wait for each write or tool result when the next action depends on it. A tool's permission prompt governs actions that need approval.

        Use TodoWrite for work that benefits from a visible checklist. Update it as real steps advance. Keep task progress and verification grounded in tool results.
    """.trimIndent()

    private val zh = """
        # 使用工具

        按已声明的工具参数构造调用。用 Glob、Grep 或 ListFiles 定位文件。改动已有文件前先 Read，局部修改用 Edit。

        互不依赖的读取可以批量执行。下一步依赖写入或工具结果时，等前一步完成。需要批准的动作以工具权限确认结果为准。

        适合用清单跟踪的任务可用 TodoWrite，实际进展或验证结果发生变化时再更新。
    """.trimIndent()
}
