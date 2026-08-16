package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Guards the contract of [MediaSessionBridge.INJECTION_SCRIPT] — the script runs
 * once per frame (document start, wildcard origin), so it must identify its frame,
 * stay silent when the frame owns no media, and never let DOM-derived values fight
 * the page's own setPositionState() updates (#566).
 */
class MediaSessionBridgeScriptTest {

    private val script = MediaSessionBridge.INJECTION_SCRIPT

    @Test
    fun `script is guarded against re-injection within a frame`() {
        val guardIdx = script.indexOf("window.__wtaMediaBridgeInstalled")
        assertThat(guardIdx).isGreaterThan(0)
        val heartbeatIdx = script.indexOf("__wtaMediaHeartbeat")
        assertThat(heartbeatIdx).isGreaterThan(guardIdx)
    }

    @Test
    fun `every frame carries a stable frame id passed to all native updates`() {
        assertThat(script).contains("window.__wtaMediaFrameId")
        assertThat(script).containsMatch("artwork,\\s*frameId")
        assertThat(script).containsMatch("state,\\s*frameId,\\s*audible")
        assertThat(script).containsMatch("playbackRate,\\s*frameId")
        assertThat(script).containsMatch("\"none\",\\s*frameId,\\s*true")
    }

    @Test
    fun `muted elements never claim the session away from the real player`() {
        assertThat(script).contains("!element.muted && element.volume > 0")
        // The audible flag is derived and forwarded on every state report.
        val audibleIdx = script.indexOf("const audible =")
        val stateIdx = script.indexOf("state,")
        assertThat(audibleIdx).isGreaterThan(0)
        assertThat(stateIdx).isGreaterThan(audibleIdx)
    }

    @Test
    fun `frames without media metadata or active playback stay silent`() {
        // The silence check must run before any native bridge call inside
        // synchronize() — a media-less frame must not even report zeros/none.
        val silenceIdx = script.indexOf("if (!frameOwnsMedia()) return;")
        val syncIdx = script.indexOf("function synchronize()")
        assertThat(silenceIdx).isGreaterThan(syncIdx)
        assertThat(script).doesNotContain("updatePosition(0, 0, 1)")
    }

    @Test
    fun `document title fallback only applies to the frame owning the media element`() {
        val fallbackIdx = script.indexOf("(element ? document.title : \"\")")
        assertThat(fallbackIdx).isGreaterThan(0)
        // And it is what gets sent to the bridge, not a bare document.title.
        assertThat(script).doesNotContain("metadata?.title || document.title ||")
    }

    @Test
    fun `heartbeat never overwrites the page's own setPositionState values`() {
        // Once the page calls setPositionState, the site owns progress: the
        // interceptor sets the flag and forwards the values itself.
        val flagIdx = script.indexOf("siteManagesPosition = true")
        val guardIdx = script.indexOf("if (siteManagesPosition)")
        assertThat(flagIdx).isGreaterThan(0)
        assertThat(guardIdx).isGreaterThan(0)
        // The guard must be inside sendPosition, before any nativeBridge call there.
        val sendPosIdx = script.indexOf("function sendPosition")
        assertThat(guardIdx).isGreaterThan(sendPosIdx)
    }

    @Test
    fun `active media selection prefers playing and audible elements`() {
        // A muted auto-playing clip must not win over the real player.
        assertThat(script).contains("function mediaScore")
        assertThat(script).contains("!element.muted && element.volume > 0")
    }

    @Test
    fun `synchronize sends position before playback state`() {
        val sendPosCall = script.indexOf("sendPosition(element);")
        val sendStateCall = script.indexOf("sendPlaybackState(element);")
        assertThat(sendPosCall).isGreaterThan(0)
        assertThat(sendStateCall).isGreaterThan(sendPosCall)
    }

    @Test
    fun `media metadata polyfill stays available for sites`() {
        assertThat(script).contains("window.MediaMetadata = class MediaMetadata")
        assertThat(script).contains("navigator.mediaSession")
    }

    @Test
    fun `navigator dot mediaSession itself is polyfilled when WebView omits it`() {
        // WebView does not expose the Web Media Session API, so plain
        // `navigator.mediaSession.metadata = ...` assignments would throw or be
        // skipped and site track info would never reach the bridge (#566).
        assertThat(script).contains("hadNativeSession")
        assertThat(script).containsMatch("Object\\.defineProperty\\(\\s*navigator")
        assertThat(script).containsMatch("set metadata\\(value\\)")
        assertThat(script).containsMatch("set playbackState\\(value\\)")
    }
}
