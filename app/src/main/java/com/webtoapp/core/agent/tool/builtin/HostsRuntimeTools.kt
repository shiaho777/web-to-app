package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.adblock.AdBlocker
import com.webtoapp.core.golang.GoToolchainManager
import com.webtoapp.core.nodejs.NodeDependencyManager
import com.webtoapp.core.python.PythonDependencyManager
import com.webtoapp.core.wordpress.WordPressDependencyManager
import org.koin.java.KoinJavaComponent

class GetAdBlockStatusTool : Tool {
    override val name = "GetAdBlockStatus"
    override val description = """
        Get the current ad-blocker status: total rules, hosts rules, enabled/disabled sources,
        and whether ad-blocking is globally enabled.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {}
    override fun isReadOnly() = true
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val blocker = KoinJavaComponent.get<AdBlocker>(AdBlocker::class.java, null, null)
        val enabled = blocker.isEnabled()
        val totalRules = blocker.getRuleCount()
        val hostsRules = blocker.getHostsFileRuleCount()
        val enabledSources = blocker.getEnabledHostsSources()
        val disabledSources = blocker.getDisabledHostsSources()
        val downloaded = blocker.getAllDownloadedSourceKeys()
        return ToolResult.ok(buildString {
            appendLine("Ad-block enabled: $enabled")
            appendLine("Total rules: $totalRules (hosts: $hostsRules)")
            appendLine("Downloaded sources: ${downloaded.size}")
            if (enabledSources.isNotEmpty()) appendLine("Enabled: ${enabledSources.joinToString(", ")}")
            if (disabledSources.isNotEmpty()) appendLine("Disabled: ${disabledSources.joinToString(", ")}")
        }.trimEnd())
    }
}

class ManageHostsRulesTool : Tool {
    override val name = "ManageHostsRules"
    override val description = """
        Manage hosts-based ad-block rule sources. Actions:
        - import_url: download and import a hosts list from a URL.
        - toggle: enable or disable a downloaded source.
        - remove: remove a downloaded source.
        - clear: remove all hosts sources.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        enum("action", listOf("import_url", "toggle", "remove", "clear"), "The action to perform.", required = true)
        string("url", "URL for import_url action.")
        string("sourceKey", "Source key for toggle/remove (use GetAdBlockStatus to find keys).")
        boolean("enabled", "For toggle: true to enable, false to disable.")
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        "Managing hosts rules: ${args.get("action")?.asString}"
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val blocker = KoinJavaComponent.get<AdBlocker>(AdBlocker::class.java, null, null)
        val action = args.get("action")?.asString ?: return ToolResult.error("ManageHostsRules: missing `action`.")
        return when (action) {
            "import_url" -> {
                val url = args.get("url")?.asString ?: return ToolResult.error("ManageHostsRules: missing `url` for import_url.")
                val result = blocker.importHostsFromUrl(url, ctx.androidContext)
                if (result.isSuccess) ToolResult.ok("Imported ${result.getOrNull()} rules from $url.")
                else ToolResult.error("Import failed: ${result.exceptionOrNull()?.message}")
            }
            "toggle" -> {
                val key = args.get("sourceKey")?.asString ?: return ToolResult.error("ManageHostsRules: missing `sourceKey`.")
                val enabled = args.get("enabled")?.asBoolean ?: true
                val result = blocker.setHostsSourceEnabled(ctx.androidContext, key, enabled)
                if (result.isSuccess) ToolResult.ok("Source $key ${if (enabled) "enabled" else "disabled"}.")
                else ToolResult.error("Toggle failed: ${result.exceptionOrNull()?.message}")
            }
            "remove" -> {
                val key = args.get("sourceKey")?.asString ?: return ToolResult.error("ManageHostsRules: missing `sourceKey`.")
                val result = blocker.removeHostsSource(ctx.androidContext, key)
                if (result.isSuccess) ToolResult.ok("Removed source $key.")
                else ToolResult.error("Remove failed: ${result.exceptionOrNull()?.message}")
            }
            "clear" -> {
                blocker.clearHostsFileRules()
                ToolResult.ok("Cleared all hosts rules.")
            }
            else -> ToolResult.error("ManageHostsRules: unknown action `$action`.")
        }
    }
}

