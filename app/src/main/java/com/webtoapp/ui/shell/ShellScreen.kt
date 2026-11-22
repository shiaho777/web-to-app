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
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.webview.LongPressHandler
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.hasAnyToolbarItem
import com.webtoapp.data.model.isBrowserToolbarVisible
import com.webtoapp.ui.components.RemainingDaysNoticeDialog
import com.webtoapp.ui.components.StatusBarOverlay
import com.webtoapp.ui.shared.StatusBarAppearance
import com.webtoapp.ui.shared.WindowHelper
import com.webtoapp.ui.shared.resolveStatusBarAppearance
import com.webtoapp.util.TvUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Model-level Announcement built from the shell payload for gating decisions. Rendering uses
 * ShellAnnouncementDialog's own construction (template/custom-icon mapping); this one carries
 * the trigger and version fields the show/hide gate must honor.
 */
private const val CONSOLE_LOG_CAP = 500

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
    onPageBottomColorChanged: (String?) -> Unit = {},
    onPageScroll: ((Int) -> Unit)? = null,
    onStatusBarAppearanceChanged: (StatusBarAppearance) -> Unit,
    onFileChooser: (ValueCallback<Array<Uri>>?, WebChromeClient.FileChooserParams?) -> Boolean,
    onShowCustomView: (View, WebChromeClient.CustomViewCallback?) -> Unit,
    onHideCustomView: () -> Unit,
    onFullscreenModeChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as android.app.Activity
    val activation = WebToAppApplication.activation
    val announcement = WebToAppApplication.announcement
    val adBlocker = WebToAppApplication.adBlock
    val siteAdBlockActive = config.multiWebConfig?.sitesUseOwnConfig == true &&
        config.multiWebConfig?.sites?.any { it.enabled && it.siteShellConfig?.adBlockEnabled == true } == true
    val effectiveAdBlockEnabled = config.adBlockEnabled || siteAdBlockActive
    // Arm before the browser view is composed. LaunchedEffect runs after that
    // view, so a hold started inside the effect would miss the first load.
    remember {
        if (effectiveAdBlockEnabled) adBlocker.holdRuleInstall()
    }

    val appType = config.appType.trim().uppercase()

    AppLogger.d("ShellScreen", "appType='${config.appType}' (normalized='$appType'), targetUrl='${config.targetUrl}'")

    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var currentUrl by remember { mutableStateOf("") }
    var pageTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showActivationDialog by remember { mutableStateOf(false) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var remainingDaysNotice by remember {
        mutableStateOf<com.webtoapp.core.activation.RemainingDaysNotice?>(null)
    }
    var remainingDaysIncludeSplash by remember { mutableStateOf(false) }
    var remainingDaysFromLaunch by remember { mutableStateOf(true) }

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
    // #1033: set when memory pressure tore the WebView down while backgrounded;
    // the next ON_RESUME recreates it instead of probing a dead view.
    var memoryTeardownPending by remember { mutableStateOf(false) }

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
    var topTabColor by remember { mutableStateOf<String?>(null) }
    var pageBottomAutoColor by remember { mutableStateOf<String?>(null) }
    var statusBarColorTracker by remember { mutableStateOf<com.webtoapp.core.webview.StatusBarPageColorTracker?>(null) }

    var showLongPressMenu by remember { mutableStateOf(false) }
    var longPressResult by remember { mutableStateOf<LongPressHandler.LongPressResult?>(null) }
    var longPressTouchX by remember { mutableFloatStateOf(0f) }
    var longPressTouchY by remember { mutableFloatStateOf(0f) }
    val longPressHandler = remember { LongPressHandler(context, scope) }

    fun shellActivationCodes(): List<com.webtoapp.core.activation.ActivationCode> =
        config.activationCodes.map { raw ->
            com.webtoapp.core.activation.ActivationCode.fromJson(raw)
                ?: com.webtoapp.core.activation.ActivationCode.fromLegacyString(raw)
        }

    suspend fun openShellAnnouncement(fromLaunch: Boolean) {
        if (!isActivated || !config.announcementEnabled) return
        if (config.announcementTitle.isEmpty() && config.announcementContent.isEmpty()) return
        val ann = buildShellAnnouncement(config)
        showAnnouncementDialog = if (fromLaunch) {
            announcement.shouldShowAnnouncementForTrigger(-1L, ann, isLaunch = true)
        } else {
            announcement.shouldShowAnnouncement(-1L, ann)
        }
    }

    fun openShellSplash() {
        if (!isActivated) return
        showSplash = config.splashEnabled && splashMediaExists
        if (showSplash) {
            splashCountdown = config.splashDuration
        }
    }

    LaunchedEffect(Unit) {

        if (effectiveAdBlockEnabled) {
            try {
                adBlocker.installCompiledRules {
                    val bundled = try {
                        context.assets.open("wta_adblock_compiled.txt").bufferedReader().use { reader ->
                            reader.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
                        }
                    } catch (_: Exception) {
                        emptyList()
                    }
                    val siteRules = if (config.multiWebConfig?.sitesUseOwnConfig == true) {
                        config.multiWebConfig.sites
                            .filter { it.enabled && it.siteShellConfig?.adBlockEnabled == true }
                            .flatMap { it.siteShellConfig?.adBlockRules.orEmpty() }
                    } else emptyList()
                    (config.adBlockRules + siteRules).distinct() + bundled
                }
            } catch (e: Exception) {
                AppLogger.e("ShellScreen", "Failed to install ad-block rules", e)
            } finally {
                adBlocker.releaseRuleInstall()
            }
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
                        deviceBound = config.activationRemoteDeviceBound,
                        trustMode = com.webtoapp.data.model.parseRemoteActivationTrustMode(
                            config.activationRemoteTrustMode
                        ),
                        pinnedCertificate = config.activationRemotePinnedCertificate,
                        pinnedCertHost = config.activationRemotePinnedCertHost
                    ),
                    reverifyEveryLaunch = config.activationRequireEveryTime
                )
            } else if (config.activationRequireEveryTime) {
                activation.resolveRelaunchActivation(-1L, shellActivationCodes())
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
            } else if (!config.activationRemoteEnabled) {
                val notice = activation.remainingDaysNoticeForLaunch(-1L, shellActivationCodes())
                if (notice != null) {
                    remainingDaysNotice = notice
                    remainingDaysIncludeSplash = true
                    remainingDaysFromLaunch = true
                }
            }
        }

        if (remainingDaysNotice == null) {
            // Trigger-aware gate: honors triggerOnLaunch=false, version-pinned showOnce, and the
            // no-network / interval modes driven by the effects below.
            openShellAnnouncement(fromLaunch = true)
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

        if (remainingDaysNotice == null) {
            openShellSplash()
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

    fun usesPageEdgeColor(): Boolean {
        val pageModes = setOf(
            com.webtoapp.data.model.StatusBarColorMode.PAGE_TOP.name,
            com.webtoapp.data.model.StatusBarColorMode.TRANSPARENT.name
        )
        return (config.webViewConfig.statusBarBackgroundType == "COLOR" &&
            config.webViewConfig.statusBarColorMode in pageModes) ||
            (config.webViewConfig.statusBarBackgroundTypeDark == "COLOR" &&
                config.webViewConfig.statusBarColorModeDark in pageModes)
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
                if (!usesPageEdgeColor()) return@createShellWebViewCallbacks
                statusBarColorTracker?.reset()
                if (statusBarAutoColor != null) {
                    statusBarAutoColor = null
                    onStatusBarAutoColorChanged(null)
                }
                if (pageBottomAutoColor != null) {
                    pageBottomAutoColor = null
                    onPageBottomColorChanged(null)
                }
            },
            scheduleStatusBarAutoColorSample = {
                statusBarColorTracker?.scheduleSample(56L, settle = true)
            },
            onRefreshFinished = { isRefreshing = false },
            onConsoleLog = { entry -> consoleMessages = (consoleMessages + entry).takeLast(CONSOLE_LOG_CAP) }
        )
    }

    val webViewConfig = buildWebViewConfig(config)

    val webViewManager = remember {
        com.webtoapp.core.webview.WebViewManager(context, adBlocker)
    }

    // Issue #1030 backstop: onRenderProcessGone is the primary renderer-death
    // signal, but some OEM WebView builds never deliver it for a background kill.
    // Probe on every resume — a dead renderer never answers evaluateJavascript —
    // and route the discovery through the same recreation path.
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // A file picker is still out. Recreating the WebView now would
                // drop the ValueCallback and reload the start URL (#1319).
                val skipProbe = com.webtoapp.core.webview.FileChooserLifetime.consumeResumeProbeSkip()
                if (com.webtoapp.core.webview.FileChooserLifetime.isOpen() || skipProbe) {
                    return@LifecycleEventObserver
                }
                if (memoryTeardownPending) {
                    memoryTeardownPending = false
                    webViewRecreationKey++
                    return@LifecycleEventObserver
                }
                val wv = webViewRef
                if (wv != null) {
                    com.webtoapp.core.webview.RendererLivenessProbe.probe(
                        wv,
                        stillCurrent = { webViewRef === wv }
                    ) {
                        AppLogger.w("ShellScreen", "Renderer unresponsive after resume — recreating WebView")
                        runCatching {
                            wv.stopLoading()
                            (wv.parent as? android.view.ViewGroup)?.removeView(wv)
                            wv.destroy()
                        }
                        webViewManager.discardWebView(wv)
                        webViewCallbacks.onRenderProcessGone(false)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Issue #1033: TRIM_MEMORY_COMPLETE means the process is next in line for
    // LMK — and on a WebView app the renderer IS the memory. Shed it ourselves
    // (navigation state stashed, recreated on resume) instead of letting the
    // system starve lower-priority processes like the Launcher.
    DisposableEffect(lifecycleOwner) {
        val componentCallbacks = object : android.content.ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {}
            override fun onLowMemory() {}
            override fun onTrimMemory(level: Int) {
                if (!com.webtoapp.core.webview.WebViewMemoryTrimmer.onTrimMemory(level, context)) return
                if (com.webtoapp.core.webview.FileChooserLifetime.isOpen()) {
                    AppLogger.w("ShellScreen", "WebView callback in flight — keeping the WebView through trim")
                    return
                }
                val wv = webViewRef ?: return
                if (memoryTeardownPending) return
                val bundle = android.os.Bundle()
                runCatching { wv.saveState(bundle) }
                if (!bundle.isEmpty) {
                    // Tag the stashed surface like onSaveInstanceState does —
                    // otherwise the restore site check (#1036) is bypassed and
                    // a multi-web bundle can graft onto the wrong site's view.
                    (activity as? ShellActivity)?.stashWebViewState(
                        bundle,
                        (wv as? com.webtoapp.core.webview.WtaWebView)?.siteId
                    )
                }
                AppLogger.w("ShellScreen", "TRIM_MEMORY_COMPLETE — tearing down WebView, will rebuild on resume")
                runCatching {
                    wv.stopLoading()
                    wv.onPause()
                    (wv.parent as? android.view.ViewGroup)?.removeView(wv)
                    wv.destroy()
                }
                webViewManager.discardWebView(wv)
                webViewRef = null
                browserSurfaceRef = null
                (activity as? ShellActivity)?.clearWebViewRefs()
                memoryTeardownPending = true
            }
        }
        context.registerComponentCallbacks(componentCallbacks)
        onDispose { context.unregisterComponentCallbacks(componentCallbacks) }
    }

    val statusConfig = config.webViewConfig
    val hideToolbar = statusConfig.hideToolbar
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val showToolbar = isBrowserToolbarVisible(
        statusConfig.browserToolbarEnabled,
        hasAnyToolbarItem(
            toolbarShowTitle = statusConfig.toolbarShowTitle,
            toolbarShowUrl = statusConfig.toolbarShowUrl,
            toolbarShowBack = statusConfig.toolbarShowBack,
            toolbarShowForward = statusConfig.toolbarShowForward,
            toolbarShowRefresh = statusConfig.toolbarShowRefresh,
            toolbarShowConsole = statusConfig.toolbarShowConsole,
            toolbarShowFind = statusConfig.toolbarShowFind
        )
    )

    LaunchedEffect(isDarkTheme) {
        statusBarColorTracker?.reset()
        statusBarColorTracker?.scheduleSample(56L, settle = true)
    }

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
            onPageBottomColorChanged(null)
        }
    }

    val pageScrollCallback = androidx.compose.runtime.rememberUpdatedState(onPageScroll)
    val handleWebViewCreated: (WebView) -> Unit = remember(onWebViewCreated, config) {
        { webView ->
            statusBarColorTracker?.detach()
            val tracker = com.webtoapp.core.webview.StatusBarPageColorTracker(
                webView = webView,
                shouldSample = { true },
                onScroll = { y -> pageScrollCallback.value?.invoke(y) },
                onColors = { colors ->
                    com.webtoapp.ui.shared.WindowHelper.setImeGapColor(colors.page)
                    if (usesPageEdgeColor()) {
                        if (statusBarAutoColor != colors.top) {
                            statusBarAutoColor = colors.top
                            onStatusBarAutoColorChanged(colors.top)
                        }
                        if (pageBottomAutoColor != colors.bottom) {
                            pageBottomAutoColor = colors.bottom
                            onPageBottomColorChanged(colors.bottom)
                        }
                    }
                }
            )
            tracker.attach()
            statusBarColorTracker = tracker
            onWebViewCreated(webView)
            tracker.scheduleSample(80L, settle = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalPageScrollTracker provides statusBarColorTracker
    ) {

    ShellScaffoldLayout(
        config = config,
        appType = appType,
        hideToolbar = hideToolbar,
        showToolbar = showToolbar,
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
                consoleMessages = (consoleMessages + ConsoleLogEntry(
                    level = ConsoleLevel.LOG,
                    message = "=> $result",
                    source = "eval",
                    lineNumber = 0,
                    timestamp = System.currentTimeMillis()
                )).takeLast(CONSOLE_LOG_CAP)
            }
            if (surface != null) {
                surface.evaluateJavascript(script, appendResult)
            } else {
                webViewRef?.evaluateJavascript(script, appendResult)
            }
        },
        statusBarHeightDp = statusConfig.statusBarHeightDp,
        pageTopColor = statusBarAutoColor,
        pageBottomColor = pageBottomAutoColor,
        onTopTabColor = { topTabColor = it }
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

                scope.launch {
                    val notice = if (!config.activationRemoteEnabled) {
                        activation.remainingDaysNoticeForLaunch(-1L, shellActivationCodes())
                    } else {
                        null
                    }
                    if (notice != null) {
                        remainingDaysNotice = notice
                        remainingDaysIncludeSplash = false
                        remainingDaysFromLaunch = false
                    } else {
                        openShellAnnouncement(fromLaunch = false)
                    }
                }
            }
        )
    }

    remainingDaysNotice?.let { notice ->
        RemainingDaysNoticeDialog(
            notice = notice,
            onDismiss = {
                val includeSplash = remainingDaysIncludeSplash
                val fromLaunch = remainingDaysFromLaunch
                remainingDaysNotice = null
                scope.launch {
                    openShellAnnouncement(fromLaunch)
                    if (includeSplash) openShellSplash()
                }
            }
        )
    }

    if (showAnnouncementDialog &&
        (config.announcementTitle.isNotEmpty() || config.announcementContent.isNotEmpty())
    ) {
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

    val appearance = resolveStatusBarAppearance(
        mode = if (isDarkTheme) statusConfig.statusBarColorModeDark else statusConfig.statusBarColorMode,
        customColor = if (isDarkTheme) statusConfig.statusBarColorDark else statusConfig.statusBarColor,
        backgroundType = if (isDarkTheme) statusConfig.statusBarBackgroundTypeDark else statusConfig.statusBarBackgroundType,
        backgroundImage = if (isDarkTheme) statusConfig.statusBarBackgroundImageDark else statusConfig.statusBarBackgroundImage,
        backgroundAlpha = if (isDarkTheme) statusConfig.statusBarBackgroundAlphaDark else statusConfig.statusBarBackgroundAlpha,
        darkIcons = if (isDarkTheme) statusConfig.statusBarDarkIconsDark else statusConfig.statusBarDarkIcons,
        pageTop = statusBarAutoColor,
        topTabColor = topTabColor,
        themeColor = MaterialTheme.colorScheme.background.toArgb(),
        pageBehindBar = hideToolbar && !showToolbar,
        browserToolbarVisible = showToolbar,
        classicWindow = WindowHelper.isClassicKeyboardResize(webViewConfig.keyboardAdjustMode)
    )
    SideEffect { onStatusBarAppearanceChanged(appearance) }
    if (!hideToolbar || statusConfig.showStatusBarInFullscreen) {
        StatusBarOverlay(appearance, statusConfig.statusBarHeightDp, Modifier.align(Alignment.TopStart))
    }

    }
    }
}
