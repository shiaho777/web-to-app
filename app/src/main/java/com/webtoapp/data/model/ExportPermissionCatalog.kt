package com.webtoapp.data.model

/**
 * Official grantable permission table for generated APKs, from the shell's
 * minSdk 23 through Android 17 (API 37).
 *
 * Included: every public dangerous permission a third-party app can request,
 * the special app-ops a third-party can send the user to Settings for, and
 * the normal permissions that exist only as the version companion of one of
 * those (storage split, bluetooth split, foreground-service types, nearby
 * devices, local network). Each row is one manifest permission.
 *
 * Excluded on purpose: signature / privileged / role / internal permissions
 * (a normal app cannot be granted them), including `RECEIVE_SENSITIVE_NOTIFICATIONS`
 * and `ACCESS_BIOMETRIC_SENSOR_STRENGTHS`; `@hide` hard-restricted permissions
 * such as `READ_CELL_BROADCASTS`; the normal-permission long tail (wallpaper,
 * multicast, hidden profiles); and public-API removals such as
 * `DOWNLOAD_WITHOUT_NOTIFICATION`. Deprecated rows stay, marked with the API
 * that deprecated them, and are not requested past that API.
 *
 * Android 16/17 companions that a third-party app can still declare are rows:
 * `ACCESS_LOCAL_NETWORK` (runtime from API 36; the public constant is API 37),
 * `POST_PROMOTED_NOTIFICATIONS` (normal|appop, API 36), and `USE_LOCATION_BUTTON`
 * (normal, API 37).
 *
 * Boolean fields on [ApkRuntimePermissions] remain the legacy bundles older
 * configs were saved with (camera still implies the camera foreground-service
 * type, and so on). [ApkRuntimePermissions.grants] records a single row the
 * user moved off that bundle. Feature defaults (local network, exact alarms,
 * biometrics, media-playback foreground type) fill in only when the row has
 * no explicit grant.
 */
enum class ExportPermKind {
    /** Runtime dialog on the API range where the platform still requests it. */
    DANGEROUS,
    /** No runtime dialog. The user grants it from a system Settings screen. */
    SPECIAL,
    /** Granted at install. Declaring it does not show a prompt. */
    NORMAL
}

enum class ExportPermGroup {
    CALENDAR,
    CAMERA,
    CONTACTS,
    LOCATION,
    MICROPHONE,
    PHONE,
    CALL_LOG,
    ACTIVITY,
    SENSORS,
    SMS,
    STORAGE,
    NEARBY,
    NOTIFICATIONS,
    SPECIAL,
    FOREGROUND,
    SYSTEM
}

data class ExportPerm(
    val key: String,
    val manifest: String,
    val group: ExportPermGroup,
    val kind: ExportPermKind,
    val addedApi: Int,
    val deprecatedApi: Int? = null,
    /** Runtime request starts at this SDK. Null means "no extra floor". */
    val requestFromApi: Int? = null,
    /** Runtime request stops at this SDK (exclusive). */
    val requestUntilApi: Int? = null,
    val playRestricted: Boolean = false,
    /** Ask in its own prompt. Background location and background sensors must not share the first batch. */
    val requestSeparately: Boolean = false,
    val legacy: (ApkRuntimePermissions) -> Boolean = { false },
    val feature: (ApkRuntimePermissions, PermissionFeatureDefaults) -> Boolean = { _, _ -> false },
    val reasonKeys: Set<String> = emptySet()
)

/**
 * What the generated app's other settings already require. These rows show as
 * on, and are written into the manifest, until the user explicitly turns that
 * one row off.
 */
data class PermissionFeatureDefaults(
    val network: Boolean = false,
    /** WebMediaPlaybackService ships. Background music only posts a notification. */
    val mediaPlayback: Boolean = false,
    /** Background run, notification polling, or the floating window. */
    val specialUse: Boolean = false,
    val scheduledExactAlarm: Boolean = false,
    val activationBiometric: Boolean = false
)

object ExportPermissionCatalog {

