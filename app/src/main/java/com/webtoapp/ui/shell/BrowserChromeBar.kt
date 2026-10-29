package com.webtoapp.ui.shell

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.shortenForBrowserChrome
import com.webtoapp.ui.design.WtaIconButton

/**
 * One leading control the browser toolbar can host for a child screen, such as
 * the multi-web drawer menu. Published only while the toolbar itself is visible,
 * so the child keeps its own bar when the toolbar is off.
 */
class BrowserChromeHost {
    var leadingAction by mutableStateOf<BrowserChromeAction?>(null)
}

class BrowserChromeAction(
    val contentDescription: String,
    val onClick: () -> Unit
)

val LocalBrowserChromeHost = staticCompositionLocalOf<BrowserChromeHost?> { null }

/**
 * Single-row browser chrome. Title and address share one line, and the row is
 * one icon tall instead of a two-line [androidx.compose.material3.TopAppBar].
 * Status-bar insets stay inside the bar, the same way the old app bar did.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserChromeBar(
    title: String,
    showTitle: Boolean,
    url: String,
    showUrl: Boolean,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    showForward: Boolean = false,
    showRefresh: Boolean = false,
    canGoBack: Boolean = false,
    canGoForward: Boolean = false,
    onBack: () -> Unit = {},
    onForward: () -> Unit = {},
    onRefresh: () -> Unit = {},
    showConsoleButton: Boolean = false,
    consoleExpanded: Boolean = false,
    onToggleConsole: () -> Unit = {},
    consoleErrorCount: Int = 0,
    showFindButton: Boolean = false,
    findExpanded: Boolean = false,
    onToggleFind: () -> Unit = {},
    showPluginButton: Boolean = false,
    leadingAction: BrowserChromeAction? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    val subtitle = if (showUrl && url.isNotEmpty()) url.shortenForBrowserChrome() else ""
    Surface(color = containerColor, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingAction != null) {
                WtaIconButton(
                    onClick = leadingAction.onClick,
                    icon = Icons.Filled.Menu,
                    contentDescription = leadingAction.contentDescription
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (leadingAction == null) 16.dp else 4.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showTitle && title.isNotEmpty()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = if (showTitle && title.isNotEmpty()) 8.dp else 0.dp)
                    )
                }
            }
            ChromeActions(
                showBack = showBack,
                showForward = showForward,
                showRefresh = showRefresh,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                onBack = onBack,
                onForward = onForward,
                onRefresh = onRefresh,
                showConsoleButton = showConsoleButton,
                consoleExpanded = consoleExpanded,
                onToggleConsole = onToggleConsole,
                consoleErrorCount = consoleErrorCount,
                showFindButton = showFindButton,
                findExpanded = findExpanded,
                onToggleFind = onToggleFind,
                showPluginButton = showPluginButton
            )
        }
    }
}

@Composable
private fun RowScope.ChromeActions(
    showBack: Boolean,
    showForward: Boolean,
    showRefresh: Boolean,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    showConsoleButton: Boolean,
    consoleExpanded: Boolean,
    onToggleConsole: () -> Unit,
    consoleErrorCount: Int,
    showFindButton: Boolean,
    findExpanded: Boolean,
    onToggleFind: () -> Unit,
    showPluginButton: Boolean
) {
    if (showBack) {
        WtaIconButton(
            onClick = onBack,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            enabled = canGoBack
        )
    }
    if (showForward) {
        WtaIconButton(
            onClick = onForward,
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Forward",
            enabled = canGoForward
        )
    }
    if (showRefresh) {
        WtaIconButton(
            onClick = onRefresh,
            icon = Icons.Filled.Refresh,
            contentDescription = "Refresh"
        )
    }
    if (showConsoleButton) {
        BadgedBox(
            badge = {
                if (consoleErrorCount > 0) {
                    Badge { Text("$consoleErrorCount") }
                }
            }
        ) {
            WtaIconButton(
                onClick = onToggleConsole,
                icon = if (consoleExpanded) Icons.Filled.Terminal else Icons.Outlined.Terminal,
                contentDescription = Strings.console
            )
        }
    }
    if (showFindButton) {
        WtaIconButton(
            onClick = onToggleFind,
            icon = if (findExpanded) Icons.Filled.Search else Icons.Outlined.Search,
            contentDescription = Strings.nativeBridgeCapsFindInPage
        )
    }
    if (showPluginButton) {
        com.webtoapp.ui.plugin.PluginToolbarEntries(
            onOpenSheet = { com.webtoapp.core.plugin.PluginHostState.openPluginSheet() }
        )
    }
}
