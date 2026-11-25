package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.GalleryCategory
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaChoiceRow
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSelectableRow
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.theme.LocalShowDescriptions

@Composable
internal fun GallerySourceSetting(
    useDeviceFolder: Boolean,
    enabled: Boolean,
    onSourceChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    WtaSettingCard {
        WtaChoiceRow(
            title = Strings.source,
            value = if (useDeviceFolder) Strings.galleryDeviceFolder else Strings.galleryEmbeddedMedia,
            icon = Icons.Outlined.VideoLibrary,
            enabled = enabled,
            isExpanded = expanded,
            onClick = { expanded = !expanded }
        )
        AnimatedVisibility(visible = expanded, enter = CardExpandTransition, exit = CardCollapseTransition) {
            Column(
                modifier = Modifier.padding(horizontal = WtaSpacing.RowHorizontal, vertical = WtaSpacing.ContentGap),
                verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
            ) {
                WtaSelectableRow(
                    title = Strings.galleryEmbeddedMedia,
                    selected = !useDeviceFolder,
                    enabled = enabled,
                    onClick = { onSourceChange(false); expanded = false }
                )
                WtaSelectableRow(
                    title = Strings.galleryDeviceFolder,
                    selected = useDeviceFolder,
                    enabled = enabled,
                    onClick = { onSourceChange(true); expanded = false }
                )
            }
        }
        AnimatedVisibility(
            visible = useDeviceFolder && LocalShowDescriptions.current,
            enter = CardExpandTransition,
            exit = CardCollapseTransition
        ) {
            Column {
                WtaSectionDivider()
                Text(
                    Strings.galleryDeviceFolderHint,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = WtaSpacing.RowHorizontal, vertical = WtaSpacing.ContentGap)
                )
            }
        }
    }
}

@Composable
internal fun GalleryFolderStrip(
    folders: List<GalleryCategory>,
    selectedId: String?,
    enabled: Boolean,
    onSelect: (String?) -> Unit,
    onNewFolder: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = WtaSpacing.RowHorizontal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedId == null,
                    enabled = enabled,
                    onClick = { onSelect(null) },
                    label = { Text(Strings.all) }
                )
            }
            items(folders, key = { it.id }) { folder ->
                FilterChip(
                    selected = selectedId == folder.id,
                    enabled = enabled,
                    onClick = { onSelect(folder.id) },
                    label = { Text(folder.name) }
                )
            }
        }
        IconButton(onClick = onNewFolder, enabled = enabled) {
            Icon(Icons.Default.CreateNewFolder, Strings.galleryNewFolder)
        }
    }
}

@Composable
internal fun GalleryFolderNameDialog(
    folder: GalleryCategory?,
    folders: List<GalleryCategory>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember(folder) { mutableStateOf(folder?.name.orEmpty()) }
    val name = text.trim()
    val available = name.isNotEmpty() && folders.none { it.id != folder?.id && it.name.equals(name, true) }
    WtaAlertDialog(
        title = if (folder == null) Strings.galleryNewFolder else Strings.edit,
        icon = Icons.Default.Folder,
        onDismissRequest = onDismiss,
        dismissButton = { TextButton(onClick = onDismiss) { Text(Strings.cancel) } },
        confirmButton = {
            TextButton(onClick = { onSave(name) }, enabled = available) { Text(Strings.btnSave) }
        },
        content = {
            PremiumTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(Strings.galleryFolderName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
internal fun GalleryMoveDialog(
    folders: List<GalleryCategory>,
    onDismiss: () -> Unit,
    onMove: (String?) -> Unit,
    onNewFolder: () -> Unit
) {
    WtaAlertDialog(
        title = Strings.galleryMoveToFolder,
        icon = Icons.Default.Folder,
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(Strings.cancel) } },
        content = {
            LazyColumn(modifier = Modifier.heightIn(max = 320.dp).fillMaxWidth()) {
                item { WtaSelectableRow(Strings.galleryUnfiled, false, { onMove(null) }) }
                items(folders, key = { it.id }) { folder ->
                    WtaSelectableRow(folder.name, false, { onMove(folder.id) })
                }
            }
            TextButton(onClick = onNewFolder) {
                Icon(Icons.Default.Add, null)
                Text(Strings.galleryNewFolder)
            }
        }
    )
}
