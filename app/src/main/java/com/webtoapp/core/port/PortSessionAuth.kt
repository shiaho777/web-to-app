package com.webtoapp.core.port

import java.security.SecureRandom

/**
 * Session-token auth for the cross-app port channel.
 *
 * The query/release receivers must stay exported — generated APKs are signed
 * with independent per-app keys, so a signature permission would sever the
 * legitimate Port Manager flow. With no pre-shared secret available, the
 * release path instead requires a short-lived, single-use token that is only
 * handed out through the query channel: a caller must complete the
 * query → token → release handshake rather than firing one blind broadcast.
 *
 * This is deliberately documented as *raising the bar*, not cryptographic
 * authentication: a determined attacker can implement the same handshake.
 * What it removes is the trivially-exploitable surface — an `am broadcast`
 * one-shot, an action-name collision, or any client that does not know the
 * protocol can no longer kill runtime processes from outside the app.
 */
object PortSessionAuth {

    private const val TTL_MS = 60_000L
    private const val MAX_TOKENS = 32

    private val random = SecureRandom()
    private val lock = Any()

    /** token -> issuedAt, insertion-ordered for bounded eviction. */
    private val issued = LinkedHashMap<String, Long>()

    /**
     * Mint a fresh single-use token. Only the query receiver calls this —
     * the token reaches the caller through the ordered-broadcast result
     * extras, which a passive observer cannot read.
     */
    fun issue(now: Long = System.currentTimeMillis()): String {
        synchronized(lock) {
            purgeExpiredLocked(now)
            val token = ByteArray(24).also { random.nextBytes(it) }
                .joinToString("") { "%02x".format(it) }
            issued[token] = now
            while (issued.size > MAX_TOKENS) {
                issued.remove(issued.entries.first().key)
            }
            return token
        }
    }

    /**
     * Consume [token]: true exactly once per issued, unexpired token. The
     * single-use semantics bound a leaked token to a single release call.
     */
    fun consume(token: String?, now: Long = System.currentTimeMillis()): Boolean {
        if (token.isNullOrBlank()) return false
        synchronized(lock) {
            purgeExpiredLocked(now)
            return issued.remove(token) != null
        }
    }

    /** Test/maintenance hook: drop all outstanding tokens. */
    fun clear() {
        synchronized(lock) { issued.clear() }
    }

    private fun purgeExpiredLocked(now: Long) {
        val it = issued.entries.iterator()
        while (it.hasNext()) {
            if (now - it.next().value > TTL_MS) it.remove()
        }
    }
}
