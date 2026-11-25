package com.webtoapp.core.host

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

internal fun Bitmap.fitInTransparentSquare(size: Int): Bitmap {
    if (width == size && height == size) return this
    val scale = size.toFloat() / maxOf(width, height)
    val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
    val scaledHeight = (height * scale).toInt().coerceAtLeast(1)
    val left = (size - scaledWidth) / 2f
    val top = (size - scaledHeight) / 2f
    val square = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    Canvas(square).drawBitmap(
        this,
        null,
        RectF(left, top, left + scaledWidth, top + scaledHeight),
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    )
    return square
}
