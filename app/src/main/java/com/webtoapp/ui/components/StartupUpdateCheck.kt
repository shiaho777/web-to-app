package com.webtoapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.update.UpdateChecker
import com.webtoapp.core.update.UpdatePrefs
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaLoadingIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StartupUpdateCheck(onOpenAbout: () -> Unit) {
    if (com.webtoapp.BuildConfig.FDROID) return

    val context = LocalContext.current
    val appContext = context.applicationContext
    val prefs = remember { UpdatePrefs.getInstance(appContext) }
    val scope = rememberCoroutineScope()
    val version = remember(appContext) { com.webtoapp.core.host.AppVersionInfo.read(appContext) }

    var autoResult by remember { mutableStateOf<UpdateChecker.Result.UpdateAvailable?>(null) }
    var whatsNewNotes by remember { mutableStateOf<String?>(null) }
    var whatsNewLoading by remember { mutableStateOf(false) }
    var whatsNewVersion by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val lastSeen = prefs.lastSeenVersionCode()
        if (lastSeen == 0L) {
            prefs.setLastSeenVersionCode(version.code)
        } else if (version.code > lastSeen) {
            whatsNewVersion = version.name
            whatsNewLoading = true
            val notes = withContext(Dispatchers.IO) {
                runCatching { UpdateChecker.fetchAllReleases() }.getOrNull()
                    ?.firstOrNull { it.version == version.name }
                    ?.body
                    .orEmpty()
            }
            whatsNewNotes = notes
            whatsNewLoading = false
            prefs.setLastSeenVersionCode(version.code)
        }

        if (!prefs.autoCheckEnabled()) return@LaunchedEffect
        val result = withContext(Dispatchers.IO) { UpdateChecker.check(version.name) }
        val available = result as? UpdateChecker.Result.UpdateAvailable ?: return@LaunchedEffect
        if (available.info.version == prefs.skippedVersion()) return@LaunchedEffect
        autoResult = available
    }

    autoResult?.let { available ->
        WtaAlertDialog(
            onDismissRequest = { autoResult = null },
            icon = Icons.Outlined.Sync,
            iconTint = MaterialTheme.colorScheme.primary,
            title = Strings.updateAvailableTitle,
            content = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "${Strings.updateNewVersionLabel}: v${available.info.version}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${Strings.updateCurrentVersionLabel}: v${available.currentVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (available.info.releaseNotes.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        ReleaseNotesMarkdown(text = available.info.releaseNotes)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    autoResult = null
                    onOpenAbout()
                }) {
                    Text(Strings.updateViewDetails)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        scope.launch { prefs.setSkippedVersion(available.info.version) }
                        autoResult = null
                    }) {
                        Text(Strings.updateSkipVersion)
                    }
                    TextButton(onClick = { autoResult = null }) {
                        Text(Strings.close)
                    }
                }
            }
        )
    }

    if (whatsNewLoading || whatsNewNotes != null) {
        WtaAlertDialog(
            onDismissRequest = {
                whatsNewLoading = false
                whatsNewNotes = null
            },
            icon = Icons.Outlined.Celebration,
            iconTint = MaterialTheme.colorScheme.primary,
            title = Strings.whatsNewTitle(whatsNewVersion),
            content = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (whatsNewLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WtaLoadingIndicator(size = 18.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(Strings.updateChecking, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        val notes = whatsNewNotes.orEmpty()
                        if (notes.isBlank()) {
                            Text(Strings.whatsNewNoNotes, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            ReleaseNotesMarkdown(text = notes)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    whatsNewLoading = false
                    whatsNewNotes = null
                }) {
                    Text(Strings.close)
                }
            }
        )
    }
}
