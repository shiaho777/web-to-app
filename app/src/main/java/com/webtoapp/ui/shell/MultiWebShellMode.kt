package com.webtoapp.ui.shell

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import android.webkit.WebView
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.logging.AppLogger
import com.webtoapp.core.shell.MultiWebShellConfig
import com.webtoapp.core.shell.MultiWebSiteShellConfig
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.core.webview.WebViewCallbacks
import com.webtoapp.data.model.WebViewConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiWebShellMode(
    config: ShellConfig,
    webViewConfig: WebViewConfig,
    webViewCallbacks: WebViewCallbacks,
    webViewManager: com.webtoapp.core.webview.WebViewManager,
    onWebViewCreated: (WebView) -> Unit,
    swipeRefreshEnabled: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val multiWebConfig = config.multiWebConfig
    val sites = multiWebConfig.sites.filter { it.enabled }

    if (sites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Home, null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No sites configured",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    when (multiWebConfig.displayMode.uppercase()) {
        "TABS" -> TabsMode(config, multiWebConfig, sites, webViewConfig, webViewCallbacks, webViewManager, onWebViewCreated, swipeRefreshEnabled, isRefreshing, onRefresh)
        "CARDS" -> CardsMode(config, multiWebConfig, sites, webViewConfig, webViewCallbacks, webViewManager, onWebViewCreated, swipeRefreshEnabled, isRefreshing, onRefresh)
        "FEED" -> FeedMode(config, multiWebConfig, sites, webViewConfig, webViewCallbacks, webViewManager, onWebViewCreated, swipeRefreshEnabled, isRefreshing, onRefresh)
        "DRAWER" -> DrawerMode(config, multiWebConfig, sites, webViewConfig, webViewCallbacks, webViewManager, onWebViewCreated, swipeRefreshEnabled, isRefreshing, onRefresh)
        else -> TabsMode(config, multiWebConfig, sites, webViewConfig, webViewCallbacks, webViewManager, onWebViewCreated, swipeRefreshEnabled, isRefreshing, onRefresh)
    }
}

