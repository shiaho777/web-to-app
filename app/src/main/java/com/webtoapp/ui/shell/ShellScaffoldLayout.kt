package com.webtoapp.ui.shell

import android.webkit.WebView
import com.webtoapp.ui.components.PremiumButton
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Terminal
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
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.webview.WebViewCallbacks
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.data.model.resolveToolbarButtons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.ShellScaffoldLayout(
    config: ShellConfig,
    appType: String,
    hideToolbar: Boolean,
    hideBrowserToolbar: Boolean = false,

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

    statusBarHeightDp: Int
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
            onReload = { webViewRef?.reload() }
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
    val hasAnyToolbarItem = toolbarCfg.toolbarShowTitle || toolbarCfg.toolbarShowUrl ||
        toolbarCfg.toolbarShowBack || toolbarCfg.toolbarShowForward || toolbarCfg.toolbarShowRefresh
    val showSlimToolbar = hideBrowserToolbar && toolbarCfg.browserToolbarCustomized && hasAnyToolbarItem
    val showToolbar = (!hideToolbar || config.webViewConfig.showToolbarInFullscreen) &&
        (!hideBrowserToolbar || showSlimToolbar)

    // In normal (non-hide) mode the toolbar always shows the full button set; only the
    // customized slim mode applies the toolbarShow* filters. This keeps a normal-mode
    // app from ending up with every button hidden after the hide toggle was turned off.
    val toolbarVisibility = resolveToolbarButtons(
        hideBrowserToolbar = toolbarCfg.hideBrowserToolbar,
        browserToolbarCustomized = toolbarCfg.browserToolbarCustomized,
        toolbarShowTitle = toolbarCfg.toolbarShowTitle,
        toolbarShowUrl = toolbarCfg.toolbarShowUrl,
        toolbarShowBack = toolbarCfg.toolbarShowBack,
        toolbarShowForward = toolbarCfg.toolbarShowForward,
        toolbarShowRefresh = toolbarCfg.toolbarShowRefresh
    )

    Scaffold(

        contentWindowInsets = if (hideToolbar && !showToolbar) {
            WindowInsets(0, 0, 0, 0)
        } else {
            ScaffoldDefaults.contentWindowInsets
        },
        modifier = Modifier,
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
                    showRefresh = toolbarVisibility.showRefresh,
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    webViewRef = webViewRef,
                    showConsoleButton = toolbarVisibility.showConsoleButton,
                    showConsole = showConsole,
                    onToggleConsole = onToggleConsole,
                    consoleErrorCount = consoleMessages.count { it.level == ConsoleLevel.ERROR }
                )
            }
        }
    ) { padding ->

        val density = LocalDensity.current

        val topInsetPx = WindowInsets.statusBars.getTop(density)
        val systemStatusBarHeightDp = if (topInsetPx > 0) {
            with(density) { topInsetPx.toDp() }
        } else {
            24.dp
        }

        val actualStatusBarPadding = if (statusBarHeightDp >= 0) statusBarHeightDp.dp else systemStatusBarHeightDp

        // 全屏模式下可选的内容内边距：把网页交互区从屏幕边缘内移，让角落按钮易于点按，
        // 同时缓解与系统返回手势边缘带的冲突。默认 0 → 向后兼容旧行为。
        val contentPad = config.webViewConfig.fullscreenContentPaddingDp.dp

        val contentModifier = when {
            hideToolbar && showToolbar -> {

                Modifier.fillMaxSize().padding(padding)
            }
            hideToolbar && config.webViewConfig.showStatusBarInFullscreen -> {

                Modifier.fillMaxSize().padding(
                    top = actualStatusBarPadding,
                    start = contentPad,
                    end = contentPad,
                    bottom = contentPad
                )
            }
            hideToolbar -> {

                Modifier.fillMaxSize().padding(contentPad)
            }
            else -> {

                Modifier.fillMaxSize().padding(padding)
            }
        }

        Box(modifier = contentModifier) {

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
                onActivityFinish = onActivityFinish
            )

            ShellLyricsOverlay(config = config, bgmState = bgmState)

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

            // Console panel (slides up from bottom when toggled)
            AnimatedVisibility(
                visible = showConsole,
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + fadeOut(),
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    canGoBack: Boolean,
    canGoForward: Boolean,
    webViewRef: WebView?,
    showConsoleButton: Boolean = true,
    showConsole: Boolean = false,
    onToggleConsole: () -> Unit = {},
    consoleErrorCount: Int = 0
) {
    val context = LocalContext.current

    TopAppBar(
        windowInsets = TopAppBarDefaults.windowInsets,
        title = {
            Column {
                if (showTitle) {
                    Text(
                        text = pageTitle.ifEmpty { appName },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (showUrl && currentUrl.isNotEmpty()) {
                    Text(
                        text = currentUrl.shortenForShellToolbar(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            if (showBack) {
                com.webtoapp.ui.design.WtaIconButton(
                    onClick = {
                        (context as? AppCompatActivity)?.let { activity ->
                            ShellWebViewNavigation.goBackOrFinish(activity, webViewRef)
                        }
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    enabled = canGoBack
                )
            }
            if (showForward) {
                com.webtoapp.ui.design.WtaIconButton(
                    onClick = { webViewRef?.goForward() },
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    enabled = canGoForward
                )
            }
            if (showRefresh) {
                com.webtoapp.ui.design.WtaIconButton(
                    onClick = { webViewRef?.reload() },
                    icon = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
            // Console / terminal button (hidden along with the other toolbar buttons,
            // so a stripped-down toolbar doesn't leave it as the only visible control)
            if (showConsoleButton) {
                IconButton(onClick = onToggleConsole) {
                    BadgedBox(
                        badge = {
                            if (consoleErrorCount > 0) {
                                Badge { Text("$consoleErrorCount") }
                            }
                        }
                    ) {
                        Icon(
                            if (showConsole) Icons.Default.Terminal
                            else Icons.Outlined.Terminal,
                            contentDescription = Strings.console,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(

            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

private fun String.shortenForShellToolbar(): String {
    val withoutScheme = when {
        startsWith("https://") -> substring(8)
        startsWith("http://") -> substring(7)
        else -> this
    }

    return withoutScheme.substringBefore('?').substringBefore('#')
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
    onActivityFinish: () -> Unit
) {

    if (!isActivationChecked) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
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
            onActivityFinish = onActivityFinish
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
