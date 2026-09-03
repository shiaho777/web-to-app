package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.GsonProvider
import java.lang.reflect.Field
import org.junit.Test

/**
 * Ensures every WebViewConfig field survives the full export pipeline:
 *
 *   WebApp.WebViewConfig
 *     → ApkConfig (toApkConfig / buildWebViewBehaviorBlock)
 *     → JSON (ApkConfigJsonFactory.toShellConfigJson)
 *     → ShellConfig.WebViewConfig (Gson deserialization)
 *
 * If a field is added to WebViewConfig but forgotten in any of these layers,
 * the generated APK silently drops the setting — "preview works, export broken".
 *
 * **When you add a new Boolean field to WebViewConfig, you MUST add it to
 * [flipAllBooleans] below.** The first test verifies that every declared
 * Boolean field is listed — if you forget, the test fails with a clear message.
 */
class WebViewConfigBooleanCoverageTest {

    // ────────────────────────────────────────────────────────────
    //  1. flipAllBooleans must cover every Boolean field (compile-time safety)
    // ────────────────────────────────────────────────────────────

    @Test
    fun `flipAllBooleans covers every declared Boolean field in WebViewConfig`() {
        val allBooleanFields = WebViewConfig::class.java.declaredFields
            .filter { it.type == java.lang.Boolean.TYPE }
            .map { it.name }
            .toSet()

        // The fields explicitly listed in flipAllBooleans — if a new Boolean field is
        // added to WebViewConfig but missing here, this test fails immediately.
        val listedFields = setOf(
            "javaScriptEnabled", "domStorageEnabled", "allowFileAccess", "allowContentAccess",
            "cacheEnabled", "clearBrowsingDataOnLaunch", "clientCertificateAuthEnabled",
            "zoomEnabled", "desktopMode",
            "browserToolbarEnabled", "hideToolbar", "toolbarShowTitle", "toolbarShowUrl",
            "toolbarShowBack", "toolbarShowForward", "toolbarShowRefresh",
            "toolbarShowConsole", "toolbarShowFind",
            "showStatusBarInFullscreen",
            "hideStatusBarInVideoFullscreen",
            "showNavigationBarInFullscreen", "showToolbarInFullscreen", "landscapeMode",
            "longPressMenuEnabled", "popupBlockerEnabled", "popupBlockerToggleEnabled",
            "openExternalLinks", "showFloatingBackButton", "swipeRefreshEnabled",
            "fullscreenEnabled", "performanceOptimization", "pwaOfflineEnabled",
            "staticAssetPackEnabled", "staticAssetPackIncludeImages", "staticAssetPackIncludeCdn",
            "downloadEnabled", "antiCapture",
            "enableKernelDisguise", "enableImageRepair", "enableScrollMemory",
            "enableBackStatePreservation", "followSystemDarkMode",
            "enableClipboardPolyfill", "enableNotificationPolyfill",
            "enableOrientationPolyfill", "enableCompatPolyfills",
            "enableCorsBypass", "allowMixedContent",
            "enableBlobDownloadInterception", "enablePrintBridge", "enableMediaSession",
            "enableCloudflareCompat", "enableCookiePersistence",
            "enablePrivateNetworkBridge", "enableNativeBridge",
            "enablePaymentSchemes", "enableShareBridge", "enableZoomPolyfill",
            "enableCrossOriginIsolation", "hideUrlPreview", "decodeBase64DeepLinks",
            "javaScriptCanOpenWindows", "mediaAutoplayEnabled",
            "acceptThirdPartyCookies", "geolocationEnabled", "keepScreenOn",
            "databaseEnabled", "primeUserActivation", "failoverEnabled",
            "hostsMappingEnabled", "autoRefreshEnabled", "autoRefreshShowCountdown",
            "allowFileAccessFromFileURLs", "allowUniversalAccessFromFileURLs",
            "tlsFingerprintEnabled"
        )

        val missing = allBooleanFields - listedFields
        val stale = listedFields - allBooleanFields

        assertThat(missing).isEmpty()
        assertThat(stale).isEmpty()
    }

    // ────────────────────────────────────────────────────────────
    //  2. Every Boolean field flips through the export pipeline
    // ────────────────────────────────────────────────────────────

