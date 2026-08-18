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

    // Slow-source watchdog: a throttling mirror never errors — it trickles at
    // a few KB/s forever, holding the download hostage. Below this sustained
    // speed (after the grace window, past the minimum byte floor, only for
    // files big enough to matter) the call is cancelled so the caller can
    // switch to the next mirror; partial bytes stay in .tmp for resume.
    private const val SLOW_THRESHOLD_BYTES_PER_SEC = 64L * 1024
    private const val SLOW_GRACE_MS = 15_000L
    private const val SLOW_MIN_BYTES = 1L * 1024 * 1024
    private const val SLOW_MIN_TOTAL_BYTES = 2L * 1024 * 1024
    private const val SLOW_POLL_MS = 2_000L

    enum class Outcome { SUCCESS, FAILED, SLOW }

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

    // Read by the watchdog thread while the copy loop mutates it — deque keeps
    // both sides safe without locking the hot path.
    private class SpeedTracker {
        private val samples = java.util.concurrent.ConcurrentLinkedDeque<Pair<Long, Long>>()

        fun recordSample(totalDownloaded: Long) {
            val now = System.currentTimeMillis()
            samples.addLast(now to totalDownloaded)
            while (true) {
                val head = samples.peekFirst() ?: break
                if (now - head.first > SPEED_WINDOW_MS) samples.pollFirst() else break
            }
        }

        fun calculateSpeed(): Long {
            val first = samples.peekFirst() ?: return 0L
            val last = samples.peekLast() ?: return 0L
            val timeDelta = last.first - first.first
            if (timeDelta <= 0) return 0L
            val bytesDelta = last.second - first.second
            return (bytesDelta * 1000 / timeDelta).coerceAtLeast(0)
        }

        fun latestBytes(): Long = samples.peekLast()?.second ?: 0L
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

    suspend fun downloadFileEx(
        url: String,
        destFile: File,
        displayName: String,
        context: Context? = null,
        taskId: TaskId = DEFAULT_TASK,
    ): Outcome = withContext(Dispatchers.IO) {
        downloadMutex.withLock {
            val fileName = url.substringAfterLast("/")
            val tempFile = File(destFile.parentFile, "${destFile.name}.tmp")
            var downloadedBytes = 0L
            val startTime = System.currentTimeMillis()
            val speedTracker = SpeedTracker()
            val totalHint = java.util.concurrent.atomic.AtomicLong(-1L)
            val slowAbort = java.util.concurrent.atomic.AtomicBoolean(false)
            val watchdogDone = java.util.concurrent.atomic.AtomicBoolean(false)

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

                val call = httpClient.newCall(requestBuilder.build())

                val watchdog = Thread {
                    try {
                        while (!watchdogDone.get()) {
                            Thread.sleep(SLOW_POLL_MS)
                            if (watchdogDone.get() || _paused.get() || _cancelled.get()) continue
                            val total = totalHint.get()
                            if (total in 1 until SLOW_MIN_TOTAL_BYTES) continue
                            val elapsed = System.currentTimeMillis() - startTime
                            val speed = speedTracker.calculateSpeed()
                            if (elapsed > SLOW_GRACE_MS &&
                                speedTracker.latestBytes() > SLOW_MIN_BYTES &&
                                speed in 1 until SLOW_THRESHOLD_BYTES_PER_SEC
                            ) {
                                slowAbort.set(true)
                                AppLogger.w(
                                    TAG,
                                    "$displayName 源速度 ${formatSpeed(speed)} 持续低于 ${formatSpeed(SLOW_THRESHOLD_BYTES_PER_SEC)}，放弃该源 [task=$taskId]"
                                )
                                call.cancel()
                                break
                            }
                        }
                    } catch (_: InterruptedException) {
                    }
                }.apply { isDaemon = true; name = "wta-dl-watchdog"; start() }

                try {
                    val response = call.execute()

                    if (!response.isSuccessful && response.code != 206) {
                        AppLogger.e(TAG, "下载失败: HTTP ${response.code} - $url [task=$taskId]")
                        emit(taskId, State.Error(Strings.downloadFailedHttp.replace("%d", response.code.toString())))
                        response.close()
                        return@withLock Outcome.FAILED
                    }

                    val body = response.body ?: run {
                        emit(taskId, State.Error(Strings.downloadReturnedEmpty))
                        response.close()
                        return@withLock Outcome.FAILED
                    }

                    body.use { responseBody ->
                        val contentLength = responseBody.contentLength()
                        val totalBytes = if (response.code == 206) {
                            downloadedBytes + contentLength
                        } else {
                            contentLength
                        }
                        totalHint.set(totalBytes)

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
                                        if (!isActive) return@withLock Outcome.FAILED
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

                    val expected = totalHint.get()
                    if (expected > 0 && tempFile.length() != expected) {
                        AppLogger.e(TAG, "$displayName 大小不匹配: expected=$expected actual=${tempFile.length()} [task=$taskId]")
                        tempFile.delete()
                        emit(taskId, State.Error(
                            Strings.downloadNameFailed
                                .replaceFirst("%s", displayName)
                                .replaceFirst("%s", "size mismatch: expected $expected bytes, got ${tempFile.length()}")
                        ))
                        return@withLock Outcome.FAILED
                    }

                    tempFile.renameTo(destFile)
                    AppLogger.i(TAG, "$displayName 下载完成: ${destFile.length()} 字节")
                    Outcome.SUCCESS
                } finally {
                    watchdogDone.set(true)
                    watchdog.interrupt()
                }

            } catch (e: kotlinx.coroutines.CancellationException) {
                AppLogger.i(TAG, "$displayName 被取消,保留临时文件以便断点续传 [task=$taskId]")
                _states.value = _states.value - taskId
                throw e
            } catch (e: Exception) {
                if (slowAbort.get()) {
                    // Keep .tmp: bytes are valid, the next mirror resumes from here.
                    AppLogger.w(TAG, "$displayName 源过慢已中断，交由调用方切换源 [task=$taskId]")
                    Outcome.SLOW
                } else {
                    AppLogger.e(TAG, "下载 $displayName 失败 [task=$taskId]", e)
                    emit(taskId, State.Error(Strings.downloadNameFailed.replaceFirst("%s", displayName).replaceFirst("%s", e.message ?: "")))
                    Outcome.FAILED
                }
            }
        }
    }

    /**
     * Multi-source download with uniform retry / slow-switch / resume policy.
     * - FAILED: retried up to [maxRetryPerUrl] times, then .tmp is dropped and
     *   the next source starts clean.
     * - SLOW: the source is abandoned immediately but .tmp is kept, so the next
     *   mirror resumes from the stalled byte offset. On the last source a SLOW
     *   abort is retried — each resume still makes forward progress.
     */
    suspend fun downloadFileWithFallback(
        urls: List<String>,
        destFile: File,
        displayName: String,
        context: Context? = null,
        maxRetryPerUrl: Int = 2,
        retryDelayMs: Long = 2_000L,
        fetch: suspend (url: String, sourceName: String) -> Outcome = { url, sourceName ->
            downloadFileEx(url, destFile, sourceName, context)
        },
    ): Boolean {
        var lastOutcome = Outcome.FAILED
        for ((urlIndex, url) in urls.withIndex()) {
            val sourceName = if (urls.size > 1) {
                String.format(Strings.pyDownloadSourceLabel, displayName, urlIndex + 1, urls.size)
            } else displayName
            AppLogger.i(TAG, "Attempting to download $sourceName: $url")

            var attempt = 0
            while (true) {
                lastOutcome = fetch(url, sourceName)
                if (lastOutcome == Outcome.SUCCESS) return true

                val hasMoreSources = urlIndex < urls.lastIndex
                if (lastOutcome == Outcome.SLOW) {
                    if (hasMoreSources) {
                        AppLogger.i(TAG, "$sourceName 过慢，切换下一源（保留断点）")
                        break
                    }
                    if (++attempt >= maxRetryPerUrl) {
                        emit(DEFAULT_TASK, State.Error(
                            Strings.downloadNameFailed
                                .replaceFirst("%s", displayName)
                                .replaceFirst("%s", "all sources too slow")
                        ))
                        return false
                    }
                    AppLogger.i(TAG, "$sourceName 过慢，保留断点重试 ($attempt/$maxRetryPerUrl)")
                    continue
                }

                if (++attempt < maxRetryPerUrl) {
                    AppLogger.i(TAG, "$sourceName download failed, retrying in ${retryDelayMs / 1000}s ($attempt/$maxRetryPerUrl)")
                    kotlinx.coroutines.delay(retryDelayMs)
                    publishState(State.Idle)
                } else {
                    if (hasMoreSources) {
                        File(destFile.parentFile, "${destFile.name}.tmp").delete()
                        AppLogger.i(TAG, "$sourceName failed, switching to next source...")
                        publishState(State.Idle)
                    }
                    break
                }
            }
        }
        return false
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
