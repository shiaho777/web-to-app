package com.webtoapp.core.perf

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * The export pipeline bakes [NativePerfEngine.getPerfJsEnd] into every
 * generated APK. That script used to set `content-visibility: auto` on tall
 * `.container > div` sections. Paint containment turns that section into the
 * containing block and stacking context of a `position: fixed` shop sidebar,
 * so the body-level close layer sits on top of the panel and a tap inside
 * the filters closes it (#1150). Preview never injects the script.
 */
class PerfScriptContainmentTest {

    @Test
    fun perfEngineDoesNotContainFixedPositionedDescendants() {
        val source = stripCComments(perfEngineSource())
        assertThat(source).doesNotContain("contentVisibility")
        assertThat(source).doesNotContain("content-visibility")
        assertThat(source).doesNotContain("containIntrinsicSize")
        assertThat(source).doesNotContain("contain:content")
        assertThat(source).doesNotContain("will-change")
    }

    private fun perfEngineSource(): String {
        val relative = listOf("src/main/cpp/perf_engine.c", "app/src/main/cpp/perf_engine.c")
        val startDir = System.getProperty("user.dir") ?: error("user.dir is unset")
        var dir: File? = File(startDir).absoluteFile
        repeat(6) {
            val current = dir ?: return@repeat
            for (rel in relative) {
                val file = File(current, rel)
                if (file.isFile) return file.readText()
            }
            dir = current.parentFile
        }
        error("perf_engine.c not found from $startDir")
    }

    private fun stripCComments(source: String): String {
        return source
            .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("//[^\n]*"), "")
    }
}
