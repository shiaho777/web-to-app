package com.webtoapp.core.nodejs

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NodeServiceReuseReadinessTest {

    private class CapturingHandler(looper: Looper) : Handler(looper) {
        val messages = mutableListOf<Message>()
        override fun handleMessage(msg: Message) {
            messages.add(Message.obtain(msg))
        }
    }

    private fun setField(service: NodeService, name: String, value: Any?) {
        val field = NodeService::class.java.getDeclaredField(name)
        field.isAccessible = true
        field.set(service, value)
    }

    private fun startData(projectDir: String, entry: String): Bundle = Bundle().apply {
        putString(NodeServiceProtocol.Keys.REQUEST_ID, "req-1")
        putString(NodeServiceProtocol.Keys.PROJECT_DIR, projectDir)
        putString(NodeServiceProtocol.Keys.ENTRY_FILE, entry)
    }

    private fun invokeStart(service: NodeService, data: Bundle, replyTo: Messenger?) {
        val method = NodeService::class.java.getDeclaredMethod(
            "handleStartServer", Bundle::class.java, Messenger::class.java
        )
        method.isAccessible = true
        method.invoke(service, data, replyTo)
    }

    private fun markRunning(service: NodeService, projectDir: String, entry: String, port: Int) {
        setField(service, "isRunning", true)
        setField(service, "nodeThread", Thread { Thread.sleep(60_000) }.apply { isDaemon = true; start() })
        setField(service, "currentPort", port)
        setField(service, "currentProjectDir", projectDir)
        setField(service, "currentEntryFile", entry)
    }

    @Test
    fun `reuse shortcut returns the running port for the same project and entry`() {
        val service = Robolectric.setupService(NodeService::class.java)
        markRunning(service, "/data/apps/proj-a", "index.js", 18080)
        val handler = CapturingHandler(Looper.getMainLooper())
        invokeStart(service, startData("/data/apps/proj-a", "index.js"), Messenger(handler))
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(handler.messages).hasSize(1)
        val reply = handler.messages[0]
        assertThat(reply.what).isEqualTo(NodeServiceProtocol.MSG_SERVER_STARTED)
        assertThat(reply.data.getInt(NodeServiceProtocol.Keys.ACTUAL_PORT)).isEqualTo(18080)
    }

    @Test
    fun `reuse shortcut recycles the engine when the project differs`() {
        val service = Robolectric.setupService(NodeService::class.java)
        markRunning(service, "/data/apps/proj-a", "index.js", 18080)
        val handler = CapturingHandler(Looper.getMainLooper())
        invokeStart(service, startData("/data/apps/proj-b", "index.js"), Messenger(handler))
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(handler.messages).hasSize(1)
        val reply = handler.messages[0]
        assertThat(reply.what).isEqualTo(NodeServiceProtocol.MSG_SERVER_FAILED)
        assertThat(reply.data.getBoolean(NodeServiceProtocol.Keys.V8_EXHAUSTED)).isTrue()
        val portField = NodeService::class.java.getDeclaredField("currentPort")
        portField.isAccessible = true
        assertThat(portField.get(service)).isEqualTo(0)
    }

    @Test
    fun `reuse shortcut recycles the engine when the entry differs`() {
        val service = Robolectric.setupService(NodeService::class.java)
        markRunning(service, "/data/apps/proj-a", "index.js", 18080)
        val handler = CapturingHandler(Looper.getMainLooper())
        invokeStart(service, startData("/data/apps/proj-a", "server.mjs"), Messenger(handler))
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(handler.messages).hasSize(1)
        assertThat(handler.messages[0].what).isEqualTo(NodeServiceProtocol.MSG_SERVER_FAILED)
        assertThat(handler.messages[0].data.getBoolean(NodeServiceProtocol.Keys.V8_EXHAUSTED)).isTrue()
    }

    @Test
    fun `readiness accepts a server whose root handler returns 500`() {
        val server = java.net.ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))
        val acceptor = Thread {
            while (!server.isClosed) {
                try {
                    val socket = server.accept()
                    socket.use { s ->
                        val input = s.getInputStream().bufferedReader()
                        while (true) {
                            val line = input.readLine() ?: break
                            if (line.isEmpty()) break
                        }
                        val body = "not ready yet"
                        val response = "HTTP/1.1 500 Internal Server Error\r\n" +
                            "Content-Length: ${body.length}\r\nConnection: close\r\n\r\n$body"
                        s.getOutputStream().write(response.toByteArray())
                        s.getOutputStream().flush()
                    }
                } catch (_: Exception) {
                    break
                }
            }
        }
        acceptor.isDaemon = true
        acceptor.start()
        try {
            val service = Robolectric.setupService(NodeService::class.java)
            setField(service, "nodeThread", Thread { Thread.sleep(60_000) }.apply { isDaemon = true; start() })
            val method = NodeService::class.java.getDeclaredMethod("waitForServerReady", Int::class.javaPrimitiveType)
            method.isAccessible = true
            val ready = method.invoke(service, server.localPort) as Boolean
            assertThat(ready).isTrue()
        } finally {
            server.close()
        }
    }
}