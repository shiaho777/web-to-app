package com.webtoapp.ui.screens.create

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.host.HostRuntimePrefs
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.AppType
import com.webtoapp.ui.design.WtaAlpha
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.design.WtaShapePlate
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.icons.WtaAppTypeIcon
import com.webtoapp.ui.icons.WtaOfflinePackTypeIcon
import androidx.compose.material3.MaterialShapes
import androidx.graphics.shapes.RoundedPolygon

private val galleryShapes: List<RoundedPolygon> = listOf(
    MaterialShapes.Boom,
    MaterialShapes.Burst,
    MaterialShapes.SemiCircle,
    MaterialShapes.Oval,
    MaterialShapes.Square,
    MaterialShapes.Arrow
)

private data class GalleryType(
    val label: String,
    val description: String,
    val advanced: Boolean,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTypeGalleryScreen(
    onBack: () -> Unit,
    onCreateApp: () -> Unit,
    onCreateMultiWebApp: () -> Unit,
    onCreateHtmlApp: () -> Unit,
    onCreateOfflinePack: () -> Unit,
    onCreateFrontendApp: () -> Unit,
    onCreateGalleryApp: () -> Unit,
    onCreatePhpApp: () -> Unit,
    onCreateWordPressApp: () -> Unit,
    onCreateNodeJsApp: () -> Unit,
    onCreatePythonApp: () -> Unit,
    onCreateGoApp: () -> Unit,
    onCreateMediaApp: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    val advancedEnabled by HostRuntimePrefs.getInstance(context)
        .advancedFeaturesFlow
        .collectAsStateWithLifecycle()

    val types = listOf(
        GalleryType(Strings.appTypeWeb, Strings.typeDescWeb, false, { WtaAppTypeIcon(AppType.WEB, Modifier.size(34.dp)) }, onCreateApp),
        GalleryType(Strings.appTypeMultiWeb, Strings.typeDescMultiWeb, false, { WtaAppTypeIcon(AppType.MULTI_WEB, Modifier.size(34.dp)) }, onCreateMultiWebApp),
        GalleryType(Strings.appTypeHtml, Strings.typeDescHtml, false, { WtaAppTypeIcon(AppType.HTML, Modifier.size(34.dp)) }, onCreateHtmlApp),
        GalleryType(Strings.websiteOfflinePack, Strings.typeDescOfflinePack, false, { WtaOfflinePackTypeIcon(Modifier.size(34.dp)) }, onCreateOfflinePack),
        GalleryType(Strings.appTypeFrontend, Strings.typeDescFrontend, false, { WtaAppTypeIcon(AppType.FRONTEND, Modifier.size(34.dp)) }, onCreateFrontendApp),
        GalleryType(Strings.appTypeGallery, Strings.typeDescGallery, false, { WtaAppTypeIcon(AppType.GALLERY, Modifier.size(34.dp)) }, onCreateGalleryApp),
        GalleryType(Strings.appTypePhp, Strings.typeDescPhp, true, { WtaAppTypeIcon(AppType.PHP_APP, Modifier.size(34.dp)) }, onCreatePhpApp),
        GalleryType(Strings.appTypeWordPress, Strings.typeDescWordPress, true, { WtaAppTypeIcon(AppType.WORDPRESS, Modifier.size(34.dp)) }, onCreateWordPressApp),
        GalleryType(Strings.appTypeNodeJs, Strings.typeDescNodeJs, true, { WtaAppTypeIcon(AppType.NODEJS_APP, Modifier.size(34.dp)) }, onCreateNodeJsApp),
        GalleryType(Strings.appTypePython, Strings.typeDescPython, true, { WtaAppTypeIcon(AppType.PYTHON_APP, Modifier.size(34.dp)) }, onCreatePythonApp),
        GalleryType(Strings.appTypeGo, Strings.typeDescGo, true, { WtaAppTypeIcon(AppType.GO_APP, Modifier.size(34.dp)) }, onCreateGoApp),
        GalleryType(Strings.createMediaApp, Strings.typeDescMedia, true, { WtaAppTypeIcon(AppType.VIDEO, Modifier.size(34.dp)) }, onCreateMediaApp)
    )

    WtaScreen(title = Strings.createTypeGalleryTitle, onBack = onBack) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = WtaSpacing.ScreenHorizontal,
                vertical = WtaSpacing.ScreenVertical
            ),
            horizontalArrangement = Arrangement.spacedBy(WtaSpacing.CardGap),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.CardGap)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)) {
                    Text(
                        text = Strings.createTypeGallerySubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!advancedEnabled) {
                        Surface(
                            onClick = onOpenAbout,
                            shape = RoundedCornerShape(WtaRadius.Card),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = WtaSpacing.RowHorizontal, vertical = WtaSpacing.Small),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = Strings.createTypeAdvancedGuide,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            itemsIndexed(types) { index, type ->
                val locked = type.advanced && !advancedEnabled
                GalleryTypeCard(
                    type = type,
                    shape = galleryShapes[index % galleryShapes.size],
                    locked = locked,
                    onClick = if (locked) onOpenAbout else type.onClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryTypeCard(
    type: GalleryType,
    shape: RoundedPolygon,
    locked: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(WtaRadius.Card),
        color = colors.surfaceContainer,
        contentColor = colors.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (locked) WtaAlpha.Disabled else 1f)
                    .padding(horizontal = WtaSpacing.RowHorizontal, vertical = WtaSpacing.ContentGap),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                WtaShapePlate(
                    polygon = shape,
                    containerColor = colors.secondaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    type.icon()
                }
                Text(
                    text = type.label,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(32.dp)
                )
            }
            if (locked) {
                Surface(
                    shape = RoundedCornerShape(WtaRadius.Chip),
                    color = colors.surfaceContainerHighest,
                    contentColor = colors.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(WtaSpacing.Small)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = Strings.createTypeLocked,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}