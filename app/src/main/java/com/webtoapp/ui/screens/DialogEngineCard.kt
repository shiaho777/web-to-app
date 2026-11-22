package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.webtoapp.core.appmodifier.DialogAnalysisResult
import com.webtoapp.core.appmodifier.DialogEngineAnalyzer
import com.webtoapp.core.appmodifier.DialogEngineComponent
import com.webtoapp.core.appmodifier.DialogEngineComponentType
import com.webtoapp.core.appmodifier.DialogEngineProject
import com.webtoapp.core.appmodifier.DialogEngineProjectStore
import com.webtoapp.core.appmodifier.DialogEngineStyle
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.animation.CardCollapseTransition
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.components.WtaCodeEditorDialog
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaSegmentedRow
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.ui.design.WtaToggleRow
import com.webtoapp.ui.theme.LocalShowDescriptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
fun DialogEngineCard(
    project: DialogEngineProject,
    apkPath: String,
    onProjectChange: (DialogEngineProject) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var analysis by remember { mutableStateOf<DialogAnalysisResult?>(null) }
    var codeLanguage by remember { mutableStateOf<String?>(null) }
    var historyPast by remember { mutableStateOf<List<DialogEngineProject>>(emptyList()) }
    var historyFuture by remember { mutableStateOf<List<DialogEngineProject>>(emptyList()) }

    fun commit(next: DialogEngineProject) {
        if (next == project) return
        historyPast = (historyPast + project).takeLast(30)
        historyFuture = emptyList()
        onProjectChange(next)
    }

    fun undo() {
        val previous = historyPast.lastOrNull() ?: return
        historyPast = historyPast.dropLast(1)
        historyFuture = listOf(project) + historyFuture
        onProjectChange(previous)
    }

    fun redo() {
        val next = historyFuture.firstOrNull() ?: return
        historyFuture = historyFuture.drop(1)
        historyPast = (historyPast + project).takeLast(30)
        onProjectChange(next)
    }

    WtaSettingCard {
        WtaToggleRow(
            icon = Icons.Outlined.Visibility,
            title = Strings.dialogEngineTitle,
            checked = project.enabled,
            onCheckedChange = { commit(project.copy(enabled = it)) },
            subtitle = Strings.dialogEngineSubtitle.takeIf { LocalShowDescriptions.current }
        )

        AnimatedVisibility(
            visible = project.enabled,
            enter = CardExpandTransition,
            exit = CardCollapseTransition
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = ::undo, enabled = historyPast.isNotEmpty()) {
                        Text(Strings.undo)
                    }
                    TextButton(onClick = ::redo, enabled = historyFuture.isNotEmpty()) {
                        Text(Strings.redo)
                    }
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val file = File(apkPath)
                            val result = DialogEngineAnalyzer.analyze(file)
                            withContext(Dispatchers.Main) { analysis = result }
                        }
                    }) {
                        Icon(Icons.Outlined.Analytics, contentDescription = Strings.dialogEngineAnalyze)
                    }
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            DialogEngineProjectStore.save(
                                context,
                                project,
                                project.title.ifBlank { Strings.dialogEngineTitle }
                            )
                        }
                    }) {
                        Icon(Icons.Outlined.Save, contentDescription = Strings.dialogEngineSaveTemplate)
                    }
                }

                WtaTextField(
                    value = project.title,
                    onValueChange = { commit(project.copy(title = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = Strings.dialogEngineTitleField,
                    singleLine = true
                )
                WtaTextField(
                    value = project.message,
                    onValueChange = { commit(project.copy(message = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = Strings.dialogEngineMessageField
                )

                val styles = listOf(
                    DialogEngineStyle.MODERN to Strings.dialogEngineStyleModern,
                    DialogEngineStyle.MATERIAL to Strings.dialogEngineStyleMaterial,
                    DialogEngineStyle.BOTTOM_SHEET to Strings.dialogEngineStyleSheet,
                    DialogEngineStyle.FULLSCREEN to Strings.dialogEngineStyleFullscreen,
                    DialogEngineStyle.COMPACT to Strings.dialogEngineStyleCompact
                )
                WtaSegmentedRow(
                    options = styles.map { it.second },
                    selectedIndex = styles.indexOfFirst { it.first == project.style }.coerceAtLeast(0),
                    onSelect = { index ->
                        commit(project.copy(style = styles[index].first))
                    }
                )

                Text(Strings.widthLabel, style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = project.widthFraction,
                    onValueChange = { commit(project.copy(widthFraction = it)) },
                    valueRange = 0.5f..1f
                )
                Text(
                    "${Strings.dialogEngineCornerRadius} · ${project.cornerRadiusDp} dp",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = project.cornerRadiusDp.toFloat(),
                    onValueChange = { commit(project.copy(cornerRadiusDp = it.toInt())) },
                    valueRange = 0f..48f
                )
                Text(
                    "${Strings.dialogEngineShadow} · ${project.elevationDp} dp",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = project.elevationDp.toFloat(),
                    onValueChange = { commit(project.copy(elevationDp = it.toInt())) },
                    valueRange = 0f..32f
                )

                WtaCard(
                    tone = WtaCardTone.Surface,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(Strings.dialogEngineComponents, style = MaterialTheme.typography.titleSmall)
                            IconButton(onClick = {
                                val component = DialogEngineComponent(
                                    id = UUID.randomUUID().toString(),
                                    type = DialogEngineComponentType.BUTTON,
                                    text = Strings.dialogEngineButton
                                )
                                commit(project.copy(components = project.components + component))
                            }) {
                                Icon(Icons.Outlined.Add, contentDescription = Strings.dialogEngineAddComponent)
                            }
                        }

                        project.components.forEachIndexed { index, component ->
                            DialogComponentEditor(
                                component = component,
                                index = index,
                                count = project.components.size,
                                onChange = { next ->
                                    commit(project.copy(
                                        components = project.components.toMutableList().also { it[index] = next }
                                    ))
                                },
                                onMoveUp = {
                                    if (index > 0) commit(project.copy(
                                        components = project.components.toMutableList().also {
                                            val item = it.removeAt(index)
                                            it.add(index - 1, item)
                                        }
                                    ))
                                },
                                onMoveDown = {
                                    if (index < project.components.lastIndex) commit(project.copy(
                                        components = project.components.toMutableList().also {
                                            val item = it.removeAt(index)
                                            it.add(index + 1, item)
                                        }
                                    ))
                                },
                                onDelete = {
                                    commit(project.copy(
                                        components = project.components.filterIndexed { i, _ -> i != index }
                                    ))
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("XML" to "xmlSource", "Java" to "javaSource", "Smali" to "smaliSource").forEach { (label, field) ->
                        WtaChip(
                            selected = codeLanguage == label,
                            onClick = { codeLanguage = label },
                            label = label,
                            leadingIcon = Icons.Outlined.Code
                        )
                    }
                }

                analysis?.let { result ->
                    WtaCard(tone = WtaCardTone.Highlighted, contentPadding = PaddingValues(12.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = Strings.dialogEngineAnalysisDialogs.replace(
                                    "%s",
                                    result.dialogClasses.joinToString()
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = Strings.dialogEngineAnalysisLayouts.replace(
                                    "%s",
                                    result.dialogLayouts.joinToString()
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (result.warnings.isNotEmpty()) {
                                Text(
                                    text = result.warnings.joinToString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    codeLanguage?.let { language ->
        val initial = when (language) {
            "XML" -> project.xmlSource
            "Java" -> project.javaSource
            else -> project.smaliSource
        }
        WtaCodeEditorDialog(
            language = language,
            initialContent = initial,
            placeholder = Strings.dialogEngineSourcePlaceholder(language),
            onSave = { code ->
                commit(when (language) {
                    "XML" -> project.copy(xmlSource = code)
                    "Java" -> project.copy(javaSource = code)
                    else -> project.copy(smaliSource = code)
                })
                codeLanguage = null
            },
            onDismiss = { codeLanguage = null },
            canSaveEmpty = true
        )
    }
}

@Composable
private fun DialogComponentEditor(
    component: DialogEngineComponent,
    index: Int,
    count: Int,
    onChange: (DialogEngineComponent) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface, contentPadding = PaddingValues(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}. ${dialogEngineComponentTypeLabel(component.type)}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onMoveUp, enabled = index > 0) {
                    Icon(Icons.Outlined.ArrowUpward, contentDescription = Strings.moveUp)
                }
                IconButton(onClick = onMoveDown, enabled = index < count - 1) {
                    Icon(Icons.Outlined.ArrowDownward, contentDescription = Strings.moveDown)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = Strings.delete)
                }
            }

            WtaTextField(
                value = component.text,
                onValueChange = { onChange(component.copy(text = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = Strings.dialogEngineText,
                singleLine = true
            )
            val types = DialogEngineComponentType.entries
            WtaSegmentedRow(
                options = types.map(::dialogEngineComponentTypeLabel),
                selectedIndex = types.indexOf(component.type),
                onSelect = { onChange(component.copy(type = types[it])) }
            )
        }
    }
}

private fun dialogEngineComponentTypeLabel(type: DialogEngineComponentType): String =
    when (type) {
        DialogEngineComponentType.TITLE -> Strings.dialogEngineTypeTitle
        DialogEngineComponentType.MESSAGE -> Strings.dialogEngineTypeMessage
        DialogEngineComponentType.IMAGE -> Strings.dialogEngineTypeImage
        DialogEngineComponentType.TEXT_FIELD -> Strings.dialogEngineTypeTextField
        DialogEngineComponentType.PASSWORD -> Strings.dialogEngineTypePassword
        DialogEngineComponentType.CHECKBOX -> Strings.dialogEngineTypeCheckbox
        DialogEngineComponentType.SWITCH -> Strings.dialogEngineTypeSwitch
        DialogEngineComponentType.LIST -> Strings.dialogEngineTypeList
        DialogEngineComponentType.BUTTON -> Strings.dialogEngineTypeButton
        DialogEngineComponentType.PROGRESS -> Strings.dialogEngineTypeProgress
    }