    private fun dangerous(
        key: String,
        group: ExportPermGroup,
        addedApi: Int,
        deprecatedApi: Int? = null,
        requestFromApi: Int? = null,
        requestUntilApi: Int? = null,
        playRestricted: Boolean = false,
        requestSeparately: Boolean = false,
        legacy: (ApkRuntimePermissions) -> Boolean = { false },
        feature: (ApkRuntimePermissions, PermissionFeatureDefaults) -> Boolean = { _, _ -> false },
        manifest: String = "android.permission.$key",
        reasonKeys: Set<String> = emptySet()
    ) = ExportPerm(
        key = key,
        manifest = manifest,
        group = group,
        kind = ExportPermKind.DANGEROUS,
        addedApi = addedApi,
        deprecatedApi = deprecatedApi,
        requestFromApi = requestFromApi,
        requestUntilApi = requestUntilApi,
        playRestricted = playRestricted,
        requestSeparately = requestSeparately,
        legacy = legacy,
        feature = feature,
        reasonKeys = reasonKeys
    )

    private fun special(
        key: String,
        group: ExportPermGroup,
        addedApi: Int,
        playRestricted: Boolean = false,
        legacy: (ApkRuntimePermissions) -> Boolean = { false },
        feature: (ApkRuntimePermissions, PermissionFeatureDefaults) -> Boolean = { _, _ -> false },
        reasonKeys: Set<String> = emptySet()
    ) = ExportPerm(
        key = key,
        manifest = "android.permission.$key",
        group = group,
        kind = ExportPermKind.SPECIAL,
        addedApi = addedApi,
        playRestricted = playRestricted,
        legacy = legacy,
        feature = feature,
        reasonKeys = reasonKeys
    )

    private fun normal(
        key: String,
        group: ExportPermGroup,
        addedApi: Int,
        deprecatedApi: Int? = null,
        playRestricted: Boolean = false,
        legacy: (ApkRuntimePermissions) -> Boolean = { false },
        feature: (ApkRuntimePermissions, PermissionFeatureDefaults) -> Boolean = { _, _ -> false },
        reasonKeys: Set<String> = emptySet()
    ) = ExportPerm(
        key = key,
        manifest = "android.permission.$key",
        group = group,
        kind = ExportPermKind.NORMAL,
        addedApi = addedApi,
        deprecatedApi = deprecatedApi,
        playRestricted = playRestricted,
        legacy = legacy,
        feature = feature,
        reasonKeys = reasonKeys
    )

