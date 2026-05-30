package com.webtoapp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import kotlin.math.abs

@Composable
fun WtaReorderableUrlList(
    items: List<String>,
    onItemsChange: (List<String>) -> Unit,
    onAddRequest: () -> Unit,
    emptyHint: String,
    addButtonText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        if (items.isEmpty()) {
            Text(
                text = emptyHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = WtaSpacing.Tiny)
            )
        } else {
            ReorderableColumn(
                items = items,
                onMove = { from, to ->
                    if (from == to) return@ReorderableColumn
                    val mutable = items.toMutableList()
                    val item = mutable.removeAt(from)
                    mutable.add(to.coerceIn(0, mutable.size), item)
                    onItemsChange(mutable)
                },
                onDelete = { index ->
                    onItemsChange(items.filterIndexed { i, _ -> i != index })
                }
            )
        }

        WtaCard(
            onClick = onAddRequest,
            tone = WtaCardTone.Surface,
            contentPadding = PaddingValues(
                vertical = WtaSpacing.Medium,
                horizontal = WtaSpacing.Large
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(WtaSize.IconSmall)
                )
                Spacer(Modifier.width(WtaSpacing.Small))
                Text(
                    addButtonText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (items.size >= 2) {
            Text(
                text = Strings.failoverDragHint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = WtaSpacing.Tiny)
            )
        }
    }
}

@Composable
private fun ReorderableColumn(
    items: List<String>,
    onMove: (from: Int, to: Int) -> Unit,
    onDelete: (Int) -> Unit,
) {
    val density = LocalDensity.current

    var rowHeightPx by remember { mutableIntStateOf(0) }

    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }

    val rowSpacingDp = WtaSpacing.Small

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(rowSpacingDp)
    ) {
        items.forEachIndexed { index, url ->
            val isBeingDragged = index == dragIndex
            val elevation by animateDpAsState(
                targetValue = if (isBeingDragged) WtaSize.Icon else 0.dp,
                animationSpec = spring(stiffness = 600f),
                label = "drag-elevation"
            )

            UrlRow(
                url = url,
                isDragging = isBeingDragged,
                rowOffsetY = if (isBeingDragged) dragOffsetPx else 0f,
                onMoveUp = if (index > 0) {
                    { onMove(index, index - 1) }
                } else null,
                onMoveDown = if (index < items.lastIndex) {
                    { onMove(index, index + 1) }
                } else null,
                onDelete = { onDelete(index) },
                onMeasuredHeight = { px -> if (rowHeightPx != px) rowHeightPx = px },
                dragHandleModifier = Modifier.pointerInput(items.size, index) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            dragIndex = index
                            dragOffsetPx = 0f
                        },
                        onDragEnd = {
                            dragIndex = -1
                            dragOffsetPx = 0f
                        },
                        onDragCancel = {
                            dragIndex = -1
                            dragOffsetPx = 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        if (rowHeightPx <= 0) return@detectDragGesturesAfterLongPress

                        dragOffsetPx += dragAmount.y
                        val rowAndGap = rowHeightPx + with(density) { rowSpacingDp.toPx() }

                        val movedRows = (dragOffsetPx / rowAndGap).toInt()
                        if (movedRows != 0) {
                            val current = dragIndex
                            if (current < 0) return@detectDragGesturesAfterLongPress
                            val target = (current + movedRows)
                                .coerceIn(0, items.lastIndex)
                            if (target != current) {
                                onMove(current, target)
                                dragOffsetPx -= movedRows * rowAndGap
                                dragIndex = target
                            }
                        }

                        val clamp = rowAndGap * 0.5f
                        val curIdx = dragIndex
                        if (curIdx == 0 && dragOffsetPx < -clamp) dragOffsetPx = -clamp
                        if (curIdx == items.lastIndex && dragOffsetPx > clamp) {
                            dragOffsetPx = clamp
                        }
                        if (abs(dragOffsetPx) > rowAndGap * 1.5f) {

                            dragOffsetPx = dragOffsetPx.coerceIn(-clamp * 3, clamp * 3)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun UrlRow(
    url: String,
    isDragging: Boolean,
    rowOffsetY: Float,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onDelete: () -> Unit,
    onMeasuredHeight: (Int) -> Unit,
    dragHandleModifier: Modifier,
) {
    val borderColor = if (isDragging) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }
    val tone = if (isDragging) WtaCardTone.Highlighted else WtaCardTone.Surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer { translationY = rowOffsetY }
            .onSizeChanged { onMeasuredHeight(it.height) }
    ) {
        WtaCard(
            tone = tone,
            border = BorderStroke(1.dp, borderColor),
            contentPadding = PaddingValues(
                horizontal = WtaSpacing.Tiny,
                vertical = WtaSpacing.Tiny
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = dragHandleModifier
                        .size(WtaSize.IconPlate)
                        .semantics { contentDescription = Strings.failoverDragHandleDesc },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DragHandle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(WtaSize.Icon)
                    )
                }

                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = WtaSpacing.Tiny)
                )

                IconButton(
                    onClick = { onMoveUp?.invoke() },
                    enabled = onMoveUp != null,
                    modifier = Modifier.size(WtaSize.ButtonHeightSmall)
                ) {
                    Icon(
                        Icons.Outlined.KeyboardArrowUp,
                        contentDescription = Strings.moveUp,
                        modifier = Modifier.size(WtaSize.Icon),
                        tint = if (onMoveUp != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
                }
                IconButton(
                    onClick = { onMoveDown?.invoke() },
                    enabled = onMoveDown != null,
                    modifier = Modifier.size(WtaSize.ButtonHeightSmall)
                ) {
                    Icon(
                        Icons.Outlined.KeyboardArrowDown,
                        contentDescription = Strings.moveDown,
                        modifier = Modifier.size(WtaSize.Icon),
                        tint = if (onMoveDown != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(WtaSize.ButtonHeightSmall)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = Strings.delete,
                        modifier = Modifier.size(WtaSize.IconSmall),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
