package com.webtoapp.core.engine.download

data class GeckoDownloadMetadata(
    val contentDisposition: String,
    val mimeType: String,
    val contentLength: Long
)

object GeckoDownloadResponse {

    private const val DEFAULT_MIME = "application/octet-stream"

    fun isStreamed(uri: String): Boolean =
        uri.startsWith("blob:", ignoreCase = true) || uri.startsWith("data:", ignoreCase = true)

    fun metadata(headers: Map<String, String>): GeckoDownloadMetadata {
        val filename = header(headers, "webresponseinfo-filename")
        val contentDisposition = header(headers, "content-disposition")
            ?: filename?.let { "attachment; filename=\"${it.replace("\"", "")}\"" }
            ?: ""
        val mimeType = (header(headers, "content-type")
            ?: header(headers, "webresponseinfo-contenttype")
            ?: DEFAULT_MIME).substringBefore(';').trim().ifEmpty { DEFAULT_MIME }
        val contentLength = header(headers, "content-length")?.toLongOrNull()
            ?: header(headers, "webresponseinfo-contentlength")?.toLongOrNull()
            ?: -1L
        return GeckoDownloadMetadata(contentDisposition, mimeType, contentLength)
    }

    private fun header(headers: Map<String, String>, name: String): String? =
        headers.entries
            .firstOrNull { it.key.equals(name, ignoreCase = true) }
            ?.value
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
}
