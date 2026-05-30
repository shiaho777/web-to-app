package com.webtoapp.core.aicoding.tool

import com.webtoapp.core.aicoding.imagery.ImageGeneratorRegistry
import com.webtoapp.core.aicoding.plan.PlanManager
import com.webtoapp.core.aicoding.skill.SkillRegistry
import com.webtoapp.core.aicoding.tool.builtin.AskUserTool
import com.webtoapp.core.aicoding.tool.builtin.DeleteFileTool
import com.webtoapp.core.aicoding.tool.builtin.EditFileTool
import com.webtoapp.core.aicoding.tool.builtin.EnterPlanModeTool
import com.webtoapp.core.aicoding.tool.builtin.ExitPlanModeTool
import com.webtoapp.core.aicoding.tool.builtin.GlobTool
import com.webtoapp.core.aicoding.tool.builtin.GrepTool
import com.webtoapp.core.aicoding.tool.builtin.ListFilesTool
import com.webtoapp.core.aicoding.tool.builtin.ReadFileTool
import com.webtoapp.core.aicoding.tool.builtin.SkillTool
import com.webtoapp.core.aicoding.tool.builtin.TodoUpdateTool
import com.webtoapp.core.aicoding.tool.builtin.TodoWriteTool
import com.webtoapp.core.aicoding.tool.builtin.WriteFileTool
import com.webtoapp.core.aicoding.tool.builtin.imagery.GenerateImageTool
import com.webtoapp.core.aicoding.tool.builtin.imagery.ListImagesTool
import com.webtoapp.core.aicoding.tool.builtin.imagery.ViewImageTool

class ToolRegistryFactory(
    private val planManager: PlanManager,
    @Suppress("unused")

    private val skillRegistry: SkillRegistry,
    private val imageRegistry: ImageGeneratorRegistry?
) {

    fun build(hasImageModel: Boolean): ToolRegistry {
        val base = baseTools()
        val plan = planTools()
        val imagery = if (hasImageModel && imageRegistry != null) imageryTools(imageRegistry) else emptyList()
        val all = (base + plan + imagery).distinctBy { it.name }
        return ToolRegistry(all)
    }

    private fun baseTools(): List<Tool> = listOf(
        ReadFileTool(),
        WriteFileTool(),
        EditFileTool(),
        DeleteFileTool(),
        ListFilesTool(),
        GlobTool(),
        GrepTool(),
        AskUserTool(),
        TodoWriteTool(),
        TodoUpdateTool(),
        SkillTool(),
    )

    private fun planTools(): List<Tool> = listOf(
        EnterPlanModeTool(planManager),
        ExitPlanModeTool(planManager),
    )

    private fun imageryTools(reg: ImageGeneratorRegistry): List<Tool> = listOf(
        GenerateImageTool(reg),
        ViewImageTool(),
        ListImagesTool(),
    )
}
