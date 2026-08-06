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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.PortConflictMode
import com.webtoapp.ui.design.WtaSwitch

/**
 * Shared "local server port + conflict policy" configuration section for runtime-backed apps
 * (Node.js / PHP / Python / Go / WordPress). Mirrors the section already used by HTML apps so
 * every runtime type exposes the same controls.
 *
 * Semantics (matching HTML):
 * - [port] == 0  → "auto-assign": the runtime asks PortManager for a free port (REASSIGN); the
 *   conflict-mode radio is then irrelevant because a free port never conflicts.
 * - [port] > 0   → fixed port: the conflict mode decides what happens if it is occupied.
 *
 * Defaults passed by callers: port = 0, portConflictMode = AUTO_KILL — the safest combination,
 * since auto-assign never collides and never kills another process.
 *
 * @param title     section heading, e.g. Strings.portConflictTitle or a runtime-specific label.
 * @param showTitle whether to render the heading row (some screens already group under a card).
 */
@Composable
fun RuntimePortConfigSection(
    port: Int,
    portConflictMode: PortConflictMode,
    onPortChange: (Int) -> Unit,
    onPortConflictModeChange: (PortConflictMode) -> Unit,
    modifier: Modifier = Modifier,
    title: String = Strings.portConflictTitle,
    showTitle: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showTitle) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
        }

        // Auto-assign toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.portAutoAssign,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = Strings.portAutoAssignHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            WtaSwitch(
                checked = port == 0,
                onCheckedChange = { auto -> onPortChange(if (auto) 0 else 8080) }
            )
        }

        // Fixed-port input (only visible when auto-assign is off)
        AnimatedVisibility(visible = port > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.portDefaultLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.width(100.dp)
                ) {
                    BasicTextField(
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
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        Text(
            text = Strings.portConflictTitle,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))

        // AUTO_KILL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.portConflictAutoKill,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = Strings.portConflictAutoKillHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = portConflictMode == PortConflictMode.AUTO_KILL,
                onClick = { onPortConflictModeChange(PortConflictMode.AUTO_KILL) }
            )
        }

        Spacer(Modifier.height(4.dp))

        // ALERT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.portConflictAlert,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = Strings.portConflictAlertHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = portConflictMode == PortConflictMode.ALERT,
                onClick = { onPortConflictModeChange(PortConflictMode.ALERT) }
            )
        }
    }
}

/**
 * Conflict-policy selector only (no port input), for screens that already render their own
 * port field (e.g. CreateNodeJsAppScreen's NodeJsPortCard) but still want the shared
 * AUTO_KILL / ALERT radio section.
 */
@Composable
fun RuntimePortConflictSelector(
    portConflictMode: PortConflictMode,
    onPortConflictModeChange: (PortConflictMode) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = Strings.portConflictTitle,
            style = MaterialTheme.typography.labelLarge,
            color = accentColor
        )
        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.portConflictAutoKill,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = Strings.portConflictAutoKillHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = portConflictMode == PortConflictMode.AUTO_KILL,
                onClick = { onPortConflictModeChange(PortConflictMode.AUTO_KILL) }
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.portConflictAlert,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = Strings.portConflictAlertHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = portConflictMode == PortConflictMode.ALERT,
                onClick = { onPortConflictModeChange(PortConflictMode.ALERT) }
            )
        }
    }
}
