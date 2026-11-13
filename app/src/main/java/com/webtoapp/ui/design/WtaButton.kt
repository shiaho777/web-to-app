package com.webtoapp.ui.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun WtaButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    variant: WtaButtonVariant = WtaButtonVariant.Primary,
    size: WtaButtonSize = WtaButtonSize.Medium,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues? = null,
    shape: Shape? = null
) {
    WtaButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        enabled = enabled,
        contentPadding = contentPadding,
        shape = shape
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(WtaSize.IconSmall))
            Spacer(modifier = Modifier.width(WtaSpacing.Small))
        }
        Text(text = text)
        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(WtaSpacing.Small))
            Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(WtaSize.IconSmall))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WtaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: WtaButtonVariant = WtaButtonVariant.Primary,
    size: WtaButtonSize = WtaButtonSize.Medium,
    enabled: Boolean = true,
    contentPadding: PaddingValues? = null,
    shape: Shape? = null,
    content: @Composable RowScope.() -> Unit
) {
    val hapticClick = rememberHapticClick(onClick)
    val heightMin = when (size) {
        WtaButtonSize.Small -> WtaSize.ButtonHeightSmall
        WtaButtonSize.Medium -> WtaSize.ButtonHeightMedium
        WtaButtonSize.Large -> WtaSize.ButtonHeightLarge
    }
    val padding = contentPadding ?: when (size) {
        WtaButtonSize.Small -> PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        WtaButtonSize.Medium -> PaddingValues(horizontal = 24.dp, vertical = 10.dp)
        WtaButtonSize.Large -> PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    }
    val btnModifier = modifier.heightIn(min = heightMin)
    val colors = MaterialTheme.colorScheme
    val buttonShapes = if (shape != null) ButtonShapes(shape, shape) else ButtonDefaults.shapes()

    when (variant) {
        WtaButtonVariant.Primary -> Button(
            onClick = hapticClick,
            shapes = buttonShapes,
            modifier = btnModifier,
            enabled = enabled,
            contentPadding = padding,
            content = content
        )
        WtaButtonVariant.Tonal -> FilledTonalButton(
            onClick = hapticClick,
            shapes = buttonShapes,
            modifier = btnModifier,
            enabled = enabled,
            contentPadding = padding,
            content = content
        )
        WtaButtonVariant.Outlined -> OutlinedButton(
            onClick = hapticClick,
            shapes = buttonShapes,
            modifier = btnModifier,
            enabled = enabled,
            contentPadding = padding,
            content = content
        )
        WtaButtonVariant.Text -> TextButton(
            onClick = hapticClick,
            shapes = buttonShapes,
            modifier = btnModifier,
            enabled = enabled,
            contentPadding = padding,
            content = content
        )
        WtaButtonVariant.Destructive -> Button(
            onClick = hapticClick,
            shapes = buttonShapes,
            modifier = btnModifier,
            enabled = enabled,
            contentPadding = padding,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.error,
                contentColor = colors.onError,
                disabledContainerColor = colors.onSurface.copy(alpha = 0.12f),
                disabledContentColor = colors.onSurface.copy(alpha = WtaAlpha.Disabled)
            ),
            content = content
        )
    }
}

@Composable
fun WtaIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tonal: Boolean = false,
    colors: IconButtonColors = if (tonal) IconButtonDefaults.filledTonalIconButtonColors()
    else IconButtonDefaults.iconButtonColors()
) {
    val hapticClick = rememberHapticClick(onClick)
    IconButton(
        onClick = hapticClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(WtaSize.Icon))
    }
}
