package com.webtoapp.core.apkbuilder

import com.webtoapp.core.feature.FeatureIds

enum class PlannerPhase {
    COMPAT,
    GRANULAR
}

data class CapabilityPlan(
    val features: List<String>,
    val reasons: List<String>,
    val abiFilters: List<String>,
    val liteOnly: Boolean
)

object CapabilityPlanner {

    fun plan(
        config: ApkConfig,
        abiFilters: List<String> = emptyList(),
        phase: PlannerPhase = PlannerPhase.COMPAT
    ): CapabilityPlan {
        val features = linkedSetOf<String>()
        val reasons = mutableListOf<String>()

        fun need(id: String, reason: String) {
            val mapped = if (phase == PlannerPhase.COMPAT && id != FeatureIds.COMPAT) {
                FeatureIds.COMPAT
            } else {
                id
            }
            if (features.add(mapped)) {
                reasons += reason
            } else if (reason !in reasons) {
                reasons += reason
            }
        }

        val appType = config.appType.trim().uppercase()
        when (appType) {
            "WEB", "HTML", "FRONTEND" -> Unit
            "IMAGE", "VIDEO", "GALLERY" -> need(FeatureIds.SHELL_MEDIA, "appType=$appType")
            "MULTI_WEB" -> need(FeatureIds.SHELL_MULTIWEB, "appType=MULTI_WEB")
            "NODEJS_APP" -> need(FeatureIds.SERVER_NODEJS, "appType=NODEJS_APP")
            "PHP_APP" -> need(FeatureIds.SERVER_PHP, "appType=PHP_APP")
            "PYTHON_APP" -> need(FeatureIds.SERVER_PYTHON, "appType=PYTHON_APP")
            "GO_APP" -> need(FeatureIds.SERVER_GO, "appType=GO_APP")
            "WORDPRESS" -> need(FeatureIds.SERVER_WORDPRESS, "appType=WORDPRESS")
            else -> need(FeatureIds.COMPAT, "unknown appType=$appType")
        }

        if (config.engineType.equals("GECKOVIEW", ignoreCase = true)) {
            need(FeatureIds.ENGINE_GECKO, "engineType=GECKOVIEW")
        }

        if (config.tlsFingerprintEnabled) {
            need(FeatureIds.NET_TLS_MITM, "tlsFingerprint")
        }
        if (isProxyEnabled(config)) {
            need(FeatureIds.NET_PROXY, "proxy")
        }
        if (isDohEnabled(config)) {
            need(FeatureIds.NET_DOH, "dns/doh")
        }

        if (config.notificationEnabled) {
            val type = config.notificationConfig?.type?.lowercase().orEmpty()
            if (type == "fcm" || hasFcmConfig(config)) {
                need(FeatureIds.PUSH_FCM, "notification=fcm")
            } else if (type != "none" && type.isNotBlank()) {
                need(FeatureIds.NOTIFY_CHANNELS, "notification=$type")
            }
        }

        if (config.extensionEnabled ||
            config.embeddedExtensionModules.isNotEmpty() ||
            config.extensionModuleIds.isNotEmpty()
        ) {
            need(FeatureIds.EXT_MODULES, "extension")
        }

        if (config.adBlockEnabled) {
            need(FeatureIds.SHELL_ADBLOCK, "adblock")
        }
        if (config.adsEnabled) {
            need(FeatureIds.SHELL_ADBLOCK, "ads")
        }
        if (config.disguiseConfig?.enabled == true) {
            need(FeatureIds.SHELL_DISGUISE, "disguise")
        }
        if (config.browserDisguiseConfig?.enabled == true || config.deviceDisguiseConfig?.enabled == true) {
            need(FeatureIds.SHELL_DISGUISE, "browser/device disguise")
        }
        if (config.forcedRunConfig?.enabled == true) {
            need(FeatureIds.SHELL_FORCEDRUN, "forcedRun")
        }
        if (config.floatingWindowEnabled) {
            need(FeatureIds.SHELL_FLOATING, "floatingWindow")
        }
        if (config.bgmEnabled) {
            need(FeatureIds.SHELL_BGM, "bgm")
        }
        if (config.translateEnabled) {
            need(FeatureIds.SHELL_TRANSLATE, "translate")
        }
        if (config.autoStartEnabled) {
            need(FeatureIds.SHELL_AUTOSTART, "autoStart")
        }
        if (config.backgroundRunEnabled) {
            need(FeatureIds.SHELL_BACKGROUND, "backgroundRun")
        }
        if (config.blackTechConfig?.enabled == true) {
            need(FeatureIds.SHELL_DEVICE_ACTIONS, "deviceActions")
        }
        if (config.isolationEnabled || config.isolationConfig?.enabled == true) {
            need(FeatureIds.SHELL_PRIVACY, "isolation")
        }
        if (config.errorPageShowMiniGame) {
            need(FeatureIds.SHELL_ERROR_GAMES, "errorPage.miniGame")
        }

        val closed = closeDependencies(features, phase)
        val abi = abiFilters.ifEmpty { listOf("arm64-v8a") }

        return CapabilityPlan(
            features = closed.toList(),
            reasons = reasons.distinct(),
            abiFilters = abi,
            liteOnly = closed.isEmpty()
        )
    }

    private fun closeDependencies(
        features: Set<String>,
        phase: PlannerPhase
    ): LinkedHashSet<String> {
        if (phase == PlannerPhase.COMPAT) {
            return LinkedHashSet(features)
        }
        val out = LinkedHashSet<String>()
        fun addWithDeps(id: String) {
            if (!out.add(id)) return
            when (id) {
                FeatureIds.SERVER_WORDPRESS -> addWithDeps(FeatureIds.SERVER_PHP)
                FeatureIds.SHELL_FORCEDRUN -> addWithDeps(FeatureIds.SHELL_FORCEDRUN_HW)
                FeatureIds.EXT_MODULES -> {
                    addWithDeps(FeatureIds.EXT_BUILTINS)
                    addWithDeps(FeatureIds.EXT_PANEL)
                    addWithDeps(FeatureIds.EXT_CHROME_SCRIPTS)
                }
                FeatureIds.SHELL_DISGUISE -> addWithDeps(FeatureIds.SHELL_DISGUISE_JS)
                FeatureIds.SHELL_TRANSLATE -> addWithDeps(FeatureIds.SHELL_TRANSLATE_SCRIPT)
            }
        }
        features.forEach { addWithDeps(it) }
        return out
    }

    private fun isProxyEnabled(config: ApkConfig): Boolean {
        val mode = config.proxyMode.trim().uppercase()
        return mode.isNotEmpty() && mode != "NONE" && mode != "OFF" && mode != "DISABLED"
    }

    private fun isDohEnabled(config: ApkConfig): Boolean {
        val mode = config.dnsMode.trim().uppercase()
        if (mode in setOf("SYSTEM", "OFF", "NONE", "")) {
            return config.dnsConfig.echEnabled
        }
        val dns = config.dnsConfig
        if (dns.echEnabled || dns.bypassSystemDns) return true
        return mode in setOf("DOH", "DNS_OVER_HTTPS", "CUSTOM", "PROVIDER")
    }

    private fun hasFcmConfig(config: ApkConfig): Boolean {
        val nc = config.notificationConfig ?: return false
        return nc.fcmProjectId.isNotBlank() ||
            nc.fcmApplicationId.isNotBlank() ||
            nc.fcmApiKey.isNotBlank() ||
            nc.fcmSenderId.isNotBlank() ||
            nc.fcmGoogleServicesJson.isNotBlank()
    }
}
