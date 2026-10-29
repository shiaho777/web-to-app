package com.webtoapp.ui.shell

import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.webtoapp.core.bgm.bgmAnchoredX
import com.webtoapp.core.bgm.bgmClampY
import com.webtoapp.core.i18n.Strings
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private const val IDLE_COLLAPSE_MS = 3500L

/**
 * Screen position of the music ball. Drag reads this object, not a Float
 * captured when the pointer callback was first created.
 */
private class BallSpot {
    var x = 0f
    var y = 0f
    var downX = 0f
    var downY = 0f
    var lastX = 0f
    var lastY = 0f
    var moved = false
}

/**
 * Music ball above the page. It stays a circle until tapped, then opens the
 * track controls on the free side and folds back on its own. Drag tracks
 * [MotionEvent.getRawX] so moving the popup under the finger cannot eat the
 * next delta.
 */
@Composable
fun BoxScope.BgmFloatingPlayer(
    title: String,
    playing: Boolean,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val marginPx = with(density) { 12.dp.toPx() }
    val slopPx = with(density) { 8.dp.toPx() }
    var parent by remember { mutableStateOf(IntSize.Zero) }
    var chip by remember { mutableStateOf(IntSize.Zero) }
    val spot = remember { BallSpot() }
    var x by remember { mutableFloatStateOf(marginPx) }
    var y by remember { mutableFloatStateOf(marginPx) }
    var anchorRight by remember { mutableStateOf(true) }
    var placed by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var idleTick by remember { mutableIntStateOf(0) }

    fun publish(nx: Float, ny: Float) {
        spot.x = nx
        spot.y = ny
        x = nx
        y = ny
    }

    LaunchedEffect(expanded, idleTick) {
        if (!expanded) return@LaunchedEffect
        delay(IDLE_COLLAPSE_MS)
        expanded = false
    }

    LaunchedEffect(parent.width, parent.height, chip.width, chip.height, anchorRight, dragging) {
        if (dragging || parent.width <= 0 || chip.width <= 0) return@LaunchedEffect
        val targetX = bgmAnchoredX(anchorRight, chip.width.toFloat(), parent.width.toFloat(), marginPx)
        val targetY = if (!placed) {
            bgmClampY(marginPx * 6f, chip.height.toFloat(), parent.height.toFloat(), marginPx)
        } else {
            bgmClampY(spot.y, chip.height.toFloat(), parent.height.toFloat(), marginPx)
        }
        placed = true
        publish(targetX, targetY)
    }

    val moveBy = rememberUpdatedState<(Float, Float) -> Unit> { dx, dy ->
        val maxX = (parent.width - chip.width).toFloat().coerceAtLeast(0f)
        val maxY = (parent.height - chip.height).toFloat().coerceAtLeast(0f)
        val nx = if (parent.width > 0) (spot.x + dx).coerceIn(0f, maxX) else spot.x + dx
        val ny = if (parent.height > 0) (spot.y + dy).coerceIn(0f, maxY) else spot.y + dy
        publish(nx, ny)
    }
    val finishDrag = rememberUpdatedState<(Boolean) -> Unit> { moved ->
        dragging = false
        if (!moved) {
            expanded = !expanded
            idleTick++
            return@rememberUpdatedState
        }
        val parentWidth = parent.width.toFloat()
        anchorRight = parentWidth <= 0f || spot.x + chip.width / 2f >= parentWidth / 2f
    }

    Box(
        modifier = Modifier
            .matchParentSize()
            .onSizeChanged { parent = it }
    )
    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(x.roundToInt(), y.roundToInt()),
        properties = PopupProperties(
            focusable = false,
            clippingEnabled = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.onSizeChanged { chip = it },
            shape = if (expanded) RoundedCornerShape(24.dp) else CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 6.dp,
            tonalElevation = 2.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (expanded && anchorRight) {
                    TrackControls(
                        title = title,
                        playing = playing,
                        onToggle = { idleTick++; onToggle() },
                        onPrevious = { idleTick++; onPrevious() },
                        onNext = { idleTick++; onNext() },
                        onClose = {
                            expanded = false
                            onClose()
                        }
                    )
                }
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = Strings.bgmFloatingPlayer,
                    tint = if (playing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .pointerInteropFilter { event ->
                            when (event.actionMasked) {
                                MotionEvent.ACTION_DOWN -> {
                                    dragging = true
                                    spot.moved = false
                                    spot.downX = event.rawX
                                    spot.downY = event.rawY
                                    spot.lastX = event.rawX
                                    spot.lastY = event.rawY
                                    true
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    val dx = event.rawX - spot.lastX
                                    val dy = event.rawY - spot.lastY
                                    spot.lastX = event.rawX
                                    spot.lastY = event.rawY
                                    if (abs(event.rawX - spot.downX) + abs(event.rawY - spot.downY) > slopPx) {
                                        spot.moved = true
                                    }
                                    moveBy.value(dx, dy)
                                    true
                                }
                                MotionEvent.ACTION_UP -> {
                                    val moved = spot.moved
                                    spot.moved = false
                                    finishDrag.value(moved)
                                    true
                                }
                                MotionEvent.ACTION_CANCEL -> {
                                    spot.moved = false
                                    finishDrag.value(true)
                                    true
                                }
                                else -> false
                            }
                        }
                        .padding(12.dp)
                        .size(24.dp)
                )
                if (expanded && !anchorRight) {
                    TrackControls(
                        title = title,
                        playing = playing,
                        onToggle = { idleTick++; onToggle() },
                        onPrevious = { idleTick++; onPrevious() },
                        onNext = { idleTick++; onNext() },
                        onClose = {
                            expanded = false
                            onClose()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackControls(
    title: String,
    playing: Boolean,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title.ifBlank { Strings.bgmTitle },
            modifier = Modifier
                .padding(start = 4.dp)
                .widthIn(max = 96.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.width(2.dp))
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
        IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Close, Strings.close, modifier = Modifier.size(18.dp))
        }
    }
}
