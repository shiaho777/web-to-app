package com.webtoapp.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.webtoapp.core.apkbuilder.ApkTemplate
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaChip
import com.webtoapp.ui.theme.ifDescriptionsShown
import com.webtoapp.util.FaviconFetcher
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconPickerWithLibrary(
    iconUri: Uri? = null,
    iconPath: String? = null,
    websiteUrl: String? = null,
    iconBackgroundColor: String? = null,
    onIconBackgroundColorChange: ((String?) -> Unit)? = null,
    onSelectFromGallery: () -> Unit,
    onSelectFromLibrary: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showLibraryDialog by remember { mutableStateOf(false) }
    var showAiGeneratorDialog by remember { mutableStateOf(false) }
    var showBackgroundPicker by remember { mutableStateOf(false) }
    var isFetchingFavicon by remember { mutableStateOf(false) }

    val hasIcon = iconUri != null || iconPath != null
    val transparentPlate = iconBackgroundColor.equals("#00000000", ignoreCase = true)
    val plateColor = remember(iconBackgroundColor) {
        iconBackgroundColor?.let { parseColor(it) } ?: Color.White
    }
    val plateFill = if (transparentPlate) {
        Modifier.background(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color.White, Color.LightGray)
            )
        )
    } else {
        Modifier.background(plateColor)
    }

    val canFetchFavicon = !websiteUrl.isNullOrBlank() &&
        (websiteUrl.contains(".") || websiteUrl.startsWith("http"))

    Column(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier
                .size(72.dp)
                .clip(MaterialTheme.shapes.medium)
                .then(if (hasIcon) plateFill else Modifier)
                .border(
                    width = 2.dp,
                    color = if (hasIcon) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.medium
                )
                .clickable { onSelectFromGallery() },
            color = if (hasIcon) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant
        ) {
            when {
                iconUri != null -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(iconUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = Strings.labelIcon,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                iconPath != null -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(iconPath))
                            .crossfade(true)
                            .build(),
                        contentDescription = Strings.labelIcon,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                else -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Outlined.AddPhotoAlternate,
                            contentDescription = Strings.selectIcon,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
            Text(
                text = Strings.labelIcon,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = Strings.clickToSelectOrUseButton,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (canFetchFavicon) {
                    FilledTonalButton(
                        onClick = {
                            if (!isFetchingFavicon && !websiteUrl.isNullOrBlank()) {
                                isFetchingFavicon = true
                                scope.launch {
                                    val iconPath = FaviconFetcher.fetchFavicon(context, websiteUrl)
                                    isFetchingFavicon = false
                                    if (iconPath != null) {
                                        onSelectFromLibrary(iconPath)
                                        Toast.makeText(context, Strings.faviconFetchSuccess, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, Strings.faviconFetchFailed, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = !isFetchingFavicon,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        if (isFetchingFavicon) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Language,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.fetchWebsiteIcon, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                FilledTonalButton(
                    onClick = { showLibraryDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Collections,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.iconLibrary, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }

    if (onIconBackgroundColorChange != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = Strings.iconBackground,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Strings.iconBackgroundDesc.ifDescriptionsShown()?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WtaChip(
                    selected = iconBackgroundColor == null,
                    onClick = { onIconBackgroundColorChange(null) },
                    label = Strings.tagAuto,
                    showSelectedCheck = false
                )
                WtaChip(
                    selected = iconBackgroundColor != null,
                    onClick = { showBackgroundPicker = true },
                    label = iconBackgroundColor?.uppercase() ?: Strings.backgroundColor,
                    showSelectedCheck = false,
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .then(plateFill)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        )
                    }
                )
            }
        }
    }
    }

    if (showBackgroundPicker && onIconBackgroundColorChange != null) {
        ColorPickerDialog(
            currentColor = iconBackgroundColor ?: "#FFFFFF",
            onColorSelected = { picked ->
                onIconBackgroundColorChange(ApkTemplate.normalizeIconBackgroundColor(picked))
                showBackgroundPicker = false
            },
            onDismiss = { showBackgroundPicker = false }
        )
    }

    if (showLibraryDialog) {
        IconLibraryDialog(
            onDismiss = { showLibraryDialog = false },
            onSelectIcon = { path ->
                onSelectFromLibrary(path)
                showLibraryDialog = false
            },
            onOpenAiGenerator = { showAiGeneratorDialog = true }
        )
    }

    if (showAiGeneratorDialog) {
        IconGeneratorDialog(
            onDismiss = { showAiGeneratorDialog = false },
            onIconGenerated = { path ->
                onSelectFromLibrary(path)
                showAiGeneratorDialog = false
            }
        )
    }
}
