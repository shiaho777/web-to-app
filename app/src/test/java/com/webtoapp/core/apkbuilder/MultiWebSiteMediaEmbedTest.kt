package com.webtoapp.core.apkbuilder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Zip-level proof that multi-web site media lands where the shell players
 * look: gallery sites under assets/multiweb_sites/<siteId>/gallery/ and
 * single-media sites under assets/multiweb_sites/<siteId>/media_content.*.
 * Before the fix, site media was never embedded at all (black cells).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MultiWebSiteMediaEmbedTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun zipEntries(bytes: ByteArray): Map<String, ByteArray> {
        val out = mutableMapOf<String, ByteArray>()
        ZipInputStream(bytes.inputStream()).use { zin ->
            while (true) {
                val entry = zin.nextEntry ?: break
                out[entry.name] = zin.readBytes()
                zin.closeEntry()
            }
        }
        return out
    }

    private fun galleryItem(dir: java.io.File, name: String, type: GalleryItemType): GalleryItem {
        val file = java.io.File(dir, name).also { it.writeBytes(byteArrayOf(1, 2, 3, 4)) }
        return GalleryItem(path = file.absolutePath, type = type, name = name)
    }

    @Test
    fun `site gallery items embed under the site prefix`() {
        val dir = createTempDir("mwgal")
        try {
            val builder = ApkBuilder(context)
            val items = listOf(
                galleryItem(dir, "a.png", GalleryItemType.IMAGE),
                galleryItem(dir, "b.mp4", GalleryItemType.VIDEO)
            )
            val baos = ByteArrayOutputStream()
            java.util.zip.ZipOutputStream(baos).use { zipOut ->
                builder.addGalleryItemsToAssets(
                    zipOut, items, null,
                    com.webtoapp.core.crypto.EncryptionConfig.DISABLED,
                    multiWebSiteGalleryAssetPrefix("s1")
                )
            }
            val entries = zipEntries(baos.toByteArray())
            assertThat(entries.keys).containsAtLeast(
                "assets/multiweb_sites/s1/gallery/item_0.png",
                "assets/multiweb_sites/s1/gallery/item_1.mp4"
            )
            assertThat(entries["assets/multiweb_sites/s1/gallery/item_0.png"])
                .isEqualTo(byteArrayOf(1, 2, 3, 4))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `standalone gallery prefix is unchanged`() {
        val dir = createTempDir("mwgalroot")
        try {
            val builder = ApkBuilder(context)
            val items = listOf(galleryItem(dir, "a.png", GalleryItemType.IMAGE))
            val baos = ByteArrayOutputStream()
            java.util.zip.ZipOutputStream(baos).use { zipOut ->
                builder.addGalleryItemsToAssets(
                    zipOut, items, null,
                    com.webtoapp.core.crypto.EncryptionConfig.DISABLED
                )
            }
            val entries = zipEntries(baos.toByteArray())
            assertThat(entries.keys).contains("assets/gallery/item_0.png")
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `site single media embeds under the site prefix`() {
        val dir = createTempDir("mwmed")
        try {
            val builder = ApkBuilder(context)
            val media = java.io.File(dir, "v.mp4").also { it.writeBytes(byteArrayOf(9, 9, 9)) }
            val baos = ByteArrayOutputStream()
            java.util.zip.ZipOutputStream(baos).use { zipOut ->
                builder.addMediaContentToAssets(
                    zipOut, media.absolutePath, true, null,
                    com.webtoapp.core.crypto.EncryptionConfig.DISABLED,
                    "multiweb_sites/s2/media_content.mp4"
                )
            }
            val entries = zipEntries(baos.toByteArray())
            assertThat(entries.keys).contains("assets/multiweb_sites/s2/media_content.mp4")
            assertThat(entries["assets/multiweb_sites/s2/media_content.mp4"])
                .isEqualTo(byteArrayOf(9, 9, 9))
        } finally {
            dir.deleteRecursively()
        }
    }

    private fun createTempDir(prefix: String): java.io.File {
        val dir = java.io.File(context.cacheDir, "$prefix-${System.nanoTime()}")
        dir.mkdirs()
        return dir
    }
}
