package com.webtoapp.core.webview

import android.webkit.CookieManager
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

/**
 * LegacyProxyFetchBridge: proves subresource traffic actually traverses the
 * configured proxy on the fallback path (pre-PROXY_OVERRIDE WebViews).
 */
@RunWith(RobolectricTestRunner::class)
class LegacyProxyFetchBridgeTest {

    private var origin: ServerSocket? = null
    private var originPort: Int = 0
    private var originThread: Thread? = null

    private var proxy: ServerSocket? = null
    private var proxyPort: Int = 0
    private var proxyThread: Thread? = null
    private val lastProxyRequest = AtomicReference<ProxyCapture?>()
    @Volatile
    private var challengeAuth = false

    data class ProxyCapture(
        val method: String,
        val uri: String,
        val headers: Map<String, String>,
        val body: ByteArray
    )

    @Before
    fun setUp() {
        LegacyProxyFetchBridge.clear()

        origin = ServerSocket().also {
            it.bind(InetSocketAddress("127.0.0.1", 0))
            originPort = it.localPort
        }
        originThread = thread(isDaemon = true, name = "test-origin") {
            try {
                while (origin?.isClosed == false) {
                    val sock = origin!!.accept()
                    try {
                        handleOriginConnection(sock)
                    } catch (_: Exception) {
                    } finally {
                        try {
                            sock.close()
                        } catch (_: Exception) {
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        proxy = ServerSocket().also {
            it.bind(InetSocketAddress("127.0.0.1", 0))
            proxyPort = it.localPort
        }
        proxyThread = thread(isDaemon = true, name = "test-forward-proxy") {
            try {
                while (proxy?.isClosed == false) {
                    val sock = proxy!!.accept()
                    try {
                        handleProxyConnection(sock)
                    } catch (_: Exception) {
                    } finally {
                        try {
                            sock.close()
                        } catch (_: Exception) {
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    @After
    fun tearDown() {
        LegacyProxyFetchBridge.clear()
        try {
            proxy?.close()
        } catch (_: Exception) {
        }
        try {
            origin?.close()
        } catch (_: Exception) {
        }
        proxyThread?.join(2000)
        originThread?.join(2000)
    }

    private fun readHttpMessage(input: java.io.InputStream): Triple<String, Map<String, String>, ByteArray> {
        val head = ByteArrayOutputStream()
        var matched = 0
        val crlfcrlf = byteArrayOf(13, 10, 13, 10)
        while (true) {
            val b = input.read()
            if (b < 0) break
            head.write(b)
            matched = if (b == crlfcrlf[matched].toInt()) matched + 1 else 0
            if (matched == 4) break
            if (head.size() > 65536) break
        }
        val headText = head.toString(StandardCharsets.ISO_8859_1.name())
        val lines = headText.split("\r\n")
        val headers = LinkedHashMap<String, String>()
        for (line in lines.drop(1)) {
            if (line.isEmpty()) continue
            val idx = line.indexOf(':')
            if (idx > 0) headers[line.substring(0, idx).trim().lowercase()] = line.substring(idx + 1).trim()
        }
        val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
        val body = if (contentLength > 0) input.readNBytes(contentLength) else ByteArray(0)
        return Triple(lines.firstOrNull() ?: "", headers, body)
    }

    private fun handleOriginConnection(sock: Socket) {
        sock.soTimeout = 15000
        val (requestLine, headers, body) = readHttpMessage(sock.getInputStream())
        val parts = requestLine.split(" ")
        val method = parts.getOrElse(0) { "" }
        val path = parts.getOrElse(1) { "/" }.substringBefore("?")
        val (statusExtra, respBody) = when (path) {
            "/hello" -> listOf("X-Test: yes", "Set-Cookie: sess=abc123; Path=/") to "hello-world".toByteArray()
            "/echo-cookie" -> emptyList<String>() to (headers["cookie"] ?: "none").toByteArray()
            "/post" -> emptyList<String>() to "method=$method len=${body.size}".toByteArray()
            else -> emptyList<String>() to "not-found".toByteArray()
        }
        val status = if (path == "/hello" || path == "/echo-cookie" || path == "/post") 200 else 404
        val out = StringBuilder()
        out.append("HTTP/1.1 $status ${if (status == 200) "OK" else "Not Found"}\r\n")
        out.append("Content-Type: text/plain\r\n")
        for (h in statusExtra) out.append("$h\r\n")
        out.append("Content-Length: ${respBody.size}\r\nConnection: close\r\n\r\n")
        sock.getOutputStream().write(out.toString().toByteArray(StandardCharsets.ISO_8859_1))
        sock.getOutputStream().write(respBody)
        sock.getOutputStream().flush()
    }

    private fun handleProxyConnection(sock: Socket) {
        sock.soTimeout = 15000
        val input = sock.getInputStream()
        val (requestLine, headers, body) = readHttpMessage(input)
        val parts = requestLine.split(" ")
        val method = parts.getOrElse(0) { "" }
        val uri = parts.getOrElse(1) { "" }
        lastProxyRequest.set(ProxyCapture(method, uri, headers, body))

        if (challengeAuth && !headers.containsKey("proxy-authorization")) {
            // Real proxies challenge once with 407; OkHttp answers on retry.
            val challenge = "HTTP/1.1 407 Proxy Authentication Required\r\n" +
                "Proxy-Authenticate: Basic realm=\"test\"\r\n" +
                "Content-Length: 0\r\nConnection: close\r\n\r\n"
            sock.getOutputStream().write(challenge.toByteArray(StandardCharsets.ISO_8859_1))
            sock.getOutputStream().flush()
            return
        }

        // Forward absolute-form URI to the origin over a raw socket, relay raw bytes.
        // Pseudo-host test.local routes to the loopback origin (which itself is
        // always bypassed, so e2e tests address it through this alias).
        val target = java.net.URI(uri)
        val relayHost = if (target.host == "test.local") "127.0.0.1" else target.host
        val targetPort = if (target.port > 0) target.port else 80
        val path = (target.rawPath?.ifEmpty { "/" } ?: "/") +
            (target.rawQuery?.let { "?$it" } ?: "")
        Socket().use { upstream ->
            upstream.connect(InetSocketAddress(relayHost, targetPort), 10000)
            upstream.soTimeout = 15000
            val out = StringBuilder()
            out.append("$method $path HTTP/1.1\r\n")
            out.append("Host: ${target.host}\r\n")
            for ((k, v) in headers) {
                if (k == "proxy-authorization" || k == "proxy-connection") continue
                out.append("$k: $v\r\n")
            }
            out.append("Connection: close\r\n\r\n")
            upstream.getOutputStream().write(out.toString().toByteArray(StandardCharsets.ISO_8859_1))
            if (body.isNotEmpty()) upstream.getOutputStream().write(body)
            upstream.getOutputStream().flush()
            upstream.shutdownOutput()
            upstream.getInputStream().copyTo(sock.getOutputStream())
        }
    }

    private fun armHttp(
        bypass: List<String> = emptyList(),
        username: String = "",
        password: String = ""
    ) {
        LegacyProxyFetchBridge.configure(
            LegacyProxyFetchBridge.Spec(
                kind = LegacyProxyFetchBridge.Kind.HTTP,
                host = "127.0.0.1",
                port = proxyPort,
                username = username,
                password = password,
                bypassRules = bypass
            )
        )
    }

    private fun originUrl(path: String) = "http://127.0.0.1:$originPort$path"

    /** E2E tests address the loopback origin through this alias (see proxy relay). */
    private fun proxyTestUrl(path: String) = "http://test.local:$originPort$path"

    @Test(timeout = 30000)
    fun `disarmed bridge returns null`() {
        assertThat(LegacyProxyFetchBridge.intercept(originUrl("/hello"), "GET", emptyMap())).isNull()
    }

    @Test(timeout = 30000)
    fun `bypassed loopback target returns null even when armed`() {
        armHttp()
        // Origin itself is loopback: always bypassed, never proxied.
        assertThat(LegacyProxyFetchBridge.intercept(originUrl("/hello"), "GET", emptyMap())).isNull()
        assertThat(lastProxyRequest.get()).isNull()
    }

    @Test(timeout = 30000)
    fun `non-http scheme returns null`() {
        armHttp()
        assertThat(LegacyProxyFetchBridge.intercept("file:///android_asset/x.html", "GET", emptyMap())).isNull()
    }

    @Test(timeout = 30000)
    fun `custom bypass rules match exact and suffix`() {
        assertThat(LegacyProxyFetchBridge.isBypassed("example.com", listOf("example.com"))).isTrue()
        assertThat(LegacyProxyFetchBridge.isBypassed("a.example.com", listOf(".example.com"))).isTrue()
        assertThat(LegacyProxyFetchBridge.isBypassed("a.example.com", listOf("*.example.com"))).isTrue()
        assertThat(LegacyProxyFetchBridge.isBypassed("other.com", listOf(".example.com"))).isFalse()
        assertThat(LegacyProxyFetchBridge.isBypassed("anything.io", listOf("*"))).isTrue()
        assertThat(LegacyProxyFetchBridge.isBypassed("127.0.0.1", emptyList())).isTrue()
        assertThat(LegacyProxyFetchBridge.isBypassed(null, emptyList())).isTrue()
    }

    @Test(timeout = 30000)
    fun `static resolve picks kind and rejects bad input`() {
        val http = LegacyProxyFetchBridge.resolveStatic("STATIC", "p.example.com", 8080, "HTTP", emptyList(), "", "")
        assertThat(http?.kind).isEqualTo(LegacyProxyFetchBridge.Kind.HTTP)
        val socks = LegacyProxyFetchBridge.resolveStatic("STATIC", "p.example.com", 1080, "SOCKS5", emptyList(), "", "")
        assertThat(socks?.kind).isEqualTo(LegacyProxyFetchBridge.Kind.SOCKS)
        val broken = LegacyProxyFetchBridge.resolveStatic("STATIC", "p.example.com", 443, "HTTPS", emptyList(), "", "")
        assertThat(broken?.kind).isEqualTo(LegacyProxyFetchBridge.Kind.BROKEN)
        assertThat(LegacyProxyFetchBridge.resolveStatic("NONE", "", 0, "HTTP", emptyList(), "", "")).isNull()
        assertThat(LegacyProxyFetchBridge.resolveStatic("STATIC", "", 0, "HTTP", emptyList(), "", "")).isNull()
    }

    @Test(timeout = 30000)
    fun `unreachable proxy fail-closes with 502`() {
        // Bind then release a loopback port: connect refused immediately, no real DNS.
        val probe = ServerSocket(0)
        val closedPort = probe.localPort
        probe.close()
        LegacyProxyFetchBridge.configure(
            LegacyProxyFetchBridge.Spec(
                kind = LegacyProxyFetchBridge.Kind.HTTP,
                host = "127.0.0.1",
                port = closedPort,
                bypassRules = emptyList()
            )
        )
        val resp = LegacyProxyFetchBridge.intercept("http://example.com/", "GET", emptyMap())
        assertThat(resp).isNotNull()
        assertThat(resp!!.statusCode).isEqualTo(502)
    }

    @Test(timeout = 30000)
    fun `pac resolve picks first usable entry`() {
        // Entries arrive pre-split on ';' (same contract as PacProxyManager).
        val spec = LegacyProxyFetchBridge.resolvePacEntries(
            listOf("PROXY p1.example.com:8080", "PROXY p2.example.com:8080"),
            emptyList(), "", ""
        )
        assertThat(spec?.kind).isEqualTo(LegacyProxyFetchBridge.Kind.HTTP)
        assertThat(spec?.host).isEqualTo("p1.example.com")
        assertThat(spec?.port).isEqualTo(8080)
        assertThat(
            LegacyProxyFetchBridge.resolvePacEntries(listOf("DIRECT"), emptyList(), "", "")
        ).isNull()
        assertThat(
            LegacyProxyFetchBridge.resolvePacEntries(listOf("SOCKS5 s.example.com:1080"), emptyList(), "", "")?.kind
        ).isEqualTo(LegacyProxyFetchBridge.Kind.SOCKS)
    }

    @Test(timeout = 30000)
    fun `broken kind fail-closes with 502`() {
        LegacyProxyFetchBridge.configure(
            LegacyProxyFetchBridge.Spec(kind = LegacyProxyFetchBridge.Kind.BROKEN, bypassRules = emptyList())
        )
        val resp = LegacyProxyFetchBridge.intercept("http://example.com/", "GET", emptyMap())
        assertThat(resp).isNotNull()
        assertThat(resp!!.statusCode).isEqualTo(502)
    }

    @Test(timeout = 30000)
    fun `end to end through proxy maps body headers and cookies`() {
        // Bypass only a sentinel host so the origin alias is fetched VIA proxy.
        armHttp(bypass = listOf("bypassed.invalid"))
        val resp = LegacyProxyFetchBridge.intercept(proxyTestUrl("/hello"), "GET", emptyMap())
        assertThat(resp).isNotNull()
        val capture = lastProxyRequest.get()
        assertThat(capture).isNotNull()
        checkNotNull(capture)
        assertThat(capture.method).isEqualTo("GET")
        assertThat(capture.uri).isEqualTo(proxyTestUrl("/hello"))
        assertThat(resp!!.statusCode).isEqualTo(200)
        assertThat(resp.responseHeaders["X-Test"]).isEqualTo("yes")
        assertThat(resp.mimeType).isEqualTo("text/plain")
        val body = resp.data?.bufferedReader()?.use { it.readText() }
        assertThat(body).isEqualTo("hello-world")

        // Set-Cookie bridged into the WebView store...
        val stored = try {
            CookieManager.getInstance().getCookie(proxyTestUrl("/hello"))
        } catch (_: Exception) {
            null
        }
        assertThat(stored).contains("sess=abc123")

        // ...and sent back on the next hop.
        lastProxyRequest.set(null)
        val resp2 = LegacyProxyFetchBridge.intercept(proxyTestUrl("/echo-cookie"), "GET", emptyMap())
        assertThat(resp2).isNotNull()
        val body2 = resp2!!.data?.bufferedReader()?.use { it.readText() }
        assertThat(body2).contains("sess=abc123")
        assertThat(lastProxyRequest.get()?.headers?.get("cookie")).contains("sess=abc123")
    }

    @Test(timeout = 30000)
    fun `proxy auth header is sent and post goes without body`() {
        challengeAuth = true
        try {
            armHttp(bypass = listOf("bypassed.invalid"), username = "alice", password = "s3cret")
            val resp = LegacyProxyFetchBridge.intercept(proxyTestUrl("/post"), "POST", mapOf("X-Custom" to "1"))
            assertThat(resp).isNotNull()
            val capture = lastProxyRequest.get()
            assertThat(capture).isNotNull()
            checkNotNull(capture)
            assertThat(capture.method).isEqualTo("POST")
            assertThat(capture.body.size).isEqualTo(0)
            assertThat(capture.headers["proxy-authorization"]).startsWith("Basic ")
            assertThat(capture.headers["x-custom"]).isEqualTo("1")
            val body = resp!!.data?.bufferedReader()?.use { it.readText() }
            assertThat(body).isEqualTo("method=POST len=0")
        } finally {
            challengeAuth = false
        }
    }

    @Test(timeout = 30000)
    fun `splitHostPort parses and rejects`() {
        assertThat(LegacyProxyFetchBridge.splitHostPort("p.example.com:8080"))
            .isEqualTo(Pair("p.example.com", 8080))
        assertThat(LegacyProxyFetchBridge.splitHostPort("[::1]:1080"))
            .isEqualTo(Pair("::1", 1080))
        assertThat(LegacyProxyFetchBridge.splitHostPort("noport")).isNull()
        assertThat(LegacyProxyFetchBridge.splitHostPort("h:0")).isNull()
    }
}
