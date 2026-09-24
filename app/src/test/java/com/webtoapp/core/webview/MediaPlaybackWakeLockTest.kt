package com.webtoapp.core.webview

import android.Manifest
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowPowerManager

/**
 * [WebMediaPlaybackService] holds a time-boxed, non-reference-counted wake
 * lock. Every PLAYING sync must renew the timeout — which the lock rotation
 * implements — rather than leaving the original acquire to expire
 * mid-playback.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MediaPlaybackWakeLockTest {

    @Before
    fun grantWakeLock() {
        // WakeLockCompat fails soft when WAKE_LOCK is missing; Robolectric
        // does not auto-grant manifest permissions.
        shadowOf(RuntimeEnvironment.getApplication())
            .grantPermissions(Manifest.permission.WAKE_LOCK)
    }

    private fun intent(action: String) = Intent(
        RuntimeEnvironment.getApplication(),
        WebMediaPlaybackService::class.java
    ).setAction(action)

    @Test
    fun `PLAYING acquires a held wake lock`() {
        val controller = Robolectric.buildService(WebMediaPlaybackService::class.java).create()
        controller.withIntent(intent(ACTION_PLAYING)).startCommand(0, 1)

        val lock = ShadowPowerManager.getLatestWakeLock()
        assertThat(lock).isNotNull()
        assertThat(lock.isHeld).isTrue()
    }

    @Test
    fun `repeated PLAYING rotates to a fresh lock and releases the old one`() {
        val controller = Robolectric.buildService(WebMediaPlaybackService::class.java).create()
        val playing = intent(ACTION_PLAYING)

        controller.withIntent(playing).startCommand(0, 1)
        val first = ShadowPowerManager.getLatestWakeLock()
        assertThat(first.isHeld).isTrue()

        // Second PLAYING report must renew coverage: a new, held lock while
        // the previous one is released (timeout effectively restarted).
        controller.withIntent(playing).startCommand(0, 2)
        val second = ShadowPowerManager.getLatestWakeLock()

        assertThat(second).isNotSameInstanceAs(first)
        assertThat(second.isHeld).isTrue()
        assertThat(first.isHeld).isFalse()
    }

    @Test
    fun `PAUSED releases the wake lock`() {
        val controller = Robolectric.buildService(WebMediaPlaybackService::class.java).create()

        controller.withIntent(intent(ACTION_PLAYING)).startCommand(0, 1)
        val lock = ShadowPowerManager.getLatestWakeLock()
        assertThat(lock.isHeld).isTrue()

        controller.withIntent(intent(ACTION_PAUSED)).startCommand(0, 2)
        assertThat(lock.isHeld).isFalse()
    }

    companion object {
        // Mirror the service's private actions — the test deliberately binds
        // to the wire contract, not the constants.
        private const val ACTION_PLAYING = "com.webtoapp.media.PLAYING"
        private const val ACTION_PAUSED = "com.webtoapp.media.PAUSED"
    }
}
