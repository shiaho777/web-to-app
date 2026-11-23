package com.webtoapp.ui.agent.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.agent.session.UserAttachment
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.AgentUiState
import com.webtoapp.ui.agent.SlashCommand
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing

@Composable
fun Composer(
    state: AgentUiState,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    onRunSlashCommand: (SlashCommand) -> Unit,
    onDismissSlash: () -> Unit,
    onPickMention: (String) -> Unit,
    onDismissMention: () -> Unit,
    onTogglePlanMode: () -> Unit,

    onAttachImage: () -> Unit,
    onAttachFile: () -> Unit,
    onAttachFolder: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    resolveAttachmentPreview: (UserAttachment) -> Any?,

    onOpenContextPicker: () -> Unit,

    onOpenModelPicker: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.slashOpen) {
            SlashSuggestions(
                commandMatches = state.slashCommands.filter {
                    val q = state.composerText.takeIf { it.startsWith("/") && !it.contains(" ") }
                        ?.removePrefix("/")?.lowercase().orEmpty()
                    q.isEmpty() || it.command.lowercase().contains(q)
                }.take(6),
                onPickCommand = onRunSlashCommand,
                onDismiss = onDismissSlash
            )
        } else if (state.mentionPickerOpen) {
            MentionSuggestions(
                matches = state.mentionMatches,
                onPick = onPickMention,
                onDismiss = onDismissMention
            )
        }
        ComposerActivity(state)
        WtaCard(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = WtaSpacing.ScreenHorizontal,
                vertical = WtaSpacing.Small
            ),
            shape = RoundedCornerShape(WtaRadius.Dialog),
            contentPadding = PaddingValues(WtaSpacing.Small)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)) {
                if (state.pendingAttachments.isNotEmpty()) {
                    PendingAttachmentsRow(
                        attachments = state.pendingAttachments,
                        onRemove = onRemoveAttachment,
                        resolveAttachmentPreview = resolveAttachmentPreview
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
                ) {
                    AttachButton(
                        onAttachImage = onAttachImage,
                        onAttachFile = onAttachFile,
                        onAttachFolder = onAttachFolder
                    )
                    ComposerField(
                        value = state.composerText,
                        onValueChange = onTextChange,
                        placeholder = composerPlaceholder(state)
                    )
                    SendButton(
                        working = state.isWorking,
                        enabled = state.canSend &&
                            (state.composerText.isNotBlank() || state.pendingAttachments.isNotEmpty()),
                        onSend = onSend,
                        onCancel = onCancel
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WtaAlpha.Divider)
                )
                ComposerActions(
                    state = state,
                    onOpenModelPicker = onOpenModelPicker,
                    onTogglePlanMode = onTogglePlanMode,
                    onOpenContextPicker = onOpenContextPicker
                )
            }
        }
    }
}

@Composable
private fun RowScope.ComposerField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val shape = RoundedCornerShape(WtaRadius.Control)
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
            .heightIn(min = WtaSize.TouchTarget, max = 120.dp)
            .padding(horizontal = WtaSpacing.Small, vertical = WtaSpacing.Small),
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerField()
            }
        }
    )
}

@Composable
private fun AttachButton(
    onAttachImage: () -> Unit,
    onAttachFile: () -> Unit,
    onAttachFolder: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        WtaIconButton(
            onClick = { menuOpen = true },
            icon = Icons.Outlined.Add,
            contentDescription = Strings.agentAttachTooltip,
            modifier = Modifier.size(WtaSize.TouchTarget)
        )
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text(Strings.agentAttachImage) },
                leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null) },
                onClick = { menuOpen = false; onAttachImage() }
            )
            DropdownMenuItem(
                text = { Text(Strings.agentAttachFile) },
                leadingIcon = { Icon(Icons.Outlined.InsertDriveFile, contentDescription = null) },
                onClick = { menuOpen = false; onAttachFile() }
            )
            DropdownMenuItem(
                text = { Text(Strings.agentAttachFolder) },
                leadingIcon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                onClick = { menuOpen = false; onAttachFolder() }
            )
        }
    }
}

@Composable
private fun PendingAttachmentsRow(
    attachments: List<UserAttachment>,
    onRemove: (String) -> Unit,
    resolveAttachmentPreview: (UserAttachment) -> Any?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = WtaSpacing.Tiny),
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        attachments.forEach { att ->
            if (att.isImage) {
                ImageAttachmentChip(att, onRemove, resolveAttachmentPreview(att))
            } else {
                FileAttachmentChip(att, onRemove)
            }
        }
    }
}

