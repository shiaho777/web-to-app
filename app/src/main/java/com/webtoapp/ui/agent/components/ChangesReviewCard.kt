package com.webtoapp.ui.agent.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.PendingChange
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaRadius
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
    Box(
        modifier = modifier.padding(
            horizontal = WtaSpacing.ScreenHorizontal,
            vertical = 2.dp
        )
    ) {
        // Dense review chrome: the default 48dp touch floor would put the
        // header buttons and undo icon back to the height this card is shrinking.
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        WtaCard(
            tone = WtaCardTone.Elevated,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            HeaderRow(
                count = changes.size,
                expanded = expanded,
                onToggle = onToggle,
                onUndoAll = onUndoAll,
                onClear = onClear
            )
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                            .copy(alpha = WtaAlpha.Divider)
                    )
                    changes.forEach { change ->
                        ChangeRow(
                            change = change,
                            onUndo = { onUndoOne(change.path) }
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun HeaderRow(
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUndoAll: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = WtaSpacing.Small, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = if (expanded) Strings.agentChangesReviewCollapse
            else Strings.agentChangesReviewExpand,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(WtaSize.IconSmall)
        )
        Spacer(Modifier.width(WtaSpacing.Tiny))
        Text(
            text = Strings.agentChangesReviewHeader.format(count),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        ReviewActionButton(
            text = Strings.agentChangesReviewUndoAll,
            onClick = onUndoAll,
            tonal = false
        )
        ReviewActionButton(
            text = Strings.agentChangesReviewClear,
            onClick = onClear,
            tonal = true
        )
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
            .padding(horizontal = WtaSpacing.Small, vertical = 2.dp),
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
        IconButton(
            onClick = onUndo,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Undo,
                contentDescription = Strings.agentChangesReviewUndoOne,
                modifier = Modifier.size(WtaSize.IconSmall)
            )
        }
    }
}

/** Header action short enough to leave the conversation the vertical room. */
@Composable
internal fun ReviewActionButton(
    text: String,
    onClick: () -> Unit,
    tonal: Boolean
) {
    val shape = RoundedCornerShape(WtaRadius.Button)
    val padding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
    val buttonModifier = Modifier.height(28.dp)
    val label: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
    if (tonal) {
        FilledTonalButton(
            onClick = onClick,
            modifier = buttonModifier,
            shape = shape,
            contentPadding = padding,
            content = label
        )
    } else {
        TextButton(
            onClick = onClick,
            modifier = buttonModifier,
            shape = shape,
            contentPadding = padding,
            content = label
        )
    }
}

@Composable
private fun KindBadge(kind: PendingChange.Kind) {
    val (label, container, content, icon) = when (kind) {
        PendingChange.Kind.Write -> KindStyle(
            label = "WRITE",
            container = WtaColors.semantic.successContainer,
            content = WtaColors.semantic.onSuccessContainer,
            icon = Icons.AutoMirrored.Outlined.NoteAdd
        )
        PendingChange.Kind.Edit -> KindStyle(
            label = "EDIT",
            container = WtaColors.semantic.warningContainer,
            content = WtaColors.semantic.onWarningContainer,
            icon = Icons.Outlined.Edit
        )
        PendingChange.Kind.Delete -> KindStyle(
            label = "DELETE",
            container = WtaColors.semantic.errorContainer,
            content = WtaColors.semantic.onErrorContainer,
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
