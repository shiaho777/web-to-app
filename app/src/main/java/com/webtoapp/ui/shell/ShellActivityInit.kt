package com.webtoapp.ui.shell

import android.view.KeyEvent
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.webtoapp.WebToAppApplication
import com.webtoapp.core.forcedrun.ForcedRunManager
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.ShellConfig

object ShellActivityInit {

    fun initLogger(activity: AppCompatActivity) {
        try {
            val tempConfig = WebToAppApplication.shellMode.getConfig()
            val versionName = try {
                activity.packageManager.getPackageInfo(activity.packageName, 0).versionName ?: "1.0.0"
            } catch (e: Exception) { "1.0.0" }

            com.webtoapp.core.shell.ShellLogger.init(
                context = activity,
                appName = tempConfig?.appName ?: "ShellApp",
                appVersion = versionName,
                fileLoggingEnabled = tempConfig?.loggingEnabled ?: false
            )
            com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "onCreate 开始")

            com.webtoapp.core.perf.SystemPerfOptimizer.optimizeActivity(activity)
        } catch (e: Exception) {
            AppLogger.e("ShellActivity", "日志系统初始化失败", e)
        }
    }

    fun initForcedRunManager(
        activity: AppCompatActivity,
        config: ShellConfig,
        forcedRunManager: ForcedRunManager,
        onStateChanged: (Boolean, com.webtoapp.core.forcedrun.ForcedRunConfig?) -> Unit
    ) {

        try {
            val hardwareController = com.webtoapp.core.forcedrun.ForcedRunHardwareAccess.getInstance(activity)
            com.webtoapp.core.forcedrun.ForcedRunHardwareAccess.setTargetActivity(hardwareController, activity)
            com.webtoapp.core.shell.ShellLogger.d("ShellActivity", "硬件控制器初始化成功")
        } catch (e: Exception) {
            com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "硬件控制器初始化失败", e)
        }

        if (config.forcedRunConfig?.enabled == true) {
            try {
                forcedRunManager.setTargetActivity(
                    packageName = activity.packageName,
                    activityClass = activity::class.java.name
                )
                forcedRunManager.setOnStateChangedCallback { active, forcedConfig ->
                    activity.runOnUiThread {
                        onStateChanged(active, forcedConfig)
                    }
                }
                com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "强制运行管理器初始化成功")
            } catch (e: Exception) {
                com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "强制运行管理器初始化失败", e)
            }
        }
    }

    fun initAutoStart(activity: AppCompatActivity, config: ShellConfig) {
        config.autoStartConfig?.let { autoStartConfig ->
            try {
                val autoStartManager = com.webtoapp.core.autostart.AutoStartManager(activity)

                autoStartManager.setBootStart(
                    appId = 0L,
                    enabled = autoStartConfig.bootStartEnabled,
                    delayMs = com.webtoapp.core.autostart.AutoStartManager.DEFAULT_BOOT_DELAY_MS
                )

                if (autoStartConfig.scheduledStartEnabled) {
                    autoStartManager.setScheduledStart(
                        appId = 0L,
                        enabled = true,
                        time = autoStartConfig.scheduledTime,
                        days = autoStartConfig.scheduledDays
                    )
                } else {
                    autoStartManager.setScheduledStart(appId = 0L, enabled = false)
                }

                com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "自启动配置已注册: 开机=${autoStartConfig.bootStartEnabled}, 定时=${autoStartConfig.scheduledStartEnabled}")
            } catch (e: Exception) {
                com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "自启动配置注册失败", e)
            }
        }
    }

    fun initIsolation(activity: AppCompatActivity, config: ShellConfig) {
        if (config.isolationEnabled && config.isolationConfig != null) {
            try {
                val isolationConfig = config.isolationConfig.toIsolationConfig()
                val isolationManager = com.webtoapp.core.privacy.IsolationPrivacyAccess.getInstance(activity)
                if (isolationManager == null) {
                    AppLogger.w("ShellActivity", "独立环境 pack 不可用，已跳过")
                    return
                }
                com.webtoapp.core.privacy.IsolationPrivacyAccess.initialize(isolationManager, isolationConfig)
                AppLogger.d("ShellActivity", "独立环境已初始化: enabled=${isolationConfig.enabled}")
                com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "独立环境已初始化")
            } catch (e: Exception) {
                AppLogger.e("ShellActivity", "独立环境初始化失败", e)
                com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "独立环境初始化失败", e)
            }
        }
    }

    fun initBackgroundService(activity: AppCompatActivity, config: ShellConfig) {
        if (config.backgroundRunEnabled) {
            try {
                val bgConfig = config.backgroundRunConfig
                com.webtoapp.core.background.BackgroundRunService.start(
                    context = activity,
                    appName = config.appName,
                    notificationTitle = bgConfig?.notificationTitle?.ifEmpty { null },
                    notificationContent = bgConfig?.notificationContent?.ifEmpty { null },
                    showNotification = bgConfig?.showNotification ?: true,
                    keepCpuAwake = bgConfig?.keepCpuAwake ?: true
                )
                AppLogger.d("ShellActivity", "后台运行服务已启动")
                com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "后台运行服务已启动")
            } catch (e: Exception) {
                com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "后台运行服务启动失败", e)
            }
        }
    }

    fun initNotificationService(activity: AppCompatActivity, config: ShellConfig) {
        if (config.notificationEnabled && config.notificationConfig != null) {
            try {
                val nc = config.notificationConfig
                when {
                    nc.type == "polling" && nc.pollUrl.isNotBlank() -> {
                        com.webtoapp.core.notification.NotificationPollingService.start(
                            context = activity,
                            appName = config.appName,
                            pollUrl = nc.pollUrl,
                            pollIntervalMinutes = nc.pollIntervalMinutes,
                            pollMethod = nc.pollMethod,
                            pollHeaders = nc.pollHeaders,
                            clickUrl = nc.clickUrl
                        )
                        com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "轮询通知服务已启动: url=${nc.pollUrl}")
                    }
                    nc.type == "websocket" && nc.wsUrl.isNotBlank() -> {
                        com.webtoapp.core.notification.NotificationWebSocketService.start(
                            context = activity,
                            appName = config.appName,
                            wsUrl = nc.wsUrl,
                            wsHeaders = nc.wsHeaders,
                            registerUrl = nc.registerUrl,
                            registerHeaders = nc.registerHeaders,
                            authToken = nc.authToken,
                            clickUrl = nc.clickUrl
                        )
                        com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "WebSocket 通知服务已启动: url=${nc.wsUrl}")
                    }
                    nc.type == "fcm" -> {
                        com.webtoapp.core.notification.FcmAccess.start(
                            context = activity,
                            projectId = nc.fcmProjectId,
                            applicationId = nc.fcmApplicationId,
                            apiKey = nc.fcmApiKey,
                            senderId = nc.fcmSenderId,
                            registerUrl = nc.registerUrl,
                            registerHeaders = nc.registerHeaders,
                            authToken = nc.authToken,
                            clickUrl = nc.clickUrl,
                            appName = config.appName,
                            googleServicesJson = nc.fcmGoogleServicesJson
                        )
                        com.webtoapp.core.shell.ShellLogger.i("ShellActivity", "FCM 通知通道已启动: project=${nc.fcmProjectId}")
                    }
                }

            } catch (e: Exception) {
                com.webtoapp.core.shell.ShellLogger.e("ShellActivity", "通知服务启动失败", e)
            }
        }
    }

    fun setTaskDescription(activity: AppCompatActivity, appName: String) {
        try {
            @Suppress("DEPRECATION")
            activity.setTaskDescription(android.app.ActivityManager.TaskDescription(appName))
        } catch (e: Exception) {
            com.webtoapp.core.shell.ShellLogger.w("ShellActivity", "setTaskDescription 失败", e)
        }
    }

    fun createBackPressedCallback(
        activity: AppCompatActivity,
        forcedRunManager: ForcedRunManager,
        getCustomView: () -> android.view.View?,
        getWebView: () -> WebView?,
        hideCustomView: () -> Unit,
        getShellConfig: () -> ShellConfig?
    ): OnBackPressedCallback {
        return object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (forcedRunManager.handleKeyEvent(KeyEvent.KEYCODE_BACK)) {
                    Toast.makeText(activity, Strings.cannotExitDuringForcedRun, Toast.LENGTH_SHORT).show()
                    return
                }
                when {
                    getCustomView() != null -> hideCustomView()
                    else -> {

                        val wv = getWebView()
                        if (wv != null) {
                            wv.evaluateJavascript("""
                                (function() {
                                    var evt = new KeyboardEvent('keydown', {
                                        key: 'Escape', code: 'Escape',
                                        keyCode: 27, which: 27,
                                        bubbles: true, cancelable: true
                                    });
                                    return !document.dispatchEvent(evt);
                                })();
                            """.trimIndent()) { result ->
                                if (result == "true") {

                                    return@evaluateJavascript
                                }

                                val useJsHistoryBack = getShellConfig()?.webViewConfig?.enableBackStatePreservation ?: false
                                ShellWebViewNavigation.goBackOrFinish(activity, wv, useJsHistoryBack = useJsHistoryBack)
                            }
                        } else {
                            activity.finish()
                        }
                    }
                }
            }
        }
    }
}
