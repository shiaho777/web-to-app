package com.webtoapp.ui.agent

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.agent.components.ChoiceBottomSheet
import com.webtoapp.ui.agent.components.Composer
import com.webtoapp.ui.agent.components.AgentContextSheet
import com.webtoapp.ui.agent.components.AgentToolsSheet
import com.webtoapp.ui.agent.components.AgentWelcome
import com.webtoapp.ui.agent.components.MarkdownText
import com.webtoapp.ui.agent.components.MessageActions
import com.webtoapp.ui.agent.components.formatMessageForCopy
import com.webtoapp.ui.agent.components.MessageBubble
import com.webtoapp.ui.agent.components.PermissionDialog
import com.webtoapp.ui.agent.components.PreviewSheet
import com.webtoapp.ui.agent.components.AgentDrawer
import com.webtoapp.ui.agent.components.StreamingBubble
import com.webtoapp.ui.agent.components.TodoChecklist
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaDropdownMenu
import com.webtoapp.ui.design.WtaDropdownMenuItem
import com.webtoapp.ui.design.WtaFadeIn
import com.webtoapp.ui.design.WtaFadeOut
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaStatusBanner
import com.webtoapp.ui.design.WtaStatusTone
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import kotlinx.coroutines.launch

@Composable
fun AgentScreen(
    onBack: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onAddModel: () -> Unit,
    onOpenApp: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val vm: AgentViewModel = viewModel(factory = AgentViewModel.factory(app))
    val state by vm.ui.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var drawerMounted by remember { mutableStateOf(false) }
    var actionsOpen by remember { mutableStateOf(false) }
    var toolsOpen by rememberSaveable { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> vm.attachUris(uris) }
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> vm.attachUris(uris) }
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> uri?.let { vm.attachFolder(it) } }
    val skillPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(vm::importSkill) }

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
            when {
                state.slashOpen -> vm.dismissSlash()
                state.mentionPickerOpen -> vm.dismissMention()
                else -> onBack()
            }
        }
        Unit
    }
    BackHandler { leaveScreen() }

    LaunchedEffect(state.pendingPlanReview) {
        if (state.pendingPlanReview != null) focusManager.clearFocus()
    }
    LaunchedEffect(state.error, state.contextPickerOpen) {
        if (!state.contextPickerOpen) {
            state.error?.let {
                snackbar.showSnackbar(it)
                vm.dismissBanner()
            }
        }
    }
    LaunchedEffect(state.info) { state.info?.let { snackbar.showSnackbar(it); vm.dismissBanner() } }
    LaunchedEffect(state.drawerOpen) {
        if (state.drawerOpen) {
            focusManager.clearFocus()
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

    val fileManager = remember { com.webtoapp.core.agent.files.ProjectFileManager(context) }
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
            onCopy = { msg, includeDetails ->
                val text = formatMessageForCopy(msg, includeDetails)
                clipboard.setText(AnnotatedString(text))
                scope.launch { snackbar.showSnackbar(Strings.agentMessageCopied) }
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
                    AgentDrawer(
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
                        onRenameSession = vm::renameSession,
                        onExportSession = { id ->
                            scope.launch { drawerState.close() }
                            vm.exportSession(id)
                        },
                        onPickFile = { path ->
                            scope.launch { drawerState.close() }
                            vm.selectFile(path)
                            vm.openPreview()
                        },
                        onCopyFilePath = { path ->
                            clipboard.setText(AnnotatedString(path))
                            scope.launch { snackbar.showSnackbar(Strings.agentPathCopied) }
                        },
                        onOpenWith = { path ->
                            scope.launch { drawerState.close() }
                            vm.openWithSystemChooser(path)
                        },
                        onDeleteFile = vm::deleteSessionFile
                    )
                }
            }
        }
    ) {
        WtaScreen(
            title = state.currentSession?.title?.takeIf { it.isNotBlank() } ?: Strings.agentTitle,
            snackbarHostState = snackbar,
            onBack = leaveScreen,
            actions = {
                WtaIconButton(
                    onClick = vm::openDrawer,
                    icon = Icons.Outlined.History,
                    contentDescription = Strings.agentOpenDrawer
                )
                WtaIconButton(
                    onClick = vm::tryNewSession,
                    icon = Icons.Outlined.Add,
                    contentDescription = Strings.agentNewSession,
                    tonal = true
                )
                val approvalMode = if (state.autoApprove) Strings.agentAutoModeLabel else Strings.agentManualModeLabel
                Box {
                    WtaIconButton(
                        onClick = { actionsOpen = true },
                        icon = if (state.autoApprove) Icons.Outlined.Bolt else Icons.Outlined.MoreVert,
                        contentDescription = "${Strings.moreOptions} · $approvalMode"
                    )
                    WtaDropdownMenu(expanded = actionsOpen, onDismissRequest = { actionsOpen = false }) {
                        if (previewable) {
                            WtaDropdownMenuItem(
                                text = Strings.agentPreviewOpen,
                                leadingIcon = Icons.Outlined.PlayArrow,
                                onClick = {
                                    actionsOpen = false
                                    vm.openPreview()
                                }
                            )
                        }
                        WtaDropdownMenuItem(
                            text = Strings.agentToolsTitle,
                            leadingIcon = Icons.Outlined.Build,
                            onClick = {
                                actionsOpen = false
                                focusManager.clearFocus()
                                toolsOpen = true
                            }
                        )
                        WtaDropdownMenuItem(
                            text = if (state.autoApprove) Strings.agentManualModeLabel else Strings.agentAutoModeLabel,
                            leadingIcon = if (state.autoApprove) Icons.Outlined.Shield else Icons.Outlined.Bolt,
                            onClick = {
                                actionsOpen = false
                                vm.setAutoApprove(!state.autoApprove)
                            }
                        )
                        WtaDropdownMenuItem(
                            text = Strings.agentSlashChipLabel,
                            leadingIcon = Icons.Outlined.Terminal,
                            onClick = {
                                actionsOpen = false
                                vm.openSlashCommands()
                            }
                        )
                        if (saveable) {
                            WtaDropdownMenuItem(
                                text = Strings.agentSaveAsApp,
                                leadingIcon = Icons.Outlined.Apps,
                                onClick = {
                                    actionsOpen = false
                                    vm.openSaveAsAppDialog()
                                }
                            )
                        }
                        WtaDropdownMenuItem(
                            text = Strings.aiSettings,
                            leadingIcon = Icons.Outlined.Settings,
                            onClick = {
                                actionsOpen = false
                                onOpenAiSettings()
                            }
                        )
                    }
                }
            }
        ) { _ ->
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars))
            ) {
                if (state.planActive) {
                    PlanBanner(
                        planFilePath = state.planFilePath,
                        onReview = if (state.canChangePlanMode) vm::submitPlanForReview else null
                    )
                }
                Conversation(
                    state = state,
                    actions = messageActions,
                    onPickSession = vm::selectSession,
                    onOpenSessions = { vm.openDrawer(AgentUiState.DrawerTab.Sessions) },
                    onFillComposer = vm::setComposerText,
                    modifier = Modifier.weight(1f)
                )
                state.pendingPlanReview?.let { review ->
                    PlanReviewCard(
                        review = review,
                        onApprove = vm::approvePlan,
                        onRequestRevisions = vm::rejectPlan
                    )
                }

                com.webtoapp.ui.agent.components.ChangesReviewCard(
                    changes = state.pendingChanges,
                    expanded = state.changesReviewExpanded,
                    onToggle = vm::toggleChangesReview,
                    onUndoOne = vm::undoChange,
                    onUndoAll = vm::undoAllChanges,
                    onClear = vm::clearChangesReview
                )

                com.webtoapp.ui.agent.components.AppChangesReviewCard(
                    changes = state.pendingAppChanges,
                    expanded = state.appChangesExpanded,
                    onToggle = vm::toggleAppChangesReview,
                    onClear = vm::clearAppChanges,
                    onOpenApp = onOpenApp
                )
                if (state.editingMessageId != null) {
                    EditingHint(onCancel = vm::cancelEditing)
                }
                Composer(
                    state = state,
                    onTextChange = vm::setComposerText,
                    onSend = vm::send,
                    onCancel = vm::cancelTurn,
                    onRunSlashCommand = vm::runSlashCommand,
                    onDismissSlash = vm::dismissSlash,
                    onPickMention = vm::pickFileMention,
                    onDismissMention = vm::dismissMention,
                    onTogglePlanMode = vm::togglePlanMode,

                    onAttachImage = { imagePicker.launch("image/*") },
                    onAttachFile = { filePicker.launch("*/*") },
                    onAttachFolder = { folderPicker.launch(null) },
                    onRemoveAttachment = vm::removePendingAttachment,
                    resolveAttachmentPreview = vm::attachmentPreviewFile,

                    onOpenContextPicker = {
                        focusManager.clearFocus()
                        vm.openContextPicker()
                    },
                    onOpenModelPicker = {
                        focusManager.clearFocus()
                        if (state.modelMissing) onAddModel() else vm.openModelPicker()
                    }
                )
            }
        }
    }

    if (state.contextPickerOpen) {
        AgentContextSheet(
            state = state,
            onDismissError = vm::dismissBanner,
            onToggleApp = vm::toggleContextApp,
            onToggleModule = vm::toggleContextModule,
            onToggleSkill = vm::toggleContextSkill,
            onImportSkill = { skillPicker.launch(arrayOf("*/*")) },
            onRemoveSkill = vm::removeSkill,
            onCompact = vm::compactNow,
            onDismiss = vm::closeContextPicker
        )
    }
    if (toolsOpen) {
        AgentToolsSheet(
            sessionId = state.currentSession?.id,
            imageModelId = state.currentSession?.config?.imageModelId,
            loadTools = vm::toolCatalog,
            onMcpEnabledChange = vm::setLocalMcpEnabled,
            onDismiss = { toolsOpen = false }
        )
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
        com.webtoapp.ui.agent.components.SaveAsAppDialog(
            artifacts = state.detectedArtifacts,
            selectedArtifactId = state.selectedArtifactId,
            inFlight = state.saveAsAppInFlight,
            onSelectArtifact = vm::selectArtifact,
            onConfirm = vm::saveSelectedArtifact,
            onDismiss = vm::closeSaveAsAppDialog
        )
    }
    if (state.modelPickerOpen) {
        com.webtoapp.ui.agent.components.ModelPickerDialog(
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
            message = Strings.agentEditingHeader,
            tone = WtaStatusTone.Info,
            actionLabel = Strings.agentEditingCancel,
            onAction = onCancel
        )
    }
}

