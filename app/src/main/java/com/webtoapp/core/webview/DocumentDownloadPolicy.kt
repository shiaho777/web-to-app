package com.webtoapp.core.webview

import java.net.URI

object DocumentDownloadPolicy {

    private val GITHUB_RELEASE_ASSET_PATH = Regex("/[^/]+/[^/]+/releases/download/[^/]+/[^/]+")

    private val UNRENDERABLE_EXTENSIONS = setOf(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "zip", "rar", "7z", "epub", "apk"
    )

    private val MIME_BY_EXTENSION = mapOf(
        "pdf" to "application/pdf",
        "doc" to "application/msword",
        "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "xls" to "application/vnd.ms-excel",
        "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "ppt" to "application/vnd.ms-powerpoint",
        "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "zip" to "application/zip",
        "rar" to "application/vnd.rar",
        "7z" to "application/x-7z-compressed",
        "epub" to "application/epub+zip",
        "apk" to "application/vnd.android.package-archive"
    )

    fun isUnrenderableDocumentUrl(url: String): Boolean {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        val ext = urlExtension(url) ?: return false
        return ext in UNRENDERABLE_EXTENSIONS
    }

    fun isGitHubReleaseAssetUrl(url: String): Boolean {
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals("github.com", ignoreCase = true) &&
            uri.userInfo == null && (uri.port == -1 || uri.port == 443) &&
            GITHUB_RELEASE_ASSET_PATH.matches(uri.rawPath.orEmpty())
    }

    fun mimeTypeFor(url: String): String =
        urlExtension(url)?.let { MIME_BY_EXTENSION[it] } ?: "application/octet-stream"

    fun isLoopbackUrl(url: String): Boolean {
        val host = uriHost(url) ?: return false
        return host == "127.0.0.1" || host == "localhost" || host == "::1"
    }

    private fun urlExtension(url: String): String? {
        var segment = url.substringBefore('#').substringBefore('?')
        val slash = segment.lastIndexOf('/')
        if (slash >= 0) segment = segment.substring(slash + 1)
        val dot = segment.lastIndexOf('.')
        if (dot <= 0 || dot == segment.length - 1) return null
        val ext = segment.substring(dot + 1).lowercase()
        return ext.ifBlank { null }?.takeIf { it.length <= 5 }
    }

    private fun uriHost(url: String): String? {
        val schemeEnd = url.indexOf("://")
        if (schemeEnd < 0) return null
        var rest = url.substring(schemeEnd + 3)
        rest = rest.substringBefore('/').substringBefore('?').substringBefore('#')
        rest = rest.substringAfterLast('@')
        if (rest.startsWith("[")) {
            return rest.substringBefore(']').removePrefix("[").lowercase()
        }
        return rest.substringBefore(':').lowercase()
    }
}
