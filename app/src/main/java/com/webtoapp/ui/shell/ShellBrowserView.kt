package com.webtoapp.ui.shell

import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.engine.BrowserSurface
import com.webtoapp.core.engine.EngineViewFactory
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.webview.WebViewCallbacks
import com.webtoapp.core.webview.WebViewManager
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.ui.components.EdgeSwipeRefreshLayout

@Composable
fun ShellBrowserAndroidView(
    config: ShellConfig,
    webViewRecreationKey: Int,
    webViewConfig: WebViewConfig,
    webViewCallbacks: WebViewCallbacks,
    webViewManager: WebViewManager,
    swipeRefreshEnabled: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onWebViewRefUpdated: (WebView) -> Unit,
    initialUrl: String? = null,
    enableLongPress: Boolean = true,
    onSurfaceCreated: ((BrowserSurface) -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    key(webViewRecreationKey) {
        AndroidView(
            factory = { ctx ->
                EdgeSwipeRefreshLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setColorSchemeColors(
                        android.graphics.Color.parseColor("#6750A4"),
                        android.graphics.Color.parseColor("#7F67BE")
                    )
                    isEnabled = swipeRefreshEnabled
                    var surfaceRef: BrowserSurface? = null
                    setOnRefreshListener {
                        onRefresh()
                        surfaceRef?.reload()
                    }
                    setOnChildScrollUpCallback { _, child ->
                        val wv = child as? WebView
                        if (wv != null) wv.scrollY > 0 else (child?.canScrollVertically(-1) == true)
                    }

                    val surface = EngineViewFactory.create(
                        context = ctx,
                        engineTypeName = config.engineType,
                        config = webViewConfig,
                        webViewManager = webViewManager,
                        callbacks = webViewCallbacks,
                        adBlocker = WebToAppApplication.adBlock,
                        extensionModuleIds = config.extensionModuleIds,
                        embeddedExtensionModules = config.embeddedExtensionModules,
                        extensionFabIcon = config.extensionFabIcon,
                        allowGlobalModuleFallback = false,
                        extensionEnabled = config.extensionEnabled ||
                            config.extensionModuleIds.isNotEmpty() ||
                            config.embeddedExtensionModules.isNotEmpty(),
                        browserDisguiseConfig = config.browserDisguiseConfig,
                        deviceDisguiseConfig = config.deviceDisguiseConfig
                    )
                    surfaceRef = surface
                    tag = surface
                    onSurfaceCreated?.invoke(surface)

                    val wv = surface.webView
                    if (wv != null && enableLongPress) {
                        var lastTouchX = 0f
                        var lastTouchY = 0f
                        wv.setOnTouchListener { view, event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN,
                                MotionEvent.ACTION_MOVE -> {
                                    lastTouchX = event.x
                                    lastTouchY = event.y
                                }
                                MotionEvent.ACTION_UP -> view.performClick()
                            }
                            false
                        }
                        wv.setOnLongClickListener {
                            webViewCallbacks.onLongPress(wv, lastTouchX, lastTouchY)
                        }
                    }

                    if (wv != null) {
                        onWebViewCreated(wv)
                        onWebViewRefUpdated(wv)
                        if (wv.tag == "state_restored") {
                            surface.reload()
                        } else if (!initialUrl.isNullOrBlank()) {
                            surface.loadUrl(initialUrl)
                        }
                    } else if (!initialUrl.isNullOrBlank()) {
                        surface.loadUrl(initialUrl)
                    }

                    addView(
                        surface.view,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            },
            update = { swipeLayout ->
                swipeLayout.isEnabled = swipeRefreshEnabled
                if (swipeLayout.isRefreshing != isRefreshing) {
                    swipeLayout.isRefreshing = isRefreshing
                }
                if (!isRefreshing && swipeLayout.isRefreshing) {
                    swipeLayout.isRefreshing = false
                }
            },
            modifier = modifier
        )
    }
}
