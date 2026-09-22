package com.webtoapp.core.plugin

import com.webtoapp.util.GsonProvider
import java.security.SecureRandom

/**
 * Builds the JavaScript injected into a page for HCJ / userscript plugins.
 *
 * Layout per page:
 *  1. [BOOTSTRAP_JS] once per document (document-start) — defines
 *     `window.__hcj`, the runtime that manufactures a scoped `hcj` API object
 *     per plugin and dispatches host -> page events.
 *  2. One wrapped IIFE per plugin carrying a capability token. The token is
 *     issued by [PluginBridge] per WebView, so a script cannot impersonate
 *     another plugin's permissions by guessing its id.
 *  3. `style.css` content is injected as a `<style id="hcj-css-<id>">` element
 *     at document-start so visual plugins apply before first paint.
 */
object PluginInjection {

    private val random = SecureRandom()

    fun newToken(): String =
        random.nextLong().toString(16) + random.nextLong().toString(16)

    /**
     * Page-side runtime. Exposed as `window.__hcj` (factory + event bus); the
     * per-plugin API object handed to plugin code is the `hcj` const created by
     * [wrapPluginCode].
     */
    const val BOOTSTRAP_JS = """
(function(){
  if (window.__hcj) return;
  var B = window.__hcjBridge;
  window.__hcj = {
    _cbs: {}, _cbSeq: 0, _on: {},
    _mk: function(pid, token, manifestJson, lang) {
      var manifest = {};
      try { manifest = JSON.parse(manifestJson); } catch (e) {}
      function req(name) { return function() {
        var args = [pid, token].concat(Array.prototype.slice.call(arguments));
        return B[name].apply(B, args);
      };}
      var hcj = {
        id: pid,
        manifest: manifest,
        lang: lang,
        config: {
          get: function(k, d) {
            var v = B.configGet(pid, token, k);
            if (v === null || v === undefined) return d;
            try { return JSON.parse(v); } catch (e) { return d; }
          },
          set: function(k, v) { B.configSet(pid, token, k, JSON.stringify(v === undefined ? null : v)); },
          remove: function(k) { B.configRemove(pid, token, k); },
          all: function() { try { return JSON.parse(B.configAll(pid, token) || '{}'); } catch (e) { return {}; } }
        },
        fetch: function(url, opts) {
          return new Promise(function(resolve, reject) {
            var cbId = 'cb' + (++window.__hcj._cbSeq) + '_' + Date.now();
            window.__hcj._cbs[cbId] = [resolve, reject];
            B.fetch(pid, token, String(url), JSON.stringify(opts || {}), cbId);
          });
        },
        notify: function(title, body) { B.notify(pid, token, String(title), String(body || '')); },
        toast: function(msg) {
          var t = document.getElementById('hcj-toast-' + pid);
          if (!t) {
            t = document.createElement('div');
            t.id = 'hcj-toast-' + pid;
            t.style.cssText = 'position:fixed;left:50%;bottom:28px;transform:translateX(-50%);' +
              'background:rgba(17,24,39,.92);color:#fff;padding:9px 18px;border-radius:20px;' +
              'font-size:13px;line-height:1.4;z-index:2147483647;pointer-events:none;' +
              'opacity:0;transition:opacity .2s;max-width:80%;text-align:center';
            (document.body || document.documentElement).appendChild(t);
          }
          t.textContent = String(msg);
          t.style.opacity = '1';
          clearTimeout(t.__hcjT);
          t.__hcjT = setTimeout(function() { t.style.opacity = '0'; }, 1600);
        },
        addStyle: function(css) {
          var id = 'hcj-css-' + pid;
          var s = document.getElementById(id);
          if (!s) { s = document.createElement('style'); s.id = id; (document.head || document.documentElement).appendChild(s); }
          s.textContent = String(css);
        },
        badge: function(text, color) { B.badge(pid, token, text === null || text === undefined ? '' : String(text), color || ''); },
        panel: {
          open: function() { B.panelOpen(pid, token); },
          close: function() { B.panelClose(pid, token); },
          send: function(msg) { B.panelSend(pid, token, JSON.stringify(msg === undefined ? null : msg)); },
          onMessage: function(fn) { window.__hcj._on[pid + ':panel'] = fn; }
        },
        on: function(evt, fn) { window.__hcj._on[pid + ':' + evt] = fn; },
        emit: function(evt, data) { window.__hcj._emit(pid, evt, JSON.stringify(data === undefined ? null : data)); },
        log: function(m) { B.log(pid, token, String(m)); }
      };
      return hcj;
    },
    _resolve: function(cbId, ok, json) {
      var c = window.__hcj._cbs[cbId];
      if (!c) return;
      delete window.__hcj._cbs[cbId];
      if (ok) { try { c[0](JSON.parse(json)); } catch (e) { c[0](json); } }
      else { c[1](new Error(json)); }
    },
    _emit: function(pid, evt, json) {
      var f = window.__hcj._on[pid + ':' + evt];
      if (!f) return;
      var data = null;
      try { data = JSON.parse(json); } catch (e) {}
      try { f(data); } catch (e) { console.error('[hcj] handler error', e); }
    }
  };
})();
"""

