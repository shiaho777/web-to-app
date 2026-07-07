package com.webtoapp.core.golang

import android.content.Context
import android.os.Build
import com.webtoapp.core.download.DependencyDownloadEngine
import com.webtoapp.core.download.DependencyDownloadNotification
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

object GoToolchainManager {

    private const val TAG = "GoToolchainManager"

    const val GO_VERSION = "1.26.4"

    private const val USTC_GO_ARCHIVE_URL =
        "https://mirrors.ustc.edu.cn/golang/go${GO_VERSION}.linux-arm64.tar.gz"

    private const val OFFICIAL_GO_ARCHIVE_URL =
        "https://dl.google.com/go/go${GO_VERSION}.linux-arm64.tar.gz"

    private val CN_GO_ARCHIVE_URLS = listOf(USTC_GO_ARCHIVE_URL)

    private val OFFSHORE_GO_ARCHIVE_URLS = listOf(OFFICIAL_GO_ARCHIVE_URL)

    private const val GO_ARCHIVE_SIZE_BYTES = 63_740_285L

    private const val MAX_RETRY_PER_URL = 2
    private const val RETRY_DELAY_MS = 2_000L

    sealed class DownloadState {
        object Idle : DownloadState()
        data class Downloading(
            val progress: Float,
            val currentFile: String,
            val bytesDownloaded: Long,
            val totalBytes: Long,
        ) : DownloadState()
        data class Verifying(val fileName: String) : DownloadState()
        data class Extracting(val fileName: String) : DownloadState()
        object Complete : DownloadState()
        data class Error(val message: String, val retryable: Boolean = true) : DownloadState()
        data class Paused(
            val progress: Float,
            val currentFile: String,
            val bytesDownloaded: Long,
            val totalBytes: Long,
        ) : DownloadState()
    }

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState
    private val installMutex = Mutex()

    fun getToolchainRoot(context: Context): File =
        File(context.filesDir, "go_toolchain").also { it.mkdirs() }

    fun getGoRoot(context: Context): File =
        File(getToolchainRoot(context), "go")

    fun getGoPath(context: Context): File =
        File(getToolchainRoot(context), "work").also { it.mkdirs() }

    fun getGoBinary(context: Context): File =
        File(getGoRoot(context), "bin/go")

    fun getGoFmtBinary(context: Context): File =
        File(getGoRoot(context), "bin/gofmt")

    fun getModCacheDir(context: Context): File =
        File(getGoPath(context), "pkg/mod").also { it.mkdirs() }

    fun getBuildCacheDir(context: Context): File =
        File(getToolchainRoot(context), "build-cache").also { it.mkdirs() }

    fun isGoReady(context: Context): Boolean {
        val goBin = getGoBinary(context)
        if (!goBin.exists() || !goBin.canExecute()) return false

        if (goBin.length() < 5L * 1024 * 1024) return false
        return true
    }

    fun ensureDnsPatched(context: Context) {
        val goBin = getGoBinary(context)
        if (!goBin.exists()) return
        runCatching {
            GoDnsPatcher.patchGoBinaryDnsPaths(context, goBin)
            GoDnsPatcher.refreshResolvConf(context)
        }.onFailure { AppLogger.w(TAG, "ensureDnsPatched 失败", it) }
    }

    fun isGoExecLoaderReady(context: Context): Boolean =
        GoDependencyManager.isGoExecLoaderReady(context)

    fun getCacheSize(context: Context): Long {
        return getToolchainRoot(context)
            .takeIf { it.exists() }
            ?.walkTopDown()
            ?.filter { it.isFile }
            ?.sumOf { it.length() }
            ?: 0L
    }

    fun clearCache(context: Context) {
        getToolchainRoot(context).deleteRecursively()
        AppLogger.i(TAG, "Go 工具链缓存已清理")
    }

