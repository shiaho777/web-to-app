package com.webtoapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.plugin.PluginKind
import com.webtoapp.core.plugin.PluginManifest
import com.webtoapp.core.plugin.PluginPermission
import com.webtoapp.core.plugin.PluginRunAt
import com.webtoapp.core.plugin.PluginStore
import com.webtoapp.core.plugin.buildPluginHtml
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.components.WtaCodeEditorDialog
import com.webtoapp.ui.design.WtaBackground
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaChoiceRow
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSegmentedRow
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaShapePlate
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaToggleRow
import com.webtoapp.ui.theme.LocalShowDescriptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun PluginEditorScreen(
    pluginId: String?,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { PluginStore.getInstance(context) }

    var isNew by remember { mutableStateOf(pluginId == null) }
    var kind by remember { mutableStateOf(PluginKind.HCJ) }

    var id by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var version by remember { mutableStateOf("1.0.0") }
    var author by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var homepage by remember { mutableStateOf("") }
    var matches by remember { mutableStateOf("*") }
    var excludeMatches by remember { mutableStateOf("") }
    var runAt by remember { mutableStateOf(PluginRunAt.DOCUMENT_END) }
    var permissions by remember { mutableStateOf(PluginPermission.values().toSet()) }
    var showEntry by remember { mutableStateOf(true) }

    var pluginHtml by remember { mutableStateOf("") }
    var css by remember { mutableStateOf("") }
    var extraFiles by remember { mutableStateOf(mapOf<String, String>()) }

    var showCodeEditor by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var advancedExpanded by remember { mutableStateOf(false) }
    var preservedManifest by remember { mutableStateOf<PluginManifest?>(null) }

    LaunchedEffect(pluginId) {
        if (pluginId == null) {
            pluginHtml = NEW_PLUGIN_STUB
            return@LaunchedEffect
        }
        val plugin = store.getPlugin(pluginId) ?: run { onNavigateBack(); return@LaunchedEffect }
        kind = plugin.kind
        id = plugin.id
        name = plugin.name
        version = plugin.versionName
        author = plugin.authorName
        description = plugin.description
        homepage = plugin.homepage
        matches = plugin.matches.filter { !it.exclude }.joinToString("\n") {
            if (it.isRegex) "/${it.pattern}/" else it.pattern
        }.ifBlank { "*" }
        excludeMatches = plugin.matches.filter { it.exclude }.joinToString("\n") {
            if (it.isRegex) "/${it.pattern}/" else it.pattern
        }
        runAt = plugin.runAt
        permissions = plugin.permissions.toSet()
        showEntry = plugin.showInToolbar

        val files = store.readPackageFiles(plugin.id)
        val rawManifest = files[PluginStore.MANIFEST_FILE]
            ?.let { PluginManifest.fromJson(it) }
        preservedManifest = rawManifest
        if (rawManifest != null) {
            matches = rawManifest.matches.joinToString("\n").ifBlank { "*" }
            excludeMatches = rawManifest.excludeMatches.joinToString("\n")
            runAt = rawManifest.resolvedRunAt()
            permissions = rawManifest.resolvedPermissions()
            showEntry = rawManifest.toolbar
            homepage = rawManifest.homepage
        }
        pluginHtml = files[PluginStore.PLUGIN_FILE]
            ?: buildPluginHtml(
                files[PluginStore.MAIN_FILE].orEmpty(),
                files[PluginStore.PANEL_FILE].orEmpty()
            )
        css = files[PluginStore.CSS_FILE].orEmpty()
        extraFiles = files - PluginStore.MANIFEST_FILE - PluginStore.MAIN_FILE -
            PluginStore.CSS_FILE - PluginStore.PANEL_FILE - PluginStore.PLUGIN_FILE
    }

    fun save() {
        if (name.isBlank()) {
            nameError = true
            return
        }
        val manifest = PluginManifest(
            id = if (isNew) id.ifBlank { slugFor(name) } else id,
            name = name.trim(),
            version = version.ifBlank { "1.0.0" },
            description = description.trim(),
            author = author.trim(),
            homepage = homepage.trim(),
            matches = matches.lines().map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("*") },
            excludeMatches = excludeMatches.lines().map { it.trim() }.filter { it.isNotEmpty() },
            runAt = runAt.name.lowercase(),
            permissions = permissions.map { it.name },
            toolbar = showEntry,
            preferredEntry = preservedManifest?.preferredEntry.orEmpty(),
            preferredPanel = preservedManifest?.preferredPanel.orEmpty(),
            gmGrants = preservedManifest?.gmGrants.orEmpty(),
            requireUrls = preservedManifest?.requireUrls.orEmpty(),
            resources = preservedManifest?.resources.orEmpty(),
            noframes = preservedManifest?.noframes ?: false,
            legacyCompat = preservedManifest?.legacyCompat ?: false
        )
        val files = buildMap {
            put(PluginStore.PLUGIN_FILE, pluginHtml)
            if (css.isNotBlank()) put(PluginStore.CSS_FILE, css)
            putAll(extraFiles)
        }
        scope.launch {
            val result = store.installPackage(manifest, kind, files)
            result.onSuccess {
                Toast.makeText(context, Strings.saveSuccess, Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }.onFailure {
                Toast.makeText(context, Strings.saveFailed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WtaShapePlate(
                            polygon = MaterialShapes.PixelCircle,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Code,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isNew) Strings.pluginNew else Strings.pluginEditorEdit,
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = Strings.back)
                    }
                },
                actions = {
                    WtaButton(
                        onClick = ::save,
                        text = Strings.save,
                        variant = WtaButtonVariant.Primary,
                        size = WtaButtonSize.Small,
                        leadingIcon = Icons.Rounded.Check,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        }
    ) { padding ->
        WtaBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(WtaSpacing.ScreenHorizontal),
                verticalArrangement = Arrangement.spacedBy(WtaSpacing.SectionGap)
            ) {
                WtaCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = WtaCardTone.Surface,
                    contentPadding = PaddingValues(WtaSpacing.Large)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)) {
                        PremiumTextField(
                            value = name,
                            onValueChange = { name = it; nameError = false },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(Strings.pluginFieldName) },
                            placeholder = { Text(Strings.pluginNamePlaceholder) },
                            leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
                            singleLine = true,
                            isError = nameError,
                            supportingText = if (nameError) {
                                { Text(Strings.pluginNameRequired) }
                            } else null,
                            shape = RoundedCornerShape(WtaRadius.Control)
                        )
                        PremiumTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(Strings.description) },
                            placeholder = { Text(Strings.pluginDescriptionPlaceholder) },
                            leadingIcon = { Icon(Icons.Rounded.Subject, contentDescription = null) },
                            minLines = 2,
                            shape = RoundedCornerShape(WtaRadius.Control)
                        )
                    }
                }

                CodeCard(
                    content = pluginHtml,
                    onEdit = { showCodeEditor = true }
                )

                WtaSettingCard {
                    Column {
                        WtaChoiceRow(
                            title = Strings.advancedSettings,
                            value = "",
                            icon = Icons.Rounded.Tune,
                            isExpanded = advancedExpanded,
                            onClick = { advancedExpanded = !advancedExpanded }
                        )
                        AnimatedVisibility(
                            visible = advancedExpanded,
                            enter = CardExpandTransition,
                            exit = CardCollapseTransition
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = WtaSpacing.RowHorizontal,
                                    vertical = WtaSpacing.ContentGap
                                ),
                                verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)) {
                                    PremiumTextField(
                                        value = version,
                                        onValueChange = { version = it },
                                        modifier = Modifier.weight(1f),
                                        label = { Text(Strings.pluginFieldVersion) },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        shape = RoundedCornerShape(WtaRadius.Control)
                                    )
                                    PremiumTextField(
                                        value = author,
                                        onValueChange = { author = it },
                                        modifier = Modifier.weight(1f),
                                        label = { Text(Strings.author) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(WtaRadius.Control)
                                    )
                                }
                                PremiumTextField(
                                    value = matches,
                                    onValueChange = { matches = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(Strings.moduleMarketUrlMatches) },
                                    minLines = 2,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    supportingText = if (LocalShowDescriptions.current) {
                                        { Text(Strings.pluginMatchesHint) }
                                    } else null,
                                    shape = RoundedCornerShape(WtaRadius.Control)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        Strings.pluginFieldRunAt,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    WtaSegmentedRow(
                                        options = listOf(
                                            Strings.runAtStart,
                                            Strings.runAtEnd,
                                            Strings.runAtIdle
                                        ),
                                        selectedIndex = runAt.ordinal,
                                        onSelect = { runAt = PluginRunAt.entries[it] }
                                    )
                                }
                                WtaToggleRow(
                                    title = Strings.pluginShowEntry,
                                    checked = showEntry,
                                    onCheckedChange = { showEntry = it }
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        Strings.pluginFieldPermissions,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap),
                                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                                    ) {
                                        PluginPermission.entries.forEach { permission ->
                                            WtaChip(
                                                selected = permission in permissions,
                                                onClick = {
                                                    permissions = if (permission in permissions) {
                                                        permissions - permission
                                                    } else {
                                                        permissions + permission
                                                    }
                                                },
                                                label = permissionLabel(permission)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCodeEditor) {
        WtaCodeEditorDialog(
            language = "HTML",
            initialContent = pluginHtml,
            placeholder = HTML_PLACEHOLDER,
            onSave = { newCode ->
                pluginHtml = newCode
                showCodeEditor = false
            },
            onDismiss = { showCodeEditor = false }
        )
    }
}

private fun slugFor(name: String): String =
    name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { "my-plugin" }

private fun permissionLabel(permission: PluginPermission): String = when (permission) {
    PluginPermission.STORAGE -> Strings.pluginPermStorage
    PluginPermission.FETCH -> Strings.pluginPermFetch
    PluginPermission.NOTIFY -> Strings.pluginPermNotify
    PluginPermission.BADGE -> Strings.pluginPermBadge
    PluginPermission.CLIPBOARD -> Strings.pluginPermClipboard
    PluginPermission.DOWNLOAD -> Strings.pluginPermDownload
    PluginPermission.SHARE -> Strings.pluginPermShare
    PluginPermission.OPEN_EXTERNAL -> Strings.pluginPermOpenExternal
    PluginPermission.CLEAR_DATA -> Strings.pluginPermClearData
    PluginPermission.EXIT_APP -> Strings.pluginPermExitApp
}

private val HTML_PLACEHOLDER = """
<!-- plugin.html — one file is the whole plugin -->
<script type="hcj/page">
// runs inside matching pages — hcj.* API
// hcj.config · hcj.fetch · hcj.badge · hcj.panel · hcj.notify · hcj.on · hcj.emit · hcj.addStyle
</script>

<!-- everything else in this document is the panel UI (hcjPanel API) -->
""".trimStart() + "\n"

private val NEW_PLUGIN_STUB = """
<script type="hcj/page">
// runs inside matching pages — hcj.* API
hcj.on('action', () => {
    // Fired when the user taps this plugin's entry.
});
</script>

<!-- Panel UI below — normal HTML/CSS/JS, talk to the page via hcjPanel.send -->
<style>
  body { font-family: sans-serif; padding: 16px; }
</style>

""".trimStart()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CodeCard(content: String, onEdit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val display = content.ifBlank { HTML_PLACEHOLDER }
    val monoStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
    WtaCard(
        modifier = Modifier.fillMaxWidth(),
        tone = WtaCardTone.Surface,
        contentPadding = PaddingValues(WtaSpacing.Large)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WtaShapePlate(
                polygon = MaterialShapes.PixelCircle,
                containerColor = colors.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colors.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    PluginStore.PLUGIN_FILE,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WtaInfoChip(label = "HTML")
                    WtaInfoChip(
                        label = display.lines().size.toString(),
                        icon = Icons.Rounded.FormatLineSpacing
                    )
                    WtaInfoChip(
                        label = formatCodeSize(display.toByteArray().size),
                        icon = Icons.Rounded.DataObject
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            WtaButton(
                onClick = onEdit,
                text = Strings.edit,
                variant = WtaButtonVariant.Tonal,
                size = WtaButtonSize.Small,
                leadingIcon = Icons.Rounded.Edit
            )
        }
        Spacer(Modifier.height(WtaSpacing.ContentGap))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(WtaRadius.Control))
                .background(colors.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = display,
                    style = monoStyle,
                    color = if (content.isBlank()) colors.onSurfaceVariant else colors.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, colors.surfaceContainerLow)
                        )
                    )
            )
        }
    }
}

private fun formatCodeSize(bytes: Int): String {
    if (bytes < 1024) return "${bytes}B"
    return String.format(java.util.Locale.US, "%.1fKB", bytes / 1024.0)
}
