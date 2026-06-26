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

    data class Config(
        val template: TlsFingerprintTemplate,
        val customCipherSuites: List<String> = emptyList(),
        val upstreamSocks: LocalHttpToSocksBridge.Upstream? = null
    )

    @Volatile private var serverSocket: ServerSocket? = null
    @Volatile private var executor: ThreadPoolExecutor? = null
    @Volatile private var acceptThread: Thread? = null
    @Volatile private var running: Boolean = false
    @Volatile private var currentConfig: Config? = null
    @Volatile private var listenPort: Int = 0
    @Volatile private var caDir: File? = null

    @Synchronized
    fun start(config: Config, caDir: File): Int {
        TlsMitmCaManager.init(caDir)
        if (!TlsMitmCaManager.isCaInitialized()) {
            AppLogger.e(TAG, "CA manager failed to init, aborting TLS MITM bridge")
            return -1
        }

        if (running && currentConfig == config && listenPort > 0) {
            return listenPort
        }
        stopInternal()

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

            AppLogger.i(
                TAG,
                "TLS MITM bridge listening 127.0.0.1:$listenPort template=${config.template.id} customCiphers=${config.customCipherSuites.size} socks=${config.upstreamSocks != null}"
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

        val config = currentConfig
        if (config == null) {
            sendStatus(clientOut, 502, "Bad Gateway")
            safeClose(client); return
        }

        val upstreamSocket = try {
            TlsUpstreamConnector.connect(
                host = host,
                port = port,
                template = config.template,
                customCipherSuites = config.customCipherSuites,
                upstreamSocks = config.upstreamSocks
            ).sslSocket
        } catch (e: Exception) {
            AppLogger.w(TAG, "Upstream TLS connect failed for $host:$port: ${e.message}")
            sendStatus(clientOut, 502, "Bad Gateway")
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

        val mitmSocket = try {
            performTlsHandshake(client, host)
        } catch (e: Exception) {
            AppLogger.w(TAG, "MITM TLS handshake failed for $host: ${e.message}")
            safeClose(upstreamSocket)
            safeClose(client); return
        }

        bridge(mitmSocket, upstreamSocket)
    }

    private fun performTlsHandshake(client: Socket, host: String): SSLSocket {
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
            params.applicationProtocols = arrayOf("h2", "http/1.1")
            params.endpointIdentificationAlgorithm = null
            sslSocket.sslParameters = params
        } catch (_: Exception) {
        }

        sslSocket.startHandshake()
        return sslSocket
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
