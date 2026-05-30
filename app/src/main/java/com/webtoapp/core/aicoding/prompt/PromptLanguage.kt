package com.webtoapp.core.aicoding.prompt

import com.webtoapp.core.i18n.AppLanguage

enum class PromptLang { EN, ZH }

fun AppLanguage.toPromptLang(): PromptLang = when (this) {
    AppLanguage.CHINESE -> PromptLang.ZH
    else -> PromptLang.EN
}
