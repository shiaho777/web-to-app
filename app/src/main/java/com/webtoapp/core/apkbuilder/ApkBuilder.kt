package com.webtoapp.core.apkbuilder

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.WebToAppApplication
import androidx.core.content.FileProvider
import com.webtoapp.core.crypto.AssetEncryptor
import com.webtoapp.core.crypto.EncryptedApkBuilder
import com.webtoapp.core.crypto.EncryptionConfig
import com.webtoapp.core.crypto.KeyManager
import com.webtoapp.core.crypto.toHexString
import com.webtoapp.core.shell.BgmShellItem
import com.webtoapp.core.shell.LrcShellTheme
import com.webtoapp.data.model.ApkRuntimePermissions
import com.webtoapp.data.model.LrcData
import com.webtoapp.data.model.AnnouncementTemplateType
import com.webtoapp.data.model.HtmlLoadMode
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.getActivationCodeStrings
import com.webtoapp.ui.components.announcement.toUiTemplate
import com.webtoapp.ui.shell.buildPackagedHtmlShellEntryUrl
import com.webtoapp.ui.theme.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.*
import javax.crypto.SecretKey
import com.webtoapp.util.AppConstants
import com.webtoapp.util.TextFileClassifier

private fun resolveOutputDir(context: Context): File {
    val external = context.getExternalFilesDir(null)
    if (external != null) {
        val dir = File(external, "built_apks")
        if (dir.exists() || dir.mkdirs()) return dir
        AppLogger.w("ApkBuilder", "External built_apks dir unavailable, falling back to filesDir")
    }
    return File(context.filesDir, "built_apks").apply { mkdirs() }
}

class ApkBuilder(private val context: Context) {

    companion object {
        private val SANITIZE_FILENAME_REGEX = AppConstants.SANITIZE_FILENAME_REGEX
        private val PACKAGE_NAME_REGEX = AppConstants.PACKAGE_NAME_REGEX
        private val CHARSET_REGEX = AppConstants.CHARSET_REGEX
        private const val FLOATING_WINDOW_MINIMIZED_ICON_ASSET = "floating_window_minimized_icon.png"
        private val GECKOVIEW_RUNTIME_COMPONENTS = (0..39)
            .map { "org.mozilla.gecko.process.GeckoChildProcessServices\$tab$it" }
            .toSet() + setOf(
            "org.mozilla.gecko.media.MediaManager",
            "org.mozilla.gecko.process.GeckoChildProcessServices\$gmplugin",
            "org.mozilla.gecko.process.GeckoChildProcessServices\$socket",
            "org.mozilla.gecko.process.GeckoChildProcessServices\$gpu",
            "org.mozilla.gecko.process.GeckoChildProcessServices\$utility",
            "org.mozilla.gecko.process.GeckoChildProcessServices\$ipdlunittest"
        )
    }

    private val template = ApkTemplate(context)
    private val templateProvider = CompositeTemplateProvider.default(context)
    private val signer = JarSigner(context)
    private val axmlEditor = AxmlEditor()
    private val axmlRebuilder = AxmlRebuilder()
    private val arscEditor = ArscEditor()
    private val arscRebuilder = ArscRebuilder()
    private val logger = BuildLogger(context)
    private val encryptedApkBuilder = EncryptedApkBuilder(context)
    private val keyManager = KeyManager.getInstance(context)
    private val buildCache = ApkBuildCache(context)

    private val outputDir = resolveOutputDir(context)
    private val tempDir = File(context.cacheDir, "apk_build_temp").apply { mkdirs() }

    private val originalAppName = "WebToApp"
    private val originalPackageName = "com.webtoapp"

    fun clearIncrementalCache(webApp: WebApp, packageName: String) {
        buildCache.clear(webApp, packageName)
    }

    fun clearIncrementalCache(webApp: WebApp) {
        val customPkg = webApp.apkExportConfig?.customPackageName?.takeIf {
            it.isNotBlank() && it.matches(PACKAGE_NAME_REGEX)
        }
        val packageName = customPkg ?: generatePackageName(webApp.name)
        buildCache.clear(webApp, packageName)
    }

    fun clearAllIncrementalCaches() {
        buildCache.clearAll()
    }

