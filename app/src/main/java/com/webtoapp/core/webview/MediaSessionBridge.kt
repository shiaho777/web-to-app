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
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.webtoapp.core.logging.AppLogger
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * Bridges the web [Media Session API](https://developer.mozilla.org/en-US/docs/Web/API/Media_Session_API)
 * (`navigator.mediaSession`) to a native Android [MediaSession].
 *
 * Audio is NOT transferred — it keeps playing inside the WebView. This bridge only
 * carries metadata, position and control commands between the page and the system
 * media controls (notification, lock screen, Bluetooth, Android Auto), backed by
 * [WebMediaPlaybackService] for background playback and wake lock.
 *
 * The bridge constructor already registers the `WtaMediaSession` JavaScript
 * interface; callers only need to install [INJECTION_SCRIPT] at document start
 * (plus [injectNow] from `onPageFinished` as a fallback).
 */
class MediaSessionBridge(
    private val activity: Activity,
    private val webView: WebView
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

    private var playbackState = PlaybackState.STATE_NONE

    /**
     * Identity of the frame that currently owns the session. The injected script
     * runs once per frame (document-start, wildcard origin), so every frame would
     * otherwise write metadata/state/position into this single bridge and the last
     * writer would win — a muted ad iframe could clobber the real player (#566).
     * Only a frame reporting `playing`/`buffering` claims ownership; its updates
     * stay authoritative while fresh, others are dropped.
     */
    private var activeFrameId: String? = null
    private var activeFrameLastSeenMs = 0L

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

    @Volatile
    private var lastNotification: Notification? = null

    init {
        activeBridge = WeakReference(this)

        webView.addJavascriptInterface(
            this,
            JAVASCRIPT_INTERFACE_NAME
        )

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
                    dispatchCommand("play")
                }

                override fun onPause() {
                    dispatchCommand("pause")
                }

                override fun onStop() {
                    dispatchCommand("stop")
                }

                override fun onSeekTo(position: Long) {
                    dispatchCommand(
                        "seekto",
                        position.toDouble() / 1000.0
                    )
                }

                override fun onSkipToPrevious() {
                    dispatchCommand("previoustrack")
                }

                override fun onSkipToNext() {
                    dispatchCommand("nexttrack")
                }

                override fun onRewind() {
                    dispatchCommand("seekbackward", 10.0)
                }

                override fun onFastForward() {
                    dispatchCommand("seekforward", 10.0)
                }
            }
        )
    }

    /**
     * Install this script using WebViewCompat.addDocumentStartJavaScript().
     *
     * Also call injectNow() from onPageFinished() as a fallback for pages that
     * were already loaded before the document-start script was registered.
     */
    fun injectNow() {
        webView.post {
            webView.evaluateJavascript(INJECTION_SCRIPT, null)
        }
    }

    @JavascriptInterface
    fun updateMetadata(
        newTitle: String?,
        newArtist: String?,
        newAlbum: String?,
        newArtworkUrl: String?,
        frameId: String?
    ) {
        mainHandler.post {
            if (!isAuthoritative(frameId)) {
                return@post
            }

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

    @JavascriptInterface
    fun updatePlaybackState(
        state: String?,
        frameId: String?,
        audible: Boolean
    ) {
        mainHandler.post {
            val resolved = when (state?.lowercase()) {
                "playing" -> PlaybackState.STATE_PLAYING
                "paused" -> PlaybackState.STATE_PAUSED
                "buffering" -> PlaybackState.STATE_BUFFERING
                "none", "stopped", null -> PlaybackState.STATE_NONE
                else -> PlaybackState.STATE_PAUSED
            }

            /*
             * Only an audible, actively playing frame claims session ownership.
             * A muted autoplaying clip (ad/preview) may still report state while
             * no fresh owner exists, but never steals the session from the real
             * player (#566). Frames without an element (WebAudio players with
             * an explicit playbackState) report as audible.
             */
            if (resolved == PlaybackState.STATE_PLAYING ||
                resolved == PlaybackState.STATE_BUFFERING
            ) {
                if (audible) {
                    activeFrameId = frameId
                    activeFrameLastSeenMs = SystemClock.elapsedRealtime()
                }
            } else if (!isAuthoritative(frameId)) {
                return@post
            }

            playbackState = resolved

            if (resolved != PlaybackState.STATE_NONE) {
                cancelTeardown()
            }

            publish()
        }
    }

    @JavascriptInterface
    fun updatePosition(
        duration: Double,
        position: Double,
        rate: Double,
        frameId: String?
    ) {
        mainHandler.post {
            if (!isAuthoritative(frameId)) {
                return@post
            }

            if (frameId != null && frameId == activeFrameId) {
                activeFrameLastSeenMs = SystemClock.elapsedRealtime()
            }

            val durationChanged =
                abs(durationSeconds - duration) >= 0.5

            durationSeconds =
                duration.takeIf { it.isFinite() && it > 0.0 } ?: 0.0

            positionSeconds =
                position.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0

            playbackRate =
                rate.takeIf { it.isFinite() && it > 0.0 }
                    ?.toFloat()
                    ?: 1.0f

            /*
             * Playback state must be updated regularly for progress tracking.
             * Metadata is also republished when duration changes so the
             * duration is never stuck at 0 when metadata arrived first.
             */
            applyPlaybackState()

            if (durationChanged) {
                applyMetadata()
            }

            updateNotification()
        }
    }

    @JavascriptInterface
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

    // ---- State publishing ----

    /**
     * A frame owns its updates while it is the active frame and has reported
     * within [FRAME_TAKEOVER_MS]. Frames that never played only get through
     * while no fresh owner exists.
     */
    private fun isAuthoritative(frameId: String?): Boolean {
        val current = activeFrameId ?: return true
        if (frameId == null || frameId == current) return true
        return SystemClock.elapsedRealtime() - activeFrameLastSeenMs > FRAME_TAKEOVER_MS
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

        val state = PlaybackState.Builder()
            .setActions(actions)
            .setState(
                playbackState,
                (positionSeconds * 1000.0).toLong(),
                playbackRate
            )
            .build()

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

    // ---- Android → JS commands ----

    private fun dispatchCommand(
        command: String,
        value: Double = 0.0
    ) {
        val escapedCommand = JSONObject.quote(command)

        val script = """
            window.__wtaMediaCommand &&
            window.__wtaMediaCommand($escapedCommand, $value);
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(script, null)
        }
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

    private fun clearPlayback() {
        cancelTeardown()

        hasActivePlaybackSession = false

        activeFrameId = null
        activeFrameLastSeenMs = 0L

        playbackState = PlaybackState.STATE_NONE
        positionSeconds = 0.0
        durationSeconds = 0.0

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

        try {
            webView.removeJavascriptInterface(
                JAVASCRIPT_INTERFACE_NAME
            )
        } catch (_: Exception) {
            // WebView may already be destroyed.
        }

        artworkExecutor.shutdownNow()
        mediaSession.release()

        if (activeBridge?.get() === this) {
            activeBridge = null
        }
    }

    companion object {
        const val JAVASCRIPT_INTERFACE_NAME =
            "WtaMediaSession"

        /**
         * How long the active frame's reports stay authoritative after the
         * last one, and how long a `none` report must survive before the
         * session is torn down.
         */
        private const val FRAME_TAKEOVER_MS = 3_000L
        private const val TEARDOWN_GRACE_MS = 2_500L

        @Volatile
        private var activeBridge:
            WeakReference<MediaSessionBridge>? = null

        internal fun dispatchFromService(
            command: String,
            value: Double
        ) {
            activeBridge?.get()?.dispatchCommand(
                command,
                value
            )
        }

        internal fun getForegroundNotification(
            context: Context
        ): Notification? {
            return activeBridge?.get()?.lastNotification
        }

        /** The document-start JS polyfill that hooks navigator.mediaSession. */
        fun getInjectionScript(): String = INJECTION_SCRIPT

        val INJECTION_SCRIPT = """
            (() => {
              if (window.__wtaMediaBridgeInstalled) return;
              window.__wtaMediaBridgeInstalled = true;

              const nativeBridge = window.WtaMediaSession;

              if (!nativeBridge) return;

              /*
               * This script runs once per frame (document start, wildcard
               * origin). Every frame gets a stable id so the native bridge can
               * arbitrate when several frames report at once (#566).
               */
              const frameId =
                window.__wtaMediaFrameId ||
                (window.__wtaMediaFrameId =
                  "f" + Math.random().toString(36).slice(2, 10));

              const registeredHandlers = Object.create(null);
              const boundElements = new WeakSet();

              const hadNativeSession =
                !!(navigator.mediaSession && navigator.mediaSession);

              /*
               * True once the page itself called setPositionState(); such a
               * site manages progress itself, so the heartbeat must not
               * overwrite its values with DOM-derived ones.
               */
              let siteManagesPosition = false;

              let metadataValue = null;
              let playbackStateValue = "none";

              function notifyChange() {
                try {
                  synchronize();
                } catch (_) {}
              }

              // WebView has no native MediaMetadata constructor; sites that do
              // `navigator.mediaSession.metadata = new MediaMetadata({...})`
              // would throw ReferenceError. Provide a compatible constructor —
              // metadata is read as plain properties by sendMetadata() below.
              if (typeof window.MediaMetadata === "undefined") {
                window.MediaMetadata = class MediaMetadata {
                  constructor(options = {}) {
                    this.title = options.title || "";
                    this.artist = options.artist || "";
                    this.album = options.album || "";
                    this.artwork = Array.isArray(options.artwork)
                      ? options.artwork.map(art => ({
                          src: (art && art.src) || "",
                          sizes: (art && art.sizes) || "",
                          type: (art && art.type) || ""
                        }))
                      : [];
                  }
                };
              }

              // WebView does not expose the Web Media Session API at all, so
              // every `navigator.mediaSession.metadata = ...` assignment a
              // site makes would throw (or be skipped by feature checks) and
              // the bridge could never see site-provided track info (#566).
              // Polyfill it with getters/setters that feed the bridge.
              if (!hadNativeSession) {
                const polyfillSession = {
                  get metadata() {
                    return metadataValue;
                  },
                  set metadata(value) {
                    metadataValue = value || null;
                    notifyChange();
                  },
                  get playbackState() {
                    return playbackStateValue;
                  },
                  set playbackState(value) {
                    playbackStateValue =
                      value && value !== "none"
                        ? String(value)
                        : "none";
                    notifyChange();
                  },
                  setActionHandler: function(action, handler) {
                    const isFunction =
                      typeof handler === "function";

                    registeredHandlers[action] =
                      isFunction ? handler : null;

                    nativeBridge.setActionSupported(
                      action,
                      isFunction
                    );
                  },
                  setPositionState: function(state) {
                    if (!state) return;

                    siteManagesPosition = true;

                    nativeBridge.updatePosition(
                      Number(state.duration) || 0,
                      Number(state.position) || 0,
                      Number(state.playbackRate) || 1,
                      frameId
                    );
                  },
                  setMicrophoneActive: function() {},
                  setCameraActive: function() {}
                };

                try {
                  Object.defineProperty(
                    navigator,
                    "mediaSession",
                    {
                      value: polyfillSession,
                      configurable: true,
                      writable: true
                    }
                  );
                } catch (_) {}
              }

              const mediaSession =
                navigator.mediaSession || null;

              // Call/camera state APIs (edge case): keep them callable so sites
              // using them do not crash; state is not surfaced to the system UI.
              if (mediaSession && typeof mediaSession.setMicrophoneActive !== "function") {
                mediaSession.setMicrophoneActive = function() {};
              }
              if (mediaSession && typeof mediaSession.setCameraActive !== "function") {
                mediaSession.setCameraActive = function() {};
              }

              function absoluteUrl(value) {
                if (!value) return "";

                try {
                  return new URL(value, document.baseURI).href;
                } catch (_) {
                  return String(value);
                }
              }

              function collectMedia(root, output) {
                if (!root || !root.querySelectorAll) return;

                root.querySelectorAll("audio, video").forEach(element => {
                  output.push(element);
                });

                root.querySelectorAll("*").forEach(element => {
                  if (element.shadowRoot) {
                    collectMedia(element.shadowRoot, output);
                  }
                });
              }

              function getMediaElements() {
                const output = [];
                collectMedia(document, output);
                return output;
              }

              /*
               * Playing beats paused-with-progress beats idle; audible beats
               * muted. Keeps a muted 10-second ad clip from winning over the
               * real player inside the same frame.
               */
              function mediaScore(element) {
                let score = 0;

                if (
                  !element.paused &&
                  !element.ended &&
                  element.readyState > 0
                ) {
                  score += 4;
                } else if (
                  element.currentTime > 0 &&
                  !element.ended
                ) {
                  score += 2;
                }

                if (!element.muted && element.volume > 0) {
                  score += 1;
                }

                return score;
              }

              function getActiveMedia() {
                const elements = getMediaElements();

                let best = null;
                let bestScore = -1;

                elements.forEach(element => {
                  const score = mediaScore(element);
                  if (score > bestScore) {
                    best = element;
                    bestScore = score;
                  }
                });

                return best;
              }

              /*
               * Frames without media elements, without page metadata and
               * without an active playback state must stay completely silent —
               * reporting zeros or "none" from such a frame used to wipe the
               * real player's session (#566).
               */
              function frameOwnsMedia() {
                if (getMediaElements().length > 0) return true;

                if (mediaSession && mediaSession.metadata) return true;

                const reported =
                  mediaSession && mediaSession.playbackState;

                return (
                  !!reported &&
                  reported !== "none"
                );
              }

              function sendMetadata(element) {
                const metadata = mediaSession?.metadata;

                let artwork = "";

                if (metadata?.artwork?.length) {
                  const candidates = Array.from(metadata.artwork);

                  candidates.sort((a, b) => {
                    const aSize =
                      parseInt(String(a.sizes || "0"), 10) || 0;

                    const bSize =
                      parseInt(String(b.sizes || "0"), 10) || 0;

                    return bSize - aSize;
                  });

                  artwork = absoluteUrl(
                    candidates[0]?.src || ""
                  );
                }

                // document.title is only a sane fallback for the frame that
                // actually owns the playing element.
                const fallbackTitle =
                  metadata?.title || (element ? document.title : "");

                nativeBridge.updateMetadata(
                  fallbackTitle,
                  metadata?.artist || "",
                  metadata?.album || "",
                  artwork,
                  frameId
                );
              }

              function sendPosition(element) {
                if (siteManagesPosition) {
                  // setPositionState() already forwarded the site's own
                  // values; DOM-derived ones would only fight them.
                  return;
                }

                if (!element) {
                  return;
                }

                const duration =
                  Number.isFinite(element.duration)
                    ? element.duration
                    : 0;

                const position =
                  Number.isFinite(element.currentTime)
                    ? element.currentTime
                    : 0;

                const playbackRate =
                  Number.isFinite(element.playbackRate) &&
                  element.playbackRate > 0
                    ? element.playbackRate
                    : 1;

                nativeBridge.updatePosition(
                  duration,
                  position,
                  playbackRate,
                  frameId
                );
              }

              function sendPlaybackState(element) {
                let state = mediaSession?.playbackState;

                if (!state || state === "none") {
                  if (!element) {
                    state = "none";
                  } else if (
                    !element.paused &&
                    !element.ended
                  ) {
                    state = "playing";
                  } else if (
                    element.currentTime > 0 &&
                    !element.ended
                  ) {
                    state = "paused";
                  } else {
                    state = "none";
                  }
                }

                // Frames without an element (WebAudio players driving an
                // explicit playbackState) count as audible; muted clips never
                // claim the session away from the real player.
                const audible =
                  !element ||
                  (!element.muted && element.volume > 0);

                nativeBridge.updatePlaybackState(
                  state,
                  frameId,
                  audible
                );
              }

              function synchronize() {
                if (!frameOwnsMedia()) return;

                const element = getActiveMedia();

                // Position first so a state change publishes fresh values;
                // state last so publish() applies metadata and position.
                sendPosition(element);
                sendMetadata(element);
                sendPlaybackState(element);

                getMediaElements().forEach(bindElement);
              }

              function bindElement(element) {
                if (!element || boundElements.has(element)) {
                  return;
                }

                boundElements.add(element);

                [
                  "play",
                  "playing",
                  "pause",
                  "ended",
                  "emptied",
                  "loadedmetadata",
                  "durationchange",
                  "ratechange",
                  "timeupdate",
                  "waiting",
                  "stalled"
                ].forEach(eventName => {
                  element.addEventListener(
                    eventName,
                    synchronize,
                    { passive: true }
                  );
                });
              }

              async function callHandler(
                action,
                value
              ) {
                const handler =
                  registeredHandlers[action];

                if (!handler) {
                  return false;
                }

                let details = { action };

                if (action === "seekto") {
                  details = {
                    action,
                    seekTime: Number(value) || 0,
                    fastSeek: false
                  };
                } else if (
                  action === "seekforward" ||
                  action === "seekbackward"
                ) {
                  details = {
                    action,
                    seekOffset:
                      Number(value) || 10
                  };
                }

                try {
                  await handler(details);
                  return true;
                } catch (error) {
                  console.error(
                    "Media Session handler failed:",
                    action,
                    error
                  );

                  return false;
                }
              }

              async function fallbackCommand(
                action,
                value
              ) {
                const element = getActiveMedia();

                if (!element) return;

                switch (action) {
                  case "play":
                    try {
                      await element.play();
                    } catch (_) {}
                    break;

                  case "pause":
                    element.pause();
                    break;

                  case "stop":
                    element.pause();

                    try {
                      element.currentTime = 0;
                    } catch (_) {}
                    break;

                  case "seekto":
                    if (Number.isFinite(Number(value))) {
                      try {
                        element.currentTime =
                          Number(value);
                      } catch (_) {}
                    }
                    break;

                  case "seekforward":
                    try {
                      element.currentTime = Math.min(
                        Number.isFinite(element.duration)
                          ? element.duration
                          : element.currentTime + 10,
                        element.currentTime +
                          (Number(value) || 10)
                      );
                    } catch (_) {}
                    break;

                  case "seekbackward":
                    try {
                      element.currentTime = Math.max(
                        0,
                        element.currentTime -
                          (Number(value) || 10)
                      );
                    } catch (_) {}
                    break;
                }
              }

              window.__wtaMediaCommand =
                async function(action, value) {
                  const handled =
                    await callHandler(action, value);

                  if (!handled) {
                    await fallbackCommand(
                      action,
                      value
                    );
                  }

                  setTimeout(synchronize, 50);
                };

              if (
                hadNativeSession &&
                mediaSession &&
                typeof mediaSession.setActionHandler ===
                  "function"
              ) {
                const originalSetActionHandler =
                  mediaSession.setActionHandler.bind(
                    mediaSession
                  );

                mediaSession.setActionHandler =
                  function(action, handler) {
                    registeredHandlers[action] =
                      typeof handler === "function"
                        ? handler
                        : null;

                    nativeBridge.setActionSupported(
                      action,
                      typeof handler === "function"
                    );

                    return originalSetActionHandler(
                      action,
                      handler
                    );
                  };
              }

              if (
                hadNativeSession &&
                mediaSession &&
                typeof mediaSession.setPositionState ===
                  "function"
              ) {
                const originalSetPositionState =
                  mediaSession.setPositionState.bind(
                    mediaSession
                  );

                mediaSession.setPositionState =
                  function(state) {
                    if (state) {
                      siteManagesPosition = true;

                      nativeBridge.updatePosition(
                        Number(state.duration) || 0,
                        Number(state.position) || 0,
                        Number(state.playbackRate) || 1,
                        frameId
                      );
                    }

                    return originalSetPositionState(
                      state
                    );
                  };
              }

              const observer = new MutationObserver(() => {
                getMediaElements().forEach(bindElement);
              });

              observer.observe(
                document.documentElement || document,
                {
                  childList: true,
                  subtree: true
                }
              );

              getMediaElements().forEach(bindElement);

              /*
               * A one-second heartbeat keeps lock-screen progress accurate and
               * detects metadata changes made by the website.
               */
              window.__wtaMediaHeartbeat =
                window.setInterval(
                  synchronize,
                  1000
                );

              window.addEventListener(
                "pagehide",
                event => {
                  if (!event.persisted) {
                    nativeBridge.updatePlaybackState(
                      "none",
                      frameId,
                      true
                    );
                  }
                }
              );

              synchronize();
            })();
        """.trimIndent()
    }
}
