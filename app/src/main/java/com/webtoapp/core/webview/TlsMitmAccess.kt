package com.webtoapp.core.webview

import com.webtoapp.core.feature.FeatureLoader
import com.webtoapp.core.logging.AppLogger
import java.io.File

object TlsMitmAccess {
    private const val TAG = "TlsMitmAccess"
    private const val BRIDGE = "com.webtoapp.core.webview.TlsMitmBridge"
    private const val TEMPLATE = "com.webtoapp.core.webview.TlsFingerprintTemplate"

    fun isAvailable(): Boolean = FeatureLoader.loadClass(BRIDGE) != null

    fun start(
        templateId: String,
        customCipherSuites: List<String>,
        upstreamSocks: LocalHttpToSocksBridge.Upstream?,
        caDir: File
    ): Int {
        return try {
            val bridge = FeatureLoader.loadClass(BRIDGE) ?: return -1
            val templateClass = FeatureLoader.loadClass(TEMPLATE) ?: Class.forName(TEMPLATE)
            val template = templateClass.getMethod("fromId", String::class.java).invoke(null, templateId)
            val configClass = FeatureLoader.loadClass("$BRIDGE\$Config")
                ?: Class.forName("$BRIDGE\$Config", false, bridge.classLoader)
            val config = configClass.getDeclaredConstructor(
                templateClass,
                List::class.java,
                LocalHttpToSocksBridge.Upstream::class.java
            ).newInstance(template, customCipherSuites, upstreamSocks)
            val method = bridge.getMethod("start", configClass, File::class.java)
            (method.invoke(null, config, caDir) as? Int) ?: -1
        } catch (e: Exception) {
            AppLogger.e(TAG, "TlsMitmBridge.start failed", e)
            -1
        }
    }

    fun isSignedByLocalCa(cert: java.security.cert.X509Certificate): Boolean {
        return try {
            val ca = FeatureLoader.loadClass("com.webtoapp.core.webview.TlsMitmCaManager") ?: return false
            ca.getMethod("isSignedByLocalCa", java.security.cert.X509Certificate::class.java)
                .invoke(null, cert) as? Boolean ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun isRunning(): Boolean {
        return try {
            val bridge = FeatureLoader.loadClass(BRIDGE) ?: return false
            bridge.getMethod("isRunning").invoke(null) as? Boolean ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun stop() {
        try {
            val bridge = FeatureLoader.loadClass(BRIDGE) ?: return
            bridge.getMethod("stop").invoke(null)
        } catch (e: Exception) {
            AppLogger.w(TAG, "TlsMitmBridge.stop failed: ${e.message}")
        }
    }
}
