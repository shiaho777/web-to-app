package com.webtoapp.core.aicoding.tool

import com.google.gson.JsonElement
import com.google.gson.JsonObject

interface Tool {

    val name: String

    val description: String

    val parametersSchema: JsonElement

    fun isReadOnly(): Boolean = false

    fun activityDescription(args: JsonObject): String? = null

    suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult
}
