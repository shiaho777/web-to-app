package com.webtoapp.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger

object PushNotificationHelper {
    private const val TAG = "PushNotificationHelper"

    fun ensureChannel(
        context: Context,
        channelId: String,
        channelName: String,
        channelDescription: String
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(channelId)
        if (existing != null) return
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = channelDescription
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun canPostNotifications(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun show(
        context: Context,
        channelId: String,
        title: String,
        body: String,
        clickUrl: String = "",
        notificationId: Int = (System.currentTimeMillis() and 0x7fffffff).toInt()
    ) {
        try {
            if (!canPostNotifications(context)) return
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                if (clickUrl.isNotBlank()) {
                    putExtra("notification_click_url", clickUrl)
                }
            }
            val pendingIntent = if (launchIntent != null) {
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else null
            val iconResId = context.applicationInfo.icon.takeIf { it != 0 }
                ?: android.R.drawable.ic_menu_info_details
            val resolvedTitle = title.ifBlank {
                context.applicationInfo.loadLabel(context.packageManager)?.toString()
                    ?: Strings.genericNotificationLabel
            }
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(iconResId)
                .setContentTitle(resolvedTitle)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .apply {
                    if (pendingIntent != null) setContentIntent(pendingIntent)
                }
                .build()
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            AppLogger.e(TAG, "Notification permission unavailable", e)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to show notification", e)
        }
    }
}
