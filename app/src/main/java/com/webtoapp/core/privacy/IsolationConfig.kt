package com.webtoapp.core.privacy

import java.util.UUID

data class IsolationConfig(
    val enabled: Boolean = false,

    val fingerprintConfig: FingerprintConfig = FingerprintConfig(),

    val blockWebRTC: Boolean = true,

    val protectCanvas: Boolean = true,

    val protectAudio: Boolean = true,

    val protectWebGL: Boolean = true,

    val protectFonts: Boolean = false,

    val spoofTimezone: Boolean = false,
    val customTimezone: String? = null,

    val spoofLanguage: Boolean = false,
    val customLanguage: String? = null,

    val spoofScreen: Boolean = false,
    val customScreenWidth: Int? = null,
    val customScreenHeight: Int? = null,
    val customDevicePixelRatio: Float? = null
) {
    companion object {
        private val gson = com.webtoapp.util.GsonProvider.gson
        val DISABLED = IsolationConfig(enabled = false)

        /** 基础防护：指纹随机化 + Canvas 噪音 + WebRTC 屏蔽，行为最不易被察觉。 */
        val BASIC = IsolationConfig(
            enabled = true,
            protectCanvas = true,
            protectAudio = false,
            protectWebGL = false,
            protectFonts = false,
            blockWebRTC = true
        )

        /** 标准防护：完整指纹防护（无环境伪装）。 */
        val STANDARD = IsolationConfig(
            enabled = true,
            protectCanvas = true,
            protectAudio = true,
            protectWebGL = true,
            protectFonts = false,
            blockWebRTC = true
        )

        /** 最强防护：全部防护 + 语言/时区/分辨率伪装 + 每次启动换指纹。 */
        val MAXIMUM = IsolationConfig(
            enabled = true,
            fingerprintConfig = FingerprintConfig(randomize = true, regenerateOnLaunch = true),
            protectCanvas = true,
            protectAudio = true,
            protectWebGL = true,
            protectFonts = true,
            blockWebRTC = true,
            spoofTimezone = true,
            spoofLanguage = true,
            spoofScreen = true
        )

        fun fromJson(json: String): IsolationConfig? {
            return try {
                gson.fromJson(json, IsolationConfig::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    /** 当前配置匹配的防护等级；返回 null 表示自定义组合。 */
    fun level(): IsolationLevel? = when {
        !enabled -> null
        matches(MAXIMUM) -> IsolationLevel.MAXIMUM
        matches(STANDARD) -> IsolationLevel.STANDARD
        matches(BASIC) -> IsolationLevel.BASIC
        else -> null
    }

    /** 仅比较“防护强度”相关的字段，忽略各伪装项的具体取值。 */
    private fun matches(preset: IsolationConfig): Boolean {
        return fingerprintConfig.randomize == preset.fingerprintConfig.randomize &&
            fingerprintConfig.regenerateOnLaunch == preset.fingerprintConfig.regenerateOnLaunch &&
            protectCanvas == preset.protectCanvas &&
            protectAudio == preset.protectAudio &&
            protectWebGL == preset.protectWebGL &&
            protectFonts == preset.protectFonts &&
            blockWebRTC == preset.blockWebRTC &&
            spoofTimezone == preset.spoofTimezone &&
            spoofLanguage == preset.spoofLanguage &&
            spoofScreen == preset.spoofScreen
    }

    fun toJson(): String = gson.toJson(this)
}

enum class IsolationLevel { BASIC, STANDARD, MAXIMUM }

/**
 * 应用“防护强度”预设：只重置防护开关与伪装启停，保留用户已选的伪装取值与指纹种子。
 */
fun IsolationConfig.withLevel(level: IsolationLevel): IsolationConfig {
    val preset = when (level) {
        IsolationLevel.BASIC -> IsolationConfig.BASIC
        IsolationLevel.STANDARD -> IsolationConfig.STANDARD
        IsolationLevel.MAXIMUM -> IsolationConfig.MAXIMUM
    }
    return copy(
        enabled = true,
        fingerprintConfig = fingerprintConfig.copy(
            randomize = preset.fingerprintConfig.randomize,
            regenerateOnLaunch = preset.fingerprintConfig.regenerateOnLaunch
        ),
        protectCanvas = preset.protectCanvas,
        protectAudio = preset.protectAudio,
        protectWebGL = preset.protectWebGL,
        protectFonts = preset.protectFonts,
        blockWebRTC = preset.blockWebRTC,
        spoofTimezone = preset.spoofTimezone,
        spoofLanguage = preset.spoofLanguage,
        spoofScreen = preset.spoofScreen
    )
}

data class FingerprintConfig(
    val randomize: Boolean = true,
    val regenerateOnLaunch: Boolean = false,

    val customUserAgent: String? = null,
    val randomUserAgent: Boolean = true,

    val platform: String? = null,
    val vendor: String? = null,

    val hardwareConcurrency: Int? = null,
    val deviceMemory: Int? = null,

    val fingerprintId: String = UUID.randomUUID().toString()
)

/** 语言伪装可选地区（label 为原生名称，无需随界面语言翻译）。 */
data class SpoofLocaleOption(val tag: String, val label: String)

/** 时区伪装可选项（label 附标准 UTC 偏移，运行期按 Intl 自动处理夏令时）。 */
data class SpoofTimezoneOption(val id: String, val label: String)

/** 分辨率伪装可选项（CSS 像素尺寸 + 配套 DPR，保证 screen/innerWidth/matchMedia 一致）。 */
data class SpoofScreenOption(val width: Int, val height: Int, val dpr: Float) {
    val label: String get() = "${width} × ${height}"
}

object IsolationPresets {

    val locales = listOf(
        SpoofLocaleOption("zh-CN", "简体中文"),
        SpoofLocaleOption("zh-TW", "繁體中文"),
        SpoofLocaleOption("en-US", "English (US)"),
        SpoofLocaleOption("en-GB", "English (UK)"),
        SpoofLocaleOption("ja-JP", "日本語"),
        SpoofLocaleOption("ko-KR", "한국어"),
        SpoofLocaleOption("de-DE", "Deutsch"),
        SpoofLocaleOption("fr-FR", "Français"),
        SpoofLocaleOption("es-ES", "Español"),
        SpoofLocaleOption("pt-BR", "Português (BR)"),
        SpoofLocaleOption("ru-RU", "Русский"),
        SpoofLocaleOption("ar-SA", "العربية"),
        SpoofLocaleOption("hi-IN", "हिन्दी"),
        SpoofLocaleOption("it-IT", "Italiano"),
        SpoofLocaleOption("nl-NL", "Nederlands")
    )

    val timezones = listOf(
        SpoofTimezoneOption("Asia/Shanghai", "Asia/Shanghai · UTC+8"),
        SpoofTimezoneOption("Asia/Hong_Kong", "Asia/Hong_Kong · UTC+8"),
        SpoofTimezoneOption("Asia/Taipei", "Asia/Taipei · UTC+8"),
        SpoofTimezoneOption("Asia/Singapore", "Asia/Singapore · UTC+8"),
        SpoofTimezoneOption("Asia/Tokyo", "Asia/Tokyo · UTC+9"),
        SpoofTimezoneOption("Asia/Seoul", "Asia/Seoul · UTC+9"),
        SpoofTimezoneOption("Asia/Kolkata", "Asia/Kolkata · UTC+5:30"),
        SpoofTimezoneOption("Europe/London", "Europe/London · UTC+0"),
        SpoofTimezoneOption("Europe/Paris", "Europe/Paris · UTC+1"),
        SpoofTimezoneOption("Europe/Berlin", "Europe/Berlin · UTC+1"),
        SpoofTimezoneOption("Europe/Moscow", "Europe/Moscow · UTC+3"),
        SpoofTimezoneOption("America/New_York", "America/New_York · UTC-5"),
        SpoofTimezoneOption("America/Chicago", "America/Chicago · UTC-6"),
        SpoofTimezoneOption("America/Denver", "America/Denver · UTC-7"),
        SpoofTimezoneOption("America/Los_Angeles", "America/Los_Angeles · UTC-8"),
        SpoofTimezoneOption("America/Sao_Paulo", "America/Sao_Paulo · UTC-3"),
        SpoofTimezoneOption("Australia/Sydney", "Australia/Sydney · UTC+10"),
        SpoofTimezoneOption("Pacific/Auckland", "Pacific/Auckland · UTC+12")
    )

    val screens = listOf(
        SpoofScreenOption(1920, 1080, 1.0f),
        SpoofScreenOption(1366, 768, 1.0f),
        SpoofScreenOption(1536, 864, 1.25f),
        SpoofScreenOption(1440, 900, 1.0f),
        SpoofScreenOption(1600, 900, 1.0f),
        SpoofScreenOption(1280, 800, 1.0f),
        SpoofScreenOption(2560, 1440, 1.5f),
        SpoofScreenOption(3840, 2160, 1.5f)
    )

    /** 由 BCP-47 主语言标签推导 navigator.languages（["en-US", "en"]）。 */
    fun languageList(tag: String): List<String> {
        val base = tag.substringBefore('-')
        return if (base == tag) listOf(tag) else listOf(tag, base)
    }

    fun isValidLanguageTag(tag: String): Boolean {
        return tag.matches(Regex("^[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})*$"))
    }

    fun isValidTimezoneId(id: String): Boolean {
        return java.util.TimeZone.getTimeZone(id).id == id
    }
}
