package com.webtoapp.core.apkbuilder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.webtoapp.core.shell.BgmShellItem
import com.webtoapp.core.shell.LrcShellTheme
import java.io.*
import java.util.zip.*

class ApkTemplate(private val context: Context) {

    companion object {

        private const val TEMPLATE_APK = "template/webview_shell.apk"

        const val CONFIG_PATH = "assets/app_config.json"

        val ICON_PATHS = listOf(
            "res/mipmap-mdpi-v4/ic_launcher.png" to 48,
            "res/mipmap-hdpi-v4/ic_launcher.png" to 72,
            "res/mipmap-xhdpi-v4/ic_launcher.png" to 96,
            "res/mipmap-xxhdpi-v4/ic_launcher.png" to 144,
            "res/mipmap-xxxhdpi-v4/ic_launcher.png" to 192
        )

        val ROUND_ICON_PATHS = listOf(
            "res/mipmap-mdpi-v4/ic_launcher_round.png" to 48,
            "res/mipmap-hdpi-v4/ic_launcher_round.png" to 72,
            "res/mipmap-xhdpi-v4/ic_launcher_round.png" to 96,
            "res/mipmap-xxhdpi-v4/ic_launcher_round.png" to 144,
            "res/mipmap-xxxhdpi-v4/ic_launcher_round.png" to 192
        )

        /**
         * Picks a solid color to back an adaptive-icon foreground layer.
         *
         * The user image must never be reused as the background layer: launchers composite
         * background + foreground, so an image-backed background shows the whole picture
         * behind the safe-zone subject (issue: "transparent background icon duplicates it
         * behind the subject"). Opaque images keep their dominant border color so the icon
         * still reads as a full tile; transparent logos fall back to white/black by
         * luminance contrast against the subject.
         */
        fun deriveLauncherBackgroundColor(bitmap: Bitmap): Int {
            // Sample a coarse grid straight from the source pixels; downscaling through
            // createScaledBitmap is neither needed nor reliable across renderers.
            val cols = minOf(32, bitmap.width)
            val rows = minOf(32, bitmap.height)
            val band = maxOf(1, minOf(cols, rows) / 8)

            val borderCounts = HashMap<Int, Int>()
            val allCounts = HashMap<Int, Int>()
            var borderOpaque = 0
            var borderCells = 0

            for (gy in 0 until rows) {
                val y = ((gy + 0.5f) * bitmap.height / rows).toInt().coerceIn(0, bitmap.height - 1)
                for (gx in 0 until cols) {
                    val x = ((gx + 0.5f) * bitmap.width / cols).toInt().coerceIn(0, bitmap.width - 1)

                    val isBorder = gx < band || gy < band || gx >= cols - band || gy >= rows - band
                    if (isBorder) {
                        borderCells++
                    }

                    val pixel = bitmap.getPixel(x, y)
                    if ((pixel ushr 24) < 128) continue

                    val quantized = quantizeColor(pixel)
                    allCounts[quantized] = (allCounts[quantized] ?: 0) + 1
                    if (isBorder) {
                        borderCounts[quantized] = (borderCounts[quantized] ?: 0) + 1
                        borderOpaque++
                    }
                }
            }

            if (borderOpaque * 2 >= borderCells) {
                borderCounts.maxByOrNull { it.value }?.let { return it.key }
            }

            val dominant = allCounts.maxByOrNull { it.value }?.key
                ?: return 0xFFFFFFFF.toInt()
            return if (luminance(dominant) >= 0.5f) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }

        fun createSolidBackgroundIcon(bitmap: Bitmap, size: Int): ByteArray {
            val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            output.eraseColor(deriveLauncherBackgroundColor(bitmap))

            val baos = ByteArrayOutputStream()
            output.compress(Bitmap.CompressFormat.PNG, 100, baos)
            output.recycle()
            return baos.toByteArray()
        }

        private fun quantizeColor(color: Int): Int {
            val r = (color shr 16) and 0xF8
            val g = (color shr 8) and 0xF8
            val b = color and 0xF8
            return 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
        }

        private fun luminance(color: Int): Float {
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            return (0.299f * r + 0.587f * g + 0.114f * b) / 255f
        }
    }

