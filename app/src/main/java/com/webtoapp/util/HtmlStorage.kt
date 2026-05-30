package com.webtoapp.util

import android.content.Context
import android.net.Uri
import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.util.UUID

object HtmlStorage {

    private const val TAG = "HtmlStorage"
    private const val HTML_DIR = "html_projects"

    private const val MIRROR_PARENT = "WebToApp"

    fun saveHtmlFile(
        context: Context,
        uri: Uri,
        fileName: String,
        projectId: String
    ): String? {
        return try {
            val projectDir = getProjectDir(context, projectId)
            val targetFile = File(projectDir, fileName)

            targetFile.parentFile?.mkdirs()

            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            mirrorToExternal(context, projectId, fileName, targetFile)

            targetFile.absolutePath
        } catch (e: Exception) {
            AppLogger.e(TAG, "Operation failed", e)
            null
        }
    }

    fun saveFromTempFile(
        context: Context,
        tempPath: String,
        fileName: String,
        projectId: String
    ): String? {
        return try {
            val tempFile = File(tempPath)
            if (!tempFile.exists()) return null

            val projectDir = getProjectDir(context, projectId)
            val targetFile = File(projectDir, fileName)

            targetFile.parentFile?.mkdirs()

            tempFile.copyTo(targetFile, overwrite = true)

            mirrorToExternal(context, projectId, fileName, targetFile)

            targetFile.absolutePath
        } catch (e: Exception) {
            AppLogger.e(TAG, "Operation failed", e)
            null
        }
    }

    fun saveProcessedHtml(
        context: Context,
        htmlContent: String,
        fileName: String,
        projectId: String
    ): String? {
        return try {
            val projectDir = getProjectDir(context, projectId)
            val targetFile = File(projectDir, fileName)

            targetFile.parentFile?.mkdirs()

            targetFile.writeText(htmlContent, Charsets.UTF_8)

            mirrorToExternal(context, projectId, fileName, targetFile)

            targetFile.absolutePath
        } catch (e: Exception) {
            AppLogger.e(TAG, "Operation failed", e)
            null
        }
    }

    fun deleteProject(context: Context, projectId: String) {
        try {
            val projectDir = getProjectDir(context, projectId)
            projectDir.deleteRecursively()

            externalMirrorDir(context, projectId)?.deleteRecursively()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Operation failed", e)
        }
    }

    fun clearTempFiles(context: Context) {
        try {
            val tempDir = File(context.cacheDir, "html_temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Operation failed", e)
        }
    }

    fun generateProjectId(): String {
        return UUID.randomUUID().toString().take(8)
    }

    private fun getProjectDir(context: Context, projectId: String): File {
        val htmlDir = File(context.filesDir, HTML_DIR)
        val projectDir = File(htmlDir, projectId)
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }
        return projectDir
    }

    private fun externalMirrorDir(context: Context, projectId: String): File? {
        val externalRoot = context.getExternalFilesDir(MIRROR_PARENT) ?: return null
        return File(File(externalRoot, HTML_DIR), projectId)
    }

    private fun mirrorToExternal(
        context: Context,
        projectId: String,
        relativeName: String,
        sourceFile: File
    ) {
        try {
            val mirrorDir = externalMirrorDir(context, projectId) ?: return
            val target = File(mirrorDir, relativeName)
            target.parentFile?.mkdirs()
            sourceFile.copyTo(target, overwrite = true)
        } catch (e: Exception) {

            AppLogger.w(TAG, "External mirror failed for $relativeName: ${e.message}")
        }
    }
}
