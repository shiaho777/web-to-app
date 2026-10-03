package com.webtoapp.core.agent.mcp

import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets

/**
 * HTTP/1.1 on 127.0.0.1 only. Speaks just enough for one MCP POST: request
 * line, headers, a Content-Length body, and one response. No chunked requests.
 */
class LoopbackHttpServer(
    private val preferredPort: Int,
    private val handler: suspend (HttpRequest) -> HttpResponse
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var server: ServerSocket? = null

    @Volatile
    var boundPort: Int = -1
        private set

    fun start() {
        val socket = ServerSocket()
        socket.reuseAddress = true
        val loopback = InetAddress.getByName("127.0.0.1")
        socket.bind(InetSocketAddress(loopback, preferredPort))
        server = socket
        boundPort = socket.localPort
        Thread({
            while (!socket.isClosed) {
                val client = try {
                    socket.accept()
                } catch (_: Exception) {
                    break
                }
                if (!client.inetAddress.isLoopbackAddress) {
                    client.closeQuietly()
                    continue
                }
                scope.launch { serve(client) }
            }
        }, "host-mcp-accept").apply { isDaemon = true }.start()
    }

    fun stop() {
        try {
            server?.close()
        } catch (_: Exception) {
        }
        server = null
    }

    private suspend fun serve(client: Socket) {
        client.soTimeout = 30_000
        try {
            val input = BufferedInputStream(client.getInputStream())
            val request = readRequest(input) ?: run {
                write(client, HttpResponse(400, "Bad Request", "text/plain", "Bad Request"))
                return
            }
            val response = try {
                handler(request)
            } catch (e: Exception) {
                AppLogger.w(TAG, "handler failed: ${e.message}")
                HttpResponse(500, "Internal Server Error", "text/plain", "Internal Server Error")
            }
            write(client, response)
        } catch (e: Exception) {
            AppLogger.w(TAG, "connection failed: ${e.message}")
        } finally {
            client.closeQuietly()
        }
    }

    private fun readRequest(input: BufferedInputStream): HttpRequest? {
        val headerBytes = readUntil(input, HEADER_END, MAX_HEADER_BYTES) ?: return null
        val headerText = String(headerBytes, StandardCharsets.ISO_8859_1)
        val splitAt = headerText.indexOf("\r\n\r\n")
        if (splitAt < 0) return null
        val head = headerText.substring(0, splitAt)
        val lines = head.split("\r\n")
        if (lines.isEmpty()) return null
        val requestLine = lines[0].split(" ")
        if (requestLine.size < 2) return null
        val headers = LinkedHashMap<String, String>()
        for (i in 1 until lines.size) {
            val line = lines[i]
            val colon = line.indexOf(':')
            if (colon <= 0) continue
            headers[line.substring(0, colon).trim().lowercase()] = line.substring(colon + 1).trim()
        }
        val length = headers["content-length"]?.toIntOrNull() ?: 0
        if (length < 0 || length > MAX_BODY_BYTES) return null
        val already = headerBytes.size - (splitAt + 4)
        val body = ByteArray(length)
        if (already > 0) {
            val copy = minOf(already, length)
            System.arraycopy(headerBytes, splitAt + 4, body, 0, copy)
            var filled = copy
            while (filled < length) {
                val n = input.read(body, filled, length - filled)
                if (n < 0) return null
                filled += n
            }
        } else if (length > 0) {
            var filled = 0
            while (filled < length) {
                val n = input.read(body, filled, length - filled)
                if (n < 0) return null
                filled += n
            }
        }
        val path = requestLine[1]
        return HttpRequest(
            method = requestLine[0],
            path = path.substringBefore('?'),
            headers = headers,
            body = String(body, StandardCharsets.UTF_8)
        )
    }

    private fun readUntil(input: BufferedInputStream, marker: ByteArray, max: Int): ByteArray? {
        val buffer = ArrayList<Byte>(1024)
        var matched = 0
        while (buffer.size < max) {
            val next = input.read()
            if (next < 0) return null
            buffer.add(next.toByte())
            if (next.toByte() == marker[matched]) {
                matched++
                if (matched == marker.size) {
                    return buffer.toByteArray()
                }
            } else {
                matched = if (next.toByte() == marker[0]) 1 else 0
            }
        }
        return null
    }

    private fun write(client: Socket, response: HttpResponse) {
        val payload = response.body.toByteArray(StandardCharsets.UTF_8)
        val head = buildString {
            append("HTTP/1.1 ").append(response.status).append(' ').append(response.reason).append("\r\n")
            append("Content-Type: ").append(response.contentType).append("\r\n")
            append("Content-Length: ").append(payload.size).append("\r\n")
            append("Connection: close\r\n")
            response.extraHeaders.forEach { (name, value) ->
                append(name).append(": ").append(value).append("\r\n")
            }
            append("\r\n")
        }
        val out = BufferedOutputStream(client.getOutputStream())
        out.write(head.toByteArray(StandardCharsets.ISO_8859_1))
        out.write(payload)
        out.flush()
    }

    private fun Socket.closeQuietly() {
        try {
            close()
        } catch (_: Exception) {
        }
    }

    companion object {
        private const val TAG = "LoopbackHttp"
        private const val MAX_HEADER_BYTES = 16 * 1024
        private const val MAX_BODY_BYTES = 1024 * 1024
        private val HEADER_END = "\r\n\r\n".toByteArray(StandardCharsets.ISO_8859_1)
    }
}

data class HttpRequest(
    val method: String,
    val path: String,
    val headers: Map<String, String>,
    val body: String
)

data class HttpResponse(
    val status: Int,
    val reason: String,
    val contentType: String,
    val body: String,
    val extraHeaders: Map<String, String> = emptyMap()
)
