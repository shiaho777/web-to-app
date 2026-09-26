package com.webtoapp.core.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.webtoapp.core.featurestack.FeatureStackLoader
import com.webtoapp.core.featurestack.api.FcmStack
import com.webtoapp.core.logging.AppLogger

/**
 * Proxy service for FCM delivery. Firebase classes live in the optional
 * `feature_stacks/fcm.dex` archive (so builds can leave the stack out entirely);
 * this service keeps the same manifest identity and forwards raw intents to the
 * dex implementation. When the stack is absent the intent is dropped and the
 * service stops — nothing crashes.
 */
class NotificationFcmService : Service() {

    companion object {
        private const val TAG = "NotificationFcmService"
        const val CHANNEL_ID = "fcm_notification_channel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent != null) {
                val stack = FeatureStackLoader.load<FcmStack>(this, FeatureStackLoader.STACK_FCM)
                if (stack == null) {
                    AppLogger.d(TAG, "FCM stack unavailable, dropping messaging event")
                } else {
                    stack.onMessagingEvent(intent, NotificationFcmManager.eventSink(applicationContext))
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to dispatch FCM event", e)
        } finally {
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }
}
