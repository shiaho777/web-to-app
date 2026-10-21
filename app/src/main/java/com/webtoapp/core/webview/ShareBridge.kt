package com.webtoapp.core.webview

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Page → system share sheet.
 *
 * Text and files travel in one [Intent.ACTION_SEND] (or [Intent.ACTION_SEND_MULTIPLE]).
 * The user picks the target — WhatsApp, Telegram, mail, or anything else on the sheet.
 * Files arrive in chunks so a photo never has to cross the JavascriptInterface
 * binder limit as one string.
 */
class ShareBridge(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val sessions = ConcurrentHashMap<String, ShareUpload>()

    @JavascriptInterface
    fun shareText(title: String?, text: String?, url: String?) {
        val caption = joinShareCaption(text, url)
        mainHandler.post {
            launchShare(title, caption, emptyList())
        }
    }

    /**
     * One-shot file share kept for older page scripts. New shares use the
     * chunked session below so the caption and every file go out together.
     */
    @JavascriptInterface
    fun shareFile(title: String?, base64Data: String?, mimeType: String?, fileName: String?) {
        if (base64Data.isNullOrEmpty()) return
        try {
            val id = "legacy-" + UUID.randomUUID().toString()
            val upload = ShareUpload()
            val dir = shareDir(id)
            if (!upload.beginFile(dir, fileName, mimeType)) return
            var offset = 0
            while (offset < base64Data.length) {
                val end = minOf(offset + 400_000, base64Data.length)
                if (!upload.appendChunk(base64Data.substring(offset, end))) return
                offset = end
            }
            val files = upload.finish() ?: return
            mainHandler.post { launchShare(title, "", files.map { it.toShared(dir) }) }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Share file failed", e)
        }
    }

    @JavascriptInterface
    fun canShare(): Boolean = true

    @JavascriptInterface
    fun canShareFiles(): Boolean = true

    @JavascriptInterface
    fun beginShare(title: String?, text: String?, url: String?): String {
        pruneOldShares()
        val id = UUID.randomUUID().toString()
        sessions[id] = ShareUpload(title.orEmpty(), joinShareCaption(text, url))
        return id
    }

    @JavascriptInterface
    fun beginFile(sessionId: String?, name: String?, mime: String?): Boolean {
        val session = sessions[sessionId] ?: return false
        return session.beginFile(shareDir(sessionId!!), name, mime)
    }

    @JavascriptInterface
    fun appendChunk(sessionId: String?, chunk: String?): Boolean {
        val session = sessions[sessionId] ?: return false
        return session.appendChunk(chunk.orEmpty())
    }

    @JavascriptInterface
    fun finishShare(sessionId: String?): Boolean {
        val session = sessions.remove(sessionId) ?: return false
        val dir = shareDir(sessionId!!)
        val files = try {
            session.finish()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Finish share failed", e)
            null
        } ?: return false
        val shared = files.map { it.toShared(dir) }
        mainHandler.post { launchShare(session.title, session.caption, shared) }
        return true
    }

    @JavascriptInterface
    fun cancelShare(sessionId: String?) {
        sessions.remove(sessionId)
        if (!sessionId.isNullOrBlank()) {
            shareDir(sessionId).deleteRecursively()
        }
    }

    private fun shareDir(id: String): File =
        File(context.cacheDir, "shared_files/$id").apply { mkdirs() }

    private fun pruneOldShares() {
        val root = File(context.cacheDir, "shared_files")
        val cutoff = System.currentTimeMillis() - SHARE_TTL_MS
        root.listFiles()?.forEach { child ->
            if (child.lastModified() < cutoff) child.deleteRecursively()
        }
    }

    private fun launchShare(title: String?, caption: String, files: List<SharedFile>) {
        try {
            val intent = buildShareIntent(title, caption, files)
            grantRead(intent, files.map { it.uri })
            val chooser = Intent.createChooser(intent, title?.takeIf { it.isNotBlank() } ?: "Share")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.clipData?.let { chooser.clipData = it }
            context.startActivity(chooser)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Share failed", e)
        }
    }

    private fun buildShareIntent(title: String?, caption: String, files: List<SharedFile>): Intent {
        val intent = when {
            files.isEmpty() -> Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, caption)
            }
            files.size == 1 -> Intent(Intent.ACTION_SEND).apply {
                val file = files[0]
                type = file.mime
                putExtra(Intent.EXTRA_STREAM, file.uri)
                if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newUri(context.contentResolver, file.name, file.uri)
            }
            else -> Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = commonShareMime(files.map { it.mime })
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(files.map { it.uri }))
                if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val first = files[0]
                val clip = ClipData.newUri(context.contentResolver, first.name, first.uri)
                files.drop(1).forEach { clip.addItem(ClipData.Item(it.uri)) }
                clipData = clip
            }
        }
        if (!title.isNullOrBlank()) intent.putExtra(Intent.EXTRA_SUBJECT, title)
        return intent
    }

    private fun grantRead(intent: Intent, uris: List<Uri>) {
        if (uris.isEmpty()) return
        val resolved = try {
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        } catch (e: Exception) {
            AppLogger.w(TAG, "queryIntentActivities failed: ${e.message}")
            emptyList()
        }
        for (info in resolved) {
            val pkg = info.activityInfo?.packageName ?: continue
            for (uri in uris) {
                try {
                    context.grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    AppLogger.w(TAG, "grantUriPermission $pkg failed: ${e.message}")
                }
            }
        }
    }

    private fun PreparedFile.toShared(dir: File): SharedFile {
        val file = File(dir, storedName)
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )
        return SharedFile(name, mime, uri)
    }

    private data class SharedFile(val name: String, val mime: String, val uri: Uri)

    private companion object {
        const val TAG = "ShareBridge"
        const val SHARE_TTL_MS = 60L * 60L * 1000L
    }
}

