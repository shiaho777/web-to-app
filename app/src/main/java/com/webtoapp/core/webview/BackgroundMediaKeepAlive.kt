package com.webtoapp.core.webview

/**
 * Whether leaving the activity should leave the page's media running.
 *
 * Holding is only true while the feature is on, the host is in the background,
 * and the page is actually playing. Idle pages keep the normal pause path.
 */
class BackgroundMediaKeepAlive {
    var enabled: Boolean = false

    var isPlaying: Boolean = false
        private set

    var isHostPaused: Boolean = false
        private set

    var holding: Boolean = false
        private set

    fun notePlaying(playing: Boolean) {
        isPlaying = playing
    }

    fun onPlayingChanged(playing: Boolean): Action {
        isPlaying = playing
        return reconcile()
    }

    /** @return true when the caller must not pause the WebView or Gecko session. */
    fun onHostPause(): Boolean {
        isHostPaused = true
        reconcile()
        return holding
    }

    fun onHostResume() {
        isHostPaused = false
        holding = false
    }

    private fun reconcile(): Action {
        val want = enabled && isHostPaused && isPlaying
        if (want == holding) return Action.NONE
        holding = want
        return if (want) Action.ARM else Action.RELEASE
    }

    enum class Action { NONE, ARM, RELEASE }
}
