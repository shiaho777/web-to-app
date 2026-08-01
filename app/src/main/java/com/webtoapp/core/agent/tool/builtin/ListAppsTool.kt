package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import kotlinx.coroutines.flow.first

class ListAppsTool : Tool {
    override val name = "ListApps"
    override val description = """
        List the apps in the user's WebToApp app list. Returns each app's id, type, and name.
        Use this to find an app's id before calling GetApp or UpdateApp.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("query", "Optional substring to filter apps by name.")
    }

    override fun isReadOnly() = true

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val query = args.get("query")?.asString?.trim().orEmpty()
        val apps = ctx.appRepository.allWebApps.first()
        val filtered = if (query.isEmpty()) apps
            else apps.filter { it.name.contains(query, ignoreCase = true) }
        if (filtered.isEmpty()) {
            return ToolResult.ok("No apps found${if (query.isEmpty()) "" else " matching \"$query\""}.")
        }
        val lines = filtered.joinToString("\n") {
            "- id=${it.id}  type=${it.appType}  name=\"${it.name}\""
        }
        return ToolResult.ok("${filtered.size} app(s):\n$lines")
    }
}
