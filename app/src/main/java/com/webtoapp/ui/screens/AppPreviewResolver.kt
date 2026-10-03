package com.webtoapp.ui.screens

import android.content.Context
import android.net.Uri
import com.webtoapp.core.golang.GoRuntime
import com.webtoapp.core.i18n.PreviewHtmlSupport.htmlLang
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.nodejs.NodeRuntime
import com.webtoapp.core.php.PhpAppRuntime
import com.webtoapp.core.python.PythonRuntime
import com.webtoapp.core.wordpress.WordPressManager
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import java.io.File

internal data class AppPreviewSpec(
    val previewFilePath: String? = null,
    val captureUrl: String? = null,

    val liveServerAppType: AppType? = null,
)

internal fun resolveAppPreviewSpec(context: Context, app: WebApp): AppPreviewSpec {
    return when (app.appType) {
        AppType.WEB -> AppPreviewSpec(captureUrl = app.url.takeIf { it.startsWith("http") })
        AppType.IMAGE -> AppPreviewSpec(previewFilePath = existingFile(app.mediaConfig?.mediaPath)?.absolutePath ?: existingFile(app.url)?.absolutePath)
        AppType.VIDEO -> AppPreviewSpec(previewFilePath = existingFile(app.mediaConfig?.mediaPath)?.absolutePath ?: existingFile(app.url)?.absolutePath)
        AppType.GALLERY -> {
            val firstItem = app.galleryConfig?.getSortedItems()?.firstOrNull()
            val previewFile = existingFile(firstItem?.thumbnailPath) ?: existingFile(firstItem?.path)
            AppPreviewSpec(previewFilePath = previewFile?.absolutePath)
        }
        AppType.HTML,
        AppType.FRONTEND -> resolveHtmlPreviewSpec(context, app)
        AppType.WORDPRESS -> resolveWordPressPreviewSpec(context, app)
        AppType.NODEJS_APP -> resolveNodePreviewSpec(context, app)
        AppType.PHP_APP -> resolvePhpPreviewSpec(context, app)
        AppType.PYTHON_APP -> resolvePythonPreviewSpec(context, app)
        AppType.GO_APP -> resolveGoPreviewSpec(context, app)
        AppType.MULTI_WEB -> resolveMultiWebPreviewSpec(app)
    }
}

internal suspend fun captureAppThumbnail(
    context: Context,
    screenshotService: com.webtoapp.core.stats.WebsiteScreenshotService,
    app: WebApp,
    spec: AppPreviewSpec,
): String? {
    val liveType = spec.liveServerAppType
    if (liveType != null && com.webtoapp.core.stats.LivePreviewServerLauncher.supports(liveType)) {
        var capturedPath: String? = null
        val started = com.webtoapp.core.stats.LivePreviewServerLauncher.withLiveServer(context, app) { liveUrl ->
            capturedPath = screenshotService.captureScreenshot(app.id, liveUrl)
        }
        if (started && capturedPath != null) {
            return capturedPath
        }

    }

    val fallbackUrl = spec.captureUrl
    if (fallbackUrl != null) {
        return screenshotService.captureScreenshot(app.id, fallbackUrl)
    }
    return null
}

private fun resolveHtmlPreviewSpec(context: Context, app: WebApp): AppPreviewSpec {
    val config = app.htmlConfig
    val entryFile = config?.getValidEntryFile() ?: "index.html"
    val storedProjectDir = config?.projectId
        ?.takeIf { it.isNotBlank() }
        ?.let { File(context.filesDir, "html_projects/$it") }
        ?.takeIf { it.exists() && it.isDirectory }
    val importedProjectDir = config?.projectDir
        ?.takeIf { it.isNotBlank() }
        ?.let(::File)
        ?.takeIf { it.exists() && it.isDirectory }
    val rootDir = storedProjectDir ?: importedProjectDir
    if (rootDir == null) {
        com.webtoapp.core.logging.AppLogger.w(
            "AppPreviewResolver",
            "HTML preview has no project dir: appId=${app.id}, projectId='${config?.projectId}', projectDir='${config?.projectDir}'"
        )
        return AppPreviewSpec()
    }

    // 入口选择,与 WebViewActivity 的加载逻辑保持一致:只要项目目录在,就一定
    // 产出一个可截图的 captureUrl,不能因为入口文件名对不上就退回空 spec
    // （那会让缩略图变成 “</>” 占位且没有刷新按钮——但应用其实能正常打开）。
    // 优先级:配置入口文件 → 目录里任意 .html/.htm → 兜底直接用配置入口名
    // （即便文件系统检查没命中,file:// 仍能让 WebView 尝试加载,失败也只是
    //  截到一张空白图,而不是连刷新入口都没有）。
    val entry = File(rootDir, entryFile).takeIf { it.exists() && it.isFile }
        ?: findStaticHtmlEntry(rootDir, listOf(""))
        ?: File(rootDir, entryFile)
    return AppPreviewSpec(captureUrl = entry.toFileUrl())
}

