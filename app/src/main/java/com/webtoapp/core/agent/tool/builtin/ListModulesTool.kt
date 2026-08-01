package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.extension.ExtensionManager

class ListModulesTool : Tool {
    override val name = "ListModules"
    override val description = """
        List the installed extension modules. Returns each module's id, sourceType, category,
        enabled flag, and name. Use this to find a module id before GetModule or UpdateModule.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("query", "Optional substring to filter modules by name.")
    }

    override fun isReadOnly() = true

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val query = args.get("query")?.asString?.trim().orEmpty()
        val mgr = ExtensionManager.getInstance(ctx.androidContext)
        mgr.awaitLoaded()
        val all = mgr.getAllModules()
        val filtered = if (query.isEmpty()) all
            else all.filter { it.name.contains(query, ignoreCase = true) }
        if (filtered.isEmpty()) {
            return ToolResult.ok("No modules found${if (query.isEmpty()) "" else " matching \"$query\""}.")
        }
        val lines = filtered.joinToString("\n") {
            "- id=${it.id}  type=${it.sourceType}  category=${it.category}  enabled=${it.enabled}  name=\"${it.name}\""
        }
        return ToolResult.ok("${filtered.size} module(s):\n$lines")
    }
}
