package com.webtoapp.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

/**
 * Draw-side backstop against oversized-bitmap crashes (#779).
 *
 * Even a correctly bounded decode can be defeated later (shared caches, future
 * code paths), and RecordingCanvas kills the process when asked to draw beyond
 * its limit. This wrapper downscales any [ImageBitmap] whose side exceeds
 * [maxDimensionPx] before handing it to [Image], so a missed decode-site can
 * only ever render soft — never crash.
 */
@Composable
fun SafeBitmapImage(
    bitmap: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    maxDimensionPx: Int = 4096
) {
    val safe = remember(bitmap, maxDimensionPx) {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0 || (w <= maxDimensionPx && h <= maxDimensionPx)) {
            bitmap
        } else {
            runCatching {
                val longest = maxOf(w, h)
                val scale = maxDimensionPx / longest.toFloat()
                val tw = maxOf(1, (w * scale).toInt())
                val th = maxOf(1, (h * scale).toInt())
                val bmp = android.graphics.Bitmap.createBitmap(tw, th, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                canvas.drawBitmap(
                    bitmap.asAndroidBitmap(), null,
                    android.graphics.Rect(0, 0, tw, th), null
                )
                bmp.asImageBitmap()
            }.getOrNull() ?: bitmap
        }
    }
    Image(
        bitmap = safe,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}
