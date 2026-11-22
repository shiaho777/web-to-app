package com.webtoapp.core.files

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.webtoapp.core.logging.AppLogger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.security.MessageDigest

data class DualPaneDocument(
    val uri: Uri,
    val name: String,
    val mimeType: String?,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean
)

data class DualPaneComparison(
    val sameSize: Boolean,
    val sameContent: Boolean,
    val firstDigest: String,
    val secondDigest: String
)

object DualPaneDocumentOps {
    const val MAX_EDIT_BYTES = 1_048_576L

    fun list(
        context: Context,
        treeUri: Uri,
        path: List<String>
    ): List<DualPaneDocument> {
        val directory = resolveDirectory(context, treeUri, path)
        if (!directory.canRead()) throw IOException("Folder is not readable")
        return directory.listFiles()
            .map { document ->
                DualPaneDocument(
                    uri = document.uri,
                    name = document.name ?: document.uri.lastPathSegment ?: "",
                    mimeType = document.type,
                    size = document.length(),
                    lastModified = document.lastModified(),
                    isDirectory = document.isDirectory
                )
            }
            .sortedWith(compareBy<DualPaneDocument> { !it.isDirectory }.thenBy { it.name.lowercase() })
    }

    fun createFolder(context: Context, treeUri: Uri, path: List<String>, name: String) {
        validateName(name)
        val parent = resolveDirectory(context, treeUri, path)
        if (parent.findFile(name) != null) throw IOException("An item with that name already exists")
        if (parent.createDirectory(name) == null) throw IOException("The folder could not be created")
    }

    fun rename(
        context: Context,
        treeUri: Uri,
        path: List<String>,
        documentUri: Uri,
        newName: String
    ) {
        validateName(newName)
        val parent = resolveDirectory(context, treeUri, path)
        val document = findSelected(parent, documentUri)
        val conflict = parent.findFile(newName)
        if (conflict != null && conflict.uri != document.uri) {
            throw IOException("An item with that name already exists")
        }
        if (!document.renameTo(newName)) throw IOException("The item could not be renamed")
    }

    fun delete(
        context: Context,
        treeUri: Uri,
        path: List<String>,
        documentUris: Set<Uri>
    ) {
        val parent = resolveDirectory(context, treeUri, path)
        val entries = parent.listFiles().filter { it.uri in documentUris }
        if (entries.size != documentUris.size) throw IOException("One or more selected items are no longer available")
        entries.forEach { document ->
            if (!deleteRecursively(document, 0)) throw IOException("Could not delete ${document.name.orEmpty()}")
        }
    }

    fun transfer(
        context: Context,
        sourceTreeUri: Uri,
        sourcePath: List<String>,
        selectedUris: Set<Uri>,
        targetTreeUri: Uri,
        targetPath: List<String>,
        move: Boolean
    ) {
        if (selectedUris.isEmpty()) throw IOException("No items are selected")
        val sourceParent = resolveDirectory(context, sourceTreeUri, sourcePath)
        val targetParent = resolveDirectory(context, targetTreeUri, targetPath)
        val sources = sourceParent.listFiles().filter { it.uri in selectedUris }
        if (sources.size != selectedUris.size) throw IOException("One or more selected items are no longer available")
        if (sourceTreeUri == targetTreeUri) {
            sources.filter { it.isDirectory }.forEach { source ->
                val sourcePathFromRoot = sourcePath + source.name.orEmpty()
                if (targetPath.size >= sourcePathFromRoot.size &&
                    targetPath.take(sourcePathFromRoot.size) == sourcePathFromRoot
                ) {
                    throw IOException("A folder cannot be copied or moved inside itself")
                }
            }
        }
        sources.forEach { source ->
            if (targetParent.findFile(source.name.orEmpty()) != null) {
                throw IOException("An item with that name already exists: ${source.name.orEmpty()}")
            }
        }

        val created = mutableListOf<DocumentFile>()
        try {
            sources.forEach { source ->
                created += copyRecursively(context, source, targetParent, 0)
            }
        } catch (error: Exception) {
            created.asReversed().forEach { deleteRecursively(it, 0) }
            throw IOException("Transfer failed; copied items were rolled back: ${error.message}", error)
        }

        if (move) {
            sources.forEach { source ->
                if (!deleteRecursively(source, 0)) {
                    throw IOException(
                        "The copy completed, but the source could not be removed: ${source.name.orEmpty()}"
                    )
                }
            }
        }
    }

