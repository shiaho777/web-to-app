package com.webtoapp.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WtaRadius.Control))
            .background(colors.surfaceContainerHighest)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(WtaRadius.Control))
                        .background(
                            if (isSelected) colors.secondaryContainer
                            else colors.surfaceContainerHighest
                        )
                        .clickable { onTabSelected(index) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) colors.onSecondaryContainer else colors.onSurfaceVariant
                        )
                        if (tab.count != null && tab.count > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            WtaBadge(
                                text = tab.count.toString(),
                                compact = true,
                                containerColor = if (isSelected) colors.primary.copy(alpha = 0.14f)
                                else colors.onSurfaceVariant.copy(alpha = 0.10f),
                                contentColor = if (isSelected) colors.primary else colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
