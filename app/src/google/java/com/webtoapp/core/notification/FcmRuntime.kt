package com.webtoapp.core.notification

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.webtoapp.core.logging.AppLogger

/**
 * Firebase Cloud Messaging client. Only on the classpath when the build is not
 * `-Pfdroid=true`. The fdroid source set supplies a no-op with this same name.
 */
internal object FcmRuntime {
    private const val TAG = "FcmRuntime"

    fun start(context: Context, config: NotificationFcmManager.FcmConfig) {
        val resolved = NotificationFcmManager.resolveConfig(config)
        try {
            initializeFirebase(context, resolved)
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    NotificationFcmManager.onNewToken(context, token)
                    AppLogger.i(TAG, "FCM token ready")
                }
                .addOnFailureListener { e ->
                    AppLogger.e(TAG, "Failed to get FCM token", e)
                }
            AppLogger.i(TAG, "FCM initialized for project=${resolved.projectId}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to start FCM", e)
        }
    }

    private fun initializeFirebase(context: Context, config: NotificationFcmManager.FcmConfig) {
        val resolved = NotificationFcmManager.resolveConfig(config)
        val options = FirebaseOptions.Builder()
            .setProjectId(resolved.projectId)
            .setApplicationId(resolved.applicationId)
            .setApiKey(resolved.apiKey)
            .setGcmSenderId(resolved.senderId)
            .build()
        val existing = FirebaseApp.getApps(context)
        if (existing.isEmpty()) {
            FirebaseApp.initializeApp(context, options)
        } else {
            val app = FirebaseApp.getInstance()
            val current = app.options
            val same = current.projectId == resolved.projectId &&
                current.applicationId == resolved.applicationId &&
                current.apiKey == resolved.apiKey &&
                current.gcmSenderId == resolved.senderId
            if (!same) {
                app.delete()
                FirebaseApp.initializeApp(context, options)
            }
        }
    }
}
