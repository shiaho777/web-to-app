package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowLeft
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material.icons.automirrored.rounded.WrapText
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FindReplace
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaDivider
import com.webtoapp.ui.design.WtaExpandIn
import com.webtoapp.ui.design.WtaFadeIn
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaPopIn
import com.webtoapp.ui.design.WtaPopOut
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaShapePlate
import com.webtoapp.ui.design.WtaShrinkOut
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val colors = MaterialTheme.colorScheme
    val scheme = rememberEditorColorScheme()
    val density = LocalDensity.current

    var showDiscardConfirm by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showReplace by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var activeMatchIndex by remember { mutableIntStateOf(0) }
    var entered by remember { mutableStateOf(false) }
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val searchFocusRequester = remember { FocusRequester() }
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    var softWrap by remember { mutableStateOf(true) }
    val syntax = remember(language, scheme) { CodeSyntaxTransformation(language, scheme) }
    val editorTextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        color = scheme.foreground
    )
    val searchFieldStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        color = colors.onSurface
    )

    val matches = remember(codeText, searchQuery, matchCase) {
        findMatchRanges(codeText, searchQuery, matchCase)
    }
    val matchColor = colors.primary.copy(alpha = 0.22f)
    val activeMatchColor = colors.primary.copy(alpha = 0.45f)
    val transformation = remember(syntax, matches, activeMatchIndex, matchColor, activeMatchColor) {
        SearchHighlightTransformation(syntax, matches, activeMatchIndex, matchColor, activeMatchColor)
    }

    LaunchedEffect(Unit) { entered = true }

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
            modifier = Modifier.fillMaxSize(),
            color = scheme.background,
            shape = RectangleShape
        ) {
            AnimatedVisibility(visible = entered, enter = WtaFadeIn) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(color = colors.surfaceContainerLow) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = WtaSpacing.Small, vertical = WtaSpacing.Tiny),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WtaIconButton(
                                onClick = requestDismiss,
                                icon = Icons.Rounded.Close,
                                contentDescription = Strings.close
                            )
                            WtaShapePlate(
                                polygon = MaterialShapes.Slanted,
                                containerColor = colors.primaryContainer,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Code,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp),
                                    tint = colors.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(WtaSpacing.Small))
                            Text(
                                text = Strings.codeEditorTitle,
                                style = MaterialTheme.typography.titleMediumEmphasized,
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            AnimatedVisibility(
                                visible = isModified,
                                enter = WtaPopIn,
                                exit = WtaPopOut
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = WtaSpacing.Tiny)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                )
                            }
                            WtaIconButton(
                                onClick = { softWrap = !softWrap },
                                icon = Icons.AutoMirrored.Rounded.WrapText,
                                contentDescription = Strings.codeEditorSoftWrap,
                                tonal = softWrap
                            )
                            WtaIconButton(
                                onClick = {
                                    showSearch = !showSearch
                                    if (!showSearch) {
                                        showReplace = false
                                    }
                                },
                                icon = Icons.Rounded.Search,
                                contentDescription = Strings.codeEditorFind,
                                tonal = showSearch
                            )
                            val canSave = canSaveEmpty || codeText.isNotBlank()
                            WtaButton(
                                onClick = { onSave(codeText) },
                                text = Strings.saveFile,
                                variant = WtaButtonVariant.Primary,
                                size = WtaButtonSize.Small,
                                enabled = canSave,
                                leadingIcon = Icons.Rounded.Save,
                                modifier = Modifier.padding(start = WtaSpacing.Tiny)
                            )
                        }
                    }
                    WtaDivider()

                    AnimatedVisibility(
                        visible = showSearch,
                        enter = WtaExpandIn,
                        exit = WtaShrinkOut
                    ) {
                        Surface(color = colors.surfaceContainer) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = WtaSpacing.Medium, vertical = WtaSpacing.Small)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PremiumTextField(
                                        value = searchQuery,
                                        onValueChange = {
                                            searchQuery = it
                                            activeMatchIndex = 0
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(searchFocusRequester),
                                        singleLine = true,
                                        textStyle = searchFieldStyle,
                                        placeholder = {
                                            Text(
                                                Strings.codeEditorFindHint,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Rounded.Search,
                                                contentDescription = null,
                                                modifier = Modifier.size(WtaSize.IconSmall)
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(
                                            onSearch = { goToNextMatch() }
                                        ),
                                        shape = RoundedCornerShape(WtaRadius.Control)
                                    )
                                    Spacer(modifier = Modifier.width(WtaSpacing.Tiny))
                                    WtaInfoChip(
                                        label = if (matches.isEmpty()) {
                                            "0/0"
                                        } else {
                                            "${activeMatchIndex + 1}/${matches.size}"
                                        },
                                        emphasized = true
                                    )
                                    WtaIconButton(
                                        onClick = { goToPreviousMatch() },
                                        icon = Icons.AutoMirrored.Rounded.ArrowLeft,
                                        contentDescription = Strings.codeEditorFindPrev,
                                        enabled = matches.isNotEmpty()
                                    )
                                    WtaIconButton(
                                        onClick = { goToNextMatch() },
                                        icon = Icons.AutoMirrored.Rounded.ArrowRight,
                                        contentDescription = Strings.codeEditorFindNext,
                                        enabled = matches.isNotEmpty()
                                    )
                                    WtaIconButton(
                                        onClick = { showReplace = !showReplace },
                                        icon = Icons.Rounded.FindReplace,
                                        contentDescription = Strings.codeEditorReplace,
                                        tonal = showReplace
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = WtaSpacing.Small),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                                ) {
                                    WtaChip(
                                        selected = matchCase,
                                        onClick = {
                                            matchCase = !matchCase
                                            activeMatchIndex = 0
                                        },
                                        label = Strings.codeEditorMatchCase,
                                        showSelectedCheck = false
                                    )
                                    if (searchQuery.isNotBlank() && matches.isEmpty()) {
                                        Text(
                                            Strings.codeEditorNoMatches,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.error
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = showReplace,
                                    enter = WtaExpandIn,
                                    exit = WtaShrinkOut
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = WtaSpacing.Small),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        PremiumTextField(
                                            value = replaceQuery,
                                            onValueChange = { replaceQuery = it },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            textStyle = searchFieldStyle,
                                            placeholder = {
                                                Text(
                                                    Strings.codeEditorReplaceHint,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Rounded.FindReplace,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(WtaSize.IconSmall)
                                                )
                                            },
                                            shape = RoundedCornerShape(WtaRadius.Control)
                                        )
                                        Spacer(modifier = Modifier.width(WtaSpacing.Tiny))
                                        WtaButton(
                                            onClick = { replaceCurrent() },
                                            text = Strings.codeEditorReplace,
                                            variant = WtaButtonVariant.Tonal,
                                            size = WtaButtonSize.Small,
                                            enabled = matches.isNotEmpty()
                                        )
                                        Spacer(modifier = Modifier.width(WtaSpacing.Tiny))
                                        WtaButton(
                                            onClick = { replaceAllMatches() },
                                            text = Strings.codeEditorReplaceAll,
                                            variant = WtaButtonVariant.Outlined,
                                            size = WtaButtonSize.Small,
                                            enabled = searchQuery.isNotBlank() && matches.isNotEmpty()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(scheme.background)
                    ) {
                        val lineCount = maxOf(codeText.count { it == '\n' } + 1, 1)
                        val cursorLine = codeText.take(textFieldValue.selection.start)
                            .count { it == '\n' } + 1
                        val showGutter = !softWrap && lineCount <= 5_000
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(verticalScrollState)
                        ) {
                            if (showGutter) {
                                val gutterContent = remember(lineCount, cursorLine, scheme.gutter, colors.primary) {
                                    buildAnnotatedString {
                                        for (line in 1..lineCount) {
                                            val start = length
                                            append(line.toString())
                                            if (line == cursorLine) {
                                                addStyle(
                                                    SpanStyle(
                                                        color = colors.primary,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    start,
                                                    length
                                                )
                                            }
                                            if (line < lineCount) append('\n')
                                        }
                                    }
                                }
                                Text(
                                    text = gutterContent,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier
                                        .width(44.dp)
                                        .background(scheme.background)
                                        .padding(end = 8.dp, top = 8.dp),
                                    style = editorTextStyle.copy(color = scheme.gutter)
                                )
                            }

                            BasicTextField(
                                value = textFieldValue,
                                onValueChange = { textFieldValue = it },
                                textStyle = editorTextStyle,
                                cursorBrush = SolidColor(colors.primary),
                                visualTransformation = transformation,
                                onTextLayout = { textLayout = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (softWrap) {
                                            Modifier
                                        } else {
                                            Modifier.horizontalScroll(horizontalScrollState)
                                        }
                                    )
                                    .padding(
                                        start = if (showGutter) 9.dp else 12.dp,
                                        top = 8.dp,
                                        end = 8.dp,
                                        bottom = 8.dp
                                    ),
                                decorationBox = { innerTextField ->
                                    Box {
                                        val layout = textLayout
                                        if (layout != null) {
                                            val cursor = textFieldValue.selection.start
                                                .coerceIn(0, layout.layoutInput.text.length)
                                            val cursorRect = layout.getCursorRect(cursor)
                                            Box(
                                                modifier = Modifier
                                                    .offset(y = with(density) { cursorRect.top.toDp() })
                                                    .width(with(density) { layout.size.width.toDp() })
                                                    .height(with(density) { cursorRect.height.toDp() })
                                                    .background(scheme.foreground.copy(alpha = 0.06f))
                                            )
                                        }
                                        if (codeText.isEmpty()) {
                                            Text(
                                                text = placeholder,
                                                style = editorTextStyle.copy(color = scheme.muted)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        EditorScrollbar(
                            scrollState = verticalScrollState,
                            color = scheme.foreground.copy(alpha = 0.28f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(5.dp)
                                .padding(vertical = 4.dp, horizontal = 1.dp)
                        )
                        if (!softWrap) {
                            EditorScrollbar(
                                scrollState = horizontalScrollState,
                                color = scheme.foreground.copy(alpha = 0.28f),
                                horizontal = true,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    WtaDivider()
                    Surface(color = colors.surfaceContainerLow) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = WtaSpacing.Medium, vertical = WtaSpacing.Small),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                        ) {
                            val lineCount = codeText.count { it == '\n' } + 1
                            val charCount = codeText.length
                            val selection = textFieldValue.selection
                            val positionLabel = if (selection.collapsed) {
                                val line = codeText.take(selection.start).count { it == '\n' } + 1
                                val col = selection.start -
                                    (codeText.lastIndexOf('\n', (selection.start - 1).coerceAtLeast(0)) + 1)
                                        .coerceAtLeast(0)
                                Strings.codeEditorPosition(line, col + 1)
                            } else {
                                Strings.codeEditorSelectionCount(selection.length)
                            }
                            WtaInfoChip(
                                label = Strings.codeEditorLineCount(lineCount),
                                icon = Icons.Rounded.FormatLineSpacing
                            )
                            WtaInfoChip(
                                label = Strings.codeEditorCharCount(charCount),
                                icon = Icons.Rounded.DataObject
                            )
                            WtaInfoChip(
                                label = positionLabel,
                                icon = if (selection.collapsed) {
                                    Icons.Rounded.MyLocation
                                } else {
                                    Icons.Rounded.TextFields
                                }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            WtaInfoChip(
                                label = language,
                                containerColor = colors.primaryContainer,
                                contentColor = colors.onPrimaryContainer,
                                emphasized = true
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDiscardConfirm) {
        WtaAlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            icon = Icons.Rounded.EditNote,
            title = Strings.unsavedChangesTitle,
            text = Strings.unsavedChangesMessage,
            confirmButton = {
                WtaButton(
                    onClick = {
                        showDiscardConfirm = false
                        onDismiss()
                    },
                    text = Strings.discardChanges,
                    variant = WtaButtonVariant.Destructive,
                    size = WtaButtonSize.Small
                )
            },
            dismissButton = {
                WtaButton(
                    onClick = { showDiscardConfirm = false },
                    text = Strings.keepEditing,
                    variant = WtaButtonVariant.Tonal,
                    size = WtaButtonSize.Small
                )
            }
        )
    }
}

@Composable
private fun EditorScrollbar(
    scrollState: ScrollState,
    color: Color,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false
) {
    val maxValue = scrollState.maxValue
    if (maxValue <= 0) return
    val viewport = scrollState.viewportSize.toFloat().coerceAtLeast(1f)
    val thumbFraction = (viewport / (viewport + maxValue)).coerceIn(0.08f, 1f)
    val progress = scrollState.value.toFloat() / maxValue
    Canvas(modifier = modifier) {
        val trackLength = if (horizontal) size.width else size.height
        val thickness = if (horizontal) size.height else size.width
        val thumbLength = (trackLength * thumbFraction).coerceAtLeast(24.dp.toPx())
            .coerceAtMost(trackLength)
        val thumbOffset = (trackLength - thumbLength) * progress
        val corner = CornerRadius(thickness / 2f)
        drawRoundRect(
            color = color,
            topLeft = if (horizontal) Offset(thumbOffset, 0f) else Offset(0f, thumbOffset),
            size = if (horizontal) Size(thumbLength, thickness) else Size(thickness, thumbLength),
            cornerRadius = corner
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
