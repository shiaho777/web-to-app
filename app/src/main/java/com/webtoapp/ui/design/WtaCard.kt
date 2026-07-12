package com.webtoapp.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    val colors = CardDefaults.cardColors(
        containerColor = spec.container,
        contentColor = spec.content
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = spec.elevation)
    val resolvedBorder = border ?: spec.border

    if (resolvedBorder != null) {
        OutlinedCard(
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = resolvedBorder,
            elevation = elevation
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
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
    val hapticClick = rememberHapticClick(onClick)
    val colors = CardDefaults.cardColors(
        containerColor = spec.container,
        contentColor = spec.content
    )
    val elevation = CardDefaults.cardElevation(
        defaultElevation = spec.elevation,
        pressedElevation = spec.elevation,
        focusedElevation = spec.elevation,
        hoveredElevation = if (spec.elevation > 0.dp) spec.elevation + 1.dp else 1.dp
    )
    val resolvedBorder = border ?: spec.border

    if (resolvedBorder != null) {
        OutlinedCard(
            onClick = hapticClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colors,
            border = resolvedBorder,
            elevation = elevation
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(
            onClick = hapticClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colors,
            elevation = elevation
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

private data class WtaCardSpec(
    val container: Color,
    val content: Color,
    val border: BorderStroke?,
    val elevation: Dp
)

@Composable
private fun resolveTone(tone: WtaCardTone): WtaCardSpec {
    val colors = MaterialTheme.colorScheme
    return when (tone) {
        WtaCardTone.Surface -> WtaCardSpec(
            container = colors.surfaceContainerLow,
            content = colors.onSurface,
            border = null,
            elevation = WtaElevation.Level0
        )
        WtaCardTone.Elevated -> WtaCardSpec(
            container = colors.surfaceContainer,
            content = colors.onSurface,
            border = null,
            elevation = WtaElevation.Level1
        )
        WtaCardTone.Highlighted -> WtaCardSpec(
            container = colors.secondaryContainer,
            content = colors.onSecondaryContainer,
            border = null,
            elevation = WtaElevation.Level0
        )
        WtaCardTone.Critical -> WtaCardSpec(
            container = colors.errorContainer,
            content = colors.onErrorContainer,
            border = null,
            elevation = WtaElevation.Level0
        )
    }
}
