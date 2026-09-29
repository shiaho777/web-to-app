package com.webtoapp.core.ads.api

import android.app.Activity
import android.content.Context
import android.view.View

/**
 * Contract between the shell template's [com.webtoapp.core.ads.AdManager] and an
 * injected ad feature stack (issue #1115).
 *
 * The interface lives in the synced runtime so both the template (which holds
 * the gate logic) and the injected stack DEX (which implements it) share one
 * class identity at runtime — the stack JAR's copy is shadowed by the
 * template's own on the shared PathClassLoader. Implementations are discovered
 * via `Class.forName`; when no stack DEX is injected, `AdManager` keeps its
 * fail-soft no-op behavior.
 *
 * All methods are main-thread safe to call. Implementations must never throw —
 * ad failures degrade silently to the host.
 */
interface AdsFeatureApi {

    /** Initialize the SDK. Idempotent; no-ops without a valid [appId]. */
    fun initialize(context: Context, appId: String, testMode: Boolean)

    /** Banner view for [adUnitId], already loading. Null when unavailable. */
    fun createBanner(context: Context, adUnitId: String): View?

    /** Load then show an interstitial for [adUnitId]; [onDone] always fires. */
    fun showInterstitial(activity: Activity, adUnitId: String, onDone: AdsCompletion)

    /** Show an app-open style splash ad for [adUnitId] for at most
     *  [timeoutSeconds]; [onDone] always fires exactly once. */
    fun showSplashAd(
        activity: Activity,
        adUnitId: String,
        timeoutSeconds: Int,
        onDone: AdsCompletion
    )

    /** Release held resources; subsequent calls re-initialize lazily. */
    fun destroy()

    companion object {
        /** Ad unit ids substituted when the host enables test mode. */
        const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_SPLASH = "ca-app-pub-3940256099942544/9257395921"
    }
}

/**
 * Completion callback for fullscreen ad ops.
 *
 * Deliberately NOT `() -> Unit`: Kotlin lambdas compile to
 * `kotlin.jvm.functions.Function0`, which the shell's R8 renames — an injected
 * stack compiled against the original name would then miss the interface
 * signature entirely (`AbstractMethodError`). A contract-owned SAM keeps the
 * binary type inside this kept package.
 */
fun interface AdsCompletion {
    fun onDone()
}
