package com.webtoapp.core.bgm

/**
 * Latest playback actions. The media notification and the floating chip both
 * call into this holder, which the player replaces whenever the session changes.
 */
internal class BgmTransport {
    var play: () -> Unit = {}
    var pause: () -> Unit = {}
    var next: () -> Unit = {}
    var previous: () -> Unit = {}
    var seek: (Long) -> Unit = {}
}

internal enum class BgmPreviousAction { RESTART, TRACK }

/** Past a few seconds, "previous" restarts the current track. */
internal fun bgmPreviousAction(positionMs: Long, restartAfterMs: Long = 3_000L): BgmPreviousAction =
    if (positionMs > restartAfterMs) BgmPreviousAction.RESTART else BgmPreviousAction.TRACK

internal fun bgmManualNextIndex(index: Int, size: Int): Int {
    if (size <= 0) return 0
    return (index + 1) % size
}

internal fun bgmPreviousLinearIndex(index: Int, size: Int): Int {
    if (size <= 0) return 0
    return if (index > 0) index - 1 else size - 1
}
