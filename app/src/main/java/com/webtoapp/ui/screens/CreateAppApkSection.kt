package com.webtoapp.ui.screens

import com.webtoapp.ui.components.PremiumFilterChip
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.*
import com.webtoapp.ui.components.*
import com.webtoapp.ui.design.*
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.util.AppConstants
import com.webtoapp.util.ConfigPresetStorage
import com.webtoapp.util.NetworkTrustStorage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

private val PACKAGE_NAME_REGEX = AppConstants.PACKAGE_NAME_REGEX

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApkExportSection(
    config: ApkExportConfig,
    onConfigChange: (ApkExportConfig) -> Unit,
    onOpenPermissionConfig: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val packageNameBringIntoViewRequester = remember { BringIntoViewRequester() }
    val versionNameBringIntoViewRequester = remember { BringIntoViewRequester() }
    val versionCodeBringIntoViewRequester = remember { BringIntoViewRequester() }

    val context = LocalContext.current
    var caImportError by remember { mutableStateOf<String?>(null) }
    val caPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            NetworkTrustStorage.importCertificate(context, uri)
        }.onSuccess { cert ->
            val next = config.networkTrustConfig.copy(
                customCaCertificates = config.networkTrustConfig.customCaCertificates + cert
            )
            caImportError = null
            onConfigChange(config.copy(networkTrustConfig = next))
        }.onFailure { error ->
            caImportError = error.message ?: Strings.invalidCertificate
        }
    }

    val packageName = config.customPackageName ?: ""
    val isPackageNameInvalid = packageName.isNotBlank() &&
        !packageName.matches(PACKAGE_NAME_REGEX)

    Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.SectionGap)) {

        WtaSection(
            title = Strings.apkConfigNote,
            headerStyle = WtaSectionHeaderStyle.Quiet
        ) {
            WtaSettingCard {
                Column(
                    modifier = Modifier.padding(
                        horizontal = WtaSpacing.RowHorizontal,
                        vertical = WtaSpacing.ContentGap
                    ),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                ) {
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = {
                            onConfigChange(config.copy(customPackageName = it.ifBlank { null }))
                        },
                        label = { Text(Strings.customPackageName) },
                        placeholder = { Text(Strings.apkPackageNamePlaceholder) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(packageNameBringIntoViewRequester)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        packageNameBringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                        isError = isPackageNameInvalid,
                        supportingText = {
                            if (isPackageNameInvalid) {
                                Text(
                                    Strings.packageNameInvalidFormat,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(Strings.packageNameHint)
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                    ) {
                        OutlinedTextField(
                            value = config.customVersionName ?: "",
                            onValueChange = {
                                onConfigChange(config.copy(customVersionName = it.ifBlank { null }))
                            },
                            label = { Text(Strings.versionName) },
                            placeholder = { Text(Strings.apkVersionNamePlaceholder) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .bringIntoViewRequester(versionNameBringIntoViewRequester)
                                .onFocusEvent { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            versionNameBringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                }
                        )

                        OutlinedTextField(
                            value = config.customVersionCode?.toString() ?: "",
                            onValueChange = { input ->
                                val code = input.filter { it.isDigit() }.toIntOrNull()
                                onConfigChange(config.copy(customVersionCode = code))
                            },
                            label = { Text(Strings.versionCode) },
                            placeholder = { Text(Strings.apkVersionCodePlaceholder) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .bringIntoViewRequester(versionCodeBringIntoViewRequester)
                                .onFocusEvent { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            versionCodeBringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        }

        WtaSection(
            title = Strings.apkArchitecture,
            headerStyle = WtaSectionHeaderStyle.Quiet
        ) {
            WtaSettingCard {
                Column(
                    modifier = Modifier.padding(
                        horizontal = WtaSpacing.RowHorizontal,
                        vertical = WtaSpacing.ContentGap
                    ),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                    ) {
                        ApkArchitecture.entries.forEach { arch ->
                            val isSelected = config.architecture == arch
                            PremiumFilterChip(
                                selected = isSelected,
                                onClick = { onConfigChange(config.copy(architecture = arch)) },
                                label = { Text(arch.displayName) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Text(
                        text = config.architecture.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (onOpenPermissionConfig != null) {
            PermissionSummaryCard(
                permissions = config.runtimePermissions,
                onClick = onOpenPermissionConfig
            )
        }

        NetworkTrustConfigPanel(
            config = config.networkTrustConfig,
            importError = caImportError,
            onConfigChange = { networkTrustConfig ->
                onConfigChange(config.copy(networkTrustConfig = networkTrustConfig))
            },
            onImportCertificate = {
                caPickerLauncher.launch(
                    arrayOf(
                        "application/x-x509-ca-cert",
                        "application/x-pem-file",
                        "application/octet-stream",
                        "text/plain",
                        "*/*"
                    )
                )
            }
        )

        PerformanceOptimizationSection(
            config = config,
            onConfigChange = onConfigChange
        )

        CustomSigningSection()

        SigningSchemeSection()
    }
}

@Composable
private fun PerformanceOptimizationSection(
    config: ApkExportConfig,
    onConfigChange: (ApkExportConfig) -> Unit
) {
    WtaSection(
        title = Strings.performanceOptimization,
        headerStyle = WtaSectionHeaderStyle.Quiet
    ) {
        WtaSettingCard {
            WtaToggleRow(
                title = Strings.performanceOptimization,
                subtitle = if (config.performanceOptimization) Strings.perfEnabled else Strings.perfDisabled,
                icon = Icons.Outlined.Speed,
                checked = config.performanceOptimization,
                onCheckedChange = { onConfigChange(config.copy(performanceOptimization = it)) }
            )
        }

        AnimatedVisibility(
            visible = config.performanceOptimization,
            enter = CardExpandTransition,
            exit = CardCollapseTransition
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.SectionGap)) {

                WtaSection(
                    title = Strings.perfResourceOptimize,
                    headerStyle = WtaSectionHeaderStyle.Quiet
                ) {
                    WtaSettingCard {
                        WtaToggleRow(
                            title = Strings.perfCompressImages,
                            subtitle = Strings.perfCompressImagesHint,
                            checked = config.performanceConfig.compressImages,
                            onCheckedChange = {
                                onConfigChange(config.copy(performanceConfig = config.performanceConfig.copy(compressImages = it)))
                            }
                        )
                        WtaSectionDivider()
                        WtaToggleRow(
                            title = Strings.perfConvertWebP,
                            subtitle = Strings.perfConvertWebPHint,
                            checked = config.performanceConfig.convertToWebP,
                            onCheckedChange = {
                                onConfigChange(config.copy(performanceConfig = config.performanceConfig.copy(convertToWebP = it)))
                            }
                        )
                        WtaSectionDivider()
                        WtaToggleRow(
                            title = Strings.perfMinifyCode,
                            subtitle = Strings.perfMinifyCodeHint,
                            checked = config.performanceConfig.minifyCode,
                            onCheckedChange = {
                                onConfigChange(config.copy(performanceConfig = config.performanceConfig.copy(minifyCode = it)))
                            }
                        )
                        WtaSectionDivider()
                        WtaToggleRow(
                            title = Strings.perfRemoveUnused,
                            subtitle = Strings.perfRemoveUnusedHint,
                            checked = config.performanceConfig.removeUnusedResources,
                            onCheckedChange = {
                                onConfigChange(config.copy(performanceConfig = config.performanceConfig.copy(removeUnusedResources = it)))
                            }
                        )
                    }
                }

                WtaSection(
                    title = Strings.perfBuildOptimize,
                    headerStyle = WtaSectionHeaderStyle.Quiet
                ) {
                    WtaSettingCard {
                        WtaToggleRow(
                            title = Strings.perfParallelProcessing,
                            subtitle = Strings.perfParallelProcessingHint,
                            checked = config.performanceConfig.parallelProcessing,
                            onCheckedChange = {
                                onConfigChange(config.copy(performanceConfig = config.performanceConfig.copy(parallelProcessing = it)))
                            }
                        )
                        WtaSectionDivider()
                        WtaToggleRow(
                            title = Strings.perfEnableCache,
                            subtitle = Strings.perfEnableCacheHint,
                            checked = config.performanceConfig.enableCache,
                            onCheckedChange = {
                                onConfigChange(config.copy(performanceConfig = config.performanceConfig.copy(enableCache = it)))
                            }
                        )
                    }
                }

                WtaSection(
                    title = Strings.perfLoadOptimize,
                    headerStyle = WtaSectionHeaderStyle.Quiet
                ) {
                    WtaSettingCard {
                        WtaToggleRow(
                            title = Strings.perfPreloadHints,
                            subtitle = Strings.perfPreloadHintsHint,
                            checked = config.performanceConfig.injectPreloadHints,
                            onCheckedChange = {
                                onConfigChange(config.copy(performanceConfig = config.performanceConfig.copy(injectPreloadHints = it)))
                            }
                        )
                        WtaSectionDivider()
                        WtaToggleRow(
                            title = Strings.perfLazyLoading,
                            subtitle = Strings.perfLazyLoadingHint,
                            checked = config.performanceConfig.injectLazyLoading,
                            onCheckedChange = {
                                onConfigChange(config.copy(performanceConfig = config.performanceConfig.copy(injectLazyLoading = it)))
                            }
                        )
                        WtaSectionDivider()
                        WtaToggleRow(
                            title = Strings.perfOptimizeScripts,
                            subtitle = Strings.perfOptimizeScriptsHint,
                            checked = config.performanceConfig.optimizeScripts,
                            onCheckedChange = {
                                onConfigChange(config.copy(performanceConfig = config.performanceConfig.copy(optimizeScripts = it)))
                            }
                        )
                    }
                }

                WtaSection(
                    title = Strings.perfRuntimeOptimize,
                    headerStyle = WtaSectionHeaderStyle.Quiet
                ) {
                    WtaSettingCard {
                        WtaToggleRow(
                            title = Strings.perfRuntimeScript,
                            subtitle = Strings.perfRuntimeScriptHint,
                            checked = config.performanceConfig.injectPerformanceScript,
                            onCheckedChange = {
                                onConfigChange(config.copy(performanceConfig = config.performanceConfig.copy(injectPerformanceScript = it)))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkTrustConfigPanel(
    config: NetworkTrustConfig,
    importError: String?,
    onConfigChange: (NetworkTrustConfig) -> Unit,
    onImportCertificate: () -> Unit
) {
    val context = LocalContext.current
    var presets by remember { mutableStateOf(ConfigPresetStorage.loadNetworkTrust(context)) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }

    WtaSection(
        title = Strings.networkTrustTitle,
        description = Strings.networkTrustHint,
        headerStyle = WtaSectionHeaderStyle.Quiet
    ) {
        WtaSettingCard {
            WtaToggleRow(
                title = Strings.trustSystemCa,
                subtitle = Strings.trustSystemCaHint,
                icon = Icons.Outlined.Security,
                checked = config.trustSystemCa,
                onCheckedChange = { onConfigChange(config.copy(trustSystemCa = it)) }
            )
            WtaSectionDivider()
            WtaToggleRow(
                title = Strings.trustUserCa,
                subtitle = Strings.trustUserCaHint,
                icon = Icons.Outlined.AdminPanelSettings,
                checked = config.trustUserCa,
                onCheckedChange = { onConfigChange(config.copy(trustUserCa = it)) }
            )
            WtaSectionDivider()
            WtaToggleRow(
                title = Strings.cleartextTrafficAllowed,
                subtitle = Strings.cleartextTrafficAllowedHint,
                icon = Icons.Outlined.Http,
                checked = config.cleartextTrafficPermitted,
                onCheckedChange = { onConfigChange(config.copy(cleartextTrafficPermitted = it)) }
            )
            WtaSectionDivider()
            WtaSettingRow(
                title = Strings.importCustomCa,
                subtitle = if (config.customCaCertificates.isEmpty()) {
                    Strings.importCustomCaHint
                } else {
                    Strings.importedCertificatesCount.replace("%d", config.customCaCertificates.size.toString())
                },
                icon = Icons.Outlined.UploadFile,
                onClick = onImportCertificate
            ) {
                Text(
                    text = Strings.btnImport,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            WtaSectionDivider()
            WtaSettingRow(
                title = Strings.saveNetworkPreset,
                subtitle = Strings.saveNetworkPresetHint,
                icon = Icons.Outlined.BookmarkAdd,
                onClick = {
                    presetName = ""
                    showSavePresetDialog = true
                }
            ) {
                Text(
                    text = Strings.save,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (!importError.isNullOrBlank()) {
            WtaStatusBanner(
                message = importError,
                tone = WtaStatusTone.Error
            )
        }

        config.customCaCertificates.forEach { cert ->
            WtaSettingCard {
                WtaSettingRow(
                    title = cert.displayName,
                    subtitle = "${Strings.sha256Prefix} ${cert.sha256.chunked(2).take(8).joinToString(":").uppercase()}...",
                    icon = Icons.Outlined.Badge
                ) {
                    TextButton(
                        onClick = {
                            onConfigChange(
                                config.copy(
                                    customCaCertificates = config.customCaCertificates.filterNot { it.id == cert.id }
                                )
                            )
                        }
                    ) {
                        Text(Strings.btnDelete)
                    }
                }
            }
        }

        if (presets.isNotEmpty()) {
            WtaSettingCard {
                presets.forEachIndexed { index, preset ->
                    WtaSettingRow(
                        title = preset.name,
                        subtitle = Strings.applySavedNetworkPreset,
                        icon = Icons.Outlined.Inventory2,
                        onClick = { onConfigChange(preset.config) }
                    ) {
                        TextButton(
                            onClick = {
                                presets = ConfigPresetStorage.deleteNetworkTrust(context, preset.id)
                            }
                        ) {
                            Text(Strings.btnDelete)
                        }
                    }
                    if (index != presets.lastIndex) {
                        WtaSectionDivider()
                    }
                }
            }
        }

        if (config.customCaCertificates.isNotEmpty()) {
            WtaStatusBanner(
                message = Strings.networkTrustTemplateLimitHint,
                tone = WtaStatusTone.Info
            )
        }
    }

    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text(Strings.saveNetworkPresetTitle) },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text(Strings.presetName) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        presets = ConfigPresetStorage.saveNetworkTrust(context, presetName, config)
                        showSavePresetDialog = false
                    },
                    enabled = presetName.isNotBlank()
                ) {
                    Text(Strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }
}

@Composable
fun CustomSigningSection() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val signer = remember { com.webtoapp.core.apkbuilder.JarSigner(context) }

    var signerType by remember { mutableStateOf(signer.getSignerType()) }
    var certInfo by remember { mutableStateOf(signer.getCertificateInfo()) }

    var showImportPasswordDialog by remember { mutableStateOf(false) }
    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var showRemoveConfirmDialog by remember { mutableStateOf(false) }
    var showCreateKeystoreDialog by remember { mutableStateOf(false) }
    var showFingerprintDialog by remember { mutableStateOf(false) }
    var pendingKeystoreUri by remember { mutableStateOf<Uri?>(null) }
    var passwordInput by remember { mutableStateOf("") }

    var keyPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf<String?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val keystorePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingKeystoreUri = it
            passwordInput = ""
            importError = null
            showImportPasswordDialog = true
        }
    }

    val keystoreExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/x-pkcs12")
    ) { uri: Uri? ->
        uri?.let {
            pendingKeystoreUri = it
            passwordInput = ""
            showExportPasswordDialog = true
        }
    }

    WtaSection(
        title = Strings.currentSigningStatus,
        headerStyle = WtaSectionHeaderStyle.Quiet
    ) {

        WtaSettingCard {
            WtaSettingRow(
                title = when (signerType) {
                    com.webtoapp.core.apkbuilder.JarSigner.SignerType.PKCS12_CUSTOM -> Strings.signingTypeCustom
                    com.webtoapp.core.apkbuilder.JarSigner.SignerType.PKCS12_AUTO -> Strings.signingTypeAutoGenerated
                    com.webtoapp.core.apkbuilder.JarSigner.SignerType.ANDROID_KEYSTORE -> Strings.signingTypeAndroidKeyStore
                },
                subtitle = certInfo,
                icon = if (signerType == com.webtoapp.core.apkbuilder.JarSigner.SignerType.PKCS12_CUSTOM)
                    Icons.Outlined.VerifiedUser else Icons.Outlined.Shield,
                subtitleMaxLines = 5
            )
        }

        WtaStatusBanner(
            title = Strings.customSigningNote,
            message = Strings.supportedKeystoreFormats,
            tone = WtaStatusTone.Info
        )

        WtaSettingCard {
            WtaSettingRow(
                title = Strings.createKeystore,
                subtitle = Strings.createKeystoreNote,
                icon = Icons.Outlined.AddModerator,
                onClick = { showCreateKeystoreDialog = true }
            )
            WtaSectionDivider()
            WtaSettingRow(
                title = Strings.importKeystore,
                subtitle = null,
                icon = Icons.Outlined.FileUpload,
                onClick = { keystorePickerLauncher.launch(arrayOf("*/*")) }
            )
            WtaSectionDivider()
            WtaSettingRow(
                title = Strings.exportKeystore,
                subtitle = null,
                icon = Icons.Outlined.FileDownload,
                onClick = { keystoreExportLauncher.launch("webtoapp_signing.p12") }
            )
            if (certInfo != null) {
                WtaSectionDivider()
                WtaSettingRow(
                    title = Strings.viewFingerprints,
                    subtitle = null,
                    icon = Icons.Outlined.Fingerprint,
                    onClick = { showFingerprintDialog = true }
                )
            }
            if (signerType == com.webtoapp.core.apkbuilder.JarSigner.SignerType.PKCS12_CUSTOM) {
                WtaSectionDivider()
                WtaSettingRow(
                    title = Strings.removeCustomKeystore,
                    subtitle = null,
                    icon = Icons.Outlined.Delete,
                    onClick = { showRemoveConfirmDialog = true },
                    tone = WtaRowTone.Danger
                )
            }
        }

        snackbarMessage?.let { msg ->
            WtaStatusBanner(
                message = msg,
                tone = WtaStatusTone.Success
            )
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                snackbarMessage = null
            }
        }
    }

    if (showCreateKeystoreDialog) {
        CreateKeystoreDialog(
            onDismiss = { showCreateKeystoreDialog = false },
            onCreate = { spec ->
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val success = signer.createCustomKeystore(spec)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (success) {
                            signerType = signer.getSignerType()
                            certInfo = signer.getCertificateInfo()
                            showCreateKeystoreDialog = false
                            snackbarMessage = Strings.certCreateSuccess
                        } else {
                            snackbarMessage = Strings.certCreateFailed
                        }
                    }
                }
            }
        )
    }

    if (showFingerprintDialog) {
        FingerprintDialog(
            fingerprints = remember { signer.getCertificateFingerprints() },
            onDismiss = { showFingerprintDialog = false },
            onCopied = { snackbarMessage = Strings.fingerprintCopied }
        )
    }

    if (showImportPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportPasswordDialog = false
                pendingKeystoreUri = null
                passwordInput = ""
                keyPasswordInput = ""
                importError = null
            },
            icon = { Icon(Icons.Outlined.Key, null) },
            title = { Text(Strings.importKeystore) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = Strings.keystorePasswordHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            importError = null
                        },
                        label = { Text(Strings.keystorePassword) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible)
                            androidx.compose.ui.text.input.VisualTransformation.None
                        else
                            androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = importError != null,
                        supportingText = importError?.let { error ->
                            { Text(error, color = MaterialTheme.colorScheme.error) }
                        }
                    )

                    OutlinedTextField(
                        value = keyPasswordInput,
                        onValueChange = {
                            keyPasswordInput = it
                            importError = null
                        },
                        label = { Text(Strings.keyPasswordOptional) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible)
                            androidx.compose.ui.text.input.VisualTransformation.None
                        else
                            androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(
                                text = Strings.keyPasswordHint,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = pendingKeystoreUri ?: return@TextButton
                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                val tempFile = java.io.File(context.cacheDir, "import_keystore_temp")
                                context.contentResolver.openInputStream(uri)?.use { input ->
                                    tempFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }

                                val keyPassParam = keyPasswordInput.takeIf { it.isNotEmpty() }
                                val success = signer.importKeystore(tempFile, passwordInput, keyPassParam)
                                tempFile.delete()

                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    if (success) {
                                        signerType = signer.getSignerType()
                                        certInfo = signer.getCertificateInfo()
                                        showImportPasswordDialog = false
                                        pendingKeystoreUri = null
                                        passwordInput = ""
                                        keyPasswordInput = ""
                                        importError = null
                                        snackbarMessage = Strings.keystoreImportSuccess
                                    } else {
                                        importError = Strings.keystoreImportFailed
                                    }
                                }
                            } catch (e: Exception) {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    importError = Strings.keystoreImportFailed
                                }
                            }
                        }
                    },
                    enabled = passwordInput.isNotEmpty()
                ) {
                    Text(Strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportPasswordDialog = false
                    pendingKeystoreUri = null
                    passwordInput = ""
                    keyPasswordInput = ""
                    importError = null
                }) {
                    Text(Strings.cancel)
                }
            }
        )
    }

    if (showExportPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportPasswordDialog = false
                pendingKeystoreUri = null
                passwordInput = ""
            },
            icon = { Icon(Icons.Outlined.FileDownload, null) },
            title = { Text(Strings.exportKeystore) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = Strings.exportPasswordHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text(Strings.exportPassword) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible)
                            androidx.compose.ui.text.input.VisualTransformation.None
                        else
                            androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = pendingKeystoreUri ?: return@TextButton
                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                val tempFile = java.io.File(context.cacheDir, "export_keystore_temp.p12")
                                val success = signer.exportPkcs12(tempFile, passwordInput)

                                if (success) {
                                    context.contentResolver.openOutputStream(uri)?.use { output ->
                                        tempFile.inputStream().use { input ->
                                            input.copyTo(output)
                                        }
                                    }
                                    tempFile.delete()
                                }

                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    showExportPasswordDialog = false
                                    pendingKeystoreUri = null
                                    passwordInput = ""
                                    snackbarMessage = if (success) Strings.keystoreExportSuccess else Strings.keystoreExportFailed
                                }
                            } catch (e: Exception) {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    showExportPasswordDialog = false
                                    snackbarMessage = Strings.keystoreExportFailed
                                }
                            }
                        }
                    },
                    enabled = passwordInput.isNotEmpty()
                ) {
                    Text(Strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportPasswordDialog = false
                    pendingKeystoreUri = null
                    passwordInput = ""
                }) {
                    Text(Strings.cancel)
                }
            }
        )
    }

    if (showRemoveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmDialog = false },
            icon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(Strings.removeCustomKeystore) },
            text = { Text(Strings.keystoreRemoveConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val success = signer.removeCustomPkcs12()
                        if (success) {
                            signerType = signer.getSignerType()
                            certInfo = signer.getCertificateInfo()
                            snackbarMessage = Strings.keystoreRemoveSuccess
                        }
                        showRemoveConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmDialog = false }) {
                    Text(Strings.cancel)
                }
            }
        )
    }
}

