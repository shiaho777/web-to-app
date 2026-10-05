package com.webtoapp.core.bgm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.util.SafeNotificationChannels

/**
 * Native media notification for background music. The session token is what
 * the shade and the lock screen use for pause, skip, and scrub.
 */
internal class BgmMediaNotifier(
    context: Context,
    private val transport: BgmTransport
) {
    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val notifications =
        appContext.getSystemService(NotificationManager::class.java)
    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY -> transport.play()
                ACTION_PAUSE -> transport.pause()
                ACTION_NEXT -> transport.next()
                ACTION_PREV -> transport.previous()
            }
        }
    }

    private val session = MediaSession(appContext, TAG).apply {
        setCallback(object : MediaSession.Callback() {
            override fun onPlay() {
                main.post { transport.play() }
            }

            override fun onPause() {
                main.post { transport.pause() }
            }

            override fun onSkipToNext() {
                main.post { transport.next() }
            }

            override fun onSkipToPrevious() {
                main.post { transport.previous() }
            }

            override fun onSeekTo(pos: Long) {
                main.post { transport.seek(pos) }
            }
        }, main)
        setSessionActivity(launchIntent())
        isActive = false
    }

    private var released = false

    init {
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREV)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            appContext.registerReceiver(commandReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(commandReceiver, filter)
        }
    }

    fun publish(title: String, playing: Boolean, positionMs: Long, durationMs: Long) {
        if (released || notifications == null) return
        session.isActive = true
        val safeTitle = title.ifBlank { Strings.bgmNotificationChannel }
        val state = if (playing) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        val actions = PlaybackState.ACTION_PLAY or
            PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_PLAY_PAUSE or
            PlaybackState.ACTION_SKIP_TO_NEXT or
            PlaybackState.ACTION_SKIP_TO_PREVIOUS or
            PlaybackState.ACTION_SEEK_TO
        session.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, safeTitle)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, Strings.bgmNotificationChannel)
                .putLong(
                    MediaMetadata.METADATA_KEY_DURATION,
                    durationMs.coerceAtLeast(0L)
                )
                .build()
        )
        session.setPlaybackState(
            PlaybackState.Builder()
                .setActions(actions)
                .setState(state, positionMs.coerceAtLeast(0L), if (playing) 1f else 0f, SystemClock.elapsedRealtime())
                .build()
        )
        SafeNotificationChannels.ensure(
            context = appContext,
            id = CHANNEL_ID,
            name = Strings.bgmNotificationChannel,
            importance = NotificationManager.IMPORTANCE_LOW
        )
        val pauseLabel = if (playing) Strings.pause else Strings.play
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(safeTitle)
            .setContentText(Strings.bgmNotificationChannel)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(playing)
            .setContentIntent(launchIntent())
            .addAction(android.R.drawable.ic_media_previous, Strings.bgmPrevious, commandIntent(ACTION_PREV))
            .addAction(
                if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                pauseLabel,
                commandIntent(if (playing) ACTION_PAUSE else ACTION_PLAY)
            )
            .addAction(android.R.drawable.ic_media_next, Strings.bgmNext, commandIntent(ACTION_NEXT))
            .setStyle(
                MediaStyle()
                    .setMediaSession(MediaSessionCompat.Token.fromToken(session.sessionToken))
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
        try {
            notifications.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            AppLogger.w(TAG, "BGM notification blocked: ${e.message}")
        }
    }

    fun hide() {
        if (released) return
        session.isActive = false
        session.setPlaybackState(
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_NONE, 0L, 0f)
                .build()
        )
        notifications?.cancel(NOTIFICATION_ID)
    }

    fun release() {
        if (released) return
        released = true
        session.isActive = false
        notifications?.cancel(NOTIFICATION_ID)
        runCatching { appContext.unregisterReceiver(commandReceiver) }
        runCatching { session.release() }
    }

    private fun launchIntent(): PendingIntent? {
        val launch = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
            ?: return null
        launch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            appContext,
            21021,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun commandIntent(action: String): PendingIntent {
        val intent = Intent(action).setPackage(appContext.packageName)
        return PendingIntent.getBroadcast(
            appContext,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "BgmMediaNotifier"
        const val CHANNEL_ID = "bgm_player_channel"
        const val NOTIFICATION_ID = 21002
        private const val ACTION_PLAY = "com.webtoapp.bgm.PLAY"
        private const val ACTION_PAUSE = "com.webtoapp.bgm.PAUSE"
        private const val ACTION_NEXT = "com.webtoapp.bgm.NEXT"
        private const val ACTION_PREV = "com.webtoapp.bgm.PREV"
    }
}
