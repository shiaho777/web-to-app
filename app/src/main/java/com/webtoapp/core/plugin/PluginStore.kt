package com.webtoapp.core.plugin

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.util.GsonProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Host-side plugin store. The package directory is the source of truth:
 *
 * ```
 * files/plugins/
 *   <id>/plugin.json     manifest (authored for HCJ, generated for userscripts)
 *   <id>/plugin.html     the authored document: inert `<script type="hcj/page">`
 *                        block = page script; the rest = the panel UI
 *   <id>/main.js         legacy page script — still injected when present
 *   <id>/style.css       legacy optional — still injected when present
 *   <id>/panel.html      legacy optional — hosted by the plugin panel surface
 *   <id>/icon.*          optional package icon
 *   <id>/files/...       optional extra files (migrated multi-file modules)
 * files/plugin_state.json   list order + chrome-extension records
 * ```
 *
 * CHROME_EXTENSION records live in the state file only; their content stays in
 * the extension engine's own directory (`ExtensionFileManager`).
 *
 * Built-in HCJ packages ship read-only in `assets/plugins/`. There is no
 * enable/pin state — a plugin runs where the app's config attaches it.
 */
@Suppress("StaticFieldLeak")
class PluginStore private constructor(private val context: Context) {

    companion object {
        private const val TAG = "PluginStore"
        const val PLUGINS_DIR = "plugins"
        const val STATE_FILE = "plugin_state.json"
        const val BUILTIN_ASSET_DIR = "plugins"
        const val PANEL_FILE = "panel.html"
        const val MAIN_FILE = "main.js"
        const val PLUGIN_FILE = "plugin.html"
        const val CSS_FILE = "style.css"
        const val MANIFEST_FILE = "plugin.json"

        @Volatile
        private var INSTANCE: PluginStore? = null

        fun getInstance(context: Context): PluginStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PluginStore(context.applicationContext).also { INSTANCE = it }
            }

