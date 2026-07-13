package com.webtoapp.ui.aicoding

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import com.webtoapp.core.aicoding.skill.Skill
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.aicoding.components.ChoiceBottomSheet
import com.webtoapp.ui.aicoding.components.Composer
import com.webtoapp.ui.aicoding.components.MaterialIconBadgeRound
import com.webtoapp.ui.aicoding.components.MessageActions
import com.webtoapp.ui.aicoding.components.MessageBubble
import com.webtoapp.ui.aicoding.components.PermissionDialog
import com.webtoapp.ui.aicoding.components.PreviewSheet
import com.webtoapp.ui.aicoding.components.SkillDrawer
import com.webtoapp.ui.aicoding.components.StreamingBubble
import com.webtoapp.ui.aicoding.components.TodoChecklist
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaStatusBanner
import com.webtoapp.ui.design.WtaStatusTone
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import kotlinx.coroutines.launch

@Composable
fun AiCodingScreen(
    onBack: () -> Unit,
    onOpenAiSettings: () -> Unit,

    onOpenSkillEditor: (skillName: String?) -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val vm: AiCodingViewModel = viewModel(factory = AiCodingViewModel.factory(app))
    val state by vm.ui.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var drawerMounted by remember { mutableStateOf(false) }

    val leaveScreen: () -> Unit = {
        scope.launch {
            if (state.previewOpen) {
                vm.closePreview()
                return@launch
            }
            if (state.drawerOpen ||
                drawerState.currentValue != DrawerValue.Closed
            ) {
                if (state.drawerOpen) vm.closeDrawer()
                runCatching { drawerState.close() }
                return@launch
            }
            onBack()
        }
        Unit
    }
    BackHandler { leaveScreen() }

    LaunchedEffect(state.error) { state.error?.let { snackbar.showSnackbar(it); vm.dismissBanner() } }
    LaunchedEffect(state.info) { state.info?.let { snackbar.showSnackbar(it); vm.dismissBanner() } }
    LaunchedEffect(state.drawerOpen) {
        if (state.drawerOpen) {

            drawerMounted = true
            drawerState.open()
        } else {

            if (drawerState.currentValue != DrawerValue.Closed) {
                drawerState.close()
            }
            drawerMounted = false
        }
    }
    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed && state.drawerOpen) vm.closeDrawer()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.reloadUserSkills()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val fileManager = remember { com.webtoapp.core.aicoding.files.ProjectFileManager(context) }
    val previewable = remember(state.projectFiles, state.previewFilePath) {
        state.previewFilePath != null || state.projectFiles.any {
            it.relativePath.endsWith(".html") ||
                it.relativePath.endsWith(".png") ||
                it.relativePath.endsWith(".jpg")
        }
    }

    val saveable = remember(state.projectFiles) { state.projectFiles.isNotEmpty() }

    val messageActions = remember(vm) {
        MessageActions(
            onCopy = { msg ->
                clipboard.setText(AnnotatedString(msg.content))
                scope.launch { snackbar.showSnackbar(Strings.aiCodingMessageCopied) }
            },
            onEdit = vm::startEditing,
            onRegenerate = { msg -> vm.regenerate(msg.id) },
            onDeleteFromHere = { msg -> vm.deleteFromMessage(msg.id) }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {

            if (drawerMounted) {
                ModalDrawerSheet(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(320.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    SkillDrawer(
                        state = state,
                        onTabChange = vm::setDrawerTab,
                        onSearchChange = vm::setDrawerSearch,
                        onPickSession = { id ->
                            scope.launch { drawerState.close() }
                            vm.selectSession(id)
                        },
                        onNewSession = {
                            scope.launch { drawerState.close() }
                            vm.tryNewSession()
                        },
                        onDeleteSession = vm::deleteSession,
                        onPinSession = vm::pinSession,
                        onPickFile = { path ->
                            scope.launch { drawerState.close() }
                            vm.selectFile(path)
                        },
                        onPickSkill = { skill ->
                            scope.launch { drawerState.close() }
                            vm.pickSkill(skill)
                        },
                        onCreateSkill = {
                            scope.launch { drawerState.close() }
                            onOpenSkillEditor(null)
                        },
                        onEditSkill = { skill ->
                            scope.launch { drawerState.close() }
                            onOpenSkillEditor(skill.name)
                        }
                    )
                }
            }
        }
    ) {
        WtaScreen(
            title = state.currentSession?.title?.takeIf { it.isNotBlank() } ?: Strings.aiCodingTitle,
            snackbarHostState = snackbar,
            onBack = leaveScreen,
            actions = {
                WtaIconButton(
                    onClick = vm::openDrawer,
                    icon = Icons.Outlined.Menu,
                    contentDescription = Strings.aiCodingOpenDrawer
                )
                WtaIconButton(
                    onClick = vm::tryNewSession,
                    icon = Icons.Outlined.Add,
                    contentDescription = Strings.aiCodingNewSession
                )
                if (previewable) {
                    WtaIconButton(
                        onClick = vm::openPreview,
                        icon = Icons.Outlined.PlayArrow,
                        contentDescription = Strings.aiCodingPreviewOpen
                    )
                }
                if (saveable) {
                    WtaIconButton(
                        onClick = vm::openSaveAsAppDialog,
                        icon = Icons.Outlined.Apps,
                        contentDescription = Strings.aiCodingSaveAsApp
                    )
                }
                var menuOpen by remember { mutableStateOf(false) }
                WtaIconButton(
                    onClick = { menuOpen = true },
                    icon = Icons.Outlined.MoreVert,
                    contentDescription = Strings.more
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(Strings.aiSettings) },
                        leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        onClick = { menuOpen = false; onOpenAiSettings() }
                    )
                }
            }
        ) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                if (state.planActive) {
                    PlanBanner(
                        planFilePath = state.planFilePath,
                        onExit = {
                            state.slashCommands.firstOrNull { it.id == "exit-plan" }
                                ?.let(vm::runSlashCommand)
                        }
                    )
                }
                Conversation(
                    state = state,
                    actions = messageActions,
                    onPickSkill = vm::pickSkill,
                    onPickSession = vm::selectSession,
                    onOpenSkills = { vm.openDrawer(AiCodingUiState.DrawerTab.Skills) },
                    onOpenSessions = { vm.openDrawer(AiCodingUiState.DrawerTab.Sessions) },
                    onFillComposer = vm::setComposerText,
                    modifier = Modifier.weight(1f)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WtaAlpha.Divider)
                )

                state.pendingPlanReview?.let { review ->
                    PlanReviewCard(
                        review = review,
                        onApprove = vm::approvePlan,
                        onRequestRevisions = vm::rejectPlan
                    )
                }

                com.webtoapp.ui.aicoding.components.ChangesReviewCard(
                    changes = state.pendingChanges,
                    expanded = state.changesReviewExpanded,
                    onToggle = vm::toggleChangesReview,
                    onUndoOne = vm::undoChange,
                    onUndoAll = vm::undoAllChanges,
                    onClear = vm::clearChangesReview
                )
                if (state.editingMessageId != null) {
                    EditingHint(onCancel = vm::cancelEditing)
                }
                Composer(
                    state = state,
                    onTextChange = vm::setComposerText,
                    onSend = vm::send,
                    onCancel = vm::cancelTurn,
                    onPickSkill = vm::pickSkill,
                    onRunSlashCommand = vm::runSlashCommand,
                    onDismissSlash = vm::dismissSlash,
                    onPickMention = vm::pickFileMention,
                    onDismissMention = vm::dismissMention,
                    onToggleAutoApprove = { vm.setAutoApprove(!state.autoApprove) },

                    onTriggerSlash = { vm.setComposerText("/") },

                    onOpenModelPicker = vm::openModelPicker,
                    onCompactContext = vm::compactNow
                )
            }
        }
    }

    PreviewSheet(
        visible = state.previewOpen,
        state = state,
        fileManager = fileManager,
        onDismiss = vm::closePreview,
        onSelectFile = { path -> vm.setPreviewFile(path) }
    )

    state.pendingPermission?.let { req ->

        if (!state.autoApprove) {
            PermissionDialog(req, onResponse = { vm.answerPermission(req.toolCallId, it) })
        }
    }
    state.pendingChoice?.let { req ->
        ChoiceBottomSheet(req, onResponse = { vm.answerChoice(req.id, it) })
    }
    if (state.saveAsAppDialogOpen) {
        com.webtoapp.ui.aicoding.components.SaveAsAppDialog(
            artifacts = state.detectedArtifacts,
            selectedArtifactId = state.selectedArtifactId,
            inFlight = state.saveAsAppInFlight,
            onSelectArtifact = vm::selectArtifact,
            onConfirm = vm::saveSelectedArtifact,
            onDismiss = vm::closeSaveAsAppDialog
        )
    }
    if (state.modelPickerOpen) {
        com.webtoapp.ui.aicoding.components.ModelPickerDialog(
            groups = state.modelProviderGroups,
            initialSelectedProviderKeyId = state.selectedProviderKeyId,
            onSelect = vm::selectModel,
            onDismiss = vm::dismissModelPicker
        )
    }
}

