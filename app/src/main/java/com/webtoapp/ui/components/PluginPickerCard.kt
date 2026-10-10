package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.plugin.Plugin
import com.webtoapp.core.plugin.PluginStore
import com.webtoapp.core.plugin.localizedDescription
import com.webtoapp.core.plugin.localizedName
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.design.*
import com.webtoapp.ui.plugin.kindLabel
import com.webtoapp.ui.plugin.pluginIcon

/**
 * Per-app plugin attachment card (create/edit app flows). Apps get the global
 * enabled set unless an explicit id list is attached — the card manages that
 * list against [PluginStore].
 */
@Composable
fun PluginPickerCard(
    enabled: Boolean,
    selectedPluginIds: Set<String>,
    onEnabledChange: (Boolean) -> Unit,
    onPluginIdsChange: (Set<String>) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { PluginStore.getInstance(context) }
    val installed by store.plugins.collectAsStateWithLifecycle()
    val builtIns by store.builtInPlugins.collectAsStateWithLifecycle()
    val all = installed + builtIns

    var showSelector by remember { mutableStateOf(false) }

    WtaCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(WtaRadius.Control))
                            .background(
                                if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Extension,
                            contentDescription = null,
                            tint = if (enabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Strings.pluginsTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (enabled) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (selectedPluginIds.isEmpty()) {
                                    Strings.pluginAttachEmpty
                                } else {
                                    Strings.pluginSelectedCount(selectedPluginIds.size)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selectedPluginIds.isEmpty())
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                WtaSwitch(checked = enabled, onCheckedChange = onEnabledChange)
            }

            AnimatedVisibility(
                visible = enabled,
                enter = CardExpandTransition,
                exit = CardCollapseTransition
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Spacer(Modifier.height(14.dp))

                    Text(
                        Strings.pluginAttachHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val selected = selectedPluginIds.mapNotNull { id -> all.firstOrNull { it.id == id } }
                    selected.forEach { plugin ->
                        PluginAttachRow(
                            plugin = plugin,
                            trailing = {
                                IconButton(
                                    onClick = { onPluginIdsChange(selectedPluginIds - plugin.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = Strings.delete,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = { showSelector = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(WtaRadius.Button)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.pluginAdd)
                    }
                }
            }
        }
    }

    if (showSelector) {
        PluginSelectorDialog(
            all = all,
            selectedIds = selectedPluginIds,
            onToggle = { id ->
                onPluginIdsChange(
                    if (id in selectedPluginIds) selectedPluginIds - id else selectedPluginIds + id
                )
            },
            onDismiss = { showSelector = false }
        )
    }
}

@Composable
private fun PluginAttachRow(plugin: Plugin, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WtaRadius.Control))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            pluginIcon(plugin.icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            plugin.localizedName(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                kindLabel(plugin.kind),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        trailing()
    }
}

@Composable
private fun PluginSelectorDialog(
    all: List<Plugin>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    WtaAlertDialog(
        onDismissRequest = onDismiss,
        title = Strings.pluginSelectTitle,
        content = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(all, key = { it.id }) { plugin ->
                    val selected = plugin.id in selectedIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(WtaRadius.Control))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.surfaceContainerLow
                            )
                            .clickable { onToggle(plugin.id) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            pluginIcon(plugin.icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                plugin.localizedName(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (plugin.localizedDescription().isNotBlank()) {
                                Text(
                                    plugin.localizedDescription(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(Strings.confirm) }
        }
    )
}
