package com.webtoapp.core.i18n

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

class StringsKtTranslationParityTest {

    @Test
    fun `every when(lang) block covers all ten languages`() {
        val source = readStringsKt()
        val problems = analyse(source)
        assertWithMessage(
            buildString {
                appendLine("Strings.kt has when(lang) blocks that fail i18n parity rules.")
                appendLine("Each block must list all ten branches explicitly:")
                appendLine("  CHINESE, ENGLISH, ARABIC, PORTUGUESE, SPANISH,")
                appendLine("  FRENCH, GERMAN, RUSSIAN, JAPANESE, KOREAN")
                appendLine()
                appendLine("'else ->' branches are forbidden. A resource deferral")
                appendLine("(else -> getString(R.string.*)) silently falls back to the")
                appendLine("default values/ (Chinese) for locales without a values-*/")
                appendLine("directory, so it masks missing translations instead of")
                appendLine("providing them. pt/es/fr/de/ru/ja/ko have no values-*/ folder")
                appendLine("and must therefore be served by inline branches.")
                appendLine()
                appendLine("Offending blocks (line number is the line containing 'when (Strings.lang)'):")
                problems.forEach { appendLine("  $it") }
            }
        ).that(problems).isEmpty()
    }

    private fun readStringsKt(): String {
        val candidates = listOf(
            "app/src/main/java/com/webtoapp/core/i18n/Strings.kt",
            "src/main/java/com/webtoapp/core/i18n/Strings.kt",
        )
        val file = candidates.map(::File).firstOrNull { it.exists() }
            ?: error("Could not locate Strings.kt from: $candidates")
        return file.readText()
    }

    private fun analyse(source: String): List<String> {
        val problems = mutableListOf<String>()
        val whenLangNeedle = "when (Strings.lang)"

        var index = 0
        while (index < source.length) {
            val (newIndex, mode) = skipNonCode(source, index)
            if (newIndex != index) {
                index = newIndex
                continue
            }

            if (mode == ScanMode.Code && source.regionMatches(index, whenLangNeedle, 0, whenLangNeedle.length)) {

                val braceIdx = findOpeningBrace(source, index + whenLangNeedle.length)
                if (braceIdx != null) {
                    val openerLine = lineOf(source, index)
                    val (analysis, blockEnd) = analyseBlock(source, braceIdx)
                    problems += inspect(analysis, openerLine)
                    index = blockEnd + 1
                    continue
                }
            }
            index++
        }
        return problems
    }

    private fun findOpeningBrace(source: String, from: Int): Int? {
        var i = from
        while (i < source.length) {
            val (next, _) = skipNonCode(source, i)
            if (next != i) {
                i = next
                continue
            }
            val ch = source[i]
            if (ch == '{') return i
            if (ch.isWhitespace()) {
                i++
                continue
            }

            return null
        }
        return null
    }

    private data class BlockAnalysis(
        val presentLanguages: Set<String>,
        val anyElseAtTopLevel: Boolean,
    )

    private val ALL_LANGUAGES = listOf(
        "CHINESE", "ENGLISH", "ARABIC", "PORTUGUESE", "SPANISH",
        "FRENCH", "GERMAN", "RUSSIAN", "JAPANESE", "KOREAN"
    )

    private fun analyseBlock(source: String, openingBraceIndex: Int): Pair<BlockAnalysis, Int> {
        val present = mutableSetOf<String>()
        var anyElseAtTopLevel = false

        var depth = 1
        var i = openingBraceIndex + 1

        while (i < source.length && depth > 0) {

            val (next, _) = skipNonCode(source, i)
            if (next != i) {
                i = next
                continue
            }
            val ch = source[i]
            when (ch) {
                '{' -> {
                    depth++
                    i++
                    continue
                }
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return BlockAnalysis(present.toSet(), anyElseAtTopLevel) to i
                    }
                    i++
                    continue
                }
            }

