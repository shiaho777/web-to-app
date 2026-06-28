package com.webtoapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.extension.ExtensionManager
import com.webtoapp.core.extension.ModuleCategory
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.market.InstallProgress
import com.webtoapp.core.market.MarketInstallState
import com.webtoapp.core.market.MarketModuleView
import com.webtoapp.core.market.MarketState
import com.webtoapp.core.market.ModuleMarketEntry
import com.webtoapp.core.market.ModuleMarketRepository
import com.webtoapp.core.market.ModuleSubmission
import com.webtoapp.core.market.ModuleSubmitter
import com.webtoapp.ui.components.PremiumFilterChip
import com.webtoapp.ui.components.PremiumTextField
import com.webtoapp.ui.design.WtaBackground
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonSize
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaTab
import com.webtoapp.ui.design.WtaTabRow
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.VolunteerActivism
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleMarketScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val extensionManager = remember { ExtensionManager.getInstance(context) }
    val repo = remember { ModuleMarketRepository.getInstance(context, extensionManager) }

    val state by repo.state.collectAsState()
    var views by remember { mutableStateOf<List<MarketModuleView>>(emptyList()) }

    LaunchedEffect(repo) {
        repo.views.collectLatest { views = it }
    }

    LaunchedEffect(repo) {

        repo.refresh(force = false)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ModuleCategory?>(null) }
    var installingId by remember { mutableStateOf<String?>(null) }
    var installProgress by remember { mutableStateOf<InstallProgress?>(null) }

    val filtered = remember(views, searchQuery, selectedCategory) {
        views.filter { v ->
            val matchesCategory = selectedCategory == null ||
                runCatching { ModuleCategory.valueOf(v.entry.category) }.getOrNull() == selectedCategory
            val q = searchQuery.trim()
            val matchesSearch = q.isBlank() ||
                v.entry.name.contains(q, ignoreCase = true) ||
                v.entry.description.contains(q, ignoreCase = true) ||
                v.entry.tags.any { it.contains(q, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val filteredCustom = filtered.filter { it.entry.sourceType != "CHROME_EXTENSION" }
    val filteredChromeExt = filtered.filter { it.entry.sourceType == "CHROME_EXTENSION" }
    val currentList = if (selectedTab == 0) filteredCustom else filteredChromeExt

    val aggregatedContributors = remember(views) { aggregateContributors(views) }
    var showContributors by remember { mutableStateOf(false) }

    var highlightModuleId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    fun focusModule(moduleId: String) {
        val targetView = views.firstOrNull { it.entry.id == moduleId } ?: return
        val isChromeExt = targetView.entry.sourceType == "CHROME_EXTENSION"
        selectedTab = if (isChromeExt) 1 else 0
        selectedCategory = null
        searchQuery = ""
        scope.launch {
            kotlinx.coroutines.delay(200)
            val targetList = views.filter {
                (isChromeExt && it.entry.sourceType == "CHROME_EXTENSION") ||
                (!isChromeExt && it.entry.sourceType != "CHROME_EXTENSION")
            }
            val targetIndex = targetList.indexOfFirst { it.entry.id == moduleId }
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex + 1)
                highlightModuleId = moduleId
                kotlinx.coroutines.delay(2500)
                if (highlightModuleId == moduleId) highlightModuleId = null
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(Strings.communityExtStoreTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.back)
                    }
                },
                actions = {
                    if (aggregatedContributors.isNotEmpty()) {
                        IconButton(onClick = { showContributors = true }) {
                            BadgedBox(badge = {
                                if (aggregatedContributors.size > 0) {
                                    Badge {
                                        Text(aggregatedContributors.size.toString())
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Group, contentDescription = Strings.moduleMarketContributorsTitle)
                            }
                        }
                    }
                    IconButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.contributingUrl)))
                    }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = Strings.moduleMarketContribute)
                    }
                    IconButton(onClick = {
                        scope.launch { repo.refresh(force = true) }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = Strings.refresh)
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
                    placeholder = { Text(Strings.moduleMarketSearchHint) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = Strings.clear)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(WtaRadius.Button)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        PremiumFilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text(Strings.moduleMarketAll) }
                        )
                    }
                    items(MarketCategoryHighlights) { category ->
                        PremiumFilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            },
                            label = { Text(category.getDisplayName()) }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                WtaTabRow(
                    tabs = listOf(
                        WtaTab(Strings.extensionModulesTab, filteredCustom.size),
                        WtaTab(Strings.browserExtTab, filteredChromeExt.size)
                    ),
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                when (val s = state) {
                    is MarketState.Idle, is MarketState.Loading -> {
                        if (views.isEmpty()) {
                            LoadingPlaceholder()
                        } else {
                            ModuleListContent(
                                items = currentList,
                                installingId = installingId,
                                installProgress = installProgress,
                                onInstall = { entry ->
                                    installingId = entry.id
                                    installProgress = null
                                    scope.launch {
                                        installModule(entry, repo, snackbarHostState, context.applicationContext) { progress ->
                                            installProgress = progress
                                        }
                                        installingId = null
                                        installProgress = null
                                    }
                                },
                                onOpenSource = { entry ->
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(repo.githubUrl(entry)))
                                    )
                                },
                                onOpenPullRequest = { url ->
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                onOpenSubmitter = { url ->
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                onOpenContributing = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.contributingUrl)))
                                },
                                resolveIcon = { entry -> repo.resolveIconUrl(entry) },
                                listState = listState,
                                highlightModuleId = highlightModuleId
                            )
                        }
                    }
                    is MarketState.Loaded -> {
                        if (currentList.isEmpty()) {
                            EmptyState(
                                searchQuery = searchQuery,
                                onOpenContributing = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.contributingUrl)))
                                }
                            )
                        } else {
                            ModuleListContent(
                                items = currentList,
                                installingId = installingId,
                                installProgress = installProgress,
                                onInstall = { entry ->
                                    installingId = entry.id
                                    installProgress = null
                                    scope.launch {
                                        installModule(entry, repo, snackbarHostState, context.applicationContext) { progress ->
                                            installProgress = progress
                                        }
                                        installingId = null
                                        installProgress = null
                                    }
                                },
                                onOpenSource = { entry ->
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(repo.githubUrl(entry)))
                                    )
                                },
                                onOpenPullRequest = { url ->
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                onOpenSubmitter = { url ->
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                onOpenContributing = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.contributingUrl)))
                                },
                                resolveIcon = { entry -> repo.resolveIconUrl(entry) },
                                listState = listState,
                                highlightModuleId = highlightModuleId
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

    if (showContributors && aggregatedContributors.isNotEmpty()) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showContributors = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ContributorsBoard(
                contributors = aggregatedContributors,
                totalModules = views.size,
                onOpenProfile = { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
                onModuleClick = { moduleName ->
                    showContributors = false
                    val target = views.firstOrNull { it.entry.name == moduleName }
                    if (target != null) focusModule(target.entry.id)
                }
            )
        }
    }
}

