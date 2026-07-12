package com.webtoapp.ui.design

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object WtaDefaults {

    @Composable
    fun outlinedTextFieldColors(): TextFieldColors {
        val colors = MaterialTheme.colorScheme
        return OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outline.copy(alpha = 0.35f),
            disabledBorderColor = colors.outline.copy(alpha = 0.15f),
            errorBorderColor = colors.error,

            cursorColor = colors.primary,
            errorCursorColor = colors.error,

            focusedLabelColor = colors.onSurface,
            unfocusedLabelColor = colors.onSurfaceVariant,
            disabledLabelColor = colors.onSurface.copy(alpha = WtaAlpha.Disabled),
            errorLabelColor = colors.error,

            focusedLeadingIconColor = colors.onSurface,
            unfocusedLeadingIconColor = colors.onSurfaceVariant,
            disabledLeadingIconColor = colors.onSurfaceVariant.copy(alpha = WtaAlpha.Disabled),

            focusedTrailingIconColor = colors.onSurface,
            unfocusedTrailingIconColor = colors.onSurfaceVariant,
            disabledTrailingIconColor = colors.onSurfaceVariant.copy(alpha = WtaAlpha.Disabled),

            focusedSupportingTextColor = colors.onSurfaceVariant,
            unfocusedSupportingTextColor = colors.onSurfaceVariant,
            errorSupportingTextColor = colors.error,

            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent
        )
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun filledTextFieldColors(): TextFieldColors {
        val colors = MaterialTheme.colorScheme
        return TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceContainerHighest,
            unfocusedContainerColor = colors.surfaceContainerHighest,
            disabledContainerColor = colors.surfaceContainerHighest.copy(alpha = 0.5f),
            errorContainerColor = colors.errorContainer,

            focusedIndicatorColor = colors.primary,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = colors.error,

            cursorColor = colors.primary,
            errorCursorColor = colors.error,

            focusedLabelColor = colors.onSurface,
            unfocusedLabelColor = colors.onSurfaceVariant,
            disabledLabelColor = colors.onSurface.copy(alpha = WtaAlpha.Disabled),
            errorLabelColor = colors.error,

            focusedLeadingIconColor = colors.onSurface,
            unfocusedLeadingIconColor = colors.onSurfaceVariant,
            focusedTrailingIconColor = colors.onSurface,
            unfocusedTrailingIconColor = colors.onSurfaceVariant
        )
    }
}
