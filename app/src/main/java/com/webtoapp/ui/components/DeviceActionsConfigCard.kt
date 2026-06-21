package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import com.webtoapp.ui.design.WtaSwitch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.webtoapp.R
import com.webtoapp.core.actions.DeviceActionsConfig
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaDivider
import com.webtoapp.ui.design.WtaFeatureCard
import com.webtoapp.ui.design.WtaFeatureCardHeader
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceActionsConfigCard(
    config: DeviceActionsConfig?,
    onConfigChange: (DeviceActionsConfig?) -> Unit
) {
    var expanded by remember { mutableStateOf(config?.enabled == true) }
    var enabled by remember(config) { mutableStateOf(config?.enabled ?: false) }

    var forceMaxVolume by remember(config) { mutableStateOf(config?.forceMaxVolume ?: false) }
    var forceMuteMode by remember(config) { mutableStateOf(config?.forceMuteMode ?: false) }
    var forceBlockVolumeKeys by remember(config) { mutableStateOf(config?.forceBlockVolumeKeys ?: false) }

    var forceMaxVibration by remember(config) { mutableStateOf(config?.forceMaxVibration ?: false) }
    var forceFlashlight by remember(config) { mutableStateOf(config?.forceFlashlight ?: false) }
    var flashlightStrobeMode by remember(config) { mutableStateOf(config?.flashlightStrobeMode ?: false) }

    var flashlightMorseMode by remember(config) { mutableStateOf(config?.flashlightMorseMode ?: false) }
    var flashlightMorseText by remember(config) { mutableStateOf(config?.flashlightMorseText ?: "") }
    var flashlightMorseUnitMs by remember(config) { mutableStateOf(config?.flashlightMorseUnitMs ?: 200) }
    var flashlightSosMode by remember(config) { mutableStateOf(config?.flashlightSosMode ?: false) }
    var flashlightHeartbeatMode by remember(config) { mutableStateOf(config?.flashlightHeartbeatMode ?: false) }
    var flashlightBreathingMode by remember(config) { mutableStateOf(config?.flashlightBreathingMode ?: false) }
    var flashlightEmergencyMode by remember(config) { mutableStateOf(config?.flashlightEmergencyMode ?: false) }

    val flashlightModes = listOf(
        Strings.flashlightModeAlwaysOn to Strings.flashlightModeAlwaysOnDesc,
        Strings.flashlightModeStrobe to Strings.flashlightModeStrobeDesc,
        Strings.flashlightModeMorse to Strings.flashlightModeMorseDesc,
        Strings.flashlightModeSos to Strings.flashlightModeSosDesc,
        Strings.flashlightModeHeartbeat to Strings.flashlightModeHeartbeatDesc,
        Strings.flashlightModeBreathing to Strings.flashlightModeBreathingDesc,
        Strings.flashlightModeTripleFlash to Strings.flashlightModeTripleFlashDesc
    )

    val selectedFlashModeIndex by remember(config) {
        mutableIntStateOf(
            when {
                config?.flashlightMorseMode == true -> 2
                config?.flashlightSosMode == true -> 3
                config?.flashlightHeartbeatMode == true -> 4
                config?.flashlightBreathingMode == true -> 5
                config?.flashlightEmergencyMode == true -> 6
                config?.flashlightStrobeMode == true -> 1
                else -> 0
            }
        )
    }
    var currentFlashModeIndex by remember { mutableIntStateOf(selectedFlashModeIndex) }

    var forceMaxPerformance by remember(config) { mutableStateOf(config?.forceMaxPerformance ?: false) }
    var forceBlockPowerKey by remember(config) { mutableStateOf(config?.forceBlockPowerKey ?: false) }

    var forceBlackScreen by remember(config) { mutableStateOf(config?.forceBlackScreen ?: false) }
    var forceScreenRotation by remember(config) { mutableStateOf(config?.forceScreenRotation ?: false) }
    var forceBlockTouch by remember(config) { mutableStateOf(config?.forceBlockTouch ?: false) }
    var forceScreenAwake by remember(config) { mutableStateOf(config?.forceScreenAwake ?: false) }

    var forceWifiHotspot by remember(config) { mutableStateOf(config?.forceWifiHotspot ?: false) }
    var hotspotSsid by remember(config) { mutableStateOf(config?.hotspotSsid ?: "WebToApp_AP") }
    var hotspotPassword by remember(config) { mutableStateOf(config?.hotspotPassword ?: "12345678") }
    var forceDisableWifi by remember(config) { mutableStateOf(config?.forceDisableWifi ?: false) }
    var forceDisableBluetooth by remember(config) { mutableStateOf(config?.forceDisableBluetooth ?: false) }
    var forceDisableMobileData by remember(config) { mutableStateOf(config?.forceDisableMobileData ?: false) }

    var nuclearMode by remember(config) { mutableStateOf(config?.nuclearMode ?: false) }
    var stealthMode by remember(config) { mutableStateOf(config?.stealthMode ?: false) }
    var customAlarmEnabled by remember(config) { mutableStateOf(config?.customAlarmEnabled ?: false) }
    var customAlarmPattern by remember(config) { mutableStateOf(config?.customAlarmPattern ?: "") }
    var customAlarmVibSync by remember(config) { mutableStateOf(config?.customAlarmVibSync ?: true) }

    fun updateConfig() {
        if (!enabled) {
            onConfigChange(null)
        } else {
            onConfigChange(DeviceActionsConfig(
                enabled = true,
                forceMaxVolume = forceMaxVolume,
                forceMuteMode = forceMuteMode,
                forceBlockVolumeKeys = forceBlockVolumeKeys,
                forceMaxVibration = forceMaxVibration,
                forceFlashlight = forceFlashlight,
                flashlightStrobeMode = flashlightStrobeMode,
                flashlightMorseMode = flashlightMorseMode,
                flashlightMorseText = flashlightMorseText,
                flashlightMorseUnitMs = flashlightMorseUnitMs,
                flashlightSosMode = flashlightSosMode,
                flashlightHeartbeatMode = flashlightHeartbeatMode,
                flashlightBreathingMode = flashlightBreathingMode,
                flashlightEmergencyMode = flashlightEmergencyMode,
                customAlarmEnabled = customAlarmEnabled,
                customAlarmPattern = customAlarmPattern,
                customAlarmVibSync = customAlarmVibSync,
                forceMaxPerformance = forceMaxPerformance,
                forceBlockPowerKey = forceBlockPowerKey,
                forceBlackScreen = forceBlackScreen,
                forceScreenRotation = forceScreenRotation,
                forceBlockTouch = forceBlockTouch,
                forceScreenAwake = forceScreenAwake,
                forceWifiHotspot = forceWifiHotspot,
                hotspotSsid = hotspotSsid,
                hotspotPassword = hotspotPassword,
                forceDisableWifi = forceDisableWifi,
                forceDisableBluetooth = forceDisableBluetooth,
                forceDisableMobileData = forceDisableMobileData,
                nuclearMode = nuclearMode,
                stealthMode = stealthMode
            ))
        }
    }

    WtaFeatureCard(modifier = Modifier) {
        WtaFeatureCardHeader(
            icon = Icons.Outlined.Memory,
            title = Strings.deviceActionsFeatures,
            subtitle = if (!enabled) Strings.notEnabled else null,
            enabled = enabled,
            onClick = { expanded = !expanded },
            trailing = {
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null
                )
            }
        )

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = WtaSpacing.Large)) {
                WtaDivider()
                Spacer(modifier = Modifier.height(WtaSpacing.Large))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
                            Text(
                                Strings.enableDeviceActions,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                Strings.deviceActionsWarning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        WtaSwitch(
                            checked = enabled,
                            onCheckedChange = {
                                enabled = it
                                updateConfig()
                            }
                        )
                    }

                    AnimatedVisibility(visible = enabled) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                Strings.volumeControl,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceMaxVolume,
                                description = Strings.forceMaxVolumeDesc,
                                checked = forceMaxVolume,
                                onCheckedChange = {
                                    forceMaxVolume = it
                                    updateConfig()
                                }
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceMuteMode,
                                description = Strings.forceMuteModeDesc,
                                checked = forceMuteMode,
                                onCheckedChange = {
                                    forceMuteMode = it
                                    updateConfig()
                                }
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceBlockVolumeKeys,
                                description = Strings.forceBlockVolumeKeysDesc,
                                checked = forceBlockVolumeKeys,
                                onCheckedChange = {
                                    forceBlockVolumeKeys = it
                                    updateConfig()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                Strings.vibrationAndFlash,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceMaxVibration,
                                description = Strings.forceMaxVibrationDesc,
                                checked = forceMaxVibration,
                                onCheckedChange = {
                                    forceMaxVibration = it
                                    updateConfig()
                                }
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceFlashlight,
                                description = Strings.forceFlashlightDesc,
                                checked = forceFlashlight,
                                onCheckedChange = {
                                    forceFlashlight = it
                                    updateConfig()
                                }
                            )

                            AnimatedVisibility(visible = forceFlashlight) {
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        Strings.flashlightModeLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    flashlightModes.forEachIndexed { index, (name, desc) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(WtaRadius.Chip))
                                                .clickable {
                                                    currentFlashModeIndex = index

                                                    flashlightStrobeMode = false
                                                    flashlightMorseMode = false
                                                    flashlightSosMode = false
                                                    flashlightHeartbeatMode = false
                                                    flashlightBreathingMode = false
                                                    flashlightEmergencyMode = false

                                                    when (index) {
                                                        1 -> flashlightStrobeMode = true
                                                        2 -> flashlightMorseMode = true
                                                        3 -> flashlightSosMode = true
                                                        4 -> flashlightHeartbeatMode = true
                                                        5 -> flashlightBreathingMode = true
                                                        6 -> flashlightEmergencyMode = true
                                                    }
                                                    updateConfig()
                                                }
                                                .padding(vertical = 6.dp, horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = currentFlashModeIndex == index,
                                                onClick = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    name,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    desc,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    AnimatedVisibility(visible = currentFlashModeIndex == 2) {
                                        Column(modifier = Modifier.padding(top = 8.dp)) {
                                            OutlinedTextField(
                                                value = flashlightMorseText,
                                                onValueChange = {
                                                    flashlightMorseText = it
                                                    updateConfig()
                                                },
                                                label = { Text(Strings.morseCodeText) },
                                                placeholder = { Text(Strings.morseCodeExample) },
                                                supportingText = {
                                                    if (flashlightMorseText.isNotBlank()) {
                                                        Text(
                                                            Strings.morseCodeLabel + textToMorseDisplay(flashlightMorseText),
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    } else {
                                                        Text(
                                                            Strings.morseSupportedChars,
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    }
                                                },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(WtaRadius.Control)
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                Strings.sendSpeedLabel.format(flashlightMorseUnitMs),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Slider(
                                                value = flashlightMorseUnitMs.toFloat(),
                                                onValueChange = {
                                                    flashlightMorseUnitMs = it.toInt()
                                                    updateConfig()
                                                },
                                                valueRange = 50f..500f,
                                                steps = 8
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(Strings.speedFast, style = MaterialTheme.typography.labelSmall,
                                                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(Strings.speedSlow, style = MaterialTheme.typography.labelSmall,
                                                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                Strings.systemControl,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceMaxPerformance,
                                description = Strings.forceMaxPerformanceDesc,
                                checked = forceMaxPerformance,
                                onCheckedChange = {
                                    forceMaxPerformance = it
                                    updateConfig()
                                }
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceBlockPowerKey,
                                description = Strings.forceBlockPowerKeyDesc,
                                checked = forceBlockPowerKey,
                                onCheckedChange = {
                                    forceBlockPowerKey = it
                                    updateConfig()
                                },
                                isDangerous = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                Strings.screenControl,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceBlackScreen,
                                description = Strings.forceBlackScreenDesc,
                                checked = forceBlackScreen,
                                onCheckedChange = {
                                    forceBlackScreen = it
                                    updateConfig()
                                },
                                isDangerous = true
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceScreenRotation,
                                description = Strings.forceScreenRotationDesc,
                                checked = forceScreenRotation,
                                onCheckedChange = {
                                    forceScreenRotation = it
                                    updateConfig()
                                }
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceBlockTouch,
                                description = Strings.forceBlockTouchDesc,
                                checked = forceBlockTouch,
                                onCheckedChange = {
                                    forceBlockTouch = it
                                    updateConfig()
                                },
                                isDangerous = true
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceScreenAwake,
                                description = Strings.forceScreenAwakeDesc,
                                checked = forceScreenAwake,
                                onCheckedChange = {
                                    forceScreenAwake = it
                                    updateConfig()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                Strings.networkControl,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceWifiHotspot,
                                description = Strings.forceWifiHotspotDesc,
                                checked = forceWifiHotspot,
                                onCheckedChange = {
                                    forceWifiHotspot = it
                                    updateConfig()
                                }
                            )

                            AnimatedVisibility(visible = forceWifiHotspot) {
                                Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                                    OutlinedTextField(
                                        value = hotspotSsid,
                                        onValueChange = {
                                            hotspotSsid = it
                                            updateConfig()
                                        },
                                        label = { Text(Strings.hotspotSsid) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(WtaRadius.Control)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = hotspotPassword,
                                        onValueChange = {
                                            hotspotPassword = it
                                            updateConfig()
                                        },
                                        label = { Text(Strings.hotspotPassword) },
                                        supportingText = { Text(Strings.hotspotPasswordHint) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(WtaRadius.Control)
                                    )
                                }
                            }

                            DeviceActionsSwitchRow(
                                title = Strings.forceDisableWifi,
                                description = Strings.forceDisableWifiDesc,
                                checked = forceDisableWifi,
                                onCheckedChange = {
                                    forceDisableWifi = it
                                    updateConfig()
                                }
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceDisableBluetooth,
                                description = Strings.forceDisableBluetoothDesc,
                                checked = forceDisableBluetooth,
                                onCheckedChange = {
                                    forceDisableBluetooth = it
                                    updateConfig()
                                }
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.forceDisableMobileData,
                                description = Strings.forceDisableMobileDataDesc,
                                checked = forceDisableMobileData,
                                onCheckedChange = {
                                    forceDisableMobileData = it
                                    updateConfig()
                                },
                                isDangerous = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                Strings.specialModes,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.nuclearMode,
                                description = Strings.nuclearModeDesc,
                                checked = nuclearMode,
                                onCheckedChange = {
                                    nuclearMode = it
                                    if (it) {

                                        forceMaxVolume = true
                                        forceMaxVibration = true
                                        forceFlashlight = true
                                        flashlightStrobeMode = true
                                        forceMaxPerformance = true
                                        forceBlockVolumeKeys = true
                                        forceBlockPowerKey = true
                                        forceScreenAwake = true
                                        stealthMode = false
                                    }
                                    updateConfig()
                                },
                                isDangerous = true
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.stealthMode,
                                description = Strings.stealthModeDesc,
                                checked = stealthMode,
                                onCheckedChange = {
                                    stealthMode = it
                                    if (it) {

                                        forceMuteMode = true
                                        forceBlockVolumeKeys = true
                                        forceBlockPowerKey = true
                                        forceBlackScreen = true
                                        forceBlockTouch = true
                                        forceDisableWifi = true
                                        forceDisableBluetooth = true
                                        nuclearMode = false
                                    }
                                    updateConfig()
                                },
                                isDangerous = true
                            )

                            DeviceActionsSwitchRow(
                                title = Strings.customAlarm,
                                description = Strings.customAlarmDesc,
                                checked = customAlarmEnabled,
                                onCheckedChange = {
                                    customAlarmEnabled = it
                                    if (it) forceFlashlight = true
                                    updateConfig()
                                }
                            )

                            AnimatedVisibility(visible = customAlarmEnabled) {
                                Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                                    OutlinedTextField(
                                        value = customAlarmPattern,
                                        onValueChange = {
                                            customAlarmPattern = it
                                            updateConfig()
                                        },
                                        label = { Text(Strings.customAlarmPattern) },
                                        supportingText = { Text(Strings.customAlarmPatternHint) },
                                        singleLine = false,
                                        maxLines = 3,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(WtaRadius.Control)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    DeviceActionsSwitchRow(
                                        title = Strings.customAlarmVibSync,
                                        description = Strings.customAlarmVibSyncDesc,
                                        checked = customAlarmVibSync,
                                        onCheckedChange = {
                                            customAlarmVibSync = it
                                            updateConfig()
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(WtaRadius.Chip))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Outlined.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        Strings.deviceActionsFinalWarning,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun DeviceActionsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDangerous: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDangerous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        WtaSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private val MORSE_TABLE: Map<Char, String> = mapOf(
    'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
    'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
    'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
    'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
    'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
    'Z' to "--..",
    '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--",
    '4' to "....-", '5' to ".....", '6' to "-....", '7' to "--...",
    '8' to "---..", '9' to "----.",
    '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '!' to "-.-.--",
    '/' to "-..-.", '(' to "-.--.", ')' to "-.--.-", '&' to ".-...",
    ':' to "---...", ';' to "-.-.-.", '=' to "-...-", '+' to ".-.-.",
    '-' to "-....-", '\"' to ".-..-.", '@' to ".--.-."
)

private fun textToMorseDisplay(text: String): String {
    return text.uppercase().map { ch ->
        if (ch == ' ') "/"
        else MORSE_TABLE[ch] ?: "?"
    }.joinToString(" ")
}
