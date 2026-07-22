package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.activation.ActivationCode
import com.webtoapp.core.activation.ActivationCodeType
import com.webtoapp.core.appearance.BrowserDisguiseConfig
import com.webtoapp.core.appearance.DeviceDisguiseConfig
import com.webtoapp.core.appearance.DisguiseConfig
import com.webtoapp.core.actions.DeviceActionsConfig
import com.webtoapp.core.forcedrun.ForcedRunConfig
import com.webtoapp.core.privacy.IsolationConfig
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.AdConfig
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.AnnouncementTemplateType
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.ApkRuntimePermissions
import com.webtoapp.data.model.AutoStartConfig
import com.webtoapp.data.model.BgmConfig
import com.webtoapp.data.model.BgmItem
import com.webtoapp.data.model.BgmPlayMode
import com.webtoapp.data.model.ActivationDialogConfig
import com.webtoapp.data.model.LrcTheme
import com.webtoapp.data.model.RemoteActivationConfig
import com.webtoapp.data.model.RemoteActivationOfflinePolicy
import com.webtoapp.data.model.SplashConfig
import com.webtoapp.data.model.SplashType
import com.webtoapp.data.model.TranslateConfig
import com.webtoapp.data.model.TranslateEngine
import com.webtoapp.data.model.TranslateLanguage
import com.webtoapp.data.model.UserAgentMode
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.util.GsonProvider
import org.junit.Test

class ConfigRoundTripSentinelTest {

    private fun roundTrip(app: WebApp, packageName: String = "com.example.test"): ShellConfig {
        val apk = app.toApkConfig(packageName)
        val json = ApkConfigJsonFactory.toShellConfigJson(apk)
        return GsonProvider.gson.fromJson(json, ShellConfig::class.java)
            ?: error("round-trip produced null ShellConfig")
    }

    private fun baseApp(appType: AppType = AppType.WEB): WebApp = WebApp(
        name = "Sentinel",
        url = "https://sentinel.example.com",
        appType = appType
    )

    @Test
    fun `meta fields appName targetUrl packageName versionCode versionName round-trip`() {
        val app = baseApp().copy(
            name = "MySentinelApp",
            apkExportConfig = ApkExportConfig(
                customVersionCode = 42,
                customVersionName = "9.9.9-sentinel"
            )
        )
        val shell = roundTrip(app, packageName = "com.sentinel.pkg")
        assertThat(shell.appName).isEqualTo("MySentinelApp")
        assertThat(shell.packageName).isEqualTo("com.sentinel.pkg")
        assertThat(shell.versionCode).isEqualTo(42)
        assertThat(shell.versionName).isEqualTo("9.9.9-sentinel")
        assertThat(shell.targetUrl).isEqualTo("https://sentinel.example.com")
    }

