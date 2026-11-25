package com.webtoapp.ui.shell

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.shell.MultiWebSiteShellConfig
import com.webtoapp.core.webview.MultiWebDrawerButtonPosition
import com.webtoapp.core.webview.MultiWebResumeStore
import kotlin.math.roundToInt

@Composable
internal fun MultiWebDrawerButton(
    site: MultiWebSiteShellConfig?,
    anchor: String,
    store: MultiWebResumeStore,
    appKey: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomStart = anchor.equals(MULTI_WEB_FAB_ANCHOR_BOTTOM_START, ignoreCase = true)
    val positionKey = if (bottomStart) "$appKey/bottomStart" else "$appKey/topEnd"
    val start = if (LocalLayoutDirection.current == LayoutDirection.Ltr) 0f else 1f
    val defaultPosition = if (bottomStart) {
        MultiWebDrawerButtonPosition(start, 1f)
    } else {
        MultiWebDrawerButtonPosition(1f - start, 0f)
    }
    var savedPosition by remember(store, positionKey) { mutableStateOf(store.drawerButtonPosition(positionKey)) }
    val restingPosition by rememberUpdatedState(savedPosition ?: defaultPosition)
    val buttonSize = 44.dp
    val buttonSizePx = with(LocalDensity.current) { buttonSize.roundToPx() }
    val label = Strings.multiWebSiteList

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(12.dp),
        contentAlignment = AbsoluteAlignment.TopLeft
    ) {
        val availableWidth = (constraints.maxWidth - buttonSizePx).coerceAtLeast(0)
        val availableHeight = (constraints.maxHeight - buttonSizePx).coerceAtLeast(0)
        var dragPosition by remember(store, positionKey, availableWidth, availableHeight) {
            mutableStateOf<MultiWebDrawerButtonPosition?>(null)
        }
        Surface(
            onClick = onClick,
            modifier = Modifier
                .absoluteOffset {
                    val position = dragPosition ?: restingPosition
                    IntOffset(
                        (position.horizontal * availableWidth).roundToInt(),
                        (position.vertical * availableHeight).roundToInt()
                    )
                }
                .size(buttonSize)
                .pointerInput(store, positionKey, availableWidth, availableHeight) {
                    detectDragGestures(
                        onDragEnd = {
                            dragPosition?.let { position ->
                                savedPosition = position
                                store.persistDrawerButtonPosition(positionKey, position)
                            }
                            dragPosition = null
                        },
                        onDragCancel = { dragPosition = null }
                    ) { change, drag ->
                        change.consume()
                        val current = dragPosition ?: restingPosition
                        dragPosition = current.movedBy(drag.x, drag.y, availableWidth, availableHeight)
                    }
                }
                .semantics { contentDescription = label },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                val emoji = site?.iconEmoji?.takeIf { it.isNotBlank() }
                if (emoji != null) {
                    Text(emoji, fontSize = 20.sp)
                } else {
                    Icon(
                        Icons.Outlined.GridView,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
