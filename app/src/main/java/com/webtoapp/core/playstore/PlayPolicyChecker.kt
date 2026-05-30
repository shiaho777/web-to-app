package com.webtoapp.core.playstore

import com.webtoapp.data.model.WebApp

object PlayPolicyChecker {

    enum class Severity {
        BLOCKER,
        WARNING,
        INFO
    }

    data class Violation(
        val ruleId: String,
        val severity: Severity,
        val featurePath: String,
        val policyArea: String,
        val fixHint: String
    )

    data class Report(
        val appId: Long,
        val appName: String,
        val violations: List<Violation>
    ) {
        val blockerCount: Int get() = violations.count { it.severity == Severity.BLOCKER }
        val warningCount: Int get() = violations.count { it.severity == Severity.WARNING }
        val infoCount: Int get() = violations.count { it.severity == Severity.INFO }
        val canPublish: Boolean get() = blockerCount == 0
        val isClean: Boolean get() = violations.isEmpty()
    }

    fun check(webApp: WebApp): Report {
        val violations = mutableListOf<Violation>()

        if (webApp.forcedRunConfig?.enabled == true) {
            violations.add(
                Violation(
                    ruleId = "FORCED_RUN_ENABLED",
                    severity = Severity.BLOCKER,
                    featurePath = "rule.forcedRun.path",
                    policyArea = "rule.forcedRun.area",
                    fixHint = "rule.forcedRun.fix"
                )
            )
        }

        if (webApp.blackTechConfig?.enabled == true) {
            violations.add(
                Violation(
                    ruleId = "DEVICE_ACTIONS_ENABLED",
                    severity = Severity.BLOCKER,
                    featurePath = "rule.deviceActions.path",
                    policyArea = "rule.deviceActions.area",
                    fixHint = "rule.deviceActions.fix"
                )
            )
        }

        val iconStormCount = webApp.disguiseConfig?.multiLauncherIcons ?: 0
        if (webApp.disguiseConfig?.enabled == true && iconStormCount > 1) {
            violations.add(
                Violation(
                    ruleId = "ICON_STORM_ENABLED",
                    severity = Severity.BLOCKER,
                    featurePath = "rule.iconStorm.path",
                    policyArea = "rule.iconStorm.area",
                    fixHint = "rule.iconStorm.fix"
                )
            )
        }

        if (webApp.apkExportConfig?.hardeningConfig?.enabled == true) {
            violations.add(
                Violation(
                    ruleId = "APP_HARDENING_ENABLED",
                    severity = Severity.BLOCKER,
                    featurePath = "rule.hardening.path",
                    policyArea = "rule.hardening.area",
                    fixHint = "rule.hardening.fix"
                )
            )
        }

        if (webApp.apkExportConfig?.encryptionConfig?.enabled == true) {
            violations.add(
                Violation(
                    ruleId = "APK_ENCRYPTION_ENABLED",
                    severity = Severity.BLOCKER,
                    featurePath = "rule.apkEncryption.path",
                    policyArea = "rule.apkEncryption.area",
                    fixHint = "rule.apkEncryption.fix"
                )
            )
        }

        if (webApp.browserDisguiseConfig?.enabled == true) {
            violations.add(
                Violation(
                    ruleId = "BROWSER_DISGUISE_ENABLED",
                    severity = Severity.WARNING,
                    featurePath = "rule.browserDisguise.path",
                    policyArea = "rule.browserDisguise.area",
                    fixHint = "rule.browserDisguise.fix"
                )
            )
        }

        if (webApp.deviceDisguiseConfig?.enabled == true) {
            violations.add(
                Violation(
                    ruleId = "DEVICE_DISGUISE_ENABLED",
                    severity = Severity.WARNING,
                    featurePath = "rule.deviceDisguise.path",
                    policyArea = "rule.deviceDisguise.area",
                    fixHint = "rule.deviceDisguise.fix"
                )
            )
        }

        if (webApp.extensionEnabled && webApp.extensionModuleIds.isNotEmpty()) {
            violations.add(
                Violation(
                    ruleId = "EXTENSION_MODULES_ENABLED",
                    severity = Severity.WARNING,
                    featurePath = "rule.extensions.path",
                    policyArea = "rule.extensions.area",
                    fixHint = "rule.extensions.fix"
                )
            )
        }

        val autoStartActive = webApp.autoStartConfig?.let {
            it.bootStartEnabled || it.scheduledStartEnabled
        } ?: false
        if (autoStartActive && webApp.apkExportConfig?.backgroundRunEnabled == true) {
            violations.add(
                Violation(
                    ruleId = "AUTOSTART_BACKGROUND_COMBO",
                    severity = Severity.WARNING,
                    featurePath = "rule.autostart.path",
                    policyArea = "rule.autostart.area",
                    fixHint = "rule.autostart.fix"
                )
            )
        }

        if (webApp.webViewConfig.injectScripts.any { it.enabled }) {
            violations.add(
                Violation(
                    ruleId = "USER_SCRIPTS_PRESENT",
                    severity = Severity.INFO,
                    featurePath = "rule.userScripts.path",
                    policyArea = "rule.userScripts.area",
                    fixHint = "rule.userScripts.fix"
                )
            )
        }

        webApp.apkExportConfig?.customPackageName?.let { pkg ->
            if (looksLikeImpersonation(pkg)) {
                violations.add(
                    Violation(
                        ruleId = "PACKAGE_NAME_IMPERSONATION",
                        severity = Severity.WARNING,
                        featurePath = "rule.packageName.path",
                        policyArea = "rule.packageName.area",
                        fixHint = "rule.packageName.fix"
                    )
                )
            }
        }

        if (webApp.activationEnabled) {
            violations.add(
                Violation(
                    ruleId = "ACTIVATION_GATING_ENABLED",
                    severity = Severity.INFO,
                    featurePath = "rule.activation.path",
                    policyArea = "rule.activation.area",
                    fixHint = "rule.activation.fix"
                )
            )
        }

        if (webApp.appType in com.webtoapp.core.playstore.aab.AabExportCoordinator.PROCESS_EXEC_APP_TYPES) {
            violations.add(
                Violation(
                    ruleId = "SERVER_RUNTIME_APP_TYPE",
                    severity = Severity.BLOCKER,
                    featurePath = "rule.serverRuntime.path",
                    policyArea = "rule.serverRuntime.area",
                    fixHint = "rule.serverRuntime.fix"
                )
            )
        }

        violations.add(
            Violation(
                ruleId = "TARGET_SDK_REWRITE",
                severity = Severity.INFO,
                featurePath = "rule.targetSdkRewrite.path",
                policyArea = "rule.targetSdkRewrite.area",
                fixHint = "rule.targetSdkRewrite.fix"
            )
        )

        return Report(
            appId = webApp.id,
            appName = webApp.name,
            violations = violations
        )
    }

