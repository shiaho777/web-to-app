package com.webtoapp.core.webview

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Engine-agnostic native [MediaSession] state machine shared by the WebView
 * bridge (JS polyfill, [MediaSessionBridge]) and the GeckoView adapter
 * (Gecko's own media session events).
 *
 * Position continuity (#593): SystemUI interpolates progress from the last
 * `setState(position, speed)` anchor, but pages that manage position via
 * `setPositionState()` only report on play/pause/seek — never per second.
 * The core therefore anchors every reported position with
 * [lastPositionUpdateMs] and re-publishes *interpolated* positions while
 * playing, so heartbeat republishes can never drag the bar backwards, and
 * identical state reports are deduplicated instead of re-anchoring the
 * SystemUI baseline to a stale position.
 */
class MediaSessionCore(
    private val activity: Activity,
    private val onCommand: (command: String, value: Double) -> Unit
) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val artworkExecutor = Executors.newSingleThreadExecutor()

    private val notificationManager =
        activity.getSystemService(NotificationManager::class.java)

    private val mediaSession = MediaSession(
        activity,
        "WebToAppMediaSession"
    )

    private var title = ""
    private var artist = ""
    private var album = ""
    private var artworkUrl = ""

    private var artworkBitmap: Bitmap? = null
    private var loadedArtworkUrl = ""

    private var durationSeconds = 0.0
    private var positionSeconds = 0.0
    private var playbackRate = 1.0f

    /** elapsedRealtime anchor of [positionSeconds]; 0 while unknown. */
    private var lastPositionUpdateMs = 0L

    private var playbackState = PlaybackState.STATE_NONE

    /**
     * `true` once any non-`none` playback state has ever been reported. Used to
     * guard [clearPlayback] so the initial `STATE_NONE` event (emitted during
     * page load before any media exists) does not run full media-session
     * teardown — which would otherwise stop the playback service on every cold
     * start.
     */
    private var hasActivePlaybackSession = false

    private var teardownRunnable: Runnable? = null

    private val supportedActions = mutableSetOf<String>()

    /** Number of native setPlaybackState() publishes; test hook for dedup. */
    internal var playbackStatePublishCount = 0
        private set

    @Volatile
    private var lastNotification: Notification? = null

    init {
        activeCore = WeakReference(this)

        createLaunchPendingIntent()?.let { pendingIntent ->
            /*
             * Opens or returns to the app when the user taps the
             * notification, lock-screen media card, or system media player.
             */
            mediaSession.setSessionActivity(pendingIntent)
        }

        mediaSession.setCallback(
            object : MediaSession.Callback() {
                override fun onPlay() {
                    onCommand("play", 0.0)
                }

                override fun onPause() {
                    onCommand("pause", 0.0)
                }

                override fun onStop() {
                    onCommand("stop", 0.0)
                }

                override fun onSeekTo(position: Long) {
                    onCommand(
                        "seekto",
                        position.toDouble() / 1000.0
                    )
                }

                override fun onSkipToPrevious() {
                    onCommand("previoustrack", 0.0)
                }

                override fun onSkipToNext() {
                    onCommand("nexttrack", 0.0)
                }

                override fun onRewind() {
                    onCommand("seekbackward", 10.0)
                }

                override fun onFastForward() {
                    onCommand("seekforward", 10.0)
                }
            }
        )
    }

    // ---- State intake (all posts land on the main thread) ----

    fun updateMetadata(
        newTitle: String?,
        newArtist: String?,
        newAlbum: String?,
        newArtworkUrl: String?
    ) {
        mainHandler.post {
            val incomingBlank =
                newTitle.isNullOrBlank() &&
                    newArtist.isNullOrBlank() &&
                    newAlbum.isNullOrBlank() &&
                    newArtworkUrl.isNullOrBlank()

            val currentBlank =
                title.isBlank() &&
                    artist.isBlank() &&
                    album.isBlank() &&
                    artworkUrl.isBlank()

            /*
             * A frame without media metadata used to send ("", "", "", "")
             * every second, wiping real track info and falling back to the
             * app label. Empty payloads never replace known metadata.
             */
            if (incomingBlank && !currentBlank) {
                return@post
            }

            cancelTeardown()

            title = newTitle.orEmpty()
            artist = newArtist.orEmpty()
            album = newAlbum.orEmpty()

            val normalizedArtwork = newArtworkUrl.orEmpty()

            if (artworkUrl != normalizedArtwork) {
                artworkUrl = normalizedArtwork
                loadArtwork(normalizedArtwork)
            }

            publish()
        }
    }

    fun updatePlaybackState(state: Int) {
        mainHandler.post { applyPlaybackStateEvent(state) }
    }

    fun updatePosition(
        duration: Double,
        position: Double,
        rate: Double
    ) {
        mainHandler.post {
            val durationChanged =
                kotlin.math.abs(durationSeconds - duration) >= 0.5

            durationSeconds =
                duration.takeIf { it.isFinite() && it > 0.0 } ?: 0.0

            positionSeconds =
                position.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0

            playbackRate =
                rate.takeIf { it.isFinite() && it > 0.0 }
                    ?.toFloat()
                    ?: 1.0f

            /*
             * Every authoritative position report re-anchors the
             * interpolation baseline; applyPlaybackState() then publishes the
             * position computed from this anchor (identical to the report
             * itself at this instant).
             */
            lastPositionUpdateMs = SystemClock.elapsedRealtime()

            applyPlaybackState()

            if (durationChanged) {
                applyMetadata()
            }
        }
    }

    fun setActionSupported(
        action: String?,
        supported: Boolean
    ) {
        if (action.isNullOrBlank()) {
            return
        }

        mainHandler.post {
            if (supported) {
                supportedActions.add(action)
            } else {
                supportedActions.remove(action)
            }

            /*
             * Apply immediately instead of waiting for another playback event.
             */
            applyPlaybackState()
            updateNotification()
        }
    }

    /**
     * Engine-provided artwork (GeckoView decodes its `Image` itself). URL
     * bookkeeping is cleared so a later URL-based load is not mistaken for
     * "already loaded".
     */
    fun updateArtwork(bitmap: Bitmap?) {
        mainHandler.post {
            artworkBitmap = bitmap
            loadedArtworkUrl = ""

            if (playbackState != PlaybackState.STATE_NONE) {
                applyMetadata()
                updateNotification()
            }
        }
    }

    // ---- State machine ----

    /**
     * Position as of [nowMs], advanced by wall time × rate while playing and
     * clamped to a known duration. This is what every native publish uses, so
     * a heartbeat re-publish never drags the SystemUI bar backwards (#593).
     */
    internal fun interpolatedPositionSeconds(
        nowMs: Long = SystemClock.elapsedRealtime()
    ): Double {
        if (playbackState != PlaybackState.STATE_PLAYING ||
            lastPositionUpdateMs <= 0L
        ) {
            return positionSeconds
        }

        val elapsedSeconds =
            ((nowMs - lastPositionUpdateMs).coerceAtLeast(0L)) / 1000.0

        val advanced = positionSeconds + elapsedSeconds * playbackRate

        return if (durationSeconds > 0.0) {
            kotlin.math.min(advanced, durationSeconds)
        } else {
            advanced
        }
    }

    private fun applyPlaybackStateEvent(state: Int) {
        /*
         * Dedup: engines report state repeatedly (the WebView heartbeat sends
         * "playing" once per second; Gecko re-fires on activity). Republishing
         * an unchanged state would re-anchor SystemUI's interpolation to a
         * stale position and visibly freeze the progress bar (#593).
         */
        if (state == playbackState) {
            return
        }

        if (playbackState == PlaybackState.STATE_PLAYING &&
            state != PlaybackState.STATE_PLAYING
        ) {
            // Leaving playback: freeze at the interpolated "now" position so
            // the pause anchor equals where the user actually heard the track.
            positionSeconds = interpolatedPositionSeconds()
            lastPositionUpdateMs = SystemClock.elapsedRealtime()
        } else if (state == PlaybackState.STATE_PLAYING) {
            // Entering playback: keep the (possibly frozen) position but
            // re-anchor, so interpolation does not charge the paused interval.
            lastPositionUpdateMs = SystemClock.elapsedRealtime()
        }

        playbackState = state

        publish()
    }

    private fun cancelTeardown() {
        teardownRunnable?.let { runnable ->
            mainHandler.removeCallbacks(runnable)
        }
        teardownRunnable = null
    }

    private fun scheduleTeardown() {
        cancelTeardown()

        val runnable = Runnable {
            teardownRunnable = null

            if (playbackState == PlaybackState.STATE_NONE &&
                (
                    hasActivePlaybackSession ||
                        mediaSession.isActive ||
                        lastNotification != null
                    )
            ) {
                clearPlayback()
            }
        }

        teardownRunnable = runnable
        mainHandler.postDelayed(runnable, TEARDOWN_GRACE_MS)
    }

    private fun publish() {
        if (playbackState == PlaybackState.STATE_NONE) {
            /*
             * "none" arrives from any frame whose media ended (or that never
             * had any). Tearing down immediately let one ended ad element kill
             * a session another frame was still playing (#566); wait out the
             * grace period and cancel if anything reports activity again.
             */
            if (hasActivePlaybackSession || mediaSession.isActive || lastNotification != null) {
                scheduleTeardown()
            }
            return
        }

        cancelTeardown()

        hasActivePlaybackSession = true

        applyMetadata()
        applyPlaybackState()

        mediaSession.isActive = true

        updateNotification()

        WebMediaPlaybackService.synchronize(
            activity.applicationContext,
            playbackState == PlaybackState.STATE_PLAYING
        )
    }

    private fun applyMetadata() {
        val metadata = MediaMetadata.Builder()
            .putString(
                MediaMetadata.METADATA_KEY_TITLE,
                title.ifBlank { appLabel() }
            )
            .putString(
                MediaMetadata.METADATA_KEY_ARTIST,
                artist
            )
            .putString(
                MediaMetadata.METADATA_KEY_ALBUM,
                album
            )
            .putLong(
                MediaMetadata.METADATA_KEY_DURATION,
                (durationSeconds * 1000.0).toLong()
            )

        artworkBitmap?.let { bitmap ->
            metadata.putBitmap(
                MediaMetadata.METADATA_KEY_ART,
                bitmap
            )

            metadata.putBitmap(
                MediaMetadata.METADATA_KEY_ALBUM_ART,
                bitmap
            )

            metadata.putBitmap(
                MediaMetadata.METADATA_KEY_DISPLAY_ICON,
                bitmap
            )
        }

        mediaSession.setMetadata(metadata.build())
    }

    private fun applyPlaybackState() {
        var actions =
            PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_STOP or
                PlaybackState.ACTION_SEEK_TO

        if ("previoustrack" in supportedActions) {
            actions = actions or PlaybackState.ACTION_SKIP_TO_PREVIOUS
        }

        if ("nexttrack" in supportedActions) {
            actions = actions or PlaybackState.ACTION_SKIP_TO_NEXT
        }

        if ("seekbackward" in supportedActions) {
            actions = actions or PlaybackState.ACTION_REWIND
        }

        if ("seekforward" in supportedActions) {
            actions = actions or PlaybackState.ACTION_FAST_FORWARD
        }

        val isPlaying = playbackState == PlaybackState.STATE_PLAYING

        /*
         * Speed 0 while not playing stops SystemUI interpolation; the
         * position carried into setState is already the interpolated "now"
         * value, so the anchor is always continuous with what the user sees.
         */
        val state = PlaybackState.Builder()
            .setActions(actions)
            .setState(
                playbackState,
                (interpolatedPositionSeconds() * 1000.0).toLong(),
                if (isPlaying) playbackRate else 0.0f
            )
            .build()

        playbackStatePublishCount++

        mediaSession.setPlaybackState(state)
    }

    // ---- Notification ----

    private fun updateNotification() {
        if (playbackState == PlaybackState.STATE_NONE) {
            notificationManager.cancel(
                WebMediaPlaybackService.NOTIFICATION_ID
            )

            lastNotification = null
            return
        }

        val notification = buildNotification()

        lastNotification = notification

        notificationManager.notify(
            WebMediaPlaybackService.NOTIFICATION_ID,
            notification
        )
    }

    private fun buildNotification(): Notification {
        val builder = NotificationCompat.Builder(
            activity,
            WebMediaPlaybackService.CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title.ifBlank { appLabel() })
            .setContentText(
                artist.ifBlank {
                    album.ifBlank { "Media playback" }
                }
            )
            .setSubText(album.takeIf { it.isNotBlank() })
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(
                playbackState == PlaybackState.STATE_PLAYING
            )
            .setContentIntent(createLaunchPendingIntent())
            .setDeleteIntent(
                WebMediaPlaybackService.commandPendingIntent(
                    activity,
                    "stop"
                )
            )

        artworkBitmap?.let(builder::setLargeIcon)

        val compactActionIndexes = mutableListOf<Int>()
        var actionIndex = 0

        if ("previoustrack" in supportedActions) {
            builder.addAction(
                android.R.drawable.ic_media_previous,
                "Previous",
                WebMediaPlaybackService.commandPendingIntent(
                    activity,
                    "previoustrack"
                )
            )

            compactActionIndexes.add(actionIndex)
            actionIndex++
        }

        val isPlaying =
            playbackState == PlaybackState.STATE_PLAYING

        builder.addAction(
            if (isPlaying) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            },
            if (isPlaying) "Pause" else "Play",
            WebMediaPlaybackService.commandPendingIntent(
                activity,
                if (isPlaying) "pause" else "play"
            )
        )

        compactActionIndexes.add(actionIndex)
        actionIndex++

        if ("nexttrack" in supportedActions) {
            builder.addAction(
                android.R.drawable.ic_media_next,
                "Next",
                WebMediaPlaybackService.commandPendingIntent(
                    activity,
                    "nexttrack"
                )
            )

            compactActionIndexes.add(actionIndex)
        }

        val mediaStyle = MediaStyle()
            .setMediaSession(
                MediaSessionCompat.Token.fromToken(mediaSession.sessionToken)
            )
            .setShowCancelButton(true)
            .setCancelButtonIntent(
                WebMediaPlaybackService.commandPendingIntent(
                    activity,
                    "stop"
                )
            )

        if (compactActionIndexes.isNotEmpty()) {
            mediaStyle.setShowActionsInCompactView(
                *compactActionIndexes
                    .take(3)
                    .toIntArray()
            )
        }

        builder.setStyle(mediaStyle)

        return builder.build()
    }

    private fun createLaunchPendingIntent(): PendingIntent? {
        val launchIntent =
            activity.packageManager.getLaunchIntentForPackage(
                activity.packageName
            ) ?: return null

        launchIntent.addFlags(
            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        )

        return PendingIntent.getActivity(
            activity,
            3101,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ---- Artwork ----

    private fun loadArtwork(url: String) {
        if (url.isBlank()) {
            artworkBitmap = null
            loadedArtworkUrl = ""
            publish()
            return
        }

        if (url == loadedArtworkUrl && artworkBitmap != null) {
            return
        }

        artworkExecutor.execute {
            val bitmap = downloadBitmap(url)

            mainHandler.post {
                /*
                 * Ignore an old request when the website changed tracks while
                 * the image was still downloading.
                 */
                if (artworkUrl != url) {
                    return@post
                }

                artworkBitmap = bitmap
                loadedArtworkUrl = url

                if (playbackState != PlaybackState.STATE_NONE) {
                    applyMetadata()
                    updateNotification()
                }
            }
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
                as HttpURLConnection

            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.instanceFollowRedirects = true
            connection.doInput = true
            connection.connect()

            connection.inputStream.use {
                BitmapFactory.decodeStream(it)
            }.also {
                connection.disconnect()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun appLabel(): String {
        return activity.applicationInfo
            .loadLabel(activity.packageManager)
            .toString()
    }

    fun clearPlayback() {
        cancelTeardown()

        hasActivePlaybackSession = false

        playbackState = PlaybackState.STATE_NONE
        positionSeconds = 0.0
        durationSeconds = 0.0
        lastPositionUpdateMs = 0L

        mediaSession.setPlaybackState(
            PlaybackState.Builder()
                .setState(
                    PlaybackState.STATE_NONE,
                    0L,
                    1.0f
                )
                .build()
        )

        mediaSession.isActive = false

        notificationManager.cancel(
            WebMediaPlaybackService.NOTIFICATION_ID
        )

        lastNotification = null

        WebMediaPlaybackService.stop(
            activity.applicationContext
        )
    }

    fun release() {
        clearPlayback()

        artworkExecutor.shutdownNow()
        mediaSession.release()

        if (activeCore?.get() === this) {
            activeCore = null
        }
    }

    companion object {
        private const val TEARDOWN_GRACE_MS = 2_500L

        @Volatile
        private var activeCore:
            WeakReference<MediaSessionCore>? = null

        internal fun dispatchFromService(
            command: String,
            value: Double
        ) {
            activeCore?.get()?.onCommand(command, value)
        }

        internal fun getForegroundNotification(
            context: Context
        ): Notification? {
            return activeCore?.get()?.lastNotification
        }
    }
}
