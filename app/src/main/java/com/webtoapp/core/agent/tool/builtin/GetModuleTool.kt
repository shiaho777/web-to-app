package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.extension.ExtensionManager
import com.webtoapp.util.GsonProvider

class GetModuleTool : Tool {
    override val name = "GetModule"
    override val description = """
        Get a module's full definition as JSON, including its code/css. Use ListModules to find
        the id. The returned JSON is the exact shape CreateModule and UpdateModule accept.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("moduleId", "The module id (from ListModules).", required = true)
    }

    override fun isReadOnly() = true

    override fun activityDescription(args: JsonObject): String? =
        args.get("moduleId")?.asString?.let { "Reading module $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val moduleId = args.get("moduleId")?.asString
            ?: return ToolResult.error("GetModule: missing `moduleId`.")
        val mgr = ExtensionManager.getInstance(ctx.androidContext)
        mgr.awaitLoaded()
        val module = mgr.getModuleById(moduleId)?.let { mgr.ensureCodeLoaded(it) }
            ?: return ToolResult.error("GetModule: no module with id $moduleId.")
        return ToolResult.ok(
            "Module id=${module.id} name=\"${module.name}\" type=${module.sourceType}\n\n" +
                GsonProvider.gson.toJson(module)
        )
    }
}