            if (depth == 1) {
                for (lang in ALL_LANGUAGES) {
                    if (matchAt(source, i, "AppLanguage.$lang")) present.add(lang)
                }

                if (matchAt(source, i, "else") && isAtWordStart(source, i) &&
                    looksLikeArrowAfter(source, i + 4)
                ) {
                    anyElseAtTopLevel = true
                }
            }
            i++
        }

        return BlockAnalysis(present.toSet(), anyElseAtTopLevel) to (source.length - 1)
    }

    private fun isAtWordStart(source: String, i: Int): Boolean {
        if (i == 0) return true
        val prev = source[i - 1]
        return !(prev.isLetterOrDigit() || prev == '_')
    }

    private fun looksLikeArrowAfter(source: String, from: Int): Boolean {
        var i = from
        while (i < source.length && (source[i] == ' ' || source[i] == '\t')) i++
        return i + 1 < source.length && source[i] == '-' && source[i + 1] == '>'
    }

    private fun matchAt(source: String, i: Int, literal: String): Boolean {
        if (!source.regionMatches(i, literal, 0, literal.length)) return false

        val before = if (i == 0) ' ' else source[i - 1]
        val afterIdx = i + literal.length
        val after = if (afterIdx >= source.length) ' ' else source[afterIdx]
        val isWordChar: (Char) -> Boolean = { it.isLetterOrDigit() || it == '_' }

        if (isWordChar(before) && literal.first().let { isWordChar(it) }) return false
        if (isWordChar(after) && literal.last().let { isWordChar(it) }) return false
        return true
    }

    private enum class ScanMode { Code, Skipped }

    private fun skipNonCode(source: String, index: Int): Pair<Int, ScanMode> {
        if (index >= source.length) return index to ScanMode.Code
        val ch = source[index]

        if (ch == '/' && index + 1 < source.length && source[index + 1] == '/') {
            var i = index + 2
            while (i < source.length && source[i] != '\n') i++
            return i to ScanMode.Skipped
        }

        if (ch == '/' && index + 1 < source.length && source[index + 1] == '*') {
            var i = index + 2
            while (i + 1 < source.length) {
                if (source[i] == '*' && source[i + 1] == '/') return (i + 2) to ScanMode.Skipped
                i++
            }
            return source.length to ScanMode.Skipped
        }

        if (ch == '"' && index + 2 < source.length &&
            source[index + 1] == '"' && source[index + 2] == '"') {
            var i = index + 3
            while (i + 2 < source.length) {
                if (source[i] == '"' && source[i + 1] == '"' && source[i + 2] == '"') {
                    return (i + 3) to ScanMode.Skipped
                }
                i++
            }
            return source.length to ScanMode.Skipped
        }

        if (ch == '"') {
            var i = index + 1
            while (i < source.length) {
                val c = source[i]
                if (c == '\\' && i + 1 < source.length) {
                    i += 2
                    continue
                }
                if (c == '"') return (i + 1) to ScanMode.Skipped
                if (c == '\n') return i to ScanMode.Skipped
                i++
            }
            return source.length to ScanMode.Skipped
        }

        if (ch == '\'') {
            var i = index + 1
            if (i < source.length && source[i] == '\\') i += 2 else if (i < source.length) i += 1
            if (i < source.length && source[i] == '\'') i += 1
            return i to ScanMode.Skipped
        }
        return index to ScanMode.Code
    }

    private fun lineOf(source: String, index: Int): Int {
        var line = 1
        for (i in 0 until index) {
            if (source[i] == '\n') line++
        }
        return line
    }

    private fun inspect(analysis: BlockAnalysis, openerLine: Int): List<String> {
        val problems = mutableListOf<String>()

        val anyBranch = analysis.presentLanguages.isNotEmpty()
        if (!anyBranch && !analysis.anyElseAtTopLevel) {

            return problems
        }
        if (analysis.anyElseAtTopLevel) {
            problems += "L$openerLine: when(lang) uses an 'else ->' branch. " +
                "List all ten AppLanguage cases instead — 'else ->' (including resource " +
                "deferrals via getString(R.string.*)) silently falls back to Chinese for " +
                "locales without a values-*/ directory."
            return problems
        }
        val missing = ALL_LANGUAGES.filter { it !in analysis.presentLanguages }
        if (missing.isNotEmpty()) {
            problems += "L$openerLine: when(lang) is missing branches for: ${missing.joinToString(", ")}"
        }
        return problems
    }
}
