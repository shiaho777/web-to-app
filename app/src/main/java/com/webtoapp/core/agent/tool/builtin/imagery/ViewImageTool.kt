package com.webtoapp.core.agent.tool.builtin.imagery

import android.graphics.Bitmap
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.ImageAttachment
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.agent.tool.builtin.jsonSchema
import com.webtoapp.util.BoundedBitmaps
import java.io.ByteArrayOutputStream

class ViewImageTool : Tool {
    override val name = "ViewImage"
    override val description = """
        Load an image from the project so you can SEE it. Use this for:
        - Re-checking an image you generated earlier this session.
        - Looking at a reference image the user attached.
        - Comparing two images for style consistency before placing them in HTML.
        Path is project-relative (e.g. `assets/hero.png`). Oversized images are
        downscaled automatically to fit vision-input limits.
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
        var mime = guessMime(path) ?: return ToolResult.error(
            "ViewImage: $path doesn't look like an image (expected .png/.jpg/.webp/.gif)."
        )
        val file = ctx.fileManager.resolveSafe(ctx.sessionId, path)
        if (file == null || !file.isFile) {
            return ToolResult.error("ViewImage: $path not found.")
        }

        // Read the whole file only when it fits inline-vision limits; anything
        // bigger would OOM the app long before the model could see it, so those
        // go through BoundedBitmaps' sample-and-decode path (required by
        // NoUnboundedBitmapDecodeTest) and get re-encoded as JPEG instead.
        val bytes: ByteArray
        var note = ""
        if (file.length() <= MAX_RAW_BYTES) {
            bytes = try {
                file.readBytes()
            } catch (e: Exception) {
                return ToolResult.error("ViewImage: failed to read $path: ${e.message}")
            }
        } else {
            val bmp = BoundedBitmaps.decodeBoundedBitmapFile(file.absolutePath, MAX_DIMENSION)
                ?: return ToolResult.error(
                    "ViewImage: $path is ${formatBytes(file.length())} but could not be " +
                        "decoded as an image."
                )
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            bmp.recycle()
            bytes = baos.toByteArray()
            note = " (downscaled from ${formatBytes(file.length())} to fit vision input)"
            mime = "image/jpeg"
        }

        ctx.readFiles += path

        return ToolResult.multimodal(
            text = "Showing $path (${bytes.size} bytes, $mime)$note.",
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

    private fun formatBytes(bytes: Long): String = when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        bytes < 1024L * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
        else -> "%.1fGB".format(bytes / (1024.0 * 1024 * 1024))
    }

    companion object {
        /** Above this, decode through BitmapFactory sampling instead of raw bytes. */
        private const val MAX_RAW_BYTES = 20L * 1024 * 1024

        /** Long-edge cap for downscaled vision input. */
        private const val MAX_DIMENSION = 2048
    }
}
