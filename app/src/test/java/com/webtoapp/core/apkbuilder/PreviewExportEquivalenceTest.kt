package com.webtoapp.core.apkbuilder

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.GalleryConfig
import com.webtoapp.data.model.GalleryItem
import com.webtoapp.data.model.GalleryItemType
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.GsonProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PreviewExportEquivalenceTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun exportEquivalentBase(app: WebApp): ShellConfig =
        app.toPreviewShellConfig(context)

    private fun previewShell(app: WebApp): ShellConfig =
        com.webtoapp.core.host.ShellPreviewLauncher.buildPreviewConfig(context, app)

    @Test
    fun `web app preview and export produce identical shell config`() {
        val app = WebApp(
            name = "EquivWeb",
            url = "https://equiv.example.com",
            appType = AppType.WEB
        )
        val export = exportEquivalentBase(app)
        val preview = previewShell(app)

        assertEquivalentExceptPreviewOnlyFields(export, preview)
    }

    @Test
    fun `html app preview entryFile fallback does not diverge from export path`() {
        val app = WebApp(
            name = "EquivHtml",
            url = "https://equiv.example.com",
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(projectDir = null)
        )
        val export = exportEquivalentBase(app)
        val preview = previewShell(app)

        assertThat(export.htmlConfig.entryFile).isNotEmpty()
        assertEquivalentExceptPreviewOnlyFields(export, preview)
        assertThat(preview.htmlConfig.entryFile).isEqualTo(export.htmlConfig.entryFile)
    }

    @Test
    fun `gallery preview only rewrites asset paths, all other item fields match export`() {
        val app = WebApp(
            name = "EquivGallery",
            url = "https://equiv.example.com",
            appType = AppType.GALLERY,
            galleryConfig = GalleryConfig(
                items = listOf(
                    GalleryItem(
                        id = "g1",
                        path = "/original/img1.png",
                        type = GalleryItemType.IMAGE,
                        name = "Item One",
                        duration = 0,
                        thumbnailPath = "/original/thumb1.jpg"
                    ),
                    GalleryItem(
                        id = "g2",
                        path = "/original/vid2.mp4",
                        type = GalleryItemType.VIDEO,
                        name = "Item Two",
                        duration = 5000L,
                        thumbnailPath = null
                    )
                )
            )
        )
        val export = exportEquivalentBase(app)
        val preview = previewShell(app)

        assertThat(preview.galleryConfig.items).hasSize(2)
        assertThat(export.galleryConfig.items).hasSize(2)

        for ((idx, pair) in (preview.galleryConfig.items zip export.galleryConfig.items).withIndex()) {
            val (previewItem, exportItem) = pair
            assertThat(previewItem.id).isEqualTo(exportItem.id)
            assertThat(previewItem.name).isEqualTo(exportItem.name)
            assertThat(previewItem.duration).isEqualTo(exportItem.duration)
            assertThat(previewItem.type).isEqualTo(exportItem.type)
        }

        val previewPaths = preview.galleryConfig.items.map { it.assetPath }
        val exportPaths = export.galleryConfig.items.map { it.assetPath }
        assertThat(exportPaths).containsExactly("gallery/item_0.png", "gallery/item_1.mp4").inOrder()
        assertThat(previewPaths).containsExactly("/original/img1.png", "/original/vid2.mp4").inOrder()

        assertThat(preview.galleryConfig.items[0].thumbnailPath).isEqualTo("/original/thumb1.jpg")
        assertThat(export.galleryConfig.items[0].thumbnailPath).isEqualTo("gallery/thumb_0.jpg")
        assertThat(preview.galleryConfig.items[1].thumbnailPath).isNull()
        assertThat(export.galleryConfig.items[1].thumbnailPath).isNull()
    }

    @Test
    fun `gallery scalar config fields are identical between preview and export`() {
        val app = WebApp(
            name = "EquivGalleryScalar",
            url = "https://equiv.example.com",
            appType = AppType.GALLERY,
            galleryConfig = GalleryConfig(
                imageInterval = 7,
                loop = false,
                autoPlay = true,
                showThumbnailBar = false,
                gridColumns = 4
            )
        )
        val export = exportEquivalentBase(app)
        val preview = previewShell(app)

        assertThat(preview.galleryConfig.imageInterval).isEqualTo(export.galleryConfig.imageInterval)
        assertThat(preview.galleryConfig.loop).isEqualTo(export.galleryConfig.loop)
        assertThat(preview.galleryConfig.autoPlay).isEqualTo(export.galleryConfig.autoPlay)
        assertThat(preview.galleryConfig.showThumbnailBar).isEqualTo(export.galleryConfig.showThumbnailBar)
        assertThat(preview.galleryConfig.gridColumns).isEqualTo(export.galleryConfig.gridColumns)
    }

    @Test
    fun `preview-only fields are populated on preview but empty on export`() {
        val app = WebApp(
            name = "EquivPreviewFields",
            url = "https://equiv.example.com",
            appType = AppType.WEB
        )
        val export = exportEquivalentBase(app)
        val preview = previewShell(app)

        assertThat(export.previewContentDir).isEmpty()
        assertThat(export.siteDirName).isEmpty()
        assertThat(export.siteId).isEmpty()

        assertThat(preview.siteId).isEqualTo("preview_${app.id}")
    }

    @Test
    fun `multi-web preview and export produce equivalent site shape`() {
        val app = WebApp(
            name = "EquivMultiWeb",
            url = "https://equiv.example.com",
            appType = AppType.MULTI_WEB,
            multiWebConfig = com.webtoapp.data.model.MultiWebConfig(
                sites = listOf(
                    com.webtoapp.data.model.MultiWebSite(
                        id = "site-1",
                        name = "Site One",
                        url = "https://site1.example.com",
                        iconEmoji = "SEN-ICON",
                        themeColor = "#SEN-COLOR"
                    )
                )
            )
        )
        val export = exportEquivalentBase(app)
        val preview = previewShell(app)

        assertThat(preview.multiWebConfig.sites).hasSize(1)
        assertThat(export.multiWebConfig.sites).hasSize(1)

        val previewSite = preview.multiWebConfig.sites[0]
        val exportSite = export.multiWebConfig.sites[0]
        assertThat(previewSite.id).isEqualTo(exportSite.id)
        assertThat(previewSite.name).isEqualTo(exportSite.name)
        assertThat(previewSite.url).isEqualTo(exportSite.url)
        assertThat(previewSite.iconEmoji).isEqualTo(exportSite.iconEmoji)
        assertThat(previewSite.themeColor).isEqualTo(exportSite.themeColor)
    }

    private fun assertEquivalentExceptPreviewOnlyFields(export: ShellConfig, preview: ShellConfig) {
        val normalizedExport = export.copy(
            previewContentDir = preview.previewContentDir,
            siteDirName = preview.siteDirName,
            siteId = preview.siteId
        )
        assertThat(preview).isEqualTo(normalizedExport)
    }
}
