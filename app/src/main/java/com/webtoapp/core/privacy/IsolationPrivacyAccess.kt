package com.webtoapp.core.privacy

import android.content.Context
import android.webkit.WebView
import com.webtoapp.core.feature.FeatureLoader

object IsolationPrivacyAccess {
    private const val CLASS = "com.webtoapp.core.privacy.IsolationManager"

    private fun resolve(): Class<*>? = FeatureLoader.loadClass(CLASS)

    fun isAvailable(): Boolean = resolve() != null

    fun getInstance(context: Context): Any? {
        return runCatching {
            val c = resolve() ?: return null
            c.getMethod("getInstance", Context::class.java).invoke(null, context)
        }.getOrNull()
    }

    fun initialize(instance: Any?, config: IsolationConfig) {
        if (instance == null) return
        runCatching {
            instance.javaClass.getMethod("initialize", IsolationConfig::class.java)
                .invoke(instance, config)
        }
    }

    fun generateIsolationScript(instance: Any?): String {
        if (instance == null) return ""
        return runCatching {
            instance.javaClass.getMethod("generateIsolationScript").invoke(instance) as? String
        }.getOrNull().orEmpty()
    }

    fun applyToWebView(instance: Any?, webView: WebView) {
        if (instance == null) return
        runCatching {
            instance.javaClass.getMethod("applyToWebView", WebView::class.java)
                .invoke(instance, webView)
        }
    }

    fun getUserAgent(instance: Any?): String? {
        if (instance == null) return null
        return runCatching {
            instance.javaClass.getMethod("getUserAgent").invoke(instance) as? String
        }.getOrNull()
    }

    @Suppress("UNCHECKED_CAST")
    fun getCustomHeaders(instance: Any?): Map<String, String> {
        if (instance == null) return emptyMap()
        return runCatching {
            instance.javaClass.getMethod("getCustomHeaders").invoke(instance) as? Map<String, String>
        }.getOrNull().orEmpty()
    }
}
