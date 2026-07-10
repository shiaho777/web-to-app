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

        You are in plan mode. The user has not approved any implementation work yet. You MUST NOT make changes outside the plan file. Only Read, Glob, Grep, ListFiles, AskUserQuestion, and writes to the plan file are allowed.

        Plan file: $planPath
        ${if (planExists) "An earlier plan draft exists. Read it before editing." else "The plan file does not exist yet — create it with Write."}

        Workflow:
        1. Explore. Read the relevant files. Use Grep / Glob aggressively in parallel. Share what you find with the user as you go — brief findings, not silence.
        2. Design. Pick one approach. Capture trade-offs only when the user has to choose.
        3. Verify alignment. If anything is unclear, AskUserQuestion. Don't ask "is this plan ok?" — that's what ExitPlanMode is for.
        4. Write the plan to $planPath. Sections: **Context** (why), **Approach** (the chosen path), **Files** (paths to touch), **Verification** (how we'll know it worked).
        5. When the plan is complete, call ExitPlanMode. The run will stop and the user will review the plan, then approve or request revisions.

        Keep the user informed throughout — share exploration findings, the approach you're leaning towards, and why. Just don't dump the full plan text in chat; the plan file is the single source of truth and ExitPlanMode will show it for approval.
    """.trimIndent()

    private fun buildZh(planPath: String, planExists: Boolean) = """
        # Plan 模式（已激活）

        你正在 plan 模式。用户尚未批准任何实现工作。**禁止**修改 plan 文件以外的任何内容。本轮只允许 Read、Glob、Grep、ListFiles、AskUserQuestion，以及对 plan 文件的写操作。

        Plan 文件：$planPath
        ${if (planExists) "已经有一份草稿。修改前先 Read。" else "Plan 文件还不存在 — 用 Write 创建。"}

        工作流程：
        1. 探索。Read 相关文件，并行使用 Grep / Glob。边探索边向用户同步发现——简短结论，不要沉默。
        2. 设计。选定一种方案。仅在用户需要做选择时才列权衡。
        3. 对齐需求。任何不清楚的地方用 AskUserQuestion 确认。不要问"这个 plan 可以吗"——那是 ExitPlanMode 的职责。
        4. 把 plan 写到 $planPath。包含：**Context**（为什么）、**Approach**（选定方案）、**Files**（要动的文件路径）、**Verification**（怎么验证完成）。
        5. plan 完成后调 ExitPlanMode。本轮会终止，用户审阅 plan 后批准或要求修改。

        全程保持和用户沟通——分享探索发现、倾向的方案及理由。只是别在聊天里贴完整 plan 文本；plan 文件是唯一事实来源，ExitPlanMode 会把它展示给用户审批。
    """.trimIndent()
}
