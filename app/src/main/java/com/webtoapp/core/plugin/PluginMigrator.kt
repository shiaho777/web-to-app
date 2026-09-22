package com.webtoapp.core.plugin

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.util.GsonProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * One-shot migration from the retired `extension_modules/` storage into the
 * plugin package layout. Deliberately parses raw JSON instead of the old
 * `ExtensionModule` model so it keeps working after that class is deleted.
 *
 * Mapping:
 *  - CUSTOM            -> HCJ package (code -> main.js, cssCode -> style.css,
 *                         panelHtml -> panel.html, codeFiles -> files/, ids kept
 *                         so per-app `extensionModuleIds` attachments survive)
 *  - USERSCRIPT/GREASYFORK -> USERSCRIPT package (full script -> main.js,
 *                         grants/requires/resources recorded in plugin.json)
 *  - CHROME_EXTENSION  -> state-file chrome record (content untouched)
 *  - builtin_states.json -> same ids in the new state overlay
 *
 * The old directory is renamed to `extension_modules.migrated/` afterwards —
 * never deleted automatically.
 */
class PluginMigrator(private val context: Context) {

    companion object {
        private const val TAG = "PluginMigrator"
        private const val OLD_DIR = "extension_modules"
        private const val MIGRATED_DIR = "extension_modules.migrated"
        private const val MODULES_FILE = "modules.json"
        private const val BUILTIN_STATES_FILE = "builtin_states.json"
        private val gson get() = GsonProvider.gson

        /** Shared by migration and by fresh Chrome-extension installs. */
        fun chromePluginFrom(m: JsonObject): Plugin? {
            val plugin = Plugin(
                id = m.str("id"),
                kind = PluginKind.CHROME_EXTENSION,
                name = m.str("name"),
                description = m.str("description"),
                icon = m.str("icon").ifBlank { "extension" },
                versionName = m.obj("version")?.str("name") ?: "1.0.0",
                authorName = m.obj("author")?.str("name") ?: "",
                chromeExtId = m.str("chromeExtId"),
                showInToolbar = true,
                createdAt = m.long("createdAt"),
                updatedAt = m.long("updatedAt")
            )
            return plugin.takeIf { it.chromeExtId.isNotBlank() }
        }

        private fun JsonObject.str(key: String): String =
            get(key)?.takeIf { it.isJsonPrimitive }?.asString.orEmpty()

        private fun JsonObject.bool(key: String, def: Boolean): Boolean =
            get(key)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isBoolean }
                ?.asBoolean ?: def

