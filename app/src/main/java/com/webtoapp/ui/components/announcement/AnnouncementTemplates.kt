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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.toArgb
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.Announcement
import com.webtoapp.data.model.AnnouncementTemplateType

enum class AnnouncementTemplate(
    val type: AnnouncementTemplateType,
    val icon: ImageVector
) {
    MINIMAL(AnnouncementTemplateType.MINIMAL, Icons.Default.Check),
    XIAOHONGSHU(AnnouncementTemplateType.XIAOHONGSHU, Icons.AutoMirrored.Filled.ArrowForward),
    GRADIENT(AnnouncementTemplateType.GRADIENT, Icons.Default.Star),
    GLASSMORPHISM(AnnouncementTemplateType.GLASSMORPHISM, Icons.Default.Phone),
    NEON(AnnouncementTemplateType.NEON, Icons.Default.Warning),
    CUTE(AnnouncementTemplateType.CUTE, Icons.Default.FavoriteBorder),
    ELEGANT(AnnouncementTemplateType.ELEGANT, Icons.Default.Star),
    FESTIVE(AnnouncementTemplateType.FESTIVE, Icons.Default.Favorite),
    DARK(AnnouncementTemplateType.DARK, Icons.Default.Settings),
    NATURE(AnnouncementTemplateType.NATURE, Icons.Default.Place)
}

data class AnnouncementConfig(
    val announcement: Announcement,
    val template: AnnouncementTemplate = AnnouncementTemplate.MINIMAL,
    val primaryColor: Color = Color(0xFF4F46E5)
)

private val announcementStyleTemplates = listOf(
    AnnouncementTemplate.MINIMAL,
    AnnouncementTemplate.GRADIENT,
    AnnouncementTemplate.DARK
)

private fun styleTemplate(template: AnnouncementTemplate): AnnouncementTemplate = when (template) {
    AnnouncementTemplate.MINIMAL,
    AnnouncementTemplate.CUTE,
    AnnouncementTemplate.ELEGANT -> AnnouncementTemplate.MINIMAL

    AnnouncementTemplate.XIAOHONGSHU,
    AnnouncementTemplate.GRADIENT,
    AnnouncementTemplate.GLASSMORPHISM,
    AnnouncementTemplate.FESTIVE,
    AnnouncementTemplate.NATURE -> AnnouncementTemplate.GRADIENT

    AnnouncementTemplate.NEON,
    AnnouncementTemplate.DARK -> AnnouncementTemplate.DARK
}

private fun currentStyleName(style: AnnouncementTemplate): String = when (style) {
    AnnouncementTemplate.MINIMAL -> when (Strings.currentLanguage.value) {
        AppLanguage.CHINESE -> Strings.announcementStyleClean
        AppLanguage.ENGLISH -> Strings.announcementStyleClean
        AppLanguage.ARABIC -> Strings.announcementStyleClean
        AppLanguage.PORTUGUESE ->Strings.announcementStyleClean
        AppLanguage.SPANISH ->Strings.announcementStyleClean
        AppLanguage.FRENCH ->Strings.announcementStyleClean
        AppLanguage.GERMAN ->Strings.announcementStyleClean
        AppLanguage.RUSSIAN ->Strings.announcementStyleClean
        AppLanguage.JAPANESE ->Strings.announcementStyleClean
        AppLanguage.KOREAN ->Strings.announcementStyleClean
    }
    AnnouncementTemplate.GRADIENT -> when (Strings.currentLanguage.value) {
        AppLanguage.CHINESE -> Strings.announcementStyleAccent
        AppLanguage.ENGLISH -> Strings.announcementStyleAccent
        AppLanguage.ARABIC -> Strings.announcementStyleAccent
        AppLanguage.PORTUGUESE ->Strings.announcementStyleAccent
        AppLanguage.SPANISH ->Strings.announcementStyleAccent
        AppLanguage.FRENCH ->Strings.announcementStyleAccent
        AppLanguage.GERMAN ->Strings.announcementStyleAccent
        AppLanguage.RUSSIAN ->Strings.announcementStyleAccent
        AppLanguage.JAPANESE ->Strings.announcementStyleAccent
        AppLanguage.KOREAN ->Strings.announcementStyleAccent
    }
    AnnouncementTemplate.DARK -> when (Strings.currentLanguage.value) {
        AppLanguage.CHINESE -> Strings.announcementStyleDark
        AppLanguage.ENGLISH -> Strings.announcementStyleDark
        AppLanguage.ARABIC -> Strings.announcementStyleDark
        AppLanguage.PORTUGUESE ->Strings.announcementStyleDark
        AppLanguage.SPANISH ->Strings.announcementStyleDark
        AppLanguage.FRENCH ->Strings.announcementStyleDark
        AppLanguage.GERMAN ->Strings.announcementStyleDark
        AppLanguage.RUSSIAN ->Strings.announcementStyleDark
        AppLanguage.JAPANESE ->Strings.announcementStyleDark
        AppLanguage.KOREAN ->Strings.announcementStyleDark
    }
    else -> ""
}

