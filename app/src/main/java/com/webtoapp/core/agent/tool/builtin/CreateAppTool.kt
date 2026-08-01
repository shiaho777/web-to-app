package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.agent.export.DetectedArtifact
import com.webtoapp.core.agent.export.SaveSessionAsAppUseCase
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.port.PortManager
import com.webtoapp.core.wordpress.WordPressManager
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.ManifestUtils
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WordPressConfig
import com.webtoapp.util.IconStorage
import java.util.UUID

class CreateAppTool : Tool {
    override val name = "CreateApp"
    override val description = """
        Create a new app in WebToApp and add it to the app list.
        - Config-only types (WEB, IMAGE, VIDEO, GALLERY, MULTI_WEB): pass `manifest` JSON with the
          config, e.g. {"url":"https://example.com"} for WEB, or galleryConfig / multiWebConfig /
          mediaConfig for the others. GALLERY/MULTI_WEB may instead pass `sourceDir` pointing to a
          sandbox folder containing gallery.json / multi-web.json.
        - Project types (HTML, FRONTEND, NODEJS_APP, PHP_APP, PYTHON_APP, GO_APP): first write the
          project files into the session sandbox, then pass `sourceDir` (the sandbox-relative folder).
        - WORDPRESS: created from bundled WordPress (dependencies must be installed); `manifest` may
          carry siteTitle / adminUser / adminEmail.
        Optionally pass `iconRef` (sandbox-relative path to an image) to set the app icon.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        enum("appType", AppType.entries.map { it.name }, "The type of app to create.", required = true)
        string("name", "The app name.", required = true)
        string("iconRef", "Sandbox-relative path to an image to use as the app icon.")
        string("manifest", "Partial WebApp manifest JSON (config-only types and WORDPRESS).")
        string("sourceDir", "Sandbox-relative folder containing the project files (project types).")
    }

    override fun activityDescription(args: JsonObject): String? =
        args.get("name")?.asString?.let { "Creating app \"$it\"" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appType = args.get("appType")?.asString
            ?.let { runCatching { AppType.valueOf(it.trim()) }.getOrNull() }
            ?: return ToolResult.error("CreateApp: missing or invalid `appType`.")
        val name = args.get("name")?.asString?.trim().orEmpty()
        if (name.isEmpty()) return ToolResult.error("CreateApp: missing `name`.")
        val iconRef = ctx.resolveSafePath(args.get("iconRef")?.asString)
        val manifest = args.get("manifest")?.asString
        val sourceDir = ctx.resolveSafePath(args.get("sourceDir")?.asString)

        val iconPath = iconRef?.let { ref ->
            ctx.fileManager.readBytes(ctx.sessionId, ref)?.let { bytes ->
                IconStorage.saveIconFromBytes(ctx.androidContext, bytes)
            }
        }

        return when {
            appType == AppType.WORDPRESS -> createWordPress(ctx, name, iconPath, manifest)
            appType in PROJECT_TYPES -> createFromSource(ctx, appType, name, iconPath, sourceDir)
            appType == AppType.GALLERY || appType == AppType.MULTI_WEB ->
                if (sourceDir != null) createFromSource(ctx, appType, name, iconPath, sourceDir)
                else createFromManifest(ctx, appType, name, iconPath, manifest)
            else -> createFromManifest(ctx, appType, name, iconPath, manifest)
        }
    }

    private suspend fun createFromManifest(
        ctx: ToolContext,
        appType: AppType,
        name: String,
        iconPath: String?,
        manifest: String?
    ): ToolResult {
        if (manifest.isNullOrBlank()) {
            return ToolResult.error(
                "CreateApp: $appType needs `manifest` JSON (e.g. {\"url\":\"https://example.com\"} for WEB)."
            )
        }
        val base = ManifestUtils.fromManifestJson(manifest)
            ?: return ToolResult.error("CreateApp: `manifest` is not valid JSON.")
        val now = System.currentTimeMillis()
        val app = base.copy(
            id = 0,
            appType = appType,
            name = name,
            iconPath = iconPath ?: base.iconPath,
            createdAt = now,
            updatedAt = now
        )
        val id = ctx.appRepository.createWebApp(app)
        return ToolResult.ok("Created $appType app id=$id name=\"$name\".")
    }

    private suspend fun createFromSource(
        ctx: ToolContext,
        appType: AppType,
        name: String,
        iconPath: String?,
        sourceDir: String?
    ): ToolResult {
        if (sourceDir == null) {
            return ToolResult.error(
                "CreateApp: $appType needs `sourceDir` — write the project files into the sandbox first, then pass that folder."
            )
        }
        val fileConfig = appType == AppType.GALLERY || appType == AppType.MULTI_WEB
        val kind = when (appType) {
            AppType.HTML -> DetectedArtifact.Kind.Html
            AppType.FRONTEND -> DetectedArtifact.Kind.FrontendReact
            AppType.NODEJS_APP -> DetectedArtifact.Kind.NodeJs
            AppType.PHP_APP -> DetectedArtifact.Kind.Php
            AppType.PYTHON_APP -> DetectedArtifact.Kind.Python
            AppType.GO_APP -> DetectedArtifact.Kind.Go
            AppType.GALLERY -> DetectedArtifact.Kind.Gallery
            AppType.MULTI_WEB -> DetectedArtifact.Kind.MultiWeb
            else -> return ToolResult.error("CreateApp: $appType cannot be created from a source dir.")
        }
        val entryFile = when (appType) {
            AppType.HTML, AppType.FRONTEND -> "index.html"
            AppType.NODEJS_APP -> "index.js"
            AppType.PHP_APP -> "index.php"
            AppType.GALLERY -> "$sourceDir/gallery.json"
            AppType.MULTI_WEB -> "$sourceDir/multi-web.json"
            else -> ""
        }
        val artifact = DetectedArtifact(
            id = UUID.randomUUID().toString(),
            kind = kind,
            displayName = name,
            rootPath = if (fileConfig) "" else sourceDir,
            entryFile = entryFile,
            fileCount = 0,
            totalSizeBytes = 0
        )
        val useCase = SaveSessionAsAppUseCase(ctx.androidContext, ctx.fileManager, ctx.appRepository)
        return when (val result = useCase.save(ctx.sessionId, artifact, name, iconUri = null)) {
            is SaveSessionAsAppUseCase.Result.Success -> {
                if (iconPath != null) {
                    ctx.appRepository.getWebApp(result.appId)?.let { app ->
                        ctx.appRepository.updateWebApp(app.copy(iconPath = iconPath))
                    }
                }
                ToolResult.ok("Created $appType app id=${result.appId} name=\"${result.name}\".")
            }
            is SaveSessionAsAppUseCase.Result.Failure ->
                ToolResult.error("CreateApp failed: ${result.message}")
        }
    }

    private suspend fun createWordPress(
        ctx: ToolContext,
        name: String,
        iconPath: String?,
        manifest: String?
    ): ToolResult {
        var siteTitle = name
        var adminUser = "admin"
        var adminEmail = ""
        if (!manifest.isNullOrBlank()) {
            runCatching { JsonParser.parseString(manifest).asJsonObject }.getOrNull()?.let { obj ->
                obj.get("siteTitle")?.asString?.takeIf { it.isNotBlank() }?.let { siteTitle = it }
                obj.get("adminUser")?.asString?.takeIf { it.isNotBlank() }?.let { adminUser = it }
                obj.get("adminEmail")?.asString?.let { adminEmail = it }
            }
        }
        val projectId = WordPressManager.createProject(ctx.androidContext, siteTitle, adminUser, adminEmail)
            ?: return ToolResult.error(
                "CreateApp: WordPress dependencies are not ready. Install them in the Linux Environment first."
            )
        val port = PortManager.allocateForPhp(projectId)
        val app = WebApp(
            name = name,
            url = "",
            iconPath = iconPath,
            appType = AppType.WORDPRESS,
            wordpressConfig = WordPressConfig(
                projectId = projectId,
                projectName = name,
                siteTitle = siteTitle,
                adminUser = adminUser,
                adminEmail = adminEmail,
                phpPort = port
            ),
            themeType = DEFAULT_THEME
        )
        val id = ctx.appRepository.createWebApp(app)
        return ToolResult.ok("Created WORDPRESS app id=$id name=\"$name\" (project $projectId).")
    }

    companion object {
        private const val DEFAULT_THEME = "AURORA"
        private val PROJECT_TYPES = setOf(
            AppType.HTML,
            AppType.FRONTEND,
            AppType.NODEJS_APP,
            AppType.PHP_APP,
            AppType.PYTHON_APP,
            AppType.GO_APP
        )
    }
}
