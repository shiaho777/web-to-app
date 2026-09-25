package com.webtoapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.extension.ChromeExtensionParser
import com.webtoapp.core.extension.ExtensionFileManager
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.plugin.*
import com.webtoapp.ui.components.PremiumButton
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.design.*
import com.webtoapp.ui.plugin.kindLabel
import com.webtoapp.ui.plugin.pluginIcon
import kotlinx.coroutines.launch

/**
 * Unified plugin management: HCJ packages, userscripts and Chrome extensions
 * in one list. Kind is a badge, not a tab — the user picked this app for
 * plugins, not for taxonomies.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginManagerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToMarket: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { PluginStore.getInstance(context) }
    val importer = remember { PluginImporter(context) }
    val extensionFileManager = remember { ExtensionFileManager(context) }

    val installed by store.plugins.collectAsStateWithLifecycle()
    val builtIns by store.builtInPlugins.collectAsStateWithLifecycle()
    val hiddenBuiltIns by store.hiddenBuiltIns.collectAsStateWithLifecycle()
    val isLoading by store.isLoading.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var styleTarget by remember { mutableStateOf<Plugin?>(null) }
    var pendingDelete by remember { mutableStateOf<Plugin?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    var chromePreview by remember { mutableStateOf<ChromeExtensionParser.ParseResult?>(null) }

    fun matches(p: Plugin): Boolean =
        searchQuery.isBlank() ||
            p.name.contains(searchQuery, ignoreCase = true) ||
            p.description.contains(searchQuery, ignoreCase = true)

    suspend fun importFrom(uri: Uri) {
        val name = importer.fileNameFor(uri)?.lowercase().orEmpty()
        when {
            name.endsWith(".user.js") || name.endsWith(".js") -> {
                when (val r = importer.importUserScript(uri)) {
                    is PluginImporter.ImportResult.Success ->
                        Toast.makeText(context, Strings.pluginImportSuccess(r.plugin.name), Toast.LENGTH_SHORT).show()
                    is PluginImporter.ImportResult.Error ->
                        Toast.makeText(context, Strings.moduleImportFailed(r.message), Toast.LENGTH_SHORT).show()
                }
            }
            name.endsWith(".crx") -> {
                when (val r = extensionFileManager.importChromeExtension(uri)) {
                    is ExtensionFileManager.ImportResult.ChromeExtension -> chromePreview = r.parseResult
                    is ExtensionFileManager.ImportResult.Error ->
                        Toast.makeText(context, Strings.moduleImportFailed(r.message), Toast.LENGTH_SHORT).show()
                    else -> {}
                }
            }
            else -> {
                // .hcj / .zip / unknown: HCJ package first, Chrome zip as fallback.
                when (val r = importer.importHcj(uri)) {
                    is PluginImporter.ImportResult.Success ->
                        Toast.makeText(context, Strings.pluginImportSuccess(r.plugin.name), Toast.LENGTH_SHORT).show()
                    is PluginImporter.ImportResult.Error -> {
                        when (val cr = extensionFileManager.importChromeExtension(uri)) {
                            is ExtensionFileManager.ImportResult.ChromeExtension -> chromePreview = cr.parseResult
                            is ExtensionFileManager.ImportResult.Error ->
                                Toast.makeText(context, Strings.moduleImportFailed(r.message), Toast.LENGTH_SHORT).show()
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            isImporting = true
            try {
                importFrom(uri)
            } finally {
                isImporting = false
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(Strings.pluginsTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.back)
                    }
                },
                actions = {
                    IconButton(onClick = { picker.launch("*/*") }) {
                        Icon(Icons.Outlined.FileOpen, contentDescription = Strings.pluginImport)
                    }
                    IconButton(onClick = { onNavigateToEditor(null) }) {
                        Icon(Icons.Outlined.Add, contentDescription = Strings.pluginNew)
                    }
                    IconButton(onClick = { onNavigateToMarket() }) {
                        Icon(Icons.Outlined.Storefront, contentDescription = Strings.communityExtStoreTitle)
                    }
                    if (hiddenBuiltIns.isNotEmpty()) {
                        IconButton(onClick = { scope.launch { store.restoreBuiltIns() } }) {
                            Icon(Icons.Outlined.Restore, contentDescription = Strings.pluginRestoreBuiltIns)
                        }
                    }
                }
            )
        }
    ) { padding ->
        WtaBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                PremiumTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(Strings.searchPlugins) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = Strings.clear)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(WtaRadius.Button)
                )

                val shownInstalled = installed.filter(::matches)
                val shownBuiltIns = builtIns.filter(::matches)

                // Working copies while a drag is in flight; store order wins
                // whenever no drag is active.
                var installedWorking by remember { mutableStateOf(shownInstalled) }
                var builtinWorking by remember { mutableStateOf(shownBuiltIns) }
                val dragState = remember { PluginDragState() }
                LaunchedEffect(shownInstalled) {
                    if (dragState.itemKey == null) installedWorking = shownInstalled
                }
                LaunchedEffect(shownBuiltIns) {
                    if (dragState.itemKey == null) builtinWorking = shownBuiltIns
                }
                val spacingPx = with(LocalDensity.current) { 10.dp.toPx() }
                val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                val latestInstalled = rememberUpdatedState(installedWorking)
                val latestBuiltIns = rememberUpdatedState(builtinWorking)

                fun persistOrder(builtIn: Boolean) {
                    val ids = (if (builtIn) latestBuiltIns else latestInstalled).value.map { it.id }
                    scope.launch { store.reorder(ids, builtIn) }
                }

                if (!isLoading && shownInstalled.isEmpty() && shownBuiltIns.isEmpty()) {
                    WtaFullEmptyState(
                        icon = Icons.Outlined.Extension,
                        title = Strings.pluginsEmpty,
                        message = Strings.pluginsEmptyHint,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (installedWorking.isNotEmpty()) {
                            item(key = "hdr_installed") {
                                PluginSectionHeader(Strings.pluginSectionInstalled)
                            }
                            items(installedWorking, key = { "i:" + it.id }) { plugin ->
                                val itemKey = "i:" + plugin.id
                                PluginRow(
                                    plugin = plugin,
                                    isDragging = dragState.itemKey == itemKey,
                                    modifier = Modifier
                                        .animateItem()
                                        .pluginReorderable(
                                            itemKey = itemKey,
                                            itemId = plugin.id,
                                            dragState = dragState,
                                            spacingPx = spacingPx,
                                            haptic = haptic,
                                            currentList = { latestInstalled.value },
                                            onReorder = { installedWorking = it },
                                            onPersist = { persistOrder(builtIn = false) },
                                            onCancel = { installedWorking = shownInstalled }
                                        ),
                                    onEdit = if (plugin.isScriptPlugin) {
                                        { onNavigateToEditor(plugin.id) }
                                    } else null,
                                    onStyle = { styleTarget = plugin },
                                    onExport = if (plugin.isScriptPlugin) {
                                        {
                                            scope.launch {
                                                importer.exportHcj(plugin)?.let { shareHcj(context, it) }
                                            }
                                        }
                                    } else null,
                                    onDelete = { pendingDelete = plugin }
                                )
                            }
                        }
                        if (builtinWorking.isNotEmpty()) {
                            item(key = "hdr_builtin") {
                                PluginSectionHeader(Strings.pluginSectionBuiltIn)
                            }
                            items(builtinWorking, key = { "b:" + it.id }) { plugin ->
                                val itemKey = "b:" + plugin.id
                                PluginRow(
                                    plugin = plugin,
                                    isDragging = dragState.itemKey == itemKey,
                                    modifier = Modifier
                                        .animateItem()
                                        .pluginReorderable(
                                            itemKey = itemKey,
                                            itemId = plugin.id,
                                            dragState = dragState,
                                            spacingPx = spacingPx,
                                            haptic = haptic,
                                            currentList = { latestBuiltIns.value },
                                            onReorder = { builtinWorking = it },
                                            onPersist = { persistOrder(builtIn = true) },
                                            onCancel = { builtinWorking = shownBuiltIns }
                                        ),
                                    onEdit = { onNavigateToEditor(plugin.id) },
                                    onStyle = { styleTarget = plugin },
                                    onExport = {
                                        scope.launch {
                                            importer.exportHcj(plugin)?.let { shareHcj(context, it) }
                                        }
                                    },
                                    onDelete = { pendingDelete = plugin }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isImporting) {
        WtaAlertDialog(
            onDismissRequest = {},
            title = Strings.loading,
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(Strings.importing)
                }
            },
            confirmButton = {}
        )
    }

    styleTarget?.let { target ->
        var entry by remember(target.id) { mutableStateOf(target.entryStyle) }
        var panel by remember(target.id) { mutableStateOf(target.panelStyle) }
        WtaAlertDialog(
            onDismissRequest = { styleTarget = null },
            title = Strings.pluginHostStyle,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(Strings.pluginEntryStyle, style = MaterialTheme.typography.labelLarge)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            PluginEntryStyle.TOOLBAR to Strings.entryStyleToolbar,
                            PluginEntryStyle.MENU to Strings.entryStyleMenu,
                            PluginEntryStyle.FLOATING_HANDLE to Strings.entryStyleFloating
                        ).forEach { (style, label) ->
                            WtaButton(
                                onClick = {
                                    entry = style
                                    scope.launch { store.setPluginStyle(target.id, entry, panel) }
                                },
                                text = label,
                                variant = if (entry == style) WtaButtonVariant.Primary else WtaButtonVariant.Tonal,
                                size = WtaButtonSize.Small,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Text(Strings.pluginPanelStyleLabel, style = MaterialTheme.typography.labelLarge)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            PluginPanelStyle.BOTTOM_SHEET to Strings.panelStyleSheet,
                            PluginPanelStyle.FLOATING_WINDOW to Strings.panelStyleWindow,
                            PluginPanelStyle.FULLSCREEN to Strings.panelStyleFullscreen
                        ).forEach { (style, label) ->
                            WtaButton(
                                onClick = {
                                    panel = style
                                    scope.launch { store.setPluginStyle(target.id, entry, panel) }
                                },
                                text = label,
                                variant = if (panel == style) WtaButtonVariant.Primary else WtaButtonVariant.Tonal,
                                size = WtaButtonSize.Small,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { styleTarget = null }) { Text(Strings.confirm) }
            }
        )
    }

    pendingDelete?.let { plugin ->
        WtaAlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = Strings.pluginDeleteConfirmTitle,
            text = plugin.name,
            confirmButton = {
                PremiumButton(onClick = {
                    scope.launch {
                        if (plugin.builtIn) store.hideBuiltIn(plugin.id) else store.removePlugin(plugin.id)
                    }
                    pendingDelete = null
                }) { Text(Strings.delete) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(Strings.btnCancel) }
            }
        )
    }

    chromePreview?.let { parseResult ->
        WtaAlertDialog(
            onDismissRequest = { chromePreview = null },
            title = Strings.installChromeExtension,
            content = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(parseResult.extensionName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("v${parseResult.extensionVersion}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (parseResult.extensionDescription.isNotBlank()) {
                        Text(parseResult.extensionDescription, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                    Text("${Strings.contentScripts}: ${parseResult.modules.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    if (parseResult.unsupportedPermissions.isNotEmpty()) {
                        Text(
                            "${Strings.unsupportedApis}: ${parseResult.unsupportedPermissions.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                PremiumButton(onClick = {
                    scope.launch {
                        when (val r = importer.installChromeRecords(parseResult.modules)) {
                            is PluginImporter.ImportResult.Success ->
                                Toast.makeText(context, Strings.pluginImportSuccess(parseResult.extensionName), Toast.LENGTH_SHORT).show()
                            is PluginImporter.ImportResult.Error ->
                                Toast.makeText(context, Strings.moduleImportFailed(r.message), Toast.LENGTH_SHORT).show()
                        }
                        chromePreview = null
                    }
                }) { Text(Strings.install) }
            },
            dismissButton = {
                TextButton(onClick = { chromePreview = null }) { Text(Strings.btnCancel) }
            }
        )
    }
}

@Composable
private fun PluginSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp, start = 4.dp)
    )
}

