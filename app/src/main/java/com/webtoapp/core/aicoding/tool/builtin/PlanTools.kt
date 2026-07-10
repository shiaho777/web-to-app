package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.plan.PlanManager
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult

class EnterPlanModeTool(private val planManager: PlanManager) : Tool {
    override val name = "EnterPlanMode"
    override val description = """
        Enter plan mode before doing non-trivial implementation work. In plan mode you can only Read, Glob, Grep, ListFiles, AskUserQuestion, and write to a single plan file. Use this when:
        - The task touches more than two files.
        - There are multiple valid approaches and the user might want a say.
        - Requirements are ambiguous and need exploration first.
        Skip plan mode for trivial fixes (typos, single-line tweaks).
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {  }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject) = "Entering plan mode"

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        return when (val r = planManager.enter()) {
            is PlanManager.EnterResult.Entered ->
                ToolResult.ok("Entered plan mode. Plan file: ${r.planPath}\nWrite the plan there with Write/Edit, then call ExitPlanMode.")
            is PlanManager.EnterResult.AlreadyActive ->
                ToolResult.ok("Already in plan mode. Plan file: ${r.planPath}")
        }
    }
}

class ExitPlanModeTool(private val planManager: PlanManager) : Tool {
    override val name = "ExitPlanMode"
    override val description = """
        Exit plan mode and request user approval for the plan you wrote. Call this only after the plan file is complete. This stops the current run — the user will review the plan and either approve it (you continue with implementation in the next turn) or request revisions. Don't ask "is this plan ok?" anywhere else — that's what this tool is for.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {  }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject) = "Exiting plan mode"

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        return when (val r = planManager.exit()) {
            is PlanManager.ExitResult.Submitted ->
                ToolResult.okPlanReview("Plan submitted for review. Waiting for user approval.", r.planPath)
            is PlanManager.ExitResult.NoPlanWritten ->
                ToolResult.error("ExitPlanMode: the plan file is empty or missing. Write the plan first.")
            is PlanManager.ExitResult.NotActive ->
                ToolResult.error("ExitPlanMode: not currently in plan mode.")
        }
    }
}
