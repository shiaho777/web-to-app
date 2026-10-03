package com.webtoapp.core.export

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.webtoapp.core.host.HostRuntimePrefs
import com.webtoapp.data.model.WebApp
import com.webtoapp.ui.webview.WebViewActivity
import com.webtoapp.util.threadLocalCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AppExporter(private val context: Context) {

    companion object {
        private const val ACTION_SHORTCUT_CREATED = "com.webtoapp.SHORTCUT_CREATED"
        private const val SHORTCUT_ICON_SIZE = 192
        private const val BUFFER_SIZE = 8192

        private val gson: Gson by lazy {
            GsonBuilder().setPrettyPrinting().create()
        }

        private val dateFormat: ThreadLocal<SimpleDateFormat> = threadLocalCompat {
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        }
    }

    fun createShortcut(webApp: WebApp): ShortcutResult {
        return try {

            val iconBitmap = prepareIconBitmap(webApp)
            val icon = if (iconBitmap != null) {
                IconCompat.createWithBitmap(iconBitmap)
            } else {
                IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon)
            }

            val separateTasks = HostRuntimePrefs.getInstance(context).isSeparateTasksEnabledBlocking()
            // The builder omits NEW_TASK when `context` is an Activity. A pinned
            // shortcut is fired by the launcher, so the flag has to be on the
            // intent itself either way.
            val launchIntent = WebViewActivity.buildLaunchIntent(
                context = context,
                separateTasks = separateTasks,
                documentUri = Uri.parse("webtoapp://webapp/${webApp.id}")
            ) {
                putExtra("app_id", webApp.id)
            }.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {

                    createShortcutApi26(webApp, icon, launchIntent)
                }
                else -> {

                    createShortcutLegacy(webApp, iconBitmap, launchIntent)
                }
            }
        } catch (e: Exception) {
            ShortcutResult.Error("创建失败: ${e.message}")
        }
    }

    private fun createShortcutApi26(
        webApp: WebApp,
        icon: IconCompat,
        launchIntent: Intent
    ): ShortcutResult {

        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {

            return tryOpenShortcutSettings() ?: ShortcutResult.Error(
                "当前启动器不支持创建快捷方式，请尝试更换默认桌面或手动授权"
            )
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "webapp_${webApp.id}")
            .setShortLabel(webApp.name.take(10))
            .setLongLabel(webApp.name.take(25))
            .setIcon(icon)
            .setIntent(launchIntent)
            .setAlwaysBadged()
            .build()

        val callbackIntent = Intent(ACTION_SHORTCUT_CREATED).apply {
            `package` = context.packageName
        }
        val successCallback = PendingIntent.getBroadcast(
            context,
            webApp.id.toInt(),
            callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val result = ShortcutManagerCompat.requestPinShortcut(
            context,
            shortcutInfo,
            successCallback.intentSender
        )

        return if (result) {
            ShortcutResult.Success
        } else {

            checkAndRequestPermission()
        }
    }

    @Suppress("DEPRECATION")
    private fun createShortcutLegacy(
        webApp: WebApp,
        iconBitmap: Bitmap?,
        launchIntent: Intent
    ): ShortcutResult {
        val shortcutIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
            putExtra(Intent.EXTRA_SHORTCUT_NAME, webApp.name)
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
            putExtra("duplicate", false)

            if (iconBitmap != null) {
                putExtra(Intent.EXTRA_SHORTCUT_ICON, iconBitmap)
            } else {
                putExtra(
                    Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(
                        context,
                        android.R.drawable.sym_def_app_icon
                    )
                )
            }
        }

        context.sendBroadcast(shortcutIntent)

        return ShortcutResult.Pending("快捷方式请求已发送，请检查桌面")
    }

    private fun prepareIconBitmap(webApp: WebApp): Bitmap? {
        webApp.iconPath?.let { path ->
            var original: Bitmap? = null
            try {
                original = when {

                    path.startsWith("/") -> {
                        val file = File(path)
                        if (file.exists()) {
                            // Output is scaled to SHORTCUT_ICON_SIZE below; cap the decode (#779).
                            com.webtoapp.util.BoundedBitmaps.decodeBoundedBitmapFile(path, 1024)
                        } else null
                    }

                    path.startsWith("file://") -> {
                        val file = File(Uri.parse(path).path ?: return null)
                        if (file.exists()) {
                            com.webtoapp.util.BoundedBitmaps.decodeBoundedBitmapFile(file.absolutePath, 1024)
                        } else null
                    }

                    else -> {
                        val uri = Uri.parse(path)
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            com.webtoapp.util.BoundedBitmaps.decodeBoundedBitmapStream(stream, 1024)
                        }
                    }
                }

                if (original != null) {

                    val scaled = Bitmap.createScaledBitmap(original, SHORTCUT_ICON_SIZE, SHORTCUT_ICON_SIZE, true)
                    if (scaled !== original) {
                        original.recycle()
                    }
                    return scaled
                } else {
                    return null
                }
            } catch (e: Exception) {

                original?.recycle()
            }
        }
        return null
    }

    private fun checkAndRequestPermission(): ShortcutResult {
        val manufacturer = Build.MANUFACTURER.lowercase()

        val message = when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> {
                "请在 设置 > 应用设置 > 应用管理 > WebToApp > 权限管理 中开启「桌面快捷方式」权限"
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                "请在 设置 > 应用 > 应用管理 > WebToApp > 权限 中开启「创建桌面快捷方式」权限"
            }
            manufacturer.contains("oppo") -> {
                "请在 设置 > 应用管理 > WebToApp > 权限 中开启「桌面快捷方式」权限"
            }
            manufacturer.contains("vivo") -> {
                "请在 i管家 > 应用管理 > 权限管理 中开启「桌面快捷方式」权限"
            }
            manufacturer.contains("meizu") -> {
                "请在 手机管家 > 权限管理 中开启「桌面快捷方式」权限"
            }
            manufacturer.contains("samsung") -> {
                "请确认桌面已解锁编辑状态，或尝试长按应用图标添加到主屏幕"
            }
            else -> {
                "创建快捷方式失败，请检查桌面设置或应用权限"
            }
        }

        return ShortcutResult.PermissionRequired(message)
    }

    private fun tryOpenShortcutSettings(): ShortcutResult? {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ShortcutResult.PermissionRequired("请在应用设置中开启快捷方式权限后重试")
        } catch (e: Exception) {
            null
        }
    }

    fun exportConfig(webApp: WebApp): ExportResult {
        return try {
            val exportDir = getExportDirectory()
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = dateFormat.get()?.format(Date()) ?: System.currentTimeMillis().toString()
            val fileName = "${webApp.name}_config_$timestamp.json"
            val file = File(exportDir, fileName)

            val exportData = AppExportData(
                version = 1,
                exportTime = System.currentTimeMillis(),
                app = webApp.toExportFormat()
            )

            FileOutputStream(file).buffered(BUFFER_SIZE).use { stream ->
                stream.write(gson.toJson(exportData).toByteArray())
            }

            ExportResult.Success(file.absolutePath)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Export failed")
        }
    }

    /**
     * Writes the source zip ([AppSourcePackager]) and returns its path.
     * Callers that should hand the file to the user open the share sheet
     * themselves; this method only produces the archive.
     */
    fun exportAsTemplate(webApp: WebApp): ExportResult {
        return try {
            ExportResult.Success(AppSourcePackager(context).pack(webApp).absolutePath)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "source export failed")
        }
    }

    private fun getExportDirectory(): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "WebToApp")
        } else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WebToApp")
        }
    }

    private fun WebApp.toExportFormat() = mapOf(
        "id" to id,
        "name" to name,
        "url" to url,
        "activationEnabled" to activationEnabled,
        "activationCodeList" to activationCodeList,
        "adBlockEnabled" to adBlockEnabled,
        "adBlockRules" to adBlockRules,
        "announcementEnabled" to announcementEnabled,
        "announcement" to announcement,
        "webViewConfig" to webViewConfig
    )
}

data class AppExportData(
    val version: Int,
    val exportTime: Long,
    val app: Map<String, Any?>
)

sealed class ShortcutResult {

    data object Success : ShortcutResult()

    data class Pending(val message: String) : ShortcutResult()

    data class PermissionRequired(val message: String) : ShortcutResult()

    data class Error(val message: String) : ShortcutResult()
}

sealed class ExportResult {
    data class Success(val path: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}
