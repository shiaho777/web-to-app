package com.webtoapp.ui.agent.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Web
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.agent.session.AgentSession
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaSectionDivider
import com.webtoapp.ui.design.WtaSettingRow
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.theme.LocalShowDescriptions
import java.util.Date

@Composable
internal fun AgentWelcome(
    sessions: List<AgentSession>,
    onPickSession: (String) -> Unit,
    onOpenSessions: () -> Unit,
    onFillComposer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val recentSessions = remember(sessions) {
        sessions.asSequence()
            .filter { it.messages.isNotEmpty() }
            .sortedByDescending { it.updatedAt }
            .take(3)
            .toList()
    }
    val starterTasks = listOf(
        StarterTask(Strings.agentStarterTodoLabel, Strings.agentStarterPrompt1, Icons.Outlined.Checklist),
        StarterTask(Strings.agentStarterPortfolioLabel, Strings.agentStarterPrompt2, Icons.Outlined.Web),
        StarterTask(Strings.agentStarterHtmlLabel, Strings.agentStarterPrompt3, Icons.Outlined.Code),
        StarterTask(Strings.agentStarterLandingLabel, Strings.agentStarterPrompt4, Icons.Outlined.Smartphone)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(
            start = WtaSpacing.ScreenHorizontal,
            end = WtaSpacing.ScreenHorizontal,
            top = 32.dp,
            bottom = WtaSpacing.Large
        ),
        verticalArrangement = Arrangement.spacedBy(
            WtaSpacing.ExtraLarge,
            if (recentSessions.isEmpty()) Alignment.CenterVertically else Alignment.Top
        )
    ) {
        item(key = "welcome") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = WtaAlpha.MutedContainer), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RocketLaunch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(WtaSize.IconLarge)
                    )
                }
                Text(
                    text = Strings.agentHomeTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                if (LocalShowDescriptions.current) {
                    Text(
                        text = Strings.agentHomeSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 350.dp)
                    )
                }
            }
        }

        item(key = "tasks") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(WtaSpacing.CardGap)
            ) {
                Text(Strings.agentHomeStartersTitle, style = MaterialTheme.typography.titleSmall)
                starterTasks.chunked(2).forEach { tasks ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.CardGap)
                    ) {
                        tasks.forEach { task ->
                            StarterTaskCard(
                                task = task,
                                onClick = { onFillComposer(task.prompt) },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }

        if (recentSessions.isNotEmpty()) {
            item(key = "recent-sessions") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            Strings.agentHomeRecentTitle,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        WtaButton(
                            onClick = onOpenSessions,
                            text = Strings.agentHomeRecentSeeAll,
                            variant = WtaButtonVariant.Text,
                            size = WtaButtonSize.Small
                        )
                    }
                    WtaCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        recentSessions.forEachIndexed { index, session ->
                            key(session.id) {
                                if (index > 0) WtaSectionDivider()
                                RecentSessionRow(session, onClick = { onPickSession(session.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class StarterTask(val label: String, val prompt: String, val icon: ImageVector)

@Composable
private fun StarterTaskCard(
    task: StarterTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    WtaCard(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = WtaSpacing.Medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = task.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(WtaSize.Icon)
            )
            Text(
                text = task.label,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RecentSessionRow(session: AgentSession, onClick: () -> Unit) {
    val context = LocalContext.current
    val timeText = remember(context, session.updatedAt, Strings.lang) {
        val date = Date(session.updatedAt)
        val dateText = android.text.format.DateFormat.getDateFormat(context).format(date)
        val clockText = android.text.format.DateFormat.getTimeFormat(context).format(date)
        "$dateText $clockText"
    }
    WtaSettingRow(
        title = session.title.ifBlank { Strings.agentHomeUntitledSession },
        subtitle = "${Strings.agentSessionMessagesShort.format(session.messages.size)} · $timeText",
        icon = Icons.Outlined.History,
        showIconPlate = false,
        titleMaxLines = 1,
        subtitleMaxLines = 1,
        titleStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(WtaSize.IconSmall)
        )
    }
}