    @Test
    fun `every WebViewConfig Boolean field flips its WebViewShellConfig counterpart on export`() {
        val defaultApp = WebApp(name = "t", url = "https://t.example.com")
        val defaultShell = roundTrip(defaultApp)
        val defaultShellWv = shellWvOf(defaultShell)

        val flippedWv = flipAllBooleans(defaultApp.webViewConfig)
        val flippedApp = defaultApp.copy(webViewConfig = flippedWv)
        val flippedShell = roundTrip(flippedApp)
        val flippedShellWv = shellWvOf(flippedShell)

        val shellWvFields = flippedShellWv.javaClass.declaredFields.associateBy { it.name }

        val knownDerivedOrIntentional = setOf(
            "allowFileAccess", "allowFileAccessFromFileURLs",
            "allowUniversalAccessFromFileURLs", "cacheEnabled",
            "pwaOfflineEnabled", "staticAssetPackIncludeImages", "staticAssetPackIncludeCdn"
        )

        val wvBooleanFields = WebViewConfig::class.java.declaredFields
            .filter { it.type == java.lang.Boolean.TYPE }

        val notFlipped = mutableListOf<String>()
        val notPresent = mutableListOf<String>()

        for (field in wvBooleanFields) {
            val name = field.name
            if (name in knownDerivedOrIntentional) continue
            val shellField = shellWvFields[name]
            if (shellField == null) {
                notPresent.add(name)
                continue
            }
            shellField.isAccessible = true
            // Shell field might be primitive boolean or boxed Boolean?
            val flippedValue = readBool(shellField, flippedShellWv)
            val defaultValue = readBool(shellField, defaultShellWv)
            if (flippedValue == defaultValue) {
                notFlipped.add(name)
            }
        }

        if (notPresent.isNotEmpty()) {
            throw AssertionError(
                "Boolean fields with no ShellWebViewConfig counterpart " +
                    "(add to knownDerivedOrIntentional if by design): $notPresent"
            )
        }
        if (notFlipped.isNotEmpty()) {
            throw AssertionError(
                "Boolean fields whose flip did NOT propagate to ShellWebViewConfig " +
                    "(missing in toWebViewBlock/toWebViewBehaviorBlock): $notFlipped"
            )
        }
    }

    // ────────────────────────────────────────────────────────────
    //  3. Field-set parity: no orphan Boolean fields on either side
    // ────────────────────────────────────────────────────────────

    @Test
    fun `WebViewConfig and ShellWebViewConfig have matching Boolean field names`() {
        val hostBooleanFields = WebViewConfig::class.java.declaredFields
            .filter { it.type == java.lang.Boolean.TYPE || it.type == java.lang.Boolean::class.java }
            .map { it.name }
            .toSet()

        val shellWvClass = shellWvOf(roundTrip(WebApp(name = "t", url = "https://t.example.com"))).javaClass
        val shellBooleanFields = shellWvClass.declaredFields
            .filter { it.type == java.lang.Boolean.TYPE || it.type == java.lang.Boolean::class.java }
            .map { it.name }
            .toSet()

        val knownDerivedOrIntentional = setOf(
            "allowFileAccess", "allowFileAccessFromFileURLs",
            "allowUniversalAccessFromFileURLs", "cacheEnabled",
            "pwaOfflineEnabled", "staticAssetPackIncludeImages", "staticAssetPackIncludeCdn"
        )

        // Shell flattens NativeBridgeCapabilities + FailoverTriggers into top-level Boolean
        // fields that don't exist individually on WebViewConfig (they're nested objects).
        val shellOnlyIntentional = setOf(
            "nativeBridgeClipboard", "nativeBridgeVibration", "nativeBridgeGeolocation",
            "nativeBridgeBrightness", "nativeBridgeNotification", "nativeBridgeNotificationScheduled",
            "nativeBridgeNotificationPersistent", "nativeBridgeDownload", "nativeBridgePrivateNetwork",
            "nativeBridgeScreenWake", "nativeBridgeOpenExternal", "nativeBridgeDeviceInfo",
            "nativeBridgeSecurityInfo", "nativeBridgeNetworkInfo", "nativeBridgeToast",
            "nativeBridgeLogging", "nativeBridgeFindInPage", "nativeBridgeOrientation",
            "nativeBridgeFullscreen", "nativeBridgePrint",
            "failoverTriggerNetworkError", "failoverTriggerHttp5xx",
            "failoverTriggerHttp4xx", "failoverTriggerTimeout"
        )

        val exempt = knownDerivedOrIntentional + shellOnlyIntentional
        val hostOnly = (hostBooleanFields - shellBooleanFields) - exempt
        val shellOnly = (shellBooleanFields - hostBooleanFields) - exempt

        assertThat(hostOnly).isEmpty()
        assertThat(shellOnly).isEmpty()
    }

    // ────────────────────────────────────────────────────────────
    //  4. Non-Boolean spot-check: key String/Int/Enum survive export
    // ────────────────────────────────────────────────────────────

