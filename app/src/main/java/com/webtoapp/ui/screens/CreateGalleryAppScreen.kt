package com.webtoapp.ui.screens

import android.net.Uri
import com.webtoapp.ui.design.WtaSwitch
import com.webtoapp.ui.components.PremiumButton
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.theme.LocalShowDescriptions
import com.webtoapp.ui.theme.ifDescriptionsShown
import com.webtoapp.data.model.*
import com.webtoapp.ui.icons.WtaAppTypeIcon
import com.webtoapp.ui.components.*
import com.webtoapp.ui.screens.create.WtaCreateFlowScaffold
import com.webtoapp.ui.screens.create.WtaCreateFlowSection
import com.webtoapp.ui.shared.GalleryVideoTitle
import com.webtoapp.util.MediaStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaBackground
import com.webtoapp.ui.design.WtaExpandIn
import com.webtoapp.ui.design.WtaShrinkOut
import com.webtoapp.ui.components.EnhancedElevatedCard

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(java.util.Locale.getDefault(), "%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / 1024.0 / 1024.0)
        else -> String.format(java.util.Locale.getDefault(), "%.2f GB", bytes / 1024.0 / 1024.0 / 1024.0)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreateGalleryAppScreenV2(
    existingAppId: Long? = null,
    onBack: () -> Unit,
    onCreated: (
        name: String,
        galleryConfig: GalleryConfig,
        iconUri: Uri?,
        themeType: String
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    var existingApp by remember { mutableStateOf<WebApp?>(null) }
    LaunchedEffect(existingAppId) {
        if (existingAppId != null) {
            existingApp = org.koin.java.KoinJavaComponent.get<com.webtoapp.data.repository.WebAppRepository>(com.webtoapp.data.repository.WebAppRepository::class.java)
                .getWebAppById(existingAppId)
                .first()
        }
    }

    var appName by remember { mutableStateOf("") }
    var appIcon by remember { mutableStateOf<Uri?>(null) }
    var appIconPath by remember { mutableStateOf<String?>(null) }

    var galleryItems by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    var folders by remember { mutableStateOf<List<GalleryCategory>>(emptyList()) }
    var activeFolderId by remember { mutableStateOf<String?>(null) }
    var useDeviceFolder by remember { mutableStateOf(false) }
    var importProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var importJob by remember { mutableStateOf<Job?>(null) }
    var importFailures by remember { mutableStateOf<List<String>>(emptyList()) }
    val draftItems = remember { mutableMapOf<String, GalleryItem>() }
    var submitted by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showFolderNameDialog by remember { mutableStateOf(false) }
    var editingFolder by remember { mutableStateOf<GalleryCategory?>(null) }
    var deletingFolder by remember { mutableStateOf<GalleryCategory?>(null) }
    var moveToNewFolder by remember { mutableStateOf(false) }
    val activeFolder = folders.firstOrNull { it.id == activeFolderId }
    val visibleItems = remember(galleryItems, activeFolderId) {
        if (activeFolderId == null) galleryItems else galleryItems.filter { it.categoryId == activeFolderId }
    }
    val isLoadingMedia = importProgress != null

    var selectedItems by remember { mutableStateOf<Set<String>>(emptySet()) }
    val isSelectionMode = selectedItems.isNotEmpty()

    var loop by remember { mutableStateOf(true) }
    var autoPlay by remember { mutableStateOf(false) }
    var imageInterval by remember { mutableIntStateOf(3) }
    var enableAudio by remember { mutableStateOf(true) }
    var videoAutoNext by remember { mutableStateOf(true) }

    var showAppInfoSection by remember { mutableStateOf(true) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var previewItem by remember { mutableStateOf<GalleryItem?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            importJob?.cancel()
            if (!submitted) draftItems.values.forEach { MediaStorage.deleteGalleryMedia(it) }
        }
    }

    LaunchedEffect(existingApp) {
        existingApp?.let { app ->
            appName = app.name
            app.iconPath?.let { appIconPath = it }
            app.galleryConfig?.let { config ->
                galleryItems = config.items
                folders = config.categories.sortedBy { it.sortIndex }
                useDeviceFolder = config.useDeviceFolder
                loop = config.loop
                autoPlay = config.autoPlay
                imageInterval = config.imageInterval
                enableAudio = config.enableAudio
                videoAutoNext = config.videoAutoNext
            }
        }
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty() && importProgress == null) {
            val targetFolder = activeFolderId
            val firstSortIndex = (galleryItems.maxOfOrNull { it.sortIndex } ?: -1) + 1
            importProgress = 0 to uris.size
            importJob = scope.launch {
                val added = mutableListOf<GalleryItem>()
                val failed = mutableListOf<String>()
                try {
                    uris.forEachIndexed { index, uri ->
                        val result = MediaStorage.importGalleryMedia(context, uri)
                        val item = result.item
                        if (item == null) {
                            failed += result.name
                        } else {
                            val filed = item.copy(categoryId = targetFolder, sortIndex = firstSortIndex + index)
                            added += filed
                            draftItems[filed.id] = filed
                        }
                        importProgress = (index + 1) to uris.size
                    }
                } finally {
                    galleryItems = galleryItems + added
                    importFailures = failed
                    importProgress = null
                }
            }
        }
    }
    val iconPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { appIcon = it } }

    val canCreate = !isLoadingMedia && (existingAppId == null || existingApp != null) &&
        (useDeviceFolder || galleryItems.isNotEmpty())
    val moveSelected: (String?) -> Unit = { destination ->
        galleryItems = galleryItems.map { item ->
            if (item.id in selectedItems) item.copy(categoryId = destination) else item
        }
        selectedItems = emptySet()
        showMoveDialog = false
    }
    val handleBack: () -> Unit = {
        when {
            isLoadingMedia -> importJob?.cancel()
            isSelectionMode -> selectedItems = emptySet()
            else -> onBack()
        }
    }
    BackHandler(enabled = isLoadingMedia || isSelectionMode, onBack = handleBack)

    WtaCreateFlowScaffold(
        title = if (isSelectionMode) "${selectedItems.size} ${Strings.galleryItemCount}" else if (existingAppId != null) Strings.editApp else Strings.galleryCreateTitle,
        onBack = handleBack,
        actions = {
            if (isSelectionMode) {
                IconButton(onClick = {
                    selectedItems = if (selectedItems.size == visibleItems.size) emptySet() else visibleItems.map { it.id }.toSet()
                }) { Icon(Icons.Default.CheckBox, null) }
                IconButton(onClick = { showMoveDialog = true }) {
                    Icon(Icons.Default.Folder, Strings.galleryMoveToFolder)
                }
                IconButton(onClick = {
                    selectedItems.forEach { id ->
                        draftItems.remove(id)?.let(MediaStorage::deleteGalleryMedia)
                    }
                    galleryItems = galleryItems.filterNot { it.id in selectedItems }
                    selectedItems = emptySet()
                }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            } else {
                if (!useDeviceFolder && activeFolder != null && !isLoadingMedia) {
                    IconButton(onClick = { editingFolder = activeFolder; showFolderNameDialog = true }) {
                        Icon(Icons.Default.Edit, Strings.edit)
                    }
                    IconButton(onClick = { deletingFolder = activeFolder }) {
                        Icon(Icons.Default.Delete, Strings.delete)
                    }
                }
                IconButton(onClick = { showSettingsSheet = true }, enabled = !isLoadingMedia) {
                    Icon(Icons.Outlined.Settings, null)
                }
                TextButton(
                    onClick = {
                        val finalIconUri = appIconPath?.let { Uri.parse("file://$it") } ?: appIcon
                        val config = (existingApp?.galleryConfig ?: GalleryConfig()).copy(
                            items = galleryItems,
                            categories = folders,
                            useDeviceFolder = useDeviceFolder,
                            imageInterval = imageInterval,
                            loop = loop,
                            autoPlay = autoPlay,
                            enableAudio = enableAudio,
                            videoAutoNext = videoAutoNext
                        )
                        submitted = true
                        onCreated(appName.ifBlank { Strings.galleryApp }, config, finalIconUri, "AURORA")
                    },
                    enabled = canCreate
                ) { Text(if (existingAppId != null) Strings.btnSave else Strings.btnCreate) }
            }
        },
        floatingActionButton = {
            if (!isSelectionMode && !useDeviceFolder && !isLoadingMedia) {
                ExtendedFloatingActionButton(
                    onClick = { mediaPickerLauncher.launch(arrayOf("image/*", "video/*")) },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(Strings.galleryAddMedia) }
                )
            }
        },
        contentScrollEnabled = false
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            WtaCreateFlowSection(title = Strings.importProject) {
            if (existingAppId == null) {
            AnimatedVisibility(visible = showAppInfoSection, enter = WtaExpandIn, exit = WtaShrinkOut) {
                Surface(color = if (com.webtoapp.ui.theme.LocalIsDarkTheme.current) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.72f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer).clickable { iconPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            val iconToShow = appIconPath ?: appIcon?.toString()
                            if (iconToShow != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(if (appIconPath != null) File(appIconPath!!) else appIcon).crossfade(true).build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                WtaAppTypeIcon(
                                    type = AppType.GALLERY,
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        PremiumTextField(
                            value = appName,
                            onValueChange = { appName = it },
                            placeholder = { Text(Strings.galleryApp) },
                            modifier = Modifier.weight(weight = 1f, fill = true),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            Surface(modifier = Modifier.fillMaxWidth().clickable { showAppInfoSection = !showAppInfoSection }, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                Icon(if (showAppInfoSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, modifier = Modifier.fillMaxWidth().padding(4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            }

            if (!useDeviceFolder && galleryItems.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatChip(icon = Icons.Outlined.Image, value = galleryItems.count { it.type == GalleryItemType.IMAGE })
                        StatChip(icon = Icons.Outlined.Videocam, value = galleryItems.count { it.type == GalleryItemType.VIDEO })
                    }
                    Text(text = formatFileSize(galleryItems.sumOf { it.fileSize }), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            }

            GallerySourceSetting(
                useDeviceFolder = useDeviceFolder,
                enabled = !isLoadingMedia && !isSelectionMode,
                onSourceChange = { useDeviceFolder = it }
            )
            AnimatedVisibility(visible = !useDeviceFolder, enter = WtaExpandIn, exit = WtaShrinkOut) {
                GalleryFolderStrip(
                    folders = folders,
                    selectedId = activeFolderId,
                    enabled = !isSelectionMode && !isLoadingMedia,
                    onSelect = { activeFolderId = it },
                    onNewFolder = { editingFolder = null; showFolderNameDialog = true }
                )
            }

            if (!useDeviceFolder) {
            WtaCreateFlowSection(title = Strings.preview, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.weight(weight = 1f, fill = true)) {
                val progress = importProgress
                if (progress != null) {
                    Column(
                        Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("${progress.first} / ${progress.second}")
                        LinearProgressIndicator(progress = { progress.first.toFloat() / progress.second })
                        TextButton(onClick = { importJob?.cancel() }) { Text(Strings.cancel) }
                    }
                } else if (visibleItems.isEmpty()) {
                    EmptyGalleryState(onAddMedia = { mediaPickerLauncher.launch(arrayOf("image/*", "video/*")) })
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(visibleItems, key = { it.id }) { item ->
                            GalleryMediaItem(
                                item = item,
                                isSelected = item.id in selectedItems,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedItems = if (item.id in selectedItems) selectedItems - item.id else selectedItems + item.id
                                    } else {
                                        previewItem = item
                                    }
                                },
                                onLongClick = { selectedItems = setOf(item.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
            }
            }
        }
    }

    if (showMoveDialog) {
        GalleryMoveDialog(
            folders = folders,
            onDismiss = { showMoveDialog = false },
            onMove = moveSelected,
            onNewFolder = {
                showMoveDialog = false
                moveToNewFolder = true
                editingFolder = null
                showFolderNameDialog = true
            }
        )
    }
    if (showFolderNameDialog) {
        GalleryFolderNameDialog(
            folder = editingFolder,
            folders = folders,
            onDismiss = { moveToNewFolder = false; showFolderNameDialog = false },
            onSave = { name ->
                val previous = editingFolder
                if (previous != null) {
                    folders = folders.map { if (it.id == previous.id) it.copy(name = name) else it }
                } else {
                    val folder = GalleryCategory(name = name, sortIndex = (folders.maxOfOrNull { it.sortIndex } ?: -1) + 1)
                    folders = folders + folder
                    if (moveToNewFolder) moveSelected(folder.id)
                    activeFolderId = folder.id
                }
                moveToNewFolder = false
                showFolderNameDialog = false
            }
        )
    }
    deletingFolder?.let { folder ->
        WtaAlertDialog(
            title = folder.name,
            onDismissRequest = { deletingFolder = null },
            dismissButton = { TextButton(onClick = { deletingFolder = null }) { Text(Strings.cancel) } },
            confirmButton = {
                TextButton(onClick = {
                    folders = folders.filterNot { it.id == folder.id }
                    galleryItems = galleryItems.map { if (it.categoryId == folder.id) it.copy(categoryId = null) else it }
                    activeFolderId = null
                    deletingFolder = null
                }) { Text(Strings.delete) }
            }
        )
    }
    if (importFailures.isNotEmpty()) {
        WtaAlertDialog(
            title = Strings.failed,
            onDismissRequest = { importFailures = emptyList() },
            confirmButton = { TextButton(onClick = { importFailures = emptyList() }) { Text(Strings.confirm) } },
            content = {
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp).fillMaxWidth()) {
                    items(importFailures) { name ->
                        Text(name, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        )
    }

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            GallerySettingsSheet(
                loop = loop, onLoopChange = { loop = it },
                autoPlay = autoPlay, onAutoPlayChange = { autoPlay = it },
                imageInterval = imageInterval, onImageIntervalChange = { imageInterval = it },
                enableAudio = enableAudio, onEnableAudioChange = { enableAudio = it },
                videoAutoNext = videoAutoNext, onVideoAutoNextChange = { videoAutoNext = it },
                onDismiss = { showSettingsSheet = false }
            )
        }
    }

    previewItem?.let { item ->
        MediaPreviewDialog(
            item = item,
            onDismiss = { previewItem = null },
            onDelete = {
                draftItems.remove(item.id)?.let(MediaStorage::deleteGalleryMedia)
                galleryItems = galleryItems.filter { it.id != item.id }
                previewItem = null
            }
        )
    }
}

@Composable
private fun StatChip(icon: ImageVector, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text = value.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyGalleryState(onAddMedia: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().clickable(onClick = onAddMedia).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.AddPhotoAlternate, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = Strings.galleryClickToAdd, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (LocalShowDescriptions.current) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = Strings.gallerySupportTypes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryMediaItem(
    item: GalleryItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(File(item.thumbnailPath ?: item.path)).crossfade(true).build(),
            contentDescription = item.name.takeUnless { item.type == GalleryItemType.VIDEO },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        if (item.type == GalleryItemType.VIDEO) {
            Box(Modifier.align(Alignment.TopStart).padding(end = if (isSelectionMode) 36.dp else 0.dp)) {
                GalleryVideoTitle(item.name)
            }
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(32.dp).background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))
                )
            )
            if (item.duration > 0) {
                Text(item.formattedDuration, modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
            Icon(Icons.Default.PlayCircle, null, modifier = Modifier.align(Alignment.Center).size(36.dp), tint = Color.White.copy(alpha = 0.9f))
        }
        if (isSelectionMode) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(24.dp).clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f))
                    .border(width = 2.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White) }
            }
        }
        if (isSelected) {
            Box(modifier = Modifier.fillMaxSize().border(width = 3.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)))
        }
    }
}

@Composable
private fun GallerySettingsSheet(
    loop: Boolean, onLoopChange: (Boolean) -> Unit,
    autoPlay: Boolean, onAutoPlayChange: (Boolean) -> Unit,
    imageInterval: Int, onImageIntervalChange: (Int) -> Unit,
    enableAudio: Boolean, onEnableAudioChange: (Boolean) -> Unit,
    videoAutoNext: Boolean, onVideoAutoNextChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingsSection(title = Strings.galleryGeneralSettings) {
            SettingsSwitch(title = Strings.loopPlay, subtitle = Strings.galleryLoopHint.ifDescriptionsShown(), checked = loop, onCheckedChange = onLoopChange)
            SettingsSwitch(title = Strings.galleryAutoPlay, subtitle = Strings.galleryAutoPlayHint.ifDescriptionsShown(), checked = autoPlay, onCheckedChange = onAutoPlayChange)
        }
        SettingsSection(title = Strings.galleryImageSettings) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(Strings.galleryImageInterval, style = MaterialTheme.typography.bodyMedium)
                    Text("${imageInterval}s", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(value = imageInterval.toFloat(), onValueChange = { onImageIntervalChange(it.toInt()) }, valueRange = 1f..15f, steps = 13, modifier = Modifier.fillMaxWidth())
            }
        }
        SettingsSection(title = Strings.galleryVideoSettings) {
            SettingsSwitch(title = Strings.enableAudio, subtitle = Strings.galleryEnableAudioHint.ifDescriptionsShown(), checked = enableAudio, onCheckedChange = onEnableAudioChange)
            SettingsSwitch(title = Strings.galleryVideoAutoNext, subtitle = Strings.galleryVideoAutoNextHint.ifDescriptionsShown(), checked = videoAutoNext, onCheckedChange = onVideoAutoNextChange)
        }
        Spacer(modifier = Modifier.height(16.dp))
        PremiumButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(Strings.btnConfirm) }
    }
}

@Composable
private fun MediaPreviewDialog(item: GalleryItem, onDismiss: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    WtaAlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        content = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp))) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(File(item.thumbnailPath ?: item.path)).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    if (item.type == GalleryItemType.VIDEO) {
                        Icon(Icons.Default.PlayCircle, null, modifier = Modifier.size(64.dp).align(Alignment.Center), tint = Color.White.copy(alpha = 0.9f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoChip(label = if (item.type == GalleryItemType.IMAGE) Strings.image else Strings.video, icon = if (item.type == GalleryItemType.IMAGE) Icons.Outlined.Image else Icons.Outlined.Videocam)
                    if (item.duration > 0) { InfoChip(label = item.formattedDuration, icon = Icons.Outlined.Timer) }
                    InfoChip(label = item.formattedFileSize, icon = Icons.Outlined.Storage)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(Strings.btnOk) } },
        dismissButton = { TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text(Strings.delete) } }
    )
}
