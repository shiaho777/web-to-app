package com.webtoapp.core.i18n

object PreviewHtmlSupport {

    fun escapeText(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun htmlLang(): String = when (Strings.currentLanguage.value) {
        AppLanguage.CHINESE -> "zh"
        AppLanguage.ENGLISH -> "en"
        AppLanguage.ARABIC -> "ar"
    }
}
