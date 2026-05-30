package com.webtoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.theme.AppColors

@Composable
fun WtaCodeEditorDialog(
    language: String,
    initialContent: String,
    placeholder: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    canSaveEmpty: Boolean = false,
) {
    var codeText by remember { mutableStateOf(initialContent) }
    val isModified = codeText != initialContent
    val accentColor = MaterialTheme.colorScheme.onSurface

    var showDiscardConfirm by remember { mutableStateOf(false) }

    val requestDismiss: () -> Unit = {
        if (isModified) {
            showDiscardConfirm = true
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {

            requestDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,

            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            color = AppColors.EditorDark.copy(alpha = 0.98f),
            shape = RectangleShape
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Surface(
                    color = AppColors.EditorDark,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = requestDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = Strings.close,
                                tint = AppColors.CodeForeground
                            )
                        }

                        Surface(
                            color = accentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(WtaRadius.Badge)
                        ) {
                            Text(
                                text = language,
                                style = MaterialTheme.typography.labelMedium,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = Strings.codeEditorTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = AppColors.CodeForeground,
                            modifier = Modifier.weight(1f)
                        )

                        if (isModified) {
                            Surface(
                                color = AppColors.CodeForeground.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(WtaRadius.Badge)
                            ) {
                                Text(
                                    text = "●",
                                    color = AppColors.CodeForeground,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        val canSave = canSaveEmpty || codeText.isNotBlank()
                        TextButton(
                            onClick = { onSave(codeText) },
                            enabled = canSave
                        ) {
                            Icon(
                                Icons.Outlined.Save,
                                contentDescription = null,
                                tint = if (canSave) AppColors.CodeForeground else AppColors.CodeMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = Strings.saveFile,
                                color = if (canSave) AppColors.CodeForeground else AppColors.CodeMuted
                            )
                        }
                    }
                }

                val verticalScrollState = rememberScrollState()
                val horizontalScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(verticalScrollState)
                    ) {
                        val lineCount = maxOf(codeText.count { it == '\n' } + 1, 1)
                        Column(
                            modifier = Modifier
                                .width(44.dp)
                                .background(AppColors.EditorDark)
                                .padding(end = 8.dp, top = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (i in 1..lineCount) {
                                Text(
                                    text = "$i",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = AppColors.CodeGutter
                                    )
                                )
                            }
                        }

                        BasicTextField(
                            value = codeText,
                            onValueChange = { codeText = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = AppColors.CodeForeground
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(horizontalScrollState)
                                .padding(start = 9.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (codeText.isEmpty()) {
                                        Text(
                                            text = placeholder,
                                            style = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 13.sp,
                                                lineHeight = 20.sp,
                                                color = AppColors.CodeMuted
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Surface(color = AppColors.EditorDarkAlt) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val lineCount = codeText.count { it == '\n' } + 1
                        val charCount = codeText.length
                        Text(
                            text = "$lineCount ${if (lineCount == 1) "line" else "lines"}, $charCount chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.CodeForeground.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = language,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text(Strings.unsavedChangesTitle) },
            text = { Text(Strings.unsavedChangesMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardConfirm = false
                        onDismiss()
                    }
                ) {
                    Text(
                        text = Strings.discardChanges,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text(Strings.keepEditing)
                }
            }
        )
    }
}
