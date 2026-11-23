package com.webtoapp.ui.agent.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChecklistRtl
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonParser
import com.webtoapp.core.agent.session.AgentMessage
import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.core.agent.session.ThinkingSegmentData
import com.webtoapp.core.agent.session.UserAttachment
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.AgentViewModel
import com.webtoapp.ui.agent.ThinkingSegment
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaExpandIn
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.design.WtaShrinkOut
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.theme.AppColors
import java.util.Date

data class MessageActions(
    val onCopy: (AgentMessage, includeDetails: Boolean) -> Unit,
    val onEdit: (AgentMessage) -> Unit,
    val onRegenerate: (AgentMessage) -> Unit,
    val onDeleteFromHere: (AgentMessage) -> Unit
)

internal fun formatMessageForCopy(message: AgentMessage, includeDetails: Boolean): String {
    val segments = message.thinkingSegmentsSafe
    val messageText = if (message.role == AgentMessage.Role.ASSISTANT) {
        assistantDisplayText(
            message.content,
            message.toolCalls.isNotEmpty() || segments.isNotEmpty() || !message.thinking.isNullOrBlank()
        )
    } else message.content
    val cleanContent = AgentViewModel.stripInlineMarkers(messageText).trim()
    if (!includeDetails) return cleanContent
    val hasThinking = segments.any { it.content.isNotBlank() }
    val hasTools = message.toolCalls.isNotEmpty()
    if (!hasThinking && !hasTools) return cleanContent

    val sb = StringBuilder()
    segments.forEachIndexed { i, seg ->
        val c = seg.content.trim()
        if (c.isBlank()) return@forEachIndexed
        if (sb.isNotEmpty()) sb.append("\n\n")
        sb.append(Strings.agentCopyThinkingHeader)
        if (segments.size > 1) sb.append(" (${i + 1})")
        sb.append("\n").append(c)
    }
    message.toolCalls.forEach { tc ->
        if (sb.isNotEmpty()) sb.append("\n\n")
        sb.append(tc.name)
        val args = tc.argumentsJson.trim()
        if (args.isNotEmpty()) sb.append("(").append(args).append(")")
        val result = tc.visibleResult.trim()
        if (result.isNotEmpty()) {
            sb.append("\n→ ").append(result)
        }
    }
    if (cleanContent.isNotEmpty()) {
        if (sb.isNotEmpty()) sb.append("\n\n")
        sb.append(cleanContent)
    }
    return sb.toString()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: AgentMessage,
    actions: MessageActions,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == AgentMessage.Role.USER
    val tone = if (isUser || message.isError) WtaCardTone.Elevated else WtaCardTone.Surface
    val onContainer = MaterialTheme.colorScheme.onSurface
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth().padding(start = if (isUser) WtaSpacing.ExtraLarge else 0.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) AgentResponseLabel()
        val segments: List<ThinkingSegmentData> = message.thinkingSegmentsSafe
        val hasStructuredThinking = !isUser && segments.isNotEmpty()
        if (!isUser && !hasStructuredThinking && !message.thinking.isNullOrBlank()) {
            ThinkingBlock(
                content = message.thinking,
                durationMs = message.thinkingDurationMs,
                isLive = false,
                modifier = Modifier.widthIn(max = 640.dp)
            )
            Spacer(Modifier.height(WtaSpacing.Tiny))
        }
        MessageSurface(
            tone = tone,
            showAsTimeline = !isUser && (message.toolCalls.isNotEmpty() || hasStructuredThinking),
            modifier = Modifier
                .widthIn(max = if (isUser) 560.dp else 640.dp)
                .then(if (isUser) Modifier else Modifier.fillMaxWidth())
                .combinedClickable(
                    onClick = {},
                    onLongClick = { menuOpen = true }
                )
        ) {
            if (isUser) {
                if (message.userAttachmentsSafe.isNotEmpty()) {
                    UserAttachmentList(message.userAttachmentsSafe)
                    if (message.content.isNotBlank()) Spacer(Modifier.height(WtaSpacing.Small))
                }
                if (message.content.isNotBlank() || message.userAttachmentsSafe.isEmpty()) {
                    SelectionContainer {
                        Text(
                            text = message.content.ifBlank { Strings.agentNoOutput },
                            style = MaterialTheme.typography.bodyMedium,
                            color = onContainer
                        )
                    }
                }
                if (message.mentionedFiles.isNotEmpty()) {
                    Spacer(Modifier.height(WtaSpacing.Small))
                    MentionList(message.mentionedFiles)
                }
            } else {
                val uiThinkingSegments = if (hasStructuredThinking) {
                    segments.map { seg ->
                        ThinkingSegment(
                            id = seg.id,
                            content = seg.content,
                            startedAt = 0L,
                            frozenDurationMs = seg.durationMs
                        )
                    }
                } else emptyList()
                RichAssistantTimeline(
                    text = message.content,
                    toolCalls = message.toolCalls,
                    thinkingSegments = uiThinkingSegments,
                    onSurface = onContainer,
                    hasLegacyThinking = !hasStructuredThinking && !message.thinking.isNullOrBlank()
                )
            }
        }
        if (message.attachments.isNotEmpty()) {
            Spacer(Modifier.height(WtaSpacing.Tiny))
            AttachmentList(message.attachments)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = remember(message.timestamp) {
                    MESSAGE_TIME_FORMAT.format(Date(message.timestamp))
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WtaAlpha.Strong)
            )
            Spacer(Modifier.width(WtaSpacing.Tiny))
            Box {
                WtaIconButton(
                    onClick = { menuOpen = true },
                    icon = Icons.Outlined.MoreHoriz,
                    contentDescription = Strings.more,
                    modifier = Modifier.size(32.dp)
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (isUser) {
                        DropdownMenuItem(
                            text = { Text(Strings.agentMessageActionCopy) },
                            leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                            onClick = { menuOpen = false; actions.onCopy(message, false) }
                        )
                    } else {
                        if (formatMessageForCopy(message, includeDetails = false).isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text(Strings.agentMessageActionCopyOutput) },
                                leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                                onClick = { menuOpen = false; actions.onCopy(message, false) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(Strings.agentMessageActionCopyAll) },
                            leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                            onClick = { menuOpen = false; actions.onCopy(message, true) }
                        )
                    }
                    if (isUser) {
                        DropdownMenuItem(
                            text = { Text(Strings.agentMessageActionEdit) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = { menuOpen = false; actions.onEdit(message) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text(Strings.agentMessageActionRegenerate) },
                            leadingIcon = { Icon(Icons.Outlined.Refresh, contentDescription = null) },
                            onClick = { menuOpen = false; actions.onRegenerate(message) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(Strings.agentMessageActionDelete) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                        onClick = { menuOpen = false; actions.onDeleteFromHere(message) }
                    )
                }
            }
        }
    }
}

