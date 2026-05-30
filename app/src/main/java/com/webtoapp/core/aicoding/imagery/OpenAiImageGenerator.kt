package com.webtoapp.core.aicoding.imagery

import android.content.Context
import android.util.Base64
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.aicoding.llm.HttpHelpers
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.SavedModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

internal class OpenAiImageGenerator(context: Context) : ImageGenerator {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun supports(model: SavedModel): Boolean {

        val provider = model.apiKeyId
        val id = model.model.id.lowercase()
        return id.startsWith("dall-e") ||
            id.startsWith("gpt-image") ||
            id.startsWith("stable-diffusion") ||
            id.startsWith("sdxl") ||
            id.startsWith("flux")
    }

    override suspend fun generate(
        request: ImageGenerator.Request,
        model: SavedModel,
        apiKey: ApiKeyConfig
    ): ImageGenerator.Result = withContext(Dispatchers.IO) {
        try {

            if (apiKey.provider == AiProvider.ANTHROPIC ||
                apiKey.provider == AiProvider.OLLAMA ||
                apiKey.provider == AiProvider.GOOGLE
            ) {
                return@withContext ImageGenerator.Result.Error(
                    "OpenAiImageGenerator does not support provider ${apiKey.provider.name}."
                )
            }

            val body = JsonObject().apply {
                addProperty("model", model.model.id)
                addProperty("prompt", request.prompt)
                addProperty("n", 1)
                addProperty("size", "${request.size}x${request.size}")

                addProperty("response_format", "b64_json")
                request.style?.takeIf { it.isNotBlank() }?.let { addProperty("style", normaliseStyle(it)) }
            }

            val base = HttpHelpers.baseUrl(apiKey)
            val url = HttpHelpers.joinUrl(base, "/v1/images/generations")
            val builder = Request.Builder()
                .url(url)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
            HttpHelpers.applyAuth(builder, apiKey)

            client.newCall(builder.build()).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val (msg, _) = HttpHelpers.classifyHttpError(response.code, raw)
                    return@withContext ImageGenerator.Result.Error(msg)
                }
                parseB64Response(raw)
            }
        } catch (t: Throwable) {
            AppLogger.e(TAG, "OpenAI image generation failed: ${t.message}", t)
            ImageGenerator.Result.Error(t.message ?: "image generation failed")
        }
    }

    private fun parseB64Response(raw: String): ImageGenerator.Result {
        val root = runCatching { JsonParser.parseString(raw).asJsonObject }
            .getOrElse { return ImageGenerator.Result.Error("invalid JSON from image API") }
        val data = root.getAsJsonArray("data") ?: return ImageGenerator.Result.Error("missing `data` array")
        val first = data.firstOrNull()?.asJsonObject ?: return ImageGenerator.Result.Error("empty `data` array")
        val b64 = first.get("b64_json")?.asString
            ?: return ImageGenerator.Result.Error(
                "no b64_json in response (saw url=${first.get("url")?.asString != null})"
            )
        val bytes = runCatching { Base64.decode(b64, Base64.DEFAULT) }
            .getOrElse { return ImageGenerator.Result.Error("base64 decode failed: ${it.message}") }
        return ImageGenerator.Result.Ok(bytes, "image/png")
    }

    private fun normaliseStyle(s: String): String = when (s.lowercase()) {
        "realistic", "photo", "minimalist" -> "natural"
        else -> "vivid"
    }

    companion object {
        private const val TAG = "OpenAiImageGenerator"
    }
}
