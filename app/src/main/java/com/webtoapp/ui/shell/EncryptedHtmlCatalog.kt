package com.webtoapp.ui.shell

import com.webtoapp.core.crypto.CryptoConstants

/**
 * Logical view of an HTML asset tree. Encrypted entries keep the `.enc` asset
 * path but are addressed by their real file name, so the local server can
 * decrypt one request at a time and never write the plaintext tree to disk.
 */
internal data class EncryptedHtmlCatalog(
    val files: Map<String, String>,
    val directories: Set<String>,
    val encrypted: Boolean,
) {
    fun resolveEntry(configured: String): String {
        val wanted = configured.removePrefix("/").trim()
        if (wanted.isNotEmpty() && files.containsKey(wanted)) return wanted
        listOf("index.html", "index.htm", "main.html").firstOrNull { files.containsKey(it) }?.let { return it }
        return files.keys.firstOrNull { path ->
            val name = path.substringAfterLast('/')
            name.endsWith(".html", ignoreCase = true) || name.endsWith(".htm", ignoreCase = true)
        } ?: wanted.ifBlank { "index.html" }
    }

    fun needsCrossOriginIsolation(): Boolean {
        return files.keys.any { path ->
            val name = path.substringAfterLast('/').lowercase()
            val extension = name.substringAfterLast('.', "")
            extension == "wasm" ||
                extension == "onnx" ||
                name.contains("ort-wasm") ||
                name.contains("worker")
        }
    }
}

internal fun catalogAssetTree(
    root: String,
    list: (String) -> Array<out String>?,
): EncryptedHtmlCatalog {
    val files = linkedMapOf<String, String>()
    val directories = mutableSetOf<String>()
    var encrypted = false

    fun addFile(logicalDir: String, rawName: String, assetPath: String) {
        if (rawName.endsWith(CryptoConstants.ENCRYPTED_EXTENSION)) encrypted = true
        val logicalName = rawName.removeSuffix(CryptoConstants.ENCRYPTED_EXTENSION)
        val logical = if (logicalDir.isEmpty()) logicalName else "$logicalDir/$logicalName"
        files[logical] = assetPath
    }

    fun walk(assetPath: String, logicalDir: String) {
        val children = list(assetPath) ?: return
        if (children.isEmpty()) return
        if (logicalDir.isNotEmpty()) directories += logicalDir
        for (child in children) {
            val childAsset = "$assetPath/$child"
            val nested = list(childAsset)
            if (nested != null && nested.isNotEmpty()) {
                val nextDir = if (logicalDir.isEmpty()) child else "$logicalDir/$child"
                directories += nextDir
                walk(childAsset, nextDir)
            } else {
                addFile(logicalDir, child, childAsset)
            }
        }
    }

    walk(root, "")
    return EncryptedHtmlCatalog(files, directories, encrypted)
}
