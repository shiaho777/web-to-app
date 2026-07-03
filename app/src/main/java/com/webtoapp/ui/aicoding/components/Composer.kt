package com.webtoapp.ui.aicoding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.aicoding.skill.Skill
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.aicoding.AiCodingUiState
import com.webtoapp.ui.aicoding.SlashCommand
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaTextField

@Composable
fun Composer(
    state: AiCodingUiState,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    onPickSkill: (Skill) -> Unit,
    onRunSlashCommand: (SlashCommand) -> Unit,
    onDismissSlash: () -> Unit,
    onPickMention: (String) -> Unit,
    onDismissMention: () -> Unit,
    onToggleAutoApprove: () -> Unit,

    onTriggerSlash: () -> Unit,

    onOpenModelPicker: () -> Unit = {},
    onCompactContext: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.slashOpen) {
            SlashSuggestions(
                skillMatches = state.slashSuggestions,
                commandMatches = state.slashCommands.filter {
                    val q = state.composerText.removePrefix("/").lowercase()
                    q.isEmpty() || it.command.lowercase().contains(q)
                }.take(6),
                onPickSkill = onPickSkill,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WtaSpacing.ScreenHorizontal,
                    vertical = WtaSpacing.Small
                ),
            verticalAlignment = Alignment.Bottom
        ) {
            WtaTextField(
                value = state.composerText,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = WtaSize.TextFieldHeight, max = 220.dp),
                placeholder = composerPlaceholder(state),
                singleLine = false,
                maxLines = 6
            )
            Spacer(Modifier.width(WtaSpacing.Small))
            SendButton(
                working = state.isWorking,
                enabled = state.canSend && state.composerText.isNotBlank(),
                onSend = onSend,
                onCancel = onCancel
            )
        }

        ModeChipRow(
            autoApprove = state.autoApprove,
            onToggleAuto = onToggleAutoApprove,
            onTriggerSlash = onTriggerSlash,
            currentModelLabel = state.currentModelLabel,
            onOpenModelPicker = onOpenModelPicker,
            estimatedTokens = state.estimatedContextTokens,
            contextCapacity = state.contextCapacity,
            compacting = state.compacting,
            onCompactContext = onCompactContext
        )
    }
}

private fun composerPlaceholder(state: AiCodingUiState): String = when {
    state.isWorking -> Strings.aiCodingComposerHintWorking
    else -> Strings.aiCodingComposerHintIdle
}

@Composable
private fun SendButton(
    working: Boolean,
    enabled: Boolean,
    onSend: () -> Unit,
    onCancel: () -> Unit
) {
    if (working) {
        WtaButton(
            onClick = onCancel,
            variant = WtaButtonVariant.Destructive,
            size = WtaButtonSize.Medium
        ) {
            Icon(
                Icons.Outlined.Stop,
                contentDescription = Strings.aiCodingStopTooltip,
                modifier = Modifier.size(WtaSize.Icon)
            )
        }
    } else {
        WtaButton(
            onClick = onSend,
            variant = WtaButtonVariant.Primary,
            size = WtaButtonSize.Medium,
            enabled = enabled
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.Send,
                contentDescription = Strings.aiCodingSendTooltip,
                modifier = Modifier.size(WtaSize.Icon)
            )
        }
    }
}

@Composable
private fun SlashSuggestions(
    skillMatches: List<Skill>,
    commandMatches: List<SlashCommand>,
    onPickSkill: (Skill) -> Unit,
    onPickCommand: (SlashCommand) -> Unit,
    onDismiss: () -> Unit
) {
    if (skillMatches.isEmpty() && commandMatches.isEmpty()) return
    SuggestionsCard(
        leading = "/",
        header = Strings.aiCodingSlashHeader,
        onDismiss = onDismiss
    ) {
        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
            items(skillMatches, key = { "skill-${it.name}" }) { skill ->
                SuggestionRow(
                    title = "/${skill.name}",
                    subtitle = skill.description.take(80),
                    hint = if (skill.argumentHint.isNotBlank())
                        Strings.aiCodingSlashArgsLabel.format(skill.argumentHint)
                    else null,
                    iconName = skill.icon,
                    iconColor = skill.iconColor,
                    onClick = { onPickSkill(skill) }
                )
            }
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
    matches: List<com.webtoapp.core.aicoding.files.ProjectFileManager.FileInfo>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    SuggestionsCard(
        leading = "@",
        header = Strings.aiCodingMentionHeader,
        onDismiss = onDismiss
    ) {
        if (matches.isEmpty()) {
            Text(
                text = Strings.aiCodingMentionEmpty,
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
                    contentDescription = Strings.aiCodingSlashClose,
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
    onCompactContext: () -> Unit
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
            label = if (autoApprove) Strings.aiCodingAutoModeLabel
            else Strings.aiCodingManualModeLabel,
            icon = if (autoApprove) Icons.Outlined.Bolt else Icons.Outlined.Lock,
            primary = autoApprove,
            onClick = onToggleAuto
        )

        ModeChip(
            label = Strings.aiCodingSlashChipLabel,
            icon = Icons.Outlined.AutoAwesome,
            primary = false,
            onClick = onTriggerSlash
        )

        if (contextCapacity > 0) {
            val usageLabel = formatTokenUsage(estimatedTokens, contextCapacity)
            val usageRatio = if (contextCapacity > 0) estimatedTokens.toFloat() / contextCapacity else 0f
            val usageHigh = usageRatio >= 0.75f
            ContextChip(
                label = usageLabel,
                warning = usageHigh,
                compacting = compacting,
                onClick = { showCompactMenu = true }
            )
        }

        Spacer(Modifier.weight(1f))

        ModelChip(
            label = currentModelLabel.ifBlank { Strings.aiCodingModelChipLabel },
            onClick = onOpenModelPicker
        )
    }

    if (showCompactMenu) {
        androidx.compose.material3.DropdownMenu(
            expanded = true,
            onDismissRequest = { showCompactMenu = false }
        ) {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(Strings.aiCodingCompactNow) },
                onClick = {
                    showCompactMenu = false
                    onCompactContext()
                },
                enabled = !compacting
            )
        }
    }
}

private fun formatTokenUsage(used: Int, capacity: Int): String {
    val usedK = if (used >= 1000) "${used / 1000}K" else used.toString()
    val capK = if (capacity >= 1000) "${capacity / 1000}K" else capacity.toString()
    return "$usedK / $capK"
}

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
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 140.dp)
            )
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
            horizontal = WtaSpacing.Small,
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
