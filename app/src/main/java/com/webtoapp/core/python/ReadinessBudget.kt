package com.webtoapp.core.python

import java.util.concurrent.atomic.AtomicLong

/**
 * Time budget for server startup readiness polling (#534).
 *
 * A fixed window (the old 30s cutoff) killed servers that were still slowly
 * importing dependencies on flash storage — the Werkzeug bind banner often
 * arrived just after the deadline. Instead the budget is progress-driven:
 *
 *  - a base window measured from the *last observed progress signal* (process
 *    output line, bound socket, probe response), and
 *  - a hard cap measured from start,
 *
 * so an alive-and-progressing server keeps waiting while a silent failure
 * still ends after the base window.
 */
internal class ReadinessBudget(
    private val baseMs: Long,
    private val hardCapMs: Long,
    private val clock: () -> Long = System::currentTimeMillis
) {

    private val startAt = AtomicLong(clock())
    private val lastProgressAt = AtomicLong(clock())

    /** Records a progress signal; resets the base window. */
    fun markProgress() {
        lastProgressAt.set(clock())
    }

    /** True when polling should give up: hard cap reached or progress went silent. */
    fun expired(): Boolean {
        val now = clock()
        return now - startAt.get() >= hardCapMs || now - lastProgressAt.get() >= baseMs
    }

    fun elapsedMs(): Long = clock() - startAt.get()

    fun msSinceProgress(): Long = clock() - lastProgressAt.get()
}
