package com.webtoapp.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.backup.DataBackupManager
import com.webtoapp.core.backup.PerAppSigningBackup
import com.webtoapp.core.backup.SigningBackupPasswordRequired
import com.webtoapp.core.backup.SigningBackupUnreadable
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.design.WtaMotion
import com.webtoapp.ui.design.WtaStatusBanner
import com.webtoapp.ui.design.WtaStatusTone
import com.webtoapp.ui.theme.LocalShowDescriptions
import com.webtoapp.ui.theme.ifDescriptionsShown
import kotlinx.coroutines.launch

@Composable
fun DataBackupDialog(onDismiss: () -> Unit) {
    WtaAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.Inventory2,
        title = Strings.dataBackupTitle,
        confirmButton = {
            WtaButton(
                onClick = onDismiss,
                text = Strings.close,
                variant = WtaButtonVariant.Text,
                size = WtaButtonSize.Medium
            )
        },
        content = {
            Strings.dataBackupDesc.ifDescriptionsShown()?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DataBackupControls()
        }
    )
}

@Composable
private fun ColumnScope.DataBackupControls() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backupManager = remember { DataBackupManager(context) }
    val repository = remember { WebToAppApplication.repository }

    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf("") }
    var errorThrowable by remember { mutableStateOf<Throwable?>(null) }
    var errorScope by remember { mutableStateOf("") }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showLegacyMissingFilesDialog by remember { mutableStateOf(false) }
    var chooseSigningBackup by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf<String?>(null) }
    var exportWithKeys by rememberSaveable { mutableStateOf(false) }
    var pendingExportUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var lockedBackup by rememberSaveable { mutableStateOf<Uri?>(null) }
    var passwordError by remember { mutableStateOf(false) }
    var missingSigningKeys by remember { mutableStateOf(0) }
    val apps by repository.allWebApps.collectAsState(initial = emptyList())
    val hasSigningProjects = apps.any {
        it.apkExportConfig?.let { config ->
            config.perAppSigningEnabled || config.publishedPerAppSigning != null
        } == true
    }
    val isBusy = isExporting || isImporting

    fun writeBackup(uri: Uri, password: String?) {
        scope.launch {
            isExporting = true
            val result = backupManager.exportAllData(
                repository = repository,
                outputUri = uri,
                signingPassword = password,
                onProgress = { _, _, message -> progressMessage = message }
            )
            isExporting = false
            progressMessage = ""
            result.onSuccess { exportResult ->
                missingSigningKeys = exportResult.missingSigningKeys
                Toast.makeText(
                    context,
                    if (password != null && exportResult.signingKeyCount == 0 && missingSigningKeys == 0) {
                        Strings.signingBackupNoKeys
                    } else {
                        Strings.backupExportSuccess.format(exportResult.appCount, exportResult.resourceCount)
                    },
                    Toast.LENGTH_LONG
                ).show()
            }.onFailure { e ->
                errorScope = "Data backup export"
                errorThrowable = e
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        val password = exportPassword
        val keysRequested = exportWithKeys
        exportPassword = null
        exportWithKeys = false
        if (uri != null) {
            if (keysRequested && password == null) {
                pendingExportUri = uri
            } else {
                writeBackup(uri, password)
            }
        }
    }

    fun restore(uri: Uri, password: String?) {
        scope.launch {
            isImporting = true
            lockedBackup = null
            missingSigningKeys = 0
            val result = backupManager.importAllData(
                repository = repository,
                inputUri = uri,
                signingPassword = password,
                onProgress = { _, _, message ->
                    progressMessage = message
                }
            )
            isImporting = false
            progressMessage = ""
            result.onSuccess { importResult ->
                Toast.makeText(
                    context,
                    if (importResult.legacySourceBundle) {
                        Strings.backupImportLegacySuccess.format(
                            importResult.importedCount,
                            importResult.totalCount
                        )
                    } else if (importResult.duplicateCount > 0) {
                        Strings.backupImportSuccessDupes.format(
                            importResult.importedCount,
                            importResult.totalCount,
                            importResult.duplicateCount
                        )
                    } else {
                        Strings.backupImportSuccess.format(
                            importResult.importedCount,
                            importResult.totalCount
                        )
                    },
                    Toast.LENGTH_LONG
                ).show()
                missingSigningKeys = importResult.missingSigningKeys
                showLegacyMissingFilesDialog = importResult.missingProjectFiles
                showRestartDialog = importResult.localFilesRestored
            }.onFailure { error ->
                if (error is SigningBackupPasswordRequired || error is SigningBackupUnreadable) {
                    passwordError = error is SigningBackupUnreadable
                    lockedBackup = uri
                } else {
                    errorScope = "Data backup import"
                    errorThrowable = error
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) restore(uri, null)
    }

    if (chooseSigningBackup) {
        SigningBackupPasswordDialog(
            exporting = true,
            onConfirm = { password ->
                chooseSigningBackup = false
                exportPassword = password
                exportWithKeys = true
                exportLauncher.launch(backupManager.generateBackupFileName())
            },
            onDismiss = { chooseSigningBackup = false },
            onWithoutKeys = {
                chooseSigningBackup = false
                exportLauncher.launch(backupManager.generateBackupFileName())
            }
        )
    }

    pendingExportUri?.let { uri ->
        SigningBackupPasswordDialog(
            exporting = true,
            onConfirm = { password ->
                pendingExportUri = null
                writeBackup(uri, password)
            },
            onDismiss = { pendingExportUri = null }
        )
    }

    lockedBackup?.let { uri ->
        SigningBackupPasswordDialog(
            exporting = false,
            invalidPassword = passwordError,
            onConfirm = { restore(uri, it) },
            onDismiss = { lockedBackup = null }
        )
    }

    errorThrowable?.let { err ->
        WtaErrorDialog(
            scope = errorScope,
            message = err.message,
            throwable = err,
            onDismiss = { errorThrowable = null }
        )
    }

    if (showRestartDialog) {
        com.webtoapp.ui.design.WtaAlertDialog(
            onDismissRequest = { showRestartDialog = false },
            icon = Icons.Outlined.RestartAlt,
            title = Strings.backupRestartTitle,
            text = if (missingSigningKeys > 0) {
                Strings.signingBackupMissingKey + "\n\n" + Strings.backupRestartMessage
            } else Strings.backupRestartMessage,
            confirmButton = {
                WtaButton(
                    onClick = {
                        showRestartDialog = false
                        backupManager.scheduleAppRestart()
                    },
                    text = Strings.backupRestartNow,
                    variant = WtaButtonVariant.Primary,
                    size = WtaButtonSize.Medium
                )
            },
            dismissButton = {
                WtaButton(
                    onClick = { showRestartDialog = false },
                    text = Strings.backupRestartLater,
                    variant = WtaButtonVariant.Text,
                    size = WtaButtonSize.Medium
                )
            }
        )
    }

    if (showLegacyMissingFilesDialog) {
        com.webtoapp.ui.design.WtaAlertDialog(
            onDismissRequest = { showLegacyMissingFilesDialog = false },
            icon = Icons.Outlined.Inventory2,
            title = Strings.dataBackupTitle,
            text = Strings.backupImportLegacyMissingFiles,
            confirmButton = {
                WtaButton(
                    onClick = { showLegacyMissingFilesDialog = false },
                    text = Strings.close,
                    variant = WtaButtonVariant.Primary,
                    size = WtaButtonSize.Medium
                )
            }
        )
    }

    AnimatedVisibility(
        visible = isBusy,
        enter = fadeIn(WtaMotion.enterTween()),
        exit = fadeOut(WtaMotion.exitTween())
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                gapSize = ProgressIndicatorDefaults.LinearIndicatorTrackGapSize
            )
            if (progressMessage.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    progressMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(14.dp))
        }
    }

    Spacer(Modifier.height(12.dp))
    WtaStatusBanner(
        message = Strings.dataBackupUninstallWarning,
        tone = WtaStatusTone.Warning
    )
    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ExportButton(
            isExporting = isExporting,
            enabled = !isBusy,
            onClick = {
                if (hasSigningProjects) {
                    chooseSigningBackup = true
                } else {
                    exportLauncher.launch(backupManager.generateBackupFileName())
                }
            },
            modifier = Modifier.weight(1f)
        )
        ImportButton(
            isImporting = isImporting,
            enabled = !isBusy,
            onClick = {
                importLauncher.launch(arrayOf("application/zip"))
            },
            modifier = Modifier.weight(1f)
        )
    }

    if (missingSigningKeys > 0) {
        Spacer(Modifier.height(12.dp))
        WtaStatusBanner(
            message = Strings.signingBackupMissingKey,
            tone = WtaStatusTone.Warning
        )
    }

    AnimatedVisibility(
        visible = !isBusy && LocalShowDescriptions.current,
        enter = fadeIn(WtaMotion.enterTween()),
        exit = fadeOut(WtaMotion.exitTween())
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.height(12.dp))
            WtaStatusBanner(
                message = Strings.dataBackupNote,
                tone = WtaStatusTone.Info
            )
        }
    }
}

