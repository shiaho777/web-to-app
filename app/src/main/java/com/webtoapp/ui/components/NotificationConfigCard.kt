package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.NotificationExportConfig
import com.webtoapp.data.model.NotificationType
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaSwitch
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.ui.theme.LocalShowDescriptions
import com.webtoapp.ui.theme.ifDescriptionsShown

private val POLL_INTERVAL_PRESETS = listOf(5, 15, 30, 60, 120)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationConfigCard(
    enabled: Boolean,
    config: NotificationExportConfig,
    onEnabledChange: (Boolean) -> Unit,
    onConfigChange: (NotificationExportConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdvanced by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary

    val typeLabel = when (config.type) {
        NotificationType.WEB_API -> Strings.notificationTypeWebApi
        NotificationType.POLLING -> Strings.notificationTypePolling
        NotificationType.WEBSOCKET -> Strings.notificationTypeWebsocket
        NotificationType.FCM -> Strings.notificationTypeFcm
        NotificationType.NONE -> Strings.notificationSelectChannel
    }

    EnhancedElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── 头部 ────────────────────────────────────────────────
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
                                if (enabled) primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = if (enabled) primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = Strings.notificationConfigTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (enabled) {
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                WtaSwitch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }

            AnimatedVisibility(
                visible = enabled,
                enter = CardExpandTransition,
                exit = CardCollapseTransition
            ) {
                Column(
                    modifier = Modifier.padding(top = WtaSpacing.ContentGap),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                ) {

                    // ── 推送通道 ─────────────────────────────────────
                    Text(
                        text = Strings.notificationTypeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = primary
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        WtaChip(
                            selected = config.type == NotificationType.WEB_API,
                            onClick = {
                                onConfigChange(config.copy(type = NotificationType.WEB_API))
                            },
                            label = Strings.notificationTypeWebApi
                        )
                        WtaChip(
                            selected = config.type == NotificationType.POLLING,
                            onClick = {
                                onConfigChange(config.copy(type = NotificationType.POLLING))
                            },
                            label = Strings.notificationTypePolling
                        )
                        WtaChip(
                            selected = config.type == NotificationType.WEBSOCKET,
                            onClick = {
                                onConfigChange(config.copy(type = NotificationType.WEBSOCKET))
                            },
                            label = Strings.notificationTypeWebsocket
                        )
                        WtaChip(
                            selected = config.type == NotificationType.FCM,
                            onClick = {
                                onConfigChange(config.copy(type = NotificationType.FCM))
                            },
                            label = Strings.notificationTypeFcm
                        )
                    }

                    when (config.type) {
                        NotificationType.WEB_API ->
                            HintLine(Strings.notificationWebApiDesc)

                        NotificationType.POLLING ->
                            PollingSection(config, onConfigChange, showAdvanced) {
                                showAdvanced = it
                            }

                        NotificationType.WEBSOCKET ->
                            WebSocketSection(config, onConfigChange, showAdvanced) {
                                showAdvanced = it
                            }

                        NotificationType.FCM ->
                            FcmSection(config, onConfigChange, showAdvanced) {
                                showAdvanced = it
                            }

                        NotificationType.NONE ->
                            HintLine(Strings.notificationSelectChannel)
                    }
                }
            }
        }
    }
}

