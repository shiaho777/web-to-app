package com.webtoapp.core.market

import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class GfSearchResult(
    val id: Long,
    val name: String,
    val description: String,
    val version: String,
    val codeUrl: String,
    val pageUrl: String,
    val author: String,
    val authorUrl: String?,
    val fanScore: Double,
    val totalInstalls: Long,
    val dailyInstalls: Long,
    val goodRatings: Long,
    val okRatings: Long,
    val badRatings: Long,
    val codeUpdatedAt: String,
    val license: String,
    val locale: String,
    val codeSize: Long
) {
    val ratingsTotal: Long get() = goodRatings + okRatings + badRatings
}

enum class GfSort(val apiValue: String) {
    DAILY("daily_installs"),
    TOTAL("total_installs"),
    SCORE("fan_score"),
    RATINGS("ratings"),
    UPDATED("code_updated_at")
}

enum class GfBrowseCategory(val apiQuery: String?) {
    HOT(null),
    AD_BLOCKING("adblock"),
    PRIVACY("privacy"),
    YOUTUBE("youtube"),
    PRODUCTIVITY("productivity"),
    DEVELOPER("developer"),
    STYLING("style");

    companion object {
        fun browseOrder(): List<GfBrowseCategory> = listOf(
            HOT,
            AD_BLOCKING,
            PRIVACY,
            YOUTUBE,
            PRODUCTIVITY,
            DEVELOPER,
            STYLING
        )
    }
}

object GreasyForkSearch {

    private const val TAG = "GfSearch"
    private const val BASE = "https://greasyfork.org"
    private const val MAX_SCRIPT_SIZE = 4L * 1024 * 1024

