package com.webtoapp.ui.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.ui.components.PremiumButton

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.download.DependencyDownloadEngine
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.linux.*
import com.webtoapp.core.python.PythonDependencyManager
import com.webtoapp.core.wordpress.WordPressDependencyManager
import com.webtoapp.ui.aicoding.components.MarkdownText
import com.webtoapp.ui.design.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private enum class RuntimeKey { PHP, COMPOSER, PYTHON }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinuxEnvironmentScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val envManager = remember { LinuxEnvironmentManager.getInstance(context) }
    val envState by envManager.state.collectAsStateWithLifecycle()
    val installProgress by envManager.installProgress.collectAsStateWithLifecycle()

    val phpDlState by WordPressDependencyManager.downloadState.collectAsStateWithLifecycle()
    val pythonDlState by PythonDependencyManager.downloadState.collectAsStateWithLifecycle()

    var envInfo by remember { mutableStateOf<EnvironmentInfo?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    val installJobs = remember { mutableStateMapOf<RuntimeKey, Job>() }
    var expandedRuntimes by remember { mutableStateOf(emptySet<RuntimeKey>()) }
    var runtimeError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        envManager.checkEnvironment()
        envInfo = envManager.getEnvironmentInfo()
    }

    fun refreshInfo() {
        scope.launch { envInfo = envManager.getEnvironmentInfo() }
    }

    fun startInstall(key: RuntimeKey, installer: suspend () -> Result<Unit>) {
        runtimeError = null
        expandedRuntimes = expandedRuntimes + key
        val job = scope.launch {
            val r = installer()
            installJobs.remove(key)
            expandedRuntimes = expandedRuntimes - key
            r.fold(
                onSuccess = {
                    refreshInfo()
                    snackbarHostState.showSnackbar("${key.name} ${Strings.installed.lowercase()}")
                },
                onFailure = { error ->
                    val isCancel = error is kotlinx.coroutines.CancellationException
                    if (isCancel) {
                        snackbarHostState.showSnackbar(Strings.downloadCanceled)
                    } else {
                        runtimeError = "${key.name}: ${error.message}"
                        snackbarHostState.showSnackbar("${key.name}: ${error.message}")
                    }
                    refreshInfo()
                }
            )
        }
        installJobs[key] = job
    }

    fun cancelInstall(key: RuntimeKey) {
        installJobs[key]?.cancel()
        installJobs.remove(key)
        expandedRuntimes = expandedRuntimes - key
        DependencyDownloadEngine.cancel()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(Strings.buildEnvironment) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, Strings.back)
                    }
                },
                actions = {
                    if (envInfo != null) {
                        IconButton(onClick = { showResetDialog = true }) {
                            Icon(Icons.Outlined.RestartAlt, Strings.btnReset)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusSection(envState, installProgress) {
                scope.launch {
                    envManager.initialize { _, _ -> }
                    refreshInfo()
                }
            }

            envInfo?.let { info ->
                ToolsCard(
                    info = info,
                    installJobs = installJobs,
                    expandedRuntimes = expandedRuntimes,
                    phpDlState = phpDlState,
                    pythonDlState = pythonDlState,
                    runtimeError = runtimeError,
                    onInstallPhp = {
                        startInstall(RuntimeKey.PHP) {
                            envManager.installPhpRuntime { _, _ -> }
                        }
                    },
                    onInstallComposer = {
                        startInstall(RuntimeKey.COMPOSER) {
                            envManager.installComposer { _, _ -> }
                        }
                    },
                    onInstallPython = {
                        startInstall(RuntimeKey.PYTHON) {
                            envManager.installPythonRuntime { _, _ -> }
                        }
                    },
                    onCancelInstall = ::cancelInstall,
                    onToggleExpand = { key ->
                        expandedRuntimes = if (key in expandedRuntimes) expandedRuntimes - key else expandedRuntimes + key
                    }
                )

                StorageCard(info) { showClearCacheDialog = true }

                CapabilitiesCard()
            }
        }
    }

    if (showResetDialog) {
        WtaAlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = Icons.Outlined.RestartAlt,
            title = Strings.resetEnvironment,
            text = Strings.resetEnvConfirm,
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    scope.launch {
                        envManager.reset()
                        refreshInfo()
                    }
                }) { Text(Strings.btnReset) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(Strings.btnCancel) }
            }
        )
    }

    if (showClearCacheDialog) {
        WtaAlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            icon = Icons.Outlined.CleaningServices,
            title = Strings.clearCacheTitle,
            text = Strings.clearCacheConfirm,
            confirmButton = {
                TextButton(onClick = {
                    showClearCacheDialog = false
                    scope.launch {
                        envManager.clearCache()
                        refreshInfo()
                    }
                }) { Text(Strings.clean) }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) { Text(Strings.btnCancel) }
            }
        )
    }
}

