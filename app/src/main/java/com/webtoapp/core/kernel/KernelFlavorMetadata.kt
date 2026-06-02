package com.webtoapp.core.kernel

import android.webkit.WebView
import androidx.webkit.UserAgentMetadata
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.webtoapp.core.logging.AppLogger

object KernelFlavorMetadata {

    private const val TAG = "KernelFlavorMetadata"

    fun apply(webView: WebView, profile: KernelFlavorProfile) {
        if (profile.isNoOp) return

        if (!WebViewFeature.isFeatureSupported(WebViewFeature.USER_AGENT_METADATA)) {
            AppLogger.d(TAG, "USER_AGENT_METADATA not supported; relying on JS-layer client hints only")
            return
        }

        try {
            val builder = UserAgentMetadata.Builder()
                .setMobile(profile.mobile)
                .setPlatform(profile.platform)
                .setPlatformVersion(profile.platformVersion)
                .setBitness(profile.bitness.toIntOrNull() ?: UserAgentMetadata.BITNESS_DEFAULT)

            if (profile.architecture.isNotBlank()) {
                builder.setArchitecture(profile.architecture)
            }
            if (profile.model.isNotBlank()) {
                builder.setModel(profile.model)
            }
            if (profile.fullVersion.isNotBlank()) {
                builder.setFullVersion(profile.fullVersion)
            }

            if (profile.supportsClientHints && profile.brands.isNotEmpty()) {
                val brandList = profile.brands.map { b ->
                    UserAgentMetadata.BrandVersion.Builder()
                        .setBrand(b.brand)
                        .setMajorVersion(b.majorVersion)
                        .setFullVersion(b.fullVersion)
                        .build()
                }
                builder.setBrandVersionList(brandList)
            } else {
                builder.setBrandVersionList(emptyList())
            }

            WebSettingsCompat.setUserAgentMetadata(webView.settings, builder.build())
            AppLogger.d(TAG, "User-Agent metadata applied for ${profile.flavor.name} (clientHints=${profile.supportsClientHints})")
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to apply User-Agent metadata for ${profile.flavor.name}", e)
        }
    }
}
