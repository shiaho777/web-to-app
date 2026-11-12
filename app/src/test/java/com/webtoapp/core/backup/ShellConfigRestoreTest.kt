package com.webtoapp.core.backup

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.activation.ActivationCode
import com.webtoapp.core.activation.ActivationCodeType
import com.webtoapp.core.apkbuilder.ApkConfigJsonFactory
import com.webtoapp.core.apkbuilder.toApkConfig
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.AdConfig
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.AnnouncementTemplateType
import com.webtoapp.data.model.ApkEncryptionConfig
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.AutoStartConfig
import com.webtoapp.data.model.BackgroundRunExportConfig
import com.webtoapp.data.model.BgmConfig
import com.webtoapp.data.model.BgmItem
import com.webtoapp.data.model.BgmPlayMode
import com.webtoapp.data.model.DisplayCutoutMode
import com.webtoapp.data.model.DownloadLocationMode
import com.webtoapp.data.model.FloatingWindowConfig
import com.webtoapp.data.model.FullscreenVideoOrientation
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.HtmlLoadMode
import com.webtoapp.data.model.KeyboardAdjustMode
import com.webtoapp.data.model.LongPressMenuStyle
import com.webtoapp.data.model.LrcTheme
import com.webtoapp.data.model.MultiWebConfig
import com.webtoapp.data.model.MultiWebSite
import com.webtoapp.data.model.NativeBridgeCapabilities
import com.webtoapp.data.model.NodeJsBuildMode
import com.webtoapp.data.model.NodeJsConfig
import com.webtoapp.data.model.NotificationExportConfig
import com.webtoapp.data.model.NotificationType
import com.webtoapp.data.model.OrientationMode
import com.webtoapp.data.model.PortConflictMode
import com.webtoapp.data.model.RemoteActivationConfig
import com.webtoapp.data.model.ScriptRunTime
import com.webtoapp.data.model.ShareDeliveryMode
import com.webtoapp.data.model.SplashConfig
import com.webtoapp.data.model.SplashOrientation
import com.webtoapp.data.model.SplashType
import com.webtoapp.data.model.StatusBarColorMode
import com.webtoapp.data.model.TranslateConfig
import com.webtoapp.data.model.TranslateLanguage
import com.webtoapp.data.model.UserAgentMode
import com.webtoapp.data.model.UserScript
import com.webtoapp.data.model.ViewportMode
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.util.GsonProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Reverse-direction sentinel for legacy source bundle import (#1538): every
 * value the export projection carries must survive
 * WebApp -> app_config.json -> ShellConfig -> WebApp.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = android.app.Application::class)
class ShellConfigRestoreTest {

    private fun roundTrip(app: WebApp): WebApp {
        val config = app.toApkConfig("com.test.restore", null)
        val json = ApkConfigJsonFactory.toShellConfigJson(config)
        val shell = GsonProvider.gson.fromJson(json, ShellConfig::class.java)
        return shell.toRestoredWebApp()
    }

    private fun baseApp() = WebApp(name = "Legacy", url = "https://legacy.example.com")

    @Test
    fun `web app identity and url survive the restore`() {
        val app = baseApp().copy(
            themeType = "MIDNIGHT",
            apkExportConfig = ApkExportConfig(customVersionName = "2.1.0", customVersionCode = 9)
        )

        val restored = roundTrip(app)

        assertThat(restored.name).isEqualTo("Legacy")
        assertThat(restored.url).isEqualTo("https://legacy.example.com")
        assertThat(restored.appType).isEqualTo(AppType.WEB)
        assertThat(restored.themeType).isEqualTo("MIDNIGHT")
        assertThat(restored.packageName).isEqualTo("com.test.restore")
        assertThat(restored.apkExportConfig?.customVersionName).isEqualTo("2.1.0")
        assertThat(restored.apkExportConfig?.customVersionCode).isEqualTo(9)
    }

