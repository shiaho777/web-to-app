package com.webtoapp.ui.agent.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaExpandIn
import com.webtoapp.ui.design.WtaFadeIn
import com.webtoapp.ui.design.WtaFadeOut
import com.webtoapp.ui.design.WtaIconButton
import com.webtoapp.ui.design.WtaMotion
import com.webtoapp.ui.design.WtaRadius
import com.webtoapp.ui.design.WtaSelectableRow
import com.webtoapp.ui.design.WtaShrinkOut
import com.webtoapp.ui.design.WtaSize
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaTextField
import com.webtoapp.ui.theme.LocalAnimationSettings
import kotlinx.coroutines.launch

@Composable
internal fun AgentPanel(
    title: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    closeLabel: String = Strings.close,
    heightFraction: Float? = 0.86f,
    dismissEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = { if (dismissEnabled) onDismiss() },
        sheetGesturesEnabled = dismissEnabled,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = dismissEnabled,
            shouldDismissOnClickOutside = dismissEnabled
        ),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = WtaRadius.Dialog, topEnd = WtaRadius.Dialog)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (heightFraction == null) Modifier else Modifier.fillMaxHeight(heightFraction))
                .padding(horizontal = WtaSpacing.ScreenHorizontal)
                .padding(bottom = WtaSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(WtaSpacing.Small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WtaSpacing.Small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(WtaSize.IconPlate).background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(WtaRadius.IconPlate)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(WtaSize.Icon))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).semantics { heading() }
                )
                WtaIconButton(
                    icon = Icons.Outlined.Close,
                    contentDescription = closeLabel,
                    enabled = dismissEnabled,
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            if (!sheetState.isVisible) onDismiss()
                        }
                    }
                )
            }
            content()
        }
    }
}

@Composable
internal fun AgentSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = Strings.search
) {
    val keyboard = LocalSoftwareKeyboardController.current
    WtaTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = Icons.Outlined.Search,
        trailingIcon = if (value.isEmpty()) null else {
            {
                WtaIconButton(
                    onClick = { onValueChange("") },
                    icon = Icons.Outlined.Close,
                    contentDescription = Strings.clear
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun AgentSelectionRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        animationSpec = WtaMotion.standardTween(),
        label = "row selection"
    )
    WtaSelectableRow(
        title = title,
        subtitle = subtitle,
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .heightIn(min = WtaSize.TouchTarget)
            .background(background, RoundedCornerShape(WtaRadius.Control))
            .semantics { this.selected = selected }
    )
}

@Composable
internal fun AgentDisclosureIcon(expanded: Boolean, modifier: Modifier = Modifier) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = WtaMotion.standardTween(),
        label = "disclosure"
    )
    Icon(
        imageVector = Icons.Outlined.ExpandMore,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.size(WtaSize.IconSmall).graphicsLayer { rotationZ = rotation }
    )
}

@Composable
internal fun AgentReveal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animate = LocalAnimationSettings.current.enabled
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (animate) WtaExpandIn else WtaFadeIn,
        exit = if (animate) WtaShrinkOut else WtaFadeOut
    ) {
        content()
    }
}
