package com.webtoapp.core.shell

import android.content.ContentResolver
import android.net.Uri
import android.os.CancellationSignal
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import android.webkit.MimeTypeMap
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class GalleryDeviceEntry(
    val id: String,
    val name: String,
    val isDirectory: Boolean,
    val lastModified: Long
)

internal suspend fun readGalleryDeviceDirectory(
    resolver: ContentResolver,
    tree: Uri,
    directoryId: String = DocumentsContract.getTreeDocumentId(tree)
): List<GalleryDeviceEntry> = coroutineScope {
    val cancellation = CancellationSignal()
    val cancelQuery = launch(start = CoroutineStart.UNDISPATCHED) {
        try {
            awaitCancellation()
        } finally {
            cancellation.cancel()
        }
    }
    try {
        withContext(Dispatchers.IO) {
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, directoryId)
            val projection = arrayOf(
                Document.COLUMN_DOCUMENT_ID,
                Document.COLUMN_DISPLAY_NAME,
                Document.COLUMN_MIME_TYPE,
                Document.COLUMN_LAST_MODIFIED
            )
            val entries = resolver.query(children, projection, null, null, null, cancellation)?.use { cursor ->
                buildList<GalleryDeviceEntry> {
                    while (cursor.moveToNext()) {
                        ensureActive()
                        val id = cursor.getString(0) ?: continue
                        val name = cursor.getString(1)?.takeIf { it.isNotBlank() } ?: id
                        val mime = cursor.getString(2)
                        val isDirectory = mime == Document.MIME_TYPE_DIR
                        if (isDirectory || isGalleryDeviceVideo(mime, name)) {
                            add(GalleryDeviceEntry(id, name, isDirectory, cursor.getLong(3)))
                        }
                    }
                }
            } ?: throw IOException("Directory provider returned no listing")
            entries.distinctBy { it.id }.sortedWith(
                compareBy<GalleryDeviceEntry> { !it.isDirectory }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                    .thenBy { it.id }
            )
        }
    } finally {
        cancelQuery.cancel()
    }
}

internal fun isGalleryDeviceVideo(mime: String?, name: String): Boolean {
    if (mime?.startsWith("video/", ignoreCase = true) == true) return true
    if (!mime.isNullOrBlank() && mime != "application/octet-stream") return false
    val extension = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.startsWith("video/") == true
}

internal fun GalleryDeviceEntry.toGalleryShellItem(tree: Uri, index: Int): GalleryShellItem =
    GalleryShellItem(
        id = id,
        assetPath = DocumentsContract.buildDocumentUriUsingTree(tree, id).toString(),
        type = "VIDEO",
        name = name,
        sortIndex = index,
        createdAt = lastModified
    )
