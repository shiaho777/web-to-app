package com.webtoapp.ui.agent

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import com.webtoapp.core.ai.AiConfigManager
import com.webtoapp.core.agent.llm.LlmMessage
import com.webtoapp.core.agent.engine.AgentEvent
import com.webtoapp.core.agent.engine.CompactService
import com.webtoapp.core.agent.files.ProjectFileManager
import com.webtoapp.core.agent.permission.ChoiceResponse
import com.webtoapp.core.agent.permission.PermissionResponse
import com.webtoapp.core.agent.plan.PlanManager
import com.webtoapp.core.agent.runtime.AgentService
import com.webtoapp.core.agent.session.AgentMessage
import com.webtoapp.core.agent.session.AgentSession
import com.webtoapp.core.agent.session.RecordedToolCall
import com.webtoapp.core.agent.session.SessionConfig
import com.webtoapp.core.agent.session.SessionStore
import com.webtoapp.core.agent.session.UserAttachment
import com.webtoapp.core.agent.todo.TodoManager
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolRegistryFactory
import com.webtoapp.core.agent.prompt.SystemPromptBuilder
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.AiFeature
import com.webtoapp.data.model.toManifestJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx: Context = application
    private val files = ProjectFileManager(ctx)
    private val sessionStore = SessionStore(ctx, files)
    private val configManager = AiConfigManager(ctx)
    private val todoManager = TodoManager()

    private val imageRegistry = com.webtoapp.core.agent.imagery.DefaultImageGenerators.create(ctx)

    private var compactService: CompactService? = null

    private val webAppRepository: com.webtoapp.data.repository.WebAppRepository by lazy {
        org.koin.java.KoinJavaComponent.get(
            com.webtoapp.data.repository.WebAppRepository::class.java
        )
    }
    private val saveSessionAsAppUseCase by lazy {
        com.webtoapp.core.agent.export.SaveSessionAsAppUseCase(
            context = ctx,
            files = files,
            repository = webAppRepository
        )
    }

    private val extensionManager: com.webtoapp.core.extension.ExtensionManager by lazy {
        org.koin.java.KoinJavaComponent.get(
            com.webtoapp.core.extension.ExtensionManager::class.java
        )
    }
    private val extensionFiles by lazy {
        com.webtoapp.core.extension.ExtensionFileManager(ctx)
    }
    private val saveSessionAsModuleUseCase by lazy {
        com.webtoapp.core.agent.export.SaveSessionAsModuleUseCase(
            context = ctx,
            files = files,
            extensionManager = extensionManager,
            extensionFiles = extensionFiles
        )
    }

    private val artifactDetector by lazy {
        com.webtoapp.core.agent.export.SessionArtifactDetector(files)
    }

    private val prefs = com.webtoapp.core.agent.AgentPrefs(ctx)

    private val sessionEnsureMutex = Mutex()

    private var planManager: PlanManager? = null
    private var registryFactory: ToolRegistryFactory? = null

    private val _ui = MutableStateFlow(
        AgentUiState(slashCommands = DEFAULT_SLASH_COMMANDS)
    )
    val ui: StateFlow<AgentUiState> = _ui.asStateFlow()

    private var service: AgentService? = null
    private var bound = false
    private var eventCollectionJob: Job? = null
    private var streamingSessionId: String? = null
    private val streamText = StringBuilder()
    private val streamThinking = StringBuilder()
    private val streamTools = LinkedHashMap<String, RecordedToolCall>()
    private val streamToolArgs = HashMap<String, StringBuilder>()
    private val readFilesThisTurn = mutableSetOf<String>()
    private var lastToolUiPushAt = 0L

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as AgentService.LocalBinder).service()
            bound = true
            attachToService()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
        }
    }

    init {
        observeStreams()
        observeTodos()
        bindService()
        observeAutoApprovePref()
        observeCurrentModel()
    }

    private fun observeAutoApprovePref() {
        viewModelScope.launch {
            prefs.autoApproveFlow.collect { enabled ->
                _ui.update { it.copy(autoApprove = enabled) }
                applyAutoApproveToService(enabled)
            }
        }
    }

    private fun applyAutoApproveToService(enabled: Boolean) {
        val checker = service?.permissionChecker ?: return
        checker.setMode(
            if (enabled) com.webtoapp.core.agent.permission.PermissionMode.AutoApprove
            else com.webtoapp.core.agent.permission.PermissionMode.Default
        )
    }

    private fun observeCurrentModel() {
        viewModelScope.launch {
            combine(configManager.savedModelsFlow, configManager.defaultModelIdFlow) { models, defaultId ->
                resolveTextModel(models) to defaultId
            }.collect { (model, _) ->
                val label = model?.alias?.takeIf { it.isNotBlank() }
                    ?: model?.model?.name
                    ?: Strings.agentModelChipLabel
                _ui.update { it.copy(currentModelLabel = label) }
            }
        }
    }

    fun openDrawer(tab: AgentUiState.DrawerTab = _ui.value.drawerTab) {
        _ui.update { it.copy(drawerOpen = true, drawerTab = tab) }
    }

    fun closeDrawer() {
        _ui.update { it.copy(drawerOpen = false) }
    }

    fun setDrawerTab(tab: AgentUiState.DrawerTab) {
        _ui.update { it.copy(drawerTab = tab) }
    }

    fun setDrawerSearch(query: String) {
        _ui.update { it.copy(drawerSearch = query) }
    }

    fun openPreview() {
        _ui.update { it.copy(previewOpen = true) }
    }

    fun closePreview() {
        _ui.update { it.copy(previewOpen = false) }
    }

    fun openSaveAsAppDialog() {
        val sessionId = _ui.value.currentSession?.id ?: return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {

            val artifacts = artifactDetector.detect(sessionId)
            _ui.update {
                if (artifacts.isEmpty()) {
                    it.copy(info = Strings.agentSaveNoArtifacts)
                } else {
                    it.copy(
                        saveAsAppDialogOpen = true,
                        detectedArtifacts = artifacts,
                        selectedArtifactId = artifacts.first().id
                    )
                }
            }
        }
    }

    fun closeSaveAsAppDialog() {
        _ui.update { it.copy(saveAsAppDialogOpen = false) }
    }

    fun selectArtifact(id: String) {
        _ui.update { it.copy(selectedArtifactId = id) }
    }

    fun setAutoApprove(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setAutoApprove(enabled)
            if (enabled) {
                _ui.value.pendingPermission?.toolCallId?.let { id ->
                    answerPermission(
                        id,
                        com.webtoapp.core.agent.permission.PermissionResponse.Allow
                    )
                }
            }
        }
    }

    fun toggleChangesReview() {
        _ui.update { it.copy(changesReviewExpanded = !it.changesReviewExpanded) }
    }

    fun clearChangesReview() {
        val sid = _ui.value.currentSession?.id
        if (sid != null) {
            files.clearSnapshots(sid)
        }
        _ui.update { it.copy(pendingChanges = emptyList(), changesReviewExpanded = false) }
    }

    fun undoAllChanges() {
        val sid = _ui.value.currentSession?.id ?: return
        viewModelScope.launch {
            val touched = _ui.value.pendingChanges
            for (change in touched) {
                kotlin.runCatching { files.undoChange(sid, change.path) }
            }
            _ui.update {
                it.copy(
                    projectFiles = files.listAll(sid),
                    pendingChanges = emptyList(),
                    changesReviewExpanded = false,
                    info = if (touched.isNotEmpty()) {
                        Strings.agentChangesUndoneToast.format(touched.size)
                    } else null
                )
            }
        }
    }

    fun undoChange(path: String) {
        val sid = _ui.value.currentSession?.id ?: return
        viewModelScope.launch {
            val ok = kotlin.runCatching { files.undoChange(sid, path) }.getOrDefault(false)
            _ui.update { state ->
                if (ok) state.copy(
                    projectFiles = files.listAll(sid),
                    pendingChanges = state.pendingChanges.filterNot { it.path == path }
                ) else state
            }
        }
    }

    fun saveSelectedArtifact(name: String, iconUri: android.net.Uri?) {
        val sessionId = _ui.value.currentSession?.id ?: return
        val artifact = currentSelectedArtifact() ?: return
        if (_ui.value.saveAsAppInFlight) return
        _ui.update { it.copy(saveAsAppInFlight = true) }
        viewModelScope.launch {
            when (artifact.kind.target) {
                com.webtoapp.core.agent.export.DetectedArtifact.Kind.Target.App ->
                    runAppSave(sessionId, artifact, name, iconUri)
                com.webtoapp.core.agent.export.DetectedArtifact.Kind.Target.Module ->
                    runModuleSave(sessionId, artifact)
            }
        }
    }

    private suspend fun runAppSave(
        sessionId: String,
        artifact: com.webtoapp.core.agent.export.DetectedArtifact,
        name: String,
        iconUri: android.net.Uri?
    ) {
        val result = saveSessionAsAppUseCase.save(
            sessionId = sessionId,
            artifact = artifact,
            name = name,
            iconUri = iconUri
        )
        _ui.update {
            when (result) {
                is com.webtoapp.core.agent.export.SaveSessionAsAppUseCase.Result.Success ->
                    it.copy(
                        saveAsAppInFlight = false,
                        saveAsAppDialogOpen = false,
                        info = Strings.agentSaveAsAppSuccess.format(result.name)
                    )
                is com.webtoapp.core.agent.export.SaveSessionAsAppUseCase.Result.Failure ->
                    it.copy(
                        saveAsAppInFlight = false,

                        error = Strings.agentSaveAsAppFailure.format(result.message)
                    )
            }
        }
    }

    private suspend fun runModuleSave(
        sessionId: String,
        artifact: com.webtoapp.core.agent.export.DetectedArtifact
    ) {
        val result = saveSessionAsModuleUseCase.save(sessionId = sessionId, artifact = artifact)
        _ui.update {
            when (result) {
                is com.webtoapp.core.agent.export.SaveSessionAsModuleUseCase.Result.Success -> {
                    val first = result.moduleNames.firstOrNull().orEmpty()
                    val msg = if (result.moduleNames.size > 1) {
                        Strings.agentSaveAsModuleSuccessMany.format(
                            first,
                            result.moduleNames.size - 1
                        )
                    } else Strings.agentSaveAsModuleSuccess.format(first)
                    it.copy(
                        saveAsAppInFlight = false,
                        saveAsAppDialogOpen = false,
                        info = msg
                    )
                }
                is com.webtoapp.core.agent.export.SaveSessionAsModuleUseCase.Result.Failure ->
                    it.copy(
                        saveAsAppInFlight = false,
                        error = Strings.agentSaveAsModuleFailure.format(result.message)
                    )
            }
        }
    }

    private fun currentSelectedArtifact():
        com.webtoapp.core.agent.export.DetectedArtifact? {
        val state = _ui.value
        val id = state.selectedArtifactId ?: return null
        return state.detectedArtifacts.firstOrNull { it.id == id }
    }

    fun setComposerText(text: String) {

        val slashOpen = text.startsWith("/") && !text.contains(" ")

        val mention = parseTrailingMention(text)
        val sid = _ui.value.currentSession?.id
        val mentionMatches = if (mention != null && sid != null && !slashOpen) {
            searchProjectFiles(sid, mention)
        } else emptyList()

        _ui.update {
            it.copy(
                composerText = text,
                slashOpen = slashOpen,
                mentionPickerOpen = mention != null && !slashOpen,
                mentionQuery = mention.orEmpty(),
                mentionMatches = mentionMatches
            )
        }
    }

    private fun parseTrailingMention(text: String): String? {
        val atIdx = text.lastIndexOf('@')
        if (atIdx < 0) return null
        val tail = text.substring(atIdx + 1)

        if (tail.any { !(it.isLetterOrDigit() || it == '_' || it == '-' || it == '.' || it == '/') }) {
            return null
        }

        val before = if (atIdx == 0) ' ' else text[atIdx - 1]
        if (!before.isWhitespace()) return null
        return tail
    }

    private fun searchProjectFiles(
        sessionId: String,
        query: String
    ): List<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo> {
        val all = files.listAll(sessionId)
        if (query.isBlank()) return all.take(8)
        val q = query.lowercase()
        return all.asSequence()
            .filter { it.relativePath.lowercase().contains(q) }
            .sortedWith(

                compareByDescending<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo> {
                    it.relativePath.substringAfterLast('/').lowercase().startsWith(q)
                }.thenBy { it.relativePath.length }
            )
            .take(8)
            .toList()
    }

    fun pickFileMention(path: String) {
        val current = _ui.value.composerText
        val atIdx = current.lastIndexOf('@')
        if (atIdx < 0) return

        val replaced = current.substring(0, atIdx) + "@" + path + " "
        _ui.update {
            it.copy(
                composerText = replaced,
                mentionPickerOpen = false,
                mentionQuery = "",
                mentionMatches = emptyList()
            )
        }
    }

    fun dismissMention() {
        _ui.update {
            it.copy(
                mentionPickerOpen = false,
                mentionQuery = "",
                mentionMatches = emptyList()
            )
        }
    }

    fun dismissSlash() {
        _ui.update { it.copy(slashOpen = false) }
    }

    fun runSlashCommand(command: SlashCommand) {
        when (command.id) {
            "clear" -> newSession()
            "compact" -> compactNow()
            "plan" -> enterPlanMode()
            "exit-plan" -> exitPlanMode()
            "model" -> openModelPicker()
            else -> Unit
        }
        _ui.update { it.copy(composerText = "", slashOpen = false) }
    }

    fun openModelPicker() {
        viewModelScope.launch {
            val all = configManager.savedModelsFlow.first()
            val keys = configManager.apiKeysFlow.first()
            val keyById = keys.associateBy { it.id }
            val models = all.filter { it.supportsFeature(AiFeature.AGENT) }
            if (models.isEmpty()) {
                _ui.update { it.copy(error = Strings.agentMissingTextModel) }
                return@launch
            }
            val current = resolveTextModel(all)
            val currentId = current?.id
            val currentKeyId = current?.apiKeyId

            val groups = models
                .groupBy { it.apiKeyId }
                .mapNotNull { (keyId, ms) ->
                    val key = keyById[keyId] ?: return@mapNotNull null
                    val choices = ms.map { m ->
                        ModelChoice(
                            id = m.id,
                            label = m.alias?.takeIf { it.isNotBlank() } ?: m.model.name,
                            subtitle = "${key.displayName} · ${m.model.name}",
                            selected = m.id == currentId
                        )
                    }
                    ProviderGroup(
                        apiKeyId = keyId,
                        displayName = key.displayName,
                        models = choices
                    )
                }
                .sortedWith(
                    compareByDescending<ProviderGroup> { it.apiKeyId == currentKeyId }
                        .thenBy { it.displayName }
                )

            _ui.update {
                it.copy(
                    modelPickerOpen = true,
                    modelProviderGroups = groups,
                    selectedProviderKeyId = currentKeyId ?: groups.firstOrNull()?.apiKeyId,
                    composerText = "",
                    slashOpen = false
                )
            }
        }
    }

    private fun selectModelByName(query: String) {
        viewModelScope.launch {
            val models = configManager.savedModelsFlow.first()
                .filter { it.supportsFeature(AiFeature.AGENT) }
            if (models.isEmpty()) {
                _ui.update { it.copy(error = Strings.agentMissingTextModel) }
                return@launch
            }
            val q = query.lowercase()
            fun names(m: com.webtoapp.data.model.SavedModel) = listOfNotNull(
                m.alias?.takeIf { it.isNotBlank() }, m.model.name
            ).map { it.lowercase() }

            val exact = models.filter { m -> names(m).any { it == q } }
            val prefix = models.filter { m -> names(m).any { it.startsWith(q) } }
            val substr = models.filter { m -> names(m).any { it.contains(q) } }
            val match = when {
                exact.size == 1 -> exact.first()
                exact.isEmpty() && prefix.size == 1 -> prefix.first()
                exact.isEmpty() && prefix.isEmpty() && substr.size == 1 -> substr.first()
                else -> null
            }
            if (match == null) {

                openModelPicker()
                return@launch
            }
            selectModel(match.id)
        }
    }

    fun dismissModelPicker() {
        _ui.update { it.copy(modelPickerOpen = false, modelProviderGroups = emptyList(), selectedProviderKeyId = null) }
    }

    private suspend fun resolveTextModel(models: List<com.webtoapp.data.model.SavedModel>): com.webtoapp.data.model.SavedModel? {
        val codingModels = models.filter { it.supportsFeature(AiFeature.AGENT) }
        if (codingModels.isEmpty()) return null
        val defaultId = configManager.defaultModelIdFlow.first()
        return codingModels.firstOrNull { it.id == defaultId } ?: codingModels.first()
    }

    fun selectModel(modelId: String) {
        viewModelScope.launch {
            val model = configManager.getSavedModelById(modelId)
            configManager.setDefaultModel(modelId)
            val label = model?.alias?.takeIf { it.isNotBlank() }
                ?: model?.model?.name
                ?: modelId
            _ui.update {
                it.copy(
                    modelPickerOpen = false,
                    modelProviderGroups = emptyList(),
                    selectedProviderKeyId = null,
                    info = Strings.agentModelSwitched.format(label)
                )
            }
        }
    }

    fun newSession() {
        viewModelScope.launch {

            val s = sessionStore.create()
            attachSession(s.id)
        }
    }

    fun tryNewSession(): Boolean {
        val cur = _ui.value.currentSession
        if (cur != null && cur.messages.isEmpty()) {
            _ui.update { it.copy(info = Strings.agentNewSessionAlreadyEmpty) }
            return false
        }
        newSession()
        return true
    }

    fun selectSession(id: String) {
        viewModelScope.launch {
            sessionStore.setCurrent(id)
            attachSession(id)
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch { sessionStore.delete(id) }
    }

    fun pinSession(id: String, pinned: Boolean) {
        viewModelScope.launch { sessionStore.pin(id, pinned) }
    }

    fun selectFile(path: String) {
        val sid = _ui.value.currentSession?.id ?: return
        val text = files.readText(sid, path)
        _ui.update {
            it.copy(
                selectedFilePath = path,
                selectedFileContent = text,
                previewFilePath = path
            )
        }
    }

    fun setPreviewFile(path: String?) {
        _ui.update { it.copy(previewFilePath = path) }
    }

    fun saveSelectedFile(content: String) {
        val sid = _ui.value.currentSession?.id ?: return
        val p = _ui.value.selectedFilePath ?: return
        files.writeText(sid, p, content)
        refreshFiles(sid)
        _ui.update { it.copy(selectedFileContent = content, info = Strings.agentFileSaved.replace("%s", p)) }
    }

    private fun refreshFiles(sessionId: String) {
        val list = files.listAll(sessionId)
        _ui.update { it.copy(projectFiles = list) }
    }

    fun cancelTurn() {
        val sid = streamingSessionId
        if (sid != null) {
            viewModelScope.launch { savePartialMessage(sid) }
        }
        service?.cancel()
        _ui.update {
            it.copy(
                phase = AgentUiState.Phase.Idle,
                currentActivity = null,
                streamingText = "",
                streamingThinking = "",
                streamingThinkingStartedAt = null,
                streamingThinkingDurationMs = null,
                pendingToolCalls = emptyList()
            )
        }
        streamingSessionId = null
        streamText.clear(); streamThinking.clear(); streamTools.clear(); streamToolArgs.clear(); readFilesThisTurn.clear()
    }

    private suspend fun savePartialMessage(sid: String) {
        val text = streamText.toString().trim()
        val thinking = streamThinking.toString().takeIf { it.isNotBlank() }
        val tools = streamTools.values.toList()
        if (text.isBlank() && thinking.isNullOrBlank() && tools.isEmpty()) return
        sessionStore.appendMessage(
            sid,
            AgentMessage(
                role = AgentMessage.Role.ASSISTANT,
                content = text.ifBlank { Strings.agentNoOutput } + "\n\n" + Strings.agentAbortedHint,
                isError = true,
                thinking = thinking,
                thinkingDurationMs = _ui.value.streamingThinkingDurationMs
                    ?: _ui.value.streamingThinkingStartedAt?.let { start -> System.currentTimeMillis() - start },
                toolCalls = tools
            )
        )
    }

    fun dismissBanner() {
        _ui.update { it.copy(error = null, info = null) }
    }

    fun answerPermission(toolCallId: String, response: PermissionResponse) {
        service?.permissionPrompter?.respond(toolCallId, response)
        _ui.update { it.copy(pendingPermission = null) }
    }

    fun answerChoice(requestId: String, response: ChoiceResponse) {
        service?.permissionPrompter?.respondChoice(requestId, response)
        _ui.update { it.copy(pendingChoice = null) }
    }

    fun compactNow() {
        val session = _ui.value.currentSession ?: return
        val service = service ?: run {
            _ui.update { it.copy(error = Strings.agentServiceNotConnected) }
            return
        }
        val gateway = service.gateway
        viewModelScope.launch {
            val models = configManager.savedModelsFlow.first()
            val keys = configManager.apiKeysFlow.first()
            val textModel = resolveTextModel(models)
            val textKey = textModel?.let { m -> keys.firstOrNull { it.id == m.apiKeyId } }
            if (textModel == null || textKey == null) {
                _ui.update { it.copy(error = Strings.agentMissingTextModel) }
                return@launch
            }

            _ui.update { it.copy(compacting = true, info = Strings.agentCompacting) }
            val svc = compactService ?: CompactService(gateway).also { compactService = it }
            val result = svc.compact(session.messages, textModel, textKey)
            if (result.didCompact) {
                sessionStore.replaceMessages(session.id, result.messages)
                _ui.update {
                    it.copy(
                        compacting = false,
                        info = Strings.agentCompactedManual.format(
                            session.messages.size,
                            result.messages.size
                        ),
                        estimatedContextTokens = svc.estimateTokens(result.messages),
                        contextCapacity = textModel.effectiveContextLength
                    )
                }
            } else {
                _ui.update { it.copy(compacting = false, info = Strings.agentCompactSkipped.format(result.reason)) }
            }
        }
    }

    fun send() {
        val current = _ui.value
        if (current.phase != AgentUiState.Phase.Idle) return
        val rawMessage = current.composerText.trim()
        if (rawMessage.isEmpty() && current.pendingAttachments.isEmpty()) return

        if (rawMessage == "/model" || rawMessage.startsWith("/model ")) {
            val arg = rawMessage.removePrefix("/model").trim()
            _ui.update { it.copy(composerText = "") }
            if (arg.isEmpty()) openModelPicker() else selectModelByName(arg)
            return
        }

        viewModelScope.launch {
            val session = ensureActiveSession() ?: return@launch
            val pending = current.pendingAttachments
            val mentionedPaths = extractMentionPaths(rawMessage, session.id)

            val editingId = _ui.value.editingMessageId
            val baseSession = if (editingId != null) {
                sessionStore.truncateAt(session.id, editingId, keep = false) ?: session
            } else session

            val userMsg = AgentMessage(
                role = AgentMessage.Role.USER,
                content = rawMessage,
                mentionedFiles = mentionedPaths,
                userAttachments = pending
            )
            val updated = sessionStore.appendMessage(baseSession.id, userMsg) ?: return@launch

            _ui.update {
                it.copy(
                    composerText = "",
                    editingMessageId = null,
                    mentionPickerOpen = false,
                    mentionQuery = "",
                    mentionMatches = emptyList(),
                    pendingAttachments = emptyList()
                )
            }

            dispatchTurn(updated, rawMessage)
        }
    }

    private fun extractMentionPaths(text: String, sessionId: String): List<String> {
        val regex = Regex("(?:^|\\s)@([A-Za-z0-9_\\-./]+)")
        val raw = regex.findAll(text).mapNotNull { it.groupValues[1].takeIf { p -> p.isNotEmpty() } }.toList()
        if (raw.isEmpty()) return emptyList()
        val seen = LinkedHashSet<String>()
        for (p in raw) {

            val cleaned = p.trimEnd('.', ',', ';', ':', '!', '?', ')', ']')
            if (cleaned.isNotEmpty() && files.exists(sessionId, cleaned)) seen.add(cleaned)
        }
        return seen.toList()
    }

    fun attachUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val session = ensureActiveSession() ?: return@launch
            val added = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri -> importUriToUploads(session.id, uri) }
            }
            if (added.isNotEmpty()) {
                _ui.update { it.copy(pendingAttachments = it.pendingAttachments + added) }
                refreshFiles(session.id)
            }
        }
    }

    fun attachFolder(treeUri: Uri) {
        viewModelScope.launch {
            val session = ensureActiveSession() ?: return@launch
            val added = withContext(Dispatchers.IO) { importFolderToUploads(session.id, treeUri) }
            if (added.isNotEmpty()) {
                _ui.update { it.copy(pendingAttachments = it.pendingAttachments + added) }
                refreshFiles(session.id)
            }
        }
    }

    fun removePendingAttachment(path: String) {
        val sid = _ui.value.currentSession?.id
        if (sid != null) runCatching { files.delete(sid, path) }
        _ui.update { it.copy(pendingAttachments = it.pendingAttachments.filter { it.path != path }) }
        if (sid != null) refreshFiles(sid)
    }

    private fun importUriToUploads(sessionId: String, uri: Uri): UserAttachment? {
        val name = queryDisplayName(uri) ?: "upload_${System.currentTimeMillis()}"
        val mime = ctx.contentResolver.getType(uri) ?: mimeFromName(name)
        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        return writeUpload(sessionId, "uploads", name, bytes, mime)
    }

    private fun importFolderToUploads(sessionId: String, treeUri: Uri): List<UserAttachment> {
        val tree = DocumentFile.fromTreeUri(ctx, treeUri) ?: return emptyList()
        val folderName = tree.name ?: "folder"
        val result = mutableListOf<UserAttachment>()
        fun walk(dir: DocumentFile, relBase: String) {
            dir.listFiles().forEach { child ->
                val childName = child.name ?: return@forEach
                if (child.isDirectory) {
                    walk(child, "$relBase$childName/")
                } else {
                    val mime = child.type ?: mimeFromName(childName)
                    val bytes = ctx.contentResolver.openInputStream(child.uri)
                        ?.use { it.readBytes() } ?: return@forEach
                    writeUpload(sessionId, "uploads/$relBase", childName, bytes, mime)?.let {
                        result += it
                    }
                }
            }
        }
        walk(tree, "$folderName/")
        return result
    }

    private fun writeUpload(
        sessionId: String,
        dir: String,
        name: String,
        bytes: ByteArray,
        mime: String
    ): UserAttachment? {
        val safeName = name.replace('/', '_')
        val base = safeName.substringBeforeLast('.', safeName)
        val ext = safeName.substringAfterLast('.', "").let { if (it.isEmpty() || it == safeName) "" else ".$it" }
        var candidate = "$dir/$safeName"
        var i = 1
        while (files.exists(sessionId, candidate)) {
            candidate = "$dir/$base-$i$ext"
            i++
        }
        files.writeBytes(sessionId, candidate, bytes) ?: return null
        return UserAttachment(
            path = candidate,
            displayName = candidate.removePrefix("uploads/"),
            mimeType = mime,
            isImage = mime.startsWith("image/")
        )
    }

    private fun queryDisplayName(uri: Uri): String? {
        var name: String? = null
        runCatching {
            ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && c.moveToFirst()) name = c.getString(idx)
            }
        }
        return name ?: uri.lastPathSegment?.substringAfterLast('/')
    }

    private fun mimeFromName(name: String): String =
        when (name.substringAfterLast('.', "").lowercase()) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "json" -> "application/json"
            "js" -> "text/javascript"
            "css" -> "text/css"
            "html", "htm" -> "text/html"
            "txt", "md", "log" -> "text/plain"
            "xml" -> "text/xml"
            else -> "application/octet-stream"
        }

    private fun isTextUpload(mime: String): Boolean =
        mime.startsWith("text/") || mime == "application/json" || mime == "application/xml"

    fun openContextPicker() {
        viewModelScope.launch {
            val session = ensureActiveSession() ?: return@launch
            val apps = webAppRepository.allWebApps.first()
                .map { ContextAppItem(it.id, it.name, it.appType.name) }
            val mgr = com.webtoapp.core.extension.ExtensionManager.getInstance(ctx)
            mgr.awaitLoaded()
            val modules = mgr.getAllModules()
                .map { ContextModuleItem(it.id, it.name, it.sourceType.name) }
            _ui.update {
                it.copy(
                    contextPickerOpen = true,
                    availableContextApps = apps,
                    availableContextModules = modules,
                    contextAppIds = session.config.contextAppIds,
                    contextModuleIds = session.config.contextModuleIds
                )
            }
        }
    }

    fun closeContextPicker() {
        _ui.update { it.copy(contextPickerOpen = false) }
    }

    fun toggleContextApp(id: Long) {
        val sid = _ui.value.currentSession?.id ?: return
        viewModelScope.launch {
            val newIds = _ui.value.contextAppIds.let { if (id in it) it - id else it + id }
            _ui.update { it.copy(contextAppIds = newIds) }
            val session = sessionStore.get(sid) ?: return@launch
            sessionStore.updateConfig(sid, session.config.copy(contextAppIds = newIds))
        }
    }

    fun toggleContextModule(id: String) {
        val sid = _ui.value.currentSession?.id ?: return
        viewModelScope.launch {
            val newIds = _ui.value.contextModuleIds.let { if (id in it) it - id else it + id }
            _ui.update { it.copy(contextModuleIds = newIds) }
            val session = sessionStore.get(sid) ?: return@launch
            sessionStore.updateConfig(sid, session.config.copy(contextModuleIds = newIds))
        }
    }

    private suspend fun buildSelectedContext(config: SessionConfig): String {
        if (config.contextAppIds.isEmpty() && config.contextModuleIds.isEmpty()) return ""
        val sb = StringBuilder()
        sb.appendLine("# Selected apps & modules (current context)")
        sb.appendLine("The user selected these as the working context. Use GetApp/UpdateApp and GetModule/UpdateModule with these ids to inspect or edit them.")
        if (config.contextAppIds.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("## Apps")
            config.contextAppIds.forEach { id ->
                val app = webAppRepository.getWebApp(id) ?: return@forEach
                sb.appendLine()
                sb.appendLine("### App id=${app.id} name=\"${app.name}\" type=${app.appType}")
                sb.appendLine(app.toManifestJson())
            }
        }
        if (config.contextModuleIds.isNotEmpty()) {
            val mgr = com.webtoapp.core.extension.ExtensionManager.getInstance(ctx)
            mgr.awaitLoaded()
            sb.appendLine()
            sb.appendLine("## Modules")
            config.contextModuleIds.forEach { id ->
                val module = mgr.getModuleById(id)?.let { mgr.ensureCodeLoaded(it) } ?: return@forEach
                sb.appendLine()
                sb.appendLine("### Module id=${module.id} name=\"${module.name}\" type=${module.sourceType}")
                sb.appendLine(com.webtoapp.util.GsonProvider.gson.toJson(module))
            }
        }
        return sb.toString().trimEnd()
    }

    fun deleteFromMessage(messageId: String) {
        val sid = _ui.value.currentSession?.id ?: return
        viewModelScope.launch {
            sessionStore.truncateAt(sid, messageId, keep = false)
        }
    }

    fun startEditing(message: AgentMessage) {
        if (message.role != AgentMessage.Role.USER) return
        _ui.update {
            it.copy(
                editingMessageId = message.id,
                composerText = message.content
            )
        }
    }

    fun cancelEditing() {
        _ui.update { it.copy(editingMessageId = null, composerText = "") }
    }

    fun regenerate(messageId: String) {
        val sid = _ui.value.currentSession?.id ?: return
        if (!_ui.value.canSend) return
        viewModelScope.launch {

            val s0 = sessionStore.get(sid) ?: return@launch
            val idx = s0.messages.indexOfFirst { it.id == messageId }
            if (idx < 0) return@launch
            val precedingUser = s0.messages.subList(0, idx)
                .lastOrNull { it.role == AgentMessage.Role.USER } ?: return@launch

            val truncated = sessionStore.truncateAt(sid, messageId, keep = false) ?: return@launch
            dispatchTurn(truncated, precedingUser.content)
        }
    }

    private suspend fun dispatchTurn(
        session: AgentSession,
        prompt: String
    ) {
        val models = configManager.savedModelsFlow.first()
        val keys = configManager.apiKeysFlow.first()

        val textModel = resolveTextModel(models)
        if (textModel == null) {
            _ui.update { it.copy(error = Strings.agentMissingTextModel) }
            return
        }
        val textKey = keys.firstOrNull { it.id == textModel.apiKeyId }
        if (textKey == null) {
            _ui.update { it.copy(error = Strings.agentMissingApiKey) }
            return
        }
        val imageModel = session.config.imageModelId?.let { id -> models.firstOrNull { it.id == id } }
        val imageKey = imageModel?.let { m -> keys.firstOrNull { it.id == m.apiKeyId } }

        val compactSvc = compactService ?: CompactService(service!!.gateway).also { compactService = it }
        val contextLength = textModel.effectiveContextLength
        val workingSession = if (compactSvc.shouldCompact(session.messages, contextLength)) {
            val result = compactSvc.compact(session.messages, textModel, textKey)
            if (result.didCompact) {
                sessionStore.replaceMessages(session.id, result.messages)
                _ui.update { it.copy(info = Strings.agentCompactedAuto) }
                sessionStore.get(session.id) ?: session
            } else session
        } else session

        streamingSessionId = workingSession.id
        streamText.clear(); streamThinking.clear(); streamTools.clear(); streamToolArgs.clear(); readFilesThisTurn.clear()
        val compactSvc2 = compactService ?: CompactService(service!!.gateway).also { compactService = it }
        val estTokens = compactSvc2.estimateTokens(workingSession.messages)
        _ui.update {
            it.copy(
                phase = AgentUiState.Phase.Connecting,
                streamingText = "",
                streamingThinking = "",
                streamingThinkingStartedAt = null,
                streamingThinkingDurationMs = null,
                pendingToolCalls = emptyList(),
                currentActivity = null,
                error = null,
                info = null,
                estimatedContextTokens = estTokens,
                contextCapacity = textModel.effectiveContextLength
            )
        }

        val plan = planManager ?: PlanManager(workingSession.id, files, service!!.permissionChecker).also {
            planManager = it
        }
        val factory = registryFactory ?: ToolRegistryFactory(plan, imageRegistry).also {
            registryFactory = it
        }
        val registry = factory.build(hasImageModel = imageModel != null)

        val projectSummary = files.listAll(workingSession.id).take(40).map { f ->
            val txt = if (f.isText) files.readText(workingSession.id, f.relativePath).orEmpty() else ""
            com.webtoapp.core.agent.prompt.sections.ProjectFilesSection.FileSummary(
                f.relativePath,
                if (txt.isEmpty()) 0 else txt.lines().size,
                f.sizeBytes
            )
        }

        val planState = plan.state.value
        val planMode = if (planState.active && planState.activePlanPath != null) {
            SystemPromptBuilder.PlanMode(
                planFilePath = planState.activePlanPath,
                planExists = plan.planExists()
            )
        } else null

        val sessionRoot = files.getSessionRoot(workingSession.id).absolutePath
        val selectedContext = buildSelectedContext(workingSession.config)
        val systemPrompt = SystemPromptBuilder.build(
            SystemPromptBuilder.Input(
                language = Strings.currentLanguage.value,
                modelName = textModel.alias?.takeIf { it.isNotBlank() } ?: textModel.model.name,
                sessionDir = sessionRoot,
                tools = registry.all,
                projectFiles = projectSummary,

                planMode = planMode,
                selectedContext = selectedContext
            )
        )

        val tailMessage = workingSession.messages.lastOrNull()
        val tailHasMentions =
            tailMessage?.role == AgentMessage.Role.USER && tailMessage.mentionedFiles.isNotEmpty()

        val messagesForHistory = if (tailHasMentions) {
            workingSession.messages
        } else {

            workingSession.messages.dropLast(1)
        }

        if (tailHasMentions) {
            readFilesThisTurn += tailMessage.mentionedFiles
        }

        val history = messagesForHistory.flatMap { m ->
            when (m.role) {
                AgentMessage.Role.USER -> {
                    val out = mutableListOf<LlmMessage>()
                    val images = m.userAttachments.filter { it.isImage }.mapNotNull { att ->
                        val bytes = files.readBytes(workingSession.id, att.path) ?: return@mapNotNull null
                        com.webtoapp.core.agent.tool.ImageAttachment(bytes, att.mimeType, att.path)
                    }
                    out += LlmMessage(LlmMessage.Role.USER, m.content, images = images)
                    out += synthesiseReadCallsFor(m, workingSession.id)
                    out
                }
                AgentMessage.Role.ASSISTANT -> {
                    val out = mutableListOf<LlmMessage>()
                    out += LlmMessage(
                        role = LlmMessage.Role.ASSISTANT,

                        content = stripToolMarkers(m.content),
                        toolCalls = m.toolCalls.map {
                            com.webtoapp.core.agent.llm.LlmToolCall(
                                it.toolCallId, it.name, it.argumentsJson
                            )
                        },

                        reasoningContent = m.thinking?.takeIf { it.isNotEmpty() }
                    )

                    m.toolCalls.forEach { tc ->
                        val raw = tc.resultPreview.takeIf {
                            it.isNotEmpty() && it != RecordedToolCall.RUNNING_SENTINEL
                        } ?: if (tc.ok) "(no output)" else "(tool failed)"
                        val capped = if (raw.length <= TOOL_RESULT_REPLAY_CHARS) raw
                                     else raw.takeLast(TOOL_RESULT_REPLAY_CHARS) +
                                          "\n… (truncated for replay)"
                        out += LlmMessage(
                            role = LlmMessage.Role.TOOL,
                            content = capped,
                            toolCallId = tc.toolCallId,
                            name = tc.name
                        )
                    }
                    out
                }
                AgentMessage.Role.SYSTEM -> emptyList()
            }
        }

        val toolCtx = ToolContext(
            androidContext = ctx,
            sessionId = workingSession.id,
            fileManager = files,
            textModel = textModel,
            textApiKey = textKey,
            imageModel = imageModel,
            imageApiKey = imageKey,
            prompter = service!!.permissionPrompter,
            todos = todoManager,
            appRepository = webAppRepository,
            readFiles = readFilesThisTurn,
            activePlanFile = planState.activePlanPath
        )

        service?.start(
            AgentService.AgentRequest(
                sessionId = workingSession.id,
                systemPrompt = systemPrompt,
                history = history,

                userMessage = if (tailHasMentions) "" else prompt,
                toolContext = toolCtx,
                registry = registry,
                temperature = session.config.temperature,
                maxTurns = session.config.maxTurns
            )
        )
    }

    private fun enterPlanMode() {
        val session = _ui.value.currentSession ?: return
        val plan = planManager ?: PlanManager(session.id, files, service!!.permissionChecker).also {
            planManager = it
        }
        when (val r = plan.enter()) {
            is PlanManager.EnterResult.Entered -> {
                _ui.update { it.copy(planActive = true, planFilePath = r.planPath, info = Strings.agentPlanEntered) }
            }
            is PlanManager.EnterResult.AlreadyActive -> {
                _ui.update { it.copy(planActive = true, planFilePath = r.planPath, info = Strings.agentPlanAlreadyActive) }
            }
        }
    }

    private fun exitPlanMode() {
        val plan = planManager ?: return
        when (val r = plan.exit()) {
            is PlanManager.ExitResult.Submitted -> {
                _ui.update { it.copy(pendingPlanReview = PlanReview(planPath = r.planPath, content = r.content)) }
            }
            is PlanManager.ExitResult.NoPlanWritten -> {
                _ui.update { it.copy(info = Strings.agentPlanEmpty) }
            }
            is PlanManager.ExitResult.NotActive -> {
                _ui.update { it.copy(planActive = false, planFilePath = null) }
            }
        }
    }

    fun approvePlan() {
        val plan = planManager ?: return
        _ui.value.currentSession ?: return
        plan.approve()
        _ui.update {
            it.copy(
                planActive = false,
                planFilePath = null,
                pendingPlanReview = null,
                composerText = "Plan approved. Proceed with implementation."
            )
        }
        send()
    }

    fun rejectPlan() {
        val plan = planManager ?: return
        plan.revise()
        _ui.update { it.copy(pendingPlanReview = null, composerText = "") }
    }

    private fun maybePushToolUi(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastToolUiPushAt < 48L) return
        lastToolUiPushAt = now
        _ui.update {
            it.copy(pendingToolCalls = streamTools.values.toList())
        }
    }

    private fun observeStreams() {
        viewModelScope.launch {
            combine(sessionStore.sessionsFlow, sessionStore.currentSessionIdFlow) { all, currentId ->
                all to currentId
            }.collect { (all, currentId) ->
                val currentMissing = currentId == null || all.none { it.id == currentId }
                if (all.isEmpty() || currentMissing) {
                    ensureActiveSession()
                    return@collect
                }
                val current = all.firstOrNull { it.id == currentId }
                _ui.update {
                    it.copy(
                        sessions = all,
                        currentSession = current,
                        projectFiles = current?.let { c -> files.listAll(c.id) }.orEmpty()
                    )
                }
            }
        }
    }

    private suspend fun ensureActiveSession(): AgentSession? = sessionEnsureMutex.withLock {
        val all = sessionStore.sessionsFlow.first()
        val currentId = sessionStore.currentSessionIdFlow.first()
        val existing = all.firstOrNull { it.id == currentId }
            ?: all.maxByOrNull { it.updatedAt }
            ?: all.firstOrNull()
        if (existing != null) {
            if (currentId != existing.id) {
                sessionStore.setCurrent(existing.id)
            }
            if (_ui.value.currentSession?.id != existing.id) {
                attachSession(existing.id)
            } else {
                _ui.update {
                    it.copy(
                        sessions = all,
                        currentSession = existing,
                        projectFiles = files.listAll(existing.id)
                    )
                }
            }
            return@withLock sessionStore.get(existing.id) ?: existing
        }
        val created = sessionStore.create()
        attachSession(created.id)
        sessionStore.get(created.id) ?: created
    }

    private fun observeTodos() {
        viewModelScope.launch {
            todoManager.items.collect { items ->
                _ui.update { it.copy(todos = items) }
            }
        }
    }

    private fun bindService() {
        val intent = Intent(ctx, AgentService::class.java)
        ContextCompat.startForegroundService(ctx, intent)
        ctx.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun attachToService() {
        eventCollectionJob?.cancel()
        eventCollectionJob = viewModelScope.launch {
            service?.events?.collect { handleAgentEvent(it) }
        }
        viewModelScope.launch {
            service?.permissionPrompter?.requests?.collect { req ->
                _ui.update { it.copy(pendingPermission = req) }
            }
        }
        viewModelScope.launch {
            service?.permissionPrompter?.choices?.collect { req ->
                _ui.update { it.copy(pendingChoice = req) }
            }
        }

        applyAutoApproveToService(_ui.value.autoApprove)
    }

    private suspend fun handleAgentEvent(ev: AgentEvent) {
        val sid = streamingSessionId ?: return
        when (ev) {
            AgentEvent.Started -> _ui.update { it.copy(phase = AgentUiState.Phase.Streaming) }
            is AgentEvent.TextDelta -> {
                streamText.setLength(0); streamText.append(ev.accumulated)
                _ui.update {

                    val frozen = it.streamingThinkingDurationMs ?: it.streamingThinkingStartedAt
                        ?.let { start -> System.currentTimeMillis() - start }
                    it.copy(
                        streamingText = ev.accumulated,
                        streamingThinkingDurationMs = frozen
                    )
                }
            }
            is AgentEvent.ThinkingDelta -> {
                streamThinking.append(ev.delta)
                _ui.update {
                    val started = it.streamingThinkingStartedAt ?: System.currentTimeMillis()
                    it.copy(
                        streamingThinking = streamThinking.toString(),
                        streamingThinkingStartedAt = started
                    )
                }
            }
            is AgentEvent.ToolCallStarted -> {
                streamTools[ev.toolCallId] = RecordedToolCall(
                    toolCallId = ev.toolCallId,
                    name = ev.name,
                    argumentsJson = "",
                    resultPreview = RecordedToolCall.RUNNING_SENTINEL,
                    ok = true
                )
                streamToolArgs.remove(ev.toolCallId)
                _ui.update {
                    it.copy(
                        phase = AgentUiState.Phase.AwaitingTool,
                        pendingToolCalls = streamTools.values.toList(),
                        currentActivity = ev.name
                    )
                }
            }
            is AgentEvent.ToolExecuting -> {

                val prev = streamTools[ev.toolCallId]
                if (prev != null) {
                    streamTools[ev.toolCallId] = prev.copy(activity = ev.activity ?: ev.name)
                }
                _ui.update {
                    it.copy(
                        currentActivity = ev.activity ?: ev.name,
                        pendingToolCalls = streamTools.values.toList()
                    )
                }
            }
            is AgentEvent.ToolCallArgsDelta -> {
                val buf = streamToolArgs.getOrPut(ev.toolCallId) { StringBuilder() }
                buf.append(ev.delta)
                val prev = streamTools[ev.toolCallId]
                if (prev != null) {
                    streamTools[ev.toolCallId] = prev.copy(argumentsJson = buf.toString())
                    maybePushToolUi()
                }
            }
            is AgentEvent.ToolProgress -> {
                val prev = streamTools[ev.toolCallId]
                if (prev != null) {
                    val capped = if (ev.accumulated.length <= STREAM_PREVIEW_CHARS) ev.accumulated
                                 else ev.accumulated.takeLast(STREAM_PREVIEW_CHARS)
                    streamTools[ev.toolCallId] = prev.copy(resultPreview = capped)
                    maybePushToolUi()
                }
            }
            is AgentEvent.ToolFinished -> {
                val prev = streamTools[ev.toolCallId]
                streamTools[ev.toolCallId] = RecordedToolCall(
                    toolCallId = ev.toolCallId,
                    name = ev.name,
                    argumentsJson = ev.argumentsJson,
                    resultPreview = ev.result.text.take(2000),
                    ok = !ev.result.isError,
                    activity = prev?.activity
                )
                streamToolArgs.remove(ev.toolCallId)
                lastToolUiPushAt = 0L
                _ui.update {
                    it.copy(
                        phase = AgentUiState.Phase.Streaming,
                        pendingToolCalls = streamTools.values.toList(),
                        currentActivity = null
                    )
                }
            }
            is AgentEvent.FileChanged -> {
                _ui.update { state ->
                    val existing = state.pendingChanges

                    val rest = existing.filterNot { it.path == ev.change.path }
                    val mappedKind = when (ev.change.kind) {
                        com.webtoapp.core.agent.tool.FileChange.Kind.WRITE -> PendingChange.Kind.Write
                        com.webtoapp.core.agent.tool.FileChange.Kind.EDIT -> PendingChange.Kind.Edit
                        com.webtoapp.core.agent.tool.FileChange.Kind.DELETE -> PendingChange.Kind.Delete
                    }
                    state.copy(
                        projectFiles = files.listAll(sid),
                        pendingChanges = listOf(
                            PendingChange(
                                path = ev.change.path,
                                kind = mappedKind,
                                touchedAt = System.currentTimeMillis()
                            )
                        ) + rest
                    )
                }
            }
            is AgentEvent.PermissionDenied -> {
                _ui.update { it.copy(info = Strings.agentToolDenied.format(ev.name)) }
            }
            is AgentEvent.Usage -> {
                _ui.update {
                    it.copy(
                        inputTokens = it.inputTokens + ev.inputTokens,
                        outputTokens = it.outputTokens + ev.outputTokens
                    )
                }
            }
            is AgentEvent.Notice -> {
                _ui.update { it.copy(info = ev.message) }
            }
            is AgentEvent.Completed -> {
                val rawText = streamText.toString()
                val text = stripToolMarkers(rawText).trim().ifEmpty {
                    stripToolMarkers(ev.summary).trim()
                }
                val contentForTimeline = rawText.ifBlank { text }
                val producedFiles = streamTools.values
                    .filter { it.ok && it.name in setOf("Write", "Edit", "Delete", "GenerateImage") }
                    .mapNotNull { tc ->
                        runCatching {
                            JsonParser.parseString(tc.argumentsJson).asJsonObject.get("path")?.asString
                        }.getOrNull()
                    }
                    .distinct()
                val attachments = streamTools.values
                    .filter { it.ok && it.name == "GenerateImage" }
                    .mapNotNull { tc ->
                        runCatching {
                            JsonParser.parseString(tc.argumentsJson).asJsonObject.get("path")?.asString
                        }.getOrNull()
                    }

                val isDegenerate = text.isBlank() &&
                    streamThinking.toString().isBlank() &&
                    streamTools.isEmpty()
                val finalContent = if (isDegenerate) {
                    Strings.agentEmptyResponseHint
                } else {
                    text
                }
                val saved = sessionStore.appendMessage(
                    sid,
                    AgentMessage(
                        role = AgentMessage.Role.ASSISTANT,
                        content = if (isDegenerate) finalContent else contentForTimeline.ifBlank { finalContent },
                        thinking = streamThinking.toString().takeIf { it.isNotBlank() },
                        thinkingDurationMs = _ui.value.streamingThinkingDurationMs
                            ?: _ui.value.streamingThinkingStartedAt?.let { start -> System.currentTimeMillis() - start },
                        toolCalls = streamTools.values.toList(),
                        producedFiles = producedFiles,
                        attachments = attachments,
                        isError = isDegenerate
                    )
                )
                streamingSessionId = null
                streamText.clear(); streamThinking.clear(); streamTools.clear(); streamToolArgs.clear(); readFilesThisTurn.clear()
                lastToolUiPushAt = 0L
                _ui.update {
                    it.copy(
                        phase = AgentUiState.Phase.Idle,
                        streamingText = "",
                        streamingThinking = "",
                        streamingThinkingStartedAt = null,
                        streamingThinkingDurationMs = null,
                        pendingToolCalls = emptyList(),
                        currentActivity = null,
                        info = if (ev.toolCallCount > 0)
                            Strings.agentTurnDoneToolCount.format(ev.toolCallCount)
                        else null
                    )
                }

                saved?.let { maybeAutoTitle(it) }
            }
            is AgentEvent.PlanReviewRequired -> {
                val planContent = files.readText(sid, ev.planPath) ?: ""
                val text = streamText.toString().trim().ifEmpty { Strings.agentPlanAwaitingReview }
                sessionStore.appendMessage(
                    sid,
                    AgentMessage(
                        role = AgentMessage.Role.ASSISTANT,
                        content = text,
                        thinking = streamThinking.toString().takeIf { it.isNotBlank() },
                        thinkingDurationMs = _ui.value.streamingThinkingDurationMs
                            ?: _ui.value.streamingThinkingStartedAt?.let { start -> System.currentTimeMillis() - start },
                        toolCalls = streamTools.values.toList()
                    )
                )
                streamingSessionId = null
                _ui.update {
                    it.copy(
                        phase = AgentUiState.Phase.Idle,
                        streamingText = "",
                        streamingThinking = "",
                        streamingThinkingStartedAt = null,
                        streamingThinkingDurationMs = null,
                        pendingToolCalls = emptyList(),
                        currentActivity = null,
                        pendingPlanReview = PlanReview(planPath = ev.planPath, content = planContent)
                    )
                }
            }
            AgentEvent.Aborted -> {
                savePartialMessage(sid)
                streamingSessionId = null
                _ui.update {
                    it.copy(
                        phase = AgentUiState.Phase.Idle,
                        streamingText = "",
                        streamingThinking = "",
                        streamingThinkingStartedAt = null,
                        streamingThinkingDurationMs = null,
                        pendingToolCalls = emptyList(),
                        currentActivity = null
                    )
                }
            }
            is AgentEvent.Failed -> {
                sessionStore.appendMessage(
                    sid,
                    AgentMessage(
                        role = AgentMessage.Role.ASSISTANT,
                        content = streamText.toString().ifBlank { Strings.agentNoOutput } + "\n\n" + Strings.agentErrorPrefix.format(ev.message),
                        isError = true,
                        thinking = streamThinking.toString().takeIf { it.isNotBlank() },
                        thinkingDurationMs = _ui.value.streamingThinkingDurationMs
                            ?: _ui.value.streamingThinkingStartedAt?.let { start -> System.currentTimeMillis() - start },
                        toolCalls = streamTools.values.toList()
                    )
                )
                streamingSessionId = null
                _ui.update {
                    it.copy(
                        phase = AgentUiState.Phase.Idle,
                        streamingText = "",
                        streamingThinking = "",
                        streamingThinkingStartedAt = null,
                        streamingThinkingDurationMs = null,
                        pendingToolCalls = emptyList(),
                        currentActivity = null
                    )
                }
            }
        }
    }

    private fun maybeAutoTitle(session: AgentSession) {
        if (!session.titleAutoGenerated) return

        val userMsgs = session.messages.count { it.role == AgentMessage.Role.USER }
        val assistantMsgs = session.messages.count { it.role == AgentMessage.Role.ASSISTANT }
        if (userMsgs != 1 || assistantMsgs != 1) return

        val gateway = service?.gateway ?: return
        viewModelScope.launch {
            val models = configManager.savedModelsFlow.first()
            val keys = configManager.apiKeysFlow.first()
            val textModel = resolveTextModel(models) ?: return@launch
            val textKey = keys.firstOrNull { it.id == textModel.apiKeyId } ?: return@launch

            val firstUser = session.messages.first { it.role == AgentMessage.Role.USER }.content
            val firstAssistant = session.messages.first { it.role == AgentMessage.Role.ASSISTANT }.content

            val sysPrompt = """
                Summarise the following conversation as a short title.
                Constraints:
                - 4 to 7 words; no leading verbs like "How to" or "Helping with".
                - Match the user's language (Chinese stays Chinese, English stays English, etc.).
                - Plain text only — no quotes, no trailing punctuation, no markdown.
                - Respond with the title only. Do not preface, do not explain.
            """.trimIndent()
            val convo = buildString {
                append("User: ")
                append(firstUser.take(800))
                append("\n\nAssistant: ")
                append(firstAssistant.take(400))
            }

            val req = com.webtoapp.core.agent.llm.ChatRequest(
                apiKey = textKey,
                model = textModel.model,
                messages = listOf(
                    LlmMessage(LlmMessage.Role.SYSTEM, sysPrompt),
                    LlmMessage(LlmMessage.Role.USER, convo)
                ),
                tools = emptyList(),
                temperature = 0.3f,
                useTools = false
            )

            val sb = StringBuilder()
            try {
                gateway.chatStream(req).collect { ev ->
                    when (ev) {
                        is com.webtoapp.core.agent.llm.LlmEvent.TextDelta -> sb.append(ev.delta)
                        is com.webtoapp.core.agent.llm.LlmEvent.Error ->
                            if (!ev.recoverable) return@collect
                        else -> Unit
                    }
                }
            } catch (_: Throwable) {
                return@launch
            }

            val title = sb.toString()
                .lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() }
                ?.trim('"', '\'', '“', '”', '「', '」', '《', '》')
                ?.trimEnd('.', '。', '!', '！', '?', '？', ':', '：', ';', '；', ',', '，')
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.take(40)
                ?: return@launch

            if (title == session.title.trim()) return@launch
            sessionStore.setAutoTitle(session.id, title)
        }
    }

    private fun synthesiseReadCallsFor(m: AgentMessage, sessionId: String): List<LlmMessage> {
        val textUploads = m.userAttachments
            .filter { !it.isImage && isTextUpload(it.mimeType) }
            .map { it.path }
        val pathsToRead = (m.mentionedFiles + textUploads).distinct()
        if (pathsToRead.isEmpty()) return emptyList()

        val out = mutableListOf<LlmMessage>()
        val toolCalls = mutableListOf<com.webtoapp.core.agent.llm.LlmToolCall>()
        val toolResults = mutableListOf<LlmMessage>()
        var budget = MENTION_TOTAL_CHAR_BUDGET

        pathsToRead.forEachIndexed { idx, path ->
            val callId = "mention-${m.id}-$idx"
            val argsJson = """{"path":"${path.replace("\"", "\\\"")}"}"""
            toolCalls += com.webtoapp.core.agent.llm.LlmToolCall(callId, "Read", argsJson)

            val text = files.readText(sessionId, path)
            val resultText = if (text == null) {
                "(Read: $path no longer exists in the session)"
            } else {
                val lines = text.lines()
                val window = lines.take(MENTION_LINE_LIMIT)
                val numbered = window.mapIndexed { i, line -> "${i + 1}\t$line" }.joinToString("\n")
                val tail = if (lines.size > MENTION_LINE_LIMIT)
                    "\n… (${lines.size - MENTION_LINE_LIMIT} more lines, ask Read with offset to see them)"
                else ""
                val combined = numbered + tail
                if (combined.length > budget) {
                    val truncated = combined.take(budget.coerceAtLeast(0))
                    "$truncated\n… (truncated to fit mention budget)"
                } else combined
            }
            budget -= resultText.length

            toolResults += LlmMessage(
                role = LlmMessage.Role.TOOL,
                content = resultText,
                toolCallId = callId,
                name = "Read"
            )
        }

        out += LlmMessage(
            role = LlmMessage.Role.ASSISTANT,
            content = "",
            toolCalls = toolCalls
        )
        out += toolResults
        return out
    }

    private suspend fun attachSession(id: String) {
        if (streamingSessionId != null && streamingSessionId != id) {
            cancelTurn()
        }
        val session = sessionStore.get(id) ?: return
        val checker = service?.permissionChecker
        if (checker != null) {
            planManager = PlanManager(session.id, files, checker)
            registryFactory = ToolRegistryFactory(planManager!!, imageRegistry)
        } else {
            planManager = null
            registryFactory = null
        }
        val sessionChanged = _ui.value.currentSession?.id != session.id
        if (sessionChanged) {
            todoManager.clear()
        }
        _ui.update {
            it.copy(
                currentSession = session,
                planActive = if (sessionChanged) false else it.planActive,
                planFilePath = if (sessionChanged) null else it.planFilePath,
                projectFiles = files.listAll(session.id),

                pendingChanges = if (sessionChanged) emptyList() else it.pendingChanges,
                changesReviewExpanded = if (sessionChanged) false else it.changesReviewExpanded,

                previewFilePath = if (sessionChanged) null else it.previewFilePath,
                drawerOpen = if (sessionChanged) false else it.drawerOpen
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        eventCollectionJob?.cancel()
        if (bound) runCatching { ctx.unbindService(connection) }
    }

    companion object {

        private val TOOL_MARKER_REGEX =
            Regex("\u2063TC:([^\u2063]+)\u2063")

        fun stripToolMarkers(text: String): String =
            if (text.isEmpty() || '\u2063' !in text) text
            else TOOL_MARKER_REGEX.replace(text, "")

        private const val MENTION_LINE_LIMIT = 1500

        private const val MENTION_TOTAL_CHAR_BUDGET = 60_000

        private const val STREAM_PREVIEW_CHARS = 4_000

        private const val TOOL_RESULT_REPLAY_CHARS = 8_000

        fun factory(app: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AgentViewModel(app) as T
        }
    }
}
