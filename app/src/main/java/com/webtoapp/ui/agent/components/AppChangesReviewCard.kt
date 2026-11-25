package com.webtoapp.ui.agent.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.PendingAppChange
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing

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
    AgentReviewCard(
        title = Strings.agentAppChangesHeader.format(changes.size),
        icon = Icons.Outlined.Apps,
        expanded = expanded,
        onToggle = onToggle,
        modifier = modifier,
        actions = {
            WtaButton(
                text = Strings.agentChangesReviewClear,
                onClick = onClear,
                variant = WtaButtonVariant.Tonal,
                size = WtaButtonSize.Small
            )
        }
    ) {
        changes.forEach { change -> AppChangeRow(change, onOpen = { onOpenApp(change.appId) }) }
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
            .clickable(role = Role.Button, onClickLabel = Strings.agentAppChangesOpen, onClick = onOpen)
            .heightIn(min = WtaSize.TouchTarget)
            .padding(horizontal = WtaSpacing.Medium, vertical = WtaSpacing.Tiny),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        KindBadge(created = change.created)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = change.appName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            contentDescription = null,
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
            container = WtaColors.semantic.neutralContainer,
            content = WtaColors.semantic.onNeutralContainer,
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
