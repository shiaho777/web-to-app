package com.webtoapp.core.python

import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PythonBootstrapExecutionTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private fun pythonBinary(): String? {
        System.getenv("PYTHON_BINARY")?.takeIf { it.isNotBlank() }?.let { return it }
        return listOf("/usr/bin/python3", "/usr/local/bin/python3", "/opt/homebrew/bin/python3")
            .firstOrNull { File(it).canExecute() }
    }

    private fun freePort(): Int = ServerSocket(0).use { it.localPort }

    private fun runEntry(entrySource: String, port: Int): Pair<Process, File> {
        val python = pythonBinary()
        assumeTrue("python3 executable not available (set PYTHON_BINARY)", python != null)
        val dir = File(context.cacheDir, "pyboot-test-${System.nanoTime()}").apply { mkdirs() }
        PythonRuntime(context).createBootstrapScript(dir, port)
        File(dir, "app.py").writeText(entrySource)
        val proc = ProcessBuilder(python!!, "_w2a_bootstrap.py", "app.py", port.toString())
            .directory(dir)
            .redirectErrorStream(true)
            .start()
        return proc to dir
    }

    private fun httpStatus(port: Int, path: String = "/"): Int? {
        return try {
            val conn = URL("http://127.0.0.1:$port$path").openConnection() as HttpURLConnection
            conn.connectTimeout = 500
            conn.readTimeout = 1000
            conn.requestMethod = "GET"
            val code = conn.responseCode
            conn.disconnect()
            code
        } catch (_: Exception) {
            null
        }
    }

    private fun awaitListening(port: Int, timeoutMs: Long = 15_000): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (httpStatus(port) != null) return true
            Thread.sleep(150)
        }
        return false
    }

    private fun cleanup(proc: Process, dir: File) {
        proc.destroyForcibly()
        proc.waitFor(5, TimeUnit.SECONDS)
        dir.deleteRecursively()
    }

    @Test
    fun `plain TCPServer serving HTTP uses the allocated port and leaves other TCP endpoints alone`() {
        val port = freePort()
        val controlPort = generateSequence(::freePort).first { it != port }
        val (proc, dir) = runEntry(
            """
            from http.server import BaseHTTPRequestHandler
            from socketserver import BaseRequestHandler, TCPServer
            class H(BaseHTTPRequestHandler):
                def do_GET(self):
                    self.send_response(200); self.end_headers(); self.wfile.write(b'ok')
                def log_message(self, *a): pass
            with TCPServer(('127.0.0.1', $controlPort), BaseRequestHandler) as control:
                assert control.server_address[1] == $controlPort
                TCPServer(('127.0.0.1', 8123), H).serve_forever()
            """.trimIndent(),
            port
        )
        try {
            assertThat(awaitListening(port)).isTrue()
            assertThat(httpStatus(port)).isEqualTo(200)
        } finally {
            cleanup(proc, dir)
        }
    }

    @Test
    fun `multi server entry binds allocated port first and a second port after`() {
        val port = freePort()
        val marker = File(context.cacheDir, "pyboot-second-${System.nanoTime()}.txt")
        val (proc, dir) = runEntry(
            """
            import threading
            from http.server import BaseHTTPRequestHandler, HTTPServer
            class H(BaseHTTPRequestHandler):
                def do_GET(self):
                    self.send_response(200); self.end_headers(); self.wfile.write(b'ok')
                def log_message(self, *a): pass
            first = HTTPServer(('127.0.0.1', 8123), H)
            second = HTTPServer(('127.0.0.1', 8234), H)
            with open(${pythonLiteral(marker.absolutePath)}, 'w') as f:
                f.write(str(first.server_address[1]) + ',' + str(second.server_address[1]))
            threading.Thread(target=second.serve_forever, daemon=True).start()
            first.serve_forever()
            """.trimIndent(),
            port
        )
        try {
            assertThat(awaitListening(port)).isTrue()
            val deadline = System.currentTimeMillis() + 10_000
            while (!marker.exists() && System.currentTimeMillis() < deadline) Thread.sleep(100)
            assertThat(marker.exists()).isTrue()
            val ports = marker.readText().trim().split(",").map { it.toInt() }
            assertThat(ports[0]).isEqualTo(port)
            assertThat(ports[1]).isEqualTo(8234)
            assertThat(httpStatus(8234)).isEqualTo(200)
        } finally {
            marker.delete()
            cleanup(proc, dir)
        }
    }

    @Test
    fun `ephemeral port zero bind is not hijacked`() {
        val port = freePort()
        val marker = File(context.cacheDir, "pyboot-ephemeral-${System.nanoTime()}.txt")
        val (proc, dir) = runEntry(
            """
            import socket
            s = socket.socket()
            s.bind(('127.0.0.1', 0))
            with open(${pythonLiteral(marker.absolutePath)}, 'w') as f:
                f.write(str(s.getsockname()[1]))
            s.close()
            from http.server import BaseHTTPRequestHandler, HTTPServer
            class H(BaseHTTPRequestHandler):
                def do_GET(self):
                    self.send_response(200); self.end_headers(); self.wfile.write(b'ok')
                def log_message(self, *a): pass
            HTTPServer(('127.0.0.1', 8123), H).serve_forever()
            """.trimIndent(),
            port
        )
        try {
            assertThat(awaitListening(port)).isTrue()
            val deadline = System.currentTimeMillis() + 10_000
            while (!marker.exists() && System.currentTimeMillis() < deadline) Thread.sleep(100)
            assertThat(marker.exists()).isTrue()
            val ephemeral = marker.readText().trim().toInt()
            assertThat(ephemeral).isGreaterThan(0)
            assertThat(ephemeral).isNotEqualTo(port)
        } finally {
            marker.delete()
            cleanup(proc, dir)
        }
    }

    @Test
    fun `udp server keeps its own port`() {
        val port = freePort()
        val marker = File(context.cacheDir, "pyboot-udp-${System.nanoTime()}.txt")
        val (proc, dir) = runEntry(
            """
            import socketserver, threading
            class U(socketserver.UDPServer):
                pass
            udp = U(('127.0.0.1', 8345), socketserver.BaseRequestHandler)
            with open(${pythonLiteral(marker.absolutePath)}, 'w') as f:
                f.write(str(udp.server_address[1]))
            threading.Thread(target=udp.serve_forever, daemon=True).start()
            from http.server import BaseHTTPRequestHandler, HTTPServer
            class H(BaseHTTPRequestHandler):
                def do_GET(self):
                    self.send_response(200); self.end_headers(); self.wfile.write(b'ok')
                def log_message(self, *a): pass
            HTTPServer(('127.0.0.1', 8123), H).serve_forever()
            """.trimIndent(),
            port
        )
        try {
            assertThat(awaitListening(port)).isTrue()
            val deadline = System.currentTimeMillis() + 10_000
            while (!marker.exists() && System.currentTimeMillis() < deadline) Thread.sleep(100)
            assertThat(marker.exists()).isTrue()
            assertThat(marker.readText().trim().toInt()).isEqualTo(8345)
        } finally {
            marker.delete()
            cleanup(proc, dir)
        }
    }

    private fun pythonLiteral(value: String): String = "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'"
}