private fun currentStyleDesc(style: AnnouncementTemplate): String = when (style) {
    AnnouncementTemplate.MINIMAL -> Strings.announcementStyleCleanDesc
    AnnouncementTemplate.GRADIENT -> Strings.announcementStyleAccentDesc
    AnnouncementTemplate.DARK -> Strings.announcementStyleDarkDesc
    else -> ""
}

fun AnnouncementTemplateType.toUiTemplate(): AnnouncementTemplate = when (this) {
    AnnouncementTemplateType.MINIMAL,
    AnnouncementTemplateType.CUTE,
    AnnouncementTemplateType.ELEGANT -> AnnouncementTemplate.MINIMAL

    AnnouncementTemplateType.XIAOHONGSHU,
    AnnouncementTemplateType.GRADIENT,
    AnnouncementTemplateType.GLASSMORPHISM,
    AnnouncementTemplateType.FESTIVE,
    AnnouncementTemplateType.NATURE -> AnnouncementTemplate.GRADIENT

    AnnouncementTemplateType.NEON,
    AnnouncementTemplateType.DARK -> AnnouncementTemplate.DARK
}

fun AnnouncementTemplate.toStoredTemplate(): AnnouncementTemplateType = styleTemplate(this).type

fun AnnouncementTemplate.getLocalizedDisplayName(): String = currentStyleName(styleTemplate(this))

fun AnnouncementTemplate.getLocalizedDescription(): String = currentStyleDesc(styleTemplate(this))

fun AnnouncementTemplate.isSelectableStyle(): Boolean = styleTemplate(this) in announcementStyleTemplates

private fun styleAccentColor(template: AnnouncementTemplate): Color = when (styleTemplate(template)) {
    AnnouncementTemplate.MINIMAL -> Color(0xFF475569)
    AnnouncementTemplate.GRADIENT -> Color(0xFF4F46E5)
    AnnouncementTemplate.DARK -> Color(0xFFCBD5E1)
    else -> Color(0xFF4F46E5)
}

private fun styleSurfaceColor(template: AnnouncementTemplate): Color = when (styleTemplate(template)) {
    AnnouncementTemplate.MINIMAL -> Color(0xFFF8FAFC)
    AnnouncementTemplate.GRADIENT -> Color(0xFFF7F7FF)
    AnnouncementTemplate.DARK -> Color(0xFF15161C)
    else -> Color(0xFFF8FAFC)
}

private fun styleBodyColor(template: AnnouncementTemplate): Color = when (styleTemplate(template)) {
    AnnouncementTemplate.MINIMAL -> Color(0xFF1F2937)
    AnnouncementTemplate.GRADIENT -> Color(0xFF27284A)
    AnnouncementTemplate.DARK -> Color(0xFFE5E7EB)
    else -> Color(0xFF1F2937)
}

private fun styleBadgeColor(template: AnnouncementTemplate): Color = when (styleTemplate(template)) {
    AnnouncementTemplate.MINIMAL -> Color(0xFFE2E8F0)
    AnnouncementTemplate.GRADIENT -> Color(0xFFE0E7FF)
    AnnouncementTemplate.DARK -> Color(0xFF2A2D36)
    else -> Color(0xFFE2E8F0)
}

