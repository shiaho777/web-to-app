package com.webtoapp.ui.aicoding.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.aicoding.AiCodingUiState
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing

@Composable
fun AiCodingActionBar(
    state: AiCodingUiState,
    onOpenSkillTab: () -> Unit,
    @Suppress("UNUSED_PARAMETER")
    onClearSkill: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WtaSpacing.ScreenHorizontal,
                    vertical = WtaSpacing.Small
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkillsBrowseChip(
                count = state.skills.size,
                onClick = onOpenSkillTab
            )
        }
    }
}

@Composable
private fun SkillsBrowseChip(count: Int, onClick: () -> Unit) {
    WtaChip(
        selected = false,
        onClick = onClick,
        leadingIcon = Icons.Outlined.AutoAwesome,
        showSelectedCheck = false
    ) {
        Text(
            text = if (count > 0) "${Strings.aiCodingPickSkill} ($count)"
            else Strings.aiCodingPickSkill,
            style = MaterialTheme.typography.labelLarge
        )

        if (count == 0) Modifier.size(WtaSize.IconSmall)
    }
}
