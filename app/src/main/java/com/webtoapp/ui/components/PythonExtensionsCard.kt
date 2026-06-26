package com.webtoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.CustomPythonExtension
import com.webtoapp.ui.screens.create.RuntimeSectionHeader

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PythonExtensionsCard(
    customExtensions: List<CustomPythonExtension> = emptyList(),
    onCustomExtensionsChange: (List<CustomPythonExtension>) -> Unit = {}
) {
    var newExtName by remember { mutableStateOf("") }
    var newExtSoName by remember { mutableStateOf("") }
    var newExtOrder by remember { mutableStateOf("0") }

    EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            RuntimeSectionHeader(
                icon = Icons.Outlined.Extension,
                title = Strings.pythonExtensions,
                brandColor = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(Strings.pythonExtensionsHint, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            if (customExtensions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(Strings.pythonCustomExtensions, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                customExtensions.sortedBy { it.loadOrder }.forEach { ext ->
                    CustomPythonExtensionRow(
                        extension = ext,
                        onEnabledChange = { enabled ->
                            onCustomExtensionsChange(
                                customExtensions.map { if (it.name == ext.name) it.copy(enabled = enabled) else it }
                            )
                        },
                        onRemove = {
                            onCustomExtensionsChange(customExtensions.filter { it.name != ext.name })
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text(Strings.pythonAddCustomExtension, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            PremiumTextField(
                value = newExtName,
                onValueChange = { newExtName = it },
                label = { Text(Strings.pythonCustomExtensionName) },
                placeholder = { Text(Strings.pythonCustomExtensionHint) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            PremiumTextField(
                value = newExtSoName,
                onValueChange = { newExtSoName = it },
                label = { Text(Strings.pythonCustomExtensionSoName) },
                placeholder = { Text(Strings.pythonCustomExtensionHint) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            PremiumTextField(
                value = newExtOrder,
                onValueChange = { newExtOrder = it.filter { c -> c.isDigit() } },
                label = { Text(Strings.pythonCustomExtensionOrder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            PremiumButton(
                onClick = {
                    val name = newExtName.trim()
                    if (name.isNotBlank() && customExtensions.none { it.name == name }) {
                        onCustomExtensionsChange(
                            customExtensions + CustomPythonExtension(
                                name = name,
                                soFileName = newExtSoName.trim(),
                                loadOrder = newExtOrder.toIntOrNull() ?: 0
                            )
                        )
                        newExtName = ""
                        newExtSoName = ""
                        newExtOrder = "0"
                    }
                },
                enabled = newExtName.trim().isNotBlank() && customExtensions.none { it.name == newExtName.trim() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(Strings.pythonAddCustomExtensionButton)
            }
        }
    }
}

@Composable
private fun CustomPythonExtensionRow(
    extension: CustomPythonExtension,
    onEnabledChange: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = extension.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = extension.effectiveSoName(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = extension.enabled,
                onCheckedChange = onEnabledChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = null)
            }
        }
    }
}
