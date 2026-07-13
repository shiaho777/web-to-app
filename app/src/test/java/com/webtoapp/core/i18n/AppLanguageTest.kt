package com.webtoapp.core.i18n

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `fromCode resolves supported languages and defaults to chinese`() {
        assertThat(AppLanguage.fromCode("zh")).isEqualTo(AppLanguage.CHINESE)
        assertThat(AppLanguage.fromCode("en")).isEqualTo(AppLanguage.ENGLISH)
        assertThat(AppLanguage.fromCode("ar")).isEqualTo(AppLanguage.ARABIC)
        assertThat(AppLanguage.fromCode("pt")).isEqualTo(AppLanguage.PORTUGUESE)
        assertThat(AppLanguage.fromCode("es")).isEqualTo(AppLanguage.SPANISH)
        assertThat(AppLanguage.fromCode("fr")).isEqualTo(AppLanguage.FRENCH)
        assertThat(AppLanguage.fromCode("de")).isEqualTo(AppLanguage.GERMAN)
        assertThat(AppLanguage.fromCode("ru")).isEqualTo(AppLanguage.RUSSIAN)
        assertThat(AppLanguage.fromCode("ja")).isEqualTo(AppLanguage.JAPANESE)
        assertThat(AppLanguage.fromCode("ko")).isEqualTo(AppLanguage.KOREAN)
        assertThat(AppLanguage.fromCode("unknown")).isEqualTo(AppLanguage.CHINESE)
    }

    @Test
    fun `language metadata includes rtl flag and locale information`() {
        assertThat(AppLanguage.ARABIC.isRtl).isTrue()
        assertThat(AppLanguage.CHINESE.isRtl).isFalse()
        assertThat(AppLanguage.ENGLISH.locale.language).isEqualTo("en")
    }
}
