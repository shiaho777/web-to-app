package com.webtoapp.core.nodejs

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class NodeBootstrapTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `mjs entry is ESM regardless of package json`() {
        val dir = tmp.newFolder("proj")
        File(dir, "package.json").writeText("""{"type":"commonjs"}""")
        assertThat(NodeBootstrap.isEsmEntry(File(dir, "server.mjs"))).isTrue()
    }

    @Test
    fun `cjs entry is never ESM even under type module`() {
        val dir = tmp.newFolder("proj")
        File(dir, "package.json").writeText("""{"type":"module"}""")
        assertThat(NodeBootstrap.isEsmEntry(File(dir, "server.cjs"))).isFalse()
    }

    @Test
    fun `js entry follows nearest package json type module`() {
        val dir = tmp.newFolder("proj")
        File(dir, "package.json").writeText("""{"type":"module"}""")
        assertThat(NodeBootstrap.isEsmEntry(File(dir, "index.js"))).isTrue()
    }

    @Test
    fun `js entry under type commonjs or missing package json is CJS`() {
        val dir = tmp.newFolder("proj")
        File(dir, "package.json").writeText("""{"type":"commonjs"}""")
        assertThat(NodeBootstrap.isEsmEntry(File(dir, "index.js"))).isFalse()

        val bare = tmp.newFolder("bare")
        assertThat(NodeBootstrap.isEsmEntry(File(bare, "index.js"))).isFalse()
    }

    @Test
    fun `nested entry uses its own nearest package json`() {
        val dir = tmp.newFolder("proj")
        File(dir, "package.json").writeText("""{"type":"module"}""")
        val sub = File(dir, "legacy").apply { mkdirs() }
        File(sub, "package.json").writeText("""{"type":"commonjs"}""")
        assertThat(NodeBootstrap.isEsmEntry(File(sub, "server.js"))).isFalse()
        assertThat(NodeBootstrap.isEsmEntry(File(dir, "server.js"))).isTrue()
    }

    @Test
    fun `malformed package json falls back to CJS`() {
        val dir = tmp.newFolder("proj")
        File(dir, "package.json").writeText("{not json")
        assertThat(NodeBootstrap.isEsmEntry(File(dir, "index.js"))).isFalse()
    }

    @Test
    fun `generated CJS bootstrap chdirs and requires the entry`() {
        val script = NodeBootstrap.generate("/data/proj", "/data/proj/index.js", esm = false)
        assertThat(script).contains("process.chdir(\"/data/proj\")")
        assertThat(script).contains("require(\"/data/proj/index.js\")")
        assertThat(script).doesNotContain("pathToFileURL")
        assertThat(script).doesNotContain("undici")
        assertThat(script.indexOf("process.chdir")).isLessThan(script.indexOf("require(\"/data/proj/index.js\")"))
    }

    @Test
    fun `generated ESM bootstrap loads via dynamic import and reports failures`() {
        val script = NodeBootstrap.generate("/data/proj", "/data/proj/server.mjs", esm = true)
        assertThat(script).contains("import(require('url').pathToFileURL(\"/data/proj/server.mjs\").href)")
        assertThat(script).contains("[wta-bootstrap] entry failed")
        assertThat(script).doesNotContain("require(\"/data/proj/server.mjs\")")
    }

    @Test
    fun `paths with quotes and backslashes stay valid JS literals`() {
        val weird = "/data/we\"ird\\proj"
        val script = NodeBootstrap.generate(weird, "$weird/index.js", esm = false)
        assertThat(script).contains(NodeBootstrap.jsStringLiteral(weird))
        assertThat(NodeBootstrap.jsStringLiteral("a\"b\\c")).isEqualTo("\"a\\\"b\\\\c\"")
    }
}