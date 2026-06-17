package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

class ApkConfigWiringGuardTest {

    private val apkBuilderFile: File by lazy {
        resolveFile(
            "src/main/java/com/webtoapp/core/apkbuilder/ApkBuilder.kt",
            "app/src/main/java/com/webtoapp/core/apkbuilder/ApkBuilder.kt"
        )
    }

    private val jsonFactoryFile: File by lazy {
        resolveFile(
            "src/main/java/com/webtoapp/core/apkbuilder/ApkConfigJsonFactory.kt",
            "app/src/main/java/com/webtoapp/core/apkbuilder/ApkConfigJsonFactory.kt"
        )
    }

    @Test
    fun `apk builder file is reachable from test working directory`() {
        assertThat(apkBuilderFile.exists()).isTrue()
        assertThat(apkBuilderFile.isFile).isTrue()
    }

    @Test
    fun `buildXxxBlock functions do not hardcode user-facing field literals`() {
        val text = apkBuilderFile.readText()
        val blocks = extractBuildBlocks(text)
        assertThat(blocks).isNotEmpty()

        val violations = mutableListOf<String>()
        for (block in blocks) {
            for (hit in scanForLiterals(block.body)) {
                if (hit.value in WHITELISTED_INTERNALS) continue
                violations.add("${block.name} (L${block.startLine}): `${hit.field} = ${hit.value}`")
            }
        }

        assertThat(violations).isEmpty()
    }

    @Test
    fun `json factory payloads do not hardcode user-facing field literals`() {
        val text = jsonFactoryFile.readText()
        val payloads = extractPayloadFunctions(text)
        assertThat(payloads).isNotEmpty()

        val violations = mutableListOf<String>()
        for (payload in payloads) {
            for (hit in scanForLiteralsInPayload(payload.body)) {
                if (hit.value in WHITELISTED_INTERNALS) continue
                violations.add("${payload.name} (L${payload.startLine}): `${hit.key} to ${hit.value}`")
            }
        }

        assertThat(violations).isEmpty()
    }

    private data class BuildBlock(
        val name: String,
        val startLine: Int,
        val body: String
    )

    private data class LiteralHit(
        val field: String,
        val value: String
    )

    private data class PayloadFunc(
        val name: String,
        val startLine: Int,
        val body: String
    )

    private data class PayloadLiteralHit(
        val key: String,
        val value: String
    )

    private fun resolveFile(vararg candidates: String): File =
        candidates.map(::File).firstOrNull { it.exists() }
            ?: error("File not found at any of: $candidates (cwd=${File(".").absolutePath})")

    private fun extractBuildBlocks(text: String): List<BuildBlock> {
        val lines = text.lines()
        val starts = mutableListOf<Pair<Int, String>>()
        for ((idx, line) in lines.withIndex()) {
            val m = BUILD_BLOCK_HEADER.find(line) ?: continue
            starts.add(idx to m.groupValues[1])
        }
        if (starts.isEmpty()) return emptyList()
        return starts.mapIndexed { i, (startIdx, name) ->
            val endIdx = if (i + 1 < starts.size) starts[i + 1].first else lines.size
            val body = lines.subList(startIdx + 1, endIdx).joinToString("\n")
            BuildBlock(name, startIdx + 2, body)
        }
    }

    private fun extractPayloadFunctions(text: String): List<PayloadFunc> {
        val lines = text.lines()
        val starts = mutableListOf<Pair<Int, String>>()
        for ((idx, line) in lines.withIndex()) {
            val m = PAYLOAD_HEADER.find(line) ?: continue
            starts.add(idx to m.groupValues[1])
        }
        if (starts.isEmpty()) return emptyList()
        return starts.mapIndexed { i, (startIdx, name) ->
            val endIdx = if (i + 1 < starts.size) starts[i + 1].first else lines.size
            val body = lines.subList(startIdx + 1, endIdx).joinToString("\n")
            PayloadFunc(name, startIdx + 2, body)
        }
    }

    private fun scanForLiterals(body: String): List<LiteralHit> {
        val hits = mutableListOf<LiteralHit>()
        for (raw in body.lines()) {
            val line = raw.trim()
            if (line.isBlank()) continue
            if (line.startsWith("val ")) continue
            if (line.startsWith("private ") || line.startsWith("internal ")) continue
            if (line.startsWith("//")) continue
            if (line.startsWith("return ")) continue
            if (line.startsWith("add(") || line.startsWith("addAll(")) continue
            if (line.startsWith("}") || line.startsWith(")")) continue

            val m = ASSIGN.find(line) ?: continue
            val field = m.groupValues[1]
            val rhs = m.groupValues[2].trimEnd(',').trim()
            detectLiteral(rhs)?.let { hits.add(LiteralHit(field, it)) }
        }
        return hits
    }

    private fun scanForLiteralsInPayload(body: String): List<PayloadLiteralHit> {
        val hits = mutableListOf<PayloadLiteralHit>()
        for (raw in body.lines()) {
            val line = raw.trim()
            if (line.isBlank()) continue

            val m = PAIR_ASSIGN.find(line) ?: continue
            val key = m.groupValues[1]
            val rhs = m.groupValues[2].trimEnd(',').trim()
            detectLiteral(rhs)?.let { hits.add(PayloadLiteralHit(key, it)) }
        }
        return hits
    }

    private fun detectLiteral(rhs: String): String? {
        if (rhs == "true" || rhs == "false" || rhs == "null") return null
        if (rhs.startsWith("listOf") || rhs.startsWith("emptyList")) return null
        if (rhs.startsWith("it.") || rhs.startsWith("this.")) return null

        val isStringLiteral = rhs.startsWith("\"") && rhs.endsWith("\"")
        val hasInterpolation = rhs.contains("\$")
        if (isStringLiteral && !hasInterpolation && rhs !in WHITELISTED_INTERNALS) {
            return rhs
        }
        if (NUMERIC_LITERAL.matches(rhs)) {
            return rhs
        }
        return null
    }

    companion object {
        private val BUILD_BLOCK_HEADER =
            Regex("^private fun WebApp\\.(build\\w+Block)\\(")
        private val PAYLOAD_HEADER =
            Regex("private fun ApkConfig\\.(\\w+Payload\\(\\))")
        private val ASSIGN = Regex("^(\\w+)\\s*=\\s*(.+)$")
        private val PAIR_ASSIGN = Regex("\"(\\w+)\"\\s+to\\s+(.+?)(?:,\\s*\$|\$)")
        private val NUMERIC_LITERAL = Regex("^-?\\d+\\.?\\d*[fFlL]?$")
        private val WHITELISTED_INTERNALS = setOf(
            "\"__kernel__\"",
            "\"__perf_start__\"",
            "\"__perf_end__\""
        )
    }
}
