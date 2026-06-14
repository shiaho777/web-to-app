package com.webtoapp.core.wordpress

import android.content.Context
import android.os.Build
import com.webtoapp.core.download.DependencyDownloadEngine
import com.webtoapp.core.download.DependencyDownloadNotification
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale

object WordPressDependencyManager {

    private const val TAG = "DependencyManager"

    const val PHP_VERSION = "8.4"

    const val WORDPRESS_VERSION = "6.9.1"

    const val SQLITE_PLUGIN_VERSION = "2.2.17"

    enum class MirrorRegion { CN, GLOBAL }

    private val GITHUB_CN_PROXIES = listOf(
        "https://ghfast.top/",
        "https://gh-proxy.com/"
    )

    private val PHP_GITHUB_URL = "https://github.com/pmmp/PHP-Binaries/releases/download/pm5-php-${PHP_VERSION}-latest/PHP-${PHP_VERSION}-Android-arm64-PM5.tar.gz"

    data class MirrorConfig(

        val phpUrls: List<String>,

        val wordpressUrls: List<String>,
        val sqlitePluginUrl: String
    )

    private fun buildCnMirror(): MirrorConfig {
        val orderedProxies = com.webtoapp.core.network.CnMirrorProbe.getOrderedProxies(GITHUB_CN_PROXIES)
        return MirrorConfig(
            phpUrls = orderedProxies.map { proxy -> "${proxy}${PHP_GITHUB_URL}" } + PHP_GITHUB_URL,
            wordpressUrls = listOf(
                "https://cn.wordpress.org/wordpress-${WORDPRESS_VERSION}-zh_CN.tar.gz",
                "https://cn.wordpress.org/latest-zh_CN.tar.gz",
                "https://wordpress.org/wordpress-${WORDPRESS_VERSION}.tar.gz",
                "https://wordpress.org/latest.tar.gz"
            ),
            sqlitePluginUrl = "https://downloads.wordpress.org/plugin/"
        )
    }

    private val GLOBAL_MIRROR = MirrorConfig(
        phpUrls = listOf(PHP_GITHUB_URL),
        wordpressUrls = listOf(
            "https://wordpress.org/wordpress-${WORDPRESS_VERSION}.tar.gz",
            "https://wordpress.org/latest.tar.gz"
        ),
        sqlitePluginUrl = "https://downloads.wordpress.org/plugin/"
    )

    sealed class DownloadState {
        object Idle : DownloadState()
        data class Downloading(val progress: Float, val currentFile: String, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
        data class Verifying(val fileName: String) : DownloadState()
        data class Extracting(val fileName: String) : DownloadState()
        object Complete : DownloadState()
        data class Error(val message: String, val retryable: Boolean = true) : DownloadState()

        data class Paused(val progress: Float, val currentFile: String, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    }

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    private var _userMirrorRegion: MirrorRegion? = null

    fun setMirrorRegion(region: MirrorRegion?) {
        _userMirrorRegion = region
    }

    fun getMirrorRegion(): MirrorRegion {
        _userMirrorRegion?.let { return it }
        val lang = Locale.getDefault().language
        return if (lang == "zh") MirrorRegion.CN else MirrorRegion.GLOBAL
    }

    fun getMirrorConfig(): MirrorConfig {
        return when (getMirrorRegion()) {
            MirrorRegion.CN -> buildCnMirror()
            MirrorRegion.GLOBAL -> GLOBAL_MIRROR
        }
    }

    fun getDepsDir(context: Context): File {
        return File(context.filesDir, "wordpress_deps").also { it.mkdirs() }
    }

    fun getPhpDir(context: Context): File {
        val abi = getDeviceAbi()
        return File(getDepsDir(context), "php/$abi").also { it.mkdirs() }
    }

    fun getWordPressProjectsDir(context: Context): File {
        return File(context.filesDir, "wordpress_projects").also { it.mkdirs() }
    }

    fun isPhpReady(context: Context): Boolean {
        return resolvePhpExecutable(context) != null
    }

    fun getPhpExecutablePath(context: Context): String {
        resolvePhpExecutable(context)?.let { phpBinary ->
            return phpBinary.absolutePath
        }

        val fallback = File(context.applicationInfo.nativeLibraryDir, "libphp.so")
        AppLogger.d(TAG, "PHP binary not ready, returning nativeLib placeholder path: ${fallback.absolutePath}")
        return fallback.absolutePath
    }

    fun buildPhpExecPrefix(context: Context): List<String> {
        val phpPath = getPhpExecutablePath(context)
        return listOf(phpPath)
    }

    fun isWordPressReady(context: Context): Boolean {
        val wpDir = File(getDepsDir(context), "wordpress")
        return wpDir.exists() && File(wpDir, "wp-includes/version.php").exists()
    }

    fun isSqlitePluginReady(context: Context): Boolean {
        val pluginDir = File(getDepsDir(context), "sqlite-database-integration")
        return pluginDir.exists() && File(pluginDir, "load.php").exists()
    }

    fun isAllReady(context: Context): Boolean {
        return isPhpReady(context) && isWordPressReady(context) && isSqlitePluginReady(context)
    }

    suspend fun downloadAllDependencies(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            _downloadState.value = DownloadState.Idle

            DependencyDownloadNotification.getInstance(context)
            DependencyDownloadEngine.reset()
            val mirror = getMirrorConfig()

            if (!isPhpReady(context)) {
                val success = downloadPhp(context, mirror)
                if (!success) return@withContext false
            }

            if (!isWordPressReady(context)) {
                val success = downloadWordPress(context, mirror)
                if (!success) return@withContext false
            }

            if (!isSqlitePluginReady(context)) {
                val success = downloadSqlitePlugin(context, mirror)
                if (!success) return@withContext false
            }

            markComplete()
            AppLogger.i(TAG, "All WordPress dependencies downloaded")
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to download dependency", e)
            markError(e.message ?: "未知错误")
            false
        }
    }