@Composable
private fun EditingHint(onCancel: () -> Unit) {
    Box(
        modifier = Modifier.padding(
            horizontal = WtaSpacing.ScreenHorizontal,
            vertical = WtaSpacing.Small
        )
    ) {
        WtaStatusBanner(
            message = Strings.aiCodingEditingHeader,
            tone = WtaStatusTone.Info,
            actionLabel = Strings.aiCodingEditingCancel,
            onAction = onCancel
        )
    }
}

@Composable
private fun PlanBanner(planFilePath: String?, onExit: () -> Unit) {
    Box(
        modifier = Modifier.padding(
            horizontal = WtaSpacing.ScreenHorizontal,
            vertical = WtaSpacing.Small
        )
    ) {
        WtaStatusBanner(
            title = Strings.aiCodingPlanModeBadge,
            message = planFilePath ?: Strings.aiCodingPlanModeNoPath,
            tone = WtaStatusTone.Warning,
            actionLabel = Strings.aiCodingPlanModeExitTooltip,
            onAction = onExit
        )
    }
}

@Composable
private fun PlanReviewCard(
    review: PlanReview,
    onApprove: () -> Unit,
    onRequestRevisions: () -> Unit
) {
    Box(
        modifier = Modifier.padding(
            horizontal = WtaSpacing.ScreenHorizontal,
            vertical = WtaSpacing.Tiny + 2.dp
        )
    ) {
        WtaCard(
            tone = WtaCardTone.Elevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(WtaSpacing.Medium)
            ) {
                Text(
                    text = Strings.aiCodingPlanAwaitingReview,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(WtaSpacing.Small))
                Text(
                    text = review.content,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WtaSpacing.Small),
                    horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                ) {
                    WtaButton(
                        onClick = onApprove,
                        text = Strings.aiCodingPlanApprove,
                        variant = WtaButtonVariant.Tonal,
                        size = WtaButtonSize.Small,
                        modifier = Modifier.weight(1f)
                    )
                    WtaButton(
                        onClick = onRequestRevisions,
                        text = Strings.aiCodingPlanRequestRevisions,
                        variant = WtaButtonVariant.Text,
                        size = WtaButtonSize.Small
                    )
                }
            }
        }
    }
}

