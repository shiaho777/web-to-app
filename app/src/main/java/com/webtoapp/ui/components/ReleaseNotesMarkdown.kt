package com.webtoapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReleaseNotesMarkdown(text: String) {
    val titleColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val lines = remember(text) { text.lines() }
    var inList = false
    Column {
        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trimEnd()
            when {
                line.startsWith("## ") -> {
                    if (inList) Spacer(Modifier.height(4.dp))
                    inList = false
                    if (index > 0) Spacer(Modifier.height(6.dp))
                    Text(
                        line.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.labelLarge,
                        color = titleColor
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    if (!inList && index > 0) Spacer(Modifier.height(2.dp))
                    inList = true
                    Row(modifier = Modifier.padding(start = 4.dp, top = 1.dp)) {
                        Text(
                            "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = mutedColor,
                            modifier = Modifier.width(12.dp)
                        )
                        Text(
                            line.removePrefix("- ").removePrefix("* ").trim(),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor
                        )
                    }
                }
                line.isBlank() -> {
                    inList = false
                }
                else -> {
                    if (inList) Spacer(Modifier.height(4.dp))
                    inList = false
                    Text(
                        line,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                }
            }
        }
    }
}
