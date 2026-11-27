package com.webtoapp.util

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.webview.DocumentDownloadPolicy
import com.webtoapp.core.webview.WebDownloadReferrer
import com.webtoapp.data.model.DownloadLocationMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

object DownloadHelper {

    private const val TAG = "DownloadHelper"

    private const val COPY_BUFFER_SIZE = 64 * 1024
    private val DOWNLOAD_ALLOWED_SCHEMES = setOf("http", "https")

    private val RFC5987_REGEX = Regex("""filename\*\s*=\s*([^']*)'([^']*)'(.+)""", RegexOption.IGNORE_CASE)
    private val QUOTED_FILENAME_REGEX = Regex("""filename\s*=\s*"([^"]+)""""", RegexOption.IGNORE_CASE)
    private val UNQUOTED_FILENAME_REGEX = Regex("""filename\s*=\s*([^;\s]+)""", RegexOption.IGNORE_CASE)

    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        return fileName.substringAfterLast('/').substringAfterLast('\\')
            .takeUnless { it.isBlank() || it == "." || it == ".." } ?: "download"
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
        referrer: String? = null,
        showEnhancedNotification: Boolean = true,
        saveToGallery: Boolean = true,
        onBlobDownload: ((blobUrl: String, filename: String) -> Unit)? = null,
        downloadLocationMode: DownloadLocationMode = DownloadLocationMode.SYSTEM_DOWNLOAD,
        customDownloadDirUri: String = "",
        albumName: String = ""
    ) {
        if (url.startsWith("blob:") || url.startsWith("data:")) {
            if (onBlobDownload != null) {
                Toast.makeText(context, Strings.blobDownloadProcessing, Toast.LENGTH_SHORT).show()
                onBlobDownload(url, parseFileName(url, contentDisposition, mimeType))
            } else {
                Toast.makeText(context, Strings.blobDownloadFailed, Toast.LENGTH_SHORT).show()
            }
            return
        }

        val safeUrl = sanitizeDownloadUrl(url)
        if (safeUrl.isEmpty()) {
            Toast.makeText(context, Strings.downloadFailed, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = parseFileName(safeUrl, contentDisposition, mimeType)
        val initialMime = mimeType.ifBlank {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substringAfterLast('.', "").lowercase())
                ?: "application/octet-stream"
        }
        val download = HttpDownload(safeUrl, userAgent, referrer, fileName, initialMime)
        val appContext = context.applicationContext
        val mode = if (downloadLocationMode == DownloadLocationMode.CUSTOM && customDownloadDirUri.isBlank()) {
            DownloadLocationMode.SYSTEM_DOWNLOAD
        } else {
            downloadLocationMode
        }

        if (mode == DownloadLocationMode.ASK) {
            DownloadSavePrompter.prompt(context, fileName, initialMime) { uri ->
                if (uri == null) {
                    Toast.makeText(context, Strings.downloadSaveCancelled, Toast.LENGTH_SHORT).show()
                } else {
                    downloadInApp(appContext, download, mode, customDownloadDirUri, uri)
                }
            }
            return
        }

        val gallery = mode == DownloadLocationMode.SYSTEM_DOWNLOAD &&
            saveToGallery && MediaSaver.isMediaFile(mimeType, fileName)
        val cookie = CookieManager.getInstance().getCookie(safeUrl)
        val network = appContext.getSystemService(ConnectivityManager::class.java)
        val capabilities = network?.getNetworkCapabilities(network.activeNetwork)
        val unvalidatedNetwork = capabilities != null &&
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val canSaveDirectly = mode != DownloadLocationMode.SYSTEM_DOWNLOAD ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        val adaptiveHttps = canSaveDirectly && Uri.parse(safeUrl).scheme.equals("https", ignoreCase = true)

        if (adaptiveHttps || gallery || mode == DownloadLocationMode.CUSTOM ||
            DocumentDownloadPolicy.isLoopbackUrl(safeUrl) || unvalidatedNetwork ||
            !cookie.isNullOrBlank() || WebDownloadReferrer.containsPageDetails(referrer)
        ) {
            downloadInApp(appContext, download, mode, customDownloadDirUri, gallery = gallery, albumName = albumName)
            return
        }

        val id = try {
            val request = DownloadManager.Request(Uri.parse(safeUrl)).apply {
                if (userAgent.isNotBlank()) addRequestHeader("User-Agent", userAgent)
                referrer?.let { addRequestHeader("Referer", it) }
                setNotificationVisibility(
                    if (showEnhancedNotification && canHideDownloadManagerNotification(appContext)) {
                        DownloadManager.Request.VISIBILITY_HIDDEN
                    } else {
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    }
                )
                setTitle(fileName)
                setDescription(Strings.downloadingNotificationText)
                if (mode == DownloadLocationMode.APP_PRIVATE) {
                    setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, fileName)
                } else {
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                }
                if (initialMime != "application/octet-stream") setMimeType(initialMime)
            }
            appContext.getSystemService(DownloadManager::class.java).enqueue(request)
        } catch (e: RuntimeException) {
            AppLogger.w(TAG, "System download could not be enqueued", e)
            downloadInApp(appContext, download, mode, customDownloadDirUri)
            return
        }

        if (showEnhancedNotification) DownloadNotificationManager.getInstance(appContext).trackDownload(id, fileName, initialMime)
        Toast.makeText(context, Strings.startDownload.replace("%s", fileName), Toast.LENGTH_SHORT).show()
    }

    private fun canHideDownloadManagerNotification(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION") ==
            PackageManager.PERMISSION_GRANTED

    private data class HttpDownload(
        val url: String,
        val userAgent: String,
        val referrer: String?,
        val fileName: String,
        val mimeType: String
    )

    private data class SavedDownload(val name: String, val path: String, val uri: Uri? = null)

    private fun downloadInApp(
        context: Context,
        download: HttpDownload,
        mode: DownloadLocationMode,
        customDir: String,
        chosenUri: Uri? = null,
        gallery: Boolean = false,
        albumName: String = ""
    ) {
        val notifications = DownloadNotificationManager.getInstance(context)
        val notificationId = notifications.showIndeterminateProgress(download.fileName)
        downloadScope.launch {
            try {
                val transfer = WebDownloadTransfer(
                    cookieFor = { CookieManager.getInstance().getCookie(it.toString()) },
                    statusError = Strings::httpErrorCode,
                    transferError = Strings.downloadFailed,
                    log = { AppLogger.d(TAG, "Download $notificationId $it") }
                )
                var mime = download.mimeType
                val saved = transfer.open(download.url, download.userAgent, download.referrer).use { input ->
                    val serverMime = input.mime
                    if (mime == "application/octet-stream" && serverMime.isNotBlank()) mime = serverMime
                    val apk = DownloadArtifact.isApk(download.fileName, mime)
                    if (apk && serverMime.equals("text/html", ignoreCase = true)) throw IOException(Strings.downloadFailed)
                    val checked = if (apk) DownloadArtifact.requireContent(input, Strings.notifDownloadEmptyResponse) else input
                    val source = DownloadProgressInput(checked, input.totalBytes) { bytes ->
                        notifications.updateTransferProgress(notificationId, download.fileName, bytes, input.totalBytes)
                    }
                    if (gallery) {
                        when (val result = MediaSaver.saveFromStream(context, source, download.fileName, mime, albumName)) {
                            is MediaSaver.SaveResult.Success -> SavedDownload(download.fileName, result.path, result.uri)
                            is MediaSaver.SaveResult.Error -> throw IOException(result.message)
                        }
                    } else {
                        saveDownload(context, download.fileName, mime, mode, customDir, chosenUri) { output ->
                            source.copyTo(output, COPY_BUFFER_SIZE)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, Strings.savedTo.replace("%s", saved.name), Toast.LENGTH_LONG).show()
                    notifications.showSaveComplete(saved.name, saved.path, mime, notificationId, saved.uri?.toString())
                }
            } catch (e: Exception) {
                val reason = e.message ?: Strings.unknownError
                AppLogger.w(TAG, "In-app download failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, Strings.saveFailedWithReason.replace("%s", reason), Toast.LENGTH_SHORT).show()
                    notifications.showSaveFailed(download.fileName, reason, notificationId)
                }
            }
        }
    }

    private class DownloadProgressInput(
        input: InputStream,
        private val totalBytes: Long,
        private val report: (Long) -> Unit
    ) : FilterInputStream(input) {
        private var bytes = 0L
        private var lastUpdate = 0L

        override fun read(): Int = super.read().also { onRead(if (it < 0) -1 else 1) }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
            super.read(buffer, offset, length).also(::onRead)

        private fun onRead(count: Int) {
            if (count <= 0) return
            bytes += count
            val now = SystemClock.elapsedRealtime()
            if (now - lastUpdate >= 500 || bytes == totalBytes) {
                lastUpdate = now
                report(bytes)
            }
        }
    }

    private fun saveDownload(
        context: Context,
        fileName: String,
        mime: String,
        mode: DownloadLocationMode,
        customDir: String,
        chosenUri: Uri?,
        write: (OutputStream) -> Unit
    ): SavedDownload {
        val resolver = context.contentResolver
        if (chosenUri != null) {
            val output = resolver.openOutputStream(chosenUri, "wt") ?: throw IOException(Strings.cannotWriteFile)
            output.use(write)
            return SavedDownload(fileName, chosenUri.toString(), chosenUri)
        }

        if (mode == DownloadLocationMode.CUSTOM && customDir.startsWith("content://")) {
            val directory = DocumentFile.fromTreeUri(context, Uri.parse(customDir))
            val target = directory?.createFile(mime, fileName) ?: throw IOException(Strings.cannotWriteFile)
            try {
                val output = resolver.openOutputStream(target.uri) ?: throw IOException(Strings.cannotWriteFile)
                output.use(write)
                return SavedDownload(target.name ?: fileName, target.uri.toString(), target.uri)
            } catch (e: Exception) {
                target.delete()
                throw e
            }
        }

        if (mode == DownloadLocationMode.SYSTEM_DOWNLOAD && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException(Strings.cannotWriteFile)
            try {
                val output = resolver.openOutputStream(uri) ?: throw IOException(Strings.cannotWriteFile)
                output.use(write)
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                if (resolver.update(uri, values, null, null) != 1) throw IOException(Strings.cannotWriteFile)
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
            val actualName = runCatching {
                resolver.query(uri, arrayOf(MediaStore.Downloads.DISPLAY_NAME), null, null, null)?.use {
                    if (it.moveToFirst()) it.getString(0) else null
                }
            }.getOrNull() ?: fileName
            return SavedDownload(actualName, uri.toString(), uri)
        }

        val directory = when (mode) {
            DownloadLocationMode.APP_PRIVATE -> context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            DownloadLocationMode.CUSTOM -> File(customDir)
            DownloadLocationMode.SYSTEM_DOWNLOAD -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            DownloadLocationMode.ASK -> throw IOException(Strings.cannotWriteFile)
        }
        if (!directory.isDirectory && !directory.mkdirs() && !directory.isDirectory) {
            throw IOException(Strings.cannotWriteFile)
        }
        var target = File(directory, fileName)
        val extension = if ('.' in fileName) "." + fileName.substringAfterLast('.') else ""
        val stem = fileName.removeSuffix(extension)
        var index = 1
        while (!target.createNewFile()) target = File(directory, "${stem}_${index++}$extension")
        try {
            target.outputStream().use(write)
        } catch (e: Exception) {
            target.delete()
            throw e
        }
        if (mode == DownloadLocationMode.CUSTOM) MediaSaver.scanIntoGallery(context, target, mime)
        return SavedDownload(target.name, target.absolutePath)
    }

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
