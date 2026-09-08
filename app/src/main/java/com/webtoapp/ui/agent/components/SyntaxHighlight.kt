package com.webtoapp.ui.agent.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.webtoapp.ui.theme.AppColors

/**
 * Lightweight regex-based syntax highlighting for fenced code blocks in the
 * agent timeline. Produces an [AnnotatedString] of per-line colored spans
 * (keyword / string / comment / number / function) mapped onto the existing
 * [AppColors] editor palette. Unknown or unlabeled languages return null and
 * the caller falls back to plain monochrome text.
 *
 * This is intentionally NOT a real lexer: no new dependencies (per project
 * policy), streaming-friendly (per-line, O(n)), and safe on malformed input.
 */
object SyntaxHighlight {

    /** Highlighting is skipped above this size to keep streaming smooth. */
    const val MAX_HIGHLIGHT_CHARS = 20_000

    /** Returns a highlighted string, or null when the language is unsupported. */
    fun highlight(code: String, language: String?): AnnotatedString? {
        if (code.length > MAX_HIGHLIGHT_CHARS) return null
        val rules = rulesFor(language) ?: return null
        return buildAnnotatedString {
            code.splitToSequence("\n").forEachIndexed { index, line ->
                if (index > 0) append('\n')
                highlightLine(line, rules)
            }
        }
    }

    /** True when [language] has a rule set (used to decide plain vs highlighted). */
    fun supports(language: String?): Boolean = rulesFor(language) != null

    private fun AnnotatedString.Builder.highlightLine(line: String, rules: LanguageRules) {
        var index = 0
        val plain = SpanStyle(color = AppColors.CodeForeground)
        while (index < line.length) {
            var best: MatchResult? = null
            var bestRule: TokenRule? = null
            for (rule in rules.tokens) {
                val m = rule.regex.find(line, index) ?: continue
                if (m.range.first == index && (best == null || m.value.length > best.value.length)) {
                    best = m
                    bestRule = rule
                }
            }
            if (best != null && bestRule != null) {
                withStyle(bestRule.style) { append(best.value) }
                index = best.range.last + 1
            } else {
                withStyle(plain) { append(line[index]) }
                index++
            }
        }
    }

    private class TokenRule(val regex: Regex, val style: SpanStyle)

    private class LanguageRules(val tokens: List<TokenRule>)

    private val keywordStyle = SpanStyle(color = AppColors.CodeKeyword)
    private val stringStyle = SpanStyle(color = AppColors.CodeString)
    private val commentStyle = SpanStyle(color = AppColors.CodeComment)
    private val numberStyle = SpanStyle(color = AppColors.CodeNumber)
    private val functionStyle = SpanStyle(color = AppColors.CodeFunction)

    private fun rules(vararg pairs: Pair<String, SpanStyle>): LanguageRules =
        LanguageRules(pairs.map { TokenRule(Regex(it.first), it.second) })

    private fun keywords(vararg words: String): String =
        "\\b(?:${words.joinToString("|") { Regex.escape(it) }})\\b"

    private val cLikeRules = rules(
        "//[^\n]*" to commentStyle,
        "/\\*(?:.|\\n)*?\\*/" to commentStyle,
        "\"(?:\\\\.|[^\"\\\\\\n])*\"" to stringStyle,
        "'(?:\\\\.|[^'\\\\\\n])+'" to stringStyle,
        "`(?:\\\\.|[^`\\\\])*`" to stringStyle,
        "\\b\\d[\\d_]*(?:\\.[\\d_]+)?[fLdD]?\\b" to numberStyle,
        keywords(
            "fun", "val", "var", "class", "object", "interface", "return", "if", "else",
            "when", "for", "while", "do", "try", "catch", "finally", "throw", "throws",
            "new", "this", "super", "null", "true", "false", "import", "package",
            "public", "private", "protected", "internal", "override", "suspend",
            "companion", "data", "sealed", "enum", "static", "final", "abstract",
            "void", "int", "long", "double", "float", "boolean", "char", "byte", "short",
            "extends", "implements", "instanceof", "in", "is", "as", "by", "init",
            "constructor", "get", "set", "lateinit", "const", "inline", "reified",
            "typealias", "vararg", "out", "break", "continue", "else", "super", "this"
        ) to keywordStyle,
        "\\b[A-Za-z_][A-Za-z0-9_]*(?=\\s*\\()" to functionStyle
    )

    private val jsRules = rules(
        "//[^\n]*" to commentStyle,
        "/\\*(?:.|\\n)*?\\*/" to commentStyle,
        "\"(?:\\\\.|[^\"\\\\\\n])*\"" to stringStyle,
        "'(?:\\\\.|[^'\\\\\\n])*'" to stringStyle,
        "`(?:\\\\.|[^`\\\\])*`" to stringStyle,
        "\\b\\d[\\d_]*(?:\\.[\\d_]+)?\\b" to numberStyle,
        keywords(
            "function", "const", "let", "var", "return", "if", "else", "for", "while",
            "do", "switch", "case", "default", "break", "continue", "try", "catch",
            "finally", "throw", "new", "delete", "typeof", "instanceof", "in", "of",
            "class", "extends", "super", "this", "import", "export", "from", "async",
            "await", "yield", "null", "undefined", "true", "false", "void", "static", "get", "set"
        ) to keywordStyle,
        "\\b[A-Za-z_$][A-Za-z0-9_$]*(?=\\s*\\()" to functionStyle
    )

