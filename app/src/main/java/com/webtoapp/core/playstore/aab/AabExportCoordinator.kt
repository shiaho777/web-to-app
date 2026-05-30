package com.webtoapp.core.playstore.aab

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.playstore.aab.axml.ProtoManifestRewriter
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import java.io.File

class AabExportCoordinator(private val context: Context) {

    private val builtApkDir: File
        get() = File(context.getExternalFilesDir(null), BUILT_APKS_DIR)

    private val builtAabDir: File
        get() = File(context.getExternalFilesDir(null), BUILT_AABS_DIR)
            .apply { if (!exists()) mkdirs() }

    fun export(
        webApp: WebApp,
        onProgress: ((stage: AabExporter.Stage, percent: Int) -> Unit)? = null
    ): AabExporter.Result {

        rejectIfRequiresProcessExec(webApp)

        val sourceApk = findMostRecentApkFor(webApp)
            ?: throw NoBuiltApkException(
                "No built APK found for '${webApp.name}'. " +
                    "Please use the regular APK export flow first."
            )
        AppLogger.d(TAG, "Source APK: ${sourceApk.absolutePath} (${sourceApk.length() / 1024}KB)")

        val versionName = webApp.apkExportConfig?.customVersionName ?: "1.0"
        val safeName = sanitizeFileName(webApp.name)
        val outputAab = File(builtAabDir, "${safeName}_v${versionName}.aab")
        AppLogger.d(TAG, "Target AAB: ${outputAab.absolutePath}")

        return AabExporter(context).export(
            sourceApk = sourceApk,
            outputAab = outputAab,
            targetSdkOverride = ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK,
            onProgress = onProgress
        )
    }

    fun hasBuiltApk(webApp: WebApp): Boolean = findMostRecentApkFor(webApp) != null

    fun listExportedAabs(): List<File> {
        if (!builtAabDir.exists()) return emptyList()
        return builtAabDir.listFiles { f -> f.extension.equals("aab", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    private fun findMostRecentApkFor(webApp: WebApp): File? {
        if (!builtApkDir.exists()) return null
        val safeName = sanitizeFileName(webApp.name)

        return builtApkDir.listFiles { f ->
            f.isFile &&
                f.extension.equals("apk", ignoreCase = true) &&
                f.name.startsWith("${safeName}_")
        }
            ?.maxByOrNull { it.lastModified() }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(com.webtoapp.util.AppConstants.SANITIZE_FILENAME_REGEX, "_").take(50)
    }

    private fun rejectIfRequiresProcessExec(webApp: WebApp) {
        if (webApp.appType in PROCESS_EXEC_APP_TYPES) {
            throw ServerRuntimeAppTypeException(
                "App type ${webApp.appType} cannot be exported to AAB: it relies " +
                    "on fork+exec of binaries inside the app's data directory, " +
                    "which is blocked by SELinux on Android 10+. The AAB Play Store " +
                    "ships will fail to launch on every modern device."
            )
        }
    }

    companion object {
        private const val TAG = "AabExportCoordinator"

        private const val BUILT_APKS_DIR = "built_apks"

        private const val BUILT_AABS_DIR = "built_aabs"

        val PROCESS_EXEC_APP_TYPES: Set<AppType> = setOf(
            AppType.PHP_APP,
            AppType.NODEJS_APP,
            AppType.PYTHON_APP,
            AppType.GO_APP,
            AppType.WORDPRESS
        )
    }
}

class NoBuiltApkException(message: String) : Exception(message)

class ServerRuntimeAppTypeException(message: String) : Exception(message)
