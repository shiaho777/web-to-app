package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.BuiltApkInfo
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.apkbuilder.BuildResult
import com.webtoapp.core.export.AppExporter
import com.webtoapp.core.export.ExportResult
import com.webtoapp.core.export.ShortcutResult
import com.webtoapp.core.playstore.aab.AabExportCoordinator
import kotlinx.coroutines.flow.first

class BuildApkTool : Tool {
    override val name = "BuildApk"
    override val description = """
        Build a signed APK for an app. Returns the APK path and size on success.
        Use ListApps/GetApp to find the appId. This is a potentially slow operation.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id to build.", required = true)
        boolean("forceFullRebuild", "Force a full rebuild instead of incremental.", default = false)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Building APK for app $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong ?: return ToolResult.error("BuildApk: missing appId.")
        val force = args.get("forceFullRebuild")?.asBoolean ?: false
        val app = ctx.appRepository.getWebApp(appId) ?: return ToolResult.error("BuildApk: app $appId not found.")
        val builder = ApkBuilder(ctx.androidContext)
        return when (val result = builder.buildApk(app, force)) {
            is BuildResult.Success -> {
                val sizeKb = result.apkFile.length() / 1024
                val versionName = extractVersionFromName(result.apkFile.name) ?: "1.0.0"
                val info = BuiltApkInfo(
                    appId = appId,
                    apkName = result.apkFile.name,
                    apkPath = result.apkFile.absolutePath,
                    sizeBytes = result.apkFile.length(),
                    buildMode = result.buildMode,
                    packageName = app.packageName,
                    versionName = versionName
                )
                ToolResult(
                    text = "APK built successfully: ${result.apkFile.absolutePath} (${sizeKb}KB, ${result.buildMode}).",
                    isError = false,
                    builtApk = info
                )
            }
            is BuildResult.Error -> ToolResult.error("BuildApk failed: ${result.message}")
        }
    }

    private fun extractVersionFromName(fileName: String): String? {
        // APK file name format: "{AppName}_v{version}.APK"
        val idx = fileName.lastIndexOf("_v")
        if (idx < 0) return null
        val afterV = fileName.substring(idx + 2)
        val dot = afterV.lastIndexOf('.')
        return if (dot > 0) afterV.substring(0, dot) else afterV.takeIf { it.isNotEmpty() }
    }
}

class ShareApkTool : Tool {
    override val name = "ShareApk"
    override val description = """
        Build an APK for an app and return its file path so the user can share it manually
        from the file manager. (The agent runs in a background service and cannot open the
        system share dialog directly.)
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Building shareable APK for app $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong ?: return ToolResult.error("ShareApk: missing appId.")
        val app = ctx.appRepository.getWebApp(appId) ?: return ToolResult.error("ShareApk: app $appId not found.")
        val builder = ApkBuilder(ctx.androidContext)
        return when (val result = builder.buildApk(app)) {
            is BuildResult.Success -> {
                val sizeKb = result.apkFile.length() / 1024
                ToolResult.ok("APK ready at ${result.apkFile.absolutePath} (${sizeKb}KB). The user can share it from the file manager.")
            }
            is BuildResult.Error -> ToolResult.error("ShareApk failed: ${result.message}")
        }
    }
}

