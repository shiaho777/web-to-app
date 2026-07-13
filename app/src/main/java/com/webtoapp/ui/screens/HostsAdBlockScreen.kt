package com.webtoapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.adblock.AdBlocker
import com.webtoapp.core.adblock.DownloadProgress
import com.webtoapp.core.adblock.HostsSource
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaFullEmptyState
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaTextField
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

private enum class HostsFilter {
    ALL,
    DOWNLOADED,
    NOT_DOWNLOADED
}

@Composable
fun HostsAdBlockScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val adBlocker = remember {
        org.koin.java.KoinJavaComponent.get<AdBlocker>(AdBlocker::class.java)
    }

    var hostsRulesCount by remember { mutableIntStateOf(0) }
    var activeHostsCount by remember { mutableIntStateOf(0) }
    var enabledSources by remember { mutableStateOf(emptySet<String>()) }
    var disabledSources by remember { mutableStateOf(emptySet<String>()) }
    var sourceCounts by remember { mutableStateOf(emptyMap<String, Int>()) }
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(HostsFilter.ALL.name) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showDeleteSourceDialog by remember { mutableStateOf<HostsSource?>(null) }
    var importUrl by remember { mutableStateOf("") }
    var urlImporting by remember { mutableStateOf(false) }

    val downloadProgress = remember { mutableStateMapOf<String, DownloadProgress>() }
    val downloadJobs = remember { mutableStateMapOf<String, Job>() }
    val popularSources = remember { AdBlocker.getPopularHostsSources() }

    fun refreshState() {
        hostsRulesCount = adBlocker.getImportedHostsRuleCount()
        activeHostsCount = adBlocker.getHostsFileRuleCount()
        enabledSources = adBlocker.getEnabledHostsSources()
        disabledSources = adBlocker.getDisabledHostsSources()
        val keys = adBlocker.getAllDownloadedSourceKeys()
        sourceCounts = keys.associateWith { adBlocker.getSourceRuleCount(it) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val result = adBlocker.importHostsFromFile(context, it)
                result.fold(
                    onSuccess = { count ->
                        adBlocker.saveHostsRules(context)
                        refreshState()
                        snackbarHostState.showSnackbar(
                            String.format(Locale.getDefault(), Strings.importHostsSuccess, count)
                        )
                    },
                    onFailure = { error ->
                        snackbarHostState.showSnackbar(
                            "${Strings.importHostsFailed}: ${error.message}"
                        )
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        adBlocker.loadHostsRules(context)
        refreshState()
    }

    val activeFilter = remember(filter) {
        runCatching { HostsFilter.valueOf(filter) }.getOrDefault(HostsFilter.ALL)
    }

    val filteredSources = remember(popularSources, query, activeFilter, enabledSources, disabledSources) {
        val q = query.trim()
        popularSources.filter { source ->
            val downloaded = enabledSources.contains(source.url) || disabledSources.contains(source.url)
            val matchesFilter = when (activeFilter) {
                HostsFilter.ALL -> true
                HostsFilter.DOWNLOADED -> downloaded
                HostsFilter.NOT_DOWNLOADED -> !downloaded
            }
            val matchesQuery = q.isEmpty() ||
                source.name.contains(q, ignoreCase = true) ||
                source.description.contains(q, ignoreCase = true) ||
                source.url.contains(q, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    fun importSource(source: HostsSource) {
        if (downloadJobs.containsKey(source.url)) return
        val job = scope.launch {
            downloadProgress[source.url] = DownloadProgress(0, 0, 0)
            val result = adBlocker.importHostsFromUrl(source.url, context) { p ->
                downloadProgress[source.url] = p
            }
            downloadProgress.remove(source.url)
            downloadJobs.remove(source.url)
            result.fold(
                onSuccess = { count ->
                    adBlocker.saveHostsRules(context)
                    refreshState()
                    snackbarHostState.showSnackbar(
                        String.format(Locale.getDefault(), Strings.importHostsSuccess, count)
                    )
                },
                onFailure = { error ->
                    val isCancel = error is kotlinx.coroutines.CancellationException ||
                        error.message?.contains("cancelled by user", ignoreCase = true) == true
                    if (isCancel) {
                        snackbarHostState.showSnackbar(Strings.downloadCanceled)
                    } else {
                        snackbarHostState.showSnackbar(
                            "${Strings.importHostsFailed}: ${error.message}"
                        )
                    }
                }
            )
        }
        downloadJobs[source.url] = job
    }

    WtaScreen(
        title = Strings.hostsAdBlock,
        subtitle = Strings.hostsAdBlockSubtitle,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        actions = {
            if (hostsRulesCount > 0 || enabledSources.isNotEmpty() || disabledSources.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = Strings.clearHostsRules)
                }
            }
        }
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SummaryCard(
                    rulesCount = hostsRulesCount,
                    activeCount = activeHostsCount,
                    sourcesCount = enabledSources.size + disabledSources.size,
                    enabledCount = enabledSources.size
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ImportActionCard(
                        title = Strings.importFromFile,
                        icon = Icons.Outlined.FileOpen,
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.weight(1f)
                    )
                    ImportActionCard(
                        title = Strings.importFromUrl,
                        icon = Icons.Outlined.Link,
                        onClick = { showUrlDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    Strings.popularHostsSources,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                WtaTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = Strings.hostsSearchHint,
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
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WtaChip(
                        selected = activeFilter == HostsFilter.ALL,
                        onClick = { filter = HostsFilter.ALL.name },
                        label = Strings.all,
                        showSelectedCheck = false
                    )
                    WtaChip(
                        selected = activeFilter == HostsFilter.DOWNLOADED,
                        onClick = { filter = HostsFilter.DOWNLOADED.name },
                        label = Strings.hostsFilterDownloaded,
                        showSelectedCheck = false
                    )
                    WtaChip(
                        selected = activeFilter == HostsFilter.NOT_DOWNLOADED,
                        onClick = { filter = HostsFilter.NOT_DOWNLOADED.name },
                        label = Strings.hostsFilterNotDownloaded,
                        showSelectedCheck = false
                    )
                }
            }

            if (filteredSources.isEmpty()) {
                item {
                    WtaFullEmptyState(
                        title = Strings.hostsNoMatch,
                        message = Strings.hostsNoMatchHint,
                        icon = Icons.Outlined.Search,
                        fillMaxSize = false
                    )
                }
            } else {
                items(filteredSources, key = { it.url }) { source ->
                    val downloaded = enabledSources.contains(source.url) || disabledSources.contains(source.url)
                    val enabled = enabledSources.contains(source.url)
                    val isDownloading = downloadProgress.containsKey(source.url)
                    val progress = downloadProgress[source.url]
                    val ruleCount = sourceCounts[source.url] ?: 0

                    HostsSourceCard(
                        source = source,
                        isDownloaded = downloaded,
                        isEnabled = enabled,
                        isDownloading = isDownloading,
                        progress = progress,
                        ruleCount = ruleCount,
                        onImport = { importSource(source) },
                        onCancel = {
                            downloadJobs[source.url]?.cancel()
                            downloadProgress.remove(source.url)
                            downloadJobs.remove(source.url)
                        },
                        onToggleEnabled = { checked ->
                            scope.launch {
                                adBlocker.setHostsSourceEnabled(context, source.url, checked)
                                adBlocker.saveHostsRules(context)
                                refreshState()
                                snackbarHostState.showSnackbar(
                                    if (checked) Strings.hostsSourceEnabled else Strings.hostsSourceDisabled
                                )
                            }
                        },
                        onDelete = { showDeleteSourceDialog = source }
                    )
                }
            }

            item {
                WtaCard(tone = WtaCardTone.Surface) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            Strings.hostsBlockingDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (hostsRulesCount > 0 || enabledSources.isNotEmpty() || disabledSources.isNotEmpty()) {
                item {
                    WtaButton(
                        onClick = { showClearDialog = true },
                        text = Strings.clearHostsRules,
                        variant = WtaButtonVariant.Outlined,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Outlined.DeleteSweep
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!urlImporting) {
                    showUrlDialog = false
                    importUrl = ""
                }
            },
            title = { Text(Strings.importFromUrl) },
            text = {
                WtaTextField(
                    value = importUrl,
                    onValueChange = { importUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = Strings.importHostsUrl,
                    placeholder = Strings.importHostsUrlHint,
                    singleLine = true,
                    enabled = !urlImporting
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (importUrl.isBlank() || urlImporting) return@TextButton
                        scope.launch {
                            urlImporting = true
                            val result = adBlocker.importHostsFromUrl(importUrl.trim(), context)
                            urlImporting = false
                            result.fold(
                                onSuccess = { count ->
                                    adBlocker.saveHostsRules(context)
                                    refreshState()
                                    showUrlDialog = false
                                    importUrl = ""
                                    snackbarHostState.showSnackbar(
                                        String.format(Locale.getDefault(), Strings.importHostsSuccess, count)
                                    )
                                },
                                onFailure = { error ->
                                    snackbarHostState.showSnackbar(
                                        "${Strings.importHostsFailed}: ${error.message}"
                                    )
                                }
                            )
                        }
                    },
                    enabled = importUrl.isNotBlank() && !urlImporting
                ) {
                    Text(if (urlImporting) Strings.downloading else Strings.downloadAndImport)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!urlImporting) {
                            showUrlDialog = false
                            importUrl = ""
                        }
                    }
                ) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(Strings.clearHostsRules) },
            text = { Text(Strings.clearHostsConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            adBlocker.clearHostsFileRules()
                            adBlocker.saveHostsRules(context)
                            refreshState()
                            showClearDialog = false
                            snackbarHostState.showSnackbar(Strings.hostsCleared)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }

    showDeleteSourceDialog?.let { source ->
        AlertDialog(
            onDismissRequest = { showDeleteSourceDialog = null },
            title = { Text(Strings.deleteHostsSource) },
            text = {
                Text(
                    String.format(
                        Locale.getDefault(),
                        Strings.deleteHostsSourceConfirm,
                        source.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            adBlocker.removeHostsSource(context, source.url)
                            adBlocker.saveHostsRules(context)
                            refreshState()
                            showDeleteSourceDialog = null
                            snackbarHostState.showSnackbar(Strings.hostsSourceDeleted)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSourceDialog = null }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(
    rulesCount: Int,
    activeCount: Int,
    sourcesCount: Int,
    enabledCount: Int
) {
    WtaCard(tone = WtaCardTone.Highlighted) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Shield,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    String.format(Locale.getDefault(), Strings.hostsRulesCount, rulesCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    String.format(
                        Locale.getDefault(),
                        Strings.hostsActiveRulesCount,
                        activeCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (sourcesCount > 0) {
                    Text(
                        String.format(
                            Locale.getDefault(),
                            Strings.hostsSourcesEnabledSummary,
                            enabledCount,
                            sourcesCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    WtaCard(
        onClick = onClick,
        modifier = modifier,
        tone = WtaCardTone.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HostsSourceCard(
    source: HostsSource,
    isDownloaded: Boolean,
    isEnabled: Boolean,
    isDownloading: Boolean,
    progress: DownloadProgress?,
    ruleCount: Int,
    onImport: () -> Unit,
    onCancel: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            source.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(8.dp))
                        StatusBadge(
                            text = when {
                                isDownloading -> Strings.downloading
                                isDownloaded && isEnabled -> Strings.hostsSourceEnabled
                                isDownloaded -> Strings.hostsSourceDisabled
                                else -> Strings.hostsFilterNotDownloaded
                            },
                            tonal = when {
                                isDownloading -> MaterialTheme.colorScheme.tertiaryContainer
                                isDownloaded && isEnabled -> MaterialTheme.colorScheme.primaryContainer
                                isDownloaded -> MaterialTheme.colorScheme.surfaceContainerHighest
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            content = when {
                                isDownloading -> MaterialTheme.colorScheme.tertiary
                                isDownloaded && isEnabled -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        source.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isDownloaded && ruleCount > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            String.format(Locale.getDefault(), Strings.hostsSourceRuleCount, ruleCount),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isDownloaded && !isDownloading) {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggleEnabled
                    )
                }
            }

            AnimatedVisibility(
                visible = isDownloading,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                DownloadProgressPanel(progress = progress, onCancel = onCancel)
            }

            if (!isDownloading) {
                if (isDownloaded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onImport,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Refresh, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(Strings.retry, style = MaterialTheme.typography.labelMedium)
                        }
                        FilledTonalButton(
                            onClick = onDelete,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Delete, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(Strings.delete, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    WtaButton(
                        onClick = onImport,
                        text = Strings.downloadAndImport,
                        variant = WtaButtonVariant.Tonal,
                        size = WtaButtonSize.Small,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Outlined.Download
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, tonal: androidx.compose.ui.graphics.Color, content: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(999.dp), color = tonal) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun DownloadProgressPanel(
    progress: DownloadProgress?,
    onCancel: () -> Unit
) {
    val p = progress ?: DownloadProgress(0, 0, 0)
    Column(modifier = Modifier.fillMaxWidth()) {
        if (p.isIndeterminate) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(
                progress = { p.fraction },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatDownloadStats(p),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onCancel,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Outlined.Cancel, null, Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(Strings.cancel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun formatDownloadStats(p: DownloadProgress): String {
    val downloaded = formatBytes(p.downloadedBytes)
    return if (p.isIndeterminate) {
        downloaded
    } else {
        val total = formatBytes(p.totalBytes)
        val percent = (p.fraction * 100).toInt()
        val eta = p.etaSeconds
        val etaStr = if (eta < 0) "" else " · ${formatEta(eta)}"
        "$downloaded / $total ($percent%)$etaStr"
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "${bytes} B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.getDefault(), "%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format(Locale.getDefault(), "%.1f MB", mb)
}

private fun formatEta(seconds: Long): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}
