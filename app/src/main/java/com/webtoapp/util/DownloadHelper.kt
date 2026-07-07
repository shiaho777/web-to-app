package com.webtoapp.util

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.i18n.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

object DownloadHelper {

    private const val TAG = "DownloadHelper"

    private const val MAX_RETRY_COUNT = 3
    private const val RETRY_DELAY_MS = 1000L
    private val DOWNLOAD_ALLOWED_SCHEMES = setOf("http", "https")

    private val RFC5987_REGEX = Regex("""filename\*\s*=\s*([^']*)'([^']*)'(.+)""", RegexOption.IGNORE_CASE)
    private val QUOTED_FILENAME_REGEX = Regex("""filename\s*=\s*"([^"]+)""""", RegexOption.IGNORE_CASE)
    private val UNQUOTED_FILENAME_REGEX = Regex("""filename\s*=\s*([^;\s]+)""", RegexOption.IGNORE_CASE)

    private val retryCountMap = mutableMapOf<String, Int>()

    fun parseFileName(url: String, contentDisposition: String?, mimeType: String?): String {
        var fileName: String? = null

        if (!contentDisposition.isNullOrBlank()) {
            fileName = parseContentDisposition(contentDisposition)
        }

        if (fileName.isNullOrBlank()) {
            fileName = parseFileNameFromUrl(url)
        }

        if (fileName.isNullOrBlank()) {
            fileName = URLUtil.guessFileName(url, contentDisposition, mimeType) ?: "download"
        }

        fileName = ensureExtension(fileName, mimeType)

        return fileName
    }

    private fun parseContentDisposition(contentDisposition: String): String? {

        RFC5987_REGEX.find(contentDisposition)?.let { match ->
            val encoding = match.groupValues[1].ifBlank { "UTF-8" }
            val encodedName = match.groupValues[3]
            if (encodedName.isNotBlank()) {
                try {
                    return URLDecoder.decode(encodedName, encoding)
                } catch (e: UnsupportedEncodingException) {

                }
            }
        }

        QUOTED_FILENAME_REGEX.find(contentDisposition)?.let { match ->
            val name = match.groupValues[1]
            if (name.isNotBlank()) {
                return decodeFileName(name)
            }
        }

        UNQUOTED_FILENAME_REGEX.find(contentDisposition)?.let { match ->
            val name = match.groupValues[1]
            if (name.isNotBlank()) {
                return decodeFileName(name)
            }
        }

        return null
    }

    private fun decodeFileName(name: String): String {
        var decoded = name
        try {

            if (name.contains("%")) {
                decoded = URLDecoder.decode(name, "UTF-8")
            }
        } catch (e: Exception) {

        }

        return decoded.trim().removeSurrounding("\"").removeSurrounding("'")
    }

    private fun parseFileNameFromUrl(url: String): String? {
        try {
            val uri = Uri.parse(url)
            val path = uri.path ?: return null

            val lastSegment = path.substringAfterLast('/')
            if (lastSegment.isBlank()) return null

            if (lastSegment.contains('.') && !lastSegment.startsWith('.')) {
                val decoded = try {
                    URLDecoder.decode(lastSegment, "UTF-8")
                } catch (e: Exception) {
                    lastSegment
                }

                if (decoded.length <= 255 && !decoded.contains('?') && !decoded.contains('&')) {
                    return decoded
                }
            }
        } catch (e: Exception) {

        }
        return null
    }

    private fun ensureExtension(fileName: String, mimeType: String?): String {

        val lastDot = fileName.lastIndexOf('.')
        if (lastDot > 0 && lastDot < fileName.length - 1) {
            val ext = fileName.substring(lastDot + 1).lowercase()

            if (ext.length in 2..5 && ext != "bin") {
                return fileName
            }
        }

        if (!mimeType.isNullOrBlank()) {
            val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (!ext.isNullOrBlank()) {
                val baseName = if (lastDot > 0) fileName.substring(0, lastDot) else fileName
                return "$baseName.$ext"
            }
        }

        return fileName
    }

