package com.webtoapp.core.export

import android.content.Context
import android.os.Build
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.threadLocalCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppExporter(private val context: Context) {

    companion object {
        private const val BUFFER_SIZE = 8192

        private val gson: Gson by lazy {
            GsonBuilder().setPrettyPrinting().create()
        }

        private val dateFormat: ThreadLocal<SimpleDateFormat> = threadLocalCompat {
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
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

sealed class ExportResult {
    data class Success(val path: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}
