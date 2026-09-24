package com.webtoapp.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.GetApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SystemUpdateAlt
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.apkbuilder.ApkExportPreflight
import com.webtoapp.core.apkbuilder.ApkExportPreflightReport
import com.webtoapp.core.apkbuilder.BuildResult
import com.webtoapp.core.apkbuilder.ExportRuntimeEnsure
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.withRuntimePermissionsSyncedFromFeatures
import com.webtoapp.ui.components.ApkExportPreflightPanel
import com.webtoapp.ui.components.BackgroundRunConfigCard
import com.webtoapp.ui.components.EncryptionConfigCard
import com.webtoapp.ui.components.IsolationConfigCard
import com.webtoapp.ui.components.NotificationConfigCard
import com.webtoapp.ui.components.PremiumButton
import com.webtoapp.ui.components.PremiumOutlinedButton
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaBadge
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaLoadingState
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaToggleRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 独立"构建 APK"配置页。
 *
 * 从应用列表点击"构建 APK"后进入本页，可在全屏页面中调整加密 / 隔离 / 后台运行 /
 * 通知 / 浏览器引擎等构建选项，执行预检、构建，并查看构建摘要与 APK 分析。
 */
@Composable
fun BuildApkScreen(
    appId: Long,
    onBack: () -> Unit,
    onExportAab: (Long) -> Unit = {}
) {
    val repository = remember { WebToAppApplication.repository }
    val webApp by repository.getWebAppById(appId).collectAsState(initial = null)

    val app = webApp
    if (app == null) {
        // 数据尚未加载或应用不存在
        WtaScreen(
            title = Strings.buildDialogTitle,
            onBack = onBack
        ) { _ ->
            WtaLoadingState()
        }
        return
    }

    BuildApkContent(
        webApp = app,
        onBack = onBack,
        onExportAab = { onExportAab(app.id) }
    )
}

