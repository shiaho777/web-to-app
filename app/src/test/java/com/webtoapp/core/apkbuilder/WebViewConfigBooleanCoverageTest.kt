package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.GsonProvider
import java.lang.reflect.Field
import org.junit.Test

class WebViewConfigBooleanCoverageTest {

    @Test
    fun `every WebViewConfig Boolean field flips its WebViewShellConfig counterpart on export`() {
        val defaultApp = WebApp(name = "t", url = "https://t.example.com")
        val defaultShell = roundTrip(defaultApp)
        val defaultWv = defaultApp.webViewConfig
        val defaultShellWv = shellWvOf(defaultShell)

        val flippedWv = flipAllBooleans(defaultWv)
        val flippedApp = defaultApp.copy(webViewConfig = flippedWv)
        val flippedShell = roundTrip(flippedApp)
        val flippedShellWv = shellWvOf(flippedShell)

        val shellWvFields = flippedShellWv.javaClass.declaredFields.associateBy { it.name }

        val wvBooleanFields = WebViewConfig::class.java.declaredFields
            .filter { it.type == java.lang.Boolean.TYPE }

        val knownDerivedOrIntentional = setOf(
            "allowFileAccess",
            "allowFileAccessFromFileURLs",
            "allowUniversalAccessFromFileURLs",
            "cacheEnabled",
            "pwaOfflineEnabled"
        )

        val knownBroken: Set<String> = emptySet()

        val exempt = knownDerivedOrIntentional + knownBroken

        val notFlipped = mutableListOf<String>()
        val notPresent = mutableListOf<String>()

        for (field in wvBooleanFields) {
            val name = field.name
            val shellField = shellWvFields[name]
            if (shellField == null) {
                if (name !in exempt) notPresent.add(name)
                continue
            }
            shellField.isAccessible = true
            val flippedValue = readBoolean(shellField, flippedShellWv)
            val defaultValue = readBoolean(shellField, defaultShellWv)
            if (flippedValue == defaultValue && name !in exempt) {
                notFlipped.add(name)
            }
        }

        if (notFlipped.isNotEmpty()) {
            throw AssertionError(
                "WebViewConfig Boolean fields whose flip did NOT propagate to WebViewShellConfig " +
                    "(missing in toWebViewBlock/toWebViewBehaviorBlock): $notFlipped"
            )
        }
        if (notPresent.isNotEmpty()) {
            throw AssertionError(
                "WebViewConfig Boolean fields with no same-named WebViewShellConfig field " +
                    "(add to knownDerivedOrIntentional if by design): $notPresent"
            )
        }

        assertThat(knownBroken).isEmpty()
    }

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

    private fun readBoolean(field: Field, target: Any): Boolean? {
        field.isAccessible = true
        return if (field.type == java.lang.Boolean.TYPE) {
            field.getBoolean(target)
        } else {
            field.get(target) as? Boolean
        }
    }

    private fun flipAllBooleans(source: WebViewConfig): WebViewConfig {
        val sourceFields = WebViewConfig::class.java.declaredFields
            .filter { it.type == java.lang.Boolean.TYPE }
            .associateBy { it.name }
        fun bool(name: String): Boolean {
            val f = sourceFields[name]!!
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
            zoomEnabled = bool("zoomEnabled"),
            desktopMode = bool("desktopMode"),
            hideToolbar = bool("hideToolbar"),
            hideBrowserToolbar = bool("hideBrowserToolbar"),
            toolbarShowTitle = bool("toolbarShowTitle"),
            toolbarShowUrl = bool("toolbarShowUrl"),
            toolbarShowBack = bool("toolbarShowBack"),
            toolbarShowForward = bool("toolbarShowForward"),
            toolbarShowRefresh = bool("toolbarShowRefresh"),
            browserToolbarCustomized = bool("browserToolbarCustomized"),
            showStatusBarInFullscreen = bool("showStatusBarInFullscreen"),
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
            tlsFingerprintEnabled = bool("tlsFingerprintEnabled"),
            statusBarDarkIconsDark = bool("statusBarDarkIconsDark")
        )
    }
}