    val entries: List<ExportPerm> = listOf(
        // Calendar
        dangerous("READ_CALENDAR", ExportPermGroup.CALENDAR, 1, legacy = { it.readCalendar }),
        dangerous("WRITE_CALENDAR", ExportPermGroup.CALENDAR, 1, legacy = { it.writeCalendar }),

        // Camera
        dangerous("CAMERA", ExportPermGroup.CAMERA, 1, legacy = { it.camera }, reasonKeys = setOf("camera")),

        // Contacts
        dangerous("READ_CONTACTS", ExportPermGroup.CONTACTS, 1, legacy = { it.readContacts }),
        dangerous("WRITE_CONTACTS", ExportPermGroup.CONTACTS, 1, legacy = { it.writeContacts }),
        dangerous("GET_ACCOUNTS", ExportPermGroup.CONTACTS, 1),

        // Location. Fine and coarse are one legacy boolean; background is its own.
        dangerous("ACCESS_COARSE_LOCATION", ExportPermGroup.LOCATION, 1,
            legacy = { it.location || it.backgroundLocation }, reasonKeys = setOf("location")),
        dangerous("ACCESS_FINE_LOCATION", ExportPermGroup.LOCATION, 1,
            legacy = { it.location || it.backgroundLocation }, reasonKeys = setOf("location")),
        dangerous("ACCESS_BACKGROUND_LOCATION", ExportPermGroup.LOCATION, 29,
            requestFromApi = 29, requestSeparately = true,
            legacy = { it.backgroundLocation }, reasonKeys = setOf("location")),
        dangerous("ACCESS_MEDIA_LOCATION", ExportPermGroup.LOCATION, 29, requestFromApi = 29),
        // API 37. Install-time. The system location button grants a temporary fix on tap.
        normal("USE_LOCATION_BUTTON", ExportPermGroup.LOCATION, 37),

        // Microphone. RECORD_AUDIO is the dangerous permission; modifying audio settings is normal.
        dangerous("RECORD_AUDIO", ExportPermGroup.MICROPHONE, 1, legacy = { it.microphone }),
        normal("MODIFY_AUDIO_SETTINGS", ExportPermGroup.MICROPHONE, 1, legacy = { it.microphone }),

        // Phone
        dangerous("READ_PHONE_STATE", ExportPermGroup.PHONE, 1, legacy = { it.readPhoneState }),
        normal("READ_BASIC_PHONE_STATE", ExportPermGroup.PHONE, 33),
        dangerous("READ_PHONE_NUMBERS", ExportPermGroup.PHONE, 26, requestFromApi = 26),
        dangerous("CALL_PHONE", ExportPermGroup.PHONE, 1, legacy = { it.callPhone }),
        dangerous("ANSWER_PHONE_CALLS", ExportPermGroup.PHONE, 26, requestFromApi = 26),
        dangerous(
            "ADD_VOICEMAIL", ExportPermGroup.PHONE, 14,
            manifest = "com.android.voicemail.permission.ADD_VOICEMAIL"
        ),
        dangerous("USE_SIP", ExportPermGroup.PHONE, 9),
        dangerous("ACCEPT_HANDOVER", ExportPermGroup.PHONE, 28, requestFromApi = 28),

        // Call log. PROCESS_OUTGOING_CALLS was deprecated in API 29 (role-based call redirection).
        dangerous("READ_CALL_LOG", ExportPermGroup.CALL_LOG, 16, playRestricted = true, legacy = { it.readCallLog }),
        dangerous("WRITE_CALL_LOG", ExportPermGroup.CALL_LOG, 16, playRestricted = true, legacy = { it.writeCallLog }),
        dangerous("PROCESS_OUTGOING_CALLS", ExportPermGroup.CALL_LOG, 1,
            deprecatedApi = 29, requestUntilApi = 29, playRestricted = true,
            legacy = { it.processOutgoingCalls }),

        // Activity recognition (its own group since API 29)
        dangerous("ACTIVITY_RECOGNITION", ExportPermGroup.ACTIVITY, 29,
            requestFromApi = 29, legacy = { it.activityRecognition }),

        // Sensors. The legacy body-sensors switch also declared the background and high-rate companions.
        dangerous("BODY_SENSORS", ExportPermGroup.SENSORS, 20, legacy = { it.bodySensors }),
        dangerous("BODY_SENSORS_BACKGROUND", ExportPermGroup.SENSORS, 33,
            requestFromApi = 33, requestSeparately = true, legacy = { it.bodySensors }),
        normal("HIGH_SAMPLING_RATE_SENSORS", ExportPermGroup.SENSORS, 31, legacy = { it.bodySensors }),

        // SMS. All five are Play-restricted.
        dangerous("SEND_SMS", ExportPermGroup.SMS, 1, playRestricted = true, legacy = { it.sendSms }),
        dangerous("RECEIVE_SMS", ExportPermGroup.SMS, 1, playRestricted = true, legacy = { it.receiveSms }),
        dangerous("READ_SMS", ExportPermGroup.SMS, 1, playRestricted = true, legacy = { it.readSms }),
        dangerous("RECEIVE_WAP_PUSH", ExportPermGroup.SMS, 1, playRestricted = true),
        dangerous("RECEIVE_MMS", ExportPermGroup.SMS, 1, playRestricted = true),

        // Storage and media. Legacy external storage is not requested from API 33.
        // Partial photo access is its own API 34 permission, not a side effect of images.
        dangerous("READ_EXTERNAL_STORAGE", ExportPermGroup.STORAGE, 16,
            requestUntilApi = 33, legacy = { it.readExternalStorage }),
        dangerous("WRITE_EXTERNAL_STORAGE", ExportPermGroup.STORAGE, 4,
            requestUntilApi = 33, legacy = { it.writeExternalStorage },
            reasonKeys = setOf("writeExternalStorage")),
        dangerous("READ_MEDIA_IMAGES", ExportPermGroup.STORAGE, 33,
            requestFromApi = 33, legacy = { it.readMediaImages }),
        dangerous("READ_MEDIA_VIDEO", ExportPermGroup.STORAGE, 33,
            requestFromApi = 33, legacy = { it.readMediaVideo }),
        dangerous("READ_MEDIA_AUDIO", ExportPermGroup.STORAGE, 33,
            requestFromApi = 33, legacy = { it.readMediaAudio }),
        dangerous("READ_MEDIA_VISUAL_USER_SELECTED", ExportPermGroup.STORAGE, 34, requestFromApi = 34),
        special("MANAGE_EXTERNAL_STORAGE", ExportPermGroup.STORAGE, 30, playRestricted = true),

        // Nearby devices. Bluetooth's legacy switch is the pre-12 normal permissions
        // plus the three API 31 runtime permissions. Nearby Wi-Fi is not part of it.
        normal("BLUETOOTH", ExportPermGroup.NEARBY, 1, legacy = { it.bluetooth }),
        normal("BLUETOOTH_ADMIN", ExportPermGroup.NEARBY, 1, legacy = { it.bluetooth }),
        dangerous("BLUETOOTH_SCAN", ExportPermGroup.NEARBY, 31, requestFromApi = 31, legacy = { it.bluetooth }),
        dangerous("BLUETOOTH_CONNECT", ExportPermGroup.NEARBY, 31, requestFromApi = 31, legacy = { it.bluetooth }),
        dangerous("BLUETOOTH_ADVERTISE", ExportPermGroup.NEARBY, 31, requestFromApi = 31, legacy = { it.bluetooth }),
        dangerous("UWB_RANGING", ExportPermGroup.NEARBY, 31, requestFromApi = 31),
        dangerous("NEARBY_WIFI_DEVICES", ExportPermGroup.NEARBY, 33,
            requestFromApi = 33, legacy = { it.nearbyWifiDevices }),
        dangerous("RANGING", ExportPermGroup.NEARBY, 36, requestFromApi = 36),
        normal("NFC", ExportPermGroup.NEARBY, 9, legacy = { it.nfc }),
        dangerous(
            "ACCESS_LOCAL_NETWORK", ExportPermGroup.NEARBY, 36, requestFromApi = 36,
            feature = { _, defaults -> defaults.network }
        ),

        // Notifications
        dangerous("POST_NOTIFICATIONS", ExportPermGroup.NOTIFICATIONS, 33,
            requestFromApi = 33, legacy = { it.notifications }, reasonKeys = setOf("notifications")),
        // normal|appop, required in addition to POST_NOTIFICATIONS. No runtime dialog.
        normal("POST_PROMOTED_NOTIFICATIONS", ExportPermGroup.NOTIFICATIONS, 36),
        special("USE_FULL_SCREEN_INTENT", ExportPermGroup.NOTIFICATIONS, 29),
        normal("ACCESS_NOTIFICATION_POLICY", ExportPermGroup.NOTIFICATIONS, 23),

        // Special app access and the normal permissions that open a Settings screen.
        special("SYSTEM_ALERT_WINDOW", ExportPermGroup.SPECIAL, 1,
            legacy = { it.systemAlertWindow }, reasonKeys = setOf("systemAlertWindow")),
        special("WRITE_SETTINGS", ExportPermGroup.SPECIAL, 1),
        special("REQUEST_INSTALL_PACKAGES", ExportPermGroup.SPECIAL, 23,
            playRestricted = true, legacy = { it.installPackages }),
        special("PACKAGE_USAGE_STATS", ExportPermGroup.SPECIAL, 23, playRestricted = true),
        special("SCHEDULE_EXACT_ALARM", ExportPermGroup.SPECIAL, 31,
            feature = { _, defaults -> defaults.scheduledExactAlarm }),
        normal("USE_EXACT_ALARM", ExportPermGroup.SPECIAL, 33,
            playRestricted = true,
            feature = { _, defaults -> defaults.scheduledExactAlarm }),
        normal("REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", ExportPermGroup.SPECIAL, 23,
            legacy = { it.requestIgnoreBatteryOptimizations },
            reasonKeys = setOf("requestIgnoreBatteryOptimizations")),
        normal("QUERY_ALL_PACKAGES", ExportPermGroup.SPECIAL, 30,
            playRestricted = true, legacy = { it.queryAllPackages }),

        // Each Play foreground-service type follows the service that actually
        // ships. DATA_SYNC is host-only (ApkBuildService, AgentService) and is
        // never a feature default — declaring it makes Play ask for a video
        // of a service the generated app does not have (#1313).
        normal("FOREGROUND_SERVICE", ExportPermGroup.FOREGROUND, 28,
            legacy = { it.foregroundService },
            feature = { _, defaults -> defaults.specialUse || defaults.mediaPlayback },
            reasonKeys = setOf("foregroundService")),
        normal("FOREGROUND_SERVICE_SPECIAL_USE", ExportPermGroup.FOREGROUND, 34,
            feature = { _, defaults -> defaults.specialUse },
            reasonKeys = setOf("foregroundService")),
        normal("FOREGROUND_SERVICE_DATA_SYNC", ExportPermGroup.FOREGROUND, 34),
        normal("FOREGROUND_SERVICE_MEDIA_PLAYBACK", ExportPermGroup.FOREGROUND, 34,
            feature = { _, defaults -> defaults.mediaPlayback },
            reasonKeys = setOf("foregroundService")),
        normal("FOREGROUND_SERVICE_LOCATION", ExportPermGroup.FOREGROUND, 34,
            legacy = { it.location }, reasonKeys = setOf("location")),
        normal("FOREGROUND_SERVICE_CAMERA", ExportPermGroup.FOREGROUND, 34,
            legacy = { it.camera }, reasonKeys = setOf("camera")),
        normal("FOREGROUND_SERVICE_MICROPHONE", ExportPermGroup.FOREGROUND, 34,
            legacy = { it.microphone }),
        normal("FOREGROUND_SERVICE_CONNECTED_DEVICE", ExportPermGroup.FOREGROUND, 34),
        normal("FOREGROUND_SERVICE_MEDIA_PROJECTION", ExportPermGroup.FOREGROUND, 34),
        normal("FOREGROUND_SERVICE_PHONE_CALL", ExportPermGroup.FOREGROUND, 34),
        normal("FOREGROUND_SERVICE_HEALTH", ExportPermGroup.FOREGROUND, 34),
        normal("FOREGROUND_SERVICE_REMOTE_MESSAGING", ExportPermGroup.FOREGROUND, 34),
        normal("FOREGROUND_SERVICE_SYSTEM_EXEMPTED", ExportPermGroup.FOREGROUND, 34),
        normal("FOREGROUND_SERVICE_MEDIA_PROCESSING", ExportPermGroup.FOREGROUND, 35),

        // Install-time companions the shell already exposes, each as its own row.
        normal("WAKE_LOCK", ExportPermGroup.SYSTEM, 1,
            legacy = { it.wakeLock }, reasonKeys = setOf("wakeLock")),
        normal("RECEIVE_BOOT_COMPLETED", ExportPermGroup.SYSTEM, 1,
            legacy = { it.bootCompleted }, reasonKeys = setOf("bootCompleted")),
        normal("VIBRATE", ExportPermGroup.SYSTEM, 1, legacy = { it.vibration }),
        normal("REQUEST_DELETE_PACKAGES", ExportPermGroup.SYSTEM, 26, legacy = { it.requestDeletePackages }),
        normal("USE_BIOMETRIC", ExportPermGroup.SYSTEM, 28,
            feature = { _, defaults -> defaults.activationBiometric }),
        normal("USE_FINGERPRINT", ExportPermGroup.SYSTEM, 23, deprecatedApi = 28,
            feature = { _, defaults -> defaults.activationBiometric }),
        normal("DETECT_SCREEN_CAPTURE", ExportPermGroup.SYSTEM, 34),
        normal("DETECT_SCREEN_RECORDING", ExportPermGroup.SYSTEM, 35),
        normal("ACCESS_WIFI_STATE", ExportPermGroup.SYSTEM, 1, legacy = { it.wifiState }),
        normal("CHANGE_WIFI_STATE", ExportPermGroup.SYSTEM, 1, legacy = { it.wifiState }),
        normal("CHANGE_NETWORK_STATE", ExportPermGroup.SYSTEM, 1, legacy = { it.wifiState })
    )

