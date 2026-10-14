package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.ApkRuntimePermissions
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.ExportPerm
import com.webtoapp.data.model.ExportPermGroup
import com.webtoapp.data.model.ExportPermissionCatalog
import com.webtoapp.data.model.HtmlLoadMode
import com.webtoapp.data.model.PermissionFeatureDefaults
import com.webtoapp.data.model.PermissionFeatureReason
import com.webtoapp.data.model.exportGranted
import com.webtoapp.data.model.withExportGrant
import com.webtoapp.ui.components.IconSwitchCard
import com.webtoapp.ui.components.PremiumButton
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.design.WtaBackground
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaStatusBanner
import com.webtoapp.ui.design.WtaStatusTone
import com.webtoapp.ui.theme.LocalShowDescriptions
import com.webtoapp.ui.theme.ifDescriptionsShown
import com.webtoapp.util.PermissionPresetStorage
import com.webtoapp.util.SavedPermissionPreset

/**
 * Checkbox approximation of what export will treat as already required.
 * Export itself uses [com.webtoapp.core.apkbuilder] network detection, so a
 * file HTML page that still loads the network can differ until build time.
 * An explicit off on the local-network row still wins at export.
 */
fun permissionFeatureDefaults(
    appType: AppType,
    htmlLoadMode: HtmlLoadMode?,
    enableMediaSession: Boolean,
    bgmEnabled: Boolean,
    scheduledStartEnabled: Boolean,
    activationEnabled: Boolean
): PermissionFeatureDefaults {
    val fileOnly = (appType == AppType.HTML || appType == AppType.FRONTEND) &&
        htmlLoadMode == HtmlLoadMode.FILE
    return PermissionFeatureDefaults(
        network = !fileOnly,
        mediaSessionOrBgm = bgmEnabled || enableMediaSession,
        scheduledExactAlarm = scheduledStartEnabled,
        activationBiometric = activationEnabled
    )
}

private data class PermissionPreset(
    val label: () -> String,
    val icon: ImageVector,
    val apply: (ApkRuntimePermissions) -> ApkRuntimePermissions,
    val match: (ApkRuntimePermissions) -> Boolean
)

private val PERMISSION_PRESETS = listOf(
    PermissionPreset(
        label = { Strings.permissionPresetNone },
        icon = Icons.Outlined.Block,
        apply = { ApkRuntimePermissions() },
        match = { it == ApkRuntimePermissions() }
    ),
    PermissionPreset(
        label = { Strings.permissionPresetMinimal },
        icon = Icons.Outlined.Language,
        apply = { ApkRuntimePermissions(notifications = true) },
        match = { it == ApkRuntimePermissions(notifications = true) }
    ),
    PermissionPreset(
        label = { Strings.permissionPresetStandard },
        icon = Icons.Outlined.Verified,
        apply = {
            ApkRuntimePermissions(
                camera = true, location = true, notifications = true,
                readMediaImages = true, readMediaVideo = true
            )
        },
        match = {
            it == ApkRuntimePermissions(
                camera = true, location = true, notifications = true,
                readMediaImages = true, readMediaVideo = true
            )
        }
    ),
    PermissionPreset(
        label = { Strings.permissionPresetFull },
        icon = Icons.Outlined.SelectAll,
        apply = {
            ApkRuntimePermissions(
                camera = true, microphone = true, location = true, notifications = true,
                readMediaImages = true, readMediaVideo = true, readMediaAudio = true,
                bluetooth = true, nfc = true, bodySensors = true
            )
        },
        match = {
            it == ApkRuntimePermissions(
                camera = true, microphone = true, location = true, notifications = true,
                readMediaImages = true, readMediaVideo = true, readMediaAudio = true,
                bluetooth = true, nfc = true, bodySensors = true
            )
        }
    )
)

