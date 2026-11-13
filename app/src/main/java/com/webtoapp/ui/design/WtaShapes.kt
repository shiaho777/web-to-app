package com.webtoapp.ui.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.graphics.shapes.Cubic
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.webtoapp.ui.theme.LocalAnimationSettings

private fun List<Cubic>.toShapePath(sizePx: Float): Path {
    val path = Path()
    if (isEmpty()) return path
    val first = first()
    path.moveTo(first.anchor0X * sizePx, first.anchor0Y * sizePx)
    for (cubic in this) {
        path.cubicTo(
            cubic.control0X * sizePx,
            cubic.control0Y * sizePx,
            cubic.control1X * sizePx,
            cubic.control1Y * sizePx,
            cubic.anchor1X * sizePx,
            cubic.anchor1Y * sizePx
        )
    }
    path.close()
    return path
}

@Composable
fun WtaMaterialShape(
    polygon: RoundedPolygon,
    color: Color,
    modifier: Modifier = Modifier,
    rotationDegrees: Float = 0f
) {
    val normalized = remember(polygon) { polygon.normalized() }
    Canvas(modifier = modifier) {
        val path = normalized.cubics.toShapePath(size.minDimension)
        rotate(degrees = rotationDegrees) {
            drawPath(path = path, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WtaMorphingShape(
    startPolygon: RoundedPolygon,
    endPolygon: RoundedPolygon,
    color: Color,
    modifier: Modifier = Modifier,
    morphDurationMillis: Int = 2600,
    rotationDurationMillis: Int = 26000
) {
    val animSettings = LocalAnimationSettings.current
    val motionEnabled = animSettings.enabled && WtaMotion.motionEnabled
    val start = remember(startPolygon) { startPolygon.normalized() }
    val end = remember(endPolygon) { endPolygon.normalized() }
    if (!motionEnabled) {
        WtaMaterialShape(polygon = start, color = color, modifier = modifier)
        return
    }
    val morph = remember(start, end) { Morph(start, end) }
    val transition = rememberInfiniteTransition(label = "wtaMorphingShape")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = morphDurationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wtaMorphProgress"
    )
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDurationMillis, easing = LinearEasing)
        ),
        label = "wtaMorphRotation"
    )
    Canvas(modifier = modifier) {
        val path = morph.asCubics(progress).toShapePath(size.minDimension)
        rotate(degrees = rotation) {
            drawPath(path = path, color = color)
        }
    }
}

@Composable
fun WtaShapePlate(
    polygon: RoundedPolygon,
    containerColor: Color,
    modifier: Modifier = Modifier,
    rotationDegrees: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        WtaMaterialShape(
            polygon = polygon,
            color = containerColor,
            modifier = Modifier.matchParentSize(),
            rotationDegrees = rotationDegrees
        )
        content()
    }
}