        fun release() {
            synchronized(this) {
                INSTANCE?.shutdown()
                INSTANCE = null
            }
        }
    }

    private val gson: Gson = GsonBuilder().setLenient().serializeNulls().create()
    private val saveMutex = Mutex()

    private val pluginsDir: File by lazy {
        File(context.filesDir, PLUGINS_DIR).apply { mkdirs() }
    }
    private val stateFile: File get() = File(context.filesDir, STATE_FILE)

    private val _plugins = MutableStateFlow<List<Plugin>>(emptyList())
    val plugins: StateFlow<List<Plugin>> = _plugins.asStateFlow()

    private val _builtInPlugins = MutableStateFlow<List<Plugin>>(emptyList())
    val builtInPlugins: StateFlow<List<Plugin>> = _builtInPlugins.asStateFlow()

    @Volatile
    private var allCache: List<Plugin> = emptyList()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var released = false

    @Volatile
    private var builtInsLanguage: AppLanguage? = null

    /** Per-plugin style overrides chosen in the manager (id → entry/panel).
     *  Declared before init: loadBuiltIns runs during construction. */
    private val styleOverrides = java.util.concurrent.ConcurrentHashMap<String, StyleOverride>()

    /** Built-ins the user hid via the row menu; restorable from the ⋮ menu. */
    private val _hiddenBuiltIns = MutableStateFlow<Set<String>>(emptySet())
    val hiddenBuiltIns: StateFlow<Set<String>> = _hiddenBuiltIns.asStateFlow()

    init {
        loadBuiltIns()
        rebuildCache()
        scope.launch {
            PluginMigrator(context).migrateIfNeeded()
            loadPackages()
            rebuildCache()
        }.invokeOnCompletion {
            _isLoading.value = false
        }
    }

    private fun shutdown() {
        released = true
        scope.cancel()
    }

    suspend fun awaitLoaded() {
        isLoading.first { !it }
    }

    // ------------------------------------------------------------------
    // State overlay
    // ------------------------------------------------------------------

    private data class StyleOverride(val entry: String, val panel: String)

    private data class StateOverlay(
        val order: MutableList<String> = mutableListOf(),
        val chromeRecords: MutableList<Plugin> = mutableListOf(),
        val styles: MutableMap<String, StyleOverride> = mutableMapOf(),
        val hidden: MutableList<String> = mutableListOf()
    )

    private fun readOverlay(): StateOverlay {
        if (!stateFile.exists()) return StateOverlay()
        return try {
            val obj = JsonParser.parseString(stateFile.readText()).asJsonObject
            val overlay = StateOverlay()
            obj.getAsJsonArray("order")?.forEach { overlay.order.add(it.asString) }
            obj.getAsJsonArray("hidden")?.forEach { overlay.hidden.add(it.asString) }
            obj.getAsJsonObject("styles")?.entrySet()?.forEach { (id, el) ->
                runCatching {
                    val s = el.asJsonObject
                    overlay.styles[id] = StyleOverride(
                        entry = s.get("entry")?.asString.orEmpty(),
                        panel = s.get("panel")?.asString.orEmpty()
                    )
                }
            }
            obj.getAsJsonArray("chromeRecords")?.forEach { el ->
                try {
                    gson.fromJson(el, Plugin::class.java)?.let {
                        overlay.chromeRecords.add(it.copy(kind = PluginKind.CHROME_EXTENSION))
                    }
                } catch (_: Exception) {
                }
            }
            overlay
        } catch (e: Exception) {
            AppLogger.e(TAG, "failed to read plugin state", e)
            StateOverlay()
        }
    }

    private suspend fun writeOverlay() = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            try {
                val overlay = JsonParser.parseString("{}").asJsonObject
                val order = com.google.gson.JsonArray()
                (_plugins.value.map { it.id } + _builtInPlugins.value.map { it.id })
                    .forEach { order.add(it) }
                overlay.add("order", order)
                val hidden = com.google.gson.JsonArray()
                hiddenBuiltIns.value.forEach { hidden.add(it) }
                overlay.add("hidden", hidden)
                val styles = com.google.gson.JsonObject()
                (_plugins.value + _builtInPlugins.value).forEach { p ->
                    styleOverrides[p.id]?.let { o ->
                        styles.add(p.id, com.google.gson.JsonObject().apply {
                            addProperty("entry", o.entry)
                            addProperty("panel", o.panel)
                        })
                    }
                }
                overlay.add("styles", styles)
                val chrome = com.google.gson.JsonArray()
                _plugins.value.filter { it.kind == PluginKind.CHROME_EXTENSION }
                    .forEach { chrome.add(gson.toJsonTree(it)) }
                overlay.add("chromeRecords", chrome)
                stateFile.writeText(gson.toJson(overlay))
            } catch (e: Exception) {
                AppLogger.e(TAG, "failed to write plugin state", e)
            }
        }
    }

    private fun Plugin.withOverride(): Plugin =
        styleOverrides[id]?.let {
            copy(entryStyle = PluginEntryStyle.parse(it.entry), panelStyle = PluginPanelStyle.parse(it.panel))
        } ?: this

    // ------------------------------------------------------------------
    // Loading
    // ------------------------------------------------------------------

    private suspend fun loadPackages() = withContext(Dispatchers.IO) {
        try {
            val overlay = readOverlay()
            val loaded = mutableListOf<Plugin>()

            pluginsDir.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
                val manifestFile = File(dir, MANIFEST_FILE)
                if (!manifestFile.exists()) return@forEach
                val manifest = PluginManifest.fromJson(manifestFile.readText()) ?: return@forEach
                val kind = PluginKind.parse(
                    try {
                        JsonParser.parseString(manifestFile.readText())
                            .asJsonObject.get("kind")?.asString
                    } catch (e: Exception) {
                        null
                    }
                )
                loaded.add(
                    Plugin.fromManifest(
                        manifest = manifest,
                        packageDir = dir.name,
                        kind = kind,
                        hasPanel = dirHasPanel(dir),
                        hasCss = File(dir, CSS_FILE).exists(),
                        builtIn = false
                    )
                )
            }

            styleOverrides.putAll(overlay.styles)
            loaded.addAll(overlay.chromeRecords)

            // Stored order first; anything new (fresh installs, migrated) after.
            val byId = loaded.associateBy { it.id }
            val ordered = overlay.order.mapNotNull { byId[it] } +
                loaded.filter { it.id !in overlay.order }
            _plugins.value = ordered.map { it.withOverride() }
            refreshBuiltIns()
            AppLogger.d(TAG, "loaded ${ordered.size} plugins")
        } catch (e: Exception) {
            AppLogger.e(TAG, "failed to load plugins", e)
            _plugins.value = emptyList()
        }
    }

    /** plugin.html wins when it carries markup; legacy panel.html is the fallback signal. */
    private fun dirHasPanel(dir: File): Boolean =
        (File(dir, PLUGIN_FILE).takeIf { it.exists() }
            ?.let { runCatching { hasPanelMarkup(it.readText()) }.getOrDefault(false) }
            ?: false) || File(dir, PANEL_FILE).exists()

    private fun assetDirHasPanel(dir: String): Boolean =
        (assetText("$dir/$PLUGIN_FILE")?.let(::hasPanelMarkup) ?: false) ||
            assetExists("$dir/$PANEL_FILE")

    private fun loadBuiltIns() {
        builtInsLanguage = Strings.lang
        val overlay = readOverlay()
        styleOverrides.putAll(overlay.styles)
        _hiddenBuiltIns.value = overlay.hidden.toSet()
        val loaded = mutableListOf<Plugin>()
        try {
            val dirs = context.assets.list(BUILTIN_ASSET_DIR) ?: emptyArray()
            for (dirName in dirs) {
                val manifestPath = "$BUILTIN_ASSET_DIR/$dirName/$MANIFEST_FILE"
                val manifest = try {
                    context.assets.open(manifestPath).bufferedReader().use { it.readText() }
                        .let { PluginManifest.fromJson(it) }
                } catch (e: Exception) {
                    null
                } ?: continue
                loaded.add(
                    Plugin.fromManifest(
                        manifest = manifest,
                        packageDir = "$BUILTIN_ASSET_DIR/$dirName",
                        kind = PluginKind.HCJ,
                        hasPanel = assetDirHasPanel("$BUILTIN_ASSET_DIR/$dirName"),
                        hasCss = assetExists("$BUILTIN_ASSET_DIR/$dirName/$CSS_FILE"),
                        builtIn = true
                    )
                )
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "failed to load built-in plugins", e)
        }
        val byId = loaded.associateBy { it.id }
        _builtInPlugins.value = filterBuiltIns(
            (overlay.order.mapNotNull { byId[it] } +
                loaded.filter { it.id !in overlay.order }).map { it.withOverride() }
        )
    }

    /** Drop hidden built-ins and any shadowed by an installed copy of the same id. */
    private fun filterBuiltIns(list: List<Plugin>): List<Plugin> {
        val installedIds = _plugins.value.map { it.id }.toSet()
        return list.filter { it.id !in _hiddenBuiltIns.value && it.id !in installedIds }
    }

    private suspend fun refreshBuiltIns() {
        _builtInPlugins.value = filterBuiltIns(_builtInPlugins.value)
    }

    fun reloadBuiltInsIfLanguageChanged() {
        if (_builtInPlugins.value.isNotEmpty() && builtInsLanguage == Strings.lang) return
        loadBuiltIns()
        rebuildCache()
    }

    private fun assetExists(path: String): Boolean = try {
        context.assets.open(path).close()
        true
    } catch (e: Exception) {
        false
    }

    private fun rebuildCache() {
        val userIds = _plugins.value.map { it.id }.toSet()
        allCache = _builtInPlugins.value
            .filter { it.id !in userIds && it.id !in _hiddenBuiltIns.value } + _plugins.value
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------

    fun getAllPlugins(): List<Plugin> = allCache

    fun getPlugin(id: String): Plugin? = allCache.firstOrNull { it.id == id }

    fun getPluginsByIds(ids: List<String>): List<Plugin> {
        val all = allCache
        return ids.mapNotNull { id -> all.firstOrNull { it.id == id } }
    }

    // ------------------------------------------------------------------
    // Code loading (injection path)
    // ------------------------------------------------------------------

    data class PackageCode(
        val mainJs: String = "",
        val css: String = "",
        val panelHtml: String = ""
    )

    fun loadPackageCode(plugin: Plugin): PackageCode {
        return if (plugin.builtIn) loadAssetPackageCode(plugin) else loadDirPackageCode(plugin)
    }

    private fun loadDirPackageCode(plugin: Plugin): PackageCode {
        val dir = File(pluginsDir, plugin.packageDir)
        fun read(name: String) = try {
            File(dir, name).takeIf { it.exists() }?.readText().orEmpty()
        } catch (e: Exception) {
            ""
        }
        return combine(read(PLUGIN_FILE), read(MAIN_FILE), read(CSS_FILE), read(PANEL_FILE))
    }

    private fun loadAssetPackageCode(plugin: Plugin): PackageCode {
        fun read(name: String) = try {
            context.assets.open("${plugin.packageDir}/$name").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
        return combine(read(PLUGIN_FILE), read(MAIN_FILE), read(CSS_FILE), read(PANEL_FILE))
    }

    /**
     * Merge the single-file layout with legacy files. plugin.html supplies the
     * page script via its inert `hcj/page` block and doubles as the panel
     * document; main.js / panel.html are kept working for older packages.
     */
    private fun combine(
        pluginHtml: String,
        legacyMainJs: String,
        css: String,
        legacyPanelHtml: String
    ): PackageCode = PackageCode(
        mainJs = listOf(extractPageJs(pluginHtml), legacyMainJs)
            .filter { it.isNotBlank() }
            .joinToString("\n"),
        css = css,
        // A page-only plugin.html must not shadow a real legacy panel.html,
        // and must not pose as a panel document itself.
        panelHtml = pluginHtml.takeIf { hasPanelMarkup(it) } ?: legacyPanelHtml
    )

    /**
     * Resolve plugins into injectable payloads. `ids == null` resolves every
     * installed plugin (marked unattached so local-runtime pages can skip
     * them); an explicit id list marks everything app-attached.
     */
    fun resolveForInjection(ids: List<String>? = null): List<PluginSession.Resolved> {
        val attached = ids != null
        val base = if (ids == null) allCache else getPluginsByIds(ids)
        return base.map { plugin ->
            if (plugin.isScriptPlugin) {
                val code = loadPackageCode(plugin)
                PluginSession.Resolved(
                    plugin = plugin.copy(hasPanel = hasPanelMarkup(code.panelHtml)),
                    mainJs = code.mainJs,
                    css = code.css,
                    panelHtml = code.panelHtml,
                    attached = attached
                )
            } else {
                PluginSession.Resolved(plugin = plugin, attached = attached)
            }
        }
    }

    fun panelHtmlFor(plugin: Plugin): String? =
        loadPackageCode(plugin).panelHtml.takeIf { it.isNotBlank() }

    // ------------------------------------------------------------------
    // Raw package access (editor)
    // ------------------------------------------------------------------

    private fun assetText(path: String): String? = try {
        context.assets.open(path).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        null
    }

    private fun assetFiles(dir: String): Map<String, String> {
        val out = mutableMapOf<String, String>()
        fun walk(path: String, rel: String) {
            val kids = context.assets.list(path).orEmpty()
            if (kids.isEmpty()) {
                assetText(path)?.let { out[rel] = it }
                return
            }
            kids.forEach { walk("$path/$it", "$rel/$it") }
        }
        context.assets.list(dir).orEmpty().forEach { walk("$dir/$it", it) }
        return out
    }

    /** One text file inside a package; null when absent. Built-ins read from assets. */
    suspend fun readPackageFile(pluginId: String, rel: String): String? =
        withContext(Dispatchers.IO) {
            if (rel.isBlank() || rel.contains("..")) return@withContext null
            val plugin = getPlugin(pluginId)
            if (plugin?.builtIn == true) {
                return@withContext assetText("${plugin.packageDir}/$rel")
            }
            val dirName = plugin?.packageDir?.takeIf { it.isNotBlank() } ?: pluginId
            val f = File(pluginsDir, dirName).resolve(rel)
            try {
                f.takeIf { it.isFile }?.readText()
            } catch (e: Exception) {
                null
            }
        }

    /** Every file in the package keyed by relative path (editor round-trip). */
    suspend fun readPackageFiles(pluginId: String): Map<String, String> =
        withContext(Dispatchers.IO) {
            val plugin = getPlugin(pluginId)
            if (plugin?.builtIn == true) return@withContext assetFiles(plugin.packageDir)
            val dirName = plugin?.packageDir?.takeIf { it.isNotBlank() } ?: pluginId
            val dir = File(pluginsDir, dirName)
            if (!dir.isDirectory) return@withContext emptyMap()
            dir.walkTopDown().filter { it.isFile }.associate { f ->
                f.relativeTo(dir).path to runCatching { f.readText() }.getOrDefault("")
            }
        }

    // ------------------------------------------------------------------
    // Mutation
    // ------------------------------------------------------------------

    /**
     * Persist a new list order (long-press drag in the manager). Ids not in
     * [orderedIds] keep their relative position at the end.
     */
    suspend fun reorder(orderedIds: List<String>, builtIn: Boolean) {
        val flow = if (builtIn) _builtInPlugins else _plugins
        val byId = flow.value.associateBy { it.id }
        flow.value = orderedIds.mapNotNull { byId[it] } +
            flow.value.filter { it.id !in orderedIds }
        writeOverlay()
        rebuildCache()
    }

    /** Persist a per-plugin host-style override (entry + panel). */
    suspend fun setPluginStyle(id: String, entry: PluginEntryStyle, panel: PluginPanelStyle) {
        styleOverrides[id] = StyleOverride(entry.name, panel.name)
        _plugins.value = _plugins.value.map { if (it.id == id) it.withOverride() else it }
        _builtInPlugins.value = _builtInPlugins.value.map { if (it.id == id) it.withOverride() else it }
        writeOverlay()
        rebuildCache()
    }

    suspend fun removePlugin(id: String): Boolean = withContext(Dispatchers.IO) {
        val plugin = _plugins.value.find { it.id == id } ?: return@withContext false
        if (plugin.kind != PluginKind.CHROME_EXTENSION) {
            File(pluginsDir, plugin.packageDir).deleteRecursively()
        }
        _plugins.value = _plugins.value.filter { it.id != id }
        PluginConfigStore(context).clear(id)
        refreshBuiltIns()
        writeOverlay()
        rebuildCache()
        true
    }

    /** Hide a built-in plugin (row ⋮ → Delete); restore from the screen ⋮ menu. */
    suspend fun hideBuiltIn(id: String) {
        _hiddenBuiltIns.value = _hiddenBuiltIns.value + id
        refreshBuiltIns()
        writeOverlay()
        rebuildCache()
    }

    /** Bring back every hidden built-in. */
    suspend fun restoreBuiltIns() {
        if (_hiddenBuiltIns.value.isEmpty()) return
        _hiddenBuiltIns.value = emptySet()
        loadBuiltIns()
        writeOverlay()
        rebuildCache()
    }

    /**
     * Install/overwrite an HCJ package. Returns the installed plugin id.
     * `files` maps package-relative paths ("plugin.html", "files/x.js"; legacy
     * "main.js" / "panel.html" / "style.css" are still honored on load).
     */
    suspend fun installPackage(
        manifest: PluginManifest,
        kind: PluginKind = PluginKind.HCJ,
        files: Map<String, String>
    ): Result<Plugin> = withContext(Dispatchers.IO) {
        try {
            val id = manifest.id.takeIf { it.isNotBlank() }
                ?: "p" + java.util.UUID.randomUUID().toString().replace("-", "").take(12)
            val dir = File(pluginsDir, id)
            if (dir.exists()) dir.deleteRecursively()
            dir.mkdirs()

            val effectiveManifest = manifest.copy(id = id)
            File(dir, MANIFEST_FILE).writeText(
                gson.toJson(
                    JsonParser.parseString(gson.toJson(effectiveManifest)).asJsonObject.apply {
                        addProperty("kind", kind.name)
                    }
                )
            )
            files.forEach { (rel, content) ->
                val safeRel = rel.removePrefix("/")
                if (safeRel.isBlank() || safeRel.contains("..")) return@forEach
                val f = File(dir, safeRel)
                f.parentFile?.mkdirs()
                f.writeText(content)
            }

            val existing = _plugins.value.firstOrNull { it.id == id }
            val plugin = Plugin.fromManifest(
                manifest = effectiveManifest,
                packageDir = id,
                kind = kind,
                hasPanel = (files[PLUGIN_FILE]?.let(::hasPanelMarkup) ?: false) ||
                    File(dir, PANEL_FILE).exists(),
                hasCss = File(dir, CSS_FILE).exists(),
                builtIn = false
            ).copy(
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )
            _plugins.value = _plugins.value.filter { it.id != plugin.id } + plugin
            refreshBuiltIns()
            writeOverlay()
            rebuildCache()
            Result.success(plugin)
        } catch (e: Exception) {
            AppLogger.e(TAG, "installPackage failed", e)
            Result.failure(e)
        }
    }

    /** Register/update a chrome-extension record (content lives in ext engine). */
    suspend fun upsertChromeRecord(plugin: Plugin) {
        _plugins.value =
            _plugins.value.filter { it.id != plugin.id } + plugin.copy(kind = PluginKind.CHROME_EXTENSION)
        writeOverlay()
        rebuildCache()
    }

    suspend fun removeChromeRecordsFor(extId: String) {
        val before = _plugins.value.size
        _plugins.value = _plugins.value.filter { it.chromeExtId != extId }
        if (_plugins.value.size != before) {
            writeOverlay()
            rebuildCache()
        }
    }

}
