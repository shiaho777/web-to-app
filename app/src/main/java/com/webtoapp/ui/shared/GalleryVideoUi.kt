package com.webtoapp.ui.shared

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger

private val gallerySpeeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

internal fun MediaPlayer.applyGalleryPlaybackSpeed(speed: Float) {
    runCatching {
        val params = playbackParams
        if (params.speed != speed) playbackParams = params.setSpeed(speed)
    }.onFailure { AppLogger.w("Gallery", "Unable to change video speed", it) }
}

@Composable
internal fun BoxScope.GalleryVideoTitle(name: String) {
    if (name.isNotBlank()) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

@Composable
internal fun GalleryPlaybackSpeedMenu(
    speed: Float,
    onSelect: (Float) -> Unit,
    onExpandedChange: (Boolean) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val onMenuChange by rememberUpdatedState(onExpandedChange)
    val label = Strings.mediaPlaybackSpeed
    val setExpanded: (Boolean) -> Unit = { open -> expanded = open; onMenuChange(open) }
    DisposableEffect(Unit) {
        onDispose { if (expanded) onMenuChange(false) }
    }
    Box {
        TextButton(
            onClick = { setExpanded(true) },
            modifier = Modifier.semantics { contentDescription = label }
        ) { Text("${speed}×", color = Color.White) }
        DropdownMenu(expanded = expanded, onDismissRequest = { setExpanded(false) }) {
            gallerySpeeds.forEach { value ->
                DropdownMenuItem(
                    text = { Text("${value}×") },
                    onClick = { onSelect(value); setExpanded(false) }
                )
            }
        }
    }
}
