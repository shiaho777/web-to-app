package com.webtoapp.ui.shell

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import java.io.File
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.webview.LongPressHandler
import com.webtoapp.data.model.Announcement
import com.webtoapp.util.TvUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Model-level Announcement built from the shell payload for gating decisions. Rendering uses
 * ShellAnnouncementDialog's own construction (template/custom-icon mapping); this one carries
 * the trigger and version fields the show/hide gate must honor.
 */
internal fun buildShellAnnouncement(config: ShellConfig): Announcement = Announcement(
    title = config.announcementTitle,
    content = config.announcementContent,
    linkUrl = config.announcementLink.ifEmpty { null },
    showOnce = config.announcementShowOnce,
    version = config.announcementVersion,
    triggerOnLaunch = config.announcementTriggerOnLaunch,
    triggerOnNoNetwork = config.announcementTriggerOnNoNetwork,
    triggerIntervalMinutes = config.announcementTriggerIntervalMinutes
)

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

    val appType = config.appType.trim().uppercase()

    AppLogger.d("ShellScreen", "appType='${config.appType}' (normalized='$appType'), targetUrl='${config.targetUrl}'")

    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var currentUrl by remember { mutableStateOf("") }
    var pageTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showActivationDialog by remember { mutableStateOf(false) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }

    // Console / terminal panel state
    var showConsole by remember { mutableStateOf(false) }
    var consoleMessages by remember { mutableStateOf<List<ConsoleLogEntry>>(emptyList()) }

    var showFindBar by remember { mutableStateOf(false) }

    var isActivated by remember { mutableStateOf(!config.activationEnabled) }

    var isActivationChecked by remember { mutableStateOf(!config.activationEnabled) }

    // Target URL delivered by the remote activation server (dynamic URL mode). Takes precedence
    // over the packaged targetUrl so the URL need not be hardcoded in the APK.
    var dynamicUrl by remember { mutableStateOf<String?>(null) }

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

            val hasPreviewPath = config.splashMediaPath?.let { File(it).exists() } ?: false

            val exists = hasEncrypted || hasNormal || hasPreviewPath
            AppLogger.d("ShellActivity", "同步检查: 启动画面媒体 encrypted=$hasEncrypted, normal=$hasNormal, previewPath=${config.splashMediaPath}, exists=$exists")
            exists
        } else false
    }

    // Splash starts only once activation has resolved (preview parity): an activation-gated
    // app must not play its splash behind the activation gate. Apps without an activation
    // requirement keep the old immediate start.
    var showSplash by remember { mutableStateOf(config.splashEnabled && splashMediaExists && !config.activationEnabled) }
    var splashCountdown by remember { mutableIntStateOf(if (showSplash) config.splashDuration else 0) }
    var originalOrientation by remember { mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }

    LaunchedEffect(showSplash) {
        if (showSplash && config.splashLandscape) {
            originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var browserSurfaceRef by remember {
        mutableStateOf<com.webtoapp.core.engine.BrowserSurface?>(null)
    }
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

            // One gate for both modes: remote re-verifies the remembered code
            // (always when "every launch" is on, otherwise only when the cached
            // result can't carry this launch); local codes re-check the remembered
            // card against the configured list under "every launch", or just the
            // persisted grant otherwise. The dialog only shows when this fails.
            val activated = if (config.activationRemoteEnabled) {
                activation.resolveRemoteStartup(
                    -1L,
                    activation.buildRemoteRequest(
                        verifyUrl = config.activationRemoteVerifyUrl,
                        publicKeyBase64 = config.activationRemotePublicKey,
                        offlinePolicy = parseOfflinePolicy(config.activationRemoteOfflinePolicy),
                        deliverUrl = config.activationRemoteDeliverUrl,
                        encryptUrl = config.activationRemoteEncryptUrl,
                        aesKeyBase64 = config.activationRemoteAesKey,
                        deviceBound = config.activationRemoteDeviceBound
                    ),
                    reverifyEveryLaunch = config.activationRequireEveryTime
                )
            } else if (config.activationRequireEveryTime) {
                activation.resolveRelaunchActivation(
                    -1L,
                    config.activationCodes.map { raw ->
                        com.webtoapp.core.activation.ActivationCode.fromJson(raw)
                            ?: com.webtoapp.core.activation.ActivationCode.fromLegacyString(raw)
                    }
                )
            } else {
                activation.resolveStartupActivation(-1L)
            }
            isActivated = activated
            isActivationChecked = true
            if (activated && config.activationRemoteEnabled && config.activationRemoteDeliverUrl) {
                dynamicUrl = activation.getCachedRemoteUrl(-1L)
            }
            if (!activated) {
                showActivationDialog = true
            }
        }

        if (config.announcementEnabled && isActivated &&
            (config.announcementTitle.isNotEmpty() || config.announcementContent.isNotEmpty())
        ) {
            // Trigger-aware gate: honors triggerOnLaunch=false, version-pinned showOnce, and the
            // no-network / interval modes driven by the effects below.
            showAnnouncementDialog = announcement.shouldShowAnnouncementForTrigger(
                -1L,
                buildShellAnnouncement(config),
                isLaunch = true
            )
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

        if (isActivated) {
            showSplash = config.splashEnabled && splashMediaExists
            if (showSplash) {
                splashCountdown = config.splashDuration
            }
        }

        AppLogger.d("ShellActivity", "LaunchedEffect: showSplash=$showSplash, splashCountdown=$splashCountdown")

    }

    // Announcement no-network trigger: monitor connectivity while the shell is up (preview parity
    // with WebViewActivity's monitoring for triggerOnNoNetwork announcements).
    DisposableEffect(Unit) {
        if (config.announcementEnabled && config.announcementTriggerOnNoNetwork) {
            announcement.startNetworkMonitoring()
        }
        onDispose { announcement.stopNetworkMonitoring() }
    }

    val networkAvailable by announcement.isNetworkAvailable.collectAsStateWithLifecycle()
    var lastNetworkState by remember { mutableStateOf(true) }

    LaunchedEffect(networkAvailable, isActivated) {
        if (lastNetworkState && !networkAvailable && isActivated &&
            config.announcementEnabled && config.announcementTriggerOnNoNetwork
        ) {
            val shouldShow = announcement.shouldShowAnnouncementForTrigger(
                -1L,
                buildShellAnnouncement(config),
                isNoNetwork = true
            )
            if (shouldShow && !showAnnouncementDialog) {
                showAnnouncementDialog = true
            }
        }
        lastNetworkState = networkAvailable
    }

    // Announcement interval trigger: re-show periodically while the app stays open.
    LaunchedEffect(isActivated) {
        val intervalMinutes = config.announcementTriggerIntervalMinutes
        if (!isActivated || !config.announcementEnabled || intervalMinutes <= 0) return@LaunchedEffect

        if (config.announcementTriggerIntervalIncludeLaunch) {
            announcement.resetIntervalTrigger(-1L)
        }

        val ann = buildShellAnnouncement(config)
        while (true) {
            val nextDelay = announcement.getMillisUntilNextIntervalAnnouncement(-1L, ann)
            delay(nextDelay.coerceIn(1_000L, intervalMinutes * 60_000L))

            if (announcement.shouldTriggerIntervalAnnouncement(-1L, ann)) {
                val shouldShow = announcement.shouldShowAnnouncementForTrigger(-1L, ann, isInterval = true)
                if (shouldShow && !showAnnouncementDialog) {
                    showAnnouncementDialog = true
                    announcement.markIntervalTrigger(-1L)
                }
            }
        }
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

    val bgmState = rememberBgmPlayerState(context, config, enabled = isActivated)

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
            onRefreshFinished = { isRefreshing = false },
            onConsoleLog = { entry -> consoleMessages = consoleMessages + entry }
        )
    }

    val webViewConfig = buildWebViewConfig(config)

    val webViewManager = remember {
        com.webtoapp.core.webview.WebViewManager(context, adBlocker)
    }

    val hideToolbar = config.webViewConfig.hideToolbar

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
        isLoading = isLoading,
        loadProgress = loadProgress,
        pageTitle = pageTitle,
        currentUrl = currentUrl,
        errorMessage = errorMessage,
        isActivationChecked = isActivationChecked,
        isActivated = isActivated,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        webViewRecreationKey = webViewRecreationKey,
        webViewRef = webViewRef,
        browserSurface = browserSurfaceRef,
        webViewConfig = webViewConfig,
        webViewCallbacks = webViewCallbacks,
        webViewManager = webViewManager,
        deepLinkUrl = deepLinkUrl ?: dynamicUrl,
        bgmState = bgmState,
        swipeRefreshEnabled = swipeRefreshEnabled,
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true },
        onWebViewCreated = handleWebViewCreated,
        onBrowserSurfaceCreated = { surface ->
            browserSurfaceRef = surface
            onBrowserSurfaceCreated(surface)
        },
        onWebViewRefUpdated = { webViewRef = it },
        onShowActivationDialog = { showActivationDialog = true },
        onErrorDismiss = { errorMessage = null },
        onActivityFinish = { activity.finish() },
        showConsole = showConsole,
        onToggleConsole = { showConsole = !showConsole },
        consoleMessages = consoleMessages,
        onClearConsole = { consoleMessages = emptyList() },
        showFindBar = showFindBar,
        onToggleFindBar = { showFindBar = !showFindBar },
        onRunScript = { script ->
            // Surface-first so the console also evaluates on the GeckoView kernel
            // (webViewRef stays null there; Gecko cannot return the eval result, so
            // the entry shows "=> null" but the script does run in the page).
            val surface = browserSurfaceRef
            val appendResult: (String?) -> Unit = { result ->
                consoleMessages = consoleMessages + ConsoleLogEntry(
                    level = ConsoleLevel.LOG,
                    message = "=> $result",
                    source = "eval",
                    lineNumber = 0,
                    timestamp = System.currentTimeMillis()
                )
            }
            if (surface != null) {
                surface.evaluateJavascript(script, appendResult)
            } else {
                webViewRef?.evaluateJavascript(script, appendResult)
            }
        },
        statusBarHeightDp = statusBarHeightDp
    )

    if (showActivationDialog) {
        ShellActivationDialog(
            config = config,
            onDismiss = { showActivationDialog = false },
            onActivated = { url ->
                isActivated = true
                showActivationDialog = false
                if (config.activationRemoteEnabled && config.activationRemoteDeliverUrl) {
                    dynamicUrl = url
                }

                if (config.announcementEnabled &&
                    (config.announcementTitle.isNotEmpty() || config.announcementContent.isNotEmpty())
                ) {
                    scope.launch {
                        showAnnouncementDialog = announcement.shouldShowAnnouncement(-1L, buildShellAnnouncement(config))
                    }
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
            mediaPath = config.splashMediaPath,
            showCountdown = config.splashShowCountdown,

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
    // On the classic pre-API-30 resize path nothing draws behind the status bar and the bar
    // itself is chrome-owned, so a Compose overlay would only paint a floating band over the
    // web content (issue #683). Reserve space is already handled by the content padding; the
    // window-level bar color comes from WindowHelper.
    val classicSystemBars = com.webtoapp.ui.shared.WindowHelper.isClassicSystemBarsWindow(activity)
    val showOverlay = !classicSystemBars && ((hideToolbar && config.webViewConfig.showStatusBarInFullscreen) ||
            (!hideToolbar && (effectiveBgType != "COLOR" ||
                effectiveColorMode == com.webtoapp.data.model.StatusBarColorMode.CUSTOM.name ||
                effectiveColorMode == com.webtoapp.data.model.StatusBarColorMode.PAGE_TOP.name)))
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