private fun resolveWordPressPreviewSpec(context: Context, app: WebApp): AppPreviewSpec {
    val config = app.wordpressConfig
    val projectId = config?.projectId?.takeIf { it.isNotBlank() }
    val projectDir = projectId?.let { WordPressManager.getProjectDir(context, it) }?.takeIf { it.exists() }
    val themeScreenshot = projectDir?.let { findWordPressThemeScreenshot(context, it, config?.themeName.orEmpty(), projectId) }
    if (themeScreenshot != null) {

        val wrapperHtml = buildScreenshotWrapperHtml(
            screenshotUrl = themeScreenshot.toFileUrl(),
            badge = "WordPress"
        )
        return AppPreviewSpec(
            captureUrl = writePreviewHtml(context, app.id, "wordpress", wrapperHtml),
            liveServerAppType = AppType.WORDPRESS,
        )
    }
    val pluginCount = when {
        projectId != null -> WordPressManager.getInstalledPlugins(context, projectId).size
        else -> config?.plugins?.size ?: 0
    }
    val html = buildProjectInfoHtml(
        title = config?.siteTitle?.ifBlank { app.name } ?: app.name,
        badge = "WordPress",
        details = listOf(
            Strings.previewLabelTheme to (config?.themeName?.ifBlank { Strings.previewValueDefault } ?: Strings.previewValueDefault),
            Strings.previewLabelPlugins to pluginCount.toString(),
            Strings.previewLabelAdmin to (config?.adminUser?.ifBlank { "admin" } ?: "admin")
        )
    )
    return AppPreviewSpec(
        captureUrl = writePreviewHtml(context, app.id, "wordpress", html),
        liveServerAppType = AppType.WORDPRESS,
    )
}

private fun resolveNodePreviewSpec(context: Context, app: WebApp): AppPreviewSpec {
    val runtime = NodeRuntime(context)
    val config = app.nodejsConfig
    val sourceProjectDir = runtime.resolveSourceProjectDir(config?.sourceProjectPath)
    val storedProjectDir = config?.projectId?.takeIf { it.isNotBlank() }?.let(runtime::getProjectDir)?.takeIf { it.exists() }
    val projectDir = sourceProjectDir ?: storedProjectDir
    val staticEntry = projectDir?.let { findStaticHtmlEntry(it, listOf("dist", "build", "public", "static", "www", "")) }
    if (staticEntry != null) {
        return AppPreviewSpec(captureUrl = staticEntry.toFileUrl())
    }
    val html = if (projectDir != null && config != null) {
        runtime.generatePreviewHtml(projectDir, config.framework, config.entryFile)
    } else {
        buildProjectInfoHtml(
            title = config?.projectName?.ifBlank { app.name } ?: app.name,
            badge = "Node.js",
            details = listOf(
                Strings.previewLabelFramework to (config?.framework?.ifBlank { Strings.previewValueUnknown } ?: Strings.previewValueUnknown),
                Strings.previewLabelEntry to (config?.entryFile?.ifBlank { "index.js" } ?: "index.js")
            )
        )
    }
    return AppPreviewSpec(
        captureUrl = writePreviewHtml(context, app.id, "nodejs", html),
        liveServerAppType = AppType.NODEJS_APP,
    )
}

