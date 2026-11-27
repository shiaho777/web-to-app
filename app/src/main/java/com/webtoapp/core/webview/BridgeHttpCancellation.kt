package com.webtoapp.core.webview

import okhttp3.Call
import java.io.Closeable

internal class BridgeHttpCancellation : Closeable {
    private val lock = Any()
    private var call: Call? = null
    private var closed = false

    fun attach(call: Call): Boolean {
        synchronized(lock) {
            if (!closed) {
                this.call = call
                return true
            }
        }
        call.cancel()
        return false
    }

    fun isClosed(): Boolean = synchronized(lock) { closed }

    override fun close() {
        val call = synchronized(lock) {
            closed = true
            this.call.also { this.call = null }
        }
        call?.cancel()
    }
}
