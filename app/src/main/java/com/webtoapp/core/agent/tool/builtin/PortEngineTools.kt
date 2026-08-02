package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.engine.EngineManager
import com.webtoapp.core.engine.EngineType
import com.webtoapp.core.port.ProcessPortScanner

class ScanPortsTool : Tool {
    override val name = "ScanPorts"
    override val description = """
        Scan all allocated local ports and return running services (port, type, owner, responding status).
        Use this to diagnose port conflicts or check which runtimes are active.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {}
    override fun isReadOnly() = true
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val services = ProcessPortScanner.scanAllPorts(ctx.androidContext)
        if (services.isEmpty()) return ToolResult.ok("No running services on any allocated ports.")
        val lines = services.joinToString("\n") { s ->
            "- port=${s.port} type=${s.type} owner=${s.owner} responding=${s.isResponding} pid=${s.pid}"
        }
        return ToolResult.ok("${services.size} running service(s):\n$lines")
    }
}

class KillPortTool : Tool {
    override val name = "KillPort"
    override val description = "Kill the process occupying a specific port."
    override val parametersSchema: JsonElement = jsonSchema {
        integer("port", "The port number to free.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("port")?.asString?.let { "Killing process on port $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val port = args.get("port")?.asInt ?: return ToolResult.error("KillPort: missing `port`.")
        val killed = ProcessPortScanner.killProcess(port)
        return if (killed) ToolResult.ok("Killed process on port $port.")
        else ToolResult.error("KillPort: no process found on port $port or kill failed.")
    }
}

class KillAllPortsTool : Tool {
    override val name = "KillAllPorts"
    override val description = "Kill all running local server processes (release all allocated ports)."
    override val parametersSchema: JsonElement = jsonSchema {}
    override fun isReadOnly() = false
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val count = ProcessPortScanner.killAllProcesses(ctx.androidContext)
        return ToolResult.ok("Killed $count running service(s).")
    }
}

class GetEngineStatusTool : Tool {
    override val name = "GetEngineStatus"
    override val description = """
        Check the status of browser engines (System WebView and GeckoView). Returns availability,
        download status, and disk size for each engine, plus the currently selected engine.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        enum("engineType", listOf("SYSTEM_WEBVIEW", "GECKOVIEW"), "Check a specific engine only.")
    }
    override fun isReadOnly() = true
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val mgr = EngineManager.getInstance(ctx.androidContext)
        val types = args.get("engineType")?.asString?.let { listOf(EngineType.valueOf(it)) }
            ?: EngineType.entries
        val lines = types.joinToString("\n") { t ->
            val status = mgr.getEngineStatus(t)
            val size = mgr.getEngineSize(t) / (1024 * 1024)
            "- ${t.name}: status=$status, size=${size}MB, available=${mgr.isEngineAvailable(t)}"
        }
        val selected = mgr.selectedEngine.value.name
        return ToolResult.ok("Selected engine: $selected\n$lines")
    }
}

class SelectEngineTool : Tool {
    override val name = "SelectEngine"
    override val description = """
        Select the active browser engine. SYSTEM_WEBVIEW is always available.
        GECKOVIEW requires downloading ~55MB of native libraries on first use.
    """.trimIndent()
    override val parametersSchema: JsonElement = jsonSchema {
        enum("engineType", listOf("SYSTEM_WEBVIEW", "GECKOVIEW"), "The engine to activate.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("engineType")?.asString?.let { "Selecting engine $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val typeName = args.get("engineType")?.asString ?: return ToolResult.error("SelectEngine: missing `engineType`.")
        val type = runCatching { EngineType.valueOf(typeName) }.getOrNull()
            ?: return ToolResult.error("SelectEngine: unknown engine `$typeName`.")
        val mgr = EngineManager.getInstance(ctx.androidContext)
        if (type == EngineType.GECKOVIEW && !mgr.isEngineAvailable(type)) {
            return ToolResult.error("SelectEngine: GeckoView is not downloaded yet. Ask the user to download it from Browser Kernel settings.")
        }
        mgr.selectEngine(type)
        return ToolResult.ok("Active engine set to ${type.name}.")
    }
}

class DeleteEngineTool : Tool {
    override val name = "DeleteEngine"
    override val description = "Delete a downloaded browser engine (e.g. GeckoView) to free disk space."
    override val parametersSchema: JsonElement = jsonSchema {
        enum("engineType", listOf("GECKOVIEW"), "The engine to delete.", required = true)
    }
    override fun isReadOnly() = false
    override fun activityDescription(args: JsonObject): String? =
        args.get("engineType")?.asString?.let { "Deleting engine $it" }
    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val typeName = args.get("engineType")?.asString ?: return ToolResult.error("DeleteEngine: missing `engineType`.")
        val type = runCatching { EngineType.valueOf(typeName) }.getOrNull()
            ?: return ToolResult.error("DeleteEngine: unknown engine `$typeName`.")
        val mgr = EngineManager.getInstance(ctx.androidContext)
        return if (mgr.deleteEngine(type)) ToolResult.ok("Deleted ${type.name} engine.")
        else ToolResult.error("DeleteEngine: failed to delete ${type.name} (not downloaded?).")
    }
}
