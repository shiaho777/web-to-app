package com.webtoapp.core.webview

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.DnsOptions
import org.chromium.net.UploadDataProviders
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Upstream HTTP engine for forced HTTP/3 (issue #721).
 *
 * The TLS-MITM bridge hands every request here once the 强制 HTTP/3 toggle is on:
 * requests are re-issued through Chromium's own network stack (Cronet) with a QUIC
 * hint for the target host, so the very first request races QUIC against TCP instead
 * of waiting for an Alt-Svc advertisement. Sites without HTTP/3 fall back to Cronet's
 * genuine Chrome TLS/TCP transport — the outbound fingerprint IS real Chromium, which
 * is why this lives next to the fingerprint-spoofing feature instead of conflicting
 * with it.
 *
 * Cronet is a full HTTP client, not a byte relay, so both sides of the bridge speak
 * plain HTTP/1.1 while the upstream leg speaks whatever Chromium negotiates (h3
 * preferred, h2 fallback) — the WebView sees an ordinary proxy and never notices.
 */
object CronetForwarder {

    private const val TAG = "CronetForwarder"

    /**
     * QUIC hints are engine-build-time only; beyond this many distinct hosts the
     * engine stops being rebuilt and newer hosts rely on Alt-Svc discovery (their
     * first request goes out on Chrome's TCP stack, subsequent ones on h3).
     */
    private const val MAX_HINTED_HOSTS = 24

    private const val READ_BUFFER_BYTES = 32 * 1024

    private val CRLF = "\r\n".toByteArray(Charsets.ISO_8859_1)

    private val buildLock = Any()

    @Volatile private var engineRef: CronetEngine? = null
    private val hintedHosts: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /**
     * ECH mode: builds engines with the built-in (Chromium async) resolver so HTTPS
     * records are queried and ECH applied on the upstream handshakes. Fixed per
     * bridge session; the bridge restarts (rebuilding the engine) when it changes.
     */
    @Volatile private var echMode = false

    /** One-shot guard for the background self-heal download when the native lib is missing. */
    private val downloadKick = AtomicBoolean(false)
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Callbacks run on these daemon threads; they write response bytes to the client sink. */
    private val callbackExecutor = ThreadPoolExecutor(
        0, 32, 30L, TimeUnit.SECONDS,
        SynchronousQueue()
    ) { r -> Thread(r, "CronetForwarder-Cb").apply { isDaemon = true } }
        .also { it.allowCoreThreadTimeOut(true) }

    /**
     * Response headers that must not be relayed verbatim to the HTTP/1.1 client:
     * hop-by-hop headers, plus the body-framing ones (the body is re-framed as
     * chunked) and Content-Encoding — Chromium transparently decodes the body but
     * still reports the original encoding header, so relaying it would make the
     * WebView try to gunzip already-decoded bytes (ERR_CONTENT_DECODING_FAILED).
     */
    private val HOP_BY_HOP_RESPONSE_HEADERS = setOf(
        "content-length", "transfer-encoding", "connection", "keep-alive",
        "proxy-authenticate", "proxy-authorization", "upgrade",
        "content-encoding"
    )

    /**
     * Cheap gate for the bridge: an engine is already live, or the native library is
     * available (embedded in generated APKs / downloaded for host preview) so one could
     * be built. Until then the bridge stays on the classic raw relay.
     */
    fun isReady(context: Context): Boolean =
        engineRef != null || CronetDependencyManager.isCronetReady(context)

    /** Called by the bridge when the Cronet upstream activates; warms the engine and,
     *  when the native library is missing, kicks a one-shot background download so the
     *  app self-heals. */
    fun start(appContext: Context, echUpstream: Boolean = false) {
        echMode = echUpstream
        try {
            ensureEngine(appContext, host = null, port = 443)
        } catch (t: Throwable) {
            AppLogger.w(TAG, "Cronet engine not available yet: ${t.message}")
        }
    }

    fun stop() {
        synchronized(buildLock) {
            val engine = engineRef
            engineRef = null
            echMode = false
            hintedHosts.clear()
            if (engine != null) {
                retireAsync(engine)
            }
        }
    }

    /** shutdown() blocks until in-flight requests drain; keep it off the caller's thread. */
    private fun retireAsync(engine: CronetEngine) {
        Thread({
            try { engine.shutdown() } catch (_: Throwable) {}
        }, "CronetForwarder-Retire").apply { isDaemon = true }.start()
    }

    /**
     * Returns the shared engine, rebuilding it with a QUIC hint for [host] when that
     * host has not been hinted yet (the rebuild keeps h3 available from the very
     * first request; the retired engine drains on a background thread).
     */
    private fun ensureEngine(context: Context, host: String?, port: Int): CronetEngine? {
        val existing = engineRef
        if (existing != null && (host == null || hintedHosts.contains(host))) return existing

        synchronized(buildLock) {
            val again = engineRef
            if (again != null && (host == null || hintedHosts.contains(host))) return again
            if (host != null && hintedHosts.size >= MAX_HINTED_HOSTS) return again

            try {
                val newEngine = buildEngine(context, if (host != null) hintedHosts + host else hintedHosts, port)
                if (host != null) hintedHosts.add(host)
                if (again != null) retireAsync(again)
                engineRef = newEngine
                AppLogger.i(TAG, "Cronet engine ready (version=${newEngine.versionString}, hinted=${hintedHosts.size}, ech=$echMode)")
                return newEngine
            } catch (t: Throwable) {
                AppLogger.e(TAG, "Failed to build Cronet engine", t)
                kickBackgroundDownload(context)
                return again
            }
        }
    }

    private fun buildEngine(context: Context, hosts: Set<String>, port: Int): CronetEngine {
        val depsDir = CronetDependencyManager.getLibDir(context)
        val builder = CronetEngine.Builder(context)
            .enableQuic(true)
            .enableHttp2(true)
            .enableHttpCache(CronetEngine.Builder.HTTP_CACHE_DISABLED, 0)
            .setLibraryLoader(object : CronetEngine.Builder.LibraryLoader() {
                override fun loadLibrary(libName: String) {
                    try {
                        // Generated APKs (and any host that bundles it): nativeLibraryDir.
                        System.loadLibrary(libName)
                        return
                    } catch (_: Throwable) {
                    }
                    // Host preview: the .so is downloaded on demand (packaging excludes it).
                    val downloaded = File(depsDir, "lib$libName.so")
                    if (downloaded.isFile && downloaded.length() > 0L) {
                        System.load(downloaded.absolutePath)
                        return
                    }
                    throw UnsatisfiedLinkError("Cronet native library unavailable: $libName")
                }
            })
        val hintPort = if (port in 1..65535) port else 443
        hosts.forEach { builder.addQuicHint(it, hintPort, hintPort) }
        if (echMode) {
            // The built-in resolver is what queries HTTPS (type 65) records; without it
            // Cronet falls back to getaddrinfo and ECH never sees an ECHConfig to use.
            builder.setDnsOptions(
                DnsOptions.builder().useBuiltInDnsResolver(true).build()
            )
        }
        return builder.build()
    }

    private fun kickBackgroundDownload(context: Context) {
        if (!downloadKick.compareAndSet(false, true)) return
        downloadScope.launch {
            try {
                val ok = CronetDependencyManager.downloadCronetRuntime(context)
                AppLogger.i(TAG, "Cronet background download: ${if (ok) "ok" else "failed"}")
            } catch (t: Throwable) {
                AppLogger.e(TAG, "Cronet background download error", t)
            }
        }
    }

    /**
     * Forwards one HTTP/1.1 request through Cronet and writes the serialized HTTP/1.1
     * response (chunked body framing) to [sink]. Blocks the caller until the response
     * completes.
     *
     * @param headers request headers in their original wire order, hop-by-hop headers
     *   already filtered by the caller (authority, body framing, and proxy headers
     *   are all re-derived by Cronet)
     * @param clientWantsClose the client sent `Connection: close`; the response head
     *   says so accordingly
     * @return true when a complete response was written; false when the exchange failed
     *   before anything was written (caller synthesizes a 502)
     * @throws IOException the client side broke mid-response (a partial chunked stream
     *   cannot be completed, so the connection must be closed)
     */
    fun forward(
        context: Context,
        method: String,
        url: String,
        headers: List<Pair<String, String>>,
        body: ByteArray?,
        sink: OutputStream,
        clientWantsClose: Boolean,
        hostForHint: String,
        portForHint: Int
    ): Boolean {
        val engine = ensureEngine(context, hostForHint, portForHint)
            ?: return false

        val latch = CountDownLatch(1)
        val headWritten = AtomicBoolean(false)
        val failure = AtomicReference<Throwable?>()
        val scratch = ByteArray(READ_BUFFER_BYTES)

        val callback = object : UrlRequest.Callback() {
            var status: Int = 0

            /** 204/205/304 and HEAD responses carry no body and thus no chunked framing. */
            val bodyPermitted: Boolean
                get() = method != "HEAD" &&
                    status != 204 && status != 205 && status != 304 &&
                    status >= 200

            override fun onRedirectReceived(request: UrlRequest, info: UrlResponseInfo, newLocation: String) {
                try {
                    // Proxy semantics: hand the 3xx to the client instead of following it —
                    // the WebView owns navigation, history, and origin decisions. The
                    // redirect response carries an empty chunked body so HTTP/1.1 framing
                    // stays valid on the keep-alive connection.
                    status = info.httpStatusCode
                    writeHead(sink, info, clientWantsClose, chunked = method != "HEAD")
                    if (method != "HEAD") {
                        sink.write("0\r\n\r\n".toByteArray(Charsets.ISO_8859_1))
                    }
                    sink.flush()
                    headWritten.set(true)
                    request.cancel()
                } catch (t: Throwable) {
                    failure.compareAndSet(null, t)
                    request.cancel()
                }
            }

            override fun onResponseStarted(request: UrlRequest, info: UrlResponseInfo) {
                status = info.httpStatusCode
                try {
                    writeHead(sink, info, clientWantsClose, chunked = bodyPermitted)
                    headWritten.set(true)
                    if (bodyPermitted) {
                        request.read(ByteBuffer.allocateDirect(READ_BUFFER_BYTES))
                    }
                    // HEAD / 204 / 304 / early responses complete via onSucceeded.
                } catch (t: Throwable) {
                    failure.compareAndSet(null, t)
                    request.cancel()
                }
            }

            override fun onReadCompleted(request: UrlRequest, info: UrlResponseInfo, byteBuffer: ByteBuffer) {
                try {
                    byteBuffer.flip()
                    if (byteBuffer.hasRemaining()) {
                        val length = byteBuffer.remaining()
                        val chunkHead = Integer.toHexString(length).toByteArray(Charsets.ISO_8859_1)
                        sink.write(chunkHead)
                        sink.write(CRLF)
                        byteBuffer.get(scratch, 0, length)
                        sink.write(scratch, 0, length)
                        sink.write(CRLF)
                    }
                    byteBuffer.clear()
                    request.read(byteBuffer)
                } catch (t: Throwable) {
                    failure.compareAndSet(null, t)
                    request.cancel()
                }
            }

            override fun onSucceeded(request: UrlRequest, info: UrlResponseInfo) {
                try {
                    if (bodyPermitted) {
                        sink.write("0\r\n\r\n".toByteArray(Charsets.ISO_8859_1))
                    }
                    sink.flush()
                } catch (t: Throwable) {
                    failure.compareAndSet(null, t)
                }
                AppLogger.d(
                    TAG,
                    "h3 forward done: ${info.httpStatusCode} proto=${info.negotiatedProtocol} url=$url"
                )
                latch.countDown()
            }

            override fun onFailed(request: UrlRequest, info: UrlResponseInfo?, error: CronetException) {
                failure.compareAndSet(null, error)
                latch.countDown()
            }

            override fun onCanceled(request: UrlRequest, info: UrlResponseInfo?) {
                latch.countDown()
            }
        }

        val requestBuilder = engine.newUrlRequestBuilder(url, callback, callbackExecutor)
            .setHttpMethod(method)
            .disableCache()
        headers.forEach { (name, value) -> requestBuilder.addHeader(name, value) }
        if (body != null && body.isNotEmpty()) {
            requestBuilder.setUploadDataProvider(UploadDataProviders.create(body), callbackExecutor)
        }

        val request = requestBuilder.build()
        request.start()

        latch.await()

        val error = failure.get()
        if (error != null) {
            if (headWritten.get()) {
                // Body cut mid-stream — the connection is unusable for keep-alive.
                throw IOException("Cronet forward failed mid-response: ${error.message}", error)
            }
            AppLogger.w(TAG, "Cronet forward failed for $url: ${error.message}")
            return false
        }
        return headWritten.get()
    }

    private fun writeHead(
        sink: OutputStream,
        info: UrlResponseInfo,
        clientWantsClose: Boolean,
        chunked: Boolean
    ) {
        val sb = StringBuilder()
        sb.append("HTTP/1.1 ").append(info.httpStatusCode)
            .append(' ').append(info.httpStatusText.ifEmpty { "Unknown" })
            .append("\r\n")
        info.allHeadersAsList.forEach { (name, value) ->
            val lower = name.lowercase(Locale.ROOT)
            if (lower in HOP_BY_HOP_RESPONSE_HEADERS) return@forEach
            sb.append(name).append(": ").append(value).append("\r\n")
        }
        if (chunked) {
            sb.append("Transfer-Encoding: chunked\r\n")
        }
        sb.append("Connection: ").append(if (clientWantsClose) "close" else "keep-alive").append("\r\n")
        sb.append("\r\n")
        sink.write(sb.toString().toByteArray(Charsets.ISO_8859_1))
    }
}