    val byKey: Map<String, ExportPerm> = entries.associateBy { it.key }

    init {
        check(byKey.size == entries.size) { "Duplicate export permission keys" }
    }

    /**
     * Manifest order for permissions that already have an export test pinned
     * to a sequence. Anything granted but not listed here is appended in
     * catalog order.
     */
    private val emitOrder = listOf(
        "CAMERA",
        "RECORD_AUDIO",
        "MODIFY_AUDIO_SETTINGS",
        "ACCESS_COARSE_LOCATION",
        "ACCESS_FINE_LOCATION",
        "ACCESS_BACKGROUND_LOCATION",
        "POST_NOTIFICATIONS",
        "READ_EXTERNAL_STORAGE",
        "WRITE_EXTERNAL_STORAGE",
        "READ_MEDIA_IMAGES",
        "READ_MEDIA_VIDEO",
        "READ_MEDIA_AUDIO",
        "BLUETOOTH",
        "BLUETOOTH_ADMIN",
        "BLUETOOTH_SCAN",
        "BLUETOOTH_CONNECT",
        "BLUETOOTH_ADVERTISE",
        "NFC",
        "ACCESS_WIFI_STATE",
        "CHANGE_WIFI_STATE",
        "CHANGE_NETWORK_STATE",
        "NEARBY_WIFI_DEVICES",
        "QUERY_ALL_PACKAGES",
        "BODY_SENSORS",
        "BODY_SENSORS_BACKGROUND",
        "HIGH_SAMPLING_RATE_SENSORS",
        "ACTIVITY_RECOGNITION",
        "READ_PHONE_STATE",
        "CALL_PHONE",
        "READ_CONTACTS",
        "WRITE_CONTACTS",
        "READ_CALENDAR",
        "WRITE_CALENDAR",
        "READ_SMS",
        "SEND_SMS",
        "RECEIVE_SMS",
        "READ_CALL_LOG",
        "WRITE_CALL_LOG",
        "PROCESS_OUTGOING_CALLS",
        "FOREGROUND_SERVICE",
        "FOREGROUND_SERVICE_SPECIAL_USE",
        "FOREGROUND_SERVICE_DATA_SYNC",
        "FOREGROUND_SERVICE_MEDIA_PLAYBACK",
        "WAKE_LOCK",
        "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
        "RECEIVE_BOOT_COMPLETED",
        "VIBRATE",
        "REQUEST_INSTALL_PACKAGES",
        "REQUEST_DELETE_PACKAGES",
        "SYSTEM_ALERT_WINDOW",
        "FOREGROUND_SERVICE_LOCATION",
        "FOREGROUND_SERVICE_CAMERA",
        "FOREGROUND_SERVICE_MICROPHONE",
        "SCHEDULE_EXACT_ALARM",
        "USE_EXACT_ALARM",
        "USE_BIOMETRIC",
        "USE_FINGERPRINT"
    )

