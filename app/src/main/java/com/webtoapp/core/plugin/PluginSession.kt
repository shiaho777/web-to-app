package com.webtoapp.core.plugin

import android.webkit.WebView
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.util.GsonProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI-facing plugin state for one page/WebView, consumed by the Compose plugin
 * surface (toolbar button, plugin sheet, panel host).
 *
 * A [PluginSession] is created per configured WebView and published as
 * [PluginHostState.session]; Compose never talks to WebViewManager directly.
 */
object PluginHostState {

    /** A plugin relevant to the current page, as shown in the plugin sheet. */
    data class Entry(
        val pluginId: String,
        val name: String,
        val icon: String,
        val kind: PluginKind,
        val matchesCurrentUrl: Boolean,
        val hasPanel: Boolean,
        val badge: String = "",
        val badgeColor: String = "",
        /** Per-plugin host styles — resolved manifest preference or override. */
        val entryStyle: PluginEntryStyle = PluginEntryStyle.TOOLBAR,
        val panelStyle: PluginPanelStyle = PluginPanelStyle.BOTTOM_SHEET,
        /** GM_registerMenuCommand names for userscript entries. */
        val menuCommands: List<String> = emptyList()
    )

    /** A request to host a plugin panel surface. */
    data class PanelRequest(
        val pluginId: String,
        val kind: PluginKind,
        /** panel.html / popup.html content or a chrome-extension:// URL. */
        val url: String,
        /** For HCJ/USERSCRIPT panels: raw html loaded via loadDataWithBaseURL. */
        val html: String? = null,
        val baseUrl: String? = null,
        val panelStyle: PluginPanelStyle = PluginPanelStyle.BOTTOM_SHEET,
        val seq: Long = System.nanoTime()
    )

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    private val _panelRequest = MutableStateFlow<PanelRequest?>(null)
    val panelRequest: StateFlow<PanelRequest?> = _panelRequest.asStateFlow()

    private val _panelOpen = MutableStateFlow(false)
    val panelOpen: StateFlow<Boolean> = _panelOpen.asStateFlow()

    /** Whether the plugin sheet is visible — toggled by toolbar/menu/handle entries. */
    private val _sheetOpen = MutableStateFlow(false)
    val sheetOpen: StateFlow<Boolean> = _sheetOpen.asStateFlow()

    /** The session bound to the currently displayed WebView (set by WebViewManager). */
    @Volatile
    var session: PluginSession? = null
        private set

    internal fun publishSession(session: PluginSession?) {
        this.session = session
        if (session == null) {
            _entries.value = emptyList()
            _panelRequest.value = null
            _panelOpen.value = false
            _sheetOpen.value = false
        }
    }

    /** Called from any entry surface (toolbar button, menu item, floating handle). */
    fun openPluginSheet() {
        _sheetOpen.value = true
    }

    fun dismissPluginSheet() {
        _sheetOpen.value = false
    }

    internal fun publishEntries(entries: List<Entry>) {
        _entries.value = entries
    }

    internal fun updateBadge(pluginId: String, text: String, color: String) {
        _entries.value = _entries.value.map {
            if (it.pluginId == pluginId) it.copy(badge = text, badgeColor = color) else it
        }
    }

    internal fun updateMenuCommands(pluginId: String, names: List<String>) {
        _entries.value = _entries.value.map {
            if (it.pluginId == pluginId) it.copy(menuCommands = names) else it
        }
    }

    internal fun requestPanel(request: PanelRequest) {
        _panelRequest.value = request
        _panelOpen.value = true
    }

    fun dismissPanel() {
        val wasOpen = _panelOpen.value
        _panelOpen.value = false
        if (wasOpen) session?.onPanelClosed?.invoke()
    }

    internal fun clearPanelRequest() {
        _panelRequest.value = null
        _panelOpen.value = false
    }
}

/**
 * Per-WebView plugin context: resolves the active plugin set for a URL, issues
 * capability tokens, builds injection code, and routes bridge events to the UI
 * surface and back.
 *
 * `pluginSource` abstracts where plugins come from:
 *  - host preview: installed packages via the source lambda reading PluginStore
 *  - generated APK: embedded plugins decoded from shell config JSON
 */
