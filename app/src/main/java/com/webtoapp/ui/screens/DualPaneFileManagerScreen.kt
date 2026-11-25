package com.webtoapp.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.files.DualPaneDocument
import com.webtoapp.core.files.DualPaneDocumentOps
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.DateFormat
import java.util.Date

private const val DUAL_PANE_PREFERENCES = "dual_pane_file_manager"

private data class PaneState(
    val treeUri: String? = null,
    val path: List<String> = emptyList(),
    val query: String = "",
    val selected: Set<String> = emptySet()
)

private data class NameDialogState(
    val leftPane: Boolean,
    val renameUri: Uri? = null
)

private data class TextEditorState(
    val leftPane: Boolean,
    val document: DualPaneDocument
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DualPaneFileManagerScreen(
    onOpenApps: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val preferences = remember(context) {
        context.getSharedPreferences(DUAL_PANE_PREFERENCES, Context.MODE_PRIVATE)
    }

    var leftPane by remember { mutableStateOf(PaneState()) }
    var rightPane by remember { mutableStateOf(PaneState()) }
    var leftItems by remember { mutableStateOf<List<DualPaneDocument>>(emptyList()) }
    var rightItems by remember { mutableStateOf<List<DualPaneDocument>>(emptyList()) }
    var leftLoading by remember { mutableStateOf(false) }
    var rightLoading by remember { mutableStateOf(false) }
    var leftError by remember { mutableStateOf<String?>(null) }
    var rightError by remember { mutableStateOf<String?>(null) }
    var split by rememberSaveable { mutableFloatStateOf(0.5f) }
    var nameDialog by remember { mutableStateOf<NameDialogState?>(null) }
    var nameValue by rememberSaveable { mutableStateOf("") }
    var deletePane by remember { mutableStateOf<Boolean?>(null) }
    var editor by remember { mutableStateOf<TextEditorState?>(null) }
    var editorText by remember { mutableStateOf("") }
    var comparison by remember { mutableStateOf<String?>(null) }
    var operationBusy by remember { mutableStateOf(false) }
    var movePane by remember { mutableStateOf<Boolean?>(null) }

    suspend fun refreshPane(left: Boolean) {
        val pane = if (left) leftPane else rightPane
        val tree = pane.treeUri?.let(Uri::parse)
        if (tree == null) {
            if (left) leftItems = emptyList() else rightItems = emptyList()
            return
        }
        if (left) {
            leftLoading = true
            leftError = null
        } else {
            rightLoading = true
            rightError = null
        }
        try {
            val result = withContext(Dispatchers.IO) {
                DualPaneDocumentOps.list(context, tree, pane.path)
            }
            val availableUris = result.map { it.uri.toString() }.toSet()
            if (left) {
                leftItems = result
                leftPane = leftPane.copy(selected = leftPane.selected.intersect(availableUris))
            } else {
                rightItems = result
                rightPane = rightPane.copy(selected = rightPane.selected.intersect(availableUris))
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (left) {
                leftError = error.message ?: error.javaClass.simpleName
                leftItems = emptyList()
            } else {
                rightError = error.message ?: error.javaClass.simpleName
                rightItems = emptyList()
            }
        } finally {
            if (left) leftLoading = false else rightLoading = false
        }
    }

    fun reload(left: Boolean) {
        scope.launch { refreshPane(left) }
    }

    fun showFailure(error: Throwable) {
        val details = error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
        scope.launch {
            snackbarHostState.showSnackbar(
                Strings.dualPaneOperationFailed.replace("%s", details)
            )
        }
    }

    fun chooseFolder(left: Boolean, uri: Uri?) {
        if (uri == null) return
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (error: SecurityException) {
            showFailure(error)
            return
        }
        val updated = PaneState(treeUri = uri.toString())
        if (left) {
            leftPane = updated
            preferences.edit().putString("left_tree", uri.toString()).apply()
        } else {
            rightPane = updated
            preferences.edit().putString("right_tree", uri.toString()).apply()
        }
    }

    val leftFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> chooseFolder(true, uri) }
    val rightFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> chooseFolder(false, uri) }

    LaunchedEffect(preferences) {
        leftPane = leftPane.copy(treeUri = preferences.getString("left_tree", null))
        rightPane = rightPane.copy(treeUri = preferences.getString("right_tree", null))
    }

    LaunchedEffect(leftPane.treeUri, leftPane.path) {
        refreshPane(true)
    }
    LaunchedEffect(rightPane.treeUri, rightPane.path) {
        refreshPane(false)
    }

    fun updatePane(left: Boolean, transform: (PaneState) -> PaneState) {
        if (left) leftPane = transform(leftPane) else rightPane = transform(rightPane)
    }

    fun selectAll(left: Boolean) {
        val pane = if (left) leftPane else rightPane
        val documents = if (left) leftItems else rightItems
        val selected = documents
            .filter { pane.query.isBlank() || it.name.contains(pane.query, ignoreCase = true) }
            .map { it.uri.toString() }
            .toSet()
        updatePane(left) { it.copy(selected = selected) }
    }

    fun launchNameDialog(left: Boolean, renameUri: Uri? = null) {
        val pane = if (left) leftPane else rightPane
        val selected = if (left) leftItems else rightItems
        val initialName = renameUri?.let { uri -> selected.firstOrNull { it.uri == uri }?.name }.orEmpty()
        nameValue = initialName
        nameDialog = NameDialogState(left, renameUri)
    }

    fun executeTransfer(left: Boolean, move: Boolean) {
        val source = if (left) leftPane else rightPane
        val target = if (left) rightPane else leftPane
        val selected = source.selected.map(Uri::parse).toSet()
        val sourceTree = source.treeUri?.let(Uri::parse)
        val targetTree = target.treeUri?.let(Uri::parse)
        if (sourceTree == null || targetTree == null) {
            scope.launch { snackbarHostState.showSnackbar(Strings.dualPaneChooseFolder) }
            return
        }
        scope.launch {
            operationBusy = true
            try {
                withContext(Dispatchers.IO) {
                    DualPaneDocumentOps.transfer(
                        context = context,
                        sourceTreeUri = sourceTree,
                        sourcePath = source.path,
                        selectedUris = selected,
                        targetTreeUri = targetTree,
                        targetPath = target.path,
                        move = move
                    )
                }
                updatePane(left) { it.copy(selected = emptySet()) }
                refreshPane(left)
                refreshPane(!left)
                snackbarHostState.showSnackbar(Strings.dualPaneTransferComplete)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                showFailure(error)
                refreshPane(left)
                refreshPane(!left)
            } finally {
                operationBusy = false
            }
        }
    }

    fun startTransfer(left: Boolean, move: Boolean) {
        if (move) {
            movePane = left
        } else {
            executeTransfer(left, false)
        }
    }

    fun openDocument(left: Boolean, document: DualPaneDocument) {
        val pane = if (left) leftPane else rightPane
        if (pane.selected.isNotEmpty()) {
            updatePane(left) {
                val next = it.selected.toMutableSet()
                if (!next.add(document.uri.toString())) next.remove(document.uri.toString())
                it.copy(selected = next)
            }
            return
        }
        if (document.isDirectory) {
            updatePane(left) {
                it.copy(path = it.path + document.name, selected = emptySet())
            }
            return
        }
        if (DualPaneDocumentOps.isTextDocument(document)) {
            scope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        DualPaneDocumentOps.readText(
                            context,
                            Uri.parse(pane.treeUri),
                            pane.path,
                            document.uri
                        )
                    }
                    editor = TextEditorState(left, document)
                    editorText = content
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    showFailure(error)
                }
            }
        } else {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setDataAndType(document.uri, document.mimeType ?: "*/*")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            } catch (error: ActivityNotFoundException) {
                showFailure(error)
            } catch (error: Exception) {
                showFailure(error)
            }
        }
    }

    fun compareSelectedFiles() {
        val first = leftPane.selected.singleOrNull()?.let(Uri::parse)
        val second = rightPane.selected.singleOrNull()?.let(Uri::parse)
        val firstTree = leftPane.treeUri?.let(Uri::parse)
        val secondTree = rightPane.treeUri?.let(Uri::parse)
        if (first == null || second == null || firstTree == null || secondTree == null) {
            showFailure(IOException(Strings.dualPaneCompareSelect))
            return
        }
        scope.launch {
            operationBusy = true
            try {
                val result = withContext(Dispatchers.IO) {
                    DualPaneDocumentOps.compare(
                        context = context,
                        firstTreeUri = firstTree,
                        firstPath = leftPane.path,
                        firstUri = first,
                        secondTreeUri = secondTree,
                        secondPath = rightPane.path,
                        secondUri = second
                    )
                }
                comparison = buildString {
                    append(if (result.sameContent) Strings.dualPaneCompareSame else Strings.dualPaneCompareDifferent)
                    append("\n")
                    append("${leftItems.firstOrNull { it.uri == first }?.name.orEmpty()} — ${result.firstDigest}")
                    append("\n")
                    append("${rightItems.firstOrNull { it.uri == second }?.name.orEmpty()} — ${result.secondDigest}")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                showFailure(error)
            } finally {
                operationBusy = false
            }
        }
    }

    WtaScreen(
        title = Strings.dualPaneTitle,
        snackbarHostState = snackbarHostState,
        actions = {
            IconButton(
                onClick = ::compareSelectedFiles,
                enabled = leftPane.selected.size == 1 && rightPane.selected.size == 1 && !operationBusy
            ) {
                Icon(Icons.Outlined.CompareArrows, contentDescription = Strings.dualPaneCompare)
            }
            IconButton(
                onClick = {
                    val oldLeft = leftPane
                    val oldRight = rightPane
                    leftPane = oldRight
                    rightPane = oldLeft
                    val oldLeftItems = leftItems
                    leftItems = rightItems
                    rightItems = oldLeftItems
                    val oldLeftTree = preferences.getString("left_tree", null)
                    preferences.edit()
                        .putString("left_tree", preferences.getString("right_tree", null))
                        .putString("right_tree", oldLeftTree)
                        .apply()
                },
                enabled = !operationBusy
            ) {
                Icon(Icons.Outlined.SwapHoriz, contentDescription = Strings.dualPaneTitle)
            }
            IconButton(onClick = onOpenApps) {
                Icon(Icons.Outlined.Apps, contentDescription = Strings.dualPaneOpenApps)
            }
        }
    ) { _ ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val wide = maxWidth >= 700.dp
            val density = LocalDensity.current
            val splitExtentPx = with(density) { (if (wide) maxWidth else maxHeight).toPx() }
            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    PaneContent(
                        pane = leftPane,
                        documents = leftItems,
                        loading = leftLoading,
                        error = leftError,
                        leftPane = true,
                        modifier = Modifier.weight(split).fillMaxHeight(),
                        onChooseFolder = { leftFolderPicker.launch(null) },
                        onRefresh = { reload(true) },
                        onCreateFolder = { launchNameDialog(true) },
                        onRename = { uri -> launchNameDialog(true, uri) },
                        onDelete = { deletePane = true },
                        onSelectAll = { selectAll(true) },
                        onTransferCopy = { startTransfer(true, false) },
                        onTransferMove = { startTransfer(true, true) },
                        onNavigate = { segments -> updatePane(true) { it.copy(path = segments, selected = emptySet()) } },
                        onOpenDocument = { openDocument(true, it) },
                        onToggleSelection = { uri ->
                            updatePane(true) {
                                val next = it.selected.toMutableSet()
                                if (!next.add(uri.toString())) next.remove(uri.toString())
                                it.copy(selected = next)
                            }
                        },
                        onQueryChange = { query -> updatePane(true) { it.copy(query = query) } },
                        onClearSelection = { updatePane(true) { it.copy(selected = emptySet()) } }
                    )
                    SplitHandle(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(12.dp)
                            .pointerInput(wide, splitExtentPx) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    split = (split + dragAmount.x / splitExtentPx).coerceIn(0.25f, 0.75f)
                                }
                            }
                    )
                    PaneContent(
                        pane = rightPane,
                        documents = rightItems,
                        loading = rightLoading,
                        error = rightError,
                        leftPane = false,
                        modifier = Modifier.weight(1f - split).fillMaxHeight(),
                        onChooseFolder = { rightFolderPicker.launch(null) },
                        onRefresh = { reload(false) },
                        onCreateFolder = { launchNameDialog(false) },
                        onRename = { uri -> launchNameDialog(false, uri) },
                        onDelete = { deletePane = false },
                        onSelectAll = { selectAll(false) },
                        onTransferCopy = { startTransfer(false, false) },
                        onTransferMove = { startTransfer(false, true) },
                        onNavigate = { segments -> updatePane(false) { it.copy(path = segments, selected = emptySet()) } },
                        onOpenDocument = { openDocument(false, it) },
                        onToggleSelection = { uri ->
                            updatePane(false) {
                                val next = it.selected.toMutableSet()
                                if (!next.add(uri.toString())) next.remove(uri.toString())
                                it.copy(selected = next)
                            }
                        },
                        onQueryChange = { query -> updatePane(false) { it.copy(query = query) } },
                        onClearSelection = { updatePane(false) { it.copy(selected = emptySet()) } }
                    )
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    PaneContent(
                        pane = leftPane,
                        documents = leftItems,
                        loading = leftLoading,
                        error = leftError,
                        leftPane = true,
                        modifier = Modifier.weight(split).fillMaxWidth(),
                        onChooseFolder = { leftFolderPicker.launch(null) },
                        onRefresh = { reload(true) },
                        onCreateFolder = { launchNameDialog(true) },
                        onRename = { uri -> launchNameDialog(true, uri) },
                        onDelete = { deletePane = true },
                        onSelectAll = { selectAll(true) },
                        onTransferCopy = { startTransfer(true, false) },
                        onTransferMove = { startTransfer(true, true) },
                        onNavigate = { segments -> updatePane(true) { it.copy(path = segments, selected = emptySet()) } },
                        onOpenDocument = { openDocument(true, it) },
                        onToggleSelection = { uri ->
                            updatePane(true) {
                                val next = it.selected.toMutableSet()
                                if (!next.add(uri.toString())) next.remove(uri.toString())
                                it.copy(selected = next)
                            }
                        },
                        onQueryChange = { query -> updatePane(true) { it.copy(query = query) } },
                        onClearSelection = { updatePane(true) { it.copy(selected = emptySet()) } }
                    )
                    SplitHandle(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .pointerInput(wide, splitExtentPx) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    split = (split + dragAmount.y / splitExtentPx).coerceIn(0.25f, 0.75f)
                                }
                            }
                    )
                    PaneContent(
                        pane = rightPane,
                        documents = rightItems,
                        loading = rightLoading,
                        error = rightError,
                        leftPane = false,
                        modifier = Modifier.weight(1f - split).fillMaxWidth(),
                        onChooseFolder = { rightFolderPicker.launch(null) },
                        onRefresh = { reload(false) },
                        onCreateFolder = { launchNameDialog(false) },
                        onRename = { uri -> launchNameDialog(false, uri) },
                        onDelete = { deletePane = false },
                        onSelectAll = { selectAll(false) },
                        onTransferCopy = { startTransfer(false, false) },
                        onTransferMove = { startTransfer(false, true) },
                        onNavigate = { segments -> updatePane(false) { it.copy(path = segments, selected = emptySet()) } },
                        onOpenDocument = { openDocument(false, it) },
                        onToggleSelection = { uri ->
                            updatePane(false) {
                                val next = it.selected.toMutableSet()
                                if (!next.add(uri.toString())) next.remove(uri.toString())
                                it.copy(selected = next)
                            }
                        },
                        onQueryChange = { query -> updatePane(false) { it.copy(query = query) } },
                        onClearSelection = { updatePane(false) { it.copy(selected = emptySet()) } }
                    )
                }
            }
        }
    }

    nameDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { nameDialog = null },
            title = {
                Text(if (dialog.renameUri == null) Strings.dualPaneNewFolder else Strings.dualPaneRename)
            },
            text = {
                OutlinedTextField(
                    value = nameValue,
                    onValueChange = { nameValue = it },
                    singleLine = true,
                    label = { Text(Strings.dualPaneNameHint) }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = nameValue.isNotBlank() && !operationBusy,
                    onClick = {
                        val pane = if (dialog.leftPane) leftPane else rightPane
                        val tree = pane.treeUri?.let(Uri::parse)
                        if (tree == null) return@TextButton
                        scope.launch {
                            operationBusy = true
                            try {
                                withContext(Dispatchers.IO) {
                                    if (dialog.renameUri == null) {
                                        DualPaneDocumentOps.createFolder(context, tree, pane.path, nameValue)
                                    } else {
                                        DualPaneDocumentOps.rename(
                                            context,
                                            tree,
                                            pane.path,
                                            dialog.renameUri,
                                            nameValue
                                        )
                                    }
                                }
                                updatePane(dialog.leftPane) { it.copy(selected = emptySet()) }
                                nameDialog = null
                                refreshPane(dialog.leftPane)
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                showFailure(error)
                            } finally {
                                operationBusy = false
                            }
                        }
                    }
                ) { Text(Strings.save) }
            },
            dismissButton = {
                TextButton(onClick = { nameDialog = null }) { Text(Strings.cancel) }
            }
        )
    }

    deletePane?.let { left ->
        val pane = if (left) leftPane else rightPane
        AlertDialog(
            onDismissRequest = { deletePane = null },
            text = { Text(Strings.dualPaneDeleteConfirm) },
            confirmButton = {
                TextButton(
                    enabled = pane.selected.isNotEmpty() && !operationBusy,
                    onClick = {
                        val tree = pane.treeUri?.let(Uri::parse) ?: return@TextButton
                        val selected = pane.selected.map(Uri::parse).toSet()
                        scope.launch {
                            operationBusy = true
                            try {
                                withContext(Dispatchers.IO) {
                                    DualPaneDocumentOps.delete(context, tree, pane.path, selected)
                                }
                                updatePane(left) { it.copy(selected = emptySet()) }
                                deletePane = null
                                refreshPane(left)
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                showFailure(error)
                                refreshPane(left)
                            } finally {
                                operationBusy = false
                            }
                        }
                    }
                ) { Text(Strings.btnDelete) }
            },
            dismissButton = {
                TextButton(onClick = { deletePane = null }) { Text(Strings.cancel) }
            }
        )
    }

    movePane?.let { left ->
        AlertDialog(
            onDismissRequest = { movePane = null },
            text = { Text(Strings.dualPaneMoveConfirm) },
            confirmButton = {
                TextButton(
                    enabled = !operationBusy,
                    onClick = {
                        movePane = null
                        executeTransfer(left, true)
                    }
                ) { Text(Strings.dualPaneMoveToOther) }
            },
            dismissButton = {
                TextButton(onClick = { movePane = null }) { Text(Strings.cancel) }
            }
        )
    }

    editor?.let { currentEditor ->
        AlertDialog(
            onDismissRequest = { editor = null },
            title = {
                Text(
                    text = currentEditor.document.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                OutlinedTextField(
                    value = editorText,
                    onValueChange = { editorText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 480.dp),
                    minLines = 8,
                    maxLines = 20
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !operationBusy,
                    onClick = {
                        val pane = if (currentEditor.leftPane) leftPane else rightPane
                        val tree = pane.treeUri?.let(Uri::parse) ?: return@TextButton
                        scope.launch {
                            operationBusy = true
                            try {
                                withContext(Dispatchers.IO) {
                                    DualPaneDocumentOps.writeTextWithBackup(
                                        context,
                                        tree,
                                        pane.path,
                                        currentEditor.document.uri,
                                        editorText
                                    )
                                }
                                editor = null
                                snackbarHostState.showSnackbar(Strings.dualPaneEditSaved)
                                refreshPane(currentEditor.leftPane)
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                showFailure(error)
                            } finally {
                                operationBusy = false
                            }
                        }
                    }
                ) { Text(Strings.save) }
            },
            dismissButton = {
                TextButton(onClick = { editor = null }) { Text(Strings.cancel) }
            }
        )
    }

    comparison?.let { result ->
        AlertDialog(
            onDismissRequest = { comparison = null },
            title = { Text(Strings.dualPaneCompare) },
            text = { Text(result) },
            confirmButton = {
                TextButton(onClick = { comparison = null }) { Text(Strings.confirm) }
            }
        )
    }

}

