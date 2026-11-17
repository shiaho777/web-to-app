package com.webtoapp.core.webview

import android.webkit.WebView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.webtoapp.core.logging.AppLogger

object SiteWebProfiles {
    private const val TAG = "SiteWebProfiles"

    fun profileNameFor(siteId: String): String? {
        val sanitized = siteId.filter {
            it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '_' || it == '-'
        }
        if (sanitized.isBlank()) return null
        return "mw_$sanitized"
    }

    fun applyTo(webView: WebView, profileName: String?) {
        if (profileName == null) return
        if (!isSupported()) {
            AppLogger.w(TAG, "MULTI_PROFILE unsupported by this WebView; site sessions share cookies")
            return
        }
        try {
            WebViewCompat.setProfile(webView, profileName)
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to apply site profile $profileName", e)
        }
    }

    fun isSupported(): Boolean = WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)
}