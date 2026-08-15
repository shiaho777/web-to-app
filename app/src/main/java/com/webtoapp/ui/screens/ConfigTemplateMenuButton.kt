package com.webtoapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.data.repository.ConfigTemplateStore
import com.webtoapp.ui.components.PremiumTextField

/**
 * Top-bar action for common-config templates: save the current WebViewConfig as a named
 * snapshot, apply one to the app (a template is a full snapshot — applying replaces the
 * whole WebViewConfig), and manage saved templates. Lives in the editor's fixed top bar
 * instead of the scrolling config list, since it applies to the config as a whole.
 */
@Composable
fun ConfigTemplateMenuButton(
    config: WebViewConfig,
    snackbarHostState: SnackbarHostState,
    onApplyConfig: (WebViewConfig) -> Unit
) {
    val context = LocalContext.current
    var templates by remember { mutableStateOf(ConfigTemplateStore.list(context)) }
    var refresh by remember { mutableIntStateOf(0) }
    LaunchedEffect(refresh, templates.isEmpty()) {
        // reload after any mutation; also opportunistically refresh when the menu opens
        if (refresh > 0) templates = ConfigTemplateStore.list(context)
    }

    var menuOpen by remember { mutableStateOf(false) }
    var saveDialogOpen by remember { mutableStateOf(false) }
    var manageOpen by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<ConfigTemplateStore.ConfigTemplate?>(null) }
    var renameText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun notify(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Box {
        IconButton(onClick = {
            templates = ConfigTemplateStore.list(context)
            menuOpen = true
        }) {
            Icon(
                Icons.Outlined.LibraryAddCheck,
                contentDescription = Strings.configTemplates
            )
        }
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false }
        ) {
            DropdownMenuItem(
                text = { Text(Strings.templateSaveAs) },
                onClick = {
                    menuOpen = false
                    saveDialogOpen = true
                }
            )
            if (templates.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    Strings.templateApplyHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
                Column(modifier = Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState())) {
                    templates.forEach { template ->
                        DropdownMenuItem(
                            text = { Text(template.name) },
                            onClick = {
                                menuOpen = false
                                onApplyConfig(template.webViewConfig)
                                notify(Strings.templateApplied(template.name))
                            }
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = { Text(Strings.templateManage) },
                    onClick = {
                        menuOpen = false
                        manageOpen = true
                    }
                )
            }
        }
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
                            notify(Strings.templateSaved(name.trim()))
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
