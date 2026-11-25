package com.webtoapp.ui.screens

import androidx.compose.animation.*
import com.webtoapp.ui.design.WtaExpandIn
import com.webtoapp.ui.design.WtaShrinkOut
import com.webtoapp.ui.design.WtaSwitch
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.scraper.WebsiteScraper
import androidx.compose.runtime.saveable.rememberSaveable
import com.webtoapp.ui.viewmodel.OfflinePackDraft
import com.webtoapp.ui.viewmodel.OfflinePackDraftState
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.components.*
import com.webtoapp.ui.screens.create.WtaCreateFlowScaffold
import com.webtoapp.ui.screens.create.WtaCreateFlowSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateOfflinePackScreen(
    state: OfflinePackDraftState,
    onBack: () -> Unit,
    onStartScrape: (name: String, config: WebsiteScraper.ScrapeConfig) -> Unit,
    onClosePreview: () -> Unit,
    onDismissError: () -> Unit,
    onSavePack: (OfflinePackDraft) -> Unit
) {
    var url by rememberSaveable { mutableStateOf("") }
    var appName by rememberSaveable { mutableStateOf("") }
    var maxDepth by rememberSaveable { mutableIntStateOf(3) }
    var downloadCdn by rememberSaveable { mutableStateOf(true) }
    var followLinks by rememberSaveable { mutableStateOf(true) }
    var maxFiles by rememberSaveable { mutableIntStateOf(500) }
    var maxTotalSizeMb by rememberSaveable { mutableIntStateOf(200) }
    var skipPatterns by rememberSaveable { mutableStateOf("") }
    var timeoutSeconds by rememberSaveable { mutableIntStateOf(30) }
    var showAdvanced by rememberSaveable { mutableStateOf(false) }

    val isScraping = state is OfflinePackDraftState.Scraping
    val scrapeProgress = (state as? OfflinePackDraftState.Scraping)?.progress
    if (state is OfflinePackDraftState.Ready) {
        OfflinePackPreviewScreen(state.draft, onClosePreview) { onSavePack(state.draft) }
        return
    }
    if (state is OfflinePackDraftState.Failed) {
        WtaAlertDialog(
            onDismissRequest = onDismissError,
            title = Strings.failed,
            text = state.message,
            confirmButton = { TextButton(onClick = onDismissError) { Text(Strings.close) } }
        )
    }
    val startScrape = {
        if (url.isNotBlank() && !isScraping) {
            val input = url.trim()
            val finalUrl = if (input.startsWith("http://", true) || input.startsWith("https://", true)) input else "https://$input"
            onStartScrape(
                appName,
                WebsiteScraper.ScrapeConfig(
                    url = finalUrl,
                    maxDepth = maxDepth,
                    downloadCdnResources = downloadCdn,
                    followLinks = followLinks,
                    maxFiles = maxFiles,
                    maxTotalSize = maxTotalSizeMb * 1024L * 1024L,
                    skipPatterns = skipPatterns.split(',', ' ', ';').map(String::trim).filter(String::isNotEmpty),
                    timeoutSeconds = timeoutSeconds
                )
            )
        }
    }

    WtaCreateFlowScaffold(
        title = Strings.websiteOfflinePackTitle,
        onBack = onBack,
        actions = {
            TextButton(
                onClick = startScrape,
                enabled = url.isNotBlank() && !isScraping
            ) {
                Text(
                    Strings.startScraping,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) {
        WtaCreateFlowSection(title = Strings.labelBasicInfo) {
            EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                PremiumTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        if (appName.isBlank() && it.isNotBlank()) {
                            try {
                                val host = java.net.URL(
                                    if (it.startsWith("http")) it else "https://$it"
                                ).host
                                appName = host.removePrefix("www.")
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text(Strings.websiteUrl) },
                    placeholder = { Text("https://example.com") },
                    leadingIcon = { Icon(Icons.Outlined.Link, null) },
                    singleLine = true,
                    enabled = !isScraping,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    )
                )

                PremiumTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text(Strings.labelAppName) },
                    placeholder = { Text(Strings.myOfflineApp) },
                    leadingIcon = { Icon(Icons.Outlined.Label, null) },
                    singleLine = true,
                    enabled = !isScraping,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                }
            }
        }

        WtaCreateFlowSection(title = Strings.scrapeStrategy) {
                EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.TravelExplore,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                Strings.scrapeStrategy,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                Strings.crawlDepth,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                Strings.depthLayers.format(maxDepth),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = maxDepth.toFloat(),
                            onValueChange = { maxDepth = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            enabled = !isScraping
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    Strings.followLinks,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    Strings.followLinksDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            WtaSwitch(
                                checked = followLinks,
                                onCheckedChange = { followLinks = it }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    Strings.downloadCdnResources,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    Strings.downloadCdnDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            WtaSwitch(
                                checked = downloadCdn,
                                onCheckedChange = { downloadCdn = it }
                            )
                        }
                    }
                }
        }

        WtaCreateFlowSection(title = Strings.advancedConfig) {
                EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Tune,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    Strings.advancedConfig,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            WtaSwitch(
                                checked = showAdvanced,
                                onCheckedChange = { showAdvanced = it }
                            )
                        }

                        AnimatedVisibility(
                            visible = showAdvanced,
                            enter = WtaExpandIn,
                            exit = WtaShrinkOut
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        Strings.maxFiles,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "$maxFiles",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = maxFiles.toFloat(),
                                    onValueChange = { maxFiles = it.toInt() },
                                    valueRange = 50f..1000f,
                                    steps = 18,
                                    enabled = !isScraping
                                )

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        Strings.maxTotalSize,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "${maxTotalSizeMb} MB",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = maxTotalSizeMb.toFloat(),
                                    onValueChange = { maxTotalSizeMb = it.toInt() },
                                    valueRange = 50f..500f,
                                    steps = 8,
                                    enabled = !isScraping
                                )

                                HorizontalDivider()

                                PremiumTextField(
                                    value = skipPatterns,
                                    onValueChange = { skipPatterns = it },
                                    label = { Text(Strings.skipPatterns) },
                                    placeholder = { Text(Strings.skipPatternsHint) },
                                    leadingIcon = { Icon(Icons.Outlined.FilterAlt, null) },
                                    singleLine = true,
                                    enabled = !isScraping,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        Strings.scrapeTimeout,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "${timeoutSeconds}s",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = timeoutSeconds.toFloat(),
                                    onValueChange = { timeoutSeconds = it.toInt() },
                                    valueRange = 10f..300f,
                                    steps = 57,
                                    enabled = !isScraping
                                )
                                Text(
                                    Strings.scrapeTimeoutDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
        }

                if (!isScraping) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                Strings.scraperLongDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isScraping && scrapeProgress == null) {
                    Text(Strings.preparing, modifier = Modifier.padding(16.dp))
                }
                if (isScraping && scrapeProgress != null) {
                    val progress = scrapeProgress

                    val animatedFileCount by animateIntAsState(
                        targetValue = progress.downloadedFiles,
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "fileCount"
                    )

                    EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val phaseIcon = when (progress.phase) {
                                    WebsiteScraper.ScrapeProgress.Phase.ANALYZING -> Icons.Outlined.Search
                                    WebsiteScraper.ScrapeProgress.Phase.DOWNLOADING -> Icons.Outlined.CloudDownload
                                    WebsiteScraper.ScrapeProgress.Phase.REWRITING -> Icons.Outlined.AutoFixHigh
                                    WebsiteScraper.ScrapeProgress.Phase.COMPLETE -> Icons.Outlined.CheckCircle
                                    WebsiteScraper.ScrapeProgress.Phase.ERROR -> Icons.Outlined.Error
                                }
                                val phaseColor = when (progress.phase) {
                                    WebsiteScraper.ScrapeProgress.Phase.COMPLETE -> MaterialTheme.colorScheme.primary
                                    WebsiteScraper.ScrapeProgress.Phase.ERROR -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.primary
                                }

                                Icon(phaseIcon, null, modifier = Modifier.size(20.dp), tint = phaseColor)
                                Text(
                                    progress.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatChip(
                                    label = Strings.statFiles,
                                    value = "$animatedFileCount",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                StatChip(
                                    label = Strings.statSize,
                                    value = formatBytes(progress.downloadedBytes),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (progress.phase != WebsiteScraper.ScrapeProgress.Phase.COMPLETE &&
                                progress.phase != WebsiteScraper.ScrapeProgress.Phase.ERROR
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    gapSize = ProgressIndicatorDefaults.LinearIndicatorTrackGapSize
                                )
                            }

                            if (progress.currentFile.isNotBlank()) {
                                Text(
                                    progress.currentFile,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (!isScraping) {
                    PremiumButton(
                        onClick = startScrape,
                        enabled = url.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.CloudDownload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Strings.startScraping)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }

@Composable
private fun StatChip(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "${bytes} B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}
