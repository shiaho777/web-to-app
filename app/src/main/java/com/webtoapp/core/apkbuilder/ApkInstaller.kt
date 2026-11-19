package com.webtoapp.core.apkbuilder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.webtoapp.core.logging.AppLogger
import java.io.File

object ApkInstaller {

    fun canInstallFromThisApp(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
    }

    fun openUnknownSourcesSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            AppLogger.w("ApkInstaller", "Unknown-sources settings unavailable, falling back", e)
            try {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
                true
            } catch (e2: Exception) {
                AppLogger.e("ApkInstaller", "App settings unavailable", e2)
                false
            }
        }
    }

    fun install(context: Context, apkFile: File): Boolean {
        if (!canInstallFromThisApp(context)) {
            AppLogger.i("ApkInstaller", "Install blocked: unknown sources not allowed for this app")
            openUnknownSourcesSettings(context)
            return false
        }
        return try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            AppLogger.e("ApkInstaller", "Install intent failed", e)
            false
        }
    }
}
