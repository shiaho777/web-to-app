package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.linux.LinuxEnvironmentManager
import com.webtoapp.core.playstore.PlayPolicyChecker

class GetBuildEnvStatusTool : Tool {
    override val name = "GetBuildEnvStatus"
    override val description = """
        Check the local build environment status: Node.js/npm/PHP/Composer/Python readiness,
        versions, storage used, and cache size. This environment is needed for on-device
        frontend builds (React/Vue) and PHP/Composer/Python dependency installation.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {}
    override fun isReadOnly() = true
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val mgr = LinuxEnvironmentManager.getInstance(ctx.androidContext)
        mgr.checkEnvironment()
        val info = mgr.getEnvironmentInfo()
        return ToolResult.ok(buildString {
            appendLine("Installed: ${info.isInstalled}")
            appendLine("Node: ${info.nodeVersion ?: "not installed"} (${if (info.nodeReady) "ready" else "not ready"})")
            appendLine("npm: ${info.npmVersion ?: "not installed"} (${if (info.npmReady) "ready" else "not ready"})")
            if (info.pnpmReady) appendLine("pnpm: ${info.pnpmVersion ?: "ready"}")
            if (info.yarnReady) appendLine("yarn: ${info.yarnVersion ?: "ready"}")
            if (info.esbuildAvailable) appendLine("esbuild: available")
            appendLine("PHP: ${info.phpVersion ?: "not installed"} (${if (info.phpReady) "ready" else "not ready"})")
            appendLine("Composer: ${info.composerVersion ?: "not installed"} (${if (info.composerReady) "ready" else "not ready"})")
            appendLine("Python: ${info.pythonVersion ?: "not installed"} (${if (info.pythonReady) "ready" else "not ready"})")
            appendLine("Storage used: ${info.storageUsed} bytes, cache: ${info.cacheSize} bytes")
        }.trimEnd())
    }
}

class InitializeBuildEnvTool : Tool {
    override val name = "InitializeBuildEnv"
    override val description = """
        Initialize or install components of the local build environment.
        - component "core" (default): install Node.js + npm (needed for frontend builds).
        - component "php": install PHP runtime.
        - component "composer": install Composer (requires PHP first).
        - component "python": install Python runtime.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        enum("component", listOf("core", "php", "composer", "python"), "The component to install.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("component")?.asString?.let { "Installing build env: $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val component = args.get("component")?.asString ?: return ToolResult.error("InitializeBuildEnv: missing `component`.")
        val mgr = LinuxEnvironmentManager.getInstance(ctx.androidContext)
        val result = when (component) {
            "core" -> mgr.initialize { _, _ -> }
            "php" -> mgr.installPhpRuntime { _, _ -> }
            "composer" -> mgr.installComposer { _, _ -> }
            "python" -> mgr.installPythonRuntime { _, _ -> }
            else -> return ToolResult.error("InitializeBuildEnv: unknown component `$component`.")
        }
        return if (result.isSuccess) ToolResult.ok("Installed $component successfully.")
        else ToolResult.error("InitializeBuildEnv failed: ${result.exceptionOrNull()?.message}")
    }
}

class CheckPlayPolicyTool : Tool {
    override val name = "CheckPlayPolicy"
    override val description = """
        Check Google Play policy compliance for an app. Returns blockers (will prevent
        Play upload), warnings, and info items. Use before exporting an AAB.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "The app id to check.", required = true)
    }
    override fun isReadOnly() = true
    override fun activityDescription(args: JsonObject): String? =
        args.get("appId")?.asString?.let { "Checking Play policy for app $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.asLong ?: return ToolResult.error("CheckPlayPolicy: missing `appId`.")
        val app = ctx.appRepository.getWebApp(appId) ?: return ToolResult.error("CheckPlayPolicy: app $appId not found.")
        val report = PlayPolicyChecker.check(app)
        if (report.isClean) {
            return ToolResult.ok("App \"${app.name}\" has no policy issues. Ready for Play.")
        }
        val lines = report.violations.joinToString("\n") { v ->
            "- [${v.severity}] ${v.ruleId}: ${v.policyArea}"
        }
        return ToolResult.ok(buildString {
            appendLine("App \"${app.name}\": ${report.blockerCount} blocker(s), ${report.warningCount} warning(s), ${report.violations.size - report.blockerCount - report.warningCount} info")
            appendLine("Can publish: ${report.canPublish}")
            appendLine(lines)
        }.trimEnd())
    }
}
