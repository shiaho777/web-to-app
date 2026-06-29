package com.webtoapp.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material.icons.outlined.GetApp
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.components.formatFileSize
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaScreen
import androidx.core.content.FileProvider
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
    WEB_TO_APP("WebToApp");

    val isLogs: Boolean get() = this == LOGS
}

private data class FileEntry(
    val file: File,
    val name: String,
    val size: Long,
    val lastModified: Long
)

private data class SectionSnapshot(
    val section: FileSection,
    val entries: List<FileEntry>
) {
    val totalSize: Long get() = entries.sumOf { it.size }
    val isEmpty: Boolean get() = entries.isEmpty()
}

@Composable
fun FileManagerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val apkBuilder = remember { ApkBuilder(context.applicationContext) }

    var snapshots by remember { mutableStateOf<List<SectionSnapshot>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var expandedSection by remember { mutableStateOf<FileSection?>(null) }

    var pendingClear by remember { mutableStateOf<FileSection?>(null) }
    var pendingDelete by remember { mutableStateOf<FileEntry?>(null) }
    var viewingLog by remember { mutableStateOf<FileEntry?>(null) }
    var logContent by remember { mutableStateOf("") }
    var logLoading by remember { mutableStateOf(false) }

    suspend fun rescan() {
        loading = true
        val result = withContext(Dispatchers.IO) {
            val base = context.getExternalFilesDir(null) ?: return@withContext emptyList<SectionSnapshot>()
            FileSection.entries.map { section ->
                val dir = File(base, section.dirName)
                val entries = if (!dir.exists()) {
                    emptyList()
                } else {
                    dir.listFiles { f -> f.isFile }
                        ?.sortedByDescending { it.lastModified() }
                        ?.map { f ->
                            FileEntry(
                                file = f,
                                name = f.name,
                                size = f.length(),
                                lastModified = f.lastModified()
                            )
                        }
                        ?: emptyList()
                }
                SectionSnapshot(section, entries)
            }
        }
        snapshots = result
        loading = false
    }

    LaunchedEffect(Unit) { rescan() }

    WtaScreen(
        title = Strings.fileManagerTitle,
        subtitle = Strings.fileManagerSubtitle,
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) { _ ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val totalSize = snapshots.sumOf { it.totalSize }
            val totalCount = snapshots.sumOf { it.entries.size }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SummaryCard(totalSize = totalSize, totalCount = totalCount)
                }

                items(snapshots.size) { index ->
                    val snap = snapshots[index]
                    SectionCard(
                        snapshot = snap,
                        expanded = expandedSection == snap.section,
                        onToggle = {
                            expandedSection = if (expandedSection == snap.section) null else snap.section
                        },
                        onShare = { entry -> shareFile(context, entry.file, snackbarHostState, scope) },
                        onInstall = { entry ->
                            val started = apkBuilder.installApk(entry.file)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (started) Strings.install else entry.name
                                )
                            }
                        },
                        onOpen = { entry -> openFile(context, entry.file, snackbarHostState, scope) },
                        onView = { entry ->
                            viewingLog = entry
                            logLoading = true
                            logContent = ""
                            scope.launch {
                                val text = withContext(Dispatchers.IO) {
                                    runCatching { entry.file.readText() }
                                        .getOrNull()
                                        ?: ""
                                }
                                logContent = text
                                logLoading = false
                            }
                        },
                        onDelete = { entry -> pendingDelete = entry },
                        onClear = { pendingClear = snap.section }
                    )
                }
            }
        }
    }

    pendingClear?.let { section ->
        val dirLabel = when (section) {
            FileSection.APK -> Strings.fileManagerSectionApk
            FileSection.AAB -> Strings.fileManagerSectionAab
            FileSection.CLONED -> Strings.fileManagerSectionCloned
            FileSection.LOGS -> Strings.fileManagerSectionLogs
            FileSection.WEB_TO_APP -> Strings.fileManagerSectionUserFiles
        }
        ConfirmDialog(
            message = String.format(Strings.fileManagerClearConfirmDir, dirLabel),
            onConfirm = {
                val toClear = pendingClear ?: return@ConfirmDialog
                pendingClear = null
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val base = context.getExternalFilesDir(null)
                        base?.let { File(it, toClear.dirName).listFiles()?.forEach { f -> f.delete() } }
                    }
                    rescan()
                }
            },
            onDismiss = { pendingClear = null }
        )
    }

    pendingDelete?.let { entry ->
        ConfirmDialog(
            message = String.format(Strings.fileManagerDeleteConfirmFile, entry.name),
            onConfirm = {
                val toDelete = pendingDelete ?: return@ConfirmDialog
                pendingDelete = null
                scope.launch {
                    withContext(Dispatchers.IO) { toDelete.file.delete() }
                    rescan()
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
            },
            title = { Text(Strings.fileManagerLogViewerTitle) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(Modifier.height(8.dp))
                    if (logLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(Strings.fileManagerView, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = logContent.ifBlank { " " },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(360.dp)
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
                        scope.launch { snackbarHostState.showSnackbar(Strings.fileManagerView) }
                    }) {
                        Icon(Icons.Outlined.ContentCopy, Strings.copy, modifier = Modifier.size(18.dp))
                    }
                    TextButton(onClick = {
                        shareFile(context, entry.file, snackbarHostState, scope)
                    }) { Text(Strings.share) }
                    TextButton(onClick = {
                        viewingLog = null
                        logContent = ""
                    }) { Text(Strings.close) }
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(totalSize: Long, totalCount: Int) {
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
            }
        }
    }
}

@Composable
private fun SectionCard(
    snapshot: SectionSnapshot,
    expanded: Boolean,
    onToggle: () -> Unit,
    onShare: (FileEntry) -> Unit,
    onInstall: (FileEntry) -> Unit,
    onOpen: (FileEntry) -> Unit,
    onView: (FileEntry) -> Unit,
    onDelete: (FileEntry) -> Unit,
    onClear: () -> Unit
) {
    val canInstall = snapshot.section == FileSection.APK || snapshot.section == FileSection.CLONED
    val canOpen = snapshot.section == FileSection.WEB_TO_APP
    val sectionLabel = when (snapshot.section) {
        FileSection.APK -> Strings.fileManagerSectionApk
        FileSection.AAB -> Strings.fileManagerSectionAab
        FileSection.CLONED -> Strings.fileManagerSectionCloned
        FileSection.LOGS -> Strings.fileManagerSectionLogs
        FileSection.WEB_TO_APP -> Strings.fileManagerSectionUserFiles
    }
    val sectionIcon = when {
        snapshot.section.isLogs -> Icons.Outlined.Article
        snapshot.section == FileSection.WEB_TO_APP -> Icons.Outlined.FolderShared
        else -> Icons.Outlined.Folder
    }

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
                        sectionIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sectionLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${snapshot.entries.size} · ${formatFileSize(snapshot.totalSize)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!snapshot.isEmpty) {
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
                                canView = snapshot.section.isLogs,
                                canInstall = canInstall,
                                canOpen = canOpen,
                                onShare = { onShare(entry) },
                                onInstall = { onInstall(entry) },
                                onOpen = { onOpen(entry) },
                                onView = { onView(entry) },
                                onDelete = { onDelete(entry) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FileEntryRow(
    entry: FileEntry,
    canView: Boolean,
    canInstall: Boolean,
    canOpen: Boolean,
    onShare: () -> Unit,
    onInstall: () -> Unit,
    onOpen: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "${formatFileSize(entry.size)} · ${dateFormat.format(Date(entry.lastModified))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
                    Icons.Outlined.OpenInNew,
                    Strings.fileManagerOpen,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (canView) {
            IconButton(onClick = onView) {
                Icon(Icons.Outlined.Article, Strings.fileManagerView, modifier = Modifier.size(20.dp))
            }
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Outlined.Share, Strings.share, modifier = Modifier.size(20.dp))
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

@Composable
private fun ConfirmDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.fileManagerClear) },
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

private fun shareFile(
    context: Context,
    file: File,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    if (!file.exists()) {
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

private fun openFile(
    context: Context,
    file: File,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    if (!file.exists()) {
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
