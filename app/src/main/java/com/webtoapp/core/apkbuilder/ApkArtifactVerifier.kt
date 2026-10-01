package com.webtoapp.core.apkbuilder

import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import com.webtoapp.data.model.HtmlFile
import com.webtoapp.data.model.MultiWebSite
import java.io.File
import java.util.zip.ZipFile

internal data class ApkArtifactVerificationRequest(
    val apkFile: File,
    val config: ApkConfig,
    val encryptionEnabled: Boolean,
    val htmlFiles: List<HtmlFile> = emptyList(),
    val galleryItems: List<GalleryItem> = emptyList(),
    val multiWebSites: List<MultiWebSite> = emptyList(),
    val frontendProjectDir: File? = null,
    val multiWebProjectDir: File? = null,
    val multiWebSiteSourceDirs: Map<String, File> = emptyMap()
)

internal data class ApkArtifactVerificationResult(
    val issues: List<ApkArtifactIssue>,
    val entryCount: Int = 0,
    val checkedEntryCount: Int = 0
) {
    val passed: Boolean get() = issues.isEmpty()
}

internal data class ApkArtifactIssue(
    val key: String,
    val message: String,
    val path: String? = null
) {
    fun summary(): String {
        return if (path.isNullOrBlank()) "$key: $message" else "$key: $message [$path]"
    }
}

internal object ApkArtifactVerifier {

    fun verify(request: ApkArtifactVerificationRequest): ApkArtifactVerificationResult {
        if (!request.apkFile.exists()) {
            return ApkArtifactVerificationResult(
                issues = listOf(
                    ApkArtifactIssue("apkFile", "APK artifact does not exist", request.apkFile.absolutePath)
                )
            )
        }

        if (!request.apkFile.canRead()) {
            return ApkArtifactVerificationResult(
                issues = listOf(
                    ApkArtifactIssue("apkFile", "APK artifact cannot be read", request.apkFile.absolutePath)
                )
            )
        }

        return try {
            ZipFile(request.apkFile).use { zip ->
                val entries = zip.entries().asSequence()
                    .filterNot { it.isDirectory }
                    .associateBy { it.name }
                val issues = mutableListOf<ApkArtifactIssue>()
                val checkedEntries = mutableSetOf<String>()

                issues.requireEntry(
                    entries = entries,
                    checkedEntries = checkedEntries,
                    key = "config",
                    path = ApkTemplate.CONFIG_PATH,
                    label = "app config"
                )

                if (request.encryptionEnabled) {
                    issues.requireEntry(
                        entries = entries,
                        checkedEntries = checkedEntries,
                        key = "encryptedConfig",
                        path = "${ApkTemplate.CONFIG_PATH}.enc",
                        label = "encrypted app config"
                    )
                }

                when (request.config.appType) {
                    "HTML" -> {
                        issues.requireAsset(
                            entries = entries,
                            checkedEntries = checkedEntries,
                            key = "htmlEntryFile",
                            path = "assets/html/${normalizeAssetPath(request.config.htmlEntryFile)}",
                            label = "HTML entry file",
                            encrypted = request.encryptionEnabled
                        )
                    }
                    "GALLERY" -> {
                        request.galleryItems.forEachIndexed { index, item ->
                            val ext = if (item.type == GalleryItemType.VIDEO) "mp4" else "png"
                            issues.requireAsset(
                                entries = entries,
                                checkedEntries = checkedEntries,
                                key = "galleryItems[$index]",
                                path = "assets/gallery/item_$index.$ext",
                                label = "gallery item ${index + 1}",
                                encrypted = request.encryptionEnabled
                            )
                        }
                    }
                    "FRONTEND" -> {
                        if (request.frontendProjectDir != null) {
                            issues.requireProjectAssets(
                                entries = entries,
                                checkedEntries = checkedEntries,
                                key = "frontendProject",
                                label = "frontend project",
                                projectDir = request.frontendProjectDir,
                                config = RuntimeAssetEmbedder.frontendConfig()
                            )
                        } else {
                            issues.requireAsset(
                                entries = entries,
                                checkedEntries = checkedEntries,
                                key = "frontendEntryFile",
                                path = "assets/html/${normalizeAssetPath(request.config.htmlEntryFile)}",
                                label = "frontend entry file",
                                encrypted = request.encryptionEnabled
                            )
                        }
                    }
                    "MULTI_WEB" -> issues.requireMultiWebAssets(
                        entries = entries,
                        checkedEntries = checkedEntries,
                        sites = request.multiWebSites,
                        projectDir = request.multiWebProjectDir,
                        siteSourceDirs = request.multiWebSiteSourceDirs
                    )
                }

                ApkArtifactVerificationResult(
                    issues = issues,
                    entryCount = entries.size,
                    checkedEntryCount = checkedEntries.size
                )
            }
        } catch (e: Exception) {
            ApkArtifactVerificationResult(
                issues = listOf(
                    ApkArtifactIssue(
                        key = "apkFile",
                        message = "APK artifact could not be opened: ${e.message ?: e::class.java.simpleName}",
                        path = request.apkFile.absolutePath
                    )
                )
            )
        }
    }

