package com.webtoapp.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.GetApp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.components.formatFileSize
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaFullEmptyState
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class FileSection(val dirName: String) {
    APK("built_apks"),
    AAB("built_aabs"),
    CLONED("cloned_apks"),
    LOGS("build_logs"),
    USER_PROJECTS("");

    val isLogs: Boolean get() = this == LOGS
    val isUserProjects: Boolean get() = this == USER_PROJECTS
    val canInstall: Boolean get() = this == APK || this == CLONED
    val canShare: Boolean get() = this != USER_PROJECTS
    val canOpen: Boolean get() = this == AAB || this == LOGS
}

private enum class FileSortMode {
    NEWEST,
    LARGEST,
    NAME
}

private data class FileEntry(
    val file: File,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val section: FileSection
) {
    val key: String get() = file.absolutePath
}

private data class SectionSnapshot(
    val section: FileSection,
    val entries: List<FileEntry>
) {
    val totalSize: Long get() = entries.sumOf { it.size }
    val isEmpty: Boolean get() = entries.isEmpty()
}

private val USER_PROJECT_DIRS = listOf(
    "html_projects",
    "frontend_builds",
    "nodejs_projects",
    "php_projects",
    "python_projects",
    "go_projects",
    "wordpress_projects",
    "docs_projects",
    "scraped_sites",
    "sample_projects"
)

private const val MAX_LOG_CHARS = 200_000