    private fun looksLikeImpersonation(pkg: String): Boolean {
        val lower = pkg.lowercase()
        val brandPatterns = listOf(
            "google", "googel", "g00gle",
            "facebook", "facebok", "faceboook",
            "instagram", "instgram",
            "whatsapp", "whatsap", "whatapp",
            "tiktok", "tikok",
            "youtube", "youtub",
            "twitter", "twiter",
            "amazon", "amaz0n",
            "microsoft", "microsft",
            "apple", "appl",
            "netflix", "netfilx",
            "spotify", "spotfy",
            "telegram", "telgram",
            "wechat", "weixin",
            "alipay", "alipy",
            "taobao", "tabao",
            "baidu", "baidoo"
        )

        for (brand in brandPatterns) {
            if (lower.contains(brand)) {

                val segments = lower.split(".", "-", "_")
                val brandIndex = segments.indexOfFirst { it.contains(brand) }
                if (brandIndex >= 2) return true

                if (brand !in setOf("google", "facebook", "instagram", "whatsapp", "tiktok",
                        "youtube", "twitter", "amazon", "microsoft", "apple", "netflix",
                        "spotify", "telegram", "wechat", "weixin", "alipay", "taobao", "baidu")) {
                    return true
                }
            }
        }
        return false
    }

    data class ResolvedViolation(
        val severity: Severity,
        val featurePath: String,
        val policyArea: String,
        val fixHint: String
    )

    fun resolveViolation(v: Violation): ResolvedViolation {
        val s = com.webtoapp.core.i18n.Strings
        return when (v.ruleId) {
            "FORCED_RUN_ENABLED" -> ResolvedViolation(
                v.severity, s.rulePathForcedRun, s.ruleAreaForcedRun, s.ruleFixForcedRun
            )
            "DEVICE_ACTIONS_ENABLED" -> ResolvedViolation(
                v.severity, s.rulePathDeviceActions, s.ruleAreaDeviceActions, s.ruleFixDeviceActions
            )
            "ICON_STORM_ENABLED" -> ResolvedViolation(
                v.severity, s.rulePathIconStorm, s.ruleAreaIconStorm, s.ruleFixIconStorm
            )
            "APP_HARDENING_ENABLED" -> ResolvedViolation(
                v.severity, s.rulePathHardening, s.ruleAreaHardening, s.ruleFixHardening
            )
            "APK_ENCRYPTION_ENABLED" -> ResolvedViolation(
                v.severity, s.rulePathApkEncryption, s.ruleAreaApkEncryption, s.ruleFixApkEncryption
            )
            "BROWSER_DISGUISE_ENABLED" -> ResolvedViolation(
                v.severity, s.rulePathBrowserDisguise, s.ruleAreaBrowserDisguise, s.ruleFixBrowserDisguise
            )
            "DEVICE_DISGUISE_ENABLED" -> ResolvedViolation(
                v.severity, s.rulePathDeviceDisguise, s.ruleAreaDeviceDisguise, s.ruleFixDeviceDisguise
            )
            "EXTENSION_MODULES_ENABLED" -> ResolvedViolation(
                v.severity, s.rulePathExtensions, s.ruleAreaExtensions, s.ruleFixExtensions
            )
            "AUTOSTART_BACKGROUND_COMBO" -> ResolvedViolation(
                v.severity, s.rulePathAutostart, s.ruleAreaAutostart, s.ruleFixAutostart
            )
            "USER_SCRIPTS_PRESENT" -> ResolvedViolation(
                v.severity, s.rulePathUserScripts, s.ruleAreaUserScripts, s.ruleFixUserScripts
            )
            "PACKAGE_NAME_IMPERSONATION" -> ResolvedViolation(
                v.severity, s.rulePathPackageName, s.ruleAreaPackageName, s.ruleFixPackageName
            )
            "ACTIVATION_GATING_ENABLED" -> ResolvedViolation(
                v.severity, s.rulePathActivation, s.ruleAreaActivation, s.ruleFixActivation
            )
            "SERVER_RUNTIME_APP_TYPE" -> ResolvedViolation(
                v.severity, s.rulePathServerRuntime, s.ruleAreaServerRuntime, s.ruleFixServerRuntime
            )
            "TARGET_SDK_REWRITE" -> ResolvedViolation(
                v.severity, s.rulePathTargetSdkRewrite, s.ruleAreaTargetSdkRewrite, s.ruleFixTargetSdkRewrite
            )
            else -> ResolvedViolation(
                v.severity, v.featurePath, v.policyArea, v.fixHint
            )
        }
    }
}
