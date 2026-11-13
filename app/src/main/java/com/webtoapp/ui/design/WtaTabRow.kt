package com.webtoapp.ui.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

data class WtaTab(
    val label: String,
    val count: Int? = null
)

@Composable
fun WtaTabRow(
    tabs: List<WtaTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tabs.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    val indicatorIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = WtaMotion.settleSpring(),
        label = "wtaTabIndicatorIndex"
    )
    val indicatorColor by animateColorAsState(
        targetValue = colors.surface,
        animationSpec = WtaMotion.standardTween(WtaMotion.DurationMedium),
        label = "wtaTabIndicatorColor"
    )
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WtaRadius.Control))
            .background(colors.surfaceContainerHigh)
    ) {
        val innerPadding = 4.dp
        val tabGap = 4.dp
        val tabWidth = (maxWidth - innerPadding * 2 - tabGap * (tabs.size - 1)) / tabs.size
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .offset(x = (tabWidth + tabGap) * indicatorIndex)
                .width(tabWidth)
                .height(36.dp)
                .clip(RoundedCornerShape(WtaRadius.Control))
                .background(indicatorColor)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            horizontalArrangement = Arrangement.spacedBy(tabGap)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index
                val labelColor by animateColorAsState(
                    targetValue = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                    animationSpec = WtaMotion.standardTween(WtaMotion.DurationMedium),
                    label = "wtaTabLabelColor"
                )
                val badgeContainer by animateColorAsState(
                    targetValue = if (isSelected) colors.onSurface.copy(alpha = 0.08f)
                    else colors.onSurfaceVariant.copy(alpha = 0.10f),
                    animationSpec = WtaMotion.standardTween(WtaMotion.DurationMedium),
                    label = "wtaTabBadgeContainer"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(WtaRadius.Control))
                        .clickable { onTabSelected(index) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tab.label,
                            style = if (isSelected) MaterialTheme.typography.labelLargeEmphasized
                            else MaterialTheme.typography.labelLarge,
                            color = labelColor
                        )
                        if (tab.count != null && tab.count > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            WtaBadge(
                                text = tab.count.toString(),
                                compact = true,
                                containerColor = badgeContainer,
                                contentColor = labelColor
                            )
                        }
                    }
                }
            }
        }
    }
}