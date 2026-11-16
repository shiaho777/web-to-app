package com.webtoapp.core.linux

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

class NativeNodeEngineExecPathTest {

    private val src = readSanitized("com/webtoapp/core/linux/NativeNodeEngine.kt")

    @Test
    fun `esbuild launches through HostProcessLauncher`() {
        val execute = src.substringAfter("suspend fun executeEsbuild")
            .substringBefore("private suspend fun downloadFile")
        assertWithMessage("executeEsbuild must route through HostProcessLauncher")
            .that(execute).contains("HostProcessLauncher.start")
        assertWithMessage("executeEsbuild must not use a raw ProcessBuilder")
            .that(execute).doesNotContain("ProcessBuilder(")
    }

    @Test
    fun `downloads use the extended-timeout download client`() {
        val download = src.substringAfter("private suspend fun downloadFile")
            .substringBefore("suspend fun reset")
        assertWithMessage("downloadFile must use NetworkModule.downloadClient")
            .that(download).contains("NetworkModule.downloadClient")
        assertWithMessage("downloadFile must not open raw HttpURLConnections")
            .that(download).doesNotContain("HttpURLConnection")
    }

    private fun readSanitized(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertWithMessage("Test harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }
}
