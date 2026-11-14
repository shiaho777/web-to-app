package com.webtoapp.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.extension.UserScriptParser
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.market.ChromeWebStoreSearch
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
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.design.WtaMaterialShape
import com.webtoapp.ui.design.WtaPopIn
import com.webtoapp.ui.design.WtaPopOut
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaShapePlate
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.screens.InstallProgressCard
import com.webtoapp.ui.screens.MarketLoadingHero
import com.webtoapp.ui.screens.MarketShapeIcon

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        MarketLoadingHero(message = Strings.gfSearching)
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
            WtaShapePlate(
                polygon = MaterialShapes.SoftBurst,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Text(
                text = Strings.gfSearchFailed,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WtaMaterialShape(
                        polygon = MaterialShapes.Diamond,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (browseCategory == GfBrowseCategory.HOT) {
                            Strings.gfHotTitle
                        } else {
                            gfBrowseCategoryLabel(browseCategory)
                        },
                        style = MaterialTheme.typography.titleSmallEmphasized
                    )
                }
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WtaShapePlate(
                        polygon = MaterialShapes.Bun,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = Strings.gfNoResults,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        itemsIndexed(results, key = { _, result -> "gf-${result.id}" }) { index, result ->
            val id = "gf-${result.id}"
            GfResultCard(
                result = result,
                isInstalled = result.name in installedUserScriptNames,
                isInstalling = installingId == id,
                installProgress = if (installingId == id) installProgress else null,
                onInstall = { onInstall(result) },
                onOpenSource = { onOpenSource(result) },
                shapeIndex = index,
                modifier = Modifier.animateItem()
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
    onOpenSource: () -> Unit,
    shapeIndex: Int,
    modifier: Modifier = Modifier
) {
    val installsLabel = if (result.totalInstalls > 0L) {
        ChromeWebStoreSearch.formatCompactCount(result.totalInstalls.toInt())
    } else ""
    val scoreLabel = if (result.fanScore > 0.0) {
        String.format(java.util.Locale.US, "%.1f", result.fanScore)
    } else ""
    WtaCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        tone = WtaCardTone.Surface,
        contentPadding = PaddingValues(WtaSpacing.Large)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            MarketShapeIcon(
                icon = Icons.Rounded.Code,
                shapeIndex = shapeIndex,
                plateSize = 48.dp,
                iconSize = 24.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    result.name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
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
        if (installsLabel.isNotBlank() || scoreLabel.isNotBlank() || result.version.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (installsLabel.isNotBlank()) {
                    WtaInfoChip(
                        label = installsLabel,
                        icon = Icons.Rounded.Download,
                        emphasized = true
                    )
                }
                if (scoreLabel.isNotBlank()) {
                    WtaInfoChip(
                        label = scoreLabel,
                        icon = Icons.Rounded.Star,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        emphasized = true
                    )
                }
                if (result.version.isNotBlank()) {
                    WtaInfoChip(
                        label = "v${result.version}",
                        icon = Icons.Rounded.Tag,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        val showProgress = isInstalling && installProgress != null
        var cachedProgress by remember { mutableStateOf<InstallProgress?>(null) }
        LaunchedEffect(installProgress) {
            if (installProgress != null) cachedProgress = installProgress
        }
        AnimatedContent(
            targetState = showProgress,
            transitionSpec = { WtaPopIn togetherWith WtaPopOut },
            label = "gfInstallAction"
        ) { progressVisible ->
            val progress = cachedProgress
            if (progressVisible && progress != null) {
                InstallProgressCard(progress = progress)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WtaIconButton(
                        onClick = onOpenSource,
                        icon = Icons.Rounded.OpenInNew,
                        contentDescription = Strings.moduleMarketViewSource,
                        modifier = Modifier.size(36.dp),
                        tonal = true
                    )
                    Spacer(Modifier.weight(1f))
                    if (isInstalled) {
                        WtaInfoChip(
                            label = Strings.moduleMarketInstalled.replace("%s", "").trim(),
                            icon = Icons.Rounded.CheckCircle,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            emphasized = true
                        )
                    } else {
                        WtaButton(
                            onClick = onInstall,
                            text = Strings.moduleMarketInstall,
                            variant = WtaButtonVariant.Primary,
                            size = WtaButtonSize.Small,
                            leadingIcon = Icons.Rounded.CloudDownload
                        )
                    }
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