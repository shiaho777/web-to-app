package com.webtoapp.core.nodejs

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Pins the NodeService language-init contract: the DataStore language read is
 * suspend work and must run on the service's worker Handler, never as a
 * runBlocking on the service main thread (StrictMode violation / ANR risk).
 * Ordering is free: every Strings consumer runs on the same worker handler,
 * so a task posted in onCreate always lands before the first String read.
 */
class NodeServiceLanguageInitTest {

    private val src = readSanitized("com/webtoapp/core/nodejs/NodeService.kt")

    @Test
    fun `language init is posted to the worker handler`() {
        val onCreate = src.substringAfter("override fun onCreate")
            .substringBefore("override fun onDestroy")
        assertWithMessage("onCreate must delegate language init to workerHandler")
            .that(onCreate).contains("workerHandler.post")
    }

    @Test
    fun `runBlocking never executes on the service main thread`() {
        val onCreate = src.substringAfter("override fun onCreate")
            .substringBefore("override fun onDestroy")
        // Everything after the worker post runs off-main; the section between
        // the opening brace of onCreate and the worker post is the only part
        // still on the main thread and must stay blocking-free.
        val mainThreadSection = onCreate.substringBefore("workerHandler.post", "")
        assertWithMessage("no runBlocking allowed on the main-thread part of onCreate")
            .that(mainThreadSection).doesNotContain("runBlocking")
        assertWithMessage("the DataStore read must live inside the worker post")
            .that(onCreate.substringAfter("workerHandler.post")).contains("getCurrentLanguage")
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
