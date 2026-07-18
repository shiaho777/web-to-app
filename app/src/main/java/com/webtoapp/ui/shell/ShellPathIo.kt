package com.webtoapp.ui.shell

import android.content.Context
import com.webtoapp.core.crypto.AssetDecryptor
import com.webtoapp.core.shell.ShellConfig
import java.io.File

object ShellPathIo {
    fun isLocalFilePath(path: String): Boolean {
        val value = path.trim()
        if (value.isEmpty()) return false
        if (value.startsWith("file:", ignoreCase = true)) return true
        if (value.startsWith("/")) return true
        return false
    }

    fun toFile(path: String): File {
        val value = path.trim()
        return if (value.startsWith("file:", ignoreCase = true)) {
            File(java.net.URI(value))
        } else {
            File(value)
        }
    }

    fun resolveContentDir(context: Context, config: ShellConfig, defaultName: String): File {
        val preview = config.previewContentDir.trim()
        if (preview.isNotBlank()) {
            val file = toFile(preview)
            if (file.isDirectory) return file
            if (file.isFile) return file.parentFile ?: file
        }
        val site = config.siteDirName.trim()
        return File(context.filesDir, if (site.isNotBlank()) site else defaultName)
    }

    fun shouldExtractAssets(config: ShellConfig): Boolean {
        return config.previewContentDir.isBlank()
    }

    fun readBytes(
        context: Context,
        path: String,
        decryptor: AssetDecryptor? = null
    ): ByteArray {
        if (isLocalFilePath(path)) {
            return toFile(path).readBytes()
        }
        if (decryptor != null) {
            try {
                return decryptor.loadAsset(path)
            } catch (_: Exception) {
            }
        }
        return context.assets.open(path).use { it.readBytes() }
    }

    fun resolvePreviewMediaFile(config: ShellConfig): File? {
        val preview = config.previewContentDir.trim()
        if (preview.isBlank()) return null
        val file = toFile(preview)
        if (file.isFile && file.exists()) return file
        if (file.isDirectory) {
            val candidates = listOf(
                "media_content.mp4",
                "media_content.png",
                "media_content.jpg",
                "media_content.jpeg",
                "media_content.webp"
            )
            for (name in candidates) {
                val child = File(file, name)
                if (child.isFile) return child
            }
            file.listFiles()?.firstOrNull { it.isFile }?.let { return it }
        }
        return null
    }
}
