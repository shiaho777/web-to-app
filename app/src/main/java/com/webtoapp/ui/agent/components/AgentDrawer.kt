package com.webtoapp.ui.agent.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.webtoapp.core.agent.session.AgentSession
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.AgentUiState
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaColors
import com.webtoapp.ui.design.WtaFullEmptyState
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaTextField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AgentDrawer(
    state: AgentUiState,
    onTabChange: (AgentUiState.DrawerTab) -> Unit,
    onSearchChange: (String) -> Unit,
    onPickSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onPinSession: (String, Boolean) -> Unit,
    onPickFile: (String) -> Unit,
    onCopyFilePath: (String) -> Unit,
    onOpenWith: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        DrawerHeader(state.drawerSearch, onSearchChange)
        TabRow(
            selectedTabIndex = state.drawerTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            AgentUiState.DrawerTab.entries.forEachIndexed { i, tab ->
                Tab(
                    selected = state.drawerTab.ordinal == i,
                    onClick = { onTabChange(tab) },
                    text = {
                        Text(
                            text = tab.label(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = when (tab) {
                                AgentUiState.DrawerTab.Sessions -> Icons.Outlined.History
                                AgentUiState.DrawerTab.Files -> Icons.Outlined.Folder
                            },
                            contentDescription = null,
                            modifier = Modifier.size(WtaSize.Icon)
                        )
                    }
                )
            }
        }
        when (state.drawerTab) {
            AgentUiState.DrawerTab.Sessions -> SessionsTab(
                sessions = filterSessions(state.sessions, state.drawerSearch),
                currentId = state.currentSession?.id,
                liveSessionId = if (state.isWorking) state.currentSession?.id else null,
                onPick = onPickSession,
                onNew = onNewSession,
                onDelete = onDeleteSession,
                onPin = onPinSession
            )
            AgentUiState.DrawerTab.Files -> FilesTab(
                files = state.projectFiles,
                builtApks = state.builtApks,
                onPick = onPickFile,
                onPickApk = { apkName -> onPickFile("apk:$apkName") },
                onCopyPath = onCopyFilePath,
                onOpenWith = onOpenWith
            )
        }
    }
}

private fun AgentUiState.DrawerTab.label() = when (this) {
    AgentUiState.DrawerTab.Sessions -> Strings.agentTabSessions
    AgentUiState.DrawerTab.Files -> Strings.agentTabFiles
}

