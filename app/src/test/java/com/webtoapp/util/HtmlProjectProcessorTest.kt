package com.webtoapp.util

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.i18n.Strings
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HtmlProjectProcessorTest {

    @Rule @JvmField
    val temp = TemporaryFolder()

    @Test
    fun `dynamic image references are not reported as missing files`() {
        val html = temp.newFile("index.html")
        html.writeText(
            """
            <!doctype html>
            <html>
            <body>
                <img src="${'$'}{track.pic || track.cover}">
                <img src="${'$'}{track.artworkUrl100}">
                <img src="${'$'}{song.cover}">
                <img src="${'$'}{url}">
                <img src="${'$'}{accountData.avatar}">
                <img src="${'$'}{avatarUrl}">
                <img :src="track.pic || track.cover">
                <img src="{{ account.avatar }}">
                <img src="<%= user.avatar %>">
                <img src="blob:https://example.com/image">
                <img src="//cdn.example.com/image.png">
                <a href="mailto:test@example.com">mail</a>
            </body>
            </html>
            """.trimIndent()
        )

        val analysis = HtmlProjectProcessor.analyzeProject(
            htmlFilePath = html.absolutePath,
            cssFilePath = null,
            jsFilePath = null
        )

        assertThat(analysis.issues.map { it.message }).doesNotContain(Strings.referencedFileNotExist)
        assertThat(analysis.issues).isEmpty()
    }

    @Test
    fun `real missing local resources are still reported`() {
        val html = temp.newFile("index.html")
        html.writeText(
            """
            <!doctype html>
            <html>
            <head>
                <link rel="stylesheet" href="missing.css?v=1">
                <script src="./missing.js#app"></script>
            </head>
            <body></body>
            </html>
            """.trimIndent()
        )

        val analysis = HtmlProjectProcessor.analyzeProject(
            htmlFilePath = html.absolutePath,
            cssFilePath = null,
            jsFilePath = null
        )

        assertThat(analysis.issues.map { it.message })
            .containsAtLeast(Strings.referencedFileNotExist, Strings.referencedFileNotExist)
        assertThat(analysis.issues.map { it.suggestion })
            .containsAtLeast(
                Strings.suggestEnsureFileImported.replace("%s", "missing.css?v=1"),
                Strings.suggestEnsureFileImported.replace("%s", "./missing.js#app")
            )
    }

    @Test
    fun `existing local resources with query or hash are accepted`() {
        val dir = temp.newFolder("project")
        File(dir, "style.css").writeText("body { color: black; }")
        File(dir, "app.js").writeText("console.log('ok')")
        File(dir, "logo.png").writeBytes(byteArrayOf(1, 2, 3))
        val html = File(dir, "index.html")
        html.writeText(
            """
            <!doctype html>
            <html>
            <head>
                <link rel="stylesheet" href="style.css?v=1">
                <script src="./app.js#main"></script>
            </head>
            <body>
                <img src="logo.png?v=2#preview">
            </body>
            </html>
            """.trimIndent()
        )

        val analysis = HtmlProjectProcessor.analyzeProject(
            htmlFilePath = html.absolutePath,
            cssFilePath = null,
            jsFilePath = null
        )

        assertThat(analysis.issues).isEmpty()
    }

    @Test
    fun `reprocessing already-inlined html does not stack duplicate style or script blocks`() {
        val css = "body { color: red; }"
        val js = "console.log('hello');"
        val first = HtmlProjectProcessor.processHtmlContent(
            htmlContent = "<html><head></head><body><script src=\"app.js\"></script></body></html>",
            cssContent = css,
            jsContent = js,
            fixPaths = false
        )

        // Simulate a second save where the HTML now already contains the inlined blocks.
        val second = HtmlProjectProcessor.processHtmlContent(
            htmlContent = first,
            cssContent = css,
            jsContent = js,
            fixPaths = false
        )

        val openStyles = Regex("<style>", RegexOption.IGNORE_CASE).findAll(second).count()
        val inlineScripts = Regex("""<script>\s*/\* Inlined JS \*/""", RegexOption.IGNORE_CASE)
            .findAll(second).count()
        assertThat(openStyles).isEqualTo(1)
        assertThat(inlineScripts).isEqualTo(1)
        assertThat(second).contains("body { color: red; }")
        assertThat(second).contains("console.log('hello');")
    }

    @Test
    fun `re-inlining picks up edited css content instead of the stale copy`() {
        val first = HtmlProjectProcessor.processHtmlContent(
            htmlContent = "<html><head></head><body></body></html>",
            cssContent = "body { color: red; }",
            jsContent = null,
            fixPaths = false
        )
        val second = HtmlProjectProcessor.processHtmlContent(
            htmlContent = first,
            cssContent = "body { color: blue; }",
            jsContent = null,
            fixPaths = false
        )

        assertThat(second).contains("color: blue;")
        assertThat(second).doesNotContain("color: red;")
        assertThat(Regex("<style>", RegexOption.IGNORE_CASE).findAll(second).count()).isEqualTo(1)
    }
}
