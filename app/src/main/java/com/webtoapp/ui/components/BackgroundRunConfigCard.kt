package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.webtoapp.core.background.BackgroundRunService
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.BackgroundRunExportConfig
import com.webtoapp.ui.design.WtaDivider
import com.webtoapp.ui.design.WtaFeatureCard
import com.webtoapp.ui.design.WtaFeatureCardHeader
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundRunConfigCard(
    enabled: Boolean,
    config: BackgroundRunExportConfig,
    onEnabledChange: (Boolean) -> Unit,
    onConfigChange: (BackgroundRunExportConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    WtaFeatureCard(modifier = modifier) {
        WtaFeatureCardHeader(
            icon = Icons.Outlined.PlayArrow,
            title = Strings.backgroundRunTitle,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        Strings.backgroundRunShowNotification,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    WtaSwitch(
                        checked = config.showNotification,
                        onCheckedChange = { onConfigChange(config.copy(showNotification = it)) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        Strings.backgroundRunKeepCpuAwake,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    WtaSwitch(
                        checked = config.keepCpuAwake,
                        onCheckedChange = { onConfigChange(config.copy(keepCpuAwake = it)) }
                    )
                }

                OutlinedButton(
                    onClick = { BackgroundRunService.requestIgnoreBatteryOptimizations(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Outlined.BatteryStd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            Strings.backgroundRunBatteryOptimization,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            Strings.backgroundRunBatteryOptimizationDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (expanded) Strings.hideAdvanced else Strings.showAdvanced)
                }

                AnimatedVisibility(visible = expanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Medium)) {
                        PremiumTextField(
                            value = config.notificationTitle,
                            onValueChange = { onConfigChange(config.copy(notificationTitle = it)) },
                            label = { Text(Strings.backgroundRunNotificationTitle) },
                            placeholder = { Text(Strings.backgroundRunNotificationTitlePlaceholder) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        PremiumTextField(
                            value = config.notificationContent,
                            onValueChange = { onConfigChange(config.copy(notificationContent = it)) },
                            label = { Text(Strings.backgroundRunNotificationContent) },
                            placeholder = { Text(Strings.backgroundRunNotificationContentPlaceholder) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}
