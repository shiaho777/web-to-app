package com.webtoapp.ui.aicoding.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.aicoding.files.ProjectFileManager
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.aicoding.AiCodingUiState
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaElevation
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSpacing

@Composable
fun PreviewSheet(
    visible: Boolean,
    state: AiCodingUiState,
    fileManager: ProjectFileManager,
    onDismiss: () -> Unit,

    onSelectFile: (String) -> Unit = {}
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss)
        )
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                topStart = WtaRadius.Dialog,
                topEnd = WtaRadius.Dialog
            ),
            tonalElevation = WtaElevation.Level3
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(WtaSpacing.Small))
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(Modifier.height(WtaSpacing.Tiny))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WtaSpacing.Small,
                            vertical = WtaSpacing.Tiny
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.size(WtaSpacing.Small))
                    Text(
                        text = Strings.aiCodingPreviewOpen,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    WtaIconButton(
                        onClick = onDismiss,
                        icon = Icons.Outlined.Close,
                        contentDescription = Strings.aiCodingPreviewClose
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                        .copy(alpha = WtaAlpha.Divider)
                )
                PreviewPane(
                    state = state,
                    fileManager = fileManager,
                    onSelectFile = onSelectFile,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
