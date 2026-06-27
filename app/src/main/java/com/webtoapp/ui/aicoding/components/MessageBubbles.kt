package com.webtoapp.ui.aicoding.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonParser
import com.webtoapp.core.aicoding.session.AgentMessage
import com.webtoapp.core.aicoding.session.RecordedToolCall
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.theme.AppColors

data class MessageActions(
    val onCopy: (AgentMessage) -> Unit,
    val onEdit: (AgentMessage) -> Unit,
    val onRegenerate: (AgentMessage) -> Unit,
    val onDeleteFromHere: (AgentMessage) -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: AgentMessage,
    actions: MessageActions,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == AgentMessage.Role.USER
    val tone = when {
        message.isError -> WtaCardTone.Critical
        isUser -> WtaCardTone.Highlighted
        else -> WtaCardTone.Surface
    }
    val onContainer = when {
        message.isError -> MaterialTheme.colorScheme.onErrorContainer
        isUser -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!message.thinking.isNullOrBlank()) {
            ThinkingBlock(
                content = message.thinking,
                durationMs = message.thinkingDurationMs,
                isLive = false,
                modifier = Modifier.widthIn(max = 560.dp)
            )
            Spacer(Modifier.height(WtaSpacing.Tiny))
        }
        WtaCard(
            tone = tone,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = WtaSpacing.Medium + 2.dp,
                vertical = WtaSpacing.Medium - 2.dp
            ),
            modifier = Modifier.widthIn(max = 560.dp)
        ) {
            if (isUser) {
                SelectionContainer {
                    Text(
                        text = message.content.ifBlank { Strings.aiCodingNoOutput },
                        style = MaterialTheme.typography.bodyMedium,
                        color = onContainer
                    )
                }
                if (message.mentionedFiles.isNotEmpty()) {
                    Spacer(Modifier.height(WtaSpacing.Small))
                    MentionList(message.mentionedFiles)
                }
            } else {
                RichAssistantTimeline(
                    text = message.content,
                    toolCalls = message.toolCalls,
                    onSurface = onContainer,
                    liveTrailing = false
                )
            }
        }
        if (message.attachments.isNotEmpty()) {
            Spacer(Modifier.height(WtaSpacing.Tiny))
            AttachmentList(message.attachments)
        }
        Spacer(Modifier.height(WtaSpacing.Tiny))
        Box {
            WtaIconButton(
                onClick = { menuOpen = true },
                icon = Icons.Outlined.MoreHoriz,
                contentDescription = Strings.more,
                modifier = Modifier.size(WtaSize.TouchTarget)
            )
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(Strings.aiCodingMessageActionCopy) },
                    leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                    onClick = { menuOpen = false; actions.onCopy(message) }
                )
                if (isUser) {
                    DropdownMenuItem(
                        text = { Text(Strings.aiCodingMessageActionEdit) },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        onClick = { menuOpen = false; actions.onEdit(message) }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(Strings.aiCodingMessageActionRegenerate) },
                        leadingIcon = { Icon(Icons.Outlined.Refresh, contentDescription = null) },
                        onClick = { menuOpen = false; actions.onRegenerate(message) }
                    )
                }
                DropdownMenuItem(
                    text = { Text(Strings.aiCodingMessageActionDelete) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                    onClick = { menuOpen = false; actions.onDeleteFromHere(message) }
                )
            }
        }
    }
}

