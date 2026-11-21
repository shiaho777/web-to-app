package com.webtoapp.core.python

import android.content.Context
import com.google.common.truth.Truth.assertThat
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PythonRuntimeTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private fun startHttp404Server(): Pair<ServerSocket, Thread> {
        val server = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
        val acceptor = thread(isDaemon = true) {
            while (!server.isClosed) {
                try {
                    val client = server.accept()
                    client.use { c ->
                        BufferedReader(InputStreamReader(c.getInputStream())).readLine()
                        c.getOutputStream().apply {
                            write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray())
                            flush()
                        }
                    }
                } catch (_: Exception) {
                    return@thread
                }
            }
        }
        return server to acceptor
    }

    @Test
    fun `isPortBound reflects the listening state`() {
        val runtime = PythonRuntime(context)
        assertThat(runtime.isPortBound(-1)).isFalse()

        val server = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
        val port = server.localPort
        try {
            assertThat(runtime.isPortBound(port)).isTrue()
        } finally {
            server.close()
        }
        assertThat(runtime.isPortBound(port)).isFalse()
    }

    @Test
    fun `waitForServerReady passes against a plain HTTP server`() {
        val (server, acceptor) = startHttp404Server()
        try {
            val runtime = PythonRuntime(context)
            val budget = ReadinessBudget(baseMs = 5_000L, hardCapMs = 15_000L)
            val ready = runBlocking { runtime.waitForServerReady(server.localPort, "flask", budget) }
            assertThat(ready).isTrue()
        } finally {
            server.close()
            acceptor.join(1_000)
        }
    }

    @Test
    fun `waitForServerReady treats a stable bound socket as ready`() {
        val server = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
        val acceptor = thread(isDaemon = true) {
            while (!server.isClosed) {
                try {
                    server.accept().close()
                } catch (_: Exception) {
                    return@thread
                }
            }
        }
        try {
            val runtime = PythonRuntime(context)
            val budget = ReadinessBudget(baseMs = 10_000L, hardCapMs = 30_000L)
            val ready = runBlocking { runtime.waitForServerReady(server.localPort, "flask", budget) }
            assertThat(ready).isTrue()
        } finally {
            server.close()
            acceptor.join(1_000)
        }
    }

    @Test
    fun `waitForServerReady expires when nothing listens`() {
        val runtime = PythonRuntime(context)
        val server = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
        val port = server.localPort
        server.close()

        val budget = ReadinessBudget(baseMs = 1_000L, hardCapMs = 3_000L)
        val ready = runBlocking { runtime.waitForServerReady(port, "flask", budget) }
        assertThat(ready).isFalse()
    }

    @Test
    fun `bootstrap script forces the allocated port beyond Flask`() {
        val runtime = PythonRuntime(context)
        val dir = File(context.cacheDir, "bootstrap-test-${System.nanoTime()}").apply { mkdirs() }
        try {
            runtime.createBootstrapScript(dir, 19527)
            val script = File(dir, "_w2a_bootstrap.py").readText()

            assertThat(script).contains("flask.Flask.run")
            assertThat(script).contains("__w2a_health")

            assertThat(script).contains("TCPServer.server_bind")
            assertThat(script).contains("_w2a_port")
            assertThat(script).contains("os.environ['PORT'] = str(_w2a_port)")

            assertThat(script).contains("HTTPServer.listen")

            assertThat(script).contains("'__name__': '__main__'")
        } finally {
            dir.deleteRecursively()
        }
    }
}
