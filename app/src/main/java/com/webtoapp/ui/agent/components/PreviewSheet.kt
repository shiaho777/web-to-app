package com.webtoapp.ui.agent.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.AgentUiState

@Composable
fun PreviewSheet(
    visible: Boolean,
    state: AgentUiState,
    fileManager: ProjectFileManager,
    onDismiss: () -> Unit,
    onSelectFile: (String) -> Unit = {}
) {
    if (!visible) return

    AgentPanel(
        title = Strings.agentPreviewOpen,
        icon = Icons.Outlined.PlayArrow,
        onDismiss = onDismiss,
        closeLabel = Strings.agentPreviewClose,
        heightFraction = 0.94f
    ) {
        PreviewPane(
            state = state,
            fileManager = fileManager,
            onSelectFile = onSelectFile,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}