    private const val BROWSER_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    suspend fun search(
        query: String,
        locale: String = "en",
        sort: GfSort = GfSort.DAILY
    ): Result<List<GfSearchResult>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return Result.success(emptyList())
        }
        return fetchScripts(query = trimmed, locale = locale, sort = sort)
    }

    suspend fun browse(
        locale: String = "en",
        sort: GfSort = GfSort.DAILY,
        category: GfBrowseCategory = GfBrowseCategory.HOT
    ): Result<List<GfSearchResult>> {
        return fetchScripts(query = category.apiQuery, locale = locale, sort = sort)
    }

    private suspend fun fetchScripts(
        query: String?,
        locale: String,
        sort: GfSort
    ): Result<List<GfSearchResult>> = withContext(Dispatchers.IO) {
        val pathLocale = mapLocale(locale)
        val url = buildString {
            append(BASE)
            append('/')
            append(pathLocale)
            append("/scripts.json?")
            val q = query?.trim().orEmpty()
            if (q.isNotEmpty()) {
                append("q=")
                append(URLEncoder.encode(q, "UTF-8"))
                append('&')
            }
            append("sort=")
            append(sort.apiValue)
        }
        val label = query?.trim().orEmpty().ifBlank { "browse:${sort.apiValue}" }

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", BROWSER_UA)
                .header("Accept", "application/json")
                .header("Accept-Language", locale)
                .get()
                .build()

            val raw = client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    AppLogger.w(TAG, "scripts HTTP ${resp.code} for '$label'")
                    return@withContext Result.failure(
                        IllegalStateException("GreasyFork returned HTTP ${resp.code}")
                    )
                }
                resp.body?.string() ?: run {
                    AppLogger.w(TAG, "Empty scripts body for '$label'")
                    return@withContext Result.failure(IllegalStateException("Empty response"))
                }
            }

            val results = parseSearchResponse(raw)
            AppLogger.i(TAG, "GreasyFork '$label' returned ${results.size} scripts")
            Result.success(results)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Fetch failed for '$label'", e)
            Result.failure(e)
        }
    }

    suspend fun fetchScriptCode(codeUrl: String): Result<String> = withContext(Dispatchers.IO) {
        if (codeUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Empty code url"))
        }
        try {
            val request = Request.Builder()
                .url(codeUrl)
                .header("User-Agent", BROWSER_UA)
                .header("Accept", "text/javascript, application/javascript, */*")
                .get()
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("HTTP ${resp.code} fetching script")
                    )
                }
                val reported = resp.body?.contentLength() ?: -1L
                if (reported in 1..MAX_SCRIPT_SIZE) {
                    val text = resp.body?.string().orEmpty()
                    if (text.isBlank()) {
                        return@withContext Result.failure(IllegalStateException("Empty script body"))
                    }
                    Result.success(text)
                } else {

                    val text = resp.body?.charStream()?.buffered()?.use { reader ->
                        val sb = StringBuilder()
                        val buffer = CharArray(8192)
                        var total = 0L
                        while (true) {
                            val n = reader.read(buffer)
                            if (n < 0) break
                            total += n
                            if (total > MAX_SCRIPT_SIZE) {
                                AppLogger.w(TAG, "Script exceeds $MAX_SCRIPT_SIZE bytes, aborting: $codeUrl")
                                return@withContext Result.failure(
                                    IllegalStateException("Script too large")
                                )
                            }
                            sb.append(buffer, 0, n)
                        }
                        sb.toString()
                    }.orEmpty()
                    if (text.isBlank()) {
                        return@withContext Result.failure(IllegalStateException("Empty script body"))
                    }
                    Result.success(text)
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "fetchScriptCode failed: $codeUrl", e)
            Result.failure(e)
        }
    }

    internal fun parseSearchResponse(raw: String): List<GfSearchResult> {
        return try {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return emptyList()
            val queryArray: JSONArray = when {
                trimmed.startsWith("[") -> JSONArray(trimmed)
                else -> JSONObject(trimmed).optJSONArray("query") ?: return emptyList()
            }
            val results = mutableListOf<GfSearchResult>()
            for (i in 0 until queryArray.length()) {
                val obj = queryArray.optJSONObject(i) ?: continue
                val parsed = convertEntry(obj) ?: continue
                results.add(parsed)
            }
            results
        } catch (e: Exception) {
            AppLogger.w(TAG, "parseSearchResponse failed: ${e.message}")
            emptyList()
        }
    }

    private fun convertEntry(obj: JSONObject): GfSearchResult? {
        return try {
            val usersArray = obj.optJSONArray("users")
            val firstUser = usersArray?.optJSONObject(0)
            GfSearchResult(
                id = obj.optLong("id"),
                name = obj.optString("name").ifBlank { return null },
                description = obj.optString("description"),
                version = obj.optString("version"),
                codeUrl = obj.optString("code_url").ifBlank { "" },
                pageUrl = obj.optString("url"),
                author = firstUser?.optString("name").orEmpty(),
                authorUrl = firstUser?.optString("url")?.takeIf { it.isNotBlank() },
                fanScore = optDoubleFlexible(obj, "fan_score"),
                totalInstalls = obj.optLong("total_installs"),
                dailyInstalls = obj.optLong("daily_installs"),
                goodRatings = obj.optLong("good_ratings"),
                okRatings = obj.optLong("ok_ratings"),
                badRatings = obj.optLong("bad_ratings"),
                codeUpdatedAt = obj.optString("code_updated_at"),
                license = obj.optString("license").takeIf { it.isNotBlank() && it != "null" }.orEmpty(),
                locale = obj.optString("locale"),
                codeSize = obj.optLong("code_size")
            )
        } catch (e: Exception) {
            AppLogger.w(TAG, "convertEntry failed: ${e.message}")
            null
        }
    }


    private fun optDoubleFlexible(obj: JSONObject, key: String): Double {
        if (!obj.has(key) || obj.isNull(key)) return 0.0
        return when (val v = obj.opt(key)) {
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    private fun mapLocale(locale: String): String {
        if (locale.isBlank()) return "en"
        val lower = locale.lowercase()
        return when {

            lower.startsWith("zh") -> "zh-CN"
            lower.startsWith("ar") -> "ar"
            lower.startsWith("pt") -> "pt-BR"
            lower.startsWith("es") -> "es"
            lower.startsWith("fr") -> "fr"
            lower.startsWith("de") -> "de"
            lower.startsWith("ru") -> "ru"
            lower.startsWith("ja") -> "ja"
            lower.startsWith("ko") -> "ko"
            else -> "en"
        }
    }

    fun formatInstallCount(count: Long): String {
        return when {
            count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    fun formatScore(score: Double): String {
        return if (score > 0.0) String.format(java.util.Locale.US, "%.1f", score) else "--"
    }
}
