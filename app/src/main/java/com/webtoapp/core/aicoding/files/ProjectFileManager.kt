package com.webtoapp.core.aicoding.files

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import java.io.File
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

    fun readBytes(sessionId: String, relativePath: String): ByteArray? {
        val target = resolveSafe(sessionId, relativePath) ?: return null
        return if (target.exists() && target.isFile) target.readBytes() else null
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

    companion object {
        private const val TAG = "ProjectFileManager"
        private const val ROOT = "aicoding/sessions"
        private const val MAX_PATH_LEN = 500

        private const val CHANGES_DIR = ".changes"

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