    fun cleanTempFiles() {
        try {
            tempDir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
            }
            AppLogger.d("ApkBuilder", "Temp files cleaned")
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to clean temp files", e)
        }
    }

    fun getTempDirSize(): Long {
        return tempDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun cleanOldBuilds(keepCount: Int = 5) {
        try {
            val apkFiles = outputDir.listFiles { file -> file.extension == "apk" }
                ?.sortedByDescending { it.lastModified() }
                ?: return

            if (apkFiles.size > keepCount) {
                apkFiles.drop(keepCount).forEach { file ->
                    file.delete()
                    AppLogger.d("ApkBuilder", "Deleted old build: ${file.name}")
                }
            }
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to clean old builds", e)
        }
    }

    suspend fun buildApk(
        webApp: WebApp,
        forceFullRebuild: Boolean = false,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): BuildResult = withContext(Dispatchers.IO) {
        var currentStage = BuildStage.PREPARE
        var currentPackageName: String? = null
        var currentUnsignedApkPath: String? = null
        var currentSignedApkPath: String? = null

        logger.startNewLog(webApp.name)

        try {
            onProgress(0, "Preparing build...")

            val encryptionConfig = webApp.apkExportConfig?.encryptionConfig?.toEncryptionConfig()
                ?: EncryptionConfig.DISABLED

            val encryptionExportConfig = webApp.apkExportConfig?.encryptionConfig
                ?: com.webtoapp.data.model.ApkEncryptionConfig()

            val perfOptEnabled = webApp.apkExportConfig?.performanceOptimization == true
            val perfConfig = if (perfOptEnabled) {
                webApp.apkExportConfig?.performanceConfig?.toOptimizerConfig()
                    ?: com.webtoapp.core.linux.PerformanceOptimizer.OptimizeConfig()
            } else null

            logger.section("WebApp Config")
            logger.logKeyValue("appName", webApp.name)
            logger.logKeyValue("appType", webApp.appType)
            logger.logKeyValue("url", webApp.url)
            logger.logKeyValue("iconPath", webApp.iconPath)
            logger.logKeyValue("splashEnabled", webApp.splashEnabled)
            logger.logKeyValue("bgmEnabled", webApp.bgmEnabled)
            logger.logKeyValue("activationEnabled", webApp.activationEnabled)
            logger.logKeyValue("adBlockEnabled", webApp.adBlockEnabled)
            logger.logKeyValue("translateEnabled", webApp.translateEnabled)
            logger.logKeyValue("encryptionEnabled", encryptionConfig.enabled)
            logger.logKeyValue("runtimeProtectionResponse", encryptionExportConfig.threatResponse.name)

            logger.logKeyValue("performanceOptimization", perfOptEnabled)

            logger.section("APK Export Config")
            logger.logKeyValue("customPackageName", webApp.apkExportConfig?.customPackageName)
            logger.logKeyValue("customVersionCode", webApp.apkExportConfig?.customVersionCode)
            logger.logKeyValue("customVersionName", webApp.apkExportConfig?.customVersionName)

            val architecture = webApp.apkExportConfig?.architecture
                ?: com.webtoapp.data.model.ApkArchitecture.ARM64
            logger.logKeyValue("architecture", architecture.name)
            logger.logKeyValue("abiFilters", architecture.abiFilters.joinToString(", "))

            logger.section("WebView Config")
            logger.logKeyValue("hideToolbar", webApp.webViewConfig.hideToolbar)
            logger.logKeyValue("javaScriptEnabled", webApp.webViewConfig.javaScriptEnabled)
            logger.logKeyValue("desktopMode", webApp.webViewConfig.desktopMode)
            logger.logKeyValue("landscapeMode", webApp.webViewConfig.landscapeMode)
            logger.logKeyValue("userAgentMode", webApp.webViewConfig.userAgentMode.name)
            logger.logKeyValue("customUserAgent", webApp.webViewConfig.customUserAgent)
            logger.logKeyValue("userAgent(legacy)", webApp.webViewConfig.userAgent)

            logger.section("Media Config")
            logger.logKeyValue("mediaConfig", webApp.mediaConfig)
            logger.logKeyValue("mediaConfig.mediaPath", webApp.mediaConfig?.mediaPath)

            if (webApp.appType == com.webtoapp.data.model.AppType.HTML) {
                logger.section("HTML Config")
                logger.logKeyValue("htmlConfig.projectId", webApp.htmlConfig?.projectId)
                logger.logKeyValue("htmlConfig.entryFile", webApp.htmlConfig?.entryFile)
                logger.logKeyValue("htmlConfig.files.size", webApp.htmlConfig?.files?.size ?: 0)
                webApp.htmlConfig?.files?.forEachIndexed { index, file ->
                    val exists = File(file.path).exists()
                    logger.log("  file[$index]: name=${file.name}, path=${file.path}, exists=$exists")
                }
            }

            logger.section("Splash Screen Config")
            logger.logKeyValue("splashEnabled", webApp.splashEnabled)
            logger.logKeyValue("splashConfig.type", webApp.splashConfig?.type)
            logger.logKeyValue("splashConfig.mediaPath", webApp.splashConfig?.mediaPath)
            logger.logKeyValue("splashMediaPath (getSplashMediaPath)", webApp.getSplashMediaPath())

            logger.section("BGM Config")
            logger.logKeyValue("bgmEnabled", webApp.bgmEnabled)
            logger.logKeyValue("bgmConfig.playlist.size", webApp.bgmConfig?.playlist?.size ?: 0)

            AppLogger.d("ApkBuilder", "Build started - WebApp config:")
            AppLogger.d("ApkBuilder", "  appName=${webApp.name}")
            AppLogger.d("ApkBuilder", "  appType=${webApp.appType}")

            logger.section("Generate Package Name")
            val customPkg = webApp.apkExportConfig?.customPackageName?.takeIf {
                it.isNotBlank() &&
                it.matches(PACKAGE_NAME_REGEX)
            }
            val packageName = customPkg ?: generatePackageName(webApp.name)
            currentPackageName = packageName

            if (webApp.apkExportConfig?.customPackageName?.isNotBlank() == true && customPkg == null) {
                logger.warn("Custom package name format invalid, using auto-generated: $packageName")
            }
            logger.logKeyValue("finalPackageName", packageName)

            val config = webApp.toApkConfigWithModules(packageName, context)
            val capabilityPlan = CapabilityPlanner.plan(
                config = config,
                abiFilters = architecture.abiFilters
            )
            logger.section("Capability Plan")
            logger.logKeyValue("liteOnly", capabilityPlan.liteOnly)
            logger.logKeyValue("features", capabilityPlan.features.joinToString(", ").ifBlank { "(none)" })
            logger.logKeyValue("reasons", capabilityPlan.reasons.joinToString(" | ").ifBlank { "(lite)" })
            val featurePackMerge = FeaturePackMerger.prepare(context, capabilityPlan, logger)
            if (featurePackMerge.missingFeatures.isNotEmpty()) {
                val missing = featurePackMerge.missingFeatures.joinToString()
                logger.warn("Missing feature packs: $missing")
                throw IllegalStateException(
                    "Missing feature packs required by capability plan: $missing"
                )
            }
            logger.logKeyValue("versionCode", config.versionCode)
            logger.logKeyValue("versionName", config.versionName)
            logger.logKeyValue("embeddedExtensionModules.size", config.embeddedExtensionModules.size)

            config.embeddedExtensionModules.forEachIndexed { index, module ->
                logger.log("  embeddedModule[$index]: id=${module.id}, name=${module.name}, enabled=${module.enabled}, runAt=${module.runAt}, codeLength=${module.code.length}")
            }

            onProgress(10, "Checking template...")
            logger.section("Parallel Resource Preparation")

            val unsignedApk = File(tempDir, "${packageName}_unsigned.apk")
            val signedApk = File(outputDir, "${sanitizeFileName(webApp.name)}_v${config.versionName}.APK")
            currentUnsignedApkPath = unsignedApk.absolutePath
            currentSignedApkPath = signedApk.absolutePath
            logger.logKeyValue("unsignedApkPath", unsignedApk.absolutePath)
            logger.logKeyValue("signedApkPath", signedApk.absolutePath)

            unsignedApk.delete()
            signedApk.delete()

            val prepStartTime = System.currentTimeMillis()
            currentStage = BuildStage.RESOURCE_PREP

            data class PreparedResources(
                val templateApk: File?,
                val mediaContentPath: String?,
                val htmlFiles: List<com.webtoapp.data.model.HtmlFile>,
                val bgmPlaylistPaths: List<String>,
                val bgmLrcDataList: List<LrcData?>,
                val bgmCoverPaths: List<String?>,
                val galleryItems: List<com.webtoapp.data.model.GalleryItem>,
                val wordPressProjectDir: File?,
                val nodejsProjectDir: File?,
                val phpAppProjectDir: File?,
                val pythonAppProjectDir: File?,
                val goAppProjectDir: File?,
                val frontendProjectDir: File?,
                val htmlProjectDir: File?,
                val encryptionKey: SecretKey?
            )

            val prepared = coroutineScope {

                val templateDeferred = async {
                    getOrCreateTemplate(config)
                }

                val encKeyDeferred = async {
                    if (encryptionConfig.enabled) {
                        val signatureHash = signer.getCertificateSignatureHash()
                        keyManager.generateKeyForPackage(
                            packageName, signatureHash,
                            encryptionConfig.customPassword
                        )
                    } else null
                }

                val wpDirDeferred = async {
                    if (webApp.appType == com.webtoapp.data.model.AppType.WORDPRESS) {
                        val projectId = webApp.wordpressConfig?.projectId ?: ""
                        if (projectId.isNotEmpty()) com.webtoapp.core.wordpress.WordPressManager.getProjectDir(context, projectId) else null
                    } else null
                }
                val nodeDirDeferred = async {
                    if (webApp.appType == com.webtoapp.data.model.AppType.NODEJS_APP) {
                        val config = webApp.nodejsConfig
                        val projectId = config?.projectId ?: ""
                        if (projectId.isNotEmpty()) {
                            val runtime = com.webtoapp.core.nodejs.NodeRuntime(context)
                            val internalProjectPath = runtime.getProjectDir(projectId).absolutePath
                            config?.sourceProjectPath
                                ?.takeIf { it.isNotBlank() }
                                ?.let(runtime::resolveSourceProjectDir)
                                ?.takeIf { it.absolutePath != internalProjectPath }
                                ?.let { sourceDir ->
                                    try {
                                        runtime.syncProjectFromSource(projectId, sourceDir)
                                    } catch (e: Exception) {
                                        com.webtoapp.core.logging.AppLogger.w("ApkBuilder", "同步 Node 源项目失败: ${sourceDir.absolutePath}", e)
                                    }
                                }
                            runtime.getProjectDir(projectId)
                        } else null
                    } else null
                }
                val phpDirDeferred = async {
                    if (webApp.appType == com.webtoapp.data.model.AppType.PHP_APP) {
                        val projectId = webApp.phpAppConfig?.projectId ?: ""
                        if (projectId.isNotEmpty()) com.webtoapp.core.php.PhpAppRuntime(context).getProjectDir(projectId) else null
                    } else null
                }
                val pythonDirDeferred = async {
                    if (webApp.appType == com.webtoapp.data.model.AppType.PYTHON_APP) {
                        val projectId = webApp.pythonAppConfig?.projectId ?: ""
                        if (projectId.isNotEmpty()) File(context.filesDir, "python_projects/$projectId") else null
                    } else null
                }
                val goDirDeferred = async {
                    if (webApp.appType == com.webtoapp.data.model.AppType.GO_APP) {
                        val projectId = webApp.goAppConfig?.projectId ?: ""
                        if (projectId.isNotEmpty()) File(context.filesDir, "go_projects/$projectId") else null
                    } else null
                }

                val mediaContentPath = if (webApp.appType == com.webtoapp.data.model.AppType.IMAGE ||
                                           webApp.appType == com.webtoapp.data.model.AppType.VIDEO) webApp.url else null
                val htmlFiles = if (webApp.appType == com.webtoapp.data.model.AppType.HTML ||
                    webApp.appType == com.webtoapp.data.model.AppType.FRONTEND
                ) webApp.htmlConfig?.files ?: emptyList() else emptyList()
                val bgmPlaylistPaths = if (webApp.bgmEnabled) webApp.bgmConfig?.playlist?.map { it.path } ?: emptyList() else emptyList()
                val bgmLrcDataList = if (webApp.bgmEnabled) webApp.bgmConfig?.playlist?.map { it.lrcData } ?: emptyList() else emptyList()
                val bgmCoverPaths = if (webApp.bgmEnabled) webApp.bgmConfig?.playlist?.map { it.coverPath } ?: emptyList() else emptyList()
                val galleryItems = if (webApp.appType == com.webtoapp.data.model.AppType.GALLERY) webApp.galleryConfig?.items ?: emptyList() else emptyList()
                val htmlProjectId = webApp.htmlConfig?.projectId?.takeIf { it.isNotBlank() }
                val htmlProjectDir = if (webApp.appType == com.webtoapp.data.model.AppType.HTML) {
                    htmlProjectId?.let { File(context.filesDir, "html_projects/$it") }
                } else null
                val frontendProjectDir = if (webApp.appType == com.webtoapp.data.model.AppType.FRONTEND) {
                    htmlProjectId?.let { File(context.filesDir, "html_projects/$it") }
                } else null

                PreparedResources(
                    templateApk = templateDeferred.await(),
                    mediaContentPath = mediaContentPath,
                    htmlFiles = htmlFiles,
                    bgmPlaylistPaths = bgmPlaylistPaths,
                    bgmLrcDataList = bgmLrcDataList,
                    bgmCoverPaths = bgmCoverPaths,
                    galleryItems = galleryItems,
                    wordPressProjectDir = wpDirDeferred.await(),
                    nodejsProjectDir = nodeDirDeferred.await(),
                    phpAppProjectDir = phpDirDeferred.await(),
                    pythonAppProjectDir = pythonDirDeferred.await(),
                    goAppProjectDir = goDirDeferred.await(),
                    frontendProjectDir = frontendProjectDir,
                    htmlProjectDir = htmlProjectDir,
                    encryptionKey = encKeyDeferred.await()
                )
            }

            val prepElapsed = System.currentTimeMillis() - prepStartTime
            logger.log("Parallel resource preparation completed in ${prepElapsed}ms")

            val templateApk = prepared.templateApk
            if (templateApk == null) {
                return@withContext failBuild(
                    stage = BuildStage.TEMPLATE,
                    cause = BuildFailureCause.TEMPLATE_UNAVAILABLE,
                    message = "Failed to get template APK",
                    details = mapOf(
                        "appName" to webApp.name,
                        "appType" to webApp.appType,
                        "packageName" to packageName,
                        "engineType" to config.engineType
                    )
                )
            }
            logger.logKeyValue("templatePath", templateApk.absolutePath)
            logger.logKeyValue("templateSize", "${templateApk.length() / 1024} KB")

            val mediaContentPath = prepared.mediaContentPath
            val htmlFiles = prepared.htmlFiles
            val bgmPlaylistPaths = prepared.bgmPlaylistPaths
            val bgmLrcDataList = prepared.bgmLrcDataList
            val bgmCoverPaths = prepared.bgmCoverPaths
            val galleryItems = prepared.galleryItems
            val wordPressProjectDir = prepared.wordPressProjectDir
            val nodejsProjectDir = prepared.nodejsProjectDir
            val phpAppProjectDir = prepared.phpAppProjectDir
            val pythonAppProjectDir = prepared.pythonAppProjectDir
            val goAppProjectDir = prepared.goAppProjectDir
            val frontendProjectDir = prepared.frontendProjectDir
            val htmlProjectDir = prepared.htmlProjectDir
            val encryptionKey = prepared.encryptionKey

            logger.section("Prepared Resources")
            logger.logKeyValue("mediaContentPath", mediaContentPath)
            if (mediaContentPath != null) {
                val mediaFile = File(mediaContentPath)
                logger.logKeyValue("mediaFile.exists", mediaFile.exists())
                logger.logKeyValue("mediaFile.size", if (mediaFile.exists()) "${mediaFile.length() / 1024} KB" else "N/A")
            }
            logger.logKeyValue("htmlFiles.size", htmlFiles.size)
            htmlFiles.forEachIndexed { index, file ->
                val exists = File(file.path).exists()
                logger.log("  html[$index]: name=${file.name}, path=${file.path}, exists=$exists")
            }
            logger.logKeyValue("bgmPlaylistPaths.size", bgmPlaylistPaths.size)
            logger.logKeyValue("galleryItems.size", galleryItems.size)
            logger.logKeyValue("wordPressProjectDir", wordPressProjectDir?.absolutePath)
            logger.logKeyValue("wordPressProjectDir.exists", wordPressProjectDir?.exists())
            logger.logKeyValue("nodejsProjectDir", nodejsProjectDir?.absolutePath)
            logger.logKeyValue("nodejsProjectDir.exists", nodejsProjectDir?.exists())
            logger.logKeyValue("phpAppProjectDir", phpAppProjectDir?.absolutePath)
            logger.logKeyValue("pythonAppProjectDir", pythonAppProjectDir?.absolutePath)
            logger.logKeyValue("goAppProjectDir", goAppProjectDir?.absolutePath)
            logger.logKeyValue("frontendProjectDir", frontendProjectDir?.absolutePath)
            logger.logKeyValue("frontendProjectDir.exists", frontendProjectDir?.exists())
            logger.logKeyValue("htmlProjectDir", htmlProjectDir?.absolutePath)
            logger.logKeyValue("htmlProjectDir.exists", htmlProjectDir?.exists())
            if (encryptionConfig.enabled) {
                logger.section("Encryption Key")
                logger.log("Encryption key generated (using target signature)")
            }

            onProgress(20, "Preparing resources...")

            logger.section("Build Input Preflight")
            currentStage = BuildStage.INPUT_PRECHECK

            val appTypeEnum = runCatching { com.webtoapp.data.model.AppType.valueOf(config.appType) }.getOrNull()
            if (appTypeEnum != null) {
                onProgress(18, "Ensuring runtime dependencies...")
                val ensured = ExportRuntimeEnsure.ensure(context, appTypeEnum)
                logger.logKeyValue("exportRuntimeEnsure", ensured)
                if (!ensured) {
                    logger.warn("Export runtime ensure failed for ${config.appType}")
                }
            }

            val phpBinaryPath = if (config.appType in setOf("PHP_APP", "WORDPRESS")) {
                com.webtoapp.core.wordpress.WordPressDependencyManager.getPhpExecutablePath(context)
            } else null
            val nodeBinaryPath = if (config.appType == "NODEJS_APP") {
                com.webtoapp.core.nodejs.NodeDependencyManager.getNodeLibraryPath(context)
            } else null
            val pythonBinaryPath = if (config.appType == "PYTHON_APP") {
                com.webtoapp.core.python.PythonDependencyManager.getPythonExecutablePath(context)
            } else null
            val muslLinkerPath = if (config.appType == "PYTHON_APP") {
                com.webtoapp.core.python.PythonDependencyManager.getMuslLinkerPath(context)
            } else null
            val builderMuslLinkerPath = if (config.appType == "PYTHON_APP") {
                com.webtoapp.core.python.PythonDependencyManager.getBuilderMuslLinkerPath(context)
            } else null

            val preflight = BuildInputPreflight.check(
                BuildInputPreflightRequest(
                    appType = config.appType,
                    htmlEntryFile = config.htmlEntryFile,
                    mediaContentPath = mediaContentPath,
                    htmlFiles = htmlFiles,
                    galleryItems = galleryItems,
                    multiWebSites = webApp.multiWebConfig?.sites.orEmpty(),
                    wordPressProjectDir = wordPressProjectDir,
                    nodejsProjectDir = nodejsProjectDir,
                    phpAppProjectDir = phpAppProjectDir,
                    pythonAppProjectDir = pythonAppProjectDir,
                    goAppProjectDir = goAppProjectDir,
                    frontendProjectDir = frontendProjectDir,
                    multiWebProjectDir = config.multiWebProjectId.takeIf { it.isNotBlank() }
                        ?.let { File(context.filesDir, "html_projects/$it") },
                    networkTrustConfig = config.networkTrustConfig,
                    phpBinaryPath = phpBinaryPath,
                    nodeBinaryPath = nodeBinaryPath,
                    pythonBinaryPath = pythonBinaryPath,
                    muslLinkerPath = muslLinkerPath,
                    builderMuslLinkerPath = builderMuslLinkerPath
                )
            )
            logger.logKeyValue("preflightPassed", preflight.passed)
            logger.logKeyValue("preflightIssueCount", preflight.issues.size)
            preflight.issues.forEachIndexed { index, issue ->
                logger.warn("preflight[$index] ${issue.summary()}")
            }
            if (!preflight.passed) {
                return@withContext failBuild(
                    stage = BuildStage.INPUT_PRECHECK,
                    cause = BuildFailureCause.INPUT_PRECHECK_FAILED,
                    message = "Build input preflight failed: ${preflight.issues.size} issue(s)",
                    details = mapOf(
                        "appName" to webApp.name,
                        "appType" to webApp.appType,
                        "packageName" to packageName,
                        "issueCount" to preflight.issues.size,
                        "issues" to preflight.issues.joinToString(" | ") { it.summary() }
                    )
                )
            }

            val errorPageMediaPath = webApp.webViewConfig.errorPageConfig.customMediaPath
                ?.takeIf { it.isNotBlank() && !it.startsWith("data:") && !it.startsWith("http") && !it.startsWith("file://") }
            val multiWebProjectDir = config.multiWebProjectId.takeIf { it.isNotBlank() }
                ?.let { File(context.filesDir, "html_projects/$it") }

            logger.section("Incremental Build Plan")
            val incrementalPlan = buildCache.plan(
                webApp = webApp,
                packageName = packageName,
                config = config,
                templateApk = templateApk,
                encryptionEnabled = encryptionConfig.enabled,
                abiFilters = architecture.abiFilters,
                projectDirs = listOf(
                    wordPressProjectDir,
                    nodejsProjectDir,
                    frontendProjectDir,
                    phpAppProjectDir,
                    pythonAppProjectDir,
                    goAppProjectDir,
                    htmlProjectDir,
                    multiWebProjectDir
                ),
                mediaContentPath = mediaContentPath,
                splashMediaPath = webApp.getSplashMediaPath(),
                bgmPlaylistPaths = bgmPlaylistPaths,
                htmlFiles = htmlFiles,
                galleryItems = galleryItems,
                errorPageMediaPath = errorPageMediaPath,
                forceFullRebuild = forceFullRebuild
            )
            logger.logKeyValue("incrementalMode", incrementalPlan.mode.name)
            logger.logKeyValue("incrementalReason", incrementalPlan.reason)
            logger.logKeyValue("identityFingerprint", incrementalPlan.identityFingerprint.take(16) + "...")
            logger.logKeyValue("contentFingerprint", incrementalPlan.contentFingerprint.take(16) + "...")
            var usedIncremental = false

            logger.section("Modify APK Content")
            currentStage = BuildStage.MODIFY_APK
            if (encryptionConfig.enabled) {
                onProgress(30, "Encrypting resources...")
                logger.log("Encryption mode enabled")
            }
            val progressMessage = java.util.concurrent.atomic.AtomicReference(
                if (encryptionConfig.enabled) "Encrypting and processing resources..." else "Processing resources..."
            )

            when (incrementalPlan.mode) {
                IncrementalBuildMode.REUSE_UNSIGNED -> {
                    usedIncremental = true
                    onProgress(35, "Reusing cached unsigned APK...")
                    val cached = incrementalPlan.cachedUnsigned
                        ?: throw IllegalStateException("REUSE_UNSIGNED without cache file")
                    cached.copyTo(unsignedApk, overwrite = true)
                    logger.log("Reused cached unsigned APK (${unsignedApk.length() / 1024} KB)")
                }
                IncrementalBuildMode.CONTENT_OVERLAY -> {
                    usedIncremental = true
                    onProgress(30, "Applying content overlay...")
                    val cached = incrementalPlan.cachedUnsigned
                        ?: throw IllegalStateException("CONTENT_OVERLAY without cache file")
                    try {
                        modifyApk(
                            sourceApk = cached,
                            outputApk = unsignedApk,
                            config = config,
                            iconPath = webApp.iconPath,
                            splashMediaPath = webApp.getSplashMediaPath(),
                            mediaContentPath = mediaContentPath,
                            bgmPlaylistPaths = bgmPlaylistPaths,
                            bgmLrcDataList = bgmLrcDataList,
                            bgmCoverPaths = bgmCoverPaths,
                            htmlFiles = htmlFiles,
                            galleryItems = galleryItems,
                            encryptionConfig = encryptionConfig,
                            encryptionKey = encryptionKey,
                            abiFilters = architecture.abiFilters,
                            wordPressProjectDir = wordPressProjectDir,
                            nodejsProjectDir = nodejsProjectDir,
                            frontendProjectDir = frontendProjectDir,
                            phpAppProjectDir = phpAppProjectDir,
                            pythonAppProjectDir = pythonAppProjectDir,
                            goAppProjectDir = goAppProjectDir,
                            htmlProjectDir = htmlProjectDir,
                            errorPageMediaPath = errorPageMediaPath,
                            perfConfig = perfConfig,
                            mode = ModifyApkMode.CONTENT_OVERLAY,
                            capabilityPlan = capabilityPlan,
                            featurePackMerge = featurePackMerge
                        ) { progress, stageMessage ->
                            if (stageMessage.isNotBlank()) {
                                progressMessage.set(stageMessage)
                            }
                            val msg = stageMessage.ifBlank { "Updating app content..." }
                            onProgress(30 + (progress * 0.4).toInt(), msg)
                        }
                    } catch (e: Exception) {
                        logger.warn("Content overlay failed, falling back to full rebuild: ${e.message}")
                        AppLogger.w("ApkBuilder", "Content overlay failed, fallback full", e)
                        usedIncremental = false
                        if (unsignedApk.exists()) unsignedApk.delete()
                        modifyApk(
                            sourceApk = templateApk,
                            outputApk = unsignedApk,
                            config = config,
                            iconPath = webApp.iconPath,
                            splashMediaPath = webApp.getSplashMediaPath(),
                            mediaContentPath = mediaContentPath,
                            bgmPlaylistPaths = bgmPlaylistPaths,
                            bgmLrcDataList = bgmLrcDataList,
                            bgmCoverPaths = bgmCoverPaths,
                            htmlFiles = htmlFiles,
                            galleryItems = galleryItems,
                            encryptionConfig = encryptionConfig,
                            encryptionKey = encryptionKey,
                            abiFilters = architecture.abiFilters,
                            wordPressProjectDir = wordPressProjectDir,
                            nodejsProjectDir = nodejsProjectDir,
                            frontendProjectDir = frontendProjectDir,
                            phpAppProjectDir = phpAppProjectDir,
                            pythonAppProjectDir = pythonAppProjectDir,
                            goAppProjectDir = goAppProjectDir,
                            htmlProjectDir = htmlProjectDir,
                            errorPageMediaPath = errorPageMediaPath,
                            perfConfig = perfConfig,
                            mode = ModifyApkMode.FULL,
                            capabilityPlan = capabilityPlan,
                            featurePackMerge = featurePackMerge
                        ) { progress, stageMessage ->
                            if (stageMessage.isNotBlank()) {
                                progressMessage.set(stageMessage)
                            }
                            val msg = when {
                                stageMessage.isNotBlank() -> stageMessage
                                perfOptEnabled && encryptionConfig.enabled -> "Optimizing & encrypting resources..."
                                perfOptEnabled -> "Optimizing resources..."
                                else -> progressMessage.get()
                            }
                            onProgress(30 + (progress * 0.4).toInt(), msg)
                        }
                    }
                }
                IncrementalBuildMode.FULL -> {
                    modifyApk(
                        sourceApk = templateApk,
                        outputApk = unsignedApk,
                        config = config,
                        iconPath = webApp.iconPath,
                        splashMediaPath = webApp.getSplashMediaPath(),
                        mediaContentPath = mediaContentPath,
                        bgmPlaylistPaths = bgmPlaylistPaths,
                        bgmLrcDataList = bgmLrcDataList,
                        bgmCoverPaths = bgmCoverPaths,
                        htmlFiles = htmlFiles,
                        galleryItems = galleryItems,
                        encryptionConfig = encryptionConfig,
                        encryptionKey = encryptionKey,
                        abiFilters = architecture.abiFilters,
                        wordPressProjectDir = wordPressProjectDir,
                        nodejsProjectDir = nodejsProjectDir,
                        frontendProjectDir = frontendProjectDir,
                        phpAppProjectDir = phpAppProjectDir,
                        pythonAppProjectDir = pythonAppProjectDir,
                        goAppProjectDir = goAppProjectDir,
                        htmlProjectDir = htmlProjectDir,
                        errorPageMediaPath = errorPageMediaPath,
                        perfConfig = perfConfig,
                        mode = ModifyApkMode.FULL,
                        capabilityPlan = capabilityPlan,
                        featurePackMerge = featurePackMerge
                    ) { progress, stageMessage ->
                        if (stageMessage.isNotBlank()) {
                            progressMessage.set(stageMessage)
                        }
                        val msg = when {
                            stageMessage.isNotBlank() -> stageMessage
                            perfOptEnabled && encryptionConfig.enabled -> "Optimizing & encrypting resources..."
                            perfOptEnabled -> "Optimizing resources..."
                            else -> progressMessage.get()
                        }
                        onProgress(30 + (progress * 0.4).toInt(), msg)
                    }
                }
            }

            onProgress(70, "Signing APK...")

            if (!unsignedApk.exists() || unsignedApk.length() == 0L) {
                return@withContext failBuild(
                    stage = BuildStage.MODIFY_APK,
                    cause = BuildFailureCause.UNSIGNED_OUTPUT_INVALID,
                    message = "Failed to generate unsigned APK",
                    details = mapOf(
                        "unsignedApk" to unsignedApk.absolutePath,
                        "exists" to unsignedApk.exists(),
                        "sizeBytes" to unsignedApk.length()
                    )
                )
            }
            logger.logKeyValue("unsignedApkSize", "${unsignedApk.length() / 1024} KB")

            logger.section("Zip Align APK")
            val zipAligned = ZipAligner.alignInPlace(unsignedApk)
            logger.logKeyValue("zipAlign16kNativeLibs", zipAligned)
            if (!zipAligned) {
                logger.warn("ZipAlign failed; APK signing will continue with the generated artifact")
            } else if (!ZipAligner.verifyNativeLibAlignment(unsignedApk)) {
                logger.warn("One or more native libraries are not 16KB zip-aligned after ZipAlign")
            }

            logger.section("Verify APK Artifact")
            currentStage = BuildStage.ARTIFACT_VERIFY
            val artifactVerification = ApkArtifactVerifier.verify(
                ApkArtifactVerificationRequest(
                    apkFile = unsignedApk,
                    config = config,
                    encryptionEnabled = encryptionConfig.enabled,
                    htmlFiles = htmlFiles,
                    galleryItems = galleryItems,
                    multiWebSites = webApp.multiWebConfig?.sites.orEmpty(),
                    wordPressProjectDir = wordPressProjectDir,
                    nodejsProjectDir = nodejsProjectDir,
                    phpAppProjectDir = phpAppProjectDir,
                    pythonAppProjectDir = pythonAppProjectDir,
                    goAppProjectDir = goAppProjectDir,
                    frontendProjectDir = frontendProjectDir,
                    multiWebProjectDir = config.multiWebProjectId.takeIf { it.isNotBlank() }
                        ?.let { File(context.filesDir, "html_projects/$it") }
                )
            )
            logger.logKeyValue("artifactEntryCount", artifactVerification.entryCount)
            logger.logKeyValue("artifactCheckedEntryCount", artifactVerification.checkedEntryCount)
            logger.logKeyValue("artifactVerificationPassed", artifactVerification.passed)
            artifactVerification.issues.forEachIndexed { index, issue ->
                logger.warn("artifact[$index] ${issue.summary()}")
            }
            if (!artifactVerification.passed) {
                return@withContext failBuild(
                    stage = BuildStage.ARTIFACT_VERIFY,
                    cause = BuildFailureCause.ARTIFACT_VERIFICATION_FAILED,
                    message = "APK artifact verification failed: ${artifactVerification.issues.size} issue(s)",
                    details = mapOf(
                        "unsignedApk" to unsignedApk.absolutePath,
                        "appName" to webApp.name,
                        "appType" to webApp.appType,
                        "packageName" to packageName,
                        "issueCount" to artifactVerification.issues.size,
                        "issues" to artifactVerification.issues.joinToString(" | ") { it.summary() }
                    )
                )
            }

            if (!encryptionConfig.enabled && unsignedApk.isFile && unsignedApk.length() > 0L) {
                buildCache.saveUnsigned(
                    webApp = webApp,
                    packageName = packageName,
                    unsignedApk = unsignedApk,
                    identityFingerprint = incrementalPlan.identityFingerprint,
                    contentFingerprint = incrementalPlan.contentFingerprint,
                    shellTemplateId = incrementalPlan.shellTemplateId
                )
            }

            logger.section("Sign APK")
            currentStage = BuildStage.SIGN
            logger.logKeyValue("signerType", signer.getSignerType().name)

            val signSuccess = try {
                signer.sign(unsignedApk, signedApk)
            } catch (e: Exception) {
                return@withContext failBuild(
                    stage = BuildStage.SIGN,
                    cause = BuildFailureCause.SIGNING_EXCEPTION,
                    message = "Signing failed: ${e.message ?: "Unknown error"}",
                    throwable = e,
                    details = mapOf(
                        "unsignedApk" to unsignedApk.absolutePath,
                        "signedApk" to signedApk.absolutePath,
                        "signerType" to signer.getSignerType().name
                    )
                )
            }

            if (!signedApk.exists() || signedApk.length() == 0L) {
                if (signedApk.exists()) signedApk.delete()
                return@withContext failBuild(
                    stage = BuildStage.SIGN,
                    cause = BuildFailureCause.SIGNED_OUTPUT_INVALID,
                    message = "APK signing failed: output file invalid",
                    details = mapOf(
                        "signedApk" to signedApk.absolutePath,
                        "exists" to signedApk.exists(),
                        "sizeBytes" to signedApk.length()
                    )
                )
            }

            logger.logKeyValue("signedApkSize", "${signedApk.length() / 1024} KB")

            if (!signSuccess) {

                logger.warn("ApkVerifier reported issues, but signed APK file is valid (${signedApk.length() / 1024} KB). Continuing build.")
            }

            onProgress(85, "Verifying APK...")
            currentStage = BuildStage.VERIFY
            logger.section("Verify APK")

            val parseResult = debugApkStructure(signedApk)
            logger.logKeyValue("apkPreParseResult", parseResult)
            if (!parseResult) {
                logger.warn("APK pre-parse failed, may not be installable")
            }

            onProgress(90, "Analyzing & cleaning up...")
            currentStage = BuildStage.ANALYZE_CLEANUP

            val analysisReport = coroutineScope {
                val analysisDeferred = async {
                    try {
                        val report = ApkAnalyzer.analyze(signedApk)
                        logger.section("APK Analysis")
                        logger.log(ApkAnalyzer.formatReport(report))
                        report
                    } catch (e: Exception) {
                        AppLogger.e("ApkBuilder", "APK analysis failed (non-fatal)", e)
                        null
                    }
                }
                val cleanupDeferred = async {
                    unsignedApk.delete()
                    cleanTempFiles()
                }

                cleanupDeferred.await()
                analysisDeferred.await()
            }

            onProgress(100, "Build complete")

            logger.logKeyValue("finalApkPath", signedApk.absolutePath)
            logger.logKeyValue("finalApkSize", "${signedApk.length() / 1024} KB")
            logger.endLog(true, "Build successful")

            logger.logKeyValue("buildMode", if (usedIncremental) incrementalPlan.mode.name else IncrementalBuildMode.FULL.name)
            logger.logKeyValue("incrementalUsed", usedIncremental)
            val resolvedMode = if (usedIncremental) {
                incrementalPlan.mode.name
            } else {
                IncrementalBuildMode.FULL.name
            }
            BuildResult.Success(
                apkFile = signedApk,
                logPath = logger.getCurrentLogPath(),
                analysisReport = analysisReport,
                incremental = usedIncremental,
                buildMode = resolvedMode,
                buildReason = if (usedIncremental) {
                    incrementalPlan.reason
                } else if (incrementalPlan.mode == IncrementalBuildMode.FULL) {
                    incrementalPlan.reason
                } else {
                    "fallbackFull:" + incrementalPlan.reason
                }
            )

        } catch (e: Exception) {
            cleanTempFiles()

            failBuild(
                stage = currentStage,
                cause = BuildFailureCause.UNHANDLED_EXCEPTION,
                message = "Build failed: ${e.message ?: "Unknown error"}",
                throwable = e,
                details = mapOf(
                    "appName" to webApp.name,
                    "appType" to webApp.appType,
                    "packageName" to currentPackageName,
                    "unsignedApk" to currentUnsignedApkPath,
                    "signedApk" to currentSignedApkPath
                )
            )
        }
    }

    private fun failBuild(
        stage: BuildStage,
        cause: BuildFailureCause,
        message: String,
        throwable: Throwable? = null,
        details: Map<String, Any?> = emptyMap()
    ): BuildResult.Error {
        val diagnostic = BuildDiagnostic(
            stage = stage,
            cause = cause,
            details = details.filterValues { it != null }
        )

        logger.section("Build Failure Diagnostic")
        logger.logKeyValue("stage", diagnostic.stage)
        logger.logKeyValue("cause", diagnostic.cause)
        diagnostic.details.forEach { (key, value) ->
            logger.logKeyValue("context.$key", value)
        }
        logger.error(message, throwable)
        logger.endLog(false, "${stage.label}: $message")

        return BuildResult.Error(
            message = message,
            logPath = logger.getCurrentLogPath(),
            diagnostic = diagnostic
        )
    }

    private fun getOrCreateTemplate(config: ApkConfig): File? {
        return try {
            val sourceTemplate = templateProvider.getTemplateFor(config) ?: return null
            val sourceName = sourceTemplate.name
            val templateFile = File(tempDir, "base_template_${sourceName.substringBeforeLast('.')}.apk")

            val needsCopy = !templateFile.exists() ||
                templateFile.length() != sourceTemplate.length() ||
                templateFile.lastModified() < sourceTemplate.lastModified()

            if (needsCopy) {
                sourceTemplate.copyTo(templateFile, overwrite = true)
                AppLogger.i("ApkBuilder", "Template APK copied from ${templateProvider.sourceName}")
            } else {
                AppLogger.d("ApkBuilder", "Using cached template APK from ${templateProvider.sourceName}")
            }
            templateFile
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Operation failed", e)
            null
        }
    }

    private suspend fun modifyApk(
        sourceApk: File,
        outputApk: File,
        config: ApkConfig,
        iconPath: String?,
        splashMediaPath: String?,
        mediaContentPath: String? = null,
        bgmPlaylistPaths: List<String> = emptyList(),
        bgmLrcDataList: List<LrcData?> = emptyList(),
        bgmCoverPaths: List<String?> = emptyList(),
        htmlFiles: List<com.webtoapp.data.model.HtmlFile> = emptyList(),
        galleryItems: List<com.webtoapp.data.model.GalleryItem> = emptyList(),
        encryptionConfig: EncryptionConfig = EncryptionConfig.DISABLED,
        encryptionKey: SecretKey? = null,
        abiFilters: List<String> = emptyList(),
        wordPressProjectDir: File? = null,
        nodejsProjectDir: File? = null,
        frontendProjectDir: File? = null,
        phpAppProjectDir: File? = null,
        pythonAppProjectDir: File? = null,
        goAppProjectDir: File? = null,
        htmlProjectDir: File? = null,
        errorPageMediaPath: String? = null,
        perfConfig: com.webtoapp.core.linux.PerformanceOptimizer.OptimizeConfig? = null,
        mode: ModifyApkMode = ModifyApkMode.FULL,
        capabilityPlan: CapabilityPlan = CapabilityPlan(emptyList(), emptyList(), emptyList(), true),
        featurePackMerge: FeaturePackMerger.MergeResult? = null,
        onProgress: (Int, String) -> Unit
    ) {
        val activePlan = if (capabilityPlan.abiFilters.isEmpty() && abiFilters.isNotEmpty()) {
            capabilityPlan.copy(abiFilters = abiFilters)
        } else {
            capabilityPlan
        }
        val packMerge = featurePackMerge ?: FeaturePackMerger.prepare(
            context = context,
            plan = activePlan,
            logger = logger
        )
        logger.log(
            "modifyApk started, mode=${mode.name}, encryption=${encryptionConfig.enabled}, " +
                "abiFilter=${abiFilters.ifEmpty { "all" }}, liteOnly=${activePlan.liteOnly}, " +
                "features=${activePlan.features}, source=${sourceApk.name}"
        )
        val iconBitmap = iconPath?.let { template.loadBitmap(it) }
            ?: generateDefaultIcon(config.appName, config.themeType)
        var hasConfigFile = false
        var strippedNativeLibSize = 0L
        val replacedIconPaths = mutableSetOf<String>()
        var discoveredOldIconPaths = emptySet<String>()

        val assetEncryptor = if (encryptionConfig.enabled && encryptionKey != null) {
            AssetEncryptor(encryptionKey)
        } else null

        ZipFile(sourceApk).use { zipIn ->
            ZipOutputStream(FileOutputStream(outputApk)).use { zipOut ->

                val entries = zipIn.entries().toList()
                    .sortedWith(compareBy<ZipEntry> { it.name != "resources.arsc" })
                val entryNames = entries.map { it.name }.toSet()

                var processedCount = 0

                entries.forEach { entry ->
                    processedCount++
                    val progressLabel = if (mode == ModifyApkMode.CONTENT_OVERLAY) {
                        "Updating cached base..."
                    } else {
                        "Repacking base template..."
                    }
                    onProgress((processedCount * 100) / entries.size, progressLabel)

                    if (mode == ModifyApkMode.CONTENT_OVERLAY) {
                        when {
                            entry.name.startsWith("META-INF/") &&
                                (entry.name.endsWith(".SF") || entry.name.endsWith(".RSA") ||
                                    entry.name.endsWith(".DSA") || entry.name == "META-INF/MANIFEST.MF") -> {
                            }
                            buildCache.isContentReplaceableEntry(entry.name) -> {
                            }
                            else -> {
                                ZipUtils.copyEntryPreserveMethod(zipIn, zipOut, entry)
                            }
                        }
                        return@forEach
                    }

                    when {

                        entry.name.startsWith("META-INF/") &&
                        (entry.name.endsWith(".SF") || entry.name.endsWith(".RSA") ||
                         entry.name.endsWith(".DSA") || entry.name == "META-INF/MANIFEST.MF") -> {

                        }

                        FeaturePackMerger.shouldStripBloatEntry(entry.name, activePlan) -> {
                            strippedNativeLibSize += entry.size
                            AppLogger.d("ApkBuilder", "Stripped bloat entry: ${entry.name}")
                        }

                        shouldStripI18nPack(entry.name, config.language) -> {
                            AppLogger.d("ApkBuilder", "Stripped unused i18n pack: ${entry.name}")
                        }

                        entry.name.startsWith("assets/splash_media.") -> {
                            AppLogger.d("ApkBuilder", "Skipping old splash media: ${entry.name}")
                        }

                        entry.name == "AndroidManifest.xml" -> {
                            val originalData = zipIn.getInputStream(entry).readBytes()

                            val aliasCount = config.disguiseConfig?.getAliasCount() ?: 0

                            val modifiedData = axmlRebuilder.expandAndModifyFull(
                                originalData,
                                originalPackageName,
                                config.packageName,
                                config.versionCode,
                                config.versionName,
                                aliasCount,
                                config.appName,
                                config.deepLinkHosts,
                                config.deepLinkSchemes,
                                buildRequiredPermissions(config),
                                buildRequiredComponents(config)
                            )
                            writeEntryDeflated(zipOut, entry.name, modifiedData)
                            if (aliasCount > 0) {
                                logger.log("Added $aliasCount activity-alias (multi desktop icons)")
                                if (aliasCount >= 100) {
                                    val overheadKb = (aliasCount * 520L) / 1024
                                    val impactLevel = com.webtoapp.core.appearance.DisguiseConfig.assessImpactLevel(aliasCount + 1)
                                    logger.log("⚡ Icon Storm mode: $aliasCount aliases, ~${overheadKb}KB manifest overhead, impact level $impactLevel")
                                }
                            }
                        }

                        entry.name == "resources.arsc" -> {
                            val originalData = zipIn.getInputStream(entry).readBytes()
                            val modifiedData = arscRebuilder.rebuildWithNewAppNameAndIcons(
                                originalData,
                                config.appName,
                                replaceIcons = true
                            )

                            discoveredOldIconPaths = arscRebuilder.getLastDiscoveredIconPaths()
                            logger.log("Discovered old icon paths from ARSC: $discoveredOldIconPaths")
                            writeEntryStored(zipOut, entry.name, modifiedData)
                        }

                        entry.name == ApkTemplate.CONFIG_PATH -> {
                            hasConfigFile = true
                            writeConfigEntry(zipOut, config, assetEncryptor, encryptionConfig)
                        }

                        iconBitmap != null && (isIconEntry(entry.name) || discoveredOldIconPaths.contains(entry.name)) -> {

                            replaceIconEntry(zipOut, entry.name, iconBitmap)
                            replacedIconPaths.add(entry.name)
                        }

                        entry.name.startsWith("lib/") -> {
                            val abi = entry.name.removePrefix("lib/").substringBefore("/")
                            val libName = entry.name.substringAfterLast("/")

                            when {

                                abiFilters.isNotEmpty() && !abiFilters.contains(abi) -> {
                                    AppLogger.d("ApkBuilder", "Skipping architecture: ${entry.name}")
                                }

                                !isRequiredNativeLib(libName, config.appType, config.engineType) -> {
                                    val sizeKb = if (entry.size >= 0) entry.size / 1024 else entry.compressedSize / 1024
                                    AppLogger.d("ApkBuilder", "APK slim: stripped $libName (${sizeKb} KB)")
                                    logger.log("APK slim: stripped $libName (${sizeKb} KB) - not needed for ${config.appType}")
                                    strippedNativeLibSize += if (entry.size >= 0) entry.size else entry.compressedSize
                                }
                                else -> {
                                    ZipUtils.copyEntryPreserveMethod(zipIn, zipOut, entry)
                                }
                            }
                        }

                        entry.name.startsWith("kotlin/") || entry.name == "DebugProbesKt.bin" -> {

                        }

                        isEditorOnlyAsset(entry.name, config.appType, config.engineType) -> {
                            AppLogger.d("ApkBuilder", "APK slim: stripped editor asset: ${entry.name}")
                        }

                        entry.name.startsWith("assets/features/") || entry.name.startsWith("features/") -> {
                            AppLogger.d("ApkBuilder", "APK slim: stripped stale feature pack entry: ${entry.name}")
                        }

                        perfConfig != null && perfConfig.removeUnusedResources &&
                        com.webtoapp.core.linux.PerformanceOptimizer.getRemovableEntries(entry.name, config.appType) -> {
                            AppLogger.d("ApkBuilder", "Perf: removed unused resource: ${entry.name}")
                        }

                        perfConfig != null && entry.name.startsWith("assets/") && isOptimizableAsset(entry.name) -> {
                            val originalData = zipIn.getInputStream(entry).readBytes()
                            val optimizedData = com.webtoapp.core.linux.PerformanceOptimizer.optimizeBytesForApk(
                                context, entry.name.substringAfterLast("/"), originalData, perfConfig
                            )
                            writeEntryDeflated(zipOut, entry.name, optimizedData)
                            if (optimizedData.size < originalData.size) {
                                AppLogger.d("ApkBuilder", "Perf: optimized ${entry.name}: ${originalData.size} -> ${optimizedData.size}")
                            }
                        }

                        else -> {
                            copyEntry(zipIn, zipOut, entry)
                        }
                    }
                }

                if (!hasConfigFile) {
                    writeConfigEntry(zipOut, config, assetEncryptor, encryptionConfig)
                }

                injectSelectedI18nPack(zipOut, config.language)
                writeEntryDeflated(zipOut, "assets/features/enabled.json", packMerge.enabledJson)
                if (packMerge.extraEntries.isNotEmpty()) {
                    FeaturePackMerger.writeEntries(zipOut, packMerge.extraEntries)
                    logger.log("Feature pack entries written: ${packMerge.extraEntries.size}")
                } else {
                    logger.log("Feature enabled.json written (packs=${activePlan.features.size}, liteOnly=${activePlan.liteOnly})")
                }

                if (config.adBlock.enabled) {
                    val globalAdBlock = WebToAppApplication.adBlock
                    try {
                        val compiledRules = globalAdBlock.compileRulesText(
                            context = context,
                            subscriptionUrls = config.adBlock.subscriptions
                        )
                        if (compiledRules.isNotEmpty()) {
                            val rulesData = compiledRules.toByteArray(Charsets.UTF_8)
                            writeEntryDeflated(zipOut, "assets/wta_adblock_compiled.txt", rulesData)
                            logger.log("AdBlock compiled rules bundled (${rulesData.size} bytes)")
                        } else {
                            logger.log("AdBlock enabled but compiled rules empty (subscriptions=${config.adBlock.subscriptions.size}, rules=${config.adBlock.rules.size})")
                        }
                    } catch (e: Exception) {
                        logger.log("AdBlock compile failed: ${e.message}")
                        throw e
                    }
                }

                ensureRequiredRuntimeAssets(zipOut, config.appType, entryNames)

                if (encryptionConfig.enabled) {

                    val signatureHash = signer.getCertificateSignatureHash()
                    encryptedApkBuilder.writeEncryptionMetadata(zipOut, encryptionConfig, config.packageName, signatureHash)
                    logger.log("Encryption metadata written")
                }

                if (encryptionConfig.enabled) {
                    onProgress(78, "Applying runtime protection...")
                    logger.section("Runtime Protection")
                    logger.log("Resource encryption enabled: shell enforces runtime protection (anti-debug / anti-Frida / DEX-tamper) at launch via app_config.json")
                }

                if (perfConfig != null && perfConfig.injectPerformanceScript) {
                    onProgress(82, "Injecting performance assets...")
                    logger.section("Performance Optimization")
                    val perfScript = com.webtoapp.core.linux.PerformanceOptimizer.generatePerformanceScript()
                    val scriptData = perfScript.toByteArray(Charsets.UTF_8)
                    writeEntryDeflated(zipOut, "assets/wta_perf_optimize.js", scriptData)
                    logger.log("Performance script injected (${scriptData.size} bytes)")
                    logger.log("Perf features: images=${perfConfig.compressImages}, code=${perfConfig.minifyCode}, " +
                        "webp=${perfConfig.convertToWebP}, preload=${perfConfig.injectPreloadHints}, " +
                        "lazy=${perfConfig.injectLazyLoading}, scripts=${perfConfig.optimizeScripts}")
                }

                if (mode == ModifyApkMode.FULL) {
                    if (iconBitmap != null && replacedIconPaths.isEmpty()) {
                        addIconsToApk(zipOut, iconBitmap)
                        logger.log("Added PNG mipmap icons (no existing PNG icons found in template)")
                    } else if (iconBitmap != null) {
                        logger.log("Replaced ${replacedIconPaths.size} existing PNG icon entries")
                    }

                    if (iconBitmap != null) {
                        addAdaptiveIconPngs(zipOut, iconBitmap, entryNames)
                    }
                }

                AppLogger.d("ApkBuilder", "Splash config: splashEnabled=${config.splashEnabled}, splashMediaPath=$splashMediaPath, splashType=${config.splashType}")
                onProgress(86, "Embedding app assets...")
                if (config.splashEnabled && splashMediaPath != null) {
                    addSplashMediaToAssets(zipOut, splashMediaPath, config.splashType, assetEncryptor, encryptionConfig)
                } else {
                    AppLogger.w("ApkBuilder", "Skipping splash embed: splashEnabled=${config.splashEnabled}, splashMediaPath=$splashMediaPath")
                }

                if (errorPageMediaPath != null) {
                    addErrorPageMediaToAssets(zipOut, errorPageMediaPath, assetEncryptor, encryptionConfig)
                }

                if (config.statusBarBackgroundType == "IMAGE" && !config.statusBarBackgroundImage.isNullOrEmpty()) {
                    addStatusBarBackgroundToAssets(zipOut, config.statusBarBackgroundImage!!)
                }

                if (config.floatingWindowEnabled && !config.floatingWindowMinimizedIconPath.isNullOrEmpty()) {
                    addFloatingWindowMinimizedIconToAssets(zipOut, config.floatingWindowMinimizedIconPath!!)
                }

                if (config.bgmEnabled && bgmPlaylistPaths.isNotEmpty()) {
                    logger.log("Embedding BGM: ${bgmPlaylistPaths.size} files")
                    addBgmToAssets(zipOut, bgmPlaylistPaths, bgmLrcDataList, bgmCoverPaths, assetEncryptor, encryptionConfig)
                }

                val projectDir = when (config.appType) {
                    "WORDPRESS" -> wordPressProjectDir
                    "NODEJS_APP" -> nodejsProjectDir
                    "PHP_APP" -> phpAppProjectDir
                    "PYTHON_APP" -> pythonAppProjectDir
                    "GO_APP" -> goAppProjectDir
                    "FRONTEND" -> frontendProjectDir
                    "HTML" -> htmlProjectDir
                    else -> null
                }
                val secondaryProjectDir = when (config.appType) {
                    "MULTI_WEB" -> config.multiWebProjectId.takeIf { it.isNotBlank() }
                        ?.let { File(context.filesDir, "html_projects/$it") }
                    else -> null
                }
                val multiWebSiteSourceDirs = if (config.appType == "MULTI_WEB") {
                    config.multiWeb.sites.mapNotNull { site ->
                        val dir = site.sourceProjectId.takeIf { it.isNotBlank() }
                            ?.let { File(context.filesDir, "html_projects/$it") }
                        if (dir != null && dir.exists()) site.id to dir else null
                    }.toMap()
                } else emptyMap()

                val embedder = AppContentEmbedderFactory.create(config.appType)
                if (embedder != null) {
                    if (config.appType == "GO_APP" && projectDir != null) {
                        onProgress(90, "Verifying Go binary...")
                        ensureGoProjectBinaryForExport(projectDir, config, onProgress)
                    }
                    onProgress(94, "Embedding project files...")
                    val embedCtx = EmbedContext(
                        config = config,
                        logger = logger,
                        encryptor = assetEncryptor,
                        encryptionConfig = encryptionConfig,
                        mediaContentPath = mediaContentPath,
                        htmlFiles = htmlFiles,
                        galleryItems = galleryItems,
                        projectDir = projectDir,
                        secondaryProjectDir = secondaryProjectDir,
                        fnAddMediaContent = ::addMediaContentToAssets,
                        fnAddHtmlFiles = ::addHtmlFilesToAssets,
                        fnAddGalleryItems = ::addGalleryItemsToAssets,
                        fnAddWordPressFiles = ::addWordPressFilesToAssets,
                        fnAddNodeJsFiles = ::addNodeJsFilesToAssets,
                        fnAddFrontendFiles = ::addFrontendFilesToAssets,
                        fnAddPhpAppFiles = ::addPhpAppFilesToAssets,
                        fnAddPythonAppFiles = ::addPythonAppFilesToAssets,
                        fnAddGoAppFiles = ::addGoAppFilesToAssets,
                        multiWebSiteSourceDirs = multiWebSiteSourceDirs
                    )
                    val result = embedder.embed(zipOut, embedCtx)
                    logger.log("Content embedding [${config.appType}]: ${result.message}")
                }

                if (mode == ModifyApkMode.FULL && config.engineType == "GECKOVIEW") {
                    onProgress(98, "Injecting native runtime...")
                    logger.section("Inject GeckoView Runtime (native libs + omni.ja)")
                    injectGeckoViewRuntime(zipOut, abiFilters)
                }
            }
        }

        if (strippedNativeLibSize > 0) {
            val savedMb = strippedNativeLibSize / 1024 / 1024
            logger.log("APK slim: total native lib savings: ${savedMb} MB")
            AppLogger.d("ApkBuilder", "APK slim: stripped ${savedMb} MB of unused native libraries")
        }

        iconBitmap?.recycle()
    }

    private fun isRequiredNativeLib(libName: String, appType: String, engineType: String): Boolean {

        if (libName == "libc++_shared.so") {
            return true
        }

        if (libName == "libcrypto_engine.so" || libName == "libapk_optimizer.so" || libName == "libcrypto_optimized.so" || libName == "libperf_engine.so" || libName == "libbrowser_kernel.so") {
            return false
        }

        if (libName == "libphp.so") {
            return appType in setOf("WORDPRESS", "PHP_APP")
        }

        if (libName == "libnode_bridge.so" || libName == "libnode.so") {
            return appType == "NODEJS_APP"
        }

        if (libName == "libgo_exec_loader.so") {
            return appType == "GO_APP"
        }

        if (libName == "libpython3.so" || libName == "libmusl-linker.so") {
            return appType == "PYTHON_APP"
        }

        val geckoViewLibs = setOf(
            "libgkcodecs.so",
            "libminidump_analyzer.so",
            "libnss3.so",
            "libfreebl3.so",
            "libsoftokn3.so",
            "liblgpllibs.so",
            "libplugin-container.so"
        )
        if (libName in geckoViewLibs) {

            return false
        }

        return true
    }

    private fun injectGeckoViewRuntime(
        zipOut: ZipOutputStream,
        abiFilters: List<String>
    ) {
        try {
            val engineFileManager = com.webtoapp.core.engine.download.EngineFileManager(context)
            val engineType = com.webtoapp.core.engine.EngineType.GECKOVIEW
            val nativeLibs = engineFileManager.listEngineNativeLibs(engineType)

            if (nativeLibs.isEmpty()) {
                logger.warn("GeckoView engine selected but no native libs found! Make sure engine is downloaded.")
                return
            }

            var totalInjected = 0
            nativeLibs.forEach { (abi, soFiles) ->

                if (abiFilters.isNotEmpty() && !abiFilters.contains(abi)) {
                    logger.log("Skipping GeckoView ABI: $abi (not in abiFilters)")
                    return@forEach
                }

                soFiles.forEach { soFile ->
                    val entryPath = "lib/$abi/" + soFile.name
                    logger.log("Injecting: $entryPath (" + (soFile.length() / 1024) + " KB)")
                    writeEntryStoredStreaming(zipOut, entryPath, soFile)
                    totalInjected++
                }
            }

            logger.logKeyValue("geckoNativeLibsInjected", totalInjected)

            val omniJa = engineFileManager.getOmniJaFile(engineType)
            if (omniJa.exists() && omniJa.length() > 0) {
                writeEntryStoredStreaming(zipOut, "assets/omni.ja", omniJa)
                logger.log("Injecting: assets/omni.ja (" + (omniJa.length() / 1024) + " KB)")
                logger.logKeyValue("geckoOmniJaInjected", true)
            } else {

                logger.error("GeckoView omni.ja missing at ${omniJa.absolutePath} — engine not fully downloaded")
                throw IllegalStateException(
                    "GeckoView omni.ja not found. The Firefox engine must be fully downloaded before building."
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to inject GeckoView runtime", e)
            throw e
        }
    }

    private fun addSplashMediaToAssets(
        zipOut: ZipOutputStream,
        mediaPath: String,
        splashType: String,
        encryptor: AssetEncryptor? = null,
        encryptionConfig: EncryptionConfig = EncryptionConfig.DISABLED
    ) {
        AppLogger.d("ApkBuilder", "Preparing to embed splash media: path=$mediaPath, type=$splashType, encrypt=${encryptionConfig.enabled}")

        val mediaFile = File(mediaPath)
        if (!mediaFile.exists()) {
            AppLogger.e("ApkBuilder", "Splash media file does not exist: $mediaPath")
            return
        }

        if (!mediaFile.canRead()) {
            AppLogger.e("ApkBuilder", "Splash media file cannot be read: $mediaPath")
            return
        }

        val fileSize = mediaFile.length()
        if (fileSize == 0L) {
            AppLogger.e("ApkBuilder", "Splash media file is empty: $mediaPath")
            return
        }

        val extension = if (splashType == "VIDEO") "mp4" else "png"
        val assetPath = "splash_media.$extension"
        val isVideo = splashType == "VIDEO"

        try {

            val largeFileThreshold = 10 * 1024 * 1024L

            if (encryptionConfig.enabled && encryptor != null) {

                if (isVideo && fileSize > largeFileThreshold) {
                    AppLogger.d("ApkBuilder", "Splash large video encryption mode: ${fileSize / 1024 / 1024} MB")
                    val encryptedData = encryptLargeFile(mediaFile, assetPath, encryptor)
                    writeEntryDeflated(zipOut, "assets/${assetPath}.enc", encryptedData)
                    AppLogger.d("ApkBuilder", "Splash media encrypted and embedded: assets/${assetPath}.enc (${encryptedData.size} bytes)")
                } else {
                    val mediaBytes = mediaFile.readBytes()
                    val encryptedData = encryptor.encrypt(mediaBytes, assetPath)
                    writeEntryDeflated(zipOut, "assets/${assetPath}.enc", encryptedData)
                    AppLogger.d("ApkBuilder", "Splash media encrypted and embedded: assets/${assetPath}.enc (${encryptedData.size} bytes)")
                }
            } else {

                if (isVideo && fileSize > largeFileThreshold) {

                    AppLogger.d("ApkBuilder", "Splash large video streaming write mode: ${fileSize / 1024 / 1024} MB")
                    writeEntryStoredStreaming(zipOut, "assets/$assetPath", mediaFile)
                } else {

                    val mediaBytes = mediaFile.readBytes()
                    writeEntryStoredSimple(zipOut, "assets/$assetPath", mediaBytes)
                    AppLogger.d("ApkBuilder", "Splash media embedded(STORED): assets/$assetPath (${mediaBytes.size} bytes)")
                }
            }
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to embed splash media: ${e.message}", e)
        }
    }

    private fun addErrorPageMediaToAssets(
        zipOut: ZipOutputStream,
        mediaPath: String,
        encryptor: AssetEncryptor? = null,
        encryptionConfig: EncryptionConfig = EncryptionConfig.DISABLED
    ) {
        AppLogger.d("ApkBuilder", "Preparing to embed error page media: path=$mediaPath, encrypt=${encryptionConfig.enabled}")

        val mediaFile = File(mediaPath)
        if (!mediaFile.exists() || !mediaFile.canRead()) {
            AppLogger.w("ApkBuilder", "Error page media file not accessible: $mediaPath")
            return
        }

        val isVideo = mediaPath.endsWith(".mp4") || mediaPath.endsWith(".webm")
        val extension = if (isVideo) {
            if (mediaPath.endsWith(".webm")) "webm" else "mp4"
        } else {
            "png"
        }
        val assetPath = "error_page_media.$extension"

        try {
            if (encryptionConfig.enabled && encryptor != null) {
                val mediaBytes = mediaFile.readBytes()
                val encryptedData = encryptor.encrypt(mediaBytes, assetPath)
                writeEntryDeflated(zipOut, "assets/${assetPath}.enc", encryptedData)
                AppLogger.d("ApkBuilder", "Error page media encrypted: assets/${assetPath}.enc (${encryptedData.size} bytes)")
            } else {
                val largeFileThreshold = 10 * 1024 * 1024L
                if (isVideo && mediaFile.length() > largeFileThreshold) {
                    writeEntryStoredStreaming(zipOut, "assets/$assetPath", mediaFile)
                } else {
                    val mediaBytes = mediaFile.readBytes()
                    writeEntryStoredSimple(zipOut, "assets/$assetPath", mediaBytes)
                }
                AppLogger.d("ApkBuilder", "Error page media embedded: assets/$assetPath")
            }
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to embed error page media: ${e.message}", e)
        }
    }

    private fun addStatusBarBackgroundToAssets(
        zipOut: ZipOutputStream,
        imagePath: String
    ) {
        AppLogger.d("ApkBuilder", "Preparing to embed status bar background: path=$imagePath")

        val imageFile = File(imagePath)
        if (!imageFile.exists()) {
            AppLogger.e("ApkBuilder", "Status bar background image does not exist: $imagePath")
            return
        }

        if (!imageFile.canRead()) {
            AppLogger.e("ApkBuilder", "Status bar background image cannot be read: $imagePath")
            return
        }

        try {
            val imageBytes = imageFile.readBytes()
            if (imageBytes.isEmpty()) {
                AppLogger.e("ApkBuilder", "Status bar background image is empty: $imagePath")
                return
            }

            writeEntryDeflated(zipOut, "assets/statusbar_background.png", imageBytes)
            AppLogger.d("ApkBuilder", "Status bar background embedded: assets/statusbar_background.png (${imageBytes.size} bytes)")
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to embed status bar background: ${e.message}", e)
        }
    }

    private fun addFloatingWindowMinimizedIconToAssets(
        zipOut: ZipOutputStream,
        imagePath: String
    ) {
        AppLogger.d("ApkBuilder", "Preparing to embed floating window icon: path=$imagePath")

        val imageFile = File(imagePath)
        if (!imageFile.exists()) {
            AppLogger.e("ApkBuilder", "Floating window icon does not exist: $imagePath")
            return
        }

        if (!imageFile.canRead()) {
            AppLogger.e("ApkBuilder", "Floating window icon cannot be read: $imagePath")
            return
        }

        try {
            val imageBytes = imageFile.readBytes()
            if (imageBytes.isEmpty()) {
                AppLogger.e("ApkBuilder", "Floating window icon is empty: $imagePath")
                return
            }

            writeEntryDeflated(zipOut, "assets/$FLOATING_WINDOW_MINIMIZED_ICON_ASSET", imageBytes)
            AppLogger.d("ApkBuilder", "Floating window icon embedded: assets/$FLOATING_WINDOW_MINIMIZED_ICON_ASSET (${imageBytes.size} bytes)")
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to embed floating window icon: ${e.message}", e)
        }
    }

    private fun writeEntryStoredSimple(zipOut: ZipOutputStream, name: String, data: ByteArray) {
        ZipUtils.writeEntryStoredSimple(zipOut, name, data)
    }

    private fun writeEntryStoredStreaming(zipOut: ZipOutputStream, name: String, file: File) {
        ZipUtils.writeEntryStoredStreaming(zipOut, name, file)
    }

    private fun ensureAligned16kNativeLib(sourceFile: File, displayName: String): File {
        val requireAligned =
            displayName == com.webtoapp.core.nodejs.NodeDependencyManager.NODE_BINARY_NAME ||
                displayName == "libnode_bridge.so" ||
                displayName == "libc++_shared.so" ||
                displayName == "libgo_exec_loader.so" ||
                displayName == "libphp.so" ||
                displayName == "libpython3.so" ||
                displayName == "libmusl-linker.so"
        return try {
            val result = ElfAligner16k.ensureAligned(sourceFile, File(tempDir, "elf16k"))
            when {
                result.alreadyAligned -> logger.log("ELF 16KB already aligned: $displayName")
                result.repacked -> logger.log(
                    "ELF 16KB repacked: $displayName (${sourceFile.length() / 1024} KB -> ${result.outputFile.length() / 1024} KB)"
                )
                result.changed -> logger.log("ELF 16KB metadata patched: $displayName")
            }
            result.outputFile
        } catch (e: Exception) {
            if (requireAligned) {
                val msg = "ELF 16KB alignment failed for $displayName: ${e.message}"
                logger.error(msg, e)
                throw IllegalStateException(msg, e)
            }
            logger.warn("ELF 16KB alignment failed for $displayName: ${e.message}; using original binary")
            sourceFile
        }
    }

    private fun addMediaContentToAssets(
        zipOut: ZipOutputStream,
        mediaPath: String,
        isVideo: Boolean,
        encryptor: AssetEncryptor? = null,
        encryptionConfig: EncryptionConfig = EncryptionConfig.DISABLED
    ) {
        AppLogger.d("ApkBuilder", "Preparing to embed media content: path=$mediaPath, isVideo=$isVideo, encrypt=${encryptionConfig.enabled}")

        val mediaFile = File(mediaPath)
        if (!mediaFile.exists()) {
            AppLogger.e("ApkBuilder", "Media file does not exist: $mediaPath")
            return
        }

        if (!mediaFile.canRead()) {
            AppLogger.e("ApkBuilder", "Media file cannot be read: $mediaPath")
            return
        }

        val fileSize = mediaFile.length()
        if (fileSize == 0L) {
            AppLogger.e("ApkBuilder", "Media file is empty: $mediaPath")
            return
        }

        val extension = if (isVideo) "mp4" else "png"
        val assetName = "media_content.$extension"

        try {

            val largeFileThreshold = 10 * 1024 * 1024L

            if (encryptionConfig.enabled && encryptor != null) {

                if (fileSize > largeFileThreshold) {
                    AppLogger.d("ApkBuilder", "Large file encryption mode: ${fileSize / 1024 / 1024} MB")

                    val encryptedData = encryptLargeFile(mediaFile, assetName, encryptor)
                    writeEntryDeflated(zipOut, "assets/${assetName}.enc", encryptedData)
                    AppLogger.d("ApkBuilder", "Media content encrypted and embedded: assets/${assetName}.enc (${encryptedData.size} bytes)")
                } else {
                    val mediaBytes = mediaFile.readBytes()
                    val encryptedData = encryptor.encrypt(mediaBytes, assetName)
                    writeEntryDeflated(zipOut, "assets/${assetName}.enc", encryptedData)
                    AppLogger.d("ApkBuilder", "Media content encrypted and embedded: assets/${assetName}.enc (${encryptedData.size} bytes)")
                }
            } else {

                if (fileSize > largeFileThreshold) {

                    AppLogger.d("ApkBuilder", "Large file streaming write mode: ${fileSize / 1024 / 1024} MB")
                    writeEntryStoredStreaming(zipOut, "assets/$assetName", mediaFile)
                } else {

                    val mediaBytes = mediaFile.readBytes()
                    writeEntryStoredSimple(zipOut, "assets/$assetName", mediaBytes)
                    AppLogger.d("ApkBuilder", "Media content embedded(STORED): assets/$assetName (${mediaBytes.size} bytes)")
                }
            }
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to embed media content", e)
        }
    }

    private fun addGalleryItemsToAssets(
        zipOut: ZipOutputStream,
        galleryItems: List<com.webtoapp.data.model.GalleryItem>,
        encryptor: AssetEncryptor? = null,
        encryptionConfig: EncryptionConfig = EncryptionConfig.DISABLED
    ) {
        AppLogger.d("ApkBuilder", "Preparing to embed ${galleryItems.size} gallery items, encrypt=${encryptionConfig.enabled}")

        galleryItems.forEachIndexed { index, item ->
            try {
                val mediaFile = File(item.path)
                if (!mediaFile.exists()) {
                    AppLogger.w("ApkBuilder", "Gallery item file not found: ${item.path}")
                    return@forEachIndexed
                }
                if (!mediaFile.canRead()) {
                    AppLogger.w("ApkBuilder", "Gallery item file cannot be read: ${item.path}")
                    return@forEachIndexed
                }

                val ext = if (item.type == com.webtoapp.data.model.GalleryItemType.VIDEO) "mp4" else "png"
                val assetName = "gallery/item_$index.$ext"
                val isVideo = item.type == com.webtoapp.data.model.GalleryItemType.VIDEO
                val fileSize = mediaFile.length()
                val largeFileThreshold = 10 * 1024 * 1024L

                if (encryptionConfig.enabled && encryptor != null) {
                    if (isVideo && fileSize > largeFileThreshold) {
                        val encryptedData = encryptLargeFile(mediaFile, assetName, encryptor)
                        writeEntryDeflated(zipOut, "assets/${assetName}.enc", encryptedData)
                    } else {
                        val data = mediaFile.readBytes()
                        val encrypted = encryptor.encrypt(data, assetName)
                        writeEntryDeflated(zipOut, "assets/${assetName}.enc", encrypted)
                    }
                    AppLogger.d("ApkBuilder", "Gallery item encrypted and embedded: assets/${assetName}.enc")
                } else {
                    if (isVideo && fileSize > largeFileThreshold) {
                        writeEntryStoredStreaming(zipOut, "assets/$assetName", mediaFile)
                    } else {
                        writeEntryStoredSimple(zipOut, "assets/$assetName", mediaFile.readBytes())
                    }
                    AppLogger.d("ApkBuilder", "Gallery item embedded(STORED): assets/$assetName (${fileSize / 1024} KB)")
                }

                item.thumbnailPath?.let { thumbPath ->
                    val thumbFile = File(thumbPath)
                    if (thumbFile.exists() && thumbFile.canRead()) {
                        val thumbAssetName = "gallery/thumb_$index.jpg"
                        val thumbBytes = thumbFile.readBytes()
                        if (encryptionConfig.enabled && encryptor != null) {
                            val encryptedThumb = encryptor.encrypt(thumbBytes, thumbAssetName)
                            writeEntryDeflated(zipOut, "assets/${thumbAssetName}.enc", encryptedThumb)
                        } else {
                            writeEntryDeflated(zipOut, "assets/$thumbAssetName", thumbBytes)
                        }
                        AppLogger.d("ApkBuilder", "Gallery thumbnail embedded: assets/$thumbAssetName")
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("ApkBuilder", "Failed to embed gallery item ${item.path}", e)
            }
        }
    }

    private fun addWordPressFilesToAssets(
        zipOut: ZipOutputStream,
        projectDir: File
    ) {
        AppLogger.d("ApkBuilder", "Embedding WordPress files from: ${projectDir.absolutePath}")

        var fileCount = 0
        var totalSize = 0L

        fun addDirRecursive(dir: File, basePath: String) {
            dir.listFiles()?.forEach { file ->
                val relativePath = "$basePath/${file.name}"
                if (file.isDirectory) {
                    addDirRecursive(file, relativePath)
                } else {
                    try {
                        val assetPath = "assets/wordpress$relativePath"

                        if (isTextFile(file.name)) {
                            writeEntryDeflated(zipOut, assetPath, file.readBytes())
                        } else {
                            writeEntryStoredSimple(zipOut, assetPath, file.readBytes())
                        }
                        fileCount++
                        totalSize += file.length()
                    } catch (e: Exception) {
                        AppLogger.w("ApkBuilder", "Failed to embed WordPress file: ${file.absolutePath}", e)
                    }
                }
            }
        }
        addDirRecursive(projectDir, "")
        logger.logKeyValue("wordpressFilesEmbedded", fileCount)
        logger.logKeyValue("wordpressTotalSize", "${totalSize / 1024} KB")

        val phpBinary = resolvePhpBinary()
        if (phpBinary != null && phpBinary.canRead()) {
            try {
                val abi = com.webtoapp.core.wordpress.WordPressDependencyManager.getDeviceAbi()
                val alignedPhpBinary = ensureAligned16kNativeLib(phpBinary, "libphp.so")

                writeEntryStoredStreaming(zipOut, "lib/$abi/libphp.so", alignedPhpBinary)
                logger.log("PHP binary injected as native lib: lib/$abi/libphp.so (${alignedPhpBinary.length() / 1024} KB)")

            } catch (e: Exception) {
                logger.error("Failed to embed PHP binary", e)
            }
        } else {
            logger.warn("PHP binary not found")
        }
    }

    private fun addNodeJsFilesToAssets(
        zipOut: ZipOutputStream,
        projectDir: File
    ) {

        RuntimeAssetEmbedder.embedProjectFiles(zipOut, projectDir, RuntimeAssetEmbedder.nodeJsConfig(), logger)
        injectNodeJsNativeLibs(zipOut)
    }

    private fun resolveNodeJsBinary(): File? {
        val nativeNode = File(
            context.applicationInfo.nativeLibraryDir,
            com.webtoapp.core.nodejs.NodeDependencyManager.NODE_BINARY_NAME
        )
        if (nativeNode.exists() && nativeNode.canRead() && nativeNode.length() > 0L) {
            AppLogger.d("ApkBuilder", "Using nativeLibraryDir Node: ${nativeNode.absolutePath}")
            return nativeNode
        }
        val downloaded = File(
            com.webtoapp.core.nodejs.NodeDependencyManager.getNodeDir(context),
            com.webtoapp.core.nodejs.NodeDependencyManager.NODE_BINARY_NAME
        )
        if (downloaded.exists() && downloaded.canRead() && downloaded.length() > 0L) {
            AppLogger.d("ApkBuilder", "Using downloaded Node: ${downloaded.absolutePath}")
            return downloaded
        }
        return null
    }

    private fun injectNodeJsNativeLibs(zipOut: ZipOutputStream) {
        val abi = com.webtoapp.core.nodejs.NodeDependencyManager.getDeviceAbi()
        val nativeDir = File(context.applicationInfo.nativeLibraryDir)
        val bridge = File(nativeDir, "libnode_bridge.so")
        if (!bridge.exists() || !bridge.canRead() || bridge.length() <= 0L) {
            val msg =
                "Node bridge missing at ${bridge.absolutePath}. Rebuild the host app with native node_bridge, then re-export the NODEJS_APP."
            logger.error(msg)
            throw IllegalStateException(msg)
        }
        val cxxShared = File(nativeDir, "libc++_shared.so")
        if (!cxxShared.exists() || !cxxShared.canRead() || cxxShared.length() <= 0L) {
            val msg =
                "libc++_shared.so missing at ${cxxShared.absolutePath}. libnode_bridge.so and libnode.so require it; rebuild the host app, then re-export the NODEJS_APP."
            logger.error(msg)
            throw IllegalStateException(msg)
        }
        val nodeBinary = resolveNodeJsBinary()
        if (nodeBinary == null) {
            val cachePath = File(
                com.webtoapp.core.nodejs.NodeDependencyManager.getNodeDir(context),
                com.webtoapp.core.nodejs.NodeDependencyManager.NODE_BINARY_NAME
            ).absolutePath
            val msg =
                "libnode.so missing from host nativeLibraryDir and download cache ($cachePath). Download Node.js runtime in Settings → Runtime Engines, then re-export."
            logger.error(msg)
            throw IllegalStateException(msg)
        }
        try {
            val alignedCxx = ensureAligned16kNativeLib(cxxShared, "libc++_shared.so")
            writeEntryStoredStreaming(zipOut, "lib/$abi/libc++_shared.so", alignedCxx)
            logger.log(
                "C++ shared runtime embedded as native lib: lib/$abi/libc++_shared.so (${alignedCxx.length() / 1024} KB)"
            )

            val alignedBridge = ensureAligned16kNativeLib(bridge, "libnode_bridge.so")
            writeEntryStoredStreaming(zipOut, "lib/$abi/libnode_bridge.so", alignedBridge)
            logger.log(
                "Node bridge embedded as native lib: lib/$abi/libnode_bridge.so (${alignedBridge.length() / 1024} KB)"
            )

            val alignedNode = ensureAligned16kNativeLib(
                nodeBinary,
                com.webtoapp.core.nodejs.NodeDependencyManager.NODE_BINARY_NAME
            )
            writeEntryStoredStreaming(
                zipOut,
                "lib/$abi/${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_BINARY_NAME}",
                alignedNode
            )
            logger.log(
                "Node.js binary embedded as native lib: lib/$abi/${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_BINARY_NAME} (${alignedNode.length() / 1024} KB)"
            )
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to embed Node.js native libs", e)
            throw IllegalStateException("Failed to embed Node.js native libs: ${e.message}", e)
        }
    }

    private fun addPhpAppFilesToAssets(
        zipOut: ZipOutputStream,
        projectDir: File
    ) {

        RuntimeAssetEmbedder.embedProjectFiles(zipOut, projectDir, RuntimeAssetEmbedder.phpConfig(), logger)

        val phpBinary = resolvePhpBinary()
        if (phpBinary != null && phpBinary.canRead()) {
            try {
                val abi = com.webtoapp.core.wordpress.WordPressDependencyManager.getDeviceAbi()
                val alignedPhpBinary = ensureAligned16kNativeLib(phpBinary, "libphp.so")

                writeEntryStoredStreaming(zipOut, "lib/$abi/libphp.so", alignedPhpBinary)
                logger.log("PHP binary injected as native lib: lib/$abi/libphp.so (${alignedPhpBinary.length() / 1024} KB)")

            } catch (e: Exception) {
                logger.error("Failed to embed PHP binary for PHP app", e)
            }
        } else {
            logger.warn("PHP binary not found")
        }
    }

    private fun resolvePhpBinary(): File? {

        val nativePhp = File(context.applicationInfo.nativeLibraryDir, "libphp.so")
        if (nativePhp.exists() && nativePhp.canExecute()) {
            AppLogger.d("ApkBuilder", "Using nativeLibraryDir PHP: ${nativePhp.absolutePath}")
            return nativePhp
        }

        val downloaded = com.webtoapp.core.wordpress.WordPressDependencyManager
            .getPhpExecutablePath(context)
            ?.let { File(it) }
        if (downloaded != null && downloaded.exists() && downloaded.canRead()) {
            AppLogger.d("ApkBuilder", "Using downloaded PHP: ${downloaded.absolutePath}")
            return downloaded
        }
        AppLogger.w(
            "ApkBuilder",
            "PHP binary missing in nativeLibraryDir and wordpress_deps; " +
                "user should install PHP via 运行时管理 first"
        )
        return null
    }

    private fun addPythonAppFilesToAssets(
        zipOut: ZipOutputStream,
        projectDir: File
    ) {

        val reqFile = File(projectDir, "requirements.txt")
        val sitePackages = File(projectDir, ".pypackages")
        if (reqFile.exists() && !com.webtoapp.core.python.PythonDependencyManager.hasInstalledPackages(sitePackages)) {
            val pythonBin = com.webtoapp.core.python.PythonDependencyManager.getPythonExecutablePath(context)
            val muslLinker = com.webtoapp.core.python.PythonDependencyManager.getMuslLinkerPath(context)
            val pythonBinaryReady = File(pythonBin).exists()
            if (pythonBinaryReady && !muslLinker.isNullOrBlank()) {
                logger.log("Pre-installing Python dependencies for APK bundling...")
                try {
                    val installed = kotlinx.coroutines.runBlocking {
                        com.webtoapp.core.python.PythonDependencyManager.installRequirements(context, projectDir) { line ->
                            AppLogger.d("ApkBuilder", "[pip-preinstall] $line")
                        }
                    }
                    if (!installed || !com.webtoapp.core.python.PythonDependencyManager.hasInstalledPackages(sitePackages)) {
                        throw IllegalStateException(
                            "Python requirements could not be pre-bundled into .pypackages. " +
                                "Exporting this APK would require runtime pip install on device."
                        )
                    }
                    val pkgCount = sitePackages.listFiles()?.size ?: 0
                    logger.log("Python dependencies pre-installed: $pkgCount packages in .pypackages")
                } catch (e: Exception) {
                    throw IllegalStateException("Python dependency pre-install failed: ${e.message}", e)
                }
            } else {
                throw IllegalStateException(
                    "Python dependency pre-install unavailable: runtime binary or musl linker is missing locally. " +
                        "Download Python runtime first, then re-export."
                )
            }
        } else if (com.webtoapp.core.python.PythonDependencyManager.hasInstalledPackages(sitePackages)) {
            logger.log("Python .pypackages already exists (${sitePackages.listFiles()?.size ?: 0} packages), skipping pre-install")
        }

        RuntimeAssetEmbedder.embedProjectFiles(zipOut, projectDir, RuntimeAssetEmbedder.pythonConfig(), logger)

        val sitecustomizeContent = """
import os, sys, builtins

# === 1. Patch importlib.metadata for --target installed packages ===
try:
    import importlib.metadata
    _orig_version = importlib.metadata.version
    def _patched_version(name):
        try:
            return _orig_version(name)
        except importlib.metadata.PackageNotFoundError:
            try:
                mod = __import__(name.replace('-', '_'))
                version_value = getattr(mod, '__dict__', {}).get('__version__')
                if isinstance(version_value, str) and version_value:
                    return version_value
            except (ImportError, Exception):
                pass
            return "0.0.0"
    importlib.metadata.version = _patched_version
except Exception:
    pass

# === 2. Patch Flask to use PORT env var ===
_w2a_port = int(os.environ.get('PORT', '5000'))
_orig_builtins_import = builtins.__import__
_flask_patched = False

def _w2a_import(name, *args, **kwargs):
    global _flask_patched
    result = _orig_builtins_import(name, *args, **kwargs)
    if name == 'flask' and not _flask_patched:
        _flask_patched = True
        try:
            _orig_run = result.Flask.run
            def _new_run(self, host=None, port=None, **kw):
                kw.pop('debug', None)
                _orig_run(self, host='127.0.0.1', port=_w2a_port, debug=False, **kw)
            result.Flask.run = _new_run
        except Exception:
            pass
    return result

builtins.__import__ = _w2a_import
""".trimIndent()
        try {
            ZipUtils.writeEntryDeflated(zipOut, "assets/python_app/sitecustomize.py", sitecustomizeContent.toByteArray())
            logger.log("Embedded sitecustomize.py for Android runtime fixes (metadata + port)")
        } catch (e: Exception) {
            logger.warn("Failed to embed sitecustomize.py: ${e.message}")
        }

        val pythonHome = com.webtoapp.core.python.PythonDependencyManager.getPythonDir(context)
        val versionedPythonBinaryName = com.webtoapp.core.python.PythonDependencyManager.getVersionedPythonBinaryName()
        var pythonBinaryVersioned = File(pythonHome, "bin/$versionedPythonBinaryName")
        var pythonBinary3 = File(pythonHome, "bin/python3")
        var pythonBinary = when {
            pythonBinaryVersioned.exists() && pythonBinaryVersioned.length() > 1024 * 1024 -> pythonBinaryVersioned
            pythonBinary3.exists() && pythonBinary3.length() > 1024 * 1024 -> pythonBinary3
            else -> null
        }

        if (pythonBinary == null) {
            logger.warn("Python binary not found locally, attempting auto-download...")
            try {
                val downloadSuccess = kotlinx.coroutines.runBlocking {
                    com.webtoapp.core.python.PythonDependencyManager.downloadPythonRuntime(context)
                }
                if (downloadSuccess) {
                    logger.log("Python runtime downloaded successfully")

                    pythonBinaryVersioned = File(pythonHome, "bin/$versionedPythonBinaryName")
                    pythonBinary3 = File(pythonHome, "bin/python3")
                    pythonBinary = when {
                        pythonBinaryVersioned.exists() && pythonBinaryVersioned.length() > 1024 * 1024 -> pythonBinaryVersioned
                        pythonBinary3.exists() && pythonBinary3.length() > 1024 * 1024 -> pythonBinary3
                        else -> null
                    }
                } else {
                    logger.error("Python runtime download failed - exported APK will not have Python interpreter!")
                }
            } catch (e: Exception) {
                logger.error("Failed to auto-download Python runtime: ${e.message}", e)
            }
        }

        val abi = com.webtoapp.core.wordpress.WordPressDependencyManager.getDeviceAbi()
        if (pythonBinary != null && pythonBinary.canRead()) {
            try {

                val alignedPythonBinary = ensureAligned16kNativeLib(pythonBinary, "libpython3.so")
                writeEntryStoredStreaming(zipOut, "lib/$abi/libpython3.so", alignedPythonBinary)
                logger.log("Python binary embedded as native lib: lib/$abi/libpython3.so (${alignedPythonBinary.length() / 1024} KB, src=${pythonBinary.name})")

                writeEntryStoredSimple(zipOut, "assets/python/$abi/python3", alignedPythonBinary.readBytes())
            } catch (e: Exception) {
                logger.error("Failed to embed Python binary", e)
            }
        } else {
            logger.error("⚠️ CRITICAL: Python binary not available! The exported APK will NOT be able to run Python apps. Please ensure Python runtime is downloaded in WebToApp settings.")
            logger.warn("Python binary not found or too small: ${versionedPythonBinaryName}=${pythonBinaryVersioned.let { "${it.exists()}/${it.length()}" }}, python3=${pythonBinary3.let { "${it.exists()}/${it.length()}" }}")
        }

        val muslLinkerName = com.webtoapp.core.python.PythonDependencyManager.getMuslLinkerName(abi)
        val muslLinkerFile = File(pythonHome, "lib/$muslLinkerName")
        if (muslLinkerFile.exists() && muslLinkerFile.canRead()) {
            try {
                val alignedMuslLinkerFile = ensureAligned16kNativeLib(muslLinkerFile, "libmusl-linker.so")
                writeEntryStoredStreaming(zipOut, "lib/$abi/libmusl-linker.so", alignedMuslLinkerFile)
                logger.log("musl linker embedded as native lib: lib/$abi/libmusl-linker.so (${alignedMuslLinkerFile.length() / 1024} KB)")
            } catch (e: Exception) {
                logger.error("Failed to embed musl linker", e)
            }
        } else {
            logger.warn("musl linker not found: ${muslLinkerFile.absolutePath} - Python may not execute in exported APK")
        }

        val pythonLibDir = File(pythonHome, "lib")
        RuntimeAssetEmbedder.embedPythonStdlib(zipOut, pythonLibDir, logger)
    }

    private fun addGoAppFilesToAssets(
        zipOut: ZipOutputStream,
        projectDir: File
    ) {
        RuntimeAssetEmbedder.embedProjectFiles(zipOut, projectDir, RuntimeAssetEmbedder.goConfig(), logger)
        injectGoExecLoaderNativeLib(zipOut)
    }

    private fun injectGoExecLoaderNativeLib(zipOut: ZipOutputStream) {
        val loader = File(context.applicationInfo.nativeLibraryDir, "libgo_exec_loader.so")
        if (!loader.exists() || !loader.canRead()) {
            val msg =
                "Go exec loader missing at ${loader.absolutePath}. Rebuild the host app with native go_exec_loader, then re-export the GO_APP."
            logger.error(msg)
            throw IllegalStateException(msg)
        }
        try {
            val abi = com.webtoapp.core.golang.GoDependencyManager.getDeviceAbi()
            val aligned = ensureAligned16kNativeLib(loader, "libgo_exec_loader.so")
            writeEntryStoredStreaming(zipOut, "lib/$abi/libgo_exec_loader.so", aligned)
            logger.log(
                "Go exec loader embedded as native lib: lib/$abi/libgo_exec_loader.so (${aligned.length() / 1024} KB)"
            )
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to embed Go exec loader", e)
            throw IllegalStateException("Failed to embed Go exec loader: ${e.message}", e)
        }
    }

    private fun ensureGoProjectBinaryForExport(
        projectDir: File,
        config: ApkConfig,
        onProgress: ((Int, String) -> Unit)? = null
    ) {
        val desiredBinaryName = config.goAppBinaryName.ifBlank { projectDir.name }
        val hasCompatibleBinary = com.webtoapp.core.golang.GoDependencyManager.findBinaryPath(projectDir, desiredBinaryName) != null ||
            com.webtoapp.core.golang.GoDependencyManager.detectAnyCompatibleBinary(projectDir) != null

        if (hasCompatibleBinary) {
            logger.log("Go binary already exists for export")
            return
        }

        throw IllegalStateException(
            "Go project has no runnable binary for export. WebToApp no longer compiles Go source during APK build. Build the binary first and retry export."
        )
    }

    private fun addFrontendFilesToAssets(
        zipOut: ZipOutputStream,
        projectDir: File,
        htmlFiles: List<com.webtoapp.data.model.HtmlFile>
    ) {
        RuntimeAssetEmbedder.embedProjectFiles(zipOut, projectDir, RuntimeAssetEmbedder.frontendConfig(), logger)
    }

    private fun encryptLargeFile(file: File, assetName: String, encryptor: AssetEncryptor): ByteArray {
        val fileSize = file.length()
        val maxEncryptSize = 100L * 1024 * 1024
        if (fileSize > maxEncryptSize) {
            AppLogger.w("ApkBuilder", "WARNING: Encrypting very large file ($assetName, ${fileSize / 1024 / 1024}MB). " +
                "May cause high memory usage. Consider disabling encryption for large media files.")
        }
        val mediaBytes = file.readBytes()
        return encryptor.encrypt(mediaBytes, assetName)
    }

    private fun isTextFile(fileName: String): Boolean {
        return TextFileClassifier.isTextFile(fileName)
    }

    private fun addBgmToAssets(
        zipOut: ZipOutputStream,
        bgmPaths: List<String>,
        lrcDataList: List<LrcData?>,
        coverPaths: List<String?> = emptyList(),
        encryptor: AssetEncryptor? = null,
        encryptionConfig: EncryptionConfig = EncryptionConfig.DISABLED
    ) {
        AppLogger.d("ApkBuilder", "Preparing to embed ${bgmPaths.size} BGM files, encrypt=${encryptionConfig.enabled}")

        bgmPaths.forEachIndexed { index, bgmPath ->
            try {
                val assetName = "bgm/bgm_$index.mp3"
                var bgmBytes: ByteArray? = null

                val bgmFile = File(bgmPath)
                if (!bgmFile.exists()) {
                    if (bgmPath.startsWith("asset:///")) {
                        val assetPath = bgmPath.removePrefix("asset:///")
                        bgmBytes = context.assets.open(assetPath).use { it.readBytes() }
                    } else {
                        AppLogger.e("ApkBuilder", "BGM file does not exist: $bgmPath")
                        return@forEachIndexed
                    }
                } else {
                    if (!bgmFile.canRead()) {
                        AppLogger.e("ApkBuilder", "BGM file cannot be read: $bgmPath")
                        return@forEachIndexed
                    }

                    bgmBytes = bgmFile.readBytes()
                    if (bgmBytes.isEmpty()) {
                        AppLogger.e("ApkBuilder", "BGM file is empty: $bgmPath")
                        return@forEachIndexed
                    }
                }

                if (bgmBytes != null) {
                    if (encryptionConfig.enabled && encryptor != null) {
                        val encryptedData = encryptor.encrypt(bgmBytes, assetName)
                        writeEntryDeflated(zipOut, "assets/${assetName}.enc", encryptedData)
                        AppLogger.d("ApkBuilder", "BGM encrypted and embedded: assets/${assetName}.enc (${encryptedData.size} bytes)")
                    } else {
                        writeEntryStoredSimple(zipOut, "assets/$assetName", bgmBytes)
                        AppLogger.d("ApkBuilder", "BGM embedded(STORED): assets/$assetName (${bgmBytes.size} bytes)")
                    }
                }

                val lrcData = lrcDataList.getOrNull(index)
                if (lrcData != null && lrcData.lines.isNotEmpty()) {
                    val lrcContent = convertLrcDataToLrcString(lrcData)
                    val lrcAssetName = "bgm/bgm_$index.lrc"
                    val lrcBytes = lrcContent.toByteArray(Charsets.UTF_8)

                    if (encryptionConfig.enabled && encryptor != null) {
                        val encryptedLrc = encryptor.encrypt(lrcBytes, lrcAssetName)
                        writeEntryDeflated(zipOut, "assets/${lrcAssetName}.enc", encryptedLrc)
                        AppLogger.d("ApkBuilder", "LRC encrypted and embedded: assets/${lrcAssetName}.enc")
                    } else {
                        writeEntryDeflated(zipOut, "assets/$lrcAssetName", lrcBytes)
                        AppLogger.d("ApkBuilder", "LRC embedded: assets/$lrcAssetName")
                    }
                }

                val coverPath = coverPaths.getOrNull(index)
                val coverAssetName = resolveBgmCoverAssetName(index, coverPath)
                if (coverAssetName != null && !coverPath.isNullOrBlank()) {
                    val coverBytes = loadBgmCoverBytes(coverPath)
                    if (coverBytes != null && coverBytes.isNotEmpty()) {
                        if (encryptionConfig.enabled && encryptor != null) {
                            val encryptedCover = encryptor.encrypt(coverBytes, coverAssetName)
                            writeEntryDeflated(zipOut, "assets/${coverAssetName}.enc", encryptedCover)
                            AppLogger.d("ApkBuilder", "BGM cover encrypted and embedded: assets/${coverAssetName}.enc")
                        } else {
                            writeEntryStoredSimple(zipOut, "assets/$coverAssetName", coverBytes)
                            AppLogger.d("ApkBuilder", "BGM cover embedded: assets/$coverAssetName (${coverBytes.size} bytes)")
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("ApkBuilder", "Failed to embed BGM: $bgmPath", e)
            }
        }
    }

    private fun resolveBgmCoverAssetName(index: Int, coverPath: String?): String? {
        if (coverPath.isNullOrBlank()) return null
        val rawExt = when {
            coverPath.startsWith("asset:///") -> coverPath.substringAfterLast('.', "jpg")
            else -> File(coverPath).extension
        }.lowercase().ifBlank { "jpg" }
        val ext = when (rawExt) {
            "jpeg", "jpe", "jfif" -> "jpg"
            "png", "jpg", "webp", "gif", "bmp", "heic", "heif" -> rawExt
            else -> "jpg"
        }
        return "bgm/bgm_$index.$ext"
    }

    private fun loadBgmCoverBytes(coverPath: String): ByteArray? {
        return try {
            if (coverPath.startsWith("asset:///")) {
                val assetPath = coverPath.removePrefix("asset:///")
                context.assets.open(assetPath).use { it.readBytes() }
            } else {
                val file = File(coverPath)
                if (!file.exists() || !file.canRead() || file.length() == 0L) null
                else file.readBytes()
            }
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to load BGM cover: $coverPath", e)
            null
        }
    }


    private fun addHtmlFilesToAssets(
        zipOut: ZipOutputStream,
        htmlFiles: List<com.webtoapp.data.model.HtmlFile>,
        encryptor: AssetEncryptor? = null,
        encryptionConfig: EncryptionConfig = EncryptionConfig.DISABLED
    ): Int {
        AppLogger.d("ApkBuilder", "Preparing to embed ${htmlFiles.size} HTML project files")

        htmlFiles.forEachIndexed { index, file ->
            AppLogger.d("ApkBuilder", "  [$index] name=${file.name}, path=${file.path}, type=${file.type}")
        }

        val htmlFilesList = htmlFiles.filter {
            it.type == com.webtoapp.data.model.HtmlFileType.HTML ||
            it.name.endsWith(".html", ignoreCase = true) ||
            it.name.endsWith(".htm", ignoreCase = true)
        }
        val cssFilesList = htmlFiles.filter {
            it.type == com.webtoapp.data.model.HtmlFileType.CSS ||
            it.name.endsWith(".css", ignoreCase = true)
        }
        val jsFilesList = htmlFiles.filter {
            it.type == com.webtoapp.data.model.HtmlFileType.JS ||
            it.name.endsWith(".js", ignoreCase = true) ||
            it.name.endsWith(".mjs", ignoreCase = true)
        }
        val otherFiles = htmlFiles.filter { file ->
            file !in htmlFilesList && file !in cssFilesList && file !in jsFilesList
        }

        AppLogger.d("ApkBuilder", "File categories: HTML=${htmlFilesList.size}, CSS=${cssFilesList.size}, JS=${jsFilesList.size}, Other=${otherFiles.size}")

        val isComplexProject = isComplexHtmlProject(htmlFiles, htmlFilesList, jsFilesList, otherFiles)

        if (isComplexProject) {
            AppLogger.i("ApkBuilder", "Complex project detected (React/WASM/ES modules) — using PRESERVE mode (no inlining)")
            return addHtmlFilesPreserveStructure(zipOut, htmlFiles, encryptor, encryptionConfig)
        }

        AppLogger.d("ApkBuilder", "Simple project — using INLINE mode")

        var successCount = 0

        val cssContent = cssFilesList.mapNotNull { cssFile ->
            try {
                val file = File(cssFile.path)
                if (file.exists() && file.canRead()) {
                    val encoding = detectFileEncoding(file)
                    com.webtoapp.util.HtmlProjectProcessor.readFileWithEncoding(file, encoding)
                } else null
            } catch (e: Exception) {
                AppLogger.e("ApkBuilder", "Failed to read CSS file: ${cssFile.path}", e)
                null
            }
        }.joinToString("\n\n")

        val jsContent = jsFilesList.mapNotNull { jsFile ->
            try {
                val file = File(jsFile.path)
                if (file.exists() && file.canRead()) {
                    val encoding = detectFileEncoding(file)
                    com.webtoapp.util.HtmlProjectProcessor.readFileWithEncoding(file, encoding)
                } else null
            } catch (e: Exception) {
                AppLogger.e("ApkBuilder", "Failed to read JS file: ${jsFile.path}", e)
                null
            }
        }.joinToString("\n\n")

        AppLogger.d("ApkBuilder", "CSS content length: ${cssContent.length}, JS content length: ${jsContent.length}")

        htmlFilesList.forEach { htmlFile ->
            try {
                val sourceFile = File(htmlFile.path)
                AppLogger.d("ApkBuilder", "Processing HTML file: ${htmlFile.path}")

                if (!sourceFile.exists()) {
                    AppLogger.e("ApkBuilder", "HTML file does not exist: ${htmlFile.path}")
                    return@forEach
                }

                if (!sourceFile.canRead()) {
                    AppLogger.e("ApkBuilder", "HTML file cannot be read: ${htmlFile.path}")
                    return@forEach
                }

                val encoding = detectFileEncoding(sourceFile)
                var htmlContent = com.webtoapp.util.HtmlProjectProcessor.readFileWithEncoding(sourceFile, encoding)

                if (htmlContent.isEmpty()) {
                    AppLogger.w("ApkBuilder", "HTML file content is empty: ${htmlFile.path}")
                    return@forEach
                }

                htmlContent = com.webtoapp.util.HtmlProjectProcessor.processHtmlContent(
                    htmlContent = htmlContent,
                    cssContent = cssContent.takeIf { it.isNotBlank() },
                    jsContent = jsContent.takeIf { it.isNotBlank() },
                    fixPaths = true
                )

                val assetPath = "assets/html/${htmlFile.name}"
                val htmlBytes = htmlContent.toByteArray(Charsets.UTF_8)

                if (encryptionConfig.enabled && encryptor != null) {

                    val encryptedData = encryptor.encrypt(htmlBytes, "html/${htmlFile.name}")
                    writeEntryDeflated(zipOut, "${assetPath}.enc", encryptedData)
                    AppLogger.d("ApkBuilder", "HTML file encrypted and embedded: ${assetPath}.enc (${encryptedData.size} bytes)")
                } else {
                    writeEntryDeflated(zipOut, assetPath, htmlBytes)
                    AppLogger.d("ApkBuilder", "HTML file embedded(inline CSS/JS): $assetPath (${htmlContent.length} bytes)")
                }
                successCount++
            } catch (e: Exception) {
                AppLogger.e("ApkBuilder", "Failed to embed HTML file: ${htmlFile.path}", e)
            }
        }

        otherFiles.forEach { otherFile ->
            try {
                val sourceFile = File(otherFile.path)
                if (sourceFile.exists() && sourceFile.canRead()) {
                    val fileBytes = sourceFile.readBytes()
                    if (fileBytes.isNotEmpty()) {
                        val assetPath = "assets/html/${otherFile.name}"
                        val assetName = "html/${otherFile.name}"

                        if (encryptionConfig.enabled && encryptor != null) {
                            val encryptedData = encryptor.encrypt(fileBytes, assetName)
                            writeEntryDeflated(zipOut, "${assetPath}.enc", encryptedData)
                            AppLogger.d("ApkBuilder", "Other file encrypted and embedded: ${assetPath}.enc (${encryptedData.size} bytes)")
                        } else {
                            writeEntryDeflated(zipOut, assetPath, fileBytes)
                            AppLogger.d("ApkBuilder", "Other file embedded: $assetPath (${fileBytes.size} bytes)")
                        }
                        successCount++
                    }
                }
            } catch (e: Exception) {
                AppLogger.e("ApkBuilder", "Failed to embed other file: ${otherFile.path}", e)
            }
        }

        AppLogger.d("ApkBuilder", "HTML files embedding complete: $successCount/${htmlFiles.size} successful")
        return successCount
    }

    private fun isComplexHtmlProject(
        allFiles: List<com.webtoapp.data.model.HtmlFile>,
        htmlFiles: List<com.webtoapp.data.model.HtmlFile>,
        jsFiles: List<com.webtoapp.data.model.HtmlFile>,
        @Suppress("UNUSED_PARAMETER") otherFiles: List<com.webtoapp.data.model.HtmlFile>
    ): Boolean {

        val hasWasm = allFiles.any { it.name.endsWith(".wasm", ignoreCase = true) }
        if (hasWasm) {
            AppLogger.d("ApkBuilder", "Complex project indicator: WASM files detected")
            return true
        }

        if (jsFiles.size > 3) {
            AppLogger.d("ApkBuilder", "Complex project indicator: ${jsFiles.size} JS files (>3)")
            return true
        }

        val chunkPattern = Regex("""(chunk|vendor|main|runtime|polyfill)[.\-][a-f0-9]{6,}\.js""", RegexOption.IGNORE_CASE)
        val hasChunkedJs = jsFiles.any { chunkPattern.containsMatchIn(it.name) }
        if (hasChunkedJs) {
            AppLogger.d("ApkBuilder", "Complex project indicator: chunked JS filenames detected")
            return true
        }

        val htmlUsesModules = htmlFiles.any { htmlFile ->
            try {
                val file = File(htmlFile.path)
                if (file.exists() && file.length() < 1024 * 1024) {
                    val content = file.readText(Charsets.UTF_8)
                    content.contains("type=\"module\"", ignoreCase = true) ||
                    content.contains("type='module'", ignoreCase = true)
                } else false
            } catch (_: Exception) { false }
        }
        if (htmlUsesModules) {
            AppLogger.d("ApkBuilder", "Complex project indicator: ES module (type=\"module\") detected in HTML")
            return true
        }

        val hasSourceMaps = allFiles.any { it.name.endsWith(".map", ignoreCase = true) }

        val hasManifest = allFiles.any {
            it.name.equals("asset-manifest.json", ignoreCase = true) ||
            it.name.equals("manifest.json", ignoreCase = true) ||
            it.name.equals(".vite-manifest.json", ignoreCase = true)
        }
        if (hasManifest) {
            AppLogger.d("ApkBuilder", "Complex project indicator: build manifest detected")
            return true
        }

        if (allFiles.size > 10 && hasSourceMaps) {
            AppLogger.d("ApkBuilder", "Complex project indicator: ${allFiles.size} files with source maps")
            return true
        }

        val jsUsesModules = jsFiles.take(2).any { jsFile ->
            try {
                val file = File(jsFile.path)
                if (file.exists() && file.length() < 512 * 1024) {
                    val content = file.readText(Charsets.UTF_8).take(5000)
                    content.contains("import ", ignoreCase = false) &&
                    (content.contains(" from ", ignoreCase = false) || content.contains("import(", ignoreCase = false))
                } else false
            } catch (_: Exception) { false }
        }
        if (jsUsesModules) {
            AppLogger.d("ApkBuilder", "Complex project indicator: ES import/export syntax in JS files")
            return true
        }

        return false
    }

    private fun addHtmlFilesPreserveStructure(
        zipOut: ZipOutputStream,
        htmlFiles: List<com.webtoapp.data.model.HtmlFile>,
        encryptor: AssetEncryptor? = null,
        encryptionConfig: EncryptionConfig = EncryptionConfig.DISABLED
    ): Int {
        var successCount = 0

        htmlFiles.forEach { htmlFile ->
            try {
                val sourceFile = File(htmlFile.path)
                if (!sourceFile.exists() || !sourceFile.canRead()) {
                    AppLogger.w("ApkBuilder", "File not accessible: ${htmlFile.path}")
                    return@forEach
                }

                val fileBytes = sourceFile.readBytes()
                if (fileBytes.isEmpty()) {
                    AppLogger.w("ApkBuilder", "File is empty: ${htmlFile.path}")
                    return@forEach
                }

                val assetPath = "assets/html/${htmlFile.name}"
                val assetName = "html/${htmlFile.name}"
                val isHtml = htmlFile.name.endsWith(".html", ignoreCase = true) ||
                             htmlFile.name.endsWith(".htm", ignoreCase = true)
                val isText = isHtml ||
                             htmlFile.name.endsWith(".js", ignoreCase = true) ||
                             htmlFile.name.endsWith(".mjs", ignoreCase = true) ||
                             htmlFile.name.endsWith(".css", ignoreCase = true) ||
                             htmlFile.name.endsWith(".json", ignoreCase = true) ||
                             htmlFile.name.endsWith(".svg", ignoreCase = true) ||
                             htmlFile.name.endsWith(".xml", ignoreCase = true) ||
                             htmlFile.name.endsWith(".map", ignoreCase = true) ||
                             htmlFile.name.endsWith(".txt", ignoreCase = true)

                val finalBytes = if (isHtml) {
                    var content = String(fileBytes, Charsets.UTF_8)
                    if (!content.contains("viewport", ignoreCase = true)) {
                        content = com.webtoapp.util.HtmlProjectProcessor.processHtmlContent(
                            htmlContent = content,
                            cssContent = null,
                            jsContent = null,
                            fixPaths = false,
                            removeLocalRefs = false
                        )
                    }
                    content.toByteArray(Charsets.UTF_8)
                } else {
                    fileBytes
                }

                val shouldEncrypt = when {
                    isHtml && encryptionConfig.enabled && encryptor != null -> true
                    !isHtml && encryptionConfig.enabled && encryptor != null -> true
                    else -> false
                }

                if (shouldEncrypt && encryptor != null) {
                    val encryptedData = encryptor.encrypt(finalBytes, assetName)
                    writeEntryDeflated(zipOut, "${assetPath}.enc", encryptedData)
                    AppLogger.d("ApkBuilder", "File encrypted: ${assetPath}.enc (${encryptedData.size} bytes)")
                } else if (isText) {
                    writeEntryDeflated(zipOut, assetPath, finalBytes)
                    AppLogger.d("ApkBuilder", "Text file preserved: $assetPath (${finalBytes.size} bytes)")
                } else {

                    writeEntryStored(zipOut, assetPath, finalBytes)
                    AppLogger.d("ApkBuilder", "Binary file preserved: $assetPath (${finalBytes.size} bytes)")
                }

                successCount++
            } catch (e: Exception) {
                AppLogger.e("ApkBuilder", "Failed to embed file: ${htmlFile.path}", e)
            }
        }

        AppLogger.d("ApkBuilder", "PRESERVE mode complete: $successCount/${htmlFiles.size} files embedded")
        return successCount
    }

    private fun detectFileEncoding(file: File): String {
        return try {
            val bytes = file.readBytes().take(1000).toByteArray()

            when {
                bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte() -> "UTF-8"
                bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte() -> "UTF-16BE"
                bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() -> "UTF-16LE"
                else -> {

                    val content = String(bytes, Charsets.ISO_8859_1)
                    val charsetMatch = CHARSET_REGEX.find(content)
                    charsetMatch?.groupValues?.get(1)?.uppercase() ?: "UTF-8"
                }
            }
        } catch (e: Exception) {
            "UTF-8"
        }
    }

    private fun convertLrcDataToLrcString(lrcData: LrcData): String {
        val sb = StringBuilder()

        lrcData.title?.let { sb.appendLine("[ti:$it]") }
        lrcData.artist?.let { sb.appendLine("[ar:$it]") }
        lrcData.album?.let { sb.appendLine("[al:$it]") }
        sb.appendLine()

        lrcData.lines.forEach { line ->
            val minutes = line.startTime / 60000
            val seconds = (line.startTime % 60000) / 1000
            val centiseconds = (line.startTime % 1000) / 10
            sb.appendLine("[%02d:%02d.%02d]%s".format(minutes, seconds, centiseconds, line.text))

            line.translation?.let { translation ->
                sb.appendLine("[%02d:%02d.%02d]%s".format(minutes, seconds, centiseconds, translation))
            }
        }

        return sb.toString()
    }

    private fun debugApkStructure(apkFile: File): Boolean {
        return try {
            val pm = context.packageManager
            val flags = PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_PROVIDERS

            val info = pm.getPackageArchiveInfo(apkFile.absolutePath, flags)

            if (info == null) {
                AppLogger.e(
                    "ApkBuilder",
                    "getPackageArchiveInfo returned null, cannot parse APK: ${apkFile.absolutePath}"
                )
                false
            } else {
                AppLogger.d(
                    "ApkBuilder",
                    "APK parsed successfully: packageName=${info.packageName}, " +
                            "versionName=${info.versionName}, " +
                            "activities=${info.activities?.size ?: 0}, " +
                            "services=${info.services?.size ?: 0}, " +
                            "providers=${info.providers?.size ?: 0}"
                )
                true
            }
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Exception while debug parsing APK: ${apkFile.absolutePath}", e)
            false
        }
    }

    private fun generateDefaultIcon(appName: String, themeType: String = "AURORA"): Bitmap {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = 0xFF000000.toInt()
        bitmap.eraseColor(bgColor)

        val initial = getDefaultIconInitial(appName)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = size * 0.45f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val textX = size / 2f
        val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(initial, textX, textY, textPaint)

        logger.log("Generated default icon for '$appName' (initial='$initial', theme=$themeType, color=#${Integer.toHexString(bgColor)})")
        return bitmap
    }

    private fun getDefaultIconInitial(appName: String): String =
        appName.trim()
            .takeIf { it.isNotEmpty() }
            ?.let { name ->
                val codePoint = name.codePointAt(0)
                String(Character.toChars(codePoint)).uppercase()
            }
            ?: "A"

    private fun addIconsToApk(zipOut: ZipOutputStream, bitmap: Bitmap) {

        ApkTemplate.ICON_PATHS.forEach { (path, size) ->
            val iconBytes = template.scaleBitmapToPng(bitmap, size)
            writeEntryDeflated(zipOut, path, iconBytes)
        }

        ApkTemplate.ROUND_ICON_PATHS.forEach { (path, size) ->
            val iconBytes = template.createRoundIcon(bitmap, size)
            writeEntryDeflated(zipOut, path, iconBytes)
        }
    }

    private fun addAdaptiveIconPngs(
        zipOut: ZipOutputStream,
        bitmap: Bitmap,
        existingEntryNames: Set<String>
    ) {

        val bases = listOf(
            "res/drawable/ic_launcher_foreground",
            "res/drawable/ic_launcher_foreground_new",
            "res/drawable-v24/ic_launcher_foreground",
            "res/drawable-v24/ic_launcher_foreground_new",
            "res/drawable-anydpi-v24/ic_launcher_foreground",
            "res/drawable-anydpi-v24/ic_launcher_foreground_new"
        )

        val iconBytes = template.createAdaptiveForegroundIcon(bitmap, 432)

        bases.forEach { base ->
            val pngPath = "${base}.png"
            if (!existingEntryNames.contains(pngPath)) {
                writeEntryDeflated(zipOut, pngPath, iconBytes)
                AppLogger.d("ApkBuilder", "Added adaptive icon foreground: $pngPath")
            }
        }
    }

    private fun addAdaptiveIconReplacementPngs(zipOut: ZipOutputStream, bitmap: Bitmap) {

        val iconPng = template.scaleBitmapToPng(bitmap, 512)
        writeEntryDeflated(zipOut, "res/mipmap-anydpi-v26/ic_launcher.png", iconPng)
        AppLogger.d("ApkBuilder", "Added replacement icon: res/mipmap-anydpi-v26/ic_launcher.png (512px, ${iconPng.size} bytes)")

        val roundPng = template.createRoundIcon(bitmap, 512)
        writeEntryDeflated(zipOut, "res/mipmap-anydpi-v26/ic_launcher_round.png", roundPng)
        AppLogger.d("ApkBuilder", "Added replacement icon: res/mipmap-anydpi-v26/ic_launcher_round.png (512px, ${roundPng.size} bytes)")

        logger.log("Added PNG icons at mipmap-anydpi-v26 paths (512px, replacing adaptive icon XMLs)")
    }

    private fun writeEntryDeflated(zipOut: ZipOutputStream, name: String, data: ByteArray) {
        ZipUtils.writeEntryDeflated(zipOut, name, data)
    }

    private fun writeEntryStored(zipOut: ZipOutputStream, name: String, data: ByteArray) {
        ZipUtils.writeEntryStored(zipOut, name, data)
    }

    private fun writeConfigEntry(
        zipOut: ZipOutputStream,
        config: ApkConfig,
        encryptor: AssetEncryptor? = null,
        encryptionConfig: EncryptionConfig = EncryptionConfig.DISABLED
    ) {
        AppLogger.d("ApkBuilder", "Writing config file: splashEnabled=${config.splashEnabled}, splashType=${config.splashType}")
        AppLogger.d("ApkBuilder", "Config userAgentMode=${config.userAgentMode}, customUserAgent=${config.customUserAgent}")

        if (encryptionConfig.enabled && encryptor != null) {

            val configJson = template.createConfigJson(config)
            val encryptedData = encryptor.encryptJson(configJson, "app_config.json")
            writeEntryDeflated(zipOut, ApkTemplate.CONFIG_PATH + ".enc", encryptedData)

            val stubJson = template.createEncryptedStubJson(config)
            val stubData = stubJson.toByteArray(Charsets.UTF_8)
            writeEntryDeflated(zipOut, ApkTemplate.CONFIG_PATH, stubData)
            AppLogger.d("ApkBuilder", "Config file encrypted (minimal stub, no sensitive data)")
        } else {

            val configJson = template.createConfigJson(config)
            val data = configJson.toByteArray(Charsets.UTF_8)
            writeEntryDeflated(zipOut, ApkTemplate.CONFIG_PATH, data)
        }
    }

    private fun isOptimizableAsset(entryName: String): Boolean {
        val ext = entryName.substringAfterLast('.', "").lowercase()
        return ext in setOf("png", "jpg", "jpeg", "js", "css", "svg")
    }

    private fun runtimeAssetsRequiredFor(appType: String): List<String> = when (appType) {
        "PHP_APP", "WORDPRESS" -> listOf("php_router_server.php")
        else -> emptyList()
    }

    private fun ensureRequiredRuntimeAssets(
        zipOut: ZipOutputStream,
        appType: String,
        templateEntries: Set<String>
    ) {
        val required = runtimeAssetsRequiredFor(appType)
        for (assetName in required) {
            val entryName = "assets/$assetName"
            if (entryName in templateEntries) {

                continue
            }
            try {
                val bytes = context.assets.open(assetName).use { it.readBytes() }
                writeEntryDeflated(zipOut, entryName, bytes)
                logger.log("Injected runtime asset from host APK (template missing): $entryName (${bytes.size} bytes)")
                AppLogger.i("ApkBuilder", "Runtime asset injected from host APK: $entryName (${bytes.size} bytes)")
            } catch (e: Exception) {
                logger.error("CRITICAL: required runtime asset missing in BOTH template AND host APK: $assetName", e)
                AppLogger.e("ApkBuilder", "Runtime asset injection FAILED: $assetName", e)
            }
        }
    }

    private fun isEditorOnlyAsset(entryName: String, appType: String, engineType: String): Boolean {

        if (entryName.startsWith("assets/template/")) return true

        if (entryName.startsWith("assets/sample_projects/")) return true

        if (entryName.startsWith("assets/ai/")) return true
        if (entryName == "assets/litellm_model_prices.json") return true

        if (entryName.startsWith("assets/extensions/")) return true

        if (entryName == "assets/omni.ja") return true

        if (entryName == "assets/php_router_server.php" && appType !in setOf("WORDPRESS", "PHP_APP")) return true

        if (entryName.startsWith("assets/python_runtime/") && appType != "PYTHON_APP") return true

        if (entryName.startsWith("assets/go_runtime/") && appType != "GO_APP") return true

        if (entryName.startsWith("assets/help/")) return true

        if (entryName.startsWith("assets/schemas/")) return true

        if (entryName == "assets/default_config.json") return true

        if (entryName.startsWith("assets/frontend_tools/") && appType != "FRONTEND") return true

        if (entryName.startsWith("assets/nodejs_runtime/") && appType != "NODEJS_APP") return true

        return false
    }

    private fun shouldStripI18nPack(entryName: String, language: String): Boolean {
        return entryName.startsWith("assets/i18n/") &&
            (entryName.endsWith(".pack") || entryName.endsWith(".json"))
    }

    private fun i18nLanguageCode(language: String): String {
        return when (language.trim().uppercase()) {
            "CHINESE", "ZH", "ZH_CN", "ZH-CN" -> "zh"
            "ENGLISH", "EN" -> "en"
            "ARABIC", "AR" -> "ar"
            "PORTUGUESE", "PT" -> "pt"
            "SPANISH", "ES" -> "es"
            "FRENCH", "FR" -> "fr"
            "GERMAN", "DE" -> "de"
            "RUSSIAN", "RU" -> "ru"
            "JAPANESE", "JA" -> "ja"
            "KOREAN", "KO" -> "ko"
            else -> language.trim().lowercase().ifEmpty { "zh" }
        }
    }

    private fun readShellI18nPackBytes(code: String): ByteArray? {
        val names = listOf(
            "shell_i18n/$code.pack",
            "i18n/$code.pack"
        )
        for (name in names) {
            try {
                context.assets.open(name).use { return it.readBytes() }
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun injectSelectedI18nPack(zipOut: java.util.zip.ZipOutputStream, language: String) {
        val code = i18nLanguageCode(language)
        val selected = readShellI18nPackBytes(code)
        val en = readShellI18nPackBytes("en")
        val primaryCode = when {
            selected != null -> code
            en != null -> "en"
            else -> return
        }
        val primaryBytes = if (selected != null) selected else en!!
        writeEntryDeflated(zipOut, "assets/i18n/$primaryCode.pack", primaryBytes)
        AppLogger.d("ApkBuilder", "Injected i18n pack: assets/i18n/$primaryCode.pack (${primaryBytes.size} bytes)")
        if (primaryCode != "en" && en != null) {
            writeEntryDeflated(zipOut, "assets/i18n/en.pack", en)
            AppLogger.d("ApkBuilder", "Injected i18n fallback pack: assets/i18n/en.pack (${en.size} bytes)")
        }
    }

    private fun isIconEntry(entryName: String): Boolean {

        if (ApkTemplate.ICON_PATHS.any { it.first == entryName } ||
            ApkTemplate.ROUND_ICON_PATHS.any { it.first == entryName }) {
            return true
        }

        val iconPatterns = listOf(
            "ic_launcher.png",
            "ic_launcher_round.png"

        )
        return iconPatterns.any { pattern ->
            entryName.endsWith(pattern) &&
            (entryName.contains("mipmap") || entryName.contains("drawable"))
        }
    }

    private fun isAdaptiveIconEntry(entryName: String): Boolean {

        if ((entryName.contains("drawable")) &&
            (entryName.contains("ic_launcher_foreground") || entryName.contains("ic_launcher_foreground_new")) &&
            (entryName.endsWith(".xml") || entryName.endsWith(".jpg") || entryName.endsWith(".png"))) {
            return true
        }
        return false
    }

    private fun isAdaptiveIconDefinition(entryName: String): Boolean {
        return entryName.startsWith("res/mipmap-anydpi") &&
            (entryName.contains("ic_launcher") || entryName.contains("ic_launcher_round")) &&
            entryName.endsWith(".xml")
    }

    private fun replaceIconEntry(zipOut: ZipOutputStream, entryName: String, bitmap: Bitmap) {

        var size = ApkTemplate.ICON_PATHS.find { it.first == entryName }?.second
            ?: ApkTemplate.ROUND_ICON_PATHS.find { it.first == entryName }?.second

        if (size == null) {
            size = when {
                entryName.contains("foreground") -> 432
                entryName.contains("xxxhdpi") -> 192
                entryName.contains("xxhdpi") -> 144
                entryName.contains("xhdpi") -> 96
                entryName.contains("hdpi") -> 72
                entryName.contains("mdpi") -> 48
                entryName.contains("ldpi") -> 36
                else -> 96
            }
        }

        val iconBytes = when {

            entryName.contains("round") -> {
                template.createRoundIcon(bitmap, size)
            }

            entryName.contains("foreground") -> {
                template.createAdaptiveForegroundIcon(bitmap, size)
            }

            else -> {
                template.scaleBitmapToPng(bitmap, size)
            }
        }

        writeEntryDeflated(zipOut, entryName, iconBytes)
    }

    private fun copyEntry(zipIn: ZipFile, zipOut: ZipOutputStream, entry: ZipEntry) {
        ZipUtils.copyEntry(zipIn, zipOut, entry)
    }

    private fun generatePackageName(appName: String): String {

        val raw = appName.hashCode().let {
            if (it < 0) (-it).toString(36) else it.toString(36)
        }.take(4).padStart(4, '0')

        val segment = normalizePackageSegment(raw)

        return "com.w2a.$segment"
    }

    private fun normalizePackageSegment(segment: String): String {
        if (segment.isEmpty()) return "a"

        val chars = segment.lowercase().toCharArray()

        chars[0] = when {
            chars[0] in 'a'..'z' -> chars[0]
            chars[0] in '0'..'9' -> ('a' + (chars[0] - '0'))
            else -> 'a'
        }

        return String(chars)
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(SANITIZE_FILENAME_REGEX, "_").take(50)
    }

    private fun buildRequiredPermissions(config: ApkConfig): List<String> {

        val permissions = linkedSetOf<String>()

        permissions += "${config.packageName}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"

        if (config.requiresNetworkPermissions()) {
            permissions += "android.permission.INTERNET"
            permissions += "android.permission.ACCESS_NETWORK_STATE"
        }

        val rp = config.runtimePermissions

        if (rp.camera) {
            permissions += "android.permission.CAMERA"
        }
        if (rp.microphone) {
            permissions += "android.permission.RECORD_AUDIO"
            permissions += "android.permission.MODIFY_AUDIO_SETTINGS"
        }
        if (rp.location) {
            permissions += "android.permission.ACCESS_COARSE_LOCATION"
            permissions += "android.permission.ACCESS_FINE_LOCATION"
        }
        if (rp.notifications) {
            permissions += "android.permission.POST_NOTIFICATIONS"
        }

        if (rp.readExternalStorage) {
            permissions += "android.permission.READ_EXTERNAL_STORAGE"
        }
        if (rp.writeExternalStorage || (config.downloadEnabled && config.downloadLocationMode != "APP_PRIVATE")) {
            permissions += "android.permission.WRITE_EXTERNAL_STORAGE"
        }
        if (rp.readMediaImages) {
            permissions += "android.permission.READ_MEDIA_IMAGES"
        }
        if (rp.readMediaVideo) {
            permissions += "android.permission.READ_MEDIA_VIDEO"
        }
        if (rp.readMediaAudio) {
            permissions += "android.permission.READ_MEDIA_AUDIO"
        }

        if (rp.bluetooth) {
            permissions += "android.permission.BLUETOOTH"
            permissions += "android.permission.BLUETOOTH_ADMIN"
            permissions += "android.permission.BLUETOOTH_SCAN"
            permissions += "android.permission.BLUETOOTH_CONNECT"
            permissions += "android.permission.BLUETOOTH_ADVERTISE"
        }
        if (rp.nfc) {
            permissions += "android.permission.NFC"
        }
        if (rp.wifiState) {
            permissions += "android.permission.ACCESS_WIFI_STATE"
            permissions += "android.permission.CHANGE_WIFI_STATE"
        }

        if (rp.bodySensors) {
            permissions += "android.permission.BODY_SENSORS"
            permissions += "android.permission.BODY_SENSORS_BACKGROUND"
        }
        if (rp.activityRecognition) {
            permissions += "android.permission.ACTIVITY_RECOGNITION"
        }

        if (rp.readPhoneState) {
            permissions += "android.permission.READ_PHONE_STATE"
        }
        if (rp.callPhone) {
            permissions += "android.permission.CALL_PHONE"
        }
        if (rp.readContacts) {
            permissions += "android.permission.READ_CONTACTS"
        }
        if (rp.writeContacts) {
            permissions += "android.permission.WRITE_CONTACTS"
        }
        if (rp.readCalendar) {
            permissions += "android.permission.READ_CALENDAR"
        }
        if (rp.writeCalendar) {
            permissions += "android.permission.WRITE_CALENDAR"
        }
        if (rp.readSms) {
            permissions += "android.permission.READ_SMS"
        }
        if (rp.sendSms) {
            permissions += "android.permission.SEND_SMS"
        }
        if (rp.receiveSms) {
            permissions += "android.permission.RECEIVE_SMS"
        }
        if (rp.readCallLog) {
            permissions += "android.permission.READ_CALL_LOG"
        }
        if (rp.writeCallLog) {
            permissions += "android.permission.WRITE_CALL_LOG"
        }
        if (rp.processOutgoingCalls) {
            permissions += "android.permission.PROCESS_OUTGOING_CALLS"
        }

        if (rp.foregroundService) {
            permissions += "android.permission.FOREGROUND_SERVICE"
            permissions += "android.permission.FOREGROUND_SERVICE_DATA_SYNC"
            permissions += "android.permission.FOREGROUND_SERVICE_SPECIAL_USE"
        }
        if (rp.wakeLock) {
            permissions += "android.permission.WAKE_LOCK"
        }
        if (rp.requestIgnoreBatteryOptimizations) {
            permissions += "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"
        }
        if (rp.bootCompleted) {
            permissions += "android.permission.RECEIVE_BOOT_COMPLETED"
        }
        if (rp.vibration) {
            permissions += "android.permission.VIBRATE"
        }
        if (rp.installPackages) {
            permissions += "android.permission.REQUEST_INSTALL_PACKAGES"
        }
        if (rp.requestDeletePackages) {
            permissions += "android.permission.REQUEST_DELETE_PACKAGES"
        }
        if (rp.systemAlertWindow) {
            permissions += "android.permission.SYSTEM_ALERT_WINDOW"
        }

        val needsForegroundService = config.backgroundRunEnabled ||
            config.notificationEnabled ||
            config.floatingWindowEnabled ||
            config.forcedRunConfig?.enabled == true ||
            config.bgmEnabled
        if (needsForegroundService || rp.foregroundService) {
            permissions += "android.permission.FOREGROUND_SERVICE"
            permissions += "android.permission.FOREGROUND_SERVICE_SPECIAL_USE"
        }
        if (config.forcedRunConfig?.enabled == true) {

            permissions += "android.permission.FOREGROUND_SERVICE_DATA_SYNC"
        }
        if (rp.location) {
            permissions += "android.permission.FOREGROUND_SERVICE_LOCATION"
        }
        if (rp.camera) {
            permissions += "android.permission.FOREGROUND_SERVICE_CAMERA"
        }
        if (rp.microphone) {
            permissions += "android.permission.FOREGROUND_SERVICE_MICROPHONE"
        }
        if (config.bgmEnabled) {
            permissions += "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"
        }

        if (config.backgroundRunEnabled ||
            config.screenAwakeMode.uppercase() != "OFF" ||
            config.keepScreenOn ||
            config.forcedRunConfig?.enabled == true ||
            rp.wakeLock) {
            permissions += "android.permission.WAKE_LOCK"
        }
        if (config.backgroundRunEnabled || rp.requestIgnoreBatteryOptimizations) {
            permissions += "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"
        }

        if (config.bootStartEnabled || config.autoStartEnabled || rp.bootCompleted) {
            permissions += "android.permission.RECEIVE_BOOT_COMPLETED"
        }
        if (config.scheduledStartEnabled) {
            permissions += "android.permission.SCHEDULE_EXACT_ALARM"
            permissions += "android.permission.USE_EXACT_ALARM"
        }

        if (config.activationEnabled) {
            permissions += "android.permission.USE_BIOMETRIC"
            permissions += "android.permission.USE_FINGERPRINT"
        }

        val bt = config.blackTechConfig
        if (bt?.forceFlashlight == true) {
            permissions += "android.permission.FLASHLIGHT"

            permissions += "android.permission.CAMERA"
        }
        if (bt?.forceMaxVibration == true || rp.vibration) {
            permissions += "android.permission.VIBRATE"
        }
        val forcedRun = config.forcedRunConfig
        val needsWriteSettings = bt?.forceScreenAwake == true ||
            bt?.forceMaxVolume == true ||
            bt?.forceMuteMode == true ||
            forcedRun?.enabled == true
        if (needsWriteSettings) {
            permissions += "android.permission.WRITE_SETTINGS"
        }
        if (bt?.forceWifiHotspot == true ||
            bt?.forceDisableWifi == true ||
            rp.wifiState) {
            permissions += "android.permission.ACCESS_WIFI_STATE"
            permissions += "android.permission.CHANGE_WIFI_STATE"
            permissions += "android.permission.CHANGE_NETWORK_STATE"
        }

        if (config.floatingWindowEnabled) {
            permissions += "android.permission.SYSTEM_ALERT_WINDOW"
        }

        if (config.downloadEnabled) {
            permissions += "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION"
        }

        if (rp.bluetooth) {
            permissions += "android.permission.NEARBY_WIFI_DEVICES"
        }

        if (rp.bodySensors) {
            permissions += "android.permission.HIGH_SAMPLING_RATE_SENSORS"
        }

        return permissions.toList()
    }

    private fun buildRequiredComponents(config: ApkConfig): Set<String> {
        val components = linkedSetOf(
            "com.webtoapp.WebToAppApplication",
            "com.webtoapp.ui.MainActivity",
            "com.webtoapp.ui.shell.ShellActivity",
            "androidx.core.content.FileProvider"
        )

        if (config.appType == "NODEJS_APP") {
            components += "com.webtoapp.core.nodejs.NodeService"
        }

        if (config.backgroundRunEnabled) {
            components += "com.webtoapp.core.background.BackgroundRunService"
        }

        if (config.notificationEnabled) {
            components += "com.webtoapp.core.notification.NotificationPollingService"
            components += "com.webtoapp.core.notification.NotificationWebSocketService"
            components += "com.webtoapp.core.notification.NotificationFcmService"
        }

        if (config.enableNativeBridge && config.webViewBehavior.nativeBridgeNotificationScheduled) {
            components += "com.webtoapp.core.notification.BridgeAlarmReceiver"
        }

        if (config.floatingWindowEnabled) {
            components += "com.webtoapp.core.floatingwindow.FloatingWindowService"
        }

        val forcedRunEnabled = config.forcedRunConfig?.enabled == true
        if (forcedRunEnabled) {
            components += "com.webtoapp.core.forcedrun.ForcedRunGuardService"
            components += "com.webtoapp.core.forcedrun.ForcedRunAccessibilityService"
            components += "com.webtoapp.core.forcedrun.ForcedRunReceiver"
        }

        if (config.bootStartEnabled || config.scheduledStartEnabled || forcedRunEnabled) {
            components += "com.webtoapp.core.autostart.BootReceiver"
        }

        if (config.scheduledStartEnabled) {
            components += "com.webtoapp.core.autostart.ScheduledStartReceiver"
        }

        if (config.appType in setOf("NODEJS_APP", "WORDPRESS", "PHP_APP", "PYTHON_APP", "GO_APP")) {
            components += "com.webtoapp.core.port.PortQueryReceiver"
            components += "com.webtoapp.core.port.PortReleaseReceiver"
        }

        if (config.engineType == "GECKOVIEW") {
            components += GECKOVIEW_RUNTIME_COMPONENTS
        }

        return components
    }

    private fun ApkConfig.requiresNetworkPermissions(): Boolean {
        if (appType != "HTML" && appType != "FRONTEND") return true
        if (!htmlUsesFileScheme) return true
        if (targetUrl.startsWith("http://", ignoreCase = true) ||
            targetUrl.startsWith("https://", ignoreCase = true)) return true
        if (adsEnabled || adBannerEnabled || adInterstitialEnabled || adSplashEnabled) return true
        if (announcementEnabled &&
            (announcementLink.startsWith("http://", ignoreCase = true) ||
                announcementLink.startsWith("https://", ignoreCase = true) ||
                announcementTriggerOnNoNetwork ||
                announcementTriggerIntervalMinutes > 0)) return true
        if (activationRemoteEnabled) return true
        if (translateEnabled) return true
        if (proxyMode != "NONE") return true
        if (dnsMode != "SYSTEM") return true
        if (tlsFingerprintEnabled) return true
        if (pwaOfflineEnabled) return true
        if (enablePrivateNetworkBridge) return true
        if (enableCloudflareCompat && webViewBehavior.cloudflareCompatMode == "ALWAYS_ON") return true
        if (failoverEnabled && failoverUrls.isNotEmpty()) return true
        return false
    }

    fun installApk(apkFile: File): Boolean {
        return try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Operation failed", e)
            false
        }
    }

    fun getBuiltApks(): List<File> {
        return outputDir.listFiles()?.filter { it.extension == "apk" } ?: emptyList()
    }

    fun deleteApk(apkFile: File): Boolean {
        return apkFile.delete()
    }

    fun clearAll() {
        outputDir.listFiles()?.forEach { it.delete() }
        tempDir.listFiles()?.forEach { it.delete() }
    }

    fun getBuildLogs(): List<File> {
        return logger.getAllLogFiles()
    }

    fun getLogDirectory(): String {
        val external = context.getExternalFilesDir(null)
        val dir = if (external != null) {
            val d = File(external, "build_logs")
            if (d.exists() || d.mkdirs()) d else File(context.filesDir, "build_logs")
        } else {
            File(context.filesDir, "build_logs")
        }
        dir.mkdirs()
        return dir.absolutePath
    }
}

fun WebApp.toApkConfig(packageName: String, context: android.content.Context? = null): ApkConfig {
    val htmlUsesFileScheme = computeHtmlUsesFileScheme(context)
    val effectiveTargetUrl = computeEffectiveTargetUrl(packageName)
    val bakedDarkMode = context?.let { ThemeManager.getInstance(it).currentDarkMode.name } ?: "SYSTEM"
    return ApkConfig(
        meta = buildMetaBlock(packageName, effectiveTargetUrl, htmlUsesFileScheme, bakedDarkMode),
        activation = buildActivationBlock(),
        adBlock = buildAdBlockBlock(),
        announcement = buildAnnouncementBlock(),
        ads = buildAdsBlock(),
        webView = buildWebViewBlock(context),
        webViewBehavior = buildWebViewBehaviorBlock(),
        screenAwake = buildScreenAwakeBlock(),
        statusBar = buildStatusBarBlock(),
        floatingWindow = buildFloatingWindowBlock(),
        proxy = buildProxyBlock(),
        dns = buildDnsBlock(),
        tlsFingerprint = buildTlsFingerprintBlock(),
        errorPage = buildErrorPageBlock(),
        splash = buildSplashBlock(),
        media = buildMediaBlock(),
        html = buildHtmlBlock(),
        gallery = buildGalleryBlock(),
        bgm = buildBgmBlock(),
        translate = buildTranslateBlock(),
        extension = buildExtensionBlock(),
        autoStart = buildAutoStartBlock(),
        optionalServices = buildOptionalServicesBlock(),
        disguise = buildDisguiseBlock(),
        deepLink = buildDeepLinkBlock(packageName),
        wordpress = buildWordpressBlock(),
        nodejs = buildNodejsBlock(),
        phpApp = buildPhpAppBlock(),
        pythonApp = buildPythonAppBlock(),
        goApp = buildGoAppBlock(),
        multiWeb = buildMultiWebBlock(context, packageName)
    )
}

private fun WebApp.computeEffectiveTargetUrl(packageName: String): String = when (appType) {
    com.webtoapp.data.model.AppType.HTML -> {
        val entryFile = htmlConfig?.getValidEntryFile() ?: "index.html"
        buildPackagedHtmlShellEntryUrl(packageName, entryFile)
    }
    com.webtoapp.data.model.AppType.IMAGE,
    com.webtoapp.data.model.AppType.VIDEO -> "asset://media_content"
    com.webtoapp.data.model.AppType.GALLERY -> "gallery://content"
    com.webtoapp.data.model.AppType.WORDPRESS -> "wordpress://localhost"
    com.webtoapp.data.model.AppType.NODEJS_APP -> when (nodejsConfig?.buildMode) {
        com.webtoapp.data.model.NodeJsBuildMode.STATIC ->
            "nodejs://localhost"
        com.webtoapp.data.model.NodeJsBuildMode.API_BACKEND,
        com.webtoapp.data.model.NodeJsBuildMode.FULLSTACK,
        com.webtoapp.data.model.NodeJsBuildMode.SSR ->
            "nodejs://localhost"
        else -> "file:///android_asset/nodejs_app/index.html"
    }
    com.webtoapp.data.model.AppType.FRONTEND -> {
        val entryFile = htmlConfig?.getValidEntryFile() ?: "index.html"
        buildPackagedHtmlShellEntryUrl(packageName, entryFile)
    }
    com.webtoapp.data.model.AppType.PHP_APP -> "phpapp://localhost"
    com.webtoapp.data.model.AppType.PYTHON_APP -> "pythonapp://localhost"
    com.webtoapp.data.model.AppType.GO_APP -> "goapp://localhost"
    com.webtoapp.data.model.AppType.MULTI_WEB -> "multiweb://localhost"
    else -> url
}

@Suppress("UNUSED_PARAMETER")
private fun com.webtoapp.data.model.HtmlConfig?.computeHtmlUsesFileScheme(context: android.content.Context?): Boolean {
    val mode = this?.loadMode ?: com.webtoapp.data.model.HtmlLoadMode.AUTO
    return when (mode) {
        com.webtoapp.data.model.HtmlLoadMode.FILE -> true
        com.webtoapp.data.model.HtmlLoadMode.LOCAL_HTTP -> false
        com.webtoapp.data.model.HtmlLoadMode.AUTO -> {
            val htmlDir = this?.projectDir?.let { java.io.File(it) }
            if (htmlDir != null && htmlDir.exists()) {
                com.webtoapp.core.webview.HtmlRuntimeLoadInspector.prefersFileScheme(htmlDir)
            } else {
                false
            }
        }
    }
}

private fun WebApp.computeHtmlUsesFileScheme(context: android.content.Context?): Boolean =
    htmlConfig.computeHtmlUsesFileScheme(context)

private fun WebApp.buildEffectiveRuntimePermissions(): ApkRuntimePermissions {
    var result = apkExportConfig?.runtimePermissions ?: ApkRuntimePermissions()
    if (apkExportConfig?.backgroundRunEnabled == true) {
        result = result.copy(
            foregroundService = true,
            wakeLock = true,
            notifications = true,
            requestIgnoreBatteryOptimizations = true
        )
    }
    if (apkExportConfig?.notificationEnabled == true) {
        result = result.copy(notifications = true, foregroundService = true)
    }
    if (autoStartConfig?.bootStartEnabled == true) {
        result = result.copy(bootCompleted = true)
    }
    if (webViewConfig.floatingWindowConfig.enabled) {
        result = result.copy(systemAlertWindow = true)
    }
    if (forcedRunConfig?.enabled == true) {
        result = result.copy(foregroundService = true, wakeLock = true)
    }
    if (webViewConfig.enableNativeBridge && webViewConfig.nativeBridgeCapabilities.notification) {
        result = result.copy(notifications = true)
    }
    if (webViewConfig.enableNotificationPolyfill) {
        result = result.copy(notifications = true)
    }
    if (webViewConfig.geolocationEnabled) {
        result = result.copy(location = true)
    }
    return result
}

private fun WebApp.buildMetaBlock(packageName: String, effectiveTargetUrl: String, htmlUsesFileScheme: Boolean, darkMode: String): MetaBlock = MetaBlock(
    appName = name,
    packageName = packageName,
    targetUrl = effectiveTargetUrl,
    versionCode = apkExportConfig?.customVersionCode ?: 1,
    versionName = apkExportConfig?.customVersionName?.takeIf { it.isNotBlank() } ?: "1.0.0",
    iconPath = iconPath,
    runtimePermissions = buildEffectiveRuntimePermissions(),
    networkTrustConfig = apkExportConfig?.networkTrustConfig ?: com.webtoapp.data.model.NetworkTrustConfig(),
    appType = appType.name,
    themeType = themeType,
    darkMode = darkMode,
    language = com.webtoapp.core.i18n.Strings.currentLanguage.value.name,
    engineType = apkExportConfig?.engineType ?: "SYSTEM_WEBVIEW",
    htmlUsesFileScheme = htmlUsesFileScheme,
    loggingEnabled = apkExportConfig?.loggingEnabled ?: false
)

private fun WebApp.buildActivationBlock(): ActivationBlock = ActivationBlock(
    enabled = activationEnabled,
    codes = getActivationCodeStrings(),
    requireEveryTime = activationRequireEveryTime,
    dialogTitle = activationDialogConfig?.title ?: "",
    dialogSubtitle = activationDialogConfig?.subtitle ?: "",
    dialogInputLabel = activationDialogConfig?.inputLabel ?: "",
    dialogButtonText = activationDialogConfig?.buttonText ?: "",
    remoteEnabled = activationRemoteConfig?.enabled ?: false,
    remoteVerifyUrl = activationRemoteConfig?.verifyUrl ?: "",
    remotePublicKey = activationRemoteConfig?.publicKeyBase64 ?: "",
    remoteOfflinePolicy = activationRemoteConfig?.offlinePolicy?.name ?: "ALLOW_CACHED"
)

private fun WebApp.buildAdBlockBlock(): AdBlockBlock = AdBlockBlock(
    enabled = adBlockEnabled,
    rules = adBlockRules,
    subscriptions = adBlockSubscriptions
)

private fun WebApp.buildAnnouncementBlock(): AnnouncementBlock = AnnouncementBlock(
    enabled = announcementEnabled,
    title = announcement?.title ?: "",
    content = announcement?.content ?: "",
    contentIsHtml = announcement?.contentIsHtml ?: false,
    link = announcement?.linkUrl ?: "",
    linkText = announcement?.linkText ?: "",
    template = announcement?.template?.toUiTemplate()?.type?.name ?: AnnouncementTemplateType.MINIMAL.name,
    showOnce = announcement?.showOnce ?: true,
    requireConfirmation = announcement?.requireConfirmation ?: false,
    allowNeverShow = announcement?.allowNeverShow ?: false,
    triggerOnLaunch = announcement?.triggerOnLaunch ?: true,
    triggerOnNoNetwork = announcement?.triggerOnNoNetwork ?: false,
    triggerIntervalMinutes = announcement?.triggerIntervalMinutes ?: 0,
    version = announcement?.version ?: 1,
    triggerIntervalIncludeLaunch = announcement?.triggerIntervalIncludeLaunch ?: false
)

private fun WebApp.buildAdsBlock(): AdsBlock = AdsBlock(
    enabled = adsEnabled,
    bannerEnabled = adConfig?.bannerEnabled ?: false,
    bannerId = adConfig?.bannerId ?: "",
    interstitialEnabled = adConfig?.interstitialEnabled ?: false,
    interstitialId = adConfig?.interstitialId ?: "",
    splashEnabled = adConfig?.splashEnabled ?: false,
    splashId = adConfig?.splashId ?: ""
)

private fun com.webtoapp.data.model.WebViewConfig.toWebViewBlock(context: android.content.Context?): WebViewBlock {
    val resolvedInjectScripts = buildList {

        if (enableKernelDisguise) {
            add(com.webtoapp.data.model.UserScript(
                name = "__kernel__",
                code = com.webtoapp.core.kernel.BrowserKernel.getBuildTimeKernelJs(),
                enabled = true,
                runAt = com.webtoapp.data.model.ScriptRunTime.DOCUMENT_START
            ))
        }
        add(com.webtoapp.data.model.UserScript(
            name = "__perf_start__",
            code = com.webtoapp.core.perf.NativePerfEngine.getPerfJsStart(),
            enabled = true,
            runAt = com.webtoapp.data.model.ScriptRunTime.DOCUMENT_START
        ))
        add(com.webtoapp.data.model.UserScript(
            name = "__perf_end__",
            code = com.webtoapp.core.perf.NativePerfEngine.getPerfJsEnd(),
            enabled = true,
            runAt = com.webtoapp.data.model.ScriptRunTime.DOCUMENT_END
        ))
        val userScripts = if (context != null && injectScripts.any {
                com.webtoapp.core.script.UserScriptStorage.isFileReference(it.code)
            }) {
            com.webtoapp.core.script.UserScriptStorage.internalizeScripts(context, injectScripts)
        } else {
            injectScripts
        }
        addAll(userScripts)
    }

    return WebViewBlock(
        javaScriptEnabled = javaScriptEnabled,
        domStorageEnabled = domStorageEnabled,
        allowFileAccess = allowFileAccess,
        allowContentAccess = allowContentAccess,
        cacheEnabled = cacheEnabled && !clearBrowsingDataOnLaunch,
        clearBrowsingDataOnLaunch = clearBrowsingDataOnLaunch,
        zoomEnabled = zoomEnabled,
        desktopMode = desktopMode,
        userAgent = userAgent,
        userAgentMode = userAgentMode.name,
        customUserAgent = customUserAgent,
        hideToolbar = hideToolbar,
        hideBrowserToolbar = hideBrowserToolbar,
        toolbarShowTitle = toolbarShowTitle,
        toolbarShowUrl = toolbarShowUrl,
        toolbarShowBack = toolbarShowBack,
        toolbarShowForward = toolbarShowForward,
        toolbarShowRefresh = toolbarShowRefresh,
        browserToolbarCustomized = browserToolbarCustomized,
        showStatusBarInFullscreen = showStatusBarInFullscreen,
        showNavigationBarInFullscreen = showNavigationBarInFullscreen,
        showToolbarInFullscreen = showToolbarInFullscreen,
        landscapeMode = landscapeMode,
        orientationMode = orientationMode.name,
        injectScripts = resolvedInjectScripts,
        longPressMenuEnabled = longPressMenuEnabled,
        longPressMenuStyle = longPressMenuStyle.name,

        popupBlockerEnabled = popupBlockerEnabled,
        popupBlockerToggleEnabled = popupBlockerToggleEnabled,
        openExternalLinks = openExternalLinks,
        showFloatingBackButton = showFloatingBackButton,
        swipeRefreshEnabled = swipeRefreshEnabled,
        fullscreenEnabled = fullscreenEnabled,
        performanceOptimization = performanceOptimization,
        pwaOfflineEnabled = pwaOfflineEnabled && !clearBrowsingDataOnLaunch,
        pwaOfflineStrategy = pwaOfflineStrategy,
        keyboardAdjustMode = keyboardAdjustMode.name,
        downloadEnabled = downloadEnabled,
        downloadLocationMode = downloadLocationMode.name,
        customDownloadDirUri = customDownloadDirUri,
        antiCapture = antiCapture
    )
}

private fun WebApp.buildWebViewBlock(context: android.content.Context?): WebViewBlock {
    val block = webViewConfig.toWebViewBlock(context)
    val needsFileAccess = appType == com.webtoapp.data.model.AppType.HTML ||
        appType == com.webtoapp.data.model.AppType.FRONTEND ||
        computeHtmlUsesFileScheme(context)
    return if (needsFileAccess && !block.allowFileAccess) {
        block.copy(allowFileAccess = true)
    } else {
        block
    }
}

private fun WebApp.buildWebViewBehaviorBlock(): WebViewBehaviorBlock = WebViewBehaviorBlock(
    initialScale = webViewConfig.initialScale,
    viewportMode = webViewConfig.viewportMode.name,
    customViewportWidth = webViewConfig.customViewportWidth,
    newWindowBehavior = webViewConfig.newWindowBehavior.name,
    enablePaymentSchemes = webViewConfig.enablePaymentSchemes,
    enableShareBridge = webViewConfig.enableShareBridge,
    enableZoomPolyfill = webViewConfig.enableZoomPolyfill,
    enableCrossOriginIsolation = webViewConfig.enableCrossOriginIsolation,
    hideUrlPreview = webViewConfig.hideUrlPreview,
    decodeBase64DeepLinks = webViewConfig.decodeBase64DeepLinks,
    decodeBase64Mode = webViewConfig.decodeBase64Mode.name,
    mediaAutoplayEnabled = webViewConfig.mediaAutoplayEnabled,
    mediaAutoplayScope = webViewConfig.mediaAutoplayScope.name,
    acceptThirdPartyCookies = webViewConfig.acceptThirdPartyCookies,
    thirdPartyCookieMode = webViewConfig.thirdPartyCookieMode.name,
    enableKernelDisguise = webViewConfig.enableKernelDisguise,
    kernelDisguiseLevel = webViewConfig.kernelDisguiseLevel.name,
    kernelFlavor = webViewConfig.kernelFlavor.name,
    enableImageRepair = webViewConfig.enableImageRepair,
    enableScrollMemory = webViewConfig.enableScrollMemory,
    enableBackStatePreservation = webViewConfig.enableBackStatePreservation,
    followSystemDarkMode = webViewConfig.followSystemDarkMode,
    enableClipboardPolyfill = webViewConfig.enableClipboardPolyfill,
    enableNotificationPolyfill = webViewConfig.enableNotificationPolyfill ||
        (apkExportConfig?.let {
            it.notificationEnabled && it.notificationConfig?.type?.key == "web_api"
        } == true),
    geolocationEnabled = webViewConfig.geolocationEnabled,
    geolocationAccuracy = webViewConfig.geolocationAccuracy.name,
    geolocationPolicy = webViewConfig.geolocationPolicy.name,
    enableOrientationPolyfill = webViewConfig.enableOrientationPolyfill,
    enableCompatPolyfills = webViewConfig.enableCompatPolyfills,
    enableNativeBridge = webViewConfig.enableNativeBridge,
    nativeBridgeClipboard = webViewConfig.nativeBridgeCapabilities.clipboard,
    nativeBridgeVibration = webViewConfig.nativeBridgeCapabilities.vibration,
    nativeBridgeGeolocation = webViewConfig.nativeBridgeCapabilities.geolocation,
    nativeBridgeBrightness = webViewConfig.nativeBridgeCapabilities.brightness,
    nativeBridgeNotification = webViewConfig.nativeBridgeCapabilities.notification,
    nativeBridgeNotificationScheduled = webViewConfig.nativeBridgeCapabilities.notificationScheduled,
    nativeBridgeNotificationPersistent = webViewConfig.nativeBridgeCapabilities.notificationPersistent,
    nativeBridgeDownload = webViewConfig.nativeBridgeCapabilities.download,
    nativeBridgePrivateNetwork = webViewConfig.nativeBridgeCapabilities.privateNetwork,
    nativeBridgeScreenWake = webViewConfig.nativeBridgeCapabilities.screenWake,
    nativeBridgeOpenExternal = webViewConfig.nativeBridgeCapabilities.openExternal,
    nativeBridgeDeviceInfo = webViewConfig.nativeBridgeCapabilities.deviceInfo,
    nativeBridgeSecurityInfo = webViewConfig.nativeBridgeCapabilities.securityInfo,
    nativeBridgeNetworkInfo = webViewConfig.nativeBridgeCapabilities.networkInfo,
    nativeBridgeToast = webViewConfig.nativeBridgeCapabilities.toast,
    nativeBridgeLogging = webViewConfig.nativeBridgeCapabilities.logging,
    nativeBridgeFindInPage = webViewConfig.nativeBridgeCapabilities.findInPage,
    nativeBridgeOrientation = webViewConfig.nativeBridgeCapabilities.orientation,
    nativeBridgeFullscreen = webViewConfig.nativeBridgeCapabilities.fullscreen,
    nativeBridgePrint = webViewConfig.nativeBridgeCapabilities.print,
    javaScriptCanOpenWindows = webViewConfig.javaScriptCanOpenWindows,
    jsOpenWindowsPolicy = webViewConfig.jsOpenWindowsPolicy.name,
    databaseEnabled = webViewConfig.databaseEnabled,
    enableCookiePersistence = webViewConfig.enableCookiePersistence,
    enablePrivateNetworkBridge = webViewConfig.enablePrivateNetworkBridge,
    privateNetworkScope = webViewConfig.privateNetworkScope.name,
    enableCorsBypass = webViewConfig.enableCorsBypass,
    allowMixedContent = webViewConfig.allowMixedContent,
    mixedContentMode = webViewConfig.mixedContentMode.name,
    enableBlobDownloadInterception = webViewConfig.enableBlobDownloadInterception,
    blobInterceptScope = webViewConfig.blobInterceptScope.name,
    blobInterceptThresholdMb = webViewConfig.blobInterceptThresholdMb,
    enablePrintBridge = webViewConfig.enablePrintBridge,
    enableCloudflareCompat = webViewConfig.enableCloudflareCompat,
    cloudflareCompatMode = webViewConfig.cloudflareCompatMode.name,
    primeUserActivation = webViewConfig.primeUserActivation,
    primeUserActivationMode = webViewConfig.primeUserActivationMode.name,
    primeUserActivationTiming = webViewConfig.primeUserActivationTiming.name,
    fullscreenVideoOrientation = webViewConfig.fullscreenVideoOrientation.name,
    failoverEnabled = webViewConfig.failoverEnabled,
    failoverUrls = webViewConfig.failoverUrls,
    failoverTriggerNetworkError = webViewConfig.failoverTriggers.networkError,
    failoverTriggerHttp5xx = webViewConfig.failoverTriggers.http5xx,
    failoverTriggerHttp4xx = webViewConfig.failoverTriggers.http4xx,
    failoverTriggerTimeout = webViewConfig.failoverTriggers.timeout,
    failoverTimeoutSeconds = webViewConfig.failoverTimeoutSeconds,

    autoRefreshEnabled = webViewConfig.autoRefreshEnabled,
    autoRefreshIntervalSec = webViewConfig.autoRefreshIntervalSec,
    autoRefreshShowCountdown = webViewConfig.autoRefreshShowCountdown
)

private fun WebApp.buildScreenAwakeBlock(): ScreenAwakeBlock = ScreenAwakeBlock(
    keepScreenOn = webViewConfig.keepScreenOn,
    mode = webViewConfig.screenAwakeMode.name,
    timeoutMinutes = webViewConfig.screenAwakeTimeoutMinutes,
    brightness = webViewConfig.screenBrightness
)

private fun WebApp.buildStatusBarBlock(): StatusBarBlock = StatusBarBlock(
    colorMode = webViewConfig.statusBarColorMode.name,
    color = webViewConfig.statusBarColor,
    darkIcons = webViewConfig.statusBarDarkIcons,
    backgroundType = webViewConfig.statusBarBackgroundType.name,
    backgroundImage = webViewConfig.statusBarBackgroundImage,
    backgroundAlpha = webViewConfig.statusBarBackgroundAlpha,
    heightDp = webViewConfig.statusBarHeightDp,
    colorModeDark = webViewConfig.statusBarColorModeDark.name,
    colorDark = webViewConfig.statusBarColorDark,
    darkIconsDark = webViewConfig.statusBarDarkIconsDark,
    backgroundTypeDark = webViewConfig.statusBarBackgroundTypeDark.name,
    backgroundImageDark = webViewConfig.statusBarBackgroundImageDark,
    backgroundAlphaDark = webViewConfig.statusBarBackgroundAlphaDark
)

private fun WebApp.buildFloatingWindowBlock(): FloatingWindowBlock {
    val fw = webViewConfig.floatingWindowConfig
    return FloatingWindowBlock(
        enabled = fw.enabled,
        windowSizePercent = fw.windowSizePercent,
        widthPercent = fw.widthPercent,
        heightPercent = fw.heightPercent,
        lockAspectRatio = fw.lockAspectRatio,
        aspectRatioMode = fw.aspectRatioMode.name,
        customAspectRatioWidth = fw.customAspectRatioWidth,
        customAspectRatioHeight = fw.customAspectRatioHeight,
        opacity = fw.opacity,
        cornerRadius = fw.cornerRadius,
        borderStyle = fw.borderStyle.name,
        minimizedIconPath = fw.minimizedIconPath,
        minimizedIconSizePercent = fw.minimizedIconSizePercent,
        minimizedIconEdgeDocking = fw.minimizedIconEdgeDocking,
        showTitleBar = fw.showTitleBar,
        autoHideTitleBar = fw.autoHideTitleBar,
        startMinimized = fw.startMinimized,
        rememberPosition = fw.rememberPosition,
        edgeSnapping = fw.edgeSnapping,
        showResizeHandle = fw.showResizeHandle,
        lockPosition = fw.lockPosition
    )
}

private fun WebApp.buildProxyBlock(): ProxyBlock = ProxyBlock(
    mode = webViewConfig.proxyMode,
    host = webViewConfig.proxyHost,
    port = webViewConfig.proxyPort,
    type = webViewConfig.proxyType,
    pacUrl = webViewConfig.pacUrl,
    bypassRules = webViewConfig.proxyBypassRules,
    username = webViewConfig.proxyUsername,
    password = webViewConfig.proxyPassword,
    hostsMappingEnabled = webViewConfig.hostsMappingEnabled,
    hostsMappings = webViewConfig.hostsMappings
)

private fun WebApp.buildDnsBlock(): DnsBlock = DnsBlock(
    mode = webViewConfig.dnsMode,
    config = DnsApkConfig(
        provider = webViewConfig.dnsConfig.provider,
        customDohUrl = webViewConfig.dnsConfig.customDohUrl,
        dohMode = webViewConfig.dnsConfig.dohMode,
        bypassSystemDns = webViewConfig.dnsConfig.bypassSystemDns,
        echEnabled = webViewConfig.dnsConfig.echEnabled
    )
)

private fun WebApp.buildTlsFingerprintBlock(): TlsFingerprintBlock = TlsFingerprintBlock(
    enabled = webViewConfig.tlsFingerprintEnabled,
    template = webViewConfig.tlsFingerprintTemplate,
    customCipherSuites = webViewConfig.tlsFingerprintCustomCiphers
)

private fun WebApp.buildErrorPageBlock(): ErrorPageBlock {
    val ep = webViewConfig.errorPageConfig
    return ErrorPageBlock(
        mode = ep.mode.name,
        builtInStyle = ep.builtInStyle.name,
        showMiniGame = ep.showMiniGame,
        miniGameType = ep.miniGameType.name,
        autoRetrySeconds = ep.autoRetrySeconds,
        customHtml = ep.customHtml ?: "",
        customMediaPath = resolveErrorPageMediaPath(ep.customMediaPath),
        retryButtonText = ep.retryButtonText,
        showHttp4xxErrorUi = ep.showHttp4xxErrorUi,
        showHttp5xxErrorUi = ep.showHttp5xxErrorUi,
        showNetworkErrorUi = ep.showNetworkErrorUi,
        showSslErrorUi = ep.showSslErrorUi,
        showRenderCrashErrorUi = ep.showRenderCrashErrorUi
    )
}

private fun resolveErrorPageMediaPath(path: String?): String {
    if (path.isNullOrBlank()) return ""
    if (path.startsWith("data:") || path.startsWith("http://") ||
        path.startsWith("https://") || path.startsWith("file://")) return path
    return try {
        val file = java.io.File(path)
        if (!file.exists() || !file.canRead()) return ""
        val isVideo = path.endsWith(".mp4") || path.endsWith(".webm")
        if (!isVideo && file.length() <= 2L * 1024 * 1024) {
            val base64 = android.util.Base64.encodeToString(file.readBytes(), android.util.Base64.NO_WRAP)
            "data:image/png;base64,$base64"
        } else {
            val ext = if (isVideo) {
                if (path.endsWith(".webm")) "webm" else "mp4"
            } else {
                "png"
            }
            "file:///android_asset/error_page_media.$ext"
        }
    } catch (e: Exception) {
        AppLogger.w("ApkBuilder", "Failed to resolve error page media: ${e.message}")
        ""
    }
}

private fun WebApp.buildSplashBlock(): SplashBlock = SplashBlock(
    enabled = splashEnabled,
    type = splashConfig?.type?.name ?: "IMAGE",
    duration = splashConfig?.duration ?: 3,
    clickToSkip = splashConfig?.clickToSkip ?: true,
    videoStartMs = splashConfig?.videoStartMs ?: 0L,
    videoEndMs = splashConfig?.videoEndMs ?: 5000L,
    landscape = splashConfig?.orientation == com.webtoapp.data.model.SplashOrientation.LANDSCAPE,
    fillScreen = splashConfig?.fillScreen ?: true,
    enableAudio = splashConfig?.enableAudio ?: false
)

private fun WebApp.buildMediaBlock(): MediaBlock = MediaBlock(
    enableAudio = mediaConfig?.enableAudio ?: true,
    loop = mediaConfig?.loop ?: true,
    autoPlay = mediaConfig?.autoPlay ?: true,
    fillScreen = mediaConfig?.fillScreen ?: true,
    landscape = mediaConfig?.orientation == com.webtoapp.data.model.SplashOrientation.LANDSCAPE,
    keepScreenOn = mediaConfig?.keepScreenOn ?: true
)

private fun com.webtoapp.data.model.HtmlConfig?.toHtmlBlock(): HtmlBlock = HtmlBlock(
    entryFile = this?.getValidEntryFile() ?: "index.html",
    enableJavaScript = this?.enableJavaScript ?: true,
    enableLocalStorage = this?.enableLocalStorage ?: true,
    backgroundColor = this?.backgroundColor ?: "#FFFFFF",
    loadMode = this?.loadMode?.name ?: HtmlLoadMode.AUTO.name,
    port = this?.port ?: 0,
    portConflictMode = this?.portConflictMode?.name ?: "AUTO_KILL"
)

private fun WebApp.buildHtmlBlock(): HtmlBlock = htmlConfig.toHtmlBlock()

private fun WebApp.buildGalleryBlock(): GalleryBlock {
    val items = galleryConfig?.items?.mapIndexed { index, item ->
        val ext = if (item.type == com.webtoapp.data.model.GalleryItemType.VIDEO) "mp4" else "png"
        GalleryShellItemConfig(
            id = item.id,
            assetPath = "gallery/item_$index.$ext",
            type = item.type.name,
            name = item.name,
            duration = item.duration,
            thumbnailPath = if (item.thumbnailPath != null) "gallery/thumb_$index.jpg" else null
        )
    } ?: emptyList()
    return GalleryBlock(
        items = items,
        playMode = galleryConfig?.playMode?.name ?: "SEQUENTIAL",
        imageInterval = galleryConfig?.imageInterval ?: 3,
        loop = galleryConfig?.loop ?: true,
        autoPlay = galleryConfig?.autoPlay ?: false,
        backgroundColor = galleryConfig?.backgroundColor ?: "#000000",
        showThumbnailBar = galleryConfig?.showThumbnailBar ?: true,
        showMediaInfo = galleryConfig?.showMediaInfo ?: true,
        orientation = galleryConfig?.orientation?.name ?: "PORTRAIT",
        enableAudio = galleryConfig?.enableAudio ?: true,
        videoAutoNext = galleryConfig?.videoAutoNext ?: true,
        shuffleOnLoop = galleryConfig?.shuffleOnLoop ?: false,
        defaultView = galleryConfig?.defaultView?.name ?: "GRID",
        gridColumns = galleryConfig?.gridColumns ?: 3,
        sortOrder = galleryConfig?.sortOrder?.name ?: "CUSTOM",
        rememberPosition = galleryConfig?.rememberPosition ?: true
    )
}

private fun WebApp.buildBgmBlock(): BgmBlock {
    val playlist = bgmConfig?.playlist?.mapIndexed { index, item ->
        val coverExt = item.coverPath?.let { path ->
            val raw = when {
                path.startsWith("asset:///") -> path.substringAfterLast('.', "jpg")
                else -> java.io.File(path).extension
            }.lowercase().ifBlank { "jpg" }
            when (raw) {
                "jpeg", "jpe", "jfif" -> "jpg"
                "png", "jpg", "webp", "gif", "bmp", "heic", "heif" -> raw
                else -> "jpg"
            }
        }
        BgmShellItem(
            id = item.id,
            name = item.name,
            assetPath = "bgm/bgm_$index.mp3",
            lrcAssetPath = if (item.lrcData != null) "bgm/bgm_$index.lrc" else null,
            coverAssetPath = coverExt?.let { "bgm/bgm_$index.$it" },
            sortOrder = item.sortOrder
        )
    } ?: emptyList()
    val theme = bgmConfig?.lrcTheme?.let {
        LrcShellTheme(
            id = it.id,
            name = it.name,
            fontSize = it.fontSize,
            textColor = it.textColor,
            highlightColor = it.highlightColor,
            backgroundColor = it.backgroundColor,
            animationType = it.animationType.name,
            position = it.position.name
        )
    }
    return BgmBlock(
        enabled = bgmEnabled,
        playlist = playlist,
        playMode = bgmConfig?.playMode?.name ?: "LOOP",
        volume = bgmConfig?.volume ?: 0.5f,
        autoPlay = bgmConfig?.autoPlay ?: true,
        showLyrics = bgmConfig?.showLyrics ?: true,
        lrcTheme = theme
    )
}

private fun WebApp.buildTranslateBlock(): TranslateBlock = TranslateBlock(
    enabled = translateEnabled,
    targetLanguage = translateConfig?.targetLanguage?.code ?: "zh-CN",
    showButton = translateConfig?.showFloatingButton ?: true
)

private fun WebApp.buildExtensionBlock(): ExtensionBlock = ExtensionBlock(
    enabled = extensionEnabled || extensionModuleIds.isNotEmpty(),
    moduleIds = extensionModuleIds,
    embeddedModules = emptyList(),
    fabIcon = extensionFabIcon ?: ""
)

private fun WebApp.buildAutoStartBlock(): AutoStartBlock = AutoStartBlock(
    enabled = false,
    bootStartEnabled = autoStartConfig?.bootStartEnabled ?: false,
    scheduledStartEnabled = autoStartConfig?.scheduledStartEnabled ?: false,
    scheduledTime = autoStartConfig?.scheduledTime ?: "08:00",
    scheduledDays = autoStartConfig?.scheduledDays ?: listOf(1, 2, 3, 4, 5, 6, 7),
    scheduledRepeat = autoStartConfig?.scheduledRepeat ?: true,
    bootDelay = autoStartConfig?.bootDelay ?: 5000L
)

private fun WebApp.buildOptionalServicesBlock(): OptionalServicesBlock = OptionalServicesBlock(
    forcedRunConfig = forcedRunConfig,
    isolationEnabled = apkExportConfig?.isolationConfig?.enabled ?: false,
    isolationConfig = apkExportConfig?.isolationConfig,
    backgroundRunEnabled = apkExportConfig?.backgroundRunEnabled ?: false,
    backgroundRunConfig = apkExportConfig?.backgroundRunConfig?.let {
        BackgroundRunConfig(
            notificationTitle = it.notificationTitle,
            notificationContent = it.notificationContent,
            showNotification = it.showNotification,
            keepCpuAwake = it.keepCpuAwake
        )
    },
    notificationEnabled = apkExportConfig?.notificationEnabled ?: false,
    notificationConfig = apkExportConfig?.notificationConfig?.let {
        NotificationConfig(
            type = it.type.key,
            pollUrl = it.pollUrl,
            pollIntervalMinutes = it.pollIntervalMinutes,
            pollMethod = it.pollMethod,
            pollHeaders = it.pollHeaders,
            clickUrl = it.clickUrl,
            wsUrl = it.wsUrl,
            wsHeaders = it.wsHeaders,
            registerUrl = it.registerUrl,
            registerHeaders = it.registerHeaders,
            authToken = it.authToken,
            fcmProjectId = it.fcmProjectId,
            fcmApplicationId = it.fcmApplicationId,
            fcmApiKey = it.fcmApiKey,
            fcmSenderId = it.fcmSenderId,
            fcmGoogleServicesJson = it.fcmGoogleServicesJson
        )
    },
    hardeningEnabled = apkExportConfig?.encryptionConfig?.enabled ?: false,
    hardeningThreatResponse = (apkExportConfig?.encryptionConfig?.threatResponse
        ?: com.webtoapp.data.model.ApkEncryptionConfig.ThreatResponse.LOG_ONLY).name
)

private fun WebApp.buildDisguiseBlock(): DisguiseBlock = DisguiseBlock(
    blackTechConfig = blackTechConfig,
    disguiseConfig = disguiseConfig,
    browserDisguiseConfig = browserDisguiseConfig,
    deviceDisguiseConfig = deviceDisguiseConfig
)

private fun WebApp.buildDeepLinkBlock(packageName: String): DeepLinkBlock = DeepLinkBlock(
    enabled = apkExportConfig?.deepLinkEnabled ?: false,
    hosts = buildOAuthReturnHosts(
        url = url,
        customHosts = apkExportConfig?.customDeepLinkHosts ?: emptyList(),
        includeCustomHosts = apkExportConfig?.deepLinkEnabled == true
    ),
    schemes = buildOAuthReturnSchemes(packageName, appType)
)

private fun WebApp.buildWordpressBlock(): WordpressBlock = WordpressBlock(
    siteTitle = wordpressConfig?.siteTitle ?: "",
    adminUser = wordpressConfig?.adminUser ?: "admin",
    adminEmail = wordpressConfig?.adminEmail ?: "",
    adminPassword = wordpressConfig?.adminPassword ?: "admin",
    themeName = wordpressConfig?.themeName ?: "",
    plugins = wordpressConfig?.plugins ?: emptyList(),
    activePlugins = wordpressConfig?.activePlugins ?: emptyList(),
    permalinkStructure = wordpressConfig?.permalinkStructure ?: "/%postname%/",
    siteLanguage = wordpressConfig?.siteLanguage ?: "zh_CN",
    autoInstall = wordpressConfig?.autoInstall ?: true,
    phpPort = wordpressConfig?.phpPort ?: 0,
    customPhpExtensions = wordpressConfig?.customPhpExtensions ?: emptyList()
)

private fun WebApp.buildNodejsBlock(): NodejsBlock = NodejsBlock(
    mode = nodejsConfig?.buildMode?.name ?: "STATIC",
    port = nodejsConfig?.serverPort ?: 0,
    entryFile = nodejsConfig?.entryFile ?: "",
    envVars = nodejsConfig?.envVars ?: emptyMap(),
    customNodeExtensions = nodejsConfig?.customNodeExtensions ?: emptyList()
)

private fun WebApp.buildPhpAppBlock(): PhpAppBlock = PhpAppBlock(
    framework = phpAppConfig?.framework ?: "",
    documentRoot = phpAppConfig?.documentRoot ?: "",
    entryFile = phpAppConfig?.entryFile ?: "index.php",
    port = phpAppConfig?.phpPort ?: 0,
    envVars = phpAppConfig?.envVars ?: emptyMap(),
    phpExtensions = phpAppConfig?.phpExtensions ?: emptyMap(),
    customPhpExtensions = phpAppConfig?.customPhpExtensions ?: emptyList()
)

private fun WebApp.buildPythonAppBlock(): PythonAppBlock = PythonAppBlock(
    framework = pythonAppConfig?.framework ?: "",
    entryFile = pythonAppConfig?.entryFile ?: "app.py",
    entryModule = pythonAppConfig?.entryModule ?: "",
    serverType = pythonAppConfig?.serverType ?: "builtin",
    port = pythonAppConfig?.serverPort ?: 0,
    envVars = pythonAppConfig?.envVars ?: emptyMap(),
    customPythonExtensions = pythonAppConfig?.customPythonExtensions ?: emptyList()
)

private fun WebApp.buildGoAppBlock(): GoAppBlock = GoAppBlock(
    framework = goAppConfig?.framework ?: "",
    binaryName = goAppConfig?.binaryName ?: "",
    targetArch = goAppConfig?.targetArch ?: "arm64-v8a",
    port = goAppConfig?.serverPort ?: 0,
    staticDir = goAppConfig?.staticDir ?: "",
    envVars = goAppConfig?.envVars ?: emptyMap()
)

private fun WebApp.buildMultiWebBlock(context: android.content.Context?, packageName: String): MultiWebBlock {
    val repo = context?.let {
        try {
            org.koin.java.KoinJavaComponent.get<com.webtoapp.data.repository.WebAppRepository>(
                com.webtoapp.data.repository.WebAppRepository::class.java
            )
        } catch (_: Exception) { null }
    }
    val sites = multiWebConfig?.sites?.map { site ->
        val siteShellConfig = if (repo != null && site.sourceAppId > 0) {
            kotlinx.coroutines.runBlocking { repo.getWebApp(site.sourceAppId) }?.let { sourceApp ->
                buildSiteShellConfig(sourceApp, packageName, site.id, context)
            }
        } else null
        com.webtoapp.core.shell.MultiWebSiteShellConfig(
            id = site.id,
            name = site.name,
            url = site.url,
            type = site.type,
            localFilePath = site.localFilePath,
            iconEmoji = site.iconEmoji,
            category = site.category,
            cssSelector = site.cssSelector,
            linkSelector = site.linkSelector,
            enabled = site.enabled,
            sourceAppId = site.sourceAppId,
            sourceProjectId = site.sourceProjectId,
            faviconUrl = site.faviconUrl,
            themeColor = site.themeColor,
            sortIndex = site.sortIndex,
            appType = site.appType,
            siteProjectId = site.siteProjectId,
            siteShellConfig = siteShellConfig
        )
    } ?: emptyList()
    return MultiWebBlock(
        sites = sites,
        displayMode = multiWebConfig?.displayMode ?: "TABS",
        refreshInterval = multiWebConfig?.refreshInterval ?: 30,
        showSiteIcons = multiWebConfig?.showSiteIcons ?: true,
        projectId = multiWebConfig?.projectId ?: ""
    )
}

internal fun buildSiteShellConfig(
    sourceWebApp: WebApp,
    parentPkg: String,
    siteId: String,
    context: android.content.Context?,
    isPreview: Boolean = false
): com.webtoapp.core.shell.ShellConfig {
    require(sourceWebApp.appType != com.webtoapp.data.model.AppType.MULTI_WEB) { "MultiWeb site source cannot be MULTI_WEB" }
    val sitePkg = "${parentPkg}_site_$siteId"
    val apkConfig = sourceWebApp.toApkConfigWithModules(sitePkg, context!!)
    val json = ApkConfigJsonFactory.toShellConfigJson(apkConfig)
    val shell = com.webtoapp.util.GsonProvider.gson.fromJson(json, com.webtoapp.core.shell.ShellConfig::class.java)
    val assetBase = when (sourceWebApp.appType) {
        com.webtoapp.data.model.AppType.HTML, com.webtoapp.data.model.AppType.FRONTEND -> "html"
        com.webtoapp.data.model.AppType.NODEJS_APP -> "nodejs_app"
        com.webtoapp.data.model.AppType.PHP_APP -> "php_app"
        com.webtoapp.data.model.AppType.PYTHON_APP -> "python_app"
        com.webtoapp.data.model.AppType.GO_APP -> "go_app"
        com.webtoapp.data.model.AppType.WORDPRESS -> "wordpress"
        else -> ""
    }
    val siteDirName = if (isPreview) {
        "html_projects/${sourceWebApp.htmlConfig?.projectId ?: siteId}"
    } else {
        "multiweb_$siteId"
    }
    return shell.copy(
        siteId = siteId,
        siteDirName = siteDirName,
        siteAssetBase = assetBase,
        packageName = sitePkg,
        multiWebConfig = com.webtoapp.core.shell.MultiWebShellConfig()
    )
}

private fun extractHostsFromUrl(url: String, customHosts: List<String> = emptyList()): List<String> {

    val secondLevelTlds = setOf(
        "co.uk", "org.uk", "ac.uk", "gov.uk",
        "com.au", "net.au", "org.au", "edu.au",
        "co.jp", "or.jp", "ne.jp", "ac.jp",
        "com.cn", "net.cn", "org.cn", "edu.cn",
        "com.br", "org.br", "net.br",
        "co.kr", "or.kr", "ne.kr",
        "co.in", "net.in", "org.in",
        "com.tw", "org.tw", "net.tw",
        "co.nz", "org.nz", "net.nz",
        "com.hk", "org.hk", "net.hk",
        "com.sg", "org.sg", "net.sg",
        "co.za", "org.za", "net.za",
        "com.mx", "org.mx", "net.mx",
        "com.ar", "org.ar", "net.ar",
        "co.id", "or.id", "web.id",
        "com.my", "org.my", "net.my",
        "co.th", "or.th", "in.th"
    )

    fun getApexDomain(host: String): String {
        val parts = host.split(".")
        if (parts.size <= 2) return host
        val lastTwo = parts.takeLast(2).joinToString(".")
        return if (lastTwo in secondLevelTlds && parts.size > 2) {
            parts.takeLast(3).joinToString(".")
        } else {
            lastTwo
        }
    }

    val hosts = mutableSetOf<String>()
    try {
        val uri = android.net.Uri.parse(url)
        val host = uri.host?.lowercase()
        if (!host.isNullOrBlank() && host != "localhost" && !host.matches(Regex("^\\d+\\.\\d+\\.\\d+\\.\\d+$"))) {
            hosts.add(host)
            val apex = getApexDomain(host)

            if (host != apex) {
                hosts.add(apex)
            }

            val wwwApex = "www.$apex"
            if (host != wwwApex) {
                hosts.add(wwwApex)
            }
        }
    } catch (_: Exception) { }

    customHosts.forEach { custom ->
        val trimmed = custom.trim().lowercase()
        if (trimmed.isNotBlank() && trimmed.contains(".")) {
            hosts.add(trimmed)
        }
    }

    return hosts.toList()
}

private fun buildOAuthReturnHosts(
    url: String,
    customHosts: List<String> = emptyList(),
    includeCustomHosts: Boolean = false
): List<String> {
    return extractHostsFromUrl(
        url = url,
        customHosts = if (includeCustomHosts) customHosts else emptyList()
    )
}

private fun buildOAuthReturnSchemes(packageName: String, appType: com.webtoapp.data.model.AppType): List<String> {
    if (appType != com.webtoapp.data.model.AppType.WEB) return emptyList()
    val normalized = packageName.lowercase()
        .map { ch -> if (ch.isLetterOrDigit()) ch else '-' }
        .joinToString("")
        .trim('-')
    if (normalized.isBlank()) return emptyList()
    return listOf("wta-$normalized")
}


fun WebApp.toPreviewShellConfig(context: android.content.Context): com.webtoapp.core.shell.ShellConfig {
    val packageName = this.packageName?.takeIf { it.isNotBlank() } ?: context.packageName
    val apkConfig = toApkConfigWithModules(packageName, context)
    val json = ApkConfigJsonFactory.toShellConfigJson(apkConfig)
    val shell = com.webtoapp.util.GsonProvider.gson.fromJson(json, com.webtoapp.core.shell.ShellConfig::class.java)
        ?: error("Failed to map WebApp to ShellConfig for preview")
    return shell
}

fun WebApp.toApkConfigWithModules(packageName: String, context: android.content.Context): ApkConfig {
    val baseConfig = toApkConfig(packageName, context)
    val extensionFileManager = com.webtoapp.core.extension.ExtensionFileManager(context)

    val embeddedModules = if (extensionModuleIds.isNotEmpty()) {
        try {
            val extensionManager = com.webtoapp.core.extension.ExtensionManager.getInstance(context)

            kotlinx.coroutines.runBlocking {
                kotlinx.coroutines.withTimeoutOrNull(5000L) {
                    extensionManager.awaitLoaded()
                }
            }

            var resolvedModules = extensionManager.getModulesByIds(extensionModuleIds)
            val foundIds = resolvedModules.map { it.id }.toSet()
            val missingIds = extensionModuleIds.filter { it !in foundIds }
            if (missingIds.isNotEmpty()) {
                val fallbackBuiltIns = runCatching {
                    com.webtoapp.core.extension.BuiltInModules.getAll()
                        .filter { it.id in missingIds.toSet() }
                }.getOrElse { emptyList() }
                if (fallbackBuiltIns.isNotEmpty()) {
                    AppLogger.w(
                        "ApkBuilder",
                        "Extension module soft-load missed ${missingIds.size} id(s); recovered ${fallbackBuiltIns.size} via BuiltInModules hard-ref"
                    )
                    resolvedModules = resolvedModules + fallbackBuiltIns
                }
                val stillMissing = extensionModuleIds.filter { id -> resolvedModules.none { it.id == id } }
                if (stillMissing.isNotEmpty()) {
                    AppLogger.w(
                        "ApkBuilder",
                        "Extension module resolution: requested ${extensionModuleIds.size}, found ${resolvedModules.size}. " +
                            "Missing IDs (will NOT be embedded in APK): $stillMissing"
                    )
                }
            }

            resolvedModules.map { module ->
                val resolvedRequireContents = linkedMapOf<String, String>()
                module.requireUrls.forEach { url ->
                    extensionFileManager.getCachedRequire(url)?.let { resolvedRequireContents[url] = it }
                }

                val resolvedResources = linkedMapOf<String, String>()
                module.resources.forEach { (name, url) ->
                    resolvedResources[name] = extensionFileManager.getCachedResource(name, url) ?: url
                }

                val resolvedCode: String
                val resolvedCss: String
                if (module.codeFiles.isNotEmpty()) {
                    val entryNames = setOf("main.js", "index.js", "app.js", "init.js", "bundle.js", "dist.js")
                    val jsFiles = module.codeFiles.entries
                        .filter { it.key.endsWith(".js", true) }
                        .sortedWith(compareByDescending<Map.Entry<String, String>> {
                            it.key.substringAfterLast("/") in entryNames
                        }.thenBy { it.key })
                    val cssFiles = module.codeFiles.entries
                        .filter { it.key.endsWith(".css", true) }
                    resolvedCode = jsFiles.joinToString("\n\n") { (path, content) ->
                        "// === $path ===\n$content"
                    }
                    resolvedCss = if (cssFiles.isNotEmpty()) {
                        val baseCss = module.cssCode
                        val mergedCss = cssFiles.joinToString("\n\n") { (path, content) ->
                            "/* === $path === */\n$content"
                        }
                        if (baseCss.isNotBlank()) "$baseCss\n\n$mergedCss" else mergedCss
                    } else {
                        module.cssCode
                    }
                } else {
                    resolvedCode = module.code
                    resolvedCss = module.cssCode
                }

                EmbeddedExtensionModule(
                    id = module.id,
                    name = module.name,
                    description = module.description,
                    icon = module.icon,
                    category = module.category.name,
                    versionName = module.version.name,
                    authorName = module.author?.name.orEmpty(),
                    code = resolvedCode,
                    cssCode = resolvedCss,
                    runAt = module.runAt.name,
                    sourceType = module.sourceType.name,
                    runMode = module.runMode.name,
                    uiConfig = EmbeddedExtensionModuleUiConfig(
                        type = module.uiConfig.type.name,
                        autoHide = module.uiConfig.autoHide,
                        autoHideDelay = module.uiConfig.autoHideDelay,
                        initiallyHidden = module.uiConfig.initiallyHidden,
                        showOnlyOnMatch = module.uiConfig.showOnlyOnMatch
                    ),
                    urlMatches = module.urlMatches.map { rule ->
                        EmbeddedUrlMatchRule(
                            pattern = rule.pattern,
                            isRegex = rule.isRegex,
                            exclude = rule.exclude
                        )
                    },
                    configValues = module.configValues,
                    configItemCount = module.configItems.size,
                    gmGrants = module.gmGrants,
                    requireUrls = module.requireUrls,
                    requireContents = resolvedRequireContents,
                    resources = resolvedResources,
                    noframes = module.noframes,

                    enabled = true
                )
            }
        } catch (e: Exception) {
            AppLogger.e("ApkBuilder", "Failed to get extension module data", e)
            emptyList()
        }
    } else {
        emptyList()
    }

    val extensionActive = baseConfig.extension.enabled ||
        extensionModuleIds.isNotEmpty() ||
        embeddedModules.isNotEmpty()
    return baseConfig.copy(
        extension = baseConfig.extension.copy(
            enabled = extensionActive,
            moduleIds = if (baseConfig.extension.moduleIds.isNotEmpty()) {
                baseConfig.extension.moduleIds
            } else {
                extensionModuleIds
            },
            embeddedModules = embeddedModules
        )
    )
}

fun WebApp.getSplashMediaPath(): String? {
    return if (splashEnabled) splashConfig?.mediaPath else null
}

enum class BuildStage(val label: String) {
    PREPARE("Preparing build"),
    RESOURCE_PREP("Preparing resources"),
    INPUT_PRECHECK("Checking build inputs"),
    TEMPLATE("Loading template APK"),
    MODIFY_APK("Modifying APK"),
    ARTIFACT_VERIFY("Verifying APK artifact"),
    SIGN("Signing APK"),
    VERIFY("Verifying APK"),
    ANALYZE_CLEANUP("Analyzing and cleaning up")
}

enum class BuildFailureCause {
    TEMPLATE_UNAVAILABLE,
    INPUT_PRECHECK_FAILED,
    UNSIGNED_OUTPUT_INVALID,
    ARTIFACT_VERIFICATION_FAILED,
    SIGNING_EXCEPTION,
    SIGNED_OUTPUT_INVALID,
    UNHANDLED_EXCEPTION
}

data class BuildDiagnostic(
    val stage: BuildStage,
    val cause: BuildFailureCause,
    val details: Map<String, Any?> = emptyMap()
)

sealed class BuildResult {
    data class Success(
        val apkFile: File,
        val logPath: String? = null,
        val analysisReport: ApkAnalyzer.AnalysisReport? = null,
        val incremental: Boolean = false,
        val buildMode: String = "FULL",
        val buildReason: String = ""
    ) : BuildResult()
    data class Error(
        val message: String,
        val logPath: String? = null,
        val diagnostic: BuildDiagnostic? = null
    ) : BuildResult()
}
