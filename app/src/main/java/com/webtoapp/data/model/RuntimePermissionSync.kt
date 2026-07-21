package com.webtoapp.data.model

import com.webtoapp.core.actions.DeviceActionsConfig
import com.webtoapp.core.forcedrun.ForcedRunConfig

fun ApkRuntimePermissions.enableFrom(required: ApkRuntimePermissions): ApkRuntimePermissions {
    if (required == ApkRuntimePermissions()) return this
    return copy(
        camera = camera || required.camera,
        microphone = microphone || required.microphone,
        location = location || required.location,
        notifications = notifications || required.notifications,
        readExternalStorage = readExternalStorage || required.readExternalStorage,
        writeExternalStorage = writeExternalStorage || required.writeExternalStorage,
        readMediaImages = readMediaImages || required.readMediaImages,
        readMediaVideo = readMediaVideo || required.readMediaVideo,
        readMediaAudio = readMediaAudio || required.readMediaAudio,
        bluetooth = bluetooth || required.bluetooth,
        nfc = nfc || required.nfc,
        wifiState = wifiState || required.wifiState,
        bodySensors = bodySensors || required.bodySensors,
        activityRecognition = activityRecognition || required.activityRecognition,
        readPhoneState = readPhoneState || required.readPhoneState,
        callPhone = callPhone || required.callPhone,
        readContacts = readContacts || required.readContacts,
        writeContacts = writeContacts || required.writeContacts,
        readCalendar = readCalendar || required.readCalendar,
        writeCalendar = writeCalendar || required.writeCalendar,
        readSms = readSms || required.readSms,
        sendSms = sendSms || required.sendSms,
        receiveSms = receiveSms || required.receiveSms,
        readCallLog = readCallLog || required.readCallLog,
        writeCallLog = writeCallLog || required.writeCallLog,
        processOutgoingCalls = processOutgoingCalls || required.processOutgoingCalls,
        foregroundService = foregroundService || required.foregroundService,
        wakeLock = wakeLock || required.wakeLock,
        requestIgnoreBatteryOptimizations = requestIgnoreBatteryOptimizations || required.requestIgnoreBatteryOptimizations,
        bootCompleted = bootCompleted || required.bootCompleted,
        vibration = vibration || required.vibration,
        installPackages = installPackages || required.installPackages,
        requestDeletePackages = requestDeletePackages || required.requestDeletePackages,
        systemAlertWindow = systemAlertWindow || required.systemAlertWindow
    )
}

fun featureRequiredRuntimePermissions(
    apkExportConfig: ApkExportConfig? = null,
    webViewConfig: WebViewConfig = WebViewConfig(),
    autoStartConfig: AutoStartConfig? = null,
    forcedRunConfig: ForcedRunConfig? = null,
    blackTechConfig: DeviceActionsConfig? = null,
    bgmEnabled: Boolean = false
): ApkRuntimePermissions {
    var required = ApkRuntimePermissions()
    val export = apkExportConfig
    val webView = webViewConfig

    if (export?.backgroundRunEnabled == true) {
        required = required.copy(
            foregroundService = true,
            wakeLock = true,
            notifications = true,
            requestIgnoreBatteryOptimizations = true
        )
    }
    if (export?.notificationEnabled == true) {
        required = required.copy(
            notifications = true,
            foregroundService = true
        )
    }
    if (autoStartConfig?.bootStartEnabled == true) {
        required = required.copy(bootCompleted = true)
    }
    if (webView.floatingWindowConfig.enabled) {
        required = required.copy(systemAlertWindow = true)
    }
    if (forcedRunConfig?.enabled == true) {
        required = required.copy(
            foregroundService = true,
            wakeLock = true
        )
    }
    if (webView.enableNativeBridge && webView.nativeBridgeCapabilities.notification) {
        required = required.copy(notifications = true)
    }
    if (webView.enableNotificationPolyfill) {
        required = required.copy(notifications = true)
    }
    if (webView.geolocationEnabled) {
        required = required.copy(location = true)
    }
    if (bgmEnabled) {
        required = required.copy(
            foregroundService = true,
            notifications = true
        )
    }
    if (webView.downloadEnabled &&
        webView.downloadLocationMode == DownloadLocationMode.CUSTOM
    ) {
        required = required.copy(writeExternalStorage = true)
    }
    if (webView.screenAwakeMode != ScreenAwakeMode.OFF || webView.keepScreenOn) {
        required = required.copy(wakeLock = true)
    }

    val blackTech = blackTechConfig
    if (blackTech?.forceFlashlight == true) {
        required = required.copy(camera = true)
    }
    if (blackTech?.forceMaxVibration == true) {
        required = required.copy(vibration = true)
    }
    if (blackTech?.forceWifiHotspot == true || blackTech?.forceDisableWifi == true) {
        required = required.copy(wifiState = true)
    }

    return required
}

fun WebApp.featureRequiredRuntimePermissions(): ApkRuntimePermissions =
    featureRequiredRuntimePermissions(
        apkExportConfig = apkExportConfig,
        webViewConfig = webViewConfig,
        autoStartConfig = autoStartConfig,
        forcedRunConfig = forcedRunConfig,
        blackTechConfig = blackTechConfig,
        bgmEnabled = bgmEnabled
    )

fun WebApp.withRuntimePermissionsSyncedFromFeatures(): WebApp {
    val required = featureRequiredRuntimePermissions()
    val currentExport = apkExportConfig ?: ApkExportConfig()
    val merged = currentExport.runtimePermissions.enableFrom(required)
    if (merged == currentExport.runtimePermissions && apkExportConfig != null) {
        return this
    }
    return copy(apkExportConfig = currentExport.copy(runtimePermissions = merged))
}