@Composable
private fun SigningBackupPasswordDialog(
    exporting: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    invalidPassword: Boolean = false,
    onWithoutKeys: (() -> Unit)? = null
) {
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var showInvalidPassword by remember(invalidPassword) { mutableStateOf(invalidPassword) }
    val ready = if (exporting) {
        password.length >= PerAppSigningBackup.MIN_PASSWORD_LENGTH &&
            password.isNotBlank() && confirmation == password
    } else password.isNotBlank()

    WtaAlertDialog(
        onDismissRequest = onDismiss,
        title = if (exporting) Strings.signingBackupTitle else Strings.signingBackupUnlock,
        confirmButton = {
            WtaButton(
                onClick = { onConfirm(password) },
                text = if (exporting) Strings.signingBackupExport else Strings.importData,
                enabled = ready
            )
        },
        dismissButton = {
            WtaButton(
                onClick = onWithoutKeys ?: onDismiss,
                text = if (onWithoutKeys != null) Strings.signingBackupWithoutKeys else Strings.cancel,
                variant = WtaButtonVariant.Text
            )
        },
        content = {
            if (exporting) {
                Text(Strings.signingBackupPasswordHint, style = MaterialTheme.typography.bodyMedium)
            }
            PremiumTextField(
                value = password,
                onValueChange = {
                    password = it
                    showInvalidPassword = false
                },
                label = { Text(Strings.signingBackupPassword) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = showInvalidPassword,
                supportingText = if (showInvalidPassword) {
                    { Text(Strings.signingBackupUnreadable) }
                } else null
            )
            if (exporting) {
                PremiumTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it },
                    label = { Text(Strings.signingBackupConfirmPassword) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = confirmation.isNotEmpty() && confirmation != password
                )
            }
        }
    )
}

@Composable
private fun ExportButton(
    isExporting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isExporting) {
        WtaCard(
            modifier = modifier,
            tone = WtaCardTone.Surface,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 10.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                WtaLoadingIndicator(
                    size = 16.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    Strings.exportData,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        WtaButton(
            onClick = onClick,
            text = Strings.exportData,
            variant = WtaButtonVariant.Outlined,
            size = WtaButtonSize.Medium,
            enabled = enabled,
            leadingIcon = Icons.Outlined.Upload,
            modifier = modifier
        )
    }
}

@Composable
private fun ImportButton(
    isImporting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isImporting) {
        WtaCard(
            modifier = modifier,
            tone = WtaCardTone.Highlighted,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 10.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                WtaLoadingIndicator(
                    size = 16.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    Strings.importData,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    } else {
        WtaButton(
            onClick = onClick,
            text = Strings.importData,
            variant = WtaButtonVariant.Primary,
            size = WtaButtonSize.Medium,
            enabled = enabled,
            leadingIcon = Icons.Outlined.Download,
            modifier = modifier
        )
    }
}
