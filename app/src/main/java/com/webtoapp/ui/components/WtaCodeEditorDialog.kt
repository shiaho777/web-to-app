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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FindReplace
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaRadius

@Composable
fun WtaCodeEditorDialog(
    language: String,
    initialContent: String,
    placeholder: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    canSaveEmpty: Boolean = false,
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(initialContent, TextRange(initialContent.length)))
    }
    val codeText = textFieldValue.text
    val isModified = codeText != initialContent
    val accentColor = MaterialTheme.colorScheme.onSurface
    val scheme = rememberEditorColorScheme()
    val density = LocalDensity.current

    var showDiscardConfirm by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showReplace by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var activeMatchIndex by remember { mutableIntStateOf(0) }
    val searchFocusRequester = remember { FocusRequester() }
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val matches = remember(codeText, searchQuery, matchCase) {
        findMatchRanges(codeText, searchQuery, matchCase)
    }

    LaunchedEffect(matches) {
        if (matches.isEmpty()) {
            activeMatchIndex = 0
        } else if (activeMatchIndex >= matches.size) {
            activeMatchIndex = matches.lastIndex
        }
    }

    LaunchedEffect(showSearch) {
        if (showSearch) {
            runCatching { searchFocusRequester.requestFocus() }
        }
    }

    fun selectMatch(index: Int) {
        if (matches.isEmpty()) return
        val safeIndex = ((index % matches.size) + matches.size) % matches.size
        activeMatchIndex = safeIndex
        val range = matches[safeIndex]
        textFieldValue = textFieldValue.copy(selection = range)
    }

    fun goToNextMatch() {
        if (matches.isEmpty()) return
        selectMatch(activeMatchIndex + 1)
    }

    fun goToPreviousMatch() {
        if (matches.isEmpty()) return
        selectMatch(activeMatchIndex - 1)
    }

    fun replaceCurrent() {
        if (matches.isEmpty() || searchQuery.isEmpty()) return
        val range = matches[activeMatchIndex.coerceIn(0, matches.lastIndex)]
        val nextText = codeText.replaceRange(range.start, range.end, replaceQuery)
        val nextCursor = range.start + replaceQuery.length
        textFieldValue = TextFieldValue(
            text = nextText,
            selection = TextRange(nextCursor, nextCursor)
        )
    }

    fun replaceAllMatches() {
        if (searchQuery.isEmpty()) return
        val nextText = replaceAllMatches(codeText, searchQuery, replaceQuery, matchCase)
        textFieldValue = TextFieldValue(
            text = nextText,
            selection = TextRange(nextText.length)
        )
        activeMatchIndex = 0
    }

    LaunchedEffect(activeMatchIndex, matches, codeText) {
        if (matches.isEmpty()) return@LaunchedEffect
        val range = matches[activeMatchIndex.coerceIn(0, matches.lastIndex)]
        val lineIndex = codeText.take(range.start).count { it == '\n' }
        val lineHeightPx = with(density) { 20.sp.toPx() }
        val target = (lineIndex * lineHeightPx - verticalScrollState.viewportSize * 0.3f)
            .toInt()
            .coerceAtLeast(0)
        verticalScrollState.animateScrollTo(target.coerceAtMost(verticalScrollState.maxValue))
    }

    val requestDismiss: () -> Unit = {
        if (isModified) {
            showDiscardConfirm = true
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { requestDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            color = scheme.background.copy(alpha = 0.98f),
            shape = RectangleShape
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = scheme.background,
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
                                tint = scheme.foreground
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
                            color = scheme.foreground,
                            modifier = Modifier.weight(1f)
                        )

                        if (isModified) {
                            Surface(
                                color = scheme.foreground.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(WtaRadius.Badge)
                            ) {
                                Text(
                                    text = "●",
                                    color = scheme.foreground,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        IconButton(
                            onClick = {
                                showSearch = !showSearch
                                if (!showSearch) {
                                    showReplace = false
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = Strings.codeEditorFind,
                                tint = if (showSearch) accentColor else scheme.foreground
                            )
                        }

                        val canSave = canSaveEmpty || codeText.isNotBlank()
                        TextButton(
                            onClick = { onSave(codeText) },
                            enabled = canSave
                        ) {
                            Icon(
                                Icons.Outlined.Save,
                                contentDescription = null,
                                tint = if (canSave) scheme.foreground else scheme.muted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = Strings.saveFile,
                                color = if (canSave) scheme.foreground else scheme.muted
                            )
                        }
                    }
                }

                if (showSearch) {
                    Surface(color = scheme.backgroundAlt) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        activeMatchIndex = 0
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(searchFocusRequester),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        color = scheme.foreground
                                    ),
                                    placeholder = {
                                        Text(
                                            Strings.codeEditorFindHint,
                                            color = scheme.muted,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = { goToNextMatch() }
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = scheme.background,
                                        unfocusedContainerColor = scheme.background,
                                        focusedTextColor = scheme.foreground,
                                        unfocusedTextColor = scheme.foreground,
                                        cursorColor = accentColor,
                                        focusedIndicatorColor = accentColor.copy(alpha = 0.5f),
                                        unfocusedIndicatorColor = scheme.muted.copy(alpha = 0.3f)
                                    )
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (searchQuery.isBlank()) {
                                        "0/0"
                                    } else if (matches.isEmpty()) {
                                        "0/0"
                                    } else {
                                        "${activeMatchIndex + 1}/${matches.size}"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = scheme.foreground.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = { goToPreviousMatch() },
                                    enabled = matches.isNotEmpty()
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = Strings.codeEditorFindPrev,
                                        tint = if (matches.isNotEmpty()) scheme.foreground else scheme.muted
                                    )
                                }
                                IconButton(
                                    onClick = { goToNextMatch() },
                                    enabled = matches.isNotEmpty()
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = Strings.codeEditorFindNext,
                                        tint = if (matches.isNotEmpty()) scheme.foreground else scheme.muted
                                    )
                                }
                                IconButton(
                                    onClick = { showReplace = !showReplace }
                                ) {
                                    Icon(
                                        Icons.Outlined.FindReplace,
                                        contentDescription = Strings.codeEditorReplace,
                                        tint = if (showReplace) accentColor else scheme.foreground
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = matchCase,
                                    onClick = {
                                        matchCase = !matchCase
                                        activeMatchIndex = 0
                                    },
                                    label = {
                                        Text(
                                            Strings.codeEditorMatchCase,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                                Spacer(Modifier.weight(1f))
                                if (searchQuery.isNotBlank() && matches.isEmpty()) {
                                    Text(
                                        Strings.codeEditorNoMatches,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            if (showReplace) {
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        value = replaceQuery,
                                        onValueChange = { replaceQuery = it },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            color = scheme.foreground
                                        ),
                                        placeholder = {
                                            Text(
                                                Strings.codeEditorReplaceHint,
                                                color = scheme.muted,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = scheme.background,
                                            unfocusedContainerColor = scheme.background,
                                            focusedTextColor = scheme.foreground,
                                            unfocusedTextColor = scheme.foreground,
                                            cursorColor = accentColor,
                                            focusedIndicatorColor = accentColor.copy(alpha = 0.5f),
                                            unfocusedIndicatorColor = scheme.muted.copy(alpha = 0.3f)
                                        )
                                    )
                                    TextButton(
                                        onClick = { replaceCurrent() },
                                        enabled = matches.isNotEmpty()
                                    ) {
                                        Text(Strings.codeEditorReplace)
                                    }
                                    TextButton(
                                        onClick = { replaceAllMatches() },
                                        enabled = searchQuery.isNotBlank() && matches.isNotEmpty()
                                    ) {
                                        Text(Strings.codeEditorReplaceAll)
                                    }
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(verticalScrollState)
                    ) {
                        val lineCount = maxOf(codeText.count { it == '\n' } + 1, 1)
                        Column(
                            modifier = Modifier
                                .width(44.dp)
                                .background(scheme.background)
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
                                        color = scheme.gutter
                                    )
                                )
                            }
                        }

                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = scheme.foreground
                            ),
                            cursorBrush = SolidColor(accentColor),
                            visualTransformation = CodeSyntaxTransformation(language, scheme),
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
                                                color = scheme.muted
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Surface(color = scheme.backgroundAlt) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val lineCount = codeText.count { it == '\n' } + 1
                        val charCount = codeText.length
                        val selectionInfo = if (textFieldValue.selection.collapsed) {
                            val line = codeText.take(textFieldValue.selection.start).count { it == '\n' } + 1
                            val col = textFieldValue.selection.start -
                                (codeText.lastIndexOf('\n', (textFieldValue.selection.start - 1).coerceAtLeast(0)) + 1)
                                .coerceAtLeast(0)
                            "Ln $line, Col ${col + 1}"
                        } else {
                            val len = textFieldValue.selection.length
                            "Sel $len"
                        }
                        Text(
                            text = "$lineCount ${if (lineCount == 1) "line" else "lines"}, $charCount chars · $selectionInfo",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.foreground.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = language,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.foreground.copy(alpha = 0.9f)
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

internal fun findMatchRanges(
    text: String,
    query: String,
    matchCase: Boolean
): List<TextRange> {
    if (query.isEmpty()) return emptyList()
    val source = if (matchCase) text else text.lowercase()
    val needle = if (matchCase) query else query.lowercase()
    if (needle.isEmpty()) return emptyList()

    val ranges = mutableListOf<TextRange>()
    var start = 0
    while (start <= source.length - needle.length) {
        val index = source.indexOf(needle, startIndex = start)
        if (index < 0) break
        ranges.add(TextRange(index, index + needle.length))
        start = index + needle.length.coerceAtLeast(1)
    }
    return ranges
}

internal fun replaceAllMatches(
    text: String,
    query: String,
    replacement: String,
    matchCase: Boolean
): String {
    if (query.isEmpty()) return text
    if (matchCase) return text.replace(query, replacement)

    val ranges = findMatchRanges(text, query, matchCase = false)
    if (ranges.isEmpty()) return text
    val builder = StringBuilder(text)
    for (range in ranges.asReversed()) {
        builder.replace(range.start, range.end, replacement)
    }
    return builder.toString()
}
