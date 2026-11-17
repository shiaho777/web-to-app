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
    val lrcData = bgmState.currentLrcData ?: return
    val line = lrcData.lines.getOrNull(bgmState.currentLrcLineIndex) ?: return
    if (!config.bgmShowLyrics) return
    val lrcTheme = config.bgmLrcTheme
    val bgColor = lrcTheme?.backgroundColor.toLrcColor(Color.Black.copy(alpha = 0.5f))
    val textColor = lrcTheme?.textColor.toLrcColor(Color.White)
    val fontSize = (lrcTheme?.fontSize ?: 18f).sp
    val fontFamily = lrcFontFamily(lrcTheme?.fontFamily)
    val strokeWidth = lrcTheme?.strokeWidth ?: 0f
    val strokeColor = lrcTheme?.strokeColor.toLrcColor(Color.Transparent)
    val shadow = if (lrcTheme?.shadowEnabled != false) {
        androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.8f), blurRadius = 6f)
    } else {
        null
    }
    val translation = line.translation?.takeIf { lrcTheme?.showTranslation != false && it.isNotBlank() }

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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (strokeWidth > 0f && strokeColor != Color.Transparent) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = line.text,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        style = androidx.compose.ui.text.TextStyle(
                            drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth * 2f,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            ),
                            shadow = shadow
                        ),
                        color = strokeColor
                    )
                    Text(
                        text = line.text,
                        color = textColor,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        style = androidx.compose.ui.text.TextStyle(shadow = shadow)
                    )
                }
            } else {
                Text(
                    text = line.text,
                    color = textColor,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    style = androidx.compose.ui.text.TextStyle(shadow = shadow)
                )
            }
            if (translation != null) {
                Text(
                    text = translation,
                    color = textColor.copy(alpha = 0.75f),
                    fontSize = fontSize * 0.8f,
                    fontFamily = fontFamily,
                    style = androidx.compose.ui.text.TextStyle(shadow = shadow)
                )
            }
        }
    }
}

internal fun String?.toLrcColor(fallback: Color): Color = try {
    Color(android.graphics.Color.parseColor(this ?: ""))
} catch (e: Exception) {
    fallback
}

internal fun lrcFontFamily(name: String?): androidx.compose.ui.text.font.FontFamily = when (name?.lowercase()) {
    "serif" -> androidx.compose.ui.text.font.FontFamily.Serif
    "monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
    "cursive" -> androidx.compose.ui.text.font.FontFamily.Cursive
    "sans-serif", "sans_serif" -> androidx.compose.ui.text.font.FontFamily.SansSerif
    else -> androidx.compose.ui.text.font.FontFamily.Default
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
