package com.webtoapp.ui.screens

import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.apkbuilder.ApkInstaller
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.ui.components.InstallPermissionDialog
import com.webtoapp.ui.components.formatFileSize
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private data class ApkCandidate(
    val file: File,
    val displayName: String,
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long
)

@Composable
fun ApkInstallScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var candidate by remember { mutableStateOf<ApkCandidate?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var showInstallPermissionDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val invalidApkMessage = Strings.invalidApkFile

    if (showInstallPermissionDialog) {
        InstallPermissionDialog(
            onDismiss = { showInstallPermissionDialog = false }
        )
    }

    val apkPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            busy = true
            error = null
            scope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        copyAndInspectApk(context, uri, invalidApkMessage)
                    }
                }
                busy = false
                result.onSuccess {
                    candidate = it
                }.onFailure {
                    AppLogger.e("ApkInstallScreen", "APK selection failed", it)
                    error = it.message ?: invalidApkMessage
                }
            }
        }
    }

    WtaScreen(
        title = Strings.installApkCenter,
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WtaCard(tone = WtaCardTone.Highlighted) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = Strings.installApkDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    WtaButton(
                        onClick = { apkPicker.launch(arrayOf("application/vnd.android.package-archive", "*/*")) },
                        text = if (busy) Strings.appModifierWorking else Strings.selectApkFile,
                        enabled = !busy,
                        leadingIcon = Icons.Outlined.FolderOpen,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            error?.let {
                WtaCard(tone = WtaCardTone.Surface) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            candidate?.let { apk ->
                WtaCard(tone = WtaCardTone.Surface) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            Icons.Outlined.Android,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = apk.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        ApkMetadataRow(Strings.apkPackageNameLabel, apk.packageName, monospace = true)
                        ApkMetadataRow(
                            Strings.apkVersionLabel,
                            "${apk.versionName} (${apk.versionCode})"
                        )
                        ApkMetadataRow(Strings.apkSizeLabel, formatFileSize(apk.file.length()))
                        Text(
                            text = apk.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        WtaButton(
                            onClick = {
                                if (!ApkInstaller.canInstallFromThisApp(context)) {
                                    showInstallPermissionDialog = true
                                } else {
                                    val launched = ApkInstaller.install(
                                        context.applicationContext,
                                        apk.file
                                    )
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (launched) Strings.apkInstallerOpened
                                            else Strings.apkInstallerLaunchFailed
                                        )
                                    }
                                }
                            },
                            text = Strings.install,
                            leadingIcon = Icons.Outlined.InstallMobile,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApkMetadataRow(label: String, value: String, monospace: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default
        )
    }
}

private fun copyAndInspectApk(
    context: Context,
    uri: Uri,
    invalidApkMessage: String
): ApkCandidate {
    val displayName = context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        } else {
            null
        }
    } ?: uri.lastPathSegment ?: invalidApkMessage

    val directory = File(context.cacheDir, "apk_install").apply {
        if (!exists() && !mkdirs()) {
            throw IllegalStateException(invalidApkMessage)
        }
    }
    val stagedFile = File(directory, "${UUID.randomUUID()}.apk")
    try {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException(invalidApkMessage)
        input.use { source ->
            FileOutputStream(stagedFile).use { destination ->
                source.copyTo(destination, bufferSize = 64 * 1024)
                destination.fd.sync()
            }
        }

        val packageInfo = readPackageInfo(context, stagedFile)
            ?: throw IllegalArgumentException(invalidApkMessage)
        val applicationInfo = packageInfo.applicationInfo
            ?: throw IllegalArgumentException(invalidApkMessage)
        applicationInfo.sourceDir = stagedFile.absolutePath
        applicationInfo.publicSourceDir = stagedFile.absolutePath
        val appName = context.packageManager.getApplicationLabel(applicationInfo).toString()
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        return ApkCandidate(
            file = stagedFile,
            displayName = displayName,
            appName = appName,
            packageName = packageInfo.packageName,
            versionName = packageInfo.versionName.orEmpty(),
            versionCode = versionCode
        )
    } catch (e: Exception) {
        stagedFile.delete()
        throw e
    }
}

private fun readPackageInfo(context: Context, file: File): PackageInfo? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageArchiveInfo(
            file.absolutePath,
            android.content.pm.PackageManager.PackageInfoFlags.of(0)
        )
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
    }
}
