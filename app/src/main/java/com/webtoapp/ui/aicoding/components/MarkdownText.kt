package com.webtoapp.ui.aicoding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }
    val codeBg = MaterialTheme.colorScheme.surfaceVariant
    val codeFg = MaterialTheme.colorScheme.onSurfaceVariant
    val linkColor = MaterialTheme.colorScheme.primary
    val quoteBar = MaterialTheme.colorScheme.outline

    SelectionContainer {
        Column(modifier = modifier.fillMaxWidth()) {
            blocks.forEachIndexed { index, block ->
                if (index > 0) Spacer(Modifier.height(blockGap(block)))
                RenderBlock(
                    block = block,
                    color = color,
                    codeBg = codeBg,
                    codeFg = codeFg,
                    linkColor = linkColor,
                    quoteBar = quoteBar
                )
            }
        }
    }
}

private fun blockGap(block: MdBlock): androidx.compose.ui.unit.Dp = when (block) {
    is MdBlock.ListItem -> 2.dp
    else -> 6.dp
}

@Composable
private fun RenderBlock(
    block: MdBlock,
    color: Color,
    codeBg: Color,
    codeFg: Color,
    linkColor: Color,
    quoteBar: Color
) {
    when (block) {
        is MdBlock.Heading -> {
            val style = when (block.level) {
                1 -> MaterialTheme.typography.titleLarge
                2 -> MaterialTheme.typography.titleMedium
                3 -> MaterialTheme.typography.titleSmall
                else -> MaterialTheme.typography.bodyLarge
            }
            androidx.compose.material3.Text(
                text = inlineAnnotated(block.text, codeBg, codeFg, linkColor),
                style = style,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.fillMaxWidth()
            )
        }

        is MdBlock.Paragraph -> {
            androidx.compose.material3.Text(
                text = inlineAnnotated(block.text, codeBg, codeFg, linkColor),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                modifier = Modifier.fillMaxWidth()
            )
        }

        is MdBlock.ListItem -> {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.width((block.indent * 16).dp))
                androidx.compose.material3.Text(
                    text = block.marker,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
                Spacer(Modifier.width(6.dp))
                androidx.compose.material3.Text(
                    text = inlineAnnotated(block.text, codeBg, codeFg, linkColor),
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        is MdBlock.Quote -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .background(quoteBar)
                )
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.Text(
                    text = inlineAnnotated(block.text, codeBg, codeFg, linkColor),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = color.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        MdBlock.Rule -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(quoteBar)
            )
        }
    }
}

internal sealed class MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock()
    data class Paragraph(val text: String) : MdBlock()

    data class ListItem(val marker: String, val text: String, val indent: Int) : MdBlock()
    data class Quote(val text: String) : MdBlock()
    object Rule : MdBlock()
}

private val ORDERED_LIST = Regex("^(\\s*)(\\d+)[.)]\\s+(.*)$")
private val UNORDERED_LIST = Regex("^(\\s*)[-*+]\\s+(.*)$")
private val HEADING = Regex("^(#{1,6})\\s+(.*)$")
private val RULE = Regex("^(-{3,}|\\*{3,}|_{3,})$")

internal fun parseMarkdownBlocks(text: String): List<MdBlock> {
    val trimmed = text.trim('\n')
    if (trimmed.isBlank()) {
        return if (text.isEmpty()) emptyList() else listOf(MdBlock.Paragraph(text))
    }
    val out = mutableListOf<MdBlock>()
    val lines = trimmed.split("\n")
    val paragraph = StringBuilder()

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            out += MdBlock.Paragraph(paragraph.toString().trim())
            paragraph.clear()
        }
    }

    for (raw in lines) {
        val line = raw.trimEnd()
        when {
            line.isBlank() -> flushParagraph()

            RULE.matches(line.trim()) -> {
                flushParagraph()
                out += MdBlock.Rule
            }

            HEADING.matchEntire(line) != null -> {
                flushParagraph()
                val m = HEADING.matchEntire(line)!!
                out += MdBlock.Heading(m.groupValues[1].length, m.groupValues[2].trim())
            }

            UNORDERED_LIST.matchEntire(line) != null -> {
                flushParagraph()
                val m = UNORDERED_LIST.matchEntire(line)!!
                val indent = m.groupValues[1].length / 2
                out += MdBlock.ListItem("•", m.groupValues[2].trim(), indent)
            }

            ORDERED_LIST.matchEntire(line) != null -> {
                flushParagraph()
                val m = ORDERED_LIST.matchEntire(line)!!
                val indent = m.groupValues[1].length / 2
                out += MdBlock.ListItem("${m.groupValues[2]}.", m.groupValues[3].trim(), indent)
            }

            line.trimStart().startsWith(">") -> {
                flushParagraph()
                out += MdBlock.Quote(line.trimStart().removePrefix(">").trim())
            }

            else -> {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(line.trim())
            }
        }
    }
    flushParagraph()
    return out
}

internal fun inlineAnnotated(
    text: String,
    codeBg: Color,
    codeFg: Color,
    linkColor: Color
): AnnotatedString = buildAnnotatedString {
    var i = 0
    val n = text.length
    while (i < n) {
        val c = text[i]
        when {

            c == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end > i) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = codeBg,
                            color = codeFg,
                            fontSize = 13.sp
                        )
                    ) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else {
                    append(c); i++
                }
            }

            c == '*' && i + 1 < n && text[i + 1] == '*' -> {
                val end = text.indexOf("**", i + 2)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        appendInlineInner(text.substring(i + 2, end), codeBg, codeFg, linkColor)
                    }
                    i = end + 2
                } else {
                    append(c); i++
                }
            }

            (c == '*' || c == '_') -> {
                val end = text.indexOf(c, i + 1)
                if (end > i && end != i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(c); i++
                }
            }

            c == '~' && i + 1 < n && text[i + 1] == '~' -> {
                val end = text.indexOf("~~", i + 2)
                if (end > i + 1) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(c); i++
                }
            }

            c == '[' -> {
                val close = text.indexOf(']', i + 1)
                if (close > i && close + 1 < n && text[close + 1] == '(') {
                    val urlEnd = text.indexOf(')', close + 2)
                    if (urlEnd > close + 1) {
                        val label = text.substring(i + 1, close)
                        withStyle(
                            SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        ) { append(label) }
                        i = urlEnd + 1
                    } else {
                        append(c); i++
                    }
                } else {
                    append(c); i++
                }
            }

            else -> {
                append(c); i++
            }
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendInlineInner(
    text: String,
    codeBg: Color,
    codeFg: Color,
    linkColor: Color
) {
    var i = 0
    val n = text.length
    while (i < n) {
        val c = text[i]
        if (c == '`') {
            val end = text.indexOf('`', i + 1)
            if (end > i) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBg,
                        color = codeFg,
                        fontSize = 13.sp
                    )
                ) { append(text.substring(i + 1, end)) }
                i = end + 1
                continue
            }
        }
        append(c); i++
    }
}
