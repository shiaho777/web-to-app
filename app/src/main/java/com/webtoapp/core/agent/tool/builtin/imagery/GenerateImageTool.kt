package com.webtoapp.core.agent.tool.builtin.imagery

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.imagery.ImageGenerator
import com.webtoapp.core.agent.imagery.ImageGeneratorRegistry
import com.webtoapp.core.ai.ApiKeyRotation
import kotlinx.coroutines.delay
import com.webtoapp.core.agent.tool.FileChange
import com.webtoapp.core.agent.tool.ImageAttachment
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.agent.tool.builtin.jsonSchema

class GenerateImageTool(private val registry: ImageGeneratorRegistry) : Tool {

    override val name = "GenerateImage"
    override val description = """
        Generate an image from a text prompt. Saves the result to `assets/<slug>.png` and returns a multimodal tool result so you can SEE the generated image and iterate.
        - Use this for icons, hero images, illustrations, and visual mockups.
        - You can reference the saved file directly: `<img src="assets/<slug>.png">`.
        - Iterate by re-generating with refined prompts; each result is visible to you in the next turn.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("prompt", "Detailed description of the image", required = true)
        string("negative_prompt", "What the image should NOT contain (optional)")
        enum(
            "style",
            listOf("realistic", "illustration", "cartoon", "icon", "abstract", "minimalist", "photo", "3d"),
            "Visual style hint (optional)"
        )
        enum("size", listOf("256", "512", "1024"), "Square pixel size (default 1024)")
        string("filename", "Override the saved filename (optional, must end in .png/.jpg)")
    }

    override fun activityDescription(args: JsonObject): String? {
        val prompt = args.get("prompt")?.asString ?: return null
        return "Painting: ${prompt.take(40)}${if (prompt.length > 40) "…" else ""}"
    }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val prompt = args.get("prompt")?.asString?.trim()
            ?: return ToolResult.error("GenerateImage: missing `prompt`.")
        if (prompt.isEmpty()) return ToolResult.error("GenerateImage: prompt is empty.")

        val model = ctx.imageModel ?: return ToolResult.error(
            "GenerateImage: no image model is configured. Ask the user to pick one in AI settings."
        )
        val key = ctx.imageApiKey ?: return ToolResult.error(
            "GenerateImage: image model has no API key configured."
        )
        val generator = registry.generatorFor(model) ?: return ToolResult.error(
            "GenerateImage: the image model `${model.model.name}` is not supported by any installed generator."
        )

        val req = ImageGenerator.Request(
            prompt = prompt,
            negativePrompt = args.get("negative_prompt")?.asString?.takeIf { it.isNotBlank() },
            style = args.get("style")?.asString,
            size = args.get("size")?.asString ?: "1024"
        )

        val pool = ctx.imageKeyPool.ifEmpty { listOf(key) }
        val rotation = ApiKeyRotation.shared
        var sameKeyRetries = 0
        var attempts = 0
        val cap = pool.size + ApiKeyRotation.MAX_SAME_KEY_RETRIES + 1
        var lastError = "image generation failed"
        while (attempts++ < cap) {
            val current = rotation.select(key, pool, model.model.id)
            if (current == null) {
                val wait = rotation.soonestWaitMs(key, pool, model.model.id)
                if (wait != null && wait <= ApiKeyRotation.MAX_INLINE_WAIT_MS &&
                    sameKeyRetries < ApiKeyRotation.MAX_SAME_KEY_RETRIES
                ) {
                    sameKeyRetries++
                    delay(wait)
                    continue
                }
                return ToolResult.error("GenerateImage: $lastError")
            }
            val result = runCatching { generator.generate(req, model, current) }
                .getOrElse { return ToolResult.error("GenerateImage: ${it.message ?: "generator threw"}") }
            if (result !is ImageGenerator.Result.Ok) {
                val error = result as ImageGenerator.Result.Error
                lastError = error.message
                val rotatable = error.httpCode == 401 || error.httpCode == 403 ||
                    error.httpCode == 429 || error.limitScope != null
                when (val decision = rotation.onFailure(
                    current = current,
                    bound = key,
                    pool = pool,
                    modelId = model.model.id,
                    httpCode = error.httpCode,
                    retryAfterMs = error.retryAfterMs,
                    limitScope = error.limitScope,
                    recoverable = rotatable || (error.httpCode != null && error.httpCode in 500..599),
                    sameKeyRetries = sameKeyRetries
                )) {
                    is ApiKeyRotation.KeyDecision.Switch -> sameKeyRetries = 0
                    is ApiKeyRotation.KeyDecision.Backoff -> {
                        sameKeyRetries = decision.sameKeyRetries
                        delay(decision.waitMs)
                    }
                    is ApiKeyRotation.KeyDecision.Fail ->
                        return ToolResult.error("GenerateImage: ${error.message}")
                }
                continue
            }

            val filename = decideFilename(args.get("filename")?.asString, prompt, result.mimeType)
            val saved: ProjectFileManager.FileInfo = ctx.fileManager.writeBytes(
                ctx.sessionId,
                "assets/$filename",
                result.bytes
            ) ?: return ToolResult.error("GenerateImage: failed to save image (path rejected).")

            val text = buildString {
                append("Generated image saved to ${saved.relativePath} ")
                append("(${saved.formatSize()}).\n")
                append("Inline it with: <img src=\"${saved.relativePath}\" alt=\"\">\n")
                append("You can SEE this image in the multimodal payload — judge whether it matches the prompt and re-generate if needed.")
            }

            return ToolResult.multimodal(
                text = text,
                images = listOf(ImageAttachment(result.bytes, result.mimeType, saved.relativePath)),
                fileChange = FileChange(saved.relativePath, FileChange.Kind.WRITE)
            )
        }
        return ToolResult.error("GenerateImage: $lastError")
    }

    private fun decideFilename(override: String?, prompt: String, mime: String): String {
        if (!override.isNullOrBlank()) {

            val cleaned = override.trim().trimStart('/').replace('\\', '/')
            val ext = cleaned.substringAfterLast('.', "").lowercase()
            if (ext == "png" || ext == "jpg" || ext == "jpeg" || ext == "webp") return cleaned
        }
        val slug = slugify(prompt).take(40).ifEmpty { "image" }
        val timestamp = System.currentTimeMillis().toString(36)
        val ext = when (mime) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/webp" -> "webp"
            else -> "png"
        }
        return "$slug-$timestamp.$ext"
    }

    private fun slugify(s: String): String =
        s.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
}
