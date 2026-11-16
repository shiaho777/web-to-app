package com.webtoapp.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.webtoapp.R
import com.webtoapp.data.model.AppType

@Composable
fun WtaAppTypeIcon(
    type: AppType,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    val lineArt = when (type) {
        AppType.WEB -> R.drawable.ic_type_web
        AppType.HTML -> R.drawable.ic_type_html
        AppType.FRONTEND -> R.drawable.ic_type_frontend
        AppType.GALLERY -> R.drawable.ic_type_gallery
        AppType.IMAGE, AppType.VIDEO -> R.drawable.ic_type_media
        AppType.NODEJS_APP -> R.drawable.ic_type_nodejs
        AppType.PHP_APP -> R.drawable.ic_type_php
        AppType.PYTHON_APP -> R.drawable.ic_type_python
        AppType.GO_APP -> R.drawable.ic_type_go
        AppType.WORDPRESS -> R.drawable.ic_type_wordpress
        AppType.MULTI_WEB -> null
    }
    if (lineArt != null) {
        Icon(
            painter = painterResource(lineArt),
            contentDescription = null,
            modifier = modifier,
            tint = color
        )
    } else {
        Icon(
            imageVector = Icons.Filled.DynamicFeed,
            contentDescription = null,
            modifier = modifier,
            tint = color
        )
    }
}

@Composable
fun WtaOfflinePackTypeIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Icon(
        painter = painterResource(R.drawable.ic_type_offline_pack),
        contentDescription = null,
        modifier = modifier,
        tint = color
    )
}