package com.webtoapp.core.plugin

import android.webkit.JavascriptInterface
import com.google.gson.JsonParser
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.network.NetworkModule
import com.webtoapp.util.GsonProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Native side of the `hcj` bridge (`window.__hcjBridge`).
 *
 * Every call carries (pluginId, token); the token is issued per WebView by
 * [issueToken] and woven into the plugin's injected code, so a page script can
 * only use capabilities for the plugin instance it was injected as. Declared
 * [PluginPermission]s are enforced per call.
 */
class PluginBridge(
    private val configStore: PluginConfigStore,
    private val host: Host
) {

    interface Host {
        /** Run JS in the page WebView (used for fetch callbacks + events). */
        fun evaluatePageJs(js: String)
        fun pluginFor(pluginId: String): Plugin?
        fun onBadge(pluginId: String, text: String, color: String)
        fun onPanelOpen(pluginId: String)
        fun onPanelClose(pluginId: String)
        /** A message from the page script towards this plugin's panel. */
        fun onPanelMessage(pluginId: String, json: String)
        fun onNotify(pluginId: String, title: String, body: String)
    }

    companion object {
        private const val TAG = "PluginBridge"
        private const val MAX_BODY_BYTES = 8 * 1024 * 1024L
        private val gson get() = GsonProvider.gson
        private val io = Executors.newCachedThreadPool { r ->
            Thread(r, "PluginBridgeIO").apply { isDaemon = true }
        }
    }

    private val tokens = ConcurrentHashMap<String, String>() // token -> pluginId

    fun issueToken(pluginId: String): String =
        PluginInjection.newToken().also { tokens[it] = pluginId }

    fun revokeAll() = tokens.clear()

    private fun authorize(pluginId: String, token: String): Plugin? {
        if (tokens[token] != pluginId) return null
        return host.pluginFor(pluginId)
    }

    private fun requirePermission(
        pluginId: String, token: String, permission: PluginPermission
    ): Plugin? {
        val plugin = authorize(pluginId, token) ?: return null
        return if (plugin.hasPermission(permission)) plugin else null
    }

    // -- hcj.config -----------------------------------------------------------

    @JavascriptInterface
    fun configGet(pluginId: String, token: String, key: String): String? {
        requirePermission(pluginId, token, PluginPermission.STORAGE) ?: return null
        return configStore.get(pluginId, key)
    }

    @JavascriptInterface
    fun configSet(pluginId: String, token: String, key: String, jsonValue: String) {
        requirePermission(pluginId, token, PluginPermission.STORAGE) ?: return
        configStore.set(pluginId, key, jsonValue)
    }

    @JavascriptInterface
    fun configRemove(pluginId: String, token: String, key: String) {
        requirePermission(pluginId, token, PluginPermission.STORAGE) ?: return
        configStore.remove(pluginId, key)
    }

    @JavascriptInterface
    fun configAll(pluginId: String, token: String): String {
        requirePermission(pluginId, token, PluginPermission.STORAGE) ?: return "{}"
        return configStore.all(pluginId)
    }

    // -- hcj.fetch ------------------------------------------------------------

    @JavascriptInterface
    fun fetch(pluginId: String, token: String, url: String, optsJson: String, cbId: String) {
        requirePermission(pluginId, token, PluginPermission.FETCH) ?: run {
            host.evaluatePageJs(PluginInjection.resolveFetch(cbId, false, "fetch permission denied"))
            return
        }
        io.execute {
            val result = runCatching {
                val opts = try {
                    JsonParser.parseString(optsJson).asJsonObject
                } catch (e: Exception) {
                    null
                }
                val method = opts?.get("method")?.asString?.uppercase() ?: "GET"
                val reqBuilder = Request.Builder().url(url)
                opts?.get("headers")?.takeIf { it.isJsonObject }?.asJsonObject?.entrySet()?.forEach { (k, v) ->
                    if (v.isJsonPrimitive) reqBuilder.addHeader(k, v.asString)
                }
                val body = opts?.get("body")?.takeIf { it.isJsonPrimitive }?.asString
                if (body != null && method != "GET" && method != "HEAD") {
                    val contentType = opts?.get("contentType")?.takeIf { it.isJsonPrimitive }?.asString
                        ?: "text/plain;charset=utf-8"
                    reqBuilder.method(method, body.toRequestBody(contentType.toMediaTypeOrNull()))
                } else {
                    reqBuilder.method(method, null)
                }
                NetworkModule.defaultClient.newCall(reqBuilder.build()).execute().use { resp ->
                    val bytes = resp.body?.bytes() ?: ByteArray(0)
                    val truncated = bytes.size > MAX_BODY_BYTES
                    val payload = bytes.let { if (truncated) it.copyOf(MAX_BODY_BYTES.toInt()) else it }
                    val binary = opts?.get("responseType")?.asString == "base64"
                    val respHeaders = com.google.gson.JsonObject()
                    resp.headers.forEach { (n, v) -> respHeaders.addProperty(n, v) }
                    gson.toJson(
                        mapOf(
                            "status" to resp.code,
                            "statusText" to resp.message,
                            "headers" to respHeaders,
                            "body" to if (binary)
                                android.util.Base64.encodeToString(payload, android.util.Base64.NO_WRAP)
                            else payload.toString(Charsets.UTF_8),
                            "encoding" to if (binary) "base64" else "utf8",
                            "truncated" to truncated
                        )
                    )
                }
            }
            host.evaluatePageJs(
                PluginInjection.resolveFetch(
                    cbId,
                    result.isSuccess,
                    result.getOrElse { it.message ?: "fetch failed" }
                )
            )
        }
    }

    // -- presentation events --------------------------------------------------

    @JavascriptInterface
    fun notify(pluginId: String, token: String, title: String, body: String) {
        requirePermission(pluginId, token, PluginPermission.NOTIFY) ?: return
        host.onNotify(pluginId, title, body)
    }

    @JavascriptInterface
    fun badge(pluginId: String, token: String, text: String, color: String) {
        requirePermission(pluginId, token, PluginPermission.BADGE) ?: return
        host.onBadge(pluginId, text, color)
    }

    @JavascriptInterface
    fun panelOpen(pluginId: String, token: String) {
        val plugin = authorize(pluginId, token) ?: return
        if (!plugin.hasPanel) return
        host.onPanelOpen(pluginId)
    }

    @JavascriptInterface
    fun panelClose(pluginId: String, token: String) {
        authorize(pluginId, token) ?: return
        host.onPanelClose(pluginId)
    }

    @JavascriptInterface
    fun panelSend(pluginId: String, token: String, json: String) {
        authorize(pluginId, token) ?: return
        host.onPanelMessage(pluginId, json)
    }

    @JavascriptInterface
    fun log(pluginId: String, token: String, message: String) {
        authorize(pluginId, token) ?: return
        AppLogger.d(TAG, "[$pluginId] $message")
    }
}

