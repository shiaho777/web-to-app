package com.webtoapp.core.shell

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class GalleryDeviceTreeTest {
    @Test
    fun `the granted tree is read one folder at a time without truncating long listings`() = runBlocking {
        val queries = mutableListOf<String>()
        val authority = "gallery.test"
        val tree = DocumentsContract.buildTreeDocumentUri(authority, "root")
        val provider = object : ContentProvider() {
            override fun onCreate(): Boolean = true

            override fun query(
                uri: Uri,
                projection: Array<out String>?,
                selection: String?,
                selectionArgs: Array<out String>?,
                sortOrder: String?
            ): Cursor {
                val id = DocumentsContract.getDocumentId(uri)
                queries.add(id)
                return MatrixCursor(requireNotNull(projection).map { it }.toTypedArray()).apply {
                    if (id == "root") {
                        addRow(arrayOf<Any>("nested", "Lessons", Document.MIME_TYPE_DIR, 0L))
                        addRow(arrayOf<Any>("text", "readme.txt", "text/plain", 0L))
                        for (index in 0 until 2_050) {
                            addRow(arrayOf<Any>("video$index", "Clip $index.mp4", "video/mp4", 0L))
                        }
                    } else {
                        addRow(arrayOf<Any>("lesson", "Introduction.mp4", "video/mp4", 50L))
                    }
                }
            }

            override fun getType(uri: Uri): String? = null
            override fun insert(uri: Uri, values: ContentValues?): Uri? = null
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
            override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
        }
        ShadowContentResolver.registerProviderInternal(authority, provider)
        val resolver = ApplicationProvider.getApplicationContext<Application>().contentResolver

        val root = readGalleryDeviceDirectory(resolver, tree)
        assertThat(queries).containsExactly("root")
        assertThat(root).hasSize(2_051)
        assertThat(root.first().id).isEqualTo("nested")
        assertThat(root.none { it.id == "text" }).isTrue()

        val child = readGalleryDeviceDirectory(resolver, tree, root.first().id).single()
        assertThat(queries).containsExactly("root", "nested").inOrder()
        val video = child.toGalleryShellItem(tree, index = 0)
        assertThat(video.name).isEqualTo("Introduction.mp4")
        assertThat(DocumentsContract.getTreeDocumentId(Uri.parse(video.assetPath))).isEqualTo("root")
        assertThat(DocumentsContract.getDocumentId(Uri.parse(video.assetPath))).isEqualTo("lesson")
    }
}
