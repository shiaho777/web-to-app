package com.webtoapp.core.stats

import android.content.Context
import com.webtoapp.core.adblock.AdBlocker
import com.webtoapp.core.adblock.CosmeticFilterScript
import com.webtoapp.core.plugin.PluginInjection
import com.webtoapp.core.plugin.PluginKind
import com.webtoapp.core.plugin.PluginRunAt
import com.webtoapp.core.plugin.PluginSession
import com.webtoapp.core.plugin.PluginStore
import com.webtoapp.core.script.UserScriptStorage
import com.webtoapp.data.model.UserScript
import com.webtoapp.data.model.WebApp

/**
 * Styles the offscreen thumbnail WebView with the same cosmetic payload and
 * the same plugin / userscript styles the real page injects. The capture
 * WebView does not run [com.webtoapp.core.webview.WebViewManager], so without
 * this the card keeps showing the raw site after a refresh.
 *
 * Userscripts run against a style stub, not the live Greasemonkey bridge:
 * [GM_addStyle] writes a stylesheet, and the other GM calls no-op so a script
 * still reaches its style call. Network blocking, extensions, and login state
 * stay out of the thumbnail.
 */
internal class PreparedThumbnailStyles(
    private val rulesKey: String?,
    private val plugins: List<PluginSession.Resolved>,
    private val injectScriptBodies: List<String>,
) {
    fun scriptFor(pageUrl: String): String {
        val payload = ThumbnailAdBlock.payload(rulesKey, pageUrl)
        return thumbnailPageScript(
            cosmeticPayloadJson = payload,
            cssBlocks = thumbnailCssBlocks(plugins, pageUrl),
            scriptBodies = thumbnailScriptBodies(plugins, pageUrl, injectScriptBodies),
        )
    }

    companion object {
        suspend fun prepare(context: Context, app: WebApp): PreparedThumbnailStyles {
            val rulesKey = if (app.adBlockEnabled) {
                ThumbnailAdBlock.prepare(context, app)
            } else {
                null
            }
            val plugins = if (!app.pluginsEnabled) {
                emptyList()
            } else {
                runCatching {
                    val store = PluginStore.getInstance(context)
                    store.awaitLoaded()
                    val ids = store.effectiveIds(true, app.pluginIds)
                    if (ids.isEmpty()) emptyList() else store.resolveForInjection(ids)
                }.getOrDefault(emptyList())
            }
            val injectScriptBodies = app.webViewConfig.injectScripts.mapNotNull { script ->
                loadedInjectScript(context, script)
            }
            return PreparedThumbnailStyles(rulesKey, plugins, injectScriptBodies)
        }
    }
}

internal fun thumbnailPageHost(url: String): String {
    val rest = url.substringAfter("://", missingDelimiterValue = "")
    if (rest.isEmpty()) return ""
    val host = rest.substringBefore('/').substringBefore('?').substringBefore('#')
        .substringBefore(':')
    if (host.isBlank() || host.any { it.isWhitespace() }) return ""
    return host.lowercase()
}

internal fun thumbnailPageScript(
    cosmeticPayloadJson: String,
    cssBlocks: List<Pair<String, String>>,
    scriptBodies: List<String>,
): String {
    val parts = ArrayList<String>(cssBlocks.size + scriptBodies.size + 2)
    if (cosmeticPayloadJson.isNotBlank()) {
        parts += "(${CosmeticFilterScript.APPLY_JS})($cosmeticPayloadJson)"
    }
    for ((pluginId, css) in cssBlocks) {
        if (css.isBlank()) continue
        parts += PluginInjection.wrapCss(pluginId, css)
    }
    if (scriptBodies.isNotEmpty()) {
        parts += THUMBNAIL_STYLE_STUB
        for (body in scriptBodies) {
            if (body.isBlank()) continue
            parts += "(function(){try{\n$body\n}catch(e){}})();"
        }
    }
    return parts.joinToString("\n")
}

internal fun thumbnailCssBlocks(
    plugins: List<PluginSession.Resolved>,
    pageUrl: String,
): List<Pair<String, String>> {
    return plugins.mapNotNull { resolved ->
        val plugin = resolved.plugin
        if (plugin.kind != PluginKind.HCJ) return@mapNotNull null
        if (plugin.runAt != PluginRunAt.DOCUMENT_START) return@mapNotNull null
        if (resolved.css.isBlank() || !plugin.matchesUrl(pageUrl)) return@mapNotNull null
        plugin.id to resolved.css
    }
}

