package com.webtoapp.ui.aicoding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.aicoding.ModelChoice
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
    choices: List<ModelChoice>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny + 2.dp)
            ) {
                items(choices, key = { it.id }) { choice ->
                    ModelRow(
                        choice = choice,
                        onClick = { onSelect(choice.id) }
                    )
                }
            }
        }
    )
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
            horizontal = WtaSpacing.Medium,
            vertical = WtaSpacing.Small + 2.dp
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
                modifier = Modifier.size(WtaSize.Icon)
            )
            Spacer(Modifier.width(WtaSpacing.Small))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = choice.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onTone,
                    maxLines = 1
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    text = choice.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSubdued,
                    maxLines = 1
                )
            }
        }
    }
}