        private fun JsonObject.long(key: String): Long =
            get(key)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }
                ?.asLong ?: 0L

        private fun JsonObject.obj(key: String): JsonObject? =
            get(key)?.takeIf { it.isJsonObject }?.asJsonObject

        private fun JsonObject.arr(key: String): com.google.gson.JsonArray? =
            get(key)?.takeIf { it.isJsonArray }?.asJsonArray

        /** Map old capability names onto the small HCJ permission set. */
        fun translatePermissions(m: JsonObject): List<String> {
            val out = linkedSetOf<String>()
            m.arr("permissions")?.forEach { p ->
                when (p.asString) {
                    "STORAGE" -> out.add("STORAGE")
                    "NETWORK", "FETCH_INTERCEPT", "WEBSOCKET" -> out.add("FETCH")
                    "NOTIFICATION" -> out.add("NOTIFY")
                    "CLIPBOARD" -> out.add("CLIPBOARD")
                    "DOWNLOAD" -> out.add("DOWNLOAD")
                }
            }
            // Migrated scripts had DOM access implicitly; storage was the
            // common need — err on enabling STORAGE so hcj.config keeps working.
            out.add("STORAGE")
            return out.toList()
        }

        /**
         * Build the plugin.json field map for a legacy module object. Shared by
         * the on-disk migrator and by fresh installs (market / agent / GreasyFork)
         * that still arrive as ExtensionModule records.
         */
        fun manifestMapFrom(
            m: JsonObject,
            id: String,
            kind: PluginKind
        ): LinkedHashMap<String, Any?> {
            val matches = mutableListOf<String>()
            val excludes = mutableListOf<String>()
            m.arr("urlMatches")?.forEach { r ->
                val rule = r.asJsonObject
                val pattern = rule.str("pattern")
                if (pattern.isBlank()) return@forEach
                val text = if (rule.bool("isRegex", false)) "/$pattern/" else pattern
                if (rule.bool("exclude", false)) excludes.add(text) else matches.add(text)
            }

            val manifest = linkedMapOf<String, Any?>(
                "id" to id,
                "kind" to kind.name,
                "name" to m.str("name").ifBlank { id },
                "description" to m.str("description"),
                "version" to (m.obj("version")?.str("name") ?: "1.0.0"),
                "author" to (m.obj("author")?.str("name") ?: ""),
                "icon" to m.str("icon").ifBlank { "extension" },
                "matches" to matches.ifEmpty { listOf("*") },
                "excludeMatches" to excludes,
                "runAt" to m.str("runAt").lowercase().let {
                    when (it) {
                        "document_start" -> "document_start"
                        "document_idle" -> "document_idle"
                        else -> "document_end"
                    }
                },
                "permissions" to translatePermissions(m),
                "toolbar" to true
            )
            if (kind == PluginKind.USERSCRIPT) {
                m.arr("gmGrants")?.let { grants ->
                    manifest["gmGrants"] = grants.mapNotNull { it.asString }
                }
                m.arr("requireUrls")?.let { urls ->
                    manifest["requireUrls"] = urls.mapNotNull { it.asString }
                }
                m.obj("resources")?.let { res ->
                    manifest["resources"] = res.entrySet()
                        .mapNotNull { (k, v) -> v.takeIf { it.isJsonPrimitive }?.asString?.let { k to it } }
                        .toMap()
                }
                manifest["noframes"] = m.bool("noframes", false)
            }
            if (kind == PluginKind.HCJ) {
                // Retired CUSTOM modules speak the old globals; the injector
                // prelude maps them onto hcj.*. Userscripts never had them.
                manifest["legacyCompat"] = true
            }
            return manifest
        }

        /** Package-relative files for a legacy module (inline fields only). */
        fun filesFrom(m: JsonObject): Map<String, String> {
            val files = linkedMapOf<String, String>()
            val code = m.str("code")
            val css = m.str("cssCode")
            val panel = m.str("panelHtml")
            // Canonical single file: page JS + panel document live together in
            // plugin.html; style.css stays a separate file the loader injects.
            val pluginHtml = buildPluginHtml(code, panel)
            if (pluginHtml.isNotBlank()) files[PluginStore.PLUGIN_FILE] = pluginHtml
            if (css.isNotBlank()) files[PluginStore.CSS_FILE] = css
            m.obj("codeFiles")?.entrySet()?.forEach { (rel, content) ->
                if (rel.contains("..")) return@forEach
                files["files/$rel"] = content.asString
            }
            return files
        }

        /** Seed hcj.config from a legacy module's configValues. */
        fun seedConfig(context: Context, id: String, m: JsonObject) {
            m.obj("configValues")?.let { cfg ->
                if (cfg.entrySet().isNotEmpty()) {
                    val store = PluginConfigStore(context)
                    cfg.entrySet().forEach { (k, v) ->
                        store.set(id, k, gson.toJson(v.asString))
                    }
                }
            }
        }
    }

    private val oldDir get() = File(context.filesDir, OLD_DIR)
    private val migratedDir get() = File(context.filesDir, MIGRATED_DIR)
    private val pluginsDir get() = File(context.filesDir, PluginStore.PLUGINS_DIR)

    suspend fun migrateIfNeeded() = withContext(Dispatchers.IO) {
        try {
            if (!oldDir.exists()) return@withContext
            val modulesFile = File(oldDir, MODULES_FILE)
            val builtinStatesFile = File(oldDir, BUILTIN_STATES_FILE)
            if (!modulesFile.exists() && !builtinStatesFile.exists()) {
                renameOld()
                return@withContext
            }

            AppLogger.i(TAG, "migrating legacy extension_modules -> plugins")
            val overlay = readOverlayStates()

            if (modulesFile.exists()) {
                migrateModules(modulesFile, overlay)
            }

            writeStateFile(overlay)
            renameOld()
            AppLogger.i(TAG, "migration complete")
        } catch (e: Exception) {
            AppLogger.e(TAG, "plugin migration failed", e)
        }
    }

    // ------------------------------------------------------------------

    private fun migrateModules(modulesFile: File, overlay: Overlay) {
        val array = try {
            JsonParser.parseString(modulesFile.readText()).asJsonArray
        } catch (e: Exception) {
            AppLogger.e(TAG, "cannot parse modules.json", e)
            return
        }
        for (el in array) {
            try {
                val m = el.asJsonObject
                val id = m.str("id").ifBlank { continue }
                val sourceType = m.str("sourceType").ifBlank { "CUSTOM" }
                when (sourceType) {
                    "CHROME_EXTENSION" -> migrateChromeRecord(m, overlay)
                    "USERSCRIPT", "GREASYFORK" -> migrateScriptPackage(m, id, PluginKind.USERSCRIPT, overlay)
                    else -> migrateScriptPackage(m, id, PluginKind.HCJ, overlay)
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "skipping module during migration: ${e.message}")
            }
        }
    }

    private fun migrateChromeRecord(m: JsonObject, overlay: Overlay) {
        val plugin = chromePluginFrom(m) ?: return
        overlay.chromeRecords.add(plugin)
    }

    private fun migrateScriptPackage(m: JsonObject, id: String, kind: PluginKind, overlay: Overlay) {
        val dir = File(pluginsDir, id)
        if (dir.exists()) dir.deleteRecursively()
        dir.mkdirs()

        // Code payloads were migrated to sidecar files by the old manager; fall
        // back to inline fields for very old installs.
        val code = sidecar("code_$id.js").ifBlank { m.str("code") }
        val css = sidecar("css_$id.css").ifBlank { m.str("cssCode") }
        val panel = m.str("panelHtml")

        val pluginHtml = buildPluginHtml(code, panel)
        if (pluginHtml.isNotBlank()) File(dir, PluginStore.PLUGIN_FILE).writeText(pluginHtml)
        if (css.isNotBlank()) File(dir, PluginStore.CSS_FILE).writeText(css)

        // Multi-file modules keep their relative paths under files/.
        val codeFilesDir = File(oldDir, "codefiles_$id")
        if (codeFilesDir.isDirectory) {
            codeFilesDir.walkTopDown().filter { it.isFile }.forEach { f ->
                val rel = f.relativeTo(codeFilesDir).path
                val target = File(dir, "files/$rel")
                target.parentFile?.mkdirs()
                f.copyTo(target, overwrite = true)
            }
        }
        filesFrom(m).forEach { (rel, content) ->
            if (rel == PluginStore.PLUGIN_FILE || rel == PluginStore.CSS_FILE) {
                // Inline fallbacks only — sidecar content above wins.
                if (File(dir, rel).exists()) return@forEach
            }
            val target = File(dir, rel)
            target.parentFile?.mkdirs()
            target.writeText(content)
        }

        File(dir, PluginStore.MANIFEST_FILE).writeText(gson.toJson(manifestMapFrom(m, id, kind)))
        seedConfig(context, id, m)

        overlay.order.add(id)
    }

    private fun sidecar(name: String): String = try {
        File(oldDir, name).takeIf { it.exists() }?.readText().orEmpty()
    } catch (e: Exception) {
        ""
    }

    // ------------------------------------------------------------------
    // State overlay I/O (mirrors PluginStore's file shape)
    // ------------------------------------------------------------------

    private class Overlay {
        val order = mutableListOf<String>()
        val chromeRecords = mutableListOf<Plugin>()
    }

    private fun readOverlayStates(): Overlay {
        val overlay = Overlay()
        val stateFile = File(context.filesDir, PluginStore.STATE_FILE)
        if (!stateFile.exists()) return overlay
        try {
            val obj = JsonParser.parseString(stateFile.readText()).asJsonObject
            obj.getAsJsonArray("order")?.forEach { overlay.order.add(it.asString) }
            obj.getAsJsonArray("chromeRecords")?.forEach { el ->
                try {
                    gson.fromJson(el, Plugin::class.java)?.let {
                        overlay.chromeRecords.add(it.copy(kind = PluginKind.CHROME_EXTENSION))
                    }
                } catch (_: Exception) {
                }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "existing plugin_state.json unreadable, starting fresh")
        }
        return overlay
    }

    private fun writeStateFile(overlay: Overlay) {
        val root = com.google.gson.JsonObject()
        val order = com.google.gson.JsonArray()
        overlay.order.forEach { order.add(it) }
        root.add("order", order)
        val chrome = com.google.gson.JsonArray()
        overlay.chromeRecords.forEach { chrome.add(gson.toJsonTree(it)) }
        root.add("chromeRecords", chrome)
        File(context.filesDir, PluginStore.STATE_FILE).writeText(gson.toJson(root))
    }

    private fun renameOld() {
        try {
            if (migratedDir.exists()) migratedDir.deleteRecursively()
            oldDir.renameTo(migratedDir)
        } catch (e: Exception) {
            AppLogger.w(TAG, "could not rename legacy dir: ${e.message}")
        }
    }

    // -- raw-json helpers -----------------------------------------------------

    private fun JsonObject.str(key: String): String =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString ?: ""

    private fun JsonObject.bool(key: String, def: Boolean): Boolean =
        get(key)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isBoolean }?.asBoolean ?: def

    private fun JsonObject.long(key: String): Long =
        get(key)?.takeIf { it.isJsonPrimitive }?.asLong ?: 0L

    private fun JsonObject.obj(key: String): JsonObject? =
        get(key)?.takeIf { it.isJsonObject }?.asJsonObject

    private fun JsonObject.arr(key: String): com.google.gson.JsonArray? =
        get(key)?.takeIf { it.isJsonArray }?.asJsonArray
}
