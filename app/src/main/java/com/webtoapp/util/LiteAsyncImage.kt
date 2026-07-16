package com.webtoapp.util

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun LiteAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, model) {
        value = withContext(Dispatchers.IO) {
            decodeModel(context, model)
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

private fun decodeModel(context: android.content.Context, model: Any?): ImageBitmap? {
    if (model == null) return null
    return try {
        when (model) {
            is ImageBitmap -> model
            is android.graphics.Bitmap -> model.asImageBitmap()
            is Int -> BitmapFactory.decodeResource(context.resources, model)?.asImageBitmap()
            is ByteArray -> BitmapFactory.decodeByteArray(model, 0, model.size)?.asImageBitmap()
            is java.io.File -> BitmapFactory.decodeFile(model.absolutePath)?.asImageBitmap()
            is android.net.Uri -> {
                context.contentResolver.openInputStream(model)?.use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }
            is String -> decodeUrlOrPath(model)
            else -> decodeUrlOrPath(model.toString())
        }
    } catch (_: Exception) {
        null
    }
}

private fun decodeUrlOrPath(value: String): ImageBitmap? {
    if (value.isBlank() || value.startsWith("blob:")) return null
    if (value.startsWith("http://") || value.startsWith("https://")) {
        val conn = (URL(value).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
            instanceFollowRedirects = true
        }
        return try {
            conn.inputStream.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
        } finally {
            conn.disconnect()
        }
    }
    return BitmapFactory.decodeFile(value)?.asImageBitmap()
}