    private val pythonRules = rules(
        "#[^\n]*" to commentStyle,
        "\"\"\"(?:.|\\n)*?\"\"\"" to stringStyle,
        "'''(?:.|\\n)*?'''" to stringStyle,
        "\"(?:\\\\.|[^\"\\\\\\n])*\"" to stringStyle,
        "'(?:\\\\.|[^'\\\\\\n])*'" to stringStyle,
        "\\b\\d[\\d_]*(?:\\.[\\d_]+)?\\b" to numberStyle,
        keywords(
            "def", "class", "return", "if", "elif", "else", "for", "while", "try",
            "except", "finally", "raise", "import", "from", "as", "pass", "break",
            "continue", "with", "lambda", "yield", "global", "nonlocal", "assert",
            "del", "in", "is", "not", "and", "or", "None", "True", "False", "async", "await", "print"
        ) to keywordStyle,
        "\\b[A-Za-z_][A-Za-z0-9_]*(?=\\s*\\()" to functionStyle
    )

    private val jsonRules = rules(
        "\"(?:\\\\.|[^\"\\\\\\n])*\"(?=\\s*:)" to keywordStyle,
        "\"(?:\\\\.|[^\"\\\\\\n])*\"" to stringStyle,
        "-?\\b\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b" to numberStyle,
        "\\b(?:true|false|null)\\b" to keywordStyle
    )

    private val markupRules = rules(
        "<!--(?:.|\\n)*?-->" to commentStyle,
        "</?[A-Za-z][A-Za-z0-9-]*" to keywordStyle,
        "/?>" to keywordStyle,
        "\\b[A-Za-z-]+(?==)" to functionStyle,
        "\"(?:\\\\.|[^\"\\\\\\n])*\"" to stringStyle,
        "'(?:\\\\.|[^'\\\\\\n])*'" to stringStyle
    )

    private val bashRules = rules(
        "#[^\n]*" to commentStyle,
        "\"(?:\\\\.|[^\"\\\\\\n])*\"" to stringStyle,
        "'(?:\\\\.|[^'\\\\\\n])*'" to stringStyle,
        "\\$\\{?[A-Za-z_][A-Za-z0-9_]*\\}?" to numberStyle,
        keywords(
            "if", "then", "else", "elif", "fi", "for", "while", "until", "do", "done",
            "case", "esac", "in", "function", "return", "local", "export", "echo",
            "cd", "ls", "mkdir", "rm", "cp", "mv", "cat", "grep", "sed", "awk",
            "chmod", "chown", "sudo", "curl", "wget", "tar", "unzip", "source", "set", "exit"
        ) to keywordStyle
    )

    private val cssRules = rules(
        "/\\*(?:.|\\n)*?\\*/" to commentStyle,
        "\"(?:\\\\.|[^\"\\\\\\n])*\"" to stringStyle,
        "'(?:\\\\.|[^'\\\\\\n])*'" to stringStyle,
        "[.#]?[A-Za-z-][A-Za-z0-9-]*(?=\\s*[{,])" to functionStyle,
        "\\b\\d+(?:\\.\\d+)?(?:px|em|rem|%|vh|vw|s|ms|deg|fr)?\\b" to numberStyle,
        "\\b[a-zA-Z-]+(?=\\s*:)" to keywordStyle
    )

    private val sqlRules = rules(
        "--[^\n]*" to commentStyle,
        "'(?:''|[^'\\n])*'" to stringStyle,
        "\\b\\d+(?:\\.\\d+)?\\b" to numberStyle,
        Regex("select|insert|update|delete|from|where|join|left|right|inner|outer|on|group|order|by|having|limit|offset|create|table|alter|drop|index|values|set|into|and|or|not|null|primary|key|foreign|references|unique|default|as|distinct|union|all|exists|like|in|between|is", RegexOption.IGNORE_CASE).pattern to keywordStyle
    )

    private fun rulesFor(language: String?): LanguageRules? {
        return when (language?.trim()?.lowercase()) {
            "kotlin", "kt", "kts", "java", "scala", "c", "cpp", "c++", "cs", "csharp",
            "go", "rust", "swift", "dart", "groovy" -> cLikeRules
            "js", "jsx", "javascript", "ts", "tsx", "typescript", "vue", "node" -> jsRules
            "py", "python", "python3" -> pythonRules
            "json", "jsonc" -> jsonRules
            "html", "xml", "xhtml", "svg", "vue-html", "md-html" -> markupRules
            "sh", "bash", "zsh", "shell", "console", "terminal" -> bashRules
            "css", "scss", "sass", "less" -> cssRules
            "sql", "sqlite", "mysql", "postgres", "postgresql" -> sqlRules
            else -> null
        }
    }

    /** Visible for tests: the palette used for a token kind. */
    internal val palette: Map<String, Color>
        get() = mapOf(
            "keyword" to AppColors.CodeKeyword,
            "string" to AppColors.CodeString,
            "comment" to AppColors.CodeComment,
            "number" to AppColors.CodeNumber,
            "function" to AppColors.CodeFunction
        )
}
