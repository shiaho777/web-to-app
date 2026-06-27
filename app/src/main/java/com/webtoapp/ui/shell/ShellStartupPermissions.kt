package com.webtoapp.ui.shell

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.webtoapp.core.logging.AppLogger

class ShellStartupPermissions(private val activity: AppCompatActivity) {

    private val specialQueue = ArrayDeque<Intent>()

    private val dangerousLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        AppLogger.d(TAG, "运行时权限请求结果: $result")
        processSpecialQueue()
    }

    private val specialLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        processSpecialQueue()
    }

    fun requestConfiguredPermissions(floatingWindowHandlesOverlay: Boolean = false) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_REQUESTED, false)) {
            return
        }
        prefs.edit().putBoolean(KEY_REQUESTED, true).apply()

        val declared = declaredPermissions()
        if (declared.isEmpty()) {
            return
        }

        buildSpecialQueue(declared, floatingWindowHandlesOverlay)

        val dangerous = buildDangerousRequestList(declared)
        if (dangerous.isNotEmpty()) {
            AppLogger.i(TAG, "请求运行时权限: ${dangerous.joinToString()}")
            dangerousLauncher.launch(dangerous.toTypedArray())
        } else {
            processSpecialQueue()
        }
    }

    private fun declaredPermissions(): Set<String> = try {
        activity.packageManager
            .getPackageInfo(activity.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions
            ?.toSet()
            ?: emptySet()
    } catch (e: Exception) {
        AppLogger.e(TAG, "读取已声明权限失败", e)
        emptySet()
    }

    private fun buildDangerousRequestList(declared: Set<String>): List<String> {
        val candidates = DANGEROUS_RUNTIME_PERMISSIONS.filter { it in declared }
        return candidates.filter { isApplicableForSdk(it) && !isGranted(it) }
    }

    private fun isApplicableForSdk(permission: String): Boolean = when (permission) {
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE ->
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        Manifest.permission.ACTIVITY_RECOGNITION ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        Manifest.permission.POST_NOTIFICATIONS ->
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        else -> true
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    private fun buildSpecialQueue(declared: Set<String>, floatingWindowHandlesOverlay: Boolean) {
        specialQueue.clear()

        if (Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS in declared &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !isIgnoringBatteryOptimizations()
        ) {
            specialQueue += Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${activity.packageName}")
            )
        }

        if (!floatingWindowHandlesOverlay &&
            Manifest.permission.SYSTEM_ALERT_WINDOW in declared &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(activity)
        ) {
            specialQueue += Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}")
            )
        }

        if (Manifest.permission.REQUEST_INSTALL_PACKAGES in declared &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !activity.packageManager.canRequestPackageInstalls()
        ) {
            specialQueue += Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${activity.packageName}")
            )
        }
    }

    private fun processSpecialQueue() {
        val intent = specialQueue.removeFirstOrNull() ?: return
        try {
            specialLauncher.launch(intent)
        } catch (e: Exception) {
            AppLogger.e(TAG, "特殊权限请求失败: ${intent.action}", e)
            processSpecialQueue()
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(activity.packageName) ?: true
    }

    companion object {
        private const val TAG = "ShellStartupPerms"
        private const val PREFS_NAME = "shell_startup_permissions"
        private const val KEY_REQUESTED = "initial_request_done"

        private val DANGEROUS_RUNTIME_PERMISSIONS = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
}
