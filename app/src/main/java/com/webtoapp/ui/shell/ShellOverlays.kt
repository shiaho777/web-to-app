package com.webtoapp.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.i18n.Strings

@Composable
fun BoxScope.ShellLyricsOverlay(
    config: ShellConfig,
    bgmState: BgmPlayerState
) {
    if (config.bgmShowLyrics && bgmState.currentLrcData != null && bgmState.currentLrcLineIndex >= 0) {
        val lrcTheme = config.bgmLrcTheme
        val bgColor = try {
            Color(android.graphics.Color.parseColor(lrcTheme?.backgroundColor ?: "#80000000"))
        } catch (e: Exception) {
            Color.Black.copy(alpha = 0.5f)
        }
        val textColor = try {
            Color(android.graphics.Color.parseColor(lrcTheme?.highlightColor ?: "#FFD700"))
        } catch (e: Exception) {
            Color.Yellow
        }

        Box(
            modifier = Modifier
                .align(
                    when (lrcTheme?.position) {
                        "TOP" -> Alignment.TopCenter
                        "CENTER" -> Alignment.Center
                        else -> Alignment.BottomCenter
                    }
                )
                .padding(16.dp)
                .background(bgColor, shape = MaterialTheme.shapes.medium)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bgmState.currentLrcData!!.lines[bgmState.currentLrcLineIndex].text,
                color = textColor,
                fontSize = (lrcTheme?.fontSize ?: 16f).sp
            )
        }
    }
}

@Composable
fun BoxScope.ShellErrorCard(
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    errorMessage?.let { error ->
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Error,
                    null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(error, modifier = Modifier.weight(weight = 1f, fill = true))
                TextButton(onClick = onDismiss) {
                    Text(Strings.cdClose)
                }
            }
        }
    }
}