@Composable
private fun Conversation(
    state: AiCodingUiState,
    actions: MessageActions,
    onPickSkill: (Skill) -> Unit,
    onPickSession: (String) -> Unit,
    onOpenSkills: () -> Unit,
    onOpenSessions: () -> Unit,
    onFillComposer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val messages = state.currentSession?.messages.orEmpty()

    var followBottom by remember { mutableStateOf(true) }
    val atBottom by remember {
        derivedStateOf { !listState.canScrollForward }
    }
    val totalContentItems = messages.size +
        (if (state.todos.isNotEmpty()) 1 else 0) +
        (if (state.isWorking) 1 else 0)

    val userScrollMonitor = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (source == NestedScrollSource.UserInput) {

                    if (available.y > 2f) {
                        followBottom = false
                    }
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    LaunchedEffect(atBottom) {

        if (atBottom) followBottom = true
    }

    val streamingTextLen = state.streamingText.length
    val streamingThinkingLen = state.streamingThinking.length
    LaunchedEffect(
        followBottom,
        messages.size,
        streamingTextLen,
        streamingThinkingLen,
        state.pendingToolCalls.size,
        state.todos.size,
        state.isWorking
    ) {
        if (!followBottom) return@LaunchedEffect
        if (totalContentItems <= 0) return@LaunchedEffect

        repeat(2) {
            val info = listState.layoutInfo
            val viewportBottom = info.viewportEndOffset - info.afterContentPadding
            val last = info.visibleItemsInfo.lastOrNull()
            if (last != null) {
                val lastBottom = last.offset + last.size
                val gap = lastBottom - viewportBottom
                if (gap > 0) {
                    listState.scrollBy(gap.toFloat())
                }
            }

            kotlinx.coroutines.yield()
        }
    }

    if (messages.isEmpty() && !state.isWorking && state.todos.isEmpty()) {

        EmptyConversationHint(
            state = state,
            onPickSkill = onPickSkill,
            onPickSession = onPickSession,
            onOpenSkills = onOpenSkills,
            onOpenSessions = onOpenSessions,
            onFillComposer = onFillComposer,
            modifier = modifier
        )
        return
    }

    val coroutineScope = rememberCoroutineScope()
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(userScrollMonitor),
            contentPadding = PaddingValues(WtaSpacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(message = msg, actions = actions)
            }
            if (state.todos.isNotEmpty()) {
                item(key = "todos") {
                    TodoChecklist(state.todos)
                }
            }
            if (state.isWorking) {
                item(key = "streaming") {
                    StreamingBubble(
                        text = state.streamingText,
                        thinking = state.streamingThinking,
                        thinkingStartedAt = state.streamingThinkingStartedAt,
                        thinkingFrozenDurationMs = state.streamingThinkingDurationMs,
                        pendingTools = state.pendingToolCalls,
                        activity = state.currentActivity
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !atBottom,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = WtaSpacing.Medium)
        ) {
            JumpToLatestPill(
                onClick = {
                    coroutineScope.launch {
                        followBottom = true
                        if (totalContentItems > 0) {
                            val targetIndex = totalContentItems - 1
                            if (listState.layoutInfo.visibleItemsInfo.none { it.index == targetIndex }) {
                                listState.scrollToItem(targetIndex)
                            }
                            val info = listState.layoutInfo
                            val viewportBottom =
                                info.viewportEndOffset - info.afterContentPadding
                            val last = info.visibleItemsInfo.lastOrNull()
                            if (last != null) {
                                val gap = (last.offset + last.size) - viewportBottom
                                if (gap > 0) listState.animateScrollBy(gap.toFloat())
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun JumpToLatestPill(onClick: () -> Unit) {
    WtaCard(
        onClick = onClick,
        tone = WtaCardTone.Highlighted,
        contentPadding = PaddingValues(
            horizontal = WtaSpacing.Medium,
            vertical = WtaSpacing.Small
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.ArrowDownward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(WtaSize.IconSmall)
            )
            Spacer(Modifier.width(WtaSpacing.Tiny + 2.dp))
            Text(
                text = Strings.aiCodingJumpToLatest,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EmptyConversationHint(
    state: AiCodingUiState,
    onPickSkill: (Skill) -> Unit,
    onPickSession: (String) -> Unit,
    onOpenSkills: () -> Unit,
    onOpenSessions: () -> Unit,
    onFillComposer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredSkills = remember(state.skills) { featuredSkills(state.skills) }
    val recentSessions = remember(state.sessions) {
        state.sessions
            .filter { it.messages.isNotEmpty() }
            .sortedByDescending { it.updatedAt }
            .take(3)
    }
    val starterPrompts = remember {
        listOf(
            Strings.aiCodingStarterPrompt1,
            Strings.aiCodingStarterPrompt2,
            Strings.aiCodingStarterPrompt3,
            Strings.aiCodingStarterPrompt4
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = WtaSpacing.ScreenHorizontal,
            vertical = WtaSpacing.ExtraLarge
        ),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.SectionGap)
    ) {
        item("hero") {
            Column {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(WtaSize.IconLarge)
                )
                Spacer(Modifier.height(WtaSpacing.Small + 2.dp))
                Text(
                    text = Strings.aiCodingHomeTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(WtaSpacing.Tiny))
                Text(
                    text = Strings.aiCodingHomeSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item("starters-header") {
            SectionHeader(
                title = Strings.aiCodingHomeStartersTitle,
                actionLabel = null,
                onAction = null
            )
        }
        item("starters-grid") {
            StarterPromptsRow(prompts = starterPrompts, onPick = onFillComposer)
        }

        if (featuredSkills.isNotEmpty()) {
            item("skills-header") {
                SectionHeader(
                    title = Strings.aiCodingHomeSkillsTitle,
                    actionLabel = Strings.aiCodingHomeSkillsBrowseAll,
                    onAction = onOpenSkills
                )
            }
            item("skills-grid") {
                SkillChipsRow(skills = featuredSkills, onPick = onPickSkill)
            }
        }

        if (recentSessions.isNotEmpty()) {
            item("sessions-header") {
                SectionHeader(
                    title = Strings.aiCodingHomeRecentTitle,
                    actionLabel = Strings.aiCodingHomeRecentSeeAll,
                    onAction = onOpenSessions
                )
            }
            items(recentSessions, key = { "session-${it.id}" }) { session ->
                RecentSessionRow(session = session, onClick = { onPickSession(session.id) })
            }
        }
    }
}

private fun featuredSkills(skills: List<Skill>) =
    skills
        .filter { it.source == Skill.Source.Bundled }
        .let { bundled ->
            val byCategory = bundled.groupBy { it.category }
            val ordered = mutableListOf<Skill>()
            for (cat in Skill.Category.entries) {
                ordered += byCategory[cat].orEmpty().take(2)
            }
            (ordered + bundled).distinctBy { it.name }.take(6)
        }

@Composable
private fun SectionHeader(title: String, actionLabel: String?, onAction: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(WtaSpacing.Tiny)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillChipsRow(skills: List<Skill>, onPick: (Skill) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        skills.forEach { skill ->
            WtaCard(
                onClick = { onPick(skill) },
                tone = WtaCardTone.Elevated,
                contentPadding = PaddingValues(
                    horizontal = WtaSpacing.Small + 2.dp,
                    vertical = WtaSpacing.Small
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MaterialIconBadgeRound(
                        name = skill.icon,
                        tintHex = skill.iconColor,
                        size = WtaSize.IconSmall + 4.dp
                    )
                    Spacer(Modifier.width(WtaSpacing.Tiny + 2.dp))
                    Text(
                        text = "/${skill.name}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StarterPromptsRow(prompts: List<String>, onPick: (String) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
    ) {
        prompts.forEach { prompt ->
            WtaCard(
                onClick = { onPick(prompt) },
                tone = WtaCardTone.Elevated,
                contentPadding = PaddingValues(
                    horizontal = WtaSpacing.Medium,
                    vertical = WtaSpacing.Small + 2.dp
                )
            ) {
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun RecentSessionRow(
    session: com.webtoapp.core.aicoding.session.AgentSession,
    onClick: () -> Unit
) {
    WtaCard(
        onClick = onClick,
        tone = WtaCardTone.Elevated,
        contentPadding = PaddingValues(
            horizontal = WtaSpacing.Medium + 2.dp,
            vertical = WtaSpacing.Medium
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title.ifBlank { Strings.aiCodingHomeUntitledSession },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                val parts = buildList {
                    if (!session.activeSkillName.isNullOrBlank()) add("/${session.activeSkillName}")
                    add(Strings.aiCodingSessionMessagesShort.format(session.messages.size))
                }
                Text(
                    text = parts.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(WtaSize.IconSmall + 2.dp)
            )
        }
    }
}
