package com.webtoapp.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import com.webtoapp.R
import com.webtoapp.data.model.AppType
import com.webtoapp.ui.design.WtaIconStroke
import com.webtoapp.ui.design.inDesignSpace
import com.webtoapp.ui.design.roundedPolygonPath
import com.webtoapp.ui.design.starPoints

@Composable
fun WtaAppTypeIcon(
    type: AppType,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    val lineArt = when (type) {
        AppType.WEB -> R.drawable.ic_type_web
        AppType.MULTI_WEB -> R.drawable.ic_type_multi_web
        AppType.FRONTEND -> R.drawable.ic_type_frontend
        AppType.NODEJS_APP -> R.drawable.ic_type_nodejs
        AppType.PHP_APP -> R.drawable.ic_type_php
        AppType.PYTHON_APP -> R.drawable.ic_type_python
        else -> null
    }
    if (lineArt != null) {
        Icon(
            painter = painterResource(lineArt),
            contentDescription = null,
            modifier = modifier,
            tint = color
        )
        return
    }
    Canvas(modifier = modifier) {
        inDesignSpace(48f) {
            when (type) {
                AppType.HTML -> drawHtmlType(color, accent)
                AppType.GALLERY -> drawGalleryType(color, accent)
                AppType.IMAGE, AppType.VIDEO -> drawMediaType(color, accent)
                AppType.GO_APP -> drawGoType(color, accent)
                AppType.WORDPRESS -> drawWordPressType(color, accent)
                else -> Unit
            }
        }
    }
}

@Composable
fun WtaOfflinePackTypeIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Icon(
        painter = painterResource(R.drawable.ic_type_offline_pack),
        contentDescription = null,
        modifier = modifier,
        tint = color
    )
}

private fun DrawScope.drawHtmlType(color: Color, accent: Color) {
    val left = Path().apply {
        moveTo(16f, 13f)
        lineTo(8f, 24f)
        lineTo(16f, 35f)
    }
    val right = Path().apply {
        moveTo(32f, 13f)
        lineTo(40f, 24f)
        lineTo(32f, 35f)
    }
    drawPath(left, color, style = WtaIconStroke)
    drawPath(right, color, style = WtaIconStroke)
    drawLine(accent, Offset(27f, 11f), Offset(21f, 37f), strokeWidth = 3f, cap = StrokeCap.Round)
    drawPath(roundedPolygonPath(starPoints(4, 4f, 1.2f, Offset(39f, 8f)), 0.3f), accent)
}

private fun DrawScope.drawGalleryType(color: Color, accent: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(7f, 10f),
        size = Size(34f, 28f),
        cornerRadius = CornerRadius(4f, 4f),
        style = WtaIconStroke
    )
    val mountains = Path().apply {
        moveTo(11f, 32f)
        lineTo(19f, 22f)
        lineTo(25f, 28f)
        lineTo(29f, 24f)
        lineTo(37f, 32f)
    }
    drawPath(mountains, accent, style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawCircle(accent, 3.5f, Offset(32f, 17f), style = Stroke(width = 2.2f))
    drawCircle(accent, 0.9f, Offset(30.8f, 16.4f))
    drawCircle(accent, 0.9f, Offset(33.2f, 16.4f))
}

private fun DrawScope.drawMediaType(color: Color, accent: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(7f, 7f),
        size = Size(34f, 34f),
        cornerRadius = CornerRadius(9f, 9f),
        style = WtaIconStroke
    )
    val play = Path().apply {
        moveTo(20f, 16f)
        lineTo(20f, 32f)
        lineTo(34f, 24f)
        close()
    }
    drawPath(play, color, style = WtaIconStroke)
    drawPath(roundedPolygonPath(starPoints(4, 3.5f, 1.1f, Offset(37.5f, 10.5f)), 0.3f), accent)
}

private fun DrawScope.drawGoType(color: Color, accent: Color) {
    val bolt = listOf(
        Offset(27f, 5f), Offset(13f, 26f), Offset(21f, 26f),
        Offset(19f, 43f), Offset(35f, 20f), Offset(26f, 20f)
    )
    drawPath(roundedPolygonPath(bolt, 0.15f), color, style = WtaIconStroke)
    drawLine(accent, Offset(5f, 15f), Offset(10f, 15f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(accent, Offset(3f, 23f), Offset(8f, 23f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(accent, Offset(5f, 31f), Offset(10f, 31f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawCircle(color, 1.3f, Offset(21.5f, 22f))
    drawCircle(color, 1.3f, Offset(26.5f, 20.5f))
}

private fun DrawScope.drawWordPressType(color: Color, accent: Color) {
    drawCircle(color, 16f, Offset(24f, 24f), style = WtaIconStroke)
    val w = Path().apply {
        moveTo(14f, 16f)
        lineTo(18.5f, 32f)
        lineTo(24f, 20f)
        lineTo(29.5f, 32f)
        lineTo(34f, 16f)
    }
    drawPath(w, accent, style = Stroke(width = 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(roundedPolygonPath(starPoints(4, 3.5f, 1.1f, Offset(39f, 9f)), 0.3f), accent)
}