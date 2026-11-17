package com.webtoapp.core.nodejs

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NodeServiceStartBudgetTest {

    @Test
    fun `client reply timeout outlasts the server ready budget`() {
        assertThat(NodeServiceProtocol.START_REPLY_TIMEOUT_MS)
            .isGreaterThan(NodeServiceProtocol.SERVER_READY_BUDGET_MS)
    }

    @Test
    fun `ready budget matches the health check loop`() {
        val perAttempt = 2L * NodeServiceProtocol.HEALTH_PROBE_TIMEOUT_MS +
            NodeServiceProtocol.HEALTH_CHECK_INTERVAL_MS
        assertThat(NodeServiceProtocol.SERVER_READY_BUDGET_MS)
            .isEqualTo(NodeServiceProtocol.MAX_HEALTH_CHECK_RETRIES * perAttempt)
    }

    @Test
    fun `scoped stop ignores a different request and honors the matching one`() {
        val service = Robolectric.setupService(NodeService::class.java)
        val requestField = NodeService::class.java.getDeclaredField("currentRequestId")
        requestField.isAccessible = true
        requestField.set(service, "request-a")

        val stop = NodeService::class.java.getDeclaredMethod("stopServerForRequest", String::class.java)
        stop.isAccessible = true

        assertThat(stop.invoke(service, "request-b")).isNull()
        assertThat(requestField.get(service)).isEqualTo("request-a")

        assertThat(stop.invoke(service, "request-a")).isEqualTo(false)
        assertThat(requestField.get(service)).isEqualTo("")

        requestField.set(service, "request-c")
        assertThat(stop.invoke(service, "")).isEqualTo(false)
    }
}
