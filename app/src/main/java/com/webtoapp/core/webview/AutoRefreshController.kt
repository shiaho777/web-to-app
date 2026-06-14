package com.webtoapp.core.webview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AutoRefreshController(
    private val intervalSec: Int,
    private val showCountdown: Boolean,
    private val onReload: () -> Unit
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tickerJob: Job? = null

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    val countdownVisible: Boolean get() = showCountdown

    fun start() {
        stop()
        if (intervalSec <= 0) return
        _remainingSeconds.value = intervalSec
        tickerJob = scope.launch {
            while (isActive) {
                delay(1000)
                val next = _remainingSeconds.value - 1
                if (next <= 0) {
                    _remainingSeconds.value = intervalSec
                    onReload()
                } else {
                    _remainingSeconds.value = next
                }
            }
        }
    }

    fun skipCurrentRound() {
        _remainingSeconds.value = intervalSec
    }

    fun pauseBriefly(seconds: Int = 10) {
        val target = intervalSec.coerceAtLeast(seconds + 1)
        if (_remainingSeconds.value < target) {
            _remainingSeconds.value = target
        }
    }

    fun stop() {
        tickerJob?.cancel()
        tickerJob = null
        _remainingSeconds.value = 0
    }
}
