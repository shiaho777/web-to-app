package com.webtoapp.ui.shell

internal fun injectTranslateScript(webView: android.webkit.WebView, targetLanguage: String, showButton: Boolean) {
    val translateScript = runCatching {
        val clazz = Class.forName("com.webtoapp.ui.shell.TranslateScriptProvider")
        clazz.getMethod("build", String::class.java, java.lang.Boolean.TYPE)
            .invoke(null, targetLanguage, showButton) as? String
    }.getOrNull()
    if (!translateScript.isNullOrBlank()) {
        webView.evaluateJavascript(translateScript, null)
    }
}
