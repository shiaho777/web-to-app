package com.webtoapp.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.webtoapp.core.logging.AppLogger

class BridgeAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SCHEDULED_NOTIFICATION = "com.webtoapp.bridge.SCHEDULED_NOTIFICATION"

        const val EXTRA_TITLE = "bridge_notif_title"
        const val EXTRA_BODY = "bridge_notif_body"
        const val EXTRA_CHANNEL_ID = "bridge_notif_channel_id"
        const val EXTRA_TAG = "bridge_notif_tag"
        const val EXTRA_DEEP_LINK = "bridge_notif_deep_link"
        const val EXTRA_PLAY_SOUND = "bridge_notif_play_sound"

        private const val DEFAULT_CHANNEL_ID = "webapp_bridge_notifications"
        private const val DEFAULT_CHANNEL_NAME = "Web App Notifications"
        private const val TAG = "BridgeAlarmReceiver"

        fun postNotification(
            context: Context,
            title: String,
            body: String,
            tag: String = "",
            channelId: String = DEFAULT_CHANNEL_ID,
            deepLink: String = "",
            playSound: Boolean = true
        ): Boolean {
            return try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    AppLogger.w(TAG, "POST_NOTIFICATIONS permission not granted")
                    return false
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = if (playSound) {
                        NotificationManager.IMPORTANCE_DEFAULT
                    } else {
                        NotificationManager.IMPORTANCE_LOW
                    }
                    val channel = NotificationChannel(
                        channelId.ifBlank { DEFAULT_CHANNEL_ID },
                        DEFAULT_CHANNEL_NAME,
                        importance
                    ).apply {
                        setShowBadge(true)
                        enableVibration(true)
                        if (!playSound) setSound(null, null)
                    }
                    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.createNotificationChannel(channel)
                }

                val launchIntent = context.packageManager
                    .getLaunchIntentForPackage(context.packageName)
                    ?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        if (deepLink.isNotBlank()) {
                            data = android.net.Uri.parse(deepLink)
                        }
                    }

                val requestCode = (tag.ifBlank { "$title|$body" }).hashCode()
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    requestCode,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, channelId.ifBlank { DEFAULT_CHANNEL_ID })
                    .setSmallIcon(context.applicationInfo.icon)
                    .setContentTitle(title.ifBlank {
                        context.applicationInfo.loadLabel(context.packageManager).toString()
                    })
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(
                        if (playSound) NotificationCompat.PRIORITY_DEFAULT
                        else NotificationCompat.PRIORITY_LOW
                    )
                    .build()

                NotificationManagerCompat.from(context).notify(requestCode, notification)
                true
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to post notification", e)
                false
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SCHEDULED_NOTIFICATION) return

        try {
            val title = intent.getStringExtra(EXTRA_TITLE) ?: "Notification"
            val body = intent.getStringExtra(EXTRA_BODY) ?: ""
            val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: DEFAULT_CHANNEL_ID
            val tag = intent.getStringExtra(EXTRA_TAG) ?: ""
            val deepLink = intent.getStringExtra(EXTRA_DEEP_LINK) ?: ""
            val playSound = intent.getBooleanExtra(EXTRA_PLAY_SOUND, true)

            postNotification(context, title, body, tag, channelId, deepLink, playSound)
            AppLogger.d(TAG, "Scheduled notification fired: title=$title, tag=$tag")
        } catch (e: Exception) {
            AppLogger.e(TAG, "onReceive failed", e)
        }
    }
}