@Composable
private fun ImageAttachmentChip(att: UserAttachment, onRemove: (String) -> Unit, previewModel: Any?) {
    Box {
        Surface(
            shape = RoundedCornerShape(WtaRadius.Control),
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            coil.compose.AsyncImage(
                model = previewModel,
                contentDescription = att.displayName,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.size(40.dp)
            )
        }
        AttachmentRemoveBadge(
            onClick = { onRemove(att.path) },
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun FileAttachmentChip(att: UserAttachment, onRemove: (String) -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(
                start = WtaSpacing.Small,
                end = WtaSpacing.Tiny,
                top = WtaSpacing.Tiny,
                bottom = WtaSpacing.Tiny
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (att.path.endsWith("/")) Icons.Outlined.Folder else Icons.Outlined.AttachFile,
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
                modifier = Modifier.widthIn(max = 140.dp),
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = Strings.btnDelete,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .size(WtaSize.IconSmall)
                    .clickable { onRemove(att.path) }
            )
        }
    }
}

@Composable
private fun AttachmentRemoveBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(WtaRadius.Pill),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        modifier = modifier
            .size(20.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = Strings.btnDelete,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

private fun composerPlaceholder(state: AgentUiState): String = when {
    state.isWorking -> Strings.agentComposerHintWorking
    else -> Strings.agentComposerHintIdle
}

@Composable
private fun SendButton(
    working: Boolean,
    enabled: Boolean,
    onSend: () -> Unit,
    onCancel: () -> Unit
) {
    val container = when {
        working -> MaterialTheme.colorScheme.secondaryContainer
        enabled -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val content = when {
        working -> MaterialTheme.colorScheme.onSecondaryContainer
        enabled -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        onClick = if (working) onCancel else onSend,
        enabled = working || enabled,
        shape = RoundedCornerShape(WtaRadius.Control),
        color = container,
        modifier = Modifier.size(WtaSize.TouchTarget)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (working) Icons.Outlined.Stop else Icons.AutoMirrored.Outlined.Send,
                contentDescription = if (working) Strings.agentStopTooltip else Strings.agentSendTooltip,
                tint = content,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SlashSuggestions(
    commandMatches: List<SlashCommand>,
    onPickCommand: (SlashCommand) -> Unit,
    onDismiss: () -> Unit
) {
    if (commandMatches.isEmpty()) return
    SuggestionsCard(
        leading = "/",
        header = Strings.agentSlashHeader,
        onDismiss = onDismiss
    ) {
        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            items(commandMatches, key = { "cmd-${it.id}" }) { cmd ->
                SuggestionRow(
                    title = cmd.command,
                    subtitle = cmd.description,
                    hint = null,
                    iconName = cmd.icon,
                    iconColor = cmd.iconColor,
                    onClick = { onPickCommand(cmd) }
                )
            }
        }
    }
}

@Composable
private fun MentionSuggestions(
    matches: List<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    SuggestionsCard(
        leading = "@",
        header = Strings.agentMentionHeader,
        onDismiss = onDismiss
    ) {
        if (matches.isEmpty()) {
            Text(
                text = Strings.agentMentionEmpty,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = WtaSpacing.RowHorizontal,
                    vertical = WtaSpacing.Large
                )
            )
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                items(matches, key = { "mention-${it.relativePath}" }) { file ->
                    WtaSettingRow(
                        title = file.relativePath.substringAfterLast('/'),
                        subtitle = file.relativePath.substringBeforeLast('/', missingDelimiterValue = ""),
                        icon = Icons.Outlined.Description,
                        onClick = { onPick(file.relativePath) }
                    ) {
                        Text(
                            text = file.formatSize(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionsCard(
    leading: String,
    header: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WtaSpacing.ScreenHorizontal,
                        vertical = WtaSpacing.Small
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = leading,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(WtaSpacing.Tiny))
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                WtaIconButton(
                    onClick = onDismiss,
                    icon = Icons.Outlined.Cancel,
                    contentDescription = Strings.agentSlashClose,
                    modifier = Modifier.size(WtaSize.TouchTarget)
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WtaAlpha.Divider)
            )
            content()
        }
    }
}

@Composable
private fun SuggestionRow(
    title: String,
    subtitle: String,
    hint: String?,
    iconName: String,
    iconColor: String,
    onClick: () -> Unit
) {
    WtaSettingRow(
        title = title,
        subtitle = subtitle.takeIf { it.isNotBlank() },
        iconContent = {
            MaterialIconGlyph(name = iconName, tintHex = iconColor, size = WtaSize.Icon)
        },
        onClick = onClick
    ) {
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun ComposerActivity(state: AgentUiState) {
    val activity = when {
        state.compacting -> Strings.agentCompacting
        state.pendingPermission != null -> Strings.agentPermissionRequired
        state.phase == AgentUiState.Phase.AwaitingTool -> state.currentActivity ?: Strings.agentPhaseToolRunning
        state.isWorking -> state.currentActivity ?: Strings.agentPhaseThinking
        else -> null
    }
    AnimatedVisibility(
        visible = activity != null,
        enter = CardExpandTransition,
        exit = CardCollapseTransition
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WtaSpacing.ScreenHorizontal,
                vertical = WtaSpacing.Tiny
            ),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WtaLoadingIndicator(size = 14.dp)
            Text(
                text = activity.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ComposerActions(
    state: AgentUiState,
    onOpenModelPicker: () -> Unit,
    onTogglePlanMode: () -> Unit,
    onOpenContextPicker: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ComposerControl(
            label = if (state.modelMissing) Strings.addModel
                else state.currentModelLabel.ifBlank { Strings.selectModel },
            icon = if (state.modelMissing) Icons.Outlined.Add else Icons.Outlined.SmartToy,
            onClick = onOpenModelPicker,
            modifier = Modifier.weight(1f)
        )
        ComposerControl(
            label = Strings.agentPlanModeBadge,
            icon = if (state.planActive) Icons.Outlined.Check else Icons.Outlined.Checklist,
            enabled = state.canChangePlanMode,
            active = state.planActive,
            onClick = onTogglePlanMode,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    selected = state.planActive
                    if (state.planActive) onClick(label = Strings.agentPlanModeExitTooltip, action = null)
                }
        )
        ComposerControl(
            label = if (state.selectedContextCount > 0) "${Strings.agentContextChipLabel} · ${state.selectedContextCount}"
                else Strings.agentContextChipLabel,
            icon = Icons.Outlined.BookmarkBorder,
            onClick = onOpenContextPicker,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ComposerControl(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    enabled: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    val color = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = WtaAlpha.Disabled)
    WtaCard(
        onClick = onClick,
        tone = if (active) WtaCardTone.Elevated else WtaCardTone.Surface,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(WtaRadius.Control),
        contentPadding = PaddingValues(horizontal = WtaSpacing.Tiny)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = WtaSize.TouchTarget),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(WtaSize.IconSmall))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