private fun resolvePhpPreviewSpec(context: Context, app: WebApp): AppPreviewSpec {
    val runtime = PhpAppRuntime(context)
    val config = app.phpAppConfig
    val projectDir = config?.projectId?.takeIf { it.isNotBlank() }?.let(runtime::getProjectDir)?.takeIf { it.exists() }
    val staticEntry = projectDir?.let { findStaticHtmlEntry(it, listOf("public", "dist", "build", "static", "www", "")) }
    if (staticEntry != null) {
        return AppPreviewSpec(captureUrl = staticEntry.toFileUrl())
    }
    val framework = if (projectDir != null) runtime.detectFramework(projectDir) else config?.framework.orEmpty()
    val documentRoot = if (projectDir != null) runtime.detectDocumentRoot(projectDir, framework) else config?.documentRoot.orEmpty()
    val entryFile = if (projectDir != null) runtime.detectEntryFile(projectDir, documentRoot) else (config?.entryFile ?: "index.php")
    val entryFilePath = projectDir?.let {
        val rootDir = if (documentRoot.isNotBlank()) File(it, documentRoot) else it
        File(rootDir, entryFile)
    }?.takeIf { it.exists() && it.isFile }
    val sourceSnippet = entryFilePath?.runCatching { readText().take(4000) }?.getOrNull()
    val html = buildProjectInfoHtml(
        title = config?.projectName?.ifBlank { app.name } ?: app.name,
        badge = "PHP",
        details = listOf(
            Strings.previewLabelFramework to framework.ifBlank { "raw" },
            Strings.previewLabelDocumentRoot to documentRoot.ifBlank { "/" },
            Strings.previewLabelEntry to entryFile
        ),
        codeSnippet = sourceSnippet
    )
    return AppPreviewSpec(
        captureUrl = writePreviewHtml(context, app.id, "php", html),
        liveServerAppType = AppType.PHP_APP,
    )
}

private fun resolvePythonPreviewSpec(context: Context, app: WebApp): AppPreviewSpec {
    val runtime = PythonRuntime(context)
    val config = app.pythonAppConfig
    var projectDir = config?.projectId?.takeIf { it.isNotBlank() }?.let(runtime::getProjectDir)?.takeIf { it.exists() }

    var entryFile = config?.entryFile ?: "app.py"
    var framework = config?.framework ?: "raw"
    if (projectDir != null && !File(projectDir, entryFile).exists()) {

        val detectedFramework = runtime.detectFramework(projectDir)
        val detectedEntry = runtime.detectEntryFile(projectDir, detectedFramework)
        if (File(projectDir, detectedEntry).exists()) {
            framework = detectedFramework
            entryFile = detectedEntry
        } else {

            val pySubDir = projectDir.listFiles()
                ?.filter { it.isDirectory && it.name != "__MACOSX" && it.name != "__pycache__" && !it.name.startsWith("._") && it.name != "venv" && it.name != ".venv" && it.name != ".git" }
                ?.firstOrNull { sub -> sub.listFiles()?.any { it.isFile && it.extension == "py" } == true }
            if (pySubDir != null) {
                projectDir = pySubDir
                framework = runtime.detectFramework(pySubDir)
                entryFile = runtime.detectEntryFile(pySubDir, framework)
            }
        }
    }

    val staticEntry = projectDir?.let { findStaticHtmlEntry(it, listOf("dist", "build", "public", "static", "www", "templates", "")) }
    if (staticEntry != null) {
        return AppPreviewSpec(captureUrl = staticEntry.toFileUrl())
    }
    val html = if (projectDir != null && config != null) {
        runtime.generatePreviewHtml(projectDir, framework, entryFile)
    } else {
        buildProjectInfoHtml(
            title = config?.projectName?.ifBlank { app.name } ?: app.name,
            badge = "Python",
            details = listOf(
                Strings.previewLabelFramework to (config?.framework?.ifBlank { "raw" } ?: "raw"),
                Strings.previewLabelEntry to (config?.entryFile?.ifBlank { "app.py" } ?: "app.py")
            )
        )
    }
    return AppPreviewSpec(
        captureUrl = writePreviewHtml(context, app.id, "python", html),
        liveServerAppType = AppType.PYTHON_APP,
    )
}

