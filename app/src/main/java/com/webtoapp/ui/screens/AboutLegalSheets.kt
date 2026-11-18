package com.webtoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.FilterNone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.legal.LicenseEntry
import com.webtoapp.core.legal.ThirdPartyLicenseCatalog
import com.webtoapp.core.legal.ThirdPartyLicenses
import com.webtoapp.ui.agent.components.MarkdownText
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.theme.ifDescriptionsShown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThirdPartyLicensesSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val catalogResult by produceState<Result<ThirdPartyLicenseCatalog>?>(initialValue = null, appContext) {
        value = withContext(Dispatchers.IO) {
            runCatching { ThirdPartyLicenses.load(appContext) }
        }
    }
    var expandedKey by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun toggle(key: String) {
        expandedKey = if (expandedKey == key) null else key
    }

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
                AboutTonalIcon(
                    icon = Icons.Rounded.Book,
                    shapeIndex = 4,
                    plateSize = 34.dp,
                    iconSize = 18.dp
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    Strings.thirdPartyLicenses,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            val result = catalogResult
            val loaded = result?.getOrNull()
            if (result != null && loaded == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        Strings.licensesLoadFailed,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (loaded == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WtaLoadingIndicator(size = 28.dp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "project") {
                        LicenseSectionCard(title = Strings.projectLicenseSection) {
                            LicenseEntryRow(
                                entry = LicenseEntry(
                                    name = "WebToApp",
                                    version = null,
                                    spdx = loaded.projectLicense.spdx,
                                    spdxNote = null,
                                    url = loaded.projectLicense.url
                                ),
                                licenseText = loaded.projectLicense.textAsset,
                                fallbackUrl = loaded.projectLicense.url,
                                expanded = expandedKey == "project",
                                onToggle = { toggle("project") }
                            )
                        }
                    }
                    item(key = "libraries") {
                        LicenseSectionCard(title = Strings.bundledLibrariesSection) {
                            loaded.libraries.forEachIndexed { index, entry ->
                                if (index > 0) Spacer(Modifier.height(10.dp))
                                val text = loaded.licenseTexts.firstOrNull { it.spdx == entry.spdx }
                                LicenseEntryRow(
                                    entry = entry,
                                    licenseText = text?.asset,
                                    fallbackUrl = text?.url ?: entry.url,
                                    expanded = expandedKey == "lib:$index",
                                    onToggle = { toggle("lib:$index") }
                                )
                            }
                        }
                    }
                    item(key = "runtimes") {
                        LicenseSectionCard(title = Strings.runtimeLicensesSection) {
                            Text(
                                Strings.runtimeLicenseNote.ifDescriptionsShown() ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            loaded.runtimes.forEachIndexed { index, entry ->
                                Spacer(Modifier.height(10.dp))
                                val text = loaded.licenseTexts.firstOrNull { it.spdx == entry.spdx }
                                LicenseEntryRow(
                                    entry = entry,
                                    licenseText = text?.asset,
                                    fallbackUrl = text?.url ?: entry.url,
                                    expanded = expandedKey == "rt:$index",
                                    onToggle = { toggle("rt:$index") }
                                )
                            }
                        }
                    }
                    item(key = "texts") {
                        LicenseSectionCard(title = Strings.licenseFullTextSection) {
                            loaded.licenseTexts.forEachIndexed { index, ref ->
                                if (index > 0) Spacer(Modifier.height(6.dp))
                                LicenseTextRow(
                                    spdx = ref.spdx,
                                    asset = ref.asset,
                                    url = ref.url,
                                    expanded = expandedKey == "text:$index",
                                    onToggle = { toggle("text:$index") }
                                )
                            }
                        }
                    }
                    item(key = "bottom_spacer") { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun LicenseSectionCard(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = MaterialTheme.colorScheme.primary
        )
        WtaCard(modifier = Modifier.fillMaxWidth()) {
            Column { content() }
        }
    }
}

@Composable
private fun LicenseEntryRow(
    entry: LicenseEntry,
    licenseText: String?,
    fallbackUrl: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.name + if (!entry.version.isNullOrBlank()) " · ${entry.version}" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    entry.spdx,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (licenseText != null) {
                TextButton(onClick = onToggle) {
                    Text(Strings.viewLicenseText, style = MaterialTheme.typography.labelMedium)
                }
            } else {
                TextButton(onClick = { context.openUrl(fallbackUrl) }) {
                    Text(Strings.viewLicenseText, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.NorthEast,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        if (!entry.spdxNote.isNullOrBlank()) {
            Text(
                entry.spdxNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (expanded && licenseText != null) {
            LicenseFullText(assetPath = licenseText)
        }
    }
}

@Composable
private fun LicenseTextRow(
    spdx: String,
    asset: String?,
    url: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                spdx,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (asset != null) {
                TextButton(onClick = onToggle) {
                    Text(Strings.viewLicenseText, style = MaterialTheme.typography.labelMedium)
                }
            } else {
                TextButton(onClick = { context.openUrl(url) }) {
                    Text(Strings.viewLicenseText, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.NorthEast,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        if (expanded && asset != null) {
            LicenseFullText(assetPath = asset)
        }
    }
}

@Composable
private fun LicenseFullText(assetPath: String) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val text by produceState<String?>(initialValue = null, assetPath) {
        value = withContext(Dispatchers.IO) {
            runCatching { ThirdPartyLicenses.readLicenseText(appContext, assetPath) }.getOrNull()
        }
    }
    val loaded = text
    if (loaded == null) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            WtaLoadingIndicator(size = 20.dp)
        }
    } else {
        SelectionContainer {
            Text(
                loaded,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrivacyStatementSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val policyUrl = if (Strings.currentLanguage.value == AppLanguage.CHINESE) {
        ThirdPartyLicenses.DOCS_PRIVACY_URL_ZH
    } else {
        ThirdPartyLicenses.DOCS_PRIVACY_URL_EN
    }

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
                AboutTonalIcon(
                    icon = Icons.Rounded.FilterNone,
                    shapeIndex = 5,
                    plateSize = 34.dp,
                    iconSize = 18.dp
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    Strings.privacyStatement,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PrivacyBlock(Strings.privacyNoTrackingTitle, Strings.privacyNoTrackingBody)
                PrivacyBlock(Strings.privacyLocalDataTitle, Strings.privacyLocalDataBody)
                PrivacyBlock(Strings.privacyNetworkTitle, Strings.privacyNetworkBody)
                TextButton(onClick = { context.openUrl(policyUrl) }) {
                    Text(Strings.openFullPrivacyPolicy, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.NorthEast,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PrivacyBlock(title: String, body: String) {
    WtaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.primary
            )
            MarkdownText(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