    fun readText(context: Context, treeUri: Uri, path: List<String>, documentUri: Uri): String {
        val document = findSelected(resolveDirectory(context, treeUri, path), documentUri)
        if (document.isDirectory) throw IOException("Folders cannot be edited as text")
        if (!isTextDocument(document.type, document.name.orEmpty())) {
            throw IOException("This file type is not supported by the text editor")
        }
        val size = document.length()
        if (size > MAX_EDIT_BYTES) throw IOException("This file is too large for the text editor")
        val bytes = context.contentResolver.openInputStream(document.uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0L
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > MAX_EDIT_BYTES) throw IOException("This file is too large for the text editor")
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: throw IOException("The file could not be opened")
        if (bytes.size > MAX_EDIT_BYTES) throw IOException("This file is too large for the text editor")
        if (bytes.any { it == 0.toByte() }) throw IOException("Binary files cannot be edited as text")
        return bytes.toString(Charsets.UTF_8)
    }

    fun writeTextWithBackup(
        context: Context,
        treeUri: Uri,
        path: List<String>,
        documentUri: Uri,
        text: String
    ): File {
        val document = findSelected(resolveDirectory(context, treeUri, path), documentUri)
        if (document.isDirectory || !isTextDocument(document.type, document.name.orEmpty())) {
            throw IOException("This file type is not supported by the text editor")
        }
        if (!document.canWrite()) throw IOException("The file is not writable")
        if (document.length() > MAX_EDIT_BYTES) throw IOException("This file is too large for the text editor")
        val bytes = text.toByteArray(Charsets.UTF_8)
        if (bytes.size > MAX_EDIT_BYTES) throw IOException("The edited file is too large")
        val backup = backupFile(context, document)
        try {
            context.contentResolver.openOutputStream(document.uri, "wt")?.use { output ->
                output.write(bytes)
                output.flush()
            } ?: throw IOException("The file could not be opened for writing")
        } catch (error: Exception) {
            try {
                backup.inputStream().use { input ->
                    context.contentResolver.openOutputStream(document.uri, "wt")?.use { output ->
                        input.copyTo(output)
                    } ?: throw IOException("The original file could not be restored")
                }
            } catch (restoreError: Exception) {
                AppLogger.e("DualPaneFiles", "Failed to restore edited file from backup", restoreError)
                throw IOException(
                    "Save failed and automatic restore failed. Backup: ${backup.absolutePath}",
                    error
                )
            }
            throw IOException("Save failed; the original file was restored. Backup: ${backup.absolutePath}", error)
        }
        return backup
    }

    fun compare(
        context: Context,
        firstTreeUri: Uri,
        firstPath: List<String>,
        firstUri: Uri,
        secondTreeUri: Uri,
        secondPath: List<String>,
        secondUri: Uri
    ): DualPaneComparison {
        val first = findSelected(resolveDirectory(context, firstTreeUri, firstPath), firstUri)
        val second = findSelected(resolveDirectory(context, secondTreeUri, secondPath), secondUri)
        if (first.isDirectory || second.isDirectory) throw IOException("Select files, not folders, to compare")
        val firstDigest = digest(context, first)
        val secondDigest = digest(context, second)
        return DualPaneComparison(
            sameSize = first.length() == second.length(),
            sameContent = firstDigest == secondDigest,
            firstDigest = firstDigest,
            secondDigest = secondDigest
        )
    }

    fun isTextDocument(document: DualPaneDocument): Boolean =
        isTextDocument(document.mimeType, document.name)

