package com.webtoapp.ui.agent.components

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.ContextAppItem
import com.webtoapp.ui.agent.ContextCategoryItem
import com.webtoapp.ui.agent.ContextModuleItem
import com.webtoapp.ui.design.WtaSpacing

@Composable
fun ContextPickerDialog(
    apps: List<ContextAppItem>,
    modules: List<ContextModuleItem>,
    categories: List<ContextCategoryItem>,
    selectedAppIds: List<Long>,
    selectedModuleIds: List<String>,
    onToggleApp: (Long) -> Unit,
    onToggleModule: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf<Long?>(null) }

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

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("${Strings.agentContextAppsHeader} (${apps.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("${Strings.agentContextModulesHeader} (${modules.size})") }
                    )
                }

                when (selectedTab) {
                    0 -> AppsPage(
                        apps = apps,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it },
                        selectedAppIds = selectedAppIds,
                        onToggleApp = onToggleApp
                    )
                    1 -> ModulesPage(
                        modules = modules,
                        selectedModuleIds = selectedModuleIds,
                        onToggleModule = onToggleModule
                    )
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
private fun AppsPage(
    apps: List<ContextAppItem>,
    categories: List<ContextCategoryItem>,
    selectedCategory: Long?,
    onSelectCategory: (Long?) -> Unit,
    selectedAppIds: List<Long>,
    onToggleApp: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (categories.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = WtaSpacing.Small),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text(Strings.agentContextAllCategories) }
                )
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category.id,
                        onClick = { onSelectCategory(category.id) },
                        label = { Text("${category.icon} ${category.name}") }
                    )
                }
            }
        }

        val filtered = if (selectedCategory == null) apps
            else apps.filter { it.categoryId == selectedCategory }

        if (filtered.isEmpty()) {
            Text(
                text = Strings.agentContextEmpty,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = WtaSpacing.Medium)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
            ) {
                items(filtered, key = { "app-${it.id}" }) { app ->
                    ContextRow(
                        title = app.name,
                        subtitle = app.appType,
                        checked = app.id in selectedAppIds,
                        onToggle = { onToggleApp(app.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModulesPage(
    modules: List<ContextModuleItem>,
    selectedModuleIds: List<String>,
    onToggleModule: (String) -> Unit
) {
    if (modules.isEmpty()) {
        Text(
            text = Strings.agentContextEmpty,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = WtaSpacing.Medium)
        )
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
            .padding(top = WtaSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
    ) {
        items(modules, key = { "mod-${it.id}" }) { module ->
            ContextRow(
                title = module.name,
                subtitle = module.sourceType,
                checked = module.id in selectedModuleIds,
                onToggle = { onToggleModule(module.id) }
            )
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