@Composable
fun SigningSchemeSection() {
    val context = LocalContext.current
    val signer = remember { com.webtoapp.core.apkbuilder.JarSigner(context) }

    var options by remember { mutableStateOf(signer.getSigningSchemeOptions()) }

    var v1NameInput by remember { mutableStateOf(options.v1SignerName) }
    var schemeError by remember { mutableStateOf<String?>(null) }
    var savedHint by remember { mutableStateOf<String?>(null) }

    val effectiveName = remember(v1NameInput, options) {
        signer.resolveV1SignerName(v1NameInput)
    }

    fun persist(next: com.webtoapp.core.apkbuilder.JarSigner.SigningSchemeOptions) {
        options = signer.setSigningSchemeOptions(next)
        savedHint = Strings.signingSchemeSaved
    }

    fun toggleScheme(
        v1: Boolean = options.v1Enabled,
        v2: Boolean = options.v2Enabled,
        v3: Boolean = options.v3Enabled
    ) {
        if (!(v1 || v2 || v3)) {
            schemeError = Strings.signingSchemeAtLeastOne
            return
        }
        schemeError = null
        persist(options.copy(v1Enabled = v1, v2Enabled = v2, v3Enabled = v3))
    }

    WtaSection(
        title = Strings.signingSchemeTitle,
        headerStyle = WtaSectionHeaderStyle.Quiet
    ) {
        WtaStatusBanner(
            message = Strings.signingSchemeNote,
            tone = WtaStatusTone.Info
        )

        WtaSettingCard {
            WtaToggleRow(
                title = Strings.signingSchemeV1Title,
                subtitle = Strings.signingSchemeV1Desc,
                icon = Icons.Outlined.Layers,
                checked = options.v1Enabled,
                onCheckedChange = { toggleScheme(v1 = it) }
            )
            WtaSectionDivider()
            WtaToggleRow(
                title = Strings.signingSchemeV2Title,
                subtitle = Strings.signingSchemeV2Desc,
                icon = Icons.Outlined.Layers,
                checked = options.v2Enabled,
                onCheckedChange = { toggleScheme(v2 = it) }
            )
            WtaSectionDivider()
            WtaToggleRow(
                title = Strings.signingSchemeV3Title,
                subtitle = Strings.signingSchemeV3Desc,
                icon = Icons.Outlined.Layers,
                checked = options.v3Enabled,
                onCheckedChange = { toggleScheme(v3 = it) }
            )
        }

        schemeError?.let { err ->
            WtaStatusBanner(
                message = err,
                tone = WtaStatusTone.Warning
            )
        }

        WtaSettingCard {
            WtaToggleRow(
                title = Strings.signingSchemeAutoFallbackTitle,
                subtitle = Strings.signingSchemeAutoFallbackDesc,
                icon = Icons.Outlined.Restore,
                checked = options.autoFallback,
                onCheckedChange = { persist(options.copy(autoFallback = it)) }
            )
        }

        AnimatedVisibility(visible = options.v1Enabled) {
            WtaSettingCard {
                Column(
                    modifier = Modifier.padding(
                        horizontal = WtaSpacing.RowHorizontal,
                        vertical = WtaSpacing.ContentGap
                    ),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                ) {
                    Text(
                        text = Strings.v1SignerNameTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = v1NameInput,
                        onValueChange = { input ->

                            v1NameInput = input.take(32)
                        },
                        label = { Text(Strings.v1SignerNameLabel) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Badge, null) },
                        trailingIcon = {
                            if (v1NameInput.isNotEmpty()) {
                                IconButton(onClick = {
                                    v1NameInput = ""
                                    persist(options.copy(v1SignerName = ""))
                                }) {
                                    Icon(Icons.Outlined.Clear, null)
                                }
                            }
                        },
                        supportingText = {
                            Text(
                                text = Strings.v1SignerNameHint,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusEvent { focusState ->

                                if (!focusState.isFocused && v1NameInput != options.v1SignerName) {
                                    persist(options.copy(v1SignerName = v1NameInput))
                                }
                            }
                    )

                    Text(
                        text = "${Strings.v1SignerNameAutoPreview}: META-INF/$effectiveName.SF · $effectiveName.RSA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        savedHint?.let { msg ->
            WtaStatusBanner(
                message = msg,
                tone = WtaStatusTone.Success
            )
            LaunchedEffect(msg, options, v1NameInput) {
                kotlinx.coroutines.delay(2000)
                savedHint = null
            }
        }
    }
}

@Composable
private fun CreateKeystoreDialog(
    onDismiss: () -> Unit,
    onCreate: (com.webtoapp.core.apkbuilder.JarSigner.CertificateSpec) -> Unit
) {
    var alias by remember { mutableStateOf("key0") }
    var password by remember { mutableStateOf("") }
    var commonName by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var organizationUnit by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var validityYears by remember { mutableStateOf("30") }
    var keySize by remember { mutableStateOf(2048) }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.AddModerator, null) },
        title = { Text(Strings.createKeystore) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = Strings.createKeystoreNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it; error = null },
                    label = { Text(Strings.certAlias) },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text(Strings.keystorePassword) },
                    singleLine = true,
                    isError = error != null,
                    visualTransformation = if (passwordVisible)
                        androidx.compose.ui.text.input.VisualTransformation.None
                    else
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = commonName,
                    onValueChange = { commonName = it },
                    label = { Text(Strings.certCommonName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = organization,
                    onValueChange = { organization = it },
                    label = { Text(Strings.certOrganization) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = organizationUnit,
                    onValueChange = { organizationUnit = it },
                    label = { Text(Strings.certOrganizationUnit) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = locality,
                        onValueChange = { locality = it },
                        label = { Text(Strings.certLocality) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text(Strings.certState) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it.take(2).uppercase() },
                        label = { Text(Strings.certCountry) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = validityYears,
                        onValueChange = { input -> validityYears = input.filter { it.isDigit() }.take(3) },
                        label = { Text(Strings.certValidityYears) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = Strings.certKeySize,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2048, 4096).forEach { size ->
                        PremiumFilterChip(
                            selected = keySize == size,
                            onClick = { keySize = size },
                            label = { Text("$size") }
                        )
                    }
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (alias.isBlank() || password.isBlank()) {
                        error = Strings.certAliasPasswordRequired
                        return@TextButton
                    }
                    onCreate(
                        com.webtoapp.core.apkbuilder.JarSigner.CertificateSpec(
                            alias = alias.trim(),
                            password = password,
                            commonName = commonName,
                            organization = organization,
                            organizationUnit = organizationUnit,
                            locality = locality,
                            state = state,
                            country = country,
                            validityYears = validityYears.toIntOrNull() ?: 30,
                            keySize = keySize
                        )
                    )
                }
            ) {
                Text(Strings.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Strings.cancel) }
        }
    )
}

@Composable
private fun FingerprintDialog(
    fingerprints: com.webtoapp.core.apkbuilder.JarSigner.CertificateFingerprints?,
    onDismiss: () -> Unit,
    onCopied: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Fingerprint, null) },
        title = { Text(Strings.viewFingerprints) },
        text = {
            if (fingerprints == null) {
                Text(Strings.signingTypeAutoGenerated)
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FingerprintRow("MD5", fingerprints.md5) { copyToClipboard(context, "MD5", it); onCopied() }
                    FingerprintRow("SHA-1", fingerprints.sha1) { copyToClipboard(context, "SHA-1", it); onCopied() }
                    FingerprintRow("SHA-256", fingerprints.sha256) { copyToClipboard(context, "SHA-256", it); onCopied() }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(Strings.confirm) }
        }
    )
}

@Composable
private fun FingerprintRow(label: String, value: String, onCopy: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopy(value) }
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                Icons.Outlined.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun copyToClipboard(context: android.content.Context, label: String, text: String) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
        as android.content.ClipboardManager
    clipboard.setPrimaryClip(android.content.ClipData.newPlainText(label, text))
}
