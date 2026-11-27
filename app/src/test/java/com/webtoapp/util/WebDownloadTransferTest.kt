package com.webtoapp.util

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.security.KeyStore
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

class WebDownloadTransferTest {
    private val data = ByteArray(384 * 1024) { (it * 31 xor (it ushr 8)).toByte() }
    private val prefix = 64 * 1024
    private val tag = "\"fixture\""

    @get:Rule
    val temporary = TemporaryFolder()

    @Test
    fun `healthy response is transferred in one request`() {
        withServer({ _, _, socket -> send(socket, "200 OK", data) }) { url, requests ->
            assertThat(transfer().open(url, "Test agent", null).use { it.readBytes() }).isEqualTo(data)
            assertThat(requests.single().headers["user-agent"]).isEqualTo("Test agent")
            assertThat(requests.single().headers).doesNotContainKey("range")
        }
    }

    @Test
    fun `connection lost before headers retries without claiming bytes`() {
        withServer({ index, _, socket ->
            if (index == 0) socket.shutdownOutput() else send(socket, "200 OK", data)
        }) { url, requests ->
            assertThat(transfer().open(url, "", null).use { it.readBytes() }).isEqualTo(data)
            assertThat(requests).hasSize(2)
            assertThat(requests.last().headers).doesNotContainKey("range")
        }
    }

    @Test
    fun `a truncated body resumes the same representation exactly where it stopped`() {
        withServer({ index, request, socket ->
            if (index == 0) send(socket, "200 OK", data.copyOf(prefix), length = data.size)
            else sendRange(socket, request)
        }) { url, requests ->
            val payload = transfer().open(url, "Test agent", null).use { it.readBytes() }
            assertThat(payload).isEqualTo(data)
            assertThat(requests.map { it.headers["range"] }).containsExactly(null, "bytes=$prefix-").inOrder()
            assertThat(requests.last().headers["if-range"]).isEqualTo(tag)
            assertThat(requests.last().headers["accept-encoding"]).isEqualTo("identity")
        }
    }

    @Test
    fun `a read timeout resumes from the last delivered byte`() {
        val log = mutableListOf<String>()
        val client = OkHttpClient.Builder().readTimeout(300, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false).followRedirects(false).build()
        withServer({ index, request, socket ->
            if (index == 0) {
                send(socket, "200 OK", data.copyOf(prefix), length = data.size)
                Thread.sleep(900)
            } else sendRange(socket, request)
        }) { url, requests ->
            val engine = WebDownloadTransfer(client = client, log = { log.add(it) }, pause = {})
            assertThat(engine.open(url, "", null).use { it.readBytes() }).isEqualTo(data)
            assertThat(requests.map { it.headers["range"] }).containsExactly(null, "bytes=$prefix-").inOrder()
            assertThat(log.any { it.contains("reason=SocketTimeoutException") }).isTrue()
        }
    }

    @Test
    fun `range rejection never appends a changed representation`() {
        val invalidReplies: List<(Socket) -> Unit> = listOf(
            { send(it, "200 OK", data) },
            { send(it, "206 Partial Content", data.copyOfRange(prefix, data.size),
                etag = "\"changed\"", range = "bytes $prefix-${data.lastIndex}/${data.size}") },
            { send(it, "206 Partial Content", data.copyOfRange(prefix, data.size),
                range = "bytes ${prefix - 1}-${data.lastIndex}/${data.size}") },
            { send(it, "206 Partial Content", data.copyOfRange(prefix, data.size),
                range = "bytes $prefix-${data.lastIndex}/${data.size + 1}") }
        )
        for (reply in invalidReplies) {
            withServer({ index, _, socket ->
                if (index == 0) send(socket, "200 OK", data.copyOf(prefix), length = data.size)
                else reply(socket)
            }) { url, requests ->
                val copied = ByteArrayOutputStream()
                assertThrows(IOException::class.java) {
                    transfer().open(url, "", null).use { it.copyTo(copied) }
                }
                assertThat(copied.toByteArray()).isEqualTo(data.copyOf(prefix))
                assertThat(requests).hasSize(2)
            }
        }
    }

