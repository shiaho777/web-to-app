package com.webtoapp.core.backup

import com.webtoapp.core.errorpage.ErrorPageConfig
import com.webtoapp.core.errorpage.ErrorPageMode
import com.webtoapp.core.errorpage.ErrorPageStyle
import com.webtoapp.core.errorpage.MiniGameType
import com.webtoapp.core.kernel.KernelFlavor
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.ActivationDialogConfig
import com.webtoapp.data.model.AdConfig
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.AnnouncementTemplateType
import com.webtoapp.data.model.ApkEncryptionConfig
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.AutoStartConfig
import com.webtoapp.data.model.BackgroundRunExportConfig
import com.webtoapp.data.model.Base64DeepLinkMode
import com.webtoapp.data.model.BgmConfig
import com.webtoapp.data.model.BgmItem
import com.webtoapp.data.model.BgmPlayMode
import com.webtoapp.data.model.BlobInterceptScope
import com.webtoapp.data.model.CloudflareCompatMode
import com.webtoapp.data.model.DisplayCutoutMode
import com.webtoapp.data.model.DnsConfig
import com.webtoapp.data.model.DownloadLocationMode
import com.webtoapp.data.model.FailoverTriggers
import com.webtoapp.data.model.FloatingBorderStyle
import com.webtoapp.data.model.FloatingWindowAspectRatioMode
import com.webtoapp.data.model.FloatingWindowConfig
import com.webtoapp.data.model.FullscreenVideoOrientation
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import com.webtoapp.data.model.GalleryPlayMode
import com.webtoapp.data.model.GallerySortOrder
import com.webtoapp.data.model.GalleryViewMode
import com.webtoapp.data.model.GeolocationAccuracy
import com.webtoapp.data.model.GeolocationPolicy
import com.webtoapp.data.model.GoAppConfig
import com.webtoapp.data.model.HostMappingEntry
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.HtmlLoadMode
import com.webtoapp.data.model.JsOpenWindowsPolicy
import com.webtoapp.data.model.KeyboardAdjustMode
import com.webtoapp.data.model.KernelDisguiseLevel
import com.webtoapp.data.model.LongPressMenuStyle
import com.webtoapp.data.model.LrcAnimationType
import com.webtoapp.data.model.LrcPosition
import com.webtoapp.data.model.LrcTheme
import com.webtoapp.data.model.MediaAutoplayScope
import com.webtoapp.data.model.MediaConfig
import com.webtoapp.data.model.MixedContentMode
import com.webtoapp.data.model.MultiWebConfig
import com.webtoapp.data.model.MultiWebSite
import com.webtoapp.data.model.NativeBridgeCapabilities
import com.webtoapp.data.model.NewWindowBehavior
import com.webtoapp.data.model.NodeJsBuildMode
import com.webtoapp.data.model.NodeJsConfig
import com.webtoapp.data.model.NotificationExportConfig
import com.webtoapp.data.model.NotificationType
import com.webtoapp.data.model.OrientationMode
import com.webtoapp.data.model.PhpAppConfig
import com.webtoapp.data.model.PortConflictMode
import com.webtoapp.data.model.PrimeUserActivationMode
import com.webtoapp.data.model.PrimeUserActivationTiming
import com.webtoapp.data.model.PrivateNetworkScope
import com.webtoapp.data.model.PythonAppConfig
import com.webtoapp.data.model.RemoteActivationConfig
import com.webtoapp.data.model.RemoteActivationOfflinePolicy
import com.webtoapp.data.model.ScreenAwakeMode
import com.webtoapp.data.model.ScriptRunTime
import com.webtoapp.data.model.ShareDeliveryMode
import com.webtoapp.data.model.SplashConfig
import com.webtoapp.data.model.SplashOrientation
import com.webtoapp.data.model.SplashType
import com.webtoapp.data.model.StatusBarBackgroundType
import com.webtoapp.data.model.StatusBarColorMode
import com.webtoapp.data.model.SwipeRefreshZone
import com.webtoapp.data.model.ThirdPartyCookieMode
import com.webtoapp.data.model.TranslateConfig
import com.webtoapp.data.model.TranslateLanguage
import com.webtoapp.data.model.UserAgentMode
import com.webtoapp.data.model.UserScript
import com.webtoapp.data.model.ViewportMode
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.data.model.WordPressConfig

private inline fun <reified E : Enum<E>> enumOrDefault(name: String, default: E): E =
    enumValues<E>().firstOrNull { it.name == name } ?: default

private val INTERNAL_SCRIPT_NAMES = setOf("__kernel__", "__perf_start__", "__perf_end__")

private val APP_RETURN_SCHEMES = setOf("mqqopensdkapi", "mqqopensdkapiV2", "mqqopensdkapiV3")

