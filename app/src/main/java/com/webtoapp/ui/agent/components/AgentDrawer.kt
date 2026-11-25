package com.webtoapp.ui.agent.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.agent.session.AgentSession
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.AgentUiState
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaDropdownMenu
import com.webtoapp.ui.design.WtaDropdownMenuItem
import com.webtoapp.ui.design.WtaFullEmptyState
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.design.WtaMotion
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaTab
import com.webtoapp.ui.design.WtaTabRow
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.ui.theme.LocalAnimationSettings
import com.webtoapp.ui.theme.ifDescriptionsShown

@Composable
fun AgentDrawer(
    state: AgentUiState,
    onClose: () -> Unit,
    onTabChange: (AgentUiState.DrawerTab) -> Unit,
    onSearchChange: (String) -> Unit,
    onPickSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onPinSession: (String, Boolean) -> Unit,
    onRenameSession: (String, String) -> Unit,
    onExportSession: (String) -> Unit,
    onPickFile: (String) -> Unit,
    onCopyFilePath: (String) -> Unit,
    onOpenWith: (String) -> Unit,
    onDeleteFile: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .focusable()
    ) {
        DrawerHeader(state.drawerSearch, onSearchChange, onClose)
        WtaTabRow(
            tabs = AgentUiState.DrawerTab.entries.map { tab ->
                WtaTab(
                    label = tab.label(),
                    count = when (tab) {
                        AgentUiState.DrawerTab.Sessions -> state.sessions.size
                        AgentUiState.DrawerTab.Files -> state.projectFiles.size
                    }
                )
            },
            selectedIndex = state.drawerTab.ordinal,
            onTabSelected = { idx -> onTabChange(AgentUiState.DrawerTab.entries[idx]) },
            modifier = Modifier.padding(horizontal = WtaSpacing.ScreenHorizontal)
        )
        Crossfade(
            targetState = state.drawerTab,
            animationSpec = WtaMotion.standardTween(),
            label = "drawer tab"
        ) { tab ->
            when (tab) {
                AgentUiState.DrawerTab.Sessions -> SessionsTab(
                    sessions = filterSessions(state.sessions, state.drawerSearch),
                    isSearching = state.drawerSearch.isNotBlank(),
                    currentId = state.currentSession?.id,
                    liveSessionId = if (state.isWorking) state.currentSession?.id else null,
                    onPick = onPickSession,
                    onNew = onNewSession,
                    onDelete = onDeleteSession,
                    onPin = onPinSession,
                    onRename = onRenameSession,
                    onExport = onExportSession
                )
                AgentUiState.DrawerTab.Files -> FilesTab(
                    files = filterFiles(state.projectFiles, state.drawerSearch),
                    builtApks = state.builtApks.filter {
                        it.apkName.contains(state.drawerSearch.trim(), ignoreCase = true)
                    },
                    isSearching = state.drawerSearch.isNotBlank(),
                    onPick = onPickFile,
                    onPickApk = { apkName -> onPickFile("apk:$apkName") },
                    onCopyPath = onCopyFilePath,
                    onOpenWith = onOpenWith,
                    onDelete = onDeleteFile
                )
            }
        }
    }
}

private fun AgentUiState.DrawerTab.label() = when (this) {
    AgentUiState.DrawerTab.Sessions -> Strings.agentTabSessions
    AgentUiState.DrawerTab.Files -> Strings.agentTabFiles
}

@Composable
private fun DrawerHeader(
    query: String,
    onSearchChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WtaSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Medium)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = Strings.agentTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            WtaIconButton(icon = Icons.Outlined.Close, contentDescription = Strings.close, onClick = onClose)
        }
        AgentSearchField(query, onSearchChange, placeholder = Strings.agentDrawerSearchHint)
    }
}

