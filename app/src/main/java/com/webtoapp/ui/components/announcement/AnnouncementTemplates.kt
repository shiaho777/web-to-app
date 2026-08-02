package com.webtoapp.ui.components.announcement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.toArgb
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.AnnouncementTemplateType
import com.webtoapp.ui.design.WtaSpacing

enum class AnnouncementTemplate(
    val type: AnnouncementTemplateType,
    val icon: ImageVector
) {
    MINIMAL(AnnouncementTemplateType.MINIMAL, Icons.Outlined.CropSquare),
    DARK(AnnouncementTemplateType.DARK, Icons.Filled.DarkMode)
}

data class AnnouncementConfig(
    val announcement: Announcement,
    val template: AnnouncementTemplate = AnnouncementTemplate.MINIMAL,
    val primaryColor: Color = Color(0xFF4F46E5),
    val customIconBitmap: android.graphics.Bitmap? = null
)

private val announcementStyleTemplates = listOf(
    AnnouncementTemplate.MINIMAL,
    AnnouncementTemplate.DARK
)

fun AnnouncementTemplateType.toUiTemplate(): AnnouncementTemplate = when (this) {
    AnnouncementTemplateType.DARK,
    AnnouncementTemplateType.NEON -> AnnouncementTemplate.DARK
    else -> AnnouncementTemplate.MINIMAL
}

fun AnnouncementTemplate.toStoredTemplate(): AnnouncementTemplateType = type

fun AnnouncementTemplate.getLocalizedDisplayName(): String = when (this) {
    AnnouncementTemplate.MINIMAL -> Strings.announcementStyleClean
    AnnouncementTemplate.DARK -> Strings.announcementStyleDark
}

fun AnnouncementTemplate.getLocalizedDescription(): String = when (this) {
    AnnouncementTemplate.MINIMAL -> Strings.announcementStyleCleanDesc
    AnnouncementTemplate.DARK -> Strings.announcementStyleDarkDesc
}

fun AnnouncementTemplate.isSelectableStyle(): Boolean = this in announcementStyleTemplates

private fun styleAccentColor(template: AnnouncementTemplate): Color = when (template) {
    AnnouncementTemplate.MINIMAL -> Color(0xFF475569)
    AnnouncementTemplate.DARK -> Color(0xFFCBD5E1)
}

private fun styleSurfaceColor(template: AnnouncementTemplate): Color = when (template) {
    AnnouncementTemplate.MINIMAL -> Color(0xFFF8FAFC)
    AnnouncementTemplate.DARK -> Color(0xFF15161C)
}

private fun styleBodyColor(template: AnnouncementTemplate): Color = when (template) {
    AnnouncementTemplate.MINIMAL -> Color(0xFF1F2937)
    AnnouncementTemplate.DARK -> Color(0xFFE5E7EB)
}

private fun styleBadgeColor(template: AnnouncementTemplate): Color = when (template) {
    AnnouncementTemplate.MINIMAL -> Color(0xFFE2E8F0)
    AnnouncementTemplate.DARK -> Color(0xFF2A2D36)
}

private fun Announcement.linkUrlOrNull(): String? = linkUrl?.takeIf { it.isNotBlank() }

private fun Announcement.linkTextOrDefault(defaultText: String = Strings.viewDetails): String =
    linkText?.ifEmpty { defaultText } ?: defaultText

/**
 * 公告内容区。根据 [Announcement.contentIsHtml] 决定渲染方式：
 *  - false（默认）：纯文本 [Text]，保持原有模板样式行为。
 *  - true：把 content 作为 HTML 嵌入一个轻量 WebView（禁用 JS，仅富文本展示），
 *    背景透明以贴合弹窗模板配色，文字默认色跟随当前模板 [textColor]。
 * 外框（标题、关闭按钮、底部按钮）由各 Dialog 自行负责，本组件只渲染"内容"。
 */
