package com.webtoapp.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RuntimePermissionSyncTest {

    @Test
    fun `default web app requires media session permissions`() {
        // enableMediaSession defaults to true: WebMediaPlaybackService is a
        // mediaPlayback foreground service that acquires a partial wake lock.
        // Missing WAKE_LOCK crashed generated APKs with SecurityException (#1034).
        val app = WebApp(name = "Plain", url = "https://example.com")
        val required = app.featureRequiredRuntimePermissions()
        assertThat(required).isEqualTo(
            ApkRuntimePermissions(foregroundService = true, wakeLock = true)
        )
    }

    @Test
    fun `media session disabled requires no runtime permissions`() {
        val app = WebApp(
            name = "Plain",
            url = "https://example.com",
            webViewConfig = WebViewConfig(enableMediaSession = false)
        )
        assertThat(app.featureRequiredRuntimePermissions()).isEqualTo(ApkRuntimePermissions())
    }

    @Test
    fun `media session disabled manually keeps wake lock when user enabled it`() {
        val app = WebApp(
            name = "Plain",
            url = "https://example.com",
            webViewConfig = WebViewConfig(enableMediaSession = false),
            apkExportConfig = ApkExportConfig(
                runtimePermissions = ApkRuntimePermissions(wakeLock = true)
            )
        )
        val synced = app.withRuntimePermissionsSyncedFromFeatures()
        assertThat(synced.apkExportConfig?.runtimePermissions?.wakeLock).isTrue()
    }

    @Test
    fun `notification feature auto enables wake lock for polling services`() {
        // NotificationPollingService/WebSocketService hold partial wake locks
        // while fetching pushes — missing WAKE_LOCK crashes them (#1034).
        val app = WebApp(
            name = "Notify",
            url = "https://example.com",
            webViewConfig = WebViewConfig(enableMediaSession = false),
            apkExportConfig = ApkExportConfig(notificationEnabled = true)
        )
        val required = app.featureRequiredRuntimePermissions()
        assertThat(required.notifications).isTrue()
        assertThat(required.foregroundService).isTrue()
        assertThat(required.wakeLock).isTrue()
    }

    @Test
    fun `scheduled start auto enables wake lock`() {
        val app = WebApp(
            name = "Sched",
            url = "https://example.com",
            webViewConfig = WebViewConfig(enableMediaSession = false),
            autoStartConfig = AutoStartConfig(scheduledStartEnabled = true)
        )
        assertThat(app.featureRequiredRuntimePermissions().wakeLock).isTrue()

        val reasons = app.featurePermissionReasons()
        assertThat(reasons["wakeLock"]).contains(PermissionFeatureReason.BOOT_START)
    }

    @Test
    fun `media session auto enables wake lock and foreground service`() {
        val app = WebApp(
            name = "Media",
            url = "https://example.com",
            webViewConfig = WebViewConfig(enableMediaSession = true)
        )
        val required = app.featureRequiredRuntimePermissions()
        assertThat(required.wakeLock).isTrue()
        assertThat(required.foregroundService).isTrue()

        val synced = app.withRuntimePermissionsSyncedFromFeatures()
        assertThat(synced.apkExportConfig?.runtimePermissions?.wakeLock).isTrue()
        assertThat(synced.apkExportConfig?.runtimePermissions?.foregroundService).isTrue()

        val reasons = app.featurePermissionReasons()
        assertThat(reasons["wakeLock"]).contains(PermissionFeatureReason.MEDIA_SESSION)
        assertThat(reasons["foregroundService"]).contains(PermissionFeatureReason.MEDIA_SESSION)
    }

    @Test
    fun `background run auto enables visible notification and service permissions`() {
        val app = WebApp(
            name = "Bg",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig(backgroundRunEnabled = true)
        )
        val required = app.featureRequiredRuntimePermissions()
        assertThat(required.notifications).isTrue()
        assertThat(required.foregroundService).isTrue()
        assertThat(required.wakeLock).isTrue()
        assertThat(required.requestIgnoreBatteryOptimizations).isTrue()

        val synced = app.withRuntimePermissionsSyncedFromFeatures()
        assertThat(synced.apkExportConfig?.runtimePermissions?.notifications).isTrue()
        assertThat(synced.apkExportConfig?.runtimePermissions?.foregroundService).isTrue()
    }

    @Test
    fun `notification feature auto enables notifications in export config`() {
        val app = WebApp(
            name = "Notify",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig(notificationEnabled = true)
        )
        val synced = app.withRuntimePermissionsSyncedFromFeatures()
        assertThat(synced.apkExportConfig?.runtimePermissions?.notifications).isTrue()
        assertThat(synced.apkExportConfig?.runtimePermissions?.foregroundService).isTrue()
    }

    @Test
    fun `native bridge notification auto enables notifications`() {
        val app = WebApp(
            name = "Bridge",
            url = "https://example.com",
            webViewConfig = WebViewConfig(
                enableNativeBridge = true,
                nativeBridgeCapabilities = NativeBridgeCapabilities(notification = true)
            )
        )
        val synced = app.withRuntimePermissionsSyncedFromFeatures()
        assertThat(synced.apkExportConfig?.runtimePermissions?.notifications).isTrue()
    }

    @Test
    fun `sync preserves manually enabled permissions`() {
        val app = WebApp(
            name = "Cam",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig(
                backgroundRunEnabled = true,
                runtimePermissions = ApkRuntimePermissions(camera = true)
            )
        )
        val synced = app.withRuntimePermissionsSyncedFromFeatures()
        val perms = synced.apkExportConfig!!.runtimePermissions
        assertThat(perms.camera).isTrue()
        assertThat(perms.notifications).isTrue()
    }

    @Test
    fun `system download default does not force write storage permission`() {
        val app = WebApp(
            name = "Dl",
            url = "https://example.com",
            webViewConfig = WebViewConfig(
                downloadEnabled = true,
                downloadLocationMode = DownloadLocationMode.SYSTEM_DOWNLOAD
            )
        )
        assertThat(app.featureRequiredRuntimePermissions().writeExternalStorage).isFalse()
    }

    @Test
    fun `custom download location auto enables write storage`() {
        val app = WebApp(
            name = "Dl",
            url = "https://example.com",
            webViewConfig = WebViewConfig(
                downloadEnabled = true,
                downloadLocationMode = DownloadLocationMode.CUSTOM
            )
        )
        assertThat(app.featureRequiredRuntimePermissions().writeExternalStorage).isTrue()
    }

    @Test
    fun `featurePermissionReasons lists sources for background run`() {
        val app = WebApp(
            name = "Bg",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig(backgroundRunEnabled = true)
        )
        val reasons = app.featurePermissionReasons()
        assertThat(reasons["notifications"]).contains(PermissionFeatureReason.BACKGROUND_RUN)
        assertThat(reasons["foregroundService"]).contains(PermissionFeatureReason.BACKGROUND_RUN)
        assertThat(reasons["wakeLock"]).contains(PermissionFeatureReason.BACKGROUND_RUN)
    }

    @Test
    fun `floating window auto enables overlay and foreground service permissions`() {
        // FloatingWindowService is a specialUse foreground service; missing
        // FOREGROUND_SERVICE made startForeground throw SecurityException and the
        // exported app crashed on launch.
        val app = WebApp(
            name = "Fw",
            url = "https://example.com",
            webViewConfig = WebViewConfig(
                floatingWindowConfig = FloatingWindowConfig(enabled = true)
            )
        )
        val required = app.featureRequiredRuntimePermissions()
        assertThat(required.systemAlertWindow).isTrue()
        assertThat(required.foregroundService).isTrue()

        val synced = app.withRuntimePermissionsSyncedFromFeatures()
        assertThat(synced.apkExportConfig?.runtimePermissions?.systemAlertWindow).isTrue()
        assertThat(synced.apkExportConfig?.runtimePermissions?.foregroundService).isTrue()

        val reasons = app.featurePermissionReasons()
        assertThat(reasons["systemAlertWindow"]).contains(PermissionFeatureReason.FLOATING_WINDOW)
        assertThat(reasons["foregroundService"]).contains(PermissionFeatureReason.FLOATING_WINDOW)
    }

    @Test
    fun `location permission implies geolocation enabled`() {
        val app = WebApp(
            name = "Loc",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig(
                runtimePermissions = ApkRuntimePermissions(location = true)
            )
        )
        val synced = app.withRuntimePermissionsSyncedFromFeatures()
        assertThat(synced.webViewConfig.geolocationEnabled).isTrue()
    }

    @Test
    fun `geolocation enabled implies location permission`() {
        val app = WebApp(
            name = "Geo",
            url = "https://example.com",
            webViewConfig = WebViewConfig(geolocationEnabled = true)
        )
        val synced = app.withRuntimePermissionsSyncedFromFeatures()
        assertThat(synced.apkExportConfig?.runtimePermissions?.location).isTrue()
        assertThat(synced.webViewConfig.geolocationEnabled).isTrue()
    }

    @Test
    fun `disabling a feature clears the permission it had auto-enabled`() {
        // Sync with the feature ON first, establishing autoEnabledPermissions.
        val withFeature = WebApp(
            name = "Notify",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig(notificationEnabled = true)
        ).withRuntimePermissionsSyncedFromFeatures()
        assertThat(withFeature.apkExportConfig?.runtimePermissions?.notifications).isTrue()

        // Turn the feature OFF and sync again: the auto-enabled permission is cleared
        // (the old one-way OR latch kept it forever — issue #356).
        val featureOff = withFeature.copy(
            apkExportConfig = withFeature.apkExportConfig!!.copy(notificationEnabled = false)
        ).withRuntimePermissionsSyncedFromFeatures()
        assertThat(featureOff.apkExportConfig?.runtimePermissions?.notifications).isFalse()
    }

    @Test
    fun `manually added permission survives the enabling feature being turned off`() {
        // User manually enabled camera while a notification feature was on.
        val withFeature = WebApp(
            name = "Mix",
            url = "https://example.com",
            apkExportConfig = ApkExportConfig(
                notificationEnabled = true,
                runtimePermissions = ApkRuntimePermissions(camera = true)
            )
        ).withRuntimePermissionsSyncedFromFeatures()
        assertThat(withFeature.apkExportConfig?.runtimePermissions?.camera).isTrue()
        assertThat(withFeature.apkExportConfig?.runtimePermissions?.notifications).isTrue()

        // Turn the feature off: auto-enabled notifications cleared, manual camera preserved.
        val featureOff = withFeature.copy(
            apkExportConfig = withFeature.apkExportConfig!!.copy(notificationEnabled = false)
        ).withRuntimePermissionsSyncedFromFeatures()
        assertThat(featureOff.apkExportConfig?.runtimePermissions?.notifications).isFalse()
        assertThat(featureOff.apkExportConfig?.runtimePermissions?.camera).isTrue()
    }
}