    fun manifestPermissions(
        packageName: String,
        permissions: ApkRuntimePermissions,
        defaults: PermissionFeatureDefaults,
        downloadWithoutNotification: Boolean
    ): List<String> {
        val granted = entries.filter { permissions.exportGranted(it, defaults) }.map { it.key }.toSet()
        val out = linkedSetOf<String>()
        out += "$packageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
        if (defaults.network) {
            out += "android.permission.INTERNET"
            out += "android.permission.ACCESS_NETWORK_STATE"
        }
        if ("ACCESS_LOCAL_NETWORK" in granted) {
            out += "android.permission.ACCESS_LOCAL_NETWORK"
        }
        for (key in emitOrder) {
            if (key !in granted || key == "ACCESS_LOCAL_NETWORK") continue
            out += byKey.getValue(key).manifest
        }
        for (perm in entries) {
            if (perm.key in granted && perm.manifest !in out) out += perm.manifest
        }
        if (downloadWithoutNotification) {
            // Removed from the public SDK. The download manager still honors the
            // declaration, so it stays a build-time companion of downloads rather
            // than a row in the table.
            out += "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION"
        }
        return out.toList()
    }
}

private data class PermBundle(
    val primary: String?,
    val members: Set<String>,
    val isOn: (ApkRuntimePermissions) -> Boolean,
    val set: (ApkRuntimePermissions, Boolean) -> ApkRuntimePermissions
)

