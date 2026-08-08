package com.webtoapp.core.webview

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.webtoapp.util.SafeNotificationChannels

/**
 * Foreground service backing WebView media playback.
 *
 * The audio itself keeps playing inside the WebView; this service keeps the app
 * eligible for ongoing media playback, holds a wake lock while playing, and owns
 * the media notification lifecycle (start while playing, detach on pause, remove
 * on stop). Notification controls dispatch explicit commands back to the active
 * [MediaSessionBridge] — they do NOT depend on a generic ACTION_MEDIA_BUTTON
 * broadcast receiver.
 */
class WebMediaPlaybackService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_PLAYING -> {
                acquireWakeLock()

                startAsForeground()
            }

            ACTION_PAUSED -> {
                startAsForeground()

                releaseWakeLock()

                stopForegroundCompat(STOP_FOREGROUND_DETACH)

                stopSelf()
            }

            ACTION_MEDIA_COMMAND -> {
                /*
                 * A foreground-service PendingIntent is used for notification
                 * controls on Android 8+. We must enter the foreground before
                 * dispatching the command.
                 */
                startAsForeground()

                val command = intent.getStringExtra(EXTRA_COMMAND)
                val value = intent.getDoubleExtra(EXTRA_VALUE, 0.0)

                if (!command.isNullOrBlank()) {
                    MediaSessionBridge.dispatchFromService(command, value)
                }

                /*
                 * A play command will immediately synchronize the service again
                 * as PLAYING. Other commands do not need to keep this temporary
                 * service instance in the foreground.
                 */
                if (command != "play") {
                    stopForegroundCompat(STOP_FOREGROUND_DETACH)

                    stopSelf()
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAsForeground() {
        val notification = MediaSessionBridge.getForegroundNotification(this)
            ?: buildFallbackNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            @Suppress("DEPRECATION")
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundCompat(mode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(mode)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(mode == STOP_FOREGROUND_REMOVE)
        }
    }

    private fun acquireWakeLock() {
        val current = wakeLock

        if (current?.isHeld == true) {
            return
        }

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$packageName:WebMediaPlayback"
        ).apply {
            setReferenceCounted(false)

            /*
             * A timeout prevents an accidental permanent wake lock.
             * The timeout is renewed whenever PLAYING is reported again.
             */
            acquire(WAKE_LOCK_TIMEOUT)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }

        wakeLock = null
    }

    private fun createNotificationChannel() {
        SafeNotificationChannels.ensure(
            this,
            CHANNEL_ID,
            "Media playback",
            importance = NotificationManager.IMPORTANCE_LOW,
            description = "Media currently playing inside the app"
        ) {
            setShowBadge(false)
        }
    }

    private fun buildFallbackNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(applicationInfo.loadLabel(packageManager))
            .setContentText("Media playback")
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "wta_media_session"
        const val NOTIFICATION_ID = 3100

        private const val ACTION_PLAYING =
            "com.webtoapp.media.PLAYING"

        private const val ACTION_PAUSED =
            "com.webtoapp.media.PAUSED"

        private const val ACTION_MEDIA_COMMAND =
            "com.webtoapp.media.COMMAND"

        private const val EXTRA_COMMAND = "command"
        private const val EXTRA_VALUE = "value"

        private const val WAKE_LOCK_TIMEOUT =
            4L * 60L * 60L * 1000L

        fun synchronize(context: Context, isPlaying: Boolean) {
            val intent = Intent(
                context,
                WebMediaPlaybackService::class.java
            ).setAction(
                if (isPlaying) ACTION_PLAYING else ACTION_PAUSED
            )

            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                // targetSdk 34+ may reject background FGS start with
                // ForegroundServiceStartNotAllowedException; fail soft rather than crash.
                android.util.Log.w("WebMediaPlayback", "startForegroundService rejected for media playback", e)
            }
        }

        /**
         * Stops the media playback service if it is running.
         *
         * This deliberately uses [Context.stopService] instead of
         * [ContextCompat.startForegroundService] with a stop action: starting a
         * foreground service only to stop it violates the foreground-service
         * contract (the service must call [android.app.Service.startForeground]
         * within the startup deadline), which crashed generated apps on launch
         * when the initial Media Session state was `none`. [Context.stopService]
         * is a no-op when the service is not running and creates no such
         * obligation.
         */
        fun stop(context: Context) {
            val appContext = context.applicationContext

            appContext
                .getSystemService(NotificationManager::class.java)
                .cancel(NOTIFICATION_ID)

            appContext.stopService(
                Intent(
                    appContext,
                    WebMediaPlaybackService::class.java
                )
            )
        }

        fun commandPendingIntent(
            context: Context,
            command: String,
            value: Double = 0.0
        ): PendingIntent {
            val requestCode = command.hashCode()

            val intent = Intent(
                context,
                WebMediaPlaybackService::class.java
            ).apply {
                action = ACTION_MEDIA_COMMAND
                putExtra(EXTRA_COMMAND, command)
                putExtra(EXTRA_VALUE, value)
            }

            val flags =
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    context,
                    requestCode,
                    intent,
                    flags
                )
            } else {
                PendingIntent.getService(
                    context,
                    requestCode,
                    intent,
                    flags
                )
            }
        }
    }
}
