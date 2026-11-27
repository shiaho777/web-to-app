package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import java.util.concurrent.TimeUnit
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PrivateNetworkBridgeFetchTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `fetch routes writes once and preserves browser reads and request data`() =
        runCases("fetch")

    @Test
    fun `XHR prefers browser reads and streams fallback without replaying writes or partial responses`() =
        runCases("xhr")

    private fun runCases(kind: String) {
        val node = System.getenv("NODE_BINARY")?.takeIf { it.isNotBlank() }
            ?: System.getenv("PATH").orEmpty().split(File.pathSeparator)
                .map { File(it, "node") }.firstOrNull { it.isFile && it.canExecute() }?.absolutePath
        assumeTrue("Node.js is required to execute the network bridge script", node != null)

        val script = tmp.newFile("bridge.js").apply {
            writeText(WebViewManager.PrivateNetworkApiBridgeScriptHolder.SCRIPT)
        }
        val cases = tmp.newFile("$kind-cases.cjs").apply {
            requireNotNull(PrivateNetworkBridgeFetchTest::class.java.getResourceAsStream("/private-network-$kind-cases.cjs"))
                .use { input -> outputStream().use { input.copyTo(it) } }
        }
        val output = tmp.newFile("node.log")
        val process = ProcessBuilder(node!!, cases.absolutePath, script.absolutePath)
            .redirectErrorStream(true)
            .redirectOutput(output)
            .start()
        try {
            assertWithMessage("Bridge execution timed out: ${output.readText()}")
                .that(process.waitFor(20, TimeUnit.SECONDS)).isTrue()
            assertWithMessage(output.readText()).that(process.exitValue()).isEqualTo(0)
        } finally {
            if (process.isAlive) process.destroyForcibly()
        }
    }
}
