package com.webtoapp.core.backup

import android.content.Context
import android.net.Uri
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.apkbuilder.BuildMetadataWriter
import com.webtoapp.core.apkbuilder.JarSigner
import com.webtoapp.core.apkbuilder.PerAppSigningIdentity
import com.webtoapp.data.model.PublishedPerAppSigning
import com.webtoapp.core.logging.AppLogger
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.data.converter.Converters
import com.webtoapp.data.database.AppDatabase
import com.webtoapp.data.model.AppCategory
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.WebAppRepository
import com.webtoapp.core.stats.AppUsageStats
import com.webtoapp.util.threadLocalCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CancellationException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.coroutines.coroutineContext

internal class ProgressThrottle(
    private val minIntervalMs: Long = 150,
    private val clockMs: () -> Long = System::currentTimeMillis
) {
    private var lastEmitMs: Long = -minIntervalMs

    fun shouldEmit(force: Boolean = false): Boolean {
        if (force) {
            lastEmitMs = clockMs()
            return true
        }
        val now = clockMs()
        if (now - lastEmitMs < minIntervalMs) return false
        lastEmitMs = now
        return true
    }
}

class DataBackupManager(private val context: Context) {

    companion object {
        private const val TAG = "DataBackupManager"

        private const val BACKUP_VERSION = 6
        internal const val APPS_JSON = "apps.json"
        internal const val SOURCE_BUNDLE_CONFIG = "app/src/main/assets/app_config.json"
        private const val RESOURCES_DIR = "resources/"
        private const val ICONS_DIR = "resources/icons/"
        private const val SPLASH_DIR = "resources/splash/"
        private const val BGM_DIR = "resources/bgm/"
        private const val BGM_LRC_DIR = "resources/bgm_lrc/"
        private const val BGM_COVER_DIR = "resources/bgm_cover/"
        private const val HTML_DIR = "resources/html/"
        private const val MEDIA_DIR = "resources/media/"
        private const val STATUSBAR_DIR = "resources/statusbar/"
        private const val GALLERY_DIR = "resources/gallery/"
        private const val CERTS_DIR = "resources/certs/"
        private const val MULTI_WEB_DIR = "resources/multi_web/"
        private const val ANNOUNCEMENT_DIR = "resources/announcement/"
        private const val EXTENSION_DIR = "extensions/"
        private const val EXTENSION_MODULES_FILE = "${EXTENSION_DIR}modules.json"
        private const val EXTENSION_BUILTIN_STATES_FILE = "${EXTENSION_DIR}builtin_states.json"
        private const val LOCAL_FILES_DIR = "local/files/"
        private const val LOCAL_EXTERNAL_FILES_DIR = "local/external_files/"
        private const val DATASTORE_DIR = "local/datastore/"
        private const val SHARED_PREFS_DIR = "local/shared_prefs/"
        private const val KEYSTORE_DIR = "local/keystores/"
        private const val ADBLOCK_DIR = "local/adblock/"

        private val MANAGED_FILES_DIRS = listOf(

            "extension_modules",
            "extensions",
            "plugins",
            "plugin_config",
            "html_projects",
            "splash_media",
            "website_icons",
            "custom_ca",
            "aicoding",
            "app_icons",
            "media_apps",

            "nodejs_projects",
            "php_projects",
            "python_projects",
            "go_projects",
            "wordpress_projects",

            "frontend_builds",
            "docs_projects",
            "scraped_sites"
        )

        private val MANAGED_FILES_EXCLUDED_SEGMENTS = setOf(
            ".git",
            ".cache",
            ".changes",
            "__pycache__",
            ".pytest_cache",
            ".mypy_cache",
            "tmp"
        )

        private val MANAGED_EXTERNAL_DIRS = emptyList<String>()

        private val SAFE_DATASTORE_NAMES = listOf(
            "language_settings",
            "theme_settings",
            "announcement",
            "activation",
            "ai_config",
            "aicoding_sessions_v1",
            "aicoding_prefs",
            "host_runtime_prefs"
        )

        private const val AI_CONFIG_DATASTORE_ENTRY = "${DATASTORE_DIR}ai_config.preferences_pb"
        private const val ACTIVATION_DATASTORE_ENTRY = "${DATASTORE_DIR}activation.preferences_pb"
        private const val HOST_RUNTIME_DATASTORE_ENTRY = "${DATASTORE_DIR}host_runtime_prefs.preferences_pb"
        private const val GALLERY_POSITIONS_PREFS_ENTRY = "${SHARED_PREFS_DIR}gallery_positions.xml"

        private val ACTIVATION_KEY_PREFIXES = listOf(
            "activated_time_", "activated_",
            "expire_time_", "usage_count_", "usage_limit_",
            "device_id_", "code_type_", "used_code_",
            "failed_attempts_", "lockout_until_",
            "remote_code_", "remote_expires_", "remote_verified_at_", "remote_url_"
        )

        private val HOST_RUNTIME_MCP_KEYS = setOf("host_mcp_enabled", "host_mcp_token", "host_mcp_port")

        private val SAFE_SHARED_PREF_NAMES = listOf(
            "installed_store_items",
            "config_presets",
            "permission_presets",
            "floating_window_prefs",
            "isolation_prefs",
            "chrome_extension_storage",
            "chrome_extension_content_scripts",
            "gallery_positions"
        )

        private val KEYSTORE_FILES = listOf(
            "webtoapp_keystore.p12",
            "custom_keystore.p12",
            "custom_keystore_alias.txt",
            "signing_scheme_options.json"
        )

        private val ADBLOCK_FILES = listOf(
            "adblock_hosts.txt",
            "adblock_hosts_sources.txt"
        )


        private val SAFE_SHARED_PREF_PREFIXES = listOf(
            "gm_storage_"
        )

        private const val BUFFER_SIZE = 8192

        private const val MAX_JSON_ENTRY_BYTES = 64L * 1024 * 1024

        private val backupDateFormat = threadLocalCompat {
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        }

        private val gson: Gson by lazy {
            Converters.gson
        }
    }

    data class BackupData(
        val version: Int = BACKUP_VERSION,
        val exportTime: Long = System.currentTimeMillis(),
        val appCount: Int = 0,
        val apps: List<WebApp> = emptyList(),
        val categories: List<AppCategory> = emptyList(),

        val usageStats: List<AppUsageStats> = emptyList(),

        val healthRecords: List<com.webtoapp.core.stats.AppHealthRecord> = emptyList()
    )

    data class AppBackupPayload(
        val appsJson: String,
        val resources: Map<String, File>,
        val projectFiles: Map<String, File>
    )

    fun buildAppBackupPayload(app: WebApp): AppBackupPayload {
        val resources = mutableMapOf<String, String>()
        collectAppResources(app, resources)
        val relativeApp = updateAppPathsToRelative(app, resources)
        val shareableApp = relativeApp.copy(
            categoryId = null,
            apkExportConfig = relativeApp.apkExportConfig?.copy(publishedPerAppSigning = null)
        )
        val backupData = BackupData(
            version = 5,
            exportTime = System.currentTimeMillis(),
            appCount = 1,
            apps = listOf(shareableApp)
        )
        val projectFiles = linkedMapOf<String, File>()
        appProjectDirs(app).forEach { (dirName, projectId) ->
            collectShareableProjectFiles(
                sourceDir = File(context.filesDir, "$dirName/$projectId"),
                zipRoot = "$LOCAL_FILES_DIR$dirName/$projectId/",
                files = projectFiles
            )
        }
        return AppBackupPayload(
            appsJson = gson.toJson(backupData),
            resources = resources.mapValues { File(it.value) },
            projectFiles = projectFiles
        )
    }