private val permBundles = listOf(
    PermBundle("CAMERA", setOf("CAMERA", "FOREGROUND_SERVICE_CAMERA"),
        { it.camera }, { p, v -> p.copy(camera = v) }),
    PermBundle("RECORD_AUDIO", setOf("RECORD_AUDIO", "MODIFY_AUDIO_SETTINGS", "FOREGROUND_SERVICE_MICROPHONE"),
        { it.microphone }, { p, v -> p.copy(microphone = v) }),
    PermBundle(null, setOf("ACCESS_COARSE_LOCATION", "ACCESS_FINE_LOCATION", "FOREGROUND_SERVICE_LOCATION"),
        { it.location }, { p, v -> p.copy(location = v) }),
    PermBundle(null, setOf("BLUETOOTH", "BLUETOOTH_ADMIN", "BLUETOOTH_SCAN", "BLUETOOTH_CONNECT", "BLUETOOTH_ADVERTISE"),
        { it.bluetooth }, { p, v -> p.copy(bluetooth = v) }),
    PermBundle(null, setOf("ACCESS_WIFI_STATE", "CHANGE_WIFI_STATE", "CHANGE_NETWORK_STATE"),
        { it.wifiState }, { p, v -> p.copy(wifiState = v) }),
    PermBundle("BODY_SENSORS", setOf("BODY_SENSORS", "BODY_SENSORS_BACKGROUND", "HIGH_SAMPLING_RATE_SENSORS"),
        { it.bodySensors }, { p, v -> p.copy(bodySensors = v) }),
    PermBundle("FOREGROUND_SERVICE", setOf("FOREGROUND_SERVICE"),
        { it.foregroundService }, { p, v -> p.copy(foregroundService = v) })
)

