package com.webtoapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.MultiWebConfig
import com.webtoapp.data.model.MultiWebSite
import com.webtoapp.data.model.HtmlFileType
import com.webtoapp.ui.components.EnhancedElevatedCard
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.components.RuntimeIconPickerCard
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

    var selectedAppIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var filterType by remember { mutableStateOf<String?>(null) }
    var filterCategoryId by remember { mutableStateOf<Long?>(null) }
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
                            displayMode = "TABS",
                            refreshInterval = refreshInterval,
                            showSiteIcons = true,
                            projectId = ""
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

                            Spacer(modifier = Modifier.height(12.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                sites.forEachIndexed { index, site ->
                                    SiteItem(
                                        site = site,
                                        showFeedConfig = false,
                                        onDelete = {
                                            sites = sites.toMutableList().also { it.removeAt(index) }
                                        },
                                        onToggleEnabled = { enabled ->
                                            sites = sites.toMutableList().also {
                                                it[index] = site.copy(enabled = enabled)
                                            }
                                        },
                                        onMoveUp = if (index > 0) {
                                            { sites = sites.toMutableList().also { val item = it.removeAt(index); it.add(index - 1, item) } }
                                        } else null,
                                        onMoveDown = if (index < sites.size - 1) {
                                            { sites = sites.toMutableList().also { val item = it.removeAt(index); it.add(index + 1, item) } }
                                        } else null
                                    )
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
                                            localFilePath = entryFile?.name ?: "index.html"
                                            sourceProjectId = app.htmlConfig!!.projectId
                                        }
                                        MultiWebSite(
                                            id = UUID.randomUUID().toString(),
                                            name = app.name,
                                            url = app.url,
                                            type = "EXISTING",
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
                        Text(
                            Strings.multiWebCustomCodeDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExistingAppPicker(
    existingApps: List<com.webtoapp.data.model.WebApp>,
    categories: List<com.webtoapp.data.model.AppCategory>,
    selectedAppIds: Set<Long>,
    addedAppIds: Set<Long>,
    filterType: String?,
    filterCategoryId: Long?,
    onFilterTypeChange: (String?) -> Unit,
    onFilterCategoryChange: (Long?) -> Unit,
    onToggleApp: (Long) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onAddSelected: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.onSurface
    val eligibleApps = existingApps.filter { it.appType != com.webtoapp.data.model.AppType.MULTI_WEB }
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = filterType == null,
                        onClick = { onFilterTypeChange(null) },
                        label = { Text(Strings.all, fontSize = 12.sp) }
                    )
                }
                items(availableTypes.size) { index ->
                    val type = availableTypes[index]
                    val (icon, label) = appTypeFilterInfo(type)
                    FilterChip(
                        selected = filterType == type,
                        onClick = { onFilterTypeChange(if (filterType == type) null else type) },
                        label = { Text(label, fontSize = 12.sp) },
                        leadingIcon = { Icon(icon, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = filterCategoryId == null,
                    onClick = { onFilterCategoryChange(null) },
                    label = { Text(Strings.allApps, fontSize = 12.sp) }
                )
            }
            item {
                FilterChip(
                    selected = filterCategoryId == -1L,
                    onClick = { onFilterCategoryChange(-1L) },
                    label = { Text(Strings.uncategorized, fontSize = 12.sp) }
                )
            }
            items(categories.size) { index ->
                val cat = categories[index]
                FilterChip(
                    selected = filterCategoryId == cat.id,
                    onClick = { onFilterCategoryChange(if (filterCategoryId == cat.id) null else cat.id) },
                    label = { Text(cat.name, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            com.webtoapp.util.SvgIconMapper.getIcon(cat.icon),
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
        .filter { it.appType != com.webtoapp.data.model.AppType.MULTI_WEB }
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
private fun SiteItem(
    site: MultiWebSite,
    showFeedConfig: Boolean,
    onDelete: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Apps, null,
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
