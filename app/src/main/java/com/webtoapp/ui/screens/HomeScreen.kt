package com.webtoapp.ui.screens
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.ui.components.PremiumButton
import com.webtoapp.ui.components.SettingsSwitch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Announcement
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.graphics.shapes.RoundedPolygon
import com.webtoapp.R
import com.webtoapp.core.host.HostRuntimePrefs
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.apkbuilder.ExportRuntimeEnsure
import com.webtoapp.core.apkbuilder.BuildResult
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.dao.WebAppSummary
import com.webtoapp.data.model.AppCategory
import com.webtoapp.data.model.withRuntimePermissionsSyncedFromFeatures
import com.webtoapp.data.model.WebApp
import com.webtoapp.ui.components.CategoryEditorDialog
import com.webtoapp.ui.components.CategoryTabRow
import com.webtoapp.ui.components.DataBackupDialog
import com.webtoapp.ui.components.EnhancedElevatedCard
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.components.LanguageSelectorButton
import com.webtoapp.ui.components.MoveToCategoryDialog
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.icons.WtaAppTypeIcon
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.design.WtaMaterialShape
import com.webtoapp.ui.design.WtaMorphingShape
import com.webtoapp.ui.design.WtaShapePlate
import com.webtoapp.ui.design.WtaPolygonShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.rememberHapticClick
import com.webtoapp.ui.design.wtaPressScale
import androidx.compose.ui.text.style.TextAlign
import com.webtoapp.ui.theme.LocalAnimationSettings
import com.webtoapp.ui.theme.AppColors
import com.webtoapp.ui.theme.ThemeManager
import com.webtoapp.ui.theme.LocalAppTheme
import com.webtoapp.ui.viewmodel.MainViewModel
import com.webtoapp.ui.viewmodel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex
import com.webtoapp.data.home.AppListSort
import com.webtoapp.data.home.HomeDragSlot
import com.webtoapp.data.home.stepCustomDrag
import com.webtoapp.ui.design.WtaBadge
import com.webtoapp.ui.theme.LocalShowDescriptions

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenCreateGallery: () -> Unit,
    onEditApp: (WebApp) -> Unit,
    onEditAppCore: (WebApp) -> Unit = {},
    onPreviewApp: (WebAppSummary) -> Unit,
    onOpenAppModifier: () -> Unit = {},
    onOpenAiSettings: () -> Unit = {},
    onOpenAgent: () -> Unit = {},
    onOpenPlugins: () -> Unit = {},
    onOpenLinuxEnvironment: () -> Unit = {},
    onOpenBrowserKernel: () -> Unit = {},
    onOpenHostsAdBlock: () -> Unit = {},
    onOpenRuntimeDeps: () -> Unit = {},
    onOpenPortManager: () -> Unit = {},
    onOpenStats: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenMore: () -> Unit = {},
    onOpenPlayStore: () -> Unit = {},
    onExportAabForApp: (Long) -> Unit = {},
    onOpenFileManager: () -> Unit = {},
    onBuildApk: (Long) -> Unit = {},
) {
    val apps by viewModel.filteredSummaries.collectAsStateWithLifecycle()
    val allWebApps by viewModel.webApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val appListSort by viewModel.appListSort.collectAsStateWithLifecycle()
    val reorderEnabled = searchQuery.isBlank()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    var showCategoryEditor by remember { mutableStateOf(false) }
    var showCategoryManager by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<AppCategory?>(null) }
    var showMoveToCategoryDialog by remember { mutableStateOf(false) }
    var appToMove by remember { mutableStateOf<WebAppSummary?>(null) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var appToClearCache by remember { mutableStateOf<WebAppSummary?>(null) }

    var isSearchActive by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<WebAppSummary?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var shareApkFailureReport by remember { mutableStateOf<BuildFailureReport?>(null) }
    var showDataBackupDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val homeThemeManager = remember { ThemeManager.getInstance(context) }
    val homeViewMode by homeThemeManager.homeViewModeFlow.collectAsStateWithLifecycle()
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar((uiState as UiState.Success).message)
                viewModel.resetUiState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as UiState.Error).message)
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    val advancedFeatures by HostRuntimePrefs.getInstance(context)
        .advancedFeaturesFlow
        .collectAsStateWithLifecycle()

    WtaScreen(
        title = Strings.myApps,
        titleContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WtaShapePlate(
                    polygon = MaterialShapes.Cookie7Sided,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = Strings.myApps,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenCreateGallery,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(Strings.createApp, style = MaterialTheme.typography.labelLargeEmphasized) },
                shape = CreateFabShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },

        actions = {
                    LanguageSelectorButton(
                        buttonSize = 40.dp,
                        onLanguageChanged = {
                            scope.launch {
                                snackbarHostState.showSnackbar(Strings.msgLanguageChanged)
                            }
                        }
                    )

                    IconButton(
                        onClick = {
                            val next = if (homeViewMode == ThemeManager.HomeViewMode.LIST) {
                                ThemeManager.HomeViewMode.GRID
                            } else {
                                ThemeManager.HomeViewMode.LIST
                            }
                            scope.launch { homeThemeManager.setHomeViewMode(next) }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Crossfade(
                            targetState = homeViewMode,
                            animationSpec = com.webtoapp.ui.design.WtaMotion.standardTween(),
                            label = "homeViewModeIcon"
                        ) { mode ->
                            Icon(
                                imageVector = if (mode == ThemeManager.HomeViewMode.GRID) {
                                    Icons.AutoMirrored.Outlined.ViewList
                                } else {
                                    Icons.Outlined.GridView
                                },
                                contentDescription = if (mode == ThemeManager.HomeViewMode.GRID) {
                                    Strings.homeViewList
                                } else {
                                    Strings.homeViewGrid
                                },
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) viewModel.search("")
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = Strings.search,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    var showMoreMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = Strings.more,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(Strings.menuAgent) },
                                onClick = { showMoreMenu = false; onOpenAgent() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.SmartToy, MaterialShapes.Sunny, 0) }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.menuAiSettings) },
                                onClick = { showMoreMenu = false; onOpenAiSettings() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.AutoAwesome, MaterialShapes.Cookie4Sided, 2) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(Strings.pluginsManageTitle) },
                                onClick = { showMoreMenu = false; onOpenPlugins() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.Extension, MaterialShapes.Clover4Leaf, 1) }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.menuAppModifier) },
                                onClick = { showMoreMenu = false; onOpenAppModifier() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.AppShortcut, MaterialShapes.Gem, 0) }
                            )
                            if (advancedFeatures) {
                                DropdownMenuItem(
                                    text = { Text(Strings.menuLinuxEnvironment) },
                                    onClick = { showMoreMenu = false; onOpenLinuxEnvironment() },
                                    leadingIcon = { MoreMenuIcon(Icons.Rounded.Terminal, MaterialShapes.Fan, 1) }
                                )
                                DropdownMenuItem(
                                    text = { Text(Strings.menuRuntimeDeps) },
                                    onClick = { showMoreMenu = false; onOpenRuntimeDeps() },
                                    leadingIcon = { MoreMenuIcon(Icons.Rounded.Memory, MaterialShapes.Cookie6Sided, 2) }
                                )
                                DropdownMenuItem(
                                    text = { Text(Strings.menuPortManager) },
                                    onClick = { showMoreMenu = false; onOpenPortManager() },
                                    leadingIcon = { MoreMenuIcon(Icons.Rounded.Router, MaterialShapes.Pentagon, 0) }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(Strings.menuBrowserKernel) },
                                onClick = { showMoreMenu = false; onOpenBrowserKernel() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.Public, MaterialShapes.Cookie9Sided, 1) }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.menuHostsAdBlock) },
                                onClick = { showMoreMenu = false; onOpenHostsAdBlock() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.Shield, MaterialShapes.Arch, 2) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(Strings.menuStats) },
                                onClick = { showMoreMenu = false; onOpenStats() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.Analytics, MaterialShapes.Sunny, 0) }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.menuGooglePlay) },
                                onClick = { showMoreMenu = false; onOpenPlayStore() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.Shop, MaterialShapes.Triangle, 1) }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.menuFileManager) },
                                onClick = { showMoreMenu = false; onOpenFileManager() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.Folder, MaterialShapes.Pill, 2) }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.dataBackupTitle) },
                                onClick = { showMoreMenu = false; showDataBackupDialog = true },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.Backup, MaterialShapes.Ghostish, 0) }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.uiConfig) },
                                onClick = { showMoreMenu = false; onOpenSettings() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.Palette, MaterialShapes.VerySunny, 1) }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.menuAbout) },
                                onClick = { showMoreMenu = false; onOpenAbout() },
                                leadingIcon = { MoreMenuIcon(Icons.Rounded.Info, MaterialShapes.Clover4Leaf, 2) }
                            )
                        }
                    }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            AnimatedVisibility(visible = isSearchActive) {
                DockedSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.search(it) },
                    onSearch = { viewModel.search(it) },
                    active = false,
                    onActiveChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(Strings.search) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.search("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                ) {}
            }

            CategoryTabRow(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { viewModel.selectCategory(it) },
                onAddCategory = {
                    editingCategory = null
                    showCategoryEditor = true
                },
                onEditCategory = { category ->
                    editingCategory = category
                    showCategoryEditor = true
                },
                onDeleteCategory = { viewModel.deleteCategory(it) },
                onMoveCategory = { category, delta -> viewModel.moveCategory(category, delta) },
                onManageCategories = { showCategoryManager = true },
                trailingContent = {
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sort,
                                contentDescription = Strings.appListSort,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            AppListSort.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.homeMenuLabel()) },
                                    onClick = {
                                        showSortMenu = false
                                        viewModel.setAppListSort(mode)
                                    },
                                    leadingIcon = {
                                        if (appListSort == mode) {
                                            Icon(
                                                Icons.Outlined.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Spacer(Modifier.size(18.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = true)
            ) {
                if (apps.isEmpty()) {

                    EmptyState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {

                    val listContext = LocalContext.current
                    val sharedExporter = remember { com.webtoapp.core.export.AppExporter(listContext) }
                    val sharedApkBuilder = remember { lazy { ApkBuilder(listContext) } }
                    val sharedScope = rememberCoroutineScope()

                    val healthMonitor: com.webtoapp.core.stats.AppHealthMonitor? = remember {
                        try { org.koin.java.KoinJavaComponent.get(com.webtoapp.core.stats.AppHealthMonitor::class.java) }
                        catch (e: Exception) { null }
                    }
                    val healthRecordsState = healthMonitor?.allHealthRecords?.collectAsState(initial = emptyList<com.webtoapp.core.stats.AppHealthRecord>())
                    val healthRecords: List<com.webtoapp.core.stats.AppHealthRecord> = healthRecordsState?.value ?: emptyList()
                    val healthMap = remember(healthRecords) { healthRecords.associateBy { it.appId } }

                    LaunchedEffect(apps.map { it.id }) {
                        val monitor = healthMonitor ?: return@LaunchedEffect
                        delay(800)
                        val ids = apps.map { it.id }
                        if (ids.isEmpty()) return@LaunchedEffect
                        withContext(Dispatchers.IO) {
                            val webApps = ids.mapNotNull { viewModel.getWebApp(it) }
                            if (webApps.isNotEmpty()) {
                                monitor.checkApps(webApps)
                            }
                        }
                    }

                    fun resolveScreenshotService(): com.webtoapp.core.stats.WebsiteScreenshotService? {
                        return try {
                            org.koin.java.KoinJavaComponent.get(com.webtoapp.core.stats.WebsiteScreenshotService::class.java)
                        } catch (e: Exception) {
                            val resolveMessage = "service resolve failed: ${e.message}"
                            com.webtoapp.core.logging.AppLogger.w("ScreenshotFlow", resolveMessage)
                            android.util.Log.w("ScreenshotFlow", resolveMessage, e)
                            null
                        }
                    }
                    val screenshotService: com.webtoapp.core.stats.WebsiteScreenshotService? = resolveScreenshotService()

                    val previewImageLoader = remember(listContext) {
                        coil.Coil.imageLoader(listContext)
                    }

                    val screenshotVersions = remember { mutableStateMapOf<Long, Int>() }
                    val screenshotLoadingStates = remember { mutableStateMapOf<Long, Boolean>() }
                    val autoCaptureAttempted = remember { mutableSetOf<Long>() }

                    val runCapture: (WebAppSummary, AppPreviewSpec) -> Unit = { cardApp, spec ->
                        val resolvedService = screenshotService ?: resolveScreenshotService()
                        if (resolvedService == null) {
                            val unavailableMessage = "capture aborted: service unavailable, appId=${cardApp.id}, name=${cardApp.name}"
                            com.webtoapp.core.logging.AppLogger.i("ScreenshotFlow", unavailableMessage)
                            android.util.Log.i("ScreenshotFlow", unavailableMessage)
                        } else {
                            sharedScope.launch {
                                screenshotLoadingStates[cardApp.id] = true
                                try {
                                    val fullApp = viewModel.getWebApp(cardApp.id)
                                    if (fullApp != null) {
                                        com.webtoapp.ui.screens.captureAppThumbnail(
                                            context = listContext.applicationContext,
                                            screenshotService = resolvedService,
                                            app = fullApp,
                                            spec = spec,
                                        )
                                    } else {
                                        spec.captureUrl?.let { resolvedService.captureScreenshot(cardApp.id, it) }
                                    }
                                } catch (e: Exception) {
                                    val errorMessage = "capture exception: appId=${cardApp.id}, error=${e.message}"
                                    com.webtoapp.core.logging.AppLogger.e("ScreenshotFlow", errorMessage, e)
                                    android.util.Log.e("ScreenshotFlow", errorMessage, e)
                                } finally {
                                    screenshotLoadingStates[cardApp.id] = false
                                    screenshotVersions[cardApp.id] = (screenshotVersions[cardApp.id] ?: 0) + 1
                                }
                            }
                        }
                    }

                    val previewSpecs = remember { mutableStateMapOf<Long, AppPreviewSpec>() }
                    LaunchedEffect(apps.map { it.id }, listContext) {
                        val currentIds = apps.map { it.id }.toHashSet()
                        val stale = previewSpecs.keys - currentIds
                        if (stale.isNotEmpty()) {
                            stale.forEach { previewSpecs.remove(it) }
                        }
                        val missingIds = apps.mapNotNull { summary ->
                            summary.id.takeIf { it !in previewSpecs }
                        }
                        if (missingIds.isEmpty()) {
                            return@LaunchedEffect
                        }
                        val resolved = withContext(Dispatchers.IO) {
                            missingIds.mapNotNull { id ->
                                val webApp = viewModel.getWebApp(id) ?: return@mapNotNull null
                                id to resolveAppPreviewSpec(listContext.applicationContext, webApp)
                            }
                        }
                        resolved.forEach { (id, spec) ->
                            previewSpecs[id] = spec
                        }
                    }



                    fun actionsFor(app: WebAppSummary): HomeAppActions = HomeAppActions(
                        onEdit = {
                            scope.launch {
                                viewModel.getWebApp(app.id)?.let(onEditApp)
                            }
                        },
                        onEditCore = {
                            scope.launch {
                                viewModel.getWebApp(app.id)?.let(onEditAppCore)
                            }
                        },
                        onDelete = {
                            selectedApp = app
                            showDeleteDialog = true
                        },
                        onCreateShortcut = {
                            scope.launch {
                                val fullApp = viewModel.getWebApp(app.id) ?: return@launch
                                when (val result = sharedExporter.createShortcut(fullApp)) {
                                    is com.webtoapp.core.export.ShortcutResult.Success -> {
                                        snackbarHostState.showSnackbar(Strings.shortcutCreatedSuccess)
                                    }
                                    is com.webtoapp.core.export.ShortcutResult.Pending -> {
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is com.webtoapp.core.export.ShortcutResult.PermissionRequired -> {
                                        snackbarHostState.showSnackbar(
                                            message = result.message,
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                    is com.webtoapp.core.export.ShortcutResult.Error -> {
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                }
                            }
                        },
                        onExport = {
                            scope.launch {
                                val fullApp = viewModel.getWebApp(app.id) ?: return@launch
                                val packed = withContext(Dispatchers.IO) {
                                    runCatching {
                                        com.webtoapp.core.export.AppSourcePackager(listContext).pack(fullApp)
                                    }
                                }
                                val file = packed.getOrNull()
                                val shared = file != null &&
                                    com.webtoapp.core.export.AppSourcePackager(listContext).share(file)
                                if (!shared) {
                                    packed.exceptionOrNull()?.let {
                                        AppLogger.e("HomeScreen", "Source export failed", it)
                                    }
                                    snackbarHostState.showSnackbar(Strings.exportAppSourceFailed)
                                }
                            }
                        },
                        onBuildApk = {
                            onBuildApk(app.id)
                        },
                        onShareApk = {
                            scope.launch {
                                val fullApp = viewModel.getWebApp(app.id) ?: return@launch
                                shareApkFailureReport = null
                                snackbarHostState.showSnackbar(Strings.shareApkBuilding)
                                val apkBuilder = withContext(Dispatchers.IO) { sharedApkBuilder.value }
                                val result = apkBuilder.buildApk(fullApp) { _, _ -> }
                                when (result) {
                                    is BuildResult.Success -> {
                                        snackbarHostState.showSnackbar(
                                            Strings.shareApkReadyMode.replace(
                                                "%s",
                                                incrementalBuildModeLabel(result.buildMode)
                                            )
                                        )
                                        try {
                                            val apkUri = androidx.core.content.FileProvider.getUriForFile(
                                                listContext,
                                                "${listContext.packageName}.fileprovider",
                                                result.apkFile
                                            )
                                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "application/vnd.android.package-archive"
                                                putExtra(android.content.Intent.EXTRA_STREAM, apkUri)
                                                putExtra(android.content.Intent.EXTRA_SUBJECT, Strings.shareApkTitle.replace("%s", fullApp.name))
                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            listContext.startActivity(android.content.Intent.createChooser(shareIntent, Strings.shareApkTitle.replace("%s", fullApp.name)))
                                        } catch (e: Exception) {
                                            shareApkFailureReport = buildActionFailureReport(
                                                title = Strings.apkShareFailed,
                                                stage = "share_apk_intent",
                                                webApp = fullApp,
                                                summary = Strings.shareApkFailed.replace("%s", e.message ?: "Unknown error"),
                                                logPath = result.logPath,
                                                throwable = e,
                                                extraLines = listOf(
                                                    "apkPath=${result.apkFile.absolutePath}",
                                                    "apkSize=${result.apkFile.length()}"
                                                )
                                            )
                                        }
                                    }
                                    is BuildResult.Error -> {
                                        shareApkFailureReport = buildActionFailureReport(
                                            title = Strings.apkShareFailed,
                                            stage = result.diagnostic?.stage?.label ?: "build_apk_for_share",
                                            webApp = fullApp,
                                            summary = Strings.shareApkFailed.replace("%s", result.message),
                                            logPath = result.logPath,
                                            extraLines = buildDiagnosticLines(result)
                                        )
                                    }
                                }
                            }
                        },
                        onMoveToCategory = {
                            appToMove = app
                            showMoveToCategoryDialog = true
                        },
                        onClearCache = {
                            appToClearCache = app
                            showClearCacheDialog = true
                        },
                        onOpenInBrowser = {
                            if (isExternalBrowserUrl(app.url)) {
                                try {
                                    val viewIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(app.url.trim())
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    listContext.startActivity(
                                        Intent.createChooser(viewIntent, Strings.chooseBrowser)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                } catch (e: Exception) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "${Strings.cannotOpenInBrowser}: ${e.message ?: Strings.failed}"
                                        )
                                    }
                                }
                            }
                        }
                    )

                    val listState = rememberLazyListState()
                    val listDrag = remember { HomeListDragState() }
                    val latestApps = rememberUpdatedState(apps)
                    val latestSort = rememberUpdatedState(appListSort)
                    val haptic = LocalHapticFeedback.current
                    val density = LocalDensity.current
                    val displayedApps = listDrag.order ?: apps
                    val showDragHint = reorderEnabled && LocalShowDescriptions.current

                    fun stepDrag() {
                        val id = listDrag.id ?: return
                        val current = listDrag.order ?: return
                        val slots = listState.layoutInfo.visibleItemsInfo.mapNotNull { info ->
                            (info.key as? Long)?.let { key ->
                                HomeDragSlot(id = key, top = info.offset, height = info.size)
                            }
                        }
                        val step = stepCustomDrag(
                            order = current.map { it.id },
                            dragId = id,
                            dragDy = listDrag.dy,
                            slots = slots,
                        ) ?: return
                        val byId = current.associateBy { it.id }
                        listDrag.order = step.order.map { byId.getValue(it) }
                        listDrag.dy = step.dragDy
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    }

                    LaunchedEffect(apps.map { it.id }, listDrag.id) {
                        val pending = listDrag.order ?: return@LaunchedEffect
                        if (listDrag.id != null) return@LaunchedEffect
                        val pendingIds = pending.map { it.id }
                        val currentIds = apps.map { it.id }
                        if (pendingIds == currentIds || pendingIds.toSet() != currentIds.toSet()) {
                            listDrag.order = null
                        }
                    }

                    LaunchedEffect(reorderEnabled) {
                        if (!reorderEnabled) listDrag.clear()
                    }

                    LaunchedEffect(listDrag.tracking) {
                        if (!listDrag.tracking) return@LaunchedEffect
                        val edge = with(density) { 56.dp.toPx() }
                        while (isActive && listDrag.tracking) {
                            val id = listDrag.id
                            val info = listState.layoutInfo
                            val dragged = info.visibleItemsInfo.firstOrNull { it.key == id }
                            if (dragged != null) {
                                val top = dragged.offset + listDrag.dy
                                val bottom = top + dragged.size
                                val topLimit = info.viewportStartOffset + edge
                                val bottomLimit = info.viewportEndOffset - edge
                                val delta = when {
                                    top < topLimit -> (top - topLimit).coerceAtLeast(-36f)
                                    bottom > bottomLimit -> (bottom - bottomLimit).coerceAtMost(36f)
                                    else -> 0f
                                }
                                if (delta != 0f) {
                                    listState.scrollBy(delta)
                                    stepDrag()
                                }
                            }
                            withFrameNanos { }
                        }
                    }

                    if (homeViewMode == ThemeManager.HomeViewMode.GRID) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            state = rememberLazyGridState(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            gridItemsIndexed(
                                displayedApps,
                                key = { _, app -> app.id },
                                contentType = { _, _ -> "AppCard" }
                            ) { _, app ->
                                AppGridTile(
                                    app = app,
                                    onClick = { onPreviewApp(app) },
                                    actions = actionsFor(app),
                                    previewImageLoader = previewImageLoader,
                                    healthStatus = healthMap[app.id]?.status,
                                    modifier = Modifier.fillMaxWidth().animateItem()
                                )
                            }
                        }
                    } else {
                    LazyColumn(
                    state = listState,
                    userScrollEnabled = listDrag.id == null,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (showDragHint) {
                        item(key = "home-sort-hint", contentType = "hint") {
                            Text(
                                text = Strings.appListSortDragHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            )
                        }
                    }
                    itemsIndexed(
                        displayedApps,
                        key = { _, app -> app.id },
                        contentType = { _, _ -> "AppCard" }
                    ) { index, app ->
                        val scope = sharedScope
                        val previewSpec = previewSpecs[app.id] ?: AppPreviewSpec()

                        LaunchedEffect(app.id, previewSpec.captureUrl) {
                            if (previewSpec.captureUrl == null || app.id in autoCaptureAttempted) {
                                return@LaunchedEffect
                            }
                            val svc = screenshotService ?: resolveScreenshotService()
                                ?: return@LaunchedEffect
                            autoCaptureAttempted += app.id
                            if (!svc.hasScreenshot(app.id)) {
                                runCapture(app, previewSpec)
                            }
                        }

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    selectedApp = app
                                    showDeleteDialog = true
                                    false
                                } else false
                            }
                        )

                        val dragged = app.id == listDrag.id
                        SwipeToDismissBox(
                            modifier = Modifier
                                .zIndex(if (dragged) 1f else 0f)
                                .graphicsLayer {
                                    if (app.id == listDrag.id) {
                                        translationY = listDrag.dy
                                        scaleX = 1.02f
                                        scaleY = 1.02f
                                        shadowElevation = 8.dp.toPx()
                                    }
                                }
                                .homeReorderDrag(
                                    enabled = reorderEnabled &&
                                        (listDrag.id == null || listDrag.id == app.id),
                                    onStart = {
                                        if (!listDrag.tracking) {
                                            listDrag.settleJob?.cancel()
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val visible = latestApps.value
                                            listDrag.order = visible
                                            listDrag.dy = 0f
                                            listDrag.id = app.id
                                            listDrag.tracking = true
                                            if (latestSort.value != AppListSort.CUSTOM) {
                                                viewModel.switchToCustomOrder(visible.map { it.id })
                                            }
                                        }
                                    },
                                    onDrag = { delta ->
                                        if (listDrag.id == app.id) {
                                            listDrag.dy += delta
                                            stepDrag()
                                        }
                                    },
                                    onEnd = { velocity ->
                                        if (listDrag.id == app.id) {
                                            listDrag.tracking = false
                                            val finalOrder = listDrag.order
                                            val changed = finalOrder != null &&
                                                finalOrder.map { it.id } != latestApps.value.map { it.id }
                                            if (changed && finalOrder != null) {
                                                viewModel.saveVisibleHomeOrder(finalOrder.map { it.id })
                                            }
                                            listDrag.settleJob = scope.launch {
                                                val anim = Animatable(listDrag.dy)
                                                anim.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = com.webtoapp.ui.design.WtaMotion.bouncySpring(),
                                                    initialVelocity = velocity,
                                                ) {
                                                    if (!listDrag.tracking) listDrag.dy = value
                                                }
                                                if (!listDrag.tracking && listDrag.id == app.id) {
                                                    listDrag.id = null
                                                    if (!changed) listDrag.order = null
                                                }
                                            }
                                        }
                                    },
                                    onCancel = {
                                        if (listDrag.id == app.id) listDrag.clear()
                                    },
                                ),
                            state = dismissState,
                            backgroundContent = {

                                val bgAlpha by animateFloatAsState(
                                    targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1f else 0f,
                                    label = "dismissBgAlpha"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(WtaRadius.Card))
                                        .background(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.15f * bgAlpha)
                                        )
                                        .padding(end = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Outlined.DeleteOutline,
                                        contentDescription = Strings.btnDelete,
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = bgAlpha),
                                        modifier = Modifier
                                            .size(28.dp)
                                            .graphicsLayer {
                                                scaleX = 0.7f + 0.3f * bgAlpha
                                                scaleY = 0.7f + 0.3f * bgAlpha
                                            }
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = listDrag.id == null
                        ) {

                        val actions = actionsFor(app)
                        AppCard(
                            app = app,
                            modifier = if (dragged) {
                                Modifier.animateItem(placementSpec = null)
                            } else {
                                Modifier.animateItem()
                            },
                            onClick = { onPreviewApp(app) },
                            onLongClick = { selectedApp = app },
                            onEdit = actions.onEdit,
                            onEditCore = actions.onEditCore,
                            onDelete = actions.onDelete,
                            onCreateShortcut = actions.onCreateShortcut,
                            onExport = actions.onExport,
                            onBuildApk = actions.onBuildApk,
                            onShareApk = actions.onShareApk,
                            onMoveToCategory = actions.onMoveToCategory,
                            onClearCache = actions.onClearCache,
                            onOpenInBrowser = actions.onOpenInBrowser,
                            healthStatus = healthMap[app.id]?.status,
                            previewImageLoader = previewImageLoader,
                            screenshotPath = if (previewSpec.captureUrl != null) {
                                screenshotService?.let { svc ->
                                    if (svc.hasScreenshot(app.id)) svc.getScreenshotPath(app.id) else null
                                }
                            } else {
                                previewSpec.previewFilePath
                            },
                            screenshotVersion = screenshotVersions[app.id] ?: 0,
                            isScreenshotLoading = screenshotLoadingStates[app.id] == true,
                            onCaptureScreenshot = if (previewSpec.captureUrl != null) {
                                { runCapture(app, previewSpec) }
                            } else null
                        )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                    }
                }
            }

            }
    }

    shareApkFailureReport?.let { report ->
        BuildFailureReportDialog(
            report = report,
            onDismiss = { shareApkFailureReport = null }
        )
    }

    if (showDeleteDialog && selectedApp != null) {
        WtaAlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedApp = null
            },
            title = Strings.deleteConfirmTitle,
            text = Strings.deleteConfirmMessage,
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedApp?.let { viewModel.deleteAppById(it.id) }
                        showDeleteDialog = false
                        selectedApp = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.btnDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    selectedApp = null
                }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }

    if (showClearCacheDialog && appToClearCache != null) {
        WtaAlertDialog(
            onDismissRequest = {
                showClearCacheDialog = false
                appToClearCache = null
            },
            title = Strings.clearAppCacheTitle,
            text = Strings.clearAppCacheConfirm,
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = appToClearCache
                        showClearCacheDialog = false
                        appToClearCache = null
                        if (target != null) {
                            scope.launch {
                                val fullApp = viewModel.getWebApp(target.id) ?: return@launch
                                val result = com.webtoapp.core.appcache.AppCacheCleaner
                                    .clearForApp(context, fullApp)
                                snackbarHostState.showSnackbar(
                                    Strings.clearAppCacheDone.replace("%s", result.freedBytesText)
                                )
                            }
                        }
                    }
                ) {
                    Text(Strings.clearAppCacheMenu)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showClearCacheDialog = false
                    appToClearCache = null
                }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }

    if (showCategoryEditor) {
        CategoryEditorDialog(
            category = editingCategory,
            onDismiss = {
                showCategoryEditor = false
                editingCategory = null
            },
            onSave = { name, icon ->
                if (editingCategory != null) {
                    viewModel.updateCategory(
                        editingCategory!!.copy(name = name, icon = icon)
                    )
                } else {
                    viewModel.createCategory(name, icon)
                }
                showCategoryEditor = false
                editingCategory = null
            }
        )
    }

    if (showCategoryManager) {
        com.webtoapp.ui.components.CategoryManageDialog(
            categories = categories,
            onDismiss = { showCategoryManager = false },
            onMoveCategory = { category, delta -> viewModel.moveCategory(category, delta) },
            onEditCategory = { category ->
                editingCategory = category
                showCategoryEditor = true
            },
            onDeleteCategory = { viewModel.deleteCategory(it) }
        )
    }

    if (showMoveToCategoryDialog && appToMove != null) {
        val summary = appToMove!!
        MoveToCategoryDialog(
            appName = summary.name,
            currentCategoryId = summary.categoryId,
            categories = categories,
            onDismiss = {
                showMoveToCategoryDialog = false
                appToMove = null
            },
            onMoveToCategory = { categoryId ->
                viewModel.moveAppToCategoryById(summary.id, categoryId)
                showMoveToCategoryDialog = false
                appToMove = null
            }
        )
    }

    if (showDataBackupDialog) {
        DataBackupDialog(onDismiss = { showDataBackupDialog = false })
    }

}