    @Test
    fun `key non-Boolean WebViewConfig fields survive the export round-trip`() {
        val app = WebApp(
            name = "test",
            url = "https://t.example.com",
            webViewConfig = WebViewConfig(
                userAgentMode = com.webtoapp.data.model.UserAgentMode.CHROME_DESKTOP,
                customUserAgent = "CustomUA/2.0",
                downloadLocationMode = com.webtoapp.data.model.DownloadLocationMode.CUSTOM,
                newWindowBehavior = com.webtoapp.data.model.NewWindowBehavior.POPUP_WINDOW,
                orientationMode = com.webtoapp.data.model.OrientationMode.LANDSCAPE,
                cloudflareCompatMode = com.webtoapp.data.model.CloudflareCompatMode.ALWAYS_ON,
                mixedContentMode = com.webtoapp.data.model.MixedContentMode.COMPATIBILITY,
                swipeRefreshZone = com.webtoapp.data.model.SwipeRefreshZone.ANYWHERE,
                autoRefreshIntervalSec = 120,
                blobInterceptThresholdMb = 10,
                screenAwakeTimeoutMinutes = 15,
                pageZoomPercent = 125
            )
        )
        val shell = roundTrip(app)
        val shellWv = shellWvOf(shell)
        val shellFields = shellWv.javaClass.declaredFields.associateBy { it.name }

        fun <T> readShell(path: String, expected: T) {
            val field = shellFields[path]
                ?: throw AssertionError("ShellWebViewConfig missing field '$path'")
            field.isAccessible = true
            val actual = field.get(shellWv)?.toString()
            assertThat(actual).isEqualTo(expected.toString())
        }

        readShell("userAgentMode", "CHROME_DESKTOP")
        readShell("customUserAgent", "CustomUA/2.0")
        readShell("downloadLocationMode", "CUSTOM")
        readShell("newWindowBehavior", "POPUP_WINDOW")
        readShell("orientationMode", "LANDSCAPE")
        readShell("cloudflareCompatMode", "ALWAYS_ON")
        readShell("swipeRefreshZone", "ANYWHERE")
        readShell("autoRefreshIntervalSec", 120)
        readShell("blobInterceptThresholdMb", 10)
        readShell("screenAwakeTimeoutMinutes", 15)
        readShell("pageZoomPercent", 125)
    }

    @Test
    fun `nullable statusBarDarkIconsDark round-trips through export`() {
        fun shellDarkIconsOf(config: WebViewConfig): Any? {
            val shellWv = shellWvOf(roundTrip(WebApp(name = "t", url = "https://t.example.com", webViewConfig = config)))
            val field = shellWv.javaClass.declaredFields.associateBy { it.name }["statusBarDarkIconsDark"]
                ?: throw AssertionError("ShellWebViewConfig missing field 'statusBarDarkIconsDark'")
            field.isAccessible = true
            return field.get(shellWv)
        }

        // Explicit choices survive; the default (auto) stays null instead of
        // degrading to false, so the runtime keeps its luminance fallback.
        assertThat(shellDarkIconsOf(WebViewConfig(statusBarDarkIconsDark = true))).isEqualTo(true)
        assertThat(shellDarkIconsOf(WebViewConfig(statusBarDarkIconsDark = false))).isEqualTo(false)
        assertThat(shellDarkIconsOf(WebViewConfig())).isNull()
    }

    // ────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────

    private fun roundTrip(app: WebApp): ShellConfig {
        val apk = app.toApkConfig("com.example.test")
        val json = ApkConfigJsonFactory.toShellConfigJson(apk)
        return GsonProvider.gson.fromJson(json, ShellConfig::class.java)!!
    }

    private fun shellWvOf(shell: ShellConfig): Any {
        val field = ShellConfig::class.java.getDeclaredField("webViewConfig")
        field.isAccessible = true
        return field.get(shell)!!
    }

    /** Reads a Boolean from a field that may be primitive boolean or boxed Boolean?. */
    private fun readBool(field: Field, target: Any): Boolean {
        field.isAccessible = true
        return if (field.type == java.lang.Boolean.TYPE) {
            field.getBoolean(target)
        } else {
            field.get(target) as? Boolean ?: false
        }
    }

