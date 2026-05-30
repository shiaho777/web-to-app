package com.webtoapp.ui.aicoding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Web
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSize

@Composable
fun MaterialIconBadge(
    name: String?,
    tintHex: String?,
    size: Dp = WtaSize.IconPlate
) {
    IconPlate(
        name = name,
        tintHex = tintHex,
        size = size,
        shape = RoundedCornerShape(WtaRadius.IconPlate)
    )
}

@Composable
fun MaterialIconBadgeRound(
    name: String?,
    tintHex: String?,
    size: Dp = WtaSize.IconPlate
) {
    IconPlate(
        name = name,
        tintHex = tintHex,
        size = size,
        shape = CircleShape
    )
}

@Composable
private fun IconPlate(
    name: String?,
    tintHex: String?,
    size: Dp,
    shape: androidx.compose.ui.graphics.Shape
) {
    val icon = resolveIcon(name)
    val tint = parseHex(tintHex) ?: MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(tint.copy(alpha = WtaAlpha.MutedContainer)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

@Composable
fun MaterialIconGlyph(
    name: String?,
    tintHex: String?,
    size: Dp = WtaSize.Icon
) {
    val icon = resolveIcon(name)
    val tint = parseHex(tintHex) ?: MaterialTheme.colorScheme.primary
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(size)
    )
}

private fun parseHex(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return runCatching {
        Color(android.graphics.Color.parseColor("#${hex.trim('#')}"))
    }.getOrNull()
}

private fun resolveIcon(name: String?): ImageVector = when (name) {
    "code" -> Icons.Outlined.Code
    "extension" -> Icons.Outlined.Extension
    "language" -> Icons.Outlined.Language
    "web" -> Icons.Outlined.Web
    "image" -> Icons.Outlined.Image
    "photo" -> Icons.Outlined.Photo
    "palette" -> Icons.Outlined.Palette
    "style" -> Icons.Outlined.Style
    "data_object" -> Icons.Outlined.DataObject
    "construction" -> Icons.Outlined.Construction
    "fact_check" -> Icons.AutoMirrored.Outlined.FactCheck
    "settings_suggest" -> Icons.Outlined.SettingsSuggest
    "history" -> Icons.Outlined.History
    "folder_open", "folder" -> Icons.Outlined.Folder
    "play_arrow" -> Icons.Outlined.PlayArrow
    "auto_delete" -> Icons.Outlined.SettingsBackupRestore
    "compress" -> Icons.Outlined.Construction
    "logout" -> Icons.Outlined.PlayArrow
    "help" -> Icons.Outlined.QuestionMark
    else -> Icons.Outlined.AutoAwesome
}