private data class AggregatedContributor(
    val login: String,
    val name: String,
    val avatarUrl: String,
    val profileUrl: String,
    val moduleCount: Int,
    val moduleNames: List<String> = emptyList()
)

private fun aggregateContributors(views: List<MarketModuleView>): List<AggregatedContributor> {
    data class Accumulator(
        var login: String = "",
        var name: String = "",
        var avatarUrl: String = "",
        var profileUrl: String = "",
        var moduleNames: MutableList<String> = mutableListOf()
    )
    val acc = mutableMapOf<String, Accumulator>()
    for (view in views) {
        val submission = view.submission ?: continue
        val moduleName = view.entry.name
        val all = listOfNotNull(submission.submitter) + submission.contributors
        for (person in all) {
            if (person.login.isBlank()) continue
            val key = person.login.lowercase()
            val bucket = acc.getOrPut(key) {
                Accumulator(
                    login = person.login,
                    name = person.name.ifBlank { person.login },
                    avatarUrl = person.avatarUrl,
                    profileUrl = person.profileUrl
                )
            }
            if (moduleName !in bucket.moduleNames) bucket.moduleNames.add(moduleName)
        }
    }
    return acc.values.map {
        AggregatedContributor(
            login = it.login,
            name = it.name,
            avatarUrl = it.avatarUrl,
            profileUrl = it.profileUrl,
            moduleCount = it.moduleNames.size,
            moduleNames = it.moduleNames
        )
    }.sortedWith(compareByDescending<AggregatedContributor> { it.moduleCount }.thenBy { it.login.lowercase() })
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
    onOpenPullRequest: (String) -> Unit,
    onOpenSubmitter: (String) -> Unit,
    onOpenContributing: () -> Unit,
    resolveIcon: (ModuleMarketEntry) -> String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    highlightModuleId: String?
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "__contribution_guide__") {
            ContributionGuideCard(onOpenContributing = onOpenContributing)
        }
        items(items, key = { it.entry.id }) { view ->
            MarketModuleCard(
                view = view,
                isInstalling = installingId == view.entry.id,
                installProgress = if (installingId == view.entry.id) installProgress else null,
                iconUrl = resolveIcon(view.entry),
                isHighlighted = highlightModuleId == view.entry.id,
                onInstall = { onInstall(view.entry) },
                onOpenSource = { onOpenSource(view.entry) },
                onOpenPullRequest = onOpenPullRequest,
                onOpenSubmitter = onOpenSubmitter
            )
        }
    }
}

