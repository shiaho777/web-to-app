package com.webtoapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.webtoapp.core.appmodifier.AppCloner
import com.webtoapp.core.appmodifier.AppFilterType
import com.webtoapp.core.appmodifier.AppListProvider
import com.webtoapp.core.appmodifier.AppModifyConfig
import com.webtoapp.core.appmodifier.AppModifyResult
import com.webtoapp.core.appmodifier.InstalledAppInfo
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.SplashOrientation
import com.webtoapp.data.model.SplashType
import com.webtoapp.ui.components.ActivationCodeCard
import com.webtoapp.ui.components.AppNameTextFieldSimple
import com.webtoapp.ui.components.BgmCard
import com.webtoapp.ui.components.IconPickerWithLibrary
import com.webtoapp.ui.components.PremiumButton
import com.webtoapp.ui.components.PremiumFilterChip
import com.webtoapp.ui.components.PremiumOutlinedButton
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.design.WtaEmptyState
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaStatusBanner
import com.webtoapp.ui.design.WtaStatusTone
import com.webtoapp.ui.screens.create.WtaCreateFlowScaffold
import com.webtoapp.ui.screens.create.WtaCreateFlowSection
import com.webtoapp.util.SplashStorage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModifierScreen(
    onBack: () -> Unit,
    onSelectApp: (String) -> Unit
) {
    val context = LocalContext.current
    val appListProvider = remember { AppListProvider(context) }

    var apps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf(AppFilterType.USER) }

    LaunchedEffect(filterType, searchQuery) {
        isLoading = true
        apps = appListProvider.getInstalledApps(filterType, searchQuery)
        isLoading = false
    }

    WtaScreen(
        title = Strings.appIconModifier,
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            PremiumTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Strings.searchApps) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        FilledIconButton(
                            onClick = { searchQuery = "" },
                            colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Clear, Strings.clear, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WtaSpacing.ScreenHorizontal,
                        vertical = WtaSpacing.ContentGap
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WtaSpacing.ScreenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                FilterChipFor(filterType, AppFilterType.USER, Strings.userApps) { filterType = it }
                FilterChipFor(filterType, AppFilterType.SYSTEM, Strings.systemApps) { filterType = it }
                FilterChipFor(filterType, AppFilterType.ALL, Strings.all) { filterType = it }
            }

            Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))

            Text(
                text = Strings.totalFilesCount.replace("%d", apps.size.toString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = WtaSpacing.ScreenHorizontal)
            )

            Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                apps.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = WtaSpacing.ScreenHorizontal, vertical = WtaSpacing.SectionGap),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        WtaEmptyState(
                            title = Strings.appNotFound,
                            message = Strings.appModifierEmptyMessage,
                            icon = Icons.Outlined.SearchOff
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = WtaSpacing.ScreenHorizontal,
                            vertical = WtaSpacing.ContentGap
                        ),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            AppListItem(
                                app = app,
                                onClick = { onSelectApp(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipFor(
    current: AppFilterType,
    target: AppFilterType,
    label: String,
    onSelect: (AppFilterType) -> Unit
) {
    PremiumFilterChip(
        selected = current == target,
        onClick = { onSelect(target) },
        label = { Text(label) }
    )
}

@Composable
private fun AppListItem(
    app: InstalledAppInfo,
    onClick: () -> Unit
) {
    WtaSettingCard(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WtaSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconBox(app = app, size = 44.dp)

            Spacer(modifier = Modifier.width(WtaSpacing.Medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "v${app.versionName} · ${app.formattedSize}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppIconBox(
    app: InstalledAppInfo,
    size: Dp = 48.dp
) {
    val drawable = app.icon
    if (drawable != null) {
        Image(
            bitmap = drawable.toBitmap(96, 96).asImageBitmap(),
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
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Android,
                null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
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
                    .padding(WtaSpacing.ScreenHorizontal),
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
                            splashMediaUri = Uri.fromFile(java.io.File(savedPath))
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
                            splashMediaUri = Uri.fromFile(java.io.File(savedPath))
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
                    scope.launch {
                        progress = p
                        progressText = t
                    }
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
                    scope.launch {
                        progress = p
                        progressText = t
                    }
                }
                isProcessing = false
                onResult(result)
            } catch (e: Exception) {
                isProcessing = false
                onResult(AppModifyResult.Error(e.message ?: Strings.failed))
            }
        }
    }

    val canClone = editState.newIconUri == null && editState.newIconPath == null

    WtaCreateFlowScaffold(
        title = Strings.modifyApp,
        onBack = if (isProcessing) {
            {  }
        } else {
            onBack
        },
        bottomBar = {
            AppModifyBottomBar(
                isProcessing = isProcessing,
                progress = progress,
                progressText = progressText,
                canClone = canClone,
                cloneEnabled = editState.newAppName.isNotBlank() && !isProcessing && canClone,
                shortcutEnabled = editState.newAppName.isNotBlank() && !isProcessing,
                onClone = ::runClone,
                onShortcut = ::runShortcut
            )
        }
    ) {
        WtaCreateFlowSection(title = Strings.labelBasicInfo) {
            OriginalAppCard(app = app)

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
                onEnabledChange = { update { copy(activationEnabled = it) } },
                onCodesChange = { update { copy(activationCodes = it) } },
                onRequireEveryTimeChange = { update { copy(activationRequireEveryTime = it) } },
                onDialogConfigChange = { update { copy(activationDialogConfig = it) } }
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

            WtaStatusBanner(
                message = Strings.cloneInstallWarning,
                tone = WtaStatusTone.Warning
            )
        }
    }
}

@Composable
private fun OriginalAppCard(app: InstalledAppInfo) {
    WtaSettingCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WtaSpacing.Large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconBox(app = app, size = 48.dp)
            Spacer(modifier = Modifier.width(WtaSpacing.Large))
            Column {
                Text(
                    Strings.originalApp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(app.appName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${app.packageName} · v${app.versionName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
    WtaSettingCard {
        Column(
            modifier = Modifier.padding(WtaSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Large)
        ) {
            IconPickerWithLibrary(
                iconUri = editState.newIconUri,
                iconPath = editState.newIconPath,
                onSelectFromGallery = onPickFromGallery,
                onSelectFromLibrary = onPickFromLibrary
            )

            if (editState.newIconUri != null || editState.newIconPath != null) {
                androidx.compose.material3.TextButton(
                    onClick = onResetIcon,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Outlined.Refresh, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(WtaSpacing.Tiny))
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
    canClone: Boolean,
    cloneEnabled: Boolean,
    shortcutEnabled: Boolean,
    onClone: () -> Unit,
    onShortcut: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WtaSpacing.ScreenHorizontal,
                    vertical = WtaSpacing.Medium
                ),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
        ) {
            if (isProcessing) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                if (progressText.isNotBlank()) {
                    Text(
                        progressText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canClone) {
                    PremiumOutlinedButton(
                        onClick = onClone,
                        enabled = cloneEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(Strings.cloneInstall)
                    }
                }
                PremiumButton(
                    onClick = onShortcut,
                    enabled = shortcutEnabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(Strings.btnShortcut)
                }
            }
        }
    }
}
