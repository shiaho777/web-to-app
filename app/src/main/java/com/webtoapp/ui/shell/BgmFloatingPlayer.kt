package com.webtoapp.ui.shell

import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.webtoapp.core.bgm.bgmClampY
import com.webtoapp.core.bgm.bgmDockX
import com.webtoapp.core.i18n.Strings
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Small music chip. Dragging the title snaps it to the nearer side of the screen.
 * Play, pause, previous, and next stay on the chip so the page can stay underneath.
 */
@Composable
fun BoxScope.BgmFloatingPlayer(
    title: String,
    playing: Boolean,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val marginPx = with(LocalDensity.current) { 12.dp.toPx() }
    var parent by remember { mutableStateOf(IntSize.Zero) }
    var chip by remember { mutableStateOf(IntSize.Zero) }
    var x by remember { mutableFloatStateOf(marginPx) }
    var y by remember { mutableFloatStateOf(marginPx) }
    var placed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var settleJob by remember { mutableStateOf<Job?>(null) }

    Box(
        modifier = Modifier
            .matchParentSize()
            .onSizeChanged { parent = it }
            .zIndex(6f)
    ) {
        Surface(
            modifier = Modifier
                .onSizeChanged { size ->
                    chip = size
                    if (!placed && parent.width > 0 && size.width > 0) {
                        placed = true
                        x = bgmDockX(
                            currentX = parent.width - size.width - marginPx,
                            chipWidth = size.width.toFloat(),
                            parentWidth = parent.width.toFloat(),
                            margin = marginPx
                        )
                        y = bgmClampY(
                            marginPx * 6f,
                            size.height.toFloat(),
                            parent.height.toFloat(),
                            marginPx
                        )
                    }
                }
                .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                .widthIn(max = 260.dp)
                .graphicsLayer(alpha = if (placed) 1f else 0f),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 6.dp,
            tonalElevation = 3.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { settleJob?.cancel() },
                                onDragEnd = {
                                    settleJob = scope.launch {
                                        val dock = bgmDockX(
                                            x,
                                            chip.width.toFloat(),
                                            parent.width.toFloat(),
                                            marginPx
                                        )
                                        val clamp = bgmClampY(
                                            y,
                                            chip.height.toFloat(),
                                            parent.height.toFloat(),
                                            marginPx
                                        )
                                        coroutineScope {
                                            launch {
                                                animate(initialValue = x, targetValue = dock) { value, _ ->
                                                    x = value
                                                }
                                            }
                                            launch {
                                                animate(initialValue = y, targetValue = clamp) { value, _ ->
                                                    y = value
                                                }
                                            }
                                        }
                                    }
                                },
                                onDrag = { change, drag ->
                                    change.consume()
                                    val maxX = (parent.width - chip.width).toFloat().coerceAtLeast(0f)
                                    val maxY = (parent.height - chip.height).toFloat().coerceAtLeast(0f)
                                    x = (x + drag.x).coerceIn(0f, maxX)
                                    y = (y + drag.y).coerceIn(0f, maxY)
                                }
                            )
                        }
                        .padding(start = 10.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = title.ifBlank { Strings.bgmTitle },
                        modifier = Modifier.widthIn(max = 120.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.SkipPrevious, Strings.bgmPrevious, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        if (playing) Strings.pause else Strings.play,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.SkipNext, Strings.bgmNext, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
