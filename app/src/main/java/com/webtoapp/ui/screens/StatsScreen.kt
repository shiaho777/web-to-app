package com.webtoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.webtoapp.R
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.stats.AppHealthRecord
import com.webtoapp.core.stats.AppUsageStats
import com.webtoapp.core.stats.HealthStatus
import com.webtoapp.core.stats.OverallStats
import com.webtoapp.core.stats.StatsFormat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaFullEmptyState
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaTab
import com.webtoapp.ui.design.WtaTabRow
import com.webtoapp.ui.design.WtaTextField
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

            when (selectedTab) {
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

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(Strings.statsClearAll) },
            text = { Text(Strings.statsClearConfirm) },
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
            Text(
                Strings.statsRankings,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (ranked.isEmpty()) {
            item {
                WtaFullEmptyState(
                    title = if (query.isNotBlank()) Strings.statsNoMatch else Strings.statsNoData,
                    message = if (query.isNotBlank()) null else Strings.statsSubtitle,
                    icon = Icons.Outlined.BarChart,
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
                    maxTime = maxTime
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
                    icon = Icons.Outlined.TouchApp,
                    value = stats.totalLaunchCount.toString(),
                    label = Strings.statsTotalLaunches,
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = Icons.Outlined.Timer,
                    value = stats.formattedTotalUsage,
                    label = Strings.statsTotalUsage,
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatItem(
                    icon = Icons.Outlined.Apps,
                    value = stats.activeAppCount.toString(),
                    label = Strings.statsActiveApps,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            HorizontalMetricRow(
                label = Strings.statsAvgSession,
                value = stats.formattedAvgSession
            )
        }
    }
}

@Composable
private fun HorizontalMetricRow(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = color
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
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
private fun UsageRankCard(
    app: WebApp,
    stats: AppUsageStats,
    rank: Int,
    sortMode: StatsSortMode,
    maxLaunches: Int,
    maxTime: Long
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

    WtaCard(tone = WtaCardTone.Surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = when (rank) {
                        1 -> MaterialTheme.colorScheme.primary
                        2 -> MaterialTheme.colorScheme.primaryContainer
                        3 -> MaterialTheme.colorScheme.surfaceContainerHighest
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "#$rank",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = when (rank) {
                                1 -> MaterialTheme.colorScheme.onPrimary
                                2 -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                AppIconSmall(app)
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
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
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetaChip(
                    label = Strings.statsLastSession,
                    value = stats.formattedLastSession
                )
                MetaChip(
                    label = Strings.statsTotalUsage,
                    value = stats.formattedTotalUsage
                )
            }
        }
    }
}

@Composable
private fun MetaChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
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
                    icon = Icons.Outlined.MonitorHeart,
                    fillMaxSize = false
                )
            }
        } else {
            itemsIndexed(webApps, key = { _, app -> app.id }) { _, app ->
                HealthStatusCard(
                    app = app,
                    record = recordMap[app.id],
                    onCheckHealth = onCheckHealth
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
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "$count",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
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
    onCheckHealth: (WebApp) -> Unit
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

    WtaCard(tone = WtaCardTone.Surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckHealth(app) }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(10.dp))
                AppIconSmall(app)
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
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
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
private fun AppIconSmall(app: WebApp) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (app.iconPath != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(app.iconPath)
                    .crossfade(true)
                    .build(),
                contentDescription = app.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            val defaultIconRes = when (app.appType) {
                AppType.WEB -> R.drawable.ic_type_web
                AppType.IMAGE -> R.drawable.ic_type_media
                AppType.VIDEO -> R.drawable.ic_type_media
                AppType.HTML -> R.drawable.ic_type_html
                AppType.GALLERY -> R.drawable.ic_type_gallery
                AppType.FRONTEND -> R.drawable.ic_type_frontend
                AppType.WORDPRESS -> R.drawable.ic_type_wordpress
                AppType.NODEJS_APP -> R.drawable.ic_type_nodejs
                AppType.PHP_APP -> R.drawable.ic_type_php
                AppType.PYTHON_APP -> R.drawable.ic_type_python
                AppType.GO_APP -> R.drawable.ic_type_go
                AppType.MULTI_WEB -> R.drawable.ic_type_multi_web
            }
            Icon(
                painterResource(defaultIconRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
