package com.webtoapp.core.nodejs

import com.google.common.truth.Truth.assertThat
import org.junit.Test

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
}
