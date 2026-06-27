package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.DnsConfig
import com.webtoapp.data.model.DnsProvider
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaBadge
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaMotion
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaStatusBanner
import com.webtoapp.ui.design.WtaStatusTone
import com.webtoapp.ui.design.WtaSwitch
import com.webtoapp.ui.design.WtaTextField

@Composable
fun DnsConfigCard(
    dnsMode: String,
    dnsConfig: DnsConfig,
    onDnsModeChange: (String) -> Unit,
    onDnsConfigChange: (DnsConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = dnsMode != "SYSTEM"

    WtaSettingCard(
        modifier = modifier,
        contentPadding = PaddingValues(WtaSpacing.Large)
    ) {
        Column {
            DnsHeader(
                enabled = enabled,
                dnsConfig = dnsConfig,
                onToggle = { onDnsModeChange(if (it) "DOH" else "SYSTEM") }
            )

            AnimatedVisibility(
                visible = !enabled,
                enter = expandVertically(animationSpec = WtaMotion.settleSpring()),
                exit = shrinkVertically(animationSpec = WtaMotion.snapSpring())
            ) {
                WtaStatusBanner(
                    modifier = Modifier.padding(top = WtaSpacing.Medium),
                    message = Strings.dnsModeSystemDesc,
                    tone = WtaStatusTone.Info
                )
            }

            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically(animationSpec = WtaMotion.settleSpring()),
                exit = shrinkVertically(animationSpec = WtaMotion.snapSpring())
            ) {
                Column(
                    modifier = Modifier.padding(top = WtaSpacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.Medium)
                ) {
                    DnsProviderSection(
                        dnsConfig = dnsConfig,
                        onDnsConfigChange = onDnsConfigChange
                    )

                    if (dnsConfig.provider == "custom") {
                        WtaTextField(
                            value = dnsConfig.customDohUrl,
                            onValueChange = { onDnsConfigChange(dnsConfig.copy(customDohUrl = it)) },
                            label = Strings.dnsCustomDohUrl,
                            placeholder = Strings.dnsCustomDohUrlPlaceholder,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    DohModeSection(
                        dnsConfig = dnsConfig,
                        onDnsConfigChange = onDnsConfigChange
                    )

                    WtaSectionDivider()

                    Text(
                        text = Strings.advancedOptions,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = WtaSpacing.Tiny)
                    )

                    WtaSettingRow(
                        title = Strings.dnsBypassSystemDns,
                        subtitle = Strings.dnsBypassSystemDnsDesc,
                        onClick = {
                            onDnsConfigChange(dnsConfig.copy(bypassSystemDns = !dnsConfig.bypassSystemDns))
                        },
                        trailingMaxWidth = 80.dp
                    ) {
                        WtaSwitch(
                            checked = dnsConfig.bypassSystemDns,
                            onCheckedChange = {
                                onDnsConfigChange(dnsConfig.copy(bypassSystemDns = it))
                            }
                        )
                    }

                    WtaSettingRow(
                        title = Strings.dnsEchLabel,
                        subtitle = Strings.dnsEchDesc,
                        icon = Icons.Outlined.Shield,
                        onClick = {
                            onDnsConfigChange(dnsConfig.copy(echEnabled = !dnsConfig.echEnabled))
                        },
                        trailingMaxWidth = 200.dp
                    ) {
                        WtaBadge(
                            text = Strings.dnsEchGeckoBadge,
                            compact = true,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = WtaAlpha.Medium),
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(WtaSpacing.Small))
                        WtaSwitch(
                            checked = dnsConfig.echEnabled,
                            onCheckedChange = {
                                onDnsConfigChange(dnsConfig.copy(echEnabled = it))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DnsHeader(
    enabled: Boolean,
    dnsConfig: DnsConfig,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(WtaSize.IconPlate)
                    .clip(RoundedCornerShape(WtaRadius.IconPlate))
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = WtaAlpha.MutedContainer)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WtaAlpha.Medium)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Dns,
                    contentDescription = null,
                    modifier = Modifier.size(WtaSize.Icon),
                    tint = if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(WtaSpacing.IconTextGap))
            Column {
                Text(
                    text = Strings.dnsConfigTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dnsStatusText(enabled, dnsConfig),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        WtaSwitch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}

@Composable
private fun DnsProviderSection(
    dnsConfig: DnsConfig,
    onDnsConfigChange: (DnsConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
        Text(
            text = Strings.dnsProviderLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DnsProvider.entries.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
            ) {
                row.forEach { provider ->
                    WtaChip(
                        selected = dnsConfig.provider == provider.key,
                        onClick = { onDnsConfigChange(dnsConfig.copy(provider = provider.key)) },
                        label = provider.displayName,
                        modifier = Modifier.weight(1f),
                        showSelectedCheck = false
                    )
                }
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DohModeSection(
    dnsConfig: DnsConfig,
    onDnsConfigChange: (DnsConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
        Text(
            text = Strings.dohModeLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
        ) {
            WtaChip(
                selected = dnsConfig.dohMode == "automatic",
                onClick = { onDnsConfigChange(dnsConfig.copy(dohMode = "automatic")) },
                label = Strings.dohModeAutomatic,
                modifier = Modifier.weight(1f),
                showSelectedCheck = false
            )
            WtaChip(
                selected = dnsConfig.dohMode == "strict",
                onClick = { onDnsConfigChange(dnsConfig.copy(dohMode = "strict")) },
                label = Strings.dohModeStrict,
                modifier = Modifier.weight(1f),
                showSelectedCheck = false
            )
        }
        WtaStatusBanner(
            message = if (dnsConfig.dohMode == "strict") Strings.dohModeStrictDesc
            else Strings.dohModeAutomaticDesc,
            tone = if (dnsConfig.dohMode == "strict") WtaStatusTone.Warning
            else WtaStatusTone.Info
        )
    }
}

private fun dnsStatusText(enabled: Boolean, dnsConfig: DnsConfig): String {
    if (!enabled) return Strings.dnsModeSystemDesc
    val providerName = DnsProvider.fromKey(dnsConfig.provider).displayName
    val modeName = if (dnsConfig.dohMode == "strict") Strings.dohModeStrict else Strings.dohModeAutomatic
    return "$providerName · $modeName"
}
