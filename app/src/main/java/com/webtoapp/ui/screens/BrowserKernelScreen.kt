package com.webtoapp.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.webkit.WebViewCompat
import com.webtoapp.core.engine.EngineManager
import com.webtoapp.core.engine.EngineStatus
import com.webtoapp.core.engine.EngineType
import com.webtoapp.core.engine.download.DownloadState
import com.webtoapp.core.engine.download.GeckoEngineDownloader
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.components.PremiumOutlinedButton
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaBadge
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaEmptyState
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaSection
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.util.BoundedBitmaps.toBoundedBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserKernelScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var webViewInfo by remember { mutableStateOf<WebViewInfo?>(null) }

    var webViewProviders by remember { mutableStateOf<List<BrowserInfo>>(emptyList()) }

    val engineManager = remember { EngineManager.getInstance(context) }
    val geckoDownloader = remember { GeckoEngineDownloader(context, engineManager.fileManager) }
    val downloadState by geckoDownloader.downloadState.collectAsStateWithLifecycle()
    var geckoStatus by remember { mutableStateOf(engineManager.getEngineStatus(EngineType.GECKOVIEW)) }
    var geckoSize by remember { mutableLongStateOf(engineManager.getEngineSize(EngineType.GECKOVIEW)) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            webViewInfo = getWebViewInfo(context)
            webViewProviders = getInstalledWebViewProviders(context)
        }
    }

    LaunchedEffect(downloadState) {
        if (downloadState is DownloadState.Completed) {
            geckoStatus = engineManager.getEngineStatus(EngineType.GECKOVIEW)
            geckoSize = engineManager.getEngineSize(EngineType.GECKOVIEW)
        }
    }

    WtaScreen(
        title = Strings.browserKernelTitle,
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = WtaSpacing.ScreenHorizontal,
                vertical = WtaSpacing.ScreenVertical
            ),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.SectionGap)
        ) {

            item {
                WtaSection(
                    title = Strings.embeddedEngineTitle,
                    description = Strings.embeddedEngineDesc
                ) {
                EngineCard(
                    name = Strings.engineSystemWebView,
                    description = Strings.engineSystemWebViewDesc,
                    icon = Icons.Outlined.WebAsset,
                    statusText = Strings.engineReady,
                    statusColor = MaterialTheme.colorScheme.primary,
                    isDefault = true,
                    actions = {}
                )
                GeckoViewEngineCard(
                    status = geckoStatus,
                    downloadState = downloadState,
                    diskSize = geckoSize,
                    onDownload = {
                        scope.launch {
                            geckoDownloader.download()
                        }
                    },
                    onCancel = { geckoDownloader.cancelDownload() },
                    onDelete = { showDeleteDialog = true },
                    onRetry = {
                        geckoDownloader.resetState()
                        scope.launch {
                            geckoDownloader.download()
                        }
                    }
                )
            }
            }

            val hasAlternatives = webViewProviders.any { it.packageName != webViewInfo?.packageName }

            item {
                WtaSection(
                    title = Strings.currentWebViewInfo
                ) {
                CurrentWebViewCard(
                    webViewInfo = webViewInfo,
                    canChangeProvider = hasAlternatives,
                    onOpenDeveloperOptions = {
                        openDeveloperOptions(context)
                    }
                )
            }
            }

            item {
                WtaSection(
                    title = Strings.webViewProvidersTitle,
                    description = Strings.webViewProvidersDesc
                ) {
                    if (webViewProviders.isEmpty()) {
                        WtaEmptyState(
                            title = Strings.noOtherWebViewProviders,
                            icon = Icons.Outlined.SearchOff
                        )
                    } else {
                        val sorted = webViewProviders.sortedWith(
                            compareByDescending<BrowserInfo> { it.packageName == webViewInfo?.packageName }
                                .thenBy { it.name.lowercase() }
                        )
                        sorted.forEach { provider ->
                            WebViewProviderCard(
                                browser = provider,
                                isCurrentProvider = webViewInfo?.packageName == provider.packageName
                            )
                        }
                    }
                }
            }

            if (hasAlternatives) {
                item {
                    WtaSection(
                        title = Strings.howToEnableDeveloperOptions
                    ) {
                        HelpCard()
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showDeleteDialog) {
            WtaAlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = Icons.Outlined.Delete,
                iconTint = MaterialTheme.colorScheme.error,
                title = Strings.engineDeleteBtn,
                text = Strings.engineDeleteConfirm,
                confirmButton = {
                    TextButton(onClick = {
                        engineManager.deleteEngine(EngineType.GECKOVIEW)
                        geckoStatus = engineManager.getEngineStatus(EngineType.GECKOVIEW)
                        geckoSize = 0L
                        geckoDownloader.resetState()
                        showDeleteDialog = false
                    }) {
                        Text(Strings.confirm, color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(Strings.cancel)
                    }
                }
            )
        }
    }
}

@Composable
private fun CurrentWebViewCard(
    webViewInfo: WebViewInfo?,
    canChangeProvider: Boolean,
    onOpenDeveloperOptions: () -> Unit
) {
    WtaCard(
        modifier = Modifier.fillMaxWidth(),
        tone = WtaCardTone.Highlighted,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.WebAsset,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    Strings.currentWebViewInfo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (webViewInfo != null) {
                InfoRow(Strings.webViewProvider, webViewInfo.providerName)
                InfoRow(Strings.webViewVersion, webViewInfo.version)
                InfoRow(Strings.webViewPackage, webViewInfo.packageName)
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }

            if (webViewInfo != null) {
                Spacer(modifier = Modifier.height(16.dp))

                if (canChangeProvider) {
                    PremiumOutlinedButton(
                        onClick = onOpenDeveloperOptions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Settings, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.changeWebViewProvider)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        Strings.changeWebViewProviderDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            Strings.singleWebViewProviderNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun EngineCard(
    name: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color,
    isDefault: Boolean = false,
    actions: @Composable ColumnScope.() -> Unit
) {
    WtaCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(WtaRadius.Card))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            WtaBadge(
                                text = Strings.engineDefault,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                WtaBadge(
                    text = statusText,
                    containerColor = statusColor.copy(alpha = 0.12f),
                    contentColor = statusColor
                )
            }

            actions()
        }
    }
}

@Composable
private fun GeckoViewEngineCard(
    status: EngineStatus,
    downloadState: DownloadState,
    diskSize: Long,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit
) {
    val statusText = when (status) {
        is EngineStatus.READY -> Strings.engineReady
        is EngineStatus.DOWNLOADED -> Strings.engineDownloaded
        is EngineStatus.NOT_DOWNLOADED -> Strings.engineNotDownloaded
    }
    val statusColor = when (status) {
        is EngineStatus.READY -> MaterialTheme.colorScheme.primary
        is EngineStatus.DOWNLOADED -> MaterialTheme.colorScheme.tertiary
        is EngineStatus.NOT_DOWNLOADED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    EngineCard(
        name = Strings.engineGeckoView,
        description = Strings.engineGeckoViewDesc,
        icon = Icons.Outlined.LocalFireDepartment,
        statusText = if (downloadState is DownloadState.Downloading) Strings.engineDownloading else statusText,
        statusColor = if (downloadState is DownloadState.Downloading) MaterialTheme.colorScheme.tertiary else statusColor,
        isDefault = false
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(visible = downloadState is DownloadState.Downloading) {
            val progress = (downloadState as? DownloadState.Downloading)?.progress ?: 0f
            val message = (downloadState as? DownloadState.Downloading)?.message ?: ""
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onCancel,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(Strings.engineCancelDownload, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        AnimatedVisibility(visible = downloadState is DownloadState.Error) {
            val errorMsg = (downloadState as? DownloadState.Error)?.message ?: ""
            WtaCard(
                modifier = Modifier.fillMaxWidth(),
                tone = WtaCardTone.Critical,
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        errorMsg,
                        modifier = Modifier.weight(weight = 1f, fill = true),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    TextButton(onClick = onRetry) {
                        Text(Strings.engineRetry)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (status is EngineStatus.DOWNLOADED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${Strings.engineVersionLabel}: ${status.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (diskSize > 0) {
                    Text(
                        "${Strings.engineCurrentSize}: ${formatFileSize(diskSize)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (downloadState !is DownloadState.Downloading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (status is EngineStatus.DOWNLOADED) {
                    PremiumOutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.engineDeleteBtn, style = MaterialTheme.typography.labelMedium)
                    }
                } else if (status is EngineStatus.NOT_DOWNLOADED) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${Strings.engineEstimatedSize}: ~${EngineType.GECKOVIEW.estimatedSizeMb} MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        FilledTonalButton(onClick = onDownload) {
                            Icon(Icons.Outlined.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Strings.engineDownloadBtn, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }
}

@Composable
private fun WebViewProviderCard(
    browser: BrowserInfo,
    isCurrentProvider: Boolean
) {
    WtaCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

                val iconBitmap = remember(browser.icon) {
                    // Third-party app icons can be arbitrarily large; raster bounded (#779).
                    try {
                        browser.icon?.toBoundedBitmap()?.asImageBitmap()
                    } catch (t: Throwable) {
                        null
                    }
                }
                if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = browser.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(WtaRadius.Card))
                )
                } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(WtaRadius.Card))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                        Icon(
                            Icons.Outlined.Language,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        browser.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrentProvider) {
                        Spacer(modifier = Modifier.width(8.dp))
                        WtaBadge(
                            text = Strings.currentlyUsing,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${browser.version} · ${browser.packageName}",
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
private fun HelpCard() {
    WtaCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    Strings.howToEnableDeveloperOptions,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                Strings.developerOptionsSteps,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                Strings.webViewNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class WebViewInfo(
    val providerName: String,
    val version: String,
    val packageName: String
)

data class BrowserInfo(
    val name: String,
    val packageName: String,
    val version: String,
    val icon: android.graphics.drawable.Drawable?
)

private fun getWebViewInfo(context: Context): WebViewInfo {
    return try {
        val webViewPackage = WebViewCompat.getCurrentWebViewPackage(context)
        if (webViewPackage != null) {
            WebViewInfo(
                providerName = webViewPackage.applicationInfo?.loadLabel(context.packageManager)?.toString() ?: webViewPackage.packageName,
                version = webViewPackage.versionName ?: "Unknown",
                packageName = webViewPackage.packageName
            )
        } else {
            getDefaultWebViewInfo()
        }
    } catch (e: Exception) {
        getDefaultWebViewInfo()
    }
}

private fun getDefaultWebViewInfo(): WebViewInfo {
    return WebViewInfo(
        providerName = "Android System WebView",
        version = "Unknown",
        packageName = "com.google.android.webview"
    )
}

// AOSP default for config_webViewPackages; OEMs may override or extend it.
private val DEFAULT_WEBVIEW_PROVIDER_PACKAGES = setOf(
    "com.google.android.webview",
    "com.android.webview",
    "com.android.chrome",
    "com.chrome.beta",
    "com.chrome.dev",
    "com.chrome.canary"
)

// Only packages in the framework's WebView whitelist can actually act as
// providers — being a browser does not qualify an app.
private fun getWebViewProviderWhitelist(): Set<String> {
    return runCatching {
        val res = android.content.res.Resources.getSystem()
        val id = res.getIdentifier("config_webViewPackages", "array", "android")
        if (id != 0) res.getStringArray(id).toSet() else emptySet()
    }.getOrDefault(emptySet()).ifEmpty { DEFAULT_WEBVIEW_PROVIDER_PACKAGES }
}

private fun getInstalledWebViewProviders(context: Context): List<BrowserInfo> {
    val pm = context.packageManager
    return getWebViewProviderWhitelist().mapNotNull { packageName ->
        try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            BrowserInfo(
                name = appInfo.loadLabel(pm).toString(),
                packageName = packageName,
                version = packageInfo.versionName ?: "Unknown",
                icon = appInfo.loadIcon(pm)
            )
        } catch (e: Exception) {
            null
        }
    }.sortedBy { it.name.lowercase() }
}

private fun openDeveloperOptions(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        context.startActivity(intent)
    } catch (e: Exception) {

        try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            context.startActivity(intent)
        } catch (e2: Exception) {

        }
    }
}
