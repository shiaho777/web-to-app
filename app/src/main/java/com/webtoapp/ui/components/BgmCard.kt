package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.animation.CardCollapseTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.BgmConfig
import com.webtoapp.data.model.BgmPlayMode
import com.webtoapp.data.model.PresetLrcThemes
import com.webtoapp.ui.design.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BgmCard(
    enabled: Boolean,
    config: BgmConfig,
    onEnabledChange: (Boolean) -> Unit,
    onConfigChange: (BgmConfig) -> Unit
) {
    var showSelectorDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary

    val playModeLabel = when (config.playMode) {
        BgmPlayMode.LOOP -> Strings.loopMode
        BgmPlayMode.SEQUENTIAL -> Strings.sequentialMode
        BgmPlayMode.SHUFFLE -> Strings.shuffleMode
    }
    val summary = when {
        !enabled -> null
        config.playlist.isEmpty() -> Strings.selectMusic
        else -> Strings.bgmTrackCount.format(config.playlist.size) + " · " + playModeLabel
    }

    EnhancedElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (enabled) primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            tint = if (enabled) primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = Strings.bgmTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (summary != null) {
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                WtaSwitch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }

            AnimatedVisibility(
                visible = enabled,
                enter = CardExpandTransition,
                exit = CardCollapseTransition
            ) {
                Column(
                    modifier = Modifier.padding(top = WtaSpacing.ContentGap),
                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)
                ) {

                    WtaSettingRow(
                        title = Strings.bgmLibraryTitle,
                        subtitle = if (config.playlist.isEmpty()) Strings.selectMusic
                        else config.playlist.take(2).joinToString("、") { it.name } +
                                if (config.playlist.size > 2)
                                    " " + Strings.andMoreTracks.format(config.playlist.size - 2)
                                else "",
                        icon = Icons.Outlined.LibraryMusic,
                        onClick = { showSelectorDialog = true },
                        trailing = {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    // 播放控制块：模式 chips + 音量滑杆
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(WtaRadius.Card),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Strings.playMode,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = primary
                                )
                                FlowRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        WtaSpacing.Small, Alignment.End
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)
                                ) {
                                    WtaChip(
                                        selected = config.playMode == BgmPlayMode.LOOP,
                                        onClick = {
                                            onConfigChange(config.copy(playMode = BgmPlayMode.LOOP))
                                        },
                                        label = Strings.loopMode,
                                        showSelectedCheck = false
                                    )
                                    WtaChip(
                                        selected = config.playMode == BgmPlayMode.SEQUENTIAL,
                                        onClick = {
                                            onConfigChange(config.copy(playMode = BgmPlayMode.SEQUENTIAL))
                                        },
                                        label = Strings.sequentialMode,
                                        showSelectedCheck = false
                                    )
                                    WtaChip(
                                        selected = config.playMode == BgmPlayMode.SHUFFLE,
                                        onClick = {
                                            onConfigChange(config.copy(playMode = BgmPlayMode.SHUFFLE))
                                        },
                                        label = Strings.shuffleMode,
                                        showSelectedCheck = false
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (config.volume > 0.5f) Icons.AutoMirrored.Outlined.VolumeUp
                                    else Icons.AutoMirrored.Outlined.VolumeDown,
                                    contentDescription = Strings.volume,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Slider(
                                    value = config.volume,
                                    onValueChange = { onConfigChange(config.copy(volume = it)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp)
                                )
                                Text(
                                    "${(config.volume * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(36.dp)
                                )
                            }
                        }
                    }

                    WtaSettingRow(
                        title = Strings.autoPlay,
                        icon = Icons.Outlined.PlayCircle,
                        active = config.autoPlay,
                        onClick = { onConfigChange(config.copy(autoPlay = !config.autoPlay)) },
                        trailing = {
                            WtaSwitch(
                                checked = config.autoPlay,
                                onCheckedChange = { onConfigChange(config.copy(autoPlay = it)) }
                            )
                        }
                    )

                    WtaSettingRow(
                        title = Strings.showLyrics,
                        subtitle = if (config.showLyrics)
                            (config.lrcTheme ?: PresetLrcThemes.themes.first()).name
                        else null,
                        icon = Icons.Outlined.Subtitles,
                        active = config.showLyrics,
                        onClick = {
                            if (config.showLyrics) showThemeDialog = true
                            else onConfigChange(
                                config.copy(
                                    showLyrics = true,
                                    lrcTheme = config.lrcTheme ?: PresetLrcThemes.themes.first()
                                )
                            )
                        },
                        trailing = {
                            WtaSwitch(
                                checked = config.showLyrics,
                                onCheckedChange = {
                                    onConfigChange(
                                        config.copy(
                                            showLyrics = it,
                                            lrcTheme = if (it && config.lrcTheme == null)
                                                PresetLrcThemes.themes.first()
                                            else if (!it) null
                                            else config.lrcTheme
                                        )
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    if (showSelectorDialog) {
        BgmSelectorDialog(
            currentConfig = config,
            onDismiss = { showSelectorDialog = false },
            onConfirm = { newConfig ->
                onConfigChange(newConfig)
                showSelectorDialog = false
            }
        )
    }

    if (showThemeDialog) {
        LrcThemeDialog(
            currentTheme = config.lrcTheme ?: PresetLrcThemes.themes.first(),
            onDismiss = { showThemeDialog = false },
            onSelect = { theme ->
                onConfigChange(config.copy(lrcTheme = theme))
                showThemeDialog = false
            }
        )
    }
}
