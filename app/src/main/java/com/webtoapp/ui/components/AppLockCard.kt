package com.webtoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.AppLockConfig

/**
 * Editor card configuring the generated app's PIN lock.
 */
@Composable
fun AppLockCard(
    config: AppLockConfig?,
    onConfigChange: (AppLockConfig) -> Unit
) {
    val current = config ?: AppLockConfig()
    var pinInput by remember(current.pin) { mutableStateOf(current.pin) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.appLockSettingTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = Strings.appLockSettingHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = current.enabled,
                    onCheckedChange = { checked ->
                        onConfigChange(current.copy(enabled = checked))
                    }
                )
            }

            if (current.enabled) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { new ->
                        if (new.length <= 6 && new.all { it.isDigit() }) {
                            pinInput = new
                            onConfigChange(current.copy(pin = new))
                        }
                    },
                    label = { Text(Strings.appLockPinLabel) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = Strings.appLockDelayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = listOf(0, 60, 300)
                    options.forEach { seconds ->
                        FilterChip(
                            selected = current.lockDelaySeconds == seconds,
                            onClick = { onConfigChange(current.copy(lockDelaySeconds = seconds)) },
                            label = {
                                Text(
                                    when (seconds) {
                                        0 -> Strings.appLockDelayImmediately
                                        60 -> Strings.appLockDelay1Minute
                                        else -> Strings.appLockDelay5Minutes
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