@Composable
private fun PaneContent(
    pane: PaneState,
    documents: List<DualPaneDocument>,
    loading: Boolean,
    error: String?,
    leftPane: Boolean,
    modifier: Modifier = Modifier,
    onChooseFolder: () -> Unit,
    onRefresh: () -> Unit,
    onCreateFolder: () -> Unit,
    onRename: (Uri) -> Unit,
    onDelete: () -> Unit,
    onSelectAll: () -> Unit,
    onTransferCopy: () -> Unit,
    onTransferMove: () -> Unit,
    onNavigate: (List<String>) -> Unit,
    onOpenDocument: (DualPaneDocument) -> Unit,
    onToggleSelection: (Uri) -> Unit,
    onQueryChange: (String) -> Unit,
    onClearSelection: () -> Unit
) {
    val paneTitle = if (leftPane) Strings.dualPaneLeft else Strings.dualPaneRight
    val filteredDocuments = remember(documents, pane.query) {
        if (pane.query.isBlank()) documents
        else documents.filter { it.name.contains(pane.query, ignoreCase = true) }
    }
    var showActions by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.padding(3.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 4.dp, top = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = paneTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onChooseFolder) {
                    Icon(Icons.Outlined.FolderOpen, contentDescription = Strings.dualPaneChooseFolder)
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = Strings.refresh)
                }
                IconButton(onClick = onCreateFolder, enabled = pane.treeUri != null) {
                    Icon(Icons.Outlined.CreateNewFolder, contentDescription = Strings.dualPaneNewFolder)
                }
                Box {
                    IconButton(onClick = { showActions = true }, enabled = pane.treeUri != null) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = Strings.more)
                    }
                    DropdownMenu(
                        expanded = showActions,
                        onDismissRequest = { showActions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(Strings.selectAll) },
                            leadingIcon = { Icon(Icons.Outlined.CheckBox, contentDescription = null) },
                            onClick = { showActions = false; onSelectAll() }
                        )
                        DropdownMenuItem(
                            text = { Text(Strings.dualPaneRename) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            enabled = pane.selected.size == 1,
                            onClick = {
                                showActions = false
                                pane.selected.singleOrNull()?.let { onRename(Uri.parse(it)) }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(Strings.dualPaneCopyToOther) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null) },
                            enabled = pane.selected.isNotEmpty(),
                            onClick = { showActions = false; onTransferCopy() }
                        )
                        DropdownMenuItem(
                            text = { Text(Strings.dualPaneMoveToOther) },
                            leadingIcon = { Icon(Icons.Outlined.FolderOpen, contentDescription = null) },
                            enabled = pane.selected.isNotEmpty(),
                            onClick = { showActions = false; onTransferMove() }
                        )
                        DropdownMenuItem(
                            text = { Text(Strings.btnDelete) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                            enabled = pane.selected.isNotEmpty(),
                            onClick = { showActions = false; onDelete() }
                        )
                    }
                }
            }

            if (pane.treeUri == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = Strings.dualPaneRootHelp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    TextButton(onClick = onChooseFolder) {
                        Text(Strings.dualPaneChooseFolder)
                    }
                }
                return@Surface
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onNavigate(emptyList()) }) {
                    Text("⌂")
                }
                pane.path.forEachIndexed { index, segment ->
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { onNavigate(pane.path.take(index + 1)) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = segment,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            OutlinedTextField(
                value = pane.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                singleLine = true,
                placeholder = { Text(Strings.fileManagerSearchHint) },
                leadingIcon = {
                    Icon(Icons.Outlined.TextSnippet, contentDescription = Strings.search)
                },
                trailingIcon = if (pane.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = Strings.cancel)
                        }
                    }
                } else null
            )
            if (pane.selected.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pane.selected.size} · ${Strings.fileManagerSelect}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onSelectAll) {
                        Icon(Icons.Outlined.CheckBox, contentDescription = Strings.selectAll)
                    }
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Outlined.Close, contentDescription = Strings.cancel)
                    }
                }
            }
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 6.dp)
            ) {
                items(filteredDocuments, key = { it.uri.toString() }) { document ->
                    val selected = document.uri.toString() in pane.selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                else Color.Transparent
                            )
                            .combinedClickable(
                                onClick = {
                                    if (pane.selected.isNotEmpty()) onToggleSelection(document.uri)
                                    else onOpenDocument(document)
                                },
                                onLongClick = { onToggleSelection(document.uri) }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (document.isDirectory) Icons.Outlined.Folder
                            else Icons.AutoMirrored.Rounded.InsertDriveFile,
                            contentDescription = null,
                            tint = if (document.isDirectory) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text(
                                text = document.name,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!document.isDirectory) {
                                Text(
                                    text = "${formatFileSize(document.size)} · ${formatDate(document.lastModified)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (selected) {
                            Icon(
                                Icons.Outlined.CheckBox,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                if (filteredDocuments.isEmpty() && !loading && error == null) {
                    item {
                        Text(
                            text = if (pane.query.isBlank()) Strings.fileManagerEmpty else Strings.fileManagerNoMatch,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(4.dp)
                .height(34.dp),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        ) {}
    }
}

private fun formatFileSize(size: Long): String = when {
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> "%.1f KB".format(size / 1024f)
    size < 1024L * 1024L * 1024L -> "%.1f MB".format(size / (1024f * 1024f))
    else -> "%.1f GB".format(size / (1024f * 1024f * 1024f))
}

private fun formatDate(timestamp: Long): String =
    if (timestamp <= 0L) "" else DateFormat.getDateInstance(DateFormat.SHORT).format(Date(timestamp))
