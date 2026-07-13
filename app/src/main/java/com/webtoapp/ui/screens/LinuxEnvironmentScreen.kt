package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.download.DependencyDownloadEngine
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.linux.EnvironmentInfo
import com.webtoapp.core.linux.EnvironmentState
import com.webtoapp.core.linux.InstallProgress
import com.webtoapp.core.linux.LinuxEnvironmentManager
import com.webtoapp.core.python.PythonDependencyManager
import com.webtoapp.core.wordpress.WordPressDependencyManager
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

private enum class OptionalRuntime {
    PHP,
    COMPOSER,
    PYTHON
}

@Composable
fun LinuxEnvironmentScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val envManager = remember { LinuxEnvironmentManager.getInstance(context) }
    val envState by envManager.state.collectAsStateWithLifecycle()
    val installProgress by envManager.installProgress.collectAsStateWithLifecycle()
    val phpDlState by WordPressDependencyManager.downloadState.collectAsStateWithLifecycle()
    val pythonDlState by PythonDependencyManager.downloadState.collectAsStateWithLifecycle()

    var envInfo by remember { mutableStateOf<EnvironmentInfo?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var runtimeError by remember { mutableStateOf<String?>(null) }
    val installJobs = remember { mutableStateMapOf<OptionalRuntime, Job>() }

    suspend fun refresh() {
        isRefreshing = true
        envManager.checkEnvironment()
        envInfo = envManager.getEnvironmentInfo()
        isRefreshing = false
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    fun startOptionalInstall(
        key: OptionalRuntime,
        label: String,
        installer: suspend () -> Result<Unit>
    ) {
        if (installJobs.containsKey(key)) return
        runtimeError = null
        val job = scope.launch {
            val result = installer()
            installJobs.remove(key)
            result.fold(
                onSuccess = {
                    envInfo = envManager.getEnvironmentInfo()
                    snackbarHostState.showSnackbar(Strings.linuxEnvInstalledToast(label))
                },
                onFailure = { error ->
                    val canceled = error is kotlinx.coroutines.CancellationException
                    if (canceled) {
                        snackbarHostState.showSnackbar(Strings.downloadCanceled)
                    } else {
                        val message = error.message ?: Strings.unknownError
                        runtimeError = Strings.linuxEnvInstallFailedToast(label, message)
                        snackbarHostState.showSnackbar(runtimeError!!)
                    }
                    envInfo = envManager.getEnvironmentInfo()
                }
            )
        }
        installJobs[key] = job
    }

    fun cancelOptionalInstall(key: OptionalRuntime) {
        installJobs[key]?.cancel()
        installJobs.remove(key)
        DependencyDownloadEngine.cancel()
    }

    val isCoreBusy = envState is EnvironmentState.Downloading || envState is EnvironmentState.Installing
    val info = envInfo

    WtaScreen(
        title = Strings.menuLinuxEnvironment,
        subtitle = Strings.linuxEnvSubtitle,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        actions = {
            IconButton(
                onClick = { scope.launch { refresh() } },
                enabled = !isCoreBusy && !isRefreshing
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.Refresh, contentDescription = Strings.refresh)
                }
            }
            if (info != null) {
                IconButton(
                    onClick = { showResetDialog = true },
                    enabled = !isCoreBusy && installJobs.isEmpty()
                ) {
                    Icon(Icons.Outlined.RestartAlt, contentDescription = Strings.btnReset)
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ReadinessHero(
                    state = envState,
                    progress = installProgress,
                    info = info,
                    busy = isCoreBusy,
                    onInstallCore = {
                        scope.launch {
                            val result = envManager.initialize { _, _ -> }
                            envInfo = envManager.getEnvironmentInfo()
                            result.fold(
                                onSuccess = {
                                    snackbarHostState.showSnackbar(Strings.envReady)
                                },
                                onFailure = { error ->
                                    snackbarHostState.showSnackbar(
                                        error.message ?: Strings.unknownError
                                    )
                                }
                            )
                        }
                    }
                )
            }

            if (info != null) {
                item { SectionHeader(Strings.linuxEnvCoreTools, Strings.linuxEnvCoreHint) }
                item {
                    CoreToolchainCard(info = info)
                }

                item { SectionHeader(Strings.linuxEnvPackageManagers, Strings.linuxEnvPmHint) }
                item {
                    PackageManagerMosaic(info = info)
                }

                item { SectionHeader(Strings.linuxEnvOptionalRuntimes, Strings.linuxEnvOptionalHint) }
                item {
                    OptionalRuntimesCard(
                        info = info,
                        installJobs = installJobs,
                        phpDlState = phpDlState,
                        pythonDlState = pythonDlState,
                        runtimeError = runtimeError,
                        onInstallPhp = {
                            startOptionalInstall(OptionalRuntime.PHP, "PHP") {
                                envManager.installPhpRuntime { _, _ -> }
                            }
                        },
                        onInstallComposer = {
                            startOptionalInstall(OptionalRuntime.COMPOSER, "Composer") {
                                envManager.installComposer { _, _ -> }
                            }
                        },
                        onInstallPython = {
                            startOptionalInstall(OptionalRuntime.PYTHON, "Python") {
                                envManager.installPythonRuntime { _, _ -> }
                            }
                        },
                        onCancel = ::cancelOptionalInstall
                    )
                }

                item { SectionHeader(Strings.linuxEnvMaintenance, null) }
                item {
                    MaintenanceCard(
                        info = info,
                        enabled = !isCoreBusy && installJobs.isEmpty(),
                        onClearCache = { showClearCacheDialog = true },
                        onReset = { showResetDialog = true }
                    )
                }

                item { SectionHeader(Strings.linuxEnvCapabilities, null) }
                item { CapabilitiesBoard() }
            } else {
                item {
                    WtaCard(tone = WtaCardTone.Surface, contentPadding = PaddingValues(0.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                Strings.preparingBuildEnv,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(28.dp)) }
        }
    }

    if (showResetDialog) {
        WtaAlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = Icons.Outlined.RestartAlt,
            iconTint = MaterialTheme.colorScheme.error,
            title = Strings.resetEnvironment,
            text = Strings.resetEnvConfirm,
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        scope.launch {
                            envManager.reset()
                            envInfo = envManager.getEnvironmentInfo()
                            snackbarHostState.showSnackbar(Strings.linuxEnvResetDone)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.btnReset)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(Strings.btnCancel)
                }
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
                TextButton(
                    onClick = {
                        showClearCacheDialog = false
                        scope.launch {
                            val result = envManager.clearCache()
                            envInfo = envManager.getEnvironmentInfo()
                            result.fold(
                                onSuccess = { freed ->
                                    snackbarHostState.showSnackbar(
                                        Strings.linuxEnvCacheFreed(formatSize(freed))
                                    )
                                },
                                onFailure = { error ->
                                    snackbarHostState.showSnackbar(
                                        error.message ?: Strings.unknownError
                                    )
                                }
                            )
                        }
                    }
                ) {
                    Text(Strings.clean)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, hint: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!hint.isNullOrBlank()) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReadinessHero(
    state: EnvironmentState,
    progress: InstallProgress,
    info: EnvironmentInfo?,
    busy: Boolean,
    onInstallCore: () -> Unit
) {
    val coreReady = info?.let { it.nodeReady && it.npmReady } == true
    val optionalReady = listOfNotNull(
        info?.phpReady,
        info?.composerReady,
        info?.pythonReady,
        info?.pnpmReady,
        info?.yarnReady,
        info?.esbuildAvailable
    ).count { it }
    val optionalTotal = 6
    val score = when {
        info == null -> 0f
        coreReady -> 0.55f + (optionalReady / optionalTotal.toFloat()) * 0.45f
        info.nodeReady -> 0.28f
        else -> 0.08f
    }
    val animatedScore by animateFloatAsState(
        targetValue = if (busy) progress.progress.coerceIn(0.05f, 0.95f) else score,
        label = "readiness"
    )

    val title = when (state) {
        is EnvironmentState.Ready -> Strings.envReady
        is EnvironmentState.NotInstalled, is EnvironmentState.NodeNotInstalled -> Strings.envNotInstalled
        is EnvironmentState.NodeInstalledNpmMissing -> Strings.nodeInstalledNpmMissing
        is EnvironmentState.Downloading -> Strings.envDownloading
        is EnvironmentState.Installing -> Strings.envInstalling
        is EnvironmentState.Error -> Strings.envInstallFailed
    }
    val subtitle = when (state) {
        is EnvironmentState.Ready -> Strings.canBuildFrontend
        is EnvironmentState.NotInstalled, is EnvironmentState.NodeNotInstalled -> Strings.builtInPackagerReady
        is EnvironmentState.NodeInstalledNpmMissing -> Strings.nodeInstalledNpmMissingHint
        is EnvironmentState.Downloading -> "${state.component} · ${(state.progress * 100).toInt()}%"
        is EnvironmentState.Installing -> state.step.ifBlank { progress.currentStep }
        is EnvironmentState.Error -> state.message
    }
    val ringColor = when {
        state is EnvironmentState.Error -> MaterialTheme.colorScheme.error
        coreReady -> WtaColors.semantic.success
        busy -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    WtaCard(tone = WtaCardTone.Highlighted, contentPadding = PaddingValues(0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReadinessRing(
                    progress = animatedScore,
                    color = ringColor,
                    busy = busy,
                    coreReady = coreReady
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (info != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = Strings.linuxEnvReadinessScore(
                                (if (info.nodeReady) 1 else 0) + (if (info.npmReady) 1 else 0) + optionalReady,
                                2 + optionalTotal
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = busy,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { progress.progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                    if (progress.currentStep.isNotBlank()) {
                        Text(
                            text = progress.currentStep,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (info != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricPill(
                        label = Strings.buildTools,
                        value = formatSize(info.storageUsed),
                        modifier = Modifier.weight(1f)
                    )
                    MetricPill(
                        label = Strings.cache,
                        value = formatSize(info.cacheSize),
                        modifier = Modifier.weight(1f)
                    )
                    MetricPill(
                        label = Strings.linuxEnvOptionalShort,
                        value = "$optionalReady/$optionalTotal",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!coreReady || state is EnvironmentState.Error || state is EnvironmentState.NodeInstalledNpmMissing) {
                WtaButton(
                    onClick = onInstallCore,
                    text = when {
                        state is EnvironmentState.Error -> Strings.linuxEnvRepair
                        state is EnvironmentState.NodeInstalledNpmMissing -> Strings.linuxEnvRepair
                        busy -> Strings.envInstalling
                        else -> Strings.linuxEnvInstallCore
                    },
                    variant = WtaButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                    leadingIcon = when {
                        state is EnvironmentState.Error || state is EnvironmentState.NodeInstalledNpmMissing -> Icons.Outlined.Refresh
                        else -> Icons.Outlined.RocketLaunch
                    }
                )
            }
        }
    }
}

@Composable
private fun ReadinessRing(
    progress: Float,
    color: Color,
    busy: Boolean,
    coreReady: Boolean
) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(
        modifier = Modifier.size(84.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2f
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        when {
            busy -> CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp,
                color = color
            )
            coreReady -> Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(30.dp)
            )
            else -> Icon(
                Icons.Outlined.Build,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CoreToolchainCard(info: EnvironmentInfo) {
    WtaCard(tone = WtaCardTone.Surface, contentPadding = PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CoreToolRow(
                icon = Icons.Outlined.Code,
                name = "Node.js",
                version = info.nodeVersion,
                ready = info.nodeReady,
                role = Strings.linuxEnvRoleRuntime
            )
            DividerLine()
            CoreToolRow(
                icon = Icons.Outlined.Terminal,
                name = "npm",
                version = info.npmVersion,
                ready = info.npmReady,
                role = Strings.linuxEnvRolePackageManager
            )
        }
    }
}

@Composable
private fun CoreToolRow(
    icon: ImageVector,
    name: String,
    version: String?,
    ready: Boolean,
    role: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (ready) WtaColors.semantic.success.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (ready) WtaColors.semantic.success else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (ready) (version ?: Strings.installed) else Strings.notInstalled,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = role,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
        StatusDot(ready = ready)
    }
}

@Composable
private fun StatusDot(ready: Boolean) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (ready) WtaColors.semantic.success.copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (ready) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = WtaColors.semantic.success
                )
            }
            Text(
                text = if (ready) Strings.linuxEnvToolReady else Strings.linuxEnvToolMissing,
                style = MaterialTheme.typography.labelSmall,
                color = if (ready) WtaColors.semantic.success else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PackageManagerMosaic(info: EnvironmentInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolTile(
                name = "pnpm",
                version = info.pnpmVersion,
                ready = info.pnpmReady,
                icon = Icons.Outlined.DeveloperMode,
                modifier = Modifier.weight(1f)
            )
            ToolTile(
                name = "yarn",
                version = info.yarnVersion,
                ready = info.yarnReady,
                icon = Icons.Outlined.Layers,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolTile(
                name = "esbuild",
                version = null,
                ready = info.esbuildAvailable,
                icon = Icons.Outlined.Speed,
                subtitle = if (info.esbuildAvailable) Strings.linuxEnvEsbuildReady else Strings.linuxEnvEsbuildMissing,
                modifier = Modifier.weight(1f)
            )
            ToolTile(
                name = "npm",
                version = info.npmVersion,
                ready = info.npmReady,
                icon = Icons.Outlined.Terminal,
                subtitle = Strings.linuxEnvNpmDefault,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToolTile(
    name: String,
    version: String?,
    ready: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    WtaCard(
        modifier = modifier,
        tone = WtaCardTone.Surface,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (ready) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (ready) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (ready) WtaColors.semantic.success
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = subtitle
                    ?: if (ready) (version ?: Strings.installed) else Strings.notInstalled,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OptionalRuntimesCard(
    info: EnvironmentInfo,
    installJobs: Map<OptionalRuntime, Job>,
    phpDlState: WordPressDependencyManager.DownloadState,
    pythonDlState: PythonDependencyManager.DownloadState,
    runtimeError: String?,
    onInstallPhp: () -> Unit,
    onInstallComposer: () -> Unit,
    onInstallPython: () -> Unit,
    onCancel: (OptionalRuntime) -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface, contentPadding = PaddingValues(0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            OptionalRuntimeBlock(
                icon = Icons.Outlined.Code,
                name = "PHP",
                detail = versionLine(info.phpReady, info.phpVersion),
                ready = info.phpReady,
                installing = OptionalRuntime.PHP in installJobs,
                progress = progressFromWp(phpDlState),
                actionLabel = Strings.installPhpRuntime,
                enabled = true,
                onInstall = onInstallPhp,
                onCancel = { onCancel(OptionalRuntime.PHP) }
            )
            DividerLine()
            OptionalRuntimeBlock(
                icon = Icons.Outlined.Inventory2,
                name = "Composer",
                detail = when {
                    info.composerReady -> versionLine(true, info.composerVersion)
                    !info.phpReady -> Strings.composerNeedsPhp
                    else -> Strings.notInstalled
                },
                ready = info.composerReady,
                installing = OptionalRuntime.COMPOSER in installJobs,
                progress = null,
                actionLabel = Strings.installComposerLabel,
                enabled = info.phpReady && !info.composerReady,
                locked = !info.phpReady,
                onInstall = onInstallComposer,
                onCancel = { onCancel(OptionalRuntime.COMPOSER) }
            )
            DividerLine()
            OptionalRuntimeBlock(
                icon = Icons.Outlined.Terminal,
                name = "Python",
                detail = versionLine(info.pythonReady, info.pythonVersion),
                ready = info.pythonReady,
                installing = OptionalRuntime.PYTHON in installJobs,
                progress = progressFromPython(pythonDlState),
                actionLabel = Strings.installPythonRuntime,
                enabled = true,
                onInstall = onInstallPython,
                onCancel = { onCancel(OptionalRuntime.PYTHON) }
            )
            DividerLine()
            OptionalRuntimeBlock(
                icon = Icons.Outlined.Layers,
                name = "pip",
                detail = if (info.pipReady) Strings.installed else Strings.linuxEnvPipFollowsPython,
                ready = info.pipReady,
                installing = false,
                progress = null,
                actionLabel = "",
                enabled = false,
                showAction = false,
                onInstall = {},
                onCancel = {}
            )

            runtimeError?.let { msg ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private data class InstallProgressUi(
    val fraction: Float,
    val indeterminate: Boolean,
    val label: String
)

@Composable
private fun OptionalRuntimeBlock(
    icon: ImageVector,
    name: String,
    detail: String,
    ready: Boolean,
    installing: Boolean,
    progress: InstallProgressUi?,
    actionLabel: String,
    enabled: Boolean,
    locked: Boolean = false,
    showAction: Boolean = true,
    onInstall: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            ready -> WtaColors.semantic.success.copy(alpha = 0.12f)
                            locked -> MaterialTheme.colorScheme.surfaceContainerHighest
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (locked) Icons.Outlined.Lock else icon,
                    contentDescription = null,
                    tint = when {
                        ready -> WtaColors.semantic.success
                        locked -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            when {
                ready -> StatusDot(ready = true)
                installing -> {
                    WtaButton(
                        onClick = onCancel,
                        text = Strings.depDlCancel,
                        variant = WtaButtonVariant.Outlined,
                        size = WtaButtonSize.Small
                    )
                }
                showAction && enabled -> {
                    WtaButton(
                        onClick = onInstall,
                        text = actionLabel,
                        variant = WtaButtonVariant.Tonal,
                        size = WtaButtonSize.Small
                    )
                }
                locked -> {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = Strings.linuxEnvLocked,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = installing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (progress == null || progress.indeterminate) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                } else {
                    LinearProgressIndicator(
                        progress = { progress.fraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                }
                Text(
                    text = progress?.label ?: Strings.linuxEnvInstallingTool(name),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MaintenanceCard(
    info: EnvironmentInfo,
    enabled: Boolean,
    onClearCache: () -> Unit,
    onReset: () -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface, contentPadding = PaddingValues(0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StorageStat(
                    icon = Icons.Outlined.Storage,
                    title = Strings.buildTools,
                    value = formatSize(info.storageUsed),
                    modifier = Modifier.weight(1f)
                )
                StorageStat(
                    icon = Icons.Outlined.CleaningServices,
                    title = Strings.cache,
                    value = formatSize(info.cacheSize),
                    modifier = Modifier.weight(1f)
                )
            }

            WtaButton(
                onClick = onClearCache,
                text = Strings.btnClearCache,
                variant = WtaButtonVariant.Tonal,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled && info.cacheSize > 0,
                leadingIcon = Icons.Outlined.CleaningServices
            )
            WtaButton(
                onClick = onReset,
                text = Strings.resetEnvironment,
                variant = WtaButtonVariant.Outlined,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                leadingIcon = Icons.Outlined.RestartAlt
            )
        }
    }
}

@Composable
private fun StorageStat(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CapabilitiesBoard() {
    val items = listOf(
        Strings.linuxEnvCapFrontend,
        Strings.linuxEnvCapStatic,
        Strings.linuxEnvCapNode,
        Strings.linuxEnvCapPhp,
        Strings.linuxEnvCapPython,
        Strings.linuxEnvCapEsbuild,
        Strings.linuxEnvCapAutoDetect,
        Strings.linuxEnvCapComposerDep
    )
    WtaCard(tone = WtaCardTone.Surface, contentPadding = PaddingValues(0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = Strings.supportedFeatures,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Text(
                text = Strings.linuxEnvCapFooter,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    )
}

private fun versionLine(ready: Boolean, version: String?): String {
    return if (!ready) Strings.notInstalled else version ?: Strings.installed
}

private fun progressFromWp(state: WordPressDependencyManager.DownloadState): InstallProgressUi? {
    return when (state) {
        is WordPressDependencyManager.DownloadState.Downloading -> {
            val indeterminate = state.totalBytes <= 0
            val label = if (indeterminate) {
                "${state.currentFile} · ${formatBytes(state.bytesDownloaded)}"
            } else {
                val percent = (state.progress * 100).toInt()
                "${state.currentFile} · ${formatBytes(state.bytesDownloaded)} / ${formatBytes(state.totalBytes)} ($percent%)"
            }
            InstallProgressUi(
                fraction = if (indeterminate) 0f else state.progress,
                indeterminate = indeterminate,
                label = label
            )
        }
        is WordPressDependencyManager.DownloadState.Extracting -> {
            InstallProgressUi(0f, true, "${Strings.extracting}: ${state.fileName}")
        }
        is WordPressDependencyManager.DownloadState.Paused -> {
            InstallProgressUi(state.progress, false, "${Strings.depDlPaused} · ${formatBytes(state.bytesDownloaded)}")
        }
        is WordPressDependencyManager.DownloadState.Error -> {
            InstallProgressUi(0f, true, state.message)
        }
        else -> null
    }
}

private fun progressFromPython(state: PythonDependencyManager.DownloadState): InstallProgressUi? {
    return when (state) {
        is PythonDependencyManager.DownloadState.Downloading -> {
            val indeterminate = state.totalBytes <= 0
            val label = if (indeterminate) {
                "${state.currentFile} · ${formatBytes(state.bytesDownloaded)}"
            } else {
                val percent = (state.progress * 100).toInt()
                "${state.currentFile} · ${formatBytes(state.bytesDownloaded)} / ${formatBytes(state.totalBytes)} ($percent%)"
            }
            InstallProgressUi(
                fraction = if (indeterminate) 0f else state.progress,
                indeterminate = indeterminate,
                label = label
            )
        }
        is PythonDependencyManager.DownloadState.Extracting -> {
            InstallProgressUi(0f, true, "${Strings.extracting}: ${state.fileName}")
        }
        is PythonDependencyManager.DownloadState.Paused -> {
            InstallProgressUi(state.progress, false, "${Strings.depDlPaused} · ${formatBytes(state.bytesDownloaded)}")
        }
        is PythonDependencyManager.DownloadState.Error -> {
            InstallProgressUi(0f, true, state.message)
        }
        else -> null
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024L * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    else -> String.format(Locale.getDefault(), "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.getDefault(), "%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format(Locale.getDefault(), "%.1f MB", mb)
}
