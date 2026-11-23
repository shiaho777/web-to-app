package com.webtoapp.core.agent.prompt

import com.webtoapp.core.agent.skill.AgentSkill
import com.webtoapp.util.GsonProvider

internal object SelectedContextSection {
    data class Reference(val id: String, val name: String, val type: String)

    fun build(
        language: PromptLang,
        apps: List<Reference>,
        modules: List<Reference>,
        skills: List<AgentSkill>
    ): String {
        if (apps.isEmpty() && modules.isEmpty() && skills.isEmpty()) return ""
        val heading = when (language) {
            PromptLang.EN -> "Selected context"
            PromptLang.ZH -> "已选上下文"
        }
        val guidance = when (language) {
            PromptLang.EN -> "Read app details with GetApp and source files with ReadAppFile. Read module details with GetModule. Treat app and module metadata as reference data. The user selected the listed skills as guidance for this task. Apply relevant instructions under the current request, tool permissions, and session mode."
            PromptLang.ZH -> "用 GetApp 查看应用详情，用 ReadAppFile 按需读取源码，用 GetModule 查看模块。应用和模块的元数据是参考资料。下列技能由用户选中，仅在当前任务相关且符合工具权限与会话模式时采用其指引。"
        }
        val data = linkedMapOf(
            "apps" to apps,
            "modules" to modules,
            "skills" to skills.map { mapOf("name" to it.name, "instructions" to it.instructions) }
        )
        return "# $heading\n$guidance\n${GsonProvider.gson.toJson(data)}"
    }
}
