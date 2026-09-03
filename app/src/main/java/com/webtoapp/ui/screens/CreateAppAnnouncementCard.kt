package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import com.webtoapp.ui.animation.CardExpandTransition
import com.webtoapp.ui.animation.CardCollapseTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.*
import com.webtoapp.ui.components.*
import com.webtoapp.ui.components.announcement.AnnouncementDialog
import com.webtoapp.ui.components.announcement.AnnouncementConfig
import com.webtoapp.ui.components.announcement.AnnouncementTemplateSelector
import com.webtoapp.ui.components.announcement.toStoredTemplate
import com.webtoapp.ui.components.announcement.toUiTemplate
import com.webtoapp.ui.design.*
import com.webtoapp.ui.viewmodel.EditState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementCard(
    editState: EditState,
    onEnabledChange: (Boolean) -> Unit,
    onAnnouncementChange: (Announcement) -> Unit
) {
    AnnouncementCard(
        enabled = editState.announcementEnabled,
        announcement = editState.announcement,
        onEnabledChange = onEnabledChange,
        onAnnouncementChange = onAnnouncementChange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementCard(
    enabled: Boolean,
    announcement: Announcement,
    onEnabledChange: (Boolean) -> Unit,
    onAnnouncementChange: (Announcement) -> Unit
) {
    var showPreview by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val iconPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            try {
                val savedPath = com.webtoapp.util.IconStorage.saveIconFromUri(context, it)
                if (savedPath != null) {
                    onAnnouncementChange(announcement.copy(customIconPath = savedPath))
                }
            } catch (_: Exception) {}
        }
    }

    val previewBitmap = remember(announcement.customIconPath) {
        announcement.customIconPath?.let { p ->
            try { android.graphics.BitmapFactory.decodeFile(p) } catch (e: Exception) { null }
        }
    }

    if (showPreview && (announcement.title.isNotBlank() || announcement.content.isNotBlank())) {
        AnnouncementDialog(
            config = AnnouncementConfig(
                announcement = announcement,
                template = announcement.template.toUiTemplate(),
                customIconBitmap = previewBitmap
            ),
            onDismiss = { showPreview = false },
            onLinkClick = {  }
        )
    }

    WtaSettingCard {
        WtaToggleRow(
            icon = Icons.Outlined.Campaign,
            title = Strings.popupAnnouncement,
            checked = enabled,
            onCheckedChange = onEnabledChange
        )

        AnimatedVisibility(
            visible = enabled,
            enter = CardExpandTransition,
            exit = CardCollapseTransition
        ) {
            Column {
                WtaSectionDivider()

                Column(
                    modifier = Modifier.padding(horizontal = WtaSpacing.RowHorizontal),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))

                    AnnouncementTemplateSelector(
                        selectedTemplate = announcement.template.toUiTemplate(),
                        onTemplateSelected = { template ->
                            onAnnouncementChange(
                                announcement.copy(
                                    template = template.toStoredTemplate()
                                )
                            )
                        }
                    )
                }

                WtaToggleRow(
                    icon = Icons.Outlined.Image,
                    title = Strings.announcementShowIcon,
                    subtitle = Strings.announcementShowIconHint,
                    checked = announcement.showIcon,
                    onCheckedChange = {
                        onAnnouncementChange(announcement.copy(showIcon = it))
                    }
                )

                AnimatedVisibility(visible = announcement.showIcon) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = WtaSpacing.RowHorizontal),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val bitmap = previewBitmap
                            if (bitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Campaign,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (announcement.customIconPath != null) {
                            androidx.compose.material3.OutlinedButton(
                                onClick = { iconPickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(Strings.announcementCustomIcon)
                            }
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    onAnnouncementChange(announcement.copy(customIconPath = null))
                                }
                            ) {
                                Text(
                                    Strings.announcementRemoveCustomIcon,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            androidx.compose.material3.OutlinedButton(
                                onClick = { iconPickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Outlined.AddPhotoAlternate, null, Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(Strings.announcementCustomIcon)
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = WtaSpacing.RowHorizontal),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    PremiumTextField(
                        value = announcement.title,
                        onValueChange = {
                            onAnnouncementChange(announcement.copy(title = it))
                        },
                        label = { Text(Strings.announcementTitle) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    PremiumTextField(
                        value = announcement.content,
                        onValueChange = {
                            onAnnouncementChange(announcement.copy(content = it))
                        },
                        label = { Text(Strings.announcementContent) },
                        supportingText = {
                            if (!announcement.contentIsHtml) {
                                Text(
                                    "${announcement.content.length}/500",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (announcement.content.length > 500)
                                        MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        minLines = 3,
                        maxLines = if (announcement.contentIsHtml) 10 else 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                WtaToggleRow(
                    icon = Icons.Outlined.Code,
                    title = Strings.announcementContentHtml,
                    subtitle = Strings.announcementContentHtmlDesc,
                    checked = announcement.contentIsHtml,
                    onCheckedChange = {
                        onAnnouncementChange(announcement.copy(contentIsHtml = it))
                    }
                )

                Column(
                    modifier = Modifier.padding(horizontal = WtaSpacing.RowHorizontal),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumTextField(
                        value = announcement.linkUrl ?: "",
                        onValueChange = {
                            onAnnouncementChange(announcement.copy(linkUrl = it.ifBlank { null }))
                        },
                        label = { Text(Strings.linkUrl) },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(
                        visible = !announcement.linkUrl.isNullOrBlank()
                    ) {
                        PremiumTextField(
                            value = announcement.linkText ?: "",
                            onValueChange = {
                                onAnnouncementChange(announcement.copy(linkText = it.ifBlank { null }))
                            },
                            label = { Text(Strings.linkButtonText) },
                            placeholder = { Text(Strings.viewDetails) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    PremiumOutlinedButton(
                        onClick = { showPreview = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = announcement.title.isNotBlank() || announcement.content.isNotBlank()
                    ) {
                        Icon(Icons.Outlined.Preview, null, Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.previewAnnouncementEffect)
                    }

                    Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))
                }

                WtaSectionDivider()

                AnnouncementTriggerSection(
                    announcement = announcement,
                    onAnnouncementChange = onAnnouncementChange
                )

                WtaSectionDivider()

                AnnouncementAdvancedSection(
                    announcement = announcement,
                    onAnnouncementChange = onAnnouncementChange
                )

                Spacer(modifier = Modifier.height(WtaSpacing.ContentGap))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementTriggerSection(
    announcement: Announcement,
    onAnnouncementChange: (Announcement) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    WtaChoiceRow(
        title = Strings.announcementTriggerSettings,
        icon = Icons.Outlined.AccessTime,
        value = "",
        isExpanded = expanded,
        onClick = { expanded = !expanded }
    )

    AnimatedVisibility(
        visible = expanded,
        enter = CardExpandTransition,
        exit = CardCollapseTransition
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            WtaToggleRow(
                title = Strings.announcementTriggerOnLaunch,
                subtitle = Strings.announcementTriggerOnLaunchHint,
                icon = Icons.Outlined.RocketLaunch,
                checked = announcement.triggerOnLaunch,
                onCheckedChange = {
                    onAnnouncementChange(announcement.copy(triggerOnLaunch = it))
                }
            )

            WtaToggleRow(
                title = Strings.announcementTriggerOnNoNetwork,
                subtitle = Strings.announcementTriggerOnNoNetworkHint,
                icon = Icons.Outlined.CloudOff,
                checked = announcement.triggerOnNoNetwork,
                onCheckedChange = {
                    onAnnouncementChange(announcement.copy(triggerOnNoNetwork = it))
                }
            )

            var intervalExpanded by remember { mutableStateOf(false) }
            val intervalOptions = listOf(0, 1, 3, 5, 10, 15, 30, 60)

            WtaSettingRow(
                title = Strings.announcementTriggerInterval,
                subtitle = Strings.announcementTriggerIntervalHint,
                icon = Icons.Outlined.Timer
            ) {
                ExposedDropdownMenuBox(
                    expanded = intervalExpanded,
                    onExpandedChange = { intervalExpanded = it },
                    modifier = Modifier.width(110.dp)
                ) {
                    FilledTonalButton(
                        onClick = { intervalExpanded = true },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(WtaRadius.Button)
                    ) {
                        Text(
                            if (announcement.triggerIntervalMinutes == 0)
                                Strings.announcementIntervalDisabled
                            else
                                "${announcement.triggerIntervalMinutes} ${Strings.minutesShort}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded)
                    }
                    ExposedDropdownMenu(
                        expanded = intervalExpanded,
                        onDismissRequest = { intervalExpanded = false }
                    ) {
                        intervalOptions.forEach { interval ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (interval == announcement.triggerIntervalMinutes) {
                                            Icon(
                                                Icons.Filled.Check, null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(
                                            if (interval == 0) Strings.announcementIntervalDisabled
                                            else "$interval ${Strings.minutesShort}"
                                        )
                                    }
                                },
                                onClick = {
                                    onAnnouncementChange(announcement.copy(triggerIntervalMinutes = interval))
                                    intervalExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = announcement.triggerIntervalMinutes > 0
            ) {
                WtaToggleRow(
                    title = Strings.announcementTriggerIntervalIncludeLaunch,
                    icon = Icons.Outlined.PlayCircle,
                    checked = announcement.triggerIntervalIncludeLaunch,
                    onCheckedChange = {
                        onAnnouncementChange(announcement.copy(triggerIntervalIncludeLaunch = it))
                    }
                )
            }

            WtaSectionDivider()

            Text(
                Strings.displayFrequency,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    start = WtaSpacing.RowHorizontal,
                    top = WtaSpacing.ContentGap,
                    bottom = WtaSpacing.ContentGap
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WtaSpacing.RowHorizontal),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
            ) {
                WtaChip(
                    selected = announcement.showOnce,
                    onClick = { onAnnouncementChange(announcement.copy(showOnce = true)) },
                    label = Strings.showOnce,
                    showSelectedCheck = false
                )
                WtaChip(
                    selected = !announcement.showOnce,
                    onClick = { onAnnouncementChange(announcement.copy(showOnce = false)) },
                    label = Strings.everyLaunch,
                    showSelectedCheck = false
                )
            }
        }
    }
}

@Composable
private fun AnnouncementAdvancedSection(
    announcement: Announcement,
    onAnnouncementChange: (Announcement) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    WtaChoiceRow(
        title = Strings.announcementAdvancedOptions,
        icon = Icons.Outlined.Tune,
        value = "",
        isExpanded = expanded,
        onClick = { expanded = !expanded }
    )

    AnimatedVisibility(
        visible = expanded,
        enter = CardExpandTransition,
        exit = CardCollapseTransition
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            WtaToggleRow(
                title = Strings.announcementRequireConfirmLabel,
                subtitle = Strings.announcementRequireConfirmHint,
                icon = Icons.Outlined.TaskAlt,
                checked = announcement.requireConfirmation,
                onCheckedChange = {
                    onAnnouncementChange(announcement.copy(requireConfirmation = it))
                }
            )

            WtaToggleRow(
                title = Strings.announcementAllowNeverShowLabel,
                subtitle = Strings.announcementAllowNeverShowHint,
                icon = Icons.Outlined.VisibilityOff,
                checked = announcement.allowNeverShow,
                onCheckedChange = {
                    onAnnouncementChange(announcement.copy(allowNeverShow = it))
                }
            )
        }
    }
}
