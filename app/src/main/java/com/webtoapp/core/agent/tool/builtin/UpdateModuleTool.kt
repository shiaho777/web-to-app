package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.extension.ExtensionManager
import com.webtoapp.core.extension.ExtensionModule
import com.webtoapp.data.converter.Converters
import com.webtoapp.util.GsonProvider

class UpdateModuleTool : Tool {
    override val description = """
        Edit an existing extension module. Provide a partial module JSON patch; only the fields
        you include are changed and everything else is preserved. The module id is always kept.
        Inspect the current shape with GetModule first.
    """.trimIndent()

    override val name = "UpdateModule"

    override val parametersSchema: JsonElement = jsonSchema {
        string("moduleId", "The module id (from ListModules).", required = true)
        string("patch", "Partial module JSON containing only the fields to change.", required = true)
    }

    override fun activityDescription(args: JsonObject): String? =
        args.get("moduleId")?.asString?.let { "Updating module $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val moduleId = args.get("moduleId")?.asString
            ?: return ToolResult.error("UpdateModule: missing `moduleId`.")
        val patchStr = args.get("patch")?.asString
            ?: return ToolResult.error("UpdateModule: missing `patch`.")
        val mgr = ExtensionManager.getInstance(ctx.androidContext)
        mgr.awaitLoaded()
        val existing = mgr.getModuleById(moduleId)?.let { mgr.ensureCodeLoaded(it) }
            ?: return ToolResult.error("UpdateModule: no module with id $moduleId.")
        val patch = runCatching { JsonParser.parseString(patchStr) }.getOrNull()
            ?: return ToolResult.error("UpdateModule: `patch` is not valid JSON.")

        val existingTree = GsonProvider.gson.toJsonTree(existing)
        val merged = Converters.mergeMissingDefaults(existingTree, patch)
        val updated = runCatching { GsonProvider.gson.fromJson(merged, ExtensionModule::class.java) }
            .getOrNull()?.sanitized()
            ?: return ToolResult.error("UpdateModule: failed to apply the patch.")

        val safe = updated.copy(id = existing.id)
        return mgr.updateModule(safe).fold(
            onSuccess = { ToolResult.ok("Updated module id=${it.id} name=\"${it.name}\".") },
            onFailure = { ToolResult.error("UpdateModule failed: ${it.message}") }
        )
    }
}
