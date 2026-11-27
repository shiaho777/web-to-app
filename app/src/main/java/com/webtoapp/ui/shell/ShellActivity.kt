package com.webtoapp.ui.shell

import com.webtoapp.core.engine.BrowserSurface
import com.webtoapp.core.engine.EngineType

import com.webtoapp.core.logging.AppLogger
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.theme.ShellTheme
import com.webtoapp.core.webview.BackgroundMediaRuntime
import com.webtoapp.core.webview.TranslateBridge
import com.webtoapp.data.model.DisplayCutoutMode
import com.webtoapp.data.model.KeyboardAdjustMode
import com.webtoapp.data.model.withNotificationPolyfill
import com.webtoapp.core.floatingwindow.FloatingWindowService
import com.webtoapp.ui.shared.StatusBarAppearance
import com.webtoapp.ui.shared.WindowHelper

private const val SHELL_RESUME_SESSION_KEY = "shell"
private const val KEY_SAVED_SURFACE_SITE_ID = "com.webtoapp.SAVED_SURFACE_SITE_ID"

class ShellActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var browserSurface: BrowserSurface? = null
    private var nativeBridge: com.webtoapp.core.webview.NativeBridge? = null
    private val backgroundMedia = BackgroundMediaRuntime(
        webView = { webView },
        surface = { browserSurface },
        dispatchHidden = { dispatchVisibilityEvent(visible = false) }
    )
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private var deepLinkUrl = mutableStateOf<String?>(null)

    val permissionDelegate = ShellPermissionDelegate(this)
    private val directoryPickerHost = com.webtoapp.core.webview.DirectoryPickerHost(this)
    private val startupPermissions = ShellStartupPermissions(this)

    private var immersiveFullscreenEnabled: Boolean = false
    private var showStatusBarInFullscreen: Boolean = false
    private var hideStatusBarInVideoFullscreen: Boolean = true
    private var showNavigationBarInFullscreen: Boolean = false
    private var translateBridge: TranslateBridge? = null
    private var clearBrowsingDataOnLaunch: Boolean = false

    private var originalOrientationBeforeFullscreen: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    private var statusBarColorMode: String = "THEME"
    private var statusBarCustomColor: String? = null
    private var statusBarDarkIcons: Boolean? = null
    private var statusBarBackgroundType: String = "COLOR"

    private var statusBarColorModeDark: String = "THEME"
    private var statusBarCustomColorDark: String? = null
    private var statusBarDarkIconsDark: Boolean? = null
    private var statusBarBackgroundTypeDark: String = "COLOR"
    private var statusBarAppearance: StatusBarAppearance? = null
    private var statusBarAutoColor: String? = null
    private var pageBottomAutoColor: String? = null
    private var keyboardAdjustMode: KeyboardAdjustMode = KeyboardAdjustMode.RESIZE
    private var displayCutoutMode: DisplayCutoutMode = DisplayCutoutMode.SHORT_EDGES

    private var pendingFloatingWindowLaunch = false
    private var notificationPolyfillEnabled = false

    private var shareReceiveMimeTypes: List<String> = emptyList()
    private var shareDeliveryMode: com.webtoapp.data.model.ShareDeliveryMode =
        com.webtoapp.data.model.ShareDeliveryMode.BOTH
    private var shareShowReceivedToast: Boolean = true

    private var openWithEnabled = false

    private var sharePageReady = false
    internal var mediaSessionBridge: com.webtoapp.core.webview.MediaSessionBridge? = null
    private var geckoMediaAdapter: com.webtoapp.core.engine.GeckoMediaSessionAdapter? = null

    private val screenAwakeHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var screenAwakeClearRunnable: Runnable? = null

    private var webViewStateBundle: Bundle? = null
    private var shellConfig: com.webtoapp.core.shell.ShellConfig? = null

    private val mouseInputCompat = com.webtoapp.core.webview.MouseInputCompat()

    private val resumeStore by lazy { com.webtoapp.core.webview.WebViewResumeStore(this) }

    internal fun noteExternalAppLaunch(sourceUrl: String?) {
        if (sourceUrl.isNullOrBlank()) return
        val baseUrl = shellConfig?.targetUrl ?: return
        resumeStore.persistExternalJump(SHELL_RESUME_SESSION_KEY, baseUrl, sourceUrl)
    }
    private fun refreshStatusBarAppearance() {
        if (customView == null) {
            if (!immersiveFullscreenEnabled || showStatusBarInFullscreen) statusBarAppearance?.applyTo(this)
            if (!immersiveFullscreenEnabled || showNavigationBarInFullscreen) applyPageEdgeNavigation(isSystemInDarkMode())
        }
    }

    private fun applyPageEdgeNavigation(isDark: Boolean) {
        val mode = if (isDark) statusBarColorModeDark else statusBarColorMode
        val custom = if (isDark) statusBarCustomColorDark else statusBarCustomColor
        com.webtoapp.ui.shared.PageEdgeChrome.applyNavigationBar(
            activity = this,
            mode = mode,
            sampleBottom = pageBottomAutoColor,
            sampleTop = statusBarAutoColor,
            custom = custom,
            darkIcons = if (isDark) statusBarDarkIconsDark else statusBarDarkIcons,
            isDark = isDark
        )
    }

    private fun isSystemInDarkMode(): Boolean =
        (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

    private fun applyImmersiveFullscreen(enabled: Boolean, hideNavBar: Boolean? = null, isDarkTheme: Boolean = isSystemInDarkMode()) {
        val shouldHideNavBar = hideNavBar ?: !showNavigationBarInFullscreen
        val systemDark = isSystemInDarkMode()
        val effectiveColorMode = if (systemDark) statusBarColorModeDark else statusBarColorMode
        val effectiveCustomColor = if (systemDark) statusBarCustomColorDark else statusBarCustomColor
        val effectiveShowStatusBar = showStatusBarInFullscreen &&
            !(hideStatusBarInVideoFullscreen && customView != null)
        WindowHelper.applyImmersiveFullscreen(
            activity = this,
            enabled = enabled,
            hideNavBar = shouldHideNavBar,
            isDarkTheme = isDarkTheme,
            showStatusBar = effectiveShowStatusBar,
            statusBarColorMode = effectiveColorMode,
            statusBarCustomColor = effectiveCustomColor,
            statusBarDarkIcons = if (systemDark) statusBarDarkIconsDark else statusBarDarkIcons,
            statusBarBgType = if (systemDark) statusBarBackgroundTypeDark else statusBarBackgroundType,
            keyboardAdjustMode = keyboardAdjustMode,
            displayCutoutMode = displayCutoutMode,
            tag = "ShellActivity",
            statusBarAppearance = statusBarAppearance
        )
        if (!enabled || !shouldHideNavBar) applyPageEdgeNavigation(systemDark)
    }

    private fun applyScreenAwakeMode() {
        val webViewConfig = WebToAppApplication.shellMode.getConfig()?.webViewConfig ?: return
        screenAwakeClearRunnable?.let { screenAwakeHandler.removeCallbacks(it) }
        screenAwakeClearRunnable = null
        when (webViewConfig.screenAwakeMode.uppercase()) {
            "ALWAYS" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "屏幕常亮: 始终常亮模式")
            }
            "TIMED" -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                val timeoutMinutes = webViewConfig.screenAwakeTimeoutMinutes
                val timeoutMs = (if (timeoutMinutes > 0) timeoutMinutes else 30) * 60 * 1000L
                val runnable = Runnable {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "屏幕常亮: 定时 ${timeoutMinutes} 分钟已到，恢复系统息屏")
                }
                screenAwakeClearRunnable = runnable
                screenAwakeHandler.postDelayed(runnable, timeoutMs)
                com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "屏幕常亮: 定时模式 ${timeoutMinutes} 分钟")
            }
            else -> {
                if (webViewConfig.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "屏幕常亮: 已启用（向后兼容模式）")
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }

    private fun shouldForwardKeyToWebView(event: KeyEvent): Boolean {
        if (event.isSystem) {
            return false
        }
        return when (event.keyCode) {
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_MUTE,
            KeyEvent.KEYCODE_POWER -> false
            else -> true
        }
    }

    private fun isFocusInsidePageView(): Boolean {
        var view = currentFocus ?: return true
        val pageView = browserSurface?.view ?: webView
        while (view is View) {
            if (view is WebView || (pageView != null && view == pageView)) return true
            view = view.parent as? View ?: return false
        }
        return false
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val volumeConfig = shellConfig?.webViewConfig
        if (com.webtoapp.core.webview.PageKeyEvents.handleVolume(
                event,
                forward = volumeConfig?.forwardVolumeKeys == true,
                consume = volumeConfig?.consumeVolumeKeys == true,
                webView = webView,
                surface = browserSurface
            )
        ) {
            return true
        }
        val isEnter = event.keyCode == KeyEvent.KEYCODE_ENTER ||
            event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
        if (shouldForwardKeyToWebView(event) && isFocusInsidePageView() &&
            (browserSurface?.dispatchKeyEvent(event) == true || webView?.dispatchKeyEvent(event) == true)
        ) {
            if (isEnter) {
                com.webtoapp.core.shell.ShellLogger.d(
                    "ShellActivity",
                    "Enter key delivered to page: meta=0x${Integer.toHexString(event.metaState)}"
                )
            }
            return true
        }

        if (isEnter) {
            com.webtoapp.core.shell.ShellLogger.d(
                "ShellActivity",
                "Enter key fell back to default dispatch (IME/focus path): meta=0x${Integer.toHexString(event.metaState)}"
            )
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mouseInputCompat.noteTouchEvent(ev.source, ev.getToolType(0), ev.actionMasked)
        return super.dispatchTouchEvent(ev)
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent): Boolean {
        val translated = mouseInputCompat.translateButtonAction(
            ev.source, ev.getToolType(0), ev.actionMasked, ev.actionButton
        )
        if (translated != null) {
            com.webtoapp.core.shell.ShellLogger.d(
                "ShellActivity",
                "Mouse primary button arrived via generic-motion path; re-dispatching as touch action=$translated"
            )
            val converted = com.webtoapp.core.webview.MouseInputCompat.copyWithAction(ev, translated)
            return try {
                dispatchTouchEvent(converted)
            } finally {
                converted.recycle()
            }
        }
        return super.dispatchGenericMotionEvent(ev)
    }

    fun handlePermissionRequest(request: PermissionRequest) = permissionDelegate.handlePermissionRequest(request)

    fun onCapturePermissionCanceled(request: PermissionRequest) =
        permissionDelegate.onCapturePermissionCanceled(request)

    internal fun reloadForCapturePermission() {
        reloadBrowser()
    }
    fun handleAndroidPermissionsRequest(permissions: Array<String>, onResult: (Boolean) -> Unit) =
        permissionDelegate.requestAndroidPermissions(permissions, onResult)
    fun handleGeolocationPermission(origin: String?, callback: GeolocationPermissions.Callback?) {
        val cfg = shellConfig
        permissionDelegate.handleGeolocationPermission(
            origin = origin,
            callback = callback,
            policy = cfg?.webViewConfig?.geolocationPolicy ?: "ALWAYS_ASK",
            accuracy = cfg?.webViewConfig?.geolocationAccuracy ?: "COARSE"
        )
    }

    fun requestGeolocationAccess(onResult: (Boolean) -> Unit) {
        permissionDelegate.requestLocationAccess(
            accuracy = shellConfig?.webViewConfig?.geolocationAccuracy ?: "COARSE",
            onResult = onResult
        )
    }

    fun handleDownloadWithPermission(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        referrer: String?
    ) = permissionDelegate.handleDownloadWithPermission(url, userAgent, contentDisposition, mimeType, referrer, webView)

    fun handleExternalDownloadResponse(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long,
        body: java.io.InputStream
    ) {
        val config = shellConfig
        if (config == null || !config.webViewConfig.downloadEnabled) {
            try { body.close() } catch (e: Exception) { }
            return
        }
        val mode = try {
            com.webtoapp.data.model.DownloadLocationMode.valueOf(config.webViewConfig.downloadLocationMode)
        } catch (e: Exception) {
            com.webtoapp.data.model.DownloadLocationMode.SYSTEM_DOWNLOAD
        }
        val filename = com.webtoapp.util.DownloadHelper.parseFileName(url, contentDisposition, mimeType)
        com.webtoapp.core.webview.DownloadBridge(
            this,
            lifecycleScope,
            mode,
            config.webViewConfig.customDownloadDirUri,
            config.webViewConfig.mediaAlbumName
        ).saveExternalDownload(body, filename, mimeType)
    }

    private fun resetFreshBrowsingSession() {
        webViewStateBundle = null
        com.webtoapp.core.webview.WebViewManager.beginFreshBrowsingSession()
        val gecko = com.webtoapp.core.engine.EngineType.fromString(shellConfig?.engineType ?: "") ==
            com.webtoapp.core.engine.EngineType.GECKOVIEW
        if (gecko) {
            if (com.webtoapp.core.engine.GeckoViewEngine.hasLiveRuntime()) {
                com.webtoapp.core.engine.GeckoViewEngine.clearStoredDataHoldingNavigation()
            }
            return
        }
        val wiped = com.webtoapp.core.webview.WebViewProfileWiper.wipeIfProviderInactive(applicationContext)
        com.webtoapp.core.webview.WebViewManager.noteFreshProfileWiped(wiped)
        if (!wiped) {
            com.webtoapp.core.webview.WebViewManager.clearBrowsingData(
                this,
                webView,
                holdNavigation = true
            )
        }
    }

    internal fun stashWebViewState(bundle: Bundle, siteId: String?) {
        bundle.putString(KEY_SAVED_SURFACE_SITE_ID, siteId)
        webViewStateBundle = bundle
    }

    internal fun clearWebViewRefs() {
        webView = null
        browserSurface = null
    }

    internal fun releaseSurfaceRefs(surface: BrowserSurface?) {
        if (surface != null && browserSurface === surface) browserSurface = null
        val wv = surface?.webView
        if (wv != null && webView === wv) webView = null
    }


    private fun loadInBrowser(url: String) {
        com.webtoapp.core.webview.WebViewManager.runWhenFreshSessionReady {
            browserSurface?.loadUrl(url) ?: webView?.loadUrl(url)
        }
    }

    private fun reloadBrowser() {
        browserSurface?.reload() ?: webView?.reload()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        try {
            val config = WebToAppApplication.shellMode.getConfig()
            val wv = webView
            if (wv != null && config?.webViewConfig != null) {
                com.webtoapp.core.webview.WebViewManager.refreshSystemDarkMode(
                    wv,
                    config.webViewConfig.followSystemDarkMode
                )
            }
        } catch (e: Exception) {
            com.webtoapp.core.shell.ShellLogger.w("ShellActivity", "onConfigurationChanged: refresh dark mode failed", e)
        }

        if (customView != null || immersiveFullscreenEnabled) {
            applyImmersiveFullscreen(true, isDarkTheme = isSystemInDarkMode())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        ShellActivityInit.initLogger(this)

        try {
            enableEdgeToEdge()
            com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "enableEdgeToEdge 成功")
        } catch (e: Exception) {
            AppLogger.w("ShellActivity", "enableEdgeToEdge failed", e)
            com.webtoapp.core.shell.ShellLogger.w("ShellActivity", "enableEdgeToEdge 失败", e)
        }

        super.onCreate(savedInstanceState)
        com.webtoapp.core.webview.DirectoryPickerRegistry.install(directoryPickerHost)

        if (!isTaskRoot &&
            intent?.action == Intent.ACTION_MAIN &&
            intent?.hasCategory(Intent.CATEGORY_LAUNCHER) == true
        ) {
            com.webtoapp.core.shell.ShellLogger.i(
                "ShellActivity",
                "Spurious launcher relaunch detected (task root exists), finishing to let system bring existing task forward"
            )
            finish()
            return
        }

        savedInstanceState?.let { webViewStateBundle = it }
        permissionDelegate.onRestoreInstanceState(savedInstanceState)

        if (WebToAppApplication.shellMode.requiresCustomPassword()) {
            showPasswordDialog()
            return
        }

        val config = WebToAppApplication.shellMode.getConfig()
        if (config == null) {
            AppLogger.e("ShellActivity", "配置加载失败，无法启动应用")
            com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "配置加载失败，无法启动应用")
            Toast.makeText(this, Strings.appConfigLoadFailed, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "配置加载成功: ${config.appName}")
        shellConfig = config
        clearBrowsingDataOnLaunch = config.webViewConfig.clearBrowsingDataOnLaunch
        if (clearBrowsingDataOnLaunch) {
            resetFreshBrowsingSession()
        }
        notificationPolyfillEnabled = config.webViewConfig.enableNotificationPolyfill
        backgroundMedia.enabled = config.webViewConfig.backgroundMediaPlayback
        com.webtoapp.core.engine.GeckoViewEngine.applyEnterpriseRootsEnabled(
            config.networkTrustConfig.trustUserCa
        )
        AppLogger.d("ShellActivity", "WebView UA config from shell: userAgentMode=${config.webViewConfig.userAgentMode}, customUserAgent=${config.webViewConfig.customUserAgent}, userAgent=${config.webViewConfig.userAgent}")

        try {
            val appLanguage = runCatching {
                com.webtoapp.core.i18n.AppLanguage.valueOf(config.language.uppercase())
            }.getOrDefault(com.webtoapp.core.i18n.AppLanguage.CHINESE)
            Strings.setLanguage(appLanguage)
            getSharedPreferences("wta_runtime_lang", MODE_PRIVATE)
                .edit().putString("app_language", appLanguage.name).apply()
            AppLogger.d("ShellActivity", "设置界面语言: ${config.language} -> $appLanguage")
        } catch (e: Exception) {
            AppLogger.e("ShellActivity", "设置语言失败", e)
        }

        ShellHardeningGuard.start(this, config.hardeningEnabled, config.hardeningThreatResponse)

        com.webtoapp.core.shell.ShellLogger.logFeature("Config", "加载配置", buildString {
            append("后台运行=${config.backgroundRunEnabled}, ")
            append("独立环境=${config.isolationEnabled}")
        })

        ShellActivityInit.initAutoStart(this, config)
        ShellActivityInit.initIsolation(this, config)
        ShellActivityInit.initBackgroundService(this, config)
        ShellActivityInit.initNotificationService(this, config)
        ShellActivityInit.setTaskDescription(this, config.appName)

        try {
            startupPermissions.requestConfiguredPermissions(
                floatingWindowHandlesOverlay = config.webViewConfig.floatingWindowConfig.enabled
            )
            com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "已声明权限请求完成")
        } catch (e: Exception) {
            com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "已声明权限请求失败", e)
        }

        com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "开始读取状态栏配置")
        statusBarColorMode = config.webViewConfig.statusBarColorMode
        statusBarCustomColor = config.webViewConfig.statusBarColor
        statusBarDarkIcons = config.webViewConfig.statusBarDarkIcons
        statusBarBackgroundType = config.webViewConfig.statusBarBackgroundType

        statusBarColorModeDark = config.webViewConfig.statusBarColorModeDark
        statusBarCustomColorDark = config.webViewConfig.statusBarColorDark
        statusBarDarkIconsDark = config.webViewConfig.statusBarDarkIconsDark
        statusBarBackgroundTypeDark = config.webViewConfig.statusBarBackgroundTypeDark
        showStatusBarInFullscreen = config.webViewConfig.showStatusBarInFullscreen
        hideStatusBarInVideoFullscreen = config.webViewConfig.hideStatusBarInVideoFullscreen
        showNavigationBarInFullscreen = config.webViewConfig.showNavigationBarInFullscreen

        keyboardAdjustMode = try {
            KeyboardAdjustMode.valueOf(config.webViewConfig.keyboardAdjustMode)
        } catch (e: Exception) {
            com.webtoapp.core.shell.ShellLogger.w("ShellActivity", "键盘调整模式解析失败: ${config.webViewConfig.keyboardAdjustMode}, 使用默认值 RESIZE")
            KeyboardAdjustMode.RESIZE
        }

        displayCutoutMode = try {
            DisplayCutoutMode.valueOf(config.webViewConfig.displayCutoutMode)
        } catch (e: Exception) {
            DisplayCutoutMode.SHORT_EDGES
        }

        shareReceiveMimeTypes = buildList {
            if (config.webViewConfig.receiveShareImages) {
                add(com.webtoapp.core.share.ShareReceiveContract.MIME_IMAGES)
            }
            if (config.webViewConfig.receiveShareText) {
                add(com.webtoapp.core.share.ShareReceiveContract.MIME_TEXT)
            }
        }
        shareDeliveryMode = try {
            com.webtoapp.data.model.ShareDeliveryMode.valueOf(config.webViewConfig.shareDeliveryMode)
        } catch (e: Exception) { com.webtoapp.data.model.ShareDeliveryMode.BOTH }
        shareShowReceivedToast = config.webViewConfig.shareShowReceivedToast
        openWithEnabled = config.webViewConfig.openWithEnabled

        if (ShellActivityInit.opensAsFloatingWindow(config)) {
            immersiveFullscreenEnabled = false
            val floatingWindowConfig = config.webViewConfig.floatingWindowConfig
            com.webtoapp.core.shell.ShellLogger.i(
                "ShellActivity",
                "悬浮窗优先于全屏模式: size=${floatingWindowConfig.windowSizePercent}%, opacity=${floatingWindowConfig.opacity}%"
            )
            if (FloatingWindowService.canDrawOverlays(this)) {
                launchFloatingWindowAndFinish(config)
                return
            }
            pendingFloatingWindowLaunch = true
            com.webtoapp.core.shell.ShellLogger.w("ShellActivity", "悬浮窗权限未授予，引导用户授权")
            Toast.makeText(this, Strings.floatingWindowPermissionRequired, Toast.LENGTH_LONG).show()
            FloatingWindowService.requestOverlayPermission(this)
        }

        immersiveFullscreenEnabled = ShellActivityInit.appliesFullscreenLaunch(config)
        try {
            applyImmersiveFullscreen(immersiveFullscreenEnabled)
            com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "沉浸式全屏模式: $immersiveFullscreenEnabled")
        } catch (e: Exception) {
            com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "应用沉浸式全屏失败", e)
        }

        applyScreenAwakeMode()

        val shellBrightness = config.webViewConfig.screenBrightness
        if (shellBrightness in 0..100) {
            val lp = window.attributes
            lp.screenBrightness = shellBrightness / 100f
            window.attributes = lp
            com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "屏幕亮度: ${shellBrightness}%")
        }

        if (!com.webtoapp.core.share.SharedContentInbox.isHistoryRelaunchIntent(intent)) {
            val intentUrl = intent?.data?.toString()
            if (isOpenWithCandidate(intent)) {
                acceptOpenWithIntent(intent)
            } else if (!intentUrl.isNullOrBlank() && intent?.action == Intent.ACTION_VIEW) {
                val validatedUrl = resolveShellDeepLinkUrl(intentUrl, config)
                deepLinkUrl.value = validatedUrl
                com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "收到 Deep Link: $validatedUrl (原始: $intentUrl)")
            }

            acceptShareIntent(intent)
        }

        com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "setContent 开始，主题=${config.themeType}")

        setContent {
            ShellTheme(
                themeTypeName = config.themeType,
                darkModeSetting = config.darkMode
            ) {

                ShellScreen(
                config = config,
                deepLinkUrl = deepLinkUrl.value,
                onBrowserSurfaceCreated = { surface ->
                    browserSurface = surface
                    if (surface.webView == null) {
                        webView = null
                        com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "Browser surface created (GeckoView), ECH engine active")
                        if (config.webViewConfig.enableMediaSession || config.webViewConfig.backgroundMediaPlayback) {
                            val geckoEngine = surface.engine as? com.webtoapp.core.engine.GeckoViewEngine
                            if (geckoEngine != null) {
                                geckoMediaAdapter?.release()
                                geckoMediaAdapter = com.webtoapp.core.engine.GeckoMediaSessionAdapter(
                                    this@ShellActivity,
                                    geckoEngine
                                )
                                backgroundMedia.bind(geckoMediaAdapter)
                                com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "[MediaSession] Gecko adapter attached")
                            }
                        }
                    }
                },
                onWebViewCreated = { wv ->
                    try {
                        webView = wv
                        if (browserSurface == null || browserSurface?.webView != null) {
                            browserSurface = BrowserSurface.fromWebView(wv)
                        }

                        wv.resumeTimers()
                        com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "WebView 创建成功, timers resumed")

                        val chooserRetained = com.webtoapp.core.webview.FileChooserLifetime.consumeReattached(wv)
                        val savedState = if (chooserRetained) null else webViewStateBundle
                        val savedSiteId = savedState?.getString(KEY_SAVED_SURFACE_SITE_ID)
                        val wvSiteId = (wv as? com.webtoapp.core.webview.WtaWebView)?.siteId
                        if (savedState != null && savedSiteId != null && savedSiteId != wvSiteId) {
                            com.webtoapp.core.shell.ShellLogger.i(
                                "ShellActivity",
                                "Saved WebView state belongs to site $savedSiteId, skipping site ${wvSiteId ?: "?"}"
                            )
                        } else if (savedState != null) {
                            val restored = wv.restoreState(savedState)
                            webViewStateBundle = null
                            if (restored != null) {
                                val restoredUrl = restored.currentItem?.url
                                val externalJumpUrl = resumeStore.consumeExternalJump(
                                    SHELL_RESUME_SESSION_KEY, config.targetUrl
                                )
                                if (com.webtoapp.core.webview.WebViewRestoreGuard
                                        .isUsableRestoredUrl(restoredUrl, externalJumpUrl)
                                ) {
                                    wv.tag = "state_restored"
                                    com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "WebView state restored from saved bundle")
                                } else {
                                    com.webtoapp.core.shell.ShellLogger.i(
                                        "ShellActivity",
                                        "Restored WebView entry is not resumable ($restoredUrl), loading start URL"
                                    )
                                }
                            }
                        }

                        translateBridge = TranslateBridge(wv, lifecycleScope)
                        wv.addJavascriptInterface(translateBridge!!, TranslateBridge.JS_INTERFACE_NAME)

                        val downloadLocationMode = try {
                            com.webtoapp.data.model.DownloadLocationMode.valueOf(config.webViewConfig.downloadLocationMode)
                        } catch (e: Exception) {
                            com.webtoapp.data.model.DownloadLocationMode.SYSTEM_DOWNLOAD
                        }
                        if (config.webViewConfig.downloadEnabled) {
                            val downloadBridge = com.webtoapp.core.webview.DownloadBridge(
                                this@ShellActivity,
                                lifecycleScope,
                                downloadLocationMode,
                                config.webViewConfig.customDownloadDirUri,
                                config.webViewConfig.mediaAlbumName,
                                currentPageUrl = { wv.url }
                            )
                            wv.addJavascriptInterface(downloadBridge, com.webtoapp.core.webview.DownloadBridge.JS_INTERFACE_NAME)
                        }

                        if (config.webViewConfig.enablePrintBridge) {
                            val printBridge = com.webtoapp.core.webview.PrintBridge(
                                context = this@ShellActivity,
                                scope = lifecycleScope,
                                webViewProvider = { wv }
                            )
                            wv.addJavascriptInterface(printBridge, com.webtoapp.core.webview.PrintBridge.JS_INTERFACE_NAME)
                            try {
                                androidx.webkit.WebViewCompat.addDocumentStartJavaScript(
                                    wv,
                                    com.webtoapp.core.webview.PrintBridge.getInjectionScript(),
                                    setOf("*")
                                )
                                com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "[PrintBridge] Installed at document start (applies to all hosts)")
                            } catch (e: Exception) {
                                wv.evaluateJavascript(com.webtoapp.core.webview.PrintBridge.getInjectionScript(), null)
                                com.webtoapp.core.shell.ShellLogger.w("ShellActivity", "[PrintBridge] Document-start unsupported, used evaluateJavascript fallback", e)
                            }
                        }

                        if (config.webViewConfig.enableMediaSession || config.webViewConfig.backgroundMediaPlayback) {
                            val mediaBridge = com.webtoapp.core.webview.MediaSessionBridge(
                                this@ShellActivity,
                                wv
                            )
                            if (mediaBridge.install()) {
                                com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "[MediaSession] Installed at document start")
                            } else {
                                com.webtoapp.core.shell.ShellLogger.w("ShellActivity", "[MediaSession] Document-start unsupported, will re-inject on page finish")
                            }
                            mediaSessionBridge = mediaBridge
                            backgroundMedia.bind(mediaBridge)
                        }

                        if (config.webViewConfig.enableNativeBridge || config.webViewConfig.enableNotificationPolyfill) {
                            val capabilities = if (!config.webViewConfig.enableNativeBridge) {
                                com.webtoapp.data.model.notificationPolyfillOnlyCapabilities()
                            } else com.webtoapp.data.model.NativeBridgeCapabilities(
                                clipboard = config.webViewConfig.nativeBridgeClipboard,
                                vibration = config.webViewConfig.nativeBridgeVibration,
                                geolocation = config.webViewConfig.nativeBridgeGeolocation,
                                brightness = config.webViewConfig.nativeBridgeBrightness,
                                notification = config.webViewConfig.nativeBridgeNotification,
                                notificationScheduled = config.webViewConfig.nativeBridgeNotificationScheduled,
                                notificationPersistent = config.webViewConfig.nativeBridgeNotificationPersistent,
                                download = config.webViewConfig.nativeBridgeDownload,
                                privateNetwork = config.webViewConfig.nativeBridgePrivateNetwork,
                                screenWake = config.webViewConfig.nativeBridgeScreenWake,
                                openExternal = config.webViewConfig.nativeBridgeOpenExternal,
                                deviceInfo = config.webViewConfig.nativeBridgeDeviceInfo,
                                securityInfo = config.webViewConfig.nativeBridgeSecurityInfo,
                                networkInfo = config.webViewConfig.nativeBridgeNetworkInfo,
                                toast = config.webViewConfig.nativeBridgeToast,
                                logging = config.webViewConfig.nativeBridgeLogging,
                                findInPage = config.webViewConfig.nativeBridgeFindInPage,
                                orientation = config.webViewConfig.nativeBridgeOrientation,
                                fullscreen = config.webViewConfig.nativeBridgeFullscreen,
                                print = config.webViewConfig.nativeBridgePrint,
                                googleSignIn = config.webViewConfig.nativeBridgeGoogleSignIn,
                                googleSignInClientId = config.webViewConfig.nativeBridgeGoogleSignInClientId,
                                directory = config.webViewConfig.nativeBridgeDirectory,
                            ).withNotificationPolyfill(config.webViewConfig.enableNotificationPolyfill)
                            val nativeBridge = com.webtoapp.core.webview.NativeBridge(
                                context = this@ShellActivity,
                                scope = lifecycleScope,
                                webViewProvider = { wv },
                                capabilities = capabilities,
                                corsBypass = config.webViewConfig.enableCorsBypass,
                                downloadLocationMode = downloadLocationMode,
                                customDownloadDirUri = config.webViewConfig.customDownloadDirUri,
                                appOriginUrl = config.targetUrl,
                                directoryGrantKey = config.packageName
                            )
                            this@ShellActivity.nativeBridge = nativeBridge
                            wv.addJavascriptInterface(nativeBridge, com.webtoapp.core.webview.NativeBridge.JS_INTERFACE_NAME)
                            nativeBridge.installDirectoryHelper(wv)
                        } else if (config.webViewConfig.enablePrivateNetworkBridge || config.webViewConfig.enableCorsBypass) {
                            val privateNetworkBridge = com.webtoapp.core.webview.PrivateNetworkNativeBridgeAdapter(
                                context = this@ShellActivity,
                                scope = lifecycleScope,
                                webViewProvider = { wv },
                                corsBypass = config.webViewConfig.enableCorsBypass,
                                appOriginUrl = config.targetUrl
                            )
                            wv.addJavascriptInterface(privateNetworkBridge, com.webtoapp.core.webview.NativeBridge.JS_INTERFACE_NAME)
                        } else {
                            wv.removeJavascriptInterface(com.webtoapp.core.webview.NativeBridge.JS_INTERFACE_NAME)
                        }
                        com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "JS 桥接接口注册完成")
                    } catch (e: Exception) {
                        com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "WebView 初始化失败", e)
                    }
                },
                onStatusBarAutoColorChanged = { color ->
                    if (statusBarAutoColor == color) return@ShellScreen
                    statusBarAutoColor = color
                    refreshStatusBarAppearance()
                },
                onPageBottomColorChanged = { color ->
                    if (pageBottomAutoColor == color) return@ShellScreen
                    pageBottomAutoColor = color
                    refreshStatusBarAppearance()
                },
                onStatusBarAppearanceChanged = { appearance ->
                    if (statusBarAppearance != appearance) {
                        statusBarAppearance = appearance
                        refreshStatusBarAppearance()
                    }
                },
                onFileChooser = { callback, params ->
                    (webView ?: browserSurface?.webView)?.let {
                        com.webtoapp.core.webview.FileChooserLifetime.parkWebView(it)
                    }
                    permissionDelegate.handleFileChooser(callback, params)
                },
                onShowCustomView = { view, callback ->
                    customView = view
                    customViewCallback = callback
                    showCustomView(view)
                },
                onHideCustomView = {
                    hideCustomView()
                },
                onFullscreenModeChanged = { enabled ->
                    immersiveFullscreenEnabled = enabled
                    if (customView == null) {
                        applyImmersiveFullscreen(enabled)
                    }
                }
                )
            }
        }

        onBackPressedDispatcher.addCallback(this, ShellActivityInit.createBackPressedCallback(
            activity = this,
            getCustomView = { customView },
            getWebView = { webView },
            getBrowserSurface = { browserSurface },
            hideCustomView = ::hideCustomView,
            getShellConfig = { shellConfig }
        ))
    }

    private fun showCustomView(view: View) {

        val orientationStrategy = run {
            val raw = WebToAppApplication.shellMode.getConfig()?.webViewConfig?.fullscreenVideoOrientation
            try {
                if (!raw.isNullOrBlank()) com.webtoapp.data.model.FullscreenVideoOrientation.valueOf(raw)
                else com.webtoapp.data.model.FullscreenVideoOrientation.AUTO_SENSOR_LANDSCAPE
            } catch (_: Exception) {
                com.webtoapp.data.model.FullscreenVideoOrientation.AUTO_SENSOR_LANDSCAPE
            }
        }
        originalOrientationBeforeFullscreen = WindowHelper.showCustomView(this, view)
        WindowHelper.applyFullscreenVideoOrientation(this, webView, orientationStrategy)
        applyImmersiveFullscreen(true)
    }

    private fun hideCustomView() {
        customView?.let { view ->
            WindowHelper.hideCustomView(this, view, customViewCallback, originalOrientationBeforeFullscreen)
            customView = null
            customViewCallback = null
            originalOrientationBeforeFullscreen = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            applyImmersiveFullscreen(immersiveFullscreenEnabled)
        }
    }

    private fun launchFloatingWindowAndFinish(config: com.webtoapp.core.shell.ShellConfig) {
        val floatingWindowConfig = config.webViewConfig.floatingWindowConfig
        val fwConfig = com.webtoapp.data.model.FloatingWindowConfig(
            enabled = true,
            windowSizePercent = floatingWindowConfig.windowSizePercent,
            widthPercent = floatingWindowConfig.widthPercent,
            heightPercent = floatingWindowConfig.heightPercent,
            lockAspectRatio = floatingWindowConfig.lockAspectRatio,
            aspectRatioMode = try {
                com.webtoapp.data.model.FloatingWindowAspectRatioMode.valueOf(floatingWindowConfig.aspectRatioMode)
            } catch (e: Exception) {
                if (floatingWindowConfig.lockAspectRatio) {
                    com.webtoapp.data.model.FloatingWindowAspectRatioMode.SCREEN
                } else {
                    com.webtoapp.data.model.FloatingWindowAspectRatioMode.FREE
                }
            },
            customAspectRatioWidth = floatingWindowConfig.customAspectRatioWidth,
            customAspectRatioHeight = floatingWindowConfig.customAspectRatioHeight,
            opacity = floatingWindowConfig.opacity,
            cornerRadius = floatingWindowConfig.cornerRadius,
            borderStyle = try {
                com.webtoapp.data.model.FloatingBorderStyle.valueOf(floatingWindowConfig.borderStyle)
            } catch (e: Exception) {
                com.webtoapp.data.model.FloatingBorderStyle.SUBTLE
            },
            minimizedIconPath = floatingWindowConfig.minimizedIconPath,
            minimizedIconSizePercent = floatingWindowConfig.minimizedIconSizePercent,
            minimizedIconEdgeDocking = floatingWindowConfig.minimizedIconEdgeDocking,
            showTitleBar = floatingWindowConfig.showTitleBar,
            autoHideTitleBar = floatingWindowConfig.autoHideTitleBar,
            startMinimized = floatingWindowConfig.startMinimized,
            rememberPosition = floatingWindowConfig.rememberPosition,
            edgeSnapping = floatingWindowConfig.edgeSnapping,
            showResizeHandle = floatingWindowConfig.showResizeHandle,
            lockPosition = floatingWindowConfig.lockPosition
        )
        val intent = Intent(this, FloatingWindowService::class.java).apply {
            putExtra(FloatingWindowService.EXTRA_ACTION, FloatingWindowService.ACTION_SHOW)
            putExtra(FloatingWindowService.EXTRA_CONFIG, com.webtoapp.util.GsonProvider.gson.toJson(fwConfig))
            putExtra(FloatingWindowService.EXTRA_URL, config.targetUrl)
            putExtra(FloatingWindowService.EXTRA_APP_NAME, config.appName)
            putExtra(FloatingWindowService.EXTRA_TRANSLATE_ENABLED, config.translateEnabled)
            putExtra(FloatingWindowService.EXTRA_TRANSLATE_TARGET_LANGUAGE, config.translateTargetLanguage)
            putExtra(FloatingWindowService.EXTRA_TRANSLATE_SHOW_BUTTON, config.translateShowButton)
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            com.webtoapp.core.shell.ShellLogger.w("ShellActivity", "悬浮窗服务启动被拒: ${e.message}")
        }
        com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "悬浮窗服务已启动，关闭主 Activity")
        finish()
    }

    override fun onUserLeaveHint() {
        backgroundMedia.onFocusLost(!isFinishing && !isChangingConfigurations)
        super.onUserLeaveHint()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            backgroundMedia.onFocusGained()
        } else {
            backgroundMedia.onFocusLost(!isFinishing && !isChangingConfigurations)
        }
        if (hasFocus) {
            if (customView != null || immersiveFullscreenEnabled) {
                applyImmersiveFullscreen(true, isDarkTheme = isSystemInDarkMode())
            } else {
                refreshStatusBarAppearance()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        browserSurface?.saveState(outState) ?: webView?.saveState(outState)
        outState.putString(
            KEY_SAVED_SURFACE_SITE_ID,
            ((browserSurface?.webView ?: webView) as? com.webtoapp.core.webview.WtaWebView)?.siteId
        )
        permissionDelegate.onSaveInstanceState(outState)
        com.webtoapp.core.shell.ShellLogger.logLifecycle("ShellActivity", "onSaveInstanceState - WebView state saved")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (com.webtoapp.core.share.SharedContentInbox.isHistoryRelaunchIntent(intent)) return

        val launcherRelaunch = intent?.action == Intent.ACTION_MAIN &&
            intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        if (clearBrowsingDataOnLaunch && launcherRelaunch) {
            resetFreshBrowsingSession()
            com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "Fresh session reset after launcher relaunch")
            recreate()
            return
        }

        val notificationClickUrl = intent?.getStringExtra("notification_click_url")
        if (!notificationClickUrl.isNullOrBlank()) {
            if (clearBrowsingDataOnLaunch) {
                resetFreshBrowsingSession()
            }
            val baseUrl = WebToAppApplication.shellMode.getConfig()?.targetUrl ?: ""
            val fullUrl = if (notificationClickUrl.startsWith("http://") || notificationClickUrl.startsWith("https://")) notificationClickUrl
                          else baseUrl.trimEnd('/') + "/" + notificationClickUrl.trimStart('/')

            if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
                com.webtoapp.core.shell.ShellLogger.w("ShellActivity", "Notification click URL rejected (unsafe scheme): $fullUrl")
                return
            }
            val safeUrl = normalizeShellTargetUrlForSecurity(fullUrl)
            loadInBrowser(safeUrl)
            com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "Notification click URL: $safeUrl")
            return
        }

        val url = intent?.data?.toString()
        if (isOpenWithCandidate(intent)) {
            acceptOpenWithIntent(intent)
        } else if (!url.isNullOrBlank() && intent?.action == Intent.ACTION_VIEW) {
            if (clearBrowsingDataOnLaunch) {
                resetFreshBrowsingSession()
            }
            val config = WebToAppApplication.shellMode.getConfig()
            val validatedUrl = if (config != null) resolveShellDeepLinkUrl(url, config) else normalizeShellTargetUrlForSecurity(url)
            if (!validatedUrl.startsWith("http://") && !validatedUrl.startsWith("https://")) return
            deepLinkUrl.value = validatedUrl

            loadInBrowser(validatedUrl)
            com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "onNewIntent Deep Link: $validatedUrl (原始: $url)")
        }

        acceptShareIntent(intent)
    }

    private fun acceptShareIntent(intent: Intent?) {
        if (shareReceiveMimeTypes.isEmpty() || intent == null) return

        lifecycleScope.launch {
            try {
                val accepted = com.webtoapp.core.share.SharedContentInbox.acceptIntent(
                    this@ShellActivity,
                    intent,
                    shareReceiveMimeTypes
                )
                if (accepted.isEmpty()) return@launch

                setIntent(Intent(Intent.ACTION_MAIN))

                com.webtoapp.core.shell.ShellLogger.i(
                    "ShellActivity",
                    "收到分享内容: ${accepted.map { "${it.name}(${it.mimeType}, ${it.size}B)" }}"
                )

                if (accepted.any { !it.isText }) {
                    maybeShowShareReceivedToast()
                }

                deliverPendingShares()
            } catch (e: Exception) {
                com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "接收分享内容失败: ${e.message}", e)
            }
        }
    }

    private fun isOpenWithCandidate(intent: Intent?): Boolean {
        if (!openWithEnabled || intent?.action != Intent.ACTION_VIEW) return false
        val scheme = intent.data?.scheme?.lowercase()
        return scheme == "content" || scheme == "file"
    }

    private fun acceptOpenWithIntent(intent: Intent?) {
        lifecycleScope.launch {
            try {
                val item = com.webtoapp.core.share.SharedContentInbox.acceptViewIntent(
                    this@ShellActivity,
                    intent
                ) ?: return@launch

                setIntent(Intent(Intent.ACTION_MAIN))

                com.webtoapp.core.shell.ShellLogger.i(
                    "ShellActivity",
                    "收到打开方式文件: ${item.name}(${item.mimeType}, ${item.size}B)"
                )

                maybeShowShareReceivedToast()

                deliverPendingShares()
            } catch (e: Exception) {
                com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "接收打开方式文件失败: ${e.message}", e)
            }
        }
    }

    private fun maybeShowShareReceivedToast() {
        if (!shareShowReceivedToast) return
        if (shareDeliveryMode == com.webtoapp.data.model.ShareDeliveryMode.JS_EVENT) return
        Toast.makeText(
            this,
            Strings.shareReceivedToast,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun onShellPageStarted() {
        sharePageReady = false
    }

    fun onShellPageReady() {
        sharePageReady = true
        deliverPendingShares()
    }

    fun deliverPendingShares() {
        if (shareReceiveMimeTypes.isEmpty() && !openWithEnabled) return
        if (shareDeliveryMode == com.webtoapp.data.model.ShareDeliveryMode.FILE_CHOOSER_PREFILL) return

        val target = webView ?: browserSurface?.webView ?: return
        if (!sharePageReady) return

        lifecycleScope.launch {
            try {
                val items = com.webtoapp.core.share.SharedContentInbox.pending(this@ShellActivity)
                if (items.isEmpty()) return@launch

                val payload = com.webtoapp.core.share.buildShareBatch(items)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    target.evaluateJavascript(
                        "window.__WTA_SHARE_PUSH__ && window.__WTA_SHARE_PUSH__($payload)",
                        null
                    )
                }
                com.webtoapp.core.shell.ShellLogger.i(
                    "ShellActivity",
                    "已向页面投递 ${items.size} 条分享内容"
                )
            } catch (e: Exception) {
                com.webtoapp.core.shell.ShellLogger.w("ShellActivity", "分享内容投递失败: ${e.message}", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        backgroundMedia.onHostResume()

        if (pendingFloatingWindowLaunch) {
            val config = WebToAppApplication.shellMode.getConfig()
            if (config != null && FloatingWindowService.canDrawOverlays(this)) {
                pendingFloatingWindowLaunch = false
                launchFloatingWindowAndFinish(config)
                return
            }

        }

        browserSurface?.onResume() ?: webView?.onResume()

        browserSurface?.resumeTimers() ?: webView?.resumeTimers()
        nativeBridge?.onWebViewResume()
        dispatchVisibilityEvent(visible = true)
        com.webtoapp.core.shell.ShellLogger.logLifecycle("ShellActivity", "onResume - WebView resumed, timers resumed")
    }

    override fun onPause() {
        val keepMedia = backgroundMedia.onHostPause(!isFinishing && !isChangingConfigurations)
        super.onPause()

        if (!keepMedia) {
            dispatchVisibilityEvent(visible = false)
        }

        if (keepMedia) {
            com.webtoapp.core.shell.ShellLogger.logLifecycle("ShellActivity", "onPause - background media playing, leaving the page running")
        } else if (notificationPolyfillEnabled && browserSurface == null) {
            com.webtoapp.core.shell.ShellLogger.logLifecycle("ShellActivity", "onPause - notification polyfill active, keeping JS timers alive")
        } else {
            browserSurface?.onPause() ?: webView?.onPause()
            nativeBridge?.onWebViewPause()
        }

        android.webkit.CookieManager.getInstance().flush()
        com.webtoapp.core.shell.ShellLogger.logLifecycle(
            "ShellActivity",
            if (keepMedia) "onPause - media kept alive, cookies flushed" else "onPause - WebView paused, cookies flushed"
        )
    }

    private fun dispatchVisibilityEvent(visible: Boolean) {
        val js = if (visible) {
            """(function(){try{Object.defineProperty(document,'visibilityState',{get:function(){return 'visible';},configurable:true});Object.defineProperty(document,'hidden',{get:function(){return false;},configurable:true});document.dispatchEvent(new Event('visibilitychange'));}catch(e){}})();"""
        } else {
            """(function(){try{Object.defineProperty(document,'visibilityState',{get:function(){return 'hidden';},configurable:true});Object.defineProperty(document,'hidden',{get:function(){return true;},configurable:true});document.dispatchEvent(new Event('visibilitychange'));}catch(e){}})();"""
        }
        try {
            webView?.evaluateJavascript(js, null)
        } catch (e: Exception) {
            AppLogger.w("ShellActivity", "Failed to dispatch visibility event", e)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            com.webtoapp.core.shell.ShellLogger.logLifecycle("ShellActivity", "Memory pressure (level=$level)")
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()

        com.webtoapp.core.shell.ShellLogger.logLifecycle("ShellActivity", "Low memory, skipped manual GC")
    }

    override fun onDestroy() {
        backgroundMedia.close()
        directoryPickerHost.cancelPending()
        com.webtoapp.core.webview.DirectoryPickerRegistry.uninstall(directoryPickerHost)
        com.webtoapp.core.shell.ShellLogger.logLifecycle("ShellActivity", "onDestroy")

        android.webkit.CookieManager.getInstance().flush()

        webView?.let { wv ->
            wv.stopLoading()

            wv.onPause()
            wv.webChromeClient = null

            (wv.parent as? ViewGroup)?.removeView(wv)
            wv.removeAllViews()
            wv.destroy()
        }
        webView = null
        mediaSessionBridge?.runCatching { release() }
        mediaSessionBridge = null
        geckoMediaAdapter?.runCatching { release() }
        geckoMediaAdapter = null
        super.onDestroy()
    }

    private fun showPasswordDialog() {
        val editText = android.widget.EditText(this).apply {
            hint = Strings.enterEncryptionPassword
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(Strings.passwordVerification)
            .setMessage(Strings.appEncryptedMessage)
            .setView(editText)
            .setPositiveButton(Strings.btnConfirm) { _, _ ->
                val password = editText.text.toString()
                if (password.isNotBlank()) {
                    WebToAppApplication.shellMode.setCustomPassword(password)

                    val config = WebToAppApplication.shellMode.getConfig()
                    if (config == null) {
                        Toast.makeText(this, Strings.wrongPasswordCannotDecrypt, Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        recreate()
                    }
                } else {
                    Toast.makeText(this, Strings.passwordCannotBeEmpty, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton(Strings.btnExit) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }
}
