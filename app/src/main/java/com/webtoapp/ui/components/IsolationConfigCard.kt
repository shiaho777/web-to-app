package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaDropdownMenu
import com.webtoapp.ui.design.WtaDropdownMenuItem
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaSwitch
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.ui.design.WtaToggleRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.privacy.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IsolationConfigCard(
    config: IsolationConfig,
    onConfigChange: (IsolationConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdvanced by remember { mutableStateOf(false) }

    // 指纹预览：与生成 APK 运行时使用同一种子 → 所见即所得
    val fingerprint = remember(config.fingerprintConfig.fingerprintId) {
        FingerprintGenerator.generateFingerprint(config.fingerprintConfig.fingerprintId)
    }

    EnhancedElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (config.enabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (config.enabled) Icons.Default.Security else Icons.Outlined.Security,
                            contentDescription = null,
                            tint = if (config.enabled)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = Strings.isolatedEnvironment,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (config.enabled) {
                            val levelName = when (config.level()) {
                                IsolationLevel.BASIC -> Strings.basic
                                IsolationLevel.STANDARD -> Strings.standard
                                IsolationLevel.MAXIMUM -> Strings.maximum
                                null -> Strings.customCombination
                            }
                            Text(
                                text = Strings.antiDetectionEnabled + " · " + levelName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                WtaSwitch(
                    checked = config.enabled,
                    onCheckedChange = { enabled ->
                        onConfigChange(
                            if (enabled) {
                                config.copy(enabled = true).withLevel(IsolationLevel.STANDARD)
                            } else {
                                config.copy(enabled = false)
                            }
                        )
                    }
                )
            }

            AnimatedVisibility(
                visible = config.enabled,
                enter = CardExpandTransition,
                exit = CardCollapseTransition
            ) {
                Column(
                    modifier = Modifier.padding(top = WtaSpacing.ContentGap),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                ) {

                    // ── 防护强度：一键预设 ─────────────────────────────
                    Text(
                        text = Strings.isolationLevel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        WtaChip(
                            selected = config.level() == IsolationLevel.BASIC,
                            onClick = { onConfigChange(config.withLevel(IsolationLevel.BASIC)) },
                            label = Strings.basic
                        )
                        WtaChip(
                            selected = config.level() == IsolationLevel.STANDARD,
                            onClick = { onConfigChange(config.withLevel(IsolationLevel.STANDARD)) },
                            label = Strings.standard
                        )
                        WtaChip(
                            selected = config.level() == IsolationLevel.MAXIMUM,
                            onClick = { onConfigChange(config.withLevel(IsolationLevel.MAXIMUM)) },
                            label = Strings.maximum
                        )
                    }

                    Text(
                        text = when (config.level()) {
                            IsolationLevel.BASIC -> Strings.isoLevelBasicSummary
                            IsolationLevel.STANDARD -> Strings.isoLevelStandardSummary
                            IsolationLevel.MAXIMUM -> Strings.isoLevelMaximumSummary
                            null -> Strings.customCombination
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // ── 当前指纹 ─────────────────────────────────────
                    Surface(
                        color = if (com.webtoapp.ui.theme.LocalIsDarkTheme.current)
                            Color.White.copy(alpha = 0.06f)
                        else
                            Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${browserName(fingerprint)} · ${platformName(fingerprint)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${primaryLangOf(fingerprint)} · ${fingerprint.timezone} · ${fingerprint.screenWidth}×${fingerprint.screenHeight}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TextButton(
                                onClick = {
                                    onConfigChange(
                                        config.copy(
                                            fingerprintConfig = config.fingerprintConfig.copy(
                                                fingerprintId = UUID.randomUUID().toString()
                                            )
                                        )
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    Strings.regenerateFingerprint,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // ── 环境伪装：三个维度各一个紧凑选择器 ─────────────
                    Text(
                        text = Strings.environmentSpoofing,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    LanguagePicker(config, onConfigChange, fingerprint)
                    TimezonePicker(config, onConfigChange, fingerprint)
                    ScreenPicker(config, onConfigChange, fingerprint)

                    // ── 高级：逐项防护开关（折叠） ────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.advancedOptions,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        TextButton(onClick = { showAdvanced = !showAdvanced }) {
                            Text(if (showAdvanced) Strings.collapse else Strings.expand)
                            Icon(
                                if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showAdvanced,
                        enter = CardExpandTransition,
                        exit = CardCollapseTransition
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            WtaToggleRow(
                                title = Strings.randomFingerprint,
                                icon = Icons.Outlined.Fingerprint,
                                checked = config.fingerprintConfig.randomize,
                                onCheckedChange = {
                                    onConfigChange(config.copy(
                                        fingerprintConfig = config.fingerprintConfig.copy(randomize = it)
                                    ))
                                }
                            )
                            WtaToggleRow(
                                title = Strings.canvasProtection,
                                icon = Icons.Outlined.Palette,
                                checked = config.protectCanvas,
                                onCheckedChange = { onConfigChange(config.copy(protectCanvas = it)) }
                            )
                            WtaToggleRow(
                                title = Strings.webglProtection,
                                icon = Icons.Outlined.Brush,
                                checked = config.protectWebGL,
                                onCheckedChange = { onConfigChange(config.copy(protectWebGL = it)) }
                            )
                            WtaToggleRow(
                                title = Strings.audioProtection,
                                icon = Icons.Outlined.VolumeUp,
                                checked = config.protectAudio,
                                onCheckedChange = { onConfigChange(config.copy(protectAudio = it)) }
                            )
                            WtaToggleRow(
                                title = Strings.fontProtection,
                                icon = Icons.Outlined.FontDownload,
                                checked = config.protectFonts,
                                onCheckedChange = { onConfigChange(config.copy(protectFonts = it)) }
                            )
                            WtaToggleRow(
                                title = Strings.webrtcProtection,
                                icon = Icons.Outlined.Wifi,
                                checked = config.blockWebRTC,
                                onCheckedChange = { onConfigChange(config.copy(blockWebRTC = it)) }
                            )
                            WtaToggleRow(
                                title = Strings.regenerateOnLaunch,
                                icon = Icons.Outlined.Refresh,
                                checked = config.fingerprintConfig.regenerateOnLaunch,
                                onCheckedChange = {
                                    onConfigChange(config.copy(
                                        fingerprintConfig = config.fingerprintConfig.copy(regenerateOnLaunch = it)
                                    ))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────── 伪装维度选择器 ────────────────────────────

@Composable
private fun SpoofChoiceRow(
    title: String,
    icon: ImageVector,
    value: String,
    active: Boolean,
    menuContent: @Composable (dismiss: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        WtaSettingRow(
            title = title,
            icon = icon,
            active = active,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
            onClick = { expanded = true }
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        WtaDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            menuContent { expanded = false }
        }
    }
}

@Composable
private fun LanguagePicker(
    config: IsolationConfig,
    onConfigChange: (IsolationConfig) -> Unit,
    fingerprint: GeneratedFingerprint
) {
    val presetLabel = IsolationPresets.locales.firstOrNull { it.tag == config.customLanguage }?.label
    val customActive = config.spoofLanguage && config.customLanguage != null && presetLabel == null
    var customMode by remember { mutableStateOf(customActive) }
    // 伪装被外部关闭（等级切换/选“关闭”）时退出自定义输入态
    LaunchedEffect(config.spoofLanguage) {
        if (!config.spoofLanguage) customMode = false
    }
    var input by remember(config.customLanguage) { mutableStateOf(config.customLanguage ?: "") }
    val inputValid = input.isBlank() || IsolationPresets.isValidLanguageTag(input.trim())

    val value = when {
        customMode -> presetLabel ?: config.customLanguage ?: Strings.customOption
        !config.spoofLanguage -> Strings.isolationOff
        config.customLanguage != null -> presetLabel ?: config.customLanguage
        else -> "${Strings.followFingerprint} · ${primaryLangOf(fingerprint)}"
    }

    Column {
        SpoofChoiceRow(
            title = Strings.languageSpoofing,
            icon = Icons.Outlined.Language,
            value = value,
            active = config.spoofLanguage || customMode
        ) { dismiss ->
            WtaDropdownMenuItem(
                text = Strings.isolationOff,
                onClick = {
                    dismiss()
                    customMode = false
                    onConfigChange(config.copy(spoofLanguage = false, customLanguage = null))
                }
            )
            WtaDropdownMenuItem(
                text = Strings.followFingerprint,
                onClick = {
                    dismiss()
                    customMode = false
                    onConfigChange(config.copy(spoofLanguage = true, customLanguage = null))
                }
            )
            IsolationPresets.locales.forEach { opt ->
                WtaDropdownMenuItem(
                    text = opt.label,
                    onClick = {
                        dismiss()
                        customMode = false
                        onConfigChange(config.copy(spoofLanguage = true, customLanguage = opt.tag))
                    }
                )
            }
            WtaDropdownMenuItem(
                text = Strings.customOption,
                onClick = {
                    dismiss()
                    customMode = true
                    if (config.customLanguage == null) input = ""
                    onConfigChange(config.copy(spoofLanguage = true))
                }
            )
        }

        AnimatedVisibility(
            visible = customMode,
            enter = CardExpandTransition,
            exit = CardCollapseTransition
        ) {
            WtaTextField(
                value = input,
                onValueChange = { v ->
                    input = v
                    val tag = v.trim()
                    if (tag.isNotEmpty() && IsolationPresets.isValidLanguageTag(tag)) {
                        onConfigChange(config.copy(spoofLanguage = true, customLanguage = tag))
                    }
                },
                label = Strings.languageTagLabel,
                placeholder = "en-US · ja-JP · fr-FR",
                singleLine = true,
                isError = !inputValid,
                supportingText = if (!inputValid) Strings.invalidValue else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun TimezonePicker(
    config: IsolationConfig,
    onConfigChange: (IsolationConfig) -> Unit,
    fingerprint: GeneratedFingerprint
) {
    val presetLabel = IsolationPresets.timezones.firstOrNull { it.id == config.customTimezone }?.label
    val customActive = config.spoofTimezone && config.customTimezone != null && presetLabel == null
    var customMode by remember { mutableStateOf(customActive) }
    LaunchedEffect(config.spoofTimezone) {
        if (!config.spoofTimezone) customMode = false
    }
    var input by remember(config.customTimezone) { mutableStateOf(config.customTimezone ?: "") }
    val inputValid = input.isBlank() || IsolationPresets.isValidTimezoneId(input.trim())

    val value = when {
        customMode -> presetLabel ?: config.customTimezone ?: Strings.customOption
        !config.spoofTimezone -> Strings.isolationOff
        config.customTimezone != null -> presetLabel ?: config.customTimezone
        else -> "${Strings.followFingerprint} · ${fingerprint.timezone}"
    }

    Column {
        SpoofChoiceRow(
            title = Strings.timezoneSpoofing,
            icon = Icons.Outlined.Schedule,
            value = value,
            active = config.spoofTimezone || customMode
        ) { dismiss ->
            WtaDropdownMenuItem(
                text = Strings.isolationOff,
                onClick = {
                    dismiss()
                    customMode = false
                    onConfigChange(config.copy(spoofTimezone = false, customTimezone = null))
                }
            )
            WtaDropdownMenuItem(
                text = Strings.followFingerprint,
                onClick = {
                    dismiss()
                    customMode = false
                    onConfigChange(config.copy(spoofTimezone = true, customTimezone = null))
                }
            )
            IsolationPresets.timezones.forEach { opt ->
                WtaDropdownMenuItem(
                    text = opt.label,
                    onClick = {
                        dismiss()
                        customMode = false
                        onConfigChange(config.copy(spoofTimezone = true, customTimezone = opt.id))
                    }
                )
            }
            WtaDropdownMenuItem(
                text = Strings.customOption,
                onClick = {
                    dismiss()
                    customMode = true
                    if (config.customTimezone == null) input = ""
                    onConfigChange(config.copy(spoofTimezone = true))
                }
            )
        }

        AnimatedVisibility(
            visible = customMode,
            enter = CardExpandTransition,
            exit = CardCollapseTransition
        ) {
            WtaTextField(
                value = input,
                onValueChange = { v ->
                    input = v
                    val id = v.trim()
                    if (id.isNotEmpty() && IsolationPresets.isValidTimezoneId(id)) {
                        onConfigChange(config.copy(spoofTimezone = true, customTimezone = id))
                    }
                },
                label = Strings.timezoneIdLabel,
                placeholder = "Europe/Paris · Asia/Dubai",
                singleLine = true,
                isError = !inputValid,
                supportingText = if (!inputValid) Strings.invalidValue else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ScreenPicker(
    config: IsolationConfig,
    onConfigChange: (IsolationConfig) -> Unit,
    fingerprint: GeneratedFingerprint
) {
    val matchedPreset = IsolationPresets.screens.firstOrNull {
        it.width == config.customScreenWidth && it.height == config.customScreenHeight &&
            (config.customDevicePixelRatio == null || it.dpr == config.customDevicePixelRatio)
    }
    val customActive = config.spoofScreen && config.customScreenWidth != null && matchedPreset == null
    var customMode by remember { mutableStateOf(customActive) }
    LaunchedEffect(config.spoofScreen) {
        if (!config.spoofScreen) customMode = false
    }
    var widthInput by remember(config.customScreenWidth) {
        mutableStateOf(config.customScreenWidth?.toString() ?: "")
    }
    var heightInput by remember(config.customScreenHeight) {
        mutableStateOf(config.customScreenHeight?.toString() ?: "")
    }
    val w = widthInput.toIntOrNull()
    val h = heightInput.toIntOrNull()
    val dimsValid = (widthInput.isBlank() || w in 240..7680) && (heightInput.isBlank() || h in 240..7680)

    val value = when {
        customMode -> matchedPreset?.label
            ?: if (w != null && h != null) "$w × $h" else Strings.customOption
        !config.spoofScreen -> Strings.isolationOff
        config.customScreenWidth == null -> "${Strings.followFingerprint} · ${fingerprint.screenWidth}×${fingerprint.screenHeight}"
        matchedPreset != null -> matchedPreset.label
        else -> "${config.customScreenWidth} × ${config.customScreenHeight ?: "?"}"
    }

    Column {
        SpoofChoiceRow(
            title = Strings.resolutionSpoofing,
            icon = Icons.Outlined.AspectRatio,
            value = value,
            active = config.spoofScreen || customMode
        ) { dismiss ->
            WtaDropdownMenuItem(
                text = Strings.isolationOff,
                onClick = {
                    dismiss()
                    customMode = false
                    onConfigChange(config.copy(
                        spoofScreen = false,
                        customScreenWidth = null,
                        customScreenHeight = null,
                        customDevicePixelRatio = null
                    ))
                }
            )
            WtaDropdownMenuItem(
                text = Strings.followFingerprint,
                onClick = {
                    dismiss()
                    customMode = false
                    onConfigChange(config.copy(
                        spoofScreen = true,
                        customScreenWidth = null,
                        customScreenHeight = null,
                        customDevicePixelRatio = null
                    ))
                }
            )
            IsolationPresets.screens.forEach { opt ->
                WtaDropdownMenuItem(
                    text = opt.label + if (opt.dpr != 1.0f) " · DPR ${opt.dpr}" else "",
                    onClick = {
                        dismiss()
                        customMode = false
                        onConfigChange(config.copy(
                            spoofScreen = true,
                            customScreenWidth = opt.width,
                            customScreenHeight = opt.height,
                            customDevicePixelRatio = opt.dpr
                        ))
                    }
                )
            }
            WtaDropdownMenuItem(
                text = Strings.customOption,
                onClick = {
                    dismiss()
                    customMode = true
                    if (config.customScreenWidth == null) {
                        widthInput = ""
                        heightInput = ""
                    }
                    onConfigChange(config.copy(spoofScreen = true))
                }
            )
        }

        AnimatedVisibility(
            visible = customMode,
            enter = CardExpandTransition,
            exit = CardCollapseTransition
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                WtaTextField(
                    value = widthInput,
                    onValueChange = { v ->
                        widthInput = v.filter { it.isDigit() }
                        val nw = widthInput.toIntOrNull()
                        if (nw != null && nw in 240..7680) {
                            onConfigChange(config.copy(
                                spoofScreen = true,
                                customScreenWidth = nw,
                                customDevicePixelRatio = null
                            ))
                        }
                    },
                    label = Strings.widthLabel,
                    placeholder = "1920",
                    singleLine = true,
                    isError = !dimsValid,
                    modifier = Modifier.weight(1f)
                )
                WtaTextField(
                    value = heightInput,
                    onValueChange = { v ->
                        heightInput = v.filter { it.isDigit() }
                        val nh = heightInput.toIntOrNull()
                        if (nh != null && nh in 240..7680) {
                            onConfigChange(config.copy(
                                spoofScreen = true,
                                customScreenHeight = nh,
                                customDevicePixelRatio = null
                            ))
                        }
                    },
                    label = Strings.heightLabel,
                    placeholder = "1080",
                    singleLine = true,
                    isError = !dimsValid,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ──────────────────────────── 指纹展示辅助 ────────────────────────────

private fun primaryLangOf(fp: GeneratedFingerprint): String =
    fp.language.split(",").first().split(";").first().trim()

private fun browserName(fp: GeneratedFingerprint): String {
    fun majorOf(token: String): String =
        fp.userAgent.substringAfter(token, "").substringBefore(" ").substringBefore('.')
    return when (fp.browserType) {
        "FIREFOX" -> "Firefox ${majorOf("Firefox/")}"
        "SAFARI" -> "Safari ${majorOf("Version/")}"
        "EDGE" -> "Edge ${majorOf("Edg/")}"
        else -> "Chrome ${majorOf("Chrome/")}"
    }
}

private fun platformName(fp: GeneratedFingerprint): String = when (fp.platform) {
    "Win32", "Win64" -> "Windows"
    "MacIntel" -> "macOS"
    "Linux x86_64", "Linux" -> "Linux"
    else -> fp.platform
}
