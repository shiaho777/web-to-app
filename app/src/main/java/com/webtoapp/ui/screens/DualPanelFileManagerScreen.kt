package com.webtoapp.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.ui.components.formatFileSize
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.ui.design.WtaAlertDialog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import org.json.JSONArray

private const val PANEL_PREFS = "dual_panel_file_manager"

private data class EntryNameDialogState(
    val isFolder: Boolean,
    val entryToRename: DocumentFile?
)

private class DualPanelState(
    context: Context,
    private val id: String
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PANEL_PREFS, Context.MODE_PRIVATE)
    var treeUri by mutableStateOf(prefs.getString("${id}_tree", null))
        private set
    var path by mutableStateOf(readPath())
        private set
    var selected by mutableStateOf<Set<String>>(emptySet())
    var query by mutableStateOf("")
    var showHidden by mutableStateOf(false)
    var refreshToken by mutableStateOf(0)
    var loading by mutableStateOf(false)
    var entries by mutableStateOf<List<DocumentFile>>(emptyList())
    var accessError by mutableStateOf(false)

    fun setTree(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        appContext.contentResolver.takePersistableUriPermission(uri, flags)
        val root = DocumentFile.fromTreeUri(appContext, uri)
        if (root == null || !root.canRead()) {
            runCatching { appContext.contentResolver.releasePersistableUriPermission(uri, flags) }
            throw IOException("The selected folder is not readable")
        }
        treeUri?.let { previous ->
            if (previous != uri.toString()) {
                runCatching {
                    appContext.contentResolver.releasePersistableUriPermission(Uri.parse(previous), flags)
                }.onFailure {
                    AppLogger.w("DualPanelFileManager", "Could not release previous folder access", it)
                }
            }
        }
        treeUri = uri.toString()
        path = emptyList()
        selected = emptySet()
        prefs.edit()
            .putString("${id}_tree", uri.toString())
            .putString("${id}_path", "[]")
            .apply()
    }

    fun navigate(nextPath: List<String>) {
        path = nextPath
        selected = emptySet()
        prefs.edit().putString(
            "${id}_path",
            JSONArray().apply { nextPath.forEach { put(it) } }.toString()
        ).apply()
    }

    fun currentDirectory(context: Context): DocumentFile? {
        val root = treeUri?.let { DocumentFile.fromTreeUri(context, android.net.Uri.parse(it)) }
            ?: return null
        return path.fold(root) { parent, name ->
            parent.findFile(name)?.takeIf { it.isDirectory }
                ?: throw IOException("The current folder is no longer available")
        }
    }

    private fun readPath(): List<String> {
        val saved = prefs.getString("${id}_path", null) ?: return emptyList()
        return try {
            val paths = JSONArray(saved)
            List(paths.length()) { index -> paths.getString(index) }
        } catch (error: Exception) {
            AppLogger.w("DualPanelFileManager", "Saved panel path is invalid", error)
            emptyList()
        }
    }
}

