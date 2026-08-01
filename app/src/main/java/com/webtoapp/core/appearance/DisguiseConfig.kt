package com.webtoapp.core.appearance

import com.google.gson.annotations.SerializedName

data class BrowserDisguiseConfig(
    @SerializedName("enabled")
    val enabled: Boolean = false,

    @SerializedName("preset")
    val preset: BrowserDisguisePreset = BrowserDisguisePreset.STEALTH,

    @SerializedName("removeXRequestedWith")
    val removeXRequestedWith: Boolean = true,

    @SerializedName("sanitizeUserAgent")
    val sanitizeUserAgent: Boolean = true,

    @SerializedName("hideWebdriver")
    val hideWebdriver: Boolean = true,

    @SerializedName("emulateWindowChrome")
    val emulateWindowChrome: Boolean = true,

    @SerializedName("fakePlugins")
    val fakePlugins: Boolean = true,

    @SerializedName("fakeVendor")
    val fakeVendor: Boolean = true,

    @SerializedName("canvasNoise")
    val canvasNoise: Boolean = false,

    @SerializedName("canvasNoiseIntensity")
    val canvasNoiseIntensity: Float = 0.001f,

    @SerializedName("webglSpoof")
    val webglSpoof: Boolean = false,

    @SerializedName("webglRenderer")
    val webglRenderer: WebGLRenderer = WebGLRenderer.INTEGRATED_INTEL,

    @SerializedName("audioNoise")
    val audioNoise: Boolean = false,

    @SerializedName("screenSpoof")
    val screenSpoof: Boolean = false,

    @SerializedName("screenProfile")
    val screenProfile: ScreenProfile = ScreenProfile.FHD_1080P,

    @SerializedName("clientRectsNoise")
    val clientRectsNoise: Boolean = false,

    @SerializedName("timezoneSpoof")
    val timezoneSpoof: Boolean = false,

    @SerializedName("targetTimezone")
    val targetTimezone: String = "America/New_York",

    @SerializedName("languageSpoof")
    val languageSpoof: Boolean = false,

    @SerializedName("targetLanguages")
    val targetLanguages: List<String> = listOf("en-US", "en"),

    @SerializedName("platformSpoof")
    val platformSpoof: Boolean = false,

    @SerializedName("targetPlatform")
    val targetPlatform: String = "Win32",

    @SerializedName("hardwareConcurrencySpoof")
    val hardwareConcurrencySpoof: Boolean = false,

    @SerializedName("targetConcurrency")
    val targetConcurrency: Int = 8,

    @SerializedName("deviceMemorySpoof")
    val deviceMemorySpoof: Boolean = false,

    @SerializedName("targetMemoryGB")
    val targetMemoryGB: Int = 8,

    @SerializedName("mediaDevicesSpoof")
    val mediaDevicesSpoof: Boolean = false,

    @SerializedName("webrtcIpShield")
    val webrtcIpShield: Boolean = false,

    @SerializedName("fontEnumerationBlock")
    val fontEnumerationBlock: Boolean = false,

    @SerializedName("batteryShield")
    val batteryShield: Boolean = false,

    @SerializedName("connectionSpoof")
    val connectionSpoof: Boolean = false,

    @SerializedName("permissionsSpoof")
    val permissionsSpoof: Boolean = false,

    @SerializedName("performanceTimingNoise")
    val performanceTimingNoise: Boolean = false,

    @SerializedName("storageEstimateSpoof")
    val storageEstimateSpoof: Boolean = false,

    @SerializedName("notificationSpoof")
    val notificationSpoof: Boolean = false,

    @SerializedName("cssMediaSpoof")
    val cssMediaSpoof: Boolean = false,

    @SerializedName("nativeToStringProtection")
    val nativeToStringProtection: Boolean = false,

    @SerializedName("iframeDisguisePropagation")
    val iframeDisguisePropagation: Boolean = false,

    @SerializedName("errorStackCleaning")
    val errorStackCleaning: Boolean = true
) {
    companion object {
        val DISABLED = BrowserDisguiseConfig(enabled = false)

        fun fromPreset(preset: BrowserDisguisePreset): BrowserDisguiseConfig = when (preset) {
            BrowserDisguisePreset.OFF -> DISABLED

            BrowserDisguisePreset.STEALTH -> BrowserDisguiseConfig(
                enabled = true,
                preset = preset,

                removeXRequestedWith = true,
                sanitizeUserAgent = true,
                hideWebdriver = true,
                emulateWindowChrome = true,
                fakePlugins = true,
                fakeVendor = true,
                errorStackCleaning = true
            )

            BrowserDisguisePreset.GHOST -> BrowserDisguiseConfig(
                enabled = true,
                preset = preset,

                removeXRequestedWith = true,
                sanitizeUserAgent = true,
                hideWebdriver = true,
                emulateWindowChrome = true,
                fakePlugins = true,
                fakeVendor = true,

                canvasNoise = true,
                webglSpoof = true,
                audioNoise = true,
                screenSpoof = true,
                clientRectsNoise = true,

                errorStackCleaning = true,
                nativeToStringProtection = true
            )

            BrowserDisguisePreset.PHANTOM -> BrowserDisguiseConfig(
                enabled = true,
                preset = preset,

                removeXRequestedWith = true,
                sanitizeUserAgent = true,
                hideWebdriver = true,
                emulateWindowChrome = true,
                fakePlugins = true,
                fakeVendor = true,

                canvasNoise = true,
                webglSpoof = true,
                audioNoise = true,
                screenSpoof = true,
                clientRectsNoise = true,

                timezoneSpoof = true,
                languageSpoof = true,
                platformSpoof = true,
                hardwareConcurrencySpoof = true,
                deviceMemorySpoof = true,

                mediaDevicesSpoof = true,
                webrtcIpShield = true,
                fontEnumerationBlock = true,
                batteryShield = true,

                nativeToStringProtection = true,
                iframeDisguisePropagation = true,
                errorStackCleaning = true,

                connectionSpoof = true,
                permissionsSpoof = true,
                performanceTimingNoise = true,
                storageEstimateSpoof = true,
                notificationSpoof = true,
                cssMediaSpoof = true
            )

            BrowserDisguisePreset.SPECTER -> BrowserDisguiseConfig(
                enabled = true,
                preset = preset,

                removeXRequestedWith = true,
                sanitizeUserAgent = true,
                hideWebdriver = true,
                emulateWindowChrome = true,
                fakePlugins = true,
                fakeVendor = true,
                canvasNoise = true,
                canvasNoiseIntensity = 0.002f,
                webglSpoof = true,
                webglRenderer = WebGLRenderer.DISCRETE_NVIDIA,
                audioNoise = true,
                screenSpoof = true,
                screenProfile = ScreenProfile.QHD_1440P,
                clientRectsNoise = true,
                timezoneSpoof = true,
                targetTimezone = "America/Los_Angeles",
                languageSpoof = true,
                targetLanguages = listOf("en-US", "en"),
                platformSpoof = true,
                targetPlatform = "Win32",
                hardwareConcurrencySpoof = true,
                targetConcurrency = 16,
                deviceMemorySpoof = true,
                targetMemoryGB = 16,
                mediaDevicesSpoof = true,
                webrtcIpShield = true,
                fontEnumerationBlock = true,
                batteryShield = true,
                nativeToStringProtection = true,
                iframeDisguisePropagation = true,
                errorStackCleaning = true,

                connectionSpoof = true,
                permissionsSpoof = true,
                performanceTimingNoise = true,
                storageEstimateSpoof = true,
                notificationSpoof = true,
                cssMediaSpoof = true
            )

            BrowserDisguisePreset.CUSTOM -> BrowserDisguiseConfig(
                enabled = true,
                preset = preset
            )
        }

        fun calculateCoverage(config: BrowserDisguiseConfig): Float {
            if (!config.enabled) return 0f
            val total = 28f
            var active = 0
            if (config.removeXRequestedWith) active++
            if (config.sanitizeUserAgent) active++
            if (config.hideWebdriver) active++
            if (config.emulateWindowChrome) active++
            if (config.fakePlugins) active++
            if (config.fakeVendor) active++
            if (config.canvasNoise) active++
            if (config.webglSpoof) active++
            if (config.audioNoise) active++
            if (config.screenSpoof) active++
            if (config.clientRectsNoise) active++
            if (config.timezoneSpoof) active++
            if (config.languageSpoof) active++
            if (config.platformSpoof) active++
            if (config.hardwareConcurrencySpoof) active++
            if (config.deviceMemorySpoof) active++
            if (config.mediaDevicesSpoof) active++
            if (config.webrtcIpShield) active++
            if (config.fontEnumerationBlock) active++
            if (config.batteryShield) active++
            if (config.nativeToStringProtection) active++
            if (config.iframeDisguisePropagation) active++
            if (config.connectionSpoof) active++
            if (config.permissionsSpoof) active++
            if (config.performanceTimingNoise) active++
            if (config.storageEstimateSpoof) active++
            if (config.notificationSpoof) active++
            if (config.cssMediaSpoof) active++

            return active / total
        }

        fun getDisguiseLevel(coverage: Float): String = when {
            coverage <= 0f -> "OFF"
            coverage < 0.3f -> "BASIC"
            coverage < 0.5f -> "MODERATE"
            coverage < 0.75f -> "ADVANCED"
            coverage < 0.95f -> "DEEP"
            else -> "MAXIMUM"
        }
    }
}

