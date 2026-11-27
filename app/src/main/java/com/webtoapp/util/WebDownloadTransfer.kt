package com.webtoapp.util

import com.webtoapp.core.webview.WebDownloadReferrer
import okhttp3.Call
import okhttp3.ConnectionPool
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Protocol
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ProtocolException
import java.net.Proxy
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.ZipException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

internal class WebDownloadTransfer(
    private val client: OkHttpClient = defaultClient,
    private val cookieFor: (HttpUrl) -> String? = { null },
    private val statusError: (Int) -> String = { "HTTP $it" },
    private val transferError: String = "Download failed",
    private val log: (String) -> Unit = {},
    private val policy: Policy = Policy(),
    private val nanoTime: () -> Long = System::nanoTime,
    private val pause: (Long) -> Unit = Thread::sleep
) {
    internal data class Policy(
        val firstWindowMs: Long = 1_500,
        val slowWindowMs: Long = 10_000,
        val slowBytesPerSecond: Long = 512 * 1024,
        val minRemaining: Long = 8 * 1024 * 1024,
        val firstPartBytes: Int = 64 * 1024,
        val partBytes: Int = 512 * 1024,
        val rangeStartupMs: Long = 7_000
    )

    private data class Representation(val size: Long, val mime: String, val tag: String?, val identity: Boolean)

    internal class Attempt(val call: Call, val response: Response, val referrer: String?) : AutoCloseable {
        val url get() = response.request.url
        val body get() = checkNotNull(response.body)

        override fun close() {
            call.cancel()
            response.close()
        }
    }

    private class Timing(private val nanoTime: () -> Long) : EventListener() {
        private var dnsStart = 0L
        private var connectStart = 0L
        private var tlsStart = 0L
        private var dnsNs = 0L
        private var connectNs = 0L
        private var tlsNs = 0L

        override fun dnsStart(call: Call, domainName: String) { dnsStart = nanoTime() }
        override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) { dnsNs += nanoTime() - dnsStart }
        override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) { connectStart = nanoTime() }
        override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {
            connectNs += nanoTime() - connectStart
        }
        override fun secureConnectStart(call: Call) { tlsStart = nanoTime() }
        override fun secureConnectEnd(call: Call, handshake: Handshake?) { tlsNs += nanoTime() - tlsStart }

        fun summary() = "dnsMs=${millis(dnsNs)} connectMs=${millis(connectNs)} tlsMs=${millis(tlsNs)}"
    }

    private class InvalidRepresentation(message: String) : IOException(message)

    fun open(rawUrl: String, userAgent: String, referrer: String?): Body {
        val url = rawUrl.toHttpUrlOrNull()?.takeIf(::safeUrl) ?: throw IOException(transferError)
        var retries = 0
        while (true) {
            try {
                return Body(url, userAgent, referrer, openFull(url, userAgent, referrer, retries != 0), retries)
            } catch (error: IOException) {
                if (!canRecover(error) || retries == MAX_RECOVERIES) throw error
                retries++
                pause(1_000L * retries)
            }
        }
    }

    private fun openFull(url: HttpUrl, userAgent: String, initialReferrer: String?, fresh: Boolean): Attempt {
        val transport = if (fresh) freshClient() else client
        var target = url
        var referrer = initialReferrer
        repeat(10) {
            val attempt = execute(transport.newCall(request(target, userAgent, referrer).build()), referrer)
            if (attempt.response.isSuccessful && attempt.response.code != 206) return attempt
            attempt.use {
                val status = it.response.code
                if (status !in REDIRECTS) throw IOException(statusError(status))
                val next = it.response.header("Location")?.let(target::resolve)?.takeIf(::safeUrl)
                    ?: throw IOException(transferError)
                target = next
                referrer = WebDownloadReferrer.fromPage(target.toString(), referrer)
            }
        }
        throw IOException(transferError)
    }

    private fun request(url: HttpUrl, userAgent: String, referrer: String?): Request.Builder =
        Request.Builder().url(url).get().tag(Timing::class.java, Timing(nanoTime)).header("Accept-Encoding", "identity").apply {
            if (userAgent.isNotBlank()) header("User-Agent", userAgent)
            cookieFor(url)?.takeIf(String::isNotBlank)?.let { header("Cookie", it) }
            referrer?.let { header("Referer", it) }
        }

    private fun freshClient(): OkHttpClient = client.newBuilder()
        .connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
        .build()

    private fun execute(call: Call, referrer: String?, trace: Boolean = true): Attempt {
        val start = nanoTime()
        val timing = call.request().tag(Timing::class.java)
        val response = try {
            call.execute()
        } catch (error: IOException) {
            if (trace) log("headers host=${call.request().url.host} failed=${error.javaClass.simpleName} ms=${millis(nanoTime() - start)} ${timing?.summary().orEmpty()}")
            throw error
        }
        if (trace) log("headers host=${response.request.url.host} protocol=${response.protocol} status=${response.code} ms=${millis(nanoTime() - start)} ${timing?.summary().orEmpty()}")
        return Attempt(call, response, referrer)
    }

    private fun representation(attempt: Attempt): Representation {
        val type = attempt.response.header("Content-Type").orEmpty().substringBefore(';').trim()
        val encoding = attempt.response.header("Content-Encoding")
        val identity = encoding == null || encoding.equals("identity", ignoreCase = true)
        return Representation(
            size = if (identity) attempt.body.contentLength() else -1,
            mime = type,
            tag = attempt.response.headers("ETag").singleOrNull()?.takeIf(STRONG_TAG::matches),
            identity = identity
        )
    }

    private fun stream(attempt: Attempt): InputStream {
        val input = attempt.body.byteStream()
        return if (attempt.response.header("Content-Encoding").equals("gzip", ignoreCase = true)) {
            GZIPInputStream(input)
        } else input
    }

    internal inner class Body internal constructor(
        private val originalUrl: HttpUrl,
        private val userAgent: String,
        private val originalReferrer: String?,
        initial: Attempt,
        private var recoveries: Int
    ) : InputStream() {
        private val original = representation(initial)
        private val oneByte = ByteArray(1)
        private var active = initial
        private var source = try {
            stream(initial)
        } catch (error: IOException) {
            initial.close()
            throw error
        }
        private var parallel = false
        private var delivered = 0L
        private var windowStart = nanoTime()
        private var windowBytes = 0L
        private var windowInputNanos = 0L
        private var firstRead = false
        private var triedParallel = false
        private var restartedParallel = false
        private var firstWindow = true
        private var closed = false
        private var inputNanos = 0L
        private var consumerNanos = 0L
        private var lastReadEnd = windowStart
        private var lastSample = windowStart
        private var lastSampleBytes = 0L

        val mime: String get() = original.mime
        val totalBytes: Long get() = original.size

        override fun read(): Int = if (read(oneByte, 0, 1) < 0) -1 else oneByte[0].toInt() and 255

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (offset < 0 || length < 0 || offset > buffer.size - length) throw IndexOutOfBoundsException()
            if (length == 0) return 0
            if (closed) throw IOException(transferError)
            val start = nanoTime()
            consumerNanos += start - lastReadEnd
            try {
                return readNext(buffer, offset, length)
            } finally {
                lastReadEnd = nanoTime()
                inputNanos += lastReadEnd - start
            }
        }

        private fun readNext(buffer: ByteArray, offset: Int, length: Int): Int {
            while (true) {
                val count = try {
                    val readStart = nanoTime()
                    source.read(buffer, offset, length).also { windowInputNanos += nanoTime() - readStart }
                } catch (error: IOException) {
                    recover(error)
                    continue
                }
                if (count < 0) {
                    if (totalBytes >= 0 && delivered != totalBytes) {
                        recover(EOFException())
                        continue
                    }
                    return -1
                }
                if (totalBytes >= 0 && count.toLong() > totalBytes - delivered) {
                    throw InvalidRepresentation(transferError)
                }
                if (!firstRead) {
                    log("first-byte host=${active.url.host} protocol=${active.response.protocol} bodyWaitMs=${millis(nanoTime() - windowStart)}")
                    firstRead = true
                }
                delivered += count
                sampleAndProbe(count)
                return count
            }
        }

        private fun sampleAndProbe(count: Int) {
            val now = nanoTime()
            windowBytes += count
            val sampleMillis = millis(now - lastSample)
            if (sampleMillis >= 1_000) {
                log("sample host=${active.url.host} bytes=$delivered delta=${delivered - lastSampleBytes} ms=$sampleMillis inputMs=${millis(inputNanos)} consumerMs=${millis(consumerNanos)}")
                lastSample = now
                lastSampleBytes = delivered
            }
            val windowMillis = millis(now - windowStart)
            if (windowMillis < (if (firstWindow) policy.firstWindowMs else policy.slowWindowMs)) return
            firstWindow = false
            val rate = windowBytes * 1_000.0 / windowMillis
            if (!triedParallel && active.url.isHttps && original.tag != null &&
                !active.response.header("Accept-Ranges").equals("none", ignoreCase = true) &&
                totalBytes - delivered >= policy.minRemaining && rate < policy.slowBytesPerSecond &&
                windowInputNanos >= (now - windowStart) / 2
            ) {
                triedParallel = true
                startParallel()
            }
            windowStart = nanoTime()
            windowBytes = 0
            windowInputNanos = 0
        }

        private fun startParallel() {
            val url = active.url
            val referrer = active.referrer
            val ranges = ParallelDownloadInput.start(
                delivered, totalBytes, policy.firstPartBytes, policy.partBytes,
                createCall = { connections, start, end ->
                    val transport = client.newBuilder()
                        .connectTimeout(8, TimeUnit.SECONDS)
                        .readTimeout(minOf(client.readTimeoutMillis, 8_000).toLong(), TimeUnit.MILLISECONDS)
                        .connectionPool(connections ?: ConnectionPool(0, 1, TimeUnit.SECONDS))
                        .build()
                    rangeCall(url, referrer, start, end, transport)
                },
                readPart = { call, start, end, buffer, offset, received ->
                    readPart(call, referrer, start, end, buffer, offset, received)
                },
                canRetry = { error -> error !is InvalidRepresentation && canRecover(error, readingBody = true) },
                onRetry = { start, end, error ->
                    log("range retry host=${url.host} start=$start end=$end reason=${error.javaClass.simpleName}")
                }
            ) ?: return
            if (!ranges.awaitFirstBytes(policy.rangeStartupMs)) {
                ranges.close()
                log("parallel kept original host=${url.host} offset=$delivered")
                return
            }
            closeActive()
            source = ranges
            parallel = true
            log("parallel started host=${url.host} offset=$delivered lanes=${ParallelDownloadInput.LANES}")
        }

        private fun readPart(
            call: Call,
            referrer: String?,
            start: Long,
            end: Long,
            buffer: ByteArray,
            offset: Int,
            received: (Int) -> Unit
        ) {
            execute(call, referrer, trace = false).use { attempt ->
                validateRange(attempt, start, end)
                val input = attempt.body.byteStream()
                var filled = offset
                while (filled < buffer.size) {
                    val count = input.read(buffer, filled, buffer.size - filled)
                    if (count < 0) throw EOFException()
                    filled += count
                    received(count)
                }
                if (input.read() >= 0) throw InvalidRepresentation(transferError)
            }
        }

        private fun recover(cause: IOException) {
            if ((!parallel && active.call.isCanceled()) || !canRecover(cause, readingBody = true) ||
                (delivered != 0L && (original.tag == null || totalBytes <= delivered))
            ) throw cause
            if (parallel && !restartedParallel && cause !is InvalidRepresentation) {
                restartedParallel = true
                triedParallel = false
                firstWindow = true
            }
            var failure = cause
            while (recoveries < MAX_RECOVERIES) {
                recoveries++
                closeActive()
                log("recover host=${active.url.host} offset=$delivered attempt=$recoveries reason=${failure.javaClass.simpleName}")
                pause(1_000L * recoveries)
                var reopened: Attempt? = null
                try {
                    val attempt = if (delivered == 0L) {
                        openFull(originalUrl, userAgent, originalReferrer, fresh = true)
                    } else execute(rangeCall(active.url, active.referrer), active.referrer)
                    reopened = attempt
                    if (delivered == 0L) {
                        if (representation(attempt) != original) throw InvalidRepresentation(transferError)
                    } else validateRange(attempt, delivered, totalBytes - 1)
                    replace(attempt, stream(attempt))
                    windowStart = nanoTime()
                    windowBytes = 0
                    windowInputNanos = 0
                    return
                } catch (error: IOException) {
                    reopened?.close()
                    if (!canRecover(error)) throw error
                    failure = error
                }
            }
            throw failure
        }

        private fun rangeCall(
            url: HttpUrl,
            referrer: String?,
            start: Long = delivered,
            end: Long? = null,
            transport: OkHttpClient = freshClient()
        ): Call = transport.newCall(
            request(url, userAgent, referrer)
                .header("Range", "bytes=$start-${end ?: ""}")
                .header("If-Range", checkNotNull(original.tag))
                .build()
        )

        private fun validateRange(attempt: Attempt, expectedStart: Long, expectedEnd: Long) {
            val representation = representation(attempt)
            val response = attempt.response
            val range = response.headers("Content-Range").singleOrNull()?.let(CONTENT_RANGE::matchEntire)
            val (start, end, size) = range?.destructured ?: throw InvalidRepresentation(transferError)
            if (response.code != 206 || !representation.identity || representation.tag != original.tag ||
                representation.mime.startsWith("multipart/", ignoreCase = true) ||
                start.toLongOrNull() != expectedStart || end.toLongOrNull() != expectedEnd ||
                size.toLongOrNull() != totalBytes ||
                (representation.size >= 0 && representation.size != expectedEnd - expectedStart + 1)
            ) throw InvalidRepresentation(transferError)
        }

        private fun closeActive() {
            active.close()
            runCatching { source.close() }.onFailure {
                log("close host=${active.url.host} reason=${it.javaClass.simpleName}")
            }
        }

        private fun replace(attempt: Attempt, next: InputStream) {
            closeActive()
            active = attempt
            source = next
            parallel = false
        }

        override fun close() {
            if (closed) return
            closed = true
            closeActive()
            log("finished host=${active.url.host} bytes=$delivered total=$totalBytes recoveries=$recoveries inputMs=${millis(inputNanos)} consumerMs=${millis(consumerNanos)}")
        }
    }

    companion object {
        private const val MAX_RECOVERIES = 2
        private val REDIRECTS = setOf(301, 302, 303, 307, 308)
        private val STRONG_TAG = Regex("\"[!#-~\\x80-\\xff]*\"")
        private val CONTENT_RANGE = Regex("bytes ([0-9]+)-([0-9]+)/([0-9]+)", RegexOption.IGNORE_CASE)
        private val defaultClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .eventListenerFactory { it.request().tag(Timing::class.java) ?: EventListener.NONE }
                .build()
        }

        private fun safeUrl(url: HttpUrl): Boolean = url.username.isEmpty() && url.password.isEmpty()
        private fun millis(nanos: Long): Long = TimeUnit.NANOSECONDS.toMillis(nanos)

        private fun canRecover(error: IOException, readingBody: Boolean = false): Boolean = when (error) {
            is SocketTimeoutException, is SocketException, is EOFException -> true
            is ProtocolException -> error.message?.contains("unexpected end", ignoreCase = true) == true
            is InterruptedIOException, is SSLHandshakeException, is SSLPeerUnverifiedException, is ZipException -> false
            else -> readingBody || error.cause is EOFException || error.cause is SocketException
        }
    }
}