class GetRuntimeStatusTool : Tool {
    override val name = "GetRuntimeStatus"
    override val description = """
        Check which server runtimes are installed and ready: PHP/WordPress, Node.js, Python, Go.
        Pass a specific runtime to check just one. "ready" means installed on disk; on host
        builds with targetSdk >= 29 (SELinux W^X) the exec-based runtimes (PHP/WordPress/
        Python/Go) still cannot start locally for preview — see localExecAllowed in the
        output. Node.js (JNI) and every exported APK are unaffected either way.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        enum("runtime", listOf("php", "node", "python", "go"), "Check a specific runtime only.")
    }
    override fun isReadOnly() = true
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val c = ctx.androidContext
        val execAllowed = com.webtoapp.core.linux.RuntimeExecPolicy.canExecAppDataBinaries(c)
        val lines = buildList {
            add("localExecAllowed=$execAllowed" + if (execAllowed) "" else " (targetSdk>=29 host: PHP/WordPress/Python/Go cannot start locally for preview; Node.js and exported APKs are unaffected)")
            if (args.get("runtime")?.asString?.let { it == "php" || it == "wordpress" } != false) {
                add("PHP: ready=${WordPressDependencyManager.isPhpReady(c)}")
                add("WordPress: ready=${WordPressDependencyManager.isWordPressReady(c)}")
            }
            if (args.get("runtime")?.asString?.let { it == "node" } != false || args.get("runtime") == null) {
                add("Node.js: ready=${NodeDependencyManager.isNodeReady(c)}")
            }
            if (args.get("runtime")?.asString?.let { it == "python" } != false || args.get("runtime") == null) {
                add("Python: ready=${PythonDependencyManager.isPythonReady(c)}")
            }
            if (args.get("runtime")?.asString?.let { it == "go" } != false || args.get("runtime") == null) {
                add("Go: ready=${GoToolchainManager.isGoReady(c)}")
            }
        }
        return ToolResult.ok(lines.joinToString("\n"))
    }
}

class InstallRuntimeTool : Tool {
    override val name = "InstallRuntime"
    override val description = """
        Download and install a server runtime (PHP/WordPress, Node.js, Python, or Go).
        This is a large download. Use GetRuntimeStatus first to check what's needed.
        On host builds with targetSdk >= 29 (localExecAllowed=false in GetRuntimeStatus),
        installing PHP/Python/Go still makes sense for building/exporting apps, but their
        local preview cannot start; do not retry the install to "fix" that.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        enum("runtime", listOf("php", "node", "python", "go"), "The runtime to install.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("runtime")?.asString?.let { "Installing runtime $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val runtime = args.get("runtime")?.asString ?: return ToolResult.error("InstallRuntime: missing `runtime`.")
        val c = ctx.androidContext
        val success = when (runtime) {
            "php" -> WordPressDependencyManager.downloadAllDependencies(c)
            "node" -> NodeDependencyManager.downloadNodeRuntime(c)
            "python" -> PythonDependencyManager.downloadPythonRuntime(c)
            "go" -> GoToolchainManager.installGoToolchain(c)
            else -> return ToolResult.error("InstallRuntime: unknown runtime `$runtime`.")
        }
        return if (success) ToolResult.ok("$runtime runtime installed successfully.")
        else ToolResult.error("InstallRuntime: failed to install $runtime. Check the download mirror region and network.")
    }
}

class ClearRuntimeCacheTool : Tool {
    override val name = "ClearRuntimeCache"
    override val description = "Clear the download cache for one or all runtimes to free disk space."
    override val parametersSchema: JsonElement = jsonSchema {
        enum("runtime", listOf("php", "node", "python", "go"), "Clear a specific runtime only (default: all).")
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        "Clearing runtime cache ${args.get("runtime")?.asString ?: "(all)"}"
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val c = ctx.androidContext
        val target = args.get("runtime")?.asString
        if (target == null || target == "php") WordPressDependencyManager.clearCache(c)
        if (target == null || target == "node") NodeDependencyManager.clearCache(c)
        if (target == null || target == "python") PythonDependencyManager.clearCache(c)
        if (target == null || target == "go") GoToolchainManager.clearCache(c)
        return ToolResult.ok("Cleared ${target ?: "all"} runtime cache(s).")
    }
}
