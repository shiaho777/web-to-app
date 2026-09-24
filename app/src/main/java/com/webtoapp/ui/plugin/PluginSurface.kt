package com.webtoapp.ui.plugin

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.plugin.PluginEntryStyle
import com.webtoapp.core.plugin.PluginHostState
import com.webtoapp.core.plugin.PluginInjection
import com.webtoapp.core.plugin.PluginKind
import com.webtoapp.core.plugin.PluginPanelStyle
import com.webtoapp.core.plugin.PluginSession
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// Strings (10 languages, no else)
// ---------------------------------------------------------------------------

private val pluginsTitle: String
    @Composable get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "插件"
        AppLanguage.ENGLISH -> "Plugins"
        AppLanguage.ARABIC -> "الإضافات"
        AppLanguage.PORTUGUESE -> "Plugins"
        AppLanguage.SPANISH -> "Plugins"
        AppLanguage.FRENCH -> "Plugins"
        AppLanguage.GERMAN -> "Plugins"
        AppLanguage.RUSSIAN -> "Плагины"
        AppLanguage.JAPANESE -> "プラグイン"
        AppLanguage.KOREAN -> "플러그인"
    }

private val activeOnPage: String
    @Composable get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "此页面运行中"
        AppLanguage.ENGLISH -> "Active on this page"
        AppLanguage.ARABIC -> "نشط على هذه الصفحة"
        AppLanguage.PORTUGUESE -> "Ativo nesta página"
        AppLanguage.SPANISH -> "Activo en esta página"
        AppLanguage.FRENCH -> "Actif sur cette page"
        AppLanguage.GERMAN -> "Auf dieser Seite aktiv"
        AppLanguage.RUSSIAN -> "Активен на странице"
        AppLanguage.JAPANESE -> "このページで有効"
        AppLanguage.KOREAN -> "이 페이지에서 활성"
    }

private val inactiveOnPage: String
    @Composable get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面不匹配"
        AppLanguage.ENGLISH -> "Does not match this page"
        AppLanguage.ARABIC -> "لا يطابق هذه الصفحة"
        AppLanguage.PORTUGUESE -> "Não corresponde a esta página"
        AppLanguage.SPANISH -> "No coincide con esta página"
        AppLanguage.FRENCH -> "Ne correspond pas à cette page"
        AppLanguage.GERMAN -> "Passt nicht zu dieser Seite"
        AppLanguage.RUSSIAN -> "Не совпадает со страницей"
        AppLanguage.JAPANESE -> "このページには非対応"
        AppLanguage.KOREAN -> "이 페이지와 일치하지 않음"
    }

private val closeLabel: String
    @Composable get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "关闭"
        AppLanguage.ENGLISH -> "Close"
        AppLanguage.ARABIC -> "إغلاق"
        AppLanguage.PORTUGUESE -> "Fechar"
        AppLanguage.SPANISH -> "Cerrar"
        AppLanguage.FRENCH -> "Fermer"
        AppLanguage.GERMAN -> "Schließen"
        AppLanguage.RUSSIAN -> "Закрыть"
        AppLanguage.JAPANESE -> "閉じる"
        AppLanguage.KOREAN -> "닫기"
    }

private val noPluginsInstalled: String
    @Composable get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有已启用的插件"
        AppLanguage.ENGLISH -> "No enabled plugins"
        AppLanguage.ARABIC -> "لا توجد إضافات مفعّلة"
        AppLanguage.PORTUGUESE -> "Nenhum plugin ativo"
        AppLanguage.SPANISH -> "No hay plugins activos"
        AppLanguage.FRENCH -> "Aucun plugin actif"
        AppLanguage.GERMAN -> "Keine aktiven Plugins"
        AppLanguage.RUSSIAN -> "Нет активных плагинов"
        AppLanguage.JAPANESE -> "有効なプラグインなし"
        AppLanguage.KOREAN -> "활성화된 플러그인 없음"
    }

internal fun kindLabel(kind: PluginKind): String = when (kind) {
    PluginKind.HCJ -> "HCJ"
    PluginKind.USERSCRIPT -> "JS"
    PluginKind.CHROME_EXTENSION -> "EXT"
}

