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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.webtoapp.data.model.CustomPhpExtension
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.screens.create.RuntimeSectionHeader

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhpExtensionsCard(
    extensions: Map<String, Boolean>,
    onToggle: (String, Boolean) -> Unit,
    customExtensions: List<CustomPhpExtension> = emptyList(),
    onCustomExtensionsChange: (List<CustomPhpExtension>) -> Unit = {}
) {
    var newExtName by remember { mutableStateOf("") }
    var newExtSoName by remember { mutableStateOf("") }
    var newExtKind by remember { mutableStateOf(CustomPhpExtension.Kind.EXTENSION) }
    var newExtOrder by remember { mutableStateOf("0") }

    EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            RuntimeSectionHeader(
                icon = Icons.Outlined.Extension,
                title = Strings.phpExtensions,
                brandColor = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (extensions.isNotEmpty()) {
                Text(Strings.phpExtensionsHint, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    extensions.entries.forEach { (name, enabled) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name)
                            Switch(
                                checked = enabled,
                                onCheckedChange = { onToggle(name, it) }
                            )
                        }
                    }
                }
            }

            if (customExtensions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(Strings.phpCustomExtensions, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                customExtensions.sortedBy { it.loadOrder }.forEach { ext ->
                    CustomPhpExtensionRow(
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
            Text(Strings.phpAddCustomExtension, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            PremiumTextField(
                value = newExtName,
                onValueChange = { newExtName = it },
                label = { Text(Strings.phpCustomExtensionName) },
                placeholder = { Text(Strings.phpCustomExtensionHint) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            PremiumTextField(
                value = newExtSoName,
                onValueChange = { newExtSoName = it },
                label = { Text(Strings.phpCustomExtensionSoName) },
                placeholder = { Text(Strings.phpCustomExtensionHint) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PremiumTextField(
                    value = newExtOrder,
                    onValueChange = { newExtOrder = it.filter { c -> c.isDigit() } },
                    label = { Text(Strings.phpCustomExtensionOrder) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1.5f)) {
                    CustomPhpExtension.Kind.entries.forEachIndexed { index, kind ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = CustomPhpExtension.Kind.entries.size),
                            onClick = { newExtKind = kind },
                            selected = newExtKind == kind,
                            label = { Text(if (kind == CustomPhpExtension.Kind.EXTENSION) "EXT" else "ZEND") }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            PremiumButton(
                onClick = {
                    val name = newExtName.trim()
                    if (name.isNotBlank() && customExtensions.none { it.name == name }) {
                        onCustomExtensionsChange(
                            customExtensions + CustomPhpExtension(
                                name = name,
                                soFileName = newExtSoName.trim(),
                                kind = newExtKind,
                                loadOrder = newExtOrder.toIntOrNull() ?: 0
                            )
                        )
                        newExtName = ""
                        newExtSoName = ""
                        newExtKind = CustomPhpExtension.Kind.EXTENSION
                        newExtOrder = "0"
                    }
                },
                enabled = newExtName.trim().isNotBlank() && customExtensions.none { it.name == newExtName.trim() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(Strings.phpAddCustomExtensionButton)
            }
        }
    }
}

@Composable
private fun CustomPhpExtensionRow(
    extension: CustomPhpExtension,
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
                text = "${extension.kind.name.lowercase().replaceFirstChar { it.uppercase() }} · ${extension.effectiveSoName()}",
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