// ──────────────────────────── 轮询 ────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PollingSection(
    config: NotificationExportConfig,
    onConfigChange: (NotificationExportConfig) -> Unit,
    showAdvanced: Boolean,
    onShowAdvancedChange: (Boolean) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    var customInterval by remember(config.pollIntervalMinutes) {
        mutableStateOf(config.pollIntervalMinutes !in POLL_INTERVAL_PRESETS)
    }
    var intervalInput by remember(config.pollIntervalMinutes) {
        mutableStateOf(config.pollIntervalMinutes.toString())
    }
    val parsedInterval = intervalInput.toIntOrNull()
    val intervalValid = parsedInterval == null || parsedInterval >= 5

    Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)) {
        WtaTextField(
            value = config.pollUrl,
            onValueChange = { onConfigChange(config.copy(pollUrl = it)) },
            label = Strings.notificationPollUrl,
            placeholder = Strings.notificationPollUrlPlaceholder,
            leadingIcon = Icons.Outlined.Link,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // 间隔：常用档一键选，其他值走自定义输入
        Text(
            text = Strings.notificationPollInterval,
            style = MaterialTheme.typography.labelMedium,
            color = primary
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
        ) {
            POLL_INTERVAL_PRESETS.forEach { minutes ->
                WtaChip(
                    selected = !customInterval && config.pollIntervalMinutes == minutes,
                    onClick = {
                        customInterval = false
                        onConfigChange(config.copy(pollIntervalMinutes = minutes))
                    },
                    label = Strings.screenAwakeTimeoutValue(minutes),
                    showSelectedCheck = false
                )
            }
            WtaChip(
                selected = customInterval,
                onClick = { customInterval = true },
                label = Strings.customOption,
                showSelectedCheck = false
            )
        }
        AnimatedVisibility(
            visible = customInterval,
            enter = CardExpandTransition,
            exit = CardCollapseTransition
        ) {
            WtaTextField(
                value = intervalInput,
                onValueChange = { v ->
                    intervalInput = v.filter { it.isDigit() }
                    intervalInput.toIntOrNull()?.takeIf { it >= 5 }?.let {
                        onConfigChange(config.copy(pollIntervalMinutes = it))
                    }
                },
                label = Strings.notificationPollInterval,
                supportingText = Strings.notificationPollIntervalHint.ifDescriptionsShown(),
                isError = !intervalValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 请求方式
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
        ) {
            Text(
                text = Strings.notificationPollMethod,
                style = MaterialTheme.typography.bodyMedium
            )
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

        AdvancedFold(
            expanded = showAdvanced,
            onToggle = onShowAdvancedChange
        ) {
            WtaTextField(
                value = config.pollHeaders,
                onValueChange = { onConfigChange(config.copy(pollHeaders = it)) },
                label = Strings.notificationPollHeaders,
                placeholder = Strings.notificationPollHeadersPlaceholder,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.clickUrl,
                onValueChange = { onConfigChange(config.copy(clickUrl = it)) },
                label = Strings.notificationClickUrl,
                placeholder = Strings.notificationClickUrlPlaceholder,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ──────────────────────────── WebSocket ────────────────────────────

@Composable
private fun WebSocketSection(
    config: NotificationExportConfig,
    onConfigChange: (NotificationExportConfig) -> Unit,
    showAdvanced: Boolean,
    onShowAdvancedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)) {
        HintLine(Strings.notificationWebsocketDesc)

        WtaTextField(
            value = config.wsUrl,
            onValueChange = { onConfigChange(config.copy(wsUrl = it)) },
            label = Strings.notificationWsUrl,
            placeholder = Strings.notificationWsUrlPlaceholder,
            leadingIcon = Icons.Outlined.Link,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        WtaTextField(
            value = config.authToken,
            onValueChange = { onConfigChange(config.copy(authToken = it)) },
            label = Strings.notificationAuthToken,
            placeholder = Strings.notificationAuthTokenPlaceholder,
            leadingIcon = Icons.Outlined.Key,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        AdvancedFold(
            expanded = showAdvanced,
            onToggle = onShowAdvancedChange
        ) {
            WtaTextField(
                value = config.registerUrl,
                onValueChange = { onConfigChange(config.copy(registerUrl = it)) },
                label = Strings.notificationRegisterUrl,
                placeholder = Strings.notificationRegisterUrlPlaceholder,
                leadingIcon = Icons.Outlined.Public,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.wsHeaders,
                onValueChange = { onConfigChange(config.copy(wsHeaders = it)) },
                label = Strings.notificationWsHeaders,
                placeholder = Strings.notificationWsHeadersPlaceholder,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.registerHeaders,
                onValueChange = { onConfigChange(config.copy(registerHeaders = it)) },
                label = Strings.notificationRegisterHeaders,
                placeholder = Strings.notificationRegisterHeadersPlaceholder,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.clickUrl,
                onValueChange = { onConfigChange(config.copy(clickUrl = it)) },
                label = Strings.notificationClickUrl,
                placeholder = Strings.notificationClickUrlPlaceholder,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ──────────────────────────── FCM ────────────────────────────

@Composable
private fun FcmSection(
    config: NotificationExportConfig,
    onConfigChange: (NotificationExportConfig) -> Unit,
    showAdvanced: Boolean,
    onShowAdvancedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)) {
        HintLine(Strings.notificationFcmDesc)

        WtaTextField(
            value = config.fcmGoogleServicesJson,
            onValueChange = { raw ->
                val parsed =
                    com.webtoapp.core.notification.NotificationFcmManager.parseGoogleServicesJson(raw)
                if (parsed != null) {
                    onConfigChange(
                        config.copy(
                            fcmGoogleServicesJson = raw,
                            fcmProjectId = parsed.projectId,
                            fcmApplicationId = parsed.applicationId,
                            fcmApiKey = parsed.apiKey,
                            fcmSenderId = parsed.senderId
                        )
                    )
                } else {
                    onConfigChange(config.copy(fcmGoogleServicesJson = raw))
                }
            },
            label = Strings.notificationFcmGoogleServicesJson,
            placeholder = Strings.notificationFcmGoogleServicesJsonPlaceholder,
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth()
        )

        AdvancedFold(
            expanded = showAdvanced,
            onToggle = onShowAdvancedChange
        ) {
            WtaTextField(
                value = config.fcmProjectId,
                onValueChange = { onConfigChange(config.copy(fcmProjectId = it)) },
                label = Strings.notificationFcmProjectId,
                placeholder = Strings.notificationFcmProjectIdPlaceholder,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.fcmApplicationId,
                onValueChange = { onConfigChange(config.copy(fcmApplicationId = it)) },
                label = Strings.notificationFcmApplicationId,
                placeholder = Strings.notificationFcmApplicationIdPlaceholder,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.fcmApiKey,
                onValueChange = { onConfigChange(config.copy(fcmApiKey = it)) },
                label = Strings.notificationFcmApiKey,
                placeholder = Strings.notificationFcmApiKeyPlaceholder,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.fcmSenderId,
                onValueChange = { onConfigChange(config.copy(fcmSenderId = it)) },
                label = Strings.notificationFcmSenderId,
                placeholder = Strings.notificationFcmSenderIdPlaceholder,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.registerUrl,
                onValueChange = { onConfigChange(config.copy(registerUrl = it)) },
                label = Strings.notificationRegisterUrl,
                placeholder = Strings.notificationRegisterUrlPlaceholder,
                leadingIcon = Icons.Outlined.Public,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.authToken,
                onValueChange = { onConfigChange(config.copy(authToken = it)) },
                label = Strings.notificationAuthToken,
                placeholder = Strings.notificationAuthTokenPlaceholder,
                leadingIcon = Icons.Outlined.Key,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.registerHeaders,
                onValueChange = { onConfigChange(config.copy(registerHeaders = it)) },
                label = Strings.notificationRegisterHeaders,
                placeholder = Strings.notificationRegisterHeadersPlaceholder,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = config.clickUrl,
                onValueChange = { onConfigChange(config.copy(clickUrl = it)) },
                label = Strings.notificationClickUrl,
                placeholder = Strings.notificationClickUrlPlaceholder,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ──────────────────────────── 共享小部件 ────────────────────────────

/** 一行小字提示：图标 + 说明，替代过去的整幅 Banner。 */
@Composable
private fun HintLine(text: String) {
    if (!LocalShowDescriptions.current) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 1.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 统一的"高级选项"折叠区。 */
@Composable
private fun AdvancedFold(
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Strings.advancedOptions,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        TextButton(onClick = { onToggle(!expanded) }) {
            Text(if (expanded) Strings.collapse else Strings.expand)
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
    AnimatedVisibility(
        visible = expanded,
        enter = CardExpandTransition,
        exit = CardCollapseTransition
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)) {
            content()
        }
    }
}


