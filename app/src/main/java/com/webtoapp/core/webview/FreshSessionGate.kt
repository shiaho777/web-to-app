package com.webtoapp.core.webview

import android.os.Handler
import android.os.Looper
import java.util.Collections

/**
 * Holds the first navigation of a fresh-session launch until an in-flight
 * cookie / storage clear has finished.
 *
 * [android.webkit.CookieManager.removeAllCookies] delivers its result on the
 * looper of the caller. Blocking that looper to "wait" deadlocks, so the
 * page load registers here and runs when the last ticket closes. A timeout
 * closes an abandoned ticket so a WebView that never calls back cannot leave
 * the app on a blank window.
 */
internal object FreshSessionGate {

    const val TIMEOUT_MS = 5_000L

    private val tickets = Collections.newSetFromMap(java.util.IdentityHashMap<Any, Boolean>())
    private val waiters = ArrayList<() -> Unit>()

    fun open(armTimeout: Boolean = true): Any {
        val ticket = Any()
        synchronized(this) { tickets.add(ticket) }
        if (armTimeout) armTimeout(ticket)
        return ticket
    }

    fun close(ticket: Any) {
        val ready = synchronized(this) {
            val removed = tickets.remove(ticket)
            if (!removed || tickets.isNotEmpty()) {
                emptyList()
            } else {
                val copy = waiters.toList()
                waiters.clear()
                copy
            }
        }
        ready.forEach { waiter ->
            try {
                waiter()
            } catch (t: Throwable) {
                android.util.Log.w("FreshSessionGate", "Fresh-session navigation failed", t)
            }
        }
    }

    fun runWhenReady(block: () -> Unit) {
        val runNow = synchronized(this) {
            if (tickets.isEmpty()) {
                true
            } else {
                waiters.add(block)
                false
            }
        }
        if (runNow) block()
    }

    internal fun resetForTest() {
        synchronized(this) {
            tickets.clear()
            waiters.clear()
        }
    }

    internal fun pendingForTest(): Int = synchronized(this) { tickets.size }

    private fun armTimeout(ticket: Any) {
        try {
            val looper = Looper.getMainLooper() ?: return
            Handler(looper).postDelayed({ close(ticket) }, TIMEOUT_MS)
        } catch (_: Throwable) {
            // No Android main looper (unit tests that opted into the timeout).
            // Leave the ticket to its real completion callback.
        }
    }
}