internal fun pluginIcon(icon: String): ImageVector = when (icon) {
    "dark_mode", "dark-mode" -> Icons.Filled.DarkMode
    "download", "file_download" -> Icons.Filled.Download
    "find_in_page", "search" -> Icons.Filled.FindInPage
    "insights", "analytics", "web_analyzer" -> Icons.Filled.Insights
    "play_circle", "video", "video_enhancer", "movie" -> Icons.Filled.PlayCircle
    "privacy_tip", "privacy", "shield" -> Icons.Filled.PrivacyTip
    "security", "block", "element_blocker", "block_circle", "ad_block" -> Icons.Filled.Security
    "visibility", "eye" -> Icons.Filled.Visibility
    "palette", "content_enhancer", "auto_awesome" -> Icons.Filled.Palette
    else -> Icons.Filled.Extension
}

// ---------------------------------------------------------------------------
// Toolbar slot
// ---------------------------------------------------------------------------

/**
 * Toolbar plugin entries. Each plugin that picked [PluginEntryStyle.TOOLBAR]
 * gets its own icon; plugins living in the menu or on the floating handle are
 * reachable through the puzzle button, which opens the plugin sheet.
 */
@Composable
fun PluginToolbarEntries(
    onOpenSheet: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    val entries by PluginHostState.entries.collectAsStateWithLifecycle()
    val session = PluginHostState.session
    val effectiveTint = if (tint == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        tint
    }
    val toolbarEntries = entries.filter { it.entryStyle == PluginEntryStyle.TOOLBAR }
    val hiddenEntries = entries.filter { it.entryStyle != PluginEntryStyle.TOOLBAR }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        toolbarEntries.forEach { entry ->
            BadgedBox(
                badge = {
                    if (entry.badge.isNotBlank()) Badge { Text(entry.badge.take(4)) }
                }
            ) {
                IconButton(onClick = { session?.activateEntry(entry.pluginId) }) {
                    Icon(
                        pluginIcon(entry.icon),
                        contentDescription = entry.name,
                        tint = effectiveTint
                    )
                }
            }
        }
        if (hiddenEntries.isNotEmpty() || toolbarEntries.isEmpty()) {
            // Plugin-authored badge only — never an auto "N active" count.
            val explicitBadge = hiddenEntries.firstOrNull { it.badge.isNotBlank() }?.badge
            BadgedBox(
                badge = {
                    if (explicitBadge != null) Badge { Text(explicitBadge.take(4)) }
                }
            ) {
                IconButton(onClick = onOpenSheet) {
                    Icon(
                        if (toolbarEntries.isEmpty()) Icons.Filled.MoreVert else Icons.Filled.Extension,
                        contentDescription = pluginsTitle,
                        tint = effectiveTint
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Floating handle (entry style for toolbar-less apps)
// ---------------------------------------------------------------------------

/**
 * Draggable native handle for apps without a toolbar. Lives in the Compose
 * layer — page CSS can never move or hide it.
 */
@Composable
fun PluginFloatingHandle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by PluginHostState.entries.collectAsStateWithLifecycle()
    val explicitBadge = entries.firstOrNull { it.badge.isNotBlank() }?.badge
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        BadgedBox(
            badge = {
                if (explicitBadge != null) Badge { Text(explicitBadge.take(4)) }
            }
        ) {
            Surface(
                onClick = onClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                shadowElevation = 4.dp
            ) {
                Icon(
                    Icons.Filled.Extension,
                    contentDescription = pluginsTitle,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp).size(22.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Plugin sheet — the unified list of plugins relevant to the current page
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginSheet(
    onDismiss: () -> Unit,
    onManage: (() -> Unit)? = null
) {
    val entries by PluginHostState.entries.collectAsStateWithLifecycle()
    val session = PluginHostState.session

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = pluginsTitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            if (entries.isEmpty()) {
                Text(
                    text = noPluginsInstalled,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    entries.forEach { entry ->
                        item(key = entry.pluginId) {
                            PluginSheetRow(
                                entry = entry,
                                onClick = {
                                    session?.activateEntry(entry.pluginId)
                                    onDismiss()
                                }
                            )
                        }
                        // Userscript menu commands surface as indented sub-rows —
                        // the unified place GM_registerMenuCommand lives now.
                        entry.menuCommands.forEach { cmd ->
                            item(key = entry.pluginId + ":menu:" + cmd) {
                                PluginMenuCommandRow(
                                    name = cmd,
                                    onClick = {
                                        session?.invokeMenuCommand(entry.pluginId, cmd)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if (onManage != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = pluginsTitle + " ›",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onManage()
                            onDismiss()
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun PluginSheetRow(
    entry: PluginHostState.Entry,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            pluginIcon(entry.icon),
            contentDescription = null,
            tint = if (entry.matchesCurrentUrl) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (entry.matchesCurrentUrl) activeOnPage else inactiveOnPage,
                style = MaterialTheme.typography.bodySmall,
                color = if (entry.matchesCurrentUrl) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                kindLabel(entry.kind),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        if (entry.badge.isNotBlank()) {
            Spacer(Modifier.width(8.dp))
            Badge { Text(entry.badge.take(4)) }
        }
    }
}

/** An indented menu-command row under a userscript entry. */
@Composable
private fun PluginMenuCommandRow(
    name: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 58.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.PlayCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Panel host — bottom sheet / floating window / fullscreen for panel.html
// ---------------------------------------------------------------------------

/** Mount the whole runtime plugin surface: sheet + panel host + (optionally) the handle. */
@Composable
fun PluginSurfaceHost(
    entryStyle: PluginEntryStyle,
    toolbarVisible: Boolean = true,
    floatingHandleModifier: Modifier = Modifier,
    onManage: (() -> Unit)? = null
) {
    val sheetOpen by PluginHostState.sheetOpen.collectAsStateWithLifecycle()
    val panelRequest by PluginHostState.panelRequest.collectAsStateWithLifecycle()
    val panelOpen by PluginHostState.panelOpen.collectAsStateWithLifecycle()
    val entries by PluginHostState.entries.collectAsStateWithLifecycle()

    if (sheetOpen) {
        PluginSheet(
            onDismiss = { PluginHostState.dismissPluginSheet() },
            onManage = onManage
        )
    }

    val request = panelRequest
    if (panelOpen && request != null) {
        PluginPanelHost(
            request = request,
            style = request.panelStyle,
            onDismiss = { PluginHostState.dismissPanel() }
        )
    }

    // TOOLBAR/MENU entries live in the app bar; a plugin that picked the
    // floating handle (or a hidden toolbar) renders the draggable launcher.
    val wantsHandle = entries.any { it.entryStyle == PluginEntryStyle.FLOATING_HANDLE }
    if (entryStyle == PluginEntryStyle.FLOATING_HANDLE || wantsHandle || !toolbarVisible) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            PluginFloatingHandle(
                onClick = { PluginHostState.openPluginSheet() },
                modifier = floatingHandleModifier.padding(20.dp)
            )
        }
    }
}

@Composable
fun PluginPanelHost(
    request: PluginHostState.PanelRequest,
    style: PluginPanelStyle,
    onDismiss: () -> Unit
) {
    when (style) {
        PluginPanelStyle.BOTTOM_SHEET -> PluginPanelBottomSheet(request, onDismiss)
        PluginPanelStyle.FLOATING_WINDOW -> PluginPanelFloating(request, onDismiss)
        PluginPanelStyle.FULLSCREEN -> PluginPanelFullscreen(request, onDismiss)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PluginPanelBottomSheet(
    request: PluginHostState.PanelRequest,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().height(480.dp)) {
            PluginPanelTitle(request, onDismiss)
            PluginPanelWebView(request, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun PluginPanelFullscreen(
    request: PluginHostState.PanelRequest,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize()) {
                PluginPanelTitle(request, onDismiss)
                PluginPanelWebView(request, Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun PluginPanelFloating(
    request: PluginHostState.PanelRequest,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }
        Surface(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .fillMaxWidth()
                .height(480.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        }
                ) {
                    PluginPanelTitle(request, onDismiss)
                }
                PluginPanelWebView(request, Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun PluginPanelTitle(
    request: PluginHostState.PanelRequest,
    onDismiss: () -> Unit
) {
    val session = PluginHostState.session
    val name = session?.pluginFor(request.pluginId)?.name ?: request.pluginId
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = closeLabel)
        }
    }
    HorizontalDivider()
}

/**
 * The WebView that renders `panel.html`: native panel bridge attached, panel
 * bootstrap injected into the authored HTML before its own scripts run.
 */
@Composable
private fun PluginPanelWebView(
    request: PluginHostState.PanelRequest,
    modifier: Modifier = Modifier
) {
    val session = PluginHostState.session
    // Feed the app's real palette into the panel document: authored panels
    // style themselves with var(--wta-*) tokens and fall back to light greys
    // when the host does not define them.
    val scheme = MaterialTheme.colorScheme
    val panelThemeHead = remember(scheme) {
        fun hex(c: Color): String = "#%06X".format(c.toArgb() and 0xFFFFFF)
        "<style>:root{" +
            "--wta-surface:${hex(scheme.surface)};" +
            "--wta-surface-dim:${hex(scheme.surfaceVariant)};" +
            "--wta-on-surface:${hex(scheme.onSurface)};" +
            "--wta-on-surface-variant:${hex(scheme.onSurfaceVariant)};" +
            "--wta-outline:${hex(scheme.outlineVariant)};" +
            "--wta-accent:${hex(scheme.primary)};" +
            "--wta-on-accent:${hex(scheme.onPrimary)};" +
            "--wta-accent-soft:${hex(scheme.primaryContainer)};" +
            "--wta-danger:${hex(scheme.error)};" +
            "}body{background:${hex(scheme.surface)};color:${hex(scheme.onSurface)}}</style>"
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Chrome-extension popups need their own fully-configured WebView
            // (resource interception + chrome.* polyfill); the runtime factory
            // supplies it. HCJ/userscript panels get the hcjPanel bridge.
            val chromeWv = if (request.kind == PluginKind.CHROME_EXTENSION) {
                session?.popupWebViewFactory?.invoke(request.url)
            } else null
            chromeWv ?: com.webtoapp.core.webview.WtaWebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(scheme.surface.toArgb())
                webViewClient = object : WebViewClient() {}
                session?.panelEvaluator = { js ->
                    evaluateJavascript(js, null)
                }
                request.html?.let { rawHtml ->
                    val pid = request.pluginId
                    session?.createPanelBridge(pid)?.let { panelBridge ->
                        addJavascriptInterface(panelBridge, "__hcjPanelBridge")
                    }
                    val bootstrap = PluginInjection.panelBootstrap(pid)
                    val html = injectPanelBootstrap(rawHtml, bootstrap, panelThemeHead)
                    loadDataWithBaseURL(request.baseUrl, html, "text/html", "UTF-8", null)
                } ?: run {
                    request.url.takeIf { it.isNotBlank() }?.let { loadUrl(it) }
                }
            }
        },
        onRelease = { wv ->
            session?.panelEvaluator = null
            // Chrome popup WebViews are owned by their ExtensionPopupManager —
            // the session's onPanelClosed hook destroys them; only destroy the
            // plain panel WebViews we created here.
            if (request.kind != PluginKind.CHROME_EXTENSION) wv.destroy()
        }
    )
}

/** Inject the hcjPanel bootstrap + theme vars into authored panel HTML ahead of its scripts. */
private fun injectPanelBootstrap(html: String, bootstrap: String, themeHead: String): String {
    val tag = "<script>$bootstrap</script>$themeHead"
    Regex("<head[^>]*>", RegexOption.IGNORE_CASE).find(html)?.let {
        return html.substring(0, it.range.last + 1) + tag + html.substring(it.range.last + 1)
    }
    Regex("<html[^>]*>", RegexOption.IGNORE_CASE).find(html)?.let {
        return html.substring(0, it.range.last + 1) + "<head>$tag</head>" +
            html.substring(it.range.last + 1)
    }
    return "<head>$tag</head>" + html
}
