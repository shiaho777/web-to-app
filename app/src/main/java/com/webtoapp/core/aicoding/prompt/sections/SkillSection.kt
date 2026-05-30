package com.webtoapp.core.aicoding.prompt.sections

import com.webtoapp.core.aicoding.prompt.PromptLang
import com.webtoapp.core.aicoding.skill.Skill

internal object SkillSection {
    fun build(lang: PromptLang, skill: Skill?, arguments: String): String {
        if (skill == null) {
            return when (lang) {
                PromptLang.EN -> "# Skill\n(no skill active — operate as a general coding assistant)"
                PromptLang.ZH -> "# 技能\n（未激活技能 — 作为通用编程助手工作）"
            }
        }
        val header = if (lang == PromptLang.EN) {
            "# Skill: ${skill.name}\n${skill.description}"
        } else {
            "# 技能：${skill.name}\n${skill.description}"
        }
        val body = skill.resolvePromptText(arguments).trim()
        return "$header\n\n$body"
    }
}
