package com.webtoapp.ui.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.webtoapp.ui.theme.LocalAnimationSettings
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WtaIdleFloat(
    modifier: Modifier = Modifier,
    amplitudeDp: Dp = 4.dp,
    tiltDegrees: Float = 2f,
    content: @Composable () -> Unit
) {
    val animSettings = LocalAnimationSettings.current
    if (!animSettings.enabled || !WtaMotion.motionEnabled) {
        Box(modifier = modifier) { content() }
        return
    }
    val transition = rememberInfiniteTransition(label = "wtaIdleFloat")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (2600 * WtaMotion.durationScale).toInt().coerceAtLeast(1),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wtaIdleFloatProgress"
    )
    val amplitudePx = with(LocalDensity.current) { amplitudeDp.toPx() }
    Box(
        modifier = modifier.graphicsLayer {
            translationY = progress * amplitudePx
            rotationZ = progress * tiltDegrees
        }
    ) { content() }
}

@Composable
fun WtaKawaiiCloud(
    modifier: Modifier = Modifier,
    bodyColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    faceColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    blushColor: Color = MaterialTheme.colorScheme.tertiary,
    sleeping: Boolean = false
) {
    Canvas(modifier = modifier) {
        inDesignSpace(100f) {
            drawCircle(bodyColor, 20f, Offset(30f, 52f))
            drawCircle(bodyColor, 26f, Offset(52f, 40f))
            drawCircle(bodyColor, 17f, Offset(74f, 54f))
            drawRoundRect(
                color = bodyColor,
                topLeft = Offset(14f, 50f),
                size = Size(74f, 26f),
                cornerRadius = CornerRadius(13f, 13f)
            )
            drawKawaiiFace(
                center = Offset(50f, 60f),
                spread = 11f,
                eyeRadius = 2.6f,
                faceColor = faceColor,
                blushColor = blushColor,
                sleeping = sleeping
            )
            if (sleeping) {
                val bubble = faceColor.copy(alpha = 0.45f)
                drawCircle(bubble, 2.4f, Offset(80f, 30f))
                drawCircle(bubble, 3.4f, Offset(88f, 19f))
            }
        }
    }
}

@Composable
fun WtaKawaiiStar(
    modifier: Modifier = Modifier,
    bodyColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    faceColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    blushColor: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        inDesignSpace(100f) {
            drawPath(roundedPolygonPath(starPoints(5, 46f, 21f, Offset(50f, 52f)), 0.22f), bodyColor)
            drawKawaiiFace(
                center = Offset(50f, 54f),
                spread = 8f,
                eyeRadius = 2.2f,
                faceColor = faceColor,
                blushColor = blushColor,
                sleeping = false
            )
        }
    }
}

@Composable
fun WtaKawaiiSparkle(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary
) {
    Canvas(modifier = modifier) {
        inDesignSpace(100f) {
            drawPath(roundedPolygonPath(starPoints(4, 48f, 13f, Offset(50f, 50f)), 0.3f), color)
        }
    }
}

@Composable
fun WtaKawaiiBox(
    modifier: Modifier = Modifier,
    bodyColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    faceColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    blushColor: Color = MaterialTheme.colorScheme.tertiary
) {
    Canvas(modifier = modifier) {
        inDesignSpace(100f) {
            val flap = bodyColor.copy(alpha = 0.72f)
            drawPath(
                roundedPolygonPath(
                    listOf(Offset(26f, 46f), Offset(12f, 32f), Offset(36f, 36f), Offset(40f, 46f)),
                    0.25f
                ),
                flap
            )
            drawPath(
                roundedPolygonPath(
                    listOf(Offset(74f, 46f), Offset(88f, 32f), Offset(64f, 36f), Offset(60f, 46f)),
                    0.25f
                ),
                flap
            )
            drawRoundRect(
                color = bodyColor,
                topLeft = Offset(24f, 44f),
                size = Size(52f, 36f),
                cornerRadius = CornerRadius(7f, 7f)
            )
            drawOval(
                color = faceColor.copy(alpha = 0.18f),
                topLeft = Offset(30f, 44f),
                size = Size(40f, 8f)
            )
            drawKawaiiFace(
                center = Offset(50f, 64f),
                spread = 10f,
                eyeRadius = 2.4f,
                faceColor = faceColor,
                blushColor = blushColor,
                sleeping = false
            )
        }
    }
}

internal inline fun DrawScope.inDesignSpace(designSize: Float, block: DrawScope.() -> Unit) {
    val scale = size.minDimension / designSize
    translate(
        left = (size.width - designSize * scale) / 2f,
        top = (size.height - designSize * scale) / 2f
    ) {
        scale(scale, scale, pivot = Offset.Zero) {
            block()
        }
    }
}

internal fun DrawScope.drawKawaiiFace(
    center: Offset,
    spread: Float,
    eyeRadius: Float,
    faceColor: Color,
    blushColor: Color,
    sleeping: Boolean
) {
    val eyeY = center.y - 2f
    val eyeStroke = Stroke(width = eyeRadius * 0.8f, cap = StrokeCap.Round)
    if (sleeping) {
        listOf(center.x - spread, center.x + spread).forEach { x ->
            drawArc(
                color = faceColor,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(x - eyeRadius * 1.6f, eyeY - eyeRadius * 1.6f),
                size = Size(eyeRadius * 3.2f, eyeRadius * 3.2f),
                style = eyeStroke
            )
        }
    } else {
        drawCircle(faceColor, eyeRadius, Offset(center.x - spread, eyeY))
        drawCircle(faceColor, eyeRadius, Offset(center.x + spread, eyeY))
    }
    drawArc(
        color = faceColor,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(center.x - 5f, center.y - 1f),
        size = Size(10f, 8f),
        style = Stroke(width = eyeRadius * 0.7f, cap = StrokeCap.Round)
    )
    val blush = blushColor.copy(alpha = 0.45f)
    drawOval(blush, Offset(center.x - spread - 7f, center.y + 1f), Size(6.5f, 3.6f))
    drawOval(blush, Offset(center.x + spread + 0.5f, center.y + 1f), Size(6.5f, 3.6f))
}

internal fun starPoints(
    points: Int,
    outerRadius: Float,
    innerRadius: Float,
    center: Offset
): List<Offset> {
    val result = mutableListOf<Offset>()
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = -PI / 2 + i * PI / points
        result += Offset(
            center.x + (radius * cos(angle)).toFloat(),
            center.y + (radius * sin(angle)).toFloat()
        )
    }
    return result
}

internal fun roundedPolygonPath(points: List<Offset>, cornerFraction: Float): Path {
    val path = Path()
    if (points.size < 3) return path
    val entries = points.mapIndexed { i, p ->
        val prev = points[(i - 1 + points.size) % points.size]
        val next = points[(i + 1) % points.size]
        Triple(
            Offset(p.x + (prev.x - p.x) * cornerFraction, p.y + (prev.y - p.y) * cornerFraction),
            p,
            Offset(p.x + (next.x - p.x) * cornerFraction, p.y + (next.y - p.y) * cornerFraction)
        )
    }
    path.moveTo(entries[0].first.x, entries[0].first.y)
    entries.forEach { (entry, corner, exit) ->
        path.lineTo(entry.x, entry.y)
        path.quadraticTo(corner.x, corner.y, exit.x, exit.y)
    }
    path.close()
    return path
}

internal val WtaIconStroke: Stroke = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)