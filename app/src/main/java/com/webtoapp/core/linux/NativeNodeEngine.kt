package com.webtoapp.core.linux

import android.content.Context
import android.os.Build
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.network.DownloadMirrorPreference
import com.webtoapp.core.network.GitHubMirror
import com.webtoapp.core.network.NetworkModule
import com.webtoapp.util.destroyForciblyCompat
import com.webtoapp.util.waitForCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.*

object NativeNodeEngine {

    private const val TAG = "NativeNodeEngine"

    private const val NODE_VERSION = "18.20.4"

    private val ESBUILD_PACKAGE_BY_ABI = mapOf(
        "arm64-v8a" to "android-arm64",
        "armeabi-v7a" to "android-arm",
        "x86_64" to "android-x64"
    )

    private val _state = MutableStateFlow<NodeEngineState>(NodeEngineState.NotInitialized)
    val state: StateFlow<NodeEngineState> = _state
    private val installMutex = Mutex()

    fun getArchitecture(): String {
        return when {
            Build.SUPPORTED_ABIS.contains("arm64-v8a") -> "arm64-v8a"
            Build.SUPPORTED_ABIS.contains("armeabi-v7a") -> "armeabi-v7a"
            Build.SUPPORTED_ABIS.contains("x86_64") -> "x86_64"
            Build.SUPPORTED_ABIS.contains("x86") -> "x86"
            else -> "arm64-v8a"
        }
    }

    private fun getEngineDir(context: Context): File {
        return File(context.filesDir, "node_engine")
    }

    fun getEsbuildPath(context: Context): File {
        return File(getEngineDir(context), "esbuild")
    }

    fun isAvailable(context: Context): Boolean {
        val esbuild = getEsbuildPath(context)
        return esbuild.exists() && esbuild.canExecute()
    }

    suspend fun initialize(
        context: Context,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ): Result<Unit> = withContext(Dispatchers.IO) {
        installMutex.withLock {
            try {
                _state.value = NodeEngineState.Initializing("检查环境", 0f)

                val engineDir = getEngineDir(context)
                engineDir.mkdirs()

                if (isAvailable(context)) {
                    _state.value = NodeEngineState.Ready
                    return@withLock Result.success(Unit)
                }

                _state.value = NodeEngineState.Initializing(Strings.nodeDownloadEsbuild, 0.1f)
                onProgress(Strings.nodeDownloadEsbuild, 0.1f)

                downloadEsbuild(context) { progress ->
                    _state.value = NodeEngineState.Initializing(Strings.nodeDownloadEsbuild, 0.1f + progress * 0.8f)
                    onProgress(Strings.nodeDownloadEsbuild, 0.1f + progress * 0.8f)
                }

                _state.value = NodeEngineState.Initializing(Strings.nodeVerifyInstall, 0.95f)
                onProgress(Strings.nodeVerifyInstall, 0.95f)

                if (!isAvailable(context)) {
                    throw Exception(Strings.nodeEsbuildInstallFailed)
                }

                _state.value = NodeEngineState.Ready
                onProgress("Done", 1f)

                AppLogger.w(TAG, "Node 引擎初始化完成")
                Result.success(Unit)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.e(TAG, "Initialization failed", e)
                _state.value = NodeEngineState.Error(e.message ?: "未知错误")
                Result.failure(e)
            }
        }
    }

    private suspend fun downloadEsbuild(
        context: Context,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val arch = getArchitecture()
        val packageName = ESBUILD_PACKAGE_BY_ABI[arch]
            ?: throw Exception(Strings.nodeUnsupportedArch.format(arch))
        val upstream = "${GitHubMirror.NPM_REGISTRY}/@esbuild/$packageName/-/$packageName-0.20.0.tgz"
        val urls = GitHubMirror.npmTarballUrls(upstream, DownloadMirrorPreference.prefersChina(context))

        val engineDir = getEngineDir(context)
        val tempFile = File(engineDir, "esbuild.tgz")
        val stagedBinary = File(engineDir, "esbuild.install")
        val esbuildFile = getEsbuildPath(context)

        try {
            var lastError: Exception? = null
            for (url in urls) {
                try {
                    downloadFile(url, tempFile, onProgress)
                    extractEsbuildFromTgz(tempFile, stagedBinary)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    lastError = e
                    AppLogger.w(TAG, "esbuild download from $url failed: ${e.message}")
                    continue
                }
                if (!stagedBinary.setExecutable(true, false) || !stagedBinary.renameTo(esbuildFile)) {
                    throw IOException(Strings.nodeEsbuildInstallFailed)
                }
                return@withContext
            }
            throw IOException(Strings.downloadFailed("esbuild", lastError?.message.orEmpty()), lastError)
        } finally {
            tempFile.delete()
            stagedBinary.delete()
        }
    }

