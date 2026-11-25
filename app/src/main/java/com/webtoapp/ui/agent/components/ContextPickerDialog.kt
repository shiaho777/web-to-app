package com.webtoapp.ui.agent.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.AgentUiState
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaSegmentedRow
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaStatusBanner
import com.webtoapp.ui.design.WtaStatusTone
import com.webtoapp.ui.theme.LocalShowDescriptions
import com.webtoapp.ui.theme.ifDescriptionsShown

@Composable
internal fun AgentContextSheet(
    state: AgentUiState,
    onDismissError: () -> Unit,
    onToggleApp: (Long) -> Unit,
    onToggleModule: (String) -> Unit,
    onToggleSkill: (String) -> Unit,
    onImportSkill: () -> Unit,
    onRemoveSkill: (String) -> Unit,
    onCompact: () -> Unit,
    onDismiss: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<Long?>(null) }
    var removeSkillId by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    LaunchedEffect(tab, category, query.trim()) { listState.scrollToItem(0) }
    val skillToRemove = state.availableSkills.firstOrNull { it.id == removeSkillId }

    AgentPanel(
        title = Strings.agentContextPickerTitle,
        icon = Icons.Outlined.BookmarkBorder,
        onDismiss = onDismiss,
        heightFraction = null
    ) {
        state.error?.let { message ->
            WtaStatusBanner(
                message = message,
                tone = WtaStatusTone.Info,
                actionLabel = Strings.done,
                onAction = onDismissError
            )
        }
        WtaSegmentedRow(
            options = listOf(
                contextTabLabel(Strings.agentContextAppsHeader, state.contextAppIds.size),
                contextTabLabel(Strings.agentContextModulesHeader, state.contextModuleIds.size),
                contextTabLabel(Strings.agentSkillsLabel, state.availableSkills.count { it.id in state.contextSkillIds })
            ),
            selectedIndex = tab,
            onSelect = { tab = it }
        )
        AgentSearchField(query, { query = it })

        AgentReveal(visible = tab == 0 && query.isBlank() && state.availableCategories.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                WtaChip(
                    selected = category == null,
                    onClick = { category = null },
                    label = Strings.agentContextAllCategories
                )
                state.availableCategories.forEach { item ->
                    WtaChip(
                        selected = category == item.id,
                        onClick = { category = item.id },
                        label = item.name
                    )
                }
            }
        }

        AgentReveal(visible = tab == 2 && query.isBlank()) {
            TextButton(onClick = onImportSkill, enabled = !state.importingSkill) {
                Text(if (state.importingSkill) Strings.importing else Strings.agentSkillImport)
            }
        }

        val showDescriptions = LocalShowDescriptions.current
        val term = query.trim()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
        ) {
            if (query.isBlank() && (state.contextCapacity > 0 || showDescriptions)) {
                item(key = "context-usage") {
                    Column(
                        modifier = Modifier.animateItem().padding(bottom = WtaSpacing.Small),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        ContextUsage(state, onCompact)
                        if (showDescriptions) {
                            Text(
                                Strings.agentContextReferencesHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (state.contextLoading && tab != 2) {
                item { ContextEmptyRow(Strings.loading) }
            } else when (tab) {
                0 -> {
                    val apps = state.availableContextApps.filter {
                        (term.isNotEmpty() || category == null || it.categoryId == category) &&
                            it.name.contains(term, ignoreCase = true)
                    }
                    if (apps.isEmpty()) item {
                        ContextEmptyRow(if (query.isBlank()) Strings.agentContextEmpty else Strings.noSearchResults)
                    }
                    items(apps, key = { "app-${it.id}" }) { app ->
                        AgentSelectionRow(
                            title = app.name,
                            subtitle = app.appType.ifDescriptionsShown(),
                            selected = app.id in state.contextAppIds,
                            onClick = { onToggleApp(app.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                1 -> {
                    val modules = state.availableContextModules.filter { it.name.contains(term, ignoreCase = true) }
                    if (modules.isEmpty()) item {
                        ContextEmptyRow(if (query.isBlank()) Strings.agentContextEmpty else Strings.noSearchResults)
                    }
                    items(modules, key = { "module-${it.id}" }) { module ->
                        AgentSelectionRow(
                            title = module.name,
                            subtitle = module.sourceType.ifDescriptionsShown(),
                            selected = module.id in state.contextModuleIds,
                            onClick = { onToggleModule(module.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                else -> {
                    val skills = state.availableSkills.filter {
                        it.name.contains(term, ignoreCase = true) || it.description.contains(term, ignoreCase = true)
                    }
                    if (skills.isEmpty()) item {
                        ContextEmptyRow(
                            if (query.isBlank()) Strings.agentSkillsEmpty else Strings.noSearchResults
                        )
                    }
                    items(skills, key = { "skill-${it.id}" }) { skill ->
                        Row(Modifier.animateItem(), verticalAlignment = Alignment.CenterVertically) {
                            AgentSelectionRow(
                                title = skill.name,
                                subtitle = skill.description.ifDescriptionsShown(),
                                selected = skill.id in state.contextSkillIds,
                                onClick = { onToggleSkill(skill.id) },
                                modifier = Modifier.weight(1f)
                            )
                            WtaIconButton(
                                onClick = { removeSkillId = skill.id },
                                icon = Icons.Outlined.DeleteOutline,
                                contentDescription = Strings.delete
                            )
                        }
                    }
                }
            }
        }
    }

    if (skillToRemove != null) {
        WtaAlertDialog(
            title = Strings.agentSkillRemoveConfirm,
            text = skillToRemove.name,
            onDismissRequest = { removeSkillId = null },
            dismissButton = { TextButton(onClick = { removeSkillId = null }) { Text(Strings.cancel) } },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveSkill(skillToRemove.id)
                    removeSkillId = null
                }) { Text(Strings.delete) }
            }
        )
    }
}

private fun contextTabLabel(label: String, selected: Int): String =
    if (selected == 0) label else "$label · $selected"

@Composable
private fun ContextUsage(state: AgentUiState, onCompact: () -> Unit) {
    if (state.contextCapacity <= 0) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)) {
            Text(
                "${Strings.agentContextCapacity} · ${state.estimatedContextTokens} / ${state.contextCapacity}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { (state.estimatedContextTokens.toFloat() / state.contextCapacity).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                drawStopIndicator = {}
            )
        }
        TextButton(
            onClick = onCompact,
            enabled = state.canSend && state.currentSession?.messages?.isNotEmpty() == true
        ) {
            Text(if (state.compacting) Strings.agentCompacting else Strings.agentCompactNow)
        }
    }
}

@Composable
private fun ContextEmptyRow(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = WtaSpacing.Medium)
    )
}
