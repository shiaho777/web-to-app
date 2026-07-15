package com.webtoapp.core.apkbuilder

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.WebApp
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

enum class ModifyApkMode {
    FULL,
    CONTENT_OVERLAY
}

enum class IncrementalBuildMode {
    FULL,
    CONTENT_OVERLAY,
    REUSE_UNSIGNED
}

data class ApkBuildCacheMeta(
    val schema: Int = SCHEMA_VERSION,
    val cacheKey: String,
    val packageName: String,
    val identityFingerprint: String,
    val contentFingerprint: String,
    val shellTemplateId: String,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject()
        .put("schema", schema)
        .put("cacheKey", cacheKey)
        .put("packageName", packageName)
        .put("identityFingerprint", identityFingerprint)
        .put("contentFingerprint", contentFingerprint)
        .put("shellTemplateId", shellTemplateId)
        .put("updatedAt", updatedAt)

    companion object {
        const val SCHEMA_VERSION = 1

        fun fromJson(raw: String): ApkBuildCacheMeta? {
            return try {
                val obj = JSONObject(raw)
                if (obj.optInt("schema") != SCHEMA_VERSION) return null
                ApkBuildCacheMeta(
                    schema = obj.getInt("schema"),
                    cacheKey = obj.getString("cacheKey"),
                    packageName = obj.getString("packageName"),
                    identityFingerprint = obj.getString("identityFingerprint"),
                    contentFingerprint = obj.getString("contentFingerprint"),
                    shellTemplateId = obj.optString("shellTemplateId", ""),
                    updatedAt = obj.optLong("updatedAt", 0L)
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

data class IncrementalPlan(
    val mode: IncrementalBuildMode,
    val cachedUnsigned: File? = null,
    val identityFingerprint: String,
    val contentFingerprint: String,
    val shellTemplateId: String,
    val reason: String
)

class ApkBuildCache(private val context: Context) {

    companion object {
        private const val TAG = "ApkBuildCache"
        private const val ROOT_DIR = "apk_build_cache"
        private const val UNSIGNED_NAME = "base_unsigned.apk"
        private const val META_NAME = "meta.json"
        private const val MAX_CACHE_APPS = 20
    }

    private val rootDir: File =
        File(context.filesDir, ROOT_DIR).apply { mkdirs() }

    fun cacheKeyFor(webApp: WebApp, packageName: String): String {
        return if (webApp.id > 0) {
            "app_${webApp.id}"
        } else {
            "pkg_" + packageName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        }
    }

    fun shellTemplateId(templateApk: File): String {
        return "name=${templateApk.name}|size=${templateApk.length()}"
    }

    fun plan(
        webApp: WebApp,
        packageName: String,
        config: ApkConfig,
        templateApk: File,
        encryptionEnabled: Boolean,
        abiFilters: List<String>,
        projectDirs: List<File?>,
        mediaContentPath: String?,
        splashMediaPath: String?,
        bgmPlaylistPaths: List<String>,
        htmlFiles: List<com.webtoapp.data.model.HtmlFile>,
        galleryItems: List<com.webtoapp.data.model.GalleryItem>,
        errorPageMediaPath: String?,
        forceFullRebuild: Boolean
    ): IncrementalPlan {
        val shellId = shellTemplateId(templateApk)
        val identity = identityFingerprint(
            config = config,
            shellTemplateId = shellId,
            encryptionEnabled = encryptionEnabled,
            abiFilters = abiFilters,
            iconPath = webApp.iconPath
        )
        val content = contentFingerprint(
            config = config,
            projectDirs = projectDirs,
            mediaContentPath = mediaContentPath,
            splashMediaPath = splashMediaPath,
            bgmPlaylistPaths = bgmPlaylistPaths,
            htmlFiles = htmlFiles,
            galleryItems = galleryItems,
            errorPageMediaPath = errorPageMediaPath,
            statusBarImage = config.statusBarBackgroundImage,
            floatingIcon = config.floatingWindowMinimizedIconPath
        )

        if (forceFullRebuild) {
            return IncrementalPlan(
                mode = IncrementalBuildMode.FULL,
                identityFingerprint = identity,
                contentFingerprint = content,
                shellTemplateId = shellId,
                reason = "forceFullRebuild"
            )
        }
        if (encryptionEnabled) {
            return IncrementalPlan(
                mode = IncrementalBuildMode.FULL,
                identityFingerprint = identity,
                contentFingerprint = content,
                shellTemplateId = shellId,
                reason = "encryptionEnabled"
            )
        }

        val key = cacheKeyFor(webApp, packageName)
        val entry = load(key) ?: return IncrementalPlan(
            mode = IncrementalBuildMode.FULL,
            identityFingerprint = identity,
            contentFingerprint = content,
            shellTemplateId = shellId,
            reason = "cacheMiss"
        )

        if (entry.meta.packageName != packageName) {
            return IncrementalPlan(
                mode = IncrementalBuildMode.FULL,
                identityFingerprint = identity,
                contentFingerprint = content,
                shellTemplateId = shellId,
                reason = "packageNameChanged"
            )
        }
        if (entry.meta.shellTemplateId != shellId) {
            return IncrementalPlan(
                mode = IncrementalBuildMode.FULL,
                identityFingerprint = identity,
                contentFingerprint = content,
                shellTemplateId = shellId,
                reason = "shellTemplateChanged"
            )
        }
        if (entry.meta.identityFingerprint != identity) {
            return IncrementalPlan(
                mode = IncrementalBuildMode.FULL,
                identityFingerprint = identity,
                contentFingerprint = content,
                shellTemplateId = shellId,
                reason = "identityChanged"
            )
        }
        if (!entry.unsignedApk.isFile || entry.unsignedApk.length() == 0L) {
            return IncrementalPlan(
                mode = IncrementalBuildMode.FULL,
                identityFingerprint = identity,
                contentFingerprint = content,
                shellTemplateId = shellId,
                reason = "cachedUnsignedMissing"
            )
        }
        if (entry.meta.contentFingerprint == content) {
            return IncrementalPlan(
                mode = IncrementalBuildMode.REUSE_UNSIGNED,
                cachedUnsigned = entry.unsignedApk,
                identityFingerprint = identity,
                contentFingerprint = content,
                shellTemplateId = shellId,
                reason = "identityAndContentMatch"
            )
        }
        return IncrementalPlan(
            mode = IncrementalBuildMode.CONTENT_OVERLAY,
            cachedUnsigned = entry.unsignedApk,
            identityFingerprint = identity,
            contentFingerprint = content,
            shellTemplateId = shellId,
            reason = "contentChanged"
        )
    }

    fun saveUnsigned(
        webApp: WebApp,
        packageName: String,
        unsignedApk: File,
        identityFingerprint: String,
        contentFingerprint: String,
        shellTemplateId: String
    ) {
        if (!unsignedApk.isFile || unsignedApk.length() == 0L) return
        val key = cacheKeyFor(webApp, packageName)
        val dir = File(rootDir, key).apply { mkdirs() }
        val dest = File(dir, UNSIGNED_NAME)
        val metaFile = File(dir, META_NAME)
        try {
            unsignedApk.copyTo(dest, overwrite = true)
            val meta = ApkBuildCacheMeta(
                cacheKey = key,
                packageName = packageName,
                identityFingerprint = identityFingerprint,
                contentFingerprint = contentFingerprint,
                shellTemplateId = shellTemplateId
            )
            metaFile.writeText(meta.toJson().toString())
            AppLogger.i(TAG, "Saved incremental cache for $key (${dest.length() / 1024} KB)")
            trimOldCaches()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to save cache for $key: ${e.message}")
            try {
                dest.delete()
                metaFile.delete()
            } catch (_: Exception) {
            }
        }
    }

    fun clear(webApp: WebApp, packageName: String) {
        val key = cacheKeyFor(webApp, packageName)
        val dir = File(rootDir, key)
        if (dir.exists()) {
            dir.deleteRecursively()
            AppLogger.i(TAG, "Cleared incremental cache for $key")
        }
    }

    fun clearAll() {
        if (rootDir.exists()) {
            rootDir.deleteRecursively()
            rootDir.mkdirs()
        }
    }

    fun isContentReplaceableEntry(entryName: String): Boolean {
        if (entryName == ApkTemplate.CONFIG_PATH) return true
        if (entryName == "${ApkTemplate.CONFIG_PATH}.enc") return true
        if (entryName == "assets/encryption_meta.json") return true
        if (entryName == "assets/wta_adblock_compiled.txt") return true
        if (entryName == "assets/wta_perf_optimize.js") return true
        if (entryName == "assets/statusbar_background.png") return true
        if (entryName == "assets/floating_window_minimized_icon.png") return true
        if (entryName.startsWith("assets/splash_media.")) return true
        if (entryName.startsWith("assets/error_page_media.")) return true
        if (entryName.startsWith("assets/media_content.")) return true
        if (entryName.startsWith("assets/gallery/")) return true
        if (entryName.startsWith("assets/bgm/")) return true
        if (entryName.startsWith("assets/html/")) return true
        if (entryName.startsWith("assets/html_projects/")) return true
        if (entryName.startsWith("assets/nodejs_app/")) return true
        if (entryName.startsWith("assets/php_app/")) return true
        if (entryName.startsWith("assets/python_app/")) return true
        if (entryName.startsWith("assets/go_app/")) return true
        if (entryName.startsWith("assets/frontend_app/")) return true
        if (entryName.startsWith("assets/wordpress/")) return true
        if (entryName.startsWith("assets/multiweb_sites/")) return true
        if (entryName.startsWith("assets/multi_web/")) return true
        return false
    }

    private data class CacheEntry(
        val meta: ApkBuildCacheMeta,
        val unsignedApk: File
    )

    private fun load(key: String): CacheEntry? {
        val dir = File(rootDir, key)
        val metaFile = File(dir, META_NAME)
        val unsigned = File(dir, UNSIGNED_NAME)
        if (!metaFile.isFile || !unsigned.isFile) return null
        val meta = ApkBuildCacheMeta.fromJson(metaFile.readText()) ?: return null
        return CacheEntry(meta, unsigned)
    }

    private fun trimOldCaches() {
        val dirs = rootDir.listFiles()?.filter { it.isDirectory } ?: return
        if (dirs.size <= MAX_CACHE_APPS) return
        dirs.sortedBy {
            val metaFile = File(it, META_NAME)
            if (metaFile.isFile) {
                ApkBuildCacheMeta.fromJson(metaFile.readText())?.updatedAt ?: it.lastModified()
            } else {
                it.lastModified()
            }
        }.take(dirs.size - MAX_CACHE_APPS).forEach { old ->
            try {
                old.deleteRecursively()
                AppLogger.i(TAG, "Evicted incremental cache ${old.name}")
            } catch (_: Exception) {
            }
        }
    }

    private fun identityFingerprint(
        config: ApkConfig,
        shellTemplateId: String,
        encryptionEnabled: Boolean,
        abiFilters: List<String>,
        iconPath: String?
    ): String {
        val parts = mutableListOf<String>()
        parts += "shell=$shellTemplateId"
        parts += "pkg=${config.packageName}"
        parts += "vc=${config.versionCode}"
        parts += "vn=${config.versionName}"
        parts += "name=${config.appName}"
        parts += "type=${config.appType}"
        parts += "engine=${config.engineType}"
        parts += "theme=${config.themeType}"
        parts += "abi=${abiFilters.sorted().joinToString(",")}"
        parts += "enc=$encryptionEnabled"
        parts += "icon=${fileFingerprint(iconPath)}"
        parts += "disguise=${config.disguiseConfig?.let { "${it.getAliasCount()}|${it}" } ?: "none"}"
        parts += "deeplinkHosts=${config.deepLinkHosts.sorted().joinToString(",")}"
        parts += "deeplinkSchemes=${config.deepLinkSchemes.sorted().joinToString(",")}"
        parts += "runtimePerms=${config.runtimePermissions}"
        parts += "networkTrust=${config.networkTrustConfig}"
        return sha256(parts.joinToString("\n"))
    }

    private fun contentFingerprint(
        config: ApkConfig,
        projectDirs: List<File?>,
        mediaContentPath: String?,
        splashMediaPath: String?,
        bgmPlaylistPaths: List<String>,
        htmlFiles: List<com.webtoapp.data.model.HtmlFile>,
        galleryItems: List<com.webtoapp.data.model.GalleryItem>,
        errorPageMediaPath: String?,
        statusBarImage: String?,
        floatingIcon: String?
    ): String {
        val parts = mutableListOf<String>()
        parts += "configJson=${ApkConfigJsonFactory.create(config)}"
        projectDirs.filterNotNull().forEach { dir ->
            parts += "dir=${dir.absolutePath}|${treeFingerprint(dir)}"
        }
        parts += "media=${fileFingerprint(mediaContentPath)}"
        parts += "splash=${fileFingerprint(splashMediaPath)}"
        parts += "errorPage=${fileFingerprint(errorPageMediaPath)}"
        parts += "statusBar=${fileFingerprint(statusBarImage)}"
        parts += "floatingIcon=${fileFingerprint(floatingIcon)}"
        bgmPlaylistPaths.forEachIndexed { index, path ->
            parts += "bgm[$index]=${fileFingerprint(path)}"
        }
        htmlFiles.forEachIndexed { index, file ->
            parts += "html[$index]=${file.name}|${fileFingerprint(file.path)}"
        }
        galleryItems.forEachIndexed { index, item ->
            parts += "gallery[$index]=${item.path}|${fileFingerprint(item.path)}|thumb=${fileFingerprint(item.thumbnailPath)}"
        }
        return sha256(parts.joinToString("\n"))
    }

    private fun fileFingerprint(path: String?): String {
        if (path.isNullOrBlank()) return "none"
        val file = File(path)
        if (!file.isFile) return "missing:$path"
        return "file:${file.absolutePath}|${file.length()}|${file.lastModified()}"
    }

    private fun treeFingerprint(dir: File): String {
        if (!dir.isDirectory) return "missing:${dir.absolutePath}"
        val digest = MessageDigest.getInstance("SHA-256")
        dir.walkTopDown()
            .filter { it.isFile }
            .sortedBy { it.relativeTo(dir).path }
            .forEach { file ->
                val rel = file.relativeTo(dir).path
                digest.update(rel.toByteArray())
                digest.update(file.length().toString().toByteArray())
                digest.update(file.lastModified().toString().toByteArray())
            }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun sha256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