    private fun collectShareableProjectFiles(
        sourceDir: File,
        zipRoot: String,
        files: MutableMap<String, File>
    ) {
        if (!sourceDir.exists() || !sourceDir.isDirectory) return
        val base = sourceDir.canonicalFile
        val basePath = base.path + File.separator
        sourceDir.walkTopDown()
            .onEnter { child ->
                child.name != "node_modules" && child.name !in MANAGED_FILES_EXCLUDED_SEGMENTS
            }
            .filter { it.isFile && it.canRead() }
            .forEach { file ->
                val canonical = file.canonicalFile
                if (canonical == base || !canonical.path.startsWith(basePath)) return@forEach
                val relativePath = file.relativeTo(sourceDir).invariantSeparatorsPath
                if (isExcludedManagedPath(relativePath)) return@forEach
                files["$zipRoot$relativePath"] = file
            }
    }

    private fun appProjectDirs(app: WebApp): List<Pair<String, String>> {
        val dirs = mutableListOf<Pair<String, String>>()
        fun add(dirName: String, projectId: String?) {
            if (!projectId.isNullOrBlank()) dirs += dirName to projectId
        }
        when (app.appType) {
            AppType.NODEJS_APP -> add("nodejs_projects", app.nodejsConfig?.projectId)
            AppType.PHP_APP -> add("php_projects", app.phpAppConfig?.projectId)
            AppType.PYTHON_APP -> add("python_projects", app.pythonAppConfig?.projectId)
            AppType.GO_APP -> add("go_projects", app.goAppConfig?.projectId)
            AppType.WORDPRESS -> {
                add("wordpress_projects", app.wordpressConfig?.projectId)
                add("html_projects", app.wordpressConfig?.sourceProjectId)
            }
            AppType.HTML, AppType.FRONTEND -> add("html_projects", app.htmlConfig?.projectId)
            AppType.MULTI_WEB -> {
                add("html_projects", app.multiWebConfig?.projectId)
                app.multiWebConfig?.sites.orEmpty().forEach { site ->
                    add("html_projects", site.sourceProjectId)
                    add("html_projects", site.siteProjectId)
                    add("nodejs_projects", site.nodejsConfig?.projectId)
                    add("php_projects", site.phpAppConfig?.projectId)
                    add("python_projects", site.pythonAppConfig?.projectId)
                    add("go_projects", site.goAppConfig?.projectId)
                    add("wordpress_projects", site.wordpressConfig?.projectId)
                }
            }
            else -> Unit
        }
        return dirs.distinct()
    }

