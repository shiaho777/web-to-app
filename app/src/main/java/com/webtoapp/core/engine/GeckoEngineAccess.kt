package com.webtoapp.core.engine

import android.content.Context
import android.view.View
import com.webtoapp.core.feature.FeatureLoader
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.DnsConfig
import com.webtoapp.data.model.WebViewConfig

object GeckoEngineAccess {
    private const val TAG = "GeckoEngineAccess"
    private const val CLASS_NAME = "com.webtoapp.core.engine.GeckoViewEngine"

    private fun resolveClass(): Class<*>? = FeatureLoader.loadClass(CLASS_NAME)

    fun isAvailable(): Boolean = resolveClass() != null

    fun create(context: Context): BrowserEngine? {
        return try {
            val c = resolveClass() ?: return null
            c.getConstructor(Context::class.java).newInstance(context) as BrowserEngine
        } catch (e: Exception) {
            AppLogger.e(TAG, "create GeckoViewEngine failed", e)
            null
        }
    }

    fun createView(
        engine: BrowserEngine,
        context: Context,
        config: WebViewConfig,
        callback: BrowserEngineCallback
    ): View? {
        return try {
            engine.javaClass.getMethod(
                "createView",
                Context::class.java,
                WebViewConfig::class.java,
                BrowserEngineCallback::class.java
            ).invoke(engine, context, config, callback) as? View
        } catch (e: Exception) {
            AppLogger.e(TAG, "createView failed", e)
            null
        }
    }

    fun applyDnsConfig(config: DnsConfig) {
        invokeStatic("applyDnsConfig", arrayOf(DnsConfig::class.java), arrayOf(config))
    }

    fun applyAntiCapture(active: Boolean) {
        invokeStatic("applyAntiCapture", arrayOf(Boolean::class.javaPrimitiveType!!), arrayOf(active))
    }

    fun setTlsMitmActive(active: Boolean) {
        invokeStatic("setTlsMitmActive", arrayOf(Boolean::class.javaPrimitiveType!!), arrayOf(active))
    }

    fun applyProxyConfig(config: ProxyConfig) {
        invokeStatic("applyProxyConfig", arrayOf(ProxyConfig::class.java), arrayOf(config))
    }

    private fun invokeStatic(method: String, types: Array<Class<*>>, args: Array<Any?>) {
        try {
            val c = resolveClass() ?: return
            c.getMethod(method, *types).invoke(null, *args)
        } catch (e: Exception) {
            AppLogger.w(TAG, "invokeStatic $method failed: ${e.message}")
        }
    }
}
