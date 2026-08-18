package com.webtoapp.core.network

import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.util.concurrent.TimeUnit

object CnMirrorProbe {

    private const val TAG = "CnMirrorProbe"
    private const val CACHE_TTL_MS = 10L * 60 * 1000

    // Probe through a real GitHub release asset — the exact path large runtime
    // downloads take (proxy -> github release CDN). A HEAD on a README says
    // nothing about release throughput, which is what actually matters.
    private const val PROBE_TARGET =
        "https://github.com/git-lfs/git-lfs/releases/download/v3.7.0/git-lfs-linux-amd64-v3.7.0.tar.gz"

    // Measure sustained download speed on the first 256 KB, capped at ~3.5 s
    // per proxy; mirrors are ordered by measured bandwidth, not RTT.
    private const val PROBE_BYTES = 256L * 1024
    private const val PROBE_BUDGET_MS = 3500L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    @Volatile
    private var cachedOrder: List<String> = emptyList()

    @Volatile
    private var cachedAt: Long = 0L

    @Volatile
    private var probing: Boolean = false

    private val probeClient by lazy {
        NetworkModule.customClient {
            connectTimeout(4, TimeUnit.SECONDS)
            readTimeout(4, TimeUnit.SECONDS)
            writeTimeout(4, TimeUnit.SECONDS)
            retryOnConnectionFailure(false)
        }
    }

    fun getOrderedProxies(baseList: List<String>): List<String> {
        val now = System.currentTimeMillis()
        val fresh = cachedOrder.isNotEmpty() && (now - cachedAt) < CACHE_TTL_MS
        if (fresh) {

            val extras = baseList.filter { it !in cachedOrder }
            return cachedOrder + extras
        }
        if (!probing) {
            scope.launch { probe(baseList) }
        }
        return baseList
    }

    suspend fun probe(baseList: List<String>) {
        mutex.withLock {
            if (probing) return@withLock
            probing = true
        }
        try {
            val results = withContext(Dispatchers.IO) {
                baseList.map { proxy ->
                    async { proxy to measureProxyBandwidth(proxy) }
                }.awaitAll()
            }
            val ordered = results
                .filter { it.second > 0 }
                .sortedByDescending { it.second }
                .map { it.first }
            if (ordered.isNotEmpty()) {
                cachedOrder = ordered
                cachedAt = System.currentTimeMillis()
                AppLogger.i(TAG, "Probed proxies: $ordered (KB/s: ${results.filter { it.second > 0 }.joinToString { "${it.first.substringAfter("//").substringBefore("/")}=${it.second / 1024}" }})")
            } else {
                AppLogger.w(TAG, "All proxies failed probe; keeping previous order")
            }
        } finally {
            probing = false
        }
    }

    /** Sustained download speed in bytes/sec through the proxy; -1 when unusable. */
    private fun measureProxyBandwidth(proxy: String): Long {
        val url = "$proxy$PROBE_TARGET"
        return try {
            val req = Request.Builder()
                .url(url)
                .header("Range", "bytes=0-${PROBE_BYTES - 1}")
                .build()
            probeClient.newCall(req).execute().use { resp ->
                if (resp.code !in 200..399) return -1
                val body = resp.body ?: return -1
                body.byteStream().use { input ->
                    val buffer = ByteArray(16 * 1024)
                    var read = 0L
                    val start = System.currentTimeMillis()
                    while (read < PROBE_BYTES) {
                        val left = (PROBE_BYTES - read).toInt().coerceAtMost(buffer.size)
                        val n = input.read(buffer, 0, left)
                        if (n == -1) break
                        read += n
                        if (System.currentTimeMillis() - start > PROBE_BUDGET_MS) break
                    }
                    val elapsed = System.currentTimeMillis() - start
                    if (read <= 0L || elapsed <= 0L) -1 else read * 1000 / elapsed
                }
            }
        } catch (_: Exception) {
            -1
        }
    }
}
