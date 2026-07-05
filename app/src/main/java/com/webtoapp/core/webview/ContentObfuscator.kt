package com.webtoapp.core.webview

import java.io.InputStream

object ContentObfuscator {
    private val obfuscatableMimePrefixes = listOf(
        "text/",
        "application/javascript",
        "application/json",
        "application/xml",
        "application/xhtml+xml"
    )

    fun shouldObfuscate(mime: String): Boolean {
        val normalized = mime.trim().lowercase()
        return obfuscatableMimePrefixes.any { normalized.startsWith(it) }
    }

    fun obfuscate(rawStream: InputStream, mime: String, encoding: String?): InputStream {
        return rawStream
    }
}