/** Shared long-press drag state for the plugin lists. */
private class PluginDragState {
    var itemKey by mutableStateOf<String?>(null)
    var offsetPx by mutableFloatStateOf(0f)
    var rowHeightPx by mutableIntStateOf(0)
}

/** Long-press then drag vertically to reorder within one list section. */
private fun Modifier.pluginReorderable(
    itemKey: String,
    itemId: String,
    dragState: PluginDragState,
    spacingPx: Float,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    currentList: () -> List<Plugin>,
    onReorder: (List<Plugin>) -> Unit,
    onPersist: () -> Unit,
    onCancel: () -> Unit
): Modifier = this
    .zIndex(if (dragState.itemKey == itemKey) 1f else 0f)
    .graphicsLayer {
        val dragging = dragState.itemKey == itemKey
        translationY = if (dragging) dragState.offsetPx else 0f
        scaleX = if (dragging) 1.02f else 1f
        scaleY = if (dragging) 1.02f else 1f
        shadowElevation = if (dragging) 8.dp.toPx() else 0f
    }
    .onSizeChanged { dragState.rowHeightPx = it.height }
    .pointerInput(itemKey) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                haptic.performHapticFeedback(
                    androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                )
                dragState.itemKey = itemKey
                dragState.offsetPx = 0f
            },
            onDragEnd = {
                onPersist()
                dragState.itemKey = null
                dragState.offsetPx = 0f
            },
            onDragCancel = {
                onCancel()
                dragState.itemKey = null
                dragState.offsetPx = 0f
            }
        ) { change, amount ->
            change.consume()
            val rowH = dragState.rowHeightPx
            if (rowH <= 0) return@detectDragGesturesAfterLongPress
            val rowAndGap = rowH + spacingPx
            dragState.offsetPx += amount.y
            val moved = (dragState.offsetPx / rowAndGap).toInt()
            if (moved != 0) {
                val list = currentList()
                val cur = list.indexOfFirst { it.id == itemId }
                val target = (cur + moved).coerceIn(0, list.lastIndex)
                if (cur >= 0 && target != cur) {
                    val m = list.toMutableList()
                    m.add(target, m.removeAt(cur))
                    onReorder(m)
                    dragState.offsetPx -= moved * rowAndGap
                }
            }
            val idx = currentList().indexOfFirst { it.id == itemId }
            val clamp = rowAndGap * 0.5f
            if (idx == 0 && dragState.offsetPx < -clamp) dragState.offsetPx = -clamp
            if (idx == currentList().lastIndex && dragState.offsetPx > clamp) dragState.offsetPx = clamp
        }
    }

