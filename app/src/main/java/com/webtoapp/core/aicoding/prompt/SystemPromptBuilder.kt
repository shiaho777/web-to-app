package com.webtoapp.core.aicoding.prompt

import com.webtoapp.core.aicoding.prompt.sections.BehaviorSection
import com.webtoapp.core.aicoding.prompt.sections.EnvironmentSection
import com.webtoapp.core.aicoding.prompt.sections.IdentitySection
import com.webtoapp.core.aicoding.prompt.sections.PlanModeSection
import com.webtoapp.core.aicoding.prompt.sections.ProjectFilesSection
import com.webtoapp.core.aicoding.prompt.sections.SkillCatalogSection
import com.webtoapp.core.aicoding.prompt.sections.ToolUsageSection
import com.webtoapp.core.aicoding.skill.Skill
import com.webtoapp.core.aicoding.tool.Tool
import com.webtoapp.core.i18n.AppLanguage

object SystemPromptBuilder {

    data class Input(
        val language: AppLanguage,
        val modelName: String,
        val sessionDir: String,
        val tools: List<Tool>,
        val projectFiles: List<ProjectFilesSection.FileSummary>,

        val skills: List<Skill> = emptyList(),
        val planMode: PlanMode? = null
    )

    data class PlanMode(
        val planFilePath: String,
        val planExists: Boolean
    )

    fun build(input: Input): String {
        val lang = input.language.toPromptLang()
        val sections = buildList {
            add(IdentitySection.build(lang))
            add(BehaviorSection.build(lang))
            add(ToolUsageSection.build(lang, input.tools))
            add(EnvironmentSection.build(lang, input.modelName, input.sessionDir, skillName = null))
            add(SkillCatalogSection.build(lang, input.skills))
            add(ProjectFilesSection.build(lang, input.projectFiles))
            input.planMode?.let { add(PlanModeSection.build(lang, it.planFilePath, it.planExists)) }
        }
        return sections
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
            .trimEnd()
    }
}