@Composable
private fun SessionsTab(
    sessions: List<AgentSession>,
    isSearching: Boolean,
    currentId: String?,
    liveSessionId: String?,
    onPick: (String) -> Unit,
    onNew: () -> Unit,
    onDelete: (String) -> Unit,
    onPin: (String, Boolean) -> Unit,
    onRename: (String, String) -> Unit,
    onExport: (String) -> Unit
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
                title = if (isSearching) Strings.noSearchResults else Strings.agentEmptySessions,
                message = if (isSearching) null else Strings.agentEmptySessionsHint.ifDescriptionsShown(),
                icon = if (isSearching) Icons.Outlined.Search else Icons.Outlined.History
            )
            return
        }
        val pinned = sessions.filter { it.pinned }
        val recent = sessions.filter { !it.pinned }
        val grouped = !isSearching && pinned.isNotEmpty()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = WtaSpacing.ScreenHorizontal,
                vertical = WtaSpacing.Tiny
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val groups = if (grouped) {
                listOf(Strings.agentDrawerPinned to pinned, Strings.agentHomeRecentTitle to recent)
            } else listOf(null to sessions)
            groups.forEachIndexed { index, (label, entries) ->
                if (label != null && entries.isNotEmpty()) {
                    item(key = "session-group:$index") { DrawerGroupLabel(label) }
                }
                items(entries, key = { "session:${it.id}" }) { session ->
                    SessionRow(
                        session = session,
                        isCurrent = session.id == currentId,
                        isLive = session.id == liveSessionId,
                        onClick = { onPick(session.id) },
                        onPin = { onPin(session.id, !session.pinned) },
                        onRename = onRename,
                        onExport = onExport,
                        onDelete = onDelete,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerGroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(
            start = WtaSpacing.Small + 2.dp,
            top = WtaSpacing.Small,
            bottom = WtaSpacing.Tiny
        )
    )
}

@Composable
private fun SessionRow(
    session: AgentSession,
    isCurrent: Boolean,
    isLive: Boolean,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onRename: (String, String) -> Unit,
    onExport: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val titleText = session.title.ifBlank { Strings.agentHomeUntitledSession }
    var menuOpen by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var renameText by remember(session.id) { mutableStateOf(titleText) }

    val selectionColor by animateColorAsState(
        targetValue = if (isCurrent) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        animationSpec = WtaMotion.standardTween(),
        label = "active conversation"
    )
    WtaSettingRow(
        title = titleText,
        subtitle = formatSubtitle(session),
        modifier = modifier
            .clip(RoundedCornerShape(WtaRadius.Control))
            .background(selectionColor)
            .semantics { selected = isCurrent },
        titleMaxLines = 1,
        subtitleMaxLines = 1,
        iconContent = {

            when {
                isLive -> {
                    if (LocalAnimationSettings.current.enabled) WtaLoadingIndicator(size = WtaSize.IconSmall)
                    else Box(Modifier.size(WtaSpacing.Small).background(MaterialTheme.colorScheme.primary, CircleShape))
                }
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
        Box {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(WtaSize.TouchTarget)) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = Strings.more,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            WtaDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                WtaDropdownMenuItem(
                    text = if (session.pinned) Strings.agentSessionUnpin else Strings.agentSessionPin,
                    leadingIcon = if (session.pinned) Icons.Outlined.StarOutline else Icons.Outlined.Star,
                    onClick = {
                        menuOpen = false
                        onPin()
                    }
                )
                WtaDropdownMenuItem(
                    text = Strings.agentSessionRename,
                    leadingIcon = Icons.Outlined.Edit,
                    onClick = { menuOpen = false; renameText = titleText; showRenameDialog = true }
                )
                WtaDropdownMenuItem(
                    text = Strings.agentSessionExport,
                    leadingIcon = Icons.Outlined.Share,
                    onClick = { menuOpen = false; onExport(session.id) }
                )
                WtaDropdownMenuItem(
                    text = Strings.agentSessionDelete,
                    leadingIcon = Icons.Outlined.Delete,
                    onClick = { menuOpen = false; showDeleteDialog = true }
                )
            }
        }
    }

    if (showRenameDialog) {
        WtaAlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = Strings.agentSessionRenameTitle,
            content = {
                WtaTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    placeholder = Strings.agentSessionRenameHint,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                WtaButton(
                    onClick = {
                        onRename(session.id, renameText.trim())
                        showRenameDialog = false
                    },
                    text = Strings.btnOk,
                    variant = WtaButtonVariant.Primary,
                    size = WtaButtonSize.Small,
                    enabled = renameText.isNotBlank()
                )
            },
            dismissButton = {
                WtaButton(
                    onClick = { showRenameDialog = false },
                    text = Strings.cancel,
                    variant = WtaButtonVariant.Text,
                    size = WtaButtonSize.Small
                )
            }
        )
    }

    if (showDeleteDialog) {
        WtaAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = Strings.agentSessionDeleteConfirmTitle,
            text = Strings.agentSessionDeleteConfirmMessage,
            confirmButton = {
                WtaButton(
                    onClick = {
                        onDelete(session.id)
                        showDeleteDialog = false
                    },
                    text = Strings.agentSessionDelete,
                    variant = WtaButtonVariant.Tonal,
                    size = WtaButtonSize.Small
                )
            },
            dismissButton = {
                WtaButton(
                    onClick = { showDeleteDialog = false },
                    text = Strings.cancel,
                    variant = WtaButtonVariant.Text,
                    size = WtaButtonSize.Small
                )
            }
        )
    }
}

private fun formatSubtitle(session: AgentSession): String {
    val parts = mutableListOf<String>()
    parts += Strings.agentSessionMessagesShort.format(session.messages.size)
    parts += relativeTime(session.updatedAt)
    return parts.joinToString(" · ")
}

