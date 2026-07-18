package com.webtoapp.core.apkbuilder

import com.webtoapp.core.actions.DeviceActionsConfig
import com.webtoapp.core.appearance.BrowserDisguiseConfig
import com.webtoapp.core.appearance.DeviceDisguiseConfig
import com.webtoapp.core.appearance.DisguiseConfig
import com.webtoapp.core.feature.FeatureIds
import com.webtoapp.core.forcedrun.ForcedRunConfig
import com.webtoapp.core.privacy.IsolationConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private fun assertCompatOnly(plan: CapabilityPlan, reasonFragment: String? = null) {
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
        assertFalse(plan.liteOnly)
        if (reasonFragment != null) {
            assertTrue(plan.reasons.any { it.contains(reasonFragment, ignoreCase = true) })
        }
    }

    private fun assertContainsAll(plan: CapabilityPlan, vararg ids: String) {
        ids.forEach { id ->
            assertTrue("expected $id in ${plan.features}", plan.features.contains(id))
        }
        assertFalse(plan.liteOnly)
    }

    private fun assertNoneOf(plan: CapabilityPlan, vararg ids: String) {
        ids.forEach { id ->
            assertFalse("did not expect $id in ${plan.features}", plan.features.contains(id))
        }
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
    fun htmlAndFrontendStayLiteOnly() {
        assertTrue(CapabilityPlanner.plan(config("HTML")).liteOnly)
        assertTrue(CapabilityPlanner.plan(config("FRONTEND")).liteOnly)
    }

    @Test
    fun blankAppTypeStaysLiteOnly() {
        val plan = CapabilityPlanner.plan(config = config(""))
        assertTrue(plan.liteOnly)
        assertTrue(plan.features.isEmpty())
    }

    @Test
    fun activationStaysLiteOnly() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(activation = ActivationBlock(enabled = true))
        )
        assertTrue(plan.liteOnly)
        assertTrue(plan.features.isEmpty())
    }

    @Test
    fun announcementStaysLiteOnly() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                announcement = AnnouncementBlock(enabled = true, title = "hi")
            )
        )
        assertTrue(plan.liteOnly)
        assertTrue(plan.features.isEmpty())
    }

    @Test
    fun geckoMapsToCompatInCompatPhase() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB", "GECKOVIEW"),
            abiFilters = listOf("arm64-v8a"),
            phase = PlannerPhase.COMPAT
        )
        assertCompatOnly(plan, "GECKOVIEW")
    }

    @Test
    fun geckoFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB", "GECKOVIEW"),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }

    @Test
    fun nodeAppMapsToCompatInCompatPhase() {
        val plan = CapabilityPlanner.plan(
            config = config("NODEJS_APP"),
            phase = PlannerPhase.COMPAT
        )
        assertCompatOnly(plan)
    }

    @Test
    fun wordpressClosesPhpAndFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WORDPRESS"),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
        assertTrue(plan.reasons.any { it.contains("WORDPRESS") })
    }

    @Test
    fun isolationNeedsPrivacyPackInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                optionalServices = OptionalServicesBlock(isolationEnabled = true)
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(plan, FeatureIds.SHELL_PRIVACY)
        assertNoneOf(plan, FeatureIds.COMPAT)
        assertTrue(plan.reasons.any { it.contains("isolation") })
    }

    @Test
    fun isolationConfigEnabledNeedsPrivacyPackInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                optionalServices = OptionalServicesBlock(
                    isolationConfig = IsolationConfig(enabled = true)
                )
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(plan, FeatureIds.SHELL_PRIVACY)
    }

    @Test
    fun isolationMapsToCompatInCompatPhase() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                optionalServices = OptionalServicesBlock(isolationEnabled = true)
            ),
            phase = PlannerPhase.COMPAT
        )
        assertCompatOnly(plan, "isolation")
    }

    @Test
    fun miniGameNeedsErrorGamesPackInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                errorPage = ErrorPageBlock(showMiniGame = true)
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(plan, FeatureIds.SHELL_ERROR_GAMES)
        assertNoneOf(plan, FeatureIds.COMPAT)
    }

    @Test
    fun miniGameMapsToCompatInCompatPhase() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                errorPage = ErrorPageBlock(showMiniGame = true)
            ),
            phase = PlannerPhase.COMPAT
        )
        assertCompatOnly(plan, "miniGame")
    }

    @Test
    fun extensionExpandsToFinePacksInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                extension = ExtensionBlock(enabled = true)
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(
            plan,
            FeatureIds.EXT_BUILTINS,
            FeatureIds.EXT_PANEL,
            FeatureIds.EXT_CHROME_SCRIPTS
        )
        assertNoneOf(plan, FeatureIds.EXT_MODULES, FeatureIds.COMPAT)
    }

    @Test
    fun embeddedModulesTriggerExtensionPacks() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                extension = ExtensionBlock(
                    embeddedModules = listOf(
                        EmbeddedExtensionModule(id = "m1", name = "M1", code = "console.log(1)")
                    )
                )
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(plan, FeatureIds.EXT_PANEL, FeatureIds.EXT_BUILTINS, FeatureIds.EXT_CHROME_SCRIPTS)
    }

    @Test
    fun extensionMapsToCompatInCompatPhase() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(extension = ExtensionBlock(enabled = true)),
            phase = PlannerPhase.COMPAT
        )
        assertCompatOnly(plan, "extension")
    }

    @Test
    fun forcedRunExpandsToHardwarePackInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                optionalServices = OptionalServicesBlock(
                    forcedRunConfig = ForcedRunConfig(enabled = true)
                )
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(plan, FeatureIds.SHELL_FORCEDRUN_HW)
        assertNoneOf(plan, FeatureIds.SHELL_FORCEDRUN, FeatureIds.COMPAT)
    }

    @Test
    fun forcedRunMapsToCompatInCompatPhase() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                optionalServices = OptionalServicesBlock(
                    forcedRunConfig = ForcedRunConfig(enabled = true)
                )
            ),
            phase = PlannerPhase.COMPAT
        )
        assertCompatOnly(plan, "forcedRun")
    }

    @Test
    fun disguiseExpandsToJsPackInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                disguise = DisguiseBlock(
                    disguiseConfig = DisguiseConfig(enabled = true)
                )
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(plan, FeatureIds.SHELL_DISGUISE_JS)
        assertNoneOf(plan, FeatureIds.SHELL_DISGUISE, FeatureIds.COMPAT)
    }

    @Test
    fun browserDisguiseNeedsDisguiseJsInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                disguise = DisguiseBlock(
                    browserDisguiseConfig = BrowserDisguiseConfig(enabled = true)
                )
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(plan, FeatureIds.SHELL_DISGUISE_JS)
    }

    @Test
    fun deviceDisguiseNeedsDisguiseJsInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                disguise = DisguiseBlock(
                    deviceDisguiseConfig = DeviceDisguiseConfig(enabled = true)
                )
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(plan, FeatureIds.SHELL_DISGUISE_JS)
    }

    @Test
    fun translateExpandsToScriptPackInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(translate = TranslateBlock(enabled = true)),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(plan, FeatureIds.SHELL_TRANSLATE_SCRIPT)
        assertNoneOf(plan, FeatureIds.SHELL_TRANSLATE, FeatureIds.COMPAT)
    }

    @Test
    fun tlsFingerprintFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(tlsFingerprint = TlsFingerprintBlock(enabled = true)),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }

    @Test
    fun proxyFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(proxy = ProxyBlock(mode = "HTTP", host = "1.1.1.1", port = 8080)),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
        assertTrue(plan.reasons.any { it.contains("proxy") })
    }

    @Test
    fun dohFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(dns = DnsBlock(mode = "DOH")),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
        assertTrue(plan.reasons.any { it.contains("dns") })
    }

    @Test
    fun fcmNotificationFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                optionalServices = OptionalServicesBlock(
                    notificationEnabled = true,
                    notificationConfig = NotificationConfig(type = "fcm", fcmProjectId = "demo")
                )
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
        assertTrue(plan.reasons.any { it.contains("fcm") })
    }

    @Test
    fun pollingNotificationFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                optionalServices = OptionalServicesBlock(
                    notificationEnabled = true,
                    notificationConfig = NotificationConfig(type = "polling")
                )
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }

    @Test
    fun adblockFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(adBlock = AdBlockBlock(enabled = true)),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }

    @Test
    fun floatingWindowFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(floatingWindow = FloatingWindowBlock(enabled = true)),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }

    @Test
    fun bgmFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(bgm = BgmBlock(enabled = true)),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }

    @Test
    fun autoStartFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(autoStart = AutoStartBlock(enabled = true)),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }

    @Test
    fun backgroundRunFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                optionalServices = OptionalServicesBlock(backgroundRunEnabled = true)
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }

    @Test
    fun deviceActionsFallsBackToCompatInGranular() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                disguise = DisguiseBlock(
                    blackTechConfig = DeviceActionsConfig(enabled = true)
                )
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
    }

    @Test
    fun multiFlagGranularMergesFinePacksAndCompatFallback() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                extension = ExtensionBlock(enabled = true),
                optionalServices = OptionalServicesBlock(isolationEnabled = true),
                tlsFingerprint = TlsFingerprintBlock(enabled = true),
                errorPage = ErrorPageBlock(showMiniGame = true)
            ),
            phase = PlannerPhase.GRANULAR
        )
        assertContainsAll(
            plan,
            FeatureIds.EXT_BUILTINS,
            FeatureIds.EXT_PANEL,
            FeatureIds.EXT_CHROME_SCRIPTS,
            FeatureIds.SHELL_PRIVACY,
            FeatureIds.SHELL_ERROR_GAMES,
            FeatureIds.COMPAT
        )
        assertNoneOf(plan, FeatureIds.EXT_MODULES)
    }

    @Test
    fun multiFlagCompatCollapsesToSingleCompatPack() {
        val plan = CapabilityPlanner.plan(
            config = config("WEB").copy(
                extension = ExtensionBlock(enabled = true),
                optionalServices = OptionalServicesBlock(
                    isolationEnabled = true,
                    forcedRunConfig = ForcedRunConfig(enabled = true)
                ),
                errorPage = ErrorPageBlock(showMiniGame = true),
                translate = TranslateBlock(enabled = true)
            ),
            phase = PlannerPhase.COMPAT
        )
        assertEquals(listOf(FeatureIds.COMPAT), plan.features)
        assertTrue(plan.reasons.size >= 3)
    }

    @Test
    fun mediaAppTypeNeedsCompatInCompatPhase() {
        val plan = CapabilityPlanner.plan(
            config = config("IMAGE"),
            phase = PlannerPhase.COMPAT
        )
        assertCompatOnly(plan, "IMAGE")
    }

    @Test
    fun multiWebNeedsCompatInCompatPhase() {
        val plan = CapabilityPlanner.plan(
            config = config("MULTI_WEB"),
            phase = PlannerPhase.COMPAT
        )
        assertCompatOnly(plan, "MULTI_WEB")
    }
}
