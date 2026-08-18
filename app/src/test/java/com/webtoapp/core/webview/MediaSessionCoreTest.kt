package com.webtoapp.core.webview

import android.app.Activity
import android.media.session.PlaybackState
import android.os.Looper
import android.os.SystemClock
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * Guards the #593 fix: SystemUI interpolates progress from the last
 * setState anchor, so the core must (a) advance the published position by
 * wall time × rate while playing, (b) freeze at the interpolated position on
 * pause, and (c) never re-anchor for a repeated, unchanged state report.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@LooperMode(LooperMode.Mode.PAUSED)
class MediaSessionCoreTest {

    private lateinit var core: MediaSessionCore
    private val commands = mutableListOf<Pair<String, Double>>()

    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        core = MediaSessionCore(activity) { command, value ->
            commands.add(command to value)
        }
    }

    private fun idle() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun play(duration: Double = 237.0, position: Double = 216.0, rate: Double = 1.0) {
        core.updatePosition(duration, position, rate)
        idle()
        core.updatePlaybackState(PlaybackState.STATE_PLAYING)
        idle()
    }

    @Test
    fun `playing position advances by wall time at rate 1x`() {
        play(duration = 237.0, position = 216.0, rate = 1.0)

        val now = SystemClock.elapsedRealtime()
        val at = core.interpolatedPositionSeconds(now + 5_000)
        assertThat(at).isWithin(0.1).of(221.0)
    }

    @Test
    fun `playing position advances by the reported rate`() {
        play(duration = 300.0, position = 100.0, rate = 2.0)

        val now = SystemClock.elapsedRealtime()
        val at = core.interpolatedPositionSeconds(now + 10_000)
        assertThat(at).isWithin(0.1).of(120.0)
    }

    @Test
    fun `interpolated position clamps to a known duration`() {
        play(duration = 237.0, position = 236.0, rate = 1.0)

        val now = SystemClock.elapsedRealtime()
        val at = core.interpolatedPositionSeconds(now + 30_000)
        assertThat(at).isWithin(0.1).of(237.0)
    }

    @Test
    fun `pausing freezes the interpolated position instead of the stale anchor`() {
        play(duration = 237.0, position = 100.0, rate = 1.0)

        // 5 s of playback pass with no position report from the page.
        Robolectric.getForegroundThreadScheduler().advanceBy(5_000)

        core.updatePlaybackState(PlaybackState.STATE_PAUSED)
        idle()

        val now = SystemClock.elapsedRealtime()
        // Frozen at ~105 s no matter how much later the system asks.
        val at = core.interpolatedPositionSeconds(now + 120_000)
        assertThat(at).isWithin(0.1).of(105.0)
    }

    @Test
    fun `repeated unchanged state reports do not republish or re-anchor`() {
        play(duration = 237.0, position = 100.0, rate = 1.0)

        val publishesAfterPlay = core.playbackStatePublishCount

        repeat(5) {
            core.updatePlaybackState(PlaybackState.STATE_PLAYING)
        }
        idle()

        // Heartbeat-style duplicates are dropped: no native re-anchor that
        // would drag the SystemUI progress bar back to the stale position.
        assertThat(core.playbackStatePublishCount).isEqualTo(publishesAfterPlay)
    }

    @Test
    fun `position report while playing re-anchors and republishes`() {
        play(duration = 237.0, position = 100.0, rate = 1.0)

        val publishesBefore = core.playbackStatePublishCount

        core.updatePosition(237.0, 150.0, 1.0)
        idle()

        assertThat(core.playbackStatePublishCount).isEqualTo(publishesBefore + 1)

        val now = SystemClock.elapsedRealtime()
        assertThat(core.interpolatedPositionSeconds(now + 2_000))
            .isWithin(0.1).of(152.0)
    }

    @Test
    fun `seek while paused publishes the new position without advancing`() {
        play(duration = 237.0, position = 100.0, rate = 1.0)

        core.updatePlaybackState(PlaybackState.STATE_PAUSED)
        idle()

        core.updatePosition(237.0, 42.0, 1.0)
        idle()

        val now = SystemClock.elapsedRealtime()
        assertThat(core.interpolatedPositionSeconds(now + 60_000))
            .isWithin(0.1).of(42.0)
    }
}