internal fun thumbnailScriptBodies(
    plugins: List<PluginSession.Resolved>,
    pageUrl: String,
    injectScriptBodies: List<String>,
): List<String> {
    val userscripts = plugins.mapNotNull { resolved ->
        val plugin = resolved.plugin
        if (plugin.kind != PluginKind.USERSCRIPT) return@mapNotNull null
        if (resolved.mainJs.isBlank() || !plugin.matchesUrl(pageUrl)) return@mapNotNull null
        resolved.mainJs
    }
    return userscripts + injectScriptBodies.filter { it.isNotBlank() }
}

private fun loadedInjectScript(context: Context, script: UserScript): String? {
    if (!script.enabled) return null
    val code = if (UserScriptStorage.isFileReference(script.code)) {
        UserScriptStorage.loadScriptCode(context, script.code)
    } else {
        script.code
    }
    return code.takeIf { it.isNotBlank() }
}

private object ThumbnailAdBlock {
    private val blocker = AdBlocker()
    private var preparedKey: String? = null

    suspend fun prepare(context: Context, app: WebApp): String {
        val key = rulesKey(app)
        if (preparedKey == key && blocker.isEnabled()) return key
        blocker.prepareRuntimeFilters(
            context = context,
            enabled = true,
            customRules = app.adBlockRules,
            subscriptionUrls = app.adBlockSubscriptions,
            refreshSources = false,
        )
        preparedKey = key
        return key
    }

    fun payload(rulesKey: String?, pageUrl: String): String {
        if (rulesKey == null || rulesKey != preparedKey || !blocker.isEnabled()) return ""
        val host = thumbnailPageHost(pageUrl)
        if (host.isEmpty()) return ""
        return blocker.getCosmeticPayloadJson(host)
    }

    private fun rulesKey(app: WebApp): String {
        return app.adBlockRules.joinToString("\n") + "\u0000" +
            app.adBlockSubscriptions.joinToString("\n") { it.trim() }
    }
}

/**
 * Enough of the Greasemonkey surface for a userscript to inject CSS.
 * Storage and network calls return empty results and do not touch the app.
 */
private const val THUMBNAIL_STYLE_STUB = """
(function(){
  if (window.__wta_thumb_style_stub__) return;
  window.__wta_thumb_style_stub__ = true;
  function GM_addStyle(css) {
    var s = document.createElement('style');
    s.setAttribute('data-wta-thumb', 'userscript');
    s.textContent = css == null ? '' : String(css);
    (document.head || document.documentElement).appendChild(s);
    return s;
  }
  function GM_getValue(k, d) { return d; }
  function GM_setValue() {}
  function GM_deleteValue() {}
  function GM_listValues() { return []; }
  function GM_xmlhttpRequest() { return { abort: function() {} }; }
  function GM_setClipboard() {}
  function GM_openInTab() {}
  function GM_log() {}
  function GM_notification() {}
  function GM_getResourceText() { return ''; }
  function GM_getResourceURL() { return ''; }
  function GM_registerMenuCommand(name) { return name; }
  function GM_unregisterMenuCommand() {}
  function GM_info() { return {}; }
  var GM = {
    info: GM_info,
    getValue: function(k, d) { return Promise.resolve(GM_getValue(k, d)); },
    setValue: function() { return Promise.resolve(); },
    deleteValue: function() { return Promise.resolve(); },
    listValues: function() { return Promise.resolve([]); },
    xmlHttpRequest: GM_xmlhttpRequest,
    addStyle: GM_addStyle,
    setClipboard: GM_setClipboard,
    openInTab: GM_openInTab,
    notification: GM_notification,
    getResourceText: function() { return Promise.resolve(''); },
    getResourceUrl: function() { return Promise.resolve(''); },
    registerMenuCommand: GM_registerMenuCommand,
    unregisterMenuCommand: GM_unregisterMenuCommand,
    log: GM_log
  };
  window.GM_addStyle = GM_addStyle;
  window.GM_getValue = GM_getValue;
  window.GM_setValue = GM_setValue;
  window.GM_deleteValue = GM_deleteValue;
  window.GM_listValues = GM_listValues;
  window.GM_xmlhttpRequest = GM_xmlhttpRequest;
  window.GM_setClipboard = GM_setClipboard;
  window.GM_openInTab = GM_openInTab;
  window.GM_log = GM_log;
  window.GM_notification = GM_notification;
  window.GM_getResourceText = GM_getResourceText;
  window.GM_getResourceURL = GM_getResourceURL;
  window.GM_registerMenuCommand = GM_registerMenuCommand;
  window.GM_unregisterMenuCommand = GM_unregisterMenuCommand;
  window.GM_info = GM_info;
  window.GM = GM;
  window.unsafeWindow = window;
})();
"""
