package com.webtoapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import com.webtoapp.core.activation.RemainingDaysNotice
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlertDialog

@Composable
fun RemainingDaysNoticeDialog(
    notice: RemainingDaysNotice,
    onDismiss: () -> Unit,
) {
    WtaAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Outlined.Timer,
        title = Strings.remainingDaysOnLaunchTitle,
        text = Strings.remainingDaysOnLaunchMessage(notice.days, notice.hours),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = Strings.gotIt,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
