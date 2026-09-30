package com.webtoapp.core.engine

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.security.KeyChain
import com.webtoapp.core.logging.AppLogger
import android.view.View
import com.webtoapp.data.model.UserAgentMode
import com.webtoapp.data.model.WebViewConfig
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.StorageController
import org.mozilla.geckoview.WebRequestError
import org.mozilla.geckoview.WebResponse
import org.mozilla.geckoview.WebExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.geckoview.MediaSession as GeckoMediaSession

data class ProxyConfig(
    val mode: String = "NONE",
    val host: String = "",
    val port: Int = 0,
    val type: String = "HTTP",
    val pacUrl: String = "",
    val username: String = "",
    val password: String = ""
)

class GeckoViewEngine(
    private val context: Context
) : BrowserEngine {

    companion object {
        private const val TAG = "GeckoViewEngine"

        /** Cache dir (under cacheDir) holding materialized file-picker uploads (#638). */
        private const val UPLOAD_CACHE_DIR = "gecko_uploads"

        @Volatile
        private var sharedRuntime: GeckoRuntime? = null

        /**
         * Every GeckoSession opened on [sharedRuntime] across all engine
         * instances. Multi-web apps run several sessions on one shared runtime
         * — recreating the runtime for a config change would kill every live
         * session, so [ensureRuntimeForConfig] defers the recreate while any
         * are open (#1035).
         */
        private val liveSessions =
            java.util.Collections.synchronizedSet(mutableSetOf<GeckoSession>())

        @Volatile
        private var runtimeConfigFingerprint: String = ""

        @Volatile
        private var nativeBridgeExtension: WebExtension? = null

        @Volatile
        private var activeNativeBridge: com.webtoapp.core.webview.NativeBridge? = null

        private const val NATIVE_BRIDGE_APP = "wta_native_bridge"

        fun ensureNativeBridgeExtension(runtime: GeckoRuntime): WebExtension? {
            nativeBridgeExtension?.let { return it }
            synchronized(this) {
                nativeBridgeExtension?.let { return it }
                val url = "resource://android/assets/web_extensions/wta_native_bridge/"
                val controller = runtime.webExtensionController
                val installResult = controller.installBuiltIn(url)
                installResult.accept { ext ->
                    ext?.setMessageDelegate(object : WebExtension.MessageDelegate {
                        override fun onMessage(
                            nativeApp: String,
                            message: Any,
                            sender: WebExtension.MessageSender
                        ): GeckoResult<Any>? {
                            val bridge = activeNativeBridge
                            if (bridge == null) {
                                return GeckoResult.fromValue(errorJson("REQUEST_FAILED", "Native bridge not ready"))
                            }
                            val requestJson = when (message) {
                                is String -> message
                                else -> message.toString()
                            }
                            return try {
                                val response = bridge.httpRequest(requestJson)
                                GeckoResult.fromValue(response)
                            } catch (e: Exception) {
                                GeckoResult.fromValue(errorJson("REQUEST_FAILED", e.message ?: e::class.java.simpleName))
                            }
                        }
                    }, NATIVE_BRIDGE_APP)
                    nativeBridgeExtension = ext
                    AppLogger.d(TAG, "Native bridge WebExtension installed")
                }
                return null
            }
        }

        private fun errorJson(code: String, message: String): String {
            return org.json.JSONObject().apply {
                put("ok", false)
                put("error", code)
                put("message", message)
            }.toString()
        }

        private fun currentConfigFingerprint(): String {
            val ech = currentDnsConfig?.echEffective == true
            val proxy = currentProxyConfig?.let { buildProxyPrefs(it) } ?: emptyMap()
            val proxyKey = proxy.entries.joinToString(",") { "${it.key}=${it.value}" }
            return "ech=$ech|proxy=$proxyKey|tlsMitm=$tlsMitmActive|enterpriseRoots=$enterpriseRootsEnabled|antiCapture=$antiCaptureActive|autoplay=$autoplayAllowed"
        }

        fun getRuntime(context: Context): GeckoRuntime {
            return sharedRuntime ?: synchronized(this) {
                sharedRuntime ?: createRuntime(context.applicationContext).also {
                    sharedRuntime = it
                    runtimeConfigFingerprint = currentConfigFingerprint()
                }
            }
        }

        fun ensureRuntimeForConfig(context: Context) {
            val want = currentConfigFingerprint()
            synchronized(this) {
                val existing = sharedRuntime
                if (existing == null) {
                    AppLogger.d(TAG, "ensureRuntimeForConfig: no existing runtime, creating new (want=$want)")
                    getRuntime(context)
                    return
                }
                if (want != runtimeConfigFingerprint) {
                    if (liveSessions.isNotEmpty()) {
                        // Multi-web: a recreate would kill the sessions other
                        // sites are using. Keep the existing runtime — the new
                        // global config applies on next process start (#1035).
                        AppLogger.w(
                            TAG,
                            "GeckoRuntime config change (want=$want, current=$runtimeConfigFingerprint) " +
                                "skipped: ${liveSessions.size} live session(s) would be killed"
                        )
                        return
                    }
                    AppLogger.i(
                        TAG,
                        "Recreating GeckoRuntime to apply config change (want=$want, current=$runtimeConfigFingerprint)"
                    )
                    try {
                        existing.shutdown()
                        clearGeckoProfileDir(context)
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "GeckoRuntime shutdown failed during config recreate: ${e.message}")
                    }
                    sharedRuntime = null
                    getRuntime(context)
                } else {
                    AppLogger.d(TAG, "ensureRuntimeForConfig: config unchanged (want=$want), reusing existing runtime")
                }
            }
        }

        @Volatile
        private var currentDnsConfig: com.webtoapp.data.model.DnsConfig? = null

        @Volatile
        private var currentProxyConfig: ProxyConfig? = null

        @Volatile
        private var tlsMitmActive: Boolean = false

        @Volatile
        private var antiCaptureActive: Boolean = false

        @Volatile
        private var enterpriseRootsEnabled: Boolean = false

        fun setTlsMitmActive(active: Boolean) {
            tlsMitmActive = active
            // Gecko's network stack has no per-request callback the host can feed the MITM
            // bridge's host allowlist from, so enforcing it here would block legitimate
            // third-party resource CONNECTs. The WebView engine path keeps enforcement on.
            com.webtoapp.core.webview.TlsMitmBridge.setHostAllowlistEnforcement(!active)
        }

        fun applyAntiCapture(active: Boolean) {
            antiCaptureActive = active
            AppLogger.d(TAG, "applyAntiCapture: active=$active")
        }

        @Volatile
        private var autoplayAllowed: Boolean = true

        /**
         * Autoplay parity with the WebView path's `mediaPlaybackRequiresUserGesture`:
         * mediaAutoplayEnabled=false (the default) must block un-gestured playback on
         * Gecko too, instead of silently leaving Gecko's allow-by-default policy in place.
         * Applied through the `media.autoplay.default` pref at runtime creation.
         */
        fun applyAutoplayPolicy(allowed: Boolean) {
            autoplayAllowed = allowed
            AppLogger.d(TAG, "applyAutoplayPolicy: allowed=$allowed")
        }

        fun applyEnterpriseRootsEnabled(enabled: Boolean) {
            enterpriseRootsEnabled = enabled
            AppLogger.d(TAG, "Android user CA trust for GeckoView: enabled=$enabled")
        }

        fun applyDnsConfig(config: com.webtoapp.data.model.DnsConfig) {
            currentDnsConfig = config
            AppLogger.d(TAG, "applyDnsConfig: provider=${config.provider}, echEnabled=${config.echEnabled}, echEffective=${config.echEffective}, dohMode=${config.dohMode}, dohUrl=${config.effectiveDohUrl}")
            val runtime = sharedRuntime ?: return
            applyDohToRuntime(runtime, config)
        }

        fun applyProxyConfig(config: ProxyConfig) {
            currentProxyConfig = config

            val runtime = sharedRuntime
            if (runtime != null && config.mode != "NONE") {
                AppLogger.w(TAG, "GeckoView proxy set after runtime creation — proxy will take effect on next runtime creation")
            }
            AppLogger.d(TAG, "Proxy config stored: mode=${config.mode}, type=${config.type}, host=${config.host}:${config.port}")
        }

        private fun buildProxyPrefs(config: ProxyConfig): Map<String, Any> {
            if (config.mode == "NONE") return emptyMap()
            val prefs = LinkedHashMap<String, Any>()
            when (config.mode) {
                "STATIC" -> {
                    if (config.host.isBlank() || config.port <= 0) return emptyMap()

                    prefs["network.proxy.type"] = 1

                    when (config.type.uppercase()) {
                        "SOCKS5", "SOCKS" -> {
                            prefs["network.proxy.socks"] = config.host
                            prefs["network.proxy.socks_port"] = config.port
                            prefs["network.proxy.socks_version"] = 5

                            prefs["network.proxy.socks_remote_dns"] = true
                        }
                        "HTTPS" -> {

                            prefs["network.proxy.ssl"] = config.host
                            prefs["network.proxy.ssl_port"] = config.port
                            prefs["network.proxy.http"] = config.host
                            prefs["network.proxy.http_port"] = config.port
                            prefs["network.proxy.share_proxy_settings"] = true
                        }
                        else -> {

                            prefs["network.proxy.http"] = config.host
                            prefs["network.proxy.http_port"] = config.port
                            prefs["network.proxy.ssl"] = config.host
                            prefs["network.proxy.ssl_port"] = config.port
                            prefs["network.proxy.share_proxy_settings"] = true
                        }
                    }
                }
                "PAC" -> {
                    if (config.pacUrl.isBlank()) return emptyMap()

                    prefs["network.proxy.type"] = 2
                    prefs["network.proxy.autoconfig_url"] = config.pacUrl
                }
            }
            return prefs
        }

        private fun applyDohToRuntime(runtime: GeckoRuntime, config: com.webtoapp.data.model.DnsConfig) {
            val dohUrl = config.effectiveDohUrl
            if (dohUrl.isBlank()) {

                runtime.settings.setTrustedRecursiveResolverMode(GeckoRuntimeSettings.TRR_MODE_OFF)
                AppLogger.d(TAG, "DoH disabled, using system DNS")
                return
            }

            val trrMode = if (config.dohMode == "strict" || config.bypassSystemDns) {
                GeckoRuntimeSettings.TRR_MODE_ONLY
            } else {
                GeckoRuntimeSettings.TRR_MODE_FIRST
            }

            runtime.settings.setTrustedRecursiveResolverMode(trrMode)
            runtime.settings.setTrustedRecursiveResolverUri(dohUrl)

            AppLogger.d(TAG, "DoH applied to GeckoView: provider=${config.provider}, mode=$trrMode, url=$dohUrl")

            if (config.echEffective) {
                AppLogger.d(
                    TAG,
                    "ECH requested but only takes effect from runtime creation (via configFilePath prefs); " +
                        "it will apply on next GeckoRuntime creation"
                )
            }
        }

        private const val GECKO_CONFIG_FILE = "geckoview-config.yaml"

        private fun writeGeckoConfigFile(context: Context, config: com.webtoapp.data.model.DnsConfig?): String {
            val configFile = java.io.File(context.filesDir, GECKO_CONFIG_FILE)
            val prefs = LinkedHashMap<String, Any>()
            if (config?.echEffective == true) {
                prefs["network.dns.echconfig.enabled"] = true
                prefs["network.dns.http3_echconfig.enabled"] = true
                prefs["network.dns.upgrade_with_https_rr"] = true
                prefs["network.dns.use_https_rr_as_altsvc"] = true
                prefs["network.dns.echconfig.fallback_to_origin_when_all_failed"] = true
                prefs["network.dns.force_use_https_rr"] = true
            }

            val appProxy = currentProxyConfig
            val hasAppProxy = appProxy != null && appProxy.mode != "NONE"
            if (antiCaptureActive && !hasAppProxy) {
                prefs["network.proxy.type"] = 0
            } else {
                appProxy?.let { prefs.putAll(buildProxyPrefs(it)) }
            }

            // media.autoplay.default: 0 = allow, 1 = block audible without user gesture
            // (Firefox's own default). Mirrors WebView's mediaPlaybackRequiresUserGesture.
            prefs["media.autoplay.default"] = if (autoplayAllowed) 0 else 1

            if (antiCaptureActive) {
                prefs["security.enterprise_roots.enabled"] = false
            } else if (tlsMitmActive || enterpriseRootsEnabled) {
                prefs["security.enterprise_roots.enabled"] = true
            }

            val yaml = buildString {
                append("prefs:\n")
                if (prefs.isEmpty()) {
                    append("  {}\n")
                } else {
                    prefs.forEach { (key, value) ->
                        append("  ").append(key).append(": ").append(yamlValue(value)).append("\n")
                    }
                }
            }

            return try {
                configFile.writeText(yaml)
                AppLogger.d(TAG, "Gecko config written (prefs=${prefs.size}): ${configFile.absolutePath}")
                AppLogger.d(TAG, "Gecko config content:\n$yaml")
                configFile.absolutePath
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to write Gecko config file", e)
                ""
            }
        }

        private fun yamlValue(value: Any): String = when (value) {
            is Boolean, is Int, is Long -> value.toString()
            else -> "\"" + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        }

        /**
         * GeckoView 142 race workaround (#582). GeckoRuntime.init() calls
         * ProcessLifecycleOwner.addObserver(LifecycleListener) right after
         * GeckoThread.launch(). When the process lifecycle is already RESUMED at that
         * moment (generated apps create the engine from Compose's first layout, after
         * the activity has resumed), LifecycleRegistry synchronously dispatches
         * ON_RESUME and LifecycleListener.onResume() dereferences
         * ThreadUtils.sGeckoHandler — a field the native side only assigns later on
         * the gecko thread, so the dereference is a guaranteed NPE. Seed it with the
         * main handler; the only runnable ever posted through this path
         * (Clipboard.updateSequenceNumber) is main-thread safe, and native overwrites
         * the field with the real gecko-thread handler once the thread boots.
         */
        internal fun ensureGeckoHandlerSeeded() {
            try {
                if (org.mozilla.gecko.util.ThreadUtils.sGeckoHandler == null) {
                    org.mozilla.gecko.util.ThreadUtils.sGeckoHandler =
                        android.os.Handler(android.os.Looper.getMainLooper())
                    AppLogger.i(TAG, "Seeded ThreadUtils.sGeckoHandler with main handler (lifecycle race workaround)")
                }
            } catch (t: Throwable) {
                AppLogger.w(TAG, "sGeckoHandler seed failed: ${t.message}")
            }
        }

        private fun createRuntime(context: Context): GeckoRuntime {
            ensureGeckoHandlerSeeded()
            val settingsBuilder = GeckoRuntimeSettings.Builder()
                .javaScriptEnabled(true)
                .consoleOutput(true)
                .contentBlocking(

                    ContentBlocking.Settings.Builder()
                        .antiTracking(ContentBlocking.AntiTracking.NONE)
                        .safeBrowsing(ContentBlocking.SafeBrowsing.NONE)
                        .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_ALL)
                        .build()
                )

            currentDnsConfig?.let { config ->
                val dohUrl = config.effectiveDohUrl
                if (dohUrl.isNotBlank()) {
                    val trrMode = if (config.dohMode == "strict" || config.bypassSystemDns || config.echEffective) {
                        GeckoRuntimeSettings.TRR_MODE_ONLY
                    } else {
                        GeckoRuntimeSettings.TRR_MODE_FIRST
                    }
                    settingsBuilder.trustedRecursiveResolverMode(trrMode)
                    settingsBuilder.trustedRecursiveResolverUri(dohUrl)
                }
            }

            val configFilePath = writeGeckoConfigFile(context, currentDnsConfig)
            if (configFilePath.isNotBlank()) {
                settingsBuilder.configFilePath(configFilePath)
                AppLogger.d(TAG, "GeckoView configFilePath set: $configFilePath (ech=${currentDnsConfig?.echEffective == true}, proxy=${currentProxyConfig?.mode ?: "NONE"})")
            }

            // Host APKs carry no gecko natives/omni.ja (downloaded on demand):
            // graft+preload the downloaded libs and hand Gecko a context whose
            // getPackageResourcePath() resolves to a packaged-style omnijar
            // container (assets/omni.ja inside a zip).
            return GeckoRuntime.create(
                GeckoRuntimeProvisioner.prepareRuntime(context),
                settingsBuilder.build()
            )
        }

        private fun clearGeckoProfileDir(context: Context) {
            try {
                val profileDir = java.io.File(context.filesDir, "geckoview")
                if (profileDir.exists()) {
                    profileDir.deleteRecursively()
                    AppLogger.d(TAG, "GeckoView profile cleared for fresh ECH/pref init")
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to clear GeckoView profile: ${e.message}")
            }
        }
    }

    override val engineType = EngineType.GECKOVIEW

    /**
     * The app's own origin URL (target URL / local base), set by EngineViewFactory before
     * createView. Feeds the NativeBridge caller gate — GeckoView has no WebView whose `.url`
     * could be consulted, so without this (plus [currentUrl] as the page-URL provider) every
     * CORS-bypass / private-network request from the bridge WebExtension is rejected.
     */
    var appOriginUrl: String = ""

    private var geckoView: GeckoView? = null
    private var session: GeckoSession? = null
    private var callback: BrowserEngineCallback? = null

    /**
     * Media session delegate to re-attach on every session (re)creation —
     * GeckoView rebuilds sessions per createView, so a delegate installed
     * once would be lost (#593).
     */
    private var mediaSessionDelegate: GeckoMediaSession.Delegate? = null
    private var currentUrl: String? = null
    private var currentTitle: String? = null
    private var canGoBackFlag = false
    private var canGoForwardFlag = false

    private var lastConfig: WebViewConfig? = null
    private var lastGeckoUaMode: Int = GeckoSessionSettings.USER_AGENT_MODE_MOBILE
    private var lastGeckoViewportMode: Int = GeckoSessionSettings.VIEWPORT_MODE_MOBILE
    private var lastAllowJavascript: Boolean = true
    private var lastUserAgentOverride: String? = null

    private var bridgeScope: kotlinx.coroutines.CoroutineScope? = null

    /**
     * Drives the off-main copy + @UiThread confirm for picked files (#638). Cancelled on
     * destroy() so a pending upload cannot outlive the engine.
     */
    private var filePromptScope: kotlinx.coroutines.CoroutineScope? = null

    override fun createView(
        context: Context,
        config: WebViewConfig,
        callback: BrowserEngineCallback
    ): View {
        // Retire a pre-existing session first: an engine reused for a second
        // createView must not orphan the earlier session — it would stay open
        // and pinned in liveSessions forever, permanently deferring runtime
        // recreation via ensureRuntimeForConfig (#1035 skip logic counts it).
        // Doing this before ensureRuntimeForConfig also keeps a stale session
        // owned by this engine from counting against the recreate decision.
        session?.let { old ->
            try { old.close() } catch (_: Exception) {}
            liveSessions.remove(old)
            session = null
        }
        geckoView?.releaseSession()
        geckoView = null

        this.callback = callback
        this.lastConfig = config

        val echInfo = if (config.dnsMode != "SYSTEM") {
            applyDnsConfig(config.dnsConfig)
            val ech = config.dnsConfig.echEffective
            if (ech) "ECH_ENABLED" else "ECH_DISABLED"
        } else {
            "SYSTEM_DNS"
        }
        AppLogger.i(TAG, "createView: engine=GeckoView, engineType=${engineType}, dnsMode=${config.dnsMode}, ech=$echInfo")
        ensureRuntimeForConfig(context)

        val runtime = getRuntime(context)

        if (config.clearBrowsingDataOnLaunch) {
            try {
                runtime.storageController.clearData(StorageController.ClearFlags.ALL)
                AppLogger.i(TAG, "Cleared browsing data on launch (clearBrowsingDataOnLaunch)")
            } catch (e: Exception) {
                AppLogger.w(TAG, "clearBrowsingDataOnLaunch failed: ${e.message}")
            }
        }

        if (config.enableCorsBypass || config.enablePrivateNetworkBridge) {
            if (bridgeScope == null) bridgeScope = kotlinx.coroutines.MainScope()
            val bridge = com.webtoapp.core.webview.NativeBridge(
                context = context.applicationContext,
                scope = bridgeScope!!,
                webViewProvider = { null },
                capabilities = config.nativeBridgeCapabilities,
                corsBypass = config.enableCorsBypass,
                downloadLocationMode = config.downloadLocationMode,
                customDownloadDirUri = config.customDownloadDirUri,
                appOriginUrl = appOriginUrl,
                callerPageUrlProvider = { currentUrl }
            )
            activeNativeBridge = bridge
            ensureNativeBridgeExtension(runtime)
        }

        val geckoUaMode = when {
            config.desktopMode -> GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
            config.userAgentMode == UserAgentMode.CHROME_DESKTOP ||
                config.userAgentMode == UserAgentMode.SAFARI_DESKTOP ||
                config.userAgentMode == UserAgentMode.FIREFOX_DESKTOP ||
                config.userAgentMode == UserAgentMode.EDGE_DESKTOP ->
                GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
            else -> GeckoSessionSettings.USER_AGENT_MODE_MOBILE
        }
        this.lastGeckoUaMode = geckoUaMode

        // Parity with the WebView path's useWideViewPort/loadWithOverviewMode (FIT_SCREEN)
        // and desktopMode. CUSTOM stays MOBILE — its meta-viewport injection is JS-based and
        // has no Gecko counterpart yet.
        val geckoViewportMode = when {
            config.desktopMode -> GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
            config.viewportMode == com.webtoapp.data.model.ViewportMode.FIT_SCREEN ||
                config.viewportMode == com.webtoapp.data.model.ViewportMode.DESKTOP ->
                GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
            else -> GeckoSessionSettings.VIEWPORT_MODE_MOBILE
        }
        this.lastGeckoViewportMode = geckoViewportMode
        this.lastAllowJavascript = config.javaScriptEnabled

        val sessionSettings = GeckoSessionSettings.Builder()
            .usePrivateMode(false)
            .useTrackingProtection(false)
            .userAgentMode(geckoUaMode)
            .viewportMode(geckoViewportMode)
            .allowJavascript(config.javaScriptEnabled)
            .build()

        val newSession = GeckoSession(sessionSettings)
        setupDelegates(newSession, callback, context, config)

        newSession.open(runtime)
        liveSessions.add(newSession)
        session = newSession

        val view = GeckoView(context)
        // GeckoView renders on an opaque surface that defaults to white until the first
        // frame is composited, which shows as a white flash on startup (#322). Make it
        // transparent so the window/splash background shows through until content paints,
        // matching the System WebView path (which has no opaque backing surface).
        view.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        view.setSession(newSession)
        geckoView = view

        val effectiveUserAgent = when (config.userAgentMode) {
            UserAgentMode.DEFAULT -> null
            UserAgentMode.CUSTOM -> config.customUserAgent?.takeIf { it.isNotBlank() }
            else -> config.userAgentMode.userAgentString
        }
        if (effectiveUserAgent != null) {
            newSession.settings.userAgentOverride = effectiveUserAgent
            lastUserAgentOverride = effectiveUserAgent
            AppLogger.d(TAG, "User-Agent set: ${effectiveUserAgent.take(80)}...")
        }

        return view
    }

    /**
     * Installs a media session delegate on the current session and every
     * future (re)created one; pass null to detach. Callers own the delegate's
     * lifecycle (see GeckoMediaSessionAdapter).
     */
    fun setMediaSessionDelegate(delegate: GeckoMediaSession.Delegate?) {
        mediaSessionDelegate = delegate
        session?.mediaSessionDelegate = delegate
    }

    private fun setupDelegates(
        session: GeckoSession,
        callback: BrowserEngineCallback,
        viewContext: Context,
        config: WebViewConfig
    ) {
        setupContentDelegate(session, callback)
        setupNavigationDelegate(session, callback)
        setupProgressDelegate(session, callback)
        setupPermissionDelegate(session)
        setupPromptDelegate(session, callback, viewContext, config)
        mediaSessionDelegate?.let { session.mediaSessionDelegate = it }
    }

    private fun setupContentDelegate(session: GeckoSession, callback: BrowserEngineCallback) {
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                currentTitle = title
                callback.onTitleChanged(title)
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                if (fullScreen) {
                    callback.onShowCustomView(geckoView, null)
                } else {
                    callback.onHideCustomView()
                }
            }

            override fun onExternalResponse(session: GeckoSession, response: WebResponse) {
                // Parity with the WebView path, where setDownloadListener is only installed
                // when downloadEnabled — a disabled download feature must not silently start
                // serving responses to the system downloader on Gecko.
                if (lastConfig?.downloadEnabled == false) {
                    AppLogger.d(TAG, "Download suppressed (downloadEnabled=false): ${response.uri}")
                    return
                }
                val contentType = response.headers["Content-Type"] ?: "application/octet-stream"
                val contentDisposition = response.headers["Content-Disposition"] ?: ""
                val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: -1L
                val ua = lastUserAgentOverride ?: lastConfig?.let { cfg ->
                    when (cfg.userAgentMode) {
                        UserAgentMode.DEFAULT -> ""
                        UserAgentMode.CUSTOM -> cfg.customUserAgent ?: ""
                        else -> cfg.userAgentMode.userAgentString
                    }
                } ?: ""
                callback.onDownloadStart(
                    response.uri,
                    ua,
                    contentDisposition,
                    contentType,
                    contentLength
                )
            }

            override fun onCrash(session: GeckoSession) {
                AppLogger.e(TAG, "GeckoView session crashed, attempting recovery...")
                callback.onError(-1, "Engine crash — recovering...")
                attemptCrashRecovery()
            }
        }
    }

    private fun setupPromptDelegate(
        session: GeckoSession,
        callback: BrowserEngineCallback,
        viewContext: Context,
        config: WebViewConfig
    ) {
        session.promptDelegate = object : GeckoSession.PromptDelegate {
            /**
             * JS window.alert(). Unhandled Gecko prompts are dismissed silently, so pages
             * using alert() as a user-facing signal (form errors, notices) went mute on
             * Gecko. BasePrompt.confirm() is protected on AlertPrompt; dismiss() is the
             * public completion and carries the same meaning for an alert (no return value).
             */
            override fun onAlertPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.AlertPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                val activity = viewContext.findActivity()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return GeckoResult.fromValue(prompt.dismiss())
                }
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                activity.runOnUiThread {
                    try {
                        android.app.AlertDialog.Builder(activity)
                            .setTitle(prompt.title)
                            .setMessage(prompt.message)
                            .setPositiveButton(com.webtoapp.core.i18n.Strings.confirm) { dialog, _ ->
                                dialog.dismiss()
                                result.complete(prompt.dismiss())
                            }
                            .setOnCancelListener { result.complete(prompt.dismiss()) }
                            .show()
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "onAlertPrompt dialog failed: ${e.message}")
                        result.complete(prompt.dismiss())
                    }
                }
                return result
            }

            /** JS window.confirm(). */
            override fun onButtonPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.ButtonPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                val activity = viewContext.findActivity()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return GeckoResult.fromValue(
                        prompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE)
                    )
                }
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                activity.runOnUiThread {
                    try {
                        android.app.AlertDialog.Builder(activity)
                            .setTitle(prompt.title)
                            .setMessage(prompt.message)
                            .setPositiveButton(com.webtoapp.core.i18n.Strings.confirm) { dialog, _ ->
                                dialog.dismiss()
                                result.complete(prompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.POSITIVE))
                            }
                            .setNegativeButton(com.webtoapp.core.i18n.Strings.btnCancel) { dialog, _ ->
                                dialog.dismiss()
                                result.complete(prompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE))
                            }
                            .setOnCancelListener {
                                result.complete(prompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE))
                            }
                            .show()
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "onButtonPrompt dialog failed: ${e.message}")
                        result.complete(prompt.confirm(GeckoSession.PromptDelegate.ButtonPrompt.Type.NEGATIVE))
                    }
                }
                return result
            }

            /** JS window.prompt() — cancel maps to dismiss() (JS receives null). */
            override fun onTextPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.TextPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                val activity = viewContext.findActivity()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return GeckoResult.fromValue(prompt.dismiss())
                }
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                activity.runOnUiThread {
                    try {
                        val input = android.widget.EditText(activity).apply {
                            setText(prompt.defaultValue ?: "")
                            setSelection(text.length)
                            isSingleLine = true
                        }
                        val container = android.widget.FrameLayout(activity).apply {
                            setPadding(64, 24, 64, 0)
                            addView(input)
                        }
                        android.app.AlertDialog.Builder(activity)
                            .setTitle(prompt.title)
                            .setMessage(prompt.message)
                            .setView(container)
                            .setPositiveButton(com.webtoapp.core.i18n.Strings.confirm) { dialog, _ ->
                                dialog.dismiss()
                                result.complete(prompt.confirm(input.text.toString()))
                            }
                            .setNegativeButton(com.webtoapp.core.i18n.Strings.btnCancel) { dialog, _ ->
                                dialog.dismiss()
                                result.complete(prompt.dismiss())
                            }
                            .setOnCancelListener { result.complete(prompt.dismiss()) }
                            .show()
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "onTextPrompt dialog failed: ${e.message}")
                        result.complete(prompt.dismiss())
                    }
                }
                return result
            }

            /**
             * HTTP Basic/Digest auth and proxy auth — the Gecko counterpart of
             * onReceivedHttpAuthRequest. Mirrors the WebView path's dialog (same strings,
             * same field shape); Gecko has no cached-credential store exposed
             * here, so credentials are requested on every challenge.
             */
            override fun onAuthPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.AuthPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                val activity = viewContext.findActivity()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return GeckoResult.fromValue(prompt.dismiss())
                }
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                val options = prompt.authOptions
                val onlyPassword = (options.flags and
                    GeckoSession.PromptDelegate.AuthPrompt.AuthOptions.Flags.ONLY_PASSWORD) != 0
                activity.runOnUiThread {
                    try {
                        val dialogView = android.widget.LinearLayout(activity).apply {
                            orientation = android.widget.LinearLayout.VERTICAL
                            setPadding(64, 32, 64, 0)

                            if (!onlyPassword) {
                                addView(android.widget.EditText(activity).apply {
                                    tag = "auth_username"
                                    hint = com.webtoapp.core.i18n.Strings.httpAuthUsername
                                    inputType = android.text.InputType.TYPE_CLASS_TEXT
                                    isSingleLine = true
                                    setText(options.username ?: "")
                                })
                                addView(android.view.View(activity).apply {
                                    layoutParams = android.widget.LinearLayout.LayoutParams(
                                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 24
                                    )
                                })
                            }

                            val passwordInput = android.widget.EditText(activity).apply {
                                tag = "auth_password"
                                hint = com.webtoapp.core.i18n.Strings.httpAuthPassword
                                inputType = android.text.InputType.TYPE_CLASS_TEXT or
                                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                                isSingleLine = true
                                setText(options.password ?: "")
                            }
                            addView(passwordInput)

                            addView(android.widget.CheckBox(activity).apply {
                                text = com.webtoapp.core.i18n.Strings.httpAuthShowPassword
                                setOnCheckedChangeListener { _, checked ->
                                    passwordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                                        if (checked) android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                                        else android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                                    passwordInput.setSelection(passwordInput.length())
                                }
                            })
                        }

                        val hostDisplay = runCatching {
                            android.net.Uri.parse(options.uri ?: "").host
                        }.getOrNull() ?: options.uri ?: "server"

                        android.app.AlertDialog.Builder(activity)
                            .setTitle(com.webtoapp.core.i18n.Strings.httpAuthTitle)
                            .setMessage(com.webtoapp.core.i18n.Strings.httpAuthMessage.format(hostDisplay))
                            .setView(dialogView)
                            .setPositiveButton(com.webtoapp.core.i18n.Strings.httpAuthLogin) { dialog, _ ->
                                val username = dialogView.findViewWithTag<android.widget.EditText>("auth_username")
                                    ?.text?.toString() ?: ""
                                val password = dialogView.findViewWithTag<android.widget.EditText>("auth_password")
                                    ?.text?.toString() ?: ""
                                dialog.dismiss()
                                result.complete(
                                    if (onlyPassword) prompt.confirm(password)
                                    else prompt.confirm(username, password)
                                )
                            }
                            .setNegativeButton(com.webtoapp.core.i18n.Strings.btnCancel) { dialog, _ ->
                                dialog.dismiss()
                                result.complete(prompt.dismiss())
                            }
                            .setOnCancelListener { result.complete(prompt.dismiss()) }
                            .show()
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "onAuthPrompt dialog failed: ${e.message}")
                        result.complete(prompt.dismiss())
                    }
                }
                return result
            }

            override fun onRequestCertificate(
                session: GeckoSession,
                request: GeckoSession.PromptDelegate.CertificateRequest
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                if (!config.clientCertificateAuthEnabled) {
                    return GeckoResult.fromValue(request.confirm(null))
                }

                val activity = viewContext.findActivity()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    AppLogger.w(TAG, "Client certificate request ignored: no active Activity")
                    return GeckoResult.fromValue(request.confirm(null))
                }

                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                activity.runOnUiThread {
                    KeyChain.choosePrivateKeyAlias(
                        activity,
                        { alias ->
                            result.complete(request.confirm(alias))
                            if (alias == null) {
                                AppLogger.i(TAG, "Client certificate selection cancelled for ${request.host}")
                            } else {
                                AppLogger.i(TAG, "Client certificate selected for ${request.host}")
                            }
                        },
                        null,
                        request.issuers,
                        request.host,
                        -1,
                        null
                    )
                }
                return result
            }

            override fun onFilePrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.FilePrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                var completed = false
                fun finish(response: GeckoSession.PromptDelegate.PromptResponse) {
                    if (!completed) {
                        completed = true
                        result.complete(response)
                    }
                }

                // Bridge GeckoView's FilePrompt onto the same ValueCallback/FileChooserParams
                // contract the System WebView path uses, so the shell's existing SAF + camera
                // chooser (ShellPermissionDelegate.handleFileChooser) handles the actual pick.
                val valueCallback = android.webkit.ValueCallback<Array<android.net.Uri>> { uris ->
                    when {
                        uris.isNullOrEmpty() -> finish(prompt.dismiss())
                        else -> deliverPickedFiles(prompt, uris) { finish(it) }
                    }
                }

                val params = GeckoFileChooserParams(
                    acceptTypes = prompt.mimeTypes,
                    multiple = prompt.type == GeckoSession.PromptDelegate.FilePrompt.Type.MULTIPLE,
                    captureEnabled = prompt.capture != GeckoSession.PromptDelegate.FilePrompt.Capture.NONE
                )

                val handled = callback.onShowFileChooser(valueCallback, params)
                if (!handled) {
                    finish(prompt.dismiss())
                }
                return result
            }

            /**
             * HTML <select> (single + multiple) and context-menu prompts.
             * GeckoView never renders these itself — an unhandled prompt is
             * silently dismissed, which is why dropdowns appeared dead (#1137).
             * The choice tree (optgroups/submenus, separators, disabled items)
             * flattens into one dialog list with non-clickable header rows.
             */
            override fun onChoicePrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.ChoicePrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                val activity = viewContext.findActivity()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return GeckoResult.fromValue(prompt.dismiss())
                }
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                activity.runOnUiThread {
                    try {
                        val rows = GeckoPromptSupport.flattenChoices(
                            prompt.choices.map { it.toPromptChoice() }
                        )
                        if (prompt.type == GeckoSession.PromptDelegate.ChoicePrompt.Type.MULTIPLE) {
                            showMultiChoiceDialog(activity, prompt, rows, result)
                        } else {
                            showSingleChoiceDialog(activity, prompt, rows, result)
                        }
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "onChoicePrompt dialog failed: ${e.message}")
                        result.complete(prompt.dismiss())
                    }
                }
                return result
            }

            /** <input type=color>. */
            override fun onColorPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.ColorPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                val activity = viewContext.findActivity()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return GeckoResult.fromValue(prompt.dismiss())
                }
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                activity.runOnUiThread {
                    try {
                        showColorPromptDialog(activity, prompt, result)
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "onColorPrompt dialog failed: ${e.message}")
                        result.complete(prompt.dismiss())
                    }
                }
                return result
            }

            /** <input type=date|time|datetime-local|month|week>. */
            override fun onDateTimePrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.DateTimePrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                val activity = viewContext.findActivity()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return GeckoResult.fromValue(prompt.dismiss())
                }
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                activity.runOnUiThread {
                    try {
                        showDateTimePromptDialog(activity, prompt, result)
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "onDateTimePrompt dialog failed: ${e.message}")
                        result.complete(prompt.dismiss())
                    }
                }
                return result
            }

            /** beforeunload — leaving the page may lose unsaved form data. */
            override fun onBeforeUnloadPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.BeforeUnloadPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                val activity = viewContext.findActivity()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return GeckoResult.fromValue(prompt.dismiss())
                }
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                activity.runOnUiThread {
                    try {
                        android.app.AlertDialog.Builder(activity)
                            .setTitle(com.webtoapp.core.i18n.Strings.geckoPromptLeaveTitle)
                            .setMessage(
                                prompt.title?.takeIf { it.isNotBlank() }
                                    ?: com.webtoapp.core.i18n.Strings.geckoPromptLeaveMessage
                            )
                            .setPositiveButton(com.webtoapp.core.i18n.Strings.geckoPromptBtnLeave) { dialog, _ ->
                                dialog.dismiss()
                                result.complete(prompt.confirm(AllowOrDeny.ALLOW))
                            }
                            .setNegativeButton(com.webtoapp.core.i18n.Strings.btnCancel) { dialog, _ ->
                                dialog.dismiss()
                                result.complete(prompt.confirm(AllowOrDeny.DENY))
                            }
                            .setOnCancelListener {
                                result.complete(prompt.confirm(AllowOrDeny.DENY))
                            }
                            .show()
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "onBeforeUnloadPrompt dialog failed: ${e.message}")
                        result.complete(prompt.dismiss())
                    }
                }
                return result
            }

            /** Form resubmission on reload/history navigation. */
            override fun onRepostConfirmPrompt(
                session: GeckoSession,
                prompt: GeckoSession.PromptDelegate.RepostConfirmPrompt
            ): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
                val activity = viewContext.findActivity()
                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    return GeckoResult.fromValue(prompt.dismiss())
                }
                val result = GeckoResult<GeckoSession.PromptDelegate.PromptResponse>()
                activity.runOnUiThread {
                    try {
                        android.app.AlertDialog.Builder(activity)
                            .setTitle(com.webtoapp.core.i18n.Strings.geckoPromptRepostTitle)
                            .setMessage(com.webtoapp.core.i18n.Strings.geckoPromptRepostMessage)
                            .setPositiveButton(com.webtoapp.core.i18n.Strings.confirm) { dialog, _ ->
                                dialog.dismiss()
                                result.complete(prompt.confirm(AllowOrDeny.ALLOW))
                            }
                            .setNegativeButton(com.webtoapp.core.i18n.Strings.btnCancel) { dialog, _ ->
                                dialog.dismiss()
                                result.complete(prompt.confirm(AllowOrDeny.DENY))
                            }
                            .setOnCancelListener {
                                result.complete(prompt.confirm(AllowOrDeny.DENY))
                            }
                            .show()
                    } catch (e: Exception) {
                        AppLogger.w(TAG, "onRepostConfirmPrompt dialog failed: ${e.message}")
                        result.complete(prompt.dismiss())
                    }
                }
                return result
            }
        }
    }

    /** Single-select / menu prompt: one tap confirms immediately. */
    private fun showSingleChoiceDialog(
        activity: Activity,
        prompt: GeckoSession.PromptDelegate.ChoicePrompt,
        rows: List<GeckoPromptSupport.ChoiceRow>,
        result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>
    ) {
        var settled = false
        fun finish(response: GeckoSession.PromptDelegate.PromptResponse) {
            if (!settled) {
                settled = true
                result.complete(response)
            }
        }
        android.app.AlertDialog.Builder(activity)
            .setTitle(choiceDialogTitle(prompt))
            .setAdapter(GeckoChoiceAdapter(activity, rows, multi = false)) { dialog, which ->
                dialog.dismiss()
                val row = rows.getOrNull(which)
                val source = row?.choice?.source
                if (row != null && row.enabled && source is GeckoSession.PromptDelegate.ChoicePrompt.Choice) {
                    finish(prompt.confirm(source))
                } else {
                    finish(prompt.dismiss())
                }
            }
            .setOnCancelListener { finish(prompt.dismiss()) }
            .show()
    }

    /** Multi-select prompt: checked rows are confirmed as a Choice array. */
    private fun showMultiChoiceDialog(
        activity: Activity,
        prompt: GeckoSession.PromptDelegate.ChoicePrompt,
        rows: List<GeckoPromptSupport.ChoiceRow>,
        result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>
    ) {
        var settled = false
        fun finish(response: GeckoSession.PromptDelegate.PromptResponse) {
            if (!settled) {
                settled = true
                result.complete(response)
            }
        }
        val listView = android.widget.ListView(activity).apply {
            choiceMode = android.widget.ListView.CHOICE_MODE_MULTIPLE
            adapter = GeckoChoiceAdapter(activity, rows, multi = true)
        }
        rows.forEachIndexed { i, row ->
            if (row.checked && row.enabled) listView.setItemChecked(i, true)
        }
        android.app.AlertDialog.Builder(activity)
            .setTitle(choiceDialogTitle(prompt))
            .setView(listView)
            .setPositiveButton(com.webtoapp.core.i18n.Strings.confirm) { dialog, _ ->
                val picked = rows.mapIndexedNotNull { i, row ->
                    val source = row.choice?.source
                    if (row.enabled && listView.isItemChecked(i) &&
                        source is GeckoSession.PromptDelegate.ChoicePrompt.Choice
                    ) source else null
                }
                dialog.dismiss()
                finish(prompt.confirm(picked.toTypedArray()))
            }
            .setNegativeButton(com.webtoapp.core.i18n.Strings.btnCancel) { dialog, _ ->
                dialog.dismiss()
                finish(prompt.dismiss())
            }
            .setOnCancelListener { finish(prompt.dismiss()) }
            .show()
    }

    private fun choiceDialogTitle(prompt: GeckoSession.PromptDelegate.ChoicePrompt): CharSequence? =
        prompt.title?.takeIf { it.isNotBlank() }
            ?: prompt.message?.takeIf { it.isNotBlank() }

    /** <input type=color>: optional predefined swatches + validated hex field. */
    private fun showColorPromptDialog(
        activity: Activity,
        prompt: GeckoSession.PromptDelegate.ColorPrompt,
        result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>
    ) {
        var settled = false
        fun finish(response: GeckoSession.PromptDelegate.PromptResponse) {
            if (!settled) {
                settled = true
                result.complete(response)
            }
        }
        val density = activity.resources.displayMetrics.density
        val input = android.widget.EditText(activity).apply {
            hint = "#RRGGBB"
            isSingleLine = true
            setText(GeckoPromptSupport.normalizeHexColor(prompt.defaultValue) ?: "#000000")
            setSelection(text.length)
        }
        val container = android.widget.LinearLayout(activity).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding((24 * density).toInt(), (16 * density).toInt(), (24 * density).toInt(), 0)
        }
        val swatches = (prompt.predefinedValues ?: emptyArray())
            .mapNotNull { GeckoPromptSupport.normalizeHexColor(it) }
            .distinct()
        if (swatches.isNotEmpty()) {
            val row = android.widget.LinearLayout(activity).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
            }
            val size = (36 * density).toInt()
            val margin = (6 * density).toInt()
            swatches.take(8).forEach { hex ->
                row.addView(android.view.View(activity).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(size, size).apply {
                        setMargins(margin, 0, margin, (12 * density).toInt())
                    }
                    setBackgroundColor(android.graphics.Color.parseColor(hex))
                    setOnClickListener { input.setText(hex) }
                })
            }
            container.addView(row)
        }
        container.addView(input)
        val dialog = android.app.AlertDialog.Builder(activity)
            .setTitle(
                prompt.title?.takeIf { it.isNotBlank() }
                    ?: com.webtoapp.core.i18n.Strings.geckoPromptPickColor
            )
            .setView(container)
            .setPositiveButton(com.webtoapp.core.i18n.Strings.confirm, null)
            .setNegativeButton(com.webtoapp.core.i18n.Strings.btnCancel) { d, _ ->
                d.dismiss()
                finish(prompt.dismiss())
            }
            .setOnCancelListener { finish(prompt.dismiss()) }
            .show()
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val hex = GeckoPromptSupport.normalizeHexColor(input.text.toString())
            if (hex == null) {
                input.error = com.webtoapp.core.i18n.Strings.geckoPromptInvalidColor
            } else {
                dialog.dismiss()
                finish(prompt.confirm(hex))
            }
        }
    }

    /**
     * <input type=date|time|datetime-local|month|week>. DATE/TIME use the
     * platform pickers honoring min/max; DATETIME_LOCAL chains date → time;
     * MONTH and WEEK get dual NumberPicker dialogs since Android has no
     * native widget for them. All cancels map to prompt.dismiss().
     */
    private fun showDateTimePromptDialog(
        activity: Activity,
        prompt: GeckoSession.PromptDelegate.DateTimePrompt,
        result: GeckoResult<GeckoSession.PromptDelegate.PromptResponse>
    ) {
        var settled = false
        fun finish(response: GeckoSession.PromptDelegate.PromptResponse) {
            if (!settled) {
                settled = true
                result.complete(response)
            }
        }
        val now = java.util.Calendar.getInstance()
        val nowY = now.get(java.util.Calendar.YEAR)
        val nowM = now.get(java.util.Calendar.MONTH) + 1
        val nowD = now.get(java.util.Calendar.DAY_OF_MONTH)

        fun newDatePicker(
            initial: GeckoPromptSupport.DateTimeParts,
            onPicked: (y: Int, m: Int, d: Int) -> Unit
        ): android.app.DatePickerDialog {
            val dialog = android.app.DatePickerDialog(
                activity,
                { _, y, m, d -> onPicked(y, m + 1, d) },
                initial.y, initial.mo - 1, initial.d
            )
            // Prompt min/max are ISO strings for the whole value; for DATE and
            // DATETIME_LOCAL the date picker can enforce the date part natively.
            GeckoPromptSupport.parseDateValue(prompt.minValue)?.let { (y, m, d) ->
                dialog.datePicker.minDate = java.util.Calendar.getInstance().apply {
                    set(y, m - 1, d, 0, 0, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            GeckoPromptSupport.parseDateValue(prompt.maxValue)?.let { (y, m, d) ->
                dialog.datePicker.maxDate = java.util.Calendar.getInstance().apply {
                    set(y, m - 1, d, 23, 59, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }.timeInMillis
            }
            return dialog
        }

        when (prompt.type) {
            GeckoSession.PromptDelegate.DateTimePrompt.Type.DATE -> {
                val def = GeckoPromptSupport.parseDateValue(prompt.defaultValue)
                    ?: Triple(nowY, nowM, nowD)
                val clamped = GeckoPromptSupport.clampDate(
                    def.first, def.second, def.third, prompt.minValue, prompt.maxValue
                )
                newDatePicker(
                    GeckoPromptSupport.DateTimeParts(clamped.first, clamped.second, clamped.third, 0, 0)
                ) { y, m, d ->
                    val c = GeckoPromptSupport.clampDate(y, m, d, prompt.minValue, prompt.maxValue)
                    finish(prompt.confirm(GeckoPromptSupport.formatDate(c.first, c.second, c.third)))
                }.apply {
                    setOnCancelListener { finish(prompt.dismiss()) }
                }.show()
            }
            GeckoSession.PromptDelegate.DateTimePrompt.Type.TIME -> {
                val def = GeckoPromptSupport.parseTimeValue(prompt.defaultValue)
                    ?: Pair(now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE))
                val clamped = GeckoPromptSupport.clampTime(
                    def.first, def.second, prompt.minValue, prompt.maxValue
                )
                android.app.TimePickerDialog(activity, { _, h, m ->
                    val c = GeckoPromptSupport.clampTime(h, m, prompt.minValue, prompt.maxValue)
                    finish(prompt.confirm(GeckoPromptSupport.formatTime(c.first, c.second)))
                }, clamped.first, clamped.second, true).apply {
                    setOnCancelListener { finish(prompt.dismiss()) }
                }.show()
            }
            GeckoSession.PromptDelegate.DateTimePrompt.Type.DATETIME_LOCAL -> {
                val def = GeckoPromptSupport.parseDateTimeLocal(prompt.defaultValue)
                    ?: GeckoPromptSupport.DateTimeParts(
                        nowY, nowM, nowD,
                        now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE)
                    )
                newDatePicker(def) { y, m, d ->
                    android.app.TimePickerDialog(activity, { _, h, mi ->
                        finish(prompt.confirm(
                            GeckoPromptSupport.formatDateTimeLocal(y, m, d, h, mi)
                        ))
                    }, def.h, def.mi, true).apply {
                        setOnCancelListener { finish(prompt.dismiss()) }
                    }.show()
                }.apply {
                    setOnCancelListener { finish(prompt.dismiss()) }
                }.show()
            }
            GeckoSession.PromptDelegate.DateTimePrompt.Type.MONTH -> {
                val def = GeckoPromptSupport.parseMonthValue(prompt.defaultValue) ?: Pair(nowY, nowM)
                val yearPicker = android.widget.NumberPicker(activity).apply {
                    minValue = 1900
                    maxValue = 2100
                    value = def.first.coerceIn(1900, 2100)
                    wrapSelectorWheel = false
                }
                val monthPicker = android.widget.NumberPicker(activity).apply {
                    minValue = 1
                    maxValue = 12
                    displayedValues = java.text.DateFormatSymbols
                        .getInstance(com.webtoapp.core.i18n.Strings.lang.locale)
                        .months.take(12).toTypedArray()
                    value = def.second.coerceIn(1, 12)
                }
                val layout = android.widget.LinearLayout(activity).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    addView(yearPicker, android.widget.LinearLayout.LayoutParams(
                        0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    addView(monthPicker, android.widget.LinearLayout.LayoutParams(
                        0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                }
                android.app.AlertDialog.Builder(activity)
                    .setTitle(
                        prompt.title?.takeIf { it.isNotBlank() }
                            ?: com.webtoapp.core.i18n.Strings.geckoPromptPickValue
                    )
                    .setView(layout)
                    .setPositiveButton(com.webtoapp.core.i18n.Strings.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish(prompt.confirm(
                            GeckoPromptSupport.formatMonth(yearPicker.value, monthPicker.value)
                        ))
                    }
                    .setNegativeButton(com.webtoapp.core.i18n.Strings.btnCancel) { dialog, _ ->
                        dialog.dismiss()
                        finish(prompt.dismiss())
                    }
                    .setOnCancelListener { finish(prompt.dismiss()) }
                    .show()
            }
            GeckoSession.PromptDelegate.DateTimePrompt.Type.WEEK -> {
                val def = GeckoPromptSupport.parseWeekValue(prompt.defaultValue) ?: Pair(
                    nowY, now.get(java.util.Calendar.WEEK_OF_YEAR)
                )
                val yearPicker = android.widget.NumberPicker(activity).apply {
                    minValue = 1900
                    maxValue = 2100
                    value = def.first.coerceIn(1900, 2100)
                    wrapSelectorWheel = false
                }
                val weekPicker = android.widget.NumberPicker(activity).apply {
                    minValue = 1
                    maxValue = 53
                    displayedValues = Array(53) { "W%02d".format(it + 1) }
                    value = def.second.coerceIn(1, 53)
                }
                val layout = android.widget.LinearLayout(activity).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    addView(yearPicker, android.widget.LinearLayout.LayoutParams(
                        0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    addView(weekPicker, android.widget.LinearLayout.LayoutParams(
                        0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                }
                android.app.AlertDialog.Builder(activity)
                    .setTitle(
                        prompt.title?.takeIf { it.isNotBlank() }
                            ?: com.webtoapp.core.i18n.Strings.geckoPromptPickValue
                    )
                    .setView(layout)
                    .setPositiveButton(com.webtoapp.core.i18n.Strings.confirm) { dialog, _ ->
                        dialog.dismiss()
                        finish(prompt.confirm(
                            GeckoPromptSupport.formatWeek(yearPicker.value, weekPicker.value)
                        ))
                    }
                    .setNegativeButton(com.webtoapp.core.i18n.Strings.btnCancel) { dialog, _ ->
                        dialog.dismiss()
                        finish(prompt.dismiss())
                    }
                    .setOnCancelListener { finish(prompt.dismiss()) }
                    .show()
            }
            else -> finish(prompt.dismiss())
        }
    }

    /**
     * FilePrompt.confirm() resolves Uris to local path strings and silently uploads nothing
     * when the provider refuses to expose one (#638: galleries, cloud, vendor pickers), so
     * content:// picks are first copied into a cache dir by GeckoUploadMaterializer (same
     * approach as Firefox's android-components). The copy runs on IO; confirm() is annotated
     * @UiThread and resumes on main. A failed materialization still confirms with the original
     * Uris — upstream resolution may succeed where the copy could not.
     */
    private fun deliverPickedFiles(
        prompt: GeckoSession.PromptDelegate.FilePrompt,
        uris: Array<android.net.Uri>,
        finish: (GeckoSession.PromptDelegate.PromptResponse) -> Unit
    ) {
        val scope = filePromptScope ?: kotlinx.coroutines.MainScope().also { filePromptScope = it }
        scope.launch {
            val prepared = runCatching {
                withContext(Dispatchers.IO) {
                    GeckoUploadMaterializer.prepare(
                        uris,
                        java.io.File(context.cacheDir, UPLOAD_CACHE_DIR),
                        context.contentResolver
                    )
                }
            }.onFailure { AppLogger.w(TAG, "File upload materialization failed: ${it.message}") }
                .getOrDefault(uris)
            try {
                if (prompt.type == GeckoSession.PromptDelegate.FilePrompt.Type.MULTIPLE) {
                    finish(prompt.confirm(context, prepared))
                } else {
                    finish(prompt.confirm(context, prepared[0]))
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "FilePrompt confirm failed: ${e.message}")
                // Free the page's input state instead of leaving the upload pending forever.
                try {
                    finish(prompt.dismiss())
                } catch (_: Exception) { }
            }
        }
    }

    private fun Context.findActivity(): Activity? {
        var current: Context = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            val base = current.baseContext
            if (base === current) break
            current = base
        }
        return current as? Activity
    }

    private fun setupNavigationDelegate(session: GeckoSession, callback: BrowserEngineCallback) {
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                currentUrl = url
                // Gecko traffic bypasses WebViewClient.shouldInterceptRequest, so the TLS
                // MITM bridge's host allowlist is fed here instead.
                if (com.webtoapp.core.webview.TlsMitmBridge.isRunning()) {
                    com.webtoapp.core.webview.TlsMitmBridge.allowHost(
                        runCatching { android.net.Uri.parse(url ?: "").host }.getOrNull()
                    )
                }
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                canGoBackFlag = canGoBack
                callback.onNavigationStateChanged(canGoBackFlag, canGoForwardFlag)
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                canGoForwardFlag = canGoForward
                callback.onNavigationStateChanged(canGoBackFlag, canGoForwardFlag)
            }

            /**
             * Main-frame load failure — the Gecko counterpart of WebViewClient.onReceivedError /
             * onReceivedSslError. Without it the shell/host error UI (error card, failover,
             * retry affordances) never fires on Gecko and a failed load leaves a blank page.
             */
            override fun onLoadError(
                session: GeckoSession,
                uri: String?,
                error: WebRequestError
            ): GeckoResult<String>? {
                val description = describeWebRequestError(error)
                AppLogger.w(TAG, "Main-frame load error: uri=$uri category=${error.category} code=${error.code}")
                if (error.category == WebRequestError.ERROR_CATEGORY_SECURITY) {
                    callback.onSslError(description)
                } else {
                    callback.onError(error.code, description)
                }
                // Resolve with null so GeckoView keeps its built-in error page beneath the
                // host's own error UI, matching the default delegate behavior.
                return GeckoResult.fromValue(null)
            }

            override fun onLoadRequest(
                session: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest
            ): GeckoResult<AllowOrDeny>? {
                val uri = request.uri

                if (uri.startsWith("tel:") || uri.startsWith("mailto:") || uri.startsWith("intent:")) {
                    callback.onExternalLink(uri)
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }

                val cfg = lastConfig
                if (cfg != null && cfg.openExternalLinks) {
                    val scheme = runCatching { android.net.Uri.parse(uri).scheme?.lowercase() }.getOrNull()
                    if (scheme == "http" || scheme == "https") {
                        val targetHost = runCatching { android.net.Uri.parse(uri).host?.lowercase() }.getOrNull()
                        val currentHost = runCatching { currentUrl?.let { android.net.Uri.parse(it).host?.lowercase() } }.getOrNull()
                        // Loopback-to-loopback navigations are the app's own local content,
                        // not external links (e.g. 127.0.0.1 <-> localhost in a local HTML app).
                        val bothLoopback = targetHost != null && currentHost != null &&
                            isLoopbackHost(targetHost) && isLoopbackHost(currentHost)
                        if (!bothLoopback &&
                            targetHost != null && currentHost != null &&
                            targetHost != currentHost &&
                            !targetHost.endsWith(".$currentHost") &&
                            !currentHost.endsWith(".$targetHost")) {
                            callback.onExternalLink(uri)
                            return GeckoResult.fromValue(AllowOrDeny.DENY)
                        }
                    }
                }

                return GeckoResult.fromValue(AllowOrDeny.ALLOW)
            }

            override fun onNewSession(
                session: GeckoSession,
                uri: String
            ): GeckoResult<GeckoSession>? {
                loadUrl(uri)
                return null
            }
        }
    }

    private fun isLoopbackHost(host: String): Boolean {
        val h = host.lowercase()
        return h == "127.0.0.1" || h == "localhost" || h == "[::1]" || h == "::1"
    }

    /**
     * Maps Gecko's WebRequestError onto the `net::ERR_*` tokens the WebView path produces, so
     * the shell/host error UI shows the same vocabulary on both engines.
     */
    private fun describeWebRequestError(error: WebRequestError): String = when (error.code) {
        WebRequestError.ERROR_UNKNOWN_HOST -> "net::ERR_NAME_NOT_RESOLVED"
        WebRequestError.ERROR_CONNECTION_REFUSED -> "net::ERR_CONNECTION_REFUSED"
        WebRequestError.ERROR_NET_TIMEOUT -> "net::ERR_TIMED_OUT"
        WebRequestError.ERROR_NET_INTERRUPT -> "net::ERR_CONNECTION_ABORTED"
        WebRequestError.ERROR_NET_RESET -> "net::ERR_CONNECTION_RESET"
        WebRequestError.ERROR_OFFLINE -> "net::ERR_INTERNET_DISCONNECTED"
        WebRequestError.ERROR_PORT_BLOCKED -> "net::ERR_UNSAFE_PORT"
        WebRequestError.ERROR_REDIRECT_LOOP -> "net::ERR_TOO_MANY_REDIRECTS"
        WebRequestError.ERROR_UNKNOWN_PROTOCOL -> "net::ERR_UNKNOWN_URL_SCHEME"
        WebRequestError.ERROR_MALFORMED_URI -> "net::ERR_INVALID_URL"
        WebRequestError.ERROR_FILE_NOT_FOUND -> "net::ERR_FILE_NOT_FOUND"
        WebRequestError.ERROR_FILE_ACCESS_DENIED -> "net::ERR_ACCESS_DENIED"
        WebRequestError.ERROR_SECURITY_SSL -> "net::ERR_SSL_PROTOCOL_ERROR"
        WebRequestError.ERROR_SECURITY_BAD_CERT -> "net::ERR_CERT_AUTHORITY_INVALID"
        WebRequestError.ERROR_BAD_HSTS_CERT -> "net::ERR_CERT_AUTHORITY_INVALID"
        WebRequestError.ERROR_PROXY_CONNECTION_REFUSED -> "net::ERR_PROXY_CONNECTION_FAILED"
        WebRequestError.ERROR_UNKNOWN_PROXY_HOST -> "net::ERR_PROXY_NAME_NOT_RESOLVED"
        else -> error.message ?: "net::ERR_FAILED"
    }

    private fun setupProgressDelegate(session: GeckoSession, callback: BrowserEngineCallback) {
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {

                callback.onPageStarted(url)

            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                callback.onPageFinished(currentUrl)
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                callback.onProgressChanged(progress)
            }

            override fun onSecurityChange(
                session: GeckoSession,
                securityInfo: GeckoSession.ProgressDelegate.SecurityInformation
            ) {

            }
        }
    }

    private fun setupPermissionDelegate(session: GeckoSession) {
        session.permissionDelegate = object : GeckoSession.PermissionDelegate {
            override fun onContentPermissionRequest(
                session: GeckoSession,
                perm: GeckoSession.PermissionDelegate.ContentPermission
            ): GeckoResult<Int>? {
                val cfg = lastConfig
                if (perm.permission == GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION) {
                    if (cfg == null || !cfg.geolocationEnabled) {
                        return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY)
                    }
                    val policy = cfg.geolocationPolicy.name
                    when (policy) {
                        "DENY_ALL" -> {
                            return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY)
                        }
                        "REMEMBER_PER_HOST" -> {
                            val allowed = com.webtoapp.ui.shell.GeolocationPermissionsSingleton.getAllowedOrigins()
                            if (perm.uri != null && allowed.contains(perm.uri)) {
                                return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW)
                            }
                        }
                    }
                    return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW)
                }
                return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW)
            }

            override fun onMediaPermissionRequest(
                session: GeckoSession,
                uri: String,
                video: Array<GeckoSession.PermissionDelegate.MediaSource>?,
                audio: Array<GeckoSession.PermissionDelegate.MediaSource>?,
                callback: GeckoSession.PermissionDelegate.MediaCallback
            ) {
                callback.grant(video?.firstOrNull(), audio?.firstOrNull())
            }

            override fun onAndroidPermissionsRequest(
                session: GeckoSession,
                permissions: Array<out String>?,
                callback: GeckoSession.PermissionDelegate.Callback
            ) {
                // Actually request the Android runtime permissions through the host Activity
                // instead of auto-granting (#344). Auto-granting told GeckoView the permission was
                // available while the OS-level permission was never obtained, so geolocation and
                // camera/mic silently failed.
                val perms = permissions?.filter { it.isNotBlank() }?.toTypedArray() ?: emptyArray()
                val host = this@GeckoViewEngine.callback
                if (perms.isEmpty() || host == null) {
                    callback.grant()
                    return
                }
                host.onAndroidPermissionsRequest(perms) { granted ->
                    if (granted) callback.grant() else callback.reject()
                }
            }
        }
    }

    private fun attemptCrashRecovery() {
        val view = geckoView ?: return
        val cb = callback ?: return
        val urlToRestore = currentUrl

        try {

            session?.let { old ->
                try { old.close() } catch (_: Exception) { }
                liveSessions.remove(old)
            }
            session = null

            val runtime = getRuntime(context)

            val sessionSettings = GeckoSessionSettings.Builder()
                .usePrivateMode(false)
                .useTrackingProtection(false)
                .userAgentMode(lastGeckoUaMode)
                .viewportMode(lastGeckoViewportMode)
                .allowJavascript(lastAllowJavascript)
                .build()

            val newSession = GeckoSession(sessionSettings)
            setupDelegates(
                session = newSession,
                callback = cb,
                viewContext = view.context,
                config = lastConfig ?: WebViewConfig()
            )
            newSession.open(runtime)
            liveSessions.add(newSession)

            lastUserAgentOverride?.let {
                newSession.settings.userAgentOverride = it
            }

            view.setSession(newSession)
            session = newSession

            if (!urlToRestore.isNullOrBlank() && urlToRestore != "about:blank") {
                newSession.loadUri(urlToRestore)
                AppLogger.i(TAG, "Crash recovery successful, restoring URL: $urlToRestore")
            } else {
                AppLogger.i(TAG, "Crash recovery successful (no URL to restore)")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Crash recovery failed", e)
            cb.onError(-2, "Engine crash recovery failed: ${e.message}")
        }
    }

    override fun loadUrl(url: String) {
        session?.loadUri(url)
    }

    override fun evaluateJavascript(script: String, resultCallback: ((String?) -> Unit)?) {
        val s = session
        if (s == null) {
            resultCallback?.invoke(null)
            return
        }

        try {
            val encoded = android.util.Base64.encodeToString(
                script.toByteArray(Charsets.UTF_8),
                android.util.Base64.NO_WRAP
            )
            val wrappedScript = "javascript:void(eval(atob('$encoded')))"
            s.loadUri(wrappedScript)
        } catch (e: Exception) {
            AppLogger.e(TAG, "evaluateJavascript encoding failed", e)

            try {
                val escaped = script
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                s.loadUri("javascript:void(eval('$escaped'))")
            } catch (ex: Exception) {
                AppLogger.e(TAG, "evaluateJavascript fallback also failed", ex)
            }
        }

        resultCallback?.invoke(null)
    }

    override fun canGoBack(): Boolean = canGoBackFlag
    override fun goBack() { session?.goBack() }
    override fun canGoForward(): Boolean = canGoForwardFlag
    override fun goForward() { session?.goForward() }
    override fun reload() { session?.reload() }
    override fun stopLoading() { session?.stop() }
    override fun getCurrentUrl(): String? = currentUrl
    override fun getTitle(): String? = currentTitle
    override fun getView(): View? = geckoView

    override fun destroy() {
        session?.let { s ->
            try {
                s.close()
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error closing session", e)
            }
            liveSessions.remove(s)
        }
        session = null
        geckoView = null
        callback = null
        lastConfig = null
        filePromptScope?.cancel()
        filePromptScope = null
        runCatching { GeckoUploadMaterializer.purgeAll(java.io.File(context.cacheDir, UPLOAD_CACHE_DIR)) }
            .onFailure { AppLogger.w(TAG, "Upload cache purge failed: ${it.message}") }
    }

    override fun clearCache(includeDiskFiles: Boolean) {
        try {
            val runtime = sharedRuntime ?: return
            runtime.storageController.clearData(StorageController.ClearFlags.ALL)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error clearing cache", e)
        }
    }

    override fun clearHistory() {
        try {
            session?.purgeHistory()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error clearing history", e)
        }
    }

    /**
     * Find-in-page backed by Gecko's native finder (SessionFinder). The old claim that
     * "findAllAsync has no GeckoView equivalent" was simply wrong — session.finder offers
     * find/findNext/clear with match counts, so the native find bar works on both engines.
     *
     * @param forward null starts a fresh search for [text]; true/false steps to the
     * next/previous match of the current search.
     */
    fun findInPage(
        text: String,
        forward: Boolean?,
        onResult: (activeMatch: Int, totalMatches: Int) -> Unit
    ) {
        val s = session
        if (s == null || text.isEmpty()) {
            onResult(-1, 0)
            return
        }
        try {
            val finder = s.finder
            finder.displayFlags = GeckoSession.FINDER_DISPLAY_HIGHLIGHT_ALL
            val flags = if (forward == false) GeckoSession.FINDER_FIND_BACKWARDS else 0
            if (forward == null) finder.clear()
            finder.find(text, flags).accept({ result ->
                val total = result?.total ?: 0
                onResult(if (total > 0) result?.current ?: 0 else -1, total)
            }, { e ->
                AppLogger.w(TAG, "findInPage failed: ${e?.message}")
                onResult(-1, 0)
            })
        } catch (e: Exception) {
            AppLogger.w(TAG, "findInPage failed: ${e.message}")
            onResult(-1, 0)
        }
    }

    fun clearFindMatches() {
        try {
            session?.finder?.clear()
        } catch (_: Exception) {
        }
    }

}

