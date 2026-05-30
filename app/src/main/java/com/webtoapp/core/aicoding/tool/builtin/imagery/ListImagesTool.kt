package com.webtoapp.core.aicoding.tool.builtin.imagery

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult
import com.webtoapp.core.aicoding.tool.builtin.jsonSchema

class ListImagesTool : Tool {
    override val name = "ListImages"
    override val description = """
        List all image files in the current project (.png/.jpg/.webp/.gif).
        Returns paths and sizes. Pair with ViewImage when you need to see the actual pixels.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {  }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject) = "Listing images"

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val files = ctx.fileManager.listAll(ctx.sessionId).filter {
            val ext = it.relativePath.substringAfterLast('.', "").lowercase()
            ext in IMAGE_EXTS
        }
        if (files.isEmpty()) return ToolResult.ok("(no images in project)")
        val rows = files.joinToString("\n") { "${it.relativePath}\t${it.formatSize()}" }
        return ToolResult.ok(rows)
    }

    companion object {
        private val IMAGE_EXTS = setOf("png", "jpg", "jpeg", "webp", "gif")
    }
}
