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
 * files/plugin_state.json   list order, enabled ids, chrome-extension records
 * ```
 *
 * CHROME_EXTENSION records live in the state file only; their content stays in
 * the extension engine's own directory (`ExtensionFileManager`).
 *
 * Built-in HCJ packages ship read-only in `assets/plugins/`. A plugin runs
 * where an app attaches it. An app that turns plugins on without its own
 * list uses the `enabled` ids in the state file. That list is empty when
 * the key is absent. Installing a plugin adds its id; built-ins stay off
 * until the user turns them on.
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

    /**
     * Ids turned on in Manage Plugins, in list order. Absent `enabled` in
     * the state file stays empty so an upgrade does not inject every built-in.
     */
    private val _enabledIds = MutableStateFlow<List<String>>(emptyList())
    val enabledIds: StateFlow<List<String>> = _enabledIds.asStateFlow()

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
        val hidden: MutableList<String> = mutableListOf(),
        val enabled: MutableList<String> = mutableListOf()
    )

    private fun readOverlay(): StateOverlay {
        if (!stateFile.exists()) return StateOverlay()
        return try {
            val obj = JsonParser.parseString(stateFile.readText()).asJsonObject
            val overlay = StateOverlay()
            obj.getAsJsonArray("order")?.forEach { overlay.order.add(it.asString) }
            obj.getAsJsonArray("hidden")?.forEach { overlay.hidden.add(it.asString) }
            obj.getAsJsonArray("enabled")?.forEach { overlay.enabled.add(it.asString) }
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
        saveMutex.withLock { writeOverlayLocked() }
    }

    /** Caller holds [saveMutex]. A nested lock would deadlock; this Mutex is not reentrant. */
    private fun writeOverlayLocked() {
        try {
            val overlay = JsonParser.parseString("{}").asJsonObject
            val order = com.google.gson.JsonArray()
            (_plugins.value.map { it.id } + _builtInPlugins.value.map { it.id })
                .forEach { order.add(it) }
            overlay.add("order", order)
            val hidden = com.google.gson.JsonArray()
            hiddenBuiltIns.value.forEach { hidden.add(it) }
            overlay.add("hidden", hidden)
            val enabled = com.google.gson.JsonArray()
            _enabledIds.value.forEach { enabled.add(it) }
            overlay.add("enabled", enabled)
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

    private fun Plugin.withOverride(): Plugin =
        styleOverrides[id]?.let {
            copy(entryStyle = PluginEntryStyle.parse(it.entry), panelStyle = PluginPanelStyle.parse(it.panel))
        } ?: this

    // ------------------------------------------------------------------
    // Loading
    // ------------------------------------------------------------------

    private suspend fun loadPackages() = withContext(Dispatchers.IO) {
        // The same lock covers installPackage, so a load cannot snapshot the
        // directory mid-install and then overwrite the package that just landed.
        saveMutex.withLock {
            try {
                recoverPluginPackageDirs(pluginsDir)
                val overlay = readOverlay()
                val loaded = mutableListOf<Plugin>()

                pluginsDir.listFiles()
                    ?.filter { it.isDirectory && isPluginPackageDirName(it.name) }
                    ?.forEach { dir ->
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

                // An install that updated memory after this snapshot was taken
                // cannot happen while the lock is held. Keep a package whose
                // directory is still on disk anyway, so a listing that missed
                // a just-published id does not drop it.
                val diskIds = loaded.map { it.id }.toSet()
                for (current in _plugins.value) {
                    if (current.id in diskIds || current.kind == PluginKind.CHROME_EXTENSION) continue
                    val dirName = current.packageDir.ifBlank { current.id }
                    if (!isPluginPackageDirName(dirName)) continue
                    val dir = File(pluginsDir, dirName)
                    if (dir.isDirectory && File(dir, MANIFEST_FILE).isFile) loaded.add(current)
                }

                styleOverrides.putAll(overlay.styles)
                loaded.addAll(overlay.chromeRecords)
                _enabledIds.value = overlay.enabled.toList()

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
        _enabledIds.value = overlay.enabled.toList()
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

    fun isGloballyEnabled(id: String): Boolean = id in _enabledIds.value

    /** Enabled ids that still exist, in the order stored in the state file. */
    fun globalEnabledIds(): List<String> {
        val known = allCache.map { it.id }.toSet()
        return _enabledIds.value.map { it.trim() }.filter { it.isNotEmpty() && it in known }.distinct()
    }

    fun effectiveIds(pluginsEnabled: Boolean, attached: List<String>): List<String> =
        resolveAttachedPluginIds(pluginsEnabled, attached, globalEnabledIds())

    suspend fun setEnabled(id: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            _enabledIds.value = updateGlobalEnabledIds(_enabledIds.value, id, enabled)
            writeOverlayLocked()
        }
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
    suspend fun reorder(orderedIds: List<String>, builtIn: Boolean) = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            val flow = if (builtIn) _builtInPlugins else _plugins
            val byId = flow.value.associateBy { it.id }
            flow.value = orderedIds.mapNotNull { byId[it] } +
                flow.value.filter { it.id !in orderedIds }
            writeOverlayLocked()
            rebuildCache()
        }
    }

    /** Persist a per-plugin host-style override (entry + panel). */
    suspend fun setPluginStyle(id: String, entry: PluginEntryStyle, panel: PluginPanelStyle) = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            styleOverrides[id] = StyleOverride(entry.name, panel.name)
            _plugins.value = _plugins.value.map { if (it.id == id) it.withOverride() else it }
            _builtInPlugins.value = _builtInPlugins.value.map { if (it.id == id) it.withOverride() else it }
            writeOverlayLocked()
            rebuildCache()
        }
    }

    suspend fun removePlugin(id: String): Boolean = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            val plugin = _plugins.value.find { it.id == id } ?: return@withLock false
            if (plugin.kind != PluginKind.CHROME_EXTENSION) {
                File(pluginsDir, plugin.packageDir).deleteRecursively()
            }
            _plugins.value = _plugins.value.filter { it.id != id }
            _enabledIds.value = updateGlobalEnabledIds(_enabledIds.value, id, enabled = false)
            PluginConfigStore(context).clear(id)
            refreshBuiltIns()
            writeOverlayLocked()
            rebuildCache()
            true
        }
    }

    /** Hide a built-in plugin (row ⋮ → Delete); restore from the screen ⋮ menu.
     *  The id stays in the enabled set so un-hiding leaves it on. */
    suspend fun hideBuiltIn(id: String) = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            _hiddenBuiltIns.value = _hiddenBuiltIns.value + id
            refreshBuiltIns()
            writeOverlayLocked()
            rebuildCache()
        }
    }

    /** Bring back every hidden built-in. */
    suspend fun restoreBuiltIns() = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            if (_hiddenBuiltIns.value.isEmpty()) return@withLock
            _hiddenBuiltIns.value = emptySet()
            // Write first so loadBuiltIns, which re-reads the state file, does
            // not put the old hidden set back.
            writeOverlayLocked()
            loadBuiltIns()
            rebuildCache()
        }
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
        saveMutex.withLock {
            try {
                val id = manifest.id.takeIf { it.isNotBlank() }
                    ?: "p" + java.util.UUID.randomUUID().toString().replace("-", "").take(12)
                if (!isPluginPackageDirName(id)) {
                    return@withLock Result.failure(
                        IllegalArgumentException("plugin id cannot use a staging suffix: $id")
                    )
                }
                val effectiveManifest = manifest.copy(id = id)
                replacePluginPackageDir(pluginsDir, id) { dir ->
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
                }

                val live = File(pluginsDir, id)
                val existing = _plugins.value.firstOrNull { it.id == id }
                val plugin = Plugin.fromManifest(
                    manifest = effectiveManifest,
                    packageDir = id,
                    kind = kind,
                    hasPanel = (files[PLUGIN_FILE]?.let(::hasPanelMarkup) ?: false) ||
                        File(live, PANEL_FILE).exists(),
                    hasCss = File(live, CSS_FILE).exists(),
                    builtIn = false
                ).copy(
                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                )
                _plugins.value = _plugins.value.filter { it.id != plugin.id } + plugin
                _enabledIds.value = updateGlobalEnabledIds(_enabledIds.value, plugin.id, enabled = true)
                refreshBuiltIns()
                writeOverlayLocked()
                rebuildCache()
                Result.success(plugin)
            } catch (e: Exception) {
                AppLogger.e(TAG, "installPackage failed", e)
                Result.failure(e)
            }
        }
    }

    /** Register/update a chrome-extension record (content lives in ext engine). */
    suspend fun upsertChromeRecord(plugin: Plugin) = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            val isNew = _plugins.value.none { it.id == plugin.id }
            _plugins.value =
                _plugins.value.filter { it.id != plugin.id } + plugin.copy(kind = PluginKind.CHROME_EXTENSION)
            if (isNew) {
                _enabledIds.value = updateGlobalEnabledIds(_enabledIds.value, plugin.id, enabled = true)
            }
            writeOverlayLocked()
            rebuildCache()
        }
    }

    suspend fun removeChromeRecordsFor(extId: String) = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            val removed = _plugins.value.filter { it.chromeExtId == extId }.map { it.id }.toSet()
            if (removed.isEmpty()) return@withLock
            _plugins.value = _plugins.value.filter { it.id !in removed }
            _enabledIds.value = _enabledIds.value.filterNot { it in removed }
            writeOverlayLocked()
            rebuildCache()
        }
    }

}

internal const val PLUGIN_PACKAGE_TMP_SUFFIX = ".tmp"
internal const val PLUGIN_PACKAGE_BAK_SUFFIX = ".bak"

internal fun isPluginPackageDirName(name: String): Boolean =
    name.isNotBlank() &&
        name != "." &&
        name != ".." &&
        !name.endsWith(PLUGIN_PACKAGE_TMP_SUFFIX) &&
        !name.endsWith(PLUGIN_PACKAGE_BAK_SUFFIX)

/**
 * A crashed install leaves `<id>.tmp` or `<id>.bak`. Drop the staging dir.
 * If only the backup remains, move it back. If the live dir also exists, the
 * install finished and the backup is leftover.
 */
internal fun recoverPluginPackageDirs(pluginsDir: File) {
    if (!pluginsDir.isDirectory) return
    val dirs = pluginsDir.listFiles()?.filter { it.isDirectory }.orEmpty()
    for (dir in dirs) {
        val name = dir.name
        if (name.endsWith(PLUGIN_PACKAGE_TMP_SUFFIX)) {
            dir.deleteRecursively()
            continue
        }
        if (!name.endsWith(PLUGIN_PACKAGE_BAK_SUFFIX)) continue
        val id = name.removeSuffix(PLUGIN_PACKAGE_BAK_SUFFIX)
        if (!isPluginPackageDirName(id)) continue
        val live = File(pluginsDir, id)
        if (live.exists()) {
            dir.deleteRecursively()
        } else if (!dir.renameTo(live)) {
            AppLogger.w("PluginStore", "could not restore plugin package $id from backup")
        }
    }
}

/**
 * Write [write] into `<id>.tmp`, then swap it over the live directory.
 * The live tree is never deleted before the new tree is complete, and a
 * failed publish puts the previous tree back.
 */
internal fun replacePluginPackageDir(pluginsDir: File, id: String, write: (File) -> Unit) {
    check(isPluginPackageDirName(id)) { "plugin id cannot use a staging suffix: $id" }
    pluginsDir.mkdirs()
    val staging = File(pluginsDir, id + PLUGIN_PACKAGE_TMP_SUFFIX)
    if (staging.exists()) staging.deleteRecursively()
    staging.mkdirs()
    val live = File(pluginsDir, id)
    val bak = File(pluginsDir, id + PLUGIN_PACKAGE_BAK_SUFFIX)
    try {
        write(staging)
        if (bak.exists()) bak.deleteRecursively()
        if (live.exists() && !live.renameTo(bak)) {
            throw java.io.IOException("could not move existing plugin package aside: $id")
        }
        if (!staging.renameTo(live)) {
            if (!live.exists() && bak.exists()) bak.renameTo(live)
            throw java.io.IOException("could not publish plugin package: $id")
        }
        if (bak.exists()) bak.deleteRecursively()
    } catch (e: Exception) {
        if (staging.exists()) staging.deleteRecursively()
        if (!live.exists() && bak.exists()) bak.renameTo(live)
        throw e
    }
}
