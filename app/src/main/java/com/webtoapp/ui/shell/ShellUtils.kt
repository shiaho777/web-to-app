package com.webtoapp.ui.shell

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.webtoapp.core.crypto.AssetDecryptor
import com.webtoapp.core.crypto.CryptoConstants
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.util.ensureWebUrlScheme
import com.webtoapp.util.normalizeExternalIntentUrl
import java.io.File

internal fun formatTimeMs(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 1000 / 60) % 60
    val hours = ms / 1000 / 60 / 60
    return if (hours > 0) {
        String.format(java.util.Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

internal fun normalizeShellTargetUrlForSecurity(rawUrl: String): String {
    val trimmed = rawUrl.trim()

    val withScheme = if (!trimmed.startsWith("http://", ignoreCase = true) &&
                          !trimmed.startsWith("https://", ignoreCase = true)) {

        "http://$trimmed"
    } else {
        trimmed
    }
    return withScheme
}

internal fun resolveShellDeepLinkUrl(
    rawUrl: String,
    config: com.webtoapp.core.shell.ShellConfig
): String {
    val trimmed = rawUrl.trim()
    val parsed = runCatching { Uri.parse(trimmed) }.getOrNull()
    val scheme = parsed?.scheme?.lowercase()
    val allowedSchemes = config.deepLinkSchemes.map { it.lowercase() }.toSet()
    if (!scheme.isNullOrBlank() && scheme in allowedSchemes) {
        val embeddedUrl = parsed.getQueryParameter("url")
            ?: parsed.getQueryParameter("target")
            ?: parsed.getQueryParameter("redirect")
            ?: parsed.getQueryParameter("redirect_url")
            ?: parsed.getQueryParameter("returnUrl")
            ?: parsed.getQueryParameter("return_url")
        val candidate = embeddedUrl?.takeIf { it.isNotBlank() } ?: buildUrlFromCustomScheme(parsed, config.targetUrl)
        val safeCandidate = normalizeShellTargetUrlForSecurity(candidate)
        return validateDeepLinkUrl(safeCandidate, config.deepLinkHosts, config.targetUrl)
    }

    val safeUrl = normalizeShellTargetUrlForSecurity(trimmed)
    return if (config.deepLinkEnabled || config.deepLinkHosts.isNotEmpty()) {
        validateDeepLinkUrl(safeUrl, config.deepLinkHosts, config.targetUrl)
    } else {
        safeUrl
    }
}

private fun buildUrlFromCustomScheme(uri: Uri, targetUrl: String): String {
    val targetUri = runCatching { Uri.parse(normalizeShellTargetUrlForSecurity(targetUrl)) }.getOrNull()
    val base = targetUri?.buildUpon()?.encodedPath(null)?.encodedQuery(null)?.fragment(null)?.build()?.toString()?.trimEnd('/')
        ?: normalizeShellTargetUrlForSecurity(targetUrl).trimEnd('/')
    val hostPart = uri.host?.takeIf { it.isNotBlank() }
    val pathPart = uri.encodedPath?.takeIf { it.isNotBlank() } ?: ""
    val queryPart = uri.encodedQuery?.takeIf { it.isNotBlank() }?.let { "?$it" } ?: ""
    val fragmentPart = uri.encodedFragment?.takeIf { it.isNotBlank() }?.let { "#$it" } ?: ""
    val encodedPath = buildString {
        if (!hostPart.isNullOrBlank() && hostPart != "oauth-return") {
            append("/")
            append(Uri.encode(hostPart))
        }
        append(pathPart)
    }.ifBlank { "/" }
    return base + encodedPath + queryPart + fragmentPart
}

internal fun buildPackagedHtmlShellBaseUrl(packageName: String): String {
    val stablePort = com.webtoapp.core.webview.LocalHttpServer.stablePortForPackageName(packageName)
    return "http://127.0.0.1:$stablePort"
}

internal fun buildPackagedHtmlShellEntryUrl(packageName: String, entryFile: String): String {
    val normalizedEntry = entryFile.removePrefix("/").ifBlank { "index.html" }
    return "${buildPackagedHtmlShellBaseUrl(packageName)}/${android.net.Uri.encode(normalizedEntry, "/")}"
}

internal fun buildPackagedHtmlFileSchemeEntryUrl(entryFile: String): String {
    val normalizedEntry = entryFile.removePrefix("/").ifBlank { "index.html" }
    return "file:///android_asset/html/${android.net.Uri.encode(normalizedEntry, "/")}"
}

internal fun validateDeepLinkUrl(url: String, allowedHosts: List<String>, targetUrl: String): String {
    if (allowedHosts.isEmpty()) return url

    val urlHost = try {
        java.net.URL(url).host?.lowercase()
    } catch (e: Exception) {
        AppLogger.w("ShellActivity", "Invalid deep link URL: $url")
        return targetUrl
    }

    if (urlHost.isNullOrBlank()) {
        AppLogger.w("ShellActivity", "Deep link URL has no host: $url")
        return targetUrl
    }

    val configHost = try {
        java.net.URL(normalizeShellTargetUrlForSecurity(targetUrl)).host?.lowercase()
    } catch (e: Exception) { null }

    val allAllowed = buildSet {
        addAll(allowedHosts.map { it.lowercase() })
        configHost?.let { add(it) }
    }

    val isAllowed = allAllowed.any { allowedHost ->
        urlHost == allowedHost || urlHost.endsWith(".$allowedHost")
    }

    if (!isAllowed) {
        AppLogger.w("ShellActivity", "Deep link URL host '$urlHost' not in allowed list: $allAllowed, redirecting to target URL")
        return targetUrl
    }

    return url
}

internal fun normalizeExternalUrlForIntent(rawUrl: String): String {
    val safeUrl = normalizeExternalIntentUrl(rawUrl)
    if (safeUrl.isEmpty()) {
        AppLogger.w("ShellActivity", "Blocked invalid or dangerous external URL: $rawUrl")
        return ""
    }
    return normalizeShellTargetUrlForSecurity(safeUrl)
}

internal fun shouldReextractAssets(marker: File, expectedToken: String): Boolean {
    if (!marker.exists()) return true
    return try {
        marker.readText() != expectedToken
    } catch (_: Exception) {
        true
    }
}

internal fun writeExtractionMarker(marker: File, token: String) {
    marker.parentFile?.mkdirs()
    marker.writeText(token)
}

internal fun buildExtractionToken(
    context: Context,
    scope: String,
    configVersionCode: Int,
    extra: String = ""
): String {
    val packageInfo = try {
        val pm = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, 0)
        }
    } catch (_: Exception) {
        null
    }

    val apkVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo?.longVersionCode ?: 0L
    } else {
        @Suppress("DEPRECATION")
        (packageInfo?.versionCode ?: 0).toLong()
    }
    val apkLastUpdate = packageInfo?.lastUpdateTime ?: 0L

    return listOf(
        scope,
        "cfg=$configVersionCode",
        "apkVer=$apkVersionCode",
        "apkUpdated=$apkLastUpdate",
        "extra=$extra"
    ).joinToString("|")
}

