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
        const val ACTION_SCHEDULED_WORKER = "com.webtoapp.bridge.SCHEDULED_WORKER"

        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_CHANNEL_NAME = "channel_name"
        const val EXTRA_TAG = "tag"
        const val EXTRA_DEEP_LINK = "deep_link"
        const val EXTRA_PLAY_SOUND = "play_sound"
        const val EXTRA_TASK_ID = "task_id"

        private const val DEFAULT_CHANNEL_ID = "webapp_notifications"
        private const val DEFAULT_CHANNEL_NAME = "Web App Notifications"

        fun postNotification(
            context: Context,
            title: String,
            body: String,
            tag: String = "",
            channelId: String = DEFAULT_CHANNEL_ID,
            channelName: String = DEFAULT_CHANNEL_NAME,
            deepLink: String = "",
            playSound: Boolean = true
        ): Boolean {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
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
                        channelName.ifBlank { DEFAULT_CHANNEL_NAME },
                        importance
                    ).apply {
                        setShowBadge(true)
                        enableVibration(true)
                        if (!playSound) {
                            setSound(null, null)
                        }
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

                val notification = NotificationCompat.Builder(
                    context,
                    channelId.ifBlank { DEFAULT_CHANNEL_ID }
                )
                    .setSmallIcon(context.applicationInfo.icon)
                    .setContentTitle(title.ifBlank {
                        context.applicationInfo.loadLabel(context.packageManager).toString()
                    })
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(
                        if (playSound) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_LOW
                    )
                    .build()

                NotificationManagerCompat.from(context).notify(requestCode, notification)
                true
            } catch (e: Exception) {
                AppLogger.e("BridgeAlarmReceiver", "Failed to post notification", e)
                false
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            when (intent.action) {
                ACTION_SCHEDULED_NOTIFICATION -> {
                    val title = intent.getStringExtra(EXTRA_TITLE) ?: "Scheduled Notification"
                    val body = intent.getStringExtra(EXTRA_BODY) ?: ""
                    val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: "webapp_notifications"
                    val channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: "Web App Notifications"
                    val tag = intent.getStringExtra(EXTRA_TAG) ?: ""
                    val deepLink = intent.getStringExtra(EXTRA_DEEP_LINK) ?: ""
                    val playSound = intent.getBooleanExtra(EXTRA_PLAY_SOUND, true)
                    postNotification(context, title, body, tag, channelId, channelName, deepLink, playSound)
                }

                ACTION_SCHEDULED_WORKER -> {
                    val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: "worker_task"
                    postNotification(
                        context = context,
                        title = "Background Worker Executed",
                        body = "Task \"$taskId\" finished",
                        tag = taskId,
                        channelId = "webapp_worker_notifications",
                        channelName = "Worker Notifications",
                        playSound = false
                    )
                }
            }
        } catch (e: Exception) {
            AppLogger.e("BridgeAlarmReceiver", "Receiver execution failed", e)
        }
    }
}
