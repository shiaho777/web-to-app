package com.webtoapp.stack.admob

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.webtoapp.core.ads.api.AdsCompletion
import com.webtoapp.core.ads.api.AdsFeatureApi

/**
 * AdMob (play-services-ads-lite) implementation of [AdsFeatureApi].
 *
 * Compiled into the `admob` feature-stack DEX and injected into generated APKs
 * at export time; the template reaches it through `Class.forName`. Loaded only
 * when the app's config enables ads — the class itself must stay dependency
 * clean except for the GMS ads and UMP consent APIs.
 *
 * Consent: every ad request is gated on Google's UMP flow (EU consent policy).
 * `initialize` runs `requestConsentInfoUpdate` → `loadAndShowConsentFormIfRequired`;
 * ad loads issued before consent resolves are queued and then either run
 * (canRequestAds) or fail-soft through their normal callbacks. `testMode`
 * additionally forces EEA debug geography so the consent form is verifiable on
 * emulators regardless of region.
 */
class AdsFeature : AdsFeatureApi {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val consentLock = Any()

    @Volatile
    private var testMode = false

    /** initialize() was called and the consent flow started. */
    @Volatile
    private var started = false

    /** Consent flow finished (allowed, denied, failed, or timed out). */
    @Volatile
    private var consentResolved = false

    /** Final answer: ad requests may proceed (implies MobileAds init ran). */
    @Volatile
    private var adsAllowed = false

    private val pendingAdOps = mutableListOf<Runnable>()

    private val consentWatchdog = Runnable {
        synchronized(consentLock) {
            if (consentResolved) return@Runnable
        }
        finishConsent(null, allowed = false)
    }

    override fun initialize(context: Context, appId: String, testMode: Boolean) {
        this.testMode = testMode
        synchronized(consentLock) {
            if (started) return
            started = true
        }
        if (appId.isBlank() && !testMode) {
            finishConsent(null, allowed = false)
            return
        }
        gatherConsent(context)
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
                whenAdsAllowed(
                    allowed = { runCatching { loadAd(AdRequest.Builder().build()) } },
                    denied = { /* banner space stays empty */ }
                )
            }
        } catch (_: Throwable) {
            null
        }
    }

    override fun showInterstitial(activity: Activity, adUnitId: String, onDone: AdsCompletion) {
        val id = unitId(adUnitId, AdsFeatureApi.TEST_INTERSTITIAL)
        if (id.isBlank()) {
            onDone.onDone()
            return
        }
        whenAdsAllowed(
            allowed = {
                try {
                    InterstitialAd.load(
                        activity, id, AdRequest.Builder().build(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(ad: InterstitialAd) {
                                ad.fullScreenContentCallback = FinishOnDismiss { onDone.onDone() }
                                ad.show(activity)
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                onDone.onDone()
                            }
                        }
                    )
                } catch (_: Throwable) {
                    onDone.onDone()
                }
            },
            denied = { onDone.onDone() }
        )
    }

    override fun showSplashAd(
        activity: Activity,
        adUnitId: String,
        timeoutSeconds: Int,
        onDone: AdsCompletion
    ) {
        val id = unitId(adUnitId, AdsFeatureApi.TEST_SPLASH)
        if (id.isBlank()) {
            onDone.onDone()
            return
        }
        var finished = false
        fun finish() {
            if (finished) return
            finished = true
            mainHandler.post { onDone.onDone() }
        }
        mainHandler.postDelayed({ finish() }, timeoutSeconds.coerceAtLeast(1) * 1000L)
        whenAdsAllowed(
            allowed = {
                try {
                    AppOpenAd.load(
                        activity, id, AdRequest.Builder().build(),
                        object : AppOpenAd.AppOpenAdLoadCallback() {
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
            },
            denied = { finish() }
        )
    }

    override fun destroy() {
        synchronized(consentLock) {
            started = false
            consentResolved = false
            pendingAdOps.clear()
        }
        adsAllowed = false
        mainHandler.removeCallbacks(consentWatchdog)
    }

    // -- consent ---------------------------------------------------------------

    private fun gatherConsent(context: Context) {
        val activity = findActivity(context)
        if (activity == null) {
            // UMP needs an Activity to render the consent form; without one we
            // cannot ask, so proceed (non-consent regions still get ads).
            finishConsent(context, allowed = true)
            return
        }
        mainHandler.postDelayed(consentWatchdog, CONSENT_TIMEOUT_MS)
        runOnMain {
            try {
                val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
                consentInfo.requestConsentInfoUpdate(
                    activity,
                    consentParams(activity),
                    {
                        try {
                            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                                finishConsent(context, consentInfo.canRequestAds())
                            }
                        } catch (_: Throwable) {
                            finishConsent(context, consentInfo.canRequestAds())
                        }
                    },
                    {
                        // Update failed (offline, missing GMS): canRequestAds
                        // still reports the cached/previous state.
                        finishConsent(context, consentInfo.canRequestAds())
                    }
                )
            } catch (_: Throwable) {
                finishConsent(context, allowed = true)
            }
        }
    }

    private fun consentParams(activity: Activity): ConsentRequestParameters {
        val builder = ConsentRequestParameters.Builder()
        if (testMode) {
            // Force the EEA consent path on emulators so the form is
            // verifiable in test builds regardless of real region.
            builder.setConsentDebugSettings(
                ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .build()
            )
        }
        return builder.build()
    }

    private fun finishConsent(context: Context?, allowed: Boolean) {
        mainHandler.removeCallbacks(consentWatchdog)
        adsAllowed = allowed
        if (allowed && context != null) initSdk(context)
        val ops = synchronized(consentLock) {
            consentResolved = true
            pendingAdOps.toList().also { pendingAdOps.clear() }
        }
        ops.forEach { it.run() }
    }

    private fun initSdk(context: Context) {
        runOnMain {
            try {
                MobileAds.initialize(context) { }
            } catch (_: Throwable) {
                // Missing GMS / blocked devices: ads simply never render.
                adsAllowed = false
            }
        }
    }

    /** Run [allowed] after consent resolves; falls back to [denied]. */
    private fun whenAdsAllowed(allowed: () -> Unit, denied: () -> Unit) {
        val op = Runnable { if (adsAllowed) runOnMain(allowed) else runOnMain(denied) }
        synchronized(consentLock) {
            // initialize() was never called (blank config): no consent can be
            // gathered — resolve denied now instead of queueing forever.
            if (!started) consentResolved = true
            if (!consentResolved) {
                pendingAdOps += op
                return
            }
        }
        op.run()
    }

    private fun findActivity(context: Context): Activity? {
        var c: Context? = context
        while (c is ContextWrapper) {
            if (c is Activity) return c
            c = c.baseContext
        }
        return null
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

    companion object {
        /** UMP must not stall ad plumbing forever (offline/hung form). */
        private const val CONSENT_TIMEOUT_MS = 15_000L
    }
}
