package com.webtoapp.core.golang

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GoRuntimeReadinessTest {
    @Test
    fun `an HTTP 503 proves the runtime is serving requests`() = runBlocking {
        withEndpoint("HTTP/1.1 503 Service Unavailable\r\nContent-Length: 0\r\nConnection: close\r\n\r\n") { port ->
            assertThat(withTimeoutOrNull(2_000) { runtime().waitForServerReady(port) }).isTrue()
        }
    }

    @Test
    fun `a TCP endpoint without HTTP does not count as ready`() = runBlocking {
        withEndpoint("Not HTTP\r\n") { port ->
            assertThat(withTimeoutOrNull(1_000) { runtime().waitForServerReady(port) }).isNull()
        }
    }

    private suspend fun withEndpoint(response: String, check: suspend (Int) -> Unit) {
        ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { server ->
            val responder = thread(isDaemon = true) {
                runCatching {
                    while (!server.isClosed) {
                        server.accept().use { socket ->
                            socket.soTimeout = 1_000
                            val reader = socket.getInputStream().bufferedReader()
                            while (!reader.readLine().isNullOrEmpty()) Unit
                            socket.getOutputStream().write(response.toByteArray())
                        }
                    }
                }
            }
            try {
                check(server.localPort)
            } finally {
                server.close()
                responder.join(1_000)
            }
        }
    }

    private fun runtime(): GoRuntime = GoRuntime(ApplicationProvider.getApplicationContext<Context>())
}