private fun headerBrush(template: AnnouncementTemplate): Brush? = when (styleTemplate(template)) {
    AnnouncementTemplate.MINIMAL -> null
    AnnouncementTemplate.GRADIENT -> Brush.horizontalGradient(listOf(Color(0xFFEFF1FF), Color(0xFFF7F3FF)))
    AnnouncementTemplate.DARK -> Brush.linearGradient(listOf(Color(0xFF23242C), Color(0xFF181A22)))
    else -> null
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
    val selectedStyle = styleTemplate(selectedTemplate)

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = Strings.selectAnnouncementStyle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = styleAccentColor(selectedStyle).copy(alpha = 0.10f)
            ) {
                Text(
                    text = currentStyleName(selectedStyle),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = styleAccentColor(selectedStyle),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(announcementStyleTemplates) { template ->
                val isSelected = template == selectedStyle
                StyleCard(
                    template = template,
                    isSelected = isSelected,
                    onClick = { onTemplateSelected(template) }
                )
            }
        }
    }
}

@Composable
private fun StyleCard(
    template: AnnouncementTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val style = styleTemplate(template)
    val colors = when (style) {
        AnnouncementTemplate.MINIMAL -> listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
        AnnouncementTemplate.GRADIENT -> listOf(Color(0xFFF3F4FF), Color(0xFFE9E5FF))
        AnnouncementTemplate.DARK -> listOf(Color(0xFF1C1D25), Color(0xFF101116))
        else -> listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(116.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) styleAccentColor(style) else Color.Transparent,
                    shape = RoundedCornerShape(18.dp)
                )
                .background(Brush.linearGradient(colors))
                .clickable(onClick = onClick)
                .padding(10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (style) {
                        AnnouncementTemplate.MINIMAL -> "◻"
                        AnnouncementTemplate.GRADIENT -> "◈"
                        AnnouncementTemplate.DARK -> "◐"
                        else -> "◻"
                    },
                    fontSize = 20.sp,
                    color = styleAccentColor(style)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(styleAccentColor(style).copy(alpha = 0.45f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(58.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(styleAccentColor(style).copy(alpha = 0.18f))
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = currentStyleName(style),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) styleAccentColor(style) else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AnnouncementDialog(
    config: AnnouncementConfig,
    onDismiss: () -> Unit,
    onLinkClick: ((String) -> Unit)? = null,
    onNeverShowChecked: ((Boolean) -> Unit)? = null
) {
    val style = styleTemplate(config.template)
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
            AnnouncementTemplate.GRADIENT -> AccentDialog(config, style, onDismiss, onLinkClick, onNeverShowChecked)
            AnnouncementTemplate.DARK -> DarkDialog(config, style, onDismiss, onLinkClick, onNeverShowChecked)
            else -> SimpleDialog(config, style, onDismiss, onLinkClick, onNeverShowChecked)
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
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(styleBadgeColor(style)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = styleAccentColor(style), modifier = Modifier.size(18.dp))
                }
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
                    Icon(Icons.Default.Close, contentDescription = Strings.close, tint = styleBodyColor(style))
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
private fun AccentDialog(
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
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBrush(style) ?: Brush.linearGradient(listOf(Color(0xFFF3F4FF), Color(0xFFE9E5FF))))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, null, tint = styleAccentColor(style), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = config.announcement.title.ifBlank { Strings.popupAnnouncement },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2350)
                        )
                        if (!config.announcement.contentIsHtml) {
                            Text(
                                text = config.announcement.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF3B3F68),
                                lineHeight = 23.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = Strings.close, tint = Color(0xFF1F2350))
                    }
                }
            }
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (config.announcement.contentIsHtml) {
                    AnnouncementContent(
                        announcement = config.announcement,
                        textColor = Color(0xFF3B3F68)
                    )
                }
                DialogFooter(config.announcement.linkText, onLinkClick, linkUrl, onDismiss)
            }
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
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.06f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
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
                    Icon(Icons.Default.Close, contentDescription = Strings.close, tint = Color.White)
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