    suspend fun exportAllData(
        repository: WebAppRepository,
        outputUri: Uri,
        signingPassword: String? = null,
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> }
    ): Result<ExportResult> = withContext(Dispatchers.IO) {
        try {
            onProgress(0, 100, Strings.backupReadingData)
            coroutineContext.ensureActive()

            val apps = repository.allWebApps.first()
            AppLogger.i(TAG, "准备导出 ${apps.size} 个应用")

            val categories = AppDatabase.getInstance(context).appCategoryDao().getAllCategories().first()
            val usageStats = runCatching {
                AppDatabase.getInstance(context).appUsageStatsDao().getAllStats().first()
            }.getOrElse {
                AppLogger.w(TAG, "读取使用统计失败，本次备份不含统计数据", it)
                emptyList()
            }
            val healthRecords = runCatching {
                AppDatabase.getInstance(context).appUsageStatsDao().getAllHealthRecords()
            }.getOrElse {
                AppLogger.w(TAG, "读取健康记录失败，本次备份不含健康数据", it)
                emptyList<com.webtoapp.core.stats.AppHealthRecord>()
            }

            val resourceFiles = mutableMapOf<String, String>()
            val collectThrottle = ProgressThrottle()

            apps.forEachIndexed { index, app ->
                coroutineContext.ensureActive()
                if (collectThrottle.shouldEmit()) {
                    onProgress(10 + (index * 30 / apps.size), 100, Strings.backupCollectingResources.format(app.name))
                }
                collectAppResources(app, resourceFiles)
            }

            AppLogger.i(TAG, "收集到 ${resourceFiles.size} 个资源文件")

            val signingKeys = signingPassword?.let { linkedMapOf<String, JarSigner.SigningIdentity>() }
            val appsWithRelativePaths = apps.map { app ->
                coroutineContext.ensureActive()
                updateAppPathsToRelative(appWithSigningReference(app, signingKeys), resourceFiles)
            }
            val signingPayload = signingKeys?.takeIf { it.isNotEmpty() }?.let {
                PerAppSigningBackup.encrypt(it, checkNotNull(signingPassword))
            }

            val backupData = BackupData(
                version = BACKUP_VERSION,
                exportTime = System.currentTimeMillis(),
                appCount = apps.size,
                apps = appsWithRelativePaths,
                categories = categories,
                usageStats = usageStats,
                healthRecords = healthRecords
            )
            val localFiles = collectLocalBackupFiles()

            onProgress(50, 100, Strings.backupCreatingFile)
            coroutineContext.ensureActive()

            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream, BUFFER_SIZE)).use { zipOut ->

                    val jsonBytes = gson.toJson(backupData).toByteArray(Charsets.UTF_8)
                    zipOut.putNextEntry(ZipEntry(APPS_JSON))
                    zipOut.write(jsonBytes)
                    zipOut.closeEntry()

                    if (signingPayload != null) {
                        zipOut.putNextEntry(ZipEntry(PerAppSigningBackup.ENTRY))
                        zipOut.write(signingPayload)
                        zipOut.closeEntry()
                    }

                    val totalFileCount = resourceFiles.size + localFiles.size
                    val writtenZipPaths = HashSet<String>(totalFileCount * 2)
                    var processedFiles = 0
                    val packThrottle = ProgressThrottle()

                    fun writeZipFile(zipPath: String, file: File) {

                        if (!writtenZipPaths.add(zipPath)) return
                        if (!file.exists() || !file.canRead()) return
                        zipOut.putNextEntry(ZipEntry(zipPath))
                        file.inputStream().buffered(BUFFER_SIZE).use { input ->
                            input.copyTo(zipOut, BUFFER_SIZE)
                        }
                        zipOut.closeEntry()
                    }

                    fun reportPackaging() {
                        processedFiles++
                        if (!packThrottle.shouldEmit()) return
                        val pct = if (totalFileCount > 0) {
                            50 + (processedFiles * 45 / totalFileCount)
                        } else 95
                        onProgress(pct, 100, Strings.backupPackaging)
                    }

                    resourceFiles.forEach { (zipPath, localPath) ->
                        coroutineContext.ensureActive()
                        reportPackaging()
                        try {
                            writeZipFile(zipPath, File(localPath))
                        } catch (e: Exception) {
                            AppLogger.w(TAG, "无法打包资源文件: $localPath", e)
                        }
                    }

                    localFiles.forEach { (zipPath, file) ->
                        coroutineContext.ensureActive()
                        reportPackaging()
                        try {
                            writeZipFile(zipPath, file)
                        } catch (e: Exception) {
                            AppLogger.w(TAG, "无法打包本地数据: ${file.absolutePath}", e)
                        }
                    }
                }
            } ?: return@withContext Result.failure(Exception(Strings.backupOutputFailed))

            onProgress(100, 100, Strings.backupExportComplete)

            Result.success(ExportResult(
                appCount = apps.size,
                resourceCount = resourceFiles.size + localFiles.size,
                signingKeyCount = signingKeys?.size ?: 0,
                missingSigningKeys = if (signingKeys != null) {
                    appsWithRelativePaths.count { app ->
                        val reference = app.apkExportConfig?.publishedPerAppSigning
                        if (reference == null) return@count false
                        val archived = signingKeys[reference.packageName] ?: return@count true
                        reference.certificateSha256 != PerAppSigningIdentity.fingerprint(archived.certificate)
                    }
                } else 0
            ))

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, "Export failed", e)
            Result.failure(e)
        }
    }

    private fun appWithSigningReference(
        app: WebApp,
        signingKeys: MutableMap<String, JarSigner.SigningIdentity>?
    ): WebApp {
        val legacyPackageName = BuildMetadataWriter.findLegacyAutomaticPackageName(context, app)
        val project = if (legacyPackageName != null) app.copy(packageName = legacyPackageName) else app
        val config = project.apkExportConfig ?: return project
        val previous = config.publishedPerAppSigning
        if (!config.perAppSigningEnabled && previous == null) return project

        val activePackage = ApkBuilder.resolvePackageName(project)
        var packageName = if (config.perAppSigningEnabled) activePackage else checkNotNull(previous).packageName
        var existing = runCatching { PerAppSigningIdentity.readExisting(context, packageName) }
        if (existing.isSuccess && existing.getOrNull() == null && previous != null &&
            previous.packageName != packageName) {
            packageName = previous.packageName
            existing = runCatching { PerAppSigningIdentity.readExisting(context, packageName) }
        }
        val reference = previous?.takeIf { it.packageName == packageName }
        val identity = existing.getOrNull()
        val fingerprint = identity?.certSha256Hex()
        val sameCertificate = reference?.certificateSha256.let { it == null || it == fingerprint }
        if (signingKeys != null && sameCertificate && identity != null) {
            signingKeys[packageName] = identity.toSigningIdentity()
        }

        val published = when {
            reference != null && fingerprint == null -> reference
            !sameCertificate -> reference
            identity != null -> PublishedPerAppSigning(packageName, fingerprint)
            existing.isFailure -> PublishedPerAppSigning(packageName, null)
            else -> return project
        }
        val pinnedPackage = packageName.takeIf {
            ApkBuilder.customPackageName(project) == null && activePackage == packageName
        }
        return project.copy(
            packageName = project.packageName ?: pinnedPackage,
            apkExportConfig = config.copy(publishedPerAppSigning = published)
        )
    }

    private fun restoreSigningReferences(
        data: BackupData,
        sourceBundle: Boolean,
        signingKeys: Map<String, JarSigner.SigningIdentity>
    ): List<WebApp> {
        check(!sourceBundle || signingKeys.isEmpty()) { Strings.signingBackupInvalid }
        val apps = data.apps.map { app ->
            val config = app.apkExportConfig
            if (sourceBundle || data.version >= 6 || config?.perAppSigningEnabled != true ||
                config.publishedPerAppSigning != null) {
                app
            } else {
                val packageName = ApkBuilder.resolvePackageName(app)
                app.copy(
                    packageName = app.packageName ?: packageName.takeIf { ApkBuilder.customPackageName(app) == null },
                    apkExportConfig = config.copy(publishedPerAppSigning = PublishedPerAppSigning(packageName, null))
                )
            }
        }
        val references = apps.mapNotNull { it.apkExportConfig?.publishedPerAppSigning }.groupBy { it.packageName }
        signingKeys.forEach { (packageName, identity) ->
            val fingerprint = PerAppSigningIdentity.fingerprint(identity.certificate)
            check(references[packageName]?.any { it.certificateSha256 == fingerprint } == true) {
                Strings.signingBackupInvalid
            }
        }
        PerAppSigningIdentity.restore(context, signingKeys)
        return apps
    }

    private fun needsSigningKey(app: WebApp): Boolean {
        val config = app.apkExportConfig ?: return false
        val published = config.publishedPerAppSigning ?: return false
        if (!config.perAppSigningEnabled || published.packageName != ApkBuilder.resolvePackageName(app)) return false
        val identity = runCatching { PerAppSigningIdentity.readExisting(context, published.packageName) }.getOrNull()
        return identity == null || (published.certificateSha256 != null &&
            identity.certSha256Hex() != published.certificateSha256)
    }

    suspend fun importAllData(
        repository: WebAppRepository,
        inputUri: Uri,
        signingPassword: String? = null,
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> }
    ): Result<ImportResult> = withContext(Dispatchers.IO) {
        val extractedFiles = mutableListOf<File>()
        val createdAppIds = mutableListOf<Long>()
        var localsCommitted = false
        val pendingLocalEntries = mutableListOf<PendingLocalEntry>()

        try {
            onProgress(0, 100, Strings.backupReadingBackup)
            coroutineContext.ensureActive()

            var backupData: BackupData? = null
            val extractedResources = mutableMapOf<String, String>()
            var modulesJsonBytes: ByteArray? = null
            var builtInStatesJsonBytes: ByteArray? = null
            var looksLikeSourceBundle = false
            var signingKeys: Map<String, JarSigner.SigningIdentity>? = null

            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream, BUFFER_SIZE)).use { zipIn ->
                    var entry = zipIn.nextEntry
                    var totalEntries = 0
                    val entryGuard = com.webtoapp.util.SafeZip.EntryGuard()
                    val extractThrottle = ProgressThrottle()

                    while (entry != null) {
                        coroutineContext.ensureActive()
                        totalEntries++
                        entryGuard.onEntry()
                        if (extractThrottle.shouldEmit()) {
                            onProgress(10 + (totalEntries % 40), 100, Strings.backupExtracting.format(entry.name))
                        }

                        when {
                            entry.name == APPS_JSON -> {

                                val jsonBytes = readJsonEntry(zipIn, entryGuard)
                                val jsonStr = String(jsonBytes, Charsets.UTF_8)
                                backupData = parseBackupData(jsonStr)
                                AppLogger.i(TAG, "读取到 ${backupData?.appCount} 个应用")
                            }
                            entry.name == PerAppSigningBackup.ENTRY && !entry.isDirectory -> {
                                check(signingKeys == null) { Strings.signingBackupInvalid }
                                val password = signingPassword ?: throw SigningBackupPasswordRequired()
                                val payload = entryGuard.readBytes(zipIn, PerAppSigningBackup.MAX_BYTES.toLong())
                                signingKeys = PerAppSigningBackup.decrypt(payload, password)
                            }
                            entry.name == SOURCE_BUNDLE_CONFIG -> {
                                looksLikeSourceBundle = true
                            }
                            entry.name == EXTENSION_MODULES_FILE -> {
                                modulesJsonBytes = readJsonEntry(zipIn, entryGuard)
                            }
                            entry.name == EXTENSION_BUILTIN_STATES_FILE -> {
                                builtInStatesJsonBytes = readJsonEntry(zipIn, entryGuard)
                            }
                            entry.name.startsWith(RESOURCES_DIR) && !entry.isDirectory -> {

                                val extractedPath = extractResourceFile(entry.name, zipIn, entryGuard)
                                if (extractedPath != null) {
                                    extractedResources[entry.name] = extractedPath
                                    extractedFiles.add(File(extractedPath))
                                }
                            }
                            isLocalBackupEntry(entry.name) && !entry.isDirectory -> {
                                stageLocalBackupEntry(entry.name, zipIn, entryGuard)?.let { pendingLocalEntries.add(it) }
                            }
                        }

                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            } ?: run {
                discardPendingLocalEntries(pendingLocalEntries)
                return@withContext Result.failure(Exception(Strings.backupUnreadableFile))
            }

            val data = backupData
            if (data == null) {
                check(signingKeys == null) { Strings.signingBackupInvalid }
                if (looksLikeSourceBundle) {
                    val legacy = runCatching {
                        context.contentResolver.openInputStream(inputUri)?.use { stream ->
                            LegacySourceBundleImporter(context).restore(stream)
                        } ?: throw IOException(Strings.backupUnreadableFile)
                    }
                    val restored = legacy.getOrNull()
                    if (restored != null) {
                        onProgress(60, 100, Strings.backupImportingData)
                        coroutineContext.ensureActive()
                        val existingKeys = repository.allWebApps.first()
                            .mapTo(HashSet()) { backupDedupKey(it) }
                        if (!existingKeys.add(backupDedupKey(restored.app))) {
                            AppLogger.i(TAG, "跳过重复的旧版源码包应用: ${restored.app.name}")
                            return@withContext Result.success(ImportResult(
                                totalCount = 1,
                                importedCount = 0,
                                skippedCount = 0,
                                duplicateCount = 1,
                                legacySourceBundle = true
                            ))
                        }
                        val now = System.currentTimeMillis()
                        repository.createWebApp(restored.app.copy(id = 0, createdAt = now, updatedAt = now))
                        onProgress(100, 100, Strings.backupImportComplete)
                        return@withContext Result.success(ImportResult(
                            totalCount = 1,
                            importedCount = 1,
                            skippedCount = 0,
                            legacySourceBundle = true,
                            missingProjectFiles = restored.missingProjectFiles
                        ))
                    }
                    AppLogger.w(TAG, "旧版源码包恢复失败", legacy.exceptionOrNull())
                }
                cleanupExtractedFiles(extractedFiles)
                discardPendingLocalEntries(pendingLocalEntries)
                val message = if (looksLikeSourceBundle) {
                    Strings.backupInvalidFormatSourceZip
                } else {
                    Strings.backupInvalidFormat
                }
                return@withContext Result.failure(Exception(message))
            }

            onProgress(60, 100, Strings.backupImportingData)
            coroutineContext.ensureActive()

            val appsToRestore = restoreSigningReferences(data, looksLikeSourceBundle, signingKeys.orEmpty())
            val missingSigningKeys = appsToRestore.count(::needsSigningKey)

            var importedCount = 0
            var skippedCount = 0
            var duplicateCount = 0
            val categoryIdMap = restoreCategories(data.categories)

            val existingAppKeys = repository.allWebApps.first()
                .mapTo(HashSet()) { backupDedupKey(it) }

            val appIdMap = mutableMapOf<Long, Long>()
            val importThrottle = ProgressThrottle()

            appsToRestore.forEachIndexed { index, app ->
                coroutineContext.ensureActive()
                if (importThrottle.shouldEmit()) {
                    onProgress(
                        60 + (index * 35 / appsToRestore.size),
                        100,
                        Strings.backupImportingApp.format(app.name)
                    )
                }

                try {

                    val appWithLocalPaths = updateAppPathsToLocal(app, extractedResources)

                    if (!existingAppKeys.add(backupDedupKey(appWithLocalPaths))) {
                        AppLogger.i(TAG, "跳过重复应用: ${app.name}")
                        duplicateCount++
                        return@forEachIndexed
                    }

                    val newApp = appWithLocalPaths.copy(
                        id = 0,
                        categoryId = remapCategoryId(appWithLocalPaths.categoryId, categoryIdMap),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    val newId = repository.createWebApp(newApp)
                    if (app.id != 0L && newId > 0L) {
                        appIdMap[app.id] = newId
                    }
                    createdAppIds.add(newId)
                    importedCount++
                } catch (e: Exception) {
                    AppLogger.w(TAG, "导入应用失败: ${app.name}", e)
                    skippedCount++
                }
            }

            localsCommitted = true
            val localsRestored = commitPendingLocalEntries(pendingLocalEntries, appIdMap)

            restoreUsageStats(data.usageStats, appIdMap)

            restoreHealthRecords(data.healthRecords, appIdMap)

            restoreExtensionFiles(modulesJsonBytes, builtInStatesJsonBytes)

            onProgress(100, 100, Strings.backupImportComplete)

            Result.success(ImportResult(
                totalCount = data.appCount,
                importedCount = importedCount,
                skippedCount = skippedCount,
                duplicateCount = duplicateCount,
                localFilesRestored = localsRestored,
                missingSigningKeys = missingSigningKeys
            ))

        } catch (e: CancellationException) {
            rollbackFailedImport(repository, createdAppIds, localsCommitted, extractedFiles)
            throw e
        } catch (e: SigningBackupPasswordRequired) {
            cleanupExtractedFiles(extractedFiles)
            Result.failure(e)
        } catch (e: Exception) {
            AppLogger.e(TAG, "导入失败", e)
            rollbackFailedImport(repository, createdAppIds, localsCommitted, extractedFiles)
            Result.failure(e)
        } finally {
            discardPendingLocalEntries(pendingLocalEntries)
        }
    }

    private suspend fun rollbackFailedImport(
        repository: WebAppRepository,
        createdAppIds: List<Long>,
        localsCommitted: Boolean,
        extractedFiles: List<File>
    ) {
        createdAppIds.forEach { id ->
            runCatching { repository.deleteWebAppById(id) }
                .onFailure { AppLogger.w(TAG, "回滚导入应用失败: $id", it) }
        }
        if (createdAppIds.isEmpty() && !localsCommitted) {
            cleanupExtractedFiles(extractedFiles)
        }
    }

    internal fun readJsonEntry(
        zipIn: ZipInputStream,
        guard: com.webtoapp.util.SafeZip.EntryGuard
    ): ByteArray {
        return guard.readBytes(zipIn, MAX_JSON_ENTRY_BYTES)
    }

    internal fun backupDedupKey(app: WebApp): String {
        val url = app.url.trim()
        val urlKey = if (url.startsWith("http://", ignoreCase = true) ||
            url.startsWith("https://", ignoreCase = true)) {
            url.lowercase().removeSuffix("/")
        } else ""
        return "${app.appType.name}|${app.name.trim().lowercase()}|$urlKey"
    }

    internal fun parseBackupData(jsonStr: String): BackupData {
        val root = JsonParser.parseString(jsonStr).asJsonObject
        val version = root.get("version")?.asInt ?: 1
        val exportTime = root.get("exportTime")?.asLong ?: System.currentTimeMillis()
        val appCount = root.get("appCount")?.asInt ?: 0
        val appsArray = root.getAsJsonArray("apps")

        val apps = appsArray?.mapNotNull { element ->
            runCatching {
                parseWebAppWithDefaults(element.asJsonObject)
            }.getOrElse {
                AppLogger.w(TAG, "解析应用配置失败，已跳过一项", it)
                null
            }
        } ?: emptyList()

        val categories = root.getAsJsonArray("categories")?.mapNotNull { element ->
            runCatching {
                gson.fromJson(element, AppCategory::class.java)
            }.getOrElse {
                AppLogger.w(TAG, "解析分类失败，已跳过一项", it)
                null
            }
        } ?: emptyList()

        val usageStats = root.getAsJsonArray("usageStats")?.mapNotNull { element ->
            runCatching {
                gson.fromJson(element, AppUsageStats::class.java)
            }.getOrElse {
                AppLogger.w(TAG, "解析使用统计失败，已跳过一项", it)
                null
            }
        } ?: emptyList()

        val healthRecords = root.getAsJsonArray("healthRecords")?.mapNotNull { element ->
            runCatching {
                gson.fromJson(element, com.webtoapp.core.stats.AppHealthRecord::class.java)
            }.getOrElse {
                AppLogger.w(TAG, "解析健康记录失败，已跳过一项", it)
                null
            }
        } ?: emptyList()

        return BackupData(
            version = version,
            exportTime = exportTime,
            appCount = if (appCount > 0) appCount else apps.size,
            apps = apps,
            categories = categories,
            usageStats = usageStats,
            healthRecords = healthRecords
        )
    }

    private suspend fun restoreCategories(categories: List<AppCategory>): Map<Long, Long> {
        if (categories.isEmpty()) return emptyMap()
        val dao = AppDatabase.getInstance(context).appCategoryDao()
        val idByName = HashMap<String, Long>()
        dao.getAllCategories().first().forEach {
            idByName[it.name.trim().lowercase()] = it.id
        }
        val idMap = mutableMapOf<Long, Long>()
        categories.forEach { category ->
            val nameKey = category.name.trim().lowercase()
            val existingId = idByName[nameKey]
            if (existingId != null) {
                idMap[category.id] = existingId
                return@forEach
            }
            runCatching {
                val newId = dao.insert(category.copy(id = 0))
                idMap[category.id] = newId
                idByName[nameKey] = newId
            }.onFailure { e ->
                AppLogger.w(TAG, "导入分类失败: ${category.name}", e)
            }
        }
        return idMap
    }

    private fun remapCategoryId(categoryId: Long?, categoryIdMap: Map<Long, Long>): Long? {
        if (categoryId == null) return null
        return categoryIdMap[categoryId] ?: categoryId
    }

    private suspend fun restoreUsageStats(
        usageStats: List<AppUsageStats>,
        appIdMap: Map<Long, Long>
    ) {
        if (usageStats.isEmpty() || appIdMap.isEmpty()) return
        val dao = AppDatabase.getInstance(context).appUsageStatsDao()
        var restored = 0
        usageStats.forEach { stats ->
            val newAppId = appIdMap[stats.appId] ?: return@forEach
            runCatching {
                dao.insert(stats.copy(id = 0, appId = newAppId))
                restored++
            }.onFailure { e ->
                AppLogger.w(TAG, "恢复使用统计失败: appId=${stats.appId}", e)
            }
        }
        AppLogger.i(TAG, "恢复使用统计 $restored/${usageStats.size} 项")
    }

    private suspend fun restoreHealthRecords(
        healthRecords: List<com.webtoapp.core.stats.AppHealthRecord>,
        appIdMap: Map<Long, Long>
    ) {
        if (healthRecords.isEmpty() || appIdMap.isEmpty()) return
        val dao = AppDatabase.getInstance(context).appUsageStatsDao()
        var restored = 0
        healthRecords.forEach { record ->
            val newAppId = appIdMap[record.appId] ?: return@forEach
            runCatching {
                dao.insertHealthRecord(record.copy(id = 0, appId = newAppId))
                restored++
            }.onFailure { e ->
                AppLogger.w(TAG, "恢复健康记录失败: appId=${record.appId}", e)
            }
        }
        AppLogger.i(TAG, "恢复健康记录 $restored/${healthRecords.size} 项")
    }

    private fun parseWebAppWithDefaults(appObject: JsonObject): WebApp {
        val defaults = gson.toJsonTree(WebApp(name = "", url = ""))
        val merged = Converters.mergeMissingDefaults(defaults, appObject)
        return runCatching {
            gson.fromJson(merged, WebApp::class.java)
        }.getOrNull() ?: throw com.google.gson.JsonParseException("Failed to parse WebApp with defaults")
    }

    private fun restoreExtensionFiles(modulesJsonBytes: ByteArray?, builtInStatesJsonBytes: ByteArray?) {
        runCatching {
            com.webtoapp.core.plugin.PluginStore.release()

            if (modulesJsonBytes != null || builtInStatesJsonBytes != null) {
                val extensionDir = File(context.filesDir, "extension_modules").apply { mkdirs() }
                modulesJsonBytes?.let { File(extensionDir, "modules.json").writeBytes(it) }
                builtInStatesJsonBytes?.let { File(extensionDir, "builtin_states.json").writeBytes(it) }
            }

            com.webtoapp.core.plugin.PluginStore.getInstance(context)
            AppLogger.i(TAG, "插件数据已恢复并重新加载")
        }.onFailure { e ->
            AppLogger.w(TAG, "恢复插件配置失败", e)
        }
    }

    private fun cleanupExtractedFiles(files: List<File>) {
        files.forEach { file ->
            try {
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "清理文件失败: ${file.absolutePath}", e)
            }
        }
    }

    fun cleanupBackupTempFiles() {
        val backupDirs = listOf(
            "backup_icons", "backup_splash", "backup_bgm",
            "backup_bgm_lrc", "backup_bgm_cover",
            "backup_html", "backup_media", "backup_statusbar",
            "backup_gallery", "backup_certs", "backup_multi_web", "backup_other"
        )

        backupDirs.forEach { dirName ->
            try {
                val dir = File(context.filesDir, dirName)
                if (dir.exists() && dir.isDirectory) {
                    dir.deleteRecursively()
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "清理备份目录失败: $dirName", e)
            }
        }
    }

    fun getBackupTempSize(): Long {
        val backupDirs = listOf(
            "backup_icons", "backup_splash", "backup_bgm",
            "backup_bgm_lrc", "backup_bgm_cover",
            "backup_html", "backup_media", "backup_statusbar",
            "backup_gallery", "backup_certs", "backup_multi_web", "backup_other"
        )

        return backupDirs.sumOf { dirName ->
            val dir = File(context.filesDir, dirName)
            if (dir.exists() && dir.isDirectory) {
                dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            } else 0L
        }
    }

    private fun collectAppResources(app: WebApp, resources: MutableMap<String, String>) {
        val appId = app.id.toString()

        app.iconPath?.let { path ->
            if (File(path).exists()) {
                val ext = path.substringAfterLast('.', "png")
                resources["${ICONS_DIR}${appId}_icon.$ext"] = path
            }
        }

        app.splashConfig?.mediaPath?.let { path ->
            if (File(path).exists()) {
                val ext = path.substringAfterLast('.', "png")
                resources["${SPLASH_DIR}${appId}_splash.$ext"] = path
            }
        }

        app.bgmConfig?.playlist?.forEachIndexed { index, bgmItem ->

            if (!bgmItem.isAsset && File(bgmItem.path).exists()) {
                val ext = bgmItem.path.substringAfterLast('.', "mp3")
                resources["${BGM_DIR}${appId}_bgm_$index.$ext"] = bgmItem.path
            }

            bgmItem.lrcPath?.let { lrcPath ->
                if (File(lrcPath).exists()) {
                    resources["${BGM_LRC_DIR}${appId}_bgm_$index.lrc"] = lrcPath
                }
            }

            bgmItem.coverPath?.let { coverPath ->
                if (File(coverPath).exists()) {
                    val ext = coverPath.substringAfterLast('.', "jpg")
                    resources["${BGM_COVER_DIR}${appId}_bgm_cover_$index.$ext"] = coverPath
                }
            }
        }

        app.webViewConfig.statusBarBackgroundImage?.let { path ->
            if (File(path).exists()) {
                val ext = path.substringAfterLast('.', "png")
                resources["${STATUSBAR_DIR}${appId}_statusbar.$ext"] = path
            }
        }

        app.webViewConfig.statusBarBackgroundImageDark?.let { path ->
            if (File(path).exists()) {
                val ext = path.substringAfterLast('.', "png")
                resources["${STATUSBAR_DIR}${appId}_statusbar_dark.$ext"] = path
            }
        }

        app.htmlConfig?.files?.forEach { htmlFile ->
            if (File(htmlFile.path).exists()) {
                resources["${HTML_DIR}${appId}/${htmlFile.name}"] = htmlFile.path
            }
        }

        if (app.appType == com.webtoapp.data.model.AppType.IMAGE ||
            app.appType == com.webtoapp.data.model.AppType.VIDEO) {
            val mediaPath = app.url
            if (mediaPath.isNotBlank() && File(mediaPath).exists()) {
                val ext = mediaPath.substringAfterLast('.', "mp4")
                resources["${MEDIA_DIR}${appId}_media.$ext"] = mediaPath
            }
        }

        app.mediaConfig?.mediaPath?.let { path ->
            if (File(path).exists()) {
                val ext = path.substringAfterLast('.', "mp4")
                resources["${MEDIA_DIR}${appId}_media_config.$ext"] = path
            }
        }

        app.galleryConfig?.items?.forEachIndexed { index, item ->
            if (File(item.path).exists()) {
                val ext = item.path.substringAfterLast('.', "jpg")
                resources["${GALLERY_DIR}${appId}_gallery_$index.$ext"] = item.path
            }
            item.thumbnailPath?.let { path ->
                if (File(path).exists()) {
                    val ext = path.substringAfterLast('.', "jpg")
                    resources["${GALLERY_DIR}${appId}_gallery_thumb_$index.$ext"] = path
                }
            }
        }

        app.apkExportConfig?.networkTrustConfig?.customCaCertificates?.forEachIndexed { index, cert ->
            if (File(cert.filePath).exists()) {
                val ext = cert.filePath.substringAfterLast('.', "cer")
                resources["${CERTS_DIR}${appId}_custom_ca_$index.$ext"] = cert.filePath
            }
        }

        app.multiWebConfig?.sites?.forEachIndexed { index, site ->
            val path = site.localFilePath
            if (path.isNotBlank() && File(path).exists()) {
                val ext = path.substringAfterLast('.', "html")
                resources["${MULTI_WEB_DIR}${appId}_site_$index.$ext"] = path
            }
        }

        app.announcement?.customIconPath?.let { path ->
            if (File(path).exists()) {
                val ext = path.substringAfterLast('.', "png")
                resources["${ANNOUNCEMENT_DIR}${appId}_announcement_icon.$ext"] = path
            }
        }

        app.webViewConfig.errorPageConfig.customMediaPath?.let { path ->
            if (File(path).exists()) {
                val ext = path.substringAfterLast('.', "png")
                resources["${MEDIA_DIR}${appId}_errorpage_media.$ext"] = path
            }
        }

        app.webViewConfig.floatingWindowConfig.minimizedIconPath?.let { path ->
            if (File(path).exists()) {
                val ext = path.substringAfterLast('.', "png")
                resources["${ICONS_DIR}${appId}_floating_minimized.$ext"] = path
            }
        }
    }

    private fun collectLocalBackupFiles(): Map<String, File> {
        val files = linkedMapOf<String, File>()

        MANAGED_FILES_DIRS.forEach { dirName ->
            collectDirectoryFiles(
                sourceDir = File(context.filesDir, dirName),
                zipRoot = "$LOCAL_FILES_DIR$dirName/",
                files = files
            )
        }

        File(context.filesDir, com.webtoapp.core.plugin.PluginStore.STATE_FILE)
            .takeIf { it.isFile && it.canRead() }
            ?.let { files["$LOCAL_FILES_DIR${it.name}"] = it }

        val externalFilesDir = context.getExternalFilesDir(null)
        if (externalFilesDir != null) {
            MANAGED_EXTERNAL_DIRS.forEach { dirName ->
                collectDirectoryFiles(
                    sourceDir = File(externalFilesDir, dirName),
                    zipRoot = "$LOCAL_EXTERNAL_FILES_DIR$dirName/",
                    files = files
                )
            }
        }

        SAFE_DATASTORE_NAMES.forEach { name ->
            val file = File(context.filesDir, "datastore/$name.preferences_pb")
            if (file.exists() && file.isFile && file.canRead()) {
                files["$DATASTORE_DIR${file.name}"] = file
            }
        }

        (KEYSTORE_FILES.map { it to KEYSTORE_DIR } + ADBLOCK_FILES.map { it to ADBLOCK_DIR })
            .forEach { (name, zipDir) ->
                val file = File(context.filesDir, name)
                if (file.exists() && file.isFile && file.canRead()) {
                    files["$zipDir$name"] = file
                }
            }

        val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        SAFE_SHARED_PREF_NAMES.forEach { name ->
            val file = File(sharedPrefsDir, "$name.xml")
            if (file.exists() && file.isFile && file.canRead()) {
                files["$SHARED_PREFS_DIR${file.name}"] = file
            }
        }

        if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory) {
            sharedPrefsDir.listFiles { f ->
                f.isFile && f.name.endsWith(".xml") && f.canRead() &&
                    SAFE_SHARED_PREF_PREFIXES.any { f.name.startsWith(it) }
            }?.forEach { file ->
                files["$SHARED_PREFS_DIR${file.name}"] = file
            }
        }

        return files
    }

    private fun collectDirectoryFiles(
        sourceDir: File,
        zipRoot: String,
        files: MutableMap<String, File>
    ) {
        if (!sourceDir.exists() || !sourceDir.isDirectory) return
        sourceDir.walkTopDown()
            .filter { it.isFile && it.canRead() }
            .forEach { file ->
                val relativePath = file.relativeTo(sourceDir).invariantSeparatorsPath
                if (isExcludedManagedPath(relativePath)) return@forEach
                files["$zipRoot$relativePath"] = file
            }
    }

    private fun isExcludedManagedPath(relativePath: String): Boolean {
        return relativePath.split('/').any { segment ->
            segment in MANAGED_FILES_EXCLUDED_SEGMENTS
        }
    }

    internal fun updateAppPathsToRelative(
        app: WebApp,
        resources: Map<String, String>
    ): WebApp {
        val appId = app.id.toString()

        fun findZipPath(localPath: String?): String? {
            if (localPath == null) return null
            return resources.entries.find { it.value == localPath }?.key
        }

        return app.copy(
            iconPath = findZipPath(app.iconPath),
            announcement = app.announcement?.copy(
                customIconPath = findZipPath(app.announcement.customIconPath)
            ),
            splashConfig = app.splashConfig?.copy(
                mediaPath = findZipPath(app.splashConfig?.mediaPath)
            ),
            bgmConfig = app.bgmConfig?.copy(
                playlist = app.bgmConfig?.playlist?.mapIndexed { index, item ->
                    if (!item.isAsset) {
                        item.copy(
                            path = findZipPath(item.path) ?: item.path,
                            lrcPath = findZipPath(item.lrcPath),
                            coverPath = findZipPath(item.coverPath)
                        )
                    } else item
                } ?: emptyList()
            ),
            htmlConfig = app.htmlConfig?.copy(
                files = app.htmlConfig?.files?.map { file ->
                    file.copy(path = "${HTML_DIR}${appId}/${file.name}")
                } ?: emptyList()
            ),
            mediaConfig = app.mediaConfig?.copy(
                mediaPath = findZipPath(app.mediaConfig.mediaPath) ?: app.mediaConfig.mediaPath
            ),
            galleryConfig = app.galleryConfig?.copy(
                items = app.galleryConfig.items.map { item ->
                    item.copy(
                        path = findZipPath(item.path) ?: item.path,
                        thumbnailPath = item.thumbnailPath?.let { findZipPath(it) ?: it }
                    )
                }
            ),
            apkExportConfig = app.apkExportConfig?.copy(
                networkTrustConfig = app.apkExportConfig.networkTrustConfig.copy(
                    customCaCertificates = app.apkExportConfig.networkTrustConfig.customCaCertificates.map { cert ->
                        cert.copy(filePath = findZipPath(cert.filePath) ?: cert.filePath)
                    }
                )
            ),
            multiWebConfig = app.multiWebConfig?.copy(
                sites = app.multiWebConfig.sites.map { site ->
                    site.copy(
                        localFilePath = findZipPath(site.localFilePath) ?: site.localFilePath
                    )
                }
            ),
            webViewConfig = app.webViewConfig.copy(
                statusBarBackgroundImage = findZipPath(app.webViewConfig.statusBarBackgroundImage),
                statusBarBackgroundImageDark = findZipPath(app.webViewConfig.statusBarBackgroundImageDark),
                errorPageConfig = app.webViewConfig.errorPageConfig.copy(
                    customMediaPath = findZipPath(app.webViewConfig.errorPageConfig.customMediaPath)
                ),
                floatingWindowConfig = app.webViewConfig.floatingWindowConfig.copy(
                    minimizedIconPath = findZipPath(app.webViewConfig.floatingWindowConfig.minimizedIconPath)
                )
            ),
            url = if (app.appType == com.webtoapp.data.model.AppType.IMAGE ||
                      app.appType == com.webtoapp.data.model.AppType.VIDEO) {
                findZipPath(app.url) ?: app.url
            } else app.url
        )
    }

    internal fun updateAppPathsToLocal(
        app: WebApp,
        extractedResources: Map<String, String>
    ): WebApp {
        return app.copy(
            iconPath = extractedResources[app.iconPath] ?: app.iconPath,
            announcement = app.announcement?.copy(
                customIconPath = app.announcement.customIconPath?.let { extractedResources[it] ?: it }
            ),
            splashConfig = app.splashConfig?.copy(
                mediaPath = extractedResources[app.splashConfig?.mediaPath] ?: app.splashConfig?.mediaPath
            ),
            bgmConfig = app.bgmConfig?.copy(
                playlist = app.bgmConfig?.playlist?.map { item ->
                    if (!item.isAsset) {
                        item.copy(
                            path = extractedResources[item.path] ?: item.path,
                            lrcPath = item.lrcPath?.let { extractedResources[it] ?: it },
                            coverPath = item.coverPath?.let { extractedResources[it] ?: it }
                        )
                    } else item
                } ?: emptyList()
            ),
            htmlConfig = app.htmlConfig?.copy(
                files = app.htmlConfig?.files?.map { file ->
                    file.copy(path = extractedResources[file.path] ?: file.path)
                } ?: emptyList()
            ),
            mediaConfig = app.mediaConfig?.copy(
                mediaPath = extractedResources[app.mediaConfig.mediaPath] ?: app.mediaConfig.mediaPath
            ),
            galleryConfig = app.galleryConfig?.copy(
                items = app.galleryConfig.items.map { item ->
                    item.copy(
                        path = extractedResources[item.path] ?: item.path,
                        thumbnailPath = item.thumbnailPath?.let { extractedResources[it] ?: it }
                    )
                }
            ),
            apkExportConfig = app.apkExportConfig?.copy(
                networkTrustConfig = app.apkExportConfig.networkTrustConfig.copy(
                    customCaCertificates = app.apkExportConfig.networkTrustConfig.customCaCertificates.map { cert ->
                        cert.copy(filePath = extractedResources[cert.filePath] ?: cert.filePath)
                    }
                )
            ),
            multiWebConfig = app.multiWebConfig?.copy(
                sites = app.multiWebConfig.sites.map { site ->
                    site.copy(
                        localFilePath = extractedResources[site.localFilePath] ?: site.localFilePath
                    )
                }
            ),
            webViewConfig = app.webViewConfig.copy(
                statusBarBackgroundImage = app.webViewConfig.statusBarBackgroundImage?.let {
                    extractedResources[it] ?: it
                },
                statusBarBackgroundImageDark = app.webViewConfig.statusBarBackgroundImageDark?.let {
                    extractedResources[it] ?: it
                },
                errorPageConfig = app.webViewConfig.errorPageConfig.copy(
                    customMediaPath = app.webViewConfig.errorPageConfig.customMediaPath?.let {
                        extractedResources[it] ?: it
                    }
                ),
                floatingWindowConfig = app.webViewConfig.floatingWindowConfig.copy(
                    minimizedIconPath = app.webViewConfig.floatingWindowConfig.minimizedIconPath?.let {
                        extractedResources[it] ?: it
                    }
                )
            ),
            url = if (app.appType == com.webtoapp.data.model.AppType.IMAGE ||
                      app.appType == com.webtoapp.data.model.AppType.VIDEO) {
                extractedResources[app.url] ?: app.url
            } else app.url
        )
    }

    private fun extractResourceFile(
        zipPath: String,
        zipIn: ZipInputStream,
        guard: com.webtoapp.util.SafeZip.EntryGuard? = null
    ): String? {
        return try {
            val (targetDir, prefix) = when {
                zipPath.startsWith(ICONS_DIR) -> File(context.filesDir, "backup_icons") to ICONS_DIR
                zipPath.startsWith(SPLASH_DIR) -> File(context.filesDir, "backup_splash") to SPLASH_DIR
                zipPath.startsWith(BGM_LRC_DIR) -> File(context.filesDir, "backup_bgm_lrc") to BGM_LRC_DIR
                zipPath.startsWith(BGM_COVER_DIR) -> File(context.filesDir, "backup_bgm_cover") to BGM_COVER_DIR
                zipPath.startsWith(BGM_DIR) -> File(context.filesDir, "backup_bgm") to BGM_DIR
                zipPath.startsWith(HTML_DIR) -> File(context.filesDir, "backup_html") to HTML_DIR
                zipPath.startsWith(MEDIA_DIR) -> File(context.filesDir, "backup_media") to MEDIA_DIR
                zipPath.startsWith(STATUSBAR_DIR) -> File(context.filesDir, "backup_statusbar") to STATUSBAR_DIR
                zipPath.startsWith(GALLERY_DIR) -> File(context.filesDir, "backup_gallery") to GALLERY_DIR
                zipPath.startsWith(CERTS_DIR) -> File(context.filesDir, "backup_certs") to CERTS_DIR
                zipPath.startsWith(MULTI_WEB_DIR) -> File(context.filesDir, "backup_multi_web") to MULTI_WEB_DIR
                zipPath.startsWith(ANNOUNCEMENT_DIR) -> File(context.filesDir, "backup_announcement") to ANNOUNCEMENT_DIR
                else -> File(context.filesDir, "backup_other") to RESOURCES_DIR
            }

            targetDir.mkdirs()

            val relativePath = zipPath.removePrefix(prefix).ifBlank { zipPath.substringAfterLast('/') }
            val targetFile = resolveSafeChild(targetDir, relativePath) ?: return null
            targetFile.parentFile?.mkdirs()

            FileOutputStream(targetFile).use { output ->
                if (guard != null) guard.copyTo(zipIn, output) else zipIn.copyTo(output)
            }

            targetFile.absolutePath
        } catch (e: com.webtoapp.util.SafeZip.ZipBombException) {
            throw e
        } catch (e: Exception) {
            AppLogger.w(TAG, "解压资源文件失败: $zipPath", e)
            null
        }
    }

    private fun isLocalBackupEntry(zipPath: String): Boolean {
        return zipPath.startsWith(LOCAL_FILES_DIR) ||
            zipPath.startsWith(LOCAL_EXTERNAL_FILES_DIR) ||
            zipPath.startsWith(DATASTORE_DIR) ||
            zipPath.startsWith(SHARED_PREFS_DIR) ||
            zipPath.startsWith(KEYSTORE_DIR) ||
            zipPath.startsWith(ADBLOCK_DIR)
    }

    internal data class PendingLocalEntry(
        val zipPath: String,
        val targetFile: File,
        val tempFile: File
    )

    internal fun stageLocalBackupEntry(
        zipPath: String,
        zipIn: ZipInputStream,
        guard: com.webtoapp.util.SafeZip.EntryGuard? = null
    ): PendingLocalEntry? {
        val targetFile = when {
            zipPath.startsWith(LOCAL_FILES_DIR) -> {
                val relativePath = zipPath.removePrefix(LOCAL_FILES_DIR)
                val target = resolveSafeChild(context.filesDir, relativePath) ?: return null
                val root = target.relativeTo(context.filesDir.canonicalFile).path.substringBefore(File.separator)
                if (root in MANAGED_FILES_DIRS || root == com.webtoapp.core.plugin.PluginStore.STATE_FILE) target else null
            }
            zipPath.startsWith(LOCAL_EXTERNAL_FILES_DIR) -> {
                val externalFilesDir = context.getExternalFilesDir(null) ?: return null
                val relativePath = zipPath.removePrefix(LOCAL_EXTERNAL_FILES_DIR)
                resolveSafeChild(externalFilesDir, relativePath)
            }
            zipPath.startsWith(DATASTORE_DIR) -> {
                val fileName = zipPath.removePrefix(DATASTORE_DIR)
                val allowed = SAFE_DATASTORE_NAMES.map { "$it.preferences_pb" }.contains(fileName)
                if (allowed) {
                    resolveSafeChild(File(context.filesDir, "datastore"), fileName)
                } else null
            }
            zipPath.startsWith(SHARED_PREFS_DIR) -> {
                val fileName = zipPath.removePrefix(SHARED_PREFS_DIR)
                val allowed = SAFE_SHARED_PREF_NAMES.map { "$it.xml" }.contains(fileName) ||
                    (fileName.endsWith(".xml") &&
                        SAFE_SHARED_PREF_PREFIXES.any { fileName.startsWith(it) })
                if (allowed) {
                    resolveSafeChild(File(context.applicationInfo.dataDir, "shared_prefs"), fileName)
                } else null
            }
            zipPath.startsWith(KEYSTORE_DIR) -> {
                val fileName = zipPath.removePrefix(KEYSTORE_DIR)
                if (KEYSTORE_FILES.contains(fileName)) {
                    resolveSafeChild(context.filesDir, fileName)
                } else null
            }
            zipPath.startsWith(ADBLOCK_DIR) -> {
                val fileName = zipPath.removePrefix(ADBLOCK_DIR)
                if (ADBLOCK_FILES.contains(fileName)) {
                    resolveSafeChild(context.filesDir, fileName)
                } else null
            }
            else -> null
        } ?: return null

        val tempFile = File.createTempFile("wta_backup_", ".part", context.cacheDir)
        return try {
            FileOutputStream(tempFile).use { output ->
                if (guard != null) guard.copyTo(zipIn, output) else zipIn.copyTo(output, BUFFER_SIZE)
            }
            PendingLocalEntry(zipPath, targetFile, tempFile)
        } catch (e: com.webtoapp.util.SafeZip.ZipBombException) {
            throw e
        } catch (e: Exception) {
            AppLogger.w(TAG, "暂存本地备份数据失败: $zipPath", e)
            tempFile.delete()
            null
        }
    }

    internal fun commitPendingLocalEntries(
        pending: List<PendingLocalEntry>,
        appIdMap: Map<Long, Long> = emptyMap()
    ): Boolean {
        var committed = false
        pending.forEach { entry ->
            if (entry.zipPath == AI_CONFIG_DATASTORE_ENTRY && entry.targetFile.exists()) {
                AppLogger.i(TAG, "Keeping the local AI config; backup keys are device-bound")
                entry.tempFile.delete()
                return@forEach
            }
            runCatching {
                entry.targetFile.parentFile?.mkdirs()
                when (entry.zipPath) {
                    ACTIVATION_DATASTORE_ENTRY -> {
                        val restored = remapActivationDataStore(entry.tempFile.readBytes(), appIdMap)
                        entry.targetFile.writeBytes(restored)
                    }
                    HOST_RUNTIME_DATASTORE_ENTRY -> {
                        val sanitized = stripHostRuntimeMcpKeys(entry.tempFile.readBytes())
                        entry.targetFile.writeBytes(sanitized)
                    }
                    GALLERY_POSITIONS_PREFS_ENTRY -> {
                        val remapped = remapGalleryPositionsXml(entry.tempFile.readText(), appIdMap)
                        entry.targetFile.writeText(remapped)
                    }
                    else -> entry.tempFile.copyTo(entry.targetFile, overwrite = true)
                }
                committed = true
            }.onFailure { e ->
                AppLogger.w(TAG, "恢复本地数据失败: ${entry.zipPath}", e)
            }
            entry.tempFile.delete()
        }
        return committed
    }

    private fun discardPendingLocalEntries(pending: List<PendingLocalEntry>) {
        pending.forEach { it.tempFile.delete() }
    }

    internal fun remapActivationDataStore(bytes: ByteArray, appIdMap: Map<Long, Long>): ByteArray {
        if (appIdMap.isEmpty()) return bytes
        return try {
            val prefs = androidx.datastore.preferences.PreferencesProto.PreferenceMap.parseFrom(bytes)
            val builder = prefs.toBuilder()
            val remapped = mutableMapOf<String, androidx.datastore.preferences.PreferencesProto.Value>()
            prefs.preferencesMap.forEach { (name, value) ->
                val suffix = name.substringAfterLast('_', "")
                val oldId = suffix.toLongOrNull()
                val newId = if (oldId != null) appIdMap[oldId] else null
                if (newId != null) {
                    val prefix = name.substring(0, name.length - suffix.length)
                    remapped["$prefix$newId"] = value
                } else {
                    remapped[name] = value
                }
            }
            builder.clearPreferences()
            builder.putAllPreferences(remapped)
            builder.build().toByteArray()
        } catch (e: Exception) {
            AppLogger.w(TAG, "激活存储重映射失败，按原样恢复", e)
            bytes
        }
    }

    internal fun stripHostRuntimeMcpKeys(bytes: ByteArray): ByteArray {
        return try {
            val prefs = androidx.datastore.preferences.PreferencesProto.PreferenceMap.parseFrom(bytes)
            val builder = prefs.toBuilder()
            HOST_RUNTIME_MCP_KEYS.forEach { builder.removePreferences(it) }
            builder.build().toByteArray()
        } catch (e: Exception) {
            AppLogger.w(TAG, "宿主偏好 MCP 密钥剔除失败，按原样恢复", e)
            bytes
        }
    }

    internal fun remapGalleryPositionsXml(xml: String, appIdMap: Map<Long, Long>): String {
        if (appIdMap.isEmpty()) return xml
        return try {
            val doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(java.io.ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))
            val nodes = doc.getElementsByTagName("int")
            for (i in 0 until nodes.length) {
                val el = nodes.item(i) as? org.w3c.dom.Element ?: continue
                val name = el.getAttribute("name")
                if (!name.startsWith("pos_")) continue
                val oldId = name.removePrefix("pos_").toLongOrNull() ?: continue
                val newId = appIdMap[oldId] ?: continue
                el.setAttribute("name", "pos_$newId")
            }
            val transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes")
            val out = java.io.StringWriter()
            transformer.transform(javax.xml.transform.dom.DOMSource(doc), javax.xml.transform.stream.StreamResult(out))
            out.toString()
        } catch (e: Exception) {
            AppLogger.w(TAG, "画廊位置重映射失败，按原样恢复", e)
            xml
        }
    }

    internal fun resolveSafeChild(baseDir: File, relativePath: String): File? {
        if (relativePath.isBlank()) return null
        val normalized = relativePath.replace('\\', '/')
        if (normalized.startsWith("/") || normalized.split('/').any { it == ".." }) {
            AppLogger.w(TAG, "跳过不安全的备份路径: $relativePath")
            return null
        }
        val targetFile = File(baseDir, normalized)
        val baseCanonical = baseDir.canonicalFile
        val targetCanonical = targetFile.canonicalFile
        return if (targetCanonical.path.startsWith(baseCanonical.path + File.separator)) {
            targetCanonical
        } else {
            AppLogger.w(TAG, "跳过不安全的备份路径: $relativePath")
            null
        }
    }

    fun generateBackupFileName(): String {
        return "WebToApp_Backup_${backupDateFormat.get()!!.format(Date())}.zip"
    }

    fun scheduleAppRestart(delayMs: Long = 800) {
        try {
            val launchIntent = context.packageManager
                .getLaunchIntentForPackage(context.packageName)
                ?.apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                } ?: return
            val pending = android.app.PendingIntent.getActivity(
                context, 0, launchIntent,
                android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val alarm = context.getSystemService(android.content.Context.ALARM_SERVICE)
                as android.app.AlarmManager
            alarm.set(android.app.AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMs, pending)
            AppLogger.i(TAG, "应用将在 ${delayMs}ms 后重启以应用恢复的数据")
            kotlin.system.exitProcess(0)
        } catch (e: Exception) {
            AppLogger.e(TAG, "计划重启失败，请手动重启应用", e)
        }
    }
}

data class ExportResult(
    val appCount: Int,
    val resourceCount: Int,
    val signingKeyCount: Int = 0,
    val missingSigningKeys: Int = 0
)

data class ImportResult(
    val totalCount: Int,
    val importedCount: Int,
    val skippedCount: Int,
    val duplicateCount: Int = 0,
    val localFilesRestored: Boolean = false,
    val legacySourceBundle: Boolean = false,
    val missingProjectFiles: Boolean = false,
    val missingSigningKeys: Int = 0
)