private fun groupIcon(group: ExportPermGroup): ImageVector = when (group) {
    ExportPermGroup.CALENDAR -> Icons.Outlined.CalendarMonth
    ExportPermGroup.CAMERA -> Icons.Outlined.PhotoCamera
    ExportPermGroup.CONTACTS -> Icons.Outlined.Contacts
    ExportPermGroup.LOCATION -> Icons.Outlined.LocationOn
    ExportPermGroup.MICROPHONE -> Icons.Outlined.Mic
    ExportPermGroup.PHONE -> Icons.Outlined.Call
    ExportPermGroup.CALL_LOG -> Icons.Outlined.History
    ExportPermGroup.ACTIVITY -> Icons.AutoMirrored.Outlined.DirectionsWalk
    ExportPermGroup.SENSORS -> Icons.AutoMirrored.Outlined.DirectionsRun
    ExportPermGroup.SMS -> Icons.Outlined.Sms
    ExportPermGroup.STORAGE -> Icons.Outlined.Folder
    ExportPermGroup.NEARBY -> Icons.Outlined.Bluetooth
    ExportPermGroup.NOTIFICATIONS -> Icons.Outlined.Notifications
    ExportPermGroup.SPECIAL -> Icons.Outlined.PrivacyTip
    ExportPermGroup.FOREGROUND -> Icons.Outlined.PlayCircleOutline
    ExportPermGroup.SYSTEM -> Icons.Outlined.Settings
}

private fun rowMatches(perm: ExportPerm, query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.trim()
    return perm.key.contains(q, ignoreCase = true) ||
        perm.manifest.contains(q, ignoreCase = true) ||
        exportPermissionTitle(perm.key).contains(q, ignoreCase = true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionConfigScreen(
    permissions: ApkRuntimePermissions,
    onPermissionsChange: (ApkRuntimePermissions) -> Unit,
    onBack: () -> Unit,
    featureDefaults: PermissionFeatureDefaults = PermissionFeatureDefaults()
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        Strings.permissionConfigTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        WtaBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PermissionConfigPanel(
                    permissions = permissions,
                    onPermissionsTransform = { transform -> onPermissionsChange(transform(permissions)) },
                    featureDefaults = featureDefaults
                )
            }
        }
    }
}

