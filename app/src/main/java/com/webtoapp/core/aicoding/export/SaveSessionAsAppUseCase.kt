package com.webtoapp.core.aicoding.export

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.aicoding.files.ProjectFileManager
import com.webtoapp.core.golang.GoRuntime
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.nodejs.NodeRuntime
import com.webtoapp.core.php.PhpAppRuntime
import com.webtoapp.core.port.PortManager
import com.webtoapp.core.python.PythonRuntime
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.GalleryCategory
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import com.webtoapp.data.model.GalleryPlayMode
import com.webtoapp.data.model.GallerySortOrder
import com.webtoapp.data.model.GalleryViewMode
import com.webtoapp.data.model.GoAppConfig
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.MultiWebConfig
import com.webtoapp.data.model.MultiWebSite
import com.webtoapp.data.model.NodeJsBuildMode
import com.webtoapp.data.model.NodeJsConfig
import com.webtoapp.data.model.PhpAppConfig
import com.webtoapp.data.model.PythonAppConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.WebAppRepository
import com.webtoapp.util.HtmlProjectHelper
import com.webtoapp.util.HtmlStorage
import com.webtoapp.util.IconStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class SaveSessionAsAppUseCase(
    private val context: Context,
    private val files: ProjectFileManager,
    private val repository: WebAppRepository
) {

    sealed class Result {
        data class Success(val appId: Long, val name: String) : Result()
        data class Failure(val message: String) : Result()
    }

    suspend fun save(
        sessionId: String,
        artifact: DetectedArtifact,
        name: String,
        iconUri: Uri?
    ): Result = withContext(Dispatchers.IO) {
        if (artifact.kind.target != DetectedArtifact.Kind.Target.App) {
            return@withContext Result.Failure(
                "Wrong use case for ${artifact.kind} — use SaveSessionAsModuleUseCase"
            )
        }
        try {
            val sessionRoot = files.getSessionRoot(sessionId)
            val artifactRoot = if (artifact.rootPath.isEmpty()) sessionRoot
            else File(sessionRoot, artifact.rootPath)
            if (!artifactRoot.exists()) {
                return@withContext Result.Failure("Artifact dir is missing: ${artifact.rootPath}")
            }
            val savedIconPath = iconUri?.let { IconStorage.saveIconFromUri(context, it) }
            val finalName = name.ifBlank { artifact.displayName }
            when (artifact.kind) {
                DetectedArtifact.Kind.Html ->
                    saveAsHtmlLike(artifactRoot, finalName, savedIconPath, AppType.HTML)
                DetectedArtifact.Kind.FrontendReact,
                DetectedArtifact.Kind.FrontendVue ->
                    saveAsHtmlLike(artifactRoot, finalName, savedIconPath, AppType.FRONTEND)
                DetectedArtifact.Kind.NodeJs ->
                    saveAsNodeJs(artifactRoot, artifact.entryFile.lastSegment(), finalName, savedIconPath)
                DetectedArtifact.Kind.Php -> saveAsPhp(artifactRoot, finalName, savedIconPath)
                DetectedArtifact.Kind.Python -> saveAsPython(artifactRoot, finalName, savedIconPath)
                DetectedArtifact.Kind.Go -> saveAsGo(artifactRoot, finalName, savedIconPath)
                DetectedArtifact.Kind.MultiWeb -> saveAsMultiWeb(
                    sessionId, artifact.entryFile, finalName, savedIconPath
                )
                DetectedArtifact.Kind.Gallery -> saveAsGallery(
                    sessionId, artifact.entryFile, finalName, savedIconPath
                )
                else -> Result.Failure("Unhandled kind ${artifact.kind}")
            }
        } catch (t: Throwable) {
            AppLogger.e(TAG, "Save artifact as app failed", t)
            Result.Failure(t.message ?: "unknown error")
        }
    }

    private suspend fun saveAsHtmlLike(
        artifactRoot: File,
        name: String,
        iconPath: String?,
        appType: AppType
    ): Result {
        val projectId = HtmlStorage.generateProjectId()
        val savedFiles = HtmlProjectHelper.copyBuildOutputToStorage(
            context = context,
            outputPath = artifactRoot.absolutePath,
            projectId = projectId
        )
        if (savedFiles.none {
                it.name.endsWith(".html", ignoreCase = true) ||
                    it.name.endsWith(".htm", ignoreCase = true)
            }
        ) {
            HtmlStorage.deleteProject(context, projectId)
            return Result.Failure("No HTML entry file in this artifact")
        }
        val app = WebApp(
            name = name,
            url = "",
            iconPath = iconPath,
            appType = appType,
            htmlConfig = HtmlConfig(
                projectId = projectId,
                entryFile = pickEntryHtml(savedFiles.map { it.name }),
                files = savedFiles
            ),
            themeType = DEFAULT_THEME
        )
        return Result.Success(repository.createWebApp(app), app.name)
    }

    private suspend fun saveAsNodeJs(
        artifactRoot: File,
        entryHint: String,
        name: String,
        iconPath: String?
    ): Result {
        val node = NodeRuntime(context)
        val projectId = newProjectId()
        val projectDir = node.createProject(projectId = projectId, sourceDir = artifactRoot)
        val entry = node.detectEntryFile(projectDir) ?: entryHint.ifBlank { "index.js" }
        val port = PortManager.allocateForNodeJs(projectId)
        val app = WebApp(
            name = name,
            url = "",
            iconPath = iconPath,
            appType = AppType.NODEJS_APP,
            nodejsConfig = NodeJsConfig(
                projectId = projectId,
                projectName = name,
                sourceProjectPath = artifactRoot.absolutePath,
                entryFile = entry,
                serverPort = port,
                buildMode = NodeJsBuildMode.API_BACKEND
            ),
            themeType = DEFAULT_THEME
        )
        return Result.Success(repository.createWebApp(app), app.name)
    }

    private suspend fun saveAsPhp(
        artifactRoot: File,
        name: String,
        iconPath: String?
    ): Result {
        val php = PhpAppRuntime(context)
        val projectId = newProjectId()
        val projectDir = php.createProject(projectId = projectId, sourceDir = artifactRoot)
        val entry = listOf("index.php", "public/index.php")
            .firstOrNull { File(projectDir, it).exists() } ?: "index.php"
        val port = PortManager.allocateForPhp(projectId)
        val app = WebApp(
            name = name,
            url = "",
            iconPath = iconPath,
            appType = AppType.PHP_APP,
            phpAppConfig = PhpAppConfig(
                projectId = projectId,
                projectName = name,
                framework = php.detectFramework(projectDir),
                documentRoot = if (entry.contains("/")) entry.substringBeforeLast("/") else "",
                entryFile = entry.substringAfterLast("/"),
                phpPort = port,
                hasComposerJson = File(projectDir, "composer.json").exists()
            ),
            themeType = DEFAULT_THEME
        )
        return Result.Success(repository.createWebApp(app), app.name)
    }

    private suspend fun saveAsPython(
        artifactRoot: File,
        name: String,
        iconPath: String?
    ): Result {
        val py = PythonRuntime(context)
        val projectId = newProjectId()
        val projectDir = py.createProject(projectId = projectId, sourceDir = artifactRoot)
        val framework = py.detectFramework(projectDir)
        val entry = py.detectEntryFile(projectDir, framework)
        val port = PortManager.allocateForPython(projectId)
        val app = WebApp(
            name = name,
            url = "",
            iconPath = iconPath,
            appType = AppType.PYTHON_APP,
            pythonAppConfig = PythonAppConfig(
                projectId = projectId,
                projectName = name,
                sourceProjectPath = artifactRoot.absolutePath,
                framework = framework,
                entryFile = entry,
                serverType = "builtin",
                serverPort = port,
                hasPipDeps = File(projectDir, "requirements.txt").exists()
            ),
            themeType = DEFAULT_THEME
        )
        return Result.Success(repository.createWebApp(app), app.name)
    }

    private suspend fun saveAsGo(
        artifactRoot: File,
        name: String,
        iconPath: String?
    ): Result {
        val go = GoRuntime(context)
        val projectId = newProjectId()
        go.createProject(projectId = projectId, sourceDir = artifactRoot)
        val port = PortManager.allocateForGo(projectId)
        val app = WebApp(
            name = name,
            url = "",
            iconPath = iconPath,
            appType = AppType.GO_APP,
            goAppConfig = GoAppConfig(
                projectId = projectId,
                projectName = name,
                framework = "raw",
                binaryName = projectId,
                serverPort = port
            ),
            themeType = DEFAULT_THEME
        )
        return Result.Success(repository.createWebApp(app), app.name)
    }

    private suspend fun saveAsMultiWeb(
        sessionId: String,
        relativePath: String,
        name: String,
        iconPath: String?
    ): Result {
        val jsonText = files.readText(sessionId, relativePath)
            ?: return Result.Failure("multi-web.json not readable")
        val cfg = parseMultiWebJson(jsonText)
            ?: return Result.Failure("multi-web.json is not valid")
        val app = WebApp(
            name = name,
            url = "",
            iconPath = iconPath,
            appType = AppType.MULTI_WEB,
            multiWebConfig = cfg,
            themeType = DEFAULT_THEME
        )
        return Result.Success(repository.createWebApp(app), app.name)
    }

    private fun parseMultiWebJson(text: String): MultiWebConfig? = runCatching {
        val obj = JsonParser.parseString(text).asJsonObject
        val sites = obj.getAsJsonArray("sites")?.mapNotNull { siteEl ->
            val s = siteEl as? JsonObject ?: return@mapNotNull null
            MultiWebSite(
                id = s.optString("id", UUID.randomUUID().toString().take(8)),
                name = s.optString("name"),
                url = s.optString("url"),
                themeColor = s.optString("themeColor"),
                cssSelector = s.optString("contentSelector"),
                iconEmoji = s.optString("iconEmoji"),
                faviconUrl = s.optString("iconUrl")
            )
        }.orEmpty()
        MultiWebConfig(
            sites = sites,
            displayMode = obj.optString("layout", "TABS").uppercase(),
            refreshInterval = obj.optInt("refreshIntervalMinutes", 30)
        )
    }.getOrNull()

    private suspend fun saveAsGallery(
        sessionId: String,
        relativePath: String,
        name: String,
        iconPath: String?
    ): Result {
        val jsonText = files.readText(sessionId, relativePath)
            ?: return Result.Failure("gallery.json not readable")
        val cfg = parseGalleryJson(jsonText)
            ?: return Result.Failure("gallery.json is not valid")
        val app = WebApp(
            name = name,
            url = "",
            iconPath = iconPath,
            appType = AppType.GALLERY,
            galleryConfig = cfg,
            themeType = DEFAULT_THEME
        )
        return Result.Success(repository.createWebApp(app), app.name)
    }

    private fun parseGalleryJson(text: String): GalleryConfig? = runCatching {
        val obj = JsonParser.parseString(text).asJsonObject
        val categories = mutableListOf<GalleryCategory>()
        val items = mutableListOf<GalleryItem>()
        obj.getAsJsonArray("categories")?.forEachIndexed { idx, catEl ->
            val cat = catEl as? JsonObject ?: return@forEachIndexed
            val catId = cat.optString("id", UUID.randomUUID().toString().take(8))
            categories += GalleryCategory(
                id = catId,
                name = cat.optString("name"),
                sortIndex = idx
            )
            cat.getAsJsonArray("items")?.forEachIndexed { itemIdx, itemEl ->
                val it = itemEl as? JsonObject ?: return@forEachIndexed
                val type = when (it.optString("type", "image").lowercase()) {
                    "video" -> GalleryItemType.VIDEO
                    else -> GalleryItemType.IMAGE
                }
                items += GalleryItem(
                    path = it.optString("src"),
                    type = type,
                    name = it.optString("title"),
                    categoryId = catId,
                    sortIndex = itemIdx
                )
            }
        }
        GalleryConfig(
            items = items,
            categories = categories,
            defaultView = parseGalleryView(obj.optString("viewMode", "grid")),
            playMode = parseGalleryPlay(obj.optString("playMode", "sequential")),
            sortOrder = parseGallerySort(obj.optString("sortBy", "custom"))
        )
    }.getOrNull()

    private fun parseGalleryView(s: String) = when (s.lowercase()) {
        "list" -> GalleryViewMode.LIST
        "timeline" -> GalleryViewMode.TIMELINE
        else -> GalleryViewMode.GRID
    }

    private fun parseGalleryPlay(s: String) = when (s.lowercase().replace("-", "_")) {
        "shuffle" -> GalleryPlayMode.SHUFFLE
        "single_loop", "loop", "single" -> GalleryPlayMode.SINGLE_LOOP
        else -> GalleryPlayMode.SEQUENTIAL
    }

    private fun parseGallerySort(s: String) = when (s.lowercase()) {
        "name" -> GallerySortOrder.NAME_ASC
        "date" -> GallerySortOrder.DATE_ASC
        "type" -> GallerySortOrder.TYPE
        else -> GallerySortOrder.CUSTOM
    }

    private fun pickEntryHtml(relativePaths: List<String>): String {
        val htmls = relativePaths.filter {
            it.endsWith(".html", ignoreCase = true) ||
                it.endsWith(".htm", ignoreCase = true)
        }
        return htmls.firstOrNull { it.equals("index.html", ignoreCase = true) }
            ?: htmls.firstOrNull()
            ?: "index.html"
    }

    private fun newProjectId(): String = UUID.randomUUID().toString().take(8)

    companion object {
        private const val TAG = "SaveSessionAsApp"
        private const val DEFAULT_THEME = "AURORA"
    }
}

private fun JsonObject.optString(key: String, default: String = ""): String =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString ?: default

private fun JsonObject.optInt(key: String, default: Int = 0): Int =
    get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.let {
        runCatching { it.asInt }.getOrDefault(default)
    } ?: default

private fun String.lastSegment(): String = substringAfterLast('/')
