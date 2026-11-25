package com.webtoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.webtoapp.data.model.WebApp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaSegmentedRow
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.icons.WtaAppTypeIcon

@Composable
fun WtaAppIcon(
    app: WebApp,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val iconPath = app.iconPath
    if (iconPath != null) {
        AppIconImage(
            model = iconPath,
            contentDescription = app.name,
            modifier = modifier.size(size)
        )
    } else {
        Surface(
            modifier = modifier.size(size),
            shape = RoundedCornerShape(size / 4.5f),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            WtaAppTypeIcon(
                type = app.appType,
                modifier = Modifier.fillMaxSize().padding(size * 2 / 9),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun AppIconImage(
    model: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    crossfade: Boolean = false
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .crossfade(crossfade)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun AppIconStyleSelector(
    preserveIconShape: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)) {
        Text(
            text = Strings.appIconStyle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        WtaSegmentedRow(
            options = listOf(Strings.adaptiveAppIcon, Strings.preserveAppIcon),
            selectedIndex = if (preserveIconShape) 1 else 0,
            onSelect = { onChange(it == 1) },
            enabled = enabled
        )
    }
}
