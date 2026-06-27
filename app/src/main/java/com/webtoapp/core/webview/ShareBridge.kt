package com.webtoapp.core.webview

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.webkit.JavascriptInterface
import androidx.core.content.FileProvider
import com.webtoapp.core.logging.AppLogger
import java.io.File

class ShareBridge(private val context: Context) {

    @JavascriptInterface
    fun shareText(title: String?, text: String?, url: String?) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                if (!title.isNullOrEmpty()) {
                    putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                }
                val shareText = buildString {
                    if (!text.isNullOrEmpty()) append(text)
                    if (!url.isNullOrEmpty()) {
                        if (isNotEmpty()) append("\n")
                        append(url)
                    }
                }
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }
            val chooser = android.content.Intent.createChooser(intent, title ?: "Share")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            AppLogger.e("ShareBridge", "Share failed", e)
        }
    }

    @JavascriptInterface
    fun shareFile(title: String?, base64Data: String?, mimeType: String?, fileName: String?) {
        if (base64Data.isNullOrEmpty() || mimeType.isNullOrEmpty()) return
        try {
            val bytes = Base64.decode(base64Data, Base64.NO_WRAP)
            val safeName = if (!fileName.isNullOrBlank()) sanitizeFileName(fileName) else "shared_file"
            val shareDir = File(context.cacheDir, "shared_files").apply { mkdirs() }
            val file = File(shareDir, safeName)
            file.writeBytes(bytes)

            val contentUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = mimeType
                if (!title.isNullOrEmpty()) {
                    putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                }
                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = android.content.Intent.createChooser(intent, title ?: "Share")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            AppLogger.e("ShareBridge", "Share file failed", e)
        }
    }

    @JavascriptInterface
    fun canShare(): Boolean = true

    @JavascriptInterface
    fun canShareFiles(): Boolean = true

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}
