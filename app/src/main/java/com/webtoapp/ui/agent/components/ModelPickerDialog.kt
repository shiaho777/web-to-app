package com.webtoapp.ui.agent.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.ProviderGroup
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaFullEmptyState
import com.webtoapp.ui.design.WtaSpacing

@Composable
fun ModelPickerDialog(
    groups: List<ProviderGroup>,
    initialSelectedProviderKeyId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var providerId by rememberSaveable { mutableStateOf(initialSelectedProviderKeyId) }
    var query by rememberSaveable { mutableStateOf("") }
    val selectedProvider = groups.firstOrNull { it.apiKeyId == providerId } ?: groups.firstOrNull()
    val term = query.trim()
    val filteredGroups = remember(groups, selectedProvider, term) {
        if (term.isEmpty()) listOfNotNull(selectedProvider)
        else groups.mapNotNull { group ->
            val models = group.models.filter {
                it.label.contains(term, ignoreCase = true) ||
                    it.subtitle.contains(term, ignoreCase = true) ||
                    group.displayName.contains(term, ignoreCase = true)
            }
            group.copy(models = models).takeIf { models.isNotEmpty() }
        }
    }
    val modelList = rememberLazyListState()
    LaunchedEffect(selectedProvider?.apiKeyId, term) { modelList.scrollToItem(0) }

    AgentPanel(
        title = Strings.agentModelPickerTitle,
        icon = Icons.Outlined.SmartToy,
        onDismiss = onDismiss,
        heightFraction = null
    ) {
        AgentSearchField(query, { query = it }, placeholder = Strings.agentModelSearchHint)
        AgentReveal(visible = term.isEmpty() && groups.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                state = rememberLazyListState(
                    initialFirstVisibleItemIndex = groups.indexOf(selectedProvider).coerceAtLeast(0)
                )
            ) {
                items(groups, key = { it.apiKeyId }) { group ->
                    WtaChip(
                        label = group.displayName,
                        selected = group == selectedProvider,
                        onClick = { providerId = group.apiKeyId },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
        if (filteredGroups.none { it.models.isNotEmpty() }) {
            WtaFullEmptyState(
                title = if (term.isEmpty()) Strings.aiCatalogEmpty else Strings.noSearchResults,
                icon = if (term.isEmpty()) Icons.Outlined.SmartToy else Icons.Outlined.Search,
                modifier = Modifier.padding(vertical = WtaSpacing.ExtraLarge),
                fillMaxSize = false
            )
        } else {
            LazyColumn(
                state = modelList,
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                contentPadding = PaddingValues(bottom = WtaSpacing.Medium),
                verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
            ) {
                filteredGroups.forEach { group ->
                    if (term.isNotEmpty()) item(key = "provider:${group.apiKeyId}") {
                        Text(
                            group.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(WtaSpacing.Medium).animateItem()
                        )
                    }
                    items(group.models, key = { it.id }) { model ->
                        AgentSelectionRow(
                            title = model.label,
                            subtitle = model.subtitle.takeUnless { it == model.label },
                            selected = model.selected,
                            onClick = { onSelect(model.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}
