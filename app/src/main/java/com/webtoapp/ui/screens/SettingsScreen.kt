package com.webtoapp.ui.screens

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaSection
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSettingCard
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaToggleRow
import com.webtoapp.ui.theme.AppAccent
import com.webtoapp.ui.theme.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    val accent by themeManager.accentFlow.collectAsStateWithLifecycle()
    val darkMode by themeManager.darkModeFlow.collectAsStateWithLifecycle()
    val fontSize by themeManager.fontSizeFlow.collectAsStateWithLifecycle()
    val cornerStyle by themeManager.cornerStyleFlow.collectAsStateWithLifecycle()
    val enableAnimations by themeManager.enableAnimationsFlow.collectAsStateWithLifecycle()
    val enableParticles by themeManager.enableParticlesFlow.collectAsStateWithLifecycle()
    val enableHaptics by themeManager.enableHapticsFlow.collectAsStateWithLifecycle()
    val animSpeed by themeManager.animationSpeedFlow.collectAsStateWithLifecycle()

    val accentOptions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AppAccent.entries.toList()
        } else {
            AppAccent.entries.filter { it != AppAccent.DYNAMIC }
        }
    }

    WtaScreen(title = Strings.uiConfig, onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = WtaSpacing.ScreenHorizontal,
                    vertical = WtaSpacing.ScreenVertical
                ),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.SectionGap)
        ) {

            WtaSection(
                title = Strings.settingsThemeColor,
                description = Strings.settingsThemeColorDesc
            ) {
                WtaSettingCard {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = WtaSpacing.RowHorizontal,
                                vertical = WtaSpacing.RowVertical
                            ),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Medium),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Medium)
                    ) {
                        accentOptions.forEach { option ->
                            AccentSwatch(
                                accent = option,
                                selected = accent == option,
                                onClick = {
                                    scope.launch { themeManager.setAccent(option) }
                                }
                            )
                        }
                    }
                }
            }

            WtaSection(title = Strings.settingsDarkMode) {
                WtaSettingCard {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = WtaSpacing.RowHorizontal,
                                vertical = WtaSpacing.RowVertical
                            ),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        ThemeManager.DarkModeSettings.entries.forEach { mode ->
                            WtaChip(
                                selected = darkMode == mode,
                                onClick = {
                                    scope.launch { themeManager.setDarkMode(mode) }
                                },
                                label = mode.getDisplayName()
                            )
                        }
                    }
                }
            }

            WtaSection(title = Strings.settingsInterface) {
                WtaSettingCard {
                    WtaSettingRow(title = Strings.fontSizeTitle) {
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = WtaSpacing.RowHorizontal,
                                vertical = WtaSpacing.Small
                            ),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        ThemeManager.FontSize.entries.forEach { size ->
                            WtaChip(
                                selected = fontSize == size,
                                onClick = {
                                    scope.launch { themeManager.setFontSize(size) }
                                },
                                label = size.getDisplayName()
                            )
                        }
                    }
                    WtaSectionDivider()
                    WtaSettingRow(title = Strings.cornerStyleTitle) {
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = WtaSpacing.RowHorizontal,
                                vertical = WtaSpacing.Small
                            ),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        ThemeManager.CornerStyle.entries.forEach { style ->
                            WtaChip(
                                selected = cornerStyle == style,
                                onClick = {
                                    scope.launch { themeManager.setCornerStyle(style) }
                                },
                                label = style.getDisplayName()
                            )
                        }
                    }
                }
            }

            WtaSection(title = Strings.settingsAnimation) {
                WtaSettingCard {
                    WtaToggleRow(
                        title = Strings.animEnableAll,
                        checked = enableAnimations,
                        onCheckedChange = {
                            scope.launch { themeManager.setEnableAnimations(it) }
                        }
                    )
                    WtaSectionDivider()
                    WtaSettingRow(
                        title = Strings.animSpeedTitle,
                        enabled = enableAnimations
                    ) {
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = WtaSpacing.RowHorizontal,
                                vertical = WtaSpacing.Small
                            ),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        ThemeManager.AnimationSpeed.entries.forEach { speed ->
                            WtaChip(
                                selected = animSpeed == speed,
                                onClick = {
                                    scope.launch { themeManager.setAnimationSpeed(speed) }
                                },
                                label = speed.getDisplayName(),
                                enabled = enableAnimations
                            )
                        }
                    }
                    WtaSectionDivider()
                    WtaToggleRow(
                        title = Strings.animParticles,
                        checked = enableParticles,
                        enabled = enableAnimations,
                        onCheckedChange = {
                            scope.launch { themeManager.setEnableParticles(it) }
                        }
                    )
                    WtaSectionDivider()
                    WtaToggleRow(
                        title = Strings.animHaptics,
                        checked = enableHaptics,
                        enabled = enableAnimations,
                        onCheckedChange = {
                            scope.launch { themeManager.setEnableHaptics(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccentSwatch(
    accent: AppAccent,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ringColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = WtaSpacing.Tiny),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .then(
                        if (selected) {
                            Modifier.border(2.5.dp, ringColor, CircleShape)
                        } else {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        }
                    )
            ) {
                drawArc(
                    color = accent.swatchPrimary,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = true
                )
                drawArc(
                    color = accent.swatchSecondary,
                    startAngle = 90f,
                    sweepAngle = 180f,
                    useCenter = true
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Color.Black.copy(alpha = 0.45f))
                    }
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(WtaSpacing.Tiny))
        Text(
            text = accent.getDisplayName(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