internal fun extractAssetsRecursive(context: Context, assetPath: String, destDir: File) {
    extractAssetsRecursive(context, assetPath, destDir, AssetDecryptor(context))
}

private fun extractAssetsRecursive(
    context: Context,
    assetPath: String,
    destDir: File,
    decryptor: AssetDecryptor
) {
    AppLogger.d("extractAssets", "提取: assetPath='$assetPath' -> destDir='${destDir.absolutePath}'")
    destDir.mkdirs()
    val children = context.assets.list(assetPath)
    if (children == null) {
        AppLogger.w("extractAssets", "assets.list('$assetPath') 返回 null")
        return
    }
    AppLogger.d("extractAssets", "assets.list('$assetPath') -> ${children.size} 项: ${children.take(20).joinToString()}")

    if (children.isEmpty()) {

        val parent = destDir.parentFile ?: destDir

        val leafFinalName = destDir.name.removeSuffix(CryptoConstants.ENCRYPTED_EXTENSION)
        val destFile = File(parent, leafFinalName)
        writeAssetFile(context, assetPath, destFile, decryptor)
        return
    }

    var extractedFiles = 0
    var extractedDirs = 0
    for (child in children) {
        val childAssetPath = "$assetPath/$child"

        val subList = context.assets.list(childAssetPath)
        if (subList != null && subList.isNotEmpty()) {
            extractedDirs++

            extractAssetsRecursive(context, childAssetPath, File(destDir, child), decryptor)
        } else {

            val finalName = child.removeSuffix(CryptoConstants.ENCRYPTED_EXTENSION)
            val childDest = File(destDir, finalName)
            writeAssetFile(context, childAssetPath, childDest, decryptor)
            extractedFiles++
        }
    }
    AppLogger.i("extractAssets", "'$assetPath' 提取完成: $extractedFiles 个文件, $extractedDirs 个子目录")
}

private fun writeAssetFile(
    context: Context,
    assetPath: String,
    destFile: File,
    decryptor: AssetDecryptor
) {
    val isEncrypted = assetPath.endsWith(CryptoConstants.ENCRYPTED_EXTENSION)

    if (isEncrypted) {
        try {
            val encryptedBytes = context.assets.open(assetPath).use { it.readBytes() }
            val decrypted = decryptor.decrypt(encryptedBytes)
            destFile.outputStream().use { it.write(decrypted) }
            AppLogger.d(
                "extractAssets",
                "  解密文件: $assetPath -> ${destFile.name} (${decrypted.size} bytes)"
            )
            return
        } catch (e: Exception) {
            AppLogger.e(
                "extractAssets",
                "解密 asset 失败，回退到原样拷贝: $assetPath",
                e
            )

        }
    }

    context.assets.open(assetPath).use { input ->
        destFile.outputStream().use { output ->
            val bytes = input.copyTo(output)
            AppLogger.d("extractAssets", "  文件: ${destFile.name} ($bytes bytes)")
        }
    }
}
