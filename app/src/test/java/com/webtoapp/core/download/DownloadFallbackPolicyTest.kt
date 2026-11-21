package com.webtoapp.core.download

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DownloadFallbackPolicyTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun dest(): Pair<File, File> {
        val destFile = tmp.newFile("pkg.zip")
        destFile.delete()
        val tmpFile = File(destFile.parentFile, "${destFile.name}.tmp")
        return destFile to tmpFile
    }

    private fun engine() = DependencyDownloadEngine

    @Test
    fun `slow source keeps the partial tmp and switches to the next url`() = runBlocking {
        val (destFile, tmpFile) = dest()
        tmpFile.writeBytes(ByteArray(64))
        val calls = mutableListOf<String>()

        val ok = engine().downloadFileWithFallback(
            listOf("https://slow/proxied", "https://fast/proxied"),
            destFile, "Unit", null,
            maxRetryPerUrl = 2, retryDelayMs = 0,
            fetch = { url, _ ->
                calls.add(url)
                if (url.endsWith("slow/proxied")) {
                    assertThat(tmpFile.exists()).isTrue()
                    DependencyDownloadEngine.Outcome.SLOW
                } else {
                    assertThat(tmpFile.exists()).isTrue()
                    DependencyDownloadEngine.Outcome.SUCCESS
                }
            }
        )
        assertThat(ok).isTrue()
        assertThat(calls).containsExactly("https://slow/proxied", "https://fast/proxied").inOrder()
    }

    @Test
    fun `failed source retries then drops tmp before switching`() = runBlocking {
        val (destFile, tmpFile) = dest()
        tmpFile.writeBytes(ByteArray(64))
        val calls = mutableListOf<String>()

        val ok = engine().downloadFileWithFallback(
            listOf("https://bad/proxied", "https://good/proxied"),
            destFile, "Unit", null,
            maxRetryPerUrl = 2, retryDelayMs = 0,
            fetch = { url, _ ->
                calls.add(url)
                if (url.endsWith("bad/proxied")) {
                    DependencyDownloadEngine.Outcome.FAILED
                } else {
                    assertThat(tmpFile.exists()).isFalse()
                    DependencyDownloadEngine.Outcome.SUCCESS
                }
            }
        )
        assertThat(ok).isTrue()
        assertThat(calls).containsExactly(
            "https://bad/proxied", "https://bad/proxied", "https://good/proxied"
        ).inOrder()
    }

    @Test
    fun `slow on the last source resumes until retries are exhausted`() = runBlocking {
        val (destFile, _) = dest()
        var calls = 0

        val ok = engine().downloadFileWithFallback(
            listOf("https://only/proxied"),
            destFile, "Unit", null,
            maxRetryPerUrl = 3, retryDelayMs = 0,
            fetch = { _, _ ->
                calls++
                DependencyDownloadEngine.Outcome.SLOW
            }
        )
        assertThat(ok).isFalse()
        assertThat(calls).isEqualTo(3)
        assertThat(engine().state.value).isInstanceOf(DependencyDownloadEngine.State.Error::class.java)
        engine().reset()
    }

    @Test
    fun `the only source can finish while steadily sending below the switch threshold`() = runBlocking {
        val (destFile, _) = dest()
        val total = 2 * 1024 * 1024
        val chunk = ByteArray(4096)
        ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { server ->
            val sender = thread(isDaemon = true) {
                runCatching {
                    server.accept().use { socket ->
                        socket.soTimeout = 2_000
                        val input = socket.getInputStream().bufferedReader()
                        while (!input.readLine().isNullOrEmpty()) Unit
                        val output = socket.getOutputStream()
                        output.write("HTTP/1.1 200 OK\r\nContent-Length: $total\r\nConnection: close\r\n\r\n".toByteArray())
                        repeat(36) {
                            output.write(chunk)
                            output.flush()
                            Thread.sleep(500)
                        }
                        output.write(ByteArray(total - 36 * chunk.size))
                    }
                }
            }
            try {
                assertThat(engine().downloadFileWithFallback(
                    listOf("http://127.0.0.1:${server.localPort}/pkg.zip"),
                    destFile, "Unit", maxRetryPerUrl = 1
                )).isTrue()
                assertThat(destFile.length()).isEqualTo(total.toLong())
            } finally {
                server.close()
                sender.join(2_000)
                engine().reset()
            }
        }
    }

    @Test
    fun `corrupt source drops tmp and switches without retrying the same url`() = runBlocking {
        val (destFile, tmpFile) = dest()
        tmpFile.writeBytes(ByteArray(64))
        val calls = mutableListOf<String>()

        val ok = engine().downloadFileWithFallback(
            listOf("https://poisoned/proxied", "https://good/proxied"),
            destFile, "Unit", null,
            maxRetryPerUrl = 2, retryDelayMs = 0,
            fetch = { url, _ ->
                calls.add(url)
                if (url.endsWith("poisoned/proxied")) {
                    DependencyDownloadEngine.Outcome.CORRUPT
                } else {
                    assertThat(tmpFile.exists()).isFalse()
                    DependencyDownloadEngine.Outcome.SUCCESS
                }
            }
        )
        assertThat(ok).isTrue()
        assertThat(calls).containsExactly("https://poisoned/proxied", "https://good/proxied").inOrder()
    }

    @Test
    fun `corrupt on the last source fails without retry and reports the error`() = runBlocking {
        val (destFile, _) = dest()
        var calls = 0

        val ok = engine().downloadFileWithFallback(
            listOf("https://only/proxied"),
            destFile, "Unit", null,
            maxRetryPerUrl = 3, retryDelayMs = 0,
            fetch = { _, _ ->
                calls++
                DependencyDownloadEngine.Outcome.CORRUPT
            }
        )
        assertThat(ok).isFalse()
        assertThat(calls).isEqualTo(1)
        assertThat(engine().state.value).isInstanceOf(DependencyDownloadEngine.State.Error::class.java)
        engine().reset()
    }

    @Test
    fun `success on first try makes a single call`() = runBlocking {
        val (destFile, _) = dest()
        var calls = 0

        val ok = engine().downloadFileWithFallback(
            listOf("https://good/proxied"),
            destFile, "Unit", null,
            fetch = { _, _ -> calls++; DependencyDownloadEngine.Outcome.SUCCESS }
        )
        assertThat(ok).isTrue()
        assertThat(calls).isEqualTo(1)
    }

    @Test
    fun `sha256Of computes the streaming digest`() {
        val f = tmp.newFile("hash.bin")
        f.writeBytes("abc".toByteArray(Charsets.US_ASCII))
        assertThat(engine().sha256Of(f))
            .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
    }
}
