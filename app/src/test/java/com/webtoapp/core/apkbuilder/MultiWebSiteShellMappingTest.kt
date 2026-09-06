package com.webtoapp.core.apkbuilder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import com.webtoapp.data.model.MediaConfig
import com.webtoapp.data.model.WebApp
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the multi-web site shell-config mapping end to end (minus the DB):
 * export rewrites gallery items to the per-site asset prefix, preview maps
 * them (and single media) to absolute host files the shell players can read.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MultiWebSiteShellMappingTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun tempMedia(dir: File, name: String): File =
        File(dir, name).also { it.writeBytes(byteArrayOf(7, 7, 7)) }

    private fun gallerySource(dir: File): WebApp {
        val photo = tempMedia(dir, "a.png")
        return WebApp(
            id = 11,
            name = "gallery-src",
            url = "",
            appType = AppType.GALLERY,
            galleryConfig = GalleryConfig(
                items = listOf(
                    GalleryItem(path = photo.absolutePath, type = GalleryItemType.IMAGE, name = "a")
                )
            )
        )
    }

    @Test
    fun `export maps gallery site items to the site asset prefix`() {
        val dir = File(context.cacheDir, "mwmap-${System.nanoTime()}").also { it.mkdirs() }
        try {
            val shell = buildSiteShellConfig(gallerySource(dir), "com.example", "s9", context, isPreview = false)
            assertThat(shell.siteId).isEqualTo("s9")
            val items = shell.galleryConfig.items
            assertThat(items).hasSize(1)
            assertThat(items[0].assetPath).isEqualTo("multiweb_sites/s9/gallery/item_0.png")
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `preview maps gallery site items to host files`() {
        val dir = File(context.cacheDir, "mwmap-${System.nanoTime()}").also { it.mkdirs() }
        try {
            val photo = tempMedia(dir, "b.png")
            val source = WebApp(
                id = 12,
                name = "gallery-src",
                url = "",
                appType = AppType.GALLERY,
                galleryConfig = GalleryConfig(
                    items = listOf(
                        GalleryItem(path = photo.absolutePath, type = GalleryItemType.IMAGE, name = "b")
                    )
                )
            )
            val shell = buildSiteShellConfig(source, "preview", "s9", context, isPreview = true)
            val items = shell.galleryConfig.items
            assertThat(items).hasSize(1)
            assertThat(items[0].assetPath).isEqualTo(photo.absolutePath)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `preview sets previewMediaPath for local single media`() {
        val dir = File(context.cacheDir, "mwmap-${System.nanoTime()}").also { it.mkdirs() }
        try {
            val video = tempMedia(dir, "v.mp4")
            val source = WebApp(
                id = 13,
                name = "video-src",
                url = "",
                appType = AppType.VIDEO,
                mediaConfig = MediaConfig(mediaPath = video.absolutePath)
            )
            val shell = buildSiteShellConfig(source, "preview", "s9", context, isPreview = true)
            assertThat(shell.previewMediaPath).isEqualTo(video.absolutePath)

            val exported = buildSiteShellConfig(source, "com.example", "s9", context, isPreview = false)
            assertThat(exported.previewMediaPath).isNull()
        } finally {
            dir.deleteRecursively()
        }
    }
}