internal fun joinShareCaption(text: String?, url: String?): String = buildString {
    if (!text.isNullOrBlank()) append(text.trim())
    if (!url.isNullOrBlank()) {
        if (isNotEmpty()) append('\n')
        append(url.trim())
    }
}

internal fun shareMime(fileName: String?, declared: String?): String {
    val given = declared?.substringBefore(';')?.trim()?.lowercase().orEmpty()
    if (given.isNotEmpty() && given != "application/octet-stream") return given
    val ext = fileName
        ?.takeIf { '.' in it }
        ?.substringAfterLast('.')
        ?.lowercase()
        .orEmpty()
    if (ext.isEmpty()) return if (given.isNotEmpty()) given else "application/octet-stream"
    return mimeTypeForFileExtension(ext)
}

/**
 * Same extension table the file chooser uses, so a shared document is labeled
 * as that document instead of a generic file.
 */
internal fun mimeTypeForFileExtension(extension: String): String {
    val ext = extension.removePrefix(".").lowercase()
    return when (ext) {
        "json" -> "application/json"
        "xml" -> "application/xml"
        "csv" -> "text/csv"
        "txt" -> "text/plain"
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "ppt" -> "application/vnd.ms-powerpoint"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "rtf" -> "application/rtf"
        "odt" -> "application/vnd.oasis.opendocument.text"
        "ods" -> "application/vnd.oasis.opendocument.spreadsheet"
        "html", "htm" -> "text/html"
        "css" -> "text/css"
        "js" -> "application/javascript"
        "ts" -> "application/typescript"
        "py" -> "text/x-python"
        "java" -> "text/x-java-source"
        "kt" -> "text/x-kotlin"
        "yaml", "yml" -> "application/x-yaml"
        "toml" -> "application/toml"
        "ini", "conf", "cfg" -> "text/plain"
        "md" -> "text/markdown"
        "log" -> "text/plain"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "svg" -> "image/svg+xml"
        "bmp" -> "image/bmp"
        "heic", "heif" -> "image/heif"
        "ico" -> "image/x-icon"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "ogg" -> "audio/ogg"
        "flac" -> "audio/flac"
        "aac" -> "audio/aac"
        "m4a" -> "audio/mp4"
        "mp4" -> "video/mp4"
        "webm" -> "video/webm"
        "mkv" -> "video/x-matroska"
        "avi" -> "video/x-msvideo"
        "mov" -> "video/quicktime"
        "3gp" -> "video/3gpp"
        "zip" -> "application/zip"
        "tar" -> "application/x-tar"
        "gz", "gzip" -> "application/gzip"
        "rar" -> "application/vnd.rar"
        "7z" -> "application/x-7z-compressed"
        "apk" -> "application/vnd.android.package-archive"
        "wasm" -> "application/wasm"
        "sql" -> "application/sql"
        "db", "sqlite" -> "application/x-sqlite3"
        else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: "application/octet-stream"
    }
}

