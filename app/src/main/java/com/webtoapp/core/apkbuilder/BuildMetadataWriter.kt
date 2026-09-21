package com.webtoapp.core.apkbuilder

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.WebApp
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Machine-readable release metadata, written as a `<name>.build.json` sidecar next to every
 * successfully built APK.
 *
 * The human-readable build log already records the same facts; the sidecar exists for
 * tooling — a script (or a future "verify an APK" screen) can answer "which host version /
 * certificate / config produced this file" without parsing log text. The file is a pure
 * sidecar: nothing in the build reads it back, so a write failure is logged and ignored.
 */
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
