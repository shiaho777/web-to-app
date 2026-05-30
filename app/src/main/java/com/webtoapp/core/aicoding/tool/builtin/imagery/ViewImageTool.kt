package com.webtoapp.core.aicoding.tool.builtin.imagery

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.tool.ImageAttachment
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult
import com.webtoapp.core.aicoding.tool.builtin.jsonSchema

class ViewImageTool : Tool {
    override val name = "ViewImage"
    override val description = """
        Load an image from the project so you can SEE it. Use this for:
        - Re-checking an image you generated earlier this session.
        - Looking at a reference image the user attached.
        - Comparing two images for style consistency before placing them in HTML.
        Path is project-relative (e.g. `assets/hero.png`).
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("path", "Project-relative path to the image file", required = true)
    }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject): String? =
        args.get("path")?.asString?.let { "Looking at $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val path = ctx.resolveSafePath(args.get("path")?.asString)
            ?: return ToolResult.error("ViewImage: invalid or missing `path`.")
        val mime = guessMime(path) ?: return ToolResult.error(
            "ViewImage: $path doesn't look like an image (expected .png/.jpg/.webp/.gif)."
        )
        val bytes = ctx.fileManager.readBytes(ctx.sessionId, path)
            ?: return ToolResult.error("ViewImage: $path not found.")

        ctx.readFiles += path

        return ToolResult.multimodal(
            text = "Showing $path (${bytes.size} bytes, $mime).",
            images = listOf(ImageAttachment(bytes, mime, path))
        )
    }

    private fun guessMime(path: String): String? {
        val ext = path.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            else -> null
        }
    }
}
