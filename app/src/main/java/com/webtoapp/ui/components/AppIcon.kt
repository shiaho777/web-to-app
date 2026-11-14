package com.webtoapp.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.webtoapp.data.model.WebApp
import com.webtoapp.ui.icons.WtaAppTypeIcon

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
            WtaAppTypeIcon(
                type = app.appType,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 2 / 9),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                accent = MaterialTheme.colorScheme.primary
            )
        }
    }
}
