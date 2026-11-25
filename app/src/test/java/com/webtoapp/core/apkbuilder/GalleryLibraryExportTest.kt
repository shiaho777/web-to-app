package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.backup.toRestoredWebApp
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.GalleryCategory
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import com.webtoapp.data.model.GallerySortOrder
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.GsonProvider
import org.junit.Test

class GalleryLibraryExportTest {
    private val folder = GalleryCategory(id = "lessons", name = "Lessons")
    private val video = GalleryItem(
        id = "video",
        path = "/editor/video.mp4",
        type = GalleryItemType.VIDEO,
        name = "Zebra",
        categoryId = folder.id
    )
    private val image = GalleryItem(
        id = "image",
        path = "/editor/image.png",
        type = GalleryItemType.IMAGE,
        name = "Artwork"
    )

    @Test
    fun `folders survive export and asset names follow the selected order`() {
        val exported = export(
            GalleryConfig(
                items = listOf(video, image),
                categories = listOf(folder),
                sortOrder = GallerySortOrder.NAME_ASC
            )
        )

        assertThat(exported.galleryConfig.categories).containsExactly(folder)
        assertThat(exported.galleryConfig.items.map { it.id to it.assetPath }).containsExactly(
            "image" to "gallery/item_0.png",
            "video" to "gallery/item_1.mp4"
        ).inOrder()
        assertThat(exported.galleryConfig.items.last().categoryId).isEqualTo(folder.id)
        val restored = checkNotNull(exported.toRestoredWebApp().galleryConfig)
        assertThat(restored.categories).containsExactly(folder)
        assertThat(restored.getItemsByCategory(folder.id).map { it.id }).containsExactly(video.id)
    }

    @Test
    fun `a device folder gallery exports without bundling saved media or requiring a host directory`() {
        val config = GalleryConfig(items = listOf(video), useDeviceFolder = true)
        val exported = export(config)

        assertThat(exported.galleryConfig.useDeviceFolder).isTrue()
        assertThat(exported.galleryConfig.items).isEmpty()
        assertThat(exported.toRestoredWebApp().galleryConfig?.useDeviceFolder).isTrue()
        assertThat(BuildInputPreflight.check(BuildInputPreflightRequest("GALLERY", galleryUsesDeviceFolder = true)).passed)
            .isTrue()
        assertThat(BuildInputPreflight.check(BuildInputPreflightRequest("GALLERY")).passed)
            .isFalse()
    }

    @Test
    fun `older galleries still load as bundled media`() {
        val json = """{"appType":"GALLERY","galleryConfig":{"items":[]}}"""
        val shell = GsonProvider.gson.fromJson(json, ShellConfig::class.java)
        assertThat(shell.galleryConfig.useDeviceFolder).isFalse()
        assertThat(shell.galleryConfig.categories).isEmpty()
    }

    private fun export(config: GalleryConfig): ShellConfig {
        val project = WebApp(name = "Videos", url = "", appType = AppType.GALLERY, galleryConfig = config)
        val json = ApkConfigJsonFactory.toShellConfigJson(project.toApkConfig("example.videos"))
        return GsonProvider.gson.fromJson(json, ShellConfig::class.java)
    }
}
