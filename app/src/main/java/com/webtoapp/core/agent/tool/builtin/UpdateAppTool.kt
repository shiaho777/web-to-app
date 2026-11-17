package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.agent.tool.AppChange
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.data.converter.Converters
import com.webtoapp.data.model.WebApp

class UpdateAppTool : Tool {
    override val name = "UpdateApp"
    override val description = """
        Edit an existing app's configuration. Provide a partial manifest JSON patch; only the
        fields you include are changed and everything else is preserved. This edits both core
        (type-specific) config and common config. Inspect the current shape with GetApp first.
        The app's id and type are always preserved.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id (from ListApps).", required = true)
        string("patch", "Partial WebApp manifest JSON containing only the fields to change.", required = true)
    }

    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Updating app $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong
            ?: return ToolResult.error("UpdateApp: missing `appId`.")
        val patchStr = args.get("patch")?.asString
            ?: return ToolResult.error("UpdateApp: missing `patch`.")
        val existing = ctx.appRepository.getWebApp(appId)
            ?: return ToolResult.error("UpdateApp: no app with id $appId.")
        val patch = runCatching { JsonParser.parseString(patchStr) }.getOrNull()
            ?: return ToolResult.error("UpdateApp: `patch` is not valid JSON.")

        val existingTree = Converters.gson.toJsonTree(existing)
        val merged = Converters.mergeMissingDefaults(existingTree, patch)
        val updated = runCatching { Converters.gson.fromJson(merged, WebApp::class.java) }.getOrNull()
            ?: return ToolResult.error("UpdateApp: failed to apply the patch.")

        val safe = updated.copy(
            id = existing.id,
            appType = existing.appType,
            createdAt = existing.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        val mergedObj = merged.asJsonObject
        val existingObj = existingTree.asJsonObject
        val requestedKeys = patch.takeIf { it.isJsonObject }?.asJsonObject?.keySet()?.toList().orEmpty()
        val changedFields = requestedKeys.filter { key ->
            mergedObj.get(key) != existingObj.get(key)
        }
        val droppedFields = requestedKeys - changedFields.toSet()
        if (changedFields.isEmpty()) {
            return ToolResult.error(
                "UpdateApp: no field changed. The patch values match the current config or were " +
                    "rejected as type-incompatible: ${droppedFields.joinToString(", ")}. " +
                    "Inspect the app with GetApp and patch again with values of the declared types."
            )
        }
        ctx.appRepository.updateWebApp(safe)
        val droppedNote = if (droppedFields.isEmpty()) "" else
            " Ignored (unchanged or type-incompatible): ${droppedFields.joinToString(", ")}."
        return ToolResult(
            text = "Updated app id=${safe.id} name=\"${safe.name}\" (type ${safe.appType}). " +
                "Changed: ${changedFields.joinToString(", ")}.$droppedNote",
            appChange = AppChange(
                appId = safe.id,
                appName = safe.name,
                appType = safe.appType.name,
                kind = AppChange.Kind.UPDATE,
                changedFields = changedFields
            )
        )
    }
}
