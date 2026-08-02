package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.ImageAttachment
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.logging.AppLogger
import java.io.File

class ReadAppFileTool : Tool {
    override val name = "ReadAppFile"
    override val description = """
        Read a file or list a directory from the app's real data storage (outside the session
        sandbox). Use this to inspect an existing app's source files, assets, or icons — for
        example the HTML files of an HTML app, or an app's icon image.
        - `path` is relative to the app data root, e.g. `html_projects/<projectId>/index.html`
          or `app_icons/icon_xxx.png`. Use GetApp first to find the projectId / iconPath.
        - Text files return content with line numbers (offset/limit supported).
        - Image files (png/jpg/gif/webp) are returned as images the model can see (if multimodal).
        - Directories return a recursive file listing.
        - Only app-data directories are readable (html_projects, nodejs_projects, php_projects,
          python_projects, go_projects, wordpress_projects, frontend_builds, scraped_sites,
          sample_projects, app_icons, media_apps, gallery_apps, splash_media, bgm,
          extension_modules, extensions, user_scripts). Sensitive dirs (credentials, encrypted,
          caches) are blocked.
        - This tool is read-only and cannot modify app data.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("path", "Path relative to the app data root (e.g. html_projects/<id>/index.html)", required = true)
        integer("offset", "Line index to start from for text files (0-based)", default = 0)
        integer("limit", "Maximum lines to return for text files", default = 2000)
    }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject): String? =
        args.get("path")?.asString?.let { "Reading app file $it" }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val rawPath = args.get("path")?.asString
            ?: return ToolResult.error("ReadAppFile: missing `path`.")
        val offset = args.get("offset")?.asInt?.coerceAtLeast(0) ?: 0
        val limit = args.get("limit")?.asInt?.coerceIn(1, 10_000) ?: 2000

        val cleaned = sanitizePath(rawPath)
            ?: return ToolResult.error("ReadAppFile: invalid path `$rawPath`.")

        val whitelisted = WHITELIST.any { cleaned == it.removeSuffix("/") || cleaned.startsWith(it) }
        if (!whitelisted) {
            return ToolResult.error(
                "ReadAppFile: `$cleaned` is outside the allowed app-data directories. " +
                    "Readable dirs: ${WHITELIST.joinToString(", ")}."
            )
        }

        val root = ctx.androidContext.filesDir
        val target = File(root, cleaned)
        val safeTarget = verifyWithinRoot(target, root)
            ?: return ToolResult.error("ReadAppFile: path escapes the app data root.")

        if (!safeTarget.exists()) {
            return ToolResult.error("ReadAppFile: `$cleaned` does not exist.")
        }

        return when {
            safeTarget.isDirectory -> listDirectory(safeTarget, cleaned)
            isImageFile(cleaned) -> readImage(safeTarget, cleaned)
            else -> readTextFile(safeTarget, cleaned, offset, limit, ctx)
        }
    }

    private fun sanitizePath(raw: String): String? {
        val cleaned = raw.trim().trimStart('/').trim('\\').replace('\\', '/')
        if (cleaned.isEmpty() || cleaned.length > 500) return null
        if (cleaned.contains("..")) return null
        if (cleaned.contains(':')) return null
        if (cleaned.startsWith('~')) return null
        return cleaned
    }

    private fun verifyWithinRoot(target: File, root: File): File? {
        return try {
            val canonRoot = root.canonicalPath
            val canonTarget = target.canonicalPath
            if (canonTarget == canonRoot || canonTarget.startsWith(canonRoot + File.separator)) target
            else null
        } catch (e: Exception) {
            AppLogger.w("ReadAppFile", "path check failed: ${e.message}")
            null
        }
    }

    private fun isImageFile(path: String): Boolean {
        val ext = path.substringAfterLast('.', "").lowercase()
        return ext in IMAGE_EXTENSIONS
    }

    private fun readImage(file: File, displayPath: String): ToolResult {
        return try {
            val bytes = file.readBytes()
            val mime = when (displayPath.substringAfterLast('.', "").lowercase()) {
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> "image/png"
            }
            ToolResult.multimodal(
                text = "Image at $displayPath (${formatSize(bytes.size)})",
                images = listOf(ImageAttachment(bytes, mime, displayPath))
            )
        } catch (e: Exception) {
            ToolResult.error("ReadAppFile: failed to read image $displayPath: ${e.message}")
        }
    }

    private fun readTextFile(
        file: File,
        displayPath: String,
        offset: Int,
        limit: Int,
        ctx: ToolContext
    ): ToolResult {
        return try {
            val content = file.readText()
            val lines = content.lines()
            if (offset >= lines.size) {
                return ToolResult.ok(
                    "(empty range — $displayPath has ${lines.size} lines, offset=$offset)"
                )
            }
            val window = lines.drop(offset).take(limit)
            val sb = StringBuilder()
            for ((i, line) in window.withIndex()) {
                if (sb.isNotEmpty()) sb.append('\n')
                sb.append(offset + i + 1).append('\t').append(line)
            }
            val tail = if (offset + limit < lines.size) {
                "\n… (${lines.size - offset - limit} more lines, raise `limit` or set `offset`)"
            } else ""
            ToolResult.ok(sb.toString() + tail)
        } catch (e: Exception) {
            ToolResult.error("ReadAppFile: failed to read $displayPath: ${e.message}")
        }
    }

    private fun listDirectory(dir: File, displayPath: String): ToolResult {
        return try {
            val files = dir.walkTopDown().take(200).map { f ->
                val rel = f.relativeTo(dir).path.replace(File.separatorChar, '/')
                val type = if (f.isDirectory) "dir" else "${f.length()} bytes"
                "$type\t$rel"
            }.toList()
            if (files.isEmpty()) {
                ToolResult.ok("$displayPath is empty.")
            } else {
                ToolResult.ok("${files.size} entries in $displayPath:\n${files.joinToString("\n")}")
            }
        } catch (e: Exception) {
            ToolResult.error("ReadAppFile: failed to list $displayPath: ${e.message}")
        }
    }

    private fun formatSize(bytes: Int): String = when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        else -> "${bytes / (1024 * 1024)}MB"
    }

    companion object {
        private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "webp")

        private val WHITELIST = listOf(
            "html_projects/",
            "frontend_builds/",
            "nodejs_projects/",
            "php_projects/",
            "python_projects/",
            "go_projects/",
            "wordpress_projects/",
            "scraped_sites/",
            "sample_projects/",
            "app_icons/",
            "media_apps/",
            "gallery_apps/",
            "splash_media/",
            "bgm/",
            "extension_modules/",
            "extensions/",
            "user_scripts/"
        )
    }
}
