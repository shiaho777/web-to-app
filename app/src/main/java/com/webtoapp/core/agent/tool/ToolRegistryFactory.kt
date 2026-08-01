package com.webtoapp.core.agent.tool

import com.webtoapp.core.agent.imagery.ImageGeneratorRegistry
import com.webtoapp.core.agent.plan.PlanManager
import com.webtoapp.core.agent.tool.builtin.AskUserTool
import com.webtoapp.core.agent.tool.builtin.CreateAppTool
import com.webtoapp.core.agent.tool.builtin.DeleteFileTool
import com.webtoapp.core.agent.tool.builtin.EditFileTool
import com.webtoapp.core.agent.tool.builtin.EnterPlanModeTool
import com.webtoapp.core.agent.tool.builtin.ExitPlanModeTool
import com.webtoapp.core.agent.tool.builtin.GetAppTool
import com.webtoapp.core.agent.tool.builtin.GlobTool
import com.webtoapp.core.agent.tool.builtin.GrepTool
import com.webtoapp.core.agent.tool.builtin.ListAppsTool
import com.webtoapp.core.agent.tool.builtin.ListFilesTool
import com.webtoapp.core.agent.tool.builtin.ReadFileTool
import com.webtoapp.core.agent.tool.builtin.TodoUpdateTool
import com.webtoapp.core.agent.tool.builtin.TodoWriteTool
import com.webtoapp.core.agent.tool.builtin.UpdateAppTool
import com.webtoapp.core.agent.tool.builtin.WriteFileTool
import com.webtoapp.core.agent.tool.builtin.imagery.GenerateImageTool
import com.webtoapp.core.agent.tool.builtin.imagery.ListImagesTool
import com.webtoapp.core.agent.tool.builtin.imagery.ViewImageTool

class ToolRegistryFactory(
    private val planManager: PlanManager,
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
        ListAppsTool(),
        GetAppTool(),
        CreateAppTool(),
        UpdateAppTool(),
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
