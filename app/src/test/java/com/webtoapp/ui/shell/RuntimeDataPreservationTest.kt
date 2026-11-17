package com.webtoapp.ui.shell

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RuntimeDataPreservationTest {

    @get:Rule
    val temp = TemporaryFolder()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `extraction token ignores reinstall timestamp for the same version`() {
        val tokenA = buildExtractionToken(context, "wordpress", 7, "My Site")
        val tokenB = buildExtractionToken(context, "wordpress", 7, "My Site")
        assertThat(tokenA).isEqualTo(tokenB)
        assertThat(tokenA).doesNotContain("apkUpdated")
    }

    @Test
    fun `extraction token still changes with config version and extra`() {
        val base = buildExtractionToken(context, "wordpress", 7, "My Site")
        assertThat(buildExtractionToken(context, "wordpress", 8, "My Site")).isNotEqualTo(base)
        assertThat(buildExtractionToken(context, "wordpress", 7, "Other Site")).isNotEqualTo(base)
        assertThat(buildExtractionToken(context, "php", 7, "My Site")).isNotEqualTo(base)
    }

    private val freshBundle: (android.content.Context, String, File) -> Unit = { _, _, dest ->
        dest.mkdirs()
        File(dest, "wp-config.php").writeText("new-config")
        File(dest, "wp-content/database").mkdirs()
        File(dest, "wp-content/database/.ht.sqlite").writeText("bundled-empty-db")
    }

    @Test
    fun `refresh preserves runtime data while replacing bundled files`() {
        val dest = temp.newFolder("wordpress_site")
        File(dest, "wp-content/database").mkdirs()
        File(dest, "wp-content/database/.ht.sqlite").writeText("user-data")
        File(dest, "wp-content/uploads").mkdirs()
        File(dest, "wp-content/uploads/photo.jpg").writeText("uploaded")
        File(dest, "wp-config.php").writeText("old-config")

        refreshRuntimeAssetsPreservingData(
            context,
            "wordpress",
            dest,
            setOf("wp-content/database", "wp-content/uploads"),
            freshBundle
        )

        assertThat(File(dest, "wp-content/database/.ht.sqlite").readText()).isEqualTo("user-data")
        assertThat(File(dest, "wp-content/uploads/photo.jpg").readText()).isEqualTo("uploaded")
        assertThat(File(dest, "wp-config.php").readText()).isEqualTo("new-config")
    }

    @Test
    fun `refresh without preserved paths behaves like a clean extract`() {
        val dest = temp.newFolder("site")
        File(dest, "stale.txt").writeText("stale")
        File(dest, "wp-content/database").mkdirs()
        File(dest, "wp-content/database/.ht.sqlite").writeText("old-data")

        refreshRuntimeAssetsPreservingData(
            context,
            "wordpress",
            dest,
            emptySet(),
            freshBundle
        )

        assertThat(File(dest, "stale.txt").exists()).isFalse()
        assertThat(File(dest, "wp-content/database/.ht.sqlite").readText()).isEqualTo("bundled-empty-db")
    }
}
