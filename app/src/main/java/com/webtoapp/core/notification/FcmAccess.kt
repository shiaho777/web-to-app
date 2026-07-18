package com.webtoapp.core.notification

import android.content.Context
import com.webtoapp.core.feature.FeatureLoader
import com.webtoapp.core.feature.ReflectInvoke
import com.webtoapp.core.logging.AppLogger

object FcmAccess {
    private const val TAG = "FcmAccess"
    private const val MANAGER = "com.webtoapp.core.notification.NotificationFcmManager"

    fun isAvailable(): Boolean = FeatureLoader.loadClass(MANAGER) != null

    fun start(
        context: Context,
        projectId: String,
        applicationId: String,
        apiKey: String,
        senderId: String,
        registerUrl: String,
        registerHeaders: String,
        authToken: String,
        clickUrl: String,
        appName: String,
        googleServicesJson: String
    ) {
        try {
            val manager = FeatureLoader.loadClass(MANAGER) ?: return
            val configClass = FeatureLoader.loadClass("$MANAGER\$FcmConfig")
                ?: Class.forName("$MANAGER\$FcmConfig", false, manager.classLoader)
            val config = configClass.getDeclaredConstructor(
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java,
                String::class.java
            ).newInstance(
                projectId,
                applicationId,
                apiKey,
                senderId,
                registerUrl,
                registerHeaders,
                authToken,
                clickUrl,
                appName,
                googleServicesJson
            )
            ReflectInvoke.call(manager, "start", context, config)
        } catch (e: Exception) {
            AppLogger.e(TAG, "FCM start failed", e)
        }
    }

    fun restoreIfNeeded(context: Context) {
        try {
            val manager = FeatureLoader.loadClass(MANAGER) ?: return
            ReflectInvoke.call(manager, "restoreIfNeeded", context)
        } catch (e: Exception) {
            AppLogger.w(TAG, "FCM restoreIfNeeded failed: ${e.message}")
        }
    }
}
