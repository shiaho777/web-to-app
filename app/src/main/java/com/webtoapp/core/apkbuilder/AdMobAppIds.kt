package com.webtoapp.core.apkbuilder

/**
 * The check [com.google.android.gms.ads.MobileAdsInitProvider] runs in
 * `attachInfo`, before any app code. A value that fails it kills the process
 * on launch. Same pattern as play-services-ads-api 24.8.0.
 */
internal object AdMobAppIds {
    private val PATTERN = Regex("^ca-app-pub-[0-9]{16}~[0-9]{10}$")

    /** Google's public sample App ID. Valid for the provider, paired with test ad units. */
    const val SAMPLE = "ca-app-pub-3940256099942544~3347511713"

    fun isValid(id: String): Boolean = PATTERN.matches(id.trim())

    /**
     * App ID to write into the manifest, or null when the ads stack must not
     * be grafted. Test mode with a blank or malformed ID uses [SAMPLE] so the
     * provider does not crash.
     */
    fun manifestValue(configured: String, testMode: Boolean): String? {
        val trimmed = configured.trim()
        if (isValid(trimmed)) return trimmed
        if (testMode) return SAMPLE
        return null
    }
}
