package com.webtoapp.core.update

import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.network.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

/**
 * States emitted by [download]. The UI renders a deterministic view per state.
 *
 * [DownloadProgress.totalBytes] is -1 when the server does not advertise Content-Length.
 */
sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState
    data class Downloading(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speedBytesPerSec: Long
    ) : UpdateDownloadState
    data object Verifying : UpdateDownloadState
    data class Done(val file: File) : UpdateDownloadState
    data class Failed(val message: String) : UpdateDownloadState
}

object ApkUpdateInstaller {

    private const val TAG = "ApkUpdateInstaller"

    /** Directory under app-private external storage that the file manager scans. */
    const val UPDATE_APK_DIR = "update_apks"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Last started download so a second "download" tap cancels / no-ops the previous. */
    @Volatile
    private var activeJob: Job? = null

    /** Resolved on first use; reused across calls. */
    private fun targetDir(context: android.content.Context): File {
        val base = context.getExternalFilesDir(null)
            ?: File(context.filesDir, "external").apply { mkdirs() }
        return File(base, UPDATE_APK_DIR).apply { mkdirs() }
    }

    /**
     * Streams the update APK into `update_apks/web-to-app-<version>.apk` via OkHttp, emitting
     * progress roughly once per second.
     *
     * [url] is the raw GitHub asset URL; the measured mirror pool expands it into candidates and
     * each is tried in turn — a mirror that stalls, errors mid-stream, or yields a file that
     * fails the SHA-256 check falls through to the next one instead of failing the download.
     * On completion the file is SHA-256 verified against [expectedSha256] (if provided); on
     * final mismatch the file is deleted and [UpdateDownloadState.Failed] is emitted.
     *
     * The APK is **not** auto-installed; the caller decides when to launch the system installer
     * (see `ApkBuilder.installApk`). This keeps the user in control and matches Android's rule
     * that apps cannot silently install packages.
     *
     * @return the [Job] backing the download; cancel it to abort.
     */
    fun download(
        context: android.content.Context,
        url: String,
        version: String,
        expectedSha256: String? = null,
        onState: (UpdateDownloadState) -> Unit
    ): Job {
        activeJob?.cancel()
        val appContext = context.applicationContext
        val job = scope.launch {
            // Fastest-first measured routes, plain GitHub URL last. If the URL
            // was already mirrored upstream this still works: non-GitHub URLs
            // expand to themselves, so behaviour degrades to the old single
            // attempt instead of breaking.
            val candidates = com.webtoapp.core.network.GitHubMirror.proxiedCn(url)
            var lastError: Exception? = null
            for (candidate in candidates) {
                try {
                    onState(UpdateDownloadState.Downloading(0L, -1L, 0L))
                    val file = streamToFile(appContext, candidate, version, onState)
                    if (expectedSha256 != null) {
                        onState(UpdateDownloadState.Verifying)
                        val actual = sha256Of(file)
                        if (actual == null || !actual.equals(expectedSha256, ignoreCase = true)) {
                            AppLogger.e(
                                TAG,
                                "APK integrity check failed via $candidate: " +
                                    "expected=$expectedSha256 actual=$actual"
                            )
                            // A mismatch here means the route served something
                            // other than the release asset; treat it like any
                            // other broken mirror and try the next route.
                            file.delete()
                            lastError = IllegalStateException("sha256-mismatch")
                            continue
                        }
                        AppLogger.i(TAG, "APK integrity verified (sha256 match)")
                    }
                    onState(UpdateDownloadState.Done(file))
                    return@launch
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Update download failed via $candidate: ${e.message}")
                    lastError = e
                }
            }
            onState(
                UpdateDownloadState.Failed(
                    lastError?.message ?: lastError?.javaClass?.simpleName ?: "all routes failed"
                )
            )
        }
        activeJob = job
        return job
    }

    fun cancel() {
        activeJob?.cancel()
        activeJob = null
    }

    private suspend fun streamToFile(
        context: android.content.Context,
        url: String,
        version: String,
        onState: (UpdateDownloadState) -> Unit
    ): File {
        val dir = targetDir(context)
        val fileName = "web-to-app-$version.apk"
        // Wipe stale copies of the same name so a partial previous download never resumes silently.
        File(dir, fileName).takeIf { it.exists() }?.delete()
        val target = File(dir, fileName)

        val response = NetworkModule.downloadClient.newCall(Request.Builder().url(url).build()).execute()
        if (!response.isSuccessful) {
            response.close()
            throw IllegalStateException("HTTP ${response.code}")
        }
        val body = response.body ?: throw IllegalStateException("empty response body")
        val total = body.contentLength()

        body.byteStream().use { input ->
            target.outputStream().buffered().use { output ->
                val buffer = ByteArray(64 * 1024)
                var downloaded = 0L
                var windowStartBytes = 0L
                var windowStartTime = System.currentTimeMillis()
                var lastEmit = 0L
                while (true) {
                    // ensureActive so cancel() propagates promptly.
                    kotlinx.coroutines.coroutineScope { ensureActive() }
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    downloaded += read

                    val now = System.currentTimeMillis()
                    val elapsed = now - windowStartTime
                    if (now - lastEmit >= 1000L || (total > 0 && downloaded == total)) {
                        val speed = if (elapsed > 0) {
                            (downloaded - windowStartBytes) * 1000L / elapsed
                        } else 0L
                        onState(UpdateDownloadState.Downloading(downloaded, total, speed))
                        lastEmit = now
                        // reset sampling window so speed reflects the recent second, not whole run.
                        windowStartBytes = downloaded
                        windowStartTime = now
                    }
                }
                output.flush()
                // final emit so the bar reaches 100% with exact totals.
                onState(UpdateDownloadState.Downloading(downloaded, total.coerceAtLeast(downloaded), 0L))
            }
        }
        return target
    }

    private fun sha256Of(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to compute sha256: ${e.message}")
            null
        }
    }
}
