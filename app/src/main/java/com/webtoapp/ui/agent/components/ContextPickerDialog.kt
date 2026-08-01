package com.webtoapp.ui.agent.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.ContextAppItem
import com.webtoapp.ui.agent.ContextModuleItem
import com.webtoapp.ui.design.WtaSpacing

@Composable
fun ContextPickerDialog(
    apps: List<ContextAppItem>,
    modules: List<ContextModuleItem>,
    selectedAppIds: List<Long>,
    selectedModuleIds: List<String>,
    onToggleApp: (Long) -> Unit,
    onToggleModule: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WtaSpacing.Medium)
            ) {
                Text(
                    text = Strings.agentContextPickerTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.padding(top = WtaSpacing.Small))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
                ) {
                if (apps.isNotEmpty()) {
                    item("apps-header") {
                        Text(
                            text = Strings.agentContextAppsHeader,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = WtaSpacing.Small)
                        )
                    }
                    items(apps, key = { "app-${it.id}" }) { app ->
                        ContextRow(
                            title = app.name,
                            subtitle = app.appType,
                            checked = app.id in selectedAppIds,
                            onToggle = { onToggleApp(app.id) }
                        )
                    }
                }
                if (modules.isNotEmpty()) {
                    item("modules-header") {
                        Text(
                            text = Strings.agentContextModulesHeader,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = WtaSpacing.Small)
                        )
                    }
                    items(modules, key = { "mod-${it.id}" }) { module ->
                        ContextRow(
                            title = module.name,
                            subtitle = module.sourceType,
                            checked = module.id in selectedModuleIds,
                            onToggle = { onToggleModule(module.id) }
                        )
                    }
                }
                if (apps.isEmpty() && modules.isEmpty()) {
                    item("empty") {
                        Text(
                            text = Strings.agentContextEmpty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = WtaSpacing.Medium)
                        )
                    }
                }
                }
                Spacer(Modifier.padding(top = WtaSpacing.Small))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Strings.agentContextPickerDone)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WtaSpacing.Tiny),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(WtaSpacing.Small))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