private val singleBooleans: Map<String, (ApkRuntimePermissions, Boolean) -> ApkRuntimePermissions> = mapOf(
    "POST_NOTIFICATIONS" to { p, v -> p.copy(notifications = v) },
    "READ_EXTERNAL_STORAGE" to { p, v -> p.copy(readExternalStorage = v) },
    "WRITE_EXTERNAL_STORAGE" to { p, v -> p.copy(writeExternalStorage = v) },
    "READ_MEDIA_IMAGES" to { p, v -> p.copy(readMediaImages = v) },
    "READ_MEDIA_VIDEO" to { p, v -> p.copy(readMediaVideo = v) },
    "READ_MEDIA_AUDIO" to { p, v -> p.copy(readMediaAudio = v) },
    "NFC" to { p, v -> p.copy(nfc = v) },
    "NEARBY_WIFI_DEVICES" to { p, v -> p.copy(nearbyWifiDevices = v) },
    "ACTIVITY_RECOGNITION" to { p, v -> p.copy(activityRecognition = v) },
    "READ_PHONE_STATE" to { p, v -> p.copy(readPhoneState = v) },
    "CALL_PHONE" to { p, v -> p.copy(callPhone = v) },
    "READ_CONTACTS" to { p, v -> p.copy(readContacts = v) },
    "WRITE_CONTACTS" to { p, v -> p.copy(writeContacts = v) },
    "READ_CALENDAR" to { p, v -> p.copy(readCalendar = v) },
    "WRITE_CALENDAR" to { p, v -> p.copy(writeCalendar = v) },
    "READ_SMS" to { p, v -> p.copy(readSms = v) },
    "SEND_SMS" to { p, v -> p.copy(sendSms = v) },
    "RECEIVE_SMS" to { p, v -> p.copy(receiveSms = v) },
    "READ_CALL_LOG" to { p, v -> p.copy(readCallLog = v) },
    "WRITE_CALL_LOG" to { p, v -> p.copy(writeCallLog = v) },
    "PROCESS_OUTGOING_CALLS" to { p, v -> p.copy(processOutgoingCalls = v) },
    "WAKE_LOCK" to { p, v -> p.copy(wakeLock = v) },
    "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" to { p, v -> p.copy(requestIgnoreBatteryOptimizations = v) },
    "RECEIVE_BOOT_COMPLETED" to { p, v -> p.copy(bootCompleted = v) },
    "VIBRATE" to { p, v -> p.copy(vibration = v) },
    "REQUEST_INSTALL_PACKAGES" to { p, v -> p.copy(installPackages = v) },
    "REQUEST_DELETE_PACKAGES" to { p, v -> p.copy(requestDeletePackages = v) },
    "SYSTEM_ALERT_WINDOW" to { p, v -> p.copy(systemAlertWindow = v) },
    "ACCESS_BACKGROUND_LOCATION" to { p, v -> p.copy(backgroundLocation = v) },
    "QUERY_ALL_PACKAGES" to { p, v -> p.copy(queryAllPackages = v) }
)

