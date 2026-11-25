package com.webtoapp.core.scraper

import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.WebResourceResponse
import com.webtoapp.util.SafeZip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.Locale
import java.util.UUID

internal class OfflinePackPreviewSite(result: WebsiteScraper.ScrapeResult.Success) {
    private val host = "${UUID.randomUUID()}.offline-preview.invalid"
    val origin = "https://$host"
    val entryUrl: String = "$origin/${Uri.encode(result.entryFile, "/")}"
    private val reportPath = "/.wta-offline-preview-${UUID.randomUUID()}"
    private val files = result.files.mapNotNull { file ->
        SafeZip.safeChild(result.projectDir, file.name)?.takeIf { it.isFile }?.let { file.name to it }
    }.toMap()
    private val unavailable = MutableStateFlow(result.missingUrls.mapNotNull { displayUrl(Uri.parse(it)) }.toSet())
    val missing = unavailable.asStateFlow()

    fun allowsNavigation(uri: Uri): Boolean =
        isLocal(uri) && uri.path.orEmpty().removePrefix("/") in files

    fun noteMissing(uri: Uri) {
        val label = if (isLocal(uri)) uri.encodedPath else displayUrl(uri)
        if (!label.isNullOrBlank()) unavailable.update {
            if (it.size >= 2_000) it else it + label.take(400)
        }
    }

    fun intercept(uri: Uri, method: String): WebResourceResponse {
        val local = isLocal(uri)
        val path = uri.path.orEmpty()
        if (local && path == reportPath) {
            uri.getQueryParameter("url")?.take(400)?.let { blocked -> noteMissing(Uri.parse(blocked)) }
            return emptyResponse(204, "No Content")
        }
        val file = if (local) files[path.removePrefix("/")] else null
        val read = method == "GET" || method == "HEAD"
        if (file == null && local && path == "/favicon.ico" && read) return emptyResponse(204, "No Content")
        if (file == null || !read) {
            noteMissing(uri)
            return emptyResponse(404, "Unavailable Offline")
        }
        return try {
            val extension = file.extension.lowercase(Locale.ROOT)
            val mime = when (extension) {
                "html", "htm" -> "text/html"
                "js", "mjs" -> "text/javascript"
                "css" -> "text/css"
                "woff2" -> "font/woff2"
                "wasm" -> "application/wasm"
                else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
            }
            val stream = when {
                method == "HEAD" -> ByteArrayInputStream(ByteArray(0))
                mime == "text/html" -> ByteArrayInputStream(instrumentHtml(file.readText()).toByteArray(Charsets.UTF_8))
                else -> file.inputStream()
            }
            WebResourceResponse(
                mime,
                "UTF-8".takeIf { mime.startsWith("text/") || mime == "application/json" },
                200,
                "OK",
                mapOf(
                    "Cache-Control" to "no-store",
                    "X-DNS-Prefetch-Control" to "off",
                    "Permissions-Policy" to "camera=(), microphone=(), geolocation=()",
                    "Content-Security-Policy" to listOf(
                        "default-src 'self' data: blob:",
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' blob:",
                        "style-src 'self' 'unsafe-inline'",
                        "connect-src $origin",
                        "frame-src 'self'",
                        "worker-src 'none'",
                        "object-src 'none'",
                        "base-uri 'self'",
                        "form-action 'self'"
                    ).joinToString("; ")
                ),
                stream
            )
        } catch (_: IOException) {
            noteMissing(uri)
            emptyResponse(404, "Unavailable Offline")
        }
    }

    private fun instrumentHtml(html: String): String {
        val script = """
            <script>
            (() => {
              for (const key of ['RTCPeerConnection', 'webkitRTCPeerConnection']) {
                if (key in window) Object.defineProperty(window, key, { value: undefined });
              }
              document.addEventListener('securitypolicyviolation', event => {
                if (/^(https?|wss?):/.test(event.blockedURI)) {
                  fetch('$origin$reportPath?url=' + encodeURIComponent(event.blockedURI), { cache: 'no-store' }).catch(() => {});
                }
              });
            })();
            </script>
        """.trimIndent()
        val doctype = Regex("""^\s*<!doctype[^>]*>""", RegexOption.IGNORE_CASE).find(html)
        val position = doctype?.let { it.range.last + 1 } ?: 0
        return html.substring(0, position) + script + html.substring(position)
    }

    private fun isLocal(uri: Uri): Boolean =
        uri.scheme == "https" && uri.encodedAuthority == host

    private fun displayUrl(uri: Uri): String? {
        val protocol = uri.scheme ?: return null
        val domain = uri.host ?: return null
        val port = uri.port.takeIf { it > 0 }?.let { ":$it" }.orEmpty()
        return "$protocol://$domain$port${uri.encodedPath.orEmpty()}"
    }

    private fun emptyResponse(code: Int, reason: String) =
        WebResourceResponse("text/plain", "UTF-8", code, reason, emptyMap(), ByteArrayInputStream(ByteArray(0)))
}
