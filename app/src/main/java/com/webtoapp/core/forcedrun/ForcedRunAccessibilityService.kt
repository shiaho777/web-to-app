package com.webtoapp.core.forcedrun

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.webtoapp.core.logging.AppLogger
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class ForcedRunAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ForcedRunA11yService"

        @Volatile
        var isServiceRunning = false
            private set

        @Volatile
        var isForcedRunActive = false

        @Volatile
        var targetPackageName: String? = null

        @Volatile
        var targetActivityClass: String? = null

        @Volatile
        var bringBackDelay: Long = 50L

        @Volatile
        var allowedPackages: Set<String> = emptySet()

        @Volatile
        var blockBackKey: Boolean = false

        fun startForcedRun(
            packageName: String,
            activityClass: String,
            allowedPkgs: Set<String> = emptySet()
        ) {
            AppLogger.d(TAG, "Starting forced-run protection: package=$packageName, activity=$activityClass")
            targetPackageName = packageName
            targetActivityClass = activityClass
            allowedPackages = allowedPkgs + setOf(
                packageName,
                "com.android.systemui",
            )
            isForcedRunActive = true
        }

        fun stopForcedRun() {
            AppLogger.d(TAG, "Stopping forced-run protection")
            isForcedRunActive = false
            targetPackageName = null
            targetActivityClass = null
            allowedPackages = emptySet()
        }

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val serviceName = "${context.packageName}/${ForcedRunAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServices.split(':').any {
                it.equals(serviceName, ignoreCase = true) ||
                it.contains(ForcedRunAccessibilityService::class.java.simpleName, ignoreCase = true)
            }
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var lastBringBackTime = 0L
    private var consecutiveBringBacks = 0
    private val bringBackRunnable = Runnable { bringAppToFront() }

    override fun onServiceConnected() {
        super.onServiceConnected()
        AppLogger.d(TAG, "Accessibility service connected")

        isServiceRunning = true

        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                        AccessibilityEvent.TYPE_VIEW_FOCUSED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                   AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                   AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                   AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            notificationTimeout = 50
        } ?: AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            notificationTimeout = 50
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false

        val keyCode = event.keyCode

        if (blockBackKey) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                AppLogger.d(TAG, "Intercepting back key: action=${event.action}")
                return true
            }
        }

        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isForcedRunActive || event == null) return

        val eventPackage = event.packageName?.toString() ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(eventPackage, event)
            }
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                if (shouldBringBack(eventPackage)) {
                    scheduleBringBack()
                }
            }
            else -> Unit
        }
    }

    private fun handleWindowStateChanged(packageName: String, event: AccessibilityEvent) {
        val className = event.className?.toString() ?: ""

        AppLogger.i(TAG, "Window changed: package=$packageName, class=$className")

        if (shouldBringBack(packageName)) {
            AppLogger.d(TAG, "Detected app leave: $packageName, preparing to pull back")
            scheduleBringBack()
        }
    }

    private fun shouldBringBack(currentPackage: String): Boolean {
        val target = targetPackageName ?: return false

        if (currentPackage == target) {
            consecutiveBringBacks = 0
            return false
        }

        if (currentPackage in allowedPackages) return false

        val systemComponents = setOf(
            "com.android.systemui",
            "com.android.launcher",
            "com.android.launcher3",
            "com.google.android.apps.nexuslauncher"
        )

        if (currentPackage in systemComponents) {
            consecutiveBringBacks++
            return consecutiveBringBacks > 3
        }

        return true
    }

    private fun scheduleBringBack() {
        handler.removeCallbacks(bringBackRunnable)
        handler.postDelayed(bringBackRunnable, bringBackDelay)
    }

    private fun bringAppToFront() {
        val pkg = targetPackageName ?: return
        val activity = targetActivityClass ?: return

        val now = System.currentTimeMillis()
        if (now - lastBringBackTime < 100) return
        lastBringBackTime = now

        AppLogger.d(TAG, "Pulling back: package=$pkg, activity=$activity")

        try {
            val intent = Intent().apply {
                component = ComponentName(pkg, activity)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
            startActivity(intent)

            try {
                val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                val tasks = activityManager.appTasks
                for (task in tasks) {
                    val taskInfo = task.taskInfo
                    if (taskInfo.baseActivity?.packageName == pkg) {
                        task.moveToFront()
                        break
                    }
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "moveTaskToFront failed", e)
            }

        } catch (e: Exception) {
            AppLogger.e(TAG, "Pull-back failed", e)
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
                launchIntent?.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
                launchIntent?.let { startActivity(it) }
            } catch (e2: Exception) {
                AppLogger.e(TAG, "Fallback pull-back also failed", e2)
            }
        }
    }

    override fun onInterrupt() {
        AppLogger.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.d(TAG, "Accessibility service destroyed")

        isServiceRunning = false
        handler.removeCallbacksAndMessages(null)
    }
}
