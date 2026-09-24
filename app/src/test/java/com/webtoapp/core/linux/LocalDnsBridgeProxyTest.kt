package com.webtoapp.core.linux

import com.google.common.truth.Truth.assertThat
import java.net.Socket
import java.net.URI
import java.nio.charset.StandardCharsets
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Drives the real proxy over a loopback socket: 407 proves the request was
 * rejected at the auth gate, while 502 proves it passed auth and failed later
 * inside the (deliberately unreachable) target connect.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocalDnsBridgeProxyTest {

    @After
    fun tearDown() {
        LocalDnsBridgeProxy.stop()
    }

    @Test
    fun `proxyEnvFor embeds session token as userinfo`() {
        val port = LocalDnsBridgeProxy.start()
        assertThat(port).isGreaterThan(0)

        val env = LocalDnsBridgeProxy.proxyEnvFor(port)
        val url = env.getValue("http_proxy")
        val userInfo = URI(url).userInfo
        assertThat(userInfo).isNotNull()
        assertThat(userInfo.substringBefore(':')).isEqualTo("wta")
        assertThat(userInfo.substringAfter(':')).hasLength(32)
        // Same credential surface on every key a runtime might read.
        assertThat(env.getValue("https_proxy")).isEqualTo(url)
        assertThat(env.getValue("npm_config_proxy")).isEqualTo(url)
        assertThat(env.getValue("no_proxy")).contains("127.0.0.1")
    }

    @Test
    fun `CONNECT without credentials is rejected with 407`() {
        val port = LocalDnsBridgeProxy.start()
        val status = exchange(port, "CONNECT 127.0.0.1:1 HTTP/1.1\r\nHost: 127.0.0.1:1\r\n\r\n")
        assertThat(status).startsWith("HTTP/1.1 407")
    }

    @Test
    fun `HTTP forward without credentials is rejected with 407`() {
        val port = LocalDnsBridgeProxy.start()
        val status = exchange(
            port,
            "GET http://127.0.0.1:1/x HTTP/1.1\r\nHost: 127.0.0.1:1\r\n\r\n"
        )
        assertThat(status).startsWith("HTTP/1.1 407")
    }

    @Test
    fun `wrong token is rejected with 407`() {
        val port = LocalDnsBridgeProxy.start()
        val status = exchange(
            port,
            "CONNECT 127.0.0.1:1 HTTP/1.1\r\n" +
                "Host: 127.0.0.1:1\r\n" +
                "Proxy-Authorization: Basic ${basic("wta:deadbeefdeadbeefdeadbeefdeadbeef")}\r\n\r\n"
        )
        assertThat(status).startsWith("HTTP/1.1 407")
    }

    @Test
    fun `valid token passes the auth gate for CONNECT`() {
        val port = LocalDnsBridgeProxy.start()
        val status = exchange(
            port,
            "CONNECT 127.0.0.1:1 HTTP/1.1\r\n" +
                "Host: 127.0.0.1:1\r\n" +
                "Proxy-Authorization: Basic ${basic("wta:${sessionToken()}")}\r\n\r\n"
        )
        // Nothing listens on port 1 → auth passed, target connect failed.
        assertThat(status).startsWith("HTTP/1.1 502")
    }

    @Test
    fun `valid token passes the auth gate for HTTP forward`() {
        val port = LocalDnsBridgeProxy.start()
        val status = exchange(
            port,
            "GET http://127.0.0.1:1/x HTTP/1.1\r\n" +
                "Host: 127.0.0.1:1\r\n" +
                "Proxy-Authorization: Basic ${basic("wta:${sessionToken()}")}\r\n\r\n"
        )
        assertThat(status).startsWith("HTTP/1.1 502")
    }

    @Test
    fun `restart rotates the token`() {
        LocalDnsBridgeProxy.start()
        val first = sessionToken()
        LocalDnsBridgeProxy.stop()

        LocalDnsBridgeProxy.start()
        val second = sessionToken()
        assertThat(second).isNotEqualTo(first)
    }

    private fun sessionToken(): String {
        val url = LocalDnsBridgeProxy.proxyEnvFor(LocalDnsBridgeProxy.getListenPort())
            .getValue("http_proxy")
        return URI(url).userInfo.substringAfter(':')
    }

    private fun basic(credential: String): String =
        java.util.Base64.getEncoder()
            .encodeToString(credential.toByteArray(StandardCharsets.UTF_8))

    /** Sends [request] and returns the first status/response line. */
    private fun exchange(port: Int, request: String): String {
        Socket("127.0.0.1", port).use { socket ->
            socket.soTimeout = 15_000
            socket.getOutputStream().apply {
                write(request.toByteArray(StandardCharsets.US_ASCII))
                flush()
            }
            val input = socket.getInputStream()
            val line = StringBuilder()
            while (true) {
                val next = input.read()
                if (next < 0 || next == '\n'.code) break
                if (next != '\r'.code) line.append(next.toChar())
            }
            return line.toString()
        }
    }
}
