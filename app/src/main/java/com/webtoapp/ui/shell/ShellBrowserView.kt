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
import com.webtoapp.core.webview.WebScrollTracker
import com.webtoapp.core.webview.WebViewCallbacks
import com.webtoapp.core.webview.WebViewManager
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.ui.components.WebSwipeRefreshLayout

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
                WebSwipeRefreshLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setColorSchemeColors(
                        android.graphics.Color.parseColor("#6750A4"),
                        android.graphics.Color.parseColor("#7F67BE")
                    )
                    gestureZone = webViewConfig.swipeRefreshZone
                    isEnabled = swipeRefreshEnabled
                    var surfaceRef: BrowserSurface? = null
                    setOnRefreshListener {
                        onRefresh()
                        surfaceRef?.reload()
                    }
                    setOnChildScrollUpCallback { _, child ->
                        val wv = child as? WebView
                        if (wv != null) {
                            WebScrollTracker.scrollUpBlocked(wv, wv.scrollY)
                        } else {
                            child?.canScrollVertically(-1) == true
                        }
                    }

                    val surface = EngineViewFactory.create(
                        context = ctx,
                        engineTypeName = config.engineType,
                        config = webViewConfig,
                        webViewManager = webViewManager,
                        callbacks = webViewCallbacks,
                        adBlocker = WebToAppApplication.adBlock,
                        pluginPayloads = config.embeddedPlugins
                            .map { it.toResolved() },
                        pluginsEnabled = config.pluginsEnabled,
                        pluginEntryStyle = com.webtoapp.core.plugin.PluginEntryStyle.parse(config.pluginEntryStyle),
                        pluginPanelStyle = com.webtoapp.core.plugin.PluginPanelStyle.parse(config.pluginPanelStyle),
                        browserDisguiseConfig = config.browserDisguiseConfig,
                        deviceDisguiseConfig = config.deviceDisguiseConfig,
                        appOriginUrl = config.targetUrl
                    )
                    surfaceRef = surface
                    tag = surface
                    onSurfaceCreated?.invoke(surface)

                    val wv = surface.webView
                    if (wv != null && enableLongPress) {
                        var lastTouchX = 0f
                        var lastTouchY = 0f
                        var downFromFinger = false
                        wv.setOnTouchListener { view, event ->
                            when (event.actionMasked) {
                                MotionEvent.ACTION_DOWN -> {
                                    lastTouchX = event.x
                                    lastTouchY = event.y
                                    downFromFinger =
                                        event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER
                                    if (event.isFromSource(android.view.InputDevice.SOURCE_MOUSE)) {
                                        // A mouse click does not always move focus
                                        // by itself on every OEM path; make it
                                        // explicit so a hardware keyboard lands in
                                        // the page afterwards (#1031).
                                        view.requestFocus()
                                    }
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    lastTouchX = event.x
                                    lastTouchY = event.y
                                }
                                MotionEvent.ACTION_UP -> {
                                    view.performClick()
                                    downFromFinger = false
                                }
                                MotionEvent.ACTION_CANCEL -> downFromFinger = false
                            }
                            false
                        }
                        wv.setOnLongClickListener {
                            // A right-click also routes through performLongClick —
                            // keep the touch menu finger-only so pointer/keyboard
                            // long-clicks fall back to the default context menu
                            // instead of a touch menu at a stale position (#1031).
                            if (downFromFinger) {
                                webViewCallbacks.onLongPress(wv, lastTouchX, lastTouchY)
                            } else {
                                false
                            }
                        }
                    }

                    if (wv != null) {
                        WebScrollTracker.install(wv)
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
            },
            modifier = modifier
        )
    }
}
