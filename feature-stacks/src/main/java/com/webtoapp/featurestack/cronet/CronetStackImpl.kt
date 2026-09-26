package com.webtoapp.featurestack.cronet

import android.content.Context
import android.os.Build
import com.webtoapp.core.featurestack.api.CronetSpec
import com.webtoapp.core.featurestack.api.CronetStack
import com.webtoapp.core.featurestack.api.FeatureRuntime
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
import java.util.zip.ZipInputStream

/**
 * Cronet upstream for the TLS-MITM bridge's forced HTTP/3 mode — packaged as the
 * `cronet` feature stack so the ~1 MB Chromium Java layer can be left out of a
 * generated APK entirely.
 *
 * Byte-for-byte port of the previous in-tree CronetForwarder +
 * CronetDependencyManager, with host services reached through [FeatureRuntime].
 */
class CronetStackImpl : CronetStack {

    private lateinit var appContext: Context
    private lateinit var runtime: FeatureRuntime

    private val buildLock = Any()

    @Volatile private var engineRef: CronetEngine? = null
    private val hintedHosts: MutableSet<String> = ConcurrentHashMap.newKeySet()

    @Volatile private var echMode = false

    private val downloadKick = AtomicBoolean(false)
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val callbackExecutor = ThreadPoolExecutor(
        0, 32, 30L, TimeUnit.SECONDS,
        SynchronousQueue()
    ) { r -> Thread(r, "CronetStack-Cb").apply { isDaemon = true } }
        .also { it.allowCoreThreadTimeOut(true) }

    override fun init(context: Context, runtime: FeatureRuntime) {
        appContext = context.applicationContext
        this.runtime = runtime
    }

    override fun isReady(): Boolean =
        engineRef != null || resolveCronetLib() != null

    override fun start(echUpstream: Boolean) {
        echMode = echUpstream
        try {
            ensureEngine(host = null, port = 443)
        } catch (t: Throwable) {
            runtime.log(FeatureRuntime.LOG_WARN, TAG, "Cronet engine not available yet: ${t.message}", t)
        }
    }

