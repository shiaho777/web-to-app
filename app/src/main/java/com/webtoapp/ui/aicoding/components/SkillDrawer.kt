package com.webtoapp.ui.aicoding.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import com.webtoapp.core.aicoding.session.AgentSession
import com.webtoapp.core.aicoding.skill.Skill
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.aicoding.AiCodingUiState
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
fun SkillDrawer(
    state: AiCodingUiState,
    onTabChange: (AiCodingUiState.DrawerTab) -> Unit,
    onSearchChange: (String) -> Unit,
    onPickSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onPinSession: (String, Boolean) -> Unit,
    onPickFile: (String) -> Unit,
    onPickSkill: (Skill) -> Unit,
    onPickPlan: (String) -> Unit,

    onCreateSkill: () -> Unit = {},

    onEditSkill: (Skill) -> Unit = {}
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
            AiCodingUiState.DrawerTab.entries.forEachIndexed { i, tab ->
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
                                AiCodingUiState.DrawerTab.Sessions -> Icons.Outlined.History
                                AiCodingUiState.DrawerTab.Files -> Icons.Outlined.Folder
                                AiCodingUiState.DrawerTab.Skills -> Icons.Outlined.AutoAwesome
                                AiCodingUiState.DrawerTab.Plans -> Icons.AutoMirrored.Outlined.FactCheck
                            },
                            contentDescription = null,
                            modifier = Modifier.size(WtaSize.Icon)
                        )
                    }
                )
            }
        }
        when (state.drawerTab) {
            AiCodingUiState.DrawerTab.Sessions -> SessionsTab(
                sessions = filterSessions(state.sessions, state.drawerSearch),
                currentId = state.currentSession?.id,
                liveSessionId = if (state.isWorking) state.currentSession?.id else null,
                onPick = onPickSession,
                onNew = onNewSession,
                onDelete = onDeleteSession,
                onPin = onPinSession
            )
            AiCodingUiState.DrawerTab.Files -> FilesTab(
                files = state.projectFiles,
                onPick = onPickFile
            )
            AiCodingUiState.DrawerTab.Skills -> SkillsTab(
                skills = filterSkills(state.skills, state.drawerSearch),
                onPick = onPickSkill,
                onCreate = onCreateSkill,
                onEdit = onEditSkill
            )
            AiCodingUiState.DrawerTab.Plans -> PlansTab(
                sessions = state.sessions,
                onPick = onPickPlan
            )
        }
    }
}

private fun AiCodingUiState.DrawerTab.label() = when (this) {
    AiCodingUiState.DrawerTab.Sessions -> Strings.aiCodingTabSessions
    AiCodingUiState.DrawerTab.Files -> Strings.aiCodingTabFiles
    AiCodingUiState.DrawerTab.Skills -> Strings.aiCodingTabSkills
    AiCodingUiState.DrawerTab.Plans -> Strings.aiCodingTabPlans
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
            text = Strings.aiCodingTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        WtaTextField(
            value = query,
            onValueChange = onSearchChange,
            placeholder = Strings.aiCodingDrawerSearchHint,
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
                text = Strings.aiCodingNewSession,
                variant = WtaButtonVariant.Primary,
                size = WtaButtonSize.Medium,
                leadingIcon = Icons.Outlined.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (sessions.isEmpty()) {
            WtaFullEmptyState(
                title = Strings.aiCodingEmptySessions,
                message = Strings.aiCodingEmptySessionsHint,
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
    val titleText = session.title.ifBlank { Strings.aiCodingHomeUntitledSession }
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
            contentDescription = Strings.aiCodingSessionPin,
            modifier = Modifier.size(WtaSize.TouchTarget)
        )
        WtaIconButton(
            onClick = onDelete,
            icon = Icons.Outlined.Delete,
            contentDescription = Strings.aiCodingSessionDelete,
            modifier = Modifier.size(WtaSize.TouchTarget)
        )
    }
}

private fun formatSubtitle(session: AgentSession): String {
    val parts = mutableListOf<String>()
    if (!session.activeSkillName.isNullOrBlank()) parts += "/${session.activeSkillName}"
    parts += Strings.aiCodingSessionMessagesShort.format(session.messages.size)
    parts += DRAWER_TIME.format(Date(session.updatedAt))
    return parts.joinToString(" · ")
}

private val DRAWER_TIME = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

@Composable
private fun FilesTab(
    files: List<com.webtoapp.core.aicoding.files.ProjectFileManager.FileInfo>,
    onPick: (String) -> Unit
) {
    if (files.isEmpty()) {
        WtaFullEmptyState(
            title = Strings.aiCodingEmptyFiles,
            message = Strings.aiCodingEmptyFilesHint,
            icon = Icons.Outlined.Folder
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(files, key = { it.relativePath }) { f ->
            WtaSettingRow(
                title = f.relativePath,
                subtitle = "${f.formatSize()} · ${f.formatTime()}",
                icon = Icons.Outlined.Code,
                onClick = { onPick(f.relativePath) }
            )
            WtaSectionDivider()
        }
    }
}

@Composable
private fun SkillsTab(
    skills: List<Skill>,
    onPick: (Skill) -> Unit,
    onCreate: () -> Unit,
    onEdit: (Skill) -> Unit
) {
    if (skills.isEmpty()) {
        WtaFullEmptyState(
            title = Strings.aiCodingEmptySkills,
            message = Strings.aiCodingEmptySkillsHint,
            icon = Icons.Outlined.AutoAwesome,
            action = {
                WtaButton(
                    onClick = onCreate,
                    text = Strings.aiCodingNewSkill,
                    variant = WtaButtonVariant.Primary,
                    size = WtaButtonSize.Small,
                    leadingIcon = Icons.Outlined.Add
                )
            }
        )
        return
    }
    val grouped = remember(skills) { skills.groupBy { it.category } }
    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item(key = "create-skill") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WtaSpacing.ScreenHorizontal,
                        vertical = WtaSpacing.Small
                    )
            ) {
                WtaButton(
                    onClick = onCreate,
                    text = Strings.aiCodingNewSkill,
                    variant = WtaButtonVariant.Tonal,
                    size = WtaButtonSize.Small,
                    leadingIcon = Icons.Outlined.Add,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            WtaSectionDivider()
        }
        Skill.Category.entries.forEach { category ->
            val list = grouped[category].orEmpty()
            if (list.isEmpty()) return@forEach
            item(key = "header-$category") {
                Text(
                    text = categoryLabel(category),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = WtaSpacing.ScreenHorizontal,
                        vertical = WtaSpacing.Medium
                    )
                )
            }
            items(list, key = { "skill-${it.name}" }) { skill ->
                SkillRow(
                    skill = skill,
                    onClick = { onPick(skill) },

                    onEdit = if (skill.source == Skill.Source.User) {
                        { onEdit(skill) }
                    } else null
                )
                WtaSectionDivider()
            }
        }
    }
}

