package com.webtoapp.ui.shell

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.shell.ShellConfig

fun buildShellErrorReport(
    config: ShellConfig,
    mode: String,
    message: String?,
    throwable: Throwable?
): String {
    val sb = StringBuilder()
    sb.appendLine("===== WebToApp Error Report =====")
    sb.appendLine("app: ${config.appName}")
    sb.appendLine("package: ${config.packageName}")
    sb.appendLine("version: ${config.versionName} (${config.versionCode})")
    sb.appendLine("mode: $mode")
    sb.appendLine("appType: ${config.appType}")
    sb.appendLine("targetUrl: ${config.targetUrl}")
    sb.appendLine("htmlUsesFileScheme: ${config.htmlUsesFileScheme}")
    sb.appendLine("androidSdk: ${android.os.Build.VERSION.SDK_INT}")
    sb.appendLine("device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
    sb.appendLine("time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}")
    sb.appendLine("message: ${message ?: throwable?.message ?: "-"}")
    if (throwable != null) {
        sb.appendLine("exception: ${throwable.javaClass.name}")
        sb.appendLine("--- stack trace ---")
        sb.appendLine(android.util.Log.getStackTraceString(throwable).trimEnd())
    }
    sb.append("=================================")
    return sb.toString()
}

private fun copyErrorReport(context: Context, report: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("WebToApp Error", report))
    Toast.makeText(context, Strings.errorCopied, Toast.LENGTH_SHORT).show()
}

@Composable
fun ShellErrorScreen(
    config: ShellConfig,
    mode: String,
    message: String?,
    throwable: Throwable? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val report = remember(config, mode, message, throwable) {
        buildShellErrorReport(config, mode, message, throwable)
    }
    val displayMessage = message ?: throwable?.message ?: Strings.serverStartFailed

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = Strings.errorScreenTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = displayMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { copyErrorReport(context, report) }) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(Strings.errorCopyDetails)
            }
            if (onRetry != null) {
                OutlinedButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.errorRetry)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { expanded = !expanded }) {
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (expanded) Strings.errorHideDetails else Strings.errorShowDetails)
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = report,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                )
            }
        }
    }
}