class ExportAppTool : Tool {
    override val name = "ExportApp"
    override val description = """
        Export an app as a Gradle project template (full source tree) or a config JSON.
        - format "template": generates a complete Gradle Android project in Documents/WebToApp/.
        - format "config": exports the app's configuration as a JSON string.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id.", required = true)
        enum("format", listOf("template", "config"), "Export format.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Exporting app $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong ?: return ToolResult.error("ExportApp: missing appId.")
        val format = args.get("format")?.asString ?: "template"
        val app = ctx.appRepository.getWebApp(appId) ?: return ToolResult.error("ExportApp: app $appId not found.")
        val exporter = AppExporter(ctx.androidContext)
        val result = if (format == "config") exporter.exportConfig(app) else exporter.exportAsTemplate(app)
        return when (result) {
            is ExportResult.Success -> ToolResult.ok("Exported to: ${result.path}")
            is ExportResult.Error -> ToolResult.error("ExportApp failed: ${result.message}")
        }
    }
}

class CreateShortcutTool : Tool {
    override val name = "CreateShortcut"
    override val description = """
        Create a home-screen launcher shortcut for an app.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Creating shortcut for app $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong ?: return ToolResult.error("CreateShortcut: missing appId.")
        val app = ctx.appRepository.getWebApp(appId) ?: return ToolResult.error("CreateShortcut: app $appId not found.")
        val exporter = AppExporter(ctx.androidContext)
        return when (val result = exporter.createShortcut(app)) {
            is ShortcutResult.Success -> ToolResult.ok("Shortcut created for \"${app.name}\".")
            is ShortcutResult.Pending -> ToolResult.ok("Shortcut creation pending: ${result.message}")
            is ShortcutResult.PermissionRequired -> ToolResult.error("Shortcut requires permission: ${result.message}")
            is ShortcutResult.Error -> ToolResult.error("CreateShortcut failed: ${result.message}")
        }
    }
}

class MoveToCategoryTool : Tool {
    override val name = "MoveToCategory"
    override val description = """
        Move an app to a category, or remove it from all categories (categoryId=null).
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id.", required = true)
        integer("categoryId", "The category id, or omit/null to remove from category.")
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Moving app $it to category" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong ?: return ToolResult.error("MoveToCategory: missing appId.")
        val categoryId = if (args.has("categoryId") && !args.get("categoryId").isJsonNull)
            args.get("categoryId").asLong else null
        val app = ctx.appRepository.getWebApp(appId) ?: return ToolResult.error("MoveToCategory: app $appId not found.")
        ctx.appRepository.updateWebApp(app.copy(categoryId = categoryId))
        return ToolResult.ok("App \"${app.name}\" moved to ${categoryId ?: "no category"}.")
    }
}

class DeleteAppTool : Tool {
    override val name = "DeleteApp"
    override val description = """
        Delete an app from the app list. This is permanent and cannot be undone.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id to delete.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Deleting app $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong ?: return ToolResult.error("DeleteApp: missing appId.")
        val app = ctx.appRepository.getWebApp(appId) ?: return ToolResult.error("DeleteApp: app $appId not found.")
        ctx.appRepository.deleteWebAppById(appId)
        return ToolResult.ok("Deleted app \"${app.name}\".")
    }
}

class DuplicateAppTool : Tool {
    override val name = "DuplicateApp"
    override val description = """
        Duplicate (copy) an existing app with a new name. Returns the new app id.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id to duplicate.", required = true)
        string("newName", "Name for the duplicate (defaults to original name + copy).")
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Duplicating app $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong ?: return ToolResult.error("DuplicateApp: missing appId.")
        val newName = args.get("newName")?.asString
        val newId = ctx.appRepository.duplicateWebApp(appId, newName)
            ?: return ToolResult.error("DuplicateApp: app $appId not found.")
        return ToolResult.ok("Duplicated to new app id=$newId.")
    }
}

class ExportAabTool : Tool {
    override val name = "ExportAab"
    override val description = """
        Export a Google Play-ready AAB for an app. Builds an APK first if needed, then
        converts to a signed AAB with targetSdk rewritten for Play. Potentially slow.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Exporting AAB for app $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong ?: return ToolResult.error("ExportAab: missing appId.")
        val app = ctx.appRepository.getWebApp(appId) ?: return ToolResult.error("ExportAab: app $appId not found.")
        val coordinator = AabExportCoordinator(ctx.androidContext)
        return try {
            val result = coordinator.export(app)
            val sizeKb = result.signedAab.length() / 1024
            ToolResult.ok("AAB exported: ${result.signedAab.absolutePath} (${sizeKb}KB).")
        } catch (e: Exception) {
            ToolResult.error("ExportAab failed: ${e.message}")
        }
    }
}
