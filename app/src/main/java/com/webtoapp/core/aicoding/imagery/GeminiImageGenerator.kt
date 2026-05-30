package com.webtoapp.core.aicoding.imagery

import android.content.Context
import android.util.Base64
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.aicoding.llm.HttpHelpers
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.SavedModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

internal class GeminiImageGenerator(context: Context) : ImageGenerator {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun supports(model: SavedModel): Boolean {
        val id = model.model.id.lowercase()
        return id.contains("imagen") || id.contains("gemini") && id.contains("image")
    }

    override suspend fun generate(
        request: ImageGenerator.Request,
        model: SavedModel,
        apiKey: ApiKeyConfig
    ): ImageGenerator.Result = withContext(Dispatchers.IO) {
        try {
            if (apiKey.provider != AiProvider.GOOGLE) {
                return@withContext ImageGenerator.Result.Error(
                    "GeminiImageGenerator only handles AiProvider.GOOGLE."
                )
            }

            val payload = JsonObject().apply {
                add("contents", JsonArray().apply {
                    add(JsonObject().apply {
                        add("parts", JsonArray().apply {
                            add(JsonObject().apply { addProperty("text", request.prompt) })
                        })
                    })
                })
                add("generationConfig", JsonObject().apply {
                    addProperty("responseMimeType", "image/png")
                })
            }

            val base = HttpHelpers.baseUrl(apiKey)
            val endpoint = "/v1beta/models/${model.model.id}:generateContent"
            val url = HttpHelpers.joinUrl(base, endpoint).toHttpUrl().newBuilder()
                .addQueryParameter("key", apiKey.apiKey.trim())
                .build()

            val req = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(req).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val (msg, _) = HttpHelpers.classifyHttpError(response.code, raw)
                    return@withContext ImageGenerator.Result.Error(msg)
                }
                parseGeminiResponse(raw)
            }
        } catch (t: Throwable) {
            AppLogger.e(TAG, "Gemini image generation failed: ${t.message}", t)
            ImageGenerator.Result.Error(t.message ?: "image generation failed")
        }
    }

    private fun parseGeminiResponse(raw: String): ImageGenerator.Result {
        val root = runCatching { JsonParser.parseString(raw).asJsonObject }
            .getOrElse { return ImageGenerator.Result.Error("invalid JSON from Gemini") }
        val candidates = root.getAsJsonArray("candidates")
            ?: return ImageGenerator.Result.Error("missing `candidates` array")
        val first = candidates.firstOrNull()?.asJsonObject
            ?: return ImageGenerator.Result.Error("empty `candidates` array")
        val parts = first.getAsJsonObject("content")?.getAsJsonArray("parts").orEmpty()
        for (part in parts) {
            val inline = part.asJsonObject?.getAsJsonObject("inlineData") ?: continue
            val mime = inline.get("mimeType")?.asString ?: "image/png"
            val data = inline.get("data")?.asString ?: continue
            val bytes = runCatching { Base64.decode(data, Base64.DEFAULT) }
                .getOrElse { continue }
            return ImageGenerator.Result.Ok(bytes, mime)
        }
        return ImageGenerator.Result.Error("Gemini response had no inlineData with image bytes")
    }

    private fun JsonArray?.orEmpty(): JsonArray = this ?: JsonArray()

    companion object {
        private const val TAG = "GeminiImageGenerator"
    }
}