@Composable
private fun PluginRow(
    plugin: Plugin,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)?,
    onStyle: (() -> Unit)?,
    onExport: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    WtaCard(
        contentPadding = PaddingValues(12.dp),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(WtaRadius.Control))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    pluginIcon(plugin.icon),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        plugin.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            kindLabel(plugin.kind),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                if (plugin.description.isNotBlank()) {
                    Text(
                        plugin.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (onEdit != null || onStyle != null || onExport != null || onDelete != null) {
                Box {
                    var rowMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { rowMenu = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = Strings.more, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = rowMenu, onDismissRequest = { rowMenu = false }) {
                        onEdit?.let { edit ->
                            DropdownMenuItem(
                                text = { Text(Strings.edit) },
                                onClick = { rowMenu = false; edit() },
                                leadingIcon = { Icon(Icons.Default.Edit, null, Modifier.size(20.dp)) }
                            )
                        }
                        onStyle?.let { style ->
                            DropdownMenuItem(
                                text = { Text(Strings.pluginHostStyle) },
                                onClick = { rowMenu = false; style() },
                                leadingIcon = { Icon(Icons.Default.Tune, null, Modifier.size(20.dp)) }
                            )
                        }
                        onExport?.let { export ->
                            DropdownMenuItem(
                                text = { Text(Strings.pluginExportHcj) },
                                onClick = { rowMenu = false; export() },
                                leadingIcon = { Icon(Icons.Default.Share, null, Modifier.size(20.dp)) }
                            )
                        }
                        onDelete?.let { del ->
                            WtaDivider()
                            DropdownMenuItem(
                                text = { Text(Strings.delete, color = MaterialTheme.colorScheme.error) },
                                onClick = { rowMenu = false; del() },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun shareHcj(context: android.content.Context, file: java.io.File) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newRawUri(file.name, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = android.content.Intent.createChooser(intent, file.name)
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, e.message ?: "share failed", Toast.LENGTH_SHORT).show()
    }
}
