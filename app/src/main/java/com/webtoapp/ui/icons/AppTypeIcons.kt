package com.webtoapp.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WebAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.webtoapp.data.model.AppType

@Composable
fun WtaAppTypeIcon(
    type: AppType,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    val vector = when (type) {
        AppType.WEB -> Icons.Filled.Language
        AppType.MULTI_WEB -> Icons.Filled.DynamicFeed
        AppType.HTML -> Icons.Filled.Html
        AppType.FRONTEND -> Icons.Filled.WebAsset
        AppType.GALLERY -> Icons.Filled.PhotoLibrary
        AppType.IMAGE, AppType.VIDEO -> Icons.Filled.PlayCircle
        AppType.NODEJS_APP -> Icons.Filled.Javascript
        AppType.PHP_APP -> Icons.Filled.Terminal
        AppType.PYTHON_APP -> Icons.Filled.Code
        AppType.GO_APP -> Icons.Filled.Bolt
        AppType.WORDPRESS -> Icons.AutoMirrored.Filled.Article
    }
    Icon(
        imageVector = vector,
        contentDescription = null,
        modifier = modifier,
        tint = color
    )
}

@Composable
fun WtaOfflinePackTypeIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Icon(
        imageVector = Icons.Filled.CloudDownload,
        contentDescription = null,
        modifier = modifier,
        tint = color
    )
}