@Composable
fun PermissionConfigPanel(
    permissions: ApkRuntimePermissions,
    onPermissionsTransform: ((ApkRuntimePermissions) -> ApkRuntimePermissions) -> Unit,
    modifier: Modifier = Modifier,
    showDescription: Boolean = true,
    featureReasons: Map<String, List<PermissionFeatureReason>> = emptyMap(),
    featureDefaults: PermissionFeatureDefaults = PermissionFeatureDefaults()
) {
    val context = LocalContext.current
    var schemeName by remember { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    var savedPresets by remember { mutableStateOf(PermissionPresetStorage.load(context)) }
    val enabledCount = countEnabledPermissions(permissions, featureDefaults)
    val conflicts = detectConflicts(permissions, featureDefaults)
    val autoHintCount = ExportPermissionCatalog.entries.count { perm ->
        perm.reasonKeys.any { key -> featureReasons[key].orEmpty().isNotEmpty() } &&
            permissions.exportGranted(perm, featureDefaults)
    }
    val visibleGroups = ExportPermGroup.entries.mapNotNull { group ->
        val items = ExportPermissionCatalog.entries.filter { it.group == group && rowMatches(it, query) }
        if (items.isEmpty()) null else group to items
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showDescription && LocalShowDescriptions.current) {
            WtaStatusBanner(
                message = Strings.permissionConfigDesc,
                tone = WtaStatusTone.Info
            )
        }

        if (autoHintCount > 0 && LocalShowDescriptions.current) {
            WtaStatusBanner(
                message = Strings.permissionAutoEnabledSummary.format(autoHintCount),
                tone = WtaStatusTone.Info
            )
        }

        PremiumTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(Strings.search) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PERMISSION_PRESETS.forEach { preset ->
                val selected = preset.match(permissions)
                WtaChip(
                    selected = selected,
                    onClick = { onPermissionsTransform { preset.apply(it) } },
                    label = preset.label(),
                    leadingIcon = preset.icon,
                    showSelectedCheck = false
                )
            }
        }

        PermissionSchemeCard(
            savedPresets = savedPresets,
            schemeName = schemeName,
            onSchemeNameChange = { schemeName = it },
            onSaveScheme = {
                savedPresets = PermissionPresetStorage.save(context, schemeName, permissions)
                schemeName = ""
            },
            onApplyScheme = { scheme -> onPermissionsTransform { scheme.permissions } },
            onDeleteScheme = {
                savedPresets = PermissionPresetStorage.delete(context, it.id)
            }
        )

        if (conflicts.isNotEmpty()) {
            WtaStatusBanner(
                title = Strings.permissionConflictTitle,
                message = conflicts.joinToString("\n") { "• $it" },
                tone = WtaStatusTone.Error,
                messageMaxLines = 20
            )
        }

        Surface(
            color = if (enabledCount > 0)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = if (enabledCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.permissionEnabledCount.format(enabledCount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (enabledCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                if (permissions != ApkRuntimePermissions()) {
                    TextButton(onClick = { onPermissionsTransform { ApkRuntimePermissions() } }) {
                        Text(Strings.permissionClearAll, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (visibleGroups.isEmpty()) {
            Text(
                text = exportPermSearchEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            visibleGroups.forEach { (group, items) ->
                PermissionGroupCard(
                    group = group,
                    items = items,
                    permissions = permissions,
                    featureDefaults = featureDefaults,
                    forceExpand = query.isNotBlank(),
                    onPermissionsTransform = onPermissionsTransform,
                    featureReasons = featureReasons
                )
            }
        }
    }
}

@Composable
fun PermissionSummaryCard(
    permissions: ApkRuntimePermissions,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    featureDefaults: PermissionFeatureDefaults = PermissionFeatureDefaults()
) {
    val enabledCount = countEnabledPermissions(permissions, featureDefaults)
    val restrictedCount = countPlayRestricted(permissions, featureDefaults)
    val summary = if (enabledCount == 0) {
        null
    } else {
        buildList {
            add(Strings.permissionEnabledCount.format(enabledCount))
            if (restrictedCount > 0) {
                add("${Strings.sensitive} $restrictedCount")
            }
        }.joinToString(" · ")
    }

    WtaSettingCard(modifier = modifier) {
        WtaSettingRow(
            title = Strings.permissionConfigButton,
            subtitle = summary,
            icon = Icons.Outlined.AdminPanelSettings,
            onClick = onClick
        ) {
            if (enabledCount > 0) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = enabledCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = Strings.details,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (enabledCount > 0) {
            WtaSectionDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PermissionSummaryPill(
                    label = Strings.configured,
                    value = enabledCount.toString(),
                    tint = MaterialTheme.colorScheme.primary
                )
                if (restrictedCount > 0) {
                    PermissionSummaryPill(
                        label = Strings.sensitive,
                        value = restrictedCount.toString(),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionSchemeCard(
    savedPresets: List<SavedPermissionPreset>,
    schemeName: String,
    onSchemeNameChange: (String) -> Unit,
    onSaveScheme: () -> Unit,
    onApplyScheme: (SavedPermissionPreset) -> Unit,
    onDeleteScheme: (SavedPermissionPreset) -> Unit
) {
    WtaSettingCard {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Bookmarks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = Strings.saveAsSchemeTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PremiumTextField(
                    value = schemeName,
                    onValueChange = onSchemeNameChange,
                    label = { Text(Strings.schemeName) },
                    placeholder = { Text(Strings.inputSchemeName) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                PremiumButton(
                    onClick = onSaveScheme,
                    enabled = schemeName.isNotBlank(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(Strings.save)
                }
            }

            if (savedPresets.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    savedPresets.forEach { preset ->
                        WtaChip(
                            selected = false,
                            onClick = { onApplyScheme(preset) },
                            label = preset.name,
                            leadingIcon = Icons.Outlined.Bolt,
                            showSelectedCheck = false,
                            trailingContent = {
                                IconButton(
                                    onClick = { onDeleteScheme(preset) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = Strings.btnDelete,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionGroupCard(
    group: ExportPermGroup,
    items: List<ExportPerm>,
    permissions: ApkRuntimePermissions,
    featureDefaults: PermissionFeatureDefaults,
    forceExpand: Boolean,
    onPermissionsTransform: ((ApkRuntimePermissions) -> ApkRuntimePermissions) -> Unit,
    featureReasons: Map<String, List<PermissionFeatureReason>>
) {
    val enabledInGroup = items.count { permissions.exportGranted(it, featureDefaults) }
    val hasRestricted = items.any { it.playRestricted && permissions.exportGranted(it, featureDefaults) }
    var expanded by rememberSaveable(group) { mutableStateOf(enabledInGroup > 0) }
    val shown = forceExpand || expanded

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = groupIcon(group),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (enabledInGroup > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exportPermGroupTitle(group),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (enabledInGroup > 0) {
                        Text(
                            text = Strings.nEnabledOfTotal.format(enabledInGroup, items.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasRestricted) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (enabledInGroup > 0) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (hasRestricted) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = enabledInGroup.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasRestricted) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = if (shown) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = shown,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { perm ->
                        val reasons = perm.reasonKeys.flatMap { featureReasons[it].orEmpty() }.distinct()
                        PermissionSwitch(
                            perm = perm,
                            checked = permissions.exportGranted(perm, featureDefaults),
                            featureReasonLabels = reasons.map { it.displayLabel() },
                            onCheckedChange = { checked ->
                                onPermissionsTransform { current ->
                                    current.withExportGrant(perm.key, checked, featureDefaults)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionSwitch(
    perm: ExportPerm,
    checked: Boolean,
    featureReasonLabels: List<String>,
    onCheckedChange: (Boolean) -> Unit
) {
    val autoHint = if (featureReasonLabels.isNotEmpty()) {
        Strings.permissionAutoEnabledBy.format(featureReasonLabels.joinToString(" · ")).ifDescriptionsShown()
    } else {
        null
    }
    val subtitle = buildString {
        append(exportPermissionIdentity(perm))
        if (autoHint != null) {
            append('\n')
            append(autoHint)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        IconSwitchCard(
            title = exportPermissionTitle(perm.key),
            subtitle = subtitle,
            icon = groupIcon(perm.group),
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        if (perm.playRestricted && checked) {
            Text(
                text = Strings.permissionPlayRestrictedWarning,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
            )
        }
    }
}

private fun PermissionFeatureReason.displayLabel(): String = when (this) {
    PermissionFeatureReason.BACKGROUND_RUN -> Strings.backgroundRunTitle
    PermissionFeatureReason.NOTIFICATION -> Strings.notificationConfigTitle
    PermissionFeatureReason.NATIVE_BRIDGE_NOTIFICATION -> Strings.nativeBridgeTitle
    PermissionFeatureReason.NOTIFICATION_POLYFILL -> Strings.notificationPolyfillTitle
    PermissionFeatureReason.GEOLOCATION -> Strings.geolocationTitle
    PermissionFeatureReason.FLOATING_WINDOW -> Strings.floatingWindowTitle
    PermissionFeatureReason.BGM -> Strings.bgmTitle
    PermissionFeatureReason.MEDIA_SESSION -> Strings.enableMediaSessionTitle
    PermissionFeatureReason.BOOT_START -> Strings.autoStartSettings
    PermissionFeatureReason.SCREEN_AWAKE -> Strings.screenAwakeModeLabel
    PermissionFeatureReason.CUSTOM_DOWNLOAD -> Strings.downloadLocationCustom
}

private fun countEnabledPermissions(
    permissions: ApkRuntimePermissions,
    defaults: PermissionFeatureDefaults
): Int = ExportPermissionCatalog.entries.count { permissions.exportGranted(it, defaults) }

private fun countPlayRestricted(
    permissions: ApkRuntimePermissions,
    defaults: PermissionFeatureDefaults
): Int = ExportPermissionCatalog.entries.count {
    it.playRestricted && permissions.exportGranted(it, defaults)
}

@Composable
private fun PermissionSummaryPill(
    label: String,
    value: String,
    tint: Color
) {
    Surface(
        color = tint.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = tint
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        }
    }
}

private fun detectConflicts(
    permissions: ApkRuntimePermissions,
    defaults: PermissionFeatureDefaults
): List<String> {
    fun on(key: String): Boolean {
        val perm = ExportPermissionCatalog.byKey[key] ?: return false
        return permissions.exportGranted(perm, defaults)
    }

    val conflicts = mutableListOf<String>()
    val hasMedia = on("READ_MEDIA_IMAGES") || on("READ_MEDIA_VIDEO") || on("READ_MEDIA_AUDIO")
    val hasLegacy = on("READ_EXTERNAL_STORAGE") || on("WRITE_EXTERNAL_STORAGE")
    if (hasMedia && hasLegacy) {
        conflicts += Strings.permissionConflictMediaVsLegacy
    }
    val hasSms = on("READ_SMS") || on("SEND_SMS") || on("RECEIVE_SMS") ||
        on("RECEIVE_WAP_PUSH") || on("RECEIVE_MMS")
    if (hasSms) {
        conflicts += Strings.permissionConflictSmsRisk
    }
    if (on("CALL_PHONE") || on("PROCESS_OUTGOING_CALLS")) {
        conflicts += Strings.permissionConflictCallRisk
    }
    if (on("QUERY_ALL_PACKAGES")) {
        conflicts += Strings.permissionConflictQueryAllPackages
    }
    return conflicts
}
