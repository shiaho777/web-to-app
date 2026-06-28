package com.webtoapp.core.market

import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.util.concurrent.TimeUnit

data class CwsSearchResult(
    val name: String,
    val storeId: String,
    val iconUrl: String? = null,
    val version: String = "",
    val userCount: String = "",
    val rating: String = ""
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
}
