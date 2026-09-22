package com.webtoapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.plugin.*
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.components.WtaCodeEditorDialog
import com.webtoapp.ui.design.*
import kotlinx.coroutines.launch

/**
 * Plugin editor: manifest fields on the first tab, package files on the rest.
 * No DSL forms — what you write is what ships inside the .hcj directory.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginEditorScreen(
    pluginId: String?,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { PluginStore.getInstance(context) }

    var loaded by remember { mutableStateOf(pluginId == null) }
    var isNew by remember { mutableStateOf(pluginId == null) }
    var kind by remember { mutableStateOf(PluginKind.HCJ) }

    // Manifest fields. Only name/description are user-facing; the
    // rest is preserved from the on-disk manifest (or defaulted for new
    // plugins) — self-authored code needs no permission ceremony.
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

    // Package files — one authored document (plugin.html); css only survives
    // a load→save round-trip for legacy packages that still carry style.css.
    var pluginHtml by remember { mutableStateOf("") }
    var css by remember { mutableStateOf("") }
    var extraFiles by remember { mutableStateOf(mapOf<String, String>()) }

    var showCodeEditor by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    // Fields the form doesn't edit (userscript grants/requires, legacyCompat,
    // noframes, preferredEntry) — preserved from the on-disk manifest on save
    // so editing a userscript or a migrated plugin doesn't silently strip them.
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
        // Manifest on disk may carry fields not mirrored into the Plugin record
        // (e.g. gmGrants) — prefer it when present.
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
        loaded = true
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
                title = { Text(if (isNew) Strings.pluginNew else Strings.pluginEditorEdit) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.back)
                    }
                },
                actions = {
                    WtaButton(
                        onClick = ::save,
                        text = Strings.save,
                        variant = WtaButtonVariant.Primary,
                        size = WtaButtonSize.Small,
                        leadingIcon = Icons.Filled.Check,
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Field(Strings.pluginFieldName, name, { name = it; nameError = false },
                    isError = nameError, errorText = Strings.pluginNameRequired)
                Field(Strings.description, description, { description = it })
                CodeSection(
                    content = pluginHtml,
                    language = "HTML",
                    fileName = PluginStore.PLUGIN_FILE,
                    placeholder = HTML_PLACEHOLDER,
                    onEdit = { showCodeEditor = true }
                )
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

@Composable
private fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    minLines: Int = 1,
    isError: Boolean = false,
    errorText: String? = null,
    placeholder: String? = null,
    hint: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        PremiumTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            isError = isError,
            placeholder = placeholder?.let { p -> { Text(p) } },
            shape = RoundedCornerShape(WtaRadius.Button)
        )
        when {
            isError && errorText != null -> Text(
                errorText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
            hint != null -> Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CodeSection(
    content: String,
    language: String,
    fileName: String,
    placeholder: String,
    onEdit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                fileName,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            Text(
                language,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(10.dp))
            WtaButton(
                onClick = onEdit,
                text = Strings.edit,
                variant = WtaButtonVariant.Tonal,
                size = WtaButtonSize.Small,
                leadingIcon = Icons.Default.Edit
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(WtaRadius.Button))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            if (content.isNotBlank()) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