@Composable
fun ThinkingBlock(
    content: String,
    durationMs: Long?,
    isLive: Boolean,
    initiallyExpanded: Boolean = isLive,
    modifier: Modifier = Modifier
) {
    var expanded by remember(content.hashCode()) { mutableStateOf(initiallyExpanded) }
    LaunchedEffect(isLive) {
        if (!isLive) expanded = false
    }
    WtaCard(
        tone = WtaCardTone.Elevated,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = WtaSpacing.Small + 2.dp, vertical = WtaSpacing.Small - 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = if (isLive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(WtaSize.IconSmall)
            )
            Spacer(Modifier.width(WtaSpacing.Tiny + 2.dp))
            val header = when {
                isLive -> {
                    val secs = (durationMs ?: 0L) / 1000.0
                    if (secs > 0) "${Strings.aiCodingThinkingHeaderLive} · ${"%.1fs".format(secs)}"
                    else Strings.aiCodingThinkingHeaderLive
                }
                durationMs != null && durationMs > 0 ->
                    Strings.aiCodingThinkingDoneSeconds.format(durationMs / 1000.0)
                else -> Strings.aiCodingThinkingHeaderLive
            }
            Text(
                text = header,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (isLive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    strokeWidth = 1.2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(WtaSpacing.Tiny + 2.dp))
            }
            Text(
                text = if (expanded) Strings.aiCodingThinkingTapToCollapse
                else Strings.aiCodingThinkingTapToExpand,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WtaAlpha.Strong)
            )
            Spacer(Modifier.width(2.dp))
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(WtaSize.IconSmall - 2.dp)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {

            val bodyMod = if (isLive) {
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WtaSpacing.Small + 2.dp)
                    .padding(bottom = WtaSpacing.Small)
            } else {
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = WtaSpacing.Small + 2.dp)
                    .padding(bottom = WtaSpacing.Small)
            }
            Box(modifier = bodyMod) {
                SelectionContainer {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StreamingBubble(
    text: String,
    thinking: String,
    thinkingStartedAt: Long?,
    thinkingFrozenDurationMs: Long?,
    pendingTools: List<RecordedToolCall>,
    activity: String?
) {
    val isThinkingLive = thinkingFrozenDurationMs == null && text.isBlank() && thinking.isNotBlank()
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(isThinkingLive) {
        while (isThinkingLive) {
            nowMs = System.currentTimeMillis()
            kotlinx.coroutines.delay(100)
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        if (thinking.isNotBlank()) {
            val liveDurationMs = thinkingStartedAt?.let { nowMs - it }
            ThinkingBlock(
                content = thinking,
                durationMs = thinkingFrozenDurationMs ?: liveDurationMs,
                isLive = isThinkingLive,
                initiallyExpanded = isThinkingLive,
                modifier = Modifier.widthIn(max = 560.dp)
            )
            Spacer(Modifier.height(WtaSpacing.Tiny))
        }
        if (text.isNotBlank() || pendingTools.isNotEmpty() || thinking.isBlank()) {
            WtaCard(
                tone = WtaCardTone.Surface,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = WtaSpacing.Medium + 2.dp,
                    vertical = WtaSpacing.Medium - 2.dp
                ),
                modifier = Modifier.widthIn(max = 560.dp)
            ) {
                if (text.isNotEmpty() || pendingTools.isNotEmpty()) {

                    RichAssistantTimeline(
                        text = text,
                        toolCalls = pendingTools,
                        onSurface = MaterialTheme.colorScheme.onSurface,
                        liveTrailing = true
                    )
                }
                if (text.isBlank() && pendingTools.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(WtaSpacing.Small))
                        Text(
                            text = activity ?: Strings.aiCodingPhaseThinking,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private val INLINE_TOOL_MARKER = Regex("\u2063TC:([^\u2063]+)\u2063")

@Composable
fun RichAssistantTimeline(
    text: String,
    toolCalls: List<RecordedToolCall>,
    onSurface: Color,
    liveTrailing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val emptyHint = Strings.aiCodingNoOutput
    val toolsById = remember(toolCalls) { toolCalls.associateBy { it.toolCallId } }
    val seenIds = remember(text, toolCalls) { mutableSetOf<String>() }

    val segments = remember(text, toolCalls) {
        buildSegments(text, toolsById, seenIds)
    }

    val leftovers = remember(toolCalls, segments) {
        toolCalls.filter { it.toolCallId !in seenIds }
    }

    val proseLastIdx = segments.indexOfLast { it is TimelineSegment.Prose }

    Column(modifier = modifier.fillMaxWidth()) {
        if (segments.isEmpty() && leftovers.isEmpty()) {

            SelectionContainer {
                Text(
                    text = emptyHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurface
                )
            }
            return@Column
        }
        segments.forEachIndexed { i, seg ->
            when (seg) {
                is TimelineSegment.Prose -> {
                    val isLastProse = i == proseLastIdx
                    val proseText = if (liveTrailing && isLastProse && leftovers.isEmpty())
                        seg.text
                    else seg.text
                    if (proseText.isNotEmpty()) {
                        RichAssistantText(
                            text = proseText,
                            onSurface = onSurface,
                            streamingCaret = liveTrailing && isLastProse && leftovers.isEmpty()
                        )
                    }
                }
                is TimelineSegment.Tool -> {
                    if (i > 0) Spacer(Modifier.height(WtaSpacing.Tiny))
                    ToolCallCard(seg.tc, live = liveTrailing &&
                        seg.tc.resultPreview == RecordedToolCall.RUNNING_SENTINEL)
                    Spacer(Modifier.height(WtaSpacing.Tiny))
                }
            }
        }
        leftovers.forEach { tc ->
            Spacer(Modifier.height(WtaSpacing.Tiny))
            ToolCallCard(tc, live = liveTrailing &&
                tc.resultPreview == RecordedToolCall.RUNNING_SENTINEL)
        }
    }
}

private sealed class TimelineSegment {
    data class Prose(val text: String) : TimelineSegment()
    data class Tool(val tc: RecordedToolCall) : TimelineSegment()
}

private fun buildSegments(
    text: String,
    toolsById: Map<String, RecordedToolCall>,
    seenIds: MutableSet<String>
): List<TimelineSegment> {
    if (text.isEmpty()) return emptyList()
    val out = mutableListOf<TimelineSegment>()
    var cursor = 0
    INLINE_TOOL_MARKER.findAll(text).forEach { match ->
        val before = text.substring(cursor, match.range.first)
        if (before.isNotEmpty()) out += TimelineSegment.Prose(before)
        val id = match.groupValues[1]
        val tc = toolsById[id]
        if (tc != null) {
            out += TimelineSegment.Tool(tc)
            seenIds += id
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) {
        val tail = text.substring(cursor)
        if (tail.isNotEmpty()) out += TimelineSegment.Prose(tail)
    }
    return out
}

@Composable
fun ToolCallCard(tc: RecordedToolCall, live: Boolean) {
    val running = tc.resultPreview == RecordedToolCall.RUNNING_SENTINEL
    val (icon, tint) = when {
        running -> Icons.Outlined.HourglassTop to MaterialTheme.colorScheme.primary
        tc.ok -> Icons.Outlined.CheckCircle to WtaColors.semantic.success
        else -> Icons.Outlined.ErrorOutline to MaterialTheme.colorScheme.error
    }
    var expanded by remember(tc.toolCallId) { mutableStateOf(live && running) }
    LaunchedEffect(running) {
        if (!running && live) expanded = false
    }
    WtaCard(
        tone = WtaCardTone.Elevated,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = WtaSpacing.Small + 2.dp, vertical = WtaSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(WtaSize.IconSmall)
            )
            Spacer(Modifier.width(WtaSpacing.Small))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tc.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val subtitle = subtitleFor(tc)
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            if (running) {
                Text(
                    text = Strings.aiCodingToolStreaming,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(WtaSpacing.Tiny + 2.dp))
            }
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(WtaSize.IconSmall)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WtaSpacing.Small + 2.dp)
                    .padding(bottom = WtaSpacing.Small + 2.dp)
            ) {
                val handled = SpecialToolBody(tc, running = running)
                if (!handled) {
                    if (tc.argumentsJson.isNotBlank()) {
                        ToolBlockLabel(Strings.aiCodingToolArgsLabel)
                        Spacer(Modifier.height(2.dp))
                        ToolMonoBlock(prettyJsonOrRaw(tc.argumentsJson))
                    }
                    if (running) {
                        Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                        ToolBlockLabel(Strings.aiCodingToolResultLabel)
                        Spacer(Modifier.height(2.dp))
                        if (tc.resultPreview.isNotBlank()) {
                            ToolMonoBlock(text = tc.resultPreview, spinner = true)
                        } else {
                            ToolMonoBlock(
                                text = (tc.activity ?: Strings.aiCodingPhaseToolRunning),
                                spinner = true
                            )
                        }
                    } else if (tc.resultPreview.isNotBlank()) {
                        Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                        ToolBlockLabel(Strings.aiCodingToolResultLabel)
                        Spacer(Modifier.height(2.dp))
                        ToolMonoBlock(text = tc.resultPreview)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialToolBody(tc: RecordedToolCall, running: Boolean): Boolean {
    val args = runCatching {
        JsonParser.parseString(tc.argumentsJson).asJsonObject
    }.getOrNull()

    return when (tc.name) {
        "Write" -> {
            val path = args?.get("path")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val content = args?.get("content")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            if (path.isEmpty() && content.isEmpty()) return false
            ToolPathHeader(path = path, kind = ToolKind.Write)
            if (content.isNotEmpty() || running) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                CodeBlock(
                    code = content,
                    language = languageFor(path),
                    showCaret = running,
                    maxHeightDp = 280
                )
            }
            if (!running && tc.resultPreview.isNotBlank()) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                Text(
                    text = tc.resultPreview,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            true
        }
        "Edit" -> {
            val path = args?.get("path")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val oldStr = args?.get("old_string")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val newStr = args?.get("new_string")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            if (path.isEmpty() && oldStr.isEmpty() && newStr.isEmpty()) return false
            val diffLines = if (oldStr.isNotEmpty() && newStr.isNotEmpty() && !running) {
                com.webtoapp.core.aicoding.diff.LineDiff.diff(oldStr, newStr)
            } else null
            val stats = diffLines?.let { com.webtoapp.core.aicoding.diff.LineDiff.stats(it) }
            ToolPathHeader(path = path, kind = ToolKind.Edit, stats = stats)

            if (diffLines != null) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                UnifiedDiffBlock(
                    lines = diffLines,
                    language = languageFor(path),
                    maxHeightDp = 320
                )
            } else {
                if (oldStr.isNotEmpty()) {
                    Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                    ToolBlockLabel("- Removed")
                    Spacer(Modifier.height(2.dp))
                    CodeBlock(code = oldStr, language = languageFor(path), maxHeightDp = 160)
                }
                if (newStr.isNotEmpty() || running) {
                    Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                    ToolBlockLabel("+ Added")
                    Spacer(Modifier.height(2.dp))
                    CodeBlock(
                        code = newStr,
                        language = languageFor(path),
                        showCaret = running,
                        maxHeightDp = 220
                    )
                }
            }
            if (!running && tc.resultPreview.isNotBlank()) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                Text(
                    text = tc.resultPreview,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            true
        }
        "Read" -> {
            val path = args?.get("path")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            if (path.isEmpty()) return false
            ToolPathHeader(path = path, kind = ToolKind.Read)
            if (running && tc.resultPreview.isBlank()) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                ToolMonoBlock(
                    text = tc.activity ?: Strings.aiCodingPhaseToolRunning,
                    spinner = true
                )
            } else if (tc.resultPreview.isNotBlank()) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                CodeBlock(
                    code = tc.resultPreview,
                    language = languageFor(path),
                    showCaret = running,
                    maxHeightDp = 280
                )
            }
            true
        }
        "Delete" -> {
            val path = args?.get("path")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            if (path.isEmpty()) return false
            ToolPathHeader(path = path, kind = ToolKind.Delete)
            if (!running && tc.resultPreview.isNotBlank()) {
                Spacer(Modifier.height(WtaSpacing.Tiny))
                Text(
                    text = tc.resultPreview,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            true
        }
        else -> false
    }
}

private enum class ToolKind { Write, Edit, Read, Delete }

@Composable
private fun ToolPathHeader(
    path: String,
    kind: ToolKind,
    stats: com.webtoapp.core.aicoding.diff.LineDiff.Stats? = null
) {
    val (label, container, content) = when (kind) {
        ToolKind.Write -> Triple(
            "WRITE",
            WtaColors.semantic.successContainer,
            WtaColors.semantic.onSuccessContainer
        )
        ToolKind.Edit -> Triple(
            "EDIT",
            WtaColors.semantic.warningContainer,
            WtaColors.semantic.onWarningContainer
        )
        ToolKind.Read -> Triple(
            "READ",
            WtaColors.semantic.infoContainer,
            WtaColors.semantic.onInfoContainer
        )
        ToolKind.Delete -> Triple(
            "DELETE",
            WtaColors.semantic.errorContainer,
            WtaColors.semantic.onErrorContainer
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        WtaInfoChip(
            label = label,
            containerColor = container,
            contentColor = content
        )
        Spacer(Modifier.width(WtaSpacing.Small))
        SelectionContainer(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = path,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
        if (stats != null && (stats.added > 0 || stats.removed > 0)) {
            Spacer(Modifier.width(WtaSpacing.Small))
            Text(
                text = "+${stats.added}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = WtaColors.semantic.success,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(WtaSpacing.Tiny))
            Text(
                text = "-${stats.removed}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun languageFor(path: String): String? {
    val ext = path.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "kt" -> "kotlin"
        "java" -> "java"
        "js" -> "javascript"
        "ts" -> "typescript"
        "tsx" -> "tsx"
        "jsx" -> "jsx"
        "py" -> "python"
        "rb" -> "ruby"
        "go" -> "go"
        "rs" -> "rust"
        "php" -> "php"
        "swift" -> "swift"
        "html", "htm" -> "html"
        "css" -> "css"
        "scss", "sass" -> "scss"
        "json" -> "json"
        "md", "markdown" -> "markdown"
        "yml", "yaml" -> "yaml"
        "xml" -> "xml"
        "toml" -> "toml"
        "sh", "bash", "zsh" -> "bash"
        "sql" -> "sql"
        "vue" -> "vue"
        "svelte" -> "svelte"
        "" -> null
        else -> ext
    }
}

@Composable
private fun ToolBlockLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ToolMonoBlock(text: String, spinner: Boolean = false) {
    val clipboard = LocalClipboardManager.current
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.EditorDark,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            com.webtoapp.ui.design.WtaRadius.IconPlate
        )
    ) {
        Row(modifier = Modifier.padding(horizontal = WtaSpacing.Small + 2.dp, vertical = WtaSpacing.Small)) {
            if (spinner) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp).padding(top = 4.dp),
                    strokeWidth = 1.2.dp,
                    color = AppColors.CodeGutter
                )
                Spacer(Modifier.width(WtaSpacing.Small))
            }
            SelectionContainer(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = AppColors.CodeForeground
                    )
                )
            }
            if (!spinner && text.isNotBlank()) {
                Spacer(Modifier.width(WtaSpacing.Small))
                WtaIconButton(
                    onClick = { clipboard.setText(AnnotatedString(text)) },
                    icon = Icons.Outlined.ContentCopy,
                    contentDescription = Strings.aiCodingMessageActionCopy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun UnifiedDiffBlock(
    lines: List<com.webtoapp.core.aicoding.diff.LineDiff.Line>,
    @Suppress("UNUSED_PARAMETER") language: String?,
    maxHeightDp: Int
) {
    val clipboard = LocalClipboardManager.current
    val plain = remember(lines) {
        lines.joinToString("\n") { ln ->
            when (ln.kind) {
                com.webtoapp.core.aicoding.diff.LineDiff.Line.Kind.Added -> "+" + ln.text
                com.webtoapp.core.aicoding.diff.LineDiff.Line.Kind.Removed -> "-" + ln.text
                com.webtoapp.core.aicoding.diff.LineDiff.Line.Kind.Context -> " " + ln.text
            }
        }
    }
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.EditorDark,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            com.webtoapp.ui.design.WtaRadius.IconPlate
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeightDp.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(horizontal = WtaSpacing.Small, vertical = WtaSpacing.Small)) {
                    lines.forEach { ln ->
                        DiffLineRow(ln)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WtaSpacing.Small, vertical = WtaSpacing.Tiny),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                WtaIconButton(
                    onClick = { clipboard.setText(AnnotatedString(plain)) },
                    icon = Icons.Outlined.ContentCopy,
                    contentDescription = Strings.aiCodingMessageActionCopy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DiffLineRow(line: com.webtoapp.core.aicoding.diff.LineDiff.Line) {
    val (bg, fg, gutter) = when (line.kind) {
        com.webtoapp.core.aicoding.diff.LineDiff.Line.Kind.Added ->
            Triple(AppColors.DiffAddedBg, AppColors.DiffAddedFg, "+")
        com.webtoapp.core.aicoding.diff.LineDiff.Line.Kind.Removed ->
            Triple(AppColors.DiffRemovedBg, AppColors.DiffRemovedFg, "-")
        com.webtoapp.core.aicoding.diff.LineDiff.Line.Kind.Context ->
            Triple(Color.Transparent, AppColors.CodeGutter, " ")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = WtaSpacing.Tiny, vertical = 1.dp)
    ) {
        Text(
            text = gutter,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = fg
            ),
            modifier = Modifier.width(14.dp)
        )
        Text(
            text = line.text.ifEmpty { " " },
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = fg
            )
        )
    }
}

private fun subtitleFor(tc: RecordedToolCall): String {
    tc.activity?.takeIf { it.isNotBlank() }?.let { return it }
    val args = runCatching { JsonParser.parseString(tc.argumentsJson).asJsonObject }.getOrNull()
        ?: return ""
    val keys = listOf("path", "file", "filePath", "pattern", "query", "command", "url", "name")
    for (k in keys) {
        val v = args.get(k)?.takeIf { !it.isJsonNull }?.asString.orEmpty()
        if (v.isNotBlank()) return v
    }
    return ""
}

private fun prettyJsonOrRaw(json: String): String {
    if (json.isBlank()) return ""
    return runCatching {
        val el = JsonParser.parseString(json)
        com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(el)
    }.getOrElse { json }
}

@Composable
private fun MentionList(paths: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny + 2.dp)) {
        items(paths.take(8), key = { "@" + it }) { path ->
            WtaInfoChip(
                label = "@$path",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        if (paths.size > 8) {
            item("more") {
                WtaInfoChip(
                    label = "+${paths.size - 8}",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun AttachmentList(paths: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny + 2.dp)) {
        items(paths.take(8), key = { "att-$it" }) { path ->
            WtaInfoChip(
                label = path,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun TodoChecklist(todos: List<com.webtoapp.core.aicoding.todo.TodoManager.Item>) {
    if (todos.isEmpty()) return
    WtaCard(
        tone = WtaCardTone.Elevated,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(WtaSpacing.Medium)
    ) {
        Text(
            text = Strings.aiCodingTodoListHeader,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(WtaSpacing.Small - 2.dp))
        todos.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (icon, color) = when (item.status) {
                    com.webtoapp.core.aicoding.todo.TodoManager.Item.Status.Completed ->
                        Icons.Outlined.CheckCircle to WtaColors.semantic.success
                    com.webtoapp.core.aicoding.todo.TodoManager.Item.Status.InProgress ->
                        Icons.Outlined.PlayArrow to MaterialTheme.colorScheme.primary
                    else -> Icons.Outlined.RadioButtonUnchecked to MaterialTheme.colorScheme.onSurfaceVariant
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(WtaSpacing.Small))
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.status == com.webtoapp.core.aicoding.todo.TodoManager.Item.Status.Completed)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(WtaSpacing.Tiny))
        }
    }
}
