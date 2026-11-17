package com.webtoapp.core.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.webtoapp.core.network.NetworkModule
import org.json.JSONArray
import org.json.JSONObject

class GreasemonkeyBridge(
    private val context: Context,
    private val webViewProvider: () -> WebView?
) {
    companion object {
        const val JS_INTERFACE_NAME = "__WTA_GM_BRIDGE__"
        private const val TAG = "GreasemonkeyBridge"
        private const val PREFS_PREFIX = "gm_storage_"

        private val STORAGE_GRANTS = setOf("GM_getValue", "GM_setValue", "GM_deleteValue", "GM_listValues", "GM.getValue", "GM.setValue", "GM.deleteValue", "GM.listValues")
        private val NETWORK_GRANTS = setOf("GM_xmlhttpRequest", "GM.xmlHttpRequest")
        private val CLIPBOARD_GRANTS = setOf("GM_setClipboard", "GM.setClipboard")
        private val TAB_GRANTS = setOf("GM_openInTab", "GM.openInTab")
        private val MENU_GRANTS = setOf("GM_registerMenuCommand", "GM_unregisterMenuCommand", "GM.registerMenuCommand", "GM.unregisterMenuCommand")

        internal fun hasGrant(grants: Collection<String>, names: Set<String>): Boolean =
            grants.any { it in names }

        internal fun grantsByName(grants: Collection<String>): Set<String> = grants.toSet()

        fun generatePolyfillScript(
            scriptId: String,
            grants: List<String>,
            scriptInfo: Map<String, String> = emptyMap(),
            resources: Map<String, String> = emptyMap(),
            token: String = ""
        ): String {
            val infoJson = JSONObject(scriptInfo).toString()
            val resourcesJson = JSONObject(resources).toString()
            val g = grantsByName(grants)
            val storage = hasGrant(g, STORAGE_GRANTS)
            val network = hasGrant(g, NETWORK_GRANTS)
            val clipboard = hasGrant(g, CLIPBOARD_GRANTS)
            val tabs = hasGrant(g, TAB_GRANTS)
            val menu = hasGrant(g, MENU_GRANTS)
            val safeToken = token.replace("\\", "\\\\").replace("'", "\\'")

            val storageFns = if (storage) """
    function GM_getValue(key, defaultValue) {
        var raw = _bridge.getValue(_sid, _token, key, null);
        if (raw === null || raw === undefined) return defaultValue;
        try { return JSON.parse(raw); } catch(e) { return raw; }
    }
    function GM_setValue(key, value) {
        _bridge.setValue(_sid, _token, key, typeof value === 'string' ? value : JSON.stringify(value));
    }
    function GM_deleteValue(key) { _bridge.deleteValue(_sid, _token, key); }
    function GM_listValues() {
        try { return JSON.parse(_bridge.listValues(_sid, _token)); } catch(e) { return []; }
    }
""" else ""

            val networkFn = if (network) """
    function GM_xmlhttpRequest(details) {
        var cbId = _nextCbId();
        window.__WTA_GM_CALLBACKS__[cbId] = {
            onload: details.onload,
            onerror: details.onerror,
            ontimeout: details.ontimeout,
            onprogress: details.onprogress
        };
        _bridge.xmlHttpRequest(_sid, _token, cbId, JSON.stringify({
            method: details.method || 'GET',
            url: details.url,
            headers: details.headers || {},
            data: details.data || '',
            responseType: details.responseType || ''
        }));
        return { abort: function() {} };
    }
""" else ""

            val clipboardFn = if (clipboard) """
    function GM_setClipboard(text, type) {
        _bridge.setClipboard(_sid, _token, text, type || 'text/plain');
    }
""" else ""

            val tabFn = if (tabs) """
    function GM_openInTab(url, options) {
        var bg = (typeof options === 'object') ? !!options.active === false : !!options;
        _bridge.openInTab(_sid, _token, url, bg);
    }
""" else ""

            val menuFns = if (menu) """
    var _menuCommands = {};
    window.__WTA_GM_MENU__ = window.__WTA_GM_MENU__ || {};
    window.__WTA_GM_MENU__[_sid] = _menuCommands;
    function GM_registerMenuCommand(name, fn, accessKey) {
        _menuCommands[name] = fn;
        _bridge.menuCommand(_sid, _token, name, false);
        if (window.__WTA_SCRIPT_WINDOWS__) {
            window.__WTA_SCRIPT_WINDOWS__.addMenuButton(_sid, name, fn, {
                windowTitle: _info.name || _sid,
                windowIcon: '\uD83D\uDC35'
            });
        }
        return name;
    }
    function GM_unregisterMenuCommand(name) {
        delete _menuCommands[name];
        _bridge.menuCommand(_sid, _token, name, true);
    }
""" else ""

            val gmMembers = StringBuilder()
            gmMembers.append("        info: GM_info,\n")
            gmMembers.append("        addStyle: GM_addStyle,\n")
            gmMembers.append("        log: GM_log,\n")
            gmMembers.append("        notification: GM_notification,\n")
            gmMembers.append("        getResourceText: function(n) { return Promise.resolve(GM_getResourceText(n)); },\n")
            gmMembers.append("        getResourceUrl: function(n) { return Promise.resolve(GM_getResourceURL(n)); },\n")
            gmMembers.append("        openScriptWindow: GM_openScriptWindow,\n")
            gmMembers.append("        updateScriptWindow: GM_updateScriptWindow,\n")
            gmMembers.append("        closeScriptWindow: GM_closeScriptWindow")
            if (storage) gmMembers.append(",\n        getValue: function(k, d) { return Promise.resolve(GM_getValue(k, d)); },\n        setValue: function(k, v) { GM_setValue(k, v); return Promise.resolve(); },\n        deleteValue: function(k) { GM_deleteValue(k); return Promise.resolve(); },\n        listValues: function() { return Promise.resolve(GM_listValues()); }")
            if (network) gmMembers.append(",\n        xmlHttpRequest: GM_xmlhttpRequest")
            if (clipboard) gmMembers.append(",\n        setClipboard: GM_setClipboard")
            if (tabs) gmMembers.append(",\n        openInTab: GM_openInTab")
            if (menu) gmMembers.append(",\n        registerMenuCommand: GM_registerMenuCommand,\n        unregisterMenuCommand: GM_unregisterMenuCommand")

            val expose = StringBuilder()
            expose.append("    window.GM_info = GM_info;\n")
            expose.append("    window.GM_addStyle = GM_addStyle;\n")
            expose.append("    window.GM_log = GM_log;\n")
            expose.append("    window.GM_notification = GM_notification;\n")
            expose.append("    window.GM_getResourceText = GM_getResourceText;\n")
            expose.append("    window.GM_getResourceURL = GM_getResourceURL;\n")
            expose.append("    window.GM_openScriptWindow = GM_openScriptWindow;\n")
            expose.append("    window.GM_updateScriptWindow = GM_updateScriptWindow;\n")
            expose.append("    window.GM_closeScriptWindow = GM_closeScriptWindow;\n")
            if (storage) expose.append("    window.GM_getValue = GM_getValue;\n    window.GM_setValue = GM_setValue;\n    window.GM_deleteValue = GM_deleteValue;\n    window.GM_listValues = GM_listValues;\n")
            if (network) expose.append("    window.GM_xmlhttpRequest = GM_xmlhttpRequest;\n")
            if (clipboard) expose.append("    window.GM_setClipboard = GM_setClipboard;\n")
            if (tabs) expose.append("    window.GM_openInTab = GM_openInTab;\n")
            if (menu) expose.append("    window.GM_registerMenuCommand = GM_registerMenuCommand;\n    window.GM_unregisterMenuCommand = GM_unregisterMenuCommand;\n")
            expose.append("    window.GM = GM;\n")
            expose.append("    window.unsafeWindow = window;")

            return """
(function() {
    'use strict';

    if (!window.__WTA_GM_CALLBACKS__) window.__WTA_GM_CALLBACKS__ = {};
    var _cbId = 0;
    function _nextCbId() { return 'gm_' + (++_cbId) + '_' + Date.now(); }

    var _bridge = window.$JS_INTERFACE_NAME;
    var _sid = '$scriptId';
    var _token = '$safeToken';
    var _info = $infoJson;
    var _resources = $resourcesJson;

    var GM_info = {
        script: {
            name: _info.name || '',
            version: _info.version || '',
            description: _info.description || '',
            author: _info.author || '',
            namespace: _info.namespace || '',
            grants: ${JSONArray(grants)},
            resources: _resources
        },
        scriptHandler: 'WebToApp',
        version: '1.0'
    };
$storageFns$networkFn$clipboardFn$tabFn$menuFns
    function GM_addStyle(css) {
        var s = document.createElement('style');
        s.textContent = css;
        (document.head || document.documentElement).appendChild(s);
        return s;
    }
    function GM_log(message) { _bridge.log(String(message)); }
    function GM_notification(details, ondone) {
        var text = typeof details === 'string' ? details : (details.text || details.title || '');
        _bridge.log('[Notification] ' + text);
        if (ondone) setTimeout(ondone, 100);
    }
    function GM_getResourceText(name) { return _resources[name] || ''; }
    function GM_getResourceURL(name) { return _resources[name] || ''; }
    function GM_openScriptWindow(html, options) {
        if (!window.__WTA_SCRIPT_WINDOWS__) return null;
        options = options || {};
        return window.__WTA_SCRIPT_WINDOWS__.createWindow(_sid, {
            title: options.title || _info.name || _sid,
            icon: options.icon || '\uD83D\uDC35',
            width: options.width || 300,
            height: options.height || 360,
            content: html || '',
            resizable: options.resizable !== false
        });
    }
    function GM_updateScriptWindow(html) {
        if (window.__WTA_SCRIPT_WINDOWS__) window.__WTA_SCRIPT_WINDOWS__.updateContent(_sid, html);
    }
    function GM_closeScriptWindow() {
        if (window.__WTA_SCRIPT_WINDOWS__) window.__WTA_SCRIPT_WINDOWS__.closeWindow(_sid);
    }

    var GM = {
$gmMembers
    };

$expose
})();
""".trimIndent()
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val httpClient get() = NetworkModule.defaultClient

    private val aliasByScriptId = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val scriptIdByAlias = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val tokenByScriptId = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val grantsByScriptId = java.util.concurrent.ConcurrentHashMap<String, Set<String>>()

    fun storageAliasFor(scriptId: String): String {
        aliasByScriptId.getOrPut(scriptId) {
            val alias = "ns_" + java.security.SecureRandom().nextInt(Int.MAX_VALUE).toString(36) +
                "_" + java.security.SecureRandom().nextInt(Int.MAX_VALUE).toString(36)
            scriptIdByAlias[alias] = scriptId
            alias
        }
        return aliasByScriptId[scriptId] ?: scriptId
    }

    fun registerScript(scriptId: String, grants: Collection<String>): String {
        grantsByScriptId[scriptId] = grants.toSet()
        return tokenByScriptId.getOrPut(scriptId) {
            val bytes = ByteArray(24)
            java.security.SecureRandom().nextBytes(bytes)
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
        }
    }

    private fun authorized(alias: String, token: String, grants: Set<String>): String? {
        val scriptId = scriptIdByAlias[alias] ?: return null
        val expected = tokenByScriptId[scriptId] ?: return null
        if (expected != token) return null
        val registered = grantsByScriptId[scriptId] ?: return null
        if (!hasGrant(registered, grants)) return null
        return scriptId
    }

    private fun authorizedExact(alias: String, token: String, vararg names: String): String? {
        val scriptId = scriptIdByAlias[alias] ?: return null
        val expected = tokenByScriptId[scriptId] ?: return null
        if (expected != token) return null
        val registered = grantsByScriptId[scriptId] ?: return null
        if (names.none { it in registered }) return null
        return scriptId
    }

    private fun getPrefs(alias: String): android.content.SharedPreferences? {
        val scriptId = scriptIdByAlias[alias] ?: return null
        return context.getSharedPreferences("$PREFS_PREFIX$scriptId", Context.MODE_PRIVATE)
    }

    @JavascriptInterface
    fun getValue(alias: String, token: String, key: String, defaultValue: String?): String? {
        if (authorizedExact(alias, token, "GM_getValue", "GM.getValue") == null) return defaultValue
        return getPrefs(alias)?.getString(key, defaultValue)
    }

    @JavascriptInterface
    fun setValue(alias: String, token: String, key: String, value: String) {
        if (authorizedExact(alias, token, "GM_setValue", "GM.setValue") == null) return
        getPrefs(alias)?.edit()?.putString(key, value)?.apply()
    }

    @JavascriptInterface
    fun deleteValue(alias: String, token: String, key: String) {
        if (authorizedExact(alias, token, "GM_deleteValue", "GM.deleteValue") == null) return
        getPrefs(alias)?.edit()?.remove(key)?.apply()
    }

    @JavascriptInterface
    fun listValues(alias: String, token: String): String {
        if (authorizedExact(alias, token, "GM_listValues", "GM.listValues") == null) return "[]"
        val keys = getPrefs(alias)?.all?.keys ?: emptySet()
        return JSONArray(keys.toList()).toString()
    }

    @JavascriptInterface
    fun xmlHttpRequest(alias: String, token: String, callbackId: String, detailsJson: String) {
        if (authorized(alias, token, NETWORK_GRANTS) == null) {
            callbackToJs(callbackId, "onerror", JSONObject().put("error", "GM_xmlhttpRequest is not granted").put("status", 0).toString())
            return
        }
        scope.launch {
            try {
                val details = JSONObject(detailsJson)
                val method = details.optString("method", "GET").uppercase()
                val url = details.getString("url")
                val data = details.optString("data", "")
                val headersObj = details.optJSONObject("headers")
                val responseType = details.optString("responseType", "")

                val uri = runCatching { java.net.URI(url) }.getOrNull()
                val scheme = uri?.scheme?.lowercase()
                if (scheme != "http" && scheme != "https") {
                    throw IllegalArgumentException("GM_xmlhttpRequest supports HTTP(S) URLs only")
                }
                val pageUrl = runCatching {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { webViewProvider()?.url }
                }.getOrNull().orEmpty()
                val pageIsLocal = com.webtoapp.core.webview.NativeBridge.isPrivateNetworkUrl(pageUrl) ||
                    pageUrl.startsWith("file:") || pageUrl.startsWith("content://")
                val targetIsPrivate = com.webtoapp.core.webview.NativeBridge.isPrivateNetworkHost(uri?.host)
                if (targetIsPrivate && !pageIsLocal) {
                    throw IllegalArgumentException(
                        "GM_xmlhttpRequest to local network addresses is only allowed from local pages"
                    )
                }
                val client = if (pageIsLocal) {
                    httpClient
                } else {
                    httpClient.newBuilder()
                        .addNetworkInterceptor { chain ->
                            val followUp = chain.request()
                            if (com.webtoapp.core.webview.NativeBridge.isPrivateNetworkHost(followUp.url.host)) {
                                throw java.io.IOException(
                                    "Blocked redirect to local network host ${followUp.url.host}"
                                )
                            }
                            chain.proceed(followUp)
                        }
                        .build()
                }

                val requestBuilder = Request.Builder().url(url)

                if (headersObj != null) {
                    val headerMap = mutableMapOf<String, String>()
                    headersObj.keys().forEach { key ->
                        headerMap[key] = headersObj.getString(key)
                    }
                    requestBuilder.headers(headerMap.toHeaders())
                }

                when (method) {
                    "POST", "PUT", "PATCH" -> {
                        val contentType = headersObj?.optString("Content-Type", "application/x-www-form-urlencoded")
                            ?: "application/x-www-form-urlencoded"
                        requestBuilder.method(method, data.toRequestBody(contentType.toMediaTypeOrNull()))
                    }
                    "DELETE" -> requestBuilder.delete(
                        if (data.isNotEmpty()) data.toRequestBody(null) else null
                    )
                    else -> requestBuilder.get()
                }

                val maxResponseBytes = 16L * 1024 * 1024
                client.newCall(requestBuilder.build()).execute().use { response ->
                    val charset = response.body?.contentType()?.charset(Charsets.UTF_8)
                        ?: Charsets.UTF_8
                    val bytes = response.body?.byteStream()?.use { input ->
                        val out = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var total = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            total += read
                            if (total > maxResponseBytes) {
                                throw IllegalArgumentException(
                                    "GM_xmlhttpRequest response exceeds ${maxResponseBytes / 1024 / 1024}MB"
                                )
                            }
                            out.write(buffer, 0, read)
                        }
                        out.toByteArray()
                    } ?: ByteArray(0)
                    val responseText = String(bytes, charset)

                    val responseHeaders = JSONObject()
                    response.headers.forEach { (name, value) ->
                        responseHeaders.put(name.lowercase(), value)
                    }

                    val result = JSONObject().apply {
                        put("status", response.code)
                        put("statusText", response.message)
                        put("responseText", responseText)
                        put("responseHeaders", responseHeaders.toString())
                        put("finalUrl", response.request.url.toString())
                    }

                    callbackToJs(callbackId, "onload", result.toString())
                }

            } catch (e: Exception) {
                AppLogger.e(TAG, "GM_xmlhttpRequest failed", e)
                val error = JSONObject().apply {
                    put("error", e.message ?: "Unknown error")
                    put("status", 0)
                }
                callbackToJs(callbackId, "onerror", error.toString())
            }
        }
    }

    @JavascriptInterface
    fun setClipboard(alias: String, token: String, text: String, type: String) {
        if (authorized(alias, token, CLIPBOARD_GRANTS) == null) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("GM_setClipboard", text)
        clipboard.setPrimaryClip(clip)
        AppLogger.d(TAG, "GM_setClipboard: ${text.take(50)}...")
    }

    @JavascriptInterface
    fun openInTab(alias: String, token: String, url: String, openInBackground: Boolean): Boolean {
        if (authorized(alias, token, TAB_GRANTS) == null) return false
        return try {
            val scheme = runCatching { java.net.URI(url).scheme?.lowercase() }.getOrNull()
            if (scheme != "http" && scheme != "https") {
                AppLogger.w(TAG, "GM_openInTab blocked non-web URL: $url")
                return false
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            AppLogger.e(TAG, "GM_openInTab failed: $url", e)
            false
        }
    }

    @JavascriptInterface
    fun log(message: String) {
        AppLogger.d(TAG, "[GM_log] $message")
    }

    private val menuCommands = java.util.concurrent.ConcurrentHashMap<String, LinkedHashSet<String>>()

    var onMenuCommandsChanged: ((scriptId: String, names: List<String>) -> Unit)? = null

    fun menuCommandsFor(scriptId: String): List<String> =
        menuCommands[scriptId]?.toList() ?: emptyList()

    @JavascriptInterface
    fun menuCommand(alias: String, token: String, name: String, remove: Boolean) {
        val scriptId = authorized(alias, token, MENU_GRANTS) ?: return
        val set = menuCommands.getOrPut(scriptId) { LinkedHashSet() }
        synchronized(set) {
            if (remove) set.remove(name) else set.add(name)
            onMenuCommandsChanged?.invoke(scriptId, set.toList())
        }
    }

    private fun callbackToJs(callbackId: String, event: String, dataJson: String) {
        val quotedCallbackId = com.webtoapp.util.JsStrings.quote(callbackId ?: "")
        val quotedEvent = com.webtoapp.util.JsStrings.quote(event)
        val escapedData = dataJson.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r")
        val js = """
            (function() {
                var cb = window.__WTA_GM_CALLBACKS__[$quotedCallbackId];
                if (cb && cb[$quotedEvent]) {
                    try {
                        cb[$quotedEvent](JSON.parse('$escapedData'));
                    } catch(e) {
                        console.error('[GM Bridge] Callback error:', e);
                    }
                }
                if ($quotedEvent === 'onload' || $quotedEvent === 'onerror' || $quotedEvent === 'ontimeout') {
                    delete window.__WTA_GM_CALLBACKS__[$quotedCallbackId];
                }
            })();
        """.trimIndent()

        MainScope().launch {
            webViewProvider()?.evaluateJavascript(js, null)
        }
    }

    fun destroy() {
        scope.cancel()
    }
}
