package com.webtoapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.linux.LinuxEnvironmentManager
import com.webtoapp.core.linux.LocalBuildEnvironment
import com.webtoapp.ui.components.PremiumButton
import com.webtoapp.ui.theme.LocalAppTheme
import kotlinx.coroutines.launch
import java.io.File

enum class DepsKind { PHP, PYTHON, NODE }

@Composable
fun InstallProjectDepsCard(
    kind: DepsKind,
    projectDir: String?,
    accentColor: Color,
    onOpenBuildEnvScreen: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val envManager = remember { LinuxEnvironmentManager.getInstance(context) }

    var phpReady by remember { mutableStateOf(false) }
    var composerReady by remember { mutableStateOf(false) }
    var pythonReady by remember { mutableStateOf(false) }
    var nodeReady by remember { mutableStateOf(false) }

    LaunchedEffect(kind) {
        when (kind) {
            DepsKind.PHP -> {
                phpReady = LocalBuildEnvironment.isPhpReady(context)
                composerReady = LocalBuildEnvironment.isComposerReady(context)
            }
            DepsKind.PYTHON -> {
                pythonReady = LocalBuildEnvironment.isPythonReady(context)
            }
            DepsKind.NODE -> {

                nodeReady = LocalBuildEnvironment.isNpmReady(context)
            }
        }
    }

    var installing by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf<Boolean?>(null) }
    val logs = remember { mutableStateListOf<String>() }
    var logsExpanded by remember { mutableStateOf(false) }

    val theme = LocalAppTheme.current
    val shape = RoundedCornerShape(theme.shapes.cardRadius)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.CloudDownload,
                        null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    Strings.installDepsInAppTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                Strings.installDepsInAppDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val notReadyMessage = when (kind) {
                DepsKind.PHP -> when {
                    !phpReady -> Strings.phpRuntimeNotReady
                    !composerReady -> Strings.composerNotReady
                    else -> null
                }
                DepsKind.PYTHON -> if (!pythonReady) Strings.pythonRuntimeNotReady else null
                DepsKind.NODE -> if (!nodeReady) Strings.nodeRuntimeNotReady else null
            }

            if (notReadyMessage != null) {
                NotReadyHint(message = notReadyMessage, onClickOpen = onOpenBuildEnvScreen)
                Spacer(modifier = Modifier.height(12.dp))
            }

            val canRun = notReadyMessage == null && projectDir != null && !installing
            val buttonLabel = when {
                installing -> Strings.depsInstalling
                kind == DepsKind.PHP -> Strings.runComposerInstall
                kind == DepsKind.PYTHON -> Strings.runPipInstall
                else -> Strings.runNpmInstall
            }

            PremiumButton(
                onClick = {
                    val dirPath = projectDir
                    if (dirPath == null) {
                        success = false
                        logs.clear()
                        logs.add(Strings.noProjectSelected)
                        return@PremiumButton
                    }
                    installing = true
                    success = null
                    logs.clear()
                    val command = when (kind) {
                        DepsKind.PHP -> "$ php composer.phar install"
                        DepsKind.PYTHON -> "$ pip install -r requirements.txt"
                        DepsKind.NODE -> "$ npm install"
                    }
                    logs.add(command)
                    scope.launch {
                        val result = when (kind) {
                            DepsKind.PHP -> envManager.installPhpProjectDependencies(File(dirPath)) { line ->
                                appendLogLine(logs, line)
                            }
                            DepsKind.PYTHON -> envManager.installPythonProjectDependencies(File(dirPath)) { line ->
                                appendLogLine(logs, line)
                            }
                            DepsKind.NODE -> envManager.installNodeProjectDependencies(File(dirPath)) { line ->
                                appendLogLine(logs, line)
                            }
                        }
                        installing = false
                        success = result.isSuccess
                        if (result.isFailure) {
                            appendLogLine(logs, "[error] ${result.exceptionOrNull()?.message.orEmpty()}")

                            logsExpanded = true
                        }
                    }
                },
                enabled = canRun,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (installing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(buttonLabel)
            }

            success?.let { ok ->
                Spacer(modifier = Modifier.height(12.dp))
                StatusChip(ok)

                if (!ok) {
                    val diagnosis = remember(logs.size, logs.lastOrNull()) {
                        diagnoseInstallFailure(kind, logs)
                    }
                    if (diagnosis != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DiagnosticBanner(diagnosis)
                    }
                }
            }

            if (logs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LogsPanel(
                    logs = logs,
                    expanded = logsExpanded,
                    onToggle = { logsExpanded = !logsExpanded },
                )
            }
        }
    }
}