    suspend fun installGoToolchain(context: Context): Boolean = withContext(Dispatchers.IO) {
        installMutex.withLock {
            DependencyDownloadNotification.getInstance(context)
            if (isGoReady(context)) {
                AppLogger.i(TAG, "Go 工具链已就绪，跳过下载")
                markComplete()
                return@withLock true
            }

            val abi = GoDependencyManager.getDeviceAbi()
            if (abi != "arm64-v8a") {

                AppLogger.e(TAG, "Go 工具链当前仅支持 arm64-v8a，设备 ABI: $abi")
                markError("当前设备架构 ($abi) 暂不支持 Go 工具链，仅支持 arm64-v8a")
                return@withLock false
            }

            try {
                _downloadState.value = DownloadState.Idle
                DependencyDownloadEngine.reset()

                val depsDir = getToolchainRoot(context)
                depsDir.mkdirs()
                val archiveFile = File(depsDir, "go-${GO_VERSION}.linux-arm64.tar.gz")

                val urlList = selectGoArchiveUrls(resolvePreferChinaMirror(context))
                val ok = downloadWithFallback(urlList, archiveFile, "Go $GO_VERSION ($abi)", context)
                syncEngineState()
                if (!ok) {
                    AppLogger.e(TAG, "Go 归档下载失败")
                    return@withLock false
                }

                val actual = archiveFile.length()
                val tolerance = (GO_ARCHIVE_SIZE_BYTES * 0.10).toLong()
                if (kotlin.math.abs(actual - GO_ARCHIVE_SIZE_BYTES) > tolerance) {
                    AppLogger.w(
                        TAG,
                        "Go 归档体积异常: 实际 $actual 字节，期望 $GO_ARCHIVE_SIZE_BYTES（容差 ±$tolerance）—— 可能镜像变更或下载损坏"
                    )

                }

                _downloadState.value = DownloadState.Extracting("Go $GO_VERSION")
                DependencyDownloadEngine.publishState(
                    DependencyDownloadEngine.State.Extracting("Go $GO_VERSION")
                )

                val goRoot = getGoRoot(context)
                goRoot.deleteRecursively()
                extractGoArchiveToRoot(archiveFile, goRoot)
                archiveFile.delete()

                fixupExecBits(goRoot)
                if (!isGoReady(context)) {
                    val goBin = getGoBinary(context)
                    AppLogger.e(
                        TAG,
                        "解压完成但 go binary 不可用: 路径=${goBin.absolutePath} 存在=${goBin.exists()} 大小=${goBin.length()} 可执行=${goBin.canExecute()}"
                    )
                    markError("Go 工具链解压不完整，请重试")
                    return@withLock false
                }

                val patched = GoDnsPatcher.patchGoBinaryDnsPaths(context, getGoBinary(context))
                AppLogger.i(TAG, "Go binary DNS 路径补丁应用次数: $patched")

                GoDnsPatcher.refreshResolvConf(context)

                AppLogger.i(TAG, "Go 工具链已就绪: ${getGoBinary(context).absolutePath}")
                markComplete()
                true
            } catch (e: Exception) {
                AppLogger.e(TAG, "安装 Go 工具链失败", e)
                markError(e.message ?: "未知错误")
                false
            }
        }
    }

    suspend fun verifyGoToolchain(context: Context): Result<String> = withContext(Dispatchers.IO) {
        if (!isGoReady(context)) {
            return@withContext Result.failure(IllegalStateException("Go 工具链未安装"))
        }
        val goBin = getGoBinary(context)
        try {
            val pb = ProcessBuilder(goBin.absolutePath, "version")
                .directory(getToolchainRoot(context))
                .redirectErrorStream(true)

            pb.environment()["GOROOT"] = getGoRoot(context).absolutePath
            pb.environment()["GOPATH"] = getGoPath(context).absolutePath
            pb.environment()["HOME"] = context.filesDir.absolutePath
            pb.environment()["TMPDIR"] = context.cacheDir.absolutePath
            val proc = pb.start()
            val out = proc.inputStream.bufferedReader().readText().trim()
            val finished = proc.waitFor()
            if (finished == 0 && out.isNotBlank()) {
                AppLogger.i(TAG, "Go verify OK: $out")
                Result.success(out)
            } else {
                AppLogger.e(TAG, "Go verify failed: exit=$finished, output=$out")
                Result.failure(RuntimeException(out.ifBlank { "exit code=$finished" }))
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Go verify 异常", e)
            Result.failure(e)
        }
    }

    private suspend fun downloadWithRetry(
        url: String,
        destFile: File,
        displayName: String,
        context: Context?,
    ): Boolean {
        for (attempt in 1..MAX_RETRY_PER_URL) {
            val ok = DependencyDownloadEngine.downloadFile(url, destFile, displayName, context)
            if (ok) return true
            if (attempt < MAX_RETRY_PER_URL) {
                AppLogger.i(TAG, "$displayName 下载失败, ${RETRY_DELAY_MS / 1000}s 后重试 ($attempt/$MAX_RETRY_PER_URL)")
                kotlinx.coroutines.delay(RETRY_DELAY_MS)
                DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Idle)
            }
        }
        return false
    }

    internal fun selectGoArchiveUrls(preferChinaMirror: Boolean): List<String> {
        return if (preferChinaMirror) {
            CN_GO_ARCHIVE_URLS + OFFSHORE_GO_ARCHIVE_URLS
        } else {
            OFFSHORE_GO_ARCHIVE_URLS
        }
    }

    internal fun shouldPreferChinaMirror(appLanguage: AppLanguage): Boolean {
        if (appLanguage == AppLanguage.CHINESE) return true
        return runCatching {
            val locale = java.util.Locale.getDefault()
            locale.language.equals("zh", ignoreCase = true) ||
                locale.country.equals("CN", ignoreCase = true) ||
                locale.country in setOf("HK", "MO", "TW")
        }.getOrDefault(false)
    }

