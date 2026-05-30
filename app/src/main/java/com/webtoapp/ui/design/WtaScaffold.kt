package com.webtoapp.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WtaScreen(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    snackbarHostState: SnackbarHostState? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},

    titleContent: (@Composable () -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable BoxScope.(PaddingValues) -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val screenContent: @Composable () -> Unit = {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            snackbarHost = {
                snackbarHostState?.let { state ->
                    SnackbarHost(hostState = state) { data ->
                        WtaSnackbar(
                            message = data.visuals.message,
                            actionLabel = data.visuals.actionLabel,
                            onAction = if (data.visuals.actionLabel != null) {
                                { data.performAction() }
                            } else null,
                            onDismiss = { data.dismiss() }
                        )
                    }
                }
            },
            topBar = {
                WtaTopBar(
                    title = title,
                    subtitle = subtitle,
                    onBack = onBack,
                    actions = actions,
                    titleContent = titleContent,
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition
        ) { padding ->
            WtaBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(contentPadding)
                    .imePadding()
            ) {
                content(padding)
            }
        }
    }

    if (onBack != null) {
        WtaSwipeBackContainer(onBack = onBack) {
            screenContent()
        }
    } else {
        screenContent()
    }
}

@Composable
private fun WtaSnackbar(
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Snackbar(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        containerColor = colors.inverseSurface,
        contentColor = colors.inverseOnSurface,
        actionContentColor = colors.inversePrimary,
        dismissActionContentColor = colors.inverseOnSurface.copy(alpha = 0.7f),
        action = if (actionLabel != null && onAction != null) {
            {
                androidx.compose.material3.TextButton(
                    onClick = onAction,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = colors.inversePrimary
                    )
                ) { Text(actionLabel) }
            }
        } else null
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WtaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},

    titleContent: (@Composable () -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                if (titleContent != null) {
                    titleContent()
                } else if (subtitle.isNullOrBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    WtaIconButton(
                        onClick = onBack,
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = Strings.back
                    )
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(

                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            scrollBehavior = scrollBehavior
        )

        val fraction = scrollBehavior?.state?.overlappedFraction ?: 0f
        val dividerAlpha = (fraction * 2f).coerceIn(0f, 1f)
        if (dividerAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = dividerAlpha * 0.7f)
                    )
            )
        }
    }
}