    @Test
    fun `webview feature toggles survive the restore`() {
        val app = baseApp().copy(
            webViewConfig = WebViewConfig(
                desktopMode = true,
                userAgentMode = UserAgentMode.CUSTOM,
                customUserAgent = "LegacyUA/1.0",
                orientationMode = OrientationMode.LANDSCAPE,
                displayCutoutMode = DisplayCutoutMode.NEVER,
                keyboardAdjustMode = KeyboardAdjustMode.NOTHING,
                downloadLocationMode = DownloadLocationMode.APP_PRIVATE,
                longPressMenuEnabled = true,
                longPressMenuStyle = LongPressMenuStyle.FULL,
                pageZoomPercent = 80,
                viewportMode = ViewportMode.CUSTOM,
                customViewportWidth = 1280,
                initialScale = 90,
                fullscreenContentPaddingDp = 12,
                fullscreenContentPaddingTopDp = 20,
                statusBarColorMode = StatusBarColorMode.CUSTOM,
                statusBarColor = "#FF112233",
                statusBarDarkIcons = false,
                statusBarHeightDp = 28,
                keepScreenOn = true,
                screenBrightness = 70,
                backButtonBehavior = WebViewConfig.BACK_EXIT,
                forwardVolumeKeys = true,
                consumeVolumeKeys = true,
                mediaAlbumName = "LegacyAlbum",
                antiCapture = true,
                forceHttp3 = true,
                tlsFingerprintEnabled = true,
                tlsFingerprintTemplate = "CHROME_131",
                proxyMode = "MANUAL",
                proxyHost = "127.0.0.1",
                proxyPort = 8888,
                proxyType = "SOCKS",
                proxyUsername = "user",
                proxyPassword = "pass",
                dnsMode = "DOH",
                enableCorsBypass = false,
                enableNativeBridge = true,
                nativeBridgeCapabilities = NativeBridgeCapabilities(directory = true, googleSignIn = true, googleSignInClientId = "cid"),
                geolocationEnabled = true,
                failoverEnabled = true,
                failoverUrls = listOf("https://backup.example.com"),
                failoverTimeoutSeconds = 30,
                autoRefreshEnabled = true,
                autoRefreshIntervalSec = 120,
                receiveShareImages = true,
                receiveShareText = true,
                shareDeliveryMode = ShareDeliveryMode.JS_EVENT,
                openWithEnabled = true,
                enableAppReturn = false,
                customAppReturnSchemes = listOf("legacyapp"),
                staticAssetPackEnabled = true,
                staticAssetPackMaxAgeDays = 7,
                pwaOfflineEnabled = true,
                floatingWindowConfig = FloatingWindowConfig(enabled = true, widthPercent = 60, opacity = 80),
                injectScripts = listOf(UserScript(name = "mine", code = "console.log(1)", runAt = ScriptRunTime.DOCUMENT_START)),
                enableKernelDisguise = true
            )
        )

        val restored = roundTrip(app).webViewConfig

        assertThat(restored.desktopMode).isTrue()
        assertThat(restored.userAgentMode).isEqualTo(UserAgentMode.CUSTOM)
        assertThat(restored.customUserAgent).isEqualTo("LegacyUA/1.0")
        assertThat(restored.orientationMode).isEqualTo(OrientationMode.LANDSCAPE)
        assertThat(restored.displayCutoutMode).isEqualTo(DisplayCutoutMode.NEVER)
        assertThat(restored.keyboardAdjustMode).isEqualTo(KeyboardAdjustMode.NOTHING)
        assertThat(restored.downloadLocationMode).isEqualTo(DownloadLocationMode.APP_PRIVATE)
        assertThat(restored.longPressMenuStyle).isEqualTo(LongPressMenuStyle.FULL)
        assertThat(restored.pageZoomPercent).isEqualTo(80)
        assertThat(restored.viewportMode).isEqualTo(ViewportMode.CUSTOM)
        assertThat(restored.customViewportWidth).isEqualTo(1280)
        assertThat(restored.initialScale).isEqualTo(90)
        assertThat(restored.fullscreenContentPaddingDp).isEqualTo(12)
        assertThat(restored.fullscreenContentPaddingTopDp).isEqualTo(20)
        assertThat(restored.statusBarColorMode).isEqualTo(StatusBarColorMode.CUSTOM)
        assertThat(restored.statusBarColor).isEqualTo("#FF112233")
        assertThat(restored.statusBarDarkIcons).isFalse()
        assertThat(restored.statusBarHeightDp).isEqualTo(28)
        assertThat(restored.keepScreenOn).isTrue()
        assertThat(restored.screenBrightness).isEqualTo(70)
        assertThat(restored.backButtonBehavior).isEqualTo(WebViewConfig.BACK_EXIT)
        assertThat(restored.forwardVolumeKeys).isTrue()
        assertThat(restored.consumeVolumeKeys).isTrue()
        assertThat(restored.mediaAlbumName).isEqualTo("LegacyAlbum")
        assertThat(restored.antiCapture).isTrue()
        assertThat(restored.forceHttp3).isTrue()
        assertThat(restored.tlsFingerprintEnabled).isTrue()
        assertThat(restored.proxyMode).isEqualTo("MANUAL")
        assertThat(restored.proxyHost).isEqualTo("127.0.0.1")
        assertThat(restored.proxyPort).isEqualTo(8888)
        assertThat(restored.proxyType).isEqualTo("SOCKS")
        assertThat(restored.proxyUsername).isEqualTo("user")
        assertThat(restored.proxyPassword).isEqualTo("pass")
        assertThat(restored.dnsMode).isEqualTo("DOH")
        assertThat(restored.enableCorsBypass).isFalse()
        assertThat(restored.nativeBridgeCapabilities.directory).isTrue()
        assertThat(restored.nativeBridgeCapabilities.googleSignIn).isTrue()
        assertThat(restored.nativeBridgeCapabilities.googleSignInClientId).isEqualTo("cid")
        assertThat(restored.geolocationEnabled).isTrue()
        assertThat(restored.failoverEnabled).isTrue()
        assertThat(restored.failoverUrls).containsExactly("https://backup.example.com")
        assertThat(restored.failoverTimeoutSeconds).isEqualTo(30)
        assertThat(restored.autoRefreshEnabled).isTrue()
        assertThat(restored.autoRefreshIntervalSec).isEqualTo(120)
        assertThat(restored.receiveShareImages).isTrue()
        assertThat(restored.receiveShareText).isTrue()
        assertThat(restored.shareDeliveryMode).isEqualTo(ShareDeliveryMode.JS_EVENT)
        assertThat(restored.openWithEnabled).isTrue()
        assertThat(restored.enableAppReturn).isFalse()
        assertThat(restored.customAppReturnSchemes).containsExactly("legacyapp")
        assertThat(restored.staticAssetPackEnabled).isTrue()
        assertThat(restored.staticAssetPackMaxAgeDays).isEqualTo(7)
        assertThat(restored.pwaOfflineEnabled).isTrue()
        assertThat(restored.floatingWindowConfig.enabled).isTrue()
        assertThat(restored.floatingWindowConfig.widthPercent).isEqualTo(60)
        assertThat(restored.floatingWindowConfig.opacity).isEqualTo(80)
        assertThat(restored.fullscreenVideoOrientation).isEqualTo(FullscreenVideoOrientation.AUTO_SENSOR_LANDSCAPE)
    }

