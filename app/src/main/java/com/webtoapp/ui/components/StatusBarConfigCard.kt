package com.webtoapp.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.StatusBarBackgroundType
import com.webtoapp.data.model.StatusBarColorMode
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSpacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StatusBarConfigCard(
    config: WebViewConfig,
    onConfigChange: (WebViewConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val topInsetPx = WindowInsets.statusBars.getTop(density)
    val systemStatusBarHeight = if (topInsetPx > 0) {
        with(density) { topInsetPx.toDp().value.toInt() }
    } else {
        24
    }

    val currentHeightDp = when {
        config.statusBarHeightDp >= 0 -> config.statusBarHeightDp
        else -> systemStatusBarHeight
    }

    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var showCropper by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pendingImageUri = it
            showCropper = true
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = config.statusBarColor,
            onColorSelected = { color ->
                onConfigChange(config.copy(
                    statusBarColorMode = StatusBarColorMode.CUSTOM,
                    statusBarBackgroundType = StatusBarBackgroundType.COLOR,
                    statusBarColor = color
                ))
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showCropper && pendingImageUri != null) {
        StatusBarImageCropper(
            imageUri = pendingImageUri!!,
            statusBarHeightDp = currentHeightDp,
            onCropComplete = { croppedPath ->
                onConfigChange(config.copy(
                    statusBarBackgroundType = StatusBarBackgroundType.IMAGE,
                    statusBarBackgroundImage = croppedPath
                ))
                showCropper = false
                pendingImageUri = null
            },
            onDismiss = {
                showCropper = false
                pendingImageUri = null
            }
        )
    }

    // Zone content: the caller (FullscreenModeCard) already provides the
    // 16dp padded zone, so this column only spaces sections like its
    // neighbours (DnsProviderSection / staticAssetPack block).
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.SectionGap)
    ) {
        StatusBarPreviewBox(
            heightDp = currentHeightDp,
            colorMode = config.statusBarColorMode,
            backgroundType = config.statusBarBackgroundType,
            backgroundColor = config.statusBarColor,
            backgroundImage = config.statusBarBackgroundImage,
            alpha = config.statusBarBackgroundAlpha
        )

        HeightSlider(
            currentHeight = currentHeightDp,
            systemDefaultHeight = systemStatusBarHeight,
            onHeightChange = { onConfigChange(config.copy(statusBarHeightDp = it)) }
        )

        Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
            GroupLabel(Strings.backgroundType)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                WtaChip(
                    selected = config.statusBarBackgroundType == StatusBarBackgroundType.COLOR,
                    onClick = { onConfigChange(config.copy(statusBarBackgroundType = StatusBarBackgroundType.COLOR)) },
                    label = Strings.solidColor,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Palette,
                    showSelectedCheck = false
                )
                WtaChip(
                    selected = config.statusBarBackgroundType == StatusBarBackgroundType.IMAGE,
                    onClick = { onConfigChange(config.copy(statusBarBackgroundType = StatusBarBackgroundType.IMAGE)) },
                    label = Strings.image,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Image,
                    showSelectedCheck = false
                )
            }
        }

        when (config.statusBarBackgroundType) {
            StatusBarBackgroundType.COLOR -> {
                Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
                    GroupLabel(Strings.backgroundColor)
                    // Four modes no longer fit one weighted row in every locale
                    // ("Transparent" overflows a quarter-width chip in English),
                    // so this group wraps like the viewport-mode chips do.
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                    ) {
                        WtaChip(
                            selected = config.statusBarColorMode == StatusBarColorMode.THEME,
                            onClick = { onConfigChange(config.copy(statusBarColorMode = StatusBarColorMode.THEME)) },
                            label = Strings.tagTheme,
                            showSelectedCheck = false
                        )
                        WtaChip(
                            selected = config.statusBarColorMode == StatusBarColorMode.PAGE_TOP,
                            onClick = { onConfigChange(config.copy(statusBarColorMode = StatusBarColorMode.PAGE_TOP)) },
                            label = Strings.followPageTop,
                            showSelectedCheck = false
                        )
                        WtaChip(
                            selected = config.statusBarColorMode == StatusBarColorMode.CUSTOM,
                            onClick = { onConfigChange(config.copy(statusBarColorMode = StatusBarColorMode.CUSTOM)) },
                            label = Strings.backgroundColor,
                            showSelectedCheck = false
                        )
                        WtaChip(
                            selected = config.statusBarColorMode == StatusBarColorMode.TRANSPARENT,
                            onClick = { onConfigChange(config.copy(statusBarColorMode = StatusBarColorMode.TRANSPARENT)) },
                            label = Strings.transparent,
                            showSelectedCheck = false
                        )
                    }

                    if (config.statusBarColorMode == StatusBarColorMode.CUSTOM) {
                        ColorSelectionRow(currentColor = config.statusBarColor, onColorClick = { showColorPicker = true })
                    }
                }
            }
            StatusBarBackgroundType.IMAGE -> {
                ImageSelectionRow(
                    currentImagePath = config.statusBarBackgroundImage,
                    onSelectImage = { imagePickerLauncher.launch("image/*") },
                    onClearImage = { onConfigChange(config.copy(statusBarBackgroundImage = null, statusBarBackgroundType = StatusBarBackgroundType.COLOR)) }
                )
            }
        }

        // Icon shade used to be permanently auto: the model field was plumbed
        // end-to-end but the editor exposed no control, so a wrong auto guess
        // could never be overridden. Tri-state (auto/dark/light) for both tabs —
        // the dark tab maps through the shared slot like the color fields do.
        Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
            GroupLabel(Strings.statusBarIconsLabel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                WtaChip(
                    selected = config.statusBarDarkIcons == null,
                    onClick = { onConfigChange(config.copy(statusBarDarkIcons = null)) },
                    label = Strings.statusBarIconsAuto,
                    modifier = Modifier.weight(1f),
                    showSelectedCheck = false
                )
                WtaChip(
                    selected = config.statusBarDarkIcons == true,
                    onClick = { onConfigChange(config.copy(statusBarDarkIcons = true)) },
                    label = Strings.statusBarIconsDark,
                    modifier = Modifier.weight(1f),
                    showSelectedCheck = false
                )
                WtaChip(
                    selected = config.statusBarDarkIcons == false,
                    onClick = { onConfigChange(config.copy(statusBarDarkIcons = false)) },
                    label = Strings.statusBarIconsLight,
                    modifier = Modifier.weight(1f),
                    showSelectedCheck = false
                )
            }
        }

        AlphaSlider(alpha = config.statusBarBackgroundAlpha, onAlphaChange = { onConfigChange(config.copy(statusBarBackgroundAlpha = it)) })
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun StatusBarPreviewBox(
    heightDp: Int,
    colorMode: StatusBarColorMode,
    backgroundType: StatusBarBackgroundType,
    backgroundColor: String?,
    backgroundImage: String?,
    alpha: Float
) {
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSurface = MaterialTheme.colorScheme.surface
    val bgColor = remember(colorMode, backgroundColor, themePrimary, themeSurface) {
        when (colorMode) {
            StatusBarColorMode.CUSTOM -> backgroundColor?.let { parseColor(it) } ?: Color.Black
            StatusBarColorMode.PAGE_TOP -> backgroundColor?.let { parseColor(it) } ?: themePrimary
            StatusBarColorMode.TRANSPARENT -> Color.Transparent
            StatusBarColorMode.THEME -> themeSurface
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        GroupLabel(Strings.statusBarPreview)

        Box(
            modifier = Modifier.fillMaxWidth().height(heightDp.dp).clip(RoundedCornerShape(WtaRadius.Control))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(WtaRadius.Control))
        ) {
            when (backgroundType) {
                StatusBarBackgroundType.COLOR -> {
                    Box(modifier = Modifier.fillMaxSize().background(bgColor.copy(alpha = alpha)))
                }
                StatusBarBackgroundType.IMAGE -> {
                    if (backgroundImage != null) {
                        AsyncImage(model = backgroundImage, contentDescription = null,
                            modifier = Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha },
                            contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center) {
                            Text(Strings.noImageSelected, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = WtaSpacing.RowHorizontal),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("12:00", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SignalCellularAlt, null, Modifier.size(12.dp), tint = Color.White)
                    Icon(Icons.Default.Wifi, null, Modifier.size(12.dp), tint = Color.White)
                    Icon(Icons.Default.BatteryFull, null, Modifier.size(12.dp), tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun HeightSlider(currentHeight: Int, systemDefaultHeight: Int, onHeightChange: (Int) -> Unit) {
    // Same slider-in-zone pattern as the staticAssetPack block: primary group
    // label plus a raw Slider (WtaSliderRow is a full-bleed row and would
    // double-pad inside the caller's padded zone).
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            GroupLabel(Strings.statusBarHeight)
            Text("${currentHeight}dp", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }

        Slider(value = currentHeight.toFloat(), onValueChange = { onHeightChange(it.toInt()) }, valueRange = 0f..48f, steps = 47, modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0dp", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            TextButton(onClick = { onHeightChange(-1) }, contentPadding = PaddingValues(horizontal = WtaSpacing.Small, vertical = 0.dp)) {
                Text("${Strings.restoreDefault} (${systemDefaultHeight}dp)", fontSize = 12.sp)
            }
            Text("48dp", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ColorSelectionRow(currentColor: String?, onColorClick: () -> Unit) {
    val color = remember(currentColor) { currentColor?.let { parseColor(it) } ?: Color.Black }
    WtaSettingRow(
        title = Strings.backgroundColor,
        subtitle = currentColor?.uppercase() ?: "#000000",
        onClick = onColorClick,
        iconContent = {
            Box(
                modifier = Modifier.size(WtaSpacing.ExtraLarge)
                    .clip(RoundedCornerShape(WtaRadius.Chip))
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(WtaRadius.Chip))
            )
        }
    ) {
        Icon(Icons.Default.Edit, contentDescription = Strings.selectColor, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ImageSelectionRow(currentImagePath: String?, onSelectImage: () -> Unit, onClearImage: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
        if (currentImagePath != null) {
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(WtaRadius.Control)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(WtaRadius.Control)).padding(WtaSpacing.Small),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Medium), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = currentImagePath, contentDescription = null, modifier = Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(WtaRadius.Chip)), contentScale = ContentScale.Crop)
                Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
                    Text(Strings.imageSelected, style = MaterialTheme.typography.bodyMedium)
                    Text(Strings.clickToChangeOrClear, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onSelectImage) { Icon(Icons.Default.Edit, Strings.changeImage) }
                IconButton(onClick = onClearImage) { Icon(Icons.Default.Delete, Strings.clearImage, tint = MaterialTheme.colorScheme.error) }
            }
        } else {
            WtaButton(
                onClick = onSelectImage,
                text = Strings.selectBackgroundImage,
                variant = WtaButtonVariant.Outlined,
                leadingIcon = Icons.Default.AddPhotoAlternate,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AlphaSlider(alpha: Float, onAlphaChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(WtaSpacing.Tiny)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            GroupLabel(Strings.backgroundAlpha)
            Text("${(alpha * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = alpha, onValueChange = onAlphaChange, valueRange = 0f..1f, modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(Strings.transparent, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(Strings.opaque, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
