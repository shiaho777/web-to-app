package com.webtoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import com.webtoapp.core.ai.CatalogModel
import com.webtoapp.core.ai.ModelCatalogState
import com.webtoapp.core.ai.ModelsDevRepository
import com.webtoapp.core.ai.AiConfigManager
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.AiFeature
import com.webtoapp.data.model.AiModel
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.ModelCapability
import com.webtoapp.data.model.SavedModel
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaSpacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

private enum class CapabilityFilter { ALL, VISION, REASONING, TOOL_CALL, IMAGE_GEN }

@Composable
fun ModelCatalogSection(
    repo: ModelsDevRepository,
    state: ModelCatalogState,
    apiKeys: List<ApiKeyConfig>,
    configManager: AiConfigManager,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<String?>(null) }
    var capabilityFilter by remember { mutableStateOf(CapabilityFilter.ALL) }
    var modelToAdd by remember { mutableStateOf<CatalogModel?>(null) }

    val providers = remember(state) { repo.allProviders() }
    val allModels = remember(state) { repo.allModels() }

    val filtered = remember(allModels, searchQuery, selectedProvider, capabilityFilter) {
        allModels.filter { m ->
            val matchesProvider = selectedProvider == null || m.providerId == selectedProvider
            val matchesQuery = searchQuery.isBlank() ||
                m.name.contains(searchQuery, ignoreCase = true) ||
                m.id.contains(searchQuery, ignoreCase = true)
            val matchesCapability = when (capabilityFilter) {
                CapabilityFilter.ALL -> true
                CapabilityFilter.VISION -> m.supportsVision
                CapabilityFilter.REASONING -> m.reasoning
                CapabilityFilter.TOOL_CALL -> m.toolCall
                CapabilityFilter.IMAGE_GEN -> m.isImageGeneration
            }
            matchesProvider && matchesQuery && matchesCapability
        }.sortedWith(compareBy({ it.providerName }, { it.name }))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header: count + refresh + attribution
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WtaSpacing.ScreenHorizontal, vertical = WtaSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.aiCatalogSource,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (state is ModelCatalogState.Loaded) {
                    Text(
                        text = Strings.aiCatalogCount.format(state.modelCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = { scope.launch { repo.refresh(force = true) } }) {
                Icon(Icons.Outlined.Refresh, contentDescription = Strings.aiCatalogRefresh)
            }
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WtaSpacing.ScreenHorizontal),
            placeholder = { Text(Strings.aiCatalogSearchHint) },
            singleLine = true
        )

        // Provider filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = WtaSpacing.ScreenHorizontal, vertical = WtaSpacing.Small),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
        ) {
            WtaChip(
                selected = selectedProvider == null,
                onClick = { selectedProvider = null },
                label = Strings.aiCatalogAllProviders,
                showSelectedCheck = false
            )
            providers.forEach { p ->
                WtaChip(
                    selected = selectedProvider == p.id,
                    onClick = { selectedProvider = if (selectedProvider == p.id) null else p.id },
                    label = p.name,
                    showSelectedCheck = false
                )
            }
        }

        // Capability filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = WtaSpacing.ScreenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
        ) {
            CapabilityFilter.entries.forEach { f ->
                WtaChip(
                    selected = capabilityFilter == f,
                    onClick = { capabilityFilter = f },
                    label = catalogCapabilityLabel(f),
                    showSelectedCheck = false
                )
            }
        }

        Spacer(Modifier.padding(top = WtaSpacing.Small))

        when {
            state is ModelCatalogState.Loading && allModels.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.padding(top = WtaSpacing.Medium))
                    Text(Strings.aiCatalogLoading, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            state is ModelCatalogState.Error && allModels.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(WtaSpacing.ScreenHorizontal),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.padding(top = WtaSpacing.Small))
                    Button(onClick = { scope.launch { repo.refresh(force = true) } }) {
                        Text(Strings.aiCatalogRefresh)
                    }
                }
            }
            filtered.isEmpty() -> {
                Text(
                    text = Strings.aiCatalogEmpty,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WtaSpacing.ScreenHorizontal),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = WtaSpacing.ScreenHorizontal,
                        vertical = WtaSpacing.Small
                    ),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                ) {
                    items(filtered, key = { "${it.providerId}/${it.id}" }) { model ->
                        CatalogModelRow(
                            model = model,
                            canAdd = apiKeys.isNotEmpty(),
                            onAdd = { modelToAdd = model }
                        )
                    }
                }
            }
        }
    }

    modelToAdd?.let { model ->
        CatalogAddModelDialog(
            model = model,
            apiKeys = apiKeys,
            onDismiss = { modelToAdd = null },
            onConfirm = { key, alias ->
                scope.launch {
                    val saved = buildSavedModel(model, key, alias)
                    if (configManager.saveModel(saved)) {
                        snackbarHostState.showSnackbar(
                            Strings.aiCatalogAdded.format(model.name),
                            duration = SnackbarDuration.Short
                        )
                    } else {
                        snackbarHostState.showSnackbar(
                            Strings.saveFailed,
                            duration = SnackbarDuration.Short
                        )
                    }
                    modelToAdd = null
                }
            }
        )
    }
}

