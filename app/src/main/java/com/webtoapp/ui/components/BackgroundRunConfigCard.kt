package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.BackgroundRunExportConfig
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaSwitch
import com.webtoapp.ui.design.WtaTextField

@Composable
fun BackgroundRunConfigCard(
    enabled: Boolean,
    config: BackgroundRunExportConfig,
    onEnabledChange: (Boolean) -> Unit,
    onConfigChange: (BackgroundRunExportConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary

    EnhancedElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── 头部：图标 + 标题 + 状态副标题 + 开关 ──────────────────
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
                            imageVector = Icons.Outlined.Autorenew,
                            contentDescription = null,
                            tint = if (enabled) primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = Strings.backgroundRunTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (enabled) Strings.backgroundRunActiveSummary
                            else Strings.notEnabled,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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

                    // ── 保持唤醒 ─────────────────────────────────────
                    WtaSettingRow(
                        title = Strings.backgroundRunKeepCpuAwake,
                        subtitle = Strings.backgroundRunKeepCpuAwakeDesc,
                        icon = Icons.Outlined.Bolt,
                        active = config.keepCpuAwake,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp),
                        onClick = {
                            onConfigChange(config.copy(keepCpuAwake = !config.keepCpuAwake))
                        }
                    ) {
                        WtaSwitch(
                            checked = config.keepCpuAwake,
                            onCheckedChange = {
                                onConfigChange(config.copy(keepCpuAwake = it))
                            }
                        )
                    }

                    // ── 电池优化豁免说明（生成 APK 首启自动请求一次）───
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Outlined.BatterySaver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 1.dp)
                        )
                        Text(
                            text = Strings.backgroundRunBatteryAutoHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // ── 常驻通知（前台服务必需，可自定义文案）───────────
                    Text(
                        text = Strings.backgroundRunNotificationSection,
                        style = MaterialTheme.typography.labelMedium,
                        color = primary
                    )
                    WtaTextField(
                        value = config.notificationTitle,
                        onValueChange = {
                            onConfigChange(config.copy(notificationTitle = it))
                        },
                        label = Strings.backgroundRunNotificationTitle,
                        placeholder = Strings.backgroundRunNotificationTitlePlaceholder,
                        leadingIcon = Icons.Outlined.NotificationsNone,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    WtaTextField(
                        value = config.notificationContent,
                        onValueChange = {
                            onConfigChange(config.copy(notificationContent = it))
                        },
                        label = Strings.backgroundRunNotificationContent,
                        placeholder = Strings.backgroundRunNotificationContentPlaceholder,
                        leadingIcon = Icons.Outlined.Info,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
