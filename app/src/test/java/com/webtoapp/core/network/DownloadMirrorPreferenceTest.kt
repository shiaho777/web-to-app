package com.webtoapp.core.network

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.i18n.AppLanguage
import com.webtoapp.core.network.DownloadMirrorPreference.Mode
import java.util.Locale
import org.junit.Test

class DownloadMirrorPreferenceTest {
    @Test
    fun `explicit download mode overrides app and system locale`() {
        assertThat(DownloadMirrorPreference.prefersChina(Mode.CN, AppLanguage.ENGLISH, Locale.US)).isTrue()
        assertThat(DownloadMirrorPreference.prefersChina(Mode.GLOBAL, AppLanguage.CHINESE, Locale.CHINA)).isFalse()
    }

    @Test
    fun `automatic mode considers app language and system locale`() {
        assertThat(DownloadMirrorPreference.prefersChina(Mode.AUTO, AppLanguage.CHINESE, Locale.US)).isTrue()
        assertThat(DownloadMirrorPreference.prefersChina(Mode.AUTO, AppLanguage.ENGLISH, Locale.CHINA)).isTrue()
        assertThat(DownloadMirrorPreference.prefersChina(Mode.AUTO, AppLanguage.ENGLISH, Locale.forLanguageTag("en-HK"))).isTrue()
        assertThat(DownloadMirrorPreference.prefersChina(Mode.AUTO, AppLanguage.ENGLISH, Locale.US)).isFalse()
    }
}