internal fun commonShareMime(mimes: List<String>): String {
    if (mimes.isEmpty()) return "*/*"
    val first = mimes.first()
    if (mimes.all { it == first }) return first
    val tops = mimes.map { it.substringBefore('/') }.distinct()
    return if (tops.size == 1) "${tops[0]}/*" else "*/*"
}

internal fun sanitizeShareFileName(name: String?): String {
    val cleaned = name?.substringAfterLast('/')?.substringAfterLast('\\')
        ?.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        ?.trim('_', '.')
        .orEmpty()
    return cleaned.ifBlank { "shared_file" }.take(80)
}

/**
 * Assembles one outbound share. Chunks are base64 and may split a quartet,
 * so the remainder waits for the next call.
 */
internal class ShareUpload(
    val title: String = "",
    val caption: String = ""
) {
    private val files = mutableListOf<PreparedFile>()
    private var dir: File? = null
    private var stream: FileOutputStream? = null
    private var currentName = ""
    private var currentMime = ""
    private var currentStored = ""
    private var pending = StringBuilder()
    private var totalBytes = 0L
    private var failed = false

    fun beginFile(directory: File, name: String?, mime: String?): Boolean {
        if (failed) return false
        if (files.size >= MAX_FILES) return fail()
        if (!closeCurrent()) return fail()
        dir = directory
        directory.mkdirs()
        currentName = sanitizeShareFileName(name)
        currentMime = shareMime(currentName, mime)
        currentStored = "${files.size}_$currentName"
        return try {
            stream = FileOutputStream(File(directory, currentStored))
            true
        } catch (e: Exception) {
            fail()
        }
    }

    fun appendChunk(chunk: String): Boolean {
        if (failed || stream == null) return fail()
        if (chunk.length > MAX_CHUNK_CHARS) return fail()
        pending.append(chunk.filter { !it.isWhitespace() })
        val usable = pending.length - (pending.length % 4)
        if (usable <= 0) return true
        val part = pending.substring(0, usable)
        pending.delete(0, usable)
        return writeDecoded(part)
    }

    fun finish(): List<PreparedFile>? {
        if (failed || !closeCurrent()) return null
        return files.toList()
    }

    private fun closeCurrent(): Boolean {
        val open = stream ?: return pending.isEmpty() || fail()
        if (pending.isNotEmpty()) {
            val padded = pending.toString() + "=".repeat((4 - pending.length % 4) % 4)
            pending.clear()
            if (!writeDecoded(padded)) {
                open.closeQuietly()
                stream = null
                return false
            }
        }
        return try {
            open.flush()
            open.close()
            stream = null
            files += PreparedFile(currentName, currentMime, currentStored)
            true
        } catch (e: Exception) {
            stream = null
            fail()
        }
    }

    private fun writeDecoded(part: String): Boolean {
        if (part.isEmpty()) return true
        val bytes = try {
            Base64.decode(part, Base64.NO_WRAP)
        } catch (e: Exception) {
            return fail()
        }
        totalBytes += bytes.size
        if (totalBytes > MAX_BYTES) return fail()
        return try {
            stream?.write(bytes)
            true
        } catch (e: Exception) {
            fail()
        }
    }

    private fun fail(): Boolean {
        failed = true
        stream?.closeQuietly()
        stream = null
        return false
    }

    private fun FileOutputStream.closeQuietly() {
        try {
            close()
        } catch (_: Exception) {
        }
    }

    companion object {
        const val MAX_BYTES = 32L * 1024L * 1024L
        const val MAX_FILES = 10
        const val MAX_CHUNK_CHARS = 512_000
    }
}

internal data class PreparedFile(
    val name: String,
    val mime: String,
    val storedName: String
)
