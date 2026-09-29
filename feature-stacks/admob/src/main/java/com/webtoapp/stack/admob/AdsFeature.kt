package com.webtoapp.stack.admob

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.webtoapp.core.ads.api.AdsFeatureApi

/**
 * AdMob (play-services-ads-lite) implementation of [AdsFeatureApi].
 *
 * Compiled into the `admob` feature-stack DEX and injected into generated APKs
 * at export time; the template reaches it through `Class.forName`. Loaded only
 * when the app's config enables ads — the class itself must stay dependency
 * clean except for the GMS ads API.
 */
class AdsFeature : AdsFeatureApi {

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var initialized = false

    @Volatile
    private var testMode = false

    override fun initialize(context: Context, appId: String, testMode: Boolean) {
        this.testMode = testMode
        if (initialized) return
        if (appId.isBlank() && !testMode) return
        runOnMain {
            if (initialized) return@runOnMain
            try {
                MobileAds.initialize(context) { }
                initialized = true
            } catch (_: Throwable) {
                // Missing GMS / blocked devices: ads simply never render.
            }
        }
    }

    override fun createBanner(context: Context, adUnitId: String): View? {
        val id = unitId(adUnitId, AdsFeatureApi.TEST_BANNER)
        if (id.isBlank()) return null
        return try {
            AdView(context).apply {
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        context, AdSize.FULL_WIDTH
                    ) ?: AdSize.BANNER
                )
                this.adUnitId = id
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                loadAd(AdRequest.Builder().build())
            }
        } catch (_: Throwable) {
            null
        }
    }

    override fun showInterstitial(activity: Activity, adUnitId: String, onDone: () -> Unit) {
        val id = unitId(adUnitId, AdsFeatureApi.TEST_INTERSTITIAL)
        if (id.isBlank()) {
            onDone()
            return
        }
        runOnMain {
            try {
                InterstitialAd.load(
                    activity, id, AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            ad.fullScreenContentCallback = FinishOnDismiss(onDone)
                            ad.show(activity)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            onDone()
                        }
                    }
                )
            } catch (_: Throwable) {
                onDone()
            }
        }
    }

    override fun showSplashAd(
        activity: Activity,
        adUnitId: String,
        timeoutSeconds: Int,
        onDone: () -> Unit
    ) {
        val id = unitId(adUnitId, AdsFeatureApi.TEST_SPLASH)
        if (id.isBlank()) {
            onDone()
            return
        }
        var finished = false
        fun finish() {
            if (finished) return
            finished = true
            mainHandler.post { onDone() }
        }
        mainHandler.postDelayed({ finish() }, timeoutSeconds.coerceAtLeast(1) * 1000L)
        runOnMain {
            try {
                AppOpenAd.load(
                    activity, id, AdRequest.Builder().build(),
                    object : com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            ad.fullScreenContentCallback = FinishOnDismiss { finish() }
                            ad.show(activity)
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            finish()
                        }
                    }
                )
            } catch (_: Throwable) {
                finish()
            }
        }
    }

    override fun destroy() {
        initialized = false
    }

    private fun unitId(configured: String, testId: String): String =
        if (testMode) testId else configured

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block()
        else mainHandler.post(block)
    }

    private class FinishOnDismiss(private val onDone: () -> Unit) :
        FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() = onDone()
        override fun onAdFailedToShowFullScreenContent(error: AdError) = onDone()
    }
}