    private fun isTextDocument(mimeType: String?, name: String): Boolean =
        mimeType?.startsWith("text/") == true ||
            mimeType in setOf("application/json", "application/xml", "application/javascript") ||
            name.substringAfterLast('.', "").lowercase() in setOf(
                "txt", "md", "json", "xml", "html", "htm", "css", "js", "ts", "kt", "java",
                "smali", "yaml", "yml", "toml", "csv", "log", "properties", "gradle", "py",
                "sh", "sql", "svg"
            )

    private fun resolveDirectory(context: Context, treeUri: Uri, path: List<String>): DocumentFile {
        var current = DocumentFile.fromTreeUri(context, treeUri)
            ?: throw IOException("The selected folder is no longer available")
        if (!current.isDirectory || !current.canRead()) throw IOException("The selected folder is not readable")
        path.forEach { segment ->
            current = current.findFile(segment)
                ?.takeIf { it.isDirectory && it.canRead() }
                ?: throw IOException("Folder no longer exists: $segment")
        }
        return current
    }

    private fun findSelected(parent: DocumentFile, uri: Uri): DocumentFile =
        parent.listFiles().firstOrNull { it.uri == uri }
            ?: throw IOException("The selected item is no longer available")

    private fun validateName(name: String) {
        if (name.isBlank() || name == "." || name == ".." || name.contains('/')) {
            throw IOException("Enter a valid name")
        }
    }

    private fun copyRecursively(
        context: Context,
        source: DocumentFile,
        destinationParent: DocumentFile,
        depth: Int
    ): DocumentFile {
        if (depth > 64) throw IOException("Folder nesting is too deep")
        val name = source.name ?: throw IOException("A selected item has no name")
        if (source.isDirectory) {
            val destination = destinationParent.createDirectory(name)
                ?: throw IOException("Could not create folder: $name")
            try {
                source.listFiles().forEach { child ->
                    copyRecursively(context, child, destination, depth + 1)
                }
            } catch (error: Exception) {
                deleteRecursively(destination, 0)
                throw error
            }
            return destination
        }

        val mime = source.type
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substringAfterLast('.', ""))
            ?: "application/octet-stream"
        val destination = destinationParent.createFile(mime, name)
            ?: throw IOException("Could not create file: $name")
        try {
            val input = context.contentResolver.openInputStream(source.uri)
                ?: throw IOException("Could not read file: $name")
            val output = context.contentResolver.openOutputStream(destination.uri, "wt")
                ?: throw IOException("Could not write file: $name")
            input.use { sourceStream ->
                output.use { destinationStream -> sourceStream.copyTo(destinationStream) }
            }
        } catch (error: Exception) {
            destination.delete()
            throw error
        }
        return destination
    }

    private fun deleteRecursively(document: DocumentFile, depth: Int): Boolean {
        if (depth > 64) return false
        if (document.isDirectory) {
            if (document.listFiles().any { !deleteRecursively(it, depth + 1) }) return false
        }
        return document.delete()
    }

    private fun backupFile(context: Context, document: DocumentFile): File {
        val directory = File(context.filesDir, "dual_pane_backups").apply {
            if (!exists() && !mkdirs()) throw IOException("Could not create backup directory")
        }
        val safeName = document.name.orEmpty().replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "file" }
        val backup = File(directory, "${System.currentTimeMillis()}_$safeName")
        try {
            val input = context.contentResolver.openInputStream(document.uri)
                ?: throw IOException("The original file could not be backed up")
            input.use { source -> backup.outputStream().use { target -> source.copyTo(target) } }
        } catch (error: Exception) {
            backup.delete()
            throw IOException("The original file could not be backed up: ${error.message}", error)
        }
        return backup
    }

    private fun digest(context: Context, document: DocumentFile): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = context.contentResolver.openInputStream(document.uri)
            ?: throw IOException("Could not read ${document.name.orEmpty()}")
        input.use { stream ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