@Composable
private fun StatusSection(
    state: EnvironmentState,
    progress: InstallProgress,
    onInstall: () -> Unit
) {
    val isReady = state is EnvironmentState.Ready
    val isInstalling = state is EnvironmentState.Downloading || state is EnvironmentState.Installing

    WtaSettingCard {
        Column(modifier = Modifier.padding(WtaSpacing.Large)) {
            WtaSettingRow(
                title = when (state) {
                    is EnvironmentState.Ready -> Strings.envReady
                    is EnvironmentState.NotInstalled -> Strings.envNotInstalled
                    is EnvironmentState.NodeInstalledNpmMissing -> Strings.nodeInstalledNpmMissing
                    is EnvironmentState.Downloading -> Strings.envDownloading
                    is EnvironmentState.Installing -> Strings.envInstalling
                    is EnvironmentState.Error -> Strings.envInstallFailed
                    else -> Strings.ready
                },
                subtitle = when (state) {
                    is EnvironmentState.Ready -> Strings.canBuildFrontend
                    is EnvironmentState.NotInstalled -> Strings.builtInPackagerReady
                    is EnvironmentState.NodeInstalledNpmMissing -> Strings.nodeInstalledNpmMissingHint
                    is EnvironmentState.Downloading -> "${state.component} · ${(state.progress * 100).toInt()}%"
                    is EnvironmentState.Installing -> "${state.step} · ${(state.progress * 100).toInt()}%"
                    is EnvironmentState.Error -> state.message
                    else -> Strings.canBuildFrontend
                },
                icon = when {
                    isReady -> Icons.Outlined.CheckCircle
                    isInstalling -> Icons.Outlined.Downloading
                    state is EnvironmentState.Error -> Icons.Outlined.ErrorOutline
                    else -> Icons.Outlined.Build
                },
                trailing = {
                    if (isReady) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = WtaColors.semantic.success,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            AnimatedVisibility(visible = isInstalling) {
                Column(modifier = Modifier.padding(top = WtaSpacing.Medium)) {
                    val progressValue = when (state) {
                        is EnvironmentState.Downloading -> state.progress
                        is EnvironmentState.Installing -> state.progress
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                    if (progress.currentStep.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            progress.currentStep,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(visible = state is EnvironmentState.NotInstalled || state is EnvironmentState.Error) {
                Column(modifier = Modifier.padding(top = WtaSpacing.Medium)) {
                    PremiumButton(
                        onClick = onInstall,
                        enabled = !isInstalling,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (state is EnvironmentState.Error) Icons.Default.Refresh else Icons.Default.Download, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (state is EnvironmentState.Error) Strings.reinstallEsbuild else Strings.installAdvancedBuildTool)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolsCard(
    info: EnvironmentInfo,
    installJobs: Map<RuntimeKey, Job>,
    expandedRuntimes: Set<RuntimeKey>,
    phpDlState: WordPressDependencyManager.DownloadState,
    pythonDlState: PythonDependencyManager.DownloadState,
    runtimeError: String?,
    onInstallPhp: () -> Unit,
    onInstallComposer: () -> Unit,
    onInstallPython: () -> Unit,
    onCancelInstall: (RuntimeKey) -> Unit,
    onToggleExpand: (RuntimeKey) -> Unit
) {
    WtaSettingCard {
        Column(modifier = Modifier.padding(WtaSpacing.Large)) {
            ToolRow(Icons.Outlined.Code, "Node.js", versionStatus(info.nodeReady, info.nodeVersion), info.nodeReady)
            WtaSectionDivider()
            ToolRow(Icons.Outlined.Terminal, "npm", versionStatus(info.npmReady, info.npmVersion), info.npmReady)
            WtaSectionDivider()
            ToolRow(Icons.Outlined.DeveloperMode, "pnpm", versionStatus(info.pnpmReady, info.pnpmVersion), info.pnpmReady)
            WtaSectionDivider()
            ToolRow(Icons.Outlined.Layers, "yarn", versionStatus(info.yarnReady, info.yarnVersion), info.yarnReady)
            WtaSectionDivider()
            ToolRow(Icons.Outlined.Speed, "esbuild", if (info.esbuildAvailable) Strings.installed else Strings.notInstalled, info.esbuildAvailable)
            WtaSectionDivider()

            RuntimeToolRow(
                icon = Icons.Outlined.Code,
                name = "PHP",
                status = versionStatus(info.phpReady, info.phpVersion),
                isAvailable = info.phpReady,
                isInstalling = RuntimeKey.PHP in installJobs,
                isExpanded = RuntimeKey.PHP in expandedRuntimes,
                dlState = phpDlState,
                installLabel = Strings.installPhpRuntime,
                onInstall = onInstallPhp,
                onCancel = { onCancelInstall(RuntimeKey.PHP) },
                onToggleExpand = { onToggleExpand(RuntimeKey.PHP) }
            )
            WtaSectionDivider()

            val composerEnabled = info.phpReady && !info.composerReady
            RuntimeToolRow(
                icon = Icons.Outlined.Inventory2,
                name = "Composer",
                status = when {
                    info.composerReady -> versionStatus(true, info.composerVersion)
                    !info.phpReady -> Strings.composerNeedsPhp
                    else -> Strings.notInstalled
                },
                isAvailable = info.composerReady,
                isInstalling = RuntimeKey.COMPOSER in installJobs,
                isExpanded = RuntimeKey.COMPOSER in expandedRuntimes,
                dlState = null,
                installLabel = Strings.installComposerLabel,
                enabled = composerEnabled,
                onInstall = onInstallComposer,
                onCancel = { onCancelInstall(RuntimeKey.COMPOSER) },
                onToggleExpand = { onToggleExpand(RuntimeKey.COMPOSER) }
            )
            WtaSectionDivider()

            RuntimeToolRow(
                icon = Icons.Outlined.Terminal,
                name = "Python",
                status = versionStatus(info.pythonReady, info.pythonVersion),
                isAvailable = info.pythonReady,
                isInstalling = RuntimeKey.PYTHON in installJobs,
                isExpanded = RuntimeKey.PYTHON in expandedRuntimes,
                dlState = pythonDlState,
                installLabel = Strings.installPythonRuntime,
                onInstall = onInstallPython,
                onCancel = { onCancelInstall(RuntimeKey.PYTHON) },
                onToggleExpand = { onToggleExpand(RuntimeKey.PYTHON) }
            )
            WtaSectionDivider()

            ToolRow(Icons.Outlined.Inventory, "pip", if (info.pipReady) Strings.installed else Strings.notInstalled, info.pipReady)

            runtimeError?.let { msg ->
                Spacer(modifier = Modifier.height(WtaSpacing.Small))
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ToolRow(
    icon: ImageVector,
    name: String,
    status: String,
    isAvailable: Boolean
) {
    WtaSettingRow(
        title = name,
        subtitle = status,
        icon = icon,
        trailing = {
            if (isAvailable) {
                Icon(
                    Icons.Filled.Check,
                    null,
                    tint = WtaColors.semantic.success,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}

@Composable
private fun RuntimeToolRow(
    icon: ImageVector,
    name: String,
    status: String,
    isAvailable: Boolean,
    isInstalling: Boolean,
    isExpanded: Boolean,
    dlState: Any?,
    installLabel: String,
    enabled: Boolean = true,
    onInstall: () -> Unit,
    onCancel: () -> Unit,
    onToggleExpand: () -> Unit
) {
    Column {
        WtaSettingRow(
            title = name,
            subtitle = status,
            icon = icon,
            enabled = enabled || isAvailable,
            trailing = {
                when {
                    isAvailable -> Icon(
                        Icons.Filled.Check,
                        null,
                        tint = WtaColors.semantic.success,
                        modifier = Modifier.size(18.dp)
                    )
                    isInstalling && dlState != null -> {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                Strings.downloading,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onToggleExpand, modifier = Modifier.size(28.dp)) {
                            Icon(
                                if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    isInstalling -> CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    enabled -> TextButton(
                        onClick = onInstall,
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        Text(installLabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        )

        AnimatedVisibility(
            visible = isInstalling && isExpanded && dlState != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            RuntimeDownloadPanel(
                dlState = dlState,
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun RuntimeDownloadPanel(
    dlState: Any?,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = WtaSize.IconPlate + WtaSpacing.IconTextGap,
                top = WtaSpacing.Tiny,
                bottom = WtaSpacing.Small
            )
    ) {
        val (fraction, isIndeterminate, statsText) = extractDlInfo(dlState)

        if (isIndeterminate) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                statsText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onCancel,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Outlined.Cancel, null, Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(Strings.cancel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun extractDlInfo(dlState: Any?): Triple<Float, Boolean, String> {
    return when (dlState) {
        is WordPressDependencyManager.DownloadState.Downloading -> {
            val indeterminate = dlState.totalBytes <= 0
            val fraction = if (indeterminate) 0f else dlState.progress
            val stats = if (indeterminate) {
                formatBytes(dlState.bytesDownloaded)
            } else {
                val percent = (dlState.progress * 100).toInt()
                "${formatBytes(dlState.bytesDownloaded)} / ${formatBytes(dlState.totalBytes)} ($percent%)"
            }
            Triple(fraction, indeterminate, "${dlState.currentFile} · $stats")
        }
        is WordPressDependencyManager.DownloadState.Extracting -> {
            Triple(0f, true, "${Strings.extracting}: ${dlState.fileName}")
        }
        is WordPressDependencyManager.DownloadState.Paused -> {
            val fraction = dlState.progress
            Triple(fraction, false, "${Strings.depDlPaused} · ${formatBytes(dlState.bytesDownloaded)}")
        }
        is WordPressDependencyManager.DownloadState.Error -> {
            Triple(0f, true, dlState.message)
        }
        is PythonDependencyManager.DownloadState.Downloading -> {
            val indeterminate = dlState.totalBytes <= 0
            val fraction = if (indeterminate) 0f else dlState.progress
            val stats = if (indeterminate) {
                formatBytes(dlState.bytesDownloaded)
            } else {
                val percent = (dlState.progress * 100).toInt()
                "${formatBytes(dlState.bytesDownloaded)} / ${formatBytes(dlState.totalBytes)} ($percent%)"
            }
            Triple(fraction, indeterminate, "${dlState.currentFile} · $stats")
        }
        is PythonDependencyManager.DownloadState.Extracting -> {
            Triple(0f, true, "${Strings.extracting}: ${dlState.fileName}")
        }
        is PythonDependencyManager.DownloadState.Paused -> {
            val fraction = dlState.progress
            Triple(fraction, false, "${Strings.depDlPaused} · ${formatBytes(dlState.bytesDownloaded)}")
        }
        is PythonDependencyManager.DownloadState.Error -> {
            Triple(0f, true, dlState.message)
        }
        else -> Triple(0f, true, Strings.downloading)
    }
}

@Composable
private fun StorageCard(info: EnvironmentInfo, onClearCache: () -> Unit) {
    WtaSettingCard {
        Column(modifier = Modifier.padding(WtaSpacing.Large)) {
            WtaSettingRow(
                title = Strings.buildTools,
                subtitle = formatSize(info.storageUsed),
                icon = Icons.Outlined.Storage
            )
            WtaSectionDivider()
            WtaSettingRow(
                title = Strings.cache,
                subtitle = formatSize(info.cacheSize),
                icon = Icons.Outlined.CleaningServices,
                trailing = {
                    if (info.cacheSize > 0) {
                        TextButton(onClick = onClearCache) {
                            Text(Strings.btnClearCache, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun CapabilitiesCard() {
    var expanded by remember { mutableStateOf(false) }

    WtaSettingCard {
        Column(modifier = Modifier.padding(WtaSpacing.Large)) {
            WtaSettingRow(
                title = Strings.supportedFeatures,
                icon = Icons.Outlined.Info,
                onClick = { expanded = !expanded },
                trailing = {
                    Icon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = WtaSpacing.Medium)) {
                    MarkdownText(
                        text = CAPABILITIES_MD,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private const val CAPABILITIES_MD = """
## 能做什么

### 设备内构建
- **前端项目** — React / Vue / Next.js / Nuxt / Angular / Svelte / Vite，自动检测框架并执行构建脚本
- **静态 HTML** — 直接打包，无需构建工具
- **Node.js 应用** — 本地 HTTP server 运行时模式
- **PHP 应用** — 本地 PHP 运行时 + Composer 依赖管理
- **Python 应用** — 本地 Python 运行时 + pip 依赖安装

### 构建能力
- 导入已有项目并自动检测框架
- 支持 Vite / Webpack 等主流打包器
- TypeScript 预编译
- 静态资源处理与 HTML 优化
- esbuild 可选加速（设备内原生编译）
- 构建产物性能优化

### 依赖关系
- **Composer** 需要 **PHP** 运行时先安装
- **Go** 项目需要预构建（设备内 Go 工具链仅支持 arm64-v8a）
"""

private fun versionStatus(ready: Boolean, version: String?): String {
    return if (!ready) Strings.notInstalled else version ?: Strings.installed
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024 * 1024 * 1024 -> String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    else -> String.format(java.util.Locale.getDefault(), "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "${bytes} B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(java.util.Locale.getDefault(), "%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format(java.util.Locale.getDefault(), "%.1f MB", mb)
}
