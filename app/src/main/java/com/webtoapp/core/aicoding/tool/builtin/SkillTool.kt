package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.aicoding.tool.ToolContext
import com.webtoapp.core.aicoding.tool.ToolResult

class SkillTool : Tool {
    override val name = "Skill"
    override val description = """
        Load a skill's full instructions on demand. Pass the skill `name`
        (without leading `/`) and any free-text `arguments`. The tool
        returns the skill's Markdown body, which you should follow on
        your next turn.

        Use this whenever a user request matches a skill's `when_to_use`
        — you don't need permission, the user, or a prior message to
        invoke it. Calling unknown skills returns an error; check the
        Available Skills list in the system prompt for valid names.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        string("name", "Skill name (without leading slash), e.g. \"html-app\"", required = true)
        string("arguments", "Free-text arguments forwarded to the skill body's \${ARGUMENTS} placeholder. Empty string means no args.", required = false)
    }

    override fun isReadOnly(): Boolean = true

    override fun activityDescription(args: JsonObject): String {
        val name = args.get("name")?.takeIf { !it.isJsonNull }?.asString
        return if (name != null) "Loading skill /$name" else "Loading skill"
    }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val skillName = args.get("name")?.takeIf { !it.isJsonNull }?.asString?.trim()
            ?: return ToolResult.error("Skill: missing `name`.")
        val registry = ctx.skillRegistry
            ?: return ToolResult.error("Skill: registry not wired into this context.")
        val skill = registry.get(skillName)
            ?: return ToolResult.error("Skill: no skill named '$skillName'. Check the Available Skills list.")

        val arguments = args.get("arguments")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
        val body = skill.resolvePromptText(arguments).trim()
        if (body.isEmpty()) {
            return ToolResult.error("Skill: '$skillName' has an empty body.")
        }

        val text = buildString {
            append("# Skill activated: /")
            append(skillName)
            append('\n')
            append(skill.description)
            append("\n\n")
            append(body)
        }
        return ToolResult.ok(text)
    }
}
