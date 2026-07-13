package com.webtoapp.ui.screens

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.webtoapp.core.appmodifier.AppCloner
import com.webtoapp.core.appmodifier.AppFilterType
import com.webtoapp.core.appmodifier.AppListProvider
import com.webtoapp.core.appmodifier.AppModifyResult
import com.webtoapp.core.appmodifier.InstalledAppInfo
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.SplashType
import com.webtoapp.ui.components.ActivationCodeCard
import com.webtoapp.ui.components.AppNameTextFieldSimple
import com.webtoapp.ui.components.BgmCard
import com.webtoapp.ui.components.IconPickerWithLibrary
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaEmptyState
import com.webtoapp.ui.design.WtaFullEmptyState
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaStatusBanner
import com.webtoapp.ui.design.WtaStatusTone
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.ui.screens.create.WtaCreateFlowScaffold
import com.webtoapp.ui.screens.create.WtaCreateFlowSection
import com.webtoapp.util.SplashStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private enum class AppSortMode {
    NAME,
    SIZE,
    RECENT
}

private enum class ModifyOutputMode {
    SHORTCUT,
    CLONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModifierScreen(
    onBack: () -> Unit,
    onSelectApp: (String) -> Unit
) {
    val context = LocalContext.current
    val appListProvider = remember { AppListProvider(context) }
    val scope = rememberCoroutineScope()

    var allApps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filterName by rememberSaveable { mutableStateOf(AppFilterType.USER.name) }
    var sortName by rememberSaveable { mutableStateOf(AppSortMode.NAME.name) }

    val filterType = runCatching { AppFilterType.valueOf(filterName) }.getOrDefault(AppFilterType.USER)
    val sortMode = runCatching { AppSortMode.valueOf(sortName) }.getOrDefault(AppSortMode.NAME)

    suspend fun reload() {
        isLoading = true
        loadError = null
        runCatching {
            withContext(Dispatchers.IO) {
                appListProvider.getInstalledApps(AppFilterType.ALL, "")
            }
        }.onSuccess {
            allApps = it
        }.onFailure {
            loadError = it.message ?: Strings.unknownError
            allApps = emptyList()
        }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        reload()
    }

    val userCount = remember(allApps) { allApps.count { !it.isSystemApp } }
    val systemCount = remember(allApps) { allApps.count { it.isSystemApp } }
    val totalCount = allApps.size

    val filteredApps = remember(allApps, filterType, searchQuery, sortMode) {
        val q = searchQuery.trim()
        allApps
            .asSequence()
            .filter { app ->
                when (filterType) {
                    AppFilterType.ALL -> true
                    AppFilterType.USER -> !app.isSystemApp
                    AppFilterType.SYSTEM -> app.isSystemApp
                }
            }
            .filter { app ->
                q.isEmpty() ||
                    app.appName.contains(q, ignoreCase = true) ||
                    app.packageName.contains(q, ignoreCase = true)
            }
            .sortedWith(
                when (sortMode) {
                    AppSortMode.NAME -> compareBy { it.appName.lowercase() }
                    AppSortMode.SIZE -> compareByDescending { it.apkSize }
                    AppSortMode.RECENT -> compareByDescending { it.updatedTime }
                }
            )
            .toList()
    }

    WtaScreen(
        title = Strings.appIconModifier,
        subtitle = Strings.appModifierSubtitle,
        onBack = onBack,
        actions = {
            IconButton(
                onClick = { scope.launch { reload() } },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.Refresh, contentDescription = Strings.refresh)
                }
            }
        }
    ) {
        when {
            isLoading && allApps.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            Strings.appModifierLoading,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            loadError != null && allApps.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WtaFullEmptyState(
                        title = Strings.appModifierLoadFailed,
                        message = loadError,
                        icon = Icons.Outlined.Warning,
                        fillMaxSize = false,
                        action = {
                            WtaButton(
                                onClick = { scope.launch { reload() } },
                                text = Strings.btnRetry,
                                variant = WtaButtonVariant.Tonal,
                                leadingIcon = Icons.Outlined.Refresh
                            )
                        }
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        LibrarySummaryCard(
                            total = totalCount,
                            userCount = userCount,
                            systemCount = systemCount
                        )
                    }

                    item {
                        WtaTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = Strings.searchApps,
                            leadingIcon = Icons.Outlined.Search,
                            singleLine = true,
                            trailingIcon = if (searchQuery.isNotEmpty()) {
                                {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Outlined.Clear, contentDescription = Strings.clear)
                                    }
                                }
                            } else null
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WtaChip(
                                selected = filterType == AppFilterType.USER,
                                onClick = { filterName = AppFilterType.USER.name },
                                label = "${Strings.userApps} · $userCount"
                            )
                            WtaChip(
                                selected = filterType == AppFilterType.SYSTEM,
                                onClick = { filterName = AppFilterType.SYSTEM.name },
                                label = "${Strings.systemApps} · $systemCount"
                            )
                            WtaChip(
                                selected = filterType == AppFilterType.ALL,
                                onClick = { filterName = AppFilterType.ALL.name },
                                label = "${Strings.all} · $totalCount"
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SortChip(
                                selected = sortMode == AppSortMode.NAME,
                                label = Strings.appModifierSortName,
                                icon = Icons.Outlined.SortByAlpha,
                                onClick = { sortName = AppSortMode.NAME.name }
                            )
                            SortChip(
                                selected = sortMode == AppSortMode.SIZE,
                                label = Strings.appModifierSortSize,
                                icon = Icons.Outlined.Storage,
                                onClick = { sortName = AppSortMode.SIZE.name }
                            )
                            SortChip(
                                selected = sortMode == AppSortMode.RECENT,
                                label = Strings.appModifierSortRecent,
                                icon = Icons.Outlined.Schedule,
                                onClick = { sortName = AppSortMode.RECENT.name }
                            )
                        }
                    }

                    item {
                        Text(
                            text = Strings.appModifierResultCount(filteredApps.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (filteredApps.isEmpty()) {
                        item {
                            WtaFullEmptyState(
                                title = if (searchQuery.isNotBlank()) Strings.appNotFound else Strings.msgNoApps,
                                message = Strings.appModifierEmptyMessage,
                                icon = Icons.Outlined.SearchOff,
                                fillMaxSize = false
                            )
                        }
                    } else {
                        items(filteredApps, key = { it.packageName }) { app ->
                            AppLibraryCard(
                                app = app,
                                onClick = { onSelectApp(app.packageName) }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SortChip(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    WtaChip(
        selected = selected,
        onClick = onClick,
        label = label,
        leadingIcon = icon,
        showSelectedCheck = false
    )
}

@Composable
private fun LibrarySummaryCard(
    total: Int,
    userCount: Int,
    systemCount: Int
) {
    WtaCard(tone = WtaCardTone.Highlighted, contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.appModifierLibraryTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = Strings.appModifierLibraryHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetric(
                    value = total.toString(),
                    label = Strings.appModifierMetricTotal,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    value = userCount.toString(),
                    label = Strings.userApps,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    value = systemCount.toString(),
                    label = Strings.systemApps,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AppLibraryCard(
    app: InstalledAppInfo,
    onClick: () -> Unit
) {
    WtaCard(
        onClick = onClick,
        tone = WtaCardTone.Surface,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconBox(app = app, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    TypeBadge(isSystem = app.isSystemApp)
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "v${app.versionName} · ${app.formattedSize}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TypeBadge(isSystem: Boolean) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (isSystem) {
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        }
    ) {
        Text(
            text = if (isSystem) Strings.appModifierBadgeSystem else Strings.appModifierBadgeUser,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = if (isSystem) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    }
}

@Composable
private fun AppIconBox(
    app: InstalledAppInfo,
    size: Dp = 48.dp
) {
    val bitmap = remember(app.packageName, app.icon) {
        app.icon?.toSafeBitmap()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = app.appName,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(WtaRadius.IconPlate))
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(WtaRadius.IconPlate))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Android,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Drawable.toSafeBitmap(): ImageBitmap? {
    return runCatching {
        toBitmap(96, 96).asImageBitmap()
    }.getOrNull()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModifyFullScreen(
    packageName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val appListProvider = remember { AppListProvider(context) }
    val appCloner = remember { AppCloner(context) }

    var app by remember { mutableStateOf<InstalledAppInfo?>(null) }
    var loadingApp by remember { mutableStateOf(true) }

    LaunchedEffect(packageName) {
        loadingApp = true
        app = appListProvider.getAppInfo(packageName)
        loadingApp = false
    }

    if (loadingApp) {
        WtaScreen(
            title = Strings.modifyApp,
            onBack = onBack,
            snackbarHostState = snackbarHostState
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val resolvedApp = app
    if (resolvedApp == null) {
        WtaScreen(
            title = Strings.modifyApp,
            onBack = onBack,
            snackbarHostState = snackbarHostState
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                WtaEmptyState(
                    title = Strings.appNotFound,
                    message = packageName,
                    icon = Icons.Outlined.SearchOff
                )
            }
        }
        return
    }

    AppModifyContent(
        app = resolvedApp,
        appCloner = appCloner,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onResult = { result ->
            scope.launch {
                when (result) {
                    is AppModifyResult.ShortcutSuccess -> {
                        snackbarHostState.showSnackbar(Strings.shortcutCreated)
                        onBack()
                    }
                    is AppModifyResult.CloneSuccess -> {
                        snackbarHostState.showSnackbar(Strings.cloneSuccess)
                        onBack()
                    }
                    is AppModifyResult.Error -> {
                        snackbarHostState.showSnackbar("${Strings.failed}: ${result.message}")
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppModifyContent(
    app: InstalledAppInfo,
    appCloner: AppCloner,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onResult: (AppModifyResult) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var editState by remember { mutableStateOf(AppModifierEditState(newAppName = app.appName)) }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var progressText by remember { mutableStateOf("") }
    var outputMode by remember { mutableStateOf(ModifyOutputMode.SHORTCUT) }

    fun update(transform: AppModifierEditState.() -> AppModifierEditState) {
        editState = editState.transform()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { update { copy(newIconUri = it, newIconPath = null) } }
    }
    val splashImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val savedPath = SplashStorage.saveMediaFromUri(context, it, isVideo = false)
                if (savedPath != null) {
                    update {
                        copy(
                            splashEnabled = true,
                            splashConfig = splashConfig.copy(
                                type = SplashType.IMAGE,
                                mediaPath = savedPath
                            ),
                            splashMediaUri = Uri.fromFile(File(savedPath))
                        )
                    }
                }
            }
        }
    }
    val splashVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val savedPath = SplashStorage.saveMediaFromUri(context, it, isVideo = true)
                if (savedPath != null) {
                    update {
                        copy(
                            splashEnabled = true,
                            splashConfig = splashConfig.copy(
                                type = SplashType.VIDEO,
                                mediaPath = savedPath,
                                videoStartMs = 0L,
                                videoEndMs = 5000L,
                                videoDurationMs = 0L
                            ),
                            splashMediaUri = Uri.fromFile(File(savedPath))
                        )
                    }
                }
            }
        }
    }

    fun runClone() {
        if (editState.newAppName.isBlank() || isProcessing) return
        isProcessing = true
        scope.launch {
            try {
                val result = appCloner.cloneAndInstall(editState.toConfig(app)) { p, t ->
                    progress = p
                    progressText = t
                }
                isProcessing = false
                onResult(result)
            } catch (e: Exception) {
                isProcessing = false
                onResult(AppModifyResult.Error(e.message ?: Strings.failed))
            }
        }
    }

    fun runShortcut() {
        if (editState.newAppName.isBlank() || isProcessing) return
        isProcessing = true
        scope.launch {
            try {
                val result = appCloner.createModifiedShortcut(editState.toConfig(app)) { p, t ->
                    progress = p
                    progressText = t
                }
                isProcessing = false
                onResult(result)
            } catch (e: Exception) {
                isProcessing = false
                onResult(AppModifyResult.Error(e.message ?: Strings.failed))
            }
        }
    }

    val hasCustomIcon = editState.newIconUri != null || editState.newIconPath != null
    val canClone = !hasCustomIcon
    val nameReady = editState.newAppName.isNotBlank()

    LaunchedEffect(canClone) {
        if (!canClone && outputMode == ModifyOutputMode.CLONE) {
            outputMode = ModifyOutputMode.SHORTCUT
        }
    }

    WtaCreateFlowScaffold(
        title = Strings.modifyApp,
        onBack = if (isProcessing) {
            { }
        } else {
            onBack
        },
        bottomBar = {
            AppModifyBottomBar(
                isProcessing = isProcessing,
                progress = progress,
                progressText = progressText,
                outputMode = outputMode,
                canClone = canClone,
                actionEnabled = nameReady && !isProcessing && (outputMode != ModifyOutputMode.CLONE || canClone),
                onPrimary = {
                    when (outputMode) {
                        ModifyOutputMode.SHORTCUT -> runShortcut()
                        ModifyOutputMode.CLONE -> runClone()
                    }
                }
            )
        }
    ) {
        WtaCreateFlowSection(title = Strings.appModifierIdentitySection) {
            IdentityPreviewCard(
                original = app,
                newName = editState.newAppName.ifBlank { app.appName },
                hasCustomIcon = hasCustomIcon
            )

            OutputModeCard(
                mode = outputMode,
                canClone = canClone,
                onModeChange = { outputMode = it }
            )

            BasicInfoCard(
                editState = editState,
                onNameChange = { update { copy(newAppName = it) } },
                onPickFromGallery = { imagePickerLauncher.launch("image/*") },
                onPickFromLibrary = { path ->
                    update { copy(newIconPath = path, newIconUri = null) }
                },
                onResetIcon = {
                    update { copy(newIconUri = null, newIconPath = null) }
                }
            )
        }

        WtaCreateFlowSection(title = Strings.appConfig) {
            ActivationCodeCard(
                enabled = editState.activationEnabled,
                activationCodes = editState.activationCodes,
                requireEveryTime = editState.activationRequireEveryTime,
                dialogConfig = editState.activationDialogConfig,
                remoteConfig = editState.activationRemoteConfig,
                onEnabledChange = { update { copy(activationEnabled = it) } },
                onCodesChange = { update { copy(activationCodes = it) } },
                onRequireEveryTimeChange = { update { copy(activationRequireEveryTime = it) } },
                onDialogConfigChange = { update { copy(activationDialogConfig = it) } },
                onRemoteConfigChange = { update { copy(activationRemoteConfig = it) } }
            )

            AnnouncementCard(
                enabled = editState.announcementEnabled,
                announcement = editState.announcement,
                onEnabledChange = { update { copy(announcementEnabled = it) } },
                onAnnouncementChange = { update { copy(announcement = it) } }
            )

            SplashScreenCard(
                enabled = editState.splashEnabled,
                splashConfig = editState.splashConfig,
                splashMediaUri = editState.splashMediaUri,
                savedSplashPath = editState.splashConfig.mediaPath,
                onEnabledChange = { update { copy(splashEnabled = it) } },
                onSelectImage = { splashImagePickerLauncher.launch("image/*") },
                onSelectVideo = { splashVideoPickerLauncher.launch("video/*") },
                onDurationChange = { duration ->
                    update { copy(splashConfig = splashConfig.copy(duration = duration)) }
                },
                onClickToSkipChange = { skip ->
                    update { copy(splashConfig = splashConfig.copy(clickToSkip = skip)) }
                },
                onOrientationChange = { orientation ->
                    update { copy(splashConfig = splashConfig.copy(orientation = orientation)) }
                },
                onFillScreenChange = { fill ->
                    update { copy(splashConfig = splashConfig.copy(fillScreen = fill)) }
                },
                onEnableAudioChange = { audio ->
                    update { copy(splashConfig = splashConfig.copy(enableAudio = audio)) }
                },
                onVideoTrimChange = { start, end, total ->
                    update {
                        copy(
                            splashConfig = splashConfig.copy(
                                videoStartMs = start,
                                videoEndMs = end,
                                videoDurationMs = total
                            )
                        )
                    }
                },
                onClearMedia = {
                    update {
                        copy(
                            splashEnabled = false,
                            splashConfig = splashConfig.copy(
                                mediaPath = null,
                                videoStartMs = 0L,
                                videoEndMs = 5000L,
                                videoDurationMs = 0L
                            ),
                            splashMediaUri = null
                        )
                    }
                }
            )

            BgmCard(
                enabled = editState.bgmEnabled,
                config = editState.bgmConfig,
                onEnabledChange = { update { copy(bgmEnabled = it) } },
                onConfigChange = { update { copy(bgmConfig = it) } }
            )

            if (outputMode == ModifyOutputMode.CLONE) {
                WtaStatusBanner(
                    message = Strings.cloneInstallWarning,
                    tone = WtaStatusTone.Warning
                )
            } else {
                WtaStatusBanner(
                    message = Strings.appModifierShortcutHint,
                    tone = WtaStatusTone.Info
                )
            }
        }
    }
}

@Composable
private fun IdentityPreviewCard(
    original: InstalledAppInfo,
    newName: String,
    hasCustomIcon: Boolean
) {
    WtaCard(tone = WtaCardTone.Highlighted, contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = Strings.appModifierPreviewTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IdentitySide(
                    label = Strings.originalApp,
                    name = original.appName,
                    packageName = original.packageName,
                    icon = { AppIconBox(app = original, size = 52.dp) },
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IdentitySide(
                    label = Strings.appModifierNewIdentity,
                    name = newName,
                    packageName = if (hasCustomIcon) Strings.appModifierCustomIcon else original.packageName,
                    icon = {
                        if (hasCustomIcon) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(WtaRadius.IconPlate))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.PhoneAndroid,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        } else {
                            AppIconBox(app = original, size = 52.dp)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun IdentitySide(
    label: String,
    name: String,
    packageName: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = packageName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OutputModeCard(
    mode: ModifyOutputMode,
    canClone: Boolean,
    onModeChange: (ModifyOutputMode) -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface, contentPadding = PaddingValues(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = Strings.appModifierOutputMode,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            ModeOption(
                selected = mode == ModifyOutputMode.SHORTCUT,
                title = Strings.btnShortcut,
                description = Strings.appModifierModeShortcutDesc,
                icon = Icons.Outlined.PhoneAndroid,
                badge = Strings.appModifierRecommended,
                onClick = { onModeChange(ModifyOutputMode.SHORTCUT) }
            )
            ModeOption(
                selected = mode == ModifyOutputMode.CLONE,
                title = Strings.cloneInstall,
                description = if (canClone) {
                    Strings.appModifierModeCloneDesc
                } else {
                    Strings.appModifierCloneNeedsOriginalIcon
                },
                icon = Icons.Outlined.ContentCopy,
                enabled = canClone,
                onClick = { if (canClone) onModeChange(ModifyOutputMode.CLONE) }
            )
        }
    }
}

@Composable
private fun ModeOption(
    selected: Boolean,
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    badge: String? = null
) {
    val container = when {
        !enabled -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
        selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (badge != null) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = WtaColors.semantic.success.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = WtaColors.semantic.success,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected && enabled) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun BasicInfoCard(
    editState: AppModifierEditState,
    onNameChange: (String) -> Unit,
    onPickFromGallery: () -> Unit,
    onPickFromLibrary: (String) -> Unit,
    onResetIcon: () -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface, contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            IconPickerWithLibrary(
                iconUri = editState.newIconUri,
                iconPath = editState.newIconPath,
                onSelectFromGallery = onPickFromGallery,
                onSelectFromLibrary = onPickFromLibrary
            )

            if (editState.newIconUri != null || editState.newIconPath != null) {
                TextButton(
                    onClick = onResetIcon,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Outlined.Refresh, null, Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.useOriginalIcon, style = MaterialTheme.typography.labelMedium)
                }
            }

            AppNameTextFieldSimple(
                value = editState.newAppName,
                onValueChange = onNameChange
            )
        }
    }
}

@Composable
private fun AppModifyBottomBar(
    isProcessing: Boolean,
    progress: Int,
    progressText: String,
    outputMode: ModifyOutputMode,
    canClone: Boolean,
    actionEnabled: Boolean,
    onPrimary: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedVisibility(
                visible = isProcessing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { (progress / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                    if (progressText.isNotBlank()) {
                        Text(
                            progressText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val primaryText = when {
                isProcessing -> Strings.appModifierWorking
                outputMode == ModifyOutputMode.CLONE -> Strings.cloneInstall
                else -> Strings.btnShortcut
            }

            WtaButton(
                onClick = onPrimary,
                text = primaryText,
                variant = WtaButtonVariant.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = actionEnabled,
                leadingIcon = when {
                    isProcessing -> null
                    outputMode == ModifyOutputMode.CLONE -> Icons.Outlined.ContentCopy
                    else -> Icons.Outlined.PhoneAndroid
                }
            )

            if (outputMode == ModifyOutputMode.CLONE && !canClone) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        Strings.appModifierCloneNeedsOriginalIcon,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
