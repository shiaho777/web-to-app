package com.webtoapp.core.webview

import com.webtoapp.core.logging.AppLogger
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object TlsMitmBridge {

    private const val TAG = "TlsMitmBridge"
    private const val IO_BUFFER = 8 * 1024
    private const val MAX_HEADER_BYTES = 64 * 1024
    private const val CLIENT_READ_TIMEOUT_MS = 60_000

    /** h3 forwarding buffers full request bodies; huge uploads exceed that contract. */
    private const val MAX_H3_BODY_BYTES = 32 * 1024 * 1024

    data class Config(
        val template: TlsFingerprintTemplate,
        val customCipherSuites: List<String> = emptyList(),
        val upstreamSocks: LocalHttpToSocksBridge.Upstream? = null,
        val forceHttp3: Boolean = false,
        /** ECH on the system engine: the upstream leg is Cronet, whose Chromium stack
         *  fetches HTTPS records and encrypts the ClientHello SNI natively (issue #721). */
        val echUpstream: Boolean = false
    ) {
        /**
         * Both forced HTTP/3 and ECH ride the same MITM bridge as fingerprint spoofing,
         * but their upstream leg is Cronet — which cannot chain through a SOCKS proxy.
         * When the user configures a SOCKS upstream the bridge stays on the classic
         * relay and both toggles are inert.
         */
        val cronetActive: Boolean get() = (forceHttp3 || echUpstream) && upstreamSocks == null
    }

    @Volatile private var serverSocket: ServerSocket? = null
    @Volatile private var executor: ThreadPoolExecutor? = null
    @Volatile private var acceptThread: Thread? = null
    @Volatile private var running: Boolean = false
    @Volatile private var currentConfig: Config? = null
    @Volatile private var listenPort: Int = 0
    @Volatile private var caDir: File? = null
    @Volatile private var appContext: android.content.Context? = null

    /** User-imported CA anchors additionally trusted when verifying upstream certificates. */
    @Volatile private var customCaAnchors: List<java.security.cert.X509Certificate> = emptyList()

    /**
     * Android loopback is shared by every app on the device, so the proxy must not be an
     * open relay: only hosts the WebView actually navigated/requested may be tunneled, and
     * leaf certificates are only minted for tunnelable hosts. WebViewManager feeds this
     * set from onPageStarted / shouldInterceptRequest.
     */
    private val allowedHosts: MutableSet<String> = java.util.concurrent.ConcurrentHashMap.newKeySet()

    /**
     * Host-allowlist enforcement. Always on for the WebView engine (whose requests are
     * observable via shouldInterceptRequest); disabled by the Gecko engine, which has no
     * per-request callback to feed the set with.
     */
    @Volatile private var hostAllowlistEnforcement: Boolean = true

    fun setHostAllowlistEnforcement(enabled: Boolean) {
        hostAllowlistEnforcement = enabled
    }

    fun allowHost(host: String?) {
        val normalized = host?.trim()?.trim('[', ']')?.lowercase(Locale.ROOT) ?: return
        if (normalized.isNotEmpty()) allowedHosts.add(normalized)
    }

    fun isHostAllowed(host: String?): Boolean {
        if (!hostAllowlistEnforcement) return true
        val normalized = host?.trim()?.trim('[', ']')?.lowercase(Locale.ROOT) ?: return false
        return allowedHosts.contains(normalized)
    }

    @Synchronized
    fun start(
        config: Config,
        caDir: File,
        customCaAnchors: List<java.security.cert.X509Certificate> = emptyList(),
        appContext: android.content.Context? = null
    ): Int {
        TlsMitmCaManager.init(caDir)
        if (!TlsMitmCaManager.isCaInitialized()) {
            AppLogger.e(TAG, "CA manager failed to init, aborting TLS MITM bridge")
            return -1
        }

        if (running && currentConfig == config && listenPort > 0) {
            this.customCaAnchors = customCaAnchors
            return listenPort
        }
        stopInternal()
        this.customCaAnchors = customCaAnchors
        this.appContext = appContext

        try {
            val socket = ServerSocket(0, 64, InetAddress.getByName("127.0.0.1"))
            serverSocket = socket
            listenPort = socket.localPort
            currentConfig = config
            this.caDir = caDir
            running = true
            executor = ThreadPoolExecutor(
                0, 64, 30L, TimeUnit.SECONDS,
                SynchronousQueue()
            ) { r ->
                Thread(r, "TlsMitmBridge-Worker").apply { isDaemon = true }
            }.also { it.allowCoreThreadTimeOut(true) }

            val accept = Thread({ acceptLoop(socket) }, "TlsMitmBridge-Accept").apply {
                isDaemon = true
            }
            acceptThread = accept
            accept.start()

            if (config.cronetActive && appContext != null) {
                CronetForwarder.start(appContext, echUpstream = config.echUpstream)
            }

            AppLogger.i(
                TAG,
                "TLS MITM bridge listening 127.0.0.1:$listenPort template=${config.template.id} customCiphers=${config.customCipherSuites.size} socks=${config.upstreamSocks != null} http3=${config.forceHttp3} ech=${config.echUpstream}"
            )
            return listenPort
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to start TLS MITM bridge", e)
            stopInternal()
            return -1
        }
    }

    @Synchronized
    fun stop() {
        if (!running && serverSocket == null) return
        AppLogger.d(TAG, "Stopping TLS MITM bridge on port $listenPort")
        stopInternal()
    }

    fun getListenPort(): Int = listenPort

    fun isRunning(): Boolean = running

    private fun stopInternal() {
        running = false
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        try { executor?.shutdownNow() } catch (_: Exception) {}
        executor = null
        acceptThread = null
        listenPort = 0
        currentConfig = null
        customCaAnchors = emptyList()
        allowedHosts.clear()
        hostAllowlistEnforcement = true
        appContext = null
        try { CronetForwarder.stop() } catch (_: Throwable) {}
    }

    private fun acceptLoop(socket: ServerSocket) {
        while (running) {
            val client: Socket = try {
                socket.accept()
            } catch (e: Exception) {
                if (running) {
                    AppLogger.w(TAG, "accept() error: ${e.message}")
                }
                break
            }
            val ex = executor
            if (ex == null) {
                try { client.close() } catch (_: Exception) {}
                break
            }
            try {
                ex.execute { handleClient(client) }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to dispatch client: ${e.message}")
                try { client.close() } catch (_: Exception) {}
            }
        }
    }

    private fun handleClient(client: Socket) {
        try {
            client.tcpNoDelay = true
            client.soTimeout = CLIENT_READ_TIMEOUT_MS
        } catch (_: Exception) {}

        try {
            val input = BufferedInputStream(client.getInputStream())
            val output = client.getOutputStream()

            val firstLine = readHttpLine(input) ?: run {
                safeClose(client); return
            }
            val headerLines = mutableListOf<String>()
            var headerBytes = firstLine.length
            while (true) {
                val line = readHttpLine(input) ?: break
                if (line.isEmpty()) break
                headerLines.add(line)
                headerBytes += line.length
                if (headerBytes > MAX_HEADER_BYTES) {
                    sendStatus(output, 431, "Request Header Fields Too Large")
                    safeClose(client); return
                }
            }

            val parts = firstLine.split(' ', limit = 3)
            if (parts.size < 3) {
                sendStatus(output, 400, "Bad Request")
                safeClose(client); return
            }
            val method = parts[0]
            val target = parts[1]

            if (method.equals("CONNECT", ignoreCase = true)) {
                handleConnect(client, output, target)
            } else {
                handleHttpForward(client, input, output, method, target, headerLines)
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "client error: ${e.message}")
            safeClose(client)
        }
    }

    private fun handleConnect(
        client: Socket,
        clientOut: OutputStream,
        hostPort: String
    ) {
        val sep = hostPort.lastIndexOf(':')
        if (sep <= 0) {
            sendStatus(clientOut, 400, "Bad Request")
            safeClose(client); return
        }
        val host = hostPort.substring(0, sep).trim().trim('[', ']')
        val port = hostPort.substring(sep + 1).toIntOrNull() ?: 443

        if (!isHostAllowed(host)) {
            // Another app on the device probing the shared loopback interface: without
            // this check the bridge relays for anyone and mints locally-trusted leaf
            // certificates for arbitrary domains.
            AppLogger.w(TAG, "CONNECT rejected (host not requested by the WebView): $host")
            sendStatus(clientOut, 403, "Forbidden")
            safeClose(client); return
        }

        val config = currentConfig
        if (config == null) {
            sendStatus(clientOut, 502, "Bad Gateway")
            safeClose(client); return
        }

        // Missing Cronet (not yet downloaded / not embedded) degrades to the classic
        // relay instead of failing every request with 502.
        val cronetCtx = appContext
        if (config.cronetActive && cronetCtx != null && CronetForwarder.isReady(cronetCtx)) {
            handleConnectCronet(client, clientOut, host, port, config)
            return
        }

        // Handshake with the WebView FIRST and remember which ALPN protocol it picked,
        // so the upstream connection can be restricted to the same protocol. The bridge
        // relays raw bytes — if the two sides negotiated independently, a WebView on h2
        // facing an http/1.1-only origin would stream HTTP/2 frames into an HTTP/1.1
        // server and every request to that host would fail.
        val mitmSocket = try {
            performTlsHandshake(client, host)
        } catch (e: Exception) {
            AppLogger.w(TAG, "MITM TLS handshake failed for $host: ${e.message}")
            sendStatus(clientOut, 502, "Bad Gateway")
            safeClose(client); return
        }
        val clientProtocol = try { mitmSocket.applicationProtocol } catch (_: Exception) { null }

        val upstreamSocket = try {
            TlsUpstreamConnector.connect(
                host = host,
                port = port,
                template = config.template,
                customCipherSuites = config.customCipherSuites,
                upstreamSocks = config.upstreamSocks,
                restrictAlpnTo = clientProtocol,
                customCaAnchors = customCaAnchors
            ).sslSocket
        } catch (e: Exception) {
            AppLogger.w(TAG, "Upstream TLS connect failed for $host:$port: ${e.message}")
            safeClose(mitmSocket)
            safeClose(client); return
        }

        try {
            clientOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray(StandardCharsets.US_ASCII))
            clientOut.flush()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to send CONNECT 200: ${e.message}")
            safeClose(upstreamSocket)
            safeClose(client); return
        }

        // Tunnel mode (SSE/WebSocket/long-poll) can stay idle for minutes. The 60s
        // handshake-phase SO_TIMEOUT would kill any such connection mid-session
        // (copyStream swallows the SocketTimeoutException, so the teardown looks like a
        // silent drop) — clear it once the tunnel is established, mirroring
        // Socks5Connector's reset to 0 after the SOCKS handshake.
        try { mitmSocket.soTimeout = 0 } catch (_: Exception) {}
        try { upstreamSocket.soTimeout = 0 } catch (_: Exception) {}

        bridge(mitmSocket, upstreamSocket)
    }

    private fun performTlsHandshake(client: Socket, host: String): SSLSocket {
        return performTlsHandshake(client, host, arrayOf("h2", "http/1.1"))
    }

    private fun performTlsHandshake(client: Socket, host: String, alpn: Array<String>): SSLSocket {
        val kmf = TlsMitmCaManager.createKeyManagerFactory(host)
            ?: throw java.io.IOException("Failed to create key manager for $host")

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(kmf.keyManagers, arrayOf<TrustManager>(AcceptAllTrustManager()), null)

        val sslSocket = sslContext.socketFactory.createSocket(
            client, host, client.port, true
        ) as SSLSocket
        sslSocket.useClientMode = false

        try {
            val params = sslSocket.sslParameters
            params.applicationProtocols = alpn
            params.endpointIdentificationAlgorithm = null
            sslSocket.sslParameters = params
        } catch (_: Exception) {
        }

        sslSocket.startHandshake()
        return sslSocket
    }

    /**
     * Cronet upstream path (forced HTTP/3 and/or ECH): the bridge terminates TLS with
     * the WebView speaking plain HTTP/1.1 only, then re-issues every request through
     * Cronet — Chromium's own stack, QUIC-first when h3 forcing is on, ECH when the
     * target publishes an HTTPS record. WebSocket upgrades and other non-plain-HTTP
     * tunnels fall back to the classic raw relay. Cronet is a request/response engine,
     * not a byte pump — this is the only part of the bridge that speaks HTTP.
     */
    private fun handleConnectCronet(
        client: Socket,
        clientOut: OutputStream,
        host: String,
        port: Int,
        config: Config
    ) {
        val context = appContext ?: run { safeClose(client); return }

        AppLogger.d(TAG, "h3 CONNECT tunnel: $host:$port")

        // Answer the CONNECT before handshaking: clients that wait for the 200
        // before starting TLS (no ClientHello pipelining) would otherwise deadlock
        // the tunnel until their connect timeout fires. A pipelining client simply
        // leaves its buffered ClientHello in the socket until we read it.
        try {
            clientOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray(StandardCharsets.US_ASCII))
            clientOut.flush()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to send CONNECT 200: ${e.message}")
            safeClose(client); return
        }

        val mitmSocket = try {
            performTlsHandshake(client, host, arrayOf("http/1.1"))
        } catch (e: Exception) {
            AppLogger.w(TAG, "MITM TLS handshake failed for $host: ${e.message}")
            safeClose(client); return
        }

        // Keep-alive connections idle between requests; Chromium closes its side when
        // its pool retires the socket, and loopback always delivers the FIN.
        try { mitmSocket.soTimeout = 0 } catch (_: Exception) {}

        val input = BufferedInputStream(mitmSocket.getInputStream())
        val output = mitmSocket.getOutputStream()
        val hostPart = if (host.contains(':') && !host.startsWith("[")) "[$host]" else host
        val portPart = if (port == 443) "" else ":$port"

        try {
            while (true) {
                val firstLine = readHttpLine(input) ?: break
                if (firstLine.isEmpty()) continue
                val parts = firstLine.split(' ', limit = 3)
                if (parts.size < 3) {
                    sendStatus(output, 400, "Bad Request")
                    break
                }
                val method = parts[0]
                val target = parts[1]

                val headerLines = mutableListOf<String>()
                var headerBytes = firstLine.length
                while (true) {
                    val line = readHttpLine(input) ?: return
                    if (line.isEmpty()) break
                    headerLines.add(line)
                    headerBytes += line.length
                    if (headerBytes > MAX_HEADER_BYTES) {
                        sendStatus(output, 431, "Request Header Fields Too Large")
                        return
                    }
                }

                // Upgrades (WebSocket, h2c, ...) cannot ride a request/response engine;
                // hand the connection to the classic raw TLS relay instead.
                val wantsUpgrade = headerLines.any { it.startsWith("upgrade:", ignoreCase = true) }
                if (wantsUpgrade) {
                    fallbackToRawRelay(mitmSocket, input, host, port, config, firstLine, headerLines)
                    return
                }

                val clientWantsClose = headerLines.any {
                    it.startsWith("connection:", ignoreCase = true) &&
                        it.substringAfter(':', "").contains("close", ignoreCase = true)
                }

                val body = try {
                    readRequestBody(input, headerLines)
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Failed to read request body for $host: ${e.message}")
                    sendStatus(output, 413, "Payload Too Large")
                    return
                }

                val filteredHeaders = headerLines.mapNotNull { line ->
                    val lower = line.lowercase(Locale.ROOT)
                    when {
                        lower.startsWith("host:") -> null
                        lower.startsWith("proxy-connection:") ||
                            lower.startsWith("proxy-authorization:") -> null
                        lower.startsWith("connection:") ||
                            lower.startsWith("keep-alive:") -> null
                        lower.startsWith("content-length:") ||
                            lower.startsWith("transfer-encoding:") -> null
                        else -> {
                            val idx = line.indexOf(':')
                            if (idx > 0) {
                                line.substring(0, idx).trim() to line.substring(idx + 1).trim()
                            } else {
                                null
                            }
                        }
                    }
                }

                val requestUrl = if (target.startsWith("http://", true) || target.startsWith("https://", true)) {
                    target
                } else {
                    "https://$hostPart$portPart$target"
                }

                val forwarded = try {
                    CronetForwarder.forward(
                        context = context,
                        method = method,
                        url = requestUrl,
                        headers = filteredHeaders,
                        body = body,
                        sink = output,
                        clientWantsClose = clientWantsClose,
                        hostForHint = host,
                        portForHint = port
                    )
                } catch (e: java.io.IOException) {
                    // Client broke mid-response; nothing left to salvage.
                    AppLogger.d(TAG, "h3 forward client I/O error for $host: ${e.message}")
                    return
                }

                if (!forwarded) {
                    sendStatus(output, 502, "Bad Gateway")
                    return
                }
                if (clientWantsClose) break
            }
        } catch (e: Exception) {
            AppLogger.d(TAG, "h3 connect loop ended for $host: ${e.message}")
        } finally {
            safeClose(mitmSocket)
        }
    }

    /**
     * WebSocket / upgrade fallback: replay the already-parsed request head into a
     * classic upstream TLS connection, flush anything the buffered stream pre-read,
     * then raw-relay the rest of the session.
     */
    private fun fallbackToRawRelay(
        mitmSocket: SSLSocket,
        bufferedInput: BufferedInputStream,
        host: String,
        port: Int,
        config: Config,
        firstLine: String,
        headerLines: List<String>
    ) {
        val upstreamSocket = try {
            TlsUpstreamConnector.connect(
                host = host,
                port = port,
                template = config.template,
                customCipherSuites = config.customCipherSuites,
                upstreamSocks = config.upstreamSocks,
                restrictAlpnTo = "http/1.1",
                customCaAnchors = customCaAnchors
            ).sslSocket
        } catch (e: Exception) {
            AppLogger.w(TAG, "h3 upgrade fallback upstream connect failed for $host:$port: ${e.message}")
            safeClose(mitmSocket)
            return
        }

        try {
            val upstreamOut = upstreamSocket.getOutputStream()
            val sb = StringBuilder()
            sb.append(firstLine).append("\r\n")
            headerLines.forEach { sb.append(it).append("\r\n") }
            sb.append("\r\n")
            upstreamOut.write(sb.toString().toByteArray(StandardCharsets.ISO_8859_1))
            upstreamOut.flush()

            // The buffered stream may hold pipelined bytes past the request head.
            val preRead = ByteArray(IO_BUFFER)
            while (bufferedInput.available() > 0) {
                val n = bufferedInput.read(preRead)
                if (n <= 0) break
                upstreamOut.write(preRead, 0, n)
            }
            upstreamOut.flush()
        } catch (e: Exception) {
            AppLogger.w(TAG, "h3 upgrade fallback replay failed for $host: ${e.message}")
            safeClose(upstreamSocket)
            safeClose(mitmSocket)
            return
        }

        try { mitmSocket.soTimeout = 0 } catch (_: Exception) {}
        try { upstreamSocket.soTimeout = 0 } catch (_: Exception) {}
        bridge(mitmSocket, upstreamSocket)
    }

    /** Reads a request body bounded by Content-Length or chunked framing. */
    private fun readRequestBody(input: InputStream, headerLines: List<String>): ByteArray? {
        val transferEncoding = headerLines.firstOrNull { it.startsWith("transfer-encoding:", ignoreCase = true) }
            ?.substringAfter(':')
            ?.trim()
            ?.lowercase(Locale.ROOT)
        return if (transferEncoding != null && transferEncoding.contains("chunked")) {
            readChunkedBody(input)
        } else {
            val contentLength = headerLines.firstOrNull { it.startsWith("content-length:", ignoreCase = true) }
                ?.substringAfter(':')
                ?.trim()
                ?.toIntOrNull()
                ?: return null
            if (contentLength <= 0) null else readBounded(input, contentLength)
        }
    }

    private fun readBounded(input: InputStream, length: Int): ByteArray {
        if (length > MAX_H3_BODY_BYTES) {
            throw java.io.IOException("Request body too large: $length")
        }
        val buffer = ByteArray(length)
        var off = 0
        while (off < length) {
            val read = input.read(buffer, off, length - off)
            if (read < 0) throw java.io.EOFException("Request body truncated")
            off += read
        }
        return buffer
    }

    private fun readChunkedBody(input: InputStream): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        while (true) {
            val sizeLine = readHttpLine(input) ?: throw java.io.EOFException("Chunked body truncated")
            val size = sizeLine.substringBefore(';').trim().toIntOrNull(16)
                ?: throw java.io.IOException("Bad chunk size: $sizeLine")
            if (size == 0) {
                // Consume trailers until the empty line.
                while (true) {
                    val trailer = readHttpLine(input) ?: break
                    if (trailer.isEmpty()) break
                }
                break
            }
            if (out.size() + size > MAX_H3_BODY_BYTES) {
                throw java.io.IOException("Chunked request body too large")
            }
            readBoundedInto(input, out, size)
            // Trailing CRLF after each chunk.
            readHttpLine(input)
        }
        return out.toByteArray()
    }

    private fun readBoundedInto(input: InputStream, out: java.io.ByteArrayOutputStream, length: Int) {
        val buffer = ByteArray(minOf(length, IO_BUFFER))
        var remaining = length
        while (remaining > 0) {
            val read = input.read(buffer, 0, minOf(remaining, buffer.size))
            if (read < 0) throw java.io.EOFException("Chunk truncated")
            out.write(buffer, 0, read)
            remaining -= read
        }
    }

    private fun handleHttpForward(
        client: Socket,
        clientIn: BufferedInputStream,
        clientOut: OutputStream,
        method: String,
        target: String,
        headers: List<String>
    ) {
        val requestUri = try { URI(target) } catch (_: Exception) { null }
        val hostHeader = headers.firstOrNull { it.startsWith("Host:", ignoreCase = true) }
            ?.substringAfter(':')
            ?.trim()
        val host = requestUri?.host ?: hostHeader?.substringBefore(':')?.trim()
        if (host.isNullOrBlank()) {
            sendStatus(clientOut, 400, "Bad Request")
            safeClose(client); return
        }

        val port = when {
            requestUri?.port != null && requestUri.port > 0 -> requestUri.port
            hostHeader?.substringAfter(':', "")?.toIntOrNull() != null -> hostHeader.substringAfter(':').toInt()
            else -> 80
        }
        if (!isHostAllowed(host)) {
            AppLogger.w(TAG, "Forward rejected (host not requested by the WebView): $host")
            sendStatus(clientOut, 403, "Forbidden")
            safeClose(client); return
        }
        val rawPath = requestUri?.rawPath
        val path = if (rawPath.isNullOrEmpty()) "/" else rawPath
        val pathAndQuery = requestUri?.rawQuery?.let { "$path?$it" } ?: path

        val config = currentConfig
        if (config == null) {
            sendStatus(clientOut, 502, "Bad Gateway")
            safeClose(client); return
        }

        val targetSocket = try {
            val rawTarget = if (config.upstreamSocks != null) {
                LocalHttpToSocksBridge.Socks5Connector.connect(
                    config.upstreamSocks.host,
                    config.upstreamSocks.port,
                    host,
                    port,
                    config.upstreamSocks.username,
                    config.upstreamSocks.password
                )
            } else {
                Socket().apply {
                    tcpNoDelay = true
                    connect(InetSocketAddress(host, port), 15_000)
                    soTimeout = CLIENT_READ_TIMEOUT_MS
                }
            }
            rawTarget
        } catch (e: Exception) {
            AppLogger.w(TAG, "HTTP forward connect failed for $host:$port: ${e.message}")
            sendStatus(clientOut, 502, "Bad Gateway")
            safeClose(client); return
        }

        try {
            val targetOut = targetSocket.getOutputStream()
            val builder = StringBuilder()
            builder.append(method).append(' ').append(pathAndQuery).append(" HTTP/1.1\r\n")
            headers.forEach { line ->
                val lower = line.lowercase(Locale.ROOT)
                if (lower.startsWith("proxy-connection:")) return@forEach
                if (lower.startsWith("proxy-authorization:")) return@forEach
                builder.append(line).append("\r\n")
            }
            builder.append("\r\n")
            targetOut.write(builder.toString().toByteArray(StandardCharsets.ISO_8859_1))
            targetOut.flush()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to forward HTTP request: ${e.message}")
            safeClose(client); safeClose(targetSocket); return
        }

        bridge(client, targetSocket)
    }

    private fun bridge(a: Socket, b: Socket) {
        val ex = executor ?: run { safeClose(a); safeClose(b); return }
        val pump = ex.submit {
            copyStream(a, b)
            try { b.shutdownOutput() } catch (_: Exception) {}
        }
        copyStream(b, a)
        try { a.shutdownOutput() } catch (_: Exception) {}
        try { pump.get(2, TimeUnit.SECONDS) } catch (_: Exception) {}
        safeClose(a)
        safeClose(b)
    }

    private fun copyStream(from: Socket, to: Socket) {
        try {
            val input = from.getInputStream()
            val output = to.getOutputStream()
            val buffer = ByteArray(IO_BUFFER)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                output.write(buffer, 0, read)
                output.flush()
            }
        } catch (_: Exception) {
        }
    }

    private fun readHttpLine(input: InputStream): String? {
        val bytes = ByteArrayOutputStream()
        while (true) {
            val next = try { input.read() } catch (_: Exception) { return null }
            if (next < 0) return if (bytes.size() == 0) null else bytes.toString(StandardCharsets.ISO_8859_1.name())
            if (next == 0x0A) {
                val raw = bytes.toByteArray()
                val length = if (raw.isNotEmpty() && raw.last() == '\r'.code.toByte()) raw.size - 1 else raw.size
                return String(raw, 0, length, StandardCharsets.ISO_8859_1)
            }
            bytes.write(next)
            if (bytes.size() > MAX_HEADER_BYTES) {
                return bytes.toString(StandardCharsets.ISO_8859_1.name())
            }
        }
    }

    private fun sendStatus(out: OutputStream, code: Int, reason: String) {
        try {
            val msg = "HTTP/1.1 $code $reason\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
            out.write(msg.toByteArray(StandardCharsets.US_ASCII))
            out.flush()
        } catch (_: Exception) {}
    }

    private fun safeClose(socket: Socket) {
        try { socket.close() } catch (_: Exception) {}
    }

    private class AcceptAllTrustManager : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = emptyArray()
    }
}
