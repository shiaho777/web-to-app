package com.webtoapp.ui.shell

import com.webtoapp.core.feature.ReflectInvoke

internal fun injectTranslateScript(webView: android.webkit.WebView, targetLanguage: String, showButton: Boolean) {
    val translateScript = ReflectInvoke.callString(
        "com.webtoapp.ui.shell.TranslateScriptProvider",
        "build",
        targetLanguage,
        showButton
    )
    if (!translateScript.isNullOrBlank()) {
        webView.evaluateJavascript(translateScript, null)
    }
}
