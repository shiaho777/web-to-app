package com.webtoapp.ui.icons

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.webtoapp.data.model.AppType
import com.webtoapp.ui.design.WtaIconStroke
import com.webtoapp.ui.design.drawKawaiiFace
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
    Canvas(modifier = modifier) {
        inDesignSpace(48f) {
            when (type) {
                AppType.WEB -> drawWebType(color, accent)
                AppType.MULTI_WEB -> drawMultiWebType(color, accent)
                AppType.HTML -> drawHtmlType(color, accent)
                AppType.FRONTEND -> drawFrontendType(color, accent)
                AppType.GALLERY -> drawGalleryType(color, accent)
                AppType.IMAGE, AppType.VIDEO -> drawMediaType(color, accent)
                AppType.NODEJS_APP -> drawNodeJsType(color, accent)
                AppType.PHP_APP -> drawPhpType(color, accent)
                AppType.PYTHON_APP -> drawPythonType(color, accent)
                AppType.GO_APP -> drawGoType(color, accent)
                AppType.WORDPRESS -> drawWordPressType(color, accent)
            }
        }
    }
}

@Composable
fun WtaOfflinePackTypeIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        inDesignSpace(48f) {
            drawOfflinePackType(color, accent)
        }
    }
}

private fun DrawScope.drawWebType(color: Color, accent: Color) {
    drawCircle(color, 16f, Offset(24f, 24f), style = WtaIconStroke)
    drawOval(
        color = color,
        topLeft = Offset(17f, 8f),
        size = Size(14f, 32f),
        style = Stroke(width = 2.2f, cap = StrokeCap.Round)
    )
    drawKawaiiFace(Offset(24f, 24f), 5.5f, 1.7f, color, accent, sleeping = false)
    drawPath(roundedPolygonPath(starPoints(4, 4.5f, 1.4f, Offset(40f, 8f)), 0.3f), accent)
}