@Composable
private fun ContributionGuideCard(
    onOpenContributing: () -> Unit,
    initiallyExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    WtaCard(
        modifier = Modifier.fillMaxWidth(),
        tone = WtaCardTone.Highlighted,
        contentPadding = PaddingValues(WtaSpacing.Large)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.VolunteerActivism,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    Strings.moduleMarketGuideTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    Strings.moduleMarketGuideSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WtaRadius.Button))
                .clickable { expanded = !expanded }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (expanded) Strings.moduleMarketGuideCollapse else Strings.moduleMarketGuideExpand,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        if (expanded) {
            Spacer(Modifier.height(8.dp))

            GuideSectionHeader(Strings.moduleMarketGuideStepsTitle)
            Spacer(Modifier.height(6.dp))
            val steps = listOf(
                Strings.moduleMarketGuideStep1,
                Strings.moduleMarketGuideStep2,
                Strings.moduleMarketGuideStep3,
                Strings.moduleMarketGuideStep4,
                Strings.moduleMarketGuideStep5
            )
            steps.forEachIndexed { index, step ->
                NumberedStep(number = index + 1, text = step)
                if (index < steps.lastIndex) Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(14.dp))

            GuideSectionHeader(Strings.moduleMarketGuideFilesTitle)
            Spacer(Modifier.height(6.dp))
            GuideFileRow(Icons.Outlined.Description, Strings.moduleMarketGuideFileModuleJson)
            GuideFileRow(Icons.Outlined.Description, Strings.moduleMarketGuideFileMainJs)
            GuideFileRow(Icons.Outlined.Description, Strings.moduleMarketGuideFileStyleCss)
            GuideFileRow(Icons.Outlined.Folder, Strings.moduleMarketGuideFileIcon)

            Spacer(Modifier.height(14.dp))

            GuideSectionHeader(Strings.moduleMarketGuideTipsTitle)
            Spacer(Modifier.height(6.dp))
            GuideTipRow(Strings.moduleMarketGuideTipValidate)
            GuideTipRow(Strings.moduleMarketGuideTipNoTopReturn)
            GuideTipRow(Strings.moduleMarketGuideTipVersion)

            Spacer(Modifier.height(14.dp))

            WtaButton(
                onClick = onOpenContributing,
                text = Strings.moduleMarketGuideOpenRepo,
                variant = WtaButtonVariant.Tonal,
                size = WtaButtonSize.Small,
                leadingIcon = Icons.Default.OpenInNew,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GuideSectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun NumberedStep(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GuideFileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GuideTipRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Filled.Lightbulb,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarketModuleCard(
    view: MarketModuleView,
    isInstalling: Boolean,
    installProgress: InstallProgress?,
    iconUrl: String?,
    isHighlighted: Boolean,
    onInstall: () -> Unit,
    onOpenSource: () -> Unit,
    onOpenPullRequest: (String) -> Unit,
    onOpenSubmitter: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val cardTone = if (isHighlighted) WtaCardTone.Highlighted else WtaCardTone.Surface
    val cardModifier = if (isHighlighted) {
        Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(WtaRadius.Card))
    } else {
        Modifier.fillMaxWidth()
    }

    WtaCard(
        modifier = cardModifier,
        tone = cardTone,
        contentPadding = PaddingValues(WtaSpacing.Large)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            ModuleIcon(
                iconUrl = iconUrl,
                fallbackLetter = view.entry.name.take(1).uppercase()
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        view.entry.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "v${view.entry.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (view.entry.description.isNotBlank()) {
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

        view.submission?.let { submission ->
            Spacer(Modifier.height(10.dp))
            SubmissionStrip(
                submission = submission,
                onOpenPullRequest = onOpenPullRequest,
                onOpenSubmitter = onOpenSubmitter
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenSource, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.OpenInNew, contentDescription = Strings.moduleMarketViewSource)
            }
            TextButton(onClick = { expanded = !expanded }) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(Strings.moduleMarketDetails, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.weight(1f))
            InstallButton(
                state = view.state,
                isInstalling = isInstalling,
                installProgress = installProgress,
                onClick = onInstall
            )
        }

        AnimatedVisibility(visible = expanded) {
            ModuleDetailsPanel(
                view = view,
                onOpenPullRequest = onOpenPullRequest,
                onOpenSubmitter = onOpenSubmitter
            )
        }
    }
}

@Composable
private fun ModuleDetailsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false).padding(start = 12.dp)
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ModuleDetailsPanel(
    view: MarketModuleView,
    onOpenPullRequest: (String) -> Unit,
    onOpenSubmitter: (String) -> Unit
) {
    val entry = view.entry
    val submission = view.submission
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp)
    ) {
        ModuleDetailsRow(Strings.moduleMarketCategory, entry.category)
        ModuleDetailsRow(Strings.moduleMarketRunAt, entry.runAt)
        ModuleDetailsRow(Strings.moduleMarketPermissions, entry.permissions.joinToString(", ").ifBlank { "—" })

        if (entry.urlMatches.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                Strings.moduleMarketUrlMatches,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            entry.urlMatches.take(4).forEach { rule ->
                Text(
                    "  " + rule.pattern,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (entry.urlMatches.size > 4) {
                Text(
                    "  +${entry.urlMatches.size - 4}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (entry.tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                Strings.moduleMarketTags,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                entry.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            tag,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        submission?.let { sub ->
            Spacer(Modifier.height(10.dp))
            androidx.compose.material3.Divider(
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(8.dp))

            sub.prNumber?.let { num ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { sub.prUrl?.let(onOpenPullRequest) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MergeType,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "#$num",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    sub.submittedAt?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            Strings.moduleMarketMergedAt + " " + formatRelativeTime(it).orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            sub.submitter?.let { submitter ->
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (submitter.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(submitter.avatarUrl).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        Strings.moduleMarketSubmittedBy.replace("%s", submitter.login),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = if (submitter.profileUrl.isNotBlank()) {
                            Modifier.clickable { onOpenSubmitter(submitter.profileUrl) }
                        } else Modifier
                    )
                }
            }

            if (sub.contributors.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PeopleAvatarStack(
                        people = sub.contributors,
                        onOpenProfile = onOpenSubmitter
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        Strings.moduleMarketWithContributors.replace("%d", sub.contributors.size.toString()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleIcon(
    iconUrl: String?,
    fallbackLetter: String,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = if (!iconUrl.isNullOrBlank()) {
            ImageRequest.Builder(context).data(iconUrl).crossfade(true).build()
        } else null
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (painter.state is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(
                text = fallbackLetter,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SubmissionStrip(
    submission: ModuleSubmission,
    onOpenPullRequest: (String) -> Unit,
    onOpenSubmitter: (String) -> Unit
) {
    val context = LocalContext.current
    val submitter = submission.submitter
    val contributors = submission.contributors
    val timeAgo = submission.submittedAt?.let { formatRelativeTime(it) }
    val prUrl = submission.prUrl

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        PeopleAvatarStack(
            people = listOfNotNull(submitter) + contributors,
            onOpenProfile = onOpenSubmitter
        )

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            val loginText = submitter?.login?.takeIf { it.isNotBlank() }
            if (loginText != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        Strings.moduleMarketSubmittedBy.replace("%s", loginText),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (submitter?.profileUrl?.isNotBlank() == true) {
                            Modifier.clickable { onOpenSubmitter(submitter.profileUrl) }
                        } else Modifier
                    )
                    if (contributors.isNotEmpty()) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            Strings.moduleMarketWithContributors.replace("%d", contributors.size.toString()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (submission.direct) {
                    Icon(
                        Icons.Default.MergeType,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        Strings.moduleMarketDirectPush,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                } else if (submission.prNumber != null) {
                    Icon(
                        Icons.Default.MergeType,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        Strings.moduleMarketPrNumber.replace("%d", submission.prNumber.toString()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }

                if (timeAgo != null) {
                    if (submission.direct || submission.prNumber != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "·",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        if (!prUrl.isNullOrBlank() && !submission.direct) {
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { onOpenPullRequest(prUrl) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = Strings.moduleMarketViewPullRequest,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PeopleAvatarStack(
    people: List<ModuleSubmitter>,
    onOpenProfile: (String) -> Unit,
    maxVisible: Int = 4
) {
    val context = LocalContext.current
    val visible = people.take(maxVisible)
    val overflow = people.size - visible.size
    val avatarSize = 22.dp
    val overlap = 12.dp

    val totalWidth = avatarSize + (overlap * (visible.size - 1).coerceAtLeast(0)) +
        (if (overflow > 0) avatarSize else 0.dp)

    Box(modifier = Modifier.size(width = totalWidth, height = avatarSize)) {
        visible.forEachIndexed { index, person ->
            Box(
                modifier = Modifier
                    .offset(x = overlap * index)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .let { base ->
                        if (person.profileUrl.isNotBlank()) {
                            base.clickable { onOpenProfile(person.profileUrl) }
                        } else base
                    }
            ) {
                if (person.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(person.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            person.login.take(1).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        if (overflow > 0) {
            Box(
                modifier = Modifier
                    .offset(x = overlap * visible.size)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "+$overflow",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun ContributorsBoard(
    contributors: List<AggregatedContributor>,
    totalModules: Int,
    onOpenProfile: (String) -> Unit,
    onModuleClick: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                Strings.moduleMarketContributorsTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            Strings.moduleMarketContributorsOverview
                .replace("%1\$d", contributors.size.toString())
                .replace("%2\$d", totalModules.toString()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 460.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            contributors.forEachIndexed { index, contributor ->
                val isExpanded = expandedStates[contributor.login] == true
                val isTop = index == 0 && contributor.moduleCount > 1

                WtaCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = if (isTop) WtaCardTone.Highlighted else WtaCardTone.Surface,
                    contentPadding = PaddingValues(WtaSpacing.Medium)
                ) {
                    Column(
                        modifier = Modifier.animateContentSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .let { base ->
                                    if (contributor.profileUrl.isNotBlank()) {
                                        base.clickable {
                                            expandedStates[contributor.login] = !isExpanded
                                        }
                                    } else base
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ContributorAvatar(contributor, size = 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        contributor.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (isTop) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                Strings.moduleMarketTopContributor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    "@" + contributor.login,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    contributor.moduleCount.toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    Strings.moduleMarketModulesContributed,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Text(
                                    Strings.moduleMarketModulesContributed,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                contributor.moduleNames.forEach { name ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onModuleClick(name) }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (contributor.profileUrl.isNotBlank()) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .clickable { onOpenProfile(contributor.profileUrl) }
                                            .padding(vertical = 10.dp, horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            Strings.moduleMarketViewProfile,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributorAvatar(contributor: AggregatedContributor, size: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    if (contributor.avatarUrl.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(contributor.avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                contributor.login.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatRelativeTime(iso: String): String? {
    val instant = try {
        Instant.parse(iso)
    } catch (e: DateTimeParseException) {
        return null
    } catch (e: Exception) {
        return null
    }
    val deltaMs = (System.currentTimeMillis() - instant.toEpochMilli()).coerceAtLeast(0)
    val seconds = deltaMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    val years = days / 365
    return when {
        seconds < 60 -> Strings.moduleMarketTimeJustNow
        minutes < 60 -> Strings.moduleMarketTimeMinutesAgo.replace("%d", minutes.toString())
        hours < 24 -> Strings.moduleMarketTimeHoursAgo.replace("%d", hours.toString())
        days < 30 -> Strings.moduleMarketTimeDaysAgo.replace("%d", days.toString())
        months < 12 -> Strings.moduleMarketTimeMonthsAgo.replace("%d", months.toString())
        else -> Strings.moduleMarketTimeYearsAgo.replace("%d", years.toString())
    }
}

@Composable
private fun InstallButton(
    state: MarketInstallState,
    isInstalling: Boolean,
    installProgress: InstallProgress?,
    onClick: () -> Unit
) {
    if (isInstalling) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    installProgress?.label ?: Strings.moduleMarketInstalling,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (installProgress != null) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { installProgress.fraction },
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )
            }
        }
        return
    }
    when (state) {
        MarketInstallState.NotInstalled -> WtaButton(
            onClick = onClick,
            text = Strings.moduleMarketInstall,
            variant = WtaButtonVariant.Primary,
            size = WtaButtonSize.Small,
            leadingIcon = Icons.Default.CloudDownload
        )
        MarketInstallState.UpdateAvailable -> WtaButton(
            onClick = onClick,
            text = Strings.moduleMarketUpdate,
            variant = WtaButtonVariant.Tonal,
            size = WtaButtonSize.Small,
            leadingIcon = Icons.Default.SystemUpdate
        )
        MarketInstallState.UpToDate -> Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                Strings.moduleMarketInstalled.replace("%s", "").trim(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoadingPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(
                Strings.moduleMarketLoading,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState(searchQuery: String, onOpenContributing: () -> Unit) {
    if (searchQuery.isNotBlank()) {

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                Strings.moduleMarketNoResults,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                Strings.moduleMarketGuideEmptyCta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        item {
            ContributionGuideCard(
                onOpenContributing = onOpenContributing,
                initiallyExpanded = true
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))
            WtaButton(
                onClick = onRetry,
                text = Strings.retry,
                variant = WtaButtonVariant.Tonal,
                size = WtaButtonSize.Small
            )
        }
    }
}

private val MarketCategoryHighlights = listOf(
    ModuleCategory.CONTENT_FILTER,
    ModuleCategory.STYLE_MODIFIER,
    ModuleCategory.FUNCTION_ENHANCE,
    ModuleCategory.MEDIA,
    ModuleCategory.SECURITY,
    ModuleCategory.TRANSLATE,
    ModuleCategory.DEVELOPER,
    ModuleCategory.OTHER
)
