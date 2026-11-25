package com.webtoapp.ui.screens

import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebStorage
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.scraper.OfflinePackPreviewSite
import com.webtoapp.ui.design.WtaAlertDialog
import com.webtoapp.ui.viewmodel.OfflinePackDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OfflinePackPreviewScreen(
    draft: OfflinePackDraft,
    onClose: () -> Unit,
    onSave: () -> Unit
) {
    val site = remember(draft) { OfflinePackPreviewSite(draft.result) }
    val missing by site.missing.collectAsStateWithLifecycle()
    var browser by remember { mutableStateOf<WebView?>(null) }
    var showMissing by remember { mutableStateOf(false) }
    val back = {
        val view = browser
        if (view?.canGoBack() == true) view.goBack() else onClose()
    }
    BackHandler(onBack = back)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.offlinePackPreviewTitle) },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, Strings.back)
                    }
                },
                actions = {
                    TextButton(onClick = onSave) { Text(Strings.btnSave) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainer) {
                if (missing.isEmpty()) {
                    Text(Strings.offlinePackNoMissingResources, Modifier.padding(12.dp))
                } else {
                    TextButton(onClick = { showMissing = true }) {
                        Text(Strings.offlinePackMissingResources.format(missing.size))
                    }
                }
            }

            AndroidView(
                modifier = Modifier.fillMaxWidth().weight(1f),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.javaScriptCanOpenWindowsAutomatically = false
                        settings.cacheMode = WebSettings.LOAD_NO_CACHE
                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                                site.intercept(request.url, request.method)

                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                if (site.allowsNavigation(request.url)) return false
                                site.noteMissing(request.url)
                                return true
                            }
                        }
                        browser = this
                        loadUrl(site.entryUrl)
                    }
                },
                onRelease = { view ->
                    browser = null
                    view.stopLoading()
                    view.destroy()
                    WebStorage.getInstance().deleteOrigin(site.origin)
                }
            )
        }
    }
    if (showMissing) {
        WtaAlertDialog(
            onDismissRequest = { showMissing = false },
            title = Strings.offlinePackMissingResources.format(missing.size),
            content = {
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(missing.sorted()) { address ->
                        Text(address, Modifier.padding(vertical = 6.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMissing = false }) { Text(Strings.close) }
            }
        )
    }
}