    fun handleDownload(
        context: Context,
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long,
        showEnhancedNotification: Boolean = true,
        saveToGallery: Boolean = true,
        scope: CoroutineScope? = null,
        onBlobDownload: ((blobUrl: String, filename: String) -> Unit)? = null,
        downloadLocationMode: com.webtoapp.data.model.DownloadLocationMode = com.webtoapp.data.model.DownloadLocationMode.SYSTEM_DOWNLOAD,
        customDownloadDirUri: String = ""
    ) {

        if (url.startsWith("blob:")) {
            val filename = parseFileName(url, contentDisposition, mimeType)
            if (onBlobDownload != null) {
                Toast.makeText(context, Strings.blobDownloadProcessing, Toast.LENGTH_SHORT).show()
                onBlobDownload(url, filename)
            } else {

                Toast.makeText(context, Strings.blobDownloadFailed, Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (url.startsWith("data:")) {
            val filename = parseFileName(url, contentDisposition, mimeType)
            if (onBlobDownload != null) {
                Toast.makeText(context, Strings.blobDownloadProcessing, Toast.LENGTH_SHORT).show()
                onBlobDownload(url, filename)
            } else {
                Toast.makeText(context, Strings.blobDownloadFailed, Toast.LENGTH_SHORT).show()
            }
            return
        }

        val safeUrl = sanitizeDownloadUrl(url)
        if (safeUrl.isEmpty()) {
            AppLogger.w(TAG, "Blocked unsafe download URL: $url")
            Toast.makeText(context, Strings.downloadFailed, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = parseFileName(safeUrl, contentDisposition, mimeType)

        if (saveToGallery && MediaSaver.isMediaFile(mimeType, fileName) && scope != null) {
            saveMediaToGallery(context, safeUrl, fileName, mimeType, scope)
            return
        }

        downloadWithManager(
            context, safeUrl, userAgent, contentDisposition, mimeType, showEnhancedNotification, scope = scope,
            downloadLocationMode = downloadLocationMode,
            customDownloadDirUri = customDownloadDirUri
        )
    }

    private fun saveMediaToGallery(
        context: Context,
        url: String,
        fileName: String,
        mimeType: String,
        scope: CoroutineScope
    ) {
        val mediaType = MediaSaver.getMediaType(mimeType) ?: MediaSaver.getMediaTypeByExtension(fileName)
        val typeText = when (mediaType) {
            MediaSaver.MediaType.IMAGE -> if (Strings.currentLanguage.value == com.webtoapp.core.i18n.AppLanguage.CHINESE) "图片" else "image"
            MediaSaver.MediaType.VIDEO -> if (Strings.currentLanguage.value == com.webtoapp.core.i18n.AppLanguage.CHINESE) "视频" else "video"
            else -> if (Strings.currentLanguage.value == com.webtoapp.core.i18n.AppLanguage.CHINESE) "文件" else "file"
        }

        val notificationManager = DownloadNotificationManager.getInstance(context)
        val progressNotificationId = notificationManager.showIndeterminateProgress(fileName)

        val savingMsg = when (mediaType) {
            MediaSaver.MediaType.IMAGE -> Strings.savingImageToGallery
            MediaSaver.MediaType.VIDEO -> Strings.savingVideoToGallery
            else -> Strings.savingToGallery
        }
        Toast.makeText(context, savingMsg, Toast.LENGTH_SHORT).show()

        scope.launch(Dispatchers.Main) {
            val result = MediaSaver.saveFromUrl(context, url, fileName, mimeType)

            when (result) {
                is MediaSaver.SaveResult.Success -> {
                    val savedMsg = when (mediaType) {
                        MediaSaver.MediaType.IMAGE -> Strings.imageSavedToGallery
                        MediaSaver.MediaType.VIDEO -> Strings.videoSavedToGallery
                        else -> Strings.imageSavedToGallery
                    }
                    Toast.makeText(context, savedMsg, Toast.LENGTH_SHORT).show()

                    notificationManager.showMediaSaveComplete(
                        fileName = fileName,
                        uri = result.uri,
                        mimeType = mimeType,
                        isImage = mediaType == MediaSaver.MediaType.IMAGE,
                        progressNotificationId = progressNotificationId
                    )
                }
                is MediaSaver.SaveResult.Error -> {
                    Toast.makeText(context, Strings.saveFailedWithReason.replace("%s", result.message), Toast.LENGTH_SHORT).show()
                    notificationManager.showSaveFailed(fileName, result.message, progressNotificationId)
                }
            }
        }
    }

    fun downloadWithManager(
        context: Context,
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        showEnhancedNotification: Boolean = true,
        retryOnFailure: Boolean = true,
        scope: CoroutineScope? = null,
        downloadLocationMode: com.webtoapp.data.model.DownloadLocationMode = com.webtoapp.data.model.DownloadLocationMode.SYSTEM_DOWNLOAD,
        customDownloadDirUri: String = ""
    ) {
        val safeUrl = sanitizeDownloadUrl(url)
        if (safeUrl.isEmpty()) {
            AppLogger.w(TAG, "Blocked unsafe download URL in downloadWithManager: $url")
            Toast.makeText(context, Strings.downloadFailed, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = parseFileName(safeUrl, contentDisposition, mimeType)

        if (downloadLocationMode == com.webtoapp.data.model.DownloadLocationMode.CUSTOM && customDownloadDirUri.isNotBlank()) {
            downloadInApp(context, safeUrl, userAgent, fileName, mimeType, scope, downloadLocationMode, customDownloadDirUri)
            return
        }

        val downloadId = try {
            val request = buildDownloadManagerRequest(
                context = context,
                safeUrl = safeUrl,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType,
                fileName = fileName,
                showEnhancedNotification = showEnhancedNotification,
                downloadLocationMode = downloadLocationMode
            )
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            AppLogger.e(TAG, "DownloadManager enqueue failed, falling back to in-app download", e)

            if (retryOnFailure) {
                val currentRetry = retryCountMap[safeUrl] ?: 0
                if (currentRetry < MAX_RETRY_COUNT) {
                    retryCountMap[safeUrl] = currentRetry + 1
                    Toast.makeText(
                        context,
                        "${Strings.downloadFailed}, ${Strings.retry} (${currentRetry + 1}/$MAX_RETRY_COUNT)...",
                        Toast.LENGTH_SHORT
                    ).show()

                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        downloadWithManager(context, safeUrl, userAgent, contentDisposition, mimeType, showEnhancedNotification, true, scope,
                            downloadLocationMode = downloadLocationMode,
                            customDownloadDirUri = customDownloadDirUri)
                    }, RETRY_DELAY_MS * (currentRetry + 1))
                    return
                }
            }

            retryCountMap.remove(safeUrl)
            downloadInApp(context, safeUrl, userAgent, fileName, mimeType, scope, downloadLocationMode, customDownloadDirUri)
            return
        }

        retryCountMap.remove(safeUrl)

        if (showEnhancedNotification) {
            try {
                val notificationManager = DownloadNotificationManager.getInstance(context)
                notificationManager.trackDownload(downloadId, fileName, mimeType)
            } catch (e: Exception) {
                AppLogger.w(TAG, "Download enqueued but enhanced notification tracking failed", e)
            }
        }

        Toast.makeText(context, Strings.startDownload.replace("%s", fileName), Toast.LENGTH_SHORT).show()
    }

    private fun buildDownloadManagerRequest(
        context: Context,
        safeUrl: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        fileName: String,
        showEnhancedNotification: Boolean,
        downloadLocationMode: com.webtoapp.data.model.DownloadLocationMode = com.webtoapp.data.model.DownloadLocationMode.SYSTEM_DOWNLOAD
    ): DownloadManager.Request {
        val originHeader = buildOriginHeader(safeUrl)

        return DownloadManager.Request(Uri.parse(safeUrl)).apply {
            addRequestHeader("User-Agent", userAgent)

            CookieManager.getInstance().getCookie(safeUrl)?.let { cookie ->
                if (cookie.isNotBlank()) {
                    addRequestHeader("Cookie", cookie)
                }
            }

            originHeader?.let { origin ->
                addRequestHeader("Origin", origin)
                addRequestHeader("Referer", "$origin/")
            }

            if (showEnhancedNotification && canHideDownloadManagerNotification(context)) {
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            } else {
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }
            setTitle(fileName)
            setDescription("正在下载...")

            when (downloadLocationMode) {
                com.webtoapp.data.model.DownloadLocationMode.APP_PRIVATE -> {
                    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                }
                else -> {
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                }
            }

            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
            )

            if (mimeType.isNotBlank()) {
                setMimeType(mimeType)
            }
        }
    }

    private fun canHideDownloadManagerNotification(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION"
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun downloadInApp(
        context: Context,
        url: String,
        userAgent: String,
        fileName: String,
        mimeType: String,
        scope: CoroutineScope?,
        downloadLocationMode: com.webtoapp.data.model.DownloadLocationMode = com.webtoapp.data.model.DownloadLocationMode.SYSTEM_DOWNLOAD,
        customDownloadDirUri: String = ""
    ) {
        val notificationManager = DownloadNotificationManager.getInstance(context)
        val notificationId = notificationManager.showIndeterminateProgress(fileName)

        val s = scope ?: CoroutineScope(Dispatchers.IO)
        s.launch(Dispatchers.IO) {
            var connection: java.net.HttpURLConnection? = null
            try {
                connection = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                    connectTimeout = 30000
                    readTimeout = 60000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", userAgent)
                    CookieManager.getInstance().getCookie(url)?.let { cookie ->
                        if (cookie.isNotBlank()) setRequestProperty("Cookie", cookie)
                    }
                    buildOriginHeader(url)?.let { origin ->
                        setRequestProperty("Origin", origin)
                        setRequestProperty("Referer", "$origin/")
                    }
                    instanceFollowRedirects = true
                }

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw java.io.IOException("HTTP $responseCode")
                }

                val actualMimeType = mimeType.ifBlank {
                    connection.contentType ?: "application/octet-stream"
                }
                val totalBytes = connection.contentLengthLong.takeIf { it > 0 } ?: -1L

                val isMedia = MediaSaver.isMediaFile(actualMimeType, fileName)
                val savedFile: File? = if (isMedia) {
                    connection.inputStream.use { input ->
                        val bytes = input.readBytes()
                        val result = MediaSaver.saveFromBytes(context, bytes, fileName, actualMimeType)
                        when (result) {
                            is MediaSaver.SaveResult.Success -> {
                                withContext(Dispatchers.Main) {
                                    val isImage = actualMimeType.startsWith("image/")
                                    val typeText = if (isImage) Strings.image else Strings.video
                                    Toast.makeText(context, Strings.savedToGallery.replace("%s", typeText), Toast.LENGTH_SHORT).show()
                                    notificationManager.showMediaSaveComplete(
                                        fileName = fileName,
                                        uri = result.uri,
                                        mimeType = actualMimeType,
                                        isImage = isImage,
                                        progressNotificationId = notificationId
                                    )
                                }
                                File(result.path)
                            }
                            is MediaSaver.SaveResult.Error -> {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, Strings.saveFailedWithReason.replace("%s", result.message), Toast.LENGTH_SHORT).show()
                                    notificationManager.showSaveFailed(fileName, result.message, notificationId)
                                }
                                null
                            }
                        }
                    }
                } else {
                    val targetFile = resolveDownloadTargetFile(context, fileName, downloadLocationMode, customDownloadDirUri)
                    connection.inputStream.use { input ->
                        java.io.FileOutputStream(targetFile).use { output ->
                            input.copyTo(output, bufferSize = 64 * 1024)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, Strings.savedTo.replace("%s", targetFile.name), Toast.LENGTH_LONG).show()
                        notificationManager.showSaveComplete(
                            fileName = targetFile.name,
                            filePath = targetFile.absolutePath,
                            mimeType = actualMimeType,
                            progressNotificationId = notificationId
                        )
                    }
                    targetFile
                }

                AppLogger.d(TAG, "In-app download complete: ${savedFile?.absolutePath}")
            } catch (e: Exception) {
                AppLogger.e(TAG, "In-app download failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, Strings.saveFailedWithReason.replace("%s", e.message ?: ""), Toast.LENGTH_SHORT).show()
                    notificationManager.showSaveFailed(fileName, e.message ?: Strings.unknownError, notificationId)
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun resolveDownloadTargetFile(
        context: Context,
        fileName: String,
        downloadLocationMode: com.webtoapp.data.model.DownloadLocationMode = com.webtoapp.data.model.DownloadLocationMode.SYSTEM_DOWNLOAD,
        customDownloadDirUri: String = ""
    ): File {
        val baseDir = when (downloadLocationMode) {
            com.webtoapp.data.model.DownloadLocationMode.APP_PRIVATE -> {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            }
            com.webtoapp.data.model.DownloadLocationMode.CUSTOM -> {
                if (customDownloadDirUri.isNotBlank()) {
                    File(customDownloadDirUri).also { if (!it.exists()) it.mkdirs() }
                } else {
                    getPublicDownloadsDir().also { if (!it.exists()) it.mkdirs() }
                }
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    getPublicDownloadsDir()
                } else {
                    getPublicDownloadsDir().also { if (!it.exists()) it.mkdirs() }
                }
            }
        }

        var targetFile = File(baseDir, fileName)
        var counter = 1
        val nameWithoutExt = fileName.substringBeforeLast(".")
        val ext = if (fileName.contains(".")) ".${fileName.substringAfterLast(".")}" else ""
        while (targetFile.exists()) {
            targetFile = File(baseDir, "${nameWithoutExt}_$counter$ext")
            counter++
        }
        return targetFile
    }

    @Suppress("DEPRECATION")
    private fun getPublicDownloadsDir(): File =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    fun openInBrowser(context: Context, url: String) {
        try {
            context.openUrl(url)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Operation failed", e)
            Toast.makeText(context, Strings.cannotOpenBrowser, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sanitizeDownloadUrl(rawUrl: String): String {
        val normalized = normalizeExternalIntentUrl(rawUrl)
        if (normalized.isEmpty()) return ""
        return if (isAllowedUrlScheme(normalized, DOWNLOAD_ALLOWED_SCHEMES)) normalized else ""
    }

    private fun buildOriginHeader(url: String): String? {
        return try {
            val uri = Uri.parse(url)
            val scheme = uri.scheme ?: return null
            val host = uri.host ?: return null
            if (!isAllowedUrlScheme(url, DOWNLOAD_ALLOWED_SCHEMES)) return null
            if (uri.port > 0) "$scheme://$host:${uri.port}" else "$scheme://$host"
        } catch (e: Exception) {
            null
        }
    }

    fun guessExtension(url: String, mimeType: String?): String {

        mimeType?.let {
            val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
            if (!ext.isNullOrBlank()) return ".$ext"
        }

        val path = Uri.parse(url).path ?: return ""
        val lastDot = path.lastIndexOf('.')
        if (lastDot >= 0 && lastDot < path.length - 1) {
            val ext = path.substring(lastDot)
            if (ext.length <= 5) return ext
        }

        return ""
    }
}
