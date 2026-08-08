package com.webtoapp.ui.screens

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.R
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.components.DataBackupCard
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaSection
import com.webtoapp.ui.design.WtaSectionHeaderStyle
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.rememberHapticClick
import com.webtoapp.ui.design.wtaPressScale
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val versionName = remember(context) { context.currentVersionName() }
    val versionCode = remember(context) { context.currentVersionCode() }

    WtaScreen(
        title = Strings.about,
        snackbarHostState = snackbarHostState,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    horizontal = WtaSpacing.ScreenHorizontal,
                    vertical = WtaSpacing.ScreenVertical
                ),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.SectionGap)
        ) {
            AuthorHeroCard(
                versionName = versionName,
                versionCode = versionCode
            )

            ContactGrid()

            WtaSection(
                title = Strings.dataBackupTitle,
                headerStyle = WtaSectionHeaderStyle.Quiet
            ) {
                DataBackupCard()
            }

            LegalTabContent()

            MadeWithLoveFooter()
        }
    }
}

@Composable
private fun AuthorHeroCard(
    versionName: String,
    versionCode: Long
) {
    val context = LocalContext.current
    val byLine = aboutAuthorByLine()
    val versionCopied = versionCopiedToast()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var updateResult by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<com.webtoapp.core.update.UpdateChecker.Result?>(null)
    }
    var isCheckingUpdate by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showUpdateDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var downloadState by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<com.webtoapp.core.update.UpdateDownloadState>(
            com.webtoapp.core.update.UpdateDownloadState.Idle
        )
    }

    // Version history state
    var showHistorySheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var historyLoading by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var historyError by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var historyReleases by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<List<com.webtoapp.core.update.UpdateChecker.ReleaseSummary>>(emptyList())
    }
    // Per-release download state keyed by tag, so each version downloads independently.
    val historyDownloadStates = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateMapOf<String, com.webtoapp.core.update.UpdateDownloadState>()
    }

    val apkBuilder = remember(context) { com.webtoapp.core.apkbuilder.ApkBuilder(context.applicationContext) }

    if (showUpdateDialog) {
        UpdateCheckDialog(
            isChecking = isCheckingUpdate,
            result = updateResult,
            currentVersionName = versionName,
            downloadState = downloadState,
            onDownload = { info ->
                com.webtoapp.core.update.ApkUpdateInstaller.download(
                    context = context,
                    url = info.downloadUrl,
                    version = info.version,
                    expectedSha256 = info.sha256,
                    onState = { state -> downloadState = state }
                )
            },
            onCancelDownload = {
                com.webtoapp.core.update.ApkUpdateInstaller.cancel()
                downloadState = com.webtoapp.core.update.UpdateDownloadState.Idle
            },
            onInstall = { file ->
                val started = apkBuilder.installApk(file)
                if (!started) {
                    Toast.makeText(context, Strings.fileManagerInstallFailed, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = {
                com.webtoapp.core.update.ApkUpdateInstaller.cancel()
                showUpdateDialog = false
                downloadState = com.webtoapp.core.update.UpdateDownloadState.Idle
            }
        )
    }

    if (showHistorySheet) {
        VersionHistorySheet(
            releases = historyReleases,
            loading = historyLoading,
            error = historyError,
            currentVersion = versionName,
            downloadStates = historyDownloadStates,
            onDownload = { release ->
                val url = release.downloadUrl ?: return@VersionHistorySheet
                com.webtoapp.core.update.ApkUpdateInstaller.download(
                    context = context,
                    url = url,
                    version = release.version,
                    expectedSha256 = release.sha256,
                    onState = { state -> historyDownloadStates[release.tag] = state }
                )
            },
            onCancelDownload = { tag ->
                com.webtoapp.core.update.ApkUpdateInstaller.cancel()
                historyDownloadStates.remove(tag)
            },
            onInstall = { file ->
                val started = apkBuilder.installApk(file)
                if (!started) {
                    Toast.makeText(context, Strings.fileManagerInstallFailed, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = {
                com.webtoapp.core.update.ApkUpdateInstaller.cancel()
                showHistorySheet = false
            }
        )
    }


    WtaCard(
        modifier = Modifier.fillMaxWidth(),
        tone = WtaCardTone.Elevated,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 24.dp,
            vertical = 28.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Image(

                    painter = painterResource(id = R.drawable.about_avatar),
                    contentDescription = Strings.authorAvatar,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "WebToApp",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = byLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = Strings.aboutAppDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(18.dp))

            VersionPill(
                versionName = versionName,
                versionCode = versionCode,
                onClick = {
                    showUpdateDialog = true
                    if (!isCheckingUpdate) {
                        isCheckingUpdate = true
                        updateResult = null
                        scope.launch {
                            val result = com.webtoapp.core.update.UpdateChecker.check(versionName)
                            updateResult = result
                            isCheckingUpdate = false
                        }
                    }
                },
                onCopy = {
                    val label = "WebToApp version"
                    val text = "WebToApp v$versionName ($versionCode)"
                    context.copyToClipboard(label, text)
                    Toast.makeText(
                        context,
                        versionCopied,
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onHistory = {
                    showHistorySheet = true
                    if (historyReleases.isEmpty() && !historyLoading) {
                        historyLoading = true
                        historyError = null
                        scope.launch {
                            try {
                                historyReleases = com.webtoapp.core.update.UpdateChecker.fetchAllReleases()
                            } catch (e: Exception) {
                                historyError = e.message ?: e.javaClass.simpleName
                            } finally {
                                historyLoading = false
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun VersionPill(
    versionName: String,
    versionCode: Long,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onHistory: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hapticClick = rememberHapticClick(onClick)
    val hapticCopy = rememberHapticClick(onCopy)
    val hapticHistory = rememberHapticClick(onHistory)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = hapticClick
            )
            .wtaPressScale(interactionSource, pressedScale = 0.95f)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "v$versionName",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "·",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = versionCode.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Outlined.Sync,
            contentDescription = Strings.updateCheckTitle,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(10.dp))
        Icon(
            Icons.Outlined.History,
            contentDescription = Strings.versionHistoryButtonHint,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = hapticHistory)
                .size(15.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(10.dp))
        Icon(
            Icons.Outlined.ContentCopy,
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = hapticCopy)
                .size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContactGrid() {
    val context = LocalContext.current
    val groupWord = groupWord()
    val qqCopied = qqCopiedToast()
    val authorLabel = authorLabel()
    val contactTitle = contactSectionTitle()
    val docsLabel = docsLabel()
    val discordCommunityLabel = discordCommunityLabel()

    val entries = remember(groupWord, qqCopied, authorLabel, docsLabel, discordCommunityLabel) {
        listOf(
            ContactEntry(
                icon = Icons.Outlined.Code,
                label = "GitHub",
                value = "shiaho777/web-to-app",
                action = ContactAction.OpenUrl("https://github.com/shiaho777/web-to-app")
            ),
            ContactEntry(
                icon = Icons.Outlined.MenuBook,
                label = docsLabel,
                value = "shiaho777.github.io",
                action = ContactAction.OpenUrl("https://shiaho777.github.io/web-to-app/")
            ),
            ContactEntry(
                icon = Icons.Outlined.Send,
                label = "Telegram",
                value = "@webtoapp777",
                action = ContactAction.OpenUrl("https://t.me/webtoapp777")
            ),
            ContactEntry(
                icon = Icons.Outlined.Chat,
                label = "Discord",
                value = discordCommunityLabel,
                action = ContactAction.OpenUrl("https://discord.gg/KUKEn4zPHQ")
            ),
            ContactEntry(
                icon = Icons.Outlined.Tag,
                label = "X (Twitter)",
                value = "@shiaho777",
                action = ContactAction.OpenUrl("https://x.com/shiaho777")
            ),
            ContactEntry(
                icon = Icons.Outlined.PlayCircleOutline,
                label = "Bilibili",
                value = "b23.tv/8mGDo2N",
                action = ContactAction.OpenUrl("https://b23.tv/8mGDo2N")
            ),
            ContactEntry(
                icon = Icons.Outlined.Groups,
                label = "QQ $groupWord",
                value = "1041130206",
                action = ContactAction.CopyText(
                    label = "QQ Group",
                    text = "1041130206",
                    toast = qqCopied
                )
            ),
            ContactEntry(
                icon = Icons.Outlined.Forum,
                label = authorLabel,
                value = "shiaho",
                action = ContactAction.OpenUrl("https://github.com/shiaho777")
            )
        )
    }

    WtaSection(
        title = contactTitle,
        headerStyle = WtaSectionHeaderStyle.Quiet
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.CardGap)) {
            entries.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(WtaSpacing.CardGap)) {
                    pair.forEach { entry ->
                        ContactTile(
                            entry = entry,
                            modifier = Modifier.weight(1f),
                            onAction = { action ->
                                when (action) {
                                    is ContactAction.OpenUrl -> context.openUrl(action.url)
                                    is ContactAction.CopyText -> {
                                        context.copyToClipboard(action.label, action.text)
                                        Toast.makeText(
                                            context,
                                            action.toast,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }

                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private data class ContactEntry(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val action: ContactAction
)

private sealed interface ContactAction {
    data class OpenUrl(val url: String) : ContactAction
    data class CopyText(
        val label: String,
        val text: String,
        val toast: String
    ) : ContactAction
}

@Composable
private fun ContactTile(
    entry: ContactEntry,
    modifier: Modifier = Modifier,
    onAction: (ContactAction) -> Unit
) {
    WtaCard(
        onClick = { onAction(entry.action) },
        modifier = modifier,
        tone = WtaCardTone.Surface,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    entry.icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.value,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Outlined.Link,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun LegalTabContent() {
    WtaSection(
        title = Strings.legalDisclaimer,
        headerStyle = WtaSectionHeaderStyle.Quiet,
        collapsible = true,
        initiallyExpanded = false
    ) {
        WtaCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LegalSection(Strings.legalDisclaimerTitle1, Strings.legalDisclaimerContent1)
                LegalSection(Strings.legalDisclaimerTitle2, Strings.legalDisclaimerContent2)
                LegalSection(Strings.legalDisclaimerTitle3, Strings.legalDisclaimerContent3)
                LegalSection(Strings.legalDisclaimerTitle4, Strings.legalDisclaimerContent4)
                LegalSection(Strings.legalDisclaimerTitle5, Strings.legalDisclaimerContent5)
            }
        }
    }
}

@Composable
private fun LegalSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpdateCheckDialog(
    isChecking: Boolean,
    result: com.webtoapp.core.update.UpdateChecker.Result?,
    currentVersionName: String,
    downloadState: com.webtoapp.core.update.UpdateDownloadState,
    onDownload: (com.webtoapp.core.update.UpdateChecker.ReleaseInfo) -> Unit,
    onCancelDownload: () -> Unit,
    onInstall: (java.io.File) -> Unit,
    onDismiss: () -> Unit
) {
    val available = result as? com.webtoapp.core.update.UpdateChecker.Result.UpdateAvailable
    val downloading = downloadState is com.webtoapp.core.update.UpdateDownloadState.Downloading ||
        downloadState is com.webtoapp.core.update.UpdateDownloadState.Verifying

    androidx.compose.material3.AlertDialog(
        onDismissRequest = {
            // Block dismiss via outside touch while a download is in flight.
            if (!downloading) onDismiss()
        },
        icon = {
            Icon(Icons.Outlined.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text(Strings.updateCheckTitle) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                when {
                    isChecking || result == null -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(Strings.updateChecking, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    available != null -> {
                        UpdateAvailableContent(
                            info = available.info,
                            currentVersion = available.currentVersion,
                            downloadState = downloadState
                        )
                    }
                    result is com.webtoapp.core.update.UpdateChecker.Result.UpToDate -> {
                        Text(Strings.updateUpToDate, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "v${result.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    result is com.webtoapp.core.update.UpdateChecker.Result.Failed -> {
                        com.webtoapp.ui.components.WtaErrorDetailsSection(
                            report = com.webtoapp.ui.components.buildErrorReport(
                                scope = "Update check",
                                message = result.message,
                                throwable = result.throwable,
                                contextLines = listOf("currentVersion=$currentVersionName")
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            when {
                // Download finished: show Install.
                downloadState is com.webtoapp.core.update.UpdateDownloadState.Done -> {
                    androidx.compose.material3.TextButton(onClick = {
                        onInstall(downloadState.file)
                    }) {
                        Text(Strings.install)
                    }
                }
                // Download failed: Retry.
                downloadState is com.webtoapp.core.update.UpdateDownloadState.Failed && available != null -> {
                    androidx.compose.material3.TextButton(onClick = { onDownload(available.info) }) {
                        Text(Strings.updateRetry)
                    }
                }
                // Downloading / verifying: Cancel.
                downloading -> {
                    androidx.compose.material3.TextButton(onClick = onCancelDownload) {
                        Text(Strings.updateCancelDownload)
                    }
                }
                // Update available, idle: Download.
                available != null -> {
                    androidx.compose.material3.TextButton(onClick = { onDownload(available.info) }) {
                        Text(Strings.updateDownloadButton)
                    }
                }
                else -> {
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text(Strings.close)
                    }
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(Strings.close)
            }
        }
    )
}

@Composable
private fun UpdateAvailableContent(
    info: com.webtoapp.core.update.UpdateChecker.ReleaseInfo,
    currentVersion: String,
    downloadState: com.webtoapp.core.update.UpdateDownloadState
) {
    Text(
        Strings.updateAvailableTitle,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "${Strings.updateNewVersionLabel}: v${info.version}",
        style = MaterialTheme.typography.bodyMedium
    )
    Text(
        "${Strings.updateCurrentVersionLabel}: v$currentVersion",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    if (info.sizeBytes > 0) {
        Text(
            "${Strings.updateSizeLabel}: ${formatBytes(info.sizeBytes)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(Modifier.height(12.dp))

    when (downloadState) {
        is com.webtoapp.core.update.UpdateDownloadState.Downloading -> {
            DownloadProgressRow(
                downloaded = downloadState.downloadedBytes,
                total = downloadState.totalBytes,
                speed = downloadState.speedBytesPerSec
            )
        }
        com.webtoapp.core.update.UpdateDownloadState.Verifying -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
                Text(Strings.updateVerifying, style = MaterialTheme.typography.bodyMedium)
            }
        }
        is com.webtoapp.core.update.UpdateDownloadState.Done -> {
            Text(
                Strings.updateInstallReady,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                Strings.updateReadyHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        is com.webtoapp.core.update.UpdateDownloadState.Failed -> {
            Text(
                Strings.updateDownloadFailed,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(2.dp))
            Text(
                downloadState.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        com.webtoapp.core.update.UpdateDownloadState.Idle -> {
            if (info.releaseNotes.isNotBlank()) {
                Text(
                    Strings.updateReleaseNotesLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                ReleaseNotesMarkdown(text = info.releaseNotes)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VersionHistorySheet(
    releases: List<com.webtoapp.core.update.UpdateChecker.ReleaseSummary>,
    loading: Boolean,
    error: String?,
    currentVersion: String,
    downloadStates: androidx.compose.runtime.snapshots.SnapshotStateMap<String, com.webtoapp.core.update.UpdateDownloadState>,
    onDownload: (com.webtoapp.core.update.UpdateChecker.ReleaseSummary) -> Unit,
    onCancelDownload: (String) -> Unit,
    onInstall: (java.io.File) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    Strings.versionHistoryTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            when {
                loading && releases.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                Strings.versionHistoryLoading,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                error != null && releases.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            Strings.versionHistoryLoadFailed,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                releases.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            Strings.versionHistoryEmpty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(releases, key = { it.tag }) { release ->
                            VersionHistoryItem(
                                release = release,
                                isCurrent = release.version == currentVersion,
                                downloadState = downloadStates[release.tag]
                                    ?: com.webtoapp.core.update.UpdateDownloadState.Idle,
                                onDownload = { onDownload(release) },
                                onCancelDownload = { onCancelDownload(release.tag) },
                                onInstall = onInstall
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionHistoryItem(
    release: com.webtoapp.core.update.UpdateChecker.ReleaseSummary,
    isCurrent: Boolean,
    downloadState: com.webtoapp.core.update.UpdateDownloadState,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onInstall: (java.io.File) -> Unit
) {
    var expanded by remember(release.tag) { androidx.compose.runtime.mutableStateOf(false) }
    val date = remember(release.publishedAt) { release.publishedAt.substringBefore('T').ifBlank { release.publishedAt } }
    val hasNotes = release.body.isNotBlank()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = hasNotes) { expanded = !expanded }
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "v${release.version}",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (isCurrent) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                Strings.versionHistoryCurrentVersion,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (date.isNotBlank()) {
                        Text(
                            date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (date.isNotBlank() && release.sizeBytes > 0) {
                        Text(
                            " · ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (release.sizeBytes > 0) {
                        Text(
                            formatBytes(release.sizeBytes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (hasNotes) {
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                if (hasNotes) {
                    // Thin divider line above notes.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        Strings.updateReleaseNotesLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    ReleaseNotesMarkdown(text = release.body)
                }
                Spacer(Modifier.height(10.dp))
                VersionHistoryDownloadRow(
                    release = release,
                    isCurrent = isCurrent,
                    downloadState = downloadState,
                    onDownload = onDownload,
                    onCancelDownload = onCancelDownload,
                    onInstall = onInstall
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        // Divider between items.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun VersionHistoryDownloadRow(
    release: com.webtoapp.core.update.UpdateChecker.ReleaseSummary,
    isCurrent: Boolean,
    downloadState: com.webtoapp.core.update.UpdateDownloadState,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onInstall: (java.io.File) -> Unit
) {
    when (downloadState) {
        is com.webtoapp.core.update.UpdateDownloadState.Downloading -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        Strings.updateDownloading,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onCancelDownload) {
                        Text(Strings.updateCancelDownload)
                    }
                }
                Spacer(Modifier.height(4.dp))
                DownloadProgressRow(
                    downloaded = downloadState.downloadedBytes,
                    total = downloadState.totalBytes,
                    speed = downloadState.speedBytesPerSec
                )
            }
        }
        com.webtoapp.core.update.UpdateDownloadState.Verifying -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text(Strings.updateVerifying, style = MaterialTheme.typography.bodyMedium)
            }
        }
        is com.webtoapp.core.update.UpdateDownloadState.Done -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    Strings.updateInstallReady,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    TextButton(onClick = { onInstall(downloadState.file) }) {
                        Text(Strings.versionHistoryInstall)
                    }
                }
            }
        }
        is com.webtoapp.core.update.UpdateDownloadState.Failed -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    Strings.updateDownloadFailed,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    downloadState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                if (release.downloadUrl != null) {
                    TextButton(onClick = onDownload) {
                        Text(Strings.updateRetry)
                    }
                }
            }
        }
        com.webtoapp.core.update.UpdateDownloadState.Idle -> {
            if (release.downloadUrl == null) {
                Text(
                    Strings.versionHistoryNoApk,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (isCurrent) {
                // Already installed — no download button.
            } else {
                TextButton(onClick = onDownload) {
                    Icon(
                        Icons.Outlined.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(Strings.versionHistoryDownload)
                }
            }
        }
    }
}

/**
 * Lightweight Markdown renderer for GitHub release notes, tuned for the
 * "## Added / ## Improved / ## Fixed" + "- item" changelog format.
 *
 * It only handles what release notes actually contain — second-level headings
 * (`##`), unordered list items (`-`/`*`), and plain paragraphs — so it stays
 * dependency-free. Anything else is shown as plain text.
 */
@Composable
private fun ReleaseNotesMarkdown(text: String) {
    val titleColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val lines = remember(text) { text.lines() }
    var inList = false
    Column {
        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trimEnd()
            when {
                // Second-level heading: the Added / Improved / Fixed sections.
                line.startsWith("## ") -> {
                    if (inList) Spacer(Modifier.height(4.dp))
                    inList = false
                    if (index > 0) Spacer(Modifier.height(6.dp))
                    Text(
                        line.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.labelLarge,
                        color = titleColor
                    )
                }
                // Unordered list item.
                line.startsWith("- ") || line.startsWith("* ") -> {
                    if (!inList && index > 0) Spacer(Modifier.height(2.dp))
                    inList = true
                    Row(modifier = Modifier.padding(start = 4.dp, top = 1.dp)) {
                        Text(
                            "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = mutedColor,
                            modifier = Modifier.width(12.dp)
                        )
                        Text(
                            line.removePrefix("- ").removePrefix("* ").trim(),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor
                        )
                    }
                }
                // Blank line: just spacing, closes any open list.
                line.isBlank() -> {
                    inList = false
                }
                // Fallback: plain paragraph text.
                else -> {
                    if (inList) Spacer(Modifier.height(4.dp))
                    inList = false
                    Text(
                        line,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadProgressRow(downloaded: Long, total: Long, speed: Long) {
    val percent = if (total > 0) {
        (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    } else {
        // indeterminate when Content-Length unknown
        -1f
    }
    Text(Strings.updateDownloading, style = MaterialTheme.typography.labelMedium)
    Spacer(Modifier.height(6.dp))
    if (percent >= 0f) {
        androidx.compose.material3.LinearProgressIndicator(
            progress = { percent },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    } else {
        androidx.compose.material3.LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(Modifier.height(6.dp))
    val percentText = if (percent >= 0f) "${(percent * 100).toInt()}% · " else ""
    val totalText = if (total > 0) "${formatBytes(downloaded)} / ${formatBytes(total)}" else formatBytes(downloaded)
    Text(
        "$percentText$totalText · ${formatBytes(speed)}${Strings.updatePerSec}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.size - 1) {
        value /= 1024
        unit++
    }
    return if (unit == 0) "${bytes} B" else "%.1f %s".format(value, units[unit])
}

@Composable
private fun MadeWithLoveFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = Strings.madeWithLove,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

private fun Context.currentVersionName(): String {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: ""
    } catch (_: PackageManager.NameNotFoundException) {
        ""
    }
}

private fun Context.currentVersionCode(): Long {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    } catch (_: PackageManager.NameNotFoundException) {
        0L
    }
}

private fun Context.copyToClipboard(label: String, text: String) {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun Context.openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, Strings.cannotOpenLink, Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        Toast.makeText(this, Strings.cannotOpenLink, Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun aboutAuthorByLine(): String = when (Strings.currentLanguage.value) {
    AppLanguage.CHINESE -> "开发者 Shiaho"
    AppLanguage.ENGLISH -> "by Shiaho"
    AppLanguage.ARABIC -> "بواسطة Shiaho"
    AppLanguage.PORTUGUESE -> "by Shiaho"
    AppLanguage.SPANISH -> "by Shiaho"
    AppLanguage.FRENCH -> "by Shiaho"
    AppLanguage.GERMAN -> "by Shiaho"
    AppLanguage.RUSSIAN -> "by Shiaho"
    AppLanguage.JAPANESE -> "by Shiaho"
    AppLanguage.KOREAN -> "by Shiaho"
}

@Composable
private fun contactSectionTitle(): String = when (Strings.currentLanguage.value) {
    AppLanguage.CHINESE -> "联系作者"
    AppLanguage.ENGLISH -> "Get in touch"
    AppLanguage.ARABIC -> "تواصل مع المؤلف"
    AppLanguage.PORTUGUESE -> "Get in touch"
    AppLanguage.SPANISH -> "Get in touch"
    AppLanguage.FRENCH -> "Get in touch"
    AppLanguage.GERMAN -> "Get in touch"
    AppLanguage.RUSSIAN -> "Get in touch"
    AppLanguage.JAPANESE -> "Get in touch"
    AppLanguage.KOREAN -> "Get in touch"
}

@Composable
private fun docsLabel(): String = when (Strings.currentLanguage.value) {
    AppLanguage.CHINESE -> "官方文档"
    AppLanguage.ENGLISH -> "Docs"
    AppLanguage.ARABIC -> "الوثائق"
    AppLanguage.PORTUGUESE -> "Docs"
    AppLanguage.SPANISH -> "Docs"
    AppLanguage.FRENCH -> "Docs"
    AppLanguage.GERMAN -> "Doku"
    AppLanguage.RUSSIAN -> "Документация"
    AppLanguage.JAPANESE -> "ドキュメント"
    AppLanguage.KOREAN -> "문서"
}

@Composable
private fun discordCommunityLabel(): String = when (Strings.currentLanguage.value) {
    AppLanguage.CHINESE -> "交流群"
    AppLanguage.ENGLISH -> "Community"
    AppLanguage.ARABIC -> "المجتمع"
    AppLanguage.PORTUGUESE -> "Community"
    AppLanguage.SPANISH -> "Community"
    AppLanguage.FRENCH -> "Community"
    AppLanguage.GERMAN -> "Community"
    AppLanguage.RUSSIAN -> "Сообщество"
    AppLanguage.JAPANESE -> "コミュニティ"
    AppLanguage.KOREAN -> "커뮤니티"
}

@Composable
private fun authorLabel(): String = when (Strings.currentLanguage.value) {
    AppLanguage.CHINESE -> "其他项目"
    AppLanguage.ENGLISH -> "More projects"
    AppLanguage.ARABIC -> "مشاريع أخرى"
    AppLanguage.PORTUGUESE -> "More projects"
    AppLanguage.SPANISH -> "More projects"
    AppLanguage.FRENCH -> "More projects"
    AppLanguage.GERMAN -> "More projects"
    AppLanguage.RUSSIAN -> "More projects"
    AppLanguage.JAPANESE -> "More projects"
    AppLanguage.KOREAN -> "More projects"
}

@Composable
private fun groupWord(): String = when (Strings.currentLanguage.value) {
    AppLanguage.CHINESE -> "群"
    AppLanguage.ENGLISH -> "Group"
    AppLanguage.ARABIC -> "مجموعة"
    AppLanguage.PORTUGUESE -> "Group"
    AppLanguage.SPANISH -> "Group"
    AppLanguage.FRENCH -> "Group"
    AppLanguage.GERMAN -> "Group"
    AppLanguage.RUSSIAN -> "Group"
    AppLanguage.JAPANESE -> "Group"
    AppLanguage.KOREAN -> "Group"
}

@Composable
private fun qqCopiedToast(): String = when (Strings.currentLanguage.value) {
    AppLanguage.CHINESE -> "QQ 群号已复制"
    AppLanguage.ENGLISH -> "QQ group number copied"
    AppLanguage.ARABIC -> "تم نسخ رقم مجموعة QQ"
    AppLanguage.PORTUGUESE -> "QQ group number copied"
    AppLanguage.SPANISH -> "QQ group number copied"
    AppLanguage.FRENCH -> "QQ group number copied"
    AppLanguage.GERMAN -> "QQ group number copied"
    AppLanguage.RUSSIAN -> "QQ group number copied"
    AppLanguage.JAPANESE -> "QQ group number copied"
    AppLanguage.KOREAN -> "QQ group number copied"
}

@Composable
private fun versionCopiedToast(): String = when (Strings.currentLanguage.value) {
    AppLanguage.CHINESE -> "版本号已复制"
    AppLanguage.ENGLISH -> "Version copied"
    AppLanguage.ARABIC -> "تم نسخ الإصدار"
    AppLanguage.PORTUGUESE -> "Version copied"
    AppLanguage.SPANISH -> "Version copied"
    AppLanguage.FRENCH -> "Version copied"
    AppLanguage.GERMAN -> "Version copied"
    AppLanguage.RUSSIAN -> "Version copied"
    AppLanguage.JAPANESE -> "Version copied"
    AppLanguage.KOREAN -> "Version copied"
}