    private fun MutableList<ApkArtifactIssue>.requireAsset(
        entries: Map<String, java.util.zip.ZipEntry>,
        checkedEntries: MutableSet<String>,
        key: String,
        path: String,
        label: String,
        encrypted: Boolean
    ) {
        val artifactPath = if (encrypted) "$path.enc" else path
        requireEntry(entries, checkedEntries, key, artifactPath, label)
    }

    private fun MutableList<ApkArtifactIssue>.requireEntry(
        entries: Map<String, java.util.zip.ZipEntry>,
        checkedEntries: MutableSet<String>,
        key: String,
        path: String,
        label: String,
        allowEmpty: Boolean = false
    ) {
        checkedEntries += path
        val entry = entries[path]
        when {
            entry == null -> add(ApkArtifactIssue(key, "$label is missing from APK", path))
            entry.size == 0L && !allowEmpty -> add(ApkArtifactIssue(key, "$label is empty in APK", path))
        }
    }

    private fun MutableList<ApkArtifactIssue>.requireProjectAssets(
        entries: Map<String, java.util.zip.ZipEntry>,
        checkedEntries: MutableSet<String>,
        key: String,
        label: String,
        projectDir: File?,
        config: RuntimeAssetEmbedder.EmbedConfig
    ) {
        requireProjectAssets(
            entries = entries,
            checkedEntries = checkedEntries,
            key = key,
            label = label,
            projectDir = projectDir,
            assetPrefix = config.assetPrefix,
            excludeDirs = config.excludeDirs
        )
    }

    private fun MutableList<ApkArtifactIssue>.requireProjectAssets(
        entries: Map<String, java.util.zip.ZipEntry>,
        checkedEntries: MutableSet<String>,
        key: String,
        label: String,
        projectDir: File?,
        assetPrefix: String,
        excludeDirs: Set<String>,
        allowEmptyAssetPaths: Set<String> = emptySet()
    ) {
        if (projectDir == null) {
            add(ApkArtifactIssue(key, "$label directory was not resolved"))
            return
        }

        val expectedPaths = collectProjectAssetPaths(projectDir, assetPrefix, excludeDirs)
        if (expectedPaths.isEmpty()) {
            add(ApkArtifactIssue(key, "$label has no project files to verify", projectDir.absolutePath))
            return
        }

        expectedPaths.forEachIndexed { index, path ->
            val sourceFile = resolveSourceFileForAssetPath(projectDir, assetPrefix, path)
            requireEntry(
                entries = entries,
                checkedEntries = checkedEntries,
                key = "$key[$index]",
                path = path,
                label = "$label file",
                allowEmpty = path in allowEmptyAssetPaths ||
                    sourceFile?.length() == 0L
            )
        }
    }

    private fun collectProjectAssetPaths(
        projectDir: File,
        assetPrefix: String,
        excludeDirs: Set<String>
    ): List<String> {
        if (!projectDir.exists() || !projectDir.isDirectory) return emptyList()

        val basePrefix = assetPrefix.trimEnd('/')
        return projectDir.walkTopDown()
            .onEnter { dir -> dir == projectDir || dir.name !in excludeDirs }
            .filter { it.isFile }
            .map { file ->
                val relativePath = file.relativeTo(projectDir).invariantSeparatorsPath
                "$basePrefix/$relativePath"
            }
            .toList()
    }