private fun isExternalBrowserUrl(url: String): Boolean {
    val trimmed = url.trim()
    return trimmed.startsWith("http://", ignoreCase = true) ||
        trimmed.startsWith("https://", ignoreCase = true)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppCard(
    app: WebAppSummary,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEdit: () -> Unit,
    onEditCore: () -> Unit = {},
    onDelete: () -> Unit,
    onCreateShortcut: () -> Unit = {},
    onExport: () -> Unit = {},
    onBuildApk: () -> Unit = {},
    onShareApk: () -> Unit = {},
    onMoveToCategory: () -> Unit = {},
    onClearCache: () -> Unit = {},
    onOpenInBrowser: () -> Unit = {},
    healthStatus: com.webtoapp.core.stats.HealthStatus? = null,
    previewImageLoader: ImageLoader,
    screenshotPath: String? = null,
    screenshotVersion: Int = 0,
    isScreenshotLoading: Boolean = false,
    onCaptureScreenshot: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val secondaryText = when (app.appType) {
        com.webtoapp.data.model.AppType.IMAGE -> app.url.ifBlank { Strings.appTypeImage }
        com.webtoapp.data.model.AppType.VIDEO -> app.url.ifBlank { Strings.appTypeVideo }
        com.webtoapp.data.model.AppType.HTML,
        com.webtoapp.data.model.AppType.FRONTEND -> app.url.ifBlank { Strings.appTypeHtml }
        com.webtoapp.data.model.AppType.GALLERY -> app.url.ifBlank { Strings.appTypeGallery }
        else -> app.url
    }

    EnhancedElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        pressScale = 0.97f,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                if (app.iconPath != null) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(WtaRadius.Card))
                            .background(colors.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(app.iconPath)
                                .crossfade(false)
                                .build(),
                            contentDescription = app.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    WtaShapePlate(
                        polygon = MaterialShapes.Cookie6Sided,
                        containerColor = colors.secondaryContainer,
                        modifier = Modifier.size(52.dp)
                    ) {
                        WtaAppTypeIcon(
                            type = app.appType,
                            modifier = Modifier.size(26.dp),
                            color = colors.onSecondaryContainer,
                            accent = colors.primary
                        )
                    }
                }

                if (healthStatus != null && healthStatus != com.webtoapp.core.stats.HealthStatus.UNKNOWN) {
                    val semantic = com.webtoapp.ui.design.WtaColors.semantic
                    val dotColor = when (healthStatus) {
                        com.webtoapp.core.stats.HealthStatus.ONLINE -> semantic.success
                        com.webtoapp.core.stats.HealthStatus.SLOW -> semantic.warning
                        com.webtoapp.core.stats.HealthStatus.OFFLINE -> semantic.error
                        else -> semantic.neutral
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(12.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(colors.surface)
                            .padding(2.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppTypeChip(appType = app.appType)
                    if (app.activationEnabled) {
                        FeatureChip(icon = Icons.Outlined.Key, label = Strings.activationCodeVerify)
                    } else if (app.adBlockEnabled) {
                        FeatureChip(icon = Icons.Outlined.Block, label = Strings.adBlocking)
                    } else if (app.announcementEnabled) {
                        FeatureChip(icon = Icons.Outlined.Info, label = Strings.popupAnnouncement)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(WtaRadius.Control))
                    .background(colors.surfaceContainerHigh)
                    .clickable(enabled = onCaptureScreenshot != null) {
                        onCaptureScreenshot?.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (screenshotPath != null) {
                    val screenshotCacheKey = "wta_shot_${app.id}_v$screenshotVersion"
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(java.io.File(screenshotPath))
                            .memoryCacheKey(screenshotCacheKey)
                            .diskCacheKey(screenshotCacheKey)
                            .crossfade(false)
                            .build(),
                        imageLoader = previewImageLoader,
                        contentDescription = Strings.btnPreview,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isScreenshotLoading) {
                        WtaLoadingIndicator(size = 14.dp, color = colors.onSurfaceVariant)
                    }
                } else if (isScreenshotLoading) {
                    WtaLoadingIndicator(size = 14.dp, color = colors.onSurfaceVariant)
                } else if (onCaptureScreenshot != null) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = Strings.btnPreview,
                        modifier = Modifier.size(18.dp),
                        tint = colors.onSurfaceVariant
                    )
                } else {
                    WtaAppTypeIcon(
                        type = app.appType,
                        modifier = Modifier.size(18.dp),
                        color = colors.onSurfaceVariant,
                        accent = colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            Box {
                com.webtoapp.ui.design.WtaIconButton(
                    onClick = { expanded = true },
                    icon = Icons.Default.MoreVert,
                    contentDescription = Strings.more
                )

                AppCardActionsMenu(
                    app = app,
                    expanded = expanded,
                    onDismiss = { expanded = false },
                    actions = HomeAppActions(
                        onEdit = onEdit,
                        onEditCore = onEditCore,
                        onDelete = onDelete,
                        onCreateShortcut = onCreateShortcut,
                        onExport = onExport,
                        onBuildApk = onBuildApk,
                        onShareApk = onShareApk,
                        onMoveToCategory = onMoveToCategory,
                        onClearCache = onClearCache,
                        onOpenInBrowser = onOpenInBrowser
                    )
                )
            }
        }
    }
}

class HomeAppActions(
    val onEdit: () -> Unit,
    val onEditCore: () -> Unit,
    val onDelete: () -> Unit,
    val onCreateShortcut: () -> Unit,
    val onExport: () -> Unit,
    val onBuildApk: () -> Unit,
    val onShareApk: () -> Unit,
    val onMoveToCategory: () -> Unit,
    val onClearCache: () -> Unit,
    val onOpenInBrowser: () -> Unit
)

@Composable
fun AppCardActionsMenu(
    app: WebAppSummary,
    expanded: Boolean,
    onDismiss: () -> Unit,
    actions: HomeAppActions
) {
    val canOpenInBrowser = (app.appType == com.webtoapp.data.model.AppType.WEB ||
        app.appType == com.webtoapp.data.model.AppType.MULTI_WEB) &&
        isExternalBrowserUrl(app.url)
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if (app.appType == com.webtoapp.data.model.AppType.WEB) {
            com.webtoapp.ui.design.WtaDropdownMenuItem(
                text = Strings.btnEdit,
                leadingIcon = Icons.Outlined.Edit,
                onClick = {
                    onDismiss()
                    actions.onEdit()
                }
            )
        } else {
            com.webtoapp.ui.design.WtaDropdownMenuItem(
                text = Strings.editCoreConfig,
                leadingIcon = Icons.Outlined.Tune,
                onClick = {
                    onDismiss()
                    actions.onEditCore()
                }
            )
            com.webtoapp.ui.design.WtaDropdownMenuItem(
                text = Strings.editCommonConfig,
                leadingIcon = Icons.Outlined.Settings,
                onClick = {
                    onDismiss()
                    actions.onEdit()
                }
            )
        }
        HorizontalDivider()
        com.webtoapp.ui.design.WtaDropdownMenuItem(
            text = Strings.btnShortcut,
            leadingIcon = Icons.Outlined.AppShortcut,
            onClick = {
                onDismiss()
                actions.onCreateShortcut()
            }
        )
        com.webtoapp.ui.design.WtaDropdownMenuItem(
            text = Strings.buildDialogTitle,
            leadingIcon = Icons.Outlined.InstallMobile,
            onClick = {
                onDismiss()
                actions.onBuildApk()
            }
        )
        com.webtoapp.ui.design.WtaDropdownMenuItem(
            text = Strings.shareApk,
            leadingIcon = Icons.Outlined.Share,
            onClick = {
                onDismiss()
                actions.onShareApk()
            }
        )
        com.webtoapp.ui.design.WtaDropdownMenuItem(
            text = Strings.exportAppSource,
            leadingIcon = Icons.Outlined.Code,
            onClick = {
                onDismiss()
                actions.onExport()
            }
        )
        com.webtoapp.ui.design.WtaDropdownMenuItem(
            text = Strings.moveToCategory,
            leadingIcon = Icons.Outlined.Folder,
            onClick = {
                onDismiss()
                actions.onMoveToCategory()
            }
        )
        com.webtoapp.ui.design.WtaDropdownMenuItem(
            text = Strings.clearAppCacheMenu,
            leadingIcon = Icons.Outlined.CleaningServices,
            onClick = {
                onDismiss()
                actions.onClearCache()
            }
        )
        if (canOpenInBrowser) {
            com.webtoapp.ui.design.WtaDropdownMenuItem(
                text = Strings.openInBrowser,
                leadingIcon = Icons.Outlined.OpenInBrowser,
                onClick = {
                    onDismiss()
                    actions.onOpenInBrowser()
                }
            )
        }
        HorizontalDivider()
        com.webtoapp.ui.design.WtaDropdownMenuItem(
            text = Strings.btnDelete,
            leadingIcon = Icons.Outlined.Delete,
            destructive = true,
            onClick = {
                onDismiss()
                actions.onDelete()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppGridTile(
    app: WebAppSummary,
    onClick: () -> Unit,
    actions: HomeAppActions,
    previewImageLoader: ImageLoader,
    healthStatus: com.webtoapp.core.stats.HealthStatus? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    EnhancedElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        pressScale = 0.97f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                if (app.iconPath != null) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(WtaRadius.Card))
                            .background(colors.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(app.iconPath)
                                .crossfade(false)
                                .build(),
                            contentDescription = app.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    WtaShapePlate(
                        polygon = MaterialShapes.Sunny,
                        containerColor = colors.secondaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        WtaAppTypeIcon(
                            type = app.appType,
                            modifier = Modifier.size(28.dp),
                            color = colors.onSecondaryContainer,
                            accent = colors.primary
                        )
                    }
                }

                if (healthStatus != null && healthStatus != com.webtoapp.core.stats.HealthStatus.UNKNOWN) {
                    val semantic = com.webtoapp.ui.design.WtaColors.semantic
                    val dotColor = when (healthStatus) {
                        com.webtoapp.core.stats.HealthStatus.ONLINE -> semantic.success
                        com.webtoapp.core.stats.HealthStatus.SLOW -> semantic.warning
                        com.webtoapp.core.stats.HealthStatus.OFFLINE -> semantic.error
                        else -> semantic.neutral
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(12.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(colors.surface)
                            .padding(2.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            AppTypeChip(appType = app.appType)

            Spacer(modifier = Modifier.height(2.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                com.webtoapp.ui.design.WtaIconButton(
                    onClick = { expanded = true },
                    icon = Icons.Default.MoreVert,
                    contentDescription = Strings.more
                )
                AppCardActionsMenu(
                    app = app,
                    expanded = expanded,
                    onDismiss = { expanded = false },
                    actions = actions
                )
            }
        }
    }
}

@Composable
fun FeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    WtaBadge(
        text = label,
        icon = icon,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
}

@Composable
fun AppTypeChip(appType: com.webtoapp.data.model.AppType) {
    val (icon, label) = when (appType) {
        com.webtoapp.data.model.AppType.WEB -> Pair(
            Icons.Outlined.Public,
            Strings.appTypeWeb
        )
        com.webtoapp.data.model.AppType.IMAGE -> Pair(
            Icons.Outlined.Image,
            Strings.appTypeImage
        )
        com.webtoapp.data.model.AppType.VIDEO -> Pair(
            Icons.Outlined.VideoLibrary,
            Strings.appTypeVideo
        )
        com.webtoapp.data.model.AppType.HTML -> Pair(
            Icons.Outlined.Html,
            Strings.appTypeHtml
        )
        com.webtoapp.data.model.AppType.GALLERY -> Pair(
            Icons.Outlined.PhotoLibrary,
            Strings.appTypeGallery
        )
        com.webtoapp.data.model.AppType.FRONTEND -> Pair(
            Icons.Outlined.Rocket,
            Strings.appTypeFrontend
        )
        com.webtoapp.data.model.AppType.WORDPRESS -> Pair(
            Icons.Outlined.Newspaper,
            Strings.appTypeWordPress
        )
        com.webtoapp.data.model.AppType.NODEJS_APP -> Pair(
            Icons.Outlined.Terminal,
            Strings.appTypeNodeJs
        )
        com.webtoapp.data.model.AppType.PHP_APP -> Pair(
            Icons.Outlined.DataObject,
            Strings.appTypePhp
        )
        com.webtoapp.data.model.AppType.PYTHON_APP -> Pair(
            Icons.Outlined.Psychology,
            Strings.appTypePython
        )
        com.webtoapp.data.model.AppType.GO_APP -> Pair(
            Icons.Outlined.Speed,
            Strings.appTypeGo
        )
        com.webtoapp.data.model.AppType.MULTI_WEB -> Pair(
            Icons.Outlined.Language,
            Strings.appTypeMultiWeb
        )
    }

    WtaBadge(
        text = label,
        icon = icon,
        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        contentColor = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyState(
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(232.dp),
            contentAlignment = Alignment.Center
        ) {
            WtaMorphingShape(
                startPolygon = MaterialShapes.Cookie9Sided,
                endPolygon = MaterialShapes.Sunny,
                color = colors.secondaryContainer,
                modifier = Modifier.size(164.dp)
            )
            Icon(
                imageVector = Icons.Outlined.AppShortcut,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = colors.onSecondaryContainer
            )
            WtaMaterialShape(
                polygon = MaterialShapes.Clover4Leaf,
                color = colors.tertiaryContainer,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-14).dp, y = 22.dp)
                    .size(44.dp),
                rotationDegrees = 18f
            )
            WtaMaterialShape(
                polygon = MaterialShapes.Gem,
                color = colors.primaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 18.dp, y = (-26).dp)
                    .size(36.dp),
                rotationDegrees = -12f
            )
            WtaMaterialShape(
                polygon = MaterialShapes.Pill,
                color = colors.surfaceContainerHigh,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 26.dp, y = 40.dp)
                    .size(width = 34.dp, height = 18.dp),
                rotationDegrees = -28f
            )
            WtaMaterialShape(
                polygon = MaterialShapes.Triangle,
                color = colors.tertiaryContainer.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-24).dp, y = (-40).dp)
                    .size(22.dp),
                rotationDegrees = 24f
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            WtaMaterialShape(
                polygon = MaterialShapes.Sunny,
                color = colors.tertiaryContainer,
                modifier = Modifier
                    .size(22.dp)
                    .offset(y = (-1).dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = Strings.msgNoApps,
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(12.dp))
            WtaMaterialShape(
                polygon = MaterialShapes.Sunny,
                color = colors.tertiaryContainer,
                modifier = Modifier
                    .size(22.dp)
                    .offset(y = (-1).dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val CreateFabShape = WtaPolygonShape(MaterialShapes.Cookie12Sided)

@Composable
private fun MoreMenuIcon(
    icon: ImageVector,
    polygon: RoundedPolygon,
    tone: Int
) {
    val colors = MaterialTheme.colorScheme
    val container: Color
    val onContainer: Color
    when (tone % 3) {
        0 -> {
            container = colors.primaryContainer
            onContainer = colors.onPrimaryContainer
        }
        1 -> {
            container = colors.secondaryContainer
            onContainer = colors.onSecondaryContainer
        }
        else -> {
            container = colors.tertiaryContainer
            onContainer = colors.onTertiaryContainer
        }
    }
    WtaShapePlate(
        polygon = polygon,
        containerColor = container,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            tint = onContainer
        )
    }
}

