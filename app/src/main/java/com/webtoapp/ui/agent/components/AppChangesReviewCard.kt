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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.PendingAppChange
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing

/**
 * Review card for apps created/updated by agent tools during a turn — the app-facing
 * counterpart of [ChangesReviewCard]. Shows what changed (name, type, patched fields)
 * and jumps to the app's editor on tap.
 */
@Composable
fun AppChangesReviewCard(
    changes: List<PendingAppChange>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onClear: () -> Unit,
    onOpenApp: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (changes.isEmpty()) return
    Box(
        modifier = modifier.padding(
            horizontal = WtaSpacing.ScreenHorizontal,
            vertical = WtaSpacing.Tiny + 2.dp
        )
    ) {
        WtaCard(
            tone = WtaCardTone.Elevated,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            HeaderRow(
                count = changes.size,
                expanded = expanded,
                onToggle = onToggle,
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
                        AppChangeRow(
                            change = change,
                            onOpen = { onOpenApp(change.appId) }
                        )
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
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(
                horizontal = WtaSpacing.Medium,
                vertical = WtaSpacing.Small + 2.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = if (expanded) Strings.agentChangesReviewCollapse
            else Strings.agentChangesReviewExpand,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(WtaSize.IconSmall)
        )
        Spacer(Modifier.width(WtaSpacing.Small))
        Text(
            text = Strings.agentAppChangesHeader.format(count),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        WtaButton(
            onClick = onClear,
            text = Strings.agentChangesReviewClear,
            variant = WtaButtonVariant.Tonal,
            size = WtaButtonSize.Small
        )
    }
}

@Composable
private fun AppChangeRow(
    change: PendingAppChange,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(
                horizontal = WtaSpacing.Medium,
                vertical = WtaSpacing.Small
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        KindBadge(created = change.created)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = change.appName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            if (change.changedFields.isEmpty()) {
                Text(
                    text = change.appType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            } else {
                Text(
                    text = Strings.agentAppChangesFields.format(
                        change.changedFields.take(6).joinToString(", ") +
                            if (change.changedFields.size > 6) " …" else ""
                    ),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = Strings.agentAppChangesOpen,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(WtaSize.IconSmall)
        )
    }
}

@Composable
private fun KindBadge(created: Boolean) {
    val (label, container, content, icon) = if (created) {
        BadgeStyle(
            label = Strings.agentAppChangesKindCreate,
            container = WtaColors.semantic.successContainer,
            content = WtaColors.semantic.onSuccessContainer,
            icon = Icons.Outlined.AddCircle
        )
    } else {
        BadgeStyle(
            label = Strings.agentAppChangesKindUpdate,
            container = WtaColors.semantic.warningContainer,
            content = WtaColors.semantic.onWarningContainer,
            icon = Icons.Outlined.Edit
        )
    }
    WtaInfoChip(
        label = label,
        icon = icon,
        containerColor = container,
        contentColor = content
    )
}

private data class BadgeStyle(
    val label: String,
    val container: androidx.compose.ui.graphics.Color,
    val content: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
