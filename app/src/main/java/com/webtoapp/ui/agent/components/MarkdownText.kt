package com.webtoapp.ui.agent.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
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

        is MdBlock.Table -> {
            // Horizontally scrollable so wide tables never squash the chat column.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                TableRow(cells = block.header, header = true, zebra = false, color = color, codeBg = codeBg, codeFg = codeFg, linkColor = linkColor)
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .background(quoteBar.copy(alpha = 0.6f))
                        .fillMaxWidth()
                )
                block.rows.forEachIndexed { index, row ->
                    TableRow(cells = row, header = false, zebra = index % 2 == 1, color = color, codeBg = codeBg, codeFg = codeFg, linkColor = linkColor)
                }
            }
        }
    }
}

@Composable
private fun TableRow(
    cells: List<String>,
    header: Boolean,
    zebra: Boolean,
    color: Color,
    codeBg: Color,
    codeFg: Color,
    linkColor: Color
) {
    Row(
        modifier = Modifier.background(
            if (zebra) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else Color.Transparent
        )
    ) {
        cells.forEach { cell ->
            androidx.compose.material3.Text(
                text = inlineAnnotated(cell, codeBg, codeFg, linkColor),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (header) FontWeight.Bold else null,
                color = color,
                modifier = Modifier
                    .widthIn(min = 64.dp, max = 260.dp)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
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

    /** GFM pipe table: header row plus body rows; column count follows the header. */
    data class Table(val header: List<String>, val rows: List<List<String>>) : MdBlock()
}

private val ORDERED_LIST = Regex("^(\\s*)(\\d+)[.)]\\s+(.*)$")
private val UNORDERED_LIST = Regex("^(\\s*)[-*+]\\s+(.*)$")
private val HEADING = Regex("^(#{1,6})\\s+(.*)$")
private val RULE = Regex("^(-{3,}|\\*{3,}|_{3,})$")

/** Table separator row like `|---|---|` or `| :--- | ---: |` (alignment colons allowed). */
private val TABLE_SEPARATOR = Regex("^\\|?[\\s:|-]+\\|[\\s:|-]*$")

private fun isTableRow(line: String): Boolean {
    val t = line.trim()
    return t.startsWith("|") && t.endsWith("|") && t.count { it == '|' } >= 2
}

private fun splitTableRow(line: String): List<String> =
    line.trim().trim('|').split('|').map { it.trim() }

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

    var i = 0
    while (i < lines.size) {
        val raw = lines[i]
        val line = raw.trimEnd()
        when {
            line.isBlank() -> flushParagraph()

            // GFM table: header row, separator row, then any number of body rows.
            isTableRow(line) && i + 1 < lines.size && TABLE_SEPARATOR.matches(lines[i + 1].trimEnd().trim()) -> {
                flushParagraph()
                val header = splitTableRow(line)
                val rows = mutableListOf<List<String>>()
                var j = i + 2
                while (j < lines.size && isTableRow(lines[j].trimEnd())) {
                    val cells = splitTableRow(lines[j].trimEnd())
                    // Normalise to the header width: pad or trim.
                    rows += List(header.size) { idx -> cells.getOrElse(idx) { "" } }
                    j++
                }
                out += MdBlock.Table(header, rows)
                i = j
                continue
            }

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
        i++
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
                        val url = text.substring(close + 2, urlEnd).trim()
                        withLink(LinkAnnotation.Url(url)) {
                            withStyle(
                                SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) { append(label) }
                        }
                        i = urlEnd + 1
                    } else {
                        append(c); i++
                    }
                } else {
                    append(c); i++
                }
            }

            c == 'h' && (text.startsWith("https://", i) || text.startsWith("http://", i)) -> {
                // Bare URL auto-link: consume until whitespace or a closing bracket.
                var end = i
                while (end < n && !text[end].isWhitespace() && text[end] != ')' && text[end] != ']') end++
                val url = text.substring(i, end).trimEnd('.', ',', ';', ':', '!', '?')
                withLink(LinkAnnotation.Url(url)) {
                    withStyle(
                        SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        )
                    ) { append(url) }
                }
                i += url.length
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
