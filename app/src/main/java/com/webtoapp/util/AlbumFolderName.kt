package com.webtoapp.util

/**
 * Subfolder used when a download is published into the system gallery.
 * One path segment. Empty and illegal names fall back to [DEFAULT].
 */
object AlbumFolderName {
    const val DEFAULT = "WebToApp"

    fun sanitize(raw: String?): String {
        val cleaned = raw.orEmpty()
            .trim()
            .replace(Regex("""[\\/:*?"<>|]"""), "")
            .trim('.')
            .take(64)
            .trim()
        return cleaned.ifBlank { DEFAULT }
    }
}
