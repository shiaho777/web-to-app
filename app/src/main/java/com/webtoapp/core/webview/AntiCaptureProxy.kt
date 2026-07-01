package com.webtoapp.core.webview

import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.webtoapp.core.logging.AppLogger

object AntiCaptureProxy {

    private const val TAG = "AntiCaptureProxy"

    @Volatile
    private var active: Boolean = false

    fun isActive(): Boolean = active

    fun apply() {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            AppLogger.w(TAG, "PROXY_OVERRIDE not supported, cannot enforce anti-capture direct proxy")
            return
        }
        try {
            val builder = ProxyConfig.Builder()
                .addProxyRule("direct://")
            builder.addBypassRule("localhost")
            builder.addBypassRule("127.0.0.1")
            builder.addBypassRule("[::1]")
            builder.addBypassRule("10.0.2.2")
            ProxyController.getInstance().setProxyOverride(
                builder.build(),
                { command -> command.run() },
                { AppLogger.i(TAG, "Anti-capture direct proxy override applied (system proxy ignored)") }
            )
            active = true
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to apply anti-capture proxy override", e)
        }
    }

    fun clear() {
        active = false
    }
}