@Composable
fun DualPanelFileManagerScreen(
    onBack: () -> Unit,
    onOpenBuildFiles: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val left = remember(context) { DualPanelState(context, "left") }
    val right = remember(context) { DualPanelState(context, "right") }
    var busy by remember { mutableStateOf(false) }
    var operationJob by remember { mutableStateOf<Job?>(null) }

    val leftFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            runCatching { left.setTree(uri) }
                .onFailure {
                    AppLogger.w("DualPanelFileManager", "Could not grant access to left panel folder", it)
                    scope.launch { snackbarHostState.showSnackbar(Strings.dualPanelAccessFailed) }
                }
        }
    }
    val rightFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            runCatching { right.setTree(uri) }
                .onFailure {
                    AppLogger.w("DualPanelFileManager", "Could not grant access to right panel folder", it)
                    scope.launch { snackbarHostState.showSnackbar(Strings.dualPanelAccessFailed) }
                }
        }
    }

    LaunchedEffect(left.treeUri, left.path, left.showHidden, left.refreshToken) {
        loadPanel(context, left)
    }
    LaunchedEffect(right.treeUri, right.path, right.showHidden, right.refreshToken) {
        loadPanel(context, right)
    }

    suspend fun transfer(source: DualPanelState, target: DualPanelState, move: Boolean) {
        val selected = source.entries.filter { it.uri.toString() in source.selected }
        if (selected.isEmpty()) return
        busy = true
        snackbarHostState.currentSnackbarData?.dismiss()
        try {
            withContext(Dispatchers.IO) {
                val destination = target.currentDirectory(context)
                if (destination == null || !destination.canWrite()) {
                    throw IOException("The destination folder is not writable")
                }
                for (entry in selected) {
                    kotlinx.coroutines.currentCoroutineContext().ensureActive()
                    val sourceRelativePath = source.path + (entry.name ?: "")
                    if (source.treeUri == target.treeUri &&
                        target.path.size >= sourceRelativePath.size &&
                        target.path.take(sourceRelativePath.size) == sourceRelativePath
                    ) {
                        throw IOException("A folder cannot be copied into itself")
                    }
                    copyDocument(context, entry, destination)
                    if (move) {
                        kotlinx.coroutines.currentCoroutineContext().ensureActive()
                        deleteDocument(entry)
                    }
                }
            }
            source.selected = emptySet()
            source.refreshToken++
            target.refreshToken++
            snackbarHostState.showSnackbar(Strings.dualPanelTransferComplete)
        } catch (cancelled: CancellationException) {
            source.selected = emptySet()
            source.refreshToken++
            target.refreshToken++
            throw cancelled
        } catch (error: Exception) {
            AppLogger.e("DualPanelFileManager", "File transfer failed", error)
            source.selected = emptySet()
            source.refreshToken++
            target.refreshToken++
            snackbarHostState.showSnackbar(Strings.dualPanelTransferFailed)
        } finally {
            busy = false
            operationJob = null
        }
    }

    suspend fun performOperation(state: DualPanelState, operation: suspend () -> Unit) {
        if (busy) return
        busy = true
        try {
            withContext(Dispatchers.IO) { operation() }
            state.selected = emptySet()
            state.refreshToken++
            snackbarHostState.showSnackbar(Strings.dualPanelTransferComplete)
        } catch (cancelled: CancellationException) {
            state.refreshToken++
            throw cancelled
        } catch (error: Exception) {
            AppLogger.e("DualPanelFileManager", "File operation failed", error)
            state.selected = emptySet()
            state.refreshToken++
            snackbarHostState.showSnackbar(Strings.dualPanelTransferFailed)
        } finally {
            busy = false
            operationJob = null
        }
    }

    fun launchOperation(operation: suspend () -> Unit) {
        if (busy || operationJob?.isActive == true) return
        operationJob = scope.launch { operation() }
    }

    fun openEntry(entry: DocumentFile) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(entry.uri, entry.type ?: "application/octet-stream")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            AppLogger.w("DualPanelFileManager", "No app can open the selected file", error)
            scope.launch { snackbarHostState.showSnackbar(Strings.dualPanelOpenFailed) }
        } catch (error: SecurityException) {
            AppLogger.w("DualPanelFileManager", "The selected file could not be opened", error)
            scope.launch { snackbarHostState.showSnackbar(Strings.dualPanelOpenFailed) }
        }
    }

    WtaScreen(
        title = Strings.fileManagerTitle,
        subtitle = Strings.dualPanelSubtitle,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        actions = {
            IconButton(onClick = onOpenBuildFiles) {
                Icon(
                    Icons.Outlined.FolderOpen,
                    contentDescription = Strings.fileManagerSectionApk
                )
            }
            IconButton(
                onClick = {
                    left.refreshToken++
                    right.refreshToken++
                },
                enabled = !busy
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = Strings.refresh)
            }
        },
        bottomBar = {
            if (busy) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(
                        Strings.dualPanelWorking,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = { operationJob?.cancel() }) {
                        Text(Strings.cancel)
                    }
                }
            }
        }
    ) { _ ->
        val horizontal = LocalConfiguration.current.screenWidthDp >= 700
        if (horizontal) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Panel(
                    state = left,
                    label = "A",
                    towardRight = true,
                    panelEnabled = !busy,
                    onChooseFolder = { leftFolderPicker.launch(left.treeUri?.let { Uri.parse(it) }) },
                    onTransfer = { move ->
                        launchOperation { transfer(left, right, move) }
                    },
                    onCreateEntry = { name, isFolder ->
                        launchOperation {
                            performOperation(left) { createEntry(context, left, name, isFolder) }
                        }
                    },
                    onRenameEntry = { entry, name ->
                        launchOperation {
                            performOperation(left) { renameEntry(context, left, entry, name) }
                        }
                    },
                    onDeleteEntries = { entries ->
                        launchOperation {
                            performOperation(left) { deleteEntries(entries) }
                        }
                    },
                    onOpenEntry = { entry -> openEntry(entry) },
                    transferEnabled = !busy && left.selected.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
                Panel(
                    state = right,
                    label = "B",
                    towardRight = false,
                    panelEnabled = !busy,
                    onChooseFolder = { rightFolderPicker.launch(right.treeUri?.let { Uri.parse(it) }) },
                    onTransfer = { move ->
                        launchOperation { transfer(right, left, move) }
                    },
                    onCreateEntry = { name, isFolder ->
                        launchOperation {
                            performOperation(right) { createEntry(context, right, name, isFolder) }
                        }
                    },
                    onRenameEntry = { entry, name ->
                        launchOperation {
                            performOperation(right) { renameEntry(context, right, entry, name) }
                        }
                    },
                    onDeleteEntries = { entries ->
                        launchOperation {
                            performOperation(right) { deleteEntries(entries) }
                        }
                    },
                    onOpenEntry = { entry -> openEntry(entry) },
                    transferEnabled = !busy && right.selected.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Panel(
                    state = left,
                    label = "A",
                    towardRight = true,
                    panelEnabled = !busy,
                    onChooseFolder = { leftFolderPicker.launch(left.treeUri?.let { Uri.parse(it) }) },
                    onTransfer = { move ->
                        launchOperation { transfer(left, right, move) }
                    },
                    onCreateEntry = { name, isFolder ->
                        launchOperation {
                            performOperation(left) { createEntry(context, left, name, isFolder) }
                        }
                    },
                    onRenameEntry = { entry, name ->
                        launchOperation {
                            performOperation(left) { renameEntry(context, left, entry, name) }
                        }
                    },
                    onDeleteEntries = { entries ->
                        launchOperation {
                            performOperation(left) { deleteEntries(entries) }
                        }
                    },
                    onOpenEntry = { entry -> openEntry(entry) },
                    transferEnabled = !busy && left.selected.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
                Panel(
                    state = right,
                    label = "B",
                    towardRight = false,
                    panelEnabled = !busy,
                    onChooseFolder = { rightFolderPicker.launch(right.treeUri?.let { Uri.parse(it) }) },
                    onTransfer = { move ->
                        launchOperation { transfer(right, left, move) }
                    },
                    onCreateEntry = { name, isFolder ->
                        launchOperation {
                            performOperation(right) { createEntry(context, right, name, isFolder) }
                        }
                    },
                    onRenameEntry = { entry, name ->
                        launchOperation {
                            performOperation(right) { renameEntry(context, right, entry, name) }
                        }
                    },
                    onDeleteEntries = { entries ->
                        launchOperation {
                            performOperation(right) { deleteEntries(entries) }
                        }
                    },
                    onOpenEntry = { entry -> openEntry(entry) },
                    transferEnabled = !busy && right.selected.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Panel(
    state: DualPanelState,
    label: String,
    towardRight: Boolean,
    panelEnabled: Boolean,
    onChooseFolder: () -> Unit,
    onTransfer: (Boolean) -> Unit,
    onCreateEntry: (String, Boolean) -> Unit,
    onRenameEntry: (DocumentFile, String) -> Unit,
    onDeleteEntries: (List<DocumentFile>) -> Unit,
    onOpenEntry: (DocumentFile) -> Unit,
    transferEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showActions by remember { mutableStateOf(false) }
    var nameDialog by remember { mutableStateOf<EntryNameDialogState?>(null) }
    var nameValue by remember { mutableStateOf("") }
    var detailsEntry by remember { mutableStateOf<DocumentFile?>(null) }
    var deleteEntries by remember { mutableStateOf<List<DocumentFile>?>(null) }
    val selectedEntries = state.entries.filter { it.uri.toString() in state.selected }

    WtaCard(
        modifier = modifier,
        tone = WtaCardTone.Surface
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = { state.showHidden = !state.showHidden },
                    enabled = panelEnabled
                ) {
                    Icon(
                        if (state.showHidden) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = Strings.dualPanelToggleHidden
                    )
                }
                TextButton(onClick = onChooseFolder, enabled = panelEnabled) {
                    Text(Strings.folderSelectFolder, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box {
                    IconButton(
                        onClick = { showActions = true },
                        enabled = panelEnabled && state.treeUri != null
                    ) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = Strings.moreOptions)
                    }
                    DropdownMenu(
                        expanded = showActions,
                        onDismissRequest = { showActions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(Strings.dualPanelNewFile) },
                            onClick = {
                                showActions = false
                                nameValue = ""
                                nameDialog = EntryNameDialogState(isFolder = false, entryToRename = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(Strings.dualPanelNewFolder) },
                            onClick = {
                                showActions = false
                                nameValue = ""
                                nameDialog = EntryNameDialogState(isFolder = true, entryToRename = null)
                            }
                        )
                        if (selectedEntries.size == 1) {
                            DropdownMenuItem(
                                text = { Text(Strings.dualPanelRename) },
                                onClick = {
                                    showActions = false
                                    nameValue = selectedEntries.single().name.orEmpty()
                                    nameDialog = EntryNameDialogState(
                                        isFolder = selectedEntries.single().isDirectory,
                                        entryToRename = selectedEntries.single()
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.details) },
                                onClick = {
                                    showActions = false
                                    detailsEntry = selectedEntries.single()
                                }
                            )
                        }
                        if (selectedEntries.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text(Strings.delete) },
                                onClick = {
                                    showActions = false
                                    deleteEntries = selectedEntries
                                }
                            )
                        }
                    }
                }
            }
            if (state.treeUri == null) {
                Text(
                    Strings.folderSelectHint,
                    modifier = Modifier.padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (state.path.isNotEmpty()) state.navigate(state.path.dropLast(1)) },
                        enabled = state.path.isNotEmpty() && panelEnabled
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = Strings.back)
                    }
                    Text(
                        text = state.path.lastOrNull() ?: DocumentFile
                            .fromTreeUri(context, android.net.Uri.parse(state.treeUri))
                            ?.name.orEmpty(),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium
                    )
                    IconButton(onClick = { state.refreshToken++ }, enabled = panelEnabled) {
                        Icon(Icons.Outlined.Refresh, contentDescription = Strings.refresh)
                    }
                }
                WtaTextField(
                    value = state.query,
                    onValueChange = { state.query = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = panelEnabled,
                    placeholder = Strings.fileManagerSearchHint,
                    singleLine = true
                )
                Spacer(Modifier.height(6.dp))
                if (state.accessError) {
                    Text(
                        Strings.dualPanelAccessFailed,
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (state.loading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val visible = state.entries.filter {
                        (state.showHidden || !it.name.orEmpty().startsWith(".")) &&
                            it.name.orEmpty().contains(state.query, ignoreCase = true)
                    }
                    if (visible.isEmpty()) {
                        Text(
                            if (state.query.isBlank()) Strings.fileManagerEmpty else Strings.fileManagerNoMatch,
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(visible, key = { it.uri.toString() }) { entry ->
                                PanelEntry(
                                    entry = entry,
                                    selected = entry.uri.toString() in state.selected,
                                    enabled = panelEnabled,
                                    onClick = {
                                        if (entry.isDirectory) {
                                            val name = entry.name ?: return@PanelEntry
                                            state.navigate(state.path + name)
                                        } else {
                                            onOpenEntry(entry)
                                        }
                                    },
                                    onLongClick = {
                                        val key = entry.uri.toString()
                                        state.selected = if (key in state.selected) {
                                            state.selected - key
                                        } else {
                                            state.selected + key
                                        }
                                    }
                                )
                            }
                        }
                    }

                    nameDialog?.let { dialog ->
                        val title = when {
                            dialog.entryToRename != null -> Strings.dualPanelRename
                            dialog.isFolder -> Strings.dualPanelNewFolder
                            else -> Strings.dualPanelNewFile
                        }
                        WtaAlertDialog(
                            onDismissRequest = { nameDialog = null },
                            title = title,
                            content = {
                                WtaTextField(
                                    value = nameValue,
                                    onValueChange = { nameValue = it },
                                    label = Strings.dualPanelName,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.None,
                                        imeAction = ImeAction.Done
                                    )
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val name = nameValue.trim()
                                        if (dialog.entryToRename != null) {
                                            onRenameEntry(dialog.entryToRename, name)
                                        } else {
                                            onCreateEntry(name, dialog.isFolder)
                                        }
                                        nameDialog = null
                                    }
                                ) {
                                    Text(Strings.confirm)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { nameDialog = null }) {
                                    Text(Strings.cancel)
                                }
                            }
                        )
                    }

                    deleteEntries?.let { entries ->
                        val confirmation = if (entries.size == 1) {
                            String.format(Strings.fileManagerDeleteConfirmFile, entries.single().name.orEmpty())
                        } else {
                            String.format(Strings.fileManagerDeleteConfirmMany, entries.size)
                        }
                        WtaAlertDialog(
                            onDismissRequest = { deleteEntries = null },
                            title = Strings.deleteConfirmTitle,
                            text = confirmation,
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        onDeleteEntries(entries)
                                        deleteEntries = null
                                    }
                                ) {
                                    Text(Strings.delete)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { deleteEntries = null }) {
                                    Text(Strings.cancel)
                                }
                            }
                        )
                    }

                    detailsEntry?.let { entry ->
                        WtaAlertDialog(
                            onDismissRequest = { detailsEntry = null },
                            title = entry.name,
                            content = {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        "${Strings.dualPanelFileType}: ${
                                            entry.type ?: if (entry.isDirectory) Strings.dualPanelFolderType
                                            else Strings.dualPanelFileTypeValue
                                        }"
                                    )
                                    Text(
                                        "${Strings.size}: ${
                                            if (entry.isFile) formatFileSize(entry.length()) else Strings.sizeUnknown
                                        }"
                                    )
                                    Text(if (entry.canWrite()) Strings.dualPanelWritable else Strings.dualPanelReadOnly)
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { detailsEntry = null }) {
                                    Text(Strings.close)
                                }
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { onTransfer(false) },
                        enabled = transferEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (towardRight) Icons.AutoMirrored.Outlined.ArrowForward
                            else Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(Strings.copy)
                    }
                    TextButton(
                        onClick = { onTransfer(true) },
                        enabled = transferEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (towardRight) Icons.AutoMirrored.Outlined.ArrowForward
                            else Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(Strings.dualPanelMove)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PanelEntry(
    entry: DocumentFile,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            when {
                selected -> Icons.Outlined.CheckBox
                entry.isDirectory -> Icons.Outlined.Folder
                else -> Icons.Outlined.InsertDriveFile
            },
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.name.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            if (entry.isFile) {
                Text(
                    formatFileSize(entry.length()),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private suspend fun loadPanel(context: Context, state: DualPanelState) {
    state.loading = true
    state.accessError = false
    try {
        state.entries = withContext(Dispatchers.IO) {
            val directory = state.currentDirectory(context) ?: return@withContext emptyList()
            directory.listFiles().sortedWith(
                compareBy<DocumentFile> { !it.isDirectory }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name.orEmpty() }
            )
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        AppLogger.w("DualPanelFileManager", "Could not read panel directory", error)
        state.entries = emptyList()
        state.accessError = true
    } finally {
        state.loading = false
    }
}

private suspend fun copyDocument(
    context: Context,
    source: DocumentFile,
    destinationDirectory: DocumentFile
): DocumentFile {
    val name = source.name ?: throw IOException("The source entry has no name")
    if (!destinationDirectory.canWrite()) throw IOException("The destination folder is not writable")
    if (destinationDirectory.findFile(name) != null) {
        throw IOException("An entry with that name already exists")
    }
    val target = if (source.isDirectory) {
        destinationDirectory.createDirectory(name)
    } else {
        destinationDirectory.createFile(mimeTypeFor(name), name)
    } ?: throw IOException("The destination entry could not be created")

    try {
        if (source.isDirectory) {
            source.listFiles().forEach { child ->
                kotlinx.coroutines.currentCoroutineContext().ensureActive()
                copyDocument(context, child, target)
            }
        } else {
            val input = context.contentResolver.openInputStream(source.uri)
                ?: throw IOException("The source file could not be opened")
            val output = context.contentResolver.openOutputStream(target.uri, "w")
                ?: throw IOException("The destination file could not be opened")
            input.use { from ->
                output.use { to ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var copied = 0L
                    while (true) {
                        kotlinx.coroutines.currentCoroutineContext().ensureActive()
                        val read = from.read(buffer)
                        if (read < 0) break
                        to.write(buffer, 0, read)
                        copied += read
                    }
                    to.flush()
                    val expected = source.length()
                    if (expected > 0 && copied != expected) {
                        throw IOException("The copied file size does not match the source")
                    }
                    val actual = target.length()
                    if (actual > 0 && actual != copied) {
                        throw IOException("The destination file size does not match the copied data")
                    }
                }
            }
        }
        return target
    } catch (error: Exception) {
        target.delete()
        throw error
    }
}

private fun createEntry(
    context: Context,
    state: DualPanelState,
    rawName: String,
    isFolder: Boolean
) {
    val name = validateEntryName(rawName)
    val parent = state.currentDirectory(context)
        ?: throw IOException("The destination folder is not available")
    if (!parent.canWrite()) throw IOException("The destination folder is not writable")
    if (parent.findFile(name) != null) throw IOException("An entry with that name already exists")
    val created = if (isFolder) {
        parent.createDirectory(name)
    } else {
        parent.createFile(mimeTypeFor(name), name)
    }
    if (created == null) throw IOException("The new entry could not be created")
    if (!created.exists()) {
        created.delete()
        throw IOException("The new entry could not be created")
    }
}

private fun renameEntry(
    context: Context,
    state: DualPanelState,
    entry: DocumentFile,
    rawName: String
) {
    val name = validateEntryName(rawName)
    if (name == entry.name) return
    val parent = state.currentDirectory(context)
        ?: throw IOException("The current folder is not available")
    if (!parent.canWrite()) throw IOException("The current folder is not writable")
    if (state.entries.any { it.name == name && it.uri != entry.uri }) {
        throw IOException("An entry with that name already exists")
    }
    if (!entry.renameTo(name)) throw IOException("The entry could not be renamed")
}

private suspend fun deleteEntries(entries: List<DocumentFile>) {
    for (entry in entries) {
        deleteDocument(entry)
    }
}

private suspend fun deleteDocument(entry: DocumentFile) {
    kotlinx.coroutines.currentCoroutineContext().ensureActive()
    if (entry.isDirectory) {
        for (child in entry.listFiles()) {
            kotlinx.coroutines.currentCoroutineContext().ensureActive()
            deleteDocument(child)
        }
    }
    if (!entry.delete()) throw IOException("An entry could not be deleted")
}

private fun validateEntryName(rawName: String): String {
    val name = rawName.trim()
    if (
        name.isEmpty() ||
        name == "." ||
        name == ".." ||
        name.contains('/') ||
        name.contains('\\') ||
        name.any { it.code < 32 || it.code == 127 }
    ) {
        throw IOException("The entry name is invalid")
    }
    return name
}

private fun mimeTypeFor(name: String): String {
    val extension = name.substringAfterLast('.', "")
    return android.webkit.MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(extension.lowercase(Locale.ROOT))
        ?: "application/octet-stream"
}
