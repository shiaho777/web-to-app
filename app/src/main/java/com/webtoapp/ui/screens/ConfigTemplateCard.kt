package com.webtoapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.data.repository.ConfigTemplateStore
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.design.*

/**
 * Save / apply named snapshots of the common config (WebViewConfig). Applying replaces
 * the whole WebViewConfig — a template is a full snapshot, so the result is exactly
 * what was saved, never a mix of old and new values.
 */
@Composable
fun ConfigTemplateCard(
    config: WebViewConfig,
    onApplyConfig: (WebViewConfig) -> Unit
) {
    val context = LocalContext.current
    var templates by remember { mutableStateOf(ConfigTemplateStore.list(context)) }
    var refresh by remember { mutableIntStateOf(0) }
    LaunchedEffect(refresh) {
        if (refresh > 0) templates = ConfigTemplateStore.list(context)
    }

    var saveDialogOpen by remember { mutableStateOf(false) }
    var manageOpen by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<ConfigTemplateStore.ConfigTemplate?>(null) }
    var renameText by remember { mutableStateOf("") }
    var toast by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toast) {
        toast?.let {
            snackbarHostState.showSnackbar(it)
            toast = null
        }
    }

    Box {
        WtaSettingCard {
            Column {
                WtaChoiceRow(
                    title = Strings.configTemplates,
                    subtitle = Strings.configTemplatesDesc,
                    icon = Icons.Outlined.LibraryAddCheck,
                    value = "",
                    isExpanded = templates.isNotEmpty(),
                    onClick = { manageOpen = true }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = WtaSpacing.RowHorizontal, vertical = WtaSpacing.ContentGap),
                    horizontalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                ) {
                    WtaButton(
                        onClick = { saveDialogOpen = true },
                        text = Strings.templateSaveAs,
                        modifier = Modifier.weight(1f),
                        variant = WtaButtonVariant.Outlined,
                        leadingIcon = Icons.Outlined.EditNote
                    )
                }

                if (templates.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = WtaSpacing.RowHorizontal)) {
                        Text(
                            Strings.templateApplyHint,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))
                        templates.forEach { template ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = {
                                    onApplyConfig(template.webViewConfig)
                                    toast = Strings.templateApplied(template.name)
                                }) {
                                    Text(template.name, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))
                        TextButton(onClick = { manageOpen = true }) {
                            Text(Strings.templateManage)
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (saveDialogOpen) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { saveDialogOpen = false },
            title = { Text(Strings.templateSaveAs) },
            text = {
                PremiumTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Strings.templateNameLabel) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        if (ConfigTemplateStore.save(context, name, config)) {
                            refresh++
                            toast = Strings.templateSaved(name.trim())
                        }
                        saveDialogOpen = false
                    }
                ) { Text(Strings.btnSave) }
            },
            dismissButton = {
                TextButton(onClick = { saveDialogOpen = false }) { Text(Strings.cancel) }
            }
        )
    }

    if (manageOpen) {
        AlertDialog(
            onDismissRequest = { manageOpen = false },
            title = { Text(Strings.templateManage) },
            text = {
                if (templates.isEmpty()) {
                    Text(Strings.templateEmpty)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(templates, key = { it.name }) { template ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(template.name, modifier = Modifier.weight(1f))
                                TextButton(onClick = {
                                    renameTarget = template
                                    renameText = template.name
                                }) {
                                    Icon(
                                        Icons.Outlined.DriveFileRenameOutline,
                                        contentDescription = Strings.templateRename,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                TextButton(onClick = {
                                    ConfigTemplateStore.delete(context, template.name)
                                    refresh++
                                }) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = Strings.delete,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { manageOpen = false }) { Text(Strings.close) }
            }
        )
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(Strings.templateRename) },
            text = {
                PremiumTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text(Strings.templateNameLabel) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameText.isNotBlank(),
                    onClick = {
                        ConfigTemplateStore.rename(context, target.name, renameText)
                        renameTarget = null
                        refresh++
                    }
                ) { Text(Strings.btnSave) }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text(Strings.cancel) }
            }
        )
    }
}
