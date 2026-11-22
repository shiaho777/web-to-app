package com.webtoapp.ui.shell

import android.webkit.WebView
import com.webtoapp.ui.components.PremiumButton
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.webtoapp.core.bgm.bgmControlEnabled
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.ui.design.WtaFadeIn
import com.webtoapp.ui.design.WtaFadeOut
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.shared.PageEdgeChrome
import com.webtoapp.ui.shared.effectiveBottomContentPadding
import com.webtoapp.ui.shared.pageEdgeBands
import com.webtoapp.ui.shared.needsPanelImePadding
import com.webtoapp.ui.shared.parseBandColor
import com.webtoapp.core.webview.WebViewCallbacks
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.data.model.resolveToolbarButtons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.ShellScaffoldLayout(
    config: ShellConfig,
    appType: String,
    hideToolbar: Boolean,
    showToolbar: Boolean,

    isLoading: Boolean,
    loadProgress: Int,
    pageTitle: String,
    currentUrl: String,
    errorMessage: String?,
    isActivationChecked: Boolean,
    isActivated: Boolean,
    canGoBack: Boolean,
    canGoForward: Boolean,
    webViewRecreationKey: Int,

    webViewRef: WebView?,
    browserSurface: com.webtoapp.core.engine.BrowserSurface? = null,
    webViewConfig: WebViewConfig,
    webViewCallbacks: WebViewCallbacks,
    webViewManager: com.webtoapp.core.webview.WebViewManager,
    deepLinkUrl: String?,
    bgmState: BgmPlayerState,

    swipeRefreshEnabled: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,

    onWebViewCreated: (WebView) -> Unit,
    onBrowserSurfaceCreated: (com.webtoapp.core.engine.BrowserSurface) -> Unit = {},
    onWebViewRefUpdated: (WebView) -> Unit,
    onShowActivationDialog: () -> Unit,
    onErrorDismiss: () -> Unit,
    onActivityFinish: () -> Unit,

    showConsole: Boolean,
    onToggleConsole: () -> Unit,
    consoleMessages: List<ConsoleLogEntry>,
    onClearConsole: () -> Unit,
    onRunScript: (String) -> Unit,

    showFindBar: Boolean,
    onToggleFindBar: () -> Unit,

    statusBarHeightDp: Int,
    pageTopColor: String? = null,
    pageBottomColor: String? = null,
    onTopTabColor: (String?) -> Unit = {}
) {
    val context = LocalContext.current

    var autoRefreshController by remember { mutableStateOf<com.webtoapp.core.webview.AutoRefreshController?>(null) }
    val autoRefreshRemaining = autoRefreshController?.remainingSeconds?.collectAsStateWithLifecycle()?.value ?: 0

    LaunchedEffect(webViewConfig.autoRefreshEnabled, webViewConfig.autoRefreshIntervalSec, webViewConfig.autoRefreshShowCountdown, webViewRecreationKey) {
        autoRefreshController?.stop()
        autoRefreshController = null
        if (!webViewConfig.autoRefreshEnabled) return@LaunchedEffect
        val controller = com.webtoapp.core.webview.AutoRefreshController(
            intervalSec = webViewConfig.autoRefreshIntervalSec.coerceAtLeast(1),
            showCountdown = webViewConfig.autoRefreshShowCountdown,
            onReload = { browserSurface?.reload() ?: webViewRef?.reload() }
        )
        autoRefreshController = controller
        controller.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            autoRefreshController?.stop()
        }
    }

    val toolbarCfg = config.webViewConfig
    val toolbarEnabled = toolbarCfg.browserToolbarEnabled
    val chromeHost = remember { BrowserChromeHost() }

    val toolbarVisibility = resolveToolbarButtons(
        toolbarEnabled = toolbarEnabled,
        toolbarShowTitle = toolbarCfg.toolbarShowTitle,
        toolbarShowUrl = toolbarCfg.toolbarShowUrl,
        toolbarShowBack = toolbarCfg.toolbarShowBack,
        toolbarShowForward = toolbarCfg.toolbarShowForward,
        toolbarShowRefresh = toolbarCfg.toolbarShowRefresh,
        toolbarShowConsole = toolbarCfg.toolbarShowConsole,
        toolbarShowFind = toolbarCfg.toolbarShowFind
    )

    val panelImePadding = if (needsPanelImePadding(webViewConfig.keyboardAdjustMode)) {
        Modifier.imePadding()
    } else {
        Modifier
    }

    CompositionLocalProvider(LocalBrowserChromeHost provides if (showToolbar) chromeHost else null) {
    Scaffold(

        contentWindowInsets = if (hideToolbar) {
            WindowInsets(0, 0, 0, 0)
        } else {
            ScaffoldDefaults.contentWindowInsets
        },
        modifier = panelImePadding,
        topBar = {
            if (showToolbar) {
                ShellTopAppBar(
                    pageTitle = pageTitle,
                    appName = config.appName,
                    currentUrl = currentUrl,
                    showTitle = toolbarVisibility.showTitle,
                    showUrl = toolbarVisibility.showUrl,
                    showBack = toolbarVisibility.showBack,
                    showForward = toolbarVisibility.showForward,
                    showRefresh = toolbarVisibility.showRefresh || chromeHost.refreshAction != null,
                    onRefresh = { chromeHost.refreshAction?.invoke() ?: browserSurface?.reload() ?: webViewRef?.reload() },
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    webViewRef = webViewRef,
                    browserSurface = browserSurface,
                    showConsoleButton = toolbarVisibility.showConsoleButton,
                    showConsole = showConsole,
                    onToggleConsole = onToggleConsole,
                    consoleErrorCount = consoleMessages.count { it.level == ConsoleLevel.ERROR },
                    showFindButton = toolbarVisibility.showFind,
                    showFindBar = showFindBar,
                    onToggleFindBar = onToggleFindBar,
                    showPluginButton = config.pluginsEnabled,
                    leadingAction = chromeHost.leadingAction
                )
            }
        }
    ) { padding ->

        val density = LocalDensity.current

        val classicSystemBars = LocalContext.current.let { ctx ->
            (ctx as? android.app.Activity)?.let { com.webtoapp.ui.shared.WindowHelper.isClassicSystemBarsWindow(it) } ?: false
        }

        val systemStatusBarHeightDp = if (classicSystemBars) {
            0.dp
        } else {
            val topInsetPx = WindowInsets.statusBars.getTop(density)
            if (topInsetPx > 0) {
                with(density) { topInsetPx.toDp() }
            } else {
                0.dp
            }
        }

        val actualStatusBarPadding = if (statusBarHeightDp >= 0) statusBarHeightDp.dp else systemStatusBarHeightDp

        val padTop = config.webViewConfig.fullscreenPadTop.dp
        val padStart = config.webViewConfig.fullscreenPadStart.dp
        val padEnd = config.webViewConfig.fullscreenPadEnd.dp
        val padBottom = effectiveBottomContentPadding(config.webViewConfig.fullscreenPadBottom.dp)

        val shellDark = androidx.compose.foundation.isSystemInDarkTheme()
        val shellBgType = if (shellDark) config.webViewConfig.statusBarBackgroundTypeDark else config.webViewConfig.statusBarBackgroundType
        val shellMode = if (shellDark) config.webViewConfig.statusBarColorModeDark else config.webViewConfig.statusBarColorMode
        val shellOverlaysContent = shellBgType == "IMAGE" ||
            shellMode == com.webtoapp.data.model.StatusBarColorMode.TRANSPARENT.name

        val contentPadding = when {
            !hideToolbar || showToolbar -> padding
            config.webViewConfig.showStatusBarInFullscreen -> {
                PaddingValues(
                    top = (if (shellOverlaysContent) 0.dp else actualStatusBarPadding) + padTop,
                    start = padStart,
                    end = padEnd,
                    bottom = padBottom
                )
            }
            else -> PaddingValues(top = padTop, start = padStart, end = padEnd, bottom = padBottom)
        }
        val contentModifier = Modifier.fillMaxSize().padding(contentPadding).then(
            if (appType == "MULTI_WEB") Modifier.consumeWindowInsets(contentPadding) else Modifier
        )

        val customBand = if (shellDark) config.webViewConfig.statusBarColorDark else config.webViewConfig.statusBarColor
        val topBand = parseBandColor(PageEdgeChrome.topBandHex(shellMode, pageTopColor, customBand, shellDark))
        val bottomBand = parseBandColor(
            PageEdgeChrome.bottomBandHex(shellMode, pageBottomColor, pageTopColor, customBand, shellDark)
        )
        val bottomBandPx = with(density) { padBottom.toPx() }
        val bands = Modifier.pageEdgeBands(topBand, bottomBand, bottomBandPx)
        val bandedModifier = if (hideToolbar && !showToolbar) bands.then(contentModifier) else contentModifier

        Box(modifier = bandedModifier) {

            WebViewLoadingBar(
                visible = isLoading,
                progress = loadProgress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )

            ShellContentArea(
                config = config,
                appType = appType,
                isActivationChecked = isActivationChecked,
                isActivated = isActivated,
                webViewRecreationKey = webViewRecreationKey,
                webViewConfig = webViewConfig,
                webViewCallbacks = webViewCallbacks,
                webViewManager = webViewManager,
                deepLinkUrl = deepLinkUrl,
                swipeRefreshEnabled = swipeRefreshEnabled,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                onWebViewCreated = onWebViewCreated,
        onBrowserSurfaceCreated = onBrowserSurfaceCreated,
                onWebViewRefUpdated = onWebViewRefUpdated,
                onShowActivationDialog = onShowActivationDialog,
                onActivityFinish = onActivityFinish,
                onTopTabColor = onTopTabColor
            )

            ShellLyricsOverlay(config = config, bgmState = bgmState)

            if (webViewConfig.showFloatingBackButton && !showToolbar && canGoBack) {
                var fabAlpha by remember { mutableFloatStateOf(0.9f) }
                var fadeKey by remember { mutableIntStateOf(0) }

                LaunchedEffect(canGoBack, fadeKey) {
                    fabAlpha = 0.9f
                    kotlinx.coroutines.delay(3000L)
                    val steps = 20
                    val stepDelay = 30L
                    for (i in 1..steps) {
                        fabAlpha = 0.9f - (0.65f * i / steps)
                        kotlinx.coroutines.delay(stepDelay)
                    }
                }

                SmallFloatingActionButton(
                    onClick = {
                        fadeKey++
                        (context as? AppCompatActivity)?.let { activity ->
                            if (browserSurface != null) {
                                ShellWebViewNavigation.goBackOrFinish(activity, browserSurface)
                            } else {
                                ShellWebViewNavigation.goBackOrFinish(activity, webViewRef)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = actualStatusBarPadding + 8.dp)
                        .graphicsLayer { alpha = fabAlpha },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    ),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.cdBack)
                }
            }

            if (config.bgmEnabled &&
                bgmControlEnabled(config.bgmShowFloatingPlayer) &&
                config.bgmPlaylist.isNotEmpty()
            ) {
                BgmFloatingPlayer(
                    title = bgmState.title,
                    playing = bgmState.isPlaying,
                    onToggle = { bgmState.toggle() },
                    onPrevious = { bgmState.previous() },
                    onNext = { bgmState.next() },
                    onClose = { bgmState.pause() }
                )
            }

            if (autoRefreshRemaining > 0 && autoRefreshController?.countdownVisible == true) {
                com.webtoapp.ui.components.AutoRefreshCountdownChip(
                    remainingSeconds = autoRefreshRemaining,
                    onClick = { autoRefreshController?.pauseBriefly() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                )
            }

            ShellErrorCard(
                errorMessage = errorMessage,
                onDismiss = onErrorDismiss
            )

            AnimatedVisibility(
                visible = showConsole,
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + WtaFadeIn,
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + WtaFadeOut,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                ConsolePanel(
                    consoleMessages = consoleMessages,
                    isExpanded = false,
                    onExpandToggle = onToggleConsole,
                    onClear = onClearConsole,
                    onRunScript = onRunScript,
                    onClose = onToggleConsole
                )
            }

            AnimatedVisibility(
                visible = showFindBar,
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + WtaFadeIn,
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + WtaFadeOut,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FindInPageBar(
                    surface = browserSurface,
                    onClose = onToggleFindBar
                )
            }

            if (config.pluginsEnabled) {
                val pluginContext = LocalContext.current
                val pluginsUnsupported = remember(config.engineType, webViewConfig.dnsConfig.echEffective) {
                    com.webtoapp.core.engine.EngineViewFactory.resolveEngineType(
                        config.engineType,
                        webViewConfig,
                        pluginContext
                    ) == com.webtoapp.core.engine.EngineType.GECKOVIEW
                }
                com.webtoapp.ui.plugin.PluginSurfaceHost(
                    entryStyle = com.webtoapp.core.plugin.PluginEntryStyle.parse(config.pluginEntryStyle),
                    toolbarVisible = showToolbar,
                    floatingHandleModifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 24.dp),
                    pluginsUnsupported = pluginsUnsupported
                )
            }
        }
    }
    }
}

