package com.webtoapp.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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

/**
 * Full-screen PIN gate shown when the generated app's App Lock is enabled.
 * The expected PIN comes from the packaged config (set by the packager);
 * it is only ever compared in memory here.
 */
@Composable
fun AppLockScreen(
    appName: String,
    expectedPin: String,
    onUnlock: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = appName.ifBlank { Strings.appLockTitle },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Strings.appLockTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { new ->
                    if (new.length <= 6 && new.all { it.isDigit() }) {
                        input = new
                        error = false
                    }
                },
                label = { Text(Strings.appLockPinHint) },
                singleLine = true,
                isError = error,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )
            if (error) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Strings.appLockWrongPin,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (expectedPin.isNotEmpty() && input == expectedPin) {
                        onUnlock()
                    } else {
                        error = true
                        input = ""
                    }
                },
                enabled = input.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(Strings.appLockUnlockButton)
            }
        }
    }
}
