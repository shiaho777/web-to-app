package com.webtoapp.core.engine

import android.app.Activity
import android.media.session.PlaybackState
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.webview.MediaSessionCore
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.MediaSession as GeckoMediaSession

/**
 * Feeds GeckoView's native Web Media Session events into the shared
 * [MediaSessionCore] and routes native media commands back to the page (#593).
 *
 * GeckoView implements the web Media Session API itself (no JS polyfill
 * needed, no frame arbitration — the engine reports the page-level session),
 * so this adapter is a pure event/command translation: onPlay/onPause/onStop
 * map to native playback states, `Metadata`/`PositionState` map to core
 * metadata/position (which interpolates progress between position reports),
 * `Feature` flags gate optional actions, and commands map to
 * [GeckoMediaSession] controller calls.
 */
class GeckoMediaSessionAdapter(
    activity: Activity,
    private val engine: GeckoViewEngine
) {

    private val core = MediaSessionCore(activity, ::dispatchCommand)

    /** Controller handle from delegate callbacks; null while inactive. */
    private var controller: GeckoMediaSession? = null

    private val delegate = object : GeckoMediaSession.Delegate {
        override fun onActivated(
            session: GeckoSession,
            mediaSession: GeckoMediaSession
        ) {
            controller = mediaSession
        }

        override fun onDeactivated(
            session: GeckoSession,
            mediaSession: GeckoMediaSession
        ) {
            controller = null
            core.clearPlayback()
        }

        override fun onMetadata(
            session: GeckoSession,
            mediaSession: GeckoMediaSession,
            meta: GeckoMediaSession.Metadata
        ) {
            controller = mediaSession
            core.updateMetadata(meta.title, meta.artist, meta.album, "")

            // Image.getBitmap decodes asynchronously off the main thread.
            meta.artwork?.getBitmap(ARTWORK_SIZE)?.accept(
                { bitmap -> core.updateArtwork(bitmap) },
                { error ->
                    AppLogger.w(
                        TAG,
                        "media session artwork decode failed: ${error?.message}"
                    )
                    core.updateArtwork(null)
                }
            )
        }

        override fun onFeatures(
            session: GeckoSession,
            mediaSession: GeckoMediaSession,
            features: Long
        ) {
            controller = mediaSession
            core.setActionSupported(
                "previoustrack",
                features and GeckoMediaSession.Feature.PREVIOUS_TRACK != 0L
            )
            core.setActionSupported(
                "nexttrack",
                features and GeckoMediaSession.Feature.NEXT_TRACK != 0L
            )
            core.setActionSupported(
                "seekbackward",
                features and GeckoMediaSession.Feature.SEEK_BACKWARD != 0L
            )
            core.setActionSupported(
                "seekforward",
                features and GeckoMediaSession.Feature.SEEK_FORWARD != 0L
            )
        }

        override fun onPlay(
            session: GeckoSession,
            mediaSession: GeckoMediaSession
        ) {
            controller = mediaSession
            core.updatePlaybackState(PlaybackState.STATE_PLAYING)
        }

        override fun onPause(
            session: GeckoSession,
            mediaSession: GeckoMediaSession
        ) {
            controller = mediaSession
            core.updatePlaybackState(PlaybackState.STATE_PAUSED)
        }

        override fun onStop(
            session: GeckoSession,
            mediaSession: GeckoMediaSession
        ) {
            controller = mediaSession
            core.updatePlaybackState(PlaybackState.STATE_NONE)
        }

        override fun onPositionState(
            session: GeckoSession,
            mediaSession: GeckoMediaSession,
            positionState: GeckoMediaSession.PositionState
        ) {
            controller = mediaSession
            core.updatePosition(
                positionState.duration,
                positionState.position,
                positionState.playbackRate
            )
        }
    }

    init {
        engine.setMediaSessionDelegate(delegate)
        AppLogger.i(TAG, "Gecko media session adapter attached")
    }

    fun release() {
        engine.setMediaSessionDelegate(null)
        core.release()
        controller = null
    }

    private fun dispatchCommand(command: String, value: Double) {
        val target = controller ?: return
        when (command) {
            "play" -> target.play()
            "pause" -> target.pause()
            "stop" -> target.stop()
            "seekto" -> target.seekTo(value, false)
            "previoustrack" -> target.previousTrack()
            "nexttrack" -> target.nextTrack()
            "seekbackward" -> target.seekBackward()
            "seekforward" -> target.seekForward()
        }
    }

    private companion object {
        const val TAG = "GeckoMediaSession"

        /** Lock-screen artwork target; SystemUI scales as needed. */
        const val ARTWORK_SIZE = 512
    }
}
