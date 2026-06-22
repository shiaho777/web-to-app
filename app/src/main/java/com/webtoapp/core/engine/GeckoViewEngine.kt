package com.webtoapp.core.engine

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.webview.OAuthCompatEngine
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
import org.mozilla.geckoview.WebResponse
import org.mozilla.geckoview.WebExtension

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

        @Volatile
        private var sharedRuntime: GeckoRuntime? = null

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
                val ext = WebExtension(url)
                ext.setMessageDelegate(object : WebExtension.MessageDelegate {
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
                val result = runtime.registerWebExtension(ext)
                result.then({
                    AppLogger.d(TAG, "Native bridge WebExtension registered")
                    null
                }, { throwable ->
                    AppLogger.e(TAG, "Failed to register native bridge WebExtension", throwable)
                    null
                })
                nativeBridgeExtension = ext
                return ext
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
            return "ech=$ech|proxy=$proxyKey"
        }

        fun getRuntime(context: Context): GeckoRuntime {
            return sharedRuntime ?: synchronized(this) {
                sharedRuntime ?: createRuntime(context.applicationContext).also {
                    sharedRuntime = it
                    runtimeConfigFingerprint = currentConfigFingerprint()
                }
            }
        }

        /**
         * ECH / 代理等 prefs 经 configFilePath 指定的 yaml 在 GeckoRuntime 创建时读取,
         * 运行时无法热更新。若当前进程里已存在的 runtime 与本次所需的 createRuntime-time
         * 配置(ECH + 代理)指纹不一致(典型:runtime 先于 DoH/ECH/代理 配置创建,带不上
         * 对应 prefs),就销毁并按正确参数重建,确保 SNI 加密与代理真正生效。
         */
        fun ensureRuntimeForConfig(context: Context) {
            val want = currentConfigFingerprint()
            synchronized(this) {
                val existing = sharedRuntime
                if (existing == null) {
                    getRuntime(context)
                    return
                }
                if (want != runtimeConfigFingerprint) {
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
                }
            }
        }

        @Volatile
        private var currentDnsConfig: com.webtoapp.data.model.DnsConfig? = null

        @Volatile
        private var currentProxyConfig: ProxyConfig? = null

        fun applyDnsConfig(config: com.webtoapp.data.model.DnsConfig) {
            currentDnsConfig = config
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

        /**
         * ECH（Encrypted Client Hello）等 network.dns.* 属于 Gecko preference，
         * 不是 Gecko 进程命令行参数。GeckoRuntimeSettings.arguments(...) 设置的是
         * 进程 args（--pref=foo=bar 不会被解析为偏好，会被静默丢弃），因此旧实现的
         * ECH 从未真正生效（用户实测 sni=plaintext 即此因）。
         *
         * Gecko 官方设 preference 的唯一可在 release 包用的途径，是写一个
         * geckoview-config.yaml 的 prefs: map，并用 configFilePath(...) 强制 runtime
         * 从该文件读取（默认只有 debuggable / adb debug-app 才读，release 包不读）。
         * 该文件只能在 runtime 创建时读取，故仍需配合 ensureRuntimeForConfig 重建。
         *
         * ECHConfig 公钥存放在 DNS 的 HTTPS(HTTPS RR / SVCB)记录里。Gecko 要拿到它、
         * 进而加密 SNI，光开 echconfig.enabled 不够，还必须开启 HTTPS RR 的解析与使用：
         *   - network.dns.echconfig.enabled        : 启用 ECH 本身
         *   - network.dns.http3_echconfig.enabled  : HTTP/3 上的 ECH
         *   - network.dns.upgrade_with_https_rr    : 解析并使用 HTTPS RR
         *   - network.dns.use_https_rr_as_altsvc   : 把 HTTPS RR 当 alt-svc（ECHConfig 经此通道取得）
         *
         * 验证:用 GeckoView 引擎打开 https://cloudflare.com/cdn-cgi/trace,看 sni=encrypted。
         * 系统 WebView(Chromium)不暴露 ECH 开关,无法由 app 控制,sni 必为 plaintext。
         */
        private fun writeGeckoConfigFile(context: Context, config: com.webtoapp.data.model.DnsConfig?): String {
            val configFile = java.io.File(context.filesDir, GECKO_CONFIG_FILE)
            val prefs = LinkedHashMap<String, Any>()
            if (config?.echEffective == true) {
                prefs["network.dns.echconfig.enabled"] = true
                prefs["network.dns.http3_echconfig.enabled"] = true
                prefs["network.dns.upgrade_with_https_rr"] = true
                prefs["network.dns.use_https_rr_as_altsvc"] = true
            }

            currentProxyConfig?.let { prefs.putAll(buildProxyPrefs(it)) }

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
                configFile.absolutePath
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to write Gecko config file", e)
                ""
            }
        }

        private fun yamlValue(value: Any): String = when (value) {
            is Boolean, is Int, is Long -> value.toString()
            else -> "\"" + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        }

        private fun createRuntime(context: Context): GeckoRuntime {
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

            return GeckoRuntime.create(context, settingsBuilder.build())
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

    private var geckoView: GeckoView? = null
    private var session: GeckoSession? = null
    private var callback: BrowserEngineCallback? = null
    private var currentUrl: String? = null
    private var currentTitle: String? = null
    private var canGoBackFlag = false
    private var canGoForwardFlag = false

    private var lastConfig: WebViewConfig? = null
    private var lastGeckoUaMode: Int = GeckoSessionSettings.USER_AGENT_MODE_MOBILE
    private var lastUserAgentOverride: String? = null

    private var bridgeScope: kotlinx.coroutines.CoroutineScope? = null

    override fun createView(
        context: Context,
        config: WebViewConfig,
        callback: BrowserEngineCallback
    ): View {
        this.callback = callback
        this.lastConfig = config

        if (config.dnsMode != "SYSTEM") {
            applyDnsConfig(config.dnsConfig)
        }
        ensureRuntimeForConfig(context)

        val runtime = getRuntime(context)

        if (config.enableCorsBypass || config.enablePrivateNetworkBridge) {
            if (bridgeScope == null) bridgeScope = kotlinx.coroutines.MainScope()
            val bridge = com.webtoapp.core.webview.NativeBridge(
                context = context.applicationContext,
                scope = bridgeScope!!,
                webViewProvider = { null },
                capabilities = config.nativeBridgeCapabilities,
                corsBypass = config.enableCorsBypass
            )
            activeNativeBridge = bridge
            ensureNativeBridgeExtension(runtime)
        }

        val geckoUaMode = when (config.userAgentMode) {
            UserAgentMode.CHROME_DESKTOP, UserAgentMode.SAFARI_DESKTOP,
            UserAgentMode.FIREFOX_DESKTOP, UserAgentMode.EDGE_DESKTOP -> GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
            else -> GeckoSessionSettings.USER_AGENT_MODE_MOBILE
        }
        this.lastGeckoUaMode = geckoUaMode

        val sessionSettings = GeckoSessionSettings.Builder()
            .usePrivateMode(false)
            .useTrackingProtection(false)
            .userAgentMode(geckoUaMode)
            .build()

        val newSession = GeckoSession(sessionSettings)
        setupDelegates(newSession, callback)

        newSession.open(runtime)
        session = newSession

        val view = GeckoView(context)
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

    private fun setupDelegates(session: GeckoSession, callback: BrowserEngineCallback) {
        setupContentDelegate(session, callback)
        setupNavigationDelegate(session, callback)
        setupProgressDelegate(session, callback)
        setupPermissionDelegate(session)
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
                val contentType = response.headers["Content-Type"] ?: "application/octet-stream"
                val contentDisposition = response.headers["Content-Disposition"] ?: ""
                val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: -1L
                callback.onDownloadStart(
                    response.uri,
                    "",
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

    private fun setupNavigationDelegate(session: GeckoSession, callback: BrowserEngineCallback) {
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                currentUrl = url
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                canGoBackFlag = canGoBack
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                canGoForwardFlag = canGoForward
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
                callback.grant()
            }
        }
    }

    private fun attemptCrashRecovery() {
        val view = geckoView ?: return
        val cb = callback ?: return
        val urlToRestore = currentUrl

        try {

            try { session?.close() } catch (_: Exception) { }
            session = null

            val runtime = getRuntime(context)

            val sessionSettings = GeckoSessionSettings.Builder()
                .usePrivateMode(false)
                .useTrackingProtection(false)
                .userAgentMode(lastGeckoUaMode)
                .build()

            val newSession = GeckoSession(sessionSettings)
            setupDelegates(newSession, cb)
            newSession.open(runtime)

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
        try {
            session?.close()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error closing session", e)
        }
        session = null
        geckoView = null
        callback = null
        lastConfig = null
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

}
