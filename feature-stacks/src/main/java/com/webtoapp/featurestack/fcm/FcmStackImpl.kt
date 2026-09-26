package com.webtoapp.featurestack.fcm

import android.content.Context
import android.content.Intent
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.webtoapp.core.featurestack.api.FcmConfig
import com.webtoapp.core.featurestack.api.FcmEventSink
import com.webtoapp.core.featurestack.api.FcmStack
import com.webtoapp.core.featurestack.api.FeatureRuntime

/**
 * FCM feature-stack implementation: Firebase init, token fetch and inbound
 * message → notification handling.
 *
 * FirebaseApp initialization uses the SDK's own manifest-metadata discovery —
 * the shell manifest declares the `ComponentDiscoveryService` entry with the
 * registrar list (the platform reads manifest metadata by component name without
 * loading the class), so [FirebaseApp.initializeApp] works identically to the
 * merged-AAR setup.
 *
 * Delivery path (no Firebase classes in the manifest):
 *   GMS → c2dm RECEIVE → FcmProxyReceiver (main dex) → unwraps wrapped_intent →
 *   NotificationFcmService proxy (main dex) → [onMessagingEvent] here.
 */
class FcmStackImpl : FcmStack {

    private lateinit var appContext: Context
    private lateinit var runtime: FeatureRuntime

    override fun init(context: Context, runtime: FeatureRuntime) {
        appContext = context.applicationContext
        this.runtime = runtime
    }

    override fun start(config: FcmConfig, sink: FcmEventSink) {
        val options = FirebaseOptions.Builder()
            .setProjectId(config.projectId)
            .setApplicationId(config.applicationId)
            .setApiKey(config.apiKey)
            .setGcmSenderId(config.senderId)
            .build()
        val existing = FirebaseApp.getApps(appContext)
        if (existing.isEmpty()) {
            FirebaseApp.initializeApp(appContext, options)
        } else {
            val app = FirebaseApp.getInstance()
            val current = app.options
            val same = current.projectId == config.projectId &&
                current.applicationId == config.applicationId &&
                current.apiKey == config.apiKey &&
                current.gcmSenderId == config.senderId
            if (!same) {
                app.delete()
                FirebaseApp.initializeApp(appContext, options)
            }
        }
        fetchToken(sink)
    }

    override fun refreshToken(sink: FcmEventSink) {
        if (FirebaseApp.getApps(appContext).isEmpty()) return
        fetchToken(sink)
    }

    private fun fetchToken(sink: FcmEventSink) {
        try {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token -> sink.onToken(token) }
                .addOnFailureListener { e ->
                    runtime.log(FeatureRuntime.LOG_ERROR, TAG, "Failed to get FCM token", e)
                }
        } catch (e: Exception) {
            runtime.log(FeatureRuntime.LOG_ERROR, TAG, "Failed to request FCM token", e)
        }
    }

    override fun onMessagingEvent(intent: Intent, sink: FcmEventSink) {
        // Host preview delivers through the real FirebaseInstanceIdReceiver →
        // ServiceStarter, which wraps the c2dm intent in `wrapped_intent` the same
        // way FirebaseMessagingService expects. The shell's FcmProxyReceiver already
        // unwraps, so unwrap only when the extra is present.
        @Suppress("DEPRECATION")
        val effective = intent.getParcelableExtra<Intent>(EXTRA_WRAPPED_INTENT) ?: intent
        when (effective.action) {
            ACTION_NEW_TOKEN -> {
                effective.getStringExtra("token")?.let { sink.onToken(it) }
            }
            else -> {
                // REMOTE_INTENT / MESSAGING_EVENT / RECEIVE_DIRECT_BOOT extras.
                val extras = effective.extras ?: return
                val messageType = extras.getString("message_type")
                if (messageType != null && messageType != "gcm") {
                    // deleted_messages / send_event / send_error — same as stock.
                    runtime.log(FeatureRuntime.LOG_DEBUG, TAG, "Skipping FCM message_type=$messageType", null)
                    return
                }
                try {
                    handleMessage(RemoteMessage(extras))
                } catch (e: Exception) {
                    runtime.log(FeatureRuntime.LOG_ERROR, TAG, "Failed to handle FCM message", e)
                }
            }
        }
    }

    private fun handleMessage(message: RemoteMessage) {
        runtime.ensureNotificationChannel(
            appContext,
            CHANNEL_ID,
            runtime.localizedString("fcm_channel_name"),
            runtime.localizedString("fcm_channel_description")
        )
        val data = message.data
        val notification = message.notification
        val title = notification?.title
            ?.ifBlank { null }
            ?: data["title"]
            ?: data["subject"]
            ?: runtime.fcmAppName(appContext).ifBlank {
                runtime.localizedString("generic_notification_label")
            }
        val body = notification?.body
            ?.ifBlank { null }
            ?: data["body"]
            ?: data["message"]
            ?: data["content"]
            ?: ""
        val clickUrl = data["url"]
            ?: data["clickUrl"]
            ?: data["link"]
            ?: runtime.fcmClickUrl(appContext)
        if (title.isBlank() && body.isBlank()) {
            runtime.log(FeatureRuntime.LOG_WARN, TAG, "Empty FCM payload, skip", null)
            return
        }
        runtime.showNotification(appContext, CHANNEL_ID, title, body, clickUrl)
        runtime.log(FeatureRuntime.LOG_DEBUG, TAG, "Displayed FCM notification: $title", null)
    }

    private companion object {
        const val TAG = "FcmStackImpl"
        const val CHANNEL_ID = "fcm_notification_channel"
        const val ACTION_NEW_TOKEN = "com.google.firebase.messaging.NEW_TOKEN"
        const val EXTRA_WRAPPED_INTENT = "wrapped_intent"
    }
}
