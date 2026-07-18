package com.webtoapp.ui.shell

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.webkit.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.shell.ShellPreviewSession
import com.webtoapp.core.webview.LongPressHandler
import com.webtoapp.data.model.Announcement
import com.webtoapp.core.forcedrun.ForcedRunConfig
import com.webtoapp.util.TvUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellScreen(
    config: ShellConfig,
    deepLinkUrl: String? = null,
    onWebViewCreated: (WebView) -> Unit,
    onBrowserSurfaceCreated: (com.webtoapp.core.engine.BrowserSurface) -> Unit = {},
    onStatusBarAutoColorChanged: (String?) -> Unit = {},
    onFileChooser: (ValueCallback<Array<Uri>>?, WebChromeClient.FileChooserParams?) -> Boolean,
    onShowCustomView: (View, WebChromeClient.CustomViewCallback?) -> Unit,
    onHideCustomView: () -> Unit,
    onFullscreenModeChanged: (Boolean) -> Unit,
    onForcedRunStateChanged: (Boolean, ForcedRunConfig?) -> Unit,

    statusBarBackgroundType: String = "COLOR",
    statusBarBackgroundColor: String? = null,
    statusBarBackgroundImage: String? = null,
    statusBarBackgroundAlpha: Float = 1.0f,
    statusBarHeightDp: Int = -1,

    statusBarBackgroundTypeDark: String = "COLOR",
    statusBarBackgroundColorDark: String? = null,
    statusBarBackgroundImageDark: String? = null,
    statusBarBackgroundAlphaDark: Float = 1.0f
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as android.app.Activity
    val activation = WebToAppApplication.activation
    val announcement = WebToAppApplication.announcement
    val adBlocker = WebToAppApplication.adBlock

    val forcedRunState = rememberForcedRunState(context)
    val forcedRunActive = forcedRunState.forcedRunActive
    val forcedRunRemainingMs = forcedRunState.forcedRunRemainingMs
    val forcedRunBlocked = forcedRunState.forcedRunBlocked
    val forcedRunBlockedMessage = forcedRunState.forcedRunBlockedMessage

    val appType = config.appType.trim().uppercase()

    AppLogger.d("ShellScreen", "appType='${config.appType}' (normalized='$appType'), targetUrl='${config.targetUrl}'")

    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var currentUrl by remember { mutableStateOf("") }
    var pageTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showActivationDialog by remember { mutableStateOf(false) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }

    var isActivated by remember { mutableStateOf(!config.activationEnabled) }

    var isActivationChecked by remember { mutableStateOf(!config.activationEnabled) }

    var webViewRecreationKey by remember { mutableIntStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val splashMediaExists = remember {
        if (config.splashEnabled) {
            val extension = if (config.splashType == "VIDEO") "mp4" else "png"
            val assetPath = "splash_media.$extension"
            val encryptedPath = "$assetPath.enc"

            val hasEncrypted = try {
                context.assets.open(encryptedPath).close()
                true
            } catch (e: Exception) { false }

            val hasNormal = try {
                context.assets.open(assetPath).close()
                true
            } catch (e: Exception) { false }

            val exists = hasEncrypted || hasNormal
            AppLogger.d("ShellActivity", "同步检查: 启动画面媒体 encrypted=$hasEncrypted, normal=$hasNormal, exists=$exists")
            exists
        } else false
    }

    var showSplash by remember { mutableStateOf(config.splashEnabled && splashMediaExists) }
    var splashCountdown by remember { mutableIntStateOf(if (config.splashEnabled && splashMediaExists) config.splashDuration else 0) }
    var originalOrientation by remember { mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }

    LaunchedEffect(showSplash) {
        if (showSplash && config.splashLandscape) {
            originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var statusBarAutoColor by remember { mutableStateOf<String?>(null) }
    var statusBarColorTracker by remember { mutableStateOf<com.webtoapp.core.webview.StatusBarPageColorTracker?>(null) }

    var showLongPressMenu by remember { mutableStateOf(false) }
    var longPressResult by remember { mutableStateOf<LongPressHandler.LongPressResult?>(null) }
    var longPressTouchX by remember { mutableFloatStateOf(0f) }
    var longPressTouchY by remember { mutableFloatStateOf(0f) }
    val longPressHandler = remember { LongPressHandler(context, scope) }

    LaunchedEffect(Unit) {

        if (config.adBlockEnabled) {
            val compiledRules = try {
                val rulesText = context.assets.open("wta_adblock_compiled.txt")
                    .bufferedReader().use { it.readText() }
                if (rulesText.isNotEmpty()) {
                    rulesText.split("\n").filter { it.isNotBlank() }
                } else emptyList()
            } catch (_: Exception) { emptyList<String>() }
            val allRules = config.adBlockRules + compiledRules
            adBlocker.initialize(allRules, useDefaultRules = false)
            adBlocker.setEnabled(true)
        }

        if (config.activationEnabled) {

            if (config.activationRequireEveryTime) {
                activation.resetActivation(ShellPreviewSession.activationAppId())
                isActivated = false
                isActivationChecked = true
                showActivationDialog = true
            } else {

                val activated = if (config.activationRemoteEnabled) {
                    activation.isActivated(ShellPreviewSession.activationAppId()).first() &&
                        activation.isRemoteStartupAllowed(
                            ShellPreviewSession.activationAppId(),
                            activation.buildRemoteRequest(
                                verifyUrl = config.activationRemoteVerifyUrl,
                                publicKeyBase64 = config.activationRemotePublicKey,
                                offlinePolicy = parseOfflinePolicy(config.activationRemoteOfflinePolicy)
                            )
                        )
                } else {
                    activation.resolveStartupActivation(ShellPreviewSession.activationAppId())
                }
                isActivated = activated
                isActivationChecked = true
                if (!activated) {
                    showActivationDialog = true
                }
            }
        }

        if (config.announcementEnabled && isActivated && config.announcementTitle.isNotEmpty()) {
            val ann = Announcement(
                title = config.announcementTitle,
                content = config.announcementContent,
                linkUrl = config.announcementLink.ifEmpty { null },
                showOnce = config.announcementShowOnce
            )
            showAnnouncementDialog = announcement.shouldShowAnnouncement(-1L, ann)
        }

        val validOrientationModes = setOf("PORTRAIT", "LANDSCAPE", "REVERSE_PORTRAIT", "REVERSE_LANDSCAPE", "SENSOR_PORTRAIT", "SENSOR_LANDSCAPE", "AUTO")
        val orientModeFromConfig = config.webViewConfig.orientationMode.uppercase()
        val resolvedOrientationMode = if (orientModeFromConfig in validOrientationModes && orientModeFromConfig != "PORTRAIT") {

            orientModeFromConfig
        } else {

            val typeSpecificLandscape = when (appType) {
                "IMAGE", "VIDEO" -> config.mediaConfig.landscape
                "GALLERY" -> config.galleryConfig.orientation.uppercase() == "LANDSCAPE"
                else -> config.webViewConfig.landscapeMode
            }
            if (typeSpecificLandscape) "LANDSCAPE" else "PORTRAIT"
        }

        when (resolvedOrientationMode) {
            "LANDSCAPE" -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            "REVERSE_PORTRAIT" -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            }
            "REVERSE_LANDSCAPE" -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
            "SENSOR_PORTRAIT" -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
            "SENSOR_LANDSCAPE" -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            "AUTO" -> {

                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            }
            else -> {
                if (TvUtils.isTv(context)) {

                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                } else {

                    @SuppressLint("SourceLockedOrientationActivity")
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }

        AppLogger.d("ShellActivity", "LaunchedEffect: showSplash=$showSplash, splashCountdown=$splashCountdown")

    }

    fun usesPageTopStatusBarColor(): Boolean {
        return (config.webViewConfig.statusBarBackgroundType == "COLOR" &&
            config.webViewConfig.statusBarColorMode == com.webtoapp.data.model.StatusBarColorMode.PAGE_TOP.name) ||
            (config.webViewConfig.statusBarBackgroundTypeDark == "COLOR" &&
                config.webViewConfig.statusBarColorModeDark == com.webtoapp.data.model.StatusBarColorMode.PAGE_TOP.name)
    }

    fun resolveStatusBarOverlayColor(isDark: Boolean): String? {
        val mode = if (isDark) config.webViewConfig.statusBarColorModeDark else config.webViewConfig.statusBarColorMode
        val configuredColor = if (isDark) statusBarBackgroundColorDark else statusBarBackgroundColor
        return when (mode) {
            com.webtoapp.data.model.StatusBarColorMode.PAGE_TOP.name -> statusBarAutoColor ?: configuredColor ?: if (isDark) "#1C1B1F" else "#FFFBFE"
            com.webtoapp.data.model.StatusBarColorMode.CUSTOM.name -> configuredColor ?: if (isDark) "#1C1B1F" else "#FFFBFE"
            com.webtoapp.data.model.StatusBarColorMode.THEME.name -> if (isDark) "#1C1B1F" else "#FFFBFE"
            com.webtoapp.data.model.StatusBarColorMode.TRANSPARENT.name -> null
            else -> configuredColor
        }
    }

    ForcedRunEffects(
        state = forcedRunState,
        config = config.forcedRunConfig,
        isActivated = isActivated,
        context = context,
        onForcedRunStateChanged = onForcedRunStateChanged
    )

    LaunchedEffect(showSplash, splashCountdown) {

        if (config.splashType == "VIDEO") return@LaunchedEffect

        if (showSplash && splashCountdown > 0) {
            delay(1000L)
            splashCountdown--
        } else if (showSplash && splashCountdown <= 0) {
            showSplash = false

            if (originalOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                activity.requestedOrientation = originalOrientation
                originalOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    val bgmState = rememberBgmPlayerState(context, config)

    val webViewCallbacks = remember {
        createShellWebViewCallbacks(
            context = context,
            config = config,
            webViewRefProvider = { webViewRef },
            currentUrlProvider = { currentUrl },
            longPressHandler = longPressHandler,
            handleShowCustomView = onShowCustomView,
            handleHideCustomView = onHideCustomView,
            handleFileChooser = onFileChooser,
            updateLoading = { isLoading = it },
            updateUrl = { currentUrl = it },
            updateTitle = { pageTitle = it },
            updateProgress = { loadProgress = it },
            updateError = { errorMessage = it },
            updateNavigation = { back, forward -> canGoBack = back; canGoForward = forward },
            updateWebViewRef = { webViewRef = it },
            notifyRecreationKeyIncrement = { webViewRecreationKey++ },
            notifyLongPressMenu = { result, x, y ->
                longPressResult = result
                longPressTouchX = x
                longPressTouchY = y
                showLongPressMenu = true
            },
            resetStatusBarAutoColor = {
                if (!usesPageTopStatusBarColor()) return@createShellWebViewCallbacks
                statusBarColorTracker?.reset()
                if (statusBarAutoColor != null) {
                    statusBarAutoColor = null
                    onStatusBarAutoColorChanged(null)
                }
            },
            scheduleStatusBarAutoColorSample = {
                statusBarColorTracker?.scheduleSample(56L)
            },
            onRefreshFinished = { isRefreshing = false }
        )
    }

    val webViewConfig = buildWebViewConfig(config)

    val webViewManager = remember {
        com.webtoapp.core.webview.WebViewManager(context, adBlocker)
    }

    val hideToolbar = config.webViewConfig.hideToolbar

    val hideBrowserToolbar = config.webViewConfig.hideBrowserToolbar

    val swipeRefreshEnabled = config.webViewConfig.swipeRefreshEnabled

    LaunchedEffect(hideToolbar) {
        onFullscreenModeChanged(hideToolbar)
    }

    val closeSplash = {
        showSplash = false

        if (originalOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activity.requestedOrientation = originalOrientation
            originalOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            statusBarColorTracker?.detach()
            statusBarColorTracker = null
            onStatusBarAutoColorChanged(null)
        }
    }

    val handleWebViewCreated: (WebView) -> Unit = remember(onWebViewCreated, config) {
        { webView ->
            statusBarColorTracker?.detach()
            val tracker = com.webtoapp.core.webview.StatusBarPageColorTracker(
                webView = webView,
                shouldSample = ::usesPageTopStatusBarColor,
                onColorChanged = { color ->
                    if (statusBarAutoColor != color) {
                        statusBarAutoColor = color
                        onStatusBarAutoColorChanged(color)
                    }
                }
            )
            tracker.attach()
            statusBarColorTracker = tracker
            onWebViewCreated(webView)
            tracker.scheduleSample(80L)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

    ShellScaffoldLayout(
        config = config,
        appType = appType,
        hideToolbar = hideToolbar,
        hideBrowserToolbar = hideBrowserToolbar,
        isLoading = isLoading,
        loadProgress = loadProgress,
        pageTitle = pageTitle,
        currentUrl = currentUrl,
        errorMessage = errorMessage,
        isActivationChecked = isActivationChecked,
        isActivated = isActivated,
        forcedRunActive = forcedRunActive,
        forcedRunBlocked = forcedRunBlocked,
        forcedRunBlockedMessage = forcedRunBlockedMessage,
        forcedRunRemainingMs = forcedRunRemainingMs,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        webViewRecreationKey = webViewRecreationKey,
        webViewRef = webViewRef,
        webViewConfig = webViewConfig,
        webViewCallbacks = webViewCallbacks,
        webViewManager = webViewManager,
        deepLinkUrl = deepLinkUrl,
        bgmState = bgmState,
        swipeRefreshEnabled = swipeRefreshEnabled,
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true },
        onWebViewCreated = handleWebViewCreated,
        onBrowserSurfaceCreated = onBrowserSurfaceCreated,
        onWebViewRefUpdated = { webViewRef = it },
        onShowActivationDialog = { showActivationDialog = true },
        onErrorDismiss = { errorMessage = null },
        onActivityFinish = { activity.finish() },
        statusBarHeightDp = statusBarHeightDp
    )

    if (showActivationDialog) {
        ShellActivationDialog(
            config = config,
            onDismiss = { showActivationDialog = false },
            onActivated = {
                isActivated = true
                showActivationDialog = false

                if (config.announcementEnabled && config.announcementTitle.isNotEmpty()) {
                    val ann = Announcement(
                        title = config.announcementTitle,
                        content = config.announcementContent,
                        linkUrl = config.announcementLink.ifEmpty { null },
                        showOnce = config.announcementShowOnce
                    )
                    showAnnouncementDialog = kotlinx.coroutines.runBlocking { announcement.shouldShowAnnouncement(-1L, ann) }
                }
            }
        )
    }

    if (showAnnouncementDialog && config.announcementTitle.isNotEmpty()) {
        ShellAnnouncementDialog(
            config = config,
            onDismiss = { showAnnouncementDialog = false }
        )
    }

    if (forcedRunState.showForcedRunPermissionDialog && config.forcedRunConfig != null) {
        ShellForcedRunPermissionDialog(
            config = config,
            forcedRunActive = forcedRunActive,
            onDismiss = { forcedRunState.showForcedRunPermissionDialog = false }
        )
    }

    AnimatedVisibility(
        visible = showSplash,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        ShellSplashOverlay(
            splashType = config.splashType,
            countdown = splashCountdown,
            videoStartMs = config.splashVideoStartMs,
            videoEndMs = config.splashVideoEndMs,
            fillScreen = config.splashFillScreen,
            enableAudio = config.splashEnableAudio,

            onSkip = if (config.splashClickToSkip) { closeSplash } else null,

            onComplete = closeSplash
        )
    }

    if (showLongPressMenu && longPressResult != null) {
        ShellLongPressMenu(
            menuStyle = config.webViewConfig.longPressMenuStyle,
            result = longPressResult!!,
            touchX = longPressTouchX,
            touchY = longPressTouchY,
            longPressHandler = longPressHandler,
            onDismiss = {
                showLongPressMenu = false
                longPressResult = null
            }
        )
    }

    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val effectiveColorMode = if (isDarkTheme) config.webViewConfig.statusBarColorModeDark else config.webViewConfig.statusBarColorMode
    val effectiveBgType = if (isDarkTheme) statusBarBackgroundTypeDark else statusBarBackgroundType
    val effectiveBgColor = resolveStatusBarOverlayColor(isDarkTheme)
    val effectiveBgImage = if (isDarkTheme) statusBarBackgroundImageDark else statusBarBackgroundImage
    val effectiveBgAlpha = if (isDarkTheme) statusBarBackgroundAlphaDark else statusBarBackgroundAlpha
    val showOverlay = (hideToolbar && config.webViewConfig.showStatusBarInFullscreen) ||
            (!hideToolbar && (effectiveBgType != "COLOR" ||
                effectiveColorMode == com.webtoapp.data.model.StatusBarColorMode.CUSTOM.name ||
                effectiveColorMode == com.webtoapp.data.model.StatusBarColorMode.PAGE_TOP.name))
    if (showOverlay) {
        com.webtoapp.ui.components.StatusBarOverlay(
            show = true,
            backgroundType = effectiveBgType,
            backgroundColor = effectiveBgColor,
            backgroundImagePath = effectiveBgImage,
            alpha = effectiveBgAlpha,
            heightDp = statusBarHeightDp,
            modifier = Modifier.align(Alignment.TopStart)
        )

        val view = activity.window.decorView
        val insetsController = androidx.core.view.WindowInsetsControllerCompat(activity.window, view)

        val explicitDarkIcons = if (isDarkTheme) {
            config.webViewConfig.statusBarDarkIconsDark
        } else {
            config.webViewConfig.statusBarDarkIcons
        }
        val isLightOverlay = explicitDarkIcons ?: when (effectiveBgType) {
            "COLOR" -> {
                val colorHex = effectiveBgColor
                if (colorHex.isNullOrBlank()) {
                    !isDarkTheme
                } else try {
                    val color = android.graphics.Color.parseColor(colorHex)
                    val luminance = (0.299 * android.graphics.Color.red(color) +
                            0.587 * android.graphics.Color.green(color) +
                            0.114 * android.graphics.Color.blue(color)) / 255.0
                    luminance > 0.5
                } catch (_: Exception) { !isDarkTheme }
            }

            else -> !isDarkTheme
        }
        insetsController.isAppearanceLightStatusBars = isLightOverlay
        insetsController.isAppearanceLightNavigationBars = isLightOverlay
    }

    }
}
