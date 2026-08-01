package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.data.model.toManifestJson

class GetAppTool : Tool {
    override val name = "GetApp"
    override val description = """
        Get the full configuration of an app as JSON (its manifest), covering both core
        (type-specific) config and common config. Use ListApps to find the id. The returned
        JSON is the exact shape UpdateApp accepts as a patch.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id (from ListApps).", required = true)
    }

    override fun isReadOnly() = true

    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Reading app $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong
            ?: return ToolResult.error("GetApp: missing `appId`.")
        val app = ctx.appRepository.getWebApp(appId)
            ?: return ToolResult.error("GetApp: no app with id $appId.")
        return ToolResult.ok(
            "App id=${app.id} name=\"${app.name}\" type=${app.appType}\n\n${app.toManifestJson()}"
        )
    }
}
