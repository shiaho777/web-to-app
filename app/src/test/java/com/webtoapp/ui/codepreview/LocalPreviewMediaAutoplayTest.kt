package com.webtoapp.ui.codepreview

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Contract: every host surface that previews local HTML content must allow
 * gesture-free media playback. WebViewActivity forces
 * `mediaPlaybackRequiresUserGesture = false` for HTML/FRONTEND apps and the shell
 * does the same via `isLocalFileApp`; preview panes that leave the platform
 * default `true` silently reject `audio.play()`/`autoplay`, which users perceive
 * as "mp3 embedded as data: URL produces no sound in the built-in preview".
 */
class LocalPreviewMediaAutoplayTest {

    @Test
    fun `html code preview disables the media gesture gate`() {
        val src = readSanitized("com/webtoapp/ui/codepreview/HtmlPreviewActivity.kt")
        assertWithMessage(
            "HtmlPreviewActivity must set mediaPlaybackRequiresUserGesture=false so " +
                "local audio/video (incl. data: sources) can play without a prior tap"
        ).that(src).contains("mediaPlaybackRequiresUserGesture = false")
    }

    @Test
    fun `agent preview pane disables the media gesture gate`() {
        val src = readSanitized("com/webtoapp/ui/agent/components/PreviewPane.kt")
        assertWithMessage(
            "PreviewPane must set mediaPlaybackRequiresUserGesture=false for parity " +
                "with the app preview and generated shells"
        ).that(src).contains("mediaPlaybackRequiresUserGesture = false")
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