@Composable
fun FileManagerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val apkBuilder = remember { ApkBuilder(context.applicationContext) }

    var snapshots by remember { mutableStateOf<List<SectionSnapshot>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var query by rememberSaveable { mutableStateOf("") }
    var filterSection by rememberSaveable { mutableStateOf<String?>(null) }
    var sortMode by rememberSaveable { mutableStateOf(FileSortMode.NEWEST.name) }
    var expandedSections by remember {
        mutableStateOf(FileSection.entries.toSet())
    }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSortMenu by remember { mutableStateOf(false) }

    var pendingClear by remember { mutableStateOf<FileSection?>(null) }
    var pendingDelete by remember { mutableStateOf<List<FileEntry>?>(null) }
    var viewingLog by remember { mutableStateOf<FileEntry?>(null) }
    var logContent by remember { mutableStateOf("") }
    var logLoading by remember { mutableStateOf(false) }
    var logTruncated by remember { mutableStateOf(false) }

    val activeSort = remember(sortMode) {
        runCatching { FileSortMode.valueOf(sortMode) }.getOrDefault(FileSortMode.NEWEST)
    }
    val activeFilter = remember(filterSection) {
        filterSection?.let { name ->
            runCatching { FileSection.valueOf(name) }.getOrNull()
        }
    }

    suspend fun rescan(keepSelection: Boolean = false) {
        loading = true
        val result = withContext(Dispatchers.IO) {
            scanAllSections(context)
        }
        snapshots = result
        if (!keepSelection) {
            selectedKeys = emptySet()
            selectionMode = false
        } else {
            val alive = result.flatMap { it.entries }.map { it.key }.toSet()
            selectedKeys = selectedKeys.intersect(alive)
            if (selectedKeys.isEmpty()) selectionMode = false
        }
        loading = false
    }

    LaunchedEffect(Unit) { rescan() }

    val filteredSnapshots = remember(snapshots, query, activeFilter, activeSort) {
        val q = query.trim()
        snapshots.mapNotNull { snap ->
            if (activeFilter != null && snap.section != activeFilter) return@mapNotNull null
            val filtered = snap.entries
                .asSequence()
                .filter { entry ->
                    q.isEmpty() || entry.name.contains(q, ignoreCase = true)
                }
                .sortedWith(entryComparator(activeSort))
                .toList()
            if (q.isNotEmpty() && filtered.isEmpty() && activeFilter == null) {
                null
            } else {
                snap.copy(entries = filtered)
            }
        }.let { list ->
            if (q.isNotEmpty()) list.filter { !it.isEmpty || activeFilter != null } else list
        }
    }

    val visibleEntries = remember(filteredSnapshots) {
        filteredSnapshots.flatMap { it.entries }
    }
    val totalSize = remember(snapshots) { snapshots.sumOf { it.totalSize } }
    val totalCount = remember(snapshots) { snapshots.sumOf { it.entries.size } }
    val visibleSize = remember(filteredSnapshots) { filteredSnapshots.sumOf { it.totalSize } }
    val visibleCount = visibleEntries.size
    val selectedEntries = remember(visibleEntries, selectedKeys) {
        visibleEntries.filter { it.key in selectedKeys }
    }

    fun toggleSelect(entry: FileEntry) {
        selectedKeys = if (entry.key in selectedKeys) {
            selectedKeys - entry.key
        } else {
            selectedKeys + entry.key
        }
        if (selectedKeys.isEmpty()) selectionMode = false
    }

    fun enterSelection(entry: FileEntry? = null) {
        selectionMode = true
        if (entry != null) selectedKeys = setOf(entry.key)
    }

    fun exitSelection() {
        selectionMode = false
        selectedKeys = emptySet()
    }

    fun selectVisibleAll() {
        selectionMode = true
        selectedKeys = visibleEntries.map { it.key }.toSet()
    }

    WtaScreen(
        title = if (selectionMode) {
            String.format(Strings.selectedCount2, selectedKeys.size)
        } else {
            Strings.fileManagerTitle
        },
        subtitle = if (selectionMode) null else Strings.fileManagerSubtitle,
        onBack = {
            if (selectionMode) exitSelection() else onBack()
        },
        snackbarHostState = snackbarHostState,
        actions = {
            if (selectionMode) {
                IconButton(onClick = {
                    if (selectedKeys.size == visibleEntries.size && visibleEntries.isNotEmpty()) {
                        selectedKeys = emptySet()
                        selectionMode = false
                    } else {
                        selectVisibleAll()
                    }
                }) {
                    Icon(
                        if (selectedKeys.size == visibleEntries.size && visibleEntries.isNotEmpty()) {
                            Icons.Outlined.CheckBox
                        } else {
                            Icons.Outlined.CheckBoxOutlineBlank
                        },
                        contentDescription = Strings.selectAll
                    )
                }
                IconButton(onClick = { exitSelection() }) {
                    Icon(Icons.Outlined.Close, contentDescription = Strings.close)
                }
            } else {
                IconButton(
                    onClick = { scope.launch { rescan() } },
                    enabled = !loading
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = Strings.refresh)
                }
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = Strings.fileManagerSort)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(Strings.fileManagerSortNewest) },
                            onClick = {
                                sortMode = FileSortMode.NEWEST.name
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(Strings.fileManagerSortLargest) },
                            onClick = {
                                sortMode = FileSortMode.LARGEST.name
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(Strings.fileManagerSortName) },
                            onClick = {
                                sortMode = FileSortMode.NAME.name
                                showSortMenu = false
                            }
                        )
                    }
                }
                if (visibleEntries.isNotEmpty()) {
                    TextButton(onClick = { enterSelection() }) {
                        Text(Strings.fileManagerSelect)
                    }
                }
            }
        },
        bottomBar = {
            if (selectionMode && selectedKeys.isNotEmpty()) {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val canShareSelected = selectedEntries.any {
                            it.section.canShare && it.file.isFile
                        }
                        if (canShareSelected) {
                            WtaButton(
                                onClick = {
                                    val files = selectedEntries.filter {
                                        it.section.canShare && it.file.isFile && it.file.exists()
                                    }
                                    if (files.isEmpty()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(Strings.fileManagerEmpty)
                                        }
                                    } else if (files.size == 1) {
                                        shareFile(context, files.first().file, snackbarHostState, scope)
                                    } else {
                                        shareMultipleFiles(
                                            context,
                                            files.map { it.file },
                                            snackbarHostState,
                                            scope
                                        )
                                    }
                                },
                                text = Strings.share,
                                variant = WtaButtonVariant.Tonal,
                                size = WtaButtonSize.Small,
                                leadingIcon = Icons.Outlined.Share,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        WtaButton(
                            onClick = { pendingDelete = selectedEntries },
                            text = Strings.btnDelete,
                            variant = WtaButtonVariant.Primary,
                            size = WtaButtonSize.Small,
                            leadingIcon = Icons.Outlined.Delete,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { _ ->
        if (loading && snapshots.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryCard(
                        totalSize = if (query.isBlank() && activeFilter == null) totalSize else visibleSize,
                        totalCount = if (query.isBlank() && activeFilter == null) totalCount else visibleCount,
                        filtered = query.isNotBlank() || activeFilter != null
                    )

                    WtaTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = Strings.fileManagerSearchHint,
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WtaChip(
                            selected = activeFilter == null,
                            onClick = { filterSection = null },
                            label = Strings.all,
                            showSelectedCheck = false
                        )
                        FileSection.entries.forEach { section ->
                            val count = snapshots.firstOrNull { it.section == section }?.entries?.size ?: 0
                            WtaChip(
                                selected = activeFilter == section,
                                onClick = {
                                    filterSection = if (activeFilter == section) null else section.name
                                    if (activeFilter != section) {
                                        expandedSections = expandedSections + section
                                    }
                                },
                                label = "${sectionLabel(section)} ($count)",
                                showSelectedCheck = false
                            )
                        }
                    }
                }

                if (filteredSnapshots.isEmpty() || (visibleCount == 0 && (query.isNotBlank() || activeFilter != null))) {
                    WtaFullEmptyState(
                        title = if (query.isNotBlank()) Strings.fileManagerNoMatch else Strings.fileManagerEmpty,
                        message = if (query.isNotBlank()) {
                            Strings.fileManagerNoMatchHint
                        } else {
                            Strings.fileManagerEmptyHint
                        },
                        icon = Icons.Outlined.Folder
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredSnapshots, key = { it.section.name }) { snap ->
                            val expanded = activeFilter != null || snap.section in expandedSections
                            SectionCard(
                                snapshot = snap,
                                expanded = expanded,
                                selectionMode = selectionMode,
                                selectedKeys = selectedKeys,
                                onToggle = {
                                    expandedSections = if (snap.section in expandedSections) {
                                        expandedSections - snap.section
                                    } else {
                                        expandedSections + snap.section
                                    }
                                },
                                onShare = { entry ->
                                    shareFile(context, entry.file, snackbarHostState, scope)
                                },
                                onInstall = { entry ->
                                    val started = apkBuilder.installApk(entry.file)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (started) Strings.fileManagerInstallStarted else Strings.fileManagerInstallFailed
                                        )
                                    }
                                },
                                onOpen = { entry ->
                                    openFile(context, entry.file, snackbarHostState, scope)
                                },
                                onView = { entry ->
                                    viewingLog = entry
                                    logLoading = true
                                    logContent = ""
                                    logTruncated = false
                                    scope.launch {
                                        val (text, truncated) = withContext(Dispatchers.IO) {
                                            readLogPreview(entry.file)
                                        }
                                        logContent = text
                                        logTruncated = truncated
                                        logLoading = false
                                    }
                                },
                                onDelete = { entry -> pendingDelete = listOf(entry) },
                                onClear = { pendingClear = snap.section },
                                onSelectToggle = { entry ->
                                    if (!selectionMode) enterSelection(entry) else toggleSelect(entry)
                                },
                                onLongPress = { entry -> enterSelection(entry) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    pendingClear?.let { section ->
        ConfirmDialog(
            title = Strings.fileManagerClear,
            message = String.format(Strings.fileManagerClearConfirmDir, sectionLabel(section)),
            onConfirm = {
                val toClear = pendingClear ?: return@ConfirmDialog
                pendingClear = null
                scope.launch {
                    withContext(Dispatchers.IO) { clearSection(context, toClear) }
                    rescan()
                    snackbarHostState.showSnackbar(Strings.fileManagerCleared)
                }
            },
            onDismiss = { pendingClear = null }
        )
    }

    pendingDelete?.let { entries ->
        val message = if (entries.size == 1) {
            String.format(Strings.fileManagerDeleteConfirmFile, entries.first().name)
        } else {
            String.format(Strings.fileManagerDeleteConfirmMany, entries.size)
        }
        ConfirmDialog(
            title = Strings.btnDelete,
            message = message,
            onConfirm = {
                val toDelete = pendingDelete ?: return@ConfirmDialog
                pendingDelete = null
                scope.launch {
                    withContext(Dispatchers.IO) {
                        toDelete.forEach { entry ->
                            if (entry.file.isDirectory) entry.file.deleteRecursively()
                            else entry.file.delete()
                        }
                    }
                    rescan()
                    snackbarHostState.showSnackbar(Strings.deleted)
                }
            },
            onDismiss = { pendingDelete = null }
        )
    }

    viewingLog?.let { entry ->
        AlertDialog(
            onDismissRequest = {
                viewingLog = null
                logContent = ""
                logLoading = false
                logTruncated = false
            },
            title = { Text(Strings.fileManagerLogViewerTitle) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    if (logLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(Strings.fileManagerView, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        if (logTruncated) {
                            Text(
                                text = Strings.fileManagerLogTruncated,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = logContent.ifBlank { " " },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 360.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    IconButton(onClick = {
                        copyToClipboard(context, entry.name, logContent)
                        scope.launch { snackbarHostState.showSnackbar(Strings.copied) }
                    }) {
                        Icon(Icons.Outlined.ContentCopy, Strings.copy, modifier = Modifier.size(18.dp))
                    }
                    TextButton(onClick = {
                        shareFile(context, entry.file, snackbarHostState, scope)
                    }) { Text(Strings.share) }
                    TextButton(onClick = {
                        viewingLog = null
                        logContent = ""
                        logTruncated = false
                    }) { Text(Strings.close) }
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(totalSize: Long, totalCount: Int, filtered: Boolean) {
    WtaCard(tone = WtaCardTone.Highlighted) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.fileManagerTotalUsage
                        .replace("%s", formatFileSize(totalSize))
                        .replace("%d", totalCount.toString()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (filtered) {
                    Text(
                        text = Strings.fileManagerFilteredHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SectionCard(
    snapshot: SectionSnapshot,
    expanded: Boolean,
    selectionMode: Boolean,
    selectedKeys: Set<String>,
    onToggle: () -> Unit,
    onShare: (FileEntry) -> Unit,
    onInstall: (FileEntry) -> Unit,
    onOpen: (FileEntry) -> Unit,
    onView: (FileEntry) -> Unit,
    onDelete: (FileEntry) -> Unit,
    onClear: () -> Unit,
    onSelectToggle: (FileEntry) -> Unit,
    onLongPress: (FileEntry) -> Unit
) {
    val section = snapshot.section
    WtaCard(tone = WtaCardTone.Surface) {
        Column {
            Surface(
                onClick = onToggle,
                shape = RoundedCornerShape(0.dp),
                color = androidx.compose.ui.graphics.Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        sectionIcon(section),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sectionLabel(section),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${snapshot.entries.size} · ${formatFileSize(snapshot.totalSize)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!snapshot.isEmpty && !selectionMode) {
                        TextButton(
                            onClick = onClear,
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Outlined.Clear, Strings.fileManagerClear, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(Strings.fileManagerClear, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Icon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    if (snapshot.isEmpty) {
                        Text(
                            text = Strings.fileManagerEmpty,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        snapshot.entries.forEachIndexed { index, entry ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                            FileEntryRow(
                                entry = entry,
                                selected = entry.key in selectedKeys,
                                selectionMode = selectionMode,
                                canView = section.isLogs,
                                canInstall = section.canInstall,
                                canOpen = section.canOpen,
                                canShare = section.canShare && entry.file.isFile,
                                onShare = { onShare(entry) },
                                onInstall = { onInstall(entry) },
                                onOpen = { onOpen(entry) },
                                onView = { onView(entry) },
                                onDelete = { onDelete(entry) },
                                onSelectToggle = { onSelectToggle(entry) },
                                onLongPress = { onLongPress(entry) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileEntryRow(
    entry: FileEntry,
    selected: Boolean,
    selectionMode: Boolean,
    canView: Boolean,
    canInstall: Boolean,
    canOpen: Boolean,
    canShare: Boolean,
    onShare: () -> Unit,
    onInstall: () -> Unit,
    onOpen: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit,
    onSelectToggle: () -> Unit,
    onLongPress: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val container = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        androidx.compose.ui.graphics.Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = {
                    if (selectionMode) onSelectToggle()
                    else if (canView) onView()
                    else if (canInstall) onInstall()
                    else if (canOpen) onOpen()
                },
                onLongClick = onLongPress
            )
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = container,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionMode) {
                    Icon(
                        if (selected) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                        contentDescription = null,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(22.dp)
                    )
                } else {
                    Icon(
                        entryIcon(entry),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatFileSize(entry.size)} · ${dateFormat.format(Date(entry.lastModified))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!selectionMode) {
                    if (canInstall) {
                        IconButton(onClick = onInstall) {
                            Icon(
                                Icons.Outlined.GetApp,
                                Strings.install,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (canOpen) {
                        IconButton(onClick = onOpen) {
                            Icon(
                                Icons.AutoMirrored.Outlined.OpenInNew,
                                Strings.fileManagerOpen,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (canView) {
                        IconButton(onClick = onView) {
                            Icon(Icons.AutoMirrored.Outlined.Article, Strings.fileManagerView, modifier = Modifier.size(20.dp))
                        }
                    }
                    if (canShare) {
                        IconButton(onClick = onShare) {
                            Icon(Icons.Outlined.Share, Strings.share, modifier = Modifier.size(20.dp))
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Outlined.Delete,
                            Strings.btnDelete,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(Strings.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.btnCancel)
            }
        }
    )
}

private fun sectionLabel(section: FileSection): String = when (section) {
    FileSection.APK -> Strings.fileManagerSectionApk
    FileSection.AAB -> Strings.fileManagerSectionAab
    FileSection.CLONED -> Strings.fileManagerSectionCloned
    FileSection.LOGS -> Strings.fileManagerSectionLogs
    FileSection.USER_PROJECTS -> Strings.fileManagerSectionUserFiles
}

private fun sectionIcon(section: FileSection): ImageVector = when {
    section.isLogs -> Icons.AutoMirrored.Outlined.Article
    section.isUserProjects -> Icons.Outlined.FolderShared
    section == FileSection.APK || section == FileSection.CLONED -> Icons.Outlined.Android
    else -> Icons.Outlined.Folder
}

private fun entryIcon(entry: FileEntry): ImageVector = when {
    entry.file.isDirectory -> Icons.Outlined.FolderShared
    entry.section.isLogs -> Icons.AutoMirrored.Outlined.Article
    entry.section.canInstall -> Icons.Outlined.Android
    else -> Icons.Outlined.Folder
}

private fun entryComparator(sort: FileSortMode): Comparator<FileEntry> = when (sort) {
    FileSortMode.NEWEST -> compareByDescending { it.lastModified }
    FileSortMode.LARGEST -> compareByDescending { it.size }
    FileSortMode.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
}

private fun scanAllSections(context: Context): List<SectionSnapshot> {
    val externalBase = context.getExternalFilesDir(null)
    val filesBase = context.filesDir
    return FileSection.entries.map { section ->
        if (section.isUserProjects) {
            scanUserProjects(filesBase)
        } else {
            val base = externalBase ?: return@map SectionSnapshot(section, emptyList())
            val dir = File(base, section.dirName)
            val entries = if (!dir.exists()) {
                emptyList()
            } else {
                dir.listFiles { f -> f.isFile }
                    ?.map { f ->
                        FileEntry(
                            file = f,
                            name = f.name,
                            size = f.length(),
                            lastModified = f.lastModified(),
                            section = section
                        )
                    }
                    ?: emptyList()
            }
            SectionSnapshot(section, entries)
        }
    }
}

private fun scanUserProjects(filesBase: File): SectionSnapshot {
    val entries = USER_PROJECT_DIRS.flatMap { projectDirName ->
        val projectDir = File(filesBase, projectDirName)
        if (!projectDir.exists() || !projectDir.isDirectory) return@flatMap emptyList()
        projectDir.listFiles { f -> f.isDirectory }
            ?.map { sub ->
                FileEntry(
                    file = sub,
                    name = "$projectDirName/${sub.name}",
                    size = dirSize(sub),
                    lastModified = sub.lastModified(),
                    section = FileSection.USER_PROJECTS
                )
            }
            ?: emptyList()
    }
    return SectionSnapshot(FileSection.USER_PROJECTS, entries)
}

private fun dirSize(dir: File): Long {
    var size = 0L
    val stack = ArrayDeque<File>()
    stack.addLast(dir)
    while (stack.isNotEmpty()) {
        val current = stack.removeLast()
        current.listFiles()?.forEach { f ->
            if (f.isFile) size += f.length()
            else if (f.isDirectory) stack.addLast(f)
        }
    }
    return size
}

private fun clearSection(context: Context, section: FileSection) {
    if (section.isUserProjects) {
        val filesBase = context.filesDir
        USER_PROJECT_DIRS.forEach { dirName ->
            File(filesBase, dirName)
                .listFiles { f -> f.isDirectory }
                ?.forEach { sub -> sub.deleteRecursively() }
        }
    } else {
        val base = context.getExternalFilesDir(null) ?: return
        File(base, section.dirName).listFiles()?.forEach { f ->
            if (f.isDirectory) f.deleteRecursively() else f.delete()
        }
    }
}

private fun readLogPreview(file: File): Pair<String, Boolean> {
    if (!file.exists() || !file.isFile) return "" to false
    return runCatching {
        file.bufferedReader().use { reader ->
            val sb = StringBuilder()
            var truncated = false
            while (true) {
                val line = reader.readLine() ?: break
                if (sb.length + line.length + 1 > MAX_LOG_CHARS) {
                    truncated = true
                    break
                }
                if (sb.isNotEmpty()) sb.append('\n')
                sb.append(line)
            }
            sb.toString() to truncated
        }
    }.getOrDefault("" to false)
}

private fun shareFile(
    context: Context,
    file: File,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    if (!file.exists() || !file.isFile) {
        scope.launch { snackbarHostState.showSnackbar(file.name) }
        return
    }
    try {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = guessMimeType(file)
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, Strings.share)
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(chooser)
    } catch (e: Exception) {
        scope.launch { snackbarHostState.showSnackbar(e.message ?: e.javaClass.simpleName) }
    }
}

private fun shareMultipleFiles(
    context: Context,
    files: List<File>,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    try {
        val uris = ArrayList(files.map { file ->
            FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
        })
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, Strings.share)
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(chooser)
    } catch (e: Exception) {
        scope.launch { snackbarHostState.showSnackbar(e.message ?: e.javaClass.simpleName) }
    }
}

private fun openFile(
    context: Context,
    file: File,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    if (!file.exists() || !file.isFile) {
        scope.launch { snackbarHostState.showSnackbar(file.name) }
        return
    }
    try {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, guessMimeType(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(viewIntent, Strings.fileManagerOpen)
        context.startActivity(chooser)
    } catch (e: Exception) {
        scope.launch { snackbarHostState.showSnackbar(e.message ?: e.javaClass.simpleName) }
    }
}

private fun guessMimeType(file: File): String {
    val ext = file.extension.lowercase(Locale.getDefault())
    return when (ext) {
        "apk" -> "application/vnd.android.package-archive"
        "aab" -> "application/octet-stream"
        "log", "txt" -> "text/plain"
        else -> android.webkit.MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(ext)
            ?: "application/octet-stream"
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
        as android.content.ClipboardManager
    clipboard.setPrimaryClip(android.content.ClipData.newPlainText(label, text))
}
