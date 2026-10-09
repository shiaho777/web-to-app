package com.webtoapp.core.apkbuilder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.zip.ZipFile
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Regression tests for the launcher icon pipeline (issue #529: a transparent-background
 * icon image was composited twice — full-bleed background layer plus safe-zone foreground).
 *
 * The generated APK must back the adaptive icon with a solid derived color, keep the
 * foreground aspect-correct inside the safe zone, and give legacy mipmap entries
 * density-appropriate sizes instead of stretched 432px foreground padding.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LauncherIconGenerationTest {

    @get:Rule
    val temp = TemporaryFolder()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val template = ApkTemplate(context)

    // --- background color derivation ---

    @Test
    fun `transparent logo with dark subject derives a white background`() {
        val logo = transparentLogo(subjectColor = Color.BLACK)

        assertThat(ApkTemplate.deriveLauncherBackgroundColor(logo)).isEqualTo(0xFFFFFFFF.toInt())
    }

    @Test
    fun `transparent logo with light subject derives a white background`() {
        val logo = transparentLogo(subjectColor = Color.WHITE)

        assertThat(ApkTemplate.deriveLauncherBackgroundColor(logo)).isEqualTo(0xFFFFFFFF.toInt())
    }

    @Test
    fun `explicit background color overrides derivation`() {
        val logo = transparentLogo(subjectColor = Color.WHITE)

        val png = ApkTemplate.createSolidBackgroundIcon(logo, 64, 0xFF3366CC.toInt())
        val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)
        val pixels = IntArray(64 * 64)
        decoded.getPixels(pixels, 0, 64, 0, 0, 64, 64)

        assertThat(pixels.distinct()).containsExactly(0xFF3366CC.toInt())
    }

    @Test
    fun `parseIconBackgroundColor accepts rgb and argb hex`() {
        assertThat(ApkTemplate.parseIconBackgroundColor(null)).isNull()
        assertThat(ApkTemplate.parseIconBackgroundColor("")).isNull()
        assertThat(ApkTemplate.parseIconBackgroundColor("#FFFFFF")).isEqualTo(0xFFFFFFFF.toInt())
        assertThat(ApkTemplate.parseIconBackgroundColor("#000000")).isEqualTo(0xFF000000.toInt())
        assertThat(ApkTemplate.parseIconBackgroundColor("#80FF0000")).isEqualTo(0xFFFF0000.toInt())
        assertThat(ApkTemplate.parseIconBackgroundColor("not-a-color")).isNull()
    }

    @Test
    fun `fully transparent image falls back to white`() {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)

        assertThat(ApkTemplate.deriveLauncherBackgroundColor(bitmap)).isEqualTo(0xFFFFFFFF.toInt())
    }

    @Test
    fun `opaque image keeps its dominant border color as background`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(0xFF3366CC.toInt())

        val derived = ApkTemplate.deriveLauncherBackgroundColor(bitmap)

        assertThat(Color.alpha(derived)).isEqualTo(255)
        assertThat(Math.abs(((derived shr 16) and 0xFF) - 0x33)).isAtMost(8)
        assertThat(Math.abs(((derived shr 8) and 0xFF) - 0x66)).isAtMost(8)
        assertThat(Math.abs((derived and 0xFF) - 0xCC)).isAtMost(8)
    }

    @Test
    fun `solid background png is uniform and fully opaque`() {
        val logo = transparentLogo(subjectColor = Color.BLACK)

        val png = ApkTemplate.createSolidBackgroundIcon(logo, 432)
        val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)

        assertThat(decoded.width).isEqualTo(432)
        assertThat(decoded.height).isEqualTo(432)

        val pixels = IntArray(432 * 432)
        decoded.getPixels(pixels, 0, 432, 0, 0, 432, 432)
        assertThat(pixels.distinct()).containsExactly(0xFFFFFFFF.toInt())
    }

    // --- foreground / legacy geometry ---

    @Test
    fun `adaptive foreground preserves aspect ratio inside the safe zone`() {
        val logo = transparentLogo(subjectColor = Color.BLACK)

        val png = template.createAdaptiveForegroundIcon(logo, 432)
        val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)

        assertThat(decoded.width).isEqualTo(432)
        assertThat(decoded.height).isEqualTo(432)

        val bounds = opaqueBounds(decoded)
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()

        assertThat(width / height).isWithin(0.2f).of(2f)
        // Safe zone is the central 72% (288px of 432); the content must not spill out.
        assertThat(bounds.left).isAtLeast(72 - 8)
        assertThat(bounds.top).isAtLeast(72 - 8)
        assertThat(bounds.right).isAtMost(360 + 8)
        assertThat(bounds.bottom).isAtMost(360 + 8)
    }

    @Test
    fun `square foreground fills the safe zone`() {
        val logo = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        logo.eraseColor(Color.BLACK)

        val png = template.createAdaptiveForegroundIcon(logo, 432)
        val bounds = opaqueBounds(BitmapFactory.decodeByteArray(png, 0, png.size))

        assertThat(bounds.width().toFloat()).isWithin(8f).of(288f)
        assertThat(bounds.height().toFloat()).isWithin(8f).of(288f)
    }

    @Test
    fun `legacy launcher png letterboxes instead of stretching`() {
        val logo = transparentLogo(subjectColor = Color.BLACK)

        val png = template.scaleBitmapToPng(logo, 48)
        val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)

        assertThat(decoded.width).isEqualTo(48)
        assertThat(decoded.height).isEqualTo(48)

        val bounds = opaqueBounds(decoded)
        assertThat(bounds.width().toFloat() / bounds.height()).isWithin(0.25f).of(2f)
    }

    @Test
    fun `round icon letterboxes inside the circle mask`() {
        val logo = transparentLogo(subjectColor = Color.BLACK)

        val png = template.createRoundIcon(logo, 96)
        val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)

        assertThat(decoded.width).isEqualTo(96)
        assertThat(decoded.height).isEqualTo(96)

        // A 2:1 wide logo inside a circle stays wider than tall, and the circle corners stay clear.
        val bounds = opaqueBounds(decoded)
        assertThat(bounds.width()).isGreaterThan(bounds.height())
        assertThat(decoded.getPixel(2, 2) ushr 24).isEqualTo(0)
    }

    // --- full pipeline against the real shell template ---

    @Test
    fun `modifyApk writes a solid background and layer-correct icon entries`() {
        val templateApk = resolveFile(
            "src/main/assets/template/webview_shell.apk",
            "app/src/main/assets/template/webview_shell.apk"
        )
        assumeTrue("shell template not built — run ':app:syncShellTemplateApk' first", templateApk.exists())

        val logoFile = temp.newFile("icon.png")
        transparentLogo(subjectColor = Color.BLACK)
            .compress(Bitmap.CompressFormat.PNG, 100, logoFile.outputStream())

        val outputApk = temp.newFile("generated.apk")
        val config = ApkConfig(
            meta = MetaBlock(
                appName = "IconFix",
                packageName = "com.example.iconfix",
                targetUrl = "https://example.com",
                appType = "WEB"
            )
        )

        runBlocking {
            ApkBuilder(context).modifyApk(
                sourceApk = templateApk,
                outputApk = outputApk,
                config = config,
                iconPath = logoFile.absolutePath,
                splashMediaPath = null
            ) { _, _ -> }
        }

        assertThat(outputApk.exists()).isTrue()
        assertThat(outputApk.length()).isGreaterThan(0)

        val specs = ArscRebuilder().let { rebuilder ->
            val arsc = ZipFile(templateApk).use {
                it.getInputStream(it.getEntry("resources.arsc")).readBytes()
            }
            rebuilder.rebuildWithNewAppNameAndIcons(arsc, "IconFix", replaceIcons = true)
            rebuilder.getLastDiscoveredIconSpecs()
        }

        assertThat(specs.filter { it.kind == ArscRebuilder.LauncherIconKind.FOREGROUND }).isNotEmpty()
        assertThat(specs.filter { it.kind == ArscRebuilder.LauncherIconKind.LAUNCHER }).isNotEmpty()
        assertThat(specs.filter { it.kind == ArscRebuilder.LauncherIconKind.ROUND }).isNotEmpty()

        ZipFile(outputApk).use { zip ->
            // The background layer must be a uniform opaque color, not the user image.
            val bgEntry = zip.getEntry(ArscRebuilder.LAUNCHER_BACKGROUND_DRAWABLE_PATH)
            assertThat(bgEntry).isNotNull()
            val bgBytes = zip.getInputStream(bgEntry).readBytes()
            val bg = BitmapFactory.decodeByteArray(bgBytes, 0, bgBytes.size)
            val bgPixels = IntArray(bg.width * bg.height)
            bg.getPixels(bgPixels, 0, bg.width, 0, 0, bg.width, bg.height)
            assertThat(bgPixels.distinct()).hasSize(1)
            assertThat(bgPixels.first() ushr 24).isEqualTo(255)
            // Dark subject on a transparent logo must get a light backing.
            assertThat(bgPixels.first()).isEqualTo(0xFFFFFFFF.toInt())

            specs.forEach { spec ->
                val entry = zip.getEntry(spec.path)
                assertThat(entry).isNotNull()
                val entryBytes = zip.getInputStream(entry).readBytes()
                val bitmap = BitmapFactory.decodeByteArray(entryBytes, 0, entryBytes.size)

                when (spec.kind) {
                    ArscRebuilder.LauncherIconKind.FOREGROUND -> {
                        assertThat(bitmap.width).isEqualTo(432)
                        assertThat(bitmap.height).isEqualTo(432)
                        val bounds = opaqueBounds(bitmap)
                        assertThat(bounds.width().toFloat() / bounds.height()).isWithin(0.25f).of(2f)
                    }
                    ArscRebuilder.LauncherIconKind.LAUNCHER,
                    ArscRebuilder.LauncherIconKind.ROUND -> {
                        assertThat(bitmap.width).isEqualTo(bitmap.height)
                        assertThat(bitmap.width).isIn(expectedLegacySizes())
                        val bounds = opaqueBounds(bitmap)
                        assertThat(bounds.width().toFloat() / bounds.height()).isWithin(0.3f).of(2f)
                    }
                }
            }
        }
    }

    // --- helpers ---

    /** 2:1 wide logo with a transparent background — the shape from issue #529. */
    private fun transparentLogo(subjectColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(200 * 100) { Color.TRANSPARENT }
        for (y in 25 until 75) {
            for (x in 50 until 150) {
                pixels[y * 200 + x] = subjectColor
            }
        }
        bitmap.setPixels(pixels, 0, 200, 0, 0, 200, 100)
        return bitmap
    }

    private fun opaqueBounds(bitmap: Bitmap): android.graphics.Rect {
        var left = bitmap.width
        var top = bitmap.height
        var right = -1
        var bottom = -1
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if ((bitmap.getPixel(x, y) ushr 24) >= 128) {
                    if (x < left) left = x
                    if (x > right) right = x
                    if (y < top) top = y
                    if (y > bottom) bottom = y
                }
            }
        }
        assertThat(right).isGreaterThan(left)
        assertThat(bottom).isGreaterThan(top)
        return android.graphics.Rect(left, top, right + 1, bottom + 1)
    }

    private fun expectedLegacySizes() = setOf(36, 48, 72, 96, 144, 192)

    private fun resolveFile(vararg candidates: String): File {
        for (c in candidates) {
            val f = File(c)
            if (f.exists()) return f
            val f2 = File(System.getProperty("user.dir"), c)
            if (f2.exists()) return f2
        }
        return File(candidates.first())
    }
}
