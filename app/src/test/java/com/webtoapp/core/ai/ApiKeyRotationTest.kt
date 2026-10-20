package com.webtoapp.core.ai

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AiProvider
import com.webtoapp.data.model.ApiFormat
import com.webtoapp.data.model.ApiKeyConfig
import org.junit.Test

class ApiKeyRotationTest {

    private val geminiDaily = """
        {
          "error": {
            "code": 429,
            "message": "You exceeded your current quota.",
            "status": "RESOURCE_EXHAUSTED",
            "details": [
              {
                "@type": "type.googleapis.com/google.rpc.QuotaFailure",
                "violations": [{
                  "quotaId": "GenerateRequestsPerDayPerProjectPerModel-FreeTier",
                  "quotaDimensions": {"model": "gemini-2.0-flash"}
                }]
              },
              {
                "@type": "type.googleapis.com/google.rpc.RetryInfo",
                "retryDelay": "86372s"
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `comma and newline lists become distinct secrets`() {
        assertThat(ApiKeyRotation.splitSecrets("key1, key2,key1\nkey3"))
            .containsExactly("key1", "key2", "key3")
            .inOrder()
        assertThat(ApiKeyRotation.splitSecrets("  ,  \n")).isEmpty()
    }

    @Test
    fun `gemini daily quota is per model and carries the body delay`() {
        val failure = ApiKeyRotation.describe(429, geminiDaily, null)
        assertThat(failure.limitScope).isEqualTo(LimitScope.MODEL)
        assertThat(failure.retryAfterMs).isEqualTo(86_372_000L)
        assertThat(failure.message).contains("exceeded your current quota")
        assertThat(failure.recoverable).isTrue()
    }

    @Test
    fun `retry-after header beats the body and a missing delay falls back`() {
        val withHeader = ApiKeyRotation.describe(429, geminiDaily, "12")
        assertThat(withHeader.retryAfterMs).isEqualTo(12_000L)

        val bare = ApiKeyRotation.describe(429, """{"error":{"message":"slow down"}}""", null)
        assertThat(bare.retryAfterMs).isEqualTo(ApiKeyRotation.DEFAULT_COOLDOWN_MS)
        assertThat(bare.limitScope).isEqualTo(LimitScope.MODEL)

        val project = ApiKeyRotation.describe(
            429,
            """{"error":{"message":"quota","details":[{"quotaId":"GenerateRequestsPerDayPerProject"}]}}""",
            null
        )
        assertThat(project.limitScope).isEqualTo(LimitScope.KEY)
        assertThat(project.retryAfterMs).isEqualTo(ApiKeyRotation.DAILY_COOLDOWN_MS)
    }

    @Test
    fun `retryDelay seconds object and retry-after-ms header parse`() {
        val body = """{"error":{"details":[{"retryDelay":{"seconds":"33"}}]}}"""
        assertThat(ApiKeyRotation.describe(429, body, null).retryAfterMs).isEqualTo(33_000L)
        assertThat(ApiKeyRotation.describe(429, "{}", null, "2500").retryAfterMs).isEqualTo(2_500L)
        assertThat(ApiKeyRotation.parseDuration("1.5m")).isEqualTo(90_000L)
    }

    @Test
    fun `pool keeps the bound key first and drops other providers`() {
        val bound = key("bound", "aaaa-1111", createdAt = 50)
        val older = key("older", "bbbb-2222", createdAt = 10)
        val other = key("openai", "sk-openai", provider = AiProvider.OPENAI)
        val custom = key("custom", "cccc-3333", baseUrl = "https://example.test")
        val inactive = key("off", "dddd-4444", active = false)
        val pool = ApiKeyRotation.poolFor(bound, listOf(older, other, custom, inactive, bound))
        assertThat(pool.map { it.id }).containsExactly("bound", "older").inOrder()
    }

    @Test
    fun `429 on one model switches keys and leaves the key free for another model`() {
        val rotation = ApiKeyRotation()
        val flash = "gemini-2.0-flash"
        val pro = "gemini-2.5-pro"
        val a = key("a", "key-a-1111")
        val b = key("b", "key-b-2222")
        val pool = listOf(a, b)
        val now = 1_000_000L
        val failure = ApiKeyRotation.describe(429, geminiDaily, null)

        val decision = rotation.onFailure(
            current = a,
            bound = a,
            pool = pool,
            modelId = flash,
            httpCode = 429,
            retryAfterMs = failure.retryAfterMs,
            limitScope = failure.limitScope,
            recoverable = true,
            sameKeyRetries = 0,
            now = now
        )
        assertThat(decision).isInstanceOf(ApiKeyRotation.KeyDecision.Switch::class.java)
        assertThat((decision as ApiKeyRotation.KeyDecision.Switch).key.id).isEqualTo("b")
        assertThat(decision.masked).isEqualTo("****2222")

        assertThat(rotation.select(a, pool, flash, now)?.id).isEqualTo("b")
        assertThat(rotation.select(a, pool, pro, now)?.id).isEqualTo("a")
    }

    @Test
    fun `project-wide cooldown benches every model on that secret`() {
        val rotation = ApiKeyRotation()
        val a = key("a", "key-a-1111")
        val now = 5_000L
        val decision = rotation.onFailure(
            current = a,
            bound = a,
            pool = listOf(a),
            modelId = "gemini-2.0-flash",
            httpCode = 429,
            retryAfterMs = ApiKeyRotation.DAILY_COOLDOWN_MS,
            limitScope = LimitScope.KEY,
            recoverable = true,
            sameKeyRetries = 0,
            now = now
        )
        assertThat(decision).isEqualTo(ApiKeyRotation.KeyDecision.Fail)
        assertThat(rotation.select(a, listOf(a), "gemini-2.5-pro", now)).isNull()
        val wait = rotation.soonestWaitMs(a, listOf(a), "gemini-2.5-pro", now)
        assertThat(wait).isEqualTo(ApiKeyRotation.DAILY_COOLDOWN_MS)
    }

    @Test
    fun `a replaced secret is not stuck on the old cooldown`() {
        val rotation = ApiKeyRotation()
        val original = key("a", "key-a-1111")
        val now = 9_000L
        rotation.onFailure(
            current = original,
            bound = original,
            pool = listOf(original),
            modelId = "m",
            httpCode = 401,
            retryAfterMs = null,
            limitScope = null,
            recoverable = false,
            sameKeyRetries = 0,
            now = now
        )
        assertThat(rotation.select(original, listOf(original), "m", now)).isNull()
        val edited = original.copy(apiKey = "key-a-9999")
        assertThat(rotation.select(edited, listOf(edited), "m", now)?.apiKey).isEqualTo("key-a-9999")
    }

    @Test
    fun `server errors back off on the same key`() {
        val rotation = ApiKeyRotation()
        val a = key("a", "key-a-1111")
        val decision = rotation.onFailure(
            current = a,
            bound = a,
            pool = listOf(a),
            modelId = "m",
            httpCode = null,
            retryAfterMs = null,
            limitScope = null,
            recoverable = true,
            sameKeyRetries = 0
        )
        assertThat(decision).isInstanceOf(ApiKeyRotation.KeyDecision.Backoff::class.java)
        val backoff = decision as ApiKeyRotation.KeyDecision.Backoff
        assertThat(backoff.waitMs).isEqualTo(1_000L)
        assertThat(backoff.sameKeyRetries).isEqualTo(1)
        assertThat(rotation.select(a, listOf(a), "m")).isNotNull()
    }

    private fun key(
        id: String,
        secret: String,
        provider: AiProvider = AiProvider.GOOGLE,
        baseUrl: String? = null,
        createdAt: Long = 0,
        active: Boolean = true
    ) = ApiKeyConfig(
        id = id,
        provider = provider,
        apiKey = secret,
        baseUrl = baseUrl,
        apiFormat = ApiFormat.GOOGLE_GEMINI,
        isActive = active,
        createdAt = createdAt
    )
}