private fun buildSavedModel(model: CatalogModel, key: ApiKeyConfig, alias: String?): SavedModel {
    val aiModel = AiModel(
        id = model.id,
        name = model.name,
        provider = key.provider,
        capabilities = model.capabilities,
        contextLength = model.contextLength.takeIf { it > 0 } ?: 4096,
        inputPrice = model.inputPrice,
        outputPrice = model.outputPrice
    )
    val category = aiModel.capabilities.firstOrNull() ?: ModelCapability.TEXT
    val featureMappings = mapOf(
        category to AiFeature.entries.filter { it.defaultCapabilities.contains(category) }.toSet()
    )
    return SavedModel(
        model = aiModel,
        apiKeyId = key.id,
        alias = alias?.ifBlank { null },
        capabilities = listOf(category),
        featureMappings = featureMappings
    )
}

@Composable
private fun CatalogModelRow(
    model: CatalogModel,
    canAdd: Boolean,
    onAdd: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WtaSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = model.providerName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.padding(top = 2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
                    if (model.supportsVision) CatalogBadge(Strings.aiCatalogBadgeVision)
                    if (model.reasoning) CatalogBadge(Strings.aiCatalogBadgeReasoning)
                    if (model.toolCall) CatalogBadge(Strings.aiCatalogBadgeToolCall)
                    if (model.isImageGeneration) CatalogBadge(Strings.aiCatalogBadgeImageGen)
                }
                val meta = buildList {
                    if (model.contextLength > 0) add(Strings.aiCatalogContext.format(model.contextLength))
                    if (model.inputPrice > 0.0 || model.outputPrice > 0.0)
                        add(Strings.aiCatalogPrice.format(model.inputPrice, model.outputPrice))
                }.joinToString(" · ")
                if (meta.isNotBlank()) {
                    Spacer(Modifier.padding(top = 2.dp))
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(WtaSpacing.Small))
            IconButton(onClick = onAdd, enabled = canAdd) {
                Icon(Icons.Outlined.Add, contentDescription = Strings.aiCatalogAdd)
            }
        }
    }
}

@Composable
private fun CatalogBadge(label: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun CatalogAddModelDialog(
    model: CatalogModel,
    apiKeys: List<ApiKeyConfig>,
    onDismiss: () -> Unit,
    onConfirm: (ApiKeyConfig, String?) -> Unit
) {
    var selectedKey by remember { mutableStateOf(apiKeys.firstOrNull()) }
    var alias by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(WtaSpacing.Medium)) {
                Text(
                    text = Strings.aiCatalogAddTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.padding(top = WtaSpacing.Small))
                Text(
                    text = "${model.name} · ${model.providerName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.padding(top = WtaSpacing.Medium))
                Text(
                    text = Strings.aiCatalogPickKey,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.padding(top = WtaSpacing.Small))
                LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                    items(apiKeys, key = { it.id }) { key ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedKey?.id == key.id,
                                    onClick = { selectedKey = key }
                                )
                                .padding(vertical = WtaSpacing.Tiny),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedKey?.id == key.id,
                                onClick = { selectedKey = key }
                            )
                            Spacer(Modifier.width(WtaSpacing.Small))
                            Text(
                                text = key.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(top = WtaSpacing.Small))
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(Strings.aiCatalogAliasHint) },
                    singleLine = true
                )
                Spacer(Modifier.padding(top = WtaSpacing.Medium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text(Strings.btnCancel) }
                    Spacer(Modifier.width(WtaSpacing.Small))
                    Button(
                        onClick = { selectedKey?.let { onConfirm(it, alias) } },
                        enabled = selectedKey != null
                    ) { Text(Strings.aiCatalogAdd) }
                }
            }
        }
    }
}

private fun catalogCapabilityLabel(filter: CapabilityFilter): String = when (filter) {
    CapabilityFilter.ALL -> Strings.aiCatalogAllProviders
    CapabilityFilter.VISION -> Strings.aiCatalogBadgeVision
    CapabilityFilter.REASONING -> Strings.aiCatalogBadgeReasoning
    CapabilityFilter.TOOL_CALL -> Strings.aiCatalogBadgeToolCall
    CapabilityFilter.IMAGE_GEN -> Strings.aiCatalogBadgeImageGen
}
