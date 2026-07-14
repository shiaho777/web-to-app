package com.webtoapp.core.market

import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class CwsSearchResult(
    val name: String,
    val storeId: String,
    val iconUrl: String? = null,
    val version: String = "",
    val userCount: String = "",
    val rating: String = "",
    val ratingValue: Double = 0.0,
    val ratingCount: Int = 0,
    val userCountValue: Long = 0L,
    val ratingCountLabel: String = ""
)

data class CwsExtensionDetails(
    val storeId: String,
    val name: String = "",
    val iconUrl: String? = null,
    val ratingValue: Double = 0.0,
    val ratingCount: Int = 0,
    val userCountValue: Long = 0L,
    val ratingLabel: String = "",
    val ratingCountLabel: String = "",
    val userCountLabel: String = ""
)

object ChromeWebStoreSearch {

    private const val TAG = "CwsSearch"

    private const val ENDPOINT =
        "https://chromewebstore.google.com/_/ChromeWebStoreConsumerFeUi/data/batchexecute"

    private const val RPC_ID = "QcU9bc"

    private const val BROWSER_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"

    private val FORM_MEDIA_TYPE = "application/x-www-form-urlencoded;charset=UTF-8".toMediaType()

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val detailsCache = ConcurrentHashMap<String, CwsExtensionDetails>()
    private val detailsMutex = Mutex()

