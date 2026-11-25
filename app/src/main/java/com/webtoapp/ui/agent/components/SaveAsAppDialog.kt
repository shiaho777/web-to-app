package com.webtoapp.ui.agent.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.webtoapp.core.agent.export.DetectedArtifact
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.ui.theme.LocalShowDescriptions

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
    var name by rememberSaveable(selected?.id) {
        mutableStateOf(selected?.displayName.orEmpty())
    }
    var iconUri by rememberSaveable(selected?.id) { mutableStateOf<Uri?>(null) }

    val iconPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { picked -> picked?.let { iconUri = it } }

    val isAppArtifact = selected?.kind?.target == DetectedArtifact.Kind.Target.App

    val showDescriptions = LocalShowDescriptions.current
    AgentPanel(
        title = if (selected?.kind?.target == DetectedArtifact.Kind.Target.Module) {
            Strings.agentSaveAsModule
        } else {
            Strings.agentSaveAsAppTitle
        },
        icon = Icons.Outlined.Apps,
        onDismiss = onDismiss,
        heightFraction = null,
        dismissEnabled = !inFlight
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
            contentPadding = PaddingValues(bottom = WtaSpacing.Medium)
        ) {
            if (selected == null || showDescriptions) item(key = "description") {
                Text(
                    text = subtitleFor(selected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (artifacts.isNotEmpty()) item(key = "artifact-label") {
                Text(
                    text = Strings.agentSaveDialogChooseLabel,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            items(artifacts, key = { "artifact:${it.id}" }) { artifact ->
                ArtifactRow(
                    artifact = artifact,
                    selected = artifact.id == selected?.id,
                    enabled = !inFlight,
                    onClick = { onSelectArtifact(artifact.id) },
                    modifier = Modifier.animateItem()
                )
            }
            if (isAppArtifact) item(key = "app-options") {
                Column(
                    modifier = Modifier.animateItem(),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                ) {
                    WtaTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = Strings.agentSaveAsAppNameLabel,
                        singleLine = true,
                        enabled = !inFlight,
                        modifier = Modifier.fillMaxWidth()
                    )
                    IconSlot(
                        iconUri = iconUri,
                        onPick = { if (!inFlight) iconPicker.launch("image/*") },
                        onClear = { if (!inFlight) iconUri = null }
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = WtaSpacing.Small),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (inFlight) WtaLoadingIndicator(size = WtaSize.IconSmall)
            WtaButton(
                onClick = { onConfirm(name.trim(), iconUri) },
                enabled = !inFlight && selected != null && (!isAppArtifact || name.isNotBlank()),
                text = Strings.agentSaveAsAppConfirm,
                variant = WtaButtonVariant.Primary,
                size = WtaButtonSize.Medium
            )
        }
    }
}

@Composable
private fun ArtifactRow(
    artifact: DetectedArtifact,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tone = if (selected) WtaCardTone.Elevated else WtaCardTone.Surface
    val colors = MaterialTheme.colorScheme

    WtaCard(
        onClick = onClick,
        enabled = enabled,
        tone = tone,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = WtaSpacing.Medium,
            vertical = WtaSpacing.Small + 2.dp
        ),
        modifier = modifier.fillMaxWidth().semantics { this.selected = selected }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (selected) Icons.Filled.CheckCircle
                else Icons.Outlined.Circle,
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
                        color = colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.size(2.dp))
                Text(
                    text = Strings.agentSaveArtifactSuffix.format(
                        artifact.fileCount,
                        humanSize(artifact.totalSizeBytes)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
                if (artifact.entryFile.isNotEmpty()) {
                    Text(
                        text = artifact.entryFile,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = colors.onSurfaceVariant,
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
            Strings.agentKindHtml,
            WtaColors.semantic.infoContainer, WtaColors.semantic.onInfoContainer
        )
        DetectedArtifact.Kind.FrontendReact -> Triple(
            Strings.agentKindReact,
            WtaColors.semantic.infoContainer, WtaColors.semantic.onInfoContainer
        )
        DetectedArtifact.Kind.FrontendVue -> Triple(
            Strings.agentKindVue,
            WtaColors.semantic.successContainer, WtaColors.semantic.onSuccessContainer
        )
        DetectedArtifact.Kind.NodeJs -> Triple(
            Strings.agentKindNodeJs,
            WtaColors.semantic.successContainer, WtaColors.semantic.onSuccessContainer
        )
        DetectedArtifact.Kind.Php -> Triple(
            Strings.agentKindPhp,
            WtaColors.semantic.neutralContainer, WtaColors.semantic.onNeutralContainer
        )
        DetectedArtifact.Kind.Python -> Triple(
            Strings.agentKindPython,
            WtaColors.semantic.infoContainer, WtaColors.semantic.onInfoContainer
        )
        DetectedArtifact.Kind.Go -> Triple(
            Strings.agentKindGo,
            WtaColors.semantic.infoContainer, WtaColors.semantic.onInfoContainer
        )
        DetectedArtifact.Kind.MultiWeb -> Triple(
            Strings.agentKindMultiWeb,
            WtaColors.semantic.neutralContainer, WtaColors.semantic.onNeutralContainer
        )
        DetectedArtifact.Kind.Gallery -> Triple(
            Strings.agentKindGallery,
            WtaColors.semantic.neutralContainer, WtaColors.semantic.onNeutralContainer
        )
        DetectedArtifact.Kind.JsModule -> Triple(
            Strings.agentKindJsModule,
            WtaColors.semantic.infoContainer, WtaColors.semantic.onInfoContainer
        )
        DetectedArtifact.Kind.StyleModule -> Triple(
            Strings.agentKindStyleModule,
            WtaColors.semantic.neutralContainer, WtaColors.semantic.onNeutralContainer
        )
        DetectedArtifact.Kind.UserScript -> Triple(
            Strings.agentKindUserScript,
            WtaColors.semantic.successContainer, WtaColors.semantic.onSuccessContainer
        )
        DetectedArtifact.Kind.ChromeExtension -> Triple(
            Strings.agentKindChromeExtension,
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
                text = Strings.agentSaveAsAppPickIcon,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
                WtaButton(
                    onClick = onPick,
                    text = Strings.agentSaveAsAppChangeIcon,
                    variant = WtaButtonVariant.Tonal,
                    size = WtaButtonSize.Small
                )
                if (iconUri != null) {
                    WtaButton(
                        onClick = onClear,
                        text = Strings.agentActionCancel,
                        variant = WtaButtonVariant.Text,
                        size = WtaButtonSize.Small
                    )
                }
            }
        }
    }
}

private fun subtitleFor(selected: DetectedArtifact?): String {
    if (selected == null) return Strings.agentSaveAsAppDescription
    val target = when (selected.kind.target) {
        DetectedArtifact.Kind.Target.App -> Strings.agentSaveDialogTargetApp
        DetectedArtifact.Kind.Target.Module -> Strings.agentSaveDialogTargetModule
    }

    return "${selected.displayName} → $target"
}

private fun humanSize(bytes: Long): String = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> "${bytes / 1024}KB"
    else -> "${bytes / (1024 * 1024)}MB"
}
