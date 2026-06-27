package com.webtoapp.ui.aicoding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.aicoding.permission.ChoiceRequest
import com.webtoapp.core.aicoding.permission.ChoiceResponse
import com.webtoapp.core.aicoding.permission.PermissionRequest
import com.webtoapp.core.aicoding.permission.PermissionResponse
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaTextField

@Composable
fun PermissionDialog(
    request: PermissionRequest,
    onResponse: (PermissionResponse) -> Unit
) {
    WtaAlertDialog(
        onDismissRequest = { onResponse(PermissionResponse.Deny) },
        icon = Icons.Outlined.Shield,
        iconTint = MaterialTheme.colorScheme.primary,
        title = request.toolName,
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WtaButton(
                    onClick = { onResponse(PermissionResponse.AlwaysAllow) },
                    text = Strings.aiCodingPermissionAlways,
                    variant = WtaButtonVariant.Text,
                    size = WtaButtonSize.Small
                )
                WtaButton(
                    onClick = { onResponse(PermissionResponse.Allow) },
                    text = Strings.aiCodingPermissionAllow,
                    variant = WtaButtonVariant.Primary,
                    size = WtaButtonSize.Small
                )
            }
        },
        dismissButton = {
            WtaButton(
                onClick = { onResponse(PermissionResponse.Deny) },
                text = Strings.aiCodingPermissionDeny,
                variant = WtaButtonVariant.Text,
                size = WtaButtonSize.Small
            )
        },
        content = {
            Text(
                text = Strings.aiCodingPermissionRequired,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            request.activity?.let { activity ->
                Text(
                    text = activity,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (request.argsPreview.isNotEmpty()) {
                WtaCard(
                    tone = WtaCardTone.Elevated,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(WtaSpacing.Medium)
                ) {
                    request.argsPreview.entries.forEachIndexed { idx, (k, v) ->
                        if (idx > 0) Spacer(Modifier.height(WtaSpacing.Tiny))
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "$k:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(96.dp)
                            )
                            Text(
                                text = v,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ChoiceBottomSheet(
    request: ChoiceRequest,
    onResponse: (ChoiceResponse) -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val singleSelections = remember(request.id) { mutableStateMapOf<Int, String>() }
    val multiSelections = remember(request.id) { mutableStateMapOf<Int, MutableSet<String>>() }
    val otherTexts = remember(request.id) { mutableStateMapOf<Int, String>() }

    ModalBottomSheet(
        onDismissRequest = { onResponse(ChoiceResponse.Cancelled) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(
            topStart = WtaRadius.Dialog,
            topEnd = WtaRadius.Dialog
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = WtaSpacing.ScreenHorizontal,
                vertical = WtaSpacing.Small
            ),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Medium)
        ) {
            Text(
                text = Strings.aiCodingChoiceTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            request.questions.forEachIndexed { qIdx, q ->
                Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
                    Text(
                        text = q.text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    q.options.forEach { opt ->
                        OptionRow(
                            label = opt.label,
                            description = opt.description,
                            selected = if (q.multiSelect) {
                                multiSelections[qIdx]?.contains(opt.label) == true
                            } else {
                                singleSelections[qIdx] == opt.label
                            },
                            multi = q.multiSelect,
                            onToggle = {
                                if (q.multiSelect) {
                                    val set = multiSelections.getOrPut(qIdx) { mutableSetOf() }
                                    if (!set.add(opt.label)) set.remove(opt.label)
                                    multiSelections[qIdx] = set
                                } else {
                                    singleSelections[qIdx] = opt.label
                                }
                            }
                        )
                    }
                    if (q.allowOther) {
                        WtaTextField(
                            value = otherTexts[qIdx].orEmpty(),
                            onValueChange = { otherTexts[qIdx] = it },
                            placeholder = Strings.aiCodingChoiceOtherHint,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (qIdx < request.questions.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                            .copy(alpha = WtaAlpha.Divider)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny, Alignment.End),
                modifier = Modifier.fillMaxWidth()
            ) {
                WtaButton(
                    onClick = { onResponse(ChoiceResponse.Cancelled) },
                    text = Strings.aiCodingActionCancel,
                    variant = WtaButtonVariant.Text,
                    size = WtaButtonSize.Medium
                )
                WtaButton(
                    onClick = {
                        val answers = request.questions.indices.map { i ->
                            val picks = mutableListOf<String>()
                            if (request.questions[i].multiSelect) {
                                picks += multiSelections[i]?.toList().orEmpty()
                            } else {
                                singleSelections[i]?.let { picks += it }
                            }
                            otherTexts[i]?.takeIf { it.isNotBlank() }?.let { picks += it }
                            picks.toList()
                        }
                        onResponse(ChoiceResponse.Answered(answers))
                    },
                    text = Strings.aiCodingActionSubmit,
                    variant = WtaButtonVariant.Primary,
                    size = WtaButtonSize.Medium
                )
            }
            Spacer(Modifier.height(WtaSpacing.Tiny))
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    description: String,
    selected: Boolean,
    multi: Boolean,
    onToggle: () -> Unit
) {
    val tone = if (selected) WtaCardTone.Highlighted else WtaCardTone.Surface
    val onContainer = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
    WtaCard(
        onClick = onToggle,
        tone = tone,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = WtaSpacing.Medium,
            vertical = WtaSpacing.Small + 2.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icon = when {
                !selected -> Icons.Outlined.RadioButtonUnchecked
                multi -> Icons.Outlined.CheckCircle
                else -> Icons.Outlined.RadioButtonChecked
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else onContainer.copy(alpha = WtaAlpha.Strong),
                modifier = Modifier.size(WtaSize.Icon)
            )
            Spacer(Modifier.width(WtaSpacing.Small))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = onContainer
            )
            if (description.isNotBlank()) {
                Spacer(Modifier.width(WtaSpacing.Small))
                Text(
                    text = "— $description",
                    style = MaterialTheme.typography.bodySmall,
                    color = onContainer.copy(alpha = WtaAlpha.Strong),
                    maxLines = 1
                )
            }
        }
    }
}
