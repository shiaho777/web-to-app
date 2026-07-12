package com.webtoapp.ui.shell

import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.webview.WebViewCallbacks
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.ui.components.EdgeSwipeRefreshLayout

@Composable
fun ShellContentRouter(
    appType: String,
    config: ShellConfig,
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
    onActivityFinish: () -> Unit
) {
    when {
        appType == "IMAGE" || appType == "VIDEO" -> {

            MediaContentDisplay(
                isVideo = appType == "VIDEO",
                mediaConfig = config.mediaConfig
            )
        }
        appType == "GALLERY" -> {

            AppLogger.d("ShellScreen", "进入 GALLERY 分支，显示 ShellGalleryPlayer")
            ShellGalleryPlayer(
                galleryConfig = config.galleryConfig,
                onBack = onActivityFinish
            )
        }
        appType == "WORDPRESS" -> {

            WordPressShellMode(
                config = config,
                webViewRecreationKey = webViewRecreationKey,
                webViewConfig = webViewConfig,
                webViewCallbacks = webViewCallbacks,
                webViewManager = webViewManager,
                onWebViewCreated = onWebViewCreated,
                onWebViewRefUpdated = onWebViewRefUpdated,
                swipeRefreshEnabled = swipeRefreshEnabled,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
        }
        appType == "NODEJS_APP" -> {
            val nodejsMode = config.nodejsConfig.mode
            if (nodejsMode == "STATIC") {

                NodeJsStaticShellMode(
                    config = config,
                    webViewRecreationKey = webViewRecreationKey,
                    webViewConfig = webViewConfig,
                    webViewCallbacks = webViewCallbacks,
                    webViewManager = webViewManager,
                    onWebViewCreated = onWebViewCreated,
                    onWebViewRefUpdated = onWebViewRefUpdated,
                    swipeRefreshEnabled = swipeRefreshEnabled,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                )
            } else {

                NodeJsShellMode(
                    config = config,
                    webViewRecreationKey = webViewRecreationKey,
                    webViewConfig = webViewConfig,
                    webViewCallbacks = webViewCallbacks,
                    webViewManager = webViewManager,
                    onWebViewCreated = onWebViewCreated,
                onWebViewRefUpdated = onWebViewRefUpdated,
                    swipeRefreshEnabled = swipeRefreshEnabled,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                )
            }
        }
        appType == "PHP_APP" -> {
            PhpAppShellMode(
                config = config,
                webViewRecreationKey = webViewRecreationKey,
                webViewConfig = webViewConfig,
                webViewCallbacks = webViewCallbacks,
                webViewManager = webViewManager,
                onWebViewCreated = onWebViewCreated,
                onWebViewRefUpdated = onWebViewRefUpdated,
                swipeRefreshEnabled = swipeRefreshEnabled,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
        }
        appType == "PYTHON_APP" -> {
            AppLogger.i("ShellScreen", "进入 PYTHON_APP 分支，启动 PythonAppShellMode")
            AppLogger.i("ShellScreen", "pythonAppConfig: framework=${config.pythonAppConfig.framework}, entry=${config.pythonAppConfig.entryFile}, module=${config.pythonAppConfig.entryModule}, server=${config.pythonAppConfig.serverType}, port=${config.pythonAppConfig.port}")
            PythonAppShellMode(
                config = config,
                webViewRecreationKey = webViewRecreationKey,
                webViewConfig = webViewConfig,
                webViewCallbacks = webViewCallbacks,
                webViewManager = webViewManager,
                onWebViewCreated = onWebViewCreated,
                onWebViewRefUpdated = onWebViewRefUpdated,
                swipeRefreshEnabled = swipeRefreshEnabled,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
        }
        appType == "GO_APP" -> {
            AppLogger.i("ShellScreen", "进入 GO_APP 分支，启动 GoAppShellMode")
            GoAppShellMode(
                config = config,
                webViewRecreationKey = webViewRecreationKey,
                webViewConfig = webViewConfig,
                webViewCallbacks = webViewCallbacks,
                webViewManager = webViewManager,
                onWebViewCreated = onWebViewCreated,
                onWebViewRefUpdated = onWebViewRefUpdated,
                swipeRefreshEnabled = swipeRefreshEnabled,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
        }
        appType == "MULTI_WEB" -> {
            if (config.siteId.isNotBlank()) {
                AppLogger.e("ShellContentRouter", "Recursive MULTI_WEB detected for site ${config.siteId}, aborting")
            } else {
                AppLogger.i("ShellScreen", "进入 MULTI_WEB 分支，启动 MultiWebShellMode, mode=${config.multiWebConfig.displayMode}, sites=${config.multiWebConfig.sites.size}")
                MultiWebShellMode(
                    config = config,
                    webViewConfig = webViewConfig,
                    webViewCallbacks = webViewCallbacks,
                    webViewManager = webViewManager,
                    onWebViewCreated = onWebViewCreated,
                    swipeRefreshEnabled = swipeRefreshEnabled,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                )
            }
        }
        appType == "HTML" || appType == "FRONTEND" -> {
            HtmlFrontendShellMode(
                config = config,
                webViewRecreationKey = webViewRecreationKey,
                webViewConfig = webViewConfig,
                webViewCallbacks = webViewCallbacks,
                webViewManager = webViewManager,
                onWebViewCreated = onWebViewCreated,
                onWebViewRefUpdated = onWebViewRefUpdated,
                swipeRefreshEnabled = swipeRefreshEnabled,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
        }
        else -> {

            val resolvedUrl = normalizeShellTargetUrlForSecurity(deepLinkUrl ?: config.targetUrl)
            AppLogger.d("ShellScreen", "进入 WebView 分支 (else)，加载 URL: $resolvedUrl")
            key(webViewRecreationKey) {
                ShellBrowserAndroidView(
                config = config,
                webViewRecreationKey = webViewRecreationKey,
                webViewConfig = webViewConfig,
                webViewCallbacks = webViewCallbacks,
                webViewManager = webViewManager,
                swipeRefreshEnabled = swipeRefreshEnabled,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                onWebViewCreated = onWebViewCreated,
                onWebViewRefUpdated = onWebViewRefUpdated,
                initialUrl = resolvedUrl,
                onSurfaceCreated = onBrowserSurfaceCreated
            )
            }
        }
    }
}

@Composable
fun ShellLocalFileWebView(
    config: ShellConfig,
    webViewRecreationKey: Int,
    webViewConfig: WebViewConfig,
    webViewCallbacks: WebViewCallbacks,
    webViewManager: com.webtoapp.core.webview.WebViewManager,
    targetUrl: String,
    enableJavaScript: Boolean,
    enableLocalStorage: Boolean,

    swipeRefreshEnabled: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onBrowserSurfaceCreated: (com.webtoapp.core.engine.BrowserSurface) -> Unit = {},
    onWebViewRefUpdated: (WebView) -> Unit
) {
    val localConfig = webViewConfig.copy(
        javaScriptEnabled = enableJavaScript,
        domStorageEnabled = enableLocalStorage
    )
    ShellBrowserAndroidView(
        config = config,
        webViewRecreationKey = webViewRecreationKey,
        webViewConfig = localConfig,
        webViewCallbacks = webViewCallbacks,
        webViewManager = webViewManager,
        swipeRefreshEnabled = swipeRefreshEnabled,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        onWebViewCreated = onWebViewCreated,
        onWebViewRefUpdated = onWebViewRefUpdated,
        initialUrl = targetUrl,
        onSurfaceCreated = onBrowserSurfaceCreated
    )
}

