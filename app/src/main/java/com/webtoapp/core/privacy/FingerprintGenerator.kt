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

    private val profiles = listOf(

        BrowserProfile(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            platform = "Win32", vendor = "Google Inc.",
            appVersion = "5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            chUa = "\"Chromium\";v=\"131\", \"Google Chrome\";v=\"131\", \"Not_A Brand\";v=\"24\"",
            chUaPlatform = "\"Windows\"", chUaMobile = "?0", chUaPlatformVersion = "\"15.0.0\"",
            chUaFullVersion = "\"131.0.6778.86\"", chUaModel = "\"\"",
            chUaArch = "\"x86\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (NVIDIA)", webglRenderer = "ANGLE (NVIDIA, NVIDIA GeForce RTX 4060 Direct3D11 vs_5_0 ps_5_0, D3D11)",
            maxTouchPoints = 0, colorDepth = 24, browserType = BrowserType.CHROME
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
            platform = "Win32", vendor = "Google Inc.",
            appVersion = "5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
            chUa = "\"Chromium\";v=\"130\", \"Google Chrome\";v=\"130\", \"Not?A_Brand\";v=\"99\"",
            chUaPlatform = "\"Windows\"", chUaMobile = "?0", chUaPlatformVersion = "\"15.0.0\"",
            chUaFullVersion = "\"130.0.6723.117\"", chUaModel = "\"\"",
            chUaArch = "\"x86\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (Intel)", webglRenderer = "ANGLE (Intel, Intel(R) UHD Graphics 770 Direct3D11 vs_5_0 ps_5_0, D3D11)",
            maxTouchPoints = 0, colorDepth = 24, browserType = BrowserType.CHROME
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
            platform = "Win32", vendor = "Google Inc.",
            appVersion = "5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
            chUa = "\"Chromium\";v=\"129\", \"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\"",
            chUaPlatform = "\"Windows\"", chUaMobile = "?0", chUaPlatformVersion = "\"10.0.0\"",
            chUaFullVersion = "\"129.0.6668.100\"", chUaModel = "\"\"",
            chUaArch = "\"x86\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (AMD)", webglRenderer = "ANGLE (AMD, AMD Radeon RX 7800 XT Direct3D11 vs_5_0 ps_5_0, D3D11)",
            maxTouchPoints = 0, colorDepth = 24, browserType = BrowserType.CHROME
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
            platform = "Win32", vendor = "Google Inc.",
            appVersion = "5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
            chUa = "\"Chromium\";v=\"128\", \"Google Chrome\";v=\"128\", \"Not;A=Brand\";v=\"24\"",
            chUaPlatform = "\"Windows\"", chUaMobile = "?0", chUaPlatformVersion = "\"10.0.0\"",
            chUaFullVersion = "\"128.0.6613.137\"", chUaModel = "\"\"",
            chUaArch = "\"x86\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (NVIDIA)", webglRenderer = "ANGLE (NVIDIA, NVIDIA GeForce GTX 1660 SUPER Direct3D11 vs_5_0 ps_5_0, D3D11)",
            maxTouchPoints = 0, colorDepth = 24, browserType = BrowserType.CHROME
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            platform = "MacIntel", vendor = "Google Inc.",
            appVersion = "5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            chUa = "\"Chromium\";v=\"131\", \"Google Chrome\";v=\"131\", \"Not_A Brand\";v=\"24\"",
            chUaPlatform = "\"macOS\"", chUaMobile = "?0", chUaPlatformVersion = "\"14.7.1\"",
            chUaFullVersion = "\"131.0.6778.86\"", chUaModel = "\"\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (Apple)", webglRenderer = "ANGLE (Apple, Apple M2, OpenGL 4.1)",
            maxTouchPoints = 0, colorDepth = 30, browserType = BrowserType.CHROME
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
            platform = "MacIntel", vendor = "Google Inc.",
            appVersion = "5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
            chUa = "\"Chromium\";v=\"130\", \"Google Chrome\";v=\"130\", \"Not?A_Brand\";v=\"99\"",
            chUaPlatform = "\"macOS\"", chUaMobile = "?0", chUaPlatformVersion = "\"14.6.0\"",
            chUaFullVersion = "\"130.0.6723.117\"", chUaModel = "\"\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (Apple)", webglRenderer = "ANGLE (Apple, Apple M1 Pro, OpenGL 4.1)",
            maxTouchPoints = 0, colorDepth = 30, browserType = BrowserType.CHROME
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            platform = "Linux x86_64", vendor = "Google Inc.",
            appVersion = "5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            chUa = "\"Chromium\";v=\"131\", \"Google Chrome\";v=\"131\", \"Not_A Brand\";v=\"24\"",
            chUaPlatform = "\"Linux\"", chUaMobile = "?0", chUaPlatformVersion = "\"6.8.0\"",
            chUaFullVersion = "\"131.0.6778.86\"", chUaModel = "\"\"",
            chUaArch = "\"x86\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (NVIDIA Corporation)", webglRenderer = "ANGLE (NVIDIA Corporation, NVIDIA GeForce RTX 3070/PCIe/SSE2, OpenGL 4.5)",
            maxTouchPoints = 0, colorDepth = 24, browserType = BrowserType.CHROME
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0",
            platform = "Win32", vendor = "",
            appVersion = "5.0 (Windows)",
            chUa = "", chUaPlatform = "", chUaMobile = "", chUaPlatformVersion = "",
            chUaFullVersion = "", chUaModel = "", chUaArch = "", chUaBitness = "",
            webglVendor = "Mozilla", webglRenderer = "Mozilla -- ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Ti Direct3D11 vs_5_0 ps_5_0, D3D11)",
            maxTouchPoints = 0, colorDepth = 24, browserType = BrowserType.FIREFOX
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:132.0) Gecko/20100101 Firefox/132.0",
            platform = "Win32", vendor = "",
            appVersion = "5.0 (Windows)",
            chUa = "", chUaPlatform = "", chUaMobile = "", chUaPlatformVersion = "",
            chUaFullVersion = "", chUaModel = "", chUaArch = "", chUaBitness = "",
            webglVendor = "Mozilla", webglRenderer = "Mozilla -- ANGLE (Intel, Intel(R) UHD Graphics 630 Direct3D11 vs_5_0 ps_5_0, D3D11)",
            maxTouchPoints = 0, colorDepth = 24, browserType = BrowserType.FIREFOX
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:133.0) Gecko/20100101 Firefox/133.0",
            platform = "MacIntel", vendor = "",
            appVersion = "5.0 (Macintosh)",
            chUa = "", chUaPlatform = "", chUaMobile = "", chUaPlatformVersion = "",
            chUaFullVersion = "", chUaModel = "", chUaArch = "", chUaBitness = "",
            webglVendor = "Mozilla", webglRenderer = "Mozilla -- Apple M2 -- Apple GPU",
            maxTouchPoints = 0, colorDepth = 30, browserType = BrowserType.FIREFOX
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:133.0) Gecko/20100101 Firefox/133.0",
            platform = "Linux x86_64", vendor = "",
            appVersion = "5.0 (X11)",
            chUa = "", chUaPlatform = "", chUaMobile = "", chUaPlatformVersion = "",
            chUaFullVersion = "", chUaModel = "", chUaArch = "", chUaBitness = "",
            webglVendor = "Mozilla", webglRenderer = "Mozilla -- NVIDIA Corporation NVIDIA GeForce RTX 3070/PCIe/SSE2 -- OpenGL",
            maxTouchPoints = 0, colorDepth = 24, browserType = BrowserType.FIREFOX
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Safari/605.1.15",
            platform = "MacIntel", vendor = "Apple Computer, Inc.",
            appVersion = "5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Safari/605.1.15",
            chUa = "", chUaPlatform = "", chUaMobile = "", chUaPlatformVersion = "",
            chUaFullVersion = "", chUaModel = "", chUaArch = "", chUaBitness = "",
            webglVendor = "Apple Inc.", webglRenderer = "Apple M2 Pro",
            maxTouchPoints = 0, colorDepth = 30, browserType = BrowserType.SAFARI
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.6 Safari/605.1.15",
            platform = "MacIntel", vendor = "Apple Computer, Inc.",
            appVersion = "5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.6 Safari/605.1.15",
            chUa = "", chUaPlatform = "", chUaMobile = "", chUaPlatformVersion = "",
            chUaFullVersion = "", chUaModel = "", chUaArch = "", chUaBitness = "",
            webglVendor = "Apple Inc.", webglRenderer = "Apple M1",
            maxTouchPoints = 0, colorDepth = 30, browserType = BrowserType.SAFARI
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.2903.70",
            platform = "Win32", vendor = "Google Inc.",
            appVersion = "5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.2903.70",
            chUa = "\"Chromium\";v=\"131\", \"Microsoft Edge\";v=\"131\", \"Not_A Brand\";v=\"24\"",
            chUaPlatform = "\"Windows\"", chUaMobile = "?0", chUaPlatformVersion = "\"15.0.0\"",
            chUaFullVersion = "\"131.0.2903.70\"", chUaModel = "\"\"",
            chUaArch = "\"x86\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (Intel)", webglRenderer = "ANGLE (Intel, Intel(R) Iris(R) Xe Graphics Direct3D11 vs_5_0 ps_5_0, D3D11)",
            maxTouchPoints = 0, colorDepth = 24, browserType = BrowserType.EDGE
        ),
        BrowserProfile(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.2849.80",
            platform = "Win32", vendor = "Google Inc.",
            appVersion = "5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.2849.80",
            chUa = "\"Chromium\";v=\"130\", \"Microsoft Edge\";v=\"130\", \"Not?A_Brand\";v=\"99\"",
            chUaPlatform = "\"Windows\"", chUaMobile = "?0", chUaPlatformVersion = "\"15.0.0\"",
            chUaFullVersion = "\"130.0.2849.80\"", chUaModel = "\"\"",
            chUaArch = "\"x86\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (NVIDIA)", webglRenderer = "ANGLE (NVIDIA, NVIDIA GeForce RTX 3080 Direct3D11 vs_5_0 ps_5_0, D3D11)",
            maxTouchPoints = 0, colorDepth = 24, browserType = BrowserType.EDGE
        ),

        BrowserProfile(
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.2903.70",
            platform = "MacIntel", vendor = "Google Inc.",
            appVersion = "5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.2903.70",
            chUa = "\"Chromium\";v=\"131\", \"Microsoft Edge\";v=\"131\", \"Not_A Brand\";v=\"24\"",
            chUaPlatform = "\"macOS\"", chUaMobile = "?0", chUaPlatformVersion = "\"14.7.1\"",
            chUaFullVersion = "\"131.0.2903.70\"", chUaModel = "\"\"",
            chUaArch = "\"arm\"", chUaBitness = "\"64\"",
            webglVendor = "Google Inc. (Apple)", webglRenderer = "ANGLE (Apple, Apple M3, OpenGL 4.1)",
            maxTouchPoints = 0, colorDepth = 30, browserType = BrowserType.EDGE
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

    private val screenResolutions = listOf(
        Pair(1920, 1080), Pair(2560, 1440), Pair(3840, 2160),
        Pair(1366, 768), Pair(1536, 864), Pair(1440, 900),
        Pair(1280, 720), Pair(1600, 900), Pair(2560, 1600),
        Pair(3440, 1440), Pair(1280, 800), Pair(1680, 1050)
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
}