/**
 * Adapts a GeckoView `FilePrompt` onto the `WebChromeClient.FileChooserParams` contract so the
 * shell's existing System-WebView file chooser logic (SAF picker + camera capture) can serve
 * GeckoView uploads unchanged. Only the accessors the chooser reads are meaningful; the rest
 * return inert defaults.
 */
private class GeckoFileChooserParams(
    private val acceptTypes: Array<String>?,
    private val multiple: Boolean,
    private val captureEnabled: Boolean
) : android.webkit.WebChromeClient.FileChooserParams() {

    override fun getMode(): Int =
        if (multiple) MODE_OPEN_MULTIPLE else MODE_OPEN

    override fun getAcceptTypes(): Array<String> =
        acceptTypes?.takeIf { it.isNotEmpty() } ?: arrayOf("*/*")

    override fun isCaptureEnabled(): Boolean = captureEnabled

    override fun getTitle(): CharSequence = ""

    override fun getFilenameHint(): String? = null

    override fun createIntent(): android.content.Intent =
        android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
            addCategory(android.content.Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
}

/** Maps a Gecko Choice tree onto the neutral [GeckoPromptSupport.PromptChoice] model. */
private fun GeckoSession.PromptDelegate.ChoicePrompt.Choice.toPromptChoice():
    GeckoPromptSupport.PromptChoice =
    GeckoPromptSupport.PromptChoice(
        label = label ?: "",
        selected = selected,
        disabled = disabled,
        separator = separator,
        children = items?.map { it.toPromptChoice() },
        source = this
    )

/**
 * List adapter for ChoicePrompt dialogs. Group headers, separators and
 * disabled items report isEnabled=false so the ListView never fires a click
 * or check-toggle for them; depth indents optgroup children and submenus.
 */
private class GeckoChoiceAdapter(
    context: Context,
    private val rows: List<GeckoPromptSupport.ChoiceRow>,
    private val multi: Boolean
) : android.widget.BaseAdapter() {

    private val inflater = android.view.LayoutInflater.from(context)
    private val density = context.resources.displayMetrics.density

    override fun getCount(): Int = rows.size
    override fun getItem(position: Int): Any = rows[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun areAllItemsEnabled(): Boolean = false
    override fun isEnabled(position: Int): Boolean = rows[position].enabled

    override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
        val row = rows[position]
        val layout = if (multi) android.R.layout.simple_list_item_multiple_choice
            else android.R.layout.simple_list_item_1
        val tv = (convertView as? android.widget.TextView)
            ?: inflater.inflate(layout, parent, false) as android.widget.TextView
        val header = row.choice?.children?.isNotEmpty() == true
        tv.text = if (row.choice?.separator == true && row.label.isBlank()) {
            "────────────"
        } else {
            row.label
        }
        tv.setTypeface(null, if (header) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        tv.alpha = if (row.enabled) 1f else 0.45f
        val indent = (16 * density).toInt() + (row.depth * 20 * density).toInt()
        tv.setPadding(indent, (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
        return tv
    }
}
