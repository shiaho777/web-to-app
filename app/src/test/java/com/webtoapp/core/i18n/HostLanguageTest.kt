package com.webtoapp.core.i18n

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HostLanguageTest {

    @Test
    fun `language selection publishes the confirmed language from the same settings update`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = LanguageManager.getInstance(context)

        manager.setLanguage(AppLanguage.CHINESE)
        assertThat(manager.selectedLanguageFlow.first()).isEqualTo(AppLanguage.CHINESE)

        manager.setLanguage(AppLanguage.ARABIC)
        assertThat(manager.selectedLanguageFlow.first()).isEqualTo(AppLanguage.ARABIC)
    }

    @Test
    fun `apply publishes the chosen language before any activity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val original = Strings.currentLanguage.value
        try {
            Strings.setLanguage(AppLanguage.CHINESE)
            HostLanguage.apply(context, AppLanguage.ENGLISH)
            assertThat(Strings.currentLanguage.value).isEqualTo(AppLanguage.ENGLISH)
            assertThat(Strings.pressAgainToExit).isEqualTo("Press back again to exit")
        } finally {
            Strings.setLanguage(original)
        }
    }

    @Test
    fun `applyBlocking reads the saved language`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val original = Strings.currentLanguage.value
        try {
            runBlocking {
                LanguageManager.getInstance(context).setLanguage(AppLanguage.ENGLISH)
            }
            Strings.setLanguage(AppLanguage.CHINESE)
            HostLanguage.applyBlocking(context)
            assertThat(Strings.currentLanguage.value).isEqualTo(AppLanguage.ENGLISH)
            assertThat(Strings.pressAgainToExit).isEqualTo("Press back again to exit")
        } finally {
            Strings.setLanguage(original)
        }
    }
}
