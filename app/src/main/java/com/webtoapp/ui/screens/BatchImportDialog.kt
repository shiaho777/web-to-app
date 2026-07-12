package com.webtoapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.stats.BatchImportService
import com.webtoapp.ui.components.PremiumButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BatchImportDialog(
    importService: BatchImportService,
    onDismiss: () -> Unit,
    onImport: suspend (List<BatchImportService.ParsedEntry>) -> BatchImportService.ImportResult
) {
    var tab by remember { mutableStateOf(0) }
    var textInput by remember { mutableStateOf("") }
    var parseResult by remember {
        mutableStateOf(BatchImportService.ParseResult(emptyList()))
    }
    var isImporting by remember { mutableStateOf(false) }
    var isParsingFile by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<BatchImportService.ImportResult?>(null) }
    var parseError by remember { mutableStateOf<String?>(null) }
    val importScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(textInput, tab) {
        if (tab != 0) return@LaunchedEffect
        delay(220)
        parseResult = withContext(Dispatchers.Default) {
            importService.parseTextDetailed(textInput)
        }
        parseError = null
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        isParsingFile = true
        parseError = null
        importScope.launch {
            try {
                val entries = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use {
                        importService.parseFromBookmarksHtml(it)
                    } ?: emptyList()
                }
                parseResult = BatchImportService.ParseResult(entries = entries)
                if (entries.isEmpty()) {
                    parseError = Strings.batchImportNoValid
                }
            } catch (e: Exception) {
                parseResult = BatchImportService.ParseResult(emptyList())
                parseError = e.message ?: Strings.batchImportNoValid
            } finally {
                isParsingFile = false
            }
        }
    }

    val entries = parseResult.entries
    val preview = remember(entries) { entries.take(80) }

    AlertDialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        title = { Text(Strings.batchImportTitle) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tab == 0,
                        onClick = {
                            tab = 0
                            importResult = null
                            parseError = null
                        },
                        label = {
                            Text(
                                Strings.batchImportFromText,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                    FilterChip(
                        selected = tab == 1,
                        onClick = {
                            tab = 1
                            importResult = null
                            parseError = null
                            parseResult = BatchImportService.ParseResult(emptyList())
                        },
                        label = {
                            Text(
                                Strings.batchImportFromBookmarks,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }

                if (tab == 0) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = {
                            textInput = it
                            importResult = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp),
                        placeholder = { Text(Strings.batchImportHint) },
                        minLines = 6,
                        maxLines = 12
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = {
                                val clip = clipboard.getText()?.text.orEmpty()
                                if (clip.isNotBlank()) {
                                    textInput = if (textInput.isBlank()) clip
                                    else textInput.trimEnd() + "\n" + clip
                                    importResult = null
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.ContentPaste, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(Strings.batchImportPaste)
                        }
                        if (textInput.isNotBlank()) {
                            TextButton(onClick = {
                                textInput = ""
                                importResult = null
                            }) {
                                Text(Strings.batchImportClear)
                            }
                        }
                    }
                } else {
                    PremiumButton(
                        onClick = { filePicker.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isParsingFile && !isImporting
                    ) {
                        if (isParsingFile) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.UploadFile, null, Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(Strings.batchImportPickBookmarks)
                    }
                    Text(
                        Strings.batchImportBookmarksHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (parseError != null) {
                    Text(
                        parseError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (entries.isNotEmpty() || parseResult.invalidLineCount > 0 || parseResult.duplicateInInputCount > 0) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                Strings.batchImportParsed.replace("%d", entries.size.toString()),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (parseResult.invalidLineCount > 0 || parseResult.duplicateInInputCount > 0) {
                                Text(
                                    Strings.batchImportParseStats(
                                        parseResult.invalidLineCount,
                                        parseResult.duplicateInInputCount
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (preview.isNotEmpty()) {
                                HorizontalDivider()
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 180.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    itemsIndexed(
                                        preview,
                                        key = { index, entry -> "${entry.url}#$index" }
                                    ) { _, entry ->
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                entry.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                entry.url,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    if (entries.size > preview.size) {
                                        item {
                                            Text(
                                                Strings.batchImportMore(entries.size - preview.size),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                importResult?.let { result ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    Strings.batchImportSuccess.replace("%d", result.imported.toString()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (result.skippedDuplicate > 0) {
                                    Text(
                                        Strings.batchImportSkipped(result.skippedDuplicate),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (importResult != null) {
                TextButton(onClick = onDismiss) {
                    Text(Strings.close)
                }
            } else if (!isImporting) {
                PremiumButton(
                    onClick = {
                        isImporting = true
                        importScope.launch {
                            try {
                                importResult = onImport(entries)
                            } finally {
                                isImporting = false
                            }
                        }
                    },
                    enabled = entries.isNotEmpty() && !isParsingFile
                ) {
                    Icon(Icons.Outlined.FileDownload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(Strings.batchImportBtn)
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        },
        dismissButton = {
            if (!isImporting && importResult == null) {
                TextButton(onClick = onDismiss) {
                    Text(Strings.btnCancel)
                }
            }
        }
    )
}
