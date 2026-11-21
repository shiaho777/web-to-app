package com.webtoapp.core.webview

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NativeBridgeWriteReplayTest {
    @Test
    fun `503 retry advice never replays POST or DELETE`() {
        for (method in listOf("POST", "DELETE")) {
            withServer("503 Service Unavailable\r\nRetry-After: 0") { port, requests ->
                val result = request(method, port)
                assertThat(result.getBoolean("ok")).isTrue()
                assertThat(result.getInt("status")).isEqualTo(503)
                assertThat(requests).containsExactly("$method /write HTTP/1.1")
            }
        }
    }

    @Test
    fun `307 redirect does not retransmit a write body`() {
        withServer("307 Temporary Redirect\r\nLocation: /other") { port, requests ->
            assertThat(request("POST", port).getInt("status")).isEqualTo(307)
            assertThat(requests).containsExactly("POST /write HTTP/1.1")
        }
    }

    @Test
    fun `303 can follow up with a read`() {
        withServer("303 See Other\r\nLocation: /result") { port, requests ->
            assertThat(request("POST", port).getInt("status")).isEqualTo(200)
            assertThat(requests).containsExactly("POST /write HTTP/1.1", "GET /result HTTP/1.1").inOrder()
        }
    }

    @Test
    fun `read can still honor retry advice`() {
        withServer("503 Service Unavailable\r\nRetry-After: 0") { port, requests ->
            assertThat(request("GET", port).getInt("status")).isEqualTo(200)
            assertThat(requests).containsExactly("GET /write HTTP/1.1", "GET /write HTTP/1.1")
        }
    }

    private fun request(method: String, port: Int): JSONObject {
        val bridge = NativeBridge(
            context = ApplicationProvider.getApplicationContext<Context>(),
            scope = CoroutineScope(Dispatchers.Unconfined),
            corsBypass = true,
            callerPageUrlProvider = { "http://localhost:5182/" }
        )
        val payload = JSONObject().put("url", "http://127.0.0.1:$port/write").put("method", method)
        if (method == "POST") payload.put("bodyBase64", "Zm9ybQ==")
        return JSONObject(bridge.httpRequest(payload.toString()))
    }

    private fun withServer(firstReply: String, verify: (Int, List<String>) -> Unit) {
        ServerSocket(0, 4, InetAddress.getByName("127.0.0.1")).use { server ->
            val requests = CopyOnWriteArrayList<String>()
            val responder = thread(isDaemon = true) {
                while (!server.isClosed) {
                    try {
                        server.accept().use { socket ->
                            socket.soTimeout = 2_000
                            val reader = socket.getInputStream().bufferedReader()
                            val requestLine = reader.readLine() ?: return@use
                            var contentLength = 0
                            while (true) {
                                val header = reader.readLine()?.takeIf { it.isNotEmpty() } ?: break
                                if (header.startsWith("Content-Length:", ignoreCase = true)) {
                                    contentLength = header.substringAfter(':').trim().toInt()
                                }
                            }
                            repeat(contentLength) { reader.read() }
                            requests.add(requestLine)
                            val reply = if (requests.size == 1) firstReply else "200 OK"
                            socket.getOutputStream().write("HTTP/1.1 $reply\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray())
                        }
                    } catch (_: IOException) {
                        if (server.isClosed) break
                    }
                }
            }
            try {
                verify(server.localPort, requests)
            } finally {
                server.close()
                responder.join(3_000)
            }
        }
    }
}