@Composable
private fun ShellTopAppBar(
    pageTitle: String,
    appName: String,
    currentUrl: String,
    showTitle: Boolean,
    showUrl: Boolean,
    showBack: Boolean,
    showForward: Boolean,
    showRefresh: Boolean,
    onRefresh: () -> Unit,
    canGoBack: Boolean,
    canGoForward: Boolean,
    webViewRef: WebView?,
    browserSurface: com.webtoapp.core.engine.BrowserSurface? = null,
    showConsoleButton: Boolean = true,
    showConsole: Boolean = false,
    onToggleConsole: () -> Unit = {},
    consoleErrorCount: Int = 0,
    showFindButton: Boolean = true,
    showFindBar: Boolean = false,
    onToggleFindBar: () -> Unit = {},
    showPluginButton: Boolean = false,
    leadingAction: BrowserChromeAction? = null
) {
    val context = LocalContext.current
    BrowserChromeBar(
        title = pageTitle.ifEmpty { appName },
        showTitle = showTitle,
        url = currentUrl,
        showUrl = showUrl,
        showBack = showBack,
        showForward = showForward,
        showRefresh = showRefresh,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        onBack = {
            (context as? AppCompatActivity)?.let { activity ->
                if (browserSurface != null) {
                    ShellWebViewNavigation.goBackOrFinish(activity, browserSurface)
                } else {
                    ShellWebViewNavigation.goBackOrFinish(activity, webViewRef)
                }
            }
        },
        onForward = { browserSurface?.goForward() ?: webViewRef?.goForward() },
        onRefresh = onRefresh,
        showConsoleButton = showConsoleButton,
        consoleExpanded = showConsole,
        onToggleConsole = onToggleConsole,
        consoleErrorCount = consoleErrorCount,
        showFindButton = showFindButton,
        findExpanded = showFindBar,
        onToggleFind = onToggleFindBar,
        showPluginButton = showPluginButton,
        leadingAction = leadingAction,
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    )
}

