package com.webtoapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.extension.ExtensionManager
import com.webtoapp.core.extension.ModuleCategory
import com.webtoapp.core.i18n.Strings
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
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

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(Strings.moduleMarketTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.back)
                    }
                },
                actions = {
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

                when (val s = state) {
                    is MarketState.Idle, is MarketState.Loading -> {
                        if (views.isEmpty()) {
                            LoadingPlaceholder()
                        } else {
                            ModuleListContent(
                                items = filtered,
                                installingId = installingId,
                                onInstall = { entry ->
                                    installingId = entry.id
                                    scope.launch {
                                        installModule(entry, repo, snackbarHostState, context.applicationContext)
                                        installingId = null
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
                                resolveIcon = { entry -> repo.resolveIconUrl(entry) }
                            )
                        }
                    }
                    is MarketState.Loaded -> {
                        if (filtered.isEmpty()) {
                            EmptyState(
                                searchQuery = searchQuery,
                                onOpenContributing = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.contributingUrl)))
                                }
                            )
                        } else {
                            ModuleListContent(
                                items = filtered,
                                installingId = installingId,
                                onInstall = { entry ->
                                    installingId = entry.id
                                    scope.launch {
                                        installModule(entry, repo, snackbarHostState, context.applicationContext)
                                        installingId = null
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
                                resolveIcon = { entry -> repo.resolveIconUrl(entry) }
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

private suspend fun installModule(
    entry: ModuleMarketEntry,
    repo: ModuleMarketRepository,
    snackbar: SnackbarHostState,
    appContext: android.content.Context
) {
    val result = repo.install(entry)
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
    onInstall: (ModuleMarketEntry) -> Unit,
    onOpenSource: (ModuleMarketEntry) -> Unit,
    onOpenPullRequest: (String) -> Unit,
    onOpenSubmitter: (String) -> Unit,
    onOpenContributing: () -> Unit,
    resolveIcon: (ModuleMarketEntry) -> String?
) {
    LazyColumn(
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
                iconUrl = resolveIcon(view.entry),
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
    iconUrl: String?,
    onInstall: () -> Unit,
    onOpenSource: () -> Unit,
    onOpenPullRequest: (String) -> Unit,
    onOpenSubmitter: (String) -> Unit
) {
    WtaCard(
        modifier = Modifier.fillMaxWidth(),
        tone = WtaCardTone.Surface,
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
            Spacer(Modifier.weight(1f))
            InstallButton(
                state = view.state,
                isInstalling = isInstalling,
                onClick = onInstall
            )
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
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!iconUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(iconUrl)
                    .crossfade(true)
                    .build(),
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
    val timeAgo = submission.submittedAt?.let { formatRelativeTime(it) }
    val prUrl = submission.prUrl

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val avatarModifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .let { base ->
                if (submitter?.profileUrl?.isNotBlank() == true) {
                    base.clickable { onOpenSubmitter(submitter.profileUrl) }
                } else base
            }
        if (submitter?.avatarUrl?.isNotBlank() == true) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(submitter.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = avatarModifier
            )
        } else {
            Box(modifier = avatarModifier, contentAlignment = Alignment.Center) {
                Text(
                    submitter?.login?.take(1)?.uppercase().orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            val loginText = submitter?.login?.takeIf { it.isNotBlank() }
            if (loginText != null) {
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
    onClick: () -> Unit
) {
    if (isInstalling) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text(Strings.moduleMarketInstalling, style = MaterialTheme.typography.labelMedium)
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
