package com.webtoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.i18n.LanguageManager
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaShapePlate
import com.webtoapp.util.isRunningOnTv
import kotlinx.coroutines.launch

private val languageBadgeShapes = listOf(
    MaterialShapes.Sunny,
    MaterialShapes.Cookie4Sided,
    MaterialShapes.Clover4Leaf,
    MaterialShapes.Gem,
    MaterialShapes.Fan,
    MaterialShapes.Flower,
    MaterialShapes.Pentagon,
    MaterialShapes.Heart,
    MaterialShapes.Cookie6Sided,
    MaterialShapes.VerySunny
)

private fun AppLanguage.monogram(): String = when (this) {
    AppLanguage.CHINESE -> "中"
    AppLanguage.ENGLISH -> "En"
    AppLanguage.ARABIC -> "ع"
    AppLanguage.PORTUGUESE -> "Pt"
    AppLanguage.SPANISH -> "Es"
    AppLanguage.FRENCH -> "Fr"
    AppLanguage.GERMAN -> "De"
    AppLanguage.RUSSIAN -> "Ру"
    AppLanguage.JAPANESE -> "あ"
    AppLanguage.KOREAN -> "한"
}

@Composable
private fun LanguageDisplayNameWithBadge(
    language: AppLanguage,
    isSelected: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = language.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        if (language.translationInProgress) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = com.webtoapp.core.i18n.Strings.translationInProgressBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun LanguageSelectorButton(
    onLanguageChanged: () -> Unit = {},
    buttonSize: androidx.compose.ui.unit.Dp = 44.dp
) {
    val context = LocalContext.current
    val languageManager = remember { LanguageManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    val currentLanguage by languageManager.currentLanguageFlow.collectAsState(initial = AppLanguage.CHINESE)
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showDialog = true },
        modifier = Modifier.size(buttonSize)
    ) {
        Icon(
            imageVector = Icons.Outlined.Language,
            contentDescription = Strings.languageSettings,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { language ->
                scope.launch {
                    languageManager.setLanguage(language)
                    onLanguageChanged()
                }
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val maxListHeight = (LocalConfiguration.current.screenHeightDp * 0.55f).dp

    WtaAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.Translate,
        title = Strings.languageSettings,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxListHeight)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.entries.forEachIndexed { index, language ->
                    LanguageOption(
                        language = language,
                        isSelected = language == currentLanguage,
                        shapeIndex = index,
                        onClick = { onLanguageSelected(language) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.btnCancel)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LanguageOption(
    language: AppLanguage,
    isSelected: Boolean,
    shapeIndex: Int,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val badgeContainer: androidx.compose.ui.graphics.Color
    val badgeOn: androidx.compose.ui.graphics.Color
    when (shapeIndex % 3) {
        0 -> {
            badgeContainer = colors.primaryContainer
            badgeOn = colors.onPrimaryContainer
        }
        1 -> {
            badgeContainer = colors.secondaryContainer
            badgeOn = colors.onSecondaryContainer
        }
        else -> {
            badgeContainer = colors.tertiaryContainer
            badgeOn = colors.onTertiaryContainer
        }
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WtaRadius.Card),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                colors.primaryContainer
            } else {
                colors.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WtaShapePlate(
                polygon = languageBadgeShapes[shapeIndex % languageBadgeShapes.size],
                containerColor = colors.surface,
                modifier = Modifier.size(46.dp)
            ) {
                WtaShapePlate(
                    polygon = languageBadgeShapes[shapeIndex % languageBadgeShapes.size],
                    containerColor = badgeContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Text(
                        text = language.monogram(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeOn
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.nativeName,
                    style = if (isSelected) {
                        MaterialTheme.typography.titleMediumEmphasized
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = if (isSelected) {
                        colors.onPrimaryContainer
                    } else {
                        colors.onSurface
                    }
                )
                LanguageDisplayNameWithBadge(language = language, isSelected = isSelected)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSelected,
                    enter = androidx.compose.animation.fadeIn() +
                        androidx.compose.animation.scaleIn(initialScale = 0.5f),
                    exit = androidx.compose.animation.fadeOut() +
                        androidx.compose.animation.scaleOut(targetScale = 0.5f)
                ) {
                    WtaShapePlate(
                        polygon = MaterialShapes.Cookie4Sided,
                        containerColor = colors.primary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.onPrimary
                        )
                    }
                }
                if (!isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceContainerHighest)
                    )
                }
            }
        }
    }
}

@Composable
fun FirstLaunchLanguageScreen(
    onLanguageSelected: () -> Unit
) {
    val context = LocalContext.current
    val languageManager = remember { LanguageManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    var selectedLanguage by remember { mutableStateOf<AppLanguage?>(null) }
    val scrollState = rememberScrollState()
    val isTv = isRunningOnTv()
    val confirmFocusRequester = remember { FocusRequester() }

    val iconSize = if (isTv) 48.dp else 72.dp
    val topSpacing = if (isTv) 12.dp else 24.dp
    val sectionSpacing = if (isTv) 16.dp else 32.dp
    val cardSpacing = if (isTv) 8.dp else 12.dp
    val horizontalPadding = if (isTv) 48.dp else 24.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = horizontalPadding, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            if (isTv) {
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .size(iconSize + 24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(topSpacing))

            val welcomeHeadlineStyle = if (isTv) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.headlineMedium
            }
            Text(
                text = when (selectedLanguage) {
                    AppLanguage.CHINESE -> "欢迎"
                    AppLanguage.ENGLISH -> "Welcome"
                    AppLanguage.ARABIC -> "مرحبا"
                    AppLanguage.PORTUGUESE -> "Bem-vindo"
                    AppLanguage.SPANISH -> "Bienvenido"
                    AppLanguage.FRENCH -> "Bienvenue"
                    AppLanguage.GERMAN -> "Willkommen"
                    AppLanguage.RUSSIAN -> "Добро пожаловать"
                    AppLanguage.JAPANESE -> "ようこそ"
                    AppLanguage.KOREAN -> "환영합니다"
                    null -> "Welcome / 欢迎 / مرحبا"
                },
                style = welcomeHeadlineStyle,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (selectedLanguage) {
                    AppLanguage.CHINESE -> "选择语言"
                    AppLanguage.ENGLISH -> "Select Language"
                    AppLanguage.ARABIC -> "اختر اللغة"
                    AppLanguage.PORTUGUESE -> "Selecione o Idioma"
                    AppLanguage.SPANISH -> "Selecciona el Idioma"
                    AppLanguage.FRENCH -> "Sélectionner la Langue"
                    AppLanguage.GERMAN -> "Sprache Auswählen"
                    AppLanguage.RUSSIAN -> "Выберите Язык"
                    AppLanguage.JAPANESE -> "言語を選択"
                    AppLanguage.KOREAN -> "언어 선택"
                    null -> "Select Language / 选择语言 / اختر اللغة"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(sectionSpacing))

            Column(
                modifier = Modifier
                    .then(if (isTv) Modifier.widthIn(max = 500.dp) else Modifier.fillMaxWidth()),
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                AppLanguage.entries.forEach { language ->
                    FirstLaunchLanguageOption(
                        language = language,
                        isSelected = language == selectedLanguage,
                        onClick = { selectedLanguage = language },
                        isTv = isTv
                    )
                }
            }

            Spacer(modifier = Modifier.height(sectionSpacing))

            PremiumButton(
                onClick = {
                    selectedLanguage?.let { lang ->
                        scope.launch {
                            languageManager.setLanguage(lang)
                            onLanguageSelected()
                        }
                    }
                },
                enabled = selectedLanguage != null,
                modifier = Modifier
                    .then(if (isTv) Modifier.widthIn(max = 500.dp).fillMaxWidth() else Modifier.fillMaxWidth())
                    .height(56.dp)
                    .focusRequester(confirmFocusRequester)
                    .focusable()
            ) {
                Text(
                    text = when (selectedLanguage) {
                        AppLanguage.CHINESE -> "确认"
                        AppLanguage.ENGLISH -> "Confirm"
                        AppLanguage.ARABIC -> "تأكيد"
                        AppLanguage.PORTUGUESE -> "Confirmar"
                        AppLanguage.SPANISH -> "Confirmar"
                        AppLanguage.FRENCH -> "Confirmer"
                        AppLanguage.GERMAN -> "Bestätigen"
                        AppLanguage.RUSSIAN -> "Подтвердить"
                        AppLanguage.JAPANESE -> "確認"
                        AppLanguage.KOREAN -> "확인"
                        null -> "Confirm / 确认 / تأكيد"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FirstLaunchLanguageOption(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
    isTv: Boolean = false
) {
    val borderBrush = if (isSelected) {
        androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
    } else {
        androidx.compose.ui.graphics.SolidColor(
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .focusable(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = borderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTv) 14.dp else 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                LanguageDisplayNameWithBadge(language = language, isSelected = isSelected)
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = androidx.compose.animation.fadeIn() +
                    androidx.compose.animation.scaleIn(initialScale = 0.6f),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsCard(
    onLanguageChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val languageManager = remember { LanguageManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    val currentLanguage by languageManager.currentLanguageFlow.collectAsState(initial = AppLanguage.CHINESE)
    var expanded by remember { mutableStateOf(false) }

    EnhancedElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = Strings.languageSettings,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = currentLanguage.nativeName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    AppLanguage.entries.forEach { language ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(language.nativeName)
                                    Text(
                                        text = language.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                scope.launch {
                                    languageManager.setLanguage(language)
                                    onLanguageChanged()
                                }
                                expanded = false
                            },
                            leadingIcon = {
                                if (language == currentLanguage) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