@Composable
private fun DrawerHeader(query: String, onSearchChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WtaSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Medium)
    ) {
        Text(
            text = Strings.agentTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        WtaTextField(
            value = query,
            onValueChange = onSearchChange,
            placeholder = Strings.agentDrawerSearchHint,
            leadingIcon = Icons.Outlined.Search,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SessionsTab(
    sessions: List<AgentSession>,
    currentId: String?,
    liveSessionId: String?,
    onPick: (String) -> Unit,
    onNew: () -> Unit,
    onDelete: (String) -> Unit,
    onPin: (String, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WtaSpacing.ScreenHorizontal,
                    vertical = WtaSpacing.Small
                )
        ) {
            WtaButton(
                onClick = onNew,
                text = Strings.agentNewSession,
                variant = WtaButtonVariant.Primary,
                size = WtaButtonSize.Medium,
                leadingIcon = Icons.Outlined.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (sessions.isEmpty()) {
            WtaFullEmptyState(
                title = Strings.agentEmptySessions,
                message = Strings.agentEmptySessionsHint,
                icon = Icons.Outlined.History
            )
            return
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(sessions, key = { it.id }) { session ->
                SessionRow(
                    session = session,
                    isCurrent = session.id == currentId,
                    isLive = session.id == liveSessionId,
                    onClick = { onPick(session.id) },
                    onPin = { onPin(session.id, !session.pinned) },
                    onDelete = { onDelete(session.id) }
                )
                WtaSectionDivider()
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: AgentSession,
    isCurrent: Boolean,
    isLive: Boolean,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit
) {
    val titleText = session.title.ifBlank { Strings.agentHomeUntitledSession }
    WtaSettingRow(
        title = titleText,
        subtitle = formatSubtitle(session),
        modifier = if (isCurrent) {
            Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        } else Modifier,
        iconContent = {

            when {
                isLive -> LivePulse()
                session.pinned -> Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(WtaSize.Icon)
                )
                else -> Icon(
                    Icons.Outlined.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(WtaSize.Icon)
                )
            }
        },
        onClick = onClick
    ) {
        WtaIconButton(
            onClick = onPin,
            icon = if (session.pinned) Icons.Outlined.Star else Icons.Outlined.StarOutline,
            contentDescription = Strings.agentSessionPin,
            modifier = Modifier.size(WtaSize.TouchTarget)
        )
        WtaIconButton(
            onClick = onDelete,
            icon = Icons.Outlined.Delete,
            contentDescription = Strings.agentSessionDelete,
            modifier = Modifier.size(WtaSize.TouchTarget)
        )
    }
}

private fun formatSubtitle(session: AgentSession): String {
    val parts = mutableListOf<String>()
    parts += Strings.agentSessionMessagesShort.format(session.messages.size)
    parts += DRAWER_TIME.format(Date(session.updatedAt))
    return parts.joinToString(" · ")
}

private val DRAWER_TIME = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

@Composable
private fun FilesTab(
    files: List<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo>,
    builtApks: List<com.webtoapp.core.agent.tool.BuiltApkInfo>,
    onPick: (String) -> Unit,
    onPickApk: (String) -> Unit,
    onCopyPath: (String) -> Unit,
    onOpenWith: (String) -> Unit
) {
    if (files.isEmpty() && builtApks.isEmpty()) {
        WtaFullEmptyState(
            title = Strings.agentEmptyFiles,
            message = Strings.agentEmptyFilesHint,
            icon = Icons.Outlined.Folder
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (builtApks.isNotEmpty()) {
            item(key = "__apk_header__") {
                Text(
                    text = Strings.agentBuiltApks,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = WtaSpacing.ScreenHorizontal, vertical = WtaSpacing.Small)
                )
            }
            items(builtApks, key = { "apk_${it.appId}_${it.apkName}" }) { apk ->
                val apkVirtualPath = "apk:${apk.apkName}"
                FileEntryRow(
                    title = apk.apkName,
                    subtitle = "${apk.formatSize()} · ${apk.buildMode}",
                    icon = Icons.Outlined.Android,
                    onOpen = { onPickApk(apk.apkName) },
                    onOpenWith = { onOpenWith(apkVirtualPath) },
                    onCopyPath = { onCopyPath(apk.apkPath) }
                )
                WtaSectionDivider()
            }
            item(key = "__apk_spacer__") { Spacer(Modifier.height(WtaSpacing.Small)) }
        }
        items(files, key = { it.relativePath }) { f ->
            FileEntryRow(
                title = f.relativePath,
                subtitle = "${f.formatSize()} · ${f.formatTime()}",
                icon = Icons.Outlined.Code,
                onOpen = { onPick(f.relativePath) },
                onOpenWith = { onOpenWith(f.relativePath) },
                onCopyPath = { onCopyPath(f.relativePath) }
            )
            WtaSectionDivider()
        }
    }
}

@Composable
private fun FileEntryRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onOpen: () -> Unit,
    onOpenWith: () -> Unit,
    onCopyPath: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    WtaSettingRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = onOpen,
        trailing = {
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(WtaSize.TouchTarget)) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = Strings.more,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(Strings.agentFileOpen) },
                        leadingIcon = { Icon(Icons.Outlined.OpenInNew, contentDescription = null) },
                        onClick = { menuOpen = false; onOpen() }
                    )
                    DropdownMenuItem(
                        text = { Text(Strings.agentFileOpenWith) },
                        leadingIcon = { Icon(Icons.Outlined.Apps, contentDescription = null) },
                        onClick = { menuOpen = false; onOpenWith() }
                    )
                    DropdownMenuItem(
                        text = { Text(Strings.agentFileCopyPath) },
                        leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                        onClick = { menuOpen = false; onCopyPath() }
                    )
                }
            }
        }
    )
}

private fun filterSessions(all: List<AgentSession>, query: String): List<AgentSession> {
    val ordered = compareByDescending<AgentSession> { it.pinned }.thenByDescending { it.updatedAt }
    if (query.isBlank()) return all.sortedWith(ordered)
    val q = query.lowercase()
    return all.filter {
        it.title.lowercase().contains(q)
    }.sortedWith(ordered)
}

@Composable
private fun LivePulse() {
    val infinite = rememberInfiniteTransition(label = "live-dot")
    val scaleValue by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alphaValue by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val pulseColor = WtaColors.semantic.success
    Box(
        modifier = Modifier
            .size(WtaSpacing.Small)
            .graphicsLayer {
                scaleX = scaleValue
                scaleY = scaleValue
                alpha = alphaValue
            }
            .background(color = pulseColor, shape = CircleShape)
    )
}
