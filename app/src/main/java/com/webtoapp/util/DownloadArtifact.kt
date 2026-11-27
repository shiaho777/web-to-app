package com.webtoapp.util

import java.io.EOFException
import java.io.InputStream
import java.io.PushbackInputStream

internal object DownloadArtifact {
    fun isApk(fileName: String, mimeType: String): Boolean =
        fileName.endsWith(".apk", ignoreCase = true) ||
            mimeType.substringBefore(';').trim().equals("application/vnd.android.package-archive", ignoreCase = true)

    fun requireContent(input: InputStream, emptyMessage: String): InputStream {
        val checked = PushbackInputStream(input)
        val firstByte = checked.read()
        if (firstByte == -1) throw EOFException(emptyMessage)
        checked.unread(firstByte)
        return checked
    }
}
