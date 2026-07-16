package com.webtoapp.core.engine

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import com.webtoapp.core.adblock.AdBlocker
import com.webtoapp.core.engine.download.EngineFileManager
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.webview.WebViewCallbacks
import com.webtoapp.core.webview.WebViewManager
import com.webtoapp.data.model.WebViewConfig

object EngineViewFactory {
    private const val TAG = "EngineViewFactory"

    fun resolveEngineType(
        requested: String?,
        config: WebViewConfig,
        context: Context
    ): EngineType {
        val raw = requested?.takeIf { it.isNotBlank() } ?: "SYSTEM_WEBVIEW"
        var type = EngineType.fromString(raw)
        if (config.dnsConfig.echEffective) {
            type = EngineType.GECKOVIEW
        }
        if (type == EngineType.GECKOVIEW) {
            val available = EngineFileManager(context).isEngineDownloaded(EngineType.GECKOVIEW) ||
                isBundledGeckoAvailable(context)
            if (!available) {
                AppLogger.w(TAG, "GeckoView requested but runtime not available; falling back to System WebView")
                return EngineType.SYSTEM_WEBVIEW
            }
        }
        return type
    }

    private fun isBundledGeckoAvailable(context: Context): Boolean {
        return try {
            context.assets.open("omni.ja").close()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun create(
        context: Context,
        engineTypeName: String?,
        config: WebViewConfig,
        webViewManager: WebViewManager,
        callbacks: WebViewCallbacks,
        adBlocker: AdBlocker,
        extensionModuleIds: List<String> = emptyList(),
        embeddedExtensionModules: List<com.webtoapp.core.shell.EmbeddedShellModule> = emptyList(),
        extensionFabIcon: String = "",
        allowGlobalModuleFallback: Boolean = false,
        extensionEnabled: Boolean = true,
        browserDisguiseConfig: com.webtoapp.core.appearance.BrowserDisguiseConfig? = null,
        deviceDisguiseConfig: com.webtoapp.core.appearance.DeviceDisguiseConfig? = null
    ): BrowserSurface {
        val engineType = resolveEngineType(engineTypeName, config, context)
        AppLogger.i(
            TAG,
            "create: requested=$engineTypeName resolved=$engineType ech=${config.dnsConfig.echEffective} dnsMode=${config.dnsMode}"
        )

        if (engineType == EngineType.GECKOVIEW) {
            prepareGeckoNetwork(config)
            val engine = EngineManager.getInstance(context).createEngine(EngineType.GECKOVIEW, adBlocker)
            val view = GeckoEngineAccess.createView(
                engine = engine,
                context = context,
                config = config,
                callback = callbacks.toBrowserEngineCallback()
            ) ?: return BrowserSurface.fromWebView(
                WebView(context).also { wv ->
                    wv.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewManager.configureWebView(
                        wv,
                        config,
                        callbacks,
                        extensionModuleIds,
                        embeddedExtensionModules,
                        extensionFabIcon,
                        allowGlobalModuleFallback = allowGlobalModuleFallback,
                        extensionEnabled = extensionEnabled,
                        browserDisguiseConfig = browserDisguiseConfig,
                        deviceDisguiseConfig = deviceDisguiseConfig
                    )
                }
            )
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            return BrowserSurface.fromEngine(engine, view)
        }

        val webView = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewManager.configureWebView(
                this,
                config,
                callbacks,
                extensionModuleIds,
                embeddedExtensionModules,
                extensionFabIcon,
                allowGlobalModuleFallback = allowGlobalModuleFallback,
                extensionEnabled = extensionEnabled,
                browserDisguiseConfig = browserDisguiseConfig,
                deviceDisguiseConfig = deviceDisguiseConfig
            )
        }
        return BrowserSurface.fromWebView(webView)
    }

    private fun prepareGeckoNetwork(config: WebViewConfig) {
        if (config.dnsMode != "SYSTEM") {
            GeckoEngineAccess.applyDnsConfig(config.dnsConfig)
        } else {
            GeckoEngineAccess.applyDnsConfig(
                com.webtoapp.data.model.DnsConfig(provider = "custom", customDohUrl = "")
            )
        }
        GeckoEngineAccess.applyAntiCapture(config.antiCapture)

        val tlsFingerprintEnabled = config.tlsFingerprintEnabled &&
            config.tlsFingerprintTemplate.isNotBlank()
        val echActive = config.dnsConfig.echEffective
        if (tlsFingerprintEnabled && echActive) {
            AppLogger.w(
                TAG,
                "TLS fingerprint MITM skipped while ECH is effective (MITM would terminate TLS and strip ECH)"
            )
            GeckoEngineAccess.setTlsMitmActive(false)
        } else if (tlsFingerprintEnabled) {
            AppLogger.w(
                TAG,
                "TLS fingerprint MITM is configured for System WebView path; Gecko path uses native TLS without MITM"
            )
            GeckoEngineAccess.setTlsMitmActive(false)
        } else {
            GeckoEngineAccess.setTlsMitmActive(false)
        }

        when (config.proxyMode) {
            "STATIC" -> {
                if (config.proxyHost.isNotBlank() && config.proxyPort > 0) {
                    GeckoEngineAccess.applyProxyConfig(
                        ProxyConfig(
                            mode = "STATIC",
                            host = config.proxyHost,
                            port = config.proxyPort,
                            type = config.proxyType.ifBlank { "HTTP" },
                            username = config.proxyUsername,
                            password = config.proxyPassword
                        )
                    )
                } else {
                    GeckoEngineAccess.applyProxyConfig(ProxyConfig(mode = "NONE"))
                }
            }
            "PAC" -> {
                if (config.pacUrl.isNotBlank()) {
                    GeckoEngineAccess.applyProxyConfig(
                        ProxyConfig(
                            mode = "PAC",
                            pacUrl = config.pacUrl
                        )
                    )
                } else {
                    GeckoEngineAccess.applyProxyConfig(ProxyConfig(mode = "NONE"))
                }
            }
            else -> GeckoEngineAccess.applyProxyConfig(ProxyConfig(mode = "NONE"))
        }
    }
}

fun WebViewCallbacks.toBrowserEngineCallback(): BrowserEngineCallback {
    val source = this
    return object : BrowserEngineCallback {
        override fun onPageStarted(url: String?) = source.onPageStarted(url)
        override fun onPageFinished(url: String?) = source.onPageFinished(url)
        override fun onProgressChanged(progress: Int) = source.onProgressChanged(progress)
        override fun onTitleChanged(title: String?) = source.onTitleChanged(title)
        override fun onIconReceived(icon: android.graphics.Bitmap?) = source.onIconReceived(icon)
        override fun onError(errorCode: Int, description: String) = source.onError(errorCode, description)
        override fun onSslError(error: String) = source.onSslError(error)
        override fun onExternalLink(url: String) = source.onExternalLink(url)
        override fun onShowCustomView(view: android.view.View?, callback: Any?) {
            @Suppress("UNCHECKED_CAST")
            val chromeCb = callback as? android.webkit.WebChromeClient.CustomViewCallback
            source.onShowCustomView(view, chromeCb)
        }
        override fun onHideCustomView() = source.onHideCustomView()
        override fun onDownloadStart(
            url: String,
            userAgent: String,
            contentDisposition: String,
            mimeType: String,
            contentLength: Long
        ) = source.onDownloadStart(url, userAgent, contentDisposition, mimeType, contentLength)

        override fun onConsoleMessage(level: Int, message: String, sourceId: String, lineNumber: Int) {
            source.onConsoleMessage(level, message, sourceId, lineNumber)
        }

        override fun onNewWindow(resultMsg: android.os.Message?) {
            source.onNewWindow(resultMsg)
        }
    }
}
