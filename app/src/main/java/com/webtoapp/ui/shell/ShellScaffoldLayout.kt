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
import androidx.compose.material.icons.automirrored.filled.*
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
    forcedRunActive: Boolean,
    forcedRunBlocked: Boolean,
    forcedRunBlockedMessage: String,
    forcedRunRemainingMs: Long,
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

    Scaffold(

        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        modifier = Modifier,
        topBar = {
            if (showToolbar) {
                ShellTopAppBar(
                    pageTitle = pageTitle,
                    appName = config.appName,
                    currentUrl = currentUrl,
                    showTitle = config.webViewConfig.toolbarShowTitle,
                    showUrl = config.webViewConfig.toolbarShowUrl,
                    showBack = config.webViewConfig.toolbarShowBack,
                    showForward = config.webViewConfig.toolbarShowForward,
                    showRefresh = config.webViewConfig.toolbarShowRefresh,
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    webViewRef = webViewRef
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

        val contentModifier = when {
            hideToolbar && showToolbar -> {

                Modifier.fillMaxSize().padding(padding)
            }
            hideToolbar && config.webViewConfig.showStatusBarInFullscreen -> {

                Modifier.fillMaxSize().padding(top = actualStatusBarPadding)
            }
            hideToolbar -> {

                Modifier.fillMaxSize()
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
                forcedRunBlocked = forcedRunBlocked,
                forcedRunBlockedMessage = forcedRunBlockedMessage,
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

            ShellForcedRunOverlay(
                config = config,
                forcedRunActive = forcedRunActive,
                forcedRunRemainingMs = forcedRunRemainingMs
            )

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
                forcedRunActive = forcedRunActive,
                onDismiss = onErrorDismiss
            )

            ShellVirtualNavBar(
                appType = appType,
                config = config,
                forcedRunActive = forcedRunActive,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                webViewRef = webViewRef
            )
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
    webViewRef: WebView?
) {
    val context = LocalContext.current

    TopAppBar(
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
    forcedRunBlocked: Boolean,
    forcedRunBlockedMessage: String,
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
    } else if (forcedRunBlocked) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(forcedRunBlockedMessage)
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
