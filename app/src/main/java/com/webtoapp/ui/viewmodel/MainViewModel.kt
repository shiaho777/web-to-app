package com.webtoapp.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.webtoapp.core.home.AppListSortStore
import com.webtoapp.data.dao.WebAppSummary
import com.webtoapp.data.home.AppListSort
import com.webtoapp.data.home.mergeVisibleOrder
import com.webtoapp.data.home.sortedForHome
import com.webtoapp.data.model.*
import com.webtoapp.data.repository.AppCategoryRepository
import com.webtoapp.ui.theme.ThemeManager
import com.webtoapp.util.HtmlStorage
import com.webtoapp.util.MediaStorage
import com.webtoapp.util.SplashStorage
import com.webtoapp.data.repository.WebAppRepository
import com.webtoapp.util.IconStorage
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.pwa.PwaAnalyzer
import com.webtoapp.core.pwa.PwaAnalysisResult
import com.webtoapp.core.pwa.PwaAnalysisState
import com.webtoapp.core.pwa.PwaDataSource
import android.content.Context
import com.webtoapp.util.HtmlProjectHelper
import com.webtoapp.util.ensureWebUrlScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@OptIn(FlowPreview::class)
class MainViewModel(
    application: Application,
    private val repository: WebAppRepository,
    private val categoryRepository: AppCategoryRepository
) : AndroidViewModel(application) {

    val webApps: StateFlow<List<WebApp>> = repository.allWebApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val webAppSummaries: StateFlow<List<WebAppSummary>> = repository.allWebAppSummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<AppCategory>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val categoryFilterStore = com.webtoapp.core.category.CategoryFilterStore(application)
    private val appListSortStore = AppListSortStore(application)

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    init {
        viewModelScope.launch {
            if (!categoryFilterStore.rememberEnabled) return@launch
            val saved = categoryFilterStore.loadSelection() ?: return@launch
            _selectedCategoryId.value = when {
                saved == -1L -> saved
                saved > 0L -> {
                    val existing = categoryRepository.allCategories.first()
                    saved.takeIf { id -> existing.any { it.id == id } }
                }
                else -> null
            }
        }
    }

    private val _currentApp = MutableStateFlow<WebApp?>(null)
    val currentApp: StateFlow<WebApp?> = _currentApp.asStateFlow()

    private val _editState = MutableStateFlow(EditState())
    val editState: StateFlow<EditState> = _editState.asStateFlow()

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _appListSort = MutableStateFlow(appListSortStore.load())
    val appListSort: StateFlow<AppListSort> = _appListSort.asStateFlow()

    fun setAppListSort(sort: AppListSort) {
        if (_appListSort.value == sort) return
        _appListSort.value = sort
        appListSortStore.save(sort)
    }

    fun switchToCustomOrder(visibleIds: List<Long>) {
        if (visibleIds.isEmpty()) return
        if (_appListSort.value != AppListSort.CUSTOM) {
            _appListSort.value = AppListSort.CUSTOM
            appListSortStore.save(AppListSort.CUSTOM)
        }
        saveVisibleHomeOrder(visibleIds)
    }

    fun saveVisibleHomeOrder(visibleIdsInOrder: List<Long>) {
        if (visibleIdsInOrder.isEmpty()) return
        viewModelScope.launch {
            try {
                val fullIds = webAppSummaries.value
                    .sortedForHome(AppListSort.CUSTOM)
                    .map { it.id }
                repository.setHomeSortOrder(mergeVisibleOrder(fullIds, visibleIdsInOrder))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: Strings.saveFailed)
            }
        }
    }

    private val _pwaAnalysisState = MutableStateFlow<PwaAnalysisState>(PwaAnalysisState.Idle)
    val pwaAnalysisState: StateFlow<PwaAnalysisState> = _pwaAnalysisState.asStateFlow()

    override fun onCleared() {
        super.onCleared()

        _editState.value.iconBitmap?.recycle()

        _editState.value = EditState()
        _currentApp.value = null
    }

    val filteredApps: StateFlow<List<WebApp>> = combine(
        webApps,
        searchQuery.debounce(300),
        selectedCategoryId,
        appListSort
    ) { apps, query, categoryId, sort ->
        var filtered = apps

        filtered = when (categoryId) {
            null -> filtered
            -1L -> filtered.filter { it.categoryId == null }
            else -> filtered.filter { it.categoryId == categoryId }
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.url.contains(query, ignoreCase = true)
            }
        }

        filtered.sortedForHome(sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredSummaries: StateFlow<List<WebAppSummary>> = combine(
        webAppSummaries,
        searchQuery.debounce(300),
        selectedCategoryId,
        appListSort
    ) { summaries, query, categoryId, sort ->
        var filtered = summaries

        filtered = when (categoryId) {
            null -> filtered
            -1L -> filtered.filter { it.categoryId == null }
            else -> filtered.filter { it.categoryId == categoryId }
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.url.contains(query, ignoreCase = true)
            }
        }

        filtered.sortedForHome(sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun getWebApp(id: Long): WebApp? = repository.getWebApp(id)

    fun deleteAppById(id: Long) {
        viewModelScope.launch {
            try {
                val webApp = repository.getWebApp(id)
                repository.deleteWebAppById(id)
                withContext(Dispatchers.IO) {
                    com.webtoapp.core.script.UserScriptStorage.deleteScriptsForApp(
                        getApplication(), id
                    )
                    if (webApp != null) {
                        com.webtoapp.core.app.ProjectDirCleaner.deleteForApp(getApplication(), webApp)
                    }
                }
                _uiState.value = UiState.Success(Strings.appDeleted)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: Strings.deleteFailed)
            }
        }
    }

    fun moveAppToCategoryById(id: Long, categoryId: Long?) {
        viewModelScope.launch {
            try {
                val webApp = repository.getWebApp(id) ?: return@launch
                repository.updateWebApp(webApp.copy(categoryId = categoryId))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: Strings.saveFailed)
            }
        }
    }

    fun createNewApp() {
        _editState.value = EditState()
        _currentApp.value = null
        _uiState.value = UiState.Idle
        _hasUnsavedChanges.value = false
        _pwaAnalysisState.value = PwaAnalysisState.Idle
    }

    fun createSampleApp() {
        viewModelScope.launch {
            try {
                val app = WebApp(
                    name = Strings.sampleAppName,
                    url = "https://shiaho777.github.io/web-to-app/"
                )
                withContext(Dispatchers.IO) { repository.createWebApp(app) }
                _uiState.value = UiState.Success(Strings.sampleAppCreated)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: Strings.saveFailed)
            }
        }
    }

    fun editApp(webApp: WebApp) {
        _currentApp.value = webApp
        _uiState.value = UiState.Idle
        _editState.value = webApp.toEditState()
        _hasUnsavedChanges.value = false
        _pwaAnalysisState.value = PwaAnalysisState.Idle

        if (webApp.webViewConfig.injectScripts.any {
            com.webtoapp.core.script.UserScriptStorage.isFileReference(it.code)
        }) {
            viewModelScope.launch {
                val resolvedScripts = withContext(Dispatchers.IO) {
                    com.webtoapp.core.script.UserScriptStorage.internalizeScripts(
                        getApplication(), webApp.webViewConfig.injectScripts
                    )
                }
                val currentState = _editState.value
                _editState.value = currentState.copy(
                    webViewConfig = currentState.webViewConfig.copy(injectScripts = resolvedScripts)
                )
            }
        }
    }

    fun updateEditState(update: EditState.() -> EditState) {
        _editState.value = _editState.value.update().withRuntimePermissionsSyncedFromFeatures()
        _hasUnsavedChanges.value = true
    }

    fun updateRuntimePermissions(transform: (ApkRuntimePermissions) -> ApkRuntimePermissions) {
        updateEditState {
            val currentExport = apkExportConfig
            copy(
                apkExportConfig = currentExport.copy(
                    runtimePermissions = transform(currentExport.runtimePermissions)
                )
            )
        }
    }

    fun analyzePwa(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _pwaAnalysisState.value = PwaAnalysisState.Analyzing
            try {
                val result = PwaAnalyzer.analyze(url)
                _pwaAnalysisState.value = if (result.errorMessage != null) {
                    PwaAnalysisState.Error(result.errorMessage)
                } else {
                    PwaAnalysisState.Success(result)
                }
            } catch (e: Exception) {
                AppLogger.e("MainViewModel", "PWA analysis error", e)
                _pwaAnalysisState.value = PwaAnalysisState.Error(
                    e.message ?: Strings.pwaAnalysisFailed
                )
            }
        }
    }

    fun applyPwaResult(result: PwaAnalysisResult) {
        viewModelScope.launch {

            result.suggestedName?.let { name ->
                if (name.isNotBlank() && _editState.value.name.isBlank()) {
                    updateEditState { copy(name = name) }
                }
            }

            result.suggestedIconUrl?.let { iconUrl ->
                try {
                    val savedPath = withContext(Dispatchers.IO) {
                        downloadAndSaveIcon(iconUrl)
                    }
                    if (savedPath != null) {
                        updateEditState { copy(savedIconPath = savedPath, iconUri = null) }
                        AppLogger.i("MainViewModel", "PWA icon downloaded: $savedPath")
                    }
                } catch (e: Exception) {
                    AppLogger.w("MainViewModel", "Failed to download PWA icon: ${e.message}")
                }
            }

            result.suggestedThemeColor?.let { color ->
                updateEditState {
                    copy(
                        webViewConfig = webViewConfig.copy(
                            statusBarColorMode = StatusBarColorMode.CUSTOM,
                            statusBarColor = color
                        )
                    )
                }
            }

            result.suggestedOrientation?.let { orientation ->
                val mode = when (orientation.lowercase()) {
                    "portrait", "portrait-primary", "portrait-secondary" -> OrientationMode.PORTRAIT
                    "landscape", "landscape-primary", "landscape-secondary" -> OrientationMode.LANDSCAPE
                    "any", "natural" -> OrientationMode.AUTO
                    else -> null
                }
                mode?.let {
                    updateEditState {
                        copy(webViewConfig = webViewConfig.copy(orientationMode = it))
                    }
                }
            }

            result.suggestedDisplay?.let { display ->
                when (display.lowercase()) {
                    "fullscreen" -> updateEditState {
                        copy(webViewConfig = webViewConfig.copy(
                            hideToolbar = true,
                            fullscreenEnabled = true
                        ))
                    }
                    "standalone" -> updateEditState {
                        copy(webViewConfig = webViewConfig.copy(hideToolbar = true))
                    }
                    "minimal-ui" -> {  }
                    "browser" -> {  }
                }
            }

            if (result.source == PwaDataSource.MANIFEST) {
                updateEditState {
                    copy(webViewConfig = webViewConfig.copy(pwaOfflineEnabled = true))
                }
            }

            val hosts = PwaAnalyzer.suggestDeepLinkHosts(result, _editState.value.url)
            if (hosts.isNotEmpty()) {
                updateEditState {
                    copy(apkExportConfig = apkExportConfig.copy(
                        deepLinkEnabled = true,
                        customDeepLinkHosts = hosts
                    ))
                }
            }

            result.startUrl?.let { startUrl ->
                val currentUrl = _editState.value.url.trim()
                if (startUrl != currentUrl && startUrl.isNotBlank()) {

                    val currentHost = PwaAnalyzer.extractHost(currentUrl)
                    val startHost = PwaAnalyzer.extractHost(startUrl)
                    if (currentHost == startHost) {
                        updateEditState { copy(url = startUrl) }
                    }
                }
            }
        }
    }

    fun resetPwaState() {
        _pwaAnalysisState.value = PwaAnalysisState.Idle
    }

    private fun downloadAndSaveIcon(iconUrl: String): String? {
        return try {
            val conn = URL(iconUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/131.0.0.0 Mobile Safari/537.36")
            try {
                if (conn.responseCode !in 200..299) return null
                val bitmap = com.webtoapp.util.BoundedBitmaps.decodeBoundedBitmapStream(conn.inputStream) ?: return null
                IconStorage.saveIconFromBitmap(getApplication(), bitmap)
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            AppLogger.e("MainViewModel", "Icon download failed: ${e.message}")
            null
        }
    }

    fun handleIconSelected(uri: Uri) {
        viewModelScope.launch {
            val savedPath = withContext(Dispatchers.IO) {
                IconStorage.saveIconFromUri(getApplication(), uri)
            }
            if (savedPath != null) {
                _editState.value = _editState.value.copy(
                    iconUri = Uri.parse(savedPath),
                    savedIconPath = savedPath
                )
            } else {
                _uiState.value = UiState.Error(Strings.failedSaveIcon)
            }
        }
    }

    fun handleSplashMediaSelected(uri: Uri, isVideo: Boolean) {
        viewModelScope.launch {
            val savedPath = withContext(Dispatchers.IO) {
                SplashStorage.saveMediaFromUri(getApplication(), uri, isVideo)
            }
            if (savedPath != null) {
                val newType = if (isVideo) SplashType.VIDEO else SplashType.IMAGE
                _editState.value = _editState.value.copy(
                    splashMediaUri = Uri.parse(savedPath),
                    savedSplashPath = savedPath,
                    splashConfig = _editState.value.splashConfig.copy(type = newType)
                )
            } else {
                _uiState.value = UiState.Error(Strings.failedSaveSplash)
            }
        }
    }

    fun clearSplashMedia() {
        _editState.value = _editState.value.copy(
            splashMediaUri = null,
            savedSplashPath = null
        )
    }

    private suspend fun deleteReplacedIconIfUnused(previousPath: String?, currentPath: String?) {
        if (previousPath.isNullOrBlank() || previousPath == currentPath) return
        withContext(Dispatchers.IO) { IconStorage.deleteIcon(previousPath) }
    }

    private suspend fun deleteReplacedSplashIfUnused(previousPath: String?, currentPath: String?) {
        if (previousPath.isNullOrBlank() || previousPath == currentPath) return
        withContext(Dispatchers.IO) { SplashStorage.deleteMedia(previousPath) }
    }

    private suspend fun deleteUnusedGalleryFiles(previous: GalleryConfig?, current: GalleryConfig?) {
        if (previous?.items.isNullOrEmpty() || previous?.items === current?.items) return
        val currentPaths = current?.items.orEmpty().mapTo(hashSetOf()) { it.path }
        val removed = previous?.items.orEmpty().filter { it.path !in currentPaths }
        if (removed.isEmpty()) return
        withContext(Dispatchers.IO) {
            val referenced = repository.allWebApps.first().asSequence()
                .flatMap { it.galleryConfig?.items.orEmpty().asSequence() }
                .flatMap { sequenceOf(it.path, it.thumbnailPath) }
                .filterNotNull()
                .toHashSet()
            removed.forEach { item ->
                if (item.path !in referenced) {
                    MediaStorage.deleteGalleryMedia(item.copy(thumbnailPath = item.thumbnailPath?.takeUnless { it in referenced }))
                }
            }
        }
    }

    fun saveApp() {
        viewModelScope.launch {
            var state = _editState.value

            if (!validateInput(state)) return@launch

            state = _editState.value

            _uiState.value = UiState.Loading

            try {
                val webApp = buildDraftWebApp(
                    state = state,
                    iconPath = state.savedIconPath ?: state.iconUri?.toString(),
                    fallbackName = null,
                    categoryId = _selectedCategoryId.value?.takeIf { it > 0 },
                    appIdForExternalization = _currentApp.value?.id ?: 0L,
                )
                AppLogger.d("MainViewModel", "saveApp: activationEnabled=${state.activationEnabled}, " +
                    "activationCodeList.size=${state.activationCodeList.size}")

                val previousIconPath = _currentApp.value?.iconPath
                val previousSplashPath = _currentApp.value?.splashConfig?.mediaPath

                if (_currentApp.value != null) {
                    repository.updateWebApp(webApp)
                    deleteReplacedIconIfUnused(previousIconPath, webApp.iconPath)
                    deleteReplacedSplashIfUnused(previousSplashPath, webApp.splashConfig?.mediaPath)
                } else {
                    val newId = repository.createWebApp(webApp)

                    if (newId > 0 && state.webViewConfig.injectScripts.any { it.code.length > com.webtoapp.core.script.UserScriptStorage.EXTERNAL_STORAGE_THRESHOLD }) {
                        val reExternalized = state.webViewConfig.copy(
                            injectScripts = com.webtoapp.core.script.UserScriptStorage.externalizeScripts(
                                getApplication(), newId, state.webViewConfig.injectScripts
                            )
                        )
                        repository.updateWebApp(webApp.copy(id = newId, webViewConfig = reExternalized))
                    }
                }

                _uiState.value = UiState.Success(Strings.appSavedSuccessfully)
                resetEditState()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: Strings.saveFailed)
            }
        }
    }

    fun deleteApp(webApp: WebApp) {
        viewModelScope.launch {
            try {
                repository.deleteWebApp(webApp)

                withContext(Dispatchers.IO) {
                    com.webtoapp.core.script.UserScriptStorage.deleteScriptsForApp(
                        getApplication(), webApp.id
                    )
                    com.webtoapp.core.app.ProjectDirCleaner.deleteForApp(getApplication(), webApp)
                }
                _uiState.value = UiState.Success(Strings.appDeleted)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: Strings.deleteFailed)
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    private fun resetEditState() {
        _editState.value = EditState()
        _currentApp.value = null
        _hasUnsavedChanges.value = false
    }

    suspend fun saveAndPreview(): Long? {
        val state = _editState.value

        if (!validateInput(state)) return null

        return try {
            val webApp = buildDraftWebApp(
                state = state,
                iconPath = state.savedIconPath ?: state.iconUri?.toString(),
                fallbackName = "Preview",
                categoryId = _selectedCategoryId.value?.takeIf { it > 0 },
                appIdForExternalization = _currentApp.value?.id ?: 0L,
            )

            val savedAppId: Long
            if (_currentApp.value != null) {

                repository.updateWebApp(webApp)
                savedAppId = webApp.id
                _currentApp.value = webApp
            } else {

                savedAppId = repository.createWebApp(webApp)
                val savedApp = webApp.copy(id = savedAppId)
                _currentApp.value = savedApp
            }

            savedAppId
        } catch (e: Exception) {
            AppLogger.e("MainViewModel", "saveAndPreview failed", e)
            null
        }
    }

    private fun validateInput(state: EditState): Boolean {
        return when {
            state.name.isBlank() -> {
                _uiState.value = UiState.Error(Strings.pleaseEnterAppName)
                false
            }

            state.appType == AppType.WEB && state.url.isBlank() &&
                !(state.activationEnabled &&
                    state.activationRemoteConfig?.enabled == true &&
                    state.activationRemoteConfig?.deliverUrl == true) -> {
                _uiState.value = UiState.Error(Strings.pleaseEnterWebsiteUrl)
                false
            }
            state.appType == AppType.WEB && state.url.isNotBlank() && !isValidUrl(state.url) -> {
                _uiState.value = UiState.Error(Strings.pleaseEnterValidUrl)
                false
            }

            state.activationEnabled &&
                state.activationRemoteConfig?.encryptUrl == true &&
                state.activationRemoteConfig?.aesKeyBase64.isNullOrBlank() -> {
                _uiState.value = UiState.Error(Strings.remoteActivationEncryptUrlNeedsKey)
                false
            }

            state.appType == AppType.HTML && (state.htmlConfig?.files?.isEmpty() != false) -> {
                _uiState.value = UiState.Error(Strings.pleaseSelectHtmlFile)
                false
            }

            (state.appType == AppType.IMAGE || state.appType == AppType.VIDEO) && state.url.isBlank() -> {
                _uiState.value = UiState.Error(Strings.mediaFilePathEmpty)
                false
            }
            else -> true
        }
    }

    private fun isValidUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return false
        return try {

            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                val uri = Uri.parse(trimmed)
                !uri.host.isNullOrBlank()
            } else {

                val host = trimmed.split("/").first()
                host.isNotBlank() && !host.contains(" ") &&
                    (host.contains(".") || host == "localhost" || isIpAddress(host))
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun isIpAddress(host: String): Boolean {
        val parts = host.split(".")
        if (parts.size != 4) return false
        return parts.all { part ->
            part.toIntOrNull()?.let { it in 0..255 } ?: false
        }
    }

    private fun normalizeUrl(url: String, appType: AppType): String {
        val trimmed = url.trim()
        if (appType != AppType.WEB) return trimmed

        return ensureWebUrlScheme(trimmed)
    }

    private suspend fun buildDraftWebApp(
        state: EditState,
        iconPath: String?,
        fallbackName: String?,
        categoryId: Long?,
        appIdForExternalization: Long,
    ): WebApp {
        val currentThemeType = getCurrentThemeType()
        val externalizedConfig = if (state.webViewConfig.injectScripts.any { it.code.length > com.webtoapp.core.script.UserScriptStorage.EXTERNAL_STORAGE_THRESHOLD }) {
            withContext(Dispatchers.IO) {
                state.webViewConfig.copy(
                    injectScripts = com.webtoapp.core.script.UserScriptStorage.externalizeScripts(
                        getApplication(), appIdForExternalization, state.webViewConfig.injectScripts
                    )
                )
            }
        } else {
            state.webViewConfig
        }

        val payload = state.toDraftPayload(
            normalizedUrl = normalizeUrl(state.url, state.appType),
            iconPath = iconPath,
            pluginIds = state.pluginIds,
            currentThemeType = currentThemeType,
            externalizedWebViewConfig = externalizedConfig,
        )
        return _currentApp.value.applyDraft(
            editState = state,
            payload = payload,
            overrides = DraftBuildOverrides(
                appNameFallback = fallbackName,
                categoryId = categoryId,
            ),
        )
    }

    private suspend fun getCurrentThemeType(): String {
        val themeManager = ThemeManager.getInstance(getApplication())
        return themeManager.themeTypeFlow.first().name
    }

    private suspend fun saveIconIfPresent(iconUri: Uri?): String? {
        return iconUri?.let { uri ->
            withContext(Dispatchers.IO) {
                IconStorage.saveIconFromUri(getApplication(), uri)
            }
        }
    }

    private suspend fun createApp(
        typeName: String,
        iconUri: Uri?,
        buildApp: suspend EditorSaveFiles.(savedIconPath: String?, currentThemeType: String, categoryId: Long?) -> WebApp
    ) = withContext(Dispatchers.IO) {
        val files = EditorSaveFiles(getApplication())
        var committed = false
        try {
            val iconPath = files.saveIcon(iconUri)
            val app = files.buildApp(iconPath, getCurrentThemeType(), _selectedCategoryId.value?.takeIf { it > 0 })
            withContext(NonCancellable) {
                check(repository.createWebApp(app) > 0)
                committed = true
                _uiState.value = UiState.Success(Strings.appCreatedSuccessfully.replaceFirst("%s", typeName))
            }
            files.cleanupDrafts()
        } finally {
            if (!committed) files.discard()
        }
    }

    private suspend fun updateApp(
        appId: Long,
        typeName: String,
        iconUri: Uri?,
        applyUpdate: suspend EditorSaveFiles.(existingApp: WebApp, savedIconPath: String?) -> WebApp
    ) = withContext(Dispatchers.IO) {
        val existingApp = repository.getWebApp(appId) ?: throw EditorSaveException(Strings.appNotFound)
        val files = EditorSaveFiles(getApplication())
        var committed = false
        try {
            val savedIconPath = files.saveIcon(iconUri, existingApp.iconPath)
            val updatedApp = files.applyUpdate(existingApp, savedIconPath)
            withContext(NonCancellable) {
                if (!repository.updateWebAppIfPresent(updatedApp)) {
                    throw EditorSaveException(Strings.appNotFound)
                }
                committed = true
                _uiState.value = UiState.Success(Strings.appUpdatedSuccessfully.replaceFirst("%s", typeName))
            }
            try {
                files.cleanupDrafts()
                deleteReplacedIconIfUnused(existingApp.iconPath, updatedApp.iconPath)
                deleteUnusedGalleryFiles(existingApp.galleryConfig, updatedApp.galleryConfig)
                deleteReplacedHtmlProject(existingApp, updatedApp)
            } catch (error: Exception) {
                AppLogger.w("MainViewModel", "Could not finish cleaning up the previous project files", error)
            }
        } finally {
            if (!committed) files.discard()
        }
    }

    private suspend fun deleteReplacedHtmlProject(previous: WebApp, updated: WebApp) {
        val projectId = (previous.htmlConfig?.projectId ?: previous.multiWebConfig?.projectId)
            ?.takeIf { it.isNotBlank() } ?: return
        if (projectId == (updated.htmlConfig?.projectId ?: updated.multiWebConfig?.projectId)) return
        val inUse = repository.allWebApps.first().any { app ->
            app.htmlConfig?.projectId == projectId ||
                app.wordpressConfig?.sourceProjectId == projectId ||
                app.multiWebConfig?.projectId == projectId ||
                app.multiWebConfig?.sites.orEmpty().any { site ->
                    site.sourceProjectId == projectId || site.siteProjectId == projectId
                }
        }
        if (!inUse) HtmlStorage.deleteProject(getApplication(), projectId)
    }

    suspend fun saveMediaApp(
        name: String,
        appType: AppType,
        mediaUri: Uri?,
        mediaConfig: MediaConfig?,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = createApp("Media", iconUri) { savedIconPath, currentThemeType, categoryId ->
        val isVideo = appType == AppType.VIDEO
        val savedMediaPath = saveMedia(mediaUri ?: throw EditorSaveException(Strings.failedSaveMediaFile), isVideo)
        WebApp(
            name = name.ifBlank { if (isVideo) "Video App" else "Image App" },
            url = savedMediaPath,
            iconPath = savedIconPath,
            appType = appType,
            mediaConfig = mediaConfig?.copy(mediaPath = savedMediaPath),
            activationEnabled = false,
            activationCodeList = emptyList(),
            bgmEnabled = false,
            bgmConfig = BgmConfig(),
            themeType = currentThemeType,
            categoryId = categoryId
        )
    }

    suspend fun saveGalleryApp(
        name: String,
        galleryConfig: GalleryConfig?,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) {
        if (galleryConfig == null || !galleryConfig.hasContent) {
            throw EditorSaveException(Strings.pleaseAddMediaFile)
        }
        createApp("Gallery", iconUri) { savedIconPath, currentThemeType, categoryId ->
            WebApp(
                name = name.ifBlank { "Media Gallery" },
                url = "",
                iconPath = savedIconPath,
                appType = AppType.GALLERY,
                galleryConfig = galleryConfig,
                activationEnabled = false,
                activationCodeList = emptyList(),
                bgmEnabled = false,
                bgmConfig = BgmConfig(),
                themeType = currentThemeType,
                categoryId = categoryId
            )
        }
    }

    private suspend fun processAndSaveHtmlFiles(
        context: Context,
        files: List<HtmlFile>,
        projectId: String
    ): List<HtmlFile> {
        val saved = HtmlProjectHelper.processAndSaveFiles(context, files, projectId)
        val savedNames = saved.mapTo(hashSetOf()) { it.name }
        if (files.any { it.name !in savedNames }) throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
        return saved
    }

    private suspend fun copyBuildOutputToStorage(
        context: Context,
        outputPath: String,
        projectId: String
    ): List<HtmlFile> = HtmlProjectHelper.copyBuildOutputToStorage(
        context, outputPath, projectId, requireAllFiles = true
    )

    private fun hasReadableHtmlEntry(files: List<HtmlFile>): Boolean {
        return files.any { file ->
            (file.type == HtmlFileType.HTML || file.name.endsWith(".html", ignoreCase = true)) &&
                java.io.File(file.path).let { it.exists() && it.isFile && it.canRead() && it.length() > 0L }
        }
    }

    suspend fun saveHtmlApp(
        name: String,
        htmlConfig: HtmlConfig?,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = createApp("HTML", iconUri) { savedIconPath, currentThemeType, categoryId ->
        val context = getApplication<Application>()
        val projectId = newHtmlProject()
        val sourceFiles = htmlConfig?.files.orEmpty()
        val savedHtmlFiles = processAndSaveHtmlFiles(context, sourceFiles, projectId)
        cleanupHtmlDraftOnSuccess(sourceFiles)

        if (savedHtmlFiles.none { it.type == HtmlFileType.HTML || it.name.endsWith(".html", ignoreCase = true) }) {
            AppLogger.e("MainViewModel", "No HTML files were saved successfully. savedHtmlFiles=$savedHtmlFiles")
            throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
        }

        val savedHtmlConfig = htmlConfig?.copy(
            projectId = projectId,
            files = savedHtmlFiles
        )
        AppLogger.d("MainViewModel", "HTML app saved successfully: projectId=$projectId, files=${savedHtmlFiles.size}")

        WebApp(
            name = name.ifBlank { "HTML App" },
            url = "",
            iconPath = savedIconPath,
            appType = AppType.HTML,
            htmlConfig = savedHtmlConfig,
            activationEnabled = false,
            activationCodeList = emptyList(),
            bgmEnabled = false,
            bgmConfig = BgmConfig(),
            themeType = currentThemeType,
            categoryId = categoryId
        )
    }

    suspend fun saveZipHtmlApp(
        name: String,
        extractedDir: String,
        entryFile: String,
        iconUri: Uri?,
        enableJavaScript: Boolean = true,
        enableLocalStorage: Boolean = true,
        loadMode: HtmlLoadMode = HtmlLoadMode.AUTO,
        port: Int = 0,
        portConflictMode: com.webtoapp.data.model.PortConflictMode = com.webtoapp.data.model.PortConflictMode.AUTO_KILL
    ) = createApp("HTML", iconUri) { savedIconPath, currentThemeType, categoryId ->
        val context = getApplication<Application>()
        val projectId = newHtmlProject()
        val savedFiles = copyBuildOutputToStorage(context, extractedDir, projectId)
        cleanupZipDraftOnSuccess(extractedDir)

        if (savedFiles.none { it.type == HtmlFileType.HTML || it.name.endsWith(".html", ignoreCase = true) }) {
            AppLogger.e("MainViewModel", "No HTML files found in ZIP project")
            throw EditorSaveException(Strings.saveFailedNoHtmlInZip)
        }

        AppLogger.d("MainViewModel", "ZIP HTML app saved: projectId=$projectId, files=${savedFiles.size}, entry=$entryFile")

        WebApp(
            name = name.ifBlank { "HTML App" },
            url = "",
            iconPath = savedIconPath,
            appType = AppType.HTML,
            htmlConfig = HtmlConfig(
                projectId = projectId,
                entryFile = entryFile,
                files = savedFiles,
                enableJavaScript = enableJavaScript,
                enableLocalStorage = enableLocalStorage,
                loadMode = loadMode,
                port = port,
                portConflictMode = portConflictMode
            ),
            activationEnabled = false,
            activationCodeList = emptyList(),
            bgmEnabled = false,
            bgmConfig = BgmConfig(),
            themeType = currentThemeType,
            categoryId = categoryId
        )
    }

    suspend fun saveFrontendApp(
        name: String,
        outputPath: String,
        iconUri: Uri?,
        framework: String
    ) = createApp(framework, iconUri) { savedIconPath, currentThemeType, categoryId ->
        val context = getApplication<Application>()
        val projectId = newHtmlProject()
        val savedFiles = copyBuildOutputToStorage(context, outputPath, projectId)

        if (!hasReadableHtmlEntry(savedFiles)) {
            AppLogger.e("MainViewModel", "No readable HTML entry found in frontend build output: $outputPath")
            throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
        }

        WebApp(
            name = name.ifBlank { "$framework App" },
            url = "",
            iconPath = savedIconPath,
            appType = AppType.FRONTEND,
            htmlConfig = HtmlConfig(
                projectId = projectId,
                files = savedFiles,
                enableJavaScript = true,
                enableLocalStorage = true
            ),
            activationEnabled = false,
            activationCodeList = emptyList(),
            bgmEnabled = false,
            bgmConfig = BgmConfig(),
            themeType = currentThemeType,
            categoryId = categoryId
        )
    }

    suspend fun saveWordPressApp(
        name: String,
        wordpressConfig: WordPressConfig,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = createApp("WordPress", iconUri) { savedIconPath, currentThemeType, categoryId ->
        WebApp(
            name = name.ifBlank { "WordPress App" },
            url = "",
            iconPath = savedIconPath,
            appType = AppType.WORDPRESS,
            wordpressConfig = wordpressConfig,
            activationEnabled = false,
            activationCodeList = emptyList(),
            bgmEnabled = false,
            bgmConfig = BgmConfig(),
            themeType = currentThemeType,
            categoryId = categoryId
        )
    }

    suspend fun saveNodeJsApp(
        name: String,
        nodejsConfig: NodeJsConfig,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = createApp("Node.js", iconUri) { savedIconPath, currentThemeType, categoryId ->
        WebApp(
            name = name.ifBlank { "Node.js App" },
            url = "",
            iconPath = savedIconPath,
            appType = AppType.NODEJS_APP,
            nodejsConfig = nodejsConfig,
            activationEnabled = false,
            activationCodeList = emptyList(),
            bgmEnabled = false,
            bgmConfig = BgmConfig(),
            themeType = currentThemeType,
            categoryId = categoryId
        )
    }

    suspend fun updateNodeJsApp(
        appId: Long,
        name: String,
        nodejsConfig: NodeJsConfig,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = updateApp(appId, "Node.js", iconUri) { existingApp, savedIconPath ->
        existingApp.copy(
            name = name.ifBlank { existingApp.name },
            iconPath = savedIconPath,
            nodejsConfig = nodejsConfig,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun savePhpApp(
        name: String,
        phpAppConfig: PhpAppConfig,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = createApp("PHP", iconUri) { savedIconPath, currentThemeType, categoryId ->
        WebApp(
            name = name.ifBlank { "PHP App" },
            url = "",
            iconPath = savedIconPath,
            appType = AppType.PHP_APP,
            phpAppConfig = phpAppConfig,
            themeType = currentThemeType,
            categoryId = categoryId
        )
    }

    suspend fun savePythonApp(
        name: String,
        pythonAppConfig: PythonAppConfig,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = createApp("Python", iconUri) { savedIconPath, currentThemeType, categoryId ->
        WebApp(
            name = name.ifBlank { "Python App" },
            url = "",
            iconPath = savedIconPath,
            appType = AppType.PYTHON_APP,
            pythonAppConfig = pythonAppConfig,
            themeType = currentThemeType,
            categoryId = categoryId
        )
    }

    suspend fun saveGoApp(
        name: String,
        goAppConfig: GoAppConfig,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = createApp("Go", iconUri) { savedIconPath, currentThemeType, categoryId ->
        WebApp(
            name = name.ifBlank { "Go Service" },
            url = "",
            iconPath = savedIconPath,
            appType = AppType.GO_APP,
            goAppConfig = goAppConfig,
            themeType = currentThemeType,
            categoryId = categoryId
        )
    }

    suspend fun updatePhpApp(
        appId: Long,
        name: String,
        phpAppConfig: PhpAppConfig,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = updateApp(appId, "PHP", iconUri) { existingApp, savedIconPath ->
        existingApp.copy(
            name = name.ifBlank { existingApp.name },
            iconPath = savedIconPath,
            phpAppConfig = phpAppConfig,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updatePythonApp(
        appId: Long,
        name: String,
        pythonAppConfig: PythonAppConfig,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = updateApp(appId, "Python", iconUri) { existingApp, savedIconPath ->
        existingApp.copy(
            name = name.ifBlank { existingApp.name },
            iconPath = savedIconPath,
            pythonAppConfig = pythonAppConfig,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updateGoApp(
        appId: Long,
        name: String,
        goAppConfig: GoAppConfig,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = updateApp(appId, "Go", iconUri) { existingApp, savedIconPath ->
        existingApp.copy(
            name = name.ifBlank { existingApp.name },
            iconPath = savedIconPath,
            goAppConfig = goAppConfig,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun saveMultiWebApp(
        name: String,
        multiWebConfig: MultiWebConfig,
        iconUri: Uri?,
        injectScripts: List<com.webtoapp.data.model.UserScript> = emptyList(),
        themeType: String = "AURORA",
        pluginsEnabled: Boolean = false,
        pluginIds: List<String> = emptyList(),
        preserveIconShape: Boolean = false,
    ) = createApp("Multi-Site", iconUri) { savedIconPath, currentThemeType, categoryId ->
        val storedConfig = materializeMultiWebConfig(multiWebConfig)
        WebApp(
            name = name.ifBlank { "Multi-Site App" },
            url = storedConfig.sites.firstOrNull()?.getEffectiveUrl() ?: "",
            iconPath = savedIconPath,
            appType = AppType.MULTI_WEB,
            multiWebConfig = storedConfig,
            webViewConfig = com.webtoapp.data.model.WebViewConfig(injectScripts = injectScripts),
            apkExportConfig = if (preserveIconShape) {
                com.webtoapp.data.model.ApkExportConfig(preserveIconShape = true)
            } else null,
            pluginsEnabled = pluginsEnabled,
            pluginIds = pluginIds,
            themeType = currentThemeType,
            categoryId = categoryId
        )
    }

    suspend fun updateMultiWebApp(
        appId: Long,
        name: String,
        multiWebConfig: MultiWebConfig,
        iconUri: Uri?,
        injectScripts: List<com.webtoapp.data.model.UserScript> = emptyList(),
        themeType: String = "AURORA",
        pluginsEnabled: Boolean = false,
        pluginIds: List<String> = emptyList(),
        preserveIconShape: Boolean = false,
    ) = updateApp(appId, "Multi-Site", iconUri) { existingApp, savedIconPath ->
        val storedConfig = materializeMultiWebConfig(multiWebConfig, existingApp.multiWebConfig?.projectId)
        existingApp.copy(
            name = name.ifBlank { existingApp.name },
            url = storedConfig.sites.firstOrNull()?.getEffectiveUrl() ?: existingApp.url,
            iconPath = savedIconPath,
            multiWebConfig = storedConfig,
            webViewConfig = existingApp.webViewConfig.copy(injectScripts = injectScripts),
            apkExportConfig = existingApp.apkExportConfig?.copy(preserveIconShape = preserveIconShape)
                ?: if (preserveIconShape) com.webtoapp.data.model.ApkExportConfig(preserveIconShape = true) else null,
            pluginsEnabled = pluginsEnabled,
            pluginIds = pluginIds,
            updatedAt = System.currentTimeMillis()
        )
    }

    private suspend fun EditorSaveFiles.materializeMultiWebConfig(
        config: MultiWebConfig,
        previousProjectId: String? = null
    ): MultiWebConfig {
        val context = getApplication<Application>()
        val previousId = config.projectId.ifBlank { previousProjectId.orEmpty() }
        val needsFiles = config.sites.any { site ->
            (site.type == "LOCAL" && (site.localFileUri.isNotBlank() || site.localFilePath.isNotBlank())) ||
                (site.type == "INLINE_HTML" && site.inlineHtml.isNotBlank()) ||
                (site.type == "EXISTING" && site.sourceProjectId.isNotBlank() && site.localFilePath.isNotBlank())
        }
        if (!needsFiles) return config.copy(projectId = "")
        val projectId = newHtmlProject()
        val previousRoot = File(context.filesDir, "html_projects/$previousId")
        if (previousId.isNotBlank()) {
            val retainedFiles = config.sites.filter { site ->
                site.type == "LOCAL" && site.localFileUri.isBlank() && site.localFilePath.isNotBlank()
            }
            retainedFiles.forEach { site ->
                val path = site.localFilePath.trimStart('/')
                val file = com.webtoapp.util.SafeZip.safeChild(previousRoot, path)
                    ?: throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
                if (HtmlStorage.saveFromTempFile(context, file.absolutePath, path, projectId) == null) {
                    throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
                }
            }
        }
        val projectRoot = File(context.filesDir, "html_projects/$projectId")
        val sites = config.sites.map { site ->
            when {
                site.type == "LOCAL" && site.localFileUri.isNotBlank() -> {
                    val name = buildMultiWebSiteRelativePath(site)
                    if (com.webtoapp.util.SafeZip.safeChild(projectRoot, name) == null) {
                        throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
                    }
                    val saved = HtmlStorage.saveHtmlFile(context, Uri.parse(site.localFileUri), name, projectId)
                    if (saved == null || !File(saved).isFile) throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
                    site.copy(localFilePath = name, localFileUri = "")
                }
                site.type == "INLINE_HTML" && site.inlineHtml.isNotBlank() -> {
                    val name = buildMultiWebSiteRelativePath(site)
                    if (com.webtoapp.util.SafeZip.safeChild(projectRoot, name) == null || HtmlStorage.saveProcessedHtml(context, site.inlineHtml, name, projectId) == null) {
                        throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
                    }
                    site.copy(localFilePath = name)
                }
                site.type == "EXISTING" && site.localFilePath.isNotBlank() ->
                    site.copy(localFilePath = buildMultiWebSiteRelativePath(site))
                else -> site
            }
        }
        copyExistingMultiWebSiteFiles(context, sites, projectId, previousRoot.takeIf { previousId.isNotBlank() })
        return config.copy(sites = sites, projectId = projectId)
    }

    private fun buildMultiWebSiteRelativePath(site: MultiWebSite): String {
        val original = site.localFilePath.trim().trimStart('/').ifBlank { "index.html" }
        val sitePrefix = "${site.id.ifBlank { "site" }}/"
        return if (original.startsWith(sitePrefix)) original else "$sitePrefix$original"
    }

    private fun copyExistingMultiWebSiteFiles(
        context: Application,
        sites: List<MultiWebSite>,
        projectId: String,
        previousRoot: File?
    ) {
        val htmlRoot = File(context.filesDir, "html_projects")
        val targetRoot = File(htmlRoot, projectId)
        sites.filter { it.type == "EXISTING" && it.sourceProjectId.isNotBlank() && it.localFilePath.isNotBlank() }
            .forEach { site ->
                val relativePath = site.localFilePath.trimStart('/')
                val sourceRoot = com.webtoapp.util.SafeZip.safeChild(htmlRoot, site.sourceProjectId)
                val sourceRelative = relativePath.removePrefix("${site.id.ifBlank { "site" }}/")
                val original = sourceRoot?.let { com.webtoapp.util.SafeZip.safeChild(it, sourceRelative) }
                val savedCopy = previousRoot?.let { com.webtoapp.util.SafeZip.safeChild(it, relativePath) }
                val source = original?.takeIf(File::isFile) ?: savedCopy?.takeIf(File::isFile)
                    ?: throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
                if (com.webtoapp.util.SafeZip.safeChild(targetRoot, relativePath) == null ||
                    HtmlStorage.saveFromTempFile(context, source.absolutePath, relativePath, projectId) == null) {
                    throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
                }
            }
    }

    suspend fun updateFrontendApp(
        appId: Long,
        name: String,
        outputPath: String?,
        iconUri: Uri?,
        framework: String
    ) = updateApp(appId, "Frontend", iconUri) { existingApp, savedIconPath ->
        val context = getApplication<Application>()

        val htmlConfig = if (!outputPath.isNullOrEmpty()) {
            val projectId = newHtmlProject()
            val savedFiles = copyBuildOutputToStorage(context, outputPath, projectId)
            if (!hasReadableHtmlEntry(savedFiles)) {
                AppLogger.e("MainViewModel", "No readable HTML entry found in frontend build output: $outputPath")
                throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
            }
            HtmlConfig(
                projectId = projectId,
                files = savedFiles,
                enableJavaScript = true,
                enableLocalStorage = true
            )
        } else {
            existingApp.htmlConfig
        }

        existingApp.copy(
            name = name.ifBlank { existingApp.name },
            iconPath = savedIconPath,
            htmlConfig = htmlConfig,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updateMediaApp(
        appId: Long,
        name: String,
        appType: AppType,
        mediaUri: Uri?,
        mediaConfig: MediaConfig?,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = updateApp(appId, "Media", iconUri) { existingApp, savedIconPath ->
        val isVideo = appType == AppType.VIDEO
        val savedMediaPath = mediaUri?.let { saveMedia(it, isVideo) } ?: existingApp.url

        existingApp.copy(
            name = name.ifBlank { existingApp.name },
            url = savedMediaPath,
            iconPath = savedIconPath,
            appType = appType,
            mediaConfig = mediaConfig?.copy(mediaPath = savedMediaPath) ?: existingApp.mediaConfig,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updateGalleryApp(
        appId: Long,
        name: String,
        galleryConfig: GalleryConfig?,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) {
        if (galleryConfig == null || !galleryConfig.hasContent) {
            throw EditorSaveException(Strings.pleaseAddMediaFile)
        }
        updateApp(appId, "Gallery", iconUri) { existingApp, savedIconPath ->
            existingApp.copy(
                name = name.ifBlank { existingApp.name },
                iconPath = savedIconPath,
                galleryConfig = galleryConfig,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    suspend fun updateHtmlApp(
        appId: Long,
        name: String,
        htmlConfig: HtmlConfig?,
        iconUri: Uri?,
        themeType: String = "AURORA"
    ) = updateApp(appId, "HTML", iconUri) { existingApp, savedIconPath ->
        val context = getApplication<Application>()

        val finalHtmlConfig = if (htmlConfig != null && (htmlConfig != existingApp.htmlConfig || htmlContentChanged(existingApp.htmlConfig, htmlConfig))) {
            AppLogger.d("MainViewModel", "HTML files changed, re-processing...")

            val projectId = newHtmlProject()
            val savedHtmlFiles = processAndSaveHtmlFiles(context, htmlConfig.files, projectId)
            cleanupHtmlDraftOnSuccess(htmlConfig.files)

            if (savedHtmlFiles.none { it.type == HtmlFileType.HTML || it.name.endsWith(".html", ignoreCase = true) }) {
                AppLogger.e("MainViewModel", "No HTML files were saved successfully in update")
                throw EditorSaveException(Strings.saveFailedCannotProcessHtml)
            }

            htmlConfig.copy(projectId = projectId, files = savedHtmlFiles)
        } else {
            existingApp.htmlConfig
        }

        existingApp.copy(
            name = name.ifBlank { existingApp.name },
            iconPath = savedIconPath,
            htmlConfig = finalHtmlConfig,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun htmlContentChanged(old: HtmlConfig?, new: HtmlConfig?): Boolean {
        if (old == null || new == null) return true
        val oldByPath = old.files.associateBy { it.path }
        return new.files.any { file ->
            val previous = oldByPath[file.path] ?: return@any false
            if (previous.path != file.path) return@any false
            try {
                val f = java.io.File(file.path)
                if (!f.isFile) return@any false
                java.io.File(previous.path).let { it.length() != f.length() || !it.readBytes().contentEquals(f.readBytes()) }
            } catch (e: Exception) {
                AppLogger.w("MainViewModel", "htmlContentChanged probe failed for ${file.path}: ${e.message}")
                false
            }
        }
    }

    suspend fun updateZipHtmlApp(
        appId: Long,
        name: String,
        extractedDir: String,
        entryFile: String,
        iconUri: Uri?,
        enableJavaScript: Boolean = true,
        enableLocalStorage: Boolean = true,
        loadMode: HtmlLoadMode = HtmlLoadMode.AUTO,
        port: Int = 0,
        portConflictMode: com.webtoapp.data.model.PortConflictMode = com.webtoapp.data.model.PortConflictMode.AUTO_KILL
    ) = updateApp(appId, "HTML", iconUri) { existingApp, savedIconPath ->
        val context = getApplication<Application>()

        val projectId = newHtmlProject()
        val savedFiles = copyBuildOutputToStorage(context, extractedDir, projectId)
        cleanupZipDraftOnSuccess(extractedDir)

        if (savedFiles.none { it.type == HtmlFileType.HTML || it.name.endsWith(".html", ignoreCase = true) }) {
            AppLogger.e("MainViewModel", "No HTML files found in updated ZIP project")
            throw EditorSaveException(Strings.saveFailedNoHtmlInZip)
        }

        AppLogger.d("MainViewModel", "ZIP HTML app updated: projectId=$projectId, files=${savedFiles.size}, entry=$entryFile")

        val finalHtmlConfig = HtmlConfig(
            projectId = projectId,
            entryFile = entryFile,
            files = savedFiles,
            enableJavaScript = enableJavaScript,
            enableLocalStorage = enableLocalStorage,
            loadMode = loadMode,
            port = port,
            portConflictMode = portConflictMode
        )

        existingApp.copy(
            name = name.ifBlank { existingApp.name },
            iconPath = savedIconPath,
            htmlConfig = finalHtmlConfig,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun saveScrapedWebsiteApp(
        name: String,
        url: String,
        iconUri: Uri?,
        maxDepth: Int = 3,
        downloadCdnResources: Boolean = true,
        followLinks: Boolean = true,
        maxFiles: Int = 500,
        maxTotalSizeMb: Int = 200,
        skipPatterns: String = "",
        timeoutSeconds: Int = 30,
        onProgress: (com.webtoapp.core.scraper.WebsiteScraper.ScrapeProgress) -> Unit = {}
    ) {
        val context = getApplication<Application>()
        val scraper = com.webtoapp.core.scraper.WebsiteScraper(context)
        val patterns = skipPatterns.split(',', ' ', ';').map(String::trim).filter(String::isNotEmpty)
        val config = com.webtoapp.core.scraper.WebsiteScraper.ScrapeConfig(
            url = url,
            maxDepth = maxDepth,
            downloadCdnResources = downloadCdnResources,
            followLinks = followLinks,
            maxFiles = maxFiles,
            maxTotalSize = maxTotalSizeMb * 1024L * 1024L,
            skipPatterns = patterns,
            timeoutSeconds = timeoutSeconds
        )
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        when (val result = scraper.scrape(config) { progress -> mainHandler.post { onProgress(progress) } }) {
            is com.webtoapp.core.scraper.WebsiteScraper.ScrapeResult.Error -> throw EditorSaveException(result.message)
            is com.webtoapp.core.scraper.WebsiteScraper.ScrapeResult.Success -> {
                try {
                    createApp(Strings.websiteOfflinePackTitle, iconUri) { iconPath, themeType, categoryId ->
                        val projectId = newHtmlProject()
                        val savedFiles = copyBuildOutputToStorage(context, result.projectDir.absolutePath, projectId)
                        if (!hasReadableHtmlEntry(savedFiles)) throw EditorSaveException(Strings.scrapeNoHtmlFound)
                        WebApp(
                            name = name.ifBlank { java.net.URL(url).host },
                            url = url,
                            iconPath = iconPath,
                            appType = AppType.HTML,
                            htmlConfig = HtmlConfig(projectId = projectId, entryFile = result.entryFile, files = savedFiles),
                            themeType = themeType,
                            categoryId = categoryId
                        )
                    }
                    _uiState.value = UiState.Success(
                        Strings.scrapePackSuccess.format(result.totalFiles, result.totalSize / 1024)
                    )
                } finally {
                    withContext(Dispatchers.IO) { scraper.deleteScrapedSite(result.projectDir.name) }
                }
            }
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
        categoryFilterStore.saveSelection(categoryId)
    }

    fun createCategory(name: String, icon: String = "folder", color: String = "#6200EE") {
        viewModelScope.launch {
            try {
                val category = AppCategory(
                    name = name,
                    icon = icon,
                    color = color,
                    sortOrder = categories.value.size
                )
                categoryRepository.createCategory(category)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(Strings.failedCreateCategory.replaceFirst("%s", e.message ?: ""))
            }
        }
    }

    fun updateCategory(category: AppCategory) {
        viewModelScope.launch {
            try {
                categoryRepository.updateCategory(category)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(Strings.failedUpdateCategory.replaceFirst("%s", e.message ?: ""))
            }
        }
    }

    fun moveCategory(category: AppCategory, delta: Int) {
        val list = categories.value
        val index = list.indexOfFirst { it.id == category.id }
        val target = index + delta
        if (index < 0 || target !in list.indices) return
        viewModelScope.launch {
            try {
                val reordered = list.toMutableList().apply { add(target, removeAt(index)) }
                reordered.forEachIndexed { i, cat ->
                    if (cat.sortOrder != i) {
                        categoryRepository.updateCategory(cat.copy(sortOrder = i))
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(Strings.failedUpdateCategory.replaceFirst("%s", e.message ?: ""))
            }
        }
    }

    fun deleteCategory(category: AppCategory) {
        viewModelScope.launch {
            try {

                repository.clearCategoryId(category.id)

                categoryRepository.deleteCategory(category)

                if (_selectedCategoryId.value == category.id) {
                    selectCategory(null)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(Strings.failedDeleteCategory.replaceFirst("%s", e.message ?: ""))
            }
        }
    }

    fun moveAppToCategory(webApp: WebApp, categoryId: Long?) {
        viewModelScope.launch {
            try {
                repository.updateWebApp(webApp.copy(categoryId = categoryId))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(Strings.moveFailed.replaceFirst("%s", e.message ?: ""))
            }
        }
    }
}
internal class EditorSaveException(message: String) : IllegalArgumentException(message)

private class EditorSaveFiles(private val context: Context) {
    private var iconPath: String? = null
    private var mediaPath: String? = null
    private var projectId: String? = null

    fun saveIcon(uri: Uri?, existingPath: String? = null): String? {
        if (uri == null || (uri.scheme == "file" && uri.path == existingPath)) return existingPath
        return (IconStorage.saveIconFromUri(context, uri) ?: throw EditorSaveException(Strings.failedSaveIcon))
            .also { iconPath = it }
    }

    fun saveMedia(uri: Uri, video: Boolean): String =
        (MediaStorage.saveMedia(context, uri, video) ?: throw EditorSaveException(Strings.failedSaveMediaFile))
            .also { mediaPath = it }

    fun newHtmlProject(): String = HtmlStorage.generateProjectId().also { projectId = it }

    private val draftsToClean = mutableSetOf<File>()

    fun cleanupHtmlDraftOnSuccess(files: List<HtmlFile>) {
        val root = File(context.cacheDir, "html_temp").canonicalFile
        files.map { File(it.path).canonicalFile }
            .filter { it.path.startsWith(root.path + File.separator) }
            .forEach { draftsToClean.add(it) }
    }

    fun cleanupZipDraftOnSuccess(extractedDir: String) {
        val cache = context.cacheDir.canonicalFile
        val source = File(extractedDir).canonicalFile
        if (!source.path.startsWith(cache.path + File.separator)) return
        val firstDirectory = source.relativeTo(cache).path.substringBefore(File.separator)
        if (firstDirectory.startsWith("zip_import_")) draftsToClean.add(File(cache, firstDirectory))
    }

    fun cleanupDrafts() {
        draftsToClean.forEach { draft ->
            if (!draft.deleteRecursively()) AppLogger.w("EditorSave", "Could not remove a saved project draft")
        }
    }

    fun discard() {
        IconStorage.deleteIcon(iconPath)
        MediaStorage.deleteMedia(mediaPath)
        projectId?.let { HtmlStorage.deleteProject(context, it) }
    }
}

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Progress(
        val message: String,
        val current: Int,
        val total: Int
    ) : UiState() {
        val percent: Int get() = if (total > 0) (current * 100 / total).coerceIn(0, 100) else 0
    }
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
}
