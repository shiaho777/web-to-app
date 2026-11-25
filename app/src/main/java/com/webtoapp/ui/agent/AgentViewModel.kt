package com.webtoapp.ui.agent

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.documentfile.provider.DocumentFile
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.webtoapp.core.ai.AiConfigManager
import com.webtoapp.core.ai.ApiKeyRotation
import com.webtoapp.data.model.ApiKeyConfig
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
import com.webtoapp.core.agent.session.PersistedApk
import com.webtoapp.core.agent.todo.TodoManager
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.BuiltApkInfo
import com.webtoapp.core.agent.tool.ToolRegistryFactory
import com.webtoapp.core.agent.prompt.SystemPromptBuilder
import com.webtoapp.core.agent.prompt.SelectedContextSection
import com.webtoapp.core.agent.prompt.toPromptLang
import com.webtoapp.core.agent.skill.AgentSkillLibrary
import com.webtoapp.core.agent.mcp.HostMcpController
import com.webtoapp.core.host.HostRuntimePrefs
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.data.model.AiFeature
import kotlinx.coroutines.CancellationException
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
    private val skillLibrary = AgentSkillLibrary(ctx.filesDir.resolve("agent_skills"))
    private val configManager = AiConfigManager(ctx)
    private val todoManager = TodoManager()

    private val imageRegistry = com.webtoapp.core.agent.imagery.DefaultImageGenerators.create(ctx)

    private var compactService: CompactService? = null

    private val webAppRepository: com.webtoapp.data.repository.WebAppRepository by lazy {
        org.koin.java.KoinJavaComponent.get(
            com.webtoapp.data.repository.WebAppRepository::class.java
        )
    }
    private val categoryRepository: com.webtoapp.data.repository.AppCategoryRepository by lazy {
        org.koin.java.KoinJavaComponent.get(
            com.webtoapp.data.repository.AppCategoryRepository::class.java
        )
    }
    private val saveSessionAsAppUseCase by lazy {
        com.webtoapp.core.agent.export.SaveSessionAsAppUseCase(
            context = ctx,
            files = files,
            repository = webAppRepository
        )
    }

    private val extensionFiles by lazy {
        com.webtoapp.core.extension.ExtensionFileManager(ctx)
    }
    private val saveSessionAsModuleUseCase by lazy {
        com.webtoapp.core.agent.export.SaveSessionAsModuleUseCase(
            context = ctx,
            files = files,
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
        AgentUiState()
    )
    val ui: StateFlow<AgentUiState> = _ui.asStateFlow()

    private var service: AgentService? = null
    private var bound = false
    private var eventCollectionJob: Job? = null
    private var permissionCollectionJob: Job? = null
    private var choiceCollectionJob: Job? = null
    private var activeTurnJob: Job? = null
    private var submissionJob: Job? = null
    private val drafts = mutableMapOf<String, ComposerDraft>()
    private var streamingSessionId: String? = null
    private val streamText = StringBuilder()
    private val streamThinkingSegments = mutableListOf<ThinkingSegment>()
    private val streamTools = LinkedHashMap<String, RecordedToolCall>()
    private val streamToolArgs = HashMap<String, StringBuilder>()
    private val readFilesThisTurn = mutableSetOf<String>()
    private var lastToolUiPushAt = 0L

    private val overflowAutoRetry = java.util.concurrent.atomic.AtomicBoolean(false)

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
        viewModelScope.launch { refreshSkills() }
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
        checker.syncAutoApprove(enabled)
    }

    private fun observeCurrentModel() {
        viewModelScope.launch {
            combine(configManager.savedModelsFlow, configManager.defaultModelIdFlow) { models, defaultId ->
                resolveTextModel(models) to defaultId
            }.collect { (model, _) ->
                val label = model?.alias?.takeIf { it.isNotBlank() }
                    ?: model?.model?.name
                    ?: Strings.agentModelChipLabel
                _ui.update {
                    it.copy(
                        currentModelLabel = label,
                        modelMissing = model == null,
                        contextCapacity = model?.effectiveContextLength ?: 0
                    )
                }
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

    fun toggleAppChangesReview() {
        _ui.update { it.copy(appChangesExpanded = !it.appChangesExpanded) }
    }

    fun clearAppChanges() {
        _ui.update { it.copy(pendingAppChanges = emptyList(), appChangesExpanded = false) }
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
        updateComposer(TextFieldValue(text, TextRange(text.length)))
    }

    fun updateComposer(value: TextFieldValue) {
        _ui.update { current ->
            val slash = value.text.startsWith("/") && value.text.none(Char::isWhitespace)
            val mention = if (value.selection.collapsed && !slash) {
                AgentFileMentions.atCaret(value.text, value.selection.end)
            } else null
            current.copy(
                composerValue = value,
                slashOpen = slash,
                mentionPickerOpen = mention != null,
                mentionQuery = mention?.value.orEmpty(),
                mentionMatches = mention?.let { searchProjectFiles(current.projectFiles, it.value) }.orEmpty()
            )
        }
    }

    private fun searchProjectFiles(
        projectFiles: List<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo>,
        query: String
    ): List<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo> {
        if (query.isBlank()) return projectFiles.take(8)
        return projectFiles.asSequence()
            .filter { it.relativePath.contains(query, ignoreCase = true) }
            .sortedWith(
                compareByDescending<com.webtoapp.core.agent.files.ProjectFileManager.FileInfo> {
                    it.relativePath.substringAfterLast('/').startsWith(query, ignoreCase = true)
                }.thenBy { it.relativePath.length }
            )
            .take(8)
            .toList()
    }

    fun pickFileMention(path: String) {
        _ui.update { current ->
            if (!current.mentionPickerOpen || !current.composerValue.selection.collapsed ||
                current.mentionMatches.none { it.relativePath == path }
            ) return@update current
            val query = AgentFileMentions.atCaret(current.composerText, current.composerValue.selection.end)
                ?: return@update current
            val edited = AgentFileMentions.insert(current.composerText, query, path)
            current.copy(
                composerValue = TextFieldValue(edited.text, TextRange(edited.caret)),
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

    fun openSlashCommands() {
        _ui.update { it.copy(slashOpen = true, mentionPickerOpen = false) }
    }

    fun dismissSlash() {
        _ui.update { it.copy(slashOpen = false) }
    }

    fun runSlashCommand(command: SlashCommand, onPickModel: () -> Unit) {
        _ui.update {
            val typedCommand = it.composerText.startsWith("/") && it.composerText.none(Char::isWhitespace)
            it.copy(
                composerValue = if (typedCommand) TextFieldValue() else it.composerValue,
                slashOpen = false
            )
        }
        when (command) {
            SlashCommand.Model -> onPickModel()
            SlashCommand.Plan -> enterPlanMode()
            SlashCommand.ReviewPlan -> submitPlanForReview()
            SlashCommand.Compact -> compactNow()
            SlashCommand.NewSession -> newSession()
        }
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
        viewModelScope.launch { sessionStore.create() }
    }

    fun tryNewSession(): Boolean {
        val cur = _ui.value.currentSession
        val draft = _ui.value
        if (cur != null && cur.messages.isEmpty() && draft.composerText.isBlank() && draft.pendingAttachments.isEmpty()) {
            _ui.update { it.copy(info = Strings.agentNewSessionAlreadyEmpty) }
            return false
        }
        newSession()
        return true
    }

    fun selectSession(id: String) {
        viewModelScope.launch {
            if (sessionStore.get(id) != null) sessionStore.setCurrent(id)
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            sessionStore.delete(id)
            drafts.remove(id)
        }
    }

    fun pinSession(id: String, pinned: Boolean) {
        viewModelScope.launch { sessionStore.pin(id, pinned) }
    }

    fun renameSession(id: String, newTitle: String) {
        val title = newTitle.trim()
        if (title.isEmpty()) return
        viewModelScope.launch { sessionStore.rename(id, title) }
    }

    fun exportSession(id: String) {
        viewModelScope.launch {
            val session = sessionStore.get(id) ?: return@launch
            runCatching {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val markdown = com.webtoapp.ui.agent.components.SessionTranscript.render(
                        session, Strings.agentCopyThinkingHeader
                    )
                    val dir = java.io.File(ctx.cacheDir, "agent_exports").apply { mkdirs() }
                    val safe = session.title.ifBlank { session.id }
                        .replace(Regex("[^\\p{L}\\p{N}_-]+"), "_").take(40).trim('_')
                        .ifBlank { "session" }
                    val file = java.io.File(dir, "$safe.md")
                    file.writeText(markdown)
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        ctx, ctx.packageName + ".fileprovider", file
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/markdown"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TITLE, file.name)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    ctx.startActivity(
                        Intent.createChooser(intent, Strings.agentSessionExport)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }.onFailure { e ->
                AppLogger.w("AgentViewModel", "exportSession failed: ${e.message}")
                _ui.update { it.copy(info = Strings.agentFileOpenFailed) }
            }
        }
    }

    fun deleteSessionFile(relativePath: String) {
        val sid = _ui.value.currentSession?.id ?: return
        if (relativePath.startsWith("apk:")) return
        viewModelScope.launch {
            val deleted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                files.delete(sid, relativePath)
            }
            if (deleted) {
                refreshFiles(sid)
            } else {
                _ui.update { it.copy(info = Strings.agentFileNotFound) }
            }
        }
    }

    fun selectFile(path: String) {
        if (path.startsWith("apk:")) {
            _ui.update {
                it.copy(
                    selectedFilePath = path,
                    selectedFileContent = null,
                    previewFilePath = path
                )
            }
            return
        }
        val sid = _ui.value.currentSession?.id ?: return
        viewModelScope.launch {
            val text = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val w = files.readTextWindow(sid, path, 0, PREVIEW_MAX_LINES)
                when {
                    w == null -> null
                    w.binary -> Strings.agentFileBinaryPreview
                        .replace("%s", formatSize(w.totalBytes))
                    else -> w.lines.joinToString("\n") +
                        if (w.remainingLines > 0 || w.remainingCapped) "\n…" else ""
                }
            }
            _ui.update {
                it.copy(
                    selectedFilePath = path,
                    selectedFileContent = text,
                    previewFilePath = path
                )
            }
        }
    }

    fun attachmentPreviewFile(att: com.webtoapp.core.agent.session.UserAttachment): java.io.File? {
        val sid = _ui.value.currentSession?.id ?: return null
        return files.resolveSafe(sid, att.path)
    }

    fun setPreviewFile(path: String?) {
        _ui.update { it.copy(previewFilePath = path) }
    }

    fun openWithSystemChooser(path: String) {
        val file: java.io.File = if (path.startsWith("apk:")) {
            val apkName = path.removePrefix("apk:")
            val apk = _ui.value.builtApks.firstOrNull { it.apkName == apkName }
            if (apk != null) java.io.File(apk.apkPath) else return
        } else {
            val sid = _ui.value.currentSession?.id ?: return
            files.resolveSafe(sid, path) ?: return
        }
        if (!file.exists()) {
            _ui.update { it.copy(info = Strings.agentFileNotFound) }
            return
        }
        runCatching {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                ctx, ctx.packageName + ".fileprovider", file
            )
            val mime = if (path.startsWith("apk:")) "application/vnd.android.package-archive"
                       else getMimeType(file.name)
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(
                android.content.Intent.createChooser(intent, Strings.agentFileOpenWith)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }.onFailure { e ->
            AppLogger.w("AgentViewModel", "openWithSystemChooser failed: ${e.message}")
            _ui.update { it.copy(info = Strings.agentFileOpenFailed) }
        }
    }

    private fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: when (ext) {
                "html", "htm" -> "text/html"
                "js" -> "application/javascript"
                "json" -> "application/json"
                "css" -> "text/css"
                "md" -> "text/markdown"
                else -> "*/*"
            }
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
        _ui.update { current ->
            if (current.currentSession?.id != sessionId) current
            else current.copy(
                projectFiles = list,
                mentionMatches = if (current.mentionPickerOpen) searchProjectFiles(list, current.mentionQuery)
                    else emptyList()
            )
        }
    }

    fun cancelTurn() {
        submissionJob?.cancel()
        service?.cancel()
        _ui.update {
            it.copy(
                phase = AgentUiState.Phase.Idle,
                currentActivity = null,
                streamingText = "",
                streamingThinkingSegments = emptyList(),
                pendingToolCalls = emptyList(),
                streamingDraftMessageId = null,
                pendingPermission = null,
                pendingChoice = null
            )
        }
        streamingSessionId = null
        streamText.clear(); streamThinkingSegments.clear(); streamTools.clear(); streamToolArgs.clear(); readFilesThisTurn.clear()
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
        val current = _ui.value
        if (!current.canSend) return
        val session = current.currentSession ?: return
        val gateway = service?.gateway ?: run {
            _ui.update { it.copy(error = Strings.agentServiceNotConnected) }
            return
        }
        _ui.update { it.copy(compacting = true) }
        viewModelScope.launch {
            try {
                val snapshot = sessionStore.get(session.id) ?: return@launch
                val textModel = resolveTextModel(configManager.savedModelsFlow.first())
                val keys = configManager.apiKeysFlow.first()
                val textKey = textModel?.let { m -> keys.firstOrNull { it.id == m.apiKeyId } }
                if (textModel == null || textKey == null) {
                    _ui.update { it.copy(error = Strings.agentMissingTextModel) }
                    return@launch
                }

                val svc = compactService ?: CompactService(gateway).also { compactService = it }
                val result = svc.compact(snapshot.id, snapshot.messages, textModel, textKey, peerKeys(textKey, keys))
                if (result.didCompact) {
                    val saved = sessionStore.replaceMessages(snapshot.id, result.messages, expectedMessages = snapshot.messages)
                    if (saved != null) {
                        _ui.update {
                            if (it.currentSession?.id != snapshot.id) it else it.copy(
                                info = Strings.agentCompactedManual.format(snapshot.messages.size, saved.messages.size),
                                estimatedContextTokens = svc.estimateTokens(saved.messages)
                            )
                        }
                    }
                } else if (_ui.value.currentSession?.id == snapshot.id) {
                    _ui.update { it.copy(info = Strings.agentCompactSkipped.format(result.reason)) }
                }
            } finally {
                _ui.update { it.copy(compacting = false) }
            }
        }
    }

    fun send() {
        val current = _ui.value
        if (!current.canSend) return
        val rawMessage = current.composerText.trim()
        if (rawMessage.isEmpty() && current.pendingAttachments.isEmpty()) return

        if (rawMessage == "/model" || rawMessage.startsWith("/model ")) {
            val arg = rawMessage.removePrefix("/model").trim()
            _ui.update { it.copy(composerValue = TextFieldValue(), slashOpen = false) }
            if (arg.isEmpty()) openModelPicker() else selectModelByName(arg)
            return
        }

        if (current.modelMissing) {
            _ui.update { it.copy(error = Strings.agentMissingTextModel) }
            return
        }

        _ui.update { it.copy(phase = AgentUiState.Phase.Connecting) }

        submissionJob = viewModelScope.launch {
            try {
                val session = (current.currentSession?.let { sessionStore.get(it.id) }
                    ?: ensureActiveSession()) ?: run {
                    resetTurnUiState(); return@launch
                }
                if (_ui.value.currentSession?.id != session.id) return@launch
                val pending = current.pendingAttachments
                val mentionedPaths = AgentFileMentions.paths(rawMessage) { files.exists(session.id, it) }

                val editingId = current.editingMessageId
                val baseSession = if (editingId != null) {
                    sessionStore.truncateAt(session.id, editingId, keep = false) ?: session
                } else session

                val userMsg = AgentMessage(
                    role = AgentMessage.Role.USER,
                    content = rawMessage,
                    mentionedFiles = mentionedPaths,
                    userAttachments = pending
                )
                val updated = sessionStore.appendMessage(baseSession.id, userMsg) ?: run {
                    resetTurnUiState(); return@launch
                }

                _ui.update { latest ->
                    if (latest.currentSession?.id != session.id) latest else {
                        val submittedText = latest.composerText == current.composerText
                        latest.copy(
                            composerValue = if (submittedText) TextFieldValue() else latest.composerValue,
                            editingMessageId = null,
                            slashOpen = !submittedText && latest.slashOpen,
                            mentionPickerOpen = !submittedText && latest.mentionPickerOpen,
                            mentionQuery = if (submittedText) "" else latest.mentionQuery,
                            mentionMatches = if (submittedText) emptyList() else latest.mentionMatches,
                            pendingAttachments = latest.pendingAttachments - pending.toSet()
                        )
                    }
                }
                if (_ui.value.currentSession?.id != session.id) return@launch

                overflowAutoRetry.set(false)
                dispatchTurn(updated, rawMessage)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (t: Throwable) {
                resetTurnUiState()
                throw t
            }
        }
    }

    private fun resetTurnUiState() {
        _ui.update { it.copy(phase = AgentUiState.Phase.Idle, error = null, currentActivity = null) }
    }

    fun attachUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val selectedId = _ui.value.currentSession?.id
        viewModelScope.launch {
            val sessionId = selectedId ?: ensureActiveSession()?.id ?: return@launch
            val added = withContext(Dispatchers.IO) {
                val bigPick = uris.any {
                    val size = querySizeBytes(it)
                    size == null || size > IMPORTING_HINT_MIN_BYTES
                }
                if (bigPick) showAttachmentStatus(sessionId, Strings.agentAttachImporting)
                uris.mapNotNull { uri -> importUriToUploads(sessionId, uri) }
            }
            addPendingAttachments(sessionId, added)
            if (added.size < uris.size) showAttachmentStatus(sessionId, Strings.agentAttachFailed)
        }
    }

    fun attachFolder(treeUri: Uri) {
        val selectedId = _ui.value.currentSession?.id
        viewModelScope.launch {
            val sessionId = selectedId ?: ensureActiveSession()?.id ?: return@launch
            showAttachmentStatus(sessionId, Strings.agentAttachImporting)
            val added = withContext(Dispatchers.IO) { importFolderToUploads(sessionId, treeUri) }
            if (added != null) addPendingAttachments(sessionId, listOf(added))
            else showAttachmentStatus(sessionId, Strings.agentAttachFailed)
        }
    }

    private fun addPendingAttachments(sessionId: String, added: List<UserAttachment>) {
        if (added.isEmpty()) return
        if (_ui.value.currentSession?.id != sessionId) {
            if (_ui.value.sessions.any { it.id == sessionId }) {
                val draft = drafts[sessionId] ?: ComposerDraft()
                drafts[sessionId] = draft.copy(attachments = (draft.attachments + added).distinctBy { it.path })
            }
            return
        }
        _ui.update { it.copy(pendingAttachments = (it.pendingAttachments + added).distinctBy { att -> att.path }) }
        refreshFiles(sessionId)
    }

    private fun showAttachmentStatus(sessionId: String, message: String) {
        _ui.update { if (it.currentSession?.id == sessionId) it.copy(info = message) else it }
    }

    fun removePendingAttachment(path: String) {
        val sid = _ui.value.currentSession?.id
        if (sid != null) runCatching {
            val target = files.resolveSafe(sid, path)
            if (target?.isDirectory == true) target.deleteRecursively()
            else files.delete(sid, path)
        }
        _ui.update { it.copy(pendingAttachments = it.pendingAttachments.filter { it.path != path }) }
        if (sid != null) refreshFiles(sid)
    }

    private fun importUriToUploads(sessionId: String, uri: Uri): UserAttachment? {
        val name = queryDisplayName(uri) ?: "upload_${System.currentTimeMillis()}"
        val mime = runCatching { ctx.contentResolver.getType(uri) }.getOrNull()
            ?: mimeFromName(name)
        val input = runCatching { ctx.contentResolver.openInputStream(uri) }.getOrNull()
            ?: return null
        return input.use { writeUploadStream(sessionId, "uploads", name, it, mime) }
    }

    private fun importFolderToUploads(sessionId: String, treeUri: Uri): UserAttachment? {
        val tree = runCatching { DocumentFile.fromTreeUri(ctx, treeUri) }.getOrNull()
            ?: return null
        val folderName = (tree.name ?: "folder").replace('/', '_')
        var base = "uploads/$folderName"
        var i = 1
        while (files.resolveSafe(sessionId, base)?.exists() == true) {
            base = "uploads/$folderName-$i"
            i++
        }
        var count = 0
        var totalBytes = 0L
        var truncated = false
        fun walk(dir: DocumentFile, relBase: String) {
            dir.listFiles().forEach { child ->
                if (truncated) return@forEach
                val childName = child.name?.replace('/', '_') ?: return@forEach
                if (child.isDirectory) {
                    walk(child, "$relBase$childName/")
                } else {
                    if (count >= MAX_FOLDER_IMPORT_FILES) {
                        truncated = true
                        return@forEach
                    }
                    runCatching { ctx.contentResolver.openInputStream(child.uri) }
                        .getOrNull()?.use { input ->
                            files.writeStream(sessionId, "$relBase$childName", input)
                        }?.let { info ->
                            count++
                            totalBytes += info.sizeBytes
                        }
                }
            }
        }
        walk(tree, "$base/")
        files.resolveSafe(sessionId, base)?.mkdirs()
        if (truncated) {
            files.writeText(
                sessionId,
                "$base/IMPORT-TRUNCATED.txt",
                "Folder import stopped at $MAX_FOLDER_IMPORT_FILES files; the source folder has more."
            )
        }
        return UserAttachment(
            path = "$base/",
            displayName = base.removePrefix("uploads/") + "/",
            mimeType = "",
            isImage = false,
            sizeBytes = totalBytes,
            entryCount = count
        )
    }

    private fun writeUploadStream(
        sessionId: String,
        dir: String,
        name: String,
        input: java.io.InputStream,
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
        val info = files.writeStream(sessionId, candidate, input) ?: return null
        return UserAttachment(
            path = candidate,
            displayName = candidate.removePrefix("uploads/"),
            mimeType = mime,
            isImage = mime.startsWith("image/"),
            sizeBytes = info.sizeBytes
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

    private fun querySizeBytes(uri: Uri): Long? = runCatching {
        ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (idx >= 0 && c.moveToFirst() && !c.isNull(idx)) c.getLong(idx) else null
        }
    }.getOrNull()

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

    fun openContextPicker() {
        if (_ui.value.contextPickerOpen) return
        _ui.update { it.copy(contextPickerOpen = true, contextLoading = true) }
        viewModelScope.launch {
            try {
                ensureActiveSession()
                val apps = webAppRepository.allWebApps.first()
                    .map { ContextAppItem(it.id, it.name, it.appType.name, it.categoryId) }
                val categories = categoryRepository.allCategories.first().sortedBy { it.sortOrder }
                    .map { ContextCategoryItem(it.id, it.name, it.icon) }
                val store = com.webtoapp.core.plugin.PluginStore.getInstance(ctx)
                store.awaitLoaded()
                val modules = store.getAllPlugins().map { ContextModuleItem(it.id, it.name, it.kind.name) }
                _ui.update {
                    it.copy(
                        availableContextApps = apps,
                        availableContextModules = modules,
                        availableCategories = categories
                    )
                }
                refreshSkills()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.w("AgentViewModel", "Context picker load failed: ${e.javaClass.simpleName}")
                _ui.update { it.copy(error = Strings.loadFailed) }
            } finally {
                _ui.update { it.copy(contextLoading = false) }
            }
        }
    }

    fun closeContextPicker() {
        _ui.update { it.copy(contextPickerOpen = false) }
    }

    fun toggleContextApp(id: Long) = updateContextConfig { config ->
        config.copy(contextAppIds = withToggledItem(config.contextAppIdsSafe, id))
    }

    fun toggleContextModule(id: String) = updateContextConfig { config ->
        config.copy(contextModuleIds = withToggledItem(config.contextModuleIdsSafe, id))
    }

    fun toggleContextSkill(id: String) = updateContextConfig { config ->
        val ids = withToggledItem(config.skillIdsSafe, id)
        val skills = _ui.value.availableSkills
        if (id in ids && skills.filter { it.id in ids }.sumOf { it.bytes } > AgentSkillLibrary.maxSelectedBytes) {
            _ui.update { it.copy(error = Strings.agentSkillBudgetExceeded) }
            config
        } else {
            config.copy(skillIds = ids)
        }
    }

    private fun updateContextConfig(transform: (SessionConfig) -> SessionConfig) {
        val sid = _ui.value.currentSession?.id ?: return
        viewModelScope.launch { sessionStore.updateConfig(sid, transform) }
    }

    private fun <T> withToggledItem(ids: List<T>, id: T): List<T> =
        if (id in ids) ids - id else ids + id

    fun importSkill(uri: Uri) {
        if (_ui.value.importingSkill) return
        _ui.update { it.copy(importingSkill = true) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val name = DocumentFile.fromSingleUri(ctx, uri)?.name ?: "SKILL.md"
                    val input = requireNotNull(ctx.contentResolver.openInputStream(uri))
                    input.use { skillLibrary.import(it, name) }
                }
                refreshSkills()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.w("AgentViewModel", "Skill import failed: ${e.javaClass.simpleName}")
                _ui.update { it.copy(error = Strings.agentSkillImportFailed) }
            } finally {
                _ui.update { it.copy(importingSkill = false) }
            }
        }
    }

    fun removeSkill(id: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { skillLibrary.remove(id) }
            sessionStore.removeSkillReferences(id)
            refreshSkills()
        }
    }

    private suspend fun refreshSkills() {
        val skills = withContext(Dispatchers.IO) {
            skillLibrary.list().map { ContextSkillItem(it.id, it.name, it.description, it.sizeBytes) }
        }
        _ui.update { it.copy(availableSkills = skills) }
    }

    fun setLocalMcpEnabled(enabled: Boolean) {
        viewModelScope.launch {
            HostRuntimePrefs.getInstance(ctx).setMcpEnabled(enabled)
            HostMcpController.apply(ctx)
        }
    }

    suspend fun toolCatalog(): List<AgentToolItem> {
        val session = _ui.value.currentSession ?: return emptyList()
        val factory = registryFactory ?: run {
            val checker = service?.permissionChecker ?: return emptyList()
            ToolRegistryFactory(PlanManager(session.id, files, checker), imageRegistry)
        }
        val imageModelId = session.config.imageModelId
        val hasImageModel = imageModelId != null && configManager.savedModelsFlow.first().any { it.id == imageModelId }
        return factory.build(hasImageModel).all.sortedBy { it.name }.map {
            AgentToolItem(it.name, it.description.lineSequence().first().trim(), it.isReadOnly())
        }
    }

    private suspend fun buildSelectedContext(config: SessionConfig): String {
        val apps = config.contextAppIdsSafe.distinct().mapNotNull { id ->
            val app = webAppRepository.getWebApp(id) ?: return@mapNotNull null
            SelectedContextSection.Reference(app.id.toString(), app.name, app.appType.name)
        }
        val modules = if (config.contextModuleIdsSafe.isEmpty()) emptyList() else {
            val store = com.webtoapp.core.plugin.PluginStore.getInstance(ctx)
            store.awaitLoaded()
            store.getPluginsByIds(config.contextModuleIdsSafe.distinct()).map { plugin ->
                SelectedContextSection.Reference(plugin.id, plugin.name, plugin.kind.name)
            }
        }
        val skills = if (config.skillIdsSafe.isEmpty()) emptyList() else withContext(Dispatchers.IO) {
            skillLibrary.selected(config.skillIdsSafe)
        }
        return SelectedContextSection.build(Strings.currentLanguage.value.toPromptLang(), apps, modules, skills)
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
                composerValue = TextFieldValue(message.content, TextRange(message.content.length))
            )
        }
    }

    fun cancelEditing() {
        _ui.update { it.copy(editingMessageId = null, composerValue = TextFieldValue()) }
    }

    fun regenerate(messageId: String) {
        val sid = _ui.value.currentSession?.id ?: return
        if (!_ui.value.canSend) return
        _ui.update { it.copy(phase = AgentUiState.Phase.Connecting) }
        viewModelScope.launch {
            try {
                val s0 = sessionStore.get(sid) ?: run {
                    resetTurnUiState(); return@launch
                }
                val idx = s0.messages.indexOfFirst { it.id == messageId }
                if (idx < 0) {
                    resetTurnUiState(); return@launch
                }
                val precedingUser = s0.messages.subList(0, idx)
                    .lastOrNull { it.role == AgentMessage.Role.USER } ?: run {
                    resetTurnUiState(); return@launch
                }

                val truncated = sessionStore.truncateAt(sid, messageId, keep = false) ?: run {
                    resetTurnUiState(); return@launch
                }
                overflowAutoRetry.set(false)
                dispatchTurn(truncated, precedingUser.content)
            } catch (t: Throwable) {
                resetTurnUiState()
                throw t
            }
        }
    }

    private fun peerKeys(bound: ApiKeyConfig, keys: List<ApiKeyConfig>): List<ApiKeyConfig> =
        ApiKeyRotation.poolFor(bound, keys)

    private suspend fun dispatchTurn(
        session: AgentSession,
        prompt: String
    ) {
        val models = configManager.savedModelsFlow.first()
        val keys = configManager.apiKeysFlow.first()

        val textModel = resolveTextModel(models)
        if (textModel == null) {
            streamingSessionId = null
            _ui.update { it.copy(phase = AgentUiState.Phase.Idle, error = Strings.agentMissingTextModel) }
            return
        }
        val textKey = keys.firstOrNull { it.id == textModel.apiKeyId }
        if (textKey == null) {
            streamingSessionId = null
            _ui.update { it.copy(phase = AgentUiState.Phase.Idle, error = Strings.agentMissingApiKey) }
            return
        }
        val imageModel = session.config.imageModelId?.let { id -> models.firstOrNull { it.id == id } }
        val imageKey = imageModel?.let { m -> keys.firstOrNull { it.id == m.apiKeyId } }

        val compactSvc = compactService ?: CompactService(service!!.gateway).also { compactService = it }
        val contextLength = textModel.effectiveContextLength

        val plan = planManager ?: PlanManager(session.id, files, service!!.permissionChecker).also {
            planManager = it
        }
        val factory = registryFactory ?: ToolRegistryFactory(plan, imageRegistry).also {
            registryFactory = it
        }
        val registry = factory.build(hasImageModel = imageModel != null)

        val projectSummary = withContext(Dispatchers.IO) {
            files.listAll(session.id).take(40).map { f ->
                val lines = if (f.isText) files.countLines(session.id, f.relativePath) else -1
                com.webtoapp.core.agent.prompt.sections.ProjectFilesSection.FileSummary(
                    f.relativePath,
                    lines,
                    f.sizeBytes
                )
            }
        }

        val planState = plan.state.value
        val planMode = if (planState.active && planState.activePlanPath != null) {
            SystemPromptBuilder.PlanMode(
                planFilePath = planState.activePlanPath,
                planExists = plan.planExists()
            )
        } else null

        val sessionRoot = files.getSessionRoot(session.id).absolutePath
        val selectedContext = buildSelectedContext(session.config)

        val modelName = textModel.alias?.takeIf { it.isNotBlank() } ?: textModel.model.name
        var parts = buildRequestParts(session, prompt, modelName, sessionRoot, selectedContext, projectSummary, planMode)
        var requestTokens = compactSvc.estimateRequestTokens(
            parts.systemPrompt, parts.history, parts.userMessage, registry.all
        )
        var didAutoCompact = false
        if (compactSvc.shouldCompactRequest(requestTokens, contextLength)) {
            val result = compactSvc.compact(session.id, session.messages, textModel, textKey, peerKeys(textKey, keys))
            if (result.didCompact) {
                sessionStore.replaceMessages(session.id, result.messages)
                val compacted = sessionStore.get(session.id) ?: session
                parts = buildRequestParts(compacted, prompt, modelName, sessionRoot, selectedContext, projectSummary, planMode)
                requestTokens = compactSvc.estimateRequestTokens(
                    parts.systemPrompt, parts.history, parts.userMessage, registry.all
                )
                didAutoCompact = true
            }
        }

        val workingSession = parts.session
        streamingSessionId = workingSession.id
        streamText.clear(); streamThinkingSegments.clear(); streamTools.clear(); streamToolArgs.clear(); readFilesThisTurn.clear()
        readFilesThisTurn += parts.tailMentions
        _ui.update {
            it.copy(
                phase = AgentUiState.Phase.Connecting,
                streamingText = "",
                streamingThinkingSegments = emptyList(),
                pendingToolCalls = emptyList(),
                currentActivity = null,
                error = null,
                info = if (didAutoCompact) Strings.agentCompactedAuto else null,
                estimatedContextTokens = requestTokens,
                contextCapacity = contextLength
            )
        }

        val toolCtx = ToolContext(
            androidContext = ctx,
            sessionId = workingSession.id,
            fileManager = files,
            textModel = textModel,
            textApiKey = textKey,
            textKeyPool = peerKeys(textKey, keys),
            imageModel = imageModel,
            imageApiKey = imageKey,
            imageKeyPool = imageKey?.let { peerKeys(it, keys) }.orEmpty(),
            prompter = service!!.permissionPrompter,
            todos = todoManager,
            appRepository = webAppRepository,
            readFiles = readFilesThisTurn,
            activePlanFile = planState.activePlanPath
        )

        service?.start(
            AgentService.AgentRequest(
                sessionId = workingSession.id,
                systemPrompt = parts.systemPrompt,
                history = parts.history,

                userMessage = parts.userMessage,
                toolContext = toolCtx,
                registry = registry,
                sessionStore = sessionStore,
                temperature = session.config.temperature,
                maxTurns = session.config.maxTurns
            )
        )
    }

    private suspend fun retryAfterContextOverflow(sessionId: String, originalError: String) {
        suspend fun giveUp() {
            _ui.update { it.copy(phase = AgentUiState.Phase.Idle, error = originalError) }
        }

        val session = sessionStore.get(sessionId) ?: run { giveUp(); return }
        val lastUserIdx = session.messages.indexOfLast { it.role == AgentMessage.Role.USER }
        if (lastUserIdx < 0) { giveUp(); return }
        val userMsg = session.messages[lastUserIdx]

        var kept = session.messages.take(lastUserIdx + 1)

        val models = configManager.savedModelsFlow.first()
        val keys = configManager.apiKeysFlow.first()
        val textModel = resolveTextModel(models)
        val textKey = textModel?.let { m -> keys.firstOrNull { it.id == m.apiKeyId } }
        val gateway = service?.gateway
        if (textModel != null && textKey != null && gateway != null) {
            val svc = compactService ?: CompactService(gateway).also { compactService = it }
            val result = svc.compact(session.id, kept, textModel, textKey, peerKeys(textKey, keys))
            if (result.didCompact) kept = result.messages
        }

        val saved = sessionStore.replaceMessages(sessionId, kept) ?: run { giveUp(); return }
        dispatchTurn(saved, userMsg.content)
    }

    private data class OutgoingRequest(
        val session: AgentSession,
        val systemPrompt: String,
        val history: List<LlmMessage>,
        val userMessage: String,
        val tailMentions: List<String>
    )

    private fun buildRequestParts(
        session: AgentSession,
        prompt: String,
        modelName: String,
        sessionRoot: String,
        selectedContext: String,
        projectSummary: List<com.webtoapp.core.agent.prompt.sections.ProjectFilesSection.FileSummary>,
        planMode: SystemPromptBuilder.PlanMode?
    ): OutgoingRequest {
        val systemPrompt = SystemPromptBuilder.build(
            SystemPromptBuilder.Input(
                language = Strings.currentLanguage.value,
                modelName = modelName,
                sessionDir = sessionRoot,
                projectFiles = projectSummary,

                planMode = planMode,
                selectedContext = selectedContext
            )
        )

        val tailMessage = session.messages.lastOrNull()
        val tailInHistory = tailMessage?.role == AgentMessage.Role.USER &&
            (tailMessage.mentionedFiles.isNotEmpty() ||
                tailMessage.userAttachmentsSafe.isNotEmpty())

        val messagesForHistory = if (tailInHistory) {
            session.messages
        } else {
            session.messages.dropLast(1)
        }

        val history = messagesForHistory.flatMap { m ->
            when (m.role) {
                AgentMessage.Role.USER -> {
                    val out = mutableListOf<LlmMessage>()
                    val images = m.userAttachmentsSafe.filter { it.isImage }.mapNotNull { att ->
                        val f = files.resolveSafe(session.id, att.path)
                            ?: return@mapNotNull null
                        if (!f.isFile || f.length() > MAX_INLINE_IMAGE_BYTES) {
                            return@mapNotNull null
                        }
                        val bytes = runCatching { f.readBytes() }.getOrNull()
                            ?: return@mapNotNull null
                        com.webtoapp.core.agent.tool.ImageAttachment(bytes, att.mimeType, att.path)
                    }
                    val note = com.webtoapp.core.agent.session.AttachmentNote.build(
                        m.userAttachmentsSafe
                    ) { p -> files.resolveSafe(session.id, p) }
                    out += LlmMessage(LlmMessage.Role.USER, m.content + note, images = images)
                    out += synthesiseReadCallsFor(m, session.id)
                    out
                }
                AgentMessage.Role.ASSISTANT -> {
                    val out = mutableListOf<LlmMessage>()
                    out += LlmMessage(
                        role = LlmMessage.Role.ASSISTANT,

                        content = stripInlineMarkers(m.content),
                        toolCalls = m.toolCalls.map {
                            com.webtoapp.core.agent.llm.LlmToolCall(
                                it.toolCallId, it.name, it.argumentsJson, it.thoughtSignature
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

        return OutgoingRequest(
            session = session,
            systemPrompt = systemPrompt,
            history = history,
            userMessage = if (tailInHistory) "" else prompt,
            tailMentions = if (tailInHistory) tailMessage.mentionedFiles else emptyList()
        )
    }

    fun togglePlanMode() {
        val state = _ui.value
        if (!state.canChangePlanMode) return
        if (state.planActive) {
            planManager?.leave()
            _ui.update { it.copy(planActive = false, planFilePath = null) }
        } else {
            enterPlanMode()
        }
    }

    private fun enterPlanMode() {
        val state = _ui.value
        if (!state.canChangePlanMode) return
        val session = state.currentSession ?: return
        val checker = service?.permissionChecker ?: run {
            _ui.update { it.copy(error = Strings.agentServiceNotConnected) }
            return
        }
        val plan = planManager ?: PlanManager(session.id, files, checker).also {
            planManager = it
            registryFactory = ToolRegistryFactory(it, imageRegistry)
        }
        when (val r = plan.enter()) {
            is PlanManager.EnterResult.Entered -> {
                _ui.update { it.copy(planActive = true, planFilePath = r.planPath) }
            }
            is PlanManager.EnterResult.AlreadyActive -> {
                _ui.update { it.copy(planActive = true, planFilePath = r.planPath, info = Strings.agentPlanAlreadyActive) }
            }
        }
    }

    fun submitPlanForReview() {
        if (!_ui.value.canChangePlanMode) return
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
        plan.leave()
        _ui.update {
            it.copy(
                planActive = false,
                planFilePath = null,
                pendingPlanReview = null,
                composerValue = TextFieldValue(Strings.agentPlanApprovedMessage)
            )
        }
        send()
    }

    fun rejectPlan() {
        val plan = planManager ?: return
        plan.revise()
        _ui.update { it.copy(pendingPlanReview = null, composerValue = TextFieldValue()) }
    }

    private fun maybePushToolUi(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastToolUiPushAt < 48L) return
        lastToolUiPushAt = now
        _ui.update {
            it.copy(pendingToolCalls = streamTools.values.toList())
        }
    }

    private var lastStreamUiPushAt = 0L

    private fun maybePushStreamUi(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastStreamUiPushAt < STREAM_UI_THROTTLE_MS) return
        lastStreamUiPushAt = now
        _ui.update {
            it.copy(
                streamingText = streamText.toString(),
                streamingThinkingSegments = streamThinkingSegments.toList()
            )
        }
    }

    private fun observeStreams() {
        viewModelScope.launch {
            sessionStore.selectionFlow.collect { (sessions, currentId) ->
                val current = sessions.firstOrNull { it.id == currentId }
                _ui.update { it.copy(sessions = sessions) }
                if (current == null) ensureActiveSession() else applySession(current)
                drafts.keys.retainAll(sessions.mapTo(mutableSetOf()) { it.id })
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
        try {
            ContextCompat.startForegroundService(ctx, intent)
        } catch (e: Exception) {
            AppLogger.w("AgentViewModel", "startForegroundService rejected, falling back to bind only", e)
        }
        ctx.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun attachToService() {
        resumeFromActiveTurn()

        eventCollectionJob?.cancel()
        eventCollectionJob = viewModelScope.launch {
            service?.events?.collect { handleAgentEvent(it) }
        }
        permissionCollectionJob?.cancel()
        permissionCollectionJob = viewModelScope.launch {
            service?.permissionPrompter?.requests?.collect { req ->
                _ui.update { it.copy(pendingPermission = req) }
            }
        }
        choiceCollectionJob?.cancel()
        choiceCollectionJob = viewModelScope.launch {
            service?.permissionPrompter?.choices?.collect { req ->
                _ui.update { it.copy(pendingChoice = req) }
            }
        }
        activeTurnJob?.cancel()
        activeTurnJob = viewModelScope.launch {
            service?.activeTurn?.collect { snap ->
                val draftId = snap?.takeIf { it.isRunning }?.draftMessage?.id
                _ui.update { it.copy(streamingDraftMessageId = draftId) }
            }
        }

        applyAutoApproveToService(_ui.value.autoApprove)
    }

    private fun resumeFromActiveTurn() {
        val svc = service ?: return
        val snapshot = svc.activeTurn.value ?: return
        if (!snapshot.isRunning) return
        val draft = snapshot.draftMessage ?: return
        if (_ui.value.currentSession?.id != snapshot.sessionId) return

        streamingSessionId = snapshot.sessionId
        streamText.setLength(0)
        streamText.append(draft.content)
        streamThinkingSegments.clear()
        val draftSegments = draft.thinkingSegmentsSafe
        if (draftSegments.isNotEmpty()) {
            val now = System.currentTimeMillis()
            draftSegments.forEach { seg ->
                streamThinkingSegments += ThinkingSegment(
                    id = seg.id,
                    content = seg.content,
                    startedAt = now,
                    frozenDurationMs = seg.durationMs ?: 0L
                )
            }
        } else if (!draft.thinking.isNullOrBlank()) {
            streamThinkingSegments += ThinkingSegment(
                id = "th-restored",
                content = draft.thinking,
                startedAt = System.currentTimeMillis(),
                frozenDurationMs = draft.thinkingDurationMs
            )
        }
        streamTools.clear()
        streamToolArgs.clear()
        draft.toolCalls.forEach { tc ->
            streamTools[tc.toolCallId] = tc
        }
        _ui.update {
            it.copy(
                phase = AgentUiState.Phase.Streaming,
                streamingText = draft.content,
                streamingThinkingSegments = streamThinkingSegments.toList(),
                pendingToolCalls = streamTools.values.toList(),
                currentActivity = streamTools.values.lastOrNull()?.activity
            )
        }
    }

    private suspend fun handleAgentEvent(ev: AgentEvent) {
        val sid = streamingSessionId ?: return
        when (ev) {
            AgentEvent.Started -> _ui.update { it.copy(phase = AgentUiState.Phase.Streaming) }
            is AgentEvent.TextDelta -> {
                streamText.setLength(0); streamText.append(ev.accumulated)
                maybePushStreamUi()
            }
            is AgentEvent.ThinkingDelta -> {
                val now = System.currentTimeMillis()
                val idx = streamThinkingSegments.indexOfFirst { it.id == ev.segmentId }
                if (idx < 0) {
                    streamThinkingSegments += ThinkingSegment(
                        id = ev.segmentId,
                        content = ev.delta,
                        startedAt = now
                    )
                } else {
                    streamThinkingSegments[idx] =
                        streamThinkingSegments[idx].copy(content = streamThinkingSegments[idx].content + ev.delta)
                }
                maybePushStreamUi()
            }
            AgentEvent.ThinkingTurnEnded -> {
                var changed = false
                for (i in streamThinkingSegments.indices) {
                    val seg = streamThinkingSegments[i]
                    if (seg.frozenDurationMs == null) {
                        streamThinkingSegments[i] =
                            seg.copy(frozenDurationMs = System.currentTimeMillis() - seg.startedAt)
                        changed = true
                    }
                }
                if (changed) {
                    _ui.update {
                        it.copy(streamingThinkingSegments = streamThinkingSegments.toList())
                    }
                }
            }
            is AgentEvent.ToolCallStarted -> {
                streamTools[ev.toolCallId] = RecordedToolCall(
                    toolCallId = ev.toolCallId,
                    name = ev.name,
                    argumentsJson = "",
                    resultPreview = "",
                    ok = true,
                    status = RecordedToolCall.Status.Running
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
                    activity = prev?.activity,
                    thoughtSignature = ev.thoughtSignature ?: prev?.thoughtSignature,
                    status = if (ev.result.isError) RecordedToolCall.Status.Failed else RecordedToolCall.Status.Completed
                )
                streamToolArgs.remove(ev.toolCallId)
                lastToolUiPushAt = 0L
                val planState = planManager?.state?.value
                _ui.update {
                    it.copy(
                        phase = AgentUiState.Phase.Streaming,
                        pendingToolCalls = streamTools.values.toList(),
                        planActive = planState?.active == true,
                        planFilePath = planState?.activePlanPath,
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
            is AgentEvent.AppChanged -> {
                _ui.update { state ->
                    val ch = ev.change
                    val entry = PendingAppChange(
                        appId = ch.appId,
                        appName = ch.appName,
                        appType = ch.appType,
                        created = ch.kind == com.webtoapp.core.agent.tool.AppChange.Kind.CREATE,
                        changedFields = ch.changedFields,
                        changedAt = System.currentTimeMillis()
                    )
                    val rest = state.pendingAppChanges.filterNot { it.appId == ch.appId }
                    state.copy(pendingAppChanges = listOf(entry) + rest)
                }
            }
            is AgentEvent.ApkBuilt -> {
                _ui.update { state ->
                    val rest = state.builtApks.filterNot { it.appId == ev.info.appId }
                    state.copy(builtApks = rest + ev.info)
                }
                val sid0 = sid
                viewModelScope.launch {
                    val apk = PersistedApk(
                        appId = ev.info.appId,
                        apkName = ev.info.apkName,
                        apkPath = ev.info.apkPath,
                        sizeBytes = ev.info.sizeBytes,
                        buildMode = ev.info.buildMode,
                        packageName = ev.info.packageName,
                        versionName = ev.info.versionName
                    )
                    sessionStore.updateConfig(sid0) { config ->
                        config.copy(builtApks = config.builtApksSafe.filterNot { it.appId == apk.appId } + apk)
                    }
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
                streamingSessionId = null
                streamText.clear(); streamThinkingSegments.clear(); streamTools.clear(); streamToolArgs.clear(); readFilesThisTurn.clear()
                lastToolUiPushAt = 0L
                lastStreamUiPushAt = 0L
                _ui.update {
                    it.copy(
                        phase = AgentUiState.Phase.Idle,
                        streamingText = "",
                        streamingThinkingSegments = emptyList(),
                        pendingToolCalls = emptyList(),
                        currentActivity = null,
                        info = if (ev.toolCallCount > 0)
                            Strings.agentTurnDoneToolCount.format(ev.toolCallCount)
                        else null
                    )
                }
                viewModelScope.launch { maybeAutoTitle(sid) }
            }
            is AgentEvent.PlanReviewRequired -> {
                val planContent = files.readText(sid, ev.planPath) ?: ""
                streamingSessionId = null
                streamText.clear(); streamThinkingSegments.clear(); streamTools.clear(); streamToolArgs.clear(); readFilesThisTurn.clear()
                _ui.update {
                    it.copy(
                        phase = AgentUiState.Phase.Idle,
                        streamingText = "",
                        streamingThinkingSegments = emptyList(),
                        pendingToolCalls = emptyList(),
                        currentActivity = null,
                        pendingPlanReview = PlanReview(planPath = ev.planPath, content = planContent)
                    )
                }
            }
            AgentEvent.Aborted -> {
                streamingSessionId = null
                streamText.clear(); streamThinkingSegments.clear(); streamTools.clear(); streamToolArgs.clear(); readFilesThisTurn.clear()
                _ui.update {
                    it.copy(
                        phase = AgentUiState.Phase.Idle,
                        streamingText = "",
                        streamingThinkingSegments = emptyList(),
                        pendingToolCalls = emptyList(),
                        currentActivity = null
                    )
                }
            }
            is AgentEvent.Failed -> {
                streamingSessionId = null
                streamText.clear(); streamThinkingSegments.clear(); streamTools.clear(); streamToolArgs.clear(); readFilesThisTurn.clear()
                if (CompactService.isContextOverflowError(ev.message) &&
                    overflowAutoRetry.compareAndSet(false, true)
                ) {
                    _ui.update {
                        it.copy(
                            phase = AgentUiState.Phase.Connecting,
                            streamingText = "",
                            streamingThinkingSegments = emptyList(),
                            pendingToolCalls = emptyList(),
                            currentActivity = null,
                            info = Strings.agentContextOverflowCompacting
                        )
                    }
                    viewModelScope.launch { retryAfterContextOverflow(sid, ev.message) }
                } else {
                    _ui.update {
                        it.copy(
                            phase = AgentUiState.Phase.Idle,
                            streamingText = "",
                            streamingThinkingSegments = emptyList(),
                            pendingToolCalls = emptyList(),
                            currentActivity = null,
                            error = ev.message
                        )
                    }
                }
            }
        }
    }

    private suspend fun maybeAutoTitle(sessionId: String) {
        val session = sessionStore.get(sessionId) ?: return
        if (!session.titleAutoGenerated) return

        val userMsgs = session.messages.count { it.role == AgentMessage.Role.USER }
        val assistantMsgs = session.messages.count { it.role == AgentMessage.Role.ASSISTANT }
        if (userMsgs != 1 || assistantMsgs != 1) return

        val gateway = service?.gateway ?: return
        val models = configManager.savedModelsFlow.first()
        val keys = configManager.apiKeysFlow.first()
        val textModel = resolveTextModel(models) ?: return
        val textKey = keys.firstOrNull { it.id == textModel.apiKeyId } ?: return

        val firstUser = session.messages.first { it.role == AgentMessage.Role.USER }.content
        val firstAssistant = stripInlineMarkers(
            session.messages.first { it.role == AgentMessage.Role.ASSISTANT }.content
        )

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
            useTools = false,
            sessionId = session.id
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
            return
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
            ?: return

        if (title == session.title.trim()) return
        sessionStore.setAutoTitle(session.id, title)
    }

    private fun synthesiseReadCallsFor(m: AgentMessage, sessionId: String): List<LlmMessage> {
        val pathsToRead = m.mentionedFiles.distinct()
        if (pathsToRead.isEmpty()) return emptyList()

        val out = mutableListOf<LlmMessage>()
        val toolCalls = mutableListOf<com.webtoapp.core.agent.llm.LlmToolCall>()
        val toolResults = mutableListOf<LlmMessage>()
        var budget = MENTION_TOTAL_CHAR_BUDGET

        pathsToRead.forEachIndexed { idx, path ->
            val callId = "mention-${m.id}-$idx"
            val argsJson = """{"path":"${path.replace("\"", "\\\"")}"}"""
            toolCalls += com.webtoapp.core.agent.llm.LlmToolCall(callId, "Read", argsJson)

            val window = files.readTextWindow(sessionId, path, 0, MENTION_LINE_LIMIT)
            val resultText = when {
                window == null ->
                    "(Read: $path no longer exists in the session)"
                window.binary ->
                    "(Read: $path is a binary file (${formatSize(window.totalBytes)}) — not readable as text)"
                else -> {
                    val numbered = window.lines
                        .mapIndexed { i, line -> "${i + 1}\t$line" }
                        .joinToString("\n")
                    val tail = when {
                        window.remainingCapped ->
                            "\n… (${window.remainingLines}+ more lines, ask Read with offset to see them)"
                        window.remainingLines > 0 ->
                            "\n… (${window.remainingLines} more lines, ask Read with offset to see them)"
                        else -> ""
                    }
                    val combined = numbered + tail
                    if (combined.length > budget) {
                        val truncated = combined.take(budget.coerceAtLeast(0))
                        "$truncated\n… (truncated to fit mention budget)"
                    } else combined
                }
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

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        bytes < 1024L * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
        else -> "%.1fGB".format(bytes / (1024.0 * 1024 * 1024))
    }

    private suspend fun attachSession(id: String) {
        sessionStore.get(id)?.let(::applySession)
    }

    private fun applySession(session: AgentSession) {
        val previous = _ui.value
        val previousId = previous.currentSession?.id
        if (previousId == session.id) {
            _ui.update { it.copy(currentSession = session) }
            return
        }
        val outgoing = previous.composerDraft()
        if (previousId != null) {
            if (outgoing == ComposerDraft()) drafts.remove(previousId)
            else drafts[previousId] = outgoing
            if (previous.isWorking) cancelTurn()
        }
        val incoming = drafts.remove(session.id) ?: if (previousId == null) outgoing else ComposerDraft()
        val checker = service?.permissionChecker
        checker?.resetToBaseline()
        planManager = checker?.let { PlanManager(session.id, files, it) }
        registryFactory = planManager?.let { ToolRegistryFactory(it, imageRegistry) }
        todoManager.clear()
        val restoredApks = session.config.builtApksSafe.map {
            BuiltApkInfo(
                appId = it.appId,
                apkName = it.apkName,
                apkPath = it.apkPath,
                sizeBytes = it.sizeBytes,
                buildMode = it.buildMode,
                packageName = it.packageName,
                versionName = it.versionName
            )
        }
        val projectFiles = files.listAll(session.id)
        _ui.update {
            it.copy(
                currentSession = session,
                composerValue = incoming.value,
                pendingAttachments = incoming.attachments,
                editingMessageId = incoming.editingMessageId,
                slashOpen = false,
                mentionPickerOpen = false,
                mentionQuery = "",
                mentionMatches = emptyList(),
                estimatedContextTokens = 0,
                planActive = false,
                planFilePath = null,
                pendingPlanReview = null,
                projectFiles = projectFiles,
                pendingChanges = emptyList(),
                builtApks = restoredApks,
                changesReviewExpanded = false,
                pendingAppChanges = emptyList(),
                appChangesExpanded = false,
                selectedFilePath = null,
                selectedFileContent = null,
                previewFilePath = null,
                previewOpen = false,
                drawerOpen = false
            )
        }
    }

    private data class ComposerDraft(
        val value: TextFieldValue = TextFieldValue(),
        val attachments: List<UserAttachment> = emptyList(),
        val editingMessageId: String? = null
    )

    private fun AgentUiState.composerDraft() = ComposerDraft(
        value = composerValue.copy(composition = null),
        attachments = pendingAttachments,
        editingMessageId = editingMessageId
    )

    override fun onCleared() {
        super.onCleared()
        eventCollectionJob?.cancel()
        permissionCollectionJob?.cancel()
        choiceCollectionJob?.cancel()
        activeTurnJob?.cancel()
        if (bound) runCatching { ctx.unbindService(connection) }
    }

    companion object {

        private val INLINE_MARKER_REGEX =
            Regex("\u2063(?:TC|TH):[^\u2063]+\u2063")

        fun stripInlineMarkers(text: String): String =
            if (text.isEmpty() || '\u2063' !in text) text
            else INLINE_MARKER_REGEX.replace(text, "")

        private const val MENTION_LINE_LIMIT = 1500

        private const val MENTION_TOTAL_CHAR_BUDGET = 60_000

        private const val MAX_INLINE_IMAGE_BYTES = 20L * 1024 * 1024

        private const val MAX_FOLDER_IMPORT_FILES = 500

        private const val IMPORTING_HINT_MIN_BYTES = 32L * 1024 * 1024

        private const val PREVIEW_MAX_LINES = 2_000

        private const val STREAM_UI_THROTTLE_MS = 66L

        private const val STREAM_PREVIEW_CHARS = 4_000

        private const val TOOL_RESULT_REPLAY_CHARS = 8_000

        fun factory(app: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AgentViewModel(app) as T
        }
    }
}
