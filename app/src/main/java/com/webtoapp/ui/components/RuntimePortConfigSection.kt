package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.PortConflictMode
import com.webtoapp.ui.design.WtaSwitch

/**
 * Shared "local server + port + conflict policy" card for runtime-backed apps
 * (Node.js / PHP / Python / Go / WordPress). Mirrors the controls HTML apps already have,
 * wrapped in the project's standard [EnhancedElevatedCard] + [RuntimeSectionHeader] style so it
 * blends with the neighbouring cards ([RuntimeEnvVarsCard], [PhpExtensionsCard], …).
 *
 * Semantics (matching HTML):
 * - [port] == 0  → auto-assign: the runtime asks PortManager for a free port (REASSIGN); the
 *   conflict-policy segmented control is then irrelevant because a free port never conflicts.
 * - [port] > 0   → fixed port: the conflict mode decides what happens if it is occupied.
 *
 * Callers pass the safest defaults — port = 0, portConflictMode = AUTO_KILL — because
 * auto-assign never collides and never kills another process.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuntimePortConfigSection(
    port: Int,
    portConflictMode: PortConflictMode,
    onPortChange: (Int) -> Unit,
    onPortConflictModeChange: (PortConflictMode) -> Unit,
    modifier: Modifier = Modifier
) {
    EnhancedElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            RuntimeSectionHeader(
                icon = Icons.Outlined.Lan,
                title = Strings.portConfigTitle
            )
            Spacer(Modifier.height(16.dp))

            // Auto-assign toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.portAutoAssign,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = Strings.portAutoAssignHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                WtaSwitch(
                    checked = port == 0,
                    onCheckedChange = { auto -> onPortChange(if (auto) 0 else 8080) }
                )
            }

            // Fixed-port input (only visible when auto-assign is off)
            AnimatedVisibility(visible = port > 0) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = if (port > 0) port.toString() else "",
                        onValueChange = { text ->
                            val num = text.toIntOrNull()
                            onPortChange(
                                when {
                                    num != null && num in 1024..65535 -> num
                                    text.isEmpty() -> 0
                                    else -> port
                                }
                            )
                        },
                        label = { Text(Strings.portDefaultLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Conflict-policy segmented control (only meaningful for a fixed port)
            Text(
                text = Strings.portConflictTitle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            PortConflictSegmentedRow(
                mode = portConflictMode,
                onModeChange = onPortConflictModeChange,
                enabled = port > 0
            )
        }
    }
}

/**
 * Conflict-policy selector card only (no port input), for screens that already render their own
 * port field (e.g. CreateNodeJsAppScreen's NodeJsPortCard) but still want the shared policy
 * control in the matching card style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuntimePortConflictSelector(
    portConflictMode: PortConflictMode,
    onPortConflictModeChange: (PortConflictMode) -> Unit,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") accentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    EnhancedElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            RuntimeSectionHeader(
                icon = Icons.Outlined.Shield,
                title = Strings.portConflictTitle
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = Strings.portConflictAutoKillHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            PortConflictSegmentedRow(
                mode = portConflictMode,
                onModeChange = onPortConflictModeChange,
                enabled = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortConflictSegmentedRow(
    mode: PortConflictMode,
    onModeChange: (PortConflictMode) -> Unit,
    enabled: Boolean
) {
    val options = listOf(
        PortConflictMode.AUTO_KILL to Strings.portConflictAutoKill,
        PortConflictMode.ALERT to Strings.portConflictAlert
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = mode == value,
                onClick = { onModeChange(value) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(label)
            }
        }
    }
}
