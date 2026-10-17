package com.webtoapp.core.apkbuilder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.webtoapp.data.model.ApkExportConfig
import com.webtoapp.data.model.ApkRuntimePermissions
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.ExportPermKind
import com.webtoapp.data.model.ExportPermissionCatalog
import com.webtoapp.data.model.PermissionFeatureDefaults
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.data.model.exportGranted
import com.webtoapp.data.model.withExportGrant
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Modifier

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExportPermissionCatalogTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun permissionsFor(rp: ApkRuntimePermissions): List<String> {
        val builder = ApkBuilder(context)
        val method = ApkBuilder::class.java.getDeclaredMethod(
            "buildRequiredPermissions",
            ApkConfig::class.java
        ).apply { isAccessible = true }
        val config = WebApp(
            name = "Perms",
            url = "https://example.com",
            appType = AppType.WEB,
            webViewConfig = WebViewConfig(downloadEnabled = false, enableMediaSession = false),
            apkExportConfig = ApkExportConfig(runtimePermissions = rp)
        ).toApkConfig("com.example.perms", context)
        @Suppress("UNCHECKED_CAST")
        return method.invoke(builder, config) as List<String>
    }

    private fun manifest(
        permissions: ApkRuntimePermissions,
        defaults: PermissionFeatureDefaults = PermissionFeatureDefaults(),
        download: Boolean = false
    ): List<String> = ExportPermissionCatalog.manifestPermissions(
        packageName = "com.example.perms",
        permissions = permissions,
        defaults = defaults,
        downloadWithoutNotification = download
    )

    @Test
    fun `every legacy boolean turns on at least one official row`() {
        val fields = ApkRuntimePermissions::class.java.declaredFields
            .filter { it.type == java.lang.Boolean.TYPE && !Modifier.isStatic(it.modifiers) }
            .map { it.name }
            .toSet()
        val probes = mapOf(
            "camera" to ApkRuntimePermissions(camera = true),
            "microphone" to ApkRuntimePermissions(microphone = true),
            "location" to ApkRuntimePermissions(location = true),
            "notifications" to ApkRuntimePermissions(notifications = true),
            "readExternalStorage" to ApkRuntimePermissions(readExternalStorage = true),
            "writeExternalStorage" to ApkRuntimePermissions(writeExternalStorage = true),
            "readMediaImages" to ApkRuntimePermissions(readMediaImages = true),
            "readMediaVideo" to ApkRuntimePermissions(readMediaVideo = true),
            "readMediaAudio" to ApkRuntimePermissions(readMediaAudio = true),
            "bluetooth" to ApkRuntimePermissions(bluetooth = true),
            "nfc" to ApkRuntimePermissions(nfc = true),
            "wifiState" to ApkRuntimePermissions(wifiState = true),
            "nearbyWifiDevices" to ApkRuntimePermissions(nearbyWifiDevices = true),
            "bodySensors" to ApkRuntimePermissions(bodySensors = true),
            "activityRecognition" to ApkRuntimePermissions(activityRecognition = true),
            "readPhoneState" to ApkRuntimePermissions(readPhoneState = true),
            "callPhone" to ApkRuntimePermissions(callPhone = true),
            "readContacts" to ApkRuntimePermissions(readContacts = true),
            "writeContacts" to ApkRuntimePermissions(writeContacts = true),
            "readCalendar" to ApkRuntimePermissions(readCalendar = true),
            "writeCalendar" to ApkRuntimePermissions(writeCalendar = true),
            "readSms" to ApkRuntimePermissions(readSms = true),
            "sendSms" to ApkRuntimePermissions(sendSms = true),
            "receiveSms" to ApkRuntimePermissions(receiveSms = true),
            "readCallLog" to ApkRuntimePermissions(readCallLog = true),
            "writeCallLog" to ApkRuntimePermissions(writeCallLog = true),
            "processOutgoingCalls" to ApkRuntimePermissions(processOutgoingCalls = true),
            "foregroundService" to ApkRuntimePermissions(foregroundService = true),
            "wakeLock" to ApkRuntimePermissions(wakeLock = true),
            "requestIgnoreBatteryOptimizations" to ApkRuntimePermissions(requestIgnoreBatteryOptimizations = true),
            "bootCompleted" to ApkRuntimePermissions(bootCompleted = true),
            "vibration" to ApkRuntimePermissions(vibration = true),
            "installPackages" to ApkRuntimePermissions(installPackages = true),
            "requestDeletePackages" to ApkRuntimePermissions(requestDeletePackages = true),
            "systemAlertWindow" to ApkRuntimePermissions(systemAlertWindow = true),
            "backgroundLocation" to ApkRuntimePermissions(backgroundLocation = true),
            "queryAllPackages" to ApkRuntimePermissions(queryAllPackages = true)
        )
        assertThat(probes.keys).containsExactlyElementsIn(fields)
        probes.forEach { (name, permissions) ->
            assertWithMessage(name)
                .that(ExportPermissionCatalog.entries.any { it.legacy(permissions) })
                .isTrue()
        }
    }

    @Test
    fun `official dangerous rows are present and signature rows are not`() {
        val dangerous = ExportPermissionCatalog.entries
            .filter { it.kind == ExportPermKind.DANGEROUS }
            .map { it.key }
            .toSet()
        assertThat(dangerous).containsAtLeast(
            "CAMERA",
            "RECORD_AUDIO",
            "ACCESS_FINE_LOCATION",
            "ACCESS_BACKGROUND_LOCATION",
            "ACCESS_MEDIA_LOCATION",
            "READ_MEDIA_VISUAL_USER_SELECTED",
            "BLUETOOTH_SCAN",
            "NEARBY_WIFI_DEVICES",
            "UWB_RANGING",
            "RANGING",
            "ACCESS_LOCAL_NETWORK",
            "POST_NOTIFICATIONS",
            "BODY_SENSORS_BACKGROUND",
            "ACTIVITY_RECOGNITION",
            "READ_SMS",
            "PROCESS_OUTGOING_CALLS",
            "GET_ACCOUNTS",
            "ANSWER_PHONE_CALLS",
            "ADD_VOICEMAIL"
        )
        assertThat(ExportPermissionCatalog.byKey).doesNotContainKey("READ_CELL_BROADCASTS")
        assertThat(ExportPermissionCatalog.byKey).doesNotContainKey("EYE_TRACKING_COARSE")
        assertThat(ExportPermissionCatalog.byKey).doesNotContainKey("INSTALL_PACKAGES")
        assertThat(ExportPermissionCatalog.byKey).doesNotContainKey("BIND_ACCESSIBILITY_SERVICE")
        assertThat(ExportPermissionCatalog.byKey).doesNotContainKey("RECEIVE_SENSITIVE_NOTIFICATIONS")
        assertThat(ExportPermissionCatalog.byKey).doesNotContainKey("ACCESS_BIOMETRIC_SENSOR_STRENGTHS")
        assertThat(ExportPermissionCatalog.byKey).doesNotContainKey("DOWNLOAD_WITHOUT_NOTIFICATION")
        assertThat(ExportPermissionCatalog.byKey["QUERY_ALL_PACKAGES"]!!.playRestricted).isTrue()
        assertThat(ExportPermissionCatalog.byKey).containsKey("POST_PROMOTED_NOTIFICATIONS")
        assertThat(ExportPermissionCatalog.byKey).containsKey("USE_LOCATION_BUTTON")
    }

    @Test
    fun `network exports declare local network unless that row is off`() {
        val permissions = permissionsFor(ApkRuntimePermissions())
        assertThat(permissions).contains("android.permission.ACCESS_LOCAL_NETWORK")
        assertThat(permissions).contains("android.permission.INTERNET")

        val off = ApkRuntimePermissions().withExportGrant(
            "ACCESS_LOCAL_NETWORK",
            false,
            PermissionFeatureDefaults(network = true)
        )
        val names = manifest(off, PermissionFeatureDefaults(network = true))
        assertThat(names).contains("android.permission.INTERNET")
        assertThat(names).doesNotContain("android.permission.ACCESS_LOCAL_NETWORK")
    }

    @Test
    fun `query all packages is opt-in and bluetooth does not imply nearby wifi`() {
        val bluetooth = permissionsFor(ApkRuntimePermissions(bluetooth = true))
        assertThat(bluetooth).containsAtLeast(
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_ADVERTISE"
        )
        assertThat(bluetooth).doesNotContain("android.permission.NEARBY_WIFI_DEVICES")
        assertThat(bluetooth).doesNotContain("android.permission.QUERY_ALL_PACKAGES")

        val nearby = permissionsFor(ApkRuntimePermissions(nearbyWifiDevices = true))
        assertThat(nearby).contains("android.permission.NEARBY_WIFI_DEVICES")

        val query = permissionsFor(ApkRuntimePermissions(queryAllPackages = true))
        assertThat(query).contains("android.permission.QUERY_ALL_PACKAGES")
    }

    @Test
    fun `photo access does not imply partial visual access`() {
        val photos = permissionsFor(ApkRuntimePermissions(readMediaImages = true))
        assertThat(photos).contains("android.permission.READ_MEDIA_IMAGES")
        assertThat(photos).doesNotContain("android.permission.READ_MEDIA_VISUAL_USER_SELECTED")

        val partial = permissionsFor(
            ApkRuntimePermissions().withExportGrant("READ_MEDIA_VISUAL_USER_SELECTED", true)
        )
        assertThat(partial).contains("android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
    }

    @Test
    fun `background location export includes fine location`() {
        val bg = permissionsFor(ApkRuntimePermissions(backgroundLocation = true))
        assertThat(bg).contains("android.permission.ACCESS_BACKGROUND_LOCATION")
        assertThat(bg).contains("android.permission.ACCESS_FINE_LOCATION")
        assertThat(bg).contains("android.permission.ACCESS_COARSE_LOCATION")
    }

    @Test
    fun `turning one bluetooth row off keeps the other four`() {
        val split = ApkRuntimePermissions(bluetooth = true).withExportGrant("BLUETOOTH_SCAN", false)
        val names = manifest(split)
        assertThat(names).contains("android.permission.BLUETOOTH_CONNECT")
        assertThat(names).contains("android.permission.BLUETOOTH_ADVERTISE")
        assertThat(names).doesNotContain("android.permission.BLUETOOTH_SCAN")
        assertThat(names).doesNotContain("android.permission.NEARBY_WIFI_DEVICES")
    }

    @Test
    fun `camera bundle still exports the camera foreground service type`() {
        val names = manifest(ApkRuntimePermissions(camera = true))
        assertThat(names).contains("android.permission.CAMERA")
        assertThat(names).contains("android.permission.FOREGROUND_SERVICE_CAMERA")
        assertThat(names).doesNotContain("android.permission.RECORD_AUDIO")
    }

    @Test
    fun `foreground service boolean does not declare specialUse or dataSync`() {
        val names = manifest(ApkRuntimePermissions(foregroundService = true))
        assertThat(names).contains("android.permission.FOREGROUND_SERVICE")
        assertThat(names).doesNotContain("android.permission.FOREGROUND_SERVICE_SPECIAL_USE")
        assertThat(names).doesNotContain("android.permission.FOREGROUND_SERVICE_DATA_SYNC")
        assertThat(names).doesNotContain("android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK")
    }

    @Test
    fun `special use default declares only specialUse`() {
        val names = manifest(
            ApkRuntimePermissions(),
            PermissionFeatureDefaults(specialUse = true)
        )
        assertThat(names).contains("android.permission.FOREGROUND_SERVICE")
        assertThat(names).contains("android.permission.FOREGROUND_SERVICE_SPECIAL_USE")
        assertThat(names).doesNotContain("android.permission.FOREGROUND_SERVICE_DATA_SYNC")
        assertThat(names).doesNotContain("android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK")
    }

    @Test
    fun `explicit false blocks the media playback type that a feature would add`() {
        val permissions = ApkRuntimePermissions(foregroundService = true).withExportGrant(
            "FOREGROUND_SERVICE_MEDIA_PLAYBACK",
            false,
            PermissionFeatureDefaults(mediaPlayback = true)
        )
        assertThat(
            permissions.exportGranted(
                ExportPermissionCatalog.byKey.getValue("FOREGROUND_SERVICE"),
                PermissionFeatureDefaults(mediaPlayback = true)
            )
        ).isTrue()
        val names = manifest(permissions, PermissionFeatureDefaults(mediaPlayback = true))
        assertThat(names).contains("android.permission.FOREGROUND_SERVICE")
        assertThat(names).doesNotContain("android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK")
    }

    @Test
    fun `download companion stays out of the table and in the manifest`() {
        val names = manifest(ApkRuntimePermissions(), download = true)
        assertThat(names).contains("android.permission.DOWNLOAD_WITHOUT_NOTIFICATION")
        assertThat(ExportPermissionCatalog.byKey).doesNotContainKey("DOWNLOAD_WITHOUT_NOTIFICATION")
    }
}
