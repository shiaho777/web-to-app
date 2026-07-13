package com.webtoapp.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.port.PortManager
import com.webtoapp.core.port.ProcessPortScanner
import com.webtoapp.core.port.ProcessPortScanner.RunningService
import com.webtoapp.core.port.ProcessPortScanner.ServiceType
import com.webtoapp.core.port.WtaAppPortDiscovery
import com.webtoapp.core.port.WtaAppPortDiscovery.RemoteAllocation
import com.webtoapp.core.port.WtaAppPortDiscovery.WtaAppPortReport
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaFullEmptyState
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaTab
import com.webtoapp.ui.design.WtaTabRow
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.util.openUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PortManagerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var isScanning by remember { mutableStateOf(false) }
    var services by remember { mutableStateOf<List<RunningService>>(emptyList()) }
    var showKillAllDialog by remember { mutableStateOf(false) }
    var showKillDialog by remember { mutableStateOf<RunningService?>(null) }
    var autoRefresh by rememberSaveable { mutableStateOf(false) }
    var showRangeStats by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableStateOf("ALL") }

    var isScanningWtaApps by remember { mutableStateOf(false) }
    var wtaReports by remember { mutableStateOf<List<WtaAppPortReport>>(emptyList()) }
    var scanErrorThrowable by remember { mutableStateOf<Throwable?>(null) }

    val nowMs by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1000)
        }
    }

    suspend fun doScan() {
        isScanning = true
        services = ProcessPortScanner.scanAllPorts(context)
        isScanning = false
    }

    suspend fun doScanWtaApps() {
        isScanningWtaApps = true
        try {
            wtaReports = WtaAppPortDiscovery.queryAllApps(context)
            scanErrorThrowable = null
        } catch (e: Exception) {
            scanErrorThrowable = e
            snackbarHostState.showSnackbar("${Strings.portManagerScanFailed}: ${e.message ?: ""}")
        } finally {
            isScanningWtaApps = false
        }
    }

    LaunchedEffect(Unit) { doScan() }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && wtaReports.isEmpty() && !isScanningWtaApps) {
            doScanWtaApps()
        }
    }

    LaunchedEffect(autoRefresh, selectedTab) {
        if (autoRefresh) {
            while (true) {
                delay(5000)
                if (selectedTab == 0) doScan() else doScanWtaApps()
            }
        }
    }

    fun refresh() {
        scope.launch {
            if (selectedTab == 0) doScan() else doScanWtaApps()
        }
    }

    fun killService(service: RunningService) {
        scope.launch {
            val success = ProcessPortScanner.killProcess(service.port)
            snackbarHostState.showSnackbar(
                if (success) Strings.portManagerServiceKilled.format(service.port)
                else Strings.portManagerKillFailed
            )
            delay(300)
            doScan()
        }
    }

    fun killAllServices() {
        scope.launch {
            val count = ProcessPortScanner.killAllProcesses(context)
            snackbarHostState.showSnackbar(Strings.portManagerAllKilled.format(count))
            delay(300)
            doScan()
        }
    }

    fun releaseRemote(report: WtaAppPortReport, alloc: RemoteAllocation) {
        scope.launch {
            val ok = WtaAppPortDiscovery.releaseRemotePort(context, report.app.packageName, alloc.port)
            if (ok) {
                snackbarHostState.showSnackbar(
                    Strings.portManagerReleaseRemoteSuccess.format(report.app.displayName, alloc.port)
                )
                delay(300)
                doScanWtaApps()
            } else {
                snackbarHostState.showSnackbar(Strings.portManagerReleaseRemoteFailed)
            }
        }
    }

    fun openInBrowser(url: String) {
        try {
            context.openUrl(url)
        } catch (_: Exception) {
            scope.launch { snackbarHostState.showSnackbar(Strings.portManagerKillFailed) }
        }
    }

    fun copyPort(port: Int) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("port", port.toString()))
        scope.launch { snackbarHostState.showSnackbar(Strings.portManagerPortCopied) }
    }

    val filteredServices = remember(services, query, typeFilter) {
        val q = query.trim()
        services.filter { service ->
            val typeOk = typeFilter == "ALL" || service.type.name == typeFilter
            val queryOk = q.isEmpty() ||
                service.port.toString().contains(q) ||
                service.owner.contains(q, ignoreCase = true) ||
                service.type.label.contains(q, ignoreCase = true) ||
                service.url.contains(q, ignoreCase = true) ||
                service.processName.contains(q, ignoreCase = true)
            typeOk && queryOk
        }
    }

    val filteredReports = remember(wtaReports, query) {
        val q = query.trim()
        if (q.isEmpty()) wtaReports
        else wtaReports.filter { report ->
            report.app.displayName.contains(q, ignoreCase = true) ||
                report.app.packageName.contains(q, ignoreCase = true) ||
                report.allocations.any {
                    it.port.toString().contains(q) ||
                        it.owner.contains(q, ignoreCase = true) ||
                        it.range.contains(q, ignoreCase = true)
                }
        }
    }

    WtaScreen(
        title = Strings.portManagerTitle,
        subtitle = Strings.portManagerSubtitle,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        actions = {
            IconButton(onClick = { autoRefresh = !autoRefresh }) {
                Icon(
                    if (autoRefresh) Icons.Outlined.SyncDisabled else Icons.Outlined.Sync,
                    contentDescription = Strings.portManagerAutoRefresh,
                    tint = if (autoRefresh) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { refresh() },
                enabled = !isScanning && !isScanningWtaApps
            ) {
                if (isScanning || isScanningWtaApps) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.Refresh, contentDescription = Strings.refresh)
                }
            }
            if (selectedTab == 0 && services.isNotEmpty()) {
                IconButton(onClick = { showKillAllDialog = true }) {
                    Icon(
                        Icons.Outlined.DeleteSweep,
                        contentDescription = Strings.portManagerKillAll,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    ) { _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            WtaTabRow(
                tabs = listOf(
                    WtaTab(label = Strings.portManagerTabThisApp, count = services.size),
                    WtaTab(
                        label = Strings.portManagerTabAllApps,
                        count = wtaReports.sumOf { it.allocations.size }.takeIf { it > 0 }
                    )
                ),
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            WtaTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = Strings.portManagerSearchHint,
                leadingIcon = Icons.Outlined.Search,
                singleLine = true,
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Clear, contentDescription = Strings.clear)
                        }
                    }
                } else null
            )

            when (selectedTab) {
                0 -> ThisAppTabContent(
                    services = filteredServices,
                    allServices = services,
                    isScanning = isScanning,
                    showRangeStats = showRangeStats,
                    onToggleRangeStats = { showRangeStats = !showRangeStats },
                    typeFilter = typeFilter,
                    onTypeFilterChange = { typeFilter = it },
                    hasActiveQuery = query.isNotBlank() || typeFilter != "ALL",
                    nowMs = nowMs,
                    onKillRequest = { showKillDialog = it },
                    onOpenInBrowser = ::openInBrowser,
                    onCopyPort = ::copyPort
                )
                1 -> AllAppsTabContent(
                    reports = filteredReports,
                    isScanning = isScanningWtaApps,
                    hasActiveQuery = query.isNotBlank(),
                    nowMs = nowMs,
                    onReleaseRequest = ::releaseRemote
                )
            }
        }
    }

    scanErrorThrowable?.let { err ->
        AlertDialog(
            onDismissRequest = { scanErrorThrowable = null },
            title = { Text(Strings.portManagerScanFailed) },
            text = { Text(err.message ?: Strings.portManagerScanFailed) },
            confirmButton = {
                TextButton(onClick = { scanErrorThrowable = null }) {
                    Text(Strings.close)
                }
            }
        )
    }

    if (showKillAllDialog) {
        AlertDialog(
            onDismissRequest = { showKillAllDialog = false },
            icon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(Strings.portManagerKillAll) },
            text = { Text(Strings.portManagerKillAllConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showKillAllDialog = false
                        killAllServices()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.portManagerKillAll)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKillAllDialog = false }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }

    showKillDialog?.let { service ->
        AlertDialog(
            onDismissRequest = { showKillDialog = null },
            icon = { Icon(Icons.Outlined.Stop, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(Strings.portManagerKillService) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(Strings.portManagerKillConfirmSingle)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${Strings.portManagerPort}: ${service.port}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "${Strings.portManagerType}: ${service.type.label}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${Strings.portManagerProject}: ${service.owner}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (service.allocatedAt > 0) {
                        Text(
                            "${Strings.portManagerUptime}: ${PortManager.formatDuration(System.currentTimeMillis() - service.allocatedAt)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val s = service
                        showKillDialog = null
                        killService(s)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.portManagerKill)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKillDialog = null }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }
}

@Composable
private fun ThisAppTabContent(
    services: List<RunningService>,
    allServices: List<RunningService>,
    isScanning: Boolean,
    showRangeStats: Boolean,
    onToggleRangeStats: () -> Unit,
    typeFilter: String,
    onTypeFilterChange: (String) -> Unit,
    hasActiveQuery: Boolean,
    nowMs: Long,
    onKillRequest: (RunningService) -> Unit,
    onOpenInBrowser: (String) -> Unit,
    onCopyPort: (Int) -> Unit
) {
    val typesPresent = remember(allServices) {
        allServices.map { it.type }.distinct().sortedBy { it.name }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PortStatsCard(
                services = allServices,
                showRangeStats = showRangeStats,
                onToggleRangeStats = onToggleRangeStats
            )
        }

        if (typesPresent.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WtaChip(
                        selected = typeFilter == "ALL",
                        onClick = { onTypeFilterChange("ALL") },
                        label = Strings.portManagerFilterAll,
                        showSelectedCheck = false
                    )
                    typesPresent.forEach { type ->
                        val count = allServices.count { it.type == type }
                        WtaChip(
                            selected = typeFilter == type.name,
                            onClick = {
                                onTypeFilterChange(
                                    if (typeFilter == type.name) "ALL" else type.name
                                )
                            },
                            label = "${type.label} ($count)",
                            showSelectedCheck = false
                        )
                    }
                }
            }
        }

        if (services.isEmpty() && !isScanning) {
            item {
                WtaFullEmptyState(
                    title = if (hasActiveQuery) Strings.portManagerNoMatch else Strings.portManagerNoServices,
                    message = if (hasActiveQuery) null else Strings.portManagerAllReleased,
                    icon = if (hasActiveQuery) Icons.Outlined.Search else Icons.Outlined.CheckCircle,
                    fillMaxSize = false
                )
            }
        } else {
            items(services, key = { it.port }) { service ->
                ServiceCard(
                    service = service,
                    nowMs = nowMs,
                    onKill = { onKillRequest(service) },
                    onOpen = { onOpenInBrowser(service.url) },
                    onCopy = { onCopyPort(service.port) }
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun PortStatsCard(
    services: List<RunningService>,
    showRangeStats: Boolean,
    onToggleRangeStats: () -> Unit
) {
    val responding = services.count { it.isResponding }
    WtaCard(tone = WtaCardTone.Highlighted) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        Strings.portManagerRunningServices,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (services.isNotEmpty()) {
                        Text(
                            String.format(Strings.portManagerRespondingCount, responding),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${services.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (services.isNotEmpty()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    IconButton(onClick = onToggleRangeStats, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.BarChart,
                            contentDescription = Strings.portManagerPortRanges,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (services.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    services.groupBy { it.type }.forEach { (type, list) ->
                        ServiceTypeChip(type = type, count = list.size)
                    }
                }
            }

            AnimatedVisibility(
                visible = showRangeStats,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        Strings.portManagerPortRanges,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    val rangeStats = remember(services) { PortManager.getRangeStats() }
                    val semantic = WtaColors.semantic
                    rangeStats.forEach { stat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                stat.range.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(80.dp)
                            )
                            LinearProgressIndicator(
                                progress = { stat.usagePercent },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = when {
                                    stat.usagePercent > 0.8f -> semantic.error
                                    stat.usagePercent > 0.5f -> semantic.warning
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                "${stat.allocated}/${stat.total}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceTypeChip(type: ServiceType, count: Int) {
    val color = Color(type.color)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                "${type.label}: $count",
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}

@Composable
private fun ServiceCard(
    service: RunningService,
    nowMs: Long,
    onKill: () -> Unit,
    onOpen: () -> Unit,
    onCopy: () -> Unit
) {
    var expanded by rememberSaveable(service.port) { mutableStateOf(false) }
    val uptimeText = remember(nowMs, service.allocatedAt) {
        if (service.allocatedAt > 0) PortManager.formatDuration(nowMs - service.allocatedAt) else ""
    }
    val semantic = WtaColors.semantic
    val statusColor = if (service.isResponding) semantic.success else semantic.warning

    WtaCard(tone = WtaCardTone.Surface) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(service.type.color))
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${Strings.portManagerPort} ${service.port}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(service.type.color).copy(alpha = 0.14f)
                        ) {
                            Text(
                                service.type.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(service.type.color)
                            )
                        }
                    }
                    Text(
                        buildString {
                            append(service.owner)
                            if (uptimeText.isNotEmpty()) append(" · $uptimeText")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(10.dp))
                    DetailRow("URL", service.url)
                    DetailRow("PID", if (service.pid > 0) service.pid.toString() else Strings.portManagerUnknown)
                    DetailRow(
                        Strings.portManagerProcess,
                        service.processName.ifEmpty { Strings.portManagerUnknown }
                    )
                    DetailRow(
                        Strings.portManagerStatus,
                        if (service.isResponding) Strings.portManagerResponding else Strings.portManagerNotResponding
                    )
                    if (service.responseTimeMs >= 0) {
                        DetailRow(Strings.portManagerLatency, "${service.responseTimeMs}ms")
                    }
                    if (uptimeText.isNotEmpty()) {
                        DetailRow(Strings.portManagerUptime, uptimeText)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WtaButton(
                            onClick = onOpen,
                            text = Strings.portManagerOpen,
                            variant = WtaButtonVariant.Outlined,
                            size = WtaButtonSize.Small,
                            enabled = service.isResponding,
                            leadingIcon = Icons.Outlined.OpenInBrowser,
                            modifier = Modifier.weight(1f)
                        )
                        WtaButton(
                            onClick = onCopy,
                            text = Strings.portManagerCopyPort,
                            variant = WtaButtonVariant.Tonal,
                            size = WtaButtonSize.Small,
                            leadingIcon = Icons.Outlined.ContentCopy,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    WtaButton(
                        onClick = onKill,
                        text = Strings.portManagerKill,
                        variant = WtaButtonVariant.Primary,
                        size = WtaButtonSize.Small,
                        leadingIcon = Icons.Outlined.Stop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun AllAppsTabContent(
    reports: List<WtaAppPortReport>,
    isScanning: Boolean,
    hasActiveQuery: Boolean,
    nowMs: Long,
    onReleaseRequest: (WtaAppPortReport, RemoteAllocation) -> Unit
) {
    when {
        reports.isEmpty() && isScanning -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        reports.isEmpty() -> {
            WtaFullEmptyState(
                title = if (hasActiveQuery) Strings.portManagerNoMatch else Strings.portManagerNoWtaApps,
                message = if (hasActiveQuery) null else Strings.portManagerNoWtaAppsHint,
                icon = if (hasActiveQuery) Icons.Outlined.Search else Icons.Outlined.Apps
            )
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports, key = { it.app.packageName }) { report ->
                    WtaAppPortReportCard(
                        report = report,
                        nowMs = nowMs,
                        onRelease = { alloc -> onReleaseRequest(report, alloc) }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun WtaAppPortReportCard(
    report: WtaAppPortReport,
    nowMs: Long,
    onRelease: (RemoteAllocation) -> Unit
) {
    var expanded by rememberSaveable(report.app.packageName) {
        mutableStateOf(report.allocations.isNotEmpty())
    }
    val semantic = WtaColors.semantic
    val statusText = when {
        !report.responded -> Strings.portManagerWtaAppOffline
        report.allocations.isEmpty() -> Strings.portManagerWtaAppNoPorts
        else -> Strings.portManagerWtaAppPortsCount.format(report.allocations.size)
    }
    val statusColor = when {
        !report.responded -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        report.allocations.isEmpty() -> semantic.success
        else -> MaterialTheme.colorScheme.primary
    }

    WtaCard(tone = WtaCardTone.Surface) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        report.app.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        report.app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        maxLines = 2
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded && report.allocations.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider()
                    report.allocations.forEach { alloc ->
                        RemoteAllocationRow(
                            allocation = alloc,
                            nowMs = nowMs,
                            onRelease = { onRelease(alloc) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteAllocationRow(
    allocation: RemoteAllocation,
    nowMs: Long,
    onRelease: () -> Unit
) {
    val semantic = WtaColors.semantic
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (allocation.alive) semantic.success else semantic.warning)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    allocation.port.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    allocation.range,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                allocation.owner,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (allocation.allocatedAt > 0) {
                Text(
                    PortManager.formatDuration((nowMs - allocation.allocatedAt).coerceAtLeast(0)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        TextButton(
            onClick = onRelease,
            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Outlined.Stop, null, Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(Strings.portManagerKill, style = MaterialTheme.typography.labelMedium)
        }
    }
}
