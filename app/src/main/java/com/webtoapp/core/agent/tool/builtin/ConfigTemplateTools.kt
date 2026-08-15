package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.data.repository.ConfigTemplateStore

class ListConfigTemplatesTool : Tool {
    override val name = "ListConfigTemplates"
    override val description = """
        List saved common-config templates (named WebViewConfig snapshots users apply to
        apps). Returns each template's name and creation time.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {}
    override fun isReadOnly() = true
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val templates = ConfigTemplateStore.list(ctx.androidContext)
        if (templates.isEmpty()) return ToolResult.ok("No config templates saved yet.")
        val lines = templates.joinToString("\n") {
            "- ${it.name} (created ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date(it.createdAt))})"
        }
        return ToolResult.ok("${templates.size} template(s):\n$lines")
    }
}

class SaveConfigTemplateTool : Tool {
    override val name = "SaveConfigTemplate"
    override val description = """
        Save an app's current common config (WebViewConfig) as a named template the user
        can later apply to other apps. Overwrites an existing template with the same
        name (case-insensitive). Use ListApps/GetApp to find the appId.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        string("name", "Template name (1-40 chars).", required = true)
        integer("appId", "The app whose current config to snapshot.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("name")?.asString?.let { "Saving config template $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val name = args.get("name")?.asString?.trim()
            ?: return ToolResult.error("SaveConfigTemplate: missing `name`.")
        val appId = args.get("appId")?.asLong
            ?: return ToolResult.error("SaveConfigTemplate: missing `appId`.")
        val app = ctx.appRepository.getWebApp(appId)
            ?: return ToolResult.error("SaveConfigTemplate: no app with id $appId.")
        if (!ConfigTemplateStore.save(ctx.androidContext, name, app.webViewConfig)) {
            return ToolResult.error("SaveConfigTemplate: invalid name (empty or longer than 40 chars).")
        }
        return ToolResult.ok("Saved template \"$name\" from app id=$appId.")
    }
}

class ApplyConfigTemplateTool : Tool {
    override val name = "ApplyConfigTemplate"
    override val description = """
        Apply a saved common-config template to an app, replacing its whole WebViewConfig
        with the template snapshot (a template is a full snapshot, never a partial merge).
        Use ListConfigTemplates to find template names and ListApps/GetApp for the appId.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        string("name", "Template name to apply.", required = true)
        integer("appId", "The app to apply the template to.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        "Applying config template ${args.get("name")?.asString}"
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val name = args.get("name")?.asString?.trim()
            ?: return ToolResult.error("ApplyConfigTemplate: missing `name`.")
        val appId = args.get("appId")?.asLong
            ?: return ToolResult.error("ApplyConfigTemplate: missing `appId`.")
        val template = ConfigTemplateStore.get(ctx.androidContext, name)
            ?: return ToolResult.error("ApplyConfigTemplate: no template named \"$name\" (see ListConfigTemplates).")
        val app = ctx.appRepository.getWebApp(appId)
            ?: return ToolResult.error("ApplyConfigTemplate: no app with id $appId.")
        val updated = app.copy(webViewConfig = template.webViewConfig)
        ctx.appRepository.updateWebApp(updated)
        return ToolResult.ok("Applied template \"${template.name}\" to app id=${app.id} name=\"${app.name}\".")
    }
}

class DeleteConfigTemplateTool : Tool {
    override val name = "DeleteConfigTemplate"
    override val description = "Delete a saved common-config template by name."
    override val parametersSchema: JsonElement = jsonSchema {
        string("name", "Template name to delete.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        "Deleting config template ${args.get("name")?.asString}"
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val name = args.get("name")?.asString?.trim()
            ?: return ToolResult.error("DeleteConfigTemplate: missing `name`.")
        if (!ConfigTemplateStore.delete(ctx.androidContext, name)) {
            return ToolResult.error("DeleteConfigTemplate: no template named \"$name\".")
        }
        return ToolResult.ok("Deleted template \"$name\".")
    }
}