    @Test
    fun `internal build scripts are stripped but user scripts survive`() {
        val app = baseApp().copy(
            webViewConfig = WebViewConfig(
                enableKernelDisguise = true,
                injectScripts = listOf(
                    UserScript(name = "mine", code = "console.log(1)", runAt = ScriptRunTime.DOCUMENT_START),
                    UserScript(name = "off", code = "x", enabled = false)
                )
            )
        )

        val scripts = roundTrip(app).webViewConfig.injectScripts

        assertThat(scripts.map { it.name }).containsExactly("mine", "off").inOrder()
        assertThat(scripts.first().runAt).isEqualTo(ScriptRunTime.DOCUMENT_START)
        assertThat(scripts.first().code).isEqualTo("console.log(1)")
        assertThat(scripts.last().enabled).isFalse()
    }

    @Test
    fun `activation announcement ads and translate survive the restore`() {
        val app = baseApp().copy(
            activationEnabled = true,
            activationCodeList = listOf(ActivationCode(code = "ABC", type = ActivationCodeType.PERMANENT)),
            activationRequireEveryTime = true,
            activationRemoteConfig = RemoteActivationConfig(enabled = true, verifyUrl = "https://verify.example.com", deviceBound = true),
            announcementEnabled = true,
            announcement = Announcement(
                title = "Hello",
                content = "<b>world</b>",
                contentIsHtml = true,
                linkUrl = "https://link.example.com",
                template = AnnouncementTemplateType.DARK,
                triggerIntervalMinutes = 30
            ),
            adsEnabled = true,
            adConfig = AdConfig(appId = "ca-app-pub-1", bannerEnabled = true, bannerId = "b1", testMode = true),
            translateEnabled = true,
            translateConfig = TranslateConfig(targetLanguage = TranslateLanguage.ENGLISH, showFloatingButton = false)
        )

        val restored = roundTrip(app)

        assertThat(restored.activationEnabled).isTrue()
        assertThat(restored.activationCodeList.map { it.code }).containsExactly("ABC")
        assertThat(restored.activationRequireEveryTime).isTrue()
        assertThat(restored.activationRemoteConfig?.verifyUrl).isEqualTo("https://verify.example.com")
        assertThat(restored.activationRemoteConfig?.deviceBound).isTrue()
        assertThat(restored.announcementEnabled).isTrue()
        assertThat(restored.announcement?.title).isEqualTo("Hello")
        assertThat(restored.announcement?.contentIsHtml).isTrue()
        assertThat(restored.announcement?.linkUrl).isEqualTo("https://link.example.com")
        assertThat(restored.announcement?.template).isEqualTo(AnnouncementTemplateType.DARK)
        assertThat(restored.announcement?.triggerIntervalMinutes).isEqualTo(30)
        assertThat(restored.adsEnabled).isTrue()
        assertThat(restored.adConfig?.appId).isEqualTo("ca-app-pub-1")
        assertThat(restored.adConfig?.bannerId).isEqualTo("b1")
        assertThat(restored.adConfig?.testMode).isTrue()
        assertThat(restored.translateEnabled).isTrue()
        assertThat(restored.translateConfig?.targetLanguage).isEqualTo(TranslateLanguage.ENGLISH)
        assertThat(restored.translateConfig?.showFloatingButton).isFalse()
    }

