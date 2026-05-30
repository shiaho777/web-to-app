package com.webtoapp.ui.webview

import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.ensureWebUrlScheme
import com.webtoapp.util.normalizeExternalIntentUrl

internal fun normalizeWebUrlForSecurity(rawUrl: String?): String {
    val trimmed = rawUrl?.trim().orEmpty()

    return ensureWebUrlScheme(trimmed)
}

internal fun normalizeExternalUrlForIntent(rawUrl: String): String {
    val safeUrl = normalizeExternalIntentUrl(rawUrl)
    if (safeUrl.isEmpty()) {
        AppLogger.w("WebViewActivity", "Blocked invalid or dangerous external URL: $rawUrl")
        return ""
    }
    return normalizeWebUrlForSecurity(safeUrl)
}

internal fun hasConfiguredAds(app: WebApp): Boolean {
    val config = app.adConfig
    return app.adsEnabled ||
        config?.bannerId?.isNotBlank() == true ||
        config?.interstitialId?.isNotBlank() == true ||
        config?.splashId?.isNotBlank() == true
}
