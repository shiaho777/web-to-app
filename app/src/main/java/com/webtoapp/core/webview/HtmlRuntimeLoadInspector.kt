package com.webtoapp.core.webview

import java.io.File

object HtmlRuntimeLoadInspector {
    fun prefersFileScheme(rootDir: File): Boolean {
        return !requiresLocalHttp(rootDir)
    }

    fun requiresLocalHttp(rootDir: File): Boolean {
        if (!rootDir.exists() || !rootDir.isDirectory) return true
        if (LocalHttpServer.shouldEnableCrossOriginIsolation(rootDir)) return true

        return rootDir.walkTopDown()
            .maxDepth(6)
            .filter { it.isFile }
            .take(2000)
            .any { file ->
                val name = file.name.lowercase()
                when {
                    name in serviceWorkerFileNames -> true
                    name in manifestFileNames -> true
                    name.endsWith(".webmanifest") -> true
                    name.endsWith(".wasm") -> true
                    file.extension.lowercase() in textExtensions -> fileRequiresLocalHttp(file)
                    else -> false
                }
            }
    }

    private fun fileRequiresLocalHttp(file: File): Boolean {
        return runCatching {
            val bytes = ByteArray(MAX_SCAN_BYTES)
            val length = file.inputStream().use { input ->
                var offset = 0
                while (offset < bytes.size) {
                    val read = input.read(bytes, offset, bytes.size - offset)
                    if (read <= 0) return@use offset
                    offset += read
                }
                offset
            }
            val text = String(bytes, 0, length, Charsets.UTF_8)
            val lower = text.lowercase()
            localHttpSignals.any { lower.contains(it) } ||
                Regex("<script\\b[^>]*\\btype\\s*=\\s*[\"']module[\"']").containsMatchIn(lower) ||
                Regex("<script\\b[^>]*\\bsrc\\s*=\\s*[\"'][^\"']+\\.mjs[\"']").containsMatchIn(lower)
        }.getOrDefault(false)
    }

    private val textExtensions = setOf("html", "htm", "js", "mjs", "css", "json")
    private val serviceWorkerFileNames = setOf(
        "sw.js",
        "service-worker.js",
        "serviceworker.js",
        "service_worker.js",
        "ngsw-worker.js",
        "firebase-messaging-sw.js"
    )
    private val manifestFileNames = setOf("manifest.json", "manifest.webmanifest")
    private val localHttpSignals = listOf(
        "audiocontext",
        "webkitaudiocontext",
        "localstorage",
        "sessionstorage",
        "indexeddb",
        "fetch(",
        "xmlhttprequest",
        "new worker",
        "sharedworker",
        "serviceworker",
        "navigator.serviceworker",
        "rel=\"manifest\"",
        "rel='manifest'",
        ".wasm",
        "import(",
        "http://",
        "https://"
    )
    private const val MAX_SCAN_BYTES = 3 * 1024 * 1024
}
