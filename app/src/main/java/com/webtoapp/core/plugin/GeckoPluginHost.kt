package com.webtoapp.core.plugin

import org.json.JSONObject

/**
 * Page plugins on GeckoView. The engine has no `addJavascriptInterface`, so
 * HCJ talks to the host through `prompt()` tokens that [handlePrompt] claims
 * before a dialog is shown. Config, badges, and panel messages use that
 * channel. Chrome extensions and the Greasemonkey bridge stay on System WebView.
 */
object GeckoPluginHost {
    const val CONFIG_PROMPT = "__wta_hcj_cfg__"
    const val BADGE_PROMPT = "__wta_hcj_badge__"
    const val PANEL_PROMPT = "__wta_hcj_panel__"

    fun runnable(plugins: List<PluginSession.Resolved>): List<PluginSession.Resolved> =
        plugins.filter { it.plugin.kind == PluginKind.HCJ || it.plugin.kind == PluginKind.USERSCRIPT }

    fun configSeed(store: PluginConfigStore, plugins: List<PluginSession.Resolved>): String {
        val root = JSONObject()
        for (resolved in runnable(plugins)) {
            val raw = store.all(resolved.plugin.id)
            val bag = try {
                JSONObject(raw)
            } catch (_: Exception) {
                JSONObject()
            }
            root.put(resolved.plugin.id, bag)
        }
        return root.toString()
    }

    fun pageScript(
        plugins: List<PluginSession.Resolved>,
        url: String,
        appLang: String,
        configSeedJson: String
    ): String {
        val matching = runnable(plugins).filter { it.plugin.matchesUrl(url) }
        if (matching.isEmpty()) return ""
        val hcj = matching.filter { it.plugin.kind == PluginKind.HCJ }
        val userscripts = matching.filter { it.plugin.kind == PluginKind.USERSCRIPT }
        val parts = ArrayList<String>()
        parts += CLEANUP_JS
        if (hcj.isNotEmpty()) {
            parts += bridgeStub(configSeedJson)
            parts += PluginInjection.BOOTSTRAP_JS
            parts += "window.__wtaAppLang=${jsString(appLang)};"
            for (resolved in hcj) {
                if (resolved.plugin.runAt == PluginRunAt.DOCUMENT_START && resolved.css.isNotBlank()) {
                    parts += PluginInjection.wrapCss(resolved.plugin.id, resolved.css)
                }
            }
            for (resolved in hcj) {
                if (resolved.mainJs.isBlank()) continue
                parts += PluginInjection.wrapPluginCode(
                    plugin = resolved.plugin,
                    code = resolved.mainJs,
                    token = "gecko",
                    appLang = appLang
                )
            }
        }
        if (userscripts.isNotEmpty()) {
            parts += USERSCRIPT_STUB
            for (resolved in userscripts) {
                if (resolved.mainJs.isBlank()) continue
                parts += "(function(){try{\n${resolved.mainJs}\n}catch(e){}})();"
            }
        }
        return parts.joinToString("\n")
    }

    /**
     * @return true when [message] is a plugin channel and the prompt must not
     * be shown. Config writes land in [store]. Badge and panel calls update
     * the host surface through [session].
     */
    fun handlePrompt(
        message: String?,
        value: String?,
        store: PluginConfigStore,
        session: PluginSession?
    ): Boolean {
        val payload = value.orEmpty()
        when (message) {
            CONFIG_PROMPT -> {
                applyConfig(store, payload)
                return true
            }
            BADGE_PROMPT -> {
                applyBadge(payload)
                return true
            }
            PANEL_PROMPT -> {
                val json = parsePanel(payload) ?: return true
                session?.deliverPanelMessageToPanel(json)
                return true
            }
            else -> return false
        }
    }

