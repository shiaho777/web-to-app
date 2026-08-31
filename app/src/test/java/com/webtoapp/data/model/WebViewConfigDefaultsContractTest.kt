package com.webtoapp.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Contract test pinning the default value of every WebViewConfig feature toggle.
 *
 * Rationale: features whose behavior is opt-in-free, fails soft, and does not affect
 * the normal run of the overwhelming majority of pages should default to ON, so users
 * get the full experience without hunting through advanced settings. Features with a
 * real downside (kernel disguise can trigger anti-bot rejections, third-party cookies
 * weaken privacy, mixed content weakens transport security, failover needs a user
 * supplied URL list, popup blocking suppresses wanted popups) stay OFF.
 *
 * If you flip a flag here, that is a product decision: update this test with the
 * reasoning in the PR description, not silently.
 */
class WebViewConfigDefaultsContractTest {

    // Features that must stay default-OFF: they carry a real trade-off, need user
    // input to be meaningful, or exist to restrict behavior.
    private val defaultOff = setOf(
        "popupBlockerEnabled",           // suppressing popups can hide wanted windows
        "popupBlockerToggleEnabled",
        "mediaAutoplayEnabled",          // audible autoplay is intrusive by default
        "enableKernelDisguise",          // anti-bot systems may reject disguised kernels
        "allowMixedContent",             // weakens HTTPS transport security
        "acceptThirdPartyCookies",        // privacy trade-off
        "enableNativeBridge",            // exposes window.NativeBridge to pages
        "enableCrossOriginIsolation",    // changes COOP/COEP headers
        "hideUrlPreview",                // hides information
        "failoverEnabled",               // meaningless without a user-supplied URL list
        "followSystemDarkMode",
        "longPressMenuEnabled",
        "landscapeMode",
        "clearBrowsingDataOnLaunch",
        "autoRefreshEnabled",
        "keepScreenOn",
        "showFloatingBackButton",
        "hostsMappingEnabled",
        "tlsFingerprintEnabled",
        "enableNotificationPolyfill",    // implies POST_NOTIFICATIONS on every export
        "geolocationEnabled"             // implies location permission on every export
    )

    // Features that must stay default-ON: safe enable-by-default experience.
    private val defaultOn = setOf(
        "javaScriptEnabled",
        "domStorageEnabled",
        "databaseEnabled",
        "enableCookiePersistence",
        "enablePaymentSchemes",
        "enableShareBridge",
        "enableZoomPolyfill",
        "enableCloudflareCompat",
        "enableCorsBypass",
        "enableBlobDownloadInterception",
        "enablePrintBridge",
        // Enable-by-default batch (experience parity without user setup):
        "decodeBase64DeepLinks",
        "javaScriptCanOpenWindows",
        "enableImageRepair",
        "enableScrollMemory",
        "enableBackStatePreservation",
        "enablePrivateNetworkBridge",
        "enableClipboardPolyfill",
        // Notification polyfill and geolocation stay default-OFF on purpose: enabling
        // either makes every exported app declare/request runtime permissions
        // (POST_NOTIFICATIONS / location) on first launch — a cost most pages never
        // earn. See RuntimePermissionSync.
        "enableOrientationPolyfill",
        "enableCompatPolyfills",
        "enableMediaSession",
        "primeUserActivation"
    )

    @Test
    fun `enable-by-default feature batch keeps its ON defaults`() {
        val config = WebViewConfig()
        val fields = WebViewConfig::class.java.declaredFields.associateBy { it.name }

        val actualOn = defaultOn.mapNotNull { name ->
            fields[name]?.let { field ->
                field.isAccessible = true
                name to (field.get(config) as? Boolean)
            }
        }
        val missing = defaultOn - actualOn.map { it.first }.toSet()
        assertThat(missing).isEmpty()

        val notOn = actualOn.filter { it.second != true }.map { it.first }
        assertThat(notOn).isEmpty()
    }

    @Test
    fun `trade-off features keep their OFF defaults`() {
        val config = WebViewConfig()
        val fields = WebViewConfig::class.java.declaredFields.associateBy { it.name }

        val actualOff = defaultOff.mapNotNull { name ->
            fields[name]?.let { field ->
                field.isAccessible = true
                name to (field.get(config) as? Boolean)
            }
        }
        val missing = defaultOff - actualOff.map { it.first }.toSet()
        assertThat(missing).isEmpty()

        val notOff = actualOff.filter { it.second != false }.map { it.first }
        assertThat(notOff).isEmpty()
    }

    @Test
    fun `geolocation stays default-off and keeps ask-first policy when enabled`() {
        val config = WebViewConfig()
        // Default OFF: enabling geolocation makes every exported app declare/request
        // location permission (RuntimePermissionSync) — too heavy a default.
        assertThat(config.geolocationEnabled).isFalse()
        // Safety boundary: when a user does enable it, requests must still be ask-first.
        assertThat(config.geolocationPolicy).isEqualTo(GeolocationPolicy.ALWAYS_ASK)
    }

    @Test
    fun `private network bridge default stays scoped to local networks`() {
        val config = WebViewConfig()
        assertThat(config.enablePrivateNetworkBridge).isTrue()
        assertThat(config.privateNetworkScope).isEqualTo(PrivateNetworkScope.LOCAL_ONLY)
    }

    @Test
    fun `base64 deep link default stays gesture-gated`() {
        val config = WebViewConfig()
        assertThat(config.decodeBase64DeepLinks).isTrue()
        assertThat(config.decodeBase64Mode).isEqualTo(Base64DeepLinkMode.GESTURE_ONLY)
    }

    @Test
    fun `js open windows default stays on the allow policy`() {
        val config = WebViewConfig()
        assertThat(config.javaScriptCanOpenWindows).isTrue()
        assertThat(config.jsOpenWindowsPolicy).isEqualTo(JsOpenWindowsPolicy.ALLOW)
    }

    @Test
    fun `prime user activation default stays on the safe timing`() {
        val config = WebViewConfig()
        assertThat(config.primeUserActivation).isTrue()
        assertThat(config.primeUserActivationMode).isEqualTo(PrimeUserActivationMode.SYNTHETIC_TAP)
        assertThat(config.primeUserActivationTiming).isEqualTo(PrimeUserActivationTiming.ON_PAGE_FINISHED)
    }
}
