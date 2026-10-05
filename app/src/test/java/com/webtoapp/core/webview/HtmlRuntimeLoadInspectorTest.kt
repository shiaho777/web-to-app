package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.HtmlLoadMode
import java.io.File
import java.nio.file.Files
import org.junit.Test

class HtmlRuntimeLoadInspectorTest {

    private fun project(vararg files: Pair<String, String>): File {
        val dir = Files.createTempDirectory("html-load").toFile()
        files.forEach { (name, body) ->
            val file = File(dir, name)
            file.parentFile?.mkdirs()
            file.writeText(body)
        }
        return dir
    }

    @Test
    fun `plain file mode stays on the file scheme`() {
        val dir = project("index.html" to "<html><h1>Hi</h1></html>")
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.FILE, dir)).isTrue()
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.AUTO, dir)).isTrue()
    }

    @Test
    fun `cdn module page leaves the file scheme even when file mode is pinned`() {
        val dir = project(
            "index.html" to """
                <link rel="stylesheet" href="https://unpkg.com/mdui@2/mdui.css">
                <script type="module" src="https://unpkg.com/mdui@2/mdui.esm.js"></script>
            """.trimIndent()
        )
        assertThat(HtmlRuntimeLoadInspector.requiresRealOrigin(dir)).isTrue()
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.FILE, dir)).isFalse()
    }

    @Test
    fun `fetch only file mode stays on the file scheme`() {
        val dir = project("index.html" to "<html><script>fetch('/data.json')</script></html>")
        assertThat(HtmlRuntimeLoadInspector.requiresRealOrigin(dir)).isFalse()
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.FILE, dir)).isTrue()
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.AUTO, dir)).isFalse()
    }

    @Test
    fun `local http never uses the file scheme`() {
        val dir = project("index.html" to "<html></html>")
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.LOCAL_HTTP, dir)).isFalse()
    }

    @Test
    fun `missing directory keeps the historical fallback`() {
        val missing = File(project(), "nope")
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.FILE, missing)).isTrue()
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.FILE, null)).isTrue()
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.AUTO, missing)).isFalse()
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.AUTO, null)).isFalse()
    }

    @Test
    fun `cross origin isolation forces the local server in auto mode only`() {
        val dir = project("index.html" to "<html></html>")
        assertThat(
            HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.AUTO, dir, crossOriginIsolation = true)
        ).isFalse()
        assertThat(
            HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.FILE, dir, crossOriginIsolation = true)
        ).isTrue()
    }

    @Test
    fun `an mjs file needs a real origin and a protocol comment does not`() {
        val modules = project("app.mjs" to "export const x = 1\n")
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.FILE, modules)).isFalse()
        val comment = project("index.html" to "<html><script>// keep this offline\n</script></html>")
        assertThat(HtmlRuntimeLoadInspector.useFileScheme(HtmlLoadMode.FILE, comment)).isTrue()
    }
}
