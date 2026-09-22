package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.plugin.PluginManifest
import com.webtoapp.core.plugin.PluginStore
import com.webtoapp.util.GsonProvider

class UpdateModuleTool : Tool {
    override val description = """
        Edit an existing plugin package. Provide a partial JSON patch: top-level
        fields merge into plugin.json; an optional "files" object maps
        package-relative paths ("plugin.html", "style.css", "files/x") to new
        file contents. The plugin id is always kept. Inspect the current shape
        with GetModule first.
    """.trimIndent()

    override val name = "UpdateModule"

    override val parametersSchema: JsonElement = jsonSchema {
        string("moduleId", "The plugin id (from ListModules).", required = true)
        string("patch", "Partial plugin JSON containing only the fields to change.", required = true)
    }

    override fun activityDescription(args: JsonObject): String? =
        args.get("moduleId")?.asString?.let { "Updating plugin $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val moduleId = args.get("moduleId")?.asString
            ?: return ToolResult.error("UpdateModule: missing `moduleId`.")
        val patchStr = args.get("patch")?.asString
            ?: return ToolResult.error("UpdateModule: missing `patch`.")
        val store = PluginStore.getInstance(ctx.androidContext)
        store.awaitLoaded()
        val plugin = store.getPlugin(moduleId)
            ?: return ToolResult.error("UpdateModule: no plugin with id $moduleId.")
        if (!plugin.isScriptPlugin) {
            return ToolResult.error("UpdateModule: only HCJ/userscript packages are editable.")
        }
        val patch = runCatching { JsonParser.parseString(patchStr).asJsonObject }.getOrNull()
            ?: return ToolResult.error("UpdateModule: `patch` is not valid JSON.")

        val files = store.readPackageFiles(plugin.id)
        val manifestObj = runCatching {
            files[PluginStore.MANIFEST_FILE]?.let { JsonParser.parseString(it).asJsonObject }
        }.getOrNull() ?: JsonObject()

        val filePatches = mutableMapOf<String, String>()
        patch.entrySet().forEach { (k, v) ->
            if (k == "files" && v.isJsonObject) {
                v.asJsonObject.entrySet().forEach { (rel, content) ->
                    if (!rel.contains("..")) filePatches[rel] = content.asString
                }
            } else {
                manifestObj.add(k, v)
            }
        }
        manifestObj.addProperty("id", plugin.id)

        val manifest = PluginManifest.fromJson(GsonProvider.gson.toJson(manifestObj))
            ?: return ToolResult.error("UpdateModule: merged manifest is malformed.")

        val newFiles = files - PluginStore.MANIFEST_FILE + filePatches
        return store.installPackage(manifest, plugin.kind, newFiles).fold(
            onSuccess = { ToolResult.ok("Updated plugin id=${it.id} name=\"${it.name}\".") },
            onFailure = { ToolResult.error("UpdateModule failed: ${it.message}") }
        )
    }
}
