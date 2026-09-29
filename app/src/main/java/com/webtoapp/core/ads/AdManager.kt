package com.webtoapp.core.ads

import android.content.Context
import android.util.Log
import com.webtoapp.core.ads.api.AdsCompletion
import com.webtoapp.core.ads.api.AdsFeatureApi
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.AdConfig

/**
 * Thin gate in front of the injected ad feature stack (issue #1115).
 *
 * The real implementation lives in `com.webtoapp.stack.admob.AdsFeature`,
 * compiled into the `admob` stack DEX and grafted into the generated APK only
 * when ads are enabled. When the stack DEX is absent (feature off, or the host
 * preview where no stack is injected) every method degrades to a no-op — the
 * template carries zero ad-SDK classes.
 */
class AdManager(private val context: Context) {

    private var isInitialized = false
    private var adConfig: AdConfig? = null

    @Volatile
    private var feature: AdsFeatureApi? = null

    @Volatile
    private var featureResolved = false

    fun initialize(config: AdConfig) {
        if (isInitialized) return
        adConfig = config
        resolveFeature()?.let {
            it.initialize(context, config.appId, config.testMode)
        }
        Log.d(
            TAG,
            "AdManager initialized: banner=${config.bannerEnabled}, " +
                "interstitial=${config.interstitialEnabled}, " +
                "splash=${config.splashEnabled}, feature=${feature != null}"
        )
        isInitialized = true
    }

    fun showBannerAd(container: android.view.ViewGroup) {
        val config = adConfig ?: return
        if (!config.bannerEnabled || config.bannerId.isBlank()) return
        val view = resolveFeature()?.createBanner(context, config.bannerId) ?: return
        container.removeAllViews()
        container.addView(view)
        container.visibility = android.view.View.VISIBLE
    }

    fun loadInterstitialAd(onLoaded: () -> Unit, onFailed: (String) -> Unit) {
        val config = adConfig ?: return
        if (!config.interstitialEnabled || config.interstitialId.isBlank()) {
            onFailed(Strings.interstitialAdNotConfigured)
            return
        }
        if (resolveFeature() == null) {
            onFailed(Strings.adSdkNotIntegrated)
            return
        }
        // The stack implementation loads on show; nothing to prefetch here.
        onLoaded()
    }

    fun showInterstitialAd(activity: android.app.Activity, onDismissed: () -> Unit) {
        val config = adConfig
        val api = resolveFeature()
        if (config == null || !config.interstitialEnabled || api == null) {
            onDismissed()
            return
        }
        api.showInterstitial(
            activity, config.interstitialId,
            AdsCompletion { onDismissed() }
        )
    }

    fun showSplashAd(
        activity: android.app.Activity,
        container: android.view.ViewGroup,
        onFinished: () -> Unit,
        onSkipped: () -> Unit
    ) {
        val config = adConfig ?: run {
            onFinished()
            return
        }
        val api = resolveFeature()
        if (!config.splashEnabled || config.splashId.isBlank() || api == null) {
            onFinished()
            return
        }
        api.showSplashAd(
            activity, config.splashId, config.splashDuration,
            AdsCompletion { onFinished() }
        )
    }

    fun destroy() {
        runCatching { feature?.destroy() }
        adConfig = null
        isInitialized = false
    }

    /**
     * Resolve the injected stack once. The template's `AdsFeatureApi` shadows
     * the copy inside the stack DEX on the shared PathClassLoader, so the
     * `as` cast is safe when the DEX is present.
     */
    private fun resolveFeature(): AdsFeatureApi? {
        if (!featureResolved) {
            featureResolved = true
            feature = runCatching {
                Class.forName(STACK_IMPL_CLASS)
                    .getDeclaredConstructor()
                    .newInstance() as? AdsFeatureApi
            }.onFailure {
                Log.d(TAG, "no ad feature stack injected")
            }.getOrNull()
        }
        return feature
    }

    companion object {
        private const val TAG = "AdManager"
        private const val STACK_IMPL_CLASS = "com.webtoapp.stack.admob.AdsFeature"
    }

    fun isAdReady(adType: AdType): Boolean {
        val config = adConfig ?: return false
        if (resolveFeature() == null) return false
        return when (adType) {
            AdType.BANNER -> config.bannerEnabled && config.bannerId.isNotBlank()
            AdType.INTERSTITIAL -> config.interstitialEnabled && config.interstitialId.isNotBlank()
            AdType.SPLASH -> config.splashEnabled && config.splashId.isNotBlank()
        }
    }
}

enum class AdType {
    BANNER,
    INTERSTITIAL,
    SPLASH
}

interface AdCallback {
    fun onAdLoaded()
    fun onAdFailed(error: String)
    fun onAdClicked()
    fun onAdClosed()
}
