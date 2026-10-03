package com.webtoapp.core.sample

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.Strings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

data class TypedSampleProject(
    val id: String,
    val name: String,
    val description: String,
    val frameworkName: String,
    val icon: String,
    val tags: List<String>,
    val brandColor: Long
)

object SampleProjectExtractor {

    private const val TAG = "SampleProjectExtractor"
    private const val SAMPLES_DIR = "sample_projects"

    private const val SAMPLE_CONTENT_VERSION = 6

    private val hiddenAssetsLock = Any()
    private val hiddenAssetsCache = mutableMapOf<String, Set<String>>()

    fun getLanguageSuffix(): String {
        return when (Strings.currentLanguage.value) {
            AppLanguage.CHINESE -> ""
            AppLanguage.ENGLISH -> "-en"
            AppLanguage.ARABIC -> "-ar"
            AppLanguage.PORTUGUESE -> "-en"
            AppLanguage.SPANISH -> "-en"
            AppLanguage.FRENCH -> "-en"
            AppLanguage.GERMAN -> "-en"
            AppLanguage.RUSSIAN -> "-en"
            AppLanguage.JAPANESE -> "-en"
            AppLanguage.KOREAN -> "-en"
        }
    }

    suspend fun extractSampleProject(
        context: Context,
        projectId: String,
        forceRefresh: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val outputDir = File(context.filesDir, "sample_projects/$projectId")

            val versionFile = File(outputDir, ".version")
            val currentVersion = getAppVersionCode(context)
            val cachedVersion = if (versionFile.exists()) versionFile.readText().trim().toLongOrNull() else null

            if (!forceRefresh && outputDir.exists() && cachedVersion == currentVersion) {

                if (outputDir.listFiles()?.any { it.name != ".version" } == true) {
                    AppLogger.d(TAG, "示例项目已存在且版本匹配: ${outputDir.absolutePath}")
                    return@withContext Result.success(outputDir.absolutePath)
                }
            }

            AppLogger.i(TAG, "解压示例项目: $projectId (版本: $cachedVersion -> $currentVersion)")

            outputDir.deleteRecursively()
            outputDir.mkdirs()

            val assetPath = "$SAMPLES_DIR/$projectId"
            copyAssetFolder(context, assetPath, outputDir)
            ensureSharedSampleAssets(context, projectId, outputDir)

            versionFile.writeText(currentVersion.toString())

            AppLogger.i(TAG, "示例项目已解压: ${outputDir.absolutePath}")
            Result.success(outputDir.absolutePath)

        } catch (e: Exception) {
            AppLogger.e(TAG, "解压示例项目失败: $projectId", e)
            Result.failure(e)
        }
    }

    private fun getAppVersionCode(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            val sampleVersion = SAMPLE_CONTENT_VERSION.toLong()
            versionCode xor sampleVersion xor (packageInfo.lastUpdateTime / 1000)
        } catch (e: Exception) {
            0L
        }
    }

    private fun copyAssetFolder(context: Context, assetPath: String, targetDir: File) {
        val assetManager = context.assets

        try {
            val files = assetManager.list(assetPath) ?: return

            if (files.isEmpty()) {
                assetManager.open(assetPath).use { input ->
                    File(targetDir.parent, targetDir.name).outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                targetDir.mkdirs()
                for (file in files) {
                    copyAssetFolder(context, "$assetPath/$file", File(targetDir, file))
                }

                supplementHiddenAssetsFromApk(context, assetPath, targetDir, visibleNames = files.toSet())
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "复制文件失败: $assetPath", e)
        }
    }

    private fun supplementHiddenAssetsFromApk(
        context: Context,
        assetPath: String,
        targetDir: File,
        visibleNames: Set<String>,
    ) {
        val hiddenChildren = synchronized(hiddenAssetsLock) {
            hiddenAssetsCache[assetPath]?.let { cached ->
                cached - visibleNames
            } ?: run {
                val sourceApk = context.applicationInfo?.sourceDir?.takeIf { it.isNotBlank() }
                val apkFile = sourceApk?.let { File(it) }
                if (apkFile == null || !apkFile.exists() || !apkFile.isFile) {
                    hiddenAssetsCache[assetPath] = emptySet()
                    emptySet()
                } else {
                    val prefix = "assets/${assetPath.trimStart('/')}/"
                    val collected = mutableSetOf<String>()
                    try {
                        ZipFile(apkFile).use { zip ->
                            val entries = zip.entries()
                            while (entries.hasMoreElements()) {
                                val name = entries.nextElement().name
                                if (!name.startsWith(prefix)) continue
                                val rel = name.removePrefix(prefix)
                                if (rel.isEmpty()) continue
                                val firstSeg = rel.substringBefore('/')
                                if (firstSeg.startsWith(".")) {
                                    collected.add(firstSeg)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "扫描 APK 隐藏 assets 失败: $assetPath", e)
                    }
                    hiddenAssetsCache[assetPath] = collected
                    collected - visibleNames
                }
            }
        }

        if (hiddenChildren.isEmpty()) return
        AppLogger.i(TAG, "APK 隐藏 assets 兜底复制 ${hiddenChildren.size} 项: $hiddenChildren under $assetPath")
        for (childName in hiddenChildren) {
            val childAssetPath = "$assetPath/$childName"
            val childTarget = File(targetDir, childName)

            val ok = copyAssetFolderFromApk(context, childAssetPath, childTarget)
            if (!ok) {
                AppLogger.w(TAG, "APK 兜底复制失败: $childAssetPath")
            }
        }
    }

    /**
     * Shared dependency packs (python `.pypackages`, go `vendor`) are too heavy
     * to ship inside the APK (~30MB raw). Bundled assets still win when present
     * — a fork can re-embed `sample_projects/<pack>/` and skip downloads
     * entirely — otherwise they are fetched on first use via
     * [SampleSharedPackManager]. When the pack is unavailable the sample is
     * extracted without it and the runtime falls back to online dependency
     * installation (pip), matching the previous fail-soft behavior.
     */
    private suspend fun ensureSharedSampleAssets(
        context: Context,
        projectId: String,
        outputDir: File
    ) {
        val shared = when {
            projectId.startsWith("python-fastapi") -> "python-fastapi-shared" to ".pypackages"
            projectId.startsWith("python-django") -> "python-django-shared" to ".pypackages"
            projectId.startsWith("python-flask") -> "python-flask-shared" to ".pypackages"
            projectId.startsWith("go-gin") -> "go-gin-shared" to "vendor"
            projectId.startsWith("go-echo") -> "go-echo-shared" to "vendor"
            projectId.startsWith("go-fiber") -> "go-fiber-shared" to "vendor"
            else -> null
        } ?: return
        val (packName, subDir) = shared
        val target = File(outputDir, subDir)

        if (bundledPackExists(context, packName)) {
            copyShared(context, "$SAMPLES_DIR/$packName/$subDir", target)
            return
        }

        val packDir = SampleSharedPackManager.ensurePack(context, packName)
        val src = packDir?.let { File(it, subDir) }
        if (src != null && src.isDirectory) {
            AppLogger.i(TAG, "复制已下载共享依赖: ${src.absolutePath} -> ${target.absolutePath}")
            copyDirectory(src, target)
        }
        if (!target.walkTopDown().any { it.isFile }) {
            AppLogger.w(TAG, "共享 sample 依赖不可用: $packName（运行时回退到在线依赖安装）")
        }
    }

    /**
     * AssetManager.list() never reports dot-children like `.pypackages`, so a
     * bundled pack is detected by scanning the host APK entries directly.
     */
    private fun bundledPackExists(context: Context, packName: String): Boolean {
        val sourceApk = context.applicationInfo?.sourceDir?.takeIf { it.isNotBlank() }
            ?.let { File(it) } ?: return false
        if (!sourceApk.isFile) return false
        val prefix = "assets/$SAMPLES_DIR/$packName/"
        return try {
            ZipFile(sourceApk).use { zip ->
                zip.entries().asSequence().any { it.name.startsWith(prefix) }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "检测内置共享依赖失败: $packName", e)
            false
        }
    }

    private fun copyDirectory(src: File, dst: File) {
        src.walkTopDown().filter { it.isFile }.forEach { file ->
            val out = File(dst, file.relativeTo(src).path)
            out.parentFile?.mkdirs()
            file.inputStream().use { input ->
                out.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }

    private fun copyShared(context: Context, assetPath: String, target: File) {
        AppLogger.i(TAG, "复制共享 sample 依赖: $assetPath -> ${target.absolutePath}")
        copyAssetFolder(context, assetPath, target)
        if (!target.walkTopDown().any { it.isFile }) {
            AppLogger.w(TAG, "共享 sample 依赖为空: $assetPath（如果是 Go vendor 还没预生成可忽略）")
        }
    }

    private fun copyAssetFolderFromApk(
        context: Context,
        assetPath: String,
        targetDir: File
    ): Boolean {
        val sourceApk = context.applicationInfo?.sourceDir?.takeIf { it.isNotBlank() } ?: return false
        val apkFile = File(sourceApk)
        if (!apkFile.exists() || !apkFile.isFile) return false

        val prefix = "assets/${assetPath.trimStart('/')}/"
        var copiedAny = false
        ZipFile(apkFile).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.isDirectory || !entry.name.startsWith(prefix)) continue
                val relativePath = entry.name.removePrefix(prefix)
                if (relativePath.isBlank()) continue
                val targetFile = File(targetDir, relativePath)
                targetFile.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input ->
                    targetFile.outputStream().use { output -> input.copyTo(output) }
                }
                copiedAny = true
            }
        }
        return copiedAny
    }

    fun clearExtractedProjects(context: Context) {
        val samplesDir = File(context.filesDir, "sample_projects")
        if (samplesDir.exists()) {
            samplesDir.deleteRecursively()
        }
        SampleSharedPackManager.clearPacks(context)
    }
}