fun ApkRuntimePermissions.grantMap(): Map<String, Boolean> = grants ?: emptyMap()

fun ApkRuntimePermissions.exportGranted(
    perm: ExportPerm,
    defaults: PermissionFeatureDefaults = PermissionFeatureDefaults()
): Boolean {
    grantMap()[perm.key]?.let { return it }
    if (perm.legacy(this)) return true
    if (perm.feature(this, defaults)) return true
    return false
}

fun ApkRuntimePermissions.withExportGrant(
    key: String,
    enabled: Boolean,
    defaults: PermissionFeatureDefaults = PermissionFeatureDefaults()
): ApkRuntimePermissions {
    val perm = ExportPermissionCatalog.byKey[key] ?: return this
    if (exportGranted(perm, defaults) == enabled) return this

    if (enabled && key == "ACCESS_BACKGROUND_LOCATION") {
        val cleared = grantMap().toMutableMap().apply {
            remove("ACCESS_BACKGROUND_LOCATION")
            remove("ACCESS_FINE_LOCATION")
            remove("ACCESS_COARSE_LOCATION")
        }
        return copy(
            backgroundLocation = true,
            location = true,
            grants = cleared.ifEmpty { null }
        )
    }

    val single = singleBooleans[key]
    if (single != null) {
        val next = single(this, enabled)
        val base = perm.legacy(next) || perm.feature(next, defaults)
        val updated = next.grantMap().toMutableMap()
        if (enabled == base) updated.remove(key) else updated[key] = enabled
        return next.copy(grants = updated.ifEmpty { null })
    }

    if (!enabled) {
        val owning = permBundles.filter { it.isOn(this) && key in it.members }
        if (owning.any { it.primary == key }) {
            var next = this
            owning.filter { it.primary == key }.forEach { next = it.set(next, false) }
            val updated = next.grantMap().toMutableMap()
            owning.filter { it.primary == key }.forEach { bundle ->
                bundle.members.forEach { member -> updated.remove(member) }
            }
            val base = perm.legacy(next) || perm.feature(next, defaults)
            if (enabled != base) updated[key] = false
            return next.copy(grants = updated.ifEmpty { null })
        }
        if (owning.isNotEmpty()) {
            var next = this
            val siblingOn = linkedSetOf<String>()
            owning.forEach { bundle ->
                bundle.members.filter { it != key }.forEach { sibling ->
                    if (next.exportGranted(ExportPermissionCatalog.byKey.getValue(sibling), defaults)) {
                        siblingOn += sibling
                    }
                }
                next = bundle.set(next, false)
            }
            val updated = next.grantMap().toMutableMap()
            siblingOn.forEach { sibling ->
                val siblingPerm = ExportPermissionCatalog.byKey.getValue(sibling)
                val still = siblingPerm.legacy(next) || siblingPerm.feature(next, defaults)
                if (!still) updated[sibling] = true
            }
            val base = perm.legacy(next) || perm.feature(next, defaults)
            if (!base) updated.remove(key) else updated[key] = false
            return next.copy(grants = updated.ifEmpty { null })
        }
    }

    val base = perm.legacy(this) || perm.feature(this, defaults)
    val updated = grantMap().toMutableMap()
    if (enabled == base) updated.remove(key) else updated[key] = enabled
    return copy(grants = updated.ifEmpty { null })
}

/** True when this SDK should show the runtime dialog for [perm]. */
fun ExportPerm.requestsAtRuntime(sdk: Int, targetSdk: Int): Boolean {
    if (kind != ExportPermKind.DANGEROUS) return false
    if (requestFromApi != null && sdk < requestFromApi) return false
    if (requestUntilApi != null && sdk >= requestUntilApi) return false
    if (key == "POST_NOTIFICATIONS" && targetSdk < 33) return false
    return true
}
