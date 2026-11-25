package com.webtoapp.ui.agent.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.agent.session.UserAttachment
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.AgentUiState
import com.webtoapp.ui.agent.SlashCommand
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaDropdownMenu
import com.webtoapp.ui.design.WtaDropdownMenuItem
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.design.WtaMotion
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.rememberHapticClick
import com.webtoapp.ui.theme.LocalAnimationSettings
import com.webtoapp.ui.theme.LocalShowDescriptions

@Composable
fun Composer(
    state: AgentUiState,
    onTextChange: (TextFieldValue) -> Unit,
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
    val commandQuery = state.composerText.takeIf { it.startsWith("/") && it.none(Char::isWhitespace) }
        ?.removePrefix("/").orEmpty()
    val commands = if (state.slashOpen) {
        SlashCommand.entries.asSequence()
            .filter { it.command.contains(commandQuery, ignoreCase = true) }
            .sortedByDescending { it.command.removePrefix("/").startsWith(commandQuery, ignoreCase = true) }
            .take(6).toList()
    } else emptyList()
    val suggestions = if (state.slashOpen) {
        commands.map { command ->
            val (label, icon) = when (command) {
                SlashCommand.Model -> (if (state.modelMissing) Strings.addModel else Strings.selectModel) to Icons.Outlined.SmartToy
                SlashCommand.Plan -> Strings.agentPlanModeBadge to Icons.Outlined.Checklist
                SlashCommand.ReviewPlan -> Strings.agentPlanReview to Icons.Outlined.Check
                SlashCommand.Compact -> Strings.agentCompactNow to Icons.Outlined.Compress
                SlashCommand.NewSession -> Strings.agentNewSession to Icons.Outlined.Add
            }
            ComposerSuggestion(command.name, command.command, label, icon) { onRunSlashCommand(command) }
        }
    } else {
        state.mentionMatches.map { file ->
            ComposerSuggestion(
                file.relativePath,
                file.relativePath.substringAfterLast('/'),
                file.relativePath.substringBeforeLast('/', missingDelimiterValue = ""),
                Icons.Outlined.Description
            ) { onPickMention(file.relativePath) }
        }
    }
    val suggestionsOpen = state.slashOpen || state.mentionPickerOpen
    var highlighted by remember(state.slashOpen, state.mentionPickerOpen, commandQuery, state.mentionQuery) {
        mutableIntStateOf(-1)
    }
    val onDismissSuggestions = if (state.slashOpen) onDismissSlash else onDismissMention
    val handleSuggestionKey: (KeyEvent) -> Boolean = { event ->
        if (!suggestionsOpen || event.type != KeyEventType.KeyDown) false
        else when (event.key) {
            Key.Escape -> {
                onDismissSuggestions()
                true
            }
            Key.DirectionDown, Key.DirectionUp -> {
                if (suggestions.isEmpty()) false else {
                    highlighted = if (event.key == Key.DirectionDown) (highlighted + 1) % suggestions.size
                    else if (highlighted <= 0) suggestions.lastIndex else highlighted - 1
                    true
                }
            }
            Key.Enter, Key.Tab -> {
                val index = if (event.key == Key.Tab && highlighted < 0) 0 else highlighted
                suggestions.getOrNull(index)?.let { it.onPick(); true } ?: false
            }
            else -> false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (suggestionsOpen) {
            ComposerSuggestions(
                header = if (state.slashOpen) Strings.agentSlashHeader else Strings.agentMentionHeader,
                icon = if (state.slashOpen) Icons.Outlined.Terminal else Icons.Outlined.Description,
                suggestions = suggestions,
                highlighted = highlighted,
                emptyLabel = if (state.slashOpen || state.mentionQuery.isNotBlank()) Strings.noSearchResults
                    else Strings.agentMentionEmpty,
                onDismiss = onDismissSuggestions,
                modifier = Modifier.weight(1f, fill = false)
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
                val attachmentMotion = if (LocalAnimationSettings.current.enabled) {
                    Modifier.animateContentSize(WtaMotion.standardTween())
                } else Modifier
                Box(attachmentMotion) {
                    if (state.pendingAttachments.isNotEmpty()) {
                        PendingAttachmentsRow(
                            attachments = state.pendingAttachments,
                            onRemove = onRemoveAttachment,
                            resolveAttachmentPreview = resolveAttachmentPreview
                        )
                    }
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
                        value = state.composerValue,
                        onValueChange = onTextChange,
                        onSuggestionKey = handleSuggestionKey,
                        placeholder = composerPlaceholder(state)
                    )
                    SendButton(
                        working = state.isWorking,
                        enabled = state.canSend && !state.modelMissing &&
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
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSuggestionKey: (KeyEvent) -> Boolean,
    placeholder: String
) {
    val shape = RoundedCornerShape(WtaRadius.Control)
    var focused by remember { mutableStateOf(false) }
    val focusBorder by animateColorAsState(
        if (focused) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = WtaMotion.standardTween(),
        label = "composer focus"
    )
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .weight(1f)
            .onPreviewKeyEvent(onSuggestionKey)
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
            .border(1.dp, focusBorder, shape)
            .heightIn(min = WtaSize.TouchTarget, max = 120.dp)
            .padding(horizontal = WtaSpacing.Small, vertical = WtaSpacing.Small),
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.text.isEmpty()) {
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
        WtaDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            WtaDropdownMenuItem(
                text = Strings.agentAttachImage,
                leadingIcon = Icons.Outlined.Image,
                onClick = { menuOpen = false; onAttachImage() }
            )
            WtaDropdownMenuItem(
                text = Strings.agentAttachFile,
                leadingIcon = Icons.Outlined.InsertDriveFile,
                onClick = { menuOpen = false; onAttachFile() }
            )
            WtaDropdownMenuItem(
                text = Strings.agentAttachFolder,
                leadingIcon = Icons.Outlined.Folder,
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
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = WtaSpacing.Tiny),
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        items(attachments, key = { it.path }) { attachment ->
            Box(Modifier.animateItem()) {
                if (attachment.isImage) {
                    ImageAttachmentChip(attachment, onRemove, resolveAttachmentPreview(attachment))
                } else {
                    FileAttachmentChip(attachment, onRemove)
                }
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
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue = if (working) colors.secondaryContainer else colors.primary,
        animationSpec = WtaMotion.standardTween(),
        label = "send container"
    )
    val description = if (working) Strings.agentStopTooltip else Strings.agentSendTooltip
    FilledIconButton(
        onClick = rememberHapticClick(if (working) onCancel else onSend),
        enabled = working || enabled,
        shape = RoundedCornerShape(WtaRadius.Control),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = container,
            contentColor = if (working) colors.onSecondaryContainer else colors.onPrimary
        ),
        modifier = Modifier.size(WtaSize.TouchTarget).semantics { contentDescription = description }
    ) {
        Crossfade(
            targetState = working,
            animationSpec = WtaMotion.standardTween(WtaMotion.DurationQuick),
            label = "send action"
        ) { stopping ->
            Icon(
                imageVector = if (stopping) Icons.Outlined.Stop else Icons.AutoMirrored.Outlined.Send,
                contentDescription = null,
                modifier = Modifier.size(WtaSize.Icon)
            )
        }
    }
}

private data class ComposerSuggestion(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onPick: () -> Unit
)

@Composable
private fun ComposerSuggestions(
    header: String,
    icon: ImageVector,
    suggestions: List<ComposerSuggestion>,
    highlighted: Int,
    emptyLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val list = rememberLazyListState()
    LaunchedEffect(highlighted) {
        if (highlighted in suggestions.indices &&
            list.layoutInfo.visibleItemsInfo.none { it.index == highlighted }
        ) list.scrollToItem(highlighted)
    }
    WtaCard(
        modifier = modifier.fillMaxWidth().padding(horizontal = WtaSpacing.ScreenHorizontal),
        tone = WtaCardTone.Elevated,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = WtaSpacing.Medium, end = WtaSpacing.Tiny),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(WtaSize.IconSmall))
            Text(header, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            WtaIconButton(
                onClick = onDismiss,
                icon = Icons.Outlined.Close,
                contentDescription = Strings.close
            )
        }
        if (suggestions.isEmpty()) {
            Text(
                text = emptyLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = WtaSpacing.Medium, bottom = WtaSpacing.Medium)
            )
        } else {
            LazyColumn(
                state = list,
                modifier = Modifier.heightIn(max = 160.dp),
                contentPadding = PaddingValues(bottom = WtaSpacing.Tiny)
            ) {
                itemsIndexed(suggestions, key = { _, item -> item.id }) { index, item ->
                    val active = index == highlighted
                    val subtitle = item.subtitle.takeIf { it.isNotBlank() && LocalShowDescriptions.current }
                    ListItem(
                        headlineContent = {
                            Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        supportingContent = subtitle?.let { { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
                        leadingContent = {
                            Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(WtaSize.Icon))
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (active) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent
                        ),
                        modifier = Modifier
                            .semantics { selected = active }
                            .clickable(role = Role.Button, onClick = rememberHapticClick(item.onPick))
                    )
                }
            }
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
    val label = activity ?: return
    Row(
        modifier = Modifier.padding(
            horizontal = WtaSpacing.ScreenHorizontal,
            vertical = WtaSpacing.Tiny
        ),
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (LocalAnimationSettings.current.enabled) WtaLoadingIndicator(size = 14.dp)
        Crossfade(
            targetState = label,
            animationSpec = WtaMotion.standardTween(WtaMotion.DurationQuick),
            label = "turn activity"
        ) { displayed ->
            Text(
                text = displayed,
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
            modifier = Modifier.weight(1f).semantics {
                if (!state.modelMissing) onClick(label = Strings.selectModel, action = null)
            }
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
