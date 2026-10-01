package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaSwitch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.ApkEncryptionConfig

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EncryptionConfigCard(
    config: ApkEncryptionConfig,
    onConfigChange: (ApkEncryptionConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary

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
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(
                                if (config.enabled) primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (config.enabled) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (config.enabled) primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = Strings.resourceEncryption,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (config.enabled) {
                            Text(
                                text = Strings.encryptionEnabled,
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
                        onConfigChange(config.copy(enabled = enabled))
                    }
                )
            }

            AnimatedVisibility(visible = config.enabled) {
                Column(
                    modifier = Modifier.padding(top = WtaSpacing.RowHorizontal),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                ) {
                    Text(
                        text = Strings.encryptionKeyMode,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val keyMode = config.keyMode ?: ApkEncryptionConfig.KEY_MODE_SIGNATURE
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        WtaChip(
                            selected = keyMode == ApkEncryptionConfig.KEY_MODE_SIGNATURE,
                            onClick = {
                                onConfigChange(config.copy(keyMode = ApkEncryptionConfig.KEY_MODE_SIGNATURE))
                            },
                            label = Strings.encryptionKeyModeSignature
                        )
                        WtaChip(
                            selected = keyMode == ApkEncryptionConfig.KEY_MODE_EMBEDDED,
                            onClick = {
                                onConfigChange(config.copy(keyMode = ApkEncryptionConfig.KEY_MODE_EMBEDDED))
                            },
                            label = Strings.encryptionKeyModeEmbedded
                        )
                    }
                    // Signature-bound keys silently stop decrypting after any re-sign;
                    // surface the warning only while that risky mode is selected.
                    if (keyMode == ApkEncryptionConfig.KEY_MODE_SIGNATURE) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 1.dp)
                            )
                            Text(
                                text = Strings.encryptionKeyModeHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = Strings.threatResponse,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = WtaSpacing.Tiny)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        WtaChip(
                            selected = config.threatResponse == ApkEncryptionConfig.ThreatResponse.LOG_ONLY,
                            onClick = {
                                onConfigChange(config.copy(threatResponse = ApkEncryptionConfig.ThreatResponse.LOG_ONLY))
                            },
                            label = Strings.threatResponseLogOnly
                        )
                        WtaChip(
                            selected = config.threatResponse == ApkEncryptionConfig.ThreatResponse.SILENT_EXIT,
                            onClick = {
                                onConfigChange(config.copy(threatResponse = ApkEncryptionConfig.ThreatResponse.SILENT_EXIT))
                            },
                            label = Strings.threatResponseSilentExit
                        )
                        WtaChip(
                            selected = config.threatResponse == ApkEncryptionConfig.ThreatResponse.CRASH_RANDOM,
                            onClick = {
                                onConfigChange(config.copy(threatResponse = ApkEncryptionConfig.ThreatResponse.CRASH_RANDOM))
                            },
                            label = Strings.threatResponseCrashRandom
                        )
                    }
                }
            }
        }
    }
}
