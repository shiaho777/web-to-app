package com.webtoapp.core.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger

class NotificationFcmService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "NotificationFcmService"
        const val CHANNEL_ID = "fcm_notification_channel"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AppLogger.i(TAG, "FCM onNewToken")
        NotificationFcmManager.onNewToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        try {
            PushNotificationHelper.ensureChannel(
                context = this,
                channelId = CHANNEL_ID,
                channelName = Strings.fcmNotificationChannelName,
                channelDescription = Strings.fcmNotificationChannelDescription
            )
            val data = message.data
            val notification = message.notification
            val title = notification?.title
                ?.ifBlank { null }
                ?: data["title"]
                ?: data["subject"]
                ?: NotificationFcmManager.getAppName(this).ifBlank { Strings.genericNotificationLabel }
            val body = notification?.body
                ?.ifBlank { null }
                ?: data["body"]
                ?: data["message"]
                ?: data["content"]
                ?: ""
            val clickUrl = data["url"]
                ?: data["clickUrl"]
                ?: data["link"]
                ?: NotificationFcmManager.getClickUrl(this)
            if (title.isBlank() && body.isBlank()) {
                AppLogger.w(TAG, "Empty FCM payload, skip")
                return
            }
            PushNotificationHelper.show(
                context = this,
                channelId = CHANNEL_ID,
                title = title,
                body = body,
                clickUrl = clickUrl
            )
            AppLogger.d(TAG, "Displayed FCM notification: $title")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to handle FCM message", e)
        }
    }
}