    private val templateDir = File(context.cacheDir, "apk_templates")

    init {
        templateDir.mkdirs()
    }

    fun getTemplateApk(): File? {
        val templateFile = File(templateDir, "webview_shell.apk")

        if (templateFile.exists()) {
            return templateFile
        }

        return try {
            context.assets.open(TEMPLATE_APK).use { input ->
                FileOutputStream(templateFile).use { output ->
                    input.copyTo(output)
                }
            }
            templateFile
        } catch (e: Exception) {

            null
        }
    }

    fun hasTemplate(): Boolean {
        return try {
            context.assets.open(TEMPLATE_APK).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun createConfigJson(config: ApkConfig): String =
        ApkConfigJsonFactory.create(config)

    fun createEncryptedStubJson(config: ApkConfig): String =
        ApkConfigJsonFactory.createEncryptedStub(config)

    fun scaleBitmapToPng(bitmap: Bitmap, size: Int): ByteArray {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)

        val scale = minOf(size.toFloat() / bitmap.width, size.toFloat() / bitmap.height)
        val scaledW = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val scaledH = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val left = (size - scaledW) / 2f
        val top = (size - scaledH) / 2f

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        canvas.drawBitmap(bitmap, null, android.graphics.RectF(left, top, left + scaledW, top + scaledH), paint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)
        output.recycle()

        return baos.toByteArray()
    }

    fun loadBitmap(iconPath: String): Bitmap? {
        return try {
            // Downstream re-encodes at mipmap scale; cap the decode (#779).
            if (iconPath.startsWith("/")) {
                com.webtoapp.util.BoundedBitmaps.decodeBoundedBitmapFile(iconPath, 1024)
            } else if (iconPath.startsWith("content://")) {
                context.contentResolver.openInputStream(android.net.Uri.parse(iconPath))?.use {
                    com.webtoapp.util.BoundedBitmaps.decodeBoundedBitmapStream(it, 1024)
                }
            } else {
                com.webtoapp.util.BoundedBitmaps.decodeBoundedBitmapFile(iconPath, 1024)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun createAdaptiveForegroundIcon(bitmap: Bitmap, size: Int): ByteArray {

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)

        val safeZoneSize = (size * 72f / 108f).toInt()
        val padding = (size - safeZoneSize) / 2f

        val scale = minOf(safeZoneSize.toFloat() / bitmap.width, safeZoneSize.toFloat() / bitmap.height)
        val scaledW = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val scaledH = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val left = padding + (safeZoneSize - scaledW) / 2f
        val top = padding + (safeZoneSize - scaledH) / 2f

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        canvas.drawBitmap(bitmap, null, android.graphics.RectF(left, top, left + scaledW, top + scaledH), paint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)

        output.recycle()

        return baos.toByteArray()
    }

    fun createRoundIcon(bitmap: Bitmap, size: Int): ByteArray {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = android.graphics.Canvas(output)

        val scale = minOf(size.toFloat() / bitmap.width, size.toFloat() / bitmap.height)
        val scaledW = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val scaledH = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val left = (size - scaledW) / 2f
        val top = (size - scaledH) / 2f

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        canvas.drawBitmap(bitmap, null, android.graphics.RectF(left, top, left + scaledW, top + scaledH), paint)

        // Mask with DST_IN over the full canvas: an SRC_IN composite against a pre-drawn
        // circle would leave the circle visible wherever the letterboxed content didn't cover.
        val maskPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN)
        }
        canvas.drawOval(android.graphics.RectF(0f, 0f, size.toFloat(), size.toFloat()), maskPaint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)

        output.recycle()

        return baos.toByteArray()
    }

    fun clearCache() {
        templateDir.listFiles()?.forEach { it.delete() }
    }
}
