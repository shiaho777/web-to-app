package com.webtoapp.ui.screens

import android.os.Build
import android.util.Log
import com.webtoapp.BuildConfig
import com.webtoapp.core.apkbuilder.ApkExportPreflightReport
import com.webtoapp.core.apkbuilder.BuildDiagnostic
import com.webtoapp.core.engine.EngineType
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.ApkArchitecture
import com.webtoapp.data.model.WebApp
import java.io.File
import java.io.RandomAccessFile

internal enum class BuildFailureAction {
    EXPORT_PREFLIGHT,
    BUILDER_INIT,
    BUILD_APK,
    BUILD_APK_FOR_SHARE,
    SHARE_APK_INTENT
}

internal class BuildFailureReport(
    val title: String,
    val summary: String,
    val publicDetails: String,
    localReport: () -> String
) {
    val localDetails: String by lazy(localReport)
}

private val publicPreflightFields = setOf(
    "advanced_features_off", "packageName", "icon", "permission.systemAlertWindow",
    "networkTrust", "network.cleartext", "mediaContentPath", "wordPressProjectDir",
    "phpBinary", "nodejsProjectDir", "nodeBinary", "phpAppProjectDir",
    "pythonAppProjectDir", "pythonBinary", "muslLinker", "pythonPrebundleMuslLinker",
    "goAppProjectDir", "goBinary", "customCa", "htmlFiles", "htmlEntryFile",
    "galleryItems", "multiWebSites", "multiWebProjectDir"
)

internal fun buildActionFailureReport(
    title: String,
    action: BuildFailureAction,
    webApp: WebApp,
    summary: String,
    logPath: String? = null,
    throwable: Throwable? = null,
    preflight: ApkExportPreflightReport? = null,
    diagnostic: BuildDiagnostic? = null,
    extraLines: List<String> = emptyList()
): BuildFailureReport {
    throwable?.let { AppLogger.e("BuildFailureReport", "Build action failed at ${action.name}", it) }
    val config = webApp.apkExportConfig
    val engine = EngineType.fromString(config?.engineType.orEmpty())
    val publicDetails = buildString {
        appendLine("WebToApp APK failure report")
        appendLine("hostVersion=${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("androidApi=${Build.VERSION.SDK_INT}")
        appendLine("action=${action.name}")
        appendLine("appType=${webApp.appType.name}")
        appendLine("engine=${engine.name}")
        appendLine("architecture=${(config?.architecture ?: ApkArchitecture.UNIVERSAL).name}")
        config?.customVersionCode?.let { appendLine("appVersionCode=$it") }
        config?.minSdk?.let { appendLine("requestedMinSdk=$it") }
        config?.targetSdk?.let { appendLine("requestedTargetSdk=$it") }
        diagnostic?.let {
            appendLine("stage=${it.stage.name}")
            appendLine("cause=${it.cause.name}")
        }
        preflight?.let {
            appendLine("preflightErrors=${it.errors.size}")
            appendLine("preflightWarnings=${it.warnings.size}")
            val fields = it.issues.map { issue -> issue.key.substringBefore('[') }
                .filter { field -> field in publicPreflightFields }
                .distinct()
            if (fields.isNotEmpty()) appendLine("preflightFields=${fields.joinToString(",")}")
        }
    }
    return BuildFailureReport(title, summary, publicDetails) {
        buildString {
            append(publicDetails)
            appendLine()
            appendLine(summary)
            appendLine("project=${webApp.name}")
            appendLine("source=${webApp.url}")
            diagnostic?.details?.forEach { (key, value) -> appendLine("$key=$value") }
            preflight?.issues?.forEach { appendLine(it.summary()) }
            extraLines.forEach { appendLine(it) }
            logPath?.takeIf { it.isNotBlank() }?.let {
                appendLine("buildLog=$it")
                appendLine(readBuildLogTail(it))
            }
            throwable?.let { append(Log.getStackTraceString(it)) }
        }
    }
}

private fun readBuildLogTail(path: String, maxBytes: Int = 20_000): String = try {
    val file = File(path)
    if (!file.isFile) {
        "<build log unavailable>"
    } else {
        RandomAccessFile(file, "r").use { log ->
            val offset = (log.length() - maxBytes).coerceAtLeast(0)
            log.seek(offset)
            val bytes = ByteArray((log.length() - offset).coerceIn(0, maxBytes.toLong()).toInt())
            log.readFully(bytes)
            val tail = String(bytes, Charsets.UTF_8)
            if (offset == 0L) tail else tail.substringAfter('\n', tail)
        }
    }
} catch (error: Exception) {
    AppLogger.e("BuildFailureReport", "Unable to read build log", error)
    "<build log unavailable>"
}