    @Test
    fun `weak or missing tags never allow partial responses to be stitched`() {
        for (validator in listOf(null, "W/$tag")) {
            withServer({ _, _, socket -> send(socket, "200 OK", data.copyOf(prefix), length = data.size, etag = validator) }) { url, requests ->
                assertThrows(IOException::class.java) { transfer().open(url, "", null).use { it.readBytes() } }
                assertThat(requests).hasSize(1)
            }
        }
    }

    @Test
    fun `recovery budget is shared across repeated body failures`() {
        withServer({ index, request, socket ->
            if (index == 0) send(socket, "200 OK", data.copyOf(prefix), length = data.size)
            else sendRange(socket, request, maxBytes = prefix)
        }) { url, requests ->
            val copied = ByteArrayOutputStream()
            assertThrows(IOException::class.java) { transfer().open(url, "", null).use { it.copyTo(copied) } }
            assertThat(copied.toByteArray()).isEqualTo(data.copyOf(3 * prefix))
            assertThat(requests.map { it.headers["range"] }).containsExactly(null, "bytes=$prefix-", "bytes=${2 * prefix}-").inOrder()
        }
    }

    @Test
    fun `redirects look up destination cookies and use the corresponding referrer`() {
        withServer({ _, _, socket -> send(socket, "200 OK", data) }) { target, targetRequests ->
            withServer({ _, _, socket ->
                socket.getOutputStream().write("HTTP/1.1 302 Found\r\nLocation: $target\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray())
            }) { original, sourceRequests ->
                val lookup: (HttpUrl) -> String? = { "port=${it.port}" }
                assertThat(transfer(lookup).open(original, "Test agent", "$original/private?q=internal")
                    .use { it.readBytes() }).isEqualTo(data)
                assertThat(sourceRequests.single().headers["cookie"]).isEqualTo("port=${original.substringAfterLast(':')}")
                assertThat(sourceRequests.single().headers["referer"]).isEqualTo("$original/private?q=internal")
                assertThat(targetRequests.single().headers["cookie"]).isEqualTo("port=${target.substringAfterLast(':')}")
                assertThat(targetRequests.single().headers["referer"]).isEqualTo("$original/")
            }
        }
    }

    @Test
    fun `slow HTTPS combines concurrent ranges without skipping or repeating bytes`() {
        val (server, client) = tls()
        val logs = CopyOnWriteArrayList<String>()
        val concurrency = CountDownLatch(4)
        withServer({ index, request, socket ->
            if (index == 0) sendSlow(socket) else {
                concurrency.countDown()
                if (concurrency.await(2, TimeUnit.SECONDS)) sendRange(socket, request)
            }
        }, server = server, scheme = "https") { url, requests ->
            assertThat(probingTransfer(client, logs, 2_500).open(url, "", null).use { it.readBytes() }).isEqualTo(data)
            assertThat(concurrency.count).isEqualTo(0)
            val ranges = requests.drop(1).map(::requestedRange).sortedBy { it.first }
            assertThat(ranges.size).isAtLeast(4)
            assertThat(ranges.last().last).isEqualTo(data.lastIndex)
            assertThat(ranges.zipWithNext().all { (left, right) -> left.last + 1 == right.first }).isTrue()
            assertThat(requests.drop(1).all { it.headers["if-range"] == tag }).isTrue()
            assertThat(logs.any { it.startsWith("parallel started ") }).isTrue()
        }
    }

    @Test
    fun `range startup deadline keeps the original HTTPS connection usable`() {
        val (server, client) = tls()
        val logs = CopyOnWriteArrayList<String>()
        withServer({ index, request, socket ->
            if (index == 0) sendSlow(socket) else {
                Thread.sleep(600)
                sendRange(socket, request)
            }
        }, server = server, scheme = "https") { url, requests ->
            assertThat(probingTransfer(client, logs, 200).open(url, "", null).use { it.readBytes() }).isEqualTo(data)
            assertThat(requests.drop(1).any { it.headers["range"]?.endsWith('-') == false }).isTrue()
            assertThat(logs.any { it.startsWith("parallel kept original ") }).isTrue()
        }
    }

    @Test
    fun `invalid parallel part resumes from the last committed byte`() {
        val (server, client) = tls()
        val logs = CopyOnWriteArrayList<String>()
        withServer({ index, request, socket ->
            if (index == 0) sendSlow(socket) else {
                val range = requestedRange(request)
                if (range.count() == testPartBytes) send(socket, "200 OK", data)
                else sendRange(socket, request)
            }
        }, server = server, scheme = "https") { url, requests ->
            assertThat(probingTransfer(client, logs, 2_500).open(url, "", null).use { it.readBytes() }).isEqualTo(data)
            val bounded = requests.drop(1).filter { it.headers["range"]?.endsWith('-') == false }
            val first = bounded.minOf { requestedRange(it).first }
            assertThat(logs.any { it.startsWith("parallel started ") }).isTrue()
            assertThat(requests.filter { it.headers["range"]?.endsWith('-') == true }.map { it.headers["range"] })
                .containsExactly("bytes=${first + testFirstPartBytes}-")
        }
    }

    @Test
    fun `timed out parallel part retries only its missing bytes`() {
        val (server, client) = tls()
        val shortTimeout = client.newBuilder().readTimeout(450, TimeUnit.MILLISECONDS).build()
        val logs = CopyOnWriteArrayList<String>()
        val stalled = AtomicReference<IntRange>()
        val received = testPartBytes / 2
        withServer({ index, request, socket ->
            if (index == 0) sendSlow(socket) else {
                val range = requestedRange(request)
                if (range.count() == testPartBytes && stalled.compareAndSet(null, range)) {
                    sendRange(socket, request, maxBytes = received)
                    Thread.sleep(1_100)
                } else sendRange(socket, request)
            }
        }, server = server, scheme = "https") { url, requests ->
            assertThat(probingTransfer(shortTimeout, logs, 2_500).open(url, "", null).use { it.readBytes() }).isEqualTo(data)
            val range = checkNotNull(stalled.get())
            assertThat(requests.filter { it.headers["range"] != null }.map(::requestedRange))
                .contains((range.first + received)..range.last)
            assertThat(requests.drop(1).none { it.headers.getValue("range").endsWith('-') }).isTrue()
            assertThat(logs.any { it.contains("start=${range.first + received} end=${range.last} reason=SocketTimeoutException") }).isTrue()
        }
    }

    private val testFirstPartBytes = 16 * 1024
    private val testPartBytes = 48 * 1024

    private fun probingTransfer(client: OkHttpClient, logs: MutableList<String>, budgetMs: Long) = WebDownloadTransfer(
        client = client,
        log = { logs.add(it) },
        policy = WebDownloadTransfer.Policy(
            firstWindowMs = 180, slowWindowMs = 180, slowBytesPerSecond = 800 * 1024,
            minRemaining = prefix.toLong(), firstPartBytes = testFirstPartBytes, partBytes = testPartBytes,
            rangeStartupMs = budgetMs
        ),
        pause = {}
    )

    private fun tls(): Pair<ServerSocket, OkHttpClient> {
        val pass = "fixture".toCharArray()
        val path = File(temporary.root, "fixture.p12")
        val keytool = File(System.getProperty("java.home"), "bin/keytool")
        val output = temporary.newFile("keytool.log")
        val process = ProcessBuilder(
            keytool.absolutePath, "-genkeypair", "-alias", "fixture", "-keyalg", "EC", "-validity", "1",
            "-dname", "CN=127.0.0.1", "-ext", "SAN=IP:127.0.0.1", "-storetype", "PKCS12",
            "-storepass", String(pass), "-keypass", String(pass), "-keystore", path.absolutePath
        ).redirectErrorStream(true).redirectOutput(output).start()
        try {
            check(process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0) { output.readText() }
        } finally {
            if (process.isAlive) process.destroyForcibly()
        }
        val store = KeyStore.getInstance("PKCS12").apply { path.inputStream().use { load(it, pass) } }
        val keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply { init(store, pass) }
        val trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply { init(store) }
        val ssl = SSLContext.getInstance("TLS").apply { init(keys.keyManagers, trust.trustManagers, null) }
        val server = ssl.serverSocketFactory.createServerSocket(0, 4, InetAddress.getByName("127.0.0.1"))
        val client = OkHttpClient.Builder()
            .sslSocketFactory(ssl.socketFactory, trust.trustManagers.filterIsInstance<X509TrustManager>().single())
            .followRedirects(false).retryOnConnectionFailure(false).build()
        return server to client
    }

    private fun sendSlow(socket: Socket) {
        val output = socket.getOutputStream()
        output.write(responseHeaders("200 OK", data.size, tag, null))
        for (offset in data.indices step 16 * 1024) {
            output.write(data, offset, minOf(data.size - offset, 16 * 1024))
            output.flush()
            if (offset + 16 * 1024 < data.size) Thread.sleep(85)
        }
    }

    private fun transfer(cookieFor: (HttpUrl) -> String? = { null }) = WebDownloadTransfer(cookieFor = cookieFor, pause = {})

    private fun requestedRange(request: Received): IntRange {
        val value = request.headers.getValue("range").removePrefix("bytes=")
        return value.substringBefore('-').toInt()..(value.substringAfter('-').toIntOrNull() ?: data.lastIndex)
    }

    private fun sendRange(socket: Socket, request: Received, maxBytes: Int = data.size) {
        val range = requestedRange(request)
        val body = data.copyOfRange(range.first, minOf(range.first + maxBytes, range.last + 1))
        send(socket, "206 Partial Content", body, length = range.count(), range = "bytes ${range.first}-${range.last}/${data.size}")
    }

    private fun send(socket: Socket, status: String, body: ByteArray, length: Int = body.size, etag: String? = tag, range: String? = null) {
        val output = socket.getOutputStream()
        output.write(responseHeaders(status, length, etag, range))
        output.write(body)
        output.flush()
    }

    private fun responseHeaders(status: String, length: Int, etag: String?, range: String?): ByteArray = buildString {
        append("HTTP/1.1 $status\r\nContent-Length: $length\r\nContent-Type: application/octet-stream\r\nConnection: close\r\n")
        etag?.let { append("ETag: $it\r\n") }
        range?.let { append("Content-Range: $it\r\n") }
        append("\r\n")
    }.toByteArray()

    private data class Received(val headers: Map<String, String>)

    private fun withServer(
        handle: (Int, Received, Socket) -> Unit,
        server: ServerSocket = ServerSocket(0, 4, InetAddress.getByName("127.0.0.1")),
        scheme: String = "http",
        verify: (String, List<Received>) -> Unit
    ) {
        server.use {
            val requests = CopyOnWriteArrayList<Received>()
            val workers = CopyOnWriteArrayList<Thread>()
            val loop = thread(isDaemon = true) {
                while (!server.isClosed) {
                    val accepted = try { server.accept() } catch (_: IOException) { break }
                    workers.add(thread(isDaemon = true) {
                        try {
                            accepted.use { socket ->
                                socket.soTimeout = 2_000
                                val input = socket.getInputStream().bufferedReader()
                                if (input.readLine() == null) return@use
                                val headers = buildMap {
                                    while (true) {
                                        val line = input.readLine()?.takeIf(String::isNotEmpty) ?: break
                                        put(line.substringBefore(':').lowercase(), line.substringAfter(':').trim())
                                    }
                                }
                                val request = Received(headers)
                                val index = synchronized(requests) { requests.size.also { requests.add(request) } }
                                handle(index, request, socket)
                            }
                        } catch (_: IOException) {
                            accepted.close()
                        }
                    })
                }
            }
            try {
                verify("$scheme://127.0.0.1:${server.localPort}", requests)
            } finally {
                server.close()
                loop.join(3_000)
                workers.forEach { it.join(3_000) }
            }
        }
    }
}
