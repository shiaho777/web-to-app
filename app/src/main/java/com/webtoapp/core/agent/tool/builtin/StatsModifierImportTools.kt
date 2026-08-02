package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.appmodifier.AppCloner
import com.webtoapp.core.appmodifier.AppFilterType
import com.webtoapp.core.appmodifier.AppListProvider
import com.webtoapp.core.appmodifier.AppModifyConfig
import com.webtoapp.core.appmodifier.AppModifyResult
import com.webtoapp.core.appmodifier.InstalledAppInfo
import com.webtoapp.core.stats.AppHealthMonitor
import com.webtoapp.core.stats.AppStatsRepository
import com.webtoapp.core.stats.BatchImportService
import com.webtoapp.data.model.WebApp
import kotlinx.coroutines.flow.first
import org.koin.java.KoinJavaComponent

class GetUsageStatsTool : Tool {
    override val name = "GetUsageStats"
    override val description = """
        Get usage statistics for apps: overall totals, or per-app stats (launch count, total usage time).
        Pass appId for a single app's stats.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "Get stats for a single app only.")
    }
    override fun isReadOnly() = true
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val repo = KoinJavaComponent.get<AppStatsRepository>(AppStatsRepository::class.java, null, null)
        val appId = args.get("appId")?.asLong
        if (appId != null) {
            val stats = repo.allStats.first().firstOrNull { it.appId == appId }
                ?: return ToolResult.ok("No usage stats for app $appId.")
            return ToolResult.ok("App $appId: launches=${stats.launchCount}, totalUsage=${stats.totalUsageMs}ms, lastSession=${stats.lastSessionDurationMs}ms")
        }
        val overall = repo.getOverallStats()
        return ToolResult.ok(buildString {
            appendLine("Overall: launches=${overall.totalLaunchCount}, totalUsage=${overall.formattedTotalUsage}, activeApps=${overall.activeAppCount}, avgSession=${overall.formattedAvgSession}")
        }.trimEnd())
    }
}

class CheckAppHealthTool : Tool {
    override val name = "CheckAppHealth"
    override val description = """
        Check URL health (online/offline/slow) for one or all web apps via HTTP HEAD.
        Pass appId for a single app; omit to check all web apps with URLs.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "Check a single app only.")
    }
    override fun isReadOnly() = true
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val repo = KoinJavaComponent.get<AppStatsRepository>(AppStatsRepository::class.java, null, null)
        val monitor = AppHealthMonitor.getInstance(ctx.androidContext, repo)
        val appId = args.get("appId")?.asLong
        if (appId != null) {
            val app = ctx.appRepository.getWebApp(appId) ?: return ToolResult.error("CheckAppHealth: app $appId not found.")
            val url = app.url.takeIf { it.startsWith("http") } ?: return ToolResult.error("CheckAppHealth: app has no http URL.")
            val record = monitor.checkUrl(appId, url)
            return ToolResult.ok("App $appId: status=${record.status}, code=${record.httpStatusCode}, responseTime=${record.responseTimeMs}ms")
        }
        val apps = ctx.appRepository.allWebApps.first()
        monitor.checkApps(apps)
        val records = repo.getAllLatestHealthRecords().first()
        if (records.isEmpty()) return ToolResult.ok("No health records. No web apps with http URLs found.")
        val lines = records.joinToString("\n") { r ->
            "- appId=${r.appId} status=${r.status} code=${r.httpStatusCode} time=${r.responseTimeMs}ms"
        }
        return ToolResult.ok("${records.size} health record(s):\n$lines")
    }
}

class ListInstalledAppsTool : Tool {
    override val name = "ListInstalledApps"
    override val description = """
        List installed apps on the device (for the App Modifier feature). Returns package name, name, and size.
        Use this before cloning or modifying an installed app.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        enum("filter", listOf("ALL", "USER", "SYSTEM"), "Filter app type. Default: USER.")
        string("query", "Search by app name or package name.")
    }
    override fun isReadOnly() = true
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val provider = AppListProvider(ctx.androidContext)
        val filterStr = args.get("filter")?.asString ?: "USER"
        val filter = runCatching { AppFilterType.valueOf(filterStr) }.getOrDefault(AppFilterType.USER)
        val query = args.get("query")?.asString ?: ""
        val apps = provider.getInstalledApps(filter, query)
        if (apps.isEmpty()) return ToolResult.ok("No installed apps found.")
        val lines = apps.take(50).joinToString("\n") { a ->
            "- ${a.packageName}  \"${a.appName}\"  ${a.formattedSize}"
        }
        return ToolResult.ok("${apps.size} app(s)" + (if (apps.size > 50) " (showing first 50)" else "") + ":\n$lines")
    }
}

class CloneAppTool : Tool {
    override val name = "CloneApp"
    override val description = """
        Clone an installed app: create a modified copy with a new name/icon and install it.
        The clone gets a new package name and is re-signed. Cannot use a custom icon with clone mode.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        string("packageName", "Package name of the app to clone.", required = true)
        string("newName", "New display name for the clone.")
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("packageName")?.asString?.let { "Cloning app $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val pkg = args.get("packageName")?.asString ?: return ToolResult.error("CloneApp: missing `packageName`.")
        val newName = args.get("newName")?.asString ?: ""
        val provider = AppListProvider(ctx.androidContext)
        val info = provider.getAppInfo(pkg) ?: return ToolResult.error("CloneApp: app $pkg not found.")
        val config = AppModifyConfig(originalApp = info, newAppName = newName.ifBlank { info.appName })
        val cloner = AppCloner(ctx.androidContext)
        return when (val result = cloner.cloneAndInstall(config)) {
            is AppModifyResult.CloneSuccess -> ToolResult.ok("Clone built and installer launched: ${result.apkPath}")
            is AppModifyResult.Error -> ToolResult.error("CloneApp failed: ${result.message}")
            else -> ToolResult.ok("Clone result: $result")
        }
    }
}

class BatchImportAppsTool : Tool {
    override val name = "BatchImportApps"
    override val description = """
        Parse a text block of app names and URLs (one per line, supports Markdown links, pipe/tab/comma
        separators, bare URLs) and import them as new web apps. Skips URLs that already exist.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        string("text", "Text to parse and import (one app per line).", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? = "Batch importing apps"
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val text = args.get("text")?.asString ?: return ToolResult.error("BatchImportApps: missing `text`.")
        val service = BatchImportService(ctx.androidContext, ctx.appRepository)
        val parseResult = service.parseTextDetailed(text)
        if (parseResult.entries.isEmpty()) {
            return ToolResult.error("BatchImportApps: no valid app entries found. Invalid lines: ${parseResult.invalidLineCount}")
        }
        val importResult = service.importEntriesDetailed(parseResult.entries)
        return ToolResult.ok("Imported ${importResult.imported} app(s). Skipped ${importResult.skippedDuplicate} duplicate(s), ${importResult.skippedInvalid} invalid.")
    }
}

class ExportAppTemplateTool : Tool {
    override val name = "ExportAppTemplate"
    override val description = """
        Export an app's configuration as a shareable JSON template (can be re-imported via BatchImportApps
        or shared with others). Returns the template JSON.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id to export as template.", required = true)
    }
    override fun isReadOnly() = true
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong ?: return ToolResult.error("ExportAppTemplate: missing `appId`.")
        val app = ctx.appRepository.getWebApp(appId) ?: return ToolResult.error("ExportAppTemplate: app $appId not found.")
        val service = BatchImportService(ctx.androidContext, ctx.appRepository)
        val template = service.exportAsTemplate(app)
        return ToolResult.ok("Template for \"${app.name}\":\n$template")
    }
}
