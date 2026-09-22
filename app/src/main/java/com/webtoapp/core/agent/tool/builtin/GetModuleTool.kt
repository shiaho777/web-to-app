package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.plugin.PluginStore
import com.webtoapp.util.GsonProvider

class GetModuleTool : Tool {
    override val name = "GetModule"
    override val description = """
        Get a plugin's full definition as JSON: its plugin.json manifest plus every
        package file (plugin.html / style.css / files/*). Use ListModules
        to find the id. CreateModule and UpdateModule accept a legacy module JSON
        or this plugin shape.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("moduleId", "The plugin id (from ListModules).", required = true)
    }

    override fun isReadOnly() = true

    override fun activityDescription(args: JsonObject): String? =
        args.get("moduleId")?.asString?.let { "Reading plugin $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val moduleId = args.get("moduleId")?.asString
            ?: return ToolResult.error("GetModule: missing `moduleId`.")
        val store = PluginStore.getInstance(ctx.androidContext)
        store.awaitLoaded()
        val plugin = store.getPlugin(moduleId)
            ?: return ToolResult.error("GetModule: no plugin with id $moduleId.")
        val files = store.readPackageFiles(plugin.id)
        val out = buildString {
            appendLine("Plugin id=${plugin.id} name=\"${plugin.name}\" kind=${plugin.kind}")
            appendLine()
            appendLine(GsonProvider.gson.toJson(plugin))
            files.forEach { (rel, content) ->
                appendLine()
                appendLine("--- $rel ---")
                appendLine(content)
            }
        }
        return ToolResult.ok(out.trimEnd())
    }
}
