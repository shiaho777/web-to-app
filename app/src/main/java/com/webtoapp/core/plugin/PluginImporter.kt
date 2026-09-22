package com.webtoapp.core.plugin

import android.content.Context
import android.net.Uri
import com.google.gson.JsonParser
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Imports plugins from external sources into the package layout:
 *
 *  - `.user.js` / `.js` — Greasemonkey scripts. The `==UserScript==` metadata
 *    block becomes `plugin.json`; the full source (metadata included, it is
 *    just comments) becomes the `hcj/page` block of `plugin.html`.
 *  - `.hcj` — a zip of one plugin directory (`plugin.json` + files). Exported
 *    via [exportHcj].
 *  - `.zip` — same handling; a top-level `plugin.json` or a single root folder
 *    containing it are both accepted.
 *
 * No manifest block is required for plain `.js`: it imports as a plugin that
 * runs on all sites at document_end (same rule Tampermonkey applies).
 */
class PluginImporter(private val context: Context) {

    companion object {
        private const val TAG = "PluginImporter"
        private val METADATA_BLOCK_REGEX = Regex(
            """//\s*==UserScript==\s*\n(.*?)//\s*==/UserScript==""",
            RegexOption.DOT_MATCHES_ALL
        )
        private val METADATA_LINE_REGEX = Regex("""//\s*@(\S+)\s+(.+)""")
        private const val MAX_PACKAGE_BYTES = 8 * 1024 * 1024L
    }

    private val store get() = PluginStore.getInstance(context)

    sealed class ImportResult {
        data class Success(val plugin: Plugin) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    suspend fun importUserScript(content: String, fileName: String = ""): ImportResult {
        val manifest = manifestFromUserScript(content, fileName)
        return install(
            manifest, PluginKind.USERSCRIPT,
            mapOf(PluginStore.PLUGIN_FILE to buildPluginHtml(content, ""))
        )
    }

    suspend fun importUserScript(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val content = try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } catch (e: Exception) {
            null
        } ?: return@withContext ImportResult.Error("cannot read file")
        val name = fileNameFor(uri) ?: "script.user.js"
        importUserScript(content, name)
    }

    suspend fun importHcj(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val files = try {
            unzipPackage(uri)
        } catch (e: Exception) {
            AppLogger.e(TAG, "unzip failed", e)
            return@withContext ImportResult.Error("not a valid .hcj package")
        } ?: return@withContext ImportResult.Error("plugin.json not found in package")

        val manifestRaw = files[PluginStore.MANIFEST_FILE]
            ?: return@withContext ImportResult.Error("plugin.json not found in package")
        val manifest = PluginManifest.fromJson(manifestRaw)
            ?: return@withContext ImportResult.Error("plugin.json is malformed")

        val kind = try {
            JsonParser.parseString(manifestRaw).asJsonObject.get("kind")?.asString
                ?.let { PluginKind.parse(it) } ?: PluginKind.HCJ
        } catch (e: Exception) {
            PluginKind.HCJ
        }

        install(manifest, kind, files - PluginStore.MANIFEST_FILE)
    }

    private suspend fun install(
        manifest: PluginManifest,
        kind: PluginKind,
        files: Map<String, String>
    ): ImportResult {
        val result = store.installPackage(manifest, kind, files)
        return result.fold(
            onSuccess = { ImportResult.Success(it) },
            onFailure = { ImportResult.Error(it.message ?: "install failed") }
        )
    }

    /**
     * Registers the content-script records of a parsed Chrome extension.
     * The unpacked extension directory itself is managed by
     * `ExtensionFileManager`; here we only mirror its modules into the
     * unified plugin list.
     */
    suspend fun installChromeRecords(
        modules: List<com.webtoapp.core.extension.ExtensionModule>
    ): ImportResult {
        val gson = com.google.gson.Gson()
        var installed = 0
        var last: Plugin? = null
        for (module in modules) {
            val obj = runCatching { gson.toJsonTree(module).asJsonObject }.getOrNull()
                ?: continue
            val plugin = PluginMigrator.chromePluginFrom(obj) ?: continue
            store.upsertChromeRecord(plugin)
            installed++
            last = plugin
        }
        return when {
            installed > 0 -> ImportResult.Success(last!!)
            else -> ImportResult.Error("no installable content scripts")
        }
    }

