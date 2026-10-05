package com.webtoapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.webtoapp.ui.theme.AppColors
import com.webtoapp.ui.theme.LocalIsDarkTheme

data class EditorColorScheme(
    val background: Color,
    val backgroundAlt: Color,
    val foreground: Color,
    val gutter: Color,
    val muted: Color,
    val divider: Color,
    val keyword: Color,
    val string: Color,
    val comment: Color,
    val number: Color,
    val function: Color
)

@Composable
fun rememberEditorColorScheme(): EditorColorScheme {
    val isDark = LocalIsDarkTheme.current
    return if (isDark) {
        EditorColorScheme(
            background = AppColors.EditorDark,
            backgroundAlt = AppColors.EditorDarkAlt,
            foreground = AppColors.CodeForeground,
            gutter = AppColors.CodeGutter,
            muted = AppColors.CodeMuted,
            divider = AppColors.CodeDivider,
            keyword = AppColors.CodeKeyword,
            string = AppColors.CodeString,
            comment = AppColors.CodeComment,
            number = AppColors.CodeNumber,
            function = AppColors.CodeFunction
        )
    } else {
        EditorColorScheme(
            background = AppColors.EditorLight,
            backgroundAlt = AppColors.EditorLightAlt,
            foreground = AppColors.CodeForegroundLight,
            gutter = AppColors.CodeGutterLight,
            muted = AppColors.CodeMutedLight,
            divider = AppColors.CodeDividerLight,
            keyword = AppColors.CodeKeywordLight,
            string = AppColors.CodeStringLight,
            comment = AppColors.CodeCommentLight,
            number = AppColors.CodeNumberLight,
            function = AppColors.CodeFunctionLight
        )
    }
}

class CodeSyntaxTransformation(
    private val language: String,
    private val scheme: EditorColorScheme
) : VisualTransformation {

    private var cachedSource: String? = null
    private var cachedHighlighted: AnnotatedString? = null

    override fun filter(text: AnnotatedString): TransformedText {
        val src = text.text
        if (src.length > MAX_HIGHLIGHT_CHARS) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val cached = cachedHighlighted
        if (cached != null && cachedSource == src) {
            return TransformedText(cached, OffsetMapping.Identity)
        }
        val highlighted = when (language.lowercase()) {
            "javascript", "js" -> highlightJs(text)
            "css" -> highlightCss(text)
            "html", "htm" -> highlightHtml(text)
            else -> text
        }
        cachedSource = src
        cachedHighlighted = highlighted
        return TransformedText(highlighted, OffsetMapping.Identity)
    }

    private data class Range(val start: Int, val end: Int, val style: SpanStyle)

    private fun build(text: AnnotatedString, ranges: List<Range>): AnnotatedString {
        if (ranges.isEmpty()) return text
        val sorted = ranges.sortedBy { it.start }
        val resolved = mutableListOf<Range>()
        var lastEnd = 0
        for (r in sorted) {
            if (r.start < lastEnd) continue
            resolved.add(r)
            lastEnd = r.end
        }
        return buildAnnotatedString {
            append(text)
            for (r in resolved) {
                addStyle(r.style, r.start, r.end)
            }
        }
    }

    private fun highlightJs(text: AnnotatedString): AnnotatedString {
        val ranges = mutableListOf<Range>()
        val src = text.text

        JS_COMMENT.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.comment)))
        }

        JS_STRING.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.string)))
        }

        JS_KEYWORD.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.keyword)))
        }

        JS_NUMBER.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.number)))
        }

        JS_CALL.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.function)))
        }

        return build(text, ranges)
    }

    private fun highlightCss(text: AnnotatedString): AnnotatedString {
        val ranges = mutableListOf<Range>()
        val src = text.text

        CSS_COMMENT.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.comment)))
        }

        CSS_AT.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.keyword)))
        }

        CSS_PROPERTY.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.keyword)))
        }

        CSS_STRING.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.string)))
        }

        CSS_NUMBER.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.number)))
        }

        return build(text, ranges)
    }

    private fun highlightHtml(text: AnnotatedString): AnnotatedString {
        val ranges = mutableListOf<Range>()
        val src = text.text

        HTML_COMMENT.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.comment)))
        }

        HTML_TAG.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.keyword)))
        }

        HTML_ATTR.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.function)))
        }

        HTML_STRING.findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.string)))
        }

        return build(text, ranges)
    }

    private companion object {
        const val MAX_HIGHLIGHT_CHARS = 40_000
        val JS_COMMENT = Regex("""//[^\n]*|/\*[\s\S]*?\*/""")
        val JS_STRING = Regex(""""(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'|`(?:[^`\\]|\\.)*`""")
        val JS_KEYWORD = Regex("""\b(function|const|let|var|if|else|return|for|while|do|switch|case|break|continue|try|catch|finally|throw|new|class|extends|super|this|typeof|instanceof|in|of|void|delete|yield|async|await|import|export|default|from|window|document|console|true|false|null|undefined)\b""")
        val JS_NUMBER = Regex("""\b\d+(?:\.\d+)?\b""")
        val JS_CALL = Regex("""\b[a-zA-Z_$][\w$]*(?=\s*\()""")
        val CSS_COMMENT = Regex("""/\*[\s\S]*?\*/""")
        val CSS_AT = Regex("""@[\w-]+""")
        val CSS_PROPERTY = Regex("""[\w-]+(?=\s*:)""")
        val CSS_STRING = Regex(""""[^"]*"|'[^']*'""")
        val CSS_NUMBER = Regex("""\b\d+(?:\.\d+)?(?:px|em|rem|vh|vw|%|s|ms|deg|fr|pt)?\b""")
        val HTML_COMMENT = Regex("""<!--[\s\S]*?-->""")
        val HTML_TAG = Regex("""</?[A-Za-z][\w:-]*""")
        val HTML_ATTR = Regex("""(?<=\s)[A-Za-z_:][\w:.-]*(?=\s*=)""")
        val HTML_STRING = Regex(""""[^"]*"|'[^']*'""")
    }
}
