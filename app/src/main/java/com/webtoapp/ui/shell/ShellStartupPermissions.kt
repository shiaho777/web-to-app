package com.webtoapp.ui.shell

import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.ExportPermissionCatalog
import com.webtoapp.data.model.requestsAtRuntime

class ShellStartupPermissions(private val activity: AppCompatActivity) {

    private val specialQueue = ArrayDeque<Intent>()
    private val separateQueue = ArrayDeque<String>()

    private val dangerousLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            AppLogger.d(TAG, "运行时权限请求结果: $result")
            launchNextSeparateOrSpecial()
        }

    private fun launchNextSeparateOrSpecial() {
        val next = separateQueue.removeFirstOrNull()
        if (next != null) {
            dangerousLauncher.launch(arrayOf(next))
        } else {
            processSpecialQueue()
        }
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
        // commit() (not apply()): the flag must survive an immediate process
        // kill after the system permission dialogs appear, otherwise every
        // cold start would re-request permissions ("dialog won't go away").
        prefs.edit().putBoolean(KEY_REQUESTED, true).commit()

        val declared = declaredPermissions()
        if (declared.isEmpty()) {
            return
        }

        buildSpecialQueue(declared, floatingWindowHandlesOverlay)

        val (batch, separate) = dangerousRequests(declared)
        separateQueue.clear()
        separateQueue.addAll(separate)
        when {
            batch.isNotEmpty() -> {
                AppLogger.i(TAG, "请求运行时权限: ${batch.joinToString()}")
                dangerousLauncher.launch(batch.toTypedArray())
            }
            separateQueue.isNotEmpty() -> {
                val next = separateQueue.removeFirst()
                AppLogger.i(TAG, "请求运行时权限: $next")
                dangerousLauncher.launch(arrayOf(next))
            }
            else -> processSpecialQueue()
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

    /**
     * Dangerous rows from the official table, gated by the API that still
     * shows a dialog. Background location and background body sensors are
     * requested one at a time after the first batch: Android rejects them
     * when they share a prompt with the foreground permission.
     */
    private fun dangerousRequests(declared: Set<String>): Pair<List<String>, List<String>> {
        val sdk = Build.VERSION.SDK_INT
        val target = runtimeTargetSdk()
        val batch = mutableListOf<String>()
        val separate = mutableListOf<String>()
        for (perm in ExportPermissionCatalog.entries) {
            if (perm.manifest !in declared) continue
            if (!perm.requestsAtRuntime(sdk, target)) continue
            if (isGranted(perm.manifest)) continue
            if (perm.requestSeparately) separate += perm.manifest else batch += perm.manifest
        }
        return batch to separate
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    private fun runtimeTargetSdk(): Int = try {
        activity.applicationInfo.targetSdkVersion
    } catch (e: Exception) {
        // Fall back to the template's own target so permission gating still works
        // if the package info read unexpectedly fails.
        AppLogger.w(TAG, "读取 targetSdkVersion 失败，按 35 处理", e)
        Build.VERSION_CODES.VANILLA_ICE_CREAM
    }

    private fun buildSpecialQueue(declared: Set<String>, floatingWindowHandlesOverlay: Boolean) {
        specialQueue.clear()
        val packageUri = Uri.parse("package:${activity.packageName}")

        if (has(declared, "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS") &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !isIgnoringBatteryOptimizations()
        ) {
            specialQueue += Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                packageUri
            )
        }

        if (!floatingWindowHandlesOverlay &&
            has(declared, "SYSTEM_ALERT_WINDOW") &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(activity)
        ) {
            specialQueue += Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri)
        }

        if (has(declared, "REQUEST_INSTALL_PACKAGES") &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !activity.packageManager.canRequestPackageInstalls()
        ) {
            specialQueue += Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri)
        }

        if (has(declared, "MANAGE_EXTERNAL_STORAGE") &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            !Environment.isExternalStorageManager()
        ) {
            specialQueue += Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, packageUri)
        }

        if (has(declared, "WRITE_SETTINGS") &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.System.canWrite(activity)
        ) {
            specialQueue += Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, packageUri)
        }

        if (has(declared, "SCHEDULE_EXACT_ALARM") &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !canScheduleExactAlarms()
        ) {
            specialQueue += Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri)
        }

        if (has(declared, "PACKAGE_USAGE_STATS") && !hasUsageAccess()) {
            specialQueue += Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, packageUri)
        }

        if (has(declared, "USE_FULL_SCREEN_INTENT") &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            !canUseFullScreenIntent()
        ) {
            specialQueue += Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, packageUri)
        }

        if (has(declared, "ACCESS_NOTIFICATION_POLICY") && !hasNotificationPolicyAccess()) {
            specialQueue += Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        }
    }

    private fun has(declared: Set<String>, key: String): Boolean {
        val manifest = ExportPermissionCatalog.byKey[key]?.manifest ?: return false
        return manifest in declared
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

    private fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = activity.getSystemService(AlarmManager::class.java) ?: return true
        return alarmManager.canScheduleExactAlarms()
    }

    private fun canUseFullScreenIntent(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        val notifications = activity.getSystemService(NotificationManager::class.java) ?: return true
        return notifications.canUseFullScreenIntent()
    }

    private fun hasNotificationPolicyAccess(): Boolean {
        val notifications = activity.getSystemService(NotificationManager::class.java) ?: return true
        return notifications.isNotificationPolicyAccessGranted
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = activity.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return true
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                activity.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                activity.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    companion object {
        private const val TAG = "ShellStartupPerms"
        private const val PREFS_NAME = "shell_startup_permissions"
        private const val KEY_REQUESTED = "initial_request_done"
    }
}
