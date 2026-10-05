package com.webtoapp.core.webview

import com.webtoapp.data.model.HtmlLoadMode
import java.io.File

object HtmlRuntimeLoadInspector {
    fun prefersFileScheme(rootDir: File): Boolean {
        return !requiresLocalHttp(rootDir)
    }

    /**
     * Decides whether an HTML/FRONTEND project can load from file://.
     *
     * LOCAL_HTTP always uses the loopback server. AUTO keeps the broad signal
     * scan (fetch, storage, modules, WASM, isolation). FILE stays on file://
     * for plain pages, but a page that references a CDN, an ES module, WASM,
     * a service worker, or a web manifest cannot run from an opaque file
     * origin, so it is promoted to the local server. A missing directory keeps
     * the old fallback: FILE stays file://, AUTO uses HTTP.
     */
    fun useFileScheme(
        mode: HtmlLoadMode,
        rootDir: File?,
        crossOriginIsolation: Boolean = false
    ): Boolean {
        return when (mode) {
            HtmlLoadMode.LOCAL_HTTP -> false
            HtmlLoadMode.AUTO -> {
                if (crossOriginIsolation) false
                else if (rootDir == null || !rootDir.isDirectory) false
                else prefersFileScheme(rootDir)
            }
            HtmlLoadMode.FILE -> {
                if (rootDir == null || !rootDir.isDirectory) true
                else !requiresRealOrigin(rootDir)
            }
        }
    }

    fun requiresLocalHttp(rootDir: File): Boolean {
        if (!rootDir.exists() || !rootDir.isDirectory) return true
        if (LocalHttpServer.shouldEnableCrossOriginIsolation(rootDir)) return true

        return anyProjectFile(rootDir) { file, name ->
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

    /**
     * True only for things an opaque file:// origin cannot run. Weaker signals
     * such as fetch() and localStorage stay on file:// when the user picked FILE,
     * so an explicit file override still wins for those pages.
     */
    fun requiresRealOrigin(rootDir: File): Boolean {
        if (!rootDir.exists() || !rootDir.isDirectory) return false
        return anyProjectFile(rootDir) { file, name ->
            when {
                name in serviceWorkerFileNames -> true
                name in manifestFileNames -> true
                name.endsWith(".webmanifest") -> true
                name.endsWith(".wasm") -> true
                name.endsWith(".mjs") -> true
                file.extension.lowercase() in textExtensions -> fileRequiresRealOrigin(file)
                else -> false
            }
        }
    }

    private fun anyProjectFile(rootDir: File, predicate: (File, String) -> Boolean): Boolean {
        return rootDir.walkTopDown()
            .maxDepth(6)
            .filter { it.isFile }
            .take(2000)
            .any { file -> predicate(file, file.name.lowercase()) }
    }

    private fun fileRequiresLocalHttp(file: File): Boolean {
        val lower = readPrefixLower(file) ?: return false
        return localHttpSignals.any { lower.contains(it) } ||
            moduleScriptRegex.containsMatchIn(lower) ||
            mjsScriptRegex.containsMatchIn(lower)
    }

    private fun fileRequiresRealOrigin(file: File): Boolean {
        val lower = readPrefixLower(file) ?: return false
        return realOriginSignals.any { lower.contains(it) } ||
            moduleScriptRegex.containsMatchIn(lower) ||
            mjsScriptRegex.containsMatchIn(lower)
    }

    private fun readPrefixLower(file: File): String? {
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
            String(bytes, 0, length, Charsets.UTF_8).lowercase()
        }.getOrNull()
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
    private val realOriginSignals = listOf(
        "import(",
        "http://",
        "https://",
        ".wasm",
        ".mjs"
    )
    private val moduleScriptRegex = Regex("<script\\b[^>]*\\btype\\s*=\\s*[\"']module[\"']")
    private val mjsScriptRegex = Regex("<script\\b[^>]*\\bsrc\\s*=\\s*[\"'][^\"']+\\.mjs[\"']")
    private const val MAX_SCAN_BYTES = 3 * 1024 * 1024
}
