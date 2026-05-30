package com.webtoapp.util

import android.net.Uri

val LOCAL_HTTP_HOSTS: Set<String> = setOf("localhost", "127.0.0.1", "10.0.2.2")

val BLOCKED_EXTERNAL_URL_SCHEMES: Set<String> = setOf("javascript", "data", "file", "content", "about")

fun getUrlScheme(rawUrl: String): String? {
    return runCatching { Uri.parse(rawUrl).scheme?.lowercase() }.getOrNull()
}

fun isAllowedUrlScheme(rawUrl: String, allowedSchemes: Set<String>): Boolean {
    val scheme = getUrlScheme(rawUrl) ?: return false
    return scheme in allowedSchemes
}

fun ensureWebUrlScheme(rawUrl: String, defaultScheme: String = "http"): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isEmpty()) return ""

    val hasScheme = runCatching { Uri.parse(trimmed).scheme?.isNotBlank() == true }.getOrDefault(false)
    if (hasScheme) return trimmed

    return "$defaultScheme://$trimmed"
}

fun normalizeExternalIntentUrl(
    rawUrl: String,
    blockedSchemes: Set<String> = BLOCKED_EXTERNAL_URL_SCHEMES
): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isEmpty()) return ""

    val hasScheme = runCatching { Uri.parse(trimmed).scheme?.isNotBlank() == true }.getOrDefault(false)
    val withScheme = if (hasScheme) trimmed else "http://$trimmed"
    val scheme = getUrlScheme(withScheme) ?: return ""
    if (scheme in blockedSchemes) return ""

    return withScheme
}