class PluginSession(
    private val configStore: PluginConfigStore,
    private val appLang: () -> String,
    private val pageEvaluator: (String) -> Unit,
    private val notifySink: (pluginId: String, title: String, body: String) -> Unit = { _, _, _ -> }
) {

    /**
     * A plugin resolved for injection: code already loaded from package.
     * [attached] marks plugins the user explicitly attached to this app —
     * local-runtime pages only run attached ones (ambient/global set stays out).
     */
    data class Resolved(
        val plugin: Plugin,
        val mainJs: String = "",
        val css: String = "",
        val panelHtml: String = "",
        /** Pre-resolved `@require` bodies (url -> code). Host previews resolve
         * these lazily through ExtensionFileManager; embedded APK payloads carry
         * them inline because the cache does not exist there. */
        val requireContents: Map<String, String> = emptyMap(),
        val attached: Boolean = true
    )

    val bridge = PluginBridge(configStore, BridgeHost())

    /**
     * Bridge callbacks (`send`, `fetch` resolves, …) fire on the WebView's
     * JavaBridge HandlerThread, but `evaluateJavascript` must run on the main
     * thread — hop through this handler for every evaluator call.
     */
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    private fun evalOnPage(js: String) {
        mainHandler.post { pageEvaluator(js) }
    }

    private fun evalOnPanel(js: String) {
        mainHandler.post { panelEvaluator?.invoke(js) }
    }

    /** Panel messaging endpoint — set by the panel host while a panel is open. */
    @Volatile
    var panelEvaluator: ((String) -> Unit)? = null

    @Volatile
    var onPanelClosed: (() -> Unit)? = null

    @Volatile
    private var resolvedPlugins: List<Resolved> = emptyList()

    @Volatile
    private var currentUrl: String = ""

    fun setPlugins(plugins: List<Resolved>) {
        resolvedPlugins = plugins
        bridge.revokeAll()
        issuedTokens.clear()
        publishEntries()
    }

    fun onUrlChanged(url: String) {
        currentUrl = url
        publishEntries()
        // SPA navigations (pushState / doUpdateVisitedHistory) never reload the
        // document, so page-side plugin code would otherwise never learn the
        // URL changed — listeners keep binding to detached elements. Emit
        // `hcj.on('navigate')` to every plugin matching the new URL. On real
        // navigations the event fires into the dying context, harmlessly.
        if (url != lastNavEmittedUrl) {
            lastNavEmittedUrl = url
            val payload = org.json.JSONObject().put("url", url).toString()
            resolvedPlugins.forEach { r ->
                if (r.plugin.kind != PluginKind.CHROME_EXTENSION && r.plugin.matchesUrl(url)) {
                    evalOnPage(PluginInjection.emitEvent(r.plugin.id, "navigate", payload))
                }
            }
        }
    }

    private var lastNavEmittedUrl: String? = null

    fun pluginFor(id: String): Plugin? = resolvedPlugins.firstOrNull { it.plugin.id == id }?.plugin

    fun resolvedFor(id: String): Resolved? = resolvedPlugins.firstOrNull { it.plugin.id == id }

    private fun publishEntries() {
        PluginHostState.publishEntries(
            resolvedPlugins
                .filter { it.plugin.showInToolbar }
                .map { r ->
                    PluginHostState.Entry(
                        pluginId = r.plugin.id,
                        name = r.plugin.name,
                        icon = r.plugin.icon,
                        kind = r.plugin.kind,
                        matchesCurrentUrl = r.plugin.matchesUrl(currentUrl),
                        hasPanel = r.plugin.hasPanel,
                        entryStyle = r.plugin.entryStyle,
                        panelStyle = r.plugin.panelStyle
                    )
                }
        )
    }

    /** Plugins eligible to run on this page, honouring app-attached scoping. */
    fun activeFor(url: String, appAttachedOnly: Boolean = false): List<Resolved> =
        resolvedPlugins.filter { !appAttachedOnly || it.attached }

    /** Chrome-extension records in the active set (content lives in the engine). */
    fun chromeExtIds(url: String, appAttachedOnly: Boolean = false): List<String> =
        activeFor(url, appAttachedOnly)
            .filter { it.plugin.kind == PluginKind.CHROME_EXTENSION && it.plugin.chromeExtId.isNotEmpty() }
            .map { it.plugin.chromeExtId }
            .distinct()

    /**
     * Injection code for one run-at phase: bootstrap (start only), CSS for
     * matching plugins, then each matching plugin's wrapped main.js.
     * HCJ plugins only — userscripts and chrome extensions run their own paths.
     */
    fun injectionFor(runAt: PluginRunAt, url: String, appAttachedOnly: Boolean = false): String {
        val out = StringBuilder()
        val matching = activeFor(url, appAttachedOnly).filter { r ->
            r.plugin.kind == PluginKind.HCJ && r.plugin.runAt == runAt && r.plugin.matchesUrl(url)
        }
        if (matching.isEmpty()) return out.toString()
        if (runAt == PluginRunAt.DOCUMENT_START) {
            out.append(PluginInjection.BOOTSTRAP_JS).append('\n')
        }
        if (runAt == PluginRunAt.DOCUMENT_START) {
            matching.forEach { r ->
                if (r.css.isNotBlank()) {
                    out.append(PluginInjection.wrapCss(r.plugin.id, r.css)).append('\n')
                }
            }
        }
        matching.forEach { r ->
            if (r.mainJs.isNotBlank()) {
                out.append(
                    PluginInjection.wrapPluginCode(
                        plugin = r.plugin,
                        code = r.mainJs,
                        token = tokenFor(r.plugin.id),
                        appLang = appLang()
                    )
                ).append('\n')
            }
        }
        return out.toString()
    }

    private fun tokenFor(pluginId: String): String {
        // Re-issue deterministically per session/plugin: tokens were all issued
        // in setPlugins; find via a second issuance is wrong, so store map.
        return issuedTokens.getOrPut(pluginId) { bridge.issueToken(pluginId) }
    }

    private val issuedTokens = mutableMapOf<String, String>()

    /** Toolbar tap on a plugin without a panel: deliver `hcj.on('action')`. */
    fun emitAction(pluginId: String) {
        evalOnPage(PluginInjection.emitEvent(pluginId, "action", "null"))
    }

    /** Set by the runtime: what to do when a chrome-extension entry is tapped. */
    var onChromeEntry: ((Plugin) -> Unit)? = null

    /** Resolves a plugin id to its GM storage alias (userscript menu invocations). */
    var userscriptAliasFor: ((String) -> String)? = null

    /** Chrome popup/options pages are hosted by this factory inside the panel host. */
    var popupWebViewFactory: ((String) -> WebView?)? = null

    /**
     * Entry-point tap on a plugin sheet row / pinned icon:
     * panel if it has one, `action` event otherwise; chrome extensions route to
     * their popup through the same panel host ([panelRequestOverride] supplies
     * the chrome-extension:// URL, [popupWebViewFactory] builds the WebView).
     */
    fun activateEntry(pluginId: String) {
        val resolved = resolvedFor(pluginId) ?: return
        when {
            resolved.plugin.kind == PluginKind.CHROME_EXTENSION -> {
                val req = panelRequestOverride?.invoke(resolved)
                if (req != null) PluginHostState.requestPanel(req)
                else onChromeEntry?.invoke(resolved.plugin)
            }
            resolved.plugin.hasPanel -> openPanelRequest(resolved)
            else -> emitAction(pluginId)
        }
    }

    /** Invoke a `GM_registerMenuCommand` handler registered by a userscript. */
    fun invokeMenuCommand(pluginId: String, name: String) {
        val alias = userscriptAliasFor?.invoke(pluginId) ?: pluginId
        evalOnPage(
            "(function(){var m=window.__WTA_GM_MENU__&&window.__WTA_GM_MENU__[" +
                org.json.JSONObject.quote(alias) + "];var f=m&&m[" +
                org.json.JSONObject.quote(name) + "];if(f)try{f()}catch(e){console.error('[GM menu]',e)}})();"
        )
    }

    /** GM bridge pushed a script's menu-command set; refresh the sheet row. */
    fun updateMenuCommands(scriptId: String, names: List<String>) {
        PluginHostState.updateMenuCommands(scriptId, names)
    }

    /** Userscript plugins matching a phase — for the GM injection path. */
    fun userscriptsFor(runAt: PluginRunAt, url: String, appAttachedOnly: Boolean = false): List<Resolved> =
        activeFor(url, appAttachedOnly).filter { r ->
            r.plugin.kind == PluginKind.USERSCRIPT && r.plugin.runAt == runAt && r.plugin.matchesUrl(url)
        }

    /** A message arrived from the panel surface towards the page script. */
    fun deliverPanelMessageToPage(pluginId: String, json: String) {
        evalOnPage(PluginInjection.emitEvent(pluginId, "panel", json))
    }

    /** A message arrived from the page script towards the panel surface. */
    fun deliverPanelMessageToPanel(json: String) {
        evalOnPanel("window.__hcjPanelOnMsg && window.__hcjPanelOnMsg($json);")
    }

    fun destroy() {
        bridge.revokeAll()
        issuedTokens.clear()
        resolvedPlugins = emptyList()
        panelEvaluator = null
        if (PluginHostState.session === this) {
            PluginHostState.publishSession(null)
        }
    }

    private inner class BridgeHost : PluginBridge.Host {
        override fun evaluatePageJs(js: String) = evalOnPage(js)
        override fun pluginFor(pluginId: String): Plugin? = this@PluginSession.pluginFor(pluginId)

        override fun onBadge(pluginId: String, text: String, color: String) {
            PluginHostState.updateBadge(pluginId, text, color)
        }

        override fun onPanelOpen(pluginId: String) {
            val resolved = resolvedFor(pluginId) ?: return
            openPanelRequest(resolved)
        }

        override fun onPanelClose(pluginId: String) {
            PluginHostState.dismissPanel()
        }

        override fun onPanelMessage(pluginId: String, json: String) {
            deliverPanelMessageToPanel(json)
        }

        override fun onNotify(pluginId: String, title: String, body: String) {
            notifySink(pluginId, title, body)
        }
    }

    /**
     * Subclass/open hook: real panel content resolution needs the package
     * loader (host) or embedded payload (shell). Set by the creator.
     */
    /** Set when this session's WebView becomes the visible one. */
    fun attach() {
        PluginHostState.publishSession(this)
        publishEntries()
    }

    /**
     * Resolves a panel surface for a resolved plugin. HCJ/userscript panels
     * carry inline html; CHROME_EXTENSION records get their popup URL from the
     * chrome runtime via [panelRequestOverride]. A null return falls back to
     * the default inline-html request.
     */
    var panelRequestOverride: ((Resolved) -> PluginHostState.PanelRequest?)? = null

    /**
     * Panel-side bridge bound to one plugin (`window.__hcjPanelBridge`). The
     * panel host attaches it to the panel WebView while the panel is open.
     */
    fun createPanelBridge(pluginId: String): PluginPanelBridge =
        PluginPanelBridge(
            pluginId = pluginId,
            configStore = configStore,
            hasPermission = { p -> pluginFor(pluginId)?.hasPermission(p) == true },
            host = object : PluginPanelBridge.Host {
                override fun onPanelSend(pluginId: String, json: String) {
                    deliverPanelMessageToPage(pluginId, json)
                }

                override fun onPanelClose(pluginId: String) {
                    PluginHostState.dismissPanel()
                }
            }
        )

    private fun openPanelRequest(resolved: Resolved) {
        val req = (panelRequestOverride?.invoke(resolved)
            ?: PluginHostState.PanelRequest(
                pluginId = resolved.plugin.id,
                kind = resolved.plugin.kind,
                url = "",
                html = resolved.panelHtml.takeIf { it.isNotBlank() },
                baseUrl = "hcj-plugin://${resolved.plugin.id}/"
            )).copy(panelStyle = resolved.plugin.panelStyle)
        PluginHostState.requestPanel(req)
    }
}