/**
 * Bridge for the panel WebView (`window.__hcjPanelBridge`). Bound to exactly
 * one plugin — the pid argument is validated, never trusted.
 */
class PluginPanelBridge(
    private val pluginId: String,
    private val configStore: PluginConfigStore,
    private val host: Host,
    private val hasPermission: (PluginPermission) -> Boolean = { true }
) {

    interface Host {
        /** Deliver a panel -> page message (`hcjPanel.send`). */
        fun onPanelSend(pluginId: String, json: String)
        fun onPanelClose(pluginId: String)
    }

    private fun check(pid: String): Boolean = pid == pluginId

    private fun checkStorage(pid: String): Boolean =
        check(pid) && hasPermission(PluginPermission.STORAGE)

    @JavascriptInterface
    fun configGet(pid: String, key: String): String? =
        if (checkStorage(pid)) configStore.get(pid, key) else null

    @JavascriptInterface
    fun configSet(pid: String, key: String, jsonValue: String) {
        if (checkStorage(pid)) configStore.set(pid, key, jsonValue)
    }

    @JavascriptInterface
    fun configRemove(pid: String, key: String) {
        if (checkStorage(pid)) configStore.remove(pid, key)
    }

    @JavascriptInterface
    fun configAll(pid: String): String =
        if (checkStorage(pid)) configStore.all(pid) else "{}"

    @JavascriptInterface
    fun send(pid: String, json: String) {
        if (check(pid)) host.onPanelSend(pid, json)
    }

    @JavascriptInterface
    fun close(pid: String) {
        if (check(pid)) host.onPanelClose(pid)
    }
}
