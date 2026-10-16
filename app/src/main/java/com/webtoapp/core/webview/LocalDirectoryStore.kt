package com.webtoapp.core.webview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.documentfile.provider.DocumentFile
import com.webtoapp.core.logging.AppLogger
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * One user-picked folder, kept with a persistable SAF grant.
 *
 * The page never receives a filesystem path. Every call takes a relative path
 * inside the granted tree, and `..` is rejected before any child is resolved.
 * The grant lives in this app's private preferences, so a preview grant does
 * not transfer into an exported APK (that package must pick the folder again).
 */
class LocalDirectoryStore internal constructor(
    private val context: Context,
    private val grantKey: String,
    private val rootOverride: (() -> DocumentFile?)?,
    private val writableOverride: Boolean?
) {
    constructor(context: Context, grantKey: String) : this(context, grantKey, null, null)

    private val lock = Any()
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun info(): String = synchronized(lock) { infoLocked() }

    fun remember(uri: Uri): String = synchronized(lock) {
        val readWrite = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val persistedWrite = runCatching {
            context.contentResolver.takePersistableUriPermission(uri, readWrite)
            true
        }.getOrDefault(false)
        if (!persistedWrite) {
            val persistedRead = runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                true
            }.getOrDefault(false)
            if (!persistedRead) {
                return error("PERSIST_FAILED", "The system did not grant access to that folder")
            }
        }
        val root = DocumentFile.fromTreeUri(context, uri)
        if (root == null || !root.canRead()) {
            runCatching { context.contentResolver.releasePersistableUriPermission(uri, readWrite) }
            return error("PERSIST_FAILED", "The selected folder cannot be read")
        }
        val previous = storedUri()
        if (previous != null && previous != uri) {
            runCatching { context.contentResolver.releasePersistableUriPermission(previous, readWrite) }
        }
        prefs.edit().putString(prefsKey(), uri.toString()).apply()
        infoLocked()
    }

    fun release(): String = synchronized(lock) {
        val uri = storedUri()
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            runCatching { context.contentResolver.releasePersistableUriPermission(uri, flags) }
            prefs.edit().remove(prefsKey()).apply()
        }
        infoLocked()
    }

    fun list(relativePath: String): String = synchronized(lock) {
        val parts = parseDirectoryPath(relativePath) ?: return error("BAD_PATH", "That path is not inside the folder")
        val root = root() ?: return error("NO_GRANT", "No folder has been chosen")
        val node = if (parts.isEmpty()) root else walk(root, parts) ?: return error("NOT_FOUND", "No such entry")
        if (!node.isDirectory) return error("NOT_A_DIRECTORY", "That entry is a file")
        val children = node.listFiles()
        val entries = JSONArray()
        var truncated = false
        for ((index, child) in children.withIndex()) {
            if (index >= MAX_LIST) {
                truncated = true
                break
            }
            val name = child.name ?: continue
            entries.put(JSONObject().apply {
                put("name", name)
                put("directory", child.isDirectory)
                put("size", if (child.isFile) child.length().coerceAtLeast(0L) else 0L)
            })
        }
        JSONObject().apply {
            put("ok", true)
            put("entries", entries)
            put("truncated", truncated)
        }.toString()
    }

    fun read(relativePath: String, offset: Long, length: Int): String = synchronized(lock) {
        val parts = parseDirectoryPath(relativePath) ?: return error("BAD_PATH", "That path is not inside the folder")
        if (parts.isEmpty()) return error("BAD_PATH", "Name the file to read")
        val root = root() ?: return error("NO_GRANT", "No folder has been chosen")
        val node = walk(root, parts) ?: return error("NOT_FOUND", "No such file")
        if (!node.isFile) return error("NOT_A_FILE", "That entry is a folder")
        val requested = length.coerceIn(0, MAX_CHUNK)
        val safeOffset = offset.coerceAtLeast(0L)
        val reported = node.length()
        val knownSize = reported > 0L
        if (knownSize && safeOffset >= reported) {
            return readPayload(size = reported, offset = safeOffset, data = ByteArray(0), eof = true)
        }
        val toRead = if (knownSize) {
            minOf(requested.toLong(), reported - safeOffset).toInt()
        } else {
            requested
        }
        val data = try {
            readAt(node, safeOffset, toRead)
        } catch (e: IOException) {
            AppLogger.w(TAG, "Directory read failed", e)
            return error("IO", "Could not read that file")
        }
        val eof = if (knownSize) safeOffset + data.size >= reported else data.size < toRead
        val size = if (knownSize) reported else safeOffset + data.size
        readPayload(size = size, offset = safeOffset, data = data, eof = eof)
    }

    fun write(relativePath: String, base64: String, append: Boolean): String = synchronized(lock) {
        val parts = parseDirectoryPath(relativePath) ?: return error("BAD_PATH", "That path is not inside the folder")
        if (parts.isEmpty()) return error("BAD_PATH", "Name the file to write")
        if (!writable()) return error("READ_ONLY", "This folder was granted for reading only")
        val bytes = decodeChunk(base64) ?: return error("BAD_DATA", "The file data is not valid base64")
        if (bytes.size > MAX_CHUNK) return error("TOO_LARGE", "Write at most $MAX_CHUNK bytes per call")
        val root = root() ?: return error("NO_GRANT", "No folder has been chosen")
        val parent = ensureDirectory(root, parts.dropLast(1)) ?: return error("IO", "Could not create the parent folder")
        val name = parts.last()
        var existing = parent.findFile(name)
        if (existing != null && existing.isDirectory) return error("NOT_A_FILE", "A folder with that name already exists")
        val created = existing == null || !append
        if (existing != null && !append) {
            if (!existing.delete()) return error("IO", "Could not replace that file")
            existing = null
        }
        val target = existing ?: parent.createFile(mimeFor(name), name) ?: return error("IO", "Could not create that file")
        try {
            writeBytes(target, bytes, append = append && !created)
        } catch (e: IOException) {
            AppLogger.w(TAG, "Directory write failed", e)
            return error("IO", "Could not write that file")
        }
        JSONObject().apply {
            put("ok", true)
            put("size", target.length().coerceAtLeast(0L))
        }.toString()
    }

    fun delete(relativePath: String): String = synchronized(lock) {
        val parts = parseDirectoryPath(relativePath) ?: return error("BAD_PATH", "That path is not inside the folder")
        if (parts.isEmpty()) return error("BAD_PATH", "Refusing to delete the granted folder itself")
        if (!writable()) return error("READ_ONLY", "This folder was granted for reading only")
        val root = root() ?: return error("NO_GRANT", "No folder has been chosen")
        val node = walk(root, parts) ?: return error("NOT_FOUND", "No such entry")
        if (!node.delete()) return error("IO", "Could not delete that entry")
        JSONObject().apply { put("ok", true) }.toString()
    }

    /** Tree URI to reopen the system picker on, if a grant is still stored. */
    fun currentTreeUri(): Uri? = synchronized(lock) { storedUri() }

    private fun infoLocked(): String {
        val root = root()
        if (root == null) {
            return JSONObject().apply {
                put("ok", true)
                put("granted", false)
            }.toString()
        }
        return JSONObject().apply {
            put("ok", true)
            put("granted", true)
            put("name", root.name ?: "")
            put("writable", writable())
        }.toString()
    }

    private fun root(): DocumentFile? {
        rootOverride?.let { return it() }
        val uri = storedUri() ?: return null
        val stillGranted = context.contentResolver.persistedUriPermissions.any { permission ->
            permission.uri == uri && permission.isReadPermission
        }
        if (!stillGranted) {
            prefs.edit().remove(prefsKey()).apply()
            return null
        }
        return DocumentFile.fromTreeUri(context, uri)?.takeIf { it.canRead() }
    }

    private fun writable(): Boolean {
        writableOverride?.let { return it }
        val uri = storedUri() ?: return false
        return context.contentResolver.persistedUriPermissions.any { permission ->
            permission.uri == uri && permission.isWritePermission
        }
    }

    private fun storedUri(): Uri? {
        val raw = prefs.getString(prefsKey(), null) ?: return null
        return runCatching { Uri.parse(raw) }.getOrNull()
    }

    private fun prefsKey(): String {
        val safe = buildString(grantKey.length) {
            for (ch in grantKey) {
                append(if (ch.isLetterOrDigit() || ch == '.' || ch == '_' || ch == '-') ch else '_')
            }
        }.take(120).ifBlank { "default" }
        return "grant_$safe"
    }

    private fun walk(root: DocumentFile, parts: List<String>): DocumentFile? {
        var current = root
        for (name in parts) {
            current = current.findFile(name) ?: return null
        }
        return current
    }

    private fun ensureDirectory(root: DocumentFile, parts: List<String>): DocumentFile? {
        var current = root
        for (name in parts) {
            val child = current.findFile(name)
            current = when {
                child == null -> current.createDirectory(name) ?: return null
                child.isDirectory -> child
                else -> return null
            }
        }
        return current
    }

    private fun readAt(node: DocumentFile, offset: Long, length: Int): ByteArray {
        if (length == 0) return ByteArray(0)
        openInput(node).use { input ->
            skipFully(input, offset)
            val buffer = ByteArray(length)
            var filled = 0
            while (filled < length) {
                val read = input.read(buffer, filled, length - filled)
                if (read < 0) break
                filled += read
            }
            return if (filled == buffer.size) buffer else buffer.copyOf(filled)
        }
    }

    private fun writeBytes(node: DocumentFile, bytes: ByteArray, append: Boolean) {
        val uri = node.uri
        if (uri.scheme == "file") {
            val path = uri.path ?: throw IOException("missing file path")
            FileOutputStream(File(path), append).use { it.write(bytes) }
            return
        }
        val mode = if (append) "wa" else "wt"
        val stream = context.contentResolver.openOutputStream(uri, mode)
            ?: throw IOException("openOutputStream failed")
        stream.use { it.write(bytes) }
    }

    private fun openInput(node: DocumentFile): InputStream {
        val uri = node.uri
        if (uri.scheme == "file") {
            val path = uri.path ?: throw IOException("missing file path")
            return FileInputStream(File(path))
        }
        return context.contentResolver.openInputStream(uri) ?: throw IOException("openInputStream failed")
    }

    private fun skipFully(input: InputStream, offset: Long) {
        var remaining = offset
        val discard = ByteArray(8192)
        while (remaining > 0) {
            val chunk = minOf(remaining, discard.size.toLong()).toInt()
            val read = input.read(discard, 0, chunk)
            if (read < 0) return
            remaining -= read
        }
    }

    private fun readPayload(size: Long, offset: Long, data: ByteArray, eof: Boolean): String {
        return JSONObject().apply {
            put("ok", true)
            put("size", size)
            put("offset", offset)
            put("length", data.size)
            put("data", Base64.encodeToString(data, Base64.NO_WRAP))
            put("eof", eof)
        }.toString()
    }

    private fun decodeChunk(base64: String): ByteArray? {
        if (base64.length > MAX_CHUNK * 2) return null
        val cleaned = buildString(base64.length) {
            for (ch in base64) {
                if (!ch.isWhitespace()) append(ch)
            }
        }
        if (cleaned.length > MAX_CHUNK * 2) return null
        if (cleaned.any { it !in BASE64_ALPHABET }) return null
        return try {
            Base64.decode(cleaned, Base64.DEFAULT)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun mimeFor(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        if (ext.isEmpty() || ext == name.lowercase()) return "application/octet-stream"
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: "application/octet-stream"
    }

    companion object {
        private const val TAG = "LocalDirectory"
        private const val PREFS_NAME = "wta_local_directory"
        internal const val MAX_CHUNK = 256 * 1024
        private const val MAX_LIST = 2000
        private const val BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="

        private fun error(code: String, message: String): String {
            return JSONObject().apply {
                put("ok", false)
                put("error", code)
                put("message", message)
            }.toString()
        }
    }
}

/**
 * Relative path inside the granted tree. Returns null when the path could
 * escape the tree or is otherwise not a sequence of entry names.
 */
internal fun parseDirectoryPath(raw: String): List<String>? {
    if (raw.length > 1024) return null
    if (raw.indexOf('\u0000') >= 0) return null
    var text = raw
    while (text.startsWith("/") || text.startsWith("\\")) text = text.substring(1)
    while (text.endsWith("/") || text.endsWith("\\")) text = text.dropLast(1)
    if (text.isEmpty()) return emptyList()
    val parts = text.split('/', '\\')
    if (parts.size > 32) return null
    val names = ArrayList<String>(parts.size)
    for (part in parts) {
        if (part.isEmpty() || part == ".") continue
        if (part == ".." || part.length > 255) return null
        names.add(part)
    }
    return names
}