@Composable
private fun SiteContent(
    site: MultiWebSiteShellConfig,
    config: ShellConfig,
    webViewConfig: WebViewConfig,
    webViewCallbacks: WebViewCallbacks,
    webViewManager: com.webtoapp.core.webview.WebViewManager,
    onWebViewCreated: (WebView) -> Unit,
    swipeRefreshEnabled: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val siteCfg = site.siteShellConfig
    val effectiveConfig = siteCfg?.copy(
        engineType = siteCfg.engineType.takeIf { it.isNotBlank() && it != "SYSTEM_WEBVIEW" }
            ?: config.engineType
    ) ?: ShellConfig(
        appName = site.name,
        appType = "WEB",
        targetUrl = site.url,
        packageName = config.packageName,
        engineType = config.engineType
    )
    val siteWvCfg = remember(site.id) { buildWebViewConfig(effectiveConfig) }
    ShellContentRouter(
        appType = site.appType,
        config = effectiveConfig,
        webViewRecreationKey = site.id.hashCode(),
        webViewConfig = siteWvCfg,
        webViewCallbacks = webViewCallbacks,
        webViewManager = webViewManager,
        deepLinkUrl = site.url.takeIf { site.appType == "WEB" && it.isNotBlank() },
        swipeRefreshEnabled = swipeRefreshEnabled,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        onWebViewCreated = onWebViewCreated,
        onWebViewRefUpdated = { },
        onActivityFinish = { }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabsMode(
    config: ShellConfig,
    multiWebConfig: MultiWebShellConfig,
    sites: List<MultiWebSiteShellConfig>,
    webViewConfig: WebViewConfig,
    webViewCallbacks: WebViewCallbacks,
    webViewManager: com.webtoapp.core.webview.WebViewManager,
    onWebViewCreated: (WebView) -> Unit,
    swipeRefreshEnabled: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab, sites.size) {
        val site = sites.getOrNull(selectedTab)
        if (site != null) {
            webViewCallbacks.onTitleChanged(site.name.ifBlank { extractDomain(site.url) })
            if (site.url.isNotBlank()) {
                webViewCallbacks.onPageStarted(site.url)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (sites.size > 1) {
                Surface(
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        sites.forEachIndexed { index, site ->
                            val isSelected = selectedTab == index
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedTab = index },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (site.iconEmoji.isNotBlank()) {
                                    Text(
                                        site.iconEmoji,
                                        fontSize = if (isSelected) 18.sp else 16.sp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = null,
                                        modifier = Modifier.size(if (isSelected) 20.dp else 18.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    site.name.ifBlank { extractDomain(site.url) },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 10.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val visitedTabs = remember { mutableStateMapOf<Int, Boolean>() }
            visitedTabs[selectedTab] = true

            sites.forEachIndexed { index, site ->
                val isVisited = visitedTabs.containsKey(index)
                if (isVisited) {
                    val isVisible = index == selectedTab
                    key(site.id) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(if (isVisible) 1f else 0f)
                                .then(
                                    if (isVisible) Modifier
                                    else Modifier.alpha(0f)
                                )
                        ) {
                            SiteContent(
                                site = site,
                                config = config,
                                webViewConfig = webViewConfig,
                                webViewCallbacks = webViewCallbacks,
                                webViewManager = webViewManager,
                                onWebViewCreated = if (isVisible) onWebViewCreated else ({ }),
                                swipeRefreshEnabled = swipeRefreshEnabled,
                                isRefreshing = isRefreshing,
                                onRefresh = onRefresh
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardsMode(
    config: ShellConfig,
    multiWebConfig: MultiWebShellConfig,
    sites: List<MultiWebSiteShellConfig>,
    webViewConfig: WebViewConfig,
    webViewCallbacks: WebViewCallbacks,
    webViewManager: com.webtoapp.core.webview.WebViewManager,
    onWebViewCreated: (WebView) -> Unit,
    swipeRefreshEnabled: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    var openSite by remember { mutableStateOf<MultiWebSiteShellConfig?>(null) }

    if (openSite != null) {
        val site = openSite!!
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = { Text(site.name.ifBlank { extractDomain(site.url) }) },
                    navigationIcon = {
                        IconButton(onClick = { openSite = null }) {
                            Icon(Icons.Default.Close, contentDescription = Strings.close)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                key(site.id) {
                    SiteContent(site, config, webViewConfig, webViewCallbacks, webViewManager, onWebViewCreated, swipeRefreshEnabled, isRefreshing, onRefresh)
                }
            }
        }
    } else {
        CardsHomeGrid(
            sites = sites,
            showIcons = multiWebConfig.showSiteIcons,
            onSiteClicked = { openSite = it }
        )
    }
}

@Composable
private fun CardsHomeGrid(
    sites: List<MultiWebSiteShellConfig>,
    showIcons: Boolean,
    onSiteClicked: (MultiWebSiteShellConfig) -> Unit
) {
    val grouped = remember(sites) {
        val categorized = sites.groupBy { it.category.ifBlank { "" } }
        categorized.entries.sortedBy { if (it.key.isBlank()) "zzz" else it.key }
    }

    val cardColors = remember {
        listOf(
            Color(0xFF6366F1) to Color(0xFF818CF8),
            Color(0xFFEC4899) to Color(0xFFF472B6),
            Color(0xFF14B8A6) to Color(0xFF2DD4BF),
            Color(0xFFF59E0B) to Color(0xFFFBBF24),
            Color(0xFF8B5CF6) to Color(0xFFA78BFA),
            Color(0xFF06B6D4) to Color(0xFF22D3EE),
            Color(0xFFEF4444) to Color(0xFFF87171),
            Color(0xFF10B981) to Color(0xFF34D399),
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("🌐", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("My Sites", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("${sites.size} sites", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        grouped.forEach { (category, categorySites) ->
            if (category.isNotBlank()) {
                item {
                    Text(
                        category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
            }

            val chunked = categorySites.chunked(2)
            items(chunked) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEachIndexed { i, site ->
                        val colorIndex = (sites.indexOf(site)) % cardColors.size
                        val (colorStart, colorEnd) = cardColors[colorIndex]
                        SiteCard(
                            site = site,
                            colorStart = colorStart,
                            colorEnd = colorEnd,
                            showIcon = showIcons,
                            onClick = { onSiteClicked(site) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SiteCard(
    site: MultiWebSiteShellConfig,
    colorStart: Color,
    colorEnd: Color,
    showIcon: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.linearGradient(colors = listOf(colorStart, colorEnd)))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (showIcon) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (site.iconEmoji.isNotBlank()) {
                            Text(site.iconEmoji, fontSize = 22.sp)
                        } else {
                            Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Column {
                    Text(
                        site.name.ifBlank { extractDomain(site.url) },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        extractDomain(site.url),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

data class FeedItem(
    val title: String,
    val url: String,
    val siteName: String,
    val siteEmoji: String,
    val siteUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedMode(
    config: ShellConfig,
    multiWebConfig: MultiWebShellConfig,
    sites: List<MultiWebSiteShellConfig>,
    webViewConfig: WebViewConfig,
    webViewCallbacks: WebViewCallbacks,
    webViewManager: com.webtoapp.core.webview.WebViewManager,
    onWebViewCreated: (WebView) -> Unit,
    swipeRefreshEnabled: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var feedItems by remember { mutableStateOf<List<FeedItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var openUrl by remember { mutableStateOf<String?>(null) }
    var openTitle by remember { mutableStateOf("") }

    LaunchedEffect(sites) {
        isLoading = true
        feedItems = withContext(Dispatchers.IO) { fetchFeedItems(sites) }
        isLoading = false
    }

    if (openUrl != null) {
        val urlCfg = remember(openUrl) {
            ShellConfig(appName = openTitle, appType = "WEB", targetUrl = openUrl ?: "", packageName = config.packageName)
        }
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = { Text(openTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = { openUrl = null }) {
                            Icon(Icons.Default.Close, contentDescription = Strings.close)
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                key(openUrl) {
                    ShellContentRouter(
                        appType = "WEB",
                        config = urlCfg,
                        webViewRecreationKey = (openUrl ?: "").hashCode(),
                        webViewConfig = webViewConfig,
                        webViewCallbacks = webViewCallbacks,
                        webViewManager = webViewManager,
                        deepLinkUrl = openUrl,
                        swipeRefreshEnabled = swipeRefreshEnabled,
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        onWebViewCreated = onWebViewCreated,
                        onWebViewRefUpdated = { },
                        onActivityFinish = { }
                    )
                }
            }
        }
    } else {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = { Text(Strings.multiWebModeFeed, fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                isLoading = true
                                feedItems = withContext(Dispatchers.IO) { fetchFeedItems(sites) }
                                isLoading = false
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = Strings.refresh)
                        }
                    }
                )
            }
        ) { padding ->
            if (isLoading && feedItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(Strings.loading, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (feedItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No articles found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Configure CSS selectors to extract articles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("${feedItems.size} articles from ${sites.size} sites", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(feedItems) { item ->
                        FeedItemCard(item = item, onClick = { openUrl = item.url; openTitle = item.title })
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FeedItemCard(item: FeedItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.siteEmoji.isNotBlank()) {
                    Text(item.siteEmoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(item.siteName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(extractDomain(item.url), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerMode(
    config: ShellConfig,
    multiWebConfig: MultiWebShellConfig,
    sites: List<MultiWebSiteShellConfig>,
    webViewConfig: WebViewConfig,
    webViewCallbacks: WebViewCallbacks,
    webViewManager: com.webtoapp.core.webview.WebViewManager,
    onWebViewCreated: (WebView) -> Unit,
    swipeRefreshEnabled: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    var selectedSite by remember { mutableStateOf(sites.firstOrNull()) }
    var drawerVisible by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(drawerVisible) {
        if (drawerVisible) scope.launch { drawerState.open() } else scope.launch { drawerState.close() }
    }
    LaunchedEffect(drawerState.currentValue) {
        if (!drawerVisible && drawerState.currentValue == DrawerValue.Open) drawerVisible = true
        else if (drawerVisible && drawerState.currentValue == DrawerValue.Closed) drawerVisible = false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(24.dp)
                ) {
                    Text("My Sites", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("${sites.size} sites", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(sites) { site ->
                        val isSelected = selectedSite?.id == site.id
                        DrawerSiteItem(site = site, isSelected = isSelected, onClick = { selectedSite = site; drawerVisible = false })
                    }
                }
            }
        },
        gesturesEnabled = true
    ) {
        val currentSite = selectedSite ?: sites.firstOrNull()
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    title = { Text(currentSite?.name?.ifBlank { currentSite?.url?.let { extractDomain(it) } ?: "" } ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = { IconButton(onClick = { drawerVisible = !drawerVisible }) { Icon(Icons.Default.Menu, contentDescription = Strings.more) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                currentSite?.let { site ->
                    key(site.id) {
                        SiteContent(site, config, webViewConfig, webViewCallbacks, webViewManager, onWebViewCreated, swipeRefreshEnabled, isRefreshing, onRefresh)
                    }
                } ?: run {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Home, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No site selected", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerSiteItem(site: MultiWebSiteShellConfig, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "drawerItemBg"
    )
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(backgroundColor).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            if (site.iconEmoji.isNotBlank()) Text(site.iconEmoji, fontSize = 18.sp)
            else Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                site.name.ifBlank { extractDomain(site.url) },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                extractDomain(site.url),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
    }
}

private fun extractDomain(url: String): String {
    return try {
        if (url.startsWith("http")) URL(url).host.removePrefix("www.") else url.substringAfterLast("/")
    } catch (e: Exception) {
        url.removePrefix("https://").removePrefix("http://").substringBefore("/")
    }
}

private fun fetchFeedItems(sites: List<MultiWebSiteShellConfig>): List<FeedItem> {
    val allItems = mutableListOf<FeedItem>()
    for (site in sites) {
        if (site.url.isBlank()) continue
        val siteName = site.name.ifBlank { extractDomain(site.url) }
        try {
            val connection = URL(site.url).openConnection()
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            val html = connection.getInputStream().bufferedReader().readText()
            if (site.cssSelector.isNotBlank()) {
                val elements = extractElementsByCss(html, site.cssSelector)
                elements.forEach { element ->
                    val text = extractTextFromHtml(element).trim()
                    val href = extractHrefFromHtml(element)
                    if (text.isNotBlank() && text.length > 5) {
                        val fullUrl = resolveUrl(site.url, href ?: "")
                        allItems.add(FeedItem(title = text, url = fullUrl.ifBlank { site.url }, siteName = siteName, siteEmoji = site.iconEmoji, siteUrl = site.url))
                    }
                }
            } else {
                val linkPattern = Regex("""<a\s[^>]*href\s*=\s*["']([^"']*)["'][^>]*>(.*?)</a>""", RegexOption.DOT_MATCHES_ALL)
                linkPattern.findAll(html).forEach { match ->
                    val href = match.groupValues[1]
                    val text = extractTextFromHtml(match.groupValues[2]).trim()
                    if (text.length in 10..200 && !text.contains("<") && href.isNotBlank()) {
                        val fullUrl = resolveUrl(site.url, href)
                        allItems.add(FeedItem(title = text, url = fullUrl, siteName = siteName, siteEmoji = site.iconEmoji, siteUrl = site.url))
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.w("MultiWebFeed", "Failed to fetch ${site.url}: ${e.message}")
        }
    }
    return allItems.distinctBy { it.title }.take(100)
}

private fun extractElementsByCss(html: String, selector: String): List<String> {
    val parts = selector.trim().split(Regex("\\s+"))
    val targetSelector = parts.last()
    val tag: String?
    val className: String?
    val idName: String?
    when {
        targetSelector.contains(".") -> {
            val split = targetSelector.split(".", limit = 2)
            tag = split[0].ifBlank { null }
            className = split[1]
            idName = null
        }
        targetSelector.contains("#") -> {
            val split = targetSelector.split("#", limit = 2)
            tag = split[0].ifBlank { null }
            idName = split[1]
            className = null
        }
        else -> {
            tag = targetSelector
            className = null
            idName = null
        }
    }
    val results = mutableListOf<String>()
    val tagPattern = tag ?: "[a-zA-Z][a-zA-Z0-9]*"
    val attrPattern = buildString {
        append("""<$tagPattern\s""")
        if (className != null) {
            append("""[^>]*class\s*=\s*["'][^"']*\b${Regex.escape(className)}\b[^"']*["']""")
        } else if (idName != null) {
            append("""[^>]*id\s*=\s*["']${Regex.escape(idName)}["']""")
        } else {
            append("[^>]*")
        }
        append(""">.*?</$tagPattern>""")
    }
    try {
        val regex = Regex(attrPattern, setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        regex.findAll(html).take(50).forEach { match -> results.add(match.value) }
    } catch (e: Exception) {
        AppLogger.w("MultiWebFeed", "CSS selector regex failed: ${e.message}")
    }
    return results
}

private fun extractTextFromHtml(html: String): String {
    return html.replace(Regex("<[^>]*>"), "")
        .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
        .replace("&quot;", "\"").replace("&#39;", "'").replace("&nbsp;", " ")
        .replace(Regex("\\s+"), " ").trim()
}

private fun extractHrefFromHtml(html: String): String? {
    val match = Regex("""href\s*=\s*["']([^"']*)["']""").find(html)
    return match?.groupValues?.get(1)
}

private fun resolveUrl(baseUrl: String, href: String): String {
    if (href.isBlank()) return baseUrl
    if (href.startsWith("http://") || href.startsWith("https://")) return href
    if (href.startsWith("//")) return "https:$href"
    return try {
        val base = URL(baseUrl)
        if (href.startsWith("/")) "${base.protocol}://${base.host}$href"
        else "${baseUrl.substringBeforeLast("/")}/$href"
    } catch (e: Exception) {
        href
    }
}
