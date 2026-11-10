package com.webtoapp.ui.shell

internal class ToolbarReveal(
    private val hideThresholdPx: Int = 24,
    private val showThresholdPx: Int = 8
) {
    private var lastY = 0
    private var climb = 0

    var visible: Boolean = true
        private set

    fun onScroll(y: Int): Boolean {
        val previous = visible
        val dy = y - lastY
        lastY = y
        when {
            y <= 0 -> {
                climb = 0
                visible = true
            }
            dy > 0 -> {
                climb += dy
                if (climb >= hideThresholdPx) {
                    climb = 0
                    visible = false
                }
            }
            dy < 0 -> {
                climb = 0
                if (-dy >= showThresholdPx) visible = true
            }
        }
        return visible != previous
    }

    fun reset() {
        lastY = 0
        climb = 0
        visible = true
    }
}
