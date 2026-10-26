package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.plugin.PluginStore

class ListModulesTool : Tool {
    override val name = "ListModules"
    override val description = """
        List the installed plugins (HCJ packages, userscripts, Chrome extensions).
        Returns each plugin's id, kind, enabled flag, and name. Use this to find a
        plugin id before GetModule or UpdateModule.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("query", "Optional substring to filter plugins by name.")
    }

    override fun isReadOnly() = true

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val query = args.get("query")?.asString?.trim().orEmpty()
        val store = PluginStore.getInstance(ctx.androidContext)
        store.awaitLoaded()
        val all = store.getAllPlugins()
        val filtered = if (query.isEmpty()) all
            else all.filter { it.name.contains(query, ignoreCase = true) }
        if (filtered.isEmpty()) {
            return ToolResult.ok("No plugins found${if (query.isEmpty()) "" else " matching \"$query\""}.")
        }
        val lines = filtered.joinToString("\n") {
            "- id=${it.id}  kind=${it.kind}  enabled=${store.isGloballyEnabled(it.id)}  name=\"${it.name}\""
        }
        return ToolResult.ok("${filtered.size} plugin(s):\n$lines")
    }
}