private fun resolveGoPreviewSpec(context: Context, app: WebApp): AppPreviewSpec {
    val runtime = GoRuntime(context)
    val config = app.goAppConfig
    val projectDir = config?.projectId?.takeIf { it.isNotBlank() }?.let(runtime::getProjectDir)?.takeIf { it.exists() }
    val staticEntry = projectDir?.let { findStaticHtmlEntry(it, listOf("dist", "build", "public", "static", "web", "www", "")) }
    if (staticEntry != null) {
        return AppPreviewSpec(captureUrl = staticEntry.toFileUrl())
    }
    val html = if (projectDir != null && config != null) {
        runtime.generatePreviewHtml(projectDir, config.framework, config.binaryName)
    } else {
        buildProjectInfoHtml(
            title = config?.projectName?.ifBlank { app.name } ?: app.name,
            badge = "Go",
            details = listOf(
                Strings.previewLabelFramework to (config?.framework?.ifBlank { "raw" } ?: "raw"),
                Strings.previewLabelBinary to (config?.binaryName?.ifBlank { "main" } ?: "main")
            )
        )
    }
    return AppPreviewSpec(
        captureUrl = writePreviewHtml(context, app.id, "go", html),
        liveServerAppType = AppType.GO_APP,
    )
}

private fun resolveLocalEntryUrl(projectDir: String?, entryFile: String): String? {
    val rootDir = projectDir?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.exists() && it.isDirectory } ?: return null
    val entry = File(rootDir, entryFile)
    if (entry.exists() && entry.isFile) {
        return entry.toFileUrl()
    }
    return findStaticHtmlEntry(rootDir, listOf(""))?.toFileUrl()
}

private fun findStaticHtmlEntry(projectDir: File, preferredDirs: List<String>): File? {
    preferredDirs.forEach { relativeDir ->
        val candidateDir = if (relativeDir.isBlank()) projectDir else File(projectDir, relativeDir)
        if (!candidateDir.exists() || !candidateDir.isDirectory) {
            return@forEach
        }
        val indexFile = File(candidateDir, "index.html")
        if (indexFile.exists() && indexFile.isFile) {
            return indexFile
        }
        val firstHtml = candidateDir.walkTopDown().firstOrNull {
            it.isFile &&
                it.name != "_preview_.html" &&
                (it.extension.equals("html", ignoreCase = true) || it.extension.equals("htm", ignoreCase = true))
        }
        if (firstHtml != null) {
            return firstHtml
        }
    }
    return null
}

private fun findWordPressThemeScreenshot(
    context: Context,
    projectDir: File,
    themeName: String,
    projectId: String?
): File? {
    val themeNames = buildList {
        if (themeName.isNotBlank()) {
            add(themeName)
        }
        if (projectId != null) {
            addAll(WordPressManager.getInstalledThemes(context, projectId))
        }
    }.distinct()
    themeNames.forEach { name ->
        val themeDir = File(projectDir, "wp-content/themes/$name")
        val screenshot = listOf("png", "jpg", "jpeg", "webp")
            .map { ext -> File(themeDir, "screenshot.$ext") }
            .firstOrNull { it.exists() && it.isFile }
        if (screenshot != null) {
            return screenshot
        }
    }
    return null
}

private fun writePreviewHtml(context: Context, appId: Long, suffix: String, html: String): String? {
    return runCatching {
        val previewDir = File(context.cacheDir, "app_card_previews").also { it.mkdirs() }
        val previewFile = File(previewDir, "app_${appId}_$suffix.html")
        previewFile.writeText(html)
        previewFile.toFileUrl()
    }.getOrNull()
}

private fun existingFile(path: String?): File? {
    if (path.isNullOrBlank()) return null
    val file = File(path)
    return file.takeIf { it.exists() && it.isFile }
}

private fun File.toFileUrl(): String = Uri.fromFile(this).toString()

private fun buildScreenshotWrapperHtml(screenshotUrl: String, badge: String): String {
    val lang = htmlLang()
    return """
        <!DOCTYPE html>
        <html lang="$lang">
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1" />
            <style>
                html, body { margin: 0; height: 100%; background: #11121a; }
                .wrap { position: relative; width: 100%; min-height: 100vh; }
                img { display: block; width: 100%; height: auto; }
                .tag {
                    position: fixed;
                    top: 14px;
                    left: 14px;
                    padding: 6px 12px;
                    border-radius: 999px;
                    background: rgba(17, 18, 26, 0.78);
                    color: #fff;
                    font: 600 22px -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                    letter-spacing: 0.03em;
                }
            </style>
        </head>
        <body>
            <div class="wrap">
                <img src="${escapeHtml(screenshotUrl)}" alt="${escapeHtml(badge)}" />
                <div class="tag">${escapeHtml(badge)} · ${escapeHtml(Strings.previewNotRunningBadge)}</div>
            </div>
        </body>
        </html>
    """.trimIndent()
}

