package com.webtoapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.webtoapp.core.logging.AppLogger

object SafeNotificationChannels {
    fun ensure(
        context: Context,
        id: String,
        name: String,
        importance: Int = NotificationManager.IMPORTANCE_LOW,
        description: String? = null,
        configure: (NotificationChannel.() -> Unit)? = null
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        try {
            val safeId = id.ifBlank { "app_default_channel" }
            val safeName = name.ifBlank { safeId }
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(safeId, safeName, importance).apply {
                if (!description.isNullOrBlank()) {
                    this.description = description
                }
                configure?.invoke(this)
            }
            manager.createNotificationChannel(channel)
        } catch (e: Exception) {
            AppLogger.e("SafeNotificationChannels", "create failed id=$id name=$name", e)
        }
    }
}
