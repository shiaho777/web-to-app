package com.webtoapp.core.playstore.aab

import android.content.Context
import com.webtoapp.core.apkbuilder.ApkBuilder
import com.webtoapp.core.host.AdvancedAppTypes
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.apkbuilder.BuildResult
import com.webtoapp.core.apkbuilder.resolveOutputDir
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.playstore.aab.axml.ProtoManifestRewriter
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import org.json.JSONObject
import java.io.File

class AabExportCoordinator(private val context: Context) {

    private val apkBuilder: ApkBuilder by lazy { ApkBuilder(context.applicationContext) }

    private val builtApkDir: File
        get() = resolveOutputDir(context)

    private val builtAabDir: File
        get() = File(context.getExternalFilesDir(null), BUILT_AABS_DIR)
            .apply { if (!exists()) mkdirs() }

    suspend fun export(
        webApp: WebApp,
        onProgress: ((stage: AabExporter.Stage, percent: Int) -> Unit)? = null
    ): AabExporter.Result {

        if (!AdvancedAppTypes.isUsable(context, webApp.appType)) {
            throw AabExportException(
                FailureStage.BUILD_APK,
                Strings.advancedFeaturesOff
            )
        }

        if (webApp.appType.requiresProcessExec) {
            throw AabExportException(
                FailureStage.BUILD_APK,
                "${webApp.appType.name} apps cannot be exported as Play AABs: the required " +
                    "targetSdk rewrite (>= 29) enables W^X, which blocks the bundled runtime binaries."
            )
        }

        onProgress?.invoke(AabExporter.Stage.BUILDING_APK, 0)
        val buildResult = buildApkOnDemand(webApp, onProgress)
        val apkToConvert = buildResult.apkFile
        AppLogger.d(
            TAG,
            "Source APK: ${apkToConvert.absolutePath} " +
                "(${apkToConvert.length() / 1024}KB, mode=${buildResult.buildMode})"
        )

        val versionName = builtVersionName(buildResult)
            ?: webApp.apkExportConfig?.customVersionName?.takeIf { it.isNotBlank() }
            ?: DEFAULT_VERSION_NAME
        val safeName = sanitizeFileName(webApp.name)
        val outputAab = File(builtAabDir, "${safeName}_v${versionName}.aab")
        AppLogger.d(TAG, "Target AAB: ${outputAab.absolutePath}")

        val signingIdentity = if (webApp.apkExportConfig?.perAppSigningEnabled == true) {
            com.webtoapp.core.apkbuilder.PerAppSigningIdentity
                .identityFor(
                    context,
                    checkNotNull(buildResult.packageName),
                    requireExisting = true
                )
                .toSigningIdentity()
        } else null

        return AabExporter(context, progressStart = BUILD_PROGRESS_WEIGHT).export(
            sourceApk = apkToConvert,
            outputAab = outputAab,
            targetSdkOverride = ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK,
            signingIdentity = signingIdentity,
            onProgress = onProgress
        )
    }

    fun hasBuiltApk(webApp: WebApp): Boolean = findReusableApkFor(webApp) != null

    fun listExportedAabs(): List<File> {
        if (!builtAabDir.exists()) return emptyList()
        return builtAabDir.listFiles { f -> f.extension.equals("aab", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    private fun findReusableApkFor(webApp: WebApp): File? {
        if (!builtApkDir.exists()) return null
        val packageName = ApkBuilder.resolvePackageName(webApp)
        val candidates = builtApkDir.listFiles { f ->
            f.isFile && f.extension.equals("apk", ignoreCase = true)
        } ?: return null
        val installedCode = ApkBuilder.findInstalledVersionCode(context, packageName)
        return candidates
            .mapNotNull { apk -> readBuildMetadata(apk)?.let { apk to it } }
            .filter { (_, metadata) ->
                val app = metadata.optJSONObject("app") ?: return@filter false
                val builtAtMs = metadata.optLong("generatedAtMs", 0L)
                app.optString("packageName") == packageName &&
                    builtAtMs > 0L &&
                    builtAtMs >= webApp.updatedAt &&
                    (installedCode == null || app.optLong("versionCode", 0L) >= installedCode)
            }
            .maxByOrNull { it.first.lastModified() }
            ?.first
    }

    private fun readBuildMetadata(apk: File): JSONObject? {
        val sidecar = File(apk.parentFile, apk.nameWithoutExtension + BUILD_METADATA_SUFFIX)
        if (!sidecar.isFile) return null
        return runCatching { JSONObject(sidecar.readText()) }.getOrNull()
    }

    private fun builtVersionName(result: BuildResult.Success): String? {
        val metadataPath = result.metadataPath ?: return null
        return runCatching {
            JSONObject(File(metadataPath).readText())
                .optJSONObject("app")?.optString("versionName")
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(com.webtoapp.util.AppConstants.SANITIZE_FILENAME_REGEX, "_").take(50)
    }

    private suspend fun buildApkOnDemand(
        webApp: WebApp,
        onProgress: ((stage: AabExporter.Stage, percent: Int) -> Unit)?
    ): BuildResult.Success {
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
            is BuildResult.Success -> result
            is BuildResult.Error -> throw AabExportException(
                failureStage = FailureStage.BUILD_APK,
                message = "Failed to build APK on demand: ${result.message}",
                cause = null
            )
        }
    }

    companion object {
        private const val TAG = "AabExportCoordinator"

        private const val BUILT_AABS_DIR = "built_aabs"

        private const val BUILD_PROGRESS_WEIGHT = 50

        private const val BUILD_METADATA_SUFFIX = ".build.json"

        private const val DEFAULT_VERSION_NAME = "1.0.0"
    }
}
