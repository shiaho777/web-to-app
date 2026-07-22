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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.extension.ExtensionManager
import com.webtoapp.core.extension.UserScriptParser
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.market.CwsTags
import com.webtoapp.core.market.GfBrowseCategory
import com.webtoapp.core.market.GfFavorite
import com.webtoapp.core.market.GfSearchResult
import com.webtoapp.core.market.GfSort
import com.webtoapp.core.market.GreasyForkFavorites
import com.webtoapp.core.market.GreasyForkSearch
import com.webtoapp.core.market.InstallProgress
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
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
    favorites: List<GfFavorite>,
    installedUserScriptNames: Set<String>,
    onInstall: (GfSearchResult) -> Unit,
    onToggleFavorite: (GfSearchResult) -> Unit,
    onOpenSource: (GfSearchResult) -> Unit,
    listState: LazyListState,
    onImportUserScript: (() -> Unit)? = null
) {
    val favoriteIds = remember(favorites) { favorites.map { it.scriptId }.toSet() }
    val showBrowse = query.isBlank()
    val showFavorites = showBrowse && !isSearching && errorMessage == null && favorites.isNotEmpty()

    if (isSearching && results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
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
                        PremiumFilterChip(
                            selected = browseCategory == category,
                            onClick = { onBrowseCategoryChange(category) },
                            label = { Text(gfBrowseCategoryLabel(category)) }
                        )
                    }
                }
            }
            item(key = "gf-sort") {
                GfSortRow(sortMode = sortMode, onSortModeChange = onSortModeChange)
            }
            if (showFavorites) {
                item(key = "gf-favorites-title") {
                    Text(
                        text = Strings.gfFavoritesSection,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(favorites, key = { "fav-${it.scriptId}" }) { fav ->
                    val favResult = GfSearchResult(
                        id = fav.scriptId,
                        name = fav.name,
                        description = fav.description,
                        version = fav.version,
                        codeUrl = fav.codeUrl,
                        pageUrl = fav.pageUrl,
                        author = fav.author,
                        authorUrl = null,
                        fanScore = fav.fanScore,
                        totalInstalls = fav.totalInstalls,
                        dailyInstalls = 0L,
                        goodRatings = 0L,
                        okRatings = 0L,
                        badRatings = 0L,
                        codeUpdatedAt = "",
                        license = "",
                        locale = "",
                        codeSize = 0L
                    )
                    val id = "gf-${fav.scriptId}"
                    GfResultCard(
                        result = favResult,
                        isFavorite = true,
                        isInstalled = fav.name in installedUserScriptNames,
                        isInstalling = installingId == id,
                        installProgress = if (installingId == id) installProgress else null,
                        onInstall = { onInstall(favResult) },
                        onToggleFavorite = { onToggleFavorite(favResult) },
                        onOpenSource = { onOpenSource(favResult) }
                    )
                }
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
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
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
                isFavorite = result.id in favoriteIds,
                isInstalled = result.name in installedUserScriptNames,
                isInstalling = installingId == id,
                installProgress = if (installingId == id) installProgress else null,
                onInstall = { onInstall(result) },
                onToggleFavorite = { onToggleFavorite(result) },
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
            PremiumFilterChip(
                selected = sortMode == mode,
                onClick = { onSortModeChange(mode) },
                label = { Text(gfSortLabel(mode)) }
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
    isFavorite: Boolean,
    isInstalled: Boolean,
    isInstalling: Boolean,
    installProgress: InstallProgress?,
    onInstall: () -> Unit,
    onToggleFavorite: () -> Unit,
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
                    Text(
                        text = "🐵",
                        style = MaterialTheme.typography.headlineSmall
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
                if (result.author.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        result.author,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (result.totalInstalls > 0L || result.dailyInstalls > 0L || result.fanScore > 0.0 || result.ratingsTotal > 0L) {
                    Spacer(Modifier.height(6.dp))
                    GfMetricsRow(result = result)
                }
                if (result.description.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        result.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val tags = CwsTags.fromName(result.name + " " + result.description).map { it.label }
                if (tags.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tags.take(3).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (isInstalling && installProgress != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
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
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isFavorite) Strings.gfUnfavorite else Strings.gfFavorite,
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

@Composable
private fun GfMetricsRow(result: GfSearchResult) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (result.dailyInstalls > 0L) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    GreasyForkSearch.formatInstallCount(result.dailyInstalls) + "/d",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (result.totalInstalls > 0L) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    GreasyForkSearch.formatInstallCount(result.totalInstalls),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (result.fanScore > 0.0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    GreasyForkSearch.formatScore(result.fanScore),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (result.ratingsTotal > 0L) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.StarHalf,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    result.ratingsTotal.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

        val extensionManager = ExtensionManager.getInstance(appContext)
        val addResult = extensionManager.addModule(parsed.module)
        addResult.onSuccess {
            Toast.makeText(
                appContext,
                Strings.moduleMarketInstalled.replace("%s", result.name),
                Toast.LENGTH_SHORT
            ).show()
        }.onFailure { e ->
            snackbar.showSnackbar(Strings.gfInstallFailed.replace("%s", e.message ?: "unknown"))
        }
    } catch (e: Exception) {
        AppLogger.e("GreasyForkInstall", "install failed for ${result.id}", e)
        snackbar.showSnackbar(Strings.gfInstallFailed.replace("%s", e.message ?: "unknown"))
    }
}

@Composable
fun rememberGreasyForkFavorites(context: android.content.Context): GreasyForkFavorites {
    return remember(context) { GreasyForkFavorites.getInstance(context) }
}
