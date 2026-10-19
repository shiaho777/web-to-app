package com.webtoapp.core.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONArray
import org.json.JSONObject

class TranslateBridge(
    private val webView: WebView,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "TranslateBridge"
        const val JS_INTERFACE_NAME = "_nativeTranslate"

        enum class TranslateEngine(val displayName: String) {
            GOOGLE("Google Translate"),
            MYMEMORY("MyMemory"),
            LIBRE("LibreTranslate"),
            LINGVA("Lingva Translate");
        }

        private const val GOOGLE_API = "https://translate.googleapis.com/translate_a/single"
        private const val MYMEMORY_API = "https://api.mymemory.translated.net/get"
        private const val LIBRE_API = "https://libretranslate.com/translate"
        private const val LINGVA_API = "https://lingva.ml/api/v1"

        private const val ENGINE_FAILURE_THRESHOLD = 3

        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 15_000

        private const val RETRY_DELAY_MS = 200L
    }

    @Volatile
    private var activeEngineIndex = 0

    private val failureCounts = IntArray(TranslateEngine.entries.size)

    private val engines = TranslateEngine.entries.toTypedArray()

    private fun currentEngine(): TranslateEngine = engines[activeEngineIndex]

    private fun recordSuccess(engine: TranslateEngine) {
        failureCounts[engine.ordinal] = 0
    }

    private fun recordFailure(engine: TranslateEngine) {
        failureCounts[engine.ordinal]++
        if (failureCounts[engine.ordinal] >= ENGINE_FAILURE_THRESHOLD) {
            val nextIndex = (engine.ordinal + 1) % engines.size
            AppLogger.w(TAG, "${engine.displayName} 连续失败 ${failureCounts[engine.ordinal]} 次，降级到 ${engines[nextIndex].displayName}")
            activeEngineIndex = nextIndex
        }
    }

    @JavascriptInterface
    fun translate(textsJson: String, targetLang: String, callbackId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val texts = JSONArray(textsJson)
                val pending = ArrayList<String>(texts.length())
                for (i in 0 until texts.length()) {
                    pending.add(texts.getString(i))
                }
                val results = translateBatchWithFallback(pending, targetLang)

                // JSONArray.toString() is a valid JS array literal — pass it unquoted so no
                // hand-rolled string escaping is involved. callbackId comes from page JS:
                // quote it, or a crafted id ('x');payload();//) executes in whatever page is
                // loaded by the time the async translation call completes.
                val resultsJson = JSONArray(results).toString()
                val quotedCallbackId = com.webtoapp.util.JsStrings.quote(callbackId)

                withContext(Dispatchers.Main) {
                    webView.evaluateJavascript(
                        "window._translateCallback($quotedCallbackId, $resultsJson);",
                        null
                    )
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "翻译执行失败", e)
                val quotedCallbackId = com.webtoapp.util.JsStrings.quote(callbackId)
                val quotedError = com.webtoapp.util.JsStrings.quote(e.message ?: "translation failed")
                withContext(Dispatchers.Main) {
                    webView.evaluateJavascript(
                        "window._translateCallback($quotedCallbackId, null, $quotedError);",
                        null
                    )
                }
            }
        }
    }

    @JavascriptInterface
    fun getEngineStatus(): String {
        val status = JSONObject().apply {
            put("activeEngine", currentEngine().displayName)
            put("engines", JSONArray().apply {
                engines.forEach { engine ->
                    put(JSONObject().apply {
                        put("name", engine.displayName)
                        put("failures", failureCounts[engine.ordinal])
                        put("active", engine == currentEngine())
                    })
                }
            })
        }
        return status.toString()
    }

    @JavascriptInterface
    fun setEngine(engineName: String) {
        val engine = engines.find {
            it.name.equals(engineName, ignoreCase = true) ||
            it.displayName.equals(engineName, ignoreCase = true)
        }
        if (engine != null) {
            activeEngineIndex = engine.ordinal
            failureCounts[engine.ordinal] = 0
            AppLogger.i(TAG, "手动切换翻译引擎: ${engine.displayName}")
        }
    }

    private suspend fun translateBatchWithFallback(texts: List<String>, targetLang: String): List<String> {
        if (texts.isEmpty()) return emptyList()

        val attemptOrder = (0 until engines.size).map { offset ->
            engines[(activeEngineIndex + offset) % engines.size]
        }

        var lastError: Exception? = null
        for (engine in attemptOrder) {
            try {
                val result = translateAll(engine, texts, targetLang)
                recordSuccess(engine)
                if (engine != currentEngine()) {
                    activeEngineIndex = engine.ordinal
                    AppLogger.i(TAG, "降级翻译成功，切换活跃引擎到: ${engine.displayName}")
                }
                return result
            } catch (e: Exception) {
                lastError = e
                AppLogger.w(TAG, "${engine.displayName} 翻译失败: ${e.message}")
                recordFailure(engine)
                delay(RETRY_DELAY_MS)
            }
        }

        AppLogger.e(TAG, "所有翻译引擎均失败")
        throw lastError ?: IllegalStateException("all translation engines failed")
    }

    private fun translateAll(
        engine: TranslateEngine,
        texts: List<String>,
        targetLang: String
    ): List<String> {
        val limit = limitFor(engine)
        val pieces = TranslateTextPlan.piecesFor(texts, limit)
        val translated = MutableList(pieces.size) { "" }
        for (batch in TranslateTextPlan.batches(pieces, limit)) {
            val payload = TranslateTextPlan.join(pieces, batch)
            val raw = translatePayload(engine, payload, targetLang)
            val parts = TranslateTextPlan.splitTranslated(raw, batch.size)
                ?: batch.map { index ->
                    translatePayload(engine, pieces[index].text, targetLang)
                }
            batch.forEachIndexed { offset, pieceIndex ->
                translated[pieceIndex] = parts[offset]
            }
        }
        return TranslateTextPlan.reassemble(texts.size, pieces, translated)
    }

    private fun limitFor(engine: TranslateEngine): TranslateTextPlan.Limit {
        return when (engine) {
            TranslateEngine.GOOGLE -> TranslateTextPlan.GOOGLE
            TranslateEngine.MYMEMORY -> TranslateTextPlan.MYMEMORY
            TranslateEngine.LIBRE -> TranslateTextPlan.LIBRE
            TranslateEngine.LINGVA -> TranslateTextPlan.LINGVA
        }
    }

    private fun translatePayload(engine: TranslateEngine, payload: String, targetLang: String): String {
        if (payload.isEmpty()) return ""
        return when (engine) {
            TranslateEngine.GOOGLE -> translateViaGoogle(payload, targetLang)
            TranslateEngine.MYMEMORY -> translateViaMyMemory(payload, targetLang)
            TranslateEngine.LIBRE -> translateViaLibre(payload, targetLang)
            TranslateEngine.LINGVA -> translateViaLingva(payload, targetLang)
        }
    }

    private fun translateViaGoogle(payload: String, targetLang: String): String {
        val encoded = URLEncoder.encode(payload, "UTF-8")
        // targetLang comes from the page: encode it so it cannot inject
        // query/path structure into the fixed API endpoint.
        val lang = URLEncoder.encode(targetLang, "UTF-8")
        val url = URL("$GOOGLE_API?client=gtx&sl=auto&tl=$lang&dt=t&q=$encoded")
        val conn = TranslateProxyRoute.open(url)
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36")
        conn.connectTimeout = CONNECT_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS

        return try {
            val response = readBody(conn, "Google")
            parseGoogleResponse(response)
        } finally {
            conn.disconnect()
        }
    }

    private fun parseGoogleResponse(response: String): String {
        val json = JSONArray(response)
        val translations = json.getJSONArray(0)
        val sb = StringBuilder()
        for (i in 0 until translations.length()) {
            val item = translations.getJSONArray(i)
            if (!item.isNull(0)) sb.append(item.getString(0))
        }
        if (sb.isEmpty()) throw IllegalStateException("Google response had no translation")
        return sb.toString()
    }

    private fun translateViaMyMemory(payload: String, targetLang: String): String {
        val encoded = URLEncoder.encode(payload, "UTF-8")
        val langPair = URLEncoder.encode("autodetect|$targetLang", "UTF-8")
        val url = URL("$MYMEMORY_API?q=$encoded&langpair=$langPair")
        val conn = TranslateProxyRoute.open(url)
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "WebToApp/1.0")
        conn.connectTimeout = CONNECT_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS

        return try {
            parseMyMemoryResponse(readBody(conn, "MyMemory"))
        } finally {
            conn.disconnect()
        }
    }

    private fun parseMyMemoryResponse(response: String): String {
        val json = JSONObject(response)
        val status = json.optInt("responseStatus", 0)
        if (status != 200) {
            throw IllegalStateException(
                "MyMemory status $status: ${json.optString("responseDetails")}"
            )
        }
        val translated = json.getJSONObject("responseData").optString("translatedText")
        if (translated.isBlank()) throw IllegalStateException("MyMemory response had no translation")
        return translated
    }

    private fun translateViaLibre(payload: String, targetLang: String): String {
        val url = URL(LIBRE_API)
        val conn = TranslateProxyRoute.open(url)
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", "WebToApp/1.0")
        conn.connectTimeout = CONNECT_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS
        conn.doOutput = true

        val body = JSONObject().apply {
            put("q", payload)
            put("source", "auto")
            put("target", targetLang)
            put("format", "text")
        }.toString()

        return try {
            conn.outputStream.use { os ->
                os.write(body.toByteArray(Charsets.UTF_8))
            }
            parseLibreResponse(readBody(conn, "LibreTranslate"))
        } finally {
            conn.disconnect()
        }
    }

    private fun parseLibreResponse(response: String): String {
        val json = JSONObject(response)
        if (json.has("error")) {
            throw IllegalStateException("LibreTranslate: ${json.optString("error")}")
        }
        val translated = json.optString("translatedText")
        if (translated.isBlank()) throw IllegalStateException("LibreTranslate response had no translation")
        return translated
    }

    private fun translateViaLingva(payload: String, targetLang: String): String {
        val encoded = URLEncoder.encode(payload, "UTF-8")
        // Path segment: encode so a crafted lang cannot walk out of /auto/<lang>/.
        val lang = URLEncoder.encode(targetLang, "UTF-8")
        val url = URL("$LINGVA_API/auto/$lang/$encoded")
        val conn = TranslateProxyRoute.open(url)
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "WebToApp/1.0")
        conn.connectTimeout = CONNECT_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS

        return try {
            parseLingvaResponse(readBody(conn, "Lingva"))
        } finally {
            conn.disconnect()
        }
    }

    private fun parseLingvaResponse(response: String): String {
        val json = JSONObject(response)
        if (json.has("error")) {
            throw IllegalStateException("Lingva: ${json.optString("error")}")
        }
        val translated = json.optString("translation")
        if (translated.isBlank()) throw IllegalStateException("Lingva response had no translation")
        return translated
    }

    private fun readBody(conn: HttpURLConnection, engineName: String): String {
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            throw IllegalStateException("$engineName HTTP $code")
        }
        if (body.isBlank()) throw IllegalStateException("$engineName returned an empty body")
        return body
    }
}