enum class BrowserDisguisePreset(
    val displayName: String,
    val description: String,
    val level: Int
) {
    OFF("Off", "No browser disguise", 0),
    STEALTH("🥷 Stealth", "Remove WebView traces, basic anti-detection", 1),
    GHOST("👻 Ghost", "Canvas/WebGL/Audio fingerprint spoofing", 2),
    PHANTOM("🔮 Phantom", "Full environment spoofing: timezone, language, hardware", 3),
    SPECTER("💀 Specter", "Maximum disguise: prototype protection + iframe propagation", 4),
    CUSTOM("⚙️ Custom", "Manual fine-grained control", 5)
}

enum class WebGLRenderer(
    val displayName: String,
    val renderer: String,
    val vendor: String
) {
    INTEGRATED_INTEL(
        "Intel HD (Most Common)",
        "ANGLE (Intel, Intel(R) UHD Graphics 630 Direct3D11 vs_5_0 ps_5_0, D3D11)",
        "Google Inc. (Intel)"
    ),
    INTEGRATED_INTEL_IRIS(
        "Intel Iris Xe",
        "ANGLE (Intel, Intel(R) Iris(R) Xe Graphics Direct3D11 vs_5_0 ps_5_0, D3D11)",
        "Google Inc. (Intel)"
    ),
    DISCRETE_NVIDIA(
        "NVIDIA GeForce RTX",
        "ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0, D3D11)",
        "Google Inc. (NVIDIA)"
    ),
    DISCRETE_AMD(
        "AMD Radeon RX",
        "ANGLE (AMD, AMD Radeon RX 6600 XT Direct3D11 vs_5_0 ps_5_0, D3D11)",
        "Google Inc. (AMD)"
    ),
    APPLE_M1(
        "Apple M1 GPU",
        "Apple GPU",
        "Apple"
    ),
    APPLE_M2(
        "Apple M2 GPU",
        "Apple GPU",
        "Apple"
    ),
    QUALCOMM_ADRENO(
        "Qualcomm Adreno (Mobile)",
        "Adreno (TM) 730",
        "Qualcomm"
    )
}

enum class ScreenProfile(
    val displayName: String,
    val width: Int,
    val height: Int,
    val colorDepth: Int,
    val pixelRatio: Double
) {
    HD_720P("720p HD", 1366, 768, 24, 1.0),
    FHD_1080P("1080p Full HD", 1920, 1080, 24, 1.0),
    QHD_1440P("1440p QHD", 2560, 1440, 24, 1.0),
    UHD_4K("4K UHD", 3840, 2160, 24, 2.0),
    MACBOOK_PRO("MacBook Pro 14\"", 3024, 1964, 30, 2.0),
    IPHONE_15("iPhone 15 Pro", 1179, 2556, 30, 3.0),
    PIXEL_8("Pixel 8", 1080, 2400, 24, 2.625)
}
