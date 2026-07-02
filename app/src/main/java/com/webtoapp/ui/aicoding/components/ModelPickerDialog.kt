package com.webtoapp.ui.aicoding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.aicoding.ModelChoice
import com.webtoapp.ui.aicoding.ProviderGroup
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing

@Composable
fun ModelPickerDialog(
    groups: List<ProviderGroup>,
    initialSelectedProviderKeyId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProviderKeyId by remember {
        mutableStateOf(initialSelectedProviderKeyId ?: groups.firstOrNull()?.apiKeyId)
    }
    val currentGroup = groups.firstOrNull { it.apiKeyId == selectedProviderKeyId } ?: groups.firstOrNull()

    WtaAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.SmartToy,
        title = Strings.aiCodingModelPickerTitle,
        confirmButton = {
            WtaButton(
                onClick = onDismiss,
                text = Strings.aiCodingActionCancel,
                variant = WtaButtonVariant.Text,
                size = WtaButtonSize.Small
            )
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp, max = 380.dp)
            ) {
                ProviderColumn(
                    groups = groups,
                    selectedKeyId = selectedProviderKeyId,
                    onSelect = { selectedProviderKeyId = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(WtaSpacing.Small))
                ModelColumn(
                    group = currentGroup,
                    onSelect = onSelect,
                    modifier = Modifier.weight(1.4f)
                )
            }
        }
    )
}

@Composable
private fun ProviderColumn(
    groups: List<ProviderGroup>,
    selectedKeyId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = Strings.aiCodingModelPickerProviders,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = WtaSpacing.Small)
        )
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny + 2.dp)
        ) {
            items(groups, key = { it.apiKeyId }) { group ->
                ProviderRow(
                    name = group.displayName,
                    count = group.models.size,
                    selected = group.apiKeyId == selectedKeyId,
                    onClick = { onSelect(group.apiKeyId) }
                )
            }
        }
    }
}

@Composable
private fun ModelColumn(
    group: ProviderGroup?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = Strings.aiCodingModelPickerModels,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = WtaSpacing.Small)
        )
        val models = group?.models ?: emptyList()
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny + 2.dp)
        ) {
            items(models, key = { it.id }) { choice ->
                ModelRow(
                    choice = choice,
                    onClick = { onSelect(choice.id) }
                )
            }
        }
    }
}

@Composable
private fun ProviderRow(
    name: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tone = if (selected) WtaCardTone.Highlighted else WtaCardTone.Surface
    val onTone = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
    val onSubdued = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = WtaAlpha.Strong)
    } else MaterialTheme.colorScheme.onSurfaceVariant

    WtaCard(
        onClick = onClick,
        tone = tone,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = WtaSpacing.Small + 2.dp,
            vertical = WtaSpacing.Small
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Verified,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(WtaSize.IconSmall)
            )
            Spacer(Modifier.width(WtaSpacing.Small))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onTone,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSubdued
                )
            }
        }
    }
}

@Composable
private fun ModelRow(
    choice: ModelChoice,
    onClick: () -> Unit
) {
    val selected = choice.selected
    val tone = if (selected) WtaCardTone.Highlighted else WtaCardTone.Surface
    val onTone = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
    val onSubdued = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = WtaAlpha.Strong)
    } else MaterialTheme.colorScheme.onSurfaceVariant

    WtaCard(
        onClick = onClick,
        tone = tone,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = WtaSpacing.Small + 2.dp,
            vertical = WtaSpacing.Small
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (selected) Icons.Outlined.RadioButtonChecked
                else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(WtaSize.IconSmall)
            )
            Spacer(Modifier.width(WtaSpacing.Small))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = choice.label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = onTone,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = choice.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSubdued,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
