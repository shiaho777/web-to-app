package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.todo.TodoManager
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult

class TodoWriteTool : Tool {
    override val name = "TodoWrite"
    override val description = """
        Create or replace the user-visible checklist for this turn.
        - Use at the start of any task with three or more steps.
        - Each item is a short imperative subject. Optional initial status.
        - Status values: pending | in_progress | completed.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        val itemSchema = JsonObject().apply {
            addProperty("type", "object")
            add("properties", JsonObject().apply {
                add("subject", JsonObject().apply { addProperty("type", "string") })
                add("status", JsonObject().apply {
                    addProperty("type", "string")
                    add("enum", JsonArray().apply { add("pending"); add("in_progress"); add("completed") })
                })
            })
            add("required", JsonArray().apply { add("subject") })
        }
        array(
            name = "todos",
            itemsSchema = itemSchema,
            description = "List of todo items in execution order",
            required = true
        )
    }

    override fun activityDescription(args: JsonObject): String = "Updating checklist"

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val arr = args.getAsJsonArray("todos") ?: return ToolResult.error("TodoWrite: missing `todos`.")
        if (arr.size() == 0) return ToolResult.error("TodoWrite: empty list.")
        val parsed = arr.mapNotNull { el ->
            val obj = el as? JsonObject ?: return@mapNotNull null
            val subject = obj.get("subject")?.asString?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val status = parseStatus(obj.get("status")?.asString)
            subject to status
        }
        if (parsed.isEmpty()) return ToolResult.error("TodoWrite: no valid items.")
        ctx.todos.replaceAll(parsed)
        return ToolResult.ok("Wrote ${parsed.size} todo item(s).")
    }
}

class TodoUpdateTool : Tool {
    override val name = "TodoUpdate"
    override val description = """
        Update one checklist item.
        - `id` is the item's number as shown by TodoWrite (1, 2, …).
        - Provide `status` and/or `subject`. Status: pending | in_progress | completed.
        - Mark in_progress when starting, completed when done. Don't skip steps.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("id", "Item id from TodoWrite", required = true)
        enum("status", listOf("pending", "in_progress", "completed"), "New status (optional)")
        string("subject", "New subject text (optional)")
    }

    override fun activityDescription(args: JsonObject): String? {
        val id = args.get("id")?.asString ?: return null
        val status = args.get("status")?.asString
        return if (status == "in_progress") "Working on todo #$id" else "Updating todo #$id"
    }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val id = args.get("id")?.asString ?: return ToolResult.error("TodoUpdate: missing `id`.")
        val newStatus = args.get("status")?.asString?.let(::parseStatus)
        val newSubject = args.get("subject")?.asString?.takeIf { it.isNotBlank() }
        val updated = ctx.todos.update(id, subject = newSubject, status = newStatus)
            ?: return ToolResult.error("TodoUpdate: todo #$id not found.")
        return ToolResult.ok("Todo #${updated.id} -> [${updated.status.name.lowercase()}] ${updated.subject}")
    }
}

private fun parseStatus(raw: String?): TodoManager.Item.Status = when (raw?.lowercase()) {
    "in_progress" -> TodoManager.Item.Status.InProgress
    "completed" -> TodoManager.Item.Status.Completed
    else -> TodoManager.Item.Status.Pending
}
