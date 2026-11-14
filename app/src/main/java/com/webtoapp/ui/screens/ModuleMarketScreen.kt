package com.webtoapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.market.GfBrowseCategory
import com.webtoapp.core.market.GfSearchResult
import com.webtoapp.core.market.GfSort
import com.webtoapp.core.market.GreasyForkSearch
import com.webtoapp.core.market.InstallProgress
import com.webtoapp.core.market.MarketInstallState
import com.webtoapp.core.market.MarketModuleView
import com.webtoapp.core.market.MarketState
import com.webtoapp.core.market.ModuleMarketEntry
import com.webtoapp.core.market.ModuleMarketRepository
import com.webtoapp.core.market.ChromeWebStoreSearch
import com.webtoapp.core.market.CwsSearchResult
import com.webtoapp.core.extension.BrowserExtensionStore
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.components.GreasyForkSearchContent
import com.webtoapp.ui.components.installGreasyForkScript
import com.webtoapp.ui.design.WtaBackground
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaInfoChip
import com.webtoapp.ui.design.WtaLoadingIndicator
import com.webtoapp.ui.design.WtaMaterialShape
import com.webtoapp.ui.design.WtaMorphingShape
import com.webtoapp.ui.design.WtaPopIn
import com.webtoapp.ui.design.WtaPopOut
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaShapePlate
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaTab
import com.webtoapp.ui.design.WtaTabRow
import com.webtoapp.ui.design.wtaTabEnterTransition
import com.webtoapp.ui.design.wtaTabExitTransition
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModuleMarketScreen(
    onNavigateBack: () -> Unit,
    initialTab: Int = 0,
    onOpenHostsAdBlock: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pluginStore = remember { com.webtoapp.core.plugin.PluginStore.getInstance(context) }
    val repo = remember { ModuleMarketRepository.getInstance(context) }

    val state by repo.state.collectAsState()
    var views by remember { mutableStateOf<List<MarketModuleView>>(emptyList()) }
    val installedModules by pluginStore.plugins.collectAsState()

    LaunchedEffect(repo) {
        repo.views.collectLatest { views = it }
    }

    LaunchedEffect(repo) {
        repo.refresh(force = false)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var installingId by remember { mutableStateOf<String?>(null) }
    var installProgress by remember { mutableStateOf<InstallProgress?>(null) }

    val filtered = remember(views, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) views else views.filter { v ->
            v.entry.name.contains(q, ignoreCase = true) ||
                v.entry.description.contains(q, ignoreCase = true)
        }
    }

    var selectedTab by remember { mutableStateOf(initialTab.coerceIn(0, 2)) }
    val filteredCustom = filtered.filter { it.entry.sourceType != "CHROME_EXTENSION" }
    val filteredChromeExt = filtered.filter { it.entry.sourceType == "CHROME_EXTENSION" }

    var cwsResults by remember { mutableStateOf<List<CwsSearchResult>>(emptyList()) }
    var cwsSearching by remember { mutableStateOf(false) }
    var cwsError by remember { mutableStateOf<String?>(null) }
    var cwsHasSearched by remember { mutableStateOf(false) }
    var cwsQuery by remember { mutableStateOf("") }

    var gfResults by remember { mutableStateOf<List<GfSearchResult>>(emptyList()) }
    var gfSearching by remember { mutableStateOf(false) }
    var gfError by remember { mutableStateOf<String?>(null) }
    var gfHasSearched by remember { mutableStateOf(false) }
    var gfQuery by remember { mutableStateOf("") }
    var gfSort by remember { mutableStateOf(GfSort.DAILY) }
    var gfBrowseCategory by remember { mutableStateOf(GfBrowseCategory.HOT) }

    val currentLocale = remember {
        val tag = java.util.Locale.getDefault().language
        if (tag.startsWith("zh")) "zh-CN" else if (tag.startsWith("ar")) "ar" else "en"
    }

    LaunchedEffect(searchQuery, selectedTab) {
        if (selectedTab != 1) return@LaunchedEffect
        val trimmed = searchQuery.trim()
        cwsQuery = trimmed
        if (trimmed.isEmpty()) {
            cwsResults = emptyList()
            cwsSearching = false
            cwsError = null
            cwsHasSearched = false
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(450)
        cwsSearching = true
        cwsError = null
        val result = ChromeWebStoreSearch.search(trimmed, currentLocale)
        if (cwsQuery != trimmed) return@LaunchedEffect
        result.onSuccess { base ->
            cwsResults = base
            cwsSearching = false
            cwsHasSearched = true
            val enriched = ChromeWebStoreSearch.enrichResults(base, currentLocale)
            if (cwsQuery == trimmed) {
                cwsResults = enriched
            }
        }.onFailure {
            cwsSearching = false
            cwsHasSearched = true
            cwsError = it.message ?: Strings.cwsSearchFailed
        }
    }

    LaunchedEffect(searchQuery, selectedTab, gfSort, gfBrowseCategory) {
        if (selectedTab != 2) return@LaunchedEffect
        val trimmed = searchQuery.trim()
        gfQuery = trimmed
        if (trimmed.isEmpty()) {
            gfSearching = true
            gfError = null
            val category = gfBrowseCategory
            val sort = gfSort
            val result = GreasyForkSearch.browse(currentLocale, sort, category)
            if (gfQuery != trimmed || gfBrowseCategory != category || gfSort != sort) return@LaunchedEffect
            result.onSuccess { list ->
                gfResults = list
                gfSearching = false
                gfHasSearched = true
            }.onFailure {
                gfResults = emptyList()
                gfSearching = false
                gfHasSearched = true
                gfError = it.message ?: Strings.gfSearchFailed
            }
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(450)
        gfSearching = true
        gfError = null
        val result = GreasyForkSearch.search(trimmed, currentLocale, gfSort)
        if (gfQuery != trimmed) return@LaunchedEffect
        result.onSuccess { list ->
            gfResults = list
            gfSearching = false
            gfHasSearched = true
        }.onFailure {
            gfResults = emptyList()
            gfSearching = false
            gfHasSearched = true
            gfError = it.message ?: Strings.gfSearchFailed
        }
    }

    val cwsViews = remember(cwsResults, installedModules) {
        cwsResults.map { r ->
            val entry = ModuleMarketEntry(
                id = "cws-${r.storeId}",
                path = "",
                name = r.name,
                description = "",
                icon = "package",
                category = "OTHER",
                iconUrl = r.iconUrl,
                sourceType = "CHROME_EXTENSION",
                storeId = r.storeId,
                homepage = BrowserExtensionStore.storePageUrl(r.storeId)
            )
            val installed = installedModules.firstOrNull {
                it.kind == com.webtoapp.core.plugin.PluginKind.CHROME_EXTENSION && it.chromeExtId == r.storeId
            }
            MarketModuleView(
                entry = entry,
                state = if (installed != null) MarketInstallState.UpToDate else MarketInstallState.NotInstalled,
                installedVersion = installed?.versionName,
                submission = null,
                ratingValue = r.ratingValue,
                ratingCount = r.ratingCount,
                userCountValue = r.userCountValue,
                ratingLabel = r.rating,
                ratingCountLabel = r.ratingCountLabel,
                userCountLabel = r.userCount
            )
        }
    }

    val listState = rememberLazyListState()

    fun installEntry(entry: ModuleMarketEntry) {
        installingId = entry.id
        installProgress = null
        scope.launch {
            installModule(entry, repo, snackbarHostState, context.applicationContext) { progress ->
                installProgress = progress
            }
            installingId = null
            installProgress = null
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WtaShapePlate(
                            polygon = MaterialShapes.Puffy,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Strings.communityExtStoreTitle,
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = Strings.back)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.contributingUrl)))
                    }) {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = Strings.moduleMarketContribute)
                    }
                    IconButton(onClick = {
                        scope.launch { repo.refresh(force = true) }
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = Strings.refresh)
                    }
                }
            )
        }
    ) { padding ->
        WtaBackground(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {

                PremiumTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = {
                        Text(
                            when (selectedTab) {
                                1 -> Strings.cwsSearchHint
                                2 -> Strings.gfSearchHint
                                else -> Strings.moduleMarketSearchHint
                            }
                        )
                    },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = Strings.clear)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(WtaRadius.Button)
                )

                WtaTabRow(
                    tabs = listOf(
                        WtaTab(Strings.extensionModulesTab, filteredCustom.size),
                        WtaTab(Strings.browserExtTab, filteredChromeExt.size),
                        WtaTab(Strings.greasyForkTab, gfResults.size)
                    ),
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        wtaTabEnterTransition(direction) togetherWith wtaTabExitTransition(direction)
                    },
                    label = "marketTabContent"
                ) { tab ->
                    when (tab) {
                        2 -> GreasyForkSearchContent(
                            query = searchQuery.trim(),
                            results = gfResults,
                            isSearching = gfSearching,
                            hasSearched = gfHasSearched,
                            errorMessage = gfError,
                            sortMode = gfSort,
                            onSortModeChange = { gfSort = it },
                            browseCategory = gfBrowseCategory,
                            onBrowseCategoryChange = { gfBrowseCategory = it },
                            installingId = installingId,
                            installProgress = installProgress,
                            installedUserScriptNames = installedModules
                                .filter { it.kind == com.webtoapp.core.plugin.PluginKind.USERSCRIPT }
                                .map { it.name }
                                .toSet(),
                            onInstall = { result ->
                                val id = "gf-${result.id}"
                                installingId = id
                                installProgress = null
                                scope.launch {
                                    installGreasyForkScript(
                                        result = result,
                                        appContext = context.applicationContext,
                                        snackbar = snackbarHostState,
                                        onProgress = { progress -> installProgress = progress }
                                    )
                                    installingId = null
                                    installProgress = null
                                }
                            },
                            onOpenSource = { result ->
                                if (result.pageUrl.isNotBlank()) {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(result.pageUrl))
                                        )
                                    }
                                }
                            },
                            listState = listState
                        )
                        1 -> Column(modifier = Modifier.fillMaxSize()) {
                            Surface(
                                onClick = { onOpenHostsAdBlock() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(WtaRadius.Card),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    WtaShapePlate(
                                        polygon = MaterialShapes.SoftBurst,
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Block,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = Strings.cwsAdBlockTip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            CwsSearchContent(
                                query = searchQuery.trim(),
                                results = cwsViews,
                                isSearching = cwsSearching,
                                hasSearched = cwsHasSearched,
                                errorMessage = cwsError,
                                installingId = installingId,
                                installProgress = installProgress,
                                onInstall = ::installEntry,
                                onOpenSource = { entry ->
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(repo.githubUrl(entry)))
                                    )
                                },
                                onInstallById = { storeId ->
                                    installEntry(
                                        ModuleMarketEntry(
                                            id = "cws-$storeId",
                                            path = "",
                                            name = storeId,
                                            description = "",
                                            icon = "package",
                                            category = "OTHER",
                                            sourceType = "CHROME_EXTENSION",
                                            storeId = storeId
                                        )
                                    )
                                },
                                listState = listState
                            )
                        }
                        else -> when (val s = state) {
                            is MarketState.Idle, is MarketState.Loading -> {
                                if (views.isEmpty()) {
                                    MarketLoadingHero(message = Strings.moduleMarketLoading)
                                } else {
                                    ModuleListContent(
                                        items = filteredCustom,
                                        installingId = installingId,
                                        installProgress = installProgress,
                                        onInstall = ::installEntry,
                                        onOpenSource = { entry ->
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(repo.githubUrl(entry)))
                                            )
                                        },
                                        resolveIcon = { entry -> repo.resolveIconUrl(entry) },
                                        listState = listState
                                    )
                                }
                            }
                            is MarketState.Loaded -> {
                                if (filteredCustom.isEmpty()) {
                                    EmptyState(
                                        searchQuery = searchQuery,
                                        onOpenContributing = {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.contributingUrl)))
                                        }
                                    )
                                } else {
                                    ModuleListContent(
                                        items = filteredCustom,
                                        installingId = installingId,
                                        installProgress = installProgress,
                                        onInstall = ::installEntry,
                                        onOpenSource = { entry ->
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(repo.githubUrl(entry)))
                                            )
                                        },
                                        resolveIcon = { entry -> repo.resolveIconUrl(entry) },
                                        listState = listState
                                    )
                                }
                            }
                            is MarketState.Error -> {
                                ErrorState(message = s.message, onRetry = {
                                    scope.launch { repo.refresh(force = true) }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun installModule(
    entry: ModuleMarketEntry,
    repo: ModuleMarketRepository,
    snackbar: SnackbarHostState,
    appContext: android.content.Context,
    onProgress: (InstallProgress) -> Unit
) {
    val result = repo.install(entry, onProgress)
    result.onSuccess {
        Toast.makeText(appContext, Strings.moduleMarketInstalled.replace("%s", entry.name), Toast.LENGTH_SHORT).show()
    }.onFailure { e ->
        snackbar.showSnackbar(Strings.moduleMarketInstallFailed.replace("%s", e.message ?: "unknown"))
    }
}

@Composable
private fun ModuleListContent(
    items: List<MarketModuleView>,
    installingId: String?,
    installProgress: InstallProgress?,
    onInstall: (ModuleMarketEntry) -> Unit,
    onOpenSource: (ModuleMarketEntry) -> Unit,
    resolveIcon: (ModuleMarketEntry) -> String?,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(items, key = { _, view -> view.entry.id }) { index, view ->
            MarketModuleCard(
                view = view,
                isInstalling = installingId == view.entry.id,
                installProgress = if (installingId == view.entry.id) installProgress else null,
                iconUrl = resolveIcon(view.entry),
                onInstall = { onInstall(view.entry) },
                onOpenSource = { onOpenSource(view.entry) },
                shapeIndex = index,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CwsSearchContent(
    query: String,
    results: List<MarketModuleView>,
    isSearching: Boolean,
    hasSearched: Boolean,
    errorMessage: String?,
    installingId: String?,
    installProgress: InstallProgress?,
    onInstall: (ModuleMarketEntry) -> Unit,
    onOpenSource: (ModuleMarketEntry) -> Unit,
    onInstallById: (String) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    val context = LocalContext.current
    val pluginStore = remember(context) { com.webtoapp.core.plugin.PluginStore.getInstance(context) }
    val installedModules by pluginStore.plugins.collectAsState()
    val builtIn by pluginStore.builtInPlugins.collectAsState()
    val allInstalled = remember(installedModules, builtIn) { installedModules + builtIn }

    var browseCategory by remember {
        mutableStateOf(BrowserExtensionStore.Category.FEATURED)
    }

    val catalogViews = remember(browseCategory, allInstalled) {
        BrowserExtensionStore.byCategory(browseCategory).map { entry ->
            storeEntryToView(entry, allInstalled)
        }
    }

    val showBrowse = query.isBlank() && !isSearching && errorMessage == null

    if (isSearching) {
        MarketLoadingHero(message = Strings.cwsSearching)
        return
    }

    if (errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WtaShapePlate(
                polygon = MaterialShapes.SoftBurst,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Text(
                text = Strings.cwsSearchFailed,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            CwsInstallByIdRow(onInstallById = onInstallById)
        }
        return
    }

    if (showBrowse) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "cws-intro") {
                Text(
                    text = Strings.cwsBrowseIntro,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item(key = "cws-categories") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(BrowserExtensionStore.browseCategories(), key = { it.name }) { category ->
                        WtaChip(
                            selected = browseCategory == category,
                            onClick = { browseCategory = category },
                            label = cwsCategoryLabel(category),
                            showSelectedCheck = false
                        )
                    }
                }
            }
            item(key = "cws-section-title") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WtaMaterialShape(
                        polygon = MaterialShapes.Diamond,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (browseCategory == BrowserExtensionStore.Category.FEATURED) {
                            Strings.cwsFeaturedTitle
                        } else {
                            cwsCategoryLabel(browseCategory)
                        },
                        style = MaterialTheme.typography.titleSmallEmphasized
                    )
                }
            }
            itemsIndexed(catalogViews, key = { _, view -> view.entry.id }) { index, view ->
                CwsResultCard(
                    view = view,
                    isInstalling = installingId == view.entry.id,
                    installProgress = if (installingId == view.entry.id) installProgress else null,
                    onInstall = { onInstall(view.entry) },
                    onOpenSource = { onOpenSource(view.entry) },
                    shapeIndex = index,
                    modifier = Modifier.animateItem()
                )
            }
            item(key = "cws-install-by-id") {
                CwsInstallByIdRow(onInstallById = onInstallById)
            }
        }
        return
    }

    if (hasSearched && results.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WtaShapePlate(
                polygon = MaterialShapes.Bun,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = Strings.cwsNoResults,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            CwsInstallByIdRow(onInstallById = onInstallById)
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(results, key = { _, view -> view.entry.id }) { index, view ->
            CwsResultCard(
                view = view,
                isInstalling = installingId == view.entry.id,
                installProgress = if (installingId == view.entry.id) installProgress else null,
                onInstall = { onInstall(view.entry) },
                onOpenSource = { onOpenSource(view.entry) },
                shapeIndex = index,
                modifier = Modifier.animateItem()
            )
        }
        item(key = "cws-install-by-id-search") {
            CwsInstallByIdRow(onInstallById = onInstallById)
        }
    }
}

private fun storeEntryToView(
    entry: BrowserExtensionStore.StoreEntry,
    installedModules: List<com.webtoapp.core.plugin.Plugin>
): MarketModuleView {
    val marketEntry = ModuleMarketEntry(
        id = "cws-${entry.storeId}",
        path = "",
        name = entry.name,
        description = entry.description,
        icon = "package",
        category = "OTHER",
        iconUrl = entry.iconUrl,
        sourceType = "CHROME_EXTENSION",
        storeId = entry.storeId,
        homepage = BrowserExtensionStore.storePageUrl(entry.storeId)
    )
    val installed = installedModules.firstOrNull {
        it.kind == com.webtoapp.core.plugin.PluginKind.CHROME_EXTENSION && it.chromeExtId == entry.storeId
    }
    return MarketModuleView(
        entry = marketEntry,
        state = if (installed != null) MarketInstallState.UpToDate else MarketInstallState.NotInstalled,
        installedVersion = installed?.versionName,
        submission = null
    )
}

@Composable
private fun CwsInstallByIdRow(onInstallById: (String) -> Unit) {
    var idInput by remember { mutableStateOf("") }
    val cleanId = idInput.trim().lowercase()
    val extracted = extractStoreId(cleanId)
    val canInstall = extracted.length == 32 && extracted.all { it.isLetterOrDigit() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PremiumTextField(
            value = idInput,
            onValueChange = { idInput = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text(Strings.browserExtStoreInstallByIdHint) },
            singleLine = true,
            shape = RoundedCornerShape(WtaRadius.Button)
        )
        WtaButton(
            onClick = {
                if (canInstall) {
                    onInstallById(extracted)
                    idInput = ""
                }
            },
            text = Strings.moduleMarketInstall,
            variant = WtaButtonVariant.Primary,
            size = WtaButtonSize.Small,
            enabled = canInstall,
            leadingIcon = Icons.Rounded.CloudDownload
        )
    }
}

private fun extractStoreId(input: String): String {
    if (input.length == 32 && input.all { it.isLetterOrDigit() }) return input
    val patterns = listOf(
        Regex("/detail/[^/]*/([a-z]{32})"),
        Regex("([a-z]{32})")
    )
    for (pattern in patterns) {
        pattern.find(input)?.let { return it.groupValues[1] }
    }
    return input
}

@Composable
private fun CwsResultCard(
    view: MarketModuleView,
    isInstalling: Boolean,
    installProgress: InstallProgress?,
    onInstall: () -> Unit,
    onOpenSource: () -> Unit,
    shapeIndex: Int,
    modifier: Modifier = Modifier
) {
    val ratingLabel = view.ratingLabel.ifBlank {
        if (view.ratingValue > 0.0) String.format(java.util.Locale.US, "%.1f", view.ratingValue) else ""
    }
    val usersLabel = view.userCountLabel.ifBlank {
        ChromeWebStoreSearch.formatUserCount(view.userCountValue)
    }
    WtaCard(
        modifier = modifier.fillMaxWidth(),
        tone = WtaCardTone.Surface,
        contentPadding = PaddingValues(WtaSpacing.Large)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ModuleIcon(
                iconUrl = view.entry.iconUrl,
                fallbackLetter = view.entry.name.take(1).uppercase(),
                shapeIndex = shapeIndex,
                size = 44.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    view.entry.name,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (view.entry.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        view.entry.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (ratingLabel.isNotBlank() || usersLabel.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (ratingLabel.isNotBlank()) {
                    WtaInfoChip(
                        label = ratingLabel,
                        icon = Icons.Rounded.Star,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        emphasized = true
                    )
                }
                if (usersLabel.isNotBlank()) {
                    WtaInfoChip(
                        label = usersLabel,
                        icon = Icons.Rounded.Group,
                        emphasized = true,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        InstallActionRow(
            state = view.state,
            isInstalling = isInstalling,
            installProgress = installProgress,
            onOpenSource = onOpenSource,
            onInstall = onInstall
        )
    }
}

@Composable
private fun MarketModuleCard(
    view: MarketModuleView,
    isInstalling: Boolean,
    installProgress: InstallProgress?,
    iconUrl: String?,
    onInstall: () -> Unit,
    onOpenSource: () -> Unit,
    shapeIndex: Int,
    modifier: Modifier = Modifier,
) {
    WtaCard(
        modifier = modifier.fillMaxWidth(),
        tone = WtaCardTone.Surface,
        contentPadding = PaddingValues(WtaSpacing.Large)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ModuleIcon(
                iconUrl = iconUrl,
                fallbackLetter = view.entry.name.take(1).uppercase(),
                shapeIndex = shapeIndex
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    view.entry.name,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (view.entry.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        view.entry.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WtaInfoChip(
                label = "v${view.entry.version}",
                icon = Icons.Rounded.Tag,
                emphasized = true
            )
            val authorName = view.entry.author?.name
            if (!authorName.isNullOrBlank()) {
                WtaInfoChip(
                    label = authorName,
                    icon = Icons.Rounded.Person,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        InstallActionRow(
            state = view.state,
            isInstalling = isInstalling,
            installProgress = installProgress,
            onOpenSource = onOpenSource,
            onInstall = onInstall
        )
    }
}

@Composable
private fun InstallActionRow(
    state: MarketInstallState,
    isInstalling: Boolean,
    installProgress: InstallProgress?,
    onOpenSource: () -> Unit,
    onInstall: () -> Unit
) {
    val showProgress = isInstalling && installProgress != null
    var cachedProgress by remember { mutableStateOf<InstallProgress?>(null) }
    LaunchedEffect(installProgress) {
        if (installProgress != null) cachedProgress = installProgress
    }
    AnimatedContent(
        targetState = showProgress,
        transitionSpec = { WtaPopIn togetherWith WtaPopOut },
        label = "installAction"
    ) { progressVisible ->
        val progress = cachedProgress
        if (progressVisible && progress != null) {
            InstallProgressCard(progress = progress)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WtaIconButton(
                    onClick = onOpenSource,
                    icon = Icons.Rounded.OpenInNew,
                    contentDescription = Strings.moduleMarketViewSource,
                    modifier = Modifier.size(36.dp),
                    tonal = true
                )
                Spacer(Modifier.weight(1f))
                InstallButton(
                    state = state,
                    isInstalling = isInstalling,
                    onClick = onInstall
                )
            }
        }
    }
}

@Composable
internal fun InstallProgressCard(progress: InstallProgress, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WtaRadius.Control),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WtaLoadingIndicator(size = 16.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    progress.label,
                    style = MaterialTheme.typography.labelMediumEmphasized,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (progress.hasByteProgress && progress.speedBytesPerSec >= 0) {
                    Text(
                        "${formatBytes(progress.downloadedBytes)} / ${formatBytes(progress.totalBytes)}  ·  ${formatSpeed(progress.speedBytesPerSec)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (progress.hasByteProgress) {
                LinearProgressIndicator(
                    progress = { progress.byteFraction },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    gapSize = ProgressIndicatorDefaults.LinearIndicatorTrackGapSize
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModuleIcon(
    iconUrl: String?,
    fallbackLetter: String,
    shapeIndex: Int,
    size: Dp = 44.dp
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val tone = shapeIndex % 3
    val container = when (tone) {
        0 -> colors.primaryContainer
        1 -> colors.secondaryContainer
        else -> colors.tertiaryContainer
    }
    val onContainer = when (tone) {
        0 -> colors.onPrimaryContainer
        1 -> colors.onSecondaryContainer
        else -> colors.onTertiaryContainer
    }
    val painter = rememberAsyncImagePainter(
        model = if (!iconUrl.isNullOrBlank()) {
            ImageRequest.Builder(context).data(iconUrl).crossfade(true).build()
        } else null
    )
    if (painter.state is AsyncImagePainter.State.Success) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(WtaRadius.IconPlate))
                .background(container),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(WtaRadius.IconPlate)),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        WtaShapePlate(
            polygon = marketShapePalette[shapeIndex % marketShapePalette.size],
            containerColor = container,
            modifier = Modifier.size(size)
        ) {
            Text(
                text = fallbackLetter,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = onContainer
            )
        }
    }
}

@Composable
private fun InstallButton(
    state: MarketInstallState,
    isInstalling: Boolean,
    onClick: () -> Unit
) {
    if (isInstalling) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WtaLoadingIndicator(size = 18.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                Strings.moduleMarketInstalling,
                style = MaterialTheme.typography.labelMediumEmphasized
            )
        }
        return
    }
    AnimatedContent(
        targetState = state,
        transitionSpec = { WtaPopIn togetherWith WtaPopOut },
        label = "installState"
    ) { target ->
        when (target) {
            MarketInstallState.NotInstalled -> WtaButton(
                onClick = onClick,
                text = Strings.moduleMarketInstall,
                variant = WtaButtonVariant.Primary,
                size = WtaButtonSize.Small,
                leadingIcon = Icons.Rounded.CloudDownload
            )
            MarketInstallState.UpdateAvailable -> WtaButton(
                onClick = onClick,
                text = Strings.moduleMarketUpdate,
                variant = WtaButtonVariant.Tonal,
                size = WtaButtonSize.Small,
                leadingIcon = Icons.Rounded.SystemUpdate
            )
            MarketInstallState.UpToDate -> WtaInfoChip(
                label = Strings.moduleMarketInstalled.replace("%s", "").trim(),
                icon = Icons.Rounded.CheckCircle,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                emphasized = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MarketLoadingHero(message: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                WtaMorphingShape(
                    startPolygon = MaterialShapes.Puffy,
                    endPolygon = MaterialShapes.Flower,
                    color = colors.primaryContainer,
                    modifier = Modifier.size(132.dp)
                )
                WtaMaterialShape(
                    polygon = MaterialShapes.ClamShell,
                    color = colors.tertiaryContainer,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = 10.dp)
                        .size(34.dp),
                    rotationDegrees = 16f
                )
                WtaMaterialShape(
                    polygon = MaterialShapes.Diamond,
                    color = colors.secondaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 8.dp, y = (-12).dp)
                        .size(26.dp),
                    rotationDegrees = -14f
                )
                WtaLoadingIndicator(size = 40.dp)
            }
            Spacer(Modifier.height(18.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyState(searchQuery: String, onOpenContributing: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                WtaShapePlate(
                    polygon = MaterialShapes.Bun,
                    containerColor = colors.secondaryContainer,
                    modifier = Modifier.size(92.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = colors.onSecondaryContainer
                    )
                }
                WtaMaterialShape(
                    polygon = MaterialShapes.Flower,
                    color = colors.tertiaryContainer,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 6.dp)
                        .size(28.dp),
                    rotationDegrees = 18f
                )
                WtaMaterialShape(
                    polygon = MaterialShapes.PuffyDiamond,
                    color = colors.primaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 6.dp, y = (-8).dp)
                        .size(22.dp),
                    rotationDegrees = -12f
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                if (searchQuery.isNotBlank()) Strings.moduleMarketNoResults else Strings.moduleMarketGuideEmptyCta,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )
            if (searchQuery.isBlank()) {
                Spacer(Modifier.height(16.dp))
                WtaButton(
                    onClick = onOpenContributing,
                    text = Strings.moduleMarketGuideOpenRepo,
                    variant = WtaButtonVariant.Tonal,
                    size = WtaButtonSize.Small,
                    leadingIcon = Icons.Rounded.OpenInNew
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        WtaCard(
            modifier = Modifier.padding(horizontal = 32.dp),
            tone = WtaCardTone.Surface,
            contentPadding = PaddingValues(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WtaShapePlate(
                    polygon = MaterialShapes.SoftBurst,
                    containerColor = colors.errorContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = colors.onErrorContainer
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                WtaButton(
                    onClick = onRetry,
                    text = Strings.retry,
                    variant = WtaButtonVariant.Tonal,
                    size = WtaButtonSize.Small,
                    leadingIcon = Icons.Rounded.Refresh
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MarketShapeIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    shapeIndex: Int,
    modifier: Modifier = Modifier,
    plateSize: Dp = 44.dp,
    iconSize: Dp = 22.dp
) {
    val colors = MaterialTheme.colorScheme
    val tone = shapeIndex % 3
    val container = when (tone) {
        0 -> colors.primaryContainer
        1 -> colors.secondaryContainer
        else -> colors.tertiaryContainer
    }
    val onContainer = when (tone) {
        0 -> colors.onPrimaryContainer
        1 -> colors.onSecondaryContainer
        else -> colors.onTertiaryContainer
    }
    WtaShapePlate(
        polygon = marketShapePalette[shapeIndex % marketShapePalette.size],
        containerColor = container,
        modifier = modifier.size(plateSize)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = onContainer
        )
    }
}

private val marketShapePalette: List<RoundedPolygon>
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    get() = listOf(
        MaterialShapes.Puffy,
        MaterialShapes.Flower,
        MaterialShapes.ClamShell,
        MaterialShapes.Bun,
        MaterialShapes.SoftBurst,
        MaterialShapes.Clover8Leaf,
        MaterialShapes.Diamond,
        MaterialShapes.PuffyDiamond
    )

private fun cwsCategoryLabel(category: BrowserExtensionStore.Category): String {
    return when (category) {
        BrowserExtensionStore.Category.FEATURED -> Strings.cwsCategoryFeatured
        BrowserExtensionStore.Category.AD_BLOCKING -> Strings.cwsCategoryAdBlocking
        BrowserExtensionStore.Category.PRIVACY -> Strings.cwsCategoryPrivacy
        BrowserExtensionStore.Category.YOUTUBE -> Strings.cwsCategoryYoutube
        BrowserExtensionStore.Category.PRODUCTIVITY -> Strings.cwsCategoryProductivity
        BrowserExtensionStore.Category.DEVELOPER -> Strings.cwsCategoryDeveloper
        BrowserExtensionStore.Category.STYLING -> Strings.cwsCategoryStyling
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "${bytes}B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(java.util.Locale.US, "%.1fKB", kb)
    val mb = kb / 1024.0
    return String.format(java.util.Locale.US, "%.1fMB", mb)
}

private fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec < 1024) return "${bytesPerSec}B/s"
    val kb = bytesPerSec / 1024.0
    if (kb < 1024) return String.format(java.util.Locale.US, "%.0fKB/s", kb)
    val mb = kb / 1024.0
    return String.format(java.util.Locale.US, "%.1fMB/s", mb)
}