@Composable
private fun ShellContentArea(
    config: ShellConfig,
    appType: String,
    isActivationChecked: Boolean,
    isActivated: Boolean,
    webViewRecreationKey: Int,
    webViewConfig: WebViewConfig,
    webViewCallbacks: WebViewCallbacks,
    webViewManager: com.webtoapp.core.webview.WebViewManager,
    deepLinkUrl: String?,

    swipeRefreshEnabled: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onBrowserSurfaceCreated: (com.webtoapp.core.engine.BrowserSurface) -> Unit = {},
    onWebViewRefUpdated: (WebView) -> Unit,
    onShowActivationDialog: () -> Unit,
    onActivityFinish: () -> Unit,
    onTopTabColor: (String?) -> Unit = {}
) {

    if (!isActivationChecked) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            WtaLoadingIndicator()
        }
    }

    else if (!isActivated && config.activationEnabled) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(Strings.pleaseActivateApp)
                Spacer(modifier = Modifier.height(16.dp))
                PremiumButton(onClick = onShowActivationDialog) {
                    Text(Strings.enterActivationCode)
                }
            }
        }
    } else {

        ShellContentRouter(
            appType = appType,
            config = config,
            webViewRecreationKey = webViewRecreationKey,
            webViewConfig = webViewConfig,
            webViewCallbacks = webViewCallbacks,
            webViewManager = webViewManager,
            deepLinkUrl = deepLinkUrl,
            swipeRefreshEnabled = swipeRefreshEnabled,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            onWebViewCreated = onWebViewCreated,
        onBrowserSurfaceCreated = onBrowserSurfaceCreated,
            onWebViewRefUpdated = onWebViewRefUpdated,
            onActivityFinish = onActivityFinish,
            onTopTabColor = onTopTabColor
        )
    }
}

@Composable
private fun WebViewLoadingBar(
    visible: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) progress.coerceIn(0f, 1f) else 1f,
        animationSpec = com.webtoapp.ui.design.WtaMotion.settleSpring(),
        label = "webviewProgress"
    )
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = com.webtoapp.ui.design.WtaMotion.exitTween(
            durationMillis = com.webtoapp.ui.design.WtaMotion.DurationMedium
        ),
        label = "webviewProgressAlpha"
    )
    if (alpha <= 0f) return

    val primary = MaterialTheme.colorScheme.primary
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .height(2.dp)
            .graphicsLayer { this.alpha = alpha }
    ) {
        val fillWidth = size.width * animatedProgress
        drawRect(
            color = primary,
            size = Size(fillWidth, size.height)
        )
    }
}
