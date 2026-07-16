package com.webtoapp.core.floatingwindow

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.webkit.WebView
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellServerLauncher
import com.webtoapp.core.webview.DownloadBridge
import com.webtoapp.core.webview.TranslateBridge
import com.webtoapp.data.model.FloatingWindowConfig
import com.webtoapp.ui.shell.injectTranslateScript
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FloatingWindowService : Service() {

    companion object {
        private const val TAG = "FloatingWindowService"
        private const val NOTIFICATION_CHANNEL_ID = "floating_window_channel"
        private const val NOTIFICATION_ID = 2024

        const val EXTRA_CONFIG = "extra_floating_window_config"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_ACTION = "extra_action"
        const val EXTRA_TRANSLATE_ENABLED = "extra_translate_enabled"
        const val EXTRA_TRANSLATE_TARGET_LANGUAGE = "extra_translate_target_language"
        const val EXTRA_TRANSLATE_SHOW_BUTTON = "extra_translate_show_button"

        const val ACTION_SHOW = "action_show"
        const val ACTION_DISMISS = "action_dismiss"
        const val ACTION_MINIMIZE = "action_minimize"
        const val ACTION_RESTORE = "action_restore"

        @Volatile
        private var instance: FloatingWindowService? = null

        fun getInstance(): FloatingWindowService? = instance

        fun canDrawOverlays(context: android.content.Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        fun requestOverlayPermission(context: android.content.Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}")
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

    private lateinit var floatingWindowManager: FloatingWindowManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    @Volatile
    private var serverStopper: ShellServerLauncher.RuntimeStopper? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        floatingWindowManager = FloatingWindowManager(this)
        createNotificationChannel()
        AppLogger.i(TAG, "FloatingWindowService 已创建")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra(EXTRA_ACTION) ?: ACTION_SHOW

        when (action) {
            ACTION_SHOW -> {
                val configJson = intent?.getStringExtra(EXTRA_CONFIG)
                val url = intent?.getStringExtra(EXTRA_URL) ?: ""
                val appName = intent?.getStringExtra(EXTRA_APP_NAME) ?: ""
                val translateEnabled = intent?.getBooleanExtra(EXTRA_TRANSLATE_ENABLED, false) == true
                val translateTargetLanguage = intent?.getStringExtra(EXTRA_TRANSLATE_TARGET_LANGUAGE) ?: "zh-CN"
                val translateShowButton = intent?.getBooleanExtra(EXTRA_TRANSLATE_SHOW_BUTTON, true) != false

                val config = if (configJson != null) {
                    try {
                        com.webtoapp.util.GsonProvider.gson.fromJson(
                            configJson, FloatingWindowConfig::class.java
                        )
                    } catch (e: Exception) {
                        AppLogger.e(TAG, "解析悬浮窗配置失败", e)
                        FloatingWindowConfig(enabled = true)
                    }
                } else {
                    FloatingWindowConfig(enabled = true)
                }

                startForeground(NOTIFICATION_ID, createNotification(appName))

                val translateBridgeFactory: ((WebView) -> Unit)? = if (translateEnabled) {
                    { webView ->
                        val translateBridge = TranslateBridge(webView, serviceScope)
                        webView.addJavascriptInterface(translateBridge, TranslateBridge.JS_INTERFACE_NAME)
                    }
                } else {
                    null
                }

                val shellConfig = try {
                    com.webtoapp.core.shell.ShellModeManager(this).getConfig()
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Shell config unavailable, falling back to raw URL", e)
                    null
                }

                val downloadLocationMode = try {
                    com.webtoapp.data.model.DownloadLocationMode.valueOf(
                        shellConfig?.webViewConfig?.downloadLocationMode ?: "SYSTEM_DOWNLOAD"
                    )
                } catch (e: Exception) {
                    com.webtoapp.data.model.DownloadLocationMode.SYSTEM_DOWNLOAD
                }
                val customDownloadDirUri = shellConfig?.webViewConfig?.customDownloadDirUri ?: ""
                val downloadBridgeFactory: (WebView) -> Unit = { webView ->
                    val downloadBridge = DownloadBridge(this, serviceScope, downloadLocationMode, customDownloadDirUri)
                    webView.addJavascriptInterface(downloadBridge, DownloadBridge.JS_INTERFACE_NAME)
                }

                val webViewConfigurator = shellConfig?.let { sc ->
                    buildWebViewManagerConfigurator(
                        shellConfig = sc,
                        translateBridgeFactory = translateBridgeFactory,
                        downloadBridgeFactory = downloadBridgeFactory,
                        translateEnabled = translateEnabled,
                        translateTargetLanguage = translateTargetLanguage,
                        translateShowButton = translateShowButton
                    )
                }

                floatingWindowManager.onDismiss = {
                    stopSelf()
                }
                if (webViewConfigurator != null) {

                    floatingWindowManager.onWebViewConfigure = webViewConfigurator

                    floatingWindowManager.onWebViewCreated = null

                    floatingWindowManager.onWebViewPageFinished = null
                } else {

                    floatingWindowManager.onWebViewConfigure = null
                    floatingWindowManager.onWebViewCreated = { webView ->
                        translateBridgeFactory?.invoke(webView)
                        downloadBridgeFactory(webView)
                    }
                    floatingWindowManager.onWebViewPageFinished = { webView, _ ->
                        if (translateEnabled) {
                            injectTranslateScript(webView, translateTargetLanguage, translateShowButton)
                        }
                        webView.evaluateJavascript(DownloadBridge.getInjectionScript(), null)
                    }
                }

                if (shellConfig != null && needsServerStartup(shellConfig.appType)) {
                    AppLogger.i(TAG, "悬浮窗启动服务端运行时: appType=${shellConfig.appType}, sentinelUrl=$url")
                    floatingWindowManager.show(config, appName, "about:blank")
                    serviceScope.launch {
                        val result = ShellServerLauncher.resolveServerBackedTargetUrl(
                            this@FloatingWindowService,
                            shellConfig
                        )
                        serverStopper = result.stopper
                        if (result.error != null) {
                            AppLogger.e(TAG, "服务端运行时启动失败: ${result.error}")
                        }
                        floatingWindowManager.getWebView()?.loadUrl(result.resolvedUrl)
                        AppLogger.i(TAG, "悬浮窗加载真实 URL: ${result.resolvedUrl}")
                    }
                } else {
                    floatingWindowManager.show(config, appName, url)
                }

                AppLogger.i(TAG, "悬浮窗已启动: url=$url, size=${config.windowSizePercent}%, opacity=${config.opacity}%")
            }

            ACTION_DISMISS -> {
                floatingWindowManager.dismiss()
                stopSelf()
            }

            ACTION_MINIMIZE -> {
                floatingWindowManager.minimize()
            }

            ACTION_RESTORE -> {
                floatingWindowManager.restore()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        runCatching { serverStopper?.stop() }
            .onFailure { AppLogger.w(TAG, "服务端运行时停止失败", it) }
        serverStopper = null
        floatingWindowManager.dismiss()
        serviceScope.cancel()
        instance = null
        AppLogger.i(TAG, "FloatingWindowService 已销毁")
        super.onDestroy()
    }

    private fun needsServerStartup(appType: String): Boolean = when (appType.uppercase()) {
        "PHP_APP", "PYTHON_APP", "GO_APP", "NODEJS_APP", "WORDPRESS" -> true
        else -> false
    }

    private fun buildWebViewManagerConfigurator(
        shellConfig: com.webtoapp.core.shell.ShellConfig,
        translateBridgeFactory: ((WebView) -> Unit)?,
        downloadBridgeFactory: (WebView) -> Unit,
        translateEnabled: Boolean,
        translateTargetLanguage: String,
        translateShowButton: Boolean
    ): (WebView) -> Unit {
        val webViewConfig = com.webtoapp.ui.shell.buildWebViewConfig(shellConfig)
        val downloadLocationMode = try {
            com.webtoapp.data.model.DownloadLocationMode.valueOf(
                shellConfig.webViewConfig.downloadLocationMode
            )
        } catch (e: Exception) {
            com.webtoapp.data.model.DownloadLocationMode.SYSTEM_DOWNLOAD
        }
        val customDownloadDirUri = shellConfig.webViewConfig.customDownloadDirUri
        val adBlocker = try {
            com.webtoapp.WebToAppApplication.adBlock
        } catch (e: Exception) {
            AppLogger.w(TAG, "AdBlocker unavailable, falling back to default", e)
            com.webtoapp.core.adblock.AdBlocker()
        }
        val webViewManager = com.webtoapp.core.webview.WebViewManager(this, adBlocker)

        val callbacks = object : com.webtoapp.core.webview.WebViewCallbacks {
            override fun onPageStarted(url: String?) {

                floatingWindowManager.getWebView()?.let { webView ->
                    translateBridgeFactory?.invoke(webView)
                    downloadBridgeFactory(webView)

                    if (shellConfig.webViewConfig.enableNativeBridge) {
                        val capabilities = com.webtoapp.data.model.NativeBridgeCapabilities(
                            clipboard = shellConfig.webViewConfig.nativeBridgeClipboard,
                            vibration = shellConfig.webViewConfig.nativeBridgeVibration,
                            geolocation = shellConfig.webViewConfig.nativeBridgeGeolocation,
                            brightness = shellConfig.webViewConfig.nativeBridgeBrightness,
                            notification = shellConfig.webViewConfig.nativeBridgeNotification,
                            notificationScheduled = shellConfig.webViewConfig.nativeBridgeNotificationScheduled,
                            notificationPersistent = shellConfig.webViewConfig.nativeBridgeNotificationPersistent,
                            download = shellConfig.webViewConfig.nativeBridgeDownload,
                            privateNetwork = shellConfig.webViewConfig.nativeBridgePrivateNetwork,
                            screenWake = shellConfig.webViewConfig.nativeBridgeScreenWake,
                            openExternal = shellConfig.webViewConfig.nativeBridgeOpenExternal,
                            deviceInfo = shellConfig.webViewConfig.nativeBridgeDeviceInfo,
                            securityInfo = shellConfig.webViewConfig.nativeBridgeSecurityInfo,
                            networkInfo = shellConfig.webViewConfig.nativeBridgeNetworkInfo,
                            toast = shellConfig.webViewConfig.nativeBridgeToast,
                            logging = shellConfig.webViewConfig.nativeBridgeLogging,
                            findInPage = shellConfig.webViewConfig.nativeBridgeFindInPage,
                            orientation = shellConfig.webViewConfig.nativeBridgeOrientation,
                            fullscreen = shellConfig.webViewConfig.nativeBridgeFullscreen,
                        )
                        val nativeBridge = com.webtoapp.core.webview.NativeBridge(
                            context = this@FloatingWindowService,
                            scope = serviceScope,
                            webViewProvider = { webView },
                            capabilities = capabilities,
                            corsBypass = shellConfig.webViewConfig.enableCorsBypass,
                            downloadLocationMode = downloadLocationMode,
                            customDownloadDirUri = customDownloadDirUri
                        )
                        webView.addJavascriptInterface(
                            nativeBridge,
                            com.webtoapp.core.webview.NativeBridge.JS_INTERFACE_NAME
                        )
                    } else if (shellConfig.webViewConfig.enablePrivateNetworkBridge || shellConfig.webViewConfig.enableCorsBypass) {
                        val privateNetworkBridge = com.webtoapp.core.webview.PrivateNetworkNativeBridgeAdapter(
                            context = this@FloatingWindowService,
                            scope = serviceScope,
                            webViewProvider = { webView },
                            corsBypass = shellConfig.webViewConfig.enableCorsBypass
                        )
                        webView.addJavascriptInterface(
                            privateNetworkBridge,
                            com.webtoapp.core.webview.NativeBridge.JS_INTERFACE_NAME
                        )
                    } else {
                        webView.removeJavascriptInterface(
                            com.webtoapp.core.webview.NativeBridge.JS_INTERFACE_NAME
                        )
                    }
                }
            }

            override fun onPageFinished(url: String?) {
                val webView = floatingWindowManager.getWebView() ?: return
                if (translateEnabled) {
                    injectTranslateScript(webView, translateTargetLanguage, translateShowButton)
                }
                webView.evaluateJavascript(DownloadBridge.getInjectionScript(), null)

                floatingWindowManager.reinjectImeFocusTracker()
                floatingWindowManager.notifyNavigationStateChanged()
            }

            override fun onProgressChanged(progress: Int) {  }
            override fun onTitleChanged(title: String?) {  }
            override fun onIconReceived(icon: android.graphics.Bitmap?) {  }

            override fun onError(errorCode: Int, description: String) {
                AppLogger.w(TAG, "悬浮窗页面加载错误: code=$errorCode, desc=$description")
            }

            override fun onSslError(error: String) {
                AppLogger.w(TAG, "悬浮窗 SSL 错误: $error")
            }

            override fun onExternalLink(url: String) {

                floatingWindowManager.getWebView()?.loadUrl(url)
            }

            override fun onShowCustomView(
                view: android.view.View?,
                callback: android.webkit.WebChromeClient.CustomViewCallback?
            ) {

                callback?.onCustomViewHidden()
            }

            override fun onHideCustomView() {}

            override fun onGeolocationPermission(
                origin: String?,
                callback: android.webkit.GeolocationPermissions.Callback?
            ) {

                callback?.invoke(origin, false, false)
            }

            override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {

                request?.deny()
            }

            override fun onShowFileChooser(
                filePathCallback: android.webkit.ValueCallback<Array<android.net.Uri>>?,
                fileChooserParams: android.webkit.WebChromeClient.FileChooserParams?
            ): Boolean {

                filePathCallback?.onReceiveValue(null)
                return false
            }

            override fun onDownloadStart(
                url: String,
                userAgent: String,
                contentDisposition: String,
                mimeType: String,
                contentLength: Long
            ) {
                AppLogger.d(TAG, "悬浮窗下载请求: url=$url, mime=$mimeType")
                com.webtoapp.util.DownloadHelper.handleDownload(
                    context = this@FloatingWindowService,
                    url = url,
                    userAgent = userAgent,
                    contentDisposition = contentDisposition,
                    mimeType = mimeType,
                    contentLength = contentLength,
                    scope = serviceScope,
                    onBlobDownload = { blobUrl, filename ->
                        floatingWindowManager.getWebView()?.evaluateJavascript(
                            buildBlobDownloadJs(blobUrl, filename), null
                        )
                    },
                    downloadLocationMode = downloadLocationMode,
                    customDownloadDirUri = customDownloadDirUri
                )
            }

            override fun onUrlChanged(webView: WebView?, url: String?) {
                floatingWindowManager.notifyNavigationStateChanged()
            }
        }

        return { webView ->
            webViewManager.configureWebView(
                webView = webView,
                config = webViewConfig,
                callbacks = callbacks,
                extensionModuleIds = shellConfig.extensionModuleIds,
                embeddedExtensionModules = shellConfig.embeddedExtensionModules,
                extensionFabIcon = shellConfig.extensionFabIcon,
                allowGlobalModuleFallback = false,
                extensionEnabled = shellConfig.extensionEnabled,
                browserDisguiseConfig = shellConfig.browserDisguiseConfig,
                deviceDisguiseConfig = shellConfig.deviceDisguiseConfig
            )
        }
    }

    fun getManager(): FloatingWindowManager = floatingWindowManager

    fun getWebView(): WebView? = floatingWindowManager.getWebView()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                Strings.floatingWindowNotificationChannel,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = Strings.floatingWindowNotificationChannelDesc
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(appName: String = ""): Notification {
        val title = Strings.floatingWindowNotificationTitle
        val content = if (appName.isNotBlank()) {
            String.format(Strings.floatingWindowNotificationContent, appName)
        } else {
            Strings.floatingWindowNotificationContentDefault
        }

        val dismissIntent = Intent(this, FloatingWindowService::class.java).apply {
            putExtra(EXTRA_ACTION, ACTION_DISMISS)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                Strings.floatingWindowClose,
                dismissPendingIntent
            )
            .build()
    }

    private fun buildBlobDownloadJs(blobUrl: String, filename: String): String {
        val safeBlobUrl = org.json.JSONObject.quote(blobUrl)
        val safeFilename = org.json.JSONObject.quote(filename)
        return """
            (function() {
                try {
                    const blobUrl = $safeBlobUrl;
                    let filename = $safeFilename;
                    const LARGE_FILE_THRESHOLD = 10 * 1024 * 1024;
                    const CHUNK_SIZE = 512 * 1024;

                    if (window.__wtaBlobNameMap) {
                        var cachedName = window.__wtaBlobNameMap.get(blobUrl);
                        if (cachedName) filename = cachedName;
                    }

                    function uint8ToBase64(u8) {
                        const S = 8192; const p = [];
                        for (let i = 0; i < u8.length; i += S) p.push(String.fromCharCode.apply(null, u8.subarray(i, i + S)));
                        return btoa(p.join(''));
                    }

                    function processChunked(blob, fname) {
                        const mimeType = blob.type || 'application/octet-stream';
                        if (!window.AndroidDownload || !window.AndroidDownload.startChunkedDownload) {
                            processSmall(blob, fname); return;
                        }
                        const did = window.AndroidDownload.startChunkedDownload(fname, mimeType, blob.size);
                        let off = 0, ci = 0; const tc = Math.ceil(blob.size / CHUNK_SIZE);
                        function next() {
                            if (off >= blob.size) { window.AndroidDownload.finishChunkedDownload(did); return; }
                            blob.slice(off, off + CHUNK_SIZE).arrayBuffer().then(function(ab) {
                                window.AndroidDownload.appendChunk(did, uint8ToBase64(new Uint8Array(ab)), ci, tc);
                                off += CHUNK_SIZE; ci++;
                                setTimeout(next, 0);
                            });
                        }
                        next();
                    }

                    function processSmall(blob, fname) {
                        const reader = new FileReader();
                        reader.onloadend = function() {
                            const base64Data = reader.result.split(',')[1];
                            const mimeType = blob.type || 'application/octet-stream';
                            if (window.AndroidDownload && window.AndroidDownload.saveBase64File) {
                                window.AndroidDownload.saveBase64File(base64Data, fname, mimeType);
                            }
                        };
                        reader.readAsDataURL(blob);
                    }

                    if (blobUrl.startsWith('data:')) {
                        const parts = blobUrl.split(',');
                        const meta = parts[0];
                        const base64Data = parts[1];
                        const mimeMatch = meta.match(/data:([^;]+)/);
                        const mimeType = mimeMatch ? mimeMatch[1] : 'application/octet-stream';
                        if (window.AndroidDownload && window.AndroidDownload.saveBase64File) {
                            window.AndroidDownload.saveBase64File(base64Data, filename, mimeType);
                        }
                    } else if (blobUrl.startsWith('blob:')) {
                        const cachedBlob = window.__wtaBlobMap && window.__wtaBlobMap.get(blobUrl);
                        function dispatch(blob) {
                            if (blob.size > LARGE_FILE_THRESHOLD) {
                                processChunked(blob, filename);
                            } else {
                                processSmall(blob, filename);
                            }
                        }
                        if (cachedBlob) {
                            dispatch(cachedBlob);
                        } else {
                            fetch(blobUrl)
                                .then(function(r) { return r.blob(); })
                                .then(dispatch)
                                .catch(function(err) {
                                    console.error('[FloatingWindow] Blob fetch failed:', err);
                                });
                        }
                    }
                } catch(e) {
                    console.error('[FloatingWindow] Download error:', e);
                }
            })();
        """.trimIndent()
    }
}
