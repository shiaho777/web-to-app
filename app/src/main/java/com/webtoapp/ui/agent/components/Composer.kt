package com.webtoapp.ui.agent.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Warning
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.agent.session.UserAttachment
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.AgentUiState
import com.webtoapp.ui.agent.SlashCommand
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaIconButton
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
    onToggleAutoApprove: () -> Unit,

    onTriggerSlash: () -> Unit,

    onAttachImage: () -> Unit,
    onAttachFile: () -> Unit,
    onAttachFolder: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    // Resolves a pending attachment's sandbox-relative path to the file Coil should
    // load — java.io.File(att.path) resolves against the process CWD and never exists.
    resolveAttachmentPreview: (UserAttachment) -> Any?,

    onOpenContextPicker: () -> Unit,

    onOpenModelPicker: () -> Unit = {},
    onCompactContext: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.slashOpen) {
            SlashSuggestions(
                commandMatches = state.slashCommands.filter {
                    val q = state.composerText.removePrefix("/").lowercase()
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
        ComposerCard(
            state = state,
            onTextChange = onTextChange,
            onSend = onSend,
            onCancel = onCancel,
            onAttachImage = onAttachImage,
            onAttachFile = onAttachFile,
            onAttachFolder = onAttachFolder,
            onRemoveAttachment = onRemoveAttachment,
            resolveAttachmentPreview = resolveAttachmentPreview
        )

        ModeChipRow(
            autoApprove = state.autoApprove,
            onToggleAuto = onToggleAutoApprove,
            onTriggerSlash = onTriggerSlash,
            currentModelLabel = state.currentModelLabel,
            onOpenModelPicker = onOpenModelPicker,
            estimatedTokens = state.estimatedContextTokens,
            contextCapacity = state.contextCapacity,
            compacting = state.compacting,
            onCompactContext = onCompactContext,
            selectedContextCount = state.contextAppIds.size + state.contextModuleIds.size,
            onOpenContextPicker = onOpenContextPicker
        )
    }
}

/**
 * Unified chat composer (ChatGPT/Claude-style): a single elevated card holding
 * the attachment strip, the growing text field, and a bottom action row with
 * the attach menu and an embedded circular send/stop button. Keeps the legacy
 * height behaviour (44dp resting, 220dp cap) that only BasicTextField allows —
 * an M3 TextField enforces a 56dp minimum.
 */
@Composable
private fun ComposerCard(
    state: AgentUiState,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    onAttachImage: () -> Unit,
    onAttachFile: () -> Unit,
    onAttachFolder: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    resolveAttachmentPreview: (UserAttachment) -> Any?
) {
    WtaCard(
        tone = WtaCardTone.Elevated,
        shape = RoundedCornerShape(WtaRadius.Card + 8.dp),
        contentPadding = PaddingValues(vertical = WtaSpacing.Small),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = WtaSpacing.ScreenHorizontal,
                vertical = WtaSpacing.Small
            )
    ) {
        if (state.pendingAttachments.isNotEmpty()) {
            PendingAttachmentsRow(
                attachments = state.pendingAttachments,
                onRemove = onRemoveAttachment,
                resolveAttachmentPreview = resolveAttachmentPreview
            )
        }
        ComposerField(
            value = state.composerText,
            onValueChange = onTextChange,
            placeholder = composerPlaceholder(state)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WtaSpacing.Small, vertical = WtaSpacing.Tiny),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AttachButton(
                onAttachImage = onAttachImage,
                onAttachFile = onAttachFile,
                onAttachFolder = onAttachFolder
            )
            Spacer(Modifier.weight(1f))
            SendButton(
                working = state.isWorking,
                enabled = state.canSend &&
                    (state.composerText.isNotBlank() || state.pendingAttachments.isNotEmpty()),
                onSend = onSend,
                onCancel = onCancel
            )
        }
    }
}

/**
 * The growing text field inside [ComposerCard]. Built on foundation
 * [BasicTextField] — an M3 TextField would enforce its own 56dp minimum and
 * add focus chrome we do not want inside the unified card.
 */
@Composable
private fun ComposerField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp, max = 220.dp)
            .padding(horizontal = WtaSpacing.RowHorizontal, vertical = WtaSpacing.Tiny),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
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
            contentDescription = Strings.agentAttachTooltip
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
            .padding(horizontal = WtaSpacing.RowHorizontal, vertical = WtaSpacing.Tiny),
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

/** Image attachment with a real thumbnail (Coil) instead of a bare text chip. */
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
                modifier = Modifier.size(56.dp)
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