    private fun extractEsbuildFromTgz(tgzFile: File, outputFile: File) {

        java.util.zip.GZIPInputStream(FileInputStream(tgzFile)).use { gzip ->
            org.apache.commons.compress.archivers.tar.TarArchiveInputStream(gzip).use { tar ->
                var entry = tar.nextTarEntry
                while (entry != null) {
                    if (entry.name.endsWith("/esbuild") || entry.name == "package/bin/esbuild") {
                        FileOutputStream(outputFile).use { out ->
                            tar.copyTo(out)
                        }
                        return
                    }
                    entry = tar.nextTarEntry
                }
            }
        }
        throw Exception(Strings.nodeEsbuildBinaryNotFound)
    }

    suspend fun executeEsbuild(
        context: Context,
        args: List<String>,
        workingDir: File,
        env: Map<String, String> = emptyMap(),
        timeout: Long = 300_000,
        onOutput: (String) -> Unit = {}
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        val esbuild = getEsbuildPath(context)
        if (!esbuild.exists()) {
            return@withContext ExecutionResult(
                exitCode = -1,
                stdout = "",
                stderr = Strings.nodeEsbuildNotInstalled,
                duration = 0
            )
        }

        val cmdList = mutableListOf(esbuild.absolutePath)
        cmdList.addAll(args)

        AppLogger.d(TAG, "执行: ${cmdList.joinToString(" ")}")

        val launchEnv = mutableMapOf(
            "HOME" to context.filesDir.absolutePath,
            "TMPDIR" to context.cacheDir.absolutePath
        )
        launchEnv.putAll(env)

        val launch = HostProcessLauncher.start(context, cmdList, launchEnv, workingDir, "esbuild")
        val process = launch.process
        if (process == null) {
            return@withContext ExecutionResult(
                exitCode = -1,
                stdout = "",
                stderr = launch.error ?: Strings.nodeEsbuildNotInstalled,
                duration = System.currentTimeMillis() - startTime
            )
        }

        val stdout = StringBuilder()
        val stderr = StringBuilder()

        val stdoutReader = Thread {
            process.inputStream.bufferedReader().forEachLine { line ->
                stdout.appendLine(line)
                onOutput(line)
            }
        }

        val stderrReader = Thread {
            process.errorStream.bufferedReader().forEachLine { line ->
                stderr.appendLine(line)
            }
        }

        stdoutReader.start()
        stderrReader.start()

        val completed = process.waitForCompat(timeout)

        stdoutReader.join(1000)
        stderrReader.join(1000)

        val exitCode = if (completed) process.exitValue() else {
            process.destroyForciblyCompat()
            -1
        }

        ExecutionResult(
            exitCode = exitCode,
            stdout = stdout.toString(),
            stderr = stderr.toString(),
            duration = System.currentTimeMillis() - startTime
        )
    }

    private suspend fun downloadFile(
        url: String,
        target: File,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val request = okhttp3.Request.Builder().url(url).build()
        NetworkModule.downloadClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("${response.request.url.host} HTTP ${response.code}")
            }
            val body = response.body ?: throw IOException(Strings.downloadReturnedEmpty)
            val total = body.contentLength()
            var downloaded = 0L

            target.parentFile?.mkdirs()

            body.byteStream().use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (total > 0) onProgress(downloaded.toFloat() / total)
                    }
                }
            }
        }
    }

    suspend fun reset(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _state.value = NodeEngineState.NotInitialized
            getEngineDir(context).deleteRecursively()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class NodeEngineState {
    object NotInitialized : NodeEngineState()
    data class Initializing(val step: String, val progress: Float) : NodeEngineState()
    object Ready : NodeEngineState()
    data class Error(val message: String) : NodeEngineState()
}

data class ExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val duration: Long
) {
    val isSuccess: Boolean get() = exitCode == 0
}
