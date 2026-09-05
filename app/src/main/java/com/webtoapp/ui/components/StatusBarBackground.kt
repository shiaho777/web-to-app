package com.webtoapp.ui.components

import android.content.Context
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.ui.shared.SafeBitmapImage
import com.webtoapp.util.BoundedBitmaps
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Loads a status-bar background image bounded to a drawable-safe size (#779).
 * The file comes from user storage and can be arbitrarily large (panoramas,
 * full-resolution crops); decoding it raw once crashed MainActivity screens
 * with "Canvas: trying to draw too large" at DRAW time — past any decode
 * try/catch. Returns null when the file is missing, oversized, or unreadable.
 */
private fun loadStatusBarImageBitmap(context: Context, backgroundImagePath: String): android.graphics.Bitmap? {
    return try {
        val file = File(backgroundImagePath)
        if (file.exists()) {
            if (!BoundedBitmaps.isProbablyImageFile(file)) return null
            BoundedBitmaps.decodeBoundedBitmapFile(backgroundImagePath)
        } else {
            val assetPath = backgroundImagePath.removePrefix("assets/")
            context.assets.open(assetPath).use { inputStream ->
                BoundedBitmaps.decodeBoundedBitmapStream(inputStream)
            }
        }
    } catch (e: Exception) {
        AppLogger.e("StatusBarBackground", "加载状态栏背景图片失败: $backgroundImagePath", e)
        null
    }
}

@Composable
fun StatusBarBackground(
    backgroundType: String,
    backgroundColor: String?,
    backgroundImagePath: String?,
    alpha: Float,
    heightDp: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Never guess a status-bar height when the insets report 0: on the classic pre-API-30
    // path the value is 0 because the decor fits system windows, and a fabricated 24dp band
    // shows up as a blank strip over the content (issue #683). The callers skip rendering on
    // that path; the fallback below only covers transient first-frame inset dispatch.
    val topInsetPx = WindowInsets.statusBars.getTop(density)
    val systemStatusBarHeight = if (topInsetPx > 0) {
        with(density) { topInsetPx.toDp() }
    } else {
        0.dp
    }

    val actualHeight = if (heightDp >= 0) heightDp.dp else systemStatusBarHeight
    if (actualHeight <= 0.dp) return

    val imageBitmap = remember(backgroundImagePath) {
        if (backgroundType == "IMAGE" && !backgroundImagePath.isNullOrEmpty()) {
            loadStatusBarImageBitmap(context, backgroundImagePath)?.asImageBitmap()
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(actualHeight)
    ) {
        when {
            backgroundType == "IMAGE" && imageBitmap != null -> {

                SafeBitmapImage(
                    bitmap = imageBitmap,
                    contentDescription = Strings.statusBarBackground,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 1f - alpha)),
                    contentScale = ContentScale.Crop,
                    alpha = alpha
                )
            }
            else -> {

                // A null color means TRANSPARENT mode (the only resolver path that
                // yields null). It must stay see-through: falling back to black
                // painted an opaque band over the content (issue #762).
                val bgColor = if (backgroundColor == null) {
                    Color.Transparent
                } else try {
                    val hex = backgroundColor.removePrefix("#")
                    when (hex.length) {
                        6 -> Color(android.graphics.Color.parseColor("#$hex")).copy(alpha = alpha)
                        8 -> Color(android.graphics.Color.parseColor("#$hex")).copy(alpha = alpha)
                        else -> Color.Black.copy(alpha = alpha)
                    }
                } catch (e: Exception) {
                    Color.Black.copy(alpha = alpha)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor)
                )
            }
        }
    }
}

@Composable
fun StatusBarOverlay(
    show: Boolean,
    backgroundType: String,
    backgroundColor: String?,
    backgroundImagePath: String?,
    alpha: Float,
    heightDp: Int,
    modifier: Modifier = Modifier
) {
    if (!show) return

    val context = LocalContext.current
    val density = LocalDensity.current

    // Never guess a status-bar height when the insets report 0: on the classic pre-API-30
    // path the value is 0 because the decor fits system windows, and a fabricated 24dp band
    // shows up as a blank strip over the content (issue #683). The callers skip rendering on
    // that path; the fallback below only covers transient first-frame inset dispatch.
    val topInsetPx = WindowInsets.statusBars.getTop(density)
    val systemStatusBarHeight = if (topInsetPx > 0) {
        with(density) { topInsetPx.toDp() }
    } else {
        0.dp
    }

    val actualHeight = if (heightDp >= 0) heightDp.dp else systemStatusBarHeight
    if (actualHeight <= 0.dp) return

    val imageBitmap = remember(backgroundImagePath) {
        if (backgroundType == "IMAGE" && !backgroundImagePath.isNullOrEmpty()) {
            loadStatusBarImageBitmap(context, backgroundImagePath)?.asImageBitmap()
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(actualHeight)
    ) {
        when {
            backgroundType == "IMAGE" && imageBitmap != null -> {

                SafeBitmapImage(
                    bitmap = imageBitmap,
                    contentDescription = Strings.statusBarBackground,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = alpha
                )
            }
            else -> {

                // Same TRANSPARENT rule as StatusBarBackground above: null stays
                // see-through instead of degrading to a black band (issue #762).
                val bgColor = if (backgroundColor == null) {
                    Color.Transparent
                } else try {
                    val hex = backgroundColor.removePrefix("#")
                    when (hex.length) {
                        6 -> Color(android.graphics.Color.parseColor("#$hex")).copy(alpha = alpha)
                        8 -> Color(android.graphics.Color.parseColor("#$hex")).copy(alpha = alpha)
                        else -> Color.Black.copy(alpha = alpha)
                    }
                } catch (e: Exception) {
                    Color.Black.copy(alpha = alpha)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor)
                )
            }
        }
    }
}
