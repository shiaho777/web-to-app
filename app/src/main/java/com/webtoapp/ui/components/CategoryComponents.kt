package com.webtoapp.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.data.model.AppCategory
import com.webtoapp.data.model.WebApp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryTabRow(
    categories: List<AppCategory>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (AppCategory) -> Unit,
    onDeleteCategory: (AppCategory) -> Unit,
    onMoveCategory: (AppCategory, Int) -> Unit,
    onManageCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategoryMenu by remember { mutableStateOf<AppCategory?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<AppCategory?>(null) }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        item {
            com.webtoapp.ui.design.WtaChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = Strings.allApps,
                showSelectedCheck = false
            )
        }

        item {
            com.webtoapp.ui.design.WtaChip(
                selected = selectedCategoryId == -1L,
                onClick = { onCategorySelected(-1L) },
                label = Strings.uncategorized,
                showSelectedCheck = false
            )
        }

        itemsIndexed(categories, key = { _, it -> it.id }) { index, category ->
            Box {
                com.webtoapp.ui.design.WtaChip(
                    selected = selectedCategoryId == category.id,
                    onClick = { onCategorySelected(category.id) },
                    onLongClick = { showCategoryMenu = category },
                    showSelectedCheck = false,
                    leadingIcon = com.webtoapp.util.SvgIconMapper.getIcon(category.icon)
                ) {
                    Text(category.name, style = MaterialTheme.typography.labelLarge)
                }

                DropdownMenu(
                    expanded = showCategoryMenu == category,
                    onDismissRequest = { showCategoryMenu = null }
                ) {
                    DropdownMenuItem(
                        text = { Text(Strings.moveUp) },
                        onClick = {
                            showCategoryMenu = null
                            onMoveCategory(category, -1)
                        },
                        enabled = index > 0,
                        leadingIcon = { Icon(Icons.Outlined.KeyboardArrowUp, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(Strings.moveDown) },
                        onClick = {
                            showCategoryMenu = null
                            onMoveCategory(category, 1)
                        },
                        enabled = index < categories.lastIndex,
                        leadingIcon = { Icon(Icons.Outlined.KeyboardArrowDown, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(Strings.editCategory) },
                        onClick = {
                            showCategoryMenu = null
                            onEditCategory(category)
                        },
                        leadingIcon = { Icon(Icons.Outlined.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(Strings.deleteCategory) },
                        onClick = {
                            showCategoryMenu = null
                            showDeleteConfirm = category
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }

        item {

            Row(
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(com.webtoapp.ui.design.WtaRadius.Chip))
                    .clickable(onClick = onAddCategory)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    Strings.addCategory,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            IconButton(
                onClick = onManageCategories,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Outlined.Tune,
                    contentDescription = Strings.manageCategories,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    showDeleteConfirm?.let { category ->
        com.webtoapp.ui.design.WtaAlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            icon = Icons.Outlined.Delete,
            iconTint = MaterialTheme.colorScheme.error,
            title = Strings.deleteCategory,
            text = Strings.deleteCategoryConfirm,
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategory(category)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.btnDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(
    category: AppCategory?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String) -> Unit
) {
    var name by remember(category) { mutableStateOf(category?.name ?: "") }
    var icon by remember(category) { mutableStateOf(category?.icon ?: "folder") }

    val iconGroups = listOf(
        Strings.iconGroupCommon to listOf("folder", "star", "heart", "fire", "lightbulb", "auto_awesome"),
        Strings.iconGroupMedia to listOf("music", "movie", "tv", "camera", "image", "newspaper"),
        Strings.iconGroupWorkStudy to listOf("menu_book", "work", "computer", "phone_android", "edit_note", "analytics"),
        Strings.iconGroupLifeTravel to listOf("home", "directions_car", "flight", "directions_boat", "public", "explore"),
        Strings.iconGroupTools to listOf("settings", "wrench", "code", "robot", "extension", "shield"),
        Strings.iconGroupFun to listOf("gaming", "gift", "celebration", "science", "cocktail", "cat")
    )

    com.webtoapp.ui.design.WtaAlertDialog(
        onDismissRequest = onDismiss,
        icon = if (category == null) Icons.Default.Add else Icons.Outlined.Edit,
        title = if (category == null) Strings.addCategory else Strings.editCategory,
        content = {
            PremiumTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(Strings.categoryName) },
                placeholder = { Text(Strings.categoryNamePlaceholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                Strings.categoryIcon,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                iconGroups.forEach { (groupLabel, groupIcons) ->
                    Text(
                        groupLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupIcons.forEach { presetIcon ->
                            val isSelected = icon == presetIcon
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(com.webtoapp.ui.design.WtaRadius.IconPlate))
                                    .background(
                                        if (isSelected)
                                            MaterialTheme.colorScheme.primary.copy(alpha = com.webtoapp.ui.design.WtaAlpha.MutedContainer)
                                        else
                                            MaterialTheme.colorScheme.surfaceContainer
                                    )
                                    .clickable { icon = presetIcon },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isSelected)
                                        com.webtoapp.util.SvgIconMapper.getFilledIcon(presetIcon)
                                    else
                                        com.webtoapp.util.SvgIconMapper.getIcon(presetIcon),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            com.webtoapp.ui.design.WtaButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, icon)
                    }
                },
                text = Strings.btnSave,
                enabled = name.isNotBlank(),
                variant = com.webtoapp.ui.design.WtaButtonVariant.Primary,
                size = com.webtoapp.ui.design.WtaButtonSize.Small
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.btnCancel)
            }
        }
    )
}

@Composable
fun MoveToCategoryDialog(
    appName: String,
    currentCategoryId: Long?,
    categories: List<AppCategory>,
    onDismiss: () -> Unit,
    onMoveToCategory: (Long?) -> Unit
) {
    com.webtoapp.ui.design.WtaAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.FolderOpen,
        title = Strings.moveToCategory,
        content = {
            Text(
                appName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            com.webtoapp.ui.design.WtaSettingCard {
                CategoryOptionRow(
                    icon = Icons.Outlined.FolderOpen,
                    name = Strings.uncategorized,
                    selected = currentCategoryId == null,
                    onClick = { onMoveToCategory(null) }
                )
                categories.forEach { category ->
                    com.webtoapp.ui.design.WtaSectionDivider()
                    CategoryOptionRow(
                        icon = com.webtoapp.util.SvgIconMapper.getIcon(category.icon),
                        name = category.name,
                        selected = currentCategoryId == category.id,
                        onClick = { onMoveToCategory(category.id) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.btnCancel)
            }
        }
    )
}

@Composable
private fun CategoryOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    com.webtoapp.ui.design.WtaSettingRow(
        title = name,
        icon = icon,
        onClick = onClick
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CategoryManageDialog(
    categories: List<AppCategory>,
    onDismiss: () -> Unit,
    onMoveCategory: (AppCategory, Int) -> Unit,
    onEditCategory: (AppCategory) -> Unit,
    onDeleteCategory: (AppCategory) -> Unit
) {
    var pendingDelete by remember { mutableStateOf<AppCategory?>(null) }

    com.webtoapp.ui.design.WtaAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.Tune,
        title = Strings.manageCategories,
        content = {
            if (categories.isEmpty()) {
                Text(
                    Strings.categoriesEmptyHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    com.webtoapp.ui.design.WtaSettingCard {
                        categories.forEachIndexed { index, category ->
                            if (index > 0) com.webtoapp.ui.design.WtaSectionDivider()
                            com.webtoapp.ui.design.WtaSettingRow(
                                title = category.name,
                                icon = com.webtoapp.util.SvgIconMapper.getIcon(category.icon),
                                onClick = { onEditCategory(category) }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onMoveCategory(category, -1) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.KeyboardArrowUp,
                                            contentDescription = Strings.moveUp,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (index > 0) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onMoveCategory(category, 1) },
                                        enabled = index < categories.lastIndex,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.KeyboardArrowDown,
                                            contentDescription = Strings.moveDown,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (index < categories.lastIndex) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onEditCategory(category) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = Strings.editCategory,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { pendingDelete = category },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = Strings.deleteCategory,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.done)
            }
        }
    )

    pendingDelete?.let { category ->
        com.webtoapp.ui.design.WtaAlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = Icons.Outlined.Delete,
            iconTint = MaterialTheme.colorScheme.error,
            title = Strings.deleteCategory,
            text = Strings.deleteCategoryConfirm,
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategory(category)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(Strings.btnDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(Strings.btnCancel)
                }
            }
        )
    }
}
