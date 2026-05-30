package com.webtoapp.core.playstore

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import org.junit.Test

class PlayPolicyCheckerTest {

    @Test
    fun `web type yields no SERVER_RUNTIME_APP_TYPE violation`() {
        val app = baseApp(appType = AppType.WEB)

        val report = PlayPolicyChecker.check(app)

        assertThat(report.violations.map { it.ruleId })
            .doesNotContain("SERVER_RUNTIME_APP_TYPE")
        assertThat(report.canPublish).isTrue()
    }

    @Test
    fun `php type produces a BLOCKER violation`() {
        val app = baseApp(appType = AppType.PHP_APP)

        val report = PlayPolicyChecker.check(app)

        val v = report.violations.firstOrNull { it.ruleId == "SERVER_RUNTIME_APP_TYPE" }
        assertThat(v).isNotNull()
        assertThat(v!!.severity).isEqualTo(PlayPolicyChecker.Severity.BLOCKER)
        assertThat(report.blockerCount).isAtLeast(1)
        assertThat(report.canPublish).isFalse()
    }

    @Test
    fun `every server-runtime app type is blocked`() {

        val blockedTypes = setOf(
            AppType.PHP_APP,
            AppType.NODEJS_APP,
            AppType.PYTHON_APP,
            AppType.GO_APP,
            AppType.WORDPRESS
        )

        for (type in blockedTypes) {
            val report = PlayPolicyChecker.check(baseApp(appType = type))
            assertThat(report.violations.map { it.ruleId })
                .contains("SERVER_RUNTIME_APP_TYPE")
        }
    }

    @Test
    fun `non-runtime types are not blocked by SERVER_RUNTIME_APP_TYPE`() {
        val nonBlocked = setOf(
            AppType.WEB,
            AppType.IMAGE,
            AppType.VIDEO,
            AppType.HTML,
            AppType.GALLERY,
            AppType.FRONTEND,
            AppType.MULTI_WEB
        )

        for (type in nonBlocked) {
            val report = PlayPolicyChecker.check(baseApp(appType = type))
            assertThat(report.violations.map { it.ruleId })
                .doesNotContain("SERVER_RUNTIME_APP_TYPE")
        }
    }

    @Test
    fun `every report carries TARGET_SDK_REWRITE info`() {

        val report = PlayPolicyChecker.check(baseApp())

        val info = report.violations.firstOrNull { it.ruleId == "TARGET_SDK_REWRITE" }
        assertThat(info).isNotNull()
        assertThat(info!!.severity).isEqualTo(PlayPolicyChecker.Severity.INFO)
    }

    @Test
    fun `resolveViolation returns localised text for new rules`() {
        val sample = listOf(
            PlayPolicyChecker.Violation(
                ruleId = "SERVER_RUNTIME_APP_TYPE",
                severity = PlayPolicyChecker.Severity.BLOCKER,
                featurePath = "rule.serverRuntime.path",
                policyArea = "rule.serverRuntime.area",
                fixHint = "rule.serverRuntime.fix"
            ),
            PlayPolicyChecker.Violation(
                ruleId = "TARGET_SDK_REWRITE",
                severity = PlayPolicyChecker.Severity.INFO,
                featurePath = "rule.targetSdkRewrite.path",
                policyArea = "rule.targetSdkRewrite.area",
                fixHint = "rule.targetSdkRewrite.fix"
            )
        )

        for (v in sample) {
            val resolved = PlayPolicyChecker.resolveViolation(v)

            assertThat(resolved.featurePath).isNotEqualTo(v.featurePath)
            assertThat(resolved.fixHint).isNotEqualTo(v.fixHint)
            assertThat(resolved.featurePath).isNotEmpty()
            assertThat(resolved.fixHint).isNotEmpty()
        }
    }

    @Test
    fun `package name impersonation still flagged independently`() {

        val app = baseApp(appType = AppType.PHP_APP).copy(
            apkExportConfig = com.webtoapp.data.model.ApkExportConfig(
                customPackageName = "com.evil.fakefacebook"
            )
        )

        val report = PlayPolicyChecker.check(app)

        assertThat(report.violations.map { it.ruleId })
            .containsAtLeast("SERVER_RUNTIME_APP_TYPE", "PACKAGE_NAME_IMPERSONATION")
    }

    private fun baseApp(
        appType: AppType = AppType.WEB
    ): WebApp = WebApp(
        name = "Test App",
        url = "https://example.com",
        appType = appType
    )
}