private fun appendLogLine(logs: MutableList<String>, line: String) {
    if (line.isBlank()) return
    if (logs.size >= 500) {
        logs.removeAt(0)
    }
    logs.add(line)
}

@Composable
private fun NotReadyHint(message: String, onClickOpen: () -> Unit) {
    val theme = LocalAppTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(theme.shapes.cornerRadius * 0.5f))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Error,
                null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        TextButton(onClick = onClickOpen) {
            Text(Strings.openBuildEnvScreen)
        }
    }
}

@Composable
private fun StatusChip(success: Boolean) {
    val color = if (success) com.webtoapp.ui.design.WtaColors.semantic.success
        else com.webtoapp.ui.design.WtaColors.semantic.warning
    val icon = if (success) Icons.Filled.CheckCircle else Icons.Filled.Error
    val label = if (success) Strings.depsInstallSuccess else Strings.depsInstallFailed
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LogsPanel(
    logs: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val theme = LocalAppTheme.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(theme.shapes.cornerRadius * 0.5f))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${Strings.viewLogs} (${logs.size})",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = {
                    val full = logs.joinToString(separator = "\n")
                    clipboardManager.setText(AnnotatedString(full))
                    Toast.makeText(context, Strings.copiedAllLogs, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = Strings.copyAll,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .padding(12.dp)
            ) {
                val scroll = rememberScrollState()

                LaunchedEffect(logs.size) {
                    scroll.animateScrollTo(scroll.maxValue)
                }

                SelectionContainer {
                    Column(modifier = Modifier.verticalScroll(scroll)) {
                        logs.forEach { line ->
                            Text(
                                line,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class InstallDiagnosis(
    val title: String,
    val message: String,
)

private fun diagnoseInstallFailure(kind: DepsKind, logs: List<String>): InstallDiagnosis? {

    val joined = logs.joinToString(separator = "\n").lowercase()

    val dnsPatterns = listOf(
        "curl error 6",
        "could not resolve host",
        "getaddrinfo failed",
        "php_network_getaddresses",
        "name or service not known",
        "temporary failure in name resolution",
        "name resolution failure",
    )
    if (dnsPatterns.any { joined.contains(it) }) {
        return InstallDiagnosis(
            title = Strings.installDiagDnsTitle,
            message = when (kind) {
                DepsKind.PHP -> Strings.installDiagDnsPhpMessage
                DepsKind.PYTHON -> Strings.installDiagDnsPythonMessage
                DepsKind.NODE -> Strings.installDiagDnsNodeMessage
            }
        )
    }

    val netPatterns = listOf(
        "curl error 28",
        "connection timed out",
        "network is unreachable",
        "operation timed out",
        "connection refused",
        "no route to host",
        "read timed out",
    )
    if (netPatterns.any { joined.contains(it) }) {
        return InstallDiagnosis(
            title = Strings.installDiagNetworkTitle,
            message = Strings.installDiagNetworkMessage,
        )
    }

    val sslPatterns = listOf(
        "curl error 60",
        "ssl certificate problem",
        "unable to verify",
        "certificate has expired",
        "self signed certificate",
        "ssl: certificate_verify_failed",
    )
    if (sslPatterns.any { joined.contains(it) }) {
        return InstallDiagnosis(
            title = Strings.installDiagSslTitle,
            message = Strings.installDiagSslMessage,
        )
    }

    val diskPatterns = listOf(
        "no space left on device",
        "enospc",
        "disk full",
    )
    if (diskPatterns.any { joined.contains(it) }) {
        return InstallDiagnosis(
            title = Strings.installDiagDiskFullTitle,
            message = Strings.installDiagDiskFullMessage,
        )
    }

    return null
}

@Composable
private fun DiagnosticBanner(diagnosis: InstallDiagnosis) {
    val theme = LocalAppTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(theme.shapes.cornerRadius * 0.5f))
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Outlined.Lightbulb,
                null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    diagnosis.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    diagnosis.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}
