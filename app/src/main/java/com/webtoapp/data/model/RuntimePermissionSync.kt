package com.webtoapp.data.model

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
            foregroundService = true,
            // Polling/WebSocket notification services hold partial wake locks
            // while fetching pushes.
            wakeLock = true
        )
    }
    if (autoStartConfig?.bootStartEnabled == true) {
        // BootReceiver holds a wake lock while scheduling the launch.
        required = required.copy(bootCompleted = true, wakeLock = true)
    }
    if (autoStartConfig?.scheduledStartEnabled == true) {
        required = required.copy(wakeLock = true)
    }
    if (webView.floatingWindowConfig.enabled) {
        // FloatingWindowService is a specialUse foreground service: without FOREGROUND_SERVICE
        // (plus FOREGROUND_SERVICE_SPECIAL_USE, bundled in ApkBuilder) startForeground throws
        // SecurityException and the exported app crashes on launch.
        required = required.copy(
            systemAlertWindow = true,
            foregroundService = true
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
    if (webView.enableMediaSession) {
        // WebMediaPlaybackService runs as a mediaPlayback foreground service and
        // acquires a partial wake lock whenever the page reports playback — a
        // missing WAKE_LOCK crashed generated APKs with SecurityException (#1034).
        required = required.copy(
            foregroundService = true,
            wakeLock = true
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

    return required
}

fun WebApp.featureRequiredRuntimePermissions(): ApkRuntimePermissions =
    featureRequiredRuntimePermissions(
        apkExportConfig = apkExportConfig,
        webViewConfig = webViewConfig,
        autoStartConfig = autoStartConfig,
        bgmEnabled = bgmEnabled
    )

fun WebApp.withRuntimePermissionsSyncedFromFeatures(): WebApp {
    val required = featureRequiredRuntimePermissions()
    val currentExport = apkExportConfig ?: ApkExportConfig()
    // Preserve permissions the user added manually (beyond what features auto-enabled last
    // time) while recomputing feature-required permissions. This lets turning a feature OFF
    // clear the permission it had auto-enabled, instead of the old one-way OR latch that kept
    // it forever (issue #356).
    val manual = currentExport.runtimePermissions.manualBeyond(currentExport.autoEnabledPermissions)
    val merged = manual.enableFrom(required)
    // Location coherence (issue #292): granting the location permission implies the
    // WebView geolocation API should be enabled. The reverse direction (geolocationEnabled
    // -> location permission) is already handled by featureRequiredRuntimePermissions above.
    val effectiveGeolocation = webViewConfig.geolocationEnabled || merged.location
    val permissionsChanged = merged != currentExport.runtimePermissions ||
        required != currentExport.autoEnabledPermissions || apkExportConfig == null
    val geolocationChanged = effectiveGeolocation != webViewConfig.geolocationEnabled
    if (!permissionsChanged && !geolocationChanged) {
        return this
    }
    return copy(
        apkExportConfig = currentExport.copy(runtimePermissions = merged, autoEnabledPermissions = required),
        webViewConfig = webViewConfig.copy(geolocationEnabled = effectiveGeolocation)
    )
}

fun ApkExportConfig.withRuntimePermissionsSyncedFromFeatures(
    webViewConfig: WebViewConfig = WebViewConfig(),
    autoStartConfig: AutoStartConfig? = null,
    bgmEnabled: Boolean = false
): ApkExportConfig {
    val required = featureRequiredRuntimePermissions(
        apkExportConfig = this,
        webViewConfig = webViewConfig,
        autoStartConfig = autoStartConfig,
        bgmEnabled = bgmEnabled
    )
    val manual = runtimePermissions.manualBeyond(autoEnabledPermissions)
    val merged = manual.enableFrom(required)
    return if (merged == runtimePermissions && required == autoEnabledPermissions) this
    else copy(runtimePermissions = merged, autoEnabledPermissions = required)
}

enum class PermissionFeatureReason {
    BACKGROUND_RUN,
    NOTIFICATION,
    NATIVE_BRIDGE_NOTIFICATION,
    NOTIFICATION_POLYFILL,
    GEOLOCATION,
    FLOATING_WINDOW,
    BGM,
    MEDIA_SESSION,
    BOOT_START,
    SCREEN_AWAKE,
    CUSTOM_DOWNLOAD
}

fun featurePermissionReasons(
    apkExportConfig: ApkExportConfig? = null,
    webViewConfig: WebViewConfig = WebViewConfig(),
    autoStartConfig: AutoStartConfig? = null,
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
        add("wakeLock", PermissionFeatureReason.NOTIFICATION)
    }
    if (autoStartConfig?.bootStartEnabled == true) {
        add("bootCompleted", PermissionFeatureReason.BOOT_START)
        add("wakeLock", PermissionFeatureReason.BOOT_START)
    }
    if (autoStartConfig?.scheduledStartEnabled == true) {
        add("wakeLock", PermissionFeatureReason.BOOT_START)
    }
    if (webView.floatingWindowConfig.enabled) {
        add("systemAlertWindow", PermissionFeatureReason.FLOATING_WINDOW)
        add("foregroundService", PermissionFeatureReason.FLOATING_WINDOW)
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
    if (webView.enableMediaSession) {
        add("foregroundService", PermissionFeatureReason.MEDIA_SESSION)
        add("wakeLock", PermissionFeatureReason.MEDIA_SESSION)
    }
    if (webView.downloadEnabled && webView.downloadLocationMode == DownloadLocationMode.CUSTOM) {
        add("writeExternalStorage", PermissionFeatureReason.CUSTOM_DOWNLOAD)
    }
    if (webView.screenAwakeMode != ScreenAwakeMode.OFF || webView.keepScreenOn) {
        add("wakeLock", PermissionFeatureReason.SCREEN_AWAKE)
    }

    return map.mapValues { (_, v) -> v.toList() }
}

fun WebApp.featurePermissionReasons(): Map<String, List<PermissionFeatureReason>> =
    featurePermissionReasons(
        apkExportConfig = apkExportConfig,
        webViewConfig = webViewConfig,
        autoStartConfig = autoStartConfig,
        bgmEnabled = bgmEnabled
    )

