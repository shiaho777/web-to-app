package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.stats.AppHealthRecord
import com.webtoapp.core.stats.AppUsageStats
import com.webtoapp.core.stats.HealthStatus
import com.webtoapp.core.stats.OverallStats
import com.webtoapp.core.stats.StatsFormat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.ui.components.WtaAppIcon
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaBadge
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaFullEmptyState
import com.webtoapp.ui.design.WtaMaterialShape
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaShapePlate
import com.webtoapp.ui.design.WtaTab
import com.webtoapp.ui.design.WtaTabRow
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.ui.design.wtaTabEnterTransition
import com.webtoapp.ui.design.wtaTabExitTransition
import androidx.graphics.shapes.RoundedPolygon
import kotlinx.coroutines.launch

private enum class StatsSortMode {
    LAUNCHES,
    TIME,
    RECENT
}

@Composable
fun StatsScreen(
    apps: List<WebApp>,
    allStats: List<AppUsageStats>,
    healthRecords: List<AppHealthRecord>,
    overallStats: OverallStats,
    onBack: () -> Unit,
    onCheckHealth: (WebApp) -> Unit = {},
    onCheckAllHealth: () -> Unit = {},
    onClearAllStats: (suspend () -> Unit)? = null,
    onRefreshOverall: (suspend () -> Unit)? = null
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var sortMode by rememberSaveable { mutableStateOf(StatsSortMode.LAUNCHES.name) }
    var showClearConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val activeSort = remember(sortMode) {
        runCatching { StatsSortMode.valueOf(sortMode) }.getOrDefault(StatsSortMode.LAUNCHES)
    }

    LaunchedEffect(allStats) {
        onRefreshOverall?.invoke()
    }

    val tabs = listOf(
        WtaTab(label = Strings.statsTitle),
        WtaTab(
            label = Strings.healthTitle,
            count = apps.count { it.appType == AppType.WEB && it.url.startsWith("http") }
        )
    )

    WtaScreen(
        title = Strings.statsTitle,
        subtitle = Strings.statsSubtitle,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        actions = {
            if (selectedTab == 0 && onClearAllStats != null && allStats.isNotEmpty()) {
                IconButton(onClick = { showClearConfirm = true }) {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = Strings.statsClearAll)
                }
            }
            if (selectedTab == 1) {
                IconButton(onClick = onCheckAllHealth) {
                    Icon(Icons.Outlined.Refresh, contentDescription = Strings.healthCheckNow)
                }
            }
        }
    ) { _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            WtaTabRow(
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    wtaTabEnterTransition(direction) togetherWith wtaTabExitTransition(direction)
                },
                label = "statsTabContent"
            ) { tab ->
                when (tab) {
                    0 -> UsageStatsTab(
                        apps = apps,
                        allStats = allStats,
                        overallStats = overallStats,
                        query = query,
                        onQueryChange = { query = it },
                        sortMode = activeSort,
                        onSortChange = { sortMode = it.name }
                    )
                    1 -> HealthMonitorTab(
                        apps = apps,
                        healthRecords = healthRecords,
                        query = query,
                        onQueryChange = { query = it },
                        onCheckHealth = onCheckHealth
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        WtaAlertDialog(
            onDismissRequest = { showClearConfirm = false },
            icon = Icons.Outlined.DeleteSweep,
            iconTint = MaterialTheme.colorScheme.error,
            title = Strings.statsClearAll,
            text = Strings.statsClearConfirm,
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        scope.launch {
                            onClearAllStats?.invoke()
                            onRefreshOverall?.invoke()
                            snackbarHostState.showSnackbar(Strings.statsCleared)
                        }
                    }
                ) {
                    Text(Strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }
}

@Composable
private fun UsageStatsTab(
    apps: List<WebApp>,
    allStats: List<AppUsageStats>,
    overallStats: OverallStats,
    query: String,
    onQueryChange: (String) -> Unit,
    sortMode: StatsSortMode,
    onSortChange: (StatsSortMode) -> Unit
) {
    val appMap = remember(apps) { apps.associateBy { it.id } }
    val ranked = remember(allStats, apps, query, sortMode) {
        val q = query.trim()
        val base = allStats
            .asSequence()
            .filter { it.launchCount > 0 || it.totalUsageMs > 0 }
            .mapNotNull { stats ->
                val app = appMap[stats.appId] ?: return@mapNotNull null
                if (q.isNotEmpty() &&
                    !app.name.contains(q, ignoreCase = true) &&
                    !app.url.contains(q, ignoreCase = true)
                ) {
                    return@mapNotNull null
                }
                app to stats
            }
            .toList()
        when (sortMode) {
            StatsSortMode.LAUNCHES -> base.sortedByDescending { it.second.launchCount }
            StatsSortMode.TIME -> base.sortedByDescending { it.second.totalUsageMs }
            StatsSortMode.RECENT -> base.sortedByDescending { it.second.lastUsedAt }
        }
    }
    val maxTime = ranked.maxOfOrNull { it.second.totalUsageMs } ?: 0L
    val maxLaunches = ranked.maxOfOrNull { it.second.launchCount } ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { OverallStatsCard(overallStats) }

        item {
            WtaTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = Strings.statsSearchHint,
                leadingIcon = Icons.Outlined.Search,
                singleLine = true,
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = Strings.clear)
                        }
                    }
                } else null
            )
        }

        if (ranked.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WtaChip(
                        selected = sortMode == StatsSortMode.LAUNCHES,
                        onClick = { onSortChange(StatsSortMode.LAUNCHES) },
                        label = Strings.statsSortLaunches,
                        showSelectedCheck = false
                    )
                    WtaChip(
                        selected = sortMode == StatsSortMode.TIME,
                        onClick = { onSortChange(StatsSortMode.TIME) },
                        label = Strings.statsSortTime,
                        showSelectedCheck = false
                    )
                    WtaChip(
                        selected = sortMode == StatsSortMode.RECENT,
                        onClick = { onSortChange(StatsSortMode.RECENT) },
                        label = Strings.statsSortRecent,
                        showSelectedCheck = false
                    )
                }
            }

            item {
                SectionLabel(Strings.statsRankings)
            }
        }

        if (ranked.isEmpty()) {
            item {
                WtaFullEmptyState(
                    title = if (query.isNotBlank()) Strings.statsNoMatch else Strings.statsNoData,
                    icon = Icons.Rounded.BarChart,
                    fillMaxSize = false
                )
            }
        } else {
            itemsIndexed(ranked, key = { _, pair -> pair.second.appId }) { index, (app, stats) ->
                UsageRankCard(
                    app = app,
                    stats = stats,
                    rank = index + 1,
                    sortMode = sortMode,
                    maxLaunches = maxLaunches,
                    maxTime = maxTime,
                    modifier = Modifier.animateItem()
                )
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun OverallStatsCard(stats: OverallStats) {
    WtaCard(tone = WtaCardTone.Highlighted) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Rounded.TouchApp,
                    shape = MaterialShapes.Sunny,
                    value = stats.totalLaunchCount.toString(),
                    label = Strings.statsTotalLaunches,
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = Icons.Rounded.Timer,
                    shape = MaterialShapes.Gem,
                    value = stats.formattedTotalUsage,
                    label = Strings.statsTotalUsage,
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    icon = Icons.Rounded.Apps,
                    shape = MaterialShapes.Clover4Leaf,
                    value = stats.activeAppCount.toString(),
                    label = Strings.statsActiveApps,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    Strings.statsAvgSession,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stats.formattedAvgSession,
                    style = MaterialTheme.typography.titleSmallEmphasized
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    shape: RoundedPolygon,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        WtaShapePlate(
            polygon = shape,
            containerColor = color.copy(alpha = 0.16f),
            modifier = Modifier.size(52.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMediumEmphasized,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UsageRankCard(
    app: WebApp,
    stats: AppUsageStats,
    rank: Int,
    sortMode: StatsSortMode,
    maxLaunches: Int,
    maxTime: Long,
    modifier: Modifier = Modifier
) {
    val progress = when (sortMode) {
        StatsSortMode.LAUNCHES -> if (maxLaunches > 0) stats.launchCount.toFloat() / maxLaunches else 0f
        StatsSortMode.TIME -> if (maxTime > 0) stats.totalUsageMs.toFloat() / maxTime else 0f
        StatsSortMode.RECENT -> 1f
    }.coerceIn(0f, 1f)
    val accent = when (sortMode) {
        StatsSortMode.LAUNCHES -> MaterialTheme.colorScheme.primary
        StatsSortMode.TIME -> MaterialTheme.colorScheme.tertiary
        StatsSortMode.RECENT -> MaterialTheme.colorScheme.secondary
    }

    WtaCard(tone = WtaCardTone.Surface, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (rank <= 3) {
                    val rankColor = when (rank) {
                        1 -> MaterialTheme.colorScheme.primary
                        2 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                    val rankShape = when (rank) {
                        1 -> MaterialShapes.Cookie9Sided
                        2 -> MaterialShapes.Sunny
                        else -> MaterialShapes.Gem
                    }
                    WtaShapePlate(
                        polygon = rankShape,
                        containerColor = rankColor.copy(alpha = 0.18f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text(
                            "$rank",
                            style = MaterialTheme.typography.labelMediumEmphasized,
                            color = rankColor
                        )
                    }
                } else {
                    Text(
                        "$rank",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(28.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                WtaAppIcon(app)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        app.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stats.formattedLastUsed,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        when (sortMode) {
                            StatsSortMode.LAUNCHES -> stats.launchCount.toString()
                            StatsSortMode.TIME -> stats.formattedTotalUsage
                            StatsSortMode.RECENT -> stats.formattedTotalUsage
                        },
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = accent
                    )
                    Text(
                        when (sortMode) {
                            StatsSortMode.LAUNCHES -> Strings.statsLaunches
                            StatsSortMode.TIME -> Strings.statsTotalUsage
                            StatsSortMode.RECENT -> "${stats.launchCount} ${Strings.statsLaunches}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (sortMode != StatsSortMode.RECENT) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    gapSize = ProgressIndicatorDefaults.LinearIndicatorTrackGapSize
                )
            }
        }
    }
}

@Composable
private fun HealthMonitorTab(
    apps: List<WebApp>,
    healthRecords: List<AppHealthRecord>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCheckHealth: (WebApp) -> Unit
) {
    val recordMap = remember(healthRecords) { healthRecords.associateBy { it.appId } }
    val webApps = remember(apps, query) {
        val q = query.trim()
        apps.filter {
            it.appType == AppType.WEB &&
                it.url.startsWith("http") &&
                (q.isEmpty() ||
                    it.name.contains(q, ignoreCase = true) ||
                    it.url.contains(q, ignoreCase = true))
        }.sortedWith(
            compareBy<WebApp> { app ->
                when (recordMap[app.id]?.status) {
                    HealthStatus.OFFLINE -> 0
                    HealthStatus.SLOW -> 1
                    HealthStatus.UNKNOWN, null -> 2
                    HealthStatus.ONLINE -> 3
                }
            }.thenBy { it.name.lowercase() }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HealthOverviewCard(
                apps = apps.filter { it.appType == AppType.WEB && it.url.startsWith("http") },
                recordMap = recordMap
            )
        }

        item {
            Text(
                Strings.statsHealthCheckHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            WtaTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = Strings.statsSearchHint,
                leadingIcon = Icons.Outlined.Search,
                singleLine = true,
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = Strings.clear)
                        }
                    }
                } else null
            )
        }

        if (webApps.isEmpty()) {
            item {
                WtaFullEmptyState(
                    title = if (query.isNotBlank()) Strings.statsNoMatch else Strings.statsNoData,
                    message = Strings.statsHealthCheckHint,
                    icon = Icons.Rounded.MonitorHeart,
                    fillMaxSize = false
                )
            }
        } else {
            itemsIndexed(webApps, key = { _, app -> app.id }) { _, app ->
                HealthStatusCard(
                    app = app,
                    record = recordMap[app.id],
                    onCheckHealth = onCheckHealth,
                    modifier = Modifier.animateItem()
                )
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun HealthOverviewCard(
    apps: List<WebApp>,
    recordMap: Map<Long, AppHealthRecord>
) {
    val online = apps.count { recordMap[it.id]?.status == HealthStatus.ONLINE }
    val slow = apps.count { recordMap[it.id]?.status == HealthStatus.SLOW }
    val offline = apps.count { recordMap[it.id]?.status == HealthStatus.OFFLINE }
    val unknown = apps.size - online - slow - offline
    val semantic = WtaColors.semantic

    WtaCard(tone = WtaCardTone.Highlighted) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HealthStatItem(online, Strings.healthOnline, semantic.success)
            HealthStatItem(slow, Strings.healthSlow, semantic.warning)
            HealthStatItem(offline, Strings.healthOffline, semantic.error)
            HealthStatItem(unknown, Strings.healthUnknown, semantic.neutral)
        }
    }
}

@Composable
private fun HealthStatItem(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        WtaMaterialShape(
            polygon = MaterialShapes.Cookie6Sided,
            color = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "$count",
            style = MaterialTheme.typography.titleLargeEmphasized,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HealthStatusCard(
    app: WebApp,
    record: AppHealthRecord?,
    onCheckHealth: (WebApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = WtaColors.semantic
    val statusColor = when (record?.status) {
        HealthStatus.ONLINE -> semantic.success
        HealthStatus.SLOW -> semantic.warning
        HealthStatus.OFFLINE -> semantic.error
        else -> semantic.neutral
    }
    val statusText = when (record?.status) {
        HealthStatus.ONLINE -> Strings.healthOnline
        HealthStatus.SLOW -> Strings.healthSlow
        HealthStatus.OFFLINE -> Strings.healthOffline
        else -> Strings.healthUnknown
    }

    WtaCard(tone = WtaCardTone.Surface, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckHealth(app) }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WtaAppIcon(app)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        app.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        app.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                WtaBadge(
                    text = statusText,
                    containerColor = statusColor.copy(alpha = 0.12f),
                    contentColor = statusColor,
                    compact = true
                )
            }

            if (record != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (record.responseTimeMs > 0) {
                        Text(
                            "${Strings.healthResponseTime}: ${record.responseTimeMs}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "${Strings.healthLastChecked}: ${StatsFormat.formatRelative(record.checkedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (record.httpStatusCode > 0) {
                        Text(
                            "HTTP ${record.httpStatusCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (!record.errorMessage.isNullOrBlank()) {
                    Text(
                        record.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (record.responseTimeMs > 0) {
                    Text(
                        "${Strings.healthLastChecked}: ${StatsFormat.formatRelative(record.checkedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmallEmphasized,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}
