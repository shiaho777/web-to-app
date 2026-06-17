package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webtoapp.core.errorpage.ErrorPageConfig
import com.webtoapp.core.errorpage.ErrorPageMode
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaToggleRow

@Composable
fun NetworkErrorPageCard(
    enabled: Boolean,
    config: ErrorPageConfig,
    onEnabledChange: (Boolean) -> Unit,
    onConfigChange: (ErrorPageConfig) -> Unit
) {
    WtaSettingCard {
        Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)) {

            WtaToggleRow(
                icon = Icons.Outlined.WifiOff,
                title = Strings.modifierErrorPageTitle,
                subtitle = null,
                checked = enabled,
                onCheckedChange = { checked ->
                    onEnabledChange(checked)
                    if (checked && config.mode == ErrorPageMode.DEFAULT) {
                        onConfigChange(config.copy(mode = ErrorPageMode.BUILTIN_STYLE))
                    }
                }
            )

            AnimatedVisibility(
                visible = enabled,
                enter = CardExpandTransition,
                exit = CardCollapseTransition
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = WtaSpacing.RowHorizontal,
                        vertical = WtaSpacing.ContentGap
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {

                    ErrorPageModeRow(
                        title = Strings.modifierErrorPageDefault,
                        hint = Strings.modifierErrorPageDefaultHint,
                        selected = config.mode == ErrorPageMode.BUILTIN_STYLE,
                        onClick = { onConfigChange(config.copy(mode = ErrorPageMode.BUILTIN_STYLE)) }
                    )

                    WtaSectionDivider(modifier = Modifier.padding(horizontal = 0.dp))

                    ErrorPageModeRow(
                        title = Strings.modifierErrorPageCustom,
                        hint = Strings.modifierErrorPageCustomHint,
                        selected = config.mode == ErrorPageMode.CUSTOM_HTML,
                        onClick = { onConfigChange(config.copy(mode = ErrorPageMode.CUSTOM_HTML)) }
                    )

                    AnimatedVisibility(
                        visible = config.mode == ErrorPageMode.CUSTOM_HTML,
                        enter = CardExpandTransition,
                        exit = CardCollapseTransition
                    ) {
                        Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp, bottom = 8.dp)) {
                            PremiumTextField(
                                value = config.customHtml ?: "",
                                onValueChange = { onConfigChange(config.copy(customHtml = it.ifBlank { null })) },
                                label = { Text(Strings.modifierErrorPageCustomHtmlLabel) },
                                supportingText = {
                                    Text(
                                        Strings.modifierErrorPageCustomHtmlHint,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                minLines = 4,
                                maxLines = 8,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    WtaSectionDivider(modifier = Modifier.padding(horizontal = 0.dp))

                    ErrorPageModeRow(
                        title = Strings.modifierErrorPageSuppressed,
                        hint = Strings.modifierErrorPageSuppressedHint,
                        selected = config.mode == ErrorPageMode.SUPPRESSED,
                        onClick = { onConfigChange(config.copy(mode = ErrorPageMode.SUPPRESSED)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorPageModeRow(
    title: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
