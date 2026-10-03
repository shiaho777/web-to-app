package com.webtoapp.core.agent.tool.builtin

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.permission.PermissionRequest
import com.webtoapp.core.agent.permission.PermissionResponse
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import com.webtoapp.ui.webview.PreviewSessions
import com.webtoapp.ui.webview.WebViewActivity
import kotlinx.coroutines.delay

/**
 * One tool for the live preview. Opens its own recents task and, in a single
 * call, can run a page script and return a short snapshot of the controls.
 * The page is the session; this call does not keep JavaScript bindings.
 */
class PreviewTool : Tool {
    override val name = "preview"

    override val description = """
        Open this app's live preview in its own recents task and operate that page. The task is visible, so the user can watch. This does not depend on the About-screen separate-tasks switch.

        Pass appId (from ListApps) to preview a saved app, or an http(s) url. Calling again with the same target reuses that task.

        Omit script to read the page. The result lists up to 60 visible controls as {n, tag, role, name, id, type}. Each one is tagged data-wta-n, so a later script can use document.querySelector('[data-wta-n="3"]').

        script is a JavaScript function body that runs in the page. Return the value you need. Use it to read getComputedStyle, change a style, or click. A non-empty script asks the user on the device before it runs. The reply includes the script result and a fresh snapshot. Call preview again to observe a navigation. Do not request the whole HTML document. Screenshot only when the visual result is the question, and say so in the script by returning what you measured.
    """.trimIndent()

    override val parametersSchema: JsonElement = jsonSchema {
        integer("appId", "Saved app id from ListApps. Preferred over url.")
        string("url", "http(s) page to open when there is no saved app.")
        string(
            "script",
            "Optional JavaScript function body run in the page. Return the value you need. Empty means snapshot only."
        )
        integer(
            "waitMs",
            "Optional pause after the script, before the snapshot. 0 to 1500. Use it when a click needs a moment to paint.",
            default = 0
        )
    }

    override fun isReadOnly() = true

    override fun activityDescription(args: JsonObject): String? {
        val appId = args.get("appId")?.takeIf { !it.isJsonNull }?.asLong
        if (appId != null && appId > 0) return "Previewing app $appId"
        val url = args.get("url")?.takeIf { !it.isJsonNull }?.asString
        if (!url.isNullOrBlank()) return "Previewing $url"
        return "Previewing the open page"
    }

    override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
        val appId = args.get("appId")?.takeIf { !it.isJsonNull }?.asLong
        val url = args.get("url")?.takeIf { !it.isJsonNull }?.asString?.trim().orEmpty()
        val script = args.get("script")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
        val waitMs = (args.get("waitMs")?.takeIf { !it.isJsonNull }?.asInt ?: 0)
            .coerceIn(0, PreviewScript.MAX_WAIT_MS)

        if (script.length > PreviewScript.MAX_SCRIPT_CHARS) {
            return ToolResult.error("preview: script is longer than ${PreviewScript.MAX_SCRIPT_CHARS} characters.")
        }
        if (url.isNotEmpty() && !PreviewScript.acceptableHttpUrl(url)) {
            return ToolResult.error("preview: url must be http or https. Use appId for a saved app.")
        }
        if ((appId == null || appId <= 0) && url.isEmpty()) {
            return ToolResult.error("preview: pass appId or url.")
        }
        if (script.isNotBlank() && !confirmScript(ctx, script)) {
            return ToolResult.error("preview: the user denied running the script on the device.")
        }

        val document = if (appId != null && appId > 0) {
            PreviewSessions.documentUriForApp(appId)
        } else {
            PreviewSessions.documentUriForUrl(url)
        }
        val intent = WebViewActivity.buildLaunchIntent(
            context = ctx.androidContext,
            separateTasks = true,
            documentUri = document
        ) {
            putExtra(PreviewSessions.EXTRA_SESSION_KEY, document.toString())
            if (appId != null && appId > 0) {
                putExtra("app_id", appId)
            } else {
                putExtra("url", url)
            }
        }
        val activity = PreviewSessions.open(ctx.androidContext, intent).getOrElse { error ->
            return ToolResult.error("preview: ${error.message ?: "could not open the preview"}")
        }
        PreviewSessions.awaitSurface(activity)
        if (activity.browserSurface == null) {
            return ToolResult.error("preview: the page did not attach. Call preview again once the task is visible.")
        }

        val scriptReport = if (script.isBlank()) {
            null
        } else {
            val raw = activity.evalForAgent(PreviewScript.wrap(script))
            PreviewScript.truncate(PreviewScript.unwrap(raw).ifBlank { "(no result)" })
        }
        if (waitMs > 0) delay(waitMs.toLong())
        val snapshot = PreviewScript.truncate(
            PreviewScript.unwrap(activity.evalForAgent(PreviewScript.snapshot()))
                .ifBlank { "(snapshot unavailable)" }
        )
        val header = buildString {
            append("Preview task ").append(document)
            activity.browserSurface?.getCurrentUrl()?.let { append("\nurl: ").append(it) }
            activity.browserSurface?.getTitle()?.let { append("\ntitle: ").append(it) }
        }
        val body = if (scriptReport == null) {
            "$header\n\nsnapshot:\n$snapshot"
        } else {
            "$header\n\nscript:\n$scriptReport\n\nsnapshot:\n$snapshot"
        }
        return ToolResult.ok(body)
    }

    private suspend fun confirmScript(ctx: ToolContext, script: String): Boolean {
        if (scriptApproved) return true
        val response = ctx.prompter.request(
            PermissionRequest(
                toolCallId = "preview-${System.nanoTime()}",
                toolName = name,
                activity = "Run a script in the live preview",
                argsPreview = mapOf("script" to script.take(200))
            )
        )
        return when (response) {
            is PermissionResponse.Deny -> false
            is PermissionResponse.AlwaysAllow -> {
                scriptApproved = true
                true
            }
            is PermissionResponse.Allow -> true
        }
    }

    private companion object {
        @Volatile
        private var scriptApproved: Boolean = false
    }
}
