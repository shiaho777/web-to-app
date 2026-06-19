package com.webtoapp.ui.components
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webtoapp.core.extension.*
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaBadge
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaCheckbox
import com.webtoapp.ui.design.WtaFeatureCardHeader
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaStatusBanner
import com.webtoapp.ui.design.WtaStatusTone
import com.webtoapp.ui.design.WtaTab
import com.webtoapp.ui.design.WtaTabRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun ExtensionModuleSelectorCard(
    selectedModuleIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extensionManager = remember { ExtensionManager.getInstance(context) }
    val modules by extensionManager.modules.collectAsStateWithLifecycle()
    val builtInModules by extensionManager.builtInModules.collectAsStateWithLifecycle()

    var expanded by remember { mutableStateOf(false) }
    var showModuleDialog by remember { mutableStateOf(false) }

    val allModules = builtInModules + modules
    val enabledModules = allModules.filter { it.id in selectedModuleIds }

    WtaCard(
        modifier = modifier.fillMaxWidth(),
        tone = WtaCardTone.Elevated,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            WtaFeatureCardHeader(
                icon = Icons.Outlined.Extension,
                title = Strings.extensionModuleTitle,
                subtitle = if (enabledModules.isEmpty()) Strings.noModuleSelected
                    else Strings.modulesSelected.format(enabledModules.size),
                trailing = {
                    Row {
                        IconButton(onClick = { showModuleDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = Strings.addModule)
                        }
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) Strings.collapse else Strings.expand
                            )
                        }
                    }
                }
            )

            if (expanded && enabledModules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                WtaSectionDivider()
                Spacer(modifier = Modifier.height(12.dp))

                enabledModules.forEach { module ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(weight = 1f, fill = true)
                        ) {
                            ModuleIcon(iconId = module.icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    module.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    module.category.getDisplayName(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                onSelectionChange(selectedModuleIds - module.id)
                            }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = Strings.cdRemove,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (enabledModules.isEmpty() && expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                WtaStatusBanner(
                    message = Strings.extensionModuleHint,
                    tone = WtaStatusTone.Info
                )
            }
        }
    }

    if (showModuleDialog) {
        ModuleSelectionDialog(
            allModules = allModules,
            selectedIds = selectedModuleIds,
            onSelectionChange = onSelectionChange,
            onDismiss = { showModuleDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleSelectionDialog(
    allModules: List<ExtensionModule>,
    selectedIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ModuleCategory?>(null) }

    val filteredModules = allModules.filter { module ->
        val matchesSearch = searchQuery.isBlank() ||
            module.name.contains(searchQuery, ignoreCase = true) ||
            module.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == null || module.category == selectedCategory
        matchesSearch && matchesCategory
    }

    var selectedTab by remember { mutableStateOf(0) }
    val filteredCustom = filteredModules.filter { it.sourceType != ModuleSourceType.CHROME_EXTENSION }
    val filteredChromeExt = filteredModules.filter { it.sourceType == ModuleSourceType.CHROME_EXTENSION }
    val currentFiltered = if (selectedTab == 0) filteredCustom else filteredChromeExt

    WtaAlertDialog(
        onDismissRequest = onDismiss,
        title = Strings.selectExtensionModules,
        content = {
            PremiumTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(Strings.searchModulesPlaceholder) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PremiumFilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text(Strings.filterAll, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(28.dp)
                )
                PremiumFilterChip(
                    selected = selectedCategory == ModuleCategory.CONTENT_FILTER,
                    onClick = {
                        selectedCategory = if (selectedCategory == ModuleCategory.CONTENT_FILTER) null
                                          else ModuleCategory.CONTENT_FILTER
                    },
                    label = { Text(Strings.filterContent, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(28.dp)
                )
                PremiumFilterChip(
                    selected = selectedCategory == ModuleCategory.STYLE_MODIFIER,
                    onClick = {
                        selectedCategory = if (selectedCategory == ModuleCategory.STYLE_MODIFIER) null
                                          else ModuleCategory.STYLE_MODIFIER
                    },
                    label = { Text(Strings.filterStyle, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(28.dp)
                )
                PremiumFilterChip(
                    selected = selectedCategory == ModuleCategory.FUNCTION_ENHANCE,
                    onClick = {
                        selectedCategory = if (selectedCategory == ModuleCategory.FUNCTION_ENHANCE) null
                                          else ModuleCategory.FUNCTION_ENHANCE
                    },
                    label = { Text(Strings.filterFunction, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            WtaTabRow(
                tabs = listOf(
                    WtaTab(Strings.extensionModulesTab, filteredCustom.size),
                    WtaTab(Strings.browserExtTab, filteredChromeExt.size)
                ),
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(currentFiltered, key = { it.id }) { module ->
                    val isSelected = module.id in selectedIds

                    WtaCard(
                        onClick = {
                            onSelectionChange(
                                if (isSelected) selectedIds - module.id
                                else selectedIds + module.id
                            )
                        },
                        tone = if (isSelected) WtaCardTone.Highlighted else WtaCardTone.Surface,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ModuleIcon(iconId = module.icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        module.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (module.builtIn) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        WtaBadge(
                                            text = Strings.builtIn,
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                                if (module.description.isNotBlank()) {
                                    Text(
                                        module.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            WtaCheckbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    onSelectionChange(
                                        if (it) selectedIds + module.id
                                        else selectedIds - module.id
                                    )
                                }
                            )
                        }
                    }
                }

                if (currentFiltered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                Strings.noMatchingModules,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            PremiumButton(onClick = onDismiss) {
                Text(Strings.done)
            }
        },
        dismissButton = {
            TextButton(onClick = { onSelectionChange(emptySet()) }) {
                Text(Strings.clearSelection)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickModuleSelector(
    selectedModuleIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extensionManager = remember { ExtensionManager.getInstance(context) }
    val builtInModules by extensionManager.builtInModules.collectAsStateWithLifecycle()

    val quickModules = builtInModules.take(5)

    Column(modifier = modifier) {
        Text(
            Strings.quickEnable,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickModules.forEach { module ->
                val isSelected = module.id in selectedModuleIds
                PremiumFilterChip(
                    selected = isSelected,
                    onClick = {
                        onSelectionChange(
                            if (isSelected) selectedModuleIds - module.id
                            else selectedModuleIds + module.id
                        )
                    },
                    label = { Text(module.name.take(4)) },
                    leadingIcon = { ModuleIcon(iconId = module.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface) }
                )
            }
        }
    }
}