@Composable
private fun SkillRow(
    skill: Skill,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    WtaSettingRow(
        title = "/${skill.name}",
        subtitle = skill.description.takeIf { it.isNotBlank() },
        iconContent = {
            MaterialIconGlyph(
                name = skill.icon,
                tintHex = skill.iconColor,
                size = WtaSize.Icon
            )
        },
        onClick = onClick
    ) {

        if (skill.pinned) {
            Icon(
                Icons.Outlined.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(WtaSize.IconSmall)
            )
        }
        if (onEdit != null) {
            Spacer(Modifier.width(WtaSpacing.Tiny))
            WtaIconButton(
                onClick = onEdit,
                icon = Icons.Outlined.Edit,
                contentDescription = Strings.aiCodingEditSkill,
                modifier = Modifier.size(WtaSize.TouchTarget)
            )
        }
    }
}

private fun categoryLabel(c: Skill.Category) = when (c) {
    Skill.Category.App -> Strings.aiCodingCategoryApp
    Skill.Category.Module -> Strings.aiCodingCategoryModule
    Skill.Category.Tool -> Strings.aiCodingCategoryTool
    Skill.Category.Custom -> Strings.aiCodingCategoryCustom
}

@Composable
private fun PlansTab(sessions: List<AgentSession>, onPick: (String) -> Unit) {
    val plans = sessions.flatMap { s -> s.planSlugs.map { slug -> s to slug } }
    if (plans.isEmpty()) {
        WtaFullEmptyState(
            title = Strings.aiCodingEmptyPlans,
            message = Strings.aiCodingEmptyPlansHint,
            icon = Icons.Outlined.Inbox
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(plans) { (session, slug) ->
            WtaSettingRow(
                title = slug,
                subtitle = session.title.ifBlank { Strings.aiCodingHomeUntitledSession },
                icon = Icons.AutoMirrored.Outlined.FactCheck,
                onClick = { onPick("${session.id}::${slug}") }
            )
            WtaSectionDivider()
        }
    }
}

private fun filterSessions(all: List<AgentSession>, query: String): List<AgentSession> {
    val ordered = compareByDescending<AgentSession> { it.pinned }.thenByDescending { it.updatedAt }
    if (query.isBlank()) return all.sortedWith(ordered)
    val q = query.lowercase()
    return all.filter {
        it.title.lowercase().contains(q) ||
            (it.activeSkillName?.lowercase()?.contains(q) == true)
    }.sortedWith(ordered)
}

private fun filterSkills(all: List<Skill>, query: String): List<Skill> {
    if (query.isBlank()) return all
    val q = query.lowercase()
    return all.filter {
        it.name.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.whenToUse.lowercase().contains(q)
    }
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
