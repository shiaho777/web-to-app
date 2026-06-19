package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.NotificationExportConfig
import com.webtoapp.data.model.NotificationType
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaDivider
import com.webtoapp.ui.design.WtaFeatureCard
import com.webtoapp.ui.design.WtaFeatureCardHeader
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationConfigCard(
    enabled: Boolean,
    config: NotificationExportConfig,
    onEnabledChange: (Boolean) -> Unit,
    onConfigChange: (NotificationExportConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    WtaFeatureCard(modifier = modifier) {
        WtaFeatureCardHeader(
            icon = Icons.Outlined.Notifications,
            title = Strings.notificationConfigTitle,
            subtitle = if (!enabled) Strings.notEnabled else null,
            enabled = enabled,
            trailing = {
                WtaSwitch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }
        )

        AnimatedVisibility(visible = enabled) {
            Column(
                modifier = Modifier.padding(top = WtaSpacing.Large),
                verticalArrangement = Arrangement.spacedBy(WtaSpacing.Medium)
            ) {
                WtaDivider()
                Spacer(modifier = Modifier.height(WtaSpacing.Large))

                Text(
                    Strings.notificationTypeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NotificationTypeChip(
                        selected = config.type == NotificationType.WEB_API,
                        label = Strings.notificationTypeWebApi,
                        onClick = { onConfigChange(config.copy(type = NotificationType.WEB_API)) }
                    )
                    NotificationTypeChip(
                        selected = config.type == NotificationType.POLLING,
                        label = Strings.notificationTypePolling,
                        onClick = { onConfigChange(config.copy(type = NotificationType.POLLING)) }
                    )
                }

                if (config.type == NotificationType.WEB_API) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            Strings.notificationWebApiDesc,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (config.type == NotificationType.POLLING) {
                    PremiumTextField(
                        value = config.pollUrl,
                        onValueChange = { onConfigChange(config.copy(pollUrl = it)) },
                        label = { Text(Strings.notificationPollUrl) },
                        placeholder = { Text(Strings.notificationPollUrlPlaceholder) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            Strings.notificationPollInterval,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        var intervalText by remember(config.pollIntervalMinutes) {
                            mutableStateOf(config.pollIntervalMinutes.toString())
                        }
                        OutlinedTextField(
                            value = intervalText,
                            onValueChange = { newText ->
                                intervalText = newText
                                newText.toIntOrNull()?.let { num ->
                                    onConfigChange(config.copy(pollIntervalMinutes = num.coerceAtLeast(5)))
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    Text(
                        Strings.notificationPollIntervalHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(if (expanded) Strings.hideAdvanced else Strings.showAdvanced)
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Medium)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Strings.notificationPollMethod,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    WtaChip(
                                        selected = config.pollMethod == "GET",
                                        onClick = { onConfigChange(config.copy(pollMethod = "GET")) },
                                        label = "GET",
                                        showSelectedCheck = false
                                    )
                                    WtaChip(
                                        selected = config.pollMethod == "POST",
                                        onClick = { onConfigChange(config.copy(pollMethod = "POST")) },
                                        label = "POST",
                                        showSelectedCheck = false
                                    )
                                }
                            }

                            PremiumTextField(
                                value = config.pollHeaders,
                                onValueChange = { onConfigChange(config.copy(pollHeaders = it)) },
                                label = { Text(Strings.notificationPollHeaders) },
                                placeholder = { Text(Strings.notificationPollHeadersPlaceholder) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 4
                            )

                            PremiumTextField(
                                value = config.clickUrl,
                                onValueChange = { onConfigChange(config.copy(clickUrl = it)) },
                                label = { Text(Strings.notificationClickUrl) },
                                placeholder = { Text(Strings.notificationClickUrlPlaceholder) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationTypeChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    WtaChip(
        selected = selected,
        onClick = onClick,
        label = label
    )
}
