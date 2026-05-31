package com.webtoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.webtoapp.core.i18n.Strings

fun buildErrorReport(
    scope: String,
    message: String?,
    throwable: Throwable? = null,
    contextLines: List<String> = emptyList()
): String {
    val sb = StringBuilder()
    sb.appendLine("===== WebToApp Error Report =====")
    sb.appendLine("scope: $scope")
    sb.appendLine("androidSdk: ${android.os.Build.VERSION.SDK_INT}")
    sb.appendLine("device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
    sb.appendLine("time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}")
    if (contextLines.isNotEmpty()) {
        contextLines.forEach { sb.appendLine(it) }
    }
    sb.appendLine("message: ${message ?: throwable?.message ?: "-"}")
    if (throwable != null) {
        sb.appendLine("exception: ${throwable.javaClass.name}")
        sb.appendLine("--- stack trace ---")
        sb.appendLine(android.util.Log.getStackTraceString(throwable).trimEnd())
    }
    sb.append("=================================")
    return sb.toString()
}

@Composable
fun WtaErrorDetailsSection(
    report: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    clipboard.setText(AnnotatedString(report))
                    Toast.makeText(context, Strings.errorCopied, Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(Strings.errorCopyDetails)
            }
            if (onRetry != null) {
                OutlinedButton(onClick = onRetry) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.errorRetry)
                }
            }
        }
        TextButton(onClick = { expanded = !expanded }) {
            Icon(
                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (expanded) Strings.errorHideDetails else Strings.errorShowDetails)
        }
        AnimatedVisibility(visible = expanded) {
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

@Composable
fun WtaErrorView(
    scope: String,
    message: String?,
    throwable: Throwable? = null,
    contextLines: List<String> = emptyList(),
    title: String = Strings.errorScreenTitle,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val report = remember(scope, message, throwable, contextLines) {
        buildErrorReport(scope, message, throwable, contextLines)
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
            Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = displayMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        WtaErrorDetailsSection(
            report = report,
            modifier = Modifier.fillMaxWidth(),
            onRetry = onRetry
        )
    }
}
