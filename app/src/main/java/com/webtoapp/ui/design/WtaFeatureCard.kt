package com.webtoapp.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun WtaFeatureCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(WtaSpacing.Large),
    content: @Composable ColumnScope.() -> Unit
) {
    WtaCard(
        modifier = modifier.fillMaxWidth(),
        tone = WtaCardTone.Surface,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun WtaFeatureCardHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val clickModifier = if (onClick != null) {
        Modifier
            .clip(RoundedCornerShape(WtaRadius.Card))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = rememberHapticClick(onClick)
            )
            .background(
                if (isPressed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                else Color.Transparent,
                shape = RoundedCornerShape(WtaRadius.Card)
            )
    } else Modifier

    Row(
        modifier = modifier.fillMaxWidth().then(clickModifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WtaIconTitle(
            icon = icon,
            title = title,
            subtitle = subtitle,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(WtaSpacing.Small))
        trailing()
    }
}

@Composable
fun WtaFeatureCardHeader(
    painter: Painter,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val clickModifier = if (onClick != null) {
        Modifier
            .clip(RoundedCornerShape(WtaRadius.Card))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = rememberHapticClick(onClick)
            )
            .background(
                if (isPressed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                else Color.Transparent,
                shape = RoundedCornerShape(WtaRadius.Card)
            )
    } else Modifier

    Row(
        modifier = modifier.fillMaxWidth().then(clickModifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WtaIconTitle(
            painter = painter,
            title = title,
            subtitle = subtitle,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(WtaSpacing.Small))
        trailing()
    }
}