    private suspend fun resolvePreferChinaMirror(context: Context): Boolean {
        val appLang = runCatching {
            com.webtoapp.core.i18n.LanguageManager.getInstance(context).getCurrentLanguage()
        }.getOrNull() ?: AppLanguage.CHINESE
        return shouldPreferChinaMirror(appLang)
    }

    private suspend fun downloadWithFallback(
        urls: List<String>,
        destFile: File,
        displayName: String,
        context: Context?,
    ): Boolean {
        if (urls.isEmpty()) {
            AppLogger.e(TAG, "$displayName 没有可用的下载源")
            return false
        }
        urls.forEachIndexed { index, url ->
            AppLogger.i(TAG, "$displayName 尝试源 ${index + 1}/${urls.size}: $url")
            val ok = downloadWithRetry(url, destFile, displayName, context)
            if (ok) {
                if (index > 0) {
                    AppLogger.i(TAG, "$displayName 从备选源下载成功: $url")
                }
                return true
            }
            AppLogger.w(TAG, "$displayName 源 $url 全部重试仍失败,切换下一源")
            DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Idle)
        }
        AppLogger.e(TAG, "$displayName 所有源均失败,共尝试 ${urls.size} 个")
        return false
    }

    private fun extractGoArchiveToRoot(archiveFile: File, destGoRoot: File) {
        destGoRoot.mkdirs()
        val marker = "go/"
        var copied = 0
        var skipped = 0
        val sampleNames = mutableListOf<String>()
        TarArchiveInputStream(
            GzipCompressorInputStream(BufferedInputStream(archiveFile.inputStream()))
        ).use { tar ->
            var entry = tar.nextTarEntry
            while (entry != null) {
                val raw = entry.name
                if (sampleNames.size < 5) sampleNames += raw

                val name = raw.trimStart('/').removePrefix("./")
                if (!name.startsWith(marker)) {
                    skipped++
                    entry = tar.nextTarEntry
                    continue
                }
                val rel = name.removePrefix(marker)
                if (rel.isEmpty()) {
                    entry = tar.nextTarEntry
                    continue
                }
                val out = File(destGoRoot, rel)
                when {
                    entry.isDirectory -> {
                        out.mkdirs()
                    }
                    entry.isSymbolicLink -> {

                        out.parentFile?.mkdirs()
                        try {
                            val target = entry.linkName

                            try {
                                java.nio.file.Files.createSymbolicLink(
                                    out.toPath(),
                                    File(target).toPath()
                                )
                            } catch (_: Exception) {

                                AppLogger.d(TAG, "symlink 跳过: $rel -> $target")
                            }
                        } catch (e: Exception) {
                            AppLogger.w(TAG, "symlink 解压失败: $rel -> ${entry.linkName}", e)
                        }
                    }
                    else -> {
                        out.parentFile?.mkdirs()
                        FileOutputStream(out).use { fos -> tar.copyTo(fos) }

                        if (entry.mode and 0b001_000_000 != 0) {
                            out.setExecutable(true, false)
                        }
                        out.setReadable(true, false)
                    }
                }
                copied++
                entry = tar.nextTarEntry
            }
        }
        AppLogger.i(
            TAG,
            "Go 归档解压完成: 复制 $copied 项，跳过 $skipped 项（非 GOROOT 内容）。" +
                "tar 前几条 entry 路径样本: ${sampleNames.joinToString(" | ")}"
        )
    }

    private fun fixupExecBits(goRoot: File) {
        val binDir = File(goRoot, "bin")
        binDir.listFiles()?.forEach {
            it.setExecutable(true, false)
            it.setReadable(true, false)
        }

        val toolDir = File(goRoot, "pkg/tool")
        toolDir.walkTopDown()
            .filter { it.isFile && it.length() > 100 * 1024 }
            .forEach {
                it.setExecutable(true, false)
                it.setReadable(true, false)
            }
    }

    private fun syncEngineState() {
        when (val es = DependencyDownloadEngine.state.value) {
            is DependencyDownloadEngine.State.Downloading -> {
                _downloadState.value = DownloadState.Downloading(
                    progress = es.progress,
                    currentFile = es.displayName,
                    bytesDownloaded = es.bytesDownloaded,
                    totalBytes = es.totalBytes,
                )
            }
            is DependencyDownloadEngine.State.Paused -> {
                _downloadState.value = DownloadState.Paused(
                    progress = es.progress,
                    currentFile = es.displayName,
                    bytesDownloaded = es.bytesDownloaded,
                    totalBytes = es.totalBytes,
                )
            }
            is DependencyDownloadEngine.State.Error -> {
                _downloadState.value = DownloadState.Error(es.message)
            }
            else -> {  }
        }
    }

    private fun markComplete() {
        _downloadState.value = DownloadState.Complete
        DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Complete)
    }

    private fun markError(message: String, retryable: Boolean = true) {
        _downloadState.value = DownloadState.Error(message, retryable = retryable)
        DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Error(message))
    }
}
