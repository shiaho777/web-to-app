package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackgroundMediaKeepAliveTest {

    @Test
    fun `leaving while audio is playing holds the session`() {
        val keepAlive = playing()
        assertThat(keepAlive.onHostPause()).isTrue()
        assertThat(keepAlive.holding).isTrue()
    }

    @Test
    fun `leaving while nothing is playing does not hold`() {
        val keepAlive = BackgroundMediaKeepAlive().apply { enabled = true }
        assertThat(keepAlive.onHostPause()).isFalse()
        assertThat(keepAlive.holding).isFalse()
    }

    @Test
    fun `the switch off never holds`() {
        val keepAlive = BackgroundMediaKeepAlive()
        keepAlive.onPlayingChanged(true)
        assertThat(keepAlive.onHostPause()).isFalse()
    }

    @Test
    fun `pausing in the background releases the hold`() {
        val keepAlive = playing()
        keepAlive.onHostPause()
        assertThat(keepAlive.onPlayingChanged(false))
            .isEqualTo(BackgroundMediaKeepAlive.Action.RELEASE)
        assertThat(keepAlive.holding).isFalse()
    }

    @Test
    fun `playback that starts in the background arms the hold`() {
        val keepAlive = BackgroundMediaKeepAlive().apply { enabled = true }
        keepAlive.onHostPause()
        assertThat(keepAlive.onPlayingChanged(true))
            .isEqualTo(BackgroundMediaKeepAlive.Action.ARM)
        assertThat(keepAlive.holding).isTrue()
    }

    @Test
    fun `returning to the app clears the hold`() {
        val keepAlive = playing()
        keepAlive.onHostPause()
        keepAlive.onHostResume()
        assertThat(keepAlive.holding).isFalse()
        assertThat(keepAlive.isHostPaused).isFalse()
        assertThat(keepAlive.onPlayingChanged(false))
            .isEqualTo(BackgroundMediaKeepAlive.Action.NONE)
    }

    private fun playing(): BackgroundMediaKeepAlive =
        BackgroundMediaKeepAlive().apply {
            enabled = true
            onPlayingChanged(true)
        }
}
