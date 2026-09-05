package com.webtoapp.core.webview

/**
 * Safety net against app-initiated auto-reload storms (#654 follow-up).
 *
 * Every individual auto-reload path is bounded (loopback/file retries, exhaustible
 * failover cursor), but their counters reset when the URL changes — a login <->
 * dashboard cycle (or any error-navigation-error cycle) can otherwise reload
 * forever while each single path believes it is within budget. This breaker counts
 * app-initiated reloads per host in a sliding window; once tripped, callers must
 * fall through to the error UI instead of scheduling another reload.
 *
 * Only auto-reload entry points consult it — user gestures (pull-to-refresh, back,
 * address-bar loads) are never gated.
 *
 * Pure logic, no Android dependencies: unit-tested on plain JVM.
 */
internal class ReloadCircuitBreaker(
    private val maxReloads: Int = 6,
    private val windowMs: Long = 10_000L,
    private val clockMs: () -> Long = System::currentTimeMillis
) {
    private val lock = Any()
    private val hits = ArrayDeque<Pair<String, Long>>()

    /**
     * Records an app-initiated reload of [url]. Returns true when the reload may
     * proceed, false when the breaker tripped (caller must show error UI instead).
     * Unparseable URLs are always allowed — never break what we cannot attribute.
     */
    fun noteAutoReload(url: String?): Boolean {
        val host = hostOf(url) ?: return true
        val now = clockMs()
        synchronized(lock) {
            while (hits.isNotEmpty() && now - hits.first().second > windowMs) {
                hits.removeFirst()
            }
            if (hits.count { it.first == host } >= maxReloads) return false
            hits.addLast(host to now)
            return true
        }
    }

    internal fun hostOf(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return try {
            java.net.URI(url).host?.lowercase()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
