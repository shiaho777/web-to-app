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
        - Each item is a short imperative subject. Optional initial status and stable id.
        - Status values: pending | in_progress | completed.
        - Prefer keeping one in_progress item at a time.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        val itemSchema = JsonObject().apply {
            addProperty("type", "object")
            add("properties", JsonObject().apply {
                add("id", JsonObject().apply { addProperty("type", "string") })
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

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject): String = "Updating checklist"

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val arr = args.getAsJsonArray("todos") ?: return ToolResult.error("TodoWrite: missing `todos`.")
        if (arr.size() == 0) return ToolResult.error("TodoWrite: empty list.")
        val parsed = arr.mapNotNull { el ->
            val obj = el as? JsonObject ?: return@mapNotNull null
            val subject = obj.get("subject")?.asString?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val status = parseStatus(obj.get("status")?.asString)
            val id = obj.get("id")?.asString?.takeIf { it.isNotBlank() }
            TodoManager.Draft(id = id, subject = subject, status = status)
        }
        if (parsed.isEmpty()) return ToolResult.error("TodoWrite: no valid items.")
        val written = ctx.todos.replaceDrafts(parsed)
        val lines = written.joinToString("\n") { "#${it.id} [${it.status.name.lowercase()}] ${it.subject}" }
        return ToolResult.ok("Wrote ${written.size} todo item(s):\n$lines")
    }
}

class TodoUpdateTool : Tool {
    override val name = "TodoUpdate"
    override val description = """
        Update one checklist item.
        - `id` is the item id from TodoWrite (1, 2, … or a custom id).
        - Provide `status` and/or `subject`. Status: pending | in_progress | completed.
        - Mark in_progress when starting, completed when done. Don't skip steps.
        - If the id is unknown, pass the exact subject to match instead.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("id", "Item id from TodoWrite", required = false)
        string("subject", "Subject to match or new subject text", required = false)
        enum("status", listOf("pending", "in_progress", "completed"), "New status (optional)")
    }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject): String? {
        val id = args.get("id")?.asString
        val status = args.get("status")?.asString
        return when {
            status == "in_progress" && id != null -> "Working on todo #$id"
            id != null -> "Updating todo #$id"
            else -> "Updating checklist"
        }
    }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val id = args.get("id")?.asString?.takeIf { it.isNotBlank() }
        val subjectArg = args.get("subject")?.asString?.takeIf { it.isNotBlank() }
        val newStatus = args.get("status")?.asString?.let(::parseStatus)
        if (id == null && subjectArg == null && newStatus == null) {
            return ToolResult.error("TodoUpdate: provide id/subject and/or status.")
        }
        val updated = ctx.todos.updateFlexible(
            id = id,
            matchSubject = if (id == null) subjectArg else null,
            subject = if (id != null) subjectArg else null,
            status = newStatus
        ) ?: return ToolResult.error(
            "TodoUpdate: item not found. Current: " + ctx.todos.snapshotSummary()
        )
        return ToolResult.ok("Todo #${updated.id} -> [${updated.status.name.lowercase()}] ${updated.subject}")
    }
}

private fun parseStatus(raw: String?): TodoManager.Item.Status = when (raw?.lowercase()) {
    "in_progress", "in-progress", "doing", "active" -> TodoManager.Item.Status.InProgress
    "completed", "complete", "done" -> TodoManager.Item.Status.Completed
    else -> TodoManager.Item.Status.Pending
}
