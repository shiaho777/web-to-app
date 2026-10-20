package com.webtoapp.core.ai

import com.google.gson.JsonParser
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiKeyConfig
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap

/**
 * How widely a 429 applies.
 * [MODEL] leaves the key usable for other models. [KEY] benches the whole key.
 */
enum class LimitScope { MODEL, KEY }

/** Classified HTTP failure from a provider call. */
data class RatedFailure(
    val message: String,
    val recoverable: Boolean,
    val httpCode: Int,
    val retryAfterMs: Long?,
    val limitScope: LimitScope?
)

/**
 * In-memory pool of sibling API keys (same provider and base URL).
 *
 * A rate-limited secret is benched until the cooldown from the response
 * elapses, then becomes eligible again. Cooldowns are keyed by the secret
 * itself, so editing the saved key starts fresh and two rows that share a
 * secret share one quota. State lives for the process; a restart clears it.
 */
class ApiKeyRotation {

    private val modelUntil = ConcurrentHashMap<String, Long>()
    private val keyUntil = ConcurrentHashMap<String, Long>()
    private val dead = ConcurrentHashMap.newKeySet<String>()

    /** What the caller should do after one failed attempt. */
    sealed class KeyDecision {
        data class Switch(val key: ApiKeyConfig, val masked: String) : KeyDecision()
        data class Backoff(val waitMs: Long, val sameKeyRetries: Int) : KeyDecision()
        data object Fail : KeyDecision()
    }

    /**
     * First secret that can serve [modelId] now. [bound] wins when it is free,
     * so a healthy session stays on the key the model was saved with.
     */
    fun select(
        bound: ApiKeyConfig,
        pool: List<ApiKeyConfig>,
        modelId: String,
        now: Long = System.currentTimeMillis()
    ): ApiKeyConfig? {
        val peers = pool.ifEmpty { listOf(bound) }
        return peers
            .sortedBy { if (it.id == bound.id) 0 else 1 }
            .firstOrNull { available(it, modelId, now) }
    }

    /** Millis until the soonest benched (not rejected) peer is free. */
    fun soonestWaitMs(
        bound: ApiKeyConfig,
        pool: List<ApiKeyConfig>,
        modelId: String,
        now: Long = System.currentTimeMillis()
    ): Long? {
        var best: Long? = null
        for (key in pool.ifEmpty { listOf(bound) }) {
            if (stamp(key) in dead) continue
            val until = cooldownUntil(key, modelId)
            if (until > now) {
                val wait = until - now
                if (best == null || wait < best) best = wait
            }
        }
        return best
    }

    fun onFailure(
        current: ApiKeyConfig,
        bound: ApiKeyConfig,
        pool: List<ApiKeyConfig>,
        modelId: String,
        httpCode: Int?,
        retryAfterMs: Long?,
        limitScope: LimitScope?,
        recoverable: Boolean,
        sameKeyRetries: Int,
        now: Long = System.currentTimeMillis()
    ): KeyDecision {
        val peers = pool.ifEmpty { listOf(bound) }
        val rotatable = httpCode == 401 || httpCode == 403 || httpCode == 429 || limitScope != null
        if (!rotatable) {
            if (!recoverable || sameKeyRetries >= MAX_SAME_KEY_RETRIES) return KeyDecision.Fail
            val wait = (retryAfterMs ?: (1_000L shl sameKeyRetries.coerceAtMost(4)))
                .coerceIn(0L, MAX_INLINE_WAIT_MS)
            return KeyDecision.Backoff(wait, sameKeyRetries + 1)
        }

        when (httpCode) {
            401 -> dead.add(stamp(current))
            403 -> bench(
                current,
                modelId,
                LimitScope.KEY,
                retryAfterMs ?: FORBIDDEN_COOLDOWN_MS,
                now
            )
            else -> bench(
                current,
                modelId,
                limitScope ?: LimitScope.MODEL,
                retryAfterMs ?: DEFAULT_COOLDOWN_MS,
                now
            )
        }

        val next = select(bound, peers, modelId, now)
        if (next != null && stamp(next) != stamp(current)) {
            return KeyDecision.Switch(next, mask(next))
        }
        val wait = soonestWaitMs(bound, peers, modelId, now)
        if (wait != null && wait <= MAX_INLINE_WAIT_MS && sameKeyRetries < MAX_SAME_KEY_RETRIES) {
            return KeyDecision.Backoff(wait, sameKeyRetries + 1)
        }
        return KeyDecision.Fail
    }

    private fun available(key: ApiKeyConfig, modelId: String, now: Long): Boolean {
        if (stamp(key) in dead) return false
        return cooldownUntil(key, modelId) <= now
    }

