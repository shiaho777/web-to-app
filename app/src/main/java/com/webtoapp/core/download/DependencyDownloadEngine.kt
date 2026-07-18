package com.webtoapp.core.download

import android.content.Context
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import com.webtoapp.core.network.NetworkModule
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias TaskId = String

object DependencyDownloadEngine {

    private const val TAG = "DependencyDownloadEngine"

    private const val SPEED_WINDOW_MS = 3000L

    private const val THROTTLE_MS = 500L

    private const val PAUSE_CHECK_MS = 200L

    val DEFAULT_TASK: TaskId = "__default__"

    sealed class State {
        object Idle : State()

        data class Downloading(

            val url: String,

            val displayName: String,

            val fileName: String,

            val bytesDownloaded: Long,

            val totalBytes: Long,

            val progress: Float,

            val speedBytesPerSec: Long,

            val etaSeconds: Long,

            val startTimeMillis: Long,

            val isPaused: Boolean
        ) : State()

        data class Extracting(val displayName: String) : State()
        data class Verifying(val displayName: String) : State()
        object Complete : State()
        data class Error(val message: String, val retryable: Boolean = true) : State()

        data class Paused(
            val url: String,
            val displayName: String,
            val fileName: String,
            val bytesDownloaded: Long,
            val totalBytes: Long,
            val progress: Float,
            val startTimeMillis: Long
        ) : State()
    }

    private val _states = MutableStateFlow<Map<TaskId, State>>(emptyMap())
    val states: StateFlow<Map<TaskId, State>> = _states.asStateFlow()

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state

    fun stateFor(taskId: TaskId): State = _states.value[taskId] ?: State.Idle

    fun publishState(newState: State, taskId: TaskId = DEFAULT_TASK) {
        emit(taskId, newState)
    }

    private fun emit(taskId: TaskId, newState: State) {
        _states.value = _states.value + (taskId to newState)
        if (taskId == DEFAULT_TASK) {
            _state.value = newState
        }
    }

    private val _paused = AtomicBoolean(false)
    private val downloadMutex = Mutex()

    val isActive: Boolean get() = _state.value is State.Downloading || _state.value is State.Paused

    private const val USER_AGENT = "WebToApp/1.0 (Android; DependencyDownloadEngine)"

