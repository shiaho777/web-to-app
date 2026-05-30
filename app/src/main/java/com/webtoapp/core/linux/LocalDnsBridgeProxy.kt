package com.webtoapp.core.linux

import com.webtoapp.core.logging.AppLogger
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object LocalDnsBridgeProxy {

    private const val TAG = "LocalDnsBridgeProxy"
    private const val IO_BUFFER = 8 * 1024
    private const val MAX_HEADER_BYTES = 64 * 1024
    private const val CONNECT_TIMEOUT_MS = 30_000
    private const val CLIENT_READ_TIMEOUT_MS = 5 * 60_000

    @Volatile private var serverSocket: ServerSocket? = null
    @Volatile private var executor: ThreadPoolExecutor? = null
    @Volatile private var acceptThread: Thread? = null
    @Volatile private var running: Boolean = false
    @Volatile private var listenPort: Int = 0

    @Volatile private var refCount: Int = 0

    @Synchronized
    fun start(): Int {
        if (running && listenPort > 0) {
            refCount++
            return listenPort
        }
        return try {
            val socket = ServerSocket(0, 64, InetAddress.getByName("127.0.0.1"))
            serverSocket = socket
            listenPort = socket.localPort
            running = true
            refCount = 1
            executor = ThreadPoolExecutor(
                0,
                64,
                30L,
                TimeUnit.SECONDS,
                SynchronousQueue()
            ) { runnable ->
                Thread(runnable, "LocalDnsBridgeProxy-Worker").apply { isDaemon = true }
            }.also { it.allowCoreThreadTimeOut(true) }

            val accept = Thread({ acceptLoop(socket) }, "LocalDnsBridgeProxy-Accept").apply {
                isDaemon = true
            }
            acceptThread = accept
            accept.start()

            AppLogger.i(TAG, "DNS bridge proxy listening on 127.0.0.1:$listenPort")
            listenPort
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to start DNS bridge proxy", e)
            stopInternal()
            -1
        }
    }

    @Synchronized
    fun stop() {
        if (!running) return
        if (refCount > 1) {
            refCount--
            return
        }
        AppLogger.d(TAG, "Stopping DNS bridge proxy on port $listenPort")
        stopInternal()
    }

    fun getListenPort(): Int = listenPort

    fun isRunning(): Boolean = running

    fun proxyEnvFor(port: Int): Map<String, String> {
        if (port <= 0) return emptyMap()
        val url = "http://127.0.0.1:$port"
        return mapOf(

            "http_proxy" to url,
            "https_proxy" to url,
            "all_proxy" to url,

            "HTTP_PROXY" to url,
            "HTTPS_PROXY" to url,
            "ALL_PROXY" to url,

            "npm_config_proxy" to url,
            "npm_config_https_proxy" to url,

            "no_proxy" to "localhost,127.0.0.1,::1",
            "NO_PROXY" to "localhost,127.0.0.1,::1",
        )
    }

    private fun stopInternal() {
        running = false
        refCount = 0
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        try { executor?.shutdownNow() } catch (_: Exception) {}
        executor = null
        acceptThread = null
        listenPort = 0
    }

    private fun acceptLoop(socket: ServerSocket) {
        while (running) {
            val client = try {
                socket.accept()
            } catch (e: Exception) {
                if (running) {
                    AppLogger.w(TAG, "accept() error: ${e.message}")
                }
                break
            }
            val ex = executor
            if (ex == null) {
                safeClose(client)
                break
            }
            try {
                ex.execute { handleClient(client) }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to dispatch client: ${e.message}")
                safeClose(client)
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
                safeClose(client)
                return
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
                    safeClose(client)
                    return
                }
            }

            val parts = firstLine.split(' ', limit = 3)
            if (parts.size < 3) {
                sendStatus(output, 400, "Bad Request")
                safeClose(client)
                return
            }
            val method = parts[0]
            val target = parts[1]
            val version = parts[2]

            if (method.equals("CONNECT", ignoreCase = true)) {
                handleConnect(client, output, target)
            } else {
                handleHttpForward(client, input, output, method, target, version, headerLines)
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
            safeClose(client)
            return
        }
        val host = hostPort.substring(0, sep).trim().trim('[', ']')
        val port = hostPort.substring(sep + 1).toIntOrNull() ?: 443

        val targetSocket = try {
            connectTarget(host, port)
        } catch (e: Exception) {
            AppLogger.w(TAG, "CONNECT failed for $host:$port: ${e.message}")
            sendStatus(clientOut, 502, "Bad Gateway")
            safeClose(client)
            return
        }

        try {
            clientOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray(StandardCharsets.US_ASCII))
            clientOut.flush()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to send CONNECT 200: ${e.message}")
            safeClose(client)
            safeClose(targetSocket)
            return
        }

        bridge(client, targetSocket)
    }

    private fun handleHttpForward(
        client: Socket,
        clientIn: BufferedInputStream,
        clientOut: OutputStream,
        method: String,
        target: String,
        version: String,
        headers: List<String>
    ) {
        val requestUri = try { URI(target) } catch (_: Exception) { null }
        val hostHeader = headers.firstOrNull { it.startsWith("Host:", ignoreCase = true) }
            ?.substringAfter(':')
            ?.trim()
        val host = requestUri?.host ?: hostHeader?.substringBefore(':')?.trim()
        if (host.isNullOrBlank()) {
            sendStatus(clientOut, 400, "Bad Request")
            safeClose(client)
            return
        }

        val port = when {
            requestUri?.port != null && requestUri.port > 0 -> requestUri.port
            hostHeader?.substringAfter(':', "")?.toIntOrNull() != null -> hostHeader.substringAfter(':').toInt()
            else -> 80
        }
        val rawPath = requestUri?.rawPath
        val path = if (rawPath.isNullOrEmpty()) "/" else rawPath
        val pathAndQuery = requestUri?.rawQuery?.let { "$path?$it" } ?: path

        val targetSocket = try {
            connectTarget(host, port)
        } catch (e: Exception) {
            AppLogger.w(TAG, "HTTP forward failed for $host:$port: ${e.message}")
            sendStatus(clientOut, 502, "Bad Gateway")
            safeClose(client)
            return
        }

        try {
            val targetOut = targetSocket.getOutputStream()
            val builder = StringBuilder()
            builder.append(method).append(' ').append(pathAndQuery).append(' ').append(version).append("\r\n")
            headers.forEach { line ->
                val lower = line.lowercase(Locale.ROOT)

                if (lower.startsWith("proxy-connection:")) return@forEach
                if (lower.startsWith("proxy-authorization:")) return@forEach
                builder.append(line).append("\r\n")
            }
            builder.append("\r\n")
            targetOut.write(builder.toString().toByteArray(StandardCharsets.ISO_8859_1))
            targetOut.flush()

            pumpRequestBodyIfPresent(clientIn, targetOut, headers)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to forward HTTP request: ${e.message}")
            safeClose(client)
            safeClose(targetSocket)
            return
        }

        bridge(client, targetSocket)
    }

    private fun connectTarget(host: String, port: Int): Socket {
        val socket = Socket()
        try {
            socket.tcpNoDelay = true
            socket.soTimeout = CLIENT_READ_TIMEOUT_MS

            val resolved = InetAddress.getByName(host)
            socket.connect(InetSocketAddress(resolved, port), CONNECT_TIMEOUT_MS)
            return socket
        } catch (e: Exception) {
            safeClose(socket)
            throw e
        }
    }

    private fun pumpRequestBodyIfPresent(
        clientIn: BufferedInputStream,
        targetOut: OutputStream,
        headers: List<String>
    ) {
        val contentLength = headers.firstOrNull { it.startsWith("Content-Length:", ignoreCase = true) }
            ?.substringAfter(':')
            ?.trim()
            ?.toLongOrNull()
        if (contentLength != null && contentLength > 0) {
            copyExact(clientIn, targetOut, contentLength)
            targetOut.flush()
            return
        }

        val transferEncoding = headers.firstOrNull { it.startsWith("Transfer-Encoding:", ignoreCase = true) }
            ?.substringAfter(':')
            ?.trim()
            ?.lowercase(Locale.ROOT)
        if (transferEncoding == "chunked") {
            forwardChunkedBody(clientIn, targetOut)
            targetOut.flush()
        }
    }

    private fun copyExact(input: InputStream, output: OutputStream, byteCount: Long) {
        var remaining = byteCount
        val buffer = ByteArray(IO_BUFFER)
        while (remaining > 0) {
            val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
            if (read < 0) throw IOException("Unexpected EOF while forwarding request body")
            output.write(buffer, 0, read)
            remaining -= read
        }
    }

    private fun forwardChunkedBody(input: BufferedInputStream, output: OutputStream) {
        while (true) {
            val chunkSizeLine = readHttpLine(input) ?: throw IOException("Unexpected EOF reading chunk size")
            output.write(chunkSizeLine.toByteArray(StandardCharsets.ISO_8859_1))
            output.write("\r\n".toByteArray(StandardCharsets.US_ASCII))

            val chunkSize = chunkSizeLine.substringBefore(';').trim().toIntOrNull(16)
                ?: throw IOException("Invalid chunk size: $chunkSizeLine")
            if (chunkSize == 0) {
                while (true) {
                    val trailerLine = readHttpLine(input) ?: throw IOException("Unexpected EOF reading chunk trailer")
                    output.write(trailerLine.toByteArray(StandardCharsets.ISO_8859_1))
                    output.write("\r\n".toByteArray(StandardCharsets.US_ASCII))
                    if (trailerLine.isEmpty()) {
                        return
                    }
                }
            }

            copyExact(input, output, chunkSize.toLong())
            copyExact(input, output, 2)
        }
    }

    private fun bridge(a: Socket, b: Socket) {
        val ex = executor ?: run {
            safeClose(a)
            safeClose(b)
            return
        }
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
        } catch (_: SocketTimeoutException) {

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
}
