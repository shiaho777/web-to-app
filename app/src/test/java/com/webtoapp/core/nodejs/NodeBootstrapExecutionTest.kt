package com.webtoapp.core.nodejs

import com.google.common.truth.Truth.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.TimeUnit

class NodeBootstrapExecutionTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun nodeBinary(): String? {
        System.getenv("NODE_BINARY")?.takeIf { it.isNotBlank() }?.let { return it }
        return listOf("/usr/local/bin/node", "/usr/bin/node", "/opt/homebrew/bin/node")
            .firstOrNull { File(it).canExecute() }
    }

    private fun runBootstrap(
        projectDir: File,
        entry: File,
        esm: Boolean,
        outputMarker: String? = null
    ): Pair<Int, String> {
        val node = nodeBinary()
        assumeTrue("node executable not available (set NODE_BINARY)", node != null)
        val bootstrap = File(projectDir, NodeBootstrap.BOOTSTRAP_FILE_NAME)
        bootstrap.writeText(NodeBootstrap.generate(projectDir.absolutePath, entry.absolutePath, esm))
        val proc = ProcessBuilder(node!!, bootstrap.absolutePath)
            .directory(File("/"))
            .redirectErrorStream(true)
            .start()
        val collected = StringBuilder()
        val reader = Thread {
            proc.inputStream.bufferedReader().forEachLine { line ->
                synchronized(collected) { collected.append(line).append('\n') }
            }
        }
        reader.isDaemon = true
        reader.start()
        val deadline = System.currentTimeMillis() + 30_000
        var markerSeen = false
        while (System.currentTimeMillis() < deadline) {
            if (!proc.isAlive) break
            if (outputMarker != null &&
                synchronized(collected) { collected.contains(outputMarker) }
            ) {
                markerSeen = true
                break
            }
            Thread.sleep(100)
        }
        val exitedNaturally = !proc.isAlive
        if (proc.isAlive) {
            proc.destroyForcibly()
            proc.waitFor(5, TimeUnit.SECONDS)
        }
        reader.join(3_000)
        val output = synchronized(collected) { collected.toString() }
        if (!exitedNaturally && !markerSeen) {
            throw AssertionError("bootstrap neither exited nor produced its marker; output:\n$output")
        }
        return (if (exitedNaturally) proc.exitValue() else -1) to output
    }

    private fun serverEntry(reportFile: File, esm: Boolean): String {
        val p = NodeBootstrap.jsStringLiteral(reportFile.absolutePath)
        return if (esm) {
            """
            import http from 'node:http';
            import fs from 'node:fs';
            const server = http.createServer((req, res) => { res.end('ok'); });
            server.listen(0, '127.0.0.1', () => {
                const port = server.address().port;
                http.get({ host: '127.0.0.1', port: port, path: '/' }, (res) => {
                    res.resume();
                    res.on('end', () => {
                        fs.writeFileSync($p, String(process.cwd()) + '\n' + port);
                        console.log('WTA_TEST_DONE');
                        server.close();
                        clearInterval(globalThis.__wtaKeepAlive);
                    });
                });
            });
            """.trimIndent()
        } else {
            """
            const http = require('http');
            const fs = require('fs');
            const server = http.createServer((req, res) => { res.end('ok'); });
            server.listen(0, '127.0.0.1', () => {
                const port = server.address().port;
                http.get({ host: '127.0.0.1', port: port, path: '/' }, (res) => {
                    res.resume();
                    res.on('end', () => {
                        fs.writeFileSync($p, String(process.cwd()) + '\n' + port);
                        console.log('WTA_TEST_DONE');
                        server.close();
                        clearInterval(globalThis.__wtaKeepAlive);
                    });
                });
            });
            """.trimIndent()
        }
    }

    @Test
    fun `CJS entry runs with project cwd and serves HTTP`() {
        val proj = tmp.newFolder("cjs-proj")
        val report = File(proj, "report.txt")
        val entry = File(proj, "index.js").apply { writeText(serverEntry(report, esm = false)) }
        val (_, output) = runBootstrap(proj, entry, esm = false, outputMarker = "WTA_TEST_DONE")
        assertThat(output).doesNotContain("[wta-bootstrap] entry failed")
        val lines = report.readText().trim().lines()
        assertThat(File(lines[0]).canonicalPath).isEqualTo(proj.canonicalPath)
        assertThat(lines[1].toInt()).isGreaterThan(0)
    }

    @Test
    fun `mjs entry loads through dynamic import`() {
        val proj = tmp.newFolder("mjs-proj")
        val report = File(proj, "report.txt")
        val entry = File(proj, "server.mjs").apply { writeText(serverEntry(report, esm = true)) }
        val (_, output) = runBootstrap(proj, entry, esm = true, outputMarker = "WTA_TEST_DONE")
        assertThat(output).doesNotContain("[wta-bootstrap] entry failed")
        assertThat(File(report.readText().trim().lines()[0]).canonicalPath).isEqualTo(proj.canonicalPath)
    }

    @Test
    fun `js entry under type module loads as ESM`() {
        val proj = tmp.newFolder("type-module-proj")
        File(proj, "package.json").writeText("""{"type":"module"}""")
        val report = File(proj, "report.txt")
        val entry = File(proj, "index.js").apply { writeText(serverEntry(report, esm = true)) }
        val esm = NodeBootstrap.isEsmEntry(entry)
        assertThat(esm).isTrue()
        val (_, output) = runBootstrap(proj, entry, esm, outputMarker = "WTA_TEST_DONE")
        assertThat(output).doesNotContain("[wta-bootstrap] entry failed")
        assertThat(File(report.readText().trim().lines()[0]).canonicalPath).isEqualTo(proj.canonicalPath)
    }

    @Test
    fun `relative fs reads resolve against the project directory`() {
        val proj = tmp.newFolder("relative-proj")
        File(proj, "data.txt").writeText("payload-42")
        val report = File(proj, "report.txt")
        val r = NodeBootstrap.jsStringLiteral(report.absolutePath)
        val entry = File(proj, "index.js").apply {
            writeText(
                """
                const fs = require('fs');
                const value = fs.readFileSync('data.txt', 'utf8');
                fs.writeFileSync($r, value);
                console.log('WTA_TEST_DONE');
                clearInterval(globalThis.__wtaKeepAlive);
                """.trimIndent()
            )
        }
        val (_, output) = runBootstrap(proj, entry, esm = false, outputMarker = "WTA_TEST_DONE")
        assertThat(output).doesNotContain("[wta-bootstrap] entry failed")
        assertThat(report.readText()).isEqualTo("payload-42")
    }

    @Test
    fun `broken entry surfaces the entry failed marker`() {
        val proj = tmp.newFolder("broken-proj")
        val entry = File(proj, "index.js").apply { writeText("throw new Error('boom-42');") }
        val (_, output) = runBootstrap(proj, entry, esm = false, outputMarker = "[wta-bootstrap] entry failed")
        assertThat(output).contains("[wta-bootstrap] entry failed")
        assertThat(output).contains("boom-42")
    }

    @Test
    fun `broken ESM entry surfaces the entry failed marker`() {
        val proj = tmp.newFolder("broken-esm-proj")
        val entry = File(proj, "server.mjs").apply { writeText("throw new Error('boom-esm');") }
        val (_, output) = runBootstrap(proj, entry, esm = true, outputMarker = "[wta-bootstrap] entry failed")
        assertThat(output).contains("[wta-bootstrap] entry failed")
        assertThat(output).contains("boom-esm")
    }
}