    suspend fun search(
        query: String,
        locale: String = "en"
    ): Result<List<CwsSearchResult>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.success(emptyList())
        }

        val url = buildString {
            append(ENDPOINT)
            append("?rpcids=").append(RPC_ID)
            append("&source-path=").append("/")
            append("&hl=").append(locale)
            append("&soc-app=1&soc-platform=1&soc-device=1")
            append("&rt=c")
        }

        val escapedQuery = trimmed.replace("\\", "\\\\").replace("\"", "\\\"")
        val freq = """[[["$RPC_ID","[\"$escapedQuery\",null,null,2]"]]]"""
        val body = "f.req=" + java.net.URLEncoder.encode(freq, "UTF-8")

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", BROWSER_UA)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .header("Accept", "*/*")
                .post(body.toRequestBody(FORM_MEDIA_TYPE))
                .build()

            val raw = client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    AppLogger.w(TAG, "batchexecute HTTP ${resp.code} for query='$trimmed'")
                    return@withContext Result.failure(
                        IllegalStateException("Chrome Web Store returned HTTP ${resp.code}")
                    )
                }
                resp.body?.string() ?: run {
                    AppLogger.w(TAG, "Empty response body for query='$trimmed'")
                    return@withContext Result.failure(IllegalStateException("Empty response"))
                }
            }

            val results = parseBatchExecute(raw)
            if (results == null) {
                AppLogger.w(TAG, "Failed to parse response for query='$trimmed' (format may have changed)")
                return@withContext Result.failure(
                    IllegalStateException("Could not parse Chrome Web Store response")
                )
            }
            Result.success(results)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Search failed for query='$trimmed'", e)
            Result.failure(e)
        }
    }

    suspend fun enrichResults(
        results: List<CwsSearchResult>,
        locale: String = "en",
        limit: Int = 12
    ): List<CwsSearchResult> = withContext(Dispatchers.IO) {
        if (results.isEmpty()) return@withContext results
        coroutineScope {
            results.take(limit).map { item ->
                async {
                    if (item.ratingValue > 0.0 && !item.iconUrl.isNullOrBlank() && item.userCountValue > 0L) {
                        return@async item
                    }
                    val details = fetchDetails(item.storeId, locale).getOrNull() ?: return@async item
                    item.copy(
                        name = item.name.ifBlank { details.name },
                        iconUrl = details.iconUrl ?: item.iconUrl,
                        rating = details.ratingLabel.ifBlank { item.rating },
                        userCount = details.userCountLabel.ifBlank { item.userCount },
                        ratingValue = details.ratingValue.takeIf { it > 0.0 } ?: item.ratingValue,
                        ratingCount = details.ratingCount.takeIf { it > 0 } ?: item.ratingCount,
                        userCountValue = details.userCountValue.takeIf { it > 0L } ?: item.userCountValue,
                        ratingCountLabel = details.ratingCountLabel.ifBlank { item.ratingCountLabel }
                    )
                }
            }.awaitAll() + results.drop(limit)
        }
    }

    suspend fun fetchDetails(
        storeId: String,
        locale: String = "en"
    ): Result<CwsExtensionDetails> = withContext(Dispatchers.IO) {
        val id = storeId.trim()
        if (id.length != 32) {
            return@withContext Result.failure(IllegalArgumentException("Invalid store id"))
        }
        detailsCache[id]?.let { return@withContext Result.success(it) }
        detailsMutex.withLock {
            detailsCache[id]?.let { return@withLock Result.success(it) }
            try {
                val url = "https://chromewebstore.google.com/detail/$id?hl=$locale"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", BROWSER_UA)
                    .header("Accept", "text/html,application/xhtml+xml")
                    .header("Accept-Language", locale)
                    .get()
                    .build()
                val html = client.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        return@withLock Result.failure(
                            IllegalStateException("Chrome Web Store detail HTTP ${resp.code}")
                        )
                    }
                    resp.body?.string().orEmpty()
                }
                val parsed = parseDetailHtml(id, html)
                    ?: return@withLock Result.failure(IllegalStateException("Could not parse extension details"))
                detailsCache[id] = parsed
                Result.success(parsed)
            } catch (e: Exception) {
                AppLogger.w(TAG, "fetchDetails failed for $id: ${e.message}")
                Result.failure(e)
            }
        }
    }

    internal fun parseBatchExecute(raw: String): List<CwsSearchResult>? {
        return try {
            val payload = extractPayloadLine(raw) ?: return null
            val envelope = JSONArray(payload)
            val wrbFr = envelope.optJSONArray(0) ?: return null
            val innerJson = wrbFr.optString(2).ifEmpty { return null }
            val wrapper = JSONArray(innerJson)
            val rows = wrapper.optJSONArray(0)
            if (rows == null) {
                if (wrapper.length() == 0) return emptyList()
                return null
            }

            val results = mutableListOf<CwsSearchResult>()
            for (i in 0 until rows.length()) {
                val converted = convertRow(rows.optJSONArray(i)) ?: continue
                results.add(converted)
            }
            results
        } catch (e: Exception) {
            AppLogger.w(TAG, "parseBatchExecute failed: ${e.message}")
            null
        }
    }

    internal fun parseDetailHtml(storeId: String, html: String): CwsExtensionDetails? {
        if (html.isBlank()) return null

        val iconUrl = OG_IMAGE_REGEX.find(html)?.groupValues?.get(1)
            ?: META_IMAGE_REGEX.find(html)?.groupValues?.get(1)

        val compact = Regex(
            Regex.escape(storeId) + ""","(https:[^"]+)","([^"]+)",([0-9]+(?:\.[0-9]+)?),([0-9]+)"""
        ).find(html)
        val iconFromPattern = compact?.groupValues?.get(1)?.takeIf { it.startsWith("http") }
        val nameFromPattern = compact?.groupValues?.get(2).orEmpty()
        val ratingFromPattern = compact?.groupValues?.get(3)?.toDoubleOrNull()
        val ratingCountFromPattern = compact?.groupValues?.get(4)?.toIntOrNull()

        val ratingText = OUT_OF_FIVE_REGEX.find(html)?.groupValues?.get(1)?.toDoubleOrNull()
        val ratingCountText = parseCompactCount(RATINGS_COUNT_REGEX.find(html)?.groupValues?.get(1))
        val usersText = USERS_REGEX.find(html)?.groupValues?.get(1)

        val ratingValue = when {
            ratingText != null && ratingText > 0 -> ratingText
            ratingFromPattern != null && ratingFromPattern > 0 ->
                kotlin.math.round(ratingFromPattern * 10.0) / 10.0
            else -> 0.0
        }
        val ratingCount = ratingCountFromPattern
            ?: ratingCountText
            ?: 0
        val userCountValue = parseUserCount(usersText) ?: 0L

        val name = nameFromPattern
            .ifBlank { OG_TITLE_REGEX.find(html)?.groupValues?.get(1).orEmpty() }
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .trim()

        if (iconUrl == null && iconFromPattern == null && ratingValue <= 0.0 && userCountValue <= 0L) {
            return null
        }

        return CwsExtensionDetails(
            storeId = storeId,
            name = name,
            iconUrl = iconUrl ?: iconFromPattern,
            ratingValue = ratingValue,
            ratingCount = ratingCount,
            userCountValue = userCountValue,
            ratingLabel = if (ratingValue > 0.0) String.format(java.util.Locale.US, "%.1f", ratingValue) else "",
            ratingCountLabel = formatCompactCount(ratingCount),
            userCountLabel = formatUserCount(userCountValue)
        )
    }

    private fun extractPayloadLine(raw: String): String? {
        val lines = raw.split("\n")
        for (idx in 0 until minOf(lines.size, 6)) {
            val trimmed = lines[idx].trim()
            if (trimmed.startsWith("[[") || trimmed.startsWith("[\"wrb")) {
                return trimmed
            }
        }
        return null
    }

    private fun convertRow(row: JSONArray?): CwsSearchResult? {
        if (row == null || row.length() < 2) return null
        val fields = row.optJSONArray(1) ?: return null
        return try {
            val name = readString(fields, 0)?.takeIf { it.isNotBlank() } ?: return null
            val storeId = readString(fields, 1)?.takeIf {
                it.length == 32 && it.all { c -> c.isLetterOrDigit() }
            } ?: return null
            val iconUrl = readString(fields, 3)?.takeIf { it.startsWith("http") }
            CwsSearchResult(
                name = name,
                storeId = storeId,
                iconUrl = iconUrl
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun readString(arr: JSONArray, index: Int): String? {
        val value = arr.opt(index) ?: return null
        return when (value) {
            is String -> value
            else -> null
        }
    }

    private fun parseUserCount(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val digits = raw.replace(",", "").replace(" ", "").trim()
        return digits.toLongOrNull()
    }

    private fun parseCompactCount(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw.trim().uppercase().replace(",", "")
        return when {
            cleaned.endsWith("K") -> {
                val n = cleaned.dropLast(1).toDoubleOrNull() ?: return null
                (n * 1_000).toInt()
            }
            cleaned.endsWith("M") -> {
                val n = cleaned.dropLast(1).toDoubleOrNull() ?: return null
                (n * 1_000_000).toInt()
            }
            else -> cleaned.toIntOrNull()
        }
    }

    fun formatCompactCount(value: Int): String {
        if (value <= 0) return ""
        return when {
            value >= 1_000_000 -> {
                if (value >= 10_000_000) (value / 1_000_000).toString() + "M"
                else String.format(java.util.Locale.US, "%.1fM", value / 1_000_000.0)
                    .trimEnd('0').trimEnd('.')
            }
            value >= 1_000 -> {
                if (value >= 100_000) (value / 1_000).toString() + "K"
                else String.format(java.util.Locale.US, "%.1fK", value / 1_000.0)
                    .trimEnd('0').trimEnd('.')
            }
            else -> value.toString()
        }
    }

    fun formatUserCount(value: Long): String {
        if (value <= 0L) return ""
        return when {
            value >= 1_000_000L -> {
                if (value % 1_000_000L == 0L || value >= 10_000_000L) {
                    (value / 1_000_000L).toString() + "M+"
                } else {
                    String.format(java.util.Locale.US, "%.1fM+", value / 1_000_000.0)
                        .replace(".0M+", "M+")
                }
            }
            value >= 1_000L -> {
                if (value % 1_000L == 0L || value >= 100_000L) {
                    (value / 1_000L).toString() + "K+"
                } else {
                    String.format(java.util.Locale.US, "%.1fK+", value / 1_000.0)
                        .replace(".0K+", "K+")
                }
            }
            else -> value.toString()
        }
    }


    private val OG_IMAGE_REGEX =
        Regex("""property=["']og:image["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val META_IMAGE_REGEX =
        Regex("""content=["'](https://lh3\.googleusercontent\.com/[^"']+)["'][^>]*property=["']og:image["']""", RegexOption.IGNORE_CASE)
    private val OG_TITLE_REGEX =
        Regex("""property=["']og:title["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val OUT_OF_FIVE_REGEX =
        Regex("""([0-9]+(?:\.[0-9]+)?)\s+out of 5""", RegexOption.IGNORE_CASE)
    private val RATINGS_COUNT_REGEX =
        Regex("""([0-9]+(?:\.[0-9]+)?[KkMm]?)\s+ratings""", RegexOption.IGNORE_CASE)
    private val USERS_REGEX =
        Regex("""([0-9][0-9,]*)\s+users""", RegexOption.IGNORE_CASE)
}