    @Test
    fun `splash bgm autostart and export services survive the restore`() {
        val app = baseApp().copy(
            splashEnabled = true,
            splashConfig = SplashConfig(
                type = SplashType.VIDEO,
                duration = 7,
                orientation = SplashOrientation.LANDSCAPE,
                videoStartMs = 100,
                videoEndMs = 4000,
                enableAudio = true,
                showCountdown = false
            ),
            bgmEnabled = true,
            bgmConfig = BgmConfig(
                playlist = listOf(BgmItem(id = "t1", name = "Track", path = "/tmp/track.mp3", sortOrder = 3)),
                playMode = BgmPlayMode.SHUFFLE,
                volume = 0.8f,
                showFloatingPlayer = false,
                lrcTheme = LrcTheme(id = "th", name = "Theme", fontSize = 22f)
            ),
            autoStartConfig = AutoStartConfig(
                bootStartEnabled = true,
                scheduledStartEnabled = true,
                scheduledTime = "09:30",
                scheduledDays = listOf(1, 3, 5)
            ),
            apkExportConfig = ApkExportConfig(
                engineType = "GECKOVIEW",
                loggingEnabled = true,
                deepLinkEnabled = true,
                customDeepLinkHosts = listOf("custom.example.com"),
                backgroundRunEnabled = true,
                backgroundRunConfig = BackgroundRunExportConfig(notificationTitle = "Running", keepCpuAwake = false),
                notificationEnabled = true,
                notificationConfig = NotificationExportConfig(type = NotificationType.POLLING, pollUrl = "https://poll.example.com", pollIntervalMinutes = 5),
                encryptionConfig = ApkEncryptionConfig(enabled = true, threatResponse = ApkEncryptionConfig.ThreatResponse.SILENT_EXIT)
            )
        )

        val restored = roundTrip(app)

        assertThat(restored.splashEnabled).isTrue()
        assertThat(restored.splashConfig?.type).isEqualTo(SplashType.VIDEO)
        assertThat(restored.splashConfig?.duration).isEqualTo(7)
        assertThat(restored.splashConfig?.orientation).isEqualTo(SplashOrientation.LANDSCAPE)
        assertThat(restored.splashConfig?.videoStartMs).isEqualTo(100)
        assertThat(restored.splashConfig?.videoEndMs).isEqualTo(4000)
        assertThat(restored.splashConfig?.showCountdown).isFalse()
        assertThat(restored.bgmEnabled).isTrue()
        assertThat(restored.bgmConfig?.playlist?.single()?.name).isEqualTo("Track")
        assertThat(restored.bgmConfig?.playlist?.single()?.sortOrder).isEqualTo(3)
        assertThat(restored.bgmConfig?.playMode).isEqualTo(BgmPlayMode.SHUFFLE)
        assertThat(restored.bgmConfig?.volume).isEqualTo(0.8f)
        assertThat(restored.bgmConfig?.showFloatingPlayer).isFalse()
        assertThat(restored.bgmConfig?.lrcTheme?.fontSize).isEqualTo(22f)
        assertThat(restored.autoStartConfig?.bootStartEnabled).isTrue()
        assertThat(restored.autoStartConfig?.scheduledTime).isEqualTo("09:30")
        assertThat(restored.autoStartConfig?.scheduledDays).containsExactly(1, 3, 5).inOrder()
        assertThat(restored.apkExportConfig?.engineType).isEqualTo("GECKOVIEW")
        assertThat(restored.apkExportConfig?.loggingEnabled).isTrue()
        assertThat(restored.apkExportConfig?.deepLinkEnabled).isTrue()
        assertThat(restored.apkExportConfig?.customDeepLinkHosts).contains("custom.example.com")
        assertThat(restored.apkExportConfig?.backgroundRunEnabled).isTrue()
        assertThat(restored.apkExportConfig?.backgroundRunConfig?.notificationTitle).isEqualTo("Running")
        assertThat(restored.apkExportConfig?.backgroundRunConfig?.keepCpuAwake).isFalse()
        assertThat(restored.apkExportConfig?.notificationEnabled).isTrue()
        assertThat(restored.apkExportConfig?.notificationConfig?.type).isEqualTo(NotificationType.POLLING)
        assertThat(restored.apkExportConfig?.notificationConfig?.pollIntervalMinutes).isEqualTo(5)
        assertThat(restored.apkExportConfig?.encryptionConfig?.enabled).isTrue()
        assertThat(restored.apkExportConfig?.encryptionConfig?.threatResponse).isEqualTo(ApkEncryptionConfig.ThreatResponse.SILENT_EXIT)
    }

