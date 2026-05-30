package com.webtoapp.core.aicoding.prompt.sections

import com.webtoapp.core.aicoding.prompt.PromptLang

internal object PlanModeSection {
    fun build(lang: PromptLang, planFilePath: String, planExists: Boolean): String {
        return when (lang) {
            PromptLang.EN -> buildEn(planFilePath, planExists)
            PromptLang.ZH -> buildZh(planFilePath, planExists)
        }
    }

    private fun buildEn(planPath: String, planExists: Boolean) = """
        # Plan mode (active)

        You are in plan mode. The user has not approved any implementation work yet. You MUST NOT make changes outside the plan file. Only Read, Glob, Grep, ListFiles, AskUserQuestion, TodoWrite, TodoUpdate, and writes to the plan file are allowed.

        Plan file: $planPath
        ${if (planExists) "An earlier plan draft exists. Read it before editing." else "The plan file does not exist yet — create it with Write."}

        Workflow:
        1. Explore. Read the relevant files. Use Grep / Glob aggressively in parallel.
        2. Design. Pick one approach. Capture trade-offs only when the user has to choose.
        3. Verify alignment. If anything is unclear, AskUserQuestion. Don't ask "is this plan ok?" — that's what ExitPlanMode is for.
        4. Write the plan to $planPath. Sections: **Context** (why), **Approach** (the chosen path), **Files** (paths to touch), **Verification** (how we'll know it worked).
        5. When the plan is complete, call ExitPlanMode. The user will then approve or revise.

        Don't reference "the plan" while talking to the user — they can't see it until you call ExitPlanMode.
    """.trimIndent()

    private fun buildZh(planPath: String, planExists: Boolean) = """
        # Plan 模式（已激活）

        你正在 plan 模式。用户尚未批准任何实现工作。**禁止**修改 plan 文件以外的任何内容。本轮只允许 Read、Glob、Grep、ListFiles、AskUserQuestion、TodoWrite、TodoUpdate，以及对 plan 文件的写操作。

        Plan 文件：$planPath
        ${if (planExists) "已经有一份草稿。修改前先 Read。" else "Plan 文件还不存在 — 用 Write 创建。"}

        工作流程：
        1. 探索。Read 相关文件，并行使用 Grep / Glob。
        2. 设计。选定一种方案。仅在用户需要做选择时才列权衡。
        3. 对齐需求。任何不清楚的地方用 AskUserQuestion 确认。不要问"这个 plan 可以吗"——那是 ExitPlanMode 的职责。
        4. 把 plan 写到 $planPath。包含：**Context**（为什么）、**Approach**（选定方案）、**Files**（要动的文件路径）、**Verification**（怎么验证完成）。
        5. plan 完成后调 ExitPlanMode。用户会审批或要求修改。

        和用户交流时不要提"plan"——他们要等到 ExitPlanMode 之后才能看到。
    """.trimIndent()
}