private fun relativeTime(timestamp: Long): String {
    val minutes = ((System.currentTimeMillis() - timestamp) / 60_000L).coerceAtLeast(0L)
    return when {
        minutes < 1L -> Strings.statsJustNow
        minutes < 60L -> Strings.statsMinutesAgo.format(minutes)
        minutes < 60L * 24L -> Strings.statsHoursAgo.format(minutes / 60L)
        minutes < 60L * 24L * 30L -> Strings.statsDaysAgo.format(minutes / (60L * 24L))
        else -> Strings.statsMonthsAgo.format(minutes / (60L * 24L * 30L))
    }
}

@Composable
private fun FilesTab(
    files: List<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo>,
    builtApks: List<com.webtoapp.core.agent.tool.BuiltApkInfo>,
    isSearching: Boolean,
    onPick: (String) -> Unit,
    onPickApk: (String) -> Unit,
    onCopyPath: (String) -> Unit,
    onOpenWith: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (files.isEmpty() && builtApks.isEmpty()) {
        WtaFullEmptyState(
            title = if (isSearching) Strings.noSearchResults else Strings.agentEmptyFiles,
            message = if (isSearching) null else Strings.agentEmptyFilesHint.ifDescriptionsShown(),
            icon = if (isSearching) Icons.Outlined.Search else Icons.Outlined.Folder
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = WtaSpacing.ScreenHorizontal,
            vertical = WtaSpacing.Tiny
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (builtApks.isNotEmpty()) {
            item(key = "__apk_header__") { DrawerGroupLabel(Strings.agentBuiltApks) }
            items(builtApks, key = { "apk_${it.appId}_${it.apkName}" }) { apk ->
                val apkVirtualPath = "apk:${apk.apkName}"
                FileEntryRow(
                    title = apk.apkName,
                    subtitle = "${apk.formatSize()} · ${apk.buildMode}",
                    icon = Icons.Outlined.Android,
                    onOpen = { onPickApk(apk.apkName) },
                    onOpenWith = { onOpenWith(apkVirtualPath) },
                    onCopyPath = { onCopyPath(apk.apkPath) },
                    onDelete = null,
                    modifier = Modifier.animateItem()
                )
            }
            if (files.isNotEmpty()) {
                item(key = "__files_header__") { DrawerGroupLabel(Strings.agentTabFiles) }
            }
        }
        items(files, key = { it.relativePath }) { f ->
            FileEntryRow(
                title = f.relativePath,
                subtitle = "${f.formatSize()} · ${f.formatTime()}",
                icon = Icons.Outlined.Code,
                onOpen = { onPick(f.relativePath) },
                onOpenWith = { onOpenWith(f.relativePath) },
                onCopyPath = { onCopyPath(f.relativePath) },
                onDelete = { onDelete(f.relativePath) },
                modifier = Modifier.animateItem()
            )
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
    onCopyPath: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    WtaSettingRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        modifier = modifier,
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
                WtaDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    WtaDropdownMenuItem(
                        text = Strings.agentFileOpen,
                        leadingIcon = Icons.Outlined.OpenInNew,
                        onClick = { menuOpen = false; onOpen() }
                    )
                    WtaDropdownMenuItem(
                        text = Strings.agentFileOpenWith,
                        leadingIcon = Icons.Outlined.Apps,
                        onClick = { menuOpen = false; onOpenWith() }
                    )
                    WtaDropdownMenuItem(
                        text = Strings.agentFileCopyPath,
                        leadingIcon = Icons.Outlined.ContentCopy,
                        onClick = { menuOpen = false; onCopyPath() }
                    )
                    if (onDelete != null) {
                        WtaDropdownMenuItem(
                            text = Strings.agentSessionDelete,
                            leadingIcon = Icons.Outlined.Delete,
                            onClick = { menuOpen = false; showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    )

    if (showDeleteDialog && onDelete != null) {
        WtaAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = Strings.agentFileDeleteConfirmTitle,
            text = Strings.agentFileDeleteConfirmMessage.format(title),
            confirmButton = {
                WtaButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    text = Strings.agentSessionDelete,
                    variant = WtaButtonVariant.Tonal,
                    size = WtaButtonSize.Small
                )
            },
            dismissButton = {
                WtaButton(
                    onClick = { showDeleteDialog = false },
                    text = Strings.cancel,
                    variant = WtaButtonVariant.Text,
                    size = WtaButtonSize.Small
                )
            }
        )
    }
}

private fun filterSessions(all: List<AgentSession>, query: String): List<AgentSession> {
    val ordered = compareByDescending<AgentSession> { it.pinned }.thenByDescending { it.updatedAt }
    if (query.isBlank()) return all.sortedWith(ordered)
    val q = query.trim().lowercase()
    return all.filter {
        it.title.lowercase().contains(q)
    }.sortedWith(ordered)
}

private fun filterFiles(
    all: List<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo>,
    query: String
): List<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo> {
    if (query.isBlank()) return all
    val q = query.trim().lowercase()
    return all.filter { it.relativePath.lowercase().contains(q) }
}