    /**
     * One-line bridge for producers that still emit [ExtensionModule] records
     * (module market, GreasyFork installs, agent tools). The record is
     * converted to a plugin package using the same mapping as the migrator.
     */
    suspend fun installLegacyModule(
        module: com.webtoapp.core.extension.ExtensionModule
    ): ImportResult {
        val gson = com.google.gson.Gson()
        val m = runCatching { gson.toJsonTree(module).asJsonObject }.getOrNull()
            ?: return ImportResult.Error("cannot serialize module")
        val id = module.id.ifBlank {
            "p" + java.util.UUID.randomUUID().toString().replace("-", "").take(12)
        }
        val kind = when (module.sourceType) {
            com.webtoapp.core.extension.ModuleSourceType.USERSCRIPT,
            com.webtoapp.core.extension.ModuleSourceType.GREASYFORK -> PluginKind.USERSCRIPT
            else -> PluginKind.HCJ
        }
        val manifestMap = PluginMigrator.manifestMapFrom(m, id, kind)
        val manifest = PluginManifest.fromJson(gson.toJson(manifestMap))
            ?: return ImportResult.Error("manifest conversion failed")
        PluginMigrator.seedConfig(context, id, m)
        return install(manifest, kind, PluginMigrator.filesFrom(m))
    }

    // ------------------------------------------------------------------
    // userscript metadata -> plugin.json
    // ------------------------------------------------------------------

    data class ParsedMeta(val manifest: PluginManifest, val warnings: List<String>)

    fun manifestFromUserScript(content: String, fileName: String = ""): PluginManifest =
        parseMeta(content, fileName).manifest

    fun parseMeta(content: String, fileName: String = ""): ParsedMeta {
        val block = METADATA_BLOCK_REGEX.find(content)?.groupValues?.get(1)
            ?: return ParsedMeta(
                PluginManifest(
                    name = fileName.removeSuffix(".user.js").removeSuffix(".js")
                        .ifBlank { "Unnamed Script" },
                    matches = listOf("*"),
                    runAt = "document_end",
                    toolbar = true
                ),
                warnings = listOf("no ==UserScript== block; imported as plain JS")
            )

        var name = ""
        var description = ""
        var version = "1.0.0"
        var author = ""
        var homepage = ""
        var icon = ""
        var runAt = "document-idle"
        var noframes = false
        val matches = mutableListOf<String>()
        val includes = mutableListOf<String>()
        val excludes = mutableListOf<String>()
        val grants = mutableListOf<String>()
        val requires = mutableListOf<String>()
        val resources = linkedMapOf<String, String>()

        METADATA_LINE_REGEX.findAll(block).forEach { m ->
            val key = m.groupValues[1].trim()
            val value = m.groupValues[2].trim()
            when (key) {
                "name" -> name = value
                "description", "desc" -> description = value
                "version" -> version = value
                "author" -> author = value
                "homepage", "homepageURL", "website" -> homepage = value
                "icon", "iconURL", "icon64", "icon64URL" -> icon = value
                "match" -> matches.add(value)
                "include" -> includes.add(value)
                "exclude", "exclude-match" -> excludes.add(value)
                "run-at" -> runAt = value
                "grant" -> grants.add(value)
                "require" -> requires.add(value)
                // @resource <name> <url>
                "resource" -> {
                    val sp = value.indexOf(' ').takeIf { it > 0 }
                        ?: value.indexOf('\t').takeIf { it > 0 }
                    if (sp != null) {
                        resources[value.substring(0, sp).trim()] = value.substring(sp + 1).trim()
                    }
                }
                "noframes" -> noframes = true
            }
        }

        val matchStrings = (matches.map { it.replace("<all_urls>", "*") } + includes)
            .ifEmpty { listOf("*") }

        val permissions = linkedSetOf("STORAGE")
        grants.forEach { g ->
            when {
                g.startsWith("GM_xmlhttpRequest") || g == "GM.xmlHttpRequest" -> permissions.add("FETCH")
                g.startsWith("GM_notification") || g == "GM.notification" -> permissions.add("NOTIFY")
                g.startsWith("GM_setClipboard") || g == "GM.setClipboard" -> permissions.add("CLIPBOARD")
                g.startsWith("GM_download") || g == "GM.download" -> permissions.add("DOWNLOAD")
            }
        }

        return ParsedMeta(
            PluginManifest(
                name = name.ifBlank {
                    fileName.removeSuffix(".user.js").removeSuffix(".js").ifBlank { "Unnamed Script" }
                },
                version = version,
                description = description,
                author = author,
                homepage = homepage,
                icon = icon.ifBlank { "code" },
                matches = matchStrings,
                excludeMatches = excludes,
                runAt = when (runAt) {
                    "document-start" -> "document_start"
                    "document-end", "document-body" -> "document_end"
                    else -> "document_idle"
                },
                permissions = permissions.toList(),
                toolbar = true,
                gmGrants = grants.filter { it != "none" && it.isNotBlank() },
                requireUrls = requires,
                resources = resources,
                noframes = noframes
            ),
            warnings = emptyList()
        )
    }

