package com.webtoapp.core.webview

import android.content.Context
import android.webkit.WebView
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.webtoapp.core.logging.AppLogger
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class PacProxyManager(private val context: Context) {

    companion object {
        private const val TAG = "PacProxyManager"

        fun isProxyOverrideSupported(): Boolean {
            return try {
                WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to check PROXY_OVERRIDE support: ${e.message}")
                false
            }
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var cachedPacScript: String? = null
    @Volatile
    private var cachedPacUrl: String? = null

    @Volatile
    private var currentProxyUsername: String = ""
    @Volatile
    private var currentProxyPassword: String = ""

    suspend fun applyProxy(
        proxyMode: String,
        staticProxyHost: String = "",
        staticProxyPort: Int = 0,
        staticProxyType: String = "HTTP",
        pacUrl: String = "",
        bypassRules: List<String> = emptyList(),
        username: String = "",
        password: String = ""
    ) {
        val bypass = (bypassRules + com.webtoapp.core.webview.CloudflareCompat.challengeProxyBypassRules())
            .filter { it.isNotBlank() }
            .distinct()
        if (!isProxyOverrideSupported()) {
            // Pre-67 WebView: ProxyController is a no-op. Arm the in-app fetch
            // fallback (same settings) instead of silently doing nothing.
            AppLogger.w(TAG, "PROXY_OVERRIDE not supported, arming legacy fetch fallback")
            applyLegacyFallback(proxyMode, staticProxyHost, staticProxyPort, staticProxyType, pacUrl, bypass, username, password)
            return
        }
        LegacyProxyFetchBridge.clear()

        currentProxyUsername = username
        currentProxyPassword = password

        if (username.isNotBlank() && password.isNotBlank()) {
            setupProxyAuthenticator(username, password)
        } else {
            clearProxyAuthenticator()
        }

        when (proxyMode.uppercase()) {
            "NONE" -> clearProxy()
            "STATIC" -> applyStaticProxy(staticProxyHost, staticProxyPort, staticProxyType, bypass, username, password)
            "PAC" -> applyPacProxy(pacUrl, bypass)
            else -> {
                AppLogger.w(TAG, "Unknown proxy mode: $proxyMode, clearing proxy")
                clearProxy()
            }
        }
    }

    fun clearProxy() {
        LegacyProxyFetchBridge.clear()
        if (!isProxyOverrideSupported()) return

        try {
            ProxyController.getInstance().clearProxyOverride(
                { command -> command.run() },
                { AppLogger.d(TAG, "Proxy cleared successfully") }
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to clear proxy", e)
        }

        currentProxyUsername = ""
        currentProxyPassword = ""
        clearProxyAuthenticator()

        LocalHttpToSocksBridge.stop()
        LocalHttpHostMappingBridge.stop()
        TlsMitmBridge.stop()
    }

    private fun setupProxyAuthenticator(username: String, password: String) {
        try {
            java.net.Authenticator.setDefault(object : java.net.Authenticator() {
                override fun getPasswordAuthentication(): java.net.PasswordAuthentication? {
                    if (requestorType == RequestorType.PROXY) {
                        return java.net.PasswordAuthentication(username, password.toCharArray())
                    }
                    return null
                }
            })
            AppLogger.d(TAG, "Proxy authenticator set for username=$username")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to set proxy authenticator", e)
        }
    }

    private fun clearProxyAuthenticator() {
        try {
            java.net.Authenticator.setDefault(null)
        } catch (_: Exception) {

        }
    }

    private fun applyStaticProxy(
        host: String,
        port: Int,
        type: String,
        bypassRules: List<String>,
        username: String = "",
        password: String = ""
    ) {
        if (host.isBlank() || port <= 0) {
            AppLogger.w(TAG, "Invalid static proxy config: host=$host, port=$port")
            return
        }

        try {
            val upperType = type.uppercase()
            val isSocks = upperType == "SOCKS5" || upperType == "SOCKS"

            val proxyRule = if (isSocks) {
                val bridgePort = LocalHttpToSocksBridge.start(
                    LocalHttpToSocksBridge.Upstream(host, port, username, password)
                )
                if (bridgePort <= 0) {
                    TranslateProxyRoute.clear()
                    AppLogger.e(TAG, "Local SOCKS5 bridge failed to start; aborting proxy apply")
                    return
                }
                // The bridge speaks HTTP CONNECT. Translation requests use the same hop
                // because HttpURLConnection does not see ProxyController.
                TranslateProxyRoute.useHttp("127.0.0.1", bridgePort)
                AppLogger.i(
                    TAG,
                    "WebView SOCKS5 routed via local bridge 127.0.0.1:$bridgePort -> $host:$port"
                )
                "127.0.0.1:$bridgePort"
            } else {

                LocalHttpToSocksBridge.stop()
                when (upperType) {
                    "HTTPS" -> {
                        // java.net.Proxy cannot speak an HTTPS upstream proxy.
                        TranslateProxyRoute.clear()
                        "https://$host:$port"
                    }
                    else -> {
                        TranslateProxyRoute.useHttp(host, port)
                        "$host:$port"
                    }
                }
            }

            val builder = ProxyConfig.Builder()
                .addProxyRule(proxyRule)

            builder.addBypassRule("localhost")
            builder.addBypassRule("127.0.0.1")
            builder.addBypassRule("[::1]")
            builder.addBypassRule("10.0.2.2")

            bypassRules.filter { it.isNotBlank() }.forEach { rule ->
                builder.addBypassRule(rule)
            }

            ProxyController.getInstance().setProxyOverride(
                builder.build(),
                { command -> command.run() },
                { AppLogger.d(TAG, "Static proxy applied: $proxyRule") }
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to apply static proxy", e)
        }
    }

    private suspend fun applyPacProxy(pacUrl: String, bypassRules: List<String>) {
        if (pacUrl.isBlank()) {
            AppLogger.w(TAG, "Empty PAC URL")
            return
        }

        try {

            val pacScript = downloadPacScript(pacUrl)
            if (pacScript.isNullOrBlank()) {
                AppLogger.e(TAG, "Failed to download PAC script from: $pacUrl")
                return
            }

            cachedPacScript = pacScript
            cachedPacUrl = pacUrl

            val pacResult = PacScriptEvaluator.evaluate(pacScript, bypassRules)

            if (pacResult.proxies.isEmpty()) {
                AppLogger.w(TAG, "PAC script resolves to DIRECT for all sampled hosts, clearing proxy")
                clearProxy()
                return
            }

            publishTranslationRoute(pacResult.proxies)

            val builder = ProxyConfig.Builder()
            pacResult.proxies.forEach { entry -> addProxyEntry(builder, entry) }

            builder.addBypassRule("localhost")
            builder.addBypassRule("127.0.0.1")
            builder.addBypassRule("[::1]")
            builder.addBypassRule("10.0.2.2")

            (bypassRules + pacResult.directHosts).filter { it.isNotBlank() }.distinct().forEach { rule ->
                builder.addBypassRule(rule)
            }

            ProxyController.getInstance().setProxyOverride(
                builder.build(),
                { command -> command.run() },
                {
                    AppLogger.d(TAG, "PAC proxy applied from $pacUrl, " +
                            "${pacResult.proxies.size} proxy entries, ${pacResult.directHosts.size} direct hosts")
                }
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to apply PAC proxy", e)
        }
    }

    /**
     * Point translation requests at the same first hop the WebView just got.
     * DIRECT and HTTPS-upstream entries stay direct: [java.net.Proxy] cannot speak
     * an HTTPS proxy, and DIRECT must not be forced through a dead hop.
     */
    private fun publishTranslationRoute(entries: List<String>) {
        for (entry in entries) {
            val trimmed = entry.trim()
            when {
                trimmed.equals("DIRECT", ignoreCase = true) -> {
                    TranslateProxyRoute.clear()
                    return
                }
                trimmed.startsWith("SOCKS5 ", ignoreCase = true) ||
                    trimmed.startsWith("SOCKS ", ignoreCase = true) -> {
                    val server = trimmed.substringAfter(" ").trim()
                    val (host, port) = LegacyProxyFetchBridge.splitHostPort(server) ?: continue
                    TranslateProxyRoute.useSocks(host, port)
                    return
                }
                trimmed.startsWith("PROXY ", ignoreCase = true) -> {
                    val server = trimmed.substringAfter(" ").trim()
                    val (host, port) = LegacyProxyFetchBridge.splitHostPort(server) ?: continue
                    TranslateProxyRoute.useHttp(host, port)
                    return
                }
                trimmed.startsWith("HTTPS ", ignoreCase = true) -> {
                    TranslateProxyRoute.clear()
                    return
                }
            }
        }
        TranslateProxyRoute.clear()
    }

    /**
     * Same settings as [applyProxy], routed to [LegacyProxyFetchBridge] for
     * WebViews without PROXY_OVERRIDE. Reuses the PAC download/parse below so
     * both paths resolve identical upstreams.
     */
    private suspend fun applyLegacyFallback(
        proxyMode: String,
        staticProxyHost: String,
        staticProxyPort: Int,
        staticProxyType: String,
        pacUrl: String,
        bypassRules: List<String>,
        username: String,
        password: String
    ) {
        when (proxyMode.uppercase()) {
            "NONE" -> LegacyProxyFetchBridge.clear()
            "STATIC" -> {
                val spec = LegacyProxyFetchBridge.resolveStatic(
                    mode = proxyMode,
                    host = staticProxyHost,
                    port = staticProxyPort,
                    type = staticProxyType,
                    bypassRules = bypassRules,
                    username = username,
                    password = password
                )
                LegacyProxyFetchBridge.configure(spec)
            }
            "PAC" -> {
                if (pacUrl.isBlank()) {
                    AppLogger.w(TAG, "Empty PAC URL")
                    LegacyProxyFetchBridge.clear()
                    return
                }
                val pacScript = downloadPacScript(pacUrl)
                if (pacScript.isNullOrBlank()) {
                    AppLogger.e(TAG, "Failed to download PAC script from: $pacUrl")
                    LegacyProxyFetchBridge.clear()
                    return
                }
                val pacResult = PacScriptEvaluator.evaluate(pacScript, bypassRules)
                LegacyProxyFetchBridge.configure(
                    LegacyProxyFetchBridge.resolvePacEntries(
                        pacResult.proxies,
                        bypassRules + pacResult.directHosts,
                        username,
                        password
                    )
                )
            }
            else -> {
                AppLogger.w(TAG, "Unknown proxy mode: $proxyMode, clearing proxy")
                LegacyProxyFetchBridge.clear()
            }
        }
    }

    private suspend fun downloadPacScript(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/x-ns-proxy-autoconfig, application/javascript, text/javascript, */*")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    AppLogger.d(TAG, "PAC script downloaded: ${body?.length ?: 0} chars")
                    body
                } else {
                    AppLogger.e(TAG, "PAC download failed: ${response.code} ${response.message}")
                    null
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "PAC download error: ${e.message}")
                null
            }
        }
    }

    private fun addProxyEntry(builder: ProxyConfig.Builder, entry: String) {
        when {
            entry.startsWith("SOCKS5 ", ignoreCase = true) ||
            entry.startsWith("SOCKS ", ignoreCase = true) -> {
                val server = entry.substringAfter(" ").trim()
                if (server.isNotBlank()) builder.addProxyRule("socks5://$server")
            }
            entry.startsWith("PROXY ", ignoreCase = true) -> {
                val server = entry.substringAfter(" ").trim()
                if (server.isNotBlank()) builder.addProxyRule(server)
            }
            entry.startsWith("HTTPS ", ignoreCase = true) -> {
                val server = entry.substringAfter(" ").trim()
                if (server.isNotBlank()) builder.addProxyRule("https://$server")
            }
            entry.isNotBlank() && !entry.equals("DIRECT", ignoreCase = true) -> builder.addProxyRule(entry)
        }
    }

    fun getCachedPacUrl(): String? = cachedPacUrl

    suspend fun refreshPac(bypassRules: List<String> = emptyList()) {
        val url = cachedPacUrl ?: return
        applyPacProxy(url, bypassRules)
    }
}