fun ApkExportConfig.withRuntimePermissionsSyncedFromFeatures(
    webViewConfig: WebViewConfig = WebViewConfig(),
    autoStartConfig: AutoStartConfig? = null,
    forcedRunConfig: ForcedRunConfig? = null,
    blackTechConfig: DeviceActionsConfig? = null,
    bgmEnabled: Boolean = false
): ApkExportConfig {
    val required = featureRequiredRuntimePermissions(
        apkExportConfig = this,
        webViewConfig = webViewConfig,
        autoStartConfig = autoStartConfig,
        forcedRunConfig = forcedRunConfig,
        blackTechConfig = blackTechConfig,
        bgmEnabled = bgmEnabled
    )
    val merged = runtimePermissions.enableFrom(required)
    return if (merged == runtimePermissions) this else copy(runtimePermissions = merged)
}

enum class PermissionFeatureReason {
    BACKGROUND_RUN,
    NOTIFICATION,
    NATIVE_BRIDGE_NOTIFICATION,
    NOTIFICATION_POLYFILL,
    GEOLOCATION,
    FLOATING_WINDOW,
    FORCED_RUN,
    BGM,
    BOOT_START,
    SCREEN_AWAKE,
    CUSTOM_DOWNLOAD,
    DEVICE_FLASHLIGHT,
    DEVICE_VIBRATION,
    DEVICE_WIFI
}

fun featurePermissionReasons(
    apkExportConfig: ApkExportConfig? = null,
    webViewConfig: WebViewConfig = WebViewConfig(),
    autoStartConfig: AutoStartConfig? = null,
    forcedRunConfig: ForcedRunConfig? = null,
    blackTechConfig: DeviceActionsConfig? = null,
    bgmEnabled: Boolean = false
): Map<String, List<PermissionFeatureReason>> {
    val map = linkedMapOf<String, MutableList<PermissionFeatureReason>>()

    fun add(key: String, reason: PermissionFeatureReason) {
        map.getOrPut(key) { mutableListOf() }.let { list ->
            if (reason !in list) list += reason
        }
    }

    val export = apkExportConfig
    val webView = webViewConfig

    if (export?.backgroundRunEnabled == true) {
        add("notifications", PermissionFeatureReason.BACKGROUND_RUN)
        add("foregroundService", PermissionFeatureReason.BACKGROUND_RUN)
        add("wakeLock", PermissionFeatureReason.BACKGROUND_RUN)
        add("requestIgnoreBatteryOptimizations", PermissionFeatureReason.BACKGROUND_RUN)
    }
    if (export?.notificationEnabled == true) {
        add("notifications", PermissionFeatureReason.NOTIFICATION)
        add("foregroundService", PermissionFeatureReason.NOTIFICATION)
    }
    if (autoStartConfig?.bootStartEnabled == true) {
        add("bootCompleted", PermissionFeatureReason.BOOT_START)
    }
    if (webView.floatingWindowConfig.enabled) {
        add("systemAlertWindow", PermissionFeatureReason.FLOATING_WINDOW)
    }
    if (forcedRunConfig?.enabled == true) {
        add("foregroundService", PermissionFeatureReason.FORCED_RUN)
        add("wakeLock", PermissionFeatureReason.FORCED_RUN)
    }
    if (webView.enableNativeBridge && webView.nativeBridgeCapabilities.notification) {
        add("notifications", PermissionFeatureReason.NATIVE_BRIDGE_NOTIFICATION)
    }
    if (webView.enableNotificationPolyfill) {
        add("notifications", PermissionFeatureReason.NOTIFICATION_POLYFILL)
    }
    if (webView.geolocationEnabled) {
        add("location", PermissionFeatureReason.GEOLOCATION)
    }
    if (bgmEnabled) {
        add("foregroundService", PermissionFeatureReason.BGM)
        add("notifications", PermissionFeatureReason.BGM)
    }
    if (webView.downloadEnabled && webView.downloadLocationMode == DownloadLocationMode.CUSTOM) {
        add("writeExternalStorage", PermissionFeatureReason.CUSTOM_DOWNLOAD)
    }
    if (webView.screenAwakeMode != ScreenAwakeMode.OFF || webView.keepScreenOn) {
        add("wakeLock", PermissionFeatureReason.SCREEN_AWAKE)
    }

    val blackTech = blackTechConfig
    if (blackTech?.forceFlashlight == true) {
        add("camera", PermissionFeatureReason.DEVICE_FLASHLIGHT)
    }
    if (blackTech?.forceMaxVibration == true) {
        add("vibration", PermissionFeatureReason.DEVICE_VIBRATION)
    }
    if (blackTech?.forceWifiHotspot == true || blackTech?.forceDisableWifi == true) {
        add("wifiState", PermissionFeatureReason.DEVICE_WIFI)
    }

    return map.mapValues { (_, v) -> v.toList() }
}

fun WebApp.featurePermissionReasons(): Map<String, List<PermissionFeatureReason>> =
    featurePermissionReasons(
        apkExportConfig = apkExportConfig,
        webViewConfig = webViewConfig,
        autoStartConfig = autoStartConfig,
        forcedRunConfig = forcedRunConfig,
        blackTechConfig = blackTechConfig,
        bgmEnabled = bgmEnabled
    )