private fun buildProjectInfoHtml(
    title: String,
    badge: String,
    details: List<Pair<String, String>>,
    codeSnippet: String? = null
): String {
    val detailRows = details
        .filter { it.second.isNotBlank() }
        .joinToString(separator = "") { (label, value) ->
            "<div class=\"row\"><span class=\"label\">${escapeHtml(label)}</span><span class=\"value\">${escapeHtml(value)}</span></div>"
        }
    val codeBlock = codeSnippet
        ?.takeIf { it.isNotBlank() }
        ?.let { "<pre>${escapeHtml(it)}</pre>" }
        .orEmpty()
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1" />
            <style>
                :root { color-scheme: light; }
                body {
                    margin: 0;
                    min-height: 100vh;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                    background: linear-gradient(180deg, #f6f2ff 0%, #efe8ff 100%);
                    color: #241047;
                    display: flex;
                    align-items: stretch;
                    justify-content: center;
                }
                .shell {
                    width: 100%;
                    box-sizing: border-box;
                    padding: 36px 28px;
                }
                .badge {
                    display: inline-flex;
                    align-items: center;
                    padding: 8px 14px;
                    border-radius: 999px;
                    background: rgba(111, 76, 255, 0.12);
                    color: #5f3dc4;
                    font-size: 22px;
                    font-weight: 700;
                    letter-spacing: 0.04em;
                }
                .note {
                    margin-top: 14px;
                    padding: 14px 18px;
                    border-radius: 18px;
                    background: rgba(95, 61, 196, 0.08);
                    color: rgba(36, 16, 71, 0.72);
                    font-size: 20px;
                    line-height: 1.4;
                    font-weight: 600;
                }
                h1 {
                    margin: 22px 0 10px;
                    font-size: 44px;
                    line-height: 1.15;
                }
                .panel {
                    margin-top: 24px;
                    padding: 24px;
                    border-radius: 28px;
                    background: rgba(255, 255, 255, 0.84);
                    box-shadow: 0 18px 48px rgba(95, 61, 196, 0.12);
                    backdrop-filter: blur(16px);
                }
                .row {
                    display: flex;
                    justify-content: space-between;
                    gap: 16px;
                    padding: 14px 0;
                    border-bottom: 1px solid rgba(95, 61, 196, 0.1);
                    font-size: 22px;
                }
                .row:last-child {
                    border-bottom: none;
                    padding-bottom: 0;
                }
                .label {
                    color: rgba(36, 16, 71, 0.64);
                    font-weight: 600;
                }
                .value {
                    text-align: right;
                    font-weight: 700;
                }
                pre {
                    margin: 22px 0 0;
                    padding: 18px;
                    border-radius: 20px;
                    background: #1f1533;
                    color: #f8f7ff;
                    font-size: 15px;
                    line-height: 1.5;
                    white-space: pre-wrap;
                    word-break: break-word;
                    overflow: hidden;
                }
            </style>
        </head>
        <body>
            <div class="shell">
                <div class="badge">${escapeHtml(badge)}</div>
                <h1>${escapeHtml(title)}</h1>
                <div class="note">${escapeHtml(Strings.previewNotRunningNote)}</div>
                <div class="panel">$detailRows$codeBlock</div>
            </div>
        </body>
        </html>
    """.trimIndent()
}

private fun escapeHtml(value: String): String {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}

private fun resolveMultiWebPreviewSpec(app: WebApp): AppPreviewSpec {

    val firstSite = app.multiWebConfig?.sites?.firstOrNull { it.enabled && it.url.isNotBlank() }
    val previewUrl = firstSite?.url?.takeIf { it.startsWith("http") }
    return AppPreviewSpec(captureUrl = previewUrl)
}
