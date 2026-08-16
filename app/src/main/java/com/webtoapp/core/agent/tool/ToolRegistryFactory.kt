package com.webtoapp.core.agent.tool

import com.webtoapp.core.agent.imagery.ImageGeneratorRegistry
import com.webtoapp.core.agent.plan.PlanManager
import com.webtoapp.core.agent.tool.builtin.AskUserTool
import com.webtoapp.core.agent.tool.builtin.BatchImportAppsTool
import com.webtoapp.core.agent.tool.builtin.BuildApkTool
import com.webtoapp.core.agent.tool.builtin.CheckPlayPolicyTool
import com.webtoapp.core.agent.tool.builtin.CheckAppHealthTool
import com.webtoapp.core.agent.tool.builtin.ClearAppCacheTool
import com.webtoapp.core.agent.tool.builtin.ClearRuntimeCacheTool
import com.webtoapp.core.agent.tool.builtin.CreateAppTool
import com.webtoapp.core.agent.tool.builtin.CreateModuleTool
import com.webtoapp.core.agent.tool.builtin.CreateShortcutTool
import com.webtoapp.core.agent.tool.builtin.DeleteAppTool
import com.webtoapp.core.agent.tool.builtin.DeleteEngineTool
import com.webtoapp.core.agent.tool.builtin.DeleteFileTool
import com.webtoapp.core.agent.tool.builtin.CloneAppTool
import com.webtoapp.core.agent.tool.builtin.DuplicateAppTool
import com.webtoapp.core.agent.tool.builtin.EditFileTool
import com.webtoapp.core.agent.tool.builtin.EnterPlanModeTool
import com.webtoapp.core.agent.tool.builtin.ExitPlanModeTool
import com.webtoapp.core.agent.tool.builtin.ExportAabTool
import com.webtoapp.core.agent.tool.builtin.ExportAppTemplateTool
import com.webtoapp.core.agent.tool.builtin.ExportAppTool
import com.webtoapp.core.agent.tool.builtin.GetUsageStatsTool
import com.webtoapp.core.agent.tool.builtin.GetAdBlockStatusTool
import com.webtoapp.core.agent.tool.builtin.GetBuildEnvStatusTool
import com.webtoapp.core.agent.tool.builtin.GetAppTool
import com.webtoapp.core.agent.tool.builtin.InitializeBuildEnvTool
import com.webtoapp.core.agent.tool.builtin.GetEngineStatusTool
import com.webtoapp.core.agent.tool.builtin.GetModuleTool
import com.webtoapp.core.agent.tool.builtin.GetRuntimeStatusTool
import com.webtoapp.core.agent.tool.builtin.GlobTool
import com.webtoapp.core.agent.tool.builtin.GrepTool
import com.webtoapp.core.agent.tool.builtin.InstallRuntimeTool
import com.webtoapp.core.agent.tool.builtin.KillAllPortsTool
import com.webtoapp.core.agent.tool.builtin.KillPortTool
import com.webtoapp.core.agent.tool.builtin.ListInstalledAppsTool
import com.webtoapp.core.agent.tool.builtin.ListAppsTool
import com.webtoapp.core.agent.tool.builtin.ListFilesTool
import com.webtoapp.core.agent.tool.builtin.ListModulesTool
import com.webtoapp.core.agent.tool.builtin.ManageHostsRulesTool
import com.webtoapp.core.agent.tool.builtin.MoveToCategoryTool
import com.webtoapp.core.agent.tool.builtin.ReadFileTool
import com.webtoapp.core.agent.tool.builtin.ReadAppFileTool
import com.webtoapp.core.agent.tool.builtin.ScanPortsTool
import com.webtoapp.core.agent.tool.builtin.SelectEngineTool
import com.webtoapp.core.agent.tool.builtin.ShareApkTool
import com.webtoapp.core.agent.tool.builtin.TodoUpdateTool
import com.webtoapp.core.agent.tool.builtin.TodoWriteTool
import com.webtoapp.core.agent.tool.builtin.UpdateAppTool
import com.webtoapp.core.agent.tool.builtin.ListConfigTemplatesTool
import com.webtoapp.core.agent.tool.builtin.SaveConfigTemplateTool
import com.webtoapp.core.agent.tool.builtin.ApplyConfigTemplateTool
import com.webtoapp.core.agent.tool.builtin.DeleteConfigTemplateTool
import com.webtoapp.core.agent.tool.builtin.UpdateModuleTool
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
        ReadAppFileTool(),
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
        ReadAppFileTool(),
        // Common-config templates
        ListConfigTemplatesTool(),
        SaveConfigTemplateTool(),
        ApplyConfigTemplateTool(),
        DeleteConfigTemplateTool(),
        // App lifecycle
        BuildApkTool(),
        ShareApkTool(),
        ExportAppTool(),
        CreateShortcutTool(),
        MoveToCategoryTool(),
        ClearAppCacheTool(),
        DeleteAppTool(),
        DuplicateAppTool(),
        ExportAabTool(),
        // Ports & engine
        ScanPortsTool(),
        KillPortTool(),
        KillAllPortsTool(),
        GetEngineStatusTool(),
        SelectEngineTool(),
        DeleteEngineTool(),
        // Hosts ad-block & runtime management
        GetAdBlockStatusTool(),
        ManageHostsRulesTool(),
        GetRuntimeStatusTool(),
        InstallRuntimeTool(),
        ClearRuntimeCacheTool(),
        // Stats, modifier & import
        GetUsageStatsTool(),
        CheckAppHealthTool(),
        ListInstalledAppsTool(),
        CloneAppTool(),
        BatchImportAppsTool(),
        ExportAppTemplateTool(),
        // Build env & Play policy
        GetBuildEnvStatusTool(),
        InitializeBuildEnvTool(),
        CheckPlayPolicyTool(),
        // Modules
        ListModulesTool(),
        GetModuleTool(),
        CreateModuleTool(),
        UpdateModuleTool(),
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
