package com.webtoapp.core.playstore.aab

import android.content.Context
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.apkbuilder.BuildResult
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.playstore.aab.axml.ProtoManifestRewriter
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import java.io.File

class AabExportCoordinator(private val context: Context) {

    private val apkBuilder: ApkBuilder by lazy { ApkBuilder(context.applicationContext) }

    private val builtApkDir: File
        get() = File(context.getExternalFilesDir(null), BUILT_APKS_DIR)

    private val builtAabDir: File
        get() = File(context.getExternalFilesDir(null), BUILT_AABS_DIR)
            .apply { if (!exists()) mkdirs() }

    suspend fun export(
        webApp: WebApp,
        onProgress: ((stage: AabExporter.Stage, percent: Int) -> Unit)? = null
    ): AabExporter.Result {

        val sourceApk = findMostRecentApkFor(webApp)
        val apkToConvert = if (sourceApk != null) {
            AppLogger.d(
                TAG,
                "Source APK: ${sourceApk.absolutePath} (${sourceApk.length() / 1024}KB)"
            )
            sourceApk
        } else {
            AppLogger.d(TAG, "No built APK found, building one on demand for '${webApp.name}'")
            onProgress?.invoke(AabExporter.Stage.BUILDING_APK, 0)
            buildApkOnDemand(webApp, onProgress)
        }

        val versionName = webApp.apkExportConfig?.customVersionName ?: "1.0"
        val safeName = sanitizeFileName(webApp.name)
        val outputAab = File(builtAabDir, "${safeName}_v${versionName}.aab")
        AppLogger.d(TAG, "Target AAB: ${outputAab.absolutePath}")

        return AabExporter(context).export(
            sourceApk = apkToConvert,
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

    private suspend fun buildApkOnDemand(
        webApp: WebApp,
        onProgress: ((stage: AabExporter.Stage, percent: Int) -> Unit)?
    ): File {
        val result = try {
            apkBuilder.buildApk(webApp) { percent, text ->
                AppLogger.d(TAG, "Build APK progress: $percent% ($text)")
                onProgress?.invoke(
                    AabExporter.Stage.BUILDING_APK,
                    (percent.coerceIn(0, 100) * BUILD_PROGRESS_WEIGHT / 100)
                )
            }
        } catch (e: Exception) {
            throw AabExportException(
                failureStage = FailureStage.BUILD_APK,
                message = "Failed to build APK on demand: ${e.message}",
                cause = e
            )
        }
        return when (result) {
            is BuildResult.Success -> result.apkFile
            is BuildResult.Error -> throw AabExportException(
                failureStage = FailureStage.BUILD_APK,
                message = "Failed to build APK on demand: ${result.message}",
                cause = null
            )
        }
    }

    companion object {
        private const val TAG = "AabExportCoordinator"

        private const val BUILT_APKS_DIR = "built_apks"

        private const val BUILT_AABS_DIR = "built_aabs"

        private const val BUILD_PROGRESS_WEIGHT = 45

        @Deprecated(
            "Use AppType.requiresProcessExec / AppType.REQUIRES_PROCESS_EXEC",
            replaceWith = ReplaceWith("AppType.REQUIRES_PROCESS_EXEC")
        )
        val PROCESS_EXEC_APP_TYPES: Set<AppType> get() = AppType.REQUIRES_PROCESS_EXEC
    }
}
