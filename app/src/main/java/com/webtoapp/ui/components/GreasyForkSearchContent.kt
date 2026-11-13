package com.webtoapp.ui.components

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.extension.UserScriptParser
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.market.GfBrowseCategory
import com.webtoapp.core.market.GfSearchResult
import com.webtoapp.core.market.GfSort
import com.webtoapp.core.market.GreasyForkSearch
import com.webtoapp.core.market.InstallProgress
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSpacing

@Composable
fun GreasyForkSearchContent(
    query: String,
    results: List<GfSearchResult>,
    isSearching: Boolean,
    hasSearched: Boolean,
    errorMessage: String?,
    sortMode: GfSort,
    onSortModeChange: (GfSort) -> Unit,
    browseCategory: GfBrowseCategory = GfBrowseCategory.HOT,
    onBrowseCategoryChange: (GfBrowseCategory) -> Unit = {},
    installingId: String?,
    installProgress: InstallProgress?,
    installedUserScriptNames: Set<String>,
    onInstall: (GfSearchResult) -> Unit,
    onOpenSource: (GfSearchResult) -> Unit,
    listState: LazyListState,
    onImportUserScript: (() -> Unit)? = null
) {
    val showBrowse = query.isBlank()

    if (isSearching && results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WtaLoadingIndicator()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = Strings.gfSearching,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    if (errorMessage != null && results.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = Strings.gfSearchFailed,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showBrowse) {
            item(key = "gf-intro") {
                Text(
                    text = Strings.gfBrowseIntro,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item(key = "gf-categories") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(GfBrowseCategory.browseOrder(), key = { it.name }) { category ->
                        WtaChip(
                            selected = browseCategory == category,
                            onClick = { onBrowseCategoryChange(category) },
                            label = gfBrowseCategoryLabel(category),
                            showSelectedCheck = false
                        )
                    }
                }
            }
            item(key = "gf-sort") {
                GfSortRow(sortMode = sortMode, onSortModeChange = onSortModeChange)
            }
            item(key = "gf-section-title") {
                Text(
                    text = if (browseCategory == GfBrowseCategory.HOT) {
                        Strings.gfHotTitle
                    } else {
                        gfBrowseCategoryLabel(browseCategory)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            item(key = "gf-sort") {
                GfSortRow(sortMode = sortMode, onSortModeChange = onSortModeChange)
            }
        }

        if (isSearching && results.isNotEmpty()) {
            item(key = "gf-refreshing") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WtaLoadingIndicator(size = 16.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        Strings.gfSearching,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (hasSearched && results.isEmpty()) {
            item(key = "gf-no-results") {
                Text(
                    text = Strings.gfNoResults,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(results, key = { "gf-${it.id}" }) { result ->
            val id = "gf-${result.id}"
            GfResultCard(
                result = result,
                isInstalled = result.name in installedUserScriptNames,
                isInstalling = installingId == id,
                installProgress = if (installingId == id) installProgress else null,
                onInstall = { onInstall(result) },
                onOpenSource = { onOpenSource(result) }
            )
        }
    }
}

private fun gfBrowseCategoryLabel(category: GfBrowseCategory): String = when (category) {
    GfBrowseCategory.HOT -> Strings.gfCategoryHot
    GfBrowseCategory.AD_BLOCKING -> Strings.cwsCategoryAdBlocking
    GfBrowseCategory.PRIVACY -> Strings.cwsCategoryPrivacy
    GfBrowseCategory.YOUTUBE -> Strings.cwsCategoryYoutube
    GfBrowseCategory.PRODUCTIVITY -> Strings.cwsCategoryProductivity
    GfBrowseCategory.DEVELOPER -> Strings.cwsCategoryDeveloper
    GfBrowseCategory.STYLING -> Strings.cwsCategoryStyling
}

@Composable
private fun GfSortRow(
    sortMode: GfSort,
    onSortModeChange: (GfSort) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp)
    ) {
        item {
            Text(
                Strings.gfSortLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp, top = 10.dp)
            )
        }
        items(GfSort.values().toList(), key = { it.name }) { mode ->
            WtaChip(
                selected = sortMode == mode,
                onClick = { onSortModeChange(mode) },
                label = gfSortLabel(mode),
                showSelectedCheck = false
            )
        }
    }
}

private fun gfSortLabel(mode: GfSort): String = when (mode) {
    GfSort.DAILY -> Strings.gfSortDaily
    GfSort.TOTAL -> Strings.gfSortTotal
    GfSort.SCORE -> Strings.gfSortScore
    GfSort.RATINGS -> Strings.gfSortRatings
    GfSort.UPDATED -> Strings.gfSortUpdated
}

@Composable
private fun GfResultCard(
    result: GfSearchResult,
    isInstalled: Boolean,
    isInstalling: Boolean,
    installProgress: InstallProgress?,
    onInstall: () -> Unit,
    onOpenSource: () -> Unit
) {
    WtaCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        tone = WtaCardTone.Surface,
        contentPadding = PaddingValues(WtaSpacing.Large)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    result.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (result.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        result.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (isInstalling && installProgress != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WtaLoadingIndicator(size = 16.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    installProgress.label,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenSource, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.OpenInNew, contentDescription = Strings.moduleMarketViewSource)
                }
                Spacer(Modifier.weight(1f))
                if (isInstalled) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        Strings.moduleMarketInstalled.replace("%s", ""),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    WtaButton(
                        onClick = onInstall,
                        text = Strings.moduleMarketInstall,
                        variant = WtaButtonVariant.Primary,
                        size = WtaButtonSize.Small,
                        leadingIcon = Icons.Default.CloudDownload
                    )
                }
            }
        }
    }
}

suspend fun installGreasyForkScript(
    result: GfSearchResult,
    appContext: android.content.Context,
    snackbar: SnackbarHostState,
    onProgress: (InstallProgress) -> Unit
) {
    try {
        onProgress(
            InstallProgress(
                label = Strings.cwsDlModule,
                current = 0,
                total = 1,
                downloadedBytes = 0L,
                totalBytes = -1L,
                speedBytesPerSec = -1L
            )
        )
        val codeResult = GreasyForkSearch.fetchScriptCode(result.codeUrl)
        val code = codeResult.getOrElse { e ->
            snackbar.showSnackbar(Strings.gfInstallFailed.replace("%s", e.message ?: "unknown"))
            return
        }

        val fileName = result.name.takeIf { it.isNotBlank() }?.let { "$it.user.js" } ?: "script.user.js"
        val parsed = UserScriptParser.parse(code, fileName)
        if (!parsed.isValid) {
            AppLogger.w("GreasyForkInstall", "Script parsed with warnings: ${parsed.warnings}")
        }

        val greasyForkModule = parsed.module.copy(sourceType = com.webtoapp.core.extension.ModuleSourceType.GREASYFORK)
        when (val addResult = com.webtoapp.core.plugin.PluginImporter(appContext)
            .installLegacyModule(greasyForkModule)
        ) {
            is com.webtoapp.core.plugin.PluginImporter.ImportResult.Success ->
                Toast.makeText(
                    appContext,
                    Strings.moduleMarketInstalled.replace("%s", result.name),
                    Toast.LENGTH_SHORT
                ).show()
            is com.webtoapp.core.plugin.PluginImporter.ImportResult.Error ->
                snackbar.showSnackbar(Strings.gfInstallFailed.replace("%s", addResult.message))
        }
    } catch (e: Exception) {
        AppLogger.e("GreasyForkInstall", "install failed for ${result.id}", e)
        snackbar.showSnackbar(Strings.gfInstallFailed.replace("%s", e.message ?: "unknown"))
    }
}