private val MESSAGE_TIME_FORMAT = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())

@Composable
fun ThinkingBlock(
    content: String,
    durationMs: Long?,
    isLive: Boolean,
    initiallyExpanded: Boolean = isLive,
    modifier: Modifier = Modifier
) {
    var userExpanded by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val expanded = userExpanded ?: (initiallyExpanded && isLive)
    var appeared by remember { mutableStateOf(!isLive) }
    LaunchedEffect(Unit) { appeared = true }
    val appearAlpha by animateFloatAsState(if (appeared) 1f else 0f, tween(220), label = "appear-alpha")
    val appearDy by animateDpAsState(if (appeared) 0.dp else 8.dp, tween(220), label = "appear-dy")
    WtaCard(
        tone = WtaCardTone.Elevated,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        modifier = modifier.graphicsLayer {
            alpha = appearAlpha
            translationY = appearDy.toPx()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (expanded) Strings.collapse else Strings.expand,
                    onClick = { userExpanded = !expanded }
                )
                .heightIn(min = WtaSize.TouchTarget)
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
                    if (secs > 0) "${Strings.agentThinkingHeaderLive} · ${"%.1fs".format(secs)}"
                    else Strings.agentThinkingHeaderLive
                }
                durationMs != null && durationMs > 0 ->
                    Strings.agentThinkingDoneSeconds.format(durationMs / 1000.0)
                else -> Strings.agentThinkingHeaderLive
            }
            Text(
                text = header,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (isLive) {
                WtaLoadingIndicator(
                    size = 10.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(WtaSpacing.Tiny + 2.dp))
            }
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(WtaSize.IconSmall - 2.dp)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = WtaExpandIn,
            exit = WtaShrinkOut
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
private fun AgentResponseLabel() {
    Row(
        modifier = Modifier.padding(bottom = WtaSpacing.Tiny),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny + 2.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.RocketLaunch,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(WtaSize.IconSmall)
        )
        Text(
            text = Strings.agentTitle,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StreamingBubble(
    text: String,
    thinkingSegments: List<ThinkingSegment>,
    pendingTools: List<RecordedToolCall>,
    activity: String?
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        AgentResponseLabel()
        MessageSurface(
            tone = WtaCardTone.Surface,
            showAsTimeline = pendingTools.isNotEmpty() || thinkingSegments.isNotEmpty(),
            modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()
        ) {
            if (text.isNotEmpty() || pendingTools.isNotEmpty() || thinkingSegments.isNotEmpty()) {
                RichAssistantTimeline(
                    text = text,
                    toolCalls = pendingTools,
                    thinkingSegments = thinkingSegments,
                    onSurface = MaterialTheme.colorScheme.onSurface,
                    liveTrailing = true
                )
                val toolSettled = pendingTools.isNotEmpty() &&
                    pendingTools.none { it.effectiveStatus == RecordedToolCall.Status.Running }
                val thinkingLive = thinkingSegments.any { it.frozenDurationMs == null }
                val tail = text.trimEnd()
                val tailIsMarker = tail.isEmpty() || tail.last() == '\u2063'
                AnimatedVisibility(
                    visible = toolSettled && !thinkingLive && tailIsMarker,
                    enter = WtaExpandIn,
                    exit = WtaShrinkOut
                ) {
                    Row(
                        modifier = Modifier.padding(top = WtaSpacing.Tiny + 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TypingDots(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(WtaSpacing.Small + 2.dp))
                        Text(
                            text = activity ?: Strings.agentPhaseThinking,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (text.isBlank() && pendingTools.isEmpty() && thinkingSegments.isEmpty()) {
                Row(
                    modifier = Modifier.padding(vertical = WtaSpacing.Tiny),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypingDots(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(WtaSpacing.Small + 2.dp))
                    Text(
                        text = activity ?: Strings.agentPhaseThinking,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageSurface(
    tone: WtaCardTone,
    showAsTimeline: Boolean,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    if (showAsTimeline) {
        Column(modifier = modifier, content = content)
    } else {
        WtaCard(
            tone = tone,
            contentPadding = PaddingValues(
                horizontal = WtaSpacing.Medium + 2.dp,
                vertical = WtaSpacing.Medium - 2.dp
            ),
            modifier = modifier,
            content = content
        )
    }
}

@Composable
private fun TypingDots(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing-dots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = i * 160),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot-$i"
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .graphicsLayer {
                        this.alpha = alpha
                        val s = 0.85f + alpha * 0.3f
                        scaleX = s
                        scaleY = s
                    }
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
private fun RichAssistantTimeline(
    text: String,
    toolCalls: List<RecordedToolCall>,
    onSurface: Color,
    thinkingSegments: List<ThinkingSegment> = emptyList(),
    liveTrailing: Boolean = false,
    hasLegacyThinking: Boolean = false
) {
    val parts = remember(text, toolCalls, thinkingSegments, hasLegacyThinking) {
        buildAssistantTimeline(text, toolCalls, thinkingSegments, hasLegacyThinking)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        if (parts.isEmpty() && !hasLegacyThinking && !liveTrailing) {
            Text(Strings.agentNoOutput, style = MaterialTheme.typography.bodyMedium, color = onSurface)
        }
        parts.forEachIndexed { index, part ->
            when (part) {
                is AssistantTimelinePart.Prose -> RichAssistantText(
                    text = part.text,
                    onSurface = onSurface,
                    streamingCaret = liveTrailing && index == parts.lastIndex
                )
                is AssistantTimelinePart.Thinking -> key("thinking-${part.segment.id}") {
                    val isLive = liveTrailing && part.segment.frozenDurationMs == null
                    ThinkingBlock(
                        content = part.segment.content,
                        durationMs = part.segment.frozenDurationMs,
                        isLive = isLive,
                        initiallyExpanded = isLive
                    )
                }
                is AssistantTimelinePart.Tools -> key("tools-${part.calls.first().toolCallId}") {
                    ToolActivityGroup(part.calls, liveTrailing)
                }
            }
        }
    }
}

@Composable
private fun ToolActivityGroup(calls: List<RecordedToolCall>, live: Boolean) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    var openDetails by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val canShowMore = calls.size > 3
    val visibleCalls = if (showAll || !canShowMore) calls else calls.takeLast(3)
    val groupStatus = calls.minOf { it.presentationStatus(live) }
    val colors = MaterialTheme.colorScheme

    WtaCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (canShowMore) Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = if (showAll) Strings.collapse else Strings.expand,
                        onClick = { showAll = !showAll }
                    ) else Modifier
                )
                .heightIn(min = if (canShowMore) WtaSize.TouchTarget else 36.dp)
                .padding(horizontal = WtaSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
        ) {
            Text(
                text = Strings.agentToolCallCount.format(calls.size),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = groupStatus.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (groupStatus == ToolPresentationStatus.Running) colors.primary else colors.onSurfaceVariant
            )
            if (canShowMore) {
                Icon(
                    imageVector = if (showAll) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(WtaSize.IconSmall)
                )
            }
        }
        visibleCalls.forEach { call ->
            key(call.toolCallId) {
                WtaSectionDivider(Modifier.padding(horizontal = WtaSpacing.Medium))
                val expanded = call.toolCallId in openDetails
                ToolActivityRow(call, call.presentationStatus(live), expanded) {
                    openDetails = if (expanded) openDetails - call.toolCallId else openDetails + call.toolCallId
                }
            }
        }
    }
}

private enum class ToolPresentationStatus {
    Failed, Stopped, Running, Completed;

    val label: String
        get() = when (this) {
            Failed -> Strings.failed
            Stopped -> Strings.agentAbortedHint.trim('[', ']')
            Running -> Strings.agentPhaseToolRunning
            Completed -> Strings.completed
        }
}

private fun RecordedToolCall.presentationStatus(live: Boolean): ToolPresentationStatus = when (effectiveStatus) {
    RecordedToolCall.Status.Failed -> ToolPresentationStatus.Failed
    RecordedToolCall.Status.Stopped -> ToolPresentationStatus.Stopped
    RecordedToolCall.Status.Running -> if (live) ToolPresentationStatus.Running else ToolPresentationStatus.Stopped
    RecordedToolCall.Status.Completed -> ToolPresentationStatus.Completed
}

@Composable
private fun ToolActivityRow(
    call: RecordedToolCall,
    status: ToolPresentationStatus,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val title = toolActionLabel(call.name)
    val target = remember(call.argumentsJson) { toolTarget(call.argumentsJson) }
    val running = status == ToolPresentationStatus.Running
    val summary = if (target.isNotBlank()) target else if (running || status == ToolPresentationStatus.Failed) {
        call.visibleResult.lineSequence().lastOrNull { it.isNotBlank() }?.take(160).orEmpty()
    } else ""
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { stateDescription = status.label }
            .clickable(
                role = Role.Button,
                onClickLabel = if (expanded) Strings.collapse else Strings.expand,
                onClick = onToggle
            )
            .heightIn(min = WtaSize.TouchTarget)
            .padding(horizontal = WtaSpacing.Medium, vertical = WtaSpacing.Tiny + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        Icon(
            imageVector = toolIconFor(call.name),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(WtaSize.IconSmall + 2.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (summary.isNotBlank()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (running) {
            WtaLoadingIndicator(size = 14.dp, color = colors.primary)
        } else {
            Icon(
                imageVector = when (status) {
                    ToolPresentationStatus.Completed -> Icons.Outlined.CheckCircle
                    ToolPresentationStatus.Stopped -> Icons.Outlined.StopCircle
                    else -> Icons.Outlined.ErrorOutline
                },
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        Icon(
            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(WtaSize.IconSmall)
        )
    }
    AnimatedVisibility(visible = expanded, enter = WtaExpandIn, exit = WtaShrinkOut) {
        ToolActivityDetails(call, status)
    }
}

@Composable
private fun ToolActivityDetails(call: RecordedToolCall, status: ToolPresentationStatus) {
    var showResult by remember { mutableStateOf(false) }
    val result = call.visibleResult
    val running = status == ToolPresentationStatus.Running
    val hasProblem = status == ToolPresentationStatus.Failed || status == ToolPresentationStatus.Stopped
    Column(
        modifier = Modifier.fillMaxWidth().padding(
            start = WtaSpacing.Medium,
            end = WtaSpacing.Medium,
            bottom = WtaSpacing.Medium
        ),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        Text(
            text = call.name,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hasProblem || !SpecialToolBody(call, running)) {
            if (result.isNotBlank()) {
                ToolBlockLabel(Strings.agentToolResultLabel)
                ToolMonoBlock(
                    text = result,
                    spinner = running,
                    truncated = result.length > 440 || result.count { it == '\n' } > 6,
                    onViewFull = { showResult = true }
                )
            } else if (running) {
                ToolMonoBlock(text = Strings.agentPhaseToolRunning, spinner = true)
            }
            if (call.argumentsJson.isNotBlank() && call.argumentsJson.trim() != "{}") {
                ToolBlockLabel(Strings.agentToolArgsLabel)
                ToolMonoBlock(prettyJsonOrRaw(call.argumentsJson))
            }
        }
    }
    if (showResult) {
        ToolFullResultDialog(title = toolActionLabel(call.name), content = result, onDismiss = { showResult = false })
    }
}

private fun toolActionLabel(name: String): String = when (name) {
    "Read", "ReadAppFile" -> Strings.agentToolReadFile
    "Write" -> Strings.agentToolWriteFile
    "Edit" -> Strings.agentToolEditFile
    "Delete" -> Strings.agentToolDeleteFile
    "Glob" -> Strings.agentToolFindFiles
    "Grep" -> Strings.agentToolSearchContent
    "ListFiles" -> Strings.agentToolListFiles
    "EnterPlanMode" -> Strings.agentPlanModeBadge
    "ExitPlanMode" -> Strings.agentToolSubmitPlan
    "TodoWrite", "TodoUpdate" -> Strings.agentTodoListHeader
    "preview" -> Strings.agentPreviewOpen
    else -> name
}

private fun toolIconFor(name: String): androidx.compose.ui.graphics.vector.ImageVector = when (name) {
    "Write" -> Icons.Outlined.Edit
    "Edit" -> Icons.Outlined.Edit
    "Read", "ReadAppFile" -> Icons.Outlined.Description
    "Delete" -> Icons.Outlined.Delete
    "Glob" -> Icons.Outlined.Search
    "Grep" -> Icons.Outlined.Search
    "ListFiles" -> Icons.Outlined.FolderOpen
    "TodoWrite", "TodoUpdate", "EnterPlanMode", "ExitPlanMode" -> Icons.Outlined.ChecklistRtl
    "CreateApp", "UpdateApp", "GetApp", "ListApps" -> Icons.Outlined.Apps
    "CreateModule", "UpdateModule", "GetModule", "ListModules" -> Icons.Outlined.Extension
    else -> Icons.Outlined.Build
}

@Composable
private fun ToolFullResultDialog(title: String, content: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    WtaAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(Strings.close)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = {
                clipboard.setText(AnnotatedString(content))
                onDismiss()
            }) {
                Text(Strings.agentMessageActionCopy)
            }
        },
        title = title,
        content = {
            Box(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                SelectionContainer {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    )
}

@Composable
private fun SpecialToolBody(tc: RecordedToolCall, running: Boolean): Boolean {
    val args = parseToolArgs(tc.argumentsJson)

    return when (tc.name) {
        "Write" -> {
            val path = args.stringValue("path")
            val content = args.stringValue("content")
            if (path.isEmpty() && content.isEmpty()) return false
            ToolPathHeader(path = path)
            if (content.isNotEmpty() || running) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                CodeBlock(
                    code = content,
                    language = languageFor(path),
                    showCaret = running,
                    maxHeightDp = 280
                )
            }
            if (!running && tc.visibleResult.isNotBlank()) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                Text(
                    text = tc.visibleResult,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            true
        }
        "Edit" -> {
            val path = args.stringValue("path")
            val oldStr = args.stringValue("old_string")
            val newStr = args.stringValue("new_string")
            if (path.isEmpty() && oldStr.isEmpty() && newStr.isEmpty()) return false
            val diffLines = if (oldStr.isNotEmpty() && newStr.isNotEmpty() && !running) {
                com.webtoapp.core.agent.diff.LineDiff.diff(oldStr, newStr)
            } else null
            val stats = diffLines?.let { com.webtoapp.core.agent.diff.LineDiff.stats(it) }
            ToolPathHeader(path = path, stats = stats)

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
            if (!running && tc.visibleResult.isNotBlank()) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                Text(
                    text = tc.visibleResult,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            true
        }
        "Read" -> {
            val path = args.stringValue("path")
            if (path.isEmpty()) return false
            ToolPathHeader(path = path)
            if (running && tc.visibleResult.isBlank()) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                ToolMonoBlock(
                    text = Strings.agentPhaseToolRunning,
                    spinner = true
                )
            } else if (tc.visibleResult.isNotBlank()) {
                Spacer(Modifier.height(WtaSpacing.Tiny + 2.dp))
                CodeBlock(
                    code = tc.visibleResult,
                    language = languageFor(path),
                    showCaret = running,
                    maxHeightDp = 280
                )
            }
            true
        }
        "Delete" -> {
            val path = args.stringValue("path")
            if (path.isEmpty()) return false
            ToolPathHeader(path = path)
            if (!running && tc.visibleResult.isNotBlank()) {
                Spacer(Modifier.height(WtaSpacing.Tiny))
                Text(
                    text = tc.visibleResult,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            true
        }
        "Glob", "Grep", "ListFiles" -> {
            if (running) return false
            val lines = tc.visibleResult.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) return false
            FileListBody(lines)
            true
        }
        else -> false
    }
}

@Composable
private fun FileListBody(lines: List<String>) {
    val shown = lines.take(60)
    Column(modifier = Modifier.fillMaxWidth()) {
        shown.forEach { line ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 1.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = line,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
        if (lines.size > shown.size) {
            Spacer(Modifier.height(WtaSpacing.Tiny))
            Text(
                text = "… +${lines.size - shown.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToolPathHeader(path: String, stats: com.webtoapp.core.agent.diff.LineDiff.Stats? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        SelectionContainer(modifier = Modifier.weight(1f)) {
            Text(
                text = path,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
        if (stats != null && (stats.added > 0 || stats.removed > 0)) {
            Text(
                text = "+${stats.added}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = WtaColors.semantic.success,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "-${stats.removed}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun ToolMonoBlock(
    text: String,
    spinner: Boolean = false,
    truncated: Boolean = false,
    onViewFull: (() -> Unit)? = null
) {
    val clipboard = LocalClipboardManager.current
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surfaceContainerHigh,
        shape = MaterialTheme.shapes.small
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = WtaSpacing.Medium, top = WtaSpacing.Small, bottom = WtaSpacing.Small),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny),
                verticalAlignment = Alignment.Top
            ) {
                if (spinner) {
                    WtaLoadingIndicator(
                        modifier = Modifier.padding(top = WtaSpacing.Tiny),
                        size = 12.dp,
                        color = colors.primary
                    )
                }
                SelectionContainer(modifier = Modifier.weight(1f)) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = colors.onSurface,
                        maxLines = if (truncated) 7 else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!spinner && text.isNotBlank()) {
                    WtaIconButton(
                        onClick = { clipboard.setText(AnnotatedString(text)) },
                        icon = Icons.Outlined.ContentCopy,
                        contentDescription = Strings.agentMessageActionCopy,
                        modifier = Modifier.size(WtaSize.TouchTarget)
                    )
                } else {
                    Spacer(Modifier.width(WtaSpacing.Small))
                }
            }
            if (truncated && onViewFull != null) {
                androidx.compose.material3.TextButton(
                    onClick = onViewFull,
                    modifier = Modifier.align(Alignment.End)
                ) { Text(Strings.details) }
            }
        }
    }
}

@Composable
private fun UnifiedDiffBlock(
    lines: List<com.webtoapp.core.agent.diff.LineDiff.Line>,
    @Suppress("UNUSED_PARAMETER") language: String?,
    maxHeightDp: Int
) {
    val clipboard = LocalClipboardManager.current
    val plain = remember(lines) {
        lines.joinToString("\n") { ln ->
            when (ln.kind) {
                com.webtoapp.core.agent.diff.LineDiff.Line.Kind.Added -> "+" + ln.text
                com.webtoapp.core.agent.diff.LineDiff.Line.Kind.Removed -> "-" + ln.text
                com.webtoapp.core.agent.diff.LineDiff.Line.Kind.Context -> " " + ln.text
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
                    contentDescription = Strings.agentMessageActionCopy,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DiffLineRow(line: com.webtoapp.core.agent.diff.LineDiff.Line) {
    val (bg, fg, gutter) = when (line.kind) {
        com.webtoapp.core.agent.diff.LineDiff.Line.Kind.Added ->
            Triple(AppColors.DiffAddedBg, AppColors.DiffAddedFg, "+")
        com.webtoapp.core.agent.diff.LineDiff.Line.Kind.Removed ->
            Triple(AppColors.DiffRemovedBg, AppColors.DiffRemovedFg, "-")
        com.webtoapp.core.agent.diff.LineDiff.Line.Kind.Context ->
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

private fun parseToolArgs(json: String): com.google.gson.JsonObject? =
    runCatching { JsonParser.parseString(json) as? com.google.gson.JsonObject }.getOrNull()

private fun com.google.gson.JsonObject?.stringValue(key: String): String =
    this?.get(key)?.takeIf { it.isJsonPrimitive }?.asString.orEmpty()

private fun toolTarget(json: String): String {
    val args = parseToolArgs(json) ?: return ""
    return listOf("path", "file", "filePath", "pattern", "query", "command", "url", "name", "appId", "moduleId")
        .firstNotNullOfOrNull { args.stringValue(it).takeIf(String::isNotBlank) }
        .orEmpty()
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
private fun UserAttachmentList(attachments: List<UserAttachment>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
        items(attachments.take(8), key = { "uatt-${it.path}" }) { att ->
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = WtaSpacing.Small,
                        vertical = WtaSpacing.Tiny
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            att.path.endsWith("/") -> Icons.Outlined.Folder
                            att.isImage -> Icons.Outlined.Image
                            else -> Icons.Outlined.AttachFile
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(WtaSize.IconSmall)
                    )
                    Spacer(Modifier.width(WtaSpacing.Tiny))
                    Text(
                        text = att.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 160.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TodoChecklist(todos: List<com.webtoapp.core.agent.todo.TodoManager.Item>) {
    if (todos.isEmpty()) return
    val done = todos.count { it.status == com.webtoapp.core.agent.todo.TodoManager.Item.Status.Completed }
    val total = todos.size
    var expanded by remember(total) { mutableStateOf(true) }
    LaunchedEffect(done, total) {
        if (done == total && total > 0) expanded = false
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = WtaSpacing.Medium, vertical = WtaSpacing.Small + 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = if (done == total) WtaColors.semantic.success else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(WtaSpacing.Small))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.agentTodoListHeader,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$done / $total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = WtaExpandIn,
                exit = WtaShrinkOut
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = WtaSpacing.Medium)
                        .padding(bottom = WtaSpacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    todos.forEach { item ->
                        val (icon, color) = when (item.status) {
                            com.webtoapp.core.agent.todo.TodoManager.Item.Status.Completed ->
                                Icons.Outlined.CheckCircle to WtaColors.semantic.success
                            com.webtoapp.core.agent.todo.TodoManager.Item.Status.InProgress ->
                                Icons.Outlined.PlayArrow to MaterialTheme.colorScheme.primary
                            else -> Icons.Outlined.CheckCircle to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(WtaSpacing.Small))
                            Text(
                                text = item.subject,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (item.status == com.webtoapp.core.agent.todo.TodoManager.Item.Status.Completed)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (item.status == com.webtoapp.core.agent.todo.TodoManager.Item.Status.InProgress)
                                    FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
