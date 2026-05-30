package com.webtoapp.ui.aicoding.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material.icons.outlined.Web
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.webtoapp.core.aicoding.files.ProjectFileManager
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.aicoding.AiCodingUiState
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import java.io.File

@Composable
fun PreviewPane(
    state: AiCodingUiState,
    fileManager: ProjectFileManager,
    onSelectFile: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sessionId = state.currentSession?.id
    val previewables = remember(state.projectFiles) { previewableFiles(state) }

    val resolvedPath = state.previewFilePath
        ?: previewables.firstOrNull()?.relativePath
    val errors = remember { mutableIntStateOf(0) }
    val warnings = remember { mutableIntStateOf(0) }

    val reloadTick = remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        PreviewToolbar(
            previewFile = resolvedPath,
            previewables = previewables,
            errors = errors.intValue,
            warnings = warnings.intValue,
            onSelectFile = { path ->

                errors.intValue = 0
                warnings.intValue = 0
                onSelectFile(path)
            },
            onRefresh = {
                errors.intValue = 0
                warnings.intValue = 0

                reloadTick.intValue += 1
            }
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WtaAlpha.Divider)
        )
        if (sessionId == null || resolvedPath == null) {
            EmptyPreview()
        } else {
            PreviewWebView(
                fileManager = fileManager,
                sessionId = sessionId,
                relativePath = resolvedPath,
                reloadTick = reloadTick.intValue,
                onConsoleError = { errors.intValue += 1 },
                onConsoleWarning = { warnings.intValue += 1 },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun PreviewTab(
    state: AiCodingUiState,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resolved = state.previewFilePath ?: previewableFiles(state).firstOrNull()?.relativePath
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(WtaSize.TouchTarget)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onExpand)
            .padding(horizontal = WtaSpacing.ScreenHorizontal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.UnfoldMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(WtaSize.Icon)
        )
        Spacer(Modifier.width(WtaSpacing.Small))
        Text(
            text = if (resolved == null) Strings.aiCodingPreviewTabIdle
            else Strings.aiCodingPreviewToolbarPrefix.format(resolved),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PreviewToolbar(
    previewFile: String?,
    previewables: List<ProjectFileManager.FileInfo>,
    errors: Int,
    warnings: Int,
    onSelectFile: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var pickerOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = WtaSpacing.Small, vertical = WtaSpacing.Tiny),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
    ) {

        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = previewables.isNotEmpty()) {
                        pickerOpen = true
                    }
                    .padding(horizontal = WtaSpacing.Small, vertical = WtaSpacing.Tiny),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
            ) {
                Text(
                    text = previewFile ?: Strings.aiCodingPreviewNoFile,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (previewables.isNotEmpty()) {
                    Icon(
                        Icons.Outlined.ExpandMore,
                        contentDescription = Strings.aiCodingPreviewSwitchTooltip,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(WtaSize.IconSmall)
                    )
                }
            }
            DropdownMenu(
                expanded = pickerOpen,
                onDismissRequest = { pickerOpen = false },
                modifier = Modifier.widthIn(min = 220.dp, max = 360.dp)
            ) {
                Text(
                    text = Strings.aiCodingPreviewSwitchHeader,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        horizontal = WtaSpacing.Medium,
                        vertical = WtaSpacing.Small
                    )
                )
                if (previewables.isEmpty()) {
                    Text(
                        text = Strings.aiCodingPreviewSwitchEmpty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            horizontal = WtaSpacing.Medium,
                            vertical = WtaSpacing.Small
                        )
                    )
                }
                previewables.forEach { f ->
                    val isSelected = f.relativePath == previewFile
                    DropdownMenuItem(
                        onClick = {
                            pickerOpen = false
                            onSelectFile(f.relativePath)
                        },
                        text = {
                            Text(
                                text = f.relativePath,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isImage(f.relativePath))
                                    Icons.Outlined.Image
                                else Icons.Outlined.Web,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null
                    )
                }
            }
        }
        if (errors > 0) {
            WtaInfoChip(
                label = Strings.aiCodingPreviewErrorsCount.format(errors),
                containerColor = WtaColors.semantic.errorContainer,
                contentColor = WtaColors.semantic.onErrorContainer
            )
        }
        if (warnings > 0) {
            WtaInfoChip(
                label = Strings.aiCodingPreviewWarningsCount.format(warnings),
                containerColor = WtaColors.semantic.warningContainer,
                contentColor = WtaColors.semantic.onWarningContainer
            )
        }
        WtaIconButton(
            onClick = onRefresh,
            icon = Icons.Outlined.Refresh,
            contentDescription = Strings.aiCodingPreviewRefresh
        )
    }
}

@Composable
private fun EmptyPreview() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(WtaSize.IconPlateLarge)
            )
            Spacer(Modifier.height(WtaSpacing.Small))
            Text(
                text = Strings.aiCodingPreviewEmpty,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PreviewWebView(
    fileManager: ProjectFileManager,
    sessionId: String,
    relativePath: String,

    reloadTick: Int,
    onConsoleError: () -> Unit,
    onConsoleWarning: () -> Unit,
    modifier: Modifier = Modifier
) {
    val absolute = remember(sessionId, relativePath) {
        fileManager.resolveSafe(sessionId, relativePath)?.absolutePath
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(absolute, webViewRef) {
        val wv = webViewRef ?: return@LaunchedEffect
        val path = absolute ?: return@LaunchedEffect
        if (File(path).exists()) {
            wv.loadUrl("file://$path")
        }
    }

    LaunchedEffect(reloadTick) {
        if (reloadTick == 0) return@LaunchedEffect
        val wv = webViewRef ?: return@LaunchedEffect
        val path = absolute ?: return@LaunchedEffect
        wv.stopLoading()
        wv.clearCache(true)
        wv.clearHistory()
        wv.clearFormData()
        android.webkit.WebStorage.getInstance().deleteAllData()
        runCatching {
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
        }

        wv.loadUrl("about:blank")
        if (File(path).exists()) {
            wv.loadUrl("file://$path")
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                    allowFileAccess = true
                    setSupportZoom(true)
                    builtInZoomControls = false
                }
                webViewClient = WebViewClient()
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        when (consoleMessage?.messageLevel()) {
                            android.webkit.ConsoleMessage.MessageLevel.ERROR -> onConsoleError()
                            android.webkit.ConsoleMessage.MessageLevel.WARNING -> onConsoleWarning()
                            else -> Unit
                        }
                        return super.onConsoleMessage(consoleMessage)
                    }
                }
                webViewRef = this
            }
        }
    )
    DisposableEffect(Unit) {
        onDispose {

            webViewRef = null
        }
    }
}

private fun previewableFiles(state: AiCodingUiState): List<ProjectFileManager.FileInfo> {
    return state.projectFiles
        .filter { isPreviewable(it.relativePath) }
        .sortedWith(
            compareByDescending<ProjectFileManager.FileInfo> { it.modifiedAt }
                .thenByDescending { it.relativePath == "index.html" }
        )
}

private fun isPreviewable(path: String): Boolean {
    val lower = path.lowercase()
    return lower.endsWith(".html") ||
        lower.endsWith(".htm") ||
        isImage(lower)
}

private fun isImage(path: String): Boolean {
    val lower = path.lowercase()
    return lower.endsWith(".png") ||
        lower.endsWith(".jpg") ||
        lower.endsWith(".jpeg") ||
        lower.endsWith(".gif") ||
        lower.endsWith(".webp") ||
        lower.endsWith(".svg")
}