    private fun cooldownUntil(key: ApiKeyConfig, modelId: String): Long {
        val id = stamp(key)
        val keyU = keyUntil[id] ?: 0L
        val modelU = modelUntil[id + "\u0000" + modelId] ?: 0L
        return maxOf(keyU, modelU)
    }

    private fun bench(
        key: ApiKeyConfig,
        modelId: String,
        scope: LimitScope,
        retryAfterMs: Long,
        now: Long
    ) {
        val until = now + retryAfterMs.coerceAtLeast(MIN_COOLDOWN_MS)
        val id = stamp(key)
        when (scope) {
            LimitScope.KEY -> keyUntil.merge(id, until) { left, right -> maxOf(left, right) }
            LimitScope.MODEL -> modelUntil.merge(id + "\u0000" + modelId, until) { left, right -> maxOf(left, right) }
        }
    }

    companion object {
        val shared = ApiKeyRotation()

        const val MAX_SAME_KEY_RETRIES = 5
        const val MAX_INLINE_WAIT_MS = 60_000L
        const val DEFAULT_COOLDOWN_MS = 60_000L
        const val MIN_COOLDOWN_MS = 1_000L

        /** A daily quota with no usable delay. Long enough that we do not hop back. */
        const val DAILY_COOLDOWN_MS = 6L * 60L * 60L * 1000L
        const val FORBIDDEN_COOLDOWN_MS = 10L * 60L * 1000L

        private val RETRY_DELAY_STRING = Regex(""""retryDelay"\s*:\s*"([^"]+)"""")
        private val RETRY_DELAY_SECONDS = Regex(
            """"retryDelay"\s*:\s*\{\s*"seconds"\s*:\s*"?(\d+(?:\.\d+)?)"""
        )
        private val RETRY_IN = Regex(
            """retry in\s+(\d+(?:\.\d+)?)\s*(ms|s|m|h)""",
            RegexOption.IGNORE_CASE
        )
        private val DURATION = Regex("""^(\d+(?:\.\d+)?)(ms|s|m|h)$""", RegexOption.IGNORE_CASE)

        /**
         * Keys that can stand in for [bound]: same provider, same base URL,
         * and (for a custom endpoint) the same wire format. [bound] stays first.
         */
        fun poolFor(bound: ApiKeyConfig, all: List<ApiKeyConfig>): List<ApiKeyConfig> {
            val peers = all.filter { other ->
                other.id != bound.id && samePool(bound, other) && usable(other)
            }.sortedBy { it.createdAt }
            val head = if (usable(bound)) listOf(bound) else emptyList()
            return (head + peers).distinctBy { it.id }.ifEmpty { listOf(bound) }
        }

        fun splitSecrets(raw: String): List<String> =
            raw.split(',', '\n', '\r')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()

        fun mask(key: ApiKeyConfig): String {
            val raw = key.apiKey.trim()
            if (raw.length < 4) return "****"
            return "****${raw.takeLast(4)}"
        }

        fun describe(
            code: Int,
            body: String,
            retryAfterHeader: String?,
            retryAfterMsHeader: String? = null
        ): RatedFailure {
            val message = messageOf(code, body)
            val recoverable = recoverableOf(code, body)
            val parsed = if (code == 429) {
                cooldownFrom(retryAfterHeader, retryAfterMsHeader, body)
            } else {
                parseRetryAfterHeader(retryAfterHeader)
                    ?: retryAfterMsHeader?.trim()?.toLongOrNull()?.takeIf { it >= 0 }
            }
            val retryAfter = when (code) {
                429 -> parsed ?: if (looksDaily(body)) DAILY_COOLDOWN_MS else DEFAULT_COOLDOWN_MS
                else -> parsed
            }
            val scope = if (code == 429) limitScopeOf(body) else null
            return RatedFailure(message, recoverable, code, retryAfter, scope)
        }

        private fun usable(key: ApiKeyConfig): Boolean {
            if (!key.isActive) return false
            if (key.provider.requiresApiKey && key.apiKey.isBlank()) return false
            return true
        }

        private fun samePool(a: ApiKeyConfig, b: ApiKeyConfig): Boolean {
            if (a.provider != b.provider) return false
            if (baseKey(a) != baseKey(b)) return false
            if (a.provider == AiProvider.CUSTOM && a.apiFormat != b.apiFormat) return false
            return true
        }

        private fun baseKey(key: ApiKeyConfig): String {
            val raw = key.baseUrl?.trim()?.trimEnd('/')
            val chosen = if (!raw.isNullOrEmpty()) raw else key.provider.baseUrl.trim().trimEnd('/')
            return chosen.lowercase()
        }

        private fun stamp(key: ApiKeyConfig): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(key.apiKey.trim().toByteArray(Charsets.UTF_8))
            val hex = digest.joinToString("") { "%02x".format(it) }
            return key.provider.name + "|" + baseKey(key) + "|" + hex
        }

        private fun messageOf(code: Int, body: String): String {
            val lower = body.lowercase()
            val toolRelated = "tool" in lower || "function" in lower
            return when (code) {
                400 -> "Bad request (400): ${body.take(300)}"
                401 -> "API key invalid or expired (401)"
                403 -> "Access denied (403)"
                404 -> {
                    val detail = body.take(300).trim()
                    if (detail.isEmpty()) "Endpoint or model not found (404)"
                    else "Endpoint or model not found (404): $detail"
                }
                429 -> {
                    val detail = providerMessage(body)?.take(300)?.trim().orEmpty()
                    if (detail.isEmpty()) "Rate limited (429)" else "Rate limited (429): $detail"
                }
                in 500..599 -> "Server error ($code)"
                else -> {
                    if (code == 400 && toolRelated) "Bad request (400): ${body.take(300)}"
                    else "Request failed ($code): ${body.take(300)}"
                }
            }
        }

        private fun recoverableOf(code: Int, body: String): Boolean {
            val lower = body.lowercase()
            val toolRelated = "tool" in lower || "function" in lower
            return code == 429 || code == 500 || code == 502 || code == 503 || code == 504 ||
                (code == 400 && toolRelated)
        }

        private fun providerMessage(body: String): String? {
            val root = runCatching { JsonParser.parseString(body).asJsonObject }.getOrNull()
                ?: return null
            val err = root.get("error") ?: root.get("message")
            if (err == null || err.isJsonNull) return null
            if (err.isJsonPrimitive) return err.asString.takeIf { it.isNotBlank() }
            if (!err.isJsonObject) return null
            val obj = err.asJsonObject
            val message = obj.get("message")?.takeIf { it.isJsonPrimitive && !it.isJsonNull }?.asString
            return message?.takeIf { it.isNotBlank() }
        }

        private fun looksDaily(body: String): Boolean {
            val lower = body.lowercase()
            return "perday" in lower || "per_day" in lower || "per-day" in lower ||
                "daily" in lower || "rpd" in lower
        }

        /**
         * Per-model quota stays on that model. A project-wide quota (no model
         * dimension) benches every model on the key.
         */
        fun limitScopeOf(body: String): LimitScope {
            val lower = body.lowercase()
            val perModel = "permodel" in lower || "per_model" in lower || "per-model" in lower ||
                ("quotadimensions" in lower && Regex(""""model"\s*:""").containsMatchIn(lower))
            if (perModel) return LimitScope.MODEL
            val perProject = "perproject" in lower || "per_project" in lower || "per-project" in lower
            if (perProject) return LimitScope.KEY
            return LimitScope.MODEL
        }

        private fun cooldownFrom(
            retryAfterHeader: String?,
            retryAfterMsHeader: String?,
            body: String
        ): Long? {
            parseRetryAfterHeader(retryAfterHeader)?.let { return it }
            retryAfterMsHeader?.trim()?.toLongOrNull()?.takeIf { it >= 0 }?.let { return it }
            RETRY_DELAY_STRING.find(body)?.let { parseDuration(it.groupValues[1]) }?.let { return it }
            RETRY_DELAY_SECONDS.find(body)?.let {
                return (it.groupValues[1].toDouble() * 1000).toLong()
            }
            RETRY_IN.find(body)?.let {
                parseDuration(it.groupValues[1] + it.groupValues[2])
            }?.let { return it }
            return null
        }

        fun parseRetryAfterHeader(value: String?): Long? {
            if (value.isNullOrBlank()) return null
            val trimmed = value.trim()
            trimmed.toLongOrNull()?.let { if (it >= 0) return it * 1000 }
            return runCatching {
                val fmt = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
                fmt.timeZone = TimeZone.getTimeZone("GMT")
                val whenMs = fmt.parse(trimmed)?.time ?: return null
                (whenMs - System.currentTimeMillis()).coerceAtLeast(0)
            }.getOrNull()
        }

        fun parseDuration(token: String): Long? {
            val match = DURATION.matchEntire(token.trim()) ?: return null
            val n = match.groupValues[1].toDouble()
            val mult = when (match.groupValues[2].lowercase()) {
                "ms" -> 1.0
                "s" -> 1_000.0
                "m" -> 60_000.0
                "h" -> 3_600_000.0
                else -> return null
            }
            return (n * mult).toLong()
        }
    }
}