    private val httpClient: OkHttpClient by lazy {
        NetworkModule.downloadClient.newBuilder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT)
                        .build()
                )
            }
            .build()
    }

    private class SpeedTracker {
        private val speedSamples = mutableListOf<Pair<Long, Long>>()

        fun recordSample(totalDownloaded: Long) {
            val now = System.currentTimeMillis()
            speedSamples.add(now to totalDownloaded)
            while (speedSamples.isNotEmpty() && now - speedSamples.first().first > SPEED_WINDOW_MS) {
                speedSamples.removeAt(0)
            }
        }

        fun calculateSpeed(): Long {
            if (speedSamples.size < 2) return 0L
            val oldest = speedSamples.first()
            val newest = speedSamples.last()
            val timeDelta = newest.first - oldest.first
            if (timeDelta <= 0) return 0L
            val bytesDelta = newest.second - oldest.second
            return (bytesDelta * 1000 / timeDelta).coerceAtLeast(0)
        }
    }

    private fun calculateEta(remaining: Long, speed: Long): Long {
        if (speed <= 0 || remaining <= 0) return -1
        return remaining / speed
    }

    fun pause(taskId: TaskId = DEFAULT_TASK) {
        val dl = stateFor(taskId) as? State.Downloading ?: return
        _paused.set(true)
        emit(taskId, State.Paused(
            url = dl.url,
            displayName = dl.displayName,
            fileName = dl.fileName,
            bytesDownloaded = dl.bytesDownloaded,
            totalBytes = dl.totalBytes,
            progress = dl.progress,
            startTimeMillis = dl.startTimeMillis
        ))
        AppLogger.i(TAG, "下载已暂停 [task=$taskId]: ${dl.displayName}")
    }

    fun resume(taskId: TaskId = DEFAULT_TASK) {
        _paused.set(false)
        AppLogger.i(TAG, "下载已继续 [task=$taskId]")
    }

    fun reset(taskId: TaskId = DEFAULT_TASK) {
        _paused.set(false)
        emit(taskId, State.Idle)
    }

    private val _cancelled = AtomicBoolean(false)

    fun cancel(taskId: TaskId = DEFAULT_TASK) {
        _cancelled.set(true)
        _paused.set(false)
        emit(taskId, State.Idle)
        _states.value = _states.value - taskId
        AppLogger.i(TAG, "下载已取消 [task=$taskId]")
    }

    suspend fun downloadFile(
        url: String,
        destFile: File,
        displayName: String,
        context: Context? = null,
        taskId: TaskId = DEFAULT_TASK,
    ): Boolean = withContext(Dispatchers.IO) {
        downloadMutex.withLock {
            val fileName = url.substringAfterLast("/")
            val tempFile = File(destFile.parentFile, "${destFile.name}.tmp")
            var downloadedBytes = 0L
            val startTime = System.currentTimeMillis()
            val speedTracker = SpeedTracker()

            _paused.set(false)
            _cancelled.set(false)

            try {

                if (tempFile.exists()) {
                    downloadedBytes = tempFile.length()
                }

                val requestBuilder = Request.Builder().url(url)
                if (downloadedBytes > 0) {
                    requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
                    AppLogger.i(TAG, "断点续传: 从 $downloadedBytes 字节继续 ($displayName)")
                }

                val response = httpClient.newCall(requestBuilder.build()).execute()

                if (!response.isSuccessful && response.code != 206) {
                    AppLogger.e(TAG, "下载失败: HTTP ${response.code} - $url [task=$taskId]")
                    emit(taskId, State.Error(Strings.downloadFailedHttp.replace("%d", response.code.toString())))
                    response.close()
                    return@withLock false
                }

                val body = response.body ?: run {
                    emit(taskId, State.Error(Strings.downloadReturnedEmpty))
                    response.close()
                    return@withLock false
                }

                body.use { responseBody ->
                    val contentLength = responseBody.contentLength()
                    val totalBytes = if (response.code == 206) {
                        downloadedBytes + contentLength
                    } else {
                        contentLength
                    }

                    val outputStream = if (response.code == 206) {
                        FileOutputStream(tempFile, true)
                    } else {
                        downloadedBytes = 0
                        FileOutputStream(tempFile)
                    }

                    var lastThrottleTime = 0L

                    outputStream.use { fos ->
                        val buffer = ByteArray(8192)
                        responseBody.byteStream().use { inputStream ->
                            var bytesRead: Int

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {

                                while (_paused.get()) {
                                    if (_cancelled.get()) {
                                        throw kotlinx.coroutines.CancellationException("cancelled by user [task=$taskId]")
                                    }
                                    delay(PAUSE_CHECK_MS)
                                    if (!isActive) return@withLock false
                                }
                                if (_cancelled.get()) {
                                    throw kotlinx.coroutines.CancellationException("cancelled by user [task=$taskId]")
                                }

                                fos.write(buffer, 0, bytesRead)
                                downloadedBytes += bytesRead

                                speedTracker.recordSample(downloadedBytes)

                                val now = System.currentTimeMillis()
                                if (now - lastThrottleTime >= THROTTLE_MS) {
                                    lastThrottleTime = now

                                    val speed = speedTracker.calculateSpeed()
                                    val remaining = if (totalBytes > 0) totalBytes - downloadedBytes else -1
                                    val eta = calculateEta(remaining, speed)
                                    val progress = if (totalBytes > 0) {
                                        (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
                                    } else 0f

                                    emit(taskId, State.Downloading(
                                        url = url,
                                        displayName = displayName,
                                        fileName = fileName,
                                        bytesDownloaded = downloadedBytes,
                                        totalBytes = totalBytes,
                                        progress = progress,
                                        speedBytesPerSec = speed,
                                        etaSeconds = eta,
                                        startTimeMillis = startTime,
                                        isPaused = false
                                    ))
                                }
                            }
                        }
                    }
                }

                tempFile.renameTo(destFile)
                AppLogger.i(TAG, "$displayName 下载完成: ${destFile.length()} 字节")
                true

            } catch (e: kotlinx.coroutines.CancellationException) {
                AppLogger.i(TAG, "$displayName 被取消,保留临时文件以便断点续传 [task=$taskId]")
                _states.value = _states.value - taskId
                throw e
            } catch (e: Exception) {
                AppLogger.e(TAG, "下载 $displayName 失败 [task=$taskId]", e)
                emit(taskId, State.Error(Strings.downloadNameFailed.replaceFirst("%s", displayName).replaceFirst("%s", e.message ?: "")))
                false
            }
        }
    }

    fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec < 1024 -> "$bytesPerSec B/s"
            bytesPerSec < 1024 * 1024 -> "${bytesPerSec / 1024} KB/s"
            else -> String.format(java.util.Locale.getDefault(), "%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
        }
    }

    fun formatSize(bytes: Long): String {
        return when {
            bytes < 0 -> Strings.sizeUnknown
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024L * 1024 * 1024 -> String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format(java.util.Locale.getDefault(), "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    fun formatEta(seconds: Long): String {
        if (seconds < 0) return "--:--"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format(java.util.Locale.getDefault(), "%d:%02d:%02d", h, m, s)
        } else {
            String.format(java.util.Locale.getDefault(), "%d:%02d", m, s)
        }
    }

    fun formatTime(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }
}
