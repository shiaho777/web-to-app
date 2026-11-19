package com.webtoapp.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.webtoapp.core.apkbuilder.ApkInstaller
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlertDialog

@Composable
fun InstallPermissionDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    WtaAlertDialog(
        onDismissRequest = onDismiss,
        title = Strings.installPermissionTitle,
        text = Strings.installPermissionMessage,
        confirmButton = {
            TextButton(onClick = {
                ApkInstaller.openUnknownSourcesSettings(context.applicationContext)
                onDismiss()
            }) {
                Text(Strings.shortcutPermissionGoToSettings)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.close)
            }
        }
    )
}