    suspend fun downloadPhpDependency(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isPhpReady(context)) {
                DependencyDownloadNotification.getInstance(context)
                markComplete()
                return@withContext true
            }
            DependencyDownloadNotification.getInstance(context)
            DependencyDownloadEngine.reset()
            val mirror = getMirrorConfig()
            val ok = downloadPhp(context, mirror)
            if (ok) markComplete()
            return@withContext ok
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to download PHP dependency", e)
            markError(e.message ?: "未知错误")
            false
        }
    }

    fun clearCache(context: Context) {
        getDepsDir(context).deleteRecursively()
        AppLogger.i(TAG, "Dependency cache cleared")
    }

    fun getCacheSize(context: Context): Long {
        return getDepsDir(context).walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun getDeviceAbi(): String {
        return Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
    }

    private fun resolvePhpExecutable(context: Context): File? {

        val nativePhp = File(context.applicationInfo.nativeLibraryDir, "libphp.so")
        if (nativePhp.exists() && nativePhp.canExecute()) {
            AppLogger.d(TAG, "Using nativeLibraryDir PHP: ${nativePhp.absolutePath}")
            return nativePhp
        }

        val abi = getDeviceAbi()
        val downloadedPhp = File(getPhpDir(context), "bin/php")
        if (downloadedPhp.exists() && downloadedPhp.length() > 1024 * 1024) {

            if (!downloadedPhp.canExecute()) {
                downloadedPhp.setExecutable(true, false)
            }
            AppLogger.d(TAG, "Using downloaded PHP: ${downloadedPhp.absolutePath} (abi=$abi)")
            return downloadedPhp
        }

        val downloadedPhpFlat = File(getPhpDir(context), "php")
        if (downloadedPhpFlat.exists() && downloadedPhpFlat.length() > 1024 * 1024) {
            if (!downloadedPhpFlat.canExecute()) {
                downloadedPhpFlat.setExecutable(true, false)
            }
            AppLogger.d(TAG, "Using downloaded PHP (legacy layout): ${downloadedPhpFlat.absolutePath}")
            return downloadedPhpFlat
        }

        AppLogger.d(
            TAG,
            "PHP not ready: nativeLib=${nativePhp.exists()}, downloaded=${downloadedPhp.exists()}"
        )
        return null
    }

    private fun repairPhpExecutable(file: File) {
        if (!file.exists() || !file.isFile) return

        val wasReadable = file.canRead()
        val wasExecutable = file.canExecute()

        if (!wasReadable) {
            file.setReadable(true, false)
        }
        if (!wasExecutable) {
            file.setExecutable(true, false)
        }

        if (!wasExecutable && file.canExecute()) {
            AppLogger.i(TAG, "Fixed PHP binary execute permission: ${file.absolutePath}")
        } else if (!file.canExecute()) {
            AppLogger.w(TAG, "PHP binary still not executable: ${file.absolutePath}")
        }
    }

    private const val MAX_RETRY_PER_URL = 2

    private const val RETRY_DELAY_MS = 2000L

    private suspend fun downloadWithRetry(
        urls: List<String>,
        destFile: File,
        displayName: String,
        context: Context?
    ): Boolean {
        for ((urlIndex, url) in urls.withIndex()) {
            val sourceName = if (urls.size > 1) "$displayName [源${urlIndex + 1}/${urls.size}]" else displayName
            AppLogger.i(TAG, "Attempting to download $sourceName: $url")

            for (attempt in 1..MAX_RETRY_PER_URL) {
                val success = DependencyDownloadEngine.downloadFile(url, destFile, sourceName, context)
                if (success) return true

                if (attempt < MAX_RETRY_PER_URL) {
                    AppLogger.i(TAG, "$sourceName download failed, retrying in ${RETRY_DELAY_MS / 1000}s ($attempt/$MAX_RETRY_PER_URL)")
                    kotlinx.coroutines.delay(RETRY_DELAY_MS)
                    DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Idle)
                }
            }

            if (urlIndex < urls.lastIndex) {
                val tmpFile = File(destFile.parentFile, "${destFile.name}.tmp")
                tmpFile.delete()
                AppLogger.i(TAG, "$sourceName failed, switching to next source...")
                DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Idle)
            }
        }
        return false
    }

    private suspend fun downloadWithRetry(
        url: String,
        destFile: File,
        displayName: String,
        context: Context?
    ): Boolean = downloadWithRetry(listOf(url), destFile, displayName, context)

    private suspend fun downloadPhp(context: Context, mirror: MirrorConfig): Boolean {
        val abi = getDeviceAbi()
        if (abi != "arm64-v8a") {
            AppLogger.e(TAG, "PHP binary only supports arm64-v8a; current device: $abi")
            markError("PHP 二进制仅支持 arm64 设备")
            return false
        }

        val phpUrls = mirror.phpUrls
        val fileName = phpUrls.first().substringAfterLast("/")
        val destDir = getPhpDir(context)
        val archiveFile = File(getDepsDir(context), fileName)

        AppLogger.i(TAG, "Downloading PHP binary (${phpUrls.size} sources)")

        val downloaded = downloadWithRetry(phpUrls, archiveFile, "PHP $PHP_VERSION ($abi)", context)
        syncEngineState()
        if (!downloaded) return false

        _downloadState.value = DownloadState.Extracting("PHP")
        DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Extracting("PHP"))
        try {
            extractTarGz(archiveFile, destDir)

            var phpBinary = File(destDir, "php")
            if (!phpBinary.exists()) {
                phpBinary = File(destDir, "bin/php")
            }

            if (!phpBinary.exists()) {
                phpBinary = destDir.walkTopDown().firstOrNull { it.name == "php" && it.isFile } ?: phpBinary
            }

            if (phpBinary.exists()) {
                phpBinary.setExecutable(true, false)

                val targetBinary = File(destDir, "php")
                if (phpBinary.absolutePath != targetBinary.absolutePath) {
                    phpBinary.copyTo(targetBinary, overwrite = true)
                    targetBinary.setExecutable(true, false)
                }
                AppLogger.i(TAG, "PHP binary ready: ${targetBinary.absolutePath}")
            } else {
                AppLogger.e(TAG, "PHP binary not found after extraction")
                markError("解压后未找到 PHP 二进制")
                return false
            }

            archiveFile.delete()
            return true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to extract PHP", e)
            markError("解压 PHP 失败: ${e.message}")
            return false
        }
    }

    private suspend fun downloadWordPress(context: Context, mirror: MirrorConfig): Boolean {
        val wpUrls = mirror.wordpressUrls
        val destDir = getDepsDir(context)

        val archiveFile = File(destDir, "wordpress-core.tar.gz")

        AppLogger.i(TAG, "Downloading WordPress core (${wpUrls.size} sources)")

        val downloaded = downloadWithRetry(wpUrls, archiveFile, "WordPress $WORDPRESS_VERSION", context)
        syncEngineState()
        if (!downloaded) return false

        _downloadState.value = DownloadState.Extracting("WordPress")
        DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Extracting("WordPress"))
        try {
            extractTarGz(archiveFile, destDir)
            archiveFile.delete()

            val wpDir = File(destDir, "wordpress")
            if (!wpDir.exists() || !File(wpDir, "wp-includes/version.php").exists()) {
                markError("WordPress 解压不完整")
                return false
            }

            AppLogger.i(TAG, "WordPress core ready")
            return true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to extract WordPress", e)
            markError("解压 WordPress 失败: ${e.message}")
            return false
        }
    }

    private suspend fun downloadSqlitePlugin(context: Context, mirror: MirrorConfig): Boolean {
        val fileName = "sqlite-database-integration.${SQLITE_PLUGIN_VERSION}.zip"
        val url = "${mirror.sqlitePluginUrl}sqlite-database-integration.${SQLITE_PLUGIN_VERSION}.zip"
        val destDir = getDepsDir(context)
        val archiveFile = File(destDir, fileName)

        AppLogger.i(TAG, "Downloading SQLite plugin: $url")

        val downloaded = downloadWithRetry(url, archiveFile, "SQLite Plugin $SQLITE_PLUGIN_VERSION", context)
        syncEngineState()
        if (!downloaded) return false

        _downloadState.value = DownloadState.Extracting("SQLite Plugin")
        DependencyDownloadEngine.publishState(DependencyDownloadEngine.State.Extracting("SQLite Plugin"))
        try {
            extractZip(archiveFile, destDir)
            archiveFile.delete()

            val pluginDir = File(destDir, "sqlite-database-integration")
            if (!pluginDir.exists()) {
                markError("SQLite 插件解压不完整")
                return false
            }

            AppLogger.i(TAG, "SQLite plugin ready")
            return true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to extract SQLite plugin", e)
            markError("解压 SQLite 插件失败: ${e.message}")
            return false
        }
    }

    private fun syncEngineState() {
        when (val es = DependencyDownloadEngine.state.value) {
            is DependencyDownloadEngine.State.Downloading -> {
                _downloadState.value = DownloadState.Downloading(
                    progress = es.progress,
                    currentFile = es.displayName,
                    bytesDownloaded = es.bytesDownloaded,
                    totalBytes = es.totalBytes
                )
            }
            is DependencyDownloadEngine.State.Paused -> {
                _downloadState.value = DownloadState.Paused(
                    progress = es.progress,
                    currentFile = es.displayName,
                    bytesDownloaded = es.bytesDownloaded,
                    totalBytes = es.totalBytes
                )
            }
            is DependencyDownloadEngine.State.Error -> {
                _downloadState.value = DownloadState.Error(es.message)
            }
            else -> {}
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

    private fun extractTarGz(archiveFile: File, destDir: File) {
        val processBuilder = ProcessBuilder("tar", "-xzf", archiveFile.absolutePath, "-C", destDir.absolutePath)
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0) {

            extractTarGzWithCommons(archiveFile, destDir)
        }
    }

    private fun extractTarGzWithCommons(archiveFile: File, destDir: File) {
        val gzIn = java.util.zip.GZIPInputStream(archiveFile.inputStream().buffered())
        val tarIn = org.apache.commons.compress.archivers.tar.TarArchiveInputStream(gzIn)

        var entry = tarIn.nextEntry
        while (entry != null) {
            val outFile = File(destDir, entry.name)
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                FileOutputStream(outFile).use { fos ->
                    tarIn.copyTo(fos)
                }

                if (entry.mode and 0b001_000_000 != 0) {
                    outFile.setExecutable(true, false)
                }
            }
            entry = tarIn.nextEntry
        }
        tarIn.close()
    }

    private fun extractZip(zipFile: File, destDir: File) {
        val zipInputStream = java.util.zip.ZipInputStream(zipFile.inputStream().buffered())
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val outFile = File(destDir, entry.name)
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                FileOutputStream(outFile).use { fos ->
                    zipInputStream.copyTo(fos)
                }
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
