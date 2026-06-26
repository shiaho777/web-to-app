package com.webtoapp.ui.screens

import android.net.Uri
import com.webtoapp.ui.components.PremiumOutlinedButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.webtoapp.ui.components.EnhancedElevatedCard
import com.webtoapp.ui.components.PremiumTextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.adblock.AdBlocker
import com.webtoapp.core.adblock.HostsSource
import com.webtoapp.core.adblock.DownloadProgress
import com.webtoapp.core.i18n.Strings
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.webtoapp.ui.design.WtaBackground
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostsAdBlockScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val adBlocker = remember { org.koin.java.KoinJavaComponent.get<com.webtoapp.core.adblock.AdBlocker>(com.webtoapp.core.adblock.AdBlocker::class.java) }

    var hostsRulesCount by remember { mutableIntStateOf(adBlocker.getImportedHostsRuleCount()) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showDeleteSourceDialog by remember { mutableStateOf<HostsSource?>(null) }
    var importUrl by remember { mutableStateOf("") }

    var enabledSources by remember { mutableStateOf(adBlocker.getEnabledHostsSources()) }
    var disabledSources by remember { mutableStateOf(adBlocker.getDisabledHostsSources()) }

    val downloadProgress = remember { mutableStateMapOf<String, DownloadProgress>() }
    val downloadJobs = remember { mutableStateMapOf<String, Job>() }
    var expandedSources by remember { mutableStateOf(emptySet<String>()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val result = adBlocker.importHostsFromFile(context, it)
                result.fold(
                    onSuccess = { count ->
                        hostsRulesCount = adBlocker.getImportedHostsRuleCount()
                        enabledSources = adBlocker.getEnabledHostsSources()
                        disabledSources = adBlocker.getDisabledHostsSources()
                        adBlocker.saveHostsRules(context)
                        snackbarHostState.showSnackbar(
                            String.format(java.util.Locale.getDefault(), Strings.importHostsSuccess, count)
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
        hostsRulesCount = adBlocker.getImportedHostsRuleCount()
        enabledSources = adBlocker.getEnabledHostsSources()
        disabledSources = adBlocker.getDisabledHostsSources()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(Strings.hostsAdBlock, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text(
                            Strings.hostsAdBlockSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, Strings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(

                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        WtaBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
        LazyColumn(
            modifier = Modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                EnhancedElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                String.format(java.util.Locale.getDefault(), Strings.hostsRulesCount, hostsRulesCount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (enabledSources.isNotEmpty() || disabledSources.isNotEmpty()) {
                                val downloadedCount = enabledSources.size + disabledSources.size
                                Text(
                                    String.format(
                                        java.util.Locale.getDefault(),
                                        Strings.hostsSourcesSummary,
                                        enabledSources.size,
                                        downloadedCount
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        if (hostsRulesCount > 0 || enabledSources.isNotEmpty()) {
                            Icon(
                                Icons.Outlined.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    Strings.importFromFile,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    EnhancedElevatedCard(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.weight(weight = 1f, fill = true)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.FileOpen,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                Strings.importFromFile,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    EnhancedElevatedCard(
                        onClick = { showUrlDialog = true },
                        modifier = Modifier.weight(weight = 1f, fill = true)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.Link,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                Strings.importFromUrl,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    Strings.popularHostsSources,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            items(AdBlocker.POPULAR_HOSTS_SOURCES) { source ->
                val isDownloaded = enabledSources.contains(source.url) || disabledSources.contains(source.url)
                val isEnabled = enabledSources.contains(source.url)
                val isDownloading = downloadProgress.containsKey(source.url)
                val progress = downloadProgress[source.url]
                val isExpanded = expandedSources.contains(source.url)

                HostsSourceCard(
                    source = source,
                    isDownloaded = isDownloaded,
                    isEnabled = isEnabled,
                    isDownloading = isDownloading,
                    progress = progress,
                    isExpanded = isExpanded,
                    onImport = {
                        expandedSources = expandedSources + source.url
                        val job = scope.launch {
                            downloadProgress[source.url] = DownloadProgress(0, 0, 0)
                            val result = adBlocker.importHostsFromUrl(source.url, context) { p ->
                                downloadProgress[source.url] = p
                            }
                            downloadProgress.remove(source.url)
                            downloadJobs.remove(source.url)
                            result.fold(
                                onSuccess = { count ->
                                    hostsRulesCount = adBlocker.getImportedHostsRuleCount()
                                    enabledSources = adBlocker.getEnabledHostsSources()
                                    disabledSources = adBlocker.getDisabledHostsSources()
                                    adBlocker.saveHostsRules(context)
                                    expandedSources = expandedSources - source.url
                                    snackbarHostState.showSnackbar(
                                        String.format(java.util.Locale.getDefault(), Strings.importHostsSuccess, count)
                                    )
                                },
                                onFailure = { error ->
                                    if (error is kotlinx.coroutines.CancellationException) {
                                        snackbarHostState.showSnackbar(Strings.downloadCanceled)
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            "${Strings.importHostsFailed}: ${error.message}"
                                        )
                                    }
                                    expandedSources = expandedSources - source.url
                                }
                            )
                        }
                        downloadJobs[source.url] = job
                    },
                    onToggleDownload = {
                        downloadJobs[source.url]?.cancel()
                        downloadProgress.remove(source.url)
                        downloadJobs.remove(source.url)
                        expandedSources = expandedSources - source.url
                    },
                    onToggleExpand = {
                        expandedSources = if (isExpanded) expandedSources - source.url else expandedSources + source.url
                    },
                    onToggle = {
                        scope.launch {
                            val enable = !isEnabled
                            adBlocker.setHostsSourceEnabled(context, source.url, enable)
                            enabledSources = adBlocker.getEnabledHostsSources()
                            disabledSources = adBlocker.getDisabledHostsSources()
                            hostsRulesCount = adBlocker.getImportedHostsRuleCount()
                            adBlocker.saveHostsRules(context)
                            snackbarHostState.showSnackbar(
                                if (enable) Strings.hostsSourceEnabledToast else Strings.hostsSourceDisabledToast
                            )
                        }
                    },
                    onDelete = {
                        showDeleteSourceDialog = source
                    }
                )
            }

            if (hostsRulesCount > 0 || enabledSources.isNotEmpty() || disabledSources.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumOutlinedButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Outlined.DeleteSweep, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.clearHostsRules)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                EnhancedElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp)
                    ) {
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
        }
    }

    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text(Strings.importFromUrl) },
            text = {
                Column {
                    PremiumTextField(
                        value = importUrl,
                        onValueChange = { importUrl = it },
                        label = { Text(Strings.importHostsUrl) },
                        placeholder = { Text(Strings.importHostsUrlHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (importUrl.isNotBlank()) {
                            scope.launch {
                                showUrlDialog = false
                                val result = adBlocker.importHostsFromUrl(importUrl, context)
                                result.fold(
                                    onSuccess = { count ->
                                        hostsRulesCount = adBlocker.getImportedHostsRuleCount()
                                        enabledSources = adBlocker.getEnabledHostsSources()
                                        disabledSources = adBlocker.getDisabledHostsSources()
                                        adBlocker.saveHostsRules(context)
                                        snackbarHostState.showSnackbar(
                                            String.format(java.util.Locale.getDefault(), Strings.importHostsSuccess, count)
                                        )
                                        importUrl = ""
                                    },
                                    onFailure = { error ->
                                        snackbarHostState.showSnackbar(
                                            "${Strings.importHostsFailed}: ${error.message}"
                                        )
                                    }
                                )
                            }
                        }
                    },
                    enabled = importUrl.isNotBlank()
                ) {
                    Text(Strings.downloadAndImport)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false; importUrl = "" }) {
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
                            hostsRulesCount = 0
                            enabledSources = emptySet()
                            disabledSources = emptySet()
                            showClearDialog = false
                            snackbarHostState.showSnackbar(Strings.hostsCleared)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.btnConfirm)
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
            text = { Text(String.format(java.util.Locale.getDefault(), Strings.deleteHostsSourceConfirm, source.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            adBlocker.removeHostsSource(context, source.url)
                            enabledSources = adBlocker.getEnabledHostsSources()
                            disabledSources = adBlocker.getDisabledHostsSources()
                            hostsRulesCount = adBlocker.getImportedHostsRuleCount()
                            adBlocker.saveHostsRules(context)
                            showDeleteSourceDialog = null
                            snackbarHostState.showSnackbar(Strings.hostsSourceDeleted)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.btnConfirm)
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
}

@Composable
private fun HostsSourceCard(
    source: HostsSource,
    isDownloaded: Boolean,
    isEnabled: Boolean,
    isDownloading: Boolean,
    progress: DownloadProgress?,
    isExpanded: Boolean,
    onImport: () -> Unit,
    onToggleDownload: () -> Unit,
    onToggleExpand: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    EnhancedElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            source.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        if (isDownloading) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    Strings.downloading,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        } else if (isDownloaded) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = if (isEnabled) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    if (isEnabled) Strings.hostsSourceEnabled else Strings.hostsSourceDisabled,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = if (isEnabled) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        source.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isDownloading) {
                    IconButton(onClick = onToggleExpand, modifier = Modifier.size(28.dp)) {
                        Icon(
                            if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isDownloading && isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                DownloadProgressPanel(
                    progress = progress,
                    onCancel = onToggleDownload
                )
            }

            if (!isDownloading) {
                Spacer(modifier = Modifier.height(12.dp))
                if (isDownloaded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = onImport,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Refresh, null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Strings.retry, style = MaterialTheme.typography.labelMedium)
                        }

                        FilledTonalButton(
                            onClick = onToggle,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (isEnabled) Icons.Outlined.Block else Icons.Outlined.CheckCircle,
                                null, Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isEnabled) Strings.disable else Strings.enable,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        FilledTonalButton(
                            onClick = onDelete,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Outlined.Delete, null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Strings.delete, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    FilledTonalButton(
                        onClick = onImport,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Download, null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.downloadAndImport, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadProgressPanel(
    progress: DownloadProgress?,
    onCancel: () -> Unit
) {
    val p = progress ?: DownloadProgress(0, 0, 0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        if (p.isIndeterminate) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            LinearProgressIndicator(
                progress = { p.fraction },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

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
                Spacer(modifier = Modifier.width(4.dp))
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
    if (kb < 1024) return String.format(java.util.Locale.getDefault(), "%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format(java.util.Locale.getDefault(), "%.1f MB", mb)
}

private fun formatEta(seconds: Long): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}