@Composable
private fun PlanBanner(planFilePath: String?, onReview: (() -> Unit)?) {
    WtaCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = WtaSpacing.ScreenHorizontal, vertical = WtaSpacing.Small),
        tone = WtaCardTone.Surface,
        contentPadding = PaddingValues(WtaSpacing.Medium)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Checklist,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(WtaSize.Icon)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(Strings.agentPlanModeBadge, style = MaterialTheme.typography.titleSmall)
                planFilePath?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (onReview != null) {
                WtaButton(
                    onClick = onReview,
                    text = Strings.agentPlanReview,
                    variant = WtaButtonVariant.Outlined,
                    size = WtaButtonSize.Small
                )
            }
        }
    }
}

@Composable
private fun PlanReviewCard(
    review: PlanReview,
    onApprove: () -> Unit,
    onRequestRevisions: () -> Unit
) {
    WtaCard(
        tone = WtaCardTone.Surface,
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = WtaSpacing.ScreenHorizontal,
            vertical = WtaSpacing.Tiny
        ),
        contentPadding = PaddingValues(WtaSpacing.Medium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
            Text(
                text = Strings.agentPlanAwaitingReview,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            MarkdownText(
                text = review.content,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .heightIn(max = 176.dp)
                    .verticalScroll(rememberScrollState())
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                WtaButton(
                    onClick = onRequestRevisions,
                    text = Strings.agentPlanRequestRevisions,
                    variant = WtaButtonVariant.Outlined,
                    size = WtaButtonSize.Small,
                    modifier = Modifier.weight(1f)
                )
                WtaButton(
                    onClick = onApprove,
                    text = Strings.agentPlanApprove,
                    variant = WtaButtonVariant.Tonal,
                    size = WtaButtonSize.Small,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Conversation(
    state: AgentUiState,
    actions: MessageActions,
    onPickSession: (String) -> Unit,
    onOpenSessions: () -> Unit,
    onFillComposer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val messages = state.currentSession?.messages.orEmpty().filterNot {
        it.id == state.streamingDraftMessageId
    }

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

    var wasWorking by remember { mutableStateOf(false) }
    LaunchedEffect(state.isWorking) {
        val working = state.isWorking
        if (working && !wasWorking) {
            followBottom = true
            val target = totalContentItems - 1
            if (target >= 0) {
                runCatching { listState.scrollToItem(target) }
            }
        }
        wasWorking = working
    }

    val streamingTextLen = state.streamingText.length
    val streamingThinkingLen = state.streamingThinkingSegments.sumOf { it.content.length }
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
            if (last != null && last.index == info.totalItemsCount - 1) {
                val gap = (last.offset + last.size) - viewportBottom
                if (gap > 0) {
                    listState.scrollBy(gap.toFloat())
                }
            } else if (info.totalItemsCount > 0) {
                listState.scrollToItem(info.totalItemsCount - 1)
            }

            kotlinx.coroutines.yield()
        }
    }

    if (messages.isEmpty() && !state.isWorking && state.todos.isEmpty()) {

        AgentWelcome(
            sessions = state.sessions,
            onPickSession = onPickSession,
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
            contentPadding = PaddingValues(
                horizontal = WtaSpacing.ScreenHorizontal,
                vertical = WtaSpacing.Medium
            ),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Medium)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    actions = actions,
                    modifier = Modifier.animateItem()
                )
            }
            if (state.todos.isNotEmpty()) {
                item(key = "todos") {
                    Box(Modifier.animateItem()) {
                        TodoChecklist(state.todos)
                    }
                }
            }
            if (state.isWorking) {
                item(key = "streaming") {
                    Box(Modifier.animateItem()) {
                        StreamingBubble(
                            text = state.streamingText,
                            thinkingSegments = state.streamingThinkingSegments,
                            pendingTools = state.pendingToolCalls,
                            activity = state.currentActivity
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !atBottom,
            enter = WtaFadeIn + scaleIn(),
            exit = WtaFadeOut + scaleOut(),
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
        tone = WtaCardTone.Elevated,
        shape = RoundedCornerShape(WtaRadius.Pill),
        contentPadding = PaddingValues(
            horizontal = WtaSpacing.Medium + 2.dp,
            vertical = WtaSpacing.Small + 2.dp
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.ArrowDownward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(WtaSize.IconSmall)
            )
            Spacer(Modifier.width(WtaSpacing.Tiny + 2.dp))
            Text(
                text = Strings.agentJumpToLatest,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
