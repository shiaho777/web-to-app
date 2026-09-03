package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webtoapp.core.appearance.DeviceDisguiseConfig
import com.webtoapp.core.appearance.DeviceType
import com.webtoapp.core.appearance.DevicePresets
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaToggleRow
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.components.SettingsSwitch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeviceDisguiseCard(
    config: DeviceDisguiseConfig,
    onConfigChange: (DeviceDisguiseConfig) -> Unit
) {
    var showCustomUA by remember { mutableStateOf(false) }
    var showCustomDevice by remember { mutableStateOf(false) }

    var customModelName by remember { mutableStateOf("") }
    var customModelId by remember { mutableStateOf("") }
    var customScreenW by remember { mutableStateOf("") }
    var customScreenH by remember { mutableStateOf("") }
    var customDensity by remember { mutableStateOf("") }

    val isEnabled = config.enabled

    WtaSettingCard {
        WtaToggleRow(
            icon = Icons.Outlined.DevicesOther,
            title = Strings.deviceDisguiseTitle,
            checked = isEnabled,
            onCheckedChange = { onConfigChange(config.copy(enabled = it)) }
        )

        AnimatedVisibility(
            visible = isEnabled,
            enter = CardExpandTransition,
            exit = CardCollapseTransition
        ) {
            Column {
                WtaSectionDivider()

                Text(
                    text = Strings.deviceQuickSelect,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        start = WtaSpacing.RowHorizontal,
                        top = WtaSpacing.ContentGap,
                        bottom = WtaSpacing.ContentGap
                    )
                )

                val deviceTypes = listOf(
                    DeviceType.PHONE to Strings.deviceTypePhone,
                    DeviceType.TABLET to Strings.deviceTypeTablet,
                    DeviceType.DESKTOP to Strings.deviceTypeDesktop,
                    DeviceType.LAPTOP to Strings.deviceTypeLaptop,
                    DeviceType.WATCH to Strings.deviceTypeWatch
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = WtaSpacing.RowHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                ) {
                    deviceTypes.forEach { (type, label) ->
                        WtaChip(
                            selected = config.deviceType == type,
                            onClick = {
                                val presets = DevicePresets.getPresetsForType(type)
                                if (presets.isNotEmpty()) {
                                    onConfigChange(presets.first().toConfig())
                                } else {
                                    onConfigChange(config.copy(deviceType = type))
                                }
                            },
                            label = label,
                            showSelectedCheck = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))
                WtaSectionDivider()

                Text(
                    text = Strings.devicePopularPresets,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        start = WtaSpacing.RowHorizontal,
                        top = WtaSpacing.ContentGap,
                        bottom = WtaSpacing.ContentGap
                    )
                )

                val presets = DevicePresets.getPresetsForType(config.deviceType)

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = WtaSpacing.RowHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                ) {
                    presets.forEach { preset ->
                        WtaChip(
                            selected = config.deviceModel == preset.model &&
                                    config.deviceBrand == preset.brand,
                            onClick = {
                                onConfigChange(preset.toConfig().copy(
                                    deviceType = config.deviceType
                                ))
                            },
                            label = preset.name,
                            showSelectedCheck = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))
                WtaSectionDivider()

                AnimatedVisibility(
                    visible = config.deviceType !in listOf(DeviceType.DESKTOP, DeviceType.LAPTOP),
                    enter = CardExpandTransition,
                    exit = CardCollapseTransition
                ) {
                    SettingsSwitch(
                        title = Strings.deviceDesktopViewport,
                        subtitle = Strings.deviceDesktopViewportHint,
                        checked = config.isDesktopViewport,
                        onCheckedChange = {
                            onConfigChange(config.copy(isDesktopViewport = it))
                        }
                    )
                }

                SettingsSwitch(
                    title = Strings.deviceCustomDevice,
                    subtitle = Strings.deviceCustomDeviceHint,
                    checked = showCustomDevice || config.isCustomDevice,
                    onCheckedChange = {
                        showCustomDevice = it
                        if (it && config.deviceModelName.isNotBlank()) {
                            customModelName = config.deviceModelName
                            customModelId = config.deviceModel
                            customScreenW = if (config.screenWidth > 0) config.screenWidth.toString() else ""
                            customScreenH = if (config.screenHeight > 0) config.screenHeight.toString() else ""
                            customDensity = if (config.pixelDensity > 0) config.pixelDensity.toString() else ""
                        }
                    }
                )

                AnimatedVisibility(
                    visible = showCustomDevice || config.isCustomDevice,
                    enter = CardExpandTransition,
                    exit = CardCollapseTransition
                ) {
                    Column(modifier = Modifier.padding(horizontal = WtaSpacing.RowHorizontal)) {
                        Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))

                        PremiumTextField(
                            value = customModelName,
                            onValueChange = { customModelName = it },
                            label = { Text(Strings.deviceCustomName) },
                            placeholder = { Text("Galaxy S26 Ultra") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        PremiumTextField(
                            value = customModelId,
                            onValueChange = { customModelId = it },
                            label = { Text(Strings.deviceCustomModelId) },
                            placeholder = { Text("SM-S938B") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PremiumTextField(
                                value = customScreenW,
                                onValueChange = { customScreenW = it.filter { c -> c.isDigit() } },
                                label = { Text(Strings.deviceCustomWidth) },
                                placeholder = { Text("1920") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            PremiumTextField(
                                value = customScreenH,
                                onValueChange = { customScreenH = it.filter { c -> c.isDigit() } },
                                label = { Text(Strings.deviceCustomHeight) },
                                placeholder = { Text("1080") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        PremiumTextField(
                            value = customDensity,
                            onValueChange = { customDensity = it },
                            label = { Text(Strings.deviceCustomDensity) },
                            placeholder = { Text("2.0") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FilledTonalButton(
                            onClick = {
                                onConfigChange(config.copy(
                                    deviceModelName = customModelName.ifBlank { "Custom Device" },
                                    deviceModel = customModelId.ifBlank { "CUSTOM-${System.currentTimeMillis()}" },
                                    screenWidth = customScreenW.toIntOrNull() ?: 0,
                                    screenHeight = customScreenH.toIntOrNull() ?: 0,
                                    pixelDensity = customDensity.toFloatOrNull() ?: 0f,
                                    isCustomDevice = true
                                ))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = customModelName.isNotBlank()
                        ) {
                            Icon(
                                Icons.Outlined.Save,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Strings.deviceCustomApply)
                        }

                        Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))
                    }
                }

                SettingsSwitch(
                    title = Strings.deviceCustomUA,
                    subtitle = Strings.deviceCustomUAHint,
                    checked = showCustomUA || !config.customUserAgent.isNullOrBlank(),
                    onCheckedChange = {
                        showCustomUA = it
                        if (!it) onConfigChange(config.copy(customUserAgent = null))
                    }
                )

                AnimatedVisibility(
                    visible = showCustomUA || !config.customUserAgent.isNullOrBlank(),
                    enter = CardExpandTransition,
                    exit = CardCollapseTransition
                ) {
                    PremiumTextField(
                        value = config.customUserAgent ?: "",
                        onValueChange = {
                            onConfigChange(config.copy(customUserAgent = it.ifBlank { null }))
                        },
                        label = { Text("User-Agent") },
                        placeholder = { Text("Mozilla/5.0 ...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = WtaSpacing.RowHorizontal)
                            .padding(top = WtaSpacing.ContentGap),
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))
            }
        }
    }
}
