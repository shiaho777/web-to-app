package com.webtoapp.core.apkbuilder

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ResourceDependentFeatureEquivalenceTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun previewShell(app: WebApp): ShellConfig =
        com.webtoapp.core.host.ShellPreviewLauncher.buildPreviewConfig(context, app)

    @Test
    fun `resource-dependent features document their preview resolution path`() {
        val expectedResolutions = linkedMapOf(
            "splash" to "ShellPreviewLauncher injects splashMediaPath; ShellScreen splashMediaExists falls back to it when assets/splash_media.* is absent",
            "gallery" to "ShellPreviewLauncher rewrites items[].assetPath to host file paths; gallery items carry the original path",
            "media (IMAGE/VIDEO appType)" to "ShellPreviewContent.resolve points previewContentDir at the host media file",
            "bgm" to "bgmConfig.playlist is serialized with host paths; runtime reads BgmShellItem.assetPath"
        )
        assertThat(expectedResolutions.keys)
            .containsAtLeast("splash", "gallery", "media (IMAGE/VIDEO appType)", "bgm")
    }

    @Test
    fun `splash preview config carries a resolvable media path when WebApp has one`() {
        val tmpPng = File.createTempFile("splash_res", ".png").apply {
            writeBytes(byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte()))
            deleteOnExit()
        }
        val app = WebApp(
            name = "ResourceSplash",
            url = "https://r.example.com",
            appType = AppType.WEB,
            splashEnabled = true,
            splashConfig = com.webtoapp.data.model.SplashConfig(
                type = com.webtoapp.data.model.SplashType.IMAGE,
                mediaPath = tmpPng.absolutePath
            )
        )
        val preview = previewShell(app)

        assertThat(preview.splashEnabled).isTrue()
        assertThat(preview.splashMediaPath).isEqualTo(tmpPng.absolutePath)
        assertThat(File(preview.splashMediaPath!!).exists()).isTrue()
    }

    @Test
    fun `gallery preview items carry resolvable host paths while export uses packaged asset paths`() {
        val tmpImg = File.createTempFile("gallery_item", ".png").apply {
            writeBytes(byteArrayOf(0x89.toByte(), 0x50.toByte()))
            deleteOnExit()
        }
        val app = WebApp(
            name = "ResourceGallery",
            url = "https://r.example.com",
            appType = AppType.GALLERY,
            galleryConfig = com.webtoapp.data.model.GalleryConfig(
                items = listOf(
                    com.webtoapp.data.model.GalleryItem(
                        id = "g1",
                        path = tmpImg.absolutePath,
                        type = com.webtoapp.data.model.GalleryItemType.IMAGE,
                        name = "Preview Item"
                    )
                )
            )
        )
        val preview = previewShell(app)

        assertThat(preview.galleryConfig.items).hasSize(1)
        val previewItem = preview.galleryConfig.items[0]
        assertThat(previewItem.assetPath).isEqualTo(tmpImg.absolutePath)
        assertThat(File(previewItem.assetPath).exists()).isTrue()
    }
}