    @Test
    fun `multi web sites and layout survive the restore`() {
        val app = WebApp(
            name = "Multi",
            url = "",
            appType = AppType.MULTI_WEB,
            multiWebConfig = MultiWebConfig(
                sites = listOf(
                    MultiWebSite(id = "s1", name = "One", url = "https://one.example.com", sortIndex = 0),
                    MultiWebSite(id = "s2", name = "Two", url = "https://two.example.com", type = "INLINE_HTML", inlineHtml = "<p>two</p>", sortIndex = 1, iconEmoji = "T")
                ),
                displayMode = "CARDS",
                cardColumns = 3,
                sitesUseOwnConfig = true,
                startTab = "SITE",
                startSiteId = "s2",
                independentSessions = true
            )
        )

        val restored = roundTrip(app)
        val config = restored.multiWebConfig

        assertThat(restored.appType).isEqualTo(AppType.MULTI_WEB)
        assertThat(config?.displayMode).isEqualTo("CARDS")
        assertThat(config?.cardColumns).isEqualTo(3)
        assertThat(config?.sitesUseOwnConfig).isTrue()
        assertThat(config?.startTab).isEqualTo("SITE")
        assertThat(config?.startSiteId).isEqualTo("s2")
        assertThat(config?.independentSessions).isTrue()
        assertThat(config?.sites?.map { it.id }).containsExactly("s1", "s2").inOrder()
        assertThat(config?.sites?.first()?.url).isEqualTo("https://one.example.com")
        assertThat(config?.sites?.last()?.iconEmoji).isEqualTo("T")
        assertThat(config?.sites?.all { it.sourceAppId == 0L }).isTrue()
    }

    @Test
    fun `html and nodejs project settings survive the restore`() {
        val html = WebApp(
            name = "Page",
            url = "",
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(projectId = "p1", entryFile = "main.html", loadMode = HtmlLoadMode.LOCAL_HTTP, port = 8080)
        )
        val node = WebApp(
            name = "Server",
            url = "",
            appType = AppType.NODEJS_APP,
            nodejsConfig = NodeJsConfig(projectId = "p2", buildMode = NodeJsBuildMode.SSR, entryFile = "server.js", serverPort = 3000, envVars = mapOf("K" to "V"), portConflictMode = PortConflictMode.ALERT)
        )

        val restoredHtml = roundTrip(html)
        val restoredNode = roundTrip(node)

        assertThat(restoredHtml.appType).isEqualTo(AppType.HTML)
        assertThat(restoredHtml.htmlConfig?.entryFile).isEqualTo("main.html")
        assertThat(restoredHtml.htmlConfig?.loadMode).isEqualTo(HtmlLoadMode.LOCAL_HTTP)
        assertThat(restoredHtml.htmlConfig?.port).isEqualTo(8080)
        assertThat(restoredNode.appType).isEqualTo(AppType.NODEJS_APP)
        assertThat(restoredNode.nodejsConfig?.buildMode).isEqualTo(NodeJsBuildMode.SSR)
        assertThat(restoredNode.nodejsConfig?.entryFile).isEqualTo("server.js")
        assertThat(restoredNode.nodejsConfig?.serverPort).isEqualTo(3000)
        assertThat(restoredNode.nodejsConfig?.envVars).containsEntry("K", "V")
        assertThat(restoredNode.nodejsConfig?.portConflictMode).isEqualTo(PortConflictMode.ALERT)
    }

    @Test
    fun `plugin references survive the restore`() {
        val app = baseApp().copy(pluginsEnabled = true, pluginIds = listOf("p1", "p2"))

        val restored = roundTrip(app)

        assertThat(restored.pluginsEnabled).isTrue()
        assertThat(restored.pluginIds).containsExactly("p1", "p2").inOrder()
    }
}