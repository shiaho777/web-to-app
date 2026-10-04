package com.webtoapp.core.privacy

import kotlin.random.Random

object FingerprintGenerator {

    private data class BrowserProfile(
        val userAgent: String,
        val platform: String,
        val vendor: String,
        val appVersion: String,

        val chUa: String,
        val chUaPlatform: String,
        val chUaMobile: String,
        val chUaPlatformVersion: String,
        val chUaFullVersion: String,
        val chUaModel: String,
        val chUaArch: String,
        val chUaBitness: String,

        val webglVendor: String,
        val webglRenderer: String,

        val maxTouchPoints: Int,
        val colorDepth: Int,

        val browserType: BrowserType
    )

    enum class BrowserType { CHROME, FIREFOX, SAFARI, EDGE }

    // Phone profiles only. These strings are written onto the real request
    // (User-Agent + Sec-CH-UA-Mobile) and onto navigator.*. A desktop profile
    // makes the destination site serve its desktop page. Every UA contains
    // "Mobile" so a previously saved desktop fingerprint can be recognised.
    private val profiles = listOf(

        BrowserProfile(
            userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
            platform = "Linux armv8l", vendor = "Google Inc.",
            appVersion = "5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
            chUa = "\"Chromium\";v=\"131\", \"Google Chrome\";v=\"131\", \"Not_A Brand\";v=\"24\"",
            chUaPlatform = "\"Android\"", chUaMobile = "?1", chUaPlatformVersion = "\"14.0.0\"",
            chUaFullVersion = "\"131.0.6778.86\"", chUaModel = "\"Pixel 8\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (Qualcomm)", webglRenderer = "ANGLE (Qualcomm, Adreno (TM) 740, OpenGL ES 3.2)",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.CHROME
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36",
            platform = "Linux armv8l", vendor = "Google Inc.",
            appVersion = "5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36",
            chUa = "\"Chromium\";v=\"130\", \"Google Chrome\";v=\"130\", \"Not?A_Brand\";v=\"99\"",
            chUaPlatform = "\"Android\"", chUaMobile = "?1", chUaPlatformVersion = "\"14.0.0\"",
            chUaFullVersion = "\"130.0.6723.117\"", chUaModel = "\"Pixel 7\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (Qualcomm)", webglRenderer = "ANGLE (Qualcomm, Adreno (TM) 730, OpenGL ES 3.2)",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.CHROME
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.36",
            platform = "Linux armv8l", vendor = "Google Inc.",
            appVersion = "5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.36",
            chUa = "\"Chromium\";v=\"129\", \"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\"",
            chUaPlatform = "\"Android\"", chUaMobile = "?1", chUaPlatformVersion = "\"14.0.0\"",
            chUaFullVersion = "\"129.0.6668.100\"", chUaModel = "\"SM-S918B\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (Qualcomm)", webglRenderer = "ANGLE (Qualcomm, Adreno (TM) 740, OpenGL ES 3.2)",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.CHROME
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36",
            platform = "Linux armv8l", vendor = "Google Inc.",
            appVersion = "5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36",
            chUa = "\"Chromium\";v=\"128\", \"Google Chrome\";v=\"128\", \"Not;A=Brand\";v=\"24\"",
            chUaPlatform = "\"Android\"", chUaMobile = "?1", chUaPlatformVersion = "\"13.0.0\"",
            chUaFullVersion = "\"128.0.6613.137\"", chUaModel = "\"Pixel 6\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (ARM)", webglRenderer = "ANGLE (ARM, Mali-G78, OpenGL ES 3.2)",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.CHROME
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
            platform = "Linux armv8l", vendor = "Google Inc.",
            appVersion = "5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
            chUa = "\"Chromium\";v=\"131\", \"Google Chrome\";v=\"131\", \"Not_A Brand\";v=\"24\"",
            chUaPlatform = "\"Android\"", chUaMobile = "?1", chUaPlatformVersion = "\"15.0.0\"",
            chUaFullVersion = "\"131.0.6778.135\"", chUaModel = "\"Pixel 9\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (ARM)", webglRenderer = "ANGLE (ARM, Mali-G715, OpenGL ES 3.2)",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.CHROME
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Linux; Android 14; SM-S931B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/27.0 Chrome/131.0.0.0 Mobile Safari/537.36",
            platform = "Linux armv8l", vendor = "Google Inc.",
            appVersion = "5.0 (Linux; Android 14; SM-S931B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/27.0 Chrome/131.0.0.0 Mobile Safari/537.36",
            chUa = "\"Chromium\";v=\"131\", \"Samsung Internet\";v=\"27\", \"Not_A Brand\";v=\"24\"",
            chUaPlatform = "\"Android\"", chUaMobile = "?1", chUaPlatformVersion = "\"14.0.0\"",
            chUaFullVersion = "\"131.0.6778.86\"", chUaModel = "\"SM-S931B\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (Qualcomm)", webglRenderer = "ANGLE (Qualcomm, Adreno (TM) 750, OpenGL ES 3.2)",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.CHROME
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (Android 14; Mobile; rv:133.0) Gecko/133.0 Firefox/133.0",
            platform = "Linux armv8l", vendor = "",
            appVersion = "5.0 (Android 14)",
            chUa = "", chUaPlatform = "", chUaMobile = "", chUaPlatformVersion = "",
            chUaFullVersion = "", chUaModel = "", chUaArch = "", chUaBitness = "",
            webglVendor = "Qualcomm", webglRenderer = "Adreno (TM) 740",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.FIREFOX
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Android 13; Mobile; rv:132.0) Gecko/132.0 Firefox/132.0",
            platform = "Linux armv8l", vendor = "",
            appVersion = "5.0 (Android 13)",
            chUa = "", chUaPlatform = "", chUaMobile = "", chUaPlatformVersion = "",
            chUaFullVersion = "", chUaModel = "", chUaArch = "", chUaBitness = "",
            webglVendor = "ARM", webglRenderer = "Mali-G710",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.FIREFOX
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Mobile/15E148 Safari/604.1",
            platform = "iPhone", vendor = "Apple Computer, Inc.",
            appVersion = "5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Mobile/15E148 Safari/604.1",
            chUa = "", chUaPlatform = "", chUaMobile = "", chUaPlatformVersion = "",
            chUaFullVersion = "", chUaModel = "", chUaArch = "", chUaBitness = "",
            webglVendor = "Apple Inc.", webglRenderer = "Apple GPU",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.SAFARI
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.6 Mobile/15E148 Safari/604.1",
            platform = "iPhone", vendor = "Apple Computer, Inc.",
            appVersion = "5.0 (iPhone; CPU iPhone OS 17_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.6 Mobile/15E148 Safari/604.1",
            chUa = "", chUaPlatform = "", chUaMobile = "", chUaPlatformVersion = "",
            chUaFullVersion = "", chUaModel = "", chUaArch = "", chUaBitness = "",
            webglVendor = "Apple Inc.", webglRenderer = "Apple GPU",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.SAFARI
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36 EdgA/131.0.2903.70",
            platform = "Linux armv8l", vendor = "Google Inc.",
            appVersion = "5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36 EdgA/131.0.2903.70",
            chUa = "\"Chromium\";v=\"131\", \"Microsoft Edge\";v=\"131\", \"Not_A Brand\";v=\"24\"",
            chUaPlatform = "\"Android\"", chUaMobile = "?1", chUaPlatformVersion = "\"14.0.0\"",
            chUaFullVersion = "\"131.0.2903.70\"", chUaModel = "\"Pixel 8\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (Qualcomm)", webglRenderer = "ANGLE (Qualcomm, Adreno (TM) 740, OpenGL ES 3.2)",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.EDGE
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36 EdgA/130.0.2849.80",
            platform = "Linux armv8l", vendor = "Google Inc.",
            appVersion = "5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36 EdgA/130.0.2849.80",
            chUa = "\"Chromium\";v=\"130\", \"Microsoft Edge\";v=\"130\", \"Not?A_Brand\";v=\"99\"",
            chUaPlatform = "\"Android\"", chUaMobile = "?1", chUaPlatformVersion = "\"14.0.0\"",
            chUaFullVersion = "\"130.0.2849.80\"", chUaModel = "\"Pixel 7\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (ARM)", webglRenderer = "ANGLE (ARM, Mali-G715, OpenGL ES 3.2)",
            maxTouchPoints = 5, colorDepth = 24, browserType = BrowserType.EDGE
        )
    )

    private val languages = listOf(
        "zh-CN,zh;q=0.9,en;q=0.8",
        "zh-TW,zh;q=0.9,en;q=0.8",
        "en-US,en;q=0.9",
        "en-GB,en;q=0.9",
        "ja-JP,ja;q=0.9,en;q=0.8",
        "ko-KR,ko;q=0.9,en;q=0.8",
        "de-DE,de;q=0.9,en;q=0.8",
        "fr-FR,fr;q=0.9,en;q=0.8",
        "es-ES,es;q=0.9,en;q=0.8",
        "pt-BR,pt;q=0.9,en;q=0.8",
        "ru-RU,ru;q=0.9,en;q=0.8"
    )

    private val timezones = listOf(
        "Asia/Shanghai", "Asia/Tokyo", "Asia/Seoul", "Asia/Singapore",
        "Asia/Hong_Kong", "Asia/Taipei", "Asia/Kolkata",
        "America/New_York", "America/Chicago", "America/Denver",
        "America/Los_Angeles", "America/Toronto", "America/Sao_Paulo",
        "Europe/London", "Europe/Paris", "Europe/Berlin", "Europe/Moscow",
        "Australia/Sydney", "Pacific/Auckland"
    )

    // CSS pixels of common phones, portrait. Width stays under the 768px
    // breakpoint sites use to switch to a desktop layout.
    private val screenResolutions = listOf(
        Pair(360, 800), Pair(384, 854), Pair(390, 844),
        Pair(393, 852), Pair(412, 915), Pair(412, 892),
        Pair(360, 780), Pair(430, 932), Pair(393, 873),
        Pair(360, 640)
    )

    private val hardwareConcurrencyOptions = listOf(4, 6, 8, 10, 12, 16, 20, 24)
    private val deviceMemoryOptions = listOf(4, 8, 16, 32)

    fun generateFingerprint(seed: String? = null): GeneratedFingerprint {
        val random = if (seed != null) Random(seed.hashCode().toLong()) else Random

        val profile = profiles[random.nextInt(profiles.size)]
        val resolution = screenResolutions[random.nextInt(screenResolutions.size)]
        val language = languages[random.nextInt(languages.size)]
        val timezone = timezones[random.nextInt(timezones.size)]

        return GeneratedFingerprint(
            userAgent = profile.userAgent,
            platform = profile.platform,
            vendor = profile.vendor,
            appVersion = profile.appVersion,
            language = language,
            timezone = timezone,
            screenWidth = resolution.first,
            screenHeight = resolution.second,
            colorDepth = profile.colorDepth,
            hardwareConcurrency = hardwareConcurrencyOptions[random.nextInt(hardwareConcurrencyOptions.size)],
            deviceMemory = deviceMemoryOptions[random.nextInt(deviceMemoryOptions.size)],
            maxTouchPoints = profile.maxTouchPoints,
            canvasNoiseSeed = random.nextLong(),
            audioNoiseSeed = random.nextLong(),
            webglVendor = profile.webglVendor,
            webglRenderer = profile.webglRenderer,

            chUa = profile.chUa,
            chUaPlatform = profile.chUaPlatform,
            chUaMobile = profile.chUaMobile,
            chUaPlatformVersion = profile.chUaPlatformVersion,
            chUaFullVersion = profile.chUaFullVersion,
            chUaModel = profile.chUaModel,
            chUaArch = profile.chUaArch,
            chUaBitness = profile.chUaBitness,
            browserType = profile.browserType.name
        )
    }
}

data class GeneratedFingerprint(
    val userAgent: String,
    val platform: String,
    val vendor: String,
    val appVersion: String,
    val language: String,
    val timezone: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val colorDepth: Int,
    val hardwareConcurrency: Int,
    val deviceMemory: Int,
    val maxTouchPoints: Int,

    val canvasNoiseSeed: Long,
    val audioNoiseSeed: Long,

    val webglVendor: String,
    val webglRenderer: String,

    val chUa: String,
    val chUaPlatform: String,
    val chUaMobile: String,
    val chUaPlatformVersion: String,
    val chUaFullVersion: String,
    val chUaModel: String,
    val chUaArch: String,
    val chUaBitness: String,

    val browserType: String
) {

    val canvasNoise: Float get() = (canvasNoiseSeed % 10000) / 100000000f
    val audioNoise: Float get() = (audioNoiseSeed % 10000) / 100000000f

    /** True when this identity asks sites for the mobile page. */
    fun identifiesAsPhone(): Boolean = userAgent.contains("Mobile")
}
