package com.webtoapp.ui.shell

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.GalleryDeviceEntry
import com.webtoapp.core.shell.GalleryShellItem
import com.webtoapp.core.shell.readGalleryDeviceDirectory
import com.webtoapp.core.shell.toGalleryShellItem
import com.webtoapp.data.model.GalleryCategory
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.shared.GalleryWatchEntry
import com.webtoapp.ui.shared.GalleryWatchHistory
import com.webtoapp.ui.shared.galleryMatchesSearch
import com.webtoapp.ui.shared.galleryWatchTime
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GalleryFolderNavigator(
    categories: List<GalleryCategory>,
    onBack: () -> Unit,
    latestVideo: () -> GalleryWatchEntry? = { null },
    content: @Composable (categoryId: String?, videoId: String?, back: () -> Unit) -> Unit
) {
    if (categories.isEmpty()) {
        content(null, null, onBack)
        return
    }
    var selectedFolder by rememberSaveable { mutableStateOf<String?>(null) }
    var showContents by rememberSaveable { mutableStateOf(false) }
    var startVideoId by rememberSaveable { mutableStateOf<String?>(null) }
    val backToFolders: () -> Unit = { showContents = false }
    BackHandler(enabled = showContents, onBack = backToFolders)

    if (showContents) {
        key(selectedFolder, startVideoId) { content(selectedFolder, startVideoId, backToFolders) }
    } else {
        Scaffold(topBar = { GalleryLibraryBar(Strings.galleryApp, onBack) }) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                latestVideo()?.let { watched ->
                    item {
                        GalleryContinueWatching(watched) {
                            selectedFolder = null
                            startVideoId = watched.videoId
                            showContents = true
                        }
                    }
                }
                item {
                    GalleryLibraryRow(Strings.all, true) {
                        selectedFolder = null
                        startVideoId = null
                        showContents = true
                    }
                }
                items(categories.sortedBy { it.sortIndex }, key = { it.id }) { category ->
                    GalleryLibraryRow(category.name, true) {
                        selectedFolder = category.id
                        startVideoId = null
                        showContents = true
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GalleryDeviceFolderLibrary(
    libraryKey: String,
    onBack: () -> Unit,
    playback: @Composable (List<GalleryShellItem>, Int, GalleryWatchHistory, List<String>, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val resolver = context.contentResolver
    val prefs = remember(context) { context.getSharedPreferences("gallery_device_folders", Context.MODE_PRIVATE) }
    var tree by remember(libraryKey) { mutableStateOf(prefs.getString(libraryKey, null)?.let(Uri::parse)) }
    var folders by remember(libraryKey) { mutableStateOf<List<GalleryDeviceEntry>>(emptyList()) }
    var revision by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf<FolderListing>(FolderListing.Loading) }
    var playingId by remember { mutableStateOf<String?>(null) }
    var pickerFailed by remember { mutableStateOf(false) }
    var query by rememberSaveable(tree, folders.lastOrNull()?.id) { mutableStateOf("") }
    var pendingVideoId by remember { mutableStateOf<String?>(null) }
    var locating by remember { mutableStateOf(false) }
    var missingRecent by remember { mutableStateOf(false) }
    val history = remember(libraryKey, tree) { GalleryWatchHistory(context, "$libraryKey/device/$tree") }
    val folder = folders.lastOrNull()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { selected ->
        if (selected != null) {
            try {
                resolver.takePersistableUriPermission(selected, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                prefs.edit().putString(libraryKey, selected.toString()).apply()
                tree = selected
                folders = emptyList()
                pendingVideoId = null
                missingRecent = false
                state = FolderListing.Loading
                revision++
                pickerFailed = false
            } catch (error: SecurityException) {
                AppLogger.w("Gallery", "Unable to persist video folder access", error)
                pickerFailed = true
            }
        }
    }

    LaunchedEffect(tree, folder?.id, revision) {
        val root = tree ?: return@LaunchedEffect
        state = FolderListing.Loading
        state = try {
            val entries = if (folder == null) readGalleryDeviceDirectory(resolver, root)
            else readGalleryDeviceDirectory(resolver, root, folder.id)
            val video = pendingVideoId
            if (video != null) {
                pendingVideoId = null
                if (entries.any { it.id == video && !it.isDirectory }) playingId = video
                else missingRecent = true
            }
            FolderListing.Ready(entries)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            AppLogger.w("Gallery", "Unable to list video folder", error)
            FolderListing.Unavailable
        }
    }

    val back: () -> Unit = {
        if (playingId != null) {
            playingId = null
        } else if (folders.isNotEmpty()) {
            state = FolderListing.Loading
            folders = folders.dropLast(1)
        } else {
            onBack()
        }
    }
    BackHandler(enabled = playingId != null || folders.isNotEmpty(), onBack = back)
    val root = tree
    val entries = (state as? FolderListing.Ready)?.entries.orEmpty()
    val selected = playingId
    if (root != null && selected != null) {
        val videos by produceState<List<GalleryShellItem>?>(null, root, entries) {
            value = withContext(Dispatchers.IO) {
                entries.asSequence()
                    .filterNot { it.isDirectory }
                    .mapIndexed { index, entry -> entry.toGalleryShellItem(root, index) }
                    .toList()
            }
        }
        val playlist = videos
        val index = remember(playlist, selected) { playlist?.indexOfFirst { it.id == selected } ?: -1 }
        if (playlist != null && index >= 0) {
            playback(playlist, index, history, folders.map { it.id }, back)
        } else if (playlist != null) {
            LaunchedEffect(selected) { playingId = null }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { WtaLoadingIndicator() }
        }
        return
    }

    val chooseFolder: () -> Unit = { picker.launch(root) }
    val matching = remember(entries, query) { entries.filter { galleryMatchesSearch(it.name, query) } }
    Scaffold(topBar = {
        GalleryLibraryBar(folder?.name ?: Strings.galleryDeviceFolder, back) {
            if (root != null) {
                IconButton(enabled = !locating, onClick = { pickerFailed = false; revision++ }) {
                    Icon(Icons.Default.Refresh, Strings.refresh)
                }
                IconButton(enabled = !locating, onClick = chooseFolder) {
                    Icon(Icons.Default.FolderOpen, Strings.galleryChooseFolder)
                }
            }
        }
    }) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when {
            root == null || pickerFailed || state == FolderListing.Unavailable -> {
                Column(
                    modifier = modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    if (pickerFailed || state == FolderListing.Unavailable) {
                        Text(Strings.galleryFolderUnavailable)
                    }
                    Button(onClick = chooseFolder) { Text(Strings.galleryChooseFolder) }
                }
            }
            state == FolderListing.Loading || locating -> Box(modifier, contentAlignment = Alignment.Center) {
                WtaLoadingIndicator()
            }
            else -> Column(modifier) {
                GallerySearchField(query, { query = it })
                if (folders.isEmpty() && query.isBlank()) {
                    val recent by history.latestEntry.collectAsState()
                    recent?.let { watched ->
                        GalleryContinueWatching(watched) {
                            locating = true
                            missingRecent = false
                            scope.launch {
                                try {
                                    var children = entries
                                    val path = mutableListOf<GalleryDeviceEntry>()
                                    for (id in watched.folderIds) {
                                        val child = children.firstOrNull { it.isDirectory && it.id == id } ?: break
                                        path += child
                                        children = readGalleryDeviceDirectory(resolver, root, child.id)
                                    }
                                    if (path.size == watched.folderIds.size &&
                                        children.any { it.id == watched.videoId && !it.isDirectory }) {
                                        pendingVideoId = watched.videoId
                                        folders = path
                                        revision++
                                    } else {
                                        missingRecent = true
                                    }
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    AppLogger.w("Gallery", "Unable to locate previously watched video", error)
                                    missingRecent = true
                                } finally {
                                    locating = false
                                }
                            }
                        }
                    }
                }
                if (missingRecent) {
                    Text(Strings.galleryResumeUnavailable, modifier = Modifier.padding(16.dp))
                }
                if (matching.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(if (query.isBlank()) Strings.galleryFolderEmpty else Strings.noSearchResults)
                    }
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        items(matching, key = { it.id }) { entry ->
                            GalleryLibraryRow(entry.name, entry.isDirectory) {
                                missingRecent = false
                                if (entry.isDirectory) {
                                    state = FolderListing.Loading
                                    folders = folders + entry
                                } else {
                                    playingId = entry.id
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun GallerySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        label = { Text(Strings.search) },
        singleLine = true
    )
}

@Composable
internal fun GalleryContinueWatching(entry: GalleryWatchEntry, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        leadingContent = { Icon(Icons.Outlined.PlayCircle, null) },
        overlineContent = { Text(Strings.galleryContinueWatching) },
        headlineContent = { Text(entry.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingContent = { Text(galleryWatchTime(entry.positionMs)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryLibraryBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, Strings.back) }
        },
        actions = actions
    )
}

@Composable
private fun GalleryLibraryRow(name: String, folder: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        leadingContent = { Icon(if (folder) Icons.Default.Folder else Icons.Outlined.PlayCircle, null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

private sealed interface FolderListing {
    data object Loading : FolderListing
    data object Unavailable : FolderListing
    data class Ready(val entries: List<GalleryDeviceEntry>) : FolderListing
}
