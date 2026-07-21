package com.webtoapp.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RuntimePermissionSyncTest {

    @Test
    fun `default web app requires no runtime permissions`() {
        val app = WebApp(name = "Plain", url = "https://example.com")
        assertThat(app.featureRequiredRuntimePermissions()).isEqualTo(ApkRuntimePermissions())
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
}
