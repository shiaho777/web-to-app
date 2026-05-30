package com.webtoapp.ui.aicoding

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.webtoapp.core.aicoding.skill.SkillEditorInput
import com.webtoapp.core.aicoding.skill.SkillLoader
import com.webtoapp.core.aicoding.skill.UserSkillRepository
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaTextField
import kotlinx.coroutines.launch

@Composable
fun SkillEditorScreen(
    skillName: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val loader = remember { SkillLoader(app) }
    val repo = remember { UserSkillRepository(loader) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var whenToUse by remember { mutableStateOf("") }
    var argumentHint by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(skillName) {
        if (skillName != null && !loaded) {
            val existing = repo.load(skillName)
            if (existing != null) {
                name = existing.name
                description = existing.description
                whenToUse = existing.whenToUse
                argumentHint = existing.argumentHint
                body = existing.body
            }
        }
        loaded = true
    }

    fun validateName(input: String): String? {
        if (input.isBlank()) return null
        if (!Regex("^[a-z0-9]+(-[a-z0-9]+)*$").matches(input)) {
            return Strings.aiCodingSkillNameInvalid
        }
        return null
    }

    val isEditing = skillName != null
    val canSave = name.isNotBlank() && body.isNotBlank() && nameError == null && !saving

    fun doSave() {
        if (!canSave) return
        saving = true
        scope.launch {
            val result = repo.save(
                SkillEditorInput(
                    name = name,
                    description = description,
                    whenToUse = whenToUse,
                    argumentHint = argumentHint,
                    body = body
                )
            )
            saving = false
            result.fold(
                onSuccess = {
                    snackbar.showSnackbar(Strings.aiCodingSkillSaved.format(name))
                    onBack()
                },
                onFailure = { err ->
                    snackbar.showSnackbar(
                        Strings.aiCodingSkillSaveFailed.format(err.message ?: "")
                    )
                }
            )
        }
    }

    fun doDelete() {
        val target = skillName ?: return
        deleting = true
        scope.launch {
            val ok = repo.delete(target)
            deleting = false
            if (ok) {
                snackbar.showSnackbar(Strings.aiCodingSkillDeleted.format(target))
                onBack()
            } else {
                snackbar.showSnackbar(
                    Strings.aiCodingSkillSaveFailed.format("")
                )
            }
        }
    }

    WtaScreen(
        title = if (isEditing) Strings.aiCodingEditSkill else Strings.aiCodingNewSkill,
        onBack = onBack,
        snackbarHostState = snackbar,
        actions = {

            if (isEditing) {
                WtaIconButton(
                    onClick = { showDeleteConfirm = true },
                    icon = Icons.Outlined.Delete,
                    contentDescription = Strings.aiCodingDeleteSkill,
                    enabled = !deleting && !saving
                )
            }
        }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WtaSpacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Medium)
        ) {
            WtaTextField(
                value = name,
                onValueChange = {

                    val cleaned = it.lowercase()
                    name = cleaned
                    nameError = validateName(cleaned)
                },
                label = Strings.aiCodingSkillNameLabel,
                placeholder = Strings.aiCodingSkillNameHint,
                supportingText = nameError,
                isError = nameError != null,
                singleLine = true,
                enabled = !isEditing,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = description,
                onValueChange = { description = it },
                label = Strings.aiCodingSkillDescriptionLabel,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = whenToUse,
                onValueChange = { whenToUse = it },
                label = Strings.aiCodingSkillWhenToUseLabel,
                placeholder = Strings.aiCodingSkillWhenToUseHint,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            WtaTextField(
                value = argumentHint,
                onValueChange = { argumentHint = it },
                label = "arguments",
                placeholder = "e.g. <prompt>",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            WtaTextField(
                value = body,
                onValueChange = { body = it },
                label = Strings.aiCodingSkillBodyLabel,
                placeholder = Strings.aiCodingSkillBodyHint,
                singleLine = false,
                maxLines = 30,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 500.dp)
            )
            Spacer(Modifier.height(WtaSpacing.Small))
            WtaButton(
                onClick = ::doSave,
                text = Strings.aiCodingSaveAsAppConfirm,
                enabled = canSave,
                variant = WtaButtonVariant.Primary,
                size = WtaButtonSize.Medium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(WtaSpacing.Large))
        }
    }

    if (showDeleteConfirm) {
        WtaAlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = Icons.Outlined.Delete,
            title = Strings.aiCodingDeleteSkill,
            text = Strings.aiCodingSkillDeleteConfirm,
            confirmButton = {
                WtaButton(
                    onClick = {
                        showDeleteConfirm = false
                        doDelete()
                    },
                    text = Strings.aiCodingDeleteSkill,
                    variant = WtaButtonVariant.Destructive,
                    size = WtaButtonSize.Small
                )
            },
            dismissButton = {
                WtaButton(
                    onClick = { showDeleteConfirm = false },
                    text = Strings.aiCodingActionCancel,
                    variant = WtaButtonVariant.Text,
                    size = WtaButtonSize.Small
                )
            }
        )
    }
}