    override fun stop() {
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
        }, "CronetStack-Retire").apply { isDaemon = true }.start()
    }

    /**
     * Returns the shared engine, rebuilding it with a QUIC hint for [host] when that
     * host has not been hinted yet (the rebuild keeps h3 available from the very
     * first request; the retired engine drains on a background thread).
     */
    private fun ensureEngine(host: String?, port: Int): CronetEngine? {
        val existing = engineRef
        if (existing != null && (host == null || hintedHosts.contains(host))) return existing

        synchronized(buildLock) {
            val again = engineRef
            if (again != null && (host == null || hintedHosts.contains(host))) return again
            if (host != null && hintedHosts.size >= MAX_HINTED_HOSTS) return again

            try {
                val newEngine = buildEngine(if (host != null) hintedHosts + host else hintedHosts, port)
                if (host != null) hintedHosts.add(host)
                if (again != null) retireAsync(again)
                engineRef = newEngine
                runtime.log(
                    FeatureRuntime.LOG_INFO, TAG,
                    "Cronet engine ready (version=${newEngine.versionString}, hinted=${hintedHosts.size}, ech=$echMode)",
                    null
                )
                return newEngine
            } catch (t: Throwable) {
                runtime.log(FeatureRuntime.LOG_ERROR, TAG, "Failed to build Cronet engine", t)
                kickBackgroundDownload()
                return again
            }
        }
    }

    private fun buildEngine(hosts: Set<String>, port: Int): CronetEngine {
        val depsDir = getLibDir()
        val builder = CronetEngine.Builder(appContext)
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
                    // Host preview: the .so is downloaded on demand.
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

    private fun kickBackgroundDownload() {
        if (!downloadKick.compareAndSet(false, true)) return
        downloadScope.launch {
            try {
                val ok = downloadCronetRuntime()
                runtime.log(
                    FeatureRuntime.LOG_INFO, TAG,
                    "Cronet background download: ${if (ok) "ok" else "failed"}", null
                )
            } catch (t: Throwable) {
                runtime.log(FeatureRuntime.LOG_ERROR, TAG, "Cronet background download error", t)
            }
        }
    }

    override fun forward(
        method: String,
        url: String,
        headers: List<Pair<String, String>>,
        body: ByteArray?,
        sink: OutputStream,
        clientWantsClose: Boolean,
        hostForHint: String,
        portForHint: Int
    ): Boolean {
        val engine = ensureEngine(hostForHint, portForHint)
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
                    // Proxy semantics: hand the 3xx to the client instead of following it.
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
                runtime.log(
                    FeatureRuntime.LOG_DEBUG, TAG,
                    "h3 forward done: ${info.httpStatusCode} proto=${info.negotiatedProtocol} url=$url",
                    null
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
                throw IOException("Cronet forward failed mid-response: ${error.message}", error)
            }
            runtime.log(FeatureRuntime.LOG_WARN, TAG, "Cronet forward failed for $url: ${error.message}", error)
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

    // ---- Native library resolution / download (ported from CronetDependencyManager) ----

    private fun getDepsDir(): File =
        File(appContext.filesDir, "cronet_deps").also { it.mkdirs() }

    private fun getDeviceAbi(): String = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"

    private fun getLibDir(): File =
        File(getDepsDir(), getDeviceAbi()).also { it.mkdirs() }

    private fun getLibFile(): File = File(getLibDir(), LIB_FILE_NAME)

    /**
     * The app's own nativeLibraryDir first (generated APKs get the lib injected),
     * then the downloaded copy (host preview). Null when neither is available.
     */
    private fun resolveCronetLib(): File? {
        val native = File(appContext.applicationInfo.nativeLibraryDir, LIB_FILE_NAME)
        if (native.isFile && native.length() > 0L) return native
        val downloaded = getLibFile()
        if (downloaded.isFile && downloaded.length() > 0L) return downloaded
        return null
    }

    private fun downloadCronetRuntime(): Boolean {
        if (resolveCronetLib() != null) return true
        val archive = File(appContext.cacheDir, "cronet_temp.aar")
        val downloaded = runtime.downloadFile(
            appContext,
            CronetSpec.downloadUrls,
            archive.absolutePath,
            CronetSpec.AAR_SHA256,
            "Cronet ${CronetSpec.ARTIFACT_VERSION}",
            MAX_RETRY_PER_URL,
            RETRY_DELAY_MS
        )
        if (!downloaded) return false
        try {
            extractCronetLib(archive)
            return true
        } finally {
            try { archive.delete() } catch (_: Exception) {}
        }
    }

    private fun extractCronetLib(archive: File) {
        val abi = getDeviceAbi()
        val wanted = "jni/$abi/$LIB_FILE_NAME"
        val outFile = getLibFile()
        outFile.parentFile?.mkdirs()

        // Drop stale libs from other Cronet releases so the deps dir stays bounded.
        getDepsDir().listFiles()?.forEach { abiDir ->
            if (abiDir.isDirectory) {
                abiDir.listFiles()?.forEach { f ->
                    if (f.isFile && f.name.startsWith("libcronet.") && f.name != LIB_FILE_NAME) {
                        try { f.delete() } catch (_: Exception) {}
                    }
                }
            }
        }

        var found = false
        ZipInputStream(archive.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            var entries = 0
            while (entry != null && entries < 512) {
                entries++
                if (!entry.isDirectory && entry.name == wanted) {
                    val tmp = File(outFile.parentFile, "$LIB_FILE_NAME.tmp")
                    tmp.outputStream().buffered().use { fos ->
                        val buf = ByteArray(64 * 1024)
                        var n = zis.read(buf)
                        var total = 0L
                        while (n >= 0) {
                            fos.write(buf, 0, n)
                            total += n
                            if (total > 64L * 1024 * 1024) throw IOException("Cronet lib too large")
                            n = zis.read(buf)
                        }
                    }
                    if (tmp.length() <= 0L) {
                        tmp.delete()
                        throw IllegalStateException("Empty Cronet library entry: ${entry.name}")
                    }
                    if (outFile.exists()) outFile.delete()
                    if (!tmp.renameTo(outFile)) {
                        tmp.copyTo(outFile, overwrite = true)
                        tmp.delete()
                    }
                    found = true
                    runtime.log(
                        FeatureRuntime.LOG_INFO, TAG,
                        "Extracted ${entry.name} -> ${outFile.absolutePath} (${outFile.length()} bytes)", null
                    )
                }
                entry = zis.nextEntry
            }
        }
        if (!found) {
            throw IllegalStateException("Cronet library not found in AAR (ABI: $abi)")
        }
    }

    private companion object {
        const val TAG = "CronetStack"

        // Artifact constants live in CronetSpec (featurestack api) so the main-side
        // injector and this dex impl cannot drift apart.
        const val LIB_FILE_NAME = CronetSpec.LIB_FILE_NAME

        const val MAX_RETRY_PER_URL = 2
        const val RETRY_DELAY_MS = 2000L

        const val MAX_HINTED_HOSTS = 24
        const val READ_BUFFER_BYTES = 32 * 1024

        val CRLF = "\r\n".toByteArray(Charsets.ISO_8859_1)

        val HOP_BY_HOP_RESPONSE_HEADERS = setOf(
            "content-length", "transfer-encoding", "connection", "keep-alive",
            "proxy-authenticate", "proxy-authorization", "upgrade",
            "content-encoding"
        )
    }
}