    /**
     * Panel-side runtime injected into the panel WebView before `panel.html`
     * scripts run: `window.hcjPanel` mirrors the page API minus page-only
     * members, plus send/onMessage/close for the page <-> panel channel.
     */
    const val PANEL_BOOTSTRAP_PREFIX = """
(function(){
  var B = window.__hcjPanelBridge;
  var pid = """

    const val PANEL_BOOTSTRAP_SUFFIX = """;
  window.hcjPanel = {
    id: pid,
    config: {
      get: function(k, d) {
        var v = B.configGet(pid, k);
        if (v === null || v === undefined) return d;
        try { return JSON.parse(v); } catch (e) { return d; }
      },
      set: function(k, v) { B.configSet(pid, k, JSON.stringify(v === undefined ? null : v)); },
      remove: function(k) { B.configRemove(pid, k); },
      all: function() { try { return JSON.parse(B.configAll(pid) || '{}'); } catch (e) { return {}; } }
    },
    send: function(msg) { B.send(pid, JSON.stringify(msg === undefined ? null : msg)); },
    onMessage: function(fn) { window.__hcjPanelOnMsg = fn; },
    close: function() { B.close(pid); },
    toast: function(msg) {
      var t = document.getElementById('hcj-panel-toast');
      if (!t) {
        t = document.createElement('div');
        t.id = 'hcj-panel-toast';
        t.style.cssText = 'position:fixed;left:50%;bottom:28px;transform:translateX(-50%);' +
          'background:rgba(17,24,39,.92);color:#fff;padding:9px 18px;border-radius:20px;' +
          'font-size:13px;line-height:1.4;z-index:2147483647;pointer-events:none;' +
          'opacity:0;transition:opacity .2s;max-width:80%;text-align:center';
        (document.body || document.documentElement).appendChild(t);
      }
      t.textContent = String(msg);
      t.style.opacity = '1';
      clearTimeout(t.__hcjT);
      t.__hcjT = setTimeout(function() { t.style.opacity = '0'; }, 1600);
    }
  };
})();
"""

    fun panelBootstrap(pluginId: String): String =
        PANEL_BOOTSTRAP_PREFIX + "'" + pluginId.escapeForJsSingleQuote() + "'" + PANEL_BOOTSTRAP_SUFFIX

    /**
     * Wrap plugin `main.js` in an IIFE with a scoped `hcj` object bound to the
     * issued capability token.
     */
    fun wrapPluginCode(
        plugin: Plugin,
        code: String,
        token: String,
        appLang: String
    ): String {
        val manifestJson = GsonProvider.gson.toJson(
            mapOf(
                "id" to plugin.id,
                "name" to plugin.name,
                "version" to plugin.versionName,
                "icon" to plugin.icon,
                "hasPanel" to plugin.hasPanel
            )
        )
        return buildString {
            append(";(function(){\n'use strict';\n")
            append("var hcj = window.__hcj && window.__hcj._mk('")
            append(plugin.id.escapeForJsSingleQuote())
            append("','")
            append(token.escapeForJsSingleQuote())
            append("','")
            append(manifestJson.escapeForJsSingleQuote())
            append("','")
            append(appLang.escapeForJsSingleQuote())
            append("');\n")
            if (plugin.legacyCompat) {
                // Retired-module globals mapped onto hcj.* so converted
                // packages keep working. The old panel/DSL runtime is gone;
                // register() degrades to wiring the action click only.
                append("var getConfig = function(k,d){ return hcj.config.get(k,d); };\n")
                append("var __MODULE_INFO__ = hcj.manifest || {};\n")
                append("var __MODULE_UI_CONFIG__ = {};\n")
                append("var __MODULE_RUN_MODE__ = 'auto';\n")
                append("var __WTA_MODULE_UI__ = { register: function(def) {")
                append(" if (def && typeof def.onClick === 'function') hcj.on('action', def.onClick); } };\n")
            }
            append("try {\n")
            append(code)
            append("\n} catch(e) { console.error('[HCJ: ")
            append(plugin.name.escapeForJsSingleQuote())
            append("]', e); }\n})();")
        }
    }

    /** CSS injected as a style element; idempotent per plugin per document. */
    fun wrapCss(pluginId: String, css: String): String = buildString {
        append("(function(){var id='hcj-css-")
        append(pluginId.escapeForJsSingleQuote())
        append("';if(document.getElementById(id))return;")
        append("var s=document.createElement('style');s.id=id;s.textContent=`")
        append(css.escapeForJsTemplate())
        append("`;(document.head||document.documentElement).appendChild(s);})();")
    }

    /** Dispatch a host event (`action`, `panel`) into the page runtime. */
    fun emitEvent(pluginId: String, event: String, jsonPayload: String): String =
        "window.__hcj && window.__hcj._emit('" + pluginId.escapeForJsSingleQuote() +
            "','" + event.escapeForJsSingleQuote() + "'," +
            "'" + jsonPayload.escapeForJsSingleQuote() + "');"

    /** Resolve a pending `hcj.fetch` promise. */
    fun resolveFetch(cbId: String, ok: Boolean, jsonPayload: String): String =
        "window.__hcj && window.__hcj._resolve('" + cbId.escapeForJsSingleQuote() +
            "'," + ok + ",'" + jsonPayload.escapeForJsSingleQuote() + "');"
}