    @Test
    fun `activation local fields round-trip through flatten and type-change`() {
        val code1 = ActivationCode(code = "SEN-111", type = ActivationCodeType.PERMANENT)
        val code2 = ActivationCode(code = "SEN-222", type = ActivationCodeType.USAGE_LIMITED)
        val app = baseApp().copy(
            activationEnabled = true,
            activationRequireEveryTime = true,
            activationCodeList = listOf(code1, code2),
            activationDialogConfig = ActivationDialogConfig(
                title = "SEN-TITLE",
                subtitle = "SEN-SUB",
                inputLabel = "SEN-LABEL",
                buttonText = "SEN-BUTTON"
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.activationEnabled).isTrue()
        assertThat(shell.activationRequireEveryTime).isTrue()
        assertThat(shell.activationCodes).containsExactly(code1.toJson(), code2.toJson()).inOrder()
        assertThat(shell.activationCodes[0]).contains("SEN-111")
        assertThat(shell.activationDialogTitle).isEqualTo("SEN-TITLE")
        assertThat(shell.activationDialogSubtitle).isEqualTo("SEN-SUB")
        assertThat(shell.activationDialogInputLabel).isEqualTo("SEN-LABEL")
        assertThat(shell.activationDialogButtonText).isEqualTo("SEN-BUTTON")
    }

    @Test
    fun `activation remote config round-trip through flatten`() {
        val app = baseApp().copy(
            activationRemoteConfig = RemoteActivationConfig(
                enabled = true,
                verifyUrl = "https://verify.sentinel.example/api",
                publicKeyBase64 = "SEN-PUBKEY-BASE64",
                offlinePolicy = RemoteActivationOfflinePolicy.DENY
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.activationRemoteEnabled).isTrue()
        assertThat(shell.activationRemoteVerifyUrl).isEqualTo("https://verify.sentinel.example/api")
        assertThat(shell.activationRemotePublicKey).isEqualTo("SEN-PUBKEY-BASE64")
        assertThat(shell.activationRemoteOfflinePolicy).isEqualTo("DENY")
    }

    @Test
    fun `ad block fields round-trip`() {
        val app = baseApp().copy(
            adBlockEnabled = true,
            adBlockRules = listOf("||ads.sentinel.example^", "||track.sentinel.example^"),
            adBlockSubscriptions = listOf("https://list.sentinel.example/block.txt")
        )
        val shell = roundTrip(app)
        assertThat(shell.adBlockEnabled).isTrue()
        assertThat(shell.adBlockRules).containsExactly("||ads.sentinel.example^", "||track.sentinel.example^").inOrder()
        assertThat(shell.adBlockSubscriptions).containsExactly("https://list.sentinel.example/block.txt")
    }

    @Test
    fun `ads block round-trip through nullable flatten`() {
        val app = baseApp().copy(
            adsEnabled = true,
            adConfig = AdConfig(
                bannerEnabled = true,
                bannerId = "SEN-BANNER",
                interstitialEnabled = true,
                interstitialId = "SEN-INTER",
                splashEnabled = true,
                splashId = "SEN-SPLASH"
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.adsEnabled).isTrue()
        assertThat(shell.adBannerEnabled).isTrue()
        assertThat(shell.adBannerId).isEqualTo("SEN-BANNER")
        assertThat(shell.adInterstitialEnabled).isTrue()
        assertThat(shell.adInterstitialId).isEqualTo("SEN-INTER")
        assertThat(shell.adSplashEnabled).isTrue()
        assertThat(shell.adSplashId).isEqualTo("SEN-SPLASH")
    }

    @Test
    fun `announcement fields round-trip through nullable flatten and enum-to-string`() {
        val app = baseApp().copy(
            announcementEnabled = true,
            announcement = Announcement(
                title = "SEN-ANN-TITLE",
                content = "SEN-ANN-BODY",
                contentIsHtml = true,
                linkUrl = "https://link.sentinel.example",
                linkText = "SEN-ANN-LINK",
                showOnce = false,
                requireConfirmation = true,
                allowNeverShow = true,
                triggerOnLaunch = false,
                triggerOnNoNetwork = true,
                triggerIntervalMinutes = 77,
                version = 5,
                triggerIntervalIncludeLaunch = true,
                template = AnnouncementTemplateType.NEON
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.announcementEnabled).isTrue()
        assertThat(shell.announcementTitle).isEqualTo("SEN-ANN-TITLE")
        assertThat(shell.announcementContent).isEqualTo("SEN-ANN-BODY")
        assertThat(shell.announcementContentIsHtml).isTrue()
        assertThat(shell.announcementLink).isEqualTo("https://link.sentinel.example")
        assertThat(shell.announcementLinkText).isEqualTo("SEN-ANN-LINK")
        assertThat(shell.announcementShowOnce).isFalse()
        assertThat(shell.announcementRequireConfirmation).isTrue()
        assertThat(shell.announcementAllowNeverShow).isTrue()
        assertThat(shell.announcementTriggerOnLaunch).isFalse()
        assertThat(shell.announcementTriggerOnNoNetwork).isTrue()
        assertThat(shell.announcementTriggerIntervalMinutes).isEqualTo(77)
        assertThat(shell.announcementVersion).isEqualTo(5)
        assertThat(shell.announcementTriggerIntervalIncludeLaunch).isTrue()
        assertThat(shell.announcementTemplate).isEqualTo("DARK")
    }

    @Test
    fun `splash fields round-trip through nullable flatten and camelCase rename`() {
        val app = baseApp().copy(
            splashEnabled = true,
            splashConfig = SplashConfig(
                type = SplashType.VIDEO,
                mediaPath = "/sentinel/splash.mp4",
                duration = 9,
                clickToSkip = false,
                fillScreen = false,
                enableAudio = true,
                videoStartMs = 1000L,
                videoEndMs = 9000L
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.splashEnabled).isTrue()
        assertThat(shell.splashType).isEqualTo("VIDEO")
        assertThat(shell.splashDuration).isEqualTo(9)
        assertThat(shell.splashClickToSkip).isFalse()
        assertThat(shell.splashFillScreen).isFalse()
        assertThat(shell.splashEnableAudio).isTrue()
        assertThat(shell.splashVideoStartMs).isEqualTo(1000L)
        assertThat(shell.splashVideoEndMs).isEqualTo(9000L)
        assertThat(shell.splashLandscape).isFalse()
    }

    @Test
    fun `bgm scalar fields round-trip`() {
        val app = baseApp().copy(
            bgmEnabled = true,
            bgmConfig = BgmConfig(
                playMode = BgmPlayMode.SHUFFLE,
                volume = 0.25f,
                autoPlay = false,
                showLyrics = false
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.bgmEnabled).isTrue()
        assertThat(shell.bgmPlayMode).isEqualTo("SHUFFLE")
        assertThat(shell.bgmVolume).isEqualTo(0.25f)
        assertThat(shell.bgmAutoPlay).isFalse()
        assertThat(shell.bgmShowLyrics).isFalse()
    }

    @Test
    fun `bgm nested playlist and lrcTheme round-trip via object serialization`() {
        val app = baseApp().copy(
            bgmEnabled = true,
            bgmConfig = BgmConfig(
                playlist = listOf(
                    BgmItem(name = "SEN-TRACK-A", path = "/sentinel/a.mp3"),
                    BgmItem(name = "SEN-TRACK-B", path = "/sentinel/b.mp3")
                ),
                lrcTheme = LrcTheme(
                    id = "sen-lrc",
                    name = "SEN-LRC-THEME",
                    textColor = "#SENTEXT",
                    highlightColor = "#SENHIGH",
                    fontSize = 24f
                )
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.bgmPlaylist).hasSize(2)
        assertThat(shell.bgmPlaylist[0].name).isEqualTo("SEN-TRACK-A")
        assertThat(shell.bgmPlaylist[1].name).isEqualTo("SEN-TRACK-B")
        assertThat(shell.bgmLrcTheme?.textColor).isEqualTo("#SENTEXT")
        assertThat(shell.bgmLrcTheme?.highlightColor).isEqualTo("#SENHIGH")
        assertThat(shell.bgmLrcTheme?.fontSize).isEqualTo(24f)
    }

    @Test
    fun `translate fields round-trip through nullable flatten and enum-to-string`() {
        val app = baseApp().copy(
            translateEnabled = true,
            translateConfig = TranslateConfig(
                targetLanguage = TranslateLanguage.JAPANESE,
                showFloatingButton = false
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.translateEnabled).isTrue()
        assertThat(shell.translateTargetLanguage).isEqualTo("ja")
        assertThat(shell.translateShowButton).isFalse()
    }

    @Test
    fun `extension fields round-trip`() {
        val app = baseApp().copy(
            extensionEnabled = true,
            extensionModuleIds = listOf("sen-mod-1", "sen-mod-2")
        )
        val shell = roundTrip(app)
        assertThat(shell.extensionEnabled).isTrue()
        assertThat(shell.extensionModuleIds).containsExactly("sen-mod-1", "sen-mod-2").inOrder()
    }

    @Test
    fun `webview core toggles round-trip`() {
        val app = baseApp().copy(
            webViewConfig = WebViewConfig(
                javaScriptEnabled = false,
                domStorageEnabled = false,
                zoomEnabled = false,
                desktopMode = true,
                cacheEnabled = false,
                clearBrowsingDataOnLaunch = true,
                swipeRefreshEnabled = false,
                fullscreenEnabled = false,
                hideToolbar = true,
                landscapeMode = true,
                userAgentMode = UserAgentMode.CHROME_MOBILE
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.webViewConfig.javaScriptEnabled).isFalse()
        assertThat(shell.webViewConfig.domStorageEnabled).isFalse()
        assertThat(shell.webViewConfig.zoomEnabled).isFalse()
        assertThat(shell.webViewConfig.desktopMode).isTrue()
        assertThat(shell.webViewConfig.cacheEnabled).isFalse()
        assertThat(shell.webViewConfig.clearBrowsingDataOnLaunch).isTrue()
        assertThat(shell.webViewConfig.swipeRefreshEnabled).isFalse()
        assertThat(shell.webViewConfig.fullscreenEnabled).isFalse()
        assertThat(shell.webViewConfig.hideToolbar).isTrue()
        assertThat(shell.webViewConfig.landscapeMode).isTrue()
        assertThat(shell.webViewConfig.userAgentMode).isEqualTo("CHROME_MOBILE")
    }

    @Test
    fun `autostart config round-trip as whole-object serialization`() {
        val app = baseApp().copy(
            autoStartConfig = AutoStartConfig(
                bootStartEnabled = true,
                scheduledStartEnabled = true,
                scheduledTime = "12:34",
                scheduledDays = listOf(2, 4, 6),
                scheduledRepeat = false,
                bootDelay = 12345L
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.autoStartConfig).isNotNull()
        assertThat(shell.autoStartConfig?.bootStartEnabled).isTrue()
        assertThat(shell.autoStartConfig?.scheduledStartEnabled).isTrue()
        assertThat(shell.autoStartConfig?.scheduledTime).isEqualTo("12:34")
        assertThat(shell.autoStartConfig?.scheduledDays).containsExactly(2, 4, 6).inOrder()
        assertThat(shell.autoStartConfig?.scheduledRepeat).isFalse()
        assertThat(shell.autoStartConfig?.bootDelay).isEqualTo(12345L)
    }

    @Test
    fun `disguise-family whole-object configs round-trip non-null`() {
        val app = baseApp().copy(
            disguiseConfig = DisguiseConfig(enabled = true),
            browserDisguiseConfig = BrowserDisguiseConfig(),
            deviceDisguiseConfig = DeviceDisguiseConfig(),
            blackTechConfig = DeviceActionsConfig(),
            forcedRunConfig = ForcedRunConfig()
        )
        val shell = roundTrip(app)
        assertThat(shell.disguiseConfig).isNotNull()
        assertThat(shell.browserDisguiseConfig).isNotNull()
        assertThat(shell.deviceDisguiseConfig).isNotNull()
        assertThat(shell.blackTechConfig).isNotNull()
        assertThat(shell.forcedRunConfig).isNotNull()
    }

    @Test
    fun `apkExportConfig engineType and isolation flag round-trip`() {
        val app = baseApp().copy(
            apkExportConfig = ApkExportConfig(
                engineType = "GECKOVIEW",
                isolationConfig = IsolationConfig(enabled = true)
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.engineType).isEqualTo("GECKOVIEW")
        assertThat(shell.isolationEnabled).isTrue()
    }

    @Test
    fun `runtimePermissions are derived from backgroundRun and notification`() {
        val app = baseApp().copy(
            apkExportConfig = ApkExportConfig(
                backgroundRunEnabled = true,
                runtimePermissions = ApkRuntimePermissions(
                    camera = true,
                    location = true,
                    microphone = false
                )
            )
        )
        val shell = roundTrip(app)
        assertThat(shell.backgroundRunEnabled).isTrue()
    }

    @Test
    fun `derived targetUrl is rewritten per appType for non-WEB types`() {
        val gallery = baseApp(appType = AppType.GALLERY).copy(
            galleryConfig = com.webtoapp.data.model.GalleryConfig()
        )
        val shellGallery = roundTrip(gallery)
        assertThat(shellGallery.targetUrl).isEqualTo("gallery://content")
        assertThat(shellGallery.appType).isEqualTo("GALLERY")

        val wordpress = baseApp(appType = AppType.WORDPRESS)
        val shellWp = roundTrip(wordpress)
        assertThat(shellWp.targetUrl).isEqualTo("wordpress://localhost")
        assertThat(shellWp.appType).isEqualTo("WORDPRESS")

        val nodejs = baseApp(appType = AppType.NODEJS_APP)
        val shellNode = roundTrip(nodejs)
        assertThat(shellNode.targetUrl).isEqualTo("file:///android_asset/nodejs_app/index.html")
    }

    @Test
    fun `preview-only fields default empty on export payload`() {
        val shell = roundTrip(baseApp())
        assertThat(shell.previewContentDir).isEmpty()
        assertThat(shell.siteDirName).isEmpty()
        assertThat(shell.siteId).isEmpty()
    }

    @Test
    fun `injectScripts receives build-time kernel and perf sentinels`() {
        val app = baseApp().copy(
            webViewConfig = WebViewConfig(enableKernelDisguise = true)
        )
        val shell = roundTrip(app)
        val names = shell.webViewConfig.injectScripts.map { it.name }
        assertThat(names).contains("__kernel__")
        assertThat(names).contains("__perf_start__")
        assertThat(names).contains("__perf_end__")
    }

    @Test
    fun `darkMode defaults to SYSTEM when context is null`() {
        val shell = roundTrip(baseApp())
        assertThat(shell.darkMode).isEqualTo("SYSTEM")
    }

    @Test
    fun `web app type preserves original url as targetUrl`() {
        val app = baseApp(appType = AppType.WEB).copy(
            url = "https://web.sentinel.example/path?q=1"
        )
        val shell = roundTrip(app)
        assertThat(shell.targetUrl).isEqualTo("https://web.sentinel.example/path?q=1")
        assertThat(shell.appType).isEqualTo("WEB")
    }
}
