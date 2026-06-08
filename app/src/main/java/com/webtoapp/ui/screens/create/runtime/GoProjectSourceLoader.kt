package com.webtoapp.ui.screens.create.runtime

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.webtoapp.core.golang.GoRuntime
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.screens.create.common.ProjectImportException
import java.io.File

class GoProjectSourceLoader {
    fun copyDocumentTreeToProject(context: Context, treeUri: Uri, projectId: String): File {
        val runtime = GoRuntime(context)
        val projectDir = runtime.getProjectDir(projectId)
        if (projectDir.exists()) {
            projectDir.deleteRecursively()
        }
        projectDir.mkdirs()

        val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
            ?: throw ProjectImportException(Strings.importGoDirNotFound)
        if (!rootDoc.exists() || !rootDoc.isDirectory) {
            throw ProjectImportException(Strings.importGoDirNotFound)
        }

        val excludeDirs = setOf(".git", "node_modules", ".idea", "tmp", ".cache", "__MACOSX")
        var copiedCount = 0

        fun copyDocTree(doc: DocumentFile, relativePath: String) {
            val name = doc.name ?: return
            if (doc.isDirectory) {
                if (name in excludeDirs || name.startsWith("._")) return
                val nextPath = if (relativePath.isEmpty()) name else "$relativePath/$name"
                doc.listFiles().forEach { child -> copyDocTree(child, nextPath) }
            } else if (doc.isFile) {
                if (name.startsWith("._")) return
                val destPath = if (relativePath.isEmpty()) name else "$relativePath/$name"
                val destFile = File(projectDir, destPath)
                destFile.parentFile?.mkdirs()
                context.contentResolver.openInputStream(doc.uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                copiedCount++
            }
        }

        rootDoc.listFiles().forEach { child -> copyDocTree(child, "") }
        if (copiedCount == 0) {
            throw ProjectImportException(Strings.importGoDirNotFound)
        }
        return projectDir
    }
}
