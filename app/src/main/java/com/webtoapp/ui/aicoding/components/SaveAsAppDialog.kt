package com.webtoapp.ui.aicoding.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.webtoapp.core.aicoding.export.DetectedArtifact
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaTextField

@Composable
fun SaveAsAppDialog(
    artifacts: List<DetectedArtifact>,
    selectedArtifactId: String?,
    inFlight: Boolean,
    onSelectArtifact: (String) -> Unit,
    onConfirm: (name: String, iconUri: Uri?) -> Unit,
    onDismiss: () -> Unit
) {
    val selected = artifacts.firstOrNull { it.id == selectedArtifactId } ?: artifacts.firstOrNull()
    var name by remember(selected?.id) {
        mutableStateOf(selected?.displayName.orEmpty())
    }
    var iconUri by remember(selected?.id) { mutableStateOf<Uri?>(null) }

    LaunchedEffect(selected?.id) {
        name = selected?.displayName.orEmpty()
    }

    val iconPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { picked -> picked?.let { iconUri = it } }

    val isAppArtifact = selected?.kind?.target == DetectedArtifact.Kind.Target.App

    WtaAlertDialog(
        onDismissRequest = { if (!inFlight) onDismiss() },
        icon = Icons.Outlined.Apps,
        title = if (selected?.kind?.target == DetectedArtifact.Kind.Target.Module) {
            Strings.aiCodingSaveAsModule
        } else {
            Strings.aiCodingSaveAsAppTitle
        },
        confirmButton = {
            WtaButton(
                onClick = { onConfirm(name.trim(), iconUri) },
                enabled = !inFlight && selected != null && (!isAppArtifact || name.isNotBlank()),
                variant = WtaButtonVariant.Primary,
                size = WtaButtonSize.Small
            ) {
                if (inFlight) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(Strings.aiCodingSaveAsAppConfirm)
                }
            }
        },
        dismissButton = {
            WtaButton(
                onClick = onDismiss,
                enabled = !inFlight,
                text = Strings.aiCodingActionCancel,
                variant = WtaButtonVariant.Text,
                size = WtaButtonSize.Small
            )
        },
        content = {

            Text(
                text = subtitleFor(selected),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(WtaSpacing.Small))

            Text(
                text = Strings.aiCodingSaveDialogChooseLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.size(WtaSpacing.Tiny))
            ArtifactList(
                artifacts = artifacts,
                selectedId = selected?.id,
                onSelect = onSelectArtifact
            )

            if (isAppArtifact) {
                Spacer(Modifier.size(WtaSpacing.Medium))
                WtaTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = Strings.aiCodingSaveAsAppNameLabel,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.size(WtaSpacing.Small))
                IconSlot(
                    iconUri = iconUri,
                    onPick = { iconPicker.launch("image/*") },
                    onClear = { iconUri = null }
                )
            }
        }
    )
}

@Composable
private fun ArtifactList(
    artifacts: List<DetectedArtifact>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny + 2.dp)
    ) {
        items(artifacts, key = { it.id }) { artifact ->
            ArtifactRow(
                artifact = artifact,
                selected = artifact.id == selectedId,
                onClick = { onSelect(artifact.id) }
            )
        }
    }
}

