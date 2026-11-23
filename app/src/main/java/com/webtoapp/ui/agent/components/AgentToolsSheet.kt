package com.webtoapp.ui.agent.components

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.os.Build
import android.os.PersistableBundle
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.agent.mcp.HostMcpController
import com.webtoapp.core.host.HostRuntimePrefs
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.AgentToolItem
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaToggleRow
import com.webtoapp.ui.theme.LocalShowDescriptions
import com.webtoapp.ui.theme.ifDescriptionsShown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AgentToolsSheet(
    sessionId: String?,
    imageModelId: String?,
    loadTools: suspend () -> List<AgentToolItem>,
    onMcpEnabledChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val tools by produceState<List<AgentToolItem>?>(null, sessionId, imageModelId) {
        value = loadTools()
    }
    val filtered = tools.orEmpty().filter {
        it.name.contains(query.trim(), ignoreCase = true) ||
            it.description.contains(query.trim(), ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.82f)
                .padding(horizontal = WtaSpacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Strings.agentToolsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onDismiss) { Text(Strings.done) }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(Strings.search) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                if (query.isBlank()) item(key = "mcp") { AgentLocalMcpCard(onMcpEnabledChange) }
                if (filtered.isEmpty()) item(key = "empty") {
                    Text(
                        if (tools == null) Strings.loading else Strings.noSearchResults,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = WtaSpacing.Medium)
                    )
                }
                items(filtered, key = { it.name }) { tool ->
                    AgentToolRow(tool)
                    WtaSectionDivider()
                }
            }
        }
    }
}

@Composable
private fun AgentToolRow(tool: AgentToolItem) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = WtaSpacing.Small,
            vertical = WtaSpacing.Small
        ),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
    ) {
        Text(tool.name, style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Monospace)
        Text(
            if (tool.readOnly) Strings.agentToolReadOnly else Strings.agentToolPermissionChecked,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (tool.description.isNotBlank() && LocalShowDescriptions.current) {
            Text(
                tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AgentLocalMcpCard(onEnabledChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    val prefs = remember(context) { HostRuntimePrefs.getInstance(context) }
    val settings by prefs.mcpFlow.collectAsStateWithLifecycle()
    val status by HostMcpController.status.collectAsStateWithLifecycle()

    WtaSettingCard {
        Column {
            WtaToggleRow(
                title = Strings.localMcp,
                subtitle = Strings.localMcpDesc.ifDescriptionsShown(),
                icon = Icons.Outlined.Hub,
                checked = settings.enabled,
                onCheckedChange = onEnabledChange
            )
            AnimatedVisibility(
                visible = settings.enabled,
                enter = CardExpandTransition,
                exit = CardCollapseTransition
            ) {
                Column {
                    WtaSectionDivider()
                    Column(
                        modifier = Modifier.padding(
                            horizontal = WtaSpacing.RowHorizontal,
                            vertical = WtaSpacing.ContentGap
                        ),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        when (val active = status) {
                            is HostMcpController.Status.Running -> {
                                Text(
                                    "http://127.0.0.1:${active.port}/mcp",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (LocalShowDescriptions.current) {
                                    Text(
                                        Strings.localMcpAdb(active.port),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(
                                    enabled = settings.token.isNotBlank(),
                                    onClick = {
                                        val clip = ClipData.newPlainText(
                                            Strings.localMcp,
                                            HostMcpController.configSnippet(active.port, settings.token)
                                        )
                                        if (Build.VERSION.SDK_INT >= 33) {
                                            clip.description.extras = PersistableBundle().apply {
                                                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                                            }
                                        }
                                        context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(clip)
                                        Toast.makeText(context, Strings.localMcpCopied, Toast.LENGTH_SHORT).show()
                                    }
                                ) { Text(Strings.localMcpCopyConfig) }
                            }
                            is HostMcpController.Status.Failed -> Text(
                                Strings.localMcpBindFailed(active.message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HostMcpController.Status.Stopped -> Text(
                                Strings.loading,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
