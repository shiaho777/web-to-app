package com.webtoapp.core.engine

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Pins the createView session-lifecycle contract: an engine reused for a
 * second createView must retire the earlier session — close it and remove it
 * from liveSessions — before opening a new one and before the runtime-recreate
 * decision. A leaked session would pin liveSessions forever and permanently
 * defer every runtime config change (#1035 skip logic).
 */
class GeckoViewEngineSessionCleanupTest {

    private val src = readSanitized("com/webtoapp/core/engine/GeckoViewEngine.kt")

    private val createView = src.substringAfter("override fun createView")
        .substringBefore("fun setMediaSessionDelegate")

    @Test
    fun `createView closes and untracks a pre-existing session`() {
        assertWithMessage("createView must close the previous session")
            .that(createView).contains("old.close()")
        assertWithMessage("createView must untrack the previous session from liveSessions")
            .that(createView).contains("liveSessions.remove(old)")
        assertWithMessage("createView must detach the previous GeckoView session")
            .that(createView).contains("releaseSession()")
    }

    @Test
    fun `old session is retired before the new one opens`() {
        val beforeNew = createView.substringBefore("val newSession = GeckoSession", "")
        assertWithMessage("session retirement must happen before the new session is built")
            .that(beforeNew).contains("liveSessions.remove(old)")
    }

    @Test
    fun `old session is retired before ensureRuntimeForConfig counts liveSessions`() {
        val cleanupIdx = createView.indexOf("liveSessions.remove(old)")
        val ensureIdx = createView.indexOf("ensureRuntimeForConfig")
        assertWithMessage("createView must reference ensureRuntimeForConfig")
            .that(ensureIdx >= 0).isTrue()
        assertWithMessage("retirement must precede the liveSessions-sensitive runtime check")
            .that(cleanupIdx in 0 until ensureIdx).isTrue()
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