private fun DrawScope.drawMultiWebType(color: Color, accent: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(7f, 7f),
        size = Size(17f, 13f),
        cornerRadius = CornerRadius(3f, 3f),
        style = WtaIconStroke
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(24f, 7f),
        size = Size(17f, 13f),
        cornerRadius = CornerRadius(3f, 3f),
        style = WtaIconStroke
    )
    drawLine(accent, Offset(10.5f, 11.5f), Offset(17f, 11.5f), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawLine(accent, Offset(27.5f, 11.5f), Offset(34f, 11.5f), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawRoundRect(
        color = color,
        topLeft = Offset(14f, 22f),
        size = Size(20f, 17f),
        cornerRadius = CornerRadius(3.5f, 3.5f),
        style = WtaIconStroke
    )
    drawKawaiiFace(Offset(24f, 30f), 5f, 1.5f, color, accent, sleeping = false)
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

private fun DrawScope.drawOfflinePackType(color: Color, accent: Color) {
    val cloud = Path().apply {
        moveTo(10f, 27f)
        cubicTo(6f, 27f, 4f, 24f, 4f, 21f)
        cubicTo(4f, 18f, 6f, 15.5f, 9f, 15f)
        cubicTo(9.5f, 10.5f, 13f, 7.5f, 17.5f, 7.5f)
        cubicTo(21f, 7.5f, 24f, 9.5f, 25.5f, 12.5f)
        cubicTo(27f, 11f, 29f, 10f, 31.5f, 10f)
        cubicTo(36f, 10f, 39.5f, 13.5f, 39.5f, 18f)
        cubicTo(42f, 18.5f, 44f, 21f, 44f, 23.5f)
        cubicTo(44f, 25.5f, 42.5f, 27f, 40f, 27f)
        close()
    }
    drawPath(cloud, color, style = WtaIconStroke)
    drawKawaiiFace(Offset(24f, 19f), 5.5f, 1.5f, color, accent, sleeping = true)
    val zzz = Path().apply {
        moveTo(36.5f, 3.5f)
        lineTo(41f, 3.5f)
        lineTo(36.5f, 7.5f)
        lineTo(41f, 7.5f)
    }
    drawPath(zzz, accent, style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawLine(color, Offset(24f, 31f), Offset(24f, 42f), strokeWidth = 3f, cap = StrokeCap.Round)
    val tip = Path().apply {
        moveTo(19f, 37f)
        lineTo(24f, 42f)
        lineTo(29f, 37f)
    }
    drawPath(tip, color, style = WtaIconStroke)
}

private fun DrawScope.drawFrontendType(color: Color, accent: Color) {
    val body = Path().apply {
        moveTo(24f, 5f)
        cubicTo(28f, 9f, 30f, 14f, 30f, 20f)
        lineTo(30f, 29f)
        lineTo(18f, 29f)
        lineTo(18f, 20f)
        cubicTo(18f, 14f, 20f, 9f, 24f, 5f)
        close()
    }
    drawPath(body, color, style = WtaIconStroke)
    drawCircle(accent, 3.2f, Offset(24f, 16.5f), style = Stroke(width = 2.4f))
    drawKawaiiFace(Offset(24f, 24f), 3.8f, 1.3f, color, accent, sleeping = false)
    val leftFin = Path().apply {
        moveTo(18f, 24f)
        lineTo(12f, 32f)
        lineTo(18f, 30f)
    }
    val rightFin = Path().apply {
        moveTo(30f, 24f)
        lineTo(36f, 32f)
        lineTo(30f, 30f)
    }
    drawPath(leftFin, color, style = WtaIconStroke)
    drawPath(rightFin, color, style = WtaIconStroke)
    val flame = Path().apply {
        moveTo(21f, 31f)
        cubicTo(21f, 35f, 22.5f, 38f, 24f, 40f)
        cubicTo(25.5f, 38f, 27f, 35f, 27f, 31f)
    }
    drawPath(flame, accent, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
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

private fun DrawScope.drawNodeJsType(color: Color, accent: Color) {
    val hexagon = listOf(
        Offset(24f, 8f), Offset(37.9f, 16f), Offset(37.9f, 32f),
        Offset(24f, 40f), Offset(10.1f, 32f), Offset(10.1f, 16f)
    )
    drawPath(roundedPolygonPath(hexagon, 0.18f), color, style = WtaIconStroke)
    drawLine(accent, Offset(24f, 30f), Offset(24f, 20f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    val leftLeaf = Path().apply {
        moveTo(24f, 21f)
        quadraticTo(17f, 20f, 16f, 13f)
        quadraticTo(23f, 14f, 24f, 21f)
        close()
    }
    val rightLeaf = Path().apply {
        moveTo(24f, 21f)
        quadraticTo(31f, 20f, 32f, 13f)
        quadraticTo(25f, 14f, 24f, 21f)
        close()
    }
    drawPath(leftLeaf, accent, style = Stroke(width = 2.2f, join = StrokeJoin.Round))
    drawPath(rightLeaf, accent, style = Stroke(width = 2.2f, join = StrokeJoin.Round))
    drawKawaiiFace(Offset(24f, 33.5f), 4.5f, 1.3f, color, accent, sleeping = false)
}

private fun DrawScope.drawPhpType(color: Color, accent: Color) {
    drawCircle(color, 5.5f, Offset(8f, 19f), style = WtaIconStroke)
    drawCircle(color, 5.5f, Offset(40f, 19f), style = WtaIconStroke)
    drawCircle(color, 12f, Offset(24f, 23f), style = WtaIconStroke)
    val trunk = Path().apply {
        moveTo(24f, 27f)
        cubicTo(24f, 33f, 24f, 36f, 27f, 38f)
        cubicTo(29f, 39.5f, 31f, 38.5f, 31f, 36.5f)
    }
    drawPath(trunk, color, style = WtaIconStroke)
    drawKawaiiFace(Offset(24f, 21f), 4.5f, 1.5f, color, accent, sleeping = false)
}

private fun DrawScope.drawPythonType(color: Color, accent: Color) {
    val snakeStroke = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val top = Path().apply {
        moveTo(15f, 19f)
        lineTo(15f, 13f)
        cubicTo(15f, 9.5f, 17.5f, 7f, 21f, 7f)
        lineTo(27f, 7f)
        cubicTo(30.5f, 7f, 33f, 9.5f, 33f, 13f)
        lineTo(33f, 21f)
        cubicTo(33f, 24f, 31f, 26f, 28f, 26f)
        lineTo(20f, 26f)
    }
    val bottom = Path().apply {
        moveTo(33f, 29f)
        lineTo(33f, 35f)
        cubicTo(33f, 38.5f, 30.5f, 41f, 27f, 41f)
        lineTo(21f, 41f)
        cubicTo(17.5f, 41f, 15f, 38.5f, 15f, 35f)
        lineTo(15f, 27f)
        cubicTo(15f, 24f, 17f, 22f, 20f, 22f)
        lineTo(28f, 22f)
    }
    drawPath(top, color, style = snakeStroke)
    drawPath(bottom, accent, style = snakeStroke)
    drawCircle(color, 1.5f, Offset(20.5f, 12.5f))
    drawCircle(accent, 1.5f, Offset(27.5f, 35.5f))
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