    private fun resolveSourceFileForAssetPath(
        projectDir: File,
        assetPrefix: String,
        assetPath: String
    ): File? {
        val prefix = assetPrefix.trimEnd('/') + "/"
        if (!assetPath.startsWith(prefix)) return null
        val relativePath = assetPath.removePrefix(prefix)
        if (relativePath.isBlank()) return null
        return File(projectDir, relativePath)
    }

    private fun MutableList<ApkArtifactIssue>.requireMultiWebAssets(
        entries: Map<String, java.util.zip.ZipEntry>,
        checkedEntries: MutableSet<String>,
        sites: List<MultiWebSite>,
        projectDir: File?,
        siteSourceDirs: Map<String, File>
    ) {
        val localSites = sites.filter {
            it.enabled && it.localFilePath.isNotBlank()
        }
        if (localSites.isEmpty()) return

        localSites.forEachIndexed { index, site ->
            val relativePath = normalizeAssetPath(site.localFilePath)

            // Mirror MultiWebContentEmbedder: HTML/FRONTEND sites embed a whole
            // project directory under assets/multiweb_sites/<siteId>/html/ —
            // the site's own source project wins over the shared multi-web
            // project dir. The flat assets/html_projects/ layout is only
            // written by the legacy fallback when no site takes a per-site
            // embed.
            val siteType = site.appType.uppercase()
            if (siteType == "HTML" || siteType == "FRONTEND") {
                val siteDir = siteSourceDirs[site.id]
                val embedDir = siteDir ?: projectDir
                if (embedDir == null || !embedDir.exists()) {
                    add(
                        ApkArtifactIssue(
                            key = "multiWebSites[$index]",
                            message = "Multi-web local site source directory is missing",
                            path = (siteDir ?: projectDir)?.absolutePath
                        )
                    )
                    return@forEachIndexed
                }

                // A site embedded from its own source project lands at the
                // project root — the site-id prefix in localFilePath only
                // exists inside the shared multi-web project directory.
                val entryRelative = if (siteDir != null) {
                    relativePath.substringAfter('/', relativePath)
                } else {
                    relativePath
                }
                val expectedSource = File(embedDir, entryRelative)
                if (!expectedSource.exists() || !expectedSource.isFile) {
                    add(
                        ApkArtifactIssue(
                            key = "multiWebSites[$index]",
                            message = "Multi-web local site source file is missing",
                            path = expectedSource.absolutePath
                        )
                    )
                    return@forEachIndexed
                }

                requireEntry(
                    entries = entries,
                    checkedEntries = checkedEntries,
                    key = "multiWebSites[$index]",
                    path = "assets/multiweb_sites/${site.id}/html/$entryRelative",
                    label = "multi-web local site file"
                )
                return@forEachIndexed
            }

            if (projectDir == null) {
                add(
                    ApkArtifactIssue(
                        key = "multiWebSites[$index]",
                        message = "Multi-web project directory was not resolved"
                    )
                )
                return@forEachIndexed
            }
            val expectedSource = File(projectDir, relativePath)
            if (!expectedSource.exists() || !expectedSource.isFile) {
                add(
                    ApkArtifactIssue(
                        key = "multiWebSites[$index]",
                        message = "Multi-web local site source file is missing",
                        path = expectedSource.absolutePath
                    )
                )
                return@forEachIndexed
            }

            requireEntry(
                entries = entries,
                checkedEntries = checkedEntries,
                key = "multiWebSites[$index]",
                path = "assets/html_projects/$relativePath",
                label = "multi-web local site file"
            )
        }
    }

    private fun normalizeAssetPath(value: String): String {
        return value.trim().replace('\\', '/').trimStart('/')
    }

    private fun normalizePackagedAssetPath(value: String): String {
        val path = normalizeAssetPath(value).removePrefix("assets/")
        return "assets/$path"
    }

}
