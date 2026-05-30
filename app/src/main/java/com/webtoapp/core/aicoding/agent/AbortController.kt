package com.webtoapp.core.aicoding.agent

import java.util.concurrent.atomic.AtomicBoolean

class AbortController {
    private val flag = AtomicBoolean(false)

    @Volatile private var onAbort: (() -> Unit)? = null

    val aborted: Boolean get() = flag.get()

    fun abort() {
        if (flag.compareAndSet(false, true)) {
            try { onAbort?.invoke() } catch (_: Throwable) {  }
        }
    }

    fun registerCloseHandler(block: () -> Unit) {
        onAbort = block
    }

    fun clearCloseHandler() {
        onAbort = null
    }
}

class AgentAbortedException : kotlinx.coroutines.CancellationException("Agent turn aborted")
