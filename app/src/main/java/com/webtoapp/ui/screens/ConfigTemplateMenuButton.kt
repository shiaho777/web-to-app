package com.webtoapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.data.repository.ConfigTemplateStore
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.design.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/** "3 days ago"-style stamp; the platform formatter localizes it for free. */
private fun relativeCreatedAt(createdAt: Long): String =
    android.text.format.DateUtils.getRelativeTimeSpanString(
        createdAt,
        System.currentTimeMillis(),
        TimeUnit.MINUTES.toMillis(1)
    ).toString()

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
    LaunchedEffect(refresh) {
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
                leadingIcon = { Icon(Icons.Outlined.Save, null) },
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
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.LibraryAddCheck,
                                    null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
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
                    leadingIcon = { Icon(Icons.Outlined.Tune, null) },
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
        val trimmed = name.trim()
        val nameExists = trimmed.isNotEmpty() && templates.any {
            it.name.equals(trimmed, ignoreCase = true)
        }
        AlertDialog(
            onDismissRequest = { saveDialogOpen = false },
            icon = { Icon(Icons.Outlined.LibraryAddCheck, null) },
            title = { Text(Strings.templateSaveAs) },
            text = {
                Column {
                    Text(
                        Strings.configTemplatesDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PremiumTextField(
                        value = name,
                        onValueChange = { if (it.length <= 40) name = it },
                        label = { Text(Strings.templateNameLabel) },
                        singleLine = true,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    if (nameExists) Strings.templateOverwriteHint else "",
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    "${name.length}/40",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = trimmed.isNotEmpty(),
                    onClick = {
                        if (ConfigTemplateStore.save(context, trimmed, config)) {
                            refresh++
                            notify(Strings.templateSaved(trimmed))
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
        Dialog(
            onDismissRequest = { manageOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            WtaCard(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.6f),
                tone = WtaCardTone.Surface,
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(WtaRadius.Card)
            ) {
                Column {
                    TopAppBar(
                        title = { Text(Strings.templateManage) },
                        navigationIcon = {
                            IconButton(onClick = { manageOpen = false }) {
                                Icon(Icons.Outlined.Close, Strings.close)
                            }
                        }
                    )

                    if (templates.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.LibraryAddCheck,
                                null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                Strings.templateEmpty,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(templates, key = { it.name }) { template ->
                                WtaCard(
                                    onClick = {
                                        manageOpen = false
                                        onApplyConfig(template.webViewConfig)
                                        notify(Strings.templateApplied(template.name))
                                    },
                                    tone = WtaCardTone.Surface
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Icon(
                                                Icons.Outlined.LibraryAddCheck,
                                                null,
                                                modifier = Modifier.padding(8.dp).size(20.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                template.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                relativeCreatedAt(template.createdAt),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = {
                                            renameTarget = template
                                            renameText = template.name
                                        }) {
                                            Icon(
                                                Icons.Outlined.DriveFileRenameOutline,
                                                contentDescription = Strings.templateRename,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(onClick = {
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
                    }
                }
            }
        }
    }

    renameTarget?.let { target ->
        var nameExistsError by remember(target) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = {
                renameTarget = null
                nameExistsError = false
            },
            title = { Text(Strings.templateRename) },
            text = {
                PremiumTextField(
                    value = renameText,
                    onValueChange = {
                        if (it.length <= 40) renameText = it
                        nameExistsError = false
                    },
                    label = { Text(Strings.templateNameLabel) },
                    singleLine = true,
                    isError = nameExistsError,
                    supportingText = {
                        if (nameExistsError) {
                            Text(
                                Strings.templateOverwriteHint,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameText.isNotBlank(),
                    onClick = {
                        if (ConfigTemplateStore.rename(context, target.name, renameText)) {
                            renameTarget = null
                            refresh++
                        } else {
                            nameExistsError = true
                        }
                    }
                ) { Text(Strings.btnSave) }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text(Strings.cancel) }
            }
        )
    }
}
