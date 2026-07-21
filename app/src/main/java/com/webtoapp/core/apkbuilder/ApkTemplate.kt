package com.webtoapp.core.apkbuilder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.webtoapp.core.forcedrun.ForcedRunConfig
import com.webtoapp.core.logging.AppLogger
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
        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.PNG, 100, baos)
        if (scaled != bitmap) {
            scaled.recycle()
        }
        return baos.toByteArray()
    }

    fun loadBitmap(iconPath: String): Bitmap? {
        return try {
            // Use BitmapFactory.Options to control sampling and get optimal resolution
            val options = BitmapFactory.Options().apply {
                inSampleSize = 1
                inMutable = false
                inDither = false
            }
            
            val decodedBitmap = if (iconPath.startsWith("/")) {
                BitmapFactory.decodeFile(iconPath, options)
            } else if (iconPath.startsWith("content://")) {
                context.contentResolver.openInputStream(android.net.Uri.parse(iconPath))?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream, null, options)
                }
            } else {
                BitmapFactory.decodeFile(iconPath, options)
            }
            
            decodedBitmap ?: return null
            
            // Ensure high quality ARGB_8888 format and recycle if needed
            if (decodedBitmap.config != Bitmap.Config.ARGB_8888) {
                val argbBitmap = decodedBitmap.copy(Bitmap.Config.ARGB_8888, false)
                decodedBitmap.recycle()
                argbBitmap
            } else {
                decodedBitmap
            }
        } catch (e: Exception) {
            AppLogger.e("ApkTemplate", "Failed to load icon from $iconPath", e)
            null
        }
    }

    fun createAdaptiveForegroundIcon(bitmap: Bitmap, size: Int): ByteArray {

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)

        // Calculate the exact center padding for proper icon display
        // Android adaptive icons use a 72% safe zone with 14% margin on each side
        val safeZoneSize = (size * 0.72f).toInt()
        val padding = (size - safeZoneSize) / 2

        // Use high-quality scaling with filtering enabled
        val scaled = Bitmap.createScaledBitmap(bitmap, safeZoneSize, safeZoneSize, true)

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        canvas.drawBitmap(scaled, padding.toFloat(), padding.toFloat(), paint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)

        if (scaled != bitmap) scaled.recycle()
        output.recycle()

        return baos.toByteArray()
    }

    fun createRoundIcon(bitmap: Bitmap, size: Int): ByteArray {
        // Scale with high quality
        val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        val rect = android.graphics.RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)

        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaled, 0f, 0f, paint)

        val baos = ByteArrayOutputStream()
        output.compress(Bitmap.CompressFormat.PNG, 100, baos)

        if (scaled != bitmap) scaled.recycle()
        output.recycle()

        return baos.toByteArray()
    }

    fun clearCache() {
        templateDir.listFiles()?.forEach { it.delete() }
    }
}