@Composable
private fun ArtifactRow(
    artifact: DetectedArtifact,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tone = if (selected) WtaCardTone.Highlighted else WtaCardTone.Surface
    val onTone = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
    val onSubdued = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = WtaAlpha.Strong)
    } else MaterialTheme.colorScheme.onSurfaceVariant

    WtaCard(
        onClick = onClick,
        tone = tone,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = WtaSpacing.Medium,
            vertical = WtaSpacing.Small + 2.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (selected) Icons.Outlined.RadioButtonChecked
                else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(WtaSize.Icon)
            )
            Spacer(Modifier.width(WtaSpacing.Small))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KindChip(artifact.kind)
                    Spacer(Modifier.width(WtaSpacing.Tiny + 2.dp))
                    Text(
                        text = artifact.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onTone,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.size(2.dp))
                Text(
                    text = Strings.aiCodingSaveArtifactSuffix.format(
                        artifact.fileCount,
                        humanSize(artifact.totalSizeBytes)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSubdued
                )
                if (artifact.entryFile.isNotEmpty()) {
                    Text(
                        text = artifact.entryFile,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = onSubdued,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun KindChip(kind: DetectedArtifact.Kind) {

    val (label, container, content) = when (kind) {
        DetectedArtifact.Kind.Html -> Triple(
            Strings.aiCodingKindHtml,
            WtaColors.semantic.infoContainer, WtaColors.semantic.onInfoContainer
        )
        DetectedArtifact.Kind.FrontendReact -> Triple(
            Strings.aiCodingKindReact,
            WtaColors.semantic.infoContainer, WtaColors.semantic.onInfoContainer
        )
        DetectedArtifact.Kind.FrontendVue -> Triple(
            Strings.aiCodingKindVue,
            WtaColors.semantic.successContainer, WtaColors.semantic.onSuccessContainer
        )
        DetectedArtifact.Kind.NodeJs -> Triple(
            Strings.aiCodingKindNodeJs,
            WtaColors.semantic.successContainer, WtaColors.semantic.onSuccessContainer
        )
        DetectedArtifact.Kind.Php -> Triple(
            Strings.aiCodingKindPhp,
            WtaColors.semantic.warningContainer, WtaColors.semantic.onWarningContainer
        )
        DetectedArtifact.Kind.Python -> Triple(
            Strings.aiCodingKindPython,
            WtaColors.semantic.infoContainer, WtaColors.semantic.onInfoContainer
        )
        DetectedArtifact.Kind.Go -> Triple(
            Strings.aiCodingKindGo,
            WtaColors.semantic.infoContainer, WtaColors.semantic.onInfoContainer
        )
        DetectedArtifact.Kind.MultiWeb -> Triple(
            Strings.aiCodingKindMultiWeb,
            WtaColors.semantic.neutralContainer, WtaColors.semantic.onNeutralContainer
        )
        DetectedArtifact.Kind.Gallery -> Triple(
            Strings.aiCodingKindGallery,
            WtaColors.semantic.warningContainer, WtaColors.semantic.onWarningContainer
        )
        DetectedArtifact.Kind.JsModule -> Triple(
            Strings.aiCodingKindJsModule,
            WtaColors.semantic.infoContainer, WtaColors.semantic.onInfoContainer
        )
        DetectedArtifact.Kind.StyleModule -> Triple(
            Strings.aiCodingKindStyleModule,
            WtaColors.semantic.warningContainer, WtaColors.semantic.onWarningContainer
        )
        DetectedArtifact.Kind.UserScript -> Triple(
            Strings.aiCodingKindUserScript,
            WtaColors.semantic.successContainer, WtaColors.semantic.onSuccessContainer
        )
        DetectedArtifact.Kind.ChromeExtension -> Triple(
            Strings.aiCodingKindChromeExtension,
            WtaColors.semantic.neutralContainer, WtaColors.semantic.onNeutralContainer
        )
    }
    WtaInfoChip(
        label = label,
        containerColor = container,
        contentColor = content
    )
}

@Composable
private fun IconSlot(
    iconUri: Uri?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(WtaSize.AvatarLarge)
                .clip(RoundedCornerShape(WtaRadius.IconPlate))
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = WtaAlpha.MutedContainer)
                )
                .clickable(onClick = onPick),
            contentAlignment = Alignment.Center
        ) {
            if (iconUri != null) {
                AsyncImage(
                    model = iconUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(WtaSize.AvatarLarge)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(WtaSize.IconLarge)
                )
            }
        }
        Spacer(Modifier.width(WtaSpacing.Medium))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = Strings.aiCodingSaveAsAppPickIcon,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
                WtaButton(
                    onClick = onPick,
                    text = Strings.aiCodingSaveAsAppChangeIcon,
                    variant = WtaButtonVariant.Tonal,
                    size = WtaButtonSize.Small
                )
                if (iconUri != null) {
                    WtaButton(
                        onClick = onClear,
                        text = Strings.aiCodingActionCancel,
                        variant = WtaButtonVariant.Text,
                        size = WtaButtonSize.Small
                    )
                }
            }
        }
    }
}

private fun subtitleFor(selected: DetectedArtifact?): String {
    if (selected == null) return Strings.aiCodingSaveAsAppDescription
    val target = when (selected.kind.target) {
        DetectedArtifact.Kind.Target.App -> Strings.aiCodingSaveDialogTargetApp
        DetectedArtifact.Kind.Target.Module -> Strings.aiCodingSaveDialogTargetModule
    }

    return "${selected.displayName} → $target"
}

private fun humanSize(bytes: Long): String = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> "${bytes / 1024}KB"
    else -> "${bytes / (1024 * 1024)}MB"
}