@Composable
private fun AnnouncementContent(
    announcement: Announcement,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    if (!announcement.contentIsHtml) {
        Text(
            text = announcement.content,
            modifier = modifier,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            lineHeight = 24.sp
        )
        return
    }

    val textCss = String.format("#%06X", 0xFFFFFF and textColor.toArgb())
    val html = remember(announcement.content, textCss) {
        """
        <!DOCTYPE html><html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
          html,body{margin:0;padding:0;background:transparent;
            color:$textCss;
            font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;
            font-size:16px;line-height:1.5;word-wrap:break-word;overflow-wrap:break-word;}
          img,video,iframe{max-width:100%;height:auto;}
          a{color:inherit;}
          table{max-width:100%;}
        </style></head>
        <body>${announcement.content}</body></html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            android.webkit.WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.javaScriptEnabled = false
                settings.domStorageEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                @Suppress("DEPRECATION")
                settings.allowFileAccessFromFileURLs = false
                @Suppress("DEPRECATION")
                settings.allowUniversalAccessFromFileURLs = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = false
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }
    )
}

@Composable
fun AnnouncementTemplateSelector(
    selectedTemplate: AnnouncementTemplate,
    onTemplateSelected: (AnnouncementTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = Strings.selectAnnouncementStyle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = WtaSpacing.ContentGap)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            announcementStyleTemplates.forEach { template ->
                StyleCard(
                    template = template,
                    isSelected = template == selectedTemplate,
                    onClick = { onTemplateSelected(template) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StyleCard(
    template: AnnouncementTemplate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = when (template) {
        AnnouncementTemplate.MINIMAL -> listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
        AnnouncementTemplate.DARK -> listOf(Color(0xFF1C1D25), Color(0xFF101116))
    }
    val accent = styleAccentColor(template)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) accent else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                )
                .background(Brush.linearGradient(colors))
                .clickable(onClick = onClick)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (template) {
                        AnnouncementTemplate.MINIMAL -> "◻"
                        AnnouncementTemplate.DARK -> "◐"
                    },
                    fontSize = 20.sp,
                    color = accent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(accent.copy(alpha = 0.45f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(58.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(accent.copy(alpha = 0.18f))
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = template.getLocalizedDisplayName(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AnnouncementIconPlate(
    config: AnnouncementConfig,
    defaultIcon: ImageVector,
    iconTint: Color,
    plateBackground: Color
) {
    if (!config.announcement.showIcon) return
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(plateBackground),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = config.customIconBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(defaultIcon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun AnnouncementDialog(
    config: AnnouncementConfig,
    onDismiss: () -> Unit,
    onLinkClick: ((String) -> Unit)? = null,
    onNeverShowChecked: ((Boolean) -> Unit)? = null
) {
    val style = config.template
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        when (style) {
            AnnouncementTemplate.MINIMAL -> SimpleDialog(config, style, onDismiss, onLinkClick, onNeverShowChecked)
            AnnouncementTemplate.DARK -> DarkDialog(config, style, onDismiss, onLinkClick, onNeverShowChecked)
        }
    }
}

@Composable
private fun DialogFooter(
    linkText: String?,
    onLinkClick: ((String) -> Unit)?,
    linkUrl: String?,
    onDismiss: () -> Unit,
    confirmText: String = Strings.btnConfirm
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (onLinkClick != null && linkUrl != null) {
            OutlinedButton(onClick = { onLinkClick(linkUrl) }, modifier = Modifier.fillMaxWidth()) {
                Text(linkText ?: Strings.viewDetails)
            }
        }
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(confirmText)
        }
    }
}

@Composable
private fun SimpleDialog(
    config: AnnouncementConfig,
    style: AnnouncementTemplate,
    onDismiss: () -> Unit,
    onLinkClick: ((String) -> Unit)?,
    onNeverShowChecked: ((Boolean) -> Unit)?
) {
    val linkUrl = config.announcement.linkUrlOrNull()
    Card(
        modifier = Modifier.fillMaxWidth(0.90f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = styleSurfaceColor(style)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnnouncementIconPlate(
                    config = config,
                    defaultIcon = Icons.Outlined.CropSquare,
                    iconTint = styleAccentColor(style),
                    plateBackground = styleBadgeColor(style)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.announcement.title.ifBlank { Strings.popupAnnouncement },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = styleBodyColor(style),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = Strings.close, tint = styleBodyColor(style))
                }
            }
            HorizontalDivider(color = styleAccentColor(style).copy(alpha = 0.10f))
            AnnouncementContent(
                announcement = config.announcement,
                textColor = styleBodyColor(style)
            )
            DialogFooter(config.announcement.linkText, onLinkClick, linkUrl, onDismiss)
        }
    }
}

@Composable
private fun DarkDialog(
    config: AnnouncementConfig,
    style: AnnouncementTemplate,
    onDismiss: () -> Unit,
    onLinkClick: ((String) -> Unit)?,
    onNeverShowChecked: ((Boolean) -> Unit)?
) {
    val linkUrl = config.announcement.linkUrlOrNull()
    Card(
        modifier = Modifier
            .fillMaxWidth(0.90f)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = styleSurfaceColor(style)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnnouncementIconPlate(
                    config = config,
                    defaultIcon = Icons.Filled.DarkMode,
                    iconTint = Color.White,
                    plateBackground = Color.White.copy(alpha = 0.06f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.announcement.title.ifBlank { Strings.popupAnnouncement },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = Strings.close, tint = Color.White)
                }
            }
            AnnouncementContent(
                announcement = config.announcement,
                textColor = styleBodyColor(style)
            )
            DialogFooter(config.announcement.linkText, onLinkClick, linkUrl, onDismiss)
        }
    }
}