    fun isUserScript(content: String): Boolean =
        METADATA_BLOCK_REGEX.containsMatchIn(content)

    // ------------------------------------------------------------------
    // .hcj zip
    // ------------------------------------------------------------------

    private fun unzipPackage(uri: Uri): Map<String, String>? {
        val files = linkedMapOf<String, String>()
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input.buffered()).use { zis ->
                var total = 0L
                var entry: ZipEntry? = zis.nextEntry
                var rootPrefix: String? = null
                while (entry != null) {
                    val e = entry
                    if (!e.isDirectory) {
                        var name = e.name
                        total += e.size.coerceAtLeast(0)
                        if (total > MAX_PACKAGE_BYTES) throw IllegalStateException("package too large")
                        // Detect a single wrapping root folder on the fly.
                        val body = zis.readBytes()
                        if (name.endsWith("/" + PluginStore.MANIFEST_FILE) || name == PluginStore.MANIFEST_FILE) {
                            if (rootPrefix == null && name != PluginStore.MANIFEST_FILE) {
                                rootPrefix = name.removeSuffix(PluginStore.MANIFEST_FILE)
                            }
                            if (rootPrefix != null && name.startsWith(rootPrefix!!)) {
                                name = name.removePrefix(rootPrefix!!)
                            }
                        } else if (rootPrefix != null && name.startsWith(rootPrefix!!)) {
                            name = name.removePrefix(rootPrefix!!)
                        }
                        files[name] = body.toString(Charsets.UTF_8)
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }
        return if (files.containsKey(PluginStore.MANIFEST_FILE)) files else null
    }

    // ------------------------------------------------------------------

    suspend fun exportHcj(plugin: Plugin): File? = withContext(Dispatchers.IO) {
        val out = File(context.cacheDir, "${plugin.name.replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fa5]"), "_")}.hcj")
        try {
            java.util.zip.ZipOutputStream(out.outputStream().buffered()).use { zos ->
                if (plugin.builtIn) {
                    writeAssetDir(zos, plugin.packageDir, "")
                } else {
                    val dir = File(File(context.filesDir, PluginStore.PLUGINS_DIR), plugin.packageDir)
                    if (!dir.isDirectory) return@withContext null
                    dir.walkTopDown().filter { it.isFile }.forEach { f ->
                        zos.putNextEntry(ZipEntry(f.relativeTo(dir).path))
                        f.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            out
        } catch (e: Exception) {
            AppLogger.e(TAG, "exportHcj failed", e)
            null
        }
    }

    /** Recursively zip an assets directory ([packageDir] like "plugins/dark-mode"). */
    private fun writeAssetDir(zos: java.util.zip.ZipOutputStream, path: String, prefix: String) {
        val children = context.assets.list(path).orEmpty()
        if (children.isEmpty()) {
            zos.putNextEntry(ZipEntry(prefix))
            context.assets.open(path).use { it.copyTo(zos) }
            zos.closeEntry()
            return
        }
        children.forEach { writeAssetDir(zos, "$path/$it", "$prefix$it") }
    }

    internal fun fileNameFor(uri: Uri): String? {
        val cursor = runCatching {
            context.contentResolver.query(uri, null, null, null, null)
        }.getOrNull() ?: return null
        return cursor.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) it.getString(idx) else null
            } else null
        }
    }
}
