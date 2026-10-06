package com.webtoapp.ui.screens

import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.shell.MULTI_WEB_DEFAULT_CARD_ASPECT
import com.webtoapp.ui.shell.MULTI_WEB_DEFAULT_CARD_COLUMNS
import com.webtoapp.ui.shell.MULTI_WEB_DEFAULT_DRAWER_COLUMNS
import com.webtoapp.ui.shell.MULTI_WEB_MAX_CARD_COLUMNS
import com.webtoapp.ui.shell.MULTI_WEB_MAX_DRAWER_COLUMNS
import com.webtoapp.ui.shell.MULTI_WEB_START_LAST
import com.webtoapp.ui.shell.MULTI_WEB_START_SITE
import com.webtoapp.ui.shell.moveListItem
import com.webtoapp.ui.shell.resolvedCardAspect
import com.webtoapp.ui.shell.resolvedGridColumns
import com.webtoapp.ui.theme.LocalShowDescriptions
import kotlin.math.roundToInt
import com.webtoapp.data.model.MultiWebConfig
import com.webtoapp.data.model.MultiWebSite
import com.webtoapp.data.model.HtmlFileType
import com.webtoapp.ui.components.EnhancedElevatedCard
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.components.RuntimeIconPickerCard
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.util.ensureWebUrlScheme
import com.webtoapp.ui.screens.create.WtaCreateFlowScaffold
import com.webtoapp.ui.screens.create.WtaCreateFlowSection
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMultiWebAppScreen(
    existingAppId: Long = 0L,
    onBack: () -> Unit,
    onCreated: (
        name: String,
        multiWebConfig: MultiWebConfig,
        iconUri: Uri?,
        injectScripts: List<com.webtoapp.data.model.UserScript>,
        themeType: String
    ) -> Unit
) {
    val isEdit = existingAppId > 0L

    var appName by remember { mutableStateOf("") }
    var appIcon by remember { mutableStateOf<Uri?>(null) }
    var injectScripts by remember { mutableStateOf<List<com.webtoapp.data.model.UserScript>>(emptyList()) }

    var sites by remember { mutableStateOf<List<MultiWebSite>>(emptyList()) }

    var existingApps by remember { mutableStateOf<List<com.webtoapp.data.model.WebApp>>(emptyList()) }
    LaunchedEffect(Unit) {
        val repo = org.koin.java.KoinJavaComponent.get<com.webtoapp.data.repository.WebAppRepository>(
            com.webtoapp.data.repository.WebAppRepository::class.java
        )
        repo.allWebApps.collect { existingApps = it }
    }

    var refreshInterval by remember { mutableStateOf(30) }
    var displayMode by remember { mutableStateOf("TABS") }
    var showSiteIcons by remember { mutableStateOf(true) }
    var cardColumns by remember { mutableIntStateOf(MULTI_WEB_DEFAULT_CARD_COLUMNS) }
    var drawerColumns by remember { mutableIntStateOf(MULTI_WEB_DEFAULT_DRAWER_COLUMNS) }
    var cardAspectRatio by remember { mutableFloatStateOf(MULTI_WEB_DEFAULT_CARD_ASPECT) }
    var sitesInheritConfig by remember { mutableStateOf(true) }
    var startTab by remember { mutableStateOf(MULTI_WEB_START_LAST) }
    var startSiteId by remember { mutableStateOf("") }

    var selectedAppIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var filterType by remember { mutableStateOf<String?>(null) }
    var filterCategoryId by remember { mutableStateOf<Long?>(null) }
    // Non-null while the add/edit custom-site dialog is open; siteId == null means adding.
    var siteDialog by remember { mutableStateOf<SiteDialogData?>(null) }
    var categories by remember { mutableStateOf<List<com.webtoapp.data.model.AppCategory>>(emptyList()) }
    LaunchedEffect(Unit) {
        val catRepo = org.koin.java.KoinJavaComponent.get<com.webtoapp.data.repository.AppCategoryRepository>(
            com.webtoapp.data.repository.AppCategoryRepository::class.java
        )
        catRepo.allCategories.collect { categories = it }
    }

    LaunchedEffect(existingAppId) {
        if (existingAppId > 0L) {
            val existingApp = org.koin.java.KoinJavaComponent
                .get<com.webtoapp.data.repository.WebAppRepository>(
                    com.webtoapp.data.repository.WebAppRepository::class.java
                ).getWebApp(existingAppId)
            existingApp?.let { app ->
                appName = app.name
                app.iconPath?.let { appIcon = android.net.Uri.parse(it) }
                app.multiWebConfig?.let { config ->
                    sites = config.sites
                    refreshInterval = config.refreshInterval
                    displayMode = config.displayMode.ifBlank { "TABS" }
                    showSiteIcons = config.showSiteIcons
                    cardColumns = resolvedGridColumns(
                        config.cardColumns,
                        MULTI_WEB_DEFAULT_CARD_COLUMNS,
                        MULTI_WEB_MAX_CARD_COLUMNS
                    )
                    drawerColumns = resolvedGridColumns(
                        config.drawerColumns,
                        MULTI_WEB_DEFAULT_DRAWER_COLUMNS,
                        MULTI_WEB_MAX_DRAWER_COLUMNS
                    )
                    cardAspectRatio = resolvedCardAspect(config.cardAspectRatio)
                    sitesInheritConfig = !config.sitesUseOwnConfig
                    startTab = if (config.startTab == MULTI_WEB_START_SITE) MULTI_WEB_START_SITE else MULTI_WEB_START_LAST
                    startSiteId = config.startSiteId
                }
                injectScripts = app.webViewConfig.injectScripts
            }
        }
    }

    val iconPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { appIcon = it } }

    val canCreate = sites.isNotEmpty()
    val accentColor = MaterialTheme.colorScheme.onSurface

    WtaCreateFlowScaffold(
        title = if (isEdit) Strings.editApp else Strings.createMultiWebApp,
        onBack = onBack,
        actions = {
            TextButton(
                onClick = {
                    onCreated(
                        appName.ifBlank { "Multi-Site App" },
                        MultiWebConfig(
                            sites = sites,
                            displayMode = displayMode,
                            refreshInterval = refreshInterval,
                            showSiteIcons = showSiteIcons,
                            cardColumns = cardColumns,
                            drawerColumns = drawerColumns,
                            cardAspectRatio = cardAspectRatio,
                            sitesUseOwnConfig = !sitesInheritConfig,
                            projectId = "",
                            startTab = startTab,
                            startSiteId = startSiteId
                        ),
                        appIcon,
                        injectScripts,
                        "AURORA"
                    )
                },
                enabled = canCreate
            ) {
                Text(
                    if (isEdit) Strings.btnSave else Strings.btnCreate,
                    fontWeight = FontWeight.SemiBold,
                    color = if (canCreate) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WtaCreateFlowSection(title = Strings.labelBasicInfo) {
                    EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            RuntimeIconPickerCard(
                                appIcon = appIcon,
                                onSelectIcon = { iconPickerLauncher.launch("image/*") }
                            )
                            PremiumTextField(
                                value = appName,
                                onValueChange = { appName = it },
                                label = { Text(Strings.labelAppName) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
            }

            WtaCreateFlowSection(title = Strings.multiWebDisplayMode) {
                EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DisplayModePicker(
                            selected = displayMode,
                            onSelect = { displayMode = it }
                        )
                        if (displayMode != "FEED") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                Strings.multiWebStartTab,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (LocalShowDescriptions.current) {
                                Text(
                                    Strings.multiWebStartTabHint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StartTabChoice(
                                selected = startTab,
                                onSelect = { mode ->
                                    startTab = mode
                                    if (mode == MULTI_WEB_START_SITE && sites.none { it.enabled && it.id == startSiteId }) {
                                        startSiteId = sites.firstOrNull { it.enabled }?.id ?: ""
                                    }
                                }
                            )
                            if (startTab == MULTI_WEB_START_SITE) {
                                sites.filter { it.enabled }.forEach { site ->
                                    StartTabRadioRow(
                                        selected = startSiteId == site.id,
                                        label = site.name.ifBlank { site.url },
                                        onClick = { startSiteId = site.id }
                                    )
                                }
                            }
                        }
                        if (displayMode == "CARDS" || displayMode == "TOP_TABS") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Strings.multiWebShowSiteIcons,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Switch(
                                    checked = showSiteIcons,
                                    onCheckedChange = { showSiteIcons = it }
                                )
                            }
                        }
                        if (displayMode == "CARDS") {
                            MultiWebIntSlider(
                                label = Strings.multiWebCardColumns,
                                hint = Strings.multiWebCardColumnsHint,
                                value = cardColumns,
                                range = 1..MULTI_WEB_MAX_CARD_COLUMNS,
                                onValueChange = { cardColumns = it }
                            )
                            MultiWebCardHeightSlider(
                                value = cardAspectRatio,
                                onValueChange = { cardAspectRatio = it }
                            )
                        }
                        if (displayMode == "DRAWER") {
                            MultiWebIntSlider(
                                label = Strings.multiWebDrawerColumns,
                                hint = Strings.multiWebDrawerColumnsHint,
                                value = drawerColumns,
                                range = 1..MULTI_WEB_MAX_DRAWER_COLUMNS,
                                onValueChange = { drawerColumns = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    Strings.multiWebSitesInheritConfig,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (LocalShowDescriptions.current) {
                                    Text(
                                        Strings.multiWebSitesInheritConfigHint,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = sitesInheritConfig,
                                onCheckedChange = { sitesInheritConfig = it }
                            )
                        }
                    }
                }
            }

            WtaCreateFlowSection(title = Strings.preview) {
                EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        if (sites.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    Strings.multiWebSiteList,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    Strings.multiWebSiteCount.replace("%d", sites.size.toString()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (LocalShowDescriptions.current) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    Strings.multiWebDragToReorder,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val view = LocalView.current
                            val latestSites = rememberUpdatedState(sites)
                            var draggingId by remember { mutableStateOf<String?>(null) }
                            val dragTranslation = remember { mutableFloatStateOf(0f) }
                            val rowHeightPx = remember { mutableFloatStateOf(0f) }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                sites.forEachIndexed { index, site ->
                                    key(site.id) {
                                        val dragging = draggingId == site.id
                                        SiteItem(
                                            site = site,
                                            showFeedConfig = false,
                                            modifier = Modifier
                                                .onSizeChanged { size ->
                                                    if (size.height > 0) rowHeightPx.floatValue = size.height.toFloat()
                                                }
                                                .zIndex(if (dragging) 1f else 0f)
                                                .graphicsLayer {
                                                    translationY = if (dragging) dragTranslation.floatValue else 0f
                                                    shadowElevation = if (dragging) 8.dp.toPx() else 0f
                                                },
                                            dragHandleModifier = Modifier.pointerInput(site.id) {
                                                val rowGapPx = 8.dp.toPx()
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        draggingId = site.id
                                                        dragTranslation.floatValue = 0f
                                                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                    },
                                                    onDragEnd = {
                                                        draggingId = null
                                                        dragTranslation.floatValue = 0f
                                                    },
                                                    onDragCancel = {
                                                        draggingId = null
                                                        dragTranslation.floatValue = 0f
                                                    },
                                                    onDrag = { change, amount ->
                                                        change.consume()
                                                        val height = rowHeightPx.floatValue + rowGapPx
                                                        if (rowHeightPx.floatValue <= 0f) return@detectDragGesturesAfterLongPress
                                                        dragTranslation.floatValue += amount.y
                                                        val steps = (dragTranslation.floatValue / height).toInt()
                                                        if (steps == 0) return@detectDragGesturesAfterLongPress
                                                        val list = latestSites.value
                                                        val from = list.indexOfFirst { it.id == site.id }
                                                        if (from < 0) return@detectDragGesturesAfterLongPress
                                                        val to = (from + steps).coerceIn(0, list.lastIndex)
                                                        if (to == from) return@detectDragGesturesAfterLongPress
                                                        sites = moveListItem(list, from, to).reindexed()
                                                        dragTranslation.floatValue -= (to - from) * height
                                                    }
                                                )
                                            },
                                            onDelete = {
                                                sites = sites.toMutableList().also { it.removeAt(index) }.reindexed()
                                            },
                                            onToggleEnabled = { enabled ->
                                                sites = sites.toMutableList().also {
                                                    it[index] = site.copy(enabled = enabled)
                                                }
                                            },
                                            onMoveUp = if (index > 0) {
                                                { sites = moveListItem(sites, index, index - 1).reindexed() }
                                            } else null,
                                            onMoveDown = if (index < sites.size - 1) {
                                                { sites = moveListItem(sites, index, index + 1).reindexed() }
                                            } else null,
                                            onEdit = if (site.sourceAppId == 0L) {
                                                { siteDialog = SiteDialogData(site.id, site.name, site.url) }
                                            } else null
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        ExistingAppPicker(
                            existingApps = existingApps,
                            categories = categories,
                            selectedAppIds = selectedAppIds,
                            addedAppIds = sites.map { it.sourceAppId }.toSet(),
                            onAddCustomSite = { siteDialog = SiteDialogData(null, "", "") },
                            filterType = filterType,
                            filterCategoryId = filterCategoryId,
                            onFilterTypeChange = { filterType = it },
                            onFilterCategoryChange = { filterCategoryId = it },
                            onToggleApp = { appId ->
                                selectedAppIds = if (appId in selectedAppIds) selectedAppIds - appId else selectedAppIds + appId
                            },
                            onSelectAll = { allSelected ->
                                selectedAppIds = if (allSelected) emptySet() else getFilteredAppIds(existingApps, filterType, filterCategoryId)
                            },
                            onAddSelected = {
                                val alreadyAppIds = sites.map { it.sourceAppId }.toSet()
                                val newSites = existingApps
                                    .filter { it.id in selectedAppIds && it.id !in alreadyAppIds }
                                    .mapIndexed { idx, app ->
                                        var localFilePath = ""
                                        var sourceProjectId = ""
                                        if (app.htmlConfig != null && app.htmlConfig!!.projectId.isNotBlank()) {
                                            val entryFile = app.htmlConfig!!.files.firstOrNull { it.type == HtmlFileType.HTML }
                                                ?: app.htmlConfig!!.files.firstOrNull()
                                            localFilePath = entryFile?.name?.takeIf { it.isNotBlank() } ?: "index.html"
                                            sourceProjectId = app.htmlConfig!!.projectId
                                        }
                                        val siteType = if (localFilePath.isNotBlank()) "EXISTING" else "URL"
                                        MultiWebSite(
                                            id = UUID.randomUUID().toString(),
                                            name = app.name,
                                            url = app.url,
                                            type = siteType,
                                            localFilePath = localFilePath,
                                            sourceAppId = app.id,
                                            sourceProjectId = sourceProjectId,
                                            appType = app.appType.name,
                                            htmlConfig = app.htmlConfig,
                                            webViewConfig = app.webViewConfig,
                                            siteProjectId = sourceProjectId.ifBlank { UUID.randomUUID().toString() },
                                            enabled = true,
                                            sortIndex = sites.size + idx
                                        )
                                    }
                                sites = sites + newSites
                                selectedAppIds = emptySet()
                            }
                        )
                    }
                }
            }

            WtaCreateFlowSection(title = Strings.multiWebCustomCodeSection) {
                EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (LocalShowDescriptions.current) {
                            Text(
                                Strings.multiWebCustomCodeDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        UserScriptsSection(
                            scripts = injectScripts,
                            onScriptsChange = { injectScripts = it }
                        )
                    }
                }
            }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

    siteDialog?.let { dialog ->
        SiteEditDialog(
            data = dialog,
            onDismiss = { siteDialog = null },
            onConfirm = { name, url ->
                val normalizedUrl = ensureWebUrlScheme(url)
                val resolvedName = name.ifBlank {
                    runCatching { Uri.parse(normalizedUrl).host }.getOrNull() ?: normalizedUrl
                }
                sites = if (dialog.siteId == null) {
                    sites + MultiWebSite(
                        id = UUID.randomUUID().toString(),
                        name = resolvedName,
                        url = normalizedUrl,
                        type = "URL",
                        appType = "WEB",
                        siteProjectId = UUID.randomUUID().toString(),
                        enabled = true,
                        sortIndex = sites.size
                    )
                } else {
                    sites.map {
                        if (it.id == dialog.siteId) it.copy(name = resolvedName, url = normalizedUrl) else it
                    }
                }
                siteDialog = null
            }
        )
    }
}

private data class SiteDialogData(val siteId: String?, val name: String, val url: String)

@Composable
private fun SiteEditDialog(
    data: SiteDialogData,
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String) -> Unit
) {
    var name by remember(data.siteId) { mutableStateOf(data.name) }
    var url by remember(data.siteId) { mutableStateOf(data.url) }
    val normalized = ensureWebUrlScheme(url)
    val urlValid = normalized.isNotBlank() &&
        runCatching { Uri.parse(normalized).host?.isNotBlank() == true }.getOrDefault(false)
    val isEdit = data.siteId != null

    WtaAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.AddLink,
        title = if (isEdit) Strings.multiWebEditSite else Strings.multiWebAddCustomSite,
        content = {
            PremiumTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(Strings.name) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            PremiumTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(Strings.labelUrl) },
                placeholder = { Text("https://example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = url.isNotBlank() && !urlValid,
                supportingText = if (url.isNotBlank() && !urlValid) {
                    { Text(Strings.pleaseEnterValidUrl) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), url.trim()) },
                enabled = urlValid
            ) {
                Text(if (isEdit) Strings.btnSave else Strings.add)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Strings.cancel) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExistingAppPicker(
    existingApps: List<com.webtoapp.data.model.WebApp>,
    categories: List<com.webtoapp.data.model.AppCategory>,
    selectedAppIds: Set<Long>,
    addedAppIds: Set<Long>,
    onAddCustomSite: () -> Unit,
    filterType: String?,
    filterCategoryId: Long?,
    onFilterTypeChange: (String?) -> Unit,
    onFilterCategoryChange: (Long?) -> Unit,
    onToggleApp: (Long) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onAddSelected: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.onSurface
    // Nested multi-web and server-runtime apps (Node/PHP/Python/Go/WordPress) cannot be
    // embedded as site sources: their runtimes are not packaged into a multi-web APK, so
    // such sites would render a broken shell mode in the export. The build degrades
    // legacy configs to URL sites; the picker hides them so new ones are never created.
    val eligibleApps = existingApps.filter {
        it.appType != com.webtoapp.data.model.AppType.MULTI_WEB && !it.appType.requiresProcessExec
    }
    val availableTypes = remember(eligibleApps) {
        eligibleApps.map { it.appType.name }.distinct()
    }
    val filteredApps = remember(eligibleApps, filterType, filterCategoryId) {
        eligibleApps.filter { app ->
            val typeMatch = filterType == null || app.appType.name == filterType
            val categoryMatch = when {
                filterCategoryId == null -> true
                filterCategoryId == -1L -> app.categoryId == null
                else -> app.categoryId == filterCategoryId
            }
            typeMatch && categoryMatch
        }
    }
    val alreadyAddedIds = addedAppIds

    Column {
        Text(
            Strings.multiWebAddSite,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            onClick = onAddCustomSite,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.AddLink, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    Strings.multiWebAddCustomSite,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (eligibleApps.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Apps, null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        Strings.multiWebNoApps,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return@Column
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (availableTypes.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    WtaChip(
                        selected = filterType == null,
                        onClick = { onFilterTypeChange(null) },
                        label = Strings.all,
                        showSelectedCheck = false
                    )
                }
                items(availableTypes.size) { index ->
                    val type = availableTypes[index]
                    val (icon, label) = appTypeFilterInfo(type)
                    WtaChip(
                        selected = filterType == type,
                        onClick = { onFilterTypeChange(if (filterType == type) null else type) },
                        label = label,
                        leadingIcon = icon,
                        showSelectedCheck = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(WtaSpacing.Tiny))
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                WtaChip(
                    selected = filterCategoryId == null,
                    onClick = { onFilterCategoryChange(null) },
                    label = Strings.allApps,
                    showSelectedCheck = false
                )
            }
            item {
                WtaChip(
                    selected = filterCategoryId == -1L,
                    onClick = { onFilterCategoryChange(-1L) },
                    label = Strings.uncategorized,
                    showSelectedCheck = false
                )
            }
            items(categories.size) { index ->
                val cat = categories[index]
                WtaChip(
                    selected = filterCategoryId == cat.id,
                    onClick = { onFilterCategoryChange(if (filterCategoryId == cat.id) null else cat.id) },
                    label = cat.name,
                    leadingIcon = com.webtoapp.util.SvgIconMapper.getIcon(cat.icon),
                    showSelectedCheck = false
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    onSelectAll(selectedAppIds.size == filteredApps.size)
                }
            ) {
                Text(
                    if (selectedAppIds.size == filteredApps.size) Strings.deselectAll else Strings.selectAll,
                    fontSize = 12.sp,
                    color = accentColor
                )
            }
        }

        if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    Strings.noSearchResult,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            filteredApps.forEach { app ->
                val isSelected = app.id in selectedAppIds
                val isAdded = app.id in alreadyAddedIds
                Card(
                    onClick = { if (!isAdded) onToggleApp(app.id) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAdded,
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isAdded -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder(true) else null
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Language, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                app.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                app.url.ifBlank { app.appType.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isAdded) {
                            Icon(
                                Icons.Outlined.CheckCircle, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggleApp(app.id) },
                                modifier = Modifier.size(24.dp),
                                colors = CheckboxDefaults.colors(checkedColor = accentColor)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        if (selectedAppIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAddSelected,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (selectedAppIds.size > 1) "${Strings.multiWebAddSite} (${selectedAppIds.size})" else Strings.multiWebAddSite)
            }
        }
    }
}

private fun getFilteredAppIds(
    existingApps: List<com.webtoapp.data.model.WebApp>,
    filterType: String?,
    filterCategoryId: Long?
): Set<Long> {
    return existingApps
        .filter { it.appType != com.webtoapp.data.model.AppType.MULTI_WEB && !it.appType.requiresProcessExec }
        .filter { app ->
            val typeMatch = filterType == null || app.appType.name == filterType
            val categoryMatch = when {
                filterCategoryId == null -> true
                filterCategoryId == -1L -> app.categoryId == null
                else -> app.categoryId == filterCategoryId
            }
            typeMatch && categoryMatch
        }
        .map { it.id }
        .toSet()
}

@Composable
private fun StartTabChoice(
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        StartTabRadioRow(
            selected = selected == MULTI_WEB_START_LAST,
            label = Strings.multiWebStartLast,
            onClick = { onSelect(MULTI_WEB_START_LAST) }
        )
        StartTabRadioRow(
            selected = selected == MULTI_WEB_START_SITE,
            label = Strings.multiWebStartSite,
            onClick = { onSelect(MULTI_WEB_START_SITE) }
        )
    }
}

@Composable
private fun StartTabRadioRow(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class DisplayModeOption(
    val mode: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val desc: String
)

@Composable
private fun DisplayModePicker(
    selected: String,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        DisplayModeOption("TABS", Icons.Outlined.Dock, Strings.multiWebModeTabs, Strings.multiWebModeTabsDesc),
        DisplayModeOption("TOP_TABS", Icons.Outlined.Tab, Strings.multiWebModeTopTabs, Strings.multiWebModeTopTabsDesc),
        DisplayModeOption("CARDS", Icons.Outlined.GridView, Strings.multiWebModeCards, Strings.multiWebModeCardsDesc),
        DisplayModeOption("DRAWER", Icons.Outlined.ViewSidebar, Strings.multiWebModeDrawer, Strings.multiWebModeDrawerDesc),
        DisplayModeOption("FEED", Icons.Outlined.RssFeed, Strings.multiWebModeFeed, Strings.multiWebModeFeedDesc)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowOptions.forEach { option ->
                    DisplayModeCard(
                        option = option,
                        isSelected = selected == option.mode,
                        onClick = { onSelect(option.mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DisplayModeCard(
    option: DisplayModeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 0.dp,
            color = borderColor
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    option.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isSelected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                option.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (LocalShowDescriptions.current) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    option.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SiteItem(
    site: MultiWebSite,
    showFeedConfig: Boolean,
    onDelete: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (site.enabled)
            MaterialTheme.colorScheme.surfaceContainerLow
        else
            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .then(dragHandleModifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Apps,
                    contentDescription = Strings.multiWebDragToReorder,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    site.name.ifBlank { Strings.multiWebTypeExisting },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    site.url.ifBlank { Strings.multiWebTypeExisting },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showFeedConfig && site.cssSelector.isNotBlank()) {
                    Text(
                        "CSS: ${site.cssSelector}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (site.enabled) Strings.multiWebDisableSite else Strings.multiWebEnableSite) },
                        onClick = { showMenu = false; onToggleEnabled(!site.enabled) },
                        leadingIcon = {
                            Icon(
                                if (site.enabled) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                null, modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    if (onEdit != null) {
                        DropdownMenuItem(
                            text = { Text(Strings.multiWebEditSite) },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = {
                                Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp))
                            }
                        )
                    }
                    if (onMoveUp != null || onMoveDown != null) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(Strings.multiWebMoveUp) },
                            onClick = { showMenu = false; onMoveUp?.invoke() },
                            leadingIcon = { Icon(Icons.Outlined.KeyboardArrowUp, null, modifier = Modifier.size(18.dp)) },
                            enabled = onMoveUp != null
                        )
                        DropdownMenuItem(
                            text = { Text(Strings.multiWebMoveDown) },
                            onClick = { showMenu = false; onMoveDown?.invoke() },
                            leadingIcon = { Icon(Icons.Outlined.KeyboardArrowDown, null, modifier = Modifier.size(18.dp)) },
                            enabled = onMoveDown != null
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(Strings.multiWebDeleteSite, color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = {
                            Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun MultiWebIntSlider(
    label: String,
    hint: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (LocalShowDescriptions.current) {
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt().coerceIn(range.first, range.last)) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first - 1).coerceAtLeast(0)
        )
    }
}

@Composable
private fun MultiWebCardHeightSlider(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val percent = (MULTI_WEB_DEFAULT_CARD_ASPECT / value * 100f).roundToInt().coerceIn(40, 100)
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(Strings.multiWebCardHeight, style = MaterialTheme.typography.bodyMedium)
            Text(
                "$percent%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (LocalShowDescriptions.current) {
            Text(
                Strings.multiWebCardHeightHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = { raw ->
                val snapped = (raw * 5f).roundToInt() / 5f
                onValueChange(snapped.coerceIn(MULTI_WEB_DEFAULT_CARD_ASPECT, 2.8f))
            },
            valueRange = MULTI_WEB_DEFAULT_CARD_ASPECT..2.8f,
            steps = 7
        )
    }
}

private fun List<MultiWebSite>.reindexed(): List<MultiWebSite> =
    mapIndexed { index, site -> if (site.sortIndex == index) site else site.copy(sortIndex = index) }

private fun appTypeFilterInfo(typeName: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, String> {
    return when (typeName) {
        "WEB" -> Icons.Outlined.Public to Strings.appTypeWeb
        "IMAGE" -> Icons.Outlined.Image to Strings.appTypeImage
        "VIDEO" -> Icons.Outlined.VideoLibrary to Strings.appTypeVideo
        "HTML" -> Icons.Outlined.Html to Strings.appTypeHtml
        "GALLERY" -> Icons.Outlined.PhotoLibrary to Strings.appTypeGallery
        "FRONTEND" -> Icons.Outlined.Rocket to Strings.appTypeFrontend
        "WORDPRESS" -> Icons.Outlined.Newspaper to Strings.appTypeWordPress
        "NODEJS_APP" -> Icons.Outlined.Terminal to Strings.appTypeNodeJs
        "PHP_APP" -> Icons.Outlined.DataObject to Strings.appTypePhp
        "PYTHON_APP" -> Icons.Outlined.Psychology to Strings.appTypePython
        "GO_APP" -> Icons.Outlined.Speed to Strings.appTypeGo
        "MULTI_WEB" -> Icons.Outlined.Language to Strings.appTypeMultiWeb
        else -> Icons.Outlined.Apps to typeName
    }
}
