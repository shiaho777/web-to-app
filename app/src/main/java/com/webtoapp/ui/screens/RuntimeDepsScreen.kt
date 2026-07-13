package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Javascript
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.download.DependencyDownloadEngine
import com.webtoapp.core.golang.GoDependencyManager
import com.webtoapp.core.golang.GoToolchainManager
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.linux.LocalBuildEnvironment
import com.webtoapp.core.nodejs.NodeDependencyManager
import com.webtoapp.core.python.PythonDependencyManager
import com.webtoapp.core.wordpress.WordPressDependencyManager
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaFullEmptyState
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private enum class RuntimeFilter {
    ALL,
    READY,
    MISSING
}

private enum class RuntimeKind {
    PHP,
    WORDPRESS,
    SQLITE,
    NODE,
    PYTHON,
    GO
}

private data class RuntimeEntry(
    val kind: RuntimeKind,
    val title: String,
    val description: String,
    val version: String,
    val icon: ImageVector,
    val accent: Color,
    val isReady: Boolean,
    val cacheSize: Long,
    val projectCount: Int,
    val section: RuntimeSection
)

private enum class RuntimeSection {
    RUNTIME,
    PLUGIN
}

private data class ProjectStat(
    val name: String,
    val count: Int,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RuntimeDepsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var phpReady by remember { mutableStateOf(WordPressDependencyManager.isPhpReady(context)) }
    var wpReady by remember { mutableStateOf(WordPressDependencyManager.isWordPressReady(context)) }
    var sqliteReady by remember { mutableStateOf(WordPressDependencyManager.isSqlitePluginReady(context)) }
    var nodeReady by remember { mutableStateOf(LocalBuildEnvironment.isNpmReady(context)) }
    var pythonReady by remember { mutableStateOf(PythonDependencyManager.isPythonReady(context)) }
    var goReady by remember { mutableStateOf(GoDependencyManager.isGoToolchainReady(context)) }

    var wpCacheSize by remember { mutableLongStateOf(0L) }
    var nodeCacheSize by remember { mutableLongStateOf(0L) }
    var pythonCacheSize by remember { mutableLongStateOf(0L) }
    var goCacheSize by remember { mutableLongStateOf(0L) }

    var wpProjectCount by remember { mutableIntStateOf(0) }
    var nodeProjectCount by remember { mutableIntStateOf(0) }
    var pythonProjectCount by remember { mutableIntStateOf(0) }
    var goProjectCount by remember { mutableIntStateOf(0) }
    var docsProjectCount by remember { mutableIntStateOf(0) }

    var wpMirrorRegion by remember { mutableStateOf(WordPressDependencyManager.getMirrorRegion()) }
    var isDownloading by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadLabel by remember { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    var filterName by rememberSaveable { mutableStateOf(RuntimeFilter.ALL.name) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var clearTarget by remember { mutableStateOf<RuntimeKind?>(null) }

    val engineState by DependencyDownloadEngine.state.collectAsStateWithLifecycle()
    val wpDownloadState by WordPressDependencyManager.downloadState.collectAsStateWithLifecycle()
    val nodeDownloadState by NodeDependencyManager.downloadState.collectAsStateWithLifecycle()
    val pythonDownloadState by PythonDependencyManager.downloadState.collectAsStateWithLifecycle()
    val goDownloadState by GoToolchainManager.downloadState.collectAsStateWithLifecycle()

    suspend fun refreshSizesAndProjects() {
        withContext(Dispatchers.IO) {
            wpCacheSize = WordPressDependencyManager.getCacheSize(context)
            nodeCacheSize = NodeDependencyManager.getCacheSize(context)
            pythonCacheSize = PythonDependencyManager.getCacheSize(context)
            goCacheSize = GoDependencyManager.getCacheSize(context)
            wpProjectCount = countSubdirs(WordPressDependencyManager.getWordPressProjectsDir(context))
            nodeProjectCount = countSubdirs(NodeDependencyManager.getNodeProjectsDir(context))
            pythonProjectCount = countSubdirs(File(context.filesDir, "python_projects"))
            goProjectCount = countSubdirs(File(context.filesDir, "go_projects"))
            docsProjectCount = countSubdirs(File(context.filesDir, "docs_projects"))
        }
    }

    fun refreshReadyFlags() {
        phpReady = WordPressDependencyManager.isPhpReady(context)
        wpReady = WordPressDependencyManager.isWordPressReady(context)
        sqliteReady = WordPressDependencyManager.isSqlitePluginReady(context)
        nodeReady = LocalBuildEnvironment.isNpmReady(context)
        pythonReady = PythonDependencyManager.isPythonReady(context)
        goReady = GoDependencyManager.isGoToolchainReady(context)
    }

    suspend fun fullRefresh() {
        isRefreshing = true
        refreshReadyFlags()
        refreshSizesAndProjects()
        isRefreshing = false
    }

    LaunchedEffect(Unit) {
        fullRefresh()
    }

    LaunchedEffect(engineState) {
        when (val es = engineState) {
            is DependencyDownloadEngine.State.Downloading -> {
                isDownloading = true
                isPaused = false
                downloadProgress = es.progress
                downloadLabel = es.displayName
            }
            is DependencyDownloadEngine.State.Paused -> {
                isDownloading = true
                isPaused = true
                downloadProgress = es.progress
                downloadLabel = es.displayName
            }
            is DependencyDownloadEngine.State.Extracting -> {
                isDownloading = true
                isPaused = false
                downloadLabel = es.displayName
            }
            is DependencyDownloadEngine.State.Complete -> {
                isDownloading = false
                isPaused = false
                refreshReadyFlags()
                refreshSizesAndProjects()
            }
            is DependencyDownloadEngine.State.Error -> {
                isDownloading = false
                isPaused = false
                snackbarHostState.showSnackbar(es.message)
            }
            else -> Unit
        }
    }

    LaunchedEffect(wpDownloadState) {
        when (val s = wpDownloadState) {
            is WordPressDependencyManager.DownloadState.Error -> {
                snackbarHostState.showSnackbar(s.message)
            }
            else -> Unit
        }
    }
    LaunchedEffect(nodeDownloadState) {
        when (val s = nodeDownloadState) {
            is NodeDependencyManager.DownloadState.Error -> {
                snackbarHostState.showSnackbar(s.message)
            }
            else -> Unit
        }
    }
    LaunchedEffect(pythonDownloadState) {
        when (val s = pythonDownloadState) {
            is PythonDependencyManager.DownloadState.Error -> {
                snackbarHostState.showSnackbar(s.message)
            }
            else -> Unit
        }
    }
    LaunchedEffect(goDownloadState) {
        when (val s = goDownloadState) {
            is GoToolchainManager.DownloadState.Error -> {
                snackbarHostState.showSnackbar(s.message)
            }
            else -> Unit
        }
    }

    val totalCacheSize = wpCacheSize + nodeCacheSize + pythonCacheSize + goCacheSize
    val readyCount = listOf(phpReady, wpReady, sqliteReady, nodeReady, pythonReady, goReady).count { it }
    val totalCount = 6
    val allReady = readyCount == totalCount
    val filter = runCatching { RuntimeFilter.valueOf(filterName) }.getOrDefault(RuntimeFilter.ALL)

    val entries = remember(
        phpReady, wpReady, sqliteReady, nodeReady, pythonReady, goReady,
        wpCacheSize, nodeCacheSize, pythonCacheSize, goCacheSize,
        wpProjectCount, nodeProjectCount, pythonProjectCount, goProjectCount
    ) {
        listOf(
            RuntimeEntry(
                kind = RuntimeKind.PHP,
                title = Strings.depPhpRuntime,
                description = Strings.depPhpDesc,
                version = WordPressDependencyManager.PHP_VERSION,
                icon = Icons.Outlined.Code,
                accent = AppColors.Php,
                isReady = phpReady,
                cacheSize = wpCacheSize,
                projectCount = 0,
                section = RuntimeSection.RUNTIME
            ),
            RuntimeEntry(
                kind = RuntimeKind.WORDPRESS,
                title = Strings.depWpCore,
                description = Strings.depWpCoreDesc,
                version = WordPressDependencyManager.WORDPRESS_VERSION,
                icon = Icons.Outlined.Language,
                accent = AppColors.WordPress,
                isReady = wpReady,
                cacheSize = wpCacheSize,
                projectCount = wpProjectCount,
                section = RuntimeSection.RUNTIME
            ),
            RuntimeEntry(
                kind = RuntimeKind.NODE,
                title = Strings.depNodeRuntime,
                description = Strings.depNodeDesc,
                version = NodeDependencyManager.NODE_VERSION,
                icon = Icons.Outlined.Javascript,
                accent = AppColors.NodeJs,
                isReady = nodeReady,
                cacheSize = nodeCacheSize,
                projectCount = nodeProjectCount,
                section = RuntimeSection.RUNTIME
            ),
            RuntimeEntry(
                kind = RuntimeKind.PYTHON,
                title = Strings.depPythonRuntime,
                description = Strings.depPythonDesc,
                version = PythonDependencyManager.PYTHON_VERSION,
                icon = Icons.Outlined.Terminal,
                accent = AppColors.Python,
                isReady = pythonReady,
                cacheSize = pythonCacheSize,
                projectCount = pythonProjectCount,
                section = RuntimeSection.RUNTIME
            ),
            RuntimeEntry(
                kind = RuntimeKind.GO,
                title = Strings.depGoRuntime,
                description = Strings.depGoDesc,
                version = GoToolchainManager.GO_VERSION,
                icon = Icons.Outlined.RocketLaunch,
                accent = AppColors.Go,
                isReady = goReady,
                cacheSize = goCacheSize,
                projectCount = goProjectCount,
                section = RuntimeSection.RUNTIME
            ),
            RuntimeEntry(
                kind = RuntimeKind.SQLITE,
                title = Strings.depSqlitePlugin,
                description = Strings.depSqliteDesc,
                version = WordPressDependencyManager.SQLITE_PLUGIN_VERSION,
                icon = Icons.Outlined.Storage,
                accent = AppColors.SQLite,
                isReady = sqliteReady,
                cacheSize = 0L,
                projectCount = 0,
                section = RuntimeSection.PLUGIN
            )
        )
    }

    val filteredEntries = remember(entries, query, filter) {
        val q = query.trim()
        entries.filter { entry ->
            val statusOk = when (filter) {
                RuntimeFilter.ALL -> true
                RuntimeFilter.READY -> entry.isReady
                RuntimeFilter.MISSING -> !entry.isReady
            }
            if (!statusOk) return@filter false
            if (q.isEmpty()) return@filter true
            entry.title.contains(q, ignoreCase = true) ||
                entry.description.contains(q, ignoreCase = true) ||
                entry.version.contains(q, ignoreCase = true) ||
                entry.kind.name.contains(q, ignoreCase = true)
        }
    }

    val projectStats = remember(wpProjectCount, nodeProjectCount, pythonProjectCount, goProjectCount, docsProjectCount) {
        listOf(
            ProjectStat(Strings.depWpProjects, wpProjectCount, AppColors.WordPress),
            ProjectStat(Strings.depNodeProjects, nodeProjectCount, AppColors.NodeJs),
            ProjectStat(Strings.depPythonProjects, pythonProjectCount, AppColors.Python),
            ProjectStat(Strings.depGoProjects, goProjectCount, AppColors.Go),
            ProjectStat(Strings.depDocsProjects, docsProjectCount, AppColors.NeutralAccent)
        )
    }

    fun applyMirror(region: String) {
        onMirrorChange(region) { wpMirrorRegion = it }
    }

    suspend fun installKind(kind: RuntimeKind): Boolean {
        return when (kind) {
            RuntimeKind.PHP, RuntimeKind.WORDPRESS, RuntimeKind.SQLITE -> {
                WordPressDependencyManager.downloadAllDependencies(context)
            }
            RuntimeKind.NODE -> {
                runCatching {
                    LocalBuildEnvironment.ensureInstalled(context)
                    true
                }.getOrElse { false }
            }
            RuntimeKind.PYTHON -> PythonDependencyManager.downloadPythonRuntime(context)
            RuntimeKind.GO -> GoToolchainManager.installGoToolchain(context)
        }
    }

    suspend fun clearKind(kind: RuntimeKind) {
        withContext(Dispatchers.IO) {
            when (kind) {
                RuntimeKind.PHP, RuntimeKind.WORDPRESS, RuntimeKind.SQLITE -> {
                    WordPressDependencyManager.clearCache(context)
                }
                RuntimeKind.NODE -> NodeDependencyManager.clearCache(context)
                RuntimeKind.PYTHON -> PythonDependencyManager.clearCache(context)
                RuntimeKind.GO -> GoDependencyManager.clearCache(context)
            }
        }
    }

    fun runInstall(kind: RuntimeKind, reinstall: Boolean = false) {
        if (isDownloading) return
        scope.launch {
            isDownloading = true
            isPaused = false
            try {
                if (reinstall) {
                    clearKind(kind)
                    refreshReadyFlags()
                }
                val success = installKind(kind)
                refreshReadyFlags()
                refreshSizesAndProjects()
                snackbarHostState.showSnackbar(
                    if (success) Strings.depInstallSuccess else Strings.depInstallFailed
                )
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: Strings.depInstallFailed)
            } finally {
                isDownloading = false
                isPaused = false
            }
        }
    }

    fun runDownloadAll() {
        if (isDownloading || allReady) return
        scope.launch {
            isDownloading = true
            isPaused = false
            try {
                val wpSuccess = if (!phpReady || !wpReady || !sqliteReady) {
                    WordPressDependencyManager.downloadAllDependencies(context)
                } else true
                val nodeSuccess = if (!nodeReady) {
                    runCatching {
                        LocalBuildEnvironment.ensureInstalled(context)
                        true
                    }.getOrElse { false }
                } else true
                val pythonSuccess = if (!pythonReady) {
                    PythonDependencyManager.downloadPythonRuntime(context)
                } else true
                val goSuccess = if (!goReady) {
                    GoToolchainManager.installGoToolchain(context)
                } else true
                refreshReadyFlags()
                refreshSizesAndProjects()
                if (wpSuccess && nodeSuccess && pythonSuccess && goSuccess) {
                    snackbarHostState.showSnackbar(Strings.depAllReady)
                } else {
                    snackbarHostState.showSnackbar(Strings.depInstallFailed)
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: Strings.depInstallFailed)
            } finally {
                isDownloading = false
                isPaused = false
            }
        }
    }

    WtaScreen(
        title = Strings.runtimeDepsTitle,
        subtitle = Strings.runtimeDepsSubtitle,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    scope.launch { fullRefresh() }
                },
                enabled = !isDownloading && !isRefreshing
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Outlined.Refresh, contentDescription = Strings.refresh)
                }
            }
            if (totalCacheSize > 0) {
                IconButton(
                    onClick = { showClearAllDialog = true },
                    enabled = !isDownloading
                ) {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = Strings.depClearAll)
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatusOverviewCard(
                    readyCount = readyCount,
                    totalCount = totalCount,
                    allReady = allReady,
                    totalCacheSize = totalCacheSize,
                    isDownloading = isDownloading,
                    isPaused = isPaused,
                    downloadProgress = downloadProgress,
                    downloadLabel = downloadLabel,
                    onPause = { DependencyDownloadEngine.pause() },
                    onResume = { DependencyDownloadEngine.resume() },
                    onCancel = { DependencyDownloadEngine.cancel() }
                )
            }

            item {
                WtaTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = Strings.depSearchHint,
                    leadingIcon = Icons.Outlined.Search,
                    singleLine = true
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
                        selected = filter == RuntimeFilter.ALL,
                        onClick = { filterName = RuntimeFilter.ALL.name },
                        label = Strings.filterAll
                    )
                    WtaChip(
                        selected = filter == RuntimeFilter.READY,
                        onClick = { filterName = RuntimeFilter.READY.name },
                        label = Strings.depFilterReady
                    )
                    WtaChip(
                        selected = filter == RuntimeFilter.MISSING,
                        onClick = { filterName = RuntimeFilter.MISSING.name },
                        label = Strings.depFilterMissing
                    )
                }
            }

            item {
                DownloadMirrorCard(
                    mirrorRegion = wpMirrorRegion,
                    allReady = allReady,
                    isDownloading = isDownloading,
                    onMirrorChange = ::applyMirror,
                    onDownloadAll = ::runDownloadAll
                )
            }

            if (filteredEntries.isEmpty()) {
                item {
                    WtaFullEmptyState(
                        title = Strings.depNoMatch,
                        message = Strings.runtimeDepsSubtitle,
                        icon = Icons.Outlined.Storage,
                        fillMaxSize = false
                    )
                }
            } else {
                val runtimes = filteredEntries.filter { it.section == RuntimeSection.RUNTIME }
                val plugins = filteredEntries.filter { it.section == RuntimeSection.PLUGIN }

                if (runtimes.isNotEmpty()) {
                    item {
                        SectionLabel(Strings.depSectionRuntimes)
                    }
                    items(runtimes, key = { it.kind.name }) { entry ->
                        RuntimeEntryCard(
                            entry = entry,
                            busy = isDownloading,
                            onInstall = { runInstall(entry.kind, reinstall = false) },
                            onReinstall = { runInstall(entry.kind, reinstall = true) },
                            onClear = { clearTarget = entry.kind }
                        )
                    }
                }

                if (plugins.isNotEmpty()) {
                    item {
                        SectionLabel(Strings.depSectionRuntimePlugins)
                    }
                    items(plugins, key = { it.kind.name }) { entry ->
                        RuntimeEntryCard(
                            entry = entry,
                            busy = isDownloading,
                            onInstall = { runInstall(entry.kind, reinstall = false) },
                            onReinstall = { runInstall(entry.kind, reinstall = true) },
                            onClear = { clearTarget = entry.kind }
                        )
                    }
                }
            }

            item {
                SectionLabel(Strings.depSectionProjects)
            }
            item {
                ProjectsCard(projectStats)
            }

            item {
                SectionLabel(Strings.depSectionStorage)
            }
            item {
                StorageCard(
                    wpCacheSize = wpCacheSize,
                    nodeCacheSize = nodeCacheSize,
                    pythonCacheSize = pythonCacheSize,
                    goCacheSize = goCacheSize,
                    totalSize = totalCacheSize,
                    enabled = !isDownloading && totalCacheSize > 0,
                    onClearTarget = { kind -> clearTarget = kind },
                    onClearAll = { showClearAllDialog = true }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            icon = { Icon(Icons.Outlined.DeleteSweep, null) },
            title = { Text(Strings.depClearAll) },
            text = { Text(Strings.depClearConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllDialog = false
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                WordPressDependencyManager.clearCache(context)
                                NodeDependencyManager.clearCache(context)
                                PythonDependencyManager.clearCache(context)
                                GoDependencyManager.clearCache(context)
                            }
                            refreshReadyFlags()
                            refreshSizesAndProjects()
                            snackbarHostState.showSnackbar(Strings.depClearDone)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.btnConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }

    clearTarget?.let { kind ->
        val label = when (kind) {
            RuntimeKind.PHP, RuntimeKind.WORDPRESS, RuntimeKind.SQLITE -> "WordPress"
            RuntimeKind.NODE -> "Node.js"
            RuntimeKind.PYTHON -> "Python"
            RuntimeKind.GO -> "Go"
        }
        AlertDialog(
            onDismissRequest = { clearTarget = null },
            icon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(Strings.depClearRuntime) },
            text = { Text(Strings.depConfirmClearCache(label)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = kind
                        clearTarget = null
                        scope.launch {
                            clearKind(target)
                            refreshReadyFlags()
                            refreshSizesAndProjects()
                            snackbarHostState.showSnackbar(Strings.depClearDone)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { clearTarget = null }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }
}

private fun onMirrorChange(
    region: String,
    setUiRegion: (WordPressDependencyManager.MirrorRegion) -> Unit
) {
    val wpRegion = when (region) {
        "cn" -> WordPressDependencyManager.MirrorRegion.CN
        "global" -> WordPressDependencyManager.MirrorRegion.GLOBAL
        else -> null
    }
    val nodeRegion = when (region) {
        "cn" -> NodeDependencyManager.MirrorRegion.CN
        "global" -> NodeDependencyManager.MirrorRegion.GLOBAL
        else -> null
    }
    val pythonRegion = when (region) {
        "cn" -> PythonDependencyManager.MirrorRegion.CN
        "global" -> PythonDependencyManager.MirrorRegion.GLOBAL
        else -> null
    }
    WordPressDependencyManager.setMirrorRegion(wpRegion)
    NodeDependencyManager.setMirrorRegion(nodeRegion)
    PythonDependencyManager.setMirrorRegion(pythonRegion)
    setUiRegion(WordPressDependencyManager.getMirrorRegion())
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun StatusOverviewCard(
    readyCount: Int,
    totalCount: Int,
    allReady: Boolean,
    totalCacheSize: Long,
    isDownloading: Boolean,
    isPaused: Boolean,
    downloadProgress: Float,
    downloadLabel: String,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    WtaCard(tone = WtaCardTone.Highlighted) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDownloading && !isPaused) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = when {
                                isPaused -> Icons.Filled.Pause
                                allReady -> Icons.Filled.CheckCircle
                                else -> Icons.Outlined.Speed
                            },
                            contentDescription = null,
                            tint = if (allReady) WtaColors.semantic.success
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (allReady) Strings.depAllReady else Strings.depSomeNotReady,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = Strings.depReadyOfTotal(readyCount, totalCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatSize(totalCacheSize),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = Strings.depTotalStorage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isDownloading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isPaused) {
                            "${Strings.depDlPaused} · $downloadLabel"
                        } else {
                            downloadLabel.ifBlank { Strings.depStatusDownloading }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    LinearProgressIndicator(
                        progress = { downloadProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WtaButton(
                            onClick = { if (isPaused) onResume() else onPause() },
                            text = if (isPaused) Strings.depDlResume else Strings.depDlPause,
                            variant = WtaButtonVariant.Tonal,
                            size = WtaButtonSize.Small,
                            leadingIcon = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            modifier = Modifier.weight(1f)
                        )
                        WtaButton(
                            onClick = onCancel,
                            text = Strings.depDlCancel,
                            variant = WtaButtonVariant.Outlined,
                            size = WtaButtonSize.Small,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadMirrorCard(
    mirrorRegion: WordPressDependencyManager.MirrorRegion?,
    allReady: Boolean,
    isDownloading: Boolean,
    onMirrorChange: (String) -> Unit,
    onDownloadAll: () -> Unit
) {
    val isCn = mirrorRegion == WordPressDependencyManager.MirrorRegion.CN
    val isGlobal = mirrorRegion == WordPressDependencyManager.MirrorRegion.GLOBAL
    val isAuto = !isCn && !isGlobal

    WtaCard(tone = WtaCardTone.Surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = Strings.depMirrorSource,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = Strings.depMirrorDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WtaChip(
                    selected = isCn,
                    onClick = { onMirrorChange("cn") },
                    label = Strings.depMirrorCN,
                    enabled = !isDownloading
                )
                WtaChip(
                    selected = isGlobal,
                    onClick = { onMirrorChange("global") },
                    label = Strings.depMirrorGlobal,
                    enabled = !isDownloading
                )
                WtaChip(
                    selected = isAuto,
                    onClick = { onMirrorChange("auto") },
                    label = Strings.depMirrorAuto,
                    enabled = !isDownloading
                )
            }

            if (!allReady) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                WtaButton(
                    onClick = onDownloadAll,
                    text = if (isDownloading) Strings.depStatusDownloading else Strings.depDownloadAll,
                    variant = WtaButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isDownloading,
                    leadingIcon = if (isDownloading) null else Icons.Outlined.CloudDownload
                )
            }
        }
    }
}

@Composable
private fun RuntimeEntryCard(
    entry: RuntimeEntry,
    busy: Boolean,
    onInstall: () -> Unit,
    onReinstall: () -> Unit,
    onClear: () -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(entry.accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        entry.icon,
                        contentDescription = null,
                        tint = entry.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(8.dp))
                        StatusBadge(
                            text = if (entry.isReady) Strings.depStatusReady else Strings.depStatusNotInstalled,
                            ready = entry.isReady
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetaChip("v${entry.version}")
                        if (entry.cacheSize > 0) {
                            MetaChip(formatSize(entry.cacheSize))
                        }
                        if (entry.projectCount > 0) {
                            MetaChip(Strings.depProjectCount(entry.projectCount))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!entry.isReady) {
                    WtaButton(
                        onClick = onInstall,
                        text = Strings.depInstall,
                        variant = WtaButtonVariant.Tonal,
                        size = WtaButtonSize.Small,
                        enabled = !busy,
                        leadingIcon = Icons.Filled.Download,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    WtaButton(
                        onClick = onReinstall,
                        text = Strings.depReinstall,
                        variant = WtaButtonVariant.Tonal,
                        size = WtaButtonSize.Small,
                        enabled = !busy,
                        leadingIcon = Icons.Outlined.Refresh,
                        modifier = Modifier.weight(1f)
                    )
                    WtaButton(
                        onClick = onClear,
                        text = Strings.depClearRuntime,
                        variant = WtaButtonVariant.Outlined,
                        size = WtaButtonSize.Small,
                        enabled = !busy,
                        leadingIcon = Icons.Outlined.Delete,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, ready: Boolean) {
    val container = if (ready) {
        AppColors.Success.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val content = if (ready) {
        AppColors.Success
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = container,
        contentColor = content
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun MetaChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ProjectsCard(stats: List<ProjectStat>) {
    WtaCard(tone = WtaCardTone.Surface) {
        Column(modifier = Modifier.fillMaxWidth()) {
            stats.forEachIndexed { index, stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(stat.color)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stat.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = Strings.depProjectCount(stat.count),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (index < stats.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StorageCard(
    wpCacheSize: Long,
    nodeCacheSize: Long,
    pythonCacheSize: Long,
    goCacheSize: Long,
    totalSize: Long,
    enabled: Boolean,
    onClearTarget: (RuntimeKind) -> Unit,
    onClearAll: () -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.depTotalStorage,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatSize(totalSize),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (totalSize > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                ) {
                    listOf(
                        wpCacheSize to AppColors.WordPress,
                        nodeCacheSize to AppColors.NodeJs,
                        pythonCacheSize to AppColors.Python,
                        goCacheSize to AppColors.Go
                    ).filter { it.first > 0 }.forEach { (size, color) ->
                        val fraction = (size.toFloat() / totalSize).coerceAtLeast(0.01f)
                        Box(
                            modifier = Modifier
                                .weight(fraction, true)
                                .fillMaxHeight()
                                .background(color)
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (wpCacheSize > 0) {
                        StorageLegendItem("WordPress", wpCacheSize, AppColors.WordPress)
                    }
                    if (nodeCacheSize > 0) {
                        StorageLegendItem("Node.js", nodeCacheSize, AppColors.NodeJs)
                    }
                    if (pythonCacheSize > 0) {
                        StorageLegendItem("Python", pythonCacheSize, AppColors.Python)
                    }
                    if (goCacheSize > 0) {
                        StorageLegendItem("Go", goCacheSize, AppColors.Go)
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (wpCacheSize > 0) {
                        WtaButton(
                            onClick = { onClearTarget(RuntimeKind.WORDPRESS) },
                            text = "WordPress · ${formatSize(wpCacheSize)}",
                            variant = WtaButtonVariant.Outlined,
                            size = WtaButtonSize.Small,
                            enabled = enabled
                        )
                    }
                    if (nodeCacheSize > 0) {
                        WtaButton(
                            onClick = { onClearTarget(RuntimeKind.NODE) },
                            text = "Node.js · ${formatSize(nodeCacheSize)}",
                            variant = WtaButtonVariant.Outlined,
                            size = WtaButtonSize.Small,
                            enabled = enabled
                        )
                    }
                    if (pythonCacheSize > 0) {
                        WtaButton(
                            onClick = { onClearTarget(RuntimeKind.PYTHON) },
                            text = "Python · ${formatSize(pythonCacheSize)}",
                            variant = WtaButtonVariant.Outlined,
                            size = WtaButtonSize.Small,
                            enabled = enabled
                        )
                    }
                    if (goCacheSize > 0) {
                        WtaButton(
                            onClick = { onClearTarget(RuntimeKind.GO) },
                            text = "Go · ${formatSize(goCacheSize)}",
                            variant = WtaButtonVariant.Outlined,
                            size = WtaButtonSize.Small,
                            enabled = enabled
                        )
                    }
                }
            } else {
                Text(
                    text = Strings.depStorageEmpty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            WtaButton(
                onClick = onClearAll,
                text = Strings.depClearAll,
                variant = WtaButtonVariant.Outlined,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                leadingIcon = Icons.Outlined.DeleteForever
            )
        }
    }
}

@Composable
private fun StorageLegendItem(label: String, size: Long, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$label ${formatSize(size)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024L * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.getDefault(), "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun countSubdirs(dir: File): Int {
    if (!dir.exists()) return 0
    return dir.listFiles()?.count { it.isDirectory } ?: 0
}
