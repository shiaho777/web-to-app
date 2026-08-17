package com.webtoapp.core.agent.permission

import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.google.gson.JsonObject

class PermissionChecker(
    private val prompter: PermissionPrompter,
    initialMode: PermissionMode = PermissionMode.Default
) {

    @Volatile var mode: PermissionMode = initialMode
        private set

    // Baseline mode outside plan sessions. Plan mode is a temporary overlay on top
    // of this baseline; approve()/resetToBaseline() return to it so an enabled
    // auto-approve preference survives a plan round-trip, and a Plan mode left
    // over from an abandoned plan review never leaks into later sessions.
    @Volatile private var baselineMode: PermissionMode = initialMode

    private val alwaysAllow = mutableSetOf<String>()

    private val planAllowedTools = setOf(
        "Read", "Glob", "Grep", "ListFiles",
        "AskUserQuestion",
        "EnterPlanMode", "ExitPlanMode"
    )
    private val planWriteTools = setOf("Write", "Edit", "Delete")

    fun allowedToolNames(): Set<String>? = when (mode) {
        PermissionMode.Plan -> planAllowedTools + planWriteTools
        else -> null
    }

    fun setMode(newMode: PermissionMode) {
        mode = newMode
    }

    /**
     * Record the user's auto-approve preference as the baseline mode. Applied
     * immediately only when no plan session is active — clobbering an active
     * Plan mode here would silently re-allow writes the plan prompt forbids.
     */
    fun syncAutoApprove(enabled: Boolean) {
        baselineMode = if (enabled) PermissionMode.AutoApprove else PermissionMode.Default
        if (mode != PermissionMode.Plan) mode = baselineMode
    }

    /** Drop any active Plan overlay and return to the baseline mode. */
    fun resetToBaseline() {
        mode = baselineMode
    }

    fun isAlwaysAllowed(toolName: String): Boolean = toolName in alwaysAllow

    suspend fun check(tool: Tool, args: JsonObject, ctx: ToolContext): PermissionDecision {
        return when (mode) {
            PermissionMode.AutoApprove -> PermissionDecision.Allow
            PermissionMode.Plan -> checkPlan(tool, args, ctx)
            PermissionMode.Dream -> checkDream(tool, args, ctx)
            PermissionMode.Default -> checkDefault(tool, args, ctx)
        }
    }

    private fun checkPlan(tool: Tool, args: JsonObject, ctx: ToolContext): PermissionDecision {
        if (tool.name in planAllowedTools) return PermissionDecision.Allow
        if (tool.name in planWriteTools) {
            val path = args.get("path")?.asString ?: args.get("file_path")?.asString
            val resolved = path?.let { ctx.resolveSafePath(it) }
            if (resolved != null && resolved == ctx.activePlanFile) return PermissionDecision.Allow
            return PermissionDecision.Deny
        }
        return PermissionDecision.Deny
    }

    private fun checkDream(tool: Tool, args: JsonObject, ctx: ToolContext): PermissionDecision {
        if (tool.isReadOnly()) return PermissionDecision.Allow
        if (tool.name in planWriteTools) {
            val path = args.get("path")?.asString ?: args.get("file_path")?.asString
            val resolved = path?.let { ctx.resolveSafePath(it) }
            if (resolved != null && resolved.startsWith(MEMORY_DIR_PREFIX)) return PermissionDecision.Allow
            return PermissionDecision.Deny
        }
        return PermissionDecision.Deny
    }

    private suspend fun checkDefault(tool: Tool, args: JsonObject, ctx: ToolContext): PermissionDecision {
        if (tool.isReadOnly()) return PermissionDecision.Allow
        if (tool.name in alwaysAllow) return PermissionDecision.Allow
        val req = PermissionRequest(
            toolCallId = "perm-${System.nanoTime()}",
            toolName = tool.name,
            activity = tool.activityDescription(args),
            argsPreview = previewArgs(args)
        )
        return when (prompter.request(req)) {
            is PermissionResponse.Allow -> PermissionDecision.Allow
            is PermissionResponse.AlwaysAllow -> {
                alwaysAllow += tool.name
                PermissionDecision.Allow
            }
            is PermissionResponse.Deny -> PermissionDecision.Deny
        }
    }

    private fun previewArgs(args: JsonObject): Map<String, String> {
        val out = LinkedHashMap<String, String>()
        for ((k, v) in args.entrySet()) {
            val asString = if (v.isJsonPrimitive) v.asJsonPrimitive.asString else v.toString()
            out[k] = if (asString.length > MAX_PREVIEW_LEN) asString.take(MAX_PREVIEW_LEN) + "…" else asString
        }
        return out
    }

    companion object {
        private const val MEMORY_DIR_PREFIX = ".memory/"
        private const val MAX_PREVIEW_LEN = 200
    }
}
