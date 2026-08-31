package com.webtoapp.core.network

import android.os.Looper
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.network.GitHubMirror.MirrorChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Picks the fastest reachable GitHub route before a download starts.
 *
 * Every channel is probed concurrently with a single small ranged request and
 * ranked by time-to-first-byte. Channels slower than [ACCEPTABLE_LATENCY_MS]
 * are dropped so a download never wastes its first attempt on a mirror that is
 * technically alive but unusable.
 *
 * The result is cached for [CACHE_TTL_MS], so repeated downloads in the same
 * session pay the probe cost once instead of re-measuring every channel every
 * time. If everything fails the probe, the caller still gets the full channel
 * list in declaration order — the probe never leaves a download with nowhere
 * to go.
 */
object CnMirrorProbe {

    private const val TAG = "CnMirrorProbe"
    private const val CACHE_TTL_MS = 5L * 60 * 1000

    /** Channels at or under this TTFB are considered usable. */
    const val ACCEPTABLE_LATENCY_MS = 1000L

    // Probe through a real GitHub release asset — the exact path large runtime
    // downloads take. A HEAD on a README says nothing about release routes.
    private const val PROBE_TARGET =
        "https://github.com/git-lfs/git-lfs/releases/download/v3.7.0/git-lfs-linux-amd64-v3.7.0.tar.gz"

    // One ranged KB is enough to measure TTFB without paying for real payload.
    private const val PROBE_RANGE_BYTES = 1024
    private const val PROBE_TIMEOUT_MS = 2500L
    private const val PROBE_BUDGET_MS = 4000L // hard ceiling for a single channel

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    @Volatile
    private var cachedOrder: List<MirrorChannel> = emptyList()

    @Volatile
    private var cachedAt: Long = 0L

    private val probeClient by lazy {
        NetworkModule.customClient {
            connectTimeout(PROBE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            readTimeout(PROBE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            writeTimeout(PROBE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            retryOnConnectionFailure(false)
        }
    }

    /**
     * Channels ordered fastest-first. Safe to call from anywhere: on a worker
     * thread a stale cache triggers one blocking probe round (bounded by
     * [PROBE_BUDGET_MS]); on the main thread it never blocks and falls back to
     * the last known order while a refresh runs in the background.
     */
    fun orderedChannels(): List<MirrorChannel> {
        val now = System.currentTimeMillis()
        if (cachedOrder.isNotEmpty() && (now - cachedAt) < CACHE_TTL_MS) {
            return mergeNewChannels(cachedOrder)
        }
        if (onMainThread()) {
            scope.launch { runCatching { probe() } }
            return mergeNewChannels(cachedOrder)
        }
        runCatching {
            runBlocking { probe() }
        }
        return mergeNewChannels(cachedOrder)
    }

    /** Forget the cached order; the next [orderedChannels] re-measures. */
    fun invalidate() {
        cachedOrder = emptyList()
        cachedAt = 0L
    }

    /**
     * Measure every channel and cache the ones that answer in time. Concurrent
     * callers serialize on [mutex] and share one probe round.
     */
    suspend fun probe(): List<MirrorChannel> {
        mutex.withLock {
            val now = System.currentTimeMillis()
            if (cachedOrder.isNotEmpty() && (now - cachedAt) < CACHE_TTL_MS) {
                return cachedOrder
            }
            val channels = GitHubMirror.ALL_CHANNELS
            val started = System.currentTimeMillis()
            val results = coroutineScope {
                channels.map { channel ->
                    async { channel to measureLatency(channel) }
                }.awaitAll()
            }
            val usable = results
                .filter { (_, latency) -> latency in 1..ACCEPTABLE_LATENCY_MS }
                .sortedBy { (_, latency) -> latency }
                .map { (channel, _) -> channel }

            val report = results.joinToString(", ") { (channel, latency) ->
                "${channel.id}=${if (latency < 0) "dead" else "${latency}ms"}"
            }
            AppLogger.i(TAG, "Probe round took ${System.currentTimeMillis() - started}ms — $report")

            if (usable.isEmpty()) {
                // Nothing answered in time. Keep the declaration order so the
                // download still has every channel to fall through, and do not
                // cache the failure — the next download should try again.
                AppLogger.w(TAG, "No channel answered within ${ACCEPTABLE_LATENCY_MS}ms; using declaration order")
                return channels
            }
            cachedOrder = usable
            cachedAt = System.currentTimeMillis()
            AppLogger.i(TAG, "Usable channels: ${usable.joinToString(" > ") { it.id }}")
            return usable
        }
    }

    /**
     * Channels added to the pool after the last probe still deserve a slot;
     * append them behind the measured order rather than dropping them.
     */
    private fun mergeNewChannels(order: List<MirrorChannel>): List<MirrorChannel> {
        if (order.isEmpty()) return GitHubMirror.ALL_CHANNELS
        val extras = GitHubMirror.ALL_CHANNELS.filter { it !in order }
        return if (extras.isEmpty()) order else order + extras
    }

    private fun onMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

    /** Time-to-first-byte in ms for one channel, or -1 when unreachable. */
    private fun measureLatency(channel: MirrorChannel): Long {
        val url = channel.rewrite(PROBE_TARGET)
        return try {
            val req = Request.Builder()
                .url(url)
                .header("Range", "bytes=0-${PROBE_RANGE_BYTES - 1}")
                .build()
            val start = System.nanoTime()
            probeClient.newCall(req).execute().use { resp ->
                if (resp.code !in 200..399) return -1
                val body = resp.body ?: return -1
                body.byteStream().use { input ->
                    var read = 0
                    val buffer = ByteArray(PROBE_RANGE_BYTES)
                    while (read < PROBE_RANGE_BYTES) {
                        val n = input.read(buffer, read, PROBE_RANGE_BYTES - read)
                        if (n == -1) break
                        read += n
                    }
                    // A proxy that answers 200 with a short plain-text error
                    // ("Suspent", "404 not found") hits EOF long before the
                    // requested range. It measures as the fastest route and
                    // then poisons every consumer of the ordering, so a
                    // truncated body counts as dead, same as a timeout.
                    if (read < PROBE_RANGE_BYTES) return -1
                }
                val elapsedMs = (System.nanoTime() - start) / 1_000_000
                if (elapsedMs > PROBE_BUDGET_MS) -1 else elapsedMs
            }
        } catch (_: Exception) {
            -1
        }
    }
}
