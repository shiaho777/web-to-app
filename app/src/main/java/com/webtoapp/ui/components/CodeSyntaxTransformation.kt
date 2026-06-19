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

    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = when (language) {
            "JavaScript", "JS", "Javascript" -> highlightJs(text)
            "CSS" -> highlightCss(text)
            else -> return TransformedText(text, OffsetMapping.Identity)
        }
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

        Regex("""//[^\n]*|/\*[\s\S]*?\*/""").findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.comment)))
        }

        Regex(""""(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*'|`(?:[^`\\]|\\.)*`""").findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.string)))
        }

        Regex("""\b(function|const|let|var|if|else|return|for|while|do|switch|case|break|continue|try|catch|finally|throw|new|class|extends|super|this|typeof|instanceof|in|of|void|delete|yield|async|await|import|export|default|from|window|document|console|true|false|null|undefined)\b""").findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.keyword)))
        }

        Regex("""\b\d+(?:\.\d+)?\b""").findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.number)))
        }

        Regex("""\b[a-zA-Z_$][\w$]*(?=\s*\()""").findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.function)))
        }

        return build(text, ranges)
    }

    private fun highlightCss(text: AnnotatedString): AnnotatedString {
        val ranges = mutableListOf<Range>()
        val src = text.text

        Regex("""/\*[\s\S]*?\*/""").findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.comment)))
        }

        Regex("""@[\w-]+""").findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.keyword)))
        }

        Regex("""[\w-]+(?=\s*:)""").findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.keyword)))
        }

        Regex(""""[^"]*"|'[^']*'""").findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.string)))
        }

        Regex("""\b\d+(?:\.\d+)?(?:px|em|rem|vh|vw|%|s|ms|deg|fr|pt)?\b""").findAll(src).forEach { m ->
            ranges.add(Range(m.range.first, m.range.last + 1, SpanStyle(color = scheme.number)))
        }

        return build(text, ranges)
    }
}
