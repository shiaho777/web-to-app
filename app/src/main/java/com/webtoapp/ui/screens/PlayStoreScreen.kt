package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.playstore.PlayPolicyChecker
import com.webtoapp.core.playstore.aab.AabExportException
import com.webtoapp.core.playstore.aab.AabExporter
import com.webtoapp.core.playstore.aab.AabExportCoordinator
import com.webtoapp.core.playstore.aab.FailureStage
import com.webtoapp.data.model.WebApp
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.viewmodel.MainViewModel
import java.io.File
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayStoreScreen(
    onBack: () -> Unit,
    initialAppId: Long? = null,
    autoStartExport: Boolean = false,
    viewModel: MainViewModel = koinViewModel()
) {
    val webApps by viewModel.webApps.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val coordinator = remember(context) {
        AabExportCoordinator(context.applicationContext)
    }

    var selectedAppId by remember { mutableStateOf(initialAppId) }
    var report by remember { mutableStateOf<PlayPolicyChecker.Report?>(null) }
    var exportState by remember { mutableStateOf<ExportState>(ExportState.Idle) }
    var policyExpanded by rememberSaveable { mutableStateOf(true) }
    var exportJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var appQuery by rememberSaveable { mutableStateOf("") }
    var showWarningConfirm by remember { mutableStateOf(false) }
    var pendingExportApp by remember { mutableStateOf<WebApp?>(null) }
    var recentAabs by remember { mutableStateOf<List<File>>(emptyList()) }
    var hasBuiltApk by remember { mutableStateOf(false) }

    val selectedApp = webApps.firstOrNull { it.id == selectedAppId }
    val filteredApps = remember(webApps, appQuery) {
        val q = appQuery.trim()
        if (q.isEmpty()) webApps
        else webApps.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.url.contains(q, ignoreCase = true) ||
                it.appType.name.contains(q, ignoreCase = true)
        }
    }

    fun refreshRecentAabs() {
        recentAabs = coordinator.listExportedAabs().take(8)
    }

    LaunchedEffect(Unit) {
        refreshRecentAabs()
    }

    LaunchedEffect(webApps) {
        if (selectedAppId == null && webApps.isNotEmpty()) {
            selectedAppId = if (initialAppId != null && webApps.any { it.id == initialAppId }) {
                initialAppId
            } else {
                webApps.first().id
            }
        }
        if (selectedAppId != null && webApps.none { it.id == selectedAppId }) {
            selectedAppId = webApps.firstOrNull()?.id
        }
    }

    LaunchedEffect(selectedApp?.id, selectedApp?.updatedAt) {
        val app = selectedApp
        if (app == null) {
            report = null
            hasBuiltApk = false
            return@LaunchedEffect
        }
        report = withContext(Dispatchers.Default) { PlayPolicyChecker.check(app) }
        hasBuiltApk = coordinator.hasBuiltApk(app)
        if ((report?.blockerCount ?: 0) > 0) {
            policyExpanded = true
        }
    }

    fun runExport(app: WebApp) {
        if (exportState is ExportState.Running) return
        exportJob?.cancel()
        exportJob = scope.launch {
            exportState = ExportState.Running(AabExporter.Stage.STARTING, 0)
            exportState = withContext(Dispatchers.IO) {
                try {
                    val result = coordinator.export(app) { stage, percent ->
                        exportState = ExportState.Running(stage, percent)
                    }
                    refreshRecentAabs()
                    hasBuiltApk = true
                    ExportState.Success(result.signedAab.absolutePath)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    ExportState.Cancelled
                } catch (e: AabExportException) {
                    ExportState.Failed(
                        failureStage = e.failureStage,
                        technicalDetails = e.message ?: e.javaClass.simpleName,
                        throwable = e
                    )
                } catch (e: Exception) {
                    ExportState.Failed(
                        failureStage = FailureStage.UNKNOWN,
                        technicalDetails = e.message ?: e.javaClass.simpleName,
                        throwable = e
                    )
                }
            }
        }
    }

    fun requestExport(app: WebApp) {
        val current = report ?: PlayPolicyChecker.check(app)
        report = current
        when {
            current.blockerCount > 0 -> {
                policyExpanded = true
                scope.launch {
                    snackbarHostState.showSnackbar(Strings.playStoreExportBlockedHint)
                }
            }
            current.warningCount > 0 -> {
                pendingExportApp = app
                showWarningConfirm = true
            }
            else -> runExport(app)
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        exportJob = null
        exportState = ExportState.Cancelled
    }

    LaunchedEffect(autoStartExport, selectedApp?.id) {
        if (autoStartExport && selectedApp != null && exportState is ExportState.Idle) {
            requestExport(selectedApp)
        }
    }

    if (showWarningConfirm) {
        AlertDialog(
            onDismissRequest = {
                showWarningConfirm = false
                pendingExportApp = null
            },
            title = { Text(Strings.playStoreExportWarningTitle) },
            text = {
                Text(
                    String.format(
                        Strings.playStoreExportWarningBody,
                        report?.warningCount ?: pendingExportApp?.let { PlayPolicyChecker.check(it).warningCount } ?: 0
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val app = pendingExportApp
                        showWarningConfirm = false
                        pendingExportApp = null
                        if (app != null) runExport(app)
                    }
                ) {
                    Text(Strings.playStoreExportContinueAnyway)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showWarningConfirm = false
                        pendingExportApp = null
                        policyExpanded = true
                    }
                ) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }

    WtaScreen(
        title = Strings.playStoreTitle,
        subtitle = Strings.playStoreSubtitle,
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AppSelectionCard(
                    apps = filteredApps,
                    totalCount = webApps.size,
                    query = appQuery,
                    onQueryChange = { appQuery = it },
                    selectedAppId = selectedAppId,
                    onSelectApp = { id ->
                        selectedAppId = id
                        if (exportState !is ExportState.Running) {
                            exportState = ExportState.Idle
                        }
                    }
                )
            }

            if (selectedApp != null) {
                item {
                    val blockers = report?.blockerCount ?: 0
                    val warnings = report?.warningCount ?: 0
                    val infos = report?.infoCount ?: 0
                    ExportActionCard(
                        app = selectedApp,
                        hasBuiltApk = hasBuiltApk,
                        blockers = blockers,
                        warnings = warnings,
                        infos = infos,
                        isRunning = exportState is ExportState.Running,
                        onExport = { requestExport(selectedApp) }
                    )
                }

                if (exportState !is ExportState.Idle) {
                    item {
                        ExportStateCard(
                            state = exportState,
                            onShare = { path ->
                                shareAab(context, File(path), snackbarHostState, scope)
                            },
                            onCancel = { cancelExport() }
                        )
                    }
                }

                item {
                    PolicyAdviceCard(
                        expanded = policyExpanded,
                        onToggle = { policyExpanded = !policyExpanded },
                        report = report,
                        webApp = selectedApp
                    )
                }
            }

            if (recentAabs.isNotEmpty()) {
                item {
                    RecentAabCard(
                        files = recentAabs,
                        onShare = { file ->
                            shareAab(context, file, snackbarHostState, scope)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppSelectionCard(
    apps: List<WebApp>,
    totalCount: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedAppId: Long?,
    onSelectApp: (Long) -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.PlayCircleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.playStoreSelectApp,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (totalCount > 0) {
                        Text(
                            text = Strings.playStoreAppCount(totalCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (totalCount == 0) {
                Text(
                    text = Strings.playStoreNoAppsHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (totalCount > 6) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null)
                        },
                        placeholder = { Text(Strings.search) }
                    )
                }

                if (apps.isEmpty()) {
                    Text(
                        text = Strings.playStoreNoMatch,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(apps, key = { it.id }) { app ->
                            AppRow(
                                app = app,
                                isSelected = app.id == selectedAppId,
                                onClick = { onSelectApp(app.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: WebApp,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val container = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val onContainer = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = onContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = app.url.ifBlank { app.appType.name }
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainer.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ExportActionCard(
    app: WebApp,
    hasBuiltApk: Boolean,
    blockers: Int,
    warnings: Int,
    infos: Int,
    isRunning: Boolean,
    onExport: () -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (blockers > 0) {
                    StatusChip(
                        label = Strings.playStoreSeverityBlocker + " $blockers",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (warnings > 0) {
                    StatusChip(
                        label = Strings.playStoreSeverityWarning + " $warnings",
                        color = Color(0xFFED6C02)
                    )
                }
                if (infos > 0) {
                    StatusChip(
                        label = Strings.playStoreSeverityInfo + " $infos",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (blockers == 0 && warnings == 0) {
                    StatusChip(
                        label = Strings.playStoreReportClean,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Text(
                text = if (hasBuiltApk) Strings.playStoreHasApkHint else Strings.playStoreNoApkHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (blockers > 0) {
                Text(
                    text = Strings.playStoreExportBlockedHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            WtaButton(
                onClick = onExport,
                text = when {
                    isRunning -> Strings.playStoreExportRunning
                    blockers > 0 -> Strings.playStoreFixBeforeExport
                    else -> Strings.playStoreExportAabButton
                },
                variant = if (blockers > 0) WtaButtonVariant.Outlined else WtaButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning && blockers == 0,
                leadingIcon = Icons.Outlined.PlayCircleOutline
            )
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun RecentAabCard(
    files: List<File>,
    onShare: (File) -> Unit
) {
    val formatter = remember {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    }
    WtaCard(tone = WtaCardTone.Surface) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = Strings.playStoreRecentAabs,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            files.forEach { file ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatter.format(Date(file.lastModified())) +
                                    " · " + formatFileSize(file.length()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { onShare(file) }) {
                            Icon(Icons.Outlined.Share, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(Strings.playStoreExportShare)
                        }
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "${bytes}B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1fKB", kb)
    val mb = kb / 1024.0
    return String.format("%.1fMB", mb)
}

@Composable
private fun PolicyAdviceCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    report: PlayPolicyChecker.Report?,
    webApp: WebApp
) {
    WtaCard(tone = WtaCardTone.Surface) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                onClick = onToggle,
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Policy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Strings.playStoreAdviceTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = Strings.playStoreAdviceSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val rpt = report ?: PlayPolicyChecker.check(webApp)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportSummaryCard(report = rpt)
                    rpt.violations.forEach { violation ->
                        ViolationCard(violation = violation)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(report: PlayPolicyChecker.Report) {
    val summary = when {
        report.isClean -> ReportSummary(
            icon = Icons.Outlined.CheckCircle,
            tone = WtaCardTone.Highlighted,
            headlineColor = Color(0xFF2E7D32),
            headline = Strings.playStoreReportClean
        )
        report.blockerCount > 0 -> ReportSummary(
            icon = Icons.Outlined.Block,
            tone = WtaCardTone.Critical,
            headlineColor = MaterialTheme.colorScheme.error,
            headline = String.format(Strings.playStoreReportBlocked, report.blockerCount)
        )
        else -> ReportSummary(
            icon = Icons.Outlined.Warning,
            tone = WtaCardTone.Elevated,
            headlineColor = Color(0xFFED6C02),
            headline = String.format(Strings.playStoreReportWarning, report.warningCount)
        )
    }

    WtaCard(tone = summary.tone) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    summary.icon,
                    contentDescription = null,
                    tint = summary.headlineColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = summary.headline,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = summary.headlineColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (report.isClean) {
                    Strings.playStoreReportCleanDesc
                } else {
                    Strings.playStoreSummaryTitle
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!report.isClean) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (report.blockerCount > 0) {
                        StatusChip(
                            label = "${Strings.playStoreSeverityBlocker} ${report.blockerCount}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (report.warningCount > 0) {
                        StatusChip(
                            label = "${Strings.playStoreSeverityWarning} ${report.warningCount}",
                            color = Color(0xFFED6C02)
                        )
                    }
                    if (report.infoCount > 0) {
                        StatusChip(
                            label = "${Strings.playStoreSeverityInfo} ${report.infoCount}",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViolationCard(violation: PlayPolicyChecker.Violation) {
    val resolved = PlayPolicyChecker.resolveViolation(violation)
    val severityColor = when (violation.severity) {
        PlayPolicyChecker.Severity.BLOCKER -> MaterialTheme.colorScheme.error
        PlayPolicyChecker.Severity.WARNING -> Color(0xFFED6C02)
        PlayPolicyChecker.Severity.INFO -> MaterialTheme.colorScheme.primary
    }
    val severityIcon = when (violation.severity) {
        PlayPolicyChecker.Severity.BLOCKER -> Icons.Outlined.Block
        PlayPolicyChecker.Severity.WARNING -> Icons.Outlined.Warning
        PlayPolicyChecker.Severity.INFO -> Icons.Outlined.Info
    }
    val severityLabel = when (violation.severity) {
        PlayPolicyChecker.Severity.BLOCKER -> Strings.playStoreSeverityBlocker
        PlayPolicyChecker.Severity.WARNING -> Strings.playStoreSeverityWarning
        PlayPolicyChecker.Severity.INFO -> Strings.playStoreSeverityInfo
    }

    WtaCard(tone = WtaCardTone.Elevated) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = severityColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            severityIcon,
                            contentDescription = null,
                            tint = severityColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = severityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = severityColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = resolved.featurePath,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = Strings.playStorePolicyAreaLabel + ": ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = resolved.policyArea,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = Strings.playStoreFixHintLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = resolved.fixHint,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private data class ReportSummary(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tone: WtaCardTone,
    val headlineColor: Color,
    val headline: String
)

internal sealed interface ExportState {
    data object Idle : ExportState

    data class Running(
        val stage: AabExporter.Stage,
        val percent: Int
    ) : ExportState

    data class Success(val aabPath: String) : ExportState

    data object Cancelled : ExportState

    data class Failed(
        val failureStage: FailureStage,
        val technicalDetails: String,
        val throwable: Throwable? = null
    ) : ExportState
}

@Composable
private fun ExportStateCard(
    state: ExportState,
    onShare: (path: String) -> Unit,
    onCancel: () -> Unit
) {
    when (state) {
        is ExportState.Idle -> Unit

        is ExportState.Running -> {
            WtaCard(tone = WtaCardTone.Highlighted) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = Strings.playStoreExportRunning,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${stageLabel(state.stage)} (${state.percent}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { state.percent / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.material3.TextButton(onClick = onCancel) {
                        Text(Strings.btnCancel)
                    }
                }
            }
        }

        is ExportState.Success -> {
            WtaCard(tone = WtaCardTone.Highlighted) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Strings.playStoreExportSuccess,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = Strings.playStoreExportSuccessDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.aabPath,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Strings.playStoreExportPreUploadHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    WtaButton(
                        onClick = { onShare(state.aabPath) },
                        text = Strings.playStoreExportShare,
                        variant = WtaButtonVariant.Tonal,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Outlined.Share
                    )
                }
            }
        }

        is ExportState.Cancelled -> {
            WtaCard(tone = WtaCardTone.Elevated) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Strings.playStoreExportCancelled,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        is ExportState.Failed -> {
            WtaCard(tone = WtaCardTone.Critical) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = failureStageLabel(state.failureStage),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (state.technicalDetails.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Strings.playStoreExportTechnicalDetails,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.technicalDetails,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    val report = remember(state.failureStage, state.technicalDetails, state.throwable) {
                        com.webtoapp.ui.components.buildErrorReport(
                            scope = "AAB export",
                            message = state.technicalDetails,
                            throwable = state.throwable,
                            contextLines = listOf("failureStage=${state.failureStage.name}")
                        )
                    }
                    com.webtoapp.ui.components.WtaErrorDetailsSection(
                        report = report,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun shareAab(
    context: android.content.Context,
    aab: File,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            aab
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, aab.name)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = android.content.Intent.createChooser(intent, Strings.playStoreExportShare)
            .apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(chooser)
    } catch (e: Exception) {
        scope.launch {
            snackbarHostState.showSnackbar(e.message ?: e.javaClass.simpleName)
        }
    }
}

private fun stageLabel(stage: AabExporter.Stage): String {
    return when (stage) {
        AabExporter.Stage.STARTING -> Strings.playStoreExportStageStarting
        AabExporter.Stage.BUILDING_APK -> Strings.playStoreExportStageBuildingApk
        AabExporter.Stage.ASSEMBLING -> Strings.playStoreExportStageAssembling
        AabExporter.Stage.ASSEMBLED -> Strings.playStoreExportStageAssembled
        AabExporter.Stage.SIGNING -> Strings.playStoreExportStageSigning
        AabExporter.Stage.SIGNED -> Strings.playStoreExportStageSigned
    }
}

private fun failureStageLabel(stage: FailureStage): String {
    return when (stage) {
        FailureStage.BUILD_APK -> Strings.playStoreExportFailureBuildApk
        FailureStage.ASSEMBLE -> Strings.playStoreExportFailureAssemble
        FailureStage.SIGN -> Strings.playStoreExportFailureSign
        FailureStage.UNKNOWN -> Strings.playStoreExportFailureUnknown
    }
}
