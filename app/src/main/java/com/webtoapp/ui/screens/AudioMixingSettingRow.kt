package com.webtoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.webtoapp.core.engine.EngineType
import com.webtoapp.core.engine.download.EngineFileManager
import com.webtoapp.core.engine.download.GeckoEngineDownloader
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.design.WtaSpacing
import com.webtoapp.ui.design.WtaToggleRow
import com.webtoapp.ui.theme.ifDescriptionsShown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun AudioMixingSettingRow(
    engine: EngineType,
    echEnabled: Boolean,
    onEngineSelected: (EngineType) -> Unit,
    onOpenBrowserKernel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val geckoAvailable by produceState<Boolean?>(null, context, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            value = withContext(Dispatchers.IO) {
                val files = EngineFileManager(context)
                files.isEngineDownloaded(EngineType.GECKOVIEW, GeckoEngineDownloader.DEFAULT_VERSION) &&
                    files.isAbiDownloaded(EngineType.GECKOVIEW, files.getDevicePrimaryAbi())
            }
        }
    }
    var pendingEngine by remember { mutableStateOf<EngineType?>(null) }
    val usesGecko = engine == EngineType.GECKOVIEW

    WtaToggleRow(
        title = Strings.audioMixingTitle,
        subtitle = when {
            usesGecko && geckoAvailable == false -> Strings.engineGeckoNotDownloaded
            usesGecko && echEnabled -> Strings.audioMixingEchRequired
            else -> Strings.audioMixingDesc.ifDescriptionsShown()
        },
        icon = Icons.Outlined.GraphicEq,
        checked = usesGecko,
        enabled = !usesGecko || !echEnabled,
        onCheckedChange = { enabled ->
            pendingEngine = if (enabled) EngineType.GECKOVIEW else EngineType.SYSTEM_WEBVIEW
        }
    )

    val nextEngine = pendingEngine ?: return
    val switchingToGecko = nextEngine == EngineType.GECKOVIEW
    val downloadNeeded = switchingToGecko && geckoAvailable == false
    val dismiss = { pendingEngine = null }
    WtaAlertDialog(
        onDismissRequest = dismiss,
        title = Strings.audioMixingTitle,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(WtaSpacing.ContentGap)) {
                Text(if (switchingToGecko) Strings.audioMixingEnableDesc else Strings.audioMixingDisableDesc)
                if (switchingToGecko) {
                    Text(Strings.engineApkSizeWarning.format(EngineType.GECKOVIEW.estimatedSizeMb))
                    if (downloadNeeded) Text(Strings.engineGeckoNotDownloaded)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !switchingToGecko || geckoAvailable != null,
                onClick = {
                    dismiss()
                    if (downloadNeeded) onOpenBrowserKernel() else onEngineSelected(nextEngine)
                }
            ) {
                Text(if (downloadNeeded) Strings.browserKernelTitle else Strings.confirm)
            }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text(Strings.cancel) } }
    )
}
