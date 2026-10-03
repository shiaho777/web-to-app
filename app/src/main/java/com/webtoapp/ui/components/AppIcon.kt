package com.webtoapp.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.webtoapp.R
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp

@Composable
fun WtaAppIcon(
    app: WebApp,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val corner = size / 4.5f
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(corner),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (app.iconPath != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(app.iconPath)
                    .crossfade(true)
                    .build(),
                contentDescription = app.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(corner)),
                contentScale = ContentScale.Crop
            )
        } else {
            val defaultIconRes = when (app.appType) {
                AppType.WEB -> R.drawable.ic_type_web
                AppType.IMAGE -> R.drawable.ic_type_media
                AppType.VIDEO -> R.drawable.ic_type_media
                AppType.HTML -> R.drawable.ic_type_html
                AppType.GALLERY -> R.drawable.ic_type_gallery
                AppType.FRONTEND -> R.drawable.ic_type_frontend
                AppType.WORDPRESS -> R.drawable.ic_type_wordpress
                AppType.NODEJS_APP -> R.drawable.ic_type_nodejs
                AppType.PHP_APP -> R.drawable.ic_type_php
                AppType.PYTHON_APP -> R.drawable.ic_type_python
                AppType.GO_APP -> R.drawable.ic_type_go
                AppType.MULTI_WEB -> R.drawable.ic_type_multi_web
            }
            Icon(
                painterResource(defaultIconRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 2 / 9),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
