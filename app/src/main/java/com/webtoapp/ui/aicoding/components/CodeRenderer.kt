package com.webtoapp.ui.aicoding.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.theme.AppColors

internal data class TextSegment(
    val isCode: Boolean,
    val text: String,
    val language: String? = null
)

internal fun parseTextSegments(text: String): List<TextSegment> {
    if (text.isEmpty()) return emptyList()
    val out = mutableListOf<TextSegment>()
    var i = 0
    val n = text.length
    while (i < n) {
        val fence = text.indexOf("```", i)
        if (fence < 0) {
            out += TextSegment(false, text.substring(i))
            break
        }
        if (fence > i) out += TextSegment(false, text.substring(i, fence))
        val langStart = fence + 3
        val nlAfterLang = text.indexOf('\n', langStart)
        val (langEnd, codeStart) = if (nlAfterLang < 0) langStart to langStart
        else nlAfterLang to (nlAfterLang + 1)
        val lang = text.substring(langStart, langEnd).trim().takeIf { it.isNotEmpty() }
        val close = text.indexOf("```", codeStart)
        if (close < 0) {
            out += TextSegment(true, text.substring(codeStart), lang)
            return out
        }
        out += TextSegment(true, text.substring(codeStart, close), lang)
        i = close + 3
        if (i < n && text[i] == '\n') i++
    }
    return out
}

@Composable
fun RichAssistantText(
    text: String,
    onSurface: Color,
    streamingCaret: Boolean = false,
    modifier: Modifier = Modifier
) {
    val segments = remember(text) { parseTextSegments(text) }
    Column(modifier = modifier.fillMaxWidth()) {
        segments.forEachIndexed { idx, seg ->
            val isLast = idx == segments.lastIndex
            if (seg.isCode) {
                if (idx > 0) Spacer(Modifier.size(WtaSpacing.ContentGap))
                CodeBlock(
                    code = seg.text.trimEnd('\n'),
                    language = seg.language,
                    showCaret = streamingCaret && isLast
                )
                Spacer(Modifier.size(WtaSpacing.Tiny))
            } else if (seg.text.isNotEmpty()) {

                val display = if (streamingCaret && isLast) seg.text + "▋" else seg.text
                MarkdownText(
                    text = display,
                    color = onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CodeBlock(
    code: String,
    language: String?,
    showCaret: Boolean = false,
    maxHeightDp: Int = 320
) {
    val clipboard = LocalClipboardManager.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.EditorDark,
        shape = RoundedCornerShape(WtaRadius.IconPlate)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WtaSpacing.Medium, vertical = WtaSpacing.Tiny),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (language ?: "code").lowercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.CodeGutter,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
                WtaIconButton(
                    onClick = { clipboard.setText(AnnotatedString(code)) },
                    icon = Icons.Outlined.ContentCopy,
                    contentDescription = Strings.aiCodingCodeCopy,
                    modifier = Modifier.size(28.dp)
                )
            }

            val hScroll = rememberScrollState()
            val streamingMode = showCaret
            val bodyModifier = if (streamingMode) {
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(hScroll)
                    .padding(horizontal = WtaSpacing.Medium, vertical = WtaSpacing.Small + 2.dp)
            } else {
                val vScroll = rememberScrollState()
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeightDp.dp)
                    .verticalScroll(vScroll)
                    .horizontalScroll(hScroll)
                    .padding(horizontal = WtaSpacing.Medium, vertical = WtaSpacing.Small + 2.dp)
            }
            Box(modifier = bodyModifier) {
                SelectionContainer {
                    Text(
                        text = if (showCaret) "$code▋" else code,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = AppColors.CodeForeground
                        )
                    )
                }
            }
        }
    }
}
