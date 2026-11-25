package com.webtoapp.core.apkbuilder

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.AppConstants
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object BuildMetadataWriter {

    private const val TAG = "BuildMetadata"
    private const val SCHEMA = 1

    fun write(
        context: Context,
        webApp: WebApp,
        config: ApkConfig,
        apkFile: File,
        signerType: String,
        certSha256Hex: String?,
        buildMode: String,
        buildReason: String,
        durationMs: Long,
        logPath: String?
    ): File? {
        return try {
            val now = System.currentTimeMillis()
            val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date(now))

            val hostInfo = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }.getOrNull()

            val json = JSONObject()
                .put("schema", SCHEMA)
                .put("generatedAtMs", now)
                .put("generatedAt", iso)
                .put("app", JSONObject()
                    .put("name", config.appName)
                    .put("packageName", config.packageName)
                    .put("versionCode", config.versionCode)
                    .put("versionName", config.versionName)
                    .put("appType", config.appType)
                    .put("engineType", config.engineType)
                    .put("themeType", config.themeType)
                    .put("language", config.language))
                .put("apk", JSONObject()
                    .put("fileName", apkFile.name)
                    .put("sizeBytes", apkFile.length())
                    .put("sha256", sha256Hex(apkFile)))
                .put("signing", JSONObject()
                    .put("signerType", signerType)
                    .put("perAppIdentity", webApp.apkExportConfig?.perAppSigningEnabled == true)
                    .put("certificateSha256", certSha256Hex ?: JSONObject.NULL))
                .put("build", JSONObject()
                    .put("mode", buildMode)
                    .put("reason", buildReason)
                    .put("durationMs", durationMs)
                    .put("logFile", logPath?.let { File(it).name } ?: JSONObject.NULL))
                .put("host", JSONObject()
                    .put("packageName", context.packageName)
                    .put("versionName", hostInfo?.versionName ?: JSONObject.NULL)
                    .put("versionCode", hostInfo?.longVersionCode ?: JSONObject.NULL)
                    .put("deviceSdk", android.os.Build.VERSION.SDK_INT))

            val out = File(apkFile.parentFile, apkFile.nameWithoutExtension + ".build.json")
            out.writeText(json.toString(2))
            AppLogger.i(TAG, "Build metadata written: ${out.name}")
            out
        } catch (e: Exception) {
            AppLogger.w(TAG, "Build metadata write failed (build is unaffected): ${e.message}")
            null
        }
    }

    internal fun findLegacyAutomaticPackageName(context: Context, app: WebApp): String? {
        if (app.packageName != null || ApkBuilder.customPackageName(app) != null) return null
        val packageName = ApkBuilder.generatePackageName(app.name)
        if (app.apkExportConfig?.perAppSigningEnabled == true &&
            PerAppSigningIdentity.existingFingerprint(context, packageName) != null) {
            return packageName
        }
        val prefix = app.name.replace(AppConstants.SANITIZE_FILENAME_REGEX, "_").take(50) + "_v"
        val files = resolveOutputDir(context).listFiles { file ->
            file.name.startsWith(prefix) && file.name.endsWith(".build.json")
        }.orEmpty()
        val hasMatchingBuild = files.any { file ->
            runCatching {
                if (file.length() !in 1..64_000) return@runCatching false
                val metadata = JSONObject(file.readText())
                val built = metadata.getJSONObject("app")
                val apkName = file.name.removeSuffix(".build.json")
                val hasApk = File(file.parentFile, "$apkName.APK").isFile ||
                    File(file.parentFile, "$apkName.apk").isFile
                hasApk && metadata.optLong("generatedAtMs") >= app.createdAt &&
                    built.optString("name") == app.name && built.optString("appType") == app.appType.name &&
                    built.optString("packageName") == packageName
            }.getOrDefault(false)
        }
        return packageName.takeIf { hasMatchingBuild }
    }

    private fun sha256Hex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
