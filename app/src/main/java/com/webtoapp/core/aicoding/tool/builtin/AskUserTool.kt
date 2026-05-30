package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.permission.ChoiceRequest
import com.webtoapp.core.aicoding.permission.ChoiceResponse
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult
import java.util.UUID

class AskUserTool : Tool {
    override val name = "AskUserQuestion"
    override val description = """
        Ask the user one to four multiple-choice clarifying questions during a turn.
        - Each question must have 2 to 4 options (we always append "Other" for free text).
        - Pass `multi_select: true` to allow more than one answer for that question.
        - If you would recommend an option, list it first and append " (Recommended)" to its label.
        - Don't use this to ask "is this plan ok?" or "should I continue?" — use plan mode for those.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        val optionItem = JsonObject().apply {
            addProperty("type", "object")
            add("properties", JsonObject().apply {
                add("label", JsonObject().apply { addProperty("type", "string") })
                add("description", JsonObject().apply { addProperty("type", "string") })
            })
            add("required", JsonArray().apply { add("label") })
        }
        val questionItem = JsonObject().apply {
            addProperty("type", "object")
            add("properties", JsonObject().apply {
                add("question", JsonObject().apply { addProperty("type", "string") })
                add("options", JsonObject().apply {
                    addProperty("type", "array")
                    add("items", optionItem)
                    addProperty("minItems", 2)
                    addProperty("maxItems", 4)
                })
                add("multi_select", JsonObject().apply {
                    addProperty("type", "boolean")
                    addProperty("default", false)
                })
            })
            add("required", JsonArray().apply { add("question"); add("options") })
        }
        array(
            name = "questions",
            itemsSchema = questionItem,
            description = "Up to 4 multi-choice questions",
            minItems = 1,
            maxItems = 4,
            required = true
        )
    }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject): String = "Asking the user…"

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val rawQs = args.getAsJsonArray("questions") ?: return ToolResult.error("AskUserQuestion: missing `questions`.")
        if (rawQs.isEmpty) return ToolResult.error("AskUserQuestion: `questions` is empty.")

        val parsed = rawQs.mapNotNull { qEl ->
            val q = qEl as? JsonObject ?: return@mapNotNull null
            val text = q.get("question")?.asString?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val opts = q.getAsJsonArray("options")?.mapNotNull { oEl ->
                val o = oEl as? JsonObject ?: return@mapNotNull null
                val label = o.get("label")?.asString?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val desc = o.get("description")?.asString.orEmpty()
                ChoiceRequest.Option(label, desc)
            } ?: return@mapNotNull null
            if (opts.size < 2) return@mapNotNull null
            ChoiceRequest.Question(
                text = text,
                options = opts,
                multiSelect = q.get("multi_select")?.asBoolean ?: false,
                allowOther = true
            )
        }
        if (parsed.isEmpty()) return ToolResult.error("AskUserQuestion: no valid questions.")

        val req = ChoiceRequest(id = "ask-${UUID.randomUUID()}", questions = parsed)
        return when (val resp = ctx.prompter.askChoice(req)) {
            is ChoiceResponse.Cancelled -> ToolResult.error("User dismissed the question.")
            is ChoiceResponse.Answered -> {
                val lines = parsed.zip(resp.answers).map { (q, ans) ->
                    val joined = if (ans.isEmpty()) "(no selection)" else ans.joinToString(", ")
                    "Q: ${q.text}\nA: $joined"
                }
                ToolResult.ok(lines.joinToString("\n\n"))
            }
        }
    }
}
