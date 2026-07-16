package com.webtoapp.core.apkbuilder

import com.webtoapp.core.feature.FeatureIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityPlannerTest {

    private fun config(appType: String, engineType: String = "SYSTEM_WEBVIEW"): ApkConfig {
        return ApkConfig(
            meta = MetaBlock(
                appName = "Test",
                packageName = "com.example.test",
                targetUrl = "https://example.com",
                appType = appType,
                engineType = engineType
            )
        )
    }

    @Test
    fun pureWebIsLiteOnly() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB"),
            abiFilters = listOf("arm64-v8a")
        )
        assertTrue(plan.liteOnly)
        assertTrue(plan.features.isEmpty())
    }

    @Test
    fun geckoMapsToCompatInPhase2() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB", "GECKOVIEW"),
            abiFilters = listOf("arm64-v8a"),
            phase = PlannerPhase.COMPAT
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
        assertTrue(plan.reasons.any { it.contains("GECKOVIEW") })
    }

    @Test
    fun nodeAppMapsToCompatInPhase2() {
        val plan = CapabilityPlanner.plan(
            config = config("NODEJS_APP"),
            phase = PlannerPhase.COMPAT
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }

    @Test
    fun isolationNeedsPrivacyPackInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                optionalServices = OptionalServicesBlock(isolationEnabled = true)
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertTrue(plan.features.contains(FeatureIds.SHELL_PRIVACY))
        assertTrue(plan.reasons.any { it.contains("isolation") })
    }

    @Test
    fun miniGameNeedsErrorGamesPackInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                errorPage = ErrorPageBlock(showMiniGame = true)
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertTrue(plan.features.contains(FeatureIds.SHELL_ERROR_GAMES))
    }

    @Test
    fun isolationMapsToCompatInCompatPhase() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                optionalServices = OptionalServicesBlock(isolationEnabled = true)
            ),
            phase = PlannerPhase.COMPAT
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }
}
