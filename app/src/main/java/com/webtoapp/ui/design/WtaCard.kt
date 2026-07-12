package com.webtoapp.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.webtoapp.ui.theme.LocalIsDarkTheme

enum class WtaCardTone {

    Surface,

    Elevated,

    Highlighted,

    Critical
}

@Composable
fun WtaCard(
    modifier: Modifier = Modifier,
    tone: WtaCardTone = WtaCardTone.Surface,
    contentPadding: PaddingValues = PaddingValues(WtaSpacing.Large),
    shape: Shape = RoundedCornerShape(WtaRadius.Card),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val spec = resolveTone(tone)
    val shadowModifier = if (spec.elevation > 0.dp) {
        Modifier.wtaSoftShadow(shape, spec.elevation)
    } else Modifier

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .background(spec.container)
            .then(
                if (border != null || spec.border != null) {
                    Modifier.border(
                        border ?: spec.border!!,
                        shape
                    )
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun WtaCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: WtaCardTone = WtaCardTone.Surface,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(WtaSpacing.Large),
    shape: Shape = RoundedCornerShape(WtaRadius.Card),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val spec = resolveTone(tone)
    val interactionSource = remember { MutableInteractionSource() }
    val hapticClick = rememberHapticClick(onClick)
    val indication = rememberWtaIndication()
    val shadowModifier = if (spec.elevation > 0.dp) {
        Modifier.wtaSoftShadow(shape, spec.elevation)
    } else Modifier

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .background(spec.container)
            .then(
                if (border != null || spec.border != null) {
                    Modifier.border(
                        border ?: spec.border!!,
                        shape
                    )
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = indication,
                enabled = enabled,
                onClick = hapticClick
            )
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

private data class WtaCardSpec(
    val container: Color,
    val border: BorderStroke?,
    val elevation: Dp
)

@Composable
private fun resolveTone(tone: WtaCardTone): WtaCardSpec {
    val colors = MaterialTheme.colorScheme
    val isDark = LocalIsDarkTheme.current
    return when (tone) {
        WtaCardTone.Surface -> WtaCardSpec(
            container = colors.surface,
            border = BorderStroke(
                width = 0.5.dp,
                color = if (isDark) Color.White.copy(alpha = 0.09f)
                else Color.Black.copy(alpha = 0.08f)
            ),
            elevation = WtaElevation.Level0
        )
        WtaCardTone.Elevated -> WtaCardSpec(
            container = colors.surfaceContainer,
            border = null,
            elevation = WtaElevation.Level1
        )
        WtaCardTone.Highlighted -> WtaCardSpec(
            container = colors.primaryContainer,
            border = null,
            elevation = WtaElevation.Level0
        )
        WtaCardTone.Critical -> WtaCardSpec(
            container = colors.errorContainer,
            border = BorderStroke(
                width = 0.5.dp,
                color = colors.error.copy(alpha = WtaAlpha.Subtle)
            ),
            elevation = WtaElevation.Level0
        )
    }
}
