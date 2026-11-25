package com.webtoapp.ui.agent.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.PendingChange
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing

@Composable
fun ChangesReviewCard(
    changes: List<PendingChange>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUndoOne: (path: String) -> Unit,
    onUndoAll: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (changes.isEmpty()) return
    AgentReviewCard(
        title = Strings.agentChangesReviewHeader.format(changes.size),
        icon = Icons.Outlined.FolderOpen,
        expanded = expanded,
        onToggle = onToggle,
        modifier = modifier,
        actions = {
            WtaButton(
                text = Strings.agentChangesReviewUndoAll,
                onClick = onUndoAll,
                variant = WtaButtonVariant.Text,
                size = WtaButtonSize.Small
            )
            WtaButton(
                text = Strings.agentChangesReviewClear,
                onClick = onClear,
                variant = WtaButtonVariant.Tonal,
                size = WtaButtonSize.Small
            )
        }
    ) {
        changes.forEach { change -> ChangeRow(change, onUndo = { onUndoOne(change.path) }) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AgentReviewCard(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable FlowRowScope.() -> Unit,
    content: @Composable () -> Unit
) {
    WtaCard(
        tone = WtaCardTone.Elevated,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (expanded) Strings.agentChangesReviewCollapse else Strings.agentChangesReviewExpand,
                    onClick = onToggle
                )
                .heightIn(min = WtaSize.TouchTarget)
                .padding(horizontal = WtaSpacing.Medium, vertical = WtaSpacing.Tiny),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(WtaSize.IconSmall))
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            AgentDisclosureIcon(expanded)
        }
        AgentReveal(visible = expanded) {
            Column {
                WtaSectionDivider()
                content()
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = WtaSpacing.Medium),
                    horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small, Alignment.End),
                    content = actions
                )
            }
        }
    }
}

@Composable
private fun ChangeRow(
    change: PendingChange,
    onUndo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = WtaSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
    ) {
        KindBadge(change.kind)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = change.path.substringAfterLast('/'),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val parent = change.path.substringBeforeLast('/', missingDelimiterValue = "")
            if (parent.isNotEmpty()) {
                Text(
                    text = parent,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        WtaIconButton(
            onClick = onUndo,
            icon = Icons.AutoMirrored.Outlined.Undo,
            contentDescription = Strings.agentChangesReviewUndoOne
        )
    }
}

@Composable
private fun KindBadge(kind: PendingChange.Kind) {
    val (label, container, content, icon) = when (kind) {
        PendingChange.Kind.Write -> KindStyle(
            label = Strings.add,
            container = WtaColors.semantic.successContainer,
            content = WtaColors.semantic.onSuccessContainer,
            icon = Icons.AutoMirrored.Outlined.NoteAdd
        )
        PendingChange.Kind.Edit -> KindStyle(
            label = Strings.edit,
            container = WtaColors.semantic.neutralContainer,
            content = WtaColors.semantic.onNeutralContainer,
            icon = Icons.Outlined.Edit
        )
        PendingChange.Kind.Delete -> KindStyle(
            label = Strings.delete,
            container = WtaColors.semantic.neutralContainer,
            content = WtaColors.semantic.onNeutralContainer,
            icon = Icons.Outlined.Delete
        )
    }
    WtaInfoChip(
        label = label,
        icon = icon,
        containerColor = container,
        contentColor = content
    )
}

private data class KindStyle(
    val label: String,
    val container: androidx.compose.ui.graphics.Color,
    val content: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