    private fun bridgeStub(configSeedJson: String): String {
        val literal = jsString(configSeedJson)
        return """
            try { delete window.__hcj; delete window.__hcjBridge; } catch (e) {}
            window.__hcjBridge = (function() {
              var seed = {};
              try { seed = JSON.parse($literal) || {}; } catch (e) {}
              function send(channel, body) {
                try { prompt(channel, JSON.stringify(body)); } catch (e) {}
              }
              function noop() {}
              return {
                configGet: function(pid, token, k) {
                  var bag = seed[pid];
                  if (!bag || !Object.prototype.hasOwnProperty.call(bag, k)) return null;
                  return JSON.stringify(bag[k]);
                },
                configSet: function(pid, token, k, json) {
                  if (!seed[pid]) seed[pid] = {};
                  try { seed[pid][k] = JSON.parse(json); } catch (e) { seed[pid][k] = json; }
                  send('${CONFIG_PROMPT}', { pid: pid, k: k, v: json });
                },
                configRemove: function(pid, token, k) {
                  if (seed[pid]) delete seed[pid][k];
                  send('${CONFIG_PROMPT}', { pid: pid, k: k, v: null });
                },
                configAll: function(pid, token) { return JSON.stringify(seed[pid] || {}); },
                fetch: function() {},
                notify: noop,
                badge: function(pid, token, text, color) {
                  send('${BADGE_PROMPT}', { pid: pid, text: text == null ? '' : String(text), color: color || '' });
                },
                panelOpen: noop,
                panelClose: noop,
                panelSend: function(pid, token, json) { send('${PANEL_PROMPT}', { pid: pid, json: json }); },
                share: noop,
                openExternal: noop,
                clearData: noop,
                exitApp: noop,
                log: function(pid, token, m) { try { console.log('[hcj]', m); } catch (e) {} }
              };
            })();
        """.trimIndent()
    }

    private fun applyConfig(store: PluginConfigStore, payload: String) {
        val obj = try {
            JSONObject(payload)
        } catch (_: Exception) {
            return
        }
        val pid = obj.optString("pid")
        val key = obj.optString("k")
        if (pid.isBlank() || key.isBlank()) return
        if (obj.isNull("v")) {
            store.remove(pid, key)
        } else {
            store.set(pid, key, obj.optString("v"))
        }
    }

    private fun applyBadge(payload: String) {
        val obj = try {
            JSONObject(payload)
        } catch (_: Exception) {
            return
        }
        val pid = obj.optString("pid")
        if (pid.isBlank()) return
        PluginHostState.updateBadge(pid, obj.optString("text"), obj.optString("color"))
    }

    private fun jsString(value: String): String = buildString {
        append('"')
        for (ch in value) {
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(ch)
            }
        }
        append('"')
    }

    private fun parsePanel(payload: String): String? {
        val obj = try {
            JSONObject(payload)
        } catch (_: Exception) {
            return null
        }
        return if (obj.isNull("json")) null else obj.optString("json")
    }
}

private const val CLEANUP_JS = """
(function(){
  try { delete window.__wta_gecko_userscript__; } catch (e) {}
  var dark = document.getElementById('wta-dark-mode');
  if (dark) dark.remove();
  var nodes = document.querySelectorAll('style[id^="hcj-css-"],style[data-wta-gecko="userscript"]');
  for (var i = 0; i < nodes.length; i++) nodes[i].remove();
})();
"""

private const val USERSCRIPT_STUB = """
(function(){
  if (window.__wta_gecko_userscript__) return;
  window.__wta_gecko_userscript__ = true;
  function GM_addStyle(css) {
    var s = document.createElement('style');
    s.setAttribute('data-wta-gecko', 'userscript');
    s.textContent = css == null ? '' : String(css);
    (document.head || document.documentElement).appendChild(s);
    return s;
  }
  function GM_getValue(k, d) { return d; }
  function GM_setValue() {}
  function GM_xmlhttpRequest() { return { abort: function() {} }; }
  window.GM_addStyle = GM_addStyle;
  window.GM_getValue = GM_getValue;
  window.GM_setValue = GM_setValue;
  window.GM_xmlhttpRequest = GM_xmlhttpRequest;
  window.GM = { addStyle: GM_addStyle, getValue: function(k, d) { return Promise.resolve(d); }, setValue: function() { return Promise.resolve(); } };
  window.unsafeWindow = window;
})();
"""
