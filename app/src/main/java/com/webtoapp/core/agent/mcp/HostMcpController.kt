package com.webtoapp.core.agent.mcp

import android.content.Context
import com.google.gson.JsonObject
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.permission.PermissionChecker
import com.webtoapp.core.agent.permission.PermissionDecision
import com.webtoapp.core.agent.permission.PermissionPrompter
import com.webtoapp.core.agent.plan.PlanManager
import com.webtoapp.core.agent.todo.TodoManager
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolRegistry
import com.webtoapp.core.agent.tool.ToolRegistryFactory
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.core.host.HostRuntimePrefs
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.AiModel
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiKeyConfig
import com.webtoapp.data.model.SavedModel
import com.webtoapp.data.repository.WebAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.java.KoinJavaComponent
import java.net.URI
import java.security.MessageDigest

/**
 * Loopback MCP in front of the same host tools the in-app agent runs.
 * Write tools still go through [prompter], which [com.webtoapp.ui.MainActivity]
 * shows no matter which screen is open.
 */
object HostMcpController {
    private const val TAG = "HostMcp"

    val prompter = PermissionPrompter()
    private val checker = PermissionChecker(prompter)

    private val lifecycle = Mutex()
    private var server: LoopbackHttpServer? = null
    private var registry: ToolRegistry? = null
    private var toolContext: ToolContext? = null

    private val _status = MutableStateFlow<Status>(Status.Stopped)
    val status: StateFlow<Status> = _status.asStateFlow()

    suspend fun restore(context: Context) {
        val settings = HostRuntimePrefs.getInstance(context).currentMcpBlocking()
        if (settings.enabled) apply(context) else stop()
    }

    suspend fun apply(context: Context) {
        lifecycle.withLock {
            val prefs = HostRuntimePrefs.getInstance(context)
            val settings = prefs.currentMcpBlocking()
            if (!settings.enabled) {
                stopLocked()
                return
            }
            if (server != null && _status.value is Status.Running) return
            stopLocked()
            var http: LoopbackHttpServer? = null
            var lastError: Exception? = null
            for (offset in 0 until 8) {
                val candidate = LoopbackHttpServer(settings.port + offset) { request -> handle(context, request) }
                try {
                    candidate.start()
                    http = candidate
                    break
                } catch (e: Exception) {
                    lastError = e
                }
            }
            val started = http
            if (started == null) {
                AppLogger.w(TAG, "bind failed: ${lastError?.message}")
                _status.value = Status.Failed(lastError?.message ?: "bind failed")
                return
            }
            if (started.boundPort != settings.port) {
                prefs.setMcpPort(started.boundPort)
            }
            server = started
            _status.value = Status.Running(started.boundPort)
            AppLogger.i(TAG, "listening on 127.0.0.1:${started.boundPort}")
        }
    }

    suspend fun stop() {
        lifecycle.withLock { stopLocked() }
    }

    private fun stopLocked() {
        server?.stop()
        server = null
        _status.value = Status.Stopped
    }

    fun configSnippet(port: Int, token: String): String = """
        {
          "mcpServers": {
            "webtoapp": {
              "type": "http",
              "url": "http://127.0.0.1:$port/mcp",
              "headers": {
                "Authorization": "Bearer $token"
              }
            }
          }
        }
    """.trimIndent()

    private suspend fun handle(context: Context, request: HttpRequest): HttpResponse {
        if (request.method != "POST" || request.path != "/mcp") {
            return HttpResponse(405, "Method Not Allowed", "text/plain", "POST /mcp", mapOf("Allow" to "POST"))
        }
        if (!originAllowed(request.headers["origin"])) {
            return HttpResponse(
                403,
                "Forbidden",
                "application/json",
                """{"jsonrpc":"2.0","error":{"code":-32600,"message":"Forbidden origin"}}"""
            )
        }
        val settings = HostRuntimePrefs.getInstance(context).currentMcpBlocking()
        if (!settings.enabled || !tokenMatches(request.headers["authorization"], settings.token)) {
            return HttpResponse(401, "Unauthorized", "application/json", """{"error":"unauthorized"}""")
        }
        val dispatcher = McpDispatcher(
            tools = { registryFor(context).all },
            callTool = { name, args -> invoke(context, name, args) }
        )
        return when (val result = dispatcher.handle(request.body, request.headers)) {
            is McpDispatcher.DispatchResult.Notification ->
                HttpResponse(202, "Accepted", "application/json", "")
            is McpDispatcher.DispatchResult.Message ->
                HttpResponse(result.httpStatus, httpReason(result.httpStatus), "application/json", result.json.toString())
        }
    }

    private fun originAllowed(origin: String?): Boolean {
        if (origin.isNullOrBlank()) return true
        val host = try {
            URI(origin).host
        } catch (_: Exception) {
            return false
        } ?: return false
        return host == "localhost" || host == "127.0.0.1" || host == "::1"
    }

    private fun httpReason(status: Int): String = when (status) {
        200 -> "OK"
        400 -> "Bad Request"
        404 -> "Not Found"
        else -> "Error"
    }

    private suspend fun invoke(context: Context, name: String, args: JsonObject): ToolResult {
        val tool = registryFor(context)[name]
            ?: return ToolResult.error("Unknown tool: $name")
        val ctx = contextFor(context)
        return when (checker.check(tool, args, ctx)) {
            PermissionDecision.Deny -> ToolResult.error("$name was denied on the device.")
            PermissionDecision.Allow -> tool.execute(args, ctx)
        }
    }

    private fun registryFor(context: Context): ToolRegistry {
        registry?.let { return it }
        val files = ProjectFileManager(context.applicationContext)
        val factory = ToolRegistryFactory(
            planManager = PlanManager(
                sessionId = "mcp",
                fileManager = files,
                permissionChecker = checker
            ),
            imageRegistry = null
        )
        return factory.buildForExternalHost().also { registry = it }
    }

    private fun contextFor(context: Context): ToolContext {
        toolContext?.let { return it }
        val appContext = context.applicationContext
        val model = SavedModel(
            model = AiModel(id = "mcp", name = "mcp", provider = AiProvider.CUSTOM),
            apiKeyId = "mcp",
            capabilities = emptyList()
        )
        val key = ApiKeyConfig(id = "mcp", provider = AiProvider.CUSTOM, apiKey = "")
        return ToolContext(
            androidContext = appContext,
            sessionId = "mcp",
            fileManager = ProjectFileManager(appContext),
            textModel = model,
            textApiKey = key,
            prompter = prompter,
            todos = TodoManager(),
            appRepository = KoinJavaComponent.get(WebAppRepository::class.java)
        ).also { toolContext = it }
    }

    private fun tokenMatches(header: String?, expected: String): Boolean {
        if (expected.isEmpty() || header.isNullOrBlank()) return false
        val prefix = "bearer "
        if (!header.startsWith(prefix, ignoreCase = true)) return false
        val presented = header.substring(prefix.length).trim()
        return MessageDigest.isEqual(
            presented.toByteArray(Charsets.UTF_8),
            expected.toByteArray(Charsets.UTF_8)
        )
    }

    sealed class Status {
        object Stopped : Status()
        data class Running(val port: Int) : Status()
        data class Failed(val message: String) : Status()
    }
}