/**
 * Reverse of the export projection for legacy source bundles: rebuilds the
 * editor project record from the shell configuration an old export carried
 * (`app_config.json`, parsed as [ShellConfig]). Values the projection never
 * serialized (host dark mode/language, runtime permission grants, SDK
 * overrides, per-site configs, lyric parsing) fall back to defaults; file
 * paths are rewritten by [LegacySourceBundleImporter] afterwards.
 */
internal fun ShellConfig.toRestoredWebApp(): WebApp {
    val type = AppType.fromPersistedName(appType) ?: AppType.WEB
    val wv = webViewConfig
    val fw = wv.floatingWindowConfig
    val ep = wv.errorPageConfig
    val gallery = galleryConfig
    val media = mediaConfig
    val html = htmlConfig
    val multiWeb = multiWebConfig
    return WebApp(
        name = appName,
        url = if (type == AppType.WEB) targetUrl else "",
        packageName = packageName,
        appType = type,
        mediaConfig = if (type == AppType.IMAGE || type == AppType.VIDEO) {
            MediaConfig(
                mediaPath = "",
                enableAudio = media.enableAudio,
                loop = media.loop,
                autoPlay = media.autoPlay,
                fillScreen = media.fillScreen,
                orientation = if (media.landscape) SplashOrientation.LANDSCAPE else SplashOrientation.PORTRAIT,
                backgroundColor = media.backgroundColor,
                keepScreenOn = media.keepScreenOn
            )
        } else null,
        galleryConfig = if (type == AppType.GALLERY) {
            GalleryConfig(
                items = gallery.items.mapIndexed { index, item ->
                    GalleryItem(
                        id = item.id,
                        path = "",
                        type = enumOrDefault(item.type, GalleryItemType.IMAGE),
                        name = item.name,
                        duration = item.duration,
                        sortIndex = index
                    )
                },
                playMode = enumOrDefault(gallery.playMode, GalleryPlayMode.SEQUENTIAL),
                imageInterval = gallery.imageInterval,
                loop = gallery.loop,
                autoPlay = gallery.autoPlay,
                shuffleOnLoop = gallery.shuffleOnLoop,
                defaultView = enumOrDefault(gallery.defaultView, GalleryViewMode.GRID),
                gridColumns = gallery.gridColumns,
                sortOrder = enumOrDefault(gallery.sortOrder, GallerySortOrder.CUSTOM),
                backgroundColor = gallery.backgroundColor,
                showThumbnailBar = gallery.showThumbnailBar,
                showMediaInfo = gallery.showMediaInfo,
                orientation = enumOrDefault(gallery.orientation, SplashOrientation.PORTRAIT),
                enableAudio = gallery.enableAudio,
                videoAutoNext = gallery.videoAutoNext,
                rememberPosition = gallery.rememberPosition
            )
        } else null,
        htmlConfig = if (type == AppType.HTML || type == AppType.FRONTEND) {
            HtmlConfig(
                entryFile = html.getValidEntryFile(),
                enableJavaScript = html.enableJavaScript,
                enableLocalStorage = html.enableLocalStorage,
                backgroundColor = html.backgroundColor,
                loadMode = enumOrDefault(html.loadMode, HtmlLoadMode.AUTO),
                port = html.port,
                portConflictMode = enumOrDefault(html.portConflictMode, PortConflictMode.AUTO_KILL)
            )
        } else null,
        wordpressConfig = if (type == AppType.WORDPRESS) {
            WordPressConfig(
                siteTitle = wordpressConfig.siteTitle,
                adminUser = wordpressConfig.adminUser,
                adminEmail = wordpressConfig.adminEmail,
                adminPassword = wordpressConfig.adminPassword,
                themeName = wordpressConfig.themeName,
                plugins = wordpressConfig.plugins,
                activePlugins = wordpressConfig.activePlugins,
                permalinkStructure = wordpressConfig.permalinkStructure,
                siteLanguage = wordpressConfig.siteLanguage,
                autoInstall = wordpressConfig.autoInstall,
                phpPort = wordpressConfig.phpPort,
                portConflictMode = enumOrDefault(wordpressConfig.portConflictMode, PortConflictMode.AUTO_KILL),
                customPhpExtensions = wordpressConfig.customPhpExtensions
            )
        } else null,
        nodejsConfig = if (type == AppType.NODEJS_APP) {
            NodeJsConfig(
                buildMode = enumOrDefault(nodejsConfig.mode, NodeJsBuildMode.STATIC),
                entryFile = nodejsConfig.entryFile,
                serverPort = nodejsConfig.port,
                portConflictMode = enumOrDefault(nodejsConfig.portConflictMode, PortConflictMode.AUTO_KILL),
                envVars = nodejsConfig.envVars,
                customNodeExtensions = nodejsConfig.customNodeExtensions
            )
        } else null,
        phpAppConfig = if (type == AppType.PHP_APP) {
            PhpAppConfig(
                framework = phpAppConfig.framework,
                documentRoot = phpAppConfig.documentRoot,
                entryFile = phpAppConfig.entryFile,
                phpPort = phpAppConfig.port,
                portConflictMode = enumOrDefault(phpAppConfig.portConflictMode, PortConflictMode.AUTO_KILL),
                envVars = phpAppConfig.envVars,
                phpExtensions = phpAppConfig.phpExtensions,
                customPhpExtensions = phpAppConfig.customPhpExtensions
            )
        } else null,
        pythonAppConfig = if (type == AppType.PYTHON_APP) {
            PythonAppConfig(
                framework = pythonAppConfig.framework,
                entryFile = pythonAppConfig.entryFile,
                entryModule = pythonAppConfig.entryModule,
                serverType = pythonAppConfig.serverType,
                serverPort = pythonAppConfig.port,
                portConflictMode = enumOrDefault(pythonAppConfig.portConflictMode, PortConflictMode.AUTO_KILL),
                envVars = pythonAppConfig.envVars,
                customPythonExtensions = pythonAppConfig.customPythonExtensions
            )
        } else null,
        goAppConfig = if (type == AppType.GO_APP) {
            GoAppConfig(
                framework = goAppConfig.framework,
                binaryName = goAppConfig.binaryName,
                targetArch = goAppConfig.targetArch,
                serverPort = goAppConfig.port,
                portConflictMode = enumOrDefault(goAppConfig.portConflictMode, PortConflictMode.AUTO_KILL),
                envVars = goAppConfig.envVars,
                staticDir = goAppConfig.staticDir
            )
        } else null,
        multiWebConfig = if (type == AppType.MULTI_WEB) {
            MultiWebConfig(
                sites = multiWeb.sites.map { site ->
                    MultiWebSite(
                        id = site.id,
                        name = site.name,
                        url = site.url,
                        type = site.type,
                        localFilePath = site.localFilePath,
                        iconEmoji = site.iconEmoji,
                        faviconUrl = site.faviconUrl,
                        themeColor = site.themeColor,
                        category = site.category,
                        cssSelector = site.cssSelector,
                        linkSelector = site.linkSelector,
                        enabled = site.enabled,
                        sortIndex = site.sortIndex,
                        appType = site.appType,
                        sourceProjectId = site.sourceProjectId,
                        siteProjectId = site.siteProjectId
                    )
                },
                displayMode = multiWeb.displayMode,
                refreshInterval = multiWeb.refreshInterval,
                showSiteIcons = multiWeb.showSiteIcons,
                cardColumns = multiWeb.cardColumns,
                drawerColumns = multiWeb.drawerColumns,
                drawerEntry = multiWeb.drawerEntry,
                drawerFabAnchor = multiWeb.drawerFabAnchor,
                cardAspectRatio = multiWeb.cardAspectRatio,
                sitesUseOwnConfig = multiWeb.sitesUseOwnConfig,
                projectId = multiWeb.projectId,
                startTab = multiWeb.startTab,
                startSiteId = multiWeb.startSiteId,
                independentSessions = multiWeb.independentSessions
            )
        } else null,
        activationEnabled = activationEnabled,
        activationCodeList = activationCodes.mapNotNull { com.webtoapp.core.activation.ActivationCode.fromJson(it) },
        activationRequireEveryTime = activationRequireEveryTime,
        activationRemoteConfig = if (activationRemoteEnabled) {
            RemoteActivationConfig(
                enabled = true,
                verifyUrl = activationRemoteVerifyUrl,
                publicKeyBase64 = activationRemotePublicKey,
                offlinePolicy = enumOrDefault(activationRemoteOfflinePolicy, RemoteActivationOfflinePolicy.ALLOW_CACHED),
                deliverUrl = activationRemoteDeliverUrl,
                encryptUrl = activationRemoteEncryptUrl,
                aesKeyBase64 = activationRemoteAesKey,
                deviceBound = activationRemoteDeviceBound
            )
        } else null,
        activationDialogConfig = if (
            activationDialogTitle.isNotBlank() || activationDialogSubtitle.isNotBlank() ||
            activationDialogInputLabel.isNotBlank() || activationDialogButtonText.isNotBlank()
        ) {
            ActivationDialogConfig(
                title = activationDialogTitle,
                subtitle = activationDialogSubtitle,
                inputLabel = activationDialogInputLabel,
                buttonText = activationDialogButtonText
            )
        } else null,
        adsEnabled = adsEnabled,
        adConfig = AdConfig(
            appId = adAppId,
            bannerEnabled = adBannerEnabled,
            bannerId = adBannerId,
            interstitialEnabled = adInterstitialEnabled,
            interstitialId = adInterstitialId,
            splashEnabled = adSplashEnabled,
            splashId = adSplashId,
            testMode = adTestMode
        ),
        announcementEnabled = announcementEnabled,
        announcement = Announcement(
            title = announcementTitle,
            content = announcementContent,
            contentIsHtml = announcementContentIsHtml,
            linkUrl = announcementLink.ifBlank { null },
            linkText = announcementLinkText.ifBlank { null },
            showOnce = announcementShowOnce,
            enabled = announcementEnabled,
            version = announcementVersion,
            template = enumOrDefault(announcementTemplate, AnnouncementTemplateType.MINIMAL),
            requireConfirmation = announcementRequireConfirmation,
            allowNeverShow = announcementAllowNeverShow,
            triggerOnLaunch = announcementTriggerOnLaunch,
            triggerOnNoNetwork = announcementTriggerOnNoNetwork,
            triggerIntervalMinutes = announcementTriggerIntervalMinutes,
            triggerIntervalIncludeLaunch = announcementTriggerIntervalIncludeLaunch,
            showIcon = announcementShowIcon
        ),
        adBlockEnabled = adBlockEnabled,
        adBlockRules = adBlockRules,
        adBlockSubscriptions = adBlockSubscriptions,
        webViewConfig = WebViewConfig(
            javaScriptEnabled = wv.javaScriptEnabled,
            domStorageEnabled = wv.domStorageEnabled,
            allowFileAccess = wv.allowFileAccess,
            allowContentAccess = wv.allowContentAccess,
            cacheEnabled = wv.cacheEnabled,
            clearBrowsingDataOnLaunch = wv.clearBrowsingDataOnLaunch,
            clientCertificateAuthEnabled = wv.clientCertificateAuthEnabled,
            userAgent = wv.userAgent,
            userAgentMode = enumOrDefault(wv.userAgentMode, UserAgentMode.DEFAULT),
            customUserAgent = wv.customUserAgent,
            desktopMode = wv.desktopMode,
            zoomEnabled = wv.zoomEnabled,
            swipeRefreshEnabled = wv.swipeRefreshEnabled,
            swipeRefreshZone = enumOrDefault(wv.swipeRefreshZone, SwipeRefreshZone.TOP_EDGE),
            overscrollEffectEnabled = wv.overscrollEffectEnabled,
            autoRefreshEnabled = wv.autoRefreshEnabled,
            autoRefreshIntervalSec = wv.autoRefreshIntervalSec,
            autoRefreshShowCountdown = wv.autoRefreshShowCountdown,
            fullscreenEnabled = wv.fullscreenEnabled,
            downloadEnabled = wv.downloadEnabled,
            downloadLocationMode = enumOrDefault(wv.downloadLocationMode, DownloadLocationMode.SYSTEM_DOWNLOAD),
            customDownloadDirUri = wv.customDownloadDirUri,
            openExternalLinks = wv.openExternalLinks,
            browserToolbarEnabled = wv.browserToolbarEnabled,
            toolbarShowTitle = wv.toolbarShowTitle,
            toolbarShowUrl = wv.toolbarShowUrl,
            toolbarShowBack = wv.toolbarShowBack,
            toolbarShowForward = wv.toolbarShowForward,
            toolbarShowRefresh = wv.toolbarShowRefresh,
            toolbarShowConsole = wv.toolbarShowConsole,
            toolbarShowFind = wv.toolbarShowFind,
            hideToolbar = wv.hideToolbar,
            showStatusBarInFullscreen = wv.showStatusBarInFullscreen,
            hideStatusBarInVideoFullscreen = wv.hideStatusBarInVideoFullscreen,
            showNavigationBarInFullscreen = wv.showNavigationBarInFullscreen,
            displayCutoutMode = enumOrDefault(wv.displayCutoutMode, DisplayCutoutMode.SHORT_EDGES),
            showToolbarInFullscreen = wv.showToolbarInFullscreen,
            fullscreenContentPaddingDp = wv.fullscreenContentPaddingDp,
            fullscreenContentPaddingTopDp = wv.fullscreenContentPaddingTopDp,
            fullscreenContentPaddingBottomDp = wv.fullscreenContentPaddingBottomDp,
            fullscreenContentPaddingStartDp = wv.fullscreenContentPaddingStartDp,
            fullscreenContentPaddingEndDp = wv.fullscreenContentPaddingEndDp,
            landscapeMode = wv.landscapeMode,
            orientationMode = enumOrDefault(wv.orientationMode, OrientationMode.PORTRAIT),
            injectScripts = wv.injectScripts
                .filterNot { it.name in INTERNAL_SCRIPT_NAMES }
                .map {
                    UserScript(
                        name = it.name,
                        code = it.code,
                        enabled = it.enabled,
                        runAt = enumOrDefault(it.runAt, ScriptRunTime.DOCUMENT_END)
                    )
                },
            statusBarColorMode = enumOrDefault(wv.statusBarColorMode, StatusBarColorMode.TRANSPARENT),
            statusBarColor = wv.statusBarColor,
            statusBarDarkIcons = wv.statusBarDarkIcons,
            statusBarBackgroundType = enumOrDefault(wv.statusBarBackgroundType, StatusBarBackgroundType.COLOR),
            statusBarBackgroundImage = null,
            statusBarBackgroundAlpha = wv.statusBarBackgroundAlpha,
            statusBarHeightDp = wv.statusBarHeightDp,
            statusBarColorModeDark = enumOrDefault(wv.statusBarColorModeDark, StatusBarColorMode.TRANSPARENT),
            statusBarColorDark = wv.statusBarColorDark,
            statusBarDarkIconsDark = wv.statusBarDarkIconsDark,
            statusBarBackgroundTypeDark = enumOrDefault(wv.statusBarBackgroundTypeDark, StatusBarBackgroundType.COLOR),
            statusBarBackgroundImageDark = null,
            statusBarBackgroundAlphaDark = wv.statusBarBackgroundAlphaDark,
            followSystemDarkMode = wv.followSystemDarkMode,
            longPressMenuEnabled = wv.longPressMenuEnabled,
            longPressMenuStyle = enumOrDefault(wv.longPressMenuStyle, LongPressMenuStyle.DISABLED),
            longPressHapticOnActionOnly = wv.longPressHapticOnActionOnly,
            popupBlockerEnabled = wv.popupBlockerEnabled,
            popupBlockerToggleEnabled = wv.popupBlockerToggleEnabled,
            initialScale = wv.initialScale,
            pageZoomPercent = wv.pageZoomPercent,
            viewportMode = enumOrDefault(wv.viewportMode, ViewportMode.DEFAULT),
            customViewportWidth = wv.customViewportWidth,
            newWindowBehavior = enumOrDefault(wv.newWindowBehavior, NewWindowBehavior.SAME_WINDOW),
            enablePaymentSchemes = wv.enablePaymentSchemes,
            enableAppReturn = deepLinkSchemes.any { it in APP_RETURN_SCHEMES },
            customAppReturnSchemes = deepLinkSchemes.filterNot { it.startsWith("wta-") || it in APP_RETURN_SCHEMES },
            enableShareBridge = wv.enableShareBridge,
            receiveShareImages = wv.receiveShareImages,
            receiveShareText = wv.receiveShareText,
            openWithEnabled = wv.openWithEnabled,
            shareDeliveryMode = enumOrDefault(wv.shareDeliveryMode, ShareDeliveryMode.BOTH),
            sharePromptBeforeUse = wv.sharePromptBeforeUse,
            shareShowReceivedToast = wv.shareShowReceivedToast,
            enableZoomPolyfill = wv.enableZoomPolyfill,
            enableCrossOriginIsolation = wv.enableCrossOriginIsolation,
            hideUrlPreview = wv.hideUrlPreview,
            decodeBase64DeepLinks = wv.decodeBase64DeepLinks,
            decodeBase64Mode = enumOrDefault(wv.decodeBase64Mode, Base64DeepLinkMode.GESTURE_ONLY),
            javaScriptCanOpenWindows = wv.javaScriptCanOpenWindows,
            jsOpenWindowsPolicy = enumOrDefault(wv.jsOpenWindowsPolicy, JsOpenWindowsPolicy.ALLOW),
            mediaAutoplayEnabled = wv.mediaAutoplayEnabled,
            mediaAutoplayScope = enumOrDefault(wv.mediaAutoplayScope, MediaAutoplayScope.VIDEO_ONLY),
            enableImageRepair = wv.enableImageRepair,
            enableScrollMemory = wv.enableScrollMemory,
            enableBackStatePreservation = wv.enableBackStatePreservation,
            enableKernelDisguise = wv.enableKernelDisguise,
            kernelDisguiseLevel = enumOrDefault(wv.kernelDisguiseLevel, KernelDisguiseLevel.STANDARD),
            kernelFlavor = enumOrDefault(wv.kernelFlavor, KernelFlavor.SYSTEM_DEFAULT),
            enableCloudflareCompat = wv.enableCloudflareCompat,
            cloudflareCompatMode = enumOrDefault(wv.cloudflareCompatMode, CloudflareCompatMode.AUTO_DETECT),
            allowMixedContent = wv.allowMixedContent,
            mixedContentMode = enumOrDefault(wv.mixedContentMode, MixedContentMode.COMPATIBILITY),
            enablePrivateNetworkBridge = wv.enablePrivateNetworkBridge,
            privateNetworkScope = enumOrDefault(wv.privateNetworkScope, PrivateNetworkScope.LOCAL_ONLY),
            enableCorsBypass = wv.enableCorsBypass,
            acceptThirdPartyCookies = wv.acceptThirdPartyCookies,
            thirdPartyCookieMode = enumOrDefault(wv.thirdPartyCookieMode, ThirdPartyCookieMode.SAME_SITE_LAX),
            databaseEnabled = wv.databaseEnabled,
            enableCookiePersistence = wv.enableCookiePersistence,
            enableClipboardPolyfill = wv.enableClipboardPolyfill,
            enableNotificationPolyfill = wv.enableNotificationPolyfill,
            enableOrientationPolyfill = wv.enableOrientationPolyfill,
            enableCompatPolyfills = wv.enableCompatPolyfills,
            enableNativeBridge = wv.enableNativeBridge,
            nativeBridgeCapabilities = NativeBridgeCapabilities(
                clipboard = wv.nativeBridgeClipboard,
                vibration = wv.nativeBridgeVibration,
                geolocation = wv.nativeBridgeGeolocation,
                brightness = wv.nativeBridgeBrightness,
                notification = wv.nativeBridgeNotification,
                notificationScheduled = wv.nativeBridgeNotificationScheduled,
                notificationPersistent = wv.nativeBridgeNotificationPersistent,
                download = wv.nativeBridgeDownload,
                privateNetwork = wv.nativeBridgePrivateNetwork,
                screenWake = wv.nativeBridgeScreenWake,
                openExternal = wv.nativeBridgeOpenExternal,
                deviceInfo = wv.nativeBridgeDeviceInfo,
                securityInfo = wv.nativeBridgeSecurityInfo,
                networkInfo = wv.nativeBridgeNetworkInfo,
                toast = wv.nativeBridgeToast,
                logging = wv.nativeBridgeLogging,
                findInPage = wv.nativeBridgeFindInPage,
                orientation = wv.nativeBridgeOrientation,
                fullscreen = wv.nativeBridgeFullscreen,
                print = wv.nativeBridgePrint,
                googleSignIn = wv.nativeBridgeGoogleSignIn,
                googleSignInClientId = wv.nativeBridgeGoogleSignInClientId,
                directory = wv.nativeBridgeDirectory
            ),
            geolocationEnabled = wv.geolocationEnabled,
            geolocationAccuracy = enumOrDefault(wv.geolocationAccuracy, GeolocationAccuracy.COARSE),
            geolocationPolicy = enumOrDefault(wv.geolocationPolicy, GeolocationPolicy.ALWAYS_ASK),
            enableBlobDownloadInterception = wv.enableBlobDownloadInterception,
            blobInterceptScope = enumOrDefault(wv.blobInterceptScope, BlobInterceptScope.ALL),
            blobInterceptThresholdMb = wv.blobInterceptThresholdMb,
            enablePrintBridge = wv.enablePrintBridge,
            enableMediaSession = wv.enableMediaSession,
            backgroundMediaPlayback = wv.backgroundMediaPlayback,
            primeUserActivation = wv.primeUserActivation,
            primeUserActivationMode = enumOrDefault(wv.primeUserActivationMode, PrimeUserActivationMode.SYNTHETIC_TAP),
            primeUserActivationTiming = enumOrDefault(wv.primeUserActivationTiming, PrimeUserActivationTiming.ON_PAGE_FINISHED),
            failoverEnabled = wv.failoverEnabled,
            failoverUrls = wv.failoverUrls,
            failoverTriggers = FailoverTriggers(
                networkError = wv.failoverTriggerNetworkError,
                http5xx = wv.failoverTriggerHttp5xx,
                http4xx = wv.failoverTriggerHttp4xx,
                timeout = wv.failoverTriggerTimeout
            ),
            failoverTimeoutSeconds = wv.failoverTimeoutSeconds,
            fullscreenVideoOrientation = enumOrDefault(wv.fullscreenVideoOrientation, FullscreenVideoOrientation.AUTO_SENSOR_LANDSCAPE),
            keepScreenOn = wv.keepScreenOn,
            screenAwakeMode = enumOrDefault(wv.screenAwakeMode, ScreenAwakeMode.OFF),
            screenAwakeTimeoutMinutes = wv.screenAwakeTimeoutMinutes,
            screenBrightness = wv.screenBrightness,
            keyboardAdjustMode = enumOrDefault(wv.keyboardAdjustMode, KeyboardAdjustMode.RESIZE),
            errorPageConfig = ErrorPageConfig(
                mode = enumOrDefault(ep.mode, ErrorPageMode.BUILTIN_STYLE),
                builtInStyle = enumOrDefault(ep.builtInStyle, ErrorPageStyle.MATERIAL),
                customHtml = ep.customHtml.ifBlank { null },
                customMediaPath = ep.customMediaPath.takeIf { it.startsWith("data:") },
                showMiniGame = ep.showMiniGame,
                miniGameType = enumOrDefault(ep.miniGameType, MiniGameType.RANDOM),
                retryButtonText = ep.retryButtonText,
                autoRetrySeconds = ep.autoRetrySeconds,
                showHttp4xxErrorUi = ep.showHttp4xxErrorUi,
                showHttp5xxErrorUi = ep.showHttp5xxErrorUi,
                showNetworkErrorUi = ep.showNetworkErrorUi,
                showSslErrorUi = ep.showSslErrorUi,
                ignoreSslErrors = ep.ignoreSslErrors,
                showRenderCrashErrorUi = ep.showRenderCrashErrorUi
            ),
            performanceOptimization = wv.performanceOptimization,
            pwaOfflineEnabled = wv.pwaOfflineEnabled,
            pwaOfflineStrategy = wv.pwaOfflineStrategy,
            staticAssetPackEnabled = wv.staticAssetPackEnabled,
            staticAssetPackMaxAgeDays = wv.staticAssetPackMaxAgeDays,
            showFloatingBackButton = wv.showFloatingBackButton,
            backButtonBehavior = wv.backButtonBehavior,
            forwardVolumeKeys = wv.forwardVolumeKeys,
            consumeVolumeKeys = wv.consumeVolumeKeys,
            mediaAlbumName = wv.mediaAlbumName,
            floatingWindowConfig = FloatingWindowConfig(
                enabled = fw.enabled,
                windowSizePercent = fw.windowSizePercent,
                widthPercent = fw.widthPercent,
                heightPercent = fw.heightPercent,
                lockAspectRatio = fw.lockAspectRatio,
                aspectRatioMode = enumOrDefault(fw.aspectRatioMode, FloatingWindowAspectRatioMode.FREE),
                customAspectRatioWidth = fw.customAspectRatioWidth,
                customAspectRatioHeight = fw.customAspectRatioHeight,
                opacity = fw.opacity,
                cornerRadius = fw.cornerRadius,
                borderStyle = enumOrDefault(fw.borderStyle, FloatingBorderStyle.SUBTLE),
                minimizedIconPath = null,
                minimizedIconSizePercent = fw.minimizedIconSizePercent,
                minimizedIconEdgeDocking = fw.minimizedIconEdgeDocking,
                showTitleBar = fw.showTitleBar,
                autoHideTitleBar = fw.autoHideTitleBar,
                startMinimized = fw.startMinimized,
                rememberPosition = fw.rememberPosition,
                edgeSnapping = fw.edgeSnapping,
                showResizeHandle = fw.showResizeHandle,
                lockPosition = fw.lockPosition
            ),
            proxyMode = wv.proxyMode,
            proxyHost = wv.proxyHost,
            proxyPort = wv.proxyPort,
            proxyType = wv.proxyType,
            pacUrl = wv.pacUrl,
            proxyBypassRules = wv.proxyBypassRules,
            proxyUsername = wv.proxyUsername,
            proxyPassword = wv.proxyPassword,
            hostsMappingEnabled = wv.hostsMappingEnabled,
            hostsMappings = wv.hostsMappings.map { HostMappingEntry(host = it.host, ip = it.ip) },
            tlsFingerprintEnabled = wv.tlsFingerprintEnabled,
            tlsFingerprintTemplate = wv.tlsFingerprintTemplate,
            tlsFingerprintCustomCiphers = wv.tlsFingerprintCustomCiphers,
            forceHttp3 = wv.forceHttp3,
            antiCapture = wv.antiCapture,
            dnsMode = wv.dnsMode,
            dnsConfig = DnsConfig(
                provider = wv.dnsConfig.provider,
                customDohUrl = wv.dnsConfig.customDohUrl,
                dohMode = wv.dnsConfig.dohMode,
                bypassSystemDns = wv.dnsConfig.bypassSystemDns,
                echEnabled = wv.dnsConfig.echEnabled
            )
        ),
        splashEnabled = splashEnabled,
        splashConfig = SplashConfig(
            type = enumOrDefault(splashType, SplashType.IMAGE),
            mediaPath = splashMediaPath,
            duration = splashDuration,
            clickToSkip = splashClickToSkip,
            orientation = if (splashLandscape) SplashOrientation.LANDSCAPE else SplashOrientation.PORTRAIT,
            fillScreen = splashFillScreen,
            enableAudio = splashEnableAudio,
            videoStartMs = splashVideoStartMs,
            videoEndMs = splashVideoEndMs,
            showCountdown = splashShowCountdown
        ),
        bgmEnabled = bgmEnabled,
        bgmConfig = BgmConfig(
            playlist = bgmPlaylist.map { item ->
                BgmItem(
                    id = item.id,
                    name = item.name,
                    path = "",
                    sortOrder = item.sortOrder
                )
            },
            playMode = enumOrDefault(bgmPlayMode, BgmPlayMode.LOOP),
            volume = bgmVolume,
            autoPlay = bgmAutoPlay,
            showLyrics = bgmShowLyrics,
            lrcTheme = bgmLrcTheme?.let {
                LrcTheme(
                    id = it.id,
                    name = it.name,
                    fontSize = it.fontSize,
                    textColor = it.textColor,
                    highlightColor = it.highlightColor,
                    backgroundColor = it.backgroundColor,
                    animationType = enumOrDefault(it.animationType, LrcAnimationType.FADE),
                    position = enumOrDefault(it.position, LrcPosition.BOTTOM)
                )
            },
            showFloatingPlayer = bgmShowFloatingPlayer,
            showNotificationPlayer = bgmShowNotificationPlayer
        ),
        apkExportConfig = ApkExportConfig(
            customPackageName = packageName,
            customVersionName = versionName,
            customVersionCode = versionCode,
            networkTrustConfig = networkTrustConfig.copy(
                customCaCertificates = networkTrustConfig.customCaCertificates.map {
                    it.copy(filePath = "")
                }
            ),
            encryptionConfig = ApkEncryptionConfig(
                enabled = hardeningEnabled,
                threatResponse = enumOrDefault(hardeningThreatResponse, ApkEncryptionConfig.ThreatResponse.LOG_ONLY)
            ),
            isolationConfig = isolationConfig?.toIsolationConfig() ?: com.webtoapp.core.privacy.IsolationConfig(),
            backgroundRunEnabled = backgroundRunEnabled,
            backgroundRunConfig = backgroundRunConfig?.let {
                BackgroundRunExportConfig(
                    notificationTitle = it.notificationTitle,
                    notificationContent = it.notificationContent,
                    keepCpuAwake = it.keepCpuAwake
                )
            } ?: BackgroundRunExportConfig(),
            engineType = engineType,
            deepLinkEnabled = deepLinkEnabled,
            customDeepLinkHosts = deepLinkHosts,
            performanceOptimization = wv.performanceOptimization,
            notificationEnabled = notificationEnabled,
            notificationConfig = notificationConfig?.let {
                NotificationExportConfig(
                    type = NotificationType.entries.firstOrNull { n -> n.key == it.type } ?: NotificationType.NONE,
                    pollUrl = it.pollUrl,
                    pollIntervalMinutes = it.pollIntervalMinutes,
                    pollMethod = it.pollMethod,
                    pollHeaders = it.pollHeaders,
                    clickUrl = it.clickUrl,
                    wsUrl = it.wsUrl,
                    wsHeaders = it.wsHeaders,
                    registerUrl = it.registerUrl,
                    registerHeaders = it.registerHeaders,
                    authToken = it.authToken,
                    fcmProjectId = it.fcmProjectId,
                    fcmApplicationId = it.fcmApplicationId,
                    fcmApiKey = it.fcmApiKey,
                    fcmSenderId = it.fcmSenderId,
                    fcmGoogleServicesJson = it.fcmGoogleServicesJson
                )
            } ?: NotificationExportConfig(),
            loggingEnabled = loggingEnabled
        ),
        themeType = themeType,
        translateEnabled = translateEnabled,
        translateConfig = TranslateConfig(
            targetLanguage = TranslateLanguage.entries.firstOrNull { it.code == translateTargetLanguage } ?: TranslateLanguage.CHINESE,
            showFloatingButton = translateShowButton
        ),
        pluginsEnabled = pluginsEnabled,
        pluginIds = pluginIds,
        autoStartConfig = autoStartConfig?.let {
            AutoStartConfig(
                bootStartEnabled = it.bootStartEnabled,
                scheduledStartEnabled = it.scheduledStartEnabled,
                scheduledTime = it.scheduledTime,
                scheduledDays = it.scheduledDays,
                scheduledRepeat = it.scheduledRepeat,
                bootDelay = it.bootDelay
            )
        },
        browserDisguiseConfig = browserDisguiseConfig,
        deviceDisguiseConfig = deviceDisguiseConfig,
        categoryId = null
    )
}