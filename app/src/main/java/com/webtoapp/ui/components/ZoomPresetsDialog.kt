package com.webtoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaChip

/** Page-zoom preset percentages offered in the overflow menu. Mirrors Chrome's stops. */
val PAGE_ZOOM_PRESETS: List<Int> = listOf(50, 67, 75, 80, 90, 100, 110, 125, 150)

/**
 * A dialog that lets the user pick a page-zoom preset. Selecting a value calls [onSelect]
 * with the percent; 100% is treated as "reset / no override".
 *
 * @param currentZoom the currently applied zoom percent (0 means "no override → 100%").
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ZoomPresetsDialog(
    currentZoom: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val selected = if (currentZoom > 0) currentZoom else 100
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.ZoomIn, contentDescription = null) },
        title = { Text(Strings.pageZoomLabel) },
        text = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PAGE_ZOOM_PRESETS.forEach { preset ->
                    val isSelected = preset == selected
                    WtaChip(
                        selected = isSelected,
                        onClick = { onSelect(preset); onDismiss() },
                        label = "$preset%",
                        showSelectedCheck = false
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(0); onDismiss() }) {
                Text(Strings.pageZoomReset)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Strings.cancel) }
        }
    )
}
