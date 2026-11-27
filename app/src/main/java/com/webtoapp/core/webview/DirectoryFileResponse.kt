package com.webtoapp.core.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.io.IOException

internal object DirectoryFileResponse {
    data class ByteRange(val start: Long, val end: Long) {
        val length: Long get() = end - start + 1
    }

    fun parseRange(value: String, size: Long): ByteRange? {
        if (size <= 0 || !value.startsWith("bytes=", ignoreCase = true)) return null
        val parts = value.substringAfter('=').split('-', limit = 2)
        if (parts.size != 2) return null
        if (parts[0].isEmpty()) {
            val suffix = parts[1].toLongOrNull()?.takeIf { it > 0 } ?: return null
            return ByteRange((size - suffix).coerceAtLeast(0), size - 1)
        }
        val start = parts[0].toLongOrNull()?.takeIf { it in 0 until size } ?: return null
        val end = if (parts[1].isEmpty()) size - 1 else parts[1].toLongOrNull() ?: return null
        return if (end >= start) ByteRange(start, minOf(end, size - 1)) else null
    }

    fun create(
        file: LocalDirectoryStore.StreamFile,
        request: WebResourceRequest,
        allowOpaqueOrigin: Boolean
    ): WebResourceResponse? {
        val size = file.size
        val headers = linkedMapOf(
            "Cache-Control" to "no-store",
            "Content-Security-Policy" to "sandbox; default-src 'none'",
            "X-Content-Type-Options" to "nosniff",
            "Accept-Ranges" to if (size >= 0) "bytes" else "none"
        )
        if (allowOpaqueOrigin) {
            headers["Access-Control-Allow-Origin"] = "null"
            headers["Access-Control-Expose-Headers"] = "Accept-Ranges, Content-Range"
        }
        val requested = request.requestHeaders?.entries?.firstOrNull { it.key.equals("Range", true) }?.value
        val range = if (size >= 0 && !requested.isNullOrBlank()) {
            parseRange(requested, size) ?: return WebViewStreamRoutes.status(
                416, "Range Not Satisfiable", headers + ("Content-Range" to "bytes */$size")
            )
        } else null
        val length = range?.length ?: size
        if (length >= 0) headers["Content-Length"] = length.toString()
        if (range != null) headers["Content-Range"] = "bytes ${range.start}-${range.end}/$size"
        val source = file.open(0) ?: return null
        val streamEnd = range?.let { it.end + 1 } ?: size
        val body = if (request.method == "HEAD" && range == null) {
            source.close()
            ByteArrayInputStream(ByteArray(0))
        } else if (streamEnd >= 0) {
            BoundedFileStream(source, streamEnd)
        } else {
            source
        }
        val inlineMime = file.mime.takeIf {
            it.startsWith("audio/") || it.startsWith("video/") ||
                (it.startsWith("image/") && it != "image/svg+xml")
        } ?: "application/octet-stream"
        return WebResourceResponse(
            inlineMime, null, if (range == null) 200 else 206,
            if (range == null) "OK" else "Partial Content", headers, body
        )
    }

    private class BoundedFileStream(input: InputStream, private var remaining: Long) : FilterInputStream(input) {
        override fun read(): Int {
            if (remaining == 0L) return -1
            return super.read().also { if (it >= 0) remaining-- }
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (length == 0) return 0
            if (remaining == 0L) return -1
            return super.read(buffer, offset, minOf(length.toLong(), remaining).toInt())
                .also { if (it > 0) remaining -= it }
        }

        override fun available(): Int = remaining.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

        override fun skip(count: Long): Long {
            val requested = count.coerceIn(0, remaining)
            if (requested == 0L) return 0L
            val skipped = try {
                super.skip(requested)
            } catch (_: IOException) {
                0L
            }
            remaining -= skipped
            if (skipped == requested) return skipped

            val buffer = ByteArray(32 * 1024)
            var completed = skipped
            while (completed < requested) {
                val got = read(buffer, 0, minOf(requested - completed, buffer.size.toLong()).toInt())
                if (got <= 0) break
                completed += got
            }
            return completed
        }
    }
}