@Composable
private fun BuildApkContent(
    webApp: WebApp,
    onBack: () -> Unit,
    onExportAab: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { WebToAppApplication.repository }

    // 双阶段渲染：先显示轻量骨架，等导航过渡动画结束（220ms）后再初始化重活，
    // 避免 ApkBuilder 构造（mkdirs）、包名查询、引擎检查、预检等与过渡动画抢主线程导致掉帧。
    var uiReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(220L)
        uiReady = true
    }
    var apkBuilderState by remember { mutableStateOf<ApkBuilder?>(null) }
    LaunchedEffect(uiReady) {
        if (!uiReady) return@LaunchedEffect
        apkBuilderState = withContext(Dispatchers.IO) { ApkBuilder(context) }
    }

    var showExportAabConfirm by remember { mutableStateOf(false) }

    var isBuilding by remember(webApp.id) { mutableStateOf(false) }
    var progress by remember(webApp.id) { mutableIntStateOf(0) }
    var progressText by remember(webApp.id) { mutableStateOf(Strings.preparing) }
    var analysisReport by remember(webApp.id) { mutableStateOf<com.webtoapp.core.apkbuilder.ApkAnalyzer.AnalysisReport?>(null) }
    var buildFailureReport by remember(webApp.id) { mutableStateOf<BuildFailureReport?>(null) }
    var preflightReport by remember(webApp.id) { mutableStateOf<ApkExportPreflightReport?>(null) }
    var isEnsuringRuntime by remember(webApp.id) { mutableStateOf(false) }
    var ensureRuntimeText by remember(webApp.id) { mutableStateOf<String?>(null) }
    // Runtime downloads under the ensure step report through the shared engine;
    // surface them so "preparing" shows real progress instead of a bare spinner.
    val depDownloadState by com.webtoapp.core.download.DependencyDownloadEngine.state
        .collectAsStateWithLifecycle()
    var forceFullRebuild by remember(webApp.id) {
        mutableStateOf(webApp.apkExportConfig?.forceFullRebuild ?: false)
    }
    var lastBuildMode by remember(webApp.id) { mutableStateOf<String?>(null) }
    var lastBuildReason by remember(webApp.id) { mutableStateOf<String?>(null) }
    var cacheMessage by remember(webApp.id) { mutableStateOf<String?>(null) }

    var encryptionConfig by remember(webApp.id) {
        mutableStateOf(webApp.apkExportConfig?.encryptionConfig ?: com.webtoapp.data.model.ApkEncryptionConfig())
    }

    var isolationConfig by remember(webApp.id) {
        mutableStateOf(resolveBuildIsolationDefault(webApp.apkExportConfig?.isolationConfig))
    }

    var backgroundRunEnabled by remember(webApp.id) {
        mutableStateOf(webApp.apkExportConfig?.backgroundRunEnabled ?: false)
    }
    var backgroundRunConfig by remember(webApp.id) {
        mutableStateOf(webApp.apkExportConfig?.backgroundRunConfig ?: com.webtoapp.data.model.BackgroundRunExportConfig())
    }

    var notificationEnabled by remember(webApp.id) {
        mutableStateOf(webApp.apkExportConfig?.notificationEnabled ?: false)
    }
    var notificationConfig by remember(webApp.id) {
        mutableStateOf(webApp.apkExportConfig?.notificationConfig ?: com.webtoapp.data.model.NotificationExportConfig())
    }

    var selectedEngineType by remember(webApp.id) {
        mutableStateOf(webApp.apkExportConfig?.engineType ?: "SYSTEM_WEBVIEW")
    }

    var perAppSigningEnabled by remember(webApp.id) {
        mutableStateOf(webApp.apkExportConfig?.perAppSigningEnabled ?: false)
    }
    // Resolve the package exactly the way the builder does. That rule used to
    // live inside ApkBuilder, so the screen could not reproduce a derived
    // package name, never found the installed app, and never bumped the version.
    val resolvedPackageName = com.webtoapp.core.apkbuilder.ApkBuilder.resolvePackageName(webApp)
    val baseVersionCode = webApp.apkExportConfig?.customVersionCode ?: 1
    val autoVersionBump = webApp.apkExportConfig?.autoVersionBump ?: true
    var suggestedVersion by remember(resolvedPackageName) {
        mutableStateOf<Pair<Int, String>?>(null)
    }
    LaunchedEffect(resolvedPackageName, baseVersionCode, autoVersionBump, uiReady) {
        if (!uiReady) return@LaunchedEffect
        suggestedVersion = withContext(Dispatchers.IO) {
            com.webtoapp.core.apkbuilder.ApkBuilder.suggestedVersionForInstall(context, webApp)
        }
    }
    val engineFileManager = remember { com.webtoapp.core.engine.download.EngineFileManager(context) }
    var isGeckoDownloaded by remember { mutableStateOf(false) }
    LaunchedEffect(selectedEngineType, uiReady) {
        if (!uiReady) return@LaunchedEffect
        isGeckoDownloaded = withContext(Dispatchers.IO) {
            engineFileManager.isEngineDownloaded(
                com.webtoapp.core.engine.EngineType.GECKOVIEW,
                com.webtoapp.core.engine.download.GeckoEngineDownloader.DEFAULT_VERSION
            )
        }
    }

    /**
     * The build-screen-managed slice of [com.webtoapp.data.model.ApkExportConfig]: every
     * option this screen edits, merged onto the app's stored config so untouched fields
     * (package name, version, permissions, …) survive. Single source for both the build
     * call and the persist path below.
     */
    fun buildScreenExportConfig(): com.webtoapp.data.model.ApkExportConfig {
        return (webApp.apkExportConfig ?: com.webtoapp.data.model.ApkExportConfig()).copy(
            encryptionConfig = encryptionConfig,
            isolationConfig = isolationConfig,
            backgroundRunEnabled = backgroundRunEnabled,
            backgroundRunConfig = backgroundRunConfig,
            notificationEnabled = notificationEnabled,
            notificationConfig = notificationConfig,
            engineType = selectedEngineType,
            perAppSigningEnabled = perAppSigningEnabled,
            forceFullRebuild = forceFullRebuild
        )
    }

    fun currentBuildConfig(): WebApp {
        return webApp.copy(
            apkExportConfig = buildScreenExportConfig().let { exportConfig ->
                val suggested = suggestedVersion
                if (suggested != null && (exportConfig.customVersionCode ?: 1) < suggested.first) {
                    exportConfig.copy(
                        customVersionCode = suggested.first,
                        customVersionName = suggested.second
                    )
                } else {
                    exportConfig
                }
            }
        ).withRuntimePermissionsSyncedFromFeatures()
    }

    // Persist the build options onto the app row so a later visit restores them instead
    // of resetting to defaults. The snapshot from first composition is the "clean"
    // baseline — writes only run after the user actually changes something, and
    // persistBuildScreenExportConfig itself skips no-op writes (updateWebApp bumps
    // `updatedAt`, which would reshuffle the app list and re-emit into the collector).
    val exportConfigSnapshot = buildScreenExportConfig()
    val initialExportConfig = remember(webApp.id) { exportConfigSnapshot }
    val latestExportConfig by rememberUpdatedState(exportConfigSnapshot)
    LaunchedEffect(exportConfigSnapshot) {
        if (exportConfigSnapshot == initialExportConfig) return@LaunchedEffect
        delay(400)
        persistBuildScreenExportConfig(repository, webApp.id, exportConfigSnapshot)
    }
    DisposableEffect(webApp.id) {
        onDispose {
            val pending = latestExportConfig
            if (pending != initialExportConfig) {
                // The composition scope is already gone by the time onDispose runs; flush
                // on a detached IO scope so a change made right before leaving still lands.
                CoroutineScope(Dispatchers.IO).launch {
                    persistBuildScreenExportConfig(repository, webApp.id, pending)
                }
            }
        }
    }

    fun launchBuild() {
        if (isBuilding) return
        val apkBuilder = apkBuilderState ?: return
        val webAppWithConfig = currentBuildConfig()

        isBuilding = true
        buildFailureReport = null
        analysisReport = null
        lastBuildMode = null
        lastBuildReason = null
        cacheMessage = null
        progress = 0
        progressText = Strings.preparing
        scope.launch {
            progressText = when (webAppWithConfig.appType) {
                AppType.PYTHON_APP -> Strings.preparingPythonEnv
                AppType.NODEJS_APP -> Strings.preparingNodeEnv
                AppType.PHP_APP,
                AppType.WORDPRESS -> Strings.preparing
                else -> Strings.preparing
            }
            val ensureOk = ExportRuntimeEnsure.ensure(
                context,
                webAppWithConfig.appType,
                webAppWithConfig.webViewConfig.forceHttp3 ||
                    webAppWithConfig.webViewConfig.dnsConfig.echEffective,
                neededAbis = webAppWithConfig.apkExportConfig?.architecture?.abiFilters
            )
            if (!ensureOk) {
                // Abort here: buildApk runs the same ensure again, so falling
                // through would burn a second full runtime download attempt
                // before the build fails on the same missing dependency.
                progressText = when (webAppWithConfig.appType) {
                    AppType.PYTHON_APP -> Strings.pythonRuntimeDownloadFailed
                    AppType.NODEJS_APP -> Strings.njsDownloadFailed
                    AppType.PHP_APP,
                    AppType.WORDPRESS -> Strings.wpDownloadFailed
                    else -> Strings.preparing
                }
                isBuilding = false
                return@launch
            }
            val nextPreflight = ApkExportPreflight.check(context, webAppWithConfig)
            preflightReport = nextPreflight
            if (nextPreflight.hasErrors) {
                isBuilding = false
                return@launch
            }

            val result = apkBuilder.buildApk(
                webApp = webAppWithConfig,
                forceFullRebuild = forceFullRebuild
            ) { p, t ->
                progress = p
                progressText = t
            }
            when (result) {
                is BuildResult.Success -> {
                    analysisReport = result.analysisReport
                    lastBuildMode = result.buildMode
                    lastBuildReason = result.buildReason.takeIf { it.isNotBlank() }
                    progressText = Strings.buildModeUsed.replace(
                        "%s",
                        incrementalBuildModeLabel(result.buildMode)
                    )
                    isBuilding = false
                }
                is BuildResult.Error -> {
                    buildFailureReport = buildBuildFailureReport(webAppWithConfig, result)
                    isBuilding = false
                }
            }
        }
    }

    LaunchedEffect(
        webApp,
        encryptionConfig,
        isolationConfig,
        backgroundRunEnabled,
        backgroundRunConfig,
        notificationEnabled,
        notificationConfig,
        selectedEngineType,
        uiReady
    ) {
        if (!uiReady) return@LaunchedEffect
        val config = currentBuildConfig()
        val needsCronet = config.webViewConfig.forceHttp3 || config.webViewConfig.dnsConfig.echEffective
        if (ExportRuntimeEnsure.needsEnsure(
                context,
                config.appType,
                needsCronet,
                neededAbis = webApp.apkExportConfig?.architecture?.abiFilters
            )
        ) {
            isEnsuringRuntime = true
            ensureRuntimeText = when (config.appType) {
                AppType.PYTHON_APP -> Strings.preparingPythonEnv
                AppType.NODEJS_APP -> Strings.preparingNodeEnv
                AppType.PHP_APP,
                AppType.WORDPRESS -> Strings.preparing
                else -> Strings.preparing
            }
            val ensureOk = ExportRuntimeEnsure.ensure(
                context,
                config.appType,
                needsCronet,
                neededAbis = webApp.apkExportConfig?.architecture?.abiFilters
            )
            if (!ensureOk) {
                ensureRuntimeText = when (config.appType) {
                    AppType.PYTHON_APP -> Strings.pythonRuntimeDownloadFailed
                    AppType.NODEJS_APP -> Strings.njsDownloadFailed
                    AppType.PHP_APP,
                    AppType.WORDPRESS -> Strings.wpDownloadFailed
                    else -> Strings.preparing
                }
            } else {
                ensureRuntimeText = null
            }
            isEnsuringRuntime = false
        } else {
            ensureRuntimeText = null
            isEnsuringRuntime = false
        }
        preflightReport = withContext(Dispatchers.IO) {
            ApkExportPreflight.check(context, config)
        }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(analysisReport) {
        if (analysisReport != null) {
            withFrameNanos {}
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    WtaScreen(
        title = Strings.buildDialogTitle,
        subtitle = webApp.name,
        onBack = { if (!isBuilding) onBack() },
        bottomBar = {
            if (!isBuilding) {
                val builtApk = analysisReport?.apkFile
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PremiumOutlinedButton(
                            onClick = { showExportAabConfirm = true },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.PlayCircleOutline,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("AAB", maxLines = 1)
                        }
                        Spacer(Modifier.width(8.dp))
                        PremiumButton(
                            onClick = {
                                if (builtApk != null) {
                                    val installStarted = apkBuilderState?.installApk(builtApk) ?: false
                                    android.widget.Toast.makeText(
                                        context,
                                        if (installStarted) Strings.fileManagerInstallStarted
                                        else Strings.fileManagerInstallFailed,
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    launchBuild()
                                }
                            },
                            enabled = !isEnsuringRuntime,
                            modifier = Modifier.weight(1f)
                        ) {
                            val icon = if (builtApk != null) Icons.Outlined.GetApp else Icons.Outlined.Build
                            Icon(icon, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                when {
                                    builtApk != null -> Strings.install
                                    buildFailureReport != null || preflightReport?.hasErrors == true -> Strings.btnRetry
                                    else -> Strings.btnStartBuild
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    ) { _ ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                WtaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val iconPath = webApp.iconPath
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(WtaRadius.IconPlate)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!iconPath.isNullOrBlank()) {
                            AsyncImage(
                                model = iconPath,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = com.webtoapp.ui.design.WtaAlpha.MutedContainer
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Android,
                                    null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            webApp.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            when (webApp.appType) {
                                AppType.IMAGE -> {
                                    webApp.mediaConfig?.mediaPath ?: webApp.url
                                }
                                AppType.VIDEO -> {
                                    webApp.mediaConfig?.mediaPath ?: webApp.url
                                }
                                AppType.HTML -> {
                                    webApp.htmlConfig?.entryFile?.takeIf { it.isNotBlank() } ?: "index.html"
                                }
                                else -> webApp.url
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            resolvedPackageName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                }
            }

            item {
                EncryptionConfigCard(
                    config = encryptionConfig,
                    onConfigChange = { encryptionConfig = it }
                )
            }

            item {
                IsolationConfigCard(
                    config = isolationConfig,
                    onConfigChange = { isolationConfig = it }
                )
            }

            item {
                BackgroundRunConfigCard(
                    enabled = backgroundRunEnabled,
                    config = backgroundRunConfig,
                    onEnabledChange = { backgroundRunEnabled = it },
                    onConfigChange = { backgroundRunConfig = it }
                )
            }

            item {
                NotificationConfigCard(
                    enabled = notificationEnabled,
                    config = notificationConfig,
                    onEnabledChange = { notificationEnabled = it },
                    onConfigChange = { notificationConfig = it }
                )
            }

            // GeckoView is offered for WEB and MULTI_WEB: multi-web surfaces share
            // one GeckoRuntime across per-site GeckoSessions (#1035).
            if (webApp.appType == AppType.WEB || webApp.appType == AppType.MULTI_WEB) {
                item {
                    WtaCard {
                        EngineSelectionCard(
                            selectedEngine = selectedEngineType,
                            isGeckoDownloaded = isGeckoDownloaded,
                            onEngineSelected = { selectedEngineType = it }
                        )
                    }
                }
            }

            if (analysisReport == null && !isBuilding) {
                item {
                    WtaCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                        WtaToggleRow(
                            icon = Icons.Outlined.Cached,
                            title = Strings.forceFullRebuild,
                            subtitle = Strings.forceFullRebuildDesc,
                            checked = forceFullRebuild,
                            onCheckedChange = { forceFullRebuild = it }
                        )
                        WtaSectionDivider()
                        WtaToggleRow(
                            icon = Icons.Outlined.VerifiedUser,
                            title = Strings.perAppSigningTitle,
                            subtitle = Strings.perAppSigningHint,
                            checked = perAppSigningEnabled,
                            onCheckedChange = { perAppSigningEnabled = it }
                        )
                        WtaSectionDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    apkBuilderState?.clearIncrementalCache(currentBuildConfig())
                                    cacheMessage = Strings.incrementalCacheCleared
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.DeleteSweep,
                                    null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    Strings.clearIncrementalCache,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            cacheMessage?.let { msg ->
                                WtaBadge(
                                    text = msg,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            if (analysisReport == null) {
                item {
                    WtaCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    Strings.buildApkForApp.replace("%s", webApp.name),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                Strings.buildCompleteInstallHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (suggestedVersion != null) {
                                Surface(
                                    shape = RoundedCornerShape(WtaRadius.Control),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.SystemUpdateAlt,
                                            null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            Strings.updateApkGuide.replace("%s", resolvedPackageName)
                                                .replace("%d", (suggestedVersion?.first ?: baseVersionCode).toString()),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isEnsuringRuntime || ensureRuntimeText != null) {
                item {
                    WtaCard(
                        tone = if (isEnsuringRuntime) WtaCardTone.Highlighted else WtaCardTone.Critical,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val downloading =
                                (depDownloadState as? com.webtoapp.core.download.DependencyDownloadEngine.State.Downloading)
                                    ?.takeIf { isEnsuringRuntime }
                            Text(
                                text = if (downloading != null) {
                                    "$ensureRuntimeText — ${downloading.displayName} " +
                                        "${downloading.bytesDownloaded / 1024 / 1024}MB" +
                                        (downloading.totalBytes.takeIf { it > 0 }
                                            ?.let { " / ${it / 1024 / 1024}MB" } ?: "") +
                                        " · ${com.webtoapp.core.download.DependencyDownloadEngine.formatSpeed(downloading.speedBytesPerSec)}/s"
                                } else {
                                    ensureRuntimeText ?: Strings.preparing
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isEnsuringRuntime) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                            if (isEnsuringRuntime) {
                                if (downloading != null && downloading.totalBytes > 0) {
                                    LinearProgressIndicator(
                                        progress = { downloading.progress },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }

            if (!isEnsuringRuntime) {
                preflightReport?.let { report ->
                    item {
                        ApkExportPreflightPanel(report = report)
                    }
                }
            }

            if (isBuilding) {
                item {
                    WtaCard(tone = WtaCardTone.Highlighted) {

                    val animatedProgress by animateFloatAsState(
                        targetValue = progress / 100f,
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "buildProgress"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                            Text(
                                "${progress}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                progressText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.fillMaxWidth(),
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        }
                    }
                    }
                }
            }

            analysisReport?.let { report ->
                item {
                    WtaCard {
                        BuildSummaryCard(
                            webApp = webApp,
                            apkFile = report.apkFile,
                            totalSizeFormatted = report.totalSizeFormatted,
                            versionName = currentBuildConfig().apkExportConfig
                                ?.customVersionName?.takeIf { it.isNotBlank() } ?: "1.0.0",
                            versionCode = currentBuildConfig().apkExportConfig?.customVersionCode ?: 1,
                            buildMode = lastBuildMode,
                            buildReason = lastBuildReason
                        )
                    }
                }

                item {
                    PremiumOutlinedButton(
                        onClick = {
                            analysisReport = null
                            lastBuildMode = null
                            lastBuildReason = null
                            buildFailureReport = null
                            cacheMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Autorenew,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(Strings.buildAgain, maxLines = 1)
                    }
                }

                item {
                    WtaCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.PieChart,
                                        null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        Strings.apkAnalysisTitle,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                WtaBadge(
                                    text = report.totalSizeFormatted,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(WtaRadius.Button))
                            ) {
                                report.categories.forEach { cat ->
                                    val segColor = try {
                                        Color(android.graphics.Color.parseColor(cat.category.color))
                                    } catch (_: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(cat.percentage.coerceAtLeast(0.01f))
                                            .fillMaxHeight()
                                            .background(segColor)
                                    )
                                }
                            }

                            report.categories.forEach { cat ->
                                val catColor = try {
                                    Color(android.graphics.Color.parseColor(cat.category.color))
                                } catch (_: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(catColor, RoundedCornerShape(WtaRadius.Button))
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        cat.category.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.weight(weight = 1f, fill = true)
                                    )
                                    Text(
                                        "${com.webtoapp.core.download.DependencyDownloadEngine.formatSize(cat.totalCompressedSize)} · " +
                                            String.format("%.1f%%", cat.percentage),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportAabConfirm) {
        WtaAlertDialog(
            onDismissRequest = { showExportAabConfirm = false },
            icon = Icons.Outlined.PlayCircleOutline,
            title = Strings.playStoreExportAabConfirmTitle,
            text = Strings.playStoreExportAabConfirmBody,
            confirmButton = {
                TextButton(onClick = {
                    showExportAabConfirm = false
                    onExportAab()
                }) { Text(Strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportAabConfirm = false
                }) { Text(Strings.btnCancel) }
            }
        )
    }

    buildFailureReport?.let { report ->
        BuildFailureReportDialog(
            report = report,
            onDismiss = { buildFailureReport = null }
        )
    }
}

private fun resolveBuildIsolationDefault(
    config: com.webtoapp.core.privacy.IsolationConfig?
): com.webtoapp.core.privacy.IsolationConfig {
    return config ?: com.webtoapp.core.privacy.IsolationConfig.DISABLED
}

/**
 * Write the build screen's export config back onto the stored app row, preserving fields
 * the screen doesn't manage. Skips the update entirely when the stored row already
 * matches — [WebAppRepository.updateWebApp] bumps `updatedAt`, so even a no-op write
 * would reshuffle the app list and feed this screen's collector another emission.
 */
internal suspend fun persistBuildScreenExportConfig(
    repository: com.webtoapp.data.repository.WebAppRepository,
    appId: Long,
    exportConfig: com.webtoapp.data.model.ApkExportConfig
) {
    val base = repository.getWebApp(appId) ?: return
    if (base.apkExportConfig == exportConfig) return
    repository.updateWebApp(base.copy(apkExportConfig = exportConfig))
}


@Composable
private fun BuildSummaryCard(
    webApp: WebApp,
    apkFile: java.io.File,
    totalSizeFormatted: String,
    versionName: String,
    versionCode: Int,
    buildMode: String? = null,
    buildReason: String? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            Strings.buildSummaryTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            val iconPath = webApp.iconPath
            if (!iconPath.isNullOrBlank()) {
                AsyncImage(
                    model = iconPath,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(WtaRadius.IconPlate)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(WtaRadius.IconPlate))
                        .background(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = com.webtoapp.ui.design.WtaAlpha.MutedContainer
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Android, null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                webApp.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        BuildSummaryRow(label = Strings.buildSummaryAppSize, value = totalSizeFormatted)
        BuildSummaryRow(label = Strings.buildSummaryVersion, value = "$versionName ($versionCode)")
        if (buildMode != null) {
            BuildSummaryRow(
                label = Strings.buildSummaryMode,
                value = incrementalBuildModeLabel(buildMode)
            )
        }
        if (!buildReason.isNullOrBlank()) {
            BuildSummaryRow(
                label = Strings.buildSummaryReason,
                value = buildReason
            )
        }
        BuildSummaryRow(label = Strings.buildSummaryJdk, value = "17")
        BuildSummaryRow(label = Strings.buildSummarySignature, value = "v1+v2+v3")

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                Strings.buildSummaryLocation,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                apkFile.absolutePath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.FilledTonalButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(apkFile.absolutePath))
                        android.widget.Toast.makeText(
                            context, Strings.copiedToClipboard,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    contentPadding = PaddingValues(
                        horizontal = 12.dp, vertical = 6.dp
                    )
                ) {
                    Icon(Icons.Outlined.ContentCopy, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(Strings.buildSummaryCopyPath, style = MaterialTheme.typography.labelMedium)
                }
                androidx.compose.material3.FilledTonalButton(
                    onClick = { openApkWithChooser(context, apkFile) },
                    contentPadding = PaddingValues(
                        horizontal = 12.dp, vertical = 6.dp
                    )
                ) {
                    Icon(Icons.Outlined.Share, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(Strings.buildSummaryOpenWith, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun BuildSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

private fun openApkWithChooser(context: android.content.Context, apkFile: java.io.File) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", apkFile
        )
        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            clipData = android.content.ClipData.newRawUri(apkFile.name, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = android.content.Intent.createChooser(sendIntent, Strings.buildSummaryOpenWith)
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        AppLogger.e("BuildSummaryCard", "openApkWithChooser failed", e)
        android.widget.Toast.makeText(
            context, e.localizedMessage ?: "Open failed",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun EngineSelectionCard(
    selectedEngine: String,
    isGeckoDownloaded: Boolean,
    onEngineSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(WtaRadius.Control))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                Strings.engineSelectTitle,
                style = MaterialTheme.typography.titleSmall
            )
        }
        Text(
            Strings.engineSelectDesc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WtaRadius.Control))
                .clickable { onEngineSelected("SYSTEM_WEBVIEW") }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedEngine == "SYSTEM_WEBVIEW",
                onClick = { onEngineSelected("SYSTEM_WEBVIEW") }
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(weight = 1f, fill = true)) {
                Text(Strings.engineSystemWebView, style = MaterialTheme.typography.bodyMedium)
                Text(
                    Strings.engineSystemWebViewDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WtaRadius.Control))
                .clickable {
                    if (isGeckoDownloaded) onEngineSelected("GECKOVIEW")
                }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedEngine == "GECKOVIEW",
                onClick = { if (isGeckoDownloaded) onEngineSelected("GECKOVIEW") },
                enabled = isGeckoDownloaded
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(weight = 1f, fill = true)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        Strings.engineGeckoView,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isGeckoDownloaded) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isGeckoDownloaded) {
                        Spacer(Modifier.width(6.dp))
                        WtaBadge(
                            text = Strings.engineReady,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                if (!isGeckoDownloaded) {
                    Text(
                        Strings.engineGeckoNotDownloaded,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        Strings.engineApkSizeWarning.replace("%s", com.webtoapp.core.engine.EngineType.GECKOVIEW.estimatedSizeMb.toString()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun buildBuildFailureReport(
    webApp: WebApp,
    error: BuildResult.Error
): BuildFailureReport {
    return buildActionFailureReport(
        title = Strings.apkBuildFailed,
        stage = error.diagnostic?.stage?.label ?: "apk_build",
        webApp = webApp,
        summary = error.message,
        logPath = error.logPath,
        extraLines = buildDiagnosticLines(error)
    )
}

internal data class BuildFailureReport(
    val title: String,
    val summary: String,
    val details: String
)

internal fun readBuildLogTail(path: String?, maxChars: Int = 20000): String {
    return try {
        val file = path
            ?.takeIf { it.isNotBlank() }
            ?.let { java.io.File(it) }
            ?.takeIf { it.exists() && it.isFile }
            ?: return "<build log unavailable>"
        // Build logs can reach multiple MB; read only the tail instead of loading the
        // whole file into memory just to drop all but the last 20k chars.
        val length = file.length()
        if (length <= maxChars) {
            file.readText()
        } else {
            val start = length - maxChars
            java.io.RandomAccessFile(file, "r").use { raf ->
                raf.seek(start)
                val buffer = ByteArray(maxChars)
                raf.readFully(buffer)
                val tail = String(buffer, Charsets.UTF_8)
                // Cut to the first newline so the report starts at a clean line.
                val firstNl = tail.indexOf('\n')
                if (firstNl >= 0 && firstNl < tail.length - 1) tail.substring(firstNl + 1) else tail
            }
        }
    } catch (e: Exception) {
        AppLogger.e("BuildApkScreen", "读取 APK 构建日志失败", e)
        Strings.readBuildLogFailed.format(e.message ?: "Unknown error")
    }
}

internal fun buildActionFailureReport(
    title: String,
    stage: String,
    webApp: WebApp,
    summary: String,
    logPath: String? = null,
    throwable: Throwable? = null,
    extraLines: List<String> = emptyList()
): BuildFailureReport {
    throwable?.let { AppLogger.e("BuildApkScreen", "$title failed at $stage", it) }
    val buildLog = readBuildLogTail(logPath)

    val details = buildString {
        appendLine(title)
        appendLine("stage: $stage")
        appendLine("summary: $summary")
        appendLine()
        appendLine("project:")
        appendLine("name=${webApp.name}")
        appendLine("appType=${webApp.appType}")
        appendLine("source=${webApp.url}")
        if (extraLines.isNotEmpty()) {
            appendLine()
            appendLine("context:")
            extraLines.forEach { appendLine(it) }
        }
        appendLine()
        appendLine("log_path:")
        appendLine(logPath ?: "<unavailable>")
        appendLine()
        appendLine("build_log:")
        appendLine(buildLog)
        if (throwable != null) {
            appendLine()
            appendLine("exception:")
            appendLine(android.util.Log.getStackTraceString(throwable))
        }
        appendLine()
        appendLine("recent_logs:")
        append(AppLogger.getRecentLogTail())
    }

    return BuildFailureReport(
        title = title,
        summary = summary,
        details = details
    )
}

internal fun incrementalBuildModeLabel(mode: String?): String {
    return when (mode) {
        "CONTENT_OVERLAY" -> Strings.buildModeContentOverlay
        "REUSE_UNSIGNED" -> Strings.buildModeReuseUnsigned
        "FULL" -> Strings.buildModeFull
        else -> mode?.takeIf { it.isNotBlank() } ?: Strings.buildModeFull
    }
}

internal fun buildDiagnosticLines(error: BuildResult.Error): List<String> {
    val diagnostic = error.diagnostic ?: return emptyList()
    return buildList {
        add("failureStage=${diagnostic.stage.name}")
        add("failureCause=${diagnostic.cause.name}")
        diagnostic.details.forEach { (key, value) ->
            add("$key=$value")
        }
    }
}

@Composable
internal fun BuildFailureReportDialog(
    report: BuildFailureReport,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    WtaAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.Build,
        iconTint = MaterialTheme.colorScheme.error,
        title = report.title,
        content = {
            Text(
                report.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                shape = RoundedCornerShape(WtaRadius.Control),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = report.details,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .padding(bottom = 48.dp)
                            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )

                    androidx.compose.material3.FilledTonalButton(
                        onClick = { clipboardManager.setText(AnnotatedString(report.details)) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.copy)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.close)
            }
        }
    )
}
