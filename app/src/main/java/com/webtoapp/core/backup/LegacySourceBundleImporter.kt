package com.webtoapp.core.backup

import android.content.Context
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.GsonProvider
import com.webtoapp.util.SafeZip
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipInputStream

/**
 * Restores source exports made before the zip doubled as a v5 backup (#1522).
 * Those archives carry the runtime configuration (`app_config.json`), the
 * local web content (`assets/www/`) and loose media (`assets/files/`) — enough
 * to rebuild the project record even when the original install is gone.
 *
 * Media recovery follows the packager's deterministic loose-file write order
 * (gallery, splash, BGM, icon, announcement icon, error media); the numeric
 * prefix each entry carries is that order's index.
 */
internal class LegacySourceBundleImporter(private val context: Context) {

    internal data class Restored(
        val app: WebApp,
        val missingProjectFiles: Boolean
    )

    internal fun restore(input: InputStream): Restored {
        val staging = File(context.cacheDir, "legacy_source_${System.nanoTime()}").apply { mkdirs() }
        try {
            var configJson: String? = null
            val wwwDir = File(staging, "www")
            val looseDir = File(staging, "files")
            val looseFiles = sortedMapOf<Int, File>()
            var launcherIcon: File? = null

            ZipInputStream(BufferedInputStream(input)).use { zip ->
                val guard = SafeZip.EntryGuard()
                var entry = zip.nextEntry
                while (entry != null) {
                    guard.onEntry()
                    val name = entry.name
                    when {
                        name == DataBackupManager.SOURCE_BUNDLE_CONFIG -> {
                            val out = ByteArrayOutputStream()
                            guard.copyTo(zip, out)
                            configJson = out.toString(Charsets.UTF_8.name())
                        }
                        name.startsWith(WWW_PREFIX) && !entry.isDirectory -> {
                            val target = safeChild(wwwDir, name.removePrefix(WWW_PREFIX))
                            if (target != null) {
                                target.parentFile?.mkdirs()
                                FileOutputStream(target).use { guard.copyTo(zip, it) }
                            }
                        }
                        name.startsWith(FILES_PREFIX) && !entry.isDirectory -> {
                            val fileName = name.removePrefix(FILES_PREFIX)
                            val target = safeChild(looseDir, fileName)
                            val index = fileName.substringBefore('-').toIntOrNull()
                            if (target != null && index != null) {
                                target.parentFile?.mkdirs()
                                FileOutputStream(target).use { guard.copyTo(zip, it) }
                                looseFiles[index] = target
                            }
                        }
                        name.startsWith(ICON_PREFIX) && !entry.isDirectory -> {
                            val target = safeChild(staging, name.substringAfterLast('/'))
                            if (target != null) {
                                FileOutputStream(target).use { guard.copyTo(zip, it) }
                                launcherIcon = target
                            }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            val json = configJson ?: throw IllegalArgumentException("No app_config.json in source bundle")
            val config = GsonProvider.gson.fromJson(json, ShellConfig::class.java)
                ?: throw IllegalArgumentException("Unreadable app_config.json")
            var app = config.toRestoredWebApp()

            app = rebuildProjects(app, wwwDir)
            app = recoverMedia(app, config, looseFiles.values.toList(), launcherIcon)

            val missing = app.appType.requiresProcessExec ||
                app.appType == AppType.IMAGE || app.appType == AppType.VIDEO
            return Restored(app, missing)
        } finally {
            staging.deleteRecursively()
        }
    }

    private fun rebuildProjects(app: WebApp, wwwDir: File): WebApp {
        if (!wwwDir.isDirectory) return app
        return when (app.appType) {
            AppType.HTML, AppType.FRONTEND -> {
                val projectId = UUID.randomUUID().toString()
                val target = File(context.filesDir, "html_projects/$projectId")
                wwwDir.copyRecursively(target, overwrite = true)
                app.copy(htmlConfig = app.htmlConfig?.copy(projectId = projectId))
            }
            AppType.MULTI_WEB -> {
                var config = app.multiWebConfig ?: return app
                val sitesDir = File(wwwDir, "sites")
                val rootFiles = wwwDir.listFiles().orEmpty().filter { it.isFile }
                if (rootFiles.isNotEmpty() && config.projectId.isNotBlank()) {
                    val projectId = UUID.randomUUID().toString()
                    val target = File(context.filesDir, "html_projects/$projectId").apply { mkdirs() }
                    rootFiles.forEach { it.copyTo(File(target, it.name), overwrite = true) }
                    wwwDir.walkTopDown()
                        .filter { it.isDirectory && it != wwwDir && it.parentFile == wwwDir && it.name != "sites" }
                        .forEach { it.copyRecursively(File(target, it.name), overwrite = true) }
                    config = config.copy(projectId = projectId)
                }
                val sites = config.sites.map { site ->
                    var updated = site
                    val segment = sanitize(site.id)
                    val siteDir = File(sitesDir, segment)
                    if (siteDir.isDirectory) {
                        val projectFiles = siteDir.listFiles().orEmpty()
                            .filter { it.name != "inline.html" && it.name != "local" }
                        if (projectFiles.isNotEmpty() && site.sourceProjectId.isNotBlank()) {
                            val projectId = UUID.randomUUID().toString()
                            val target = File(context.filesDir, "html_projects/$projectId").apply { mkdirs() }
                            projectFiles.forEach { child ->
                                if (child.isDirectory) {
                                    child.copyRecursively(File(target, child.name), overwrite = true)
                                } else {
                                    child.copyTo(File(target, child.name), overwrite = true)
                                }
                            }
                            updated = updated.copy(sourceProjectId = projectId)
                        }
                        File(siteDir, "inline.html").takeIf { it.isFile }?.let {
                            updated = updated.copy(inlineHtml = it.readText())
                        }
                        File(siteDir, "local").takeIf { it.isFile }?.let {
                            updated = updated.copy(localFilePath = place(it, "backup_multi_web", "site_${sanitize(site.id)}"))
                        }
                    }
                    updated
                }
                app.copy(multiWebConfig = config.copy(sites = sites))
            }
            else -> app
        }
    }

    private fun recoverMedia(app: WebApp, config: ShellConfig, loose: List<File>, launcherIcon: File?): WebApp {
        var cursor = 0
        fun next(): File? = loose.getOrNull(cursor++)
        var result = app

        if (app.appType == AppType.GALLERY) {
            val items = app.galleryConfig?.items.orEmpty().mapIndexed { index, item ->
                var updated = item.copy(sortIndex = index)
                next()?.let { file ->
                    updated = updated.copy(path = place(file, "backup_gallery", "legacy_${UUID.randomUUID()}"))
                }
                if (config.galleryConfig.items.getOrNull(index)?.thumbnailPath != null) {
                    next()?.let { file ->
                        updated = updated.copy(thumbnailPath = place(file, "backup_gallery", "legacy_thumb_${UUID.randomUUID()}"))
                    }
                }
                updated
            }
            result = result.copy(galleryConfig = app.galleryConfig?.copy(items = items))
        }

        if (app.splashEnabled) {
            next()?.let { file ->
                result = result.copy(
                    splashConfig = (result.splashConfig ?: com.webtoapp.data.model.SplashConfig())
                        .copy(mediaPath = place(file, "backup_splash", "legacy_${UUID.randomUUID()}"))
                )
            }
        }

        if (app.bgmEnabled) {
            val playlist = result.bgmConfig?.playlist.orEmpty().mapIndexed { index, item ->
                var updated = item
                next()?.let { file ->
                    updated = updated.copy(path = place(file, "backup_bgm", "legacy_${UUID.randomUUID()}"))
                }
                val shellItem = config.bgmPlaylist.getOrNull(index)
                if (shellItem?.coverAssetPath != null) {
                    next()?.let { file ->
                        updated = updated.copy(coverPath = place(file, "backup_bgm_cover", "legacy_${UUID.randomUUID()}"))
                    }
                }
                if (shellItem?.lrcAssetPath != null) {
                    next()?.let { file ->
                        updated = updated.copy(lrcPath = place(file, "backup_bgm_lrc", "legacy_${UUID.randomUUID()}"))
                    }
                }
                updated
            }
            result = result.copy(bgmConfig = result.bgmConfig?.copy(playlist = playlist))
        }

        if (launcherIcon != null) {
            val iconSource = next() ?: launcherIcon
            result = result.copy(iconPath = place(iconSource, "backup_icons", "legacy_${UUID.randomUUID()}"))
        }

        if (config.announcementHasCustomIcon) {
            next()?.let { file ->
                result = result.copy(
                    announcement = result.announcement?.copy(
                        customIconPath = place(file, "backup_icons", "legacy_announcement_${UUID.randomUUID()}")
                    )
                )
            }
        }

        if (config.webViewConfig.errorPageConfig.customMediaPath.isNotBlank()) {
            next()?.let { file ->
                result = result.copy(
                    webViewConfig = result.webViewConfig.copy(
                        errorPageConfig = result.webViewConfig.errorPageConfig.copy(
                            customMediaPath = place(file, "backup_media", "legacy_error_${UUID.randomUUID()}")
                        )
                    )
                )
            }
        }

        return result
    }

    private fun place(source: File, dirName: String, baseName: String): String {
        val dir = File(context.filesDir, dirName).apply { mkdirs() }
        val extension = source.extension.ifBlank { "bin" }
        val target = File(dir, "$baseName.$extension")
        source.copyTo(target, overwrite = true)
        return target.absolutePath
    }

    private fun safeChild(baseDir: File, relativePath: String): File? {
        if (relativePath.isBlank()) return null
        val normalized = relativePath.replace('\\', '/')
        if (normalized.startsWith("/") || normalized.split('/').any { it == ".." || it.isEmpty() }) return null
        val target = File(baseDir, normalized)
        return if (target.canonicalPath.startsWith(baseDir.canonicalPath + File.separator)) target else null
    }

    companion object {
        private const val WWW_PREFIX = "app/src/main/assets/www/"
        private const val FILES_PREFIX = "app/src/main/assets/files/"
        private const val ICON_PREFIX = "app/src/main/res/drawable/ic_launcher."

        private fun sanitize(name: String): String =
            name.replace(Regex("[^a-zA-Z0-9._\\-\\u4e00-\\u9fa5]"), "_").take(60)
    }
}