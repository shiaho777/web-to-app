package com.webtoapp.core.aicoding.prompt.sections

import com.webtoapp.core.aicoding.prompt.PromptLang
import com.webtoapp.core.aicoding.skill.Skill

internal object SkillCatalogSection {

    fun build(lang: PromptLang, skills: List<Skill>): String {
        val visible = skills
            .filterNot { it.hidden }
            .sortedWith(
                compareByDescending<Skill> { it.pinned }
                    .thenBy { it.source.ordinal }
                    .thenBy { it.name }
            )
        if (visible.isEmpty()) return ""

        val header = when (lang) {
            PromptLang.EN -> "# Available Skills"
            PromptLang.ZH -> "# 可用技能"
        }
        val intro = when (lang) {
            PromptLang.EN ->
                "Each skill is a reusable workflow. When a user request matches a skill's `When to use`, " +
                    "call the `Skill` tool with that skill's `name` (no leading slash) to load its full " +
                    "instructions, then follow them on the next turn. You may also chain multiple skills " +
                    "across a conversation — they aren't exclusive."
            PromptLang.ZH ->
                "每个技能是一段可复用的工作流。当用户的请求匹配某个技能的「适用场景」时，调用 " +
                    "`Skill` 工具传入对应的 `name`（不带斜杠），即可加载完整指令，下一轮按其执行。" +
                    "技能之间不互斥，单个会话里可以串联多个。"
        }

        val rows = visible.joinToString("\n") { s ->
            buildString {
                append("- ")
                append(s.name)
                if (s.description.isNotBlank()) {
                    append(": ")
                    append(s.description.lineSequence().first().trim())
                }
                if (s.whenToUse.isNotBlank()) {
                    append(" — ")
                    append(s.whenToUse.lineSequence().first().trim())
                }
            }
        }

        return "$header\n$intro\n\n$rows"
    }
}
