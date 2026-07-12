package com.webtoapp.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.webtoapp.ui.theme.LocalIsDarkTheme

@Composable
fun Modifier.wtaSoftShadow(
    shape: Shape,
    level: Dp = WtaElevation.Level2
): Modifier {
    val base = level.value.coerceIn(0f, 24f)
    if (base <= 0f) return this
    val (ambient, spot) = wtaShadowColors()
    return this.shadow(
        elevation = base.dp,
        shape = shape,
        ambientColor = ambient,
        spotColor = spot,
        clip = false
    )
}

@Composable
@ReadOnlyComposable
private fun wtaShadowColors(): Pair<Color, Color> {
    val isDark = LocalIsDarkTheme.current
    return if (isDark) {
        Color(0x66000000) to Color(0x88000000)
    } else {
        Color(0x14000000) to Color(0x1A000000)
    }
}