    /**
     * Creates a WebViewConfig where every Boolean is the negation of its default.
     * When adding a new Boolean field to WebViewConfig, you MUST add it here —
     * the `flipAllBooleans covers every declared Boolean field` test will fail
     * if you forget, with a message telling you exactly which field is missing.
     */
    private fun flipAllBooleans(source: WebViewConfig): WebViewConfig {
        val fields = WebViewConfig::class.java.declaredFields
            .filter { it.type == java.lang.Boolean.TYPE }
            .associateBy { it.name }
        fun bool(name: String): Boolean {
            val f = fields[name]!!
            f.isAccessible = true
            return !f.getBoolean(source)
        }
        return WebViewConfig(
            javaScriptEnabled = bool("javaScriptEnabled"),
            domStorageEnabled = bool("domStorageEnabled"),
            allowFileAccess = bool("allowFileAccess"),
            allowContentAccess = bool("allowContentAccess"),
            cacheEnabled = bool("cacheEnabled"),
            clearBrowsingDataOnLaunch = bool("clearBrowsingDataOnLaunch"),
            clientCertificateAuthEnabled = bool("clientCertificateAuthEnabled"),
            zoomEnabled = bool("zoomEnabled"),
            desktopMode = bool("desktopMode"),
            hideToolbar = bool("hideToolbar"),
            browserToolbarEnabled = bool("browserToolbarEnabled"),
            toolbarShowTitle = bool("toolbarShowTitle"),
            toolbarShowUrl = bool("toolbarShowUrl"),
            toolbarShowBack = bool("toolbarShowBack"),
            toolbarShowForward = bool("toolbarShowForward"),
            toolbarShowRefresh = bool("toolbarShowRefresh"),
            toolbarShowConsole = bool("toolbarShowConsole"),
            toolbarShowFind = bool("toolbarShowFind"),
            showStatusBarInFullscreen = bool("showStatusBarInFullscreen"),
            hideStatusBarInVideoFullscreen = bool("hideStatusBarInVideoFullscreen"),
            showNavigationBarInFullscreen = bool("showNavigationBarInFullscreen"),
            showToolbarInFullscreen = bool("showToolbarInFullscreen"),
            landscapeMode = bool("landscapeMode"),
            longPressMenuEnabled = bool("longPressMenuEnabled"),
            popupBlockerEnabled = bool("popupBlockerEnabled"),
            popupBlockerToggleEnabled = bool("popupBlockerToggleEnabled"),
            openExternalLinks = bool("openExternalLinks"),
            showFloatingBackButton = bool("showFloatingBackButton"),
            swipeRefreshEnabled = bool("swipeRefreshEnabled"),
            fullscreenEnabled = bool("fullscreenEnabled"),
            performanceOptimization = bool("performanceOptimization"),
            pwaOfflineEnabled = bool("pwaOfflineEnabled"),
            staticAssetPackEnabled = bool("staticAssetPackEnabled"),
            downloadEnabled = bool("downloadEnabled"),
            antiCapture = bool("antiCapture"),
            enableKernelDisguise = bool("enableKernelDisguise"),
            enableImageRepair = bool("enableImageRepair"),
            enableScrollMemory = bool("enableScrollMemory"),
            enableBackStatePreservation = bool("enableBackStatePreservation"),
            followSystemDarkMode = bool("followSystemDarkMode"),
            enableClipboardPolyfill = bool("enableClipboardPolyfill"),
            enableNotificationPolyfill = bool("enableNotificationPolyfill"),
            enableOrientationPolyfill = bool("enableOrientationPolyfill"),
            enableCompatPolyfills = bool("enableCompatPolyfills"),
            enableCorsBypass = bool("enableCorsBypass"),
            allowMixedContent = bool("allowMixedContent"),
            enableBlobDownloadInterception = bool("enableBlobDownloadInterception"),
            enablePrintBridge = bool("enablePrintBridge"),
            enableMediaSession = bool("enableMediaSession"),
            enableCloudflareCompat = bool("enableCloudflareCompat"),
            enableCookiePersistence = bool("enableCookiePersistence"),
            enablePrivateNetworkBridge = bool("enablePrivateNetworkBridge"),
            enableNativeBridge = bool("enableNativeBridge"),
            enablePaymentSchemes = bool("enablePaymentSchemes"),
            enableShareBridge = bool("enableShareBridge"),
            enableZoomPolyfill = bool("enableZoomPolyfill"),
            enableCrossOriginIsolation = bool("enableCrossOriginIsolation"),
            hideUrlPreview = bool("hideUrlPreview"),
            decodeBase64DeepLinks = bool("decodeBase64DeepLinks"),
            javaScriptCanOpenWindows = bool("javaScriptCanOpenWindows"),
            mediaAutoplayEnabled = bool("mediaAutoplayEnabled"),
            acceptThirdPartyCookies = bool("acceptThirdPartyCookies"),
            geolocationEnabled = bool("geolocationEnabled"),
            keepScreenOn = bool("keepScreenOn"),
            databaseEnabled = bool("databaseEnabled"),
            primeUserActivation = bool("primeUserActivation"),
            failoverEnabled = bool("failoverEnabled"),
            hostsMappingEnabled = bool("hostsMappingEnabled"),
            autoRefreshEnabled = bool("autoRefreshEnabled"),
            autoRefreshShowCountdown = bool("autoRefreshShowCountdown"),
            allowFileAccessFromFileURLs = bool("allowFileAccessFromFileURLs"),
            allowUniversalAccessFromFileURLs = bool("allowUniversalAccessFromFileURLs"),
            tlsFingerprintEnabled = bool("tlsFingerprintEnabled")
        )
    }
}
