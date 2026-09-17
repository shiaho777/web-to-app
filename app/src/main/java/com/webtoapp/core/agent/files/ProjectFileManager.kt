package com.webtoapp.core.agent.files

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectFileManager(private val context: Context) {

    fun getRoot(): File {
        val dir = File(context.filesDir, ROOT)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getSessionRoot(sessionId: String): File {
        val dir = File(getRoot(), sanitizeSessionId(sessionId))
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun deleteSession(sessionId: String): Boolean {
        val dir = getSessionRoot(sessionId)
        return if (dir.exists()) dir.deleteRecursively() else false
    }

    fun writeText(sessionId: String, relativePath: String, content: String): FileInfo? {
        val target = resolveSafe(sessionId, relativePath) ?: return null
        snapshotBeforeWrite(sessionId, relativePath, target)
        target.parentFile?.mkdirs()
        target.writeText(content)
        return describe(target, sessionId)
    }

    fun readText(sessionId: String, relativePath: String): String? {
        val target = resolveSafe(sessionId, relativePath) ?: return null
        return if (target.exists() && target.isFile) target.readText() else null
    }

    fun writeBytes(sessionId: String, relativePath: String, bytes: ByteArray): FileInfo? {
        val target = resolveSafe(sessionId, relativePath) ?: return null
        snapshotBeforeWrite(sessionId, relativePath, target)
        target.parentFile?.mkdirs()
        target.writeBytes(bytes)
        return describe(target, sessionId)
    }

    /**
     * Streamed copy into the sandbox — unlike [writeBytes] the source bytes never
     * land fully in memory, so multi-hundred-MB attachments cannot OOM the app.
     * On failure the partial target is removed and null is returned. The caller
     * keeps ownership of [input] (wrap it in `use`).
     */
    fun writeStream(sessionId: String, relativePath: String, input: InputStream): FileInfo? {
        val target = resolveSafe(sessionId, relativePath) ?: return null
        snapshotBeforeWrite(sessionId, relativePath, target)
        target.parentFile?.mkdirs()
        return try {
            target.outputStream().buffered().use { out -> input.copyTo(out) }
            describe(target, sessionId)
        } catch (e: Exception) {
            runCatching { target.delete() }
            AppLogger.w(TAG, "writeStream failed for $relativePath: ${e.message}")
            null
        }
    }

    fun readBytes(sessionId: String, relativePath: String): ByteArray? {
        val target = resolveSafe(sessionId, relativePath) ?: return null
        return if (target.exists() && target.isFile) target.readBytes() else null
    }

    /**
     * One bounded window into a text file — chunked scan, never a whole-file
     * read, and never even a whole LINE: lines over [MAX_LINE_CHARS] keep only
     * the first chars plus a marker, so a single-line multi-GB upload can't OOM
     * the reader either. [remainingLines] counts at most [REMAINING_COUNT_CAP]
     * lines past the window ([remainingCapped] flags the early stop).
     */
    fun readTextWindow(
        sessionId: String,
        relativePath: String,
        offset: Int,
        limit: Int
    ): TextWindow? {
        val target = resolveSafe(sessionId, relativePath) ?: return null
        if (!target.exists() || !target.isFile) return null
        val totalBytes = target.length()
        if (sniffBinary(target)) {
            return TextWindow(emptyList(), 0, false, totalBytes, binary = true)
        }
        return try {
            scanLines(target, offset, limit, REMAINING_COUNT_CAP).copy(totalBytes = totalBytes)
        } catch (e: Exception) {
            AppLogger.w(TAG, "readTextWindow failed for $relativePath: ${e.message}")
            null
        }
    }

    /**
     * Bounded line count for the project summary — same chunked scan, zero lines
     * collected, gives up at [maxLines]. Returns -1 for binary files or counts
     * beyond [maxLines].
     */
    fun countLines(sessionId: String, relativePath: String, maxLines: Int = LINE_COUNT_CAP): Int {
        val target = resolveSafe(sessionId, relativePath) ?: return -1
        if (!target.exists() || !target.isFile || sniffBinary(target)) return -1
        return try {
            val w = scanLines(target, 0, 0, maxLines)
            if (w.remainingCapped) -1 else w.remainingLines
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Chunked line scan over [file]: collects at most [take] lines starting at
     * [offset], then counts up to [countCap] further line terminators. Memory is
     * bounded by [SCAN_CHUNK_CHARS] + [MAX_LINE_CHARS] regardless of file size;
     * both \n and \r\n endings are handled (a \r split across chunk borders is
     * carried via [pendingCR]).
     */
    private fun scanLines(file: File, offset: Int, take: Int, countCap: Int): TextWindow {
        val lines = ArrayList<String>(minOf(take, 512))
        var remaining = 0
        var capped = false
        var lineIndex = 0
        var pendingCR = false
        val cur = StringBuilder()
        var truncated = false
        var lineHasChars = false

        fun flushLine() {
            when {
                lineIndex < offset -> Unit
                lines.size < take ->
                    lines += cur.toString() + if (truncated) LINE_TRUNCATED_MARK else ""
                remaining < countCap -> remaining++
                else -> capped = true
            }
            lineIndex++
            cur.setLength(0)
            truncated = false
            lineHasChars = false
        }

        file.bufferedReader().use { r ->
            val buf = CharArray(SCAN_CHUNK_CHARS)
            while (!capped) {
                val n = r.read(buf)
                if (n < 0) break
                var i = 0
                if (pendingCR) {
                    pendingCR = false
                    if (buf[0] == '\n') i = 1
                }
                while (i < n && !capped) {
                    val c = buf[i]
                    if (c == '\r' || c == '\n') {
                        flushLine()
                        i++
                        if (c == '\r') {
                            if (i < n) {
                                if (buf[i] == '\n') i++
                            } else pendingCR = true
                        }
                    } else {
                        lineHasChars = true
                        if (lineIndex in offset until offset + take) {
                            if (cur.length < MAX_LINE_CHARS) cur.append(c) else truncated = true
                        }
                        i++
                    }
                }
            }
            // EOF: a trailing line without a final terminator still counts — even
            // in pure-count mode where its chars were never collected.
            if (!capped && lineHasChars) flushLine()
        }
        return TextWindow(lines, remaining, capped, totalBytes = 0, binary = false)
    }

    /** NUL byte in the first [BINARY_SNIFF_BYTES] → treat as binary, not text. */
    private fun sniffBinary(file: File): Boolean {
        return try {
            file.inputStream().use { s ->
                val buf = ByteArray(BINARY_SNIFF_BYTES)
                val n = s.read(buf)
                var i = 0
                var binary = false
                while (i < n) {
                    if (buf[i] == 0.toByte()) { binary = true; break }
                    i++
                }
                binary
            }
        } catch (e: Exception) {
            false
        }
    }

    fun exists(sessionId: String, relativePath: String): Boolean {
        val target = resolveSafe(sessionId, relativePath) ?: return false
        return target.exists()
    }

    fun delete(sessionId: String, relativePath: String): Boolean {
        val target = resolveSafe(sessionId, relativePath) ?: return false
        snapshotBeforeWrite(sessionId, relativePath, target)
        return target.exists() && target.delete()
    }

    fun listAll(sessionId: String): List<FileInfo> {
        val root = getSessionRoot(sessionId)
        if (!root.exists()) return emptyList()
        val out = mutableListOf<FileInfo>()
        root.walkTopDown().forEach { f ->
            if (f.isFile) {
                val rel = f.relativeTo(root).path.replace(File.separatorChar, '/')

                if (rel.startsWith("$CHANGES_DIR/") || rel == CHANGES_DIR) return@forEach
                out += FileInfo(
                    relativePath = rel,
                    sizeBytes = f.length(),
                    modifiedAt = f.lastModified(),
                    isText = looksLikeText(rel)
                )
            }
        }
        return out.sortedBy { it.relativePath }
    }

    fun describe(file: File, sessionId: String): FileInfo? {
        if (!file.exists() || !file.isFile) return null
        val rel = file.relativeTo(getSessionRoot(sessionId)).path.replace(File.separatorChar, '/')
        return FileInfo(
            relativePath = rel,
            sizeBytes = file.length(),
            modifiedAt = file.lastModified(),
            isText = looksLikeText(rel)
        )
    }

    fun resolveSafe(sessionId: String, relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        val cleaned = relativePath.trim().trimStart('/').replace('\\', '/')
        if (cleaned.isEmpty() || cleaned.length > MAX_PATH_LEN) return null
        if (cleaned.contains("..")) return null
        if (cleaned.contains(':')) return null
        if (cleaned.startsWith('~')) return null
        val root = getSessionRoot(sessionId)
        val target = File(root, cleaned)

        return try {
            val canonRoot = root.canonicalPath
            val canonTarget = target.canonicalPath
            if (canonTarget == canonRoot || canonTarget.startsWith(canonRoot + File.separator)) target else null
        } catch (e: Exception) {
            AppLogger.w(TAG, "resolveSafe failed for $relativePath: ${e.message}")
            null
        }
    }

    private fun snapshotDir(sessionId: String): File =
        File(getSessionRoot(sessionId), CHANGES_DIR)

    private fun snapshotPath(sessionId: String, relativePath: String): File =
        File(snapshotDir(sessionId), relativePath)

    private fun snapshotBeforeWrite(sessionId: String, relativePath: String, target: File) {
        if (relativePath.startsWith("$CHANGES_DIR/") || relativePath == CHANGES_DIR) return
        val shadow = snapshotPath(sessionId, relativePath)
        val marker = File(shadow.parentFile, shadow.name + DELETED_MARKER_SUFFIX)
        if (shadow.exists() || marker.exists()) return
        try {
            shadow.parentFile?.mkdirs()
            if (target.exists() && target.isFile) {
                target.copyTo(shadow, overwrite = false)
            } else {
                marker.createNewFile()
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Snapshot failed for $relativePath: ${e.message}")
        }
    }

    fun clearSnapshots(sessionId: String) {
        snapshotDir(sessionId).deleteRecursively()
    }

    fun undoChange(sessionId: String, relativePath: String): Boolean {
        val target = resolveSafe(sessionId, relativePath) ?: return false
        val shadow = snapshotPath(sessionId, relativePath)
        val marker = File(shadow.parentFile, shadow.name + DELETED_MARKER_SUFFIX)
        return when {
            shadow.exists() && shadow.isFile -> {
                target.parentFile?.mkdirs()
                shadow.copyTo(target, overwrite = true)
                shadow.delete()
                true
            }
            marker.exists() -> {
                if (target.exists()) target.delete()
                marker.delete()
                true
            }
            else -> false
        }
    }

    private fun sanitizeSessionId(id: String): String {

        val cleaned = id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return cleaned.ifEmpty { "default" }
    }

    private fun looksLikeText(relPath: String): Boolean {
        val ext = relPath.substringAfterLast('.', "").lowercase()
        return ext in TEXT_EXTENSIONS
    }

    data class FileInfo(
        val relativePath: String,
        val sizeBytes: Long,
        val modifiedAt: Long,
        val isText: Boolean
    ) {
        fun formatSize(): String = when {
            sizeBytes < 1024 -> "${sizeBytes}B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024}KB"
            else -> "${sizeBytes / (1024 * 1024)}MB"
        }
        fun formatTime(): String = TIME_FMT.format(Date(modifiedAt))
    }

    /**
     * Result of [readTextWindow]: a bounded line window plus enough metadata for
     * the caller to render "more lines" hints without ever holding the whole file.
     */
    data class TextWindow(
        val lines: List<String>,
        val remainingLines: Int,
        val remainingCapped: Boolean,
        val totalBytes: Long,
        val binary: Boolean
    )

    companion object {
        private const val TAG = "ProjectFileManager"
        private const val ROOT = "aicoding/sessions"
        private const val MAX_PATH_LEN = 500

        private const val CHANGES_DIR = ".changes"

        private const val BINARY_SNIFF_BYTES = 8 * 1024

        private const val SCAN_CHUNK_CHARS = 16 * 1024

        private const val LINE_COUNT_CAP = 20_000

        private const val REMAINING_COUNT_CAP = 10_000

        const val MAX_LINE_CHARS = 2_000

        private const val LINE_TRUNCATED_MARK = " …"

        private const val DELETED_MARKER_SUFFIX = ".__deleted__"
        private val TIME_FMT = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        private val TEXT_EXTENSIONS = setOf(
            "html", "htm", "css", "js", "jsx", "mjs", "cjs", "ts", "tsx",
            "json", "md", "markdown", "txt", "xml", "svg", "yaml", "yml",
            "toml", "ini", "env", "sh", "py", "rb", "go", "java", "kt",
            "kts", "php", "sql", "graphql", "gql", "vue", "astro", "csv",
            "log", "rs", "c", "h", "cpp", "hpp"
        )
    }
}
