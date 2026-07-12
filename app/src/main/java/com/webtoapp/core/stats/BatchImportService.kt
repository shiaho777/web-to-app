package com.webtoapp.core.stats

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.repository.WebAppRepository
import java.io.InputStream
import java.net.URI
import java.util.Locale

class BatchImportService(
    private val context: Context,
    private val repository: WebAppRepository
) {
    companion object {
        private const val TAG = "BatchImportService"
        private val MARKDOWN_LINK = Regex("""\[([^\]]*)\]\((https?://[^)\s]+)\)""", RegexOption.IGNORE_CASE)
        private val HREF_DOUBLE = Regex("""<A\s+[^>]*HREF\s*=\s*"([^"]+)"[^>]*>(.*?)</A>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        private val HREF_SINGLE = Regex("""<A\s+[^>]*HREF\s*=\s*'([^']+)'[^>]*>(.*?)</A>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        private val BARE_URL = Regex("""https?://[^\s<>"'`]+""", RegexOption.IGNORE_CASE)
        private val DOMAIN = Regex("""^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}(?::\d{1,5})?(?:/[^\s]*)?$""")
    }

    private val gson: Gson by lazy { GsonBuilder().setPrettyPrinting().create() }

    data class ParsedEntry(
        val name: String,
        val url: String
    )

    data class ParseResult(
        val entries: List<ParsedEntry>,
        val invalidLineCount: Int = 0,
        val duplicateInInputCount: Int = 0
    )

    data class ImportResult(
        val imported: Int,
        val skippedDuplicate: Int = 0,
        val skippedInvalid: Int = 0
    )

    fun parseFromText(text: String): List<ParsedEntry> = parseTextDetailed(text).entries

    fun parseTextDetailed(text: String): ParseResult {
        if (text.isBlank()) return ParseResult(emptyList())

        val seen = linkedSetOf<String>()
        val entries = mutableListOf<ParsedEntry>()
        var invalid = 0
        var dupInInput = 0

        fun accept(name: String, rawUrl: String) {
            val normalized = normalizeUrl(rawUrl) ?: run {
                invalid++
                return
            }
            val key = normalized.lowercase(Locale.US)
            if (!seen.add(key)) {
                dupInInput++
                return
            }
            val resolvedName = name.trim().ifBlank { extractName(normalized) }
            entries += ParsedEntry(resolvedName, normalized)
        }

        text.lines().forEach { rawLine ->
            val line = rawLine.trim()
                .removePrefix("\uFEFF")
                .trim()
            if (line.isBlank()) return@forEach
            if (line.startsWith("#") || line.startsWith("//") || line.startsWith(";")) return@forEach

            val md = MARKDOWN_LINK.find(line)
            if (md != null) {
                accept(md.groupValues[1], md.groupValues[2])
                return@forEach
            }

            when {
                line.contains("|") -> {
                    val parts = line.split("|", limit = 2)
                    val name = parts[0].trim().trim('"', '\'')
                    val url = parts.getOrNull(1)?.trim().orEmpty()
                    if (url.isNotBlank()) accept(name, url) else invalid++
                }
                line.contains('\t') -> {
                    val parts = line.split('\t').map { it.trim() }.filter { it.isNotEmpty() }
                    when {
                        parts.size >= 2 && looksLikeUrl(parts.last()) ->
                            accept(parts.dropLast(1).joinToString(" "), parts.last())
                        parts.size == 1 && looksLikeUrl(parts[0]) ->
                            accept("", parts[0])
                        else -> invalid++
                    }
                }
                line.contains(",") && !line.startsWith("http", ignoreCase = true) -> {
                    val parts = line.split(",").map { it.trim().trim('"') }.filter { it.isNotEmpty() }
                    when {
                        parts.size >= 2 && looksLikeUrl(parts.last()) ->
                            accept(parts.dropLast(1).joinToString(", "), parts.last())
                        parts.size == 1 && looksLikeUrl(parts[0]) ->
                            accept("", parts[0])
                        else -> {
                            val bare = BARE_URL.find(line)?.value
                            if (bare != null) accept(line.replace(bare, "").trim(' ', '-', ':', '|'), bare)
                            else invalid++
                        }
                    }
                }
                looksLikeUrl(line) -> accept("", line)
                line.contains(" ") -> {
                    val lastSpace = line.lastIndexOf(' ')
                    val possibleUrl = line.substring(lastSpace + 1).trim().trimEnd(',', ';', '.')
                    if (looksLikeUrl(possibleUrl)) {
                        accept(line.substring(0, lastSpace).trim(), possibleUrl)
                    } else {
                        val bare = BARE_URL.find(line)?.value
                        if (bare != null) accept(line.replace(bare, "").trim(), bare) else invalid++
                    }
                }
                else -> {
                    val bare = BARE_URL.find(line)?.value
                    if (bare != null) accept("", bare) else invalid++
                }
            }
        }

        return ParseResult(
            entries = entries,
            invalidLineCount = invalid,
            duplicateInInputCount = dupInInput
        )
    }

    fun parseFromBookmarksHtml(input: InputStream): List<ParsedEntry> {
        return try {
            val html = input.bufferedReader().use { it.readText() }
            parseBookmarksHtml(html)
        } catch (e: Exception) {
            AppLogger.e(TAG, "解析书签文件失败: ${e.message}")
            emptyList()
        }
    }

    fun parseBookmarksHtml(html: String): List<ParsedEntry> {
        val seen = linkedSetOf<String>()
        val out = mutableListOf<ParsedEntry>()
        fun add(url: String, nameRaw: String) {
            val normalized = normalizeUrl(url) ?: return
            val key = normalized.lowercase(Locale.US)
            if (!seen.add(key)) return
            val name = nameRaw
                .replace(Regex("<[^>]+>"), "")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim()
            out += ParsedEntry(name.ifBlank { extractName(normalized) }, normalized)
        }
        HREF_DOUBLE.findAll(html).forEach { add(it.groupValues[1], it.groupValues[2]) }
        HREF_SINGLE.findAll(html).forEach { add(it.groupValues[1], it.groupValues[2]) }
        return out
    }

    suspend fun importEntries(entries: List<ParsedEntry>): Int {
        return importEntriesDetailed(entries).imported
    }

    suspend fun importEntriesDetailed(
        entries: List<ParsedEntry>,
        skipExistingUrls: Boolean = true
    ): ImportResult {
        if (entries.isEmpty()) return ImportResult(0)

        val existing = if (skipExistingUrls) {
            repository.getAllUrls()
                .mapNotNull { normalizeUrl(it)?.lowercase(Locale.US) }
                .toHashSet()
        } else {
            emptySet()
        }

        var skippedDup = 0
        val toImport = ArrayList<WebApp>(entries.size)
        val batchSeen = hashSetOf<String>()
        for (entry in entries) {
            val normalized = normalizeUrl(entry.url) ?: continue
            val key = normalized.lowercase(Locale.US)
            if (!batchSeen.add(key)) {
                skippedDup++
                continue
            }
            if (key in existing) {
                skippedDup++
                continue
            }
            toImport += WebApp(
                name = entry.name.ifBlank { extractName(normalized) },
                url = normalized
            )
        }

        if (toImport.isEmpty()) {
            return ImportResult(imported = 0, skippedDuplicate = skippedDup)
        }

        val ids = repository.createWebApps(toImport)
        AppLogger.i(TAG, "批量导入 ${ids.size} 个应用, 跳过重复 $skippedDup")
        return ImportResult(imported = ids.size, skippedDuplicate = skippedDup)
    }

    fun exportAsTemplate(app: WebApp): String {
        val template = AppTemplate(
            name = app.name,
            url = app.url,
            appType = app.appType.name,
            webViewConfig = app.webViewConfig,
            adBlockEnabled = app.adBlockEnabled,
            adBlockRules = app.adBlockRules,
            adBlockSubscriptions = app.adBlockSubscriptions,
            extensionModuleIds = app.extensionModuleIds,
            extensionEnabled = app.extensionEnabled,
            splashEnabled = app.splashEnabled,
            bgmEnabled = app.bgmEnabled,
            translateEnabled = app.translateEnabled
        )
        return gson.toJson(template)
    }

    fun parseTemplate(json: String): AppTemplate? {
        return try {
            gson.fromJson(json, AppTemplate::class.java)
        } catch (e: Exception) {
            AppLogger.e(TAG, "解析模板失败: ${e.message}")
            null
        }
    }

    suspend fun importFromTemplate(template: AppTemplate): Long {
        val app = WebApp(
            name = template.name,
            url = template.url,
            adBlockEnabled = template.adBlockEnabled,
            adBlockRules = template.adBlockRules,
            adBlockSubscriptions = template.adBlockSubscriptions,
            extensionModuleIds = template.extensionModuleIds,
            extensionEnabled = template.extensionEnabled || template.extensionModuleIds.isNotEmpty()
        )
        return repository.createWebApp(app)
    }

    private fun looksLikeUrl(value: String): Boolean {
        val v = value.trim().trimEnd(',', ';', '.', ')', ']', '>')
        if (v.startsWith("http://", ignoreCase = true) || v.startsWith("https://", ignoreCase = true)) {
            return true
        }
        return DOMAIN.matches(v)
    }

    fun normalizeUrl(url: String): String? {
        var value = url.trim()
            .trimEnd(',', ';', '.', ')', ']', '>', '"', '\'')
            .trimStart('<', '"', '\'')
        if (value.isBlank()) return null
        if (value.startsWith("www.", ignoreCase = true)) {
            value = "https://$value"
        } else if (!value.startsWith("http://", ignoreCase = true) &&
            !value.startsWith("https://", ignoreCase = true)
        ) {
            if (!DOMAIN.matches(value)) return null
            value = "https://$value"
        }
        return try {
            val uri = URI(value)
            if (uri.host.isNullOrBlank() && !value.contains("://")) return null
            val scheme = uri.scheme?.lowercase(Locale.US) ?: return null
            if (scheme != "http" && scheme != "https") return null
            value
        } catch (_: Exception) {
            null
        }
    }

    private fun extractName(url: String): String {
        return try {
            val host = URI(url).host ?: return url.take(30)
            host.removePrefix("www.")
                .substringBeforeLast(".")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } catch (_: Exception) {
            url.take(30)
        }
    }
}

data class AppTemplate(
    val name: String,
    val url: String,
    val appType: String = "WEB",
    val webViewConfig: com.webtoapp.data.model.WebViewConfig = com.webtoapp.data.model.WebViewConfig(),
    val adBlockEnabled: Boolean = false,
    val adBlockRules: List<String> = emptyList(),
    val adBlockSubscriptions: List<String> = emptyList(),
    val extensionModuleIds: List<String> = emptyList(),
    val extensionEnabled: Boolean = false,
    val splashEnabled: Boolean = false,
    val bgmEnabled: Boolean = false,
    val translateEnabled: Boolean = false,
    val version: Int = 1
)