/** Small circular "×" badge overlaid on an image thumbnail. */
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
        working -> MaterialTheme.colorScheme.errorContainer
        enabled -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val content = when {
        working -> MaterialTheme.colorScheme.onErrorContainer
        enabled -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        onClick = if (working) onCancel else onSend,
        enabled = working || enabled,
        shape = RoundedCornerShape(WtaRadius.Pill),
        color = container,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (working) Icons.Outlined.Stop else Icons.AutoMirrored.Outlined.Send,
                contentDescription = if (working) Strings.agentStopTooltip else Strings.agentSendTooltip,
                tint = content,
                modifier = Modifier.size(18.dp)
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
private fun ModeChipRow(
    autoApprove: Boolean,
    onToggleAuto: () -> Unit,
    onTriggerSlash: () -> Unit,
    currentModelLabel: String,
    onOpenModelPicker: () -> Unit,
    estimatedTokens: Int,
    contextCapacity: Int,
    compacting: Boolean,
    onCompactContext: () -> Unit,
    selectedContextCount: Int,
    onOpenContextPicker: () -> Unit
) {
    var showCompactMenu by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = WtaSpacing.ScreenHorizontal,
                vertical = WtaSpacing.Tiny + 2.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModeChip(
            label = if (autoApprove) Strings.agentAutoModeLabel
            else Strings.agentManualModeLabel,
            icon = if (autoApprove) Icons.Outlined.Bolt else Icons.Outlined.Lock,
            primary = autoApprove,
            onClick = onToggleAuto
        )

        ModeChip(
            label = Strings.agentSlashChipLabel,
            icon = Icons.Outlined.Terminal,
            primary = false,
            onClick = onTriggerSlash
        )

        ModeChip(
            label = if (selectedContextCount > 0)
                "${Strings.agentContextChipLabel} · $selectedContextCount"
            else Strings.agentContextChipLabel,
            icon = Icons.Outlined.BookmarkBorder,
            primary = selectedContextCount > 0,
            onClick = onOpenContextPicker
        )

        if (contextCapacity > 0) {
            val usageLabel = formatTokenUsage(estimatedTokens, contextCapacity)
            val usageRatio = if (contextCapacity > 0) estimatedTokens.toFloat() / contextCapacity else 0f
            val usageHigh = usageRatio >= 0.75f
            // Box anchors the compact-context menu to this chip — emitted as a row
            // sibling it would anchor to the column's top-start corner instead.
            Box {
                ContextChip(
                    label = usageLabel,
                    warning = usageHigh,
                    compacting = compacting,
                    onClick = { showCompactMenu = true }
                )
                androidx.compose.material3.DropdownMenu(
                    expanded = showCompactMenu,
                    onDismissRequest = { showCompactMenu = false }
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(Strings.agentCompactNow) },
                        onClick = {
                            showCompactMenu = false
                            onCompactContext()
                        },
                        enabled = !compacting
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        ModelChip(
            label = currentModelLabel.ifBlank { Strings.agentModelChipLabel },
            onClick = onOpenModelPicker
        )
    }
}

private fun formatTokenUsage(used: Int, capacity: Int): String {
    val usedK = if (used >= 1000) "${used / 1000}K" else used.toString()
    val capK = if (capacity >= 1000) "${capacity / 1000}K" else capacity.toString()
    return "$usedK / $capK"
}

/**
 * Current-model chip at the trailing edge of the composer mode row. The model
 * name lives in an inner horizontally scrollable lane (capped, never
 * ellipsized), so long provider/model ids stay fully readable by swiping the
 * chip itself instead of truncating the label.
 */
@Composable
private fun ModelChip(
    label: String,
    onClick: () -> Unit
) {
    com.webtoapp.ui.design.WtaCard(
        onClick = onClick,
        tone = com.webtoapp.ui.design.WtaCardTone.Highlighted,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = WtaSpacing.Small + 2.dp,
            vertical = WtaSpacing.Tiny + 2.dp
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.SmartToy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(WtaSize.IconSmall - 2.dp)
            )
            Spacer(Modifier.width(WtaSpacing.Tiny + 2.dp))
            Box(
                modifier = Modifier
                    .widthIn(max = 200.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun ContextChip(
    label: String,
    warning: Boolean,
    compacting: Boolean,
    onClick: () -> Unit
) {
    com.webtoapp.ui.design.WtaCard(
        onClick = onClick,
        tone = com.webtoapp.ui.design.WtaCardTone.Surface,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = WtaSpacing.Small + 2.dp,
            vertical = WtaSpacing.Tiny + 2.dp
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (compacting) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp
                )
            } else {
                Icon(
                    imageVector = if (warning) Icons.Outlined.Warning else Icons.Outlined.DataUsage,
                    contentDescription = null,
                    tint = if (warning) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(Modifier.width(WtaSpacing.Tiny))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = if (warning) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primary: Boolean,
    onClick: () -> Unit
) {
    val tone = if (primary) com.webtoapp.ui.design.WtaCardTone.Highlighted
    else com.webtoapp.ui.design.WtaCardTone.Surface
    val onTone = if (primary) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
    com.webtoapp.ui.design.WtaCard(
        onClick = onClick,
        tone = tone,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = WtaSpacing.Small + 2.dp,
            vertical = WtaSpacing.Tiny + 2.dp
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = onTone,
                modifier = Modifier.size(WtaSize.IconSmall - 2.dp)
            )
            Spacer(Modifier.width(WtaSpacing.Tiny + 2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = onTone
            )
        }
    }
}
